 /*
  * Events.java
  * Author: Nick Grauel
  * Group: Nick Grauel, Tyler Janowski, Nick DeRossi
  * This class mantains a collection of events ordered by event name.
  */
 
 import java.util.*;
 
 public class Events extends ArrayList<Event>{
     
     //Adds an event to the collection.
     public boolean addEvent(Event e)
     {
 	for(int x = 0; x < size(); x++)
 	{
 	    if(get(x).isConflicting(e))
 		return false;
 	}
 	add(e);
 	return true;
     }
     
     //Finds an event given the event name.
     public Event find(String name)
     {
         for(int x = 0; x < size(); x++)
 	{
 	    if(get(x).getName().equals(name))
 		return get(x);
 	}
 	return null;
     }
     
     //Updates the providers used for all events based on the current capacity.
     public void updateEventProviders(int curCap)
     {
 	for(int x = 0; x < size(); x++)
 	    get(x).updateProviders(curCap);
     }
 
     //Adds a provider to all events in the collection.
     public void addProvider(Provider p)
     {
 	for(int x = 0; x < size(); x++)
 	    get(x).addProvider(p);
     }
 
     //Returns the list of all events in the collection.
     public String toString()
     {
      String str = "";
       for(int x = 0; x < size(); x++)
       {
 	  str += get(x).toString() + '\n';
       }
       return str;
     }
     
 }
