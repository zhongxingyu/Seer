 package jewas.http.impl;
 
 import jewas.http.HttpRequest;
 import jewas.http.RequestHandler;
 import jewas.http.data.BodyParameters;
 import jewas.http.data.HttpData;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author fcamblor
  */
 public abstract class AbstractRequestHandlerDelegate implements RequestHandler {
 
     private Map<HttpRequest, List<RequestHandler> > cachedDelegates = Collections.synchronizedMap(
             new HashMap<HttpRequest, List<RequestHandler> >() );
 
     @Override
     public void onRequest(HttpRequest request) {
         for(RequestHandler delegate : findNonNullDelegatesFor(request)){
             delegate.onRequest(request);
         }
     }
 
     @Override
     public void offer(HttpRequest request, HttpData data) {
         for(RequestHandler delegate : findNonNullDelegatesFor(request)){
             delegate.offer(request, data);
         }
     }
 
     @Override
     public void onReady(HttpRequest request, BodyParameters bodyParameters) {
         for(RequestHandler delegate : findNonNullDelegatesFor(request)){
             delegate.onReady(request, bodyParameters);
         }
     }
 
     protected List<RequestHandler> findNonNullDelegatesFor(HttpRequest request){
         List<RequestHandler> delegates = null;
         if(cachedDelegates.containsKey(request)){
             delegates = cachedDelegates.get(request);
         } else {
             delegates = findDelegatesFor(request);
             cachedDelegates.put(request, delegates);
         }
 
         if(delegates == null || delegates.isEmpty()){
            throw new IllegalStateException(String.format("No request delegate found for request uri %s and method %s!", request.uri(), request.method()));
         }
 
         return delegates;
     }
 
     public abstract List<RequestHandler> findDelegatesFor(HttpRequest request);
 }
