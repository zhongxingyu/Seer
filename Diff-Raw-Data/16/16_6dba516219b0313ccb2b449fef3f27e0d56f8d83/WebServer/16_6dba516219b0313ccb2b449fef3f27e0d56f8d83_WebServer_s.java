 package com.mss.infrastructure.web;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.lang.reflect.Type;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntityBuilder;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONException;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParseException;
 import com.mss.infrastructure.web.dtos.ISO8601Utils;
 
 public class WebServer {
 	
 	private String address;
 	
 	public WebServer(String address) {		
 		this.address = address;
 	}
 	
 	private CookieStore cookieStore;
 	private HttpContext httpContext;
 	
 	public WebConnection connect(String login, String password) throws WebConnectionException, AuthenticationFailedException{		
 		cookieStore = new BasicCookieStore();
 		httpContext = new BasicHttpContext();
 		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
 		
 		webConnection = new WebConnection(this);			
 		try {
 			
 			get("/users/sign_in", new ArrayList<NameValuePair>());
 						
 			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();			
 			nameValuePairs.add(new BasicNameValuePair("user[username]", login));
 			nameValuePairs.add(new BasicNameValuePair("user[password]", password));
 			nameValuePairs.add(new BasicNameValuePair("user[remember_me]", "0"));
 			nameValuePairs.add(new BasicNameValuePair("commit", "Sign in"));
 			
 			int status = post("/users/sign_in", nameValuePairs).getStatusCode();
 			System.out.print("Logon status: " + status + '\n');
 			if (status != 302)
 				throw new AuthenticationFailedException();
 			
 			return webConnection;
 		} catch (JSONException e) { 
 			e.printStackTrace(); 
 		} catch (IOException e) { 
 			e.printStackTrace(); 
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		throw new WebConnectionException();
 	}
 	
 	public Date getTime() throws Exception{
         String json = get("/synchronization/datetime.json", new ArrayList<NameValuePair>());
         
         
         JsonDeserializer<Date> jsonDateDeserializer = new JsonDeserializer<Date>() {
         	public Date deserialize(JsonElement json, Type typeOfT,
         		JsonDeserializationContext context) throws JsonParseException {
         		return ISO8601Utils.parse(json.getAsString());
 			}
         };
         Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, jsonDateDeserializer).create();
         
 		return gson.fromJson(json, ServerTime.class).getTime();
 	} 
 	
 	public String get(String url, List<NameValuePair> params) throws URISyntaxException, Exception {
 		String getUrl = address + url;
 		if (params.size() > 0) {
 			getUrl += '?';
 		}
 		
 		String getParamsEncoded = URLEncodedUtils.format(params, "utf-8");
 		getUrl += getParamsEncoded;
 		
 		HttpGet httpGet = new HttpGet(getUrl);
 		httpGet.addHeader("User-Agent", "MSS.Android mobile client");
 						
 		HttpResponse response = Dispatch(httpGet);
 		String content = Parse(response);
 		String csrfToken = extractCsrfToken(content);
 		if (csrfToken != "") {
 			getCurrentConnection().setCsrf(csrfToken);
 			System.out.print("Get csrf token: " + csrfToken + '\n');
 		}
 				
 		return content;
 	}
 	
 	public PostResult post(String url, List<NameValuePair> params) throws JSONException, Exception {
 		params.add(new BasicNameValuePair("authenticity_token", getCurrentConnection().getCsrf()));
 		
 		HttpPost httpPost = new HttpPost(address + url);
 		httpPost.addHeader("User-Agent", "MSS.Android mobile client");
 		httpPost.addHeader("X-CSRF-Token", getCurrentConnection().getCsrf());
 		httpPost.setEntity(new UrlEncodedFormEntity(ParametersEscape(params), "UTF-8"));		
 		
 		HttpResponse response = Dispatch(httpPost);
 		String content = Parse(response);
 		String csrfToken = extractCsrfToken(content);
 		if (csrfToken != "") {
 			getCurrentConnection().setCsrf(csrfToken);
 			System.out.print("Get csrf token: " + csrfToken + '\n');
 		}
 		
 		return new PostResult(response.getStatusLine().getStatusCode(), content);
 	}
 	
 	
 	public PostResult post(String url, List<NameValuePair> params, List<NameValuePair> attachments) 
 			throws JSONException, Exception {
 		params.add(new BasicNameValuePair("authenticity_token", getCurrentConnection().getCsrf()));
 		
 		HttpPost httpPost = new HttpPost(address + url);
 		httpPost.addHeader("User-Agent", "MSS.Android mobile client");
 		httpPost.addHeader("X-CSRF-Token", getCurrentConnection().getCsrf());
 
 		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
 		
 		List<NameValuePair> escapedParams = ParametersEscape(params);
         for(int index=0; index < escapedParams.size(); index++) {
        	multipartEntityBuilder.addTextBody(escapedParams.get(index).getName(), 
        			escapedParams.get(index).getValue());
        }
                 
         for(int index=0; index < attachments.size(); index++) {
         	File file = new File (attachments.get(index).getValue());
         	multipartEntityBuilder.addBinaryBody(attachments.get(index).getName(), 
         			file,
         			ContentType.MULTIPART_FORM_DATA, 
         			file.getName());
         }

        multipartEntityBuilder.setCharset(Charset.defaultCharset());
 		httpPost.setEntity(multipartEntityBuilder.build());
 		
 		HttpResponse response = Dispatch(httpPost);
 		String content = Parse(response);
 		String csrfToken = extractCsrfToken(content);
 		if (csrfToken != "") {
 			getCurrentConnection().setCsrf(csrfToken);
 			System.out.print("Get csrf token: " + csrfToken + '\n');
 		}
 		
 		return new PostResult(response.getStatusLine().getStatusCode(), content);
 	}
 	
 	public void download(String url, String destination) throws URISyntaxException, Exception {
 		String downloadUrl = address + "/" + url;
 		
 		HttpGet httpGet = new HttpGet(downloadUrl);
 		httpGet.addHeader("User-Agent", "MSS.Android mobile client");
 		HttpResponse response = Dispatch(httpGet);				
 		
 		if (response.getStatusLine().getStatusCode() == 200) {
 			HttpEntity entity = response.getEntity();
 			if (entity != null) {
 			    InputStream inputStream = entity.getContent();
 			    OutputStream f= new FileOutputStream(destination);
 		        byte[] buffer=new byte[4096];
 		        int len=0;
 
 		        while ((len=inputStream.read(buffer)) > 0) {
 		          f.write(buffer, 0, len);
 		        }
 		        
 		        f.close();
 		        inputStream.close();
 			}				        
 	    }
 	    else {
 	      throw new RuntimeException(String.format("Received %d from server",
 	    		  response.getStatusLine().getStatusCode()));
 	    }
 	}
 	
 	private List<NameValuePair> ParametersEscape(List<NameValuePair> params) {
 		ArrayList<NameValuePair> escapedParameters = new ArrayList<NameValuePair>();
 		for (NameValuePair nameValuePair : params) {
 			escapedParameters.add(new BasicNameValuePair(
 					nameValuePair.getName(), 
 					EscapeChars.forJSON(nameValuePair.getValue())));
 		}
 		
 		return escapedParameters;
 	}
 	
 	private HttpResponse Dispatch(HttpUriRequest request) throws IOException {
 		HttpClient httpClient = new DefaultHttpClient();
 		httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
 		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 60000);
 		HttpConnectionParams.setSoTimeout(httpClient.getParams(), 60000);
 		
 		HttpResponse response = httpClient.execute(request, httpContext);
 		
 		return response;
 	}
 	
 	private String Parse(HttpResponse response) throws IllegalStateException, IOException {
 		InputStream stream = response.getEntity().getContent();
 		BufferedReader getOutputBuffer = new BufferedReader(new InputStreamReader((stream)));
 		
 		String line;
 		StringBuffer output = new StringBuffer("");
 		while ((line = getOutputBuffer.readLine()) != null) {
 			output.append(line);
 		}
 		
 		return output.toString();
 	}
 	
 	private String extractCsrfToken(String html){
 		String csrfToken = "";
 		
 		try {
 			final Pattern pattern = Pattern.compile("<input name=\"authenticity_token\" type=\"hidden\" value=\"(.+?)\" />");
 			final Matcher matcher = pattern.matcher(html);
 			if (matcher.find()) {
 				csrfToken = matcher.group(1); 
 			}
 		} catch (Exception e) {
 			e.printStackTrace(); 
 		}
 		
 		return csrfToken;
 	}
 	
 	private WebConnection webConnection;
 	public WebConnection getCurrentConnection(){
 		return webConnection;
 	}
 	
 	public class PostResult {
 		public PostResult(int statusCode, String content) {
 			this.statusCode = statusCode;
 			this.content = content;
 		}
 		
 		private int statusCode;
 		
 		public int getStatusCode(){
 			return statusCode;
 		}
 		
 		private String content;
 		
 		public String getContent(){
 			return content;
 		}
 	}
 
 	class ServerTime {
 		private Date datetime;
 		
 		public Date getTime(){
 			return datetime;
 		}
 	}
 }
