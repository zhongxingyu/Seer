 /*******************************************************************************
  * Copyright (c) 2007 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 
 package org.jboss.tools.jst.firstrun;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.datatools.connectivity.ConnectionProfileConstants;
 import org.eclipse.datatools.connectivity.ConnectionProfileException;
 import org.eclipse.datatools.connectivity.ProfileManager;
 import org.eclipse.datatools.connectivity.db.generic.IDBConnectionProfileConstants;
 import org.eclipse.datatools.connectivity.db.generic.IDBDriverDefinitionConstants;
 import org.eclipse.datatools.connectivity.drivers.DriverInstance;
 import org.eclipse.datatools.connectivity.drivers.DriverManager;
 import org.eclipse.datatools.connectivity.drivers.IDriverMgmtConstants;
 import org.eclipse.datatools.connectivity.drivers.IPropertySet;
 import org.eclipse.datatools.connectivity.drivers.PropertySetImpl;
 import org.eclipse.datatools.connectivity.drivers.models.TemplateDescriptor;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.ui.IStartup;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.IRuntimeType;
 import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.IServerType;
 import org.eclipse.wst.server.core.IServerWorkingCopy;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.internal.RuntimeWorkingCopy;
 import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
 import org.jboss.tools.common.util.FileUtil;
 
 /**
  * @author eskimo
  *
  */
 public class JBossASAdapterInitializer implements IStartup {
 
 	public static final String JBOSS_AS_HOME = "../../../../jboss-eap/jboss-as"; 	// JBoss AS home directory (relative to plugin)- <RHDS_HOME>/jbossas.
 	
 	public static final String SERVERS_FILE = "../../../../studio/application_platforms.properties";
 	
 	
 	// This constants are made to avoid dependency with org.jboss.ide.eclipse.as.core plugin
 	public static final String JBOSS_AS_RUNTIME_TYPE_ID[] = {
 		"org.jboss.ide.eclipse.as.runtime.32",
 		"org.jboss.ide.eclipse.as.runtime.40",
 		"org.jboss.ide.eclipse.as.runtime.42",
 		"org.jboss.ide.eclipse.as.runtime.50"
 		};
 
 	public static final String JBOSS_AS_TYPE_ID[] = {
 		"org.jboss.ide.eclipse.as.32",
 		"org.jboss.ide.eclipse.as.40",
 		"org.jboss.ide.eclipse.as.42",
 		"org.jboss.ide.eclipse.as.50"
 		};
 	
 
 	public static final String JBOSS_AS_NAME[] = {
 		"JBoss Application Server 3.2",
 		"JBoss Application Server 4.0",
 		"JBoss Application Server 4.2",
 		"JBoss Application Server 5.0"
 		};
 	
 	private static final int installedASIndex = 2;
 
 	public static final String JBOSS_AS_HOST = "localhost";
 
 	public static final String JBOSS_AS_DEFAULT_CONFIGURATION_NAME = "default";
 
 	public static final String FIRST_START_PREFERENCE_NAME = "FIRST_START";
 
 	public static final String HSQL_DRIVER_DEFINITION_ID 
 												= "DriverDefn.Hypersonic DB";
 
 	public static final String HSQL_DRIVER_NAME = "Hypersonic DB";
 
 	public static final String HSQL_DRIVER_TEMPLATE_ID 
 						= "org.eclipse.datatools.enablement.hsqldb.1_8.driver";
 
 	public static final String DTP_DB_URL_PROPERTY_ID 
 								= "org.eclipse.datatools.connectivity.db.URL";
 	/**
 	 * @see org.eclipse.ui.IStartup#earlyStartup()
 	 */
 	public void earlyStartup() {
 
 		/*
 		 * If there are any problems with EAP not functioning the same as 
 		 * servers created from scratch, THIS is the method to go to.
 		 * 
 		 * Compare this method with JBossServerWizardFragment#performFinish()
 		 */
 		try {
 			JstFirstRunPlugin.getDefault().getPreferenceStore().setDefault(FIRST_START_PREFERENCE_NAME, true);
 			boolean firstStart = JstFirstRunPlugin.getDefault().getPreferenceStore().getBoolean(FIRST_START_PREFERENCE_NAME);
 			if (!firstStart) {
 				return;
 			}
 			JstFirstRunPlugin.getDefault().getPreferenceStore().setValue(FIRST_START_PREFERENCE_NAME, false);
 			String pluginLocation = FileLocator.resolve(JstFirstRunPlugin.getDefault().getBundle().getEntry("/")).getPath();
 			File serversFile = new File(pluginLocation, SERVERS_FILE).getCanonicalFile();
 			if(serversFile.exists()){
 				String str = FileUtil.readFile(serversFile);
 				int position = 0;
 				while(true){
 					String jbossASLocation = null;
 					
 					// server name
 					int namePosition = str.indexOf("=",position+1);
 					if(namePosition < 0) break;
 					
 					// server type
 					int typePosition = str.indexOf(",",namePosition+1);
 					if(position < 0) break;
 					
 					String name = str.substring(namePosition+1,typePosition).trim();
 
 					// server version
 					int versionPosition = str.indexOf(",",typePosition+1);
 					if(versionPosition < 0) break;
 					
 					String type = str.substring(typePosition+1,versionPosition).trim();
 					
 					// server location
 					position = str.indexOf(",",versionPosition+1);
 					if(position < 0) break;
 					
 					String version = str.substring(versionPosition+1,position);
 					
 					int index = 0;
 					if(type.startsWith("AS")){
 						if(version.startsWith("3.2"))
 							index = 0;
 						else if(version.startsWith("4.0"))
 							index = 1;
						else if(version.startsWith("4.2") || version.startsWith("4.3"))
 							index = 2;
 						else if(version.startsWith("5.0"))
 							index = 3;
 					}else
 						index = 2;
 					
 					int next = str.indexOf("server",position+1);
 					
 					if(next < 0)
 						jbossASLocation = str.substring(position+1,str.length()-1);
 					else
 						jbossASLocation = str.substring(position+1,next);
 					
 					jbossASLocation = jbossASLocation.trim();
 					
 					IRuntime runtime = null;
 					IProgressMonitor progressMonitor = new NullProgressMonitor();
 					if (runtime == null) {
 						runtime = createRuntime(name + " Runtime", jbossASLocation, progressMonitor, index);
 					}
 					if (runtime != null) {
 						createServer(progressMonitor, runtime, index, name);
 					}
 
 					createDriver(jbossASLocation);
 				}
 			}
 
 			String jbossASLocation = null;
 			
 			File jbossASDir = new File(pluginLocation, JBOSS_AS_HOME).getCanonicalFile();
 			if (jbossASDir.isDirectory()) {
 				jbossASLocation = jbossASDir.getAbsolutePath();
 			} else {
 				return;
 			}
 
 			IPath jbossAsLocationPath = new Path(jbossASLocation);
 
 			IServer[] servers = ServerCore.getServers();
 			for (int i = 0; i < servers.length; i++) {
 				IRuntime runtime = servers[i].getRuntime();
 				if(runtime != null && runtime.getLocation().equals(jbossAsLocationPath)) {
 					return;
 				}
 			}
 
 			IRuntime runtime = null;
 			IRuntime[] runtimes = ServerCore.getRuntimes();
 			for (int i = 0; i < runtimes.length; i++) {
 				if (runtimes[0].getLocation().equals(jbossASLocation)) {
 					runtime = runtimes[0].createWorkingCopy();
 					break;
 				}
 			}
 
 			IProgressMonitor progressMonitor = new NullProgressMonitor();
 			if (runtime == null) {
 				runtime = createRuntime(null, jbossASLocation, progressMonitor, 2);
 			}
 			if (runtime != null) {
 				createServer(progressMonitor, runtime, 2, null);
 			}
 
 			createDriver(jbossASLocation);
 		} catch (CoreException e) {
 			JstFirstRunPlugin.getPluginLog().log(new Status(IStatus.ERROR,
 					JstFirstRunPlugin.PLUGIN_ID, "Can't create new JBoss Server", e));
 		} catch (IOException e) {
 			JstFirstRunPlugin.getPluginLog().log(new Status(IStatus.ERROR,
 					JstFirstRunPlugin.PLUGIN_ID, "Can't create new JBoss Server", e));
 		} catch (ConnectionProfileException e) {
 			JstFirstRunPlugin.getPluginLog().log(new Status(IStatus.ERROR,
 					JstFirstRunPlugin.PLUGIN_ID, "Can't create new DTP "
 					+ "Connection Profile for JBoss AS Hypersonic embedded database", e));
 		}
 	}
 
 	/**
 	 * Creates new JBoss AS Runtime, Server and hsqldb driver
 	 * @param jbossASLocation location of JBoss Server
 	 * @param progressMonitor to report progress
 	 * @return server working copy
 	 * @throws CoreException
 	 * @throws ConnectionProfileException
 	 */
 	public static IServerWorkingCopy initJBossAS(String jbossASLocation, IProgressMonitor progressMonitor) throws CoreException, ConnectionProfileException {
 		IRuntime runtime = createRuntime(null, jbossASLocation, progressMonitor, 2);
 		IServerWorkingCopy server = null;
 		if (runtime != null) {
 			server = createServer(progressMonitor, runtime, 2, null);
 		}
 		createDriver(jbossASLocation);
 		return server;
 	}
 
 	/**
 	 * Creates new JBoss AS Runtime
 	 * @param jbossASLocation location of JBoss AS
 	 * @param progressMonitor
 	 * @return runtime working copy
 	 * @throws CoreException
 	 */
 	private static IRuntime createRuntime(String runtimeName, String jbossASLocation, IProgressMonitor progressMonitor, int index) throws CoreException {
 		IRuntimeWorkingCopy runtime = null;
 		String type = null;
 		String version = null;
 		String runtimeId = null;
 		IPath jbossAsLocationPath = new Path(jbossASLocation);
 		IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(type, version, JBOSS_AS_RUNTIME_TYPE_ID[index]);
 		if (runtimeTypes.length > 0) {
 			runtime = runtimeTypes[0].createRuntime(runtimeId, progressMonitor);
 			runtime.setLocation(jbossAsLocationPath);
 			if(runtimeName!=null) {
 				runtime.setName(runtimeName);				
 			}
 			IVMInstall defaultVM = JavaRuntime.getDefaultVMInstall();
 			// IJBossServerRuntime.PROPERTY_VM_ID
 			((RuntimeWorkingCopy) runtime).setAttribute("PROPERTY_VM_ID", defaultVM.getId());
 			// IJBossServerRuntime.PROPERTY_VM_TYPE_ID
 			((RuntimeWorkingCopy) runtime).setAttribute("PROPERTY_VM_TYPE_ID", defaultVM.getVMInstallType().getId());
 			// IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME
 			((RuntimeWorkingCopy) runtime).setAttribute("org.jboss.ide.eclipse.as.core.runtime.configurationName", JBOSS_AS_DEFAULT_CONFIGURATION_NAME);
 
 			return runtime.save(false, progressMonitor);
 		}
 		return runtime;
 	}
 
 	/**
 	 * Creates new JBoss Server
 	 * @param progressMonitor
 	 * @param runtime parent JBoss AS Runtime
 	 * @return server working copy
 	 * @throws CoreException
 	 */
 	private static IServerWorkingCopy createServer(IProgressMonitor progressMonitor, IRuntime runtime, int index, String name) throws CoreException {
 		IServerType serverType = ServerCore.findServerType(JBOSS_AS_TYPE_ID[index]);
 		IServerWorkingCopy server = serverType.createServer(null, null, runtime, progressMonitor);
 
 		server.setHost(JBOSS_AS_HOST);
 		if(name != null)
 			server.setName(name);
 		else
 			server.setName(JBOSS_AS_NAME[installedASIndex]);
 		
 		// JBossServer.DEPLOY_DIRECTORY
 		String deployVal = runtime.getLocation().append("server").append(JBOSS_AS_DEFAULT_CONFIGURATION_NAME).append("deploy").toOSString();
 		((ServerWorkingCopy) server).setAttribute("org.jboss.ide.eclipse.as.core.server.deployDirectory", deployVal);
 
 		// IDeployableServer.TEMP_DEPLOY_DIRECTORY
 		String deployTmpFolderVal = runtime.getLocation().append("server").append(JBOSS_AS_DEFAULT_CONFIGURATION_NAME).append("tmp").append("jbosstoolsTemp").toOSString();
 		((ServerWorkingCopy) server).setAttribute("org.jboss.ide.eclipse.as.core.server.tempDeployDirectory", deployTmpFolderVal);
 
 		// If we'd need to set up a username / pw for JMX, do it here.
 //		((ServerWorkingCopy)serverWC).setAttribute(JBossServer.SERVER_USERNAME, authUser);
 //		((ServerWorkingCopy)serverWC).setAttribute(JBossServer.SERVER_PASSWORD, authPass);
 
 		server.save(false, progressMonitor);
 		return server;
 	}
 
 	private static boolean driverIsCreated = false;
 
 	/**
 	 * Creates HSQL DB Driver
 	 * @param jbossASLocation location of JBoss AS
 	 * @throws ConnectionProfileException
 	 * @return driver instance
 	 */
 	private static void createDriver(String jbossASLocation) throws ConnectionProfileException {
 		if(driverIsCreated) {
 			// Don't create the driver a few times
 			return;
 		}
 		String driverPath;
 		try {
 			driverPath = new File(jbossASLocation + "/server/default/lib/hsqldb.jar").getCanonicalPath();
 		} catch (IOException e) {
 			JstFirstRunPlugin.getPluginLog().log(new Status(IStatus.ERROR,
 					JstFirstRunPlugin.PLUGIN_ID, "Can't create new HSQL DB Driver.", e));
 			return;
 		}
 
 		DriverInstance driver = DriverManager.getInstance().getDriverInstanceByName(HSQL_DRIVER_NAME);
 		if (driver == null) {
 			TemplateDescriptor descr = TemplateDescriptor.getDriverTemplateDescriptor(HSQL_DRIVER_TEMPLATE_ID);
 			IPropertySet instance = new PropertySetImpl(HSQL_DRIVER_NAME, HSQL_DRIVER_DEFINITION_ID);
 			instance.setName(HSQL_DRIVER_NAME);
 			instance.setID(HSQL_DRIVER_DEFINITION_ID);
 			Properties props = new Properties();
 
 			IConfigurationElement[] template = descr.getProperties();
 			for (int i = 0; i < template.length; i++) {
 				IConfigurationElement prop = template[i];
 				String id = prop.getAttribute("id"); //$NON-NLS-1$
 
 				String value = prop.getAttribute("value"); //$NON-NLS-1$
 				props.setProperty(id, value == null ? "" : value);
 			}
 			props.setProperty(DTP_DB_URL_PROPERTY_ID, "jdbc:hsqldb:.");
 			props.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE, descr.getId());
 			props.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST, driverPath);
 
 			instance.setBaseProperties(props);
 			DriverManager.getInstance().removeDriverInstance(instance.getID());
 			System.gc();
 			DriverManager.getInstance().addDriverInstance(instance);
 		}
 
 		driver = DriverManager.getInstance().getDriverInstanceByName(HSQL_DRIVER_NAME);
 		if (driver != null && ProfileManager.getInstance().getProfileByName("DefaultDS") == null) {
 			// create profile
 			Properties props = new Properties();
 			props.setProperty(ConnectionProfileConstants.PROP_DRIVER_DEFINITION_ID, HSQL_DRIVER_DEFINITION_ID);
 			props.setProperty(IDBConnectionProfileConstants.CONNECTION_PROPERTIES_PROP_ID, "");
 			props.setProperty(IDBDriverDefinitionConstants.DRIVER_CLASS_PROP_ID, driver.getProperty(IDBDriverDefinitionConstants.DRIVER_CLASS_PROP_ID));
 			props.setProperty(IDBDriverDefinitionConstants.DATABASE_VENDOR_PROP_ID,	driver.getProperty(IDBDriverDefinitionConstants.DATABASE_VENDOR_PROP_ID));
 			props.setProperty(IDBDriverDefinitionConstants.DATABASE_VERSION_PROP_ID, driver.getProperty(IDBDriverDefinitionConstants.DATABASE_VERSION_PROP_ID));
 			props.setProperty(IDBDriverDefinitionConstants.DATABASE_NAME_PROP_ID, "Default");
 			props.setProperty(IDBDriverDefinitionConstants.PASSWORD_PROP_ID, "");
 			props.setProperty(IDBConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, "false");
 			props.setProperty(IDBDriverDefinitionConstants.USERNAME_PROP_ID, driver.getProperty(IDBDriverDefinitionConstants.USERNAME_PROP_ID));
 			props.setProperty(IDBDriverDefinitionConstants.URL_PROP_ID, driver.getProperty(IDBDriverDefinitionConstants.URL_PROP_ID));
 
 			ProfileManager.getInstance().createProfile("DefaultDS",	"The JBoss AS Hypersonic embedded database", IDBConnectionProfileConstants.CONNECTION_PROFILE_ID, props, "", false);
 		}
 		if(driver!=null) {
 			driverIsCreated = true;
 		}
 	}
 }
