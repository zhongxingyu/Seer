 package com.ashrafishak.crunchbase4j.util;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 public class ConfigProperties {
 
 	private static Properties properties = null;
 	
 	public static Properties getInstance(){
 		if (properties == null){
 			properties = new Properties();
 			try {
				properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("default.config"));
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return properties;
 	}
 }
