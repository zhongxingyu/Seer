import java.lang.ArrayList;
 
 public class EventManager extends Thread{
 	private ArrayList<Event> eventList;
 	
 	public EventManager(){
 		eventList = new ArrayList<Event>();
 	}
 
 	public void run(){
 		while(true){
 			Iterator<Event> it = eventList.iterator();
 			while(it.hasNext()){
 				Event nextEvent = it.next();
				if(nextEvent.proof){
 					nextEvent.trigger();
 				}
 			}
 		}
 	}
 
 	public boolean registerEvent(Event e){
 		return eventList.add(e);
 	}
 
 	public boolean unregisterEvent(Event e){
 		return eventList.remove(e);
 	}
 }
