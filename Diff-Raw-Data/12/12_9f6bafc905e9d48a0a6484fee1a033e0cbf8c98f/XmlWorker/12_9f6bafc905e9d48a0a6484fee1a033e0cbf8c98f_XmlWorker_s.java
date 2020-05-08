 /*
  * Copyright (C) [2011]  [Pascal Knig]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, see <http://www.gnu.org/licenses/>. 
 */
 package de.sockenklaus.XmlStats.XmlWorkers;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.GZIPOutputStream;
 
 import com.sun.net.httpserver.Headers;
 import com.sun.net.httpserver.HttpContext;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpExchange;
 
 import de.sockenklaus.XmlStats.XmlStats;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class XmlWorker.
  */
 @SuppressWarnings("restriction")
 public abstract class XmlWorker implements HttpHandler {
 	
 	/* (non-Javadoc)
 	 * @see com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange)
 	 */
 	public void handle(HttpExchange exchange) {
 		Map<String, List<String>> parameters = new HashMap<String, List<String>>();
 		
 		Headers headers = exchange.getRequestHeaders();
 		
 		if("get".equalsIgnoreCase(exchange.getRequestMethod())){
 			String queryString = exchange.getRequestURI().getRawQuery();
 			String xmlResponse = "";
 			byte[] byteResponse = null;
 			
 			try {
 				parameters = parseParameters(queryString);
 			} catch(UnsupportedEncodingException ex){
 				XmlStats.LogWarn("Fehler beim Parsen des HTTP-Query-Strings.");
 				XmlStats.LogWarn(ex.getMessage());
 			}
 			
 			xmlResponse = getXML(parameters);
 			
 			
 			/*
 			 * Check if the clients sends the header "Accept-encoding", the option "gzip-enabled" is true and the clients supports gzip.
 			 */
 			
 			
 			
 			if(parameters.containsKey("gzip") && parameters.get("gzip").contains("true")){
 				XmlStats.LogDebug("Raw gzip requested.");
 				
 				HttpContext context = exchange.getHttpContext();
 				String filename = context.getPath().substring(1);
 				
 				byteResponse = compressData(xmlResponse.getBytes());
 				exchange.getResponseHeaders().set("Content-type", "application/gzip");
 				exchange.getResponseHeaders().set("Content-disposition", "attachment; filename="+filename+".gzip");
 			}
 			else if(clientAcceptsGzip(headers)) {
 				byteResponse = compressData(xmlResponse.getBytes());
 				exchange.getResponseHeaders().set("Content-encoding", "gzip");
 			}	
 			else {
 				byteResponse = xmlResponse.getBytes();
 			}
 									
 			try {
 				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, byteResponse.length);
 		        exchange.getResponseBody().write(byteResponse);
 			}
 			catch(IOException ex){
 				XmlStats.LogError("Fehler beim Senden der HTTP-Antwort.");
 				XmlStats.LogError(ex.getMessage());
 			}
 	        
 	        exchange.close();
 		}
 	}
 	
 	/**
 	 * Parses the parameters.
 	 *
 	 * @param queryString the query string
 	 * @return the map
 	 * @throws UnsupportedEncodingException the unsupported encoding exception
 	 */
 	private Map<String, List<String>> parseParameters(String queryString) throws UnsupportedEncodingException {
 		Map<String, List<String>> result = new HashMap<String, List<String>>();
 		
 		if (queryString != null){
 			String pairs[] = queryString.split("[&]");
 			
 			for (String pair : pairs){
 				String param[] = pair.split("[=]");
 				
 				String key = null;
 				String value = null;
 				
 				if(param.length > 0){
					key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
 				}
 				
 				if(param.length > 1){
					value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
 				}
 				
 				if (result.containsKey(key)){
 					List<String> values = result.get(key);
 					
 					values.add(value);
 				}
 				else {
 					List<String> values = new ArrayList<String>();
 					values.add(value);
 					
 					result.put(key, values);
 				}
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Gets the xML.
 	 *
 	 * @param parameters the parameters
 	 * @return the xML
 	 */
 	abstract String getXML(Map<String, List<String>> parameters);
 	
 	private byte[] compressData(byte[] input){
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		byte[] output;
 		
 		try {
 			XmlStats.LogDebug("OK... let's try gzip compression...");
 			XmlStats.LogDebug("Actual size of the xml file: "+input.length+"Bytes");
 			GZIPOutputStream gzip = new GZIPOutputStream(out);
 			gzip.write(input);
 			gzip.close();
 			output = out.toByteArray();
 			XmlStats.LogDebug("Compressed size of the xml file: "+output.length+"Bytes");
 		}
 		catch(IOException e){
 			XmlStats.LogError("GZIP-Compression failed! Returning empty byte[]");
 			output = new byte[0];
 		}
 		
 		return output;
 	}
 
 	private boolean clientAcceptsGzip(Headers headers){
 		if(headers.containsKey("Accept-encoding")){
 			List<String> header = headers.get("Accept-encoding");
 			
 			for(String val : header){
 				if(val.toLowerCase().indexOf("gzip") > -1){
 					return true;
 				}
 			}
 			
 		}
 		
 		return false;
 	}
 }
