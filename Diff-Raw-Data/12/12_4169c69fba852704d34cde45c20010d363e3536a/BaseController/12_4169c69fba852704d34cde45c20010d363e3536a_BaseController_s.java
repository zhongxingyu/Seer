 /*
  * Copyright (c) 2011 Imaginea Technologies Private Ltd.
  * Hyderabad, India
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.imaginea.mongodb.controllers;
 
 import com.imaginea.mongodb.exceptions.ApplicationException;
 import com.imaginea.mongodb.exceptions.CollectionException;
 import com.imaginea.mongodb.exceptions.DatabaseException;
 import com.imaginea.mongodb.exceptions.DocumentException;
 import com.imaginea.mongodb.exceptions.ErrorCodes;
 import com.imaginea.mongodb.exceptions.InvalidHTTPRequestException;
 import com.imaginea.mongodb.exceptions.MongoHostUnknownException;
 import com.imaginea.mongodb.exceptions.ValidationException;
 import com.imaginea.mongodb.services.AuthService;
 import com.imaginea.mongodb.services.impl.AuthServiceImpl;
 import com.imaginea.mongodb.utils.ApplicationUtils;
 import com.mongodb.MongoException;
 import com.mongodb.MongoInternalException;
 import org.apache.log4j.Logger;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.net.UnknownHostException;
 import java.util.Set;
 
 /**
  * Defines validation functions for validating dbInfo from session. An error
  * JSON object is returned when dbInfo is found invalid.
  *
  * @author Rachit Mittal
  */
 public class BaseController {
 
     protected static final AuthService authService = AuthServiceImpl.getInstance();
 
     /**
      * To identify the HTTP Request type made to the request dispatchers.
      */
     protected static enum RequestMethod {
         GET, POST, PUT, DELETE
     }
 
     /**
      * Validates connectionId with the connectionId Array present in session.
      *
      * @param connectionId  Mongo Db config information provided to user at time of login.
      * @param logger  Logger to write error message to
      * @param request Request made by client containing session attributes.
      * @return null if connectionId is valid else error object.
      */
     protected static String validateConnectionId(String connectionId, Logger logger, HttpServletRequest request) {
 
         HttpSession session = request.getSession();
         Set<String> existingConnectionIdsInSession = (Set<String>) session.getAttribute("existingConnectionIdsInSession");
         if(existingConnectionIdsInSession == null) {
             InvalidHTTPRequestException e = new InvalidHTTPRequestException(ErrorCodes.INVALID_SESSION, "Invalid Session");
             return formErrorResponse(logger, e);
         }
 
         String response = null;
         if (connectionId == null || !existingConnectionIdsInSession.contains(connectionId)) {
             InvalidHTTPRequestException e = new InvalidHTTPRequestException(ErrorCodes.INVALID_CONNECTION, "Invalid Connection");
             return formErrorResponse(logger, e);
         }
         return response;
     }
 
     /**
      * Returns the JSON error response after an invalid dbInfo is found.
      *
      * @param logger Logger to log the error response.
      * @return JSON Error response.
      */
     protected static String formErrorResponse(Logger logger, ApplicationException e) {
 
         String response = null;
         JSONObject jsonErrorResponse = new JSONObject();
         JSONObject error = new JSONObject();
         try {
             error.put("message", e.getMessage());
             error.put("code", e.getErrorCode());
            logger.error(error);
 
             JSONObject tempResponse = new JSONObject();
             tempResponse.put("error", error);
             jsonErrorResponse.put("response", tempResponse);
             response = jsonErrorResponse.toString();
         } catch (JSONException m) {
             logger.error(m);
             response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\"," + "\"message\": \"Error while forming JSON Object\"}";
         }
         return response;
     }
 
     /**
      * Catches error for a block of code and from JSON error Response.
      */
     protected static class ErrorTemplate {
         public static String execute(Logger logger, ResponseCallback callback) {
             return execute(logger, callback, true);
         }
 
         public static String execute(Logger logger, ResponseCallback callback, boolean wrapResult) {
             String response = null;
             Object dispatcherResponse = null;
             try {
                 dispatcherResponse = callback.execute();
                 if (wrapResult) {
                     JSONObject tempResult = new JSONObject();
                     JSONObject jsonResponse = new JSONObject();
                     try {
                         tempResult.put("result", dispatcherResponse);
                         jsonResponse.put("response", tempResult);
                         response = ApplicationUtils.serializeToJSON(jsonResponse);
                     } catch (JSONException e) {
                         logger.error(e);
                         response = "{\"code\":" + "\"" + ErrorCodes.JSON_EXCEPTION + "\"," + "\"message\": \"Error while forming JSON Object\"}";
                     }
                 } else if (dispatcherResponse instanceof JSONObject) {
                     response = ((JSONObject) dispatcherResponse).toString();
                 } else if (dispatcherResponse instanceof JSONArray) {
                     response = ((JSONArray) dispatcherResponse).toString();
                 } else if (dispatcherResponse instanceof String) {
                     response = dispatcherResponse.toString();
                 }
             } catch (NumberFormatException m) {
                 ApplicationException e = new ApplicationException(ErrorCodes.ERROR_PARSING_PORT, "Invalid Port", m.getCause());
                 response = formErrorResponse(logger, e);
             } catch (IllegalArgumentException m) {
                 // When port out of range
                 ApplicationException e = new ApplicationException(ErrorCodes.INVALID_ARGUMENT, m.getMessage(), m.getCause());
                 response = formErrorResponse(logger, e);
             } catch (UnknownHostException m) {
                 MongoHostUnknownException e = new MongoHostUnknownException("Unknown host", m);
                 response = formErrorResponse(logger, e);
             } catch (MongoInternalException m) {
                 // Throws when cannot connect to localhost.com
                 MongoHostUnknownException e = new MongoHostUnknownException("Unknown host", m);
                 response = formErrorResponse(logger, e);
             } catch (MongoException m) {
                 MongoHostUnknownException e = new MongoHostUnknownException("Unknown host", m);
                 response = formErrorResponse(logger, e);
             } catch (DatabaseException e) {
                 response = formErrorResponse(logger, e);
             } catch (CollectionException e) {
                 response = formErrorResponse(logger, e);
             } catch (DocumentException e) {
                 response = formErrorResponse(logger, e);
             } catch (ValidationException e) {
                 response = formErrorResponse(logger, e);
             } catch (ApplicationException e) {
                 response = formErrorResponse(logger, e);
             } catch (Exception m) {
                ApplicationException e = new ApplicationException(ErrorCodes.ANY_OTHER_EXCEPTION, m.getMessage(), m.getCause());
                 response = formErrorResponse(logger, e);
             }
             return response;
         }
     }
 
     protected static class ResponseTemplate {
 
         public String execute(Logger logger, String connectionId, HttpServletRequest request, ResponseCallback callback) {
             return execute(logger, connectionId, request, callback, true);
         }
 
         public String execute(Logger logger, String connectionId, HttpServletRequest request, ResponseCallback callback, boolean wrapResult) {
             // Validate first
             String response = validateConnectionId(connectionId, logger, request);
             if (response != null) {
                 return response;
             }
             return ErrorTemplate.execute(logger, callback, wrapResult);
         }
     }
 
     protected interface ResponseCallback {
         public Object execute() throws Exception;
         // object can be collection
     }
 }
