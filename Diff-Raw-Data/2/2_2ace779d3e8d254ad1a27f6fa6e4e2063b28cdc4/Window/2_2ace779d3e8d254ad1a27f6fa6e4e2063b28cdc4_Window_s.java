 /*---------------------------------------------------------------------------*
 * $Id$
 *----------------------------------------------------------------------------*
 * May 23, 2013 - shane
 * 
 * Initial version
 *---------------------------------------------------------------------------*/
 package com.thegaragelab.quickui;
 
 //--- Imports
 import java.util.*;
 import com.thegaragelab.quickui.utils.*;
 
 /** Represents a simple window.
  * 
  * This is the base class for all visual elements in the Framework. It
  * provides the basic functionality required for all visual elements.
  */
 public class Window implements IWindow {
   //--- Constants
   protected static final int WIN_FLAG_DIRTY            = 0x0001;
   protected static final int WIN_FLAG_VISIBLE          = WIN_FLAG_DIRTY << 1;
   protected static final int WIN_FLAG_ACCEPT_TOUCH     = WIN_FLAG_VISIBLE << 1;
   protected static final int WIN_FLAG_ERASE_BACKGROUND = WIN_FLAG_ACCEPT_TOUCH << 1;
   
   //--- Instance variables
   private Container m_parent;     //! The parent Window
   private Rectangle m_rectangle;  //! Position and size of the window
   private Flags     m_flags;      //! Current flags
   
   //-------------------------------------------------------------------------
   // Construction and initialisation
   //-------------------------------------------------------------------------
 
   /** Constructor with a Rectangle describing its area.
    * 
    * @param rect the Rectangle describing the location and size of the window.
    */
   Window(Rectangle rect) {
     m_rectangle = new Rectangle(rect);
     m_flags = new Flags();
     initialiseState();
     // Call the creation method
     onCreate();
     }
 
   /** Constructor with a parent Window a position and flags to set or clear.
    * 
    * @param parent the parent window for this instance.
    * @param rect the Rectangle describing the location and size of the window.
    * @param require additional flags to set
    * @param exclude flags to mask out
    */
   public Window(Container parent, IRectangle rect, int require, int exclude) {
     m_parent = parent;
     m_flags = new Flags();
     m_rectangle = new Rectangle(rect);
     initialiseState();
     // Adjust flags
     m_flags.setFlags(require);
     m_flags.clearFlags(exclude);
     // Call the creation method
     onCreate();
     }
 
   /** Constructor with a parent Window and a Rectangle for position and size.
    * 
    * @param parent the parent window for this instance.
    * @param rect the Rectangle describing the location and size of the window.
    */
   public Window(Container parent, IRectangle rect) {
     this(parent, rect, 0, 0);
     }
 
   /** Initialise the state
    * 
    *  This method is used to initialise the state for the type of window
    *  being created. Child classes may override this to set their own
    *  initial state but must call the parent implementation.
    */
   void initialiseState() {
     // Always start as visible and dirty
     setDirty(true);
     setVisible(true);
     // Add ourselves to the parent (if we have one)
     if(m_parent!=null) {
       // Translate our co-ordinates from relative to absolute
       m_rectangle = (Rectangle)m_rectangle.translate(m_parent);
       // Add ourselves to the parent
       m_parent.add(this);
       }
     }
   
   //-------------------------------------------------------------------------
   // Window specific operations
   //-------------------------------------------------------------------------
 
   /** Get the preferred width of this window
    * 
    * This method is used to determine the preferred width of the window,
    * the width required to fit it's contents in.
    * 
    * @return the preferred width of the window.
    */
   public int getPreferredWidth() {
     // We have no preferred width so just use the current width
     return getWidth();
     }
   
   /** Get the preferred height of this window
    * 
    * This method is used to determine the preferred height of the window,
    * the height required to fit it's contents in.
    * 
    * @return the preferred height of the window.
    */
   public int getPreferredHeight() {
     // We have no preferred height so just use the current height
     return getHeight();
     }
   
   /** Set the preferred size of the window
    * 
    * This method will change the current size of the window to match the
    * preferred width and height.
    */
   public void setPreferredSize() {
     int value = getPreferredWidth();
     if(value>0)
       setWidth(value);
     value = getPreferredHeight();
     if(value>0)
       setHeight(value);
     }
   
   /**
    * @see com.thegaragelab.quickui.IWindow#getParent()
    */
   public IWindow getParent() {
     return m_parent;
     }
   
   /**
    * @see com.thegaragelab.quickui.IWindow#setDirty(boolean)
    */
   public void setDirty(boolean dirty) {
     if(dirty)
       m_flags.setFlags(WIN_FLAG_DIRTY);
     else
       m_flags.clearFlags(WIN_FLAG_DIRTY);
     }
   
   /**
    * @see com.thegaragelab.quickui.IWindow#isDirty()
    */
   public boolean isDirty() {
     return m_flags.areFlagsSet(WIN_FLAG_DIRTY | WIN_FLAG_VISIBLE);
     }
 
   /**
    * @see com.thegaragelab.quickui.IWindow#setVisible(boolean)
    */
   public void setVisible(boolean visible) {
     // Any change ?
     if(m_flags.areFlagsSet(WIN_FLAG_VISIBLE)==visible)
       return;
     // Change the flag
     if(visible)
       m_flags.setFlags(WIN_FLAG_VISIBLE | WIN_FLAG_DIRTY);
     else
       m_flags.clearFlags(WIN_FLAG_VISIBLE);
     // Let the window know the state has changed
     onVisible(visible);
     }
   
   /**
    * @see com.thegaragelab.quickui.IWindow#isVisible()
    */
   public boolean isVisible() {
     return m_flags.areFlagsSet(WIN_FLAG_VISIBLE);
     }
   
   /** Indicate to the Window it should erase it's background on repaint.
    * 
    * @param erase true if the Window should erase it's background.
    */
   public void setEraseBackground(boolean erase) {
     // Any change ?
     if(m_flags.areFlagsSet(WIN_FLAG_ERASE_BACKGROUND)==erase)
       return;
     // Change the flag
     if(erase)
       m_flags.setFlags(WIN_FLAG_ERASE_BACKGROUND | WIN_FLAG_DIRTY);
     else
       m_flags.clearFlags(WIN_FLAG_ERASE_BACKGROUND);
     }
   
   /** Determine if the window should erase it's background on repaint.
    * 
    * @return true if the window should erase it's background.
    */
   public boolean getEraseBackground() {
     return m_flags.areFlagsSet(WIN_FLAG_ERASE_BACKGROUND);
     }
   
   /**
    * @see com.thegaragelab.quickui.IWindow#setAcceptTouch(boolean)
    */
   public void setAcceptTouch(boolean accept) {
     m_flags.setFlags(WIN_FLAG_ACCEPT_TOUCH);
     }
   
   /**
    * @see com.thegaragelab.quickui.IWindow#getAcceptTouch()
    */
   public IWindow getAcceptTouch() {
     if(m_flags.areFlagsSet(WIN_FLAG_ACCEPT_TOUCH))
       return this;
     return getParent().getAcceptTouch();
     }
   
   /**
    * @see com.thegaragelab.quickui.IWindow#getWindowByPoint(com.thegaragelab.quickui.IPoint)
    */
   public IWindow getWindowByPoint(IPoint point) {
     if(!contains(point))
       return null;
     return this;
     }
   
   //-------------------------------------------------------------------------
   // Internal event and painting helpers
   //-------------------------------------------------------------------------
 
   /** Find all dirty child windows
    * 
    * As we have no child windows we simply add ourselves to the list (if we
    * are dirty).
    * 
    * @param children the list of dirty children
    */
   void findDirtyChildren(List<Window> children) {
     if(isDirty())
       children.add(this);
     }
   
   /** Set the offset for this window
    * 
    * @param offset the offset to use for future painting operations
    */
   void setOffset(IPoint offset) {
     Application.getInstance().setOffset(offset);
     }
   
   /** Called to do a repaint of the window
    * 
    * This is an internal helper to manage the painting process. Child
    * windows should only override the onPaint() method and do custom
    * painting there - it will be called as needed by the internal logic
    * of the framework.
    * 
    * @param force if true force a repaint regardless of the 'dirty' state.
    */
   void doRepaint(boolean force) {
    // If we are not visible, don'tfalse do anything
     if(!isVisible())
       return;
     // If we are not dirty, don't do anything
     if(!(isDirty()||force))
       return;
     // Start the paint operation
     Rectangle region = new Rectangle(this);
     Application.getInstance().setClip(region);
     beginPaint();
     setOffset(region);
     // Erase the background if needed
     if(getEraseBackground())
       onEraseBackground();
     // Repaint the window
     onPaint();
     // Finish the paint operation
     endPaint();
     setDirty(false);
     }
   
   /** Called to do an update of the window
    */
   void doUpdate() {
     onUpdate();
     }
   
   //-------------------------------------------------------------------------
   // Public event methods
   //-------------------------------------------------------------------------
 
   /**
    * @see com.thegaragelab.quickui.IWindow#onCreate()
    */
   public void onCreate() {
     // Do nothing in this instance
     }
   
   /**
    * @see com.thegaragelab.quickui.IWindow#onClose()
    */
   public void onClose() {
     // Do nothing in this instance
     }
   
   /**
    * @see com.thegaragelab.quickui.IWindow#onUpdate()
    */
   public void onUpdate() {
     // Do nothing in this instance
     }
   
   /** Called to erase the background of the window.
    */
   public void onEraseBackground() {
     fillRect(
       new Rectangle(Point.ORIGIN, this),
       Application.getInstance().getSystemColor(Application.SYS_COLOR_WIN_BACKGROUND)
       );
     }
   
   /**
    * @see com.thegaragelab.quickui.IWindow#onPaint()
    */
   public void onPaint() {
     // Do nothing in this instance
     }
   
   /**
    * @see com.thegaragelab.quickui.IWindow#onTouchEvent(int, com.thegaragelab.quickui.IPoint)
    */
   public void onTouchEvent(int evType, IPoint where) {
     // Do nothing in this instance
     }
   
   /**
    * @see com.thegaragelab.quickui.IWindow#onVisible(boolean)
    */
   public void onVisible(boolean visible) {
     // Do nothing in this instance
     }
   
   //-------------------------------------------------------------------------
   // Implementation of IPoint and IRectangle
   //-------------------------------------------------------------------------
 
   /** Get the X co-ordinate for this point.
    * 
    * @return the X co-ordinate for this point.
    */
   public int getX() {
     return m_rectangle.x;
     }
   
   /** Set the X co-ordinate for the window
    * 
    * For a window this sets the parent to dirty so all child windows are
    * redrawn.
    * 
    * @param nx the new X co-ordinate for this point.
    */
   public void setX(int nx) {
     m_rectangle.x = nx;
     getParent().setDirty(true);
     }
   
   /** Get the Y co-ordinate for this point.
    * 
    * @return the Y co-ordinate for this point.
    */
   public int getY() {
     return m_rectangle.y;
     }
   
   /** Set the Y co-ordinate for this point.
    * 
    * For a window this sets the parent to dirty so all child windows are
    * redrawn.
    * 
    * @param ny the new Y co-ordinate for this point.
    */
   public void setY(int ny) {
     m_rectangle.y = ny;
     getParent().setDirty(true);
     }
   
   /** Translate the point so the given point is the origin
    * 
    * @point origin the new origin for the co-ordinates
    * 
    * @return IPoint the translated instance.
    */
   public IPoint translate(IPoint origin) {
     return m_rectangle.translate(origin);
     }
   
   /** Get the width of the rectangle.
    * 
    * @return the width of the rectangle.
    */
   public int getWidth() {
     return m_rectangle.width;
     }
   
   /** Set the width of the rectangle.
    * 
    * For a window this sets the parent to dirty so all child windows are
    * redrawn.
    * 
    * @param w the new width of the rectangle.
    */
   public void setWidth(int w) {
     m_rectangle.width = w;
     getParent().setDirty(true);
     }
   
   /** Get the height of the rectangle.
    * 
    * @return the height of the rectangle.
    */
   public int getHeight() {
     return m_rectangle.height;
     }
   
   /** Set the height of the rectangle.
    * 
    * For a window this sets the parent to dirty so all child windows are
    * redrawn.
    * 
    * @param h the new height of the rectangle.
    */
   public void setHeight(int h) {
     m_rectangle.height = h;
     getParent().setDirty(true);
     }
   
   /** Determine if this rectangle contains the given point
    * 
    * @param point the point to test.
    * 
    * @return true if the rectangle contains the given point, false otherwise.
    */
   public boolean contains(IPoint point) {
     return m_rectangle.contains(point);
     }
   
   //-------------------------------------------------------------------------
   // Implementation of IFlags
   //-------------------------------------------------------------------------
   
   /** Set one or more flags to true.
    * 
    * @param flags the bit values to set
    */
   public void setFlags(int flags) {
     if(m_flags==null)
       m_flags = new Flags();
     m_flags.setFlags(flags);
     }
   
   /** Clear one or more flags.
    * 
    * @param flags the bit values to clear
    */
   public void clearFlags(int flags) {
     m_flags.clearFlags(flags);
     }
   
   /** Get the current set of flags
    * 
    * @return the current value of the flags
    */
   public int getFlags() {
     return m_flags.getFlags();
     }
   
   /** Determine if one or more flags are set
    * 
    * @param flags the flags to test for. All must be set to pass.
    */
   public boolean areFlagsSet(int flags) {
     return m_flags.areFlagsSet(flags);
     }
   
   //-------------------------------------------------------------------------
   // Implementation of ISurface
   //
   // Windows will delegate drawing operations to the root window.
   //-------------------------------------------------------------------------
 
   /** Begin a paint operation.
    * 
    * This method is used to signal the start of a complex paint operation.
    * It is used to help the driver optimise updates to the physical display.
    */
   public void beginPaint() {
     Application.getInstance().beginPaint();
     }
   
   /** End a paint operation.
    * 
    * This method is used to signal the end of a complex paint operation.
    */
   public void endPaint() {
     Application.getInstance().endPaint();
     }
 
   /** Set the clipping region for future operations
    * 
    * @param rect the Rectangle describing the clipping region.
    */
   public void setClip(IRectangle rect) {
     // TODO: Should clip relative to this window.
     }
 
   /** Display a single pixel.
    * 
    * @param point the Point at which to display the pixel.
    * @param color the Color to set the pixel to.
    */
   public void putPixel(IPoint point, Color color) {
     Application.getInstance().putPixel(point, color);
     }
 
   /** Fill a rectangle with a specific color.
    * 
    * @param rect the Rectangle describing the area to fill.
    * @param color the Color to fill the rectangle with.
    */
   public void fillRect(IRectangle rect, Color color) {
     Application.getInstance().fillRect(rect, color);
     }
   
   /** Draw a line from one point to another 
    * 
    * @param start the starting point for the line.
    * @param end the ending point for the line.
    * @param color the color to draw the line in.
    */
   public void drawLine(IPoint start, IPoint end, Color color) {
     Application.getInstance().drawLine(start, end, color);
     }
   
   /** Draw a box around a rectangle.
    * 
    * @param rect the Rectangle to draw the box around.
    * @param color the Color to draw the box in.
    */
   public void drawBox(IRectangle rect, Color color) {
     Application.getInstance().drawBox(rect, color);
     }
 
   /** Draw an Icon to the screen.
    * 
    * @param point the Point specifying the top left corner of the icon.
    * @param icon the Icon to display.
    * @param color the Color to use for the solid parts of the icon.
    */
   public void drawIcon(IPoint point, Icon icon, Color color) {
     Application.getInstance().drawIcon(point, icon, color);
     }
 
   /** Draw a portion of an Icon to the screen.
    * 
    * @param point the Point specifying the top left corner of the icon.
    * @param icon the Icon to display.
    * @param color the Color to use for the solid parts of the icon.
    * @param portion a Rectangle specifying the portion of the icon to draw.
    */
   public void drawIcon(IPoint point, Icon icon, Color color, IRectangle portion) {
     Application.getInstance().drawIcon(point, icon, color, portion);
     }
   
   /** Draw an Image to the screen.
    * 
    * @param point the Point specifying the top left corner of the icon.
    * @param image the Image to display.
    * @param palette the Palette to use to display the image.
    */
   public void drawImage(IPoint point, Image image, Palette palette) {
     Application.getInstance().drawImage(point, image, palette);
     }
   
   /** Draw an Image to the screen.
    * 
    * @param point the Point specifying the top left corner of the icon.
    * @param image the Image to display.
    * @param palette the Palette to use to display the image.
    * @param portion the Rectangle describing the portion of the image to display.
    */
   public void drawImage(IPoint point, Image image, Palette palette, IRectangle portion) {
     Application.getInstance().drawImage(point, image, palette, portion);
     }
   
   /** Draw a single character using the given font.
    * 
    * @param font the Font to use to render the character.
    * @param point the location to draw the character at.
    * @param color the Color to draw the character with.
    * @param ch the character to draw.
    */
   public void drawChar(Font font, IPoint point, Color color, char ch) {
     font.drawChar(this, point, color, ch);
     }
 
   /** Draw a string using the given font.
    * 
    * @param font the Font to use to render the character.
    * @param point the location to draw the character at.
    * @param color the Color to draw the character with.
    * @param string the string to draw.
    */
   public void drawString(Font font, IPoint point, Color color, String string) {
     font.drawString(this, point, color, string);
     }
 
   }
