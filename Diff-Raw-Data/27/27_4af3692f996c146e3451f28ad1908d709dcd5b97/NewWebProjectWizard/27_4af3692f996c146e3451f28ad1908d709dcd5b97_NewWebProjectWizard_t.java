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
 package org.jboss.tools.jst.web.ui.wizards.project;
 
 import java.lang.reflect.InvocationTargetException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
 import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
 import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
 import org.jboss.tools.common.meta.key.WizardKeys;
 import org.jboss.tools.common.reporting.ProblemReportingHelper;
 import org.jboss.tools.common.model.ui.ModelUIPlugin;
 import org.jboss.tools.jst.web.project.helpers.NewWebProjectContext;
 import org.jboss.tools.jst.web.ui.WebUiPlugin;
 import org.jboss.tools.jst.web.ui.operation.WebNatureOperation;
 
 public abstract class NewWebProjectWizard extends Wizard implements INewWizard, IExecutableExtension {
 	private static final String CANNOT_CREATE_LOCATION = "NewStrutsProjectWizard.CannotCreateLocation"; //$NON-NLS-1$
 
 	protected IWorkbench workbench; 
 	protected IStructuredSelection selection;
 	protected IConfigurationElement fConfigElement;
 	protected NewWebProjectContext context;
 	
 	public NewWebProjectWizard() {
 		setNeedsProgressMonitor(true);
 		setHelpAvailable(false);
 	}
 
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.workbench = workbench;
 		this.selection = selection;
 	}
 	
 	public boolean canFinish() {
 		return super.canFinish() && context.getRegisterServerContext().getErrorMessage() == null;
 	}
 	
 	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data)	{
 		fConfigElement= cfig;
 	}
 	
 	protected abstract IRunnableWithProgress createOperation();
 
 	public boolean performFinish() {
 		boolean result = true;
 		IRunnableWithProgress runnable = createOperation();
 		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
 		try {
			getContainer().run(true, false, op);
 			BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
 			BasicNewResourceWizard.selectAndReveal(context.getProject(), ModelUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow());
 			if(runnable instanceof WebNatureOperation && ((WebNatureOperation)runnable).isCancelled()) {
 				result = false;
 			}
 		} catch (InvocationTargetException e) {
 			WebUiPlugin.getPluginLog().logError(e);
 			String message = WizardKeys.getString(CANNOT_CREATE_LOCATION + ".ErrorMessage") + ": " + context.getLocationPath(); //$NON-NLS-1$ //$NON-NLS-2$
 			Status status = new Status(IStatus.ERROR, "org.jboss.tools.jst.web.ui", 0, message, e); //$NON-NLS-1$
 			ProblemReportingHelper.reportProblem(status);
 			result = false;
 		} catch (InterruptedException e) {
 			WebUiPlugin.getPluginLog().logError(e);
 			String message = WizardKeys.getString(CANNOT_CREATE_LOCATION+".ErrorMessage")+": "+context.getLocationPath(); //$NON-NLS-1$ //$NON-NLS-2$
 			Status status = new Status(IStatus.ERROR, "org.jboss.tools.jst.web.ui", 0, message, e); //$NON-NLS-1$
 			ProblemReportingHelper.reportProblem(status);
 			result = false;
 		} 
 		return result;
 	}
 	
 }
