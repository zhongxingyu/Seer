 /*
  * The MIT License
  *
  * Copyright (c) 2010 Petar Petrov
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package net.vexelon.mobileops;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.security.InvalidAlgorithmParameterException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 
 import net.vexelon.myglob.configuration.Defs;
 import net.vexelon.myglob.utils.TrustAllSocketFactory;
 import net.vexelon.myglob.utils.UserAgentHelper;
 import net.vexelon.myglob.utils.Utils;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.NoHttpResponseException;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpRequestRetryHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.SingleClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 
 import android.util.Log;
 
 public class GLBHttpClient implements Client {
 
 	private final String HTTP_MYGLOBUL_SITE = "https://my.globul.bg";
 
 	private final int DEFAULT_BUFFER_SIZE = 1024;
 	private final String RESPONSE_ENCODING = "Windows-1251";
 	private final int MAX_REQUEST_RETRIES = 2;
 
 	private String username;
 	private String password;
 	private DefaultHttpClient httpClient = null;
 	private CookieStore httpCookieStore = null;
 	
 	private HashMap<GLBRequestType, String> operationsHash;
 
 	public GLBHttpClient(String username, String password) {
 		this.username = username;
 		this.password = password;
 
 		initHttpClient();
 	}
 
 	public void close() {
 		if ( httpClient != null )
 			httpClient.getConnectionManager().shutdown();
 	}
 
 	/**
 	 * Perform login into the web system using the specified user and password
 	 */
 	public void login() throws HttpClientException, InvalidCredentialsException, SecureCodeRequiredException {
 		
 //		rametersapplication/x-www-form-urlencoded
 //		action
 //		continuation	myglobul.portal%3Faction%3Duserhome%26pkey%3D0%26jkey%3D0
 //		image.x	0
 //		image.y	0
 //		imglogin	JFNH
 //		password	mypassword
 //		refid		0a7b8fa558b46142e2d400ae0f2548f2f
 //		username	0899123456
 		
 		try {
 			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
 			qparams.add(new BasicNameValuePair("action", "loginexec"));
 			//qparams.add(new BasicNameValuePair("refid", ""));
 			qparams.add(new BasicNameValuePair("continuation",
 					URLDecoder.decode("myglobul.portal%3Faction%3Duserhome%26pkey%3D0%26jkey%3D0",
 							"UTF-8")));
 			qparams.add(new BasicNameValuePair("image.x", Integer.toString(Utils.getRandomInt(1024)) ));
 			qparams.add(new BasicNameValuePair("image.y", Integer.toString(Utils.getRandomInt(768)) ));
 			qparams.add(new BasicNameValuePair("password", password));
 			qparams.add(new BasicNameValuePair("username", username));
 			
 			handleLogin(qparams);
 			
 			operationsHash = findOperationsHashCodes();
 			
 		} catch (UnsupportedEncodingException e) {
 			throw new HttpClientException("Failed to create url! " + e.getMessage(), e);
 		}
 	}
 
 	public void logout()
 		throws HttpClientException {
 
 		StringBuilder fullUrl = new StringBuilder(100);
 		fullUrl.append(HTTP_MYGLOBUL_SITE).append(GLBRequestType.LOGOUT.getPath()).append("?")
 			.append(GLBRequestType.LOGOUT.getParams());
 
 		try {
 			HttpGet httpGet = new HttpGet(fullUrl.toString());
 			HttpResponse resp = httpClient.execute(httpGet);
 			StatusLine status = resp.getStatusLine();
 
 			if ( status.getStatusCode() != HttpStatus.SC_OK )
 				throw new HttpClientException(status.getReasonPhrase(), status.getStatusCode());
 
 			resp.getEntity().consumeContent();
 		} catch (ClientProtocolException e) {
 			throw new HttpClientException("Client protocol error!" + e.getMessage(), e);
 		} catch (IOException e) {
 			throw new HttpClientException("Client error!" + e.getMessage(), e);
 		}
 	}
 	
 	public String getCurrentBalance()
 		throws HttpClientException {
 
 		return doPostRequest(GLBRequestType.GET_BALANCE);
 	}
 
 	public String getAvailableMinutes()
 		throws HttpClientException {
 
 		return doPostRequest(GLBRequestType.GET_MINUTES);
 	}
 
 	public String getAvailableInternetBandwidth()
 		throws HttpClientException {
 
 		return doPostRequest(GLBRequestType.GET_BANDWIDTH);
 	}
 
 	public String getCreditLimit()
 		throws HttpClientException {
 
 		return doPostRequest(GLBRequestType.GET_CREDITLIMIT);
 	}
 
 	public String getAvailableMSPackage()
 		throws HttpClientException {
 
 		return doPostRequest(GLBRequestType.GET_MSPACKAGE);
 	}
 
 	// ---
 
 	/**
 	 * Initialize Http Client
 	 */
 	private void initHttpClient() {
 
 		HttpParams params = new BasicHttpParams();
 		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		params.setParameter(CoreProtocolPNames.USER_AGENT, UserAgentHelper.getRandomUserAgent());
 		//params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
 
 		// Bugfix #1: The target server failed to respond
 		params.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.FALSE);
 
 		DefaultHttpClient client = new DefaultHttpClient(params);
 		httpCookieStore = new BasicCookieStore();
 		client.setCookieStore(httpCookieStore);
 
 		// Bugfix #1: Adding retry handler to repeat failed requests
 		HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
 
 			@Override
 			public boolean retryRequest(IOException exception, int executionCount,
 					HttpContext context) {
 
 				if (executionCount >= MAX_REQUEST_RETRIES) {
 					return false;
 				}
 
 				if (exception instanceof NoHttpResponseException || exception instanceof ClientProtocolException) {
 					return true;
 				}
 
 				return false;
 			}
 		};
 		client.setHttpRequestRetryHandler(retryHandler);
 
 		// SSL
 		HostnameVerifier verifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
 
 		try {
 			SchemeRegistry registry = new SchemeRegistry();
 			registry.register(new Scheme("http", new PlainSocketFactory(), 80));
 			registry.register(new Scheme("https", new TrustAllSocketFactory(), 443));
 
 			SingleClientConnManager connMgr = new SingleClientConnManager(
 					client.getParams(), registry);
 
 			httpClient = new DefaultHttpClient(connMgr, client.getParams());
 		}
 		catch (InvalidAlgorithmParameterException e) {
 //			Log.e(Defs.LOG_TAG, "", e);
 
 			// init without connection manager
 			httpClient = new DefaultHttpClient(client.getParams());
 		}
 
 		HttpsURLConnection.setDefaultHostnameVerifier(verifier);
 
 	}
 
 	/**
 	 * Login logic
 	 * @param qparams
 	 */
 	private void handleLogin(List<NameValuePair> qparams) throws HttpClientException, InvalidCredentialsException, 
 	SecureCodeRequiredException, UnsupportedEncodingException {
 
 		StringBuilder fullUrl = new StringBuilder(100);
 		fullUrl.append(HTTP_MYGLOBUL_SITE).append(GLBRequestType.LOGIN.getPath());
 
 		HttpPost httpPost = createPostRequest(fullUrl.toString(), qparams);
 		HttpResponse resp;
 		
 		try {
 			resp = httpClient.execute(httpPost);
 		} catch (Exception e) {
 			throw new HttpClientException("Client protocol error!" + e.getMessage(), e);
 		}
 		
 		StatusLine status = resp.getStatusLine();
 
 		if ( status.getStatusCode() == HttpStatus.SC_OK ) {
 
 			// retrieve contents of the reply
 			HttpEntity entity = resp.getEntity();
 			ByteArrayOutputStream baos = null;
 			try {
 				baos = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
 				entity.writeTo(baos);
 			} catch (IOException e) {
 				throw new HttpClientException("Failed to load response! " + e.getMessage(), e);
 			} finally {
 				if (baos != null) try { baos.close(); } catch (IOException e) {};
 			}
 
 			String content = baos.toString();
 			
 			// if the username is not present in the content, then we're obviously not logged in
 			if ( content.indexOf("action=pdetails") == -1 || content.indexOf("action=chpass") == -1 ) {
 				//check if secure code image is sent
 				if ( content.indexOf("/mg/my/GetImage?refid=") != -1 ) {
 					//TODO: retrieve image url
 					//<img class="code" alt="Ако се затруднявате с разчитането на кода от картинката, моля кликнете
 					//върху нея за да я смените." src="/mg/my/GetImage?refid=b7b8fa558b461f1e2d400ae0f3348f2f">
 					throw new SecureCodeRequiredException("");
 				}
 
 				throw new InvalidCredentialsException();
 			}
 		} else if ( status.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY || 
 				status.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY || 
 				status.getStatusCode() == HttpStatus.SC_SEE_OTHER ||
 				status.getStatusCode() == HttpStatus.SC_TEMPORARY_REDIRECT) {
 			
 			// XXX Kind of a hack (sometimes we get 302 from the web serv),
 			//     May not work if Globul changes impl.	
 			//	   What we should do is proper redirect and parsing.
 			// See http://hc.apache.org/httpclient-legacy/redirects.html
 			
 //			String redirectLocation;
 //	        Header locationHeader = resp.getFirstHeader("location");
 //	        if (locationHeader != null) {
 //	            redirectLocation = locationHeader.getValue();
 //	        } else {
 //	            // The response is invalid and did not provide the new location for
 //	            // the resource.  Report an error or possibly handle the response
 //	            // like a 404 Not Found error.
 //	        }
 			
 			try {
 				resp.getEntity().consumeContent();
 			} catch (IOException e) {
 				throw new HttpClientException("Could not consume MOVED_TEMPORARILY content!", e);
 			}			
 			
 		} else {
 			// Unhandled response
 			throw new HttpClientException(status.getReasonPhrase(), status.getStatusCode());
 		}
 	}
 	
 	private HashMap<GLBRequestType, String> findOperationsHashCodes() throws HttpClientException {
 		
 		HashMap<GLBRequestType, String>  result = new HashMap<GLBRequestType, String>();
 		
 		StringBuilder fullUrl = new StringBuilder(100);
 		fullUrl.append(HTTP_MYGLOBUL_SITE).append(GLBRequestType.PAGE_BILLCHECK.getPath()).append("?")
 			.append(GLBRequestType.PAGE_BILLCHECK.getParams());
 
 		BufferedReader reader = null;
 		
 		try {
 			HttpGet httpGet = new HttpGet(fullUrl.toString());
 			HttpResponse resp = httpClient.execute(httpGet);
 			StatusLine status = resp.getStatusLine();
 			
 			if ( status.getStatusCode() == HttpStatus.SC_OK ) {
 				
 				reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
 				
 				boolean inParam = false;
 				String line = null;
 				GLBRequestType reqType = null;
 				
 				while((line = reader.readLine()) != null) {
 					
 					if (line.contains("performAjaxRequest")) {
 						// reset hash code expect, since we obviously passed the last one
 						inParam = false;
 						
 						String params[] = line.split("=");
 						if (params.length > 1) {
 							
 							if (Defs.LOG_ENABLED)
 								Log.d(Defs.LOG_TAG, "Found param: " + params[1]);
 							
 							// extract keyword
 							Pattern p = Pattern.compile("([a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE);
 							Matcher m = p.matcher(params[1]);
 							if (m.find()) {
 //								Log.v(Defs.LOG_TAG, "Match: " + m.group());
 								
 								reqType = GLBRequestType.getFromAction(m.group());
 								if (reqType != null) {
 									// we expect to find hash code, next
 									inParam = true;
 								} 
 							}
 							
 							continue;
 						}
 					}
 					
 					if (inParam) {
 						if (line.contains("action=billcheck")) {
 							String params[] = line.split(",");
 							if (params.length > 1) {
 								
 								if (Defs.LOG_ENABLED)
 									Log.d(Defs.LOG_TAG, "Hash param: " + params[1]);
 								
 								inParam = false;
 								
 								// extract hash
 								Pattern p = Pattern.compile("([a-z0-9]+)", Pattern.CASE_INSENSITIVE);
 								Matcher m = p.matcher(params[1]);
 								if (m.find()) {
 									
 									if (Defs.LOG_ENABLED)
 										Log.v(Defs.LOG_TAG, "Putting: " + reqType.getParams() + " to " + m.group());
 									
 									result.put(reqType, m.group());
 								}
 							}
 						}
 					}
 					
 				}
 				
 			} else {
 				throw new HttpClientException(status.getReasonPhrase(), status.getStatusCode());
 			}
 			
 		} catch (ClientProtocolException e) {
 			throw new HttpClientException("Client protocol error!" + e.getMessage(), e);
 		} catch (IOException e) {
 			throw new HttpClientException("Client error!" + e.getMessage(), e);
 		} finally {
 			if (reader != null) try { reader.close(); } catch (IOException e) {};
 		}
 		
 		return result;
 	}	
 	
 	private HttpPost createPostRequest(String url, List<NameValuePair> qparams)
 		throws UnsupportedEncodingException {
 		HttpPost httpPost = new HttpPost(url);
 		httpPost.setEntity(new UrlEncodedFormEntity(qparams, HTTP.UTF_8));
 		return httpPost;
 	}
 
 	private String doPostRequest(GLBRequestType requestType) throws HttpClientException {
 
 		HttpResponse resp;
 		try {
 			List<NameValuePair> qparams = requestType.getParamsAsList();
 			// Fix Issue #7 - Page does not exist
 			qparams.add(new BasicNameValuePair("parameter", this.operationsHash.get(requestType)));
 			
//			for (NameValuePair nameValuePair : qparams) {
//				Log.d(Defs.LOG_TAG, "Param: " + nameValuePair.getName() + " = " + nameValuePair.getValue());
//			}
			
 			HttpPost httpPost = createPostRequest(HTTP_MYGLOBUL_SITE + requestType.getPath(), qparams);
 			httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
 			httpPost.setHeader("X-Prototype-Version", "1.6.0.2");
			
 			resp = httpClient.execute(httpPost);
 		} catch (Exception e) {
 			throw new HttpClientException("Client protocol error!" + e.getMessage(), e);
 		}
 		
 		StatusLine status = resp.getStatusLine();
 		
 		if (status.getStatusCode() != HttpStatus.SC_OK)
 			throw new HttpClientException(status.getReasonPhrase(), status.getStatusCode());
 
 		HttpEntity entity = resp.getEntity();
 		ByteArrayOutputStream baos = null;
 		try {
 			baos = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
 			entity.writeTo(baos);
 		} catch (Exception e) {
 			throw new HttpClientException("Failed to load response! " + e.getMessage(), e);
 		} finally {
 			if (baos != null) try { baos.close(); } catch (IOException e) {};
 		}
 
 		try {
 			return new String(baos.toByteArray(), RESPONSE_ENCODING);
 		} catch (UnsupportedEncodingException e) {
 			return new String(baos.toByteArray()); // XXX check this!
 		}
 	}
 
 }
