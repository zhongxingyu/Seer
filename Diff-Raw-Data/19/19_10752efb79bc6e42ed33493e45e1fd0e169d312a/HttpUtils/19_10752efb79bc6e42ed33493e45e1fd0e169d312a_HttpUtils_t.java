 package com.cffreedom.utils.net;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.KeyStore;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 import javax.security.cert.CertificateException;
 import javax.security.cert.X509Certificate;
 
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 
 import com.cffreedom.beans.EmailMessage;
 import com.cffreedom.exceptions.GeneralException;
 import com.cffreedom.utils.LoggerUtil;
 import com.cffreedom.utils.SystemUtils;
 
 /**
  * @author markjacobsen.net (http://mjg2.net/code)
  * Copyright: Communication Freedom, LLC - http://www.communicationfreedom.com
  * 
  * Free to use, modify, redistribute.  Must keep full class header including 
  * copyright and note your modifications.
  * 
  * If this helped you out or saved you time, please consider...
  * 1) Donating: http://www.communicationfreedom.com/go/donate/
  * 2) Shoutout on twitter: @MarkJacobsen or @cffreedom
  * 3) Linking to: http://visit.markjacobsen.net
  * 
  * Changes:
  * 2013-04-24 	markjacobsen.net 	Moved from com.cffreedom.utils to com.cffreedom.net package
  * 2013-04-24 	markjacobsen.net 	Added optional setupProxy param to httpGet() and created httpPost()
  * 2013-05-07 	markjacobsen.net 	Added getMailtoLink()
 * 2013-05-09 	markjacobsen.net 	Fixed encoding of params containing an ampersand in getMailtoLink()
  */
 public class HttpUtils
 {
 	public static String buildUrl(String urlStr, Map<String, String> queryParams)
 	{
 		try
 		{
 			if ((queryParams != null) && (queryParams.size() > 0))
 			{
 				String queryStr = encodeParams(queryParams);
 				urlStr += "?" + queryStr;
 			}
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			e.printStackTrace();
 			urlStr = null;
 		}
 		return urlStr;
 	}
 	
 	public static String httpGet(String urlStr) throws IOException { return httpGet(urlStr, null); }
 	public static String httpGet(String urlStr, Map<String, String> queryParams) throws IOException { return httpGet(urlStr, queryParams, true); }
 	public static String httpGet(String urlStr, Map<String, String> queryParams, boolean setupProxy) throws IOException
 	{
 		final String METHOD = "httpGet";
 		urlStr = buildUrl(urlStr, queryParams);
 		
 		if (setupProxy == true) { setupProxy(); }
 		
 		URL url = new URL(urlStr);
 		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 
 		if (conn.getResponseCode() != 200) {
 			throw new IOException(conn.getResponseMessage());
 		}
 
 		// Buffer the result into a string
 		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		StringBuilder sb = new StringBuilder();
 		String line;
 		while ((line = rd.readLine()) != null) {
 			sb.append(line);
 		}
 		rd.close();
 
 		conn.disconnect();
 		return sb.toString();
 	}
 
 	public static String httpGetWithReqProp(String urlStr, String reqPropKey, String reqPropVal) throws IOException
 	{
 		URL url = new URL(urlStr);
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 		conn.setRequestProperty(reqPropKey, reqPropVal);
 
 		if (conn.getResponseCode() != 200) {
 			throw new IOException(conn.getResponseMessage());
 		}
 
 		// Buffer the result into a string
 		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		StringBuilder sb = new StringBuilder();
 		String line;
 		while ((line = rd.readLine()) != null) {
 			sb.append(line);
 		}
 		rd.close();
 
 		conn.disconnect();
 		return sb.toString();
 	}
 	
 	public static String httpPost(String urlStr, Map<String, String> queryParams) throws GeneralException, IOException { return httpPost(urlStr, queryParams, true); }
 	public static String httpPost(String urlStr, Map<String, String> queryParams, boolean setupProxy) throws GeneralException, IOException
 	{
 		final String METHOD = "httpPost";
 		int responseCode = 0;
 		StringBuilder response = new StringBuilder();
 		String urlParameters = encodeParams(queryParams);
 		
 		if (setupProxy == true) { setupProxy(); }
 		
 		URL url = new URL(urlStr);
 		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
 		conn.setDoOutput(true);
 		conn.setDoInput(true);
 		conn.setInstanceFollowRedirects(false); 
 		conn.setRequestMethod("POST"); 
 		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
 		conn.setRequestProperty("charset", "utf-8");
 		conn.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
 		conn.setUseCaches(false);
 
 		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
 		wr.writeBytes(urlParameters);
 		wr.flush();
 		wr.close();
 
 		responseCode = conn.getResponseCode();
 		
 		if ((responseCode == 200) || (responseCode == 301) || (responseCode == 302))
 		{
 			// Buffer the result into a string
 			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String line;
 			while ((line = rd.readLine()) != null) {
 				response.append(line);
 			}
 			rd.close();
 		}
 		
 		conn.disconnect();
 		
 		if (responseCode != 200) {
 			throw new GeneralException(METHOD, "Bad Response Code: " + responseCode);
 		}
 		return response.toString();
 	}
 	
 	public static String encodeParams(Map<String, String> params) throws UnsupportedEncodingException
 	{
 		String ret = "";
 		for (Map.Entry<String, String> entry : params.entrySet())
 		{
 			ret += entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8") + "&";
 		}
 		if ((ret.length() > 0) && (ret.charAt(ret.length() - 1) == '&'))
 		{
 			// Strip the last ampersand
 			ret = ret.substring(0, ret.length() - 1);
 		}
 		return ret;
 	}
 	
 	public static void setupProxy()
 	{
 		final String METHOD = "setupProxy";
 		
 		if (System.getProperties().get("http.proxyHost") != null)
 		{
 			LoggerUtil.log(METHOD, "Proxy already setup via system properties");
 		}
 		else
 		{
 			String proxy = null;
 			String envVar = null;
 			if ((envVar == null) && (SystemUtils.getEnvVal("HTTP_PROXY") != null)) { envVar = "HTTP_PROXY"; }
 			if ((envVar == null) && (SystemUtils.getEnvVal("HTTPS_PROXY") != null)) { envVar = "HTTPS_PROXY"; }
 			if ((envVar == null) && (SystemUtils.getEnvVal("http_proxy") != null)) { envVar = "http_proxy"; }
 			if ((envVar == null) && (SystemUtils.getEnvVal("https_proxy") != null)) { envVar = "https_proxy"; }
 			
 			if (envVar != null)
 			{
 				LoggerUtil.log(METHOD, "Setting up proxy from env var = " + envVar);
 				proxy = SystemUtils.getEnvVal(envVar);
 				String[] parts = proxy.split("@");
 				if (parts.length == 1)
 				{
 					String[] serverParts = parts[0].split(":");
 					
 					System.getProperties().put("http.proxyHost", serverParts[0]);
 					System.getProperties().put("https.proxyHost", serverParts[0]);
 					
 					if (serverParts.length > 1)
 					{
 						System.getProperties().put("http.proxyPort", serverParts[1]);
 						System.getProperties().put("https.proxyPort", serverParts[1]);
 					}
 				}
 				else if (parts.length == 2)
 				{
 					String[] userParts = parts[0].replace("http://", "").split(":");
 					String[] serverParts = parts[1].split(":");
 					
 					System.getProperties().put("http.proxyUser", userParts[0]);
 					System.getProperties().put("https.proxyUser", userParts[0]);
 					
 					if (userParts.length > 1)
 					{
 						System.getProperties().put("http.proxyPassword", userParts[1]);
 						System.getProperties().put("https.proxyPassword", userParts[1]);
 					}
 					
 					System.getProperties().put("http.proxyHost", serverParts[0]);
 					System.getProperties().put("https.proxyHost", serverParts[0]);
 					
 					if (serverParts.length > 1)
 					{
 						System.getProperties().put("http.proxyPort", serverParts[1]);
 						System.getProperties().put("https.proxyPort", serverParts[1]);
 					}
 				}
 				
 				LoggerUtil.log(LoggerUtil.LEVEL_DEBUG, METHOD, "http.proxyHost = " + System.getProperties().get("http.proxyHost"));
 				LoggerUtil.log(LoggerUtil.LEVEL_DEBUG, METHOD, "http.proxyPort = " + System.getProperties().get("http.proxyPort"));
 				LoggerUtil.log(LoggerUtil.LEVEL_DEBUG, METHOD, "http.proxyUser = " + System.getProperties().get("http.proxyUser"));
 				//LoggerUtil.log(METHOD, "http.proxyPassword = " + System.getProperties().get("http.proxyPassword"));
 				
 				LoggerUtil.log(LoggerUtil.LEVEL_DEBUG, METHOD, "https.proxyHost = " + System.getProperties().get("https.proxyHost"));
 				LoggerUtil.log(LoggerUtil.LEVEL_DEBUG, METHOD, "https.proxyPort = " + System.getProperties().get("https.proxyPort"));
 				LoggerUtil.log(LoggerUtil.LEVEL_DEBUG, METHOD, "https.proxyUser = " + System.getProperties().get("https.proxyUser"));
 				//LoggerUtil.log(METHOD, "https.proxyPassword = " + System.getProperties().get("https.proxyPassword"));
 			}
 		}
 	}
 	
 	public static String getMailtoLink(String to, String linkText) { return getMailtoLink(to, linkText, null); }
 	public static String getMailtoLink(String to, String linkText, String subject) { return getMailtoLink(to, linkText, subject, ""); }
 	public static String getMailtoLink(String to, String linkText, String subject, String body) { return getMailtoLink(to, linkText, subject, body, null); }
 	public static String getMailtoLink(String to, String linkText, String subject, String body, String cc) { return getMailtoLink(to, linkText, subject, body, cc, null); }
 	public static String getMailtoLink(String to, String linkText, String subject, String body, String cc, String bcc)
 	{
 		try
 		{
			String qs = "";
			if ((cc != null) && (cc.trim().length() > 0)){ qs += "cc="+cc+"&"; }
			if ((bcc != null) && (bcc.trim().length() > 0)){ qs += "bcc="+bcc+"&"; }
			if ((subject != null) && (subject.trim().length() > 0)){ qs += "subject="+subject.replace("&", "%26")+"&"; }
			if ((body != null) && (body.trim().length() > 0)){ qs += "body="+body.replace("&", "%26")+"&"; }
			
 			return "<a href=\"mailto:"+to+"&"+qs+"\">"+linkText+"</a>";
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static String getMailtoLink(String to, String linkText, String subject, String[] body)
 	{
 		String newBody = "";
 		for (int x = 0; x < body.length; x++)
 		{
 			newBody += body[x] + "%0A";
 		}
 		return getMailtoLink(to, linkText, subject, newBody);
 	}
 	
 	public static String getMailtoLink(EmailMessage email, String linkText)
 	{
 		return getMailtoLink(email.getTo(), linkText, email.getSubject(), email.getBody(), email.getCc(), email.getBcc());
 	}
 }
