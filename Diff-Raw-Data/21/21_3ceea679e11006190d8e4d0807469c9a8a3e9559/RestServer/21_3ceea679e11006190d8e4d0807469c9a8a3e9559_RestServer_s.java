 package models;
 
 import play.libs.F;
 import play.libs.WS;
 
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jpereira
  * Date: 09-04-2013
  * Time: 1:10
  * To change this template use File | Settings | File Templates.
  */
 public abstract class RestServer {
 
 
     private UsernamePasswordAuthentication authentication;
 
    public RestServer(UsernamePasswordAuthentication athentication) {
         this.authentication = authentication;
     }
 
     public String execute(UrlBuilder urlBuilder) {
 
 
         //Abstract this code out to a Rest Server, for example
 
         play.Logger.debug("Calling: " + urlBuilder.getUrl());
         WS.WSRequestHolder wsreqHolder = WS.url(urlBuilder.getUrl()).setAuth(authentication.getUserName(), authentication.getPassword());
 
         urlBuilder.buildRequestQueryString(wsreqHolder);
 
         F.Promise<WS.Response> promiseOfResult = wsreqHolder.get();
 
         WS.Response response = promiseOfResult.get(3L, TimeUnit.MINUTES); //block here
        return response.getBody();
 
     }
 }
