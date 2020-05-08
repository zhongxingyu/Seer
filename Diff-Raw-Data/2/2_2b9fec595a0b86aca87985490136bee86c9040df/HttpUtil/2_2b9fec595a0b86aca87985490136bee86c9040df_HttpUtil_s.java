 package com.kniffenwebdesign.roku.ecp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.HTTP;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.ParseException;
 import android.util.Log;
 
 public class HttpUtil {
 	private static final String LOG_TAG = "HttpUtil";
 
 	public static String request(String url, String method){
 		HttpClient client = new DefaultHttpClient();
 		HttpUriRequest request = null;
 	
 		if(method.equals("GET")){
 			request = new HttpGet(url);
 		} else {
 			request = new HttpPost(url);
 		}
 		
		String responseText = null;
 		try {
 			HttpResponse response = client.execute(request);
 			responseText = HttpUtil.getResponseBody(response);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return responseText;
 	}
 	
 	protected static String _getResponseBody(final HttpEntity entity) throws IOException, ParseException {
 		if (entity == null) {
 			throw new IllegalArgumentException("HTTP entity may not be null");
 		}
 
 		InputStream instream = entity.getContent();
 
 		if (instream == null) {
 			return "";
 		}
 
 		if (entity.getContentLength() > Integer.MAX_VALUE) {
 			throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
 		}
 
 		String charset = getContentCharSet(entity);
 
 		if (charset == null) {
 			charset = HTTP.DEFAULT_CONTENT_CHARSET;
 		}
 
 		Reader reader = new InputStreamReader(instream, charset);
 		StringBuilder buffer = new StringBuilder();
 
 		try {
 			char[] tmp = new char[1024];
 			int l;
 
 			while ((l = reader.read(tmp)) != -1) {
 				buffer.append(tmp, 0, l);
 			}
 		} finally {
 			reader.close();
 		}
 		return buffer.toString();
 	}
 
 	public static String getContentCharSet(final HttpEntity entity)
 			throws ParseException {
 
 		if (entity == null) {
 			throw new IllegalArgumentException("HTTP entity may not be null");
 		}
 
 		String charset = null;
 		if (entity.getContentType() != null) {
 			HeaderElement values[] = entity.getContentType().getElements();
 			if (values.length > 0) {
 				NameValuePair param = values[0].getParameterByName("charset");
 				if (param != null) {
 					charset = param.getValue();
 				}
 			}
 		}
 		return charset;
 	}
 
 	public static String getResponseBody(HttpResponse response) {
 		String response_text = null;
 		HttpEntity entity = null;
 		try {
 			entity = response.getEntity();
 			response_text = _getResponseBody(entity);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			if (entity != null) {
 				try {
 					entity.consumeContent();
 				} catch (IOException e1) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return response_text;
 	}
 	
 	public static Bitmap drawableFromUrl(String url) throws java.net.MalformedURLException, java.io.IOException {
 		Bitmap bitmap;
 		
 		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
 		
 		connection.connect();
 		InputStream input = connection.getInputStream();
 		
 		bitmap = BitmapFactory.decodeStream(input);
 		return bitmap;
 	}
 }
