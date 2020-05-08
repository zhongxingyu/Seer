 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.wizards.appregister;
 
 import java.text.MessageFormat;
 import java.util.*;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.wizard.*;
 import org.jboss.tools.common.meta.action.SpecialWizard;
 import org.jboss.tools.common.meta.key.WizardKeys;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.filesystems.impl.FileSystemImpl;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.reporting.ProblemReportingHelper;
 import org.jboss.tools.common.model.ui.*;
 import org.jboss.tools.jst.web.context.RegisterServerContext;
 import org.jboss.tools.jst.web.server.RegistrationHelper;
 import org.jboss.tools.jst.web.ui.Messages;
 import org.jboss.tools.jst.web.ui.WebUiPlugin;
 
 public class AppRegisterWizard extends Wizard implements SpecialWizard {
 	Properties p;
 	XModelObject object;
 	RegisterServerContext registry;	
 	AppRegisterWizardPage page;
 	
 	public AppRegisterWizard() {
 		setHelpAvailable(false);
 	}
 	
 	public boolean performFinish() {
 		Job job = new RegisterServerJob();
 		job.schedule(100);
 		return true;
 	}
 	
 	class RegisterServerJob extends Job {
 
 		public RegisterServerJob() {
 			super(Messages.AppRegisterWizard_RegisterInServer);
 		}
 
 		protected IStatus run(IProgressMonitor monitor) {
 			try {
 				ModelPlugin.getWorkspace().run(new WR(), monitor);
 			} catch (Exception e) {
 				WebUiPlugin.getPluginLog().logError(e);
 			}
 			return Status.OK_STATUS;
 		}
 	}
 	
 	class WR implements IWorkspaceRunnable {
 		public void run(IProgressMonitor monitor) throws CoreException {
 			try {
 				register(monitor);
 			} catch (Exception e) {
 				WebUiPlugin.getPluginLog().logError(e);
 			}
 		}
 		
 	}
 
 	private void register(IProgressMonitor monitor) throws Exception {
 		if(monitor != null) monitor.beginTask("", 100); //$NON-NLS-1$
 		if(monitor != null) monitor.worked(5);
 
 		IServer[] servers = registry.getTargetServers();
 		int step = 70;
 		if(monitor != null) {
 			monitor.worked(5);
 			step = step / (2 * servers.length + 1);
 		}
 		IProject p = EclipseResourceUtil.getProject(object);
 		String contextRoot = registry.getApplicationName();
 		if(!contextRoot.equals(ComponentUtilities.getServerContextRoot(p))) {
 			ComponentUtilities.setServerContextRoot(p, contextRoot);
 		}
 		for (int i = 0; i < servers.length; i++) {
 			if(monitor != null) {
 				monitor.worked(step);
 				monitor.subTask(servers[i].getName());
 			}
 			RegistrationHelper.register(p, servers[i]);
 			if(monitor != null) monitor.worked(step);
 		}
 		object.getModel().changeObjectAttribute(object, "application name", registry.getApplicationName()); //$NON-NLS-1$
 		if(monitor != null) monitor.worked(20);
 	}
 
 	public String getWebRootLocation() {
 		XModelObject fs = object.getModel().getByPath("FileSystems/WEB-ROOT"); //$NON-NLS-1$
 		///if(fs == null) fs = getRootFileSystemForModule(model, "");
 		if(!(fs instanceof FileSystemImpl)) return null;
 		return ((FileSystemImpl)fs).getAbsoluteLocation();
 	}
     
 	public void setObject(Object object) {
 		p = (Properties)object;
 		registry = new RegisterServerContext(RegisterServerContext.PROJECT_MODE_EXISTING);
 		this.object = (XModelObject)p.get("object"); //$NON-NLS-1$
 		registry.setProjectHandle(EclipseResourceUtil.getProject(this.object));
 		registry.init();
 		registry.setNatureIndex(p.getProperty("natureIndex")); //$NON-NLS-1$
 		XModelObject web = this.object.getModel().getByPath("Web"); //$NON-NLS-1$
 		String servletVersion = web == null ? null : web.getAttributeValue("servlet version"); //$NON-NLS-1$
 		registry.setServletVersion(servletVersion);
 		registry.setApplicationName(this.object.getAttributeValue("application name")); //$NON-NLS-1$
 		page = new AppRegisterWizardPage(registry);
 		String n = p.getProperty("natureIndex"); //$NON-NLS-1$
 		if (n == null) {
 			n = Messages.AppRegisterWizard_GenericProject;
 		} else {
 			n = MessageFormat.format(Messages.AppRegisterWizard_Project, n);
 		}
 		page.setTitle(WizardKeys.toDisplayName(n));
 		addPage(page);
 	}
 
 	public int execute() {
 		Shell shell = ModelUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
 		WizardDialog dialog = new WizardDialog(shell, this);
 		dialog.create();
 		dialog.getShell().setText("" + p.getProperty("title")); //$NON-NLS-1$ //$NON-NLS-2$
		dialog.setTitleImage(ModelUIImages.getImage(ModelUIImages.WIZARD_DEFAULT));
 		return dialog.open();		
 	}
 
 }
