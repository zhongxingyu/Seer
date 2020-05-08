 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: EmbeddedOpenDS.java,v 1.2 2007-09-15 08:03:25 rajeevangal Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.setup;
 
 import com.sun.identity.shared.debug.Debug;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.Map;
 import javax.servlet.ServletContext;
 
 
 import org.opends.server.config.ConfigException;
 import org.opends.server.core.DirectoryServer;
 import org.opends.server.extensions.ConfigFileHandler;
 import org.opends.server.types.DirectoryException;
 import org.opends.server.types.InitializationException;
 import org.opends.messages.Message;
 import org.opends.server.util.EmbeddedUtils;
 import org.opends.server.types.DirectoryEnvironmentConfig;
 import org.opends.server.extensions.SaltedSHA512PasswordStorageScheme;
 
 /**
   * This class encapsulates all <code>OpenDS</code>  dependencies.
   * All the interfaces are invoked from <code>AMSetupServlet</code> class
   * at different points : initial installation, normal startup and
   * normal shutdown of the embedded <code>OpenDS</code> instance.
   */
 public class EmbeddedOpenDS {
 
     private static boolean serverStarted = false;
 
     /**
      * Returns <code>true</code> if the server has already been started.
      *
      * @return <code>true</code> if the server has already been started.
      */ 
     public static boolean isStarted()
     {
         return serverStarted;
     }
 
     /**
      * Sets up embedded opends during initial installation :
      * <ul>
      * <li>lays out the filesystem directory structure needed by opends
      * <li>sets up port numbers for ldap and replication
      * <li>invokes <code>EmbeddedUtils</code> to start the embedded server.
      *
      *  @param map Map of properties collected by the configurator.
      *  @param servletCtx Servlet Context to read deployed war contents.
      *
      *  @throws Exception on encountering errors.
      */
     public static void setup(Map map, ServletContext servletCtx) throws Exception
     {
         String basedir = (String)map.get(SetupConstants.CONFIG_VAR_BASE_DIR);
         String odsRoot = basedir + "/" + SetupConstants.SMS_OPENDS_DATASTORE;
         new File(odsRoot).mkdir();
         String[] subDirectories =
         { "adminDb", "bak", "bin", "changelogDb", "classes",
           "config", "db", "db_verify", "ldif", "lib",
           "locks", "logs", "db_rebuild", "db_unindexed",
           "db_index_test", "db_import_test", "config/schema", 
           "config/upgrade" };
 
         
         // create sub dirs
         for (int i = 0; i < subDirectories.length; i++) {
             new File(odsRoot, subDirectories[i]).mkdir();
         }
 
 
         // copy files
         String[] files = {
            "config/upgrade/schema.ldif.3056",
            "config/upgrade/config.ldif.3056",
             "config/config.ldif",
             "config/famsuffix.ldif",
             "config/schema/00-core.ldif",
             "config/schema/01-pwpolicy.ldif",
             "config/schema/02-config.ldif",
            "config/schema/03-changelog.ldif",
            "config/schema/03-rfc2713.ldif",
             "config/schema/03-rfc2714.ldif",
            "config/schema/03-rfc2739.ldif",
            "config/schema/03-rfc2926.ldif",
             "config/schema/03-rfc3112.ldif",
            "config/schema/03-rfc3712.ldif",
            "config/schema/03-uddiv3.ldif",
             "config/schema/04-rfc2307bis.ldif"
         };
         for (int i = 0 ; i < files.length; i++) {
             String file = "/WEB-INF/template/opends/"+files[i];
             InputStreamReader fin = new InputStreamReader(
                 servletCtx.getResourceAsStream(file));
 
             StringBuffer sbuf = new StringBuffer();
             char[] cbuf = new char[1024];
             int len;
             while ((len = fin.read(cbuf)) > 0) {
                 sbuf.append(cbuf, 0, len);
             }
             FileWriter fout = null;
 
             try {
                 fout = new FileWriter(odsRoot + "/" + files[i]);
                 String inpStr = sbuf.toString();
                 fout.write(ServicesDefaultValues.tagSwap(inpStr));
             } catch (IOException e) {
                 Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                     "EmbeddedOpenDS.setup(). Error loading ldifs:", e);
                 throw e;
             } finally {
                 if (fin != null) {
                     try {
                         fin.close();
                     } catch (Exception ex) {
                         //No handling requried
                     }
                 }  
                 if (fout != null) {
                     try {
                         fout.close();
                     } catch (Exception ex) {
                         //No handling requried
                     }
                 }
             }
         }
         EmbeddedOpenDS.startServer(odsRoot);
         java.lang.Thread.sleep(5000);
         EmbeddedOpenDS.shutdownServer("to load ldif");
         EmbeddedOpenDS.loadLDIF(odsRoot, odsRoot+ "/config/famsuffix.ldif");
         EmbeddedOpenDS.startServer(odsRoot);
         java.lang.Thread.sleep(5000);
     }
 
     /**
      *  Starts the embedded <code>OpenDS</code> instance.
      *
      *  @param odsRoot File system directory where <code>OpenDS</code> 
      *                 is installed.
      *
      *  @throws Exception upon encountering errors.
      */
     public static void startServer(String odsRoot) throws Exception {
         if (isStarted()) {
             return;
         }
         Debug debug = Debug.getInstance(SetupConstants.DEBUG_NAME);
         debug.message("EmbeddedOpenDS.startServer("+odsRoot+")");
 
         DirectoryEnvironmentConfig config = new DirectoryEnvironmentConfig();
         config.setServerRoot(new File(odsRoot));
         config.setForceDaemonThreads(true);
         config.setConfigClass(ConfigFileHandler.class);
         config.setConfigFile(new File(odsRoot+"/config", "config.ldif"));
         debug.message("EmbeddedOpenDS.startServer:starting DS Server...");
         EmbeddedUtils.startServer(config);
         debug.message("...EmbeddedOpenDS.startServer:DS Server started.");
 
         serverStarted = true;
     }
     
 
     /**
      *  Gracefully shuts down the embedded opends instance.
      *
      *  @param reason  string representing reasn why shutdown was called.
      *
      *  @throws Exception on encountering errors.
      */
     public static void shutdownServer(String reason) throws Exception {
         Debug debug = Debug.getInstance(SetupConstants.DEBUG_NAME);
         if (isStarted()) {
             debug.message("EmbeddedOpenDS.shutdown server...");
             DirectoryServer.shutDown(
                 "com.sun.identity.setup.EmbeddedOpenDS",
                 Message.EMPTY);
             debug.message("EmbeddedOpenDS.shutdown server success.");
             serverStarted = false;
         }
     }
 
     /**
      *  Utility function to preload data in the embedded instance.
      *  Must be called when the directory instance is shutdown.
      *
      *  @param odsRoot Local directory where <code>OpenDS</code> is installed.
      *  @param ldif Full path of the ldif file to be loaded.
      *
      */
     public static void loadLDIF(String odsRoot, String ldif)
     {
         Debug debug = Debug.getInstance(SetupConstants.DEBUG_NAME);
         try {
             debug.message("EmbeddedOpenDS:loadLDIF("+ldif+")");
             String[] args1 = 
             { 
                 "-C", "org.opends.server.extensions.ConfigFileHandler",
                 "-f", odsRoot+"/config/config.ldif",
                 "-n", "userRoot",
                 "-l", ldif,
                 "-q" 
             };
             org.opends.server.tools.ImportLDIF.mainImportLDIF(args1);
             debug.message("EmbeddedOpenDS:loadLDIF Success");
         } catch (Exception ex) {
               debug.error("EmbeddedOpenDS:loadLDIF:ex="+ex);
         }
     } 
 
     /**
       * Returns a one-way hash for passwd using SSHA512 scheme.
       *
       * @param p Clear password string
       * @return hash value
       */
     public static String hash(String p)
     {
         String str = null;
         try {
             byte[] bb = p.getBytes();
             str = SaltedSHA512PasswordStorageScheme.encodeOffline(bb);
         } catch (Exception ex) {
             Debug debug = Debug.getInstance(SetupConstants.DEBUG_NAME);
             debug.error("EmbeddedOpenDS.hash failed : ex="+ex);
         }
         return str;
     }
 
 }
