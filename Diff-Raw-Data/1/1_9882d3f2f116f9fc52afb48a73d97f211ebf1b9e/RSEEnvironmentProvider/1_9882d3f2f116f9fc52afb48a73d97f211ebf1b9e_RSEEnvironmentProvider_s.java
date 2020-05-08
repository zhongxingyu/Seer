 package org.eclipse.dltk.core.internal.rse;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
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
 
 	public static final String RSE_SCHEME = "rse"; //$NON-NLS-1$
 
 	public static final String RSE_ENVIRONMENT_PREFIX = DLTKRSEPlugin.PLUGIN_ID
 			+ ".rseEnvironment."; //$NON-NLS-1$
 
 	public RSEEnvironmentProvider() {
 	}
 
 	public String getProviderName() {
 		return Messages.RSEEnvironmentProvider_providerName;
 	}
 
 	public IEnvironment getEnvironment(String envId) {
 		if (envId.startsWith(RSE_ENVIRONMENT_PREFIX)) {
 			String name = envId.substring(RSE_ENVIRONMENT_PREFIX.length());
 			IHost connection = getRSEConnection(name);
 			if (connection != null) {
 				IRemoteFileSubSystem fs = RemoteFileUtility
 						.getFileSubSystem(connection);
 				if (fs != null)
 					return new RSEEnvironment(fs);
 			}
 		}
 		return null;
 	}
 
 	private IHost getRSEConnection(String name) {
 		if (isReady()) {
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
 	private boolean initialized = false;
 	private InitThread initThread = null;
 
 	private static final boolean DEBUG = false;
 
 	private class InitThread extends Thread {
 
 		public void run() {
 			try {
 				RSECorePlugin.waitForInitCompletion();
 			} catch (InterruptedException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			} finally {
 				synchronized (lock) {
 					initialized = true;
 					initThread = null;
 					lock.notifyAll();
 				}
 				EnvironmentManager.fireEnvirontmentChange();
 			}
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
 				return true;
 			}
 			boolean newThread = false;
 			if (initThread == null) {
 				newThread = true;
 				initThread = new InitThread();
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
 				final List environments = new ArrayList(connections.length);
 				for (int i = 0; i < connections.length; i++) {
 					final IHost connection = connections[i];
 					if (isSupportedConnection(connection)) {
 						final IRemoteFileSubSystem fs = RemoteFileUtility
 								.getFileSubSystem(connection);
 						if (fs != null)
 							environments.add(new RSEEnvironment(fs));
 					}
 				}
 				return (IEnvironment[]) environments
 						.toArray(new IEnvironment[environments.size()]);
 			}
 		}
 		return new IEnvironment[0];
 	}
 
 	public void waitInitialized() {
 		try {
 			while (!isReady(false)) {
 				synchronized (lock) {
 					lock.wait(1000);
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
 
 }
