 package org.daum.library.javase.jtouchDB;
 
 
 import com.couchbase.touchdb.TDServer;
 import com.couchbase.touchdb.listener.TDListener;
 
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jed
  * Date: 23/11/12
  * Time: 15:31
  */
 @Library(name = "JavaSE", names = {"Android"})
 @DictionaryType({
         @DictionaryAttribute(name = "port_db", defaultValue = "8888", optional = false),
        @DictionaryAttribute(name = "path", defaultValue ="",optional = false),
 })
 @Provides({
         @ProvidedPort(name = "cluster", type = PortType.MESSAGE,theadStrategy = ThreadStrategy.NONE)
 })
 @ComponentType
 public class JTouchDB extends AbstractComponentType
 {
     private TDListener listener;
     private  TDServer server = null;
     private Integer port=8888;
     private Logger logger = LoggerFactory.getLogger(getClass());
 
     @Start
     public void start()
     {
         try
         {
             String filesDir =getDictionary().get("path").toString();
             if(filesDir.length() == 0){
                 logger.error("You have to choose a path to store sqlitedb");
             }else
             {
                 server = new TDServer(filesDir);
                 port = Integer.parseInt(getDictionary().get("port_db").toString());
                 listener = new TDListener(server, port);
                 listener.start();
             }
 
         } catch (Exception e) {
             logger.error("Unable to create JTouchDB", e);
         }
     }
 
 
     @Stop
     public void stop()
     {
         try
         {
             if(server != null){
                 server.close();
             }
             if(listener !=null){
                 listener.stop();
             }
         } catch (Exception e) {
             logger.error("Unable to stop CTouchDB", e);
         }
     }
 
 
     @Update
     public void update()
     {
         stop();
         start();
 
     }
 
     @Port(name = "cluster")
     public void enterthevoid(Object e){
 
     }
 
 
 }
