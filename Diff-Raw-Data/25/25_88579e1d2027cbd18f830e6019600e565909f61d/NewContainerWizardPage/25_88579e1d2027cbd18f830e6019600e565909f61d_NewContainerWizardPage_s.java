 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.wizards;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.ScriptModelUtil;
 import org.eclipse.dltk.internal.core.ExternalScriptFolder;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.internal.ui.StandardModelElementContentProvider;
 import org.eclipse.dltk.internal.ui.wizards.NewWizardMessages;
 import org.eclipse.dltk.internal.ui.wizards.TypedViewerFilter;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.IStringButtonAdapter;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.LayoutUtil;
 import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringButtonDialogField;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.ModelElementLabelProvider;
 import org.eclipse.dltk.ui.ModelElementSorter;
 import org.eclipse.dltk.ui.dialogs.StatusInfo;
 import org.eclipse.dltk.ui.viewsupport.IViewPartInputProvider;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.views.contentoutline.ContentOutline;
 
 /**
  * Wizard page that acts as a base class for wizard pages that create new Script
  * elements. The class provides a input field for source folders (called
  * container in this class) and API to validate the enter source folder name.
  * 
  * <p>
  * Clients may subclass.
  * </p>
  * 
  * 
  */
 public abstract class NewContainerWizardPage extends NewElementWizardPage {
 	/** Id of the container field */
 	protected static final String CONTAINER = "NewContainerWizardPage.container"; //$NON-NLS-1$
 
 	// The status of the last validation
 	protected IStatus containerStatus;
 
 	private StringButtonDialogField containerDialogField;
 
 	// script folder corresponding to the input type (can be null)
 	private IScriptFolder currRoot;
 
 	private IWorkspaceRoot workspaceRoot;
 
 	private class ContainerFieldAdapter implements IStringButtonAdapter,
 			IDialogFieldListener {
 		public void changeControlPressed(DialogField field) {
 			containerChangeControlPressed(field);
 		}
 
 		public void dialogFieldChanged(DialogField field) {
 			containerDialogFieldChanged(field);
 		}
 	}
 
 	/**
 	 * Create a new <code>NewContainerWizardPage</code>
 	 * 
 	 * @param name
 	 *            the wizard page's name
 	 */
 	public NewContainerWizardPage(String name) {
 		super(name);
 		workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		ContainerFieldAdapter adapter = new ContainerFieldAdapter();
 		containerDialogField = new StringButtonDialogField(adapter);
 		containerDialogField.setDialogFieldListener(adapter);
 		containerDialogField.setLabelText(getContainerLabel());
 		containerDialogField
 				.setButtonLabel(NewWizardMessages.NewContainerWizardPage_container_button);
 		containerStatus = new StatusInfo();
 		currRoot = null;
 	}
 
 	/**
 	 * Returns the label that is used for the container input field.
 	 * 
 	 * @return the label that is used for the container input field.
 	 * 
 	 */
 	protected String getContainerLabel() {
 		return NewWizardMessages.NewContainerWizardPage_container_label;
 	}
 
 	/**
 	 * Initializes the source folder field with a valid package fragment root.
 	 * The package fragment root is computed from the given Script element.
 	 * 
 	 * @param elem
 	 *            the Script element used to compute the initial package
 	 *            fragment root used as the source folder
 	 */
 	protected void initContainerPage(IModelElement elem) {
 		IScriptFolder initRoot = null;
 		if (elem != null) {
 			initRoot = (IScriptFolder) elem
 					.getAncestor(IModelElement.SCRIPT_FOLDER);
 			if (initRoot instanceof ExternalScriptFolder)
 				initRoot = null;
 			// TODO: I think this piece of code is a mess, please fix it
 			try {
 				if (initRoot == null) {
 					IProjectFragment fragment = ScriptModelUtil
 							.getProjectFragment(elem);
 					if (fragment != null
 							&& fragment.getKind() == IProjectFragment.K_SOURCE
 							&& !fragment.isExternal())
 						initRoot = fragment.getScriptFolder(""); //$NON-NLS-1$
 
 					if (initRoot == null) {
 						IScriptProject project = elem.getScriptProject();
 						if (project != null) {
 							initRoot = null;
 							if (project.exists()) {
 								IProjectFragment[] roots = project
 										.getProjectFragments();
 								for (int i = 0; i < roots.length; i++) {
 									if (roots[i].getKind() == IProjectFragment.K_SOURCE) {
 										initRoot = roots[i].getScriptFolder(""); //$NON-NLS-1$
 										break;
 									}
 								}
 							}
 							if (initRoot == null) {
 								initRoot = project.getProjectFragment(
 										project.getResource()).getScriptFolder(
 										""); //$NON-NLS-1$
 							}
 						}
 					}
 				}
 			} catch (ModelException e) {
 				DLTKUIPlugin.log(e);
 			}
 		}
 		setScriptFolder(initRoot, true);
 		handleFieldChanged(CONTAINER);
 	}
 
 	/**
 	 * Utility method to inspect a selection to find a Script element.
 	 * 
 	 * @param selection
 	 *            the selection to be inspected
 	 * @return a Script element to be used as the initial selection, or
 	 *         <code>null</code>, if no Script element exists in the given
 	 *         selection
 	 */
 	protected IModelElement getInitialScriptElement(
 			IStructuredSelection selection) {
 		IModelElement scriptElement = null;
 
 		// Check selection
 		if (selection != null && !selection.isEmpty()) {
 			Object selectedElement = selection.getFirstElement();
 			// Check for adapters
 			if (selectedElement instanceof IAdaptable) {
 				IAdaptable adaptable = (IAdaptable) selectedElement;
 				scriptElement = (IModelElement) adaptable
 						.getAdapter(IModelElement.class);
 				if (scriptElement == null) {
 					IResource resource = (IResource) adaptable
 							.getAdapter(IResource.class);
 					if (resource != null
 							&& resource.getType() != IResource.ROOT) {
 						while (scriptElement == null
 								&& resource.getType() != IResource.PROJECT) {
 							resource = resource.getParent();
 							scriptElement = (IModelElement) resource
 									.getAdapter(IModelElement.class);
 						}
 
 						if (scriptElement == null) {
 							scriptElement = DLTKCore.create(resource);
 						}
 					}
 				}
 			}
 		}
 
 		// Check view
 		if (scriptElement == null) {
 			IWorkbenchPart part = DLTKUIPlugin.getActivePage().getActivePart();
 			if (part instanceof ContentOutline) {
 				part = DLTKUIPlugin.getActivePage().getActiveEditor();
 			}
 
 			if (part instanceof IViewPartInputProvider) {
 				Object provider = ((IViewPartInputProvider) part)
 						.getViewPartInput();
 				if (provider instanceof IModelElement) {
 					scriptElement = (IModelElement) provider;
 				}
 			}
 		}
 
 		if (scriptElement == null
 				|| scriptElement.getElementType() == IModelElement.SCRIPT_MODEL) {
 			try {
 				IScriptProject[] projects = DLTKCore.create(getWorkspaceRoot())
 						.getScriptProjects();
 				if (projects.length == 1) {
 					scriptElement = projects[0];
 				}
 			} catch (ModelException e) {
 				DLTKUIPlugin.log(e);
 			}
 		}
 
 		return scriptElement;
 	}
 
 	/**
 	 * Returns the recommended maximum width for text fields (in pixels). This
 	 * method requires that createContent has been called before this method is
 	 * call. Subclasses may override to change the maximum width for text
 	 * fields.
 	 * 
 	 * @return the recommended maximum width for text fields.
 	 */
 	protected int getMaxFieldWidth() {
 		return convertWidthInCharsToPixels(40);
 	}
 
 	/**
 	 * Creates the necessary controls (label, text field and browse button) to
 	 * edit the source folder location. The method expects that the parent
 	 * composite uses a <code>GridLayout</code> as its layout manager and that
 	 * the grid layout has at least 3 columns.
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 * @param nColumns
 	 *            the number of columns to span. This number must be greater or
 	 *            equal three
 	 */
 	protected void createContainerControls(Composite parent, int nColumns) {
 		containerDialogField.doFillIntoGrid(parent, nColumns);
 		LayoutUtil.setWidthHint(containerDialogField.getTextControl(null),
 				getMaxFieldWidth());
 	}
 
 	/**
 	 * Sets the focus to the source folder's text field.
 	 */
 	protected void setFocusOnContainer() {
 		containerDialogField.setFocus();
 	}
 
 	private void containerChangeControlPressed(DialogField field) {
 		IScriptFolder root = chooseContainer();
 		if (root != null) {
 			setScriptFolder(root, true);
 		}
 	}
 
 	private void containerDialogFieldChanged(DialogField field) {
 		if (field == containerDialogField) {
 			containerStatus = containerChanged();
 		}
 		// tell all others
 		handleFieldChanged(CONTAINER);
 	}
 
 	// ----------- validation ----------
 	protected abstract String getRequiredNature();
 
 	/**
 	 * This method is a hook which gets called after the source folder's text
 	 * input field has changed. This default implementation updates the model
 	 * and returns an error status. The underlying model is only valid if the
 	 * returned status is OK.
 	 * 
 	 * @return the model's error status
 	 */
 	protected IStatus containerChanged() {
 		StatusInfo status = new StatusInfo();
 		currRoot = null;
 		String str = getScriptFolderText();
 		if (str.length() == 0) {
 			status
 					.setError(NewWizardMessages.NewContainerWizardPage_error_EnterContainerName);
 			return status;
 		}
 		IPath path = new Path(str);
 		IResource res = workspaceRoot.findMember(path);
 		if (res != null) {
 			int resType = res.getType();
 			if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
 				IProject proj = res.getProject();
 				if (!proj.isOpen()) {
 					status
 							.setError(Messages
 									.format(
 											NewWizardMessages.NewContainerWizardPage_error_ProjectClosed,
 											proj.getFullPath().toString()));
 					return status;
 				}
 
 				IScriptProject jproject = DLTKCore.create(proj);
 				if (resType == IResource.PROJECT)
 					currRoot = jproject.getProjectFragment(res)
 							.getScriptFolder(""); //$NON-NLS-1$
 				else {
					currRoot = jproject.getProjectFragment(res.getProject())
							.getScriptFolder(path.removeFirstSegments(1));
 				}
 
 				if (res.exists()) {
 					try {
 						// if
 						// (!DLTKLanguageManager.hasScriptNature(jproject.getProject()))
 						// {
 						String nature = getRequiredNature();
 						if (nature != null && !proj.hasNature(nature)) {
 							if (resType == IResource.PROJECT) {
 								status
 										.setError(NewWizardMessages.NewContainerWizardPage_warning_NotAScriptProject);
 							} else {
 								status
 										.setWarning(NewWizardMessages.NewContainerWizardPage_warning_NotInAScriptProject);
 							}
 							return status;
 						}
 						// }
 
 					} catch (CoreException e) {
 						status
 								.setWarning(NewWizardMessages.NewContainerWizardPage_warning_NotAScriptProject);
 					}
 				}
 				return status;
 			} else {
 				status
 						.setError(Messages
 								.format(
 										NewWizardMessages.NewContainerWizardPage_error_NotAFolder,
 										str));
 				return status;
 			}
 		} else {
 			status
 					.setError(Messages
 							.format(
 									NewWizardMessages.NewContainerWizardPage_error_ContainerDoesNotExist,
 									str));
 			return status;
 		}
 	}
 
 	// -------- update message ----------------
 	/**
 	 * Hook method that gets called when a field on this page has changed. For
 	 * this page the method gets called when the source folder field changes.
 	 * <p>
 	 * Every sub type is responsible to call this method when a field on its
 	 * page has changed. Subtypes override (extend) the method to add
 	 * verification when a own field has a dependency to an other field. For
 	 * example the class name input must be verified again when the package
 	 * field changes (check for duplicated class names).
 	 * 
 	 * @param fieldName
 	 *            The name of the field that has changed (field id). For the
 	 *            source folder the field id is <code>CONTAINER</code>
 	 */
 	protected void handleFieldChanged(String fieldName) {
 	}
 
 	// ---- get ----------------
 	/**
 	 * Returns the workspace root.
 	 * 
 	 * @return the workspace root
 	 */
 	protected IWorkspaceRoot getWorkspaceRoot() {
 		return workspaceRoot;
 	}
 
 	/**
 	 * Returns the <code>IProjectFragment</code> that corresponds to the current
 	 * value of the source folder field.
 	 * 
 	 * @return the IProjectFragment or <code>null</code> if the current source
 	 *         folder value is not a valid package fragment root
 	 * 
 	 */
 	public IProjectFragment getProjectFragment() {
 		if (currRoot == null)
 			return null;
 		IProjectFragment fragment = (IProjectFragment) currRoot
 				.getAncestor(IModelElement.PROJECT_FRAGMENT);
 		if (fragment != null)
 			return fragment;
 		IScriptProject project = currRoot.getScriptProject();
 		try {
 			if (project.exists()) {
 				IProjectFragment[] roots = project.getProjectFragments();
 				for (int i = 0; i < roots.length; i++) {
 					if (roots[i].getKind() == IProjectFragment.K_SOURCE) {
 						return roots[i];
 					}
 				}
 			}
 		} catch (ModelException e) {
 		}
 		return null;
 	}
 
 	public IScriptFolder getScriptFolder() {
 		return currRoot;
 	}
 
 	/**
 	 * Returns the current text of source folder text field.
 	 * 
 	 * @return the text of the source folder text field
 	 */
 	public String getScriptFolderText() {
 		return containerDialogField.getText();
 	}
 
 	/**
 	 * Sets the current source folder (model and text field) to the given
 	 * package fragment root.
 	 * 
 	 * @param root
 	 *            The new root.
 	 * @param canBeModified
 	 *            if <code>false</code> the source folder field can not be
 	 *            changed by the user. If <code>true</code> the field is
 	 *            editable
 	 */
 	public void setScriptFolder(IScriptFolder root, boolean canBeModified) {
 		currRoot = root;
 		String str = (root == null) ? "" : root.getPath().makeRelative().toString(); //$NON-NLS-1$
 		containerDialogField.setText(str);
 		containerDialogField.setEnabled(canBeModified);
 	}
 
 	// ------------- choose source container dialog
 	/**
 	 * Opens a selection dialog that allows to select a source container.
 	 * 
 	 * @return returns the selected package fragment root or <code>null</code>
 	 *         if the dialog has been canceled. The caller typically sets the
 	 *         result to the container input field.
 	 *         <p>
 	 *         Clients can override this method if they want to offer a
 	 *         different dialog.
 	 *         </p>
 	 * 
 	 * 
 	 */
 	protected IScriptFolder chooseContainer() {
 		IModelElement initElement = getProjectFragment();
 		Class[] acceptedClasses = new Class[] { IScriptModel.class,
 				IScriptFolder.class, IScriptProject.class,
 				IProjectFragment.class };
 
 		ViewerFilter filter = new TypedViewerFilter(acceptedClasses) {
 			public boolean select(Viewer viewer, Object parent, Object element) {
 				if (element instanceof IProjectFragment) {
 					try {
 						IProjectFragment fragment = (IProjectFragment) element;
 						if (fragment.getKind() != IProjectFragment.K_SOURCE
 								|| fragment.isExternal())
 							return false;
 					} catch (ModelException e) {
 						return false;
 					}
 					return true;
 				}
 				return super.select(viewer, parent, element);
 			}
 		};
 
 		StandardModelElementContentProvider provider = new StandardModelElementContentProvider();
 		ILabelProvider labelProvider = new ModelElementLabelProvider(
 				ModelElementLabelProvider.SHOW_DEFAULT);
 		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
 				getShell(), labelProvider, provider);
 
 		dialog.setComparator(new ModelElementSorter());
 		dialog
 				.setTitle(NewWizardMessages.NewContainerWizardPage_ChooseSourceContainerDialog_title);
 		dialog
 				.setMessage(NewWizardMessages.NewContainerWizardPage_ChooseSourceContainerDialog_description);
 		dialog.addFilter(filter);
 		dialog.setInput(DLTKCore.create(workspaceRoot));
 		dialog.setInitialSelection(initElement);
 		dialog.setHelpAvailable(false);
 		if (dialog.open() == Window.OK) {
 			Object element = dialog.getFirstResult();
 			if (element instanceof IScriptProject) {
 				IScriptProject jproject = (IScriptProject) element;
 				return jproject.getProjectFragment(jproject.getResource())
 						.getScriptFolder(""); //$NON-NLS-1$				
 			} else if (element instanceof IScriptFolder) {
 				return (IScriptFolder) element;
 			} else if (element instanceof IProjectFragment) {
 				return ((IProjectFragment) element).getScriptFolder(""); //$NON-NLS-1$
 			}
 			return null;
 		}
 
 		return null;
 	}
 }
