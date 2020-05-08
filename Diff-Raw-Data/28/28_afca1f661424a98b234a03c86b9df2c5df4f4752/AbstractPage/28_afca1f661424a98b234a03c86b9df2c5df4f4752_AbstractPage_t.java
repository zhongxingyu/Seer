 package org.kevoree.library.javase.webserver;
 
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.framework.MessagePort;
 import org.kevoree.library.javase.webserver.impl.KevoreeHttpResponseImpl;
 import org.kevoree.log.Log;
 
 /**
  * Created by IntelliJ IDEA.
  * User: duke
  * Date: 14/10/11
  * Time: 08:52
  */
 
 
 @Library(name = "JavaSE")
 @ComponentType
 @Provides({
         @ProvidedPort(name = "request", type = PortType.MESSAGE)
 })
 @Requires({
         @RequiredPort(name = "content", type = PortType.MESSAGE),
         @RequiredPort(name = "forward", type = PortType.MESSAGE, optional = true)
 })
 @DictionaryType({
         @DictionaryAttribute(name = "urlpattern", optional = true, defaultValue = "/")
 })
 public abstract class AbstractPage extends AbstractComponentType {
 
     protected static final int NO_RETURN_RESPONSE = 418;
     protected URLHandler handler = new URLHandler();
 
     public String getLastParam(String url) {
         String urlPattern = this.getDictionary().get("urlpattern").toString();
         return handler.getLastParam(url, urlPattern);
     }
 
     @Start
     public void startPage() {
         handler.initRegex(this.getDictionary().get("urlpattern").toString());
         Log.debug("Abstract page start");
     }
 
     @Stop
     public void stopPage() {
         //NOOP
     }
 
     @Update
     public void updatePage() {
         handler.initRegex(this.getDictionary().get("urlpattern").toString());
     }
 
     public KevoreeHttpRequest resolveRequest(Object param) {
         Log.debug("KevoreeHttpRequest handler triggered");
         if (param instanceof KevoreeHttpRequest) {
             return handler.check((KevoreeHttpRequest) param);
         } else {
             return null;
         }
     }
 
     public KevoreeHttpResponse buildResponse(KevoreeHttpRequest request) {
         KevoreeHttpResponse responseKevoree = new KevoreeHttpResponseImpl();
         responseKevoree.setTokenID(request.getTokenID());
         return responseKevoree;
     }
 
     @Port(name = "request")
     public void requestHandler(Object param) {
         KevoreeHttpRequest request = resolveRequest(param);
         if (request != null) {
             KevoreeHttpResponse response = buildResponse(request);
             response = process(request, response);
             if (response.getStatus() != 418) {
                 this.getPortByName("content", MessagePort.class).process(response);//SEND MESSAGE
             } else {
                 Log.debug("Status code correspond to tea pot: No response returns!");
             }
        }  else {
            // url pattern check fail --> forward request
            forward(param);
         }
     }
 
 
     public abstract KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response); /*{
         //TO OVERRIDE
         return response;
     }*/
 
    public void forward(Object request ) {
         if (isPortBinded("forward")) {
             getPortByName("forward", MessagePort.class).process(request);
         } else {
             Log.debug("Unable to forward request because the forward port is not bind for {}", getName());
         }
     }
 
 }
