 package com.anthropomo.digitalrattle.io;
 
 import java.util.List;
 import java.util.Vector;
 
 public class EventPublisher implements IEventPublisher {
 	private List<IEventSubscriber> subscribers;
 	private List<ICommand> events;
 	
 	public EventPublisher(){
 		subscribers = new Vector<IEventSubscriber>();
 		events = new Vector<ICommand>();
 	}
 	
 	public List<ICommand> getEventRecord(){
 		return events;
 	}
 	
 	public void reviewEvents(IEventSubscriber s){
 		for(ICommand e : events){
 			e.execute(s);
 		}
 	}
 	
 	private void maintainEventList(ICommand e){
 		if(events.contains(e)){
 			events.remove(e);
 		}
 		events.add(e);
 	}
 	
 	@Override
 	public void registerSubscriber(IEventSubscriber subscriber) {
 		subscribers.add(subscriber);
 		reviewEvents(subscriber);
 	}
 
 	@Override
 	public void unregisterSubscriber(IEventSubscriber subscriber) {
 		subscribers.remove(subscriber);
 	}
 	
 	@Override
 	public void sendEvent(ICommand e){
 		for(IEventSubscriber s : subscribers){
 			s.onEvent(e);
 		}
 	}
 	
 	@Override
 	public void receiveCommand(ICommand e){
 		sendEvent(e);
 		maintainEventList(e);
 	}
 
 }
