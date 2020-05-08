 package com.Zolli.EnderCore.Utils;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.channels.ReadableByteChannel;
 
 import com.Zolli.EnderCore.Logger.simpleLogger;
 import com.Zolli.EnderCore.Logger.simpleLogger.Level;
 
 public class networkUtils {
 	
 	protected ReadableByteChannel rbc;
 	
 	/**
 	 * Download The file at given url, and save to specified location
 	 * @param Url The url from file is downloaded
 	 * @param saveLocation The location on the file is saved
 	 * @return int The download rate
 	 */
	public static int downloadAndSave(String Url, String saveLocation, simpleLogger logger) throws Exception {
 		BufferedInputStream in = null;
 		BufferedOutputStream out = null;
 		File fl = new File(saveLocation);
 		int transferRate = 0;
 		
 		if(!fl.exists()) {
 			logger.log(Level.INFO, "Started downloading the following file: " + Url);
 			fl.getParentFile().mkdirs();
 			byte[] buffer = new byte[1024];
 			int byteRead = 0;
 			
 			try {
 				URL url = new URL(Url);
 				URLConnection conn = url.openConnection();
 				conn.connect();
 				int fileLength = conn.getContentLength();
 				in = new BufferedInputStream(url.openStream());
 				out = new BufferedOutputStream(new FileOutputStream(fl));
 	
 				long startTime = System.nanoTime();
 				while((byteRead = in.read(buffer)) != -1) {
 					out.write(buffer, 0, byteRead);
 				}
 				long endTime = System.nanoTime();
 				transferRate = (int) ((endTime-startTime)/fileLength);
 			} catch(Exception e) {
 				e.printStackTrace();
 			} finally {
 				out.flush();
 				out.close();
 				in.close();
 				logger.log(Level.INFO, "Download successfully. Average download rate is: " + transferRate + "Kb/s");
 			}
 		}
		return transferRate;
 	}
 	
 	/**
 	 * Get the contetn of specified url
 	 * @param uri The url from contents get
 	 * @return The url content
 	 */
 	public static String getContent(String uri) {
 		try {
             URL url = new URL(uri);
             URLConnection yc = url.openConnection();
             BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
             String inputLine;
             inputLine = in.readLine();
             in.close();
             return inputLine;
         } catch (Exception e) {
             e.printStackTrace();
         }
 		return null;
 	}
 	
 	/**
 	 * Compare given string and string form given location
 	 * @param uri The location from string is given
 	 * @param search The matched string
 	 * @return True is two string is match (ignore case), false if not
 	 */
 	public static boolean compareResult(String uri, String search) {
 		try {
             URL url = new URL(uri);
             URLConnection yc = url.openConnection();
             BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
             String inputLine;
             inputLine = in.readLine();
             in.close();
 
             if(inputLine.equalsIgnoreCase(search)) {
             	return true;
             } else {
             	return false;
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 		return false;
 	}
 }
