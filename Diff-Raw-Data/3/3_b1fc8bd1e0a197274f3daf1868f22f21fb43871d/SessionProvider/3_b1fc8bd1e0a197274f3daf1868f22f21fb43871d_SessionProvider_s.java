 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.ipc.session.infinispan;
 
 import java.util.UUID;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.infinispan.Cache;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 
 import de.cosmocode.palava.concurrent.BackgroundScheduler;
 import de.cosmocode.palava.core.Registry;
 import de.cosmocode.palava.core.lifecycle.Disposable;
 import de.cosmocode.palava.core.lifecycle.Initializable;
 import de.cosmocode.palava.core.lifecycle.LifecycleException;
 import de.cosmocode.palava.ipc.IpcConnection;
 import de.cosmocode.palava.ipc.IpcConnectionDestroyEvent;
 import de.cosmocode.palava.ipc.IpcSession;
 import de.cosmocode.palava.ipc.IpcSessionConfig;
 import de.cosmocode.palava.ipc.IpcSessionNotAttachedException;
 import de.cosmocode.palava.ipc.IpcSessionProvider;
 import de.cosmocode.palava.ipc.session.infinispan.Session.Key;
 import de.cosmocode.palava.jmx.MBeanService;
 
 /**
  * Session provider baced by a {@link Cache}.
  *
  * @author Tobias Sarnowski
  */
 @Singleton
 final class SessionProvider implements IpcSessionProvider, Initializable, Runnable,
     IpcConnectionDestroyEvent, Disposable, SessionProviderMBean {
 
     private static final Logger LOG = LoggerFactory.getLogger(SessionProvider.class);
 
     private final Cache<Key, IpcSession> cache;
     
     private final Registry registry;
     private final MBeanService mBeanService;
     
     private final ScheduledExecutorService scheduler;
     
     private long initialCheckDelay = 1;
     private long checkPeriod = 15;
     private TimeUnit checkPeriodUnit = TimeUnit.MINUTES;
     
     private final long expirationTime;
     private final TimeUnit expirationTimeUnit;
 
     @Inject
     @SuppressWarnings("unchecked")
     public SessionProvider(
         Registry registry,
         MBeanService mBeanService,
        @SessionCache Cache<?, ?> cache,
         @BackgroundScheduler ScheduledExecutorService scheduler,
         @Named(IpcSessionConfig.EXPIRATION_TIME) long time,
         @Named(IpcSessionConfig.EXPIRATION_TIME_UNIT) TimeUnit timeUnit) {
         this.registry = Preconditions.checkNotNull(registry, "Registry");
         this.mBeanService = Preconditions.checkNotNull(mBeanService, "MBeanService");
         this.cache = (Cache<Session.Key, IpcSession>) Preconditions.checkNotNull(cache, "Cache");
         this.scheduler = Preconditions.checkNotNull(scheduler, "Scheduler");
         this.expirationTime = time;
         this.expirationTimeUnit = Preconditions.checkNotNull(timeUnit, "TimeUnit");
     }
 
     @Inject(optional = true)
     void setInitialCheckDelay(@Named(InfinispanSessionConfig.INITIAL_CHECK_DELAY) long initialCheckDelay) {
         this.initialCheckDelay = initialCheckDelay;
     }
 
     @Inject(optional = true)
     void setCheckPeriod(@Named(InfinispanSessionConfig.CHECK_PERIOD) long checkPeriod) {
         this.checkPeriod = checkPeriod;
     }
 
     @Inject(optional = true)
     void setCheckPeriodUnit(@Named(InfinispanSessionConfig.CHECK_PERIOD_UNIT) TimeUnit checkPeriodUnit) {
         this.checkPeriodUnit = Preconditions.checkNotNull(checkPeriodUnit, "CheckPeriodUnit");
     }
     
     @Override
     public void initialize() throws LifecycleException {
         registry.register(IpcConnectionDestroyEvent.class, this);
         
         final String unit = checkPeriodUnit.name().toLowerCase();
         LOG.info("Scheduling {} in {} {} and then periodically every {} {}", new Object[] {
             this, initialCheckDelay, unit, checkPeriod, unit
         });
         
         scheduler.scheduleAtFixedRate(this, initialCheckDelay, checkPeriod, checkPeriodUnit);
         
         mBeanService.register(this);
     }
 
     @Override
     public IpcSession getSession(String sessionId, String identifier) {
         IpcSession session = cache.get(new Key(sessionId, identifier));
         if (session != null && session.isExpired()) {
             expireSession(session);
             session = null;
         }
         if (session == null) {
             session = new Session(UUID.randomUUID().toString(), identifier, expirationTime, expirationTimeUnit);
             LOG.info("Created {}", session);
         }
         return session;
     }
 
     @Override
     public void run() {
         for (IpcSession session : cache.values()) {
             if (session.isExpired()) {
                 expireSession(session);
             }
         }
     }
 
     private void expireSession(IpcSession session) {
         LOG.info("Expiring {}", session);
         try {
             cache.removeAsync(Key.get(session));
         } finally {
             session.clear();
         }
     }
 
     @Override
     public void eventIpcConnectionDestroy(IpcConnection connection) {
         final IpcSession session;
         
         try {
             session = connection.getSession();
         } catch (IpcSessionNotAttachedException e) {
             return;
         }
         
         cache.put(Key.get(session), session);
     }
 
     @Override
     public int getCurrentNumberOfEntries() {
         return cache.getAdvancedCache().getStats().getCurrentNumberOfEntries();
     }
 
     @Override
     public long getEvictions() {
         return cache.getAdvancedCache().getStats().getEvictions();
     }
 
     @Override
     public long getHits() {
         return cache.getAdvancedCache().getStats().getHits();
     }
 
     @Override
     public long getMisses() {
         return cache.getAdvancedCache().getStats().getMisses();
     }
 
     @Override
     public long getRemoveHits() {
         return cache.getAdvancedCache().getStats().getRemoveHits();
     }
 
     @Override
     public long getRemoveMisses() {
         return cache.getAdvancedCache().getStats().getRemoveMisses();
     }
 
     @Override
     public long getRetrievals() {
         return cache.getAdvancedCache().getStats().getRetrievals();
     }
 
     @Override
     public long getStores() {
         return cache.getAdvancedCache().getStats().getStores();
     }
 
     @Override
     public long getTimeSinceStart() {
         return cache.getAdvancedCache().getStats().getTimeSinceStart();
     }
 
     @Override
     public long getTotalNumberOfEntries() {
         return cache.getAdvancedCache().getStats().getTotalNumberOfEntries();
     }
 
     @Override
     public void dispose() throws LifecycleException {
         try {
             mBeanService.unregister(this);
         } finally {
             registry.remove(this);
         }
     }
 
     @Override
     public String toString() {
         return "SessionProvider {" + "cache=" + cache + '}';
     }
 
 }
