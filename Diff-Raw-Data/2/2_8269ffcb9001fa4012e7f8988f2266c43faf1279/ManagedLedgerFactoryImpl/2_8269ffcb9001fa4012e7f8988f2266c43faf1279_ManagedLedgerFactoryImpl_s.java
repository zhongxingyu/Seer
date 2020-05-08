 /**
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.bookkeeper.mledger.impl;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.bookkeeper.client.BKException;
 import org.apache.bookkeeper.client.BookKeeper;
 import org.apache.bookkeeper.conf.ClientConfiguration;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.DeleteLedgerCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.OpenLedgerCallback;
 import org.apache.bookkeeper.mledger.ManagedLedger;
 import org.apache.bookkeeper.mledger.ManagedLedgerConfig;
 import org.apache.bookkeeper.mledger.ManagedLedgerException;
 import org.apache.bookkeeper.mledger.ManagedLedgerFactory;
 import org.apache.bookkeeper.mledger.impl.ManagedLedgerImpl.ManagedLedgerInitializeLedgerCallback;
 import org.apache.bookkeeper.mledger.impl.MetaStore.OpenMode;
 import org.apache.bookkeeper.util.OrderedSafeExecutor;
 import org.apache.zookeeper.WatchedEvent;
 import org.apache.zookeeper.Watcher;
 import org.apache.zookeeper.ZooKeeper;
 import org.apache.zookeeper.ZooKeeper.States;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Maps;
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 
 public class ManagedLedgerFactoryImpl implements ManagedLedgerFactory {
     private final MetaStore store;
     private final BookKeeper bookKeeper;
     private final boolean isBookkeeperManaged;
     private final ZooKeeper zookeeper;
     protected final ScheduledExecutorService executor = Executors.newScheduledThreadPool(5, new ThreadFactoryBuilder()
             .setNameFormat("bookkeeper-ml-%s").build());
     private final OrderedSafeExecutor orderedExecutor = new OrderedSafeExecutor(5, "bookkeper-ml-workers");
 
     protected final ConcurrentMap<String, ManagedLedgerImpl> ledgers = Maps.newConcurrentMap();
 
     public ManagedLedgerFactoryImpl(ClientConfiguration bkClientConfiguration) throws Exception {
         final CountDownLatch counter = new CountDownLatch(1);
         final String zookeeperQuorum = checkNotNull(bkClientConfiguration.getZkServers());
 
         zookeeper = new ZooKeeper(zookeeperQuorum, bkClientConfiguration.getZkTimeout(), new Watcher() {
             @Override
             public void process(WatchedEvent event) {
                 if (event.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
                     log.info("Connected to zookeeper");
                     counter.countDown();
                 } else {
                     log.error("Error connecting to zookeeper {}", event);
                 }
             }
         });
 
         if (!counter.await(bkClientConfiguration.getZkTimeout(), TimeUnit.MILLISECONDS)
                 || zookeeper.getState() != States.CONNECTED) {
             throw new ManagedLedgerException("Error connecting to ZooKeeper at '" + zookeeperQuorum + "'");
         }
 
         this.bookKeeper = new BookKeeper(bkClientConfiguration, zookeeper);
         this.isBookkeeperManaged = true;
 
         this.store = new MetaStoreImplZookeeper(zookeeper);
     }
 
     public ManagedLedgerFactoryImpl(BookKeeper bookKeeper, ZooKeeper zooKeeper) throws Exception {
         this.bookKeeper = bookKeeper;
         this.isBookkeeperManaged = false;
         this.zookeeper = null;
         this.store = new MetaStoreImplZookeeper(zooKeeper);
     }
 
     @Override
     public ManagedLedger open(String name) throws InterruptedException, ManagedLedgerException {
         return open(name, new ManagedLedgerConfig());
     }
 
     @Override
     public ManagedLedger open(String name, ManagedLedgerConfig config) throws InterruptedException,
             ManagedLedgerException {
         class Result {
             ManagedLedger l = null;
             ManagedLedgerException e = null;
         }
         final Result r = new Result();
         final CountDownLatch latch = new CountDownLatch(1);
         asyncOpen(name, config, new OpenLedgerCallback() {
             public void openLedgerComplete(ManagedLedger ledger, Object ctx) {
                 r.l = ledger;
                 latch.countDown();
             }
 
             public void openLedgerFailed(ManagedLedgerException exception, Object ctx) {
                 r.e = exception;
                 latch.countDown();
             }
         }, null);
 
         if (!latch.await(ManagedLedgerImpl.AsyncOperationTimeoutSeconds, TimeUnit.SECONDS)) {
             throw new ManagedLedgerException("Timeout during ledger open operation");
         }
 
         if (r.e != null) {
             throw r.e;
         }
         return r.l;
     }
 
     @Override
     public void asyncOpen(String name, OpenLedgerCallback callback, Object ctx) {
         asyncOpen(name, new ManagedLedgerConfig(), callback, ctx);
     }
 
     @Override
     public void asyncOpen(final String name, final ManagedLedgerConfig config, final OpenLedgerCallback callback,
             final Object ctx) {
         ManagedLedgerImpl ledger = ledgers.get(name);
         if (ledger != null) {
             log.info("Reusing opened ManagedLedger: {}", name);
             callback.openLedgerComplete(ledger, ctx);
         } else {
             final ManagedLedgerImpl newledger = new ManagedLedgerImpl(this, bookKeeper, store, config, executor,
                     orderedExecutor, name);
             newledger.initialize(OpenMode.CreateIfNotFound, new ManagedLedgerInitializeLedgerCallback() {
                 public void initializeComplete() {
                     ManagedLedgerImpl oldValue = ledgers.putIfAbsent(name, newledger);
                     if (oldValue != null) {
                         try {
                             newledger.close();
                         } catch (InterruptedException ie) {
                             Thread.currentThread().interrupt();
                             log.warn("Interruped while closing managed ledger", ie);
                         } catch (ManagedLedgerException mle) {
                             callback.openLedgerFailed(mle, ctx);
                             return;
                         }
 
                         callback.openLedgerComplete(oldValue, ctx);
                     } else {
                         callback.openLedgerComplete(newledger, ctx);
                     }
                 }
 
                 public void initializeFailed(ManagedLedgerException e) {
                     callback.openLedgerFailed(e, ctx);
                 }
             }, null);
         }
     }
 
     public ManagedLedger openReadOnly(String name) throws InterruptedException, ManagedLedgerException {
         return openReadOnly(name, new ManagedLedgerConfig());
     }
 
     public ManagedLedger openReadOnly(String name, ManagedLedgerConfig config) throws InterruptedException,
             ManagedLedgerException {
         class Result {
             ManagedLedger l = null;
             ManagedLedgerException e = null;
         }
         final Result r = new Result();
         final CountDownLatch latch = new CountDownLatch(1);
         asyncOpenReadOnly(name, config, new OpenLedgerCallback() {
             public void openLedgerComplete(ManagedLedger ledger, Object ctx) {
                 r.l = ledger;
                 latch.countDown();
             }
 
             public void openLedgerFailed(ManagedLedgerException exception, Object ctx) {
                 r.e = exception;
                 latch.countDown();
             }
         }, null);
 
         if (!latch.await(ManagedLedgerImpl.AsyncOperationTimeoutSeconds, TimeUnit.SECONDS)) {
             throw new ManagedLedgerException("Timeout during open-read-only operation");
         }
 
         if (r.e != null) {
             throw r.e;
         }
         return r.l;
     }
 
     public void asyncOpenReadOnly(String name, OpenLedgerCallback callback, Object ctx) {
         asyncOpenReadOnly(name, new ManagedLedgerConfig(), callback, ctx);
     }
 
     public void asyncOpenReadOnly(final String name, final ManagedLedgerConfig config,
             final OpenLedgerCallback callback, final Object ctx) {
         final ManagedLedgerAdminOnlyImpl ledger = new ManagedLedgerAdminOnlyImpl(this, bookKeeper, store, config,
                 executor, orderedExecutor, name);
         ledger.initialize(OpenMode.AdminObserver, new ManagedLedgerInitializeLedgerCallback() {
             public void initializeComplete() {
                 callback.openLedgerComplete(ledger, ctx);
             }
 
             public void initializeFailed(ManagedLedgerException e) {
                 callback.openLedgerFailed(e, ctx);
             }
         }, null);
     }
 
     @Override
     public void delete(String name) throws InterruptedException, ManagedLedgerException {
        ManagedLedgerImpl ledger = (ManagedLedgerImpl) openReadOnly(name);
         ledgers.remove(ledger.getName());
         ledger.delete();
     }
 
     @Override
     public void asyncDelete(final String ledger, final DeleteLedgerCallback callback, final Object ctx) {
         executor.submit(new Runnable() {
             public void run() {
                 try {
                     delete(ledger);
                     callback.deleteLedgerComplete(ctx);
                 } catch (Exception e) {
                     log.warn("Got exception when deleting MangedLedger: {}", e);
                     callback.deleteLedgerFailed(new ManagedLedgerException(e), ctx);
                 }
             }
         });
     }
 
     void close(ManagedLedger ledger) {
         // Remove the ledger from the internal factory cache
         ledgers.remove(ledger.getName(), ledger);
     }
 
     @Override
     public void shutdown() throws InterruptedException, ManagedLedgerException {
         executor.shutdown();
 
         for (ManagedLedger ledger : ledgers.values()) {
             ledger.close();
         }
 
         if (zookeeper != null) {
             zookeeper.close();
         }
 
         if (isBookkeeperManaged) {
             try {
                 bookKeeper.close();
             } catch (BKException e) {
                 throw new ManagedLedgerException(e);
             }
         }
 
     }
 
     public MetaStore getMetaStore() {
         return store;
     }
 
     private static final Logger log = LoggerFactory.getLogger(ManagedLedgerFactoryImpl.class);
 }
