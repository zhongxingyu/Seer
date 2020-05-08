 package org.zkoss.todo.event;
 
 import java.util.List;
 import java.util.UUID;
 
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.bind.annotation.NotifyChange;
 
 public class EventViewModel {
 	
 	private EventDAO eventDao = new EventDAO();
 	
 	private TodoEvent selectedEvent, newEvent = new TodoEvent();
 	
 	public TodoEvent getSelectedEvent() {
 		return selectedEvent;
 	}
 
 	public void setSelectedEvent(TodoEvent selectedEvent) {
 		this.selectedEvent = selectedEvent;
 	}
 	
 	public TodoEvent getNewEvent() {
 		return newEvent;
 	}
 
 	public void setNewEvent(TodoEvent newEvent) {
 		this.newEvent = newEvent;
 	}
 
 	public List<TodoEvent> getEvents() {
 		return eventDao.findAll();
 	}
 	
 	@Command("add")
 	@NotifyChange("events")
 	public void add() {
 		this.newEvent.setId(UUID.randomUUID().toString());
 		eventDao.insert(this.newEvent);
 		this.newEvent = new TodoEvent();
 	}
 	
 	@Command("update")
 	@NotifyChange("events")
 	public void update() {
 		eventDao.update(this.selectedEvent);
 	}
 	
 	@Command("delete")
 	@NotifyChange("events")
 	public void delete() {
 		//shouldn't be able to delete with selectedEvent being null anyway
		//unless trying to hack the system, so just ignore the request
 		if(this.selectedEvent != null)
 			eventDao.delete(this.selectedEvent);
 	}
 }
