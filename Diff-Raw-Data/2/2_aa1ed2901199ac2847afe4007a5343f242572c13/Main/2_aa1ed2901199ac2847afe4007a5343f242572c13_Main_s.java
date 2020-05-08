 /**************************************************************************************
  * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
  * http://fusesource.com                                                              *
  * ---------------------------------------------------------------------------------- *
  * The software in this package is published under the terms of the AGPL license      *
  * a copy of which has been included with this distribution in the license.txt file.  *
  **************************************************************************************/
 package org.fusesource.cloudmix.agent.mop;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fusesource.cloudmix.agent.AgentPoller;
 import org.fusesource.cloudmix.agent.RestGridClient;
 import org.fusesource.cloudmix.common.dto.Constants;
 
 import java.io.File;
 
 /**
  * @version $Revision: 1.1 $
  */
 public class Main {
 
     private static final transient Log LOG = LogFactory.getLog(Main.class);
 
     public static void main(String[] args) {
         try {
             String controllerUrl = "http://localhost:8181/";
             String profile = Constants.WILDCARD_PROFILE_NAME;
            String directory = "";
 
             if (args.length > 0) {
                 String arg0 = args[0];
                 if (arg0.startsWith("?") || arg0.startsWith("-")) {
                     System.out.println("Usage: DirectoryInstallerAgent [controllerURL] [profile]");
                     return;
                 } else {
                     controllerUrl = arg0;
                 }
                 if (args.length > 1) {
                     profile = args[1];
                 }
                 if (args.length > 2) {
                     directory = args[2];
                 }
             }
             LOG.info("Connecting to Cloudmix controller at: " + controllerUrl + " with profile: " + profile + " with working directory: " + directory);
 
 
             MopAgent agent = new MopAgent();
             agent.setClient(new RestGridClient(controllerUrl));
             agent.setProfile(profile);
             agent.setWorkDirectory(new File(directory));
             agent.init();
 
             AgentPoller poller = new AgentPoller(agent);
             poller.start();
         } catch (Exception e) {
             LOG.error("Caught: " + e, e);
         }
     }
 }
