 package org.eclipse.dltk.debug.ui.launchConfigurations;
 
import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.ScriptModelHelper;
 import org.eclipse.dltk.debug.ui.messages.DLTKLaunchConfigurationsMessages;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.internal.ui.DLTKUIStatus;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 import org.eclipse.dltk.ui.DLTKUILanguageManager;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 
 public abstract class ScriptLaunchConfigurationTab extends
 		AbstractLaunchConfigurationTab {
 
 	protected static final String EMPTY_STRING = "";
 
 	private Text fProjText;
 	private Button fProjButton;
 	private WidgetListener fListener = new WidgetListener();
 
 	// protected Text fMainText;
 
 	/**
 	 * A listener which handles widget change events for the controls in this
 	 * tab.
 	 */
 	class WidgetListener implements ModifyListener, SelectionListener {
 		public void modifyText(ModifyEvent e) {
 			setErrorMessage(null);
 			if (e.getSource() == fProjText) {
 				IScriptProject proj = getProject();
 				if (proj != null) {
 					if (!validateProject(proj))
 						setErrorMessage(DLTKLaunchConfigurationsMessages.error_notAValidProject);
 				} else
 					setErrorMessage(DLTKLaunchConfigurationsMessages.error_selectProject);
 			}
 			updateLaunchConfigurationDialog();
 		}
 
 		public void widgetDefaultSelected(SelectionEvent e) {/* do nothing */
 		}
 
 		public void widgetSelected(SelectionEvent e) {
 			Object source = e.getSource();
 			if (source == fProjButton) {
 				handleProjectButtonSelected();
 			} else {
 				updateLaunchConfigurationDialog();
 			}
 		}
 	}
 
 	/*
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	public final void createControl(Composite parent) {
 		Font font = parent.getFont();
 		Composite comp = new Composite(parent, SWT.NONE);
 		setControl(comp);
 
 		GridLayout topLayout = new GridLayout();
 		topLayout.verticalSpacing = 0;
 		comp.setLayout(topLayout);
 		comp.setFont(font);
 
 		createProjectEditor(comp);
 		createVerticalSpacer(comp, 1);
 
 		doCreateControl(comp);
 
 		// PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
 		// IScriptDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);
 	}
 
 	public final void initializeFrom(ILaunchConfiguration config) {
 		updateProjectFromConfig(config);
 		doInitializeForm(config);
 	}
 
 	public final void performApply(ILaunchConfigurationWorkingCopy config) {
 		String project = fProjText.getText().trim();
 		config.setAttribute(
 				ScriptLaunchConfigurationConstants.ATTR_PROJECT_NAME, project);
 
 		doPerformApply(config);
 	}
 
 	protected abstract void doPerformApply(
 			ILaunchConfigurationWorkingCopy config);
 
 	/**
 	 * Performs the sub-class specific configuration tab initialization.
 	 * 
 	 * @param config
 	 *            launch configuration
 	 * 
 	 * @see #initializeFrom(ILaunchConfiguration)
 	 */
 	protected abstract void doInitializeForm(ILaunchConfiguration config);
 
 	/**
 	 * Creates the sub-class specific control.
 	 * 
 	 * <p>
 	 * Sub-classes can widgets directly to the <code>composite</code> object
 	 * that is passed to them.
 	 * </p>
 	 * 
 	 * @param composite
 	 *            control composite
 	 * 
 	 * @see #createControl(Composite)
 	 */
 	protected abstract void doCreateControl(Composite composite);
 
 	/**
 	 * Creates a project editor
 	 * 
 	 * <p>
 	 * Creates a group containing an input text field and 'Browse' button to
 	 * select a project from the workspace.
 	 * </p>
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 */
 	protected void createProjectEditor(Composite parent) {
 		Font font = parent.getFont();
 		Group group = new Group(parent, SWT.NONE);
 		group.setText(DLTKLaunchConfigurationsMessages.mainTab_projectGroup);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		group.setLayoutData(gd);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		group.setLayout(layout);
 		group.setFont(font);
 		fProjText = new Text(group, SWT.SINGLE | SWT.BORDER);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		fProjText.setLayoutData(gd);
 		fProjText.setFont(font);
 		fProjText.addModifyListener(fListener);
 		fProjButton = createPushButton(group,
 				DLTKLaunchConfigurationsMessages.mainTab_projectButton, null);
 		fProjButton.addSelectionListener(fListener);
 	}
 
 	/**
 	 * Return the IScriptProject corresponding to the project name in the
 	 * project name text field, or null if the text does not match a project
 	 * name.
 	 */
 	protected IScriptProject getProject() {
 		String projectName = fProjText.getText().trim();
 		if (projectName.length() < 1) {
 			return null;
 		}// end if
 		return getScriptModel().getScriptProject(projectName);
 	}
 
 	/**
 	 * chooses a project for the type of launch config that it is
 	 * 
 	 * @return
 	 */
 	protected IScriptProject chooseProject() {
 		final ILabelProvider labelProvider = DLTKUILanguageManager
 				.createLabelProvider(getNatureID());
 		final ElementListSelectionDialog dialog = new ElementListSelectionDialog(
 				getShell(), labelProvider);
 		dialog
 				.setTitle(DLTKLaunchConfigurationsMessages.mainTab_chooseProject_title);
 		dialog
 				.setMessage(DLTKLaunchConfigurationsMessages.mainTab_chooseProject_message);
 
 		try {
 			final IScriptProject[] projects = ScriptModelHelper
 					.getOpenedScriptProjects(DLTKCore
 							.create(getWorkspaceRoot()), getNatureID());
 			dialog.setElements(projects);
 		} catch (ModelException e) {
 			DLTKLaunchingPlugin.log(e);
 		}
 
 		final IScriptProject project = getProject();
 		if (project != null) {
 			dialog.setInitialSelections(new Object[] { project });
 		}
 
 		if (dialog.open() == Window.OK) {
 			return (IScriptProject) dialog.getFirstResult();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Convenience method to get access to thescriptmodel.
 	 */
 	protected IScriptModel getScriptModel() {
 		return DLTKCore.create(getWorkspaceRoot());
 	}
 
 	/**
 	 * Convenience method to get the workspace root.
 	 */
 	protected IWorkspaceRoot getWorkspaceRoot() {
 		return ResourcesPlugin.getWorkspace().getRoot();
 	}
 
 	protected abstract boolean validateProject(IScriptProject project);
 
 	protected String getLanguageName() {
 		try {
 			IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 					.getLanguageToolkit(getNatureID());
 			return toolkit.getLanguageName();
 		} catch (CoreException e) {
 
 		}
 
 		return null;
 	}
 
 	protected abstract String getNatureID();
 
 	/**
 	 * Show a dialog that lets the user select a project. This in turn provides
 	 * context for the main type, allowing the user to key a main type name, or
 	 * constraining the search for main types to the specified project.
 	 */
 	protected void handleProjectButtonSelected() {
 		IScriptProject project = chooseProject();
 		if (project == null)
 			return;
 		if (!validateProject(project)) {
 			String msg = Messages
 					.format(
 							DLTKLaunchConfigurationsMessages.mainTab_errorDlg_notALangProject,
 							new String[] { getLanguageName() });
 			String reason = Messages
 					.format(
 							DLTKLaunchConfigurationsMessages.mainTab_errorDlg_reasonNotALangProject,
 							new String[] { getLanguageName() });
 			ErrorDialog
 					.openError(
 							getShell(),
 							DLTKLaunchConfigurationsMessages.mainTab_errorDlg_invalidProject,
 							msg, DLTKUIStatus.createError(IStatus.ERROR,
 									reason, null));
 			return;
 		}
 		String projectName = project.getElementName();
 		fProjText.setText(projectName);
 	}
 
 	/**
 	 * Attemps to guess the current project and script being launched.
 	 * 
 	 * <p>
 	 * If the project and script are able to be determined, the string array
 	 * returned will contain the project name in position 0 and the script name
 	 * in position 1.
 	 * </p>
 	 * 
 	 * @return project name and script name as string array, or
 	 *         <code>null</code> if they could not be determined.
 	 */
 	protected String[] getProjectAndScriptNames() {
 		IWorkbenchPage page = DLTKUIPlugin.getActivePage();
 		if (page == null) {
 			return null;
 		}
 
 		IEditorPart editor = page.getActiveEditor();
 		if (editor == null) {
 			return null;
 		}
 
 		IEditorInput editorInput = editor.getEditorInput();
 		if (editorInput == null) {
 			return null;
 		}
 
 		IModelElement me = DLTKUIPlugin.getEditorInputModelElement(editorInput);
 		IScriptProject project = me.getScriptProject();
 		if (project != null && validateProject(project)) {
 			String projectName = project.getProject().getName();
 
 			/*
 			 * TODO: validate script is an executable and not library/module
 			 * otherwise, return null and make user select
 			 */
			IResource resource = me.getResource();
			if (resource != null) {
				String scriptName = resource.getProjectRelativePath()
						.toPortableString();
				// me.getResource().getLocation().toPortableString();
				// /*me.getResource().getFullPath().toPortableString();*/
	
				return new String[] { projectName, scriptName };
			}
 		}
 		return null;
 	}
 
 	/**
 	 * Sets the name of the project associated with the launch configuration
 	 * 
 	 * @param name
 	 *            project name
 	 */
 	protected final void setProjectName(String name) {
 		fProjText.setText(name);
 	}
 
 	protected final String getProjectName() {
 		return fProjText.getText().trim();
 	}
 
 	/**
 	 * updates the project text field form the configuration
 	 * 
 	 * @param config
 	 *            the configuration we are editing
 	 */
 	protected void updateProjectFromConfig(ILaunchConfiguration config) {
 		String projectName = EMPTY_STRING;
 		try {
 			projectName = config.getAttribute(
 					ScriptLaunchConfigurationConstants.ATTR_PROJECT_NAME,
 					EMPTY_STRING);
 		} catch (CoreException ce) {
 			DLTKLaunchingPlugin.log(ce);
 		}
 
 		if (EMPTY_STRING.equals(projectName)) {
 			projectName = guessProjectName();
 		}
 
 		fProjText.setText(projectName);
 	}
 
 	protected String guessProjectName() {
 		return EMPTY_STRING;
 	}
 
 	protected boolean validateProject() {
 		String projectName = getProjectName();
 		if (projectName.length() == 0) {
 			setErrorMessage(DLTKLaunchConfigurationsMessages.error_selectProject);
 			return false;
 		}
 		IScriptProject proj = getScriptModel().getScriptProject(projectName);
 		if (proj == null || !validateProject(proj)) {
 			setErrorMessage(DLTKLaunchConfigurationsMessages.error_notAValidProject);
 			return false;
 		}
 		return true;
 	}
 
 	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
 	}
 
 	/*
 	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
 	 */
 	public boolean canSave() {
 		return validateProject() && doCanSave();
 	}
 
 	protected abstract boolean doCanSave();
 
 	public boolean isValid(ILaunchConfiguration launchConfig) {
 		// clear the error messages first
 		setMessage(null);
 		setErrorMessage(null);
 
 		// if the configuration can be saved, it can be launched
 		boolean canSave = canSave();
 
 		// TODO: check launch configuration?
 		return canSave;
 	}
 
 }
