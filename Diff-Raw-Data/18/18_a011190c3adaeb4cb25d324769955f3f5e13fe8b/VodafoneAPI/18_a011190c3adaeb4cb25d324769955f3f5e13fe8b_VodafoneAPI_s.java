 package com.vaguehope.loctxt.vodafone;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import org.eclipse.jetty.util.log.Log;
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.builder.api.Api20;
 import org.scribe.model.Request;
 import org.scribe.model.Response;
 import org.scribe.model.Verb;
 import org.scribe.utils.Preconditions;
 import org.scribe.utils.URLUtils;
 
 public class VodafoneAPI implements Api20
 {
 	private final String HOST = "http://api.developer.vodafone.com";
 
 	@Override
 	public String getAuthorizationUrl (ServiceBuilder service)
 	{
 		final String AUTHORIZE_URL = HOST + "/2/oauth/authorize?" +
 				"client_id=%s&" +
 				"redirect_uri=%s&" +
 				"response_type=%s&" +
 				"scope=%s";
 		Preconditions.checkValidUrl(service.getCallback(), "Must provide a valid url as callback.");
 		String authCodeURL = String.format(AUTHORIZE_URL, service.getApiKey(), URLUtils.urlEncodeWrapper(service.getCallback()),
 				service.getResponseType(), service.getScope());
 
 		InputStream is = null;
 		try {
 			URL resourceUrl = new URL(authCodeURL);
 			HttpURLConnection conn = (HttpURLConnection) resourceUrl.openConnection();
 			conn.setConnectTimeout(60000);
 			conn.setReadTimeout(60000);
 			conn.connect();
 			is = conn.getInputStream();
 			String res = conn.getURL().toString();
 			if (res != null)
 				return res;
 		}
 		catch (Exception e) {
 			System.out.println("error happened: " + e.toString());
 		}
 		finally {
 			if (is != null)
 				try {
 					is.close();
 				}
 				catch (IOException e) {
 					e.printStackTrace();
 				}
 		}
 
 		return null;
 	}
 
 	@Override
 	public String getAccessTokenUrl (ServiceBuilder service, String authCodeToken) {
 		final String AUTHORIZE_URL = HOST + "/2/oauth/access_token?client_id=%s";
 		return String.format(AUTHORIZE_URL, service.getApiKey());
 	}
 
	public static void sendSms (String fromNumber, String toNumber, String msg) {
		String PROTECTED_RESOURCE_URL = "http://api.developer.vodafone.com/v2/smsmessaging/outbound/tel:{fn}/requests";
		String url = PROTECTED_RESOURCE_URL.replace("{fn}", fromNumber);
 
 		String vkey = System.getenv("vkey");
 		if (vkey == null) throw new IllegalStateException("vkey not set.");
 
 		try {
			Request req = new Request(Verb.POST, url);
 			req.addBodyParameter("message", msg);
 			req.addBodyParameter("address", URLEncoder.encode("tel:" + toNumber));
 			req.addBodyParameter("key", vkey);
 			Response response = req.send();
 			if (response.getCode() != 200) throw new RuntimeException(response.getCode() + " " + response.getBody());
			Log.info("Send sms from " + fromNumber + " to " + toNumber);
 		}
 		catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			throw new RuntimeException(e);
 		}
 	}
 
 }
