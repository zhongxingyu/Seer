 /*******************************************************************************
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
  *
  ******************************************************************************/
 package org.spiffyui.spiffyforms.server;
 
 import java.text.DateFormat;
 import java.util.Iterator;
 import java.util.Date;
 import java.util.HashMap;
 
 
 import javax.ws.rs.GET;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Produces;
 import javax.ws.rs.Path;
 
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.WebApplicationException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 // The Java class will be hosted at the URI path "/users"
 @Path("/users/{arg1}")
 public class User {
     static final String RESULT_SUCCESS = "{\"result\" : \"success\"}";
 
     @Context UriInfo uriInfo;
 
     // this Java method finds a particular user in the list of users
     static JSONObject findUserInArray(String userID){
 	JSONArray users = Users.getUserList();
 	if (users == null)
 	    return null;
 
 	JSONObject user = null;
 	int len = users.length();
 	try {
 	    for (int i=0; i<len; i++) {
 		user = users.getJSONObject(i);
 
 		if (user != null){
 		    String id = user.getString("userID");
 		    if (userID.equals(id)){
 			// found it!
 			return user;
 		    }
 		}
 	    }
 	} catch (JSONException je){
 	    // not going to happen in this demo app
 	}
 		    
 	// if we got here then we didn't find it
 	return null;
     }
 
     // this Java method finds a particular user in the list of users
     static int findUserIndexInArray(String userID){
 	JSONArray users = Users.getUserList();
 	if (users == null)
 	    return -1;
 
 	JSONObject user = null;
 	int len = users.length();
 	try {
 	    for (int i=0; i<len; i++) {
 		user = users.getJSONObject(i);
 
 		if (user != null){
 		    String id = user.getString("userID");
 		    if (userID.equals(id)){
 			// found it!
 			return i;
 		    }
 		}
 	    }
 	} catch (JSONException je){
 	    // not going to happen in this demo app
 	}
 		    
 	// if we got here then we didn't find it
 	return -1;
     }
 
 
 
     // The Java method will process HTTP GET requests
     @GET 
     // The Java method will produce content identified by the MIME Media
     // type "application/JSON"
     @Produces("application/json")
     // This method returns a JSONObject containing the user info 
     // for the userID passed in the arg1 parameter on the URL
    public Response getUserInfo() {
         MultivaluedMap<String, String> params = uriInfo.getPathParameters();
         String userid = params.getFirst("arg1");
 	
 	if (userid == null){
 	    throw new WebApplicationException(400);
 	}
 
 	JSONObject user = findUserInArray(userid);
 	if (user == null) {
 	    try {
 		JSONObject reason = new JSONObject();
 		reason.put("Text", "User id \""+ userid+"\" not found");
 
 		JSONObject subcode = new JSONObject();
 		subcode.put("Value", "0");
 	    
 		JSONObject code = new JSONObject();
 		code.put("Subcode", subcode);
 		code.put("Value", Response.Status.NOT_FOUND);
 	    
 		JSONObject fault = new JSONObject();
 		fault.put("Code", code);
 		fault.put("Reason", reason);
 
 		Response.ResponseBuilder rb = Response.status(Response.Status.NOT_FOUND);
 		rb.entity(fault.toString());
 		Response response = rb.build();
 
 		throw new WebApplicationException(response);
 	    } catch (JSONException je){
 		// this is extremely unlikely to happen with the static data used here.
 		throw new WebApplicationException(500);
 	    }
 	}
 
 	Response.ResponseBuilder rb = Response.created();
 	rb.entity(user.toString());
 	return rb.build();
     }
 
 
     @POST 
     // The Java method will produce content identified by the MIME Media
     // type "application/JSON"
     @Produces("application/JSON")
     // This method attempts to create new user info based on the info 
     // in the input string
     public String createUser(String input) {
         MultivaluedMap<String, String> params = uriInfo.getPathParameters();
         String userID = params.getFirst("arg1");
 	// we know that userID is not null because of the Path annotation 
 
 	// do we already have this user? 
 	if (findUserInArray(userID) != null){
 	    throw new WebApplicationException(Response.Status.BAD_REQUEST);
 	}
 
 	try {
 	    JSONArray userList = Users.getUserList();
 	    if (userList != null){
 		userList.put(new JSONObject(input));
 	    }		    
 	} catch (JSONException je){
 	    // input string was probably not correctly formatted JSON.
 	    // we could perhaps be more informative here.
 	    throw new WebApplicationException(Response.Status.BAD_REQUEST);
 	}
 
 	return RESULT_SUCCESS;
     }
 
     @PUT 
     // Modify the information stored for a given user. 
     // The Java method will produce content identified by the MIME Media
     // type "application/JSON"
     @Produces("application/json")
     @Consumes("application/json")
     public String updateUser(String input) {
         MultivaluedMap<String, String> params = uriInfo.getPathParameters();
         String userID = params.getFirst("arg1");
 	// we know userID is non-null
 	JSONObject storedUser = findUserInArray(userID);
 	if (storedUser != null){
 	    try {
 		JSONObject inputUser = new JSONObject(input);
 		Iterator iter = inputUser.keys();
 		while (iter.hasNext()){
 		    String key = (String)iter.next();
 		    storedUser.put(key, inputUser.get(key));
 		}
 	    } catch (JSONException je){
 		// I don't know what could make this happen
 		throw new WebApplicationException(Response.Status.BAD_REQUEST);
 	    }
 	} else {
 	    throw new WebApplicationException(Response.Status.NOT_FOUND);
 	}
 	return RESULT_SUCCESS;
     }
 
     @DELETE 
     // The Java method will produce content identified by the MIME Media
     // type "application/JSON"
     @Produces("application/JSON")
     public String deleteUser(String input) {
         MultivaluedMap<String, String> params = uriInfo.getPathParameters();
         String userID = params.getFirst("arg1");
 	// we know userID is non-null
 	int i = findUserIndexInArray(userID);
 	if (i != -1){ // -1 means not found
 	    JSONArray userList = Users.getUserList();
 	    try {
 		userList.put(i, (Object) null);
 	    } catch (JSONException e) {
 		e.printStackTrace();
 	    }
 	    return "{\"success\":true}";
 	} else {
 	    throw new WebApplicationException(Response.Status.NOT_FOUND);
 	}
     }
 }
