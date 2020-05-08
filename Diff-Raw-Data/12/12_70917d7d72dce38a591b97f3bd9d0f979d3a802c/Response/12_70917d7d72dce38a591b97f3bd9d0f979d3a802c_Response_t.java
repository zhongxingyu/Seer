 package com.aeroheart.owlery;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.aeroheart.owlery.model.Model;
 import com.aeroheart.owlery.parser.JSONParser;
 import com.aeroheart.owlery.parser.LazyAssIdleParser;
 import com.aeroheart.owlery.parser.OAuthParser;
 
 /**
  * Class used for processing the response fetched by Request.
  * 
  * @author aeroheart.c6
  */
 public class Response {
     public enum Mode {
         SINGLE,
         MULTIPLE;
     }
     
     public enum Type {
         TEXT,
         JSON,
         OAUTH;
     }
     
     protected Type                      type;
     protected Mode                      mode;
     protected Callback                  callback;
     
     protected Map<String, List<String>> headers;
     protected int                       statusCode;
     protected String                    statusMsg;
     protected byte[]                    body;
 
     protected Parser                    parser;
     protected Class<? extends Model>    modelClass;
     protected Model                     model;
     protected List<Model>               models;
     
     public Response(Type type, Mode mode, Callback callback) {
         this.headers  = new HashMap<String, List<String>>();
         this.type     = type;
         this.mode     = mode;
         this.body     = null;
         
         this.setCallback(callback);
     }
     
     public Response setParser(Parser parser) {
         this.parser = parser;
         return this;
     }
     
     public Response setModelClass(Class<? extends Model> modelClass) {
         this.modelClass = modelClass;
         return this;
     }
     
     public Response setCallback(Callback callback) {
         this.callback = callback;
         return this;
     }
     
     /**
      * Sets the status code ang status message information from the HttpURLConnection instance
      * 
      * Note:
      * EOFException might mean that the request timed out
      * 
      * @param connection
      * 
      * @return the current instance
      */
     public Response setStatus(HttpURLConnection connection) {
         try {
             this.statusCode = connection.getResponseCode();
             this.statusMsg  = connection.getResponseMessage();
         }
         catch (IOException exception) {
             this.statusCode = 400;
             this.statusMsg  = "Bad Request";
         }
         
         return this;
     }
     
     /**
      * Sets the status code and status message information directly from the provided arguments. The
      * method is usually used only when the Response object is not used in conjunction with a
      * Request instance
      * 
      * @param code
      * @param message
      * 
      * @return the current instance
      */
     public Response setStatus(int code, String message) {
         this.statusCode = code;
         this.statusMsg  = message;
         
         return this;
     }
     
     public Response setHeaders(HttpURLConnection connection) {
         Map<String, List<String>> headers = connection.getHeaderFields();
         
         if (headers != null)
             this.headers.putAll(connection.getHeaderFields());
         
         return this;
     }
     
     public Response setBody(byte[] body) {
         this.body = body;
         return this;
     }
     
     public boolean isSuccess() {
         return this.statusCode / 100 == 2;
     }
     
     public List<String> getHeaderNames() {
         List<String> headers = new ArrayList<String>();
         
         for (String header : this.headers.keySet())
             headers.add(header);
         
         return Collections.unmodifiableList(headers);
     }
     
     public String getHeaderValue(String name) {
         List<String> values = this.getHeaderValues(name);
         
         return values.get(values.size() - 1);
     }
     
     public List<String> getHeaderValues(String name) {
         return Collections.unmodifiableList(this.headers.get(name));
     }
     
     public int getStatusCode() {
         return this.statusCode;
     }
     
     public String getStatusMessage() {
         return this.statusMsg;
     }
     
     public String getBody() {
         String body;
         
         try {
             body = new String(this.body, "UTF-8");
         }
         catch (UnsupportedEncodingException exception) {
             body = "";
         }
         
         return body;
     }
     
     /**
      * Returns the raw byte array form of the response body. Exercise care in using this as the
      * reference of the byte array is returned. Changes made by this return value will most likely
      * reflect in this instance's body data. 
      * 
      * @return the body as a byte array
      */
     public byte[] getBodyRaw() {
        if (this.body == null)
            return this.body = new byte[0];
        else
            return this.body;
     }
     
     public Model getModel() {
         return this.model;
     }
     
     public List<Model> getModels() {
         return this.models;
     }
     
     /*
      ***********************************************************************************************
      * Response Processing
      ***********************************************************************************************
      */
     public Response process() {
         Parser parser;
         String responseBody;
         
         if (!this.isSuccess()) {
             this.model  = null;
             this.models = null;
             
             return this;
         }
         
         if (this.parser == null)
             if (this.type == Type.OAUTH)
                 parser = new OAuthParser();
             else if (this.type == Type.JSON)
                 parser = new JSONParser();
             else
                 parser = new LazyAssIdleParser();
         else
             parser = this.parser;
         
         parser.setModelClass(this.modelClass);
         
        if (parser instanceof LazyAssIdleParser)
            responseBody = null;
        else
            responseBody = this.getBody();
        
         if (this.mode == Mode.SINGLE) {
             this.model  = parser.parseSingle(responseBody);
             this.models = new ArrayList<Model>();
             
             this.models.add(this.model);
         }
         else {
             this.models = parser.parseMultiple(responseBody);
             this.model  = this.models.get(0);
         }
         
         return this;
     }
     
     public Response triggerCallback(Request request) {
         if (this.callback == null)
             return this;
         
         if (this.isSuccess())
             callback.onSuccess(request, this);
         else
             callback.onError(request, this);
         
         callback.onComplete(request, this);
         
         return this;
     }
     
 
     /*
      ***********************************************************************************************
      * InnerClasses: Callback Interface
      ***********************************************************************************************
      */
     public static interface Callback {
         public void onComplete(Request request, Response response);
         
         public void onSuccess(Request request, Response response);
         public void onError(Request request, Response response);
     }
     
     /*
      ***********************************************************************************************
      * InnerClasses: Parser Interface
      ***********************************************************************************************
      */    
     public static interface Parser {
         public Parser setModelClass(Class<? extends Model> ModelClass);
         
         public Model parseSingle(String data);
         public List<Model> parseMultiple(String data);
     }
 }
