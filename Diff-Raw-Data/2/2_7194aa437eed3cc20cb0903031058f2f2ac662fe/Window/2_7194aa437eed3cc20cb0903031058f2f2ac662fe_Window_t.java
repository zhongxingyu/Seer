 /*---------------------------------------------------------------------------*
 * $Id$
 *----------------------------------------------------------------------------*
 * May 23, 2013 - shane
 * 
 * Initial version
 *---------------------------------------------------------------------------*/
 package com.thegaragelab.quickui;
 
 //--- Imports
 import com.thegaragelab.quickui.utils.*;
 
 /** Represents a simple window.
  * 
  * This is the base class for all visual elements in the Framework. It
  * provides the basic functionality required for all visual elements.
  */
 public class Window implements IRectangle, ISurface, IFlags {
   //--- Constants
   public static final int WFLAG_IS_DIRTY         = 0x0001;
   public static final int WFLAG_ERASE_BACKGROUND = 0x0002;
   public static final int WFLAG_HAS_FOCUS        = 0x0004;
   public static final int WFLAG_CAN_HAVE_FOCUS   = 0x0008;
   
   //--- Instance variables
   private Container m_parent;    //! The parent Window
   private Window    m_root;      //! The root Window
   private Rectangle m_rectangle; //! Position and size of the window
   private Flags     m_flags;     //! Current flags
   
   //-------------------------------------------------------------------------
   // Construction and initialisation
   //-------------------------------------------------------------------------
 
   /** Constructor with a parent Window and a Rectangle describing it position.
    * 
    * @param parent the parent window for this instance.
    * @param rect the Rectangle describing the location and size of the window.
    */
   public Window(Container parent, Rectangle rect) {
     m_parent = parent;
     if(m_parent!=null)
       m_root = m_parent.getRoot();
     m_rectangle = new Rectangle(rect);
     initialiseState();
     onCreate();
     }
 
   /** Constructor with a parent Window and a Dimension describing it's size.
    * 
    * @param parent the parent window for this instance.
    * @param dimension the dimension of the new window.
    */
   public Window(Container parent, IDimension dimension) {
     m_parent = parent;
     if(m_parent!=null)
       m_root = m_parent.getRoot();
     m_rectangle = new Rectangle(Point.ORIGIN, dimension);
     initialiseState();
     onCreate();
     }
   
   /** Constructor with a parent Window and a width and height.
    * 
    * @param parent the parent Window for this instance.
    * @param width the width of the window in pixels.
    * @param height the height of the window in pixels.
    */
   public Window(Container parent, int width, int height) {
     m_parent = parent;
     if(m_parent!=null)
       m_root = m_parent.getRoot();
     m_rectangle = new Rectangle(Point.ORIGIN, width, height);
     initialiseState();
     onCreate();
     }
 
   /** Initialise the state
    * 
    *  This method is used to initialise the state for the type of window
    *  being created. Child classes may override this to set their own
    *  initial state but must call the parent implementation.
    */
   protected void initialiseState() {
     // Set default flags
     setFlags(
       Window.WFLAG_IS_DIRTY | 
       Window.WFLAG_CAN_HAVE_FOCUS
       );
     }
   
   //-------------------------------------------------------------------------
   // Window specific operations
   //-------------------------------------------------------------------------
 
   /** Get the parent of this Window
    * 
    * @return the Window instance that represents the parent of this window.
    */
   public Window getParent() {
     return m_parent;
     }
   
   /** Get the root Window
    * 
    * @return the Window instance that represents the display.
    */
   public Window getRoot() {
     return m_root;
     }
   
   /** Mark the window as 'dirty' (needs to be repainted)
    * 
    * @param dirty true if the window is dirty, false if not
    */
   public void setDirty(boolean dirty) {
     if(dirty)
       setFlags(Window.WFLAG_IS_DIRTY);
     else
       clearFlags(Window.WFLAG_IS_DIRTY);
     }
   
   /** Determine if the window is dirty (needs to be repainted)
    * 
    * @return true if the window is dirty and needs to be repainted.
    */
   public boolean isDirty() {
     return areFlagsSet(Window.WFLAG_IS_DIRTY);
     }
   
   /** Set or remove focus from this window
    * 
    * @param focus true if this window now has focus, false if not
    */
   public void setFocus(boolean focus) {
     // Can we have focus ?
     if(!areFlagsSet(Window.WFLAG_CAN_HAVE_FOCUS))
       return;
     // Has the focus changed ?
     if(focus==areFlagsSet(Window.WFLAG_CAN_HAVE_FOCUS | Window.WFLAG_HAS_FOCUS))
       return;
     // Change the focus
     if(focus)
       setFlags(Window.WFLAG_HAS_FOCUS);
     else
       clearFlags(Window.WFLAG_HAS_FOCUS);
     onFocus();
     }
   
   /** Determine if we have focus
    * 
    * @return true if we currently have focus
    */
   public boolean hasFocus() {
     return areFlagsSet(Window.WFLAG_CAN_HAVE_FOCUS | Window.WFLAG_HAS_FOCUS);
     }
   
   /** Get the focused window
    * 
    * @return the window that currently has focus.
    */
   public Window getFocusedWindow() {
     if(!hasFocus())
       return null;
     return this;
     }
   
   /** Get a window by location
    * 
    * @param point the Point we are searching for.
    * 
    * @return the smallest window that contains this point.
    */
   public Window getWindowByPoint(Point point) {
     if(!contains(point))
       return null;
     return this;
     }
   
   //-------------------------------------------------------------------------
   // Internal event methods
   //-------------------------------------------------------------------------
 
   /** Called to do a repaint of the window
    * 
    */
   void doRepaint() {
     // If we are not dirty, don't do anything
     if(!isDirty())
       return;
     // Erase the background if needed
     boolean doEraseBackground = areFlagsSet(Window.WFLAG_ERASE_BACKGROUND);
     Rectangle region = new Rectangle(this);
     if(doEraseBackground) {
       beginPaint();
       onEraseBackground(region);
       }
     // Repaint the window
     onPaint(region);
     // Finish the paint operation
     if(doEraseBackground)
       endPaint();
     }
   
   /** Called to do an update of the window
    */
   void doUpdate() {
     onUpdate();
     }
   
   //-------------------------------------------------------------------------
   // Public event methods
   //-------------------------------------------------------------------------
 
   /** Called to update the window
    * 
    */
   public void onUpdate() {
     // Do nothing in this instance
     }
   
   /** Called when the window is being created.
    * 
    * This method can be overwritten to provide creation specific operations
    * outside of the framework.
    */
   public void onCreate() {
     // Do nothing in this instance
     }
   
   /** Called when the window needs to be painted
    * 
    *  This method is called to redraw the window.
    * 
    *  @param region the rectangle describing the area to repaint. If this is
    *                null the entire window should be repainted.
    */
   public void onPaint(Rectangle region) {
     // Do nothing in this instance
     }
   
   /** Called to erase the background for the window.
    * 
    *  This method is invoked to clear all or part of the window area prior to
    *  painting. The implementation must not change anything outside of the
    *  rectangle specified.
    *  
    *  @param region the rectangle describing the area to repaint. If this is
    *                null the entire window should be repainted.
    */
   public void onEraseBackground(Rectangle region) {
     // Do nothing in this instance
     }
   
   /** Called when the window focus has changed.
    * 
    * This method is invoked when the window focus state has changed. The
    * window may need to redraw itself when that happens.
    */
   public void onFocus() {
     // Do nothing in this instance
     }
   
   //-------------------------------------------------------------------------
   // Implementation of IRectangle
   //-------------------------------------------------------------------------
 
   /** Get the X co-ordinate for this point.
    * 
    * @return the X co-ordinate for this point.
    */
   public int getX() {
     return m_rectangle.x;
     }
   
   /** Set the X co-ordinate for this point.
    * 
    * @param nx the new X co-ordinate for this point.
    */
   public void setX(int nx) {
     m_rectangle.x = nx;
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
    * @param ny the new Y co-ordinate for this point.
    */
   public void setY(int ny) {
     m_rectangle.y = ny;
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
    * @param w the new width of the rectangle.
    */
   public void setWidth(int w) {
     m_rectangle.width = w;
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
    * @param h the new height of the rectangle.
    */
   public void setHeight(int h) {
     m_rectangle.height = h;
     }
   
   /** Determine if this rectangle contains the given point
    * 
    * @param point the point to test.
    * 
    * @return true if the rectangle contains the given point, false otherwise.
    */
   public boolean contains(Point point) {
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
     getRoot().beginPaint();
     }
   
   /** End a paint operation.
    * 
    * This method is used to signal the end of a complex paint operation.
    */
   public void endPaint() {
     getRoot().endPaint();
     }
 
   /** Display a single pixel.
    * 
    * @param point the Point at which to display the pixel.
    * @param color the Color to set the pixel to.
    */
   public void putPixel(Point point, Color color) {
     getRoot().putPixel(point, color);
     }
 
   /** Fill a rectangle with a specific color.
    * 
    * @param rect the Rectangle describing the area to fill.
    * @param color the Color to fill the rectangle with.
    */
   public void fillRect(Rectangle rect, Color color) {
     getRoot().fillRect(rect, color);
     }
   
   /** Draw a line from one point to another 
    * 
    * @param start the starting point for the line.
    * @param end the ending point for the line.
    * @param color the color to draw the line in.
    */
   public void drawLine(Point start, Point end, Color color) {
     getRoot().drawLine(start, end, color);
     }
   
   /** Draw a box around a rectangle.
    * 
    * @param rect the Rectangle to draw the box around.
    * @param color the Color to draw the box in.
    */
   public void drawBox(Rectangle rect, Color color) {
     getRoot().drawBox(rect, color);
     }
 
   /** Draw an Icon to the screen.
    * 
    * @param point the Point specifying the top left corner of the icon.
    * @param icon the Icon to display.
    * @param color the Color to use for the solid parts of the icon.
    */
   public void drawIcon(Point point, Icon icon, Color color) {
     getRoot().drawIcon(point, icon, color);
     }
 
   /** Draw a portion of an Icon to the screen.
    * 
    * @param point the Point specifying the top left corner of the icon.
    * @param icon the Icon to display.
    * @param color the Color to use for the solid parts of the icon.
    * @param portion a Rectangle specifying the portion of the icon to draw.
    */
   public void drawIcon(Point point, Icon icon, Color color, Rectangle portion) {
     getRoot().drawIcon(point, icon, color, portion);
     }
   
   /** Draw an Image to the screen.
    * 
    * @param point the Point specifying the top left corner of the icon.
    * @param image the Image to display.
    * @param palette the Palette to use to display the image.
    */
   public void drawImage(Point point, Image image, Palette palette) {
     getRoot().drawImage(point, image, palette);
     }
   
   /** Draw an Image to the screen.
    * 
    * @param point the Point specifying the top left corner of the icon.
    * @param image the Image to display.
    * @param palette the Palette to use to display the image.
    * @param portion the Rectangle describing the portion of the image to display.
    */
   public void drawImage(Point point, Image image, Palette palette, Rectangle portion) {
     getRoot().drawImage(point, image, palette, portion);
     }
   
   }
