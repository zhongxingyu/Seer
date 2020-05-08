 /*
 Copyright (c) 1998, 1999 Wabasoft  All rights reserved.
 
 This software is furnished under a license and may be used only in accordance
 with the terms of that license. This software and documentation, and its
 copyrights are owned by Wabasoft and are protected by copyright law.
 
 THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
 AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
 OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
 INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
 SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.
 
 WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
 MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
 ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
 YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
 */
 
 package waba.fx;
 
 import palmos.*;
 
 /**
  * Graphics draws on a surface.
  * <p>
  * Surfaces are objects that implement the ISurface interface.
  * MainWindow and Image are both examples of surfaces.
  * <p>
  * Here is an example that uses Graphics to draw a line:
  *
  * <pre>
  * public class MyProgram extends MainWindow
  * {
  * public void onPaint(Graphics g)
  *  {
  *  g.setColor(0, 0, 255);
  *  g.drawLine(0, 0, 10, 10);
  * ...
  * </pre>
  */
 
 public class Graphics
 {
    private static final int BLACK=0;
    private static final int WHITE=1;
 
    private static Rect rect;
    private ISurface surface;
    private Rect clip=null;
    private int tx=0;
    private int ty=0;
    private int col=BLACK;
    private int drawOp;
    private Font font=waba.ui.MainWindow.defaultFont;
 //   private boolean flgHasClip=false;
 //   private int iWinHandle=0;
 //   private int iOldWinHandle=0;
 //   private Rectangle oOldClip=new Rectangle();
 
    private void getFocus()
    {
       Palm.WinSetDrawWindow(surface.getWinHandle());
    }
 
   /**
    * Utility method for filling the parameters of a rectangle.  The returned
    * Rectangle should not be kept as it will be reused.
    */
    private static Rect getRect(int x,int y,int width,int height)
    {
      rect=new Rect(x,y,width,height);
      return rect;
    }
 
    private static Rectangle getRectangle(int x,int y,int width,int height)
    {
       Rectangle oRectangle=new Rectangle();
       oRectangle.topLeft_x=(short)x;
       oRectangle.topLeft_y=(short)y;
       oRectangle.extent_x=(short)width;
       oRectangle.extent_y=(short)height;
 
       return oRectangle;
    }
     /*
    private void drawStart(Rectangle oOldClip, int iOldWinHandle)
    {
       int iWinHandle=0;
 
       if(this.surface instanceof waba.fx.Image){
          iOldWinHandle=Palm.WinGetDrawWindow();
          iWinHandle=this.surface.getWinHandle();
          Palm.WinSetDrawWindow(iWinHandle);
       }
       else{
          iWinHandle=Palm.WinGetDrawWindow();
          iOldWinHandle=0;
       }
 
       if(flgHasClip){
          Palm.WinGetClip(oOldClip);
          Rectangle oRectangle=getRectangle(clip.x, clip.y, clip.width, clip.height);
          Palm.WinClipRectangle(oRectangle);
          Palm.WinSetClip(oRectangle);
       }
    }
     */
 /*
    private void drawStop(Rectangle oOldClip, int iOldWinHandle)
    {
       if(flgHasClip){
          Palm.WinSetClip(oOldClip);
       }
       if(iOldWinHandle!=0){
          Palm.WinSetDrawWindow(iOldWinHandle);
       }
    }
 */
 
    /**
     * The constant for a draw operation that draws the source over
     * the destination.
     */
    public static final int DRAW_OVER = 1;
 
    /**
     * The constant for a draw operation that AND's the source with the
     * destination. Commonly used to create image masks.
     */
    public static final int DRAW_AND = 2;
 
    /**
     * The constant for a draw operation that OR's the source with the
     * destination. Commonly used with image masks.
     */
    public static final int DRAW_OR = 3;
 
    /**
     * The constant for a draw operation that XOR's the source with the
     * destination.
     */
    public static final int DRAW_XOR = 4;
 
    /**
     * Constructs a graphics object which can be used to draw on the given
     * surface. For the sake of the methods in this class, the given surface
     * is known as the "current surface".
     * <p>
     * If you are trying to create a graphics object for drawing in a subclass
     * of control, use the createGraphics() method in the Control class. It
     * creates a graphics object and translated the origin to the origin of
     * the control.
     */
    public Graphics(ISurface surface)
 	{
 	   this.surface = surface;
 	   myWinHandle=surface.getWinHandle();
 
 	   setColor(0,0,0);
 
 	   /*
 	   curColor.r = (byte)0;
 	   curColor.g = (byte)0;
 	   curColor.b = (byte)0;
 	   blackIndex = Palm.WinRGBToIndex(curColor);
 	   */
 	}
 
     public Graphics(ISurface surface, int cacheSize)
     {
 	this(surface);
 	       
 	CACHE_SIZE = cacheSize;
 
 	colCache = new int [CACHE_SIZE];
 	colCacheI = new byte [CACHE_SIZE];
     }
 
    /**
     * Clears the current clipping rectangle. This allows drawing to occur
     * anywhere on the current surface.
     */
    public void clearClip()
    {
       //debug("clearClip");
       clip=null;
       Palm.WinSetClip(oOldClip);
    }
 
 
    /**
     * Copies a rectangular area from a surface to the given coordinates on
     * the current surface. The copy operation is performed by combining
     * pixels according to the setting of the current drawing operation.
     * The same surface can be used as a source and destination to
     * implement quick scrolling.
     * <p>
     * Not all combinations of surfaces are supported on all platforms.
     * PalmOS has problems copying from an Window surface to an Image and
     * between two Image surfaces. Java doesn't allow copying from an
     * Window surface to an Image.
     *
     * @param surface the surface to copy from
     * @param x the source x coordinate
     * @param y the source y coordinate
     * @param width the width of the area on the source surface
     * @param height the height of the area on the source surface
     * @param dstX the destination x location on the current surface
     * @param dstY the destination y location on the current surface
     * @see #setDrawOp
     */
    public void copyRect(ISurface src, int x, int y,
 	   int width, int height, int dstX, int dstY)
    {
        if(this != drawWinGraphics){
 	   setDrawWindow();
        }
      
        Rectangle rect=getRectangle(x,y,width,height);
        //here implement drawOp
        //here test to see if dstX and dstY need tx and ty added
        Palm.WinCopyRectangle(src.getWinHandle(), surface.getWinHandle(), rect, dstX+tx, dstY+ty, 0);
 
    }
 
 
    /**
     * Frees any system resources (native device contexts) associated with the
     * graphics object. After calling this function, the graphics object can no
     * longer be used to draw. Calling this method is not required since any
     * system resources allocated will be freed when the object is garbage
     * collected. However, if a program uses many graphics objects, free()
     * should be called whenever one is no longer needed to prevent allocating
     * too many system resources before garbage collection can occur.
     */
     public void free() {//should this free an associated image?
     }
 
 
    /**
     * Draws text at the given coordinates. The x and y coordinates specify
     * the upper left hand corner of the text's bounding box.
     * @param chars the character array to display
     * @param start the start position in array
     * @param count the number of characters to display
     * @param x the left coordinate of the text's bounding box
     * @param y the top coordinate of the text's bounding box
     */
    public void drawText(char chars[], int start, int count, int x, int y)
    {
      drawText(new String(chars,start,count),x,y);
    }
 
    /** Draws an image at the given x and y coordinates.*/
    public void drawImage(Image image, int x, int y)
    {
       copyRect(image, 0, 0, image.getWidth(), image.getHeight(), x, y);
    }
 
    static int cursorImage=0;
    static int cursorImageWidth,cursorImageHeight;
 
    /** Draws a cursor by XORing the given rectangular area on the surface.
      * Since it is XORed, calling the method a second time with the same
      * parameters will erase the cursor.
      */
    public void drawCursor(int x, int y, int width, int height)
    {
        if(this != drawWinGraphics){
 	   setDrawWindow();
        }
 
 
       int dstX = x+tx;
       int dstY = y+ty;
       Rectangle rect=getRectangle(0,0,width,height);
       ShortHolder err=new ShortHolder((short)0);
       if (cursorImage==0||cursorImageWidth!=width||cursorImageHeight!=height)
       {
          if (cursorImage!=0)
             Palm.WinDeleteWindow(cursorImage,false);
          cursorImage = Palm.WinCreateOffscreenWindow(width,height,0,err);
          cursorImageWidth=width;
          cursorImageHeight=height;
       }
       if (err.value!=0)
          return;
       int oldDraw=Palm.WinSetDrawWindow(cursorImage);
       Palm.WinDrawRectangle(rect, 0);
       Palm.WinSetDrawWindow(oldDraw);
       Palm.WinCopyRectangle(cursorImage, surface.getWinHandle(), rect, dstX, dstY, 3 );
 
       // Just for safety
       setDrawWindow();
    }
 
     private Rectangle oOldClip = new Rectangle();
     private static int iMWinHandle;
     private static Rectangle mwOldClip = new Rectangle();
     static Graphics drawWinGraphics = null;
     private int myWinHandle;
 
     public static void saveState()
     {
 	iMWinHandle =  Palm.WinGetDisplayWindow(); //assuming this is the right window.. could be bad
 	Palm.WinGetClip(mwOldClip);
     }
 
 
     public static void restoreState()
     {
          Palm.WinSetDrawWindow(iMWinHandle);
          Palm.WinSetClip(mwOldClip);
     }
 
	public static void clearDrawWindow()
	{
	if(drawWinGraphics != null){
	    Palm.WinSetClip(drawWinGraphics.oOldClip);
	}
	drawWinGraphics = null;		
	}

     public void setDrawWindow()
     {
 	if(drawWinGraphics != null){
 	    Palm.WinSetClip(drawWinGraphics.oOldClip);
 	}
 	drawWinGraphics = this;
 	Palm.WinSetDrawWindow(myWinHandle);
 	Palm.WinGetClip(oOldClip);
 	setClip();
 	Palm.WinSetForeColor(curColIndex);
     }
 
    /**
     * Draws a line at the given coordinates. The drawing includes both
     * endpoints of the line.
     */
    public void drawLine(int x1, int y1, int x2, int y2)
    {
        if(this != drawWinGraphics){
 	   setDrawWindow();
        }
 
        if (col==BLACK)
 	   Palm.WinDrawLine(x1+tx,y1+ty,x2+tx,y2+ty);
        else
 	   Palm.WinEraseLine(x1+tx,y1+ty,x2+tx,y2+ty);
        
    }
     /*
    public void drawPath(int points[], int start, int count)
     {
 	    int iOldWinHandle=0;
 	    int iWinHandle=0;
 	    
 	    if(this.surface instanceof waba.fx.Image){
 		iOldWinHandle=iMWinHandle;
 		iWinHandle=this.surface.getWinHandle();
 		Palm.WinSetDrawWindow(iWinHandle);
 	    }
 	    else{
 		Palm.WinSetDrawWindow(iMWinHandle);
 		iOldWinHandle=0;
 	    }
 	    
 	    if(flgHasClip){
 		Palm.WinGetClip(oOldClip);
 		Rectangle oRectangle=getRectangle(clip.x, clip.y, clip.width, clip.height);
 		Palm.WinClipRectangle(oRectangle);
 		Palm.WinSetClip(oRectangle);
 	    }
 
 	    if (count < 2)
    		return;
 	    int i = start;
 	    for (; i < count - 2; i+=2)
 		Palm.WinDrawLine(points[i]+tx,points[i+1]+ty,points[i+2]+tx,points[i+3]+ty);
 
 	    if(flgHasClip){
 		Palm.WinSetClip(oOldClip);
 	    }
 	    if(iOldWinHandle!=0){
 		Palm.WinSetDrawWindow(iOldWinHandle);
 	    }
 
     }
     */
    /**
     * Draws a rectangle at the given coordinates.
     */
    public void drawRect(int x, int y, int width, int height)
    {
    	// NOTE: only valid for drawing rects with width >=1, height >= 1
    	int x2 = x + width - 1;
    	int y2 = y + height - 1;
    	drawLine(x, y, x2 - 1, y);
    	drawLine(x2, y, x2, y2 - 1);
    	drawLine(x2, y2, x + 1, y2);
    	drawLine(x, y2, x, y + 1);
   	}
 
    public String toString()
    {
       return "Graphics("+tx+","+ty+")"; //here
    }
 
    /**
     * Draws a dotted line at the given coordinates. Dotted lines must
     * be either horizontal or vertical, they can't be drawn at arbitrary angles.
     */
    public void drawDots(int x1, int y1, int x2, int y2)
    {
       if (x1 == x2)
       {
          // vertical
          if (y1 > y2)
          {
             int y = y1;
             y1 = y2;
             y2 = y;
          }
          for (; y1 <= y2; y1 += 2)
             drawLine(x1,y1,x1,y1);
       }
       else if (y1 == y2)
       {
          // horitzontal
          if (x1 > x2)
          {
             int x = x1;
             x1 = x2;
             x2 = x;
          }
          for (; x1 <= x2; x1 += 2)
             drawLine(x1,y1,x1,y1);
       }
    }
 
    /**
     * Draws the outline of a polygon with the given coordinates.
     * The polygon is automatically closed, you should not duplicate
     * the start point to close the polygon.
     * @param x x vertex coordinates
     * @param y y vertex coordinates
     * @param count number of vertices
     */
    public void drawPolygon(int x[], int y[], int count)
 	{
 	   if (count < 3)
    		return;
    	int i = 0;
    	for (; i < count - 1; i++)
    		drawLine(x[i], y[i], x[i + 1], y[i + 1]);
    	drawLine(x[i], y[i], x[0], y[0]);
 	}
 
    /**
     * Draws a filled polygon with the given coordinates.
     * The polygon is automatically closed, you should not duplicate
     * the start point to close the polygon. The polygon is filled
     * according to Jordan's rule - a point is inside if a horizontal
     * line to the point intersects the polygon an odd number of times.
     * This function is not implemented for the PalmOS VM. Under PalmOS only
     * the outline of the polygon is drawn.
     * @param x x vertex coordinates
     * @param y y vertex coordinates
     * @param count number of vertices
     */
    public void fillPolygon(int x[], int y[], int count)
    {
       drawPolygon(x,y,count);
    }
 
 
    /**
     * Draws text at the given coordinates. The x and y coordinates specify
     * the upper left hand corner of the text.
     */
    public void drawText(String s, int x, int y)
    {
       if(this != drawWinGraphics){
 	  setDrawWindow();
       }
 
       //font.select();
       Palm.FntSetFont(this.font.getStyle());
 
       if (col==BLACK)
          Palm.WinDrawChars(s,s.length(),tx+x,ty+y); //here
       else
          Palm.WinEraseChars(s,s.length(),tx+x,ty+y); //here
 
    }
 
    /**
     * Fills a rectangular area with the current color.
     */
    public void fillRect(int x, int y, int width, int height)
    {
 
       if(this != drawWinGraphics){
 	  setDrawWindow();
       }
 
       Rectangle oRect=getRectangle(x+tx,y+ty,width,height);
       if (col==BLACK)
          Palm.WinDrawRectangle(oRect,0);
       else
          Palm.WinEraseRectangle(oRect,0);
 
    }
 
    /**
     * Sets a clipping rectangle. Anything drawn outside of the rectangular
     * area specified will be clipped. Setting a clip overrides any previous clip.
     */
    public void setClip(int x, int y, int width, int height)
    {
 //      String sDebug="setClip " + x + "," + y + "," + width + "," + height;
 //      debug(sDebug);
 
       clip=new Rect(x+tx,y+ty,width,height);
       if(this != drawWinGraphics){
 	  setDrawWindow();
       } else {
 	  setClip();
       }
    }
 
     private void setClip()
     {
 	if(clip != null){
 	    Palm.WinSetClip(oOldClip);
 	    Rectangle oRectangle=getRectangle(clip.x, clip.y, clip.width, clip.height);
 	    Palm.WinClipRectangle(oRectangle);
 	    Palm.WinSetClip(oRectangle);
 	}
     }
 
    /**
     * Sets the x, y, width and height coordinates in the rectangle passed
     * to the current clip coordinates. To reduce the use of temporary objects
     * during drawing, this method does not allocate its own rectangle
     * object. If there is no current clip, null will be returned and
     * the rectangle passed will remain unchanged. Upon success, the
     * rectangle passed to the method will be returned.
     */
    public Rect getClip(Rect r)
    {
 //      debug("getClip");
       if(clip==null || r==null){
          return null;
       }
       else{
          r.x = clip.x-tx;
          r.y = clip.y-ty;
          r.width=clip.width;
          r.height=clip.height;
          return r;
       }
    }
 
    /**
     * Sets the current color for drawing operations.
     * @param r the red value (0..255)
     * @param g the green value (0..255)
     * @param b the blue value (0..255)
     */
     static RGBColor curColor = new RGBColor(0,0,0,0);
     int CACHE_SIZE = 16;
     int [] colCache = new int [CACHE_SIZE];
     byte [] colCacheI = new byte [CACHE_SIZE];
     int numColsCached = 0;
     int curCol = 0;
     static int blackIndex;
     byte curColIndex = 0;
 
    public void setColor(int r, int g, int b)
    {
        
        if(this != drawWinGraphics){
 	   setDrawWindow();
        }
 
        if (r==255&&g==255&&b==255){
 	   col=WHITE;
 	   return;
        }
        else
 	   col=BLACK;
        
 
        int i;
        byte closeIndex;
        
        int reqColor = (r << 16) | (g << 8) | b;
        for(i=0; i < numColsCached; i++){
 	   if(colCache[i] == reqColor){
 	       Palm.WinSetForeColor(colCacheI[i]);
 	       curColIndex = colCacheI[i];
 	       return;
 	   }
        }
        
 
        curColor.r = (byte)r;
        curColor.g = (byte)g;
        curColor.b = (byte)b;
 
        closeIndex = Palm.WinRGBToIndex(curColor);
        Palm.WinSetForeColor(closeIndex);
        curColIndex = closeIndex;
 
        colCache[curCol] = reqColor;
        colCacheI[curCol] = closeIndex;
        curCol = (curCol + 1) % CACHE_SIZE;
        if(numColsCached < CACHE_SIZE) numColsCached++;
 
    }
 
    /**
     * Sets the drawing operation. The setting determines the raster
     * operation to use when drawing lines, rectangles, text and
     * images on the current surface. It also determines how pixels are
     * combined when copying one surface to another. The setting of
     * DRAW_OVER, where any drawing simply draws over the pixels on
     * a surface, is the default.
     * <p>
     * Not all operations are supported on all platforms. When used with
     * Java, DRAW_OVER is supported for all types of drawing and DRAW_XOR
     * is supported for drawing lines, rectangles, text and images.
     * However, DRAW_XOR is not supported when copying surface areas and
     * the DRAW_AND and DRAW_OR operations aren't supported at all under
     * Java.
     * <p>
     * PalmOS platforms supports all the drawing operations when drawing
     * images and copying surface regions. However, only the DRAW_OVER
     * operation is supported when drawing lines, rectangles and text.
     * If you need to use the XOR drawing operation for drawing lines
     * under PalmOS, you can draw the line into an image and then draw
     * the image with an XOR drawing operation.
     * <p>
     * Win32 and Windows CE platforms support all the drawing operations
     * except when drawing text. Only DRAW_OVER is supported when drawing
     * text. If you need to draw XOR'ed text, you can draw the text into
     * an image and then draw the image with an XOR draw operation.
     * <p>
     * When calculating the result of XOR, AND and OR drawing, the value
     * of the color black is all 1's (fully set) in binary and white is
     * all 0's (fully unset).
     *
     * @param op drawing operation
     * @see #DRAW_OVER
     * @see #DRAW_AND
     * @see #DRAW_OR
     * @see #DRAW_XOR
     */
    public void setDrawOp(int drawOp)
    {
      this.drawOp=drawOp;
    }
 
    /** Sets the current font for operations that draw text. */
    public void setFont(Font font)
    {
      this.font=font;
    }
 
    /**
     * Translates the origin of the current coordinate system by the given
     * x and y values.
     */
    public void translate(int x, int y)
    {
       tx+=x;
       ty+=y;
    }
 
     public void translateZero()
     {
 	tx = 0; ty = 0;
     }
 
    private void debug(String sText)
    {
       String sDisplay=sText;
       Palm.WinDrawChars(sDisplay,sDisplay.length(),80,0);
       waba.sys.Vm.sleep(500);
    }
 }
