 package com.vbitz.MinecraftScript.web;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 
 public class StaticFileEndpoint implements HttpHandler {
 
 	private static HashMap<String, String> _resTypes = new HashMap<String, String>();
 	
 	static {
 		_resTypes.put("html", "text/html");
 		_resTypes.put("js", "text/js");
 	}
 	
 	private static String getFileExtention(String filename) {
 		String[] tokens = filename.split("\\.");
 		return _resTypes.get(tokens[tokens.length - 1]);
 	}
 	
 	@Override
 	public void handle(HttpExchange ex) {
 		try {
 			String filename = ex.getRequestURI().getPath();
 			filename = filename.substring("/static/".length(), filename.length());
 			if (filename.startsWith("..") || filename.endsWith("..")) {
 				ex.sendResponseHeaders(403, 0);
 				ex.close();
 				return;
 			}
			filename = "/com/vbitz/MinecraftScript/htmlsrc/" + filename;
			URL fileURL = this.getClass().getResource(filename);
 			if (fileURL == null) {
 				ex.sendResponseHeaders(404, 0);
 				ex.close();
 				return;
 			}
			int fileSize = 0;
			InputStream fileSizeRead = this.getClass().getResourceAsStream(filename);
			while (fileSizeRead.available() > 0) { // not the best code but it works, I might improve this in the future
				int arrLength = 2048;
				if (fileSizeRead.available() < 2048) {
					arrLength = fileSizeRead.available();
				}
				byte[] buf = new byte[arrLength];
				if (fileSizeRead.read(buf) != arrLength) {
					break;
				}
				fileSize += arrLength;
			}
			fileSizeRead.close();
			InputStream str = this.getClass().getResourceAsStream(filename);
			ex.sendResponseHeaders(200, fileSize);
 			OutputStream resStr = ex.getResponseBody();
 			int writenCount = 0;
 			while (str.available() > 0) {
 				int arrLength = 2048;
 				if (str.available() < 2048) {
 					arrLength = str.available();
 				}
 				byte[] buf = new byte[arrLength];
 				if (str.read(buf) != arrLength) {
 					break;
 				}
 				writenCount += arrLength;
 				resStr.write(buf);
 			}
			resStr.close(); // this sometimes fails
 			str.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
