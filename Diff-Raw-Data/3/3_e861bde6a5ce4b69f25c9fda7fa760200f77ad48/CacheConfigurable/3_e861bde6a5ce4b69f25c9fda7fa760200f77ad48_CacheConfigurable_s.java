 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.paxxis.cornerstone.cache;
 
 import org.infinispan.config.Configuration;
 import org.infinispan.config.FluentConfiguration;
 import org.infinispan.config.FluentConfiguration.ClusteringConfig;
 import org.infinispan.config.FluentConfiguration.EvictionConfig;
 import org.infinispan.config.FluentConfiguration.ExpirationConfig;
 import org.infinispan.config.FluentConfiguration.HashConfig;
 import org.infinispan.config.FluentConfiguration.L1Config;
 import org.infinispan.config.FluentConfiguration.LockingConfig;
 import org.infinispan.config.FluentConfiguration.StoreAsBinaryConfig;
 import org.infinispan.config.FluentConfiguration.TransactionConfig;
 import org.infinispan.eviction.EvictionStrategy;
 import org.infinispan.eviction.EvictionThreadPolicy;
 import org.infinispan.transaction.lookup.GenericTransactionManagerLookup;
 import org.infinispan.transaction.lookup.TransactionManagerLookup;
 import org.infinispan.util.concurrent.IsolationLevel;
 
 import com.paxxis.cornerstone.service.BeanNameAwareConfigurable;
 
 /**
  * Configurable for caches.  Package-private as only Cornerstone cache's should extend this class
  * 
  * @author Matthew Pflueger
  */
 abstract class CacheConfigurable extends BeanNameAwareConfigurable {
     
     private IsolationLevel isolationLevel = null;
     private Long lockAcquisitionTimeout = null;
     private Boolean writeSkewCheck = null;
     private Boolean useLockStriping = null;
     private Integer concurrencyLevel = null;
     
     private Boolean invocationBatching = null;
 
     private Boolean storeAsBinary = null;
     private Boolean storeKeysAsBinary = null;
     private Boolean storeValuesAsBinary = null;
     
     private String transactionManagerLookupClass = null;
     
     private Boolean syncCommitPhase = null;
     private Boolean syncRollbackPhase = null;
     private Boolean useEagerLocking = null;
     private Boolean eagerLockSingleNode = null;
     private Boolean useSynchronization = null;
     private Integer cacheStopTimeout = null;
     
     private Boolean deadlockDetectionEnabled = null;
     private Long deadlockDetectionSpinDuration = null;
 
 
     private Boolean async = null; 
             
     private Boolean l1CacheEnabled = null; 
     private Long l1Lifespan = null;
     private Boolean l1OnRehash = null;
     private Integer l1InvalidationThreshold = null; 
     
     private Integer numOwners = null;
     private Long rehashWaitTime = null;
     private Long rehashRpcTimeout = null;
     private Boolean rehashEnabled = null;
 
     private EvictionStrategy evictionStrategy = null;
     private Integer evictionMaxEntries = null;
     private EvictionThreadPolicy threadPolicy = null;
 
     private Long expirationLifespan = null;
     private Long expirationMaxIdle = null;
     private Long expirationWakeUpInterval = null;
     
     
     
     
     @Override
     public void initialize() {
         super.initialize();
         defineConfiguration();
     }
     
     protected <T> T choose(T... values) {
         for (T value : values) {
             if (value != null) {
                 return value;
             }
         }
         return null;
     }
     
     protected abstract void defineConfiguration();
 
     protected void defineConfiguration(Configuration defaultConfig) {
     	defineConfiguration(defaultConfig, null);
     }
     
     protected void defineConfiguration(Configuration defConfig, Configuration namedConfig) {
     	applyConfig(defConfig, defConfig);
     	if (namedConfig != null) {
     		applyConfig(namedConfig, defConfig);
     	}
     }
     
     @SuppressWarnings("unchecked")
     private void applyConfig(Configuration defaultConfig, Configuration applyTo) {
         Configuration overrides = new Configuration();
         FluentConfiguration fluentOverrides = overrides.fluent();
         
         LockingConfig locking = fluentOverrides.locking();
         locking.isolationLevel(choose(getIsolationLevel(), defaultConfig.getIsolationLevel()));
         locking.lockAcquisitionTimeout(choose(getLockAcquisitionTimeout(), defaultConfig.getLockAcquisitionTimeout()));
         locking.writeSkewCheck(choose(isWriteSkewCheck(), defaultConfig.isWriteSkewCheck()));
         locking.concurrencyLevel(choose(getConcurrencyLevel(), defaultConfig.getConcurrencyLevel()));
         locking.useLockStriping(choose(isUseLockStriping(), defaultConfig.isUseLockStriping()));
 
         if (choose(isInvocationBatching(), defaultConfig.isInvocationBatchingEnabled(), false)) {
             fluentOverrides.invocationBatching();
         }
         
         
         if (choose(isStoreAsBinary(), defaultConfig.isStoreAsBinary(), false)) {
             StoreAsBinaryConfig storeAsBinaryConfig = fluentOverrides.storeAsBinary();
             storeAsBinaryConfig.storeKeysAsBinary(choose(isStoreKeysAsBinary(), defaultConfig.isStoreKeysAsBinary()));
             storeAsBinaryConfig.storeValuesAsBinary(choose(isStoreValuesAsBinary(), defaultConfig.isStoreValuesAsBinary()));
         } else {
             fluentOverrides.storeAsBinary().disable();
         }
 
 
         TransactionConfig transaction = fluentOverrides.transaction();
         try {
             transaction.transactionManagerLookupClass((Class<? extends TransactionManagerLookup>) 
                    Class.forName(choose(getTransactionManagerLookupClass(), GenericTransactionManagerLookup.class.getName())));
         } catch (ClassNotFoundException cnfe) {
             throw new RuntimeException("Error lookup up transaction manager lookup class", cnfe);
         }
         
         transaction.syncRollbackPhase(choose(isSyncRollbackPhase(), defaultConfig.isSyncRollbackPhase()));
         transaction.syncCommitPhase(choose(isSyncCommitPhase(), defaultConfig.isSyncCommitPhase()));
         transaction.useEagerLocking(choose(isUseEagerLocking(), defaultConfig.isUseEagerLocking()));
         transaction.eagerLockSingleNode(choose(isEagerLockSingleNode(), defaultConfig.isEagerLockSingleNode()));
         transaction.useSynchronization(choose(isUseSynchronization(), defaultConfig.isUseSynchronizationForTransactions()));
         transaction.cacheStopTimeout(choose(getCacheStopTimeout(), defaultConfig.getCacheStopTimeout()));
 
 
         if (choose(isDeadlockDetectionEnabled(), defaultConfig.isDeadlockDetectionEnabled(), false)) {
             fluentOverrides.deadlockDetection().spinDuration(choose(getDeadlockDetectionSpinDuration(), defaultConfig.getDeadlockDetectionSpinDuration()));
         } else {
             fluentOverrides.deadlockDetection().disable();
         }
         
         ClusteringConfig clustering = fluentOverrides.clustering();
         if (choose(isAsync(), true)) { //default is false but we typically want async... 
             clustering.async();
         } else {
             clustering.sync();
         }
         
         if (choose(isL1CacheEnabled(), defaultConfig.isL1CacheEnabled(), false)) {
             L1Config l1Config = clustering.l1();
             l1Config.lifespan(choose(getL1Lifespan(), defaultConfig.getL1Lifespan()));
             l1Config.invalidationThreshold(choose(getL1InvalidationThreshold(), defaultConfig.getL1InvalidationThreshold()));
             l1Config.onRehash(choose(isL1OnRehash(), defaultConfig.isL1OnRehash()));
         } else {
             clustering.l1().disable();
         }
         
         HashConfig hash = clustering.hash();
         hash.rehashEnabled(choose(isRehashEnabled(), defaultConfig.isRehashEnabled())); 
         hash.numOwners(choose(getNumOwners(), defaultConfig.getNumOwners()));
         hash.rehashRpcTimeout(choose(getRehashRpcTimeout(), defaultConfig.getRehashRpcTimeout()));
         hash.rehashWait(choose(getRehashWaitTime(), defaultConfig.getRehashWaitTime()));
         
         EvictionConfig eviction = clustering.eviction();
         eviction.strategy(choose(getEvictionStrategy(), defaultConfig.getEvictionStrategy()));
         eviction.maxEntries(choose(getEvictionMaxEntries(), defaultConfig.getEvictionMaxEntries()));
         
         ExpirationConfig expiration = clustering.expiration();
         expiration.lifespan(choose(getExpirationLifespan(), defaultConfig.getExpirationLifespan()));
         expiration.maxIdle(choose(getExpirationMaxIdle(), defaultConfig.getExpirationMaxIdle()));
         expiration.wakeUpInterval(choose(getExpirationWakeUpInterval(), defaultConfig.getExpirationWakeUpInterval()));
         
 
         applyTo.applyOverrides(fluentOverrides.build());
     }
 
     public IsolationLevel getIsolationLevel() {
         return isolationLevel;
     }
 
     public void setIsolationLevel(IsolationLevel isolationLevel) {
         this.isolationLevel = isolationLevel;
     }
 
     public Long getLockAcquisitionTimeout() {
         return lockAcquisitionTimeout;
     }
 
     public void setLockAcquisitionTimeout(Long lockAcquisitionTimeout) {
         this.lockAcquisitionTimeout = lockAcquisitionTimeout;
     }
 
     public Boolean isWriteSkewCheck() {
         return writeSkewCheck;
     }
 
     public void setWriteSkewCheck(Boolean writeSkewCheck) {
         this.writeSkewCheck = writeSkewCheck;
     }
 
     public Boolean isUseLockStriping() {
         return useLockStriping;
     }
 
     public void setUseLockStriping(Boolean useLockStriping) {
         this.useLockStriping = useLockStriping;
     }
 
     public Integer getConcurrencyLevel() {
         return concurrencyLevel;
     }
 
     public void setConcurrencyLevel(Integer concurrencyLevel) {
         this.concurrencyLevel = concurrencyLevel;
     }
 
     public Boolean isInvocationBatching() {
         return invocationBatching;
     }
 
     public void setInvocationBatching(Boolean invocationBatching) {
         this.invocationBatching = invocationBatching;
     }
 
     public Boolean isStoreAsBinary() {
         return storeAsBinary;
     }
 
     public void setStoreAsBinary(Boolean storeAsBinary) {
         this.storeAsBinary = storeAsBinary;
     }
 
     public Boolean isStoreKeysAsBinary() {
         return storeKeysAsBinary;
     }
 
     public void setStoreKeysAsBinary(Boolean storeKeysAsBinary) {
         this.storeKeysAsBinary = storeKeysAsBinary;
     }
 
     public Boolean isStoreValuesAsBinary() {
         return storeValuesAsBinary;
     }
 
     public void setStoreValuesAsBinary(Boolean storeValuesAsBinary) {
         this.storeValuesAsBinary = storeValuesAsBinary;
     }
 
     public String getTransactionManagerLookupClass() {
         return transactionManagerLookupClass;
     }
 
     public void setTransactionManagerLookupClass(String transactionManagerLookupClass) {
         this.transactionManagerLookupClass = transactionManagerLookupClass;
     }
 
     public Boolean isSyncCommitPhase() {
         return syncCommitPhase;
     }
 
     public void setSyncCommitPhase(Boolean syncCommitPhase) {
         this.syncCommitPhase = syncCommitPhase;
     }
 
     public Boolean isSyncRollbackPhase() {
         return syncRollbackPhase;
     }
 
     public void setSyncRollbackPhase(Boolean syncRollbackPhase) {
         this.syncRollbackPhase = syncRollbackPhase;
     }
 
     public Boolean isUseEagerLocking() {
         return useEagerLocking;
     }
 
     public void setUseEagerLocking(Boolean useEagerLocking) {
         this.useEagerLocking = useEagerLocking;
     }
 
     public Boolean isEagerLockSingleNode() {
         return eagerLockSingleNode;
     }
 
     public void setEagerLockSingleNode(Boolean eagerLockSingleNode) {
         this.eagerLockSingleNode = eagerLockSingleNode;
     }
 
     public Boolean isUseSynchronization() {
         return useSynchronization;
     }
 
     public void setUseSynchronization(Boolean useSynchronization) {
         this.useSynchronization = useSynchronization;
     }
 
     public Integer getCacheStopTimeout() {
         return cacheStopTimeout;
     }
 
     public void setCacheStopTimeout(Integer cacheStopTimeout) {
         this.cacheStopTimeout = cacheStopTimeout;
     }
 
     public Boolean isDeadlockDetectionEnabled() {
         return deadlockDetectionEnabled;
     }
 
     public void setDeadLockDetectionEnabled(Boolean deadlockDetectionEnabled) {
         this.deadlockDetectionEnabled = deadlockDetectionEnabled;
     }
 
     public Long getDeadlockDetectionSpinDuration() {
         return deadlockDetectionSpinDuration;
     }
 
     public void setDeadlockDetectionSpinDuration(Long deadlockDetectionSpinDuration) {
         this.deadlockDetectionSpinDuration = deadlockDetectionSpinDuration;
     }
 
     public Boolean isAsync() {
         return async;
     }
 
     public void setAsync(Boolean async) {
         this.async = async;
     }
 
     public Boolean isL1CacheEnabled() {
         return l1CacheEnabled;
     }
 
     public void setL1CacheEnabled(Boolean l1CacheEnabled) {
         this.l1CacheEnabled = l1CacheEnabled;
     }
 
     public Long getL1Lifespan() {
         return l1Lifespan;
     }
 
     public void setL1Lifespan(Long l1Lifespan) {
         this.l1Lifespan = l1Lifespan;
     }
 
     public Boolean isL1OnRehash() {
         return l1OnRehash;
     }
 
     public void setL1OnRehash(Boolean l1OnRehash) {
         this.l1OnRehash = l1OnRehash;
     }
 
     public Integer getL1InvalidationThreshold() {
         return l1InvalidationThreshold;
     }
 
     public void setL1InvalidationThreshold(Integer l1InvalidationThreshold) {
         this.l1InvalidationThreshold = l1InvalidationThreshold;
     }
 
     public Integer getNumOwners() {
         return numOwners;
     }
 
     public void setNumOwners(Integer numOwners) {
         this.numOwners = numOwners;
     }
 
     public Long getRehashWaitTime() {
         return rehashWaitTime;
     }
 
     public void setRehashWaitTime(Long rehashWaitTime) {
         this.rehashWaitTime = rehashWaitTime;
     }
 
     public Long getRehashRpcTimeout() {
         return rehashRpcTimeout;
     }
 
     public void setRehashRpcTimeout(Long rehashRpcTimeout) {
         this.rehashRpcTimeout = rehashRpcTimeout;
     }
 
     public Boolean isRehashEnabled() {
         return rehashEnabled;
     }
 
     public void setRehashEnabled(Boolean rehashEnabled) {
         this.rehashEnabled = rehashEnabled;
     }
 
     public EvictionStrategy getEvictionStrategy() {
         return evictionStrategy;
     }
 
     public void setEvictionStrategy(EvictionStrategy evictionStrategy) {
         this.evictionStrategy = evictionStrategy;
     }
 
     public Integer getEvictionMaxEntries() {
         return evictionMaxEntries;
     }
 
     public void setEvictionMaxEntries(Integer evictionMaxEntries) {
         this.evictionMaxEntries = evictionMaxEntries;
     }
 
     public EvictionThreadPolicy getThreadPolicy() {
         return threadPolicy;
     }
 
     public void setThreadPolicy(EvictionThreadPolicy threadPolicy) {
         this.threadPolicy = threadPolicy;
     }
 
     @Deprecated
     public Long getLifespan() {
         return getExpirationLifespan();
     }
     
     @Deprecated
     public void setLifespan(Long lifespan) {
         setExpirationLifespan(lifespan);
     }
     
     public Long getExpirationLifespan() {
         return expirationLifespan;
     }
 
     public void setExpirationLifespan(Long expirationLifespan) {
         this.expirationLifespan = expirationLifespan;
     }
 
     @Deprecated
     public Long getMaxIdle() {
         return getExpirationMaxIdle();
     }
     
     @Deprecated
     public void setMaxIdle(Long maxIdle) {
         setExpirationMaxIdle(maxIdle);
     }
     
     public Long getExpirationMaxIdle() {
         return expirationMaxIdle;
     }
 
     public void setExpirationMaxIdle(Long expirationMaxIdle) {
         this.expirationMaxIdle = expirationMaxIdle;
     }
 
     public Long getExpirationWakeUpInterval() {
         return expirationWakeUpInterval;
     }
 
     public void setExpirationWakeUpInterval(Long expirationWakeUpInterval) {
         this.expirationWakeUpInterval = expirationWakeUpInterval;
     }
     
 }
