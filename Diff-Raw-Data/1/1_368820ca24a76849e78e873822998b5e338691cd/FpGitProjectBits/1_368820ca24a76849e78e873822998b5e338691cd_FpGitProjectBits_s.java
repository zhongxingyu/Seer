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
 package org.fedoraproject.eclipse.packager.git;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.egit.core.project.RepositoryMapping;
 import org.eclipse.jgit.errors.NotSupportedException;
 import org.eclipse.jgit.errors.TransportException;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.NullProgressMonitor;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.transport.FetchResult;
 import org.eclipse.jgit.transport.RefSpec;
 import org.eclipse.jgit.transport.Transport;
 import org.eclipse.jgit.transport.URIish;
 import org.fedoraproject.eclipse.packager.FedoraProjectRoot;
 import org.fedoraproject.eclipse.packager.IFpProjectBits;
 import org.fedoraproject.eclipse.packager.handlers.FedoraHandlerUtils;
 
 /**
  * Git specific project bits (branches management and such).
  * Implementation of
  * org.fedoraproject.eclipse.packager.vcsContribution
  * extension point.
  * 
  * @author Red Hat Inc.
  *
  */
 public class FpGitProjectBits implements IFpProjectBits {
 	
 	private IResource project; // The underlying project
 	private HashMap<String, String> branches; // All branches
 	private Repository gitRepository; // The Git repository for this project
 	private boolean initialized = false; // keep track if instance is initialized
 	
 	// String regexp pattern used for branch mapping this should basically be the
 	// same pattern as fedpkg uses. ATM this pattern is:
 	// BRANCHFILTER = 'f\d\d\/master|master|el\d\/master|olpc\d\/master'
 	private final Pattern BRANCH_PATTERN = Pattern
 			.compile("(?:origin/)?(fc?)(\\d\\d?)/master|(?:origin/)?(master)|(?:origin/)?(el)(\\d)/master|(?:origin/)?(olpc)(\\d)/master" //$NON-NLS-1$
 			);
 	
 	/**
 	 * See {@link IFpProjectBits#getBranchName(String)}
 	 */
 	@Override
 	public String getBranchName(String branchName) {
 		if (!isInitialized()) {
 			return null;
 		}
 		return this.branches.get(branchName);
 	}
 
 	/**
 	 * Parse current branch from active local branch.
 	 * 
 	 * See {@link IFpProjectBits#getCurrentBranchName()}
 	 */
 	@Override
 	public String getCurrentBranchName() {
 		if (!isInitialized()) {
 			return null;
 		}
 		String currentBranch = null;
 		try {
 			// make sure it's a named branch
 			if (!isNamedBranch(this.gitRepository.getFullBranch())) {
 				return null; // unknown branch!
 			}
 			// get the current head target
 			currentBranch = this.gitRepository.getBranch();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return mapBranchName(currentBranch);
 	}
 
 	/**
 	 * See {@link IFpProjectBits#getScmUrl()}
 	 */
 	@Override
 	public String getScmUrl() {
 		if (!isInitialized()) {
 			return null;
 		}
 		String username = FedoraHandlerUtils.getUsernameFromCert();
 		String packageName = this.project.getProject().getName();
 		if (username.equals("anonymous")) { //$NON-NLS-1$
 			return "git://pkgs.fedoraproject.org/" + packageName + ".git"; //$NON-NLS-1$ //$NON-NLS-2$
 		} else {
 			return "ssh://" + username + "@pkgs.fedoraproject.org/" //$NON-NLS-1$ //$NON-NLS-2$
 					+ packageName + ".git"; //$NON-NLS-1$
 		}
 	}
 	
 	/**
 	 * Git should always return anonymous checkout with git protocol for koji.
 	 * 
 	 * @see org.fedoraproject.eclipse.packager.IFpProjectBits#getScmUrlForKoji(FedoraProjectRoot)
 	 */
 	@Override
 	public String getScmUrlForKoji(FedoraProjectRoot projectRoot) {
 		if (!isInitialized()) {
 			return null;
 		}
 		String packageName = this.project.getProject().getName();
 		return "git://pkgs.fedoraproject.org/" + packageName + ".git?#" + getCommitHash(); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 	
 	/**
 	 * Get the SHA1 representing the current branch.
 	 * 
 	 * @return The SHA1 as hex in String form.
 	 */
 	private String getCommitHash() {
 		String commitHash = null;
 		try {
 			String currentBranchRefString = gitRepository.getFullBranch();
 			Ref ref = gitRepository.getRef(currentBranchRefString);
 			commitHash = ref.getObjectId().getName();
 		} catch (IOException ioException) {
 			ioException.printStackTrace();
 		}
 		return commitHash;
 	}
 
 	/**
 	 * Parse available branch names from Git remote branches.
 	 * 
 	 * @return
 	 */
 	private HashMap<String, String> getBranches() {
 		HashMap<String, String> branches = new HashMap<String, String>();
 		try {
 			Map<String, Ref> remotes = gitRepository.getRefDatabase().getRefs(
 					Constants.R_REMOTES);
 			Set<String> keyset = remotes.keySet();
 			String branch;
 			for (String key : keyset) {
 				// use shortenRefName() to get rid of refs/*/ prefix
 				branch = gitRepository.shortenRefName(remotes.get(key).getName());
 				branch = mapBranchName(branch); // do the branch name mapping
 				if (branch != null) {
 					branches.put(branch, branch);
 				}
 			}
 		} catch (IOException ioException) {
 			ioException.printStackTrace();
 		}
 		return branches;
 	}
 
 	/**
 	 * Do instance specific initialization.
 	 * 
 	 * See {@link IFpProjectBits#initialize(FedoraProjectRoot)}
 	 */
 	@Override
 	public void initialize(FedoraProjectRoot fedoraprojectRoot) {
 		this.project = fedoraprojectRoot.getProject();
 		// now set Git Repository object
 		this.gitRepository = getGitRepository();
 		this.branches = getBranches();
 		this.initialized = true;
 	}
 	
 	/**
 	 * Determine if instance has been properly initialized
 	 */
 	private boolean isInitialized() {
 		return this.initialized;
 	}
 
 	/**
 	 * Determine distribution qualifier. This is VCS specific because
 	 * branch determination is VCS specific.
 	 * 
 	 * See {@link IFpProjectBits#getDist()}
 	 */
 	@Override
 	public String getDist() {
 		String currBranch = getCurrentBranchName();
 		if (currBranch.startsWith("F-") || currBranch.startsWith("FC-")) { //$NON-NLS-1$ //$NON-NLS-2$
 			return ".fc" + getDistVal(); //$NON-NLS-1$
 		} else if (currBranch.startsWith("EL-")) { //$NON-NLS-1$
 			return ".el" + getDistVal(); //$NON-NLS-1$
 		} else if (currBranch.startsWith("OLPC-")) {  //$NON-NLS-1$
 			return ".olpc" + getDistVal(); //$NON-NLS-1$
 		} else if (currBranch.equals("devel")) { //$NON-NLS-1$
 			return ".fc" + determineNextReleaseNumber(); //$NON-NLS-1$
 		}
 		return null;
 	}
 	
 	/**
 	 * See {@link IFpProjectBits#getDistVal()}
 	 */
 	@Override
 	public String getDistVal() {
 		String currBranch = getCurrentBranchName();
 		if (currBranch.equals("devel")) { //$NON-NLS-1$
 			return determineNextReleaseNumber();
 		}
 		return currBranch.split("-")[1]; //$NON-NLS-1$
 	}
 
 	/**
 	 * See {@link IFpProjectBits#getDistVariable()}
 	 */
 	@Override
 	public String getDistVariable() {
 		String currBranch = getCurrentBranchName();
 		if (currBranch.startsWith("F-") || currBranch.startsWith("FC-")) { //$NON-NLS-1$ //$NON-NLS-2$
 			return "fedora"; //$NON-NLS-1$" +
 		} else if (currBranch.startsWith("EL-")) { //$NON-NLS-1$
 			return "rhel"; //$NON-NLS-1$
 		} else if (currBranch.startsWith("OLPC-")) {  //$NON-NLS-1$
 			return "olpc"; //$NON-NLS-1$
 		} else if (currBranch.equals("devel")) { //$NON-NLS-1$
 			return "fedora"; //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	/**
 	 * See {@link IFpProjectBits#getTarget()}
 	 */
 	@Override
 	public String getTarget() {
 		String currBranch = getCurrentBranchName();
 		if (currBranch.startsWith("F-") || currBranch.startsWith("FC-")) { //$NON-NLS-1$ //$NON-NLS-2$
 			return "dist-f" + getDistVal() + "-updates-candidate"; //$NON-NLS-1$" //$NON-NLS-2$
 		} else if (currBranch.startsWith("EL-")) { //$NON-NLS-1$
 			return "dist-" + getDistVal() + "E-epel-testing-candidate"; //$NON-NLS-1$ //$NON-NLS-2$
 		} else if (currBranch.startsWith("OLPC-")) {  //$NON-NLS-1$
 			return "dist-olpc" + getDistVal(); //$NON-NLS-1$
 		} else if (currBranch.equals("devel")) { //$NON-NLS-1$
 			return "dist-rawhide"; //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	/**
 	 * Maps branch names to the internal format used by all IFpProjectBits
 	 * implementations. For example <code>mapBranchName("f8")</code> would
 	 * return <code>"F-8"</code> and <code>mapBranchName("master")</code> would
 	 * return <code>"devel"</code>.
 	 * 
 	 * @param from
 	 *            The original raw branch name with "refs/something"
 	 *            prefixes omitted.
 	 * @return The mapped branch name.
 	 */
 	private String mapBranchName(String from) {
 		String prefix, version;
 		Matcher branchMatcher = BRANCH_PATTERN.matcher(from);
 		if (!branchMatcher.matches()) {
 			// This should never happen. Maybe something wrong with the regular
 			// expression?
 			return null;
 		}
 		for (int i = 1; i < branchMatcher.groupCount(); i++) {
 			prefix = branchMatcher.group(i); // null if group didn't match at all
 			version = branchMatcher.group(i+1);
 			if (version == null && prefix != null && prefix.equals(Constants.MASTER)) {
 				// matched master
 				return "devel"; //$NON-NLS-1$
 			} else if (version != null && prefix != null) {
 				// F, EPEL, OLPC matches
 				return prefix.toUpperCase() + "-" + version; //$NON-NLS-1$
 			}
 		}
 		// something's fishy
 		return null;
 	}
 	
 	/**
 	 * Returns true if given branch name is NOT an ObjectId in string format.
 	 * I.e. if branchName has been created by doing repo.getBranch(), it would
 	 * return SHA1 Strings for remote branches. We don't want that.
 	 * 
 	 * @param branchName
 	 * @return
 	 */
 	private boolean isNamedBranch(String branchName) {
 		if (branchName.startsWith(Constants.R_HEADS)
 				|| branchName.startsWith(Constants.R_TAGS)
 				|| branchName.startsWith(Constants.R_REMOTES)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * See {@link IFpProjectBits#updateVCS(FedoraProjectRoot, IProgressMonitor)}
 	 */
 	@Override
 	public IStatus updateVCS(FedoraProjectRoot projectRoot,
 			IProgressMonitor monitor) {
 		// FIXME: Not working just, yet. Use projectRoot and monitor!.
 //		return performPull();
 		// Return OK status to not see NPEs
 		return Status.OK_STATUS;
 	}
 	
 	/**
 	 * Pull "sources" and ".gitignore".
 	 * 
 	 * TODO: Clean this up a little
 	 */
 	private IStatus performPull() {
 		IStatus errorStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Fail"); //$NON-NLS-1$
 		if (!isInitialized()) {
 			return errorStatus;
 		}
 		final Transport transport;
 		URIish uri = null;
 		List<RefSpec> mRefSpecs = new ArrayList<RefSpec>();
 		final List<RefSpec> refSpecs;
 		try {
 			final RefSpec singleRefSpec = new RefSpec(this.gitRepository.getFullBranch() + ":" + Constants.R_REMOTES + "origin/" + this.gitRepository.getBranch());  //$NON-NLS-1$//$NON-NLS-2$
 			mRefSpecs.add(singleRefSpec);
 			uri = new URIish(getScmUrl());
 			refSpecs = Collections.unmodifiableList(mRefSpecs);
 			transport = Transport.open(this.gitRepository, uri);
 		} catch (URISyntaxException e1) {
 			e1.printStackTrace();
 			return errorStatus;
 		} catch (final NotSupportedException e) {
 			e.printStackTrace();
 			return errorStatus;
 		}	catch (IOException e1) {
 			e1.printStackTrace();
 			return errorStatus;
 		} 
 		
 		Job fetchJob = new Job(Messages.fpGitProjectBits_fetchJobName) {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				FetchResult result = null;
 				try {
 					result = transport.fetch(NullProgressMonitor.INSTANCE, refSpecs);
 				} catch (NotSupportedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (TransportException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				String resultMsg = result.getMessages();
 				return new Status(IStatus.INFO, Activator.PLUGIN_ID, resultMsg);
 			}
 			
 		};
 		fetchJob.setUser(true);
 		fetchJob.schedule();
 		return fetchJob.getResult(); // TODO: Do merging!
 	}
 	
 	/**
 	 * Get the JGit repository.
 	 */
 	private Repository getGitRepository() {
 		RepositoryMapping  repoMapping = RepositoryMapping.getMapping(project);
 		return repoMapping.getRepository();
 	}
 	
 	/**
 	 * Determine what the next release number (in terms of the
 	 * distribution) will be.
 	 * 
 	 * @return The next release number in String representation
 	 */
 	private String determineNextReleaseNumber() {
 		if (!isInitialized()) {
 			return null;
 		}
 		// Try to guess the next release number based on existing branches
 		Set<String> keySet = this.branches.keySet();
 		String branchName;
 		int maxRelease = -1;
 		for (String key : keySet) {
 			branchName = this.branches.get(key);
 			if (branchName.startsWith("F-") || branchName.startsWith("FC-")) { //$NON-NLS-1$ //$NON-NLS-2$
 				// fedora
 				maxRelease = Math.max(maxRelease, Integer.parseInt(branchName.substring("F-".length()))); //$NON-NLS-1$
 			} else if (branchName.startsWith("EL-")) { //$NON-NLS-1$
 				// EPEL
 				maxRelease = Math.max(maxRelease, Integer.parseInt(branchName.substring("EL-".length()))); //$NON-NLS-1$
 			} else if (branchName.startsWith("OLPC-")) {  //$NON-NLS-1$
 				// OLPC
 				maxRelease = Math.max(maxRelease, Integer.parseInt(branchName.substring("OLPC-".length()))); //$NON-NLS-1$
 			}
 			// ignore
 		}
 		if (maxRelease == -1) {
 			// most likely a new package. ATM this is F-15
 			return "15"; //$NON-NLS-1$
 		} else {
 			return Integer.toString(maxRelease + 1);
 		}
 	}
 	
 	@Override
 	public IStatus ignoreResource(IResource resourceToIgnore) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * Determine if Git tag exists.
 	 * 
 	 * See {@link IFpProjectBits#isVcsTagged(FedoraProjectRoot, String)}
 	 */
 	@Override
 	public boolean isVcsTagged(FedoraProjectRoot fedoraProjectRoot, String tag) {
 		if (!isInitialized()) {
 			return false; // If we are not initialized we can't go any further!
 		}
 		// Look at tags and see if we can find the tag in question.
 		Map<String, Ref> remotes = this.gitRepository.getTags();
 		if (remotes != null) {
 			Set<String> keyset = remotes.keySet();
 			String currentTag;
 			for (String key : keyset) {
 				// use shortenRefName() to get rid of refs/*/ prefix
 				currentTag = this.gitRepository.shortenRefName(remotes.get(key)
 						.getName());
 				if (tag.equals(currentTag)) {
 					return true; // tag found
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Create new Git tag.
 	 * 
 	 * See {@link IFpProjectBits#tagVcs(FedoraProjectRoot, IProgressMonitor)}
 	 */
 	@Override
 	public IStatus tagVcs(FedoraProjectRoot projectRoot,
 			IProgressMonitor monitor) {
 		if (!isInitialized()) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Git tag error. Not initialized!");
 		}
 		//TODO fix to use latest jgit which has removed Tag object.
 //		Tag newTag = new Tag(this.gitRepository);
 //		try {
 //			newTag.setTag(FedoraHandlerUtils.makeTagName(projectRoot));
 //			newTag.setMessage("Automatic Eclipse Fedorapackager tag");
 //			// use FAS username as identity which did the tagging
 //			newTag.setAuthor(new PersonIdent(FedoraHandlerUtils
 //					.getUsernameFromCert()));
 //			newTag.setObjId(this.gitRepository.resolve(this.gitRepository
 //					.getFullBranch()));
 //			TagOperation top = new TagOperation(this.gitRepository, newTag,
 //					false);
 //			top.execute(monitor);
 			return new Status(IStatus.OK, Activator.PLUGIN_ID, "Tag succeeded!");
 //		} catch (Exception e) {
 //			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
 //		}
 		// TODO: Extend and do a commit & push!
 	}
 
 	/**
 	 * Fedora git doesn't need to tag because commit hashes are used.
 	 * 
 	 * @see org.fedoraproject.eclipse.packager.IFpProjectBits#needsTag()
 	 */
 	@Override
 	public boolean needsTag() {
 		return false;
 	}
 
 }
