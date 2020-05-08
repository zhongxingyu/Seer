 package com.dropbox.client;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 public class Util {
 
 	public static final String LOGIN_URL = "https://www.dropbox.com/login";
 	public static final String AUTH_URL = "https://www.dropbox.com/0/oauth/authorize";
 
 	public static void authorizeDropbox(String requestTokenUrl, String username,
 			String password, String token) throws Exception {
 		log("authorizeDropbox: "+requestTokenUrl);
 
 		DefaultHttpClient client = new DefaultHttpClient();
 		HttpGet tokenget = new HttpGet(requestTokenUrl);
 		HttpResponse response = client.execute(tokenget);
 
 		int statusCode = response.getStatusLine().getStatusCode();
 		log("token get statusCode: "+statusCode);
 
 		InputStream is = response.getEntity().getContent();
 		String res = readStream(is);
 		if(!res.contains("Log in")){
 			//TODO
 			log("already authorized?");
 		}
 
 		String conturl = AUTH_URL + "?oauth_token=" + token
 				+ "&amp;oauth_callback=";
 
 		HttpPost post = new HttpPost(LOGIN_URL);
 		
 		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
//		pairs.add(new BasicNameValuePair("t", "791206fc33"));
 		pairs.add(new BasicNameValuePair("cont", conturl));
 //		pairs.add(new BasicNameValuePair("cont", requestTokenUrl));
 		pairs.add(new BasicNameValuePair("login_email", username));
 		pairs.add(new BasicNameValuePair("login_password", password));
 		post.setEntity(new UrlEncodedFormEntity(pairs));
 		response = client.execute(post);
 		statusCode = response.getStatusLine().getStatusCode();
 		log("post statusCode: "+statusCode);
 
 		is = response.getEntity().getContent();
 		res = readStream(is);
 		if(!res.contains("you should be redirected automatically")){
 			//TODO verify
 			log("no redirect?");
 		}
 
         if(statusCode == 200){
         	log("WARN: no redirect");
         }else if(statusCode == 302){
 	        Header locationHeader = response.getFirstHeader("location");
 	        if (locationHeader != null) {
 	        	String redirect = locationHeader.getValue();
 	        	redirect = redirect.replace("&amp;", "&");
 	        	log("redirect: "+redirect);
 
 	        	HttpGet get = new HttpGet(redirect);
 	        	response = client.execute(get);
 
 	    		statusCode = response.getStatusLine().getStatusCode();
 	    		log("statusCode: "+statusCode);
 
 	    		is = response.getEntity().getContent();
 	    		res = readStream(is);
 	    		if(!res.contains("Allow") || !res.contains("Deny")){
 	    			//TODO no need to allow?
 	    			log("already allowed?");
 	    		}
 	        }
         }
 		//allow
 		log("auth url: "+AUTH_URL);
 		pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("t", "0015b2efc8"));
 		pairs.add(new BasicNameValuePair("oauth_token", token));
 		pairs.add(new BasicNameValuePair("access_option", "Allow"));
 
 		HttpPost allowpost = new HttpPost(AUTH_URL);
 		allowpost.setEntity(new UrlEncodedFormEntity(pairs));
 
 		response = client.execute(allowpost);
 
 		statusCode = response.getStatusLine().getStatusCode();
 		log("statusCode: "+statusCode);
 
 		is = response.getEntity().getContent();
 		res = readStream(is);
 		if(!res.contains("Success!")){
 			log("Unauthorized?");
 		}else{
 			log("Authorized!");
 		}
 	}
 
 	public static void log(String msg) {
 		System.out.println(msg);
 	}
 
 	public static String readStream(InputStream is) {
 		if (is == null) {
 			return null;
 		}
 		try {
 			int c;
 			byte[] buffer = new byte[8192];
 			StringBuilder sb = new StringBuilder();
 			while ((c = is.read(buffer)) != -1) {
 				sb.append(new String(buffer, 0, c));
 			}
 			return sb.toString();
 		} catch (Throwable e) {
 			e.printStackTrace();
 		} finally {
 			closeStream(is);
 		}
 		return null;
 	}
 
 	public static void closeStream(Closeable stream) {
 		if (stream != null) {
 			try {
 				stream.close();
 				stream = null;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private Util() {}
 
 }
