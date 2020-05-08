 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses.map.kml;
 
 import com.vividsolutions.jts.geom.Envelope;
 import org.geotools.data.DataUtilities;
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.Query;
 import org.geotools.factory.Hints;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.GeometryAttributeType;
 import org.geotools.filter.BBoxExpression;
 import org.geotools.filter.Expression;
 import org.geotools.filter.Filter;
 import org.geotools.filter.FilterFactory;
 import org.geotools.filter.FilterFactoryFinder;
 import org.geotools.filter.FilterFactoryImpl;
 import org.geotools.filter.GeometryFilter;
 import org.geotools.filter.IllegalFilterException;
 import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.image.ImageWorker;
 import org.geotools.map.MapContext;
 import org.geotools.map.MapLayer;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.FactoryFinder;
 import org.geotools.renderer.lite.RendererUtilities;
 import org.geotools.renderer.lite.StreamingRenderer;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.CoordinateOperationFactory;
 import org.vfny.geoserver.wms.WMSMapContext;
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import javax.imageio.stream.MemoryCacheImageOutputStream;
 import javax.media.jai.GraphicsJAI;
 
 
 /**
  *
  */
 public class EncodeKML {
     /** Standard Logger */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.responses.wms.map.kml");
 
     /** Filter factory for creating bounding box filters */
     //private FilterFactory filterFactory = FilterFactoryFinder.createFilterFactory();
 
     /** the XML and KML header */
     private static final String KML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\t"
         + "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n";
 
     /** the KML closing element */
     private static final String KML_FOOTER = "</kml>\n";
 
    static {
        Hints hints = new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
    }

     /**
      * Map context document - layers, styles aoi etc.
      *
      * @uml.property name="mapContext"
      * @uml.associationEnd multiplicity="(1 1)"
      */
     private WMSMapContext mapContext;
 
     /**
      * Actualy writes the KML out
      *
      * @uml.property name="writer"
      * @uml.associationEnd multiplicity="(0 1)"
      */
     private KMLWriter writer;
 
     /** Filter factory for creating bounding box filters */
     private FilterFactory filterFactory = FilterFactoryFinder.createFilterFactory();
 
     /** Flag to be monotored by writer loops */
     private boolean abortProcess;
 
     /**
      * Creates a new EncodeKML object.
      *
      * @param mapContext A full description of the map to be encoded.
      */
     public EncodeKML(WMSMapContext mapContext) {
         this.mapContext = mapContext;
     }
 
     /**
      * Sets the abort flag.  Active encoding may be halted, but this is not garanteed.
      */
     public void abort() {
         abortProcess = true;
     }
 
     /**
      * Perform the actual encoding.  May return early if abort it called.
      *
      * @param out Ouput stream to send the data to.
      *
      * @throws IOException Thrown if anything goes wrong whilst writing
      */
     public void encodeKML(final OutputStream out) throws IOException {
         this.writer = new KMLWriter(out, mapContext);
         //once KML supports bbox queries against WMS this can be used to 
         //decimate the geometries based on zoom level.
         //writer.setMinCoordDistance(env.getWidth() / 1000);
         abortProcess = false;
 
         long t = System.currentTimeMillis();
 
         try {
             writeHeader();
 
             ArrayList layerRenderList = new ArrayList(); // not used in straight KML generation
             writeLayers(false, layerRenderList);
             writeFooter();
 
             this.writer.flush();
             t = System.currentTimeMillis() - t;
             LOGGER.fine(new StringBuffer("KML generated, it took").append(t).append(" ms").toString());
         } catch (IOException ioe) {
             if (abortProcess) {
                 LOGGER.fine("KML encoding aborted");
 
                 return;
             } else {
                 throw ioe;
             }
         } catch (AbortedException ex) {
             return;
         }
     }
 
     /**
      * This method is used to encode kml + images and put all the stuff into a KMZ
      * file.
      *
      * @param out the response is a Zipped output stream
      * @throws IOException
      */
     public void encodeKMZ(final ZipOutputStream out) throws IOException {
         this.writer = new KMLWriter(out, mapContext);
 
         abortProcess = false;
 
         long t = System.currentTimeMillis();
 
         try {
             // first we produce the KML file containing the code and the PlaceMarks
             final ZipEntry e = new ZipEntry("wms.kml");
             out.putNextEntry(e);
             writeHeader();
 
             ArrayList layerRenderList = new ArrayList();
             writeLayers(true, layerRenderList);
             writeFooter();
             this.writer.flush();
             out.closeEntry();
 
             // then we produce and store all the layer images
             writeImages(out, layerRenderList);
 
             t = System.currentTimeMillis() - t;
             LOGGER.fine(new StringBuffer("KMZ generated, it took").append(t).append(" ms").toString());
         } catch (IOException ioe) {
             if (abortProcess) {
                 LOGGER.fine("KMZ encoding aborted");
 
                 return;
             } else {
                 throw ioe;
             }
         } catch (AbortedException ex) {
             return;
         }
     }
 
     /**
      * Determines whether to return a vector (KML) result of the data or to
      * return an image instead.
      * If the kmscore is 100, then the output should always be vector. If
      * the kmscore is 0, it should always be raster. In between, the number of
      * features is weighed against the kmscore value.
      * kmscore determines whether to return the features as vectors, or as one
      * raster image. It is the point, determined by the user, where X number of
      * features is "too many" and the result should be returned as an image instead.
      *
      * kmscore is logarithmic. The higher the value, the more features it takes
      * to make the algorithm return an image. The lower the kmscore, the fewer
      * features it takes to force an image to be returned.
      * (in use, the formula is exponential: as you increase the KMScore value,
      * the number of features required increases exponentially).
      *
      * @param kmscore the score, between 0 and 100, use to determine what output to use
      * @param numFeatures how many features are being rendered
      * @return true: use just kml vectors, false: use raster result
      */
     private boolean useVectorOutput(int kmscore, int numFeatures) {
         if (kmscore == 100) {
             return true; // vector KML
         }
 
         if (kmscore == 0) {
             return false; // raster KMZ
         }
 
         // For numbers in between, determine exponentionally based on kmscore value:
         // 10^(kmscore/15)
         // This results in exponential growth.
         // The lowest bound is 1 feature and the highest bound is 3.98 million features
         // The most useful kmscore values are between 20 and 70 (21 and 46000 features respectively)
         // A good default kmscore value is around 40 (464 features)
         double magic = Math.pow(10, kmscore / 15);
 
         if (numFeatures > magic) {
             return false; // return raster
         } else {
             return true; // return vector
         }
     }
 
     /**
     * writes out standard KML header
     *
     * @throws IOException
     */
     private void writeHeader() throws IOException {
         writer.write(KML_HEADER);
     }
 
     /**
      * writes out standard KML footer
      *
      * @throws IOException DOCUMENT ME!
      */
     private void writeFooter() throws IOException {
         writer.write(KML_FOOTER);
     }
 
     /**
      * Processes each of the layers within the current mapContext in turn.
      *
      * writeLayers must be called before writeImages in order for the kmScore
      * algorithm to work.
      *
      * @throws IOException
      * @throws AbortedException
      *
      */
     private void writeLayers(final boolean kmz, ArrayList layerRenderList)
         throws IOException, AbortedException {
         MapLayer[] layers = mapContext.getLayers();
         int nLayers = layers.length;
 
         FilterFactory fFac = new FilterFactoryImpl();
 
         final int imageWidth = this.mapContext.getMapWidth();
         final int imageHeight = this.mapContext.getMapHeight();
         //final CoordinateReferenceSystem requestedCrs = mapContext.getCoordinateReferenceSystem();
         //writer.setRequestedCRS(requestedCrs);
         //writer.setScreenSize(new Rectangle(imageWidth, imageHeight));
         writer.startDocument("GeoServer", null);
 
         for (int i = 0; i < nLayers; i++) { // for every layer specified in the request
 
             MapLayer layer = layers[i];
 
             //FeatureReader featureReader = null;
             FeatureSource fSource = layer.getFeatureSource();
             FeatureType schema = fSource.getSchema();
 
             //GeometryAttributeType geometryAttribute = schema.getDefaultGeometry();
             //CoordinateReferenceSystem sourceCrs = geometryAttribute.getCoordinateSystem();
             Rectangle paintArea = new Rectangle(imageWidth, imageHeight);
             AffineTransform worldToScreen = RendererUtilities.worldToScreenTransform(mapContext
                     .getAreaOfInterest(), paintArea);
             double scaleDenominator = 1;
 
             try {
                 scaleDenominator = RendererUtilities.calculateScale(mapContext.getAreaOfInterest(),
                         mapContext.getCoordinateReferenceSystem(), paintArea.width,
                         paintArea.height, 90); // 90 = OGC standard DPI (see SLD spec page 37)
             } catch (Exception e) // probably either (1) no CRS (2) error xforming
              {
                 scaleDenominator = 1 / worldToScreen.getScaleX(); //DJB old method - the best we can do
             }
 
             writer.setRequestedScale(scaleDenominator);
 
             String[] attributes;
             boolean isRaster = false;
 
             AttributeType[] ats = schema.getAttributeTypes();
             final int length = ats.length;
             attributes = new String[length];
 
             for (int t = 0; t < length; t++) {
                 attributes[t] = ats[t].getName();
 
                 if (attributes[t].equals("grid")) {
                     isRaster = true;
                 }
             }
 
             try {
                 CoordinateReferenceSystem sourceCrs = schema.getDefaultGeometry()
                                                             .getCoordinateSystem();
                 writer.setSourceCrs(sourceCrs); // it seems to work better getting it from the schema, here
 
                 Envelope envelope = mapContext.getAreaOfInterest();
                 ReferencedEnvelope aoi = new ReferencedEnvelope(envelope,
                         mapContext.getCoordinateReferenceSystem());
 
                 Filter filter = null;
 
                 //ReferencedEnvelope aoi = mapContext.getAreaOfInterest();
                 if (!CRS.equalsIgnoreMetadata(aoi.getCoordinateReferenceSystem(),
                             schema.getDefaultGeometry().getCoordinateSystem())) {
                     aoi = aoi.transform(schema.getDefaultGeometry().getCoordinateSystem(), true);
                 }
 
                 BBoxExpression rightBBox = filterFactory.createBBoxExpression(aoi);
                 filter = createBBoxFilters(schema, attributes, rightBBox);
 
                 // now build the query using only the attributes and the bounding
                 // box needed
                 DefaultQuery q = new DefaultQuery(schema.getTypeName());
                 q.setFilter(filter);
                 q.setPropertyNames(attributes);
 
                 // now, if a definition query has been established for this layer, be
                 // sure to respect it by combining it with the bounding box one.
                 Query definitionQuery = layer.getQuery();
 
                 if (definitionQuery != Query.ALL) {
                     if (q == Query.ALL) {
                         q = (DefaultQuery) definitionQuery;
                     } else {
                         q = (DefaultQuery) DataUtilities.mixQueries(definitionQuery, q, "KMLEncoder");
                     }
                 }
 
                 q.setCoordinateSystem(layer.getFeatureSource().getSchema().getDefaultGeometry()
                                            .getCoordinateSystem());
 
                 FeatureCollection fc = fSource.getFeatures(q);
 
                 int kmscore = mapContext.getRequest().getKMScore(); //KMZ score value
                 boolean useVector = useVectorOutput(kmscore, fc.size()); // kmscore = render vector/raster
 
                 if (useVector || !kmz) {
                     LOGGER.info("Layer (" + layer.getTitle() + ") rendered with KML vector output.");
                     layerRenderList.add(new Integer(i)); // save layer number so it won't be rendered
 
                     if (!isRaster) {
                         writer.writeFeaturesAsVectors(fc, layer); // vector
                     } else {
                         writer.writeCoverages(fc, layer); // coverage
                     }
                 } else {
                     // user requested KMZ and kmscore says render raster
                     LOGGER.info("Layer (" + layer.getTitle() + ") rendered with KMZ raster output.");
                     // layer order is only needed for raster results. In the <GroundOverlay> tag 
                     // you need to point to a raster image, this image has the layer number as
                     // part of the name. The kml will then reference the image via the layer number
                     writer.writeFeaturesAsRaster(fc, layer, i); // raster
                 }
 
                 LOGGER.fine("finished writing");
             } catch (IOException ex) {
                 LOGGER.info(new StringBuffer("process failed: ").append(ex.getMessage()).toString());
                 throw ex;
             } catch (AbortedException ae) {
                 LOGGER.info(new StringBuffer("process aborted: ").append(ae.getMessage()).toString());
                 throw ae;
             } catch (Throwable t) {
                 LOGGER.warning(new StringBuffer("UNCAUGHT exception: ").append(t.getMessage())
                                                                        .toString());
 
                 IOException ioe = new IOException(new StringBuffer("UNCAUGHT exception: ").append(
                             t.getMessage()).toString());
                 ioe.setStackTrace(t.getStackTrace());
                 throw ioe;
             } finally {
                 /*if (featureReader != null) {
                     try{
                         featureReader.close();
                     }catch(IOException ioe){
                         //featureReader was probably closed already.
                     }
                 }*/
             }
         }
 
         writer.endDocument();
     }
 
     /**
      * This method produces and stores PNG images of all map layers using the StreamingRenderer and JAI Encoder.
      *
      * @param outZ
      * @throws IOException
      * @throws AbortedException
      */
     private void writeImages(final ZipOutputStream outZ, ArrayList layerRenderList)
         throws IOException, AbortedException {
         MapLayer[] layers = this.mapContext.getLayers();
         int nLayers = layers.length;
 
         for (int i = 0; i < nLayers; i++) {
             if (layerRenderList.size() > 0) {
                 int num = ((Integer) layerRenderList.get(0)).intValue();
 
                 if (num == i) { // if this layer is a layer that doesn't need to be rendered, move to next layer
                     layerRenderList.remove(0);
 
                     continue;
                 }
             }
 
             final MapLayer layer = layers[i];
             MapContext map = this.mapContext;
             map.clearLayerList();
             map.addLayer(layer);
 
             final int width = this.mapContext.getMapWidth();
             final int height = this.mapContext.getMapHeight();
 
             LOGGER.fine(new StringBuffer("setting up ").append(width).append("x").append(height)
                                                        .append(" image").toString());
 
             // simone: ARGB should be much better
             BufferedImage curImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
 
             // simboss: this should help out with coverages
             final Graphics2D graphic = GraphicsJAI.createGraphicsJAI(curImage.createGraphics(), null);
 
             LOGGER.fine("setting to transparent");
 
             int type = AlphaComposite.SRC;
             graphic.setComposite(AlphaComposite.getInstance(type));
 
             Color c = new Color(this.mapContext.getBgColor().getRed(),
                     this.mapContext.getBgColor().getGreen(),
                     this.mapContext.getBgColor().getBlue(), 0);
 
             //LOGGER.info("****** bg color: "+c.getRed()+","+c.getGreen()+","+c.getBlue()+","+c.getAlpha()+", trans: "+c.getTransparency());
             graphic.setBackground(this.mapContext.getBgColor());
             graphic.setColor(c);
             graphic.fillRect(0, 0, width, height);
 
             type = AlphaComposite.SRC_OVER;
             graphic.setComposite(AlphaComposite.getInstance(type));
 
             Rectangle paintArea = new Rectangle(width, height);
 
             final StreamingRenderer renderer = new StreamingRenderer();
             renderer.setContext(map);
 
             RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_ON);
             renderer.setJava2DHints(hints);
 
             // we already do everything that the optimized data loading does...
             // if we set it to true then it does it all twice...
             Map rendererParams = new HashMap();
             rendererParams.put("optimizedDataLoadingEnabled", Boolean.TRUE);
             rendererParams.put("renderingBuffer", new Integer(mapContext.getBuffer()));
             renderer.setRendererHints(rendererParams);
 
             Envelope dataArea = map.getAreaOfInterest();
             AffineTransform at = RendererUtilities.worldToScreenTransform(dataArea, paintArea);
             renderer.paint(graphic, paintArea, dataArea, at);
             graphic.dispose();
 
             // /////////////////////////////////////////////////////////////////
             //
             // Storing Image ...
             //
             // /////////////////////////////////////////////////////////////////
             final ZipEntry e = new ZipEntry("layer_" + (i) + ".png");
             outZ.putNextEntry(e);
            new ImageWorker(curImage).writePNG(outZ, "FILTERED", 0.75f, false, false);
            //final MemoryCacheImageOutputStream memOutStream = new MemoryCacheImageOutputStream(outZ);
             /*final PlanarImage encodedImage = PlanarImage
                             .wrapRenderedImage(curImage);
             //final PlanarImage finalImage = encodedImage.getColorModel() instanceof DirectColorModel?ImageUtilities
             //                .reformatColorModel2ComponentColorModel(encodedImage):encodedImage;
             final PlanarImage finalImage = encodedImage;
             final Iterator it = ImageIO.getImageWritersByMIMEType("image/png");
             ImageWriter imgWriter = null;
             if (!it.hasNext()) {
                     LOGGER.warning("No PNG ImageWriter found");
                     throw new IllegalStateException("No PNG ImageWriter found");
             } else
                     imgWriter = (ImageWriter) it.next();
             */
 
             //---------------------- bo- new
             //			PngEncoderB png = new PngEncoderB(curImage, PngEncoder.ENCODE_ALPHA, 0, 1);
             //			byte[] pngbytes = png.pngEncode();
             //			memOutStream.write(pngbytes);
             //----------------------
 
             //imgWriter.setOutput(memOutStream);
             //imgWriter.write(null, new IIOImage(finalImage, null, null), null);
            //memOutStream.flush();
            //memOutStream.close();
             //imgWriter.dispose();
             outZ.closeEntry();
         }
     }
 
     /**
      * Creates the bounding box filters (one for each geometric attribute) needed to query a
      * <code>MapLayer</code>'s feature source to return just the features for the target
      * rendering extent
      *
      * @param schema the layer's feature source schema
      * @param attributes set of needed attributes
      * @param bbox the expression holding the target rendering bounding box
      * @return an or'ed list of bbox filters, one for each geometric attribute in
      *         <code>attributes</code>. If there are just one geometric attribute, just returns
      *         its corresponding <code>GeometryFilter</code>.
      * @throws IllegalFilterException if something goes wrong creating the filter
      */
     private Filter createBBoxFilters(FeatureType schema, String[] attributes, BBoxExpression bbox)
         throws IllegalFilterException {
         Filter filter = null;
 
         final int length = attributes.length;
 
         for (int j = 0; j < length; j++) {
             AttributeType attType = schema.getAttributeType(attributes[j]);
 
             //DJB: added this for better error messages!
             if (attType == null) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine(new StringBuffer("Could not find '").append(attributes[j])
                                                                     .append("' in the FeatureType (")
                                                                     .append(schema.getTypeName())
                                                                     .append(")").toString());
                 }
 
                 throw new IllegalFilterException(new StringBuffer("Could not find '").append(attributes[j]
                         + "' in the FeatureType (").append(schema.getTypeName()).append(")")
                                                                                      .toString());
             }
 
             if (attType instanceof GeometryAttributeType) {
                 GeometryFilter gfilter = filterFactory.createGeometryFilter(Filter.GEOMETRY_BBOX);
 
                 // TODO: how do I get the full xpath of an attribute should
                 // feature composition be used?
                 Expression left = filterFactory.createAttributeExpression(schema, attType.getName());
                 gfilter.addLeftGeometry(left);
                 gfilter.addRightGeometry(bbox);
 
                 if (filter == null) {
                     filter = gfilter;
                 } else {
                     filter = filter.or(gfilter);
                 }
             }
         }
 
         return filter;
     }
 }
