 /*******************************************************************************
  * Copyright (c) 2003, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Apr 1, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.deploy;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.componentcore.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPluginResourceHandler;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.eclipse.wst.common.internal.emf.utilities.CommandContext;
 import org.eclipse.wst.common.internal.emf.utilities.ICommand;
 import org.eclipse.wst.common.internal.emf.utilities.ICommandContext;
 import org.eclipse.wst.server.core.IRuntime;
 
 /**
  * @author cbridgha
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class J2EEDeployOperation extends AbstractDataModelOperation {
 
 	private Object[] selection;
 	private IStatus multiStatus;
 	private IProject currentProject;
 
 	/**
 	 *  
 	 */
 	public J2EEDeployOperation(Object[] deployableObjects) {
 		super();
 		selection = deployableObjects;
 		// TODO Auto-generated constructor stub
 	}
 
 	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.operation.WTPOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		DeployerRegistry reg = DeployerRegistry.instance();
 		List modules = getSelectedModules(selection);
 		monitor.beginTask(J2EEPluginResourceHandler.J2EEDeployOperation_UI_0, modules.size()); 
 		for (int i = 0; i < modules.size(); i++) {
 			EnterpriseArtifactEdit edit = null;
 			try {
 				edit = (EnterpriseArtifactEdit) modules.get(i);
 				EObject module = edit.getDeploymentDescriptorRoot();
 				IProject proj = ProjectUtilities.getProject(module);
 				IRuntime runtime = null;
 				try {
 					runtime = J2EEProjectUtilities.getServerRuntime(proj);
 				}
 				catch (CoreException e) {
 					J2EEPlugin.getDefault().getLog().log(e.getStatus());
 				}
 				if (runtime == null)
 					continue;
 				List visitors = reg.getDeployModuleExtensions(module, runtime);
 				deploy(visitors, module, monitor);
 				monitor.worked(1);
 			} finally {
 				if (edit != null)
 					edit.dispose();
 			}
 		}
 		return getMultiStatus();
 	}
 
 	/**
 	 * @param visitors
 	 * @param module
 	 */
 	private void deploy(List visitors, EObject module, IProgressMonitor monitor) {
 		IProject proj = ProjectUtilities.getProject(module);
 		for (int i = 0; i < visitors.size(); i++) {
 			if (!(visitors.get(i) instanceof IConfigurationElement))
 				continue;
 			ICommand dep = null;
 			try {
 				dep = (ICommand) ((IConfigurationElement) visitors.get(i)).createExecutableExtension(DeployerRegistryReader.DEPLOYER_CLASS);
 			} catch (Exception e) {
 				e.printStackTrace();
 				continue;
 			}
 			ICommandContext ctx = new CommandContext(monitor, null, module.eResource().getResourceSet());
 			if (dep == null) continue;
 			dep.init(selection);
 
 			monitor.setTaskName(J2EEPluginResourceHandler.getString(J2EEPluginResourceHandler.J2EEDeployOperation_1_UI_, new Object[]{proj.getName(), dep.getClass().getName()})); 
 			try {
 				dep.execute(proj, null, ctx);
 				addOKStatus(dep.getClass().getName());
 			} catch (CoreException ex) {
 				Logger.getLogger().logError(ex);
 				Throwable statusException = (ex.getStatus().getException() != null) ? ex.getStatus().getException() : ex;
 				addErrorStatus(ex.getStatus(), dep.getClass().getName(), statusException);
 				continue;
 			}
 		}
 	}
 
 	/**
 	 * @param proj
 	 * @param name
 	 */
 	private void addOKStatus(String DeployerName) {
 
 		IStatus statusLocal = new Status(IStatus.OK, " ", IStatus.OK, (J2EEPluginResourceHandler.getString("J2EEDeployOperation_2_UI_", new Object[]{DeployerName})), null); //$NON-NLS-1$ //$NON-NLS-2$		
 		//TODO
 		getMultiStatus().add(statusLocal);
 
 	}
 
 	/**
 	 * @param exceptionStatus
 	 * @param proj
 	 * @param name
 	 */
 	private void addErrorStatus(IStatus exceptionStatus, String DeployerName, Throwable ex) {
 
 		Throwable mainCause = null;
 		if (exceptionStatus instanceof MultiStatus) {
 			IStatus[] stati = ((MultiStatus) exceptionStatus).getChildren();
 			for (int i = 0; 1 < stati.length; i++) {
 				addErrorStatus(stati[i], DeployerName, stati[i].getException());
 			}
 		}
 		mainCause = (ex.getCause() != null) ? ex.getCause() : ex;
 			
 		//String errorNotes = (mainCause != null && mainCause.getMessage() != null) ? mainCause.getMessage() : "";
 
 		String message = J2EEPluginResourceHandler.bind(J2EEPluginResourceHandler.J2EEDeployOperation_3_UI_,DeployerName, ""); //$NON-NLS-1$
 		IStatus statusLocal = new Status(IStatus.ERROR, J2EEPlugin.getPlugin().getPluginID(), IStatus.ERROR, message, mainCause); //$NON-NLS-1$
 		getMultiStatus().add(statusLocal);
 
 
 
 	}
 
 	private IStatus getMainStatus(IProject proj) {
 
 		IStatus aStatus = new MultiStatus(J2EEPlugin.getPlugin().getPluginID(), IStatus.OK, J2EEPluginResourceHandler.getString(J2EEPluginResourceHandler.J2EEDeployOperation_4_UI_, new Object[]{proj.getName()}), null); 
 
 		return aStatus;
 	}
 
 	/**
 	 * @return Returns the multiStatus.
 	 */
 	public MultiStatus getMultiStatus() {
 		if (multiStatus == null)
 			multiStatus = getMainStatus(currentProject);
 		return (MultiStatus)multiStatus;
 	}
 
 	/**
 	 * @param multiStatus
 	 *            The multiStatus to set.
 	 */
 	public void setMultiStatus(IStatus newStatus) {
 		this.multiStatus = newStatus;
 	}
 	
 	protected List getSelectedModules(Object[] mySelections) {
 		List modules = new ArrayList();
 		List components = new ArrayList();
 		for (int i = 0; i < mySelections.length; i++) {
 			Object object = mySelections[i];
 			if (object instanceof EObject) {
 				object = ProjectUtilities.getProject(object);
 				currentProject = (IProject)object;
 			}
 			if (object instanceof IProject) {
 				currentProject = (IProject)object;
 				IVirtualComponent component = ComponentCore.createComponent((IProject)object);
 				EnterpriseArtifactEdit edit = null;
 				edit = (EnterpriseArtifactEdit)ComponentUtilities.getArtifactEditForRead(component);
 				if (components.contains(edit)) {
 					if (edit != null)
 						edit.dispose();
 					continue;
 				}
 				// Order Ears first...
 				if (J2EEProjectUtilities.isEARProject(component.getProject())) {
 					modules.add(0, edit);
 					components.add(0,component);
 				}
 				else {
 					modules.add(edit);
 					components.add(component);
 				}
 			}
 		}
 		return modules;
 	}
 }
