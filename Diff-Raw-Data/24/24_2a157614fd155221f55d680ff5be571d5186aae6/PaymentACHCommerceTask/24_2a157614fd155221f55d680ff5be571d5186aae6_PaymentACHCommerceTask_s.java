 /*
 The contents of this file are subject to the Jbilling Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.jbilling.com/JPL/
 
 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.
 
 The Original Code is jbilling.
 
 The Initial Developer of the Original Code is Emiliano Conde.
 Portions created by Sapienter Billing Software Corp. are Copyright 
 (C) Sapienter Billing Software Corp. All Rights Reserved.
 
 Contributor(s): ______________________________________.
 */
 
 package com.sapienter.jbilling.server.pluggableTask;
 
 import java.io.IOException;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.common.Constants;
 import com.sapienter.jbilling.server.entity.PaymentAuthorizationDTO;
 import com.sapienter.jbilling.server.payment.PaymentAuthorizationDTOEx;
 import com.sapienter.jbilling.server.payment.PaymentDTOEx;
 import com.sapienter.jbilling.server.user.ContactBL;
 
 /**
  * A pluggable PaymentTask that uses ACHCommerce gateway for ACH transactions. 
  * 
  * The following parameters must be configured for this payment task to work: -
  *    
  * gatewayUrl -- URL to the ACHCommerce gateway
  * login      -- merchant gateway login name provided by ACH Commerce
  * password   -- merchant gateway password provided by ACH Commerce
  * merchantid -- merchant id provided by ACH Commerce
  * batchid    -- batch id provided by ACH Commerce
  * 
  * @author Narinder
  */
 public class PaymentACHCommerceTask extends PaymentTaskBase {
 	private final static String PAYMENT_SUCCESS_CODE = "000";
 	private final static String PROCESSOR_NAME = "ACHCommerce";
 	private final static String RESPONSE_DELIMITER = "[|]";
     private static final int timeOut = 10000; // in millisec
     
 	private Logger logger;
 	
 	public PaymentACHCommerceTask(){
 		logger = Logger.getLogger(PaymentACHCommerceTask.class);
 	}
 
 	/* 
 	 * This method has not been implemented as this pluggable task is only processing
 	 * Debit card transactions and therefore simply returns false.
 	 */
 	public boolean confirmPreAuth(PaymentAuthorizationDTO auth,
 			PaymentDTOEx paymentInfo) throws PluggableTaskException {
 		return false;
 	}
 
 	/* 
 	 * (non-Javadoc)
 	 * @see com.sapienter.jbilling.server.pluggableTask.PaymentTask#failure(java.lang.Integer, java.lang.Integer)
 	 */
 	public void failure(Integer userId, Integer retry) {}
 
 	/* 
 	 * This method has not been implemented as this pluggable task is only processing
 	 * Debit card transactions and therefore simply returns false.
 	 * 
 	 * @see com.sapienter.jbilling.server.pluggableTask.PaymentTask#preAuth(com.sapienter.jbilling.server.payment.PaymentDTOEx)
 	 */
 	public boolean preAuth(PaymentDTOEx paymentInfo)
 			throws PluggableTaskException {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sapienter.jbilling.server.pluggableTask.PaymentTask#process(com.sapienter.jbilling.server.payment.PaymentDTOEx)
 	 */
 	public boolean process(PaymentDTOEx paymentInfo)
 			throws PluggableTaskException {
 		try{
             if (paymentInfo.getAch() == null || 
                     paymentInfo.getAch().getBankAccount() == null ||
                     paymentInfo.getAch().getBankAccount().length() == 0) {
                 // this payment is not for ach, might be for credit cards
                 return true;
             }
 			PaymentAuthorizationDTOEx paymentAuthDTO = processACHRequest(paymentInfo);
 	        super.storeProcessedAuthorization(paymentInfo,paymentAuthDTO);
 	        paymentInfo.setAuthorization(paymentAuthDTO);
 	        if(paymentAuthDTO.getCode1().equals(PAYMENT_SUCCESS_CODE)){
 	        	paymentInfo.setResultId(Constants.RESULT_OK);
 	        }else{
 	        	paymentInfo.setResultId(Constants.RESULT_FAIL);
 	        }
 			return false;
 		}catch(HttpException he){
 			logger.error("Error during payment processing",he);
         	paymentInfo.setResultId(Constants.RESULT_UNAVAILABLE);
 			return true;
 		}catch(IOException ie){
         	paymentInfo.setResultId(Constants.RESULT_UNAVAILABLE);
 			logger.error("Error during payment processing",ie);
 			return true;
 		}
 	}
 	
 	/**
 	 * Executes an http payment proessing request to the ACHCommerce gateway. The request is constructed and sent using the
 	 * ACH Transit guide documented at http://gateway.achcommerce.com/achgate/achgate.html
 	 * @param paymentInfo
 	 * @return
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws PluggableTaskException
 	 */
 	private PaymentAuthorizationDTOEx processACHRequest(PaymentDTOEx paymentInfo) throws HttpException, IOException, PluggableTaskException {
 		
 		String url = super.ensureGetParameter("gatewayUrl");
         //create a singular HttpClient object
         HttpClient client = new HttpClient();
         client.setConnectionTimeout(timeOut);
 
         PostMethod post = new PostMethod(url);
         NameValuePair[] data = getChargeData(paymentInfo);
         post.setRequestBody(data);
 
         //execute the method
         client.executeMethod(post);
         String responseBody = post.getResponseBodyAsString();
        if(logger.isDebugEnabled()){
	        logger.debug("Got response:" + responseBody);
        }
         
         PaymentAuthorizationDTOEx paymentAuthDTO = processResponse(responseBody);
         post.releaseConnection();
         post.recycle();
         return paymentAuthDTO;
 	}
 	
 	/**
 	 * Constructs HttpClient request data for a payment request to be sent to ACHCommerce gateway.
 	 * @param paymentInfo
 	 * @return
 	 * @throws PluggableTaskException if any of the required parameters for this pluggable task are not configured.
 	 */
 	private NameValuePair[] getChargeData(PaymentDTOEx paymentInfo) throws PluggableTaskException{
 		ContactBL contact = null;
 		String firstName = null;
 		String lastName = null;
 		try{
 	        contact = new ContactBL();
 	        contact.set(paymentInfo.getUserId());
         	firstName = contact.getEntity().getFirstName();
         	lastName = contact.getEntity().getLastName();
 		}catch(Exception e){
 			throw new PluggableTaskException("Error loading Contact for user id " + paymentInfo.getUpdateDateTime(),e);
 		}
 			
         NameValuePair[] data = {
             new NameValuePair("usermode", "cgi"),
             new NameValuePair("action", "submit"),
             new NameValuePair("login", super.ensureGetParameter("login")),
             new NameValuePair("password", super.ensureGetParameter("password")),
             new NameValuePair("merchantid", super.ensureGetParameter("merchantid")),
             new NameValuePair("batchid", super.ensureGetParameter("batchid")),
             new NameValuePair("verstr", "RS"),
             new NameValuePair("routing", paymentInfo.getAch().getAbaRouting()),
             new NameValuePair("account", paymentInfo.getAch().getBankAccount()),
             new NameValuePair("amount", paymentInfo.getAmount().toString()),
             new NameValuePair("fname", firstName),
             new NameValuePair("lname", lastName),
             new NameValuePair("individualid", paymentInfo.getId().toString()),
             new NameValuePair("trancode", "22"),
             new NameValuePair("sec", "PPD"),
             new NameValuePair("replymode", "csv")
         };
         
         return data;
     }
 	
 	/**
 	 * Parses data returned by ACHCommerce gateway which is expected to be in the
 	 * following formate: -
 	 * 
 	 * REPLYCODE|DESCRIPTION|TRANSACTIONID|REQUESTID|BANKNAME|EOTXXX
 
 	 * @param gatewayResponse
 	 */
 	private PaymentAuthorizationDTOEx processResponse(String gatewayResponse) throws PluggableTaskException{
 		String[] returnData = gatewayResponse.split(RESPONSE_DELIMITER);
		if(returnData.length<6){
 			throw new PluggableTaskException("Received invalid response " + gatewayResponse + 
 					" from ACHCommerce Gateway");
 		}
 		String replyCode = returnData[0];
 		String description = returnData[1];
		String txnId = returnData[2];
		PaymentAuthorizationDTOEx paymentAuthDTO = new PaymentAuthorizationDTOEx();
		paymentAuthDTO.setCode1(replyCode);
		paymentAuthDTO.setResponseMessage(description);
		paymentAuthDTO.setProcessor(PROCESSOR_NAME);
		paymentAuthDTO.setTransactionId(txnId);
		paymentAuthDTO.setResult(replyCode.equals(PAYMENT_SUCCESS_CODE));
 		return paymentAuthDTO;
 	}
 }
