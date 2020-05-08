 package org.geoserver.wcs.kvp;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.opengis.wcs.v1_1_1.AxisSubsetType;
 import net.opengis.wcs.v1_1_1.FieldSubsetType;
 import net.opengis.wcs.v1_1_1.GetCoverageType;
 import net.opengis.wcs.v1_1_1.RangeSubsetType;
 
 import org.geoserver.wcs.test.WCSTestSupport;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.wcs.WcsException;
 import static org.vfny.geoserver.wcs.WcsException.WcsExceptionCode.*;
 
 public class GetCoverageReaderTest extends WCSTestSupport {
 
     GetCoverageRequestReader reader;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         Data catalog = (Data) applicationContext.getBean("catalog");
         reader = new GetCoverageRequestReader(catalog);
     }
     
     protected String getDefaultLogConfiguration() {
         return "/DEFAULT_LOGGING.properties";
     }
     
 
     public void testMissingParams() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
 
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("Hey, format is missing, this should have failed");
         } catch (WcsException e) {
             assertEquals("MissingParameterValue", e.getCode());
         }
 
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("Hey, format is missing, this should have failed");
         } catch (WcsException e) {
             assertEquals("MissingParameterValue", e.getCode());
         }
 
         raw.put("format", "GeoTiff");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("Hey, boundingBox is missing, this should have failed");
         } catch (WcsException e) {
             assertEquals("MissingParameterValue", e.getCode());
         }
         
         raw.put("BoundingBox", "-45,146,-42,147");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
         } catch (WcsException e) {
             fail("This time all mandatory params where provided?");
             assertEquals("MissingParameterValue", e.getCode());
         }
 
     }
 
     public void testWrongFormatParams() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "SuperCoolFormat");
         raw.put("BoundingBox", "-45,146,-42,147");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("When did we learn to encode SuperCoolFormat?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.toString(), e.getCode());
             assertEquals("format", e.getLocator());
         }
     }
 
     public void testUnknownCoverageParams() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = "fairyTales:rumpelstilskin";
         raw.put("identifier", layerId);
         raw.put("format", "SuperCoolFormat");
         raw.put("BoundingBox", "-45,146,-42,147");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("That coverage is not registered???");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.toString(), e.getCode());
             assertEquals("identifier", e.getLocator());
         }
     }
 
     public void testBasic() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "GeoTiff");
         raw.put("BoundingBox", "-45,146,-42,147");
         raw.put("store", "false");
         raw.put("GridBaseCRS", "urn:ogc:def:crs:EPSG:6.6:4326");
 
         GetCoverageType getCoverage = (GetCoverageType) reader.read(reader.createRequest(),
                 parseKvp(raw), raw);
         assertEquals(layerId, getCoverage.getIdentifier().getValue());
         assertEquals("GEOTIFF", getCoverage.getOutput().getFormat());
         assertFalse(getCoverage.getOutput().isStore());
         assertEquals("urn:ogc:def:crs:EPSG:6.6:4326", getCoverage.getOutput().getGridCRS()
                 .getGridBaseCRS());
     }
 
     public void testStoreUnsupported() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "GeoTiff");
         raw.put("BoundingBox", "-45,146,-42,147");
         raw.put("store", "true");
 
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals("store", e.getLocator());
             assertEquals("InvalidParameterValue", e.getCode());
         }
     }
 
     public void testUnsupportedCRS() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "GeoTiff");
         raw.put("GridBaseCRS", "urn:ogc:def:crs:EPSG:6.6:-1000");
 
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals("GridBaseCRS", e.getLocator());
             assertEquals("InvalidParameterValue", e.getCode());
         }
     }
 
     public void testGridTypes() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "GeoTiff");
         raw.put("BoundingBox", "-45,146,-42,147");
 
         raw.put("gridType", GridType.GT2dGridIn2dCrs.getXmlConstant());
         GetCoverageType getCoverage = (GetCoverageType) reader.read(reader.createRequest(),
                 parseKvp(raw), raw);
         assertEquals(GridType.GT2dGridIn2dCrs.getXmlConstant(), getCoverage.getOutput()
                 .getGridCRS().getGridType());
 
         raw.put("gridType", GridType.GT2dSimpleGrid.getXmlConstant());
         getCoverage = (GetCoverageType) reader.read(reader.createRequest(), parseKvp(raw), raw);
         assertEquals(GridType.GT2dSimpleGrid.getXmlConstant(), getCoverage.getOutput().getGridCRS()
                 .getGridType());
 
         raw.put("gridType", GridType.GT2dGridIn3dCrs.getXmlConstant());
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("GridType", e.getLocator());
         }
 
         raw.put("gridType", "Hoolabaloola");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("GridType", e.getLocator());
         }
     }
 
     public void testGridCS() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "GeoTiff");
         raw.put("BoundingBox", "-45,146,-42,147");
 
         raw.put("GridCS", GridCS.GCSGrid2dSquare.getXmlConstant());
         GetCoverageType getCoverage = (GetCoverageType) reader.read(reader.createRequest(),
                 parseKvp(raw), raw);
         assertEquals(GridCS.GCSGrid2dSquare.getXmlConstant(), getCoverage.getOutput().getGridCRS()
                 .getGridCS());
 
         raw.put("GridCS", "Hoolabaloola");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("GridCS", e.getLocator());
         }
     }
 
     public void testGridOrigin() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "GeoTiff");
         raw.put("BoundingBox", "-45,146,-42,147");
 
         GetCoverageType getCoverage = (GetCoverageType) reader.read(reader.createRequest(),
                 parseKvp(raw), raw);
         double[] origin = (double[]) getCoverage.getOutput().getGridCRS().getGridOrigin();
         assertEquals(2, origin.length);
         assertEquals(0.0, origin[0]);
         assertEquals(0.0, origin[1]);
 
         raw.put("GridOrigin", "10.5,-30.2");
         getCoverage = (GetCoverageType) reader.read(reader.createRequest(), parseKvp(raw), raw);
         origin = (double[]) getCoverage.getOutput().getGridCRS().getGridOrigin();
         assertEquals(2, origin.length);
         assertEquals(10.5, origin[0]);
         assertEquals(-30.2, origin[1]);
 
         raw.put("GridOrigin", "12");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("GridOrigin", e.getLocator());
         }
 
         raw.put("GridOrigin", "12,a");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("GridOrigin", e.getLocator());
         }
 
         raw.put("GridOrigin", "12,13,14");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("GridOrigin", e.getLocator());
         }
     }
 
     public void testGridOffsets() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "GeoTiff");
         raw.put("BoundingBox", "-45,146,-42,147");
 
         GetCoverageType getCoverage = (GetCoverageType) reader.read(reader.createRequest(),
                 parseKvp(raw), raw);
         double[] offsets = (double[]) getCoverage.getOutput().getGridCRS().getGridOffsets();
         assertEquals(4, offsets.length);
         assertEquals(1.0, offsets[0]);
         assertEquals(0.0, offsets[1]);
         assertEquals(0.0, offsets[2]);
         assertEquals(1.0, offsets[3]);
 
         raw.put("GridOffsets", "10.5,-30.2");
         getCoverage = (GetCoverageType) reader.read(reader.createRequest(), parseKvp(raw), raw);
         offsets = (double[]) getCoverage.getOutput().getGridCRS().getGridOffsets();
         assertEquals(2, offsets.length);
         assertEquals(10.5, offsets[0]);
         assertEquals(-30.2, offsets[1]);
 
         raw.put("GridOffsets", "12");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("GridOffsets", e.getLocator());
         }
 
         raw.put("GridOffsets", "12,a");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("GridOffsets", e.getLocator());
         }
 
         raw.put("GridOffsets", "12,13,14");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("GridOffsets", e.getLocator());
         }
     }
     
     /**
      * Tests valid range subset expressions, but with a mix of valid and invalid identifiers
      * @throws Exception
      */
     public void testRangeSubset() throws Exception {
         Map<String, Object> raw = new HashMap<String, Object>();
         final String layerId = layerId(WCSTestSupport.TASMANIA_BM);
         raw.put("identifier", layerId);
         raw.put("format", "GeoTiff");
         raw.put("BoundingBox", "-45,146,-42,147");
         
         // unknown field
         raw.put("rangeSubset", "jimbo:nearest");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("RangeSubset", e.getLocator());
         }
         
         // unknown axis
         raw.put("rangeSubset", "BlueMarble:nearest[MadAxis[key]]");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("RangeSubset", e.getLocator());
         }
         
         // unknown key
         raw.put("rangeSubset", "BlueMarble:nearest[Bands[MadKey]]");
         try {
             reader.read(reader.createRequest(), parseKvp(raw), raw);
             fail("We should have had a WcsException here?");
         } catch (WcsException e) {
             assertEquals(InvalidParameterValue.name(), e.getCode());
             assertEquals("RangeSubset", e.getLocator());
         }
         
         // ok, finally something we can parse
         raw.put("rangeSubset", "BlueMarble:nearest[Bands[ReD_BaNd]]");
         GetCoverageType getCoverage = (GetCoverageType) reader.read(reader.createRequest(), parseKvp(raw), raw);
         RangeSubsetType rs = getCoverage.getRangeSubset();
         assertNotNull(rs);
         assertEquals(1, rs.getFieldSubset().size());
         FieldSubsetType field = (FieldSubsetType) rs.getFieldSubset().get(0);
         assertEquals("BlueMarble", field.getIdentifier().getValue());
         assertEquals(1, field.getAxisSubset().size());
         AxisSubsetType axis = (AxisSubsetType) field.getAxisSubset().get(0);
         assertEquals("Bands", axis.getIdentifier());
         List keys = axis.getKey();
         assertEquals(1, keys.size());
         // make sure the name has been fixed during the parsing too
         assertEquals("Red_band", keys.get(0));
     }
 
 }
