 /*
  * Copyright (C) 2012-2013 Mikhail Blinov
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; If not, see <http://www.gnu.org/licenses/>.
  */
 package com.mikebl71.android.websms.connector.cabbage;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import de.ub0r.android.websms.connector.common.Connector;
 import de.ub0r.android.websms.connector.common.ConnectorCommand;
 import de.ub0r.android.websms.connector.common.ConnectorSpec;
 import de.ub0r.android.websms.connector.common.ConnectorSpec.SubConnectorSpec;
 import de.ub0r.android.websms.connector.common.Log;
 import de.ub0r.android.websms.connector.common.Utils;
 import de.ub0r.android.websms.connector.common.Utils.HttpOptions;
 import de.ub0r.android.websms.connector.common.WebSMSException;
 import de.ub0r.android.websms.connector.common.WebSMSNoNetworkException;
 
 /**
  * Main class for Cabbage Connector.
  * Receives commands from WebSMS and acts upon them.
  */
 public class CabbageConnector extends Connector {
 
 	// Logging tag
 	private static final String TAG = "cabbage";
 
 	// Id of the dummy subconnector
 	private static final String DUMMY_SUB_CONNECTOR_ID = "0";
 
 	// Timeout for establishing a connection and waiting for a response from the server
 	private static final int CONN_TIMEOUT_MS = 60000;
 
 	// HTTP request properties
 	private static final String ENCODING = "UTF-8";
 
 	// Parameters for Cabbage send script
 	private static final String PARAM_PROVIDER   = "s";
 	private static final String PARAM_USERNAME   = "u";
 	private static final String PARAM_PASSWORD   = "p";
	private static final String PARAM_SENDER     = "name";
 	private static final String PARAM_RECIPIENTS = "d";
 	private static final String PARAM_TEXT       = "m";
 	private static final String PARAM_BALANCE_ONLY = "c";
 	private static final String PARAM_CAPTCHA_ANSWER = "cap";
 
 	// Regex for extracting the first number from the script response
 	private static final Pattern FIRST_NUMBER = Pattern.compile("^(-?\\d+)");
 
 	// Prefix used by resources with error messages
 	private static final String ERR_MESSAGE_PREFIX = "cabbage_err_";
 
 	// Return codes for trySendingData method 
 	private static final int SENT_DONE         = 0; 
 	private static final int SENT_NEED_CAPTCHA = 1; 
 
 	// Timeout for waiting a captcha answer from a user
     private static final long CAPTCHA_ANSWER_TIMEOUT = 60000;
 
 	// Sync object for solving a captcha
     private static final Object CAPTCHA_SYNC = new Object();
     // Captcha answer returned by user or a solver app 
     private static String receivedCaptchaAnswer;
 
 	/**
 	 * Initializes {@link ConnectorSpec}. This is only run once. Changing properties are set in updateSpec(). 
 	 * Registers subconnectors and sets up the connector. UpdateSpec() is called later.
 	 */
 	@Override
 	public final ConnectorSpec initSpec(final Context context) {
 		final String connectorName = context.getString(R.string.connector_cabbage_name);
 
 		// create ConnectorSpec
 		final ConnectorSpec connectorSpec = new ConnectorSpec(connectorName);
 		connectorSpec.setAuthor(context.getString(R.string.connector_cabbage_author));
 		connectorSpec.setBalance(null);
 
 		connectorSpec.setCapabilities(ConnectorSpec.CAPABILITIES_UPDATE
 				| ConnectorSpec.CAPABILITIES_SEND
 				| ConnectorSpec.CAPABILITIES_PREFS);
 		
 		// init subconnectors
 		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		final List<String> accIds = AccountPreferences.getAccountIds(prefs);
 
 		if (accIds.size() > 0) {
 			for (String accId : accIds) {
 				connectorSpec.addSubConnector(accId, 
 						AccountPreferences.getLabel(prefs, accId),
 						SubConnectorSpec.FEATURE_MULTIRECIPIENTS);
 			}
 			Log.d(TAG, "initSpec: inited with " + accIds.size() + " subconnectors");
 		} else {
 			// WebSMS requires connectors to have at least one subconnector hence creating a dummy one
 			connectorSpec.addSubConnector(DUMMY_SUB_CONNECTOR_ID, 
 					"dummy",
 					SubConnectorSpec.FEATURE_NONE);
 			Log.d(TAG, "initSpec: inited with dummy subconnector");
 		}
 
 		return connectorSpec;
 	}
 
 	/**
 	 * Updates connector's status. 
 	 */
 	@Override
 	public final ConnectorSpec updateSpec(final Context context, final ConnectorSpec connectorSpec) {
 		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 
 		if (CabbageConnectorPreferences.isEnabled(prefs) && CabbageConnectorPreferences.isValid(prefs)) {
 			connectorSpec.setReady();
 			Log.d(TAG, "updateSpec: set ready");
 		} else {
 			connectorSpec.setStatus(ConnectorSpec.STATUS_INACTIVE);
 			Log.d(TAG, "updateSpec: set inactive");
 		}
 
 		return connectorSpec;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onReceive(final Context context, final Intent intent) {
 		if (CaptcherSolverClient.isResponseIntent(context, intent)) {
 			processCaptchaSolverAnswer(context, intent);
 		} else {
 			super.onReceive(context, intent);
 		}
 	}
 
 	/**
 	 * Called when a new request is received.
 	 */
 	@Override
 	protected void onNewRequest(final Context context, final ConnectorSpec reqSpec, final ConnectorCommand command) {
 		// restore balance info that we might have lost from the request
 		if (reqSpec != null) {
 			final ConnectorSpec connSpec = this.getSpec(context);
 			SubConnectorSpec[] connSubs = connSpec.getSubConnectors();
 			SubConnectorSpec[] reqSubs = reqSpec.getSubConnectors();
 			
 			for (int idx = 0; idx < connSubs.length && idx < reqSubs.length; idx++) {
 				final String connBalance = connSubs[idx].getBalance();
 				final String reqBalance = reqSubs[idx].getBalance();
 	
 				if (connBalance == null && reqBalance != null) {
 					connSubs[idx].setBalance(reqBalance);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Called to update balance. Updates subconnector's balances concurrently.
 	 */
 	@Override
 	protected void doUpdate(final Context context, final Intent intent) {
 		final ConnectorSpec cs = this.getSpec(context);
 		final int subCount = cs.getSubConnectorCount();
 		final SubConnectorSpec[] subs = cs.getSubConnectors();
 
 		final List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(subCount);
 		for (SubConnectorSpec sub : subs) {
 			final String subId = sub.getID();
 
 			tasks.add(new Callable<Void>() {
 				public Void call() throws Exception {
 					// clone intent and assign it to this sub connector
 					final Intent subIntent = new Intent(intent);
 					ConnectorCommand cmd = new ConnectorCommand(subIntent);
 					cmd.setSelectedSubConnector(subId);
 					cmd.setToIntent(subIntent);
 					// update balance for this subconnector
 					sendData(context, new ConnectorCommand(subIntent));
 					return null;
 				}
 			});
 		}
 
 		try {
 			final ExecutorService executor = Executors.newFixedThreadPool(subCount);
 			// execute all updates in parallel and wait till all are complete
 			final List<Future<Void>> results = executor.invokeAll(tasks);
 			executor.shutdownNow();
 
 			// if any of the updates failed then re-throw the first exception
 			// (which will then be returned to WebSMS)
 			for (int idx = 0; idx < results.size(); idx++) {
 				Future<Void> result = results.get(idx);
 				try {
 					result.get();
 				} catch (ExecutionException ex) {
 					String subName = subs[idx].getName();
 					throw new WebSMSException(subName + ": " + ConnectorSpec.convertErrorMessage(context, ex.getCause()));
 				}
 			}
 		} catch (InterruptedException ex) {
 			Thread.currentThread().interrupt();
 		}
 	}
 
 	/**
 	 * Called to send the actual message.
 	 */
 	@Override
 	protected void doSend(final Context context, final Intent intent)
 			throws IOException {
 		sendData(context, new ConnectorCommand(intent));
 	}
 
 	/**
 	 * Communicates with Cabbage server to send a message or to request the current balance.
 	 */
 	private void sendData(final Context context, final ConnectorCommand command)
 			throws IOException {
 
 		// check network availability
 		if (!Utils.isNetworkAvailable(context)) {
 			throw new WebSMSNoNetworkException(context);
 		}
 
 		int sendRes = trySendingData(context, command, null);
 
 		if (sendRes == SENT_NEED_CAPTCHA) {
 			boolean canUseCaptchaSolver = CaptcherSolverClient.canUse(context);
 			boolean wasSolverUsed = false;
 
 			int attempts = 0;
 			while (sendRes == SENT_NEED_CAPTCHA) {
 				++attempts;
 
 				Bitmap captcha = retrieveCaptcha(context, command);
 
 				String captchaAnswer = null;
 				if (canUseCaptchaSolver) {
 					int remainingAttempts = CaptcherSolverClient.getMaxAttempts() - attempts;
 					if (remainingAttempts >= 0) {
 						captchaAnswer = solveCaptchaWithSolver(context, captcha);
 						wasSolverUsed = true;
 
 						if (TextUtils.isEmpty(captchaAnswer) && remainingAttempts > 0) {
 							captchaAnswer = "x";    // request another captcha
 						}
 					}
 				}
 
 				if (TextUtils.isEmpty(captchaAnswer)) {
 					captchaAnswer = solveCaptchaWithUser(context, captcha, wasSolverUsed);
 				}
 
 				if (TextUtils.isEmpty(captchaAnswer)) {
 					throw new WebSMSException(context, R.string.error_captcha_not_solved);
 				}
 
 				sendRes = trySendingData(context, command, captchaAnswer);
 			}
 		}
 	}
 	
 	/**
 	 * First attempt to communicates with Cabbage server.
 	 * Returns SENT_DONE or SENT_NEED_CAPTCHA.
 	 */
 	private int trySendingData(final Context context, final ConnectorCommand command, final String captchaAnswer)
 			throws IOException {
 		Log.d(TAG, "trying to send request to the server");
 		int res = SENT_DONE;
 
 		final ConnectorSpec cs = this.getSpec(context);
 		final String accId = command.getSelectedSubConnector();
 
 		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		final String provider = AccountPreferences.getProvider(prefs, accId);
 
 		// prepare web request
 		final HttpOptions o = new HttpOptions(ENCODING);
 		final ArrayList<BasicNameValuePair> d = new ArrayList<BasicNameValuePair>();
 		populateCommonOptions(o, d, cs, accId, provider, prefs);
 
 		o.url = CabbageConnectorPreferences.getCabbageUrl(prefs, provider);
 
 		String text = command.getText();
 		if (text != null && text.length() > 0) {
			addParam(d, PARAM_SENDER, command.getDefSender());
 			addParam(d, PARAM_RECIPIENTS, Utils.joinRecipientsNumbers(command.getRecipients(), ",", false /*oldFormat*/));
 			addParam(d, PARAM_TEXT, text);
 		} else {
 			addParam(d, PARAM_BALANCE_ONLY, "1");
 		}
 
 		if (captchaAnswer != null) {
 			addParam(d, PARAM_CAPTCHA_ANSWER, captchaAnswer);
 		}
 
 		o.addFormParameter(d);
 
 		// send web request to the server and read the response
 		HttpResponse response = Utils.getHttpClient(o);
 
 		// process the response
 		checkResponseCode(context, response);
 
 		String responseText = Utils.stream2str(response.getEntity().getContent()).trim();
 		Log.d(TAG, "HTTP RESPONSE: " + responseText);
 
 		if (provider.equals(AccountPreferences.PROVIDER_VODAFONE) && responseText.contains("JSESSIONID")) {
 			processVodafoneCookies(prefs, accId, responseText);
 			res = SENT_NEED_CAPTCHA;
 		} else {
 			processRegularResponse(context, command, cs, responseText);
 		}
 		return res;
 	}
 
 	/**
 	 * Populates common request options.
 	 */
 	private void populateCommonOptions(final HttpOptions o, final ArrayList<BasicNameValuePair> d,
 			final ConnectorSpec cs, final String accId, final String provider, final SharedPreferences prefs) {
 		o.timeout = CONN_TIMEOUT_MS;
 		o.maxConnections = cs.getSubConnectorCount() + 1;
 		
 		addParam(d, PARAM_PROVIDER, provider);
 		addParam(d, PARAM_USERNAME, AccountPreferences.getUsername(prefs, accId));
 		addParam(d, PARAM_PASSWORD, AccountPreferences.getPassword(prefs, accId));
 		
 		String cookies = AccountPreferences.getCookies(prefs, accId);
 		if (!TextUtils.isEmpty(cookies)) {
 			for (String cookie : cookies.split("&")) {
 				String[] cookieParts = cookie.split("=");
 				addParam(d, cookieParts[0], URLDecoder.decode(cookieParts[1]));
 			}
 		}
 	}
 
 	/**
 	 * Parses HTTP response code. Throws {@link WebSMSException} if response != HTTP_OK.
 	 */
 	private void checkResponseCode(final Context context, final HttpResponse response) {
 		
 		final int resp = response.getStatusLine().getStatusCode();
 		if (resp != HttpURLConnection.HTTP_OK) {
 			// log error response
 			Log.e(TAG, "HTTP Status Line: " + response.getStatusLine().toString());
 			Log.e(TAG, "HTTP Headers:");
 			for (Header h : response.getAllHeaders()) {
 				Log.e(TAG, h.getName() + ": " + h.getValue());
 			}
 			try {
 				final String htmlText = Utils.stream2str(response.getEntity().getContent()).trim();
 				Log.e(TAG, "HTTP Body:");
 				for (String l : htmlText.split("\n")) {
 					Log.e(TAG, l);
 				}
 			} catch (Exception e) {
 				Log.w(TAG, "error getting content", e);
 			}
 
 			throw new WebSMSException(context, R.string.error_http, String.valueOf(resp));
 		}
 	}
 
 	/**
 	 * Parses response from Cabbage server.
 	 * Should be a number: remaining balance if positive or error code if negative.
 	 * NOTE that some free php hosting sites add a trailer to all pages, so take the first number from the response.
 	 */
 	private void processRegularResponse(final Context context, 
 			final ConnectorCommand command, final ConnectorSpec cs, final String responseText) {
 
 		final Matcher m = FIRST_NUMBER.matcher(responseText);
 		if (!m.find()) {
 			throw new WebSMSException(context.getString(R.string.cabbage_err_unexpected));
 		}
 		final String retCode = m.group();
 		int retNumCode = Integer.parseInt(retCode);
 		
 		if (retNumCode >= 0) {
 			synchronized (SYNC_UPDATE) {
 				cs.getSubConnector(command.getSelectedSubConnector()).setBalance(retCode);
 			}
 		} else {
 			throw new WebSMSException(getErrorMessage(context, retNumCode));
 		}
 	}
 
 	/**
 	 * Parses Vodafone cookies returned from Cabbage server in the form:
 	 *   JSESSIONID=m7m0Q4QC0qTfyyQQ42YL8S<br/>supercookie=6d376d3071347163201e<br/>
 	 * and store ("&" separated) in Preferences.
 	 * NOTE that some free php hosting sites add a trailer to all pages, so need to ignore it.
 	 */
 	private void processVodafoneCookies(final SharedPreferences prefs, final String accId, final String responseText) {
 		String jsessionid = "";
 		int startIdx = responseText.indexOf("JSESSIONID=");
 		if (startIdx >= 0) {
 			int endIdx = responseText.indexOf("<br/>", startIdx+11);
 			if (endIdx >= 0) {
 				jsessionid = responseText.substring(startIdx+11, endIdx);
 			}
 		}
 
 		String supercookie = "";
 		startIdx = responseText.indexOf("supercookie=");
 		if (startIdx >= 0) {
 			int endIdx = responseText.indexOf("<br/>", startIdx+12);
 			if (endIdx >= 0) {
 				supercookie = responseText.substring(startIdx+12, endIdx);
 			}
 		}
 		
 		AccountPreferences.setCookies(prefs, accId, 
 				"JSESSIONID=" + URLEncoder.encode(jsessionid) + "&supercookie=" + URLEncoder.encode(supercookie));
 	}
 
 	/**
 	 * Retrieves captcha image from Cabbage server.
 	 */
 	private Bitmap retrieveCaptcha(final Context context, final ConnectorCommand command)
 			throws IOException {
 		Log.d(TAG, "retrieving captch image");
 		final ConnectorSpec cs = this.getSpec(context);
 		final String accId = command.getSelectedSubConnector();
 
 		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		final String provider = AccountPreferences.getProvider(prefs, accId);
 
 		// prepare web request
 		final HttpOptions o = new HttpOptions(ENCODING);
 		final ArrayList<BasicNameValuePair> d = new ArrayList<BasicNameValuePair>();
 		populateCommonOptions(o, d, cs, accId, provider, prefs);
 
 		o.url = CabbageConnectorPreferences.getCabbageUrl(prefs, provider).replace("/send.php", "/voda.send.php");
 		addParam(d, "print", "cap");
 
 		o.addFormParameter(d);
 
 		// send web request to the server and get the response
 		final HttpResponse response = Utils.getHttpClient(o);
 
 		// process the response
 		checkResponseCode(context, response);
 
 		final HttpEntity entity = response.getEntity();
         if (entity == null) {
         	throw new WebSMSException(context, R.string.error_retrieve_captcha);
         }
         InputStream inputStream = null;
         try {
             inputStream = entity.getContent();
             return BitmapFactory.decodeStream(inputStream);
         } finally {
             if (inputStream != null) {
                 inputStream.close();
             }
             entity.consumeContent();
         }
 	}
 
 	/**
 	 * Asks user to solve the captcha.
 	 */
 	private String solveCaptchaWithUser(final Context context, final Bitmap captcha, final boolean wasSolverUsed) {
 		Log.d(TAG, "requesting captcha answer from user");
 		CabbageConnector.receivedCaptchaAnswer = null;
 
 		// send request to WebSMS
 		final Intent intent = new Intent(Connector.ACTION_CAPTCHA_REQUEST);
 	    getSpec(context).setToIntent(intent);
 	    intent.putExtra(Connector.EXTRA_CAPTCHA_DRAWABLE, captcha);
 	    if (CaptcherSolverClient.shouldRemind(context)) {
 		    intent.putExtra(Connector.EXTRA_CAPTCHA_MESSAGE, context.getString(R.string.websms_captcha_text_with_tip));
 	    } else if (wasSolverUsed) {
 		    intent.putExtra(Connector.EXTRA_CAPTCHA_MESSAGE, context.getString(R.string.websms_captcha_text_auto_failed));
 	    } else {
 		    intent.putExtra(Connector.EXTRA_CAPTCHA_MESSAGE, context.getString(R.string.websms_captcha_text));
 	    }
 	    context.sendBroadcast(intent);
 
 	    // wait for answer
 	    try {
 	        synchronized (CAPTCHA_SYNC) {
 	            CAPTCHA_SYNC.wait(CAPTCHA_ANSWER_TIMEOUT);
 	        }
 	    } catch (InterruptedException e) {
 	    }
 
 	    return CabbageConnector.receivedCaptchaAnswer;
 	}
 
 	/**
 	 * Asks the captcha solver app to solve the captcha.
 	 */
 	private String solveCaptchaWithSolver(final Context context, final Bitmap captcha) {
 		Log.d(TAG, "requesting captcha answer from Captcha Solver");
 		CabbageConnector.receivedCaptchaAnswer = null;
 
 		// send request to captcha solver app
 		final Intent intent = CaptcherSolverClient.createRequestIntent(captcha);
 	    context.sendBroadcast(intent);
 
 	    // wait for answer
 	    try {
 	        synchronized (CAPTCHA_SYNC) {
 	            CAPTCHA_SYNC.wait(CAPTCHA_ANSWER_TIMEOUT);
 	        }
 	    } catch (InterruptedException e) {
 	    }
 	    return CabbageConnector.receivedCaptchaAnswer;
 	}
 
 	/**
 	 * Processes an answer from the captcha solver app. 
 	 */
 	private void processCaptchaSolverAnswer(final Context context, final Intent intent) {
 		final ConnectorSpec specs = this.getSpec(context);
 		final String tag = specs.toString();
 		Log.d(tag, "got solved captcha");
 
 		String answer = CaptcherSolverClient.parseResponseIntent(context, intent);
 
 		gotSolvedCaptcha(context, answer);
 
 		try {
 			this.setResultCode(Activity.RESULT_OK);
 		} catch (Exception e) {
 			Log.w(tag, "not an ordered boradcast: " + e.toString());
 		}
 	}
 
 	/**
 	 * Called if any broadcast with a solved captcha arrived.
 	 */
 	@Override
     protected void gotSolvedCaptcha(final Context context, final String solvedCaptcha) {
 		CabbageConnector.receivedCaptchaAnswer = solvedCaptcha;
         synchronized (CAPTCHA_SYNC) {
             CAPTCHA_SYNC.notify();
         }
     }
 
 
 	private ArrayList<BasicNameValuePair> addParam(final ArrayList<BasicNameValuePair> d, final String n, final String v) {
 		if (!TextUtils.isEmpty(n)) {
 			d.add(new BasicNameValuePair(n, v));
 		}
 		return d;
 	}
 
 	private String getErrorMessage(final Context context, final int retNumCode) {
 		final String msgIdStr = ERR_MESSAGE_PREFIX + Integer.toString(-retNumCode);
 		final int msgId = context.getResources().getIdentifier(msgIdStr, "string", context.getPackageName());
 		if (msgId > 0) {
 			return context.getString(msgId);
 		} else {
 			String msgTemplate = context.getString(R.string.cabbage_err_N);
 			return MessageFormat.format(msgTemplate, retNumCode);
 		}
 	}
 	
 }
