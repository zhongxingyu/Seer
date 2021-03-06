 /*******************************************************************************
  * Copyright (c) 2008 itemis AG (http://www.itemis.eu) and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.xtext.ui.core.util;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 
 /**
  * Represents a helper/util class to work with eclipse core resource
  * abstractions for workspace,project,files and directories. For the most part
  * this code was copied from the last version 4.3.x of openArchitectureWare.
 *
  * @author Michael Clay
  */
 public class EclipseResourceUtil {
 
 	private static Logger logger = Logger.getLogger(EclipseResourceUtil.class);
 
 	public static final String ISO_8859_1 = "iso-8859-1";
 
 	public static IProject createProject(final String projectName, final List<String> srcFolders,
 			final List<IProject> referencedProjects, final Set<String> requiredBundles,
			final List<String> exportedPackages, final List<String> importedPackages,
			final String activatorClassName,
 			final IProgressMonitor progressMonitor,	final Shell theShell) {
 		IProject project = null;
 		try {
 			progressMonitor.beginTask("", 10);
 			progressMonitor.subTask("Creating project " + projectName);
 			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			project = workspace.getRoot().getProject(projectName);
 
 			// Clean up any old project information.
 			if (project.exists()) {
 				final boolean[] result = new boolean[1];
 				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 					public void run() {
 						result[0] = MessageDialog.openQuestion(theShell, "Do you want to overwrite the project "
 								+ projectName, "Note that everything inside the project '" + projectName
 								+ "' will be deleted if you confirm this dialog.");
 					}
 				});
 				if (result[0]) {
 					project.delete(true, true, new SubProgressMonitor(progressMonitor, 1));
 				}
 				else
 					return null;
 			}
 
 			final IJavaProject javaProject = JavaCore.create(project);
 			final IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(
 					projectName);
 			projectDescription.setLocation(null);
 			project.create(projectDescription, new SubProgressMonitor(progressMonitor, 1));
 			final List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
 			if (referencedProjects.size() != 0) {
 				projectDescription.setReferencedProjects(referencedProjects.toArray(new IProject[referencedProjects
 						.size()]));
 				for (final IProject referencedProject : referencedProjects) {
 					final IClasspathEntry referencedProjectClasspathEntry = JavaCore.newProjectEntry(referencedProject
 							.getFullPath());
 					classpathEntries.add(referencedProjectClasspathEntry);
 				}
 			}
 
 			projectDescription.setNatureIds(new String[] { JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature" });
 
 			final ICommand java = projectDescription.newCommand();
 			java.setBuilderName(JavaCore.BUILDER_ID);
 
 			final ICommand manifest = projectDescription.newCommand();
 			manifest.setBuilderName("org.eclipse.pde.ManifestBuilder");
 
 			final ICommand schema = projectDescription.newCommand();
 			schema.setBuilderName("org.eclipse.pde.SchemaBuilder");
 
 			projectDescription.setBuildSpec(new ICommand[] { java, manifest, schema });
 
 			project.open(new SubProgressMonitor(progressMonitor, 1));
 			project.setDescription(projectDescription, new SubProgressMonitor(progressMonitor, 1));
 
 			for (final String src : srcFolders) {
 				final IFolder srcContainer = project.getFolder(src);
 				if (!srcContainer.exists()) {
 					srcContainer.create(false, true, new SubProgressMonitor(progressMonitor, 1));
 				}
 				final IClasspathEntry srcClasspathEntry = JavaCore.newSourceEntry(srcContainer.getFullPath());
 				classpathEntries.add(srcClasspathEntry);
 			}
 
 			classpathEntries.add(JavaCore.newContainerEntry(new Path("org.eclipse.jdt.launching.JRE_CONTAINER")));
 			classpathEntries.add(JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins")));
 
 			javaProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]),
 					new SubProgressMonitor(progressMonitor, 1));
 
 			javaProject.setOutputLocation(new Path("/" + projectName + "/bin"), new SubProgressMonitor(progressMonitor,
 					1));
 			createManifest(projectName, requiredBundles, exportedPackages, importedPackages, activatorClassName, progressMonitor, project);
 			createBuildProps(progressMonitor, project, srcFolders);
 		}
 		catch (final Exception exception) {
 			exception.printStackTrace();
 			logger.error(exception);
 		}
 		finally {
 			progressMonitor.done();
 		}
 
 		return project;
 	}
 
 	public static IFile createFile(final String name, final IContainer container, final String content,
 			final IProgressMonitor progressMonitor) {
 		final IFile file = container.getFile(new Path(name));
 		assertExist(file.getParent());
 		try {
 			final InputStream stream = new ByteArrayInputStream(content.getBytes(file.getCharset()));
 			if (file.exists()) {
 				file.setContents(stream, true, true, progressMonitor);
 			}
 			else {
 				file.create(stream, true, progressMonitor);
 			}
 			stream.close();
 		}
 		catch (final Exception e) {
 			logger.error(e);
 		}
 		progressMonitor.worked(1);
 
 		return file;
 	}
 
 	public static IFile createFile(final String name, final IContainer container, final String content,
 			final String charSet, final IProgressMonitor progressMonitor) throws CoreException {
 		final IFile file = createFile(name, container, content, progressMonitor);
 		if (file != null && charSet != null) {
 			file.setCharset(charSet, progressMonitor);
 		}
 
 		return file;
 	}
 
 	private static void createBuildProps(final IProgressMonitor progressMonitor, final IProject project,
 			final List<String> srcFolders) {
 		final StringBuilder bpContent = new StringBuilder("source.. = ");
 		for (final Iterator<String> iterator = srcFolders.iterator(); iterator.hasNext();) {
 			bpContent.append(iterator.next()).append('/');
 			if (iterator.hasNext()) {
 				bpContent.append(",");
 			}
 		}
 		bpContent.append("\n");
		bpContent.append("bin.includes = META-INF/,\\\n");
		bpContent.append("               .,\\\n");
		bpContent.append("               plugin.xml");

 		createFile("build.properties", project, bpContent.toString(), progressMonitor);
 	}
 
 	private static void createManifest(final String projectName, final Set<String> requiredBundles,
 			final List<String> exportedPackages, final List<String> importedPackages,
			final String activatorClassName,
 			final IProgressMonitor progressMonitor,	final IProject project) throws CoreException {
 		final StringBuilder mainContent = new StringBuilder("Manifest-Version: 1.0\n");
 		mainContent.append("Bundle-ManifestVersion: 2\n");
 		mainContent.append("Bundle-Name: " + projectName + "\n");
 		mainContent.append("Bundle-Vendor: My Company\n");
 		mainContent.append("Bundle-Version: 1.0.0\n");
 		mainContent.append("Bundle-SymbolicName: " + projectName.toLowerCase() + "; singleton:=true\n");
 		mainContent.append("Eclipse-RegisterBuddy: org.eclipse.xtext.log4j\n");
 //		mainContent.append("Bundle-RequiredExecutionEnvironment: J2SE-1.5\n");
 		if (null != activatorClassName) {
 			mainContent.append("Bundle-Activator: " + activatorClassName + "\n");
 		}
 		mainContent.append("Bundle-ActivationPolicy: lazy\n");
 
 		if (!requiredBundles.isEmpty()) {
 			mainContent.append("Require-Bundle: ");
 		}

 		for (Iterator<String> iterator = requiredBundles.iterator(); iterator.hasNext();) {
 			mainContent.append(" " + iterator.next());
 			if (iterator.hasNext()) {
 				mainContent.append(",");
 			}
 			mainContent.append("\n");
 		}
 
 		if (exportedPackages != null && !exportedPackages.isEmpty()) {
 			mainContent.append("Export-Package: " + exportedPackages.get(0));
 			for (int i = 1, x = exportedPackages.size(); i < x; i++) {
 				mainContent.append(",\n " + exportedPackages.get(i));
 			}
 			mainContent.append("\n");
 		}

 		if (importedPackages != null && !importedPackages.isEmpty()) {
 			mainContent.append("Import-Package: " + importedPackages.get(0));
 			for (int i = 1, x = importedPackages.size(); i < x; i++) {
 				mainContent.append(",\n " + importedPackages.get(i));
 			}
 			mainContent.append("\n");
 		}
 
 		final IFolder metaInf = project.getFolder("META-INF");
 		metaInf.create(false, true, new SubProgressMonitor(progressMonitor, 1));
 		createFile("MANIFEST.MF", metaInf, mainContent.toString(), progressMonitor);
 	}
 
 	/**
 	 * @param name
 	 *            of the destination file
 	 * @param container
 	 *            directory containing the the destination file
 	 * @param contentUrl
 	 *            Url pointing to the src of the content
 	 * @param progressMonitor
 	 *            used to interact with and show the user the current operation
 	 *            status
 	 * @return
 	 */
 	public static IFile createFile(final String name, final IContainer container, final URL contentUrl,
 			final IProgressMonitor progressMonitor) {
 
 		final IFile file = container.getFile(new Path(name));
 		InputStream inputStream = null;
 		try {
 			inputStream = contentUrl.openStream();
 			if (file.exists()) {
 				file.setContents(inputStream, true, true, progressMonitor);
 			}
 			else {
 				file.create(inputStream, true, progressMonitor);
 			}
 			inputStream.close();
 		}
 		catch (final Exception e) {
 			logger.error(e);
 		}
 		finally {
 			if (null != inputStream) {
 				try {
 					inputStream.close();
 				}
 				catch (final IOException e) {
 					logger.error(e);
 				}
 			}
 		}
 		progressMonitor.worked(1);
 
 		return file;
 	}
 
 	private static void assertExist(final IContainer c) {
 		if (!c.exists()) {
 			if (!c.getParent().exists()) {
 				assertExist(c.getParent());
 			}
 			if (c instanceof IFolder) {
 				try {
 					((IFolder) c).create(false, true, new NullProgressMonitor());
 				}
 				catch (final CoreException e) {
 					logger.error(e);
 				}
 			}
 
 		}
 
 	}
 
 	public static void openFileToEdit(final Shell s, final IFile file) {
 		s.getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 				try {
 					IDE.openEditor(page, file, true);
 				}
 				catch (final PartInitException e) {
 					logger.error(e);
 				}
 			}
 		});
 	}
 
 	public static void createPackagesWithDummyClasses(IProject dslProject, String string, List<String> exportedPackages) throws CoreException {
 		for (String string2 : exportedPackages) {
 			IFolder folder = dslProject.getFolder(string+"/"+(string2.replace('.', '/')));
 			create(folder);
 			IFile file = folder.getFile("Foo.java");
 			String contents = "package "+string2+";\nclass Foo {}";
 			file.create(new ByteArrayInputStream(contents.getBytes()), true, null);
 		}
 	}
 
 	private static void create(IFolder folder) throws CoreException {
 		if (!folder.getParent().exists())
 			create((IFolder) folder.getParent());
 		folder.create(true, true, null);
 	}
 
 }
