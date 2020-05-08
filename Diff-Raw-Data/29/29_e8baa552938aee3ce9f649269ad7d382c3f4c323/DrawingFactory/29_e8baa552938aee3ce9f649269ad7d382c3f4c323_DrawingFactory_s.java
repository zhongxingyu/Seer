 package pt.inevo.encontra.drawing;
 
 import org.apache.batik.bridge.BridgeContext;
 import org.apache.batik.bridge.DocumentLoader;
 import org.apache.batik.bridge.GVTBuilder;
 import org.apache.batik.bridge.UserAgentAdapter;
 import org.apache.batik.css.engine.SVGCSSEngine;
 import org.apache.batik.css.engine.StyleMap;
 import org.apache.batik.css.engine.value.ComputedValue;
 import org.apache.batik.css.engine.value.RGBColorValue;
 import org.apache.batik.css.engine.value.Value;
 import org.apache.batik.css.engine.value.ValueConstants;
 import org.apache.batik.dom.svg.*;
 import org.apache.batik.gvt.GraphicsNode;
 import org.apache.batik.parser.ParseException;
 import org.apache.batik.parser.PathHandler;
 import org.apache.batik.parser.PathParser;
 import org.apache.batik.util.SVGConstants;
 import org.apache.xerces.parsers.SAXParser;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.svg.*;
 import pt.inevo.encontra.drawing.util.Color;
 import pt.inevo.encontra.drawing.util.Functions;
 import pt.inevo.encontra.geometry.Point;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Represents a class responsible for instanciating drawing objects.
  *
  * @author Gabriel
  */
 public class DrawingFactory {
     private static final DrawingFactory INSTANCE = new DrawingFactory();
     private int CURVE_SEGMENTATION;
     private boolean simplified;
     private long count;
     private Simplification simplification;
 
     //private Pattern findDpath;
     private Pattern findX;
     private Pattern findY;
     private Pattern findWidth;
     private Pattern findHeight;
     private Pattern findNum;
     private Pattern findId;
     private Pattern findAlphaNum;
 
     /**
      * Returns true if the drawing should be simplified, false otherwise.
      *
      * @return true if the drawing should be simplified, false otherwise.
      */
     public boolean isSimplified() {
         return simplified;
     }
 
     /**
      * Sets whether the new drawing should be simplified or not.
      *
      * @param useSimplify true if the new drawing should be simplified, false otherwise.
      */
     public void setSimplified(boolean useSimplify) {
         this.simplified = useSimplify;
     }
 
     /**
      * Returns the number of segments in which a bezier curve should be segmented.
      *
      * @return the number of segments in which a bezier curve should be segmented.
      */
     public int getCurveSegmentation() {
         return CURVE_SEGMENTATION;
     }
 
     /**
      * Sets the number of segments in which a bezier curve should be segmented.
      *
      * @param n the number of segments in which a bezier curve should be segmented.
      */
     public void setCurveSegmentation(int n) {
         CURVE_SEGMENTATION = n;
     }
 
     /**
      * Returns current simplification settings.
      *
      * @return the simplification settings.
      */
     public Simplification getSimplification() {
         return simplification;
     }
 
     /**
      * Sets the simplification settings.
      *
      * @param simplification the simplification settings to be set.
      */
     public void setSimplification(Simplification simplification) {
         this.simplification = simplification;
     }
 
     /**
      * Returns the singleton object of this class.
      *
      * @return the singleton object of this class.
      */
     public static DrawingFactory getInstance() {
         return INSTANCE;
     }
 
     private DrawingFactory() {
         simplified = true;
 //        simplified = false;
         count = 0;
         CURVE_SEGMENTATION = 24;
         //findDpath = Pattern.compile("^[ \t]+d=\"");
         findX = Pattern.compile("x=\"\\d+(.\\d+)?\"");
         findY = Pattern.compile("y=\"\\d+(.\\d+)?\"");
         findWidth = Pattern.compile("width=\"\\d+(.\\d+)?\"");
         findHeight = Pattern.compile("height=\"\\d+(.\\d+)?\"");
         findId = Pattern.compile("id=\"[a-zA-Z0-9]+\"");
         findNum = Pattern.compile("\\d+(.\\d+)?");
         findAlphaNum = Pattern.compile("\"[a-zA-Z0-9]+\"");
         simplification = new Simplification();
     }
 
     /**
      * Returns a new Drawing object from the supplied SVG file.
      *
      * @param filePath the path to the SVG source file.
      * @return the drawing.
      */
     public Drawing drawingFromSVG(String filePath) throws FileNotFoundException, IOException {
 //        StringBuilder contents = new StringBuilder();
 //        String line;
 //        FileReader fr = new FileReader(filePath);
 //        BufferedReader br = new BufferedReader(fr);
 //        while((line = br.readLine()) != null) {
 //            contents.append(line).append(System.getProperty("line.separator"));
 //        }
 ////        Drawing drawing = drawingFromSVGContent(contents.toString());
 ////        return drawing;
 //        StringReader reader = new StringReader(contents.toString());
 //        URI uri = null;
 //        try {
 //            uri = new URI("file:///" + filePath.replace("\\", "/"));
 //        } catch (Exception e) { }
         Drawing drawing = drawingFromBatik(filePath);
         drawing.setFilename(filePath);
         return drawing;
     }
 
     /**
      * Generates a drawing from the supplied svg path, using batik.
      *
      * @param filePath the path to the SVG source file.
      * @return the generated drawing.
      * @throws IOException
      */
     private Drawing drawingFromBatik(String filePath) throws IOException {
         Drawing drawing = new Drawing(count++);
 
         FileReader reader = new FileReader(filePath);
         String ns = SVGDOMImplementation.SVG_NAMESPACE_URI;
         String parser = SAXParser.class.getCanonicalName();//XMLResourceDescriptor.getXMLParserClassName(); //devolve null
 //        String parser = XMLResourceDescriptor.getXMLParserClassName();
         SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
         SVGDocument doc = f.createSVGDocument(ns, reader);
 
         // boot the CSS engine to get Batik to compute the CSS
         UserAgentAdapter userAgent = new UserAgentAdapter();
         DocumentLoader loader = new DocumentLoader(userAgent);
         BridgeContext ctx = new BridgeContext(userAgent, loader);
         ctx.setDynamicState(BridgeContext.DYNAMIC);
         GVTBuilder builder = new GVTBuilder();
         GraphicsNode rootGN = builder.build(ctx, doc);
 
         // parse elements
         SVGElement root = doc.getRootElement();
         ArrayList<Primitive> primitives = walkSvg(root);
         int last;
         for (Primitive primitive : primitives) {
             //check if last point = first point, because then it is closed=polygon
             last = primitive.getNumPoints() - 1;
 
             if (last > 0) {
                 // if last point is the first point, then setClosed(true)
                 if (((primitive.getPoint(0).getX()) == (primitive.getPoint(last).getX())) &&
                         ((primitive.getPoint(0).getY()) == (primitive.getPoint(last).getY()))) {
                     primitive.setClosed(true);
                 }
             }
 
             if (primitive.getNumPoints() > 1) {
                 drawing.addPrimitive(primitive);
             } else {
                 System.err.println("empty primitive");
             }
         }
 
         if (this.simplified)
             drawing.simplify(simplification);
 
         // cleanup
         reader.close();
         loader.dispose();
         ctx.dispose();
 
         drawing.setDocument(doc);
 
         return drawing;
     }
 
     //    public BufferedImage getImage(int bitmap_resolution)
 //    {
 //        try {
 //            TranscoderInput input = new TranscoderInput(_document);
 //            Rasterizer r = new Rasterizer();
 //            r.addTranscodingHint(ImageTranscoder.KEY_WIDTH, new Float(bitmap_resolution));
 //            r.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, new Float(bitmap_resolution));
 //            r.addTranscodingHint(ImageTranscoder.KEY_BACKGROUND_COLOR, java.awt.Color.WHITE);
 //            r.transcode(input,null);
 //
 //            return r.getImage();
 //
 //        } catch (Exception e) {
 //
 //            log.log(Level.WARNING,"Error rasterizing!\n.",e);
 //        }
 //
 //
 //        return null;
 //    }
 
     private ArrayList<Primitive> walkSvg(SVGElement root) {
         //SVGMatrix mat = new SVGOMMatrix(AffineTransform.getScaleInstance(1.0, 1.0)); // identity matrix
         ArrayList<Primitive> primitives = walkElement(root, root, 0);
         return primitives;
     }
 
     private ArrayList<Primitive> walkElement(SVGElement root, SVGElement node, int depth) {
         ArrayList<Primitive> primitives = new ArrayList<Primitive>();
 
 //        SVGElement child = (SVGElement)node.getFirstChild();
 //        String tag;
 //        while (child != null) {
 //            tag = child.getTagName();
 //            if (tag.equals(SVGConstants.SVG_G_TAG))
 //                primitives.addAll(walkG(root, (SVGGElement)child, depth));
 //            else if(tag.equals(SVGConstants.SVG_PATH_TAG))
 //                primitives.addAll(walkPath(root, (SVGOMPathElement)child, depth+1));
 //            else if (tag.equals(SVGConstants.SVG_RECT_TAG))
 //                primitives.addAll(walkRect(root, (SVGOMRectElement)child, depth+1));
 //            child = (SVGElement)child.getNextSibling();
 //        }
 
         // TODO: falta tratar outras primitivas
         // TODO: falta tratar use e include
 
         NodeList elements = node.getChildNodes();
         Node n;
         for (int i = 0; i < elements.getLength(); i++) {
             n = elements.item(i);
             if (n.getParentNode() == node) {
                 if (n.getNodeName().equals(SVGConstants.SVG_G_TAG)) {
                     primitives.addAll(walkG(root, (SVGGElement) n, depth));
                 } else if (n.getNodeName().equals(SVGConstants.SVG_PATH_TAG)) {
                     primitives.addAll(walkPath(root, (SVGOMPathElement) n, depth + 1));
                 } else if (n.getNodeName().equals(SVGConstants.SVG_RECT_TAG)) {
                     primitives.addAll(walkRect(root, (SVGOMRectElement) n, depth + 1));
                 }
             }
         }
 
 //        NodeList gs = node.getElementsByTagName(SVGConstants.SVG_G_TAG);
 //        for (int i = 0; i < gs.getLength(); i++) {
 //            n = gs.item(i);
 //            if (n.getParentNode() == node)
 //                primitives.addAll(walkG(root, (SVGGElement)n, depth));
 //        }
 //
 //        NodeList paths = node.getElementsByTagName(SVGConstants.SVG_PATH_TAG);
 //        for (int i = 0; i < paths.getLength(); i++) {
 //            n = paths.item(i);
 //            if (n.getParentNode() == node)
 //                primitives.addAll(walkPath(root, (SVGOMPathElement)n, depth+1));
 //        }
 //
 //        NodeList rects = node.getElementsByTagNameNS(node.getNamespaceURI(),SVGConstants.SVG_RECT_TAG);
 //        for (int i = 0; i < rects.getLength(); i++) {
 //            n = rects.item(i);
 //            if (n.getParentNode() == node)
 //                primitives.addAll(walkRect(root, (SVGOMRectElement)n, depth+1));
 //        }
 
         return primitives;
     }
 
     private ArrayList<Primitive> walkG(SVGElement root, SVGGElement g, int depth) {
         ArrayList<Primitive> primitives = new ArrayList<Primitive>();
         SVGMatrix mat = g.getTransformToElement(root);
 
         primitives.addAll(walkElement(root, g, depth));
 
         return primitives;
     }
 
     private ArrayList<Primitive> walkRect(SVGElement root, SVGOMRectElement rect, int depth) {
         ArrayList<Primitive> primitives = new ArrayList<Primitive>();
         Primitive primitive = new Primitive();
         SVGMatrix mat = rect.getTransformToElement(root);
         SVGRect r = rect.getBBox();
 
         // point extraction
         SVGPoint P1 = new SVGOMPoint(r.getX(), r.getY()).matrixTransform(mat);
         SVGPoint P2 = new SVGOMPoint(r.getX() + r.getWidth(), r.getY()).matrixTransform(mat);
         SVGPoint P3 = new SVGOMPoint(r.getX() + r.getWidth(), r.getY() + r.getHeight()).matrixTransform(mat);
         SVGPoint P4 = new SVGOMPoint(r.getX(), r.getY() + r.getHeight()).matrixTransform(mat);
 
         primitive.addPoint(P1.getX(), P1.getY());
         primitive.addPoint(P2.getX(), P2.getY());
         primitive.addPoint(P3.getX(), P3.getY());
         primitive.addPoint(P4.getX(), P4.getY());
         primitive.addPoint(P1.getX(), P1.getY());
 
         // set properties
 
         // color and border extraction
         setSVGElementProperties(rect, primitive);
 
         // add the new primitive and the primitives created from this node's children
         primitives.add(primitive);
         primitives.addAll(walkElement(root, rect, depth));
 
         return primitives;
     }
 
     private ArrayList<Primitive> walkPath(SVGElement root, SVGOMPathElement path, int depth) {
         ArrayList<Primitive> primitives = new ArrayList<Primitive>();
         Primitive primitive = new Primitive();
         SVGMatrix mat = path.getTransformToElement(root);
 
         // point extraction
         SVGPathSegList list = path.getNormalizedPathSegList();
         int len = list.getNumberOfItems();
         SVGPathSeg seg;
         SVGPoint firstPoint = null;
         SVGPoint lastPoint = null;
         for (int i = 0; i < len; i++) {
             seg = list.getItem(i);
             if (seg.getPathSegType() == SVGPathSeg.PATHSEG_MOVETO_ABS) {
                 SVGPathSegMovetoAbs M = (SVGPathSegMovetoAbs) seg;
                 lastPoint = new SVGOMPoint(M.getX(), M.getY());
                 lastPoint = transformPoint(lastPoint, mat);
                 if (firstPoint == null)
                     firstPoint = lastPoint;
                 primitive.addPoint(lastPoint.getX(), lastPoint.getY());
             } else if (seg.getPathSegType() == SVGPathSeg.PATHSEG_LINETO_ABS) {
                 SVGPathSegLinetoAbs L = (SVGPathSegLinetoAbs) seg;
                 lastPoint = new SVGOMPoint(L.getX(), L.getY());
                 lastPoint = transformPoint(lastPoint, mat);
                 if (firstPoint == null)
                     firstPoint = lastPoint;
                 primitive.addPoint(lastPoint.getX(), lastPoint.getY());
             } else if (seg.getPathSegType() == SVGPathSeg.PATHSEG_CURVETO_CUBIC_ABS) {
                 SVGPathSegCurvetoCubicAbs C = (SVGPathSegCurvetoCubicAbs) seg;
                 SVGPoint P0 = new SVGOMPoint(lastPoint.getX(), lastPoint.getY());
                 SVGPoint P1 = new SVGOMPoint(C.getX1(), C.getY1());
                 P1 = transformPoint(P1, mat);
                 SVGPoint P2 = new SVGOMPoint(C.getX2(), C.getY2());
                 P2 = transformPoint(P2, mat);
                 SVGPoint P3 = new SVGOMPoint(C.getX(), C.getY());
                 P3 = transformPoint(P3, mat);
 
                 // find a representative number of points from the curv
                 ArrayList<Point> points = Functions.findPointsCubicBezier(
                         new pt.inevo.encontra.geometry.Point(P0.getX(), P0.getY()),
                         new pt.inevo.encontra.geometry.Point(P1.getX(), P1.getY()),
                         new pt.inevo.encontra.geometry.Point(P2.getX(), P2.getY()),
                         new pt.inevo.encontra.geometry.Point(P3.getX(), P3.getY()),
                         CURVE_SEGMENTATION);
                 if (points.size() > 1)
                     points.remove(0);
                 // insert all representative points
                 for (Point p : points) {
                     primitive.addPoint(p.getX(), p.getY());
                 }
 
                 // set last point
                 lastPoint.setX(P3.getX());
                 lastPoint.setY(P3.getY());
                 if (firstPoint == null)
                     firstPoint = lastPoint;
             } else if (seg.getPathSegType() == SVGPathSeg.PATHSEG_CLOSEPATH) {
 //                if (firstPoint != lastPoint)
 //                    primitive.addPoint(firstPoint.getX(), firstPoint.getY());
 
                 // color and border extraction
                 setSVGElementProperties(path, primitive);
 
 //                for (Primitive lp : primitives) {
 //                    if (primitive.isInsidePrimitive(lp))
 //                        primitive.setFillColor(new Color(0,0,0,0));
 //                }
 
                 primitives.add(primitive);
                 primitive = new Primitive();
                 firstPoint = null;
             }
         }
 
         if (firstPoint != null) { // last primitive not added yet, no z found
             // color and border extraction
             setSVGElementProperties(path, primitive);
 
             primitives.add(primitive);
             firstPoint = null;
         }
 
         // add the new primitive and the primitives created from this node's children
         primitives.addAll(walkElement(root, path, depth));
 
         return primitives;
     }
 
     private SVGPoint transformPoint(SVGPoint point, SVGMatrix transform) {
         try {
             SVGPoint transformedPoint = point.matrixTransform(transform);
             return transformedPoint;
         } catch (NullPointerException e) {
             return point;
         }
     }
 
     private void setSVGElementProperties(SVGGraphicsElement element, Primitive primitive) {
         String svgId = element.getId();
         if (svgId == null || svgId == "") {
             svgId = "none";
         }
         primitive.setSvgId(svgId);
 
 //        if (primitive.getSvgId().equals("path6583")
 //                || primitive.getSvgId().equals("path1381")
 //                || primitive.getSvgId().equals("path2262")
 //                || primitive.getSvgId().equals("rect3440")) {
 //            System.out.println("test");
 //        }
 
         StyleMap style = element.getComputedStyleMap(null);
 
         Value opacityV = style.getValue(SVGCSSEngine.OPACITY_INDEX);
         Value fillV = style.getValue(SVGCSSEngine.FILL_INDEX);
         Value fillOpacityV = style.getValue(SVGCSSEngine.FILL_OPACITY_INDEX);
         Value strokeV = style.getValue(SVGCSSEngine.STROKE_INDEX);
         Value strokeWidthV = style.getValue(SVGCSSEngine.STROKE_WIDTH_INDEX);
         Value strokeOpacityV = style.getValue(SVGCSSEngine.STROKE_OPACITY_INDEX);
 
         Color fill = new Color(127, 127, 127, 127);
         Color stroke = new Color(127, 127, 127, 127);
         int opacity;
         int fillOpacity;
         int strokeWidth;
         int strokeOpacity;
         RGBColorValue tempColor;
 
         opacity = (opacityV == null || opacityV == ValueConstants.NONE_VALUE ? 255 :
                 (int) (opacityV.getFloatValue() * 255.0f));
         fillOpacity = (fillOpacityV == null || fillOpacityV == ValueConstants.NONE_VALUE ? opacity :
                 (int) (fillOpacityV.getFloatValue() * opacity));
         strokeWidth = (strokeWidthV == null || strokeWidthV == ValueConstants.NONE_VALUE ? 1 :
                 (int) strokeWidthV.getFloatValue());
         strokeOpacity = (strokeOpacityV == null || strokeOpacityV == ValueConstants.NONE_VALUE ? opacity :
                 (int) (strokeOpacityV.getFloatValue() * opacity));
 
         if (fillV == null || fillV == ValueConstants.NONE_VALUE) {
             fill = new Color(0, 0, 0, 0);
         } else if (fillV instanceof RGBColorValue) {
             tempColor = (RGBColorValue) fillV;
             fill = new Color((int) tempColor.getRed().getFloatValue(), (int) tempColor.getGreen().getFloatValue(), (int) tempColor.getBlue().getFloatValue(), fillOpacity);
         } else if (fillV instanceof ComputedValue) {
             ComputedValue cv = (ComputedValue) fillV;
             if ((Value) cv instanceof RGBColorValue) {
                 tempColor = (RGBColorValue) cv.getComputedValue();
                 fill = new Color((int) tempColor.getRed().getFloatValue(), (int) tempColor.getGreen().getFloatValue(), (int) tempColor.getBlue().getFloatValue(), fillOpacity);
             }
         }
         if (strokeV == null || strokeV == ValueConstants.NONE_VALUE) {
             strokeWidth = 0;
             stroke = fill;
         } else if (strokeV instanceof RGBColorValue) {
             tempColor = (RGBColorValue) strokeV;
             stroke = new Color((int) tempColor.getRed().getFloatValue(), (int) tempColor.getGreen().getFloatValue(), (int) tempColor.getBlue().getFloatValue(), strokeOpacity);
         } else if (strokeV instanceof ComputedValue) {
             ComputedValue cv = (ComputedValue) strokeV;
             if ((Value) cv instanceof RGBColorValue) {
                 tempColor = (RGBColorValue) cv.getComputedValue();
                 fill = new Color((int) tempColor.getRed().getFloatValue(), (int) tempColor.getGreen().getFloatValue(), (int) tempColor.getBlue().getFloatValue(), fillOpacity);
             }
         }
 
         primitive.setBorderColor(new Color(stroke));
         primitive.setFillColor(new Color(fill));
         primitive.setBorderWidth(strokeWidth);
     }
 
     /**
      * Returns a new Drawing object from the supplied SVG file.
      *
      * @param fileContent content of the SVG source file.
      * @return the drawing.
      */
     @Deprecated
    private Drawing drawingFromSVGContent(String fileContent) {
         // TODO Sandy: (1) SVG to Drawing. Ver: dejasvg.cpp::svg2drawing
         // É preciso melhorar isto, talvez inserir a z order em cada primitiva?
         // Esta é a versão do Dejavista. Dá para o gasto mas penso que poderia
         // fazer mais uso das tags do SVG. A cor, por exemplo.
         Drawing drawing = new Drawing(count++);
 
         int pathBegin, dTagBegin;
         String dpath, path;
         int end = 0;
         // while can find "<path"
         while ((pathBegin = fileContent.indexOf("<path", end)) != -1) {
             // search d="
             dTagBegin = fileContent.indexOf(" d=\"", pathBegin);
             // if not found, next loop
             if (dTagBegin == -1)
                 continue;
             // search '>'
             end = fileContent.indexOf('>', dTagBegin);
             // if not found, next loop
             if (end == -1)
                 continue;
             // if the d=" was before the '>', do things
             if (dTagBegin < end) {
                 // find closing-"
                 path = fileContent.substring(pathBegin, end);
                 end = fileContent.indexOf('\"', dTagBegin + 4);
                 if (end == -1)
                     continue;
                 // now the dpath is between the d=" and the "
                 dpath = fileContent.substring(dTagBegin + 4, end);
                 // add it to the sketch
                 //			cout << "path found ..." << endl;
                 //Primitive prim = readPrimitive(dpath);
                 Primitive primitive = primitiveFromPath(dpath);
                 primitive.setSvgId(extractId(path));
 
                 //check if last point = first point, because then it is closed=polygon
                 int last = primitive.getNumPoints() - 1;
 
                 if (last > 0) {
                     // if last point is the first point, then setClosed(true)
                     if (((primitive.getPoint(0).getX()) == (primitive.getPoint(last).getX())) &&
                             ((primitive.getPoint(0).getY()) == (primitive.getPoint(last).getY()))) {
                         primitive.setClosed(true);
                     }
                 }
 
 
                 if (primitive.getNumPoints() > 1) {
                     // cout << "path added" << endl;
                     // cout << drawing->getAllPrimitives()->size() << endl;
                     primitive.setBorderColor(new Color(0, 0, 0, 1));  //BLACK color
                     drawing.addPrimitive(primitive);
                     // cout << drawing->getAllPrimitives()->size() << endl;
                 } else {
                     System.err.println("path empty");
                 }
             }
         }
         double x, y, width, height;
         end = 0;
 
         while ((pathBegin = fileContent.indexOf("<rect", end)) != -1) {
             end = fileContent.indexOf('>', pathBegin) - 2;
             String thisRect = fileContent.substring(pathBegin, end);
 
             Primitive primitive = primitiveFromRect(thisRect);
             primitive.setSvgId(extractId(thisRect));
 
             //check if last point = first point, because then it is closed=polygon
             int last = primitive.getNumPoints() - 1;
 
             if (last > 0) {
                 // if last point is the first point, then setClosed(true)
                 if (((primitive.getPoint(0).getX()) == (primitive.getPoint(last).getX())) &&
                         ((primitive.getPoint(0).getY()) == (primitive.getPoint(last).getY()))) {
                     primitive.setClosed(true);
                 }
             }
 
             if (primitive.getNumPoints() > 1) {
                 // cout << "path added" << endl;
                 // cout << drawing->getAllPrimitives()->size() << endl;
                 primitive.setBorderColor(new Color(new Color(0, 0, 0, 1))); // BLACK color for now TODO falta a cor???
                 drawing.addPrimitive(primitive);
                 // cout << drawing->getAllPrimitives()->size() << endl;
             } else {
                 System.err.println("path empty");
             }
             // TODO Gabe & Sandy: Baki aqui!!!
         }
         if (this.simplified)
             drawing.simplify(simplification);
 //            drawing.simplify();
 
         return drawing;
     }
 
     /**
      * Converts a dpath from a SVG file to a primitive.
      *
      * @param dpath dpath from the SVG file.
      * @return the new primitive.
      */
     @Deprecated
     private Primitive readPrimitive(String dpath) {
         /*
 	 * M (absolute)
 	 * m (relative)	moveto	(x y)+
 	 *
 	 * Z or
 	 * z	closepath	(none)
 	 *
 	 * L (absolute)
 	 * l (relative)	lineto	(x y)+
 	 *
 	 * H (absolute)
 	 * h (relative)	horizontal lineto	x+
 	 *
 	 * V (absolute)
 	 * v (relative)	vertical lineto	y+
 	 *
 	 * C (absolute)
 	 * c (relative)	curveto	(x1 y1 x2 y2 x y)+
 	 *
 	 * S (absolute)
 	 * s (relative)	shorthand/smooth curveto	(x2 y2 x y)+
 	 *
 	 * Q (absolute)
 	 * q (relative)	quadratic Bézier curveto	(x1 y1 x y)+
 	 *
 	 * T (absolute)
 	 * t (relative)	Shorthand/smooth quadratic Bézier curveto	(x y)+
 	 *
 	 * A (absolute)
 	 * a (relative)	elliptical arc	(rx ry x-axis-rotation large-arc-flag sweep-flag x y)+
 	 */
 
         int i = 0;
         char chars[] = dpath.toCharArray();
 //	cout << "path: " << chars << endl;
 
         char prevc = 'M';
         double prevx = 0;
         double prevy = 0;
         int argc = 0;
 
         String num;
         double x = 0;
         double y = 0;
 
         Primitive prim = new Primitive();
 
         char n;
 
         // Read next command c
         char c = chars[i];
         while (c != 0) {
             while (c == ' ' || c == '\t' || c == '\n') {
                 c = chars[++i];
                 if (c == 0)
                     return prim;
             }
 
             if (c >= '0' && c <= '9') {
                 i--;
                 c = prevc;
             }
 //		cout << "Command found: " << c << endl;
             // How many arguments ?
             switch (c) {
                 case 'z':
                 case 'Z':
                     // do nothing
                     continue;
                 case 'h':
                 case 'H':
                 case 'v':
                 case 'V':
                     argc = 1;
                     break;
                 case 'm':
                 case 'M':
                 case 'l':
                 case 'L':
                 case 't':
                 case 'T':
                     argc = 2;
                     break;
                 case 's':
                 case 'S':
                 case 'q':
                 case 'Q':
                     argc = 4;
                     break;
                 case 'c':
                 case 'C':
                     argc = 6;
                     break;
                 case 'a':
                 case 'A':
                     argc = 7;
                     break;
                 default:
                     return prim;
             }
 
             // Get arguments
             for (int j = 0; j < argc; j++) {
                 num = "";
                 // Skip whitespaces
                 ++i;
                 while (chars[i] == ' ' || chars[i] == '\t' || chars[i] == '\n') {
                     if (chars[++i] == 0)
                         return prim;
                 }
                 // Get numbers
 
                 n = chars[i];
                 while (n >= '0' && n <= '9' || n == '.') {
                     num += n;
                     n = chars[++i];
                     if (n == 0)
                         return prim;
                 }
                 x = y;
                 // no round() in c++ ??!?
                 //y = floor(atof(num.c_str())+0.5);
                 y = Double.parseDouble(num);
 //			cout << "Argument: " << y << endl;
             }
             // Adjust coords
             switch (c) {
                 case 'h':
                     x = prevx + y;
                     y = prevy;
                     break;
                 case 'H':
                     x = y;
                     y = prevy;
                     break;
                 case 'v':
                     x = prevx;
                     y = prevy + y;
                     break;
                 case 'V':
                     x = prevx;
                     break;
                 default:
                     if (c >= 'a' && c <= 'z') {
                         x += prevx;
                         y += prevy;
                     }
             }
 
             if ((c == 'z') || (c == 'Z')) {
                 if (prim.getNumPoints() > 0) {
                     prim.addPoint(prim.getPoint(0).getX(), prim.getPoint(0).getY());
                 }
             } else {
                 // add point
                 prim.addPoint(x, y);
                 // save values
                 prevx = x;
                 prevy = y;
             }
             prevc = c;
             c = (i == chars.length - 1 ? 0 : chars[++i]);
         }
         return prim;
     }
 
     /**
      * Returns the id of a SVG object.
      *
      * @param str the string containing the object.
      * @return the id.
      */
     private String extractId(String str) {
        String id;
 
        Matcher m = findId.matcher(str);
        m.find();
        m = findAlphaNum.matcher(m.group());
        m.find();
        id = m.group().replaceAll("\"", "");
 
         return id;
     }
 
     /**
      * Converts a rectangle from a SVG file to a primitive.
      *
      * @param rect rectangle to be converted.
      * @return the new primitive.
      * @throws ParseException
      */
     private Primitive primitiveFromRect(String rect) {
         final Primitive primitive = new Primitive();
         double x, y, width, height;
 
         Matcher m = findX.matcher(rect);
         m.find();
         m = findNum.matcher(m.group());
         m.find();
         x = Math.round(Double.parseDouble(m.group()));
 
         m = findY.matcher(rect);
         m.find();
         m = findNum.matcher(m.group());
         m.find();
         y = Math.round(Double.parseDouble(m.group()));
 
         m = findWidth.matcher(rect);
         m.find();
         m = findNum.matcher(m.group());
         m.find();
         width = Math.round(Double.parseDouble(m.group()));
 
         m = findHeight.matcher(rect);
         m.find();
         m = findNum.matcher(m.group());
         m.find();
         height = Math.round(Double.parseDouble(m.group()));
 
         primitive.addPoint(x, y);
         primitive.addPoint(x + width, y);
         primitive.addPoint(x + width, y + height);
         primitive.addPoint(x, y + height);
         primitive.addPoint(x, y);
 
         return primitive;
     }
 
     /**
      * Converts a dpath from a SVG file to a primitive. This version uses Batik.
      *
      * @param dpath dpath from the SVG file.
      * @return the new primitive.
      */
     private Primitive primitiveFromPath(String dpath) throws ParseException {
         final Primitive primitive = new Primitive();
         final Point lastPoint = new Point(0, 0);
         final Point lastControlPoint = new Point(0, 0);
         PathParser pp = new PathParser();
         //PointsParser pp = new PointsParser();
         PathHandler ph = new PathHandler() {
             public void startPath() throws ParseException {
 
             }
 
             public void movetoRel(float x, float y) throws ParseException { // m
                 lastPoint.setX(lastPoint.getX() + x);
                 lastPoint.setY(lastPoint.getY() + y);
                 primitive.addPoint(lastPoint.getX(), lastPoint.getY());
                 // primitive.addPoint(lastPoint.getX(), lastPoint.getY());
             }
 
             public void movetoAbs(float x, float y) throws ParseException { // M
                 lastPoint.setX(x);
                 lastPoint.setY(y);
                 primitive.addPoint(lastPoint.getX(), lastPoint.getY());
             }
 
             public void linetoRel(float x, float y) throws ParseException { // l
                 lastPoint.setX(lastPoint.getX() + x);
                 lastPoint.setY(lastPoint.getY() + y);
                 primitive.addPoint(lastPoint.getX(), lastPoint.getY());
             }
 
             public void linetoAbs(float x, float y) throws ParseException { // L
                 lastPoint.setX(x);
                 lastPoint.setY(y);
                 primitive.addPoint(lastPoint.getX(), lastPoint.getY());
             }
 
             public void linetoHorizontalRel(float x) throws ParseException { // h
                 lastPoint.setX(lastPoint.getX() + x);
                 lastPoint.setY(lastPoint.getY());
                 primitive.addPoint(lastPoint.getX(), lastPoint.getY());
             }
 
             public void linetoHorizontalAbs(float x) throws ParseException { // H
                 lastPoint.setX(x);
                 lastPoint.setY(lastPoint.getY());
                 primitive.addPoint(lastPoint.getX(), lastPoint.getY());
             }
 
             public void linetoVerticalRel(float y) throws ParseException { // v
                 lastPoint.setX(lastPoint.getX());
                 lastPoint.setY(lastPoint.getY() + y);
                 primitive.addPoint(lastPoint.getX(), lastPoint.getY());
             }
 
             public void linetoVerticalAbs(float y) throws ParseException { // V
                 lastPoint.setX(lastPoint.getX());
                 lastPoint.setY(y);
                 primitive.addPoint(lastPoint.getX(), lastPoint.getY());
             }
 
             public void curvetoCubicRel(float x1, float y1,
                                         float x2, float y2,
                                         float x, float y) throws ParseException { // c
                 Point P0 = lastPoint;
 
                 Point P1 = new Point(lastPoint.getX() + x1, lastPoint.getY() + y1);
                 Point P2 = new Point(lastPoint.getX() + x2, lastPoint.getY() + y2);
                 Point P3 = new Point(lastPoint.getX() + x, lastPoint.getY() + y);
 
                 // find a representative number of points from the curv
                 ArrayList<Point> points = Functions.findPointsCubicBezier(P0, P1, P2, P3, CURVE_SEGMENTATION);
                 if (points.size() > 1)
                     points.remove(0);
                 // insert all representative points
                 for (Point p : points) {
                     primitive.addPoint(p.getX(), p.getY());
                 }
 
                 // set last control point
                 lastControlPoint.setX(P2.getX());
                 lastControlPoint.setY(P2.getY());
                 // set last point
                 lastPoint.setX(P3.getX());
                 lastPoint.setY(P3.getY());
             }
 
             public void curvetoCubicAbs(float x1, float y1,
                                         float x2, float y2,
                                         float x, float y) throws ParseException { // C
                 Point P0 = lastPoint;
                 Point P1 = new Point(x1, y1);
                 Point P2 = new Point(x2, y2);
                 Point P3 = new Point(x, y);
 
                 // find a representative number of points from the curv
                 ArrayList<Point> points = Functions.findPointsCubicBezier(P0, P1, P2, P3, CURVE_SEGMENTATION);
                 if (points.size() > 1)
                     points.remove(0);
                 // insert all representative points
                 for (Point p : points) {
                     primitive.addPoint(p.getX(), p.getY());
                 }
 
                 // set last control point
                 lastControlPoint.setX(P2.getX());
                 lastControlPoint.setY(P2.getY());
                 // set last point
                 lastPoint.setX(P3.getX());
                 lastPoint.setY(P3.getY());
             }
 
             public void curvetoCubicSmoothRel(float x2, float y2,
                                               float x, float y) throws ParseException { // s
                 Point P0 = lastPoint;
                 Point P1 = Functions.reflectControlPoint(lastControlPoint, lastPoint);
                 Point P2 = new Point(lastPoint.getX() + x2, lastPoint.getY() + y2);
                 Point P3 = new Point(lastPoint.getX() + x, lastPoint.getY() + y);
 
                 // find a representative number of points from the curv
                 ArrayList<Point> points = Functions.findPointsCubicBezier(P0, P1, P2, P3, CURVE_SEGMENTATION);
                 if (points.size() > 1)
                     points.remove(0);
                 // insert all representative points
                 for (Point p : points) {
                     primitive.addPoint(p.getX(), p.getY());
                 }
 
                 // set last control point
                 lastControlPoint.setX(P2.getX());
                 lastControlPoint.setY(P2.getY());
                 // set last point
                 lastPoint.setX(P3.getX());
                 lastPoint.setY(P3.getY());
             }
 
             public void curvetoCubicSmoothAbs(float x2, float y2,
                                               float x, float y) throws ParseException { // S
                 Point P0 = lastPoint;
                 Point P1 = Functions.reflectControlPoint(lastControlPoint, lastPoint);
                 Point P2 = new Point(x2, y2);
                 Point P3 = new Point(x, y);
 
                 // find a representative number of points from the curv
                 ArrayList<Point> points = Functions.findPointsCubicBezier(P0, P1, P2, P3, CURVE_SEGMENTATION);
                 if (points.size() > 1)
                     points.remove(0);
                 // insert all representative points
                 for (Point p : points) {
                     primitive.addPoint(p.getX(), p.getY());
                 }
 
                 // set last control point
                 lastControlPoint.setX(P2.getX());
                 lastControlPoint.setY(P2.getY());
                 // set last point
                 lastPoint.setX(P3.getX());
                 lastPoint.setY(P3.getY());
             }
 
             public void curvetoQuadraticRel(float x1, float y1,
                                             float x, float y) throws ParseException { // q
                 Point P0 = lastPoint;
                 Point P1 = new Point(lastPoint.getX() + x1, lastPoint.getY() + y1);
                 Point P2 = new Point(lastPoint.getX() + x, lastPoint.getY() + y);
 
                 // find a representative number of points from the curv
                 ArrayList<Point> points = Functions.findPointsQuadraticBezier(P0, P1, P2, CURVE_SEGMENTATION);
                 if (points.size() > 1)
                     points.remove(0);
                 // insert all representative points
                 for (Point p : points) {
                     primitive.addPoint(p.getX(), p.getY());
                 }
 
                 // set last control point
                 lastControlPoint.setX(P1.getX());
                 lastControlPoint.setY(P1.getY());
                 // set last point
                 lastPoint.setX(P2.getX());
                 lastPoint.setY(P2.getY());
             }
 
             public void curvetoQuadraticAbs(float x1, float y1,
                                             float x, float y) throws ParseException { // Q
                 Point P0 = lastPoint;
 
                 Point P1 = new Point(x1, y1);
 
                 Point P2 = new Point(x, y);
 
                 // find a representative number of points from the curv
                 ArrayList<Point> points = Functions.findPointsQuadraticBezier(P0, P1, P2, CURVE_SEGMENTATION);
                 if (points.size() > 1)
                     points.remove(0);
                 // insert all representative points
                 for (Point p : points) {
                     primitive.addPoint(p.getX(), p.getY());
                 }
 
                 // set last control point
                 lastControlPoint.setX(P1.getX());
                 lastControlPoint.setY(P1.getY());
                 // set last point
                 lastPoint.setX(P2.getX());
                 lastPoint.setY(P2.getY());
             }
 
             public void curvetoQuadraticSmoothRel(float x, float y)
                     throws ParseException { // t
                 Point P0 = lastPoint;
                 Point P1 = Functions.reflectControlPoint(lastControlPoint, lastPoint);
                 Point P2 = new Point(lastPoint.getX() + x, lastPoint.getY() + y);
 
                 // find a representative number of points from the curv
                 ArrayList<Point> points = Functions.findPointsQuadraticBezier(P0, P1, P2, CURVE_SEGMENTATION);
                 if (points.size() > 1)
                     points.remove(0);
                 // insert all representative points
                 for (Point p : points) {
                     primitive.addPoint(p.getX(), p.getY());
                 }
 
                 // set last control point
                 lastControlPoint.setX(P1.getX());
                 lastControlPoint.setY(P1.getY());
                 // set last point
                 lastPoint.setX(P2.getX());
                 lastPoint.setY(P2.getY());
             }
 
             public void curvetoQuadraticSmoothAbs(float x, float y)
                     throws ParseException { // T
                 Point P0 = lastPoint;
                 Point P1 = Functions.reflectControlPoint(lastControlPoint, lastPoint);
                 Point P2 = new Point(x, y);
 
                 // find a representative number of points from the curv
                 ArrayList<Point> points = Functions.findPointsQuadraticBezier(P0, P1, P2, CURVE_SEGMENTATION);
                 if (points.size() > 1)
                     points.remove(0);
                 // insert all representative points
                 for (Point p : points) {
                     primitive.addPoint(p.getX(), p.getY());
                 }
 
                 // set last control point
                 lastControlPoint.setX(P1.getX());
                 lastControlPoint.setY(P1.getY());
                 // set last point
                 lastPoint.setX(P2.getX());
                 lastPoint.setY(P2.getY());
             }
 
             public void arcRel(float rx, float ry,
                                float xAxisRotation,
                                boolean largeArcFlag, boolean sweepFlag,
                                float x, float y) throws ParseException { // a
                 Point start = lastPoint;
                 Point end = new Point(x + lastPoint.getX(), y + lastPoint.getY());
 
                 // find a representative number of points from the arc
                 ArrayList<Point> points = Functions.findPointsArc(start, rx, ry, xAxisRotation, largeArcFlag, sweepFlag, end, CURVE_SEGMENTATION);
                 if (points.size() > 1)
                     points.remove(0);
                 // insert all representative points
                 for (Point p : points) {
                     primitive.addPoint(p.getX(), p.getY());
                 }
 
                 // set last point
                 lastPoint.setX(end.getX());
                 lastPoint.setY(end.getY());
             }
 
             public void arcAbs(float rx, float ry,
                                float xAxisRotation,
                                boolean largeArcFlag, boolean sweepFlag,
                                float x, float y) throws ParseException { // A
                 Point start = lastPoint;
                 Point end = new Point(x, y);
 
                 // find a representative number of points from the arc
                 ArrayList<Point> points = Functions.findPointsArc(start, rx, ry, xAxisRotation, largeArcFlag, sweepFlag, end, CURVE_SEGMENTATION);
                 if (points.size() > 1)
                     points.remove(0);
                 // insert all representative points
                 for (Point p : points) {
                     primitive.addPoint(p.getX(), p.getY());
                 }
 
                 // set last point
                 lastPoint.setX(end.getX());
                 lastPoint.setY(end.getY());
             }
 
             public void closePath() throws ParseException {
 
             }
 
             public void endPath() throws ParseException {
 
             }
         };
         pp.setPathHandler(ph);
         pp.parse(dpath);
         return primitive;
     }
 }
