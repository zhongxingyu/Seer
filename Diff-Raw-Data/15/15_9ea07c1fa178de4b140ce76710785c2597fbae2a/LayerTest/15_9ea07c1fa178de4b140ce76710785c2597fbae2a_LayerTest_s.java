 /* Copyright (c) 2001 - 2009 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.catalog.rest;
 
 import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
 
 import org.geoserver.catalog.LayerInfo;
 import org.w3c.dom.Document;
 
 import com.mockrunner.mock.web.MockHttpServletResponse;
 
 public class LayerTest extends CatalogRESTTestSupport {
 
     public void testGetAsXML() throws Exception {
         Document dom = getAsDOM( "/rest/layers/cite:Buildings.xml");
         
         assertEquals( "layer", dom.getDocumentElement().getNodeName() );
         assertXpathEvaluatesTo("Buildings", "/layer/name", dom );
     }
     
     public void testGetAsHTML() throws Exception {
         getAsDOM("/rest/layers/cite:Buildings.html" );
     }
     
     public void testGetAllAsXML() throws Exception {
         Document dom = getAsDOM( "/rest/layers.xml");
         assertXpathEvaluatesTo(catalog.getLayers().size()+"", "count(//layer)", dom );
     }
     
     public void testGetAllAsHTML() throws Exception {
         getAsDOM( "/rest/layers.html");
     }
     
     public void testPut() throws Exception {
         LayerInfo l = catalog.getLayerByName( "cite:Buildings" );
         assertEquals( "Buildings", l.getDefaultStyle().getName() );
         String xml = 
             "<layer>" +
               "<defaultStyle>Forests</defaultStyle>" + 
               "<styles>" + 
                 "<style>Ponds</style>" +
               "</styles>" + 
             "</layer>";
         MockHttpServletResponse response = 
             putAsServletResponse("/rest/layers/cite:Buildings", xml, "text/xml");
         assertEquals( 200, response.getStatusCode() );
         
         l = catalog.getLayerByName("cite:Buildings");
         assertEquals( "Forests", l.getDefaultStyle().getName() );
     }
 }
