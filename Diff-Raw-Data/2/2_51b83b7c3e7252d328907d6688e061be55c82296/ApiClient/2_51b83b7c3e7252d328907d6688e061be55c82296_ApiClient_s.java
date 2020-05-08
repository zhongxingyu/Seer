 package com.moac.android.soundmap.api;
 
 import android.util.Log;
 
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.google.gson.Gson;
 
 public class ApiClient {
 
     private static final String TAG = ApiClient.class.getSimpleName();
 
     private final String hostDomain;
     private final String hostScheme;
     private final String clientId;
 
     private Gson gson;
     private RequestQueue requestQueue;
 
     public ApiClient(RequestQueue requestQueue, Gson gson, String hostScheme, String hostDomain, String clientId) {
         this.requestQueue = requestQueue;
         this.gson = gson;
         this.hostScheme = hostScheme;
         this.hostDomain = hostDomain;
         this.clientId = clientId;
     }
 
     public <T> void execute(ApiRequest<T> request, Response.Listener<T> okListener, Response.ErrorListener errorListener) {
 
         // Add required header
         if (!request.getQueryMap().containsKey(ApiConst.CLIENT_ID_PARAM)) {
             request.withQuery(ApiConst.CLIENT_ID_PARAM, clientId);
         }
 
         final String url = String.format("%s://%s%s", hostScheme, hostDomain, request.toUrl());
         Log.i(TAG, "### execute() ### - url: " + url);
        GsonRequest apiRequest = new GsonRequest<>(gson, url, request.getTargetType(), okListener, errorListener);
         requestQueue.add(apiRequest);
     }
 }
