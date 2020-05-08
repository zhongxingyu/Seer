 package com.san.twilio.application;
 
 import java.util.Map;
 import java.util.HashMap;
 import com.twilio.sdk.TwilioRestClient;
 import com.twilio.sdk.TwilioRestException;
 import com.twilio.sdk.resource.instance.Account;
 import com.twilio.sdk.resource.instance.Call;
 import com.twilio.sdk.resource.factory.CallFactory;
 
 public class MakeCall {
 	public static final String ACCOUNT_SID = "AC5d9713c0c006630d5bf85ccbcc8134be";
 	public static final String AUTH_TOKEN = "a4a888fe8c43d99cd2a1a59fbbf75a83";
	public static final String HOST = "50.174.216.252";
//	public static final String URL = "http://"+ HOST  + ":8080/SpringDictionaryService/dictionary/getResponse";
//	public static final String URL = "http://"+ HOST + ":8080/SpringDictionaryService/dictionary/newService";
	public static final String URL = "http://"+ HOST  + ":8080/SpringDictionaryService/dictionary/getResponseWithPost";
 
 	public static void main(String[] args) throws TwilioRestException {
 		TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
 		Account mainAccount = client.getAccount();
 		CallFactory callFactory = mainAccount.getCallFactory();
 		Map<String, String> callParams = new HashMap<String, String>();
 		final String toNumber = "4153263626";
 		final String fromNumber = "3109862198";
 		callParams.put("To", toNumber); // Replace with your phone number
 		callParams.put("From", fromNumber); // Replace with a Twilio number
 		callParams.put("Url", URL);
//		callParams.put("Url", "http://demo.twilio.com/welcome/voice?fromNumber="+toNumber);
 		// Make the call
 		Call call = callFactory.create(callParams);
 		// Print the call SID (a 32 digit hex like CA123..)
 		System.out.println("sid - " + call.getSid());
 		System.out.println("dir - " + call.getDirection());
 		System.out.println("fro - " + call.getFrom());
 		System.out.println("answered by - " + call.getAnsweredBy());
 		System.out.println("price" + call.getPrice());
 		System.out.println("status - " + call.getStatus());
 		System.out.println("to - " + call.getTo());
 		System.out.println("callerName " + call.getCallerName());
 		System.out.println("start time - " + call.getStartTime());
 		System.out.println("duration - " + call.getDuration());
 	}
 }
