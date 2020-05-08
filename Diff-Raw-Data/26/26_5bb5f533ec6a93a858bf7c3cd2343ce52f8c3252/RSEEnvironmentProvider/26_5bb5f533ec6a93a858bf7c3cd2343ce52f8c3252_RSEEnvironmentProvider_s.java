 package org.eclipse.dltk.core.internal.rse;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IEnvironmentProvider;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.core.model.SystemStartHere;
 import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
 import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 
 public class RSEEnvironmentProvider implements IEnvironmentProvider {
 	public static final String RSE_ENVIRONMENT_PREFIX = DLTKRSEPlugin.PLUGIN_ID
 			+ ".rseEnvironment.";
 
 	public RSEEnvironmentProvider() {
 	}
 
 	boolean fakeRSEInitialized = false;
 
 	public IEnvironment getEnvironment(String envId) {
 		initializeRSE();
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
 		initializeRSE();
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
 		initializeRSE();
 		IHost[] connections = SystemStartHere.getConnections();
 		List environments = new LinkedList();
		for (int i = 0; i < connections.length; i++) {
			IHost connection = connections[i];
			if (connection.getSystemType().isWindows()
					|| connection.getSystemType().isLocal()) {
				continue;
			}
 
			IRemoteFileSubSystem fs = RemoteFileUtility
					.getFileSubSystem(connection);
			if (fs != null)
				environments.add(new RSEEnvironment(fs));
 		}
 		return (IEnvironment[]) environments
 				.toArray(new IEnvironment[environments.size()]);
 	}
 
 	/**
 	 * TODO: Hack to initialize RSE UI. Without UI initialized RSE not setup
 	 * connections.
 	 */
 	private void initializeRSE() {
 		if (!fakeRSEInitialized) {
 			fakeRSEInitialized = true;
 			Bundle bundle = Platform.getBundle("org.eclipse.rse.ui");
 			if (bundle != null) {
 				try {
 					bundle.start();
 				} catch (BundleException e) {
 					if (DLTKCore.DEBUG) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 }
