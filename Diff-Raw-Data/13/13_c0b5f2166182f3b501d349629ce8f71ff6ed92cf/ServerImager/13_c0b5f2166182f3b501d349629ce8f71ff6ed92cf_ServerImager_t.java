 /* Copyright 2013 Rabidgremlin Limited
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package com.rabidgremlin.serverimager;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.ParameterException;
 import com.google.common.base.Joiner;
 import com.rabidgremlin.serverimager.api.ApiHelper;
 import com.rabidgremlin.serverimager.api.ImageDetails;
 import com.rabidgremlin.serverimager.api.ServerDetails;
 import com.rabidgremlin.serverimager.opts.MainOptions;
 
 public class ServerImager
 {
   private static Logger log = LoggerFactory.getLogger(ServerImager.class);
 
   public static void main(String[] args)
   {
     // Bridge JUL to SLF4J
     SLF4JBridgeHandler.removeHandlersForRootLogger();
     SLF4JBridgeHandler.install();
 
     log.info("Starting ServerImager...");
 
     MainOptions options = new MainOptions();
     JCommander jc = new JCommander(options);
     jc.setProgramName("java -jar serverimager.jar");
 
     ApiHelper apiHelper = null;
 
     try
     {
       // validate the args
       jc.parse(args);
 
       // turn on debugging logging if requested
       if (options.debug)
       {
         ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
             .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
         root.setLevel(ch.qos.logback.classic.Level.DEBUG);
       }
 
       // create the api helper
       apiHelper = new ApiHelper(options.username, options.apiKey, options.rackspaceZone);
 
       // get the list of servers
       List<ServerDetails> servers = apiHelper.getServers();
 
       // create map to validate servers against
       HashMap<String, ServerDetails> serversByNameMap = new HashMap<String, ServerDetails>();
       for (ServerDetails server : servers)
       {
         serversByNameMap.put(server.getName(), server);
       }
 
       // validate that requested servers exist
       for (String server : options.servers)
       {
         log.debug("Checking if server {} exists...", server);
 
         if (!serversByNameMap.containsKey(server))
         {
           throw new Exception("Cannot find server: " + server + ". Current servers are: "
               + Joiner.on(", ").join(serversByNameMap.keySet()));
         }
       }
 
       // dump list of servers that will be processed
       log.info("Processing images for servers:");
 
       for (String server : options.servers)
       {
         ServerDetails serverDetails = serversByNameMap.get(server);
 
         log.info("    {} ({})", serverDetails.getName(), serverDetails.getId());
       }
 
       // create images
       if (options.noImageCreation)
       {
         log.warn("-nocreate specified. NO IMAGES WILL BE CREATED!");
       }
       else
       {
         log.info("Creating server images...");
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
         String timestamp = formatter.format(new Date());
 
         log.debug("Using timestamp {}", timestamp);
 
         // create images for server
         for (String server : options.servers)
         {
           ServerDetails serverDetails = serversByNameMap.get(server);
 
           String imageName = options.imageNamePrefix + "_" + serverDetails.getName() + "_" + timestamp;
 
           log.info("Requested creation of image {} for server {}", imageName, serverDetails.getName());
           apiHelper.createImage(imageName, serverDetails.getId());
         }
       }
 
       // delete old images
       if (options.nukeOldImages)
       {
        // build up set of server ids for the servers specified on cmd line
         HashSet<String> serverIdSet = new HashSet<String>();
        for (String server : options.servers)
         {
          ServerDetails serverDetails = serversByNameMap.get(server);

          if (serverDetails != null)
          {
            serverIdSet.add(serverDetails.getId());
          }
         }
 
         log.info("Deleting images older then {} days", options.maxImageAgeInDays);
         List<ImageDetails> images = apiHelper.getImages(options.imageNamePrefix, serverIdSet);
 
         int deletedCount = 0;
 
         for (ImageDetails image : images)
         {
           log.debug("Image {} is {} days old", image.getName(), image.getAgeInDays());
 
           if (image.getAgeInDays() >= options.maxImageAgeInDays)
           {
             log.info("Deleting image {} ({})...", image.getName(), image.getId());
             apiHelper.deleteImage(image.getId());
             deletedCount++;
           }
           else
           {
             log.info("Skipping image {}. It is only {} days old", image.getName(), image.getAgeInDays());
           }
         }
 
         log.info("{} images deleted.", deletedCount);
       }
       else
       {
         log.warn("-nuke not specified. NO IMAGES WILL BE DELETED!");
       }
 
       log.info("Done.");
     }
     catch (ParameterException e)
     {
       log.error("Parameter error: {}", e.getMessage());
       jc.usage();
       System.exit(1);
     }
     catch (Throwable t)
     {
       log.error("Unexpected error: {}", t.getMessage());
       log.debug("Stacktrace: ", t);
       System.exit(1);
     }
     finally
     {
       if (apiHelper != null)
       {
         apiHelper.close();
       }
     }
   }
 
 }
