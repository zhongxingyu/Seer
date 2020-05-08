 /*******************************************************************************
  * Copyright (c) 17 dec. 2012 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/ 
 package com.netxforge.netxstudio.server.job.internal;
 
 import static com.google.inject.util.Modules.override;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.eclipse.osgi.framework.console.CommandInterpreter;
 import org.eclipse.osgi.framework.console.CommandProvider;
 import org.eclipse.osgi.service.debug.DebugOptions;
 import org.eclipse.osgi.service.debug.DebugOptionsListener;
 import org.eclipse.osgi.service.debug.DebugTrace;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.netxforge.netxstudio.server.ServerModule;
 import com.netxforge.netxstudio.server.job.JobHandler;
 
 /**
  * 
  * @author Christophe Bouhier
  *
  */
 public class JobActivator implements BundleActivator, DebugOptionsListener,
 		CommandProvider {
 
 	private static BundleContext context;
 	private static JobActivator INSTANCE;
 
 	private static final String PLUGIN_ID = "com.netxforge.netxstudio.server.job";
 	
 
 	// fields to cache the debug flags
 	public static boolean DEBUG = false;
 	public static DebugTrace TRACE = null;
 
 	// Tracing for jobs. 
 	public static String TRACE_JOBS_OPTION = "/trace.jobs";
 
 	public static BundleContext getContext() {
 		return context;
 	}
 
 	public static JobActivator getInstance() {
 		return INSTANCE;
 	}
 
 	private Injector injector;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	public void start(BundleContext bundleContext) throws Exception {
 		JobActivator.context = bundleContext;
 		INSTANCE = this;
 
 		// register our trace and debugging listener.
 		Dictionary<String, String> props = new Hashtable<String, String>(4);
 		props.put(DebugOptions.LISTENER_SYMBOLICNAME, PLUGIN_ID);
 		context.registerService(DebugOptionsListener.class.getName(), this,
 				props);
 
 		// register our command provider.
 		context.registerService(CommandProvider.class.getName(), this, null);
 
 	}
 
 	public void createInjector() {
 		Module om = ServerModule.getModule();
 		om = override(om).with(new JobModule());
 		injector = Guice.createInjector(om);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		JobActivator.context = null;
 		JobHandler.deActivate();
 	}
 
 	public Injector getInjector() {
 		return injector;
 	}
 
 	public void optionsChanged(DebugOptions options) {
 		DEBUG = options.getBooleanOption(PLUGIN_ID + "/debug", true);
 		TRACE = options.newDebugTrace(PLUGIN_ID);
 	}
 
 	public String getHelp() {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("---NetXStudio Job commands---\n");
 		buffer.append("\tjob status - status of the scheduler \n");
 		buffer.append("\tjob start - start the scheduler \n");
 		buffer.append("\tjob stop - stop the scheduler \n");
 		buffer.append("\tjob schedule - list the scheduled jobs \n");
 		buffer.append("\tjob list - list the existing jobs \n");
 		buffer.append("\tjob activate [Job name]- activate a job \n");
 		buffer.append("\tjob deactivate [Job name]- activate a job \n");
 		buffer.append("\tjob pause - pause the scheduled jobs (Running jobs are not interrupted) \n");
 		buffer.append("\tjob resume - resume the schduled jobs \n");
 		buffer.append("\tjob clean - clean job progress data\n");
 		return buffer.toString();
 	}
 
 	public Object _job(CommandInterpreter interpreter) {
 		try {
 			String cmd = interpreter.nextArgument();
 			if ("status".equals(cmd)) {
 				JobHandler.status(interpreter);
 				return null;
 			}
 			if ("schedule".equals(cmd)) {
 				JobHandler.scheduled(interpreter); // we could be initializing!
 				return null;
 			}
 			if ("list".equals(cmd)) {
 				JobHandler.list(interpreter); // we could be initializing!
 				return null;
 			}
 			if ("activate".equals(cmd)) {
 				JobHandler.activate(interpreter); // we could be initializing!
 				return null;
 			}
 			if ("deactivate".equals(cmd)) {
 				JobHandler.deActivate(interpreter); // we could be initializing!
 				return null;
 			}
 			if ("start".equals(cmd)) {
 				JobHandler.start(interpreter); // we could be initializing!
 				return null;
 			}
 			if ("stop".equals(cmd)) {
 				JobHandler.stop(interpreter); // we could be initializing!
 				return null;
 			}
 			if ("pause".equals(cmd)) {
 				JobHandler.pauseAll(interpreter); // we could be initializing!
 				return null;
 			}
 			if ("resume".equals(cmd)) {
 				JobHandler.resumeAll(interpreter); // we could be initializing!
 				return null;
 			}
 			if ("clean".equals(cmd)) {
 				JobHandler.clean(interpreter); // We could be initializing and
 												// running jobs!
 				return null;
 			}
 
 			interpreter.println(getHelp());
 		} catch (Exception ex) {
 			interpreter.printStackTrace(ex);
 		}
 
 		return null;
 	}
 
 }
