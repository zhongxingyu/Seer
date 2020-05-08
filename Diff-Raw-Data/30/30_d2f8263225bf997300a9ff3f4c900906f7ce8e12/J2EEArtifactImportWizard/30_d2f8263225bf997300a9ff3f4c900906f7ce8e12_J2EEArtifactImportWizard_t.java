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
 package org.eclipse.jst.j2ee.internal.wizard;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jst.j2ee.internal.plugin.CommonEditorUtility;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizard;
 import org.eclipse.wst.web.internal.DelegateConfigurationElement;
 
 /**
  * <p>
  * Serves as a base class for Wizards which import J2EE artifact structures into Eclipse projects.
  * </p>
  * <p>
  * Subclasses must provide the methods that are required by
  * {@see org.eclipse.wst.common.frameworks.internal.ui.WTPWizard}.
  * </p>
  * <p>
  * Optionally, subclasses may also override the following methods:
  * <ul>
  * <li>{@link #getFinalPerspectiveID()}
  * <li>{@link #doInit()()}
  * <li>{@link #doDispose()()}
  * </ul>
  * </p>
  * <p>
  * The base class will ensure that the Wizard is not spawned unless all open editors are in a
  * non-dirty state. Additionally, the selection from the active view which spanwed the wizard will
  * be made available to subclasses via {@link #getCurrentSelection()}.
  * </p>
  */
 public abstract class J2EEArtifactImportWizard extends DataModelWizard implements IImportWizard, IExecutableExtension {
 
 	/**
 	 * <p>
 	 * Constant used to identify the key of the main page of the Wizard.
 	 * </p>
 	 */
 	protected static final String MAIN_PG = "main"; //$NON-NLS-1$
 
 	private IConfigurationElement configurationElement;
 	private IStructuredSelection selection;
 
 	/**
 	 * <p>
 	 * The default constructor. Creates a wizard with no selection, no model instance, and no
 	 * operation instance. The model and operation will be created as needed.
 	 * </p>
 	 */
 	public J2EEArtifactImportWizard() {
 		super();
 	}
 
 	/**
 	 * <p>
 	 * The model is used to prepopulate the wizard controls and interface with the operation.
 	 * </p>
 	 * 
 	 * @param model
 	 *            The model parameter is used to pre-populate wizard controls and interface with the
 	 *            operation
 	 */
 	public J2EEArtifactImportWizard(IDataModel model) {
 		super(model);
 	}
 
 	/**
 	 * <p>
 	 * The selection is used to pre-populate values in the Wizard dialog controls.
 	 * </p>
 	 * 
 	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
 	 *      org.eclipse.jface.viewers.IStructuredSelection)
 	 * 
 	 * @param workbench
 	 *            the current workbench parent of the wizard
 	 * @param aSelection
 	 *            the selection from the view used to start the wizard (if any)
 	 */
 	public final void init(IWorkbench workbench, IStructuredSelection aSelection) {
 		this.selection = aSelection;
 		setNeedsProgressMonitor(true);
 		doInit();
 	}
 
 	/**
 	 * <p>
 	 * Calls {@link #doDispose()}and then nulls out fields that are no longer needed once the
 	 * wizard completes.
 	 * </p>
 	 * 
 	 * @see com.ibm.etools.j2ee.common.wizard.datamodel.WTPWizard#dispose()
 	 */
 	public final void dispose() {
 		super.dispose();
 		doDispose();
 		this.selection = null;
 		this.configurationElement = null;
 	}
 
 	/**
 	 * <p>
 	 * Invoked after the selection has been saved off in
 	 * {@link #init(IWorkbench, IStructuredSelection)}. Should be used to handle any specific
 	 * Wizard initialization.
 	 * </p>
 	 */
 	protected void doInit() {
 		// init
 	}
 
 	/**
 	 * <p>
 	 * Invoked from {@link #dispose()}. Should be used to handle any specific Wizard disposal.
 	 * </p>
 	 */
 	protected void doDispose() {
 		// dispose
 	}
 
 	/**
 	 * <p>
 	 * The return value of this method will be used to suggest a final perspective to the user once
 	 * the wizard completes.
 	 * </p>
 	 * 
 	 * @return Returns the J2EE Perpsective ID by default
 	 */
 	protected String getFinalPerspectiveID() {
 		return null;
 	}
 
 	/**
 	 * <p>
 	 * Prompts the user to save open, dirty editors.
 	 * </p>
 	 * 
 	 * @return true only if all editors are saved
 	 */
 	protected final boolean prePerformFinish() {
 		if (!CommonEditorUtility.promptToSaveAllDirtyEditors()) {
 			return false;
 		}
		//Must have selected to not save, but should close all remaining
		CommonEditorUtility.closeAllEditors();
 		return super.prePerformFinish();
 	}
 
 	/**
 	 * <p>
 	 * Invoked after the user has clicked the "Finish" button of the wizard. The default
 	 * implementation will attempt to update the final perspective to the value specified by
 	 * {@link #getFinalPerspectiveID() }
 	 * </p>
 	 * 
 	 * @throws InvocationTargetException
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizard#postPerformFinish()
 	 */
 	protected final void postPerformFinish() throws InvocationTargetException {
 		super.postPerformFinish();
 		if (getFinalPerspectiveID() != null && getFinalPerspectiveID().length() > 0) {
 			final IConfigurationElement element = new DelegateConfigurationElement(configurationElement) {
 				public String getAttribute(String aName) {
 					if (aName.equals("finalPerspective")) { //$NON-NLS-1$
 						return getFinalPerspectiveID();
 					}
 					return super.getAttribute(aName);
 				}
 			};
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 					BasicNewProjectResourceWizard.updatePerspective(element);
 				}
 			});
 		} else {
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 					BasicNewProjectResourceWizard.updatePerspective(configurationElement);
 				}
 			});
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * <p>
 	 * The configuration element is saved to use when the wizard completes in order to change the
 	 * current perspective using either (1) the value specified by {@see #getFinalPerspectiveID()}
 	 * or (2) the value specified by the finalPerspective attribute in the Wizard's configuration
 	 * element.
 	 * </p>
 	 * 
 	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
 	 *      java.lang.String, java.lang.Object)
 	 */
 	public final void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
 		this.configurationElement = config;
 	}
 
 	/**
 	 * @return Returns the selection.
 	 */
 	protected final IStructuredSelection getSelection() {
 		return selection;
 	}
 
 }
