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
 * $Id: FAMClassLoader.java,v 1.6 2008-04-14 23:54:24 mrudul_uchil Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.classloader;
 
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.List;
 import java.util.ArrayList;
 import javax.servlet.ServletContext;
 import java.util.Arrays;
 import com.sun.identity.common.SystemConfigurationUtil;
 import com.sun.identity.wss.sts.STSConstants;
 
 /**
  * Federated Access Mananger class loader to overcome the class loading
  * issues of jars that are not compatible for Federation Access Manager.
  */
 public class FAMClassLoader {
     
    public static ClassLoader cl;
     
     /** Creates a new instance of FAMClassLoader */
     public FAMClassLoader() {
     }
     
     public static ClassLoader getFAMClassLoader(ServletContext context, 
         String[] reqJars) {
         setSystemProperties();
         if (cl == null) {
             try {
                 URL[] urls = null;
                 if (context != null) {
                     urls = jarFinder(context, reqJars);
                 } else {
                     urls = getJarsFromConfigFile(reqJars);
                 }
 
                 ClassLoader localcc = FAMClassLoader.class.getClassLoader();
 
                 List<String> mask = 
                     new ArrayList<String>(Arrays.asList(maskedPackages));
                 
                 List<String> maskRes =
                     new ArrayList<String>(Arrays.asList(maskedResouces));
 
                 // first create a protected area so that we load WS 2.1 API
                 // and everything that depends on them, inside FAM classloader.
                 localcc = new MaskingClassLoader(localcc,mask,maskRes,urls);
 
                 // then this classloader loads the API and tools.jar
                 cl = new URLClassLoader(urls, localcc);
 
                 //Thread.currentThread().setContextClassLoader(cl);
             } catch (Exception ex) {                
                 ex.printStackTrace();
             }
         }
         if (cl != null) {
             Thread.currentThread().setContextClassLoader(cl);
         }
         return (cl);        
     }
     
     private static URL[] jarFinder(ServletContext context, String[] reqJars) {
         if (reqJars != null) {
             jars = reqJars;
         }
         URL[] urls = new URL[jars.length];
         
         try {
             for (int i=0; i < jars.length; i++) {
                 urls[i] = context.getResource("/WEB-INF/lib/" + jars[i]);
                 System.out.println("FAM urls[" + i + "] : " + 
                                    (urls[i]).toString());
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return urls;
     }
     
     private static URL[] getJarsFromConfigFile(String[] reqJars) {
         if (reqJars != null) {
             jars = reqJars;
         }
         URL[] urls = new URL[jars.length];
         String FILE_BEGIN = "file:";
         String FILE_SEPARATOR = "/";
         String installRoot = System.getProperty("com.sun.aas.installRoot");
         String defaultJarsPath = installRoot + FILE_SEPARATOR + "addons" 
             + FILE_SEPARATOR + "accessmanager";
         String jarsPath = FILE_BEGIN + SystemConfigurationUtil.getProperty(
             STSConstants.FAM_CLASSLOADER_DIR_PATH, defaultJarsPath) 
             + FILE_SEPARATOR;
         try {
             for (int i=0; i < jars.length; i++) {
                 urls[i] = new URL(jarsPath + jars[i]);
                 System.out.println("FAM urls[" + i + "] : " + 
                                    (urls[i]).toString());
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return urls;
     }
     
     private static void setSystemProperties() {
        // Fix for Geronimo Application server and WebLogic 10       
         System.setProperty("javax.xml.soap.MetaFactory", 
             "com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");
         System.setProperty("javax.xml.soap.MessageFactory", 
             "com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl");
         System.setProperty("javax.xml.soap.SOAPConnectionFactory", 
             "com.sun.xml.messaging.saaj.client.p2p.HttpSOAPConnectionFactory");
         System.setProperty("javax.xml.soap.SOAPFactory", 
             "com.sun.xml.messaging.saaj.soap.ver1_1.SOAPFactory1_1Impl");
        System.setProperty("javax.xml.parsers.SAXParserFactory", 
            "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", 
            "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
     }
 
     /**
      * The list of jar files to be loaded by FAMClassLoader.
      */
     public static String[] jars = new String[]{
         "webservices-api.jar",
         "webservices-rt.jar",
         "webservices-tools.jar",
         "webservices-extra-api.jar",
         "webservices-extra.jar",
         "fam.jar"
     };
 
     /**
      * The list of package prefixes we want the
      * {@link MaskingClassLoader} to prevent the parent
      * classLoader from loading.
      */
     public static String[] maskedPackages = new String[]{
         "com.sun.istack.tools.",
         "com.sun.tools.jxc.",
         "com.sun.tools.xjc.",
         "com.sun.tools.ws.",
         "com.sun.codemodel.",
         "com.sun.relaxng.",
         "com.sun.xml.xsom.",
         "com.sun.xml.bind.",
         "com.sun.xml.bind.v2.",
         "com.sun.xml.messaging.",
         "com.sun.xml.ws.",
         "com.sun.xml.ws.addressing.",
         "com.sun.xml.ws.api.",
         "com.sun.xml.ws.api.addressing.",
         "com.sun.xml.ws.server.",
         "com.sun.xml.ws.transport.",
         "com.sun.xml.wss.",
         "com.sun.xml.security.",
         "com.sun.xml.xwss.",
         "javax.xml.bind.",
         "javax.xml.ws.",
         "javax.jws.",
         "javax.jws.soap.",
         "javax.xml.soap.",
         "com.sun.istack.",
         "com.sun.identity.wss."
     };
     
     /**
      * The list of resources we want the
      * {@link MaskingClassLoader} to prevent the parent
      * classLoader from loading.
      */
     public static String[] maskedResouces = new String[]{
         "META-INF/services/javax.xml.bind.JAXBContext",
         "META-INF/services",
         "/META-INF/services",
         "javax/xml/bind/",
         "com/sun/xml/ws/",
         "com/sun/xml/wss/",
         "com/sun/xml/bind/",
         "com/sun/xml/messaging/"
     };
     
     
 }
