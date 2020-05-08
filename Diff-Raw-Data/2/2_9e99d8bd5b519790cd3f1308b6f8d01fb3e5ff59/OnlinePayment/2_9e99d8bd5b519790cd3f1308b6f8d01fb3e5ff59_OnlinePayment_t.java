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
         Hashtable results = new Hashtable();
 
         AuthNet authNetObj = new AuthNet();
 		authNetObj.enableLogging();
 		authNetObj.doSSLPost(postData);
 
         results.put("Status","Could not connect to payment system. Please try again later.");
 
         if (authNetObj.postSuccess()) {
             results.put("Response",String.valueOf(authNetObj.getField(4)));
             if (authNetObj.getField(1).equals("1")){
                 results.put("Status","Success");
                 results.put("AuthCode",authNetObj.getField(5));
             }
             else {
                 results.put("Status","Error");
                 results.put("ErrorCode",String.valueOf(authNetObj.getErrorCode()));
                 results.put("ErrorMessage",authNetObj.getErrorMessage());
             }
 
             postResponse = authNetObj.getResponseString();
         }
         if(testMode) log.debug("postData: " + postData);
         if(testMode) log.debug("postResponse: " + postResponse);
         log.debug("Results: " + results);
 
         return results;
     }
 }
