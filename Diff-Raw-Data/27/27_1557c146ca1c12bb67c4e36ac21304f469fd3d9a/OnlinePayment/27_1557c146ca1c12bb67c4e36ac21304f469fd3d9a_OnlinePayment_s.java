 package org.alt60m.util;
 import java.util.Hashtable;
 import org.alt60m.util.AuthNet;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Created Aug 6, 2002, by David Bowdoin
  * This class processes online payments via www.authorizenet.com
 
 Fields in Hashtable paymentInfoIn
   Description, InvoiceNum, CustID, PaymentAmt, Freight, Tax
   FirstName, LastName, Address1, Address2, City, State, Zip, Country, Phone, Email
  Required for EChecks - CustomerIP, Ssn, DLNum, DLState, DLDob
 
  /// Testing Information ///
 
  TEST CARD NUMBERs
  370000000000002 American Express
  6011000000000012 Discover
  5424000000000015 MasterCard
  4007000000027 Visa
 
  There is also a test credit card number that can be used to generate errors.
  THIS CARD IS INTENDED TO PRODUCE ERRORS, and should only be used if that is the intent.
  To cause the system to generate a specific error, set the account to Test Mode and submit a
  transaction with the card number 4222222222222. The system will return the response reason
  code equal to the amount of the submitted transaction. For example, to test response reason
  code number 27, a test transaction would be submitted with the credit card number,
  "4222222222222," and the amount, "27.00."
 
  */
 public class OnlinePayment {
 
 	private static Log log = LogFactory.getLog(OnlinePayment.class);
 	
 	
     private String merchantAcctNum = "";
     private String merchantAcctPassword = "";
     private boolean testMode = false;
     private String postData = "";
     private String postResponse = "";
     private Hashtable paymentInfo;
 
     public boolean isTestMode() {
         return testMode;
     }
 
     public void setTestMode(boolean testMode) {
         this.testMode = testMode;
     }
 
     public void setMerchantInfo(String MerchantAcctNum, String Password) {
         merchantAcctNum = MerchantAcctNum;
         merchantAcctPassword = Password;
     }
 
     public String getPostResponse() {
         return postResponse;
     }
 
     public String getPostData() {
         return postData;
     }
     //Created:  7/17/02, DMB
 // Expects: Hashtable paymentInfo (ccNum,ccExpM,ccExpY,ccAmt,FirstName,LastName,Address,City,State,Zip,Country,Phone,Email )
 // Returns: Hashtable (Status=Success,AuthCode,Post,Response)
     public Hashtable processCreditCard(Hashtable paymentInfoIn) throws Exception {
         Hashtable results = new Hashtable();
         results.put("Status","Could not connect to payment system. Please try again later.");
 
         paymentInfo = paymentInfoIn;
         buildBasePostData();
 		postData += ",x_Exp_Date=" + paymentInfo.get("CCExpM") + "/" + paymentInfo.get("CCExpY");
         addValuePair("x_Card_Num", "CCNum");
         postData = postData.replace('\'', ' ');
 
         return doAuthNetPost();
     }
 
     public Hashtable processECheck(Hashtable paymentInfoIn) throws Exception {
         Hashtable results = new Hashtable();
         results.put("Status","Could not connect to payment system. Please try again later.");
 
         paymentInfo = paymentInfoIn;
         buildBasePostData();
         postData += ",x_Method=ECHECK";
         addValuePair("x_Bank_ABA_Code", "BankABACode");
         addValuePair("x_Bank_Acct_Num", "BankAcctNum");
         addValuePair("x_Bank_Name", "BankName");
 
         postData = postData.replace('\'', ' ');
 
         return doAuthNetPost();
     }
 
     private void addValuePair(String remoteName, String ourName) throws Exception {
         if(paymentInfo.get(ourName)!=null)
             postData += "," + remoteName + "=" + ((String)paymentInfo.get(ourName)).replace(',',' ');
     }
 
     private void buildBasePostData() throws Exception {
         postData = testMode?"x_Test_Request=True,":"";  //Test mode?
 		postData += "x_ADC_Delim_Data=TRUE,x_ADC_URL=FALSE";				//specify our servlet connection (not a html redirect)
         postData += ",x_Login=" + merchantAcctNum;
         postData += ",x_tran_key=" + merchantAcctPassword;
 
         addValuePair("x_Description", "Description");
         addValuePair("x_Invoice_Num", "InvoiceNum");
         addValuePair("x_Cust_ID", "CustID");
 
         addValuePair("x_First_Name", "FirstName");
         addValuePair("x_Last_Name", "LastName");
         postData += ",x_Address=" + ((String)paymentInfo.get("Address1")).replace(',',' ') + ((String)paymentInfo.get("Address2")).replace(',',' ');
         addValuePair("x_City", "City");
         addValuePair("x_State", "State");
         addValuePair("x_Zip", "Zip");
         addValuePair("x_Country", "Country");
 
         addValuePair("x_Phone", "Phone");
         addValuePair("x_Email", "Email");
 
         addValuePair("x_Amount", "PaymentAmt");
         //For these two, if no value is specified, it puts in a "0" ammount.
         postData += ",x_Freight=" + ((paymentInfo.get("Freight")!=null) ? paymentInfo.get("Freight") : "0");
         postData += ",x_Tax=" + ((paymentInfo.get("Tax")!=null) ? paymentInfo.get("Tax") : "0");
 
         addValuePair("x_Customer_IP", "CustomerIP");
 
         addValuePair("x_Customer_Tax_ID", "Ssn");
         addValuePair("x_Drivers_License_Num", "DLNum");
         addValuePair("x_Drivers_License_State", "DLState");
         addValuePair("x_Drivers_License_DOB", "DLDob");
         addValuePair("x_version", "3.1");
     }
 
     private Hashtable doAuthNetPost() throws Exception {
         Hashtable<String, String> results = new Hashtable<String, String>();
 
         AuthNet authNetObj = new AuthNet();
 		authNetObj.enableLogging();
 		authNetObj.doSSLPost(postData);
 
         results.put("Status","Could not connect to payment system. Please try again later.");
 
         if (authNetObj.postSuccess()) {
             String responseReasonText = authNetObj.getField(4);
 			results.put("Response",String.valueOf(responseReasonText));
             
             String responseCode = authNetObj.getField(1);
 			if (responseCode.equals("1")){ //Approved
                 results.put("Status","Success");
                 String authCode = authNetObj.getField(5);
 				results.put("AuthCode",authCode);
                 log.info("Successful credit card transaction; AuthCode: " + authCode);
             } else if (responseCode.equals("2")) { //Declined
                 results.put("Status","Error");
                 log.info("Credit card transaction declined; reason: " + responseReasonText);
             } else if ((responseCode.equals("3"))) { // Error
				String errorInfo = "Response Subcode: "
						+ authNetObj.getField(2) + "; "
						+ "Response Reason Code: " + authNetObj.getField(3)
						+ "; " + "Response Reason Text: " + responseReasonText;
				log.error("Error processing credit card: " + errorInfo);
             	results.put("Status","Error");
             } else {
             	log.error("unknown response code: " + responseCode);
             	results.put("Status","Error");
             }
 
             postResponse = authNetObj.getResponseString();
         } else {
         	log.error("Failure to connect with authnet: " + authNetObj.getErrorMessage() + " (" + authNetObj.getErrorCode() + ")");
         }
         if(testMode) log.debug("postData: " + postData);
         if(testMode) log.debug("postResponse: " + postResponse);
         log.debug("Results: " + results);
 
         return results;
     }
 }
