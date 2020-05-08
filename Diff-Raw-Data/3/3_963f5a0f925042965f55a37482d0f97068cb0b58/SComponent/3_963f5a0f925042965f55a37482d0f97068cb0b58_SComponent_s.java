 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
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
 import org.wings.event.*;
 import org.wings.io.Device;
 import org.wings.plaf.ComponentCG;
 import org.wings.plaf.Update;
 import org.wings.script.JavaScriptListener;
 import org.wings.script.ScriptListener;
 import org.wings.session.Session;
 import org.wings.session.SessionManager;
 import org.wings.style.*;
 import org.wings.util.ComponentVisitor;
 
 import javax.swing.*;
 import javax.swing.event.EventListenerList;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.beans.*;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.lang.reflect.Array;
 import java.lang.reflect.Method;
 import java.util.*;
 import java.util.List;
 
 /**
  * Object having a graphical representation that can be displayed on the
  * screen and that can interact with the user.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
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
     private transient ComponentCG cg;
 
     /**
      * Vertical alignment
      */
     private int verticalAlignment = SConstants.NO_ALIGN;
 
     /**
      * Horizontal alignment
      */
     private int horizontalAlignment = SConstants.NO_ALIGN;
 
     /**
      * The name of the style class
      */
     private String style;
 
     /**
      * Map of {@link Selector} to CSS {@link Style}s currently assigned to this component.
      */
     protected Map<Selector, Style> dynamicStyles;
 
     /**
      * Visibility of the component.
      */
     protected boolean visible = true;
 
     /**
      * Enabled / disabled.
      */
     protected boolean enabled = true;
 
     /**
      * The container, this component resides in.
      */
     private SContainer parent;
 
     /**
      * The frame in which this component resides.
      */
     private SFrame parentFrame;
 
     /**
      * The border for the component.
      */
     private SBorder border;
 
     /**
      * The tooltip for this component.
      */
     private String tooltip;
 
     /**
      * The focus traversal Index
      */
     private int focusTraversalIndex = -1;
 
     /**
      * Preferred size of component in pixel.
      */
     private SDimension preferredSize;
 
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
 
     private boolean showAsFormComponent = true;
 
     private boolean reloadForced = false;
 
     private SPopupMenu popupMenu;
 
     /*private boolean inheritsPopupMenu;*/
 
     private InputMap[] inputMaps;
 
     /**
      * Contains all script listeners of the component.
      */
     private List<ScriptListener> scriptListenerList;
 
     private ActionMap actionMap;
 
     private Map<Action, ActionEvent> actionEvents;
 
     private transient SRenderEvent renderEvent;
 
     private SParentFrameListener globalInputMapListener;
 
     private Map<Object, Object> clientProperties;
 
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
      *
      * @see #setInputMap(int, javax.swing.InputMap)
      */
     public static final int WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT = 0;
 
     /**
      * Constants for conditions on which actions are triggered. Mainly two
      * cases: the focus has either to be at the component (or at a child)
      * or somewhere in the parent frame.
      *
      * @see #setInputMap(int, javax.swing.InputMap)
      */
     public static final int WHEN_IN_FOCUSED_FRAME = 1;
 
     /**
      * Global CSS selector 
      */
     public static final Selector SELECTOR_ALL = new Selector("everything");
 
     /**
      * Performance improvement constant
      */
     private static final ScriptListener[] EMPTY_SCRIPTLISTENERLIST = new ScriptListener[0];
 
     /**
      *  PropertyChangeSupport
      */
     protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
 
     /**
      * Default empty constructor.
      * The method updateCG is called during construction time to get a cg delegate installed (renderer).
      */
     public SComponent() {
         updateCG();
     }
 
     /**
      * Returns the border of this component or null if no border has been set.
      *
      * @return the border object
      * @see #setBorder(SBorder)
      */
     public SBorder getBorder() {
         return border;
     }
 
     /**
      * Sets the border for this component.
      *
      * @param border the border to be set for the component
      */
     public void setBorder(SBorder border) {
         SBorder oldVal = this.getBorder();
         reloadIfChange(this.border, border);
         if (this.border != null)
             this.border.setComponent(null);
         this.border = border;
         if (this.border != null)
             this.border.setComponent(this);
         propertyChangeSupport.firePropertyChange("border", oldVal, this.border);
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
         SContainer oldVal = this.parent;
         this.parent = parent;
         if (parent != null)
             setParentFrame(parent.getParentFrame());
         else
             setParentFrame(null);
         propertyChangeSupport.firePropertyChange("parent", oldVal, this.parent);
     }
 
     /**
      * Sets the parent frame.
      *
      * @param parentFrame the frame
      */
     protected void setParentFrame(SFrame parentFrame) {
         SFrame oldVal = this.parentFrame;
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
 
         //reload();
         if (getScriptListeners().length > 0)
             reload();
         if (dynamicStyles != null && dynamicStyles.size() > 0)
             reload();
 
         propertyChangeSupport.firePropertyChange("parentFrame", oldVal, this.parentFrame);
     }
 
     /*public void setInheritsPopupMenu(boolean inheritsPopupMenu) {
         reloadIfChange(this.inheritsPopupMenu, inheritsPopupMenu);
         this.inheritsPopupMenu = inheritsPopupMenu;
     }
 
     public boolean getInheritsPopupMenu() {
         return inheritsPopupMenu;
     }*/
 
     public void setComponentPopupMenu(SPopupMenu popupMenu) {
         reloadIfChange(this.popupMenu, popupMenu);
         if (this.popupMenu != null) {
             getSession().getMenuManager().deregisterMenuLink(this.popupMenu, this);
             this.popupMenu.setParentFrame(null);
         }
         SPopupMenu oldVal = this.popupMenu;
         this.popupMenu = popupMenu;
         if (this.popupMenu != null) {
             getSession().getMenuManager().registerMenuLink(this.popupMenu, this);
             this.popupMenu.setParentFrame(getParentFrame());
         }
         propertyChangeSupport.firePropertyChange("componentPopupMenu", oldVal, this.popupMenu);
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
 
     /*  No reason -- even JComponent does not define such a method.
         Undo change also in DynamicScriptResource.visit().
     public boolean hasComponentPopupMenu() {
         return popupMenu != null;
     } */
 
     /**
      * The URL under which this component is accessible for the browser.
      * This is equivalent to the URL of the component's root frame, as this is the
      * node externalized to the browser via the {@link org.wings.resource.ReloadResource}
      * externalizer.
      *
      * @return The HTTP URL where this component can be accessed.
      */
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
         SDimension oldVal = this.preferredSize;
         reloadIfChange(this.preferredSize, preferredSize);
         this.preferredSize = preferredSize;
         propertyChangeSupport.firePropertyChange("preferredSize", oldVal, this.preferredSize);
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
      * @see SComponent#removeNotify()
      * @see SComponent#addNotify()
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
      * @see SComponent#removeNotify()
      * @see SComponent#addNotify()
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
         if (scriptListenerList != null && scriptListenerList.contains(listener))
             return;
 
         if (scriptListenerList == null) {
             scriptListenerList  = new LinkedList<ScriptListener>();
         }
 
         int placingPosition = -1;
         for (int i = 0; i < scriptListenerList.size() && placingPosition < 0; i++) {
             ScriptListener existingListener = scriptListenerList.get(i);
             if (existingListener.getPriority() < listener.getPriority())
                 placingPosition = i;
         }
         reload();
         if (placingPosition >= 0)
             scriptListenerList.add(placingPosition, listener);
         else
             scriptListenerList.add(listener);
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
         if (scriptListenerList != null) {
             scriptListenerList.remove(listener);
             reload();
         }
     }
 
     /**
      * returns the script listeners of this component
      *
      * @return the ScriptListener Array.
      */
     public ScriptListener[] getScriptListeners() {
         if (scriptListenerList != null) {
             return scriptListenerList.toArray(new ScriptListener[scriptListenerList.size()]);
         } else {
             return EMPTY_SCRIPTLISTENERLIST;
         }
     }
 
     /**
      * Returns the script listeners of this component.
      *
      * @return The <code>ScriptListener</code>s in a <code>List</code>.
      */
     public List<ScriptListener> getScriptListenerList() {
         if (scriptListenerList != null) {
             return Collections.unmodifiableList(scriptListenerList);
         } else {
             return Collections.emptyList();
         }
     }
 
     /**
      * Sets the name property of a component which must be <b>unique</b>!
      * <br/>Assigning the same name multiple times will cause strange results!
      * <p/>
      * <p>Valid names must begin with a letter ([A-Za-z]), underscores ("_") or dollars ("$") and may be followed by any number of
      * letters, digits ([0-9]), underscores ("_") and dollars ("$")
      * <p/>
      * <p>If no name is set, it is generated when necessary.
      * <p/>
      * <p><i>Explanation:</i> This property is an identifier which is used inside the generated HTML as an element identifier (id="")
      * and sometimes as a javascript function name.
      *
      * @param uniqueName A <b>unique</b> name to set. <b>Only valid identifier as described are allowed!</b>
      * @see Character
      */
     public void setName(String uniqueName) {
         if (uniqueName != null) {
             char ch = uniqueName.charAt(0);
             if (uniqueName.length() == 0 || !(Character.isLetter(ch) || ch == '_' || ch == '$'))
                 throw new IllegalArgumentException(uniqueName + " is not a valid identifier");
             for (int i = 1; i < uniqueName.length(); i++) {
                 ch = uniqueName.charAt(i);
                 if (!(Character.isLetter(ch) || Character.isDigit(ch) || ch == '_' || ch == '$'))
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
         String oldVal = this.name;
         reloadIfChange(this.name, uncheckedName);
         this.name = uncheckedName;
         propertyChangeSupport.firePropertyChange("name", oldVal, this.name);
     }
 
     /**
      * Gets the name property of a component. This property is an identifier,so it should be always unique.
      * For details refer to {@link #setName(String)}
      *
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
 
     /*
      * If a subclass implements the {@link LowLevelEventListener} interface,
      * it will be unregistered at the associated dispatcher.
      */
     final void unregister() {
         if (getSession().getDispatcher() != null && this instanceof LowLevelEventListener) {
             getSession().getDispatcher().unregister((LowLevelEventListener) this);
         }
     }
 
     /*
      * If a subclass implements the {@link LowLevelEventListener} interface,
      * it will be registered at the associated dispatcher.
      */
     final void register() {
         if (getSession().getDispatcher() != null && this instanceof LowLevelEventListener) {
             getSession().getDispatcher().register((LowLevelEventListener) this);
         }
     }
 
     /**
      * Set an CSS class name provided by the laf-provided Cascading Style Sheet (CSS), which should
      * be applied to this component.
      * <p>
      * <b>Note:</b> Probably the {@link #addStyle(String)} method is more what you want.
      * <p>By <b>default</b> this is set to the wingS component class name (i.e. "SLabel").
      * <p>The PLAFs render the value of this String to an <code>class="<i>cssClassName</i>"</code> attribute
      * inside the generated HTML code of this component.
      * <p>The default wingS plaf initializes this by default to the wingS component class name
      * (i.e. <code>SButton</code> for button instances). <br/>Please be aware if you <b>replace</b> this
      * default value, the default wingS style will no longer take effect, as they operate on these
      * default styles. To avoid this you should append your CSS styles via spaces i.e.<br>
      * <code>c.setStyle(c.getStyle + "myStyle");</code>
      *
      * @param cssClassName The new CSS name value for this component
      * @see #addStyle(String)
      * @see #removeStyle(String)
      */
     // <p>Please consider using {@link #addStyle(String)} to avoid disabling of default wingS stylesheet set.
     public void setStyle(String cssClassName) {
         String oldVal = this.style;
         reloadIfChange(style, cssClassName);
         this.style = cssClassName;
         propertyChangeSupport.firePropertyChange("style", oldVal, this.style);
     }
 
     /**
      * Append a style class name to the style string. Use this method if you want to append a specific CSS
      * class to a componentn without loosing the other CSS styles assigned to the component (i.e. the wingS
      * default styles.)
      * @param additionalCssClassName The style class to remove (if existing).
      * @see #removeStyle(String)
      */
     public void addStyle(String additionalCssClassName) {
         if (this.style == null || this.style.length() == 0) {
             setStyle(additionalCssClassName);  // trivial case
         } else {
             if (this.style.indexOf(additionalCssClassName) < 0) {
                 setStyle(this.style+" "+additionalCssClassName);
             }
         }
     }
 
     /**
      * Remove a style class definiton from this component.
      * @param cssStyleClassName The style class to remove (if existing).
      */
     public void removeStyle(String cssStyleClassName) {
         if (this.style != null && cssStyleClassName != null && this.style.indexOf(cssStyleClassName) >= 0) {
             if (this.style.length() == cssStyleClassName.length())
                 setStyle(null); // trivial case
             else
                 setStyle(this.style.replaceAll("\\b"+cssStyleClassName+"\\b","").replaceAll("  "," ").trim());
         }
     }
 
     /**
      * @return The current CSS style class name. Defaults to the unqualified wingS component class name.
      * @see #setStyle(String)
      */
     public String getStyle() {
         return style;
     }
 
 
     /**
      * Register a new CSS style on this component for a specfic CSS selector.
      * <p>Typically you will want to use the method {@link #setAttribute(org.wings.style.CSSProperty, String)}
      * to specify a single CSS property/value pair on this component.
      *
      * @param style A Style instance.
      */
     public void addDynamicStyle(Style style) {
         if (dynamicStyles == null)
             dynamicStyles = new HashMap<Selector, Style>(4);
         dynamicStyles.put(style.getSelector(), style);
         reload();
     }
 
     /**
      * Remove all CSS style definitions defined for the passed CSS selector.
      *
      * @param selector The selector. The default selector for most CSS attributes is {@link #SELECTOR_ALL}.
      */
     public void removeDynamicStyle(Selector selector) {
         if (dynamicStyles == null)
             return;
         dynamicStyles.remove(selector);
         reload();
     }
 
     /**
      * Returns the style defined for the passed CSS selector.
      *
      * @param selector The CSS selector the style to retrieve. See {@link org.wings.style.Style#getSelector()}.
      *         The default selector for most CSS styles is {@link #SELECTOR_ALL}.
      * @return A style (collection of css property/value pairs) or <code>null</code>
      */
     public Style getDynamicStyle(Selector selector) {
         if (dynamicStyles == null)
             return null;
         return dynamicStyles.get(selector);
     }
 
     /**
      * Adds the passed collection of {@link Style} definitions. Existing Styles for the same CSS selectors
      * (see {@link org.wings.style.Style#getSelector()}) are overwritten.
      *
      * @param dynamicStyles A collection collection of {@link Style} definitions.
      */
     public void setDynamicStyles(Collection dynamicStyles) {
         Map oldVal = this.dynamicStyles;
         if (dynamicStyles == null)
             return;
         if (this.dynamicStyles == null)
             this.dynamicStyles = new HashMap<Selector, Style>(4);
         for (Object dynamicStyle : dynamicStyles) {
             Style style = (Style) dynamicStyle;
             this.dynamicStyles.put(style.getSelector(), style);
         }
         reload();
         propertyChangeSupport.firePropertyChange("dynamicStyles", oldVal, this.dynamicStyles);
     }
 
     /**
      * Returns the collection of currently defined CSS styles on this component.
      *
      * @return A unmodifyable collection of {@link Style} instances.
      */
     public Collection getDynamicStyles() {
         if (dynamicStyles == null || dynamicStyles.size() == 0)
             return null;
         return Collections.unmodifiableCollection(dynamicStyles.values());
     }
 
     /**
      * Defines a free text css property / value pair to this component.
      * The CSS property will appear as an inline style in the generated HTML code.
      */
     public void setAttribute(String cssPropertyName, String value) {
         final CSSProperty property = CSSProperty.valueOf(cssPropertyName);
         if (CSSProperty.BORDER_PROPERTIES.contains(property))
             throw new IllegalArgumentException("Border properties have to be applied to the border!");
         setAttribute(SELECTOR_ALL, property, value);
     }
 
     /**
      * Assign or overwrite a CSS property/value pair on this component. This CSS property definition will
      * use a CSS selector which adresses this component as whole as CSS selector (<code>new CSSProperty(this)</code>).
      * The CSS property will appear as an inline style in the generated HTML code.
      *
      * @param property      The CSS property (i.e. {@link CSSProperty#BACKGROUND}).
      * @param propertyValue A valid string value for this CSS property (i.e. <code>red</code> or <code>#fff</code> in our example).
      */
     public void setAttribute(CSSProperty property, String propertyValue) {
         if (CSSProperty.BORDER_PROPERTIES.contains(property))
             throw new IllegalArgumentException("Border properties have to be applied to the border!");
         setAttribute(SELECTOR_ALL, property, propertyValue);
     }
 
     /**
      * Assign or overwrite a CSS property/value pair at this component. This CSS property definition will
      * use the CSS selector you passed, so in the most exotic case it could affect a totally different
      * component or component area. Typically you use this method to assign CSS property values to
      * pseudo CSS selectors {@link Selector}. This are selector affecting only a part of a component
      * and not the component at all..
      * The CSS property will appear as an inline style in the generated HTML code.
      *
      * @param selector A valid CSS selector. Typically values are i.e. the {@link #SELECTOR_ALL}
      * or other <code>SELECTOR_xxx</code> value instances declared in the component.
      * (look ie. at {@link STabbedPane#SELECTOR_CONTENT}) or manually constructed instances of
      * <code>Selector</code>. In most case {@link #setAttribute(org.wings.style.CSSProperty, String)} will be your
      * choice.
      * @param property The css property you want to define a value for
      * @param propertyValue A valid string value for this property.
      */
     public void setAttribute(Selector selector, CSSProperty property, String propertyValue) {
         Style style = getDynamicStyle(selector);
         if (style == null) {
             addDynamicStyle(new CSSStyle(selector, property, propertyValue));
         } else {
             Map<CSSProperty, String> oldStyle = style.properties();
             String oldVal = oldStyle.get(property);
             String old = style.put(property, propertyValue);
             reloadIfChange(old, propertyValue);
             propertyChangeSupport.firePropertyChange("attribute", oldVal, propertyValue);
         }
     }
 
     /**
      * Convenience variant of {@link #setAttribute(org.wings.style.Selector, org.wings.style.CSSProperty, String)}.
      * Converts the passed icon into a URL and applies the according CSS style.
      *
      * @param selector A valid CSS selector. Typically values are i.e. the {@link #SELECTOR_ALL}
      * or other <code>SELECTOR_xxx</code> value instances declared in the component.
      * (look ie. at {@link STabbedPane#SELECTOR_CONTENT}) or manually constructed instances of
      * <code>Selector</code>. In most case {@link #setAttribute(org.wings.style.CSSProperty, String)} will be your
      * choice.
      * @param property The css property you want to define a value for (in this case
      *                 mostly something like {@link CSSProperty#BACKGROUND_IMAGE}.
      * @param icon     The icon you want to assign.
      */
     public void setAttribute(Selector selector, CSSProperty property, SIcon icon) {
         setAttribute(selector, property, icon != null ? "url('" + icon.getURL().toString() + "')" : "none");
     }
 
     /**
      * Convenience variant of {@link #setAttribute(org.wings.style.Selector, org.wings.style.CSSProperty, String)}.
      * Converts the passed color into according color string.
      *
      * @param selector A valid CSS selector. Typically values are i.e. the {@link #SELECTOR_ALL}
      * or other <code>SELECTOR_xxx</code> value instances declared in the component.
      * (look ie. at {@link STabbedPane#SELECTOR_CONTENT}) or manually constructed instances of
      * <code>Selector</code>. In most case {@link #setAttribute(org.wings.style.CSSProperty, String)} will be your
      * choice.
      * @param property The css property you want to define a value for (in this case
      *                 mostly something like {@link CSSProperty#BACKGROUND_IMAGE}.
      * @param color    The color value you want to assign.
      */
     public void setAttribute(Selector selector, CSSProperty property, Color color) {
         setAttribute(selector, property, CSSStyleSheet.getAttribute(color));
     }
 
     public void setAttributes(Selector selector, CSSAttributeSet attributes) {
         Style style = getDynamicStyle(selector);
        Map<CSSProperty, String> oldStyleProperties = style.properties();
         if (style == null) {
             addDynamicStyle(new CSSStyle(selector, attributes));
         } else {
             boolean changed = style.putAll(attributes);
             if (changed)
                 reload();
             propertyChangeSupport.firePropertyChange("attributes", oldStyleProperties, style.properties());
         }
     }
 
     /**
      * Returns the current background color of this component.
      *
      * @return The current background color or <code>null</code>
      */
     public Color getBackground() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_ALL) == null ? null : CSSStyleSheet.getBackground((CSSAttributeSet) dynamicStyles.get(SELECTOR_ALL));
     }
 
     /**
      * Set the components background color.
      *
      * @param color the new background color or <code>null</code>
      */
      public void setBackground(Color color) {
         Color oldVal = this.getBackground();
         setAttribute(SELECTOR_ALL, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
         propertyChangeSupport.firePropertyChange("background", oldVal, this.getBackground());
      }
 
     /**
      * Return the components foreground color.
      *
      * @return the foreground color or <code>null</code>
      */
     public Color getForeground() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_ALL) == null ? null : CSSStyleSheet.getForeground((CSSAttributeSet) dynamicStyles.get(SELECTOR_ALL));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color or <code>null</code>
      */
     public void setForeground(Color color) {
         Color oldVal = this.getForeground();
         setAttribute(SELECTOR_ALL, CSSProperty.COLOR, CSSStyleSheet.getAttribute(color));
 
         propertyChangeSupport.firePropertyChange("foreground", oldVal, this.getForeground());
     }
 
     /**
      * Set the font.
      *
      * @param font the new font
      */
     //TODO: firePropertyChange() in IF
     public void setFont(SFont font) {
         SFont oldVal = this.getFont();
         CSSAttributeSet attributes = CSSStyleSheet.getAttributes(font);
         Style style = getDynamicStyle(SELECTOR_ALL);
         if (style == null) {
             addDynamicStyle(new CSSStyle(SELECTOR_ALL, attributes));
         }
         else {
             style.remove(CSSProperty.FONT);
             style.remove(CSSProperty.FONT_FAMILY);
             style.remove(CSSProperty.FONT_SIZE);
             style.remove(CSSProperty.FONT_STYLE);
             style.remove(CSSProperty.FONT_WEIGHT);
             style.putAll(attributes);
             reload();
             propertyChangeSupport.firePropertyChange("font", oldVal, this.getFont());
         }
     }
 
     /**
      * Return the font used inside this component.
      *
      * @return The current font declaration or <code>null</code>
      */
     public SFont getFont() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_ALL) == null ? null : CSSStyleSheet.getFont((CSSAttributeSet) dynamicStyles.get(SELECTOR_ALL));
     }
 
     /**
      * Set the visibility.
      *
      * @param visible wether this component will show or not
      */
     public void setVisible(boolean visible) {
         boolean old = this.visible;
         this.visible = visible;
         if (visible != old) {
             if (fireComponentChangeEvents) {
                 fireComponentChangeEvent(new SComponentEvent(this, visible
                         ? SComponentEvent.COMPONENT_SHOWN
                         : SComponentEvent.COMPONENT_HIDDEN));
             }
 
             if (parent != null) {
             	parent.reload();
             } else {
             	reload();
             }
             propertyChangeSupport.firePropertyChange("visible", old, this.visible);
         }
     }
 
     /**
      * Return the <b>local</b> visibility. If set to <code>true</code> this ccmponent
      * should be visible if all parent components are visible, too.
      *
      * @return <code>true</code> If the component and it's children should show, <code>false</code> otherwise
      * @see #isRecursivelyVisible()
      */
     public boolean isVisible() {
         return visible;
     }
 
     /**
      * Return the visibility. If the Component itself or any of it's parent is invisible,
      * this method will return <code>false</code>.
      *
      * @return <code>true</code> if this component and all it's ancestors are visible, <code>false</code> otherwise.
      */
     public boolean isRecursivelyVisible() {
         return visible && (parent == null || (parent.isShowingChildren() && parent.isRecursivelyVisible()));
     }
 
     /**
      * Set wether this component should be enabled.
      *
      * @param enabled true if the component is enabled, false otherwise
      */
     public void setEnabled(boolean enabled) {
         boolean oldVal = this.enabled;
         reloadIfChange(this.enabled, enabled);
         this.enabled = enabled;
         propertyChangeSupport.firePropertyChange("enabled", oldVal, this.enabled);
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
      * Marks this component as subject to reload.
      * The component will be registered with the ReloadManager.
      */
     public void reload() {
         getSession().getReloadManager().reload(this);
     }
 
     /**
      * Hands the given update to the Reload Manager.
      * @param update  the update for this component
      */
     public void update(Update update) {
         getSession().getReloadManager().addUpdate(this, update);
     }
 
     protected boolean isUpdatePossible() {
         return getSession().getReloadManager().isUpdateMode();
     }
 
     public boolean isReloadForced() {
         return reloadForced;
     }
 
     public void setReloadForced(boolean forced) {
         if (reloadForced != forced) {
             boolean oldVal = this.reloadForced;
             Object clientProperty = getClientProperty("onChangeSubmitListener");
             if (clientProperty != null && clientProperty instanceof JavaScriptListener) {
                 removeScriptListener((JavaScriptListener) clientProperty);
                 putClientProperty("onChangeSubmitListener", null);
             }
             reloadForced = forced;
             reload();
             propertyChangeSupport.firePropertyChange("reloadForced", oldVal, this.reloadForced);
         }
     }
 
     /**
      * Mark this component as subject to reload if the property,
      * that is given in its old and new fashion, changed.
      *
      * Fires an PropertyChangeEvent with the property name, the old value and the new value
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(Object oldVal, Object newVal) {
         if (isDifferent(oldVal, newVal)) reload();
     }
 
     /**
      * Mark this component as subject to reload if the property,
      * that is given in its old and new fashion, changed.
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(int oldVal, int newVal) {
         if (oldVal != newVal) reload();
     }
 
     /**
      * Mark this component as subject to reload if the property,
      * that is given in its old and new fashion, changed.
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(boolean oldVal, boolean newVal) {
         if (oldVal != newVal) reload();
     }
 
     /**
      * Mark this component as subject to reload if the property,
      * that is given in its old and new fashion, changed.
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(byte oldVal, byte newVal) {
         if (oldVal != newVal) reload();
     }
 
     /**
      * Mark this component as subject to reload if the property,
      * that is given in its old and new fashion, changed.
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(short oldVal, short newVal) {
         if (oldVal != newVal) reload();
     }
 
     /**
      * Mark this component as subject to reload if the property,
      * that is given in its old and new fashion, changed.
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(long oldVal, long newVal) {
         if (oldVal != newVal) reload();
     }
 
     /**
      * Mark this component as subject to reload if the property,
      * that is given in its old and new fashion, changed.
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(float oldVal, float newVal) {
         if (oldVal != newVal) reload();
     }
 
     /**
      * Mark this component as subject to reload if the property,
      * that is given in its old and new fashion, changed.
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(double oldVal, double newVal) {
         if (oldVal != newVal) reload();
     }
 
     /**
      * Mark this component as subject to reload if the property,
      * that is given in its old and new fashion, changed.
      *
      * @param oldVal the old value of some property
      * @param newVal the new value of some property
      */
     protected final void reloadIfChange(char oldVal, char newVal) {
         if (oldVal != newVal) reload();
     }
 
     /**
      * Mark this component as subject to reload if the property,
      * that is given in its old and new fashion, changed.
      */
     @SuppressWarnings({"unchecked"})
     public void write(Device s) throws IOException {
         try {
             if (visible) {
                 cg.write(s, this);
             }
         } catch (IOException se) {
             // Typical double-clicks. Not severe
             log.debug("Not Severe: Socket exception during code generation for " + getClass().getName() + se);
         } catch (Throwable t) {
             // should we warn here? or maybe throw an error...
             log.error("Exception during code generation for " + getClass().getName(), t);
         }
     }
 
     /**
      * A string representation of this component. Uses the {@link #paramString()} methods
      *
      * @return string representation of this component with all properties.
      */
     @Override
     public String toString() {
         return getClass().getName() + "[" + getName() + "]";
     }
 
 
     /**
      * Generates a string describing this <code>SComponent</code>.
      * This method is mainly for debugging purposes.
      *
      * @return a string containing all properties
      */
     protected String paramString() {
         StringBuilder buffer = new StringBuilder(getClass().getName());
         buffer.append("[");
 
         try {
             BeanInfo info = Introspector.getBeanInfo(getClass());
             PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
 
             boolean first = true;
             for (PropertyDescriptor descriptor : descriptors) {
                 try {
                     Method getter = descriptor.getReadMethod();
                     if (getter == null || getter.getName().startsWith("getParent")) {
                         continue;
                     }
                     // System.out.println("invoking " + this.getClass().getDescription()+"."+getter.getDescription());
                     Object value = getter.invoke(this);
                     if (first) {
                         first = false;
                     } else {
                         buffer.append(",");
                     }
                     buffer.append(descriptor.getName() + "=" + value);
                 } catch (Exception e) {
                     log.debug("Exception during paramString()" +e );
                 }
             }
         } catch (Exception e) {
             log.debug("Exception during paramString()" +e );
         }
 
         buffer.append("]");
         return buffer.toString();
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
      * @return the parent frame
      * @see #addParentFrameListener(org.wings.event.SParentFrameListener)
      */
     public SFrame getParentFrame() {
         return parentFrame;
     }
 
     /**
      * Return true, if this component is contained in a form.
      *
      * @return true, if this component resides in a form, false otherwise
      */
     public boolean getResidesInForm() {
         SComponent parent = getParent();
 
         boolean actuallyDoes = parent instanceof SForm;
         while (parent != null && !actuallyDoes) {
             parent = parent.getParent();
             actuallyDoes = parent instanceof SForm;
         }
 
         return actuallyDoes;
     }
 
     /**
      * Set the tooltip text. To style use HTML tags.
      *
      * @param t the new tooltip text
      */
     public void setToolTipText(String t) {
         String oldVal = this.tooltip;
         reloadIfChange(this.tooltip, t);
         this.tooltip = t;
         propertyChangeSupport.firePropertyChange("toolTipText", oldVal, this.tooltip);
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
         int oldVal = this.focusTraversalIndex;
         reloadIfChange(this.focusTraversalIndex, index);
         focusTraversalIndex = index;
         propertyChangeSupport.firePropertyChange("focusTraversalIndex", oldVal, this.focusTraversalIndex);
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
     @Override
     public Object clone() {
         try {
             return super.clone();
         } catch (Exception e) {
             log.error("Unable to clone component", e);
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
         int oldVal = this.horizontalAlignment;
         reloadIfChange(this.horizontalAlignment, alignment);
         horizontalAlignment = alignment;
         propertyChangeSupport.firePropertyChange("horizontalAlignment", oldVal, this.horizontalAlignment);
     }
 
     /**
      * Set the vertical alignment.
      *
      * @param alignment new value for the vertical alignment
      * @see SConstants
      */
     public void setVerticalAlignment(int alignment) {
         int oldVal = this.verticalAlignment;
         reloadIfChange(this.verticalAlignment, alignment);
         verticalAlignment = alignment;
         propertyChangeSupport.firePropertyChange("verticalAlignment", oldVal, this.verticalAlignment);
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
 
 
     /**
      * @return a small HashMap
      * @see #putClientProperty
      * @see #getClientProperty
      */
     private Map<Object, Object> getClientProperties() {
         if (clientProperties == null) {
             clientProperties = new HashMap<Object, Object>(2);
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
     @SuppressWarnings({"unchecked"})
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
     protected static boolean isDifferent(Object oldObject,
                                          Object newObject) {
         if (oldObject == newObject)
             return false;
 
         if (oldObject == null)
             return true;
 
         return !oldObject.equals(newObject);
     }
 
     /**
      * Adds an event listener for the given event class
      *
      * @param type     The class/type of events to listen to.
      * @param listener The listener itself.
      */
     protected final <T extends EventListener> void addEventListener(Class<T> type, T listener) {
         if (listeners == null) {
             listeners = new EventListenerList();
         }
         listeners.add(type, listener);
     }
 
     /**
      * Removed named event listener.
      *
      * @param type     The class/type of events to listen to.
      * @param listener The listener itself.
      */
     protected final <T extends EventListener> void removeEventListener(Class<T> type, T listener) {
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
     public final EventListener[] getListeners(Class<? extends EventListener> type) {
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
      * <b>Internal method</b> called by the CGs to indicate different states of the rendering process.
      *
      * @param type Either {@link #DONE_RENDERING} or {@link #START_RENDERING}.
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
             boolean oldVal = this.showAsFormComponent;
             this.showAsFormComponent = showAsFormComponent;
             reload();
             propertyChangeSupport.firePropertyChange("showAsFormComponent", oldVal, this.showAsFormComponent);
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
         ActionMap oldVal = this.actionMap;
         this.actionMap = actionMap;
         propertyChangeSupport.firePropertyChange("actionMap", oldVal, this.actionMap);
     }
 
     /**
      * Action map for key binding feature
      *
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
      *
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
      *
      * @param inputMap  The new input map
      * @param condition Either {@link #WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT} or {@link #WHEN_IN_FOCUSED_FRAME}
      * @see JComponent#setInputMap(int, javax.swing.InputMap)
      */
     public void setInputMap(int condition, InputMap inputMap) {
         initInputMaps();
         InputMap oldVal = inputMaps[condition];
         this.inputMaps[condition] = inputMap;
         registerGlobalInputMapWithFrame();
         propertyChangeSupport.firePropertyChange("inputMap", oldVal, this.inputMaps[condition]);
     }
 
     /**
      * @return The input map for the condition {@link #WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT}
      * @see #setInputMap(javax.swing.InputMap)
      */
     public InputMap getInputMap() {
         return getInputMap(WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT);
     }
 
     /**
      * @param condition Either {@link #WHEN_FOCUSED_OR_ANCESTOR_OF_FOCUSED_COMPONENT} or {@link #WHEN_IN_FOCUSED_FRAME}
      * @return The input map for the given condition.
      * @see #setInputMap(int, javax.swing.InputMap)
      */
     public InputMap getInputMap(int condition) {
         initInputMaps();
         InputMap result = inputMaps[condition];
         if (result == null) {
             inputMaps[condition] = new InputMap();
             result = inputMaps[condition];
         }
         registerGlobalInputMapWithFrame();
         return result;
     }
 
 
     private void registerGlobalInputMapWithFrame() {
         final SFrame parentFrame = getParentFrame();
         if (parentFrame != null)
             parentFrame.registerGlobalInputMapComponent(this);
 
         if (globalInputMapListener == null) {
             globalInputMapListener = new GlobalInputMapParentFrameListener(this);
             addParentFrameListener(globalInputMapListener);
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
         for (String value : values) {
             final Action action = actionMap.get(value);
             if (action != null) {
                 if (actionEvents == null)
                     actionEvents = new HashMap<Action, ActionEvent>();
 
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
 
     /**
      * Internal method to trigger firing of key events.
      */
     protected void fireKeyEvents() {
         if (actionEvents != null) {
             for (Map.Entry<Action, ActionEvent> entry : actionEvents.entrySet()) {
                 Action action = entry.getKey();
                 ActionEvent event = entry.getValue();
                 action.actionPerformed(event);
             }
             actionEvents.clear();
         }
     }
 
     /**
      * Method called to notify this <code>SComponent</code> that it has no longer a parent component.
      * This Method is called internal and should not be called directly, but can be overerloaded
      * to react on this event.
      */
     public void removeNotify() {
         /* currently nothing to do, but great to overwrite for some dangling eventListener */
     }
 
     /**
      * Method called to notify this <code>SComponent</code> that it has a new parent component.
      * This Method is called internal and should not be called directly, but can be overerloaded
      * to react on this event.
      */
     public void addNotify() {
         /* currently nothing to do */
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
 
     private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         // preprocessing, e. g. serialize static vars or transient variables as cipher text
         in.defaultReadObject(); // default serialization
         // do postprocessing here
     }
 
     private void writeObject(ObjectOutputStream out) throws IOException {
         try {
             // preprocessing
             out.defaultWriteObject(); // default
             // postprocessing
         } catch (IOException e) {
             log.warn("Unexpected Exception", e);
             throw e;
         }
     }
 
     /**
      * Listener registering/deregistering this component on the parent frame.
      */
     private final static class GlobalInputMapParentFrameListener implements SParentFrameListener, Serializable {
         private final SComponent me;
 
         public GlobalInputMapParentFrameListener(SComponent me) {
             this.me = me;
         }
 
         public void parentFrameAdded(SParentFrameEvent e) {
             if (e.getParentFrame() != null)
                 e.getParentFrame().registerGlobalInputMapComponent(me);
         }
 
         public void parentFrameRemoved(SParentFrameEvent e) {
             if (e.getParentFrame() != null)
                 e.getParentFrame().deregisterGlobalInputMapComponent(me);
         }
     }
 
 
     /**
      * Add a PropertyChangeListener to the listener list. The listener is registered for all properties.
      *
      */
     public void addPropertyChangeListener(PropertyChangeListener listener) {
         if(listener == null)
             return;
         propertyChangeSupport.addPropertyChangeListener(listener);
     }
 
     /**
      * Remove a PropertyChangeListener from the listener list.
      *
      */
     public void removePropertyChangeListener(PropertyChangeListener listener) {
         if(propertyChangeSupport != null)
             propertyChangeSupport.removePropertyChangeListener(listener);
     }
 
     /**
      * Add a PropertyChangeListener for a specific property.
      */
     public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
         if(listener == null || propertyName == null)
             return;
         propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
     }
 
 
     /**
      * Remove a PropertyChangeListener for a specific property.
      */
     public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
         if(propertyChangeSupport != null || propertyName != null)
             propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
     }
 }
