 package org.eclipse.iee.editor.wizard.wizards;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourceAttributes;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jdt.launching.LibraryLocation;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
 import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
 
 public class IEEProjectWizard extends Wizard implements INewWizard,
 		IExecutableExtension {
 
 	/*
 	 * Use the WizardNewProjectCreationPage, which is provided by the Eclipse
 	 * framework.
 	 */
 	private WizardNewProjectCreationPage wizardPage;
 
 	private IConfigurationElement config;
 
 	private IWorkbench workbench;
 
 	private IStructuredSelection selection;
 
 	private IProject project;
 
 	/**
 	 * Constructor
 	 */
 	public IEEProjectWizard() {
 		super();
 	}
 
 	public void addPages() {
 		wizardPage = new WizardNewProjectCreationPage(
				"NewExampleComSiteProject");
		wizardPage.setDescription("Create a new Example.com Site Project.");
		wizardPage.setTitle("New Example.com Site Project");
 		addPage(wizardPage);
 	}
 
 	@Override
 	public boolean performFinish() {
 
 		if (project != null) {
 			return true;
 		}
 
 		final IProject projectHandle = wizardPage.getProjectHandle();
 
 		URI projectURI = (!wizardPage.useDefaults()) ? wizardPage
 				.getLocationURI() : null;
 
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 
 		final IProjectDescription desc = workspace
 				.newProjectDescription(projectHandle.getName());
 
 		desc.setLocationURI(projectURI);
 
 		/*
 		 * Just like the ExampleWizard, but this time with an operation object
 		 * that modifies workspaces.
 		 */
 		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
 			protected void execute(IProgressMonitor monitor)
 					throws CoreException {
 				createProject(desc, projectHandle, monitor);
 			}
 		};
 
 		/*
 		 * This isn't as robust as the code in the BasicNewProjectResourceWizard
 		 * class. Consider beefing this up to improve error handling.
 		 */
 		try {
 			getContainer().run(true, true, op);
 		} catch (InterruptedException e) {
 			return false;
 		} catch (InvocationTargetException e) {
 			Throwable realException = e.getTargetException();
 			MessageDialog.openError(getShell(), "Error",
 					realException.getMessage());
 			return false;
 		}
 
 		project = projectHandle;
 
 		if (project == null) {
 			return false;
 		}
 
 		BasicNewProjectResourceWizard.updatePerspective(config);
 		BasicNewProjectResourceWizard.selectAndReveal(project,
 				workbench.getActiveWorkbenchWindow());
 
 		return true;
 	}
 
 	/**
 	 * This creates the project in the workspace.
 	 * 
 	 * @param description
 	 * @param projectHandle
 	 * @param monitor
 	 * @throws CoreException
 	 * @throws OperationCanceledException
 	 */
 	void createProject(IProjectDescription description, IProject proj,
 			IProgressMonitor monitor) throws CoreException,
 			OperationCanceledException {
 		try {
 			proj.create(null);
 			proj.open(null);
 
 			monitor.beginTask("", 2000);
 
 			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
 			proj.setDescription(description, null);
 			/*
 			 * Okay, now we have the project and we can do more things with it
 			 * before updating the perspective.
 			 */
 			IJavaProject javaProject = JavaCore.create(proj);
 
 			/*
 			 * Add the bin folder
 			 */
 			IFolder binFolder = proj.getFolder(new Path("bin"));
 			binFolder.create(false, true, monitor);
 			javaProject.setOutputLocation(binFolder.getFullPath(), null);
 
 			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
 			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
 			LibraryLocation[] locations = JavaRuntime
 					.getLibraryLocations(vmInstall);
 			for (LibraryLocation element : locations) {
 				entries.add(JavaCore.newLibraryEntry(
 						element.getSystemLibraryPath(), null, null));
 			}
 
			IPath jamaEntry = new Path(getClass().getClassLoader()
					.getResource(".").getPath()
					+ "lib/jama-1.0.2.jar");
 
			entries.add(JavaCore.newLibraryEntry(jamaEntry, null, null));
			// add libs to project class path
			javaProject.setRawClasspath(
					entries.toArray(new IClasspathEntry[entries.size()]), null);
 
 			/* Add the src folder */
 			final IFolder srcFolder = proj.getFolder(new Path("src"));
 			srcFolder.create(false, true, monitor);
 
 			/* Add an java file */
 			addFileToProject(proj, new Path("src/iee.java"),
 					GeneralIEEWizard.openContentStream(), monitor);
 
 			/* Add the lib folder */
 			final IFolder libFolder = proj.getFolder(new Path("lib"));
 			libFolder.create(false, true, monitor);
 			
 			InputStream input = IEEProjectWizard.class.getResourceAsStream("templates/Jama-1.0.2.jar");
			
 			/* Add an java file */
 			addFileToProject(proj, new Path("lib/Jama-1.0.2.jar"),
 					input, monitor);

 			/*
 			 * Add the images folder
 			 */
 			IFolder imageFolder = proj.getFolder(new Path("images"));
 			imageFolder.create(false, true, monitor);
 
 			IPackageFragmentRoot root = javaProject
 					.getPackageFragmentRoot(srcFolder);
 			IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
 			IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
 			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
 			newEntries[oldEntries.length] = JavaCore.newSourceEntry(root
 					.getPath());
 			javaProject.setRawClasspath(newEntries, null);
 		} finally {
 			monitor.done();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
 	 * org.eclipse.jface.viewers.IStructuredSelection)
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.workbench = workbench;
 		
 		// snipped...
 	}
 
 	/**
 	 * Sets the initialization data for the wizard.
 	 */
 	public void setInitializationData(IConfigurationElement config,
 			String propertyName, Object data) throws CoreException {
 		// snipped...
 	}
 
 	/**
 	 * Adds a new file to the project.
 	 * 
 	 * @param container
 	 * @param path
 	 * @param contentStream
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	/**
 	 * Adds a new file to the project.
 	 * 
 	 * @param container
 	 * @param path
 	 * @param contentStream
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	private void addFileToProject(IContainer container, Path path,
 			InputStream contentStream, IProgressMonitor monitor)
 			throws CoreException {
 		final IFile file = container.getFile(path);
 
 		if (file.exists()) {
 			file.setContents(contentStream, true, true, monitor);
 		} else {
 			file.create(contentStream, true, monitor);
 		}
 
 	}
 }
