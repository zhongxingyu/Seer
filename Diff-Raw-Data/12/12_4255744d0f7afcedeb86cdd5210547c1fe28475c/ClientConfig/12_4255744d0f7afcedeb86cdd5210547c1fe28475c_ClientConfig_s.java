 /*******************************************************************************
  * Copyright (C) 2009, 2010, Hoccer GmbH Berlin, Germany <www.hoccer.com> These coded instructions,
  * statements, and computer programs contain proprietary information of Hoccer GmbH Berlin, and are
  * copy protected by law. They may be used, modified and redistributed under the terms of GNU
  * General Public License referenced below. Alternative licensing without the obligations of the GPL
  * is available upon request. GPL v3 Licensing: This file is part of the "Linccer Java-API". Linccer
  * Java-API is free software: you can redistribute it and/or modify it under the terms of the GNU
  * General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version. Linccer Java-API is distributed in the hope that
  * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You
  * should have received a copy of the GNU General Public License along with Linccer Java-API. If
  * not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package com.hoccer.api;
 
 import java.util.UUID;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class ClientConfig {
 
     protected static String mLinccerUri;
     protected static String mFileCacheUri;
     private final String    mApplicationName;
     private final UUID      mClientId;
     private final String    mClientName;
 
     private String          mApiKey;
     private String          mSharedSecret;
     private String          mPublicKey;
 
     static {
         useDevelopmentServers();
     }
 
     public ClientConfig(String applicatioName) {
         mApplicationName = applicatioName;
         mClientId = UUID.randomUUID();
         mClientName = "unnamed client";
         useDemoApiKey();
     }
 
     public ClientConfig(String applicatioName, UUID clientId, String clientName) {
         mApplicationName = applicatioName;
         mClientId = clientId;
         mClientName = clientName;
         useDemoApiKey();
     }
 
     public ClientConfig(String applicatioName, String apiKey, String sharedSecret) {
         mApplicationName = applicatioName;
         mClientId = UUID.randomUUID();
         mClientName = "unnamed client";
         mApiKey = apiKey;
         mSharedSecret = sharedSecret;
     }
 
     public ClientConfig(String applicatioName, String apiKey, String sharedSecret, UUID clientId,
             String clientName, String publicKey) {
         mApplicationName = applicatioName;
         mClientId = clientId;
         mClientName = clientName;
 
         mApiKey = apiKey;
         mSharedSecret = sharedSecret;
         mPublicKey = publicKey;
     }
 
     public static void useProductionServers() {
         mLinccerUri = "https://linccer.hoccer.com/v3";
         mFileCacheUri = "https://filecache.hoccer.com/v3";
     }
     
     public static void useExperimentalServers() {
         mLinccerUri = "https://linccer-experimental.hoccer.com/v3";
         mFileCacheUri = "https://filecache-experimental.hoccer.com/v3";
     }
     
     public static void useDevelopmentServers() {
         mLinccerUri = "https://linccer-development.hoccer.com/v3";
         mFileCacheUri = "https://filecache-development.hoccer.com/v3";
     }
 
     public static void useTestingServers() {
        mLinccerUri = "http://linccer-testing.hoccer.com/v3";
        mFileCacheUri = "http://filecache-testing.hoccer.com/v3";
     }
 
     public static void useSpecialServers(String ip, String port) {
         mLinccerUri = "http://" + ip + ":" + port + "/v3";
         mFileCacheUri = "http://" + ip + ":" + port + "/v3";
     }
 
     private void useDemoApiKey() {
         mApiKey = "e101e890ea97012d6b6f00163e001ab0";
         mSharedSecret = "JofbFD6w6xtNYdaDgp4KOXf/k/s=";
     }
 
     public static String getLinccerBaseUri() {
         return mLinccerUri;
     }
 
     public String getApplicationName() {
         return mApplicationName;
     }
 
     public String getClientUri() {
         return ClientConfig.getLinccerBaseUri() + "/clients/" + getClientId();
     }
 
     public JSONObject toJson() throws JSONException {
         JSONObject json = new JSONObject();
         json.put("application", getApplicationName());
         return json;
     }
 
     public String getApiKey() {
         return mApiKey;
     }
 
     public String getSharedSecret() {
         return mSharedSecret;
     }
 
     public UUID getClientId() {
         return mClientId;
     }
 
     public String getPublicKeyString() {
         return mPublicKey;
     }
 
     public void setPublicKeyString(String thePublicKeyString) {
         mPublicKey = thePublicKeyString;
     }
 
     public static String getFileCacheBaseUri() {
         return mFileCacheUri;
     }
 
 }
