 /* 
  * WhiteCat - A dynamic role injector for agents.
  *
  * This project represents a new implementation of the so called BlackCat,
  * a project I made during my thesis degree. For more information about such project please see:
  * 
  *   G., L. Ferrari, L. Leonardi,
  *   Injecting Roles in Java Agents Through Run-Time Bytecode Manipulation
  *   IBM Systems Journal, Vol. 44, No. 1, pp.185-208, 2005
  *
  * This new approach exploits a completely different implementation, keeping the
  * same idea of BlackCat.
  * 
  *
  * Copyright (C) Luca Ferrari 2008-2010 - cat4hire@users.sourceforge.net
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
  */
 package whitecat.core.role.task.scheduling;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.eclipse.osgi.framework.eventmgr.EventDispatcher;
 
 import whitecat.core.RoleBooster;
 import whitecat.core.WCException;
 import whitecat.core.agents.AgentProxy;
 import whitecat.core.agents.AgentProxyID;
 import whitecat.core.event.Event;
 import whitecat.core.event.EventListener;
 import whitecat.core.event.EventType;
 import whitecat.core.exceptions.WCSchedulingException;
 import whitecat.core.role.descriptors.RoleDescriptor;
 import whitecat.core.role.task.IRoleTask;
 import whitecat.core.role.task.ITaskExecutionResult;
 
 /**
  * The default implementation of the task scheduler.
  * The scheduler keep a queue of tasks to be executed. Once a role event happens,
  * the scheduler checks the role that generated the event, and checks for a task
  * to execute on such role. If found, and every execution condition matches, then
  * the task is executed.
  * @author Luca Ferrari - cat4hire (at) sourceforge.net
  *
  */
 public class TaskSchedulerImpl implements ITaskScheduler, EventListener {
 
     /**
      * The logger for this class loader.
      */
    private static Logger logger = org.apache.log4j.Logger.getLogger(TaskScheduler.class);
     
     // configure the logger
     static{
 	DOMConfigurator.configure("conf/log4j.xml");
     }
 
     
     
     /** 
      * A role descriptor contains a set of tasks, but here we need to find
      * a role descriptor by the task, so we keep a map of role descriptors
      * and see, when an event occurs, if the role descriptor of the event
      * contains the task specified.
      *
      */
     private Map<IRoleTask, ScheduledTaskData> scheduledTasks = new HashMap< IRoleTask, ScheduledTaskData>();
     
     
     /**
      * An inner class to keep the data about a task to execute.
      * All the fields are public, this is intended to be used a C-like
      * struct.
      */
     class ScheduledTaskData{
 	public IRoleTask toExecute = null;
 	public AgentProxyID executor = null;
 	public TaskSchedulingExecutor executorMatchPolicy = null;
 	public TaskSchedulingInstant executionInstantPolicy = null;
 	ITaskExecutionResult result = null;
 	
 	public boolean equals( Object o ){
 	    if( !(o instanceof ScheduledTaskData) )
 		return false;
 	    else{
 		ScheduledTaskData st = (ScheduledTaskData) o;
 		return ( st.toExecute.equals( toExecute) && st.executor.equals( executor ) 
 			&& st.executionInstantPolicy.equals( executionInstantPolicy ) && st.executorMatchPolicy.equals( executionInstantPolicy ) 
 			);
 	    }
 		
 	}
     }
     
 
     /**
      * Registers this scheduler as a global event listener, so that it will be
      * notified of every role event.
      */
     public TaskSchedulerImpl(){
 	super();
 	// register myself as a global event listener
 	whitecat.core.event.EventDispatcher.getInstance().addGlobalEventListener( this );
     }
     
     
     /* (non-Javadoc)
      * @see whitecat.core.role.task.scheduling.ITaskScheduler#scheduleTask(whitecat.core.role.task.IRoleTask, whitecat.core.agents.AgentProxy, whitecat.core.role.task.scheduling.TaskSchedulingExecutor, whitecat.core.role.task.scheduling.TaskSchedulingInstant, whitecat.core.role.task.ITaskExecutionResult)
      */
     @Override
     public final synchronized boolean scheduleTask(
 				IRoleTask toExecute,
 				AgentProxyID executor,
 				TaskSchedulingExecutor executorMatchPolicy,
 				TaskSchedulingInstant executionInstantPolicy,
 				ITaskExecutionResult result)
 							    throws WCSchedulingException {
 	// build a scheduled task object
 	ScheduledTaskData singleTask = new ScheduledTaskData();
 	singleTask.toExecute = toExecute;
 	singleTask.executionInstantPolicy = executionInstantPolicy;
 	singleTask.executorMatchPolicy = executorMatchPolicy;
 	singleTask.executor = executor;
 	singleTask.result = result;
 	
 	// if this task has been already scheduled, do not add it again
 	if( this.scheduledTasks.containsValue(singleTask) )
 	    return false;
 	else{
 	    this.scheduledTasks.put(toExecute, singleTask);
 	    return true;
 	}
     }
 
     /* (non-Javadoc)
      * @see whitecat.core.role.task.scheduling.ITaskScheduler#removeScheduledTask(whitecat.core.role.task.IRoleTask, whitecat.core.agents.AgentProxy, whitecat.core.role.task.scheduling.TaskSchedulingExecutor, whitecat.core.role.task.scheduling.TaskSchedulingInstant, whitecat.core.role.task.ITaskExecutionResult)
      */
     @Override
     public final synchronized boolean removeScheduledTask(
 				       IRoleTask toExecute,
 				       AgentProxyID executor,
 				       TaskSchedulingExecutor executorMatchPolicy,
 				       TaskSchedulingInstant executionInstantPolicy,
 				       ITaskExecutionResult result) {
 	
 	// build a scheduled task object
 	ScheduledTaskData singleTask = new ScheduledTaskData();
 	singleTask.toExecute = toExecute;
 	singleTask.executionInstantPolicy = executionInstantPolicy;
 	singleTask.executorMatchPolicy = executorMatchPolicy;
 	singleTask.executor = executor;
 	singleTask.result = result;
 	
 	if( this.scheduledTasks.containsKey(singleTask) ){
 	    this.scheduledTasks.remove(singleTask);
 	    return true;
 	}
 	else 
 	    return false;
     }
 
     @Override
     public synchronized void handleEvent(Event event) {
 	// check arguments
 	if( event == null )
 	    return;
 	
 
 	
 	// get the agent proxy and the role this event is related to
 	AgentProxyID srcProxyID = event.getAgentProxyID();
 	EventType eType = event.getEventType();
 	RoleDescriptor descriptor = event.getRoleDescriptor();
 	if( descriptor == null )
 	    // cannot proceed!
 	    return;
 	
 	
 	try{
 
 	    // first of all search for a task that is contained into the role descriptor
 	    // and that has been scheduled for the execution
 	    for( IRoleTask executableTask : this.scheduledTasks.keySet() ){
 
 
 		
 		ScheduledTaskData scheduledTaskData = this.scheduledTasks.get( executableTask );
 
 		// for each scheduled task to be executed, see if it belongs
 		// to the role descriptor that generated the event
 		if( descriptor.containsTask(executableTask) ){
 		    // ok, the task belongs to the role that generated the event
 		    // so let see what kind of event we have and if it matches
 		    // the execution policy
 		    
 		    // first check if we have to execute for a specific proxy
 		    if( (scheduledTaskData.executorMatchPolicy.equals( TaskSchedulingExecutor.EXECUTE_BY_EXACT_AGENT) 
 			    && srcProxyID.equals(scheduledTaskData.executor) ) 
 			||
 			(scheduledTaskData.executorMatchPolicy.equals( TaskSchedulingExecutor.EXECUTE_BY_ANY_AGENT) )
 			){
 
 			// ok to execute the task on this agent
 			// check the event conditions
 
 			if( (eType.equals( EventType.PUBLIC_ROLE_ADDED ) && scheduledTaskData.executionInstantPolicy.equals( TaskSchedulingInstant.SCHEDULE_AT_ROLE_ASSUMPTION) )	// role assumption
 				||
 				( eType.equals( EventType.PUBLIC_ROLE_REMOVING ) && scheduledTaskData.executionInstantPolicy.equals( TaskSchedulingInstant.SCHEDULE_AT_ROLE_RELEASE) )	// role release
 				){
 			    // execute the task for this event
 			    this.execute(scheduledTaskData);
 			}
 		    }
 		}
 	    }
 	}
 	catch(WCException e ){
 	    // ops, the execution of the task has gone bad!
 	    logger.error("Something bad happened while executing a task ", e);
 	}
 
     }
 
     
     /**
      * Executes a specific task starting from its internal data. This is an utility method
      * that contains the logic to exeucte a single task. It requires that the task data
      * and its main fields are not null!
      * @param scheduledTaskData the data of the task to execute
      */
     private final void execute( ScheduledTaskData scheduledTaskData ) throws WCException{	
 	assert( scheduledTaskData != null );
 	assert( scheduledTaskData.result != null );
 	assert( scheduledTaskData.toExecute != null );
 	
 	// executed the task and place the result
 	scheduledTaskData.result = scheduledTaskData.toExecute.execute();
     }
     
 }
