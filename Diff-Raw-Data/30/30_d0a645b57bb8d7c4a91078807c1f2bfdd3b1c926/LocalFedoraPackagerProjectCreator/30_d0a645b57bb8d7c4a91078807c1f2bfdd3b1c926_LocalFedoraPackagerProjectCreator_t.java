 /*******************************************************************************
  * Copyright (c) 2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.local.api;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
import org.eclipse.egit.core.RepositoryUtil;
 import org.eclipse.egit.core.op.ConnectProviderOperation;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.InitCommand;
 import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
 import org.eclipse.jgit.api.errors.JGitInternalException;
 import org.eclipse.jgit.api.errors.NoFilepatternException;
 import org.eclipse.jgit.api.errors.NoHeadException;
 import org.eclipse.jgit.api.errors.NoMessageException;
 import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.util.FileUtils;
 import org.eclipse.linuxtools.rpm.core.RPMProject;
 import org.eclipse.linuxtools.rpm.core.RPMProjectLayout;
 import org.eclipse.linuxtools.rpmstubby.Generator;
 import org.eclipse.linuxtools.rpmstubby.InputType;
 import org.fedoraproject.eclipse.packager.local.LocalFedoraPackagerPlugin;
 import org.fedoraproject.eclipse.packager.local.LocalFedoraPackagerText;
 import org.fedoraproject.eclipse.packager.local.internal.ui.LocalFedoraPackagerPageFour;
 
 /**
  * Utility class to create to enable existing and 
  *   new maintainers work with fedora packages locally
  * 
  */
 public class LocalFedoraPackagerProjectCreator {
 
 	private IProject project;
 	private IProgressMonitor monitor;
 	private Repository repository;
 	private Git git;
 
 	/**
 	 * Construct the local fedora packager project 
 	 *   based on the created project in main wizard
 	 * @param IProject
 	 *            the base of the project
 	 * @param IProgressMonitor
 	 *            Progress monitor to report back status
 	 *
 	 */
 	public LocalFedoraPackagerProjectCreator(IProject project, IProgressMonitor monitor) {
 		this.project = project;
 		this.monitor = monitor;
 	}
 	
 	/**
 	 * Starts a plain project using the specfile template
 	 *
 	 * @param LocalFedoraPackagerPageFour
 	 *            instance of this class to get the contents
 	 * @param IProject
 	 *            the base of the project
 	 * @param IProgressMonitor
 	 *            Progress monitor to report back status           
 	 * @throws CoreException 
 	 *
 	 */
 	public void create(LocalFedoraPackagerPageFour pageFour) throws CoreException{
 		final String projectName = project.getName();
 		final String fileName = projectName + ".spec"; //$NON-NLS-1$
 		final InputStream contentInputStream = new ByteArrayInputStream(
 				pageFour.getContent().getBytes());
 		final IFile file = project.getFile(new Path(fileName));
 		try {
 			InputStream stream = contentInputStream;
 			if (file.exists()) {
 				file.setContents(stream, true, true, monitor);
 			} else {
 				file.create(stream, true, monitor);
 			}
 			stream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Populate the project based on the imported srpm file
 	 *
 	 * @param File
 	 *            the external xml file uploaded from file system     
 	 * @throws CoreException
 	 */
 	public void create(File externalFile) throws CoreException {
 		RPMProject rpmProject = new RPMProject
 				(project, RPMProjectLayout.FLAT);
 		rpmProject.importSourceRPM(externalFile);
 	}
 
 	/**
 	 * Populate the project using rpmstubby based on the 
 	 *  eclipse-feature or maven-pom choice of user
 	 *
 	 * @param InputType
 	 *            type of the stubby project
 	 * @param File
 	 *            the external xml file uploaded from file system
 	 * @throws CoreException 
 	 * @throws FileNotFoundException 
 	 * 
 	 */
 	public void create(InputType inputType, File externalFile) 
 			throws FileNotFoundException, CoreException {
 			IFile stubbyFile = project.getFile(externalFile.getName());
 			stubbyFile.create(new FileInputStream(externalFile), false, monitor);
 
 			Generator specfilegGenerator = new Generator(inputType);
 			specfilegGenerator.generate(stubbyFile);
 	}
 
 	/**
 	 * Creates project structure inside the base project
 	 *
 	 * @throws CoreException
 	 * @throws IOException
 	 * @throws WrongRepositoryStateException
 	 * @throws JGitInternalException
 	 * @throws ConcurrentRefUpdateException
 	 * @throws NoMessageException
 	 * @throws NoHeadException
 	 * @throws NoFilepatternException
 	 *
 	 */
 	public void createProjectStructure() throws NoFilepatternException,
 			NoHeadException, NoMessageException, ConcurrentRefUpdateException,
 			JGitInternalException, WrongRepositoryStateException, IOException,
 			CoreException {
 		File directory = createLocalGitRepo();
 
 		addContentToGitRepo(directory);
 
 		// Set persistent property so that we know when to show the context
 		// menu item.
 		project.setPersistentProperty(LocalFedoraPackagerPlugin.PROJECT_PROP,
 				"true" /* unused value */); //$NON-NLS-1$
 
 		ConnectProviderOperation connect = new ConnectProviderOperation(project);
 		connect.execute(null);
		
		// Add created repository to the list of Git repositories so that it
		// shows up in the Git repositories view.
		final RepositoryUtil config = org.eclipse.egit.core.Activator.getDefault().getRepositoryUtil();
		config.addConfiguredRepository(repository.getDirectory());
 	}
 
 	/**
 	 * Initialize a local git repository in project location
 	 *
 	 * @throws IOException
 	 * @return File directory of the git repository
 	 */
 	private File createLocalGitRepo() throws IOException {
 		File directory = new File(project.getLocation().toString());
 		FileUtils.mkdirs(directory, true);
 		directory.getCanonicalFile();
 
 		InitCommand command = new InitCommand();
 		command.setDirectory(directory);
 		command.setBare(false);
 		repository = command.call().getRepository();
 
 		git = new Git(repository);
 		return directory;
 	}
 
 	/**
 	 * Add the contents to the Git repository and 
 	 * does the first commit
 	 *
 	 * @param File
 	 *            directory of the git repository
 	 * @throws NoFilepatternException
 	 * @throws IOException
 	 * @throws WrongRepositoryStateException
 	 * @throws JGitInternalException
 	 * @throws ConcurrentRefUpdateException
 	 * @throws NoMessageException
 	 * @throws NoHeadException
 	 * @throws CoreException
 	 */
 	private void addContentToGitRepo(File directory) throws IOException,
 			NoFilepatternException, NoHeadException, NoMessageException,
 			ConcurrentRefUpdateException, JGitInternalException,
 			WrongRepositoryStateException, CoreException {
 
 		for (File file : directory.listFiles()) {
 			String name = file.getName();
 
 			if (name.contains(".spec")) { //$NON-NLS-1$
 				git.add().addFilepattern(name).call();
 			}
 
 			if (name.equals(".gitignore") || name.equals(".project")) { //$NON-NLS-1$
 				git.add().addFilepattern(name).call();
 			}
 		}
 
 		// do the first commit
 		git.commit().setMessage(LocalFedoraPackagerText.LocalFedoraPackagerProjectCreator_FirstCommit)
 				.call();
 	}
 
 }
