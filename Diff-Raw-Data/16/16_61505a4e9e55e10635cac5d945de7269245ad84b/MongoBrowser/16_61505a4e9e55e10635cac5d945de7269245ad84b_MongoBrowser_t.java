 /*
  * mongobrowser - a webstart gui application for viewing, 
  *                editing and administering a Mongo Database
  * Copyright 2009-2011 MeBigFatGuy.com
  * Copyright 2009-2011 Dave Brosius
  * Copyright 2009-2011 Rodrigo Hjort
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0 
  *    
  * Unless required by applicable law or agreed to in writing, 
  * software distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and limitations 
  * under the License. 
  */
 package com.mebigfatguy.mongobrowser;
 
 import java.net.UnknownHostException;
 
 import com.mebigfatguy.mongobrowser.dialogs.MongoBrowserFrame;
 import com.mongodb.MongoException;
 
 /**
  * main application class
  */
 public class MongoBrowser {
 
 	public static final String DEFAULT_SERVER = "localhost";
 	public static final int DEFAULT_PORT = 27017;
 
 	/**
 	 * @param args
 	 *            the command line arguments
 	 */
 	public static void main(String[] args) {
 
 		final String host = (args.length > 0 ? args[0] : null);
 
 		MongoBrowserFrame frame = new MongoBrowserFrame();
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 
 		if (host != null) {
			final int port = (args.length > 1 ? Integer.parseInt(args[1])
					: DEFAULT_PORT);
 			try {
 				frame.startupConnection(host, port);
 			} catch (UnknownHostException e) {
 				System.err.println("Unknown host specified: " + host);
 			} catch (MongoException e) {
 				System.err.println("An error has occurred: " + e.getMessage());
 			}
 		}
 	}
 }
