 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-shell Project
 //
 // TomcatServer.java
 // Since: Sep 12, 2007
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.shell.tomcat;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.nio.channels.FileChannel;
 import java.util.List;
 
 import org.apache.catalina.Context;
 import org.apache.catalina.Engine;
 import org.apache.catalina.LifecycleException;
 import org.apache.catalina.core.StandardHost;
 import org.apache.catalina.startup.Embedded;
 import org.apache.catalina.startup.HostConfig;
 import org.xerial.core.XerialErrorCode;
 import org.xerial.core.XerialException;
 import org.xerial.util.FileResource;
 import org.xerial.util.io.VirtualFile;
 import org.xerial.util.log.Logger;
 import org.xerial.util.opt.Option;
 import org.xerial.util.opt.OptionParser;
 import org.xerial.util.opt.OptionParserException;
 
 /**
  * Embedded Tomcat Server
  * 
  * @author leo
  * 
  */
 public class TomcatServer {
 	private static Logger _logger = Logger.getLogger(TomcatServer.class);
 
 	private TomcatServerConfiguration configuration;
 	private Embedded embeddedTomcat = null;
 	private Engine tomcatEngine = null;
 	private StandardHost tomcatHost = null;
 
 	static {
 	}
 
 	public static class Opt {
 		@Option(symbol = "h", longName = "help", description = "display help message")
 		boolean displayHelp = false;
 
 		@Option(symbol = "p", longName = "port", varName = "PORT", description = "server port number. default=8989")
 		int port = 8989;
 
 		@Option(symbol = "t", longName = "tomcat_home", varName = "TOMCAT_HOME", description = "set the home directory of the Tomcat engine")
 		String catalinaBase = null;
 
 		@Option(symbol = "c", longName = "contextPath", varName = "path", description = "/path: URL path of the web application")
 		String contextPath = null;
 
 		@Option(symbol = "d", longName = "docBase", varName = "(WAR or docBase)", description = "path to the war file or docBase to deploy")
 		String docBase = null;
 	}
 
 	/**
 	 * entry point for running Tomcat from CUI
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		Opt opt = new Opt();
 		final OptionParser optionParser = new OptionParser(opt);
 		final TomcatServerConfiguration config = new TomcatServerConfiguration();
 
 		try {
 			optionParser.parse(args);
 
 			config.setPort(opt.port);
 
 			if (opt.displayHelp) {
 				throw new OptionParserException(XerialErrorCode.MISSING_ARGUMENT, "help");
 			}
 
 			if (opt.catalinaBase != null)
 				config.setCatalinaBase(opt.catalinaBase);
 
 			// start the server
 			final TomcatServer server = new TomcatServer(config);
 			server.start();
 
 			// deploy a given war
 			if (opt.contextPath != null && opt.docBase != null) {
 				File docBase = new File(opt.docBase);
 				server.addContext(opt.contextPath, docBase.getAbsolutePath());
 			}
 
 			Runtime.getRuntime().addShutdownHook(new Thread() {
 				@Override
 				public void run() {
 					try {
 						server.stop();
 					}
 					catch (XerialException e) {
 						_logger.error(e);
 					}
 				}
 			});
 
 			// wait until Ctrl+C terminates the program
 			while (true) {
 				Thread.sleep(10000000000000000L);
 			}
 
 		}
 		catch (OptionParserException e) {
 			if (e.getMessage().equals("help")) {
 				System.out.println("> TomcatServer [option]");
 				optionParser.printUsage();
 			}
 			else
 				System.err.println(e.getMessage());
 			return;
 		}
 		catch (XerialException e) {
 			_logger.error(e);
 		}
 		catch (InterruptedException e) {
 			_logger.error(e);
 		}
 
 	}
 
 	/**
 	 * Creates a TomcatServer instance with the specified port
 	 * 
 	 * @param port
 	 *            port used by the tomcat
 	 * @throws XerialException
 	 */
 	public TomcatServer(int port) throws XerialException {
 		this(TomcatServerConfiguration.newInstance(port));
 	}
 
 	/**
 	 * Configures the tomcat server
 	 * 
 	 * @param configuration
 	 *            configuration parameters
 	 * @throws XerialException
 	 */
 	public TomcatServer(TomcatServerConfiguration configuration) throws XerialException {
 		setConfiguration(configuration);
 
 		_logger.debug("port: " + configuration.getPort());
 		_logger.debug("catalina base: " + configuration.getCatalinaBase());
 
 		try {
 			prepareScaffold(configuration.getCatalinaBase());
 		}
 		catch (IOException e) {
 			throw new XerialException(XerialErrorCode.IO_EXCEPTION, e);
 		}
 
 		// configure a logger
 		String logConfigPath = new File(configuration.getCatalinaBase(), "conf/logging.properties").getPath();
 		System.setProperty("catalina.base", configuration.getCatalinaBase());
 		System.setProperty("java.util.logging.manager", "org.apache.juli.ClassLoaderLogManager");
 		System.setProperty("java.util.logging.config.file", logConfigPath);
 
 		for (String p : new String[] { "catalina.base", "java.util.logging.manager", "java.util.logging.config.file" })
 			_logger.info(String.format("%s = %s", p, System.getProperty(p)));
 
 		if (_logger.isDebugEnabled())
 			_logger.debug("juli log config file: " + logConfigPath);
 
 	}
 
 	public void setConfiguration(TomcatServerConfiguration configuration) {
 		this.configuration = configuration;
 	}
 
 	private ClassLoader getExtensionClassLoader() {
 		ClassLoader cl = Thread.currentThread().getContextClassLoader();
 		if (cl != null) {
 			ClassLoader parent = cl.getParent();
 			if (parent != null)
 				cl = parent;
 		}
 
 		return cl;
 
 	}
 
 	/**
 	 * Starts a Tomcat server
 	 * 
 	 * @throws TomcatException
 	 *             when failed to launch tomcat
 	 */
 	public void start() throws XerialException {
 
 		// Create an embedded server
 		embeddedTomcat = new Embedded();
 		embeddedTomcat.setAwait(true);
 		embeddedTomcat.setCatalinaBase(configuration.getCatalinaBase());
 
 		// Create an engine
 		tomcatEngine = embeddedTomcat.createEngine();
 		tomcatEngine.setName("utgb");
 		tomcatEngine.setDefaultHost("localhost");
		tomcatEngine.setParentClassLoader(getExtensionClassLoader());
 
 		// Create a default virtual host
 		String appBase = configuration.getCatalinaBase() + "/webapps";
 		_logger.debug("appBase: " + appBase);
 		tomcatHost = (StandardHost) embeddedTomcat.createHost("localhost", appBase);
 
 		// Hook up a host config to search for and pull in webapps.
 		HostConfig hostConfig = new HostConfig();
 		tomcatHost.addLifecycleListener(hostConfig);
 
 		// Tell the engine about the host
 		tomcatEngine.addChild(tomcatHost);
 		tomcatEngine.setDefaultHost(tomcatHost.getName());
 
 		// Tell the embedded manager about the engine
 		embeddedTomcat.addEngine(tomcatEngine);
 
 		// Tell the embedded server about the connector
 		InetAddress nullAddr = null;
 		org.apache.catalina.connector.Connector conn = embeddedTomcat.createConnector(nullAddr, configuration.getPort(), false);
 		conn.setEnableLookups(true);
 		// connector.setProxyPort(configuration.getAjp13port());
 		embeddedTomcat.addConnector(conn);
 
 		// Add AJP13 connector
 		// <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
 
 		try {
 			org.apache.catalina.connector.Connector ajp13connector = new org.apache.catalina.connector.Connector("org.apache.jk.server.JkCoyoteHandler");
 			ajp13connector.setPort(configuration.getAjp13port());
 			ajp13connector.setProtocol("AJP/1.3");
 			ajp13connector.setRedirectPort(8443);
 			embeddedTomcat.addConnector(ajp13connector);
 		}
 		catch (Exception e1) {
 			throw new XerialException(XerialErrorCode.INVALID_STATE, e1);
 		}
 
 		// create the ROOT context
 		// Context rootContext = embeddedTomcat.createContext("", "ROOT");
 		// tomcatHost.addChild(rootContext);
 
 		// // add manager context
 		// Context managerContext = embeddedTomcat.createContext("/manager",
 		// "manager");
 		// managerContext.setPrivileged(true);
 		// tomcatHost.addChild(managerContext);
 
 		// start up the tomcat
 		try {
 			embeddedTomcat.start();
 		}
 		catch (LifecycleException e) {
 			_logger.error(e);
 			String m = e.getMessage();
 			if (m != null && m.indexOf("already in use") != -1)
 				m = "port " + configuration.getPort() + " is already in use. You probably have another tomcat listening the same port";
 
 			// releaseTomcatResources();
 
 			throw new XerialException(XerialErrorCode.INVALID_STATE, m);
 		}
 
 	}
 
 	public void registerWAR(String contextPath, String pathToTheWarFile) throws XerialException {
 		addContext(contextPath, pathToTheWarFile);
 	}
 
 	public void addContext(String contextPath, String docBase) throws XerialException {
 		if (embeddedTomcat == null)
 			throw new XerialException(XerialErrorCode.INVALID_STATE, "tomcat server is not started yet.");
 
 		_logger.debug("deploy: contextPath=" + contextPath + ", docBase=" + docBase);
 
 		Context context = embeddedTomcat.createContext(contextPath, docBase);
 		// load the META-INF/context.xml
 		context.setConfigFile(docBase + "/META-INF/context.xml");
 		tomcatHost.addChild(context);
 	}
 
 	/*
 	 * private void releaseTomcatResources() { embeddedTomcat = null; tomcatEngine = null; tomcatHost = null; }
 	 */
 
 	/**
 	 * Stops the tomcat server
 	 * 
 	 * @throws TomcatException
 	 *             failed to stop tomcat
 	 */
 	public void stop() throws XerialException {
 		if (embeddedTomcat != null) {
 			try {
 				embeddedTomcat.stop();
 				embeddedTomcat = null;
 			}
 			catch (LifecycleException e) {
 				throw new XerialException(XerialErrorCode.INVALID_STATE, e);
 			}
 			finally {
 				// releaseTomcatResources();
 			}
 		}
 	}
 
 	/**
 	 * Creates the folder structure for the tomcat
 	 * 
 	 * @param catalinaBase
 	 * @throws IOException
 	 */
 	private void prepareScaffold(String catalinaBase) throws IOException {
 		// create the base folder for the scaffold
 		File tomcatBase = new File(catalinaBase);
 
		List<VirtualFile> tomcatResources = FileResource.listResources("org.utgenome.shell.tomcat.scaffold");
 
 		if (tomcatResources.size() <= 0)
			throw new IllegalStateException("org.utgenome.shell.tomcat.scaffold is not found");
 
 		// sync scaffoldDir with tomcatBase
 		for (VirtualFile vf : tomcatResources) {
 			String srcLogicalPath = vf.getLogicalPath();
 			File targetFile = new File(tomcatBase, srcLogicalPath);
 
 			if (vf.isDirectory()) {
 				targetFile.mkdirs();
 			}
 			else {
 				_logger.debug("rsync: src=" + vf.getLogicalPath() + " target=" + targetFile.getPath());
 
 				File parentFolder = targetFile.getParentFile();
 				parentFolder.mkdirs();
 
 				// copy the file content without overwrite
 				if (!targetFile.exists()) {
 					InputStream reader = vf.getURL().openStream();
 					FileOutputStream writer = new FileOutputStream(targetFile);
 					byte[] buffer = new byte[1024];
 					int bytesRead = 0;
 					while ((bytesRead = reader.read(buffer)) > 0) {
 						writer.write(buffer, 0, bytesRead);
 					}
 				}
 			}
 		}
 
 	}
 
 	private static String packagePath(String packageName) {
 		String packageAsPath = packageName.replaceAll("\\.", "/");
 		return packageAsPath.endsWith("/") ? packageAsPath : packageAsPath + "/";
 	}
 
 	public File findDir(String resourceName) {
 		String resourcePath = packagePath(resourceName);
 		if (!resourcePath.startsWith("/"))
 			resourcePath = "/" + resourcePath;
 		URL resourceURL = this.getClass().getResource(resourcePath);
 		if (resourceURL != null) {
 			_logger.debug("found resource:" + resourceURL);
 			String protocol = resourceURL.getProtocol();
 			if (protocol.equals("file")) {
 				try {
 					return new File(resourceURL.toURI());
 				}
 				catch (URISyntaxException e) {
 					_logger.error(e);
 				}
 			}
 
 			return new File(resourceURL.toString());
 		}
 		return null;
 	}
 
 	/**
 	 * Sync the srcDir content to the targetDir (no overwrite is performed from the source to the target)
 	 * 
 	 * @param srcDir
 	 * @param targetDir
 	 * @throws IOException
 	 */
 	private void rsync(File srcDir, File targetDir) throws IOException {
 		_logger.trace("rsync: src=" + srcDir + ", dest=" + targetDir);
 		/*
 		 * if (!srcDir.isDirectory()) throw new IllegalStateException(srcDir + " is not a directory");
 		 */
 
 		// omit the .svn folders
 		if (srcDir.getName() == ".svn")
 			return;
 
 		// create directories
 		targetDir.mkdirs();
 
 		for (File file : srcDir.listFiles()) {
 			String fileName = file.getName();
 			if (file.isDirectory()) {
 				rsync(file, new File(targetDir, fileName));
 				continue;
 			}
 
 			if (fileName.startsWith("~"))
 				continue;
 
 			File newFile = new File(targetDir, fileName);
 			// copy a file
 			copyWithNoOverwrite(file, newFile);
 		}
 	}
 
 	private void copyWithNoOverwrite(File from, File to) throws IOException {
 		if (to.exists())
 			return;
 		FileChannel srcChannel = new FileInputStream(from).getChannel();
 		FileChannel destChannel = new FileOutputStream(to).getChannel();
 		srcChannel.transferTo(0, srcChannel.size(), destChannel);
 		srcChannel.close();
 		destChannel.close();
 	}
 
 }
