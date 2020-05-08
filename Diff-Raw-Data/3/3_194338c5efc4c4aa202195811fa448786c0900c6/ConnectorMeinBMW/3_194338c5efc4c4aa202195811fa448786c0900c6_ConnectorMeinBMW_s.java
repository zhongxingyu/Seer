 /*
  * Copyright (C) 2010 Silas Graffy
  *
  * This file is part of MeinBMW Connector for WebSMS by Felix Bechstein.
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
 
 package info.graffy.android.websms.connector.meinbmw;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import de.ub0r.android.websms.connector.common.Connector;
 import de.ub0r.android.websms.connector.common.ConnectorCommand;
 import de.ub0r.android.websms.connector.common.ConnectorSpec;
 import de.ub0r.android.websms.connector.common.ConnectorSpec.SubConnectorSpec;
 import de.ub0r.android.websms.connector.common.Utils;
 import de.ub0r.android.websms.connector.common.WebSMSException;
 
 public class ConnectorMeinBMW extends Connector {
 
 	private final static String ROOT_PAGE_URL  = "https://www.meinbmw.de";
 	private final static String SMS_PAGE_URL = "https://www.meinbmw.de/tabid/80/Default.aspx";
 	private final static String REFERER_URL = "https://www.meinbmw.de/Home/tabid/36/ctl/Login/Default.aspx";
 	private final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.52 Safari/536.5";
 	private final static String ENCODING = "UTF-8";
	private final static String[] SSL_FINGERPRINTS = { "b4:2b:76:39:ec:53:ee:83:39:02:a3:70:c1:fc:a9:a5:f7:24:d5:2a", "0f:9e:67:16:e7:e0:98:02:22:49:d6:a0:74:9b:76:11:7e:c6:e1:e9" };
 	private final static String SESSION_INPUT_VALUE_REGEXP = ".*<input type=\"hidden\" name=\"__VIEWSTATE_CACHEKEY\" id=\"__VIEWSTATE_CACHEKEY\" value=\"(VS_[a-z\\d]+_\\d+)\" />.*";
 	private final static String LOGIN_POST_DESTINATION_REGEXP = ".*<form name=\"Form\" method=\"post\" action=\"(/ServiceCenter/.+/SMSService/tabid/80/ctl/Login/Default.aspx\\?returnurl=.+\\.aspx)\".*id=\"Form\" enctype=\"multipart/form-data\" .*";
 	private final static String SEND_SUCCESS_SUBSTRING = "verschickt.";
 	private final static String SMS_PAGE_LOAD_SUCCESS_SUBSTRING = "MeinBMW.de - Service-Center - Mein BMW Services - SMS Service";
 	private final static String WRONG_LOGIN_MESSAGE_SUBSTRING = "Ihre E-Mail-Adresse oder Ihr Passwort ist leider nicht korrekt. Bitte überprüfen Sie Ihre Eingaben."; // TODO changed
 	private final static String TODAYS_SMS_EXPIRED_SUBSTRING = "heute ist erreicht. Bitte nutzen Sie diesen Service wieder ab morgen";
 
 	private static String currentHtmlResultPage;
 	private static Context currentContext;
 	private final static Pattern sessionInputValueExtractPattern = Pattern.compile(SESSION_INPUT_VALUE_REGEXP, Pattern.DOTALL);
 	private final static Pattern postUrlExtractPattern = Pattern.compile(LOGIN_POST_DESTINATION_REGEXP, Pattern.DOTALL);
 
 	@Override
 	public final ConnectorSpec initSpec(final Context context) {
 		setCurrentContext(context);
 		return createConnectorSpec();
 	}
 
 	@Override
 	public final ConnectorSpec updateSpec(final Context context, final ConnectorSpec connectorSpec) {
 		setCurrentContext(context);
 		return setConnectorStatus(connectorSpec);
 	}
 
 	@Override
 	protected final void doSend(final Context context, final Intent intent) {
 		setCurrentContext(context);
 
 		tryToLoadSmsPage();
 
 		if (!isSmsPageLoaded()) {
 			final String user = getUserName();
 			final String pass = getPassword();
 			doLogin(user, pass);
 		}
 
 		final String phonenumber = getPhonenumber(intent);
 		final String text = getMessageText(intent);
 
 		sendSms(phonenumber, text);
 	}
 
 	private final void setCurrentContext(final Context context) {
 		currentContext = context;
 	}
 
 	private final ConnectorSpec setConnectorStatus(final ConnectorSpec connectorSpec) {
 		final SharedPreferences p = getDefaultSharedPreferences();
 		if (p.getBoolean(Preferences.PREFS_ENABLED, false)) 
 			if (getUserName().length() > 0 && getPassword().length() > 0)
 				connectorSpec.setReady();
 			else
 				connectorSpec.setStatus(ConnectorSpec.STATUS_ENABLED);
 		 else 
 			connectorSpec.setStatus(ConnectorSpec.STATUS_INACTIVE);
 		return connectorSpec;
 	}
 
 	private final SharedPreferences getDefaultSharedPreferences() {
 		return PreferenceManager.getDefaultSharedPreferences(currentContext);
 	}
 
 	private final String getUserName() {
 		final SharedPreferences p = getDefaultSharedPreferences();
 		return p.getString(Preferences.PREFS_USER, "");
 	}
 
 	private final String getPassword() {
 		final SharedPreferences p = getDefaultSharedPreferences();
 		return p.getString(Preferences.PREFS_PASSWORD, "");
 	}
 
 	private final ConnectorSpec createConnectorSpec() {
 		final String name = getStringResource(R.string.connector_meinbmw_name);
 		final ConnectorSpec c = new ConnectorSpec(name);
 		c.setAuthor(getStringResource(R.string.connector_meinbmw_author));
 		c.setBalance(null);
 		c.setCapabilities(ConnectorSpec.CAPABILITIES_SEND | ConnectorSpec.CAPABILITIES_PREFS);
 		c.addSubConnector("0", "", SubConnectorSpec.FEATURE_NONE);
 		return c;
 	}
 
 	private final String getStringResource(final int resourceStringID) {
 		return currentContext.getString(resourceStringID);
 	}
 
 	private final String getPhonenumber(final Intent intent) {
 		final ConnectorCommand cc = new ConnectorCommand(intent);
 		return Utils.getRecipientsNumber(cc.getRecipients()[0]);
 	}
 
 	private final String getMessageText(final Intent intent) {
 		final ConnectorCommand cc = new ConnectorCommand(intent);
 		return cc.getText();
 	}
 
 	private final void tryToLoadSmsPage() {
 		StatusLine httpResult;
 		httpResult = performHttpRequestForStatusLine(SMS_PAGE_URL);
 
 		if (httpResult.getStatusCode() != HttpStatus.SC_OK)
 			throw new WebSMSException(getStringResource(R.string.error_sms_page_result) + httpResult.toString());
 	}
 
 	private final boolean isSmsPageLoaded() {
         return currentHtmlResultPage.indexOf(SMS_PAGE_LOAD_SUCCESS_SUBSTRING) > -1;
 	}
 
 	private final void doLogin(final String user, final String password) {
         StatusLine httpResult;
 
         final String sessionInputValueLogin = extractSessionInputValue();
         final String postDestination = ROOT_PAGE_URL + extractRelativePostUrl();
         httpResult = performRawLogin(user, password, postDestination, sessionInputValueLogin);
 
         if (httpResult.getStatusCode() != HttpStatus.SC_OK)
 			throw new WebSMSException(getStringResource(R.string.error_login_form_result) + httpResult.toString());
 
 		if (wasLoginOrPasswordWrong())
 			throw new WebSMSException(getStringResource(R.string.error_mail));
 
 		if (!isSmsPageLoaded())
 			throw new WebSMSException(getStringResource(R.string.error_login_sms_unknown));
 	}
 
 	private final void sendSms(String phonenumber, final String message) {
 		phonenumber = Utils.international2oldformat(phonenumber);
 
 		if (message.length() > 160)
 			throw new WebSMSException(getStringResource(R.string.error_length));
 
 		if (areSmsExiredForToday())
 			throw new WebSMSException(getStringResource(R.string.error_sms_expired));
 
 		final String sessionInputValueSms = extractSessionInputValue();
 		StatusLine httpResult;
 		httpResult = performRawSendSms(phonenumber, message, sessionInputValueSms);
 
 		if (httpResult.getStatusCode() != HttpStatus.SC_OK)
 			throw new WebSMSException(getStringResource(R.string.error_sms_form_result) + httpResult.toString());
 
 		if (sendingFailed())
 			throw new WebSMSException(getStringResource(R.string.error_sending_sms_unknown));
 	}
 
 	private final StatusLine performRawLogin(final String user, final String password, final String postDestinationUrl, final String sessionInputValueLogin) {
 		final ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
 		nameValuePairs.add(new BasicNameValuePair("dnn$ctr$Login$Login_DNN$txtUsername", user));
 		nameValuePairs.add(new BasicNameValuePair("dnn$ctr$Login$Login_DNN$txtPassword", password));
 		nameValuePairs.add(new BasicNameValuePair("dnn$ctr$Login$Login_DNN$cmdLogin", "Login"));
 		nameValuePairs.add(new BasicNameValuePair("__EVENTTARGET", ""));
 		nameValuePairs.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
 		nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE_CACHEKEY", sessionInputValueLogin));
 		nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", ""));
 		
 		return performHttpRequestForStatusLine(postDestinationUrl, nameValuePairs);
 	}
 
 	private final StatusLine performRawSendSms(final String phonenumber, final String message, final String sessionInputValueSms) {
 		final ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
 		nameValuePairs.add(new BasicNameValuePair("dnn$ctr493$SfiSms_View$phone", phonenumber));
 		nameValuePairs.add(new BasicNameValuePair("dnn$ctr493$SfiSms_View$subject", message));
 		nameValuePairs.add(new BasicNameValuePair("dnn$ctr493$SfiSms_View$sendData", "Senden"));
 		nameValuePairs.add(new BasicNameValuePair("__EVENTTARGET", ""));
 		nameValuePairs.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
 		nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE_CACHEKEY", sessionInputValueSms));
 		nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", ""));
 
 		return performHttpRequestForStatusLine(SMS_PAGE_URL, nameValuePairs);
 	}
 
 
 	private final StatusLine performHttpRequestForStatusLine(final String url) {
 		return performHttpRequestForStatusLine(url, null);	
 	}
 	
 	private final StatusLine performHttpRequestForStatusLine(final String url, final ArrayList<BasicNameValuePair> postData) {
 		try {
 			final HttpResponse response = performHttpRequestForStatusLineForResponse(url, postData);			
 			final HttpEntity entity = response.getEntity();
 			if (entity != null) {
 				currentHtmlResultPage = EntityUtils.toString(entity, ENCODING);
 				entity.consumeContent();
 			}
 			return response.getStatusLine();
 		}
 		catch (IOException e) {
 			throw new WebSMSException(e.toString());
 		}
 	}
 
 	private final HttpResponse performHttpRequestForStatusLineForResponse(final String url, final ArrayList<BasicNameValuePair> postData) throws IOException {
 		try {
 			return performHttpRequestForStatusLineUtils(url, postData);
 		}
 		catch (IOException e) {
 			//HACK: This fails regularly with "SSL shutdown failed: I/O error during system call, Broken pipe",
 			//      see https://issues.apache.org/jira/browse/HTTPCLIENT-951?focusedCommentId=12901563&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#action_12901563
 			//      But in this case the second try usually works ...
 			return performHttpRequestForStatusLineUtils(url, postData);
 		}
 	}
 
 	private final HttpResponse performHttpRequestForStatusLineUtils(final String url, final ArrayList<BasicNameValuePair> postData) throws IOException {
         Utils.HttpOptions o = new Utils.HttpOptions(ENCODING);
         o.url = url;
         o.userAgent = USER_AGENT;
         o.referer = REFERER_URL;
         o.knownFingerprints = SSL_FINGERPRINTS;
         o.addFormParameter(postData);
         return Utils.getHttpClient(o);
 	}
 
 	private final String extractRelativePostUrl() {
 		final Matcher matcher = postUrlExtractPattern.matcher(currentHtmlResultPage);
 		if(matcher.matches())
 			return matcher.group(1);
 		throw new WebSMSException(getStringResource(R.string.error_no_post_url_found));
 	}
 
 	private final String extractSessionInputValue() {
 		final Matcher matcher = sessionInputValueExtractPattern.matcher(currentHtmlResultPage);
 		if(matcher.matches())
 			return matcher.group(1);
 		throw new WebSMSException(getStringResource(R.string.error_no_session_input_value_found));
 	}
 
 	private final boolean wasLoginOrPasswordWrong() {
 		return currentHtmlResultPage.indexOf(WRONG_LOGIN_MESSAGE_SUBSTRING) > -1;
 	}
 
 	private final boolean areSmsExiredForToday() {
 		return currentHtmlResultPage.indexOf(TODAYS_SMS_EXPIRED_SUBSTRING) > -1;
 	}
 
 	private final boolean sendingFailed()	{
 		return currentHtmlResultPage.indexOf(SEND_SUCCESS_SUBSTRING) == -1;
 	}
 }
