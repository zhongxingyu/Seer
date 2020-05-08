 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.beans.*;
 import java.io.*;
 import java.lang.reflect.*;
 import java.util.*;
 import java.util.logging.*;
 import javax.swing.event.EventListenerList;
 
 import org.wings.border.SBorder;
 import org.wings.event.*;
 import org.wings.io.Device;
 import org.wings.plaf.*;
 import org.wings.plaf.ComponentCG;
 import org.wings.script.ScriptListener;
 import org.wings.session.LowLevelEventDispatcher;
 import org.wings.session.Session;
 import org.wings.session.SessionManager;
 import org.wings.style.*;
 import org.wings.util.*;
 import org.wings.script.JavaScriptListener;
 
 /**
  * The basic component implementation for all components in this package.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public abstract class SComponent
     implements SConstants, Cloneable, Serializable, Renderable {
 
     public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
 
     public static final String ENABLED_PROPERTY = "_Enabled_Property";
     public static final String VISIBLE_PROPERTY = "_Visible_Property";
     public static final String OPAQUE_PROPERTY = "_Opaque_Property";
     public static final String BORDER_PROPERTY = "_Border_Property";
     public static final String NAME_PROPERTY = "_Name_Property";
 
     private final static Logger logger = Logger.getLogger("org.wings");
 
     /* */
     private transient String componentId;
 
     /** @see #getCGClassID */
     private static final String cgClassID = "ComponentCG";
 
     /** the session */
     private transient Session session;
 
     /**
      * The code generation delegate, which is responsible for
      * the visual representation of this component.
      */
     protected transient ComponentCG cg;
 
     /** Vertical alignment */
     protected int verticalAlignment = NO_ALIGN;
 
     /** Horizontal alignment */
     protected int horizontalAlignment = NO_ALIGN;
 
     /** The name of the style class */
     protected String style;
 
     /** The name of the style class */
     protected String disabledStyle;
 
     /** The attributes */
     protected AttributeSet attributes;
 
     /** Visibility. */
     protected boolean visible = true;
 
     /** Enabled / disabled. */
     protected boolean enabled = true;
 
     /**  */
     protected boolean opaque = true;
 
     /** The container, this component resides in. */
     protected SContainer parent;
 
     /** The frame, this component resides in. */
     protected SFrame parentFrame;
 
     /** The name of the component. */
     protected String name;
 
     /** The border for the component. */
     protected SBorder border;
 
     /** The tooltip for this component. */
     protected String tooltip;
 
     /** The focus traversal Index */
     protected int focusTraversalIndex = -1;
 
     /** Preferred size of component in pixel. */
     protected SDimension preferredSize;
 
 
     /**
      * This is for performance optimizations. With this flag is set, property change
      * events are generated and so every property setter method has to test if a property
      * has changed and temporarily store the old value to generate the property
      * change event
      */
     private boolean firePropertyChangeEvents = false;
 
     private boolean fireComponentChangeEvents = false;
 
     private EventListenerList listeners;
     private Boolean useNamedEvents;
 
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
         if (firePropertyChangeEvents) {
             if (this.border != border) {
                 SBorder oldBorder = this.border;
 
                 this.border = border;
 
                 firePropertyChange(BORDER_PROPERTY, oldBorder, border);
             }
         } else {
             this.border = border;
         }
     }
 
     /**
      * Return the parent container.
      * @return the container this component resides in
      */
     public final SContainer getParent() {
         return parent;
     }
 
     /**
      * Sets the parent container. Also gets the parent frame from the parent.
      *
      * @param p the container
      */
     public void setParent(SContainer p) {
         parent = p;
         if (p != null)
             setParentFrame(p.getParentFrame());
         else
             setParentFrame(null);
     }
 
     /**
      * Sets the parent frame.
      *
      * @param f the frame
      */
     protected void setParentFrame(SFrame f) {
         if (f != parentFrame) {
             unregister();
             parentFrame = f;
             register();
         }
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
      * @see org.wings.SComponent#getPreferredSize
      */
     public void setPreferredSize(SDimension preferredSize) {
         this.preferredSize = preferredSize;
     }
 
     /**
      * Get the preferred size of this component.
      * @see SComponent#setPreferredSize
      */
     public SDimension getPreferredSize() {
         return preferredSize;
     }
 
 
     /**
      * Adds the specified component listener to receive component events from
      * this component.
      * If l is null, no exception is thrown and no action is performed.
      * @param    l   the component listener.
      * @see      org.wings.event.SComponentEvent
      * @see      org.wings.event.SComponentListener
      * @see      org.wings.SComponent#removeComponentListener
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
      * @param    l   the component listener.
      * @see      org.wings.event.SComponentEvent
      * @see      org.wings.event.SComponentListener
      * @see      org.wings.SComponent#addComponentListener
      */
     public final void removeComponentListener(SComponentListener l) {
         removeEventListener(SComponentListener.class, l);
     }
 
     /**
      * Reports a component change.
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
                 processComponentEvent((SComponentListener)listeners[i + 1],
                                       aEvent);
             }
         }
 
     }
 
     /**
      * Processes component events occurring on this component by
      * dispatching them to any registered
      * <code>SComponentListener</code> objects.
      * <p>
      * This method is not called unless component events are
      * enabled for this component. Component events are enabled
      * when one of the following occurs:
      * <p><ul>
      * <li>A <code>SComponentListener</code> object is registered
      * via <code>addComponentListener</code>.
      * </ul>
      * @param       e the component event.
      * @see         org.wings.event.SComponentEvent
      * @see         org.wings.event.SComponentListener
      * @see         org.wings.SComponent#addComponentListener
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
      * @param    listener   the component listener.
      * @see      org.wings.event.SComponentEvent
      * @see      org.wings.event.SComponentListener
      * @see      org.wings.SComponent#removeComponentListener
      */
     public final void addScriptListener(ScriptListener listener) {
         addEventListener(ScriptListener.class, listener);
     }
     
    
 
     /**
      * Removes the specified component listener so that it no longer
      * receives component events from this component. This method performs
      * no function, nor does it throw an exception, if the listener
      * specified by the argument was not previously added to this component.
      * If l is null, no exception is thrown and no action is performed.
      * @param    listener   the component listener.
      * @see      org.wings.event.SComponentEvent
      * @see      org.wings.event.SComponentListener
      * @see      org.wings.SComponent#addComponentListener
      */
     public final void removeScriptListener(ScriptListener listener) {
         removeEventListener(ScriptListener.class, listener);
     }
 
     public ScriptListener[] getScriptListeners() {
         return (ScriptListener[])getListeners(ScriptListener.class);
     }
 
 
     /**
      * Return a jvm wide unique id.
      * @return an id
      */
     public final String getComponentId() {
         if (componentId == null)
             componentId = getSession().createUniqueId();
         return componentId;
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
 
     /**
      * Set the locale.
      *
      * @param l the new locale
      */
     public void setLocale(Locale l) {
         getSession().setLocale(l);
     }
 
     /**
      * Return the local.
      *
      * @return the locale
      */
     public final Locale getLocale() {
         return getSession().getLocale();
     }
 
     /*
      * If a subclass implements the {@link LowLevelEventListener} interface,
      * it will be unregistered at the associated dispatcher.
      */
     private final void unregister() {
         if (getDispatcher() != null && this instanceof LowLevelEventListener) {
             getDispatcher().unregister((LowLevelEventListener)this);
         }
     }
 
     /*
      * If a subclass implements the {@link LowLevelEventListener} interface,
      * it will be registered at the associated dispatcher.
      */
     private final void register() {
         if (getDispatcher() != null && this instanceof LowLevelEventListener) {
             getDispatcher().register((LowLevelEventListener)this);
         }
     }
 
     /**
      * Watch components beeing garbage collected.
      */
     /* comment out, unless explicitly debugged ..
        protected void finalize() {
        System.out.println("finalize " + getClass().getName());
        }
     */
 
     /**
      * Set the class of the laf-provided style.
      * @param value the new value for style
      */
     public void setStyle(String value) {
         reloadIfChange(ReloadManager.RELOAD_CODE, style, value);
         this.style = value;
     }
 
     /**
      * @return the current style
      */
     public String getStyle() {
         return style;
     }
 
     /**
      * Set the class of the laf-provided style.
      * @param value the new value for style
      */
     public void setDisabledStyle(String value) {
         reloadIfChange(ReloadManager.RELOAD_CODE, disabledStyle, value);
         this.disabledStyle = value;
     }
 
     /**
      * @return the current style
      */
     public String getDisabledStyle() {
         return disabledStyle;
     }
 
     /**
      * Set a attribute.
      * @param name the attribute name
      * @param value the attribute value
      */
     public void setAttribute(String name, String value) {
         if ( attributes == null ) {
             attributes = new SimpleAttributeSet();
         }
         String oldVal = attributes.put(name, value);
         reloadIfChange(ReloadManager.RELOAD_STYLE, oldVal, value);
     }
 
     /**
      * return the value of an attribute.
      * @param name the attribute name
      */
     public String getAttribute(String name) {
         return attributes == null ? null : attributes.get(name);
     }
 
     /**
      * remove an attribute
      * @param name the attribute name
      */
     public String removeAttribute(String name) {
         if (attributes!=null && attributes.contains(name)) {
             String value = attributes.remove(name);
 
             reload(ReloadManager.RELOAD_STYLE);
 
             return value;
         }
 
         return null;
     }
 
 
     /**
      * Set the attributes.
      * @param newAttributes the attributes
      */
     public void setAttributes(AttributeSet newAttributes) {
         if (newAttributes == null) {
             throw new IllegalArgumentException("null not allowed");
         }
         reloadIfChange(ReloadManager.RELOAD_STYLE, attributes, newAttributes);
         attributes = newAttributes;
     }
 
     /**
      * @return the current attributes
      */
     public AttributeSet getAttributes() {
         return attributes==null ? AttributeSet.EMPTY_ATTRIBUTESET : attributes;
     }
 
     /**
      * Set the background color.
      * @param color the new background color
      */
     public void setBackground(Color color) {
         setAttribute(Style.BACKGROUND_COLOR,
                      CSSStyleSheet.getAttribute(color));
     }
 
     /**
      * Return the background color.
      * @return the background color
      */
     public Color getBackground() {
         return attributes == null ? null : CSSStyleSheet.getBackground(attributes);
     }
 
     /**
      * Set the foreground color.
      * @param color the new foreground color
      */
     public void setForeground(Color color) {
         setAttribute(Style.COLOR, CSSStyleSheet.getAttribute(color));
     }
 
     /**
      * Return the foreground color.
      * @return the foreground color
      */
     public Color getForeground() {
         return attributes == null ? null : CSSStyleSheet.getForeground(attributes);
     }
 
     /**
      * Set the font.
      * @param f the new font
      */
     public void setFont(Font f) {
         if (f == null) {
             setFont((SFont)null);
             return;
         }
 
         SFont font = new SFont(f.getName(), f.getStyle(), f.getSize());
         setFont(font);
     }
 
     /**
      * Set the font.
      * @param font the new font
      */
     public void setFont(SFont font) {
         if ( attributes==null ) {
             attributes = new SimpleAttributeSet();
         }
 
         boolean changed = attributes.putAll(CSSStyleSheet.getAttributes(font));
         if (changed) {
             reload(ReloadManager.RELOAD_STYLE);
         }
     }
 
     /**
      * Return the font.
      * @return the font
      */
     public SFont getFont() {
         return attributes == null ? null : CSSStyleSheet.getFont(attributes);
     }
 
     /**
      * Set the visibility.
      * @param visible wether this component will show or not
      */
     public void setVisible(boolean visible) {
         final boolean old = SComponent.this.visible;
         if (firePropertyChangeEvents) {
             if (this.visible != visible) {
                 this.visible = visible;
                 firePropertyChange(VISIBLE_PROPERTY,
                                    Boolean.valueOf(!visible),
                                    Boolean.valueOf(visible));
             }
         } else {
             this.visible = visible;
         }
         if (fireComponentChangeEvents && (visible != old)) {
             fireComponentChangeEvent(
                 new SComponentEvent(this, visible
                                           ? SComponentEvent.COMPONENT_SHOWN
                                           : SComponentEvent.COMPONENT_HIDDEN));
         }
     }
 
     /**
      * Return the visibility.
      *
      * @return wether the component will show
      * @deprecated use isVisible instead
      */
     public boolean getVisible() {
         return visible;
     }
 
     /**
      * Return the visibility.
      *
      * @return wether the component will show
      */
     public boolean isVisible() {
         return visible;
     }
 
     /**
      * Set wether this component should be enabled.
      *
      * @param enabled true if the component is enabled, false otherwise
      */
     public void setEnabled(boolean enabled) {
         if (firePropertyChangeEvents) {
             if (this.enabled != enabled) {
                 this.enabled = enabled;
                 firePropertyChange(ENABLED_PROPERTY,
                                    Boolean.valueOf(!enabled),
                                    Boolean.valueOf(enabled));
             }
         } else {
             this.enabled = enabled;
         }
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
      * Return the name of this component.
      *
      * @return the name of this component
      */
     public String getName() {
         return name;
     }
 
     /**
      * Set the name of this component.
      *
      * @param name the new name for this component
      */
     public void setName(String name) {
         if (firePropertyChangeEvents) {
             if (isDifferent(this.name, name)) {
                 this.name = name;
 
                 firePropertyChange(NAME_PROPERTY,
                                    this.name,
                                    name);
             }
         } else {
             this.name = name;
         }
     }
 
 
     /**
      * Mark the component as subject to reload.
      * The component will be registered with the ReloadManager.
      *
      * @param aspect the aspect to reload; this is one of the constants
      *               defined in ReloadManager:
      *               <code>ReloadManager.RELOAD_*</code>
      */
     public final void reload(int aspect) {
         getSession().getReloadManager().reload(this, aspect);
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload(int)}
      *
      * @param aspect the aspect to reload; this is one of the constants
      *               defined in ReloadManager:
      *               <code>ReloadManager.RELOAD_*</code>
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(int aspect,
                                         Object oldVal, Object newVal) {
         if (!((oldVal == newVal) || (oldVal != null && oldVal.equals(newVal)))) {
             //System.err.println(getClass().getName() + ": reload. old:" + oldVal + "; new: "+ newVal);
             reload(aspect);
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload(int)}
      *
      * @param aspect the aspect to reload; this is one of the constants
      *               defined in ReloadManager:
      *               <code>ReloadManager.RELOAD_*</code>
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(int aspect,
                                         int oldVal, int newVal) {
         if (oldVal != newVal) {
             reload(aspect);
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload(int)}
      *
      * @param aspect the aspect to reload; this is one of the constants
      *               defined in ReloadManager:
      *               <code>ReloadManager.RELOAD_*</code>
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(int aspect,
                                         boolean oldVal, boolean newVal) {
         if (oldVal != newVal) {
             reload(aspect);
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload(int)}
      *
      * @param aspect the aspect to reload; this is one of the constants
      *               defined in ReloadManager:
      *               <code>ReloadManager.RELOAD_*</code>
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(int aspect,
                                         byte oldVal, byte newVal) {
         if (oldVal != newVal) {
             reload(aspect);
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload(int)}
      *
      * @param aspect the aspect to reload; this is one of the constants
      *               defined in ReloadManager:
      *               <code>ReloadManager.RELOAD_*</code>
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(int aspect,
                                         short oldVal, short newVal) {
         if (oldVal != newVal) {
             reload(aspect);
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload(int)}
      *
      * @param aspect the aspect to reload; this is one of the constants
      *               defined in ReloadManager:
      *               <code>ReloadManager.RELOAD_*</code>
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(int aspect,
                                         long oldVal, long newVal) {
         if (oldVal != newVal) {
             reload(aspect);
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload(int)}
      *
      * @param aspect the aspect to reload; this is one of the constants
      *               defined in ReloadManager:
      *               <code>ReloadManager.RELOAD_*</code>
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(int aspect,
                                         float oldVal, float newVal) {
         if (oldVal != newVal) {
             reload(aspect);
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload(int)}
      *
      * @param aspect the aspect to reload; this is one of the constants
      *               defined in ReloadManager:
      *               <code>ReloadManager.RELOAD_*</code>
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(int aspect,
                                         double oldVal, double newVal) {
         if (oldVal != newVal) {
             reload(aspect);
         }
     }
 
     /**
      * Mark this component as subject to reload for the given
      * aspect if the property, that is given in its old and new
      * fashion, changed. Convenience method for {@link #reload(int)}
      *
      * @param aspect the aspect to reload; this is one of the constants
      *               defined in ReloadManager:
      *               <code>ReloadManager.RELOAD_*</code>
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(int aspect,
                                         char oldVal, char newVal) {
         if (oldVal != newVal) {
             reload(aspect);
         }
     }
 
     /**
      * Let the code generator deletate write the component's code
      * to the device. The code generator is the actual 'plaf'.
      *
      * @param s the Device to write into
      * @throws IOException Thrown if the connection to the client gets broken,
      *         for example when the user stops loading
      */
     public void write(Device s) throws IOException {
         try {
             if (visible) {
                 cg.write(s, this);
             }
         }
         catch (Throwable t) {
             logger.log(Level.SEVERE, "exception during code generation for " +
                                      getClass().getName(), t);
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
                     // System.out.println("invoking " + this.getClass().getName()+"."+getter.getName());
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
                 ((LowLevelEventListener)this).checkEpoch()) {
                 return ( getParentFrame().getEventEpoch()
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
             useNamedEvents = ("true".equalsIgnoreCase((String)getSession().getProperty("wings.event.usenames")))
                 ? Boolean.TRUE : Boolean.FALSE;
         }
         return useNamedEvents.booleanValue();
     }
 
     /**
      * Default implementation of the method in
      * {@link LowLevelEventListener}.
      */
     public String getLowLevelEventId() {
         return getComponentId();
     }
 
     /**
      * @deprecated use getEncodedLowLevelEventId()
      */
     public String getNamePrefix() {
         if (this instanceof LowLevelEventListener) {
             return encodeLowLevelEventId(this.getLowLevelEventId());
         }
         return getComponentId();
     }
 
 
     /**
      * Return the parent frame.
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
         return (getParentForm() != null);
     }
 
     public final SForm getParentForm() {
         SComponent parent = getParent();
 
         while (parent != null && !(parent instanceof SForm)) {
             parent = parent.getParent();
         }
 
         return (SForm) parent;
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
      * @return the horizontal alignment
      * @see SConstants
      */
     public int getHorizontalAlignment() {
         return horizontalAlignment;
     }
 
     /**
      * Set the horizontal alignment.
      * @param alignment new value for the horizontal alignment
      * @see SConstants
      */
     public void setHorizontalAlignment(int alignment) {
         horizontalAlignment = alignment;
     }
 
     /**
      * Set the vertical alignment.
      * @param alignment new value for the vertical alignment
      * @see SConstants
      */
     public void setVerticalAlignment(int alignment) {
         verticalAlignment = alignment;
     }
 
     /**
      * Return the value of the vertical alignment property.
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
      * <p>
      * The <code>get/putClientProperty<code> methods provide access to
      * a small per-instance hashtable. Callers can use get/putClientProperty
      * to annotate components that were created by another module, e.g. a
      * layout manager might store per child constraints this way.  For example:
      * <pre>
      * componentA.putClientProperty("to the left of", componentB);
      * </pre>
      * <p>
      * If value is null this method will remove the property.
      * Changes to client properties are reported with PropertyChange
      * events.  The name of the property (for the sake of PropertyChange
      * events) is <code>key.toString()</code>.
      * <p>
      * The clientProperty dictionary is not intended to support large
      * scale extensions to SComponent nor should be it considered an
      * alternative to subclassing when designing a new component.
      *
      * @see #getClientProperty
      * @see #addPropertyChangeListener
      */
     public final void putClientProperty(Object key, Object value) {
         Object oldValue = getClientProperties().get(key);
 
         if (value != null) {
             getClientProperties().put(key, value);
         } else {
             getClientProperties().remove(key);
         }
 
         firePropertyChange(key.toString(), oldValue, value);
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
      * @beaninfo
      *        bound: true
      *  description: The component's look and feel delegate
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
         firePropertyChange("CG", oldCG, newCG);
         reloadIfChange(ReloadManager.RELOAD_ALL, cg, oldCG);
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
             logger.warning("no session yet.");
         } else if (getSession().getCGManager() == null) {
             logger.warning("no CGManager");
         } else {
             setCG(getSession().getCGManager().getCG(this));
         }
         if (border != null)
             border.updateCG();
     }
 
     /**
      * Returns the name of the CGFactory class that generates the
      * look and feel for this component.
      *
      * @return content of private static final cgClassID attribute
      * @see SComponent#getCGClassID
      * @see org.wings.plaf.CGDefaults
      */
     public String getCGClassID() {
         return cgClassID;
     }
 
     /**
      * Invite a ComponentVisitor.
      * Invokes visit(SComponent) on the ComponentVisitor.
      * @param visitor the visitor to be invited
      */
     public void invite(ComponentVisitor visitor)
         throws Exception {
         visitor.visit(this);
     }
 
 
     /**
      * Returns true if this component is completely opaque.
      * <p>
      * An opaque component paints every pixel within its
      * rectangular bounds. A non-opaque component paints only a subset of
      * its pixels or none at all, allowing the pixels underneath it to
      * "show through".  Therefore, a component that does not fully paint
      * its pixels provides a degree of transparency.
      * <p>
      * Subclasses that guarantee to always completely paint their contents
      * should override this method and return true.
      *
      * @return true if this component is completely opaque
      * @see #setOpaque
      */
     public boolean isOpaque() {
         return opaque;
     }
 
     /**
      * If true the component paints every pixel within its bounds.
      * Otherwise, the component may not paint some or all of its
      * pixels, allowing the underlying pixels to show through.
      * <p>
      * The default value of this property is false for <code>JComponent</code>.
      * However, the default value for this property on most standard
      * <code>JComponent</code> subclasses (such as <code>JButton</code> and
      * <code>JTree</code>) is look-and-feel dependent.
      *
      * @param opaque true if this component should be opaque
      * @see #isOpaque
      * @beaninfo
      *        bound: true
      *       expert: true
      *  description: The component's opacity
      */
     public void setOpaque(boolean opaque) {
         if (firePropertyChangeEvents) {
             if (this.opaque != opaque) {
                 this.opaque = opaque;
                 firePropertyChange(OPAQUE_PROPERTY,
                                    Boolean.valueOf(!opaque),
                                    Boolean.valueOf(opaque));
             }
         } else {
             this.opaque = opaque;
         }
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
             return true;
 
         return !oldObject.equals(newObject);
     }
 
     protected final void addEventListener(Class type, EventListener listener) {
         if (listeners == null) {
             listeners = new EventListenerList();
         }
 
         listeners.add(type, listener);
     }
 
     protected final void removeEventListener(Class type, EventListener listener) {
         if (listeners != null) {
             listeners.remove(type, listener);
         }
     }
 
     /**
      * Returns the number of listeners of the specified type for this component.
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
      * @return All listeners of this component. The result array has a pair structure,
      * the first element of each pair is the listener type, the second the listener
      * itself. It is guaranteed that this returns a non-null array.
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
      * @param type All listeners of this type are added to the result array
      * @return an array of the specified type with all listeners of the specified type
      * @see EventListenerList
      */
     protected final EventListener[] getListeners(Class type) {
         if (listeners != null) {
             return listeners.getListeners(type);
         } else {
             return (EventListener[])Array.newInstance(type, 0);
         }
     }
 
     /**
      * Adds a new {@link PropertyChangeListener} to this component.
      * This forces the
      * {@link #setFirePropertyChangeEvents firePropertyChangeEvents}
      * flag to be set to true.
      * @param listener
      */
     public final void addPropertyChangeListener(PropertyChangeListener listener) {
         if (listener == null)
             throw new IllegalArgumentException("null parameter not allowed");
 
         addEventListener(PropertyChangeListener.class, listener);
 
         // somebody is interested, so fire the events.
         setFirePropertyChangeEvents(true);
     }
 
     /**
      * Removes a {@link PropertyChangeListener} from this component. If it was the last
      * {@link PropertyChangeListener} it forces the
      * {@link #setFirePropertyChangeEvents firePropertyChangeEvents}
      * flags to be set to false
      * @param listener
      */
     public final void removePropertyChangeListener(PropertyChangeListener listener) {
         if (listener == null)
             throw new IllegalArgumentException("null parameter not allowed");
 
         removeEventListener(PropertyChangeListener.class, listener);
 
         if (getListenerCount(PropertyChangeListener.class) == 0) {
             setFirePropertyChangeEvents(false);
         }
     }
 
     /**
      * This is for performance optimizations. With the firePropertyChangeEvents flag set,
      * property change
      * events are generated and so every property setter method has to test if a property
      * has changed and temporarily store the old value to generate the property
      * change event. If it is not set, a setter just have to set the property.
      */
     public final void setFirePropertyChangeEvents(boolean b) {
         firePropertyChangeEvents = b;
     }
 
     /**
      * Indicates, if PropertyChangeEvents are fired.
      * @see #setFirePropertyChangeEvents
      * @return true, if PropertyChangeEvents are fired, false otherwise
      */
     public final boolean getFirePropertyChangeEvents() {
         return firePropertyChangeEvents;
     }
 
     /**
      * Reports a bound property change.
      *
      * @param propertyName the programmatic name of the property
      *		that was changed
      * @param oldValue the old value of the property (as a byte)
      * @param newValue the new value of the property (as a byte)
      * @see #firePropertyChange(java.lang.String, java.lang.Object,
         *		java.lang.Object)
      */
     public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
         if (firePropertyChangeEvents && (oldValue != newValue)) {
             firePropertyChange(propertyName, new Byte(oldValue), new Byte(newValue));
         }
     }
 
     /**
      * Reports a bound property change.
      *
      * @param propertyName the programmatic name of the property
      *		that was changed
      * @param oldValue the old value of the property (as a char)
      * @param newValue the new value of the property (as a char)
      * @see #firePropertyChange(java.lang.String, java.lang.Object,
         *		java.lang.Object)
      */
     public void firePropertyChange(String propertyName, char oldValue, char newValue) {
         if (firePropertyChangeEvents && (oldValue != newValue)) {
             firePropertyChange(propertyName, new Character(oldValue), new Character(newValue));
         }
     }
 
     /**
      * Reports a bound property change.
      *
      * @param propertyName the programmatic name of the property
      *		that was changed
      * @param oldValue the old value of the property (as a short)
      * @param newValue the old value of the property (as a short)
      * @see #firePropertyChange(java.lang.String, java.lang.Object,
         *		java.lang.Object)
      */
     public void firePropertyChange(String propertyName, short oldValue, short newValue) {
         if (firePropertyChangeEvents && (oldValue != newValue)) {
             firePropertyChange(propertyName, new Short(oldValue), new Short(newValue));
         }
     }
 
     /**
      * Reports a bound property change.
      *
      * @param propertyName the programmatic name of the property
      *		that was changed
      * @param oldValue the old value of the property (as an int)
      * @param newValue the new value of the property (as an int)
      * @see #firePropertyChange(java.lang.String, java.lang.Object,
         *		java.lang.Object)
      */
     public void firePropertyChange(String propertyName, int oldValue, int newValue) {
         if (firePropertyChangeEvents && (oldValue != newValue)) {
             firePropertyChange(propertyName, new Integer(oldValue), new Integer(newValue));
         }
     }
 
     /**
      * Reports a bound property change.
      *
      * @param propertyName the programmatic name of the property
      *		that was changed
      * @param oldValue the old value of the property (as a long)
      * @param newValue the new value of the property (as a long)
      * @see #firePropertyChange(java.lang.String, java.lang.Object,
         *		java.lang.Object)
      */
     public void firePropertyChange(String propertyName, long oldValue, long newValue) {
         if (firePropertyChangeEvents && (oldValue != newValue)) {
             firePropertyChange(propertyName, new Long(oldValue), new Long(newValue));
         }
     }
 
     /**
      * Reports a bound property change.
      *
      * @param propertyName the programmatic name of the property
      *		that was changed
      * @param oldValue the old value of the property (as a float)
      * @param newValue the new value of the property (as a float)
      * @see #firePropertyChange(java.lang.String, java.lang.Object,
         *		java.lang.Object)
      */
     public void firePropertyChange(String propertyName, float oldValue, float newValue) {
         if (firePropertyChangeEvents && (oldValue != newValue)) {
             firePropertyChange(propertyName, new Float(oldValue), new Float(newValue));
         }
     }
 
     /**
      * Reports a bound property change.
      *
      * @param propertyName the programmatic name of the property
      *		that was changed
      * @param oldValue the old value of the property (as a double)
      * @param newValue the new value of the property (as a double)
      * @see #firePropertyChange(java.lang.String, java.lang.Object,
         *		java.lang.Object)
      */
     public void firePropertyChange(String propertyName, double oldValue, double newValue) {
         if (firePropertyChangeEvents && (oldValue != newValue)) {
             firePropertyChange(propertyName, new Double(oldValue), new Double(newValue));
         }
     }
 
     /**
      * Reports a bound property change.
      *
      * @param propertyName the programmatic name of the property
      *		that was changed
      * @param oldValue the old value of the property (as a boolean)
     * @param oldValue the old value of the property (as a boolean)
      * @see #firePropertyChange(java.lang.String, java.lang.Object,
         *		java.lang.Object)
      */
     public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
         if (firePropertyChangeEvents && (oldValue != newValue)) {
             firePropertyChange(propertyName, oldValue ? Boolean.TRUE : Boolean.FALSE,
                                newValue ? Boolean.TRUE : Boolean.FALSE);
         }
     }
 
     /**
      * Fires a PropertyChangedEvent to all listeners if the
      * {@link #getFirePropertyChangeEvents firePropertyChangeEvents} is set.
      * @param property The property which has changed
      * @param oldValue The old value of the property before the change
      * @param newValue The new value of the property
      */
     protected final void firePropertyChange(String property,
                                             Object oldValue, Object newValue) {
         if (firePropertyChangeEvents) {
 
             PropertyChangeEvent event = null;
 
             // maybe the better way to do this is to user the getListenerList
             // and iterate through all listeners, this saves the creation of
             // an array but it must cast to the apropriate listener
             Object[] listeners = getListenerList();
             for (int i = listeners.length - 2; i >= 0; i -= 2) {
                 if (listeners[i] == PropertyChangeListener.class) {
                     // Lazily create the event:
                     if (event == null)
                         event = new PropertyChangeEvent(this, property, oldValue, newValue);
                     ((PropertyChangeListener)listeners[i + 1]).propertyChange(event);
                 }
             }
 
             // this is an array with length > 0 , so it is not the
             // EMPTY_EVENTLISTENER_ARRAY and so of the
             // correct (PropertyChangeListener[]) type
             /* this is the alternative aproach
                PropertyChangeListener[] pcListener =
                (PropertyChangeListener[]) getListeners(PropertyChangeListener.class);
 
                for (int i = 0; i < pcListener.length; i++) {
                pcListener[i].propertyChange(event);
                }
             */
         }
 
     }
 
 
     private transient SRenderEvent renderEvent;
 
     /**
      * for performance reasons
      *
      */
     private boolean fireRenderEvents = false;
 
     public static final int START_RENDERING = 1;
     public static final int DONE_RENDERING = 2;
 
     public final void addRenderListener(SRenderListener l) {
         addEventListener(SRenderListener.class, l);
         fireRenderEvents = true;
     }
 
     public final void removeRenderListener(SRenderListener l) {
         removeEventListener(SRenderListener.class, l);
     }
 
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
                             ((SRenderListener)listeners[i + 1]).startRendering(renderEvent);
                             break;
                         case DONE_RENDERING:
                             ((SRenderListener)listeners[i + 1]).doneRendering(renderEvent);
                             break;
                     } // end of switch ()
                 }
             }
         } // end of if ()
     }
 
     /**
      * Forwards the scrollRectToVisible() message to the SComponent's
      * parent. Components that can service the request, such as
      * SScrollPane, override this method and perform the scrolling.
      * @param aRect the visible Rectangle
      * @see SScrollPane
      */
     public void scrollRectToVisible(Rectangle aRect) {
         if (parent != null) {
             parent.scrollRectToVisible(aRect);
         }
     }
     
     /**
      * requests the focus for this component
      */
     public void requestFocus(){  
         if (getParentFrame() != null) {
             getParentFrame().focusRequest(this); 
         }
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
