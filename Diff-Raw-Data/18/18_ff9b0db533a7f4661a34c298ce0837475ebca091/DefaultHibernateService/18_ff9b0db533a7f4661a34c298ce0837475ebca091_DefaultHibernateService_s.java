 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.jpa.hibernate;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import javax.management.InstanceNotFoundException;
 import javax.management.JMException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 
 import org.hibernate.Interceptor;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.event.AutoFlushEventListener;
 import org.hibernate.event.DeleteEventListener;
 import org.hibernate.event.DirtyCheckEventListener;
 import org.hibernate.event.EvictEventListener;
 import org.hibernate.event.FlushEntityEventListener;
 import org.hibernate.event.FlushEventListener;
 import org.hibernate.event.InitializeCollectionEventListener;
 import org.hibernate.event.LoadEventListener;
 import org.hibernate.event.LockEventListener;
 import org.hibernate.event.MergeEventListener;
 import org.hibernate.event.PersistEventListener;
 import org.hibernate.event.PostCollectionRecreateEventListener;
 import org.hibernate.event.PostCollectionRemoveEventListener;
 import org.hibernate.event.PostCollectionUpdateEventListener;
 import org.hibernate.event.PostDeleteEventListener;
 import org.hibernate.event.PostInsertEventListener;
 import org.hibernate.event.PostLoadEventListener;
 import org.hibernate.event.PostUpdateEventListener;
 import org.hibernate.event.PreCollectionRecreateEventListener;
 import org.hibernate.event.PreCollectionRemoveEventListener;
 import org.hibernate.event.PreCollectionUpdateEventListener;
 import org.hibernate.event.PreDeleteEventListener;
 import org.hibernate.event.PreInsertEventListener;
 import org.hibernate.event.PreLoadEventListener;
 import org.hibernate.event.PreUpdateEventListener;
 import org.hibernate.event.RefreshEventListener;
 import org.hibernate.event.ReplicateEventListener;
 import org.hibernate.event.SaveOrUpdateEventListener;
 import org.hibernate.jmx.StatisticsService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableMap;
 import com.google.inject.Inject;
 import com.google.inject.internal.Sets;
 import com.google.inject.name.Named;
 import com.google.inject.servlet.RequestScoped;
 
 import de.cosmocode.palava.core.Registry;
 import de.cosmocode.palava.core.Registry.Key;
 import de.cosmocode.palava.core.lifecycle.Disposable;
 import de.cosmocode.palava.core.lifecycle.Initializable;
 import de.cosmocode.palava.core.lifecycle.LifecycleException;
 
 /**
  * Default implementation of the {@link HibernateService} interface.
  *
  * @author Willi Schoenborn
  */
 public final class DefaultHibernateService implements HibernateService, Initializable, Disposable {
     
     private static final Logger LOG = LoggerFactory.getLogger(DefaultHibernateService.class);
     
     private static final String JMX_NAME_FORMAT = "de.cosmocode.palava.jpa:type=%s/%s";
     
     private static final ImmutableMap<String, Class<?>> LISTENERS;
     
     static {
         final ImmutableMap.Builder<String, Class<?>> builder = ImmutableMap.builder();
 
         builder.put("auto-flush", AutoFlushEventListener.class);
         builder.put("merge", MergeEventListener.class);
         builder.put("create", PersistEventListener.class);
         builder.put("create-onflush", PersistEventListener.class);
         builder.put("delete", DeleteEventListener.class);
         builder.put("dirty-check", DirtyCheckEventListener.class);
         builder.put("evict", EvictEventListener.class);
         builder.put("flush", FlushEventListener.class);
         builder.put("flush-entity", FlushEntityEventListener.class);
         builder.put("load", LoadEventListener.class);
         builder.put("load-collection", InitializeCollectionEventListener.class);
         builder.put("lock", LockEventListener.class);
         builder.put("refresh", RefreshEventListener.class);
         builder.put("replicate", ReplicateEventListener.class);
         builder.put("save-update", SaveOrUpdateEventListener.class);
         builder.put("save", SaveOrUpdateEventListener.class);
         builder.put("update", SaveOrUpdateEventListener.class);
         builder.put("pre-load", PreLoadEventListener.class);
         builder.put("pre-update", PreUpdateEventListener.class);
         builder.put("pre-delete", PreDeleteEventListener.class);
         builder.put("pre-insert", PreInsertEventListener.class);
         builder.put("pre-collection-recreate", PreCollectionRecreateEventListener.class);
         builder.put("pre-collection-remove", PreCollectionRemoveEventListener.class);
         builder.put("pre-collection-update", PreCollectionUpdateEventListener.class);
         builder.put("post-load", PostLoadEventListener.class);
         builder.put("post-update", PostUpdateEventListener.class);
         builder.put("post-delete", PostDeleteEventListener.class);
         builder.put("post-insert", PostInsertEventListener.class);
         builder.put("post-commit-update", PostUpdateEventListener.class);
         builder.put("post-commit-delete", PostDeleteEventListener.class);
         builder.put("post-commit-insert", PostInsertEventListener.class);
         builder.put("post-collection-recreate", PostCollectionRecreateEventListener.class);
         builder.put("post-collection-remove", PostCollectionRemoveEventListener.class);
         builder.put("post-collection-update", PostCollectionUpdateEventListener.class);
         
         LISTENERS = builder.build();
     }
     
     private final File config;
     
     private final URL schema;
     
     private final Registry registry;
     
     private Interceptor interceptor;
     
     private boolean propagateEvents;
     
     private MBeanServer mBeanServer;
     
     private String mBeanName = DefaultHibernateService.class.getName();
     
     private final Set<ObjectName> mBeanNames = Sets.newHashSet();
     
     private SessionFactory factory;
     
     @Inject
     public DefaultHibernateService(
         @Named("hibernate.cfg") File config, 
         @Named("hibernate.schema") URL schema,
         Registry registry) {
         this.config = Preconditions.checkNotNull(config, "Config");
         this.schema = Preconditions.checkNotNull(schema, "Schema");
         this.registry = Preconditions.checkNotNull(registry, "Registry");
     }
     
     @Inject(optional = true)
     void setInterceptor(Interceptor interceptor) {
         this.interceptor = Preconditions.checkNotNull(interceptor, "Interceptor");
     }
     
     @Inject(optional = true)
     void setPropagateEvents(@Named("hibernate.events.propagate") boolean propagateEvents) {
         this.propagateEvents = propagateEvents;
     }
     
     @Inject(optional = true)
     void setMBeanServer(MBeanServer mBeanServer) {
         this.mBeanServer = Preconditions.checkNotNull(mBeanServer, "MBeanServer");
     }
     
     @Inject(optional = true)
     void setMBeanName(@Named("hibernate.jmx.name") String mBeanName) {
         this.mBeanName = Preconditions.checkNotNull(mBeanName, "MBeanName");
     }
     
     @Override
     public void initialize() {
         final Configuration configuration = new AnnotationConfiguration();
     
         LOG.debug("Adding hibernate schema: {}", schema);
         configuration.addURL(schema);
         
         LOG.debug("Adding hibernate config file: {}", config);
         configuration.configure(config);
     
         if (interceptor == null) {
             LOG.info("No interceptor configured");
         } else {
             LOG.info("Using {} as interceptor", interceptor);
             configuration.setInterceptor(interceptor);
         }
         
         if (propagateEvents) {
             LOG.info("Registering event listeners");
             for (Entry<String, Class<?>> entry : LISTENERS.entrySet()) {
                 final String event = entry.getKey();
                 final Class<?> type = entry.getValue();
                 final Key<?> key = Key.get(type, event);
                 final Object listener = registry.proxy(key);
                 LOG.info("Registering {} for {}", listener, event);
                 configuration.setListener(event, listener);
             }
         } else {
             LOG.info("Events are not propagated through the registry");
         }
         
         LOG.debug("Building session factory");
         this.factory = configuration.buildSessionFactory();
         
         if (mBeanServer == null) {
             LOG.info("No support for JMX, running in normal mode");
         } else {
             LOG.info("MBeanServer provided by {}", mBeanServer);
             
             final StatisticsService statistics = new StatisticsService();
             statistics.setSessionFactory(factory);
             statistics.setStatisticsEnabled(true);
             registerMBean(statistics);
         }
     }
     
     private void registerMBean(Object service) {
         try {
             final String name = String.format(JMX_NAME_FORMAT, mBeanName, service.getClass().getSimpleName());
             LOG.info("Regsitering {} as {}", service, name);
             final ObjectName objectName = new ObjectName(name);
             mBeanNames.add(objectName);
             mBeanServer.registerMBean(service, objectName);
         } catch (JMException e) {
             throw new LifecycleException(e);
         }
     }
     
     @Override
     public SessionFactory getSessionFactory() {
         return factory;
     }
     
     @Override
    @RequestScoped
     public Session get() {
         return new DestroyableSession(factory.openSession());
     }
     
     @Override
     public void dispose() throws LifecycleException {
         if (mBeanServer == null) return;
         for (ObjectName objectName : mBeanNames) {
             unregister(objectName);
         }
     }
     
     private void unregister(ObjectName objectName) {
         if (mBeanServer.isRegistered(objectName)) {
             try {
                 mBeanServer.unregisterMBean(objectName);
             } catch (InstanceNotFoundException e) {
                 throw new LifecycleException(e);
             } catch (MBeanRegistrationException e) {
                 throw new LifecycleException(e);
             }
         }
     }
     
 }
