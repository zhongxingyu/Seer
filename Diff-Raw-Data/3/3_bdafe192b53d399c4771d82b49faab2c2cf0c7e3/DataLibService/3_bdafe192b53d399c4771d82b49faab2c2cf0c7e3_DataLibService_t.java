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
 
 package fr.eyal.lib.data.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.apache.http.Header;
 import org.apache.http.client.CookieStore;
 import org.apache.http.cookie.CookieSpecRegistry;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.ResultReceiver;
 import android.util.Log;
 import fr.eyal.lib.data.model.BusinessObjectDAO;
 import fr.eyal.lib.data.model.ResponseBusinessObjectDAO;
 import fr.eyal.lib.data.processor.ProcessorResponseHandler;
 import fr.eyal.lib.data.service.model.BusinessResponse;
 import fr.eyal.lib.data.service.model.ComplexOptions;
 import fr.eyal.lib.data.service.model.DataLibRequest;
 import fr.eyal.lib.data.service.model.DataLibResponse;
 import fr.eyal.lib.service.MultiThreadService;
 import fr.eyal.lib.util.FileManager;
 import fr.eyal.lib.util.Out;
 
 @TargetApi(Build.VERSION_CODES.CUPCAKE)
 public abstract class DataLibService extends MultiThreadService implements ProcessorResponseHandler {
 
     private static final String TAG = "DataLibService";
 
     protected static final int MAX_THREADS = 3;
 
     //different Service's possible actions
     public static final int COOKIES_FLUSH = 999999999;
 
     
     //different information recieved by the Service
     /**
      * Intext extra content : Webservice type
      */
     public static final String INTENT_EXTRA_PROCESSOR_TYPE = "processorType";
     /**
      * Intext extra content : Useragent to send
      */
     public static final String INTENT_EXTRA_USER_AGENT = "userAgent";
     /**
      * Intext extra content : Webservice URL
      */
     public static final String INTENT_EXTRA_URL = "url";
     /**
      * Intext extra content : parameters to send
      */
     public static final String INTENT_EXTRA_PARAMS = "params";
     /**
      * Intext extra content : Webservice path
      */
     public static final String INTENT_EXTRA_PATH = "path";
     /**
      * Intext extra content : web service data
      */
     public static final String INTENT_EXTRA_DATA = "data"; 
     /**
      * Intext extra content : content type
      */
     public static final String INTENT_EXTRA_CONTENT_TYPE = "contentType"; 
     /**
      * Intext extra content : options to add to the request
      */
     public static final String INTENT_EXTRA_REQUEST_OPTION = "optionRequest";
     /**
      * Intext extra content : parsing type of the response
      */
     public static final String INTENT_EXTRA_PARSE_TYPE = "parseType";
     /**
      * Intext extra content : complex option for the request
      */
     public static final String INTENT_EXTRA_COMPLEX_OPTIONS = "complexOptions";
 
     /**
      * title of the cookies' header
      */
     protected static final String COOKIE_HEADER_TITLE = "Set-Cookie";
 
 
 
     protected ArrayList<Header> mHeaders; //Headers to send at every client request with NetworkConnection
     public static CookieStore mCookies = null; //Headers to send at every client request with NetworkConnection
     public static CookieSpecRegistry mCookiesSpec = null; //Headers returned by the server
 
     /**
      * Connectivity Manager to access to the network configuration
      */
     protected ConnectivityManager mConnectivityManager;
 
     public DataLibService() {
         super(MAX_THREADS);
         mHeaders = new ArrayList<Header>();
     }
 
     public DataLibService(final int maxThreads) {
         super(maxThreads);
     }
 
     @Override
     public void onCreate() {
         mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         //we initialize the FileManager
         FileManager.getInstance(getApplicationContext());
         super.onCreate();
     }
 
     @SuppressWarnings("unchecked")
 	@Override
     protected void onHandleIntent(final Intent intent) {
         final int processorType = intent.getIntExtra(INTENT_EXTRA_PROCESSOR_TYPE, -1);
 
         Out.d(TAG, "onHandleIntent");
 
         //		String userAgent = intent.getStringExtra(INTENT_EXTRA_USER_AGENT);
 
         final DataLibRequest request = new DataLibRequest();
         DataLibWebConfig.applyToRequest(request, DataLibWebConfig.getInstance()); //we apply a default configuration for the request
 
         //we get the action data
         request.url = intent.getStringExtra(INTENT_EXTRA_URL);
         request.params = intent.getParcelableExtra(INTENT_EXTRA_PARAMS);
         request.parseType = intent.getIntExtra(INTENT_EXTRA_PARSE_TYPE, DataLibRequest.PARSE_TYPE_SAX_XML);
 
         //we eventually add the complex options
         Object complexOptions = intent.getSerializableExtra(INTENT_EXTRA_COMPLEX_OPTIONS);
         if(complexOptions instanceof ComplexOptions)
         	request.complexOptions = (ComplexOptions) complexOptions;  
         
         request.context = getApplicationContext();
         //we get the options to apply
         int option = intent.getIntExtra(INTENT_EXTRA_REQUEST_OPTION, DataLibRequest.OPTION_NO_OPTION);
         DataLibWebConfig.applyToRequest(request, option, true);
 
         //we add the intent
         request.intent = intent;
 
         try {
 
             if (processorType == COOKIES_FLUSH)
                 this.flushCookies();
 
             else {
                 //we launch the processor on a daughter class
                 launchProcessor(processorType, request);
             }
 
         } catch (final Exception e) {
             Out.e(TAG, "Erreur", e);
             final BusinessResponse response = new BusinessResponse();
             response.status = BusinessResponse.STATUS_ERROR;
             response.statusMessage = Log.getStackTraceString(e);
             sendResult(request, response, response.status);
         }
 
     }
 
     /**
      * Function that flush the cookies stored inside the headers ArrayList
      */
     private void flushCookies() {
         for (final Header header : mHeaders) {
             if (header.getName().equalsIgnoreCase(COOKIE_HEADER_TITLE))
                 mHeaders.remove(header);
         }
     }
 
     /**
      * Send the result of a request to the linked {@link ServiceHelper}
      * 
      * @param request the request
      * @param response the response
      * @param code the status of the request
      */
     protected void sendResult(final DataLibRequest request, final BusinessResponse response, final int code) {
         Out.d(TAG, "sendResult");
 
         final Intent intent = request.intent;
         final ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra(INTENT_EXTRA_RECEIVER);
 
         if (receiver != null) {
             final Bundle b = new Bundle();
 
             if (response != null && response.response != null) {
 
                 //if the Business Object have to be transmit inside the Bundle
                 if (request.isParcelableMethodEnabled())
                     b.putParcelable(ServiceHelper.RECEIVER_EXTRA_RESULT, response.response);
 
                 //we add the request id to the response
                 if (response.response instanceof ResponseBusinessObjectDAO) {
                     ResponseBusinessObjectDAO r = (ResponseBusinessObjectDAO) response.response;
                     b.putLong(ServiceHelper.RECEIVER_EXTRA_RESULT_ID, r._id);
                 } else {
                     //in case of no data cache, we set an invalid ID
                     b.putLong(ServiceHelper.RECEIVER_EXTRA_RESULT_ID, BusinessObjectDAO.ID_INVALID);
                 }
 
             } else {
                 Out.e(TAG, "Unfined response");
             }
 
             //we copy the content of the response in the intent's bundle
             b.putInt(ServiceHelper.RECEIVER_EXTRA_REQUEST_ID, intent.getIntExtra(INTENT_EXTRA_REQUEST_ID, -1));
             b.putInt(ServiceHelper.RECEIVER_EXTRA_WEBSERVICE_TYPE, intent.getIntExtra(INTENT_EXTRA_PROCESSOR_TYPE, -1));
             b.putInt(ServiceHelper.RECEIVER_EXTRA_RETURN_CODE, response.returnCode);
             b.putInt(ServiceHelper.RECEIVER_EXTRA_RESULT_CODE, response.status);
            b.putString(ServiceHelper.RECEIVER_EXTRA_RESULT_MESSAGE, response.statusMessage);
            b.putParcelable(ServiceHelper.RECEIVER_EXTRA_REQUEST, request);
 
             receiver.send(code, b);
         }
     }
 
     @Override
     public void handleProcessorResponse(final DataLibResponse response) {
 
         final DataLibRequest request = response.request;
 
         //we add the cookies to the service's headers
         if (request.isConservingTheCookies()) {
 
             //TODO CHECK THE COOKIE CONSERVATION
             //            final Header[] headers = response.headers;
             //
             //            if (headers != null) {
             //                for (final Header header : headers) {
             //                    //if we have to keep the cookie
             //                    if (request.isConservingTheCookies() && header.getName().equalsIgnoreCase(COOKIE_HEADER_TITLE))
             //                        mHeaders.add(header);
             //                }
             //            }
         }
         sendResult(request, response, response.status);
     }
 
     /**
      * Function that implements the instantiation and the launching of the processor process. i-e. network requesting, parsing, ... and returning the
      * {@link DataLibResponse} through {@link handleProcessorResponse} in the {@link DataLibService} class
      * 
      * @param processorType type of response to handle
      * @param request Request passed to the Service
      */
     public abstract void launchProcessor(final int processorType, final DataLibRequest request);
 }
