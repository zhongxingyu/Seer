 package com.baixing.network.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Map;
 import java.util.zip.GZIPOutputStream;
 
 import android.util.Log;
 
 public class ZippedPostRequest extends PostRequest {
 
 	public ZippedPostRequest(String baseUrl, Map<String, String> parameters) {
 		super(baseUrl, parameters);
 //		this.addHeader("Content-Encoding", "gzip");
 		charset = "ISO-8859-1";
 	}
 	
 	@Override
 	public String getContentType() {
 		return "application/zip";
 //		return "text/plain";
 	}
 	
 	public int writeContent(OutputStream out) {
 		final String params = getFormatParams();
 		if (params == null) {
 			return 0;
 		}
 		
 		try {
 			
 			ByteArrayOutputStream bo = new ByteArrayOutputStream();   
 			GZIPOutputStream gzip = new GZIPOutputStream(bo);   
 			gzip.write(params.getBytes());
 			gzip.close(); 
		    byte[] content = bo.toByteArray();
 			
 			out.write(content);
 			out.flush();
 			
 			return content.length;
 		} catch (IOException e) {
 			Log.d(TAG, "exception when write data to outputstream." + e.getMessage());
 		}
 		
 		return 0;
 	}
 
 }
