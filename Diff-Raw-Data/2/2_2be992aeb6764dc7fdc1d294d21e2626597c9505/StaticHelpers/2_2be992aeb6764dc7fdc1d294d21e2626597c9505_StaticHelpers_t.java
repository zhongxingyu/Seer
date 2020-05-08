 package de.hpi.InformationSpreading;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class StaticHelpers {
 	
 	public static String getContentFromUrl(String urlString) throws Exception {
 		InputStream inputStream = null;
 		String line;
 		StringBuilder httpContent = new StringBuilder();
 		URL url;
 		try {
 			url = new URL(urlString);
 			inputStream = url.openStream(); // throws an IOException
 			DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(
 					inputStream));
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
					dataInputStream, "UTF-8"));
 			while ((line = reader.readLine()) != null){
 				httpContent.append(line);
 				httpContent.append("\n");
 			}
 		} catch (MalformedURLException e) {
 			throw e;
 		} catch (UnsupportedEncodingException e) {
 			throw e;
 		} catch (IOException e) {
 			throw e;
 		} finally {
 			try {
 				inputStream.close();
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			} catch (NullPointerException e){
 				e.printStackTrace();
 			}
 		}
 		return httpContent.toString();
 	}
 
 }
