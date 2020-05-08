 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.0 license, available at the root
  * application directory.
  */
 package org.geogit.storage;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.MultiLineString;
 import com.vividsolutions.jts.geom.MultiPoint;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 
 /**
  * This enum describes the data type of each encoded feature attribute.
  * 
  * The integer value of each data type should never be changed, for backwards compatibility
  * purposes. If the method of encoding of an attribute type is changed, a new type should be
  * created, with a new value, and the writers updated to use it. The readers should continue to
  * support both the old and new versions.
  * 
  * @author mleslie
  */
 @SuppressWarnings("rawtypes")
 public enum GtEntityType implements Serializable {
     NULL(-1, null), //
     STRING(0, String.class), //
     BOOLEAN(1, Boolean.class), //
     BYTE(2, Byte.class), //
     DOUBLE(3, Double.class), //
     BIGDECIMAL(4, BigDecimal.class), //
     FLOAT(5, Float.class), //
     INT(6, Integer.class), //
     BIGINT(7, BigInteger.class), //
     LONG(8, Long.class), //
     BOOLEAN_ARRAY(9, boolean[].class), //
     BYTE_ARRAY(10, byte[].class), //
     CHAR_ARRAY(11, char[].class), //
     DOUBLE_ARRAY(12, double[].class), //
     FLOAT_ARRAY(13, float[].class), //
     INT_ARRAY(14, int[].class), //
     LONG_ARRAY(15, long[].class), //
     POINT(16, Point.class), //
     MULTIPOINT(17, MultiPoint.class), //
     LINESTRING(18, LineString.class), //
     MULTILINESTRING(19, MultiLineString.class), //
     POLYGON(20, Polygon.class), //
     MULTIPOLYGON(21, MultiPolygon.class), //
     GEOMETRYCOLLECTION(22, GeometryCollection.class), //
     GEOMETRY(23, Geometry.class), //
     UNKNOWN_SERIALISABLE(24, Serializable.class), //
     UNKNOWN(25, null), //
     UUID(26, java.util.UUID.class), //
     DATE_UTIL(27, java.util.Date.class), //
     DATE_SQL(28, java.sql.Date.class), //
     TIME_SQL(29, java.sql.Time.class), //
     TIMESTAMP_SQL(30, java.sql.Timestamp.class)//
     ;
 
     private int value;
 
     private Class binding;
 
     private GtEntityType(int value, Class binding) {
         this.value = value;
         this.binding = binding;
     }
 
     /**
      * @return the {@code int} value of the enumeration
      */
     public int getValue() {
         return this.value;
     }
 
    public boolean isGeometry() {
        return binding != null && Geometry.class.isAssignableFrom(binding);
    }

     /**
      * @return the actual class of the enumerated type
      */
     public Class getBinding() {
         return this.binding;
     }
 
     /**
      * Determines the EntityType given a particular class.
      * 
      * @param cls the class to look up
      * @return the EntityType for the given class, UNKNOWN if a match couldn't be found.
      */
     @SuppressWarnings("unchecked")
     public static GtEntityType fromBinding(Class cls) {
         if (cls == null)
             return NULL;
         /*
          * We're handling equality first, as some entity types are top-level catch-alls, and we
          * can't rely on processing order to ensure the more specific cases are handled first.
          */
         for (GtEntityType type : GtEntityType.values()) {
             if (type.binding != null && type.binding.equals(cls))
                 return type;
         }
         for (GtEntityType type : GtEntityType.values()) {
             if (type.binding != null && type.binding.isAssignableFrom(cls))
                 return type;
         }
         return UNKNOWN;
     }
 
     /**
      * Determines the EntityType given its integer value.
      * 
      * @param value The value of the desired EntityType, as read from the blob
      * @return The correct EntityType for the value, or null if none is found.
      */
     public static GtEntityType fromValue(int value) {
         for (GtEntityType type : GtEntityType.values()) {
             if (type.value == value) {
                 return type;
             }
         }
         return null;
     }
 }
