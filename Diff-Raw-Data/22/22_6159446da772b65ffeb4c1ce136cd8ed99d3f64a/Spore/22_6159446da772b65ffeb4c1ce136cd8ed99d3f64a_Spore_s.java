 package net.linkfluence.jspore;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Strings;
 import com.ning.http.client.AsyncHttpClient;
 import com.ning.http.client.Request;
 import com.ning.http.client.RequestBuilder;
 import com.ning.http.client.Response;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import net.linkfluence.jspore.middleware.Logger;
 import net.linkfluence.jspore.middleware.Middleware;
 
 /**
  * Main class of Java Spore implementation.
  * T is the result type.
  * 
  * @author Nicolas Yzet <nyzet@linkfluence.net>
  */
 public class Spore<T> {
 
     private final Model model;
     private final AsyncHttpClient httpClient = new AsyncHttpClient();
     private final Middleware requestMiddleware; // first entry of the MiddleWare request processing chain.
     private final Middleware responseMiddleware; // first entry of the middleware response processing chain.
     
     protected Spore(Model model, Middleware requestMiddleware,
             Middleware responseMiddleware) {
         this.model = model;
         this.requestMiddleware = requestMiddleware;
         this.responseMiddleware = responseMiddleware;
     }
 
     public SporeResult<T> call(String methodName) throws SporeException {
         return this.call(methodName, new HashMap<String, String>(), (Object) null);
     }
 
     public SporeResult<T> call(String methodName, Map<String, String> params) throws SporeException {
         return this.call(methodName, params, (Object) null);
     }
     
     public SporeResult<T> call(String methodName, String payload) throws SporeException {
         return this.call(methodName, new HashMap<String, String>(), payload);
     }
     
     public SporeResult<T> call(String methodName, Map<String, String> params, String payload) throws SporeException {
         Method method = getMethod(methodName);
         RequestBuilder reqBuilder = method.buildRequest(model.baseUrl.toString(), params, payload);
         return doCall(reqBuilder, payload, method);
     }
     
     public SporeResult<T> call(String methodName, byte[] payload) throws SporeException {
         return this.call(methodName, new HashMap<String, String>(), payload);
     }
     
     public SporeResult<T> call(String methodName, Map<String, String> params, byte[] payload) throws SporeException {
         Method method = getMethod(methodName);
         RequestBuilder reqBuilder = method.buildRequest(model.baseUrl.toString(), params, payload);
         return doCall(reqBuilder, payload, method);
     }
 
     public SporeResult<T> call(String methodName, Object payload) throws SporeException {
         return this.call(methodName, new HashMap<String, String>(), payload);
     }
 
     public SporeResult<T> call(String methodName, Map<String, String> params, Object payload) throws SporeException {
         Method method = getMethod(methodName);
         RequestBuilder reqBuilder = method.buildRequest(model.baseUrl.toString(), params, payload);
         return doCall(reqBuilder, payload, method);
     }
 
     private Method getMethod(String methodName) throws SporeException {
         Method method = model.routes.get(methodName);
         if (method == null) {
             throw new SporeException("route does not exist");
         }
         return method;
     }
     
     private SporeResult<T> doCall(RequestBuilder reqBuilder, Object payload, Method context) throws SporeException {
         if(this.requestMiddleware != null){
             this.requestMiddleware.sendRequest(reqBuilder, payload, context);
         }
         Request req = reqBuilder.build();
         Response res = null;
         try {
             res = httpClient.executeRequest(req).get();
             // response checking
             int statusCode = res.getStatusCode();
            if(!context.expectedStatus.contains(statusCode)){
                 throw new SporeException("Receive unexpected response status: " + statusCode);
             }
             
             if(this.responseMiddleware != null){
                 return new SporeResult<T>(res, 
                     (T) this.responseMiddleware.receiveRequest(res, res.getResponseBody(), context));
             } else {
                 return new SporeResult<T>(res,
                         (T) res.getResponseBody());
             }
         } catch (IOException ex) {
             throw new SporeException(ex.getMessage());
         } catch (InterruptedException ex) {
             throw new SporeException(ex.getMessage());
         } catch (ExecutionException ex) {
             throw new SporeException(ex.getMessage());
         }
     }
     
     /**
      * Spore Builder init a Spore client from various sources.
      * Sources can be:
      * - a JSON formatted spec,
      * - a remote location (download spec through http),
      * - manually add routes.
      * 
      * Builder allow to override params. For instance load a spec file and then
      * override the parameters read.
      */
     public static class Builder<T> {
         private final Model model = new Model();
         private Middleware middleware = null;
         private final SpecParser parser = new SpecParser();
         private boolean debug = false;
         
         public Builder() {
         }
 
         public Builder addSpecContent(File spec) throws FileNotFoundException, IOException, InvalidSporeSpecException {
             Preconditions.checkArgument(spec.exists(), "File does not exists");
             Preconditions.checkArgument(spec.canRead(), "File cannot be read");
             FileInputStream stream = new FileInputStream(spec);
             addSpecContent(stream);
             stream.close();
             return this;
         }
 
         public Builder addSpecContent(InputStream stream) throws IOException, InvalidSporeSpecException {
             model.merge(parser.parse(stream));
             return this;
         }
 
         public Builder addSpecContent(String spec) throws IOException, IllegalArgumentException, InvalidSporeSpecException {
             model.merge(parser.parse(spec));
             return this;
         }
 
         public Builder addMethod(Method method) {
             Preconditions.checkNotNull(method);
             model.addMethod(method);
             return this;
         }
 
         public Builder setName(String name) {
             Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
             model.name = name;
             return this;
         }
 
         public Builder setVersion(String version) {
             Preconditions.checkArgument(!Strings.isNullOrEmpty(version));
             model.version = version;
             return this;
         }
 
         public Builder setBaseUrl(String url) throws MalformedURLException {
             Preconditions.checkArgument(!Strings.isNullOrEmpty(url));
             model.baseUrl = new URL(url);
             return this;
         }
 
         public Builder setBaseUrl(URL url) {
             Preconditions.checkNotNull(url);
             model.baseUrl = url;
             return this;
         }
         
         public Builder setDebug(boolean debug){
             this.debug = debug;
             return this;
         }
 
         public Builder addMiddleware(Middleware nextMiddleware) {
             Preconditions.checkNotNull(nextMiddleware);
             if(this.middleware == null){
                 this.middleware = nextMiddleware;
             } else {
                 this.middleware.setNext(nextMiddleware);
                 nextMiddleware.setPrev(this.middleware);
             }
             return this;
         }
 
         public Spore<T> build() {
             if(this.debug){
                 addMiddleware(new Logger());
             }
            
             Middleware last = this.middleware;
             if(last != null){
                 while(last.getNext() != null){
                    last = last.getNext();
                 }
             }
             return new Spore<T>(model, middleware, last);
         }
     }
 }
