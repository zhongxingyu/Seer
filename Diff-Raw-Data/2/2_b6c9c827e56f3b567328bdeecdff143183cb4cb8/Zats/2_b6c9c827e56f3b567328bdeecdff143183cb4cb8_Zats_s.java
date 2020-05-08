 /* Zats.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		Mar 20, 2012 Created by pao
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
  */
 package org.zkoss.zats.mimic;
 
 import org.zkoss.zats.ZatsException;
 
 
 
 /**
  * The main class to start or stop the default {@link ZatsEnvironment}
  * @author Hawk
  * @author Dennis
  */
 public class Zats {
 	//the singleton default environment
 	private static ZatsEnvironment instance;
 	
 	private synchronized static ZatsEnvironment getInstance(boolean create){
 		if(instance == null){
 			if(create){
 				//the default emulator zats environment
 				instance = new DefaultZatsEnvironment();
 			}else{
				throw new ZatsException("instance not foudn, please call init first");
 			}
 
 		}
 		return instance;
 	}
 	/**
 	 * To initialize a test runtime and hold a {@link ZatsEnvironment}, the default environment is {@link DefaultZatsEnvironment}.
 	 * It use built-in configuration files(web.xml,zk.xml) for starting quickly and safely.
 	 *  
 	 * @param resourceRoot the resource root folder of the zul, it is usually the web content folder.
 	 */
 	public static void init(String resourceRoot){
 		getInstance(true).init(resourceRoot);
 	}
 	
 	/**
 	 * to end the test runtime and destroy the held {@link ZatsEnvironment}. 
 	 */
 	public static void end() {
 		getInstance(true).destroy();
 	}
 
 	/**
 	 * to create a new client
 	 * @return
 	 */
 	public static Client newClient(){
 		return getInstance(false).newClient();
 	}
 	
 	/**
 	 * to cleanup the held {@link ZatsEnvironment}
 	 */
 	public static void cleanup() {
 		getInstance(true).cleanup();
 	}
 	
 }
