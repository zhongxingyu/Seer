 package com.lostcode.javalib.entities.processes;
 
 import com.lostcode.javalib.entities.EntityWorld;
 
 /**
  * A {@link Process} that simply waits for a specified amount of time before
  * triggering its child processes.
  * 
  * @author Natman64
  * 
  */
 public class DelayProcess extends Process {
 
 	// region Fields
 
	protected float time;
 
 	// endregion
 
 	// region Initialization
 
 	public DelayProcess(float time, Process... children) {
 		this.time = time;
 
 		for (Process child : children) {
 			attachChild(child);
 		}
 	}
 
 	// endregion
 
 	// region Update
 
 	@Override
 	public void update(EntityWorld world, float deltaTime) {
 		time -= deltaTime;
 
 		if (time <= 0) {
 			end(ProcessState.SUCCEEDED);
 		}
 	}
 
 	// endregion
 
 	// region Events
 
 	@Override
 	public void onEnd(EntityWorld world, ProcessState endState) {
 	}
 
 	// endregion
 
 }
