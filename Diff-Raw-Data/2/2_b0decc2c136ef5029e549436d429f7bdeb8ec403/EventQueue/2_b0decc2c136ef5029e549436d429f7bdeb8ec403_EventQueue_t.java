 package core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 public class EventQueue {
 
 	private static final int HIGH_PRIORITY = 0;
 	private static final int MEDIUM_PRIORITY = 1;
 	private static final int NORMAL_PRIORITY = 2;
 	private int maximumQueueSize;
 	private List<LinkedList<String>> queues;
 	private Map<String, ArrayList<EventListener>> myEventListeners;
 
 	public EventQueue() {
 		myEventListeners = new HashMap<String, ArrayList<EventListener>>();
 
		queues = new ArrayList<LinkedList<String>>();
		
 		queues.add(new LinkedList<String>());
 		queues.add(new LinkedList<String>());
 		queues.add(new LinkedList<String>());
 	}
 
 	public EventQueue(int numQueues) {
 		for (int i = 0; i < numQueues; i++) {
 			queues.add(new LinkedList<String>());
 		}
 	}
 
 	public void addEvent(String e) {
 		queues.get(0).add(e);
 	}
 
 	public void addEvent(String e, int queueNumber) {
 		queues.get(queueNumber).add(e);
 	}
 
 	public String removeEvent() {
 		return queues.get(0).remove();
 	}
 
 	public String removeEvent(int queueNumber) {
 		return queues.get(queueNumber).remove();
 	}
 
 	public boolean hasEvents() {
 		for (LinkedList<String> list : queues) {
 			if (!list.isEmpty()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void registerEventListener(String e, EventListener listener) {
 		if (!myEventListeners.containsKey(e)) {
 			ArrayList<EventListener> list = new ArrayList<EventListener>();
 			list.add(listener);
 			myEventListeners.put(e, list);
 		} else {
 			ArrayList<EventListener> list = myEventListeners.get(e);
 			list.add(listener);
 			myEventListeners.put(e, list);
 		}
 	}
 
 	public void unregisterEventListener(String e, EventListener listener){
 		ArrayList<EventListener> list = myEventListeners.get(e);
 		list.remove(listener);
 		myEventListeners.put(e, list);
 	}
 	
 	public ArrayList<EventListener> getEventListeners(String eventName){
 		return myEventListeners.get(eventName);
 	}
 	
 	public Object[] getNextEvents() {
 		ArrayList<String> list = new ArrayList<String>();
 		for (Queue<String> q : queues) {
 			list.add(q.poll());
 		}
 		return list.toArray();
 	}
 
 	public void swapQueues(int q1, int q2) {
 		queues.get(q2).addAll(0, queues.get(q1));
 		queues.get(q1).clear();
 		queues.set(q1, queues.get(q2));
 		queues.get(q2).clear();
 	}
 
 	public Map<String, ArrayList<EventListener>> getEventListenerMap(){
 		return myEventListeners;
 	}
 	
 	// public void addEvent(Event e, int queueNumber){
 	// if(queueNumber >= queues.length-1){
 	// Queue<Event>[] temp = new Queue[queueNumber-queues.length+1];
 	// temp[queueNumber-queues.length+1] = new LinkedList<Event>();
 	// temp[queueNumber-queues.length+1].add(e);
 	// }
 	// queues[queueNumber].add(e);
 	// }
 
 }
