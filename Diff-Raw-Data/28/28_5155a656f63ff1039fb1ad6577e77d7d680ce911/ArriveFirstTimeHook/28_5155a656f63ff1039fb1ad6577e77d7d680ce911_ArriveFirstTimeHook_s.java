 package org.lehirti.luckysurvivor.peninsulaisthmus;
 
import static org.lehirti.engine.state.StateObject.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.lehirti.engine.events.Event;
 import org.lehirti.engine.events.Location;
 import org.lehirti.engine.events.LocationHook;
 
 public final class ArriveFirstTimeHook implements LocationHook {
   static {
     /**
      * LocationHooks get registered with Locations like this; we tell the game that when the player enters the location
      * "MapToCliffWest" it should ask the location hook "MeetJordanForTheFirstTimeHook" - this class - if there are
      * events available for the player to encounter
      */
     Location.registerDispatcher(MapToPeninsulaIsthmus.class, new ArriveFirstTimeHook());
   }
   
   /**
    * now, whenever the player enters the MapToCliffWest location, the getCurrentEvents() method is called; it must
    * return all available events, together with the probability of the event happening
    */
   @Override
   public Map<Event<?>, Double> getCurrentEvents() {
     // we start out with an empty map, meaning no events from this hook
     final Map<Event<?>, Double> events = new HashMap<Event<?>, Double>();
     
     /*
      * TODO since in-game time is not really progressing, yet, i just check if the player has been here before, has not
      * met jordan, yet and if the time is greater than 0 (which is always true); once time is progressing properly, this
      * can be changed easily. if you want to show the event only after day 103; 18 hours 44 minutes and 3 seconds just
      * write DateTime.getDDhhmmss() > 103184403
      */
     if (is(PeninsulaIsthmusBool.HAS_NOT_BEEN_HERE_BEFORE) && !is(PeninsulaIsthmusBool.HAS_SEEN_GORGE)) {
      // I removed the time, since it doesn't matter when you arrive. It will just tell you, that there is a gorge you cannot cross(yet)
       events.put(new ArriveFirstTime(), Double.valueOf(100.0));
     }
     return events;
   }
 }
