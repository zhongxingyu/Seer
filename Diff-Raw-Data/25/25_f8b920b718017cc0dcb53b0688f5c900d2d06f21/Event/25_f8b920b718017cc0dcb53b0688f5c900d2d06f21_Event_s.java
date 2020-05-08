 package eu.scape_project.pw.simulator.engine.model;
 
 
 public abstract class Event implements IEvent {
 
 	protected long scheduleTime;
 
 	protected String name;
 
 	
 	public long getScheduleTime() {
 		return scheduleTime;
 	}
 
 	public void setScheduleTime(long time) {
 		scheduleTime = time;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public int compareTo(IEvent e) {
 
 		if (this.scheduleTime < e.getScheduleTime()) {
 			return -1;
 		}
 		if (this.scheduleTime == e.getScheduleTime()) {
 			return 0;
 		}
 		return 1;
 
 	}
 
 }
