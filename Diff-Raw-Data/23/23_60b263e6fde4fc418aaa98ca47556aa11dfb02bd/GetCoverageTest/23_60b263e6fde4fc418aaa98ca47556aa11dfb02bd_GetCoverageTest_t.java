 package org.geoserver.wcs;
 
 import static org.vfny.geoserver.wcs.WcsException.WcsExceptionCode.InvalidParameterValue;
 
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.opengis.wcs.v1_1_1.GetCoverageType;
 
 import org.geoserver.wcs.kvp.GetCoverageRequestReader;
 import org.geoserver.wcs.test.WCSTestSupport;
 import org.geoserver.wcs.xml.v1_1_1.WCSConfiguration;
 import org.geoserver.wcs.xml.v1_1_1.WcsXmlReader;
 import org.geotools.coverage.grid.GridCoverage2D;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.operation.transform.AffineTransform2D;
 import org.opengis.coverage.grid.GridCoverage;
 import org.opengis.coverage.grid.GridRange;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.wcs.WcsException;
 
 public class GetCoverageTest extends WCSTestSupport {
 
     private static final double EPS = 10 - 6;
 
     private GetCoverageRequestReader kvpreader;
 
     private WebCoverageService111 service;
 
     private Data catalog;
 
     private WCSConfiguration configuration;
 
     private WcsXmlReader xmlReader;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         kvpreader = (GetCoverageRequestReader) applicationContext
                 .getBean("wcs111GetCoverageRequestReader");
         service = (WebCoverageService111) applicationContext.getBean("wcs111ServiceTarget");
         catalog = (Data) applicationContext.getBean("catalog");
         configuration = new WCSConfiguration();
         xmlReader = new WcsXmlReader("GetCoverage", "1.1.1", configuration);
     }
 
 //    @Override
 //    protected String getDefaultLogConfiguration() {
 //        return "/DEFAULT_LOGGING.properties";
 //    }
     
     
 
     public void testKvpBasic() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "image/geotiff");
         raw.put("BoundingBox", "-90,-180,90,180,urn:ogc:def:crs:EPSG:6.6:4326");
         raw.put("store", "false");
         raw.put("GridBaseCRS", "urn:ogc:def:crs:EPSG:6.6:4326");
 
         GridCoverage[] coverages = executeGetCoverageKvp(raw);
         assertEquals(1, coverages.length);
         GridCoverage2D coverage = (GridCoverage2D) coverages[0];
         assertEquals(CRS.decode("urn:ogc:def:crs:EPSG:6.6:4326"), coverage.getEnvelope()
                 .getCoordinateReferenceSystem());
     }
 
     public void testWrongFormatParams() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "SuperCoolFormat");
         raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
         try {
             GridCoverage[] coverages = executeGetCoverageKvp(raw);
             fail("When did we learn to encode SuperCoolFormat?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.toString(), e.getCode());
             assertEquals("format", e.getLocator());
         }
     }
 
    public void testDefaultGridOrigin() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "image/geotiff");
         raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
 
         GridCoverage[] coverages = executeGetCoverageKvp(raw);
        AffineTransform2D tx = (AffineTransform2D) coverages[0].getGridGeometry().getGridToCRS();
        assertEquals(0.0, tx.getTranslateX());
        assertEquals(0.0, tx.getTranslateY());
    }
    
    public void testWrongGridOrigin() throws Exception {
        Map<String, Object> raw = new HashMap<String, Object>();
        final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
        raw.put("identifier", layerId);
        raw.put("format", "image/geotiff");
        raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
         raw.put("GridOrigin", "12,13,14");
         try {
             executeGetCoverageKvp(raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("GridOrigin", e.getLocator());
         }
     }
 
     public void testWrongGridOffsets() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "image/geotiff");
         raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
         raw.put("GridBaseCRS", "urn:ogc:def:crs:EPSG:6.6:4326");
         raw.put("GridOffsets", "12,13,14");
         try {
             executeGetCoverageKvp(raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("GridOffsets", e.getLocator());
         }
     }
     
     public void testNoGridOffsets() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "image/geotiff");
         raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
         raw.put("GridBaseCRS", "urn:ogc:def:crs:EPSG:6.6:4326");
 
         GridCoverage[] coverages = executeGetCoverageKvp(raw);
         GridCoverage original = catalog.getCoverageInfo(layerId).getCoverage();
 
         final AffineTransform2D originalTx = (AffineTransform2D) original.getGridGeometry()
                 .getGridToCRS();
         final AffineTransform2D flippedTx = (AffineTransform2D) coverages[0].getGridGeometry()
                 .getGridToCRS();
         assertEquals(originalTx.getScaleX(), flippedTx.getShearY(), EPS);
         assertEquals(originalTx.getScaleY(), flippedTx.getShearX(), EPS);
         assertEquals(originalTx.getShearX(), flippedTx.getScaleY(), EPS);
         assertEquals(originalTx.getShearY(), flippedTx.getScaleX(), EPS);
        assertEquals(0.0, flippedTx.getTranslateY(), EPS);
        assertEquals(0.0, flippedTx.getTranslateX(), EPS);
     }
     
     /**
      * This one needs to be reactivated when GEOS-1701 is fixed (along with other tests
      * to make sure rotated tx are correct)
      */
 //    public void testGridOffsetsSubsample() throws Exception {
 //        Map<String, Object> raw = new HashMap<String, Object>();
 //        final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
 //        raw.put("identifier", layerId);
 //        raw.put("format", "image/geotiff");
 //        raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
 //        raw.put("GridBaseCRS", "urn:ogc:def:crs:EPSG:6.6:4326");
 //        raw.put("GridType", "urn:ogc:def:method:WCS:1.1:2dSimpleGrid");
 //        raw.put("GridOffsets", "0.1,0.1");
 //        GridCoverage[] coverages = executeGetCoverageKvp(raw);
 //        final AffineTransform2D flippedTx = (AffineTransform2D) coverages[0].getGridGeometry()
 //        .getGridToCRS();
 //        assertEquals(0.0, flippedTx.getShearY(), EPS);
 //        assertEquals(0.0, flippedTx.getShearX(), EPS);
 //        assertEquals(0.1, flippedTx.getScaleY(), EPS);
 //        assertEquals(0.1, flippedTx.getScaleX(), EPS);
 //        assertEquals(0.0, flippedTx.getTranslateY(), EPS);
 //        assertEquals(0.0, flippedTx.getTranslateX(), EPS);
 //    }
 
     /**
      * Tests valid range subset expressions, but with a mix of valid and invalid
      * identifiers
      * 
      * @throws Exception
      */
     public void testInvalidRangeSubset() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "image/geotiff");
         raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
 
         // unknown field
         raw.put("rangeSubset", "jimbo:nearest");
         try {
             executeGetCoverageKvp(raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("RangeSubset", e.getLocator());
         }
 
         // unknown axis
         raw.put("rangeSubset", "contents:nearest[MadAxis[key]]");
         try {
             executeGetCoverageKvp(raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("RangeSubset", e.getLocator());
         }
 
         // unknown key
         raw.put("rangeSubset", "contents:nearest[Bands[MadKey]]");
         try {
             executeGetCoverageKvp(raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("RangeSubset", e.getLocator());
         }
     }
     
     public void testRangeSubsetSingle() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "image/geotiff");
         raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
 
         // extract all bands. We had two bugs here, one related to the case sensitiveness
         // and the other about the inability to extract bands at all (with exception of the red one) 
         String[] bands = new String[] {"Red_Band", "GREEN_BAND", "blue_band"};
         for (int i = 0; i < bands.length; i++) {
             raw.put("rangeSubset", "contents:nearest[Bands[" + bands[i] + "]]");
             GridCoverage[] coverages = executeGetCoverageKvp(raw);
             assertEquals(1, coverages[0].getNumSampleDimensions());
             final String coverageBand = coverages[0].getSampleDimension(0).getDescription().toString();
             assertEquals(bands[i].replace('_', ' ').toLowerCase(), coverageBand.toLowerCase());
         }
     }
     
     public void testRangeSubsetMulti() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "image/geotiff");
         raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
 
         raw.put("rangeSubset", "contents:nearest[Bands[Red_band,Blue_band]]");
         GridCoverage[] coverages = executeGetCoverageKvp(raw);
         assertEquals(2, coverages[0].getNumSampleDimensions());
         assertEquals("Red band", coverages[0].getSampleDimension(0).getDescription().toString());
         assertEquals("Blue band", coverages[0].getSampleDimension(1).getDescription().toString());
     }
     
     public void testRangeSubsetSwap() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "image/geotiff");
         raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
 
         raw.put("rangeSubset", "contents:nearest[Bands[Blue_band,Green_band]]");
         GridCoverage[] coverages = executeGetCoverageKvp(raw);
         assertEquals(2, coverages[0].getNumSampleDimensions());
         assertEquals("Blue band", coverages[0].getSampleDimension(0).getDescription().toString());
         assertEquals("Green band", coverages[0].getSampleDimension(1).getDescription().toString());
     }
     
     public void testNullGridOrigin() throws Exception {
         String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
             "<wcs:GetCoverage service=\"WCS\" " + //
             "xmlns:ows=\"http://www.opengis.net/ows/1.1\"\r\n" + // 
             "  xmlns:wcs=\"http://www.opengis.net/wcs/1.1.1\"\r\n" + //
             "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \r\n" + //
             "  xsi:schemaLocation=\"http://www.opengis.net/wcs/1.1.1 " + //
             "schemas/wcs/1.1.1/wcsAll.xsd\"\r\n" + //
             "  version=\"1.1.1\" >\r\n" + //
             "  <ows:Identifier>wcs:BlueMarble</ows:Identifier>\r\n" + //
             "  <wcs:DomainSubset>\r\n" + //
             "    <ows:BoundingBox crs=\"urn:ogc:def:crs:EPSG:6.6:4326\">\r\n" + //
             "      <ows:LowerCorner>-90 -180</ows:LowerCorner>\r\n" + //
             "      <ows:UpperCorner>90 180</ows:UpperCorner>\r\n" + //
             "    </ows:BoundingBox>\r\n" + //
             "  </wcs:DomainSubset>\r\n" + //
             "  <wcs:Output format=\"image/tiff\">\r\n" + //
             "    <wcs:GridCRS>\r\n" + //
             "      <wcs:GridBaseCRS>urn:ogc:def:crs:EPSG:6.6:4326</wcs:GridBaseCRS>\r\n" + //
             "      <wcs:GridType>urn:ogc:def:method:WCS:1.1:2dSimpleGrid</wcs:GridType>\r\n" + //
             "      <wcs:GridOffsets>1 2</wcs:GridOffsets>\r\n" + //
             "    </wcs:GridCRS>\r\n" + //
             "  </wcs:Output>\r\n" + //
             "</wcs:GetCoverage>";
     
         GridCoverage[] coverages = executeGetCoverageXml(request);
     }
     
     /**
      * Runs GetCoverage on the specified parameters and returns an array of coverages
      */
     GridCoverage[] executeGetCoverageKvp(Map<String, Object> raw) throws Exception {
         GetCoverageType getCoverage = (GetCoverageType) kvpreader.read(kvpreader.createRequest(),
                 parseKvp(raw), raw);
         return service.getCoverage(getCoverage);
     }
     
     /**
      * Runs GetCoverage on the specified parameters and returns an array of coverages
      */
     GridCoverage[] executeGetCoverageXml(String request) throws Exception {
         GetCoverageType getCoverage = (GetCoverageType) xmlReader.read(null, new StringReader(
                 request), null);
         return service.getCoverage(getCoverage);
     }
     
     
 
 }
