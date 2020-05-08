 /* 
  * This file is part of PS2YT
  *
  * Copyright (C) 2013 Frédéric Bertolus (Niavok)
  * 
  * PS2YT is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.niavok.youtube;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class PostQuery {
 
 	private String request;
 	private String data;
 	private Map<String, String> propertiesMap = new HashMap<String, String>();
 	private String contentType = "application/x-www-form-urlencoded";
 	private String location; 
 
 	public PostQuery(String request, String data) {
 		this.request = request;
 		this.data = data;

		System.out.println("PostQuery request="+request);
		System.out.println("PostQuery data="+data);
 	}
 
 	private void execute(QueryOutput queryOutput) {
 
 		try {
 			URL url = new URL(request);
 
 			HttpURLConnection connection = (HttpURLConnection) url
 					.openConnection();
 			connection.setDoOutput(true);
 			connection.setDoInput(true);
 			connection.setInstanceFollowRedirects(false);
 			connection.setRequestMethod("POST");
 			
 			if(data.length() > 0) {
 				connection.setRequestProperty("Content-Type",
 						contentType);
 				connection.setRequestProperty("charset", "UTF-8");
 				connection.setRequestProperty("Content-Length",
 						"" + Integer.toString(data.getBytes().length));
 			} else {
 				connection.setRequestProperty("Content-Type",
 						"none");
 			}
 			
 			for(Entry<String,String> entry: propertiesMap.entrySet()) {
 				connection.addRequestProperty(entry.getKey(), entry.getValue());
 			}
 			
 			connection.setUseCaches(false);
 
 			
 			
 			
 			DataOutputStream wr = new DataOutputStream(
 					connection.getOutputStream());
 			wr.write(data.getBytes(Charset.forName("UTF-8")));
 			wr.flush();
 			wr.close();
 
 			if(connection.getResponseCode() != 200) {
 				for (int i = 0;; i++) {
 				      String headerName = connection.getHeaderFieldKey(i);
 				      String headerValue = connection.getHeaderField(i);
 				      System.out.println(headerName);
 				      System.out.println(headerValue);
 
 				      if (headerName == null && headerValue == null) {
				        System.out.println("No more headers");
 				        break;
 				      }
 			    }
 				return;
 			}
 			
 			for (int i = 0;; i++) {
 			      String headerName = connection.getHeaderFieldKey(i);
 			      String headerValue = connection.getHeaderField(i);
			      System.out.println(headerName);
			      System.out.println(headerValue);
 
 			      if (headerName == null && headerValue == null) {
			        System.out.println("No more headers");
 			        break;
 			      }
 			      
 			      if(headerName != null && headerName.equals("Location")) {
 			    	  location = headerValue;
 			      }
 		    }
 			
 			
 			
 			if (queryOutput != null) {
 
 				String line;
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
 
 				while ((line = reader.readLine()) != null) {
 					queryOutput.processLine(line);
 				}
 
 				reader.close();
 			}
 
 			connection.disconnect();
 
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public JSonMap getJsonResult() {
 		JSonQueryOutput jSonQueryOutput = new JSonQueryOutput();
 		execute(jSonQueryOutput);
 		return jSonQueryOutput.getOutput();
 	}
 
 	public void addProperty(String key, String value) {
 		propertiesMap.put(key, value);
 	}
 
 	public String getTextResult() {
 		TextQueryOutput textQueryOutput = new TextQueryOutput();
 		execute(textQueryOutput);
 		return textQueryOutput.getOutput();
 	}
 
 	public void setContentType(String contentType) {
 		this.contentType = contentType;
 	}
 
 	public String getLocation() {
 		return location;
 	}
 
 }
