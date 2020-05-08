 /* Copyright (c) 2001 - 2008 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.requests;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.geoserver.catalog.impl.CatalogImpl;
 import org.geoserver.catalog.impl.CoverageInfoImpl;
 import org.geoserver.catalog.impl.CoverageStoreInfoImpl;
 import org.geoserver.catalog.impl.DataStoreInfoImpl;
 import org.geoserver.catalog.impl.FeatureTypeInfoImpl;
 import org.geoserver.catalog.impl.LayerInfoImpl;
 import org.geoserver.catalog.impl.NamespaceInfoImpl;
 import org.geoserver.catalog.impl.WorkspaceInfoImpl;
 import org.geoserver.config.impl.GeoServerImpl;
 import org.geoserver.platform.ServiceException;
 import org.geoserver.wms.WMS;
import org.geoserver.wms.WMSInfoImpl;
 import org.vfny.geoserver.wms.WmsException;
 
 import com.mockrunner.mock.web.MockHttpServletRequest;
 
 /**
  * Unit test suite for {@link DescribeLayerKvpRequestReader}
  * 
  * @author Gabriel Roldan
  * @version $Id$
  */
 public class DescribeLayerKvpRequestReaderTest extends TestCase {
 
 	private GeoServerImpl geoServerImpl;
 
 	private WMS wms;
 
 	private MockHttpServletRequest request;
 
 	private Map<String, String> params;
 
 	protected void setUp() throws Exception {
 		geoServerImpl = new GeoServerImpl();
		geoServerImpl.add(new WMSInfoImpl());
 		wms = new WMS(geoServerImpl);
 		request = new MockHttpServletRequest();
 		params = new HashMap<String, String>();
 	}
 
 	protected void tearDown() throws Exception {
 		wms = null;
 		request = null;
 		params = null;
 	}
 
 	private DescribeLayerRequest getRequest(Map<String, String> params)
 			throws ServiceException {
 
 		DescribeLayerKvpRequestReader reader = new DescribeLayerKvpRequestReader(
 				params, wms);
 		return (DescribeLayerRequest) reader.getRequest(request);
 	}
 
 	public void testGetRequestNullHttpReq() {
 		try {
 			getRequest(null);
 			fail("expected NPE on null request");
 		} catch (NullPointerException e) {
 			assertTrue(true);
 		}
 	}
 
 	public void testGetRequestNoVersion() {
 		params.put("LAYERS", "topp:states");
 		try {
 			getRequest(params);
 			fail("expected ServiceException if version is not provided");
 		} catch (WmsException e) {
 			assertEquals("NoVersionInfo", e.getCode());
 		}
 	}
 
 	public void testGetRequestInvalidVersion() {
 		params.put("LAYERS", "topp:states");
 		params.put("VERSION", "fakeVersion");
 		try {
 			getRequest(params);
 			fail("expected ServiceException if the wrong version is requested");
 		} catch (WmsException e) {
 			assertEquals("InvalidVersion", e.getCode());
 		}
 	}
 
 	public void testGetRequestNoLayerRequested() {
 		params.put("VERSION", "1.1.1");
 		try {
 			getRequest(params);
 			fail("expected ServiceException if no layer is requested");
 		} catch (WmsException e) {
 			assertEquals("NoLayerRequested", e.getCode());
 		}
 	}
 
 	public void testGetRequestLayerNotDefined() {
 		CatalogImpl catalog = new CatalogImpl();
 		geoServerImpl.setCatalog(catalog);
 		NamespaceInfoImpl ns = new NamespaceInfoImpl();
 		ns.setPrefix("topp");
 		ns.setURI("http//www.geoserver.org");
 		catalog.add(ns);
 
 		params.put("VERSION", "1.1.1");
 		params.put("LAYERS", "topp:states");
 		try {
 			getRequest(params);
 			fail("expected ServiceException if no layer is requested");
 		} catch (WmsException e) {
 			assertEquals("LayerNotDefined", e.getCode());
 		}
 	}
 
 	public void testGetRequest() {
 		CatalogImpl catalog = new CatalogImpl();
 		geoServerImpl.setCatalog(catalog);
 		NamespaceInfoImpl ns = new NamespaceInfoImpl();
 		ns.setPrefix("topp");
 		ns.setURI("http//www.geoserver.org");
 
 		WorkspaceInfoImpl workspace = new WorkspaceInfoImpl();
 		workspace.setId("fakeWs");
 		workspace.setName("fakeWs");
 
 		DataStoreInfoImpl dataStoreInfo = new DataStoreInfoImpl(catalog);
 		dataStoreInfo.setName("fakeDs");
 		dataStoreInfo.setId("fakeDs");
 		dataStoreInfo.setWorkspace(workspace);
 
 		FeatureTypeInfoImpl featureTypeInfo = new FeatureTypeInfoImpl(catalog);
 		featureTypeInfo.setNamespace(ns);
 		featureTypeInfo.setName("states");
 		featureTypeInfo.setStore(dataStoreInfo);
 
 		LayerInfoImpl layerInfo = new LayerInfoImpl();
 		layerInfo.setId("states");
 		layerInfo.setName("states");
 		layerInfo.setResource(featureTypeInfo);
 
 		catalog.add(ns);
 		catalog.add(workspace);
 		catalog.add(dataStoreInfo);
 		catalog.add(featureTypeInfo);
 		catalog.add(layerInfo);
 
 		params.put("VERSION", "1.1.1");
 		params.put("LAYERS", "topp:states");
 
 		DescribeLayerRequest describeRequest = getRequest(params);
 		assertNotNull(describeRequest);
 		assertNotNull(describeRequest.getLayers());
 		assertEquals(1, describeRequest.getLayers().size());
 
 		CoverageStoreInfoImpl coverageStoreInfo = new CoverageStoreInfoImpl(
 				catalog);
 		coverageStoreInfo.setId("coverageStore");
 		coverageStoreInfo.setName("coverageStore");
 		coverageStoreInfo.setWorkspace(workspace);
 
 		CoverageInfoImpl coverageInfo = new CoverageInfoImpl(catalog);
 		coverageInfo.setNamespace(ns);
 		coverageInfo.setName("fakeCoverage");
 		coverageInfo.setStore(coverageStoreInfo);
 
 		layerInfo = new LayerInfoImpl();
 		layerInfo.setId("fakeCoverage");
 		layerInfo.setName("fakeCoverage");
 		layerInfo.setResource(coverageInfo);
 
 		catalog.add(coverageStoreInfo);
 		catalog.add(coverageInfo);
 		catalog.add(layerInfo);
 
 		params.put("LAYERS", "topp:states,topp:fakeCoverage");
 		describeRequest = getRequest(params);
 		assertNotNull(describeRequest);
 		assertNotNull(describeRequest.getLayers());
 		assertEquals(2, describeRequest.getLayers().size());
 	}
 }
