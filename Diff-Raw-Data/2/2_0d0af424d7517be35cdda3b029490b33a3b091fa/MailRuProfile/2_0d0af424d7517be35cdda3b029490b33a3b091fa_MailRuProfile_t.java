 package com.shingrus.myplayer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.security.KeyManagementException;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.ProtocolException;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.RedirectHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.SingleClientConnManager;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreConnectionPNames;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 public class MailRuProfile implements MyPlayerAccountProfile {
 	public static final String SWA_URL = "http://swa.mail.ru/cgi-bin/auth?";
 	public static final String MUSIC_URL = "http://my.mail.ru/musxml";
 	public static final String SUCCESS_OATH_PREFIX = "http://connect.mail.ru/oauth/success.html#";
 	public static final String APPID = "640583";
 	public static final String OAUTH_URL = "https://connect.mail.ru/oauth/authorize?client_id=" + APPID
 			+ "&response_type=token&redirect_uri=http%3A%2F%2Fconnect.mail.ru%2Foauth%2Fsuccess.html&display=mobile";
 	public static final String POST_OAUTH_TOKEN_URL = "https://appsmail.ru/oauth/token";
 
 	// AUTH CONTANTS
 	public static final String REFRESH_TOKEN_NAME = "refresh_token";
 	public static final String ACCESS_TOKEN_NAME = "access_token";
 	public static final String UID_RESPONSE_NAME = "x_mailru_vid";
 	public static final String EXPIRES_IN_RESPONSE_NAME = "expires_in";
 
 	// PREFERENCES CONSTANTS
 	public static final String REFRESH_TOKEN_KEY = "mailru_refresh_token";
 	public static final String ACCESS_TOKEN_KEY = "mailru_access_token";
 	public static final String UID_KEY = "mailru_uid";
 	public static final String LOGIN_KEY = "mailru_login";
 	public static final String PASSWORD_KEY = "mailru_password";
 
 	private static final String ACCESS_TOKEN_VALID_KEY = "mailru_valid_until";
 
 	// API CONSTANTS
 	private static final String BASE_API_URL = "http://www.appsmail.ru/platform/api?";
	private static final String GET_TRACKS_LIST_METHOD = "audio.get";
 	private static final String PRIVATE_KEY = "8bd7022c723f4cea429a70437d72ad07";
 	private static final String JSON_MUSIC_ID = "mid";
 	private static final String JSON_MUSIC_TITLE = "title";
 	private static final String JSON_MUSIC_URL = "link";
 	private static final String JSON_MUSIC_DURATION = "duration";
 	private static final String JSON_MUSIC_ARTIST = "artist";
 
 	public static final String MAILRU_COOKIE_NAME = "Mpop";
 
 	private String accessToken;
 	boolean isAccessTokenChanged = false;
 
 	private String refreshToken;
 	boolean isRefreshTokenChanged = false;
 
 	private String uid;
 	boolean isUidChanged = false;
 
 	long accessTokenValidUntil = 0;
 
 	String login, password;
 	boolean isLoginChanged = false, isPasswordChanged = false;
 
 	private String mpopCookie = "";
 	TrackListFetchingStatus lastFetchResult;
 
 	public MailRuProfile() {
 		accessToken = "";
 		refreshToken = "";
 	}
 
 	private enum GrantType {
 		PASSWORD, TOKEN
 	}
 
 	/**
 	 * @param Strins
 	 *            {@link GrantType} grantType if grantType is
 	 *            GrantType.PASSWORD, you should set login, password as strings
 	 * 
 	 * 
 	 */
 	private final AuhorizeStatus getTokens(GrantType grantType, String... strings) {
 		AuhorizeStatus result = AuhorizeStatus.UNKNOWN;
 		if (grantType == GrantType.TOKEN) {
 			setAccessToken("", 0);
 		}
 		if ((grantType == GrantType.PASSWORD && strings.length == 2 && strings[0] != null && strings[1] != null) || (grantType == GrantType.TOKEN)) {
 			String refreshToken = null;
 			String accessToken = null;
 			String login = null, password = null;
 			String uid = null;
 			int expires_in = 0;
 
 			BasicHttpParams httpParams = new BasicHttpParams();
 			httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, MyPlayerPreferences.CONNECTION_TIMEOUT);
 			httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, MyPlayerPreferences.CONNECTION_TIMEOUT);
 			try {
 				SchemeRegistry schemeRegistry = new SchemeRegistry();
 				KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
 				trustStore.load(null, null);
 
 				// SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
 
 				schemeRegistry.register(new Scheme("https", new MySSLSocketFactory(trustStore)
 
 				, 443));
 
 				SingleClientConnManager mgr = new SingleClientConnManager(httpParams, schemeRegistry);
 
 				HttpClient swaClient = new DefaultHttpClient(mgr, httpParams);
 				((AbstractHttpClient) (swaClient)).setRedirectHandler(new RedirectHandler() {
 					@Override
 					public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
 						return false;
 					}
 
 					@Override
 					public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
 						return null;
 					}
 				});
 				// grant_type=password&client_id=123&client_secret=234&
 				// username=test@mail.ru&password=qwerty&scope=widget
 
 				HttpPost httpPost = new HttpPost(POST_OAUTH_TOKEN_URL);
 				List<NameValuePair> postParams = new ArrayList<NameValuePair>(6);
 				if (grantType == GrantType.PASSWORD) {
 					postParams.add(new BasicNameValuePair("grant_type", "password"));
 				} else if (grantType == GrantType.TOKEN) {
 					postParams.add(new BasicNameValuePair("grant_type", "refresh_token"));
 				}
 				postParams.add(new BasicNameValuePair("client_id", APPID));
 				postParams.add(new BasicNameValuePair("client_secret", PRIVATE_KEY));
 				if (grantType == GrantType.PASSWORD) {
 					login = strings[0];
 					password = strings[1];
 					postParams.add(new BasicNameValuePair("username", login));
 					postParams.add(new BasicNameValuePair("password", password));
 				} else { // token
 					postParams.add(new BasicNameValuePair(REFRESH_TOKEN_NAME, this.refreshToken));
 				}
 				httpPost.setEntity(new UrlEncodedFormEntity(postParams));
 				HttpResponse response = swaClient.execute(httpPost);
 				if (response != null) {
 					StatusLine sl = response.getStatusLine();
 					if (sl.getStatusCode() == 200) {
 						InputStream is = response.getEntity().getContent();
 						BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 						StringBuilder body = new StringBuilder(4096);
 						String readed = "";
 						while ((readed = reader.readLine()) != null) {
 							body.append(readed);
 						}
 						if (body.length() > 0) {
 							Log.d("shingrus", body.toString());
 							try {
 								// JSONArray authData = new
 								// JSONArray(body.toString());
 								JSONObject authData = new JSONObject(body.toString());
 								accessToken = authData.getString(ACCESS_TOKEN_NAME);
 								refreshToken = authData.getString(REFRESH_TOKEN_NAME);
 								uid = authData.getString(UID_RESPONSE_NAME);
 								String expires = authData.getString(EXPIRES_IN_RESPONSE_NAME);
 
 								if (refreshToken != null && refreshToken.length() == 32 && accessToken != null && accessToken.length() == 32 && uid != null
 										&& uid.length() > 0) {
 									if (grantType == GrantType.PASSWORD) {
 										setRefreshToken(refreshToken);
 										setLogin(login);
 										setPassword(password);
 									}
 									try {
 										expires_in = Integer.parseInt(expires);
 									} catch (NumberFormatException e) {
 										expires_in = 100;
 									}
 									setAccessToken(accessToken, expires_in);
 									setUID(uid);
 									result = AuhorizeStatus.SUCCESS;
 								}
 
 							} catch (JSONException e) {
 								result = AuhorizeStatus.INVALID;
 
 							}
 						}
 					} else if (grantType == GrantType.PASSWORD) {
 						result = AuhorizeStatus.INVALID;
 					}
 				}
 			} catch (UnsupportedEncodingException e) {
 			} catch (ClientProtocolException e) {
 			} catch (IOException e) {
 			} catch (KeyStoreException e) {
 			} catch (NoSuchAlgorithmException e) {
 			} catch (CertificateException e) {
 			} catch (KeyManagementException e) {
 			} catch (UnrecoverableKeyException e) {
 			}
 		}
 		return result;
 	}
 
 	private final String md5(String s) {
 		try {
 			// Create MD5 Hash
 			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
 			digest.update(s.getBytes());
 			byte messageDigest[] = digest.digest();
 
 			// Create Hex String
 			StringBuffer hexString = new StringBuffer();
 			for (int i = 0; i < messageDigest.length; i++) {
 				hexString.append(Character.forDigit((messageDigest[i] & 0xf0) >> 4, 16));
 				hexString.append(Character.forDigit(messageDigest[i] & 0x0f, 16));
 				// Integer.toHexString(0xFF & messageDigest[i]));
 			}
 			return hexString.toString();
 
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	private final TrackListFetchingStatus getTrackList(List<MusicTrack> tl, String accessToken) {
 		TrackListFetchingStatus result = TrackListFetchingStatus.ERROR;
 
 		if (accessToken != null && accessToken.length() > 0) {
 			String params = "app_id=" + APPID + "method=" + GET_TRACKS_LIST_METHOD + "secure=0session_key=" + accessToken;
 
 			String md5 = md5("1324730981306483817app_id=423004method=friends.getsession_key=be6ef89965d58e56dec21acb9b62bdaa7815696ecbf1c96e6894b779456d330e");
 			md5 = md5(uid + params + PRIVATE_KEY);
 
 			String trackListURL = BASE_API_URL + "method=" + GET_TRACKS_LIST_METHOD + "&app_id=" + APPID + "&session_key=" + accessToken + "&secure=0&sig="
 					+ md5;
 			// well we have the url
 
 			BasicHttpParams httpParams = new BasicHttpParams();
 			httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, MyPlayerPreferences.CONNECTION_TIMEOUT);
 			httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, MyPlayerPreferences.CONNECTION_TIMEOUT);
 
 			AbstractHttpClient httpClient = new DefaultHttpClient(httpParams);
 			HttpGet httpGet = new HttpGet(trackListURL);
 			try {
 				HttpResponse response = httpClient.execute(httpGet);
 				if (response != null) {
 					StatusLine sl = response.getStatusLine();
 					if (sl.getStatusCode() == 200) {
 						InputStream is = response.getEntity().getContent();
 						BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 						StringBuilder body = new StringBuilder(4096);
 						String readed = "";
 						while ((readed = reader.readLine()) != null) {
 							body.append(readed);
 						}
 						if (body.length() > 0) {
 							try {
 								JSONArray tracksData = new JSONArray(body.toString());
 								for (int i = 0; i < tracksData.length(); i++) {
 									JSONObject trackDescription = tracksData.getJSONObject(i);
 									String id, title, murl, artist;
 									id = trackDescription.getString(JSON_MUSIC_ID);
 									title = trackDescription.getString(JSON_MUSIC_TITLE);
 									murl = trackDescription.getString(JSON_MUSIC_URL);
 									artist = trackDescription.getString(JSON_MUSIC_ARTIST);
 									result = TrackListFetchingStatus.SUCCESS;
 									Log.d("shingrus", "Got track: " + artist + "-" + title + ":" + murl);
 									MusicTrack mt = new MusicTrack(id, artist + "-" + title, murl, "");
 									tl.add(mt);
 								}
 
 							} catch (JSONException e) {
 								// we have an error - try to get access key and
 								// return back
 								result = TrackListFetchingStatus.UPDATEACCESSTOKEN;
 							}
 						}
 					} else {
 						result = TrackListFetchingStatus.UPDATEACCESSTOKEN;
 						setAccessToken("", 0);
 					}
 				}
 
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 		return result;
 	}
 
 	public final TrackListFetchingStatus getTrackListFromInternet() {
 		TrackListFetchingStatus result = TrackListFetchingStatus.UNKNOWN;
 		int attempts = 0;
 		do {
 			accessToken = getAccessToken();
 			attempts++;
 			if (accessToken != null && accessToken.length() == 32) {
 				List<MusicTrack> newTrackList = new LinkedList<MusicTrack>();
 				result = getTrackList(newTrackList, accessToken);
 				TrackList tl = TrackList.getInstance();
 				if (result == TrackListFetchingStatus.SUCCESS) {
 					for (MusicTrack mt : newTrackList) {
 						tl.addTrack(mt);
 					}
 				}
 			} else {
 				result = TrackListFetchingStatus.UPDATEACCESSTOKEN;
 			}
 		} while (result == TrackListFetchingStatus.UPDATEACCESSTOKEN && attempts < 2);
 		return result;
 	}
 
 	private void refreshAccessToken() {
 		getTokens(GrantType.TOKEN);
 	}
 
 	@Override
 	public final TrackListFetchingStatus lastFetchResult() {
 		return lastFetchResult;
 	}
 
 	@Override
 	public String getOAuthURL() {
 		return OAUTH_URL;
 	}
 
 	public void setAccessToken(String accessToken, int expiresIn) {
 		this.accessToken = accessToken;
 		this.accessTokenValidUntil = System.currentTimeMillis() + (expiresIn * 1000);
 		this.isAccessTokenChanged = true;
 	}
 
 	private final String getAccessToken() {
 		String result = null;
 		if (this.accessToken == null || this.accessToken.length() < 32 || accessTokenValidUntil == 0 || accessTokenValidUntil < System.currentTimeMillis()) {
 			refreshAccessToken();
 		}
 		result = this.accessToken;
 		return result;
 	}
 
 	public void setRefreshToken(String refreshToken) {
 		this.refreshToken = refreshToken;
 		this.isRefreshTokenChanged = true;
 	}
 
 	public final String getRefreshToken() {
 		return refreshToken;
 	}
 
 	@Override
 	public void setUID(String uid) {
 		this.uid = uid;
 		isUidChanged = true;
 
 	}
 
 	private final void setLogin(String login) {
 		this.login = login;
 		isLoginChanged = true;
 	}
 
 	private void setPassword(String password) {
 		this.password = password;
 		isPasswordChanged = true;
 	}
 
 	@Override
 	public void loadPreferences(SharedPreferences preferences) {
 		this.refreshToken = preferences.getString(REFRESH_TOKEN_KEY, "");
 		this.accessToken = preferences.getString(ACCESS_TOKEN_KEY, "");
 		this.accessTokenValidUntil = preferences.getLong(ACCESS_TOKEN_VALID_KEY, 0);
 		this.uid = preferences.getString(UID_KEY, "");
 		this.login = preferences.getString(LOGIN_KEY, "");
 		this.password = preferences.getString(PASSWORD_KEY, "");
 
 	}
 
 	@Override
 	public void storePreferences(SharedPreferences preferences) {
 		SharedPreferences.Editor editor = preferences.edit();
 		if (isAccessTokenChanged) {
 			editor.putString(ACCESS_TOKEN_KEY, accessToken);
 			editor.putLong(ACCESS_TOKEN_VALID_KEY, accessTokenValidUntil);
 		}
 		if (isRefreshTokenChanged) {
 			editor.putString(REFRESH_TOKEN_KEY, refreshToken);
 		}
 		if (isUidChanged) {
 			editor.putString(UID_KEY, uid);
 		}
 		if (isLoginChanged) {
 			editor.putString(LOGIN_KEY, login);
 		}
 		if (isPasswordChanged) {
 			editor.putString(PASSWORD_KEY, password);
 		}
 
 		editor.apply();
 		isAccessTokenChanged = isRefreshTokenChanged = isUidChanged = true;
 	}
 
 	@Override
 	public AuhorizeStatus authorize(String login, String password) {
 		AuhorizeStatus result = getTokens(GrantType.PASSWORD, login, password);
 		if (result == AuhorizeStatus.SUCCESS && refreshToken != null && refreshToken.length() == 32) {
 			result = AuhorizeStatus.SUCCESS;
 		}
 		return result;
 	}
 
 	private final String getMpopCookie() {
 
 		setMpopCookie("");
 		BasicHttpParams httpParams = new BasicHttpParams();
 		httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, MyPlayerPreferences.CONNECTION_TIMEOUT);
 		httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, MyPlayerPreferences.CONNECTION_TIMEOUT);
 		HttpClient swaClient = new DefaultHttpClient(httpParams);
 
 		((AbstractHttpClient) (swaClient)).setRedirectHandler(new RedirectHandler() {
 			@Override
 			public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
 				return false;
 			}
 
 			@Override
 			public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
 				return null;
 			}
 		});
 
 		try {
 			HttpPost httpPost = new HttpPost(SWA_URL);
 			List<NameValuePair> postParams = new ArrayList<NameValuePair>(6);
 			postParams.add(new BasicNameValuePair("Login", login));
 			postParams.add(new BasicNameValuePair("Password", password));
 			httpPost.setEntity(new UrlEncodedFormEntity(postParams));
 			HttpResponse swaResponse = swaClient.execute(httpPost);
 			if (null != swaResponse) {
 				for (Cookie cookie : ((AbstractHttpClient) swaClient).getCookieStore().getCookies()) {
 					if (cookie.getName().equalsIgnoreCase(MAILRU_COOKIE_NAME)) {
 						setMpopCookie(cookie.getValue());
 						break;
 					}
 				}
 			}
 		} catch (ClientProtocolException e) {
 		} catch (IOException e) {
 		}
 
 		return mpopCookie;
 	}
 
 	/**
 	 * Setter for the internal field mpopCookie
 	 * 
 	 * @param mpopCookie
 	 */
 	private void setMpopCookie(String mpopCookie) {
 		this.mpopCookie = mpopCookie;
 	}
 
 	public boolean downloadAudioFile(String url, File whereToDownload) {
 		boolean result = false;
 
 		if (mpopCookie == null || mpopCookie.length() < 10) {
 			getMpopCookie();
 		}
 
 		if (mpopCookie != null && mpopCookie.length() > 0) {
 			BasicHttpParams httpParams = new BasicHttpParams();
 			httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, MyPlayerPreferences.CONNECTION_TIMEOUT);
 			httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, MyPlayerPreferences.CONNECTION_TIMEOUT);
 
 			AbstractHttpClient httpClient = new DefaultHttpClient(httpParams);
 			HttpGet httpGet = new HttpGet(url);
 
 			BasicClientCookie cookie = new BasicClientCookie(MailRuProfile.MAILRU_COOKIE_NAME, mpopCookie);
 			cookie.setDomain(".mail.ru");
 			cookie.setExpiryDate(new Date(2039, 1, 1, 0, 0));
 			cookie.setPath("/");
 			httpClient.getCookieStore().addCookie(cookie);
 			try {
 				HttpResponse resp = httpClient.execute(httpGet);
 				StatusLine status = resp.getStatusLine();
 				if (status != null && status.getStatusCode() == 200) {
 					Header contentLengthH = resp.getFirstHeader("Content-Length");
 					Header contentTypeH = resp.getFirstHeader("Content-Type");
 
 					if (contentTypeH != null && contentTypeH.getValue().contains("audio")) {
 						InputStream is = resp.getEntity().getContent();
 						byte[] buf = new byte[4 * 1024 * 10];
 						int readed = 0;
 						int written = 0;
 
 						OutputStream out = new FileOutputStream(whereToDownload);
 						while ((readed = is.read(buf)) != -1) {
 							out.write(buf, 0, readed);
 							written += readed;
 						}
 						if (written > 0) {
 							if (contentLengthH != null) {
 								int fileLength = Integer.parseInt(contentLengthH.getValue());
 								if (fileLength == written)
 									result = true;
 							}
 						}
 					}
 				} else if (status.getStatusCode() == 500) { // seems like we are
 															// not authorized
 					setMpopCookie("");
 				}
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 			}
 		}
 		return result;
 	}
 
 	// public final TrackListFetchingStatus getTrackListFromInternet2(final
 	// TrackList tl, String mpopCookie) {
 	// lastFetchResult = TrackListFetchingStatus.ERROR;
 	//
 	// if (mpopCookie != null && mpopCookie.length() > 0) {
 	// BasicHttpParams httpParams = new BasicHttpParams();
 	// httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
 	// CONNECTION_TIMEOUT);
 	// httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
 	// CONNECTION_TIMEOUT);
 	//
 	// AbstractHttpClient httpClient = new DefaultHttpClient(httpParams);
 	// HttpGet httpGet = new HttpGet(MailRuProfile.MUSIC_URL);
 	//
 	// BasicClientCookie cookie = new
 	// BasicClientCookie(MailRuProfile.MAILRU_COOKIE_NAME, mpopCookie);
 	// cookie.setDomain(".mail.ru");
 	// cookie.setExpiryDate(new Date(2039, 1, 1, 0, 0));
 	// cookie.setPath("/");
 	// httpClient.getCookieStore().addCookie(cookie);
 	//
 	// try {
 	// HttpResponse musicListResponse = httpClient.execute(httpGet);
 	//
 	// if (null != musicListResponse &&
 	// musicListResponse.getStatusLine().getStatusCode() == 200) {
 	// lastFetchResult = TrackListFetchingStatus.SUCCESS;
 	// SAXParserFactory sf = SAXParserFactory.newInstance();
 	// try {
 	// SAXParser parser = sf.newSAXParser();
 	// XMLReader xr = parser.getXMLReader();
 	// xr.setContentHandler(new DefaultHandler() {
 	//
 	// MusicTrack mt = new MusicTrack();
 	//
 	// public final String TRACK_TAG = "TRACK", NAME_TAG = "NAME", URL_TAG =
 	// "FURL", PARAM_ID = "id", MUSICLIST_TAG = "MUSIC_LIST";
 	// boolean isInsideTrackTag = false, isInsideFURL = false, isInsideName =
 	// false, isInsideMusicList = false;
 	// StringBuilder builder = new StringBuilder();
 	//
 	// @Override
 	// public void characters(char[] ch, int start, int length) throws
 	// SAXException {
 	// if (isInsideFURL || isInsideName || isInsideMusicList) {
 	// builder.append(ch, start, length);
 	// }
 	// }
 	//
 	// @Override
 	// public void startElement(String uri, String localName, String qName,
 	// Attributes attributes) throws SAXException {
 	// // Log.i("shingrus",
 	// // "XML: start element: " + localName);
 	// super.startElement(uri, localName, qName, attributes);
 	// if (localName.equalsIgnoreCase(TRACK_TAG)) {
 	// isInsideTrackTag = true;
 	// mt = new MusicTrack();
 	// mt.setId(attributes.getValue(PARAM_ID));
 	// } else if (localName.equalsIgnoreCase(URL_TAG) && isInsideTrackTag) {
 	// isInsideFURL = true;
 	// } else if (localName.equalsIgnoreCase(NAME_TAG) && isInsideTrackTag) {
 	// isInsideName = true;
 	// } else if (localName.equalsIgnoreCase(MUSICLIST_TAG)) {
 	// isInsideMusicList = true;
 	// }
 	//
 	// }
 	//
 	// @Override
 	// public void endElement(String uri, String localName, String qName) throws
 	// SAXException {
 	//
 	// if (localName.equalsIgnoreCase(TRACK_TAG)) {
 	// isInsideTrackTag = false;
 	// isInsideName = isInsideFURL = false;
 	// if (mt.isComplete()) {
 	//
 	// Log.i("shingrus", mt.toString());
 	//
 	// // well, we have completed mt
 	// // object with url and id
 	// tl.addTrack(mt);
 	// }
 	// } else if (localName.equalsIgnoreCase(URL_TAG)) {
 	// isInsideFURL = false;
 	// mt.setUrl(builder.toString().replaceAll("[\\r\\n\\s]", ""));
 	// } else if (localName.equalsIgnoreCase(NAME_TAG)) {
 	// isInsideName = false;
 	// mt.setTitle(builder.toString().replaceAll("^\\s+", ""));
 	// } else if (localName.equalsIgnoreCase(MUSICLIST_TAG)) {
 	// isInsideMusicList = false;
 	// if (builder.toString().equals("Error!")) {
 	// lastFetchResult = TrackListFetchingStatus.NEEDREAUTH;
 	// }
 	//
 	// }
 	// if (builder.length() > 0) {
 	// builder.setLength(0);
 	// }
 	// }
 	// });
 	// InputSource is = new
 	// InputSource(musicListResponse.getEntity().getContent());
 	// xr.parse(is);
 	// } catch (ParserConfigurationException e) {
 	// e.printStackTrace();
 	// } catch (SAXException e) {
 	// e.printStackTrace();
 	// }
 	//
 	// }
 	// } catch (ClientProtocolException e) {
 	// e.printStackTrace();
 	// } catch (IllegalStateException e) {
 	// e.printStackTrace();
 	// } catch (IOException e) {
 	// e.printStackTrace();
 	// }
 	// } else
 	// lastFetchResult = TrackListFetchingStatus.NEEDREAUTH;
 	//
 	// return lastFetchResult;
 	// }
 
 }
