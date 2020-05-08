 /**     
  * LICENSE:
  *       
  * Copyright (c) 2012, Sumilux Technologies, LLC 
  * All rights reserved.
  *       
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions
  * are met:
  *       
  *  * Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the distribution.
  *       
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
 
 /*
  * Social-sign-in's SDK
  * 2012/02/20 by kevin
  *
  */
 package com.sumilux.ssi.client;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 import com.sumilux.ssi.client.json.JSONException;
 import com.sumilux.ssi.client.json.JSONObject;
 
 /**
  * The Function set that Idme provides 
  * and this is one and only entrance to invoke idme's function.
  * 
  * @author  kevin 2012/02/20
  * @version 1.0
  */
 public class Idme {
 
 	private String token;
 
     /** 
      * Idme's constructor
      * @param token your token
      * @param apiURL idme service's address
      */
 	public Idme(String token,String apiURL) throws IdmeException {
 		this.token = token;
 		IdmeClient.setBaseUrl(apiURL);
 	}
 	
     /** 
      * Idme's constructor
      * @param token your token
      */
 	public Idme(String token){
 		this.token = token;
 	}
 	
 	//*********** auth info ***************
     /** 
      * make token invalid
      * 
      * @return true:succedd false:failed
      * @exception IdmeException
      */
 	public boolean expireToken() throws IdmeException {
 		Map<String, String> queryParas = new TreeMap<String, String>();
 		queryParas.put("token", token);
 		IdmeClient client = new IdmeClient("/auth/v1/expireToken");
 		try {
 			return "1".equals(((JSONObject) client.execute(queryParas)).get("result")) ? true : false;
 		} catch (JSONException e) {
 			throw new IdmeException(e);
 		}
 	}
 	
     /** 
      * Get the test user's token
      * 
      * @return String token
      * @exception IdmeException
      */
 	public static String allocateTestUser() throws IdmeException {
 		Map<String, String> emptyParas = new TreeMap<String, String>();
 		IdmeClient client = new IdmeClient("/auth/v1/allocateTestUser");
 		try {
 			JSONObject retJO = (JSONObject) client.execute(emptyParas);
 			return retJO.getString("ssi_token");
 		} catch (JSONException e) {
 			throw new IdmeException(e);
 		}
 	}
 	
     /** 
      * verify token's validity
      * 
      * @return true:valid false:invalid
      * @exception IdmeException
      */
 	public boolean isValidToken() throws IdmeException {
 		Map<String, String> queryParas = new TreeMap<String, String>();
 		queryParas.put("token", token);
 		IdmeClient client = new IdmeClient("/auth/v1/isValidToken");
 		try {
 			return "1".equals(((JSONObject) client.execute(queryParas)).get("result")) ? true : false;
 		} catch (JSONException e) {
 			throw new IdmeException(e);
 		}
 	}
 	
     /**
      * verify appName is matched with appSecret or not.
      *
      * @param appName widget name
      * @return true:match false:not match
      * @exception IdmeException
      */
 	public boolean isMatchAppNameAndSecret(String appName, String appSecret) throws IdmeException {
 		Map<String, String> queryParas = new TreeMap<String, String>();
 		IdmeClient client = new IdmeClient("/auth/v1/isMatchAppNameAndSecret");
 		queryParas.put("appName", appName);
 		queryParas.put("appSecret", appSecret);
 		try {
 			return "1".equals(((JSONObject) client.execute(queryParas)).get("result")) ? true : false;
 		} catch (JSONException e) {
 			throw new IdmeException(e);
 		}
 	}
 	
     /**
      * Get Identity's attribute value from auth source
      *
      * @return JSON String(the return value are different for the different auth sources)
      * @exception IdmeException
      */
 	public JSONObject getIdentityAttr() throws IdmeException {
 		Map<String, String> queryParas = new TreeMap<String, String>();
 		queryParas.put("token", token);
 		IdmeClient client = new IdmeClient("/auth/v1/getIdentityAttr");
 		return (JSONObject) client.execute(queryParas);
 	}
 	
     /**
      * Get Auth source of token
      *
     * @return JSON String({"authsource":"google"})
      * @exception IdmeException
      */
 	public JSONObject getMyAuthSource() throws IdmeException {
 		Map<String, String> queryParas = new TreeMap<String, String>();
 		queryParas.put("token", token);
 		IdmeClient client = new IdmeClient("/auth/v1/getMyAuthSource");
 		return (JSONObject) client.execute(queryParas);
 	}
 	//*********** auth info ***************
 	
 	//*********** photo info **************
     /**
      * Get user's album list
      *
      * @param appName widget name
      * @return JSON String(reference the return value of auth source)
      * @exception IdmeException
      */
 	public JSONObject getUserAlbumList(String appName) throws IdmeException {
 		Map<String, String> queryParas = new TreeMap<String, String>();
 		queryParas.put("token", token);
 		queryParas.put("appName", appName);
 		IdmeClient client = new IdmeClient("/photo/v1/getUserAlbumList");
 		return (JSONObject) client.execute(queryParas);
 	}
 	
     /**
      * Get user's photos
      *
      * @param appName widget name
      * @param albumid album ID (get by getUserAlbumList)
      * @param pid picture ID
      * @param password album's password
      * @return JSON String(reference the return value of auth source)
      * @exception IdmeException
      */
 	public JSONObject getUserPhotos(String appName, String albumid, String pid, String password) throws IdmeException {
 		Map<String, String> queryParas = new TreeMap<String, String>();
 		queryParas.put("token", token);
 		queryParas.put("appName", appName);
 		queryParas.put("albumid", albumid);
 		queryParas.put("pid", pid);
 		queryParas.put("password", password);
 		IdmeClient client = new IdmeClient("/photo/v1/getUserPhotos");
 		return (JSONObject) client.execute(queryParas);
 	}
 	
 	//*********** photo info **************
 	
 	//*********** user info ***************
     /**
      * Get user ID generated by idme server
      *
      * @return String user ID
      * @exception IdmeException
      */
 	public String getUID() throws IdmeException {
 		Map<String, String> queryParas = new TreeMap<String, String>();
 		queryParas.put("token", token);
 		IdmeClient client = new IdmeClient("/user/v1/getUID");
 		try {
 			return ((JSONObject) client.execute(queryParas)).getString("data");
 		} catch (JSONException e) {
 			throw new IdmeException(e);
 		}
 	}
 	
     /**
      * Get user profile from idme server
      *
      * @return JSON String key:{userID, salutation, firstName, lastName, displayName, defaultOrg, emails}
      *          emails is JSON Array[{"primary": "dummy@dummy.com"},{"secondary": "dummy@dummy.com"}]
      * @exception IdmeException
      */
 	public JSONObject getUserProfile() throws IdmeException {
 		Map<String, String> queryParas = new TreeMap<String, String>();
 		queryParas.put("token", token);
 		IdmeClient client = new IdmeClient("/user/v1/getUserProfile");
 		return (JSONObject) client.execute(queryParas);
 	}
 	//*********** user info ***************
 }
