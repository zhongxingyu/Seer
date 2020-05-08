 package org.eclipse.dltk.launching;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.core.IDbgpService;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTargetListener;
 import org.eclipse.dltk.debug.internal.core.model.ScriptDebugTarget;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.launching.debug.DbgpConstants;
 import org.eclipse.dltk.launching.debug.IDebuggingEngine;
 
 public class DebuggingEngineRunner extends AbstractInterpreterRunner {
 	protected static final int DEFAULT_WAITING_TIMEOUT = 1000 * 1000;
 
 	protected static final int DEFAULT_PAUSE = 500;
 
 	private IDebuggingEngine engine;
 
 	private static void sleep(long millis) {
 		try {
 			Thread.sleep(millis);
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 		}
 	}
 
 	protected String getPluginId() {
 		return DLTKLaunchingPlugin.ID_PLUGIN;
 	}
 
 	public static class ScriptDebugTargetWaiter implements
 			IScriptDebugTargetListener {
 		private IScriptDebugTarget target;
 
 		public ScriptDebugTargetWaiter(IScriptDebugTarget target) {
 			if (target == null) {
 				throw new IllegalArgumentException();
 			}
 
 			this.target = target;
 		}
 
 		public synchronized void targetInitialized() {
 			notifyAll();
 		}
 
 		public synchronized boolean waitThread(int timeout) {
 			target.addListener(this);
 
 			try {
 				wait(timeout);
 			} catch (InterruptedException e) {
 				Thread.interrupted();
 			}
 
 			target.removeListener(this);
 			return target.isInitialized();
 		}
 	}
 
 	protected String generateSessionId() {
 		return "dbgp" + System.currentTimeMillis();
 	}
 
 	protected String getSessionId(ILaunchConfiguration configuration)
 			throws CoreException {
 		String id = configuration.getAttribute(
 				IDLTKLaunchConfigurationConstants.ATTR_DLTK_DBGP_SESSION_ID,
 				(String) null);
 
 		if (id == null) {
 			id = generateSessionId();
 		}
 
 		return id;
 	}
 
 	protected void addDebugTarget(ILaunch launch,
 			ILaunchConfiguration configuration, IDbgpService dbgpService,
 			String sessionId) throws CoreException {
 
 		IScriptDebugTarget target = new ScriptDebugTarget(engine.getModelId(),
 				dbgpService, sessionId, launch, null);
 		launch.addDebugTarget(target);
 	}
 
 	public DebuggingEngineRunner(IInterpreterInstall install,
 			IDebuggingEngine engine) {
 		super(install);
 
 		if (engine == null) {
 			throw new IllegalArgumentException();
 		}
 
 		this.engine = engine;
 	}
 
 	protected IDbgpService createDebuggingService(
 			ILaunchConfiguration configuration) throws CoreException {
 		try {
 			int port = configuration.getAttribute(
 					IDLTKLaunchConfigurationConstants.ATTR_DLTK_DBGP_PORT, -1);
 
 			if (port == -1) {
 				return DLTKDebugPlugin.getDefault().createDbgpService();
 			} else {
 				return DLTKDebugPlugin.getDefault().creaeDbgpService(port);
 			}
 		} catch (Exception e) {
 			abort(DLTKLaunchingPlugin.ID_PLUGIN, "Dbgp service not available",
 					null, DLTKLaunchingPlugin.DBGP_SERVICE_NOT_AVAILABLE);
 		}
 
 		return null;
 	}
 
 	public void run(InterpreterConfig config, ILaunch launch,
 			IProgressMonitor monitor) throws CoreException {
 
 		try {
 			final ILaunchConfiguration configuration = launch
 					.getLaunchConfiguration();
 
 			final IDbgpService service = createDebuggingService(configuration);
 			final String sessionId = getSessionId(configuration);
 
 			addDebugTarget(launch, configuration, service, sessionId);
 
 			final int port = service.getPort();
 			final String host = "127.0.0.1";
 
 			config.setProperty(DbgpConstants.HOST_PROP, host);
 			config.setProperty(DbgpConstants.PORT_PROP, Integer.toString(port));
 			config.setProperty(DbgpConstants.SESSION_ID_PROP, sessionId);
 
 			InterpreterConfig newConfig = engine.getConfigModifier().modify(
 					constructProgramString(), config);
 
 			sleep(DEFAULT_PAUSE);
 
 			try {
				newConfig.setProperty("NO_PROCESS", "true");
 				super.run(newConfig, launch, monitor);
 			} catch (CoreException e) {
 				abort(DLTKLaunchingPlugin.ID_PLUGIN,
 						"Debugging engine not started", null,
 						DLTKLaunchingPlugin.DEBUGGING_ENGINE_NOT_STARTED);
 			}
 
 			int waitingTimeout = configuration
 					.getAttribute(
 							IDLTKLaunchConfigurationConstants.ATTR_DLTK_DBGP_WAITING_TIMEOUT,
 							DEFAULT_WAITING_TIMEOUT);
 
 			ScriptDebugTargetWaiter waiter = new ScriptDebugTargetWaiter(
 					(IScriptDebugTarget) launch.getDebugTarget());
 
 			if (!waiter.waitThread(waitingTimeout)) {
 				abort(DLTKLaunchingPlugin.ID_PLUGIN,
 						"Debugging engine not connected", null,
 						DLTKLaunchingPlugin.DEBUGGING_ENGINE_NOT_CONNECTED);
 			}
 		} catch (CoreException e) {
 			launch.terminate();
 			throw e;
 		}
 
 		// Happy debugging :)
 	}
 }
