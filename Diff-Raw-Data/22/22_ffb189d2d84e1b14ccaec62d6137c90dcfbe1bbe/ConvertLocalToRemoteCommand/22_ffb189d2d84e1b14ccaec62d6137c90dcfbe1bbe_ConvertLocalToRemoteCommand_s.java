 /*******************************************************************************
  * Copyright (c) 2010-2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.git.api;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.egit.core.RepositoryCache;
 import org.eclipse.jgit.api.FetchCommand;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.MergeCommand;
 import org.eclipse.jgit.lib.ConfigConstants;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.transport.RefSpec;
 import org.eclipse.jgit.transport.RemoteConfig;
 import org.eclipse.jgit.transport.URIish;
 import org.eclipse.osgi.util.NLS;
 import org.fedoraproject.eclipse.packager.IFpProjectBits;
 import org.fedoraproject.eclipse.packager.PackagerPlugin;
 import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
 import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
 import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
 import org.fedoraproject.eclipse.packager.git.FedoraPackagerGitText;
 import org.fedoraproject.eclipse.packager.git.GitUtils;
 import org.fedoraproject.eclipse.packager.git.api.errors.LocalProjectConversionFailedException;
 import org.fedoraproject.eclipse.packager.git.api.errors.RemoteAlreadyExistsException;
 import org.fedoraproject.eclipse.packager.utils.FedoraPackagerUtils;
 
 /**
  * A class used to execute a {@code convert local to remote} command. It has
  * setters for all supported options and arguments of this command and a
  * {@link #call(IProgressMonitor)} method to finally execute the command. Each
  * instance of this class should only be used for one invocation of the command
  * (means: one call to {@link #call(IProgressMonitor)})
  *
  */
 public class ConvertLocalToRemoteCommand extends
 		FedoraPackagerCommand<ConvertLocalResult> {
 
 	private Git git;
 	private String existingRemote;
 	private boolean addRemote = false;
 	private boolean addBranch = false;
 	private boolean hadFetched = false;
 
 	/**
 	 * The unique ID of this command.
 	 */
 	public static final String ID = "ConvertLocalToRemoteCommand"; //$NON-NLS-1$
 
 	/**
 	 * Implementation of the {@code ConvertLocalToRemoteCommand}.
 	 *
 	 * @param monitor
 	 * @throws CommandMisconfiguredException
 	 *             If the command was not properly configured when it was
 	 *             called.
 	 * @throws CommandListenerException
 	 *             If some listener detected a problem.
 	 * @return The result of this command.
 	 * @throws LocalProjectConversionFailedException
 	 * @throws RemoteAlreadyExistsException
 	 */
 	@Override
 	public ConvertLocalResult call(IProgressMonitor monitor)
 			throws CommandMisconfiguredException, CommandListenerException,
 			LocalProjectConversionFailedException, RemoteAlreadyExistsException {
 
 		try {
 			callPreExecListeners();
 		} catch (CommandListenerException e) {
 			if (e.getCause() instanceof CommandMisconfiguredException) {
 				// explicitly throw the specific exception
 				throw (CommandMisconfiguredException) e.getCause();
 			}
 			throw e;
 		}
 
 		IFpProjectBits projectBits = FedoraPackagerUtils
 				.getVcsHandler(projectRoot);
 
 		// Find the local repository
 		RepositoryCache repoCache = org.eclipse.egit.core.Activator
 				.getDefault().getRepositoryCache();
 
 		try {
 			git = new Git(repoCache.lookupRepository(projectRoot.getProject()
 					.getFile(".git").getLocation().toFile())); //$NON-NLS-1$
 
 			String uri = projectBits.getScmUrl();
 			Map<String, Ref> ref = git.getRepository().getAllRefs();
 
 			Set<String> existingRemoteList = git.getRepository().getConfig().getSubsections(
 					ConfigConstants.CONFIG_REMOTE_SECTION);
 
 			if (existingRemoteList.size() == 0) {
 				addRemote = true;
 				addBranch = true;
 			}
 
 			Iterator<String> itr = existingRemoteList.iterator();
 			while (itr.hasNext()) {
 				String remote = itr.next();
 				if (remote.equals("origin")) { //$NON-NLS-1$
 					if (!checkExistingRemoteRepository(uri)) {
 						throw new RemoteAlreadyExistsException(
 								NLS.bind(
 										FedoraPackagerGitText.ConvertLocalToRemoteCommand_existingRemoteNotficiation,
 										existingRemote));
 					} else {
 						if (ref.toString().contains(Constants.R_REMOTES)) {
 							hadFetched = true;
 							addBranch = true;
 						} else {
 							addRemote = true;
 							addBranch = true;
 							hadFetched = false;
 						}
 					}
 				} else {
 					addRemote = true;
 					addBranch = true;
 				}
 			}
 
 			if (addRemote) {
 				addRemoteRepository(uri, monitor);
 			}
 			if (addBranch) {
 				GitUtils.createLocalBranches(git, monitor);
 			}
 			mergeLocalRemoteBranches(monitor);
 
 			// set the project property to main fedora packager's property
 			projectRoot.getProject().setPersistentProperty(
 					PackagerPlugin.PROJECT_PROP, "true"); //$NON-NLS-1$
 			projectRoot.getProject().setPersistentProperty(
 					PackagerPlugin.PROJECT_LOCAL_PROP, null);
 
		} catch (RemoteAlreadyExistsException e) {
			throw new RemoteAlreadyExistsException(e.getMessage(), e);
 		} catch (Exception e) {
			throw new LocalProjectConversionFailedException(e.getMessage(), e);
 		}
 
 		ConvertLocalResult result = new ConvertLocalResult(git, addRemote,
 				addBranch, hadFetched);
 
 		// Call post-exec listeners
 		callPostExecListeners();
 		result.setSuccessful(true);
 		setCallable(false);
 		return result;
 	}
 
 	private boolean checkExistingRemoteRepository(String uri) {
 		existingRemote = git
 				.getRepository()
 				.getConfig()
 				.getString(ConfigConstants.CONFIG_REMOTE_SECTION,
 						"origin", "url"); //$NON-NLS-1$//$NON-NLS-2$
 		String[] existingRemoteSplit = existingRemote.split("://"); //$NON-NLS-1$
 		return uri.contains(existingRemoteSplit[1]);
 	}
 
 	/**
 	 * Adds the corresponding remote repository as the default name 'origin' to
 	 * the existing local repository (uses the JGit API)
 	 *
 	 * @param uri
 	 * @param monitor
 	 * @throws LocalProjectConversionFailedException
 	 */
 	private void addRemoteRepository(String uri, IProgressMonitor monitor)
 			throws LocalProjectConversionFailedException {
 
 		try {
 			RemoteConfig config = new RemoteConfig(git.getRepository()
 					.getConfig(), "origin"); //$NON-NLS-1$
 			config.addURI(new URIish(uri));
 			String dst = Constants.R_REMOTES + config.getName();
 			RefSpec refSpec = new RefSpec();
 			refSpec = refSpec.setForceUpdate(true);
 			refSpec = refSpec.setSourceDestination(
 					Constants.R_HEADS + "*", dst + "/*"); //$NON-NLS-1$ //$NON-NLS-2$
 
 			config.addFetchRefSpec(refSpec);
 			config.update(git.getRepository().getConfig());
 			git.getRepository().getConfig().save();
 
 			// fetch all the remote branches,
 			// create corresponding branches locally and merge them
 			FetchCommand fetch = git.fetch();
 			fetch.setRemote("origin"); //$NON-NLS-1$
 			fetch.setTimeout(0);
 			fetch.setRefSpecs(refSpec);
 			fetch.call();
 
 		} catch (Exception e) {
 			throw new LocalProjectConversionFailedException(e.getCause()
 					.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Merges remote HEAD with local HEAD (uses the JGit API)
 	 *
 	 * @param monitor
 	 * @throws LocalProjectConversionFailedException
 	 */
 	private void mergeLocalRemoteBranches(IProgressMonitor monitor)
 			throws LocalProjectConversionFailedException {
 
 		MergeCommand merge = git.merge();
 		try {
 			merge.include(git.getRepository().getRef(
 					Constants.R_REMOTES + "origin/" + Constants.MASTER)); //$NON-NLS-1$
 			merge.call();
 		} catch (Exception e) {
 			throw new LocalProjectConversionFailedException(e.getCause()
 					.getMessage(), e);
 		}
 	}
 
 	@Override
 	protected void checkConfiguration() throws CommandMisconfiguredException {
 		// We are good to go with the defaults. No-Op.
 	}
 }
