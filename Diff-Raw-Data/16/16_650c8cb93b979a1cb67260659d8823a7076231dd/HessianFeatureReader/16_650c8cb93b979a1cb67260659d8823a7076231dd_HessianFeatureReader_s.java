 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.0 license, available at the root
  * application directory.
  */
 package org.geogit.storage.hessian;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.geogit.api.ObjectId;
 import org.geogit.storage.ObjectReader;
 import org.geotools.factory.CommonFactoryFinder;
 import org.geotools.factory.Hints;
 import org.geotools.feature.simple.SimpleFeatureImpl;
 import org.opengis.feature.Feature;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.FeatureType;
 import org.opengis.filter.FilterFactory2;
 import org.opengis.filter.identity.ResourceId;
 
 import com.caucho.hessian.io.Hessian2Input;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
 import com.vividsolutions.jts.io.InStream;
 import com.vividsolutions.jts.io.ParseException;
 import com.vividsolutions.jts.io.WKBReader;
 
 /**
  * Reads features from a binary encoded stream. Refer to HessianFeatureWriter for encoding details.
  * 
  * @author mleslie
  */
 class HessianFeatureReader implements ObjectReader<Feature> {
 
     private static final FilterFactory2 FILTER_FAC = CommonFactoryFinder.getFilterFactory2(null);
 
     private FeatureType featureType;
 
     private String featureId;
 
     private static GeometryFactory geometryFactory = new GeometryFactory();
 
     public HessianFeatureReader(final FeatureType featureType, final String featureId,
             final Hints hints) {
         this.featureType = featureType;
         this.featureId = featureId;
         if (hints != null) {
             GeometryFactory gf = (GeometryFactory) hints.get(Hints.GEOMETRY_FACTORY);
             if (gf != null) {
                 geometryFactory = gf;
             }
         }
     }
 
     public Feature read(ObjectId id, InputStream rawData) throws IOException,
             IllegalArgumentException {
         Hessian2Input in = new Hessian2Input(rawData);
         in.startMessage();
         BlobType type = BlobType.fromValue(in.readInt());
         if (type != BlobType.FEATURE)
             throw new IllegalArgumentException("Could not parse blob of type " + type
                     + " as a feature.");
         List<Object> values = new ArrayList<Object>();
         String typeString = in.readString();
         int attrCount = in.readInt();
         for (int i = 0; i < attrCount; i++) {
             Object obj = readValue(in);
             values.add(obj);
         }
         in.completeMessage();
         ResourceId fid = FILTER_FAC.resourceId(featureId, id.toString());
         SimpleFeature feat = new SimpleFeatureImpl(values, (SimpleFeatureType) featureType, fid);
         return feat;
     }
 
     static Object readValue(final Hessian2Input in) throws IOException {
         EntityType type = EntityType.fromValue(in.readInt());
         if (type == null)
             throw new IOException("Illegal format in data stream");
         switch (type) {
         case STRING:
             String str = in.readString();
             return str;
         case BOOLEAN:
             Boolean bool = Boolean.valueOf(in.readBoolean());
             return bool;
         case BYTE:
             byte[] bts = in.readBytes();
             if (bts.length == 1)
                 return Byte.valueOf(bts[0]);
             else
                 return null;
         case DOUBLE:
             Double doub = Double.valueOf(in.readDouble());
             return doub;
         case FLOAT:
             Float flt = Float.intBitsToFloat(in.readInt());
             return flt;
         case INT:
             Integer intg = Integer.valueOf(in.readInt());
             return intg;
         case LONG:
             Long lng = Long.valueOf(in.readLong());
             return lng;
         case BYTE_ARRAY:
             return in.readBytes();
         case BOOLEAN_ARRAY:
             int boolLength = in.readInt();
             boolean[] bools = new boolean[boolLength];
             for (int i = 0; i < boolLength; i++) {
                 bools[i] = in.readBoolean();
             }
             return bools;
         case CHAR_ARRAY:
             String charstring = in.readString();
             return charstring.toCharArray();
         case DOUBLE_ARRAY:
             in.readNull();
             break;
         case FLOAT_ARRAY:
             in.readNull();
             break;
         case INT_ARRAY:
             in.readNull();
             break;
         case LONG_ARRAY:
             in.readNull();
             break;
         case BIGDECIMAL:
             String bdString = in.readString();
             return new BigDecimal(bdString);
         case BIGINT:
             byte[] biBytes = in.readBytes();
             return new BigInteger(biBytes);
         case GEOMETRY:
             String srs = in.readString();
             if (geometryFactory == null)
                 geometryFactory = new GeometryFactory(new PackedCoordinateSequenceFactory());
             WKBReader wkbReader = new WKBReader(geometryFactory);
             Geometry geom;
             try {
                 geom = wkbReader.read(new InStream() {
 
                     public void read(byte[] buf) throws IOException {
                         int length = buf.length;
                         int returned = in.readBytes(buf, 0, length);
                     }
                 });
             } catch (ParseException ex) {
                 throw (IOException) new IOException(ex.getMessage()).initCause(ex);
             }
             return geom;
         case NULL:
             in.readNull();
             return null;
         case UNKNOWN_SERIALISABLE:
             return in.readObject();
         case UNKNOWN:
             String classname = in.readString();
             String value = in.readString();
             return classname + value;
         }
         return null;
     }
 }
