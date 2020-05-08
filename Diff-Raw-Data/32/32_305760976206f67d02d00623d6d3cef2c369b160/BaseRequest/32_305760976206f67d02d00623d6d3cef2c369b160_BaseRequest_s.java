 package ru.tulupov.nsuconnect.request;
 
 import android.net.Uri;
 
 import com.android.volley.AuthFailureError;
 import com.android.volley.Response;
 import com.android.volley.toolbox.StringRequest;
 import com.google.gson.Gson;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import ru.tulupov.nsuconnect.model.Session;
 
 public abstract class BaseRequest<T> extends StringRequest {
     protected Session session;
 
     public BaseRequest(int method, Uri uri, Session session, final Class<? extends T> clazz, final Response.Listener<T> listener, Response.ErrorListener errorListener) {
 
         super(method, uri.toString(), new Response.Listener<String>() {
             @Override
             public void onResponse(String s) {
                 if (listener != null) {
                     Gson gson = new Gson();
                     listener.onResponse(gson.fromJson(s, clazz));
                 }
             }
         }, errorListener);
         this.session = session;
     }
 
     public BaseRequest(int method, String url, Session session, final Class<? extends T> clazz, final Response.Listener<T> listener, Response.ErrorListener errorListener) {
 
         super(method, url, new Response.Listener<String>() {
             @Override
             public void onResponse(String s) {
                 if (listener != null) {
                     Gson gson = new Gson();
                     listener.onResponse(gson.fromJson(s, clazz));
                 }
             }
         }, errorListener);
         this.session = session;
     }
 
     public BaseRequest(int method, Uri uri, Session session, final Response.Listener<String> listener, Response.ErrorListener errorListener) {
         super(method, uri.toString(), listener, errorListener);
         this.session = session;
     }
 
     @Override
     public Map<String, String> getHeaders() throws AuthFailureError {
         Map<String, String> headers = new HashMap<String, String>();
         headers.put("Cookie", session.getSearch());
         headers.put("Referer", "http://inctalk.net/chat.php?noheader=1&provider=nsu&local=1&basecss=http://nsu-connect.ru/chatcss/base.css&chatcss=http://nsu-connect.ru/chatcss/chat.css&settingscss=http://nsu-connect.ru/chatcss/settings.css");
 
         return headers;
     }
 }
