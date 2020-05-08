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
 
 /** A container is a special type of window that can have child windows.
  *
  */
 public class Container extends Window {
   //--- Instance variables
   private List<Window> m_children; //! Child windows
   
   //-------------------------------------------------------------------------
   // Construction and initialisation
   //-------------------------------------------------------------------------
 
   /** Constructor with a Rectangle describing its area.
    * 
    * @param rect the Rectangle describing the location and size of the window.
    */
   Container(Rectangle rect) {
     super(rect);
     }
 
   /** Constructor with a parent Window a position and flags to set or clear.
    * 
    * @param parent the parent window for this instance.
    * @param rect the Rectangle describing the location and size of the window.
    * @param require additional flags to set
    * @param exclude flags to mask out
    */
   public Container(Container parent, IRectangle rect, int require, int exclude) {
     super(parent, rect, require, exclude);
     }
   
   /** Constructor with a parent Window and a Rectangle for position and size.
    * 
    * @param parent the parent window for this instance.
    * @param rect the Rectangle describing the location and size of the window.
    */
   public Container(Container parent, IRectangle rect) {
     super(parent, rect);
     }
 
   /** Initialise the state
    * 
    *  This method is used to initialise the state for the type of window
    *  being created. Child classes may override this to set their own
    *  initial state but must call the parent implementation.
    */
   void initialiseState() {
     super.initialiseState();
     // Create our list of child elements
     m_children = new ArrayList<Window>();
     }
   
   //-------------------------------------------------------------------------
   // Implementation of Window
   //-------------------------------------------------------------------------
   
   /** Get a window by location
    * 
    * @param point the Point we are searching for.
    * 
    * @return the smallest window that contains this point.
    */
   @Override
   public IWindow getWindowByPoint(IPoint point) {
     if(!contains(point))
       return null;
     // Do we have children ?
     if((m_children==null)||(m_children.size()==0))
       return this;
     // Check each child
     Point test = Point.offset(this, point);
     for(IWindow child: m_children) {
       IWindow result = child.getWindowByPoint(test);
       if(result!=null)
         return result;
       }
     // Not found, that means it's us
     return this;
     }
   
   //-------------------------------------------------------------------------
   // Container specific operations
   //-------------------------------------------------------------------------
 
   /** Add a child window to this container.
    * 
    * @param window the Window to add as a child.
    */
   void add(Window child) {
     m_children.add(child);
     }
   
   /** Remove a child window from this container
    * 
    * @param window the Window to remove
    */
   public void remove(Window child) {
     if(m_children.remove(child))
       child.onClose();
     }
     
   /** Find all dirty children of this container
    * 
    */
   @Override
   void findDirtyChildren(List<Window> children) {
    // If we are not visible we don't care about dirty state
    if(!isVisible())
      return;
     // If we are dirty simply add ourselves
     if(isDirty())
       children.add(this);
     else {
       // Walk through our children and add them
       for(Window child: m_children)
         child.findDirtyChildren(children);
       }
     }
   
   //-------------------------------------------------------------------------
   // Internal event methods
   //-------------------------------------------------------------------------
 
   /** Called to do a repaint of the window
    * 
    * Containers are treated differently here. Even though the container itself
    * may not be dirty one or more of it's child windows may be so we have to
    * walk through all of them and repaint. 
    * 
    * @param force if true force a repaint regardless of the 'dirty' state.
    */
   @Override
   void doRepaint(boolean force) {
     // Don't do anything if we are not visible
    if(!isVisible()) {
      setDirty(false);
       return;
      }
     // Even if we are not dirty we may have to repaint some child windows
     if(isDirty()||force) {
       beginPaint();
      // Erase the background and repaint
      onEraseBackground();
       super.doRepaint(true);
       // Repaint everything
       for(Window child: m_children) {
         setOffset(this);
         child.doRepaint(true);
         }
       endPaint();
       }
     else {
       // Only repaint child windows that need it
       List<Window> children = new ArrayList<Window>();
       findDirtyChildren(children);
       for(Window child: children) {
         beginPaint();
         // TODO: Should repaint our background before painting the child
         setOffset(this);
         child.doRepaint(false);
         endPaint();
         }
       }
    setDirty(false);
     }
   
   /** Called to do an update of the window.
    * 
    *  In the case of a container we do an update on all child windows as
    *  well as our own.
    */
   void doUpdate() {
     super.doUpdate();
     // Do we have children to deal with?
     if((m_children==null)||(m_children.size()==0))
       return;
     // Process all child windows
     for(Window child: m_children)
       child.doUpdate();
     }
   
   }
