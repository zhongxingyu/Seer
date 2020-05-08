 package com.kalidu.codeblue;
 
 public class URLManager {
 	private String base = "http://10.0.2.2:5000/api/";
 	private String androidBase = "http://10.0.2.2:5000/api/android/";
 	private String verifyToken = "verify_token.json";
 	private String verifyCredentials = "user/verify_credentials.json";
 	
 	public String getVerifyTokenURL(){
 		return androidBase + verifyToken;
 	}
 	
 	public String getVerifyCredentialsURL(){
 		return base + verifyCredentials;
 	}
 }
