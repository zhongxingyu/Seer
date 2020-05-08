 /* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the LGPL 2.1 license, available at the root
  * application directory.
  */
 
 package org.geogit.geotools.data;
 
 import java.io.IOException;
 
 import javax.annotation.Nullable;
 
 import org.geogit.api.CommandLocator;
 import org.geogit.api.GeogitTransaction;
 import org.geogit.api.plumbing.TransactionBegin;
 import org.geogit.api.porcelain.AddOp;
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
 public class GeogitTransactionState implements State {
 
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
         }
     }
 
     @Override
     public void addAuthorization(String AuthID) throws IOException {
         // not required
     }
 
     @Override
     public void commit() throws IOException {
         Preconditions.checkState(this.geogitTx != null);
         this.geogitTx.command(AddOp.class).call();
        try {
            this.geogitTx.command(CommitOp.class).call();
        } catch (NothingToCommitException nochanges) {
            // ok
        }
         this.geogitTx.commit();
         this.geogitTx = null;
     }
 
     @Override
     public void rollback() throws IOException {
         Preconditions.checkState(this.geogitTx != null);
         this.geogitTx.abort();
         this.geogitTx = null;
     }
 
 }
