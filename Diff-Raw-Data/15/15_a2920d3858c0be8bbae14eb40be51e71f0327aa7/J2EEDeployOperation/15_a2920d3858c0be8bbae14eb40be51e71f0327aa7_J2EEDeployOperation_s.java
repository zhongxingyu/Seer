 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
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
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
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
 	private List multiStatus = new ArrayList();
 
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
 		return OK_STATUS;
 	}
 
 	/**
 	 * @param visitors
 	 * @param module
 	 */
 	private void deploy(List visitors, EObject module, IProgressMonitor monitor) {
 
 		IProject proj = ProjectUtilities.getProject(module);
 		IStatus main = addMainStatus(proj);
 		for (int i = 0; i < visitors.size(); i++) {
 			if (!(visitors.get(i) instanceof ICommand))
 				continue;
 			ICommand dep = (ICommand) visitors.get(i);
 			ICommandContext ctx = new CommandContext(monitor, null, module.eResource().getResourceSet());
 			dep.init(selection);
 
 			monitor.setTaskName(J2EEPluginResourceHandler.getString(J2EEPluginResourceHandler.J2EEDeployOperation_1_UI_, new Object[]{proj.getName(), dep.getClass().getName()})); 
 			try {
 				dep.execute(proj, null, ctx);
 				addOKStatus(dep.getClass().getName(), main);
 			} catch (CoreException ex) {
 				Logger.getLogger().logError(ex);
 				monitor.setCanceled(true);
 				Throwable statusException = (ex.getStatus().getException() != null) ? ex.getStatus().getException() : ex;
 				addErrorStatus(ex.getStatus(), dep.getClass().getName(), statusException, main);
 				continue;
 			}
 		}
 
 	}
 
 	/**
 	 * @param proj
 	 * @param name
 	 */
 	private void addOKStatus(String DeployerName, IStatus main) {
 
 		//IStatus statusLocal = new Status(IStatus.OK, " ", IStatus.OK, (J2EEPluginResourceHandler.getString("J2EEDeployOperation_2_UI_", new Object[]{DeployerName})), null); //$NON-NLS-1$ //$NON-NLS-2$		
 		//TODO
 		//addStatus(statusLocal);
 
 	}
 
 	/**
 	 * @param exceptionStatus
 	 * @param proj
 	 * @param name
 	 */
 	private void addErrorStatus(IStatus exceptionStatus, String DeployerName, Throwable ex, IStatus main) {
 
 		if (exceptionStatus instanceof MultiStatus) {
 			IStatus[] stati = ((MultiStatus) exceptionStatus).getChildren();
 			for (int i = 0; 1 < stati.length; i++) {
 				addErrorStatus(stati[i], DeployerName, stati[i].getException(), main);
 			}
 		}
 		//String errorNotes = (ex != null && ex.getMessage() != null) ? ex.getMessage() : main.getMessage();
 
 		//String message = J2EEPluginResourceHandler.getString("J2EEDeployOperation_3_UI_", new Object[]{DeployerName, errorNotes}); //$NON-NLS-1$
 		//IStatus statusLocal = new Status(IStatus.ERROR, J2EEPlugin.getPlugin().getPluginID(), IStatus.ERROR, message, ex); //$NON-NLS-1$
 		//TODO
 		//addStatus(statusLocal);
 
 
 
 	}
 
 	private IStatus addMainStatus(IProject proj) {
 
 		IStatus aStatus = new MultiStatus(J2EEPlugin.getPlugin().getPluginID(), IStatus.OK, J2EEPluginResourceHandler.getString(J2EEPluginResourceHandler.J2EEDeployOperation_4_UI_, new Object[]{proj.getName()}), null); 
 
 		getMultiStatus().add(aStatus);
 		return aStatus;
 	}
 
 	/**
 	 * @return Returns the multiStatus.
 	 */
 	public List getMultiStatus() {
 		return multiStatus;
 	}
 
 	/**
 	 * @param multiStatus
 	 *            The multiStatus to set.
 	 */
 	public void setMultiStatus(List multiStatus) {
 		this.multiStatus = multiStatus;
 	}
 	
 	protected List getSelectedModules(Object[] mySelections) {
 		List modules = new ArrayList();
 		for (int i = 0; i < mySelections.length; i++) {
 			Object object = mySelections[i];
 			if (object instanceof EObject) {
 				object = ProjectUtilities.getProject(object);
 			}
 			if (object instanceof IProject) {
 				IVirtualComponent component = ComponentCore.createComponent((IProject)object);
 				EnterpriseArtifactEdit edit = null;
 				edit = (EnterpriseArtifactEdit)ComponentUtilities.getArtifactEditForRead(component);
				if (modules.contains(edit)) {
 					if (edit != null)
 						edit.dispose();
 					continue;
 				}
 				// Order Ears first...
				if (J2EEProjectUtilities.isEARProject(component.getProject()))
 					modules.add(0, edit);
				else
 					modules.add(edit);
 			}
 		}
 		return modules;
 	}
 }
