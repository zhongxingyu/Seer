 /*******************************************************************************
  * Copyright (c) 2009, 2011 SpringSource, a divison of VMware, Inc. and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *     SAP AG - moving to Eclipse Libra project and enhancements
  *******************************************************************************/
 package org.eclipse.virgo.ide.runtime.internal.core.command;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Map;
 import java.util.concurrent.TimeoutException;
 
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 
 import org.eclipse.libra.framework.editor.core.model.IBundle;
 import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
 import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
 
 
 /**
  * @author Christian Dupuis
  * @author Kaloyan Raev
  */
 public class JmxBundleAdminServerCommand extends AbstractJmxServerCommand implements
 		IServerCommand<Map<Long, IBundle>> {
 
 	public JmxBundleAdminServerCommand(IServerBehaviour serverBehaviour) {
 		super(serverBehaviour);
 	}
 
 	@SuppressWarnings("unchecked")
 	public Map<Long, IBundle> execute() throws IOException, TimeoutException {
 
 		return (Map<Long, IBundle>) execute(new JmxServerCommandTemplate() {
 
 			public Object invokeOperation(MBeanServerConnection connection) throws Exception {
 				ObjectName name = ObjectName
						.getInstance("com.springsource.server:type=BundleAdmin");
 
 				// Verify that the BundleAdmin exists and runs
 				checkBundleAdminAndInstall(serverBehaviour, connection, name);
  
 				return connection.invoke(name, "retrieveBundles", null, null);
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
