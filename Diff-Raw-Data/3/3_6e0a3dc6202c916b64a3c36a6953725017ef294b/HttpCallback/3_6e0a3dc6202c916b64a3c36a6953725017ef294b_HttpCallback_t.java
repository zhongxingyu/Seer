 package com.leansoft.nanorest.callback;
 
 import com.leansoft.nanorest.domain.ResponseStatus;
 
 /**
  * The {@link HttpCallback} interface is used for HTTP response notification. It contains methods
  * that describe the error if the callback fails and return response data if the callback succeeds.
  * 
  * @param <T> Parameter that indicates which object the callback returns. It can be of any type.
  */
 public interface HttpCallback<T> {
 
     /**
      * This method shows that the callback successfully finished. It contains response data which
      * represents the return type of the callback.
      * 
      * @param responseData It can be of any type.
      */
     public void onSuccess(T responseData);
 
     /**
     * This method shows that the callback has failed due to HTTP related issue
      * 
      * @param responseCode
      */
     public void onHttpError(ResponseStatus responseCode);
 
 }
