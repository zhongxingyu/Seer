 /**********************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 *
  * Contributors:
  *    IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.jst.server.tomcat.core.internal;
 
 import java.net.URL;
 import java.util.List;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jst.server.core.IWebModule;
 
 import org.eclipse.wst.server.core.*;
 import org.eclipse.wst.server.core.internal.ServerMonitorManager;
 import org.eclipse.wst.server.core.model.*;
 /**
  * Generic Tomcat server.
  */
 public class TomcatServer extends ServerDelegate implements ITomcatServer, ITomcatServerWorkingCopy {
 	public static final String PROPERTY_SECURE = "secure";
 	public static final String PROPERTY_DEBUG = "debug";
 
 	protected transient TomcatConfiguration configuration;
 
 	/**
 	 * TomcatServer.
 	 */
 	public TomcatServer() {
 		super();
 	}
 
 	public TomcatRuntime getTomcatRuntime() {
 		if (getServer().getRuntime() == null)
 			return null;
 		
 		return (TomcatRuntime) getServer().getRuntime().getAdapter(TomcatRuntime.class);
 	}
 
 	public ITomcatVersionHandler getTomcatVersionHandler() {
 		if (getServer().getRuntime() == null)
 			return null;
 
 		return getTomcatRuntime().getVersionHandler();
 	}
 
 	public ITomcatConfiguration getServerConfiguration() throws CoreException {
 		return getTomcatConfiguration();
 	}
 
 	public TomcatConfiguration getTomcatConfiguration() throws CoreException {
 		if (configuration == null) {
 			IFolder folder = getServer().getServerConfiguration();
			if (!folder.exists())
 				throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, TomcatPlugin.getResource("%errorNoConfiguration", folder.getFullPath().toOSString()), null));
 			
 			String id = getServer().getServerType().getId();
 			if (id.indexOf("32") > 0)
 				configuration = new Tomcat32Configuration(folder);
 			else if (id.indexOf("40") > 0)
 				configuration = new Tomcat40Configuration(folder);
 			else if (id.indexOf("41") > 0)
 				configuration = new Tomcat41Configuration(folder);
 			else if (id.indexOf("50") > 0)
 				configuration = new Tomcat50Configuration(folder);
 			else if (id.indexOf("55") > 0)
 				configuration = new Tomcat55Configuration(folder);
 			try {
 				configuration.load(folder, null);
 			} catch (CoreException ce) {
 				// ignore
 				configuration = null;
 				throw ce;
 			}
 		}
 		return configuration;
 	}
 
 	public void importConfiguration(IRuntime runtime, IProgressMonitor monitor) {
 		if (runtime == null) {
 			configuration = null;
 			return;
 		}
 		IPath path = runtime.getLocation().append("conf");
 		
 		String id = getServer().getServerType().getId();
 		IFolder folder = getServer().getServerConfiguration();
 		if (id.indexOf("32") > 0)
 			configuration = new Tomcat32Configuration(folder);
 		else if (id.indexOf("40") > 0)
 			configuration = new Tomcat40Configuration(folder);
 		else if (id.indexOf("41") > 0)
 			configuration = new Tomcat41Configuration(folder);
 		else if (id.indexOf("50") > 0)
 			configuration = new Tomcat50Configuration(folder);
 		else if (id.indexOf("55") > 0)
 			configuration = new Tomcat55Configuration(folder);
 		try {
 			configuration.importFromPath(path, isTestEnvironment(), monitor);
 		} catch (CoreException ce) {
 			// ignore
 			configuration = null;
 		}
 	}
 
 	public void saveConfiguration(IProgressMonitor monitor) throws CoreException {
 		TomcatConfiguration config = getTomcatConfiguration();
 		if (config == null)
 			throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, TomcatPlugin.getResource("%errorCouldNotSaveConfiguration", "null"), null));
 		config.save(getServer().getServerConfiguration(), monitor);
 	}
 
 	public void configurationChanged() {
 		configuration = null;
 	}
 
 	/**
 	 * Return the root URL of this module.
 	 * @param module org.eclipse.wst.server.core.model.IModule
 	 * @return java.net.URL
 	 */
 	public URL getModuleRootURL(IModule module) {
 		try {
 			if (module == null)
 				return null;
 	
 			TomcatConfiguration config = getTomcatConfiguration();
 			if (config == null)
 				return null;
 	
 			String url = "http://localhost";
 			int port = config.getMainPort().getPort();
 			port = ServerMonitorManager.getInstance().getMonitoredPort(getServer(), port, "web");
 			if (port != 80)
 				url += ":" + port;
 
 			url += config.getWebModuleURL(module);
 			
 			if (!url.endsWith("/"))
 				url += "/";
 
 			return new URL(url);
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Could not get root URL", e);
 			return null;
 		}
 	}
 
 	/**
 	 * Returns true if the process is set to run in debug mode.
 	 * This feature only works with Tomcat v4.0.
 	 *
 	 * @return boolean
 	 */
 	public boolean isDebug() {
 		return getAttribute(PROPERTY_DEBUG, false);
 	}
 
 	/**
 	 * Returns true if this is a test (run code out of the workbench) server.
 	 *
 	 * @return boolean
 	 */
 	public boolean isTestEnvironment() {
 		return getAttribute(PROPERTY_TEST_ENVIRONMENT, false);
 	}
 
 	/**
 	 * Returns true if the process is set to run in secure mode.
 	 *
 	 * @return boolean
 	 */
 	public boolean isSecure() {
 		return getAttribute(PROPERTY_SECURE, false);
 	}
 	
 	protected static String renderCommandLine(String[] commandLine, String separator) {
 		if (commandLine == null || commandLine.length < 1)
 			return "";
 		StringBuffer buf= new StringBuffer(commandLine[0]);
 		for (int i = 1; i < commandLine.length; i++) {
 			buf.append(separator);
 			buf.append(commandLine[i]);
 		}	
 		return buf.toString();
 	}
 
 	/**
 	 * Return a string representation of this object.
 	 * @return java.lang.String
 	 */
 	public String toString() {
 		return "TomcatServer";
 	}
 
 	/*
 	 * Returns the child module(s) of this module.
 	 */
 	public IModule[] getChildModules(IModule[] module) {
 		return new IModule[0];
 	}
 
 	/*
 	 * Returns the root module(s) of this module.
 	 */
 	public IModule[] getRootModules(IModule module) throws CoreException {
 		if (module.getAdapter(IWebModule.class) != null) {
 			IStatus status = canModifyModules(new IModule[] { module }, null);
 			if (status == null || !status.isOK())
 				throw new CoreException(status);
 			return new IModule[] { module };
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the project references for projects that are in
 	 * this configuration.
 	 *
 	 * @return java.lang.String[]
 	 */
 	/*public IModule[] getModules() {
 		List list = new ArrayList();
 		
 		ITomcatConfiguration config = getTomcatConfiguration();
 		if (config != null) {
 			List modules = config.getWebModules();
 			int size = modules.size();
 			for (int i = 0; i < size; i++) {
 				WebModule module = (WebModule) modules.get(i);
 				
 				String memento = module.getMemento();
 				if (memento != null) {
 					IModule module2 = ServerUtil.getModule(memento);
 					if (module2 != null)
 						list.add(module2);
 				}
 			}
 		}
 		
 		IModule[] s = new IModule[list.size()];
 		list.toArray(s);
 		
 		return s;
 	}*/
 
 	/**
 	 * Returns true if the given project is supported by this
 	 * server, and false otherwise.
 	 *
 	 * @param add modules
 	 * @param remove modules
 	 * @return the status
 	 */
 	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
 		if (add != null) {
 			int size = add.length;
 			for (int i = 0; i < size; i++) {
 				IModule module = add[i];
 				IWebModule webModule = (IWebModule) module.getAdapter(IWebModule.class);
 				if (webModule == null)
 					return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, TomcatPlugin.getResource("%errorWebModulesOnly"), null);
 				
 				IStatus status = getTomcatVersionHandler().canAddModule(webModule);
 				if (status != null && !status.isOK())
 					return status;
 			}
 		}
 		
 		return new Status(IStatus.OK, TomcatPlugin.PLUGIN_ID, 0, "%canModifyModules", null);
 	}
 
 	public ServerPort[] getServerPorts() {
 		if (getServer().getServerConfiguration() == null)
 			return new ServerPort[0];
 		
 		try {
 			List list = getTomcatConfiguration().getServerPorts();
 			ServerPort[] sp = new ServerPort[list.size()];
 			list.toArray(sp);
 			return sp;
 		} catch (Exception e) {
 			return new ServerPort[0]; 
 		}
 	}
 	
 	public void setDefaults() {
 		setTestEnvironment(true);
 	}
 	
 	/**
 	 * Sets this process to debug mode. This feature only works
 	 * with Tomcat v4.0.
 	 *
 	 * @param b boolean
 	 */
 	public void setDebug(boolean b) {
 		setAttribute(PROPERTY_DEBUG, b);
 	}
 
 	/**
 	 * Sets this process to secure mode.
 	 * @param b boolean
 	 */
 	public void setSecure(boolean b) {
 		setAttribute(PROPERTY_SECURE, b);
 	}
 
 	/**
 	 * Sets this server to test environment mode.
 	 * 
 	 * @param b boolean
 	 */
 	public void setTestEnvironment(boolean b) {
 		setAttribute(PROPERTY_TEST_ENVIRONMENT, b);
 	}
 	
 	/**
 	 * @see ServerDelegate#modifyModules(IModule[], IModule[], IProgressMonitor)
 	 */
 	public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
 		IStatus status = canModifyModules(add, remove);
 		if (status == null || !status.isOK())
 			throw new CoreException(status);
 		
 		TomcatConfiguration config = getTomcatConfiguration();
 
 		if (add != null) {
 			int size = add.length;
 			for (int i = 0; i < size; i++) {
 				IModule module3 = add[i];
 				IWebModule module = (IWebModule) module3.getAdapter(IWebModule.class);
 				String contextRoot = module.getContextRoot();
 				if (contextRoot != null && !contextRoot.startsWith("/"))
 					contextRoot = "/" + contextRoot;
 				WebModule module2 = new WebModule(contextRoot,
 						module.getLocation().toOSString(), module3.getId(), true);
 				config.addWebModule(-1, module2);
 			}
 		}
 		
 		if (remove != null) {
 			int size2 = remove.length;
 			for (int j = 0; j < size2; j++) {
 				IModule module3 = remove[j];
 				String memento = module3.getId();
 				List modules = getTomcatConfiguration().getWebModules();
 				int size = modules.size();
 				for (int i = 0; i < size; i++) {
 					WebModule module = (WebModule) modules.get(i);
 					if (memento.equals(module.getMemento()))
 						config.removeWebModule(i);
 				}
 			}
 		}
 		config.save(config.getFolder(), monitor);
 	}
 }
