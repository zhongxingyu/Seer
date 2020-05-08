 package de.oliverpabst.gml;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import javax.xml.namespace.QName;
 import org.eclipse.xsd.XSDSchema;
 
 import org.geotools.feature.DefaultFeatureCollection;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.gml3.v3_2.GMLConfiguration;
 import org.geotools.wfs.v1_0.WFS;
 import org.geotools.xml.Configuration;
 import org.geotools.xml.Encoder;
 import org.geotools.xml.Schemas;
 import org.w3c.dom.Document;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.io.ParseException;
 import com.vividsolutions.jts.io.WKTReader;
 
 import net.opengis.wfs.FeatureCollectionType;
 import net.opengis.wfs.WfsFactory;
 
 public class GML3EncodingTest {
 	static GeometryFactory gf = new GeometryFactory();
 	public static String NAMESPACE = "http://www.geotools.org/test";
 	public static QName feature = new QName(NAMESPACE, "TestFeature");
 		
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		buildFeatureCollection();
 		//encodeGML3();
 		//encodeGML3FromJTSPoint();
 		//encodeGML3FromJTSMultiLineString();
 		encodeGML3Multiple();
 
 		//encodeGML3FromJTSPoint();
 		//encodeGML3FromJTSMultiLineString();
 		//encodeGML3FromJTSPolygon();
 		//encodeWFS10();
 	}
 	
 	public static void encodeGML3() {
 		FeatureCollectionType fc = buildFeatureCollection();
 		Configuration config = new GMLConfiguration();
 		XSDSchema schema = null;
 		try {
 			schema = Schemas.parse("test.xsd");
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		if(schema == null) {
 			System.out.println("parsing schema went wrong!");
 		}
 		Encoder encoder = new Encoder(config, schema);
 		ByteArrayOutputStream xml = new ByteArrayOutputStream();
 		// TODO: Define custom feature collection for given type
 		
 		try {
 			encoder.encode(fc, feature, xml);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(xml.toString());
 		try {
 			xml.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static void encodeWFS10() {
 		FeatureCollectionType fc = buildFeatureCollection();
 		Encoder e = new Encoder( new org.geotools.wfs.v1_0.WFSConfiguration() );
         e.getNamespaces().declarePrefix( "geotools", "http://geotools.org/test");
         e.setIndenting(true);
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         try {
 			e.encode(fc, WFS.FeatureCollection, out);
 		} catch (IOException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
         System.out.println(out.toString());
         try {
 			out.close();
 		} catch (IOException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
         Document d = null;
        /* try {
 			d = e.encodeAsDOM( fc, WFS.FeatureCollection );
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (SAXException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (TransformerException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}*/
         //NodeList nl = d.getElementsByTagName( "gml:Point" );
 	}
 	
 	public static FeatureCollectionType buildFeatureCollection() {
 		FeatureCollectionType fc = WfsFactory.eINSTANCE.createFeatureCollectionType();
         FeatureCollection features = new DefaultFeatureCollection(null,null);
         
         SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
         tb.setName( "feature" );
         tb.setNamespaceURI( "http://geotools.org");
         tb.add( "geometry", Point.class );
         tb.add( "integer", Integer.class);
         
         SimpleFeatureBuilder b = new SimpleFeatureBuilder( tb.buildFeatureType() );
         b.add( new GeometryFactory().createPoint( new Coordinate( 0, 0 ) ) );
         b.add( 0 );
         features.add( b.buildFeature( "zero" ) );
         
         b.add( new GeometryFactory().createPoint( new Coordinate( 1, 1 ) ) );
         b.add( 1 );
         features.add( b.buildFeature( "one" ) );
         
         fc.getFeature().add( features );
         
         return fc;
 	}
 	
 	public static void encodeGML3FromJTSPoint() {
 		WKTReader wkt = new WKTReader();
 		Geometry geom = null;
 		XSDSchema schema = null;
 		try {
 			schema = Schemas.parse("point.xsd");
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		try {
 			geom = wkt.read("POINT (2 5)");
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Configuration config = new GMLConfiguration();
 		Encoder encoder = new Encoder(config, schema);
 		encoder.setIndenting(true);
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		QName name = new QName("http://www.example.org/point", "Point");
 		try {
 			encoder.encode(geom, name, out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(out.toString());
 	}
 	
 	public static void encodeGML3FromJTSMultiLineString() {
 		WKTReader wkt = new WKTReader();
 		Geometry geom = null;
 		XSDSchema schema = null;
 		try {
 			schema = Schemas.parse("multilinestring.xsd");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		try {
 			geom = wkt.read("MULTILINESTRING ((3470947.89 5526163.53, 3470970.01 5526161.37))");
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Configuration config = new GMLConfiguration();
 		Encoder encoder = new Encoder(config, schema);
 		encoder.setIndenting(true);
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		QName name = new QName("http://www.example.org/multilinestring", geom.getGeometryType());
 		try {
 			encoder.encode(geom, name, out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(out.toString());
 	}
 	
 	public static void encodeGML3Multiple() {
 		WKTReader wkt = new WKTReader();
 		Geometry point = null;
 		Geometry line = null;
 		Geometry polygon = null;
 		XSDSchema schema = null;
 		
 		try {
 			point = wkt.read("POINT (2 5)");
 			line = wkt.read("MULTILINESTRING ((3470947.89 5526163.53, 3470970.01 5526161.37))");
 			polygon = wkt.read("POLYGON ((3471872.26 5530082.57, 3471896.49 5530265.69, 3471896.49 5530265.69, 3471870.77 5530243.09, 3471870.77 5530243.09, 3471846.22 5530224.71, 3471846.22 5530224.71, 3471831.58 5530206.06, 3471831.58 5530206.06, 3471826.59 5530185.62, 3471826.59 5530185.62, 3471828.33 5530167.71, 3471828.33 5530167.71, 3471837.43 5530146.74, 3471837.43 5530146.74, 3471857.43 5530113.3, 3471857.43 5530113.3, 3471872.26 5530082.57))");
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			schema = Schemas.parse("single.xsd");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Configuration config = new GMLConfiguration();
 		Encoder encoder = new Encoder(config, schema);
 		encoder.setIndenting(true);
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		QName name = new QName("http://www.example.org/single", point.getGeometryType());
 		try {
 			encoder.encode(point, name, out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(out.toString());
 		encoder = new Encoder(config, schema);
 		encoder.setIndenting(true);
 		out = null;
 		out = new ByteArrayOutputStream();
 		QName wurst = new QName("http://www.example.org/single", line.getGeometryType());
 		try {
 			encoder.encode(line, wurst, out);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
		System.out.println(wurst.toString());
 		
 		encoder = new Encoder(config, schema);
 		encoder.setIndenting(true);
 		out = null;
 		out = new ByteArrayOutputStream();
 		QName poly = new QName("http://www.example.org/single", polygon.getGeometryType());
 		try {
 			encoder.encode(polygon, poly, out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(out.toString());
 	}
 		
 	public static void encodeGML3FromJTSPolygon() {
 		WKTReader wkt = new WKTReader();
 		Geometry polygon = null;
 		XSDSchema schema = null;
 		try {
 			schema = Schemas.parse("polygon.xsd");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			polygon = wkt.read("POLYGON ((3471872.26 5530082.57, 3471896.49 5530265.69, 3471896.49 5530265.69, 3471870.77 5530243.09, 3471870.77 5530243.09, 3471846.22 5530224.71, 3471846.22 5530224.71, 3471831.58 5530206.06, 3471831.58 5530206.06, 3471826.59 5530185.62, 3471826.59 5530185.62, 3471828.33 5530167.71, 3471828.33 5530167.71, 3471837.43 5530146.74, 3471837.43 5530146.74, 3471857.43 5530113.3, 3471857.43 5530113.3, 3471872.26 5530082.57))");
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Configuration config = new GMLConfiguration();
 		Encoder encoder = new Encoder(config, schema);
 		encoder.setIndenting(true);
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		QName name = new QName("http://www.example.org/polygon", "Polygon");
 		try {
 			encoder.encode(polygon, name, out);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(out.toString());
 	}
 }
