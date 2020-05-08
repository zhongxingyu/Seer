 package reevent.web.myEvents.newEvent;
 
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.util.convert.IConverter;
 import reevent.domain.Event;
 import reevent.domain.User;
 import reevent.service.EventService;
 import reevent.web.ReEventSession;
 import reevent.web.convert.DateTimeConverter;
 import reevent.web.myEvents.myEvents;
 
 import java.text.DateFormat;
 import java.util.Date;
 
 @AuthorizeInstantiation("USER")
 public class editEvent extends myEvents {
 
 	ListView<Event> myEventList;
 	
 	Link detailEvent;
 	
 	@SpringBean
 	EventService events;
 	 
 	public editEvent(){
 		
     User user = ReEventSession.get().getUserSignedIn();
     
     
    
     add(myEventList = new ListView<Event>("myEventList", events.getByUser(user)) {
         @Override
         protected void populateItem(ListItem<Event> item) {
         	
             item.add(new Label("eventName", new PropertyModel(item.getModel(), "name")));
             
             item.add(detailEvent = new Link("detailEvent"){
             	public void onClick() {
             		
             		Event obj = (Event) getModelObject();
                     setResponsePage(new detailEvent(obj));
             		
             	}
             });
             
             item.add(new Label("eventStart", new PropertyModel(item.getModel(), "start")) {
                 @Override
                 public <C> IConverter<C> getConverter(Class<C> type) {
                     if (!Date.class.isAssignableFrom(type)) {
                         throw new IllegalArgumentException("Can't convert from types other than Date");
                     }
                     return (IConverter<C>) DateTimeConverter.both(DateFormat.SHORT, DateFormat.SHORT);
                 }
             });
             
             
             item.add(new Label("eventPerformer", new PropertyModel(item.getModel(), "performer")));
            item.add(new Label("eventLocation", new PropertyModel(item.getModel(), "location")));
             
         }
     });
 		
 	}
 	
 	
 }
