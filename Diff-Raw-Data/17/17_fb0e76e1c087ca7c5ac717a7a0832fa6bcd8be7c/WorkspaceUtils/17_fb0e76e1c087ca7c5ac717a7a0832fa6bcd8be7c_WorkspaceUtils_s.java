 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.test.util;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Provide utilities to ease workspace manipulation.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public final class WorkspaceUtils {
 
 	/**
 	 * Prevents instantiation.
 	 */
 	private WorkspaceUtils() {
 	}
 
 	/**
 	 * Creates and opens a project using the given name.
 	 * 
 	 * @param projectName
 	 *            the project name
 	 * @param monitor
 	 *            the progress monitor
 	 * @return the newly created project or the existing one if present
 	 * @throws CoreException
 	 *             if there is an issue creating the project
 	 */
 	public static IProject createProject(final String projectName, IProgressMonitor monitor)
 			throws CoreException {
 		return createProject(projectName, monitor, true);
 	}
 
 	/**
 	 * Creates a project using the given name.
 	 * 
 	 * @param projectName
 	 *            the project name
 	 * @param monitor
 	 *            the progress monitor
	 * @param openProject
	 *            indicates whether created project should be opened
 	 * @return the newly created project or the existing one if present
 	 * @throws CoreException
 	 *             if there is an issue creating the project
 	 */
	public static IProject createProject(String projectName, IProgressMonitor monitor, boolean openProject)
			throws CoreException {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists()) {
 			project.create(monitor);
 		}
		if (!project.isOpen() && openProject) {
 			project.open(monitor);
 		}
 		return project;
 	}
 
 	/**
 	 * Unzip all the projects contained by the zip.
 	 * 
 	 * @param bundleName
 	 *            the bundle name
 	 * @param zipLocation
 	 *            the zip location inside of the bundle
 	 * @param monitor
 	 *            the progress monitor
 	 * @return
 	 * @throws IOException
 	 *             if there is an issue copying a file from the zip
 	 * @throws CoreException
 	 *             if there is an issue creating one of the projects
 	 */
 	public static Collection<IProject> unzipAllProjects(String bundleName, String zipLocation,
 			IProgressMonitor monitor) throws IOException, CoreException {
 		final URL interpreterZipUrl = FileLocator.find(Platform.getBundle(bundleName), new Path(zipLocation),
 				null);
 		final ZipInputStream zipFileStream = new ZipInputStream(interpreterZipUrl.openStream());
 		ZipEntry zipEntry = zipFileStream.getNextEntry();
 
 		Set<IProjectDescription> projectsToCreate = Sets.newLinkedHashSet();
 
 		while (zipEntry != null) {
 			String projectName = zipEntry.getName().split("/")[0];
 
 			IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(
 					projectName);
 			projectDescription.setLocation(new Path(ResourcesPlugin.getWorkspace().getRoot().getLocation()
					+ "/" + projectName));
 			projectsToCreate.add(projectDescription);
 			createProject(projectName, monitor, false);
 
 			final File file = new File(projectDescription.getLocation().toString(), zipEntry.getName()
 					.replaceFirst(projectName + "/", "")); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
 
 			if (!zipEntry.isDirectory()) {
 
 				/*
 				 * Copy files (and make sure parent directory exist)
 				 */
 				final File parentFile = file.getParentFile();
 				if (null != parentFile && !parentFile.exists()) {
 					parentFile.mkdirs();
 				}
 
 				OutputStream os = null;
 
 				try {
 					os = new FileOutputStream(file);
 
 					final int bufferSize = 102400;
 					final byte[] buffer = new byte[bufferSize];
 					while (true) {
 						final int len = zipFileStream.read(buffer);
 						if (zipFileStream.available() == 0) {
 							break;
 						}
 						os.write(buffer, 0, len);
 					}
 				} finally {
 					if (null != os) {
 						os.close();
 					}
 				}
 			}
 			zipFileStream.closeEntry();
 			zipEntry = zipFileStream.getNextEntry();
 		}
 
 		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
 
 		Collection<IProject> projects = Sets.newLinkedHashSet();
 		for (IProjectDescription projectToCreate : projectsToCreate) {
 			createProject(projectToCreate, new NullProgressMonitor());
 		}
 		return projects;
 	}
 
 	/**
 	 * /** Creates and opens a project using the given project description.
 	 * 
 	 * @param newProjectDescription
 	 *            the project to create description
 	 * @param monitor
 	 *            the progress monitor
 	 * @return the newly created project or the existing one if present
 	 * @throws CoreException
 	 *             if there is an issue creating the project
 	 */
 	private static IProject createProject(IProjectDescription newProjectDescription, IProgressMonitor monitor)
 			throws CoreException {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot()
 				.getProject(newProjectDescription.getName());
 		if (!project.exists()) {
 			project.create(newProjectDescription, monitor);
 		}
 		if (!project.isOpen()) {
 			project.open(monitor);
 		}
 		return project;
 	}
 
 	/**
 	 * Imports a java project in the test workspace.
 	 * 
 	 * @param zipLocation
 	 *            the location of the archive containing the project to import (e.g.
 	 *            'data/unit/java/java.example01.zip')
 	 */
 	public static void importJavaProject(String zipLocation) {
 		try {
 			// Deactivate the auto build to avoid problem of test before build is
 			// finish.
 			ResourcesPlugin.getWorkspace().getDescription().setAutoBuilding(false);
 			unzipAllProjects("org.eclipse.mylyn.docs.intent.client.ui.test", zipLocation,
 					new NullProgressMonitor());
 			// Launch a manual build and wait the end of the workspace build
 			ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD,
 					new NullProgressMonitor());
 			ResourcesPlugin.getWorkspace().getDescription().setAutoBuilding(true);
 		} catch (IOException e) {
 			AssertionFailedError assertionFailedError = new AssertionFailedError(
 					"Could not import java project in test workspace");
 			assertionFailedError.setStackTrace(e.getStackTrace());
 			throw assertionFailedError;
 		} catch (CoreException e) {
 			AssertionFailedError assertionFailedError = new AssertionFailedError(
 					"Could not import java project in test workspace");
 			assertionFailedError.setStackTrace(e.getStackTrace());
 			throw assertionFailedError;
 		}
 	}
 
 	/**
 	 * Deletes every project in the workspace.
 	 */
 	public static void cleanWorkspace() {
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
 		for (final IProject proj : Lists.newArrayList(ResourcesPlugin.getWorkspace().getRoot().getProjects())) {
 			try {
 				proj.delete(true, new NullProgressMonitor());
 				System.out.println("deleting " + proj);
 			} catch (CoreException e) {
 				// Nothing we can do
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Close the welcomePage.
 	 */
 	public static void closeWelcomePage() {
 		IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
 				.getActivePart();
 		if (activePart != null && "Welcome".equals(activePart.getTitle()) && activePart instanceof IViewPart) {
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
 					.hideView((IViewPart)activePart);
 		}
 	}
 }
