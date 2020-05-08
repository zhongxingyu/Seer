 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc..
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Incorporated - initial API and implementation
  *******************************************************************************/
 package org.jboss.tools.deltacloud.integration.wizard;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.text.MessageFormat;
 import java.util.Iterator;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
 import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.IRuntimeType;
 import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.IServerType;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
 import org.jboss.ide.eclipse.as.core.server.internal.DeployableServer;
 import org.jboss.ide.eclipse.as.core.util.ServerCreationUtils;
 import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
 import org.jboss.tools.common.jobs.ChainedJob;
 import org.jboss.tools.deltacloud.core.DeltaCloudInstance;
 import org.jboss.tools.deltacloud.integration.DeltaCloudIntegrationPlugin;
 
 /**
  * @author Rob Stryker
  * @author André Dietisheim
  */
 public class CreateServerFromRSEJob extends ChainedJob {
 	public static final String JBOSS_HOME_KEY = "JBOSS_HOME";
 	public static final String JBOSS_CONFIG_KEY = "JBOSS_CONFIG";
 	public static final String CREATE_DEPLOY_ONLY_SERVER = "CREATE_DEPLOY_ONLY_SERVER";
 	public static final String CHECK_SERVER_FOR_DETAILS = "CHECK_SERVER_FOR_DETAILS";
 	public static final String SET_DETAILS_NOW = "SET_DETAILS_NOW";
 
 	private String type;
 	private String[] data;
 	private IHost host;
 	private String imageId;
 	private DeltaCloudInstance instance;
 
 	public CreateServerFromRSEJob(String type, String[] data, DeltaCloudInstance instance) {
 		this(type, data, instance.getImageId());
 		this.instance = instance;
 	}
 
 	public CreateServerFromRSEJob(String type, String[] data, String imageId) {
 		super("Create Server From RSE Host");
 		this.data = data;
 		this.type = type;
 		this.imageId = imageId;
 	}
 
 	public void setHost(IHost host) {
 		this.host = host;
 	}
 
 	protected IStatus run(IProgressMonitor monitor) {
 		try {
 			monitor.setTaskName(MessageFormat.format("Creating server adapter for {0}...", getHostName()));
 			if (type.equals(CREATE_DEPLOY_ONLY_SERVER)) {
 				createDeployOnlyServer();
 			} else if (type.equals(CHECK_SERVER_FOR_DETAILS)) {
 				createServerCheckRemoteDetails(monitor);
 			} else if (type.equals(SET_DETAILS_NOW)) {
 				createServerSetDetailsNow();
 			}
 		} catch (CoreException ce) {
 			return ce.getStatus();
 		} finally {
 			monitor.done();
 		}
 
 		return Status.OK_STATUS;
 	}
 
 	private Object getHostName() {
 		String hostname = "";
 		if (host != null) {
 			hostname = host.getName();
 		}
 		return hostname;
 	}
 
 	protected IServer createDeployOnlyServer() throws CoreException {
 		IServer server = createDeployOnlyServerWithRuntime(data[0], data[0], imageId);
 		server = RSEUtils.setServerToRSEMode(server, host);
 		return server;
 	}
 
 	protected String loadRemoteFileData(String path, IRemoteFileSubSystem system) throws CoreException {
 		Throwable e;
 		IPath p = new Path(path);
 		IPath remoteParent = p.removeLastSegments(1);
 		String remoteFile = p.lastSegment();
 		try {
 			boolean exists = system.getRemoteFileObject(path, new NullProgressMonitor()).exists();
 			if (!exists) {
 				throw new CoreException(new Status(IStatus.ERROR, DeltaCloudIntegrationPlugin.PLUGIN_ID, 
 						MessageFormat.format("Remote file {0} not found ", path)));
 			}
 
 			Writer writer = new StringWriter();
 			char[] buffer = new char[1024];
 
 			InputStream is = system.getInputStream(remoteParent.toOSString(), remoteFile, false,
 					new NullProgressMonitor());
 			Reader reader = new BufferedReader(new InputStreamReader(is));
 			int n;
 			try {
 				while ((n = reader.read(buffer)) != -1) {
 					writer.write(buffer, 0, n);
 				}
 				return writer.toString();
 			} finally {
 				is.close();
 			}
 		} catch (SystemMessageException sme) {
 			e = sme;
 		} catch (IOException ioe) {
 			e = ioe;
 		}
 		if (e.getCause() != null)
 			e = e.getCause();
 		if (e != null)
 			throw new CoreException(new Status(IStatus.ERROR, DeltaCloudIntegrationPlugin.PLUGIN_ID,
 					e.getMessage()));
 		return null;
 	}
 
 	protected Properties turnRemoteFileIntoProperties(String content) {
 		Properties p = new Properties();
 		if (content == null)
 			return p;
 		String[] byLine = content.split("\n");
 		String line, key, val;
 		int eqIn;
 		for (int i = 0; i < byLine.length; i++) {
 			line = byLine[i].trim();
 			eqIn = line.indexOf("=");
 			if (eqIn != -1) {
 				key = line.substring(0, eqIn);
 				val = line.substring(eqIn + 1);
 				while (val.contains("$")) {
 					String tmpKey;
 					Iterator<?> j = p.keySet().iterator();
 					while (j.hasNext()) {
 						tmpKey = j.next().toString();
 						val = val.replace("$" + tmpKey, p.getProperty(tmpKey));
 					}
 				}
 				p.put(key, val);
 			}
 		}
 		return p;
 	}
 
 	protected void createServerCheckRemoteDetails(IProgressMonitor monitor) throws CoreException {
 		IRemoteFileSubSystem system = org.jboss.tools.deltacloud.integration.rse.util.RSEUtils.findRemoteFileSubSystem(host);
 		if (system != null) {
 			ensureIsConnected(system, monitor);
 
 			String contents = null;
 			CoreException ce = null;
 			try {
 				contents = loadRemoteFileData(data[0], system);
 			} catch (CoreException ce2) {
 				ce = ce2;
 			}
 
 			Properties props = turnRemoteFileIntoProperties(contents);
 			String home = (String) props.get(JBOSS_HOME_KEY);
 			String config = (String) props.get(JBOSS_CONFIG_KEY);
 
 			if (home != null && config != null) {
 				String rtId = data[1];
 				IRuntime runtime = ServerCore.findRuntime(rtId);
 				IServer newServer = null;
 				newServer = ServerCreationUtils.createServer2(instance.getAlias(), runtime);
 				newServer = RSEUtils.setServerToRSEMode(newServer, host, home, config);
 			}
 
 			// Handle the case in which the file doesn't exist, or home /
 			// config
 			// are null
 			final Properties props2 = props;
 			final Exception ce2 = ce;
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 					handleRemoteFileIncomplete(host, props2, ce2);
 				}
 			});
 		}
 	}
 
 	private void ensureIsConnected(IRemoteFileSubSystem system, IProgressMonitor monitor) throws CoreException {
 		try {
 			org.jboss.tools.deltacloud.integration.rse.util.RSEUtils.connect(system, monitor);
 		} catch (Exception e) {
 			throw new CoreException(
 					new Status(IStatus.ERROR, DeltaCloudIntegrationPlugin.PLUGIN_ID, MessageFormat.format(
 							"Could not connect to host {0}", system.getName()), e));
 		}
 	}
 
 	protected IServer handleRemoteFileIncomplete(IHost host, Properties props, Exception e) {
 		String message = "";
 		if (e != null) {
 			message = "Error reading remote file: " + data[0] + ", " + e.getMessage();
 		} else {
 			String home = (String) props.get(JBOSS_HOME_KEY);
 			String config = (String) props.get(JBOSS_CONFIG_KEY);
 			if (home == null)
 				message += "Remote property file missing JBOSS_HOME property.\n";
 			if (config == null)
 				message += "Remote property file missing JBOSS_CONFIG property.";
 		}
 		message += "\n\nTry again?";
 
 		MessageBox messageBox = new MessageBox(new Shell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
 		messageBox.setText("Cannot create Server");
 		messageBox.setMessage(message);
 		int buttonID = messageBox.open();
 		switch (buttonID) {
 		case SWT.OK:
 			// saves changes ...
 			ConvertRSEToServerWizard w = new ConvertRSEToServerWizard(instance, host);
 			new WizardDialog(new Shell(), w).open();
 			break;
 		case SWT.CANCEL:
 			break;
 		}
 		return null;
 	}
 
 	protected void createServerSetDetailsNow() throws CoreException {
 		String home = data[0];
 		String config = data[1];
 		String rtId = data[2];
 		IRuntime runtime = ServerCore.findRuntime(rtId);
 		IServer newServer = ServerCreationUtils.createServer2(instance.getAlias(), runtime);
 		newServer = RSEUtils.setServerToRSEMode(newServer, host, home, config);
 	}
 
 	private static IRuntime findOrCreateStubDeployOnlyRuntime() throws CoreException {
 		IRuntime[] rts = ServerCore.getRuntimes();
 		for (int i = 0; i < rts.length; i++) {
 			if (rts[i].getRuntimeType().getId().equals("org.jboss.ide.eclipse.as.runtime.stripped")) {
 				return rts[i];
 			}
 		}
 		IRuntimeType rt = ServerCore.findRuntimeType("org.jboss.ide.eclipse.as.runtime.stripped");
 		IRuntimeWorkingCopy wc = rt.createRuntime("Deploy Only Runtime", null);
 		IRuntime runtime = wc.save(true, null);
 		return runtime;
 	}
 
 	private static IServer createDeployOnlyServerWithRuntime(String deployLocation, String tempDeployLocation,
 			String serverName) throws CoreException {
 		IRuntime rt = findOrCreateStubDeployOnlyRuntime();
 		IServerType st = ServerCore.findServerType("org.jboss.ide.eclipse.as.systemCopyServer");
 		ServerWorkingCopy swc = (ServerWorkingCopy) st.createServer(serverName, null, null);
 		swc.setServerConfiguration(null);
 		swc.setName(serverName);
 		swc.setRuntime(rt);
 		swc.setAttribute(DeployableServer.DEPLOY_DIRECTORY, deployLocation);
 		swc.setAttribute(DeployableServer.TEMP_DEPLOY_DIRECTORY, tempDeployLocation);
 		IServer server = swc.save(true, null);
 		return server;
 	}
 
 }
