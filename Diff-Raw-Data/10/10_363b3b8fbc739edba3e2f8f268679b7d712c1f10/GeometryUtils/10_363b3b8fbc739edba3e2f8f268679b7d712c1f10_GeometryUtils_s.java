 /*
  * Copyright (c) 2010
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.gis.spatial;
 
 import java.util.HashMap;
 
 import org.neo4j.graphdb.Node;
 
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.MultiLineString;
 import com.vividsolutions.jts.geom.MultiPoint;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.io.ParseException;
 import com.vividsolutions.jts.io.WKBReader;
 import com.vividsolutions.jts.io.WKBWriter;
 
 /**
  * @author Davide Savazzi
  */
 public class GeometryUtils implements Constants {
 
     // Public methods
 
     /**
      * Find and extract the bounding box from the node properties.
      */
     public static Envelope getEnvelope(Node node) {
         double[] bbox = (double[])node.getProperty(PROP_BBOX);
 
         // Envelope parameters: xmin, xmax, ymin, ymax)
         return new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
     }
 
     /**
      * Create a bounding box encompassing the two bounding boxes passed in.
      * 
      * @param e
      * @param e1
      * @return
      */
     public static Envelope getEnvelope(Envelope e, Envelope e1) {
         Envelope result = new Envelope(e);
         result.expandToInclude(e1);
         return result;
     }
 
     /**
      * Encode the geometry information into the Node. Currently this stores the geometry in WKB as a
      * byte[] property of the Node. Future versions will allow alternative storage, like WKT,
      * sub-graphs and vertex point arrays with additional metadata properties. TODO: Support more
      * encoding options TODO: Optimize (reduce object creation)
      * 
      * @param geom
      * @param geomNode
      */
     public static void encode(Geometry geom, Node geomNode) {
         geomNode.setProperty(PROP_TYPE, encodeGeometryType(geom.getGeometryType()));
 
         Envelope mbb = geom.getEnvelopeInternal();
         geomNode.setProperty(PROP_BBOX, new double[] {mbb.getMinX(), mbb.getMinY(), mbb.getMaxX(), mbb.getMaxY()});
 
         WKBWriter writer = new WKBWriter();
         geomNode.setProperty(PROP_WKB, writer.write(geom));
     }
 
     /**
      * Decode the geometry information in the Node, and create a JTS Geometry object. Currently this
      * looks for a WKB byte[]. Future versions will also look for WKT, sub-graphs or other metadata
      * options with different performance characteristics. TODO: Support more decoding options
      * 
      * @param geomNode
      * @param geomFactory
      * @return
      */
     public static Geometry decode(Node geomNode, GeometryFactory geomFactory) {
         try {
             WKBReader reader = new WKBReader(geomFactory);
             return reader.read((byte[])geomNode.getProperty(PROP_WKB));
         } catch (ParseException e) {
             throw new SpatialDatabaseException(e.getMessage(), e);
         }
     }
 
     // Private methods
     private static HashMap<String, Integer> geometryTypesMap = new HashMap<String, Integer>();
     static {
         geometryTypesMap.put("Point", GTYPE_POINT);
         geometryTypesMap.put("MultiPoint", GTYPE_MULTIPOINT);
         geometryTypesMap.put("LineString", GTYPE_LINESTRING);
         geometryTypesMap.put("MultiLineString", GTYPE_MULTILINESTRING);
         geometryTypesMap.put("Polygon", GTYPE_POLYGON);
         geometryTypesMap.put("MultiPolygon", GTYPE_MULTIPOLYGON);
     }
 
	public static Integer convertJtsClassToGeometryType(Class jtsClass) {
 		if (jtsClass.equals(Point.class)) {
 			return GTYPE_POINT;
 		} else if (jtsClass.equals(LineString.class)) {
 			return GTYPE_LINESTRING;
 		} else if (jtsClass.equals(Polygon.class)) {
 			return GTYPE_POLYGON;
 		} else if (jtsClass.equals(MultiPoint.class)) {
 			return GTYPE_MULTIPOINT;
 		} else if (jtsClass.equals(MultiLineString.class)) {
 			return GTYPE_MULTILINESTRING;
 		} else if (jtsClass.equals(MultiPolygon.class)) {
 			return GTYPE_MULTIPOLYGON;
 		} else {
 			return null;
 		}
 	}
 	
	public static Class convertGeometryTypeToJtsClass(Integer geometryType) {
 		switch (geometryType) {
 			case GTYPE_POINT: return Point.class;
 			case GTYPE_LINESTRING: return LineString.class; 
 			case GTYPE_POLYGON: return Polygon.class;
 			case GTYPE_MULTIPOINT: return MultiPoint.class;
 			case GTYPE_MULTILINESTRING: return MultiLineString.class;
 			case GTYPE_MULTIPOLYGON: return MultiPolygon.class;
 			default: return null;
 		}
 	}
 	
 	
 	// Private methods
 	
 	private static Integer encodeGeometryType(String jtsGeometryType) {
	        // TODO: Consider alternatives for specifying type, like relationship to type category
	        // objects (or similar indexing structure)
 		if ("Point".equals(jtsGeometryType)) {
 			return GTYPE_POINT;
 		} else if ("MultiPoint".equals(jtsGeometryType)) {
 			return GTYPE_MULTIPOINT;
 		} else if ("LineString".equals(jtsGeometryType)) {
 			return GTYPE_LINESTRING;
 		} else if ("MultiLineString".equals(jtsGeometryType)) {
 			return GTYPE_MULTILINESTRING;
 		} else if ("Polygon".equals(jtsGeometryType)) {
 			return GTYPE_POLYGON;
 		} else if ("MultiPolygon".equals(jtsGeometryType)) {
 			return GTYPE_MULTIPOLYGON;
 		} else {
 			throw new IllegalArgumentException("unknown type:" + jtsGeometryType);
 		}
 	}
 }
