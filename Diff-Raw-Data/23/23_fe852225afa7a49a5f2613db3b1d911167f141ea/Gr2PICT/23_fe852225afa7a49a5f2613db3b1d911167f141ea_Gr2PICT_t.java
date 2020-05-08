 // dclap/gr2pict.java 
 // write java graphics calls to outstream in PICT format (apple macintosh)
 // d.gilbert, dec. 1996
 // based on PSgr by E.J. Friedman-Hill
 
 
 package dclap;
 // pack edu.indiana.bio.dclap;
 
 
 import java.awt.*;
 import java.awt.image.*;
 import java.io.*;
import java.text.AttributedCharacterIterator;
 import java.util.*;
 
 /**
  * Gr2PICT is a Graphics subclass that draws to PICT format.
  * @version 1.0
  * @author Don Gilbert
  */
 
 
 public class Gr2PICT extends java.awt.Graphics {
   public final static int CLONE = 49;
   protected final static int PAGEHEIGHT = 792;
   protected final static int PAGEWIDTH = 612;
 
   protected DataOutputStream os;  
   protected Color clr = Color.black;
   protected Font font = new 
   	Font("Times",Font.PLAIN,12);
   protected Rectangle clipr = new Rectangle(-30000,-30000,60000,60000);
   protected Point origin = new Point(0,0);
   protected boolean trouble = false;
   protected Graphics g;
   
   public Rectangle getClipBounds(){
 	Rectangle r = new Rectangle();
 	return r;
 }
public void drawString(java.text.AttributedCharacterIterator aa, int a, int b) {};
 
 public void setClip(int a, int b, int c, int d)
 	{return;}
 public void setClip( java.awt.Shape a)
 	{return;}
 // 4 JDK 1.1...un comment
 public java.awt.Shape getClip(){
 	Rectangle s = new Rectangle();
 	return (java.awt.Shape)s;
 }
 public void drawPolyline(int[] a, int[] b, int c){return;}
 public boolean drawImage(Image i, int a, int b, int c, int d, int e, int f, int g, int h, ImageObserver o){return true;}
 public boolean drawImage(Image i, int a, int b, int c, int d, int e, int f, int g, int h,  Color cl,ImageObserver o){return true;}
 
   /**
    * Constructs a new Gr2PICT Object. Unlike regular Graphics objects,
    * Gr2PICT contexts can be created directly.
    * @param o Output stream for PostScript output
    * @see #create
    */
 
 
   public Gr2PICT(OutputStream o, Graphics g) {
     os = new DataOutputStream(o);
     trouble = false;
     this.g = g;
     Rectangle r = g.getClipBounds();
     if (r == null) r = new 
     	Rectangle(0,0,PAGEWIDTH,PAGEHEIGHT);
     emitHeader(r.width,r.height);
     }
 
   public Gr2PICT(OutputStream o, 
   	Graphics g, int what) {
     os = new DataOutputStream(o);
     trouble = false;
     this.g = g;
     Rectangle r = g.getClipBounds();
     if (r == null) r = new 
     	Rectangle(0,0,PAGEWIDTH,PAGEHEIGHT);
     if (what != CLONE) 
     	emitHeader(r.width,r.height);
     }
 
   private int fAlign= 0;
   
   protected void emitbyte(int v) { 
     try { 
     	os.writeByte(v); fAlign++; 
     	}
     catch (IOException ex) { 
     	trouble = true; 
     	}
     }
   protected void emitword(int v) { 
     try { os.writeShort(v); }
     catch (IOException ex) { trouble = true; }
     }
   protected void emitint(int v) { 
     try { os.writeInt(v); }
     catch (IOException ex) { trouble = true; }
     }
   protected void emitstring(String s)  { 
     try { 
     os.writeBytes(s); 
     fAlign += s.length();
     }
     catch (IOException ex) { trouble = true; }
     }
 
   protected final void emitop(int op) { 
     if ((fAlign & 1) == 1) emitbyte(0); // pad to word size
     emitword(op);
     }
   
   protected final void emitcolor(Color c) {
     emitword(c.getRed() << 8);
     emitword(c.getGreen() << 8);
     emitword(c.getBlue() << 8);
     }
     
   protected final void emitrect(int x, int y, int width, int height) {
     emitword(y);
     emitword(x);
     emitword(y+height);
     emitword(x+width);
     }
 
   protected final void emitroundrect(
   	int opcode,int x, int y, 
     int width, int height, int arcWidth, 
     int arcHeight)
   {
     emitop(QD.oOvSize);
     emitword(arcHeight);
     emitword(arcWidth);
     emitop(opcode);
     emitrect(x,y,width,height);
   }
 
   protected void emitpolygon(Polygon p) {
     int polysize= 2 + 8 + p.npoints * 4;
     emitword(polysize);
     Rectangle r= p.getBounds();
     emitrect(r.x,r.y,r.width,r.height);
     for (int i=0; i<p.npoints; i++) {
       emitword(p.ypoints[i]);
       emitword(p.xpoints[i]);
       }
     }
 
   protected void emitcomment(int kind, int datasize, String data) {
     if (datasize==0) {
       emitop(QD.oShortComment);
       emitword(kind);
       }
     else {
       emitop(QD.oLongComment);
       emitword(kind);
       emitword(data.length());
       emitstring(data);
       }
     }
     
   public final void beginPicGroup() { emitcomment( QD.picGrpBeg,0,null); }
   public final void endPicGroup() { emitcomment( QD.picGrpEnd,0,null); }
 
   public void laserLine(int num, int denom) {
     // set laserwriter line width
     // use num=1, denom=4 for 1/300 dpi line
     //emitcomment( QD.setLineWidth, sizeof(lineSize), lineSize); 
     emitop(QD.oLongComment);
     emitword(QD.setLineWidth);
     emitword(4);
     emitword(num);
     emitword(denom);
     }
 
   
   /**
     Top of every PICT file
     */
 
   protected void emitHeader(int picwidth, int picheight) {
     // 512 byte header
     try {
       int buflen= 512;
       byte[] buf= new byte[buflen]; // java zeros buf !?
       os.write(buf, 0, buflen);
       }
     catch (IOException ex) { trouble = true; }
     
     int picbytes = 0;  // will zero here suffice !? seems to be working...
     emitword( picbytes);
     emitrect( 0,0, picwidth, picheight);
     emitop( QD.oVersion);
     emitword( QD.version2);
 
     // header from clarisdraw
     // C00   0x10 0xc00           :  (skip 24) FF FF FF FF FF FF  0  0 FF FF
 
     emitop( QD.oHeaderOp); // reserved header opcode, followed by 24 byte header
     emitint( -1); // total size in bytes or -1
     for (int i=0; i<4; i++) { emitword( -1); emitword(0); } // fixedpt bound box or -1
     emitint( -1); // reserved or -1
     
       //oDefHilite & clipRect of pict size as 1st op is usual but not required !?
     emitop(QD.oDefHilite);
     clipRect(clipr.x, clipr.y, clipr.width, clipr.height); 
     beginPicGroup();
     }
 
 
   
     ///////////// Graphics Public Interface ////////////
 
 
 
   /**
    * Creates a new Gr2PICT Object that is a copy of the original Gr2PICT Object.
    */
   public Graphics create() {
     Gr2PICT grpict = new Gr2PICT(os,g,CLONE);
     grpict.font = font;
     grpict.clipr = clipr;
     grpict.clr = clr;
     return (Graphics) grpict;
   }
   
   /**
    * Creates a new Graphics Object with the specified parameters,
    * based on the original
    * Graphics Object. 
    * This method translates the specified parameters, x and y, to
    * the proper origin coordinates and then clips the Graphics Object to the
    * area.
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the area
    * @param height the height of the area
    * @see #translate
    */
   public Graphics create(int x, int y, int width, int height) {
     Graphics g = create();
     g.translate(x, y);
     g.clipRect(0, 0, width, height);
     return g;
     }
 
   /**
    * Translates the specified parameters into the origin of
    * the graphics context. All subsequent
    * operations on this graphics context will be relative to this origin.
    * @param x the x coordinate
    * @param y the y coordinate
    * @see #scale
    */
 
   public void translate(int x, int y) {
     // ? do this internally, adjusting each emitted x,y?
     origin.x= x;
     origin.y= y;
     // or as emitted opcode ??
     emitop(QD.oOrigin);
     emitword(-x);
     emitword(-y);  
     }
 
 
   /**
    * Gets the current color.
    * @see #setColor
    */
   public Color getColor() {
     return clr;
     }
   
 
   /**
    * Sets the current color to the specified color. All subsequent graphics operations
    * will use this specified color.
    * @param c the color to be set
    * @see Color
    * @see #getColor
    */
    
   public void setColor(Color c) {
     if (c != null) clr = c;
     emitop(QD.oRGBFgCol);
     emitcolor(clr);
     }
 
   
   /**
    * Sets the default paint mode to overwrite the destination with the
    * current color.
    */
   public void setPaintMode() {
     emitop(QD.oPnMode);
     emitword(QD.patCopy); // or QD.patOr
     }
 
   /**
    * Sets the paint mode to alternate between the current color
    * and the new specified color.
    * @param c1 the second color
    */
   public void setXORMode(Color c1) {
     emitop(QD.oPnMode);
     emitword(QD.patXor);  
     if (c1 != null) {
       // set c1 as PICT HiliteColor & set HiliteMode !?
       emitop(QD.oHiliteMode);
       emitop(QD.oHiliteColor);
       emitcolor(c1);
       }
     }
 
   /**
    * Gets the current font.
    * @see #setFont
    */
   public Font getFont() {
     return font;
     }
 
   /**
    * Sets the font for all subsequent text-drawing operations.
    * @param font the specified font
    * @see Font
    * @see #getFont
    * @see #drawString
    * @see #drawBytes
    * @see #drawChars
    */
   public void setFont(Font f) {
     if (f != null) {
       this.font = f;
       
         // count # bytes in name + number + 
       String fname= font.getName();
       fname = "Times"; // HACK!
       // Make sure the font is really 
       // Times, for the purpose of printing!!
       int qdnum= QD.getQuickDrawFontNum(fname);
       if (qdnum >= 0) {
         emitop(QD.oTxFont);
         emitword(qdnum);
         }
       else {
         emitop(QD.oFontName);
         int len= fname.length() + 1 + 2 + 2; // length-byte + fontnum + #bytes total
         emitword(len);
         emitword(QD.fontnum++);
         emitstring(fname);
         }
 
       int face= 0;
       int style= font.getStyle();
       if ((style & Font.BOLD) != 0) face |= QD.bold;
       if ((style & Font.ITALIC) != 0) face |= QD.italic;
       emitop(QD.oTxFace);
       emitbyte(face);   
       
       emitop(QD.oTxSize);
       emitword(font.getSize()); 
       }
   }
 
 
   /**
    * Gets the current font metrics.
    * @see #getFont
    */
   public FontMetrics getFontMetrics() {
     return getFontMetrics(getFont());
   }
 
   /**
    * Gets the current font metrics for the specified font.
    * @param f the specified font
    * @see #getFont
    * @see #getFontMetrics
    */
   public FontMetrics getFontMetrics(Font f) {
     return g.getFontMetrics(f);
   }
 
 
   /** 
    * Returns the bounding rectangle of the current clipping area.
    * @see #clipRect
    */
   public Rectangle getBounds() {
     return clipr;
   }
 
   /** 
    * Clips to a rectangle. The resulting clipping area is the
    * intersection of the current clipping area and the specified
    * rectangle. Graphic operations have no effect outside of the
    * clipping area.
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @see #getClipRect
    */
   public void clipRect(int x, int y, int width, int height) {
     clipr = new Rectangle(x,y,width,height);
     emitop(QD.oClip);
     int rgnsize= 2 + 8; //size-word + sizeof(rect) + sizeof(region)??
     emitword(rgnsize);
     emitrect(x,y,width,height);
     }
 
   /**
    * Copies an area of the screen.
    * @param x the x-coordinate of the source
    * @param y the y-coordinate of the source
    * @param width the width
    * @param height the height
    * @param dx the horizontal distance
    * @param dy the vertical distance
    */
   public void copyArea(int x, int y, int width, int height, int dx, int dy) {
     //throw new RuntimeException("copyArea not supported");
    }
 
   /** 
    * Draws a line between the coordinates (x1,y1) and (x2,y2). The line is drawn
    * below and to the left of the logical coordinates.
    * @param x1 the first point's x coordinate
    * @param y1 the first point's y coordinate
    * @param x2 the second point's x coordinate
    * @param y2 the second point's y coordinate
    */
    
   public void drawLine(int x1, int y1, int x2, int y2) {
     emitop(QD.oLine);
     emitword(y1);
     emitword(x1);
     emitword(y2);
     emitword(x2);
     }
 
   /** 
    * Fills the specified rectangle with the current color. 
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @see #drawRect
    * @see #clearRect
    */
   public void fillRect(int x, int y, int width, int height) {
     emitop(QD.opaintRect);
     emitrect(x,y,width,height);
     }
 
   /** 
    * Draws the outline of the specified rectangle using the current color.
    * Use drawRect(x, y, width-1, height-1) to draw the outline inside the specified
    * rectangle.
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @see #fillRect
    * @see #clearRect
    */
   public void drawRect(int x, int y, int width, int height) {   
     emitop(QD.oframeRect);
     emitrect(x,y,width,height);
     }
   
   /** 
    * Clears the specified rectangle by filling it with the current background color
    * of the current drawing surface.
    * Which drawing surface it selects depends on how the graphics context
    * was created.
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @see #fillRect
    * @see #drawRect
    */
   public void clearRect(int x, int y, int width, int height) {
     emitop(QD.oeraseRect);
     emitrect(x,y,width,height);
     }
 
 
 
   /** 
    * Draws an outlined rounded corner rectangle using the current color.
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @param arcWidth the diameter of the arc
    * @param arcHeight the radius of the arc
    * @see #fillRoundRect
    */
   public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
     emitroundrect(QD.oframeRRect,x,y,width,height,arcWidth,arcHeight);
     }
 
   /** 
    * Draws a rounded rectangle filled in with the current color.
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @param arcWidth the diameter of the arc
    * @param arcHeight the radius of the arc
    * @see #drawRoundRect
    */
   public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
     emitroundrect(QD.opaintRRect,x,y,width,height,arcWidth,arcHeight);
     }
 
   /**
    * Draws a highlighted 3-D rectangle.
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @param raised a boolean that states whether the rectangle is raised or not
    */
   public void draw3DRect(int x, int y, int width, int height, boolean raised) {
     Color c = getColor();
     Color brighter = c.brighter();
     Color darker = c.darker();
 
     setColor(raised ? brighter : darker);
     drawLine(x, y, x, y + height);
     drawLine(x + 1, y, x + width - 1, y);
     setColor(raised ? darker : brighter);
     drawLine(x + 1, y + height, x + width, y + height);
     drawLine(x + width, y, x + width, y + height);
     setColor(c);
   }    
 
   /**
    * Paints a highlighted 3-D rectangle using the current color.
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @param raised a boolean that states whether the rectangle is raised or not
    */
   public void fill3DRect(int x, int y, int width, int height, boolean raised) {
     Color c = getColor();
     Color brighter = c.brighter();
     Color darker = c.darker();
 
     if (!raised) setColor(darker);
     fillRect(x+1, y+1, width-2, height-2);
     setColor(raised ? brighter : darker);
     drawLine(x, y, x, y + height - 1);
     drawLine(x + 1, y, x + width - 2, y);
     setColor(raised ? darker : brighter);
     drawLine(x + 1, y + height - 1, x + width - 1, y + height - 1);
     drawLine(x + width - 1, y, x + width - 1, y + height - 1);
     setColor(c);
   }    
 
   /** 
    * Draws an oval inside the specified rectangle using the current color.
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @see #fillOval
    */
   public void drawOval(int x, int y, int width, int height) {
     emitop(QD.oframeOval);
     emitrect(x,y,width,height);
     }
 
   /** 
    * Fills an oval inside the specified rectangle using the current color.
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @see #drawOval
    */
   public void fillOval(int x, int y, int width, int height) {
     emitop(QD.opaintOval);
     emitrect(x,y,width,height);
     }
 
 
 
   /**
    * Draws an arc bounded by the specified rectangle from startAngle to
    * endAngle. 0 degrees is at the 3-o'clock position.Positive arc
    * angles indicate counter-clockwise rotations, negative arc angles are
    * drawn clockwise. 
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @param startAngle the beginning angle
    * @param arcAngle the angle of the arc (relative to startAngle).
    * @see #fillArc
    */
   public void drawArc(int x, int y, int width, int height,
                       int startAngle, int arcAngle) {
     emitop(QD.oframeArc);
     emitrect(x,y,width,height);
     emitword(startAngle+90);
     emitword(arcAngle);
     }
 
   /** 
    * Fills an arc using the current color. This generates a pie shape.
    *
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the arc
    * @param height the height of the arc
    * @param startAngle the beginning angle
    * @param arcAngle the angle of the arc (relative to startAngle).
    * @see #drawArc
    */
   public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
     emitop(QD.opaintArc);
     emitrect(x,y,width,height);
     emitword(startAngle);
     emitword(arcAngle);
     }
 
     
   /** 
    * Draws a polygon defined by an array of x points and y points.
    * @param xPoints an array of x points
    * @param yPoints an array of y points
    * @param nPoints the total number of points
    * @see #fillPolygon
    */
   public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
     drawPolygon(new Polygon(xPoints, yPoints, nPoints));
     }
 
   /** 
    * Draws a polygon defined by the specified point.
    * @param p the specified polygon
    * @see #fillPolygon
    */
   public void drawPolygon(Polygon p) {
     emitop(QD.oframePoly);
     emitpolygon(p);
     }
   
   /** 
    * Fills a polygon with the current color.
    * @param xPoints an array of x points
    * @param yPoints an array of y points
    * @param nPoints the total number of points
    * @see #drawPolygon
    */
   public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
     fillPolygon(new Polygon(xPoints, yPoints, nPoints));
     }
 
   /** 
    * Fills the specified polygon with the current color.
    * @param p the polygon
    * @see #drawPolygon
    */
   public void fillPolygon(Polygon p) {
     emitop(QD.opaintPoly);
     emitpolygon(p);
     }
 
   /** 
    * Draws the specified String using the current font and color.
    * The x,y position is the starting point of the baseline of the String.
    * @param str the String to be drawn
    * @param x the x coordinate
    * @param y the y coordinate
    * @see #drawChars
    * @see #drawBytes
    */
   public void drawString(String str, int x, int y) {
     emitop(QD.oLongText);
     emitword(y);
     emitword(x);
     emitbyte(str.length());
     emitstring(str);
     }
     
   /** 
    * Draws the specified characters using the current font and color.
    * @param data the array of characters to be drawn
    * @param offset the start offset in the data
    * @param length the number of characters to be drawn
    * @param x the x coordinate
    * @param y the y coordinate
    * @see #drawString
    * @see #drawBytes
    */
   public void drawChars(char data[], int offset, int length, int x, int y) {
     drawString(new String(data, offset, length), x, y);
     }
 
   /** 
    * Draws the specified bytes using the current font and color.
    * @param data the data to be drawn
    * @param offset the start offset in the data
    * @param length the number of bytes that are drawn
    * @param x the x coordinate
    * @param y the y coordinate
    * @see #drawString
    * @see #drawChars
    */
   public void drawBytes(byte data[], int offset, int length, int x, int y) {
     // deprecated:
     //drawString(new String(data, 0, offset, length), x, y);
     drawString( new String(data,offset,length), x, y);
     }
 
 
 
 
 
   /******
     // possible data for imaging
     // hexadecimal digits
   protected final static char hd[] = {'0', '1', '2', '3', '4', '5', '6', '7',
                                       '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    // number of chars in a full row of pixel data
   protected final static int charsPerRow = 12*6;
   *******/
   
   
   public boolean doImage(Image img, int x, int y, int width, int height,
                            ImageObserver observer, Color bgcolor) {
   /******
     
     // This class fetches the pixels in its constructor.
     PixelConsumer pc = new PixelConsumer(img);
         
     y = transformY(y);
     os.println("gsave");
 
     os.println("% build a temporary dictionary");
     os.println("20 dict begin");
     emitColorImageProlog(pc.xdim);
 
     os.println("% lower left corner");
     os.print(x);
     os.print(" ");
     os.print(y);
     os.println(" translate");
 
     // compute image size. First of all, if width or height is 0, image is 1:1.
     if (height == 0 || width == 0) {
       height = pc.ydim;
       width = pc.xdim;
     }       
 
     os.println("% size of image");
     os.print(width);
     os.print(" ");
     os.print(height);
     os.println(" scale");
 
     os.print(pc.xdim);
     os.print(" ");
     os.print(pc.ydim);
     os.println(" 8");
 
     os.print("[");
     os.print(pc.xdim);
     os.print(" 0 0 -");
     os.print(pc.ydim);
     os.print(" 0 ");
     os.print(0);
     os.println("]");
 
     os.println("{currentfile pix readhexstring pop}");
     os.println("false 3 colorimage");
     os.println("");
 
 
     int offset, sleepyet=0;;
     // array to hold a line of pixel data
     char[] sb = new char[charsPerRow + 1];
 
       for (int i=0; i<pc.ydim; i++) {
         offset = 0;
         ++sleepyet;
         if (bgcolor == null) {
           // real color image. We're deliberately duplicating code here
           // in the interest of speed - we don't want to check bgcolor
           // on every iteration.
           for (int j=0; j<pc.xdim; j++) {
             int n = pc.pix[j][i];
             
             // put hex chars into string
             // flip red for blue, to make postscript happy.
             
             sb[offset++] = hd[(n & 0xF0)     >>  4];
             sb[offset++] = hd[(n & 0xF)           ];
             sb[offset++] = hd[(n & 0xF000)   >> 12];
             sb[offset++] = hd[(n & 0xF00)    >>  8];
             sb[offset++] = hd[(n & 0xF00000) >> 20];
             sb[offset++] = hd[(n & 0xF0000)  >> 16];
             
             if (offset >= charsPerRow) {
               String s = String.copyValueOf(sb, 0, offset);
               os.println(s);
               if (sleepyet > 5) {
                 try {
                   // let the screen update occasionally!
                   Thread.sleep(15);
                 } catch (java.lang.InterruptedException ex) {
                   // yeah, so?
                 }
                 sleepyet = 0;
               }
               offset = 0;
             }
           }
         } else {
           os.println("%FalseColor"); // was System.out.println
           // false color image.
           for (int j=0; j<pc.xdim; j++) {
             int bg =
               bgcolor.getGreen() << 16 + bgcolor.getBlue() << 8 + bgcolor.getRed();
             int fg =
               clr.getGreen() << 16 + clr.getBlue() << 8 + clr.getRed();
             
             int n = (pc.pix[j][i] == 1 ? fg : bg);
             
             // put hex chars into string
             
             sb[offset++] = hd[(n & 0xF0)     ];
             sb[offset++] = hd[(n & 0xF)     ];
             sb[offset++] = hd[(n & 0xF000)  ];
             sb[offset++] = hd[(n & 0xF00)   ];
             sb[offset++] = hd[(n & 0xF00000)];
             sb[offset++] = hd[(n & 0xF0000) ];
             
             if (offset >= charsPerRow) {
               String s = String.copyValueOf(sb, 0, offset);
               os.println(s);
               if (sleepyet > 5) {
                 try {
                   // let the screen update occasionally!
                   Thread.sleep(15);
                 } catch (java.lang.InterruptedException ex) {
                   // yeah, so?
                 }
                 sleepyet = 0;
               }
               offset = 0;
             }
           }
         }   
         // print partial rows
         if (offset != 0) {
           String s = String.copyValueOf(sb, 0, offset);
           os.println(s);
         }
       }
     
     os.println("");
     os.println("end");
     os.println("grestore");
   ************/
     return true;
   }
   
   /** 
    * Draws the specified image at the specified coordinate (x, y). If the image is 
    * incomplete the image observer will be notified later.
    * @param img the specified image to be drawn
    * @param x the x coordinate
    * @param y the y coordinate
    * @param observer notifies if the image is complete or not
    * @see Image
    * @see ImageObserver
    */
 
   public boolean drawImage(Image img, int x, int y,
                            ImageObserver observer) {
     return doImage(img, x, y, 0, 0, observer, null);
     }
   
   /**
    * Draws the specified image inside the specified rectangle. The image is
    * scaled if necessary. If the image is incomplete the image observer will be
    * notified later.
    * @param img the specified image to be drawn
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @param observer notifies if the image is complete or not
    * @see Image
    * @see ImageObserver
    */
   public boolean drawImage(Image img, int x, int y,
                            int width, int height, 
                            ImageObserver observer) {
     return doImage(img, x, y, width, height, observer, null);
     }
 
   /** 
    * Draws the specified image at the specified coordinate (x, y). If the image is 
    * incomplete the image observer will be notified later.
    * @param img the specified image to be drawn
    * @param x the x coordinate
    * @param y the y coordinate
    * @param bgcolor the background color
    * @param observer notifies if the image is complete or not
    * @see Image
    * @see ImageObserver
    */
 
   public boolean drawImage(Image img, int x, int y, Color bgcolor,
                            ImageObserver observer) {
     return doImage(img, x, y, 0, 0, observer, bgcolor);
     }
 
   /**
    * Draws the specified image inside the specified rectangle. The image is
    * scaled if necessary. If the image is incomplete the image observer will be
    * notified later.
    * @param img the specified image to be drawn
    * @param x the x coordinate
    * @param y the y coordinate
    * @param width the width of the rectangle
    * @param height the height of the rectangle
    * @param bgcolor the background color
    * @param observer notifies if the image is complete or not
    * @see Image
    * @see ImageObserver
    * 
    */
   public boolean drawImage(Image img, int x, int y,
                            int width, int height, Color bgcolor,
                            ImageObserver observer) {
     return doImage(img, x, y, width, height, observer, bgcolor);
     }
   
   /**
    * Disposes of this graphics context.  The Graphics context cannot be used after 
    * being disposed of.
    * @see #finalize
    */
   public void dispose() {
     endPicGroup();
     emitop(QD.oopEndPic);
     try { os.flush(); }
     catch (IOException ex) { trouble = true; }
     }
 
   /**
    * Disposes of this graphics context once it is no longer referenced.
    * @see #dispose
    */
   public void finalize() {
     super.finalize();
     dispose();
   }
 
   /**
    * Returns a String object representing this Graphic's value.
    */
   public String toString() {  
     return getClass().getName() + "[font=" + getFont() + ",color=" + getColor() + "]";
     }
 
   public boolean checkError() {
     return trouble;
     }
 


 }
 
 
