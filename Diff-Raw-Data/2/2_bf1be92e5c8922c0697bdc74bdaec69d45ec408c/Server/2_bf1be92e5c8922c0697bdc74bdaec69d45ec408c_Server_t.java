 /*******************************************************************************
  * Copyright (c) 2003, 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.core.internal;
 
 import java.util.*;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.*;
 import org.eclipse.debug.core.*;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.server.core.*;
 import org.eclipse.wst.server.core.model.*;
 
 /**
  * 
  */
 public class Server extends Base implements IServer {
 	/**
 	 * Server id attribute (value "server-id") of launch configurations.
 	 * This attribute is used to tag a launch configuration with the
 	 * id of the corresponding server.
 	 * 
 	 * @see ILaunchConfiguration
 	 */
 	public static final String ATTR_SERVER_ID = "server-id";
 
 	protected static final List<String> EMPTY_LIST = new ArrayList<String>(0);
 
 	/**
 	 * File extension (value "server") for serialized representation of
 	 * server instances.
 	 * <p>
 	 * [issue: What is relationship between this file extension and
 	 * the file passed to IServerType.create(...) or returned by
 	 * IServer.getFile()? That is, are server files expected to end
 	 * in ".server", or is this just a default? If the former
 	 * (as I suspect), then IServerType.create needs to say so,
 	 * and the implementation should enforce the restriction.]
 	 * </p>
 	 */
 	public static final String FILE_EXTENSION = "server";
 
 	public static final int AUTO_PUBLISH_DISABLE = 1;
 	public static final int AUTO_PUBLISH_ENABLE = 2;
 
 	protected static final String PROP_HOSTNAME = "hostname";
 	protected static final String SERVER_ID = "server-id";
 	protected static final String RUNTIME_ID = "runtime-id";
 	protected static final String CONFIGURATION_ID = "configuration-id";
 	protected static final String MODULE_LIST = "modules";
 	protected static final String PROP_DISABLED_PERFERRED_TASKS = "disabled-preferred-publish-tasks";
 	protected static final String PROP_ENABLED_OPTIONAL_TASKS = "enabled-optional-publish-tasks";
 	public static final String PROP_PUBLISHERS = "publishers";
 	public static final String PROP_AUTO_PUBLISH_TIME = "auto-publish-time";
 	public static final String PROP_AUTO_PUBLISH_SETTING = "auto-publish-setting";
 	public static final String PROP_START_TIMEOUT = "start-timeout";
 	public static final String PROP_STOP_TIMEOUT = "stop-timeout";	
 
 	protected static final char[] INVALID_CHARS = new char[] {'\\', '/', ':', '*', '?', '"', '<', '>', '|', '\0', '@', '&'};
 
 	protected IServerType serverType;
 	protected ServerDelegate delegate;
 	protected ServerBehaviourDelegate behaviourDelegate;
 
 	protected IRuntime runtime;
 	protected IFolder configuration;
 
 	// the list of modules that are to be published to the server
 	protected List<IModule> modules;
 
 	// the list of external modules
 	protected List<IModule> externalModules;
 
 	// transient fields
 	protected transient String mode = ILaunchManager.RUN_MODE;
 	protected transient int serverState = STATE_UNKNOWN;
 	protected transient int serverSyncState;
 	protected transient boolean serverRestartNeeded;
 
 	protected transient Map<String, Integer> moduleState = new HashMap<String, Integer>();
 	protected transient Map<String, Integer> modulePublishState = new HashMap<String, Integer>();
 	protected transient Map<String, Boolean> moduleRestartState = new HashMap<String, Boolean>();
 
 	protected transient IStatus serverStatus;
 	protected transient Map<String, IStatus> moduleStatus = new HashMap<String, IStatus>();
 
 	protected transient ServerPublishInfo publishInfo;
 	protected transient AutoPublishThread autoPublishThread;
 
 	/**
 	 * The most recent launch used to start the server.
 	 */
 	protected transient ILaunch launch;
 
 /*	private static final String[] stateStrings = new String[] {
 		"unknown", "starting", "started", "started_debug",
 		"stopping", "stopped", "started_unsupported", "started_profile"
 	};*/
 
 	// publish listeners
 	protected transient List<IPublishListener> publishListeners;
 
 	// server listeners
 	protected transient ServerNotificationManager notificationManager;
 
 	public class AutoPublishThread extends Thread {
 		public boolean stop;
 		public int time = 0;
 
 		public AutoPublishThread() {
 			super("Automatic Publishing");
 		}
 
 		public void run() {
 			Trace.trace(Trace.FINEST, "Auto-publish thread starting for " + Server.this + " - " + time + "s");
 			if (stop)
 				return;
 			
 			try {
 				sleep(time * 1000);
 			} catch (Exception e) {
 				// ignore
 			}
 			
 			if (stop)
 				return;
 			
 			Trace.trace(Trace.FINEST, "Auto-publish thread publishing " + Server.this);
 			
 			if (getServerState() != IServer.STATE_STARTED)
 				return;
 			
 			publish(IServer.PUBLISH_AUTO, null, null, null);
 		}
 	}
 
 	private abstract class ServerJob extends Job {
 		public ServerJob(String name) {
 			super(name);
 		}
 
 		public boolean belongsTo(Object family) {
 			return ServerUtil.SERVER_JOB_FAMILY.equals(family);
 		}
 	
 		public IServer getServer() {
 			return Server.this;
 		}
 	}
 
 	public class ResourceChangeJob extends ServerJob {
 		private IModule module;
 
 		public ResourceChangeJob(IModule module) {
 			super(NLS.bind(Messages.jobUpdateServer, Server.this.getName()));
 			this.module = module;
 			
 			if (module.getProject() == null)
 				setRule(Server.this);
 			else {
 				ISchedulingRule[] rules = new ISchedulingRule[2];
 				IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
 				rules[0] = ruleFactory.createRule(module.getProject());
 				rules[1] = Server.this;
 				setRule(MultiRule.combine(rules));
 			}
 		}
 
 		protected IModule getModule() {
 			return module;
 		}
 
 		protected IStatus run(IProgressMonitor monitor) {
 			final boolean[] changed = new boolean[1];
 			final List<IModule[]> modules2 = new ArrayList<IModule[]>();
 			
 			IModuleVisitor visitor = new IModuleVisitor() {
 				public boolean visit(IModule[] module2) {
 					modules2.add(module2);
 					
 					int size = module2.length;
 					IModule m = module2[size - 1];
 					if (m.getProject() == null)
 						return true;
 					
 					if (getModule().equals(m)) {
 						if (hasPublishedResourceDelta(module2)) {
 							changed[0] = true;
 							setModulePublishState(module2, IServer.PUBLISH_STATE_INCREMENTAL);
 						}
 					}
 					return true;
 				}
 			};
 			
 			visit(visitor, null);
 			
 			if (getServerPublishInfo().hasStructureChanged(modules2))
 				setServerPublishState(IServer.PUBLISH_STATE_INCREMENTAL);
 			
 			if (!changed[0])
 				return Status.OK_STATUS;
 			
 			if (getServerState() != IServer.STATE_STOPPED && behaviourDelegate != null)
 				behaviourDelegate.handleResourceChange();
 			
 			if (getServerState() == IServer.STATE_STARTED)
 				autoPublish();
 			
 			return Status.OK_STATUS;
 		}
 	}
 
 	public class PublishJob extends ServerJob {
 		protected int kind;
 		protected List<IModule[]> modules4;
 		protected IAdaptable info;
 		protected boolean start;
 
 		/**
 		 * Create a new publishing job.
 		 * 
 		 * @param kind the kind of publish
 		 * @param modules4 a list of modules to publish, or <code>null</code> to
 		 *    publish all modules
 		 * @param start true if we need to start the server first
 		 * @param info the IAdaptable (or <code>null</code>) provided by the
 		 *    caller in order to supply UI information for prompting the
 		 *    user if necessary. When this parameter is not
 		 *    <code>null</code>, it should minimally contain an adapter
 		 *    for the Shell class.
 		 */
 		public PublishJob(int kind, List<IModule[]> modules4, boolean start, IAdaptable info) {
 			super(NLS.bind(Messages.publishing, Server.this.getName()));
 			this.kind = kind;
 			this.modules4 = modules4;
 			this.start = start;
 			this.info = info;
 		}
 
 		protected IStatus run(IProgressMonitor monitor) {
 			if (start) {
 				try{
 					// 237222 - Apply the rules only when the job has started
 					Job.getJobManager().beginRule(Server.this, monitor);
 					IStatus status = startImpl(ILaunchManager.RUN_MODE, monitor);
 					if (status != null && status.getSeverity() == IStatus.ERROR)
 						return status;
 				}
 				finally{
 					Job.getJobManager().endRule(Server.this);
 				}
 			}
 			
 			// 102227 - lock entire workspace during publish
 			ISchedulingRule publishRule = MultiRule.combine(new ISchedulingRule[] {
 				ResourcesPlugin.getWorkspace().getRoot(), Server.this
 			});
 			
 			try{
 				// 237222 - Apply the rules only when the job has started
 				Job.getJobManager().beginRule(publishRule, monitor);	
 				return publishImpl(kind, modules4, info, monitor);
 			} finally {
 				Job.getJobManager().endRule(publishRule);
 			}
 		}
 	}
 
 	public class StartJob extends ServerJob {
 		protected static final byte PUBLISH_NONE = 0;
 		protected static final byte PUBLISH_BEFORE = 1;
 		protected static final byte PUBLISH_AFTER = 2;
 
 		protected String launchMode;
 
 		public StartJob(String launchMode) {
 			super(NLS.bind(Messages.jobStarting, Server.this.getName()));
 			this.launchMode = launchMode;
 			
 			setRule(Server.this);
 		}
 
 		protected IStatus run(IProgressMonitor monitor) {
 			IStatus stat = startImpl(launchMode, monitor);
 			return stat;
 		}
 	}
 
 	public class RestartJob extends ServerJob {
 		protected String launchMode;
 
 		public RestartJob(String launchMode) {
 			super(NLS.bind(Messages.jobRestarting, Server.this.getName()));
 			this.launchMode = launchMode;
 			setRule(Server.this);
 		}
 
 		protected IStatus run(IProgressMonitor monitor) {
 			return restartImpl(launchMode, monitor);
 		}
 	}
 
 	public class StopJob extends ServerJob {
 		protected boolean force;
 
 		public StopJob(boolean force) {
 			super(NLS.bind(Messages.jobStopping, Server.this.getName()));
 			setRule(Server.this);
 			this.force = force;
 		}
 
 		protected IStatus run(IProgressMonitor monitor) {
 			return stopImpl(force, monitor);
 		}
 	}
 
 	private static final Comparator<PublishOperation> PUBLISH_OPERATION_COMPARTOR = new Comparator<PublishOperation>() {
       public int compare(PublishOperation leftOp, PublishOperation rightOp) {
           PublishOperation left = leftOp;
           PublishOperation right = rightOp;
           if (left.getOrder() > right.getOrder())
               return 1;
           if (left.getOrder() < right.getOrder())
               return -1;
           return 0;
       }
 	};
 
 	// working copy, loaded resource
 	public Server(IFile file) {
 		super(file);
 		map.put(PROP_HOSTNAME, "localhost");
 	}
 
 	// creation (working copy)
 	public Server(String id, IFile file, IRuntime runtime, IServerType serverType) {
 		super(file, id);
 		this.runtime = runtime;
 		this.serverType = serverType;
 		map.put("server-type-id", serverType.getId());
 		map.put(PROP_HOSTNAME, "localhost");
 		map.put(PROP_START_TIMEOUT, ((ServerType)serverType).getStartTimeout()/1000 + "");
 		map.put(PROP_STOP_TIMEOUT, ((ServerType)serverType).getStopTimeout()/1000 + "");
 		if (runtime != null && runtime.getRuntimeType() != null) {
 			String name = runtime.getRuntimeType().getName();
 			map.put(PROP_NAME, name);
 		}
 		serverState = ((ServerType)serverType).getInitialState();
 	}
 	
 	public IServerType getServerType() {
 		return serverType;
 	}
 	
 	public IServerWorkingCopy createWorkingCopy() {
 		return new ServerWorkingCopy(this); 
 	}
 
 	public boolean isWorkingCopy() {
 		return false;
 	}
 
 	protected void deleteFromFile() throws CoreException {
 		super.deleteFromFile();
 		ResourceManager.getInstance().removeServer(this);
 	}
 
 	protected void deleteFromMetadata() {
 		ResourceManager.getInstance().removeServer(this);
 	}
 
 	protected void doSave(IProgressMonitor monitor) throws CoreException {
 		super.doSave(monitor);
 		fireServerChangeEvent();
 	}
 
 	protected void saveToFile(IProgressMonitor monitor) throws CoreException {
 		super.saveToFile(monitor);
 		ResourceManager.getInstance().addServer(this);
 	}
 
 	protected void saveToMetadata(IProgressMonitor monitor) {
 		super.saveToMetadata(monitor);
 		ResourceManager.getInstance().addServer(this);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.core.IServerAttributes#getRuntime()
 	 */
 	public IRuntime getRuntime() {
 		return runtime;
 	}
 
 	protected String getRuntimeId() {
 		return getAttribute(RUNTIME_ID, (String) null);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.core.IServerAttributes#getServerConfiguration()
 	 */
 	public IFolder getServerConfiguration() {
 		return configuration;
 	}
 
 	protected ServerDelegate getDelegate(IProgressMonitor monitor) {
 		if (delegate != null || serverType == null)
 			return delegate;
 		
 		synchronized (this) {
 			if (delegate == null) {
 				try {
 					long time = System.currentTimeMillis();
 					delegate = ((ServerType) serverType).createServerDelegate();
 					if (delegate != null)
 						InternalInitializer.initializeServerDelegate(delegate, Server.this, monitor);
 					Trace.trace(Trace.PERFORMANCE, "Server.getDelegate(): <" + (System.currentTimeMillis() - time) + "> " + getServerType().getId());
 				} catch (Throwable t) {
 					ServerPlugin.logExtensionFailure(toString(), t);
 				}
 			}
 		}
 		return delegate;
 	}
 
 	protected ServerBehaviourDelegate getBehaviourDelegate(IProgressMonitor monitor) {
 		if (behaviourDelegate != null || serverType == null)
 			return behaviourDelegate;
 		
 		synchronized (moduleState) {
 			if (behaviourDelegate == null) {
 				try {
 					long time = System.currentTimeMillis();
 					behaviourDelegate = ((ServerType) serverType).createServerBehaviourDelegate();
 					if (behaviourDelegate != null)
 						InternalInitializer.initializeServerBehaviourDelegate(behaviourDelegate, Server.this, monitor);
 					Trace.trace(Trace.PERFORMANCE, "Server.getBehaviourDelegate(): <" + (System.currentTimeMillis() - time) + "> " + getServerType().getId());
 					
 					if (getServerState() == IServer.STATE_STARTED)
 						autoPublish();
 				} catch (Throwable t) {
 					ServerPlugin.logExtensionFailure(toString(), t);
 				}
 			}
 		}
 		return behaviourDelegate;
 	}
 
 	public void dispose() {
 		if (delegate != null) {
 			delegate.dispose();
 			delegate = null;
 		}
 		if (behaviourDelegate != null) {
 			behaviourDelegate.dispose();
 			behaviourDelegate = null;
 		}
 	}
 
 	public String getHost() {
 		return getAttribute(PROP_HOSTNAME, "localhost");
 	}
 
 	public int getAutoPublishTime() {
 		return getAttribute(PROP_AUTO_PUBLISH_TIME, 15);
 	}
 
 	public int getAutoPublishSetting() {
 		return getAttribute(PROP_AUTO_PUBLISH_SETTING, AUTO_PUBLISH_ENABLE);
 	}
 
 	public int getStartTimeout() {
 		return getAttribute(PROP_START_TIMEOUT, ((ServerType)getServerType()).getStartTimeout()/1000);
 	}
 
 	public int getStopTimeout() {
 		return getAttribute(PROP_STOP_TIMEOUT, ((ServerType)getServerType()).getStopTimeout()/1000);
 	}
 
 	/**
     * Returns a list of id (String) of preferred publish operations that will not be run
     * during publish.
     * 
     * @return a list of publish operation ids
     */
 	public List<String> getDisabledPreferredPublishOperationIds() {
 		return getAttribute(PROP_DISABLED_PERFERRED_TASKS, EMPTY_LIST);		
 	}
 
 	/**
     * Returns a list of id (String) of optional publish operations that are enabled to 
     * be run during publish.
     * 
     * @return a list of publish operation ids
     */
 	public List<String> getEnabledOptionalPublishOperationIds() {
 		return getAttribute(PROP_ENABLED_OPTIONAL_TASKS, EMPTY_LIST);
 	}
 
 	/**
     * Returns a list of id (String) of publishers and state (true or false).
     * 
     * @return a list of publishers
     */
 	public List<String> getPublisherIds() {
 		return getAttribute(PROP_PUBLISHERS, EMPTY_LIST);		
 	}
 
 	public boolean isPublisherEnabled(Publisher pub) {
 		if (pub == null)
 			return false;
 		
 		// check for existing enablement
 		List<String> list = getAttribute(PROP_PUBLISHERS, EMPTY_LIST);
 		Iterator<String> iter = list.iterator();
 		while (iter.hasNext()) {
 			String id = iter.next();
 			int ind = id.indexOf(":");
 			boolean enabled = false;
 			if ("true".equals(id.substring(ind+1)))
 				enabled = true;
 			id = id.substring(0, ind);
 			if (pub.getId().equals(id))
 				return enabled;
 		}
 		
 		// otherwise use the default enablement
 		return true;
 	}
 
 	/**
 	 * Returns the current state of the server. (see SERVER_XXX constants)
 	 *
 	 * @return int
 	 */
 	public int getServerState() {
 		return serverState;
 	}
 
 	public String getMode() {
 		return mode;
 	}
 
 	public void setServerState(int state) {
 		if (state == serverState)
 			return;
 		
 		// ensure that any server monitors are started
 		if (state == IServer.STATE_STARTED)
 			ServerMonitorManager.getInstance();
 		
 		this.serverState = state;
 		fireServerStateChangeEvent();
 	}
 
 	/**
 	 * Add a listener to this server.
 	 *
 	 * @param listener org.eclipse.wst.server.model.IServerListener
 	 */
 	public void addServerListener(IServerListener listener) {
 		if (listener == null)
 			throw new IllegalArgumentException("Module cannot be null");
 		Trace.trace(Trace.LISTENERS, "Adding server listener " + listener + " to " + this);
 		getServerNotificationManager().addListener(listener);
 	}
 
 	/**
 	 * Add a listener to this server with the given event mask.
 	 *
 	 * @param listener org.eclipse.wst.server.model.IServerListener
 	 * @param eventMask to limit listening to certain types of events
 	 */
 	public void addServerListener(IServerListener listener, int eventMask) {
 		if (listener == null)
 			throw new IllegalArgumentException("Module cannot be null");
 		Trace.trace(Trace.LISTENERS, "Adding server listener " + listener + " to " + this + " with eventMask " + eventMask);
 		getServerNotificationManager().addListener(listener, eventMask);
 	}
 
 	/**
 	 * Remove a listener from this server.
 	 *
 	 * @param listener org.eclipse.wst.server.model.IServerListener
 	 */
 	public void removeServerListener(IServerListener listener) {
 		if (listener == null)
 			throw new IllegalArgumentException("Module cannot be null");
 		Trace.trace(Trace.LISTENERS, "Removing server listener " + listener + " from " + this);
 		getServerNotificationManager().removeListener(listener);
 	}
 
 	/**
 	 * Fire a server listener restart state change event.
 	 */
 	protected void fireRestartStateChangeEvent() {
 		Trace.trace(Trace.LISTENERS, "->- Firing server restart change event: " + getName() + " ->-");
 		
 		if (notificationManager == null || notificationManager.hasNoListeners())
 			return;
 		
 		notificationManager.broadcastChange(
 			new ServerEvent(ServerEvent.SERVER_CHANGE | ServerEvent.RESTART_STATE_CHANGE, this, getServerState(), 
 				getServerPublishState(), getServerRestartState()));
 	}
 
 	/**
 	 * Fire a server listener state change event.
 	 */
 	protected void fireServerStateChangeEvent() {
 		Trace.trace(Trace.LISTENERS, "->- Firing server state change event: " + getName() + ", " + getServerState() + " ->-");
 		
 		if (notificationManager == null || notificationManager.hasNoListeners())
 			return;
 		
 		notificationManager.broadcastChange(
 			new ServerEvent(ServerEvent.SERVER_CHANGE | ServerEvent.STATE_CHANGE, this, getServerState(), 
 				getServerPublishState(), getServerRestartState()));
 	}
 
 	/**
 	 * Fire a server listener change event.
 	 */
 	protected void fireServerChangeEvent() {
 		Trace.trace(Trace.LISTENERS, "->- Firing server change event: " + getName() + ", " + getServerState() + " ->-");
 		
 		if (notificationManager == null || notificationManager.hasNoListeners())
 			return;
 		
 		notificationManager.broadcastChange(
 			new ServerEvent(ServerEvent.SERVER_CHANGE, this, getServerState(), 
 				getServerPublishState(), getServerRestartState()));
 	}
 
 	/**
 	 * Fire a server listener module state change event.
 	 */
 	protected void fireModuleStateChangeEvent(IModule[] module) {
 		Trace.trace(Trace.LISTENERS, "->- Firing module state change event: " + getName() + ", " + getServerState() + " ->-");
 		
 		if (notificationManager == null || notificationManager.hasNoListeners())
 			return;
 		
 		notificationManager.broadcastChange(
 			new ServerEvent(ServerEvent.MODULE_CHANGE | ServerEvent.STATE_CHANGE, this, module, getModuleState(module), 
 				getModulePublishState(module), getModuleRestartState(module)));
 	}
 
 	/**
 	 * Fire a server listener module publish state change event.
 	 */
 	protected void fireModulePublishStateChangeEvent(IModule[] module) {
 		Trace.trace(Trace.LISTENERS, "->- Firing module publish state change event: " + getName() + ", " + getServerState() + " ->-");
 		
 		if (notificationManager == null || notificationManager.hasNoListeners())
 			return;
 		
 		notificationManager.broadcastChange(
 			new ServerEvent(ServerEvent.MODULE_CHANGE | ServerEvent.PUBLISH_STATE_CHANGE, this, module, getModuleState(module), 
 				getModulePublishState(module), getModuleRestartState(module)));
 	}
 
 	/**
 	 * Fire a server listener module state change event.
 	 */
 	protected void fireModuleRestartChangeEvent(IModule[] module) {
 		Trace.trace(Trace.LISTENERS, "->- Firing module restart change event: " + getName() + ", " + getServerState() + " ->-");
 		
 		if (notificationManager == null || notificationManager.hasNoListeners())
 			return;
 		
 		notificationManager.broadcastChange(
 			new ServerEvent(ServerEvent.MODULE_CHANGE | ServerEvent.RESTART_STATE_CHANGE, this, module, getModuleState(module), 
 				getModulePublishState(module), getModuleRestartState(module)));
 	}
 
 	public void setMode(String m) {
 		if (m == mode)
 			return;
 
 		this.mode = m;
 		fireServerStateChangeEvent();
 	}
 
 	public void setModuleState(IModule[] module, int state) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		int oldState = getModuleState(module);
 		if (oldState == state)
 			return;
 		
 		Integer in = new Integer(state);
 		moduleState.put(getKey(module), in);
 		fireModuleStateChangeEvent(module);
 	}
 
 	public void setModulePublishState(IModule[] module, int state) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		int oldState = getModulePublishState(module);
 		if (oldState == state)
 			return;
 		
 		Integer in = new Integer(state);
 		if (state == -1)
 			modulePublishState.remove(getKey(module));
 		modulePublishState.put(getKey(module), in);
 		fireModulePublishStateChangeEvent(module);
 	}
 
 	public void setModuleRestartState(IModule[] module, boolean r) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		boolean oldState = getModuleRestartState(module);
 		if (oldState == r)
 			return;
 		
 		Boolean b = new Boolean(r);
 		moduleRestartState.put(getKey(module), b);
 		fireModuleRestartChangeEvent(module);
 	}
 
 	public void setExternalModules(IModule[] modules) {
 		externalModules = new ArrayList<IModule>();
 		if (modules != null) {
 			int size = modules.length;
 			for (int i = 0; i < size; i++)
 				externalModules.add(modules[i]);
 		}
 	}
 
 	protected List<IModule> getExternalModules() {
 		return externalModules;
  	}
 
 	protected void handleModuleProjectChange(IModule module) {
 		Trace.trace(Trace.FINEST, "> handleDeployableProjectChange() " + this + " " + module);
 		
 		// check for duplicate jobs already waiting and don't create a new one
 		Job[] jobs = Job.getJobManager().find(ServerUtil.SERVER_JOB_FAMILY);
 		if (jobs != null) {
 			int size = jobs.length;
 			for (int i = 0; i < size; i++) {
 				if (jobs[i] instanceof ResourceChangeJob) {
 					ResourceChangeJob rcj = (ResourceChangeJob) jobs[i];
 					if (rcj.getServer().equals(this) && rcj.getModule().equals(module) && rcj.getState() == Job.WAITING)
 						return;
 				}
 			}
 		}
 		
 		ResourceChangeJob job = new ResourceChangeJob(module);
 		job.setSystem(true);
 		job.setPriority(Job.BUILD);
 		job.schedule();
 		
 		Trace.trace(Trace.FINEST, "< handleDeployableProjectChange()");
 	}
 
 	protected void stopAutoPublish() {
 		if (autoPublishThread == null)
 			return;
 		
 		autoPublishThread.stop = true;
 		autoPublishThread.interrupt();
 		autoPublishThread = null;
 	}
 
 	/**
 	 * Reset automatic publish thread if it is running and start a new
 	 * thread if automatic publishing is currently enabled.
 	 */
 	protected void autoPublish() {
 		stopAutoPublish();
 		
 		if (getAutoPublishSetting() == AUTO_PUBLISH_DISABLE)
 			return;
 		
 		int time = getAutoPublishTime();
 		if (time >= 0) {
 			autoPublishThread = new AutoPublishThread();
 			autoPublishThread.time = time;
 			autoPublishThread.setPriority(Thread.MIN_PRIORITY + 1);
 			autoPublishThread.setDaemon(true);
 			autoPublishThread.start();
 		}
 	}
 
 	/**
 	 * Returns the event notification manager.
 	 * 
 	 * @return the notification manager
 	 */
 	private ServerNotificationManager getServerNotificationManager() {
 		if (notificationManager == null)
 			notificationManager = new ServerNotificationManager();
 		
 		return notificationManager;
 	}
 
 	/**
 	 * Returns the server's publish sync state.
 	 *
 	 * @return int
 	 */
 	public int getServerPublishState() {
 		return serverSyncState;
 	}
 
 	/**
 	 * Sets the server's publish sync state.
 	 *
 	 * @param state int
 	 */
 	public void setServerPublishState(int state) {
 		if (state == serverSyncState)
 			return;
 		serverSyncState = state;
 		firePublishStateChange();
 	}
 
 	/**
 	 * Adds a publish listener to this server.
 	 * Has no effect if an identical listener is already registered.
 	 *
 	 * @param listener the publish listener
 	 * @see #removePublishListener(IPublishListener)
 	 */
 	public void addPublishListener(IPublishListener listener) {
 		if (listener == null)
 			throw new IllegalArgumentException("Listener cannot be null");
 		Trace.trace(Trace.LISTENERS, "Adding publish listener " + listener + " to " + this);
 		
 		if (publishListeners == null)
 			publishListeners = new ArrayList<IPublishListener>();
 		publishListeners.add(listener);
 	}
 
 	/**
 	 * Removes a publish listener from this server.
 	 * Has no effect if the listener is not registered.
 	 *
 	 * @param listener the publish listener
 	 * @see #addPublishListener(IPublishListener)
 	 */
 	public void removePublishListener(IPublishListener listener) {
 		if (listener == null)
 			throw new IllegalArgumentException("Listener cannot be null");
 		Trace.trace(Trace.LISTENERS, "Removing publish listener " + listener + " from " + this);
 
 		if (publishListeners != null)
 			publishListeners.remove(listener);
 	}
 	
 	/**
 	 * Fire a publish start event.
 	 */
 	protected void firePublishStarted() {
 		Trace.trace(Trace.FINEST, "->- Firing publish started event ->-");
 	
 		if (publishListeners == null || publishListeners.isEmpty())
 			return;
 
 		int size = publishListeners.size();
 		IPublishListener[] srl = new IPublishListener[size];
 		publishListeners.toArray(srl);
 
 		for (int i = 0; i < size; i++) {
 			Trace.trace(Trace.FINEST, "  Firing publish started event to " + srl[i]);
 			try {
 				srl[i].publishStarted(this);
 			} catch (Exception e) {
 				Trace.trace(Trace.SEVERE, "  Error firing publish started event to " + srl[i], e);
 			}
 		}
 
 		Trace.trace(Trace.FINEST, "-<- Done firing publish started event -<-");
 	}
 
 	/**
 	 * Fire a publish stop event.
 	 *
 	 * @param status publishing status
 	 */
 	protected void firePublishFinished(IStatus status) {
 		Trace.trace(Trace.FINEST, "->- Firing publishing finished event: " + status + " ->-");
 	
 		if (publishListeners == null || publishListeners.isEmpty())
 			return;
 
 		int size = publishListeners.size();
 		IPublishListener[] srl = new IPublishListener[size];
 		publishListeners.toArray(srl);
 
 		for (int i = 0; i < size; i++) {
 			Trace.trace(Trace.FINEST, "  Firing publishing finished event to " + srl[i]);
 			try {
 				srl[i].publishFinished(this, status);
 			} catch (Exception e) {
 				Trace.trace(Trace.SEVERE, "  Error firing publishing finished event to " + srl[i], e);
 			}
 		}
 
 		Trace.trace(Trace.FINEST, "-<- Done firing publishing finished event -<-");
 	}
 
 	/**
 	 * Fire a publish state change event.
 	 */
 	protected void firePublishStateChange() {
 		Trace.trace(Trace.FINEST, "->- Firing publish state change event ->-");
 		
 		if (notificationManager == null || notificationManager.hasNoListeners())
 			return;
 		
 		notificationManager.broadcastChange(
 			new ServerEvent(ServerEvent.SERVER_CHANGE | ServerEvent.PUBLISH_STATE_CHANGE, this, getServerState(), 
 					getServerPublishState(), getServerRestartState()));
 	}
 
 	/**
 	 * Fire a publish state change event.
 	 */
 	protected void firePublishStateChange(IModule[] module) {
 		Trace.trace(Trace.FINEST, "->- Firing publish state change event: " + module + " ->-");
 	
 		if (notificationManager == null || notificationManager.hasNoListeners())
 			return;
 	
 		notificationManager.broadcastChange(
 			new ServerEvent(ServerEvent.MODULE_CHANGE | ServerEvent.PUBLISH_STATE_CHANGE, this, module, getModuleState(module), 
 				getModulePublishState(module), getModuleRestartState(module)));
 	}
 
 	/**
 	 * Returns true if the server is in a state that it can
 	 * be published to.
 	 *
 	 * @return boolean
 	 */
 	public IStatus canPublish() {
 		if (getServerType() == null)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorMissingAdapter, null);
 		
 		// can't publish if the server is starting or stopping
 		int state = getServerState();
 		if (state == STATE_STARTING || state == STATE_STOPPING)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorPublishStarting, null);
 		
 		// can't publish if there is no configuration
 		if (getServerType().hasServerConfiguration() && configuration == null)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorPublishNoConfiguration, null);
 		
 		if (behaviourDelegate != null)
 			return behaviourDelegate.canPublish();
 		
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * Returns true if the server should be published to. This is <code>true</code> when the server
 	 * can be published to and the server's publish state or any module's publish state is not
 	 * PUBLISH_STATE_NONE. 
 	 * 
 	 * @return boolean
 	 */
 	public boolean shouldPublish() {
 		if (!canPublish().isOK())
 			return false;
 		
 		if (getServerPublishState() != PUBLISH_STATE_NONE)
 			return true;
 		
 		final boolean[] publish = new boolean[1];
 		
 		visit(new IModuleVisitor() {
 			public boolean visit(IModule[] module) {
 				if (getModulePublishState(module) != PUBLISH_STATE_NONE) {
 					publish[0] = true;
 					return false;
 				}
 				return true;
 			}
 		}, null);
 		
 		return publish[0];
 	}
 
 	public boolean isPublishUnknown() {
 		if (getServerPublishState() != PUBLISH_STATE_UNKNOWN)
 			return false;
 		
 		final boolean[] publish = new boolean[1];
 		publish[0] = true;
 		
 		visit(new IModuleVisitor() {
 			public boolean visit(IModule[] module) {
 				if (getModulePublishState(module) != PUBLISH_STATE_UNKNOWN) {
 					publish[0] = false;
 					return false;
 				}
 				return true;
 			}
 		}, null);
 		
 		return publish[0];
 	}
 
 	/**
 	 * Returns true if the server should be restarted. This is <code>true</code> when the server
 	 * can be restarted and the server's restart state or any module's restart states is not
 	 * false. 
 	 * 
 	 * @return boolean
 	 */
 	public boolean shouldRestart() {
 		if (!canPublish().isOK())
 			return false;
 		
 		if (getServerRestartState())
 			return true;
 		
 		final boolean[] publish = new boolean[1];
 		
 		visit(new IModuleVisitor() {
 			public boolean visit(IModule[] module) {
 				if (getModuleRestartState(module)) {
 					publish[0] = true;
 					return false;
 				}
 				return true;
 			}
 		}, null);
 		
 		return publish[0];
 	}
 
 	public ServerPublishInfo getServerPublishInfo() {
 		if (publishInfo == null) {
 			publishInfo = PublishInfo.getInstance().getServerPublishInfo(this);
 		}
 		return publishInfo;
 	}
 
 	/*
 	 * Publish to the server using the progress monitor. The result of the
 	 * publish operation is returned as an IStatus.
 	 */
 	public IStatus publish(int kind, IProgressMonitor monitor) {
 		if (getServerType() == null)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorMissingAdapter, null);
 		
 		// check what is out of sync and publish
 		if (getServerType().hasServerConfiguration() && configuration == null)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorNoConfiguration, null);
 		
 		// make sure that the delegate is loaded and the server state is correct
 		loadAdapter(ServerBehaviourDelegate.class, monitor);
 		
 		if (((ServerType)getServerType()).startBeforePublish() && (getServerState() == IServer.STATE_STOPPED)) {
 			IStatus status = startImpl(ILaunchManager.RUN_MODE, monitor);
 			if (status != null && status.getSeverity() == IStatus.ERROR)
 				return status;
 		}
 		
 		return publishImpl(kind, null, null, monitor);
 	}
 
 	/*
 	 * Publish the given modules to the server.
 	 * TODO: Implementation of publishing individual modules!
 	 */
 	public void publish(final int kind, final List<IModule[]> modules2, final IAdaptable info, final IOperationListener opListener) {
 		if (getServerType() == null) {
 			if (opListener != null)
 				opListener.done(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorMissingAdapter, null));
 			return;
 		}
 		
 		// check what is out of sync and publish
 		if (getServerType().hasServerConfiguration() && configuration == null) {
 			if (opListener != null)
 				opListener.done(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorNoConfiguration, null));
 			return;
 		}
 		
 		// make sure that the delegate is loaded and the server state is correct
 		loadAdapter(ServerBehaviourDelegate.class, null);
 		
 		boolean start = false;
 		if (((ServerType)getServerType()).startBeforePublish() && (getServerState() == IServer.STATE_STOPPED))
 			start = true;
 		
 		PublishJob publishJob = new PublishJob(kind, modules2, start, info);
 		if (opListener != null) {
 			publishJob.addJobChangeListener(new JobChangeAdapter() {
 				public void done(IJobChangeEvent event) {
 					opListener.done(event.getResult());
 				}
 			});
 		}
 		
 		// to make the publishing dialog appear, require adaptable to covert String.class
 		// into "user" string. a bit of a kludge, but works fine for now
 		if (info != null && "user".equals(info.getAdapter(String.class)))
 			publishJob.setUser(true);
 		publishJob.schedule();
 	}
 
 	/**
 	 * Returns the publish tasks that have been targeted to this server.
 	 * These tasks should be run during publishing and will be initialized
 	 * with a task model.
 	 * 
 	 * @param kind one of the IServer.PUBLISH_XX constants
 	 * @param moduleList a list of modules
 	 * @param kindList one of the IServer publish change constants
 	 * @return a possibly empty array of IOptionalTasks
 	 */
 	public PublishOperation[] getTasks(int kind, List moduleList, List kindList) {
 		List<PublishOperation> tasks = new ArrayList<PublishOperation>();
 		
 		String serverTypeId = getServerType().getId();
 		
 		IPublishTask[] publishTasks = ServerPlugin.getPublishTasks();
 		if (publishTasks != null) {
 			List enabledTasks = getEnabledOptionalPublishOperationIds();
 			List disabledTasks = getDisabledPreferredPublishOperationIds();
 			
 			TaskModel taskModel = new TaskModel();
 			taskModel.putObject(TaskModel.TASK_SERVER, this);
 			
 			int size = publishTasks.length;
 			for (int i = 0; i < size; i++) {
 				IPublishTask task = publishTasks[i];
 				if (task.supportsType(serverTypeId)) {
 					PublishOperation[] tasks2 = task.getTasks(this, kind, moduleList, kindList);
 					if (tasks2 != null) {
 						int size2 = tasks2.length;
 						for (int j = 0; j < size2; j++) {
 							if (tasks2[j].getKind() == PublishOperation.REQUIRED) {
 								tasks.add(tasks2[j]);
 								tasks2[j].setTaskModel(taskModel);
 							} else if (tasks2[j].getKind() == PublishOperation.PREFERRED) {
 								String opId = getPublishOperationId(tasks2[j]);
 								if (!disabledTasks.contains(opId)) {
 									tasks.add(tasks2[j]);
 									tasks2[j].setTaskModel(taskModel);
 								}
 							} else if (tasks2[j].getKind() == PublishOperation.OPTIONAL) {
 								String opId = getPublishOperationId(tasks2[j]);
 								if (enabledTasks.contains(opId)) {
 									tasks.add(tasks2[j]);
 									tasks2[j].setTaskModel(taskModel);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		Collections.sort(tasks, PUBLISH_OPERATION_COMPARTOR);
 		
 		return tasks.toArray(new PublishOperation[tasks.size()]);
 	}
 
 	/**
 	 * Returns all the publishers that are available with this server,
 	 * whether or not they are currently enabled.
 	 * 
 	 * @return a possibly empty array of Publishers
 	 */
 	public Publisher[] getAllPublishers() {
 		List<Publisher> pubs = new ArrayList<Publisher>();
 		
 		String serverTypeId = getServerType().getId();
 		
 		TaskModel taskModel = new TaskModel();
 		taskModel.putObject(TaskModel.TASK_SERVER, this);
 		
 		Publisher[] publishers = ServerPlugin.getPublishers();
 		if (publishers != null) {
 			int size = publishers.length;
 			for (int i = 0; i < size; i++) {
 				Publisher pub = publishers[i];
 				if (pub.supportsType(serverTypeId)) {
 					pub.setTaskModel(taskModel);
 					pubs.add(pub);
 				}
 			}
 		}
 		
 		return pubs.toArray(new Publisher[pubs.size()]);
 	}
 
 	/**
 	 * Returns the publishers that have been targeted to this server and
 	 * are enabled.
 	 * 
 	 * @return a possibly empty array of Publishers
 	 */
 	public Publisher[] getEnabledPublishers() {
 		List<Publisher> pubs = new ArrayList<Publisher>();
 		
 		String serverTypeId = getServerType().getId();
 		
 		TaskModel taskModel = new TaskModel();
 		taskModel.putObject(TaskModel.TASK_SERVER, this);
 		
 		Publisher[] publishers = ServerPlugin.getPublishers();
 		if (publishers != null) {
 			int size = publishers.length;
 			for (int i = 0; i < size; i++) {
 				Publisher pub = publishers[i];
 				if (pub.supportsType(serverTypeId) && isPublisherEnabled(pub)) {
 					pub.setTaskModel(taskModel);
 					pubs.add(pub);
 				}
 			}
 		}
 		
 		return pubs.toArray(new Publisher[pubs.size()]);
 	}
 
 	/**
 	 * Returns all publish tasks that have been targeted to this server type.
 	 * The tasks will not be initialized with a task model. 
 	 * 
 	 * @param moduleList a list of modules
 	 * @return an array of publish operations
 	 */
 	public PublishOperation[] getAllTasks(List moduleList) {
 		String serverTypeId = getServerType().getId();
 		if (serverTypeId == null)
 			return new PublishOperation[0];
 		
 		List<PublishOperation> tasks = new ArrayList<PublishOperation>();
 		
 		IPublishTask[] publishTasks = ServerPlugin.getPublishTasks();
 		if (publishTasks != null) {
 			int size = publishTasks.length;
 			for (int i = 0; i < size; i++) {
 				IPublishTask task = publishTasks[i];
 				if (task.supportsType(serverTypeId)) {
 					PublishOperation[] tasks2 = task.getTasks(this, moduleList);
 					tasks.addAll(Arrays.asList(tasks2));
 				}
 			}
 		}
 		
 		Collections.sort(tasks, PUBLISH_OPERATION_COMPARTOR);
 		
 		return tasks.toArray(new PublishOperation[tasks.size()]);
 	}
 
 	public String getPublishOperationId(PublishOperation op) {
 		return getId()+"|"+op.getLabel();
 	}
 
 	/**
 	 * Returns a list containing module arrays of every module on the
 	 * server.
 	 * 
 	 * @return a list of IModule[]
 	 */
 	public List<IModule[]> getAllModules() {
 		final List<IModule[]> moduleList = new ArrayList<IModule[]>();
 		
 		IModuleVisitor visitor = new IModuleVisitor() {
 			public boolean visit(IModule[] module) {
 				if (!moduleList.contains(module))
 					moduleList.add(module);
 				return true;
 			}
 		};
 		
 		visit(visitor, null);
 		
 		return moduleList;
 	}
 
 	/*
 	 * Returns the module resources that have been published.
 	 * 
 	 * @see ServerBehaviourDelegate.getPublishedResources(IModule[])
 	 */
 	public IModuleResource[] getResources(IModule[] module) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		return getServerPublishInfo().getResources(module);
 	}
 
 	/*
 	 * Returns the module resources that have been published.
 	 * 
 	 * @see ServerBehaviourDelegate.getPublishedResources(IModule[])
 	 */
 	public IModuleResource[] getPublishedResources(IModule[] module) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		return getServerPublishInfo().getModulePublishInfo(module).getResources();
 	}
 
 	/*
 	 * Returns the delta of the current module resources that have been
 	 * published compared to the current state of the module.
 	 * 
 	 * @see ServerBehaviourDelegate.getPublishedResourceDelta(IModule[])
 	 */
 	public IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		return getServerPublishInfo().getDelta(module);
 	}
 
 	/*
 	 * Returns the delta of the current module resources that have been
 	 * published compared to the current state of the module.
 	 * 
 	 * @see ServerBehaviourDelegate.getPublishedResourceDelta(IModule[])
 	 */
 	public boolean hasPublishedResourceDelta(IModule[] module) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		return getServerPublishInfo().hasDelta(module);
 	}
 
 	/**
 	 * @see IServer#getAdapter(Class)
 	 */
 	public Object getAdapter(Class adapter) {
 		if (delegate != null) {
 			if (adapter.isInstance(delegate))
 				return delegate;
 		}
 		if (behaviourDelegate != null) {
 			if (adapter.isInstance(behaviourDelegate))
 				return behaviourDelegate;
 		}
 		return Platform.getAdapterManager().getAdapter(this, adapter);
 	}
 
 	/**
 	 * @see IServer#loadAdapter(Class, IProgressMonitor)
 	 */
 	public Object loadAdapter(Class adapter, IProgressMonitor monitor) {
 		getDelegate(monitor);
 		if (adapter.isInstance(delegate))
 			return delegate;
 		
 		getBehaviourDelegate(monitor);
 		if (adapter.isInstance(behaviourDelegate))
 			return behaviourDelegate;
 		
 		return Platform.getAdapterManager().loadAdapter(this, adapter.getName());
 	}
 
 	/**
 	 * Returns true if the server is in a state that it can
 	 * be started, and supports the given mode.
 	 *
 	 * @param mode2
 	 * @return status
 	 */
 	public IStatus canStart(String mode2) {
 		if (getServerType() == null)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorMissingAdapter, null);
 		
 		int state = getServerState();
 		if (state != STATE_STOPPED && state != STATE_UNKNOWN)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.canStartErrorState, null);
 		
 		if (!getServerType().supportsLaunchMode(mode2))
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorLaunchMode, null);
 		
 		if (behaviourDelegate != null)
 			return behaviourDelegate.canStart(mode2);
 		
 		return Status.OK_STATUS;
 	}
 
 	public ILaunch getLaunch() {
 		if (launch != null && !launch.isTerminated())
 			return launch;
 		return null;
 	}
 
 	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) {
 		try {
 			getBehaviourDelegate(monitor).setupLaunchConfiguration(workingCopy, monitor);
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error calling delegate setupLaunchConfiguration() " + toString(), e);
 		}
 	}
 
 	/**
 	 * Return the launch configuration for this server. If one does not exist, it
 	 * will be created if "create" is true, and otherwise will return <code>null</code>.
 	 * Will return <code>null</code> if this server type has no associated launch
 	 * configuration type (i.e. the server cannot be started).
 	 * 
 	 * @param create <code>true</code> if a new launch configuration should be
 	 *    created if there are none already
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @return the launch configuration, or <code>null</code> if there was no
 	 *    existing launch configuration and <code>create</code> was false
 	 * @throws CoreException
 	 */
 	public ILaunchConfiguration getLaunchConfiguration(boolean create, IProgressMonitor monitor) throws CoreException {
 		if (getServerType() == null)
 			return null;
 		
 		ILaunchConfigurationType launchConfigType = ((ServerType) getServerType()).getLaunchConfigurationType();
 		if (launchConfigType == null)
 			return null;
 		
 		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
 		ILaunchConfiguration[] launchConfigs = null;
 		try {
 			launchConfigs = launchManager.getLaunchConfigurations(launchConfigType);
 		} catch (CoreException e) {
 			// ignore
 		}
 		
 		if (launchConfigs != null) {
 			int size = launchConfigs.length;
 			for (int i = 0; i < size; i++) {
 				try {
 					String serverId = launchConfigs[i].getAttribute(SERVER_ID, (String) null);
 					if (getId().equals(serverId)) {
 						final ILaunchConfigurationWorkingCopy wc = launchConfigs[i].getWorkingCopy();
 						setupLaunchConfiguration(wc, monitor);
 						if (wc.isDirty()) {
 							final ILaunchConfiguration[] lc = new ILaunchConfiguration[1];
 							Job job = new Job("Saving launch configuration") {
 								protected IStatus run(IProgressMonitor monitor2) {
 									try {
 										lc[0] = wc.doSave();
 									} catch (CoreException ce) {
 										Trace.trace(Trace.SEVERE, "Error configuring launch", ce);
 									}
 									return Status.OK_STATUS;
 								}
 							};
 							job.setSystem(true);
 							job.schedule();
 							try {
 								job.join();
 							} catch (Exception e) {
 								Trace.trace(Trace.SEVERE, "Error configuring launch", e);
 							}
 							if (job.getState() != Job.NONE) {
 								job.cancel();
 								lc[0] = wc.doSave();
 							}
 							
 							return lc[0];
 						}
 						return launchConfigs[i];
 					}
 				} catch (CoreException e) {
 					Trace.trace(Trace.SEVERE, "Error configuring launch", e);
 				}
 			}
 		}
 		
 		if (!create)
 			return null;
 		
 		// create a new launch configuration
 		String launchName = getValidLaunchConfigurationName(getName());
 		launchName = launchManager.generateUniqueLaunchConfigurationNameFrom(launchName); 
 		ILaunchConfigurationWorkingCopy wc = launchConfigType.newInstance(null, launchName);
 		wc.setAttribute(SERVER_ID, getId());
 		setupLaunchConfiguration(wc, monitor);
 		return wc.doSave();
 	}
 
 	protected String getValidLaunchConfigurationName(String s) {
 		if (s == null || s.length() == 0)
 			return "1";
 		int size = INVALID_CHARS.length;
 		for (int i = 0; i < size; i++) {
 			s = s.replace(INVALID_CHARS[i], '_');
 		}
 		return s;
 	}
 
 	/**
 	 * Clean up any metadata associated with the server, typically in preparation for
 	 * deletion.
 	 */
 	protected void deleteMetadata() {
 		deleteLaunchConfigurations();
 		ServerPlugin.getInstance().removeTempDirectory(getId());
 		PublishInfo.getInstance().removeServerPublishInfo(this);
 	}
 
 	/**
 	 * Clean up any old launch configurations with the current server's id.
 	 */
 	protected void deleteLaunchConfigurations() {
 		if (getServerType() == null)
 			return;
 		
 		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
 		ILaunchConfigurationType launchConfigType = ((ServerType) getServerType()).getLaunchConfigurationType();
 		
 		ILaunchConfiguration[] configs = null;
 		try {
 			configs = launchManager.getLaunchConfigurations(launchConfigType);
 			int size = configs.length;
 			for (int i = 0; i < size; i++) {
 				try {
 					if (getId().equals(configs[i].getAttribute(SERVER_ID, (String) null)))
 						configs[i].delete();
 				} catch (Exception e) {
 					// ignore
 				}
 			}
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 
 	/**
 	 * @see IServer#canRestart(String)
 	 */
 	public IStatus canRestart(String mode2) {
 		if (getServerType() == null)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorMissingAdapter, null);
 		
 		if (!getServerType().supportsLaunchMode(mode2))
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorLaunchMode, null);
 		
 		if (getServerState() != STATE_STARTED)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorRestartNotStarted, null);
 		
 		if (behaviourDelegate != null)
 			return behaviourDelegate.canRestart(mode2);
 		
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * Returns the current restart state of the server. This
 	 * implementation will always return false when the server
 	 * is stopped.
 	 *
 	 * @return boolean
 	 */
 	public boolean getServerRestartState() {
 		if (getServerState() == STATE_STOPPED)
 			return false;
 		return serverRestartNeeded;
 	}
 
 	/**
 	 * Sets the server restart state.
 	 *
 	 * @param state boolean
 	 */
 	public synchronized void setServerRestartState(boolean state) {
 		if (state == serverRestartNeeded)
 			return;
 		serverRestartNeeded = state;
 		fireRestartStateChangeEvent();
 	}
 
 	/**
 	 * @see IServer#restart(String, IProgressMonitor)
 	 */
 	public void restart(final String mode2, final IProgressMonitor monitor) {
 		if (getServerType() == null || getServerState() == STATE_STOPPED)
 			return;
 		
 		if (getServerType() == null)
 			return;
 		
 		if (getServerState() == STATE_STOPPED)
 			return;
 		
 		Trace.trace(Trace.FINEST, "Restarting server: " + getName());
 		
 		RestartJob restartJob = new RestartJob(mode2);
 		restartJob.schedule();
 	}
 
 	/**
 	 * Returns true if the server is in a state that it can
 	 * be stopped.
 	 *
 	 * @return boolean
 	 */
 	public IStatus canStop() {
 		if (getServerType() == null)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorMissingAdapter, null);
 		
 		if (getServerState() == STATE_STOPPED || getServerState() == STATE_STOPPING)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorStopAlreadyStopped, null);
 		
 		if (!getServerType().supportsLaunchMode(getMode()))
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorLaunchMode, null);
 		
 		if (behaviourDelegate != null)
 			return behaviourDelegate.canStop();
 		
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * @see IServer#stop(boolean)
 	 */
 	public void stop(boolean force) {
 		if (getServerState() == STATE_STOPPED)
 			return;
 		
 		StopJob job = new StopJob(force);
 		job.schedule();
 	}
 
 	protected IStatus publishBeforeStart(IProgressMonitor monitor, boolean synchronous){
 
 		// check if we need to publish
 		byte pub = StartJob.PUBLISH_NONE;
 		if (ServerCore.isAutoPublishing() && shouldPublish()) {
 			if (((ServerType)getServerType()).startBeforePublish())
 				return Status.OK_STATUS;
 			pub = StartJob.PUBLISH_BEFORE;
 		}
 		
 		final IStatus [] pubStatus = new IStatus[]{Status.OK_STATUS};
 		
 		// publish before start
 		byte publish = pub;
 		if (publish == StartJob.PUBLISH_BEFORE) {
 			PublishJob pubJob = new PublishJob(IServer.PUBLISH_INCREMENTAL, null,false, null);
 			pubJob.addJobChangeListener(new JobChangeAdapter(){
 				public void done(IJobChangeEvent event) {
 					IStatus status = event.getResult();
 					if (status != null && status.getSeverity() == IStatus.ERROR)
 						pubStatus[0] = status; 
 				}
 				
 			});
 			
 			pubJob.schedule();
 			
 			try{
 				if (synchronous)
 					pubJob.join();
 					
 			}
 			catch (InterruptedException ie){
 				return Status.CANCEL_STATUS;
 			}
 		}
 		return pubStatus[0];
 	}
 	
 	protected IStatus publishAfterStart(IProgressMonitor monitor, boolean synchronous, final IOperationListener op){
 		
 		// check if we need to publish
 		byte pub = StartJob.PUBLISH_NONE;
 		if (ServerCore.isAutoPublishing() && shouldPublish()) {
 			if (((ServerType)getServerType()).startBeforePublish())
 				pub = StartJob.PUBLISH_AFTER;
 			else
 				return Status.OK_STATUS;
 		}
 		
 		final IStatus [] pubStatus = new IStatus[]{Status.OK_STATUS};
 		
 		// publish after start
 		byte publish = pub;
 		if (publish == StartJob.PUBLISH_AFTER) {
 			PublishJob pubJob = new PublishJob(IServer.PUBLISH_INCREMENTAL, null,false, null);
 			pubJob.addJobChangeListener(new JobChangeAdapter(){
 				public void done(IJobChangeEvent event) {
 					IStatus status = event.getResult();
 					if (status != null && status.getSeverity() == IStatus.ERROR)
 						pubStatus[0] = status; 
 					if (op != null){
 						op.done(status);
 					}
 				}
 				
 			});
 			
 			pubJob.schedule();
 			
 			try{
 				if (synchronous)
 					pubJob.join();
 			}
 			catch (InterruptedException ie){
 				return Status.CANCEL_STATUS;
 			}
 		}
 		return pubStatus[0];
 	}
 	
 	/**
 	 * @see IServer#start(String, IProgressMonitor)
 	 */
 	public void start(String mode2, IProgressMonitor monitor) throws CoreException {
 		Trace.trace(Trace.FINEST, "Starting server: " + toString() + ", launchMode: " + mode2);
 		if (getServerType() == null)
 			return;
 		
 		// make sure that the delegate is loaded and the server state is correct
 		loadAdapter(ServerBehaviourDelegate.class, null);
 		
 		IStatus status = publishBeforeStart(monitor,false);
 		
 		if (status != null && status.getSeverity() == IStatus.ERROR){
 			Trace.trace(Trace.FINEST,"Failed publish job during start routine");
 			return;
 		}
 		
 		StartJob startJob = new StartJob(mode2);
 		startJob.schedule();
 		
 		publishAfterStart(monitor,false,null);
 	}
 	
 	/**
 	 * @see IServer#start(String, IOperationListener)
 	 */
 	public void start(String mode2, final IOperationListener opListener) {
 		if (getServerType() == null) {
 			if (opListener != null)
 				opListener.done(Status.OK_STATUS);
 			return;
 		}
 		
 		// make sure that the delegate is loaded and the server state is correct
 		loadAdapter(ServerBehaviourDelegate.class, null);
 		
 		IStatus status = publishBeforeStart(null,false);
 		
 		if (status != null && status.getSeverity() == IStatus.ERROR){
 			Trace.trace(Trace.FINEST,"Failed publish job during start routine");
 			return;
 		}
 
 		// check the publish flag (again) to determine when to call opListener.done 
 		byte pub = StartJob.PUBLISH_NONE;
 		if (ServerCore.isAutoPublishing() && shouldPublish()) {
 			if (((ServerType)getServerType()).startBeforePublish())
 				pub = StartJob.PUBLISH_AFTER;
 			else
 				pub = StartJob.PUBLISH_BEFORE;
 		}
 
 		StartJob startJob = new StartJob(mode2);
 		if (opListener != null && pub == StartJob.PUBLISH_BEFORE) {
 			startJob.addJobChangeListener(new JobChangeAdapter() {
 				public void done(IJobChangeEvent event) {
 					opListener.done(event.getResult());
 				}
 			});
 		}
 		startJob.schedule();
 		
 		publishAfterStart(null,false, opListener);
 	}
 
 	public void synchronousStart(String mode2, IProgressMonitor monitor) throws CoreException {
 		if (getServerType() == null)
 			return;
 		
 		// make sure that the delegate is loaded and the server state is correct
 		loadAdapter(ServerBehaviourDelegate.class, monitor);
 		
 		IStatus status = publishBeforeStart(monitor,true);
 		
 		if (status != null && status.getSeverity() == IStatus.ERROR){
 			Trace.trace(Trace.FINEST,"Failed publish job during start routine");
 			return;
 		}
 		
 		StartJob startJob = new StartJob(mode2);
 		startJob.schedule();
 		
 		try {
 			startJob.join();
 		} catch (InterruptedException e) {
 			Trace.trace(Trace.WARNING, "Error waiting for job", e);
 		}
 		
 		publishAfterStart(monitor,true,null);
 	}
 
 	/*
 	 * @see IServer#synchronousRestart(String, IProgressMonitor)
 	 */
 	public void synchronousRestart(String mode2, IProgressMonitor monitor) throws CoreException {
 		synchronousStop(true);
 		synchronousStart(mode2, monitor);
 	}
 
 	/*
 	 * @see IServer#restart(String, IOperationListener)
 	 */
 	public void restart(String mode2, final IOperationListener opListener) {
 		if (getServerType() == null) {
 			if (opListener != null)
 				opListener.done(Status.OK_STATUS);
 			return;
 		}
 		
 		if (getServerState() == STATE_STOPPED) {
 			if (opListener != null)
 				opListener.done(Status.OK_STATUS);
 			return;
 		}
 		
 		RestartJob restartJob = new RestartJob(mode2);
 		if (opListener != null) {
 			restartJob.addJobChangeListener(new JobChangeAdapter() {
 				public void done(IJobChangeEvent event) {
 					opListener.done(event.getResult());
 				}
 			});
 		}
 		restartJob.schedule();
 	}
 
 	/*
 	 * @see IServer#stop(boolean, IOperationListener)
 	 */
 	public void stop(boolean force, final IOperationListener opListener) {
 		if (getServerType() == null) {
 			if (opListener != null)
 				opListener.done(Status.OK_STATUS);
 			return;
 		}
 		
 		if (getServerState() == IServer.STATE_STOPPED) {
 			if (opListener != null)
 				opListener.done(Status.OK_STATUS);
 			return;
 		}
 		
 		StopJob job = new StopJob(force);
 		if (opListener != null) {
 			job.addJobChangeListener(new JobChangeAdapter() {
 				public void done(IJobChangeEvent event) {
 					opListener.done(event.getResult());
 				}
 			});
 		}
 		job.schedule();
 	}
 
 	/*
 	 * @see IServer#synchronousStop()
 	 */
 	public void synchronousStop(boolean force) {
 		if (getServerType() == null)
 			return;
 		
 		if (getServerState() == IServer.STATE_STOPPED)
 			return;
 		
 		StopJob job = new StopJob(force);
 		job.schedule();
 		try {
 			job.join();
 		} catch (InterruptedException e) {
 			Trace.trace(Trace.WARNING, "Error waiting for job", e);
 		}
 	}
 
 	/*
 	 * Trigger a restart of the given module and wait until it has finished restarting.
 	 *
 	 * @param module org.eclipse.wst.server.core.IModule
 	 * @param monitor org.eclipse.core.runtime.IProgressMonitor
 	 * @exception org.eclipse.core.runtime.CoreException - thrown if an error occurs while trying to restart the module
 	 *
 	public void synchronousRestartModule(final IModule[] module, IProgressMonitor monitor) throws CoreException {
 		Trace.trace(Trace.FINEST, "synchronousModuleRestart 1");
 
 		final Object mutex = new Object();
 	
 		// add listener to the module
 		IServerListener listener = new IServerListener() {
 			public void serverChanged(ServerEvent event) {
 				int eventKind = event.getKind();
 				IServer server = event.getServer();
 				if (eventKind == (ServerEvent.MODULE_CHANGE | ServerEvent.STATE_CHANGE)) {
 					int state = server.getModuleState(module);
 					if (state == IServer.STATE_STARTED || state == IServer.STATE_STOPPED) {
 						// notify waiter
 						synchronized (mutex) {
 							try {
 								Trace.trace(Trace.FINEST, "synchronousModuleRestart notify");
 								mutex.notifyAll();
 							} catch (Exception e) {
 								Trace.trace(Trace.SEVERE, "Error notifying module restart", e);
 							}
 						}
 					}
 				}
 			}
 		};
 		addServerListener(listener);
 		
 		// make sure it times out after 30s
 		class Timer {
 			boolean timeout;
 			boolean alreadyDone;
 		}
 		final Timer timer = new Timer();
 		
 		Thread thread = new Thread() {
 			public void run() {
 				try {
 					Thread.sleep(30000);
 					if (!timer.alreadyDone) {
 						timer.timeout = true;
 						// notify waiter
 						synchronized (mutex) {
 							Trace.trace(Trace.FINEST, "synchronousModuleRestart notify timeout");
 							mutex.notifyAll();
 						}
 					}
 				} catch (Exception e) {
 					Trace.trace(Trace.SEVERE, "Error notifying module restart timeout", e);
 				}
 			}
 		};
 		thread.setDaemon(true);
 		thread.start();
 	
 		Trace.trace(Trace.FINEST, "synchronousModuleRestart 2");
 	
 		// restart the module
 		try {
 			getBehaviourDelegate(null).restartModule(module, monitor);
 		} catch (CoreException e) {
 			removeServerListener(listener);
 			throw e;
 		}
 	
 		Trace.trace(Trace.FINEST, "synchronousModuleRestart 3");
 	
 		// wait for it! wait for it! ...
 		synchronized (mutex) {
 			try {
 				while (!timer.timeout && !(getModuleState(module) == IServer.STATE_STARTED || getModuleState(module) == IServer.STATE_STOPPED))
 					mutex.wait();
 			} catch (Exception e) {
 				Trace.trace(Trace.SEVERE, "Error waiting for server start", e);
 			}
 		}
 		removeServerListener(listener);
 		if (timer.timeout)
 			throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorModuleRestartFailed, getName()), null));
 		timer.alreadyDone = true;
 		
 		if (getModuleState(module) == IServer.STATE_STOPPED)
 			throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorModuleRestartFailed, getName()), null));
 	
 		Trace.trace(Trace.FINEST, "synchronousModuleRestart 4");
 	}*/
 
 	public IPath getTempDirectory() {
 		return ServerPlugin.getInstance().getTempDirectory(getId());
 	}
 
 	protected String getXMLRoot() {
 		return "server";
 	}
 
 	protected void loadState(IMemento memento) {
 		resolve();
 	}
 
 	public void serialize(IMemento memento) {
 		save(memento);
 	}
 
 	public void deserialize(IMemento memento) {
 		load(memento);
 	}
 
 	protected void resolve() {
 		IServerType oldServerType = serverType;
 		String serverTypeId = getAttribute("server-type-id", (String)null);
 		if (serverTypeId != null)
 			serverType = ServerCore.findServerType(serverTypeId);
 		else
 			serverType = null;
 		if (serverType != null && !serverType.equals(oldServerType))
 			serverState = ((ServerType)serverType).getInitialState();
 		
 		String runtimeId = getAttribute(RUNTIME_ID, (String)null);
 		if (runtimeId != null)
 			runtime = ServerCore.findRuntime(runtimeId);
 		
 		String configPath = getAttribute(CONFIGURATION_ID, (String)null);
 		configuration = null;
 		if (configPath != null)
 			configuration = ResourcesPlugin.getWorkspace().getRoot().getFolder(new Path(configPath));
 
 		// for migration from WTP 2.0 -> WTP 3.0
 		int autoPubSetting = getAttribute(PROP_AUTO_PUBLISH_SETTING, AUTO_PUBLISH_ENABLE);
 		if (autoPubSetting == 0)
 			map.put(PROP_AUTO_PUBLISH_SETTING, AUTO_PUBLISH_ENABLE);
 	}
 
 	protected void setInternal(ServerWorkingCopy wc) {
 		map = new HashMap<String, Object>(wc.map);
 		configuration = wc.configuration;
 		runtime = wc.runtime;
 		serverSyncState = wc.serverSyncState;
 		//restartNeeded = wc.restartNeeded;
 		serverType = wc.serverType;
 		modules = wc.modules;
 		
 		// can never modify the following properties via the working copy
 		//serverState = wc.serverState;
 		delegate = wc.delegate;
 		
 		if (getServerState() == IServer.STATE_STARTED)
 			autoPublish();
 	}
 
 	protected void saveState(IMemento memento) {
 		if (serverType != null)
 			memento.putString("server-type", serverType.getId());
 		
 		if (configuration != null)
 			memento.putString(CONFIGURATION_ID, configuration.getFullPath().toString());
 		else
 			memento.putString(CONFIGURATION_ID, null);
 		
 		if (runtime != null)
 			memento.putString(RUNTIME_ID, runtime.getId());
 		else
 			memento.putString(RUNTIME_ID, null);
 	}
 
 	/*public void updateConfiguration() {
 		try {
 			getDelegate(null).updateConfiguration();
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error calling delegate updateConfiguration() " + toString(), e);
 		}
 	}*/
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.core.IServerConfiguration#canModifyModule(org.eclipse.wst.server.core.model.IModule)
 	 */
 	public IStatus canModifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) {
 		if ((add == null || add.length == 0) && (remove == null || remove.length == 0))
 			throw new IllegalArgumentException("Add and remove cannot both be null/empty");
 		
 		if (add != null) {
 			int size = add.length;
 			for (int i = 0; i < size; i++)
 				if (add[i] == null)
 					throw new IllegalArgumentException("Cannot add null entries");
 		}
 		
 		if (remove != null) {
 			int size = remove.length;
 			for (int i = 0; i < size; i++)
 				if (remove[i] == null)
 					throw new IllegalArgumentException("Cannot remove null entries");
 		}
 		
 		if (getServerType() == null)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorMissingAdapter, null);
 		
 		if (add != null && add.length > 0) {
 			int size = add.length;
 			for (int i = 0; i < size; i++) {
 				IModuleType moduleType = add[i].getModuleType(); 
 				if (!ServerUtil.isSupportedModule(getServerType().getRuntimeType().getModuleTypes(), moduleType))
 					return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorCannotAddModule,
 							new Object[] { moduleType.getName(), moduleType.getVersion() }), null);
 			}
 		}
 		
 		try {
 			return getDelegate(monitor).canModifyModules(add, remove);
 		} catch (Exception e) {
 			ServerPlugin.logExtensionFailure(toString(), e);
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, e.getMessage(), e);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.core.IServer#getModules()
 	 */
 	public IModule[] getModules() {
 		if (modules == null) {
 			// convert from attribute
 			List<String> list = getAttribute(MODULE_LIST, (List<String>) null);
 			if (list == null)
 				list = new ArrayList<String>(1);
 			
 			modules = new ArrayList<IModule>(list.size() + 1);
 			Iterator iterator = list.iterator();
 			while (iterator.hasNext()) {
 				String moduleId = (String) iterator.next();
 				String name = "<unknown>";
 				int index = moduleId.indexOf("::");
 				if (index > 0) {
 					name = moduleId.substring(0, index);
 					moduleId = moduleId.substring(index+2);
 				}
 				
 				String moduleTypeId = null;
 				String moduleTypeVersion = null;
 				index = moduleId.indexOf("::");
 				if (index > 0) {
 					int index2 = moduleId.indexOf("::", index+1);
 					moduleTypeId = moduleId.substring(index+2, index2);
 					moduleTypeVersion = moduleId.substring(index2+2);
 					moduleId = moduleId.substring(0, index);
 				}
 				
 				IModule module = ServerUtil.getModule(moduleId);
 				if (module == null) {
 					IModuleType moduleType = null;
 					if (moduleTypeId != null)
 						moduleType = ModuleType.getModuleType(moduleTypeId, moduleTypeVersion);
 					module = new DeletedModule(moduleId, name, moduleType);
 				}
 				modules.add(module);
 			}
 		}
 		
 		List em = getExternalModules();
 		if (em == null || em.isEmpty()) {
 			IModule[] modules2 = new IModule[modules.size()];
 			modules.toArray(modules2);
 			return modules2;
 		}
 		
 		IModule[] modules2 = new IModule[modules.size() + em.size()];
 		modules.toArray(modules2);
 		
 		Object[] obj = em.toArray();
 		System.arraycopy(obj, 0, modules2, modules.size(), em.size());
 		
 		return modules2;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.core.IServer#getModuleState()
 	 */
 	public int getModuleState(IModule[] module) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		try {
 			Integer in = moduleState.get(getKey(module));
 			if (in != null)
 				return in.intValue();
 		} catch (Exception e) {
 			// ignore
 		}
 		return STATE_UNKNOWN;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.core.IServer#getModuleState()
 	 */
 	public int getModulePublishState(IModule[] module) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		try {
 			Integer in = modulePublishState.get(getKey(module));
 			if (in != null)
 				return in.intValue();
 		} catch (Exception e) {
 			// ignore
 		}
 		return PUBLISH_STATE_UNKNOWN;
 	}
 
 	/*
 	 * @see IServer#getChildModule(IModule[])
 	 */
 	public IModule[] getChildModules(IModule[] module, IProgressMonitor monitor) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		try {
 			int i = module.length - 1;
 			if (module[i].getProject() == null || !module[i].getProject().isAccessible())
 				return null;
 			
 			ServerDelegate sd = getDelegate(monitor);
 			if (sd == null)
 				return null;
 			IModule[] children = sd.getChildModules(module);
 			if (children != null && children.length == 1 && children[0].equals(module[module.length - 1]))
 				return null;
 			return children;
 		} catch (Exception e) {
 			ServerPlugin.logExtensionFailure(toString(), e);
 			return null;
 		}
 	}
 
 	/*
 	 * @see IServer#getRootModules(IModule)
 	 */
 	public IModule[] getRootModules(IModule module, IProgressMonitor monitor) throws CoreException {
 		if (module == null)
 			throw new IllegalArgumentException("Module cannot be null");
 		try {
 			return getDelegate(monitor).getRootModules(module);
 		} catch (CoreException se) {
 			throw se;
 		} catch (Exception e) {
 			ServerPlugin.logExtensionFailure(toString(), e);
 			return null;
 		}
 	}
 
 	/**
 	 * Returns whether the given module can be restarted.
 	 *
 	 * @param module the module
 	 * @param monitor
 	 * @return <code>true</code> if the given module can be
 	 *    restarted, and <code>false</code> otherwise
 	 */
 	public IStatus canControlModule(IModule[] module, IProgressMonitor monitor) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		try {
 			ServerBehaviourDelegate bd = getBehaviourDelegate(monitor);
 			if (bd == null)
 				return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorRestartModule, null);
 			boolean b = bd.canControlModule(module);
 			if (b)
 				return Status.OK_STATUS;
 		} catch (Exception e) {
 			ServerPlugin.logExtensionFailure(toString(), e);
 		}
 		return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorRestartModule, null);
 	}
 
 	/**
 	 * Check if the given module is in sync on the server. It should
 	 * return true if the module should be restarted (is out of
 	 * sync) or false if the module does not need to be restarted.
 	 *
 	 * @param module org.eclipse.wst.server.core.model.IModule
 	 * @return boolean
 	 */
 	public boolean getModuleRestartState(IModule[] module) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		try {
 			Boolean b = moduleRestartState.get(getKey(module));
 			if (b != null)
 				return b.booleanValue();
 		} catch (Exception e) {
 			// ignore
 		}
 		return false;
 	}
 
 	abstract class OperationContext {
 		public abstract void run(IProgressMonitor monitor) throws CoreException;
 		public void cancel() {
 			// do nothing
 		}
 		public abstract String getTimeoutMessage();
 		public abstract String getFailureMessage();
 	}
 	/**
 	 * 
 	 * @param module the module that the operation applies to, or null for a server operation
 	 * @param monitor cannot be null
 	 * @return the status
 	 */
 	protected IStatus runAndWait(final IModule[] module, OperationContext runnable, final int operationTimeout, IProgressMonitor monitor) {
 		final boolean[] notified = new boolean[1];
 		IServerListener listener = new IServerListener() {
 			public void serverChanged(ServerEvent event) {
 				int eventKind = event.getKind();
 				if ((eventKind | ServerEvent.STATE_CHANGE) != 0) {
 					if ((module == null && (eventKind | ServerEvent.SERVER_CHANGE) != 0) ||
 							(module != null && module.equals(event.getModule()) && (eventKind | ServerEvent.MODULE_CHANGE) != 0)) {
 						int state = getServerState();
 						if (state == IServer.STATE_STARTED || state == IServer.STATE_STOPPED) {
 							// notify waiter
 							synchronized (notified) {
 								try {
 									Trace.trace(Trace.FINEST, "runAndWait notify");
 									notified[0] = true;
 									notified.notifyAll();
 								} catch (Exception e) {
 									Trace.trace(Trace.SEVERE, "Error notifying runAndWait", e);
 								}
 							}
 						}
 					}
 				}
 			}
 		};
 		
 		class Timer {
 			boolean timeout;
 			boolean alreadyDone;
 		}
 		final Timer timer = new Timer();
 		
 		final IProgressMonitor monitor2 = monitor;
 		Thread thread = new Thread("Server RunAndWait Timeout") {
 			public void run() {
 				try {
 					int totalTimeout = operationTimeout;
 					if (totalTimeout < 0)
 						totalTimeout = 1;
 					boolean userCancelled = false;
 					int retryPeriod = 1;
 					while (!notified[0] && totalTimeout > 0 && !userCancelled && !timer.alreadyDone) {
 						// The operationTimeout is in seconds.  Therefore, each retry period have to wait for 1 sec.
 						Thread.sleep(retryPeriod * 1000);
 						if (operationTimeout > 0)
 							totalTimeout -= retryPeriod;
 						if (!notified[0] && !timer.alreadyDone && monitor2.isCanceled()) {
 							// user canceled
 							userCancelled = true;
 							if (launch != null && !launch.isTerminated())
 								launch.terminate();//TODO
 							// notify waiter
 							synchronized (notified) {
 								Trace.trace(Trace.FINEST, "runAndWait user cancelled");
 								notified[0] = true;
 								notified.notifyAll();
 							}
 						}
 					}
 					if (!userCancelled && !timer.alreadyDone && !notified[0]) {
 						// notify waiter
 						synchronized (notified) {
 							Trace.trace(Trace.FINEST, "runAndWait notify timeout");
 							if (!timer.alreadyDone && totalTimeout <= 0)
 								timer.timeout = true;
 							notified[0] = true;
 							notified.notifyAll();
 						}
 					}
 				} catch (Exception e) {
 					Trace.trace(Trace.SEVERE, "Error notifying runAndWait timeout", e);
 				}
 			}
 		};
 		thread.setDaemon(true);
 		thread.start();
 	
 		Trace.trace(Trace.FINEST, "runAndWait 2");
 	
 		// do the operation
 		try {
 			runnable.run(monitor);
 		} catch (CoreException e) {
 			removeServerListener(listener);
 			timer.alreadyDone = true;
 			return e.getStatus();
 		}
 		if (monitor.isCanceled()) {
 			removeServerListener(listener);
 			timer.alreadyDone = true;
 			return Status.CANCEL_STATUS;
 		}
 		
 		Trace.trace(Trace.FINEST, "runAndWait 3");
 		
 		// wait for it! wait for it! ...
 		synchronized (notified) {
 			try {
 				while (!notified[0] && !monitor.isCanceled() && !timer.timeout
 						&& !(getServerState() == IServer.STATE_STARTED || getServerState() == IServer.STATE_STOPPED)) {
 					notified.wait();
 				}
 			} catch (Exception e) {
 				Trace.trace(Trace.SEVERE, "Error waiting for operation", e);
 			}
 			timer.alreadyDone = true;
 		}
 		removeServerListener(listener);
 		
 		if (timer.timeout) {
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 1, NLS.bind(Messages.errorStartTimeout, new String[] { getName(), (operationTimeout / 1000) + "" }), null);
 		}
 		
 		if (!monitor.isCanceled() && getServerState() == IServer.STATE_STOPPED)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 2, NLS.bind(Messages.errorStartFailed, getName()), null);
 		
 		return Status.OK_STATUS;
 	}
 
 	/*
 	 * @see IServer#startModule(IModule[], IOperationListener)
 	 */
 	public void startModule(final IModule[] module, final IOperationListener opListener) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty or empty");
 		
 		Job moduleJob = new ServerJob(NLS.bind(Messages.jobStarting, module[0].getName())) {
 			protected IStatus run(IProgressMonitor monitor) {
 				return runAndWait(module, new OperationContext() {
 					public String getFailureMessage() {
 						return Messages.errorStartFailed;
 					}
 
 					public String getTimeoutMessage() {
 						return Messages.errorStartTimeout;
 					}
 
 					public void run(IProgressMonitor monitor2) throws CoreException {
 						try {
 							getBehaviourDelegate(monitor2).startModule(module, monitor2);
 						} catch (Exception e) {
 							Trace.trace(Trace.SEVERE, "Error calling delegate startModule() " + toString(), e);
 							throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, e.getMessage()));
 						}
 					}
 				}, getStartTimeout(), monitor);
 			}
 		};
 		if (opListener != null) {
 			moduleJob.addJobChangeListener(new JobChangeAdapter() {
 				public void done(IJobChangeEvent event) {
 					opListener.done(event.getResult());
 				}
 			});
 		}
 		moduleJob.schedule();
 	}
 
 	/*
 	 * @see IServer#stopModule(IModule[], IOperationListener)
 	 */
 	public void stopModule(final IModule[] module, final IOperationListener opListener) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		
 		Job moduleJob = new ServerJob(NLS.bind(Messages.jobStopping, module[0].getName())) {
 			protected IStatus run(IProgressMonitor monitor) {
 				return runAndWait(module, new OperationContext() {
 					public String getFailureMessage() {
 						return Messages.errorStopFailed;
 					}
 
 					public String getTimeoutMessage() {
 						return Messages.errorStopFailed;
 					}
 
 					public void run(IProgressMonitor monitor2) throws CoreException {
 						try {
 							getBehaviourDelegate(monitor2).stopModule(module, monitor2);
 						} catch (Exception e) {
 							Trace.trace(Trace.SEVERE, "Error calling delegate stopModule() " + toString(), e);
 							throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, e.getMessage()));
 						}
 					}
 				}, getStopTimeout(), monitor);
 			}
 		};
 		if (opListener != null) {
 			moduleJob.addJobChangeListener(new JobChangeAdapter() {
 				public void done(IJobChangeEvent event) {
 					opListener.done(event.getResult());
 				}
 			});
 		}
 		moduleJob.schedule();
 	}
 
 	/*
 	 * @see IServer#restartModule(IModule[], IOperationListener, IProgressMonitor)
 	 */
 	public void restartModule(final IModule[] module, final IOperationListener opListener) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		
 		Job moduleJob = new ServerJob(NLS.bind(Messages.jobRestarting, module[0].getName())) {
 			protected IStatus run(IProgressMonitor monitor) {
 				return runAndWait(module, new OperationContext() {
 					public String getFailureMessage() {
 						return Messages.errorRestartFailed;
 					}
 
 					public String getTimeoutMessage() {
 						return Messages.errorRestartTimeout;
 					}
 
 					public void run(IProgressMonitor monitor2) throws CoreException {
 						try {
 							getBehaviourDelegate(monitor2).restartModule(module, monitor2);
 						} catch (Exception e) {
 							Trace.trace(Trace.SEVERE, "Error calling delegate restartModule() " + toString(), e);
 							throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, e.getMessage()));
 						}
 					}
 				}, getStopTimeout() + getStartTimeout(), monitor);
 			}
 		};
 		if (opListener != null) {
 			moduleJob.addJobChangeListener(new JobChangeAdapter() {
 				public void done(IJobChangeEvent event) {
 					opListener.done(event.getResult());
 				}
 			});
 		}
 		moduleJob.schedule();
 	}
 
 	/**
 	 * Returns an array of IServerPorts that this server has.
 	 * 
 	 * @param monitor
 	 * @return a possibly empty array of servers ports
 	 */
 	public ServerPort[] getServerPorts(IProgressMonitor monitor) {
 		try {
 			return getDelegate(monitor).getServerPorts();
 		} catch (Exception e) {
 			ServerPlugin.logExtensionFailure(toString(), e);
 			return null;
 		}
 	}
 
 	/**
 	 * Visit all the modules in the server with the given module visitor.
 	 * 
 	 * @param visitor the visitor
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 */
 	public void visit(IModuleVisitor visitor, IProgressMonitor monitor) {
 		if (visitor == null)
 			throw new IllegalArgumentException("Visitor cannot be null");
 		
 		IModule[] modules2 = getModules();
 		if (modules2 != null) { 
 			int size = modules2.length;
 			for (int i = 0; i < size; i++) {
 				if (!visitModule(new IModule[] { modules2[i] }, visitor, monitor))
 					return;
 			}
 		}
 	}
 
 	/**
 	 * Returns true to keep visiting, and false to stop.
 	 * 
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 */
 	private boolean visitModule(IModule[] module, IModuleVisitor visitor, IProgressMonitor monitor) {
 		if (module == null)
 			return true;
 		
 		if (!visitor.visit(module))
 			return false;
 		
 		IModule[] children = getChildModules(module, monitor);
 		if (children != null) {
 			int size = children.length;
 			for (int i = 0; i < size; i++) {
 				IModule[] module2 = new IModule[module.length + 1];
 				System.arraycopy(module, 0, module2, 0, module.length);
 				module2[module.length] = children[i];
 				
 				if (!visitModule(module2, visitor, monitor))
 					return false;
 			}
 		}
 		
 		return true;
 	}
 
 	protected String getKey(IModule[] module) {
 		StringBuffer sb = new StringBuffer();
 		
 		if (module != null) {
 			int size = module.length;
 			for (int i = 0; i < size; i++) {
 				if (i != 0)
 					sb.append("#");
 				sb.append(module[i].getId());
 			}
 		}
 		
 		return sb.toString();
 	}
 
 	public void setModuleStatus(IModule[] module, IStatus status) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		moduleStatus.put(getKey(module), status);
 		//fireServerModuleStateChangeEvent(module);
 	}
 
 	public IStatus getModuleStatus(IModule[] module) {
 		if (module == null || module.length == 0)
 			throw new IllegalArgumentException("Module cannot be null or empty");
 		try {
 			return moduleStatus.get(getKey(module));
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public void setServerStatus(IStatus status) {
 		serverStatus = status;
 		//fireServerStateChangeEvent();
 	}
 
 	public IStatus getServerStatus() {
 		return serverStatus;
 	}
 
 	/**
 	 * Switch the server's location between the workspace and .metadata.
 	 * 
 	 * @param server a server
 	 * @param monitor a progress monitor
 	 * @throws CoreException if something goes wrong
 	 */
 	public static void switchLocation(Server server, IProgressMonitor monitor) throws CoreException {
 		IFile file = server.getFile();
 		ServerWorkingCopy wc = (ServerWorkingCopy) server.createWorkingCopy();
 		server.delete();
 		if (file == null) {
 			IProject project = ServerType.getServerProject();
 			file = ServerUtil.getUnusedServerFile(project, wc);
 			wc.setFile(file);
 			server.file = file;
 		} else {
 			wc.setFile(null);
 			server.file = null;
 		}
 		wc.save(true, monitor);
 	}
 
 	/**
 	 * Returns the current state of the server (see SERVER_XXX constants) after
 	 * refreshing the state of the server. The only difference between this method
 	 * and the method without a progress monitor is that this method may cause
 	 * plugin loading and not return immediately. However, the server will always
 	 * be updated and in sync, so the IServer.STATE_UNKNOWN state should never be
 	 * returned.
 	 * 
 	 * @param monitor
 	 * @return the server state
 	 */
 	public int getServerState(IProgressMonitor monitor) {
 		loadAdapter(ServerBehaviourDelegate.class, monitor);
 		return getServerState();
 	}
 
 	protected IStatus publishImpl(int kind, List<IModule[]> modules4, IAdaptable info, IProgressMonitor monitor) {
 		Trace.trace(Trace.FINEST, "-->-- Publishing to server: " + Server.this.toString() + " -->--");
 		
 		stopAutoPublish();
 		
 		try {
 			long time = System.currentTimeMillis();
 			firePublishStarted();
 			
 			getServerPublishInfo().startCaching();
 			IStatus status = Status.OK_STATUS;
 			try {
 				getBehaviourDelegate(monitor).publish(kind, modules4, monitor, info);
 			} catch (CoreException ce) {
 				Trace.trace(Trace.WARNING, "Error during publishing", ce);
 				status = ce.getStatus();
 			}
 			
 			final List<IModule[]> modules2 = new ArrayList<IModule[]>();
 			visit(new IModuleVisitor() {
 				public boolean visit(IModule[] module) {
 					if (getModulePublishState(module) == IServer.PUBLISH_STATE_NONE)
 						getServerPublishInfo().fill(module);
 					
 					modules2.add(module);
 					return true;
 				}
 			}, monitor);
 			
 			getServerPublishInfo().removeDeletedModulePublishInfo(Server.this, modules2);
 			getServerPublishInfo().clearCache();
 			getServerPublishInfo().save();
 			
 			firePublishFinished(Status.OK_STATUS);
 			Trace.trace(Trace.PERFORMANCE, "Server.publish(): <" + (System.currentTimeMillis() - time) + "> " + getServerType().getId());
 			return status;
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error calling delegate publish() " + Server.this.toString(), e);
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorPublishing, e);
 		}
 	}
 
 	protected IStatus restartImpl(String launchMode, IProgressMonitor monitor) {
 		Trace.trace(Trace.FINEST, "Restarting server: " + getName());
 		
 		//final IStatus[] status2 = new IStatus[1];
 		
 		try {
 			IServerListener curListener = new IServerListener() {
 				public void serverChanged(ServerEvent event) {
 					int eventKind = event.getKind();
 					IServer server = event.getServer();
 					if (eventKind == (ServerEvent.SERVER_CHANGE | ServerEvent.STATE_CHANGE)) {
 						if (server.getServerState() == STATE_STARTED) {
 							server.removeServerListener(this);
 							//status2[0] = Status.OK_STATUS;
 						}
 					}
 				}
 			};
 			
 			try {
 				addServerListener(curListener);
 				
 				// synchronous restart
 				restartImpl2(launchMode, monitor);
 				
 				return Status.OK_STATUS;
 			} catch (CoreException ce) {
 				removeServerListener(curListener);
 				if (ce.getStatus().getCode() != -1) {
 					Trace.trace(Trace.SEVERE, "Error calling delegate restart() " + Server.this.toString());
 					return ce.getStatus();
 				}
 			}
 			
 			final String mode3 = launchMode;
 			// add listener to start it as soon as it is stopped
 			addServerListener(new IServerListener() {
 				public void serverChanged(ServerEvent event) {
 					int eventKind = event.getKind();
 					IServer server = event.getServer();
 					if (eventKind == (ServerEvent.SERVER_CHANGE | ServerEvent.STATE_CHANGE)) {
 						if (server.getServerState() == STATE_STOPPED) {
 							server.removeServerListener(this);
 							
 							// restart in a quarter second (give other listeners a chance
 							// to hear the stopped message)
 							Thread t = new Thread("Server Restart") {
 								public void run() {
 									try {
 										Thread.sleep(250);
 									} catch (Exception e) {
 										// ignore
 									}
 									
 									Server.this.start(mode3, (IOperationListener)null);
 								}
 							};
 							t.setDaemon(true);
 							t.setPriority(Thread.NORM_PRIORITY - 2);
 							t.start();
 						}
 					}
 				}
 			});
 			
 			// stop the server
 			stop(false);
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error restarting server", e);
			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorStartFailed, getName()), e);
 		}
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * Synchroneous restart. Throws a core exception in case the the adopter doesn't implement restart 
 	 * @param launchMode
 	 * @param monitor
 	 * @return IStatus 
 	 * @throws CoreException
 	 */
 	protected IStatus restartImpl2(String launchMode, IProgressMonitor monitor) throws CoreException{
 		final boolean[] notified = new boolean[1];
 		
 		// add listener to the server
 		IServerListener listener = new IServerListener() {
 			public void serverChanged(ServerEvent event) {
 				int eventKind = event.getKind();
 				IServer server = event.getServer();
 				if (eventKind == (ServerEvent.SERVER_CHANGE | ServerEvent.STATE_CHANGE)) {
 					int state = server.getServerState();
 					if (state == IServer.STATE_STARTED) {
 						// notify waiter
 						synchronized (notified) {
 							try {
 								Trace.trace(Trace.FINEST, "synchronousRestart notify");
 								notified[0] = true;
 								notified.notifyAll();
 							} catch (Exception e) {
 								Trace.trace(Trace.SEVERE, "Error notifying server restart", e);
 							}
 						}
 					}
 				}
 			}
 		};
 		addServerListener(listener);
 		
 		final int restartTimeout = (getStartTimeout() * 1000) + (getStopTimeout() * 1000);
 		class Timer {
 			boolean timeout;
 			boolean alreadyDone;
 		}
 		final Timer timer = new Timer();
 		
 		final IProgressMonitor monitor2 = monitor;
 		Thread thread = new Thread("Server Restart Timeout") {
 			public void run() {
 				try {
 					int totalTimeout = restartTimeout;
 					if (totalTimeout < 0)
 						totalTimeout = 1;
 					boolean userCancelled = false;
 					int retryPeriod = 2500;
 					while (!notified[0] && totalTimeout > 0 && !userCancelled && !timer.alreadyDone) {
 						Thread.sleep(retryPeriod);
 						if (restartTimeout > 0)
 							totalTimeout -= retryPeriod;
 						if (!notified[0] && !timer.alreadyDone && monitor2.isCanceled()) {
 							// user canceled - set the server state to stopped
 							userCancelled = true;
 							if (launch != null && !launch.isTerminated())
 								launch.terminate();
 							// notify waiter
 							synchronized (notified) {
 								Trace.trace(Trace.FINEST, "synchronousRestart user cancelled");
 								notified[0] = true;
 								notified.notifyAll();
 							}
 						}
 					}
 					if (!userCancelled && !timer.alreadyDone && !notified[0]) {
 						// notify waiter
 						synchronized (notified) {
 							Trace.trace(Trace.FINEST, "synchronousRestart notify timeout");
 							if (!timer.alreadyDone && totalTimeout <= 0)
 								timer.timeout = true;
 							notified[0] = true;
 							notified.notifyAll();
 						}
 					}
 				} catch (Exception e) {
 					Trace.trace(Trace.SEVERE, "Error notifying server restart timeout", e);
 				}
 			}
 		};
 		thread.setDaemon(true);
 		thread.start();
 		
 		Trace.trace(Trace.FINEST, "synchronousRestart 2");
 		
 		// call the delegate restart
 		try {
 			getBehaviourDelegate(null).restart(launchMode);
 		} catch (CoreException e) {
 			removeServerListener(listener);
 			timer.alreadyDone = true;
 			throw new CoreException(e.getStatus());
 		}
 		if (monitor.isCanceled()) {
 			removeServerListener(listener);
 			timer.alreadyDone = true;
 			return Status.CANCEL_STATUS;
 		}
 		
 		Trace.trace(Trace.FINEST, "synchronousRestart 3");
 		
 		// wait for it! wait for it! ...
 		synchronized (notified) {
 			try {
 				while (!notified[0] && !monitor.isCanceled() && !timer.timeout
 						&& !(getServerState() == IServer.STATE_STARTED)) {
 					notified.wait();
 				}
 			} catch (Exception e) {
 				Trace.trace(Trace.SEVERE, "Error waiting for server restart", e);
 			}
 			timer.alreadyDone = true;
 		}
 		removeServerListener(listener);
 		
 		if (timer.timeout) {
 			stop(false);
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorRestartTimeout, new String[] { getName(), (restartTimeout / 1000) + "" }), null);
 		}
 		
 		if (!monitor.isCanceled() && getServerState() == IServer.STATE_STOPPED)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorRestartFailed, getName()), null);
 		
 		Trace.trace(Trace.FINEST, "synchronousRestart 4");
 		return Status.OK_STATUS;
 	}
 	
 	protected IStatus startImpl(String launchMode, IProgressMonitor monitor) {
 		final boolean[] notified = new boolean[1];
 		
 		monitor = ProgressUtil.getMonitorFor(monitor);
 		
 		// add listener to the server
 		IServerListener listener = new IServerListener() {
 			public void serverChanged(ServerEvent event) {
 				int eventKind = event.getKind();
 				IServer server = event.getServer();
 				if (eventKind == (ServerEvent.SERVER_CHANGE | ServerEvent.STATE_CHANGE)) {
 					int state = server.getServerState();
 					if (state == IServer.STATE_STARTED || state == IServer.STATE_STOPPED) {
 						// notify waiter
 						synchronized (notified) {
 							try {
 								Trace.trace(Trace.FINEST, "synchronousStart notify");
 								notified[0] = true;
 								notified.notifyAll();
 							} catch (Exception e) {
 								Trace.trace(Trace.SEVERE, "Error notifying server start", e);
 							}
 						}
 					}
 				}
 			}
 		};
 		addServerListener(listener);
 		
 		final int serverTimeout = getStartTimeout() * 1000;
 		class Timer {
 			boolean timeout;
 			boolean alreadyDone;
 		}
 		final Timer timer = new Timer();
 		
 		final IProgressMonitor monitor2 = monitor;
 		Thread thread = new Thread("Server Start Timeout") {
 			public void run() {
 				try {
 					int totalTimeout = serverTimeout;
 					if (totalTimeout < 0)
 						totalTimeout = 1;
 					boolean userCancelled = false;
 					int retryPeriod = 1000;
 					while (!notified[0] && totalTimeout > 0 && !userCancelled && !timer.alreadyDone) {
 						Thread.sleep(retryPeriod);
 						if (serverTimeout > 0)
 							totalTimeout -= retryPeriod;
 						if (!notified[0] && !timer.alreadyDone && monitor2.isCanceled()) {
 							// user canceled - set the server state to stopped
 							userCancelled = true;
 							if (launch != null && !launch.isTerminated())
 								launch.terminate();
 							// notify waiter
 							synchronized (notified) {
 								Trace.trace(Trace.FINEST, "synchronousStart user cancelled");
 								notified[0] = true;
 								notified.notifyAll();
 							}
 						}
 					}
 					if (!userCancelled && !timer.alreadyDone && !notified[0]) {
 						// notify waiter
 						synchronized (notified) {
 							Trace.trace(Trace.FINEST, "synchronousStart notify timeout");
 							if (!timer.alreadyDone && totalTimeout <= 0)
 								timer.timeout = true;
 							notified[0] = true;
 							notified.notifyAll();
 						}
 					}
 				} catch (Exception e) {
 					Trace.trace(Trace.SEVERE, "Error notifying server start timeout", e);
 				}
 			}
 		};
 		thread.setDaemon(true);
 		thread.start();
 	
 		Trace.trace(Trace.FINEST, "synchronousStart 2");
 	
 		// start the server
 		try {
 			startImpl2(launchMode, monitor);
 		} catch (CoreException e) {
 			removeServerListener(listener);
 			timer.alreadyDone = true;
 			return e.getStatus();
 		}
 		if (monitor.isCanceled()) {
 			removeServerListener(listener);
 			timer.alreadyDone = true;
 			return Status.CANCEL_STATUS;
 		}
 		
 		Trace.trace(Trace.FINEST, "synchronousStart 3");
 		
 		// wait for it! wait for it! ...
 		synchronized (notified) {
 			try {
 				while (!notified[0] && !monitor.isCanceled() && !timer.timeout
 						&& !(getServerState() == IServer.STATE_STARTED || getServerState() == IServer.STATE_STOPPED)) {
 					notified.wait();
 				}
 			} catch (Exception e) {
 				Trace.trace(Trace.SEVERE, "Error waiting for server start", e);
 			}
 			timer.alreadyDone = true;
 		}
 		removeServerListener(listener);
 		
 		if (timer.timeout) {
 			stop(false);
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorStartTimeout, new String[] { getName(), (serverTimeout / 1000) + "" }), null);
 		}
 		
 		if (!monitor.isCanceled() && getServerState() == IServer.STATE_STOPPED)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorStartFailed, getName()), null);
 		
 		Trace.trace(Trace.FINEST, "synchronousStart 4");
 		return Status.OK_STATUS;
 	}
 
 	protected void startImpl2(String mode2, IProgressMonitor monitor) throws CoreException {
 		Trace.trace(Trace.FINEST, "Starting server: " + Server.this.toString() + ", launchMode: " + mode2);
 		
 		// make sure that the delegate is loaded and the server state is correct
 		loadAdapter(ServerBehaviourDelegate.class, monitor);
 		
 		try {
 			ILaunchConfiguration launchConfig = getLaunchConfiguration(true, monitor);
 			//if (launchConfig == null)
 			//	throw new CoreException();
 			launch = launchConfig.launch(mode2, monitor); // , true); - causes workspace lock
 			Trace.trace(Trace.FINEST, "Launch: " + launch);
 		} catch (CoreException e) {
 			Trace.trace(Trace.SEVERE, "Error starting server " + Server.this.toString(), e);
 			throw e;
 		}
 	}
 
 	protected IStatus stopImpl(boolean force, IProgressMonitor monitor) {
 		final Object mutex = new Object();
 		
 		// add listener to the server
 		IServerListener listener = new IServerListener() {
 			public void serverChanged(ServerEvent event) {
 				int eventKind = event.getKind();
 				IServer server = event.getServer();
 				if (eventKind == (ServerEvent.SERVER_CHANGE | ServerEvent.STATE_CHANGE)) {
 					int state = server.getServerState();
 					if (Server.this == server && (state == IServer.STATE_STOPPED || state == IServer.STATE_STARTED)) {
 						// notify waiter
 						synchronized (mutex) {
 							try {
 								mutex.notifyAll();
 							} catch (Exception e) {
 								Trace.trace(Trace.SEVERE, "Error notifying server stop", e);
 							}
 						}
 					}
 				}
 			}
 		};
 		addServerListener(listener);
 		
 		class Timer {
 			boolean timeout;
 			boolean alreadyDone;
 		}
 		final Timer timer = new Timer();
 		
 		final int serverTimeout = getStopTimeout() * 1000;
 		if (serverTimeout > 0) {
 			Thread thread = new Thread("Server Stop Timeout") {
 				public void run() {
 					try {
 						Thread.sleep(serverTimeout);
 						if (!timer.alreadyDone) {
 							timer.timeout = true;
 							// notify waiter
 							synchronized (mutex) {
 								Trace.trace(Trace.FINEST, "stop notify timeout");
 								mutex.notifyAll();
 							}
 						}
 					} catch (Exception e) {
 						Trace.trace(Trace.SEVERE, "Error notifying server stop timeout", e);
 					}
 				}
 			};
 			thread.setDaemon(true);
 			thread.start();
 		}
 		
 		// stop the server
 		stopImpl2(force);
 		
 		// wait for it! wait for it!
 		synchronized (mutex) {
 			try {
 				while (!timer.timeout && getServerState() != IServer.STATE_STOPPED &&
 						getServerState() != IServer.STATE_STARTED)
 					mutex.wait();
 			} catch (Exception e) {
 				Trace.trace(Trace.SEVERE, "Error waiting for server stop", e);
 			}
 		}
 		removeServerListener(listener);
 		
 		//can't throw exceptions
 		/*if (timer.timeout)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorStopTimeout, getName()), null);
 		else
 			timer.alreadyDone = true;
 		*/
 		if (!monitor.isCanceled() && getServerState() == IServer.STATE_STARTED)
 			return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorStopFailed, getName()), null);
 		
 		return Status.OK_STATUS;
 	}
 
 	protected void stopImpl2(boolean force) {
 		if (getServerState() == STATE_STOPPED)
 			return;
 		
 		Trace.trace(Trace.FINEST, "Stopping server: " + Server.this.toString());
 		
 		try {
 			getBehaviourDelegate(null).stop(force);
 		} catch (RuntimeException e) {
 			Trace.trace(Trace.SEVERE, "Error calling delegate stop() " + Server.this.toString(), e);
 			throw e;
 		} catch (Throwable t) {
 			Trace.trace(Trace.SEVERE, "Error calling delegate stop() " + Server.this.toString(), t);
 			throw new RuntimeException(t);
 		}
 	}
 
 	public boolean contains(ISchedulingRule rule) {
 		return (rule instanceof IServer || rule instanceof ServerSchedulingRule);
 	}
 
 	public boolean isConflicting(ISchedulingRule rule) {
 		if (!(rule instanceof IServer) && !(rule instanceof ServerSchedulingRule))
 			return false;
 		
 		if (rule instanceof IServer) {
 			IServer s = (IServer) rule;
 			return this.equals(s);
 		}
 		ServerSchedulingRule ssrule = (ServerSchedulingRule) rule;
 		return ssrule.server.equals(this);
 	}
 
 	public String toString() {
 		return getName();
 	}
 }
