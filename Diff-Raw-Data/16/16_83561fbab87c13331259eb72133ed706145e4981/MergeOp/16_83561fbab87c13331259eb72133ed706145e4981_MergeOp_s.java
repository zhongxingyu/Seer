 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.api.porcelain;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.geogit.api.AbstractGeoGitOp;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Ref;
 import org.geogit.api.RevCommit;
 import org.geogit.api.SymRef;
 import org.geogit.api.plumbing.DiffTree;
 import org.geogit.api.plumbing.FindCommonAncestor;
 import org.geogit.api.plumbing.RefParse;
 import org.geogit.api.plumbing.ResolveBranchId;
 import org.geogit.api.plumbing.UpdateRef;
 import org.geogit.api.plumbing.UpdateSymRef;
 import org.geogit.api.plumbing.diff.ConflictsReport;
 import org.geogit.api.plumbing.diff.DiffEntry;
 import org.geogit.api.plumbing.merge.CheckMergeConflictsOp;
 import org.geogit.api.plumbing.merge.Conflict;
 import org.geogit.api.plumbing.merge.ConflictsWriteOp;
 import org.geogit.api.plumbing.merge.MergeConflictsException;
 import org.geogit.api.plumbing.merge.ReportMergeConflictsOp;
 import org.geogit.api.plumbing.merge.SaveMergeCommitMessageOp;
 import org.geogit.repository.Repository;
 import org.geotools.util.SubProgressListener;
 import org.opengis.util.ProgressListener;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Supplier;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 
 /**
  * 
  * Merge two or more histories together.
  * 
  */
 public class MergeOp extends AbstractGeoGitOp<RevCommit> {
 
     private List<ObjectId> commits = new ArrayList<ObjectId>();;
 
     private String message = null;
 
     private Repository repository;
 
     private boolean ours;
 
     private boolean theirs;
 
     private boolean noCommit;
 
     /**
      * Constructs a new {@code MergeOp} using the specified parameters.
      * 
      * @param repository the repository to use
      */
     @Inject
     public MergeOp(Repository repository) {
         this.repository = repository;
     }
 
     /**
      * @param message the message for the merge commit
      * @return {@code this}
      */
     public MergeOp setMessage(final String message) {
         this.message = message;
         return this;
     }
 
     /**
      * Adds a commit whose history should be merged.
      * 
      * @param onto a supplier for the commit id
      * @return {@code this}
      */
     public MergeOp addCommit(final Supplier<ObjectId> commit) {
         Preconditions.checkNotNull(commit);
 
         this.commits.add(commit.get());
         return this;
     }
 
     /**
      * 
      * @param ours true if the "ours" strategy should be used
      * @return {@code this}
      */
     public MergeOp setOurs(boolean ours) {
         this.ours = ours;
         return this;
     }
 
     /**
      * 
      * @param ours true if the "theirs" strategy should be used
      * @return {@code this}
      */
     public MergeOp setTheirs(boolean theirs) {
         this.theirs = theirs;
         return this;
     }
 
     /**
      * 
      * @param ours true if no commit should be made after the merge, leaving just the index with the
      *        merge result
      * @return {@code this}
      */
     public MergeOp setNoCommit(boolean noCommit) {
         this.noCommit = noCommit;
         return this;
     }
 
     /**
      * Executes the merge operation.
      * 
      * @return always {@code true}
      */
     @Override
     public RevCommit call() throws RuntimeException {
 
         Preconditions.checkArgument(commits.size() > 0, "No commits specified for merge.");
         Preconditions.checkArgument(!(ours && theirs), "Cannot use both --ours and --theirs.");
 
         final Optional<Ref> currHead = command(RefParse.class).setName(Ref.HEAD).call();
         Preconditions.checkState(currHead.isPresent(), "Repository has no HEAD, can't rebase.");
         Preconditions.checkState(currHead.get() instanceof SymRef,
                 "Can't rebase from detached HEAD");
         SymRef headRef = (SymRef) currHead.get();
         final String currentBranch = headRef.getTarget();
 
         getProgressListener().started();
 
         boolean fastForward = true;
         boolean changed = false;
 
         boolean hasConflicts;
         List<RevCommit> revCommits = Lists.newArrayList();
         if (!ObjectId.NULL.equals(headRef.getObjectId())) {
             revCommits.add(repository.getCommit(headRef.getObjectId()));
         }
         for (ObjectId commitId : commits) {
             revCommits.add(repository.getCommit(commitId));
         }
         hasConflicts = command(CheckMergeConflictsOp.class).setCommits(revCommits).call()
                 .booleanValue();
 
         if (hasConflicts && !theirs) {
             if (commits.size() > 1) {
                 throw new CannotMergeException(
                         "Conflicted merge.\nCannot merge more than two commits when conflicts exist");
             } else {
                 RevCommit headCommit = repository.getCommit(headRef.getObjectId());
                 ObjectId commitId = commits.get(0);
                 Preconditions.checkArgument(!ObjectId.NULL.equals(commitId),
                         "Cannot merge a NULL commit.");
                 Preconditions.checkArgument(repository.commitExists(commitId),
                         "Not a valid commit: " + commitId.toString());
 
                 final RevCommit targetCommit = repository.getCommit(commitId);
                 ConflictsReport conflicts = command(ReportMergeConflictsOp.class)
                         .setMergeIntoCommit(headCommit).setToMergeCommit(targetCommit).call();
 
                 List<DiffEntry> unconflicting = conflicts.getUnconflicted();
                 if (!unconflicting.isEmpty()) {
                     getIndex().stage(getProgressListener(), unconflicting.iterator(), 0);
                     changed = true;
                     fastForward = false;
                 }
                 getWorkTree().updateWorkHead(getIndex().getTree().getId());
 
                 if (!ours) {
                     // In case we use the "ours" strategy, we do nothing. We ignore conflicting
                     // changes and leave the current elements
 
                     command(UpdateRef.class).setName(Ref.MERGE_HEAD).setNewValue(commitId).call();
                     command(UpdateRef.class).setName(Ref.ORIG_HEAD).setNewValue(headCommit.getId())
                             .call();
                     command(ConflictsWriteOp.class).setConflicts(conflicts.getConflicts()).call();
 
                     StringBuilder msg = new StringBuilder();
                     Optional<Ref> ref = command(ResolveBranchId.class).setObjectId(commitId).call();
                     if (ref.isPresent()) {
                         msg.append("Merge branch " + ref.get().getName());
                     } else {
                         msg.append("Merge commit '" + commitId.toString() + "'. ");
                     }
                     msg.append("\n\nConflicts:\n");
                     for (Conflict conflict : conflicts.getConflicts()) {
                         msg.append("\t" + conflict.getPath() + "\n");
                     }
 
                     command(SaveMergeCommitMessageOp.class).setMessage(msg.toString()).call();
 
                     StringBuilder sb = new StringBuilder();
                     for (Conflict conflict : conflicts.getConflicts()) {
                         sb.append("CONFLICT: Merge conflict in " + conflict.getPath() + "\n");
                     }
                     sb.append("Automatic merge failed. Fix conflicts and then commit the result.\n");
                     throw new MergeConflictsException(sb.toString());
                 }
             }
         } else {
             if (hasConflicts && commits.size() > 1) {
                 throw new CannotMergeException(
                         "Conflicted merge.\nCannot merge more than two commits when conflicts exist");
             }
             for (ObjectId commitId : commits) {
                 ProgressListener subProgress = subProgress(100.f / commits.size());
 
                 Preconditions.checkArgument(!ObjectId.NULL.equals(commitId),
                         "Cannot merge a NULL commit.");
                 Preconditions.checkArgument(repository.commitExists(commitId),
                         "Not a valid commit: " + commitId.toString());
 
                 subProgress.started();
                 if (ObjectId.NULL.equals(headRef.getObjectId())) {
                     // Fast-forward
                     command(UpdateRef.class).setName(currentBranch).setNewValue(commitId).call();
                     headRef = (SymRef) command(UpdateSymRef.class).setName(Ref.HEAD)
                             .setNewValue(currentBranch).call().get();
 
                     getWorkTree().updateWorkHead(commitId);
                     getIndex().updateStageHead(commitId);
                     subProgress.complete();
                     changed = true;
                     continue;
                 }
 
                 RevCommit headCommit = repository.getCommit(headRef.getObjectId());
                 final RevCommit targetCommit = repository.getCommit(commitId);
                 Optional<RevCommit> ancestorCommit = command(FindCommonAncestor.class)
                         .setLeft(headCommit).setRight(targetCommit).call();
                 subProgress.progress(10.f);
 
                 Preconditions.checkState(ancestorCommit.isPresent(),
                         "No ancestor commit could be found.");
 
                 if (ancestorCommit.get().getId().equals(headCommit.getId())) {
                     // Fast-forward
                     command(UpdateRef.class).setName(currentBranch).setNewValue(commitId).call();
                     headRef = (SymRef) command(UpdateSymRef.class).setName(Ref.HEAD)
                             .setNewValue(currentBranch).call().get();
 
                     getWorkTree().updateWorkHead(commitId);
                     getIndex().updateStageHead(commitId);
                     subProgress.complete();
                     changed = true;
                     continue;
                 } else if (ancestorCommit.get().getId().equals(commitId)) {
                     continue;
                 }
 
                 // Get all commits between the head commit and the ancestor.
                 Iterator<RevCommit> commitIterator = command(LogOp.class).setUntil(commitId).call();
 
                 List<RevCommit> commitsToMerge = new ArrayList<RevCommit>();
 
                 RevCommit commit;
                 do {
                     commit = commitIterator.next();
                     commitsToMerge.add(commit);
                 } while (!commit.getId().equals(ancestorCommit.get().getId()));
 
                 int numCommits = commitsToMerge.size() - 1;
 
                 int commitCount = 0;
                 for (int i = commitsToMerge.size() - 2; i >= 0; i--) {
                     commitCount++;
                     // get changes
                     RevCommit oldCommit = commitsToMerge.get(i);
                     Iterator<DiffEntry> diff = command(DiffTree.class)
                             .setOldTree(commitsToMerge.get(i + 1).getId())
                            .setNewTree(oldCommit.getId()).call();
                     // stage changes
                     getIndex().stage(
                             new SubProgressListener(subProgress, commitCount * 100.f / numCommits),
                             diff, 0);
                     changed = true;
                     fastForward = false;
                 }
 
                 getWorkTree().updateWorkHead(getIndex().getTree().getId());
 
                 subProgress.complete();
 
             }
 
         }
 
         if (!changed) {
             throw new NothingToCommitException("Already up to date.");
         }
 
         return commit(fastForward);
 
     }
 
     private RevCommit commit(boolean fastForward) {
 
         RevCommit mergeCommit;
         if (fastForward) {
             mergeCommit = repository.getCommit(commits.get(0));
         } else {
             String commitMessage = message;
             if (commitMessage == null) {
                 commitMessage = "";
                 for (ObjectId commit : commits) {
                     Optional<Ref> ref = command(ResolveBranchId.class).setObjectId(commit).call();
                     if (ref.isPresent()) {
                         commitMessage += "Merge branch " + ref.get().getName();
                     } else {
                         commitMessage += "Merge commit '" + commit.toString() + "'. ";
                     }
                 }
             }
             if (noCommit) {
                 final Optional<Ref> currHead = command(RefParse.class).setName(Ref.HEAD).call();
                 SymRef headRef = (SymRef) currHead.get();
                 RevCommit headCommit = repository.getCommit(headRef.getObjectId());
                 command(UpdateRef.class).setName(Ref.MERGE_HEAD).setNewValue(commits.get(0)).call();
                 // TODO:how to store multiple ids when octopus merge
                 command(UpdateRef.class).setName(Ref.ORIG_HEAD).setNewValue(headCommit.getId())
                         .call();
                 mergeCommit = headCommit;
                 command(SaveMergeCommitMessageOp.class).setMessage(commitMessage).call();
             } else {
                 mergeCommit = command(CommitOp.class).setMessage(commitMessage).addParents(commits)
                         .call();
             }
         }
 
         getProgressListener().complete();
 
         return mergeCommit;
     }
 }
