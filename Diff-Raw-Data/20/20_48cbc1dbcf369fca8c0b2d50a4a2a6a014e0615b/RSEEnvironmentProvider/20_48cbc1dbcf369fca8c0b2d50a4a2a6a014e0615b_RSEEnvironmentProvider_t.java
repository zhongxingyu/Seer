 package org.eclipse.dltk.core.internal.rse;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IEnvironmentProvider;
 import org.eclipse.dltk.core.internal.rse.perfomance.RSEPerfomanceStatistics;
 import org.eclipse.rse.core.IRSESystemType;
 import org.eclipse.rse.core.RSECorePlugin;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.core.model.SystemStartHere;
 import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
 import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
 
 public class RSEEnvironmentProvider implements IEnvironmentProvider {
 
 	/**
 	 * @since 2.0
 	 */
 	public static final String ID = "org.eclipse.dltk.rse.RSEEnvironmentProvider"; //$NON-NLS-1$
 
 	public static final String RSE_SCHEME = "rse"; //$NON-NLS-1$
 
	public static final String RSE_ENVIRONMENT_PREFIX = DLTKRSEPlugin.NAMESPACE
 			+ ".rseEnvironment."; //$NON-NLS-1$
 
 	public RSEEnvironmentProvider() {
 	}
 
 	public String getProviderName() {
 		return Messages.RSEEnvironmentProvider_providerName;
 	}
 
 	public IEnvironment getEnvironment(String envId) {
 		return getEnvironment(envId, true);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public IEnvironment getEnvironment(String envId, boolean lazy) {
 		if (envId.startsWith(RSE_ENVIRONMENT_PREFIX)) {
 			String name = envId.substring(RSE_ENVIRONMENT_PREFIX.length());
 			IHost connection = getRSEConnection(name);
 			if (connection != null) {
 				IRemoteFileSubSystem fs = RemoteFileUtility
 						.getFileSubSystem(connection);
 				if (fs != null)
 					return new RSEEnvironment(fs);
 			} else if (lazy) {
 				return new RSELazyEnvironment(envId, this);
 			}
 		}
 		return null;
 	}
 
 	private IHost getRSEConnection(String name) {
 		if (isReady(false)) {
 			IHost[] connections = SystemStartHere.getConnections();
 			for (int i = 0; i < connections.length; i++) {
 				IHost connection = connections[i];
 				if (name.equals(connection.getAliasName())) {
 					return connection;
 				}
 			}
 		}
 		return null;
 	}
 
 	private final Object lock = new Object();
 	private boolean waitTimeoutReached = false;
 	private static final int MAX_WAIT_COUNT = 20;
 	private static final int WAIT_INTERVAL = 500;
 	private boolean initialized = false;
 	private InitThread initThread = null;
 	private static final long RSE_INIT_THREAD_TIMEOUT = 60 * 1000;
 
 	private static final boolean DEBUG = false;
 
 	private static class WatchdogThread extends Job {
 		private final Thread thread;
 
 		public WatchdogThread(Thread thread) {
 			super(RSEEnvironmentProvider.class.getSimpleName()
 					+ "-WatchdogThread"); //$NON-NLS-1$			
 			this.thread = thread;
 			setSystem(true);
 		}
 
 		@Override
 		public IStatus run(IProgressMonitor monitor) {
 			if (thread.isAlive()) {
 				if (DEBUG)
 					System.out.println("InitThread.interrupt()"); //$NON-NLS-1$
 				thread.interrupt();
 			} else {
 				if (DEBUG)
 					System.out.println("InitThread is already terminated"); //$NON-NLS-1$
 			}
 			return Status.OK_STATUS;
 		}
 
 	}
 
 	private class InitThread extends Thread {
 
 		@Override
 		public void run() {
 			final WatchdogThread watchdog = new WatchdogThread(this);
 			watchdog.schedule(RSE_INIT_THREAD_TIMEOUT);
 			try {
 				if (DEBUG)
 					System.out.println(Thread.currentThread().getName()
 							+ " started"); //$NON-NLS-1$
 				RSECorePlugin.waitForInitCompletion();
 				if (DEBUG)
 					System.out.println(Thread.currentThread().getName()
 							+ " finished"); //$NON-NLS-1$
 				watchdog.cancel();
 			} catch (InterruptedException e) {
 				if (DLTKCore.DEBUG || DEBUG) {
 					e.printStackTrace();
 				}
 			} finally {
 				synchronized (lock) {
 					initialized = true;
 					initThread = null;
 					lock.notifyAll();
 				}
 			}
 			for (IEnvironment environment : getEnvironments()) {
 				fireAdded(environment);
 			}
 		}
 
 	}
 
 	private final Set<String> firedEnvironmentIds = new HashSet<String>();
 
 	/**
 	 * @since 2.0
 	 */
 	public void fireAdded(IEnvironment environment) {
 		final boolean added;
 		synchronized (firedEnvironmentIds) {
 			added = firedEnvironmentIds.add(environment.getId());
 		}
 		if (added) {
 			if (DEBUG) {
 				System.out.println("added " + environment.getId()); //$NON-NLS-1$
 			}
 			EnvironmentManager.environmentAdded(environment);
 		}
 	}
 
 	public boolean isInitialized() {
 		return isReady(false);
 	}
 
 	private boolean isReady() {
 		return isReady(true);
 	}
 
 	private boolean isReady(boolean allowWait) {
 		synchronized (lock) {
 			if (initialized) {
 				if (RSECorePlugin.getDefault() == null) { // Shutdown process
 					return false;
 				}
 				return true;
 			}
 			boolean newThread = false;
 			if (initThread == null) {
 				newThread = true;
 				initThread = new InitThread();
 				initThread.setName(getClass().getSimpleName() + "-InitThread"); //$NON-NLS-1$
 				initThread.setDaemon(true);
 				initThread.start();
 				if (DEBUG)
 					System.out.println("start initThread"); //$NON-NLS-1$
 			}
 			if (allowWait) {
 				if (DEBUG)
 					System.out.println("wait initThread"); //$NON-NLS-1$
 				try {
 					lock.wait(newThread ? 250 : 100);
 				} catch (InterruptedException e) {
 					if (DLTKCore.DEBUG) {
 						e.printStackTrace();
 					}
 				}
 				if (initialized) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean isSupportedConnection(IHost connection) {
 		final IRSESystemType systemType = connection.getSystemType();
 		if (systemType == null || systemType.isWindows()
 				|| systemType.isLocal()) {
 			return false;
 		}
 		return true;
 	}
 
 	public IEnvironment[] getEnvironments() {
 		if (isReady()) {
 			final IHost[] connections = SystemStartHere.getConnections();
 			if (connections != null && connections.length != 0) {
 				final List<IEnvironment> environments = new ArrayList<IEnvironment>(
 						connections.length);
 				for (int i = 0; i < connections.length; i++) {
 					final IHost connection = connections[i];
 					if (isSupportedConnection(connection)) {
 						final IRemoteFileSubSystem fs = RemoteFileUtility
 								.getFileSubSystem(connection);
 						if (fs != null)
 							environments.add(new RSEEnvironment(fs));
 					}
 				}
 				return environments.toArray(new IEnvironment[environments
 						.size()]);
 			}
 		}
 		return new IEnvironment[0];
 	}
 
 	public void waitInitialized() {
 		try {
 			int waitCount = 0;
 			while (!isReady(false)) {
 				synchronized (lock) {
 					if (waitTimeoutReached) {
 						break;
 					}
 					if (++waitCount > MAX_WAIT_COUNT) {
 						waitTimeoutReached = true;
 						if (DEBUG)
 							System.out.println(Thread.currentThread().getName()
 									+ " - waitInitialized - timeout reached"); //$NON-NLS-1$						
 						break;
 					}
 					lock.wait(WAIT_INTERVAL);
 				}
 			}
 		} catch (InterruptedException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public IEnvironment getProjectEnvironment(IProject project) {
 		if (RSEPerfomanceStatistics.PERFOMANCE_TRACING) {
 			RSEPerfomanceStatistics
 					.inc(RSEPerfomanceStatistics.HAS_PROJECT_EXECUTIONS);
 		}
 		final long start = System.currentTimeMillis();
 		try {
 			if (project.isAccessible()) {
 				try {
 					final URI uri = project.getDescription().getLocationURI();
 					if (uri != null
 							&& RSE_SCHEME.equalsIgnoreCase(uri.getScheme())
 							&& isReady()) {
 						final IHost[] connections = SystemStartHere
 								.getConnections();
 						if (connections != null) {
 							final String projectHost = uri.getHost();
 							for (int i = 0; i < connections.length; i++) {
 								final IHost connection = connections[i];
 								if (isSupportedConnection(connection)
 										&& projectHost
 												.equalsIgnoreCase(connection
 														.getHostName())) {
 									final IRemoteFileSubSystem fs = RemoteFileUtility
 											.getFileSubSystem(connection);
 									if (fs != null)
 										return new RSEEnvironment(fs);
 								}
 							}
 						}
 					}
 				} catch (CoreException e) {
 					if (DLTKCore.DEBUG) {
 						e.printStackTrace();
 					}
 				}
 			}
 			return null;
 		} finally {
 			if (RSEPerfomanceStatistics.PERFOMANCE_TRACING) {
 				final long end = System.currentTimeMillis();
 				RSEPerfomanceStatistics.inc(
 						RSEPerfomanceStatistics.HAS_POJECT_EXECUTIONS_TIME,
 						(end - start));
 			}
 		}
 	}
 
 	/*
 	 * @see IEnvironmentProvider#getEnvironment(java.net.URI)
 	 */
 	public IEnvironment getEnvironment(URI locationURI) {
 		if (RSE_SCHEME.equalsIgnoreCase(locationURI.getScheme()) && isReady()) {
 			final IHost[] connections = SystemStartHere.getConnections();
 			if (connections != null) {
 				final String projectHost = locationURI.getHost();
 				for (int i = 0; i < connections.length; i++) {
 					final IHost connection = connections[i];
 					if (isSupportedConnection(connection)
 							&& projectHost.equalsIgnoreCase(connection
 									.getHostName())) {
 						final IRemoteFileSubSystem fs = RemoteFileUtility
 								.getFileSubSystem(connection);
 						if (fs != null)
 							return new RSEEnvironment(fs);
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 }
