 package org.daum.library.javase.touchDB;
 
 import org.apache.http.HttpResponse;
 import org.kevoree.Channel;
 import org.kevoree.ComponentInstance;
 import org.kevoree.ContainerNode;
 import org.kevoree.MBinding;
 import org.kevoree.annotation.*;
 import org.kevoree.framework.*;
 import org.kevoree.framework.message.Message;
 import org.lightcouch.CouchDbClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import scala.Option;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jed
  * Date: 30/11/12
  * Time: 15:04
  * To change this template use File | Settings | File Templates.
  */
 @Library(name = "JavaSE", names = {"Android"})
 @DictionaryType({
         @DictionaryAttribute(name = "dbname", defaultValue = "jed", optional = false, fragmentDependant = false),
         @DictionaryAttribute(name = "refresh", defaultValue = "9000", optional = false, fragmentDependant = false),
         @DictionaryAttribute(name = "login", defaultValue = "", optional = false, fragmentDependant = false),
         @DictionaryAttribute(name = "password", defaultValue = "", optional = false, fragmentDependant = false),
         @DictionaryAttribute(name = "protocol", defaultValue = "http", optional = false, fragmentDependant = false,vals={"http","https"})
 })
 @org.kevoree.annotation.ChannelTypeFragment(theadStrategy = ThreadStrategy.SHARED_THREAD )
 public class syncTouchDB extends AbstractChannelFragment
 {
     private Logger logger  = LoggerFactory.getLogger(getClass());
     private ScheduledExecutorService service =     Executors.newSingleThreadScheduledExecutor();
     private  Runnable requestSync;
     private ArrayList<TouchDBInstance> current=new ArrayList<TouchDBInstance>();
 
     public enum ACTION {
         ADD,
         REMOVE
     }
 
     public void requestSync(ACTION evt)
     {
 
         CouchDbClient   local=null;
         CouchDbClient   remote=null;
         String dbname = getDictionary().get("dbname").toString();
         String protocol = getDictionary().get("protocol").toString();
         String login = getDictionary().get("login").toString();
         String password = getDictionary().get("password").toString();
 
         if(evt == ACTION.ADD)
         {
             current.clear();
             current=getTouchDBInstances();
         }
 
             ArrayList<TouchDBInstance> sources=new ArrayList<TouchDBInstance>();
             sources.addAll(current);
 
             for(TouchDBInstance src :sources)
             {
                 try
                 {
                     logger.warn("local "+src.adr+" "+src.port);
 
                     if(src.adr.length() == 0)
                     {
                         logger.warn("You have define an address "+getNodeName());
                     }
                     local =    new CouchDbClient(dbname,true,protocol,src.adr,src.port,login,password);
 
                     for(TouchDBInstance dest :current)
                     {
                         if(src != dest)
                         {
                             try
                             {
                                 if(dest.adr.length() == 0)
                                 {
                                     logger.warn("You have define an address "+getNodeName());
                                 }
                                 logger.warn("remote "+dest.adr+" "+dest.port);
                                 remote =  new CouchDbClient(dbname,true,protocol,dest.adr,dest.port,login,password);
 
                                 HttpResponse response=null;
                                 logger.warn(evt+" sync from "+local.getDBUri()+" to "+remote.getDBUri());
                                 if(evt == ACTION.ADD)
                                 {
                                     response = local.replicator().cancelreplicatorTouchDB(remote);
                                 } else
                                 {
                                     response = local.replicator().addreplicatorTouchDB(remote,true);
                                 }
 
                             } catch (Exception e){
                                 logger.warn("The node is not available ",e);
 
                             }
                         }
                     }
                 } catch (Exception e ){
                     e.printStackTrace();
                 }
 
 
             }
 
 
     }
     @Start
     public void start()
     {
         requestSync= new Runnable() {
             @Override
             public void run()
             {
                 while (Thread.currentThread().isAlive())
                 {
                     requestSync(ACTION.ADD);
                     try
                     {
                         Thread.sleep(8000);
                     } catch (InterruptedException e1) {
 
                     }
                 }
 
 
             }
         } ;
 
         service.schedule(requestSync,1, TimeUnit.SECONDS);
 
     }
 
     @Stop
     public void stop()
     {
         service.shutdownNow();
     }
 
 
     @Update
     public void updated()
     {
         requestSync(ACTION.REMOVE);
 
         service.execute(requestSync);
     }
 
     public ArrayList<TouchDBInstance> getTouchDBInstances()
     {
         ArrayList<TouchDBInstance> current=new ArrayList<TouchDBInstance>();
 
         for (MBinding binding : getModelElement().getBindingsForJ())
         {
 
             ContainerNode node = (ContainerNode) binding.getPort().eContainer().eContainer();
             ComponentInstance instance = (ComponentInstance)binding.getPort().eContainer();
 
            Integer port = Integer.parseInt(KevoreePropertyHelper.getProperty(instance, "port_db", false, null).get());
 
             TouchDBInstance in = new TouchDBInstance();
             in.adr = getAddress(node.getName());
             in.port = port;
             logger.warn(in.toString());
             current.add(in);
 
         }
         return current;
     }
 
 
     @Override
     public Object dispatch(Message message) {
         return null;
     }
 
     @Override
     public ChannelFragmentSender createSender(String s, String s1) {
         return null;
     }
 
 
     public String getAddress(String remoteNodeName) {
         Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(getModelService().getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
         if (ipOption.isDefined()) {
             return ipOption.get();
         } else {
             return "";
         }
     }
 
     public class TouchDBInstance {
         private String adr;
         private Integer port;
 
         public String toString(){
             return adr+"  "+port;
         }
     }
     /*
     public int parsePortNumber (String nodeName) {
         Option<Channel> channelOption = getModelService().getLastModel().findByQuery("hubs[" + getName() + "]", Channel.class);
         int port = 8000;
         if (channelOption.isDefined()) {
             Option<String> portOption = KevoreePropertyHelper.getProperty(channelOption.get(), "port", true, nodeName);
             if (portOption.isDefined()) {
                 try {
                     port = Integer.parseInt(portOption.get());
                 } catch (NumberFormatException e) {
                     logger.warn("Attribute \"port\" of {} is not an Integer, default value ({}) is used.", getName(), port);
                 }
             }
         } else {
             logger.warn("There is no channel named {}, default value ({}) is used.", getName(), port);
         }
         return port;
     }
         */
 }
