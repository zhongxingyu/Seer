 package com.themosec;
 
 public class EnvironmentHelper {
 	public static final int DEVELOPMENT = 0;
 	public static final int PRODUCTION = 1;
 	
 	private static int sEnvironment;
 	
	private static final String sDevelopmentUrl = "http://192.168.0.100:5000";
 	
 	private static final String sProductionUrl = "https://themosec.herokuapp.com";
 	
 	public static void setEnvironment(int environment) {
 		switch(environment) {
 		case DEVELOPMENT:
 			sEnvironment = DEVELOPMENT;
 			
 			break;
 		case PRODUCTION:
 			sEnvironment = PRODUCTION;
 			
 			break;
 		default:
 			sEnvironment = DEVELOPMENT;
 			
 			break;
 		}
 	}
 	
 	public static String getUrl() {
 		switch(sEnvironment) {
 		case DEVELOPMENT:
 			return sDevelopmentUrl;
 		case PRODUCTION:
 			return sProductionUrl;
 		default:
 			return sDevelopmentUrl;
 		}
 	}
 }
