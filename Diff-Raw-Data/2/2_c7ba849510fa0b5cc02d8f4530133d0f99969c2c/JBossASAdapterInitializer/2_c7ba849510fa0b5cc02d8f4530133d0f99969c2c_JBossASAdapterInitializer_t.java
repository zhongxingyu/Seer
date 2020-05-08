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
 
 /**
  * @author eskimo
  *
  */
 public class JBossASAdapterInitializer implements IStartup {
 
 	public static final String JBOSS_AS_HOME = "../../../../jboss-eap/jboss-as"; 	// JBoss AS home directory (relative to plugin)- <RHDS_HOME>/jbossas.
 	
 	public static final String JBOSS_AS_RUNTIME_TYPE_ID 
 										= "org.jboss.ide.eclipse.as.runtime.42";
 	
 	public static final String JBOSS_AS_TYPE_ID = "org.jboss.ide.eclipse.as.42";
 	
 	public static final String JBOSS_AS_NAME = "JBoss Application Server 4.2";
 	
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
 
 		try {
 			
 			JstFirstRunPlugin.getDefault().getPreferenceStore().setDefault(FIRST_START_PREFERENCE_NAME, true);
 			boolean firstStart = JstFirstRunPlugin.getDefault().getPreferenceStore().getBoolean(FIRST_START_PREFERENCE_NAME);
 			if(!firstStart) {
 				return;
 			}
 			JstFirstRunPlugin.getDefault().getPreferenceStore().setValue(FIRST_START_PREFERENCE_NAME, false);
 
 			String jbossASLocation = null;
 			String pluginLocation = FileLocator.resolve(JstFirstRunPlugin.getDefault().getBundle().getEntry("/")).getPath();
 			File jbossASDir = new File(pluginLocation, JBOSS_AS_HOME);
 			if(jbossASDir.isDirectory()) {
 				jbossASLocation = jbossASDir.getAbsolutePath();
 			} else {
 				return;
 			}
 
 			IPath jbossAsLocationPath = new Path(jbossASLocation);
 			String type = null;
 			String version = null;
 
 			IServer[] servers = ServerCore.getServers();
 			for(int i=0; i<servers.length; i++) {
 				IRuntime runtime = servers[i].getRuntime();
 				if(runtime!=null && runtime.getLocation().equals(jbossAsLocationPath)) {
 					return;
 				}
 			}
 
 			IRuntimeWorkingCopy runtime = null;
 			IRuntime[] runtimes = ServerCore.getRuntimes();
 			String runtimeId = null;
 			for(int i=0; i<runtimes.length; i++) {
 				if(runtimes[0].getLocation().equals(jbossASLocation)) {
 					runtime = runtimes[0].createWorkingCopy();
 					runtimeId = null;
 					break;
 				}
 			}
 
 			IProgressMonitor progressMonitor = new NullProgressMonitor();
 			if(runtime==null) {
 				IRuntimeType[] runtimeTypes = ServerUtil.getRuntimeTypes(type, version, JBOSS_AS_RUNTIME_TYPE_ID);
 				if(runtimeTypes.length>0) {
 					runtime = runtimeTypes[0].createRuntime(runtimeId, progressMonitor);
 					runtime.setLocation(jbossAsLocationPath);
 					IVMInstall defaultVM = JavaRuntime.getDefaultVMInstall();
 					// IJBossServerRuntime.PROPERTY_VM_ID
 					((RuntimeWorkingCopy)runtime).setAttribute("PROPERTY_VM_ID", defaultVM.getId());
 					// IJBossServerRuntime.PROPERTY_VM_TYPE_ID
 					((RuntimeWorkingCopy)runtime).setAttribute("PROPERTY_VM_TYPE_ID", defaultVM.getVMInstallType().getId());
 					// IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME
 					((RuntimeWorkingCopy)runtime).setAttribute("org.jboss.ide.eclipse.as.core.runtime.configurationName", JBOSS_AS_DEFAULT_CONFIGURATION_NAME);
 
 					runtime.save(false, progressMonitor);
 				}
 			}
 
 			if(runtime!=null) {
 				IServerType serverType = ServerCore.findServerType(JBOSS_AS_TYPE_ID);
 				IServerWorkingCopy server = serverType.createServer(null, null, runtime, progressMonitor);
 
 				server.setHost(JBOSS_AS_HOST);
 				server.setName(JBOSS_AS_NAME);
 				server.save(false, progressMonitor);
 			}
 			
 			DriverInstance driver = DriverManager.getInstance()
 									.getDriverInstanceByName(HSQL_DRIVER_NAME);
 			if(driver==null) { 
 				TemplateDescriptor descr = TemplateDescriptor
 				.getDriverTemplateDescriptor(HSQL_DRIVER_TEMPLATE_ID);
 				IPropertySet instance = new PropertySetImpl(
 									HSQL_DRIVER_NAME,HSQL_DRIVER_DEFINITION_ID);
 				instance.setName(HSQL_DRIVER_NAME);
 				instance.setID(HSQL_DRIVER_DEFINITION_ID);
 				Properties props = new Properties();
 
 				IConfigurationElement[] template = descr.getProperties();
 				for (int i = 0; i < template.length; i++) {
 					IConfigurationElement prop = template[i];
 					String id = prop.getAttribute("id"); //$NON-NLS-1$
 					
 					String value = prop.getAttribute("value"); //$NON-NLS-1$
					props.setProperty(id, value == null ? ""
 							: value);
 				}
 				props.setProperty(DTP_DB_URL_PROPERTY_ID, "jdbc:hsqldb:.");
 				props.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE,
 						descr.getId());
 				props.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST,
 						jbossASLocation+"/server/default/lib/hsqldb.jar");
 
 				instance.setBaseProperties(props);
 				DriverManager.getInstance().removeDriverInstance(instance.getID());
 				System.gc();
 				DriverManager.getInstance().addDriverInstance(instance);
 				
 			}		
 			
 			driver = DriverManager.getInstance().getDriverInstanceByName(HSQL_DRIVER_NAME);
 			if(driver!=null) {
 				// create profile
 				Properties props = new Properties();
 				props.setProperty(ConnectionProfileConstants.PROP_DRIVER_DEFINITION_ID,
 						HSQL_DRIVER_DEFINITION_ID);
 				props.setProperty(
 						IDBConnectionProfileConstants.CONNECTION_PROPERTIES_PROP_ID,
 						"");
 				props.setProperty(IDBDriverDefinitionConstants.DRIVER_CLASS_PROP_ID, 
 						driver.getProperty(IDBDriverDefinitionConstants.DRIVER_CLASS_PROP_ID));
 				props.setProperty(IDBDriverDefinitionConstants.DATABASE_VENDOR_PROP_ID, 
 						driver.getProperty(IDBDriverDefinitionConstants.DATABASE_VENDOR_PROP_ID));		
 				props.setProperty(IDBDriverDefinitionConstants.DATABASE_VERSION_PROP_ID,
 						driver.getProperty(IDBDriverDefinitionConstants.DATABASE_VERSION_PROP_ID));			
 				props.setProperty(IDBDriverDefinitionConstants.DATABASE_NAME_PROP_ID, 
 						"Default");
 				props.setProperty(IDBDriverDefinitionConstants.PASSWORD_PROP_ID, "");
 				props.setProperty(
 						IDBConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, "false");
 				props.setProperty(IDBDriverDefinitionConstants.USERNAME_PROP_ID, 
 						driver.getProperty(IDBDriverDefinitionConstants.USERNAME_PROP_ID));
 				props.setProperty(IDBDriverDefinitionConstants.URL_PROP_ID,
 						driver.getProperty(IDBDriverDefinitionConstants.URL_PROP_ID));
 
 				ProfileManager.getInstance().createProfile(
 						"DefaultDS",
 						"The JBoss AS Hypersonic embedded database",
 						IDBConnectionProfileConstants.CONNECTION_PROFILE_ID,
 						props,
 						"", false);
 			}
 			
 		} catch (CoreException e) {
 			JstFirstRunPlugin.getPluginLog().log(new Status(IStatus.ERROR,
 					JstFirstRunPlugin.PLUGIN_ID,"Can't create new JBoss Server", e));
 		} catch (IOException e) {
 			JstFirstRunPlugin.getPluginLog().log(new Status(IStatus.ERROR,
 					JstFirstRunPlugin.PLUGIN_ID,"Can't create new JBoss Server", e));
 		} catch (ConnectionProfileException e) {
 			JstFirstRunPlugin.getPluginLog().log(new Status(IStatus.ERROR,
 					JstFirstRunPlugin.PLUGIN_ID,"Can't create new DTP " +
 					"Connection Profile for JBoss AS Hypersonic embedded database", e));
 		}
 	}
 }
