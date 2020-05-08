 package org.kevoree.sky.web;
 
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.log.Log;
 import org.webbitserver.WebServer;
 import org.webbitserver.WebServers;
 import org.webbitserver.handler.StaticFileHandler;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 09/07/13
  * Time: 11:52
  */
 
 @Library(name = "Sky")
 @DictionaryType({
         @DictionaryAttribute(name = "port", defaultValue = "80", optional = false)
 })
 @ComponentType
 public class WebFrontend extends AbstractComponentType {
 
     private WebServer webServer;
     private ModelServiceSocketHandler mhandler = null;
 
     @Start
     public void startServer() {
         try {
             mhandler = new ModelServiceSocketHandler(this.getModelService());
             int port = Integer.parseInt(getDictionary().get("port").toString());
 
             webServer = WebServers.createWebServer(port)
                     .add(new MetaDataHandler(this.getModelService()))
                     .add("/model/service", mhandler)
                     .add(new StaticFileHandler("/home/edaubert/workspace/kevoree-corelibrary/sky/org.kevoree.library.sky.web/src/main/resources")) // path to web content
                     //.add(new EmbedHandler()) // path to web content
                     .start()
                     .get();
         } catch (Exception e) {
             Log.error("Error while starting Kloud Web front end", e);
         }
     }
 
     @Stop
     public void stopServer() {
         webServer.stop();
         mhandler.destroy();
     }
 
 }
