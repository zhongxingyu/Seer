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
 package org.eclipse.virgo.ide.runtime.internal.core;
 
 import java.io.File;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
 import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
 import org.eclipse.jdt.launching.ExecutionArguments;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.IVMRunner;
 import org.eclipse.jdt.launching.VMRunnerConfiguration;
 import org.eclipse.jst.server.core.ServerProfilerDelegate;
 import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
 import org.eclipse.virgo.ide.runtime.core.ServerUtils;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.internal.ServerPreferences;
 
 /**
  * {@link ILaunchConfigurationDelegate} for the dm server.
  * 
  * @author Christian Dupuis
  * @since 1.0.0
  */
 @SuppressWarnings("restriction")
 public class ServerLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate {
 
 	@SuppressWarnings("unchecked")
 	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
 			throws CoreException {
 		IServer server = ServerUtil.getServer(configuration);
 		if (server == null) {
 			return;
 		}
 
 		IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 5);
 		checkCancelled(subMonitor);
 		subMonitor.beginTask("Starting SpringSource dm Server instance", 5);
 
 		if (server.shouldPublish() && ServerPreferences.getInstance().isAutoPublishing()) {
 			subMonitor.subTask("Publishing to staging directory...");
 			server.publish(IServer.PUBLISH_INCREMENTAL, monitor);
 		}
 		subMonitor.worked(1);
 		checkCancelled(subMonitor);
 		subMonitor.subTask("Configuring launch parameters...");
 
 		ServerBehaviour behaviour = (ServerBehaviour) server.loadAdapter(ServerBehaviour.class, null);
 
 		String mainTypeName = ServerUtils.getServer(behaviour).getRuntime().getRuntimeClass();
 
 		IVMInstall vm = verifyVMInstall(configuration);
 		IVMRunner runner = vm.getVMRunner(mode);
 		if (runner == null) {
 			runner = vm.getVMRunner(ILaunchManager.RUN_MODE);
 		}
 
 		File workingDir = verifyWorkingDirectory(configuration);
 		String workingDirName = null;
 		if (workingDir != null) {
 			workingDirName = workingDir.getAbsolutePath();
 		}
 
 		String pgmArgs = getProgramArguments(configuration);
 		String vmArgs = getVMArguments(configuration);
 		String[] envp = getEnvironment(configuration);
 
 		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
 
		Map<String, String> vmAttributesMap = getVMSpecificAttributesMap(configuration);
 
 		String[] classpath = getClasspath(configuration);
 
 		VMRunnerConfiguration runConfiguration = new VMRunnerConfiguration(mainTypeName, classpath);
 		runConfiguration.setProgramArguments(execArgs.getProgramArgumentsArray());
 		runConfiguration.setVMArguments(execArgs.getVMArgumentsArray());
 		runConfiguration.setWorkingDirectory(workingDirName);
 		runConfiguration.setEnvironment(envp);
 		runConfiguration.setVMSpecificAttributesMap(vmAttributesMap);
 
 		String[] bootpath = getBootpath(configuration);
 		if (bootpath != null && bootpath.length > 0) {
 			runConfiguration.setBootClassPath(bootpath);
 		}
 
 		subMonitor.worked(1);
 		checkCancelled(subMonitor);
 
 		subMonitor.subTask("Setting up source locator...");
 		setDefaultSourceLocator(launch, configuration);
 		subMonitor.worked(1);
 		checkCancelled(subMonitor);
 
 		if (ILaunchManager.PROFILE_MODE.equals(mode)) {
 			try {
 				subMonitor.subTask("Configuring TPTP profiling parameter...");
 				ServerProfilerDelegate.configureProfiling(launch, vm, runConfiguration, monitor);
 			} catch (CoreException ce) {
 				behaviour.stopServer();
 				throw ce;
 			}
 		}
 		subMonitor.worked(1);
 		checkCancelled(subMonitor);
 
 		subMonitor.subTask("Launching SpringSource dm Server...");
 		behaviour.setupLaunch(launch, mode, monitor);
 		launch.setAttribute(IServerBehaviour.PROPERTY_MBEAN_SERVER_IP, "127.0.0.1");
 
 		try {
 			runner.run(runConfiguration, launch, monitor);
 			behaviour.addProcessListener(launch.getProcesses()[0]);
 			subMonitor.worked(1);
 		} catch (Exception e) {
 		}
 	}
 
 	protected void checkCancelled(IProgressMonitor monitor) throws CoreException {
 		if (monitor.isCanceled()) {
 			throw new CoreException(Status.CANCEL_STATUS);
 		}
 	}
 
 }
