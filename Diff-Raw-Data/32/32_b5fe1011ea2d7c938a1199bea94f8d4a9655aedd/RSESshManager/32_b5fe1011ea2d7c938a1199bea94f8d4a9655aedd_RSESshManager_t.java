 package org.eclipse.dltk.core.internal.rse.ssh;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.dltk.core.internal.rse.DLTKRSEPlugin;
 import org.eclipse.dltk.ssh.core.ISshConnection;
 import org.eclipse.dltk.ssh.core.SshConnectionManager;
 import org.eclipse.rse.core.IRSESystemType;
 import org.eclipse.rse.core.PasswordPersistenceManager;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.core.model.SystemSignonInformation;
 import org.eclipse.rse.core.subsystems.IConnectorService;
 
 import com.jcraft.jsch.Session;
 
 public class RSESshManager {
 	/**
 	 * Create or return Ssh connection for specified remote host.
 	 * 
 	 * Right now support only stored ssh passwords retrieval.
 	 * 
 	 */
 	public static synchronized ISshConnection getConnection(IHost host) {
 		IConnectorService[] connectorServices = host.getConnectorServices();
 		IRSESystemType systemType = host.getSystemType();
 
 		// Only ssh connections are supported
 		if (!systemType.getId().equals(IRSESystemType.SYSTEMTYPE_SSH_ONLY_ID)) {
 			return null;//
 		}
 		for (IConnectorService connector : connectorServices) {
 			String hostName = host.getHostName();
 			// Retrive user name
 			String userId = connector.getUserId();
 			String location = userId + "@" + host.getHostName();
 			ISshConnection connection = SshConnectionManager
 					.getConnection(location);
 			if (connection.isConnected()) {
 				return connection;
 			}
 			// Try to find password and specify it for Ssh Connection.
 			SystemSignonInformation information = PasswordPersistenceManager
 					.getInstance().find(host.getSystemType(),
 							host.getHostName(), userId);
 			if (information != null && information.getPassword() != null) {
 				connection.setPassword(information.getPassword());
 				connection.connect();
 				if (connection.isConnected()) {
 					return connection;
 				}
 			}

 			// Try to resolve not persisted password from SSh connector
			try {
				if (!connector.isConnected()) {
					connector.connect(new NullProgressMonitor());
				}
			} catch (Exception e) {
				DLTKRSEPlugin.log(e);
			}
			if (!connector.isConnected()) {
				return null;
			}

 			String name = connector.getClass().getName();
 			if ("org.eclipse.rse.internal.connectorservice.ssh.SshConnectorService"
 					.equals(name)) {
 				// Ssh is available
 				try {
 					Method method = connector.getClass().getMethod(
 							"getSession", null);
 					if (method != null) {
 						Object invoke = method.invoke(connector, null);
 						if (invoke instanceof Session) {
 							Session session = (Session) invoke;
 							connection.setPassword(session.getUserInfo()
 									.getPassword());
 							connection.connect();
 							if (connection.isConnected()) {
 								return connection;
 							}
 						}
 					}
 				} catch (SecurityException e) {
 					e.printStackTrace();
 				} catch (NoSuchMethodException e) {
 					e.printStackTrace();
 				} catch (IllegalArgumentException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (InvocationTargetException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
			// Try to connect any way
			connection.connect();
			if (connection.isConnected()) {
				return connection;
			}
			// Set connection to disabled state for 10 seconds
			connection.setDisabled(10000);
 		}
 		System.out.println("Failed to create direct ssh connection for:"
 				+ host.toString());
 		return null;
 	}
 }
