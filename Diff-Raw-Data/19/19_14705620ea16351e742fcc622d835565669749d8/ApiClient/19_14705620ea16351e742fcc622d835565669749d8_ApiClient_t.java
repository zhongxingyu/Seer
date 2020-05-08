 package com.moac.android.soundmap.api;
 
 import android.util.Log;
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 
 import com.google.gson.Gson;
 
 public class ApiClient {
 
     private static final String TAG = ApiClient.class.getSimpleName();
     public static final String CLIENT_ID_PARAM = "client_id";
 
     private final String HOST_DOMAIN;
     private final String HOST_SCHEME;
     private final String CLIENT_ID;
 
     private Gson mGson;
     private RequestQueue mQueue;
 
     public ApiClient(RequestQueue _requestQueue, Gson _gson, String _hostScheme, String _hostDomain, String _clientId) {
         mQueue = _requestQueue;
         mGson = _gson;
         HOST_SCHEME = _hostScheme;
         HOST_DOMAIN = _hostDomain;
         CLIENT_ID = _clientId;
     }
 
     public <T> void execute(ApiRequest<T> _request, Response.Listener<T> _okListener, Response.ErrorListener _errorListener) {
         if(!_request.getQueries().containsKey(CLIENT_ID_PARAM)) {
             _request.withQuery(CLIENT_ID_PARAM, CLIENT_ID);
         }
 
         final String url = HOST_SCHEME + HOST_DOMAIN + _request.toUrl();
        Log.i(TAG, "### execute() ### - url: " + url);
         GsonRequest request = new GsonRequest<T>(mGson, url, _request.getTargetType(), _okListener, _errorListener);
         mQueue.add(request);
     }
 }
