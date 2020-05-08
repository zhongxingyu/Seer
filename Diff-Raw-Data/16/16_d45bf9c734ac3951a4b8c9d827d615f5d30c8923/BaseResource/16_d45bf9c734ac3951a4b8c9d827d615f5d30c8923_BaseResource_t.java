 package com.orgsync.api;
 
 import java.lang.reflect.Type;
 import java.text.SimpleDateFormat;
 import java.util.Date;
import java.util.TimeZone;
 
 import com.ning.http.client.FluentStringsMap;
 import com.ning.http.client.ListenableFuture;
 
 /*package*/abstract class BaseResource {
 
     /* package */static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 
    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

     private final ApiClientImpl client;
 
     private final String endpoint;
 
     /* package */BaseResource(final ApiClientImpl client, final String endpoint) {
         this.client = client;
         this.endpoint = endpoint;
     }
 
     /* package */<T> ListenableFuture<ApiResponse<T>> getResponse(final RequestParams params, final Type type) {
         return client.getResponse(params, type);
     }
 
     /* package */String dateToQueryParam(final Date date) {
         return dateFormat.format(date);
     }
 
     String getEndpoint() {
         return endpoint;
     }
 
     String showFor(final int id) {
         return String.format("%s/%d", endpoint, id);
     }
 
     <T> ListenableFuture<ApiResponse<T>> list(final Type type) {
         return list("", type);
     }
 
     <T> ListenableFuture<ApiResponse<T>> list(final String prefix, final Type type) {
         return getResponse(RequestParams.get(prefix + endpoint), type);
     }
 
     <T> ListenableFuture<ApiResponse<T>> show(final int id, final Type type) {
         return getResponse(RequestParams.get(showFor(id)), type);
     }
 
     <T> ListenableFuture<ApiResponse<T>> update(final int id, final FluentStringsMap params, final Type type) {
         return getResponse(RequestParams.put(showFor(id), params), type);
     }
 
     <T> ListenableFuture<ApiResponse<T>> delete(final int id, final Type type) {
         return getResponse(RequestParams.delete(showFor(id)), type);
     }
 
     void checkAddField(final FluentStringsMap params, final String field, final Object value) {
         if (value != null) {
             params.add(field, value.toString());
         }
     }
 
 }
