 /******************************************************************************* 
  * Copyright (c) 2009 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.bpel.runtimes.module;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.model.IModuleResource;
 import org.eclipse.wst.server.core.model.IModuleResourceDelta;
 import org.jboss.ide.eclipse.archives.webtools.modules.LocalZippedPublisherUtil;
 import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
 import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
 import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
 import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
 import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
 import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
 import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
 import org.jboss.ide.eclipse.as.core.server.xpl.LocalCopyCallback;
 import org.jboss.ide.eclipse.as.core.server.xpl.PublishCopyUtil;
 import org.jboss.ide.eclipse.as.core.util.FileUtil;
 import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
 import org.jboss.ide.eclipse.as.core.util.IWTPConstants;
 import org.jboss.ide.eclipse.as.core.util.ServerConverter;
 import org.jboss.tools.bpel.runtimes.IBPELModuleFacetConstants;
 import org.jboss.tools.jmx.core.IMemento;
 import org.jboss.tools.jmx.core.util.XMLMemento;
 
 /**
  * This class allows you to publish a BPEL module specifically
  * to a JBossTools server entity. 
  * @author rob.stryker@jboss.com
  *
  */
 public class JBTBPELPublisher implements IJBossServerPublisher {
 	private IServer server;
 	private int publishState = IServer.PUBLISH_STATE_INCREMENTAL;
 	public JBTBPELPublisher() {
 	}
 	
 	public int getPublishState() {
 		return publishState;
 	}
 	
 	public boolean accepts(String method, IServer server, IModule[] module) {
 		if( LocalPublishMethod.LOCAL_PUBLISH_METHOD.equals(method) 
 				&& module != null && module.length > 0 
 				&& module[module.length-1] != null  
 				&& (
 						module[module.length-1].getModuleType().getId().equals(IBPELModuleFacetConstants.JBT_BPEL_MODULE_TYPE) ||
 						module[module.length-1].getModuleType().getId().equals(IBPELModuleFacetConstants.BPEL_MODULE_TYPE))
 					)
 			return true;
 		return false;
 	}
 
 	public IStatus publishModule(
 			IJBossServerPublishMethod method,
 			IServer server, IModule[] module, 
 			int publishType, IModuleResourceDelta[] delta, 
 			IProgressMonitor monitor) throws CoreException {
 
 		this.server = server;
 		IModule last = module[module.length-1];
 		IStatus status = null;
 		if(publishType == REMOVE_PUBLISH){
 			// https://jira.jboss.org/browse/JBIDE-7620
 			if (last.getProject()!=null)
 				removeAll(server, last.getProject());
         } else if( publishType == FULL_PUBLISH ){
         	// Publish a new version forced
         	status = publish(module, delta, publishType, monitor);
         	publishState = IServer.PUBLISH_STATE_NONE;
         } else if( publishType == INCREMENTAL_PUBLISH ) {
        	// Do nothing. This is intentional
        	publishState = IServer.PUBLISH_STATE_INCREMENTAL;
         }
         // https://issues.jboss.org/browse/JBDS-1573
         // hack: display a warning dialog.
         // Deployment validation should really be handled as a WizardFragment invoked from
         // org.eclipse.wst.server.ui.internal.wizard.ModifyModulesWizard
         // but there is no WizardFragment extension point for this class...
         // 
 		if (status!=null && !status.isOK()) {
 			final IStatus s = status;
 			Display.getDefault().syncExec(new Runnable() {
 				public void run() {
 					MessageDialog.openWarning(Display.getDefault()
 							.getActiveShell(), Messages.DeployError, s
 							.getMessage());
 				}
 			});
 		}
 		return status == null ? Status.OK_STATUS : status;
 	}
 	
 
 	protected IStatus publish(IModule[] moduleTree, 
 			IModuleResourceDelta[] delta, int publishType, IProgressMonitor monitor) throws CoreException {
 		ArrayList<IStatus> resultList = new ArrayList<IStatus>();
 		IDeployableServer ds = ServerConverter.getDeployableServer(server);
 		IModule last = moduleTree[moduleTree.length -1];
 		IPath deployPath = getDeployPath(moduleTree, ds);
 		IPath tempDeployPath = PublishUtil.getTempDeployFolder(moduleTree, ds);
 		IModuleResource[] members = PublishUtil.getResources(last, new NullProgressMonitor());
 		// https://issues.jboss.org/browse/JBDS-1573
 		// make sure the project has a deploy.xml (bpel-deploy.xml for backward compatibility).
 		boolean hasDeployXML = false;
 		for (int i=0; i<members.length; ++i) {
 			IModuleResource res = members[i];
 			String name = res.getName();
 			if ("deploy.xml".equals(name) || "bpel-deploy.xml".equals(name)) {
 				hasDeployXML = true;
 				break;
 			}
 		}
 		if (!hasDeployXML) {
 			Status ms = new Status(IStatus.ERROR,JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FULL_FAIL, 
 					NLS.bind(Messages.MissingDeployXML, last.getName()), null);
 			return ms;
 		}
 		if( shouldZip() ) {
 			String deployRoot = PublishUtil.getDeployRootFolder(
 					moduleTree, ds, ds.getDeployFolder(),
 					IJBossToolingConstants.LOCAL_DEPLOYMENT_LOC);
 			BPELZippedPublisherUtil util = new BPELZippedPublisherUtil(deployPath);
 			IStatus ret = util.publishModule(server, deployRoot, moduleTree, publishType, delta, monitor);
 			resultList.add(ret);
 		} else {
 			LocalCopyCallback handler = new LocalCopyCallback(server, deployPath, tempDeployPath);
 			PublishCopyUtil util = new PublishCopyUtil(handler);
 			resultList.addAll(Arrays.asList(util.initFullPublish(members, monitor)));
 		}
 		addDeployedPathToDescriptor(server, last.getProject(), deployPath); // persist it
 		pruneList(resultList);
 		if( resultList.size() > 0 ) {
 			MultiStatus ms = new MultiStatus(JBossServerCorePlugin.PLUGIN_ID, IEventCodes.JST_PUB_FULL_FAIL, 
 					NLS.bind(org.jboss.ide.eclipse.as.core.Messages.FullPublishFail, last.getName()), null);
 			for( int i = 0; i < resultList.size(); i++ )
 				ms.add(resultList.get(i));
 			return ms;
 		}
 		return Status.OK_STATUS;
 	}
 	
 	// Prune out ok status
 	protected void pruneList(ArrayList<IStatus> list) {
 		Iterator<IStatus> i = list.iterator();
 		while(i.hasNext()) {
 			if( i.next().isOK())
 				i.remove();
 		}
 	}
 	
 	protected boolean shouldZip() {
 		IDeployableServer ds = ServerConverter.getDeployableServer(server);
 		return ds == null || ds.zipsWTPDeployments();
 	}
 	
 	public static IPath getDeployPath(IModule[] moduleTree, IDeployableServer server) {
 		IPath path = PublishUtil.getDeployPath(moduleTree, server);
 		path = path.removeLastSegments(1).append(getNewLastSegment(moduleTree));
 		return path;
 	}
 	
 	public static String getNewLastSegment(IModule[] moduleTree) {
 		IModule last = moduleTree[moduleTree.length-1];
 		Calendar cal = Calendar.getInstance();
 		StringBuffer lastSeg = new StringBuffer(formatString(cal.get(Calendar.YEAR)));
 		lastSeg.append(formatString(cal.get(Calendar.MONTH) + 1));
 		lastSeg.append(formatString(cal.get(Calendar.DAY_OF_MONTH)));
 		lastSeg.append(formatString(cal.get(Calendar.HOUR_OF_DAY)));
 		lastSeg.append(formatString(cal.get(Calendar.MINUTE)));
 		lastSeg.append(formatString(cal.get(Calendar.SECOND)));
 
 		
 		return last.getName() + "-" + lastSeg.toString() + IWTPConstants.EXT_JAR;
 	}
 	
 	private static String formatString(int dateUnit){
 		if(String.valueOf(dateUnit).length() < 2){
 			return "0" + dateUnit;
 		}
 		
 		return String.valueOf(dateUnit);
 	}
 	
 	private static final String DEPLOYMENTS = "deployments";
 	private static final String PROJECT = "project";
 	private static final String NAME = "name";
 	private static final String VERSION = "version";
 	
 	protected static void save(IServer server, XMLMemento memento) {
 		try {
 			memento.save(new FileOutputStream(getDeployDetailsFile(server)));
 		} catch( IOException ioe) {
 			// TODO LOG
 		}
 	}
 	
 	public static void removeAll(IServer server, IProject project) {
 		String[] paths = getDeployedPathsFromDescriptor(server, project);
 		for( int i = 0; i < paths.length; i++ ) {
 			// remove them all, with full force!!! >=[
 			FileUtil.safeDelete(new File(paths[i]));
 		}
 		removeProjectFromDescriptor(server, project);
 	}
 	
 	public static void removeVersion(IServer server, IProject project, String path) {
 		// delete file
 		FileUtil.safeDelete(new File(path));
 		// remove from descriptor
 		removeVersionFromDescriptor(server, project, path);
 	}
 	
 	public static void removeVersionFromDescriptor(IServer server, IProject project, String path) {
 		File f = getDeployDetailsFile(server);
 		XMLMemento memento = null;
 		try {
 			memento = XMLMemento.createReadRoot(new FileInputStream(f));
 			IMemento[] projects = memento.getChildren(PROJECT);//$NON-NLS-1$
 			for( int i = 0; i < projects.length; i++ ) {
 				if( project.getName().equals(projects[i].getString(NAME)) ) {
 					IMemento[] versions = projects[i].getChildren(VERSION);
 					for( int j = 0; j < versions.length; j++ ) {
 						if( ((XMLMemento)versions[j]).getTextData().equals(path)) {
 							((XMLMemento)projects[i]).removeChild((XMLMemento)versions[j]);
 						}
 					}
 				}
 			}
 			save(server, memento);
 		} catch( FileNotFoundException fnfe) {}
 	}
 
 	public static void removeProjectFromDescriptor(IServer server, IProject project) {
 		File f = getDeployDetailsFile(server);
 		XMLMemento memento = null;
 		try {
 			memento = XMLMemento.createReadRoot(new FileInputStream(f));
 			IMemento[] projects = memento.getChildren(PROJECT);//$NON-NLS-1$
 			for( int i = 0; i < projects.length; i++ ) {
 				if( project.getName().equals(projects[i].getString(NAME)) ) {
 					memento.removeChild((XMLMemento)projects[i]);
 				}
 			}
 			save(server, memento);
 		} catch( FileNotFoundException fnfe) {}
 	}
 	
 	public static void addDeployedPathToDescriptor(IServer server, IProject project, IPath path) {
 		File f = getDeployDetailsFile(server);
 		XMLMemento memento = null;
 		try {
 			memento = XMLMemento.createReadRoot(new FileInputStream(f));
 		} catch( FileNotFoundException fnfe) {}
 		
 		if( memento == null )
 			memento = XMLMemento.createWriteRoot(DEPLOYMENTS);
 
 		IMemento[] projects = memento.getChildren(PROJECT);//$NON-NLS-1$
 		boolean projectFound = false;
 		for( int i = 0; i < projects.length; i++ ) {
 			if( project.getName().equals(projects[i].getString(NAME))) {
 				projectFound = true;
 				XMLMemento child = (XMLMemento)projects[i].createChild(VERSION);
 				child.putTextData(path.toOSString());
 			}
 		}
 		if( !projectFound ) {
 			XMLMemento proj = (XMLMemento)memento.createChild(PROJECT);
 			proj.putString(NAME, project.getName());
 			XMLMemento child = (XMLMemento)proj.createChild(VERSION);
 			child.putTextData(path.toOSString());
 		}
 		save(server, memento);
 	}
 	
 	public static String[] getDeployedPathsFromDescriptor(IServer server, IProject project) {
 		File f = getDeployDetailsFile(server);
 		ArrayList<String> list = new ArrayList<String>();
 		if( f.exists() ) {
 			try {
 				XMLMemento memento = XMLMemento.createReadRoot(new FileInputStream(f));
 				IMemento[] projects = memento.getChildren(PROJECT);//$NON-NLS-1$
 				for( int i = 0; i < projects.length; i++ ) {
 					if( project.getName().equals(projects[i].getString(NAME))) {
 						IMemento[] deployments = projects[i].getChildren(VERSION);
 						for( int j = 0; j < deployments.length; j++ ) {
 							String s = ((XMLMemento)deployments[j]).getTextData();
 							if( s != null && !s.equals(""))
 								list.add(s);
 						}
 						break;
 					}
 				}
 			} catch( FileNotFoundException fnfe) {}
 		}
 		return (String[]) list.toArray(new String[list.size()]);
 	}
 	
 	public static File getDeployDetailsFile(IServer server) {
 		return JBossServerCorePlugin.getServerStateLocation(server)
 					.append("bpel.deployment.versions").toFile();
 	}
 	
 	public static class BPELZippedPublisherUtil extends LocalZippedPublisherUtil {
 		private IPath deployPath;
 		public BPELZippedPublisherUtil(IPath deployPath) {
 			this.deployPath = deployPath;
 		}
 		
 		@Override
 		// https://issues.jboss.org/browse/JBIDE-6617
 		// This was probably a typo - the actual override should have an IModule[] arg
 		public IPath getOutputFilePath(IModule[] module) {
 			return getOutputFilePath();
 		}
 
 		public IPath getOutputFilePath() {
 			return deployPath;
 		}
 	}
 }
