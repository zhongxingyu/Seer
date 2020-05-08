 /**
  * Copyright 2012 LivLiv Solutions
  *
  */
package com.stackmob.examples;
 
 
 //import com.google.gson.JsonElement;
 //import com.google.gson.JsonObject;
 //import com.google.gson.JsonParser;
 import com.stackmob.core.MethodVerb;
 import com.stackmob.core.PushServiceException;
 import com.stackmob.core.ServiceNotActivatedException;
 import com.stackmob.core.customcode.CustomCodeMethod;
 import com.stackmob.sdkapi.LoggerService;
 import com.stackmob.sdkapi.PushService;
 import com.stackmob.core.rest.ProcessedAPIRequest;
 import com.stackmob.core.rest.ResponseToProcess;
 import com.stackmob.sdkapi.SDKServiceProvider;
 import java.net.HttpURLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 public class SendPush implements CustomCodeMethod {
 
 
 	private final static String PUSH_RECEIVER = "receiver";
 	@Override
 	public String getMethodName() {
 		return "sendPush";
 	}
 
 	@Override
 	public List<String> getParams() {
 		List<String> params = new ArrayList<String>();
 		return params;
 	}
 
 	@Override
 	public ResponseToProcess execute(ProcessedAPIRequest request, SDKServiceProvider serviceProvider) {
 		final String username = request.getLoggedInUser();
 
 /*
 		if (request.getVerb() != MethodVerb.POST) {
 			HashMap<String, String> errParams = new HashMap<String, String>();
 			errParams.put("error", "not a post sorry...");
 			return new ResponseToProcess(HttpURLConnection.HTTP_BAD_REQUEST, errParams); // http 400 - bad request
 		}
 
 		LoggerService logger = serviceProvider.getLoggerService(SendPush.class);
 
 		//Lets parse what we got
 		final JsonParser parser = new JsonParser();
 		String receiver = null;
 		final Map<String, String> payload = new HashMap<String, String>();
 
 		try {
 			final JsonElement element = parser.parse( request.getBody() );
 
 			final JsonObject object = element.getAsJsonObject();
 
 			//All that comes goes :P, here probably some sanity check is needed
 			for (final Map.Entry<String,JsonElement> entry : object.entrySet()) {
 				payload.put(entry.getKey(), entry.getValue().getAsString());
 			}
 		} catch (Exception e1) {
 			logger.debug("Bad json");
 			final HashMap<String, String> errParams = new HashMap<String, String>();
 			errParams.put("error", "Bad json");
 			return new ResponseToProcess(HttpURLConnection.HTTP_BAD_REQUEST, errParams); // http 400 - bad request
 		} 
 		//Now we should have the needed params.
 
 		receiver = payload.get(PUSH_RECEIVER);
 		
 		logger.debug("push to " + receiver);
 		
 		if (receiver == null || receiver.isEmpty()) {
 			final HashMap<String, String> errParams = new HashMap<String, String>();
 			errParams.put("error", "receiver cannot be empty");
 			return new ResponseToProcess(HttpURLConnection.HTTP_BAD_REQUEST, errParams); // http 400 
 		}
 
 		final PushService pushService;
 		try {
 			pushService = serviceProvider.getPushService();
 		} catch (ServiceNotActivatedException e) {
 			logger.debug("Push not working");
 			final HashMap<String, String> errParams = new HashMap<String, String>();
 			errParams.put("error", "Push not working");
 			return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, errParams); // http 500 
 		}
 		
 		final List<String> receivers = new LinkedList<String>();
 		receivers.add(receiver);
 		try {
 			pushService.sendPushToUsers(receivers, payload );
 		} catch (PushServiceException e) {
 			logger.debug("Push failure");
 			final HashMap<String, String> errParams = new HashMap<String, String>();
 			errParams.put("error", "Push failure");
 			return new ResponseToProcess(HttpURLConnection.HTTP_INTERNAL_ERROR, errParams); // http 500 
 		}
 
 */
 		final Map<String, Object> response = new HashMap<String, Object>();
 		//response.put("receiver", receiver);
 		response.put("receiver", username);
 		return new ResponseToProcess(HttpURLConnection.HTTP_OK, response);
 
 	}
 }
