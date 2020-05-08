 package reevent.web.myEvents.newEvent;
 
 import org.apache.wicket.markup.html.list.ListItem;
 
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import reevent.dao.EventDao;
 import reevent.domain.Event;
import reevent.web.Template;
 import reevent.web.myEvents.EventDetails;
 import reevent.web.myEvents.myEvents;
 
public class detailEvent extends Template {
 	ListView<Event> detailEvent;
 
     @SpringBean
     EventDao eventDao;
 	
 	public detailEvent(Event o){
 		// TO DO: instead of findAll(0, 1) which returns just the first Event there should be displayed the Event given in the method header?
 		
 		this.add(new EventDetails("details", new Model(o)));
             }
 		
 	}
