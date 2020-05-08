 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans.util;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * A Utility class for updating the the catalina.properties to load the deployed ibeans modules
  */
 public class TomcatUpdater
 {
     public static final String SHARED_LOADER_PROPERTY = "shared.loader";
 
     public static void main(String[] args)
     {
         String basepath = null;
 
         boolean libsOnly = false;
         boolean webappsOnly = false;
 
         for (int i = 0; i < args.length; i++)
         {
             String arg = args[i];
             if (arg.equals("-b"))
             {
                 basepath = args[++i];
             }
             else if (arg.equals("-l"))
             {
                 libsOnly = true;
             }
             else if (arg.equals("-w"))
             {
                 webappsOnly = true;
             }
             else if (arg.equals("-u"))
             {
                 usage();
                 System.exit(0);
             }
             else
             {
                System.err.println("Unkwonn argument:" + arg);
                 usage();
                 System.exit(1);
             }
 
         }
 
 
         if (System.getProperty("catalina.home") != null && basepath == null)
         {
             basepath = System.getProperty("catalina.home");
         }
         //This is our fallback, it assumes this class is run in ${CATALINA_HOME}/mule-ibeans/bin
         if (basepath == null)
         {
             basepath = "../..";
         }
 
         try
         {
             basepath = new File(basepath).getCanonicalPath();
         }
         catch (IOException e)
         {
             e.printStackTrace();
             System.exit(-1);
         }
         String filename = basepath + "/conf/catalina.properties";
         File catalinaProps = new File(filename);
         if (!catalinaProps.exists())
         {
            System.err.println("Could not find catlina properties at: " + filename + ". Make sure you have the -Dcatalina.home=$CATALINA_HOME set or pass in the home path when running this util");
             System.exit(1);
         }
 
         System.out.println("Base path is: " + basepath);
         if (!webappsOnly)
         {
             addModulesClasspath(basepath, catalinaProps);
         }
 
         if (!libsOnly)
         {
             copyWebApps(basepath);
         }
     }
 
     public static void usage()
     {
         System.err.println("Valid options for the iBeans Tomcat updater tool:");
         System.err.println("-b [basepath] : The location of the Tcat or Tomcat root directory. This can also be set by defining a VM parameter: -Dcatalina.home=[basepath]");
         System.err.println("-l : Only update the lib classpath entries (do not copy bundled webapps)");
         System.err.println("-w : Copy the bundled webapps (do not update classpath entries)");
         System.err.println("-u : Display this information");
     }
 
     protected static void addModulesClasspath(String basepath, File catalinaProps)
     {
         try
         {
 
             String modulesPath = basepath + "/mule-ibeans/lib/modules";
             File modules = new File(modulesPath);
             if (!modules.exists())
             {
                System.err.println("Could not find Mule iBeans modules: " + modulesPath + ". Make sure you have the CATALINE_HOME set or pass in the home path when running this util");
                 System.exit(1);
             }
             StringBuffer sharedLoader = new StringBuffer();
             sharedLoader.append(SHARED_LOADER_PROPERTY).append("=");
             sharedLoader.append("${catalina.home}/mule-ibeans/conf,${catalina.home}/mule-ibeans/lib/*.jar,${catalina.home}/mule-ibeans/lib/ibeans/deployed/*.jar");
             File deployed = new File(modulesPath, "deployed");
             File[] fileModules = deployed.listFiles();
             for (int i = 0; i < fileModules.length; i++)
             {
                 File fileModule = fileModules[i];
                 if (fileModule.isDirectory())
                 {
                     sharedLoader.append(",${catalina.home}/mule-ibeans/lib/modules/deployed/").append(fileModule.getName()).append("/*.jar");
                 }
             }
 
             File newConfig = new File(catalinaProps.getAbsolutePath() + ".new");
             if (newConfig.exists())
             {
                 newConfig.delete();
                 if (!newConfig.createNewFile())
                 {
                    System.err.println("Failed to create temp catalina.properties");
                     System.exit(1);
                 }
             }
             BufferedWriter writer = new BufferedWriter(new FileWriter(newConfig));
 
             BufferedReader reader = new BufferedReader(new FileReader(catalinaProps));
             String s;
             try
             {
                 while ((s = reader.readLine()) != null)
                 {
                     if (s.startsWith(SHARED_LOADER_PROPERTY))
                     {
                         System.out.println("Setting shared loader to: " + sharedLoader + ". Old value is: " + s);
                         writer.write(sharedLoader.toString());
                         writer.newLine();
                     }
                     else
                     {
                         writer.write(s);
                         writer.newLine();
                     }
                 }
                 writer.flush();
                 writer.close();
 
                 safeCopyFile(newConfig, catalinaProps);
             }
             finally
             {
                 writer.close();
                 reader.close();
                 newConfig.delete();
             }
 
 
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
     }
 
     protected static void copyWebApps(String basepath)
     {
         File wa = new File(basepath, "mule-ibeans/webapps");
         File[] apps = wa.listFiles();
         for (int i = 0; i < apps.length; i++)
         {
             File app = apps[i];
             File to = new File(basepath, "webapps/" + app.getName());
             try
             {
                 if (to.createNewFile() || to.exists())
                 {
                     System.out.println("Copying app: " + app.getAbsolutePath() + " to " + to.getAbsolutePath());
                     safeCopyFile(app, to);
                 }
                 else
                 {
                     System.err.println("Unable to create file: " + to.getAbsolutePath());
                 }
             }
             catch (IOException e)
             {
                 System.err.println("Unable to create file: " + to.getAbsolutePath() + ", " + e.getMessage());
             }
         }
     }
 
     public static void safeCopyFile(File in, File out)
     {
         try
         {
             FileInputStream fis = new FileInputStream(in);
             FileOutputStream fos = new FileOutputStream(out);
             try
             {
                 byte[] buf = new byte[1024];
                 int i = 0;
                 while ((i = fis.read(buf)) != -1)
                 {
                     fos.write(buf, 0, i);
                 }
             }
             catch (IOException e)
             {
                 throw e;
             }
             finally
             {
                 try
                 {
                     fis.close();
                     fos.close();
                 }
                 catch (IOException e)
                 {
                     e.printStackTrace(System.err);
                 }
 
             }
         }
         catch (IOException e)
         {
             e.printStackTrace(System.err);
         }
     }
 }
