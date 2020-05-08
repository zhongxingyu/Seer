 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wfs;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.custommonkey.xmlunit.SimpleNamespaceContext;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.geoserver.data.test.MockData;
 import org.geoserver.test.GeoServerTestSupport;
 import org.geoserver.wfs.xml.v1_0_0.WFSConfiguration;
 
 
 /**
  * Base support class for wfs tests.
  * <p>
  * Deriving from this test class provides the test case with preconfigured
  * geoserver and wfs objects.
  * </p>
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  *
  */
 public class WFSTestSupport extends GeoServerTestSupport {
     /**
      * @return The global wfs instance from the application context.
      */
     protected WFS getWFS() {
         return (WFS) applicationContext.getBean("wfs");
     }
     
     /**
      * @return The 1.0 xml configuration.
      */
     protected WFSConfiguration getXmlConfiguration10() {
         return (WFSConfiguration) applicationContext.getBean( "wfsXmlConfiguration-1.0" );
     }
     
     /**
      * @return The 1.1 xml configuration.
      */
     protected org.geoserver.wfs.xml.v1_1_0.WFSConfiguration getXmlConfiguration11() {
         return  (org.geoserver.wfs.xml.v1_1_0.WFSConfiguration) applicationContext.getBean( "wfsXmlConfiguration-1.1" );
     }
     
     @Override
     protected void oneTimeSetUp() throws Exception {
         super.oneTimeSetUp();
         
         // init xmlunit
         Map<String, String> namespaces = new HashMap<String, String>();
         namespaces.put("wfs", "http://www.opengis.net/wfs");
         namespaces.put("ows", "http://www.opengis.net/ows");
         namespaces.put("xs", "http://www.w3.org/2001/XMLSchema");
         namespaces.put("xsd", "http://www.w3.org/2001/XMLSchema");
         namespaces.put("gml", "http://www.opengis.net/gml");
         namespaces.put(MockData.CITE_PREFIX, MockData.CITE_URI); 
         namespaces.put(MockData.CDF_PREFIX, MockData.CDF_URI);
         namespaces.put(MockData.CGF_PREFIX, MockData.CGF_URI);
         namespaces.put(MockData.SF_PREFIX, MockData.SF_URI);
         XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(namespaces));
     }
 }
