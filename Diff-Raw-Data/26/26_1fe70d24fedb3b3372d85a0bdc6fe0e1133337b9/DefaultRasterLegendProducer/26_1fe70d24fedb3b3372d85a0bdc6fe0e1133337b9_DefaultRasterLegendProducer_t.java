 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.responses.wms;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.Feature;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.IllegalAttributeException;
 import org.geotools.renderer.lite.LiteShape;
 import org.geotools.renderer.lite.StyledShapePainter;
 import org.geotools.renderer.style.SLDStyleFactory;
 import org.geotools.renderer.style.Style2D;
 import org.geotools.styling.FeatureTypeStyle;
 import org.geotools.styling.LineSymbolizer;
 import org.geotools.styling.PointSymbolizer;
 import org.geotools.styling.PolygonSymbolizer;
 import org.geotools.styling.Rule;
 import org.geotools.styling.Style;
 import org.geotools.styling.Symbolizer;
 import org.geotools.styling.TextSymbolizer;
 import org.geotools.util.NumberRange;
 import org.vfny.geoserver.WmsException;
 import org.vfny.geoserver.requests.wms.GetLegendGraphicRequest;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 
 /**
  * Template {@linkPlain
  * org.vfny.geoserver.responses.wms.GetLegendGraphicProducer} based on
  * GeoTools' {@link
  * http://svn.geotools.org/geotools/trunk/gt/module/main/src/org/geotools/renderer/lite/StyledShapePainter.java
  * StyledShapePainter} that produces a BufferedImage with the appropiate
  * legend graphic for a given GetLegendGraphic WMS request.
  * 
  * <p>
  * It should be enough for a subclass to implement {@linkPlain
  * org.vfny.geoserver.responses.wms.GetLegendGraphicProducer#writeTo(OutputStream)}
  * and <code>getContentType()</code> in order to encode the BufferedImage
  * produced by this class to the appropiate output format.
  * </p>
  * 
  * <p>
  * This class takes literally the fact that the arguments <code>WIDTH</code>
  * and <code>HEIGHT</code> are just <i>hints</i> about the desired dimensions
  * of the produced graphic, and the need to produce a legend graphic
  * representative enough of the SLD style for which it is being generated.
  * Thus, if no <code>RULE</code> parameter was passed and the style has more
  * than one applicable Rule for the actual scale factor, there will be
  * generated a legend graphic of the specified width, but with as many stacked
  * graphics as applicable rules were found, providing by this way a
  * representative enough legend.
  * </p>
  *
  * @author Gabriel Roldan, Axios Engineering
  * @version $Id$
  */
 public abstract class DefaultRasterLegendProducer
     implements GetLegendGraphicProducer {
     /** shared package's logger */
     private static final Logger LOGGER = Logger.getLogger(DefaultRasterLegendProducer.class.getPackage()
                                                                                            .getName());
 
     /** Factory that will resolve symbolizers into rendered styles */
     private static final SLDStyleFactory styleFactory = new SLDStyleFactory();
 
     /** Tolerance used to compare doubles for equality */
     private static final double TOLERANCE = 1e-6;
 
     /**
      * Singleton shape painter to serve all legend requests. We can use a
      * single shape painter instance as long as it remains thread safe.
      */
     private static final StyledShapePainter shapePainter = new StyledShapePainter();
 
     /**
      * used to create sample point shapes with LiteShape (not lines nor
      * polygons)
      */
     private static final GeometryFactory geomFac = new GeometryFactory();
 
     /**
      * Legend graphics background color, since no BGCOLOR parameter is defined
      * for the GetLegendGraphic operation.
      */
     public static final Color BG_COLOR = Color.WHITE;

     /**
      * Image observer to help in creating the stack like legend graphic from
      * the images created for each rule
      */
     private static final ImageObserver imgObs = new Canvas();
 
     /** padding percentaje factor at both sides of the legend. */
    private static final float hpaddingFactor = 0.15f;
 
     /** top & bottom padding percentaje factor for the legend */
    private static final float vpaddingFactor = 0.15f;
 
     /** The image produced at <code>produceLegendGraphic</code> */
     private BufferedImage legendGraphic;
 
     /**
      * set to <code>true</code> when <code>abort()</code> gets called,
      * indicates that the rendering of the legend graphic should stop
      * gracefully as soon as possible
      */
     private boolean renderingStopRequested;
 
     /**
      * Just a holder to avoid creating many polygon shapes from inside
      * <code>getSampleShape()</code>
      */
     private Shape sampleRect;
 
     /**
      * Just a holder to avoid creating many line shapes from inside
      * <code>getSampleShape()</code>
      */
     private Shape sampleLine;
 
     /**
      * Just a holder to avoid creating many point shapes from inside
      * <code>getSampleShape()</code>
      */
     private Shape samplePoint;
 
     /**
      * Default constructor. Subclasses may provide its own with a String
      * parameter to establish its desired output format, if they support more
      * than one (e.g. a JAI based one)
      */
     public DefaultRasterLegendProducer() {
         super();
     }
 
     /**
      * Takes a GetLegendGraphicRequest and produces a BufferedImage that then
      * can be used by a subclass to encode it to the appropiate output format.
      *
      * @param request the "parsed" request, where "parsed" means that it's
      *        values are already validated so this method must not take care
      *        of verifying the requested layer exists and the like.
      *
      * @throws WmsException if there are problems creating a "sample" feature
      *         instance for the FeatureType <code>request</code> returns as
      *         the required layer (which should not occur).
      */
     public void produceLegendGraphic(GetLegendGraphicRequest request)
         throws WmsException {
         final Feature sampleFeature = createSampleFeature(request.getLayer());
 
         final Style gt2Style = request.getStyle();
         final FeatureTypeStyle[] ftStyles = gt2Style.getFeatureTypeStyles();
 
         final double scaleDenominator = request.getScale();
 
         final Rule[] applicableRules;
 
         if (request.getRule() != null) {
             applicableRules = new Rule[] { request.getRule() };
         } else {
             applicableRules = getApplicableRules(ftStyles, scaleDenominator);
         }
 
         final NumberRange scaleRange = new NumberRange(scaleDenominator,
                 scaleDenominator);
 
         final int ruleCount = applicableRules.length;
 
         /**
          * A legend graphic is produced for each applicable rule. They're being
          * holded here until the process is done and then painted on a "stack"
          * like legend.
          */
         final List /*<BufferedImage>*/ legendsStack = new ArrayList(ruleCount);
 
         final int w = request.getWidth();
         final int h = request.getHeight();
 
         for (int i = 0; i < ruleCount; i++) {
             Symbolizer[] symbolizers = applicableRules[i].getSymbolizers();
 
             BufferedImage image = new BufferedImage(w, h,
                     BufferedImage.TYPE_INT_ARGB);
             Graphics2D graphics = image.createGraphics();
             graphics.setColor(BG_COLOR);
             graphics.fillRect(0, 0, w, h);

             for (int sIdx = 0; sIdx < symbolizers.length; sIdx++) {
                 Symbolizer symbolizer = symbolizers[sIdx];
                 Style2D style2d = styleFactory.createStyle(sampleFeature,
                         symbolizer, scaleRange);
                 Shape shape = getSampleShape(symbolizer, w, h);
 
                 shapePainter.paint(graphics, shape, style2d, scaleDenominator);
             }
 
             legendsStack.add(image);
         }
 
         this.legendGraphic = mergeLegends(legendsStack);
     }
 
     /**
      * Recieves a list of <code>BufferedImages</code> and produces a new one
      * which holds all  the images in <code>imageStack</code> one above the
      * other.
      *
      * @param imageStack the list of BufferedImages, one for each applicable
      *        Rule
      *
      * @return the stack image with all the images on the argument list.
      *
      * @throws IllegalArgumentException if the list is empty
      */
     private static BufferedImage mergeLegends(List imageStack) {
         if (imageStack.size() == 0) {
             throw new IllegalArgumentException("No legend graphics passed");
         }
 
         BufferedImage finalLegend = null;
 
         if (imageStack.size() == 1) {
             finalLegend = (BufferedImage) imageStack.get(0);
         } else {
             Graphics2D finalGraphics = null;
             final int imgCount = imageStack.size();
             int w = 0;
             int h = 0;
 
             for (int i = 0; i < imgCount; i++) {
                 BufferedImage img = (BufferedImage) imageStack.get(i);
 
                 if (i == 0) {
                     w = img.getWidth();
                     h = img.getHeight();
 
                     finalLegend = new BufferedImage(w, imgCount * h,
                             BufferedImage.TYPE_INT_ARGB);
                     finalGraphics = finalLegend.createGraphics();
                 }
 
                 finalGraphics.drawImage(img, 0, h * i, imgObs);
             }
         }
 
         return finalLegend;
     }
 
     /**
      * Returns a <code>java.awt.Shape</code> appropiate to render a legend
      * graphic given the symbolizer type and the legend dimensions.
      *
      * @param symbolizer the Symbolizer for whose type a sample shape will be
      *        created
      * @param legendWidth the requested width, in output units, of the legend
      *        graphic
      * @param legendHeight the requested height, in output units, of the legend
      *        graphic
      *
      * @return an appropiate Line2D, Rectangle2D or LiteShape(Point) for the
      *         symbolizer, wether it is a LineSymbolizer, a PolygonSymbolizer,
      *         or a Point ot Text Symbolizer
      *
      * @throws IllegalArgumentException if an unknown symbolizer impl was
      *         passed in.
      */
     private Shape getSampleShape(Symbolizer symbolizer, int legendWidth,
         int legendHeight) {
         Shape sampleShape;
         float hpad = (legendWidth * hpaddingFactor);
         float vpad = (legendHeight * vpaddingFactor);
 
         if (symbolizer instanceof LineSymbolizer) {
             if (this.sampleLine == null) {
                 this.sampleLine = new Line2D.Float(hpad, legendHeight - vpad,
                         legendWidth - hpad, vpad);
             }
 
             sampleShape = this.sampleLine;
         } else if (symbolizer instanceof PolygonSymbolizer) {
             if (this.sampleRect == null) {
                 this.sampleRect = new Rectangle2D.Float(hpad, vpad,
                        legendWidth - 2 * hpad, legendHeight - 2 * vpad);
             }
 
             sampleShape = this.sampleRect;
         } else if (symbolizer instanceof PointSymbolizer
                 || symbolizer instanceof TextSymbolizer) {
             if (this.samplePoint == null) {
                 Coordinate coord = new Coordinate(legendWidth / 2,
                         legendHeight / 2);
                 this.samplePoint = new LiteShape(geomFac.createPoint(coord),
                         null, false);
             }
 
             sampleShape = this.samplePoint;
         } else {
             throw new IllegalArgumentException("Unknown symbolizer: "
                 + symbolizer);
         }
 
         return sampleShape;
     }
 
     /**
      * Creates a sample Feature instance in the hope that it can be used in the
      * rendering of the legend graphic.
      *
      * @param schema the schema for which to create a sample Feature instance
      *
      * @return
      *
      * @throws WmsException
      */
     private Feature createSampleFeature(FeatureType schema)
         throws WmsException {
         Feature sampleFeature;
 
         try {
             AttributeType[] atts = schema.getAttributeTypes();
             Object[] attributes = new Object[atts.length];
 
             for (int i = 0; i < atts.length; i++)
                 attributes[i] = atts[i].createDefaultValue();
 
             sampleFeature = schema.create(attributes);
         } catch (IllegalAttributeException e) {
             e.printStackTrace();
             throw new WmsException(e);
         }
 
         return sampleFeature;
     }
 
     /**
      * Finds the applicable Rules for the given scale denominator.
      *
      * @param ftStyles
      * @param scaleDenominator
      *
      * @return
      */
     private Rule[] getApplicableRules(FeatureTypeStyle[] ftStyles,
         double scaleDenominator) {
         /**
          * Holds both the rules that apply and the ElseRule's if any, in the
          * order they appear
          */
         final List ruleList = new ArrayList();
 
         // get applicable rules at the current scale
         for (int i = 0; i < ftStyles.length; i++) {
             FeatureTypeStyle fts = ftStyles[i];
             Rule[] rules = fts.getRules();
 
             for (int j = 0; j < rules.length; j++) {
                 Rule r = rules[j];
 
                 if (isWithInScale(r, scaleDenominator)) {
                     ruleList.add(r);
 
                    /*
                     * I'm commented this out since I guess it has no sense
                     * for producing the legend, since wether or not the rule
                     * has an else filter, the legend is drawn only if the
                     * scale denominator lies inside the rule's scale range.
                          if (r.hasElseFilter()) {
                              ruleList.add(r);
                          }
                     */
                 }
             }
         }
 
         return (Rule[]) ruleList.toArray(new Rule[ruleList.size()]);
     }
 
     /**
      * Checks if a rule can be triggered at the current scale level
      *
      * @param r The rule
      * @param scaleDenominator the scale denominator to check if it is between
      *        the rule's scale range. -1 means that it allways is.
      *
      * @return true if the scale is compatible with the rule settings
      */
     private boolean isWithInScale(Rule r, double scaleDenominator) {
         return (scaleDenominator == -1)
         || (((r.getMinScaleDenominator() - TOLERANCE) <= scaleDenominator)
         && ((r.getMaxScaleDenominator() + TOLERANCE) > scaleDenominator));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return
      *
      * @throws IllegalStateException DOCUMENT ME!
      */
     public BufferedImage getLegendGraphic() {
         if (this.legendGraphic == null) {
             throw new IllegalStateException();
         }
 
         return this.legendGraphic;
     }
 
     /**
      * Asks the rendering to stop processing.
      */
     public void abort() {
         this.renderingStopRequested = true;
     }
 }
