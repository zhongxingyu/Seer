 package com.artcom.y60.http;
 
 import java.util.HashMap;
 
 import com.artcom.y60.data.StreamableContent;
 
 public interface HttpResponseHandler {
     
     public void onHeaderAvailable(HashMap<String, String> headers);
     
     public void onSuccess(int statusCode, StreamableContent body);
     
     public void onError(int statusCode, StreamableContent body);
     
     public void onReceiving(double progress);
 }
