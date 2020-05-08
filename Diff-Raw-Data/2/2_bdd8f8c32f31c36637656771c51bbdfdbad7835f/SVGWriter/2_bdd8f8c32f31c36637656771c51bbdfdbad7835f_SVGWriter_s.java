 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.svg;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.NoSuchElementException;
 import java.util.logging.Logger;
 
 import org.geotools.data.DataSourceException;
 import org.geotools.data.FeatureReader;
 import org.geotools.feature.Feature;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.IllegalAttributeException;
 import org.vfny.geoserver.wms.WMSMapContext;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.LinearRing;
 import com.vividsolutions.jts.geom.MultiLineString;
 import com.vividsolutions.jts.geom.MultiPoint;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author Gabriel Rold?n
  * @version $Id: SVGWriter.java,v 1.5 2004/09/05 17:16:53 cholmesny Exp $
  */
 public class SVGWriter extends OutputStreamWriter {
     private static final Logger LOGGER = Logger.getLogger(SVGWriter.class.getPackage()
                                                                          .getName());
 
     /**
      * a number formatter setted up to write SVG legible numbers ('.' as
      * decimal separator, no group separator
      */
     private static DecimalFormat formatter;
 
     /** 
      * map of geometry class to writer
      */
     private HashMap writers;
     
     static {
         Locale locale = new Locale("en", "US");
         DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols(locale);
         decimalSymbols.setDecimalSeparator('.');
         formatter = new DecimalFormat();
         formatter.setDecimalFormatSymbols(decimalSymbols);
 
         //do not group
         formatter.setGroupingSize(0);
 
         //do not show decimal separator if it is not needed
         formatter.setDecimalSeparatorAlwaysShown(false);
         formatter.setDecimalFormatSymbols(null);
 
         //set default number of fraction digits
         formatter.setMaximumFractionDigits(5);
 
         //minimun fraction digits to 0 so they get not rendered if not needed
         formatter.setMinimumFractionDigits(0);
     }
 
     /** DOCUMENT ME! */
     private double minY;
 
     /** DOCUMENT ME! */
     private double maxY;
 
     /** DOCUMENT ME! */
     private int coordsSkipCount;
 
     /** DOCUMENT ME! */
     private int coordsWriteCount;
 
     /** DOCUMENT ME! */
     private SVGFeatureWriterHandler writerHandler = new SVGFeatureWriterHandler();
 
     /** DOCUMENT ME! */
     private SVGFeatureWriter featureWriter = null;
 
     /** DOCUMENT ME! */
     private double minCoordDistance;
 
     /** DOCUMENT ME! */
     private String attributeStyle;
 
     /** DOCUMENT ME! */
     private boolean pointsAsCircles;
 
     /** DOCUMENT ME! */
     private WMSMapContext mapContext;
 
     /**
      * Creates a new SVGWriter object.
      *
      * @param out DOCUMENT ME!
      * @param config DOCUMENT ME!
      */
     public SVGWriter(OutputStream out, WMSMapContext mapContext) {
         super(out);
         this.mapContext = mapContext;
 
         Envelope space = mapContext.getAreaOfInterest();
         this.minY = space.getMinY();
         this.maxY = space.getMaxY();
         
         initWriters();
     }
 
     private void initWriters() {
     	writers = new HashMap();
     	writers.put(Point.class,new PointWriter());
     	writers.put(LineString.class, new LineStringWriter());
     	writers.put(LinearRing.class, new LineStringWriter());
     	writers.put(Polygon.class, new PolygonWriter());
     	writers.put(MultiPoint.class, new MultiPointWriter());
    	writers.put(MultiLineString.class, new MultiPointWriter());
     	writers.put(MultiPolygon.class, new MultiPolygonWriter());
     }
     
     /**
      * DOCUMENT ME!
      *
      * @param attributeName DOCUMENT ME!
      */
     public void setAttributeStyle(String attributeName) {
         this.attributeStyle = attributeName;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param asCircles DOCUMENT ME!
      */
     public void setPointsAsCircles(boolean asCircles) {
         this.pointsAsCircles = asCircles;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param gtype DOCUMENT ME!
      *
      * @throws IllegalArgumentException DOCUMENT ME!
      */
     public void setGeometryType(Class gtype) {
     	
     	featureWriter = (SVGFeatureWriter) writers.get(gtype);
     	if (featureWriter == null) {
     		//check for abstract Geometry type
     		if (gtype == Geometry.class) {
     			featureWriter = new GeometryWriter(); 
     		}
     		else {
     			throw new IllegalArgumentException(
 	                "No SVG Feature writer defined for " + gtype
                 );
     		}
     	}
 //        if (gtype == Point.class) {
 //            featureWriter = new PointWriter();
 //        } else if (gtype == MultiPoint.class) {
 //            featureWriter = new MultiPointWriter();
 //        } else if (gtype == LineString.class) {
 //            featureWriter = new LineStringWriter();
 //        } else if (gtype == MultiLineString.class) {
 //            featureWriter = new MultiLineStringWriter();
 //        } else if (gtype == Polygon.class) {
 //            featureWriter = new PolygonWriter();
 //        } else if (gtype == MultiPolygon.class) {
 //            featureWriter = new MultiPolygonWriter();
 //        } else {
 //            throw new IllegalArgumentException(
 //                "No SVG Feature writer defined for " + gtype);
 //        }
 
         /*
            if (config.isCollectGeometries()) {
                this.writerHandler = new CollectSVGHandler(featureWriter);
            } else {
                this.writerHandler = new SVGFeatureWriterHandler();
                this.writerHandler = new FIDSVGHandler(this.writerHandler);
                this.writerHandler = new BoundsSVGHandler(this.writerHandler);
                this.writerHandler = new AttributesSVGHandler(this.writerHandler);
            }
          */
     }
 
     public void setWriterHandler(SVGFeatureWriterHandler handler) {
         this.writerHandler = handler;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param minCoordDistance DOCUMENT ME!
      */
     public void setMinCoordDistance(double minCoordDistance) {
         this.minCoordDistance = minCoordDistance;
     }
 
     /**
      * if a reference space has been set, returns a translated Y coordinate
      * wich is inverted based on the height of such a reference space,
      * otherwise just returns <code>y</code>
      *
      * @param y DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public double getY(double y) {
         return (maxY - y) + minY;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param x DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public double getX(double x) {
         return x;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param numDigits DOCUMENT ME!
      */
     public void setMaximunFractionDigits(int numDigits) {
         formatter.setMaximumFractionDigits(numDigits);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public int getMaximunFractionDigits() {
         return formatter.getMaximumFractionDigits();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param numDigits DOCUMENT ME!
      */
     public void setMinimunFractionDigits(int numDigits) {
         formatter.setMinimumFractionDigits(numDigits);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public int getMinimunFractionDigits() {
         return formatter.getMinimumFractionDigits();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param d DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public void write(double d) throws IOException {
         write(formatter.format(d));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param c DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public void write(char c) throws IOException {
         super.write(c);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public void newline() throws IOException {
         super.write('\n');
     }
 
     public void writeFeatures(FeatureReader reader, String style)
         throws IOException, AbortedException {
         Feature ft;
 
         try {
             FeatureType featureType = reader.getFeatureType();
             Class gtype = featureType.getDefaultGeometry().getType();
 
             boolean doCollect = false;
             /*
             boolean doCollect = config.isCollectGeometries()
                 && (gtype != Point.class) && (gtype != MultiPoint.class);
             */
             setGeometryType(gtype);
 
             setPointsAsCircles("#circle".equals(style));
 
             if ((style != null) && !"#circle".equals(style)
                     && style.startsWith("#")) {
                 style = style.substring(1);
             } else {
                 style = null;
             }
 
             setAttributeStyle(style);
 
             setUpWriterHandler(featureType, doCollect);
 
             if (doCollect) {
                 write("<path ");
                 write("d=\"");
             }
 
             while (reader.hasNext()) {
                 ft = reader.next();
                 writeFeature(ft);
                 ft = null;
             }
 
             if (doCollect) {
                 write("\"/>\n");
             }
 
             LOGGER.fine("encoded " + featureType.getTypeName());
         } catch (NoSuchElementException ex) {
             throw new DataSourceException(ex.getMessage(), ex);
         } catch (IllegalAttributeException ex) {
             throw new DataSourceException(ex.getMessage(), ex);
         }
     }
 
     private void setUpWriterHandler(FeatureType featureType, boolean doCollect)
         throws IOException {
         if (doCollect) {
             this.writerHandler = new CollectSVGHandler(featureWriter);
             LOGGER.finer("Established a collecting features writer handler");
         } else {
             this.writerHandler = new SVGFeatureWriterHandler();
 
             String typeName = featureType.getTypeName();
             /*
              * REVISIT: get rid of all this attribute stuff, since if attributes are 
              * needed it fits better to have SVG with gml attributes as another output
              * format for WFS's getFeature.
              */
             List atts = new ArrayList(0);// config.getAttributes(typeName);
 
             if (atts.contains("#FID")) {
                 this.writerHandler = new FIDSVGHandler(this.writerHandler);
                 atts.remove("#FID");
                 LOGGER.finer("Added FID handler decorator");
             }
 
             if (atts.contains("#BOUNDS")) {
                 this.writerHandler = new BoundsSVGHandler(this.writerHandler);
                 atts.remove("#BOUNDS");
                 LOGGER.finer("Added BOUNDS handler decorator");
             }
 
             if (atts.size() > 0) {
                 this.writerHandler = new AttributesSVGHandler(this.writerHandler);
                 LOGGER.finer("Added ATTRIBUTES handler decorator");
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param ft
      *
      * @throws IOException si algo ocurre escribiendo a <code>out</code>
      */
     public void writeFeature(Feature ft) throws IOException {
         writerHandler.startFeature(featureWriter, ft);
         writerHandler.startGeometry(featureWriter, ft);
         writerHandler.writeGeometry(featureWriter, ft);
         writerHandler.endGeometry(featureWriter, ft);
         writerHandler.endFeature(featureWriter, ft);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @author $author$
      * @version $Revision: 1.5 $
      */
     public class SVGFeatureWriterHandler {
         /**
          * DOCUMENT ME!
          *
          * @param featureWriter DOCUMENT ME!
          * @param ft DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         public void startFeature(SVGFeatureWriter featureWriter, Feature ft)
             throws IOException {
             featureWriter.startElement(ft);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param featureWriter DOCUMENT ME!
          * @param ft DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         public void endFeature(SVGFeatureWriter featureWriter, Feature ft)
             throws IOException {
             featureWriter.endElement(ft);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param featureWriter DOCUMENT ME!
          * @param ft DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         public void startGeometry(SVGFeatureWriter featureWriter, Feature ft)
             throws IOException {
             featureWriter.startGeometry(ft.getDefaultGeometry());
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param featureWriter DOCUMENT ME!
          * @param ft DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         public void writeGeometry(SVGFeatureWriter featureWriter, Feature ft)
             throws IOException {
             featureWriter.writeGeometry(ft.getDefaultGeometry());
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param featureWriter DOCUMENT ME!
          * @param ft DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         public void endGeometry(SVGFeatureWriter featureWriter, Feature ft)
             throws IOException {
             featureWriter.endGeometry(ft.getDefaultGeometry());
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @author $author$
      * @version $Revision: 1.5 $
      */
     public class CollectSVGHandler extends SVGFeatureWriterHandler {
         /** DOCUMENT ME! */
         private SVGFeatureWriter featureWriter;
 
         /**
          * Creates a new CollectSVGHandler object.
          *
          * @param featureWriter DOCUMENT ME!
          */
         public CollectSVGHandler(SVGFeatureWriter featureWriter) {
             this.featureWriter = featureWriter;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param ft DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         public void writeFeature(Feature ft) throws IOException {
             featureWriter.writeGeometry(ft.getDefaultGeometry());
             write('\n');
         }
     }
 
     /**
      * decorator handler that adds the feature id as the "id" attribute
      */
     public class FIDSVGHandler extends SVGFeatureWriterHandler {
         /** DOCUMENT ME! */
         private SVGFeatureWriterHandler handler;
 
         /**
          * Creates a new NormalSVGHandler object.
          *
          * @param handler DOCUMENT ME!
          */
         public FIDSVGHandler(SVGFeatureWriterHandler handler) {
             this.handler = handler;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param featureWriter DOCUMENT ME!
          * @param ft DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         public void startFeature(SVGFeatureWriter featureWriter, Feature ft)
             throws IOException {
             handler.startFeature(featureWriter, ft);
             write(" id=\"");
 
             try {
                 write(ft.getID());
             } catch (IOException ex) {
                 System.err.println("error getting fid from " + ft);
                 throw ex;
             }
 
             write("\"");
         }
     }
 
     /**
      * decorator handler that adds the feature id as the "id" attribute
      */
     public class BoundsSVGHandler extends SVGFeatureWriterHandler {
         /** DOCUMENT ME! */
         private SVGFeatureWriterHandler handler;
 
         /**
          * Creates a new NormalSVGHandler object.
          *
          * @param handler DOCUMENT ME!
          */
         public BoundsSVGHandler(SVGFeatureWriterHandler handler) {
             this.handler = handler;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param featureWriter DOCUMENT ME!
          * @param ft DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         public void startFeature(SVGFeatureWriter featureWriter, Feature ft)
             throws IOException {
             handler.startFeature(featureWriter, ft);
 
             Geometry geom = ft.getDefaultGeometry();
             Envelope env = geom.getEnvelopeInternal();
             write(" bounds=\"");
             write(env.getMinX());
             write(' ');
             write(env.getMinY());
             write(' ');
             write(env.getMaxX());
             write(' ');
             write(env.getMaxY());
             write('\"');
         }
     }
 
     /**
      * decorator handler that adds the feature id as the "id" attribute
      */
     public class AttributesSVGHandler extends SVGFeatureWriterHandler {
         /** DOCUMENT ME! */
         private SVGFeatureWriterHandler handler;
 
         /**
          * Creates a new NormalSVGHandler object.
          *
          * @param handler DOCUMENT ME!
          */
         public AttributesSVGHandler(SVGFeatureWriterHandler handler) {
             this.handler = handler;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param featureWriter DOCUMENT ME!
          * @param ft DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         public void startFeature(SVGFeatureWriter featureWriter, Feature ft)
             throws IOException {
             handler.startFeature(featureWriter, ft);
 
             FeatureType type = ft.getFeatureType();
             int numAtts = type.getAttributeCount();
             String name;
             Object value;
 
             for (int i = 0; i < numAtts; i++) {
                 value = ft.getAttribute(i);
 
                 if ((value != null) && !(value instanceof Geometry)) {
                     write(' ');
                     write(type.getAttributeType(i).getName());
                     write("=\"");
                     encodeAttribute(String.valueOf(value));
                     write('\"');
                 }
             }
         }
 
         /**
          * Parses the passed string, and encodes the special characters (used
          * in xml for special purposes) with the appropriate codes. e.g.
          * '&lt;' is changed to '&amp;lt;'
          *
          * @param inData The string to encode into xml.
          *
          * @throws IOException DOCUMENT ME!
          *
          * @task REVISIT: Once we write directly to out, as we should, this
          *       method should be simpler, as we can just write strings with
          *       escapes directly to out, replacing as we iterate of chars to
          *       write them.
          */
         private void encodeAttribute(String inData) throws IOException {
             //return null, if null is passed as argument
             if (inData == null) {
                 return;
             }
 
             //get the length of input String
             int length = inData.length();
 
             char charToCompare;
 
             //iterate over the input String
             for (int i = 0; i < length; i++) {
                 charToCompare = inData.charAt(i);
 
                 //if the ith character is special character, replace by code
                 if (charToCompare == '"') {
                     write("&quot;");
                 } else if (charToCompare > 127) {
                     writeUnicodeEscapeSequence(charToCompare);
                 } else {
                     write(charToCompare);
                 }
             }
         }
 
         /**
          * returns the xml unicode escape sequence for the character
          * <code>c</code>, such as <code>"&#x00d1;"</code> for the character
          * <code>'?'</code>
          *
          * @param c DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         private void writeUnicodeEscapeSequence(char c)
             throws IOException {
             write("&#x");
 
             String hex = Integer.toHexString(c);
             int pendingZeros = 4 - hex.length();
 
             for (int i = 0; i < pendingZeros; i++)
                 write('0');
 
             write(hex);
             write(';');
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @author $author$
      * @version $Revision: 1.5 $
      */
     private abstract class SVGFeatureWriter {
         /**
          * DOCUMENT ME!
          * @param feature TODO
          *
          * @throws IOException DOCUMENT ME!
          */
         protected abstract void startElement(Feature feature) throws IOException;
 
         /**
          * DOCUMENT ME!
          * @param geom TODO
          *
          * @throws IOException DOCUMENT ME!
          */
         protected abstract void startGeometry(Geometry geom) throws IOException;
 
         /**
          * DOCUMENT ME!
          *
          * @param geom DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected abstract void writeGeometry(Geometry geom)
             throws IOException;
 
         /**
          * DOCUMENT ME!
          * @param geom TODO
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void endGeometry(Geometry geom) throws IOException {
             write("\"");
         }
 
         /**
          * DOCUMENT ME!
          * @param feature TODO
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void endElement(Feature feature) throws IOException {
             write("/>\n");
         }
 
         /**
          * Writes the content of the <b>d</b> attribute in a <i>path</i> SVG
          * element
          * 
          * <p>
          * While iterating over the coordinate array passed as parameter, this
          * method performs a kind of very basic path generalization, verifying
          * that the distance between the current coordinate and the last
          * encoded one is greater than the minimun distance expressed by the
          * field <code>minCoordDistance</code> and established by the method
          * {@link #setReferenceSpace(Envelope, float)
          * setReferenceSpace(Envelope, blurFactor)}
          * </p>
          *
          * @param coords
          *
          * @throws IOException
          */
         protected void writePathContent(Coordinate[] coords)
             throws IOException {
             write('M');
 
             Coordinate prev = coords[0];
             Coordinate curr = null;
             write(getX(prev.x));
             write(' ');
             write(getY(prev.y));
 
             int nCoords = coords.length;
             write('l');
 
             for (int i = 1; i < nCoords; i++) {
                 curr = coords[i];
 
                 //let at least 3 points in case it is a polygon
                 if ((i > 3) && (prev.distance(curr) <= minCoordDistance)) {
                     ++coordsSkipCount;
 
                     continue;
                 }
 
                 ++coordsWriteCount;
                 write((getX(curr.x) - getX(prev.x)));
                 write(' ');
                 write(getY(curr.y) - getY(prev.y));
                 write(' ');
                 prev = curr;
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param coords DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void writeClosedPathContent(Coordinate[] coords)
             throws IOException {
             writePathContent(coords);
             write('Z');
         }
     }
 
     /**
      *
      */
     private class PointWriter extends SVGFeatureWriter {
         /**
          * Creates a new PointWriter object.
          */
         public PointWriter() {
         }
 
         /**
          * DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void startElement(Feature feature) throws IOException {
             write(pointsAsCircles ? "<circle r='0.25%' fill='blue'" : "<use");
         }
 
         /**
          * DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         /**
          * protected void writeAttributes(Feature ft) throws IOException { if
          * (!pointsAsCircles) { write(" xlink:href=\"#"); if (attributeStyle
          * != null) { write(String.valueOf(ft.getAttribute(attributeStyle)));
          * } else { write("point"); } write("\""); }
          * super.writeAttributes(ft); }
          *
          * @throws IOException DOCUMENT ME!
          */
         /**
          * DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void startGeometry(Geometry geom) throws IOException {
         }
 
         /**
          * overrides writeBounds for points to do nothing. You can get the
          * position of the point with the x and y attributes of the "use" SVG
          * element written to represent each point
          *
          * @param env DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void writeBounds(Envelope env) throws IOException {
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param geom DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void writeGeometry(Geometry geom) throws IOException {
             Point p = (Point) geom;
 
             if (pointsAsCircles) {
                 write(" cx=\"");
                 write(getX(p.getX()));
                 write("\" cy=\"");
                 write(getY(p.getY()));
             } else {
                 write(" x=\"");
                 write(getX(p.getX()));
                 write("\" y=\"");
                 write(getY(p.getY()));
 		//Issue GEOS-193, from John Steining.
                 write("\" xlink:href=\"#point");
                 //putting this in to fix the issue, but I am not sure about
                 //the broader implications - I don't think we need it for
                 //pointsAsCircles.  And it looks like the quote gets closed
                 //somewhere else, but I'm not sure where.
             }
         }
     }
 
     /**
      *
      */
     private class MultiPointWriter extends PointWriter {
         /**
          * Creates a new MultiPointWriter object.
          */
         public MultiPointWriter() {
         }
 
         /**
          * DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void startElement(Feature feature) throws IOException {
             write("<g ");
         }
 
         /**
          * DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void startGeometry(Geometry geom) throws IOException {
             write("/>\n");
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param geom DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void writeGeometry(Geometry geom) throws IOException {
             MultiPoint mp = (MultiPoint) geom;
 
             for (int i = 0; i < mp.getNumGeometries(); i++) {
                 super.startElement(null);
                 super.writeGeometry(mp.getGeometryN(i));
                 super.endGeometry(mp.getGeometryN(i));
                 super.endElement(null);
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void endElement(Feature feature) throws IOException {
             write("</g>\n");
         }
     }
 
     /**
      * Writer to handle feature types which contain a Geometry attribute that 
      * is actually of the class Geometry. This can occur in heterogeneous data
      * sets.
      * 
      * @author Justin Deoliveira, jdeolive@openplans.org
      *
      */
     private class GeometryWriter extends SVGFeatureWriter {
 
     	SVGFeatureWriter delegate;
     	
     	protected void startElement(Feature feature) throws IOException {
     		
     		Geometry g = feature.getDefaultGeometry();
     		delegate = null;
     		if (g != null) {
     			delegate = (SVGFeatureWriter) writers.get(g.getClass());	
     		}
 			
     		if (delegate == null) {
     			throw new IllegalArgumentException(
 	                "No SVG Feature writer defined for " + g
                 ); 
     		}
     		delegate.startElement(feature);
 		}
 
 		protected void startGeometry(Geometry geom) throws IOException {
 			delegate.startGeometry(geom);
 		}
 
 		protected void writeGeometry(Geometry geom) throws IOException {
 			delegate.writeGeometry(geom);
 		}
     	
     }
     
     /**
      *
      */
     private class LineStringWriter extends SVGFeatureWriter {
         /**
          * Creates a new LineStringWriter object.
          */
         public LineStringWriter() {
         }
 
         /**
          * DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void startElement(Feature feature) throws IOException {
             write("<path");
         }
 
         /**
          * DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void startGeometry(Geometry geom) throws IOException {
             write(" d=\"");
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param geom DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void writeGeometry(Geometry geom) throws IOException {
             writePathContent(((LineString) geom).getCoordinates());
         }
     }
 
     /**
      *
      */
     private class MultiLineStringWriter extends LineStringWriter {
         /**
          * Creates a new MultiLineStringWriter object.
          */
         public MultiLineStringWriter() {
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param geom DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void writeGeometry(Geometry geom) throws IOException {
             MultiLineString ml = (MultiLineString) geom;
 
             for (int i = 0; i < ml.getNumGeometries(); i++) {
                 super.writeGeometry(ml.getGeometryN(i));
             }
         }
     }
 
     /**
      *
      */
     private class PolygonWriter extends SVGFeatureWriter {
         /**
          * Creates a new PolygonWriter object.
          */
         public PolygonWriter() {
         }
 
         /**
          * DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void startElement(Feature feature) throws IOException {
             write("<path");
         }
 
         /**
          * DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void startGeometry(Geometry geom) throws IOException {
             write(" d=\"");
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param geom DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void writeGeometry(Geometry geom) throws IOException {
             Polygon poly = (Polygon) geom;
             LineString shell = poly.getExteriorRing();
             int nHoles = poly.getNumInteriorRing();
             writeClosedPathContent(shell.getCoordinates());
 
             for (int i = 0; i < nHoles; i++)
                 writeClosedPathContent(poly.getInteriorRingN(i).getCoordinates());
         }
     }
 
     /**
      *
      */
     private class MultiPolygonWriter extends PolygonWriter {
         /**
          * Creates a new MultiPolygonWriter object.
          */
         public MultiPolygonWriter() {
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param geom DOCUMENT ME!
          *
          * @throws IOException DOCUMENT ME!
          */
         protected void writeGeometry(Geometry geom) throws IOException {
             MultiPolygon mpoly = (MultiPolygon) geom;
 
             for (int i = 0; i < mpoly.getNumGeometries(); i++) {
                 super.writeGeometry(mpoly.getGeometryN(i));
             }
         }
     }
 }
