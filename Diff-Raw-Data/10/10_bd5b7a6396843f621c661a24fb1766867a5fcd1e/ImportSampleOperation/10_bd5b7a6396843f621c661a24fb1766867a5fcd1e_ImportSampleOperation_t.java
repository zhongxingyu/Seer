 package org.eclipsercp.book.tools.actions;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorRegistry;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.eclipse.ui.progress.UIJob;
 
 import org.osgi.service.prefs.Preferences;
 
 import org.eclipsercp.book.tools.BundleLocation;
 import org.eclipsercp.book.tools.IConstants;
 import org.eclipsercp.book.tools.Sample;
 import org.eclipsercp.book.tools.Sample.ProjectImport;
 import org.eclipsercp.book.tools.SamplesModel;
 import org.eclipsercp.book.tools.Utils;
 
 public class ImportSampleOperation implements IRunnableWithProgress {
 
 	private final Sample sample;
 	private final SamplesModel samples;
 	private final Shell shell;
 	private final boolean cleanup;
 
 	public ImportSampleOperation(final Shell shell, final Sample sample, final SamplesModel samples, final boolean cleanup) {
 		this.shell = shell;
 		this.sample = sample;
 		this.samples = samples;
 		this.cleanup = cleanup;
 	}
 
 	protected void cleanupWorkspace() {
 		// delete launch configurations
 		final ILaunchManager mng = DebugPlugin.getDefault().getLaunchManager();
 		try {
 			final ILaunchConfiguration[] launchConfigs = mng.getLaunchConfigurations();
 			for (int i = 0; i < launchConfigs.length; i++) {
 				final ILaunchConfiguration configuration = launchConfigs[i];
 				if (!configuration.isLocal()) {
 					configuration.delete();
 				}
 			}
 		} catch (final CoreException e) {
 			Utils.handleError(shell, e, "Error", "Problems deleting launch configurations");
 		}
 	}
 
 	private boolean deleteProjects(final IProgressMonitor monitor, boolean prompted) throws CoreException {
 		// deletion
 		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		final SubProgressMonitor subDeleteMonitor = new SubProgressMonitor(monitor, 30);
 		subDeleteMonitor.beginTask("Deleting projects", projects.length * 100);
 		for (int i = 0; i < projects.length; i++) {
 			final IProject project = projects[i];
 			if (project.isAccessible() && (project.getPersistentProperty(samples.getTagId()) != null)) {
 				if (!prompted && !promptToOverwrite()) {
 					return false;
 				}
 				prompted = true;
 				project.delete(true, true, new SubProgressMonitor(subDeleteMonitor, 100));
 			}
 		}
 		subDeleteMonitor.done();
 		return true;
 	}
 
 	/**
 	 * Returns whether the given project contains any problem markers of the
 	 * specified severity.
 	 * 
 	 * @param proj
 	 *            the project to search
 	 * @return whether the given project contains any problems that should stop
 	 *         it from launching
 	 * @throws CoreException
 	 *             if an error occurs while searching for problem markers
 	 */
 	protected boolean existsProblems(final IProject proj) throws CoreException {
 		final IMarker[] markers = proj.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
 		if (markers.length > 0) {
 			for (int i = 0; i < markers.length; i++) {
 				if (isLaunchProblem(markers[i])) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Import the project described in record. If it is successful return true.
 	 * 
 	 * @param sample
 	 * @return boolean <code>true</code> of successult
 	 * @throws InterruptedException
 	 * @throws InvocationTargetException
 	 */
 	private boolean importProject(final Sample sample, final BundleLocation record, final boolean updateProperty, final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 		monitor.beginTask("Importing", 2000);
 
 		final String projectName = record.location.lastSegment();
 		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		final IProject project = workspace.getRoot().getProject(projectName);
 		final IProjectDescription description = workspace.newProjectDescription(projectName);
 		final URL instanceLocation = Platform.getInstanceLocation().getURL();
 		final File projectDir = new File(instanceLocation.getPath(), projectName);
 		projectDir.mkdirs();
 		try {
 			Utils.copy(record, projectDir, true, monitor);
 			description.setLocation(null);
 			if (project.exists()) {
 				if (!project.isOpen()) {
 					project.open(null);
 				}
 				project.delete(true, null);
 			}
 			project.create(description, new SubProgressMonitor(monitor, 1000));
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 			project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));
 			if (updateProperty) {
 				project.setPersistentProperty(IConstants.SAMPLE_NUMBER_KEY, sample.getNumber().toString());
 			}
 			project.setPersistentProperty(samples.getTagId(), "samples-manager-project");
 		} catch (final Exception e) {
 			Utils.handleError(shell, e, "Error", "Problem importing projects");
 		}
 
 		return true;
 	}
 
 	protected void importSample(final IProgressMonitor monitor) {
 		try {
 			importSample(sample, monitor);
 		} catch (final CoreException e) {
 			Utils.handleError(shell, e, "Error", "Importing projects");
 		} catch (final InvocationTargetException e) {
 			Utils.handleError(shell, e, "Error", "Importing projects");
 		} catch (final InterruptedException e) {
 			Utils.handleError(shell, e, "Error", "Importing projects");
 		}
 	}
 
 	private void importSample(final Sample sample, final IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
 		final boolean prompted = false;
 		try {
 			monitor.beginTask("Importing", 100);
 
 			if (!deleteProjects(monitor, prompted)) {
 				return;
 			}
 
 			// import
 			final SubProgressMonitor subImportMonitor = new SubProgressMonitor(monitor, 70);
 			subImportMonitor.beginTask("Importing projects", sample.getProjects().size() * 100);
 
 			final List importedProjects = new ArrayList();
 			final IWorkspaceRunnable workspaceOperation = new IWorkspaceRunnable() {
 				private void importPrereq(final ProjectImport prereq, final Sample firstElement, final List importedProjects, final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, CoreException {
 
 					final Sample sample = samples.findSampleById(prereq.getSampleNumber());
 					if (sample == null) {
 						return;
 					}
 					for (final Iterator it = sample.getProjects().iterator(); it.hasNext();) {
 						final BundleLocation project = (BundleLocation) it.next();
 						final IProject current = ResourcesPlugin.getWorkspace().getRoot().getProject(project.location.lastSegment());
 						importedProjects.add(current);
 						if (current.getName().endsWith(prereq.getProjectName())) {
 							if (current.exists() && !current.isOpen()) {
 								current.open(null);
 							} else {
 								importProject(sample, project, false, new SubProgressMonitor(monitor, 100));
 							}
 						}
 					}
 
 				}
 
 				public void run(final IProgressMonitor monitor) throws CoreException {
 					try {
 						// Import all the projects for this sample
 						for (final Iterator it = sample.getProjects().iterator(); it.hasNext();) {
 							final BundleLocation element = (BundleLocation) it.next();
 							importProject(sample, element, true, new SubProgressMonitor(monitor, 100));
 						}
 
 						// import all the prereqs for this sample
 						final Sample.ProjectImport[] imports = sample.getImports();
 						for (int i = 0; i < imports.length; i++) {
 							importPrereq(imports[i], sample, importedProjects, monitor);
 						}
 					} catch (final InvocationTargetException e) {
 						throw new CoreException(Utils.statusFrom(e));
 					} catch (final InterruptedException e) {
 						throw new CoreException(Utils.statusFrom(e));
 					}
 				}
 			};
 			ResourcesPlugin.getWorkspace().run(workspaceOperation, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE, subImportMonitor);
 
 			// HACK - to ensure that the imported projects don't have
 			// compile errors, close and re-open them.
 			try {
				final IJobManager jobManager = Job.getJobManager();
 				jobManager.join(ResourcesPlugin.FAMILY_MANUAL_BUILD, monitor);
 				jobManager.join(ResourcesPlugin.FAMILY_AUTO_BUILD, monitor);
 
 				final IWorkspaceRunnable closeRunnable = new IWorkspaceRunnable() {
 					public void run(final IProgressMonitor monitor) throws CoreException {
 						boolean reopen = false;
 						for (final Iterator it = importedProjects.iterator(); it.hasNext();) {
 							final IProject project = (IProject) it.next();
 							if (project.exists() && existsProblems(project)) {
 								reopen = true;
 							}
 						}
 						if (reopen) {
 							final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 							for (int i = 0; i < projects.length; i++) {
 								final IProject project = projects[i];
 								project.close(new NullProgressMonitor());
 							}
 						}
 					}
 				};
 				final IWorkspaceRunnable openRunnable = new IWorkspaceRunnable() {
 					public void run(final IProgressMonitor monitor) throws CoreException {
 						final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 						for (int i = 0; i < projects.length; i++) {
 							final IProject project = projects[i];
 							project.open(new NullProgressMonitor());
 						}
 					}
 				};
 				ResourcesPlugin.getWorkspace().run(closeRunnable, new NullProgressMonitor());
 				ResourcesPlugin.getWorkspace().run(openRunnable, new NullProgressMonitor());
 
 			} catch (final InterruptedException e) {
 				// just continue.
 			}
 
 			subImportMonitor.done();
 		} finally {
 			monitor.done();
 		}
 	}
 
 	/**
 	 * Returns whether the given problem should potentially abort the launch. By
 	 * default if the problem has an error severity, the problem is considered a
 	 * potential launch problem. Subclasses may override to specialize error
 	 * detection.
 	 * 
 	 * @param problemMarker
 	 *            candidate problem
 	 * @return whether the given problem should potentially abort the launch
 	 * @throws CoreException
 	 *             if any exceptions occurr while accessing marker attributes
 	 */
 	protected boolean isLaunchProblem(final IMarker problemMarker) throws CoreException {
 		final Integer severity = (Integer) problemMarker.getAttribute(IMarker.SEVERITY);
 		if (severity != null) {
 			return severity.intValue() >= IMarker.SEVERITY_ERROR;
 		}
 
 		return false;
 	}
 
 	private void openDefaultEditor() {
 		if (sample.getFileToOpen() == null) {
 			return;
 		}
 
 		final UIJob job = new UIJob("Opening editor") {
 			public IStatus runInUIThread(final IProgressMonitor monitor) {
 				final IFile fileToOpen = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(sample.getFileToOpen()));
 				if ((fileToOpen == null) || !fileToOpen.exists()) {
 					return Status.OK_STATUS;
 				}
 				final IWorkbench workbench = PlatformUI.getWorkbench();
 				final IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
 				final IEditorRegistry registry = workbench.getEditorRegistry();
 				final IEditorDescriptor descriptor = registry.getDefaultEditor(fileToOpen.getName());
 				final String id = descriptor == null ? "org.eclipse.ui.DefaultTextEditor" : descriptor.getId();
 				try {
 					page.openEditor(new FileEditorInput(fileToOpen), id);
 				} catch (final PartInitException e) {
 					Utils.handleError(shell, e, "Error", "Error opening editor");
 				}
 				return Status.OK_STATUS;
 			};
 		};
 		job.schedule(750);
 	}
 
 	public boolean promptToOverwrite() {
		final Preferences prefs = InstanceScope.INSTANCE.getNode(IConstants.PLUGIN_ID);
 		final boolean defaultValue = prefs.getBoolean(IConstants.PROMPT_OVERWRITE_PREF, true);
 		if (!defaultValue) {
 			return true;
 		}
 		final boolean[] retVal = { false };
 		shell.getDisplay().syncExec(new Runnable() {
 			public void run() {
				final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, IConstants.PLUGIN_ID);
 				final Dialog d = MessageDialogWithToggle.openOkCancelConfirm(shell, "Warning", "This will delete all imported projects and in your workspace and replace them with the projects for the selected samples. Are you sure about this?", "Don't show this message again", false, store, IConstants.PROMPT_OVERWRITE_PREF);
 				retVal[0] = d.getReturnCode() == Window.OK;
 				try {
 					store.save();
 				} catch (final IOException e) {
 					Utils.handleError(shell, e, "Error", "Problems saving preferences");
 				}
 			}
 		});
 		return retVal[0];
 	}
 
 	public void run(final IProgressMonitor monitor) {
 		if (sample == null) {
 			return;
 		}
 		if (cleanup) {
 			cleanupWorkspace();
 		}
 		importSample(monitor);
 		openDefaultEditor();
 	}
 }
