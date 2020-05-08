 package com.ac;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 
 import org.apache.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.mgi.paypal.inputbean.CommitTransactionInputBean;
 import com.mgi.paypal.inputbean.FeeLinkValueInputBean;
 import com.mgi.paypal.inputbean.FeeLookupInputBean;
 import com.mgi.paypal.inputbean.HistroyLookupInputBean;
 import com.mgi.paypal.inputbean.SendMailInputBean;
 import com.mgi.paypal.inputbean.SendProofInputBean;
 import com.mgi.paypal.inputbean.SendValidationInputBean;
 import com.mgi.paypal.inputbean.TransactionInformationMailInputBean;
 import com.mgi.paypal.inputbean.UserDataInputBean;
 import com.mgi.paypal.inputbean.UserLimitInputBean;
 import com.mgi.paypal.interf.ACInterface;
 import com.mgi.paypal.response.CommitTransactionResponse;
 import com.mgi.paypal.response.FeeLookupResponse;
 import com.mgi.paypal.response.HistroyLookupResponse;
 import com.mgi.paypal.response.SendValidationResponse;
 import com.mgi.paypal.response.TransactionInformationMailResponse;
 import com.mgi.paypal.util.PropertyUtil;
 import com.mgi.paypal.util.UserData;
 import com.paypal.adaptivepayment.client.PayResponse;
 
 @Consumes("application/json")
 @Produces("application/JSON")
 public class ACImpl implements ACInterface {
 
 	public ACImpl() {
 
 	}
 
 	private static Logger LOGGER = Logger.getLogger(ACImpl.class);
 
 	
 
 	/**
 	 * getUserLimits. This method will call getUserLimits API of
 	 * AdaptivePayment.
 	 * 
 	 * @param UserLimitInputBean
 	 *            the user Limit Input Bean
 	 * @return GetUserLimitsResponse which contains userLimitAmount as JSON
 	 *         object.
 	 */
 	@POST
 	@Path("/getUserLimits")
 	@Override
 	public String getUserLimits(
 			@Context HttpServletRequest httpServletRequest,
 			UserLimitInputBean userLimitInputBean) {
 		
 		String paypalToken = (String) httpServletRequest.getSession()
 				.getAttribute("paypalToken");
 		if (userLimitInputBean.getToken() == null
 				|| !userLimitInputBean.getToken().equals(paypalToken)) {
 			LOGGER.debug("Invalid token in input bean : Token In session : "
 					+ paypalToken + " Token from UI : "
 					+ userLimitInputBean.getToken());
 
 			com.mgi.paypal.response.GetUserLimitsResponse getUserLimitsResponse 
 			= new com.mgi.paypal.response.GetUserLimitsResponse();
 			getUserLimitsResponse.setErrorMessage("Invalid Transaction.");
 			getUserLimitsResponse.setTransactionSuccess(false);
 
 			return new Gson().toJson(getUserLimitsResponse);
 		}
 		
 		
 		userLimitInputBean.setEmailID((String) httpServletRequest.getSession()
 				.getAttribute("customerEmail"));
 
 		return PayPalBO.getUserLimits(userLimitInputBean);
 	}
 
 	@POST
 	@Path("/getUserData")
 	@Override
 	public String getUserData(
 			@Context HttpServletRequest httpServletRequest,
 			UserDataInputBean userDataInputBean) {
 
 		UserData userData = PayPalBO.getUserData(userDataInputBean);
 
 		if (userData.isTransactionSuccess()) {
 			httpServletRequest.getSession().setAttribute("userLoggedIn", "true");
 			httpServletRequest.getSession().setAttribute("paypalToken",
 					userData.getToken());
 			httpServletRequest.getSession().setAttribute("customerEmail",
 					userData.getEmail());
 			httpServletRequest.getSession().setAttribute("id_Token", userDataInputBean.getId_Token());
 		}
 
 		return new Gson().toJson(userData);
 	}
 
 	/**
 	 * getFeeLinkValue. This method will call feeLookUp API to get fee for only
 	 * two hundred and five hundred USD.
 	 * 
 	 * @return feeLinkValues. The feeLinkValues object contains fee for two
 	 *         hundred and five hundred USD in JSON format.
 	 */
 	@POST
 	@Path("/getFeeLinkValue")
 	@Override
 	public String getFeeLinkValue(
 			FeeLinkValueInputBean feeLinkValueInputBean) {
 
 		FeeDetailsBO feeDetailsBO = new FeeDetailsBO();
 		return feeDetailsBO.getFeeLinkValue(feeLinkValueInputBean);
 	}
 
 	@POST
 	@Path("/sendmail")
 	@Override
 	public String sendMail(
 			@Context HttpServletRequest httpServletRequest,
 			SendMailInputBean sendMailInputBean) {
 
 		MailServiceBO mailServiceBO = new MailServiceBO();
 		return mailServiceBO.sendReportInformationMail(sendMailInputBean);
 	}
 
 	@POST
 	@Path("/sendProofMessage")
 	@Override
 	public String sendProofMessage(
 			SendProofInputBean sendProofInputBean) {
 
 		SignUpBO signUpBO = new SignUpBO();
 		return signUpBO.sendProofMessage(sendProofInputBean);
 	}
 
 	@POST
 	@Path("/sendTransactionInformationMail")
 	@Override
 	public String sendTransactionInformationMail(
 			@Context HttpServletRequest httpServletRequest,
 			TransactionInformationMailInputBean transactionInformationMailInputBean) {
 
 		String paypalToken = (String) httpServletRequest.getSession()
 				.getAttribute("paypalToken");
 		
 		//String userAgent = null;
 		if (transactionInformationMailInputBean.getToken() == null
 				|| !transactionInformationMailInputBean.getToken().equals(
 						paypalToken)) {
 			LOGGER.debug("Invalid token in input bean : Token In session : "
 					+ paypalToken + " Token from UI : "
 					+ transactionInformationMailInputBean.getToken());
 
 			TransactionInformationMailResponse transactionInformationMailResponse = new TransactionInformationMailResponse();
 			transactionInformationMailResponse.setTransactionSuccess(false);
 			transactionInformationMailResponse
 					.setMessageToUser("Invalid Transaction.");
 			return new Gson().toJson(transactionInformationMailResponse);
 
 		}
 		
 		transactionInformationMailInputBean.setCustomerEmail((String) httpServletRequest
 				.getSession().getAttribute("customerEmail"));
 		
 		//userAgent = httpServletRequest.getHeader("referer");
 		
 		MailServiceBO mailServiceBO = new MailServiceBO();
 		return mailServiceBO.sendTransactionInformationMail(
 				transactionInformationMailInputBean.getCustomerEmail(),
 				transactionInformationMailInputBean.getTransactionAmount(),
 				transactionInformationMailInputBean.getReferenceNumber(),
 				transactionInformationMailInputBean.getCustomerName(),
 				transactionInformationMailInputBean.getStateName(),
 				transactionInformationMailInputBean.getFee());//userAgent
 
 	}
 
 	/**
 	 * getHistoryDetails. This method will return last ten transaction from
 	 * history table. It will call detailLookUp API for transaction which is not
 	 * in 'received' status and update the status in history table.
 	 * 
 	 * @param HistroyLookupInputBean
 	 *            The histroyLookupInputBean
 	 * 
 	 * @return histroyLookupResponse. This return object will contain last ten
 	 *         transaction in JSON format.
 	 */
 	@POST
 	@Path("/getHistoryDetails")
 	@Override
 	public String getHistoryDetails(
 			@Context HttpServletRequest httpServletRequest,
 			HistroyLookupInputBean histroyLookupInputBean) {
 
 		String paypalToken = (String) httpServletRequest.getSession()
 				.getAttribute("paypalToken");
 		if (histroyLookupInputBean == null
 				|| !histroyLookupInputBean.getToken().equals(paypalToken)) {
 			LOGGER.debug("Invalid token in input bean : Token In session : "
 					+ paypalToken + " Token from UI : "
 					+ histroyLookupInputBean.getToken());
 
 			HistroyLookupResponse histroyLookupResponse = new HistroyLookupResponse();
 			histroyLookupResponse.setTransactionSuccess(false);
 			histroyLookupResponse
 					.setErrorMessage("Invalid Transaction.");
 			return new Gson().toJson(histroyLookupResponse);
 
 		}
 
 		histroyLookupInputBean.setCustomerEmailId((String) httpServletRequest.getSession()
 				.getAttribute("customerEmail"));
 
 		return HistoryBO.retrieveHistoryDetails(histroyLookupInputBean);
 	}
 
 	/**
 	 * getState. This method will call codeTable API for only one time in a day
 	 * to get states in USA and it will update static Object. From the second
 	 * call this method will return the list of states from static object
 	 * 'STATES_IN_USA'.
 	 * 
 	 * @return list of states in USA in JSON format.
 	 */
 	@POST
 	@Path("/getStateCode")
 	@Override
 	public String getState() {
 
 		return CountryBO.getStateForUSA();
 	}
 	/**
 	 * getFee. This method will return fee in USD for given input amount.
 	 * 
 	 * @param HttpServletRequest
 	 *            ,HttpServletResponse,FeeLookupInputBean
 	 * 
 	 * @return FeeLookupResponse This response contains fee in JSON format.
 	 */
 	@POST
 	@Path("/getFee")
 	@Override
 	public String getFee(
 			@Context HttpServletRequest httpServletRequest,
 			@Context HttpServletResponse response,
 			FeeLookupInputBean feeLookupInputBean) {
 
 		String paypalToken = (String) httpServletRequest.getSession()
 				.getAttribute("paypalToken");
 		if (feeLookupInputBean.getToken() == null
 				|| !feeLookupInputBean.getToken().equals(paypalToken)) {
 			LOGGER.debug("Invalid token in input bean : Token In session : "
 					+ paypalToken + " Token from UI : "
 					+ feeLookupInputBean.getToken());
 
 			FeeLookupResponse feeLookupResponseUI = new FeeLookupResponse();
 			feeLookupResponseUI.setErrorMessage("Invalid Transaction.");
 			feeLookupResponseUI.setTransactionSuccess(false);
 
 			return new Gson().toJson(feeLookupResponseUI);
 		}
 		
 		LOGGER.info("IP Address : " + httpServletRequest.getRemoteAddr());
 
 		FeeLookupResponse feeLookupResponse = FeeDetailsBO
 				.getFee(feeLookupInputBean);
 		if (feeLookupResponse.isTransactionSuccess()) {
 			httpServletRequest.getSession().setAttribute(
 					feeLookupResponse.getMgiTransactionSessionID(),
 					"sendValidate");
 		}
 		return new Gson().toJson(feeLookupResponse);
 	}
 	/**
 	 * sendValidation. This method will call sendValidation API of Agent
 	 * Connect. This method will store the transaction details in MoneyGram
 	 * server and the stored data will be available for 30 minutes only.
 	 * 
 	 * @param SendValidationInputBean
 	 *            the send Validation Input Bean
 	 * @return SendValidationResponse which contains MgiTransactionSessionID as
 	 *         JSON object.
 	 */
 	@POST
 	@Path("/sendValidation")
 	@Override
 	public String sendValidation(
 			@Context HttpServletRequest httpServletRequest,
 			SendValidationInputBean sendValidationInputBean) {
 
 		// Validating session is valid or not.
 
 		String paypalToken = (String) httpServletRequest.getSession()
 				.getAttribute("paypalToken");
 		if (sendValidationInputBean.getToken() == null
 				|| !sendValidationInputBean.getToken().equals(paypalToken)) {
 			LOGGER.debug("Invalid token in input bean : Token In session : "
 					+ paypalToken + " Token from UI : "
 					+ sendValidationInputBean.getToken());
 			SendValidationResponse sendValidationResponse = new SendValidationResponse();
 			sendValidationResponse.setTransactionSuccess(false);
 			sendValidationResponse.setErrorMessage("Invalid Transaction.");
 			return new Gson().toJson(sendValidationResponse);
 
 		}
 		
 		// Validating request coming for first time.
 		String key = (String) httpServletRequest.getSession().getAttribute(
 				sendValidationInputBean.getMgiTransactionSessionID());
 		if (key != null && key.equals("sendValidate")) {
 			LOGGER.debug(key);
 			httpServletRequest.getSession().setAttribute(
 					sendValidationInputBean.getMgiTransactionSessionID(),
 					"commitTransaction");
 			LOGGER.debug((String) httpServletRequest.getSession().getAttribute(
 				sendValidationInputBean.getMgiTransactionSessionID()));
 		} else {
 			SendValidationResponse sendValidationResponse = new SendValidationResponse();
 			sendValidationResponse.setTransactionSuccess(true);
 			return new Gson().toJson(sendValidationResponse);
 		}
 		
 		SendValidationResponse sendValidationResponse = new SendValidationResponse();
 		sendValidationInputBean.setSenderEmail((String) httpServletRequest.getSession()
 				.getAttribute("customerEmail"));
 
 		// insert session , consumer ID in history table.
 		MoneyGramPayPalDAO moneyGramPayPalDAO = new MoneyGramPayPalDAO();
 		
 		try {
 			moneyGramPayPalDAO
 					.insertHistoryDetailsBeforeSendValidation(sendValidationInputBean);
 		} catch (Exception exception) {
 			// If insert DB failed , then return error msg to UI.
 			LOGGER.error(exception.getLocalizedMessage());
 			sendValidationResponse.setTransactionSuccess(false);
 			sendValidationResponse
 					.setErrorMessage(PropertyUtil.messageFromProperties
 							.getString("TRANSACTION_FAILED_RETRY"));
 			return new Gson().toJson(sendValidationResponse);
 		}
 
 		sendValidationResponse = TransactionBO
 				.validate(sendValidationInputBean);
 
 		if (!sendValidationResponse.isTransactionSuccess()) {
 			// update history table 'MGI_FAILED'
 			try {
 				moneyGramPayPalDAO
 						.updateHistorySendValidationOrCommitTransactionFailed(sendValidationInputBean
 								.getMgiTransactionSessionID());
 			} catch (Exception exception) {
 
 				exception.getLocalizedMessage();
 				LOGGER.error("Updating history table as 'MGI_FAILED' failed for MgiTransactionSessionID : "
 						+ sendValidationInputBean.getMgiTransactionSessionID());
 			}
 		}
 
 		return new Gson().toJson(sendValidationResponse);
 	}
 
 	/**
 	 * commitTransaction. This method will call commitTransaction API and it
 	 * will generate referenceNumber for the transaction.After committing the
 	 * transaction, This method will insert one tuple in HistoryTable with
 	 * referenceNumber.
 	 * 
 	 * @param CommitTransactionInputBean
 	 *            the commit Transaction InputBean
 	 * @return CommitTransactionResponse which contains reference number as JSON
 	 *         object.
 	 */
 	@POST
 	@Path("/commitTransaction")
 	@Override
 	public String commitTransaction(
 			@Context HttpServletRequest httpServletRequest,
 			CommitTransactionInputBean commitTransactionInputBean) {
 
 		LOGGER.debug("Enter commitTransaction.");
 		
 		
 		// validate payPal token and current session are valid.
 
 		String paypalToken = (String) httpServletRequest.getSession().getAttribute(
 				"paypalToken");
 		if (commitTransactionInputBean.getToken() == null
 				|| !commitTransactionInputBean.getToken().equals(paypalToken)) {
 			LOGGER.debug("Invalid token in input bean : Token In session : "
 					+ paypalToken + " Token from UI : "
 					+ commitTransactionInputBean.getToken());
 
 			CommitTransactionResponse commitTransactionResponse = new CommitTransactionResponse();
 			commitTransactionResponse.setTransactionSuccess(false);
 			commitTransactionResponse.setErrorMessage("Invalid Transaction.");
 
 			return new Gson().toJson(commitTransactionResponse);
 		}
 		
 		// Validating the request is coming for first time.
 		String key = (String) httpServletRequest.getSession().getAttribute(
 				commitTransactionInputBean.getMgiTransactionSessionID());
 		
 		if (key != null && key.equals("commitTransaction")) {
 			LOGGER.debug(key);
 			// httpServletRequest.getSession().removeAttribute(
 			// commitTransactionInputBean.getMgiTransactionSessionID());
 			httpServletRequest.getSession()
 					.setAttribute(
 						commitTransactionInputBean
 									.getMgiTransactionSessionID(), "");
 			LOGGER.debug((String) httpServletRequest.getSession().getAttribute(
 				commitTransactionInputBean.getMgiTransactionSessionID()));
 		} else {
 			String referenceNumber = (String) httpServletRequest
 					.getSession().getAttribute(
 							commitTransactionInputBean
 									.getMgiTransactionSessionID());
 			CommitTransactionResponse commitTransactionResponse = new CommitTransactionResponse();
 			if (referenceNumber.trim().equals("")) {
 
 				commitTransactionResponse.setTransactionSuccess(false);
 				commitTransactionResponse.setErrorMessage(PropertyUtil.messageFromProperties
 						.getString("TRANSACTION_FAILED_RETRY"));
 			} else {
 				commitTransactionResponse.setTransactionSuccess(true);
 			}
 			commitTransactionResponse
 					.setReferenceNumber(referenceNumber);
 			return new Gson().toJson(commitTransactionResponse);
 		}
 		
 		
 		if (commitTransactionInputBean.getTransactionAmount() == null
 				|| commitTransactionInputBean.getTransactionFee() == null) {
 			CommitTransactionResponse commitTransactionResponse = new CommitTransactionResponse();
 			commitTransactionResponse.setTransactionSuccess(false);
 			commitTransactionResponse.setErrorMessage("Invalid input data.");
 
 			return new Gson().toJson(commitTransactionResponse);
 
 		}
 		
 		commitTransactionInputBean.setCustomerEmail((String) httpServletRequest
 				.getSession().getAttribute("customerEmail"));
 
 		CommitTransactionResponse commitTransactionResponse = new CommitTransactionResponse();
 		MoneyGramPayPalDAO moneyGramPayPalDAO = new MoneyGramPayPalDAO();
 
 		// Calling commitTransaction Of Agent Connect.
 		commitTransactionResponse = TransactionBO
 				.commitTransaction(commitTransactionInputBean);
 
 		if (!commitTransactionResponse.isTransactionSuccess()) {
 			// If Commit FAILED .
 			// Update history 'MGI_FAILED'
 			try {
 				moneyGramPayPalDAO
 						.updateHistorySendValidationOrCommitTransactionFailed(commitTransactionInputBean
 								.getMgiTransactionSessionID());
 			} catch (Exception exception) {
 				exception.getLocalizedMessage();
 				LOGGER.error("Updating history table as 'MGI_FAILED' failed for MgiTransactionSessionID : "
 						+ commitTransactionInputBean
 								.getMgiTransactionSessionID());
 
 			}
 
 			return new Gson().toJson(commitTransactionResponse);
 		}
 
 		// If Commit Success then update history with MGIReference Number.
 		try {
 			if (!moneyGramPayPalDAO.updateHistoryAfterCommitTransaction(
 					commitTransactionInputBean.getMgiTransactionSessionID(),
 					commitTransactionResponse.getReferenceNumber())) {
 				CommitTransactionResponse commitTransactionResponse2 = new CommitTransactionResponse();
 				commitTransactionResponse2.setTransactionSuccess(false);
 				commitTransactionResponse2
 						.setErrorMessage(PropertyUtil.messageFromProperties
 								.getString("TRANSACTION_FAILED_RETRY"));
 
 				return new Gson().toJson(commitTransactionResponse2);
 			}
 		} catch (Exception exception) {
 
 			LOGGER.error("Updating history with MgiReferenceNumber : "
 					+ commitTransactionResponse.getReferenceNumber()
 					+ " Failed for MgiTransactionSessionID : "
 					+ commitTransactionInputBean.getMgiTransactionSessionID());
 			LOGGER.error(exception.getLocalizedMessage());
 			exception.printStackTrace();
 			
 			CommitTransactionResponse commitTransactionResponse2 = new CommitTransactionResponse();
 			commitTransactionResponse2.setTransactionSuccess(false);
 			commitTransactionResponse2.setErrorMessage(PropertyUtil.messageFromProperties
 					.getString("TRANSACTION_FAILED_RETRY"));
 
 			return new Gson().toJson(commitTransactionResponse2);
 		}
 
 		// Call PAY API of Paypal.
 
 		PayResponse payResponse = null;
 		try {
 			payResponse = PayPalBO.payToMoneyGram(commitTransactionInputBean
 					.getToken(), commitTransactionInputBean.getCustomerEmail(),
 					commitTransactionInputBean.getTransactionAmount(),
 					commitTransactionInputBean.getCustomerPhoneNumber(),
 					commitTransactionResponse.getReferenceNumber(),
 					commitTransactionInputBean.getTransactionFee());
 		} catch (Exception exception) {
 			// If PAY API call failed
 			LOGGER.error(exception.getLocalizedMessage());
 			exception.printStackTrace();
 			return payAPIFailed(commitTransactionInputBean.getCustomerEmail(),
 					commitTransactionInputBean.getMgiTransactionSessionID(),
 					commitTransactionResponse.getReferenceNumber());
 
 		}
 		if (payResponse == null) {
 			// If PAY API call failed
 			LOGGER.error("Response for PAY API is null. CustomerEmailId :"
 					+ commitTransactionInputBean.getCustomerEmail()
 					+ ". MgiTransactionSessionID : "
 					+ commitTransactionInputBean.getMgiTransactionSessionID());
 			return payAPIFailed(commitTransactionInputBean.getCustomerEmail(),
 					commitTransactionInputBean.getMgiTransactionSessionID(),
 					commitTransactionResponse.getReferenceNumber());
 
 		}
 		httpServletRequest.getSession().setAttribute(
 				commitTransactionInputBean.getMgiTransactionSessionID(),
 				commitTransactionResponse.getReferenceNumber());
 		// If PAY API Call success.
 		// update history PAYPAL_TRAN_ID = payResponse.getPayKey() and
 
 		try {
 			moneyGramPayPalDAO.updateHistoryAfterPay(commitTransactionInputBean
 					.getMgiTransactionSessionID(), payResponse.getPayKey());
 		} catch (Exception exception) {
 			LOGGER.error("Updating PAYPAL_TRAN_ID in history Failed. PAYPAL_TRAN_ID : "
 					+ payResponse.getPayKey()
 					+ " MgiTransactionSessionID : "
 					+ commitTransactionInputBean.getMgiTransactionSessionID());
 			LOGGER.error(exception.getLocalizedMessage());
 			exception.printStackTrace();
 		}
 
 		LOGGER.debug("Exit commitTransaction.");
 
 		return new Gson().toJson(commitTransactionResponse);
 	}
 
 	private static String payAPIFailed(
 			String customerEmail, String mgiTransactionSessionID,
 			String referenceNumber) {
 
 		LOGGER.error("Pay API Call failed for email Id : " + customerEmail
 				+ " MgiTransactionSessionID : " + mgiTransactionSessionID
 				+ " MgiReferenceNumber : " + referenceNumber);
 
 		MoneyGramPayPalDAO moneyGramPayPalDAO = new MoneyGramPayPalDAO();
 		try {
 			moneyGramPayPalDAO.updatePaypalFailed(mgiTransactionSessionID);
 		} catch (Exception exception) {
 
 			exception.printStackTrace();
 			LOGGER.error("Updating TRAN_STATUS = 'PAYPAL_FAILED' in history table fai"
 					+ "led for mgiTransactionSessionID : "
 					+ mgiTransactionSessionID);
 		}
 		CommitTransactionResponse commitTransactionResponse = new CommitTransactionResponse();
 		commitTransactionResponse.setTransactionSuccess(false);
 		commitTransactionResponse
 				.setErrorMessage(PropertyUtil.messageFromProperties
 						.getString("TRANSACTION_FAILED_RETRY"));
 
 		return new Gson().toJson(commitTransactionResponse);
 
 	}
 	
 	@POST
 	@Path("/logOutUser")
 	@Override
 	public void logOutUser(
 			@Context HttpServletRequest httpServletRequest) {
 
 		// String id_Token = (String)
 		// httpServletRequest.getSession().getAttribute("id_Token");
 
 		httpServletRequest.getSession().removeAttribute("userLoggedIn");
 		httpServletRequest.getSession().removeAttribute("paypalToken");
 		httpServletRequest.getSession().removeAttribute("customerEmail");
 		httpServletRequest.getSession().removeAttribute("id_Token");
 		httpServletRequest.getSession().removeAttribute(
 				"commitTransactionRequest");
 		httpServletRequest.getSession()
 				.removeAttribute("sendValidationRequest");
 		httpServletRequest.getSession().invalidate();
 
 		/*
 		 * try { PayPalBO.logOutPayPal(id_Token); } catch (Exception e) { //
 		 * TODO Auto-generated catch block e.printStackTrace(); }
 		 */
 	}
 }
