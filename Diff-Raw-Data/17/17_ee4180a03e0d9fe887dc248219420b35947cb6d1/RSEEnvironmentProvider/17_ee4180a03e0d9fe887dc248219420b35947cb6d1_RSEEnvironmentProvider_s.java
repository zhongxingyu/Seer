 package org.eclipse.dltk.core.internal.rse;
 
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IEnvironmentProvider;
 import org.eclipse.dltk.core.internal.rse.perfomance.RSEPerfomanceStatistics;
 import org.eclipse.rse.core.IRSESystemType;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.core.model.SystemStartHere;
 import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
 import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
 
 public class RSEEnvironmentProvider implements IEnvironmentProvider {
 	public static final String RSE_ENVIRONMENT_PREFIX = DLTKRSEPlugin.PLUGIN_ID
 			+ ".rseEnvironment.";
 
 	public RSEEnvironmentProvider() {
 	}
 
 	boolean fakeRSEInitialized = false;
 
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
 		IHost[] connections = SystemStartHere.getConnections();
 		for (int i = 0; i < connections.length; i++) {
 			IHost connection = connections[i];
 			if (name.equals(connection.getAliasName())) {
 				return connection;
 			}
 		}
 		return null;
 	}
 
 	public IEnvironment[] getEnvironments() {
		System.out.println("ASK for RSE");
 		IHost[] connections = SystemStartHere.getConnections();
 		List environments = new LinkedList();
 		if (connections != null) {
 			for (int i = 0; i < connections.length; i++) {
 				IHost connection = connections[i];
 				IRSESystemType systemType = connection.getSystemType();
 				if (systemType == null || systemType.isWindows()
 						|| systemType.isLocal()) {
 					continue;
 				}
 
 				IRemoteFileSubSystem fs = RemoteFileUtility
 						.getFileSubSystem(connection);
 				if (fs != null)
 					environments.add(new RSEEnvironment(fs));
 			}
 		}
 		return (IEnvironment[]) environments
 				.toArray(new IEnvironment[environments.size()]);
 	}
 
 	public void waitInitialized() {
 	}
 
 	public IEnvironment getProjectEnvironment(IProject project) {
 		if (RSEPerfomanceStatistics.PERFOMANCE_TRACING) {
 			RSEPerfomanceStatistics
 					.inc(RSEPerfomanceStatistics.HAS_PROJECT_EXECUTIONS);
 		}
 		long start = System.currentTimeMillis();
 		try {
 			if (!project.isAccessible()) {
 				return null;
 			}
 			IProjectDescription description;
 			try {
 				description = project.getDescription();
 				URI uri = description.getLocationURI();
 				if (uri != null) {
 					String scheme = uri.getScheme();
 					if (!"rse".equalsIgnoreCase(scheme)) {
 						return null;
 					}
 					String uriHost = uri.getHost();
 					IEnvironment[] rseEnvironments = getEnvironments();
 					for (int i = 0; i < rseEnvironments.length; i++) {
 						RSEEnvironment rseEnvironment = (RSEEnvironment) rseEnvironments[i];
 						if (rseEnvironment.getHost().getHostName()
 								.equalsIgnoreCase(uriHost)) {
 							return rseEnvironment;
 						}
 
 					}
 				}
 			} catch (CoreException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 			return null;
 		} finally {
 			long end = System.currentTimeMillis();
 			if (RSEPerfomanceStatistics.PERFOMANCE_TRACING) {
 				RSEPerfomanceStatistics.inc(
 						RSEPerfomanceStatistics.HAS_POJECT_EXECUTIONS_TIME,
 						(end - start));
 			}
 		}
 	}
 
 }
