 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.totsp.gwittir.rest.client;
 
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import com.totsp.gwittir.rest.client.Transport.RequestControl;
 import com.totsp.gwittir.serial.client.Codec;
 
 
 /**
  *
  * @author rcooper
  */
 public class SimpleRESTService<T> implements RESTService<T> {
     private String baseURL;
     private Transport transport;
     private Codec<T> codec;
 
     public void setBaseURL(String baseURL) {
         this.baseURL = baseURL;
     }
 
     public void setTransport(Transport transport) {
         this.transport = transport;
     }
 
     public void setCodec(Codec<T> codec) {
         this.codec = codec;
     }
 
     public RequestControl get(String key, final AsyncCallback<T> callback) {
         return transport.get(codec.getMimeType(), baseURL + key,
             new AsyncCallback<String>() {
                 public void onFailure(Throwable caught) {
                     callback.onFailure(caught);
                 }
 
                 public void onSuccess(String result) {
                     try {
                         T value = codec.deserialize(result);
                         callback.onSuccess(value);
                     } catch (Throwable t) {
                         callback.onFailure(t);
                     }
                 }
             });
     }
 
     public RequestControl put(String key, T object, final AsyncCallback<String> callback) {
         try {
             String value = codec.serialize(object);
 
            return transport.put(codec.getMimeType(), baseURL+key, value, callback);
         } catch (final Exception e) {
             DeferredCommand.addCommand(new Command() {
                     public void execute() {
                         callback.onFailure(e);
                     }
                 });
 
             return null;
         }
     }
 
     public RequestControl delete(String key, AsyncCallback<String> callback) {
         return transport.delete(codec.getMimeType(), baseURL + key, callback);
     }
 
     public RequestControl post(String key, T object,
         final AsyncCallback<String> callback) {
         try {
             String value = codec.serialize(object);
 
             return transport.post(codec.getMimeType(), baseURL + key, value,
                 callback);
         } catch (final Exception e) {
             DeferredCommand.addCommand(new Command() {
                     public void execute() {
                         callback.onFailure(e);
                     }
                 });
 
             return null;
         }
     }
 }
