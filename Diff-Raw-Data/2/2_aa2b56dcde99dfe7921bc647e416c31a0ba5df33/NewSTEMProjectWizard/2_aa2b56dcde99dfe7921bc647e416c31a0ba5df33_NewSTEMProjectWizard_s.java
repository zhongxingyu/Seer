 // ProjectWizard.java
 package org.eclipse.stem.ui.wizards;
 
 /*******************************************************************************
  * Copyright (c) 2006,2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.stem.core.Constants;
 import org.eclipse.stem.core.experiment.Experiment;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.core.model.Model;
 import org.eclipse.stem.core.modifier.Modifier;
 import org.eclipse.stem.core.predicate.Predicate;
 import org.eclipse.stem.core.scenario.Scenario;
 import org.eclipse.stem.core.sequencer.Sequencer;
 import org.eclipse.stem.core.trigger.Trigger;
 import org.eclipse.stem.ui.Activator;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 /**
  * This class is an Eclipse {@link Wizard} for creating new STEM projects.
  */
 public class NewSTEMProjectWizard extends Wizard implements INewWizard,
 		IExecutableExtension {
 
 	/**
 	 * This is the identifier of the {@link Wizard} that creates a STEM project.
 	 * * {@value}
 	 */
 	public static final String ID_STEM_PROJECT_WIZARD = Constants.ID_ROOT
 			+ ".ui.wizards.newstemproject"; //$NON-NLS-1$
 
 	/**
 	 * The name of the folder used to serialize {@link Graph} in a project.
 	 */
 	public static final String GRAPHS_FOLDER_NAME = "graphs";
 
 	/**
 	 * The name of the folder used to serialize Recorded Simulations in a project.
 	 */
	public static final String RECORDED_SIMULATIONS_FOLDER_NAME = "recordedsimulations";
 
 	
 	/**
 	 * The name of the folder used to serialize {@link Model}s in a project.
 	 */
 	public static final String MODELS_FOLDER_NAME = "models";
 
 	/**
 	 * The name of the folder used to serialize {@link Scenario}s in a project.
 	 */
 	public static final String SCEANARIOS_FOLDER_NAME = "scenarios";
 
 	/**
 	 * The name of the folder used to serialize {@link Decorator}s in a project.
 	 */
 	public static final String DECORATORS_FOLDER_NAME = "decorators";
 
 	/**
 	 * The name of the folder used to serialize {@link Sequencer}s in a project.
 	 */
 	public static final String SEQUENCERS_FOLDER_NAME = "sequencers";
 
 	/**
 	 * The name of the folder used to serialize {@link Experiment}s in a
 	 * project.
 	 */
 	public static final String EXPERIMENTS_FOLDER_NAME = "experiments";
 
 	/**
 	 * The name of the folder used to serialize {@link Modifier}s in a project.
 	 */
 	public static final String MODIFIERS_FOLDER_NAME = "modifiers";
 
 	/**
 	 * The name of the folder used to serialize {@link Trigger}s in a project.
 	 */
 	public static final String TRIGGERS_FOLDER_NAME = "triggers";
 
 	/**
 	 * The name of the folder used to serialize {@link Predicate}s in a project.
 	 */
 	public static final String PREDICATES_FOLDER_NAME = "predicates";
 
 	/**
 	 * This is the Wizard page presented to the user to get the information
 	 * necessary to create the new project.
 	 */
 	WizardNewProjectCreationPage newProjectPage = null;
 
 	/**
 	 * The names of the folders in each STEM project by default.
 	 */
 	private final String[] defaultFolders = { GRAPHS_FOLDER_NAME,
 			MODELS_FOLDER_NAME, SCEANARIOS_FOLDER_NAME, DECORATORS_FOLDER_NAME,
 			SEQUENCERS_FOLDER_NAME, EXPERIMENTS_FOLDER_NAME,
 			MODIFIERS_FOLDER_NAME, TRIGGERS_FOLDER_NAME, PREDICATES_FOLDER_NAME, RECORDED_SIMULATIONS_FOLDER_NAME};
 
 	/**
 	 * @inheritDoc
 	 */
 	public void init(@SuppressWarnings("unused") final IWorkbench workbench,
 			@SuppressWarnings("unused") final IStructuredSelection selection) {
 		setNeedsProgressMonitor(true);
 	} // init
 
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public void addPages() {
 		super.addPages();
 		setWindowTitle(Messages.getString("NSTEMProWiz.title")); //$NON-NLS-1$
 		newProjectPage = new WizardNewProjectCreationPage(Messages
 				.getString("NSTEMProWiz.title")); //$NON-NLS-1$
 		newProjectPage.setTitle(Messages.getString("NSTEMProWiz.page_title")); //$NON-NLS-1$
 		newProjectPage.setDescription(Messages
 				.getString("NSTEMProWiz.page_description")); //$NON-NLS-1$
 		// TODO set icon for new STEM project wizard
 		// newProjectPage.setImageDescriptor(image);
 		addPage(newProjectPage);
 	} // addPages
 
 	/**
 	 * @inheritDoc
 	 */
 	@Override
 	public boolean performFinish() {
 		final boolean retValue = true;
 		try {
 			final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
 				@Override
 				protected void execute(final IProgressMonitor monitor) {
 					createProject(monitor == null ? new NullProgressMonitor()
 							: monitor);
 				} // execute
 			}; // WorkspaceModifyOperation
 
 			// The container is a IRunnableContext, we use it to run the
 			// potentially long operation of creating the project.
 			// The first argument of false indicates not to "fork", the second
 			// of "true" indicates that the operation is cancelable.
 			getContainer().run(false, true, op);
 		} // try
 		catch (final InvocationTargetException e) {
 			Activator.logError(ID_STEM_PROJECT_WIZARD, e);
 		} catch (final InterruptedException e) {
 			// This gets thrown if the operation is canceled
 			Activator.logInformation(ID_STEM_PROJECT_WIZARD, e);
 		}
 		return retValue;
 	} // performFinish
 
 	/**
 	 * Create the project.
 	 * 
 	 * @param monitor
 	 *            a progress monitor to report the progress in creating the
 	 *            project
 	 */
 	void createProject(IProgressMonitor monitor) {
 		// Was a progress monitor specified?
 		if (monitor == null) {
 			// No
 			monitor = new NullProgressMonitor();
 		}
 
 		monitor
 				.beginTask(
 						Messages.getString("NSTEMProWiz.title"), 1 + defaultFolders.length); //$NON-NLS-1$
 
 		try {
 			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
 					.getRoot();
 
 			monitor.subTask(Messages.getString("NSTEMProWiz.creating_project")); //$NON-NLS-1$
 
 			final IProject project = root.getProject(newProjectPage
 					.getProjectName());
 			final IProjectDescription projectDescription = ResourcesPlugin
 					.getWorkspace().newProjectDescription(project.getName());
 
 			// Did the user change the location of the project?
 			if (!Platform.getLocation()
 					.equals(newProjectPage.getLocationPath())) {
 				// Yes
 				// ...new location then
 				projectDescription
 						.setLocation(newProjectPage.getLocationPath());
 			} // if new locations
 
 			// This project has a "STEM" nature
 			projectDescription
 					.setNatureIds(new String[] { Constants.ID_STEM_PROJECT_NATURE });
 
 			// Create the project
 			project.create(projectDescription, monitor);
 			monitor.worked(1);
 
 			monitor.subTask(Messages
 					.getString("NSTEMProWiz.creating_directories")); //$NON-NLS-1$
 
 			project.open(monitor);
 
 			final IPath projectPath = project.getFullPath();
 
 			// Create the default folders
 			for (final String folderName : defaultFolders) {
 				final IPath fullFolderPath = projectPath.append(folderName);
 
 				final IFolder folder = root.getFolder(fullFolderPath);
 				createFolder(folder, monitor);
 
 				monitor.worked(1);
 			} // for each default folder name
 			// monitor.subTask("stem.creating_files");
 
 			// Populate the new project with default files.
 			// final IPath
 		} // try
 		catch (final CoreException ce) {
 			Activator.logError(
 					Messages.getString("NSTEMProWiz.Create_Problem"), ce); //$NON-NLS-1$
 		} finally {
 			monitor.done();
 		}
 	} // createProject
 
 	/**
 	 * Create a folder in the project. All folders along the path will be
 	 * created as necessary.
 	 * 
 	 * @param folder
 	 *            the folder to create
 	 * @param monitor
 	 *            the monitor watching our progress
 	 * @throws CoreException
 	 */
 	private void createFolder(final IFolder folder,
 			final IProgressMonitor monitor) throws CoreException {
 		// Does the folder already exist?
 		if (!folder.exists()) {
 			// No
 			// Ok, get the would be parent of this folder.
 			final IContainer parent = folder.getParent();
 			// Is the parent also a folder?
 			if (parent instanceof IFolder) {
 				// Yes
 				// Does it exist?
 				if (!((IFolder) parent).exists()) {
 					// No
 					// Create it then
 					createFolder((IFolder) parent, monitor);
 				} // if parent doesn't exist
 			} // if parent a folder
 
 			// Now create this folder
 			folder.create(false, true, monitor);
 		} // if the folder doesn't already exist
 
 	} // createFolder
 
 	/**
 	 * @inheritDoc
 	 */
 	@SuppressWarnings("unused")
 	public void setInitializationData(final IConfigurationElement config,
 			final String propertyName, final Object data) throws CoreException {
 		// configElement = config;
 	} // setInitializationData
 
 	/**
 	 * This class is a {@link IHandler} for the command that creates a
 	 * {@link NewSTEMProjectWizard}
 	 */
 	public static class NewSTEMProjectWizardCommandHandler extends
 			AbstractHandler implements IHandler {
 
 		/**
 		 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
 		 */
 		public Object execute(final ExecutionEvent executionEvent)
 				throws ExecutionException {
 			final IWorkbenchWindow window = HandlerUtil
 					.getActiveWorkbenchWindowChecked(executionEvent);
 			final NewSTEMProjectWizard wizard = new NewSTEMProjectWizard();
 			wizard.init(window.getWorkbench(), StructuredSelection.EMPTY);
 			final WizardDialog wizardDialog = new WizardDialog(window
 					.getShell(), wizard);
 			wizardDialog.open();
 			return null;
 		} // execute
 	} // NewSTEMProjectWizardWizardCommandHandler
 
 } // ProjectWizard
