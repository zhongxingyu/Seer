 /**
  * For copyright information see the LICENSE document.
  */
 
 package com.realityshard.host;
 
 import com.realityshard.container.ContainerFacade;
 import com.realityshard.network.NetworkFacade;
 import com.realityshard.shardlet.GlobalExecutor;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * This program creates the objects necessary to start the server
  * 
  * TODO: implement functionality, clean up
  * 
  * @author _rusty
  */
 public final class HostApplication 
 {
     /**
      * Runs the application.
      * 
      * @param       args
      * @throws      MalformedURLException   Should never happen.
      */
     public static void main(String[] args) 
             throws MalformedURLException
     {
         // temporary logger
         Logger logger = LoggerFactory.getLogger(HostApplication.class);
   
         
         // init the file paths
         // see the documentation diagrams on deployment
         // for a reference on how this should look like
         File localPath = new File(HostApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath());
         
         File configPath = new File(localPath, "config");
         File schemaPath = new File(localPath, "schema");
         File protocolsPath = new File(localPath, "protocols");
         File gameAppsPath = new File(localPath, "gameapps");
         
         File libsPath = new File(localPath, "lib");
         
         
         // init the additional classpaths
         ClassLoaderExtension.addLibs(libsPath);
         
         
         // TODO process the args ;D
         // we might want to show if any found args have actually 
         // been used by this application
         
         
         // output some smart info
         logger.info("Host application starting up...");
         
         
         // create the executor
         // TODO: let the parameter be defined by command line args
         // TODO: check if the implementation is appropriate
         ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(12);
         
         // ok, we can now define the executor as global...
         // this can actually be done within the API
         GlobalExecutor.init(executor);
         
         // we need a new concurrent network manager here
         // note that this has to be a concrete implementation atm
         NetworkFacade netMan = new NetworkFacade("127.0.0.1");
         
         // we've done anything we wanted to, so lets start the container!
         try 
         {
             // create the container
             ContainerFacade container = new ContainerFacade(netMan, configPath, schemaPath, protocolsPath, gameAppsPath);
         } 
         catch (Exception ex) 
         {
             logger.error("Container failed to start up.", ex);
             System.exit(1);
         }
     }
 }
