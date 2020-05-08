 /*
  * Copyright (C) 2010 Nathaniel Baxter
  * This program is based off and uses WebSMS. Copyright (C) 2010 Felix Bechstein
  * 
  * This file is part of WebSMS Connector: Exetel.
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
 package com.baxtern.android.websms.connector.exetel;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import de.ub0r.android.websms.connector.common.Connector;
 import de.ub0r.android.websms.connector.common.ConnectorCommand;
 import de.ub0r.android.websms.connector.common.ConnectorSpec;
 import de.ub0r.android.websms.connector.common.Log;
 import de.ub0r.android.websms.connector.common.Utils;
 import de.ub0r.android.websms.connector.common.WebSMSException;
 import de.ub0r.android.websms.connector.common.ConnectorSpec.SubConnectorSpec;
 
 // TODO: Check all code, test and bugfix.
 
 /**
  * Receives commands coming as broadcasts from WebSMS.
  * 
  * @author Nathaniel Baxter
  */
 public class ConnectorExetel extends Connector {
 	/** Tag for output. */
 	private static final String TAG = "WebSMS.exetel";
 
 	/** API url */
 	private static final String URL = "https://smsgw.exetel.com.au/sendsms/";
 	/** API url for sending an sms */
 	private static final String URL_SEND = URL + "api_sms.php";
 	/** API url for sending a scheduled SMS */
 	// private static final String URL_SEND_SCHEDULED = URL +
 	// "api_sms_schedule.php";
 	/**
 	 * Use POST seeing as this is the HTTPS protocal. We might as well send the
 	 * username/password/message using encryption.
 	 */
 	private static final boolean USE_POST = true;
 
 	/** Username parameter key. */
 	private static final String PARAM_USERNAME = "username";
 	/** Password parameter key. */
 	private static final String PARAM_PASSWORD = "password";
 	/** Sender parameter key. */
 	private static final String PARAM_SENDER = "sender";
 	/** To parameter key. */
 	private static final String PARAM_TO = "mobilenumber";
 	/** Text parameter key. */
 	private static final String PARAM_TEXT = "message";
 	/** Message type parameter key. */
 	private static final String PARAM_TYPE = "messagetype";
 
 	/** The delimer for the API's response text. */
	private static final String HTTP_RESPONSE_TEXT_DELIMITER = "\\|";
 	/** The recipients delimer for the url query. */
 	private static final String RECIPIENTS_DELIMTER = ",";
 	/** The encoding to use for the request. */
 	private static final String HTTP_REQUEST_ENCODING = "Unicode";
 
 	/** The parameters returned by the API. */
 	private static final int RETURN_PARAM_STATUS = 0;
 	// private static final int RETURN_PARAM_MOBILENUMBER = 1;
 	// private static final int RETURN_PARAM_REFERENCENUMBER = 2;
 	// private static final int RETURN_PARAM_ID = 3;
 	private static final int RETURN_PARAM_NOTES = 4;
 
 	/** The status response codes given by the API. */
 	// private static final int STATUS_SMS_REQUEST_FAILED = 0;
 	// private static final int STATUS_SMS_FAILED = 2;
 	private static final int STATUS_SMS_SENT = 1;
 
 	/** Max. length of the message. */
 	private static final int MAX_MESSAGE_LENGTH = 612;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final ConnectorSpec initSpec(final Context context) {
 		final String name = context.getString(R.string.connector_exetel_name);
 		final ConnectorSpec c = new ConnectorSpec(name);
 
 		c.setAuthor(context.getString(R.string.connector_exetel_author));
 		c.setBalance(null);
 		c.setLimitLength(MAX_MESSAGE_LENGTH);
 		c.setCapabilities(ConnectorSpec.CAPABILITIES_UPDATE
 				| ConnectorSpec.CAPABILITIES_SEND
 				| ConnectorSpec.CAPABILITIES_PREFS);
 		// The Connector supports multiple receipients and custom senders.
 		c.addSubConnector(TAG, name, SubConnectorSpec.FEATURE_MULTIRECIPIENTS
 				| SubConnectorSpec.FEATURE_CUSTOMSENDER);
 		// TODO: Implement the sendlater feature.
 		// | SubConnectorSpec.FEATURE_SENDLATER);
 		return c;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final ConnectorSpec updateSpec(final Context context,
 			final ConnectorSpec connectorSpec) {
 		final SharedPreferences p = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		if (p.getBoolean(Preferences.PREFS_ENABLED, false)) {
 			if (p.getString(Preferences.PREFS_USER, "").length() > 0
 					&& p.getString(Preferences.PREFS_PASSWORD, "").length() > 0) {
 				connectorSpec.setReady();
 			} else {
 				connectorSpec.setStatus(ConnectorSpec.STATUS_ENABLED);
 			}
 		} else {
 			connectorSpec.setStatus(ConnectorSpec.STATUS_INACTIVE);
 		}
 		return connectorSpec;
 	}
 
 	/**
 	 * Send some data! (ie. the SMS)
 	 * 
 	 * @param context
 	 *            Context
 	 * @param command
 	 *            ConnectorCommand
 	 * @throws WebSMSException
 	 *             WebSMSException
 	 */
 	private void sendData(final Context context, final ConnectorCommand command)
 			throws WebSMSException {
 		try {
 			// Get the preferences.
 			final SharedPreferences prefs = PreferenceManager
 					.getDefaultSharedPreferences(context);
 
 			// The request url. Used for single or multiple receipients.
 			String url = URL_SEND;
 
 			// Create an array of GET data.
 			ArrayList<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
 
 			// Username.
 			data.add(new BasicNameValuePair(PARAM_USERNAME, prefs.getString(
 					Preferences.PREFS_USER, "")));
 			// Password.
 			data.add(new BasicNameValuePair(PARAM_PASSWORD, prefs.getString(
 					Preferences.PREFS_PASSWORD, "")));
 			// Destination/Recipients.
 			// This code could validate the recipients so that they are numbers
 			// of length maximum 15.
 			data.add(new BasicNameValuePair(PARAM_TO, Utils
 					.joinRecipientsNumbers(command.getRecipients(),
 							RECIPIENTS_DELIMTER, true)));
 			// Sender.
 			final String customSender = command.getCustomSender();
 			// Default sender.
 			if (customSender == null) {
 				data.add(new BasicNameValuePair(PARAM_SENDER, Utils
 						.national2international(command.getDefPrefix(), Utils
 								.getSender(context, command.getDefSender()))));
 			}
 			// A custom sender.
 			else {
 				data.add(new BasicNameValuePair(PARAM_SENDER, customSender));
 			}
 			// Message.
 			data.add(new BasicNameValuePair(PARAM_TEXT, command.getText()));
 			// Message type.
 			data.add(new BasicNameValuePair(PARAM_TYPE, HTTP_REQUEST_ENCODING));
 
 			// Only if we have to make a GET request.
 			if (!USE_POST) {
 				// Create a string of the data for the url.
 				StringBuilder data_string = new StringBuilder();
 				data_string.append("?");
 
 				for (int i = 0; i < data.size(); i++) {
 					BasicNameValuePair nv = data.get(i);
 					data_string.append(nv.getName());
 					data_string.append("=");
 					data_string.append(URLEncoder.encode(nv.getValue(),
 							HTTP_REQUEST_ENCODING));
 					data_string.append("&");
 				}
 
 				// Add the data to the end of the url.
 				url.concat(data_string.toString());
 			}
 
 			// Log that we're making a Http request.
 			Log.d(TAG, "HTTP REQUEST: " + url);
 
 			// Do the Http request.
 			HttpResponse response = Utils.getHttpClient(url, null, data, null,
 					null, true);
 
 			// Get the http response text and status code.
 			int response_code = response.getStatusLine().getStatusCode();
 			String response_text = Utils.stream2str(
 					response.getEntity().getContent()).trim();
 			// Log the http response.
 			Log.d(TAG, "HTTP RESPONSE (" + Integer.toString(response_code)
 					+ "): " + response_text);
 
 			// Time to inspect the results of all our hard work!
 			String[] response_data = response_text
 					.split(HTTP_RESPONSE_TEXT_DELIMITER);
 
 			// Deal with the response data provided by the api.
 			try {
 				// Check that the status is okay.
 				int status = Integer
 						.parseInt(response_data[RETURN_PARAM_STATUS]);
 				if (status != STATUS_SMS_SENT) {
 					// Give the user the api's error message.
 					throw new WebSMSException(response_data[RETURN_PARAM_NOTES]);
 				}
 			} catch (ArrayIndexOutOfBoundsException e) {
 				throw new WebSMSException(context,
 						R.string.error_exetel_invalid_return);
 			}
 			// Catch any annoying IO errors.
 		} catch (IOException e) {
 			Log.e(TAG, null, e);
 			throw new WebSMSException(e.getMessage());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected final void doUpdate(final Context context, final Intent intent)
 			throws WebSMSException {
 		this.sendData(context, new ConnectorCommand(intent));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected final void doSend(final Context context, final Intent intent)
 			throws WebSMSException {
 		this.sendData(context, new ConnectorCommand(intent));
 	}
 }
