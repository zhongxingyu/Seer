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
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static java.lang.Math.min;
 import static org.apache.bookkeeper.mledger.util.VarArgs.va;
 
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Queue;
 import java.util.TreeMap;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.apache.bookkeeper.client.AsyncCallback;
 import org.apache.bookkeeper.client.AsyncCallback.CreateCallback;
 import org.apache.bookkeeper.client.AsyncCallback.OpenCallback;
 import org.apache.bookkeeper.client.AsyncCallback.ReadCallback;
 import org.apache.bookkeeper.client.BKException;
 import org.apache.bookkeeper.client.BKException.BKNoSuchLedgerExistsException;
 import org.apache.bookkeeper.client.BookKeeper;
 import org.apache.bookkeeper.client.LedgerEntry;
 import org.apache.bookkeeper.client.LedgerHandle;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.AddEntryCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.CloseCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.DeleteCursorCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.OpenCursorCallback;
 import org.apache.bookkeeper.mledger.Entry;
 import org.apache.bookkeeper.mledger.ManagedCursor;
 import org.apache.bookkeeper.mledger.ManagedLedger;
 import org.apache.bookkeeper.mledger.ManagedLedgerConfig;
 import org.apache.bookkeeper.mledger.ManagedLedgerException;
 import org.apache.bookkeeper.mledger.ManagedLedgerException.ManagedLedgerFencedException;
 import org.apache.bookkeeper.mledger.ManagedLedgerException.MetaStoreException;
 import org.apache.bookkeeper.mledger.Position;
 import org.apache.bookkeeper.mledger.impl.ManagedCursorImpl.VoidCallback;
 import org.apache.bookkeeper.mledger.impl.MetaStore.MetaStoreCallback;
 import org.apache.bookkeeper.mledger.impl.MetaStore.OpenMode;
 import org.apache.bookkeeper.mledger.impl.MetaStore.Version;
 import org.apache.bookkeeper.mledger.proto.MLDataFormats.ManagedLedgerInfo;
 import org.apache.bookkeeper.mledger.proto.MLDataFormats.ManagedLedgerInfo.LedgerInfo;
 import org.apache.bookkeeper.mledger.util.CallbackMutex;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.cache.Cache;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.RemovalListener;
 import com.google.common.cache.RemovalNotification;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 class ManagedLedgerImpl implements ManagedLedger, CreateCallback, OpenCallback, ReadCallback {
     private final static long MegaByte = 1024 * 1024;
 
     private final BookKeeper bookKeeper;
     private final String name;
 
     private final ManagedLedgerConfig config;
     private final MetaStore store;
 
     private final Cache<Long, LedgerHandle> ledgerCache;
     private final TreeMap<Long, LedgerInfo> ledgers = Maps.newTreeMap();
     private Version ledgersVersion;
 
     private final ManagedCursorContainer cursors = new ManagedCursorContainer();
 
     AtomicLong numberOfEntries = new AtomicLong(0);
     AtomicLong totalSize = new AtomicLong(0);
 
     private final CallbackMutex trimmerMutex = new CallbackMutex();
 
     /**
      * This lock is held while the ledgers list is updated asynchronously on the
      * metadata store. Since we use the store version, we cannot have multiple
      * concurrent updates.
      */
     private final CallbackMutex ledgersListMutex = new CallbackMutex();
 
     private LedgerHandle currentLedger;
     private long currentLedgerEntries = 0;
     private long currentLedgerSize = 0;
 
     enum State {
         None, // Uninitialized
         LedgerOpened, // A ledger is ready to write into
         ClosingLedger, // Closing current ledger
         ClosedLedger, // Current ledger has been closed and there's no pending
                       // operation
         CreatingLedger, // Creating a new ledger
         Closed, // ManagedLedger has been closed
         Fenced, // A managed ledger is fenced when there is some concurrent
                 // access from a different session/machine. In this state the
                 // managed ledger will throw exception for all operations, since
                 // the new instance will take over
     };
 
     private State state;
 
     private final ScheduledExecutorService executor;
     private final ManagedLedgerFactoryImpl factory;
 
     /**
      * Queue of pending entries to be added to the managed ledger. Typically
      * entries are queued when a new ledger is created asynchronously and hence
      * there is no ready ledger to write into.
      */
     private final Queue<OpAddEntry> pendingAddEntries = Lists.newLinkedList();
 
     // //////////////////////////////////////////////////////////////////////
 
     public ManagedLedgerImpl(ManagedLedgerFactoryImpl factory, BookKeeper bookKeeper, MetaStore store,
             ManagedLedgerConfig config, ScheduledExecutorService executor, final String name) {
         this.factory = factory;
         this.bookKeeper = bookKeeper;
         this.config = config;
         this.store = store;
         this.name = name;
         this.executor = executor;
         this.currentLedger = null;
         this.state = State.None;
         this.ledgersVersion = null;
 
         RemovalListener<Long, LedgerHandle> removalListener = new RemovalListener<Long, LedgerHandle>() {
             public void onRemoval(RemovalNotification<Long, LedgerHandle> entry) {
                 LedgerHandle ledger = entry.getValue();
                 log.debug("[{}] Closing ledger: {} cause={}", va(name, ledger.getId(), entry.getCause()));
                 try {
                     ledger.close();
                 } catch (Exception e) {
                     log.error("[{}] Error closing ledger {}", name, ledger.getId());
                     log.error("Exception: ", e);
                 }
             }
         };
         this.ledgerCache = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS)
                 .removalListener(removalListener).build();
     }
 
     synchronized void initialize(final OpenMode openMode, final ManagedLedgerInitializeLedgerCallback callback,
             final Object ctx) {
         log.info("Opening managed ledger {}", name);
 
         // Fetch the list of existing ledgers in the managed ledger
         store.getManagedLedgerInfo(name, openMode, new MetaStoreCallback<ManagedLedgerInfo>() {
             public void operationComplete(ManagedLedgerInfo mlInfo, Version version) {
                 ledgersVersion = version;
                 for (LedgerInfo ls : mlInfo.getLedgerInfoList()) {
                     ledgers.put(ls.getLedgerId(), ls);
                 }
 
                 // Last ledger stat may be zeroed, we must update it
                 if (ledgers.size() > 0) {
                     final long id = ledgers.lastKey();
                     OpenCallback opencb = new OpenCallback() {
                         public void openComplete(int rc, LedgerHandle lh, Object ctx) {
                             if (rc == BKException.Code.OK) {
                                 LedgerInfo info = LedgerInfo.newBuilder().setLedgerId(id)
                                         .setEntries(lh.getLastAddConfirmed() + 1).setSize(lh.getLength()).build();
                                 ledgers.put(id, info);
                                 lh.asyncClose(new AsyncCallback.CloseCallback() {
                                     public void closeComplete(int rc, LedgerHandle lh, Object ctx) {
                                         if (rc == BKException.Code.OK) {
                                             initializeBookKeeper(openMode, callback);
                                         } else {
                                             callback.initializeFailed(new ManagedLedgerException(BKException.create(rc)));
                                         }
                                     }
                                 }, null);
                             } else if (rc == BKException.Code.NoSuchLedgerExistsException) {
                                 log.warn("[{}] Ledger not found: {}", name, ledgers.lastKey());
                                 initializeBookKeeper(openMode, callback);
                             } else {
                                 callback.initializeFailed(new ManagedLedgerException(BKException.create(rc)));
                                 return;
                             }
                         }
                     };
 
                     if (openMode == OpenMode.AdminObserver) {
                         // When we are read-only observers we don't want to
                         // fence current ledgers
                         bookKeeper.asyncOpenLedgerNoRecovery(id, config.getDigestType(), config.getPassword(), opencb,
                                 null);
                     } else {
                         bookKeeper.asyncOpenLedger(id, config.getDigestType(), config.getPassword(), opencb, null);
                     }
                 } else {
                     initializeBookKeeper(openMode, callback);
                 }
             }
 
             public void operationFailed(MetaStoreException e) {
                 callback.initializeFailed(new ManagedLedgerException(e));
             }
         });
     }
 
     private synchronized void initializeBookKeeper(OpenMode openMode,
             final ManagedLedgerInitializeLedgerCallback callback) {
         log.debug("[{}] initializing bookkeeper; ledgers {}", name, ledgers);
 
         // Calculate total entries and size
         for (LedgerInfo ls : ledgers.values()) {
             numberOfEntries.addAndGet(ls.getEntries());
             totalSize.addAndGet(ls.getSize());
         }
 
         if (openMode == OpenMode.AdminObserver) {
             // We are not opening a new ledger
             ledgersListMutex.unlock();
             initializeCursors(callback);
             return;
         }
 
         final MetaStoreCallback<Void> storeLedgersCb = new MetaStoreCallback<Void>() {
             public void operationComplete(Void v, Version version) {
                 ledgersVersion = version;
                 ledgersListMutex.unlock();
                 initializeCursors(callback);
             }
 
             public void operationFailed(MetaStoreException e) {
                 ledgersListMutex.unlock();
                 callback.initializeFailed(new ManagedLedgerException(e));
             }
         };
 
         // Create a new ledger to start writing
         bookKeeper.asyncCreateLedger(config.getEnsembleSize(), config.getQuorumSize(), config.getDigestType(),
                 config.getPassword(), new CreateCallback() {
                     public void createComplete(int rc, LedgerHandle lh, Object ctx) {
                         if (rc == BKException.Code.OK) {
                             state = State.LedgerOpened;
                             currentLedger = lh;
                             LedgerInfo info = LedgerInfo.newBuilder().setLedgerId(currentLedger.getId()).build();
                             ledgers.put(currentLedger.getId(), info);
                             // Save it back to ensure all nodes exist
                             ledgersListMutex.lock();
 
                             ManagedLedgerInfo mlInfo = ManagedLedgerInfo.newBuilder()
                                     .addAllLedgerInfo(ledgers.values()).build();
                             store.asyncUpdateLedgerIds(name, mlInfo, ledgersVersion, storeLedgersCb);
                         } else {
                             callback.initializeFailed(new ManagedLedgerException(BKException.create(rc)));
                         }
                     }
                 }, null);
     }
 
     private void initializeCursors(final ManagedLedgerInitializeLedgerCallback callback) {
         log.debug("[{}] initializing cursors", name);
         store.getConsumers(name, new MetaStoreCallback<List<String>>() {
             public void operationComplete(List<String> consumers, Version v) {
                 // Load existing cursors
                 final AtomicInteger cursorCount = new AtomicInteger(consumers.size());
                 log.debug("[{}] Found {} cursors", name, consumers.size());
 
                 if (consumers.isEmpty()) {
                     callback.initializeComplete();
                     return;
                 }
 
                 for (final String cursorName : consumers) {
                     log.debug("[{}] Loading cursor {}", name, cursorName);
                     final ManagedCursorImpl cursor = new ManagedCursorImpl(bookKeeper, config, ManagedLedgerImpl.this,
                             cursorName);
                     cursor.recover(new VoidCallback() {
                         public void operationComplete() {
                             log.debug("[{}] Recovery for cursor {} completed. todo={}",
                                     va(name, cursorName, cursorCount.get() - 1));
                             synchronized (ManagedLedgerImpl.this) {
                                 cursors.add(cursor);
                             }
 
                             if (cursorCount.decrementAndGet() == 0) {
                                 callback.initializeComplete();
                             }
                         }
 
                         public void operationFailed(ManagedLedgerException exception) {
                             log.warn("[{}] Recovery for cursor {} failed", name, cursorName);
                             cursorCount.set(-1);
                             callback.initializeFailed(exception);
                         }
                     });
                 }
             }
 
             public void operationFailed(MetaStoreException e) {
                 log.warn("[{}] Failed to get the cursors list", name, e);
                 callback.initializeFailed(new ManagedLedgerException(e));
             }
         });
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     public Position addEntry(byte[] data) throws InterruptedException, ManagedLedgerException {
         final CountDownLatch counter = new CountDownLatch(1);
         // Result list will contain the status exception and the resulting
         // position
         class Result {
             ManagedLedgerException status = null;
             Position position = null;
         }
         final Result result = new Result();
 
         asyncAddEntry(data, new AddEntryCallback() {
             public void addComplete(Position position, Object ctx) {
                 result.position = position;
                 counter.countDown();
             }
 
             public void addFailed(ManagedLedgerException exception, Object ctx) {
                 result.status = exception;
                 counter.countDown();
             }
         }, null);
 
         counter.await();
         if (result.status != null) {
             log.error("Error adding entry", result.status);
             throw result.status;
         }
 
         return result.position;
     }
 
     @Override
     public synchronized void asyncAddEntry(final byte[] data, final AddEntryCallback callback, final Object ctx) {
         log.debug("[{}] asyncAddEntry size={} state={}", va(name, data.length, state));
         if (state == State.Fenced) {
             callback.addFailed(new ManagedLedgerFencedException(), ctx);
             return;
         } else if (state == State.Closed) {
             callback.addFailed(new ManagedLedgerException("Managed ledger was already closed"), ctx);
             return;
         }
 
         OpAddEntry addOperation = new OpAddEntry(this, data, callback, ctx);
 
         if (state == State.ClosingLedger || state == State.CreatingLedger) {
             // We don't have a ready ledger to write into
             // We are waiting for a new ledger to be created
             log.debug("[{}] Queue addEntry request", name);
             pendingAddEntries.add(addOperation);
         } else if (state == State.ClosedLedger) {
             // No ledger and no pending operations. Create a new one
             pendingAddEntries.add(addOperation);
             log.debug("[{}] Creating a new ledger", name);
             state = State.CreatingLedger;
             bookKeeper.asyncCreateLedger(config.getEnsembleSize(), config.getQuorumSize(), config.getDigestType(),
                     config.getPassword(), this, ctx);
         } else {
             checkArgument(state == State.LedgerOpened);
             checkArgument(!currentLedgerIsFull());
 
             // Write into lastLedger
             log.debug("[{}] Write into current ledger lh={}", name, currentLedger.getId());
             addOperation.setLedger(currentLedger);
 
             ++currentLedgerEntries;
             currentLedgerSize += data.length;
             if (currentLedgerIsFull()) {
                 // This entry will be the last added to current ledger
                 addOperation.setCloseWhenDone(true);
                 state = State.ClosingLedger;
             }
 
             addOperation.initiate();
         }
     }
 
     @Override
     public synchronized ManagedCursor openCursor(String cursorName) throws InterruptedException, ManagedLedgerException {
         final CountDownLatch counter = new CountDownLatch(1);
         class Result {
             ManagedCursor cursor = null;
             ManagedLedgerException exception = null;
         }
         final Result result = new Result();
 
         asyncOpenCursor(cursorName, new OpenCursorCallback() {
             public void openCursorComplete(ManagedCursor cursor, Object ctx) {
                 result.cursor = cursor;
                 counter.countDown();
             }
 
             public void openCursorFailed(ManagedLedgerException exception, Object ctx) {
                 result.exception = exception;
                 counter.countDown();
             }
 
         }, null);
 
         counter.await();
         if (result.exception != null) {
             log.error("Error adding entry", result.exception);
             throw result.exception;
         }
 
         return result.cursor;
     }
 
     @Override
     public synchronized void asyncOpenCursor(final String cursorName, final OpenCursorCallback callback,
             final Object ctx) {
         try {
             checkManagedLedgerIsOpen();
             checkFenced();
         } catch (ManagedLedgerException e) {
             callback.openCursorFailed(e, ctx);
         }
 
         ManagedCursor cachedCursor = cursors.get(cursorName);
         if (cachedCursor != null) {
             log.debug("[{}] Cursor was already created {}", name, cachedCursor);
             callback.openCursorComplete(cachedCursor, ctx);
             return;
         }
 
         // Create a new one and persist it
         log.debug("[{}] Creating new cursor: {}", name, cursorName);
         final PositionImpl position = new PositionImpl(currentLedger.getId(), currentLedger.getLastAddConfirmed());
         final ManagedCursorImpl cursor = new ManagedCursorImpl(bookKeeper, config, this, cursorName);
         cursor.initialize(position, new VoidCallback() {
             public void operationComplete() {
                 log.debug("[{}] Opened new cursor: {}", name, cursor);
                 cursors.add(cursor);
                 callback.openCursorComplete(cursor, ctx);
             }
 
             public void operationFailed(ManagedLedgerException exception) {
                 log.debug("[{}] Failed to open cursor: {}", name, cursor);
                 callback.openCursorFailed(exception, ctx);
             }
         });
     }
 
     @Override
     public void asyncDeleteCursor(final String consumerName, final DeleteCursorCallback callback, final Object ctx) {
         store.asyncRemoveConsumer(this.name, consumerName, new MetaStoreCallback<Void>() {
             public void operationComplete(Void result, Version version) {
                 synchronized (ManagedLedgerImpl.this) {
                     cursors.removeCursor(consumerName);
                 }
 
                 trimConsumedLedgersInBackground();
                 callback.deleteCursorComplete(ctx);
             }
 
             public void operationFailed(MetaStoreException e) {
                 callback.deleteCursorFailed(e, ctx);
             }
 
         });
     }
 
     @Override
     public void deleteCursor(String name) throws InterruptedException, ManagedLedgerException {
         final CountDownLatch counter = new CountDownLatch(1);
         class Result {
             ManagedLedgerException exception = null;
         }
         final Result result = new Result();
 
         asyncDeleteCursor(name, new DeleteCursorCallback() {
             public void deleteCursorComplete(Object ctx) {
                 counter.countDown();
             }
 
             public void deleteCursorFailed(ManagedLedgerException exception, Object ctx) {
                 result.exception = exception;
                 counter.countDown();
             }
 
         }, null);
 
         counter.await();
         if (result.exception != null) {
             log.error("Error adding entry", result.exception);
             throw result.exception;
         }
     }
 
     @Override
     public synchronized Iterable<ManagedCursor> getCursors() {
         return cursors.toList();
     }
 
     @Override
     public long getNumberOfEntries() {
         return numberOfEntries.get();
     }
 
     @Override
     public long getTotalSize() {
         return totalSize.get();
     }
 
     @Override
     public synchronized void close() throws InterruptedException, ManagedLedgerException {
         checkFenced();
 
         for (LedgerHandle ledger : ledgerCache.asMap().values()) {
             log.debug("Closing ledger: {}", ledger.getId());
             try {
                 ledger.close();
             } catch (BKException e) {
                 throw new ManagedLedgerException(e);
             }
         }
 
         ledgerCache.invalidateAll();
         log.info("Invalidated {} ledgers in cache", ledgerCache.size());
         factory.close(this);
         state = State.Closed;
     }
 
     @Override
     public void asyncClose(final CloseCallback callback, final Object ctx) {
         executor.execute(new Runnable() {
             public void run() {
                 try {
                     close();
                     callback.closeComplete(ctx);
                 } catch (Exception e) {
                     log.warn("[{}] Got exception when closin managed ledger: {}", name, e);
                     callback.closeFailed(new ManagedLedgerException(e), ctx);
                 }
             }
         });
     }
 
     // //////////////////////////////////////////////////////////////////////
     // Callbacks
 
     @Override
     public synchronized void createComplete(int rc, LedgerHandle lh, Object ctx) {
         log.debug("[{}] createComplete rc={} ledger={}", va(name, rc, lh != null ? lh.getId() : -1));
 
         if (rc != BKException.Code.OK) {
             state = State.ClosedLedger;
             log.error("[{}] Error creating ledger rc={} {}", va(name, rc, BKException.getMessage(rc)));
             ManagedLedgerException status = new ManagedLedgerException(BKException.create(rc));
 
             // Empty the list of pending requests and make all of them fail
            while (!pendingAddEntries.isEmpty()) {
                pendingAddEntries.poll().failed(status);
             }
         } else {
             log.debug("[{}] Successfully created new ledger {}", name, lh.getId());
             ledgers.put(lh.getId(), LedgerInfo.newBuilder().setLedgerId(lh.getId()).build());
             currentLedger = lh;
             currentLedgerEntries = 0;
             currentLedgerSize = 0;
 
             MetaStoreCallback<Void> cb = new MetaStoreCallback<Void>() {
                 public void operationComplete(Void v, Version version) {
                     log.debug("Updating of ledgers list after create complete");
                     ledgersVersion = version;
                     ledgersListMutex.unlock();
                     updateLedgersIdsComplete(version);
                 }
 
                 public void operationFailed(MetaStoreException e) {
                     log.warn("Error updating meta data with the new list of ledgers");
                     ledgersListMutex.unlock();
 
                     synchronized (ManagedLedgerImpl.this) {
                        while (!pendingAddEntries.isEmpty()) {
                            pendingAddEntries.poll().failed(e);
                         }
                     }
                 }
             };
 
             ledgersListMutex.lock();
             ManagedLedgerInfo mlInfo = ManagedLedgerInfo.newBuilder().addAllLedgerInfo(ledgers.values()).build();
             store.asyncUpdateLedgerIds(name, mlInfo, ledgersVersion, cb);
             log.debug("Updating ledgers ids with new ledger");
         }
     }
 
     public synchronized void updateLedgersIdsComplete(Version version) {
         state = State.LedgerOpened;
 
         // Process all the pending addEntry requests
        while (!pendingAddEntries.isEmpty()) {
             OpAddEntry op = pendingAddEntries.poll();
 
             op.setLedger(currentLedger);
             ++currentLedgerEntries;
             currentLedgerSize += op.data.length;
 
             if (currentLedgerIsFull()) {
                 state = State.ClosingLedger;
                 op.setCloseWhenDone(true);
                 op.initiate();
                 log.debug("[{}] Stop writing into ledger {} queue={}",
                         va(name, currentLedger.getId(), pendingAddEntries.size()));
                 break;
             } else {
                 op.initiate();
             }
         }
     }
 
     // //////////////////////////////////////////////////////////////////////
     // Private helpers
 
     synchronized void ledgerClosed(LedgerHandle lh) {
         checkArgument(lh.getId() == currentLedger.getId());
         state = State.ClosedLedger;
 
         log.debug("[{}] Ledger has been closed id={} entries={}", va(name, lh.getId(), lh.getLastAddConfirmed() + 1));
         LedgerInfo info = LedgerInfo.newBuilder().setLedgerId(lh.getId()).setEntries(lh.getLastAddConfirmed() + 1)
                 .setSize(lh.getLength()).build();
         ledgers.put(lh.getId(), info);
 
         trimConsumedLedgersInBackground();
 
         if (!pendingAddEntries.isEmpty()) {
             // Need to create a new ledger to write pending entries
             log.debug("[{}] Creating a new ledger", name);
             state = State.CreatingLedger;
             bookKeeper.asyncCreateLedger(config.getEnsembleSize(), config.getQuorumSize(), config.getDigestType(),
                     config.getPassword(), this, null);
         }
     }
 
     synchronized void asyncReadEntries(OpReadEntry opReadEntry) {
         if (state == State.Fenced) {
             opReadEntry.failed(new ManagedLedgerFencedException());
             return;
         }
 
         LedgerHandle ledger = null;
 
         long id = opReadEntry.readPosition.getLedgerId();
 
         if (id == currentLedger.getId()) {
             // Current writing ledger is not in the cache (since we don't want
             // it to be automatically evicted), and we cannot use 2 different
             // ledger handles (read & write)for the same ledger.
             ledger = currentLedger;
         } else {
             ledger = ledgerCache.getIfPresent(id);
             if (ledger == null) {
                 // Open the ledger and cache the handle
                 log.debug("[{}] Asynchronously opening ledger {} for read", name, id);
                 bookKeeper.asyncOpenLedger(id, config.getDigestType(), config.getPassword(), this, opReadEntry);
                 return;
             }
         }
 
         internalReadFromLedger(ledger, opReadEntry);
     }
 
     private void internalReadFromLedger(LedgerHandle ledger, OpReadEntry opReadEntry) {
         // Perform the read
         long firstEntry = opReadEntry.readPosition.getEntryId();
 
         if (firstEntry > ledger.getLastAddConfirmed()) {
             log.debug("[{}] No more messages to read from ledger={} lastEntry={} readEntry={}",
                     va(name, ledger.getId(), ledger.getLastAddConfirmed(), firstEntry));
 
             if (ledger.getId() != currentLedger.getId()) {
                 // Cursor was placed past the end of one ledger, move it to the
                 // beginning of the next ledger
                 Long nextLedgerId = ledgers.ceilingKey(ledger.getId() + 1);
                 opReadEntry.nextReadPosition = new PositionImpl(nextLedgerId, 0);
             }
 
             opReadEntry.emptyResponse();
             return;
         }
 
         long lastEntry = min(firstEntry + opReadEntry.count - 1, ledger.getLastAddConfirmed());
 
         long expectedEntries = lastEntry - firstEntry + 1;
         opReadEntry.entries = Lists.newArrayListWithExpectedSize((int) expectedEntries);
 
         log.debug("[{}] Reading entries from ledger {} - first={} last={}",
                 va(name, ledger.getId(), firstEntry, lastEntry));
         ledger.asyncReadEntries(firstEntry, lastEntry, this, opReadEntry);
     }
 
     @Override
     public void readComplete(int rc, LedgerHandle lh, Enumeration<LedgerEntry> entriesEnum, Object ctx) {
         OpReadEntry opReadEntry = (OpReadEntry) ctx;
 
         if (rc != BKException.Code.OK) {
             log.warn("[{}] read failed from ledger {} at position:{}", va(name, lh.getId(), opReadEntry.readPosition));
             opReadEntry.failed(new ManagedLedgerException(BKException.create(rc)));
             return;
         }
 
         List<Entry> entries = opReadEntry.entries;
         while (entriesEnum.hasMoreElements())
             entries.add(new EntryImpl(entriesEnum.nextElement()));
 
         PositionImpl lastPosition = (PositionImpl) entries.get(entries.size() - 1).getPosition();
         long lastEntry = lastPosition.getEntryId();
 
         // Get the "next read position", we need to advance the position taking
         // care of ledgers boundaries
         PositionImpl nextReadPosition;
         if (lastEntry < lh.getLastAddConfirmed()) {
             nextReadPosition = new PositionImpl(lh.getId(), lastEntry + 1);
         } else {
             // Move to next ledger
             Long nextLedgerId = ledgers.ceilingKey(lh.getId() + 1);
             if (nextLedgerId == null) {
                 // We are already in the last ledger
                 nextReadPosition = new PositionImpl(lh.getId(), lastEntry + 1);
             } else {
                 nextReadPosition = new PositionImpl(nextLedgerId, 0);
             }
         }
 
         opReadEntry.nextReadPosition = nextReadPosition;
         opReadEntry.succeeded();
     }
 
     @Override
     public synchronized void openComplete(int rc, LedgerHandle ledger, Object ctx) {
         OpReadEntry opReadEntry = (OpReadEntry) ctx;
 
         if (rc != BKException.Code.OK) {
             opReadEntry.failed(new ManagedLedgerException(BKException.create(rc)));
             return;
         }
 
         log.debug("[{}] Successfully opened ledger {} for reading", name, ledger.getId());
         ledgerCache.put(ledger.getId(), ledger);
         internalReadFromLedger(ledger, opReadEntry);
     }
 
     synchronized boolean hasMoreEntries(PositionImpl position) {
         if (position.getLedgerId() == currentLedger.getId()) {
             // If we are reading from the last ledger, use the
             // LedgerHandle metadata
             return position.getEntryId() <= currentLedger.getLastAddConfirmed();
         } else if (currentLedger.getLastAddConfirmed() >= 0) {
             // We have entries in the current ledger and we are reading from an
             // older ledger
             return true;
         } else {
             // At this point, currentLedger is empty, we need to check in the
             // older ledgers for entries past the current position
             LedgerInfo ls = ledgers.get(position.getLedgerId());
             if (ls == null) {
                 // The cursor haven't been initialized yet
                 checkArgument(position.getLedgerId() == -1);
                 return true;
             } else if (position.getEntryId() < ls.getEntries()) {
                 // There are still entries to read in the current reading ledger
                 return true;
             } else {
                 for (LedgerInfo stat : ledgers.tailMap(position.getLedgerId(), false).values()) {
                     if (stat.getEntries() > 0)
                         return true;
                 }
 
                 return false;
             }
         }
     }
 
     synchronized void updateCursor(ManagedCursorImpl cursor, PositionImpl oldPosition, PositionImpl newPosition) {
         // checkFenced();
         // First update the metadata store, so that if we don't succeed we have
         // not changed any other state
         // store.updateConsumer(name, cursor.getName(), newPosition);
         // PositionImpl oldPosition =
         // cursor.setAcknowledgedPosition(newPosition);
         cursors.cursorUpdated(cursor);
 
         if (oldPosition.getLedgerId() != newPosition.getLedgerId()) {
             // Only trigger a trimming when switching to the next ledger
             trimConsumedLedgersInBackground();
         }
     }
 
     void trimConsumedLedgersInBackground() {
         executor.execute(new Runnable() {
             public void run() {
                 internalTrimConsumedLedgers();
             }
         });
     }
 
     /**
      * Checks whether there are ledger that have been fully consumed and deletes
      * them
      * 
      * @throws Exception
      */
     void internalTrimConsumedLedgers() {
         // Ensure only one trimming operation is active
         trimmerMutex.lock();
 
         List<LedgerInfo> ledgersToDelete = Lists.newArrayList();
 
         synchronized (this) {
             log.debug("[{}] Start TrimConsumedLedgers. ledgers={} entries={}",
                     va(name, ledgers.size(), totalSize.get()));
 
             long slowestReaderLedgerId = -1;
             if (cursors.isEmpty()) {
                 // At this point the lastLedger will be pointing to the
                 // ledger that has just been closed, therefore the +1 to
                 // include lastLedger in the trimming.
                 slowestReaderLedgerId = currentLedger.getId() + 1;
             } else {
                 slowestReaderLedgerId = cursors.getSlowestReaderPosition().getLedgerId();
             }
 
             for (LedgerInfo ls : ledgers.headMap(slowestReaderLedgerId, false).values()) {
                 if (ls.getLedgerId() == currentLedger.getId()) {
                     break;
                 }
 
                 ledgersToDelete.add(ls);
                 ledgerCache.invalidate(ls.getLedgerId());
             }
 
             if (ledgersToDelete.isEmpty()) {
                 trimmerMutex.unlock();
                 return;
             }
         }
 
         // Delete the ledgers _without_ holding the lock on 'this'
         long removedCount = 0;
         long removedSize = 0;
 
         for (LedgerInfo ls : ledgersToDelete) {
             log.info("[{}] Removing ledger {}", name, ls.getLedgerId());
             try {
                 bookKeeper.deleteLedger(ls.getLedgerId());
                 ++removedCount;
                 removedSize += ls.getSize();
             } catch (BKNoSuchLedgerExistsException e) {
                 log.warn("[{}] Ledger was already deleted {}", name, ls.getLedgerId());
             } catch (Exception e) {
                 log.error("[{}] Error deleting ledger {}", name, ls.getLedgerId());
                 trimmerMutex.unlock();
                 return;
             }
         }
 
         // Update metadata
         synchronized (this) {
             numberOfEntries.addAndGet(-removedCount);
             totalSize.addAndGet(-removedSize);
             for (LedgerInfo ls : ledgersToDelete) {
                 ledgers.remove(ls.getLedgerId());
             }
 
             if (state == State.CreatingLedger) {
                 // Give up now and schedule a new trimming
                 executor.schedule(new Runnable() {
                     public void run() {
                         internalTrimConsumedLedgers();
                     }
                 }, 100, TimeUnit.MILLISECONDS);
 
                 trimmerMutex.unlock();
                 return;
             }
 
             ledgersListMutex.lock();
 
             log.debug("Updating of ledgers list after trimming");
             ManagedLedgerInfo mlInfo = ManagedLedgerInfo.newBuilder().addAllLedgerInfo(ledgers.values()).build();
             store.asyncUpdateLedgerIds(name, mlInfo, ledgersVersion, new MetaStoreCallback<Void>() {
                 public void operationComplete(Void result, Version version) {
                     log.info("[{}] End TrimConsumedLedgers. ledgers={} entries={}",
                             va(name, ledgers.size(), totalSize.get()));
                     ledgersVersion = version;
                     ledgersListMutex.unlock();
                     trimmerMutex.unlock();
                 }
 
                 public void operationFailed(MetaStoreException e) {
                     log.error("[{}] Failed to update the list of ledgers after trimming", name, e);
                     ledgersListMutex.unlock();
                     trimmerMutex.unlock();
                 }
             });
         }
     }
 
     /**
      * Delete this ManagedLedger completely from the system.
      * 
      * @throws Exception
      */
     void delete() throws InterruptedException, ManagedLedgerException {
         close();
 
         synchronized (this) {
             checkFenced();
 
             try {
                 for (LedgerInfo ls : ledgers.values()) {
                     log.debug("[{}] Deleting ledger {}", name, ls);
                     try {
                         bookKeeper.deleteLedger(ls.getLedgerId());
                     } catch (BKNoSuchLedgerExistsException e) {
                         log.warn("[{}] Ledger {} not found when deleting it", name, ls.getLedgerId());
                     }
                 }
             } catch (BKException e) {
                 throw new ManagedLedgerException(e);
             }
 
             store.removeManagedLedger(name);
         }
     }
 
     synchronized long getNumberOfEntries(PositionImpl position) {
         long count = 0;
         // First count the number of unread entries in the ledger pointed by
         // position
         count += ledgers.get(position.getLedgerId()).getEntries() - position.getEntryId();
 
         // Then, recur all the next ledgers and sum all the entries they contain
         for (LedgerInfo ls : ledgers.tailMap(position.getLedgerId(), false).values()) {
             count += ls.getEntries();
         }
 
         // Last add the entries in the current ledger
         if (state != State.ClosedLedger && currentLedger != null) {
             count += currentLedger.getLastAddConfirmed() + 1;
         }
 
         return count;
     }
 
     /**
      * Skip a specified number of entries and return the resulting position.
      * 
      * @param startPosition
      *            the current position
      * @param entriesToSkip
      *            the numbers of entries to skip
      * @return the new position
      */
     synchronized PositionImpl skipEntries(PositionImpl startPosition, int entriesToSkip) {
         log.debug("[{}] Skipping {} entries from position {}", va(name, entriesToSkip, startPosition));
         long ledgerId = startPosition.getLedgerId();
         entriesToSkip += startPosition.getEntryId();
 
         while (entriesToSkip > 0) {
             if (ledgerId == currentLedger.getId()) {
                 checkArgument(entriesToSkip <= (currentLedger.getLastAddConfirmed() + 1));
                 return new PositionImpl(ledgerId, entriesToSkip);
             } else {
                 LedgerInfo ledger = ledgers.get(ledgerId);
 
                 if (entriesToSkip < ledger.getEntries()) {
                     return new PositionImpl(ledgerId, entriesToSkip);
                 } else {
                     // Move to next ledger
                     entriesToSkip -= ledger.getEntries();
                     ledgerId = ledgers.ceilingKey(ledgerId + 1);
                 }
             }
         }
 
         return new PositionImpl(ledgerId, 0);
     }
 
     /**
      * Validate whether a specified position is valid for the current managed
      * ledger.
      * 
      * @param position
      *            the position to validate
      * @return true if the position is valid, false otherwise
      */
     synchronized boolean isValidPosition(PositionImpl position) {
         if (position.getLedgerId() == currentLedger.getId()) {
             return position.getEntryId() <= (currentLedger.getLastAddConfirmed() + 1);
         } else {
             // Look in the ledgers map
             LedgerInfo ls = ledgers.get(position.getLedgerId());
             if (ls == null)
                 return false;
 
             return position.getEntryId() <= ls.getEntries();
         }
     }
 
     private boolean currentLedgerIsFull() {
         return currentLedgerEntries >= config.getMaxEntriesPerLedger()
                 || currentLedgerSize >= (config.getMaxSizePerLedgerMb() * MegaByte);
     }
 
     public Iterable<LedgerInfo> getLedgersInfo() {
         return ledgers.values();
     }
 
     Executor getExecutor() {
         return executor;
     }
 
     /**
      * Throws an exception if the managed ledger has been previously fenced
      * 
      * @throws ManagedLedgerException
      */
     private void checkFenced() throws ManagedLedgerException {
         if (state == State.Fenced) {
             log.error("[{}] Attempted to use a fenced managed ledger", name);
             throw new ManagedLedgerFencedException();
         }
     }
 
     private void checkManagedLedgerIsOpen() throws ManagedLedgerException {
         if (state == State.Closed) {
             throw new ManagedLedgerException("ManagedLedger " + name + " has already been closed");
         }
     }
 
     synchronized void setFenced() {
         state = State.Fenced;
     }
 
     MetaStore getStore() {
         return store;
     }
 
     static interface ManagedLedgerInitializeLedgerCallback {
         public void initializeComplete();
 
         public void initializeFailed(ManagedLedgerException e);
     }
 
     private static final Logger log = LoggerFactory.getLogger(ManagedLedgerImpl.class);
 
 }
