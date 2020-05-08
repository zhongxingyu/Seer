 package jDistsim.core.simulation.event.description;
 
import jDistsim.core.simulation.event.ui.IEventDescription;
 import jDistsim.utils.resource.EventTextResource;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 29.9.12
  * Time: 18:13
  */
 public class EmptyDescription implements IEventDescription {
 
     @Override
     public String getTitle() {
         return EventTextResource.EMPTY_EVENT_DESCRIPTION_TITLE;
     }
 
     @Override
     public String getDescription() {
         return EventTextResource.EMPTY_EVENT_DESCRIPTION_TEXT;
     }
 }
