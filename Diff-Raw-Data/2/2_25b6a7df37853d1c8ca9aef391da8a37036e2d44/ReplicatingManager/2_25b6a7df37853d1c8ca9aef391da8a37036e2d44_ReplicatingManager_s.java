 package org.daum.library.replicatingMap;
 
 import org.daum.library.replicatingMap.msg.Command;
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.framework.MessagePort;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Created by jed
  * User: jedartois@gmail.com
  * Date: 22/05/12
  * Time: 18:00
  */
 
 @Library(name = "JavaSE", names = {"Android"})
 @ComponentType
 @Requires({
         @RequiredPort(name = "messagetoSend", type = PortType.MESSAGE),
         @RequiredPort(name = "trigger", type = PortType.MESSAGE,optional = true)
 })
 @Provides({
         @ProvidedPort(name = "remoteReceived", type = PortType.MESSAGE) ,
         @ProvidedPort(name = "service", type = PortType.SERVICE, className = ReplicatingService.class)
 })
 public class ReplicatingManager extends AbstractComponentType implements ReplicatingService {
     private CacheManager cacheManager =null;
     KChannelImpl kChannel=null;
     //  private Thread thread = new Thread(this);
     private Logger logger = LoggerFactory.getLogger(this.getClass());
 
     @Start
     public void start()
     {
         kChannel   = new KChannelImpl(this);
         cacheManager = new CacheManager(kChannel,getNodeName());
         cacheManager.snapshot();
     }
 
     @Stop
     public void stop()
     {

     }
 
     @Update
     public void update()
     {
         kChannel   = new KChannelImpl(this);
         cacheManager.setChanel(kChannel);
     }
 
 
     @Port(name = "remoteReceived")
     public void messageReceived(Object o)
     {
         cacheManager.remoteReceived(o);
 
         if(!(o instanceof Command))
         {
             getPortByName("trigger", MessagePort.class).process("tick");
         }
 
     }
 
 
     @Port(name = "service", method = "getCache")
     @Override
     public Cache getCache(String name) {
         return cacheManager.getCache(name);
     }
 
 
 }
