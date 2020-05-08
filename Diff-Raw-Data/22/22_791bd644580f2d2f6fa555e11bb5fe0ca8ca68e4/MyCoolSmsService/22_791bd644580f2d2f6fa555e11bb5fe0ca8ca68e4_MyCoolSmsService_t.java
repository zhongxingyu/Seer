 package services.sms;
 
 
 
 import play.Logger;
 import play.Play;
 import play.libs.WS;
 import play.libs.WS.HttpResponse;
 
 import com.google.gson.JsonObject;
 
 
 /**
  * My Cool SMS Api
  * http://www.my-cool-sms.com/
  */
 public class MyCoolSmsService implements SmsService {
 	
 	private static final String ENDPOINT = "https://www.my-cool-sms.com/api-socket.php";
 	
 	
 	public enum Error{
 		
 		LOGIN_FAILED(101, "Login Failed. Wrong Username or Password.");
 		
 		private final int code;
 		private final String message;
 		
 		private Error(final int code, final String message) {
 			this.message = message;
 			this.code = code;
 		}
 		
 		public String getMessage() {
 			return message;
 		}
 		
 		public static Error forNumber(final int code){
 			for(Error error:values()){
 				if(error.code == code){
 					return error;
 				}
 			}
 			return null;
 		}
 		
 		
 //		101	Login Failed. Wrong Username or Password.	
 //		102	This account has not been activated yet.	
 //		103	Mandatory parameter missing	
 //		104	Invalid Request. No GET, POST and raw JSON data found. If you are trying to send JSON, please check if it is properly encoded.	
 //		105	Unknown Function	
 //		106	Permission Denied	
 //		107	Generic API Error	
 //		108	Invalid request structure	
 //		109	Not enough credits
 		
 //		201	Invalid UCS2 Input
 		
 		
 //		210	The number seems to be invalid	
 //		211	The message parameter is empty	
 //		212	The message parameter is too long	
 //		213	The unicode parameter is set to force Unicode but the message parameter was not submitted in UCS2 notation.	
 //		214	The unicode parameter is set to force GSM but the message parameter was submitted with UCS2 notation	
 //		215	The unicode parameter is set to force GSM but the message parameter contains characters outside the GSM7 alphabet	
 //		216	The input for Sender ID is invalid. You may use a phone number or an alphanumeric text with up to eleven characters (A-Z, a-z, 0-9 and the dash symbol).	
 //		217	Shortcode Sender IDs are not permitted	
 //		218	The callbackurl parameter seems to be invalid	
 //		219	The schedule time seems invalid. Use ATOM format and timezone, i.e.: "2011-04-17T17:59:36.67+08" or "2011-04-17 17:59:36-02"
 		
 //		220	The unique id is not associated with any of your mailing lists
 //		221	Input for {field} is too long.
 //		222	There is no subscriber with that phone number on this mailing list
 		
 //		223	Unknown SMS ID	
 //		224	Unknown Group Key
 //		225	Invalid limit parameter
 		
 //		226	HLR lookup failed. No charge applies.
 		
 //		232	The number seems to be invalid	
 //		233	The virtual number is either invalid or not registered with this user account. Please contact service@my-cool-sms.com for assistance.	
 //		234	The provided thresholdid is invalid	
 //		235	Invalid limit parameter	
 //		236	Invalid order parameter
 		
 //		237	The number could not be normalized.
 		
 	}
 	
 	/**
 	 * @param number The cell phone number in international format. For example: 49123456789 or +49123456789 or 0049123456789	string
 	 * @param message The message body. My-Cool-SMS auto-detects the input encoding and processes the SMS accordingly as GSM or Unicode.
	 * @param senderId The sender id to use or null for default
 	 * 
 	 * You can use the optional Unicode parameter to force a particular encoding. If Unicode is set to true the SMS will be sent as Unicode 
 	 * and the message parameter must be provided in Unicode UCS2 notation. If Unicode is set to false the SMS will be sent as GSM 
 	 * and the message parameter must contain characters from the GSM7 alphabet only.
 	 * Long messages are automatically concatenated. Maximum 765 characters for GSM messages or 335 for Unicode.
 	 * Please note that you should always use the unicode parameter when sending GET requests.
 	 * 
 	 * @throws SmsException 
 	 */
 	@Override
 	public void sendSms(final String number, final String message, final String senderId) throws SmsException{
 		
 		JsonObject requestBody = buildAuthenticatedRequestBody("sendSms");
 		
 		if(senderId != null && !senderId.trim().isEmpty()){
 			//senderid	Cell phone number in international format, for example: +44123456789 or alpha‐numeric sender id up to 11 characters, for example: Company
			requestBody.addProperty("senderid", senderId);
 		}
		requestBody.addProperty("number", number);
		requestBody.addProperty("message", message);
 		
 		HttpResponse response = WS.url(ENDPOINT).body(requestBody).post();
 		Logger.debug("Response = > " + response.getString());
 		JsonObject responseBody = response.getJson().getAsJsonObject();
 		boolean success = responseBody.get("success").getAsBoolean();
 		if(success){
 			String smsid = responseBody.get("smsid").getAsString();
 			Logger.info("SMS sent to %s with id %s and content [%s]", number, smsid, message);
 		}else{
 			int errorCode = responseBody.get("errorcode").getAsInt();
 			Error error = Error.forNumber(errorCode);
 			String description = responseBody.get("description").getAsString();
 			throw new SmsException("Send sms failed: " + errorCode + " " + description);
 		}
 	}
 
 	private JsonObject buildAuthenticatedRequestBody(final String function) {
 		JsonObject requestBody = new JsonObject();
 		requestBody.addProperty("username", Play.configuration.getProperty(SMS.SMS_USERNAME, ""));
 		requestBody.addProperty("password", Play.configuration.getProperty(SMS.SMS_PASSWORD, ""));
 		requestBody.addProperty("function", function);
 		return requestBody;
 	}
 	
 	public static class SmsException extends Exception{
 		private SmsException(String message, Throwable cause) {
 			super(message, cause);
 		}
 
 		private SmsException(String message) {
 			super(message);
 		}
 	}
 	
 //	{
 //    "username":"xxx",
 //    "password":"yyy",
 //    "function":"sendSms",
 //    "number":"+491234567890",
 //    "message":"Have a nice day!",
 //    "senderid":"+449876543210",
 //    "callbackurl":"http://www.my-server.com/callback.php"
 //}
 	
 //	{
 //	    "success":true,
 //	    "smsid":"ce184cc0a6d1714d1ac763f4fe89f521",
 //	    "body":"Have a nice day!",
 //	    "bodyucs2":"0048006100760065002000610020006E00690063006500200064",
 //	    "bodygsm7":"486176652061206E6963652064617921",
 //	    "number":"+491234567890"
 //	    "senderid":"+449876543210",
 //	    "senderidenabled":true,
 //	    "unicode":false,
 //	    "numchars":321,
 //	    "escapenumchars":0,
 //	    "smscount":3,
 //	    "charge":0.112,
 //	    "balance":752.121,
 //	    "callbackurl":"http://www.my-server.com/callback.php"
 //	}
 
 
 //{
 //    "username":"xxx",
 //    "password":"yyy",
 //    "function":"getBalance"
 //}
 
 //{
 //    "success":true,
 //    "smsid":"ce184cc0a6d1714d1ac763f4fe89f521",
 //    "status":"SMS_STATUS_DELIVERED"
 //}
 
 //{
 //    "success":false,
 //    "errorcode":"101",
 //    "description":"Login Failed. Wrong Username or Password."
 //}
 
 }
 
 
