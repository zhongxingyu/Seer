 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2014
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared;
 
 import com.flexive.shared.cache.FxCacheException;
 import com.flexive.shared.configuration.*;
 import com.flexive.shared.configuration.parameters.ParameterFactory;
 import com.flexive.shared.content.FxCachedContent;
 import com.flexive.shared.content.FxCachedContentContainer;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLoadException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.mbeans.FxCacheMBean;
 import com.flexive.shared.mbeans.FxCacheProxy;
 import com.flexive.shared.mbeans.MBeanHelper;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.FxFilteredEnvironment;
 import com.flexive.stream.ServerLocation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import java.util.List;
 
 /**
  * FxCache access
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class CacheAdmin {
     private static final Log LOG = LogFactory.getLog(CacheAdmin.class);
 
     public static final String ROOT_USERTICKETSTORE = "/UserTicketStore";
     public static final String ROOT_WEBDAV_USERTICKETSTORE = "/WebdavUserTicketStore";
     public static final String CACHE_SERVICE_NAME = "flexive:service=FxCache";
    public static final String LANGUAGES_ID = "/FxLang/FxId";
     public static final String LANGUAGES_ISO = "/FxLang/ISO";
     public static final String LANGUAGES_ALL = "/FxLang/ISO";
     public static final String ENVIRONMENT_BASE = "/FxEnvironment";
     public static final String ENVIRONMENT_RUNTIME = "runtime";
     public static final String STREAMSERVER_BASE = "/FxStreamServers";
     public static final String STREAMSERVER_EJB_KEY = "ejb_servers";
     public static final String CONTENTCACHE_BASE = "/FxContent";
     public static final String TREE_BASE = "/FxTree";
     public static final String TREE_MODIFIED_TIMESTAMP = "modified";
 
     private static final String CONTENTCACHE_KEY_STORE = "content";
 
     private static final Parameter<Boolean> DROP_RUNONCE = ParameterFactory.newInstance(Boolean.class,
             "/cacheAdmin/dropInRunOnce", ParameterScope.DIVISION_ONLY, false);
 
     // request-scoped cache for FxEnvironment
     public static final String ATTR_ENVIRONMENT = "$flexive_environment$";
 
 
     // "cached" cache beans
     private static FxCacheMBean cache = null;
     //    private static ThreadLocal<FxEnvironment> requestEnvironment = new ThreadLocal<FxEnvironment>();
     // environment loader lock
     private static final Object ENV_LOCK = new Object();
     
     /** 
      * {@see #isSharedCache()}
      */
     private static final boolean SHARED_CACHE;
     private static final boolean CACHE_ALL_VERSIONS;
 
     // system property to override SHARED_CACHE setting
     private static final String CONFIG_SHARED_CACHE = "flexive.cache.shared";
 
     // system property to override CACHE_ALL_VERSIONS settings
     private static final String CONFIG_CACHE_ALL_VERSIONS = "flexive.cache.allVersions";
 
     /**
      * {@see #isWebProfileDeployment}
      */
     private static final boolean WEB_PROFILE_DEPLOYMENT;
 
     static {
         // initialize web profile and shared cache configuration.
         
         WEB_PROFILE_DEPLOYMENT = Thread.currentThread().getContextClassLoader().getResource("flexive-ejb-interfaces-onlylocal.properties") != null;
 
         final String sharedCacheEnabled = System.getProperty(CONFIG_SHARED_CACHE);
         SHARED_CACHE =
                 // shared cache manually activated
                 Boolean.parseBoolean(sharedCacheEnabled)
                 // web profile deployment (and not disabled explicitly)
                 || (WEB_PROFILE_DEPLOYMENT && !"false".equals(sharedCacheEnabled));
 
         CACHE_ALL_VERSIONS = Boolean.parseBoolean(System.getProperty(CONFIG_SHARED_CACHE));
 
         if (LOG.isInfoEnabled()) {
             LOG.info("EJB web profile deployment: " + (WEB_PROFILE_DEPLOYMENT ? "yes" : "no"));
             LOG.info("Shared cache: " + (SHARED_CACHE ? "yes" : "no") + ". This can be set manually with -D" + CONFIG_SHARED_CACHE + "=true or false");
         }
     }
 
 
     /**
      * Returns the FxCache instance.
      *
      * @return the cache instance
      */
     public static synchronized FxCacheMBean getInstance() {
         if (cache != null) {
             return cache;
         }
 //        System.out.println("Locating ....");
 
         try {
             MBeanServer server = MBeanHelper.locateServer();
             cache = new FxCacheProxy(server);
             if (!server.isRegistered(((FxCacheProxy) cache).getName())) {
                 LOG.info("Registering FxCacheMBean .... ");
                 EJBLookup.getGlobalConfigurationEngine().registerCacheMBean(((FxCacheProxy) cache).getName());
 //                    server.registerMBean(new FxCache(), ((FxCacheProxy)cache).getName());
                 if (LOG.isDebugEnabled())
                     LOG.debug("Registered: " + server.isRegistered(((FxCacheProxy) cache).getName()));
                 cache.create();
             } else if (!isSharedCache()) {
                 // check if MBean Server is from current deployment. Currently this is disabled
                 // if the cache is shared between different deployments in the same VM, so undeploying
                 // the instance that provides will remove the cache for the other applications until
                 // a new flexive application is deployed.
                 
                 String deployedId = cache.getDeploymentId();
                 if (MBeanHelper.DEPLOYMENT_ID.equals(deployedId))
                     return cache;
                 LOG.info("Redeployment! Have to undeploy " + deployedId + " (vs " + MBeanHelper.DEPLOYMENT_ID + ")");
                 uninstallLocalInstance();
                 EJBLookup.getGlobalConfigurationEngine().registerCacheMBean(((FxCacheProxy) cache).getName());
                 LOG.debug("Registered: " + server.isRegistered(((FxCacheProxy) cache).getName()));
                 cache.create();
             }
             return cache;
         } catch (Exception e) {
             LOG.error(e.getMessage(), e);
             return null;
         }
     }
 
     /**
      * Uninstall the local cache instance. This also stops local streaming servers.
      *
      * @throws Exception    on errors
      * @since 3.1.4
      */
     public static synchronized void uninstallLocalInstance() throws Exception {
         if (isCacheMBeanInstalled() && MBeanHelper.DEPLOYMENT_ID.equals(getInstance().getDeploymentId())) {
             if (LOG.isInfoEnabled()) {
                 LOG.info("Uninstalling local cache instance and streaming server.");
             }
             getInstance().destroy();
             MBeanHelper.locateServer().unregisterMBean(((FxCacheProxy) cache).getName());
         }
     }
 
     /**
      * Check if the cache MBean is installed in the application server
      *
      * @return cache MBean installed
      */
     public static boolean isCacheMBeanInstalled() {
         MBeanServer server = MBeanHelper.locateServer();
         try {
             return server.isRegistered((new FxCacheProxy(server)).getName());
         } catch (MalformedObjectNameException e) {
             LOG.error(e);
             return false;
         }
     }
 
     /**
      * Is this a new and uninitialized flexive installation?
      * Useful to display splash screens etc.
      *
      * @return if this is a new flexive installation
      */
     public static boolean isNewInstallation() {
         try {
             return !isEnvironmentLoaded()
                     && !EJBLookup.getDivisionConfigurationEngine().get(SystemParameters.DIVISION_RUNONCE);
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     /**
      * Has the environment been loaded (yet) - internal helper
      *
      * @return if the environment has been loaded yet
      */
     public static boolean isEnvironmentLoaded() {
         try {
             return getInstance().exists(ENVIRONMENT_BASE, ENVIRONMENT_RUNTIME);
         } catch (FxCacheException e) {
             return false;
         }
     }
 
     /**
      * Get the FxEnvironment runtime from the cache
      *
      * @return FxEnvironment
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public static FxEnvironment getEnvironment() {
         if (getCachedEnvironment() != null) {
             return getCachedEnvironment();
         }
         try {
             FxEnvironment ret = (FxEnvironment) getInstance().get(ENVIRONMENT_BASE, ENVIRONMENT_RUNTIME);
             if (ret == null) {
                 synchronized (ENV_LOCK) {
                     // try to get the environment again. We may have blocked for a long time on ENV_LOCK
                     // if another thread started the environment initialization
                     ret = (FxEnvironment) getInstance().get(ENVIRONMENT_BASE, ENVIRONMENT_RUNTIME);
                     if (ret != null) {
                         return ret;
                     }
                     FxContext ri = FxContext.get();
                     if (!DivisionData.isValidDivisionId(ri.getDivisionId())) {
                         throw new FxCacheException("Division Id missing in request information");
                     }
                     EJBLookup.getDivisionConfigurationEngine().patchDatabase();
                     getInstance().reloadEnvironment(ri.getDivisionId());
                     //execute run-once scripts
                     EJBLookup.getScriptingEngine().executeRunOnceScripts();
                     //execute startup scripts
                     EJBLookup.getScriptingEngine().executeStartupScripts();
                     if (!EJBLookup.getTimerService().isInstalled()) {
                         EJBLookup.getTimerService().install(true);
                     }
 
                     for (String dropName : FxSharedUtils.getDrops()) {
                         // set the drop name as key on the DROP_RUNONCE parameter
                         final Parameter<Boolean> dropParameter = DROP_RUNONCE.copy().setData(
                                 new ParameterDataEditBean<Boolean>(DROP_RUNONCE.getData()).setKey(dropName));
                         EJBLookup.getScriptingEngine().executeDropRunOnceScripts(dropParameter, dropName);
 
                         // also run startup-scripts now
                         EJBLookup.getScriptingEngine().executeDropStartupScripts(dropName);
                     }
                     //Create eviction strategy if supported (and unless one has already been specified (FX-384))
                     getInstance().setEvictionStrategy(ri.getDivisionId(), CONTENTCACHE_BASE, 100, -1, 120, false);
 
                     // make sure we don't miss any updates in the environment
                     getInstance().reloadEnvironment(ri.getDivisionId());
                     ret = (FxEnvironment) getInstance().get(ENVIRONMENT_BASE, ENVIRONMENT_RUNTIME);
                     if (ret == null)
                         throw new FxLoadException("ex.structure.runtime.cache.notFound");
                 }
             }
             setCachedEnvironment(ret);
             return ret;
         } catch (FxCacheException e) {
             throw new FxLoadException(LOG, e, "ex.cache.access.error", e.getMessage()).asRuntimeException();
         } catch (FxLoadException f) {
             throw f.asRuntimeException(); //pass thru
         } catch (Exception e) {
             throw new FxLoadException(LOG, e, "ex.cache.access.error", e.getClass().getName() + ": " + e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Return a {@link com.flexive.shared.structure.FxFilteredEnvironment} for the calling user.
      *
      * @return a {@link com.flexive.shared.structure.FxFilteredEnvironment} for the calling user.
      */
     public static FxEnvironment getFilteredEnvironment() {
         if (FxContext.getUserTicket().isGlobalSupervisor())
             return getEnvironment();
         return new FxFilteredEnvironment(getEnvironment());
     }
 
     /**
      * Force reloading of the environment for the current division. May only
      * be called by the supervisor user.
      *
      * @throws Exception on errors
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public static void reloadEnvironment() throws Exception {
         if (!FxContext.getUserTicket().isGlobalSupervisor()) {
             throw new FxNoAccessException("ex.cache.reload.privileges").asRuntimeException();
         }
         getInstance().reloadEnvironment(FxContext.get().getDivisionId());
         setCachedEnvironment(null);
     }
 
     /**
      * Put a content and its security info in the cache
      *
      * @param content the content to cache
      */
     public static void cacheContent(FxCachedContent content) {
         if (!isCacheAllVersions() && !(content.getContent().isMaxVersion() || content.getContent().isLiveVersion())) {
             return;
         }
         try {
             final String cachePath = getContentCachePath(content.getContent().getId());
             FxCachedContentContainer container = (FxCachedContentContainer) getInstance().get(cachePath, CONTENTCACHE_KEY_STORE);
             if (container != null) {
                 container.add(content);
             } else
                 container = new FxCachedContentContainer(content);
             getInstance().put(cachePath, CONTENTCACHE_KEY_STORE, container);
         } catch (FxCacheException e) {
             LOG.warn(e.getMessage(), e);
         }
     }
 
     /**
      * Try to obtain a cached copy of a content identified by its primary key, will return <code>null</code> if not cached
      *
      * @param pk requested primary key
      * @return the cached content or <code>null</code> if not found
      */
     public static FxCachedContent getCachedContent(FxPK pk) {
         try {
             FxCachedContentContainer container = (FxCachedContentContainer) getInstance().get(
                     getContentCachePath(pk.getId()),
                     CONTENTCACHE_KEY_STORE
             );
             if (container != null)
                 return container.get(pk);
             return null;
         } catch (FxCacheException e) {
             LOG.warn(e.getMessage(), e);
             return null;
         }
     }
 
     private static String getContentCachePath(long id) {
         return CONTENTCACHE_BASE + "/" + id;
     }
 
 
     /**
      * Expire all cached versions of a content with the requested id
      *
      * @param id requested id
      */
     public static void expireCachedContent(long id) {
         if (id <= 0)
             return;
         try {
 //            System.out.println("=> Expired cache for id "+id);
             getInstance().remove(getContentCachePath(id), CONTENTCACHE_KEY_STORE);
         } catch (FxCacheException e) {
             LOG.warn(e.getMessage(), e);
         }
     }
 
     /**
      * Expire all cached contents
      */
     public static void expireCachedContents() {
         try {
             getInstance().remove(CONTENTCACHE_BASE);
         } catch (FxCacheException e) {
             LOG.warn(e.getMessage(), e);
         }
     }
 
     /**
      * Called from external methods when the environment changed.
      */
     public static void environmentChanged() {
         setCachedEnvironment(null);
     }
 
     /**
      * Get a list of all available StreamServers
      *
      * @return list of all available StreamServers
      */
     @SuppressWarnings({"unchecked", "ThrowableInstanceNeverThrown"})
     public static List<ServerLocation> getStreamServers() {
         try {
             List<ServerLocation> ret = (List<ServerLocation>) getInstance().globalGet(STREAMSERVER_BASE, STREAMSERVER_EJB_KEY);
             if (ret == null)
                 throw new FxLoadException("ex.cache.streamservers.notFound");
             return ret;
         } catch (FxCacheException e) {
             throw new FxLoadException(LOG, e, "ex.cache.access.error", e.getMessage()).asRuntimeException();
         } catch (FxLoadException f) {
             throw f.asRuntimeException(); //pass thru
         } catch (Exception e) {
             throw new FxLoadException(LOG, e, "ex.cache.access.error", e.getClass().getName(), e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Return the cached environment for this request. Do not call this code to retrieve the environment,
      * use {@link com.flexive.shared.CacheAdmin#getEnvironment()} instead.
      *
      * @return the cached environment for this request, or null if it hasn't been set.
      * @since 3.1
      */
     private static FxEnvironment getCachedEnvironment() {
         return (FxEnvironment) FxContext.get().getAttribute(ATTR_ENVIRONMENT);
     }
 
     /**
      * Store the given environment for the rest of this request.
      *
      * @param env the new environment
      * @since 3.1
      */
     private static void setCachedEnvironment(FxEnvironment env) {
         FxContext.get().setAttribute(ATTR_ENVIRONMENT, env);
     }
 
     /**
      * Flag the tree as modified by setting the current timestamp as modification date
      */
     public static void setTreeWasModified() {
         try {
             getInstance().put(TREE_BASE, TREE_MODIFIED_TIMESTAMP, System.currentTimeMillis());
         } catch (FxCacheException e) {
             LOG.error("Failed to set tree modified timestamp", e);
         }
     }
 
     /**
      * Get the timestamp of the last tree modification
      *
      * @return timestamp of the last tree modification
      */
     public static long getTreeModificationTimestamp() {
         try {
             Object ts = getInstance().get(TREE_BASE, TREE_MODIFIED_TIMESTAMP);
             if (ts == null) {
                 synchronized (ENV_LOCK) {
                     setTreeWasModified();
                     return getTreeModificationTimestamp();
                 }
             }
             if (ts instanceof Long)
                 return (Long) ts;
             LOG.error("Tree modified timestamp expected as Long, but was: " + ts.getClass().getCanonicalName());
             return 0;
         } catch (FxCacheException e) {
             LOG.error("Failed to get tree modified timestamp", e);
             return 0;
         }
     }
 
     /**
      * Is the cache shared between different deployments in the same VM ({@code -Dflexive.cache.shared=(true|false)})?
      *
      *
      * <p>This parameter is set automatically when the only local interfaces are deployed
      * (as in a EJB 3.1 Web profile deployment), since you typically want to deploy more than one
      * flexive application.</p>
      *
      * <p>In EAR deployments, this is set to false by default. This improves performance since no
      * serialization is involved when writing objects to the cache. In WAR deployments, you can disable
      * the shared cache if you deploy only one flexive application in your container.</p>
      *
      * @since 3.1.4
      */
     public static boolean isSharedCache() {
         return SHARED_CACHE;
     }
 
     /**
      * Should all content versions be cached, or only the maximum/live versions?
      *
      * <p>
      *     Beginning with flexive 3.2.0, only the maximum and live versions of a content are cached by default.
      *     This is to avoid memory load issues when the database contains many instances with lots of versions.
      *     When loading all versions of an instance (e.g. due to an export operation), they will all end up in the
      *     cache but the entry still count as only one content (since all versions are stored in a single cache node).
      * </p>
      *
      * @return  whether all content versions should be cached
      *
      * @since 3.2.0
      */
     public static boolean isCacheAllVersions() {
         return CACHE_ALL_VERSIONS;
     }
 
     /**
      * Is this a JavaEE 6 Web Profile deployment? Automatically set when flexive-ejb-interfaces-local
      * is packaged.
      *
      * @since 3.1.4
      */
     public static boolean isWebProfileDeployment() {
         return WEB_PROFILE_DEPLOYMENT;
     }
 }
