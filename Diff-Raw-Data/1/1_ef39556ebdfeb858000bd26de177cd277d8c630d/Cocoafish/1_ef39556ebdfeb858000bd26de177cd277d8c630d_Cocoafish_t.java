 package com.cocoafish.sdk;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.FileNameMap;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import oauth.signpost.OAuthConsumer;
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 import oauth.signpost.signature.AuthorizationHeaderSigningStrategy;
 import oauth.signpost.signature.HmacSha1MessageSigner;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 
 public class Cocoafish {
 	private String appKey = null;
 	private OAuthConsumer consumer = null;
 
 	private HttpClient httpClient = null;
 	private CookieStore cookieStore = null;
 	private Context curApplicationContext = null;
 	
 	private CCUser currentUser = null;
 
 	public Cocoafish(String appKey) {
 		this(appKey, (Context)null);
 	}
 	
 	public Cocoafish(String consumerKey, String consumerSecret) {
 		this(consumerKey, consumerSecret, (Context)null);
 	}
 	
 	public Cocoafish(String appKey, Context context) {
 		this.appKey = appKey;
 		curApplicationContext = context;
 		initializeSDK();
 	}
 
 	public Cocoafish(String consumerKey, String consumerSecret, Context context) {
 		consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
 		consumer.setMessageSigner(new HmacSha1MessageSigner());
 		consumer.setSigningStrategy(new AuthorizationHeaderSigningStrategy());
 		curApplicationContext = context;
 		initializeSDK();
 	}
 	
 	private void initializeSDK() {
 		httpClient = new DefaultHttpClient();
 		cookieStore = new BasicCookieStore();
 		try {
 			loadSessionInfo();
 		} catch (CocoafishError e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param url
 	 *            The last fragment of request url
 	 * @param method
 	 *            It only can be one of CCRequestMthod.GET, CCRequestMthod.POST,
 	 *            CCRequestMthod.PUT, CCRequestMthod.DELETE.
 	 * @param data
 	 *            The name-value pairs which is ready to be sent to server, the
 	 *            value only can be String type or java.io.File type
 	 * @param useSecure
 	 *            Decide whether use http or https protocol.
 	 * @return
 	 * @throws IOException
 	 *             If there is network problem, the method will throw this type
 	 *             of exception.
 	 * @throws CocoafishError
 	 *             If other problems cause the request cannot be fulfilled, the
 	 *             CocoafishError will be threw.
 	 */
 	public CCResponse sendRequest(String url, CCRequestMethod method, Map<String, Object> data, boolean useSecure) throws CocoafishError {
 		CCResponse response = null;
 
 		try {
 			// parameters
 			List<NameValuePair> paramsPairs = null; // store all request
 			Map<String, File> fileMap = null; // store the requested file and its parameter name
 
 			if (data != null && !data.isEmpty()) {
 				Iterator<String> it = data.keySet().iterator();
 				while (it.hasNext()) {
 					String name = it.next();
 					Object value = data.get(name);
 					
 					if (value instanceof String) { 
 						if (paramsPairs == null) 
 							paramsPairs = new ArrayList<NameValuePair>();
 						paramsPairs.add(new BasicNameValuePair(name, (String)value)); 
 					} else if (value instanceof File) { 
 						if (fileMap == null) 
 							fileMap = new HashMap<String, File>();
 						fileMap.put(name, (File) value);
 					}
 				}
 			}
 
 			StringBuffer requestUrl = null;
 			if (useSecure) {
 				requestUrl = new StringBuffer(CCConstants.BASE_URL_SECURE);
 			} else {
 				requestUrl = new StringBuffer(CCConstants.BASE_URL);
 			}
 			requestUrl.append(url);
 
 			if (appKey != null) {
 				requestUrl.append(CCConstants.KEY);
 				requestUrl.append(appKey);
 			}
 			
 			if(method == CCRequestMethod.GET || method == CCRequestMethod.DELETE) {
 				if(paramsPairs != null && !paramsPairs.isEmpty()) {
 					String queryString = URLEncodedUtils.format(paramsPairs, CCConstants.ENCODING_UTF8);
 					if(requestUrl.toString().indexOf("?") > 0) {
 						requestUrl.append("&");
 						requestUrl.append(queryString);
 					} else {
 						requestUrl.append("?");
 						requestUrl.append(queryString);
 					}
 				}
 			}
 			
 			URI reqUri = new URL(requestUrl.toString()).toURI();
 			
 			HttpUriRequest request = null;
 			if (method == CCRequestMethod.GET) {
 				request = new HttpGet(reqUri);
 			}
 			if (method == CCRequestMethod.POST) {
 				request = new HttpPost(reqUri);
 			}
 			if (method == CCRequestMethod.PUT) {
 				request = new HttpPut(reqUri);
 			}
 			if (method == CCRequestMethod.DELETE) {
 				request = new HttpDelete(reqUri);
 			}
 
 			if (request == null)
 				throw new CocoafishError("The request method is invalid.");
 
 			if (fileMap != null && fileMap.size() > 0) {
 				CCMultipartEntity entity = new CCMultipartEntity();
 
 				// Append the nameValuePairs to request's url string
 				if (paramsPairs != null && !paramsPairs.isEmpty()) {
 					for(NameValuePair pair: paramsPairs) {
 						entity.addPart(URLEncoder.encode(pair.getName()), URLEncoder.encode(pair.getValue()));
 					}
 				}
 
 				// Add up the file to request's entity.
 				Set<String> nameSet = fileMap.keySet();
 				Iterator<String> it = nameSet.iterator();
 				if (it.hasNext()) {
 					String name = it.next();
 					File file = fileMap.get(name);
 
 					FileNameMap fileNameMap = URLConnection.getFileNameMap();
 					String mimeType = fileNameMap.getContentTypeFor(file.getName());
 					// Assume there is only one file in the map.
 					entity.addPart(name, new FileBody(file, mimeType));
 				}
 				if (request instanceof HttpEntityEnclosingRequestBase) {
 					((HttpEntityEnclosingRequestBase) request).setEntity(entity);
 				}
 			} else {
 				if (paramsPairs != null && !paramsPairs.isEmpty()) {
 					if (request instanceof HttpEntityEnclosingRequestBase) {
 						((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(paramsPairs));
 					}
 				}
 			}
 
 			if (consumer != null) {
 				try {
 					consumer.sign(request);
 				} catch (OAuthMessageSignerException e) {
 					throw new CocoafishError(e.getLocalizedMessage());
 				} catch (OAuthExpectationFailedException e) {
 					throw new CocoafishError(e.getLocalizedMessage());
 				} catch (OAuthCommunicationException e) {
 					throw new CocoafishError(e.getLocalizedMessage());
 				}
 			}
 
 			HttpContext localContext = new BasicHttpContext();
 			localContext.setAttribute(ClientContext.COOKIE_STORE, getCookieStore());
 			
 			HttpResponse httpResponse = httpClient.execute(request, localContext);
 			HttpEntity entity = httpResponse.getEntity();
 
 			if (entity != null) {
 				// A Simple JSON Response Read
 				InputStream instream = entity.getContent();
 				String result = convertStreamToString(instream);
 
 				// A Simple JSONObject Creation
 				JSONObject jsonObject = new JSONObject(result);
 				response = new CCResponse(jsonObject);
 				updateSessionInfo(response);
 			}
 		} catch (Exception e) {
 			throw new CocoafishError(e.getLocalizedMessage());
 		}
 		return response;
 	}
 
 	/*
 	 * To convert the InputStream to String we use the
 	 * BufferedReader.readLine() method. We iterate until the BufferedReader
 	 * return null which means there's no more data to read. Each line will
 	 * appended to a StringBuilder and returned as String.
 	 */
 	private String convertStreamToString(InputStream is) {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder sb = new StringBuilder();
 
 		String line = null;
 		try {
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return sb.toString();
 	}
 
 	private void updateSessionInfo(CCResponse response) throws CocoafishError {
 		if(response != null && response.getMeta() != null) {
 			CCMeta meta = response.getMeta();
 			if(meta.getCode() == CCConstants.SUCCESS_CODE) {
 				if(CCConstants.LOGIN_METHOD.equals(meta.getMethod()) 
 						|| CCConstants.CREATE_METHOD.equals(meta.getMethod())) {
 					try {
 						if(response.getResponseData() != null) {
 							JSONArray array = response.getResponseData().getJSONArray(CCConstants.USERS);
 							if(array != null && array.length() > 0) {
 								currentUser = new CCUser(array.getJSONObject(0));
 								saveSessionInfo();
 							}
 						}
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				} else if(CCConstants.LOGOUT_METHOD.equals(meta.getMethod())) {
 					clearSessionInfo();
 				}
 			}
 		}
 	}
 	
 	private CookieStore getCookieStore() { 
 		return cookieStore; 
 	}
 
 	private void loadSessionInfo() throws CocoafishError {
 		if(curApplicationContext != null) {
 			try {
 				// read the user cookies
 				FileInputStream fis = curApplicationContext.openFileInput(CCConstants.COOKIES_FILE);
 				ObjectInputStream in = new ObjectInputStream(fis);
 				currentUser = (CCUser)in.readObject();
 				int size = in.readInt();
 				for (int i = 0; i < size; i++) {
 					SerializableCookie cookie = (SerializableCookie)in.readObject();
 					cookieStore.addCookie(cookie);
 				}
 			} catch (Exception e) {
 				throw new CocoafishError(e.getLocalizedMessage());
 			}
 		}
 	}
 
 	protected void saveSessionInfo() throws CocoafishError {
 		if(curApplicationContext != null) {
 			try {
 				// save the cookies
 				List<Cookie> cookies = cookieStore.getCookies();
 				if (cookies.isEmpty()) {
 					// should not happen when we have a current user but no cookies
 					return;
 				} else {
 					final List<Cookie> serialisableCookies = new ArrayList<Cookie>(cookies.size());
 					for (Cookie cookie : cookies) {
 						serialisableCookies.add(new SerializableCookie(cookie));
 					}
 					try {                  
 						FileOutputStream fos = curApplicationContext.openFileOutput(CCConstants.COOKIES_FILE, Context.MODE_PRIVATE);
 						ObjectOutputStream out = new ObjectOutputStream(fos);
 						out.writeObject(currentUser);
 						out.writeInt(cookies.size());
 						for (Cookie cookie : serialisableCookies) {
 							out.writeObject(cookie);
 						}
 						out.flush();
 						out.close();
 					}
 					catch(IOException ex) {
 						throw new CocoafishError(ex.getLocalizedMessage());
 					}
 				}
 			} catch (Exception e) {
 				throw new CocoafishError(e.getLocalizedMessage());
 			}
 		}
 	}
 
 	protected void clearSessionInfo() {
 		if(curApplicationContext != null) {
 			curApplicationContext.getFileStreamPath(CCConstants.COOKIES_FILE).delete();
 		}
		currentUser = null;
 	}
 
 	public CCUser getCurrentUser() {
 		return currentUser;
 	}
 }
