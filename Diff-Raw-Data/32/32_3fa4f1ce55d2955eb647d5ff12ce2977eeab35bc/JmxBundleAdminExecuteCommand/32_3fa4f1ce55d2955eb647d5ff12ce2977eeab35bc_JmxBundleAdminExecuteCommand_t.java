 /*******************************************************************************
  * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.runtime.internal.core.command;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.concurrent.TimeoutException;
 
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 
 import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
 import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
 
 
 /**
  * @author Christian Dupuis
  */
 public class JmxBundleAdminExecuteCommand extends AbstractJmxServerCommand implements
 		IServerCommand<String> {
 
 	private final String cmdLine;
 
 	public JmxBundleAdminExecuteCommand(IServerBehaviour serverBehaviour, String cmdLine) {
 		super(serverBehaviour);
 		this.cmdLine = cmdLine;
 	}
 
 	public String execute() throws IOException, TimeoutException {
 
 		return (String) execute(new JmxServerCommandTemplate() {
 
 			public Object invokeOperation(MBeanServerConnection connection) throws Exception {
 				ObjectName name = ObjectName
						.getInstance("org.eclipse.virgo.kernel:type=BundleAdmin");
 
 				// Verify that the BundleAdmin exists and runs
 				checkBundleAdminAndInstall(serverBehaviour, connection, name);
 
 				return connection.invoke(name, "execute", new Object[] { cmdLine },
 						new String[] { String.class.getName() });
 			}
 
 		});
 	}
 	
 	private static void checkBundleAdminAndInstall(IServerBehaviour behaviour, MBeanServerConnection connection,
 			ObjectName name) throws IOException, TimeoutException, URISyntaxException {
 		try {
 			// Check if BundleAdmin MBean is registered
 			connection.getObjectInstance(name);
 		}
 		catch (InstanceNotFoundException e) {
 			// Install the BundleAdmin bundle
 			behaviour.getVersionHandler().getServerDeployCommand(behaviour,
 					ServerCorePlugin.getDefault().getConnectorBundleUri()).execute();
 		}
 	}
 
 }
