 /*******************************************************************************
  * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.runtime.internal.core.command;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.concurrent.TimeoutException;
 
 import javax.management.MBeanServerConnection;
 import javax.management.ObjectName;
 
 import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
 import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
 import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.model.IModuleFile;
 
 
 /**
  * {@link IServerCommand} to update/refresh a single static resource on dm Server 2.0.
  * @author Christian Dupuis
  * @since 2.2.2
  */
 public class JmxServerUpdateCommand extends AbstractJmxServerCommand implements IServerCommand<Void> {
 
 	/** Symbolic name of the bundle inside a PAR that contains the resource to update */
 	private final String bundleSymbolicName;
 
 	/** {@link DeploymentIdentity} of the deployed PAR or bundle */
 	private final DeploymentIdentity identity;
 
 	/** File to refresh or update */
 	private final IModuleFile moduleFile;
 
 	/** Module that needs refreshing */
 	private final IModule module;
 
 	/** Target path of the file relative to the PAR or bundle root */
 	private final String targetPath;
 	
 	private final String bundleObjectName;
 	
 	private final String parObjectName;
 
 	private final String planObjectName;
 
 	/**
 	 * Creates a new {@link JmxServerUpdateCommand}.
 	 */
 	public JmxServerUpdateCommand(IServerBehaviour serverBehaviour, IModule module, IModuleFile moduleFile,
 			DeploymentIdentity identity, String bundleSymbolicName, String targetPath, String bundleObjectName,
 			String parObjectName, String planObjectName) {
 		super(serverBehaviour);
 		this.bundleSymbolicName = bundleSymbolicName;
 		this.moduleFile = moduleFile;
 		this.module = module;
 		this.identity = identity;
 		this.targetPath = targetPath;
 		this.bundleObjectName = bundleObjectName;
 		this.parObjectName = parObjectName;
 		this.planObjectName = planObjectName;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Void execute() throws IOException, TimeoutException {
 
 		JmxServerCommandTemplate template = new JmxServerCommandTemplate() {
 
 			public Object invokeOperation(MBeanServerConnection connection) throws Exception {
 				ObjectName bundleObject = null;
 				if (bundleSymbolicName != null) {
					String partialObjectName = "com.springsource.kernel:artifact-type=bundle,name="
 							+ bundleSymbolicName + ",type=Model,version=";
 
 					String parObject = null;
 					// Figure out the bundle reference from par or plan dependents
 					if (module.getModuleType().getId().equals(FacetCorePlugin.PAR_FACET_ID)) {
 						parObject = parObjectName.replace("$VERSION", identity.getVersion())
 								.replace("$NAME", identity.getSymbolicName());
 					}
 					else {
 						parObject = planObjectName.replace("$VERSION", identity.getVersion())
 								.replace("$NAME", identity.getSymbolicName());
 					}
 
 					ObjectName name = ObjectName.getInstance(parObject);
 
 					ObjectName[] dependents = (ObjectName[]) connection
 							.getAttribute(name, getDependentsAttributeName());
 					for (ObjectName dependent : dependents) {
 						if (dependent.getCanonicalName().startsWith(partialObjectName)
 								|| dependent.getCanonicalName().contains("-" + bundleSymbolicName + ",type=Model")) {
 							bundleObject = dependent;
 							break;
 						}
 					}
 				}
 				else {
 					bundleObject = ObjectName.getInstance(bundleObjectName.replace("$VERSION",
 							identity.getVersion()).replace("$NAME", identity.getSymbolicName()));
 				}
 
 				// Update the resource
 				Object[] operationArguments = getUpdateOperationArguments();
 
 				String[] classNames = new String[operationArguments.length];
 				for (int i = 0; i < operationArguments.length; i++) {
 					if (operationArguments[i] instanceof Boolean) {
 						classNames[i] = boolean.class.getName();
 					}
 					else {
 						classNames[i] = operationArguments[i].getClass().getName();
 					}
 				}
 
 				return connection.invoke(bundleObject, getUpdateOperationName(operationArguments),
 						operationArguments, classNames);
 			}
 
 		};
 
 		execute(template);
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected Object[] getUpdateOperationArguments() {
 		URI uri = AbstractJmxServerDeployerCommand.getUri(serverBehaviour.getModuleDeployUri(module).append(
 				moduleFile.getModuleRelativePath()).append(moduleFile.getName()));
 		if (new File(uri).exists()) {
 			return new Object[] { uri.toString(), targetPath + "/" + moduleFile.getName() };
 		}
 		else {
 			return new Object[] { targetPath + "/" + moduleFile.getName() };
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected String getUpdateOperationName(Object[] arguments) {
 		if (arguments.length == 1) {
 			return "deleteEntry";
 		}
 		return "updateEntry";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected String getDependentsAttributeName() {
 		return "Dependents";
 	}
 
 }
