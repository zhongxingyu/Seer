 package org.pointrel.pointrel20120623.core;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 public class Server implements VariablesInterface, ResourcesInterface {
 	final String serverURL;
 	final String variableScript = "variable.php";
 	final String downloadScript = "download.php";
 	final String uploadScript = "upload.php";
 	
 	public Server(String serverURL) {
 		this.serverURL = serverURL;
 	}
 	
 	public String basicGetValue(String variableName) {
 		URL fullURL;
 		String variableInfo;
 		try {
 			fullURL = new URL(serverURL + variableScript + "?variable=" + variableName);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return null;
 		}
 		try {
 			variableInfo = IOUtils.toString(fullURL, "utf-8");
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		//System.out.println("variableInfo:\n" + variableInfo);
 		// now parse the variableInfo
 		String lines[] = variableInfo.split("\\r?\\n");
 		for (String line: lines) {
 			if (line.length() == 0) continue;
 			if (line.startsWith("#")) continue;
 			if (line.startsWith("current_value: ")) {
 				String[] segments = line.split(" ");
				if (segments.length != 2) return null;
				if (segments[1].isEmpty()) return null;
 				return segments[1];
 			}
 		}
 		return null;
 	}
 
 	public boolean basicSetValue(String variableName, String userID, String previousValue, String newValue, String comment) {
 		String variableNameEncoded;
 		String userIDEncoded;
 		String previousValueEncoded;
 		String newValueEncoded;
 		String commentEncoded;
 		try {
 			variableNameEncoded = URLEncoder.encode(variableName, "utf-8");
 			userIDEncoded = URLEncoder.encode(userID, "utf-8");
			if (previousValue == null) {
				previousValueEncoded = "";
			} else {
				previousValueEncoded = URLEncoder.encode(previousValue, "utf-8");
			}
 			newValueEncoded = URLEncoder.encode(newValue, "utf-8");
 			commentEncoded = URLEncoder.encode(comment, "utf-8");
 		} catch (UnsupportedEncodingException e1) {
 			e1.printStackTrace();
 			return false;
 		}
 		
 		URL fullURL;
 		String variableInfo;
 		try {
 			fullURL = new URL(serverURL + variableScript + "?variable=" + variableNameEncoded + "&new_value=" + newValueEncoded + "&previous_value=" + previousValueEncoded + "&user_id=" + userIDEncoded + "&comment=" + commentEncoded);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		try {
 			variableInfo = IOUtils.toString(fullURL, "utf-8");
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		//System.out.println("variableInfo:\n" + variableInfo);
 		// now parse the variableInfo
 		String lines[] = variableInfo.split("\\r?\\n");
 		for (String line: lines) {
 			if (line.length() == 0) continue;
 			if (line.startsWith("#")) continue;
 			if (line.startsWith("new_value: ")) {
 				String[] segments = line.split(" ");
 				if (newValue.length() == 0) {
 					return segments.length == 1;
 				}
 				if (segments.length != 2) return false;
 				return newValue.equals(segments[1]);
 			}
 		}
 		return false;
 	}
 
 	public String getContentForURIAsString(String resourceURI) {
 		URL fullURL;
 		String resourceContents;
 		try {
 			fullURL = new URL(serverURL + downloadScript + "?resource=" + resourceURI);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return null;
 		}
 		try {
 			resourceContents = IOUtils.toString(fullURL, "utf-8");
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		return resourceContents;
 	}
 	
  	public byte[] getContentForURI(String resourceURI) {
 		URL fullURL;
 		byte[] resourceContents;
 		try {
 			fullURL = new URL(serverURL + downloadScript + "?resource=" + resourceURI);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return null;
 		}
 		try {
 			resourceContents = toByteArray(fullURL);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		return resourceContents;
 	}
  	
  	// Copied from Apache IOUtils because otherwise requires Java 1.6
 	private static byte[] toByteArray(URL url) throws IOException {
 		URLConnection conn = url.openConnection();
 		try {
 			return toByteArray(conn);
 		} finally {
 			close(conn);
 		}
 	}
 
 	// Copied from Apache IOUtils because otherwise requires Java 1.6
 	private static byte[] toByteArray(URLConnection urlConn) throws IOException {
 		InputStream inputStream = urlConn.getInputStream();
 		try {
 			return IOUtils.toByteArray(inputStream);
 		} catch (java.io.FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			inputStream.close();
 		}
 	}
 	
 	// Copied from Apache IOUtils because otherwise requires Java 1.6
 	private static void close(URLConnection conn) {
     		if (conn instanceof HttpURLConnection) {
     			((HttpURLConnection) conn).disconnect();
     		}
     }
 	
 	public String addContent(byte[] content, String contentType, String userID, String uri) {
 		if (uri == null) uri = Utility.calculateURI(content, contentType);
 
         HttpClient httpclient = new DefaultHttpClient();
         try {
             HttpPost httppost = new HttpPost(serverURL + uploadScript);
 
             String fileName = uri.substring("pointrel://".length());
             ByteArrayBody resourceBody = new ByteArrayBody(content, contentType, fileName);
             StringBody userIDBody;
 			try {
 				userIDBody = new StringBody(userID);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 				return null;
 			}
 
             MultipartEntity reqEntity = new MultipartEntity();
             reqEntity.addPart("uploaded", resourceBody);
             reqEntity.addPart("userID", userIDBody);
 
             httppost.setEntity(reqEntity);
 
             System.out.println("executing request " + httppost.getRequestLine());
             HttpResponse response;
 			try {
 				response = httpclient.execute(httppost);
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 				return null;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
             HttpEntity resEntity = response.getEntity();
 
             System.out.println("----------------------------------------");
             System.out.println(response.getStatusLine());
             if (resEntity != null) {
                 System.out.println("Response content length: " + resEntity.getContentLength());
             }
             try {
 				EntityUtils.consume(resEntity);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 			int status = response.getStatusLine().getStatusCode();
 			if (status < 200 || status > 299) {
 				System.out.println("Bad status");
 				return null;
 			}
         } finally {
         		// TODO: Maybe should leave this running?
             try { httpclient.getConnectionManager().shutdown(); } catch (Exception ignore) {}
         }
 
 		return uri;
 	}
 
 	public ArrayList<String> getAllVariableNames() {
 		URL fullURL;
 		String variableInfo;
 		try {
 			fullURL = new URL(serverURL + variableScript);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return null;
 		}
 		try {
 			variableInfo = IOUtils.toString(fullURL, "utf-8");
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		ArrayList<String> result = new ArrayList<String>();
 		//System.out.println("variableInfo:\n" + variableInfo);
 		// now parse the variableInfo
 		String lines[] = variableInfo.split("\\r?\\n");
 		for (String line: lines) {
 			if (line.length() == 0) continue;
 			if (line.startsWith("#")) continue;
 			if (line.startsWith("variable: ")) {
 				String[] segments = line.split(" ");
 				if (segments.length != 2) return null;
 				result.add(segments[1]);
 			}
 		}
 		return result;
 	}
 }
