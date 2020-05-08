 package models.bamboo;
 
 import models.UrlBuilder;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jpereira
  * Date: 09-04-2013
  * Time: 1:03
  * To change this template use File | Settings | File Templates.
  */
 public class BambooBuildAPI {
 
    private static final String BUILT_REST_API = "rest/api/latest/build/";
 
     private final BambooServer bambooServer;
 
     public BambooBuildAPI(BambooServer bambooServer) {
         this.bambooServer = bambooServer;
     }
 
     public String getBuild(String bambooPlanKey) {
         return bambooServer.execute(new BambooBuildUrlBuilder(bambooServer.getBambooServerUrl(), bambooPlanKey));
     }
 
     private class BambooBuildUrlBuilder extends UrlBuilder {
 
 
         public BambooBuildUrlBuilder(String bambooServerUrl, String bambooPlanKey) {
             super(bambooServerUrl + BUILT_REST_API + bambooPlanKey + ".json");
 
         }
     }
 }
