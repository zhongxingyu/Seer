 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.kml;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.geoserver.ows.util.RequestUtils;
 import org.geoserver.platform.GeoServerExtensions;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureIterator;
 import org.geotools.feature.FeatureTypes;
 import org.geotools.feature.type.DateUtil;
 import org.geotools.map.MapLayer;
 import org.geotools.renderer.style.LineStyle2D;
 import org.geotools.renderer.style.MarkStyle2D;
 import org.geotools.renderer.style.PolygonStyle2D;
 import org.geotools.renderer.style.SLDStyleFactory;
 import org.geotools.renderer.style.Style2D;
 import org.geotools.renderer.style.TextStyle2D;
 import org.geotools.styling.ExternalGraphic;
 import org.geotools.styling.FeatureTypeStyle;
 import org.geotools.styling.LineSymbolizer;
 import org.geotools.styling.Mark;
 import org.geotools.styling.PointSymbolizer;
 import org.geotools.styling.PolygonSymbolizer;
 import org.geotools.styling.Rule;
 import org.geotools.styling.SLD;
 import org.geotools.styling.Style;
 import org.geotools.styling.Symbolizer;
 import org.geotools.styling.TextSymbolizer;
 import org.geotools.util.NumberRange;
 import org.geotools.xml.transform.Translator;
 import org.geotools.xs.bindings.XSDateTimeBinding;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.Filter;
 import org.opengis.filter.expression.Expression;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.NameSpaceInfo;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.vfny.geoserver.wms.responses.featureInfo.FeatureTemplate;
 import org.vfny.geoserver.wms.responses.featureInfo.FeatureTimeTemplate;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.LineSegment;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.MultiLineString;
 import com.vividsolutions.jts.geom.MultiPoint;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.geom.Polygon;
 
 
 /**
  * Transforms a feature collection to a kml document consisting of nested
  * "Style" and "Placemark" elements for each feature in the collection.
  * A new transfomer must be instantianted for each feature collection, 
  * the feature collection provided to the translator is supposed to be
  * the one coming out of the MapLayer 
  * <p>
  * Usage:
  * </p>
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  *
  */
 public class KMLVectorTransformer extends KMLTransformerBase {
     /**
      * logger
      */
     static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.kml");
 
     /**
      * Tolerance used to compare doubles for equality
      */
     static final double TOLERANCE = 1e-6;
 
     /**
      * The map context
      */
     protected final WMSMapContext mapContext;
 
     /**
      * The map layer being transformed
      */
     protected final MapLayer mapLayer;
 
     /**
      * The scale denominator.
      *
      * TODO: calcuate a real value based on image size to bbox ratio, as image
      * size has no meanining for KML yet this is a fudge.
      */
     double scaleDenominator = 1;
     NumberRange scaleRange = new NumberRange(scaleDenominator, scaleDenominator);
 
     /**
      * used to create 2d style objects for features
      */
     SLDStyleFactory styleFactory = new SLDStyleFactory();
     
     /**
      * Feature template, cached for performance reasons
      */
     FeatureTemplate template = new FeatureTemplate();
     
     /**
      * list of formats which correspond to the default formats in which 
      * freemarker outputs dates when a user calls the ?datetime(),?date(),?time()
      * fuctions. 
      */
     static List/*<SimpleDateFormat>*/ dtformats = new ArrayList();
     static List/*<SimpleDateFormat>*/ dformats = new ArrayList();
     static List/*<SimpleDateFormat>*/ tformats = new ArrayList();
     static {
         
         //add default freemarker ones first since they are likely to be used 
         // first, the order of this list matters.
         
         dtformats.add( FeatureTemplate.DATETIME_FORMAT );
         addFormats(dtformats,"dd%MM%yy hh:mm:ss" );
         addFormats(dtformats,"MM%dd%yy hh:mm:ss" );
         //addFormats(formats,"yy%MM%dd hh:mm:ss" );
         addFormats(dtformats,"dd%MMM%yy hh:mm:ss" );
         addFormats(dtformats,"MMM%dd%yy hh:mm:ss" );
         //addFormats(formats,"yy%MMM%dd hh:mm:ss" );
         
         addFormats(dtformats,"dd%MM%yy hh:mm" );
         addFormats(dtformats,"MM%dd%yy hh:mm" );
         //addFormats(formats,"yy%MM%dd hh:mm" );
         addFormats(dtformats,"dd%MMM%yy hh:mm" );
         addFormats(dtformats,"MMM%dd%yy hh:mm" );
         //addFormats(formats,"yy%MMM%dd hh:mm" );
         
         dformats.add( FeatureTemplate.DATE_FORMAT );
         addFormats(dformats,"dd%MM%yy" );
         addFormats(dformats,"MM%dd%yy" );
         //addFormats(formats,"yy%MM%dd" );
         addFormats(dformats,"dd%MMM%yy" );
         addFormats(dformats,"MMM%dd%yy" );
         //addFormats(formats,"yy%MMM%dd" );
          
         tformats.add( FeatureTemplate.TIME_FORMAT );
     }
     
     static void addFormats( List formats, String pattern ) {
         
         formats.add( new SimpleDateFormat( pattern.replaceAll("%","-" ) ) );
         formats.add( new SimpleDateFormat( pattern.replaceAll("%","/" ) ) );
         formats.add( new SimpleDateFormat( pattern.replaceAll("%","." ) ) );
         formats.add( new SimpleDateFormat( pattern.replaceAll("%"," " ) ) );
         formats.add( new SimpleDateFormat( pattern.replaceAll("%","," ) ) );
         
     }
     
     public KMLVectorTransformer(WMSMapContext mapContext, MapLayer mapLayer) {
         this.mapContext = mapContext;
         this.mapLayer = mapLayer;
 
         setNamespaceDeclarationEnabled(false);
     }
 
    /**
      * Sets the scale denominator.
      */
     public void setScaleDenominator(double scaleDenominator) {
         this.scaleDenominator = scaleDenominator;
     }
 
     public Translator createTranslator(ContentHandler handler) {
         return new KMLTranslator(handler);
     }
 
     protected class KMLTranslator extends KMLTranslatorSupport {
         /**
          * Store the regionating strategy being applied
          */
         private RegionatingStrategy myStrategy;
 
         /**
          * Geometry transformer
          */
         Translator geometryTranslator;
 
         public KMLTranslator(ContentHandler contentHandler) {
             super(contentHandler);
 
             KMLGeometryTransformer geometryTransformer = new KMLGeometryTransformer();
             //geometryTransformer.setUseDummyZ( true );
             geometryTransformer.setOmitXMLDeclaration(true);
             geometryTransformer.setNamespaceDeclarationEnabled(true);
 
             GeoServer config = mapContext.getRequest().getGeoServer();
             geometryTransformer.setNumDecimals(config.getNumDecimals());
 
             geometryTranslator = geometryTransformer.createTranslator(contentHandler);
         }
 
         public void setRegionatingStrategy(RegionatingStrategy rs){
             myStrategy = rs;
         }
 
         public void encode(Object o) throws IllegalArgumentException {
             FeatureCollection<SimpleFeatureType, SimpleFeature> features = (FeatureCollection) o;
             SimpleFeatureType featureType = features.getSchema();
             Data catalog = mapContext.getRequest().getWMS().getData();
 
             if (isStandAlone()) {
                 start( "kml" );
             }
 
             //start the root document, name it the name of the layer
             start("Document", KMLUtils.attributes(
                     new String[] {"xmlns:atom", "http://purl.org/atom/ns#" }));
             element("name", mapLayer.getTitle());
 
             String relLinks = (String)mapContext.getRequest().getFormatOptions().get("relLinks");
             // Add prev/next links if requested
             if (mapContext.getRequest().getMaxFeatures() != null &&
                 relLinks != null && relLinks.equalsIgnoreCase("true") ){
 
                 String linkbase = "";
                 try {
                     linkbase = getFeatureTypeURL();
                     linkbase += ".kml";
                 } catch (IOException ioe) {
                     throw new RuntimeException(ioe);
                 }
 
                 int maxFeatures = mapContext.getRequest().getMaxFeatures();
                 int startIndex =
                     (mapContext.getRequest().getStartIndex() == null)
                     ? 0 
                     : mapContext.getRequest().getStartIndex().intValue();
                 int prevStart = startIndex - maxFeatures;
                 int nextStart = startIndex + maxFeatures;
 
                 // Previous page, if any
                 if (prevStart >= 0) {
                     String prevLink = linkbase + "?startindex=" 
                         + prevStart + "&maxfeatures=" + maxFeatures;
                     element("atom:link", null, KMLUtils.attributes(new String[] {
                                 "rel", "prev", "href", prevLink }));
                     encodeSequentialNetworkLink(linkbase, prevStart,
                             maxFeatures, "prev", "Previous page");
                 }
 
                 // Next page, if any
                 if (features.size() >= maxFeatures) {
                     String nextLink = linkbase + "?startindex=" + nextStart
                         + "&maxfeatures=" + maxFeatures;
                     element("atom:link", null, KMLUtils.attributes(new String[] {
                                 "rel", "next", "href", nextLink }));
                     encodeSequentialNetworkLink(linkbase, nextStart,
                             maxFeatures, "next", "Next page");
                 }
             }
 
             //get the styles for the layer
             FeatureTypeStyle[] featureTypeStyles = filterFeatureTypeStyles(mapLayer.getStyle(),
                     featureType);
 
             //Determine which strategy to use to regionate the results
             String stratname = (String)mapContext.getRequest().getFormatOptions().get("regionateBy");
             if (("auto").equals(stratname))
                 stratname = catalog.getFeatureTypeInfo(featureType.getName()).getRegionateStrategy();
             List<RegionatingStrategyFactory> factories = GeoServerExtensions.extensions(RegionatingStrategyFactory.class);
             Iterator<RegionatingStrategyFactory> it = factories.iterator();
             while (it.hasNext()){
                 RegionatingStrategyFactory factory = it.next();
                 if (factory.canHandle(stratname)){
                     setRegionatingStrategy(factory.createStrategy());
                     break;
                 }
             }
 
             if (myStrategy == null){
                 setRegionatingStrategy(new SLDRegionatingStrategy());
             }
 
             myStrategy.preProcess(mapContext, mapLayer);
             
             // encode the schemas (kml 2.2)
             encodeSchemas(features);
 
             // encode the layers
             encode(features, featureTypeStyles);
             
             //encode the legend
             //encodeLegendScreenOverlay();
             end("Document");
             
             if ( isStandAlone() ) {
                 end( "kml" );
             }
         }
 
         /**
          * 
          * Encodes a networklink for previous or next document in a sequence
          * 
          * Note that in KML 2.2 atom:link is supported and may be better.
          *
          * @param linkbase the base fore creating URLs
          * @param prevStart previous start value
          * @param maxFeatures maximum number of features to return
          * @param id attribute to use for this NetworkLink
          * @param readableName goes into linkName
          */
         private void encodeSequentialNetworkLink(String linkbase, int prevStart,
                 int maxFeatures, String id, String readableName) {
             String link = linkbase + "?startindex=" + prevStart
                     + "&maxfeatures=" + maxFeatures;
             start("NetworkLink", KMLUtils.attributes(new String[] {"id", id}));
             element("linkName",readableName);
             start("Link");
             element("href",link);
             end("Link");
             end("NetworkLink");
         }
 
         /**
          * Encodes the <Schema> element in kml 2.2
          * @param featureTypeStyles
          */
         protected void encodeSchemas(FeatureCollection<SimpleFeatureType, SimpleFeature> featureTypeStyles) {
             // the code is at the moment in KML3VectorTransformer
         }
 
         protected void encode(FeatureCollection<SimpleFeatureType, SimpleFeature> features,
                 FeatureTypeStyle[] styles) {
            //grab a feader and process
             FeatureIterator<SimpleFeature> reader = features.features();
 
             try {
                 while (reader.hasNext()) {
                     SimpleFeature feature = (SimpleFeature) reader.next();
 
                     try {
                         encode(feature, styles);
                     } catch (RuntimeException t) {
                         // if the stream has been closed by the client don't keep on going forward, this is not
                         // a feature local issue
                         if(t.getCause() instanceof SAXException)
                             throw t;
                         else
                             LOGGER.log(Level.WARNING, "Failure tranforming feature to KML:" + feature.getID(), t);
                     } 
                 }
             } finally {
                 //make sure we always close
                 features.close(reader);
             }
         }
 
         protected void encode(SimpleFeature feature, FeatureTypeStyle[] styles) {
             //get the feature id
 
             if ( myStrategy.include(feature) ) {
                 encodeStyle(feature, styles);
                 encodePlacemark(feature,styles);    
             }
             
 
             //end("Document");
         }
 
         /**
          * Encodes the provided set of rules as KML styles.
          */
         protected boolean encodeStyle(SimpleFeature feature, FeatureTypeStyle[] styles) {
            
             //encode the Line/Poly styles
             List symbolizerList = new ArrayList();
             for ( int j = 0; j < styles.length ; j++ ) {
                Rule[] rules = filterRules(styles[j], feature);
             	
                 for (int i = 0; i < rules.length; i++) {
                     symbolizerList.addAll(Arrays.asList(rules[i].getSymbolizers()));
                 }
             }
             
             if ( !symbolizerList.isEmpty() ) {
                 //start the style
                 start("Style",
                     KMLUtils.attributes(new String[] { "id", "GeoServerStyle" + feature.getID() }));
 
                 Symbolizer[] symbolizers = (Symbolizer[]) symbolizerList.toArray(new Symbolizer[symbolizerList.size()]);
                 encodeStyle(feature, symbolizers);
                 
                 //end the style
                 end("Style");
                 
                 //return true to specify that the feature has a style
                 return true;
             }
             else {
                 //dont encode
                 return false;
             }
 
         }
 
         /**
          * Encodes an IconStyle for a feature.
          */
         protected void encodeDefaultIconStyle(SimpleFeature feature) {
             // encode the style for the icon
 
             // figure out if line or polygon
             boolean line = feature.getDefaultGeometry() != null
                     && (feature.getDefaultGeometry() instanceof LineString || feature
                             .getDefaultGeometry() instanceof MultiLineString);
             boolean poly = feature.getDefaultGeometry() != null
                     && (feature.getDefaultGeometry() instanceof Polygon || feature
                             .getDefaultGeometry() instanceof MultiPolygon);
 
             // Final pre-flight check
             if (!line && !poly) {
                 LOGGER.log(Level.FINER, "Unexpectedly entered encodeDefaultIconStyle() "
                            + "with something that does not have a multipoint geometry.");
                 return;
             }
 
             // start IconStyle
             start("IconStyle");
 
             // make transparent if they didn't ask for attributes
             if (!mapContext.getRequest().getKMattr()) {
                 encodeColor("00ffffff");
             }
 
             // if line or polygon scale the label
             if (line || poly) {
                 element("scale", "0.4");
             }
 
             // start Icon
             start("Icon");
 
             // Note the version number in case we want to replace the icon
             String imageURL = "http://icons.opengeo.org/markers/icon-"
                     + (poly ? "poly.1" : "line.1") + ".png";
             element("href", imageURL);
 
             end("Icon");
 
             // end IconStyle
             end("IconStyle");
 
         }
         /**
          * Encodes the provided set of symbolizers as KML styles.
          */
         protected void encodeStyle(SimpleFeature feature, Symbolizer[] symbolizers) {
             // look for line symbolizers, if there is any, we should tell the
             // polygon style to have an outline
             boolean forceOutline = false;
             for (int i = 0; i < symbolizers.length; i++) {
                 if (symbolizers[i] instanceof LineSymbolizer) {
                     forceOutline = true;
                     break;
                 }
             }
 
             // if this is a polygon or line, it should also be given a point
             boolean lacksPointSymbolizer = true;
             Symbolizer multiPointSymbolizer = null;
 
             // create a 2-D style
             try {
 
                 for (int i = 0; i < symbolizers.length; i++) {
                     Symbolizer symbolizer = symbolizers[i];
                     LOGGER.finer(new StringBuffer("Applying symbolizer ")
                             .append(symbolizer).toString());
 
                     // Used for custom placemark icon, sometimes even the smallest 
                     // placemarks will cover up the underlying polygons.
                     
                     // But ff we pass a PointSymbolizer to something that is essentially
                     // a polygon this throws up, so we catch it?
                     Style2D style = null;
                     try {
                         styleFactory.createStyle(feature, symbolizer, scaleRange);
                     } catch (IllegalArgumentException iae) {
                         // Do nothing
                     }
 
                     // split out each type of symbolizer
                     if (symbolizer instanceof TextSymbolizer) {
                         encodeTextStyle((TextStyle2D) style,
                                 (TextSymbolizer) symbolizer);
                     }
 
                     if (symbolizer instanceof PolygonSymbolizer) {
                         encodePolygonStyle((PolygonStyle2D) style,
                                 (PolygonSymbolizer) symbolizer, forceOutline);
                         multiPointSymbolizer = symbolizer;
                     }
 
                     if (symbolizer instanceof LineSymbolizer) {
                         encodeLineStyle((LineStyle2D) style,
                                 (LineSymbolizer) symbolizer);
                         multiPointSymbolizer = symbolizer;
                     }
 
                     if (symbolizer instanceof PointSymbolizer) {
                         encodePointStyle(style, (PointSymbolizer) symbolizer);
                         lacksPointSymbolizer = false;
                     }
 
                 }
                 
                 // Add a default point symbolizer, so people have something
                 // to click on in the Google Earth
                 if (multiPointSymbolizer != null && lacksPointSymbolizer) {
                     encodeDefaultIconStyle(feature);
                 }
 
             } catch (Exception e) {
                 LOGGER.log(Level.WARNING,
                         "Error occurred during style encoding", e);
             }
         }
 
         /**
          * Encodes a KML IconStyle + PolyStyle from a polygon style and symbolizer.
          */
         protected void encodePolygonStyle(PolygonStyle2D style, PolygonSymbolizer symbolizer, boolean forceOutline) {
             //star the polygon style
             start("PolyStyle");
 
             //fill
             if (symbolizer.getFill() != null) {
                 //get opacity
                 double opacity = SLD.opacity(symbolizer.getFill());
 
                 if (Double.isNaN(opacity)) {
                     //none specified, default to full opacity
                     opacity = 1.0;
                 }
 
                 encodeColor(SLD.color(symbolizer.getFill()), opacity);
             } else {
                 //make it transparent
                 encodeColor("00aaaaaa");
             }
 
             //outline
             if (symbolizer.getStroke() != null || forceOutline) {
                 element("outline", "1");
             } else {
                 element("outline", "0");
             }
 
             end("PolyStyle");
 
             //if stroke specified add line style as well
             if (symbolizer.getStroke() != null) {
                 start("LineStyle");
 
                 //opacity
                 double opacity = SLD.opacity(symbolizer.getStroke());
 
                 if (Double.isNaN(opacity)) {
                     //none specified, default to full opacity
                     opacity = 1.0;
                 }
 
                encodeColor(colorToHex((Color) style.getContour(), opacity));

                 //width
                 int width = SLD.width(symbolizer.getStroke());
 
                 if (width != SLD.NOTFOUND) {
                     element("width", Integer.toString(width));
                 }
 
                 end("LineStyle");
             }
         }
 
         /**
          * Encodes a KML IconStyle + LineStyle from a polygon style and symbolizer.
          */
         protected void encodeLineStyle(LineStyle2D style, LineSymbolizer symbolizer) {
             start("LineStyle");
 
             //stroke
             if (symbolizer.getStroke() != null) {
                 //opacity
                 double opacity = SLD.opacity(symbolizer.getStroke());
 
                 if (Double.isNaN(opacity)) {
                     //default to full opacity
                     opacity = 1.0;
                 }
 
                encodeColor((Color) style.getContour(), opacity);

                 //width
                 int width = SLD.width(symbolizer.getStroke());
 
                 if (width != SLD.NOTFOUND) {
                     element("width", Integer.toString(width));
                 }
             } else {
                 //default
                 encodeColor("ffaaaaaa");
                 element("width", "1");
             }
 
             end("LineStyle");
         }
 
         /**
          * Encodes a KML IconStyle from a point style and symbolizer.
          */
         protected void encodePointStyle(Style2D style, PointSymbolizer symbolizer) {
             start("IconStyle");
 
             if (style instanceof MarkStyle2D) {
                 Mark mark = SLD.mark(symbolizer);
 
                 if (mark != null) {
                     double opacity = SLD.opacity(mark.getFill());
 
                     if (Double.isNaN(opacity)) {
                         //default to full opacity
                         opacity = 1.0;
                     }
 
                     if(mark.getFill() != null) {
                         final Color color = SLD.color(mark.getFill());
                         encodeColor(color, opacity);
                     }
                 } 
             } 
 
             element("colorMode", "normal");
 
             // placemark icon
             String iconHref = null;
 
             //if the point symbolizer uses an external graphic use it
             if ((symbolizer.getGraphic() != null)
                     && (symbolizer.getGraphic().getExternalGraphics() != null)
                     && (symbolizer.getGraphic().getExternalGraphics().length > 0)) {
                 ExternalGraphic graphic = symbolizer.getGraphic().getExternalGraphics()[0];
 
                 try {
                     if ("file".equals(graphic.getLocation().getProtocol())) {
                         //it is a local file, reference locally from "styles" directory
                         File file = new File(graphic.getLocation().getFile());
                         iconHref = RequestUtils.baseURL(mapContext.getRequest()
                                                                   .getHttpServletRequest())
                             + "styles/" + file.getName();
                     } else if ( "http".equals(graphic.getLocation().getProtocol()) ) {
                         iconHref = graphic.getLocation().toString();
                     } else {
                         // TODO: should we check for http:// and use it directly?
                         //other protocols?
                      }
 
                 } catch (Exception e) {
                     LOGGER.log(Level.WARNING, "Error processing external graphic:" + graphic, e);
                 }
             }
 
             if (iconHref == null) {
                 iconHref = "http://maps.google.com/mapfiles/kml/pal4/icon25.png";
             }
 
             start("Icon");
 
             element("href", iconHref);
             end("Icon");
 
             end("IconStyle");
         }
 
         /**
          * Encodes a KML LabelStyle from a text style and symbolizer.
          */
         protected void encodeTextStyle(TextStyle2D style, TextSymbolizer symbolizer) {
             start("LabelStyle");
 
             if (symbolizer.getFill() != null) {
                 double opacity = SLD.opacity(symbolizer.getFill());
 
                 if (Double.isNaN(opacity)) {
                     //default to full opacity
                     opacity = 1.0;
                 }
 
                 encodeColor(SLD.color(symbolizer.getFill()), opacity);
             } else {
                 //default
                 encodeColor("ffffffff");
             }
 
             end("LabelStyle");
         }
 
         /**
          * Encodes a KML Placemark from a feature and optional name.
          */
         protected void encodePlacemark(SimpleFeature feature, FeatureTypeStyle[] styles) {
             Geometry geometry = featureGeometry(feature);
             Coordinate centroid = geometryCentroid(geometry);
 
             start("Placemark", KMLUtils.attributes(new String[] { "id", feature.getID() }));
 
             //encode name + description only if kmattr was specified
             if (mapContext.getRequest().getKMattr()) {
                 //name
                 try {
                         encodePlacemarkName( feature, styles );
                 }
                 catch( Exception e ) {
                         String msg = "Error occured processing 'title' template.";
                         LOGGER.log( Level.WARNING, msg, e );
                 }
                 
                 // snippet (only used by OWS5 prototype at the moment)
                 try {
                     encodePlacemarkSnippet(feature, styles);
                 } catch (Exception e) {
                         String msg = "Error occured processing 'description' template.";
                         LOGGER.log( Level.WARNING, msg, e );
                 }
                 
                 //description
                 try {
                     encodePlacemarkDescription(feature, styles);
                 } catch (Exception e) {
                         String msg = "Error occured processing 'description' template.";
                         LOGGER.log( Level.WARNING, msg, e );
                 }
             }
            
             String selfLinks = (String)mapContext.getRequest().getFormatOptions().get("selfLinks");
             if (selfLinks != null && selfLinks.equalsIgnoreCase("true")){
                 GetMapRequest request = mapContext.getRequest(); 
                 String link = "";
 
                 try {
                     link = getFeatureTypeURL();
                 } catch (IOException ioe) {
                     /* what could *possibly* go wrong? */
                     throw new RuntimeException(ioe);
                 }
                 String[] id = feature.getID().split("\\.");
 
                 link = link + "/" + id[1] + ".kml";
 
                 element("atom:link", null, KMLUtils.attributes(new String[] {
                             "rel", "self", "href", link }));
             }
             
             //look at
             encodePlacemarkLookAt(centroid);
             
             //time
             try {
                 encodePlacemarkTime(feature, styles);
             } catch (Exception e) {
                 String msg = "Error occured processing 'time' template: " +  e.getMessage();
                 LOGGER.log( Level.WARNING, msg );
                 LOGGER.log( Level.FINE, "", e );
             }
             
             //style reference
             element("styleUrl", "#GeoServerStyle" + feature.getID());
             
             // encode extended data (kml 2.2)
             encodeExtendedData(feature);
 
             //geometry
             encodePlacemarkGeometry(geometry, centroid, styles);
 
             end("Placemark");
         }
 
         /**
          * Encodes kml 2.2 extended data section
          * @param feature
          */
         protected void encodeExtendedData(SimpleFeature feature) {
             // code at the moment is in KML3VectorTransfomer
         }
         
         private String getFeatureTypeURL() throws IOException {
             String nsUri = mapLayer.getFeatureSource().getSchema().getName().getNamespaceURI();
             NameSpaceInfo ns = mapContext.getRequest().getWMS().getData().getNameSpaceFromURI(nsUri);
             String featureTypeName = mapLayer.getFeatureSource().getSchema().getName().getLocalPart();
             GetMapRequest request = mapContext.getRequest();
             
             //TODO The old code, commented out below, results in
             // link = http://localhost:8080/geoserver/rest/geosearch/topp/states.kml?st
             // due to getBaseUrl()
             //
             //String link = RequestUtils.proxifiedBaseURL(request.getBaseUrl(),
             //        request.getGeoServer().getProxyBaseUrl());
             //
 
             // If you prefer pretty code, this is a good point to close your eyes:
             String baseUrl = request.getHttpServletRequest().getRequestURL().toString();
             int searchIdx = baseUrl.indexOf("rest/geosearch");
             if(searchIdx < 0) {
 //              LOGGER.log(Level.WARNING, "Unable to find rest/geosearch in URL " + baseUrl);
             } else {
                 baseUrl = baseUrl.substring(0,searchIdx);
             }
             baseUrl = RequestUtils.proxifiedBaseURL(baseUrl,
                     request.getGeoServer().getProxyBaseUrl());
                     
             return baseUrl + "rest/geosearch/" + ns.getPrefix()
                     + "/" + featureTypeName;
         }
 
 
 
         /**
          * Encodes a KML Placemark name from a feature by processing a
          * template.
          */
         protected void encodePlacemarkName(SimpleFeature feature, FeatureTypeStyle[] styles )
                 throws IOException {
                 
             //order to use when figuring out what the name / label of a 
             // placemark should be:
             // 1. the title template for features
             // 2. a text sym with a label from teh sld
             // 3. nothing ( do not use fid )
 
             String title = template.title( feature );       
             
             //ensure not empty and != fid
             if ( title == null || "".equals( title ) || feature.getID().equals( title ) ) {
                 //try sld
                 StringBuffer label = new StringBuffer();
                 for ( int i = 0; i < styles.length; i++ ) {
                         Rule[] rules = filterRules(styles[i], feature );
                         for ( int j = 0; j < rules.length; j++ ) {
                                 Symbolizer[] syms = rules[j].getSymbolizers();
                                 for ( int k = 0; k < syms.length; k++) {
                                         if ( syms[k] instanceof TextSymbolizer ) {
                                                 Expression e = SLD.textLabel((TextSymbolizer) syms[k]);
                         Object object = null;
                         if(e != null)
                             object = e.evaluate(feature);
                         String value = null;
 
                         if (object instanceof String) {
                             value = (String) object;
                         } else {
                             if (object != null) {
                                 value = object.toString();
                             }
                         }
 
                         if ((value != null) && !"".equals(value.trim())) {
                             label.append(value);
                         }
                                         }
                                 }
                         }
                 }
                 
                 if ( label.length() > 0 ) {
                         title = label.toString();
                 }
                 else {
                         title = null;
                 }
             
             }
             
             if ( title != null ) {
                 start("name");
                 cdata(title);
                 end("name");    
             }
             
         }
         
         /**
          * Encodes a KML Placemark description from a feature 
          */
         protected void encodePlacemarkDescription(SimpleFeature feature, FeatureTypeStyle[] styles)
             throws IOException {
         
            String description = template.description( feature );
          
             if (description != null) {
                 start("description");
                 cdata(description);
                 end("description");
             }
         }
         
         /**
          * Encodes the Snipped element
          * @param feature
          * @param styles
          */
         protected void encodePlacemarkSnippet(SimpleFeature feature, FeatureTypeStyle[] styles) {
             // does nothing at the moment
         }
 
         /**
          * Encods a KML Placemark LookAt from a geometry + centroid.
          */
         protected void encodePlacemarkLookAt(Coordinate centroid) {
             start("LookAt");
 
             element("longitude", Double.toString(centroid.x));
             element("latitude", Double.toString(centroid.y));
             element("range", "700");
             element("tilt", "10.0");
             element("heading", "10.0");
 
             end("LookAt");
         }
         
         /**
          * Encodes a KML TimePrimitive geometry from a feature.
          */
         protected void encodePlacemarkTime(SimpleFeature feature, FeatureTypeStyle[] styles) throws IOException {
             try {
                 String[] time = new FeatureTimeTemplate(template).execute(feature);
                 if ( time.length == 0 ) {
                     return;
                 }
                 
                 if ( time.length == 1 ) {
                     encodeKmlTimeStamp(parseDateTime(time[0]));
                     
                 } else {
                     encodeKmlTimeSpan(parseDateTime(time[0]), parseDateTime(time[1]));
                 }
             } catch (Exception e) {
                 throw (IOException) new IOException().initCause(e);
             }
         }
 
         /**
          * Encodes the time pairs into a kml TimeSpan (from and to will be parsed
          * into the official kml date/time representation)
          * @param from
          * @param to
          * @throws Exception
          */
         protected void encodeKmlTimeSpan(Date from, Date to) throws Exception {
             //timespan case
             String begin = encodeDateTime(from);
             String end = encodeDateTime(to);
             
             if (!(begin == null && end == null)) {
                 start("TimeSpan");    
                 if ( begin != null ) {
                     element("begin", begin);
                 }
                 if ( end != null ) {
                     element("end", end);
                 }    
                 end("TimeSpan");
             }
         }
 
         /**
          * Encodes a kml Timestamp element with provided time (which will be parsed into
          * the standard kml representation)
          * @param time
          * @throws Exception
          */
         protected void encodeKmlTimeStamp(Date time) throws Exception {
             String datetime = encodeDateTime(time);
             if ( datetime != null ) {
                 //timestamp case
                 start("TimeStamp");
                 element("when", datetime );
                 end("TimeStamp");    
             }
         }
         
         protected String encodeDateTime(Date date) {
             if(date != null) {
                 Calendar c = Calendar.getInstance();
                 c.setTime(date);
                 return new XSDateTimeBinding().encode(  c , null );
             } else {
                 return null;
             }
         }
 
         /**
          * Encodes a date as an xs:dateTime.
          */
         protected Date parseDateTime( String date ) throws Exception {
             
             //first try as date time
             Date d = parseDate( dtformats, date );
             if ( d == null ) {
                 //then try as date
                 d = parseDate( dformats, date );    
             }
             if ( d == null ) {
                 //try as time
                 d = parseDate( tformats, date );
             }
             
             if ( d == null ) {
                 //last ditch effort, try to parse as xml dates
                 try {
                     //try as xml date time
                     d = DateUtil.deserializeDateTime( date );
                 }
                 catch( Exception e1 ) {
                     try {
                         //try as xml date
                         d = DateUtil.deserializeDate( date );
                     }
                     catch( Exception e2 ) {}
                 }
             }
            
             if ( d != null ) {
                 return d;
             }
             
             LOGGER.warning("Could not parse date: " + date);
             return null;
         }
         
         /**
          * Parses a date as a string into a well-known format.
          */
         protected Date parseDate( List formats, String date ) {
             for ( Iterator f = formats.iterator(); f.hasNext(); ) {
                 SimpleDateFormat format = (SimpleDateFormat) f.next();
                 Date d = null;
                 try {
                     d = format.parse(date);
                 } catch (ParseException e) {}
                 
                 if ( d != null ) {
                     return d;
                 }
             }
             
             return null;
         }
         /**
          * Encodes a KML Placemark geometry from a geometry + centroid.
          * @param styles 
          */
         protected void encodePlacemarkGeometry(Geometry geometry, Coordinate centroid, FeatureTypeStyle[] styles) {
             //if point, just encode a single point, otherwise encode the geometry
             // + centroid
             if ( geometry instanceof Point || 
                     (geometry instanceof MultiPoint) && ((MultiPoint)geometry).getNumPoints() == 1 ) {
                 encodeGeometry( geometry, styles );
             }
             else {
                 start("MultiGeometry");
 
                 //the centroid
                 start("Point");
 
                 if (!Double.isNaN(centroid.z)) {
                     element("coordinates", centroid.x + "," + centroid.y + "," + centroid.z);
                 } else {
                     element("coordinates", centroid.x + "," + centroid.y);
                 }
 
                 end("Point");
 
                 //the actual geometry
                 encodeGeometry(geometry, styles);
 
                 end("MultiGeometry");
             }
             
         }
 
         /**
          * Encodes a KML geometry.
          * @param styles 
          */
         protected void encodeGeometry(Geometry geometry, FeatureTypeStyle[] styles) {
             if (geometry instanceof GeometryCollection) {
                 //unwrap the collection
                 GeometryCollection collection = (GeometryCollection) geometry;
 
                 for (int i = 0; i < collection.getNumGeometries(); i++) {
                     encodeGeometry(collection.getGeometryN(i), styles);
                 }
             } else {
                 geometryTranslator.encode(geometry);
             }
         }
 
         /**
          * Encodes a color element from its color + opacity representation.
          *
          * @param color The color to encode.
          * @param opacity The opacity ( alpha ) of the color.
          */
         void encodeColor(Color color, double opacity) {
             encodeColor(colorToHex(color, opacity));
         }
 
         /**
          * Encodes a color element from its hex representation.
          *
          * @param hex The hex value ( with alpha ) of the color.
          *
          */
         void encodeColor(String hex) {
             element("color", hex);
         }
 
        /**
         * Checks if a rule can be triggered at the current scale level
         * 
         * @param r
         *            The rule
         * @return true if the scale is compatible with the rule settings
         */
         boolean isWithInScale(Rule r) {
                return ((r.getMinScaleDenominator() - TOLERANCE) <= scaleDenominator)
                    && ((r.getMaxScaleDenominator() + TOLERANCE) > scaleDenominator);
        }
 
         /**
          * Returns the id of the feature removing special characters like
          * '&','>','<','%'.
          */
         String featureId(SimpleFeature feature) {
             String id = feature.getID();
             id = id.replaceAll("&", "");
             id = id.replaceAll(">", "");
             id = id.replaceAll("<", "");
             id = id.replaceAll("%", "");
 
             return id;
         }
 
         /**
          * Rreturns the geometry for the feature reprojecting if necessary.
          */
         Geometry featureGeometry(SimpleFeature f) {
             // get the geometry
             Geometry geom = (Geometry) f.getDefaultGeometry();
 
             //rprojection done in KMLTransformer
 //            if (!CRS.equalsIgnoreMetadata(sourceCrs, mapContext.getCoordinateReferenceSystem())) {
 //                try {
 //                    MathTransform transform = CRS.findMathTransform(sourceCrs,
 //                            mapContext.getCoordinateReferenceSystem(), true);
 //                    geom = JTS.transform(geom, transform);
 //                } catch (MismatchedDimensionException e) {
 //                    LOGGER.severe(e.getLocalizedMessage());
 //                } catch (TransformException e) {
 //                    LOGGER.severe(e.getLocalizedMessage());
 //                } catch (FactoryException e) {
 //                    LOGGER.severe(e.getLocalizedMessage());
 //                }
 //            }
 
             return geom;
         }
 
         /**
          * Returns the centroid of the geometry, handling  a geometry collection.
          * <p>
          * In the case of a collection a multi point containing the centroid of
          * each geometry in the collection is calculated. The first point in
          * the multi point is returned as the cetnroid.
          * </p>
          */
         Coordinate geometryCentroid(Geometry g) {
             //TODO: should the collecftion case return the centroid of hte 
             // multi point?
             if (g instanceof GeometryCollection) {
                 GeometryCollection gc = (GeometryCollection) g;
                 
                 //check for case of single geometry
                 if ( gc.getNumGeometries() == 1 ) {
                         g = gc.getGeometryN(0);
                 }
                 else {
                         Coordinate[] pts = new Coordinate[gc.getNumGeometries()];
 
                     for (int t = 0; t < gc.getNumGeometries(); t++) {
                         pts[t] = gc.getGeometryN(t).getCentroid().getCoordinate();
                     }
 
                     return g.getFactory().createMultiPoint(pts).getCoordinates()[0];
                 }
             } 
             
             if ( g instanceof Point ) {
                 //thats easy
                 return g.getCoordinate();
             }
             else if ( g instanceof LineString ) {
                 //make sure the point we return is actually on the line
                 double tol = 1E-6;
                 double mid = g.getLength() / 2d;
                 
                 Coordinate[] coords = g.getCoordinates();
                 
                 //walk along the linestring until we get to a point where we 
                 // have two coordinates that straddle the midpoint
                 double len = 0d;
                 for ( int i = 1; i < coords.length; i++) {
                         LineSegment line = new LineSegment( coords[i-1],coords[i] );
                         len += line.getLength();
                         
                         if ( Math.abs( len - mid ) < tol ) {
                                 //close enough
                                 return line.getCoordinate(1);
                         }
                         
                         if ( len > mid ) {
                                 //we have gone past midpoint
                                 return line.pointAlong( 1 - ((len-mid)/line.getLength()) );
                         }
                 }
                 
                 //should never get there
                 return g.getCentroid().getCoordinate();
             }
             else {
                 //return the actual centroid
                 return g.getCentroid().getCoordinate();
             }
         }
 
         /**
          * Utility method to convert an int into hex, padded to two characters.
          * handy for generating colour strings.
          *
          * @param i Int to convert
          * @return String a two character hex representation of i
          * NOTE: this is a utility method and should be put somewhere more useful.
          */
         String intToHex(int i) {
             String prelim = Integer.toHexString(i);
 
             if (prelim.length() < 2) {
                 prelim = "0" + prelim;
             }
 
             return prelim;
         }
 
         /**
          * Utility method to convert a Color and opacity (0,1.0) into a KML
          * color ref.
          *
          * @param c The color to convert.
          * @param opacity Opacity / alpha, double from 0 to 1.0.
          *
          * @return A String of the form "#AABBGGRR".
          */
         String colorToHex(Color c, double opacity) {
             return new StringBuffer().append(intToHex(new Float(255 * opacity).intValue()))
                                      .append(intToHex(c.getBlue())).append(intToHex(c.getGreen()))
                                      .append(intToHex(c.getRed())).toString();
         }
 
         /**
          * Filters the feature type styles of <code>style</code> returning only
          * those that apply to <code>featureType</code>
          * <p>
          * This methods returns feature types for which
          * <code>featureTypeStyle.getFeatureTypeName()</code> matches the name
          * of the feature type of <code>featureType</code>, or matches the name of
          * any parent type of the feature type of <code>featureType</code>. This
          * method returns an empty array in the case of which no rules match.
          * </p>
          * @param style The style containing the feature type styles.
          * @param featureType The feature type being filtered against.
          *
          */
         protected FeatureTypeStyle[] filterFeatureTypeStyles(Style style, SimpleFeatureType featureType) {
             FeatureTypeStyle[] featureTypeStyles = style.getFeatureTypeStyles();
 
             if ((featureTypeStyles == null) || (featureTypeStyles.length == 0)) {
                 return new FeatureTypeStyle[0];
             }
 
             ArrayList filtered = new ArrayList(featureTypeStyles.length);
 
             for (int i = 0; i < featureTypeStyles.length; i++) {
                 FeatureTypeStyle featureTypeStyle = featureTypeStyles[i];
                 String featureTypeName = featureTypeStyle.getFeatureTypeName();
 
                 //does this style have any rules
                 if (featureTypeStyle.getRules() == null || featureTypeStyle.getRules().length == 0 ) {
                        continue;
                 }
 
                 //does this style apply to the feature collection
                 if (featureType.getTypeName().equalsIgnoreCase(featureTypeName)
                         || FeatureTypes.isDecendedFrom(featureType, null, featureTypeName)) {
                     filtered.add(featureTypeStyle);
                 }
             }
 
             return (FeatureTypeStyle[]) filtered.toArray(new FeatureTypeStyle[filtered.size()]);
         }
 
         /**
          * Filters the rules of <code>featureTypeStyle</code> returnting only
          * those that apply to <code>feature</code>.
          * <p>
          * This method returns rules for which:
          * <ol>
          *  <li><code>rule.getFilter()</code> matches <code>feature</code>, or:
          *  <li>the rule defines an "ElseFilter", and the feature matches no
          *  other rules.
          * </ol>
          * This method returns an empty array in the case of which no rules
          * match.
          * </p>
          * @param featureTypeStyle The feature type style containing the rules.
          * @param feature The feature being filtered against.
          *
          */
         Rule[] filterRules(FeatureTypeStyle featureTypeStyle, SimpleFeature feature) {
             Rule[] rules = featureTypeStyle.getRules();
 
             if ((rules == null) || (rules.length == 0)) {
                 return new Rule[0];
             }
 
             ArrayList filtered = new ArrayList(rules.length);
 
             //process the rules, keep track of the need to apply an else filters
             boolean match = false;
             boolean hasElseFilter = false;
 
             for (int i = 0; i < rules.length; i++) {
                 Rule rule = rules[i];
                 LOGGER.finer(new StringBuffer("Applying rule: ").append(rule.toString()).toString());
 
                 //does this rule have an else filter
                 if (rule.hasElseFilter()) {
                     hasElseFilter = true;
 
                     continue;
                 }
                 
                 //is this rule within scale?
                 if ( !isWithInScale(rule)) {
                         continue;
                 }
 
                 //does this rule have a filter which applies to the feature
                 Filter filter = rule.getFilter();
 
                 if ((filter == null) || filter.evaluate(feature)) {
                     match = true;
 
                     filtered.add(rule);
                 }
             }
 
             //if no rules mached the feautre, re-run through the rules applying
             // any else filters
             if (!match && hasElseFilter) {
                 //loop through again and apply all the else rules
                 for (int i = 0; i < rules.length; i++) {
                     Rule rule = rules[i];
                     
                     //is this rule within scale?
                     if ( !isWithInScale(rule)) {
                             continue;
                     }
 
                     if (rule.hasElseFilter()) {
                         filtered.add(rule);
                     }
                 }
             }
 
             return (Rule[]) filtered.toArray(new Rule[filtered.size()]);
         }
     }
 
     
 }
