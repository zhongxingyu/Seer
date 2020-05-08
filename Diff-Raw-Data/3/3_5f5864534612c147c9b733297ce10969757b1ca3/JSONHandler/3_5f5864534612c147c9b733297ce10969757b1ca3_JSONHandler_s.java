 package com.mcprohosting.plugins.donations;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import java.io.*;
 import java.net.URL;
 import java.nio.charset.Charset;
 
 public class JSONHandler {
 	public static JSONObject readJsonFromUrl(String url) {
 		InputStream inputStream = null;
 
 		try {
 			inputStream = new URL(url).openStream();
 
 			BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
 			String parsedText = readAll(rd);
 			
 			JSONObject json = (JSONObject) new JSONParser().parse(parsedText);
 			return json;
 		} catch (IOException e) {
			Donations.getPlugin().getLogger().severe("Failed to get data from " + url);
			e.printStackTrace();
 		} catch (ParseException e) {
 			Donations.getPlugin().getLogger().severe("Failed to process data from " + url);
 			e.printStackTrace();
 		} finally {
 			try {
 				inputStream.close();
 			} catch (IOException e) {
 				Donations.getPlugin().getLogger().severe("Something has gone very wrong.");
 				e.printStackTrace();
 			}
 		}
 
 		return null;
 	}
 
 	public static boolean verifyURL(String url) {
 		try {
 			readJsonFromUrl(url);
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	private static String readAll(Reader rd) throws IOException {
 		StringBuilder sb = new StringBuilder();
 		int cp;
 		while ((cp = rd.read()) != -1) {
 			sb.append((char) cp);
 		}
 		
 		return sb.toString();
 	}
 }
