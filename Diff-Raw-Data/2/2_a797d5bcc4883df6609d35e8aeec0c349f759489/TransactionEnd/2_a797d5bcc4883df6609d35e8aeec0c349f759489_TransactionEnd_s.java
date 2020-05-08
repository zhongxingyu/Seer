 /* Copyright (c) 2013 OpenPlans. All rights reserved.
  * This code is licensed under the BSD New License, available at the root
  * application directory.
  */
 
 package org.geogit.api.plumbing;
 
 import java.util.concurrent.TimeoutException;
 
 import org.geogit.api.AbstractGeoGitOp;
 import org.geogit.api.GeogitTransaction;
 import org.geogit.api.Ref;
 import org.geogit.api.SymRef;
 import org.geogit.api.porcelain.CheckoutOp;
 import org.geogit.api.porcelain.MergeOp;
 import org.geogit.api.porcelain.RebaseOp;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Suppliers;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableSet;
 import com.google.inject.Inject;
 
 /**
  * Finishes a {@link GeogitTransaction} by merging all refs that have been changed.
  * <p>
  * If a given ref has not been changed on the repsoitory, it will simply update the repository's ref
  * to the value of the transaction ref.
  * <p>
  * If the repository ref was updated while the transaction occurred, the changes will be brought
  * together via a merge or rebase operation and the new ref will be updated to the result.
  * 
  * @see GeogitTransaction
  */
 public class TransactionEnd extends AbstractGeoGitOp<Boolean> {
 
     private boolean cancel = false;
 
     private GeogitTransaction transaction = null;
 
     private boolean rebase = false;
 
     @Inject
     public TransactionEnd() {
     }
 
     /**
      * @param cancel if {@code true}, the transaction will be cancelled, otherwise it will be
      *        committed
      * @return {@code this}
      */
     public TransactionEnd setCancel(boolean cancel) {
         this.cancel = cancel;
         return this;
     }
 
     /**
      * @param transaction the transaction to end
      * @return {@code this}
      */
     public TransactionEnd setTransaction(GeogitTransaction transaction) {
         this.transaction = transaction;
         return this;
     }
 
     public TransactionEnd setRebase(boolean rebase) {
         this.rebase = rebase;
         return this;
     }
 
     /**
      * Ends the current transaction by either committing the changes or discarding them depending on
      * whether cancel is true or not.
      * 
      * @return Boolean - true if the transaction was successfully closed
      */
     @Override
     public Boolean call() {
         Preconditions.checkState(!(commandLocator instanceof GeogitTransaction),
                 "Cannot end a transaction within a transaction!");
         Preconditions.checkArgument(transaction != null, "No transaction was specified!");
 
         final Optional<Ref> currHead = command(RefParse.class).setName(Ref.HEAD).call();
         final String currentBranch;
         if (currHead.isPresent() && currHead.get() instanceof SymRef) {
             currentBranch = ((SymRef) currHead.get()).getTarget();
         } else {
             currentBranch = "";
         }
 
         if (!cancel) {
             ImmutableSet<Ref> changedRefs = getChangedRefs();
             // Lock the repository
             try {
                 getRefDatabase().lock();
             } catch (TimeoutException e) {
                 Throwables.propagate(e);
             }
 
             try {
                 // Update refs
                 for (Ref ref : changedRefs) {
                     Ref updatedRef = ref;
 
                     Optional<Ref> repoRef = command(RefParse.class).setName(ref.getName()).call();
                     if (repoRef.isPresent() && repositoryChanged(repoRef.get())) {
                         if (rebase) {
                             // Try to rebase
                             transaction.command(CheckoutOp.class).setSource(ref.getName())
                                     .setForce(true).call();
                             transaction.command(RebaseOp.class)
                                     .setUpstream(Suppliers.ofInstance(repoRef.get().getObjectId()))
                                     .call();
                             updatedRef = transaction.command(RefParse.class).setName(ref.getName())
                                     .call().get();
                         } else {
                             // sync transactions have to use merge to prevent divergent history
                             transaction.command(CheckoutOp.class).setSource(ref.getName())
                                     .setForce(true).call();
                             transaction.command(MergeOp.class)
                                     .addCommit(Suppliers.ofInstance(repoRef.get().getObjectId()))
                                     .call();
                             updatedRef = transaction.command(RefParse.class).setName(ref.getName())
                                     .call().get();
                         }
                     }
                     command(UpdateRef.class).setName(ref.getName())
                             .setNewValue(updatedRef.getObjectId()).call();
 
                     if (currentBranch.equals(ref.getName())) {
                         // Update HEAD, WORK_HEAD and STAGE_HEAD
                         command(UpdateSymRef.class).setName(Ref.HEAD).setNewValue(ref.getName())
                                 .call();
                         command(UpdateRef.class).setName(Ref.WORK_HEAD)
                                 .setNewValue(updatedRef.getObjectId()).call();
                         command(UpdateRef.class).setName(Ref.STAGE_HEAD)
                                 .setNewValue(updatedRef.getObjectId()).call();
                     }
                 }
 
                 // TODO: What happens if there are unstaged or staged changes in the repository when
                 // a transaction is committed?
             } finally {
                 // Unlock the repository
                 getRefDatabase().unlock();
             }
 
         }
 
         // Erase old refs
         transaction.close();
 
         // Success
         return true;
     }
 
     private ImmutableSet<Ref> getChangedRefs() {
        return transaction.command(ForEachRef.class).setPrefixFilter(Ref.HEADS_PREFIX).call();
     }
 
     private boolean repositoryChanged(Ref ref) {
         Optional<Ref> transactionOriginal = transaction.command(RefParse.class)
                 .setName(ref.getName().replace("refs/", "orig/refs/")).call();
         if (transactionOriginal.isPresent()) {
             return !ref.getObjectId().equals(transactionOriginal.get().getObjectId());
         }
         // Ref was created in transaction and on the repo
         return true;
     }
 }
