 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.Feature;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.IllegalAttributeException;
 import org.geotools.geometry.jts.LiteShape2;
 import org.geotools.renderer.lite.StyledShapePainter;
 import org.geotools.renderer.style.SLDStyleFactory;
 import org.geotools.renderer.style.Style2D;
 import org.geotools.styling.FeatureTypeStyle;
 import org.geotools.styling.LineSymbolizer;
 import org.geotools.styling.PointSymbolizer;
 import org.geotools.styling.PolygonSymbolizer;
 import org.geotools.styling.RasterSymbolizer;
 import org.geotools.styling.Rule;
 import org.geotools.styling.Style;
 import org.geotools.styling.Symbolizer;
 import org.geotools.styling.TextSymbolizer;
 import org.geotools.util.NumberRange;
 import org.vfny.geoserver.wms.GetLegendGraphicProducer;
 import org.vfny.geoserver.wms.WmsException;
 import org.vfny.geoserver.wms.requests.GetLegendGraphicRequest;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.LineString;
 import com.vividsolutions.jts.geom.LinearRing;
 import com.vividsolutions.jts.geom.Polygon;
 
 
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
 public abstract class DefaultRasterLegendProducer implements GetLegendGraphicProducer {
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
     private static final StyledShapePainter shapePainter = new StyledShapePainter(null);
 
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
     private LiteShape2 sampleRect;
 
     /**
      * Just a holder to avoid creating many line shapes from inside
      * <code>getSampleShape()</code>
      */
     private LiteShape2 sampleLine;
 
     /**
      * Just a holder to avoid creating many point shapes from inside
      * <code>getSampleShape()</code>
      */
     private LiteShape2 samplePoint;
 
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
 
         final NumberRange scaleRange = new NumberRange(scaleDenominator, scaleDenominator);
 
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
 
             BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
             Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             graphics.setColor(BG_COLOR);
             graphics.fillRect(0, 0, w, h);
 
             for (int sIdx = 0; sIdx < symbolizers.length; sIdx++) {
                 Symbolizer symbolizer = symbolizers[sIdx];
 
                 if (symbolizer instanceof RasterSymbolizer) {
                     BufferedImage imgShape = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
 
                     try {
                         imgShape = ImageIO.read(new URL(request.getHttpServletRequest()
                                                                .getRequestURL()
                                     + "/../data/images/rasterLegend.png"));
                     } catch (MalformedURLException e) {
                         LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                         throw new WmsException(e);
                     } catch (IOException e) {
                         LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                         throw new WmsException(e);
                     }
 
                     graphics.drawImage(imgShape, 0, 0, w, h, null);
                 } else {
                     Style2D style2d = styleFactory.createStyle(sampleFeature, symbolizer, scaleRange);
                     LiteShape2 shape = getSampleShape(symbolizer, w, h);
 
                     if (style2d != null) {
                         shapePainter.paint(graphics, shape, style2d, scaleDenominator);
                     }
                 }
             }
 
             legendsStack.add(image);
         }
 
         //JD: changd legend behaviour, see GEOS-812
         //this.legendGraphic = scaleImage(mergeLegends(legendsStack), request);
         this.legendGraphic = mergeLegends(legendsStack, applicableRules, request);
     }
 
     /**
      *   Scales the image so that its the size specified in the request.
      *   @hack -- there should be a much better way to do this.  See handleLegendURL() in WMSCapsTransformer.
          * @param image
          * @return
          */
     private BufferedImage scaleImage(BufferedImage image, GetLegendGraphicRequest request) {
         final int w = request.getWidth();
         final int h = request.getHeight();
 
         BufferedImage scaledImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D graphics = scaledImage.createGraphics();
         graphics.setColor(BG_COLOR);
         graphics.fillRect(0, 0, w, h);
 
         AffineTransform xform = new AffineTransform();
         xform.setToScale(((double) w) / image.getWidth(), ((double) h) / image.getHeight());
 
         graphics.drawImage(image, xform, null);
 
         return scaledImage;
     }
 
     /**
     * Recieves a list of <code>BufferedImages</code> and produces a new one
     * which holds all  the images in <code>imageStack</code> one above the
     * other.
     *
     * @param imageStack the list of BufferedImages, one for each applicable
     *        Rule
     * @param rules The applicable rules, one for each image in the stack
     * @param request The request.
     *
     * @return the stack image with all the images on the argument list.
     *
     * @throws IllegalArgumentException if the list is empty
     */
     private static BufferedImage mergeLegends(List imageStack, Rule[] rules,
         GetLegendGraphicRequest req) {
         
         Font labelFont = getLabelFont(req);
         boolean useAA = false;
         if (req.getLegendOptions().get("fontAntiAliasing") instanceof String) {
             String aaVal = (String)req.getLegendOptions().get("fontAntiAliasing");
             if (aaVal.equalsIgnoreCase("on") || aaVal.equalsIgnoreCase("true") ||
                     aaVal.equalsIgnoreCase("yes") || aaVal.equalsIgnoreCase("1")) {
                 useAA = true;
             }
         }
         
         boolean forceLabelsOn = false;
         boolean forceLabelsOff = false;
         if (req.getLegendOptions().get("forceLabels") instanceof String) {
             String forceLabelsOpt = (String)req.getLegendOptions().get("forceLabels");
             if (forceLabelsOpt.equalsIgnoreCase("on")) {
                 forceLabelsOn = true;
             } else if (forceLabelsOpt.equalsIgnoreCase("off")) {
                 forceLabelsOff = true;
             }
         }
         
         if (imageStack.size() == 0) {
             throw new IllegalArgumentException("No legend graphics passed");
         }
 
         BufferedImage finalLegend = null;
 
         if (imageStack.size() == 1 && !forceLabelsOn) {
             finalLegend = (BufferedImage) imageStack.get(0);
         } else {
             final int imgCount = imageStack.size();
             final String[] labels = new String[imgCount];
 
             BufferedImage img = ((BufferedImage) imageStack.get(0));
 
             int totalHeight = 0;
             int totalWidth = 0;
             int[] rowHeights = new int[imgCount];
 
             for (int i = 0; i < imgCount; i++) {
                 img = (BufferedImage) imageStack.get(i);
                 
                 if (forceLabelsOff) {
                     totalWidth = (int) Math.ceil(Math.max(img.getWidth(), totalWidth));
                     rowHeights[i] = img.getHeight();
                     totalHeight += img.getHeight(); 
                 } else {
 
                     Rule rule = rules[i];
     
                     //What's the label on this rule?  We prefer to use
                     //the 'title' if it's available, but fall-back to 'name'
                     labels[i] = rule.getTitle();
                     if (labels[i] == null) labels[i] = rule.getName();
                     if (labels[i] == null) labels[i] = "";
                     
                     Graphics2D g = img.createGraphics();
                     g.setFont(labelFont);
     
                     if (useAA) {
                         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                     } else {
                         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                     }
     
                     if(labels[i] != null && labels[i].length() > 0) {
                         final BufferedImage renderedLabel = renderLabel(labels[i], g);
                         final Rectangle2D bounds = new Rectangle2D.Double(0, 0, renderedLabel.getWidth(),
                                 renderedLabel.getHeight());
         
                         totalWidth = (int) Math.ceil(Math.max(img.getWidth() + bounds.getWidth(), totalWidth));
                         rowHeights[i] = (int) Math.ceil(Math.max(img.getHeight(), bounds.getHeight()));
                     } else {
                         totalWidth = (int) Math.ceil(Math.max(img.getWidth(), totalWidth));
                         rowHeights[i] = (int) Math.ceil(img.getHeight());
                     }
                     totalHeight += rowHeights[i];
                 }
             }
 
             //buffer the width a bit
             totalWidth += 2;
 
             //create the final image
             finalLegend = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
 
             Graphics2D finalGraphics = finalLegend.createGraphics();
 
             finalGraphics.setColor(BG_COLOR);
             finalGraphics.fillRect(0, 0, totalWidth, totalHeight);
 
             int topOfRow = 0;
 
             for (int i = 0; i < imgCount; i++) {
                 img = (BufferedImage) imageStack.get(i);
 
                 //draw the image
                 int y = topOfRow;
 
                 if (img.getHeight() < rowHeights[i]) {
                     //move the image to the center of the row
                     y += (int) ((rowHeights[i] - img.getHeight()) / 2d);
                 }
 
                 finalGraphics.drawImage(img, 0, y, imgObs);
                 if (forceLabelsOff) {
                     topOfRow += rowHeights[i];
                     continue;
                 }
                 
                 finalGraphics.setFont(labelFont);
 
                 if (useAA) {
                     finalGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                         RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                 } else {
                     finalGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                         RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                 }
 
                 //draw the label
                 if (labels[i] != null && labels[i].length() > 0) {
                     //first create the actual overall label image.
                     final BufferedImage renderedLabel = renderLabel(labels[i], finalGraphics);
 
                     y = topOfRow;
 
                     if (renderedLabel.getHeight() < rowHeights[i]) {
                         y += (int) ((rowHeights[i] - renderedLabel.getHeight()) / 2d);
                     }
 
                     finalGraphics.drawImage(renderedLabel, img.getWidth(), y, imgObs);
                 }
 
                 topOfRow += rowHeights[i];
             }
         }
 
         return finalLegend;
     }
     
     private static Font getLabelFont(GetLegendGraphicRequest req) {
         
         String legendFontName = "Sans-Serif";
         String legendFontFamily = "plain";
         int legendFontSize = 12;
         
         Map legendOptions = req.getLegendOptions();
         if (legendOptions.get("fontName") != null) {
             legendFontName = (String) legendOptions.get("fontName");
         }
         if (legendOptions.get("fontStyle") != null) {
             legendFontFamily = (String)legendOptions.get("fontStyle");
         }
         if (legendOptions.get("fontSize") != null) {
             try {
                 legendFontSize = Integer.parseInt((String)legendOptions.get("fontSize"));
             } catch (Exception e) {
                 LOGGER.warning("Error trying to interpret legendOption 'fontSize': " + legendOptions.get("fontSize"));
             }
         }
         
         Font legendFont;
         if (legendFontFamily.equalsIgnoreCase("italic")) {
             legendFont = new Font(legendFontName, Font.ITALIC, legendFontSize);
         } else if (legendFontFamily.equalsIgnoreCase("bold")) {
             legendFont = new Font(legendFontName, Font.BOLD, legendFontSize);
         } else {
             legendFont = new Font(legendFontName, Font.PLAIN, legendFontSize);
         }
         
         return legendFont;
     }
     
     /**
      * Return a {@link BufferedImage} representing this label.
      * The characters '\n' '\r' and '\f' are interpreted as linebreaks,
      * as is the characater combination "\n" (as opposed to the actual '\n' character).
      * This allows people to force line breaks in their labels by
      * including the character "\" followed by "n" in their
      * label.
      *
      * @param label - the label to render
      * @param g - the Graphics2D that will be used to render this label
      * @return a {@link BufferedImage} of the properly rendered label.
      */
     public static BufferedImage renderLabel(String label, Graphics2D g) {
         // We'll accept '/n' as a text string
         //to indicate a line break, as well as a traditional 'real' line-break in the XML.
         BufferedImage renderedLabel;
 
         if ((label.indexOf("\n") != -1) || (label.indexOf("\\n") != -1)) {
             //this is a label WITH line-breaks...we need to figure out it's height *and*
             //width, and then adjust the legend size accordingly
             Rectangle2D bounds = new Rectangle2D.Double(0, 0, 0, 0);
             ArrayList lineHeight = new ArrayList();
             // four backslashes... "\\" -> '\', so "\\\\n" -> '\' + '\' + 'n'
             final String realLabel = label.replaceAll("\\\\n", "\n");
             StringTokenizer st = new StringTokenizer(realLabel, "\n\r\f");
 
             while (st.hasMoreElements()) {
                 final String token = st.nextToken();
                 Rectangle2D thisLineBounds = g.getFontMetrics().getStringBounds(token, g);
 
                 //if this is directly added as thisLineBounds.getHeight(), then there are rounding errors
                 //because we can only DRAW fonts at discrete integer coords.
                 final int thisLineHeight = (int) Math.ceil(thisLineBounds.getHeight());
                 bounds.add(0, thisLineHeight + bounds.getHeight());
                 bounds.add(thisLineBounds.getWidth(), 0);
                 lineHeight.add(new Integer((int) Math.ceil(thisLineBounds.getHeight())));
             }
 
             //make the actual label image
             renderedLabel = new BufferedImage((int) Math.ceil(bounds.getWidth()),
                     (int) Math.ceil(bounds.getHeight()), BufferedImage.TYPE_INT_ARGB);
 
             st = new StringTokenizer(realLabel, "\n\r\f");
 
             Graphics2D rlg = renderedLabel.createGraphics();
             rlg.setColor(Color.black);
             rlg.setFont(g.getFont());
             rlg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                 g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING));
 
             int y = 0 - g.getFontMetrics().getDescent();
             int c = 0;
 
             while (st.hasMoreElements()) {
                 y += ((Integer) lineHeight.get(c++)).intValue();
                 rlg.drawString(st.nextToken(), 0, y);
             }
         } else {
             //this is a traditional 'regular-old' label.  Just figure the
             //size and act accordingly.
             int height = (int) Math.ceil(g.getFontMetrics().getStringBounds(label, g).getHeight());
             int width = (int) Math.ceil(g.getFontMetrics().getStringBounds(label, g).getWidth());
             renderedLabel = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 
             Graphics2D rlg = renderedLabel.createGraphics();
             rlg.setColor(Color.black);
             rlg.setFont(g.getFont());
             rlg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                 g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING));
             rlg.drawString(label, 0, height - rlg.getFontMetrics().getDescent());
         }
 
         return renderedLabel;
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
     private LiteShape2 getSampleShape(Symbolizer symbolizer, int legendWidth, int legendHeight) {
         LiteShape2 sampleShape;
         final float hpad = (legendWidth * hpaddingFactor);
         final float vpad = (legendHeight * vpaddingFactor);
 
         if (symbolizer instanceof LineSymbolizer) {
             if (this.sampleLine == null) {
                 Coordinate[] coords = {
                         new Coordinate(hpad, legendHeight - vpad),
                         new Coordinate(legendWidth - hpad, vpad)
                     };
                 LineString geom = geomFac.createLineString(coords);
 
                 try {
                     this.sampleLine = new LiteShape2(geom, null, null, false);
                 } catch (Exception e) {
                     this.sampleLine = null;
                 }
             }
 
             sampleShape = this.sampleLine;
         } else if ((symbolizer instanceof PolygonSymbolizer)
                 || (symbolizer instanceof RasterSymbolizer)) {
             if (this.sampleRect == null) {
                 final float w = legendWidth - (2 * hpad);
                 final float h = legendHeight - (2 * vpad);
 
                 Coordinate[] coords = {
                         new Coordinate(hpad, vpad), new Coordinate(hpad, vpad + h),
                         new Coordinate(hpad + w, vpad + h), new Coordinate(hpad + w, vpad),
                         new Coordinate(hpad, vpad)
                     };
                 LinearRing shell = geomFac.createLinearRing(coords);
                 Polygon geom = geomFac.createPolygon(shell, null);
 
                 try {
                     this.sampleRect = new LiteShape2(geom, null, null, false);
                 } catch (Exception e) {
                     this.sampleRect = null;
                 }
             }
 
             sampleShape = this.sampleRect;
         } else if (symbolizer instanceof PointSymbolizer || symbolizer instanceof TextSymbolizer) {
             if (this.samplePoint == null) {
                 Coordinate coord = new Coordinate(legendWidth / 2, legendHeight / 2);
 
                 try {
                     this.samplePoint = new LiteShape2(geomFac.createPoint(coord), null, null, false);
                 } catch (Exception e) {
                     this.samplePoint = null;
                 }
             }
 
             sampleShape = this.samplePoint;
         } else {
             throw new IllegalArgumentException("Unknown symbolizer: " + symbolizer);
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
     private Rule[] getApplicableRules(FeatureTypeStyle[] ftStyles, double scaleDenominator) {
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
