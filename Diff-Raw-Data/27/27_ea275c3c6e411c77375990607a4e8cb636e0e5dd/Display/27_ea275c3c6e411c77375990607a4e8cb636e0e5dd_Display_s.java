 /*
  * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
  * 
  * Copyright (C) 2005-2009 FengGUI Project
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details:
  * http://www.gnu.org/copyleft/lesser.html#TOC3
  * 
  * Created on 2005-3-2
  * $Id: Display.java 611 2009-03-22 15:58:20Z marcmenghin $
  */
 package org.fenggui;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.fenggui.binding.render.Binding;
 import org.fenggui.binding.render.Graphics;
 import org.fenggui.binding.render.IOpenGL;
 import org.fenggui.composite.IWindow;
 import org.fenggui.event.DisplayResizedEvent;
 import org.fenggui.event.Event;
 import org.fenggui.event.FocusEvent;
 import org.fenggui.event.IDisplayResizedListener;
 import org.fenggui.event.IDragAndDropListener;
 import org.fenggui.event.IEventListener;
 import org.fenggui.event.ITickListener;
 import org.fenggui.event.SizeChangedEvent;
 import org.fenggui.event.TickEvent;
 import org.fenggui.event.key.Key;
 import org.fenggui.event.key.KeyPressedEvent;
 import org.fenggui.event.key.KeyReleasedEvent;
 import org.fenggui.event.key.KeyTypedEvent;
 import org.fenggui.event.mouse.MouseButton;
 import org.fenggui.event.mouse.MouseClickedEvent;
 import org.fenggui.event.mouse.MouseDoubleClickedEvent;
 import org.fenggui.event.mouse.MouseDraggedEvent;
 import org.fenggui.event.mouse.MouseEnteredEvent;
 import org.fenggui.event.mouse.MouseExitedEvent;
 import org.fenggui.event.mouse.MousePressedEvent;
 import org.fenggui.event.mouse.MouseReleasedEvent;
 import org.fenggui.event.mouse.MouseWheelEvent;
 import org.fenggui.layout.LayoutManager;
 import org.fenggui.layout.StaticLayout;
 import org.fenggui.theme.XMLTheme;
 import org.fenggui.theme.xml.IXMLStreamable;
 import org.fenggui.theme.xml.IXMLStreamableException;
 import org.fenggui.theme.xml.InputOutputStream;
 import org.fenggui.tooltip.ITooltipManager;
 import org.fenggui.util.Dimension;
 
 /**
  * Root of the widget tree. The Display spans over the whole screen. Serves also as the
  * entry point for the event distribution in the widget tree.<br/>
  * 
  * @author Johannes Schaback aka Schabby, last changed by $Author: marcmenghin $, $Date:
  *         2008-05-07 15:28:14 +0200 (Mi, 07 Mai 2008) $
  * @version $Revision: 611 $
  */
 public class Display extends Container
 {
   private java.util.List<IDragAndDropListener> dndListeners        = new ArrayList<IDragAndDropListener>();
   private java.util.List<IEventListener>       globalEventListener = new CopyOnWriteArrayList<IEventListener>();
 
   private IWidget                              mouseOverWidget     = this;
   private Binding                              binding             = null;
   private boolean                              depthTestEnabled    = false;
   private KeyPressTracker                      keyPressTracker;
 
   /** Widget that is dragged (and dropped) */
   // private Widget dndWidget = null;
   private IDragAndDropListener                 draggingListener    = null;
 
   private IWidget                              focusedWidget       = null;
 
   private Widget                               popupWidget         = null;
 
   private boolean                              enabled             = true;
 
   private ITooltipManager                      tooltips            = null;
 
   public Display()
   {
     this(Binding.getInstance());
   }
 
   /**
    * Constructs a new <code>Display</code> object. Note that you can have several
    * <code>Display</code> instances but only one <code>Binding</code>.
    * 
    * @param binding
    *          the opengl binding used to render FengGUI.
    */
   public Display(Binding binding)
   {
     if (binding == null)
       throw new IllegalArgumentException("binding = null");
     keyPressTracker = new KeyPressTracker();
     this.binding = binding;
     this.setY(0);
     this.setX(0);
 
     setSize(binding.getCanvasWidth(), binding.getCanvasHeight());
     setLayoutManager(new StaticLayout());
     binding.addDisplayResizedListener(new IDisplayResizedListener()
     {
 
       public void displayResized(DisplayResizedEvent displayResizedEvent)
       {
         setSize(displayResizedEvent.getWidth(), displayResizedEvent.getHeight());
         layout();
         fireGlobalEventListener(displayResizedEvent);
       }
 
     });
 
     binding.addTickEventListener(new ITickListener()
     {
 
       public void tick(TickEvent tickEvent)
       {
         // Distribute tick event as global event
         fireGlobalEventListener(tickEvent);
       }
 
     });
   }
 
   @Override
   public void process(InputOutputStream stream) throws IOException, IXMLStreamableException
   {
     setLayoutManager((LayoutManager) stream.processChild(getLayoutManager(), XMLTheme.TYPE_REGISTRY));
     stream.processChildren(notifyList, XMLTheme.TYPE_REGISTRY);
     for (IWidget w : notifyList)
     {
       w.setParent(this);
     }
 
     if (this.getTooltips() != null)
       stream.processChild((IXMLStreamable) this.getTooltips(), XMLTheme.TOOLTIPMANAGER_REGISTRY);
   }
 
   /** Returns true as the display is the root of the widget tree. */
   @Override
   public final boolean isInWidgetTree()
   {
     return true;
   }
 
   /**
    * Adds the given widget as a popup widget to this display.
    * 
    * @param pus
    *          the popup widget
    */
   public void displayPopUp(Widget pus)
   {
     // addWidget calls setParent() and addedToWidgetTree()
     addWidget(pus);
     popupWidget = pus;
   }
 
   /** Removes the widget currently set as popup widget from the display. */
   public void removePopup()
   {
     // Remove the popup from widgetTree
     removeWidget(popupWidget);
     popupWidget = null;
   }
 
   /**
    * Returns the binding of this display.
    * 
    * @return Binding of this display.
    */
   public final Binding getBinding()
   {
     return binding;
   }
 
   /** Returns null because the display is the root of the widget tree. */
   public final Container getParent()
   {
     return null;
   }
 
   @Override
   public boolean isKeyTraversalRoot()
   {
     // has to be the absolute root of traversal
     return true;
   }
 
   /**
    * Returns this <code>Display</code> instance. Realize the this method marks the end
    * of recursive <code>getDisplay</code> calls.
    */
   @Override
   public final Display getDisplay()
   {
     return this;
   }
 
   /**
    * This has to be called each time FengGUI should update itself. Usually this is done at
    * the end of your render loop.
    * 
    * <p>
    * NOTE: If your engine does use Multitexturing you need to set the active Texture Unit
    * to 0 and enable 2D Texturing if not already.
    * </p>
    * 
    */
   public void display()
   {
     if (!this.isVisible())
       return;
 
     IOpenGL opengl = binding.getOpenGL();
     opengl.pushAllAttribs();
 
     // this call is a problem, on newly installed systems it will raise an error as
     // no OpenGL extensions are installed. Should be replaced by something else if
     // possible.
     // opengl.activateTexture(0);
 
    opengl.setViewPort(0, 0, binding.getCanvasWidth(), binding.getCanvasHeight());
 
     opengl.setProjectionMatrixMode();
     opengl.pushMatrix();
     opengl.loadIdentity();
    opengl.setOrtho2D(0, binding.getCanvasWidth(), 0, binding.getCanvasHeight());
 
     opengl.setModelMatrixMode();
     opengl.pushMatrix();
     opengl.loadIdentity();
     opengl.setupStateVariables(depthTestEnabled);
 
     // opengl.translateZ(-50);
 
     Graphics g = binding.getGraphics();
     g.resetTransformations();
     g.resetClipSpace();
     g.forceColor(true);
 
     for (IWidget c : notifyList)
     {
       if (c == null)
       {
         // TODO: bug, removing components from cont. causes null pointers here # this
         // should not happen anymore
         System.err
             .println("NullPointerEx. prevention :( It is known a bug caused by multi threading! Should not happen anymore!");
         continue;
       }
 
       opengl.pushMatrix();
 
       clipWidget(g, c);
 
       g.translate(c.getX(), c.getY());
 
       c.paint(g);
 
       g.translate(-c.getX(), -c.getY());
 
       g.removeLastClipSpace();
       opengl.popMatrix();
 
     }
 
     g.resetClipSpace();
 
     opengl.setProjectionMatrixMode();
     opengl.popMatrix();
 
     opengl.setModelMatrixMode();
     opengl.popMatrix();
     opengl.popAllAttribs();
   }
 
   /** @return the focused widget */
   public IWidget getFocusedWidget()
   {
     return focusedWidget;
   }
 
   /**
    * Sets the focused widget
    * 
    * @param widget
    *          the widget to receive the focus
    */
   public void setFocusedWidget(IWidget widget)
   {
     IWidget oldWidget = focusedWidget;
     focusedWidget = widget;
 
     if (oldWidget != null && oldWidget != widget)
     {
       FocusEvent e = new FocusEvent(oldWidget, true);
       oldWidget.focusChanged(e);
       fireGlobalEventListener(e);
     }
 
     if (widget != null)
     {
       FocusEvent e = new FocusEvent(widget, false);
       widget.focusChanged(e);
       fireGlobalEventListener(e);
     }
 
   }
 
   private boolean grandParentIsPopupWidget(IWidget w)
   {
     if (w.getParent() == null)
       return false;
 
     if (w.getParent().equals(popupWidget))
       return true;
 
     return grandParentIsPopupWidget(w.getParent());
   }
 
   /**
    * Triggers a mouse pressed event in joglui.
    * 
    * @param mouseX
    *          distance of cursor to left hand side of screen
    * @param mouseY
    *          distance of cursor to bottom of screen
    * @param mouseButton
    *          the pressed mouse button
    * @param clickCount
    *          indicates double click, tripple click, etc.
    * @return true if GUI component within Display was hit, false otherwise
    */
   public boolean fireMousePressedEvent(int mouseX, int mouseY, MouseButton mouseButton, int clickCount)
   {
     IWidget w = getWidget(mouseX, mouseY);
 
     if (!this.isEnabled())
       return w != null;
 
     /*
      * Exceptional case for pop up shell. Pop up shells are opened by clickling on a menu
      * or combo box or so. If a second click does not fall in the pop up shell, the shell
      * disappears. The popupWidget is marked to delete and will be delete after
      * fireMousePressedEvent on the widget tree.
      */
     IWidget toDeletePopupWidget = null;
     if (popupWidget != null && !w.equals(popupWidget) && !grandParentIsPopupWidget(w))
     {
       toDeletePopupWidget = popupWidget;
     }
 
     // didn't hit the plain Display...
     boolean returnValue = false;
 
     if (!w.equals(this))
     {
       IWidget targetWidget = w;
 
       if (targetWidget.isTraversable() && !(targetWidget instanceof Container))
       {
         setFocusedWidget(targetWidget);
       }
 
       // Set the new focused widget
 
       MousePressedEvent e = new MousePressedEvent(w, mouseX, mouseY, mouseButton, clickCount, keyPressTracker
           .getModifiers());
       w.mousePressed(e);
       fireGlobalEventListener(e);
 
       for (int i = 0; i < dndListeners.size(); i++)
       {
         IDragAndDropListener dndListener = dndListeners.get(i);
         if (dndListener.isDndWidget(w, mouseX, mouseY))
         {
           dndListener.select(mouseX, mouseY, keyPressTracker.getModifiers());
           draggingListener = dndListener;
         }
       }
 
       // determine whether a frame was hit
       while (w.getParent() != null && !(w.getParent() instanceof IWindow))
         w = (Widget) w.getParent();
 
       if (w.getParent() instanceof IWindow && w.getParent().getParent() == this)
         bringToFront(w.getParent());
 
       // if yes, then re-order frame in list so that it is diplayed
       // on top of all the others
       // @todo: crude way to bring frames to the top #
       // Done : now, windows put themselves on top of their parents
       // when their titlebar is pressed.
 
       returnValue = true;
     }
     else
     {
       // The user click outside all widgets, focusedWidget is set to null
       setFocusedWidget(null);
     }
 
     if (toDeletePopupWidget != null)
     {
       if (toDeletePopupWidget.equals(popupWidget))
       {
         removePopup();
       }
       else
       {
         // A new popup was added : only removed the 'toDeletePopupWidget'
         removeWidget(toDeletePopupWidget);
       }
     }
 
     return returnValue;
   }
 
   /**
    * Triggers a mouse released event in FengGUI.
    * 
    * @param mouseX
    *          distance of cursor to left hand side of screen
    * @param mouseY
    *          distance of cursor to bottom of screen
    * @param mouseButton
    *          the pressed mouse button
    * @param clickCount
    * @return true if GUI component within Display was hit, false otherwise
    */
   public boolean fireMouseReleasedEvent(int mouseX, int mouseY, MouseButton mouseButton, int clickCount)
   {
     IWidget w = getWidget(mouseX, mouseY);
 
     if (!this.isEnabled())
       return w != null;
 
     boolean ret = false;
     if (draggingListener != null)
     {
       draggingListener.drop(mouseX, mouseY, w, keyPressTracker.getModifiers());
       draggingListener = null;
       ret = true;
     }
 
     if (w.equals(this))
       return ret;
 
     MouseReleasedEvent e = new MouseReleasedEvent(w, mouseX, mouseY, mouseButton, clickCount, keyPressTracker
         .getModifiers());
     w.mouseReleased(e);
     fireGlobalEventListener(e);
 
     return true;
   }
 
   /**
    * Triggers a mouse dragged event.
    * 
    * @param mouseX
    *          distance of cursor to left hand side of screen
    * @param mouseY
    *          distance of cursor to bottom of screen
    * @param mouseButton
    *          the pressed mouse button
    * @return true if GUI component within Display was hit, false otherwise
    */
   public boolean fireMouseDraggedEvent(int mouseX, int mouseY, MouseButton mouseButton, int clickCount)
   {
     IWidget w = getWidget(mouseX, mouseY);
 
     if (!this.isEnabled())
       return w != null;
 
     if (draggingListener != null)
     {
       draggingListener.drag(mouseX, mouseY, keyPressTracker.getModifiers());
     }
 
     if (!mouseOverWidget.equals(w))
     {
       MouseExitedEvent exited = new MouseExitedEvent(w, mouseOverWidget, mouseX, mouseY, mouseButton, clickCount,
           keyPressTracker.getModifiers());
       mouseOverWidget.mouseExited(exited);
       fireGlobalEventListener(exited);
 
       MouseEnteredEvent entered = new MouseEnteredEvent(w, mouseOverWidget, mouseX, mouseY, mouseButton, clickCount,
           keyPressTracker.getModifiers());
       w.mouseEntered(entered);
       fireGlobalEventListener(entered);
     }
     mouseOverWidget = w;
 
     if (w.equals(this))
       return false;
 
     MouseDraggedEvent e = new MouseDraggedEvent(w, mouseX, mouseY, mouseButton, keyPressTracker.getModifiers());
     w.mouseDragged(e);
     fireGlobalEventListener(e);
 
     return true;
   }
 
   public boolean fireMouseDoubleClickEvent(int mouseX, int mouseY, MouseButton mouseButton, int clickCount)
   {
     IWidget w = getWidget(mouseX, mouseY);
 
     if (!this.isEnabled())
       return w != null;
 
     MouseDoubleClickedEvent e = new MouseDoubleClickedEvent(w, mouseX, mouseY, mouseButton, clickCount, keyPressTracker
         .getModifiers());
 
     w.mouseDoubleClicked(e);
     fireGlobalEventListener(e);
 
     return true;
   }
 
   public boolean fireMouseClickEvent(int mouseX, int mouseY, MouseButton mouseButton, int clickCount)
   {
     IWidget w = getWidget(mouseX, mouseY);
 
     if (!this.isEnabled())
       return w != null;
 
     MouseClickedEvent e = new MouseClickedEvent(w, mouseX, mouseY, mouseButton, clickCount, keyPressTracker
         .getModifiers());
 
     w.mouseClicked(e);
     fireGlobalEventListener(e);
 
     return true;
   }
 
   public boolean fireMouseWheel(int mouseX, int mouseY, boolean up, int rotation, int scrollAmount)
   {
     IWidget w = getWidget(mouseX, mouseY);
 
     // IWidget w = getFocusedWidget();
 
     if (!this.isEnabled() && w != null)
       return w != null;
 
     MouseWheelEvent e = new MouseWheelEvent(w, mouseX, mouseY, up, rotation, scrollAmount, keyPressTracker
         .getModifiers());
     w.mouseWheel(e);
     fireGlobalEventListener(e);
 
     // if the widget under the mouse isn't the display, we mustn't send the event
     // elsewhere.
     if (getWidget(mouseX, mouseY) == this)
       return false;
 
     return true;
     /*
      * huh? What are you guys doing here? The focused widget has to receive the mouse
      * wheel event! // do not hit plain display if (!w.equals(this)) { for (Widget wi :
      * notifyList) { if (wi.insideMargin(mouseX, mouseY)) wi.mouseWheel(new
      * MouseWheelEvent(wi, up)); } }
      * 
      * return false;
      */
   }
 
   /**
    * Returns 0 because the display is the root of the widget tree. Realize that this
    * method marks the end of a recursive call.
    */
   public int getDisplayX()
   {
     return 0;
   }
 
   /**
    * Returns 0 becayse the display is the root of the widget tree. Realize that this
    * method marks the end of a recursive call.
    */
   public int getDisplayY()
   {
     return 0;
   }
 
   /**
    * Triggers a mouse moved event. Note that only the Widget over which the mouse cursor
    * is hovering will be notified by the mouse move event.
    * 
    * @param displayX
    *          distance of cursor to left hand side of the screen
    * @param displayY
    *          distance of cursor to bottom of the screen
    * @return true if GUI component within Display was hit, false otherwise
    */
   public boolean fireMouseMovedEvent(int displayX, int displayY, MouseButton button, int clickCount)
   {
     // retrieve Widget below mouse cursor
     IWidget w = getWidget(displayX, displayY);
     if (!this.isEnabled())
       return w != null;
 
     w.mouseMoved(displayX, displayY);
 
     // w points to a different Widget than before!
     if (!mouseOverWidget.equals(w))
     {
       MouseExitedEvent exited = new MouseExitedEvent(w, mouseOverWidget, displayX, displayY, button, clickCount,
           keyPressTracker.getModifiers());
       mouseOverWidget.mouseExited(exited);
       fireGlobalEventListener(exited);
 
       MouseEnteredEvent entered = new MouseEnteredEvent(w, mouseOverWidget, displayX, displayY, button, clickCount,
           keyPressTracker.getModifiers());
       w.mouseEntered(entered);
       fireGlobalEventListener(entered);
     }
     mouseOverWidget = w;
     return !w.equals(this);
   }
 
   public IWidget getWidget(int x, int y)
   {
     IWidget w = super.getWidget(x, y);
     if (w != null)
       return w;
     return this;
   }
 
   public boolean fireKeyPressedEvent(char keyValue, Key keyClass)
   {
     if (!this.isEnabled())
       return focusedWidget != null;
 
     keyPressTracker.setModifierPressed(keyClass);
 
     if (focusedWidget != null)
     {
       KeyPressedEvent e = new KeyPressedEvent(focusedWidget, keyValue, keyClass, this.keyPressTracker.getModifiers());
       focusedWidget.keyPressed(e);
       fireGlobalEventListener(e);
       return true;
     }
     else
     {
       return false;
     }
   }
 
   public boolean fireKeyReleasedEvent(char keyValue, Key keyClass)
   {
     if (!this.isEnabled())
       return focusedWidget != null;
 
     keyPressTracker.setModifierReleased(keyClass);
 
     if (focusedWidget != null)
     {
       KeyReleasedEvent e = new KeyReleasedEvent(focusedWidget, keyValue, keyClass, this.keyPressTracker.getModifiers());
       focusedWidget.keyReleased(e);
       fireGlobalEventListener(e);
       return true;
     }
     else
     {
       return false;
     }
   }
 
   public boolean fireKeyTypedEvent(char keyValue)
   {
     if (!this.isEnabled())
       return focusedWidget != null;
 
     if (focusedWidget != null)
     {
       KeyTypedEvent e = new KeyTypedEvent(focusedWidget, keyValue, this.keyPressTracker.getModifiers());
       focusedWidget.keyTyped(e);
       fireGlobalEventListener(e);
       return true;
     }
     else
     {
       return false;
     }
   }
 
   /**
    * Returns the currently displayed popup widget.
    * 
    * @return popup widget
    */
   public IWidget getPopupWidget()
   {
     return popupWidget;
   }
 
   /**
    * Adds a global drag and drop listener to this display.
    * 
    * @param dndl
    *          the DnD listener
    */
   public void addDndListener(IDragAndDropListener dndl)
   {
     if (!dndListeners.contains(dndl))
       dndListeners.add(dndl);
   }
 
   /**
    * Removes the given drag and drop listener from this display.
    * 
    * @param dndl
    *          the DnD listener
    */
   public void removeDndListener(IDragAndDropListener dndl)
   {
     dndListeners.remove(dndl);
   }
 
   /**
    * Fires the given event through the registered global event listeners
    * 
    * @param event
    *          event to dispatch
    */
   public void fireGlobalEventListener(Event event)
   {
     if (!(event instanceof SizeChangedEvent) && !this.isEnabled())
       return;
 
     if (globalEventListener.isEmpty())
       return;
 
     // Loop this way because sometimes a globalevent will modify the
     // globalEventListeners and cause a ConcurrentModificationException with that.
     // FIXME: This way some listeners may not receive a event. (depends on where they
     // are removed)
     int i = 0;
     while (i < globalEventListener.size())
     {
       globalEventListener.get(i).processEvent(event);
       i++;
     }
   }
 
   /**
    * Adds the given global event listener to this display. The given listener will be
    * notified upon every event except <code>MouseMovedEvent</code>.
    * 
    * @param listener
    *          the global event listener
    */
   public void addGlobalEventListener(IEventListener listener)
   {
     globalEventListener.add(listener);
   }
 
   public boolean isDepthTestEnabled()
   {
     return depthTestEnabled;
   }
 
   public void setDepthTestEnabled(boolean depthTestDisabled)
   {
     this.depthTestEnabled = depthTestDisabled;
   }
 
   /**
    * Removes the global event listener from this display.
    * 
    * @param listener
    *          event listener
    */
   public void removeGlobalEventListener(IEventListener listener)
   {
     globalEventListener.remove(listener);
   }
 
   /**
    * Checks if the <code>focusedWidget</code> is still in the widget tree an if no, sets
    * it to null.
    */
   protected void focusedWidgetValityCheck()
   {
     if (getFocusedWidget() != null && getFocusedWidget().getDisplay() == null)
     {
       setFocusedWidget(null);
     }
   }
 
   /** @return the tooltips */
   public ITooltipManager getTooltips()
   {
     return tooltips;
   }
 
   /**
    * @param tooltips
    *          the tooltips to set
    */
   public void setTooltips(ITooltipManager tooltips)
   {
     this.tooltips = tooltips;
   }
 
   /** @return Returns the enabled. */
   public boolean isEnabled()
   {
     return enabled;
   }
 
   /**
    * @param enabled
    *          The enabled to set.
    */
   public void setEnabled(boolean enabled)
   {
     this.enabled = enabled;
   }
 
   public KeyPressTracker getKeyPressTracker()
   {
     return keyPressTracker;
   }
 
   /* (non-Javadoc)
    * @see org.fenggui.Container#getMinContentSize()
    */
   @Override
   public Dimension getMinContentSize()
   {
     //Displays content size doesn't count
     return null;
   }
   
   
 }
