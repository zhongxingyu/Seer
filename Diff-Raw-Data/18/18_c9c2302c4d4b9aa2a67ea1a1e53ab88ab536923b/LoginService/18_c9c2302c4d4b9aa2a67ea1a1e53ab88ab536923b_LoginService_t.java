 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package riddimon.android.asianetautologin;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import riddimon.android.asianetautologin.HttpUtils.HttpMethod;
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.IBinder;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.NotificationCompat.Builder;
 import android.support.v4.content.LocalBroadcastManager;
 import android.text.TextUtils;
 import android.text.format.DateUtils;
 
 public class LoginService extends Service {
 	private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
 	private static final int REQUEST_CODE = 100;
 	private static final long REPEAT_FREQ = 5 * DateUtils.MINUTE_IN_MILLIS;
 
 	public static final String ACTION_LOGIN = "action_login";
 	public static final String ACTION_KEEP_ALIVE = "action_keep_alive";
 	public static final String ACTION_LOGOUT = "action_logout";
 
 	public static final String EX_STATUS = "status";
 	public static final String EX_LOGGED_IN_ELSEWHERE = "elsewhere";
 	public static final int STATUS_OK = 1;
 	public static final int STATUS_FAIL = 2;
 
 	private static final String REFERENCE_SITE = "http://www.google.co.in";
 
 	// logout
 	private static final String FIELD_LOGOUT_ID = "logout_id";
 	private static final String FIELD_LOGOUT = "logout";
 	private static final String FIELD_LOGOUT_VALUE = "Logout";
 
 	// keep alive
 	private static final String FIELD_ALIVE = "alive";
 	private static final String FIELD_ALIVE_VALUE = "y";
 	private static final String FIELD_ALIVE_UN = "un";
 	
 	// login
 	private static final String FIELD_AUTH_USER = "auth_user";
 	private static final String FIELD_AUTH_PASS = "auth_pass";
 	private static final String FIELD_AUTH_ACCEPT = "accept";
 	private static final String FIELD_AUTH_ACCEPT_VALUE = "Login >>";
 	private static final String FIELD_AUTH_REDIR_URL = "redirurl";
 	private static final String FIELD_AUTH_REDIR_URL_VAL = "$PORTAL_REDIRURL$";
 
 	private static final String URL_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
 	private static final SimpleDateFormat sdf = new SimpleDateFormat("kk:mm dd/MM/yyyy");
 
 	public class LoginBinder extends Binder {
 		LoginService getService() {
 			return LoginService.this;
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return new LoginBinder();
 	}
 
 	/**
 	 * Ping reference site to check if the site is accessible
 	 * <p><b>Warning :</b> This method runs on the called thread.</p>
 	 * @return
 	 */
 	public boolean isLoggedIn() {
 		boolean loggedIn = false;
 		HttpResponse response = HttpUtils.execute(this, HttpMethod.GET, REFERENCE_SITE, null);
 		if (response != null) {
 			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 				loggedIn = true;
 			}
 		}
 		logger.info("Redirected : {}", !loggedIn);
 		if (!loggedIn && response != null) {
 			Header[] headers = response.getHeaders("Location");
 			if (headers != null && headers.length > 0) {
 				logger.info("Redirected to : {}", headers[0].getValue());
 			}
 		}
 		return loggedIn;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		super.onStartCommand(intent, flags, startId);
 		if (intent != null) {
 			String action = intent.getAction();
 			Thread thread = new Thread(new ISPTask(this, action), "isp_worker");
 			thread.start();
 		}
 		return Service.START_STICKY;
 	}
 
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		// cancel the notification so the user knows that the login process
 		// is not active
 		NotificationManager nm = (NotificationManager) getSystemService(
 				NOTIFICATION_SERVICE);
 		nm.cancel(1000);
 
 		logger.info("LoginService is destroyed");
 	}
 
 	private class ISPTask implements Runnable {
 		Context mContext;
 		String mAction;
 
 		public ISPTask(Context context, String action) {
 			this.mContext = context;
 			this.mAction = action;
 		}
 
 		@Override
 		public void run() {
 			int attempt = 0;
 			while (attempt++ < 3) {
 				if (work()) {
 					try {
 						Thread.sleep((attempt + 1) * 3 * DateUtils.SECOND_IN_MILLIS);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 						break;
 					}
 				}
 			}
 			if (attempt == 3) {
 				// schedule attempt after ten seconds
 				setAlarm(10 * DateUtils.SECOND_IN_MILLIS, false);
 			}
 		}
 
 		private boolean work() {
 			// don't proceed if we're not connected to the proper network
 			ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context
 					.CONNECTIVITY_SERVICE);
 			boolean status = true;
 			boolean retry = false;
 			boolean loggedInElseWhere = false;
 			boolean connected = NetworkUtil.isConnectedToProperNetwork(mContext);
 			logger.info("Connected to proper network : {}", connected);
 
 			// set preferred network to WiFi so that, in the case of login
 			// google is contacted through the WiFi network rather than the 
 			// 3G network
 
 			int cmp = cm.getNetworkPreference();
 			cm.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
 			status &= connected;
 
 			if (connected) {
 				String username = SettingsManager.getString(mContext
 						, SettingsManager.USERNAME, null);
 				String password = SettingsManager.getString(mContext
 						, SettingsManager.PASSWORD, null);
 				String url = SettingsManager.getString(mContext
 						, SettingsManager.URL, null);
 
 				logger.info("Operation : {}", mAction);
 				boolean renewOrLogin = shouldRenewOrLogin();
 				if (mAction.equals(LoginService.ACTION_KEEP_ALIVE) && renewOrLogin) {
 					// if we don't know the url, login will find it
 					status &= TextUtils.isEmpty(url) ? login(url, username
 							, password)	: keepAlive(url, username);
 					retry = !status;
 				} else if (mAction.equals(LoginService.ACTION_LOGIN) && renewOrLogin) {
 					boolean loggedIn = isLoggedIn();
 					if (loggedIn && !TextUtils.isEmpty(url)) {
 						status &= keepAlive(url, username);
 						if (status) {
 							setAlarm(REPEAT_FREQ, true);
 						}
 						retry = !status;
 					} else if (!loggedIn) {
 						status &= login(url, username, password);
 						retry = !status;
 					} else {
 						status = false;
 						loggedInElseWhere = true;
 					}
 				} else if (mAction.equals(LoginService.ACTION_LOGOUT) && !TextUtils.isEmpty(url)) {
 					status &= logout(url, username);
 					// no need to continue running the service
 					// send stop signal
 					stopSelf();
 				} else {
 					status = false;
 				}
 			}
 
 			Intent intent = new Intent(mAction);
 			intent.putExtra(EX_STATUS, status);
 			intent.putExtra(EX_LOGGED_IN_ELSEWHERE, loggedInElseWhere);
 			LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
 			cm.setNetworkPreference(cmp);
 
 			return retry;
 		}
 
 		private String getUrlFromPage(String url) {
 			HttpResponse response = HttpUtils
 					.execute(mContext, HttpMethod.GET, url, null);
 			String furl = null;
 			if (response != null) {
 				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 					try {
 						String page = HttpUtils.readStreamIntoString(response
 								.getEntity().getContent());
 						Document doc = Jsoup.parse(page, url);
 						Elements elements = doc.getElementsByTag("form");
 						for (Element el : elements) {
 							furl = el.attr("action");
 							if (furl.matches(URL_REGEX)) {
 								break;
 							} else furl = null;
 							logger.info("Found action URL : {}", furl);
 						}
 					} catch (IllegalStateException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			return furl;
 		}
 
 		private boolean login(String url, String uname, String pwd) {
 			logger.info("Login initiated");
 			boolean alreadyLoggedIn = isLoggedIn();
			// the url might change from time to time, so keep it updated
			if (!alreadyLoggedIn/* && TextUtils.isEmpty(url)*/) {
 				// find the url
 				HttpResponse response = HttpUtils
 						.execute(mContext, HttpMethod.GET, REFERENCE_SITE, null);
 				boolean found = false;
 				if (response != null) {
 					if (response.getStatusLine().getStatusCode()
 							== HttpStatus.SC_MOVED_TEMPORARILY) {
 						Header[] headers = response.getHeaders("Location");
 						if (headers != null) {
 							for (Header header : headers) {
 								if (header.getName().equalsIgnoreCase("Location")) {
 									url = header.getValue();
 									Uri uri = Uri.parse(url);
 									StringBuilder urlbuilder = new StringBuilder();
 									url = urlbuilder.append(uri.getScheme())
 											.append("://").append(uri.getAuthority())
 											.toString();
 									// now visit this url and get the action from
 									// the login form
 									url = getUrlFromPage(url);
 									if (!TextUtils.isEmpty(url)) {
 										logger.info("Found URL from page : {}", url);
 										SettingsManager.putString(mContext
 												, SettingsManager.URL, url);
 										found = true;
 									}
 								}
 							}
 						}
 					}
 				}
 				if (!found) return false;
 			}
 			Map<String, String> paramz = new HashMap<String, String>();
 			paramz.put(FIELD_AUTH_USER, uname);
 			paramz.put(FIELD_AUTH_PASS, pwd);
 			paramz.put(FIELD_AUTH_ACCEPT, FIELD_AUTH_ACCEPT_VALUE);
 			paramz.put(FIELD_AUTH_REDIR_URL, FIELD_AUTH_REDIR_URL_VAL);
 
 			HttpResponse response = HttpUtils
 					.execute(mContext, HttpMethod.POST, url, paramz);
 			if (response != null && response.getStatusLine().getStatusCode()
 					== HttpStatus.SC_OK) {
 				logger.info("Login succeeded");
 				try {
 					String loginPage = HttpUtils.readStreamIntoString(response
 							.getEntity().getContent());
 					logger.info(loginPage);
 				} catch (IllegalStateException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 				// set alarm to run service every 5 mins
 				setAlarm(REPEAT_FREQ, true);
 
 				return true;
 			}
 			return false;
 		}
 
 		private void setAlarm(long interval, boolean repeat) {
 			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 			Intent intent = new Intent(ACTION_KEEP_ALIVE);
 			intent.setClass(mContext, LoginService.class);
 			PendingIntent pi = PendingIntent.getService(mContext, REQUEST_CODE
 					, intent
 					, PendingIntent.FLAG_CANCEL_CURRENT);
 			long next = System.currentTimeMillis() + interval + 1000;
 			if (repeat) {
 				am.setRepeating(AlarmManager.RTC_WAKEUP, next, REPEAT_FREQ, pi);
 				// if show notification is enabled, show it
 				if (SettingsManager.getBoolean(mContext, SettingsManager.SHOW_NOTIF
 						, true)) {
 					long now = System.currentTimeMillis();
 					int limit = SettingsManager.getInt(mContext
 							, SettingsManager.KEEP_ALIVE, -1);
 					long login = SettingsManager.getLong(mContext
 							, SettingsManager.LOG_IN_TIME, now);
 		
 					NotificationManager nm = (NotificationManager) getSystemService(
 							NOTIFICATION_SERVICE);
 					Builder b = new NotificationCompat.Builder(mContext);
 					b.setOngoing(true);
 					b.setContentTitle(getResources().getString(R.string.app_name));
 					b.setContentText(getResources().getString(R.string.from)
 							+ " : " + sdf.format(new Date(login))
 							+ (limit <= 0 ? "" : " | " + getResources().getString(R
 									.string.upto) + " : " + sdf.format(new Date(login
 											+ limit * DateUtils.HOUR_IN_MILLIS))));
 					b.setSmallIcon(R.drawable.ic_launcher);
 					b.setTicker(getResources().getString(R.string.logged_in)
 							+ ", " + getResources().getString(R.string.internet_access));
 					Intent i = new Intent(mContext, CredentialActivity.class);
 					PendingIntent pia = PendingIntent.getActivity(mContext, 0, i, Intent
 							.FLAG_ACTIVITY_CLEAR_TOP);
 					b.setContentIntent(pia);
 					Notification n = b.build();
 					nm.notify(1000, n);
 				}
 			} else {
 				am.set(AlarmManager.RTC_WAKEUP, next, pi);
 			}
 		}
 
 		private boolean keepAlive(String url, String uname) {
 			logger.info("Keep alive initiated");
 			Map<String, String> paramz = new HashMap<String, String>();
 			paramz.put(FIELD_ALIVE_UN, uname);
 			paramz.put(FIELD_ALIVE, FIELD_ALIVE_VALUE);
 			HttpResponse response = HttpUtils
 					.execute(mContext, HttpMethod.POST, url, paramz);
 			boolean status = (response != null && (response.getStatusLine().getStatusCode()
 					== HttpStatus.SC_OK || response.getStatusLine().getStatusCode()
 					== HttpStatus.SC_NO_CONTENT));
 			if (response != null) {
 				logger.info("Keep alive response : {} / {}", response.toString()
 						, response.getStatusLine().toString());
 			}
 			logger.info("Keep alive status : {}", status);
 			return status;
 		}
 
 		private boolean logout(String url, String uname) {
 			logger.info("Logout initiated");
 			Map<String, String> paramz = new HashMap<String, String>();
 			paramz.put(FIELD_LOGOUT_ID, uname);
 			paramz.put(FIELD_LOGOUT, FIELD_LOGOUT_VALUE);
 			HttpResponse response = HttpUtils
 					.execute(mContext, HttpMethod.POST, url, paramz);
 			boolean status = (response != null && response.getStatusLine().getStatusCode()
 					== HttpStatus.SC_OK);
 			logger.info("Logout status : {}", status);
 
 			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 			Intent intent = new Intent(ACTION_KEEP_ALIVE);
 			intent.setClass(mContext, LoginService.class);
 			PendingIntent pi = PendingIntent.getService(mContext, REQUEST_CODE, intent
 					, PendingIntent.FLAG_CANCEL_CURRENT);
 			am.cancel(pi);
 			NotificationManager nm = (NotificationManager) getSystemService(
 					NOTIFICATION_SERVICE);
 			nm.cancel(1000);
 			return status;
 		}
 
 		private boolean shouldRenewOrLogin() {
 			long now = System.currentTimeMillis();
 			int limit = SettingsManager.getInt(mContext
 					, SettingsManager.KEEP_ALIVE, -1);
 			long login = SettingsManager.getLong(mContext
 					, SettingsManager.LOG_IN_TIME, now);
 			return limit > 0 ? now < (login + limit * DateUtils.HOUR_IN_MILLIS)
 					: true;
 		}
 	}
 }
