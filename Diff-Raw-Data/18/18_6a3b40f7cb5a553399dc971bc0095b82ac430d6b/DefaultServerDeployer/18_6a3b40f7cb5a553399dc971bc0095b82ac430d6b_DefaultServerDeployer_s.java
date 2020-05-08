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
 package org.eclipse.virgo.ide.runtime.internal.core;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
 import org.eclipse.virgo.ide.facet.core.FacetUtils;
 import org.eclipse.virgo.ide.manifest.core.BundleManifestCorePlugin;
 import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
 import org.eclipse.virgo.ide.runtime.core.IServerDeployer;
 import org.eclipse.virgo.ide.runtime.core.ServerUtils;
 import org.eclipse.virgo.ide.runtime.internal.core.command.IServerCommand;
import org.eclipse.virgo.ide.runtime.internal.core.utils.StatusUtil;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.model.IModuleFile;
 
 /**
  * Deployer helper that hides the JMX-based communication with a running dm Server.
  * @author Christian Dupuis
  * @since 2.0.0
  */
 public class DefaultServerDeployer implements IServerDeployer {
 
 	private IServerBehaviour behaviour;
 
 	public DefaultServerDeployer(IServerBehaviour behaviour) {
 		this.behaviour = behaviour;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void deploy(IModule... modules) {
 		List<IModule> orderedModules = Arrays.asList(modules);
 
 		// make sure we honor the user configured order
 		final List<String> orderedArtefacts = getArtefactOrder();
 
 		// sort the modules according the order defined in the server configuration
 		Collections.sort(orderedModules, new Comparator<IModule>() {
 
 			public int compare(IModule o1, IModule o2) {
 				Integer m1 = (orderedArtefacts.contains(o1.getId()) ? orderedArtefacts.indexOf(o1.getId())
 						: Integer.MAX_VALUE);
 				Integer m2 = (orderedArtefacts.contains(o2.getId()) ? orderedArtefacts.indexOf(o2.getId())
 						: Integer.MAX_VALUE);
 				return m1.compareTo(m2);
 			}
 		});
 
 		for (IModule module : orderedModules) {
 			DeploymentIdentity identity = executeDeployerCommand(getServerDeployCommand(module));
 			if (behaviour instanceof ServerBehaviour) {
 				((ServerBehaviour) behaviour).tail(identity);
 			}
 			behaviour.onModulePublishStateChange(new IModule[] { module }, IServer.PUBLISH_STATE_NONE);
 			// if (identity != null) {
 			// behaviour.onModuleStateChange(new IModule[] { module }, IServer.STATE_STARTED);
 			// }
 			// else {
 			// behaviour.onModuleStateChange(new IModule[] { module }, IServer.STATE_STOPPED);
 			// }
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Boolean ping() throws IOException, TimeoutException {
 		return getServerPingCommand().execute();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void redeploy(IModule module) {
 
 		DeploymentIdentity identity = behaviour.getDeploymentIdentities().get(module.getId());
 		if (identity == null) {
 			identity = executeDeployerCommand(getServerDeployCommand(module));
 		}
 		else {
 			// Special handling for pars with nested bundles to determine if only one of the bundles
 			// refreshing
 			if (FacetUtils.isParProject(module.getProject())) {
 
 				Set<String> bundleSymbolicNames = new LinkedHashSet<String>();
 				IModule[] children = getModuleChildren(module);
 				// If only one module has actual changes; emit a bundle refresh only
 				for (IModule child : children) {
 					int state = getModuleState(module, child);
 					if (state != IServer.PUBLISH_STATE_NONE) {
 						if (FacetUtils.isBundleProject(child.getProject())) {
 							BundleManifest manifest = BundleManifestCorePlugin.getBundleManifestManager()
 									.getBundleManifest(JavaCore.create(child.getProject()));
 							if (manifest != null && manifest.getBundleSymbolicName() != null) {
 								bundleSymbolicNames.add(manifest.getBundleSymbolicName().getSymbolicName());
 							}
 						}
 					}
 				}
 
 				// Only one changed bundle found -> update single one
 				if (bundleSymbolicNames.size() == 1) {
 					executeDeployerCommand(getServerRefreshCommand(module, bundleSymbolicNames.iterator().next()));
 				}
 				else {
 					identity = executeDeployerCommand(getServerDeployCommand(module));
 				}
 			}
 			else {
 				identity = executeDeployerCommand(getServerDeployCommand(module));
 			}
 		}
 
 		// if (identity != null) {
 		// behaviour.onModuleStateChange(new IModule[] { module }, IServer.STATE_STARTED);
 		// }
 		// else {
 		// behaviour.onModuleStateChange(new IModule[] { module }, IServer.STATE_STOPPED);
 		// }
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected IModule[] getModuleChildren(IModule module) {
 		return ServerUtils.getServer(behaviour).getChildModules(new IModule[] { module });
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected int getModuleState(IModule module, IModule child) {
 		return ServerUtils.getServer(behaviour).getServer().getModulePublishState(new IModule[] { module, child });
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected List<String> getArtefactOrder() {
 		return ServerUtils.getServer(behaviour).getConfiguration().getArtefactOrder();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void refresh(IModule parModule, IModule... modules) {
 		for (IModule module : modules) {
 			BundleManifest manifest = BundleManifestCorePlugin.getBundleManifestManager().getBundleManifest(
 					JavaCore.create(module.getProject()));
 			if (manifest != null && manifest.getBundleSymbolicName() != null) {
 				executeDeployerCommand(getServerRefreshCommand(parModule, manifest.getBundleSymbolicName()
 						.getSymbolicName()));
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void refreshStatic(IModule module, IModuleFile file) {
 
 		DeploymentIdentity identity = behaviour.getDeploymentIdentities().get(module.getId());
 		if (identity != null) {
 
 			if (FacetUtils.isParProject(module.getProject()) || FacetUtils.isPlanProject(module.getProject())) {
 				IFile f = (IFile) file.getAdapter(IFile.class);
 				if (f != null) {
 					if (FacetUtils.isBundleProject(f.getProject())
 							|| FacetUtils.hasProjectFacet(f.getProject(), FacetCorePlugin.WEB_FACET_ID)) {
 						BundleManifest manifest = BundleManifestCorePlugin.getBundleManifestManager()
 								.getBundleManifest(JavaCore.create(f.getProject()));
 						String symbolicName = null;
 						if (manifest != null && manifest.getBundleSymbolicName() != null) {
 							symbolicName = manifest.getBundleSymbolicName().getSymbolicName();
 						}
 						else {
 							symbolicName = f.getProject().getName();
 						}
 						String projectPath = f.getProject().getName();
 						String path = file.getModuleRelativePath().toString();
 
 						// add 5 for .jar or .war + / (see ServerModuleDelegate#members())
 						if (path.length() > projectPath.length() + 5) {
 							path = path.substring(projectPath.length() + 5);
 							executeDeployerCommand(getServerUpdateCommand(module, file, identity, symbolicName, path));
 						}
 						else {
 							executeDeployerCommand(getServerUpdateCommand(module, file, identity, symbolicName, ""));
 						}
 					}
 				}
 			}
 			else {
 				executeDeployerCommand(getServerUpdateCommand(module, file, identity, null, file
 						.getModuleRelativePath().toString()));
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void shutdown() throws IOException, TimeoutException {
 		getServerShutdownCommand().execute();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void undeploy(IModule... modules) {
 		for (IModule module : modules) {
 			executeDeployerCommand(getServerUndeployCommand(module));
 			// behaviour.onModuleStateChange(new IModule[] { module }, IServer.STATE_STOPPED);
 		}
 	}
 
 	protected <T> T executeDeployerCommand(IServerCommand<T> serverCommand) {
 		try {
 			return (T) serverCommand.execute();
 		}
 		catch (IOException e) {
			StatusUtil.error("Failed execution of deployer command " + serverCommand, e);
 		}
 		catch (TimeoutException e) {
			StatusUtil.error("Failed execution of deployer command " + serverCommand, e);
 		}
 		return null;
 	}
 
 	protected IServerCommand<DeploymentIdentity> getServerDeployCommand(IModule module) {
 		return behaviour.getVersionHandler().getServerDeployCommand(behaviour, module);
 	}
 
 	protected IServerCommand<Boolean> getServerPingCommand() {
 		return behaviour.getVersionHandler().getServerPingCommand(behaviour);
 	}
 
 	protected IServerCommand<Void> getServerRefreshCommand(IModule module, String bundleSymbolicName) {
 		return behaviour.getVersionHandler().getServerRefreshCommand(behaviour, module, bundleSymbolicName);
 	}
 
 	protected IServerCommand<Void> getServerShutdownCommand() {
 		return behaviour.getVersionHandler().getServerShutdownCommand(behaviour);
 	}
 
 	protected IServerCommand<Void> getServerUndeployCommand(IModule module) {
 		return behaviour.getVersionHandler().getServerUndeployCommand(behaviour, module);
 	}
 
 	protected IServerCommand<Void> getServerUpdateCommand(IModule module, IModuleFile moduleFile,
 			DeploymentIdentity identity, String bundleSymbolicName, String targetPath) {
 		return behaviour.getVersionHandler().getServerUpdateCommand(behaviour, module, moduleFile, identity,
 				bundleSymbolicName, targetPath);
 	}
 
 }
