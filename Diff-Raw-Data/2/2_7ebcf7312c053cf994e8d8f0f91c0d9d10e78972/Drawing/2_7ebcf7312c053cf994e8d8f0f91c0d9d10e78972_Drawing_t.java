 package pt.inevo.encontra.drawing;
 
 import com.seisw.util.geom.Poly;
 import com.seisw.util.geom.PolyDefault;
 import org.apache.batik.bridge.BridgeContext;
 import org.apache.batik.bridge.DocumentLoader;
 import org.apache.batik.bridge.GVTBuilder;
 import org.apache.batik.bridge.UserAgentAdapter;
 import org.apache.batik.dom.svg.*;
 import org.apache.batik.gvt.GraphicsNode;
 import org.apache.batik.transcoder.TranscoderException;
 import org.apache.batik.transcoder.TranscoderInput;
 import org.apache.batik.transcoder.TranscoderOutput;
 import org.apache.batik.transcoder.image.ImageTranscoder;
 import org.apache.batik.transcoder.image.PNGTranscoder;
 import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
 import org.apache.batik.transcoder.wmf.tosvg.WMFTranscoder;
 import org.apache.batik.util.SVG12Constants;
 import org.apache.batik.util.XMLResourceDescriptor;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.svg.SVGDocument;
 import pt.inevo.encontra.drawing.util.Color;
 import pt.inevo.encontra.drawing.util.Functions;
 import pt.inevo.encontra.geometry.Point;
 
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class Drawing {
 
     private static Logger _log=Logger.getLogger(Drawing.class.getName());
     private static int THUMB_WIDTH = 128;
     private static int THUMB_HEIGHT = 128;
 
     public static enum FileType{ WMF,SVG }
 
     // Indagare
     public static final double MM_TO_PIXEL = 0.3;			//this constant is dependent of the screen resolution!
     public static final double PT_TO_PIXEL = 1.333;		//this constant is dependent of the screen resolution!
 
     //polygon/polyline level
     public static final double HUE_TOL = .015 * 360;       //tollerance for hue of color [0-360>, lucky number
     public static final double SATURATION_TOL = .20;      //tollerance for saturation of color [0-1], lucky number
     public static final double INTENSITY_TOL = .6;         //tollerance for intensity of color [0-1], lucky number
 
     //object level
     public static final double AREA_TOL = .00025;           //tollerance for area size, lucky number
     public static final double LINE_TOL = .20;            //total line length relative to length of the main-diagonal, lucky number
 
     //
     public static final double SURROUND_TOL_DIST = .006;       //tollerance for distance between two points of different vertices, lucky number (this tells something about how near they two points should be next to each other)
     public static final double SURROUND_TOL_POINTS = .9;     //tollerance for nr of points that should be within range of the points outer vertices, lucky number (this tells something about how many of the points should match the max_distance-requirement)
 
     //vertice level
     public static final double TOL = .002;                 //tollerance for inner vertices relative to length of the main-diagonal, lucky number (the bigger the simpeler)
     public static final double PRE_TOL = .00075;           //tollerance for inner vertices relative to length of the main-diagonal, lucky number (the bigger the simpeler) Used to simplify before clipping, should not exceed TOL
     public static final int MAX_COUNT_OPTIMIZE = 20;       //sets modulo for when to pre-optimize
     public static final int MAX_VERTICES = 1000;           //maximum number of vertices per Primitive
     public static final double OVER_TOL = 2;               //set <=1 to dissable, this makes it possible to over optimize object wich exist of more then MAX_VERTICES vectices
 
     //gradient elimination
     public static final double HUE_DIF = 25;				//maximum difference between hue values to consider similar colors
     public static final double SATURATION_DIF = .35;		//maximum difference between saturation values to consider similar colors
     public static final double VALUE_DIF = .15;			//maximum difference between value values to consider similar colors
 
 
     private List <Primitive> _list_primitives;	//!< List of all the Primitives
     private List <Primitive> _list_primitives_area; //!< List of all Primitivies ordered by area
 
     private int	_id;
 
     private  double	_height,_width; 			//!< height,width of the Drawing
 
     private String _svgNS; // SVG Namespace URI
     protected SVGDocument _document; // SVGDocument
 
     public void setId(int id) { _id = id; } //!< Sets this Drawing's id.
     public int	getId() { return _id; }; //!< Returns this Drawing's id.
 
     /** Default constructor.
      */
     public Drawing() {
         _width=0;
         _height=0;
         _id=-1;
         _list_primitives=new ArrayList<Primitive>();
 
         // Obtaining a DOM implementation
         DOMImplementation dom = SVGDOMImplementation.getDOMImplementation();
 
         // Creating a Document
         _svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
 
         _document = (SVGDocument)dom.createDocument(_svgNS, "svg", null);
     }
 
     public Drawing(String filename) {
         this();
 
         FileType type=null;
 
         // extension without the dot
         String ext;
 
         // where the last dot is. There may be more than one.
         int dotPlace = filename.lastIndexOf ( '.' );
 
         if ( dotPlace >= 0 )
         {
             // possibly empty
             ext = filename.substring( dotPlace + 1 );
 
             if(ext.equalsIgnoreCase("svg"))
                 type=FileType.SVG;
             else if(ext.equalsIgnoreCase("wmf"))
                 type=FileType.WMF;
         }
 
         File inputFile=new File(filename);
 
         switch(type) {
             case WMF:
                 createFromWMF(inputFile);
                 break;
             case SVG:
                 createFromSVG(inputFile);
                 break;
             default:
                 break;
         }
 
         initialize(); // Parse SVG and initialize
     }
 
     public void createFromSVG(String svg) {
 
         StringReader reader = new StringReader(svg);
 
         String parser = XMLResourceDescriptor.getXMLParserClassName();
         SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
 
         System.out.println("Create SVG Document");
         try {
             _document = f.createSVGDocument(_svgNS,reader);
 
             /*
                System.out.println("Created SVG Document=");
                SVGTranscoder transcoder2 = new SVGTranscoder();
                TranscoderInput svgInput = new TranscoderInput(_document);
                TranscoderOutput out =
                new TranscoderOutput(new OutputStreamWriter(System.out, "UTF-8"));
                transcoder2.transcode(svgInput, out);*/
 
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         /*catch (TranscoderException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }*/
 
     }
 
     public void createFromSVG(File file) {
 
         FileReader reader;
         try {
             reader = new FileReader(file);
 
             String parser = XMLResourceDescriptor.getXMLParserClassName();
             SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
 
             System.out.println("Create SVG Document");
 
             _document = f.createSVGDocument(_svgNS,reader);
 
 
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public void createFromWMF(File file) {
 
         WMFTranscoder transcoder = new WMFTranscoder();
         try {
             TranscoderInput input = new TranscoderInput(file.toURI().toString());
 
             StringWriter svgWriter=new StringWriter();
             TranscoderOutput output = new TranscoderOutput(svgWriter);
 
             transcoder.transcode(input,output);
 
             svgWriter.flush();
             String svgStr=svgWriter.toString();
 
             createFromSVG(svgStr);
 
         } catch (TranscoderException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     private void createRects() {
         NodeList list=_document.getElementsByTagName(SVG12Constants.SVG_RECT_TAG);
 
 
         for(int i=0; i<list.getLength(); i++){
             SVGOMRectElement rect=(SVGOMRectElement)list.item(i);
         }
     }
 
     private void createLines() {
         NodeList list=_document.getElementsByTagName(SVG12Constants.SVG_LINE_TAG);
 
          for(int i=0; i<list.getLength(); i++){
             SVGOMLineElement line=(SVGOMLineElement)list.item(i);
             Primitive prim = new Primitive(line);
             addPrimitive( prim );
         }
     }
 
     private void createPolylines() {
         NodeList list=_document.getElementsByTagName(SVG12Constants.SVG_POLYLINE_TAG);
 
 
         for(int i=0; i<list.getLength(); i++){
 
 
             SVGOMPolylineElement line=(SVGOMPolylineElement)list.item(i);
             Primitive prim = new Primitive(line);
             addPrimitive( prim );
         }
     }
     private void createPaths() {
         NodeList list=_document.getElementsByTagName(SVG12Constants.SVG_PATH_TAG);
 
         for(int i=0; i<list.getLength(); i++){
 
             SVGOMPathElement path=(SVGOMPathElement)list.item(i);
 
             //SVGMatrix m=SVGLocatableSupport.getCTM(path);
             //float a=m.getA();
 
             //System.out.println("Creating Primitive from path...");
             Primitive prim = new Primitive(path);
             //System.out.println("SVG Path="+prim.getSVG());
 
             //check if last point = first point, because then it is closed=polygon
             int last = prim.getNumPoints() - 1;
 
             if (last>0) {
                 // if last point is the first point, then setClosed(true)
                 if (((prim.getPoint(0).x)==(prim.getPoint(last).x)) &&
                         ((prim.getPoint(0).y)==(prim.getPoint(last).y))) {
                     prim.setClosed(true);
                 }
             }
 
 
             if (prim.getNumPoints()>1) {
                 // cout << "path added" << endl;
                 // cout << drawing->getPrimitives()->size() << endl;
                 addPrimitive( prim );
                 // cout << drawing->getPrimitives()->size() << endl;
             } else {
                 //cout << "path empty" << endl;
                 //delete prim;
             }
         }
     }
     public void initialize() {
 
         _log.info("Initializing Drawing...");
 
        // boot the CSS engine to get Batik to compute the CSS
         UserAgentAdapter userAgent = new UserAgentAdapter();
         DocumentLoader loader    = new DocumentLoader(userAgent);
         BridgeContext ctx       = new BridgeContext(userAgent, loader);
         ctx.setDynamicState(BridgeContext.DYNAMIC);
         GVTBuilder builder   = new GVTBuilder();
         GraphicsNode rootGN    = builder.build(ctx, _document);
 
         _log.info("Creating paths...");
         createPaths();
         _log.info("Creating lines...");
         createLines();
         _log.info("Creating polylines...");
         createPolylines();
         _log.info("Creating rects...");
         //createRects();
         _log.info("... drawing initialized!");
 
     }
 
 
     public String getSketch(){
         String sketchSVG;
 
         sketchSVG="<g>";
 
         Double minX=Double.POSITIVE_INFINITY, minY=Double.POSITIVE_INFINITY;
         Double maxX=Double.NEGATIVE_INFINITY, maxY=Double.NEGATIVE_INFINITY;
         for(Primitive primitive:getAllPrimitives()) {
             sketchSVG+=primitive.getSVG();
             if(primitive.getXmin()<minX) minX=primitive.getXmin();
             if(primitive.getYmin()<minY) minY=primitive.getYmin();
             if(primitive.getXmax()>maxX) maxX=primitive.getXmax();
             if(primitive.getYmax()>maxY) maxY=primitive.getYmax();
         }
         Double width=maxX-minX;
         Double height=maxY-minY;
 
         sketchSVG="<svg xmlns=\"http://www.w3.org/2000/svg\" width=\""+width.toString()+"\" height=\""+height.toString()+"\" viewBox=\""+minX.toString()+" "+minY.toString()+" "+width.toString()+" "+height.toString()+"\" >"+sketchSVG;
         sketchSVG+="</g></svg>";
 
         StringReader reader = new StringReader(sketchSVG);
 
         String parser = XMLResourceDescriptor.getXMLParserClassName();
         SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
 
         SVGDocument svgDocument;
 
         try {
             svgDocument = f.createSVGDocument(_svgNS,reader);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return sketchSVG;
     }
 
     public String getSVG() {
         String result="";
         StringWriter sw=new StringWriter();
         SVGTranscoder transcoder2 = new SVGTranscoder();
         TranscoderInput svgInput = new TranscoderInput(_document);
         TranscoderOutput out;
         try {
             out = new TranscoderOutput(sw);
             //new OutputStreamWriter(sw, "UTF-8")
             transcoder2.transcode(svgInput, out);
             result=sw.toString();
         } catch (TranscoderException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return result;
     }
 
     public SVGDocument getSVGDocument() {
         return _document;
     }
 
     /**
      * An image transcoder that stores the resulting image.
      */
     protected class Rasterizer extends ImageTranscoder {
 
         private BufferedImage _img;
 
         public BufferedImage getImage(){
             return _img;
         }
 
         public BufferedImage createImage(int w, int h) {
             return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
         }
 
         public void writeImage(BufferedImage img, TranscoderOutput output)
                 throws TranscoderException {
             _img = img;
         }
     }
 
     public BufferedImage getImage(int bitmap_resolution)
     {
         try {
             TranscoderInput input = new TranscoderInput(_document);
             Rasterizer r = new Rasterizer();
             r.addTranscodingHint(ImageTranscoder.KEY_WIDTH, new Float(bitmap_resolution));
             r.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, new Float(bitmap_resolution));
             r.addTranscodingHint(ImageTranscoder.KEY_BACKGROUND_COLOR, java.awt.Color.WHITE);
             r.transcode(input,null);
 
             return r.getImage();
 
         } catch (Exception e) {
 
             _log.log(Level.WARNING,"Error rasterizing!\n.",e);
         }
 
 
         return null;
     }
 
     public byte [] getPNG() {
         PNGTranscoder transcoder=new PNGTranscoder();
         TranscoderInput input = new TranscoderInput(_document);
         ByteArrayOutputStream outstream=new ByteArrayOutputStream();
         TranscoderOutput output = new TranscoderOutput(outstream);
         transcoder.addTranscodingHint(PNGTranscoder.KEY_MAX_WIDTH, new Float(THUMB_WIDTH));
         transcoder.addTranscodingHint(PNGTranscoder.KEY_MAX_HEIGHT, new Float(THUMB_HEIGHT));
         try {
             transcoder.transcode(input, output);
 
         } catch (TranscoderException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return outstream.toByteArray();
     }
     /** Destructor.
      *
 
      Drawing::~Drawing() {
      removeAllPrimitives();
      delete _list_primitives;
      }*/
 
     /** Gets all Primitives from the Drawing
      *
      * @return all primitives
      */
     public List<Primitive> getAllPrimitives() {
         return _list_primitives;
     }
 
     /**
      * Returns the primitives sorted on an axis (currently x).
      *
      * @return the primitives sorted on an axis (currently x).
      */
     public List<Primitive> getAllPrimitivesSorted() {
         // std::vector<Primitive*>
 
         List<Primitive> orderedPrimitives = new ArrayList<Primitive>();
 
         int size = _list_primitives.size();
 
         Primitive [] primitiveArray = new Primitive[size];
 
         // Copy pointers to a temporary array
         for(int i = 0; i < size; i++) {
             primitiveArray[i] = _list_primitives.get(i);
         }
 
         while(orderedPrimitives.size() != _list_primitives.size()) {
             double xPointer = Double.MAX_VALUE;//std::numeric_limits<double>::max();
             int indexPointer = Integer.MIN_VALUE;//std::numeric_limits<int>::min();
 
             for (int j = 0; j < size; j++) {
                 if(primitiveArray[j] != null) {
                     if(primitiveArray[j].getXmin() <= xPointer) {
                         xPointer = primitiveArray[j].getXmin();
                         indexPointer = j;
                     }
                 }
             }
             orderedPrimitives.add(_list_primitives.get(indexPointer)); // TODO check if it equals push_back();
             primitiveArray[indexPointer] = null;
         }
         return orderedPrimitives;
     }
 
     // Indagare
     /**
      * Returns the primitives sorted by area.
      *
      * @return the primitives sorted by area.
      * */
     List<Primitive> getAllPrimitivesSortedByArea() {
         // std::vector<Primitive*>
         List<Primitive> orderedPrimitives = new ArrayList<Primitive>();
 
         int size = _list_primitives.size();
 
         Primitive [] primitiveArray = new Primitive[size];
 
         // Copy pointers to a temporary array
         for(int i = 0; i < size; i++) {
             primitiveArray[i] = _list_primitives.get(i);
         }
 
         while(orderedPrimitives.size() != _list_primitives.size()) {
             double aPointer = Double.MAX_VALUE;
             int indexPointer = Integer.MIN_VALUE;
 
             for (int j = 0; j < size; j++) {
                 if(primitiveArray[j] != null) {
                     if(primitiveArray[j].getAreaSize() <= aPointer) {
                         aPointer = primitiveArray[j].getAreaSize();
                         indexPointer = j;
                     }
                 }
             }
             orderedPrimitives.add(_list_primitives.get(indexPointer));
             primitiveArray[indexPointer] = null;
         }
         return orderedPrimitives;
     }
 
     /** Sets all Primitives of the Drawing (removes all existing primitives in current drawing)
      *
      * @param primitives	primitives to be set in this drawing
      */
     public void setPrimitives(List <Primitive> primitives) {
         removeAllPrimitives();
         //delete _list_primitives;
         _list_primitives = primitives;
 //		cout << "Exiting setPrimitives" << endl;
     }
 
     /** Remove all Primitives of the Drawing
      *
      */
     public void removeAllPrimitives() {
 //		cout << "removing All Primitives" << endl;
 
         _list_primitives = new ArrayList<Primitive>();
 
 //		cout << "All Primitives removed" << endl;
     }
 
 
     /** return number of Primitives in the Drawing
      *
      * @return	number of primitives in the drawing
      */
     public int primitivesCount() {
         if (_list_primitives == null) {
             return -1;
         } else {
             return _list_primitives.size();
         }
     }
 
 
     /** adds a new Primitive to this drawing
      *  the Primitive WILL BE REMOVED BY THIS DRAWING OBJECT, when this drawing gets destructed
      *
      * @return pointer to the newly created primitive
      */
     public Primitive createNewPrimitive() {
 
         Primitive prim=new Primitive();
         addPrimitive(prim);
 
         return prim;
     }
 
 
     /** creates and returns pointer to a new Primitive and inserts it at a given location in the vector
      *  the Primitive WILL BE REMOVED BY THIS DRAWING OBJECT, when this drawing gets destructed
      *
      * @param i	location in the vector to insert the new primitive
      *
      * @return returns null if location doesn't exist
      */
     public Primitive insertNewPrimitive(int i) {
 
         Primitive prim=new Primitive();
         boolean success = insertPrimitive(prim, i);
 
         if (!success) {
             prim = null;
         }
 
         return prim;
     }
 
 
     /** Adds a Primitive to the Drawing
      *  Primitive WILL BE REMOVED BY THIS DRAWING OBJECT
      *
      * @param prim Primitive to be added to this drawing
      */
     public void addPrimitive(Primitive prim) {
         if (_list_primitives == null) {
             _list_primitives = new ArrayList<Primitive>();
         }
 
         prim.setId(_list_primitives.size()+1);
         _list_primitives.add(prim); // TODO check it equals - push_back(prim);
 
 //	    std::cout << " adding primitive " << prim->getId() << std::endl;
     }
 
 
     /** insert a Primitive to the Drawing at place i
      *  Primitive WILL BE REMOVED BY THIS DRAWING OBJECT
      *
      * @param prim Primitive to be added to this drawing
      * @param i	location in the vector to insert the primitive
      *
      * @return returns false if location doesn't exist
      */
     public boolean insertPrimitive(Primitive prim, int i) {
         if (_list_primitives == null) {
             _list_primitives = new ArrayList<Primitive>();
         }
 
         if ((i < 0) || (i > _list_primitives.size())) {
             return false;
         }
 
         prim.setId(_list_primitives.size()+1); //maybe resey the ID's of all the primitives
 //	    std::cout << "WARNING: is it bad that ID's aren't ascending?" <<std::endl;
 
         /* TODO - Does this do the same thing?
           ListIterator<Primitive> it=_list_primitives.listIterator();
 
           int j=0;
           for (j=0; j<i; j++) {
               it.next();
           }*/
         _list_primitives.add(i ,prim);
 
         return true;
     }
 
 
     /** Gets a Primitive from the Drawing
      *
      * @param idx	location of the primitive in the vector
      *
      * @return Pointer to the retrieved primitive, null if location i is invalid
      */
     public Primitive getPrimitive(int idx) {
         if (_list_primitives == null)
             return null;
         else
             return _list_primitives.get(idx);
     }
 
     /** Gets the total height of the Drawing
      *
      * @return height of the Drawing
      */
     public double getHeight() {
         if (_list_primitives!=null && _list_primitives.size()>0) {
             double ymin = _list_primitives.get(0).getYmin();
             double ymax = _list_primitives.get(0).getYmax();
             for (int i=1; i<_list_primitives.size(); i++) {
                 double min = _list_primitives.get(i).getYmin();
                 double max = _list_primitives.get(i).getYmax();
                 if (min<ymin) ymin=min;
                 if (max>ymax) ymax=max;
             }
             return Math.abs(ymax-ymin);
         } else return 0;
     }
 
 
     /** Gets the total width of the Drawing
      *
      * @return width of the Drawing
      */
     public double getWidth() {
         if (_list_primitives!=null && _list_primitives.size()>0) {
             double xmin = _list_primitives.get(0).getXmin();
             double xmax = _list_primitives.get(0).getXmax();
             for (int i=1; i<_list_primitives.size(); i++) {
                 double min = _list_primitives.get(i).getXmin();
                 double max = _list_primitives.get(i).getXmax();
                 if (min<xmin) xmin=min;
                 if (max>xmax) xmax=max;
             }
             return Math.abs(xmax-xmin);
         } else return 0;
     }
 
 
     /** Gets the length of the main diagonal of the Drawing
      *
      * @return length of the diagonal of this Drawing
      */
     public double getDiagonalLength() {
         return Functions.dist(getWidth(),getHeight());
     }
 
 
     /** Gets the total area of the Drawing
      *
      * @return area of the Drawing
      */
     public double getAreaSize() {
         return getWidth()*getHeight();
     }
 
 
     /** Retturns a string representation of this Drawing
      *
      * @return String representation of this Drawing
      */
     public String toString() {
         StringBuffer ss=new StringBuffer();
 
         ss.append("Drawing with id: " + _id );
         for (int i = 0; i < _list_primitives.size(); i++) {
             Primitive p = _list_primitives.get(i);
             assert(p!=null);
             String s = p.toString();
             ss.append(s);
         }
         return ss.toString();
     }
 
     /**
      * Simplifies the Drawing.
      * Calls simplification methods for each Primitive in the drawing.
      *
      */
     public void simplify()
     {
         System.out.println("Drawing: Simplify : reduceVertexCount");
         reduceVertexCount(TOL);
 
         System.out.println("Drawing: Simplify : removeSurroundingPrimitives");
         removeSurroundingPrimitives();
 
         // Indagare
         System.out.println("Drawing: Simplify :  gradientElimination");
         gradientElimination();
 
         System.out.println("Drawing: Simplify :  colorConcatPrimitives");
         colorConcatPrimitives();
 
         System.out.println("Drawing: Simplify : removeSmallPrimitives");
         removeSmallPrimitives();
 
     }
 
 
 
 
 
     // -------------------- simplification heuristics ------------------------------
 
     /** Indagare
      * Gradient elimination
      *
      * */
 
     void gradientElimination(){
 
         double	dist_xmin, dist_xmax, dist_ymin, dist_ymax, // distance between the boundaries of two polygons
                 dist_h, dist_s, dist_v;		// distancia between the HSV color components of two polygons
 
         double xmin_i, xmax_i, ymin_i, ymax_i, color_h_i, color_s_i, color_v_i, color_h_j, color_s_j, color_v_j;
 
         List<Primitive> lst_primitives_area = getAllPrimitivesSortedByArea();
 
         List<Primitive> new_list_primitives = new ArrayList<Primitive>();
 
         int size = this.primitivesCount();
         double area_max;
         double max_dist = this.getWidth() * 0.05;
 
         for(int i=0; i < size - 1; i++){
 
             Primitive p = lst_primitives_area.get(i);
             //assert(p);
 
             if(p.getBorderColor().is_set() || p.getFillColor().is_set()){
 
                 xmin_i = p.getXmin();
                 xmax_i = p.getXmax();
                 ymin_i = p.getYmin();
                 ymax_i = p.getYmax();
                 color_h_i = p.getFillColor()._h;
                 color_s_i = p.getFillColor()._s;
                 color_v_i = p.getFillColor()._v;
 
                 int j = i+1;
 
                 while(j < size){
                     Primitive p_j = lst_primitives_area.get(j);
 
                     area_max = 0.6*p_j.getAreaSize();
 
 
                     if((p_j.getAreaSize() - p.getAreaSize()) >= area_max)
                         break;
 
                     if(p_j.getBorderColor()._is_set || p_j.getFillColor()._is_set){
                         dist_xmin = Math.abs(xmin_i - p_j.getXmin());
                         dist_xmax = Math.abs(p_j.getXmax() - xmax_i);
                         dist_ymin = Math.abs(ymin_i - p_j.getYmin());
                         dist_ymax = Math.abs(p_j.getYmax() - ymax_i);
 
                         color_h_j = p_j.getFillColor()._h;
                         color_s_j = p_j.getFillColor()._s;
                         color_v_j = p_j.getFillColor()._v;
 
 
                         dist_h = Math.abs(color_h_i - color_h_j);
                         dist_s = Math.abs(color_s_i - color_s_j);
                         dist_v = Math.abs(color_v_i - color_v_j);
 
                         // Verify if the polygons have close boundaries and colors
                         if(	(dist_xmin >= 0) && (dist_xmin < max_dist) &&
                                 (dist_xmax >= 0) && (dist_xmax < max_dist) &&
                                 (dist_ymin >= 0) && (dist_ymin < max_dist) &&
                                 (dist_ymax >= 0) && (dist_ymax < max_dist) &&
                                 (dist_h >= 0) && (dist_h < HUE_DIF) &&
                                 (dist_s >= 0) && (dist_s < SATURATION_DIF) &&
                                 (dist_v >= 0) && (dist_v < VALUE_DIF)){
 
                             p.setBorderColor(new Color(0,0,0,false));
                             p.setFillColor(new Color(0,0,0,false));
                             break;
 
                         }
 
                     }
                     j++;
 
                 }
 
             }
         }
 
         int ii;
 
         for(ii=0; ii < size; ii++){
             Primitive p_i = this.getPrimitive(ii);
 
             if(p_i.getFillColor().is_set() || p_i.getBorderColor().is_set()){
                 new_list_primitives.add(p_i);
             }
             else
                 p_i=null;
 
         }
 
         _list_primitives.clear();
         _list_primitives = new_list_primitives;
     }
 
 
     /** Concatenate primitives with similar fill-color, and cuts-out primitives with different colors or borders
      * reducing primitive count by placing primitives in an exclude list, when its vertices are added to another primitive
      *
      */
     public void colorConcatPrimitives(){
 
         List <Primitive> new_list_primitives = new ArrayList<Primitive>();
 
         if (_list_primitives != null) {
 
             int nrToBeRemoved = 0;
             double pre_tol = PRE_TOL * this.getDiagonalLength();
 
             Primitive p_i = null;
             Primitive p_j = null;
 
             int p_i_points;
             int p_j_points;
 
             Color p_i_bcolor = null;
             Color p_i_fcolor = null;
             Color p_j_bcolor = null;
             Color p_j_fcolor = null;
 
             int i = 0;
             int j = 0;
 
             int countOptimize = 0;
 
 
             //search for a primitive which overlaps a already drawn primitive
             for (i = 1; i < this.primitivesCount(); i++) {
 
                 p_i = this.getPrimitive(i);
                 p_i_points = p_i.getNumPoints();
 
                 if (p_i_points == 0) {
                     p_i.setBorderColor(new Color(0,0,0,false));
                     p_i.setFillColor(new Color(0,0,0,false));
                 }
 
                 p_i_bcolor = p_i.getBorderColor();
                 p_i_fcolor = p_i.getFillColor();
 
                 //this is possible when there are objects inserted after an clipping operation
                 if (p_i_bcolor._is_set ||  p_i_fcolor._is_set) {
                     //get already drawn primitives (backwards)
                     for (j = i-1; j >=0 ; j--) {
 
                         p_j=this.getPrimitive(j);
                         p_j_points = p_j.getNumPoints();
 
                         if (p_j_points == 0) {
                             p_j.setBorderColor(new Color(0,0,0,false));
                             p_j.setFillColor(new Color(0,0,0,false));
                         }
 
                         p_j_bcolor = p_j.getBorderColor();
                         p_j_fcolor = p_j.getFillColor();
 
                         //skip test with primitives, which are already excluded
                         if ((p_i_bcolor._is_set ||  p_i_fcolor._is_set) &&
                                 (p_j_bcolor._is_set ||  p_j_fcolor._is_set)) {
 
                             //boundingbox test (if the boundin boxes don't collide, then we don't even have to look at the vertices)
 //	                        std::cout << "searching for colliding bounding boxes..." << std::endl;
                             if (p_i.boundingBoxesCollide(p_j)) {
 //	                            std::cout << "boundingboxes collide" << std::endl;
 
 
                                 //first case: two polygons // lines do still nothing
                                 if ( (p_i.isClosed()) && (p_j.isClosed()) ) {
 
                                     //color tests to eventually combine and exclude j (inner primitive)
                                     if ((p_i_fcolor._is_set) && (p_j_fcolor._is_set)) {
 
                                         //bordercolor test
                                         //combine methode
                                         //if bordercolor = fillcolor then remove bordercolor
                                         Color cb = null;
                                         Color cf = null;
                                         cb = p_i_bcolor;
                                         cf = p_i_fcolor;
                                         if ( (cb._is_set) && (cf._is_set) && (cb._r==cf._r) && (cb._g==cf._g) && (cb._b==cf._b) ) cb._is_set = false;
 
                                         cb = p_j_bcolor;
                                         cf = p_j_fcolor;
                                         if ( (cb._is_set) && (cf._is_set) && (cb._r==cf._r) && (cb._g==cf._g) && (cb._b==cf._b) ) cb._is_set = false;
 
 
                                         //SPEED Pre-optimalisation
                                         if ( countOptimize > MAX_COUNT_OPTIMIZE ) {
                                             if (p_i_points > MAX_VERTICES) {
                                                 p_i.poly_simplify(pre_tol);
                                                 if ((p_i_points > MAX_VERTICES) && (OVER_TOL > 1)) {
                                                     p_i.poly_simplify(pre_tol*OVER_TOL);
                                                 }
                                                 countOptimize=0;
                                             }
 
                                             if (p_j_points > MAX_VERTICES) {
                                                 p_j.poly_simplify(pre_tol);
                                                 if ((p_j_points > MAX_VERTICES) && (OVER_TOL > 1)) {
                                                     p_j.poly_simplify(pre_tol*OVER_TOL);
                                                 }
                                                 countOptimize=0;
                                             }
 //	                                        std::cout << "pre-opt done" << std::endl;
                                         }
                                         countOptimize++;
 
 
 
                                         //test if color is within range AND there is no border
                                         if (
                                                 (
                                                         ((Math.abs(p_i_fcolor._h - p_j_fcolor._h) < HUE_TOL)  || (2*Math.PI - Math.abs(p_i_fcolor._h - p_j_fcolor._h) < HUE_TOL))&&
                                                                 (Math.abs(p_i_fcolor._s - p_j_fcolor._s) < SATURATION_TOL) &&
                                                                 (Math.abs(p_i_fcolor._v - p_j_fcolor._v) < INTENSITY_TOL)
                                                 ) && (
                                                         (!p_i_bcolor._is_set) && (!p_j_bcolor._is_set)
                                                 )
 
                                                 ) {
                                             //merge p_j with p_i
                                             int insertedPrimitives = merge(i,j);
 
 //	                                        std::cout << " i=" << i << "/" << this.primitivesCount() << " j=" << j << std::endl;
 
 
                                             if (insertedPrimitives > 0) {
 
                                                 nrToBeRemoved += 2;
 
 //	                                            std::cout << "ik heb ge-unioned; " << insertedPrimitives << " polygonen toegevoegd, new_count:" << (this.primitivesCount() - nrToBeRemoved) << std::endl;
                                                 //set original i and j transparent, so it gets excluded
                                                 p_i.setBorderColor(new Color(255,255,255,false));
                                                 p_i.setFillColor(new Color(255,255,255,false));
                                                 p_j.setBorderColor(new Color(255,255,255,false));
                                                 p_j.setFillColor(new Color(255,255,255,false));
 
                                                 //update p_i to union of i and j
                                                 p_i = this.getPrimitive(i);
                                                 p_i_points = p_i.getNumPoints();
                                                 p_i_bcolor = p_i.getBorderColor();
                                                 p_i_fcolor = p_i.getFillColor();
                                             }
                                         } else { //color not in range or border seperates them
                                             //cut p_i out of p_j
                                             int insertedPrimitives = diff(i,j);
 
                                             nrToBeRemoved += 1;
 
 //	                                      std::cout << "ik heb geclipped; " << insertedPrimitives << " polygonen toegevoegd, new_count:" << (this.primitivesCount() - nrToBeRemoved) << std::endl;
                                             //set original j transparent, so it gets excluded
                                             p_j.setBorderColor(new Color(255,255,255,false));
                                             p_j.setFillColor(new Color(255,255,255,false));
 
 
                                             //the upper primitive has moved to i + insertedPrimitives
                                             i += insertedPrimitives;
 
                                             //p_i.setBorderColor(new Color(255,255,0,true));
                                             //p_i.setFillColor(new Color(255,0,255,false));
 
 
                                         } // end color.range test
 
 
 //	                                    std::cout << " i=" << i << "/" << this.primitivesCount() << " j=" << j << std::endl;
 
                                     } //end color.is_set test
                                 } //end polygon/line test
                             }//end boundingBoxesCollide test
                         } // end exclude contains j test
                     } //end j loop
                 } // end exclude contains i test
             } //end i loop
 
             //creating new drawing, without primitives which are in the exclude-list
             int ii;
             for(ii=0;ii<this.primitivesCount();ii++) {
                 Primitive p = this.getPrimitive(ii);
 
                 if (p.getBorderColor()._is_set || p.getFillColor()._is_set) {
                     new_list_primitives.add(p);//push_back(p);
                 } else {
                     //delete p;
                 }
             }
             //delete _list_primitives;
         }
 
         _list_primitives=new_list_primitives;
     }
 
 
 
     /** removes relatively small primitives from the drawing
      *
      */
     public void removeSmallPrimitives()
     {
 
         List <Primitive> new_list_primitives = new ArrayList<Primitive>();
 
         if (_list_primitives != null) {
 
             int size = this.primitivesCount();
 
             //based on percentage of the total area
             double threshold_size = AREA_TOL * getAreaSize();
 
             //optimize the drawing by removing small primitives
             for (int i = 0; i < size; i++) {
                 Primitive p = _list_primitives.get(i);
                 assert(p!=null);
 
 //	            std::cout << "simpl" << p.getId << "/" << size << std::endl;
 
                 // check if the primitive isn't invisible
                 if ((p.getFillColor()._is_set) || (p.getBorderColor()._is_set)) {
                     //add to new list, when there isn't an areaSize or when the areaSize is above the threshold
                     Double area=p.getAreaSize();
                     if ((area==-1) || (area > threshold_size)) {
                         p.setId(new_list_primitives.size()+1);
                         new_list_primitives.add(p); //push_back(p);
                     } else {
                         //delete p;
                     }
                 }
             }
             //delete _list_primitives;
         }
 
         _list_primitives = new_list_primitives;
     }
 
 
     /** Remove vertices from all primitives, which distance between each other are to small
      * if 2 edges where left and there distance between each other is to small too,
      * then the complete primitive is removed from the drawing
      *
      */
     public void reduceVertexCount(double tol) {
 
         List<Primitive> new_list_primitives = new ArrayList<Primitive>();
 
 
         if (_list_primitives != null) {
 
             double rel_tol = tol * this.getDiagonalLength();
             double rel_min_length = LINE_TOL * getDiagonalLength();
             Primitive p = null;
 
             //optimize all primitives by removing some small primitives and small lines withing primitives
             int size = this.primitivesCount();
             int i;
             for (i=0; i < size; i++) {
                 p = this.getPrimitive(i);
                 assert(p!=null);
 
 
 //	            std::cout << "simpl" << p.getId << "/" << size << std::endl;
 
                 p.poly_simplify(rel_tol);
 
                 //add to new list, when there are still points left and in case of lines, the total length of a line still is bigger then the threshold
                 if ( p.getNumPoints() > 0 ) {
                     if ( (p.isClosed()) || (p.getPerimeter() > rel_min_length) ) {
                         p.setId(new_list_primitives.size() + 1);
                         new_list_primitives.add(p);//push_back(p);
 
                     } else {
                         //delete p;
                     }
                 }
             }
 
             // delete _list_primitives;
         }
 
         _list_primitives = new_list_primitives;
     }
 
 
     /** tries to save the last (upper) polygon from their surrounding polygons
      * small lines which trace a part of some larger polygon/line will get removed too.
      *
      */
     public void removeSurroundingPrimitives()
     {
 
         List <Primitive> new_list_primitives = new ArrayList<Primitive>();
 
         if (_list_primitives != null) {
             double pre_tol = PRE_TOL * this.getDiagonalLength();
 
             double max_dist = SURROUND_TOL_DIST * this.getDiagonalLength(); // normalised maxdistance between points
             double max_dist2_allowed =  Math.pow(max_dist,2);
 
             int primitive_count = this.primitivesCount();
             Primitive p_i = null;
             Primitive p_j = null;
             int p_i_points;
             int p_j_points;
             Point point_i = null;
             Point point_j = null;
 
             ArrayList exclude_list_primitives=new ArrayList();
 
             for(int i=0;i<primitive_count;i++) {
 //	            std::cout << " i=" << i << std::endl;
                 p_i=this.getPrimitive(i);
 //	            p_i.poly_simplify(pre_tol);
 
                 p_i_points=p_i.getNumPoints();
                 double points_needed = SURROUND_TOL_POINTS * p_i_points;
 
                 for(int j=i+1;j<primitive_count;j++) {
 
                     if ((j!=i) && (!exclude_list_primitives.contains(j))) {
 
 //	                    std::cout << "  j=" << j << std::endl;
 
                         p_j=this.getPrimitive(j);
 //	                    p_j.poly_simplify(pre_tol);
                         p_j_points=p_j.getNumPoints();
 
                         double points_needed_j = SURROUND_TOL_POINTS*p_j_points;
 
                         int points_in_range = 0;
 
                         //loops through all points of i and j and removes add removes polygon i when all points of polygon j are less far from any point of i then max_dist, i!=j
                         for(int k=0; k<p_i_points; k++) {
                             //                    std::cout << "   k=" << k << std::endl;
                             point_i = p_i.getPoint(k);
 
                             //search for nearest point_j from point_i
                             for(int l=0; l<p_j_points; l++) {
                                 //                        std::cout << "    l=" << l << std::endl;
                                 point_j = p_j.getPoint(l);
                                 double cur_dist2 = point_i.DistanceTo(point_j);
 
                                 //                        std::cout << "     testing..." << std::endl;
                                 if (cur_dist2 <= max_dist2_allowed) {
                                     //                            std::cout << "     distance between points is small enough!" << std::endl;
                                     points_in_range++;
                                     break;
                                 }
                                 if (points_in_range >= points_needed) {
                                     break;
                                 }
 
                             } //end l
 
                             //if point
                             //if there are enough points in range the primitive gets excluded
                             if (points_in_range >= points_needed) {
                                 break;
                                 //if there are enough points in range for the other primitive the other primitive gets excluded (only possible for lines, not polygons)
                             } else if (points_in_range >= points_needed_j) {
                                 // only possible for lines, because we want the upper polygon, not the outer, because that is probably shadow  (after gradient color polygon combining)
                                 if (!(p_j.isClosed())) {
                                     //combine bordercolors if needed
                                     if (!(p_i.getBorderColor()._is_set)) {
                                         Color bc = p_j.getBorderColor();
                                         p_i.setBorderColor(new Color(bc._r,bc._g,bc._b,bc._is_set));
 
                                     }
 
                                     //exclude j (line in front) from the new primitive list
                                     exclude_list_primitives.add(j); //push_back(j);
                                 }
                                 // or if there aren't enough points left to get included eventually, the loop can be stopped
 //	                        } else if ( (points_in_range + ((p_j_points - 1) - l)) < points_needed) {
                                 //break; //EXCEPT FOR THE FACT THAT THIS ISN'T POSSIBLE ANYMORE DUE TO THE EXCLUDE J PART!!!
                             }
                         } // end k
 
                         // add point to include list if there are enough points in range and continue with the next primitive
                         if (points_in_range >= points_needed) {
                             /*
                                    std::cout << "excluding: " << points_in_range << "/" << points_needed << std::endl;
                                    std::cout << " bordercolor: " << (p_j.getBorderColor()._is_set) << std::endl;
                                    std::cout << " isClosed: " << (p_j.isClosed()) << std::endl;
                                    std::cout << " fillcolor: " << (p_j.getFillColor()._is_set) << std::endl;
 
            */
                             //combine colors if needed
                             if (!(p_j.getBorderColor()._is_set)) {
                                 Color bc = p_i.getBorderColor();
                                 p_j.setBorderColor(new Color(bc._r,bc._g,bc._b,bc._is_set));
                             }
 
                             if ((p_j.isClosed()) && (!(p_j.getFillColor()._is_set))) {
                                 Color fc = p_i.getFillColor();
                                 p_j.setFillColor(new Color(fc._r,fc._g,fc._b,fc._is_set));
                                 //if the one you keep isn't a polygon(but a polyline), BUT the one you exclude is
                             } else if ((p_i.isClosed()) && (!(p_i.getFillColor()._is_set))) {
                                 //then exclude the outer polyline, and keep the inner polygon
                                 if (!(p_i.getBorderColor()._is_set)) {
                                     Color bc = p_j.getBorderColor();
                                     p_i.setBorderColor(new Color(bc._r,bc._g,bc._b,bc._is_set));
                                 }
 
                                 exclude_list_primitives.add(j);//push_back(j);
                                 break;
                             }
 
                             //exclude i (one at the back) from the new primitive list
                             exclude_list_primitives.add(i);//push_back(i);
                             break;
                         }
                     } //ends test for doubles
                 } //end j
             } //end i
 
             for(int ii=0;ii<primitive_count;ii++) {
                 Primitive p = this.getPrimitive(ii);
 
                 if (!exclude_list_primitives.contains(ii)) {
                     //resetting the id of the primitive, so it keeps ascending
                     p.setId(_list_primitives.size()+1);
                     new_list_primitives.add(p);//push_back(p);
                 } else {
                     //delete p;
                 }
             }
 
             //delete _list_primitives;
         }
 
         _list_primitives=new_list_primitives;
 
     }
 
 
 
 
     /** helper function, which merges two primitives
      *
      * @param pr_i	first location to a Primitive in the Drawing to be merged
      * @param pr_j	second location to a Primitive in the Drawing to be merged
      *
      * @return number of primitives created by the merging process
      */
     public int merge(int pr_i, int pr_j) {
 
 //	    std::cout << "start union van " << pr_j << " with " << pr_i << std::endl;
 
         //make sure that i is the upper primitive
         if (pr_i<pr_j) {
             int t = pr_i;
             pr_i = pr_j;
             pr_j = pr_i;
         }
 
         Primitive p_i = this.getPrimitive(pr_i);
         Primitive p_j = this.getPrimitive(pr_j);
 
         //create ordered list of points from this
         int i_count = p_i.getNumPoints();
         int j_count = p_j.getNumPoints();
 
         int i=0;
         int j=0;
         int newPrimitives = 0;
         int insertedNewPrimitives = 0;
         boolean containsHole = false;
 
 
         Poly poly1=new PolyDefault();//gpc_polygon poly1=new gpc_polygon();
         Poly poly2=new PolyDefault();//gpc_polygon poly2=new gpc_polygon();
         //gpc_polygon result=new gpc_polygon();
 
         //gpc_vertex_list vertex_list1=new gpc_vertex_list();
         //gpc_vertex_list vertex_list2=new gpc_vertex_list();
 
 
         //gpc_vertex_array vertex_array1;
         //gpc_vertex_array vertex_array2;
 
 
         /*setting gpc-polygon default values
           poly1.setContour(null);
           poly1.setHole(null);
           poly1.setNum_contours(0);
 
           poly2.setContour(null);
           poly2.setHole(null);
           poly2.setNum_contours(0);
 
           result.setContour(null);
           result.setHole(null);
           result.setNum_contours(0); */
 
 
         //creating first poly
         //vertex_array1 =  new gpc_vertex_array(i_count);
         for(i=0; i<i_count; i++) {
             poly1.add(p_i.getPoint(i).x, p_i.getPoint(i).y);
             //gpc_vertex v=vertex_array1.getitem(i);
             //v.setX(p_i.getPoint(i).x);
             //v.setY(p_i.getPoint(i).y);
             //vertex_array1.setitem(i,v);
         }
         //vertex_list1 = new gpc_vertex_list();
         //vertex_list1.setNum_vertices(i_count);
         //vertex_list1.setVertex(vertex_array1.cast());
 
         //JNI_GPC.gpc_add_contour(poly1, vertex_list1, 0);
 
         //creating second poly
         //vertex_array2 = new gpc_vertex_array(j_count);
         for(i=0; i<j_count; i++) {
             poly2.add(p_j.getPoint(i).x, p_j.getPoint(i).y);
             //gpc_vertex v=vertex_array2.getitem(i);
             //v.setX(p_j.getPoint(i).x);
             //v.setY(p_j.getPoint(i).y);
             //vertex_array2.setitem(i,v);
         }
         //vertex_list2 = new gpc_vertex_list();
         //vertex_list2.setNum_vertices(j_count);
         //vertex_list2.setVertex(vertex_array2.cast());
 
         //JNI_GPC.gpc_add_contour(poly2,vertex_list2,0);
 
         Color tb = null;
         Color tf = null;
         Color t2b = null;
         Color t2f = null;
 
         tb = p_i.getBorderColor();
         tf = p_i.getFillColor();
         t2b = p_j.getBorderColor();
         t2f = p_j.getFillColor();
 
         // ---------- END OF INITIALISATION ---------
 
         System.out.println("POLY1");
         ((PolyDefault) poly1).print();
 
         System.out.println("POLY2");
         ((PolyDefault) poly2).print();
 
         //perform the requested operation
         Poly result=poly1.union(poly2);
         //JNI_GPC.gpc_polygon_clip(gpc_op.GPC_UNION, poly1,poly2,result);
 
         System.out.println("RESULT");
         ((PolyDefault) result).print();
 
         newPrimitives = result.getNumInnerPoly();
 
         /*
           //create and add new primitives from result
           for (j=0; j<newPrimitives; j++) {
               if (int_array.frompointer(result.getHole()).getitem(j) == 1) containsHole = true;
           }
 
           //cancel operation if the union failed (when it is the same as the original)
           if (((newPrimitives == 2) &&
               (int_array.frompointer(result.getHole()).getitem(0) == 0) &&
               (int_array.frompointer(result.getHole()).getitem(1) == 0)) || containsHole) {
               newPrimitives = 0;
           //else continue
           } else { */
         Primitive p_new;
 
         //create and add new primitives from result
         for (j=0; j<newPrimitives; j++) {
 
 //	                std::cout << "  hole: i=" << pr_i << " j=" << pr_j << " inserted at location:" << insert_place << std::endl;
 
             //if it isn't an hole, but a unified part
             boolean isHole=(result.getNumInnerPoly()>1)?false:result.isHole();
             if(!isHole) { //if (int_array.frompointer(result.getHole()).getitem(j) == 0) {
                 //create a new primitive and insert it at location of the lowest polygon
                 p_new = this.insertNewPrimitive(pr_j); //pr_i
                 insertedNewPrimitives++;
                 assert(p_new!=null);
 
 
                 //give it the avarage fill color of the original
                 if (t2f._is_set) {
                     p_new.setBorderColor(new Color((tb._r + t2b._r) / 2,(tb._g + t2b._g) / 2,(tb._b + tb._b) /2,tb._is_set));
                     p_new.setFillColor(new Color((tf._r + t2f._r) / 2,(tf._g + t2f._g) / 2,(tf._b + tf._b) /2,tf._is_set));
                 } else {
                     p_new.setBorderColor(new Color(tb._r,tb._g,tb._b,tb._is_set));
                     p_new.setFillColor(new Color(tf._r,tf._g,tf._b,tf._is_set));
                 }
 
             } else {
                 //when it is a hole, add it at the top and make it white
                 p_new = this.insertNewPrimitive(pr_i + insertedNewPrimitives);
                 assert(p_new!=null);
 
                 p_new.setBorderColor(new Color(tb._r,tb._g,tb._b,tb._is_set));
                 p_new.setFillColor(new Color(255,255,255,true));
             }
 
             // gpc_vertex_list contour=gpc_vertex_list_array.frompointer(result.getContour()).getitem(j);
             //create the polygon
             for(i=0; i<result.getNumPoints(); i++) {
                 //gpc_vertex vertex=gpc_vertex_array.frompointer(contour.getVertex()).getitem(i);
                 p_new.addPoint(result.getX(i),result.getY(i));
             }
             if (result.getNumPoints() != 0) {
                 //gpc_vertex vertex=gpc_vertex_array.frompointer(contour.getVertex()).getitem(0);
                 p_new.addPoint(result.getX(0),result.getY(0));
             }
 
             //confirm it's a polygon
             p_new.setClosed(p_i.isClosed());
 
         } // end for
 
         //} //end doPerform2
 
 
         // clean up
         /*
           result.delete();
           poly1.delete();
           poly2.delete();
 
           vertex_list1.delete();
           vertex_list2.delete();
 
           vertex_array1.delete();
           vertex_array2.delete();*/
 
 //	    std::cout << "finished union" << std::endl;
 
         return insertedNewPrimitives;
 
     }
 
 
     // TODO - diff with GPCJ
     /** helper function, which difers two primitives
      *
      * @param pr_i	first location to a Primitive in the Drawing
      * @param pr_j	second location to a Primitive in the Drawing to be distracted
      *
      * @return number of primitives created by the differentiation process
      */
     public int diff(int pr_i, int pr_j) {
 
 //	    std::cout << "start diff van " << pr_j << " with " << pr_i << std::endl;
 
 
         //make sure that i is the upper primitive
         if (pr_i<pr_j) {
             int t = pr_i;
             pr_i = pr_j;
             pr_j = pr_i;
         }
 
         Primitive p_i = this.getPrimitive(pr_i);
         Primitive p_j = this.getPrimitive(pr_j);
 
         //create ordered list of points from this
         int i_count = p_i.getNumPoints();
         int j_count = p_j.getNumPoints();
 
         int i=0;
         int j=0;
         int newPrimitives = 0;
         int insertedNewPrimitives = 0;
 
         Poly poly1=new PolyDefault();//gpc_polygon poly1=new gpc_polygon();
         Poly poly2=new PolyDefault();//gpc_polygon poly2=new gpc_polygon();
         //gpc_polygon result=new gpc_polygon();
 
         //gpc_vertex_list vertex_list1;
         //gpc_vertex_list vertex_list2;
         //gpc_vertex_array vertex_array1, vertex_array2;
 
         /*setting gpc-polygon default values
           poly1.setContour(null);
           poly1.setHole(null);
           poly1.setNum_contours(0);
 
           poly2.setContour(null);
           poly2.setHole(null);
           poly2.setNum_contours(0);
 
           result.setContour(null);
           result.setHole(null);
           result.setNum_contours(0);
           */
 
         //creating first poly
         //creating first poly
         //vertex_array1 = new gpc_vertex_array(i_count);
         for(i=0; i<i_count; i++) {
             poly1.add(p_i.getPoint(i).x, p_i.getPoint(i).y);
             //gpc_vertex v=vertex_array1.getitem(i);
             //v.setX(p_i.getPoint(i).x);
             //v.setY(p_i.getPoint(i).y);
             //vertex_array1.setitem(i,v);
         }
         //vertex_list1 = new gpc_vertex_list();
         //vertex_list1.setNum_vertices(i_count);
         //vertex_list1.setVertex(vertex_array1.cast());
 
         //JNI_GPC.gpc_add_contour(poly1, vertex_list1, 0);
 
         //creating second poly
         //vertex_array2 = new gpc_vertex_array(j_count);
         for(i=0; i<j_count; i++) {
             poly2.add(p_j.getPoint(i).x, p_j.getPoint(i).y);
             //gpc_vertex v=vertex_array2.getitem(i);
             //v.setX(p_j.getPoint(i).x);
             //v.setY(p_j.getPoint(i).y);
             //vertex_array2.setitem(i,v);
         }
         //vertex_list2 = new gpc_vertex_list();
         //vertex_list2.setNum_vertices(j_count);
         //vertex_list2.setVertex(vertex_array2.cast());
 
         //JNI_GPC.gpc_add_contour(poly2,vertex_list2,0);
 
         Color tb = null;
         Color tf = null;
         Color t2b = null;
         Color t2f = null;
 
         tb = p_i.getBorderColor();
         tf = p_i.getFillColor();
         t2b = p_j.getBorderColor();
         t2f = p_j.getFillColor();
 
         // ---------- END OF INITIALISATION ---------
 
         System.out.println("POLY1");
         ((PolyDefault) poly1).print();
 
         System.out.println("POLY2");
         ((PolyDefault) poly2).print();
 
 //	    if (newPrimitives==2) std::cout << "  nr contours:" << result.num_contours << " gat1?" << result.hole[0] << " gat2?" << result.hole[1] << std::endl;
 
 
         // ---- the real clipping -----
 
         //perform the requested operation
         //JNI_GPC.gpc_polygon_clip(gpc_op.GPC_DIFF,poly2,poly1,result);
         Poly result=poly2.diff(poly1);
 
         System.out.println("RESULT");
         ((PolyDefault) result).print();
 
         newPrimitives = result.getNumInnerPoly();//.getNum_contours();
 
 
         if (newPrimitives != 0) {
             Primitive p_new;
             boolean foundHole=false;
 
             //create and add new primitives from result
             for (j=0; j<newPrimitives; j++) {
 
                 //        std::cout << "  hole: i=" << pr_i << " j=" << pr_j << " inserted at location:" << insert_place << std::endl;
 
                 //set the new object if it isn't a hole
                 boolean isHole=(result.getNumInnerPoly()>1)?false:result.isHole();
 
                 if(!isHole) { //if (int_array.frompointer(result.getHole()).getitem(j) == 0) {
                     //create a new primitive which contains the result and insert it at the bottom
                     p_new = this.insertNewPrimitive(pr_j);//pr_j + insertedNewPrimitives);
                     insertedNewPrimitives++;
                     assert(p_new!=null);
 
                     //when creating (a) new primitive(s), let it contain all primitives that p_j contained (doesn't matter that this isn't always true, it is used to reset the height of the contained objects)
 
                     p_new.setBorderColor(new Color(t2b._r,t2b._g,t2b._b,t2b._is_set));
                     p_new.setFillColor(new Color(t2f._r,t2f._g,t2f._b,t2f._is_set));
 
                     //gpc_vertex_list contour=gpc_vertex_list_array.frompointer(result.getContour()).getitem(j);
                     //create the polygon
                     for(i=0; i<result.getNumPoints(); i++) {
                         //gpc_vertex vertex=gpc_vertex_array.frompointer(contour.getVertex()).getitem(i);
                         p_new.addPoint(result.getX(i),result.getY(i));
                     }
                     if (result.getNumPoints() != 0) {
                         //gpc_vertex vertex=gpc_vertex_array.frompointer(contour.getVertex()).getitem(0);
                         p_new.addPoint(result.getX(0),result.getY(0));
                     }
 
                     //confirm it's a polygon
                     p_new.setClosed(p_i.isClosed());
                 } else {
                     //if it is a hole
                     foundHole = true;
 
                     p_new = this.insertNewPrimitive(pr_j+insertedNewPrimitives+1);
                     insertedNewPrimitives++;
                     assert(p_new!=null);
 
                     //when creating (a) new primitive(s), let it contain all primitives that p_j contained (doesn't matter that this isn't always true, it is used to reset the height of the contained objects)
 
                     p_new.setBorderColor(new Color(t2b._r,t2b._g,t2b._b,t2b._is_set));
                     p_new.setFillColor(new Color(255,255,255,true));
 
                     //gpc_vertex_list contour=gpc_vertex_list_array.frompointer(result.getContour()).getitem(j);
                     //create the polygon
                     for(i=0; i<result.getNumPoints(); i++) {
                         //gpc_vertex vertex=gpc_vertex_array.frompointer(contour.getVertex()).getitem(i);
                         p_new.addPoint(result.getX(i),result.getY(i));
                     }
                     if (result.getNumPoints() != 0) {
                         //gpc_vertex vertex=gpc_vertex_array.frompointer(contour.getVertex()).getitem(0);
                         p_new.addPoint(result.getX(0),result.getY(0));
                     }
 
                     //confirm it's a polygon
                     p_new.setClosed(p_i.isClosed());
                 }
 
             } // end for
 
         }
 
         /*
           result.delete();
           poly1.delete();
           poly2.delete();
 
           vertex_list1.delete();
           vertex_list2.delete();
 
           vertex_array1.delete();
           vertex_array2.delete(); */
 
 //	    std::cout << "finished diff" << std::endl;
 
         return insertedNewPrimitives;
     }
 }
