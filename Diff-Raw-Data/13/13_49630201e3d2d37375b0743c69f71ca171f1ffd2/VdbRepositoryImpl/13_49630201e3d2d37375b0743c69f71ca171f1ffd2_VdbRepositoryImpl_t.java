 /*
  * Copyright (c) 2008-2012 Vrije Universiteit, The Netherlands All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * Neither the name of the Vrije Universiteit nor the names of its contributors
  * may be used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package interdroid.vdb.persistence.impl;
 
 import interdroid.vdb.content.avro.SchemaEvolutionValidator;
 import interdroid.vdb.persistence.api.RemoteInfo;
 import interdroid.vdb.persistence.api.VdbCheckout;
 import interdroid.vdb.persistence.api.VdbInitializer;
 import interdroid.vdb.persistence.api.VdbRepository;
 import interdroid.vdb.transport.SmartSocketsTransport;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.avro.Schema;
 import org.eclipse.jgit.dircache.DirCacheCheckout;
 import org.eclipse.jgit.errors.CheckoutConflictException;
 import org.eclipse.jgit.lib.AnyObjectId;
 import org.eclipse.jgit.lib.Config;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.ProgressMonitor;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.lib.RefDatabase;
 import org.eclipse.jgit.lib.RefUpdate;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.lib.RepositoryBuilder;
 import org.eclipse.jgit.lib.StoredConfig;
 import org.eclipse.jgit.lib.RefUpdate.Result;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevTree;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.eclipse.jgit.revwalk.filter.RevFilter;
 import org.eclipse.jgit.transport.RemoteConfig;
 import org.eclipse.jgit.transport.Transport;
 import org.eclipse.jgit.treewalk.filter.TreeFilter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The implementation of a repository in the system.
  *
  * @author nick &lt;palmer@cs.vu.nl&gt;
  *
  */
 public class VdbRepositoryImpl implements VdbRepository {
 	/**
 	 * Access to logger.
 	 */
 	private static final Logger LOG = LoggerFactory
 			.getLogger(VdbRepositoryImpl.class);
 
 	/**
 	 * The directory for the repository.
 	 */
 	private File mRepoDir;
 	/**
 	 * The name of the repository.
 	 */
 	private final String mName;
 	/**
 	 * The underlying git repository.
 	 */
 	private Repository mGitRepo;
 	/**
 	 * The initializer for the repository.
 	 */
 	private final VdbInitializer mInitializer;
 	/**
 	 * The checkouts that have been made from this repository.
 	 */
 	private final Map<String, VdbCheckoutImpl> mCheckouts
 	= new HashMap<String, VdbCheckoutImpl>();
 
 	/**
 	 * The prefix for a branch reference.
 	 */
 	private static final String BRANCH_REF_PREFIX = Constants.R_HEADS;
 	/**
 	 * The prefix for a remote reference.
 	 */
 	private static final String REMOTES_REF_PREFIX = Constants.R_REMOTES;
 
 	/**
 	 * The preferences section we store into.
 	 */
 	private static final String VDB_PREFERENCES_SECTION = "vdb";
 
 	/**
 	 * The ispublic preference for this repository.
 	 */
 	private static final String PREF_IS_PUBLIC = "ispublic";
 
 	/**
 	 * Construct a new repository and initialize it.
 	 * @param name the name of the repo
 	 * @param repoDir the directory for the repo
 	 * @param initializer the initializer for the repo
 	 * @throws IOException if reading or writing fails
 	 */
 	public VdbRepositoryImpl(final String name, final File repoDir,
 			final VdbInitializer initializer)
 					throws IOException {
 		mRepoDir = repoDir;
 		mName = name;
 		mInitializer = initializer;
 
 		initializeRepository();
 	}
 
 	/**
 	 * @return the directory for this repository.
 	 */
 	public final File getRepositoryDir() {
 		return mRepoDir;
 	}
 
 	/**
 	 * @return the underlying git repository for this repository.
 	 */
 	public final Repository getGitRepository() {
 		if (mGitRepo == null) {
 			mGitRepo = getGitRepository(null);
 		}
 		return mGitRepo;
 	}
 
 	/**
 	 * This gives an underlying git repository where the working directory
 	 * has been set to the given directory.
 	 *
 	 * @param workingDir the working directory for the repository
 	 * @return the underlying git repository with the given dir as work tree
 	 */
 	public final Repository getGitRepository(final String workingDir) {
 		RepositoryBuilder builder = new RepositoryBuilder();
 		builder.setWorkTree(mRepoDir);
 		builder.setGitDir(new File(mRepoDir, ".git"));
 		if (workingDir == null) {
 			builder.setBare();
 		} else {
 			builder.setWorkTree(new File(mRepoDir, workingDir));
 		}
 		Repository repo;
 		try {
 			repo = builder.build();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		return repo;
 	}
 
 	/**
 	 * Initializes this repository.
 	 */
 	private void initializeRepository() {
 		LOG.debug("Initializing Repository");
 		Repository repo;
 		repo = getGitRepository();
 
 		File gitDir = new File(mRepoDir, ".git");
 		if (!gitDir.exists()) {
 			try {
 				repo.create();
 				if (LOG.isDebugEnabled()) {
 					LOG.debug("Creating master.");
 				}
 				VdbCheckoutImpl master =
 						VdbCheckoutImpl.createMaster(this, mInitializer);
 				mCheckouts.put(Constants.MASTER, master);
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	/**
 	 * Checkout a reference.
 	 * @param subdir the directory to checkout in
 	 * @param reference the reference to checkout
 	 * @return the checkout directory as a file.
 	 * @throws IOException if reading or writing fails
 	 */
 	public final File checkoutReference(final String subdir,
 			final String reference) throws IOException {
 		LOG.debug("getting checkout: {} {}", subdir, reference);
 		Repository repo = getGitRepository(subdir);
 		ObjectId headId = repo.resolve(reference);
 		LOG.debug("Head id is: {}", headId);
 
 		File checkoutDir = null;
 
 		if (headId != null) {
 			checkoutDir = new File(mRepoDir, subdir);
 
 			if (!checkoutDir.isDirectory()) {
 				if (!checkoutDir.mkdir() || !checkoutDir.isDirectory()) {
 					throw new IOException("Could not create checkout directory: "
 							+ subdir);
 				}
 			} else {
 				// Assume already checked out.
 				return checkoutDir;
 			}
 
 			RevWalk revWalk = new RevWalk(repo);
 			RevCommit headCommit = revWalk.parseCommit(headId);
 			RevTree headTree = headCommit.getTree();
 			DirCacheCheckout dco = new DirCacheCheckout(repo, repo.lockDirCache(),
 					headTree);
 			dco.setFailOnConflict(true);
 			try {
 				dco.checkout();
 			} catch (CheckoutConflictException e) {
 				LOG.error("Conflict!", e);
 				throw e;
 			}
 
 		} else {
 			throw new RuntimeException("No such reference.");
 		}
 		return checkoutDir;
 	}
 
 	/**
 	 * Checkout the named branch.
 	 * @param branchName the name of the desired branch.
 	 * @return the directory with the checkout
 	 * @throws IOException if reading or writing fails
 	 */
 	public final File checkoutBranch(final String branchName)
 			throws IOException {
 		return checkoutReference(branchName, BRANCH_REF_PREFIX + branchName);
 	}
 
 	/**
 	 * Checkout a read only commit.
 	 * @param sha1 the sha1 of the commit to check out.
 	 * @return the checkout directory
 	 * @throws IOException if reading or writing fails
 	 */
 	public final File checkoutCommit(final String sha1) throws IOException {
 		return checkoutReference(sha1, sha1);
 	}
 
 	@Override
 	public final void createBranch(final String branchName,
 			final String baseRef) throws IOException {
 		ObjectId oId = getGitRepository(null).resolve(baseRef);
 		createBranchFromId(branchName, oId);
 	}
 
 	/**
 	 * Creates a branch from a given commit.
 	 * @param branchName the name of the new branch
 	 * @param oId the id of the commit to branch from.
 	 * @throws IOException if reading or writing fails
 	 */
 	private void createBranchFromId(final String branchName, final ObjectId oId)
 			throws IOException {
 		Ref ref = getGitRepository().getRef(BRANCH_REF_PREFIX + branchName);
 		if (ref != null && ref.getObjectId() != null) {
 			throw new IOException("Branch already exists, not overwriting.");
 		}
 		final RefUpdate ru = getGitRepository().updateRef(
 				BRANCH_REF_PREFIX + branchName);
 		ru.setNewObjectId(oId);
 		ru.disableRefLog();
 		if (ru.forceUpdate() == RefUpdate.Result.LOCK_FAILURE) {
 			throw new IOException("Error updating reference " + branchName);
 		}
 		try {
 			checkoutBranch(branchName);
 		} catch (IOException e) {
 			RefUpdate undoRu = getGitRepository().updateRef(
 					BRANCH_REF_PREFIX + branchName);
 			undoRu.delete();
 
 			throw e;
 		}
 	}
 
 	@Override
 	public final Set<String> listBranches() throws IOException {
 		Map<String, Ref> branches =
 				getGitRepository().getRefDatabase().getRefs(BRANCH_REF_PREFIX);
 		return branches.keySet();
 	}
 
 	/**
 	 * Lists the remotes for this repository.
 	 * @return a list of remote names
 	 * @throws IOException if reading or writing fails
 	 */
 	public final Set<String> listRemotes() throws IOException {
 		Config rc = getGitRepository().getConfig();
 		return rc.getSubsections(RemoteInfo.SECTION);
 	}
 
 	@Override
 	public final Set<String> listRemoteBranches() throws IOException {
 		Map<String, Ref> branches =
 				getGitRepository().getRefDatabase().getRefs(REMOTES_REF_PREFIX);
 		return branches.keySet();
 	}
 
 	@Override
 	public final RevWalk enumerateCommits(final String... leaves)
 			throws IOException {
 		RevWalk rw = new RevWalk(getGitRepository());
 		for (String leaf : leaves) {
 			Ref r = getGitRepository().getRef(leaf);
 			if (r == null  || r.getObjectId() == null) {
 				// TODO: (emilian) proper exception
 				throw new IOException("Invalid leaf reference.");
 			}
 			RevCommit commit = rw.parseCommit(r.getObjectId());
 			rw.markStart(commit);
 		}
 		return rw;
 	}
 
 	/**
 	 * Returns the commit which is the base for a merge of 2 or more commits.
 	 * @param commitIds the ids of the commits being merged
 	 * @return the commit which is the common base
 	 * @throws IOException if reading or writing fails.
 	 */
 	public final RevCommit getMergeBase(final AnyObjectId... commitIds)
 			throws IOException {
 		if (commitIds.length < 2) {
 			throw new IllegalArgumentException(
 					"Need to specify at least 2 commits.");
 		}
 
 		RevWalk walk = new RevWalk(getGitRepository());
 		walk.setRevFilter(RevFilter.MERGE_BASE);
 		walk.setTreeFilter(TreeFilter.ALL);
 
 		for (AnyObjectId commitId : commitIds) {
 			walk.markStart(walk.parseCommit(commitId));
 		}
 
 		RevCommit baseCommit = walk.next();
 		if (baseCommit == null) {
 			throw new IllegalStateException("Could not find merge base.");
 		}
 		return baseCommit;
 	}
 
 	@Override
 	public final synchronized VdbCheckout getBranch(final String branchName)
 			throws IOException {
 		if (!mCheckouts.containsKey(branchName)) {
 			// statements below throw if not successful
 			checkoutBranch(branchName);
 
 			VdbCheckoutImpl branch = new VdbCheckoutImpl(this, branchName);
 			mCheckouts.put(branchName, branch);
 		}
 		return mCheckouts.get(branchName);
 	}
 
 	@Override
 	public final VdbCheckout getCommit(final String sha1) throws IOException {
 		if (!mCheckouts.containsKey(sha1)) {
 			checkoutCommit(sha1);
 
 			VdbCheckoutImpl checkout = new VdbCheckoutImpl(this, sha1, true);
 			mCheckouts.put(sha1, checkout);
 		}
 		return mCheckouts.get(sha1);
 	}
 
 	@Override
 	public final String getName() {
 		return mName;
 	}
 
 	/**
 	 * Removes the named checkout.
 	 * @param checkoutName the name of the checkout to remove.
 	 */
 	/* package */ final void releaseCheckout(final String checkoutName) {
 		mCheckouts.remove(checkoutName);
 	}
 
 	@Override
 	public final synchronized void deleteBranch(final String branchName)
 			throws IOException {
 		VdbCheckout checkout = getBranch(branchName);
 		checkout.delete();
 		mCheckouts.remove(branchName);
 
 		RefUpdate update = getGitRepository().getRefDatabase()
 				.newUpdate(BRANCH_REF_PREFIX + branchName, true /* detach */);
 		update.setForceUpdate(true);
 		Result res = update.delete();
 		if (res != Result.FORCED) {
 			throw new RuntimeException("Could not delete, instead was " + res);
 		}
 	}
 
 	@Override
 	public final synchronized void deleteRemote(final String remoteName)
 			throws IOException {
 		StoredConfig rc = getGitRepository().getConfig();
 		rc.unsetSection(RemoteInfo.SECTION, remoteName);
 		rc.save();
 
 		// now remove all references
 		RefDatabase refDb = getGitRepository().getRefDatabase();
 		String refsPrefix = REMOTES_REF_PREFIX + remoteName + "/";
 		Map<String, Ref> branches = refDb.getRefs(refsPrefix);
 		for (String name : branches.keySet()) {
 			String fullName = refsPrefix + name;
 			RefUpdate update = refDb.newUpdate(fullName, true /* detach */);
 			update.delete();
 		}
 	}
 
 	@Override
 	public final VdbCheckout getRemoteBranch(final String remoteBranchName)
 			throws IOException {
 		getGitRepository().scanForRepoChanges();
 		Ref ref = getGitRepository().getRefDatabase().getRefs(
 				Constants.R_REMOTES)
 				.get(remoteBranchName);
 		ObjectId commitId = ref.getObjectId();
 		if (commitId == null) {
 			throw new IllegalStateException("Invalid remote branch.");
 		}
 		return getCommit(commitId.name());
 	}
 
 	@Override
 	public final synchronized RemoteInfo getRemoteInfo(final String remoteName)
 			throws IOException {
 		if (!listRemotes().contains(remoteName)) {
 			return null;
 		}
 		RemoteInfo info = new RemoteInfo(remoteName);
 		try {
 			info.load(getGitRepository().getConfig());
 		} catch (URISyntaxException e) {
 			throw new RuntimeException(e);
 		}
 		return info;
 	}
 
 	@Override
 	public final void saveRemote(final RemoteInfo info) throws IOException {
 		StoredConfig rc = getGitRepository().getConfig();
 		try {
 			info.save(rc);
 		} catch (URISyntaxException e) {
 			throw new RuntimeException(e);
 		}
 		rc.save();
 	}
 
 	/**
 	 * Builds a transport for communicating with the specified remote.
 	 * @param remoteName the name of the remote to communicate with
 	 * @return a Transport for sending data to the remote.
 	 */
 	public final Transport buildConnection(final String remoteName) {
 		// this will throw if remoteName is invalid
 		try {
 			if (getRemoteInfo(remoteName) == null) {
 				throw new RuntimeException("The remote is not configured.");
 			}
 			RemoteConfig cfg = new RemoteConfig(
 					getGitRepository().getConfig(), remoteName);
 			Transport.register(SmartSocketsTransport.PROTO);
 			return Transport.open(getGitRepository(), cfg);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (URISyntaxException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public final void pullFromRemote(final String remoteName,
 			final ProgressMonitor monitor)  throws IOException {
 		Transport connection = null;
 		try {
 			connection = buildConnection(remoteName);
 			// TODO: (emilian): need to watch for semantics depending on type
 			connection.setRemoveDeletedRefs(true);
 			connection.fetch(monitor, null);
 		} finally {
 			if (connection != null) {
 				connection.close();
 			}
 		}
 	}
 
 	@Override
 	public final void pushToRemote(final String remoteName,
 			final ProgressMonitor monitor) throws IOException {
 		Transport connection = null;
 		try {
 			connection = buildConnection(remoteName);
 			// TODO: (emilian) need to watch semantics for MERGE_POINTs and HUBs
 			connection.setRemoveDeletedRefs(true);
 			connection.push(monitor, null);
 		} finally {
 			if (connection != null) {
 				connection.close();
 			}
 		}
 			}
 
 	@Override
 	public final void pushToRemoteExplicit(final String remoteName,
 			final String localBranch, final String remoteBranch)
 					throws IOException {
 
 		// TODO: (emilian) implement me
 		throw new RuntimeException("Not implemented");
 	}
 
 	@Override
 	public final boolean isPublic() {
 		return mGitRepo.getConfig().getBoolean(
 				VDB_PREFERENCES_SECTION, PREF_IS_PUBLIC, false);
 	}
 
 	@Override
 	public final void setIsPublic(final boolean isChecked) throws IOException {
 		StoredConfig config = mGitRepo.getConfig();
 		config.setBoolean(VDB_PREFERENCES_SECTION, null,
 				PREF_IS_PUBLIC, isChecked);
 		config.save();
 	}
 
 	@Override
 	public void updateDatabase(String branchName, Schema newSchema) throws IOException {
 		VdbCheckout branch = getBranch(branchName);
 		String old = branch.getSchema();
 
 		// Do we need to run an update?
 		if (!old.equals(newSchema.toString())) {
 			// Need to check projection.
 			Schema oldSchema = Schema.parse(old);
 			SchemaEvolutionValidator validator = new SchemaEvolutionValidator();
 			if (validator.validateProjection(newSchema, oldSchema)) {
 				LOG.debug("Schema projection validated.");
 
 				branch.updateDatabase(newSchema);
 			} else {
 				throw new IOException("Schema does not project.");
 			}
 		} else {
 			LOG.debug("Schema's match. No need to update.");
 		}
 	}
 
 	/**
 	 * Close any open checkouts.
 	 */
 	public void close() {
 		for (Entry<String, VdbCheckoutImpl> checkout : mCheckouts.entrySet()) {
 			checkout.getValue().close();
 		}
 	}
 }
