 package com.grikly;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import com.grikly.model.AccessToken;
 import com.grikly.model.UserCredential;
 import com.grikly.request.HttpBuilder;
 import com.grikly.request.Request;
 
 public class AccessTokenManager {
 	private final String apiKey;
 	
 	
 	/**
 	 * Default Constructor
 	 * @author Mario Dennis
 	 * @param apiKey
 	 */
 	public AccessTokenManager(String apiKey)
 	{
 		this.apiKey = apiKey;
 	}//end constructor
 	
 	
 	private String getApiKey ()
 	{
 		return apiKey;
 	}//end getApiKey method
 	
 	
 	/**
 	 * Gets AccessToken 
 	 * @param userCredential
 	 * @param apiKey
 	 * @return AccessToken
 	 */
 	public AccessToken getAccessToken(UserCredential userCredential) 
 	{
 		if (userCredential == null)
 			throw new NullPointerException("Null UserCredential instance supplied");
 		
 		AccessToken accessToken = loadToken();//loads cached AccessToken
 		
 		if (accessToken != null && !accessToken.isExpired())
 			return accessToken;
 		
 		return processToken (userCredential);
 	}//end getAccessToken method
 	
 	
 	/**
 	 * Sends a HTTP request for AccessToken
 	 * @param userCredential
 	 * @param apiKey
 	 * @return
 	 */
 	private AccessToken processToken (UserCredential userCredential)
 	{
 		//add parameters
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("grant_type", "password");
 		params.put("username", userCredential.getEmail());
 		params.put("password", userCredential.getPassword());
 		
 		HttpBuilder<Void, AccessToken> builder = new HttpBuilder<Void, AccessToken>(AccessToken.class, getApiKey()); 
 		builder.setPath("Token");
 		builder.addHttpParams(params);
 		
 		Request<Void, AccessToken> request = builder.buildHttpPost();
 		AccessToken accessToken = request.execute();
 		storeToken(accessToken);//stores to cache
 		return accessToken;
 	}//end processToken method
 	
 	
 	/**
 	 * Stores AccessToken information into
 	 * a property file so that it can be
 	 * accessed at a later time.
 	 * @author mario
 	 * @param accessToken
 	 */
 	private void storeToken (AccessToken accessToken)
 	{
 		Properties properties = new Properties();
 		try {
			if (accessToken != null && accessToken.getAccess_token() != null) 
 			{
 				properties.setProperty(GriklyToken.ACCESS_TOKEN.toString(),
 						               accessToken.getAccess_token());
 				properties.setProperty(GriklyToken.EXPIRE_IN.toString(),
 						               Integer.toString(accessToken.getExpires_in()));
 				properties.setProperty(GriklyToken.USER.toString(),
 						               accessToken.getUserName());
 				properties.setProperty(GriklyToken.USER.toString(),
 						               accessToken.getUserName());
 				properties.store(new FileOutputStream("accessToken.property"),null);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}//end storeToken method
 	
 	
 	
 	/**
 	 * Loads AccessToken information
 	 * from property file and returns
 	 * a AccessToken instance.
 	 * @author mario
 	 * @return AccessToken null when no 
 	 * @throws IOException 
 	 */
 	private AccessToken loadToken ()
 	{
 		Properties properties = new Properties();
 		try {
 			File file = new File("accessToken.property");
 			file.createNewFile();
 			
 			properties.load(new FileInputStream (file));
 			
 			/*
 			 * Parse Property File only if an
 			 * AccessToken is stored.
 			 */
 			if (properties.containsKey(GriklyToken.ACCESS_TOKEN.toString()))
 			{
 				AccessToken accessToken = new AccessToken();
 				accessToken.setAccess_token(properties.getProperty(GriklyToken.ACCESS_TOKEN.toString()));
 				accessToken.setToken_type(properties.getProperty(GriklyToken.TOKEN_TYPE.toString()));
 				accessToken.setUserName(properties.getProperty(GriklyToken.USER.toString()));
 				accessToken.setExpires_in(Integer.parseInt(properties.getProperty(GriklyToken.EXPIRE_IN.toString())));
 				accessToken.setExpires(properties.getProperty(GriklyToken.EXPIRES.toString()));
 				accessToken.setIssued(properties.getProperty(GriklyToken.ISSUED.toString()));
 				return accessToken;
 			}
 		} catch (FileNotFoundException e) {
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}//end loadToken method
 	
 }//end AccessTokenManager class
