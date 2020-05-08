 package common;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import play.Logger;
 
 import com.google.i18n.phonenumbers.NumberParseException;
 import com.google.i18n.phonenumbers.PhoneNumberUtil;
 import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
 import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
 import com.twilio.sdk.TwilioRestClient;
 import com.twilio.sdk.TwilioRestException;
 import com.twilio.sdk.resource.factory.ApplicationFactory;
 import com.twilio.sdk.resource.factory.SmsFactory;
 import com.twilio.sdk.resource.instance.Account;
 import com.twilio.sdk.resource.instance.IncomingPhoneNumber;
 import com.twilio.sdk.resource.instance.Sms;
 import com.twilio.sdk.resource.list.AccountList;
 import com.twilio.sdk.resource.list.ApplicationList;
 import com.twilio.sdk.resource.list.IncomingPhoneNumberList;
 
 public class TwilioUtility {
 	// Find your Account Sid and Token at twilio.com/user/account
 	public static final String ACCOUNT_SID = "AC09e767fc3f77c6570adc1b5608166a16";
 	public static final String AUTH_TOKEN = "0139de0591840e8839a109412daa506f";
 	public static final String TWILIO_PHONE_NUM = "+16505675735";
 
 	public static void main(String[] args) throws TwilioRestException {
 		/*
 		 * sendSMS(ACCOUNT_SID, AUTH_TOKEN, TWILIO_PHONE_NUM, "+16506915076",
 		 * "This is a new payload string");
 		 * 
 		 * createApplication(ACCOUNT_SID, AUTH_TOKEN, "NewApp",
 		 * "http://localhost:9000/abcde/callbackurl", "GET");
 		 */
 		deleteAllApplications(ACCOUNT_SID, AUTH_TOKEN);
 	}
 
 	/**
 	 * 
 	 * @param sid
 	 * @param aToken
 	 * @param from
 	 * @param to
 	 * @param payload
 	 * @throws TwilioRestException
 	 */
 	public static String sendSMS(String sid, String aToken, String from,
 			String to, String recipientCountry, String payload)
 			throws TwilioRestException {
 		Logger.debug("Connecting to twilio %s:%s", sid, aToken);
 		TwilioRestClient client = new TwilioRestClient(sid, aToken);
 		String e164FormattedPhoneNumber = to;
 		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
 		try {
 			String countryCode = RegionUtil.getCountryCode(recipientCountry);
 			Logger.debug("Country code for country %s is %s.  ",
 					recipientCountry, countryCode);
 			PhoneNumber phoneNumber = phoneUtil.parse(to, countryCode);
 			e164FormattedPhoneNumber = phoneUtil.format(phoneNumber,
 					PhoneNumberFormat.E164);
 		} catch (NumberParseException e) {
 			Logger.error("NumberParseException was thrown: %s", e.toString());
 		}
 
 		// Build a filter for the SmsList
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("Body", payload);
		params.put("To", e164FormattedPhoneNumber);
 		params.put("From", from);
 		/* perform additional check on phone number to see if it can receive SMS */
 		params.put("ForceDelivery", "false");
 
 		Logger.debug("Trying to send message %s to %s.  ", payload,
 				e164FormattedPhoneNumber);
 		try {
 			SmsFactory messageFactory = client.getAccount().getSmsFactory();
 			Sms message = messageFactory.create(params);
 			Logger.debug("Successfully sent message to %s.  SMS id : %s",
 					e164FormattedPhoneNumber, message.getSid());
 			return message.getStatus();
 		} catch (TwilioRestException te) {
 			Logger.error("Unable to send message to %s",
 					e164FormattedPhoneNumber);
 			return "error : " + te.getErrorMessage();
 		}
 	}
 
 	public static AccountList getAccounts(String sid, String aToken) {
 		AccountList accounts = null;
 		try {
 			TwilioRestClient client = new TwilioRestClient(sid, aToken);
 
 			// Build a filter for the AccountList
 			Map<String, String> params = new HashMap<String, String>();
 			params.put("Status", "active");
 			accounts = client.getAccounts(params);
 
 			// Loop over accounts and print out a property
 			for (Account account : accounts) {
 				System.out.println(account.getFriendlyName());
 			}
 		} catch (Exception e) {
 			return null;
 		}
 
 		return accounts;
 	}
 
 	/**
 	 * 
 	 * @param sid
 	 * @param aToken
 	 * @param friendlyName
 	 * @param smsUrl
 	 * @param smsMethod
 	 *            - GET or POST
 	 * @throws TwilioRestException
 	 */
 	public static String createApplication(String sid, String aToken,
 			String friendlyName, String smsUrl, String smsMethod)
 			throws TwilioRestException {
 		Logger.debug("Connecting to twilio %s:%s", sid, aToken);
 		TwilioRestClient client = new TwilioRestClient(sid, aToken);
 
 		// Build a filter for the ApplicationList
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("FriendlyName", friendlyName);
 		params.put("SmsUrl", smsUrl);
 		params.put("SmsMethod", smsMethod);
 
 		ApplicationFactory appFactory = client.getAccount()
 				.getApplicationFactory();
 		Logger.debug("Trying to create a new application with url:%s", smsUrl);
 		com.twilio.sdk.resource.instance.Application app = appFactory
 				.create(params);
 		Logger.debug("Successfully created a new application with id:%s",
 				app.getSid());
 
 		return app.getSid();
 	}
 
 	public static void deleteAllApplications(String sid, String aToken)
 			throws TwilioRestException {
 		TwilioRestClient client = new TwilioRestClient(sid, aToken);
 		ApplicationList appList = client.getAccount().getApplications();
 		for (com.twilio.sdk.resource.instance.Application app : appList) {
 			app.delete();
 		}
 	}
 
 	public static void deleteApplication(String sid, String aToken, String appId) {
 		TwilioRestClient client = new TwilioRestClient(sid, aToken);
 		ApplicationList appList = client.getAccount().getApplications();
 		for (com.twilio.sdk.resource.instance.Application app : appList) {
 			if (app.getSid().equals(appId)) {
 				try {
 					Logger.info("Deleting application with id:%s", app.getSid());
 					app.delete();
 					Logger.info("Application successfully deleted");
 				} catch (TwilioRestException e) {
 					Logger.info("Could not delete Application [%s]",
 							e.getMessage());
 				}
 			}
 		}
 	}
 
 	public static boolean setCallbackUrl(String sid, String aToken,
 			String callbackUrl, String smsGatewayPhoneNumber) {
 		TwilioRestClient client = new TwilioRestClient(sid, aToken);
 
 		IncomingPhoneNumberList numbers = client.getAccount()
 				.getIncomingPhoneNumbers();
 		for (IncomingPhoneNumber number : numbers) {
 			Logger.debug("Found number %s", number.getPhoneNumber());
 			if (number.getPhoneNumber().equals(smsGatewayPhoneNumber)) {
 				Logger.info("Found matching number %s", number.getPhoneNumber());
 
 				Map<String, String> params = new HashMap<String, String>();
 				params.put("SmsUrl", callbackUrl);
 				try {
 					number.update(params);
 				} catch (TwilioRestException e) {
 					Logger.error("Unable to configure callback Url : %s",
 							e.getMessage());
 					return false;
 				}
 
 				Logger.info(
 						"Inbound msgs to number %s will be forwarded to %s",
 						number.getPhoneNumber(), callbackUrl);
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
