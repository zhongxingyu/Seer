 package org.geoserver.wcs.kvp;
 
 import static org.vfny.geoserver.wcs.WcsException.WcsExceptionCode.InvalidParameterValue;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 import net.opengis.wcs.v1_1_1.AxisSubsetType;
 import net.opengis.wcs.v1_1_1.FieldSubsetType;
 import net.opengis.wcs.v1_1_1.RangeSubsetType;
 
 import org.vfny.geoserver.wcs.WcsException;
 
 
 public class RangeSubsetKvpParserTest extends TestCase {
     RangeSubsetKvpParser parser = new RangeSubsetKvpParser();
     
     public void testSimpleFields() throws Exception {
         RangeSubsetType rs = (RangeSubsetType) parser.parse("radiance;temperature");
         assertNotNull(rs);
         assertEquals(2, rs.getFieldSubset().size());
         FieldSubsetType field = (FieldSubsetType) rs.getFieldSubset().get(0);
         assertEquals("radiance",field.getIdentifier().getValue());
         assertEquals(null,field.getInterpolationType());
         field = (FieldSubsetType) rs.getFieldSubset().get(1);
         assertEquals("temperature", field.getIdentifier().getValue());
         assertEquals(null, field.getInterpolationType());
     }
     
     public void testInvalidInterpolation() throws Exception {
         try {
             parser.parse("radiance:mindReadingWarper");
             fail("We do not support _that_ interpolation!");
         } catch(WcsException e) {
             assertEquals(InvalidParameterValue.toString(), e.getCode());
             assertEquals("RangeSubset", e.getLocator());
         }
     }
     
     public void testInterpolation() throws Exception {
         RangeSubsetType rs = (RangeSubsetType) parser.parse("radiance:linear;temperature:nearest");
         assertNotNull(rs);
         assertEquals(2, rs.getFieldSubset().size());
         FieldSubsetType field = (FieldSubsetType) rs.getFieldSubset().get(0);
         assertEquals("radiance",field.getIdentifier().getValue());
         assertEquals("linear",field.getInterpolationType());
         field = (FieldSubsetType) rs.getFieldSubset().get(1);
         assertEquals("temperature", field.getIdentifier().getValue());
         assertEquals("nearest", field.getInterpolationType());
     }
     
     public void testAxisKeys() throws Exception {
        RangeSubsetType rs = (RangeSubsetType) parser.parse("radiance[bands[Red,Blue]]");
         assertNotNull(rs);
         assertEquals(1, rs.getFieldSubset().size());
         FieldSubsetType field = (FieldSubsetType) rs.getFieldSubset().get(0);
         assertEquals("radiance", field.getIdentifier().getValue());
         assertEquals(1, field.getAxisSubset().size());
         AxisSubsetType axis = (AxisSubsetType) field.getAxisSubset().get(0);
         assertEquals("bands", axis.getIdentifier());
         List keys = axis.getKey();
        assertEquals(2, keys.size());
         assertEquals("Red", keys.get(0));
        assertEquals("Blue", keys.get(1));
     }
 }
