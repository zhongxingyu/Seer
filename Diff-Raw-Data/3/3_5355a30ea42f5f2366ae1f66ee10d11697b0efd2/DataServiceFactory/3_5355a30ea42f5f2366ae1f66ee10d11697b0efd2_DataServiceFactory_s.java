 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.safeonline.data.ws;
 
 import java.net.URL;
 import javax.xml.namespace.QName;
 import liberty.dst._2006_08.ref.safe_online.DataService;
 
 
 public class DataServiceFactory {
 
     private DataServiceFactory() {
 
         // empty
     }
 
     public static DataService newInstance() {
 
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         URL wsdlUrl = classLoader.getResource( "liberty-idwsf-dst-ref-v2.1-link.wsdl" );
         if (null == wsdlUrl)
             throw new RuntimeException( "Liberty ID-WSF DST Ref WSDL not found" );
 
        DataService dataService = new DataService( wsdlUrl, new QName( "urn:liberty:dst:2006-08:ref:safe-online", "DataService" ) );
        return dataService;
     }
 }
