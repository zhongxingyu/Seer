 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses;
 
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 
 import junit.framework.TestCase;
 
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.ows.adapters.ResponseAdapter;
 import org.geoserver.platform.ServiceException;
 import org.geoserver.wms.MapLayerInfo;
 import org.geoserver.wms.WMS;
 import org.geoserver.wms.WMSMockData;
 import org.geoserver.wms.WMSMockData.DummyRasterMapProducer;
 import org.vfny.geoserver.Response;
 import org.vfny.geoserver.wms.GetMapProducer;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.vfny.geoserver.wms.responses.map.metatile.MetatileMapProducer;
 
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Point;
 
 /**
  * Unit test for {@link GetMapResponse}
  * <p>
  * Trying to mock up collaborators lead to 3 direct ones needing to be mocked up, plus {@link WMS}
  * obtained from this test super class. Smells like too much coupling to me, though I'm not going to
  * change that until we have a plan to get rid of the old {@link Response} stuff in favor of the new
  * dispatching system, for which GetMapResponse is being adapted right now ({@link ResponseAdapter})
  * </p>
  * 
  * @author Gabriel Roldan (TOPP)
  * @version $Id$
  * @since 2.5.x
  * @source $URL:
  *         https://svn.codehaus.org/geoserver/branches/1.7.x/geoserver/wms/src/test/java/org/vfny
  *         /geoserver/wms/responses/GetMapResponseTest.java $
  */
 public class GetMapResponseTest extends TestCase {
 
     private WMSMockData mockData;
 
     private GetMapRequest request;
 
     private GetMapResponse response;
 
     @Override
     protected void setUp() throws Exception {
         GetMapResponse.LOGGER.setLevel(Level.FINEST);
         mockData = new WMSMockData();
         mockData.setUp();
 
         request = mockData.createRequest();
         //add a layer so its a valid request
         MapLayerInfo layer = mockData.addFeatureTypeLayer("testType", Point.class);
         request.setLayers(new MapLayerInfo[] { layer });
 
         response = mockData.createResponse();
     }
 
     @Override
     protected void tearDown() throws Exception {
         GetMapResponse.LOGGER.setLevel(Level.INFO);
     }
 
     public void testConstructor() {
         try {
             new GetMapResponse(null);
             fail("should fail on null list of available producers");
         } catch (NullPointerException e) {
             assertTrue(true);
         }
         try {
             Collection<GetMapProducer> producers = Collections.emptyList();
             new GetMapResponse(producers);
             fail("should fail on empty list of available producers");
         } catch (IllegalArgumentException e) {
             assertTrue(true);
         }
     }
 
     /**
      * Test method for {@link GetMapResponse#execute(org.vfny.geoserver.Request)}.
      */
     public void testDelegateLookup() {
         GetMapProducer producer = new WMSMockData.DummyRasterMapProducer();
         response = new GetMapResponse(Collections.singleton(producer));
         request.setFormat(WMSMockData.DummyRasterMapProducer.MIME_TYPE);
 
         response.execute(request);
 
         GetMapProducer delegate = response.getDelegate();
         assertSame(producer, delegate);
     }
 
     /**
      * Test method for {@link GetMapResponse#execute(org.vfny.geoserver.Request)}.
      */
     public void testExecuteNoExtent() {
         request.setBbox(null);
         assertInvalidMandatoryParam("MissingBBox");
     }
 
     public void testExecuteEmptyExtent() {
         request.setBbox(new Envelope());
         assertInvalidMandatoryParam("InvalidBBox");
     }
 
     public void testExecuteTilingRequested() {
         request.setBbox(new Envelope(-180, -90, 180, 90));
         // request tiling
         request.setTiled(true);
         request.setTilesOrigin(new Point2D.Double(0, 0));
         request.setWidth(256);
         request.setHeight(256);
 
         try {
             response.execute(request);
             fail("Expected failure");
         } catch (RuntimeException e) {
             // let execute crash, we're only interested in the delegate
             assertTrue(true);
         }
         GetMapProducer delegate = response.getDelegate();
         assertTrue(delegate instanceof MetatileMapProducer);
     }
 
     public void testSingleVectorLayer() throws IOException {
         DummyRasterMapProducer producer = new DummyRasterMapProducer();
         response = new GetMapResponse(Collections.singleton((GetMapProducer) producer));
         request.setFormat(DummyRasterMapProducer.MIME_TYPE);
 
         MapLayerInfo layer = mockData.addFeatureTypeLayer("testSingleVectorLayer", Point.class);
         request.setLayers(new MapLayerInfo[] { layer });
 
         response.execute(request);
         
         assertNotNull(producer.mapContext);
         
         assertEquals(1, producer.mapContext.getLayerCount());
         
         assertEquals(DummyRasterMapProducer.MIME_TYPE, producer.outputFormat);
         
         assertFalse(producer.abortCalled);
         
         assertTrue(producer.produceMapCalled);
     }
 
     public void testExecuteNoLayers() throws Exception {
        request.setLayers((List<LayerInfo>) null);
         assertInvalidMandatoryParam("LayerNotDefined");
     }
 
     public void testExecuteNoWidth() {
         request.setWidth(0);
         assertInvalidMandatoryParam("MissingOrInvalidParameter");
 
         request.setWidth(-1);
         assertInvalidMandatoryParam("MissingOrInvalidParameter");
     }
 
     public void testExecuteNoHeight() {
         request.setHeight(0);
         assertInvalidMandatoryParam("MissingOrInvalidParameter");
 
         request.setHeight(-1);
         assertInvalidMandatoryParam("MissingOrInvalidParameter");
     }
 
     public void testExecuteInvalidFormat() {
         request.setFormat("non-existent-output-format");
         assertInvalidMandatoryParam("InvalidFormat");
     }
 
     public void testExecuteNoFormat() {
         request.setFormat(null);
         assertInvalidMandatoryParam("InvalidFormat");
     }
 
     public void testExecuteNoStyles() {
         request.setStyles(null);
         assertInvalidMandatoryParam("StyleNotDefined");
     }
 
     private void assertInvalidMandatoryParam(String expectedExceptionCode) {
         try {
             response.execute(request);
             fail("Expected ServiceException");
         } catch (ServiceException e) {
             assertEquals(expectedExceptionCode, e.getCode());
         }
     }
 
 }
