 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.geotools.data;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import javax.annotation.Nullable;
 
 import org.geogit.api.CommandLocator;
 import org.geogit.api.GeogitTransaction;
 import org.geogit.api.plumbing.DiffIndex;
 import org.geogit.api.plumbing.TransactionBegin;
 import org.geogit.api.plumbing.diff.DiffEntry;
 import org.geogit.api.porcelain.AddOp;
 import org.geogit.api.porcelain.CheckoutOp;
 import org.geogit.api.porcelain.CommitOp;
 import org.geogit.api.porcelain.NothingToCommitException;
 import org.geotools.data.Transaction;
 import org.geotools.data.Transaction.State;
 import org.geotools.data.store.ContentEntry;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 
 /**
  *
  */
 class GeogitTransactionState implements State {
 
     /** VERSIONING_COMMIT_AUTHOR */
     static final String VERSIONING_COMMIT_AUTHOR = "VersioningCommitAuthor";
 
     /** VERSIONING_COMMIT_MESSAGE */
     static final String VERSIONING_COMMIT_MESSAGE = "VersioningCommitMessage";
 
     private ContentEntry entry;
 
     private GeogitTransaction geogitTx;
 
     private Transaction tx;
 
     /**
      * @param entry
      */
     public GeogitTransactionState(ContentEntry entry) {
         this.entry = entry;
     }
 
     public Optional<GeogitTransaction> getGeogitTransaction() {
         return Optional.fromNullable(this.geogitTx);
     }
 
     @Override
     public void setTransaction(@Nullable final Transaction transaction) {
         Preconditions.checkArgument(!Transaction.AUTO_COMMIT.equals(transaction));
 
         if (transaction != null && this.tx != null) {
             throw new IllegalStateException(
                     "New transaction set without closing old transaction first.");
         }
         this.tx = transaction;
 
         if (transaction == null) {
            // Transaction.removeState has been called (during transaction.close())
             if (this.geogitTx != null) {
                // throw new IllegalStateException("Transaction is attempting to "
                 // + "close a non committed or aborted geogit transaction");
                 geogitTx.abort();
             }
             this.geogitTx = null;
         } else {
             GeoGitDataStore dataStore = (GeoGitDataStore) entry.getDataStore();
             CommandLocator commandLocator = dataStore.getCommandLocator(this.tx);
             this.geogitTx = commandLocator.command(TransactionBegin.class).call();
             // checkout the working branch
             final String workingBranch = dataStore.getOrFigureOutBranch();
             this.geogitTx.command(CheckoutOp.class).setForce(true).setSource(workingBranch).call();
         }
     }
 
     @Override
     public void addAuthorization(String AuthID) throws IOException {
         // not required
     }
 
     @Override
     public void commit() throws IOException {
         Preconditions.checkState(this.geogitTx != null);
         /*
          * This follows suite with the hack set on GeoSever's
          * org.geoserver.wfs.Transaction.getDatastoreTransaction()
          */
         final String author = (String) this.tx.getProperty(VERSIONING_COMMIT_AUTHOR);
         String commitMessage = (String) this.tx.getProperty(VERSIONING_COMMIT_MESSAGE);
         this.geogitTx.command(AddOp.class).call();
         try {
             CommitOp commitOp = this.geogitTx.command(CommitOp.class);
             if (author != null) {
                 commitOp.setAuthor(author, null);
             }
             if (commitMessage == null) {
                 commitMessage = composeDefaultCommitMessage();
             }
             commitOp.setMessage(commitMessage);
             commitOp.call();
         } catch (NothingToCommitException nochanges) {
             // ok
         }
 
         this.geogitTx.commit();
         GeoGitDataStore dataStore = (GeoGitDataStore) entry.getDataStore();
         GeogitSynchronizationHandler.get().setDirty(dataStore.getGeogit(),
                 dataStore.getOrFigureOutBranch());
 
         this.geogitTx = null;
     }
 
     private String composeDefaultCommitMessage() {
         Iterator<DiffEntry> indexDiffs = this.geogitTx.command(DiffIndex.class).call();
         int added = 0, removed = 0, modified = 0;
         StringBuilder msg = new StringBuilder();
         while (indexDiffs.hasNext()) {
             DiffEntry entry = indexDiffs.next();
             switch (entry.changeType()) {
             case ADDED:
                 added++;
                 break;
             case MODIFIED:
                 modified++;
                 break;
             case REMOVED:
                 removed++;
                 break;
             }
             if ((added + removed + modified) < 10) {
                 msg.append("\n ").append(entry.changeType().toString().toLowerCase()).append(" ")
                         .append(entry.newPath() == null ? entry.oldName() : entry.newPath());
             }
         }
         int count = added + removed + modified;
         if (count > 10) {
             msg.append("\n And ").append(count - 10).append(" more changes.");
         }
         StringBuilder title = new StringBuilder();
         if (added > 0) {
             title.append("added ").append(added);
         }
         if (modified > 0) {
             if (title.length() > 0) {
                 title.append(", ");
             }
             title.append("modified ").append(modified);
         }
         if (removed > 0) {
             if (title.length() > 0) {
                 title.append(", ");
             }
             title.append("removed ").append(removed);
         }
         if (count > 0) {
             title.append(" features via unversioned legacy client.\n");
         }
         msg.insert(0, title);
         return msg.toString();
     }
 
     @Override
     public void rollback() throws IOException {
         Preconditions.checkState(this.geogitTx != null);
         this.geogitTx.abort();
         this.geogitTx = null;
     }
 
 }
