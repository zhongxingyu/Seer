 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.client.jme.input;
 
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.client.input.Event;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 
 /**
  * An event which indicates that the mouse location has entered or exitted the "swing region."
  * The swing region is the union of the regions occupied by all WindowSwings.
  *
  * @author deronj
  */
 @ExperimentalAPI
 public class SwingEnterExitEvent3D extends Event {
 
     static {
         /** Allocate this event type's class ID. */
         EVENT_CLASS_ID = Event.allocateEventClassID();
     }
     /** Whether this event is an enter or an exit. */
     private boolean isEntered;
    /** The entity entered or exitted. */
    private Entity entity;
 
     /** Default constructor (for cloning) */
     protected SwingEnterExitEvent3D() {
     }
 
     /**
      * Create a new SwingEnterExitEvent3D.
      * @param isEntered Whether this is an enter or exit event.
      * @param entity The entity entered or exitted.
      */
     public SwingEnterExitEvent3D(boolean isEntered, Entity entity) {
         this.isEntered = isEntered;
         this.entity = entity;
     }
 
     /**
      * Returns true if this event is an enter.
      */
     public boolean isEntered() {
         return isEntered;
     }
 
     /**
      * Returns the event's entity.
      */
     public Entity getEntity() {
         return entity;
     }
 
     /**
      *
     public String toString () {
     return "SwingEnterExitEvent3D, isEntered = " + isEntered + ", entity = " + entity);
     }
 
     /** 
      * {@inheritDoc}
      * <br>
      * If event is null, a new event of this class is created and returned.
      */
     @Override
     public Event clone(Event event) {
         if (event == null) {
             event = new SwingEnterExitEvent3D(isEntered, entity);
         }
         return super.clone(event);
     }
 }
