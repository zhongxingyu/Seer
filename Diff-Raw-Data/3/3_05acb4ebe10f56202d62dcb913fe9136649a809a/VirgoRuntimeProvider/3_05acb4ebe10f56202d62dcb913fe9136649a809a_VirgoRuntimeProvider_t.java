 /*******************************************************************************
  * Copyright (c) 2010 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.runtime.internal.core.runtimes;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.libra.framework.editor.core.model.IBundle;
 import org.eclipse.virgo.ide.runtime.core.IServerBehaviour;
 import org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider;
 import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
 import org.eclipse.virgo.ide.runtime.core.ServerUtils;
 import org.eclipse.virgo.ide.runtime.internal.core.DeploymentIdentity;
 import org.eclipse.virgo.ide.runtime.internal.core.command.GenericJmxServerDeployCommand;
 import org.eclipse.virgo.ide.runtime.internal.core.command.IServerCommand;
 import org.eclipse.virgo.ide.runtime.internal.core.command.JmxBundleAdminExecuteCommand;
 import org.eclipse.virgo.ide.runtime.internal.core.command.JmxBundleAdminServerCommand;
 import org.eclipse.virgo.ide.runtime.internal.core.command.JmxServerDeployCommand;
 import org.eclipse.virgo.ide.runtime.internal.core.command.JmxServerPingCommand;
 import org.eclipse.virgo.ide.runtime.internal.core.command.JmxServerRefreshCommand;
 import org.eclipse.virgo.ide.runtime.internal.core.command.JmxServerShutdownCommand;
 import org.eclipse.virgo.ide.runtime.internal.core.command.JmxServerUndeployCommand;
 import org.eclipse.virgo.ide.runtime.internal.core.command.JmxServerUpdateCommand;
 import org.eclipse.virgo.util.common.StringUtils;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.model.IModuleFile;
 import org.eclipse.wst.server.core.util.PublishHelper;
 
 /**
  * {@link IServerRuntimeProvider} for Generic Virgo Server.
  * 
  * @author Terry Hon
  * @author Christian Dupuis
  * @author Miles Parker
  * @since 2.0.0
  */
 public abstract class VirgoRuntimeProvider implements IServerRuntimeProvider {
 
 	private static final String EXT_DIR = "ext";
 
 	private static final String USR_DIR = "usr";
 
 	private static final String REPOSITORY_DIR = "repository";
 
 	public static final String SERVER_VIRGO_BASE = "org.eclipse.virgo.server.runtime.virgo";
 
 	private static final String BUNDLE_OBJECT_NAME = "org.eclipse.virgo.kernel:type=Model,artifact-type=bundle,name=$NAME,version=$VERSION";
 
 	private static final String DEPLOYER_MBEAN_NAME = "org.eclipse.virgo.kernel:category=Control,type=Deployer";
 
 	private static final String PAR_OBJECT_NAME = "org.eclipse.virgo.kernel:type=Model,artifact-type=par,name=$NAME,version=$VERSION";
 
 	private static final String PLAN_OBJECT_NAME = "org.eclipse.virgo.kernel:type=Model,artifact-type=plan,name=$NAME,version=$VERSION";
 
 	private static final String RECOVERY_MONITOR_MBEAN_NAME = "org.eclipse.virgo.kernel:category=Control,type=RecoveryMonitor";
 
 	private static final String SHUTDOWN_MBEAN_NAME = "org.eclipse.virgo.kernel:type=Shutdown";
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getDeployerMBeanName() {
 		return DEPLOYER_MBEAN_NAME;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getRecoveryMonitorMBeanName() {
 		return RECOVERY_MONITOR_MBEAN_NAME;
 	}
 
 	/**
 	 * {@inheritDoc} Provides generic runtime arguments shared by all versions.
 	 */
 	public String[] getRuntimeVMArguments(IServerBehaviour behaviour, IPath installPath, IPath configPath,
 			IPath deployPath) {
 		String serverHome = ServerUtils.getServer(behaviour).getRuntimeBaseDirectory().toOSString();
 		String absConfigDir = serverHome + "/" + getConfigDir();
 		List<String> list = new ArrayList<String>();
 		list.add("-XX:+HeapDumpOnOutOfMemoryError");
 		list.add("-XX:ErrorFile=\"" + serverHome + "/serviceability/error.log\"");
 		list.add("-XX:HeapDumpPath=\"" + serverHome + "/serviceability/heap_dump.hprof\"");
		list.add("-XX:MaxPermSize=256m");
		list.add("-Xms256m");
		list.add("-Xmx768m");
 		list.add("-Djava.rmi.server.hostname=127.0.0.1");
 		list.add("-Dorg.eclipse.virgo.kernel.home=\"" + serverHome + "\"");
 		list.add("-Djava.io.tmpdir=\"" + serverHome + "/work/tmp/\"");
 		list.add("-Dcom.sun.management.jmxremote");
 		list.add("-Dssh.server.keystore=\"" + absConfigDir + "/hostkey.ser\"");
 		list.add("-Dcom.sun.management.jmxremote.port=" + ServerUtils.getServer(behaviour).getMBeanServerPort());
 		list.add("-Dcom.sun.management.jmxremote.authenticate=false");
 		list.add("-Dcom.sun.management.jmxremote.ssl=false");
 		list.add("-Dorg.eclipse.virgo.kernel.authentication.file=\"" + absConfigDir
 				+ "/org.eclipse.virgo.kernel.users.properties\"");
 		list.add("-Djava.security.auth.login.config=\"" + absConfigDir
 				+ "/org.eclipse.virgo.kernel.authentication.config\"");
 		if (getInstallationType(ServerUtils.getServer(behaviour).getRuntime().getRuntime()) == InstallationType.JETTY) {
 			list.add("-Djetty.home=\"" + serverHome + "/jetty\"");
 		}
 		return list.toArray(new String[list.size()]);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public IServerCommand<Void> getServerUndeployCommand(IServerBehaviour serverBehaviour, IModule module) {
 		return new JmxServerUndeployCommand(serverBehaviour, module, BUNDLE_OBJECT_NAME, PAR_OBJECT_NAME,
 				PLAN_OBJECT_NAME);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public IServerCommand<Void> getServerUpdateCommand(IServerBehaviour serverBehaviour, IModule module,
 			IModuleFile moduleFile, DeploymentIdentity identity, String bundleSymbolicName, String targetPath) {
 		return new JmxServerUpdateCommand(serverBehaviour, module, moduleFile, identity, bundleSymbolicName,
 				targetPath, BUNDLE_OBJECT_NAME, PAR_OBJECT_NAME, PLAN_OBJECT_NAME);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getShutdownMBeanName() {
 		return SHUTDOWN_MBEAN_NAME;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @throws IOException
 	 */
 	public Properties getProperties(IPath installPath, String type) throws IOException {
 		String version = installPath.append("lib").append("." + type).toOSString();
 		File versionFile = new File(version);
 		if (versionFile.exists()) {
 			InputStream is = null;
 			is = new FileInputStream(versionFile);
 			Properties versionProperties = new Properties();
 			versionProperties.load(is);
 			try {
 				is.close();
 			} catch (IOException e) {
 			}
 			return versionProperties;
 		} else {
 			throw new IOException("Installation does not contain a properties file. Path: " + installPath);
 		}
 	}
 
 	protected String getRepositoryConfigurationFileName() {
 		return "org.eclipse.virgo.repository.properties";
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public IStatus canAddModule(IModule module) {
 		return Status.OK_STATUS;
 	}
 
 	public IServerCommand<Boolean> getServerPingCommand(IServerBehaviour IServerBehaviour) {
 		return new JmxServerPingCommand(IServerBehaviour);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public IServerCommand<Void> getServerShutdownCommand(IServerBehaviour IServerBehaviour) {
 		return new JmxServerShutdownCommand(IServerBehaviour);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public IServerCommand<Void> getServerRefreshCommand(IServerBehaviour IServerBehaviour, IModule module,
 			String bundleSymbolicName) {
 		return new JmxServerRefreshCommand(IServerBehaviour, module, bundleSymbolicName);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String[] getExcludedRuntimeProgramArguments(boolean starting) {
 		List<String> list = new ArrayList<String>();
 		return list.toArray(new String[list.size()]);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getConfigPath(IRuntime runtime) {
 		return runtime.getLocation().append(getConfigDir()).append("server.config").toString();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getProfilePath(IRuntime runtime) {
 		return runtime.getLocation().append(getProfileDir()).append("java6-server.profile").toString();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public IPath getRuntimeBaseDirectory(IServer server) {
 		return server.getRuntime().getLocation();
 	}
 
 	/**
 	 * Provides runtime class path common to server versions.
 	 * 
 	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getRuntimeClasspath(org.eclipse.core.runtime.IPath)
 	 */
 	public List<IRuntimeClasspathEntry> getRuntimeClasspath(IPath installPath) {
 		List<IRuntimeClasspathEntry> cp = new ArrayList<IRuntimeClasspathEntry>();
 
 		IPath binPath = installPath.append("lib");
 		if (binPath.toFile().exists()) {
 			File libFolder = binPath.toFile();
 			for (File library : libFolder.listFiles(new FileFilter() {
 				public boolean accept(File pathname) {
 					return pathname.isFile() && pathname.toString().endsWith(".jar");
 				}
 			})) {
 				IPath path = binPath.append(library.getName());
 				cp.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
 			}
 		}
 
 		return cp;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getUserLevelBundleRepositoryPath(IRuntime runtime) {
 		return runtime.getLocation().append(REPOSITORY_DIR).append(USR_DIR).toString();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public String getUserLevelLibraryRepositoryPath(IRuntime runtime) {
 		return runtime.getLocation().append(REPOSITORY_DIR).append(USR_DIR).toString();
 	}
 
 	/**
 	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getExtLevelBundleRepositoryPath(org.eclipse.wst.server.core.IRuntime)
 	 */
 	public String getExtLevelBundleRepositoryPath(IRuntime runtime) {
 		return runtime.getLocation().append(REPOSITORY_DIR).append(EXT_DIR).toString();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public IServerCommand<DeploymentIdentity> getServerDeployCommand(IServerBehaviour IServerBehaviour, IModule module) {
 		return new JmxServerDeployCommand(IServerBehaviour, module);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public IServerCommand<Map<Long, IBundle>> getServerBundleAdminCommand(IServerBehaviour serverBehaviour) {
 		return new JmxBundleAdminServerCommand(serverBehaviour);
 	}
 
 	/**
 	 * @see org.eclipse.virgo.ide.runtime.core.IServerRuntimeProvider#getServerDeployCommand(org.eclipse.virgo.ide.runtime.core.IServerBehaviour)
 	 */
 	public IServerCommand<DeploymentIdentity> getServerDeployCommand(IServerBehaviour serverBehaviour) {
 		return new GenericJmxServerDeployCommand(serverBehaviour, getConnectorBundleUri());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public IServerCommand<String> getServerBundleAdminExecuteCommand(IServerBehaviour serverBehaviour, String command) {
 		return new JmxBundleAdminExecuteCommand(serverBehaviour, command);
 	}
 
 	private void createRepositoryConfiguration(IServerBehaviour serverBehaviour, String fileName) {
 		// copy repository.properties into the stage and add the stage
 		// repository
 		File serverHome = ServerUtils.getServer(serverBehaviour).getRuntimeBaseDirectory().toFile();
 		Properties properties = new Properties();
 		try {
 			properties.load(new FileInputStream(new File(serverHome, getConfigDir() + File.separatorChar + fileName)));
 		} catch (FileNotFoundException e) {
 			// TODO CD add logging
 		} catch (IOException e) {
 			// TODO CD add logging
 		}
 
 		properties.put("stage.type", "watched");
 		properties.put("stage.watchDirectory", "stage");
 
 		String chain = properties.getProperty("chain");
 		chain = "stage" + (StringUtils.hasLength(chain) ? "," + chain : "");
 		properties.put("chain", chain);
 
 		try {
 			File stageDirectory = new File(serverHome, "stage");
 			if (!stageDirectory.exists()) {
 				stageDirectory.mkdirs();
 			}
 			properties.store(new FileOutputStream(new File(serverHome, "stage" + File.separator + fileName)),
 					"Generated by Virgo IDE " + ServerCorePlugin.getDefault().getBundle().getVersion());
 		} catch (FileNotFoundException e) {
 			// TODO CD add logging
 		} catch (IOException e) {
 			// TODO CD add logging
 		}
 	}
 
 	public void preStartup(IServerBehaviour serverBehaviour) {
 		if (ServerUtils.getServer(serverBehaviour).shouldCleanStartup()) {
 			File serverHome = ServerUtils.getServer(serverBehaviour).getRuntimeBaseDirectory().toFile();
 			PublishHelper.deleteDirectory(new File(serverHome, "work"), new NullProgressMonitor());
 			PublishHelper.deleteDirectory(new File(serverHome, "serviceability"), new NullProgressMonitor());
 		}
 		createRepositoryConfiguration(serverBehaviour, getRepositoryConfigurationFileName());
 	}
 
 	public boolean isHandlerFor(IRuntime runtime) {
 		IPath configPath = runtime.getLocation().append(getConfigDir());
 		File configDir = configPath.toFile();
 		return configDir.exists();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public IStatus verifyInstallation(IRuntime runtime) {
 		InstallationType installation = getInstallationType(runtime);
 		if (installation == null) {
 			return new Status(IStatus.ERROR, ServerCorePlugin.PLUGIN_ID,
 					"Invalid Virgo Server: Could not determine version.");
 		}
 		return new Status(IStatus.OK, ServerCorePlugin.PLUGIN_ID, "Valid installation: " + getName(runtime) + ".");
 	}
 
 	public InstallationType getInstallationType(IRuntime runtime) {
 		Properties versionProperties;
 		IPath location = runtime.getLocation();
 		try {
 			versionProperties = getProperties(location, "version");
 		} catch (IOException e) {
 			return null;
 		}
 		String versionString = versionProperties.getProperty("virgo.server.version");
 		if (versionString != null) {
 			if (location.append("jetty").toFile().exists()) {
 				return InstallationType.JETTY;
 			} else {
 				return InstallationType.TOMCAT;
 			}
 		}
 		versionString = versionProperties.getProperty("virgo.kernel.version");
 		if (versionString != null) {
 			return InstallationType.KERNEL;
 		}
 		return null;
 	}
 
 	public String getVersionName(IRuntime runtime) {
 		Properties versionProperties;
 		try {
 			versionProperties = getProperties(runtime.getLocation(), "version");
 		} catch (IOException e) {
 			return null;
 		}
 		String versionString = versionProperties.getProperty("virgo.server.version");
 		if (versionString == null) {
 			versionString = versionProperties.getProperty("virgo.kernel.version");
 		}
 		if (versionString == null) {
 			versionString = versionProperties.getProperty("virgo.nano.version");
 		}
 		if (versionString == null) {
 			versionString = "Unknown";
 		}
 		return versionString;
 	}
 
 	public String getName(IRuntime runtime) {
 		return getInstallationType(runtime).getName() + " v" + getVersionName(runtime);
 	}
 
 	public abstract String getSupportedVersions();
 
 	/**
 	 * Non-API
 	 */
 	abstract String getConfigDir();
 
 	/**
 	 * Non-API
 	 */
 	abstract String getProfileDir();
 }
