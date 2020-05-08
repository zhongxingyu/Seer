 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.wizards;
 
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IScriptProjectFilenames;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.internal.ui.util.CoreUtility;
 import org.eclipse.dltk.internal.ui.wizards.BuildpathDetector;
 import org.eclipse.dltk.internal.ui.wizards.NewWizardMessages;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.dltk.launching.ScriptRuntime.DefaultInterpreterEntry;
 import org.eclipse.dltk.ui.PreferenceConstants;
 import org.eclipse.dltk.ui.util.ExceptionHandler;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
 
 /**
  * As addition to the DLTKCapabilityConfigurationPage, the wizard does an early
  * project creation (so that linked folders can be defined) and, if an existing
  * external location was specified, offers to do a buildpath detection
  */
 public abstract class ProjectWizardSecondPage extends
 		CapabilityConfigurationPage implements IProjectWizardLastPage {
 
 	private final ProjectWizardFirstPage fFirstPage;
 
 	private URI fCurrProjectLocation; // null if location is platform location
 	private IProject fCurrProject;
 
 	private boolean fKeepContent;
 
 	private ProjectMetadataBackup projectFileBackup = null;
 	private Boolean fIsAutobuild;
 
 	/**
 	 * Constructor for ScriptProjectWizardSecondPage.
 	 */
 	public ProjectWizardSecondPage(ProjectWizardFirstPage mainPage) {
 		fFirstPage = mainPage;
 		fCurrProjectLocation = null;
 		fCurrProject = null;
 		fKeepContent = false;
 
 		fIsAutobuild = null;
 	}
 
 	public ProjectWizardFirstPage getFirstPage() {
 		return fFirstPage;
 	}
 
 	protected boolean useNewSourcePage() {
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
 	 */
 	@Override
 	public void setVisible(boolean visible) {
 		if (visible) {
 			changeToNewProject();
 		} else {
 			removeProject();
 		}
 		super.setVisible(visible);
 	}
 
 	protected void changeToNewProject() {
 		fKeepContent = fFirstPage.getDetect();
 
 		final IRunnableWithProgress op = new IRunnableWithProgress() {
 			public void run(IProgressMonitor monitor)
 					throws InvocationTargetException, InterruptedException {
 				try {
 					if (fIsAutobuild == null) {
 						fIsAutobuild = Boolean.valueOf(CoreUtility
 								.enableAutoBuild(false));
 					}
 					updateProject(monitor);
 				} catch (CoreException e) {
 					throw new InvocationTargetException(e);
 				} catch (OperationCanceledException e) {
 					throw new InterruptedException();
 				} finally {
 					monitor.done();
 				}
 			}
 		};
 
 		try {
 			getContainer().run(true, false,
 					new WorkspaceModifyDelegatingOperation(op));
 		} catch (InvocationTargetException e) {
 			final String title = NewWizardMessages.ScriptProjectWizardSecondPage_error_title;
 			final String message = NewWizardMessages.ScriptProjectWizardSecondPage_error_message;
 			ExceptionHandler.handle(e, getShell(), title, message);
 		} catch (InterruptedException e) {
 			// cancel pressed
 		}
 	}
 
 	final void updateProject(IProgressMonitor monitor) throws CoreException,
 			InterruptedException {
 
 		fCurrProject = fFirstPage.getProjectHandle();
 		fCurrProjectLocation = getProjectLocationURI();
 
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 		try {
 			monitor
 					.beginTask(
 							NewWizardMessages.ScriptProjectWizardSecondPage_operation_initialize,
 							70);
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 
 			URI realLocation = fCurrProjectLocation;
 			if (realLocation == null) { // inside workspace
 				try {
 					URI rootLocation = ResourcesPlugin.getWorkspace().getRoot()
 							.getLocationURI();
 					/*
 					 * Path.fromPortableString() is required here, because it
 					 * handles path in the way expected by URI constructor. (On
 					 * windows the path keeps the leading slash, e.g.
 					 * "/C:/Users/alex/...")
 					 */
 					realLocation = new URI(rootLocation.getScheme(), null, Path
 							.fromPortableString(rootLocation.getPath()).append(
 									fCurrProject.getName()).toString(), null);
 				} catch (URISyntaxException e) {
 					Assert.isTrue(false, "Can't happen"); //$NON-NLS-1$
 				}
 			}
 
 			rememberExistingFiles(realLocation);
 
 			createProject(fCurrProject, fCurrProjectLocation,
 					new SubProgressMonitor(monitor, 20));
 
 			final IBuildpathEntry[] entries;
 
 			if (fFirstPage.getDetect()) {
 				if (!fCurrProject.getFile(
 						IScriptProjectFilenames.BUILDPATH_FILENAME).exists()) {
 					final IBuildpathDetector detector = createBuildpathDetector();
 					detector
 							.detectBuildpath(new SubProgressMonitor(monitor, 20));
 					entries = detector.getBuildpath();
 				} else {
 					monitor.worked(20);
 					entries = null;
 				}
 			} else if (fFirstPage.isSrc()) {
 				IPreferenceStore store = getPreferenceStore();
 				IPath srcPath = new Path(store
 						.getString(PreferenceConstants.SRC_SRCNAME));
 
 				if (srcPath.segmentCount() > 0) {
 					IFolder folder = fCurrProject.getFolder(srcPath);
 					CoreUtility.createFolder(folder, true, true,
 							new SubProgressMonitor(monitor, 10));
 				} else {
 					monitor.worked(10);
 				}
 
 				// if (srcPath.segmentCount() > 0) {
 				// IFolder folder = fCurrProject.getFolder(srcPath);
 				// CoreUtility.createFolder(folder, true, true,
 				// new SubProgressMonitor(monitor, 10));
 				// } else {
 				// monitor.worked(10);
 				// }
 
 				final IPath projectPath = fCurrProject.getFullPath();
 
 				// configure the buildpath entries, including the default
 				// InterpreterEnvironment library.
 				List<IBuildpathEntry> cpEntries = new ArrayList<IBuildpathEntry>();
 				cpEntries.add(DLTKCore.newSourceEntry(projectPath
 						.append(srcPath)));
 				cpEntries.addAll(ProjectWizardUtils
 						.getDefaultBuildpathEntry(fFirstPage));
 				entries = cpEntries.toArray(new IBuildpathEntry[cpEntries
 						.size()]);
 
 			} else {
 				IPath projectPath = fCurrProject.getFullPath();
 				List<IBuildpathEntry> cpEntries = new ArrayList<IBuildpathEntry>();
 				cpEntries.add(DLTKCore.newSourceEntry(projectPath));
 				cpEntries.addAll(ProjectWizardUtils
 						.getDefaultBuildpathEntry(fFirstPage));
 				entries = cpEntries.toArray(new IBuildpathEntry[cpEntries
 						.size()]);
 
 				monitor.worked(20);
 			}
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 
 			init(DLTKCore.create(fCurrProject), entries, false);
 			configureScriptProject(new SubProgressMonitor(monitor, 30)); // create
 			/*
 			 * the script project to allow the use of the new source folder page
 			 */
 		} finally {
 			monitor.done();
 		}
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	protected IBuildpathDetector createBuildpathDetector() {
 		return new BuildpathDetector(fCurrProject, getLanguageToolkit());
 	}
 
	/**
	 * @since 2.0
	 */
 	@Deprecated
 	protected void createBuildpathDetector(IProgressMonitor monitor,
 			IDLTKLanguageToolkit toolkit) {
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	protected IDLTKLanguageToolkit getLanguageToolkit() {
 		return DLTKLanguageManager.getLanguageToolkit(getScriptNature());
 	}
 
 	@Override
 	protected final String getScriptNature() {
 		return fFirstPage.getScriptNature();
 	}
 
 	protected abstract IPreferenceStore getPreferenceStore();
 
 	private URI getProjectLocationURI() throws CoreException {
 		if (fFirstPage.isInWorkspace()) {
 			return null;
 		}
 		return fFirstPage.getLocationURI();
 	}
 
 	private void rememberExistingFiles(URI projectLocation)
 			throws CoreException {
 		projectFileBackup = new ProjectMetadataBackup();
 		projectFileBackup.backup(projectLocation, new String[] {
 				IScriptProjectFilenames.PROJECT_FILENAME,
 				IScriptProjectFilenames.BUILDPATH_FILENAME });
 	}
 
 	private void restoreExistingFiles(URI projectLocation,
 			IProgressMonitor monitor) throws CoreException {
 		if (projectFileBackup != null) {
 			projectFileBackup.restore(projectLocation, monitor);
 		}
 	}
 
 	/**
 	 * Called from the wizard on finish.
 	 */
 	public void performFinish(IProgressMonitor monitor) throws CoreException,
 			InterruptedException {
 		try {
 			monitor
 					.beginTask(
 							NewWizardMessages.ScriptProjectWizardSecondPage_operation_create,
 							4);
 			if (fCurrProject == null) {
 				updateProject(new SubProgressMonitor(monitor, 1));
 			}
 			configureScriptProject(new SubProgressMonitor(monitor, 2));
 
 			if (!fKeepContent) {
 				if (DLTKCore.DEBUG) {
 					System.err
 							.println("Add compiler compilance options here..."); //$NON-NLS-1$
 				}
 				// String compliance= fFirstPage.getCompilerCompliance();
 				// if (compliance != null) {
 				// IScriptProject project= DLTKCore.create(fCurrProject);
 				// Map options= project.getOptions(false);
 				// ModelUtil.setCompilanceOptions(options, compliance);
 				// project.setOptions(options);
 				// }
 			}
 
 			// Not rebuild project external libraries if exist project with same
 			// interpreter.
 			configureEnvironment(monitor);
 			postConfigureProject(new SubProgressMonitor(monitor, 1));
 		} finally {
 			monitor.done();
 			fCurrProject = null;
 			if (fIsAutobuild != null) {
 				CoreUtility.enableAutoBuild(fIsAutobuild.booleanValue());
 				fIsAutobuild = null;
 			}
 		}
 	}
 
 	protected void configureEnvironment(IProgressMonitor monitor)
 			throws CoreException {
 		IInterpreterInstall projectInterpreter = this.fFirstPage
 				.getInterpreter();
 		if (projectInterpreter == null) {
 			final String nature = getScriptNature();
 			if (nature != null) {
 				projectInterpreter = ScriptRuntime
 						.getDefaultInterpreterInstall(new DefaultInterpreterEntry(
 								nature, fFirstPage.getInterpreterEnvironment()
 										.getId()));
 			}
 		}
 		if (projectInterpreter != null) {
 			final IEnvironment interpreterEnv = projectInterpreter
 					.getEnvironment();
 			if (!fFirstPage.getEnvironment().equals(interpreterEnv)) {
 				EnvironmentManager.setEnvironmentId(fCurrProject,
 						interpreterEnv.getId(), false);
 			} else {
 				EnvironmentManager.setEnvironmentId(fCurrProject, null, false);
 			}
 			// Locate projects with same interpreter.
 			ProjectWizardUtils.reuseInterpreterLibraries(fCurrProject,
 					projectInterpreter, monitor);
 		} else {
 			EnvironmentManager.setEnvironmentId(fCurrProject, null, false);
 		}
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	protected void postConfigureProject(IProgressMonitor monitor)
 			throws CoreException, InterruptedException {
 		// empty override in descendants
 		monitor.done();
 	}
 
 	protected void removeProject() {
 		if (fCurrProject == null || !fCurrProject.exists()) {
 			return;
 		}
 
 		IRunnableWithProgress op = new IRunnableWithProgress() {
 			public void run(IProgressMonitor monitor)
 					throws InvocationTargetException, InterruptedException {
 				doRemoveProject(monitor);
 			}
 		};
 
 		try {
 			getContainer().run(true, true,
 					new WorkspaceModifyDelegatingOperation(op));
 		} catch (InvocationTargetException e) {
 			final String title = NewWizardMessages.ScriptProjectWizardSecondPage_error_remove_title;
 			final String message = NewWizardMessages.ScriptProjectWizardSecondPage_error_remove_message;
 			ExceptionHandler.handle(e, getShell(), title, message);
 		} catch (InterruptedException e) {
 			// cancel pressed
 		}
 	}
 
 	final void doRemoveProject(IProgressMonitor monitor)
 			throws InvocationTargetException {
 		final boolean noProgressMonitor = (fCurrProjectLocation == null); // inside
 		// workspace
 		if (monitor == null || noProgressMonitor) {
 			monitor = new NullProgressMonitor();
 		}
 		monitor
 				.beginTask(
 						NewWizardMessages.ScriptProjectWizardSecondPage_operation_remove,
 						3);
 		try {
 			try {
 				URI projLoc = fCurrProject.getLocationURI();
 
 				boolean removeContent = !fKeepContent
 						&& fCurrProject
 								.isSynchronized(IResource.DEPTH_INFINITE);
 				fCurrProject.delete(removeContent, false,
 						new SubProgressMonitor(monitor, 2));
 
 				restoreExistingFiles(projLoc,
 						new SubProgressMonitor(monitor, 1));
 			} finally {
 				CoreUtility.enableAutoBuild(fIsAutobuild.booleanValue()); // fIsAutobuild
 				// must
 				// be
 				// set
 				fIsAutobuild = null;
 			}
 		} catch (CoreException e) {
 			throw new InvocationTargetException(e);
 		} finally {
 			monitor.done();
 			fCurrProject = null;
 			fKeepContent = false;
 		}
 	}
 
 	/**
 	 * Called from the wizard on cancel.
 	 */
 	public void performCancel() {
 		removeProject();
 	}
 
 	public IProject getCurrProject() {
 		return fCurrProject;
 	}
 }
