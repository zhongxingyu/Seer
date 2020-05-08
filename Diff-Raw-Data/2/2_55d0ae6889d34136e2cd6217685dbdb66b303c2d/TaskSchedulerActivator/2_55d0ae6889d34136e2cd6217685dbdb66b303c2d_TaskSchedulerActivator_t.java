 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 21st February 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.tasksched;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceReference;
 
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.tasksched.impl.SchedulingServiceListener;
 import au.edu.uts.eng.remotelabs.schedserver.tasksched.impl.TaskScheduler;
 
 /**
  * The task scheduler bundle sets up service listener to allow {@link Runnable}
  * services to be run be the executors in this bundles thread pool. If the 
  * {@link Runnable} task is to run to be registered and run, a property 
  * '<tt>period</tt>' must be set on the service specifying how often the 
  * task should run in seconds.
  */
 public class TaskSchedulerActivator implements BundleActivator 
 {
     /** The task scheduler. */
     private TaskScheduler scheduler;
     
     /** Runnable task service listener. */
     private SchedulingServiceListener listener;
     
     /** Logger. */
     private Logger logger;
 
     @Override
 	public void start(BundleContext context) throws Exception 
 	{
 		this.logger = LoggerActivator.getLogger();
 		this.logger.info("Starting Task Scheduler bundle.");
 		
 		this.scheduler = new TaskScheduler();
 		this.listener = new SchedulingServiceListener(this.scheduler, context);
 		
 		/* Set up the service listener for runnable tasks with a period. */
 		String filter = "(&(" + Constants.OBJECTCLASS + "=" + Runnable.class.getName() + ")(period>=1))";
 		this.logger.debug("Adding a service listener with filter " + filter + '.');
 		context.addServiceListener(this.listener, filter);
 		
 		/* Fire pseudo events for each of the previously registered tasks. */
		ServiceReference refs[] = context.getServiceReferences((String)null, filter);
 		if (refs != null)
 		{
     		for (ServiceReference ref : refs)
     		{
     		    this.listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, ref));
     		}
 		}
 	}
 	
 
 	@Override
 	public void stop(BundleContext context) throws Exception
 	{
 		context.removeServiceListener(this.listener);
 		this.scheduler.shutdown();
 	}
 
 }
