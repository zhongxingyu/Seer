 /*
  * Copyright (C) 2012 Eyal LEZMY (http://www.eyal.fr)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package fr.eyal.lib.data.processor;
 
 import java.util.ArrayList;
 
 import org.apache.http.Header;
 
 import android.content.Context;
 import fr.eyal.lib.data.communication.rest.AndroidHttpClient;
 import fr.eyal.lib.data.communication.rest.HttpDelete;
 import fr.eyal.lib.data.communication.rest.HttpGet;
 import fr.eyal.lib.data.communication.rest.HttpHead;
 import fr.eyal.lib.data.communication.rest.HttpPost;
 import fr.eyal.lib.data.communication.rest.HttpPut;
 import fr.eyal.lib.data.communication.rest.HttpRequest;
 import fr.eyal.lib.data.communication.rest.HttpResponse;
 import fr.eyal.lib.data.model.ResponseBusinessObject;
 import fr.eyal.lib.data.parser.GenericHandler;
 import fr.eyal.lib.data.parser.GenericParser;
 import fr.eyal.lib.data.service.model.BusinessResponse;
 import fr.eyal.lib.data.service.model.DataLibRequest;
 import fr.eyal.lib.data.service.model.DataLibResponse;
 import fr.eyal.lib.util.Out;
 
 /**
  * @author Eyal LEZMY
  */
 public class Processor {
 
     private static final String CONNECTIVITY_ERROR_MESSAGE = "Network is not accesible.";
 
     private static final String TAG = "Processor";
 
     protected ProcessorResponseHandler mHandler;
     protected ArrayList<Header> mHeaders;
 
     public Processor(final ProcessorResponseHandler handler, final ArrayList<Header> cookies) {
         mHandler = handler;
         mHeaders = cookies;
     }
 
     /**
      * Start to reach and process the data to get a {@link ResponseBusinessObject}
      * 
      * @param request the request information
      * @param isConnected define is the device can access to the network
      * @param handler the object the will handle the parsing
      * @param context
      */
     public void start(final DataLibRequest request, final boolean isConnected, final GenericHandler handler, final Context context) {
         Out.d(TAG, "startProcessor " + isConnected);
 
         //we create the response to send at the end of this function
         final DataLibResponse response = new DataLibResponse();
         response.request = request;
 
         //if the device have an active network connection
         if (isConnected) {
 
             try {
                 //we send the request on the network and get the result
                 final HttpResponse result = doNetwork(request, context);
 
                 //we clear the cookie manager id needed 
                 if (!request.isConservingTheCookies())
                     AndroidHttpClient.flushCookieManager();
 
                 response.returnCode = result.getStatus();
 
                 ResponseBusinessObject businessObjectNetwork = null;
 
                 //we parse the result to get a BusinessResponse
                 final GenericParser parser = new GenericParser(handler);
 
                 parser.parseSheet(result.getBody(), request.parseType);
 
                 //we finish to fill the BusinessObject and save it
                 businessObjectNetwork = handler.getParsedData();
 
                 //if we have to save the result
                 if (request.isDatabaseCacheEnabled())
                     businessObjectNetwork.save(request);
 
                 //we build the response to return
                 response.headers = result.getHeaders();
                 response.status = BusinessResponse.STATUS_OK;
                 response.response = businessObjectNetwork;
 
             } catch (final Exception e) {
                 e.printStackTrace();
                 response.status = BusinessResponse.STATUS_ERROR;
                 response.statusMessage = e.getMessage();
             }
 
         } else {
             response.status = BusinessResponse.STATUS_ERROR;
             response.statusMessage = CONNECTIVITY_ERROR_MESSAGE;
         }
         //we return the response
         mHandler.handleProcessorResponse(response);
     }
 
     /**
      * Process the network request. This method implements the REST, SOAP, or other network processing
      * For now, it implements only REST requests
      * 
      * @param request the request to send
      * @param context Context of execution 
      * 
      * @return
      */
     public HttpResponse doNetwork(final DataLibRequest request, final Context context) {
         //TODO manage other kinds of network requests (SOAP, ...)
 
         AndroidHttpClient httpClient = new AndroidHttpClient(request.url);
         HttpRequest httpRequest = null;
 
         //we create the request, depending on the method
         switch (request.requestMethod) {
 
             case DataLibRequest.HTTP_REST_DELETE:
                 httpRequest = new HttpDelete(request.path, request.params);
                 break;
 
             case DataLibRequest.HTTP_REST_GET:
                 httpRequest = new HttpGet(request.path, request.params);
                 break;
 
             case DataLibRequest.HTTP_REST_HEAD:
                 httpRequest = new HttpHead(request.path, request.params);
                 break;
 
             case DataLibRequest.HTTP_REST_POST:
                 if (request.data != null && request.data.length > 0)
                     httpRequest = new HttpPost(request.path, request.params, request.contentType, request.data);
                 else
                     httpRequest = new HttpPost(request.path, request.params);
                 break;
 
             case DataLibRequest.HTTP_REST_PUT:
                 if (request.data != null && request.data.length > 0)
                     httpRequest = new HttpPut(request.path, request.params, request.contentType, request.data);
                 else
                     httpRequest = new HttpPut(request.path, request.params);
                 break;
 
             default:
                 httpRequest = new HttpGet(request.path, request.params);
                 break;
         }
         HttpResponse httpResponse = httpClient.execute(httpRequest);
        Out.d(TAG, httpResponse.getBodyAsString());
         return httpResponse;
     }
 }
