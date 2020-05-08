 package eu.scape_project.pw.simulator.engine.model;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.scape_project.pw.simulator.engine.model.state.ISimulationState;

 
 public abstract class Event implements IEvent {
 
	private Logger LOG = LoggerFactory.getLogger(Event.class);
	
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
 
	public void execute(ISimulationState state) {
		LOG.info("Executing event:" + name );
		this.run(state);
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
	
	protected abstract void run(ISimulationState state);
 
 }
