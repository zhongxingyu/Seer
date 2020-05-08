 /* Copyright (c) 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.opensocial.providers;
 
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.opensocial.client.OpenSocialHttpResponseMessage;
 import org.opensocial.client.OpenSocialRequest;
 
 public class MySpaceProvider extends OpenSocialProvider {
   
   public MySpaceProvider() {
     super();
     
     requestTokenUrl = "http://api.myspace.com/request_token";
     authorizeUrl = "http://api.myspace.com/authorize";
     accessTokenUrl = "http://api.myspace.com/access_token";
     restEndpoint = "http://opensocial.myspace.com/roa/09";
     providerName = "MySpace";
     signBodyHash = false;
     isOpenSocial = true;
   }
   
   public void preRequest(OpenSocialRequest request) {
     
   }
   
   public void postRequest(OpenSocialRequest request, 
       OpenSocialHttpResponseMessage response) {
     
     try{
       _fixStatusLink(request, response);
       _fixModelContainer(request, response);
     }catch(JSONException e) {
       e.printStackTrace();
     }
   }
   
   private void _fixModelContainer(OpenSocialRequest request, 
       OpenSocialHttpResponseMessage response) throws JSONException {
     HashMap<String, String> rules = new HashMap<String, String>();
     
     rules.put("groups", "group");
     rules.put("people", "person");
     rules.put("albums", "album");
     rules.put("mediaItems", "mediaItem");
     rules.put("activities", "activity");
     rules.put("appdata", "userAppData");
     
     String service = request.getRestPathComponent();
     String model = rules.get(service);
     String data = response.getOpenSocialDataString();
     
     if(data.startsWith("{")) {
       JSONObject obj = new JSONObject(data);
       
       if(obj.has("entry") ){
         if(obj.getString("entry").startsWith("[")) {
           JSONArray tmp = new JSONArray();
           JSONArray entry = obj.getJSONArray("entry");
           int length = entry.length();
           
           for(int i=0; i< length; i++) {
             tmp.put(entry.getJSONObject(i).getJSONObject(model));
           }
           
           obj.put("entry", tmp);
           response.setOpenSocialDataString(obj.toString());
         }
       }else if(obj.has(model)) {
         obj.put("entry", obj.getJSONObject(model));
         obj.remove(model);
         response.setOpenSocialDataString(obj.toString());
       }
     }
   }
   
   private void _fixStatusLink(OpenSocialRequest request, 
       OpenSocialHttpResponseMessage response) throws JSONException {
   
     if(response.getStatusCode() <= 201 && 
         (request.getRestMethod().equals("POST") || 
             request.getRestMethod().equals("PUT"))) {
       
       JSONObject data = new JSONObject(response.getOpenSocialDataString());
       
       if(data.has("statusLink")) {
         JSONObject tmp = new JSONObject();
         
         String sl = data.getString("statusLink");
         tmp.put("id", sl.substring(sl.lastIndexOf('/')+1));
         
         data.put("entry", tmp);
         data.remove("statusLink");
         response.setOpenSocialDataString(data.toString());
       }
     }
   }
 }
