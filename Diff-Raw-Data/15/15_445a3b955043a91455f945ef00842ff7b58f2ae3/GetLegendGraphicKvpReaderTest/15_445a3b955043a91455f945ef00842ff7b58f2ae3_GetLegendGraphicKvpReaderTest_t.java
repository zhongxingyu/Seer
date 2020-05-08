 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.legend;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import junit.framework.Test;
 
 import org.geoserver.test.ows.KvpRequestReaderTestSupport;
import org.geoserver.wms.WMS;
import org.geoserver.wms.kvp.WMSKvpRequestReaderAdapter;
 import org.geotools.styling.Style;
 import org.vfny.geoserver.wms.requests.GetLegendGraphicKvpReader;
 import org.vfny.geoserver.wms.requests.GetLegendGraphicRequest;
 import org.vfny.geoserver.wms.servlets.GetLegendGraphic;
 
 import com.mockrunner.mock.web.MockHttpServletRequest;
 
 
 public class GetLegendGraphicKvpReaderTest extends KvpRequestReaderTestSupport {
     
    WMSKvpRequestReaderAdapter reader;
     
     /**
      * This is a READ ONLY TEST so we can use one time setup
      */
     public static Test suite() {
         return new OneTimeTestSetup(new GetLegendGraphicKvpReaderTest());
     }
     
     protected void setUpInternal() throws Exception {
         super.setUpInternal();
         GetLegendGraphic getLegend = (GetLegendGraphic) applicationContext.getBean("wmsGetLegendGraphic");
        WMS wms = new WMS(getGeoServer());
        reader = new WMSKvpRequestReaderAdapter(GetLegendGraphicRequest.class, GetLegendGraphicKvpReader.class, wms);
     }
     
     
 
     /**
      * This test ensures that when a SLD parameter has been passed that refers
      * to a SLD document with multiple styles, the required one is choosed based
      * on the LAYER parameter.
      * <p>
      * This is the case where a remote SLD document is used in "library" mode.
      * </p>
      */
     public void testRemoteSLDMultipleStyles() throws Exception {
         Map params = new HashMap();
         params.put("VERSION", "1.0.0");
         params.put("REQUEST", "GetLegendGraphic");
         params.put("LAYER", "cite:Ponds");
         params.put("FORMAT", "image/png");
 
         params.put("STYLE", "Ponds");
         params.put("FEATURETYPE", "fake_not_used");
         params.put("SCALE", "1000");
         params.put("WIDTH", "120");
         params.put("HEIGHT", "90");
         
         final URL remoteSldUrl = getClass().getResource("MultipleStyles.sld");
         params.put("SLD", remoteSldUrl.toExternalForm());
         params.put("LAYER", "cite:Ponds");
         params.put("STYLE", "Ponds");
         
         reader.setHttpRequest(buildMockRequest(params));
         GetLegendGraphicRequest request = (GetLegendGraphicRequest) reader.createRequest();
 
         //the style names Ponds is declared in third position on the sld doc
         Style selectedStyle = request.getStyle();
         assertNotNull(selectedStyle);
         assertEquals("Ponds", selectedStyle.getName());
 
         params.put("LAYER", "cite:Lakes");
         params.put("STYLE", "Lakes");
         reader.setHttpRequest(buildMockRequest(params));
         request = (GetLegendGraphicRequest) reader.createRequest();
 
         //the style names Ponds is declared in third position on the sld doc
         selectedStyle = request.getStyle();
         assertNotNull(selectedStyle);
         assertEquals("Lakes", selectedStyle.getName());
     }
 
 
 
     protected MockHttpServletRequest buildMockRequest(Map params) {
         MockHttpServletRequest mockRequest = new MockHttpServletRequest();
         for (Iterator ot = params.entrySet().iterator(); ot.hasNext();) {
             Map.Entry entry = (Map.Entry) ot.next();
             mockRequest.setupAddParameter((String) entry.getKey(), (String) entry.getValue());
         }
         return mockRequest;
     }
 }
