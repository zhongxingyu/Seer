 /**
  * <copyright>
  *
  * Copyright (c) 2011 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: EntityManagerProvider.java,v 1.7 2011/09/26 19:48:10 mtaal Exp $
  */
 package org.eclipse.emf.texo.server.store;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import org.eclipse.emf.texo.component.ComponentProvider;
 import org.eclipse.emf.texo.component.TexoComponent;
 import org.eclipse.emf.texo.component.TexoStaticSingleton;
 
 /**
  * Class which provides an entity manager.
  * 
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  */
 public class EntityManagerProvider implements TexoComponent, TexoStaticSingleton {
 
   public static final String ECLIPSELINK_CLASSLOADER_OPTION = "eclipselink.classloader"; //$NON-NLS-1$
   public static final String MULTITENANT_PROPERTY_DEFAULT = "eclipselink.tenant-id"; //$NON-NLS-1$
 
   private static EntityManagerProvider instance = ComponentProvider.getInstance().newInstance(
       EntityManagerProvider.class);
 
   public static EntityManagerProvider getInstance() {
     return instance;
   }
 
   public static void setInstance(EntityManagerProvider entityManagerProvider) {
     instance = entityManagerProvider;
   }
 
   private ThreadLocal<EntityManager> currentEntityManager = new ThreadLocal<EntityManager>();
 
   private EntityManagerFactory entityManagerFactory;
   private String persistenceUnitName;
   private Map<Object, Object> persistenceOptions = new HashMap<Object, Object>();
   private boolean useCurrentEntityManagerPattern = false;
 
   /**
    * Creates the internally used {@link EntityManagerFactory} if it is not already set. It uses the persistenceUnitName
    * (and if set the persistenceOptions).
    * 
    * Is called automatically when retrieving an {@link EntityManager} through the {@link #getEntityManager()} method.
    */
   public void initialize() {
     if (entityManagerFactory != null) {
       return;
     }
     if (persistenceOptions != null) {
       if (!persistenceOptions.containsKey(ECLIPSELINK_CLASSLOADER_OPTION)) {
         persistenceOptions.put(ECLIPSELINK_CLASSLOADER_OPTION, EntityManagerProvider.class.getClassLoader());
       }
      persistenceOptions.put(MULTITENANT_PROPERTY_DEFAULT, "texo"); //$NON-NLS-1$
       entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName, persistenceOptions);
     } else {
       entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
     }
   }
 
   /**
    * This method should return a new {@link EntityManager} instance. It is called once for each request at the beginning
    * of the processing of a request.
    * 
    * @see #releaseEntityManager(EntityManager)
    */
   public EntityManager createEntityManager() {
     initialize();
     return entityManagerFactory.createEntityManager();
   }
 
   /**
    * If {@link #isUseCurrentEntityManagerPattern()} is true then the entity manager stored in a threadlocal is returned
    * (see {@link #getCurrentEntityManager()}), otherwise always a new entity manager is returned (see
    * {@link #createEntityManager()}.
    */
   public EntityManager getEntityManager() {
     if (isUseCurrentEntityManagerPattern()) {
       return getCurrentEntityManager();
     }
     return createEntityManager();
   }
 
   /**
    * Maintains an {@link EntityManager} in a ThreadLocal. Will check a {@link ThreadLocal} to see if an EntityManager is
    * present there. If not, one is created, set in the ThreadLocal and returned.
    * 
    * When an EntityManager is created this method automatically begins a transaction also!
    * 
    * When finished with the entity manager, at the end of the Thread call
    * {@link EntityManagerProvider#clearCurrentEntityManager()}.
    * 
    * Note that it is especially important in servlet environments to clear the entity manager at the end of the request
    * as Tomcat and maybe other servlet containers will re-use thread objects for subsequent requests.
    * 
    * @return a new {@link EntityManager} or the {@link EntityManager} currently stored in the thread if there is one
    */
   public EntityManager getCurrentEntityManager() {
     if (currentEntityManager.get() != null) {
       return currentEntityManager.get();
     }
     currentEntityManager.set(createEntityManager());
     currentEntityManager.get().getTransaction().begin();
     return currentEntityManager.get();
   }
 
   /**
    * Set the entity manager in the thread local, if there is a current entity manager in the threadlocal then it is
    * closed before the new one is set.
    * 
    * @see #getCurrentEntityManager()
    */
   public void setCurrentEntityManager(EntityManager entityManager) {
     if (currentEntityManager.get() != null) {
       currentEntityManager.get().close();
     }
     currentEntityManager.set(entityManager);
   }
 
   /**
    * Clears the entity manager from the current thread and closes it if this was not already done.
    */
   public void clearCurrentEntityManager() {
     if (currentEntityManager.get() != null) {
       if (currentEntityManager.get().isOpen()) {
         currentEntityManager.get().close();
       }
       currentEntityManager.set(null);
     }
   }
 
   /**
    * @return true if there is an EntityManager in the current thread.
    */
   public boolean hasCurrentEntityManager() {
     return null != currentEntityManager.get();
   }
 
   /**
    * Can be used to close/release an entity manager. Is called when the entity manager can be closed. It will also be
    * called if the processing resulted in errors/exceptions.
    * 
    * The default implementation calls {@link EntityManager#close()}.
    * 
    * @param entityManager
    */
   public void releaseEntityManager(EntityManager entityManager) {
     if (entityManager.isOpen()) {
       entityManager.close();
     }
   }
 
   public EntityManagerFactory getEntityManagerFactory() {
     return entityManagerFactory;
   }
 
   public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
     this.entityManagerFactory = entityManagerFactory;
   }
 
   public String getPersistenceUnitName() {
     return persistenceUnitName;
   }
 
   public void setPersistenceUnitName(String persistenceUnitName) {
     this.persistenceUnitName = persistenceUnitName;
   }
 
   public Map<Object, Object> getPersistenceOptions() {
     return persistenceOptions;
   }
 
   public void setPersistenceOptions(Map<Object, Object> persistenceOptions) {
     this.persistenceOptions = persistenceOptions;
   }
 
   public boolean isUseCurrentEntityManagerPattern() {
     return useCurrentEntityManagerPattern;
   }
 
   public void setUseCurrentEntityManagerPattern(boolean useCurrentEntityManagerPattern) {
     this.useCurrentEntityManagerPattern = useCurrentEntityManagerPattern;
   }
 }
