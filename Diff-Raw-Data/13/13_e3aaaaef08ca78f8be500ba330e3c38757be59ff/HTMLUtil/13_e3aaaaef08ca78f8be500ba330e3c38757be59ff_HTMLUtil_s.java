 package com.blastedstudios.crittercaptors.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.utils.GdxRuntimeException;
 
 public class HTMLUtil {
 	private static String getString(InputStream stream){
 		String line, result = "";
 		BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
 		try {
 			while ((line = rd.readLine()) != null) 
 				result += line;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	/*public static String getHTML(String urlToRead) {
 		try {
 			java.net.HttpURLConnection conn = (HttpURLConnection) new URL(urlToRead).openConnection();
 			conn.setRequestMethod("GET");
 			return getString(conn.getInputStream());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return "";
 	}*/
 
 	/**
 	 * Apaches method, handles redirects etc.
 	 * @param urlToRead 
 	 * "http://gisdata.usgs.gov/"
 	            + "xmlwebservices2/elevation_service.asmx/"
 	            + "getElevation?X_Value=" + String.valueOf(longitude)
 	            + "&Y_Value=" + String.valueOf(latitude)
 	            + "&Elevation_Units=METERS&Source_Layer=-1&Elevation_Only=true"
 	 * @return http content of urlToRead
 	 */
 	
 	public static String getHTML(String urlToRead) {
 		String result = "";
 	    HttpClient httpClient = new DefaultHttpClient();
 	    HttpContext localContext = new BasicHttpContext();
 	    HttpGet httpGet = new HttpGet(urlToRead);
 	    try {
 	        HttpResponse response = httpClient.execute(httpGet, localContext);
 	        HttpEntity entity = response.getEntity();
 	        if (entity != null)
 	            return getString(entity.getContent());
	    } catch (ClientProtocolException e) {
 	    	e.printStackTrace();
 	    } 
	    catch (IOException e) {}
 	    return result;
 	}
 
 	public static class URLHandle extends FileHandle {
 		final URL url; 
 
 		public URLHandle(URL url) {
 			try {
 				this.url = url;
 			} catch(Exception e) {
 				throw new GdxRuntimeException("Couldn't create URLHandle for '" + url + "'", e);
 			}
 		}
 
 		@Override
 		public FileHandle child(String name) {
 			return null;
 		}
 
 		@Override
 		public FileHandle parent() {
 			return null;
 		}               
 
 		public InputStream read () {
 			try {
 				return url.openStream();
 			} catch (IOException e) {
 				throw new GdxRuntimeException("Couldn't read URL '" + url.toString() + "'");
 			}
 		}
 	}
 }
