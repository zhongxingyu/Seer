 package org.geoserver.wcs;
 
 import static org.vfny.geoserver.wcs.WcsException.WcsExceptionCode.InvalidParameterValue;
 
 import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 
import net.opengis.wcs.v1_1_1.AxisSubsetType;
import net.opengis.wcs.v1_1_1.FieldSubsetType;
 import net.opengis.wcs.v1_1_1.GetCoverageType;
import net.opengis.wcs.v1_1_1.RangeSubsetType;
 
 import org.geoserver.wcs.kvp.GetCoverageRequestReader;
 import org.geoserver.wcs.test.WCSTestSupport;
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
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         kvpreader = (GetCoverageRequestReader) applicationContext
                 .getBean("wcs111GetCoverageRequestReader");
         service = (WebCoverageService111) applicationContext.getBean("wcs111ServiceTarget");
         catalog = (Data) applicationContext.getBean("catalog");
     }
 
 //    @Override
 //    protected String getDefaultLogConfiguration() {
 //        return "/DEFAULT_LOGGING.properties";
 //    }
     
     /**
      * Runs GetCoverage on the specified parameters and returns an array of coverages
      */
     GridCoverage[] executeGetCoverage(Map<String, Object> raw) throws Exception {
         GetCoverageType getCoverage = (GetCoverageType) kvpreader.read(kvpreader.createRequest(),
                 parseKvp(raw), raw);
         return service.getCoverage(getCoverage);
     }
 
     public void testKvpBasic() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "GeoTiff");
         raw.put("BoundingBox", "-90,-180,90,180,urn:ogc:def:crs:EPSG:6.6:4326");
         raw.put("store", "false");
         raw.put("GridBaseCRS", "urn:ogc:def:crs:EPSG:6.6:4326");
 
         GridCoverage[] coverages = executeGetCoverage(raw);
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
             GridCoverage[] coverages = executeGetCoverage(raw);
             fail("When did we learn to encode SuperCoolFormat?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.toString(), e.getCode());
             assertEquals("format", e.getLocator());
         }
     }
 
     public void testWrongGridOrigin() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "GeoTiff");
         raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
 
         GridCoverage[] coverages = executeGetCoverage(raw);
         final GridRange range = coverages[0].getGridGeometry().getGridRange();
         assertEquals(0, range.getLower(0));
         assertEquals(0, range.getLower(1));
 
         raw.put("GridOrigin", "12,13,14");
         try {
             executeGetCoverage(raw);
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
         raw.put("format", "GeoTiff");
         raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
         raw.put("GridBaseCRS", "urn:ogc:def:crs:EPSG:6.6:4326");
 
         GridCoverage[] coverages = executeGetCoverage(raw);
         GridCoverage original = catalog.getCoverageInfo(layerId).getCoverage();
 
         final AffineTransform2D originalTx = (AffineTransform2D) original.getGridGeometry()
                 .getGridToCRS();
         final AffineTransform2D flippedTx = (AffineTransform2D) coverages[0].getGridGeometry()
                 .getGridToCRS();
         assertEquals(originalTx.getScaleX(), flippedTx.getShearY(), EPS);
         assertEquals(originalTx.getScaleY(), flippedTx.getShearX(), EPS);
         assertEquals(originalTx.getShearX(), flippedTx.getScaleY(), EPS);
         assertEquals(originalTx.getShearY(), flippedTx.getScaleX(), EPS);
         assertEquals(originalTx.getTranslateX(), flippedTx.getTranslateY(), EPS);
         assertEquals(originalTx.getTranslateY(), flippedTx.getTranslateX(), EPS);
 
         raw.put("GridOffsets", "12,13,14");
         try {
             executeGetCoverage(raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("GridOffsets", e.getLocator());
         }
     }
 
     /**
      * Tests valid range subset expressions, but with a mix of valid and invalid
      * identifiers
      * 
      * @throws Exception
      */
     public void testRangeSubset() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "GeoTiff");
         raw.put("BoundingBox", "-45,146,-42,147,urn:ogc:def:crs:EPSG:6.6:4326");
 
         // unknown field
         raw.put("rangeSubset", "jimbo:nearest");
         try {
             executeGetCoverage(raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("RangeSubset", e.getLocator());
         }
 
         // unknown axis
         raw.put("rangeSubset", "BlueMarble:nearest[MadAxis[key]]");
         try {
             executeGetCoverage(raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("RangeSubset", e.getLocator());
         }
 
         // unknown key
         raw.put("rangeSubset", "BlueMarble:nearest[Bands[MadKey]]");
         try {
             executeGetCoverage(raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("RangeSubset", e.getLocator());
         }
 
         // ok, finally something we can parse
         raw.put("rangeSubset", "BlueMarble:nearest[Bands[ReD_BaNd]]");
         GridCoverage[] coverages = executeGetCoverage(raw);
         
         assertEquals(1, coverages[0].getNumSampleDimensions());
         assertEquals("Red band", coverages[0].getSampleDimension(0).getDescription().toString());
     }
 
 }
