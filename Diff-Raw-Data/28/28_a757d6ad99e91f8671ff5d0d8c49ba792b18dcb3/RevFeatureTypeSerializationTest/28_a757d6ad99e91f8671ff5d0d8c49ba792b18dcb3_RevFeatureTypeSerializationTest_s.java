 package org.geogit.storage;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.RevObject.TYPE;
 import org.geotools.data.DataUtilities;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.referencing.CRS;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Point;
 
 public abstract class RevFeatureTypeSerializationTest extends Assert {
     private ObjectSerializingFactory factory = getObjectSerializingFactory();
 
     private String namespace = "http://geoserver.org/test";
 
     private String typeName = "TestType";
 
 //    private String typeSpec = "♥";
 //            "str:String," + "bool:Boolean," + "byte:java.lang.Byte,"
 //            + "doub:Double," + "bdec:java.math.BigDecimal," + "flt:Float," + "int:Integer,"
 //            + "bint:java.math.BigInteger," + "pp:Point:srid=4326," + "lng:java.lang.Long,"
 //            + "uuid:java.util.UUID";
 
     private SimpleFeatureType featureType1;
 
     private SimpleFeatureType featureType2_axis_order;
 
     protected abstract ObjectSerializingFactory getObjectSerializingFactory();
 
     @Before
     public void setUp() throws Exception {
         final boolean longitudeFirst = true;
         CoordinateReferenceSystem LAT_LON = CRS.decode("EPSG:4326");
         CoordinateReferenceSystem LON_LAT = CRS.decode("EPSG:4326", longitudeFirst);
         SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
         builder.setName("featuretype1");
         builder.add("str", String.class);
         builder.add("bool", Boolean.class);
         builder.add("doub", Double.class);
         builder.add("bdec", BigDecimal.class);
         builder.add("flt", Float.class);
         builder.add("int", Integer.class);
         builder.add("bint", BigInteger.class);
         builder.add("pp", Point.class, LAT_LON);
         builder.add("long", Long.class);
         featureType1 = builder.buildFeatureType();
         
         builder.setName("featuretype2_axisorder");
         builder.add("str", String.class);
         builder.add("bool", Boolean.class);
         builder.add("doub", Double.class);
         builder.add("bdec", BigDecimal.class);
         builder.add("flt", Float.class);
         builder.add("int", Integer.class);
         builder.add("bint", BigInteger.class);
         builder.add("pp", Point.class, LON_LAT);
         builder.add("long", Long.class);
         featureType2_axis_order = builder.buildFeatureType();
     }
 
     @Test
     public void testSerialization() throws Exception {
         roundtrip(featureType1);
     }
     
     @Test
     public void testAxisOrder() throws Exception {
         roundtrip(featureType2_axis_order);
     }
 
     private void roundtrip(SimpleFeatureType ftype) throws IOException {
         RevFeatureType revFeatureType = RevFeatureType.build(ftype);
         ObjectWriter<RevFeatureType> writer = factory.createObjectWriter(TYPE.FEATURETYPE);
 
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         writer.write(revFeatureType, output);
 
         byte[] data = output.toByteArray();
         assertTrue(data.length > 0);
 
         ObjectReader<RevFeatureType> reader = factory.createObjectReader(TYPE.FEATURETYPE);
         ByteArrayInputStream input = new ByteArrayInputStream(data);
         RevFeatureType rft = reader.read(revFeatureType.getId(), input);
 
         assertNotNull(rft);
         SimpleFeatureType serializedFeatureType = (SimpleFeatureType) rft.type();
         assertEquals(serializedFeatureType.getDescriptors().size(), ftype.getDescriptors()
                 .size());
 
         for (int i = 0; i < ftype.getDescriptors().size(); i++) {
             assertEquals(ftype.getDescriptor(i), serializedFeatureType.getDescriptor(i));
         }
 
         assertEquals(ftype.getGeometryDescriptor(),
                 serializedFeatureType.getGeometryDescriptor());
         assertEquals(ftype.getCoordinateReferenceSystem(),
                 serializedFeatureType.getCoordinateReferenceSystem());
     }
 }
