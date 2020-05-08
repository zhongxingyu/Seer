 /*
  * $Id$
  * (c) Copyright 2001 wingS development team.
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
 package org.wings.event;
 
 import org.wings.SContainer;
 import org.wings.SComponent;
 
 /**
  * A container event, that is issued, whenever a 
  * component is added or removed from an container.
  *
  * @see org.wings.SContainer#addContainerListener(SContainerListener)
  * @author <a href="mailto:H.Zeller@acm.org">Henner Zeller</a>
  */
 public class SContainerEvent extends SComponentEvent {
     /**
      * The first number of used IDs for container events.
      */
     public static final int CONTAINER_FIRST   = 11000;
 
     /**
      * An event with this ID indicates, that a component was added to
      * the container.
      */
     public static final int COMPONENT_ADDED   = CONTAINER_FIRST;
 
     /**
      * An event with this ID indicates, that a component was removed from
      * the container.
      */
     public static final int COMPONENT_REMOVED = CONTAINER_FIRST + 1;
 
     /**
      * The last number of used IDs for container events.
      */
     public static final int CONTAINER_LAST    = COMPONENT_REMOVED;
 
     /**
      * the child component that has been added or removed.
      */
     private final SComponent child;
 
     /**
      * create a new container event issued by 'source' and affecting
      * 'child'.
      * @param source the Container issuing this event
      * @param id     the integer ID describing the action; one of
      *               <code>COMPONENT_ADDED</code> 
      *               or <code>COMPONENT_REMOVED</code>.
     * @param chilld the component that is affected by this event, i.e. that
      *               is added or removed.
      */
     public SContainerEvent(SContainer source, int id, SComponent child) {
 	super(source, id);
 	this.child = child;
     }
     
     /**
      * returns the source container, this event origins from.
      */
     public SContainer getContainer() { 
 	return (SContainer) source;
     }
 
     /**
      * returns the child component, whose new status in the container
      * is reported by this event.
      */
     public SComponent getChild() {
 	return child;
     }
 
     public String paramString() {
 	switch (id) {
 	case COMPONENT_ADDED:
 	    return "COMPONENT_ADDED";
 	case COMPONENT_REMOVED:
 	    return "COMPONENT_REMOVED";
 	default:
 	    return super.paramString();
 	}
     }
     
     public String toString() {
         return "ContainerEvent[container=" + source + "; " + paramString() + 
             "child=" + child + "]";
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
