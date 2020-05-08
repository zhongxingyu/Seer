package com.letsdoapps.commons.telephonynexmo;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.log4j.Logger;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.i18n.phonenumbers.NumberParseException;
 import com.google.i18n.phonenumbers.PhoneNumberUtil;
 import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
 import com.letsdoapps.commons.http.HttpRequest;
 import com.letsdoapps.commons.http.HttpResponse;
 import com.letsdoapps.commons.http.HttpRequest.Method;
 import com.letsdoapps.commons.telephony.SMSService;
 
 public class NexmoHelper implements SMSService {
 
 	private static final String NEXMO_API_URL = "https://rest.nexmo.com";
 
 	private static final String NEXMO_SMS_URL = NEXMO_API_URL + "/sms/json";
 
 	private static final String NEXMO_NUMBERS_URL = NEXMO_API_URL + "/account/numbers/%s/%s";
 
 	private static final Logger logger = Logger.getLogger(NexmoHelper.class);
 
 	private String username;
 
 	private String password;
 
 	private BlockingQueue<String> usNumbers = new LinkedBlockingQueue<String>();
 
 	private BlockingQueue<String> caNumbers = new LinkedBlockingQueue<String>();
 
 	private final List<Thread> sendingThreads = new LinkedList<Thread>();
 
 	public NexmoHelper(String username, String password) {
 		this.username = username;
 		this.password = password;
 		reloadNumbers();
 	}
 
 	public void send(String to, String msg, SMSServiceCallback callback) {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("to", to);
 		params.put("text", msg);
 		params.put("username", username);
 		params.put("password", password);
 		if (to.startsWith("+1")) {
 			/* IF WE SEND TO US OR CANADIAN NUMBER,
 			 * WE NEED TO SET THE RIGHT FROM NUMBER FROM OUR NEXMO ACCOUNT.
 			 * THOSE NUMBERS CAN ONLY SEND 1SMS/second, SO CREATE A TASK,
 			 * THAT POLLS FOR A NUMBER AND OFFER IT BACK AFTER ONE SECOND.
 			 */
 	    	PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
 	    	try {
 				PhoneNumber pn = phoneUtil.parse(to, null);
 				String countryCode = phoneUtil.getRegionCodeForNumber(pn);
 				if (countryCode.equals("US")) {
 					createSendingThread(usNumbers, params, callback);
 					return;
 				} else if (countryCode.equals("CA")) {
 					createSendingThread(caNumbers, params, callback);
 					return;
 				}
 			} catch (NumberParseException e) {
 				logger.error("Failed to parse phone number in SMS sending... VERY strange: " + to);
 				if (callback != null) {
 					callback.result(to, false);
 				}
 			}
 		}
 		
 		// If we reach here, it's not an US / CA number, send it right away.
 		params.put("from", "MobiGuard");
 		logger.debug("DIRECT: Sending message to " + params.get("to"));
 		sendSmsRequest(params, callback);
 	}
 
 	/*
 	 * Method that actually issues the SMS send request.
 	 */
 	private void sendSmsRequest(Map<String, String> msg, SMSServiceCallback callback) {
 		HttpRequest request = new HttpRequest(Method.POST, NEXMO_SMS_URL);
 		request.setParameters(msg);
 
 		HttpResponse response = request.request();
 		if (response.success() == false) {
 			logger.error("Failed to send Nexmo sms.");
 			logger.debug(response.getBody());
 		}
 		if (callback != null) {
 			callback.result(msg.get("to"), response.success());
 		}
 	}
 
 	/*
 	 * Reload purchased numbers from the Nexmo account
 	 */
 	public void reloadNumbers() {
 		logger.info("Reloading purchased numbers from Nexmo account.");
 		String url = String.format(NEXMO_NUMBERS_URL, username, password);
 		HttpRequest request = new HttpRequest(Method.GET, url);
 
 		Map<String, String> headers = new HashMap<String, String>();
 		headers.put("Accept", "application/json");
 		request.setHeaders(headers);
 		HttpResponse response = request.request();
 		if (response.success() == false) {
 			logger.error("Failed to load US/CA numbers from Nexmo API");
 			logger.debug(response.getBody());
 		} else {
 			JsonObject obj = new JsonParser().parse(response.getBody()).getAsJsonObject();
 			if (obj.get("count").getAsInt() > 0) {
 				for (JsonElement numberElement : obj.get("numbers").getAsJsonArray()) {
 					JsonObject numObj = numberElement.getAsJsonObject();
 					String country = numObj.get("country").getAsString();
 					String number = numObj.get("msisdn").getAsString();
 					if (country.equals("US")) {
 						usNumbers.add(number);
 					} else if (country.equals("CA")) {
 						caNumbers.add(number);
 					}
 					logger.debug("Added number " + number + " for country " + country);
 				}
 			}
 		}
 	}
 
 	private void createSendingThread(BlockingQueue<String> numbersQueue, Map<String, String> msg, SMSServiceCallback callback) {
 		Thread t = new SendMessageTask(this, numbersQueue, msg, callback);
 		synchronized (sendingThreads) {
 			sendingThreads.add(t);
 		}
 		t.start();
 	}
 
 	public void notifySendingThreadFinished(Thread t) {
 		synchronized (sendingThreads) {
 			sendingThreads.remove(t);
 		}
 	}
 
 	public int getNbSendingThreads() {
 		synchronized (sendingThreads) {
 			return sendingThreads.size();
 		}
 	}
 
 	private static class SendMessageTask extends Thread {
 		private NexmoHelper helper;
 		private BlockingQueue<String> numbers;
 		private Map<String, String> msg;
 		private SMSServiceCallback callback;
 		
 		public SendMessageTask(NexmoHelper helper, BlockingQueue<String> numbers, Map<String, String> msg, SMSServiceCallback callback) {
 			this.helper = helper;
 			this.numbers = numbers;
 			this.msg = msg;
 			this.callback = callback;
 		}
 
 		@Override
 		public void run() {
 			try {
 				String number = numbers.take();
 				logger.debug("TASK: Sending message to " + msg.get("to") + " using number " + number);
 				msg.put("from", number);
 				helper.sendSmsRequest(msg, callback);
 				try { Thread.sleep(1050L); } catch (Exception e2) {}
 				numbers.offer(number);
 			} catch (InterruptedException e) {
 				logger.error("Failed to take number from queue !");
 				e.printStackTrace();
 			}
 			helper.notifySendingThreadFinished(this);
 		}
 	}
 
 }
