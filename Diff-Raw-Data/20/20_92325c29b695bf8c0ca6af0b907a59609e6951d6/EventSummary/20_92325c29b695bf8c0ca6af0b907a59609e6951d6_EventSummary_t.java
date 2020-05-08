 package reevent.web.myEvents;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.util.convert.IConverter;
 import reevent.domain.Event;
 import reevent.web.StyledPanel;
 import reevent.web.convert.DateTimeConverter;
 import reevent.web.media.MediaDisplay;
 
 import java.text.DateFormat;
 
 /**
  * A block that shows the main picture and basic data about an event.
  */
 public class EventSummary extends StyledPanel {
     MediaDisplay mainPicture;
     Label name;
     Label locationName;
     Label start;
     Label genre;
 
     public EventSummary(String id, IModel<Event> event) {
         super(id, new CompoundPropertyModel<Event>(event));
         this.add(mainPicture = new MediaDisplay("mainPicture", event.getObject().getMainPicture().getId()));
         this.add(new Label("name"));
         this.add(new Label("locationName"));
        this.add(new Label("performer"));
         this.add(new Label("start") {
             @SuppressWarnings("unchecked")
             @Override
             public IConverter getConverter(Class type) {
                 return DateTimeConverter.both(DateFormat.SHORT, DateFormat.SHORT);
             }
         });
         this.add(new Label("genre"));
     }
 }
