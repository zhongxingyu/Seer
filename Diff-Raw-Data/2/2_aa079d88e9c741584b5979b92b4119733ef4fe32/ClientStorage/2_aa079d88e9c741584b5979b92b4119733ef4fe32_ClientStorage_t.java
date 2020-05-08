 /*
  * Copyright 2008-2010 Amazon Technologies, Inc. or its affiliates.
  * Amazon, Amazon.com and Carbonado are trademarks or registered trademarks
  * of Amazon Technologies, Inc. or its affiliates.  All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.amazon.carbonado.repo.dirmi;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.cojen.dirmi.Pipe;
 
 import com.amazon.carbonado.FetchException;
 import com.amazon.carbonado.PersistException;
 import com.amazon.carbonado.Query;
 import com.amazon.carbonado.Repository;
 import com.amazon.carbonado.RepositoryException;
 import com.amazon.carbonado.Storable;
 import com.amazon.carbonado.Storage;
 import com.amazon.carbonado.SupportException;
 import com.amazon.carbonado.Trigger;
 
 import com.amazon.carbonado.filter.Filter;
 import com.amazon.carbonado.filter.FilterValues;
 
 import com.amazon.carbonado.info.StorableIntrospector;
 import com.amazon.carbonado.info.StorableProperty;
 
 import com.amazon.carbonado.sequence.SequenceValueProducer;
 
 import com.amazon.carbonado.gen.DelegateStorableGenerator;
 import com.amazon.carbonado.gen.DelegateSupport;
 import com.amazon.carbonado.gen.MasterFeature;
 
 import com.amazon.carbonado.qe.OrderingList;
 
 import com.amazon.carbonado.spi.TriggerManager;
 
 import com.amazon.carbonado.util.QuickConstructorGenerator;
 
 /**
  * 
  *
  * @author Brian S O'Neill
  */
 class ClientStorage<S extends Storable> implements Storage<S>, DelegateSupport<S> {
     private final Class<S> mType;
     private final ClientRepository mRepository;
     private final TriggerManager<S> mTriggerManager;
     private final InstanceFactory mInstanceFactory;
     private final ClientQueryFactory<S> mQueryFactory;
     private final boolean mReadStartMarker;
 
    private volatile StorageProxy<S> mStorageProxy;
 
     ClientStorage(Class<S> type, ClientRepository repo, RemoteStorageTransport transport)
         throws SupportException, RepositoryException
     {
         mType = type;
         mRepository = repo;
         mTriggerManager = new TriggerManager<S>(type, null);
 
         EnumSet<MasterFeature> features = EnumSet.noneOf(MasterFeature.class);
 
         Class<? extends S> delegateStorableClass =
             DelegateStorableGenerator.getDelegateClass(type, features);
 
         mInstanceFactory = QuickConstructorGenerator
             .getInstance(delegateStorableClass, InstanceFactory.class);
 
         mQueryFactory = new ClientQueryFactory<S>(type, this);
 
         mReadStartMarker = transport.getProtocolVersion() >= 1;
 
         // Set mStorage and determine supported independent properties.
         reconnect(transport);
     }
 
     public Class<S> getStorableType() {
         return mType;
     }
 
     public S prepare() {
         return (S) mInstanceFactory.instantiate(this);
     }
 
     public Query<S> query() throws FetchException {
         return mQueryFactory.query();
     }
 
     public Query<S> query(String filter) throws FetchException {
         return mQueryFactory.query(filter);
     }
 
     public Query<S> query(Filter<S> filter) throws FetchException {
         return mQueryFactory.query(filter);
     }
 
     public void truncate() throws PersistException {
         try {
             mStorageProxy.mStorage.truncate(mRepository.localTransactionScope().getTxn());
         } catch (PersistException e) {
             throw e;
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new PersistException(e);
         }
     }
 
     public boolean addTrigger(Trigger<? super S> trigger) {
         return mTriggerManager.addTrigger(trigger);
     }
 
     public boolean removeTrigger(Trigger<? super S> trigger) {
         return mTriggerManager.removeTrigger(trigger);
     }
 
     public Repository getRootRepository() {
         return mRepository;
     }
 
     public boolean isPropertySupported(String propertyName) {
         return mStorageProxy.mSupportedProperties.contains(propertyName);
     }
 
     public Trigger<? super S> getInsertTrigger() {
         return mTriggerManager.getInsertTrigger();
     }
 
     public Trigger<? super S> getUpdateTrigger() {
         return mTriggerManager.getUpdateTrigger();
     }
 
     public Trigger<? super S> getDeleteTrigger() {
         return mTriggerManager.getDeleteTrigger();
     }
 
     public Trigger<? super S> getLoadTrigger() {
         return mTriggerManager.getLoadTrigger();
     }
 
     public void locallyDisableLoadTrigger() {
         mTriggerManager.locallyDisableLoad();
     }
 
     public void locallyEnableLoadTrigger() {
         mTriggerManager.locallyEnableLoad();
     }
 
     public SequenceValueProducer getSequenceValueProducer(String name) throws PersistException {
         throw new PersistException("unsupported");
     }
 
     public boolean doTryLoad(S storable) throws FetchException {
         try {
             RemoteTransaction txn = mRepository.localTransactionScope().getTxn();
             if (txn instanceof FailedTransaction) {
                 throw new FetchException("Transaction invalid due to a reconnect");
             }
 
             StorageProxy<S> proxy = mStorageProxy;
 
             Pipe pipe = proxy.mStorage.tryLoad(txn, null);
             try {
                 proxy.mWriter.writeForLoad(storable, pipe.getOutputStream());
                 RepositoryException ex = (RepositoryException) pipe.readThrowable();
                 if (ex != null) {
                     throw ex.toFetchException();
                 }
                 if (pipe.readBoolean()) {
                     storable.readFrom(pipe.getInputStream());
                     return true;
                 }
                 return false;
             } finally {
                 pipe.close();
             }
         } catch (FetchException e) {
             throw e;
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new FetchException(e);
         }
     }
 
     public boolean doTryInsert(S storable) throws PersistException {
         try {
             RemoteTransaction txn = mRepository.localTransactionScope().getTxn();
             if (txn instanceof FailedTransaction) {
                 throw new PersistException("Transaction invalid due to a reconnect");
             }
 
             StorageProxy<S> proxy = mStorageProxy;
 
             Pipe pipe = proxy.mStorage.tryInsert(txn, null);
             try {
                 proxy.mWriter.writeForInsert(storable, pipe.getOutputStream());
                 RepositoryException ex = (RepositoryException) pipe.readThrowable();
                 if (ex != null) {
                     throw ex.toPersistException();
                 }
                 int result = pipe.readByte();
                 switch (result) {
                 case RemoteStorageServer.STORABLE_UNCHANGED:
                     return true;
                 case RemoteStorageServer.STORABLE_CHANGED:
                     storable.readFrom(pipe.getInputStream());
                     return true;
                 default:
                     return false;
                 }
             } finally {
                 pipe.close();
             }
         } catch (PersistException e) {
             throw e;
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new PersistException(e);
         }
     }
 
     public boolean doTryUpdate(S storable) throws PersistException {
         try {
             RemoteTransaction txn = mRepository.localTransactionScope().getTxn();
             if (txn instanceof FailedTransaction) {
                 throw new PersistException("Transaction invalid due to a reconnect");
             }
 
             StorageProxy<S> proxy = mStorageProxy;
 
             Pipe pipe = proxy.mStorage.tryUpdate(txn, null);
             try {
                 proxy.mWriter.writeForUpdate(storable, pipe.getOutputStream());
                 RepositoryException ex = (RepositoryException) pipe.readThrowable();
                 if (ex != null) {
                     throw ex.toPersistException();
                 }
                 int result = pipe.readByte();
                 switch (result) {
                 case RemoteStorageServer.STORABLE_UNCHANGED:
                     return true;
                 case RemoteStorageServer.STORABLE_CHANGED:
                     storable.readFrom(pipe.getInputStream());
                     return true;
                 default:
                     return false;
                 }
             } finally {
                 pipe.close();
             }
         } catch (PersistException e) {
             throw e;
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new PersistException(e);
         }
     }
 
     public boolean doTryDelete(S storable) throws PersistException {
         try {
             RemoteTransaction txn = mRepository.localTransactionScope().getTxn();
             if (txn instanceof FailedTransaction) {
                 throw new PersistException("Transaction invalid due to a reconnect");
             }
 
             StorageProxy<S> proxy = mStorageProxy;
 
             Pipe pipe = proxy.mStorage.tryDelete(txn, null);
             try {
                 proxy.mWriter.writeForDelete(storable, pipe.getOutputStream());
                 RepositoryException ex = (RepositoryException) pipe.readThrowable();
                 if (ex != null) {
                     throw ex.toPersistException();
                 }
                 return pipe.readBoolean();
             } finally {
                 pipe.close();
             }
         } catch (PersistException e) {
             throw e;
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new PersistException(e);
         }
     }
 
     long queryCount(FilterValues<S> fv) throws FetchException {
         try {
             RemoteTransaction txn = mRepository.localTransactionScope().getTxn();
             return mStorageProxy.mStorage.queryCount(fv, txn);
         } catch (FetchException e) {
             throw e;
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new FetchException(e);
         }
     }
 
     ClientCursor<S> queryFetch(FilterValues fv, OrderingList orderBy, Long from, Long to)
         throws FetchException
     {
         try {
             RemoteTransaction txn = mRepository.localTransactionScope().getTxn();
             Pipe pipe = mStorageProxy.mStorage.queryFetch(fv, orderBy, from, to, txn, null);
             ClientCursor<S> cursor = new ClientCursor<S>(this, pipe);
             if (txn != null) {
                 // Block until server has created it's cursor against the
                 // transaction we just passed to it.
                 if (mReadStartMarker) {
                     if (pipe.readByte() != RemoteStorageServer.CURSOR_START) {
                         try {
                             cursor.close();
                         } catch (FetchException e) {
                             // Ignore.
                         }
                         throw new FetchException("Cursor protocol error");
                     }
                 } else {
                     // Fallback to potentially slower method.
                     cursor.hasNext();
                 }
             }
             return cursor;
         } catch (FetchException e) {
             throw e;
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new FetchException(e);
         }
     }
 
     S queryLoadOne(FilterValues fv) throws FetchException {
         try {
             RemoteTransaction txn = mRepository.localTransactionScope().getTxn();
             if (txn instanceof FailedTransaction) {
                 throw new FetchException("Transaction invalid due to a reconnect");
             }
 
             Pipe pipe = mStorageProxy.mStorage.queryLoadOne(fv, txn, null);
             try {
                 RepositoryException ex = (RepositoryException) pipe.readThrowable();
                 if (ex != null) {
                     throw ex.toFetchException();
                 }
                 S storable = prepare();
                 storable.readFrom(pipe.getInputStream());
                 return storable;
             } finally {
                 pipe.close();
             }
         } catch (FetchException e) {
             throw e;
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new FetchException(e);
         }
     }
 
     S queryTryLoadOne(FilterValues fv) throws FetchException {
         try {
             RemoteTransaction txn = mRepository.localTransactionScope().getTxn();
             if (txn instanceof FailedTransaction) {
                 throw new FetchException("Transaction invalid due to a reconnect");
             }
 
             Pipe pipe = mStorageProxy.mStorage.queryTryLoadOne(fv, txn, null);
             try {
                 RepositoryException ex = (RepositoryException) pipe.readThrowable();
                 if (ex != null) {
                     throw ex.toFetchException();
                 }
                 if (pipe.readBoolean()) {
                     S storable = prepare();
                     storable.readFrom(pipe.getInputStream());
                     return storable;
                 }
                 return null;
             } finally {
                 pipe.close();
             }
         } catch (FetchException e) {
             throw e;
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new FetchException(e);
         }
     }
 
     void queryDeleteOne(FilterValues fv) throws PersistException {
         try {
             mStorageProxy.mStorage.queryDeleteOne
                 (fv, mRepository.localTransactionScope().getTxn());
         } catch (FetchException e) {
             throw e.toPersistException();
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new PersistException(e);
         }
     }
 
     boolean queryTryDeleteOne(FilterValues fv) throws PersistException {
         try {
             return mStorageProxy.mStorage.queryTryDeleteOne
                 (fv, mRepository.localTransactionScope().getTxn());
         } catch (FetchException e) {
             throw e.toPersistException();
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new PersistException(e);
         }
     }
 
     void queryDeleteAll(FilterValues fv) throws PersistException {
         try {
             mStorageProxy.mStorage.queryDeleteAll
                 (fv, mRepository.localTransactionScope().getTxn());
         } catch (FetchException e) {
             throw e.toPersistException();
         } catch (RuntimeException e) {
             throw e;
         } catch (Exception e) {
             throw new PersistException(e);
         }
     }
 
     String queryPrintNative(FilterValues fv, OrderingList orderBy, int indentLevel)
         throws FetchException
     {
         return mStorageProxy.mStorage.queryPrintNative(fv, orderBy, indentLevel);
     }
 
     String queryPrintPlan(FilterValues fv, OrderingList orderBy, int indentLevel)
         throws FetchException
     {
         return mStorageProxy.mStorage.queryPrintPlan(fv, orderBy, indentLevel);
     }
 
     public static interface InstanceFactory {
         Storable instantiate(DelegateSupport support);
     }
 
     void reconnect(RemoteStorageTransport transport) throws RepositoryException {
         RemoteStorage storage = transport.getRemoteStorage();
         StorableWriter<S> writer = ReconstructedCache.THE.writerFor(mType, transport.getLayout());
 
         List<String> indieList = null;
         for (StorableProperty<S> property :
                  StorableIntrospector.examine(mType).getAllProperties().values())
         {
             if (property.isIndependent()) {
                 if (indieList == null) {
                     indieList = new ArrayList<String>();
                 }
                 indieList.add(property.getName());
             }
         }
 
         Set<String> supported;
         if (indieList == null) {
             supported = Collections.emptySet();
         } else {
             supported = storage.getPropertySupport(indieList.toArray(new String[0]));
         }
 
         mStorageProxy = new StorageProxy<S>(storage, writer, supported);
     }
 
     // Allows several objects to be swapped-in atomically.
     private static final class StorageProxy<S extends Storable> {
         final RemoteStorage mStorage;
         final StorableWriter<S> mWriter;
         // Cache of independent property support.
         final Set<String> mSupportedProperties;
 
         StorageProxy(RemoteStorage storage, StorableWriter<S> writer, Set<String> supported) {
             mStorage = storage;
             mWriter = writer;
             mSupportedProperties = supported;
         }
     }
 }
