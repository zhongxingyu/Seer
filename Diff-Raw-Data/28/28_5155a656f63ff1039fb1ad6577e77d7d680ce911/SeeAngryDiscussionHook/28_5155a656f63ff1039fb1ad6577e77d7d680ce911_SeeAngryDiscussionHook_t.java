 package org.lehirti.luckysurvivor.crashsite;
 
import static org.lehirti.engine.state.State.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.lehirti.engine.events.Event;
 import org.lehirti.engine.events.Location;
 import org.lehirti.engine.events.LocationHook;
 import org.lehirti.engine.state.DateTime;
 
 public final class SeeAngryDiscussionHook implements LocationHook {
   static {
     Location.registerDispatcher(MapToCrashSite.class, new SeeAngryDiscussionHook());
   }
   
   @Override
   public Map<Event<?>, Double> getCurrentEvents() {
     final Map<Event<?>, Double> events = new HashMap<Event<?>, Double>();
     
    if (is(CrashSiteBool.HAS_BEEN_HERE_BEFORE) && DateTime.getDDhhmmss() > 0
        && !is(CrashSiteBool.HAS_SEEN_ANGRY_DISCUSSION)) {
      events.put(new SeeAngryDiscussion(), Double.valueOf(100.0));
      /*
       * I Like the idea with the %! 100%for testing
       */
     }
    return events;
   }
 }
