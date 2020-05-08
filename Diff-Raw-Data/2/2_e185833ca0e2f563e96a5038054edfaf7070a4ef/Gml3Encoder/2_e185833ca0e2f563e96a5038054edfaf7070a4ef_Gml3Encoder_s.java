 package org.gvsig.bxml.geoserver;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.Timestamp;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.geoserver.wfs.GMLInfo.SrsNameStyle;
 import org.geotools.feature.type.DateUtil;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.gml2.bindings.GML2EncodingUtils;
 import org.geotools.gml3.GML;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.geotools.util.Converters;
 import org.geotools.util.Range;
 import org.gvsig.bxml.stream.BxmlStreamWriter;
 import org.gvsig.bxml.stream.EventType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.feature.type.GeometryDescriptor;
 import org.opengis.geometry.BoundingBox;
 import org.opengis.geometry.DirectPosition;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 import com.vividsolutions.jts.geom.CoordinateSequence;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.MultiLineString;
 import com.vividsolutions.jts.geom.MultiPoint;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 
 /**
  * Pure utility class to help in encoding common gml constructs, such as geometries, envelope,
  * srsName attributes, etc.
  * 
  * @author Gabriel Roldan (OpenGeo)
  * @version $Id$
  */
 public final class Gml3Encoder {
 
     private final SrsNameStyle srsNameStyle;
 
     private final Map<CoordinateReferenceSystem, String> srsNameUris;
 
     private static final Map<Class<?>, AttributeEncoder> encodingBindings;
 
     private final EncoderConfig config;
 
     public Gml3Encoder(final EncoderConfig config) {
         this.config = config;
         SrsNameStyle srsNameStyle = config.getSrsNameStyle();
         this.srsNameStyle = srsNameStyle;
         srsNameUris = new TreeMap<CoordinateReferenceSystem, String>(
                 new Comparator<CoordinateReferenceSystem>() {
                     public int compare(CoordinateReferenceSystem crs1,
                             CoordinateReferenceSystem crs2) {
                         if (crs1 == crs2) {
                             return 0;
                         }
                         return 1;
                     }
                 });
     }
 
     public EncoderConfig getConfig() {
         return config;
     }
 
     private static final AttributeEncoder UNKNOWN_ATT_TYPE_ENCODER = new AttributeEncoder() {
         @Override
         public void encode(final Gml3Encoder gmlEncoder, Object value,
                 AttributeDescriptor descriptor, BxmlStreamWriter encoder) throws IOException {
             String stringValue = (String) Converters.convert(value, String.class);
             if (stringValue != null) {
                 encoder.writeValue(stringValue);
             }
         }
     };
 
     static {
         /**
          * Registry of stateless per class type attribute encoders encoders
          */
         Map<Class<?>, AttributeEncoder> encoders = new HashMap<Class<?>, AttributeEncoder>();
 
         encoders.put(Boolean.class, new BooleanEncoder());
         encoders.put(Byte.class, new ByteEncoder());
         encoders.put(Short.class, new ShortEncoder());
         encoders.put(Integer.class, new IntegerEncoder());
         encoders.put(Long.class, new LongEncoder());
         encoders.put(Float.class, new FloatEncoder());
         encoders.put(Double.class, new DoubleEncoder());
         encoders.put(String.class, new StringEncoder());
         encoders.put(java.util.Date.class, new DateTimeEncoder());
         encoders.put(java.sql.Date.class, new DateEncoder());
         encoders.put(java.sql.Time.class, new TimeEncoder());
         encoders.put(Timestamp.class, new TimestampEncoder());
         encoders.put(BigInteger.class, new BigIntegerEncoder());
         encoders.put(BigDecimal.class, new BigDecimalEncoder());
         encoders.put(BoundingBox.class, new BoundingBoxEncoder());
 
         encoders.put(Geometry.class, new GeometryEncoder());
         encoders.put(GeometryCollection.class, new GeometryCollectionEncoder());
         encoders.put(Point.class, new PointEncoder());
         encoders.put(MultiPoint.class, new MultiPointEncoder());
         encoders.put(LineString.class, new LineStringEncoder());
         encoders.put(MultiLineString.class, new MultiLineStringEncoder());
         encoders.put(Polygon.class, new PolygonEncoder());
         encoders.put(MultiPolygon.class, new MultiPolygonEncoder());
         encodingBindings = Collections.unmodifiableMap(encoders);
     }
 
     /**
      * @author Gabriel Roldan (OpenGeo)
      * @version $Id$
      */
     public static abstract class AttributeEncoder {
         /**
          * @param value
          * @param descriptor
          * @param encoder
          * @throws IOException
          */
         public abstract void encode(Gml3Encoder gmlEncoder, Object value,
                 AttributeDescriptor descriptor, BxmlStreamWriter encoder) throws IOException;
     }
 
     /**
      * Generic geometry encoder, delegates to appropriate encoder based on geometry type, or writes
      * down a {@code gml:Null} element if {@code geometry} is null;
      */
     public final void encodeGeometry(final BxmlStreamWriter encoder,
             final CoordinateReferenceSystem crs, final Geometry geometry) throws IOException {
         if (geometry == null) {
             encoder.writeStartElement(GML.Null);
             encoder.writeEndElement();
             return;
         }
         AttributeEncoder geometryAttEncoder = getAttributeEncoder(geometry.getClass());
         if (geometryAttEncoder == null) {
             throw new IllegalArgumentException(
                     "Didn't find an AttributeEncoder for this kind of Geomerty");
         }
         if (!(geometryAttEncoder instanceof AbstractGeometryEncoder)) {
             throw new IllegalStateException(geometryAttEncoder.getClass().getName()
                     + " is not a GeometryEncoder");
         }
         final AbstractGeometryEncoder geomEncoder = (AbstractGeometryEncoder) geometryAttEncoder;
         geomEncoder.encode(this, geometry, encoder, crs);
     }
 
     /**
      * Encodes the gml:srsName attribute.
      * <p>
      * Does not close the attribute list, so the calling code shall make sure to call
      * encoder.writeEndAttributes() as appropriate.
      * </p>
      * 
      * @param encoder
      * @param crs
      * @throws IOException
      */
     public final void encodeSrsName(final BxmlStreamWriter encoder,
             final CoordinateReferenceSystem crs) throws IOException {
         if (crs == null) {
             return;
         }
 
         String srsUri = getSrsUri(crs);
         if (srsUri == null) {
             return;
         }
 
         if (encoder.supportsStringTableValues()) {
             final long srsUriStringTableEntryId = encoder.getStringTableReference(srsUri);
             encoder.writeStartAttribute(GML.srsName);
             encoder.writeStringTableValue(srsUriStringTableEntryId);
         } else {
             encoder.writeStartAttribute(GML.srsName);
             encoder.writeValue(srsUri);
         }
     }
 
     private String getSrsUri(final CoordinateReferenceSystem crs) {
         String srsUri = srsNameUris.get(crs);
         if (srsUri == null) {
             String epsgCode;
             if (CRS.equalsIgnoreMetadata(DefaultGeographicCRS.WGS84, crs)) {
                 epsgCode = "4326";
             } else {
                 epsgCode = GML2EncodingUtils.epsgCode(crs);
             }
             srsUri = this.srsNameStyle.getPrefix() + epsgCode;
             srsNameUris.put(crs, srsUri);
         }
         return srsUri;
     }
 
     /**
      * Writes the {@code coordinates} as an array of doubles, does not write any element, but just
      * the array contents.
      * 
      * @param coordinates
      * @param encoder
      * @param crsDimension
      * @throws IOException
      */
     public final void encodeCoordinates(final CoordinateSequence coordinates,
             final BxmlStreamWriter encoder, final int crsDimension) throws IOException {
 
         // TODO: REVISIT: if coordinates == null should we write gml:Null?
         if (coordinates != null) {
 
             final int nCoords = coordinates.size();
             // TODO: dimensions and CRS dimensions should match
             final int dimensions = Math.min(crsDimension, coordinates.getDimension());
 
             encoder.startArray(EventType.VALUE_DOUBLE, dimensions * nCoords);
             double ordinate;
             for (int coordN = 0; coordN < nCoords; coordN++) {
                 for (int dimN = 0; dimN < dimensions; dimN++) {
                     ordinate = coordinates.getOrdinate(coordN, dimN);
                     encoder.writeValue(ordinate);
                 }
             }
             encoder.endArray();
         }
     }
 
     /**
      * Encodes a {@code gml:posList} element for the given coordinates and srs.
      * <p>
      * The posList element will contain the attributes crsDimension and count, as well as the
      * srsName attribute, if the crs id can be inferred from the CRS
      * </p>
      * 
      * @param encoder
      * @param positionsList
      * @param crs
      *            the CRS for which to get the URI for the {@code gml:srsName} attribute, may be
      *            {@code null}, in which case the {@code gml:srsName} attribute is not written.
      * @throws IOException
      */
     public final void encodePosList(final BxmlStreamWriter encoder,
             final CoordinateSequence positionsList, final CoordinateReferenceSystem crs)
             throws IOException {
         encoder.writeStartElement(GML.posList);
 
         encodeSrsName(encoder, crs);
 
         final int crsDimension = crs.getCoordinateSystem().getDimension();
         encoder.writeStartAttribute(GML.NAMESPACE, "srsDimension");
         encoder.writeValue(crsDimension);
 
         encoder.writeStartAttribute(GML.NAMESPACE, "count");
         encoder.writeValue(positionsList.size());
 
         encoder.writeEndAttributes();
 
         encodeCoordinates(positionsList, encoder, crsDimension);
 
         encoder.writeEndElement();
     }
 
     public void encodeDirectPosition(BxmlStreamWriter encoder, DirectPosition directPosition,
             final String namespaceUri, final String localName) throws IOException {
 
         encoder.writeStartElement(namespaceUri, localName);
 
         final int dimension = directPosition.getDimension();
         encoder.startArray(EventType.VALUE_DOUBLE, dimension);
         for (int dim = 0; dim < dimension; dim++) {
            encoder.writeValue(directPosition.getOrdinate(0));
         }
         encoder.endArray();
 
         encoder.writeEndElement();
     }
 
     /**
      * Encodes a {@link GML#Envelope gml:Envelope} for {@code bounds}, or {@link GML#Null} if
      * {@code bounds} is {@code null}.
      * <p>
      * If {@code bounds} is {@code null}, the {@code gml:Null} element will be written with
      * {@code "unknown"} reason, like in {@code <gml:Null>unknown</gml:Null>}
      * </p>
      * 
      * @param encoder
      *            the encoder to write to
      * @param bounds
      *            the envelope to encode, may be {@code null}
      * @throws IOException
      */
     public void encodeEnvelope(final BxmlStreamWriter encoder, final BoundingBox bounds)
             throws IOException {
 
         if (bounds == null) {
             encoder.writeStartElement(GML.Null);
             encoder.writeValue("unknown");
             encoder.writeEndElement();
         } else {
             final CoordinateReferenceSystem crs = bounds.getCoordinateReferenceSystem();
 
             encoder.writeStartElement(GML.Envelope);
             // srsName attribute
             encodeSrsName(encoder, crs);
             encoder.writeEndAttributes();
 
             // lowerCorner
             DirectPosition directPosition = bounds.getLowerCorner();
             encodeDirectPosition(encoder, directPosition, GML.NAMESPACE, "lowerCorner");
 
             // upperCorner
             directPosition = bounds.getUpperCorner();
             encodeDirectPosition(encoder, directPosition, GML.NAMESPACE, "upperCorner");
 
             encoder.writeEndElement();// Envelope
         }
     }
 
     public void encodePoint(final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs,
             Point point) throws IOException {
         encoder.writeStartElement(GML.Point);
         final CoordinateSequence coordinates = (point).getCoordinateSequence();
 
         encoder.writeStartElement(GML.pos);
         encodeSrsName(encoder, crs);
         encoder.writeEndAttributes();
 
         encodeCoordinates(coordinates, encoder, crs.getCoordinateSystem().getDimension());
         encoder.writeEndElement();
         encoder.writeEndElement();
     }
 
     /**
      * Writes down a {@link GML#LinearRing LinearRing} element out of the provided
      * {@code linearRing} coordinates.
      * 
      * @param encoder
      * @param linearRing
      * @param srsUri
      * @throws IOException
      */
     public void encodeLinearRring(final BxmlStreamWriter encoder, CoordinateSequence linearRing,
             final CoordinateReferenceSystem crs) throws IOException {
         encoder.writeStartElement(GML.LinearRing);
 
         encodePosList(encoder, linearRing, crs);
 
         encoder.writeEndElement();// LinearRing
     }
 
     public void encode(final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs,
             final Polygon poly) throws IOException {
         encoder.writeStartElement(GML.Polygon);
         CoordinateSequence linearRing;
         linearRing = poly.getExteriorRing().getCoordinateSequence();
         encoder.writeStartElement(GML.exterior);
         encodeLinearRring(encoder, linearRing, crs);
         encoder.writeEndElement();// exterior
 
         final int numInteriorRings = poly.getNumInteriorRing();
         for (int ringN = 0; ringN < numInteriorRings; ringN++) {
             linearRing = poly.getInteriorRingN(ringN).getCoordinateSequence();
             encoder.writeStartElement(GML.interior);
             encodeLinearRring(encoder, linearRing, crs);
             encoder.writeEndElement();// interior
         }
         encoder.writeEndElement();
     }
 
     public void encode(final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs,
             final MultiPolygon mpoly) throws IOException {
         encoder.writeStartElement(GML.MultiSurface);
 
         final int numGeoms = mpoly.getNumGeometries();
         Polygon poly;
         for (int geomN = 0; geomN < numGeoms; geomN++) {
             poly = (Polygon) mpoly.getGeometryN(geomN);
             if (poly != null) {
                 encoder.writeStartElement(GML.surfaceMember);
                 encode(encoder, crs, poly);
                 encoder.writeEndElement();
             }
         }
 
         encoder.writeEndElement();// MultiSurface
     }
 
     public void encode(final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs,
             LineString line) throws IOException {
         encoder.writeStartElement(GML.LineString);
         CoordinateSequence coordinates = (line).getCoordinateSequence();
         encodePosList(encoder, coordinates, crs);
         encoder.writeEndElement();
     }
 
     public void encode(final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs,
             final MultiLineString mline) throws IOException {
         encoder.writeStartElement(GML.MultiLineString);
 
         final int nGeoms = mline.getNumGeometries();
         LineString member;
         for (int geomN = 0; geomN < nGeoms; geomN++) {
             member = (LineString) mline.getGeometryN(geomN);
             if (member != null) {
                 encoder.writeStartElement(GML.lineStringMember);
                 encode(encoder, crs, member);
                 encoder.writeEndElement();
             }
         }
         encoder.writeEndElement();
     }
 
     public void encode(final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs,
             final MultiPoint mpoint) throws IOException {
         encoder.writeStartElement(GML.MultiPoint);
 
         final int nGeoms = mpoint.getNumGeometries();
         Geometry member;
         for (int geomN = 0; geomN < nGeoms; geomN++) {
             encoder.writeStartElement(GML.pointMember);
             member = mpoint.getGeometryN(geomN);
             if (member != null) {
                 encodePoint(encoder, crs, (Point) member);
             }
             encoder.writeEndElement();
         }
         encoder.writeEndElement();
     }
 
     /**
      * Abstract value encoder for {@link Geometry}
      * 
      * @author Gabriel Roldan (OpenGeo)
      * @version $Id$
      */
     private static abstract class AbstractGeometryEncoder extends AttributeEncoder {
 
         public final void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             encode(gmlEncoder, value, encoder,
                     ((GeometryDescriptor) descriptor).getCoordinateReferenceSystem());
         }
 
         public abstract void encode(final Gml3Encoder gmlEncoder, Object value,
                 BxmlStreamWriter encoder, CoordinateReferenceSystem crs) throws IOException;
     }
 
     /**
      * Value encoder for a generic {@link Geometry}
      * 
      * @author Gabriel Roldan (OpenGeo)
      * @version $Id$
      */
     private static class GeometryEncoder extends AbstractGeometryEncoder {
 
         /**
          * @see org.gvsig.bxml.geoserver.BinaryGml3OutputFormat.AttributeEncoder#encode
          */
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object geom,
                 final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs)
                 throws IOException {
             if (geom != null) {
                 gmlEncoder.encodeGeometry(encoder, crs, (Geometry) geom);
             }
         }
     }
 
     /**
      * A value encoder for {@link BoundingBox}
      * 
      * @author Gabriel Roldan (OpenGeo)
      * @version $Id$
      */
     private static class BoundingBoxEncoder extends AttributeEncoder {
 
         /**
          * Encodes the provided value (which shall be an instance of {@link ReferencedEnvelope}, as
          * a {@code gml:Envelope} element.
          * 
          * @param value
          *            the ReferencedEnvelope to encode. Not null.
          * @param type
          *            the attribute type, not used
          * @param encoder
          *            the encoder used to write down the gml:Envelope element with
          * @throws IOException
          */
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
 
             final BoundingBox bounds = (BoundingBox) value;
 
             // let the utility method handle the null case
             gmlEncoder.encodeEnvelope(encoder, bounds);
         }
 
     }
 
     /**
      * Value encoder for {@link Point}
      * 
      * @author Gabriel Roldan (OpenGeo)
      * @version $Id$
      */
     private static class PointEncoder extends AbstractGeometryEncoder {
 
         /**
          * @see org.gvsig.bxml.geoserver.BinaryGml3OutputFormat.AttributeEncoder#encode
          */
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object geom,
                 final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs)
                 throws IOException {
             if (geom != null) {
                 gmlEncoder.encodePoint(encoder, crs, (Point) geom);
             }
         }
     }
 
     /**
      * Value encoder for {@link MultiPolygon}
      * 
      * @author Gabriel Roldan (OpenGeo)
      * @version $Id$
      */
     private static class MultiPolygonEncoder extends AbstractGeometryEncoder {
 
         /**
          * @see org.gvsig.bxml.geoserver.BinaryGml3OutputFormat.PolygonEncoder#encode
          * @see GML#MultiSurface
          * @see GML#surfaceMembers
          * @see GML#Polygon
          */
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object geom,
                 final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs)
                 throws IOException {
             if (geom != null) {
                 gmlEncoder.encode(encoder, crs, (MultiPolygon) geom);
             }
         }
 
     }
 
     /**
      * Value encoder for {@link Polygon}
      * 
      * @author Gabriel Roldan (OpenGeo)
      * @version $Id$
      */
     private static class PolygonEncoder extends AbstractGeometryEncoder {
 
         /**
          * @see org.gvsig.bxml.geoserver.BinaryGml3OutputFormat.AttributeEncoder#encode
          * @see GML#Polygon
          * @see GML#exterior
          * @see GML#interior
          * @see GML#LinearRing
          */
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object geom,
                 final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs)
                 throws IOException {
             if (geom != null) {
                 gmlEncoder.encode(encoder, crs, (Polygon) geom);
             }
         }
     }
 
     /**
      * Value encoder for {@link LineString}
      * 
      * @author Gabriel Roldan (OpenGeo)
      * @version $Id$
      */
     private static class LineStringEncoder extends AbstractGeometryEncoder {
 
         /**
          * @see org.gvsig.bxml.geoserver.BinaryGml3OutputFormat.AttributeEncoder#encode
          * @see GML#LineString
          */
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object geom,
                 final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs)
                 throws IOException {
             if (geom != null) {
                 gmlEncoder.encode(encoder, crs, (LineString) geom);
             }
         }
     }
 
     /**
      * Value encoder for {@link MultiLineString}
      * 
      * @author Gabriel Roldan (OpenGeo)
      * @version $Id$
      */
     private static class MultiLineStringEncoder extends AbstractGeometryEncoder {
 
         /**
          * @see org.gvsig.bxml.geoserver.BinaryGml3OutputFormat.LineStringEncoder#encode
          * @see GML#MultiLineString
          * @see GML#lineStringMember
          * @see LineStringEncoder#encode
          */
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object geom,
                 final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs)
                 throws IOException {
             if (geom != null) {
                 gmlEncoder.encode(encoder, crs, (MultiLineString) geom);
             }
         }
     }
 
     /**
      * Value encoder for {@link MultiPoint}
      * 
      * @author Gabriel Roldan (OpenGeo)
      * @version $Id$
      */
     private static class MultiPointEncoder extends AbstractGeometryEncoder {
 
         /**
          * @see org.gvsig.bxml.geoserver.BinaryGml3OutputFormat.PointEncoder#encode
          * @see GML#MultiPoint
          * @see GML#pointMembers
          * @see PointEncoder#encode
          */
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object geom,
                 final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs)
                 throws IOException {
             if (geom != null) {
                 gmlEncoder.encode(encoder, crs, (MultiPoint) geom);
             }
         }
     }
 
     /**
      * Value encoder for {@link GeometryCollection}
      * 
      * @author Gabriel Roldan (OpenGeo)
      * @version $Id$
      */
     private static class GeometryCollectionEncoder extends AbstractGeometryEncoder {
 
         /**
          * @see AttributeEncoder#encode
          * @see GML#MultiGeometry
          * @see GML#geometryMembers
          */
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object geom,
                 final BxmlStreamWriter encoder, final CoordinateReferenceSystem crs)
                 throws IOException {
             final GeometryCollection gcol = (GeometryCollection) geom;
 
             encoder.writeStartElement(GML.MultiGeometry);
             if (gcol != null) {
                 final int nGeoms = gcol.getNumGeometries();
                 Geometry member;
                 encoder.writeStartElement(GML.geometryMembers);
                 for (int geomN = 0; geomN < nGeoms; geomN++) {
                     member = gcol.getGeometryN(geomN);
                     if (member != null) {
                         AbstractGeometryEncoder memberEncoder;
                         memberEncoder = (AbstractGeometryEncoder) Gml3Encoder
                                 .getAttributeEncoder(member.getClass());
                         memberEncoder.encode(gmlEncoder, member, encoder, crs);
                     }
                 }
                 encoder.writeEndElement();
             }
             encoder.writeEndElement();
         }
     }
 
     /**
      * Value encoder for {@link java.util.Date}
      * 
      * @version $Id$
      */
     private static class DateTimeEncoder extends AttributeEncoder {
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             final Date date = (Date) value;
             if (date != null) {
                 String stringValue = DateUtil.serializeDateTime(date.getTime(), true);
                 encoder.writeValue(stringValue);
             }
         }
     }
 
     /**
      * Value encoder for {@link java.sql.Date}
      * 
      * @version $Id$
      */
     private static class DateEncoder extends AttributeEncoder {
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             final java.sql.Date date = (java.sql.Date) value;
             if (date != null) {
                 String stringValue = DateUtil.serializeDate(date);
                 encoder.writeValue(stringValue);
             }
         }
     }
 
     /**
      * Value encoder for {@link java.sql.Time}
      * 
      * @version $Id$
      */
     private static class TimeEncoder extends AttributeEncoder {
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             final java.sql.Time time = (java.sql.Time) value;
             if (time != null) {
                 String stringValue = DateUtil.serializeSqlTime(time);
                 encoder.writeValue(stringValue);
             }
         }
     }
 
     /**
      * Value encoder for {@link Timestamp}
      * 
      * @version $Id$
      */
     private static class TimestampEncoder extends AttributeEncoder {
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             final Timestamp timeStamp = (Timestamp) value;
             if (timeStamp != null) {
                 String stringValue = DateUtil.serializeTimestamp(timeStamp);
                 encoder.writeValue(stringValue);
             }
         }
     }
 
     /**
      * Value encoder for {@link String}
      * 
      * @version $Id$
      */
     private static class StringEncoder extends AttributeEncoder {
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             if (value != null) {
                 encoder.writeValue(((CharSequence) value).toString());
             }
         }
     }
 
     /**
      * Value encoder for {@link Double}
      * 
      * @version $Id$
      */
     private static class DoubleEncoder extends AttributeEncoder {
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             if (value != null) {
                 encoder.writeValue(((Double) value).doubleValue());
             }
         }
     }
 
     /**
      * Value encoder for {@link BigInteger}
      * 
      * @version $Id$
      */
     private static class BigIntegerEncoder extends AttributeEncoder {
 
         private static final Range<BigInteger> BYTE_RANGE = new Range<BigInteger>(BigInteger.class,
                 BigInteger.valueOf(Byte.MIN_VALUE), true, BigInteger.valueOf(Byte.MAX_VALUE), true);
 
         private static final Range<BigInteger> INT_RANGE = new Range<BigInteger>(BigInteger.class,
                 BigInteger.valueOf(Integer.MIN_VALUE), true, BigInteger.valueOf(Integer.MAX_VALUE),
                 true);
 
         private static final Range<BigInteger> LONG_RANGE = new Range<BigInteger>(BigInteger.class,
                 BigInteger.valueOf(Long.MIN_VALUE), true, BigInteger.valueOf(Long.MAX_VALUE), true);
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             if (value != null) {
                 final BigInteger bi = (BigInteger) value;
                 if (BYTE_RANGE.contains(bi)) {
                     encoder.writeValue(bi.byteValue());
                 } else if (INT_RANGE.contains(bi)) {
                     encoder.writeValue(bi.intValue());
                 } else if (LONG_RANGE.contains(bi)) {
                     encoder.writeValue(bi.longValue());
                 } else {
                     // well, it really was a big number...
                     encoder.writeValue(bi.toString());
                 }
             }
         }
     }
 
     /**
      * Value encoder for {@link BigDecimal}
      * 
      * @version $Id$
      */
     private static class BigDecimalEncoder extends AttributeEncoder {
 
         private static final Range<BigDecimal> DOUBLE_RANGE = new Range<BigDecimal>(
                 BigDecimal.class, BigDecimal.valueOf(Double.MIN_VALUE), true,
                 BigDecimal.valueOf(Double.MAX_VALUE), true);
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             if (value != null) {
                 final BigDecimal bd = (BigDecimal) value;
                 if (DOUBLE_RANGE.contains(bd)) {
                     // ehem... nobody really uses large scales, right?
                     encoder.writeValue(bd.doubleValue());
                 } else {
                     // well, it really was a big number...
                     encoder.writeValue(bd.toString());
                 }
             }
         }
     }
 
     /**
      * Value encoder for {@link Float}
      * 
      * @version $Id$
      */
     private static class FloatEncoder extends AttributeEncoder {
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             if (value != null) {
                 encoder.writeValue(((Float) value).floatValue());
             }
         }
     }
 
     /**
      * Value encoder for {@link Long}
      * 
      * @version $Id$
      */
     private static class LongEncoder extends AttributeEncoder {
         private static final Range<Long> BYTE_RANGE = new Range<Long>(Long.class,
                 Long.valueOf(Byte.MIN_VALUE), true, Long.valueOf(Byte.MAX_VALUE), true);
 
         private static final Range<Long> INT_RANGE = new Range<Long>(Long.class,
                 Long.valueOf(Long.MIN_VALUE), true, Long.valueOf(Long.MAX_VALUE), true);
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             if (value != null) {
                 Long lval = (Long) value;
                 long longValue = lval.longValue();
                 if (BYTE_RANGE.contains(longValue)) {
                     encoder.writeValue(lval.byteValue());
                 } else if (INT_RANGE.contains(longValue)) {
                     encoder.writeValue(lval.intValue());
                 } else {
                     encoder.writeValue(longValue);
                 }
             }
         }
     }
 
     /**
      * Value encoder for {@link Integer}
      * 
      * @version $Id$
      */
     private static class IntegerEncoder extends AttributeEncoder {
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             if (value != null) {
                 encoder.writeValue(((Integer) value).intValue());
             }
         }
     }
 
     /**
      * Value encoder for {@link Short}
      * 
      * @version $Id$
      */
     private static class ShortEncoder extends AttributeEncoder {
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             if (value != null) {
                 encoder.writeValue(((Short) value).intValue());
             }
         }
     }
 
     /**
      * Value encoder for {@link Byte}
      * 
      * @version $Id$
      */
     private static class ByteEncoder extends AttributeEncoder {
 
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             if (value != null) {
                 encoder.writeValue(((Byte) value).byteValue());
             }
         }
     }
 
     /**
      * Value encoder for {@link Boolean}
      * 
      * @version $Id$
      */
     private static class BooleanEncoder extends AttributeEncoder {
         @Override
         public void encode(final Gml3Encoder gmlEncoder, final Object value,
                 final AttributeDescriptor descriptor, final BxmlStreamWriter encoder)
                 throws IOException {
             if (value != null) {
                 encoder.writeValue(((Boolean) value).booleanValue());
             }
         }
     }
 
     public static AttributeEncoder getAttributeEncoder(final Class<?> binding) {
         AttributeEncoder attEncoder = encodingBindings.get(binding);
         if (attEncoder == null) {
             attEncoder = UNKNOWN_ATT_TYPE_ENCODER;
         }
         return attEncoder;
     }
 }
