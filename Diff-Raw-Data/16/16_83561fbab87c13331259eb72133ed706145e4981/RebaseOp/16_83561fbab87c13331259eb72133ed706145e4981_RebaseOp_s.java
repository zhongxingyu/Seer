 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 package org.geogit.api.porcelain;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.geogit.api.AbstractGeoGitOp;
 import org.geogit.api.CommitBuilder;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Platform;
 import org.geogit.api.Ref;
 import org.geogit.api.RevCommit;
 import org.geogit.api.SymRef;
 import org.geogit.api.plumbing.DiffTree;
 import org.geogit.api.plumbing.FindCommonAncestor;
 import org.geogit.api.plumbing.RefParse;
 import org.geogit.api.plumbing.ResolveTreeish;
 import org.geogit.api.plumbing.UpdateRef;
 import org.geogit.api.plumbing.UpdateSymRef;
 import org.geogit.api.plumbing.WriteTree;
 import org.geogit.api.plumbing.diff.DiffEntry;
 import org.geogit.repository.Repository;
 import org.opengis.util.ProgressListener;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Supplier;
 import com.google.common.base.Suppliers;
 import com.google.inject.Inject;
 
 /**
  * 
  * Rebase the current head to the included branch head.
  * 
  * <b>NOTE:</b> so far we don't have the ability to merge non conflicting changes. Instead, the diff
  * list we get acts on whole objects, , so its possible that this operation overrites non
  * conflicting changes when reabasing a branch that has non conflicting changes at both sides. This
  * needs to be revisited once we get more merge tools.
  */
 public class RebaseOp extends AbstractGeoGitOp<Boolean> {
 
     private Supplier<ObjectId> upstream;
 
     private Supplier<ObjectId> onto;
 
     private Repository repository;
 
     private Platform platform;
 
     /**
      * Constructs a new {@code RebaseOp} using the specified parameters.
      * 
      * @param repository the repository to use
      * @param platform the platform to use
      */
     @Inject
     public RebaseOp(Repository repository, Platform platform) {
         this.repository = repository;
         this.platform = platform;
     }
 
     /**
      * Sets the commit to replay commits onto.
      * 
      * @param onto a supplier for the commit id
      * @return {@code this}
      */
     public RebaseOp setOnto(final Supplier<ObjectId> onto) {
         this.onto = onto;
         return this;
     }
 
     /**
      * Sets the upstream commit. This is used in finding the common ancestor.
      * 
      * @param upstream a supplier for the upstream commit
      * @return {@code this}
      */
     public RebaseOp setUpstream(final Supplier<ObjectId> upstream) {
         this.upstream = upstream;
         return this;
     }
 
     /**
      * Executes the rebase operation.
      * 
      * @return always {@code true}
      */
     @Override
     public Boolean call() {
 
         final Optional<Ref> currHead = command(RefParse.class).setName(Ref.HEAD).call();
         Preconditions.checkState(currHead.isPresent(), "Repository has no HEAD, can't rebase.");
         Preconditions.checkState(currHead.get() instanceof SymRef,
                 "Can't rebase from detached HEAD");
         final SymRef headRef = (SymRef) currHead.get();
         final String currentBranch = headRef.getTarget();
 
         Preconditions.checkState(upstream != null, "No upstream target has been specified.");
 
         Preconditions.checkState(!ObjectId.NULL.equals(upstream.get()),
                 "Upstream did not resolve to a commit.");
 
         getProgressListener().started();
 
         if (ObjectId.NULL.equals(headRef.getObjectId())) {
             // Fast-forward
             command(UpdateRef.class).setName(currentBranch).setNewValue(upstream.get()).call();
             command(UpdateSymRef.class).setName(Ref.HEAD).setNewValue(currentBranch).call();
 
             getWorkTree().updateWorkHead(upstream.get());
             getIndex().updateStageHead(upstream.get());
             getProgressListener().complete();
             return true;
         }
 
         final RevCommit headCommit = repository.getCommit(headRef.getObjectId());
         final RevCommit targetCommit = repository.getCommit(upstream.get());
 
         Optional<RevCommit> ancestorCommit = command(FindCommonAncestor.class).setLeft(headCommit)
                 .setRight(targetCommit).setProgressListener(subProgress(10.f)).call();
 
         Preconditions.checkState(ancestorCommit.isPresent(), "No ancestor commit could be found.");
 
         if (ancestorCommit.get().getId().equals(headCommit.getId())) {
             // Fast-forward
             command(UpdateRef.class).setName(currentBranch).setNewValue(upstream.get()).call();
             command(UpdateSymRef.class).setName(Ref.HEAD).setNewValue(currentBranch).call();
 
             getWorkTree().updateWorkHead(upstream.get());
             getIndex().updateStageHead(upstream.get());
             getProgressListener().complete();
             return true;
         }
 
         // Get all commits between the head commit and the ancestor.
         Iterator<RevCommit> commitIterator = command(LogOp.class).call();
 
         List<RevCommit> commitsToRebase = new ArrayList<RevCommit>();
 
         RevCommit commit;
         do {
             commit = commitIterator.next();
             commitsToRebase.add(commit);
         } while (!commit.getId().equals(ancestorCommit.get().getId()));
 
         // Check out the target
         if (onto == null) {
             onto = Suppliers.ofInstance(upstream.get());
         }
         command(CheckoutOp.class).setSource(onto.get().toString()).call();
 
         ObjectId rebaseHead = onto.get();
 
         ProgressListener subProgress = subProgress(90.f);
 
         int numCommits = commitsToRebase.size() - 1;
 
         for (int i = commitsToRebase.size() - 2; i >= 0; i--) {
             // get changes
             RevCommit oldCommit = commitsToRebase.get(i);
             Iterator<DiffEntry> diff = command(DiffTree.class)
                     .setOldTree(commitsToRebase.get(i + 1).getId()).setNewTree(oldCommit.getId())
                    .call();
             // stage changes
             getIndex().stage(getProgressListener(), diff, 0);
             // write new tree
             ObjectId oldTreeId = command(ResolveTreeish.class).setTreeish(rebaseHead).call().get();
             ObjectId newTreeId = command(WriteTree.class).call();
             if (newTreeId.equals(oldTreeId)) {
                 // No changes, skip this commit
                 continue;
             }
             long timestamp = platform.currentTimeMillis();
             // Create new commit
             CommitBuilder builder = new CommitBuilder(oldCommit);
             builder.setParentIds(Arrays.asList(rebaseHead));
             builder.setTreeId(newTreeId);
             builder.setCommitterTimestamp(timestamp);
             builder.setCommitterTimeZoneOffset(platform.timeZoneOffset(timestamp));
 
             RevCommit newCommit = builder.build();
             repository.getObjectDatabase().put(newCommit);
 
             rebaseHead = newCommit.getId();
 
             command(UpdateRef.class).setName(currentBranch).setNewValue(rebaseHead).call();
             command(UpdateSymRef.class).setName(Ref.HEAD).setNewValue(currentBranch).call();
 
             getWorkTree().updateWorkHead(newTreeId);
             getIndex().updateStageHead(newTreeId);
 
             subProgress.progress((numCommits - i) * 100.f / numCommits);
 
         }
         subProgress.complete();
 
         getProgressListener().complete();
 
         return true;
     }
 }
