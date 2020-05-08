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
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jst.server.tomcat.core.internal.xml.Factory;
 import org.eclipse.jst.server.tomcat.core.internal.xml.XMLUtil;
 import org.eclipse.jst.server.tomcat.core.internal.xml.server40.*;
 import org.eclipse.osgi.util.NLS;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import org.eclipse.wst.server.core.ServerPort;
 /**
  * Tomcat v5.5 server configuration.
  */
 public class Tomcat55Configuration extends TomcatConfiguration {
 	protected static final String DEFAULT_SERVICE = "Catalina";
 	protected Server server;
 	protected Factory serverFactory;
 	protected boolean isServerDirty;
 
 	protected WebAppDocument webAppDocument;
 
 	protected Document tomcatUsersDocument;
 
 	protected String policyFile;
 	protected boolean isPolicyDirty;
 
 	/**
 	 * Tomcat55Configuration constructor.
 	 * 
 	 * @param path a path
 	 */
 	public Tomcat55Configuration(IFolder path) {
 		super(path);
 	}
 
 	/**
 	 * Return the port number.
 	 * @return int
 	 */
 	public ServerPort getMainPort() {
 		Iterator iterator = getServerPorts().iterator();
 		while (iterator.hasNext()) {
 			ServerPort port = (ServerPort) iterator.next();
 			if (port.getName().equals("HTTP"))
 				return port;
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns the mime mappings.
 	 * @return java.util.List
 	 */
 	public List getMimeMappings() {
 		return webAppDocument.getMimeMappings();
 	}
 
 	/**
 	 * Returns a list of ServerPorts that this configuration uses.
 	 *
 	 * @return java.util.List
 	 */
 	public List getServerPorts() {
 		List ports = new ArrayList();
 	
 		// first add server port
 		try {
 			int port = Integer.parseInt(server.getPort());
 			ports.add(new ServerPort("server", Messages.portServer, port, "TCPIP"));
 		} catch (Exception e) {
 			// ignore
 		}
 	
 		// add connectors
 		try {
 			int size = server.getServiceCount();
 			for (int i = 0; i < size; i++) {
 				Service service = server.getService(i);
 				int size2 = service.getConnectorCount();
 				for (int j = 0; j < size2; j++) {
 					Connector connector = service.getConnector(j);
 					String name = "HTTP";
					String protocol2 = "HTTP";
 					boolean advanced = true;
 					String[] contentTypes = null;
 					int port = -1;
 					try {
 						port = Integer.parseInt(connector.getPort());
 					} catch (Exception e) {
 						// ignore
 					}
 					String protocol = connector.getProtocol();
 					if (protocol != null && protocol.length() > 0) {
 						name = protocol;
 						protocol2 = protocol; 
 					}
 					if ("HTTP".equals(protocol))
 						contentTypes = new String[] { "web", "webservices" };
 					String secure = connector.getSecure();
 					if (secure != null && secure.length() > 0) {
 						name = "SSL";
 						protocol2 = "SSL";
 					} else
 						advanced = false;
 					ports.add(new ServerPort(i +"/" + j, name, port, protocol2, contentTypes, advanced));
 				}
 			}
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error getting server ports", e);
 		}
 		return ports;
 	}
 	
 	/**
 	 * Return a list of the web modules in this server.
 	 * @return java.util.List
 	 */
 	public List getWebModules() {
 		List list = new ArrayList();
 	
 		try {
 			int size = server.getServiceCount();
 			for (int i = 0; i < size; i++) {
 				Service service = server.getService(i);
 				if (service.getName().equalsIgnoreCase(DEFAULT_SERVICE)) {
 					Engine engine = service.getEngine();
 					Host host = engine.getHost();
 					int size2 = host.getContextCount();
 					for (int j = 0; j < size2; j++) {
 						Context context = host.getContext(j);
 						String reload = context.getReloadable();
 						if (reload == null)
 							reload = "false";
 						WebModule module = new WebModule(context.getPath(), 
 							context.getDocBase(), context.getSource(),
 							reload.equalsIgnoreCase("true") ? true : false);
 						list.add(module);
 					}
 				}
 			}
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error getting project refs", e);
 		}
 		return list;
 	}
 	
 	/**
 	 * @see TomcatConfiguration#load(IPath, IProgressMonitor)
 	 */
 	public void load(IPath path, IProgressMonitor monitor) throws CoreException {
 		try {
 			monitor = ProgressUtil.getMonitorFor(monitor);
 			monitor.beginTask(Messages.loadingTask, 5);
 			
 			// check for catalina.policy to verify that this is a v5.5 config
 			InputStream in = new FileInputStream(path.append("catalina.policy").toFile());
 			in.read();
 			in.close();
 			monitor.worked(1);
 
 			serverFactory = new Factory();
 			serverFactory.setPackageName("org.eclipse.jst.server.tomcat.core.internal.xml.server40");
 			server = (Server) serverFactory.loadDocument(new FileInputStream(path.append("server.xml").toFile()));
 			monitor.worked(1);
 
 			webAppDocument = new WebAppDocument(path.append("web.xml"));
 			monitor.worked(1);
 			
 			tomcatUsersDocument = XMLUtil.getDocumentBuilder().parse(new InputSource(new FileInputStream(path.append("tomcat-users.xml").toFile())));
 			monitor.worked(1);
 			
 			// load policy file
 			BufferedReader br = null;
 			try {
 				br = new BufferedReader(new InputStreamReader(new FileInputStream(path.append("catalina.policy").toFile())));
 				String temp = br.readLine();
 				policyFile = "";
 				while (temp != null) {
 					policyFile += temp + "\n";
 					temp = br.readLine();
 				}
 			} catch (Exception e) {
 				Trace.trace(Trace.WARNING, "Could not load policy file", e);
 			} finally {
 				if (br != null)
 					br.close();
 			}
 			monitor.worked(1);
 			
 			if (monitor.isCanceled())
 				return;
 			monitor.done();
 		} catch (Exception e) {
 			Trace.trace(Trace.WARNING, "Could not load Tomcat v5.5 configuration from " + path.toOSString() + ": " + e.getMessage());
 			throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorCouldNotLoadConfiguration, path.toOSString()), e));
 		}
 	}
 
 	public void importFromPath(IPath path, boolean isTestEnv, IProgressMonitor monitor) throws CoreException {
 		load(path, monitor);
 		
 		// for test environment, remove existing contexts since a separate
 		// catalina.base will be used
 		if (isTestEnv) {
 			int size = server.getServiceCount();
 			for (int i = 0; i < size; i++) {
 				Service service = server.getService(i);
 				if (service.getName().equalsIgnoreCase(DEFAULT_SERVICE)) {
 					Host host = service.getEngine().getHost();
 					int size2 = host.getContextCount();
 					for (int j = 0; j < size2; j++) {
 						host.removeElement("Context", 0);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @see TomcatConfiguration#load(IFolder, IProgressMonitor)
 	 */
 	public void load(IFolder folder, IProgressMonitor monitor) throws CoreException {
 		try {
 			monitor = ProgressUtil.getMonitorFor(monitor);
 			monitor.beginTask(Messages.loadingTask, 800);
 	
 			// check for catalina.policy to verify that this is a v4.0 config
 			IFile file = folder.getFile("catalina.policy");
 			if (!file.exists())
 				throw new CoreException(new Status(IStatus.WARNING, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorCouldNotLoadConfiguration, folder.getFullPath().toOSString()), null));
 	
 			// load server.xml
 			file = folder.getFile("server.xml");
 			InputStream in = file.getContents();
 			serverFactory = new Factory();
 			serverFactory.setPackageName("org.eclipse.jst.server.tomcat.core.internal.xml.server40");
 			server = (Server) serverFactory.loadDocument(in);
 			monitor.worked(200);
 	
 			// load web.xml
 			file = folder.getFile("web.xml");
 			webAppDocument = new WebAppDocument(file);
 			monitor.worked(200);
 	
 			// load tomcat-users.xml
 			file = folder.getFile("tomcat-users.xml");
 			in = file.getContents();
 			
 			tomcatUsersDocument = XMLUtil.getDocumentBuilder().parse(new InputSource(in));
 			monitor.worked(200);
 		
 			// load catalina.policy
 			file = folder.getFile("catalina.policy");
 			in = file.getContents();
 			BufferedReader br = null;
 			try {
 				br = new BufferedReader(new InputStreamReader(in));
 				String temp = br.readLine();
 				policyFile = "";
 				while (temp != null) {
 					policyFile += temp + "\n";
 					temp = br.readLine();
 				}
 			} catch (Exception e) {
 				Trace.trace(Trace.WARNING, "Could not load policy file", e);
 			} finally {
 				if (br != null)
 					br.close();
 			}
 			monitor.worked(200);
 	
 			if (monitor.isCanceled())
 				throw new Exception("Cancelled");
 			monitor.done();
 		} catch (Exception e) {
 			Trace.trace(Trace.WARNING, "Could not reload Tomcat v5.5 configuration from: " + folder.getFullPath() + ": " + e.getMessage());
 			throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorCouldNotLoadConfiguration, folder.getFullPath().toOSString()), e));
 		}
 	}
 
 	/**
 	 * Save to the given directory.
 	 * @param path a path
 	 * @param forceDirty boolean
 	 * @param monitor a progress monitor
 	 * @exception CoreException
 	 */
 	protected void save(IPath path, boolean forceDirty, IProgressMonitor monitor) throws CoreException {
 		try {
 			monitor = ProgressUtil.getMonitorFor(monitor);
 			monitor.beginTask(Messages.savingTask, 3);
 	
 			// make sure directory exists
 			if (!path.toFile().exists()) {
 				forceDirty = true;
 				path.toFile().mkdir();
 			}
 			monitor.worked(1);
 	
 			// save files
 			if (forceDirty || isServerDirty)
 				serverFactory.save(path.append("server.xml").toOSString());
 			monitor.worked(1);
 	
 			//if (forceDirty || isWebAppDirty)
 			//	webAppFactory.save(dirPath + "web.xml");
 			//webAppDocument.save(path.toOSString(), forceDirty || isPolicyDirty);
 			webAppDocument.save(path.append("web.xml").toOSString(), forceDirty);
 			monitor.worked(1);
 	
 			if (forceDirty)
 				XMLUtil.save(path.append("tomcat-users.xml").toOSString(), tomcatUsersDocument);
 			monitor.worked(1);
 	
 			if (forceDirty || isPolicyDirty) {
 				BufferedWriter bw = new BufferedWriter(new FileWriter(path.append("catalina.policy").toFile()));
 				bw.write(policyFile);
 				bw.close();
 			}
 			monitor.worked(1);
 			isServerDirty = false;
 			isPolicyDirty = false;
 	
 			if (monitor.isCanceled())
 				return;
 			monitor.done();
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Could not save Tomcat v5.5 configuration to " + path, e);
 			throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorCouldNotSaveConfiguration, new String[] {e.getLocalizedMessage()}), e));
 		}
 	}
 	
 	public void save(IPath path, IProgressMonitor monitor) throws CoreException {
 		save(path, true, monitor);
 	}
 
 	/**
 	 * Save the information held by this object to the given directory.
 	 *
 	 * @param folder a folder
 	 * @param monitor a progress monitor
 	 * @throws CoreException
 	 */
 	public void save(IFolder folder, IProgressMonitor monitor) throws CoreException {
 		try {
 			monitor = ProgressUtil.getMonitorFor(monitor);
 			monitor.beginTask(Messages.savingTask, 900);
 	
 			// save server.xml
 			byte[] data = serverFactory.getContents();
 			InputStream in = new ByteArrayInputStream(data);
 			IFile file = folder.getFile("server.xml");
 			if (file.exists()) {
 				if (isServerDirty)
 					file.setContents(in, true, true, ProgressUtil.getSubMonitorFor(monitor, 200));
 				else
 					monitor.worked(200);
 			} else
 				file.create(in, true, ProgressUtil.getSubMonitorFor(monitor, 200));
 	
 			// save web.xml
 			webAppDocument.save(folder.getFile("web.xml"), ProgressUtil.getSubMonitorFor(monitor, 200));
 	
 			// save tomcat-users.xml
 			data = XMLUtil.getContents(tomcatUsersDocument);
 			in = new ByteArrayInputStream(data);
 			file = folder.getFile("tomcat-users.xml");
 			if (file.exists())
 				monitor.worked(200);
 				//file.setContents(in, true, true, ProgressUtil.getSubMonitorFor(monitor, 200));
 			else
 				file.create(in, true, ProgressUtil.getSubMonitorFor(monitor, 200));
 	
 			// save catalina.policy
 			in = new ByteArrayInputStream(policyFile.getBytes());
 			file = folder.getFile("catalina.policy");
 			if (file.exists())
 				monitor.worked(200);
 				//file.setContents(in, true, true, ProgressUtil.getSubMonitorFor(monitor, 200));
 			else
 				file.create(in, true, ProgressUtil.getSubMonitorFor(monitor, 200));
 	
 			if (monitor.isCanceled())
 				return;
 			monitor.done();
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Could not save Tomcat v5.5 configuration to " + folder.toString(), e);
 			throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorCouldNotSaveConfiguration, new String[] {e.getLocalizedMessage()}), e));
 		}
 	}
 
 	protected static boolean hasMDBListener(Server server) {
 		if (server == null)
 			return false;
 		
 		int count = server.getListenerCount();
 		if (count == 0)
 			return false;
 			
 		for (int i = 0; i < count; i++) {
 			Listener listener = server.getListener(i);
 			if (listener != null && listener.getClassName() != null && listener.getClassName().indexOf("mbean") >= 0)
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * @see ITomcatConfigurationWorkingCopy#addMimeMapping(int, IMimeMapping)
 	 */
 	public void addMimeMapping(int index, IMimeMapping map) {
 		webAppDocument.addMimeMapping(index, map);
 		firePropertyChangeEvent(ADD_MAPPING_PROPERTY, new Integer(index), map);
 	}
 
 	/**
 	 * @see ITomcatConfigurationWorkingCopy#addWebModule(int, ITomcatWebModule)
 	 */
 	public void addWebModule(int index, ITomcatWebModule module) {
 		try {
 			int size = server.getServiceCount();
 			for (int i = 0; i < size; i++) {
 				Service service = server.getService(i);
 				if (service.getName().equalsIgnoreCase(DEFAULT_SERVICE)) {
 					Engine engine = service.getEngine();
 					Host host = engine.getHost();
 					Context context = (Context) host.createElement(index, "Context");
 					context.setDocBase(module.getDocumentBase());
 					context.setPath(module.getPath());
 					context.setReloadable(module.isReloadable() ? "true" : "false");
 					if (module.getMemento() != null && module.getMemento().length() > 0)
 						context.setSource(module.getMemento());
 					isServerDirty = true;
 					firePropertyChangeEvent(ADD_WEB_MODULE_PROPERTY, null, module);
 					return;
 				}
 			}
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error adding web module " + module.getPath(), e);
 		}
 	}
 
 	/**
 	 * Change the extension of a mime mapping.
 	 * 
 	 * @param index
 	 * @param map
 	 */
 	public void modifyMimeMapping(int index, IMimeMapping map) {
 		webAppDocument.modifyMimeMapping(index, map);
 		firePropertyChangeEvent(MODIFY_MAPPING_PROPERTY, new Integer(index), map);
 	}
 
 	/**
 	 * Modify the port with the given id.
 	 *
 	 * @param id java.lang.String
 	 * @param port int
 	 */
 	public void modifyServerPort(String id, int port) {
 		try {
 			if ("server".equals(id)) {
 				server.setPort(port + "");
 				isServerDirty = true;
 				firePropertyChangeEvent(MODIFY_PORT_PROPERTY, id, new Integer(port));
 				return;
 			}
 	
 			int i = id.indexOf("/");
 			int servNum = Integer.parseInt(id.substring(0, i));
 			int connNum = Integer.parseInt(id.substring(i + 1));
 			
 			Service service = server.getService(servNum);
 			Connector connector = service.getConnector(connNum);
 			connector.setPort(port + "");
 			isServerDirty = true;
 			firePropertyChangeEvent(MODIFY_PORT_PROPERTY, id, new Integer(port));
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error modifying server port " + id, e);
 		}
 	}
 	/**
 	 * Change a web module.
 	 * @param index int
 	 * @param docBase java.lang.String
 	 * @param path java.lang.String
 	 * @param reloadable boolean
 	 */
 	public void modifyWebModule(int index, String docBase, String path, boolean reloadable) {
 		try {
 			int size = server.getServiceCount();
 			for (int i = 0; i < size; i++) {
 				Service service = server.getService(i);
 				if (service.getName().equalsIgnoreCase(DEFAULT_SERVICE)) {
 					Engine engine = service.getEngine();
 					Host host = engine.getHost();
 					Context context = host.getContext(index);
 					context.setPath(path);
 					context.setDocBase(docBase);
 					context.setReloadable(reloadable ? "true" : "false");
 					isServerDirty = true;
 					WebModule module = new WebModule(path, docBase, null, reloadable);
 					firePropertyChangeEvent(MODIFY_WEB_MODULE_PROPERTY, new Integer(index), module);
 					return;
 				}
 			}
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error modifying web module " + index, e);
 		}
 	}
 
 	/**
 	 * Removes a mime mapping.
 	 * @param index int
 	 */
 	public void removeMimeMapping(int index) {
 		webAppDocument.removeMimeMapping(index);
 		firePropertyChangeEvent(REMOVE_MAPPING_PROPERTY, null, new Integer(index));
 	}
 
 	/**
 	 * Removes a web module.
 	 * @param index int
 	 */
 	public void removeWebModule(int index) {
 		try {
 			int size = server.getServiceCount();
 			for (int i = 0; i < size; i++) {
 				Service service = server.getService(i);
 				if (service.getName().equalsIgnoreCase(DEFAULT_SERVICE)) {
 					Engine engine = service.getEngine();
 					Host host = engine.getHost();
 					host.removeElement("Context", index);
 					isServerDirty = true;
 					firePropertyChangeEvent(REMOVE_WEB_MODULE_PROPERTY, null, new Integer(index));
 					return;
 				}
 			}
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error removing module ref " + index, e);
 		}
 	}
 
 	protected IStatus publishContextConfig(IPath baseDir, IProgressMonitor monitor) {
 		monitor = ProgressUtil.getMonitorFor(monitor);
 		monitor.beginTask(Messages.publishConfigurationTask, 300);
 
 		Trace.trace(Trace.FINER, "Apply context configurations");
 		IPath confDir = baseDir.append("conf");
 		IPath webappsDir = baseDir.append("webapps");
 		try {
 			monitor.subTask(Messages.publishContextConfigTask);
 			Factory factory = new Factory();
 			factory.setPackageName("org.eclipse.jst.server.tomcat.core.internal.xml.server40");
 			Server publishedServer = (Server) factory.loadDocument(new FileInputStream(confDir.append("server.xml").toFile()));
 			monitor.worked(100);
 			
 			boolean modified = false;
 
 			MultiStatus ms = new MultiStatus(TomcatPlugin.PLUGIN_ID, 0, Messages.publishContextConfigTask, null);
 			int size = publishedServer.getServiceCount();
 			for (int i = 0; i < size; i++) {
 				Service service = publishedServer.getService(i);
 				if (service.getName().equalsIgnoreCase(DEFAULT_SERVICE)) {
 					Engine engine = service.getEngine();
 					Host host = engine.getHost();
 					int size2 = host.getContextCount();
 					for (int j = 0; j < size2; j++) {
 						Context context = host.getContext(j);
 						monitor.subTask(NLS.bind(Messages.checkingContextTask,
 								new String[] {context.getPath()}));
 						if (addContextConfig(webappsDir, context, ms)) {
 							modified = true;
 						}
 					}
 				}
 			}
 			monitor.worked(100);
 			if (modified) {
 				monitor.subTask(Messages.savingContextConfigTask);
 				factory.save(confDir.append("server.xml").toOSString());
 			}
 			monitor.done();
 			
 			// If problem(s) occurred adding context configurations, return error status
 			if (ms.getChildren().length > 0) {
 				return ms;
 			}
 			Trace.trace(Trace.FINER, "Server.xml updated with context.xml configurations");
 			return Status.OK_STATUS;
 		} catch (Exception e) {
 			Trace.trace(Trace.WARNING, "Could not apply context configurations to published Tomcat v5.5 configuration from " + confDir.toOSString() + ": " + e.getMessage());
 			return new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorPublishConfiguration, new String[] {e.getLocalizedMessage()}), e);
 		}
 	}
 	
 	/**
 	 * If the specified Context is linked to a project, try to
 	 * update it with any configuration from a META-INF/context.xml found
 	 * relative to the specified web applications directory and context docBase.
 	 * @param webappsDir Path to server's web applications directory.
 	 * @param context Context object to receive context.xml contents.
 	 * @param ms MultiStatus object to receive error status.
 	 * @return Returns true if context is modified.
 	 */
 	protected boolean addContextConfig(IPath webappsDir, Context context, MultiStatus ms) {
 		boolean modified = false;
 		String source = context.getSource();
 		if (source != null && source.length() > 0 )
 		{
 			String docBase = context.getDocBase();
 			try {
 				Context contextConfig = loadContextConfig(webappsDir.append(docBase));
 				if (null != contextConfig) {
 					if (context.hasChildNodes())
 						context.removeChildren();
 					contextConfig.copyChildrenTo(context);
 					Map attrs = contextConfig.getAttributes();
 					Iterator iter = attrs.keySet().iterator();
 					while (iter.hasNext()) {
 						String name = (String) iter.next();
 						if (!name.equalsIgnoreCase("path")
 								&& !name.equalsIgnoreCase("docBase")
 								&& !name.equalsIgnoreCase("source")) {
 							String value = (String)attrs.get(name);
 							context.setAttributeValue(name, value);
 						}
 					}
 					modified = true;
 				}
 			} catch (Exception e) {
 				String contextPath = context.getPath();
 				if (contextPath.startsWith("/")) {
 					contextPath = contextPath.substring(1);
 				}
 				Trace.trace(Trace.SEVERE, "Error reading context.xml file for " + contextPath, e);
 				IStatus s = new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0,
 						NLS.bind(Messages.errorCouldNotLoadContextXml, contextPath), e);
 				ms.add(s);
 			}
 		}
 		return modified;
 	}
 	
 	/**
 	 * Tries to read a META-INF/context.xml file relative to the
 	 * specified web application path.  If found, it creates a Context object
 	 * containing the contexts of that file.
 	 * @param webappDir Path to the web application
 	 * @return Context element created from context.xml, or null if not found.
 	 * @throws SAXException If there is a error parsing the XML. 
 	 * @throws IOException If there is an error reading the file.
 	 */
 	protected Context loadContextConfig(IPath webappDir) throws IOException, SAXException {
 		File contextXML = new File(webappDir.toOSString()+ File.separator + "META-INF" + File.separator + "context.xml");
 		if (contextXML.exists()) {
 			try {
 				InputStream is = new FileInputStream(contextXML);
 				Factory ctxFactory = new Factory();
 				ctxFactory.setPackageName("org.eclipse.jst.server.tomcat.core.internal.xml.server40");
 				Context ctx = (Context)ctxFactory.loadDocument(is);
 				is.close();
 				return ctx;
 			} catch (FileNotFoundException e) {
 				// Ignore, should never occur
 			}
 		}
 		return null;
  	}
 
 	protected IStatus prepareRuntimeDirectory(IPath confDir) {
 		Trace.trace(Trace.FINER, "Preparing runtime directory");
 		// Prepare a catalina.base directory structure
 		File temp = confDir.append("conf").toFile();
 		if (!temp.exists())
 			temp.mkdirs();
 		temp = confDir.append("logs").toFile();
 		if (!temp.exists())
 			temp.mkdirs();
 		temp = confDir.append("temp").toFile();
 		if (!temp.exists())
 			temp.mkdirs();
 		IPath tempPath = confDir.append("webapps/ROOT/WEB-INF");
 		temp = tempPath.toFile();
 		if (!temp.exists())
 			temp.mkdirs();
 		temp = tempPath.append("web.xml").toFile();
 		if (!temp.exists()) {
 			FileWriter fw;
 			try {
 				fw = new FileWriter(temp);
 				fw.write(DEFAULT_WEBXML_SERVLET24);
 				fw.close();
 			} catch (IOException e) {
 				Trace.trace(Trace.WARNING, "Unable to create web.xml for ROOT context.", e);
 			}
 		}
 		temp = confDir.append("work").toFile();
 		if (!temp.exists())
 			temp.mkdirs();
 
 		return Status.OK_STATUS;		
 	}
 }
