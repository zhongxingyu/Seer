 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.client.help;
 
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.common.help.HelpInfo;
 
 /**
  * A collection of static utility routines to help with the help system on the
  * client.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class HelpUtils {
 
     /* The base URL of the web server */
     private static final String BASE_URL = "wonderland-web-help/help/";
     
     /* The error logger for this class */
     private static Logger logger = Logger.getLogger(HelpUtils.class.getName());
     
     /**
      * Asks the web server for the help menu items given the current primary
      * server.
      *
      * @param manager The current primary server
      * @return An object representing the Help menu structure
      */
     public static HelpInfo fetchHelpInfo(ServerSessionManager manager) {
         try {
             // Open an HTTP connection to the Jersey RESTful service using the
             // base URL of the primary connection.
             String serverURL =  manager.getServerURL();
            System.out.println("HELP URL " + serverURL + BASE_URL + "info/get");
             URL url = new URL(serverURL + BASE_URL + "info/get");
             return HelpInfo.decode(new InputStreamReader(url.openStream()));
         } catch (java.lang.Exception excp) {
             /* Log an error and return null */
             logger.log(Level.WARNING, "Fetch of Help Info Failed", excp);
             return null;
         }
     }
 }
