 package org.macno.puma.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 
 import javax.net.ssl.SSLException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 
 import org.apache.http.Header;
 import org.apache.http.HttpException;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.ProtocolException;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.AuthState;
 import org.apache.http.auth.Credentials;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.RedirectHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.MIME;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.AbstractContentBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.ExecutionContext;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import android.util.Log;
 
 public class HttpUtil {
 
 	private static final String VERSION = "1.1";
 
 	private static final String IMAGE_MIME_JPG = "image/jpeg";
 	private static final String IMAGE_MIME_PNG = "image/png";
 
 	public static final String GET = "GET";
 	public static final String POST = "POST";
 	public static final String DELETE = "DELETE";
 
 	private static final Integer DEFAULT_REQUEST_TIMEOUT = 30000;
 	private static final Integer DEFAULT_POST_REQUEST_TIMEOUT = 40000;
 
 	//private AuthScope mAuthScope;
 	private DefaultHttpClient mClient;
 
 	private CommonsHttpOAuthConsumer consumer ;
 
 	private String mHost;
 
 	private String mUserAgent = "Mozilla/5.0  (Linux; U; Android " + 
 			android.os.Build.VERSION.RELEASE + ") HttpUtil/" + VERSION + " Mobile";
 
 	//	public HttpUtil() {
 	//		HttpParams params = getHttpParams();
 	//        SchemeRegistry schemeRegistry = new SchemeRegistry();
 	//        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 	//        schemeRegistry.register(new Scheme("https", 
 	//                SSLSocketFactory.getSocketFactory(), 443));
 	//        ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
 	//        mClient = new DefaultHttpClient(manager,params);
 	//	}
 
 	public HttpParams getHttpParams() {
 		final HttpParams params = new BasicHttpParams();
 		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 		HttpProtocolParams.setContentCharset(params, "UTF-8");
 
 		HttpConnectionParams.setStaleCheckingEnabled(params, true);
 		//        HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
 		HttpConnectionParams.setSoTimeout(params, DEFAULT_REQUEST_TIMEOUT);
 		HttpConnectionParams.setSocketBufferSize(params, 2*8192);
 
 		//        HttpClientParams.setRedirecting(params, true);
 
 		HttpProtocolParams.setUserAgent(params, getUserAgent());
 		HttpProtocolParams.setUseExpectContinue(params,false);
 
 		return params;
 
 	}
 
 	private String getUserAgent() {
 		return mUserAgent;
 	}
 
 	//	public HttpUtil(String host) {
 	//		HttpParams params = getHttpParams();
 	//        SchemeRegistry schemeRegistry = new SchemeRegistry();
 	//        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 	//        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
 	//        ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
 	//        mClient = new DefaultHttpClient(manager,params);
 	//		mHost=host;
 	//	}
 
 	public void setHostXXX(String host) {
 		mHost=host;
 	}
 
 	public void setHost(String host,boolean untrusted) {
 		HttpParams params = getHttpParams();
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
 		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 
 		mHost=host;
 		if(untrusted) {
 			schemeRegistry.register(new Scheme("https", 
 					UntrustedSSLSocketFactory.getSocketFactory(), 443));
 		} else {
 			schemeRegistry.register(new Scheme("https", 
 					SSLSocketFactory.getSocketFactory(), 443));
 		}
 		ClientConnectionManager manager = new ThreadSafeClientConnManager(params, schemeRegistry);
 		mClient = new DefaultHttpClient(manager,params);
 	}
 
 	public void setCredentials(String username, String password) {
 
 		Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
 		String host=AuthScope.ANY_HOST;
 		if (mHost!=null)
 			host=mHost;
 		BasicCredentialsProvider cP = new BasicCredentialsProvider(); 
 		cP.setCredentials(new AuthScope(host, AuthScope.ANY_PORT, AuthScope.ANY_REALM), defaultcreds);
 		mClient.setCredentialsProvider(cP);
 		mClient.addRequestInterceptor(preemptiveAuth, 0);
 
 	}
 
 	public void setOAuthConsumer(CommonsHttpOAuthConsumer consumer ) {
 		this.consumer=consumer;
 	}
 
 	public CommonsHttpOAuthConsumer getOAuthConsumer() {
 		return consumer;
 	}
 
 	public DefaultHttpClient getHttpClient() {
 		return mClient;
 	}
 
 	public JSONObject getJsonObject(String url) throws HttpUtilException {
 		return getJsonObject(url,GET);
 	}
 
 	public JSONObject getJsonObject(String url, String httpMethod) throws HttpUtilException {
 		return getJsonObject(url,httpMethod,"");
 	}
 
 	public JSONObject getJsonObject(String url, String httpMethod,
 			ArrayList<NameValuePair> params) throws HttpUtilException, SSLException {
 		JSONObject json = null;
 		try {
 			json = new JSONObject(StreamUtil.toString(requestData(url,httpMethod,params)));
 		} catch (SSLException e ) {
 			throw e;
 
 		} catch (JSONException e) {
 			throw new HttpUtilException(998,"Non json response: " + e.getMessage());
 		} catch (IOException e) {
 			throw new HttpUtilException(1200,"IOException: " + e.getMessage());
 		}
 		return json;
 	}
 
 	public JSONObject getJsonObject(String url, ArrayList<NameValuePair> params,String attachmentParam, File attachment) throws IOException,HttpUtilException {
 		JSONObject json = null;
 		try {
 			json = new JSONObject(StreamUtil.toString(requestData(url,params,attachmentParam,attachment)));
 		} catch (JSONException e) {
 			throw new HttpUtilException(998,"Non json response: " + e.toString());
 		}
 		return json;
 	}
 
 	public JSONObject getJsonObject(String url, String httpMethod, String requestBody) throws HttpUtilException {
 		JSONObject json = null;
 		try {
 			json = new JSONObject(StreamUtil.toString(requestData(url,httpMethod,requestBody)));
 		} catch (JSONException e) {
 			throw new HttpUtilException(998,"Non json response: " + e.getMessage());
 		} catch (IOException e) {
 			throw new HttpUtilException(1200,"IOException: " + e.getMessage());
 		}
 		return json;
 	}
 
 
 	public JSONArray getJsonArray(String url) throws HttpUtilException {
 		return getJsonArray(url,GET,null);
 	}
 
 	public JSONArray getJsonArray(String url, String httpMethod) 
 			throws HttpUtilException {
 		return getJsonArray(url,httpMethod,null);
 	}
 
 	public JSONArray getJsonArray(String url, String httpMethod,
 			ArrayList<NameValuePair> params) throws HttpUtilException {
 		return getJsonArray(url,httpMethod,params,false);
 	}
 
 	public JSONArray getJsonArray(String url, String httpMethod,
 			ArrayList<NameValuePair> params, boolean debug) throws HttpUtilException {
 		JSONArray json = null;
 		InputStream is = null;
 		try {
 			is = requestData(url,httpMethod,params);
 			String s = StreamUtil.toString(is);
 			if(debug)
 				Log.d("Mustard","\n"+s+"\n");
 			json = new JSONArray(s);
 		} catch (JSONException e) {
 			throw new HttpUtilException(998,"Non json response: " + e.toString());
 		} catch (IOException e) {
 			throw new HttpUtilException(1200,"IOException: " + e.getMessage());
 		} finally {
 			if(is != null) {
 				try { is.close();} catch (Exception e){}
 			}
 		}
 		return json;
 	}
 
 	public InputStream requestData(String url, String httpMethod,ArrayList<NameValuePair> params) throws HttpUtilException, SSLException {
 		return requestData(url, httpMethod,params,0);
 	}
 
 	public InputStream requestData(String url, String httpMethod,String requestBody) throws HttpUtilException, SSLException {
 		return requestData(url, httpMethod,requestBody,0);
 	}
 
 	private HashMap<String,String> mHeaders;
 
 	public void setContentType(String mimeType) {
 		if(mHeaders == null) {
 			mHeaders = new HashMap<String, String>();
 		}
 		mHeaders.put("Content-Type", mimeType);
 	}
 
 	public void setExtraHeaders(HashMap<String,String> headers) {
 		mHeaders = headers;
 	}
 
 	public void addExtraHeaders(HashMap<String,String> headers) {
 		if(mHeaders == null) {
 			mHeaders = headers;
 		} else {
 			mHeaders.putAll(headers);
 		}
 	}
 
 	public InputStream requestData(String url, String httpMethod,ArrayList<NameValuePair> params, int loop) throws HttpUtilException, SSLException {
 		return requestData(url,httpMethod, params, null, loop);
 	}
 
 	public InputStream requestData(String url, String httpMethod,String body, int loop) throws HttpUtilException, SSLException {
 		return requestData(url,httpMethod, null, body, loop);
 	}
 
 	public InputStream requestData(String url, String httpMethod, ArrayList<NameValuePair> params, String body, int loop) throws HttpUtilException, SSLException {
 
 		URI uri;
 
 		try {
 			uri = new URI(url);
 		} catch (URISyntaxException e) {
 			throw new HttpUtilException(1000,"Invalid URL: " + url);
 		}
 		HttpUriRequest method;
 
 		if (POST.equals(httpMethod)) {
 			HttpPost post = new HttpPost(uri);
 			if(body != null && !"".equals(body)) {
 				try {
 					post.setEntity(new StringEntity(body,HTTP.UTF_8));
 				} catch (UnsupportedEncodingException e) {
 					throw new HttpUtilException(1300,"UnsupportedEncodingException: " + e.getMessage());
 				}
 			} else if(params != null) {
 				try {
 					post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
 				} catch (UnsupportedEncodingException e) {
 					throw new HttpUtilException(1300,"UnsupportedEncodingException: " + e.getMessage());
 				}
 			}
 			method = post;
 		} else if (DELETE.equals(httpMethod)) {
 			method = new HttpDelete(uri);
 		} else {
 			method = new HttpGet(uri);
 		}
 
 		if (mHeaders != null) {
 			Iterator<String> headKeys = mHeaders.keySet().iterator();
 			while(headKeys.hasNext()) {
 				String key = headKeys.next();
 				method.setHeader(key, mHeaders.get(key));
 			}
 		}
 
 		if (consumer != null) {
 			try {
 				consumer.sign(method);
 				mClient.setRedirectHandler(new RedirectHandler() {
 
 					// Ignore
 					@Override
 					public boolean isRedirectRequested(HttpResponse response,
 							HttpContext context) {
 						return false;
 					}
 
 					@Override
 					public URI getLocationURI(HttpResponse response, HttpContext context)
 							throws ProtocolException {
 						return null;
 					}
 				});
 			} catch (OAuthMessageSignerException e) {
 				e.printStackTrace();
 			}catch (OAuthExpectationFailedException e) {
 				e.printStackTrace();
 			} catch (OAuthCommunicationException e) {
 				e.printStackTrace();
 			}
 		}
 
 		HttpResponse response;
 		try {
 			Header[] headers = method.getAllHeaders();
 			for (int i=0;i<headers.length;i++) {
 				Log.d("HttpUtil", headers[i].getName() + "\n\t" + headers[i].getValue());
 			}
 			response = mClient.execute(method);
 		} catch(SSLException e) {
 			throw e;
 		} catch (ClientProtocolException e) {
 			throw new HttpUtilException(800,"HTTP protocol error: " + e.getMessage());
 		} catch(IOException e) {
 			e.printStackTrace();
 			throw new HttpUtilException(800,e.getMessage());
 		}
 
 		int statusCode = response.getStatusLine().getStatusCode();
 		Log.d("HttpManager", url + " >> " + statusCode);
 		if (statusCode == 401) {
 			throw new HttpUtilException(401,"Unauthorized");
 		} else if (statusCode == 403 || statusCode == 406) {
 			try {
 				JSONObject json = null;
 				try {
 					json = new JSONObject(StreamUtil.toString(response.getEntity().getContent()));
 				} catch (JSONException e) {
 					throw new HttpUtilException(998,"Non json response: " + e.toString());
 				} catch (IOException e) {
 					throw new HttpUtilException(1200,"IOException: " + e.getMessage());
 				}
 				throw new HttpUtilException(statusCode, json.getString("error"));
 			} catch (IllegalStateException e) {
 				throw new HttpUtilException(1400,"IllegalStateException: " + e.getMessage());
 			} catch (JSONException e) {
 				throw new HttpUtilException(998,"Non json response: " + e.toString());
 			}
 		} else if (statusCode == 404) {
 			// User/Group or page not found
 			throw new HttpUtilException(404,"Not found: " + url);
 		} else if (statusCode == 400) {
 			// User/Group or page not found
 			try {
 				throw new HttpUtilException(400,StreamUtil.toString(response.getEntity().getContent()));
 			} catch (IOException e) {
 				throw new HttpUtilException(1200,"IOException: " + e.getMessage());
 			}
 
 		} else if ( (statusCode == 301 || statusCode == 302 || statusCode == 303)) {
 			//			Log.v("HttpManager", "Got : " + statusCode);
 			if(loop > 3) {
 				throw new HttpUtilException(statusCode,"Too many redirect: " + url);
 			}
 			Header hLocation = response.getLastHeader("Location");
 			if (hLocation != null) {
 				Log.v("HttpManager", "Got : " + hLocation.getValue());
 				return requestData(hLocation.getValue(), httpMethod, params, body, loop+1);
 			} else {
 				throw new HttpUtilException(statusCode,"redirect without location header: ");
 			}
 
 		} else if (statusCode != 200) {
 			throw new HttpUtilException(999,"Unmanaged response code: " + statusCode);
 		}
 
 		try {
 			InputStream is = response.getEntity().getContent();
 			return is;
 		} catch (IOException e) {
 			throw new HttpUtilException(1200,"IOException: " + e.getMessage());
 		}
 	}
 
 	private InputStream requestData(String url, ArrayList<NameValuePair> params, String attachmentParam, File attachment) 
 			throws HttpUtilException, SSLException {
 
 		URI uri;
 
 		try {
 			uri = new URI(url);
 		} catch (URISyntaxException e) {
 			throw new HttpUtilException(1000,"Invalid URL.");
 		}
 
 		HttpPost post = new HttpPost(uri);
 
 		HttpResponse response;
 
 		// create the multipart request and add the parts to it 
 		MultipartEntity requestContent = new MultipartEntity(); 
 		//		long len = attachment.length();
 		try {
 			InputStream ins = new FileInputStream(attachment);
 			InputStreamEntity ise = new InputStreamEntity(ins, -1L); 
 			byte[] data = EntityUtils.toByteArray(ise);
 
 			String IMAGE_MIME = attachment.getName().toLowerCase(Locale.US).endsWith("png") ? IMAGE_MIME_PNG : IMAGE_MIME_JPG;
 			requestContent.addPart(attachmentParam, new ByteArrayBody(data, IMAGE_MIME, attachment.getName()));
 
 			if (params != null) {
 				for (NameValuePair param : params) {
 					//					len += param.getValue().getBytes().length;
 					requestContent.addPart(param.getName(), new StringBody(param.getValue()));
 				}
 			}
 		} catch(IOException e) {
 			throw new HttpUtilException(1200,"IOException: " + e.getMessage());
 		}
 		post.setEntity(requestContent); 
 
 		if (mHeaders != null) {
 			Iterator<String> headKeys = mHeaders.keySet().iterator();
 			while(headKeys.hasNext()) {
 				String key = headKeys.next();
 				post.setHeader(key, mHeaders.get(key));
 			}
 		}
 		if (consumer != null) {
 			try {
 				consumer.sign(post);
 				mClient.setRedirectHandler(new RedirectHandler() {
 
 					// Ignore
 					@Override
 					public boolean isRedirectRequested(HttpResponse response,
 							HttpContext context) {
 						return false;
 					}
 
 					@Override
 					public URI getLocationURI(HttpResponse response, HttpContext context)
 							throws ProtocolException {
 						return null;
 					}
 				});
 			} catch (OAuthMessageSignerException e) {
 				e.printStackTrace();
 			}catch (OAuthExpectationFailedException e) {
 				e.printStackTrace();
 			} catch (OAuthCommunicationException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			mClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, DEFAULT_POST_REQUEST_TIMEOUT);
 			mClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, DEFAULT_POST_REQUEST_TIMEOUT);
 			response = mClient.execute(post);
 		} catch(SSLException e) { 
 			throw e;
 		} catch (ClientProtocolException e) {
 			throw new HttpUtilException(1500,"ClientProtocolException: " + e.getMessage());
 		} catch(IOException e) {
 			throw new HttpUtilException(1200,"IOException: " + e.getMessage());
 		}
 
 		int statusCode = response.getStatusLine().getStatusCode();
 
 
 		if (statusCode == 401) {
 			throw new HttpUtilException(401,"Unauthorized: " + url);
 		} else if (statusCode == 403 || statusCode == 406) {
 			try {
 				JSONObject json = null;
 				try {
 					json = new JSONObject(StreamUtil.toString(response.getEntity().getContent()));
 				} catch (JSONException e) {
 					throw new HttpUtilException(998,"Non json response: " + e.toString());
 				} catch(IOException e) {
 					throw new HttpUtilException(1200,"IOException: " + e.getMessage());
 				}
 				throw new HttpUtilException(statusCode, json.getString("error"));
 			} catch (IllegalStateException e) {
 				throw new HttpUtilException(1400,"IllegalStateException: " + e.getMessage());
 
 			} catch (JSONException e) {
 				throw new HttpUtilException(998,"Non json response: " + e.toString());
 			}
 		} else if (statusCode != 200) {
 			Log.e("Mustard", response.getStatusLine().getReasonPhrase());
 			throw new HttpUtilException(999,"Unmanaged response code: " + statusCode);
 		}
 
 		try {
 			InputStream is = response.getEntity().getContent();
 			return is;
 		} catch (IOException e) {
 			throw new HttpUtilException(1200,"IOException: " + e.getMessage());
 		}
 	}
 
 	public String getResponseAsString(String url)
 			throws HttpUtilException {
 		return getResponseAsString(url,GET,null);
 	}
 
 	public String getResponseAsString(String url, String httpMethod)
 			throws HttpUtilException {
 		return getResponseAsString(url,httpMethod,null);
 	}
 
 	public String getResponseAsString(String url, String httpMethod,
 			ArrayList<NameValuePair> params) throws HttpUtilException {
 		try {
 			String ret = StreamUtil.toString(requestData(url,httpMethod,params));
 			return ret;
 		} catch(IOException e) {
 			throw new HttpUtilException(1200,"IOException: " + e.getMessage());
 		}
 
 	}
 
 	public String getResponseAsString(String url, ArrayList<NameValuePair> params,String attachmentParam, File attachment) throws HttpUtilException {
 		try {
 			String ret = StreamUtil.toString(requestData(url,params,attachmentParam,attachment));
 			return ret;
 		} catch(IOException e) {
 			throw new HttpUtilException(1200,"IOException: " + e.getMessage());
 		}
 	}
 
 
 	public Document getDocument(String url, String httpMethod,ArrayList<NameValuePair> params) throws IOException, HttpUtilException {
 		Document  dom = null;
 		InputStream is = null;
 		try {
 			is = requestData(url,httpMethod,params);
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			dom = builder.parse(is);
 		} catch(ParserConfigurationException e) {
 			e.printStackTrace();
 			throw new HttpUtilException(980,"Parser exception: " + e.getMessage());
 		} catch(SAXException e) {
 			e.printStackTrace();
 			throw new HttpUtilException(981,"Parser exception: " + e.getMessage());
 		} finally {
 			if(is != null) {
 				try { is.close();} catch (Exception e){}
 			}
 		}
 		return dom;
 	}
 
 
 	private HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
 
 		public void process(
 				final HttpRequest request, 
 				final HttpContext context) throws HttpException, IOException {
 
 			AuthState authState = (AuthState) context.getAttribute(
 					ClientContext.TARGET_AUTH_STATE);
 			CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
 					ClientContext.CREDS_PROVIDER);
 			HttpHost targetHost = (HttpHost) context.getAttribute(
 					ExecutionContext.HTTP_TARGET_HOST);
 
 			// If not auth scheme has been initialized yet
 			if (authState.getAuthScheme() == null) {
 				AuthScope authScope = new AuthScope(
 						targetHost.getHostName(), 
 						targetHost.getPort());
 				// Obtain credentials matching the target host
 				Credentials creds = credsProvider.getCredentials(authScope);
 				// If found, generate BasicScheme preemptively
 				if (creds != null) {
 					authState.setAuthScheme(new BasicScheme());
 					authState.setCredentials(creds);
 				}
 			}
 		}
 
 	};
 
 	private  class ByteArrayBody extends AbstractContentBody {
 
 		private final byte[] bytes;
 		private final String fileName;
 
 		public ByteArrayBody(byte[] bytes, String mimeType, String fileName) {
 			super(mimeType);
 			this.bytes = bytes;
 			this.fileName = fileName;
 		}
 
 		public String getFilename() {
 			return fileName;
 		}
 
 		@Override
 		public void writeTo(OutputStream out) throws IOException {
 			out.write(bytes);
 		}
 
 		public String getCharset() {
 			return null;
 		}
 
 		public long getContentLength() {
 			return bytes.length;
 		}
 
 		public String getTransferEncoding() {
 			return MIME.ENC_BINARY;
 		}
 
 	}
 
 }
