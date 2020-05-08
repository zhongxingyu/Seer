 package com.autburst.picture.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.FileEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
 
 import android.util.Log;
 
 public class HttpRequest {
 
 	private static final String TAG = HttpRequest.class.getSimpleName();
 
 	public void delete(String sUrl) throws Exception {
 		HttpClient httpclient = new DefaultHttpClient();
 
 		try {
 
 			// Prepare a request object
 			HttpDelete httpdelete = new HttpDelete(sUrl);
 
 			// Execute the request
 			HttpResponse response;
 
 			response = httpclient.execute(httpdelete);
 			// Examine the response status
 			StatusLine statusLine = response.getStatusLine();
 			Log.i(TAG, statusLine.toString());
 			int statusCode = statusLine.getStatusCode();
 			
 			if (statusCode != HttpStatus.SC_OK  && statusCode != HttpStatus.SC_NO_CONTENT) 
 				throw new Exception("could not perform delete request: "
 						+ statusLine.getStatusCode());
 
 		} catch (Exception ex) {
 			Log.e(TAG, "---------Error-----" + ex.getMessage());
 			throw new Exception(ex);
 		} finally {
 			httpclient.getConnectionManager().shutdown();
 		}
 	}
 
 	/**
 	 * HttpGet request
 	 * 
 	 * @param sUrl
 	 * @return
 	 */
 	public String get(String sUrl) throws Exception {
 
		HttpParams my_httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(my_httpParams,
15000);
		HttpClient httpclient = new DefaultHttpClient(my_httpParams);
 
 		try {
 
 			// Prepare a request object
 			HttpGet httpget = new HttpGet(sUrl);
 			
 			// Execute the request
 			HttpResponse response;
 
 			response = httpclient.execute(httpget);
 			// Examine the response status
 			StatusLine statusLine = response.getStatusLine();
 			Log.i(TAG, statusLine.toString());
 
 			if (statusLine.getStatusCode() != HttpStatus.SC_OK)
 				throw new Exception("could not perform get request: "
 						+ statusLine.getStatusCode());
 
 			// Get hold of the response entity
 			HttpEntity entity = response.getEntity();
 			// If the response does not enclose an entity, there is no need
 			// to worry about connection release
 
 			if (entity != null) {
 				InputStream instream = entity.getContent();
 				String result = convertStreamToString(instream);
 				Log.i(TAG, result);
 
 				// Closing the input stream will trigger connection release
 				instream.close();
 
 				return result;
 			}
 
 			throw new Exception("Http get request did not deliver any payload!");
 		} catch (Exception ex) {
 			Log.e(TAG, "---------Error-----" + ex.getMessage());
 			throw new Exception(ex);
 		} finally {
 			httpclient.getConnectionManager().shutdown();
 		}
 	}
 
 	private String convertStreamToString(InputStream is) throws IOException {
 		/*
 		 * To convert the InputStream to String we use the
 		 * BufferedReader.readLine() method. We iterate until the BufferedReader
 		 * return null which means there's no more data to read. Each line will
 		 * appended to a StringBuilder and returned as String.
 		 */
 		// BufferedReader reader = new BufferedReader(new
 		// InputStreamReader(is));
 		// StringBuilder sb = new StringBuilder();
 		//
 		// String line = null;
 		// try {
 		// while ((line = reader.readLine()) != null) {
 		// sb.append(line);
 		// }
 		// } catch (IOException e) {
 		// e.printStackTrace();
 		// } finally {
 		// try {
 		// is.close();
 		// } catch (IOException e) {
 		// e.printStackTrace();
 		// }
 		// }
 		// return sb.toString();
 		try {
 			InputStreamReader in = new InputStreamReader(is);
 			StringWriter sw = new StringWriter();  
 			char[] buffer = new char[1024];  
 			for (int n; (n = in.read(buffer)) != -1; )  
 			    sw.write(buffer, 0, n);  
 
 			return sw.toString(); 
 		} catch (IOException e) {
 			throw new IOException(e.getMessage());
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void post(String url, File file) throws Exception {
 		HttpClient httpClient = new DefaultHttpClient();
 		try {
 
 			httpClient.getParams().setParameter("http.socket.timeout",
 					new Integer(90000)); // 90 sec.
 			HttpPost post = new HttpPost(url);
 			
 //			Multi mpEntity = new MultipartEntity();
 //		    ContentBody cbFile = new FileBody(file, "image/jpeg");
 //		    mpEntity.addPart("userfile", cbFile);
 			
 			
 			FileEntity entity;
 
 			// entity = new FileEntity(file, "binary/octet-stream");
 			entity = new FileEntity(file, "image/jpeg");
 			//entity.setChunked(true);
 			post.setEntity(entity);
 			//post.setHeader("Content-type", "image/jpeg");
 			// Send any XML file as the body of the POST request
 //	        File f = new File("students.xml");
 //	        System.out.println("File Length = " + f.length());
 
 //	        post.setBody(new FileInputStream(file));
 ////	        c
 ////	            "image/jpeg; charset=ISO-8859-1");
 //	        post.setHeader("Content-type", "image/jpeg");
 			post.addHeader("filename", file.getName());
 
 			HttpResponse response = httpClient.execute(post);
 			int statusCode = response.getStatusLine().getStatusCode();
 			if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_NO_CONTENT) {
 				Log.e(TAG, "--------Error--------Response Status linecode:"
 						+ response.getStatusLine());
 				throw new Exception(
 						"--------Error--------Response Status linecode:"
 								+ response.getStatusLine());
 			}
 			// else {
 			// // Here every thing is fine.
 			// }
 			// HttpEntity resEntity = response.getEntity();
 			// if (resEntity == null) {
 			// Log.e(TAG, "---------Error No Response !!!-----");
 			// }
 		} catch (Exception ex) {
 			Log.e(TAG, "---------Error-----" + ex.getMessage());
 			throw new Exception(ex);
 		} finally {
 			httpClient.getConnectionManager().shutdown();
 		}
 	}
 }
