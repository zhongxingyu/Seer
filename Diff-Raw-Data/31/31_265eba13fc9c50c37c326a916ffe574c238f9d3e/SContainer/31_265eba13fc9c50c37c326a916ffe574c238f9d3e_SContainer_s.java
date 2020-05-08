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
 
 import org.wings.event.SContainerEvent;
 import org.wings.event.SContainerListener;
 import org.wings.plaf.ContainerCG;
 import org.wings.style.StyleConstants;
 import org.wings.util.ComponentVisitor;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 
 /**
  * This is a container which can hold several other <code>SComponents</code>.
  *
  * @see SLayoutManager
  * @see SComponent
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SContainer extends SComponent implements ClickableRenderComponent
 {
     /**
      * @see #getCGClassID
      */
     private static final String cgClassID = "ContainerCG";
 
     /**
      * The layout for the component.
      */
     protected SLayoutManager layout;
 
     /**
      * background image, that is rendered for this container.
      */
     private SIcon backgroundImage;
 
     /**
      * The components in this container.
      */
     private ArrayList componentList;
 
     /**
      * The constraints for the components.
      */
     private ArrayList constraintList;
     
      
 
     /**
      * creates a new container with the given layout
      *
      * @param l the layout for this container
      */
     public SContainer(SLayoutManager l) {
         setLayout(l);
     }
 
     /**
      * Creates a new container with no layout manager, i.e. the components
      * are simply written in the same order they were added.
      */
     public SContainer() {}
 
     public void updateCG() {
         super.updateCG();
 
         if (layout != null)
             layout.updateCG();
     }
 
     /**
      * Sets a new layout manager.
      *
      * @param l new layout manager
      */
     public void setLayout(SLayoutManager l) {
         if (layout != null) {
             for ( int i=0; i<getComponentCount(); i++ ) {
                 layout.removeComponent(getComponent(i));
             }
             layout.setContainer(null);
         }
 
         layout = l;
 
         if ( layout!=null ) {
             for ( int i=0; i<getComponentCount(); i++ ) {
                 layout.addComponent(getComponent(i), getConstraintAt(i), i);
             }
 
             layout.setContainer(this);
         }
     }
 
     /**
      * Set the background image.
      *
      * @param icon the SIcon representing the background image.
      */
     public void setBackgroundImage(SIcon icon) {
         backgroundImage = icon;
         if (icon == null) {
             removeAttribute(StyleConstants.BACKGROUND_IMAGE);
         }
         else {
             setAttribute(StyleConstants.BACKGROUND_IMAGE, 
                          "url(" + icon.getURL().toString() + ")");
         }
     }
 
     /**
      * return the background image for this frame.
      *
      * @return the background image as SIcon.
      */
     public SIcon getBackgroundImage() {
         return backgroundImage;
     }
 
     /**
      * Returns the current layout
      *
      * @return current layout
      */
     public SLayoutManager getLayout() {
         return layout;
     }
 
     /**
      * Adds the specified container listener to receive container events
      * from this container.
      * If l is null, no exception is thrown and no action is performed.
      *
      * @param    l the container listener
      */
     public void addContainerListener(SContainerListener l) {
         addEventListener(SContainerListener.class, l);
     }
 
     /**
      * Removes the specified container listener so it no longer receives
      * container events from this container.
      * If l is null, no exception is thrown and no action is performed.
      *
      * @param 	l the container listener
      */
     public void removeContainerListener(SContainerListener l) {
         removeEventListener(SContainerListener.class, l);
     }
 
     protected void fireContainerEvent(int type, SComponent comp) {
         SContainerEvent event = null;
 
         Object[] listeners = getListenerList();
         for ( int i = listeners.length-2; i>=0; i -= 2 ) {
             if ( listeners[i]==SContainerListener.class ) {
                 // Lazily create the event:
                 if ( event==null )
                     event = new SContainerEvent(this, type, comp);
 
                 processContainerEvent((SContainerListener)listeners[i+1],
                                       event);
             }
         }
     }
 
     /**
      * Processes container events occurring on this container by
      * dispatching them to any registered ContainerListener objects.
      * NOTE: This method will not be called unless container events
      * are enabled for this component; this happens when one of the
      * following occurs:
      * a) A ContainerListener object is registered via addContainerListener()
      * b) Container events are enabled via enableEvents()
      * @param e the container event
      */
     protected void processContainerEvent(SContainerListener listener,
                                          SContainerEvent e) {
         switch( e.getID() ) {
         case SContainerEvent.COMPONENT_ADDED:
             listener.componentAdded(e);
             break;
 
         case SContainerEvent.COMPONENT_REMOVED:
             listener.componentRemoved(e);
             break;
         }
     }
 
     protected ArrayList getComponentList() {
         if ( componentList == null ) {
             componentList = new ArrayList(3);
         }
         return componentList;
     }
 
     protected ArrayList getConstraintList() {
         if ( constraintList == null )
             constraintList = new ArrayList(3);
         return constraintList;
     }
 
 
     /**
      * returns the number of components in this container
      *
      * @return number of components
      */
     public int getComponentCount() {
         return getComponentList().size();
     }
 
     /**
      * returns the component at the given position
      *
      * @param i position
      * @return component at given pos
      * @deprecated use {@link #getComponent(int)} instead (awt conformity)
      */
     public SComponent getComponentAt(int i) {
         return getComponent(i);
     }
 
     /**
      * returns the component at the given position
      *
      * @param i position
      * @return component at given pos
      */
     public SComponent getComponent(int i) {
         return (SComponent)getComponentList().get(i);
     }
 
     public SComponent[] getComponents() {
         // vorsichtig mit Threads ( eigentlich TreeLock!!!)
         return (SComponent[])getComponentList().toArray(new SComponent[getComponentCount()]);
     }
 
 
     /**
      * returns the constraint for the given component position
      *
      * @param i position
      * @return constraint for component at given position
      */
     public Object getConstraintAt(int i) {
         return getConstraintList().get(i);
     }
 
 
 
     /**
      * Removes the given component from the container. This includes removing
      * it from the LayoutManager (if set), the internal list and
      * the dispatcher.
      *
      * @param c the component to remove
      * @deprecated use {@link #remove(SComponent)} instead for swing conformity
      */
     public void removeComponent(SComponent c) {
         remove(c);
     }
 
     /**
      * Removes the component at the given position from the container.
      *
      * @param i the position of the component to remove
      * @return the removed component
      * @deprecated use {@link #remove(int)} instead for swing conformity
      */
     public SComponent removeComponentAt(int i) {
         SComponent c = getComponent(i);
         remove(c);
         return c;
     }
 
     /**
      * Removes the given component from the container.
      *
      * @param c the component to remove
      * @see #removeComponent(org.wings.SComponent)
      */
     public void remove(SComponent c) {
         if ( c==null )  return;
 
         if ( layout!=null )
             layout.removeComponent(c);
 
         int index = getComponentList().indexOf(c);
         if ( getComponentList().remove(c) ) {
             getConstraintList().remove(index);

             fireContainerEvent(SContainerEvent.COMPONENT_REMOVED, c);
 
             c.setParent(null);
             reload(ReloadManager.RELOAD_CODE);
         }
     }
 
     /**
      * Removes the component at the given position from the container.
      *
      * @param index remove the component at position <i>index</i>
      * 	from this container
      */
     public void remove(int index) {
         SComponent c = getComponent(index);
         remove(c);
     }
 
     /**
      * Removes all components from the container.
      * 
      * @deprecated use {@link #removeAll()} instead for swing conformity
      */
     public void removeAllComponents() {
         removeAll();
     }
 
     /**
      * Removes all components from the container.
      */
     public void removeAll() {
         while ( getComponentCount() > 0 ) {
             remove(0);
         }
     }
 
 
     /**
      * Adds a component to the container with null constraint at the end
      * of the internal list.
      *
      * @param c the component to add
      * @return the added component
      */
     public SComponent add(SComponent c) {
         return addComponent(c, null);
     }
 
     /**
      * Adds a component to the container with the given constraint at the end
      * of the internal list.
      *
      * @param c the component to add
      * @param constraint the constraint for this component
      */
     public void add(SComponent c, Object constraint) {
         addComponent(c, constraint);
     }
 
     /**
      * Adds a component to the container with null constraint at the given
      * index.
      *
      * @param c the component to add
      * @param index the index of the component
      * @return the added component
      */
     public SComponent add(SComponent c, int index) {
         return addComponent(c, null, index);
     }
 
     /**
      * Adds a component to the container with the given constraint at
      * the given index.
      *
      * @param c the component to add
      * @param index the index of the component
      */
     public void add(SComponent c, Object constraint, int index) {
         addComponent(c, constraint, index);
     }
 
     /**
      * Adds a component to the container with null constraint at the end
      * of the internal list.
      *
      * @param c the component to add
      * @return the added component
      */
     public SComponent addComponent(SComponent c) {
         return addComponent(c, null);
     }
 
     /**
      * Adds a component to the container with the given constraint at the end
      * of the internal list.
      *
      * @param c the component to add
      * @param constraint the constraint for this component
      * @return the added component
      */
     public SComponent addComponent(SComponent c, Object constraint) {
         return addComponent(c, constraint, getComponentList().size());
     }
 
     /**
      * Adds a component to the container with the given constraint at
      * the given index.
      *
      * @param c the component to add
      * @param index the index of the component
      * @return the added component
      */
     public SComponent addComponent(SComponent c, int index) {
         return addComponent(c, null, index);
     }
 
     /**
      * Adds a component to the container with the given constraint at
      * the given index.
      *
      * @param c the component to add
      * @param index the index of the component
      * @return the added component
      */
     public SComponent addComponent(SComponent c, Object constraint, int index){
         if (c != null) {
             if (layout != null)
                 layout.addComponent(c, constraint, index);
 
             getComponentList().add(index, c);
             getConstraintList().add(index, constraint);
             c.setParent(this);
 
             fireContainerEvent(SContainerEvent.COMPONENT_ADDED, c);
             reload(ReloadManager.RELOAD_ALL);
         }
 
         return c;
     }
 
     /**
      * Sets the parent frame.
      *
      * @param f parent frame
      */
     protected void setParentFrame(SFrame f) {
         if ( f!=parentFrame ) {
             super.setParentFrame(f);
             for ( int i=0; i<getComponentCount(); i++ ) {
                 getComponent(i).setParentFrame(getParentFrame());
             }
         }
     }
 
     /**
      * CAVEAT this did not work yet... We need to clone the layout manager as
      * well, so SLayoutManager must be Cloneable
      */
     public Object clone() {
         try {
             SContainer erg = (SContainer)super.clone();
             // uiuiui, layout manager must be cloned as well,...
             
             // componentList and constraintList contain references to the
             // original components / constraints
             erg.getComponentList().clear();
             erg.getConstraintList().clear();
             for ( int i=0; i<getComponentCount(); i++ ) {
                 erg.addComponent((SComponent)getComponent(i).clone());
             }
             return erg;
         }
         catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
     public String getCGClassID() {
         return cgClassID;
     }
 
     public void setCG(ContainerCG cg) {
         super.setCG(cg);
     }
 
     /**
      * Invite a ComponentVisitor.
      * Invokes visit(SContainer) on the ComponentVisitor.
      * @param visitor the visitor to be invited 
      */
     public void invite(ComponentVisitor visitor)
         throws Exception
     {
         visitor.visit(this);
     }
     
     /**
      * Calls the visitor on each SComponent this container has. You might
      * want to call this in your visitor in visit(SContainer).
      *
      * @param visitor an implementation of the {@link ComponentVisitor}
      *                interface.
      */
     public void inviteEachComponent(ComponentVisitor visitor) 
         throws Exception {
 	Iterator iterator = getComponentList().iterator();
 	while (iterator.hasNext()) {
 	    ((SComponent)iterator.next()).invite(visitor);
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
