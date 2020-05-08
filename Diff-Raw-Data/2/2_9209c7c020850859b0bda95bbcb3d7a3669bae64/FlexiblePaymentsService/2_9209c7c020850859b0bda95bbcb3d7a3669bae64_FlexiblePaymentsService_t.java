 package com.xerox.amazonws.fps;
 
 import com.xerox.amazonws.common.AWSError;
 import com.xerox.amazonws.common.AWSException;
 import com.xerox.amazonws.common.AWSQueryConnection;
 import com.xerox.amazonws.sdb.DataUtils;
 import com.xerox.amazonws.typica.fps.jaxb.*;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.bind.JAXBException;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.text.Collator;
 
 /**
  * This class provides an interface with the Amazon FPS service.
  *
  * @author J. Bernard
  * @author Elastic Grid, LLC.
  * @author jerome.bernard@elastic-grid.com
  */
 public class FlexiblePaymentsService extends AWSQueryConnection {
     private final String callerToken;
     private final String recipientToken;
     private final DescriptorPolicy descriptorPolicy;
     private final TemporaryDeclinePolicy tempDeclinePolicy;
     private final String uiPipeline;
     private static Log logger = LogFactory.getLog(FlexiblePaymentsService.class);
 
     /**
      * Initializes the FPS service with your AWS login information.
      *
      * @param awsAccessId           the your user key into AWS
      * @param awsSecretKey          the secret string used to generate signatures for authentication.
      */
     public FlexiblePaymentsService(String awsAccessId, String awsSecretKey) {
         this(awsAccessId, awsSecretKey, true, null, null, null, null);
     }
 
 
     /**
      * Initializes the FPS service with your AWS login information.
      *
      * @param awsAccessId           the your user key into AWS
      * @param awsSecretKey          the secret string used to generate signatures for authentication.
      * @param callerToken           the default caller token to be used when not explicitely specified
      * @param recipientToken        the default recipient token to be used when not explicitely specified
      * @param descriptorPolicy      the descriptor policy to use as descriptive string on credit card statements
      * @param tempDeclinePolicy     the temporary decline policy and the retry time out (in minutes)
      */
     public FlexiblePaymentsService(String awsAccessId, String awsSecretKey,
                                    String callerToken, String recipientToken,
                                    DescriptorPolicy descriptorPolicy, TemporaryDeclinePolicy tempDeclinePolicy) {
         this(awsAccessId, awsSecretKey, true, callerToken, recipientToken, descriptorPolicy, tempDeclinePolicy);
     }
 
     /**
      * Initializes the FPS service with your AWS login information.
      *
      * @param awsAccessId           the your user key into AWS
      * @param awsSecretKey          the secret string used to generate signatures for authentication.
      * @param isSecure              true if the data should be encrypted on the wire on the way to or from FPS.
      * @param callerToken           the default caller token to be used when not explicitely specified
      * @param recipientToken        the default recipient token to be used when not explicitely specified
      * @param descriptorPolicy      the descriptor policy to use as descriptive string on credit card statements
      * @param tempDeclinePolicy     the temporary decline policy and the retry time out (in minutes)
      */
     public FlexiblePaymentsService(String awsAccessId, String awsSecretKey, boolean isSecure,
                                    String callerToken, String recipientToken,
                                    DescriptorPolicy descriptorPolicy, TemporaryDeclinePolicy tempDeclinePolicy) {
         this(awsAccessId, awsSecretKey, isSecure, callerToken, recipientToken, descriptorPolicy, tempDeclinePolicy,
                 "fps.amazonaws.com", "https://authorize.payments.amazon.com/cobranded-ui/actions/start");
     }
 
     /**
      * Initializes the FPS service with your AWS login information.
      *
      * @param awsAccessId  The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
      * @param isSecure     True if the data should be encrypted on the wire on the way to or from FPS.
      * @param callerToken  the default caller token to be used when not explicitely specified
      * @param recipientToken the default recipient token to be used when not explicitely specified
      * @param descriptorPolicy the descriptor policy to use as descriptive string on credit card statements
      * @param tempDeclinePolicy     the temporary decline policy and the retry time out (in minutes)
      * @param server       Which host to connect to.  Usually, this will be fps.amazonaws.com.
      *                     You can also use fps.sandbox.amazonaws.com instead if you want to test your code within the Sandbox environment
      * @param uiPipeline   the URL of the UI pipeline
      */
     public FlexiblePaymentsService(String awsAccessId, String awsSecretKey, boolean isSecure,
                                    String callerToken, String recipientToken,
                                    DescriptorPolicy descriptorPolicy, TemporaryDeclinePolicy tempDeclinePolicy,
                                    String server, String uiPipeline) {
         this(awsAccessId, awsSecretKey, isSecure, callerToken, recipientToken, descriptorPolicy, tempDeclinePolicy,
                 server, isSecure ? 443 : 80, uiPipeline);
     }
 
     /**
      * Initializes the FPS service with your AWS login information.
      *
      * @param awsAccessId           the your user key into AWS
      * @param awsSecretKey          the secret string used to generate signatures for authentication.
      * @param isSecure              true if the data should be encrypted on the wire on the way to or from FPS.
      * @param callerToken           the default caller token to be used when not explicitely specified
      * @param recipientToken        the default recipient token to be used when not explicitely specified
      * @param descriptorPolicy      the descriptor policy to use as descriptive string on credit card statements
      * @param tempDeclinePolicy     the temporary decline policy and the retry time out (in minutes)
      * @param server                which host to connect to.  Usually, this will be fps.amazonaws.com.
      *                              You can also use fps.sandbox.amazonaws.com instead if you want to test your code within the Sandbox environment
      * @param port                  which port to use
      * @param uiPipeline            the URL of the UI pipeline
      */
     public FlexiblePaymentsService(String awsAccessId, String awsSecretKey, boolean isSecure,
                                    String callerToken, String recipientToken,
                                    DescriptorPolicy descriptorPolicy, TemporaryDeclinePolicy tempDeclinePolicy,
                                    String server, int port, String uiPipeline) {
         super(awsAccessId, awsSecretKey, isSecure, server, port);
         if (callerToken != null && !"".equals(callerToken) && callerToken.length() != 64)
             throw new IllegalArgumentException("The caller token must have a length of 64 bytes! Invalid value: " + callerToken);
         if (recipientToken != null && !"".equals(recipientToken) && recipientToken.length() != 64)
             throw new IllegalArgumentException("The caller token must have a length of 64 bytes! Invalid value: " + recipientToken);
         this.uiPipeline = uiPipeline;
         this.callerToken = "".equals(callerToken) ? null : callerToken;
         this.recipientToken = "".equals(recipientToken) ? null : recipientToken;
         this.descriptorPolicy = descriptorPolicy;
         this.tempDeclinePolicy = tempDeclinePolicy;
         setVersionHeader(this);
         setSignatureVersion(1);
     }
 
     /**
      * This method returns the signature version
      *
      * @return the version
      */
     public int getSignatureVersion() {
         return 1;
     }
 
     /**
      * Cancel any token that you installed on your own account.
      *
      * @param tokenID the token to be cancelled
      * @throws FPSException wraps checked exceptions
      */
     public void cancelToken(String tokenID) throws FPSException {
         cancelToken(tokenID, "");
     }
 
     /**
      * Cancel any token that you installed on your own account.
      *
      * @param tokenID the token to be cancelled
      * @param reason  reason for cancelling the token -- max 64 characters
      * @throws FPSException wraps checked exceptions
      */
     public void cancelToken(String tokenID, String reason) throws FPSException {
         if (tokenID == null || tokenID.length() != 64)
             throw new IllegalArgumentException("The token must have a length of 64 bytes");
         Map<String, String> params = new HashMap<String, String>();
         params.put("TokenId", tokenID);
         params.put("ReasonText", reason);
         GetMethod method = new GetMethod();
         try {
             makeRequestInt(method, "CancelToken", params, CancelTokenResponse.class);
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Discard the results that are fetched using the {@link #getResults()} operation.
      *
      * @param transactionIDs the list of transaction to be discarded
      * @throws FPSException wraps checked exceptions
      */
     public void discardResults(String... transactionIDs) throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         for (int i = 0; i < transactionIDs.length; i++)
             params.put("TransactionID." + i, transactionIDs[i]);
         GetMethod method = new GetMethod();
         try {
             makeRequestInt(method, "DiscardResults", params, DiscardResultsResponse.class);
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Transfer money from the sender's payment instrument specified in the funding token to the recipient's account
      * balance. This operation creates a prepaid balance on the sender' prepaid instrument.
      * Note: there is no support for <tt>NewSenderTokenUsage</tt> yet.
      *
      * @param senderTokenID       the token identifying the funding payment instructions
      * @param prepaidInstrumentID the prepaid instrument ID returned by the prepaid instrument installation pipeline
      * @param fundingAmount       amount to fund the prepaid instrument
      * @param callerReference     a unique reference that you specify in your system to identify a transaction
      * @return the completed transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction fundPrepaid(String senderTokenID, String prepaidInstrumentID,
                                    double fundingAmount, String callerReference) throws FPSException {
         return fundPrepaid(senderTokenID, callerToken, prepaidInstrumentID,
                 fundingAmount, new Date(),
                 null, null, callerReference,
                 ChargeFeeTo.RECIPIENT,
                 null, null, null,
                 null, descriptorPolicy, tempDeclinePolicy
         );
     }
 
     /**
      * Transfer money from the sender's payment instrument specified in the funding token to the recipient's account
      * balance. This operation creates a prepaid balance on the sender' prepaid instrument.
      * Note: there is no support for <tt>NewSenderTokenUsage</tt> yet.
      *
      * @param senderTokenID        the token identifying the funding payment instructions
      * @param callerTokenID        the caller's token ID
      * @param prepaidInstrumentID  the prepaid instrument ID returned by the prepaid instrument installation pipeline
      * @param fundingAmount        amount to fund the prepaid instrument
      * @param transactionDate      the date specified by the caller and stored with the transaction
      * @param senderReference      any reference that the caller might use to identify the sender in the transaction
      * @param recipientReference   any reference that the caller might use to identify the recipient in the transaction
      * @param callerReference      a unique reference that you specify in your system to identify a transaction
      * @param chargeFeeTo          the participant paying the fee for the transaction
      * @param senderDescription    128-byte field to store transaction description
      * @param recipientDescription 128-byte field to store transaction description
      * @param callerDescription    128-byte field to store transaction description
      * @param metadata             a 2KB free-form field used to store transaction data
      * @param descriptorPolicy       the soft descriptor type and the customer service number to pass to the payment processor
      * @param tempDeclinePolicy      the temporary decline policy and the retry time out (in minutes)
      * @return the completed transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction fundPrepaid(String senderTokenID, String callerTokenID, String prepaidInstrumentID,
                                    double fundingAmount, Date transactionDate,
                                    String senderReference, String recipientReference, String callerReference,
                                    ChargeFeeTo chargeFeeTo,
                                    String senderDescription, String recipientDescription, String callerDescription,
                                    String metadata,
                                    DescriptorPolicy descriptorPolicy, TemporaryDeclinePolicy tempDeclinePolicy)
             throws FPSException {
         if (callerTokenID == null || callerTokenID.length() != 64)
             throw new IllegalArgumentException("The token must have a length of 64 bytes");
         Map<String, String> params = new HashMap<String, String>();
         params.put("SenderTokenId", senderTokenID);
         params.put("CallerTokenId", callerTokenID);
         params.put("PrepaidInstrumentId", prepaidInstrumentID);
         params.put("FundingAmount", Double.toString(fundingAmount));
         params.put("TransactionDate", DataUtils.encodeDate(transactionDate));
         if (senderReference != null)
             params.put("SenderReference", senderReference);
         if (recipientReference != null)
             params.put("RecipientReference", recipientReference);
         params.put("CallerReference", callerReference);
         params.put("ChargeFeeTo", chargeFeeTo.value());
         params.put("ChargeFeeTo", chargeFeeTo.value());
         if (senderDescription != null)
             params.put("SenderDescription", senderDescription);
         if (recipientDescription != null)
             params.put("RecipientDescription", recipientDescription);
         if (callerDescription != null)
             params.put("CallerDescription", callerDescription);
         if (metadata != null)
             params.put("MetaData", metadata);
         if (descriptorPolicy != null) {
             params.put("SoftDescriptorType", descriptorPolicy.getSoftDescriptorType().value());
             params.put("CSNumberOf", descriptorPolicy.getCSNumberOf().value());
         }
         if (tempDeclinePolicy != null) {
             params.put("TemporaryDeclinePolicy.TemporaryDeclinePolicyType", tempDeclinePolicy.getTemporaryDeclinePolicyType().value());
             params.put("ImplicitRetryTimeoutInMins", Integer.toString(tempDeclinePolicy.getImplicitRetryTimeoutInMins()));
         }
         GetMethod method = new GetMethod();
         try {
             FundPrepaidResponse response =
                     makeRequestInt(method, "FundPrepaid", params, FundPrepaidResponse.class);
             TransactionResponse transactionResponse = response.getTransactionResponse();
             return new Transaction(
                     transactionResponse.getTransactionId(),
                     Transaction.Status.valueOf(transactionResponse.getStatus().value()),
                     transactionResponse.getStatusDetail()
                     // todo: transactionResponse.getNewSenderTokenUsage()
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     public AccountActivity getAccountActivity(Date startDate) throws FPSException {
         return getAccountActivity(null, null, 0, startDate, null, null, null);
     }
 
     public AccountActivity getAccountActivity(Date startDate, Date endDate) throws FPSException {
         return getAccountActivity(null, null, 0, startDate, endDate, null, null);
     }
 
     /**
      * Retrieve transactions from an account for a given time period.
      *
      * @param filter operation type filter -- use null if this filter shouldn't be used
      * @param paymentMethod payment method filter -- use null if this filter shouldn't be used
      * @param maxBatchSize maximum number of transactions to be returned<br/>
      *                     {@link AccountActivity} is {@link Iterable} over each page of the paginated results from
      *                     the underneath FPS operation.
      * @param startDate will filter transactions beginning from that date
      * @param endDate will filter transactions ending up to that date
      * @param role role filter -- use null if this filter shouldn't be used
      * @param transactionStatus transaction status filter -- use null if this filter shouldn't be used 
      * @return the account activity matching the filters
      * @throws FPSException wraps checked exceptions
      */
     public AccountActivity getAccountActivity(FPSOperation filter, PaymentMethod paymentMethod, int maxBatchSize,
                                               Date startDate, Date endDate,
                                               TransactionalRoleFilter role, Transaction.Status transactionStatus)
             throws FPSException {
         if (startDate == null)
             throw new IllegalArgumentException("The start date should not be null!");
         Map<String, String> params = new HashMap<String, String>();
         if (filter != null)
             params.put("Operation", filter.value());
         if (paymentMethod != null)
             params.put("PaymentMethod", paymentMethod.value());
         if (maxBatchSize != 0)
             params.put("MaxBatchSize", Integer.toString(maxBatchSize));
         params.put("StartDate", DataUtils.encodeDate(startDate));
         if (endDate != null)
             params.put("EndDate", DataUtils.encodeDate(endDate));
         if (role != null)
             params.put("Role", role.value());
         if (transactionStatus != null)
             params.put("Status", transactionStatus.value());
         GetMethod method = new GetMethod();
         try {
             GetAccountActivityResponse response =
                     makeRequestInt(method, "GetAccountActivity", params, GetAccountActivityResponse.class);
             Date nextStartDate = null;
             if (response.getStartTimeForNextTransaction() != null)
                 nextStartDate = response.getStartTimeForNextTransaction().toGregorianCalendar().getTime();
             BigInteger nbTransactions = response.getResponseBatchSize();
             List<com.xerox.amazonws.typica.fps.jaxb.Transaction> rawTransactions = response.getTransactions();
             List<Transaction> transactions = new ArrayList<Transaction>(rawTransactions.size());
             for (com.xerox.amazonws.typica.fps.jaxb.Transaction txn : rawTransactions) {
                 com.xerox.amazonws.typica.fps.jaxb.Amount txnAmount = txn.getTransactionAmount();
                 com.xerox.amazonws.typica.fps.jaxb.Amount fees = txn.getFees();
                 com.xerox.amazonws.typica.fps.jaxb.Amount balance = txn.getBalance();
                 transactions.add(new Transaction(
                         txn.getTransactionId(), Transaction.Status.fromValue(txn.getStatus().value()),
                         txn.getDateReceived().toGregorianCalendar().getTime(),
                         txn.getDateCompleted().toGregorianCalendar().getTime(),
                         new Amount(new BigDecimal(txnAmount.getAmount()), txnAmount.getCurrencyCode().toString()),
                         FPSOperation.fromValue(txn.getOperation().value()),
                         PaymentMethod.fromValue(txn.getPaymentMethod().value()),
                         txn.getSenderName(), txn.getCallerName(), txn.getRecipientName(),
                         new Amount(new BigDecimal(fees.getAmount()), fees.getCurrencyCode().toString()),
                         new Amount(new BigDecimal(balance.getAmount()), balance.getCurrencyCode().toString()),
                         txn.getCallerTokenId(), txn.getSenderTokenId(), txn.getRecipientTokenId()
                 ));
             }
             return new AccountActivity(nextStartDate, nbTransactions, transactions,
                     filter, paymentMethod, maxBatchSize, endDate, transactionStatus, this);
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Retrieve all the prepaid instruments associated with your account
      * @return the list of prepaid instruments
      * @throws FPSException wraps checked exceptions
      */
     public List<String> getAllPrepaidInstruments() throws FPSException {
         return getAllPrepaidInstruments(null);
     }
 
     /**
      * Retrieve all the prepaid instruments associated with your account
      *
      * @param instrumentStatus filter instruments by status
      * @return the list of prepaid instruments
      * @throws FPSException wraps checked exceptions
      */
     public List<String> getAllPrepaidInstruments(Instrument.Status instrumentStatus) throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         if (instrumentStatus != null)
             params.put("InstrumentStatus", instrumentStatus.value());
         GetMethod method = new GetMethod();
         try {
             GetAllPrepaidInstrumentsResponse response =
                     makeRequestInt(method, "GetAllPrepaidInstruments", params, GetAllPrepaidInstrumentsResponse.class);
             return response.getPrepaidInstrumentIds();
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Retrieve all credit instruments associated with an account.
      *
      * @return the list of credit instruments IDs associated with the account
      * @throws FPSException wraps checked exceptions
      */
     public List<String> getAllCreditInstruments() throws FPSException {
         return getAllCreditInstruments(null);
     }
 
     /**
      * Retrieve all credit instruments associated with an account.
      *
      * @return the list of credit instruments balances associated with the account
      * @throws FPSException wraps checked exceptions
      */
     public List<DebtBalance> getAllCreditInstrumentBalances() throws FPSException {
         return getAllCreditInstrumentBalances(null);
     }
 
     /**
      * Retrieve all credit instruments associated with an account.
      *
      * @param instrumentStatus filter instruments by status
      * @return the list of credit instruments IDs associated with the account
      * @throws FPSException wraps checked exceptions
      */
     public List<String> getAllCreditInstruments(Instrument.Status instrumentStatus) throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         if (instrumentStatus != null)
             params.put("InstrumentStatus", instrumentStatus.value());
         GetMethod method = new GetMethod();
         try {
             GetAllCreditInstrumentsResponse response =
                     makeRequestInt(method, "GetAllCreditInstruments", params, GetAllCreditInstrumentsResponse.class);
             return response.getCreditInstrumentIds();
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Retrieve all credit instruments associated with an account.
      *
      * @param instrumentStatus filter instruments by status
      * @return the list of credit instruments balances associated with the account
      * @throws FPSException wraps checked exceptions
      */
     public List<DebtBalance> getAllCreditInstrumentBalances(Instrument.Status instrumentStatus) throws FPSException {
         List<String> creditInstruments = getAllCreditInstruments(instrumentStatus);
         List<DebtBalance> balances = new ArrayList<DebtBalance>(creditInstruments.size());
         for (String instrument : creditInstruments)
             balances.add(getDebtBalance(instrument));
         return balances;
     }
 
     /**
      * Retrieve the balance of a credit instrument.
      * Note: only on the instruments for which you are the sender or the recipient can be queried
      *
      * @param creditInstrumentId the credit instrument Id for which debt balance is queried
      * @return the balance
      * @throws FPSException wraps checked exceptions
      */
     public DebtBalance getDebtBalance(String creditInstrumentId) throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         params.put("CreditInstrumentId", creditInstrumentId);
         GetMethod method = new GetMethod();
         try {
             GetDebtBalanceResponse response =
                     makeRequestInt(method, "GetDebtBalance", params, GetDebtBalanceResponse.class);
             com.xerox.amazonws.typica.fps.jaxb.DebtBalance balance = response.getDebtBalance();
             com.xerox.amazonws.typica.fps.jaxb.Amount availableBalance = balance.getAvailableBalance();
             com.xerox.amazonws.typica.fps.jaxb.Amount pendingOutBalance = balance.getPendingOutBalance();
             return new DebtBalance(
                     new Amount(new BigDecimal(availableBalance.getAmount()), availableBalance.getCurrencyCode().toString()),
                     new Amount(new BigDecimal(pendingOutBalance.getAmount()), pendingOutBalance.getCurrencyCode().toString())
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Retrieve balances of all credit instruments owned by the sender.
      * Note: only on the instruments for which you are the sender or the recipient can be queried
      *
      * @return the aggregated balance
      * @throws FPSException wraps checked exceptions
      */
     public DebtBalance getOutstandingDebtBalance() throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         GetMethod method = new GetMethod();
         try {
             GetOutstandingDebtBalanceResponse response =
                     makeRequestInt(method, "GetOutstandingDebtBalance", params, GetOutstandingDebtBalanceResponse.class);
             OutstandingDebtBalance balance = response.getOutstandingDebt();
             com.xerox.amazonws.typica.fps.jaxb.Amount outstanding = balance.getOutstandingBalance();
             com.xerox.amazonws.typica.fps.jaxb.Amount pendingOut = balance.getPendingOutBalance();
             return new DebtBalance(
                     new Amount(new BigDecimal(outstanding.getAmount()), outstanding.getCurrencyCode().toString()),
                     new Amount(new BigDecimal(pendingOut.getAmount()), pendingOut.getCurrencyCode().toString())
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Retrieve the details of a payment instruction.
      *
      * @param tokenID token for which the payment instruction is to be retrieved
      * @return a 64-character alphanumeric string that represents the installed payment instruction
      * @throws FPSException wraps checked exceptions
      */
     public PaymentInstructionDetail getPaymentInstruction(String tokenID) throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         params.put("TokenId", tokenID);
         GetMethod method = new GetMethod();
         try {
             GetPaymentInstructionResponse response =
                     makeRequestInt(method, "GetPaymentInstruction", params, GetPaymentInstructionResponse.class);
             Token token = new Token(
                     response.getToken().getTokenId(),
                     response.getToken().getFriendlyName(),
                     Token.Status.fromValue(response.getToken().getStatus().value()),
                     response.getToken().getDateInstalled().toGregorianCalendar().getTime(),
                     response.getToken().getCallerInstalled(),
                     TokenType.fromValue(response.getToken().getTokenType().value()),
                     response.getToken().getOldTokenId(),
                     response.getToken().getPaymentReason()
             );
             return new PaymentInstructionDetail(token, response.getPaymentInstruction(),
                     response.getAccountId(), response.getTokenFriendlyName());
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Retrieve the balance of a prepaid instrument.
      * Note: only on the instruments for which you are the sender or the recipient can be queried
      *
      * @param prepaidInstrumentId prepaid instrument for which the balance is queried
      * @return the balance
      * @throws FPSException wraps checked exceptions
      */
     public PrepaidBalance getPrepaidBalance(String prepaidInstrumentId) throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         params.put("PrepaidInstrumentId", prepaidInstrumentId);
         GetMethod method = new GetMethod();
         try {
             GetPrepaidBalanceResponse response =
                     makeRequestInt(method, "GetPrepaidBalance", params, GetPrepaidBalanceResponse.class);
             com.xerox.amazonws.typica.fps.jaxb.PrepaidBalance balance = response.getPrepaidBalance();
             com.xerox.amazonws.typica.fps.jaxb.Amount availableBalance = balance.getAvailableBalance();
             com.xerox.amazonws.typica.fps.jaxb.Amount pendingOutBalance = balance.getPendingInBalance();
             return new PrepaidBalance(
                     new Amount(new BigDecimal(availableBalance.getAmount()), availableBalance.getCurrencyCode().toString()),
                     new Amount(new BigDecimal(pendingOutBalance.getAmount()), pendingOutBalance.getCurrencyCode().toString())
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * This operation is used to poll for transaction results that are returned asynchronously.
      *
      * @throws FPSException wraps checked exceptions
      */
     public List<TransactionResult> getResults() throws FPSException {
         return getResults(null, null);
     }
 
     /**
      * This operation is used to poll for transaction results that are returned asynchronously.
      *
      * @param operation Used to filter results based on the operation type (e.g. Pay, Refund, Settle, SettleDebt, WriteOffDebt, FundPrepaid)
      * @param maxResultsCount Used to specify the maximum results that can be retrieved. The minimum value is 1 and the maximum value is 25. By default the maximum or the available results are returned.
      * @return the list of transactions
      * @throws FPSException wraps checked exceptions
      */
     public List<TransactionResult> getResults(FPSOperationFilter operation, Integer maxResultsCount) throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         if (operation != null)
             params.put("Operation", operation.toString());
         if (maxResultsCount != null)
             params.put("MaxResultsCount", maxResultsCount.toString());
         GetMethod method = new GetMethod();
         try {
             GetResultsResponse response =
                     makeRequestInt(method, "GetResults", params, GetResultsResponse.class);
             List<com.xerox.amazonws.typica.fps.jaxb.TransactionResult> rawTransactions = response.getTransactionResults();
             List<TransactionResult> transactionResults = new ArrayList<TransactionResult>(rawTransactions.size());
             for (com.xerox.amazonws.typica.fps.jaxb.TransactionResult txn : rawTransactions) {
                 transactionResults.add(new TransactionResult(
                         txn.getTransactionId(),
                         FPSOperation.fromValue(txn.getOperation().value()),
                         txn.getCallerReference(),
                         Transaction.Status.fromValue(txn.getStatus().value())
                 ));
             }
             return transactionResults;
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Fetch all the tokens installed on your (caller) account.
      *
      * @return the list of tokens
      * @throws FPSException wraps checked exceptions
      */
     public List<Token> getAllTokens() throws FPSException {
         return getTokens(null, null, null);
     }
 
     /**
      * Fetch the tokens installed on your (caller) account, filtered by friendly name.
      *
      * @param tokenFriendlyName filter by friendly name
      * @return the list of tokens
      * @throws FPSException wraps checked exceptions
      */
     public List<Token> getTokensByFriendlyName(String tokenFriendlyName) throws FPSException {
         return getTokens(tokenFriendlyName, null, null);
     }
 
     /**
      * Fetch the tokens installed on your (caller) account, filtered by status.
      *
      * @param tokenStatus filter by token status
      * @return the list of tokens
      * @throws FPSException wraps checked exceptions
      */
     public List<Token> getTokensByStatus(Token.Status tokenStatus) throws FPSException {
         return getTokens(null, tokenStatus, null);
     }
 
     /**
      * Fetch the tokens installed on your (caller) account, filtered by caller reference.
      *
      * @param callerReference filter by caller reference
      * @return the list of tokens
      * @throws FPSException wraps checked exceptions
      */
     public List<Token> getTokensByCallerReference(String callerReference) throws FPSException {
         return getTokens(null, null, callerReference);
     }
 
     /**
      * Fetch the tokens installed on your (caller) account, based on the filtering parameters.
      * A null parameter means to NOT filter on that parameter.
      *
      * @param tokenFriendlyName filter by friendly name
      * @param tokenStatus filter by token status
      * @param callerReference filter by caller reference
      * @return the list of tokens
      * @throws FPSException wraps checked exceptions
      */
     public List<Token> getTokens(String tokenFriendlyName, Token.Status tokenStatus, String callerReference) throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         if (tokenFriendlyName != null)
             params.put("TokenFriendlyName", tokenFriendlyName);
         if (tokenStatus != null)
             params.put("TokenStatus", tokenStatus.value());
         if (callerReference != null)
             params.put("CallerReference", callerReference);
         GetMethod method = new GetMethod();
         try {
             GetTokensResponse response =
                     makeRequestInt(method, "GetTokens", params, GetTokensResponse.class);
             List<com.xerox.amazonws.typica.fps.jaxb.Token> rawTokens = response.getTokens();
             List<Token> tokens = new ArrayList<Token>(rawTokens.size());
             for (com.xerox.amazonws.typica.fps.jaxb.Token token : rawTokens) {
                 tokens.add(new Token(
                     token.getTokenId(),
                     token.getFriendlyName(),
                     Token.Status.fromValue(token.getStatus().value()),
                     token.getDateInstalled().toGregorianCalendar().getTime(),
                     token.getCallerInstalled(),
                     TokenType.fromValue(token.getTokenType().value()),
                     token.getOldTokenId(),
                     token.getPaymentReason()
                 ));
             }
             return tokens;
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Fetch the details of a particular token.
      *
      * @param tokenID the token Id of the specific token installed on the callers account
      * @return the token
      * @throws FPSException wraps checked exceptions
      */
     public Token getTokenByID(String tokenID) throws FPSException {
         return getToken(tokenID, null);
     }
 
     /**
      * Fetch the details of a particular token.
      *
      * @param callerReference the caller reference that was passed at the time of the token installation
      * @return the token
      * @throws FPSException wraps checked exceptions
      */
     public Token getTokenByCaller(String callerReference) throws FPSException {
         return getToken(null, callerReference);
     }
 
     /**
      * Fetch the details of a particular token.
      *
      * @param tokenID the token Id of the specific token installed on the callers account
      * @param callerReference the caller reference that was passed at the time of the token installation
      * @return the token
      * @throws FPSException wraps checked exceptions
      */
     private Token getToken(String tokenID, String callerReference) throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         if (tokenID == null && callerReference == null)
             throw new IllegalArgumentException("Either the token ID or the caller reference must be given!");
         if (tokenID != null && tokenID.length() != 64)
             throw new IllegalArgumentException("The token must have a length of 64 bytes");
         if (tokenID != null)
             params.put("TokenId", tokenID);
         if (callerReference != null)
             params.put("CallerReference", callerReference);
         GetMethod method = new GetMethod();
         try {
             GetTokenByCallerResponse response =
                     makeRequestInt(method, "GetTokenByCaller", params, GetTokenByCallerResponse.class);
             return new Token(
                     response.getToken().getTokenId(),
                     response.getToken().getFriendlyName(),
                     Token.Status.fromValue(response.getToken().getStatus().value()),
                     response.getToken().getDateInstalled().toGregorianCalendar().getTime(),
                     response.getToken().getCallerInstalled(),
                     TokenType.fromValue(response.getToken().getTokenType().value()),
                     response.getToken().getOldTokenId(),
                     response.getToken().getPaymentReason()
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Fetch the details and usage of a multi-use token.
      * Note: the usage limit is returned only for the multi-use token and not for the single-use token
      * @param tokenID the token for which the usage is queried
      * @return the list of available usage limits on the token
      * @throws FPSException wraps checked exceptions
      */
     public List<TokenUsageLimit> getTokenUsage(String tokenID) throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         if (tokenID == null)
             throw new IllegalArgumentException("Either the token ID or the caller reference must be given!");
         if (tokenID.length() != 64)
             throw new IllegalArgumentException("The token must have a length of 64 bytes");
         params.put("TokenId", tokenID);
         GetMethod method = new GetMethod();
         try {
             GetTokenUsageResponse response =
                     makeRequestInt(method, "GetTokenUsage", params, GetTokenUsageResponse.class);
             List<TokenUsageLimit> limits = new ArrayList<TokenUsageLimit>(response.getTokenUsageLimits().size());
             for (com.xerox.amazonws.typica.fps.jaxb.TokenUsageLimit limit : response.getTokenUsageLimits()) {
                 limits.add(new TokenUsageLimit(
                         limit.getCount(),
                         new Amount(
                                 new BigDecimal(limit.getAmount().getAmount()),
                                 limit.getAmount().getCurrencyCode().value()
                         ),
                         limit.getLastResetCount(),
                         new Amount(
                                 new BigDecimal(limit.getLastResetAmount().getAmount()),
                                 limit.getLastResetAmount().getCurrencyCode().value()
                         ),
                         limit.getLastResetTimeStamp().toGregorianCalendar().getTime()
                 ));
             }
             return limits;
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Returns the total liability held by the recipient corresponding to all the prepaid instruments.
      * @return the total liability
      * @throws FPSException wraps checked exceptions
      */
     public OutstandingPrepaidLiability getTotalPrepaidLiability() throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         GetMethod method = new GetMethod();
         try {
             GetTotalPrepaidLiabilityResponse response =
                     makeRequestInt(method, "GetTotalPrepaidLiability", params, GetTotalPrepaidLiabilityResponse.class);
             com.xerox.amazonws.typica.fps.jaxb.OutstandingPrepaidLiability liability = response.getOutstandingPrepaidLiability();
             return new OutstandingPrepaidLiability(
                     new Amount(
                             new BigDecimal(liability.getOutstandingBalance().getAmount()),
                             liability.getOutstandingBalance().getCurrencyCode().value()
                     ),
                     new Amount(
                             new BigDecimal(liability.getPendingInBalance().getAmount()),
                             liability.getPendingInBalance().getCurrencyCode().value()
                     )
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Fetch details of a transaction referred by the <tt>transactionId</tt>.
      *
      * @param transactionID a transaction Id for the query
      * @return the transaction
      * @throws FPSException wraps checked exceptions
      */
     public TransactionDetail getTransaction(String transactionID) throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         params.put("TransactionId", transactionID);
         GetMethod method = new GetMethod();
         try {
             GetTransactionResponse response =
                     makeRequestInt(method, "GetTransaction", params, GetTransactionResponse.class);
             com.xerox.amazonws.typica.fps.jaxb.TransactionDetail txn = response.getTransaction();
             return new TransactionDetail(
                     txn.getTransactionId(),
                     txn.getCallerTransactionDate().toGregorianCalendar().getTime(),
                     txn.getDateReceived().toGregorianCalendar().getTime(),
                     txn.getDateCompleted().toGregorianCalendar().getTime(),
                     new Amount(
                             new BigDecimal(txn.getTransactionAmount().getAmount()),
                             txn.getTransactionAmount().getCurrencyCode().value()
                     ),
                     new Amount(
                             new BigDecimal(txn.getFees().getAmount()),
                             txn.getFees().getCurrencyCode().value()
                     ),
                     txn.getCallerTokenId(), txn.getSenderTokenId(), txn.getRecipientTokenId(),
                     txn.getPrepaidInstrumentId(), txn.getCreditInstrumentId(),
                     FPSOperation.fromValue(txn.getOperation().value()),
                     PaymentMethod.fromValue(txn.getPaymentMethod().value()),
                     Transaction.Status.fromValue(txn.getStatus().value()),
                     txn.getErrorCode(), txn.getErrorMessage(), txn.getMetaData(),
                     txn.getSenderName(), txn.getCallerName(), txn.getRecipientName()
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Install unrestricted caller token on your own accounts.
      *
      * @param callerReference    a unique reference to the payment instructions. This is used to recover or retrieve
      *                           tokens that are lost or not received after the payment instruction is installed
      * @return a 64-character alphanumeric string that represents the installed payment instruction
      * @throws FPSException wraps checked exceptions
      */
     public String installUnrestrictedCallerPaymentInstruction(String callerReference)
             throws FPSException {
         return installPaymentInstruction("MyRole == 'Caller' orSay 'Role does not match';",
                 callerReference, callerReference, TokenType.UNRESTRICTED, callerReference);
     }
 
     /**
      * Install unrestricted recipient token on your own accounts.
      *
      * @param callerReference    a unique reference to the payment instructions. This is used to recover or retrieve
      *                           tokens that are lost or not received after the payment instruction is installed
      * @return a 64-character alphanumeric string that represents the installed payment instruction
      * @throws FPSException wraps checked exceptions
      */
     public String installUnrestrictedRecipientPaymentInstruction(String callerReference)
             throws FPSException {
         return installPaymentInstruction("MyRole == 'Recipient' orSay 'Role does not match';",
                 callerReference, callerReference, TokenType.UNRESTRICTED, callerReference);
     }
 
     /**
      * Install tokens (payment instructions) on your own accounts.
      *
      * @param paymentInstruction set of rules in the GateKeeper language format to be installed on the caller's account
      * @param callerReference    a unique reference to the payment instructions. This is used to recover or retrieve
      *                           tokens that are lost or not received after the payment instruction is installed
      * @param type               the type of token
      * @return a 64-character alphanumeric string that represents the installed payment instruction
      * @throws FPSException wraps checked exceptions
      */
     public String installPaymentInstruction(String paymentInstruction, String callerReference, TokenType type)
             throws FPSException {
         return installPaymentInstruction(paymentInstruction, null, callerReference, type, null);
     }
 
     /**
      * Install tokens (payment instructions) on your own accounts.
      *
      * @param paymentInstruction set of rules in the GateKeeper language format to be installed on the caller's account
      * @param tokenFriendlyName  a human-friendly, readable name for the payment instruction
      * @param callerReference    a unique reference to the payment instructions. This is used to recover or retrieve
      *                           tokens that are lost or not received after the payment instruction is installed
      * @param type               the type of token
      * @param comment             the reason for making the payment
      * @return a 64-character alphanumeric string that represents the installed payment instruction
      * @throws FPSException wraps checked exceptions
      */
     public String installPaymentInstruction(String paymentInstruction, String tokenFriendlyName, String callerReference,
                                             TokenType type, String comment) throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         params.put("PaymentInstruction", paymentInstruction);
         if (tokenFriendlyName != null)
             params.put("TokenFriendlyName", tokenFriendlyName);
         params.put("CallerReference", callerReference);
         params.put("TokenType", type.value());
         if (comment != null)
             params.put("PaymentReason", comment);
         GetMethod method = new GetMethod();
         try {
             InstallPaymentInstructionResponse response =
                     makeRequestInt(method, "InstallPaymentInstruction", params, InstallPaymentInstructionResponse.class);
             return response.getTokenId();
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Initiate a transaction to move funds from the sender to the recipient.
      *
      * @param senderToken            sender token
      * @param amount                 amount to be charged to the sender
      * @param callerReference        a unique reference that you specify in your system to identify a transaction
      * @return                       the transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction pay(String senderToken, Amount amount, String callerReference)
             throws FPSException {
         return pay(recipientToken, senderToken, callerToken, amount, new Date(), ChargeFeeTo.RECIPIENT, callerReference,
                 null, null, null, null, null, null, 0, 0, descriptorPolicy);
     }
 
     /**
      * Initiate a transaction to move funds from the sender to the recipient.
      *
      * @param senderToken            sender token
      * @param amount                 amount to be charged to the sender
      * @param callerReference        a unique reference that you specify in your system to identify a transaction
      * @param descriptorPolicy       the soft descriptor type and the customer service number to pass to the payment processor
      * @return                       the transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction pay(String senderToken, Amount amount, String callerReference, DescriptorPolicy descriptorPolicy)
             throws FPSException {
         return pay(recipientToken, senderToken, callerToken, amount, new Date(), ChargeFeeTo.RECIPIENT, callerReference,
                 null, null, null, null, null, null, 0, 0, descriptorPolicy);
     }
 
     /**
      * Initiate a transaction to move funds from the sender to the recipient.
      *
      * @param recipientToken         recipient token
      * @param senderToken            sender token
      * @param callerToken            caller token
      * @param amount                 amount to be charged to the sender
      * @param transactionDate        the date specified by the caller and stored with the transaction
      * @param chargeFeeTo            the participant paying the fee for the transaction
      * @param callerReference        a unique reference that you specify in your system to identify a transaction
      * @param senderReference        any reference that the caller might use to identify the sender in the transaction
      * @param recipientReference     any reference that the caller might use to identify the recipient in the transaction
      * @param senderDescription      128-byte field to store transaction description
      * @param recipientDescription   128-byte field to store transaction description
      * @param callerDescription      128-byte field to store transaction description
      * @param metadata               a 2KB free-form field used to store transaction data
      * @param marketplaceFixedFee    the fee charged by the marketplace developer as a fixed amount of the transaction
      * @param marketplaceVariableFee the fee charged by the marketplace developer as a variable amount of the transaction
      * @param descriptorPolicy       the soft descriptor type and the customer service number to pass to the payment processor
      * @return                       the transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction pay(String recipientToken, String senderToken, String callerToken, Amount amount,
                            Date transactionDate, ChargeFeeTo chargeFeeTo,
                            String callerReference, String senderReference, String recipientReference,
                            String senderDescription, String recipientDescription, String callerDescription,
                            String metadata, double marketplaceFixedFee, int marketplaceVariableFee,
                            DescriptorPolicy descriptorPolicy)
             throws FPSException {
         return pay(recipientToken, senderToken, callerToken, amount, transactionDate, chargeFeeTo,
                 callerReference, senderReference, recipientReference,
                 senderDescription, recipientDescription, callerDescription,
                 metadata, marketplaceFixedFee, marketplaceVariableFee,
                 descriptorPolicy, tempDeclinePolicy);
     }
 
     /**
      * Initiate a transaction to move funds from the sender to the recipient.
      *
      * @param recipientToken         recipient token
      * @param senderToken            sender token
      * @param callerToken            caller token
      * @param amount                 amount to be charged to the sender
      * @param transactionDate        the date specified by the caller and stored with the transaction
      * @param chargeFeeTo            the participant paying the fee for the transaction
      * @param callerReference        a unique reference that you specify in your system to identify a transaction
      * @param senderReference        any reference that the caller might use to identify the sender in the transaction
      * @param recipientReference     any reference that the caller might use to identify the recipient in the transaction
      * @param senderDescription      128-byte field to store transaction description
      * @param recipientDescription   128-byte field to store transaction description
      * @param callerDescription      128-byte field to store transaction description
      * @param metadata               a 2KB free-form field used to store transaction data
      * @param marketplaceFixedFee    the fee charged by the marketplace developer as a fixed amount of the transaction
      * @param marketplaceVariableFee the fee charged by the marketplace developer as a variable amount of the transaction
      * @param descriptorPolicy       the soft descriptor type and the customer service number to pass to the payment processor
      * @param tempDeclinePolicy      the temporary decline policy and the retry time out (in minutes)
      * @return                       the transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction pay(String recipientToken, String senderToken, String callerToken, Amount amount,
                            Date transactionDate, ChargeFeeTo chargeFeeTo,
                            String callerReference, String senderReference, String recipientReference,
                            String senderDescription, String recipientDescription, String callerDescription,
                            String metadata, double marketplaceFixedFee, int marketplaceVariableFee,
                            DescriptorPolicy descriptorPolicy, TemporaryDeclinePolicy tempDeclinePolicy)
             throws FPSException {
         if (recipientToken != null && recipientToken.length() != 64)
             throw new IllegalArgumentException("The recipient token must have a length of 64 bytes");
         if (senderToken != null && senderToken.length() != 64)
             throw new IllegalArgumentException("The sender token must have a length of 64 bytes");
         if (callerToken != null && callerToken.length() != 64)
             throw new IllegalArgumentException("The caller token must have a length of 64 bytes");
         if (logger.isInfoEnabled())
             logger.info("Payment: " + senderToken + " paying " + recipientToken + " for " + amount);
         Map<String, String> params = new HashMap<String, String>();
         if (recipientToken != null)
             params.put("RecipientTokenId", recipientToken);
         params.put("SenderTokenId", senderToken);
         params.put("CallerTokenId", callerToken);
         params.put("TransactionAmount.Amount", Double.toString(amount.getAmount().doubleValue()));
         params.put("TransactionAmount.CurrencyCode", amount.getCurrencyCode());
         if (transactionDate != null)
             params.put("TransactionDate", DataUtils.encodeDate(transactionDate));
         params.put("ChargeFeeTo", chargeFeeTo.value());
         params.put("CallerReference", callerReference);
         if (senderReference != null)
             params.put("SenderReference", senderReference);
         if (recipientReference != null)
             params.put("RecipientReference", recipientReference);
         if (senderDescription != null)
             params.put("SenderDescription", senderDescription);
         if (recipientDescription != null)
             params.put("RecipientDescription", recipientDescription);
         if (callerDescription != null)
             params.put("CallerDescription", callerDescription);
         if (metadata != null)
             params.put("MetaData", metadata);
         if (marketplaceFixedFee != 0)
             params.put("MarketplaceFixedFee", Double.toString(marketplaceFixedFee));
         if (marketplaceVariableFee != 0)
             params.put("MarketplaceVariableFee", Integer.toString(marketplaceVariableFee));
         if (descriptorPolicy != null) {
             params.put("SoftDescriptorType", descriptorPolicy.getSoftDescriptorType().value());
             params.put("CSNumberOf", descriptorPolicy.getCSNumberOf().value());
         }
         if (tempDeclinePolicy != null) {
             params.put("TemporaryDeclinePolicy.TemporaryDeclinePolicyType", tempDeclinePolicy.getTemporaryDeclinePolicyType().value());
            params.put("TemporaryDeclinePolicy.ImplicitRetryTimeoutInMins", Integer.toString(tempDeclinePolicy.getImplicitRetryTimeoutInMins()));
         }
         GetMethod method = new GetMethod();
         try {
             PayResponse response =
                     makeRequestInt(method, "Pay", params, PayResponse.class);
             TransactionResponse transactionResponse = response.getTransactionResponse();
             return new Transaction(
                     transactionResponse.getTransactionId(),
                     Transaction.Status.fromValue(transactionResponse.getStatus().value()),
                     transactionResponse.getStatusDetail()
                     // todo: transactionResponse.getNewSenderTokenUsage()
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Refund a successfully completed payment transaction.
      *
      * @param senderToken token of the original recipient who is now the sender in the refund
      * @param transactionID the transaction that is to be refunded
      * @param callerReference a unique reference that identifies this refund
      * @return the refund transaction
      * @throws FPSException FPSException wraps checked exceptions
      */
     public Transaction refund(String senderToken, String transactionID, String callerReference) throws FPSException {
         return refund(callerToken, senderToken, transactionID, null, ChargeFeeTo.RECIPIENT, new Date(),
                 callerReference, null, null, null, null, null, null, null);
     }
 
     /**
      * Refund a successfully completed payment transaction.
      *
      * @param callerToken the caller token
      * @param senderToken token of the original recipient who is now the sender in the refund
      * @param transactionID the transaction that is to be refunded<
      * @param refundAmount the amount to be refunded<br/>
      *                     If this value is not specified, then the remaining funds from the original transaction
      *                     is refunded.
      * @param chargeFeeTo the participant who pays the fee<br/>
      *                    Currently Amazon FPS does not charge any fee for the refund and this has no impact on
      *                    the transaction
      * @param transactionDate the date of the transaction from the caller
      * @param callerReference a unique reference that identifies this refund
      * @param senderReference the reference created by the recipient of original transaction for this refund transaction
      * @param recipientReference the reference created by the Sender (of the original transaction) for this refund transaction
      * @param senderDescription a 128-byte field to store transaction description
      * @param recipientDescription a 128-byte field to store transaction description
      * @param callerDescription a 128-byte field to store transaction description
      * @param metadata a 2KB free form field used to store transaction data
      * @param policy the refund choice: refund the master transaction, the marketplace fee, or both
      * @return the refund transaction
      * @throws FPSException FPSException wraps checked exceptions
      */
     public Transaction refund(String callerToken, String senderToken, String transactionID, Amount refundAmount,
                        ChargeFeeTo chargeFeeTo, Date transactionDate,
                        String callerReference, String senderReference, String recipientReference,
                        String senderDescription, String recipientDescription, String callerDescription,
                        String metadata, MarketplaceRefundPolicy policy) throws FPSException {
         if (callerToken == null || callerToken.length() != 64)
             throw new IllegalArgumentException("The caller token must have a length of 64 bytes");
         if (logger.isInfoEnabled())
             logger.info("Refund: " + senderToken + " refunding transaction " + transactionID + " for " + refundAmount);
         Map<String, String> params = new HashMap<String, String>();
         params.put("CallerTokenId", callerToken);
         params.put("RefundSenderTokenId", senderToken);
         params.put("TransactionId", transactionID);
         if (refundAmount != null) {
             params.put("RefundAmount.Amount", refundAmount.getAmount().toString());
             params.put("RefundAmount.CurrencyCode", refundAmount.getCurrencyCode());
         }
         params.put("ChargeFeeTo", chargeFeeTo.value());
         if (transactionDate != null)
             params.put("TransactionDate", DataUtils.encodeDate(transactionDate));
         params.put("CallerReference", callerReference);
         if (senderReference != null)
             params.put("RefundSenderReference", senderReference);
         if (recipientReference != null)
             params.put("RefundRecipientReference", recipientReference);
         if (senderDescription != null)
             params.put("RefundSenderDescription", senderDescription);
         if (recipientDescription != null)
             params.put("RefundRecipientDescription", recipientDescription);
         if (callerDescription != null)
             params.put("CallerDescription", callerDescription);
         if (metadata != null)
             params.put("MetaData", metadata);
         if (policy != null)
             params.put("MarketplaceRefundPolicy", policy.value());
         GetMethod method = new GetMethod();
         try {
             RefundResponse response =
                     makeRequestInt(method, "Refund", params, RefundResponse.class);
             TransactionResponse transactionResponse = response.getTransactionResponse();
             return new Transaction(
                     transactionResponse.getTransactionId(),
                     Transaction.Status.fromValue(transactionResponse.getStatus().value()),
                     transactionResponse.getStatusDetail()
                     // todo: transactionResponse.getNewSenderTokenUsage()
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * This operation is part of the Reserve and Settle operations that allow payment transactions when the
      * authorization and settlement have a time difference. The transaction is not complete until the Settle
      * operation is executed successfully. A reserve authorization is only valid for 7 days.
      * Currently, you can't cancel a reserve.
      *
      * @param senderToken            sender token
      * @param amount                 amount to be reserved on the sender account/credit card
      * @param callerReference        a unique reference that you specify in your system to identify a transaction
      * @return                       the transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction reserve(String senderToken, Amount amount, String callerReference)
             throws FPSException {
         return reserve(recipientToken, senderToken, callerToken, amount, new Date(), ChargeFeeTo.RECIPIENT, callerReference,
                 null, null, null, null, null, null, 0, 0, descriptorPolicy, tempDeclinePolicy);
     }    
 
     /**
      * This operation is part of the Reserve and Settle operations that allow payment transactions when the
      * authorization and settlement have a time difference. The transaction is not complete until the Settle
      * operation is executed successfully. A reserve authorization is only valid for 7 days.
      * Currently, you can't cancel a reserve.
      *
      * @param recipientToken         recipient token
      * @param senderToken            sender token
      * @param callerToken            caller token
      * @param amount                 amount to be reserved on the sender account/credit card
      * @param transactionDate        the date specified by the caller and stored with the transaction
      * @param chargeFeeTo            the participant paying the fee for the transaction
      * @param callerReference        a unique reference that you specify in your system to identify a transaction
      * @param senderReference        any reference that the caller might use to identify the sender in the transaction
      * @param recipientReference     any reference that the caller might use to identify the recipient in the transaction
      * @param senderDescription      128-byte field to store transaction description
      * @param recipientDescription   128-byte field to store transaction description
      * @param callerDescription      128-byte field to store transaction description
      * @param metadata               a 2KB free-form field used to store transaction data
      * @param marketplaceFixedFee    the fee charged by the marketplace developer as a fixed amount of the transaction
      * @param marketplaceVariableFee the fee charged by the marketplace developer as a variable amount of the transaction
      * @param descriptorPolicy       the soft descriptor type and the customer service number to pass to the payment processor
      * @param tempDeclinePolicy      the temporary decline policy and the retry time out (in minutes)
      * @return                       the transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction reserve(String recipientToken, String senderToken, String callerToken, Amount amount,
                            Date transactionDate, ChargeFeeTo chargeFeeTo,
                            String callerReference, String senderReference, String recipientReference,
                            String senderDescription, String recipientDescription, String callerDescription,
                            String metadata, double marketplaceFixedFee, int marketplaceVariableFee,
                            DescriptorPolicy descriptorPolicy, TemporaryDeclinePolicy tempDeclinePolicy)
             throws FPSException {
         if (recipientToken == null || recipientToken.length() != 64)
             throw new IllegalArgumentException("The recipient token must have a length of 64 bytes");
         if (senderToken == null || senderToken.length() != 64)
             throw new IllegalArgumentException("The sender token must have a length of 64 bytes");
         if (callerToken == null || callerToken.length() != 64)
             throw new IllegalArgumentException("The caller token must have a length of 64 bytes");
         if (logger.isInfoEnabled())
             logger.info("Reserve: " + recipientToken + " reserving " + senderToken  + " for " + amount);
         Map<String, String> params = new HashMap<String, String>();
         params.put("RecipientTokenId", recipientToken);
         params.put("SenderTokenId", senderToken);
         params.put("CallerTokenId", callerToken);
         params.put("TransactionAmount.Amount", Double.toString(amount.getAmount().doubleValue()));
         params.put("TransactionAmount.CurrencyCode", amount.getCurrencyCode());
         if (transactionDate != null)
             params.put("TransactionDate", DataUtils.encodeDate(transactionDate));
         params.put("ChargeFeeTo", chargeFeeTo.value());
         params.put("CallerReference", callerReference);
         if (senderReference != null)
             params.put("SenderReference", senderReference);
         if (recipientReference != null)
             params.put("RecipientReference", recipientReference);
         if (senderDescription != null)
             params.put("SenderDescription", senderDescription);
         if (recipientDescription != null)
             params.put("RecipientDescription", recipientDescription);
         if (callerDescription != null)
             params.put("CallerDescription", callerDescription);
         if (metadata != null)
             params.put("MetaData", metadata);
         if (marketplaceFixedFee != 0)
             params.put("MarketplaceFixedFee", Double.toString(marketplaceFixedFee));
         if (marketplaceVariableFee != 0)
             params.put("MarketplaceVariableFee", Integer.toString(marketplaceVariableFee));
         if (descriptorPolicy != null) {
             params.put("SoftDescriptorType", descriptorPolicy.getSoftDescriptorType().value());
             params.put("CSNumberOf", descriptorPolicy.getCSNumberOf().value());
         }
         if (tempDeclinePolicy != null) {
             params.put("TemporaryDeclinePolicy.TemporaryDeclinePolicyType", tempDeclinePolicy.getTemporaryDeclinePolicyType().value());
             params.put("ImplicitRetryTimeoutInMins", Integer.toString(tempDeclinePolicy.getImplicitRetryTimeoutInMins()));
         }
         GetMethod method = new GetMethod();
         try {
             ReserveResponse response = makeRequestInt(method, "Reserve", params, ReserveResponse.class);
             TransactionResponse transactionResponse = response.getTransactionResponse();
             return new Transaction(
                     transactionResponse.getTransactionId(),
                     Transaction.Status.fromValue(transactionResponse.getStatus().value()),
                     transactionResponse.getStatusDetail()
                     // todo: transactionResponse.getNewSenderTokenUsage()
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Submits a transaction for processing.
      * If a transaction was temporarily declined, the transaction can be processed again using the original transaction ID.
      * 
      * @param transactionID the transaction to retry
      * @return              the transaction
      * @throws FPSException wraps checked exceptions
      */
      public Transaction retryTransaction(String transactionID) throws FPSException {
         if (transactionID == null || transactionID.length() == 0 || transactionID.length() > 35)
             throw new IllegalArgumentException("The transaction ID must not be null/empty and has a max size of 35 bytes");
         if (logger.isInfoEnabled())
             logger.info("Retry tranasction: " + transactionID);
         Map<String, String> params = new HashMap<String, String>();
         params.put("OriginalTransactionId", transactionID);
         GetMethod method = new GetMethod();
         try {
             RetryTransactionResponse response = makeRequestInt(method, "RetryTransaction", params, RetryTransactionResponse.class);
             TransactionResponse transactionResponse = response.getTransactionResponse();
             return new Transaction(
                     transactionResponse.getTransactionId(),
                     Transaction.Status.fromValue(transactionResponse.getStatus().value()),
                     transactionResponse.getStatusDetail()
                     // todo: transactionResponse.getNewSenderTokenUsage()
             );
         } finally {
             method.releaseConnection();
         }
      }
 
     /**
      * Settles fully or partially the amount that is reserved using the {@link #reserve} operation
      *
      * @param reserveTransactionID   the transaction ID of the reserve transaction that has to be settled
      * @param amount                 amount to be settled
      * @return                       the transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction settle(String reserveTransactionID, Amount amount) throws FPSException {
         return settle(reserveTransactionID, amount, null);
     }
 
     /**
      * Settles fully or partially the amount that is reserved using the {@link #reserve} operation
      *
      * @param reserveTransactionID   the transaction ID of the reserve transaction that has to be settled
      * @param amount                 amount to be settled
      * @param transactionDate        the date of the transaction
      * @return                       the transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction settle(String reserveTransactionID, Amount amount, Date transactionDate) throws FPSException {
         if (reserveTransactionID == null || reserveTransactionID.length() == 0 || reserveTransactionID.length() > 35)
             throw new IllegalArgumentException("The reserve transaction ID must not be null/empty and has a max size of 35 bytes");
         if (logger.isInfoEnabled())
             logger.info("Settle: " + reserveTransactionID + " for " + amount);
         Map<String, String> params = new HashMap<String, String>();
         params.put("ReserveTransactionId", reserveTransactionID);
         params.put("TransactionAmount.Amount", Double.toString(amount.getAmount().doubleValue()));
         params.put("TransactionAmount.CurrencyCode", amount.getCurrencyCode());
         if (transactionDate != null)
             params.put("TransactionDate", DataUtils.encodeDate(transactionDate));
         GetMethod method = new GetMethod();
         try {
             SettleResponse response = makeRequestInt(method, "Settle", params, SettleResponse.class);
             TransactionResponse transactionResponse = response.getTransactionResponse();
             return new Transaction(
                     transactionResponse.getTransactionId(),
                     Transaction.Status.fromValue(transactionResponse.getStatus().value()),
                     transactionResponse.getStatusDetail()
                     // todo: transactionResponse.getNewSenderTokenUsage()
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * The SettleDebt operation takes the settlement amount, credit instrument, and the settlement token among other
      * parameters. Using this operation you can:
      * <ul>
      * <li>
      * Transfer money from sender's payment instrument specified in the settlement token to the recipient's
      * account balance. The fee charged is deducted from the settlement amount and deposited into recipient's
      * account balance.
      * </li>
      * <li>
      * Decrement debt balances by the settlement amount.
      * </li>
      * </ul>
      * @param settlementToken the token ID of the settlement token
      * @param creditInstrument the credit instrument Id returned by the co-branded UI pipeline
      * @param amount the amount for the settlement
      * @param callerReference a unique reference that you specify in your system to identify a transaction
      * @return the transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction settleDebt(String settlementToken, String creditInstrument, Amount amount,
                                   String callerReference)
             throws FPSException {
         return settleDebt(settlementToken, callerToken, creditInstrument, amount, new Date(), null, null, callerReference,
                 ChargeFeeTo.RECIPIENT, null, null, null, null, descriptorPolicy, tempDeclinePolicy);
     }
 
     /**
      * The SettleDebt operation takes the settlement amount, credit instrument, and the settlement token among other
      * parameters. Using this operation you can:
      * <ul>
      * <li>
      * Transfer money from sender's payment instrument specified in the settlement token to the recipient's
      * account balance. The fee charged is deducted from the settlement amount and deposited into recipient's
      * account balance.
      * </li>
      * <li>
      * Decrement debt balances by the settlement amount.
      * </li>
      * </ul>
      * @param settlementToken        the token ID of the settlement token
      * @param callerToken            the callers token
      * @param creditInstrument       the credit instrument Id returned by the co-branded UI pipeline
      * @param amount                 the amount for the settlement
      * @param transactionDate        the date of the callers transaction
      * @param senderReference        the unique value that will be used as a reference for the sender in this transaction
      * @param recipientReference     the unique value that will be used as a reference for the recipient in this transaction
      * @param callerReference        a unique reference that you specify in your system to identify a transaction
      * @param chargeFeeTo            the participant paying the fee for the transaction
      * @param senderDescription      a 128-byte field to store transaction description
      * @param recipientDescription   a 128-byte field to store transaction description
      * @param callerDescription      a 128-byte field to store transaction description
      * @param metadata               a 2KB free form field used to store transaction data
      * @param descriptorPolicy       the descriptor policy to use as descriptive string on credit card statements
      * @param tempDeclinePolicy      the temporary decline policy and the retry time out (in minutes)
      * @return the transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction settleDebt(String settlementToken, String callerToken,
                        String creditInstrument, Amount amount,
                        Date transactionDate, String senderReference, String recipientReference, String callerReference,
                        ChargeFeeTo chargeFeeTo,
                        String senderDescription, String recipientDescription, String callerDescription,
                        String metadata, DescriptorPolicy descriptorPolicy, TemporaryDeclinePolicy tempDeclinePolicy)
             throws FPSException {
         if (settlementToken == null || settlementToken.length() != 64)
             throw new IllegalArgumentException("The settlement token must have a length of 64 bytes");
         if (callerToken == null || callerToken.length() != 64)
             throw new IllegalArgumentException("The caller token must have a length of 64 bytes");
         Map<String, String> params = new HashMap<String, String>();
         params.put("SenderTokenId", settlementToken);
         params.put("CallerTokenId", callerToken);
         params.put("CreditInstrumentId", creditInstrument);
         params.put("SettlementAmount.Amount", Double.toString(amount.getAmount().doubleValue()));
         params.put("SettlementAmount.CurrencyCode", amount.getCurrencyCode());
         if (transactionDate != null)
             params.put("TransactionDate", DataUtils.encodeDate(transactionDate));
         if (senderReference != null)
             params.put("SenderReference", senderReference);
         if (recipientReference != null)
             params.put("RecipientReference", recipientReference);
         params.put("CallerReference", callerReference);
         params.put("ChargeFeeTo", chargeFeeTo.value());
         if (senderDescription != null)
             params.put("SenderDescription", senderDescription);
         if (recipientDescription != null)
             params.put("RecipientDescription", recipientDescription);
         if (callerDescription != null)
             params.put("CallerDescription", callerDescription);
         if (metadata != null)
             params.put("MetaData", metadata);
         if (descriptorPolicy != null) {
             params.put("SoftDescriptorType", descriptorPolicy.getSoftDescriptorType().value());
             params.put("CSNumberOf", descriptorPolicy.getCSNumberOf().value());
         }
         if (tempDeclinePolicy != null) {
             params.put("TemporaryDeclinePolicy.TemporaryDeclinePolicyType", tempDeclinePolicy.getTemporaryDeclinePolicyType().value());
             params.put("ImplicitRetryTimeoutInMins", Integer.toString(tempDeclinePolicy.getImplicitRetryTimeoutInMins()));
         }
         GetMethod method = new GetMethod();
         try {
             SettleDebtResponse response =
                     makeRequestInt(method, "SettleDebt", params, SettleDebtResponse.class);
             TransactionResponse transactionResponse = response.getTransactionResponse();
             return new Transaction(
                     transactionResponse.getTransactionId(),
                     Transaction.Status.fromValue(transactionResponse.getStatus().value()),
                     transactionResponse.getStatusDetail()
                     // todo: transactionResponse.getNewSenderTokenUsage()
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Allows callers to subscribe to events that are given out using the web service notification mechanism.
      * This operation is used for subscribing to the notifications provided to callers through web services.
      * Amazon FPS supports two events, Transaction results and token deletion that you can subscribe.
      * @param operationType specify the event types for which the notifications are required
      * @param webService the URL to your web service
      * @throws FPSException wraps checked exceptions
      */
     public void subscribeForCallerNotification(NotificationEventType operationType, URL webService) throws FPSException {
         if (operationType == null)
             throw new IllegalArgumentException("The notification operation name is required!");
         if (webService == null)
             throw new IllegalArgumentException("The Web Service API URL is required!");
         if (logger.isInfoEnabled())
             logger.info("Subscribe for caller notification for operations " + operationType + " at " + webService);
         Map<String, String> params = new HashMap<String, String>();
         params.put("NotificationOperationName", operationType.value());
         params.put("WebServiceAPIURLt", webService.toString());
         GetMethod method = new GetMethod();
         try {
             makeRequestInt(method, "SubscribeForCallerNotification", params, SubscribeForCallerNotificationResponse.class);
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Allows callers to unsubscribe to events that are previously subscribed by the calling applications.
      * @param operationType specify the event types for which the notifications are required
      * @throws FPSException wraps checked exceptions
      */
     public void unsubscribeForCallerNotification(NotificationEventType operationType) throws FPSException {
         if (operationType == null)
             throw new IllegalArgumentException("The notification operation name is required!");
         if (logger.isInfoEnabled())
             logger.info("Unsubscribe for caller notification for operations " + operationType);
         Map<String, String> params = new HashMap<String, String>();
         params.put("NotificationOperationName", operationType.value());
         GetMethod method = new GetMethod();
         try {
             makeRequestInt(method, "UnSubscribeForCallerNotification", params, UnSubscribeForCallerNotificationResponse.class);
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Write off the debt accumulated by the recipient on any credit instrument
      * @param creditInstrument the credit instrument Id returned by the co-branded UI pipeline
      * @param adjustmentAmount the amount for the settlement<br/>
      *                         if the <tt>adjustmentAmount</tt> is not a positive value,
      *                         a {@link IllegalArgumentException} is thrown
      * @param callerReference a unique reference that you specify in your system to identify a transaction
      * @return the transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction writeOffDebt(String creditInstrument, double adjustmentAmount, String callerReference)
             throws FPSException {
         return writeOffDebt(callerToken, creditInstrument, adjustmentAmount, new Date(),
                 callerReference, null, null, null, null, null, null);
     }
 
     /**
      * Write off the debt accumulated by the recipient on any credit instrument
      * @param callerToken the callers token
      * @param creditInstrument the credit instrument Id returned by the co-branded UI pipeline
      * @param adjustmentAmount the amount for the settlement<br/>
      *                         if the <tt>adjustmentAmount</tt> is not a positive value,
      *                         a {@link IllegalArgumentException} is thrown
      * @param transactionDate the date of the callers transaction
      * @param senderReference the unique value that will be used as a reference for the sender in this transaction
      * @param recipientReference the unique value that will be used as a reference for the recipient in this transaction
      * @param callerReference a unique reference that you specify in your system to identify a transaction
      * @param senderDescription a 128-byte field to store transaction description
      * @param recipientDescription a 128-byte field to store transaction description
      * @param callerDescription a 128-byte field to store transaction description
      * @param metadata a 2KB free form field used to store transaction data
      * @return the transaction
      * @throws FPSException wraps checked exceptions
      */
     public Transaction writeOffDebt(String callerToken, String creditInstrument, double adjustmentAmount,
                                     Date transactionDate,
                                     String callerReference, String recipientReference, String senderReference,
                                     String senderDescription, String recipientDescription, String callerDescription,
                                     String metadata)
             throws FPSException {
         if (callerToken == null || callerToken.length() != 64)
             throw new IllegalArgumentException("The caller token must have a length of 64 bytes");
         if (adjustmentAmount <= 0)
             throw new IllegalArgumentException("The adjustment amount should be a positive value");
         if (logger.isInfoEnabled())
             logger.info("Writing off debt instrument " + creditInstrument + " for an amount of " + adjustmentAmount);
         Map<String, String> params = new HashMap<String, String>();
         params.put("CallerTokenId", callerToken);
         params.put("CreditInstrumentId", creditInstrument);
         params.put("AdjustmentAmount.Amount", Double.toString(adjustmentAmount));
         params.put("AdjustmentAmount.CurrencyCode", "USD");
         if (transactionDate != null)
             params.put("TransactionDate", DataUtils.encodeDate(transactionDate));
         if (senderReference != null)
             params.put("SenderReference", senderReference);
         if (recipientReference != null)
             params.put("RecipientReference", recipientReference);
         params.put("CallerReference", callerReference);
         if (senderDescription != null)
             params.put("SenderDescription", senderDescription);
         if (recipientDescription != null)
             params.put("RecipientDescription", recipientDescription);
         if (callerDescription != null)
             params.put("CallerDescription", callerDescription);
         if (metadata != null)
             params.put("MetaData", metadata);
         GetMethod method = new GetMethod();
         try {
             WriteOffDebtResponse response =
                     makeRequestInt(method, "WriteOffDebt", params, WriteOffDebtResponse.class);
             TransactionResponse transactionResponse = response.getTransactionResponse();
             return new Transaction(
                     transactionResponse.getTransactionId(),
                     Transaction.Status.fromValue(transactionResponse.getStatus().value()),
                     transactionResponse.getStatusDetail()
                     // todo: transactionResponse.getNewSenderTokenUsage()
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Get the current balance on your account.
      *
      * @return the balance
      * @throws FPSException wraps checked exceptions
      */
     public AccountBalance getAccountBalance() throws FPSException {
         Map<String, String> params = new HashMap<String, String>();
         GetMethod method = new GetMethod();
         try {
             GetAccountBalanceResponse response =
                     makeRequestInt(method, "GetAccountBalance", params, GetAccountBalanceResponse.class);
             com.xerox.amazonws.typica.fps.jaxb.AccountBalance balance = response.getAccountBalance();
             com.xerox.amazonws.typica.fps.jaxb.Amount available = balance.getTotalBalance();
             com.xerox.amazonws.typica.fps.jaxb.Amount pendingIn = balance.getPendingInBalance();
             com.xerox.amazonws.typica.fps.jaxb.Amount pendingOut = balance.getPendingOutBalance();
             com.xerox.amazonws.typica.fps.jaxb.Amount disburse = balance.getAvailableBalances().getDisburseBalance();
             com.xerox.amazonws.typica.fps.jaxb.Amount refund = balance.getAvailableBalances().getRefundBalance();
             return new AccountBalance(
                     new Amount(new BigDecimal(available.getAmount()), available.getCurrencyCode().toString()),
                     new Amount(new BigDecimal(pendingIn.getAmount()), pendingIn.getCurrencyCode().toString()),
                     new Amount(new BigDecimal(pendingOut.getAmount()), pendingOut.getCurrencyCode().toString()),
                     new Amount(new BigDecimal(disburse.getAmount()), disburse.getCurrencyCode().toString()),
                     new Amount(new BigDecimal(refund.getAmount()), refund.getCurrencyCode().toString())
             );
         } finally {
             method.releaseConnection();
         }
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquireSingleUseToken(String callerReference, Amount amount, String returnURL, String reason)
             throws FPSException, MalformedURLException {
         return acquireSingleUseToken(callerReference, amount, false, null, null,
                 false, true, null,
                 null, null, null, null, null, null,
                 returnURL, reason);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquireSingleUseToken(String callerReference, Amount amount, PaymentMethod paymentMethod,
                                         String returnURL, String reason)
             throws FPSException, MalformedURLException {
         return acquireSingleUseToken(callerReference, amount, false, paymentMethod, null,
                 false, true, null,
                 null, null, null, null, null, null,
                 returnURL, reason);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquireSingleUseToken(String callerReference, Amount amount, boolean reserve,
                                         PaymentMethod paymentMethod, String recipientToken,
                                         Boolean isRecipientCobranding, Boolean collectShippingAddress, Address address,
                                         Amount itemTotal, Amount shipping, Amount handling, Boolean giftWrapping, Amount discount, Amount tax,
                                         String returnURL, String reason)
             throws FPSException, MalformedURLException {
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("callerReference", callerReference);
         parameters.put("transactionAmount", amount.getAmount().toString());
         parameters.put("currencyCode", amount.getCurrencyCode());
         if (paymentMethod != null)
             parameters.put("paymentMethod", paymentMethod.value());
         if (recipientToken != null)
             parameters.put("recipientToken", recipientToken);
         if (reason != null)
             parameters.put("paymentReason", reason);
         if (reserve)
             parameters.put("reserve", "True");
         if (isRecipientCobranding != null)
             parameters.put("isRecipientCobranding", isRecipientCobranding.toString());
         if (collectShippingAddress)
             parameters.put("collectShippingAddress", "True");
         if (address != null) {
             parameters.put("addressName", address.getName());
             parameters.put("addressLine1", address.getLine1());
             parameters.put("addressLine2", address.getLine2());
             parameters.put("city", address.getCity());
             parameters.put("zip", address.getZipCode());
         }
         if (itemTotal != null)
             parameters.put("itemTotal", itemTotal.getAmount().toString());
         if (shipping != null)
             parameters.put("shipping", shipping.getAmount().toString());
         if (handling != null)
             parameters.put("handling", handling.getAmount().toString());
         if (discount != null)
             parameters.put("discount", discount.getAmount().toString());
         if (tax != null)
             parameters.put("tax", tax.getAmount().toString());
         if (giftWrapping != null)
             parameters.put("giftWrapping", "True");
         return generateUIPipelineURL("SingleUse", returnURL, parameters);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquireMultiUseToken(String callerReference, Amount amount,
                                        Amount globalLimit, String returnURL, String reason)
     throws MalformedURLException, FPSException {
         return acquireMultiUseToken(callerReference, amount, null, null, globalLimit, null,
                 null, null, null,
                 null, null, null,
                 returnURL, reason);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquireMultiUseToken(String callerReference, Amount amount,
                                        List<String> recipientTokens, AmountType amountType, Amount globalLimit, List<UsageLimit> usageLimits,
                                        Boolean isRecipientCobranding, Boolean collectShippingAddress, Address address,
                                        Date validityStart, Date validityExpiry, PaymentMethod paymentMethod,
                                        String returnURL, String reason)
     throws FPSException, MalformedURLException {
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("callerReference", callerReference);
         parameters.put("currencyCode", amount.getCurrencyCode());
         parameters.put("transactionAmount", amount.getAmount().toString());
 
         if (reason != null)
             parameters.put("paymentReason", reason);
         if (recipientTokens != null && recipientTokens.size() > 0) {
             StringBuffer buffer = new StringBuffer();
             for (int i = 0; i < recipientTokens.size(); i++) {
                 String token = recipientTokens.get(i);
                 if (i > 0)
                     buffer.append(',');
                 buffer.append(token);
             }
             parameters.put("recipientTokenList", buffer.toString());
         }
         if (amountType != null)
             parameters.put("amountType", amountType.value());
         if (validityStart != null)
             parameters.put("validityStart", DataUtils.encodeDate(validityStart));
         if (validityExpiry != null)
             parameters.put("validityExpiry", DataUtils.encodeDate(validityExpiry));
         if (paymentMethod != null)
             parameters.put("paymentMethod", paymentMethod.value());
         if (usageLimits != null) {
             for (int i = 0; i < usageLimits.size(); i++) {
                 UsageLimit limit = usageLimits.get(i);
                 parameters.put("usageLimitType" + i, limit.getType().value());
                 if (limit.getPeriodicity() != null)
                     parameters.put("usageLimitPeriod" + i, limit.getPeriodicity().toString());
             }
         }
         if (isRecipientCobranding != null)
             parameters.put("isRecipientCobranding", isRecipientCobranding.toString());
         if (collectShippingAddress != null)
             parameters.put("collectShippingAddress", collectShippingAddress.toString());
         if (address != null) {
             parameters.put("addressName", address.getName());
             parameters.put("addressLine1", address.getLine1());
             parameters.put("addressLine2", address.getLine2());
             parameters.put("city", address.getCity());
             parameters.put("zip", address.getZipCode());
         }
         return generateUIPipelineURL("MultiUse", returnURL, parameters);
     }
 
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquireRecurringToken(String callerReference, Amount amount,
                                         int recurringInterval, RecurringGranularity recurringGranularity,
                                         String returnURL, String reason)
             throws MalformedURLException, FPSException {
         return acquireRecurringToken(callerReference, amount, recurringInterval, recurringGranularity,
                 null, null, null, null,
                 null, null, null,
                 returnURL, reason);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquireRecurringToken(String callerReference, Amount amount,
                                         int recurringInterval, RecurringGranularity recurringGranularity,
                                         Date validityStart, Date validityExpiry,
                                         PaymentMethod paymentMethod, String recipientToken,
                                         Boolean isRecipientCobranding, Boolean collectShippingAddress, Address address,
                                         String returnURL, String reason)
             throws FPSException, MalformedURLException {
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("callerReference", callerReference);
         parameters.put("transactionAmount", amount.getAmount().toString());
         parameters.put("currencyCode", amount.getCurrencyCode());
         if (paymentMethod != null)
             parameters.put("paymentMethod", paymentMethod.value());
         if (recipientToken != null)
             parameters.put("recipientToken", recipientToken);
         if (reason != null)
             parameters.put("paymentReason", reason);
         if (validityStart != null)
             parameters.put("validityStart", DataUtils.encodeDate(validityStart));
         if (validityExpiry != null)
             parameters.put("validityExpiry", DataUtils.encodeDate(validityExpiry));
         String recurringPeriod = Integer.toString(recurringInterval) + " " + recurringGranularity.getValue();
         parameters.put("recurringPeriod", recurringPeriod);
         if (isRecipientCobranding != null)
             parameters.put("isRecipientCobranding", isRecipientCobranding.toString());
         if (collectShippingAddress)
             parameters.put("collectShippingAddress", "True");
         if (address != null) {
             parameters.put("addressName", address.getName());
             parameters.put("addressLine1", address.getLine1());
             parameters.put("addressLine2", address.getLine2());
             parameters.put("city", address.getCity());
             parameters.put("zip", address.getZipCode());
         }
         return generateUIPipelineURL("Recurring", returnURL, parameters);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquireRecipientToken(String callerReference, Boolean recipientPaysFee,
                                         String returnURL, String reason)
             throws FPSException, MalformedURLException {
         return acquireRecipientToken(callerReference, null, null, null, recipientPaysFee,
                 null, null, null, returnURL, reason);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquireRecipientToken(String callerReference, Date validityStart, Date validityExpiry,
                                         PaymentMethod paymentMethod, Boolean recipientPaysFee,
                                         String callerReferenceRefund, Long maxVariableFee, Long maxFixedFee,
                                         String returnURL, String reason)
             throws FPSException, MalformedURLException {
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("callerReference", callerReference);
         parameters.put("recipientPaysFee", recipientPaysFee ? "True" : "False");
         if (validityStart != null)
             parameters.put("validityStart", DataUtils.encodeDate(validityStart));
         if (validityExpiry != null)
             parameters.put("validityExpiry", DataUtils.encodeDate(validityExpiry));
         if (paymentMethod != null)
             parameters.put("paymentMethod", paymentMethod.value());
         if (callerReferenceRefund != null)
             parameters.put("callerReferenceRefund", callerReferenceRefund);
         if (maxVariableFee != null)
             parameters.put("maxVariableFee", maxVariableFee.toString());
         if (maxFixedFee != null)
             parameters.put("maxFixedFee", maxFixedFee.toString());
         return generateUIPipelineURL("Recipient", returnURL, parameters);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquirePrepaidToken(String callerReferenceSender, String callerReferenceFunding, Amount amount,
                                       String returnURL, String reason)
             throws FPSException, MalformedURLException {
         return acquirePrepaidToken(callerReferenceSender, callerReferenceFunding, amount,
                 null, null, null, null, null, returnURL, reason);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquirePrepaidToken(String callerReferenceSender, String callerReferenceFunding, Amount amount,
                                       PaymentMethod paymentMethod,
                                       Date validityStart, Date validityExpiry,
                                       Boolean collectShippingAddress, Address address,
                                       String returnURL, String reason)
             throws FPSException, MalformedURLException {
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("callerReferenceSender", callerReferenceSender);
         parameters.put("callerReferenceFunding", callerReferenceFunding);
         parameters.put("currencyCode", amount.getCurrencyCode());
         parameters.put("transactionAmount", amount.getAmount().toString());
 
         if (reason != null)
             parameters.put("paymentReason", reason);
         if (validityStart != null)
             parameters.put("validityStart", DataUtils.encodeDate(validityStart));
         if (validityExpiry != null)
             parameters.put("validityExpiry", DataUtils.encodeDate(validityExpiry));
         if (paymentMethod != null)
             parameters.put("paymentMethod", paymentMethod.value());
         if (collectShippingAddress != null)
             parameters.put("collectShippingAddress", collectShippingAddress.toString());
         if (address != null) {
             parameters.put("addressName", address.getName());
             parameters.put("addressLine1", address.getLine1());
             parameters.put("addressLine2", address.getLine2());
             parameters.put("city", address.getCity());
             parameters.put("zip", address.getZipCode());
         }
         return generateUIPipelineURL("SetupPrepaid", returnURL, parameters);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquirePostPaidToken(String callerReferenceSender, String callerReferenceSettlement,
                                        Amount creditLimit, Amount globalAmountLimit,
                                        String returnURL, String reason)
             throws FPSException, MalformedURLException {
         return acquirePostPaidToken(callerReferenceSender, callerReferenceSettlement, null, null,
                 creditLimit, globalAmountLimit, null, null, null, null, returnURL, reason);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquirePostPaidToken(String callerReferenceSender, String callerReferenceSettlement,
                                        Amount creditLimit, Amount globalAmountLimit,
                                        PaymentMethod paymentMethod,
                                        String returnURL, String reason)
             throws FPSException, MalformedURLException {
         return acquirePostPaidToken(callerReferenceSender, callerReferenceSettlement, null, null,
                 creditLimit, globalAmountLimit, null, null, null, paymentMethod, returnURL, reason);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquirePostPaidToken(String callerReferenceSender, String callerReferenceSettlement,
                                        Date validityStart, Date validityExpiry,
                                        Amount creditLimit, Amount globalAmountLimit,
                                        List<UsageLimit> usageLimits, Boolean collectShippingAddress, Address address,
                                        PaymentMethod paymentMethod,
                                        String returnURL, String reason)
             throws FPSException, MalformedURLException {
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("callerReferenceSender", callerReferenceSender);
         parameters.put("callerReferenceSettlement", callerReferenceSettlement);
         if (validityStart != null)
             parameters.put("validityStart", DataUtils.encodeDate(validityStart));
         if (validityExpiry != null)
             parameters.put("validityExpiry", DataUtils.encodeDate(validityExpiry));
         parameters.put("currencyCode", creditLimit.getCurrencyCode());
         parameters.put("creditLimit", creditLimit.getAmount().toString());
         parameters.put("globalAmountLimit", globalAmountLimit.getAmount().toString());
         if (paymentMethod != null)
             parameters.put("paymentMethod", paymentMethod.value());
         if (reason != null)
             parameters.put("paymentReason", reason);
         if (usageLimits != null) {
             for (int i = 0; i < usageLimits.size(); i++) {
                 UsageLimit limit = usageLimits.get(i);
                 parameters.put("usageLimitType" + i, limit.getType().value());
                 if (limit.getPeriodicity() != null)
                     parameters.put("usageLimitPeriod" + i, limit.getPeriodicity().toString());
             }
         }
         if (collectShippingAddress != null)
             parameters.put("collectShippingAddress", collectShippingAddress.toString());
         if (address != null) {
             parameters.put("addressName", address.getName());
             parameters.put("addressLine1", address.getLine1());
             parameters.put("addressLine2", address.getLine2());
             parameters.put("city", address.getCity());
             parameters.put("zip", address.getZipCode());
         }
         return generateUIPipelineURL("SetupPostpaid", returnURL, parameters);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      */
     public String acquireEditToken(String callerReference, String tokenID, PaymentMethod paymentMethod,
                                    String returnURL)
             throws FPSException, MalformedURLException {
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("callerReference", callerReference);
         parameters.put("tokenID", tokenID);
         if (paymentMethod != null)
             parameters.put("paymentMethod", paymentMethod.value());
         return generateUIPipelineURL("EditToken", returnURL, parameters);
     }
 
     /**
      * Generate a signed URL for the CBUI pipeline.
      *
      * @param pipelineName the name of the pipeline
      * @param returnURL the URL where the user should be redirected at the end of the pipeline
      * @param params all CBUI parameters
      * @return the signed URL
      * @throws MalformedURLException
      */
     public String generateUIPipelineURL(String pipelineName, String returnURL, Map<String, String> params) throws MalformedURLException {
         // build the map of parameters
         SortedMap<String, String> parameters = new TreeMap<String, String>(params);
         parameters.put("callerKey", super.getAwsAccessKeyId());
         parameters.put("pipelineName", pipelineName);
         parameters.put("returnURL", returnURL);
         // build the URL
         StringBuffer url = new StringBuffer(uiPipeline);
         boolean first = true;
         for (Map.Entry<String, String> parameter : parameters.entrySet()) {
             if (first) {
                 url.append('?');
                 first = false;
             } else {
                 url.append('&');
             }
             url.append(urlencode(parameter.getKey())).append("=").append(urlencode(parameter.getValue()));
         }
         // calculate the signature
         URL rawURL = new URL(url.toString());
         StringBuilder toBeSigned = new StringBuilder(rawURL.getPath()).append('?').append(rawURL.getQuery());
         String signature = urlencode(encode(getSecretAccessKey(), toBeSigned.toString(), false));
         url.append("&awsSignature=").append(signature);
         return url.toString();
     }
 
     /**
      * Extract the single use token from the CBUI pipeline return.
      */
     public SingleUseInstrument extractSingleUseTokenFromCBUI(HttpServletRequest request)
             throws MalformedURLException, FPSException {
 		// parse status message
 		String status = request.getParameter("status");
 		String errorMessage = request.getParameter("errorMessage");
 		String requestID = request.getParameter("RequestId");
 		if ("SE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("A".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("CE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("PE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("NP".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("NM".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		if (logger.isDebugEnabled()) {
             logger.debug("Status: " + status);
 		    logger.debug("Error Message: " + errorMessage);
         }
         // ensure first that the request is valid
         if (!isSignatureValid(request))
             throw new InvalidSignatureException(request.getParameter("awsSignature"), request.getRequestURI());
         // extract expiry
         Date expiry = null;
         try {
             String expiryValue = request.getParameter("expiry");
             if (expiryValue != null)
                 expiry = DataUtils.decodeDate(expiryValue);
         } catch (ParseException e) {
             // do nothing -- this might happen!
         }
         return new SingleUseInstrument(
                 request.getParameter("tokenID"),
                 expiry,
                 new Address(
                         request.getParameter("addressName"),
                         request.getParameter("addressLine1"),
                         request.getParameter("addressLine2"),
                         request.getParameter("city"),
                         request.getParameter("state"),
                         request.getParameter("zip")
                 )
         );
     }
 
     /**
      * Extract the multi use token from the CBUI pipeline return.
      */
     public MultiUseInstrument extractMultiUseTokenFromCBUI(HttpServletRequest request)
             throws MalformedURLException, FPSException {
         // parse status message
 		String status = request.getParameter("status");
 		String errorMessage = request.getParameter("errorMessage");
 		String requestID = request.getParameter("RequestId");
 		if ("SE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("A".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("CE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("PE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("NP".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("NM".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		if (logger.isDebugEnabled()) {
             logger.debug("Status: " + status);
 		    logger.debug("Error Message: " + errorMessage);
         }
         // ensure first that the request is valid
         if (!isSignatureValid(request))
             throw new InvalidSignatureException(request.getParameter("awsSignature"), request.getRequestURI());
         // extract expiry
         Date expiry = null;
         try {
             String expiryValue = request.getParameter("expiry");
             if (expiryValue != null)
                 expiry = DataUtils.decodeDate(expiryValue);
         } catch (ParseException e) {
             // do nothing -- this might happen!
         }
         return new MultiUseInstrument(
                 request.getParameter("tokenID"),
                 expiry,
                 new Address(
                         request.getParameter("addressName"),
                         request.getParameter("addressLine1"),
                         request.getParameter("addressLine2"),
                         request.getParameter("city"),
                         request.getParameter("state"),
                         request.getParameter("zip")
                 )
         );
     }
 
     /**
      * Extract the recurring token from the CBUI pipeline return.
      */
     public RecurringInstrument extractRecurringTokenFromCBUI(HttpServletRequest request)
             throws MalformedURLException, FPSException {
         // parse status message
 		String status = request.getParameter("status");
 		String errorMessage = request.getParameter("errorMessage");
 		String requestID = request.getParameter("RequestId");
 		if ("SE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("A".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("CE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("PE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("NP".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("NM".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		if (logger.isDebugEnabled()) {
             logger.debug("Status: " + status);
 		    logger.debug("Error Message: " + errorMessage);
         }
         // ensure first that the request is valid
         if (!isSignatureValid(request))
             throw new InvalidSignatureException(request.getParameter("awsSignature"), request.getRequestURI());
         // extract expiry
         Date expiry = null;
         try {
             String expiryValue = request.getParameter("expiry");
             if (expiryValue != null)
                 expiry = DataUtils.decodeDate(expiryValue);
         } catch (ParseException e) {
             // do nothing -- this might happen!
         }
         return new RecurringInstrument(
                 request.getParameter("tokenID"),
                 expiry,
                 new Address(
                         request.getParameter("addressName"),
                         request.getParameter("addressLine1"),
                         request.getParameter("addressLine2"),
                         request.getParameter("city"),
                         request.getParameter("state"),
                         request.getParameter("zip")
                 )
         );
     }
 
     /**
      * Extract the recurring token from the CBUI pipeline return.
      */
     public RecipientInstrument extractRecipientTokenFromCBUI(HttpServletRequest request)
             throws MalformedURLException, FPSException {
         // parse status message
 		String status = request.getParameter("status");
 		String errorMessage = request.getParameter("errorMessage");
 		String requestID = request.getParameter("RequestId");
 		if ("SE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("A".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("CE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("PE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("NP".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("NM".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		if (logger.isDebugEnabled()) {
             logger.debug("Status: " + status);
 		    logger.debug("Error Message: " + errorMessage);
         }
         // ensure first that the request is valid
         if (!isSignatureValid(request))
             throw new InvalidSignatureException(request.getParameter("awsSignature"), request.getRequestURI());
         return new RecipientInstrument(
                 request.getParameter("tokenID"),
                 request.getParameter("refundTokenID")
         );
     }
 
     /**
      * Extract the recurring token from the CBUI pipeline return.
      */
     public PrepaidInstrument extractPrepaidTokenFromCBUI(HttpServletRequest request)
             throws MalformedURLException, FPSException {
         // parse status message
 		String status = request.getParameter("status");
 		String errorMessage = request.getParameter("errorMessage");
 		String requestID = request.getParameter("RequestId");
 		if ("SE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("A".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("CE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("PE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("NP".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("NM".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		if (logger.isDebugEnabled()) {
             logger.debug("Status: " + status);
 		    logger.debug("Error Message: " + errorMessage);
         }
         // ensure first that the request is valid
         if (!isSignatureValid(request))
             throw new InvalidSignatureException(request.getParameter("awsSignature"), request.getRequestURI());
         // extract expiry
         Date expiry = null;
         try {
             String expiryValue = request.getParameter("expiry");
             if (expiryValue != null)
                 expiry = DataUtils.decodeDate(expiryValue);
         } catch (ParseException e) {
             // do nothing -- this might happen!
         }
         return new PrepaidInstrument(
                 request.getParameter("prepaidInstrumentID"),
                 request.getParameter("fundingTokenID"),
                 request.getParameter("prepaidSenderTokenID"),
                 expiry,
                 new Address(
                         request.getParameter("addressName"),
                         request.getParameter("addressLine1"),
                         request.getParameter("addressLine2"),
                         request.getParameter("city"),
                         request.getParameter("state"),
                         request.getParameter("zip")
                 )
         );
     }
 
     /**
      * Extract the post paid token from the CBUI pipeline return.
      *
      * @param request the HTTP request
      * @return the post paid token ID
      * @throws MalformedURLException
      * @throws FPSException
      */
     public PostPaidInstrument extractPostPaidTokenFromCBUI(HttpServletRequest request)
             throws MalformedURLException, FPSException {
         // parse status message
 		String status = request.getParameter("status");
 		String errorMessage = request.getParameter("errorMessage");
 		String requestID = request.getParameter("RequestId");
 		if ("SE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("A".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("CE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("PE".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("NP".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		else if ("NM".equals(status))
 			throw new FPSException(requestID, status, errorMessage);
 		if (logger.isDebugEnabled()) {
             logger.debug("Status: " + status);
 		    logger.debug("Error Message: " + errorMessage);
         }
         // ensure first that the request is valid
         if (!isSignatureValid(request))
             throw new InvalidSignatureException(request.getParameter("awsSignature"), request.getRequestURI());
         // extract expiry
         Date expiry = null;
         try {
             String expiryValue = request.getParameter("expiry");
             if (expiryValue != null)
                 expiry = DataUtils.decodeDate(expiryValue);
         } catch (ParseException e) {
             // do nothing -- this might happen!
         }
         return new PostPaidInstrument(
                 request.getParameter("creditInstrumentID"),
                 request.getParameter("creditSenderTokenID"),
                 request.getParameter("settlementTokenID"),
                 expiry,
                 new Address(
                         request.getParameter("addressName"),
                         request.getParameter("addressLine1"),
                         request.getParameter("addressLine2"),
                         request.getParameter("city"),
                         request.getParameter("state"),
                         request.getParameter("zip")
                 )
         );
     }
 
     @SuppressWarnings("unchecked")
     public boolean isSignatureValid(HttpServletRequest request) throws MalformedURLException {
         String signature = urlencode(request.getParameter("awsSignature"));
         if (signature == null)
             return false;
         List<String> parameters = new ArrayList(request.getParameterMap().keySet());
         Collator stringCollator = Collator.getInstance();
         stringCollator.setStrength(Collator.PRIMARY);
         Collections.sort(parameters, stringCollator);
         parameters.remove("awsSignature");
         // build the URL to sign in order to ensure this is a valid signature we received
         StringBuffer url = new StringBuffer(request.getRequestURL());
         boolean first = true;
         for (String parameter : parameters) {
             if (first) {
                 url.append('?');
                 first = false;
             } else {
                 url.append('&');
             }
             url.append(urlencode(parameter)).append("=").append(urlencode(request.getParameter(parameter)));
         }
         // sign the URL
         URL rawURL = new URL(url.toString());
         StringBuilder toBeSigned = new StringBuilder(rawURL.getPath()).append('?').append(rawURL.getQuery());
         String ourSignature = urlencode(encode(getSecretAccessKey(), toBeSigned.toString(), false));
         ourSignature = ourSignature.replaceAll("%2B", "+");
         if (logger.isDebugEnabled()) {
             logger.debug("AWS sig: " + signature);
             logger.debug("Our sig: " + ourSignature);
         }
         return ourSignature.equals(signature);
     }
 
     protected <T> T makeRequestInt(HttpMethodBase method, String action, Map<String, String> params, Class<T> respType)
 		throws FPSException {
 		try {
 			T response = makeRequest(method, action, params, respType);
             Class responseClass = response.getClass();
             ResponseStatus status = (ResponseStatus) responseClass.getMethod("getStatus").invoke(response);
             if (ResponseStatus.FAILURE.equals(status)) {
                 String requestID = (String) responseClass.getMethod("getRequestId").invoke(response);
                 ServiceErrors rawErrors = (ServiceErrors) responseClass.getMethod("getErrors").invoke(response);
                 List<FPSError> errors = new ArrayList<FPSError>(rawErrors.getErrors().size());
                 for (ServiceError error : rawErrors.getErrors()) {
                     AWSError.ErrorType type = null;
                     switch (error.getErrorType()) {
                         case BUSINESS:
                             type = AWSError.ErrorType.SENDER;
                             break;
                         case SYSTEM:
                             type = AWSError.ErrorType.RECEIVER;
                             break;
                     }
                     errors.add(new FPSError(type, error.getErrorCode(), error.getReasonText(), error.isIsRetriable()));
                 }
                 throw new FPSException(requestID, errors);
             }
             return response;
 		} catch (AWSException ex) {
 			throw new FPSException(ex);
 		} catch (JAXBException ex) {
 			throw new FPSException("Problem parsing returned message.", ex);
 		} catch (HttpException ex) {
 			throw new FPSException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new FPSException(ex.getMessage(), ex);
 		} catch (InvocationTargetException ex) {
             throw new FPSException(ex.getMessage(), ex);
         } catch (NoSuchMethodException ex) {
             throw new FPSException(ex.getMessage(), ex);
         } catch (IllegalAccessException ex) {
             throw new FPSException(ex.getMessage(), ex);
         }
     }
 
     static void setVersionHeader(AWSQueryConnection connection) {
         List<String> vals = new ArrayList<String>();
         vals.add("2007-01-08");
         connection.getHeaders().put("Version", vals);
     }
 }
