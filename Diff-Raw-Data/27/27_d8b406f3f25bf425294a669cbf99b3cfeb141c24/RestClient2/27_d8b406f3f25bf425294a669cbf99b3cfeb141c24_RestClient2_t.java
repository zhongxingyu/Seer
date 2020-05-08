 package com.vzw.hackathon.apihandler;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
//import net.sf.json.JSONObject;
 
 import org.apache.log4j.Logger;
 
import com.hackathon.tvnight.util.JSONHelper;
 import com.vzw.hackathon.GroupEvent;
 import com.vzw.hackathon.MemberStatus;
//import com.vzw.util.JSONUtil;
 
 public class RestClient2 {
 
 	public static final String CHARSET = "UTF-8";
 	private static final Logger logger = Logger.getLogger(RestClient2.class);
 
 	public static String encodeParam(Map<String, String> paramMap)
 			throws Exception {
 		String urlParameters = null;
 
 		for (Entry<String, String> entry : paramMap.entrySet()) {
 			String pp = entry.getKey() + "="
 					+ URLEncoder.encode(entry.getValue(), CHARSET);
 			if (urlParameters == null) {
 				urlParameters = pp;
 			} else {
 				urlParameters += "&" + pp;
 			}
 		}
 
 		return urlParameters;
 	}
 
 	public static String execute(boolean doPost, String targetURL,
 			Map<String, String> paramMap) throws Exception {
 
 		String urlParameters = encodeParam(paramMap);
 		URL url;
 		HttpURLConnection connection = null;
 		try {
 			// Create connection
 			if (doPost) {
 				url = new URL(targetURL);
 			}
 			else  {
 				url = new URL(targetURL + "?" + urlParameters);
 			}
 			connection = (HttpURLConnection) url.openConnection();
 			
 			if (doPost) {
 				connection.setRequestMethod("POST");
 				connection.setRequestProperty("Content-Type",
 						"application/x-www-form-urlencoded");
 				
 				connection.setDoOutput(true);
 			}
 			else {
 				connection.setRequestMethod("GET");
 			}
 
 			connection.setRequestProperty("Content-Length",
 					"" + Integer.toString(urlParameters.getBytes().length));
 			//connection.setRequestProperty("Content-Language", "en-US");
 
 			connection.setReadTimeout(10000);
 			connection.setUseCaches(false);
 			connection.setDoInput(true);
 
 			// Send request
 			if (doPost) {
 				DataOutputStream wr = new DataOutputStream(
 						connection.getOutputStream());
 				wr.writeBytes(urlParameters);
 				wr.flush();
 				wr.close();
 			}
 			
 			
 			// Get Response
 			InputStream is = connection.getInputStream();
 			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
 			String line;
 			StringBuffer response = new StringBuffer();
 			while ((line = rd.readLine()) != null) {
 				response.append(line);
 				response.append('\r');
 			}
 			rd.close();
 			return response.toString();
 
 		} catch (Exception e) {
 
 			e.printStackTrace();
 			return null;
 
 		} finally {
 
 			if (connection != null) {
 				connection.disconnect();
 			}
 		}
 	}
 
 	
 	protected static int extractId(String response){
//		JSONObject jsonObject = JSONObject.fromObject( response );
//		ServerResponse sr = (ServerResponse) JSONUtil.toJava(jsonObject, ServerResponse.class);
		ServerResponse sr = JSONHelper.fromJson(response, ServerResponse.class);
 		return sr.getId();
 	}
 	
 	public static int createGroupEvent(GroupEvent ge, String serverBaseURL) {
 		int id = -1;
 		
 		try {
 			Map<String, String> pm = new HashMap<String, String>();
//			String js = JSONUtil.toJsonString(ge);
			String js = JSONHelper.toJson(ge);
			pm.put("groupEvent", js);
 			
 			String res = execute(true, serverBaseURL + "/server/groupEvents", pm);
 			
 			logger.info("got group event id: " + res);
 			id = extractId(res);
 		}
 		catch (Exception e) {
 			logger.error("Failed to create group event: " + ge, e);
 		}
 
 		return id;
 	}
 	
 	public static void rsvp(int groupEventId, String mdn, MemberStatus status, String serverBaseURL){
         try {
         	
         	String url = serverBaseURL + "/server/groupEvents/" + groupEventId + "/rsvp";
         	logger.info("url = " + url);
         	
 			Map<String, String> pm = new HashMap<String, String>();
 			pm.put("mdn", mdn);
 			pm.put("status", status.toString());
         	String res = execute(false, url, pm);
 
         	logger.info("rsvp response: " + res);
 	    } catch (Exception e) {
 	        logger.error("error", e);
 	    }
 	}
 	
 	public static void postMessage(String sender, String recipient, String message, String serverBaseURL){
         try {
         	
         	String url = serverBaseURL + "/server/messages";
         	Map<String, String> pm = new HashMap<String, String>();
         	pm.put("mdn", recipient);
         	pm.put("from", sender);
         	pm.put("message", message);
         	
 	        String res = execute(true, url, pm);
 	        logger.info("postMessage response: " + res);
 	        
 	        
 	    } catch (Exception e) {
 	        logger.error("error", e);
 	    }
 		
 	}
 
 	
 	public static int sendFling(byte[] data, String fileName, String serverBaseURL){
 		int id = -1;
 		
 		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
 		String CRLF = "\r\n"; // Line separator required by multipart/form-data.
 		
 		String url = serverBaseURL + "/server/flings";
 		PrintWriter writer = null;
 		URLConnection connection = null;
 		String charset = "UTF-8";
 		try {
 			connection = new URL(url).openConnection();
 			connection.setDoOutput(true);
 			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
 			
 		    OutputStream output = connection.getOutputStream();
 		    writer = new PrintWriter(new OutputStreamWriter(output, charset), true); // true = autoFlush, important!
 
 		    // Send binary file.
 		    writer.append("--" + boundary).append(CRLF);
 		    writer.append("Content-Disposition: form-data; name=\"fling\"; filename=\"" + fileName + "\"").append(CRLF);
 		    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(CRLF);
 		    writer.append("Content-Transfer-Encoding: binary").append(CRLF);
 		    writer.append(CRLF).flush();
 		    InputStream input = null;
 		    try {
 		        input = new ByteArrayInputStream(data);
 		        byte[] buffer = new byte[1024];
 		        for (int length = 0; (length = input.read(buffer)) > 0;) {
 		            output.write(buffer, 0, length);
 		        }
 		        output.flush(); // Important! Output cannot be closed. Close of writer will close output as well.
 		    } finally {
 		        if (input != null) try { input.close(); } catch (IOException logOrIgnore) {}
 		    }
 		    writer.append(CRLF).flush(); // CRLF is important! It indicates end of binary boundary.
 
 		    // End of multipart/form-data.
 		    writer.append("--" + boundary + "--").append(CRLF);
 		    
 		} catch (Exception e) {
 			System.out.println("Error uploading fling" + e);
 		} finally {
 		    if (writer != null) writer.close();
 		}
 		
 		// read response
 		try {
 			InputStream response = connection.getInputStream();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(response, charset));
 			StringBuilder sb = new StringBuilder();
 		    for (String line; (line = reader.readLine()) != null;) {
 		            sb.append(line);
 		    }
 			if (sb.length() > 0) {
 				id = extractId(sb.toString());
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		return id;
 	}
 	
 
 	
 	
 	public static File downloadFling(int flingId, String serverBaseURL, String folderPath){
 		String url = serverBaseURL + "/server/flings/" + flingId + ".file";
 
 		
 		URLConnection connection;
 		try {
 			connection = new URL(url).openConnection();
 			InputStream response = connection.getInputStream();
 			
 			String contentType = connection.getHeaderField("Content-Type");
 			String contentDisposition = connection.getHeaderField("Content-Disposition");
 			logger.debug("***** contentDisposition: " + contentDisposition);
 			String filename = null;
 			if (contentDisposition == null) {
 				filename = "defaultFilename";
 			} else {
 				filename = contentDisposition.split(";")[1].split("=")[1];
 			}
 			
 			File tempFile = new File(folderPath, filename);
 
 			// read bytes
 			response = connection.getInputStream();
 			byte[] buffer = new byte[4096];
 			int n = - 1;
 
 			OutputStream output = new FileOutputStream(tempFile);
 			while ( (n = response.read(buffer)) != -1)
 			{
 			    if (n > 0)
 			    {
 			        output.write(buffer, 0, n);
 			    }
 			}
 			output.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	
 		
 	
 }
