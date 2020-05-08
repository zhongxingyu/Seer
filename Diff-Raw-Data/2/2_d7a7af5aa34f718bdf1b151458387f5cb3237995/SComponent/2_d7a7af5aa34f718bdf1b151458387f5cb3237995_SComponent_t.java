 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.border.SBorder;
 import org.wings.event.SComponentEvent;
 import org.wings.event.SComponentListener;
 import org.wings.event.SParentFrameEvent;
 import org.wings.event.SParentFrameListener;
 import org.wings.event.SRenderEvent;
 import org.wings.event.SRenderListener;
 import org.wings.io.Device;
 import org.wings.plaf.ComponentCG;
 import org.wings.script.ScriptListener;
 import org.wings.session.LowLevelEventDispatcher;
 import org.wings.session.Session;
 import org.wings.session.SessionManager;
 import org.wings.style.CSSAttributeSet;
 import org.wings.style.CSSProperty;
 import org.wings.style.CSSSelector;
 import org.wings.style.CSSStyle;
 import org.wings.style.CSSStyleSheet;
 import org.wings.style.Style;
 import org.wings.util.ComponentVisitor;
 import javax.swing.*;
 import javax.swing.event.EventListenerList;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.beans.BeanInfo;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.io.IOException;
 import java.io.Serializable;
 import java.lang.reflect.Array;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EventListener;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * Object having a graphical representation that can be displayed on the
  * screen and that can interact with the user.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public abstract class SComponent implements Cloneable, Serializable, Renderable {
 
     private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
 
     private static final Log log = LogFactory.getLog(SComponent.class);
 
     private static final int ACTION_CONDITIONS_AMOUNT = 2;
 
     /* Components unique name. */
     private String name;
 
     /**
      * the session
      */
     private transient Session session;
 
     /**
      * The code generation delegate, which is responsible for
      * the visual representation of this component.
      */
     protected transient ComponentCG cg;
 
     /**
      * Vertical alignment
      */
     protected int verticalAlignment = SConstants.NO_ALIGN;
 
     /**
      * Horizontal alignment
      */
     protected int horizontalAlignment = SConstants.NO_ALIGN;
 
     /**
      * The name of the style class
      */
     protected String style;
 
     /**
      * List of dynamic styles
      */
     protected Map dynamicStyles;
 
     /**
      * Visibility.
      */
     protected boolean visible = true;
 
     /**
      * Enabled / disabled.
      */
     protected boolean enabled = true;
 
     /**
      * The container, this component resides in.
      */
     protected SContainer parent;
 
     /**
      * The frame in which this component resides.
      */
     protected SFrame parentFrame;
 
     /**
      * The border for the component.
      */
     protected SBorder border;
 
     /**
      * The tooltip for this component.
      */
     protected String tooltip;
 
     /**
      * The focus traversal Index
      */
     protected int focusTraversalIndex = -1;
 
     /**
      * Preferred size of component in pixel.
      */
     protected SDimension preferredSize;
 
     /**
      * This is for performance optimizations. With this flag is set, property change
      * events are generated and so every property setter method has to test if a property
      * has changed and temporarily store the old value to generate the property
      * change event
      */
     private boolean fireComponentChangeEvents = false;
 
     /**
      * Generate and fire {@link SParentFrameEvent}s. Performace optimitation
      */
     private boolean fireParentFrameChangeEvents = false;
 
     /**
      * Flag indiccating if {@link SRenderEvent}s should be fired. Used for performace reasons
      */
     private boolean fireRenderEvents = false;
 
     /**
      * All event listeners of this component
      */
     private EventListenerList listeners;
 
     private Boolean useNamedEvents;
 
     private boolean showAsFormComponent = true;
 
     private SPopupMenu popupMenu;
 
     private boolean inheritsPopupMenu;
 
     private InputMap[] inputMaps;
 
     private ActionMap actionMap;
 
     private final Map actionEvents = new HashMap();
 
     private final CSSSelector thisComponentCssSelector = new CSSSelector(this);
 
     private transient SRenderEvent renderEvent;
 
     private SParentFrameListener globalInputMapListener;
 
 
     /**
      * Internal constants for {@link #fireRenderEvent(int)}
      */
     public static final int START_RENDERING = 1;
 
     /**
      * Internal constants for {@link #fireRenderEvent(int)}
      */
     public static final int DONE_RENDERING = 2;
 
     /**
      * Constants for conditions on which actions are triggered. Mainly two
      * cases: the focus has either to be at the component (or at a child)
      * or somewhere in the parent frame.
      * @see #setInputMap(int, javax.swing.InputMap)
      */
     public static final int WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT = 0;
 
     /**
      * Constants for conditions on which actions are triggered. Mainly two
      * cases: the focus has either to be at the component (or at a child)
      * or somewhere in the parent frame.
      * @see #setInputMap(int, javax.swing.InputMap)
      */
     public static final int WHEN_IN_FOCUSED_FRAME = 1;
 
 
     /**
      * Default constructor.cript
      * The method updateCG is called to get a cg delegate installed.
      */
     public SComponent() {
         updateCG();
     }
 
     public SBorder getBorder() {
         return border;
     }
 
     public void setBorder(SBorder border) {
         reloadIfChange(this.border, border);
         this.border = border;
     }
 
     /**
      * Return the parent container.
      *
      * @return the container this component resides in
      */
     public final SContainer getParent() {
         return parent;
     }
 
     /**
      * Sets the parent container. Also gets the parent frame from the parent.
      *
      * @param parent the container
      */
     public void setParent(SContainer parent) {
         reloadIfChange(this.parent, parent);
         this.parent = parent;
         if (parent != null)
             setParentFrame(parent.getParentFrame());
         else
             setParentFrame(null);
     }
 
     /**
      * Sets the parent frame.
      *
      * @param parentFrame the frame
      */
     protected void setParentFrame(SFrame parentFrame) {
         if (this.parentFrame == parentFrame) {
             return;
         }
 
         if (this.parentFrame != null) {
             unregister();
             fireParentFrameEvent(new SParentFrameEvent(this, SParentFrameEvent.PARENTFRAME_REMOVED, this.parentFrame));
         }
 
         this.parentFrame = parentFrame;
 
         if (this.parentFrame != null) {
             register();
             // notify the listeners...
             fireParentFrameEvent(new SParentFrameEvent(this, SParentFrameEvent.PARENTFRAME_ADDED, this.parentFrame));
         }
 
         if (this.popupMenu != null) {
             popupMenu.setParentFrame(parentFrame);
         }
 
         reload();
     }
 
     public void setInheritsPopupMenu(boolean inheritsPopupMenu) {
         reloadIfChange(this.inheritsPopupMenu, inheritsPopupMenu);
         this.inheritsPopupMenu = inheritsPopupMenu;
     }
 
     public boolean getInheritsPopupMenu() {
         return inheritsPopupMenu;
     }
 
     public void setComponentPopupMenu(SPopupMenu popupMenu) {
         reloadIfChange(this.popupMenu, popupMenu);
         if (this.popupMenu != null)
             this.popupMenu.setParentFrame(null);
         this.popupMenu = popupMenu;
         if (this.popupMenu != null)
             this.popupMenu.setParentFrame(getParentFrame());
     }
 
     public SPopupMenu getComponentPopupMenu() {
         /* (OL) we probably don't need the recursive stuff here... */
 //        if (!getInheritsPopupMenu())
 //            return popupMenu;
 //
 //        if (popupMenu == null) {
 //            // Search parents for its popup
 //            SContainer parent = getParent();
 //            while (parent != null) {
 //                if (parent instanceof SComponent) {
 //                    return ((SComponent) parent).getComponentPopupMenu();
 //                }
 //                if (parent instanceof SFrame)
 //                    break;
 //
 //                parent = parent.getParent();
 //            }
 //            return null;
 //        }
         return popupMenu;
     }
 
     public boolean hasComponentPopupMenu() {
         return popupMenu != null;
     }
 
     public RequestURL getRequestURL() {
         SFrame p = getParentFrame();
         if (p == null)
             throw new IllegalStateException("no parent frame");
 
         return p.getRequestURL();
     }
 
     /**
      * Set the preferred size of the receiving component in pixel.
      * It is not guaranteed that the component accepts this property because of
      * missing implementations in the component cg or html properties.
      * If <i>width</i> or <i>height</i> is zero, it is ignored and the browser
      * defines the size.
      *
      * @see org.wings.SComponent#getPreferredSize
      */
     public void setPreferredSize(SDimension preferredSize) {
         reloadIfChange(this.preferredSize, preferredSize);
         this.preferredSize = preferredSize;
     }
 
     /**
      * Get the preferred size of this component.
      *
      * @see SComponent#setPreferredSize
      */
     public SDimension getPreferredSize() {
         return preferredSize;
     }
 
 
     /**
      * Adds the specified component listener to receive component events from
      * this component.
      * If l is null, no exception is thrown and no action is performed.
      *
      * @param l the component listener.
      * @see org.wings.event.SComponentEvent
      * @see org.wings.event.SComponentListener
      * @see org.wings.SComponent#removeComponentListener
      */
     public final void addComponentListener(SComponentListener l) {
         addEventListener(SComponentListener.class, l);
         fireComponentChangeEvents = true;
     }
 
     /**
      * Removes the specified component listener so that it no longer
      * receives component events from this component. This method performs
      * no function, nor does it throw an exception, if the listener
      * specified by the argument was not previously added to this component.
      * If l is null, no exception is thrown and no action is performed.
      *
      * @param l the component listener.
      * @see org.wings.event.SComponentEvent
      * @see org.wings.event.SComponentListener
      * @see org.wings.SComponent#addComponentListener
      */
     public final void removeComponentListener(SComponentListener l) {
         removeEventListener(SComponentListener.class, l);
     }
 
     /**
      * Registers a "parent frame listener" to receive events from
      * this component when the parent frame chanegs i.e. via {@link #setParentFrame(SFrame)}.
      * If l is null, no exception is thrown and no action is performed.
      *
      * @param l the parent frame listener. May be <code>null</code>.
      * @see org.wings.event.SParentFrameEvent
      * @see org.wings.event.SParentFrameListener
      * @see org.wings.SComponent#removeParentFrameListener
      */
     public final void addParentFrameListener(SParentFrameListener l) {
         addEventListener(SParentFrameListener.class, l);
         fireParentFrameChangeEvents = true;
     }
 
     /**
      * Removes the specified parent frame listener so that it no longer
      * receives events from this component. This method performs
      * no function, nor does it throw an exception, if the listener
      * specified by the argument was not previously added to this component.
      * If l is null, no exception is thrown and no action is performed.
      *
      * @param l the parent frame listener.
      * @see org.wings.event.SParentFrameEvent
      * @see org.wings.event.SParentFrameListener
      * @see org.wings.SComponent#addParentFrameListener
      */
     public final void removeParentFrameListener(SParentFrameListener l) {
         removeEventListener(SParentFrameListener.class, l);
     }
 
     /**
      * Reports a component change.
      *
      * @param aEvent report this event to all listeners
      * @see org.wings.event.SComponentListener
      */
     protected void fireComponentChangeEvent(SComponentEvent aEvent) {
         // maybe the better way to do this is to user the getListenerList
         // and iterate through all listeners, this saves the creation of
         // an array but it must cast to the apropriate listener
         Object[] listeners = getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == SComponentListener.class) {
                 // Lazily create the event:
                 processComponentEvent((SComponentListener) listeners[i + 1],
                         aEvent);
             }
         }
 
     }
 
     /**
      * Reports a parent frame change.
      *
      * @param aEvent report this event to all listeners
      * @see org.wings.event.SParentFrameListener
      */
     private void fireParentFrameEvent(SParentFrameEvent aEvent) {
         // are listeners registered?
         if (fireParentFrameChangeEvents) {
             // maybe the better way to do this is to user the getListenerList
             // and iterate through all listeners, this saves the creation of
             // an array but it must cast to the apropriate listener
             Object[] listeners = getListenerList();
             for (int i = listeners.length - 2; i >= 0; i -= 2) {
                 if (listeners[i] == SParentFrameListener.class) {
                     // Lazily create the event:
                     processParentFrameEvent((SParentFrameListener) listeners[i + 1],
                             aEvent);
                 }
             }
         }
 
     }
 
     /**
      * Processes parent frame events occurring on this component by
      * dispatching them to any registered
      * <code>SParentFrameListener</code> objects.
      * <p/>
      *
      */
     private void processParentFrameEvent(SParentFrameListener listener, SParentFrameEvent event) {
         int id = event.getID();
         switch (id) {
             case SParentFrameEvent.PARENTFRAME_ADDED:
                 listener.parentFrameAdded(event);
                 break;
             case SParentFrameEvent.PARENTFRAME_REMOVED:
                 listener.parentFrameRemoved(event);
                 break;
         }
     }
 
     /**
      * Processes component events occurring on this component by
      * dispatching them to any registered
      * <code>SComponentListener</code> objects.
      * <p/>
      * This method is not called unless component events are
      * enabled for this component. Component events are enabled
      * when one of the following occurs:
      * <p><ul>
      * <li>A <code>SComponentListener</code> object is registered
      * via <code>addComponentListener</code>.
      * </ul>
      *
      * @param e the component event.
      * @see org.wings.event.SComponentEvent
      * @see org.wings.event.SComponentListener
      * @see org.wings.SComponent#addComponentListener
      */
     protected void processComponentEvent(SComponentListener listener, SComponentEvent e) {
         int id = e.getID();
         switch (id) {
             case SComponentEvent.COMPONENT_RESIZED:
                 listener.componentResized(e);
                 break;
             case SComponentEvent.COMPONENT_MOVED:
                 listener.componentMoved(e);
                 break;
             case SComponentEvent.COMPONENT_SHOWN:
                 listener.componentShown(e);
                 break;
             case SComponentEvent.COMPONENT_HIDDEN:
                 listener.componentHidden(e);
                 break;
         }
     }
 
     /**
      * Adds the specified component listener to receive component events from
      * this component.
      * If l is null, no exception is thrown and no action is performed.
      * If there is already a ScriptListener which is equal, the new one is not
      * added.
      *
      * @param listener the component listener.
      * @see org.wings.event.SComponentEvent
      * @see org.wings.event.SComponentListener
      * @see org.wings.SComponent#removeComponentListener
      */
     public final void addScriptListener(ScriptListener listener) {
         ScriptListener[] listeners = getScriptListeners();
         for (int i = 0; i < listeners.length; i++) {
             if (listeners[i].equals(listener)) {
                 return;
             }
         }
 	reload();
         addEventListener(ScriptListener.class, listener);
     }
 
 
     /**
      * Removes the specified component listener so that it no longer
      * receives component events from this component. This method performs
      * no function, nor does it throw an exception, if the listener
      * specified by the argument was not previously added to this component.
      * If l is null, no exception is thrown and no action is performed.
      *
      * @param listener the component listener.
      * @see org.wings.event.SComponentEvent
      * @see org.wings.event.SComponentListener
      * @see org.wings.SComponent#addComponentListener
      */
     public final void removeScriptListener(ScriptListener listener) {
         removeEventListener(ScriptListener.class, listener);
         reload();
     }
 
     /**
      * returns the script listeners of this component
      * @return the ScriptListener Array.
      */
     public ScriptListener[] getScriptListeners() {
         return (ScriptListener[]) getListeners(ScriptListener.class);
     }
 
     /**
      * Sets the name property of a component which must be <b>unique</b>!
      * <br/>Assigning the same name multiple times will have strange results!
      *
      * <p>Valid names must begin with a letter ([A-Za-z]) and may be followed by any number of
      * letters, digits ([0-9]), hyphens ("-") and colons (":")
      *
      * <p>If no name is set, it is generated when necessary.
      *
      * <p><i>Explanation:</i> This property is an identifier which is also used inside the generated HTML as HTML element identifier. Hence it
      * must conform to <code>http://www.w3.org/TR/REC-html40/types.html#type-name</code> and respect wingS reserved characters as '.'
      * and '_' {@link SConstants#UID_DIVIDER}
      *
      * @param uniqueName A <b>unique</b> name to set. <b>Only valid identifier as described are allowed!</b>
      * @see Character
      */
     public void setName(String uniqueName) {
         // W3C: ID and NAME tokens must begin with a letter ([A-Za-z]) and may be followed by any number of
         // letters, digits ([0-9]), hyphens ("-"), underscores ("_"), colons (":"), and periods (".").
         //
         // wingS: Low level event dispatcher uses underscore (_) and point (.) so that's why these are forbidden.
         if (uniqueName != null) {
             if (uniqueName.indexOf(SConstants.UID_DIVIDER) >= 0)
                 throw new IllegalArgumentException(uniqueName + " is not a valid identifier (name may not contain SConstants.UID_DIVIDER)");
             if (uniqueName.length() == 0 || !Character.isLetter(uniqueName.charAt(0)))
                 throw new IllegalArgumentException(uniqueName + " is not a valid identifier");
             for (int i=1; i < uniqueName.length(); i++) {
                 final char ch = uniqueName.charAt(i);
                 if (!(Character.isLetter(ch) || Character.isDigit(ch) || ch == '-' || ch == ':'))
                     throw new IllegalArgumentException(uniqueName + " is not a valid identifier");
             }
         }
         setNameRaw(uniqueName);
     }
 
     /**
      * <b>Direct setter for name. Do not use unless you explicitly know what you're doing!</b>
      * (Former package) protected raw setter for component name to avoid sanity check.
      *
      * @param uncheckedName String to use as componentn name/identifier.
      */
     public void setNameRaw(String uncheckedName) {
         reloadIfChange(this.name, uncheckedName);
         this.name = uncheckedName;
     }
 
     /**
      * Gets the name property of a component. This property is an identifier,so it should be always unique.
      * For details refer to {@link #setName(String)}
      * @return The name of the component.
      */
     public final String getName() {
         if (name == null)
             name = getSession().createUniqueId();
         return name;
     }
 
     /**
      * Return the session this component belongs to.
      *
      * @return the session
      */
     public final Session getSession() {
         if (session == null) {
             session = SessionManager.getSession();
         }
 
         return session;
     }
 
     /**
      * Return the dispatcher.
      *
      * @return the dispatcher
      */
     public final LowLevelEventDispatcher getDispatcher() {
         return getSession().getDispatcher();
     }
 
     /*
      * If a subclass implements the {@link LowLevelEventListener} interface,
      * it will be unregistered at the associated dispatcher.
      */
     private final void unregister() {
         if (getDispatcher() != null && this instanceof LowLevelEventListener) {
             getDispatcher().unregister((LowLevelEventListener) this);
         }
     }
 
     /*
      * If a subclass implements the {@link LowLevelEventListener} interface,
      * it will be registered at the associated dispatcher.
      */
     private void register() {
         if (getDispatcher() != null && this instanceof LowLevelEventListener) {
             getDispatcher().register((LowLevelEventListener) this);
         }
     }
 
     /**
      * Set an CSS class name provided by the laf-provided Cascading Style Sheet (CSS), which should
      * be applied to this component.
      *
      * <p>By <b>default</b> this is set to the wingS component class name (i.e. "SLabel").
      *
      * <p>The PLAFs render the value of this String to an <code>class="<i>cssClassName</i>"</code> attribute
      * inside the generated HTML code of this component.
      *
      * <p>The default wingS plaf initializes this by default to the wingS component class name
      * (i.e. <code>SButton</code> for button instances). <br/>Please be aware if you <b>replace</b> this
      * default value, the default wingS style will no longer take effect, as they operate on these
      * default styles. To avoid this you should append your CSS styles via spaces i.e.<br>
      * <code>c.setStyle(c.getStyle + "myStyle");</code>
      *
      * @param cssClassName The new CSS name value for this component
      */
     // <p>Please consider using {@link #addStyle(String)} to avoid disabling of default wingS stylesheet set.
     public void setStyle(String cssClassName) {
         reloadIfChange(style, cssClassName);
         this.style = cssClassName;
     }
 
     /* *
      * Appends the passed style to the current style string. Refer to {@link #setStyle(String)} for details.
      *
      * @param cssClassName Additional CSS class name to apply to this component.
      * /
     public void addStyle(String cssClassName) {
         if (cssClassName != null) {
             if (style != null && !style.contains(cssClassName)) {
                 setStyle(style + " " + cssClassName); // works for all current browsers.
             } else {
                 setStyle(cssClassName);
             }
         }
     }  */
 
 
     /**
      * @return The current CSS style class name. Defaults to <code>null</code>
      * @see #setStyle(String)
      */
     public String getStyle() {
         return style;
     }
 
     public void addDynamicStyle(Style style) {
         if (dynamicStyles == null)
             dynamicStyles = new HashMap(4);
         dynamicStyles.put(style.getSelector(), style);
         reload();
     }
 
     public void removeDynamicStyle(String selector) {
         if (dynamicStyles == null)
             return;
         dynamicStyles.remove(selector);
         reload();
     }
 
     public Style getDynamicStyle(Object selector) {
         if (dynamicStyles == null)
             return null;
         return (Style) dynamicStyles.get(selector);
     }
 
     public void setDynamicStyles(Collection dynamicStyles) {
         if (dynamicStyles == null)
             return;
         if (this.dynamicStyles == null)
             this.dynamicStyles = new HashMap(4);
         for (Iterator iterator = dynamicStyles.iterator(); iterator.hasNext();) {
             Style style = (Style) iterator.next();
             this.dynamicStyles.put(style.getSelector(), style);
         }
         reload();
     }
 
     public Collection getDynamicStyles() {
         if (dynamicStyles == null || dynamicStyles.size() == 0)
             return null;
         return Collections.unmodifiableCollection(dynamicStyles.values());
     }
 
     /** @deprecated Use {@link #setAttribute(org.wings.style.CSSProperty, String)}  */
     public void setAttribute(String cssPropertyName, String value) {
         setAttribute(thisComponentCssSelector, new CSSProperty(cssPropertyName), value);
     }
 
     public void setAttribute(CSSProperty property, String propertyValue) {
         setAttribute(thisComponentCssSelector, property, propertyValue);
    }
 
     public void setAttribute(CSSSelector selector, CSSProperty property, SIcon icon) {
         setAttribute(selector, property, icon != null ? "url('"+icon.getURL().toString()+"')" : "none");
     }
 
     public void setAttribute(CSSSelector selector, CSSProperty property, String propertyValue) {
         CSSStyle style = (CSSStyle) getDynamicStyle(selector);
         if (style == null) {
             addDynamicStyle(new CSSStyle(selector, property, propertyValue));
             reload();
         } else {
             String old = style.put(property, propertyValue);
             reloadIfChange(old, propertyValue);
         }
     }
 
     public void setAttributes(CSSAttributeSet attributes) {
         log.debug("attributes = " + attributes);
         setAttributes(thisComponentCssSelector, attributes);
     }
 
     public void setAttributes(CSSSelector selector, CSSAttributeSet attributes) {
         CSSStyle style = (CSSStyle) getDynamicStyle(selector);
         if (style == null) {
             addDynamicStyle(new CSSStyle(selector, attributes));
             reload();
         } else {
             boolean changed = style.putAll(attributes);
             if (changed)
                 reload();
         }
     }
 
     /**
      * Return the background color.
      *
      * @return the background color
      */
     public Color getBackground() {
         return dynamicStyles == null || dynamicStyles.get(thisComponentCssSelector) == null ? null : CSSStyleSheet.getBackground((CSSAttributeSet) dynamicStyles.get(thisComponentCssSelector));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color
      */
     public void setBackground(Color color) {
         setAttribute(thisComponentCssSelector, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
     }
 
     /**
      * Return the foreground color.
      *
      * @return the foreground color
      */
     public Color getForeground() {
         return dynamicStyles == null || dynamicStyles.get(thisComponentCssSelector) == null ? null : CSSStyleSheet.getForeground((CSSAttributeSet) dynamicStyles.get(thisComponentCssSelector));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color
      */
     public void setForeground(Color color) {
         setAttribute(thisComponentCssSelector, CSSProperty.COLOR, CSSStyleSheet.getAttribute(color));
     }
 
     /**
      * Set the font.
      *
      * @param font the new font
      */
     public void setFont(SFont font) {
         setAttributes(thisComponentCssSelector, CSSStyleSheet.getAttributes(font));
     }
 
     /**
      * Return the font.
      *
      * @return the font
      */
     public SFont getFont() {
         return dynamicStyles == null || dynamicStyles.get(thisComponentCssSelector) == null ? null : CSSStyleSheet.getFont((CSSAttributeSet) dynamicStyles.get(thisComponentCssSelector));
     }
 
     /**
      * Set the visibility.
      *
      * @param visible wether this component will show or not
      */
     public void setVisible(boolean visible) {
         boolean old = this.visible;
         this.visible = visible;
         if (fireComponentChangeEvents && (visible != old)) {
             fireComponentChangeEvent(new SComponentEvent(this, visible
                     ? SComponentEvent.COMPONENT_SHOWN
                     : SComponentEvent.COMPONENT_HIDDEN));
         }
     }
 
     /**
      * Return the visibility. If the Component has a parent which is invisible,
      * this method returns an invisible status.
      * @return wether the component will show
      */
     public boolean isVisible() {
         return visible;
     }
 
     /**
      * Return the visibility. If the Component has a parent which is invisible,
      * this method returns an invisible status.
      * @return wether the component will show
      */
     public boolean isRecursivelyVisible() {
         return visible && (parent == null || parent.isRecursivelyVisible());
     }
 
     /**
      * Set wether this component should be enabled.
      *
      * @param enabled true if the component is enabled, false otherwise
      */
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     /**
      * Return true if this component is enabled.
      *
      * @return true if component is enabled
      */
     public boolean isEnabled() {
         return enabled;
     }
 
     /**
      * Mark the component as subject to reload.
      * The component will be registered with the ReloadManager.
      */
     public final void reload() {
         getSession().getReloadManager().reload(this);
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload()}
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(Object oldVal, Object newVal) {
         if (!((oldVal == newVal) || (oldVal != null && oldVal.equals(newVal)))) {
             //System.err.println(getClass().getDescription() + ": reload. old:" + oldVal + "; new: "+ newVal);
             reload();
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload()}
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(int oldVal, int newVal) {
         if (oldVal != newVal) {
             reload();
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload()}
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(boolean oldVal, boolean newVal) {
         if (oldVal != newVal) {
             reload();
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload()}
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(byte oldVal, byte newVal) {
         if (oldVal != newVal) {
             reload();
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload()}
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(short oldVal, short newVal) {
         if (oldVal != newVal) {
             reload();
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload()}
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(long oldVal, long newVal) {
         if (oldVal != newVal) {
             reload();
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload()}
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(float oldVal, float newVal) {
         if (oldVal != newVal) {
             reload();
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload()}
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(double oldVal, double newVal) {
         if (oldVal != newVal) {
             reload();
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload()}
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(char oldVal, char newVal) {
         if (oldVal != newVal) {
             reload();
         }
     }
 
     /**
      * Let the code generator delegate write the component's code
      * to the device. The code generator is the actual 'plaf'.
      *
      * @param s the Device to write into
      * @throws IOException Thrown if the connection to the client gets broken,
      *                     for example when the user stops loading
      */
     public void write(Device s) throws IOException {
         try {
             if (visible)
                 cg.write(s, this);
         } catch (IOException se) {
             // Typical double-clicks. Not severe
             log.debug( "Not Severe: Socket exception during code generation for " + getClass().getName() + se);
         } catch (NoClassDefFoundError e) {
             // Mostly happens when DWR jar is not in classpath
             log.fatal( "Error during code generation for " + getClass().getName(), e);
         } catch (Throwable t) {
             // should we warn here? or maybe throw an error...
             log.error( "Exception during code generation for " + getClass().getName(), t);
         }
     }
 
     /**
      * a string representation of this component. Just
      * renders the component into a string.
      */
     public String toString() {
         return paramString();
     }
 
 
     /**
      * Generic implementation for generating a string that represents
      * the components configuration.
      *
      * @return a string containing all properties
      */
     public String paramString() {
         StringBuffer buffer = new StringBuffer(getClass().getName());
         buffer.append("[");
 
         try {
             BeanInfo info = Introspector.getBeanInfo(getClass());
             PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
 
             boolean first = true;
             for (int i = 0; i < descriptors.length; i++) {
                 try {
                     Method getter = descriptors[i].getReadMethod();
                     if (getter == null || getter.getName().startsWith("getParent"))
                         continue;
                     // System.out.println("invoking " + this.getClass().getDescription()+"."+getter.getDescription());
                     Object value = getter.invoke(this, null);
                     if (first)
                         first = false;
                     else
                         buffer.append(",");
                     buffer.append(descriptors[i].getName() + "=" + value);
                 } catch (Exception e) {
                 }
             }
         } catch (Exception e) {
         }
 
         buffer.append("]");
         return buffer.toString();
     }
 
     /**
      * Encodes a low level event id for using it in a request parameter. Every
      * {@link LowLevelEventListener} should encode its LowLevelEventId before
      * using it in a request parameter. This encoding adds consistency checking
      * for outtimed requests ("Back Button")
      */
     private String encodeLowLevelEventId(String lowLevelEventId) {
         if (getParentFrame() != null)
             if (!(this instanceof LowLevelEventListener) ||
                     ((LowLevelEventListener) this).isEpochCheckEnabled()) {
                 return (getParentFrame().getEventEpoch()
                         + SConstants.UID_DIVIDER
                         + lowLevelEventId);
             }
         return lowLevelEventId;
     }
 
     /**
      * Encodes a low level event id for using it in a request parameter. Every
      * {@link LowLevelEventListener} should encode its LowLevelEventId before
      * using it in a request parameter. This encoding adds consistency checking
      * for outtimed requests ("Back Button")
      */
     public final String getEncodedLowLevelEventId() {
         if (getUseNamedEvents() && getName() != null)
             return name;
         else
             return encodeLowLevelEventId(getLowLevelEventId());
     }
 
     private boolean getUseNamedEvents() {
         if (useNamedEvents == null) {
             useNamedEvents = ("true".equalsIgnoreCase((String) getSession().getProperty("wings.event.usenames")))
                     ? Boolean.TRUE : Boolean.FALSE;
         }
         return useNamedEvents.booleanValue();
     }
 
     /**
      * Default implementation of the method in
      * {@link LowLevelEventListener}.
      */
     public String getLowLevelEventId() {
         return getName();
     }
 
     /**
      * Return the parent frame.
      * <p><b>NOTE:</b> You will receive <code>null</code> if you call this i.e. during
      * component creation time, as the parent frame is set when you add it to a visible {@link SContainer}.
      * Use {@link #addParentFrameListener(org.wings.event.SParentFrameListener)} in this case.
      *
      * @see #addParentFrameListener(org.wings.event.SParentFrameListener)
      *
      * @return the parent frame
      */
     public SFrame getParentFrame() {
         return parentFrame;
     }
 
     /**
      * Return true, if this component is contained in a form.
      *
      * @return true, if this component resides in a form, false otherwise
      */
     public final boolean getResidesInForm() {
         SComponent parent = getParent();
 
         boolean actuallyDoes = false;
         while (parent != null && !(actuallyDoes = (parent instanceof SForm))) {
             parent = parent.getParent();
         }
 
         return actuallyDoes;
     }
 
     /**
      * Set the tooltip text.
      *
      * @param t the new tooltip text
      */
     public void setToolTipText(String t) {
         tooltip = t;
     }
 
     /**
      * Return the tooltip text.
      *
      * @return the tooltip text
      */
     public String getToolTipText() {
         return tooltip;
     }
 
     /**
      * The index in which the focus is traversed using Tab. This is
      * a very simplified notion of traversing the focus, but that is,
      * what browser like interfaces currently offer. This has a bit rough
      * edge, since you have to make sure, that the index is unique within
      * the whole frame. You probably don't want to change this
      * programmatically, but this is set usually by the template property
      * manager.
      *
      * @param index the focus traversal index. Pressing the focus traversal
      *              key (usually TAB) in the browser jumps to the next index.
      *              Must not be zero.
      */
     public void setFocusTraversalIndex(int index) {
         focusTraversalIndex = index;
     }
 
     /**
      * returns the focus traversal index.
      *
      * @see #setFocusTraversalIndex(int)
      */
     public int getFocusTraversalIndex() {
         return focusTraversalIndex;
     }
 
     /**
      * Clone this component.
      *
      * @return a clone of this component
      */
     public Object clone() {
         try {
             return super.clone();
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
     /**
      * Return the value of the horizontal alignment property.
      *
      * @return the horizontal alignment
      * @see SConstants
      */
     public int getHorizontalAlignment() {
         return horizontalAlignment;
     }
 
     /**
      * Set the horizontal alignment.
      *
      * @param alignment new value for the horizontal alignment
      * @see SConstants
      */
     public void setHorizontalAlignment(int alignment) {
         horizontalAlignment = alignment;
     }
 
     /**
      * Set the vertical alignment.
      *
      * @param alignment new value for the vertical alignment
      * @see SConstants
      */
     public void setVerticalAlignment(int alignment) {
         verticalAlignment = alignment;
     }
 
     /**
      * Return the value of the vertical alignment property.
      *
      * @return the vertical alignment
      * @see SConstants
      */
     public int getVerticalAlignment() {
         return verticalAlignment;
     }
 
     private Map clientProperties;
 
     /**
      * @return a small HashMap
      * @see #putClientProperty
      * @see #getClientProperty
      */
     private Map getClientProperties() {
         if (clientProperties == null) {
             clientProperties = new HashMap(2);
         }
         return clientProperties;
     }
 
 
     /**
      * Returns the value of the property with the specified key.  Only
      * properties added with <code>putClientProperty</code> will return
      * a non-null value.
      *
      * @return the value of this property or null
      * @see #putClientProperty
      */
     public final Object getClientProperty(Object key) {
         if (clientProperties == null) {
             return null;
         } else {
             return getClientProperties().get(key);
         }
     }
 
     /**
      * Add an arbitrary key/value "client property" to this component.
      * <p/>
      * The <code>get/putClientProperty<code> methods provide access to
      * a small per-instance hashtable. Callers can use get/putClientProperty
      * to annotate components that were created by another module, e.g. a
      * layout manager might store per child constraints this way.  For example:
      * <pre>
      * componentA.putClientProperty("to the left of", componentB);
      * </pre>
      * <p/>
      * If value is null this method will remove the property.
      * Changes to client properties are reported with PropertyChange
      * events.  The name of the property (for the sake of PropertyChange
      * events) is <code>key.toString()</code>.
      * <p/>
      * The clientProperty dictionary is not intended to support large
      * scale extensions to SComponent nor should be it considered an
      * alternative to subclassing when designing a new component.
      *
      * @see #getClientProperty
      */
     public final void putClientProperty(Object key, Object value) {
         if (value != null) {
             getClientProperties().put(key, value);
         } else {
             getClientProperties().remove(key);
         }
     }
 
 
     /**
      * Set the look and feel delegate for this component.
      * SComponent subclasses generally override this method
      * to narrow the argument type, e.g. in STextField:
      * <pre>
      * public void setCG(TextFieldCG newCG) {
      *     super.setCG(newCG);
      * }
      * </pre>
      *
      * @see #updateCG
      * @see org.wings.plaf.CGManager#getLookAndFeel
      * @see org.wings.plaf.CGManager#getCG
      */
     public void setCG(ComponentCG newCG) {
         /* We do not check that the CG instance is different
          * before allowing the switch in order to enable the
          * same CG instance *with different default settings*
          * to be installed.
          */
         if (cg != null) {
             cg.uninstallCG(this);
         }
         ComponentCG oldCG = cg;
         cg = newCG;
         if (cg != null) {
             cg.installCG(this);
         }
         reloadIfChange(cg, oldCG);
     }
 
     /**
      * Return the look and feel delegate.
      *
      * @return the componet's cg
      */
     public ComponentCG getCG() {
         return cg;
     }
 
     /**
      * Notification from the CGFactory that the L&F has changed.
      *
      * @see SComponent#updateCG
      */
     public void updateCG() {
         if (getSession() == null) {
             log.warn("no session yet.");
         } else if (getSession().getCGManager() == null) {
             log.warn("no CGManager");
         } else {
             setCG(getSession().getCGManager().getCG(this));
         }
     }
 
     /**
      * Invite a ComponentVisitor.
      * Invokes visit(SComponent) on the ComponentVisitor.
      *
      * @param visitor the visitor to be invited
      */
     public void invite(ComponentVisitor visitor)
             throws Exception {
         visitor.visit(this);
     }
 
     /**
      * use this method for changing a variable. if a new value is different
      * from the old value set the new one and notify e.g. the reloadmanager...
      */
     protected static final boolean isDifferent(Object oldObject,
                                                Object newObject) {
         if (oldObject == newObject)
             return false;
 
         if (oldObject == null)
            return newObject != null;
 
         return !oldObject.equals(newObject);
     }
 
     /**
      * Adds an event listener for the given event class
      * @param type The class/type of events to listen to.
      * @param listener The listener itself.
      */
     protected final void addEventListener(Class type, EventListener listener) {
         if (listeners == null) {
             listeners = new EventListenerList();
         }
         listeners.add(type, listener);
     }
 
     /**
      * Removed named event listener.
      * @param type The class/type of events to listen to.
      * @param listener The listener itself.
      */
     protected final void removeEventListener(Class type, EventListener listener) {
         if (listeners != null) {
             listeners.remove(type, listener);
         }
     }
 
     /**
      * Returns the number of listeners of the specified type for this component.
      *
      * @param type The type of listeners
      * @return The number of listeners
      * @see EventListenerList
      */
     protected final int getListenerCount(Class type) {
         if (listeners != null) {
             return listeners.getListenerCount(type);
         } else {
             return 0;
         }
     }
 
     /**
      * Returns all the listeners of this component. For performance reasons, this is the actual data
      * structure and so no modification of this array should be made.
      *
      * @return All listeners of this component. The result array has a pair structure,
      *         the first element of each pair is the listener type, the second the listener
      *         itself. It is guaranteed that this returns a non-null array.
      * @see EventListenerList
      */
     protected final Object[] getListenerList() {
         if (listeners == null) {
             return EMPTY_OBJECT_ARRAY;
         } else {
             return listeners.getListenerList();
         } // end of else
     }
 
     /**
      * Creates an typed array of all listeners of the specified type
      *
      * @param type All listeners of this type are added to the result array
      * @return an array of the specified type with all listeners of the specified type
      * @see EventListenerList
      */
     protected final EventListener[] getListeners(Class type) {
         if (listeners != null) {
             return listeners.getListeners(type);
         } else {
             return (EventListener[]) Array.newInstance(type, 0);
         }
     }
 
     /**
      * Adds a listenere to this component which wil get notified when the rendering of
      * this components starts. (This happens after all events / request has been processed and wingS
      * starts to build the response).
      *
      * @param renderListener
      * @see SRenderListener
      */
     public final void addRenderListener(SRenderListener renderListener) {
         addEventListener(SRenderListener.class, renderListener);
         fireRenderEvents = true;
     }
 
 
     /**
      * Removes the named render listener.
      *
      * @param renderListener Render listener to remove
      * @see #addRenderListener(org.wings.event.SRenderListener)
      * @see SRenderListener
      */
     public final void removeRenderListener(SRenderListener renderListener) {
         removeEventListener(SRenderListener.class, renderListener);
     }
 
     /**
      * Called by the CGs to indicate different states of the rendering process.
      * @param type
      */
     public final void fireRenderEvent(int type) {
         if (fireRenderEvents) {
             // maybe the better way to do this is to user the getListenerList
             // and iterate through all listeners, this saves the creation of
             // an array but it must cast to the apropriate listener
             Object[] listeners = getListenerList();
             for (int i = listeners.length - 2; i >= 0; i -= 2) {
                 if (listeners[i] == SRenderListener.class) {
                     // Lazily create the event:
                     if (renderEvent == null) {
                         renderEvent = new SRenderEvent(this);
                     } // end of if ()
 
                     switch (type) {
                         case START_RENDERING:
                             ((SRenderListener) listeners[i + 1]).startRendering(renderEvent);
                             break;
                         case DONE_RENDERING:
                             ((SRenderListener) listeners[i + 1]).doneRendering(renderEvent);
                             break;
                     }
                 }
             }
         }
     }
 
     /**
      * Forwards the scrollRectToVisible() message to the SComponent's
      * parent. Components that can service the request, such as
      * SScrollPane, override this method and perform the scrolling.
      *
      * @param aRect the visible Rectangle
      * @see SScrollPane
      */
     public void scrollRectToVisible(Rectangle aRect) {
         if (parent != null) {
             parent.scrollRectToVisible(aRect);
         }
     }
 
     /**
      * Requests the edit focus for this component for the following renderings
      * by calling {@link SFrame#setFocus(SComponent)}.
      */
     public void requestFocus() {
         if (getParentFrame() != null) {
             getParentFrame().setFocus(this);
         }
     }
 
     /**
      * Returns <code>true</code> if this <code>SComponent</code> is owning the edit focus.
      *
      * @return <code>true</code> if this <code>SComponent</code> is owning the edit focus otherwise <code>false</code>
      * @see #requestFocus()
      */
     public boolean isFocusOwner() {
         if (getParentFrame() != null)
             return this == getParentFrame().getFocus();
         return false;
     }
 
     /**
      * Set display mode (href or form-component).
      * An AbstractButton can appear as HTML-Form-Button or as
      * HTML-HREF. If button is inside a {@link SForm} the default
      * is displaying it as html form button.
      * Setting <i>showAsFormComponent</i> to <i>false</i> will
      * force displaying as href even if button is inside
      * a form.
      *
      * @param showAsFormComponent if true, display as link, if false as html form component.
      */
     public void setShowAsFormComponent(boolean showAsFormComponent) {
         if (this.showAsFormComponent != showAsFormComponent) {
             this.showAsFormComponent = showAsFormComponent;
             reload();
         }
     }
 
     /**
      * Test, what display method is set.
      *
      * @return true, if displayed as link, false when displayed as html form component.
      * @see #setShowAsFormComponent(boolean)
      */
     public boolean getShowAsFormComponent() {
         return showAsFormComponent && getResidesInForm();
     }
 
     /**
      * Binds action names to {@link Action}s. Use for key binding feature.
      *
      * @param actionMap The new action map.
      * @see #setInputMap(javax.swing.InputMap)
      * @see ActionMap
      * @see InputMap
      */
     public void setActionMap(ActionMap actionMap) {
         this.actionMap = actionMap;
     }
 
     /**
      * Action map for key binding feature
      * @return The current action map
      * @see #setActionMap(javax.swing.ActionMap)
      */
     public ActionMap getActionMap() {
         if (actionMap == null)
             actionMap = new ActionMap();
         return actionMap;
     }
 
 
     /**
      * Map for key binding feature. (?) Binds input keystrokes to action names (?).
      * @param inputMap The current input map.
      * @see InputMap
      * @see ActionMap
      * @see #setActionMap(javax.swing.ActionMap)
      * @see JComponent#setInputMap(int, javax.swing.InputMap)
      */
     public void setInputMap(InputMap inputMap) {
         setInputMap(WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
     }
 
     /**
      * Sets The current input map.
      * @param inputMap The new input map
      * @param condition Either {@link #WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT} or {@link #WHEN_IN_FOCUSED_FRAME}
      * @see JComponent#setInputMap(int, javax.swing.InputMap)
      */
     public void setInputMap(int condition, InputMap inputMap) {
         initInputMaps();
         this.inputMaps[condition] = inputMap;
         if (condition == WHEN_IN_FOCUSED_FRAME && inputMap != null) {
             registerGlobalInputMapWithFrame();
         }
     }
 
     /**
      * @return The input map for the condition {@link #WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT}
      * @see #setInputMap(javax.swing.InputMap)
      */
     public InputMap getInputMap() {
         return getInputMap(WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT);
     }
 
     /**
      * @return The input map for the given condition.
      * @see #setInputMap(int, javax.swing.InputMap)
      * @param condition Either {@link #WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT} or {@link #WHEN_IN_FOCUSED_FRAME}
      */
     public InputMap getInputMap(int condition) {
         initInputMaps();
         InputMap result = inputMaps[condition];
         if (result == null) {
             inputMaps[condition] = new InputMap();
             result = inputMaps[condition];
         }
         if (condition == WHEN_IN_FOCUSED_FRAME) {
             // map could be filled
             registerGlobalInputMapWithFrame();
         }
         return result;
     }
 
 
     private void registerGlobalInputMapWithFrame() {
         final SComponent me = this;
         final SFrame parentFrame = getParentFrame();
         if (parentFrame != null) {
             parentFrame.registerGlobalInputMapComponent(me);
         } else {
             if (globalInputMapListener == null) {
                 globalInputMapListener = new SParentFrameListener() {
                     public void parentFrameAdded(SParentFrameEvent e) {
                         e.getParentFrame().registerGlobalInputMapComponent(me);
                     }
                     public void parentFrameRemoved(SParentFrameEvent e) {
                     }
                 };
                 addParentFrameListener(globalInputMapListener);
             }
         }
     }
 
     private void initInputMaps() {
         if (inputMaps == null) {
             inputMaps = new InputMap[ACTION_CONDITIONS_AMOUNT];
         }
     }
 
     protected void processLowLevelEvent(String name, String[] values) {
     }
 
     protected boolean processKeyEvents(String[] values) {
         if (actionMap == null)
             return false;
 
         if (log.isDebugEnabled())
             log.debug("processKeyEvents " + Arrays.asList(values));
 
         boolean arm = false;
         for (int i = 0; i < values.length; i++) {
             String value = values[i];
             Action action = actionMap.get(value);
             if (action != null) {
                 actionEvents.put(action, new ActionEvent(this, 0, value));
                 arm = true;
             }
         }
         if (arm)
             SForm.addArmedComponent((LowLevelEventListener) this);
 
         return arm;
     }
 
     /**
      * Internal event trigger used by CGs.
      * This Method is called internal and should not be called directly
      */
     public void fireFinalEvents() {
         fireKeyEvents();
     }
 
     protected void fireKeyEvents() {
         for (Iterator iterator = actionEvents.entrySet().iterator(); iterator.hasNext();) {
             Map.Entry entry = (Map.Entry) iterator.next();
             Action action = (Action) entry.getKey();
             ActionEvent event = (ActionEvent) entry.getValue();
             action.actionPerformed(event);
         }
         actionEvents.clear();
     }
 
     /**
      * Makes this <code>SComponent</code> unavailable.
      * This Method is called internal and should not be called directly
      */
     public void removeNotify() {
     }
 
     // Nice: undocumented and useless
     /*public ArrayList getMenus() {
         ArrayList menus = new ArrayList();
         if (isVisible()) {
             SPopupMenu pmenu = getComponentPopupMenu();
             if (pmenu != null) {
                 menus.add(pmenu);
             }
         }
         return menus;
     } */
 }
