 /*
  * This file is part of Rabbit Messenger.
  * 
  * Rabbit Messenger is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Rabbit Messenger is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Rabbit Messenger. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright 2012 Julien Faucher
  */
 package com.rabbitmessenger.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Logger;
 
 public class RabbitCommunication {
 
 	public static final ConfigurationManager config = ConfigurationManager
 			.getInstance();
 
 	private static final Logger log = Logger
 			.getLogger(RabbitCommunication.class.getName());
 
 	public static boolean sendMessage(String message) {
 
 		String url = "http://api.nabaztag.com/vl/api.jsp?token="
 				+ config.getToken() + "&sn=" + config.getSerialNumber()
 				+ "&tts=" + message;
 
 		return sendURL(url);
 	}
 
 	public static boolean playMP3(String message) {
 
 		String url = "http://api.nabaztag.com/vl/FR/api_stream.jsp?token="
 				+ config.getToken() + "&sn=" + config.getSerialNumber()
 				+ "&urlList=" + message;
 
 		return sendURL(url);
 	}
 
 	private static boolean sendURL(String inputUrl) {
 
 		inputUrl = inputUrl.replaceAll(" ", "%20");
 
 		log.fine("Calling URL: " + inputUrl);
 
 		boolean result = false;
 
 		try {
 			URL url = new URL(inputUrl);
 
 			HttpURLConnection connection = (HttpURLConnection) url
 					.openConnection();
 			connection.setRequestProperty("Cache-Control", "max-age=0");
 			connection.setDoOutput(true);
 
 			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
 				// OK
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(connection.getInputStream()));
 				String line;
 
 				while ((line = reader.readLine()) != null) {
 					log.fine("Received: " + line);
 					result = "<string>ok</string>".equals(line);
 					break;
 				}
 				reader.close();
 			} else {
 				// Server returned HTTP error code.
				log.info("Received HTTP error code: "
 						+ connection.getResponseCode());
 				return false;
 			}
 
 		} catch (MalformedURLException e) {
 			log.severe("MalformedURLException: " + e.getLocalizedMessage());
 			return false;
 		} catch (IOException e) {
 			log.severe("IOException: " + e.getLocalizedMessage());
 			return false;
 		}
 
 		return result;
 	}
 
 }
