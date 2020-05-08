 package de.flower.rmt.ui.page.events;
 
 import de.flower.common.ui.inject.InjectorAwareObject;
 import de.flower.rmt.model.db.entity.User;
 import de.flower.rmt.model.db.entity.event.Event;
 import de.flower.rmt.model.db.entity.event.QEvent;
 import de.flower.rmt.service.IEventManager;
 import de.flower.rmt.ui.model.EventModel;
 import de.flower.rmt.ui.model.UserModel;
 import org.apache.wicket.markup.repeater.data.IDataProvider;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import java.util.Iterator;
 
 /**
  * @author flowerrrr
  */
 public class EventDataProvider extends InjectorAwareObject implements IDataProvider<Event> {
 
     @SpringBean
     private IEventManager eventManager;
 
     private int itemsPerPage;
 
     private Long size;
 
     private IModel<User> userModel;
 
     private Long nextEventId;
 
     public EventDataProvider(final int itemsPerPage) {
         this(itemsPerPage, null);
     }
 
     public EventDataProvider(final int itemsPerPage, final UserModel userModel) {
         this.itemsPerPage = itemsPerPage;
         this.userModel = userModel;
     }
 
     @Override
     public Iterator<? extends Event> iterator(final int first, final int count) {
         int pageNum = first / itemsPerPage;
         return eventManager.findAll(pageNum, itemsPerPage, getUser(), QEvent.event.team).iterator();
     }
 
     @Override
     public int size() {
         // method is called at least twice during rendering -> cache value
         if (size == null) {
             size = eventManager.getNumEventsByUser(getUser());
         }
         return size.intValue();
     }
 
     /**
      * Get the next upcoming event.
      */
     public boolean isNextEvent(Event event) {
         if (nextEventId == null) {
            nextEventId = eventManager.findNextEvent(getUser()).getId();
         }
         return event.getId().equals(nextEventId);
     }
 
     @Override
     public IModel<Event> model(final Event object) {
         return new EventModel<Event>(object);
     }
 
     @Override
     public void detach() {
         this.size = null;
         if (userModel != null) {
             this.userModel.detach();
         }
         this.nextEventId = null;
     }
 
     private User getUser() {
         return (userModel == null) ? null : userModel.getObject();
     }
 }
