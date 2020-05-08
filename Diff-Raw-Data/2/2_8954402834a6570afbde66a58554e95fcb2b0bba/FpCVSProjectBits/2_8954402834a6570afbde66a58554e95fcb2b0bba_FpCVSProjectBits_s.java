 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.cvs;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.team.core.RepositoryProvider;
 import org.eclipse.team.internal.ccvs.core.CVSException;
 import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
 import org.eclipse.team.internal.ccvs.core.CVSTag;
 import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
 import org.eclipse.team.internal.ccvs.core.ICVSFile;
 import org.eclipse.team.internal.ccvs.core.ICVSFolder;
 import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
 import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
 import org.eclipse.team.internal.ccvs.core.client.Command;
 import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
 import org.eclipse.team.internal.ccvs.core.client.Session;
 import org.eclipse.team.internal.ccvs.core.client.Tag;
 import org.eclipse.team.internal.ccvs.core.client.listeners.TagListener;
 import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
 import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
 import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
 import org.fedoraproject.eclipse.packager.IFpProjectBits;
 import org.fedoraproject.eclipse.packager.PackagerPlugin;
 import org.fedoraproject.eclipse.packager.SourcesFile;
 import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;
 
 /**
  * CVS specific FpProject bits. Implementation of
  * org.fedoraproject.eclipse.packager.vcsContribution
  * extension point.
  * 
  * @author Red Hat Inc.
  * 
  */
 @SuppressWarnings("restriction")
 public class FpCVSProjectBits implements IFpProjectBits {
 
 	private FedoraProjectRoot fedoraprojectRoot; // The underlying project root
 	private IResource container; 				 // The underlying container
 	private HashMap<String, HashMap<String, String>> branches; // All branches
 	private boolean initialized = false; 		 // keep track if instance is initialized
 	
 	/**
 	 * See {@link IFpProjectBits#getCurrentBranchName()}
 	 */
 	@Override
 	public String getCurrentBranchName() {
 		if (!isInitialized()) {
 			return null;
 		}
 		// retrieve current branch name from branch file.
 		// Current implementation is incorrect.
 		IFile branchesFile = this.fedoraprojectRoot.getContainer().getFile(new Path("branch"));
		if (branchesFile != null) {
 			InputStream is;
 			try {
 				is = branchesFile.getContents();
 				BufferedReader bufReader = new BufferedReader(
 						new InputStreamReader(is));
 				String line = bufReader.readLine();
 				if (line != null) {
 					line = line.trim();
 					if (!line.equals("")) {
 						return line;
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (CoreException e) {
 				e.printStackTrace();
 			}
 		}
 		// default to devel
 		return "devel"; //$NON-NLS-1$
 	}
 
 	/**
 	 * See {@link IFpProjectBits#getBranchName(String)}
 	 */
 	@Override
 	public String getBranchName(String branchName) {
 		// make sure we are properly initialized
 		if (!isInitialized()) {
 			return null;
 		}
 		// check for early-branched
 		if (branchName.equals("devel")) { //$NON-NLS-1$
 			try {
 				return getDevelBranch();
 			} catch (CoreException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return branchName;
 	}
 
 	/**
 	 * Parse branches from "common/branches" file.
 	 * 
 	 * @return A map of branch names and according properties required for
 	 * 		   building.
 	 */
 	// TODO: We need to be smarter than this. Won't work for messed up branches
 	//		 file. Probably we should walk the project and look for folders which
 	//       contain "sources" and ".spec" files. We can never be 100% accurate
 	//       so we should make sure that we do proper error checking.
 	private HashMap<String, HashMap<String, String>> getBranches() {
 		HashMap<String, HashMap<String, String>> ret = new HashMap<String, HashMap<String, String>>();
 
 		IFile branchesFile = this.fedoraprojectRoot.getProject().getFolder("common").getFile( //$NON-NLS-1$
 				"branches"); //$NON-NLS-1$
 		InputStream is;
 		try {
 			is = branchesFile.getContents();
 			BufferedReader bufReader = new BufferedReader(
 					new InputStreamReader(is));
 			List<String> branches = new ArrayList<String>();
 			String line;
 			while ((line = bufReader.readLine()) != null) {
 				// skip commented lines
 				if (! line.startsWith("#")) {
 					branches.add(line);
 				}
 			}
 
 			for (String branch : branches) {
 				HashMap<String, String> temp = new HashMap<String, String>();
 				StringTokenizer st = new StringTokenizer(branch, ":"); //$NON-NLS-1$
 				String target = null;
 				try {
 					target = st.nextToken();
 					temp.put("target", st.nextToken()); //$NON-NLS-1$
 					temp.put("dist", st.nextToken()); //$NON-NLS-1$
 					temp.put("distvar", st.nextToken()); //$NON-NLS-1$
 					temp.put("distval", st.nextToken()); //$NON-NLS-1$
 				} catch (NoSuchElementException e) {
 					// ignore cases for which branches file is broken
 				}
 				ret.put(target, temp);
 			}
 		} catch (CoreException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Checks if branch has been early-branched.
 	 * 
 	 * @return The actual name of the development branch.
 	 * @throws CoreException
 	 */
 	private String getDevelBranch() throws CoreException {
 		int highestVersion = 0;
 		for (String branch : branches.keySet()) {
 			if (branch.startsWith("F-")) { //$NON-NLS-1$
 				int version = Integer.parseInt(branch.substring(2));
 				highestVersion = Math.max(version, highestVersion);
 			}
 		}
 		String newestBranch = "F-" + String.valueOf(highestVersion); //$NON-NLS-1$
 		String secondNewestBranch = "F-" + String.valueOf(highestVersion - 1); //$NON-NLS-1$
 
 		// Why is it determining if a .spec file is present?
 		return containsSpec(secondNewestBranch) ? newestBranch : "devel"; //$NON-NLS-1$
 	}
 
 	/**
 	 * Check if branch contains spec file.
 	 * 
 	 * @param branch
 	 * @return True if given branch contains the spec file in CVS.
 	 * @throws CoreException
 	 */
 	private boolean containsSpec(String branch) throws CoreException {
 		// get CVSProvider
 		CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
 				.getProvider(this.container.getProject(),
 						CVSProviderPlugin.getTypeId());
 
 		// get CVSROOT
 		CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();
 
 		ICVSFolder folder = cvsRoot.getLocalRoot().getFolder(branch);
 
 		// search "branch" for a spec file
 		// FIXME: Make this less hard-coded!
 		return folder.getFile(this.container.getProject().getName() + ".spec") != null;
 	}
 	
 	/**
 	 * Determine if instance has been properly initialized
 	 */
 	private boolean isInitialized() {
 		return this.initialized;
 	}
 
 	/**
 	 * See {@link IFpProjectBits#getScmUrl()}
 	 */
 	@Override
 	public String getScmUrl() {
 		// make sure we are properly initialized
 		if (!isInitialized()) {
 			return null;
 		}
 		String ret = null;
 		// get the project for this resource
 		IProject proj = this.container.getProject();
 
 		if (CVSTeamProvider.isSharedWithCVS(proj)) {
 			// get CVSProvider
 			CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
 					.getProvider(proj, CVSProviderPlugin.getTypeId());
 			// get Repository Location
 			try {
 				ICVSRepositoryLocation location = provider.getRemoteLocation();
 
 				// get CVSROOT
 				CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();
 
 				ICVSFolder folder = cvsRoot.getLocalRoot();
 				FolderSyncInfo syncInfo = folder.getFolderSyncInfo();
 
 				String module = syncInfo.getRepository();
 
 				ret = "cvs://" + location.getHost() + location.getRootDirectory() //$NON-NLS-1$
 						+ "?" + module + "/" + getCurrentBranchName(); //$NON-NLS-1$ //$NON-NLS-2$
 			} catch (CVSException cvsException) {
 				cvsException.printStackTrace();
 			}
 		}
 
 		return ret;
 	}
 	
 	/**
 	 * Do CVS update to get updated "sources" and ".cvsignore" file.
 	 * 
 	 * See {@link IFpProjectBits#updateVCS(FedoraProjectRoot, IProgressMonitor)}
 	 */
 	@Override
 	public IStatus updateVCS(FedoraProjectRoot projectRoot,
 			IProgressMonitor monitor) {
 		IStatus status = Status.OK_STATUS;
 		IFile specfile = projectRoot.getSpecFile();
 		File ignoreFile = projectRoot.getIgnoreFile();
 		SourcesFile sources = projectRoot.getSourcesFile();
 		// get CVSProvider
 		CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
 				.getProvider(specfile.getProject(),
 						CVSProviderPlugin.getTypeId());
 
 		try {
 			ICVSRepositoryLocation location = provider.getRemoteLocation();
 
 			// get CVSROOT
 			CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();
 			ICVSFolder rootFolder = cvsRoot.getLocalRoot();
 
 			// get Branch
 			ICVSFolder branchFolder = rootFolder.getFolder(specfile.getParent()
 					.getName());
 			if (branchFolder != null) {
 				ICVSFile cvsSources = branchFolder.getFile(sources.getName());
 				if (cvsSources != null) {
 					// if 'sources' is not shared with CVS, add it
 					Session session = new Session(location, branchFolder, true);
 					session.open(monitor, true);
 					if (!cvsSources.isManaged()) {
 						if (monitor.isCanceled()) {
 							throw new OperationCanceledException();
 						}
 						String[] arguments = new String[] { sources.getName() };
 						status = Command.ADD.execute(session,
 								Command.NO_GLOBAL_OPTIONS,
 								Command.NO_LOCAL_OPTIONS, arguments, null,
 								monitor);
 					}
 					if (status.isOK()) {
 						// everything has passed so far
 						if (monitor.isCanceled()) {
 							throw new OperationCanceledException();
 						}
 						// perform update on sources and .cvsignore
 						String[] arguments = new String[] { sources.getName(),
 								ignoreFile.getName() };
 						status = Command.UPDATE.execute(session,
 								Command.NO_GLOBAL_OPTIONS,
 								Command.NO_LOCAL_OPTIONS, arguments, null,
 								monitor);
 					}
 				} else {
 					status = new Status(IStatus.ERROR ,
 							PackagerPlugin.PLUGIN_ID, "Can't find sources file"); //$NON-NLS-1$
 				}
 			} else {
 				status =  new Status(IStatus.ERROR ,
 						PackagerPlugin.PLUGIN_ID, "Can't find sources file"); //$NON-NLS-1$
 			}
 
 		} catch (CVSException e) {
 			e.printStackTrace();
 			status = new Status(IStatus.ERROR ,
 					PackagerPlugin.PLUGIN_ID, e.getMessage(), e);
 		}
 		return status;
 	}
 	
 	/**
 	 * Do proper initialization of this instance.
 	 * 
 	 * @param fedoraProjectRoot The underlying project.
 	 */
 	@Override
 	public void initialize(FedoraProjectRoot fedoraProjectRoot) {
 		this.fedoraprojectRoot = fedoraProjectRoot;
 		this.container = fedoraProjectRoot.getContainer();
 		this.branches = getBranches();
 		this.initialized = true;
 	}
 
 	/**
 	 * Get distribution String.
 	 * 
 	 * See {@link IFpProjectBits#getDist()}
 	 */
 	@Override
 	public String getDist() {
 		return this.branches.get(getCurrentBranchName()).get("dist");//$NON-NLS-1$
 	}
 
 	/**
 	 * See {@link IFpProjectBits#getDistVal()}
 	 */
 	@Override
 	public String getDistVal() {
 		return this.branches.get(getCurrentBranchName()).get("distval"); //$NON-NLS-1$
 	}
 
 	/**
 	 * See {@link IFpProjectBits#getDistVariable()}
 	 */
 	@Override
 	public String getDistVariable() {
 		return this.branches.get(getCurrentBranchName()).get("distvar"); //$NON-NLS-1$
 	}
 
 	/**
 	 * See {@link IFpProjectBits#getTarget()}
 	 */
 	@Override
 	public String getTarget() {
 		return this.branches.get(getCurrentBranchName()).get("target"); //$NON-NLS-1$
 	}
 	
 	/**
 	 * See {@link IFpProjectBits#ignoreResource(IResource)}
 	 */
 	@Override
 	public IStatus ignoreResource(IResource resourceToIgnore) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	/**
 	 * Do CVS tag.
 	 * 
 	 * See {@link IFpProjectBits#tagVcs(FedoraProjectRoot, IProgressMonitor)}
 	 */
 	@Override
 	public IStatus tagVcs(FedoraProjectRoot projectRoot,
 			IProgressMonitor monitor) {
 		monitor.subTask("Generating Tag Name from Specfile");
 		final String tagName;
 		try {
 			tagName = FedoraHandlerUtils.makeTagName(projectRoot);
 		} catch (CoreException e) {
 			e.printStackTrace();
 			return new Status(IStatus.ERROR, CVSPlugin.PLUGIN_ID, e.getMessage());
 		}
 
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 		monitor.subTask("Tagging as " + tagName);
 		IStatus result = createCVSTag(tagName, false, monitor, projectRoot);
 		String errExists = "Tag " + tagName + " has been already created";
 		if (!result.isOK()) {
 			boolean tagExists = false;
 			if (result.getMessage().contains(errExists)) {
 				tagExists = true;
 			}
 			if (result.isMultiStatus()) {
 				for (IStatus error : result.getChildren()) {
 					if (error.getMessage().contains(errExists)) {
 						tagExists = true;
 					}
 				}
 			}
 			if (tagExists) {
 				// prompt to force tag
 				// FIXME: move this to the appropriate place
 				/*if (promptForceTag(tagName)) {
 					if (monitor.isCanceled()) {
 						throw new OperationCanceledException();
 					}
 					result = createCVSTag(tagName, true, monitor);
 				}*/
 			}
 		}
 		return result;
 	}
 	
 	private IStatus createCVSTag(String tagName, boolean forceTag,
 			IProgressMonitor monitor, FedoraProjectRoot projectRoot) {
 		IStatus result;
 		IFile specfile = projectRoot.getSpecFile();
 		IProject proj = specfile.getProject();
 		CVSTag tag = new CVSTag(tagName, CVSTag.VERSION);
 
 		// get CVSProvider
 		CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider
 				.getProvider(proj, CVSProviderPlugin.getTypeId());
 		// get Repository Location
 		ICVSRepositoryLocation location;
 		try {
 			location = provider.getRemoteLocation();
 
 			// get CVSROOT
 			CVSWorkspaceRoot cvsRoot = provider.getCVSWorkspaceRoot();
 
 			ICVSFolder folder = cvsRoot.getLocalRoot().getFolder(
 					specfile.getParent().getName());
 
 			// Make new CVS Session
 			Session session = new Session(location, folder, true);
 			session.open(monitor, true);
 
 			TagListener listener = new TagListener();
 
 			String args[] = new String[] { "." }; //$NON-NLS-1$
 
 			LocalOption[] opts;
 			if (forceTag) {
 				opts = new LocalOption[] { Tag.FORCE_REASSIGNMENT };
 			} else {
 				opts = new LocalOption[0];
 			}
 
 			// cvs tag "tagname" "project"
 			IStatus status = Command.TAG.execute(session,
 					Command.NO_GLOBAL_OPTIONS, opts, tag, args, listener,
 					monitor);
 
 			session.close();
 			if (!status.isOK()) {
 				MultiStatus temp = new MultiStatus(status.getPlugin(), status
 						.getCode(), status.getMessage(), status.getException());
 				for (IStatus error : session.getErrors()) {
 					temp.add(error);
 				}
 				result = temp;
 			} else {
 				result = status;
 			}
 		} catch (CVSException e) {
 			result = new Status(IStatus.ERROR, CVSPlugin.PLUGIN_ID, e.getMessage());
 		}
 
 		return result;
 	}
 	
 	/**
 	 * Determine if CVS tag exists.
 	 * 
 	 * See {@link IFpProjectBits#isVcsTagged(FedoraProjectRoot, String)}
 	 */
 	@Override
 	public boolean isVcsTagged(FedoraProjectRoot fedoraProjectRoot, String tagName) {
 		if (!isInitialized()) {
 			return false; // can't do this without being initialized
 		}
 		IResource specfile = fedoraProjectRoot.getSpecFile();
 
 		CVSTag tag = null;
 		try {
 		tag = new CVSTag(tagName, CVSTag.VERSION);
 		ICVSRemoteResource remoteResource = CVSWorkspaceRoot.getRemoteResourceFor(specfile).forTag(tag);
 		if (remoteResource == null) {
 			return false;
 		}
 		} catch (CVSException e) {
 			e.printStackTrace();
 		}
 		String createdTag = null;
 		try {
 			createdTag = FedoraHandlerUtils.makeTagName(fedoraProjectRoot);
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return tag.getName().equals(createdTag);
 	}
 
 	/**
 	 * CVS needs tags because commits are not atomic in cvs.
 	 * 
 	 * @see org.fedoraproject.eclipse.packager.IFpProjectBits#needsTag()
 	 */
 	@Override
 	public boolean needsTag() {
 		return true;
 	}
 
 	@Override
 	public String getScmUrlForKoji(FedoraProjectRoot projectRoot) {
 		try {
 			return getScmUrl()+"#"+FedoraHandlerUtils.makeTagName(projectRoot);
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "";
 	}
 }
