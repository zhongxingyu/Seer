 package de.flower.common.ui.ajax.event;
 
 import de.flower.common.model.IEntity;
 import org.apache.wicket.Component;
 import org.apache.wicket.event.Broadcast;
 
 /**
  * Small wrapper to minimize coding effort when sending wicket events for ajax updates.
  */
 public final class AjaxEventSender {
 
     /**
      * Instantiates a new ajax event sender.
      */
     private AjaxEventSender() {
     }
 
     /**
      * Send ajax event. Saves client from typing Broadcast.DEPTH every time.
      *
      * @param sender the sender
      * @param payload the payload
     * @deprecated try to use typed methods of this class. e.g. {@link #sendEntityChangedEvent(Component, Class)}.
      */
     @Deprecated
     public static void send(final Component sender, final Object payload) {
         sender.send(sender.getPage(), Broadcast.DEPTH, payload);
     }
 
     /**
      * Send entity changed event.
      *
      * @param sender the sender
      * @param entityClass the entity class
      */
     public static void entityEvent(final Component sender, final Class<? extends IEntity> entityClass) {
         send(sender, entityClass);
     }
 
 
 }
