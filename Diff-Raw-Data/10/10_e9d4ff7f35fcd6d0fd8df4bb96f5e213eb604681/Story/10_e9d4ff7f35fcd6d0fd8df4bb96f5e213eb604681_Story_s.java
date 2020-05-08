 /*
  *  Copyright (C) 2013 caryoscelus
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Additional permission under GNU GPL version 3 section 7:
  *  If you modify this Program, or any covered work, by linking or combining
  *  it with Clojure (or a modified version of that library), containing parts
  *  covered by the terms of EPL 1.0, the licensors of this Program grant you
  *  additional permission to convey the resulting work. {Corresponding Source
  *  for a non-source form of such a combination shall include the source code
  *  for the parts of Clojure used as well as that of the covered work.}
  */
 
 package chlorophytum.story;
 
 import chlorophytum.*;
 
 import com.badlogic.gdx.Gdx;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 /**
  * Story is singleton storing named objects and events
  */
 public class Story {
     public static Story instance () {
         return World.instance().story;
     }
     
     
     protected Map<String, StoryObject> objects = new HashMap();
     protected Map<String, StoryEvent> events = new HashMap();
     
     /**
      * @deprecated
      */
     protected StoryDialog saved = null;
     
     public StoryScreen screen;
     
     protected LinkedList<StoryContext> contexts = new LinkedList();
     
     /**
      * Init: load scripts and run their init
      */
     public void init () {
         Scripting.run("data/scripts/story.clj");
         trigger("_init");
     }
     
     
     //// Object storing
     
     /**
      * Add new named event
      * @param name new event's name
      * @param event event itself
      */
     public void addEvent (String name, StoryEvent event) {
         events.put(name, event);
     }
     
     /**
      * Get event by name
      * @param name name of event
      * @return event
      */
     public StoryEvent getEvent (String name) {
         return events.get(name);
     }
     
     /**
      * Add StoryObject to story object list
      */
     public void addObject (String name, StoryObject object) {
         objects.put(name, object);
     }
     
     /**
      * Get story object by name
      */
     public StoryObject getObject (String name) {
         return objects.get(name);
     }
     
     
     //// Contexts
     
     /**
      * @return currently active context
      */
     public StoryContext activeContext () {
         StoryContext c = contexts.peek();
         if (c == null) {
             return newContext();
         }
         return c;
     }
     
     /**
      * Creates new context and adds it to queue of contexts
      * @return newly created context
      */
     public StoryContext newContext () {
         StoryContext c = new StoryContext(this);
         contexts.add(c);
         return c;
     }
     
     //// Events
     
     public void queue (String name) {
         trigger (name, newContext());
     }
     
     public void trigger (String name) {
         trigger(name, null);
     }
     
     public void trigger (StoryEvent event) {
         trigger(event, null);
     }
     
     /**
      * Trigger event by name
      */
     public void trigger (String name, StoryContext context) {
         if (getEvent(name) == null) {
             Gdx.app.log("story", "triggered event is null: "+name);
         } else {
             trigger(getEvent(name), context);
         }
     }
     
     /**
      * Trigger specific event
      */
     public void trigger (StoryEvent event, StoryContext context) {
         if (context == null) {
             context = activeContext();
         }
         
         if (event != null) {
             event.trigger(context);
         } else {
             Gdx.app.log("story", "triggered event is null");
         }
     }
     
     /**
      * Dialog was closed..
      * This is not something strictly defined, but ok for now..
      */
     public void closed () {
         trigger("_dialogexit");
     }
     
     public void show () {
         screen.showStory (activeContext());
     }
 }
