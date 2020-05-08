 /* Copyright (c) 2008 Google Inc.
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
 
 
 package org.opensocial.client;
 
 import net.oauth.OAuthException;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 /**
  * An object representing a collection of OpenSocial requests. Instances may be
  * instantiated at the top level; individual requests are created via static
  * methods of OpenSocialClient and added to the batch request with the add
  * method. The requests are then submitted to the container with the send
  * method.
  *
  * @author Jason Cooper
  */
 public class OpenSocialBatch {
 
   private List<OpenSocialRequest> requests;
 
   public OpenSocialBatch() {
     this.requests = new Vector<OpenSocialRequest>();
   }
 
   /**
    * Sets the id property of the passed request and adds it to the internal
    * collection.
    *
    * @param request OpenSocialRequest object to add to internal collection
    * @param id Request label, used to extract data from OpenSocialResponse
    *           object returned
    */
   public void addRequest(OpenSocialRequest request, String id) {
     request.setId(id);
     this.requests.add(request);
   }
 
   /**
    * Calls one of two private methods which submit the queued requests to the
    * container given the properties of the OpenSocialClient object passed in.
    *
    * @param  client OpenSocialClient object with REST_BASE_URI or RPC_ENDPOINT
    *                properties set
    * @return Object encapsulating the data requested from the container
    * @throws OpenSocialRequestException
    * @throws IOException
    */
   public OpenSocialResponse send(OpenSocialClient client)
       throws OpenSocialRequestException, IOException {
 
     if (this.requests.size() == 0) {
       throw new OpenSocialRequestException(
           "One or more requests must be added before sending batch");
     }
 
     String rpcEndpoint =
       client.getProperty(OpenSocialClient.Properties.RPC_ENDPOINT);
     String restBaseUri =
       client.getProperty(OpenSocialClient.Properties.REST_BASE_URI);
 
    String urlRegEx = "([A-Za-z][A-Za-z0-9+.-]{1,120}:[A-Za-z0-9/]" +
        "(([A-Za-z0-9$_.+!*,;/?:@&~=-])|%[A-Fa-f0-9]{2}){1,333}" + 
        "(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*,;/?:@&~=%-]{0,1000}))?)";

     if (rpcEndpoint == null && restBaseUri == null) {
       throw new OpenSocialRequestException(
           "REST base URI or RPC endpoint required");
    } else if (!rpcEndpoint.matches(urlRegEx) &&
        !restBaseUri.matches(urlRegEx)) {
      throw new OpenSocialRequestException(
          "REST base URI and RPC endpoint must be valid URLs");
     }
 
    if (rpcEndpoint != null && rpcEndpoint.matches(urlRegEx)) {
       return this.submitRpc(client);
     } else {
       return this.submitRest(client);
     }
   }
 
   /**
    * Collects all of the queued requests and encodes them into a single JSON
    * string before creating a new HTTP request, attaching this string to the
    * request body, signing it, and sending it to the container.
    *
    * @param  client OpenSocialClient object with RPC_ENDPOINT property set
    * @return Object encapsulating the data requested from the container
    * @throws OpenSocialRequestException
    * @throws IOException
    */
   private OpenSocialResponse submitRpc(OpenSocialClient client)
       throws OpenSocialRequestException, IOException {
 
     String rpcEndpoint =
       client.getProperty(OpenSocialClient.Properties.RPC_ENDPOINT);
 
     JSONArray requestArray = new JSONArray();
     for (OpenSocialRequest r : this.requests) {
       try {
         requestArray.put(new JSONObject(r.toJson()));
       } catch (org.json.JSONException e) {
         throw new OpenSocialRequestException(
             "Invalid JSON object string " + r.toJson());
       }
     }
 
     OpenSocialUrl requestUrl = new OpenSocialUrl(rpcEndpoint);
 
     OpenSocialHttpRequest request = new OpenSocialHttpRequest("POST", requestUrl);
     request.setBody(requestArray.toString());
 
     OpenSocialOAuthClient.signRequest(request, client);
 
     String responseString = request.execute();
 
     String debug = client.getProperty(OpenSocialClient.Properties.DEBUG);
     if (debug != null && !debug.equals("")) {
       System.out.println("Request URL:\n" + request.getUrl().toString());
       System.out.println("Request body:\n" + request.getBody());
       System.out.println("Response:\n" + responseString);
     }
 
     return OpenSocialJsonParser.getResponse(responseString);
   }
 
   /**
    * Retrieves the first request in the queue and builds the full REST URI
    * given the properties of this request before creating a new HTTP request,
    * signing it, and sending it to the container.
    *
    * @param  client OpenSocialClient object with REST_BASE_URI property set
    * @return Object encapsulating the data requested from the container
    * @throws OpenSocialRequestException
    * @throws IOException
    */
   private OpenSocialResponse submitRest(OpenSocialClient client)
       throws OpenSocialRequestException, IOException {
 
     String restBaseUri =
       client.getProperty(OpenSocialClient.Properties.REST_BASE_URI);
 
     OpenSocialRequest r = this.requests.get(0);
 
     OpenSocialUrl requestUrl = new OpenSocialUrl(restBaseUri);
     requestUrl.addPathComponent(r.getRestPathComponent());
 
     if (r.hasParameter("userId")) {
       requestUrl.addPathComponent((String) r.popParameter("userId"));
     }
     if (r.hasParameter("groupId")) {
       requestUrl.addPathComponent((String) r.popParameter("groupId"));
     }
     if (r.hasParameter("appId")) {
       requestUrl.addPathComponent((String) r.popParameter("appId"));
     }
 
     String method = r.getRestMethod();
     OpenSocialHttpRequest request = new OpenSocialHttpRequest(method, requestUrl);
     
     if (method.equals("PUT")  || method.equals("POST")) {
       if (r.hasParameter("data")) {
         request.setBody(r.popParameter("data"));
       }
     }
 
     Set<Map.Entry<String, OpenSocialRequestParameter>> parameters = r.getParameters();
     for (Map.Entry<String, OpenSocialRequestParameter> entry : parameters) {
       requestUrl.addQueryStringParameter(entry.getKey(), entry.getValue().getValuesString());
     }
 
     OpenSocialOAuthClient.signRequest(request, client);
 
     String debug = client.getProperty(OpenSocialClient.Properties.DEBUG);
     if (debug != null && !debug.equals("")) {
       System.out.println("Request URL:\n" + request.getUrl().toString());
       System.out.println("Request body:\n" + request.getBody());
     }
 
     String responseString = request.execute();
 
     if (debug != null && !debug.equals("")) {
       System.out.println("Response:\n" + responseString);
     }
 
     return OpenSocialJsonParser.getResponse(responseString, r.getId());
   }
 }
