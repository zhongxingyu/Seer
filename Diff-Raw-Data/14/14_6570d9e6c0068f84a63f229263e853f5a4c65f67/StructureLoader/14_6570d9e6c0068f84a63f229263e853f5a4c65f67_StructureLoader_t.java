 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
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
 package com.flexive.core.structure;
 
 import com.flexive.core.Database;
 import com.flexive.core.security.UserTicketImpl;
 import com.flexive.core.storage.EnvironmentLoader;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.cache.FxCacheException;
 import com.flexive.shared.configuration.DivisionData;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLoadException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.security.UserGroup;
 import com.flexive.shared.structure.FxType;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * Load structure information into the cache
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public final class StructureLoader {
     private static final Log LOG = LogFactory.getLog(StructureLoader.class);
 
     /**
      * Private c'tor to avoid instantiation
      */
     private StructureLoader() {
     }
 
     /**
      * (Re)Load the FxStructure runtime for a given division and put it into the cache
      *
      * @param divisionId  division to load
      * @param forceReload load even if exists
      * @param _con        optional open connection
      * @throws FxCacheException on errors
      * @throws FxLoadException  on errors
      */
     public static void load(int divisionId, boolean forceReload, Connection _con) throws FxCacheException, FxLoadException {
         DivisionData dd;
         try {
             dd = EJBLookup.getGlobalConfigurationEngine().getDivisionData(divisionId);
             if (!dd.isAvailable()) {
                 LOG.error("Division " + divisionId + " is not available!");
                 return;
             }
             if (LOG.isDebugEnabled())
                 LOG.debug("Loading environment for division " + divisionId + " (" + dd.getDbVendor() + ": " + dd.getDbVersion() + "), forcing reload: " + forceReload);
         } catch (Exception e) {
             LOG.error(e, e);
             throw new FxLoadException(e);
         }
         EnvironmentLoader loader;
         try {
             loader = StorageManager.getEnvironmentLoader(dd);
         } catch (FxNotFoundException e) {
             throw new FxLoadException(e);
         }
         Connection con = _con;
         try {
             long time = System.currentTimeMillis();
             try {
                 if (con == null)
                     con = FxEnvironmentUtils.getDbConnection(divisionId);
             } catch (SQLException e) {
                 LOG.error(e, e);
                 return;
             }
             FxEnvironmentImpl environment = (FxEnvironmentImpl) FxEnvironmentUtils.cacheGet(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME);
             if (environment != null && !forceReload)
                 return;
             final boolean bootstrap = (environment == null);
             environment = new FxEnvironmentImpl();
             environment.setDataTypes(loader.loadDataTypes(con));
             environment.setAcls(loader.loadACLs(con));
             environment.setStepDefinitions(loader.loadStepDefinitions(con));
             environment.setSteps(loader.loadSteps(con));
             environment.setWorkflows(loader.loadWorkflows(con, environment));
             environment.setMandators(loader.loadMandators(con));
             environment.setGroups(loader.loadGroups(con));
             environment.setSelectLists(loader.loadSelectLists(con, environment));
             environment.setTypes(loader.loadTypes(con, environment));
             environment.setProperties(loader.loadProperties(con, environment));
             environment.setAssignments(loader.loadAssignments(con, environment));
             environment.setScripts(loader.loadScripts(con));
             environment.setScriptMappings(loader.loadScriptMapping(con, environment));
             environment.setScriptSchedules(loader.loadScriptSchedules(con, environment));
             environment.setLanguages(EJBLookup.getLanguageEngine().loadAvailable(true));
             environment.setUserGroups(EJBLookup.getUserGroupEngine().loadAll(-1));
             environment.resolveDependencies();
             environment.updateTimeStamp();
             CacheAdmin.environmentChanged();
             //structure might have been loaded meanwhile on another node - only put it into the cache if forcing
             if (FxEnvironmentUtils.cacheGet(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME) == null || forceReload) {
                 FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, environment);
                 if (LOG.isDebugEnabled())
                     LOG.debug("Loaded structure and put into cache in " + (System.currentTimeMillis() - time) + "[ms]");
                 //put a dummy cached entry to create the path to avoid cache warnings if the content cache is
                 //accessed and it does not exist because its empty
                 CacheAdmin.getInstance().put(CacheAdmin.CONTENTCACHE_BASE, -1, null);
                 UserTicketImpl.reloadGuestTicketAssignments(true);
                 if (bootstrap) {
                     //have to reload since default values require a present environment
                     environment.setProperties(loader.loadProperties(con, environment));
                     environment.setAssignments(loader.loadAssignments(con, environment));
                     environment.resolveDependencies();
                     FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, environment);
                 }
                 CacheAdmin.expireCachedContents();
             }
         } catch (FxApplicationException e) {
             throw new FxLoadException(LOG, e);
         } finally {
             if (_con == null)
                 Database.closeObjects(StructureLoader.class, con, null);
         }
     }
 
     /**
      * Updates the current division's environment timestamp, but does not reload the environment from the DB
      */
     public static synchronized void updateEnvironmentTimestamp() {
         final FxEnvironmentImpl env = (FxEnvironmentImpl) CacheAdmin.getEnvironment();
         env.updateTimeStamp();
         try {
             FxEnvironmentUtils.cachePut(FxContext.get().getDivisionId(), CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, env);
         } catch (FxCacheException e) {
             throw new IllegalStateException(e);
         }
     }
 
     /**
      * Load all FxStructure runtimes from the registered databases and place them into the cache
      *
      * @throws FxCacheException       on errors
      * @throws FxApplicationException on errors
      */
     public static void loadAll() throws FxCacheException, FxApplicationException {
         for (int div : EJBLookup.getGlobalConfigurationEngine().getDivisionIds())
             StructureLoader.load(div, false, null);
     }
 
     /**
      * Reload all workflows for the given division
      *
      * @param divisionId the division to reload
      * @throws FxApplicationException on errors
      */
     public static void reloadWorkflows(int divisionId) throws FxApplicationException {
         long time = System.currentTimeMillis();
         Connection con = null;
         try {
             try {
                 con = FxEnvironmentUtils.getDbConnection(divisionId);
             } catch (SQLException e) {
                 LOG.error(e);
                 return;
             }
             FxEnvironmentImpl structure = ((FxEnvironmentImpl) CacheAdmin.getEnvironment()).deepClone();
 
             EnvironmentLoader loader;
             loader = StorageManager.getEnvironmentLoader(EJBLookup.getGlobalConfigurationEngine().getDivisionData(divisionId));
             structure.setStepDefinitions(loader.loadStepDefinitions(con));
             structure.setSteps(loader.loadSteps(con));
             structure.setWorkflows(loader.loadWorkflows(con, structure));
             //resync workflows
             for (FxType type : structure.getTypes(true, true, true, true))
                 type.reloadWorkflow(structure);
             FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, structure);
             CacheAdmin.getEnvironment();
         } catch (FxCacheException e) {
             LOG.error(e, e);
         } catch (FxNotFoundException e) {
             throw new FxLoadException(e);
         } finally {
             Database.closeObjects(StructureLoader.class, con, null);
         }
         if (LOG.isDebugEnabled())
             LOG.debug("Reloaded Workflows in " + (System.currentTimeMillis() - time) + "[ms]");
     }
 
     /**
      * Reload all scripting informations for the given division
      *
      * @param divisionId the division to reload
      * @throws FxApplicationException on errors
      */
     public static void reloadScripting(int divisionId) throws FxApplicationException {
         long time = System.currentTimeMillis();
         Connection con = null;
         try {
             try {
                 con = FxEnvironmentUtils.getDbConnection(divisionId);
             } catch (SQLException e) {
                 LOG.error(e);
                 return;
             }
             FxEnvironmentImpl environment = ((FxEnvironmentImpl) CacheAdmin.getEnvironment()).deepClone();
             EnvironmentLoader loader;
             loader = StorageManager.getEnvironmentLoader(EJBLookup.getGlobalConfigurationEngine().getDivisionData(divisionId));
             environment.updateScripting(loader.loadScripts(con), loader.loadScriptMapping(con, environment), loader.loadScriptSchedules(con, environment));
             environment.updateTimeStamp();
             FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, environment);
             CacheAdmin.getEnvironment();
         } catch (FxCacheException e) {
             LOG.error(e, e);
         } catch (FxNotFoundException e) {
             throw new FxLoadException(e);
         } finally {
             Database.closeObjects(StructureLoader.class, con, null);
         }
         if (LOG.isDebugEnabled())
             LOG.debug("Reloaded Scripting in " + (System.currentTimeMillis() - time) + "[ms]");
     }
 
     /**
      * Reload all assignment-related informations including types and properties for the given division.
      *
      * @param divisionId    the division to reload
      * @throws FxApplicationException on errors
      * @since 3.1.4
      */
     public static void reloadAssignments(int divisionId) throws FxApplicationException {
         Connection con = null;
         try {
             try {
                 con = FxEnvironmentUtils.getDbConnection(divisionId);
             } catch (SQLException e) {
                 LOG.error(e);
                 return;
             }
             final FxEnvironmentImpl existingEnv = (FxEnvironmentImpl) CacheAdmin.getEnvironment();
             if (existingEnv == null) {
                 load(divisionId, true, con);
                 return;
             }
             FxEnvironmentImpl environment = (existingEnv).deepClone();
             EnvironmentLoader loader;
             loader = StorageManager.getEnvironmentLoader(EJBLookup.getGlobalConfigurationEngine().getDivisionData(divisionId));
 
             environment.setGroups(loader.loadGroups(con));
             environment.setProperties(loader.loadProperties(con, environment));
             environment.setAssignments(loader.loadAssignments(con, environment));
             environment.resolveDependencies();
 
             environment.updateTimeStamp();
             FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, environment);
         } catch (FxCacheException e) {
             LOG.error(e, e);
         } catch (FxNotFoundException e) {
             throw new FxLoadException(e);
         } finally {
             Database.closeObjects(StructureLoader.class, con, null);
         }
     }
 
     /**
      * Update or add an ACL
      *
      * @param divisionId division
      * @param acl        the ACL to update
      */
     public static void updateACL(int divisionId, ACL acl) {
         try {
             FxEnvironmentImpl structure = ((FxEnvironmentImpl) CacheAdmin.getEnvironment()).deepClone();
             structure.updateACL(acl);
             FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, structure);
         } catch (FxCacheException e) {
             LOG.error(e, e);
         }
     }
 
     /**
      * Remove an ACL
      *
      * @param divisionId division
      * @param id         ACL id
      */
     public static void removeACL(int divisionId, long id) {
         try {
             FxEnvironmentImpl structure = ((FxEnvironmentImpl) FxEnvironmentUtils.cacheGet(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME)).deepClone();
             structure.removeACL(id);
             FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, structure);
         } catch (FxCacheException e) {
             LOG.error(e, e);
         }
     }
 
     /**
      * Update or add a FxType
      *
      * @param divisionId division
      * @param type       FxType to add/update
      * @throws FxNotFoundException on errors
      */
     public static void updateType(int divisionId, FxType type) throws FxNotFoundException {
         try {
             FxEnvironmentImpl structure = ((FxEnvironmentImpl) FxEnvironmentUtils.cacheGet(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME)).deepClone();
             structure.updateType(type);
             FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, structure);
         } catch (FxCacheException e) {
             LOG.error(e, e);
         }
     }
 
     /**
      * Reload structure for an open connection (must not be called from managed beans!)
      *
      * @param con open and valid connection
      * @throws FxCacheException on errors
      * @throws FxLoadException  on errors
      */
     public static void reload(Connection con) throws FxCacheException, FxLoadException {
         load(FxContext.get().getDivisionId(), true, con);
     }
 
     /**
      * Add a new Mandator to the environment
      *
      * @param divisionId division
      * @param mandator   mandator
      */
     public static void addMandator(int divisionId, Mandator mandator) {
         try {
             FxEnvironmentImpl structure = ((FxEnvironmentImpl) FxEnvironmentUtils.cacheGet(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME)).deepClone();
             structure.addMandator(mandator);
             FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, structure);
         } catch (FxCacheException e) {
             LOG.error(e, e);
         }
     }
 
     /**
      * Update an existing mandator, silently fails if the mandator does not exist
      *
      * @param divisionId division
      * @param mandator   mandator
      */
     public static void updateMandator(int divisionId, Mandator mandator) {
         try {
             FxEnvironmentImpl structure = ((FxEnvironmentImpl) FxEnvironmentUtils.cacheGet(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME)).deepClone();
             structure.updateMandator(mandator);
             FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, structure);
         } catch (FxCacheException e) {
             LOG.error(e, e);
         }
     }
 
     /**
      * Remove a mandator
      *
      * @param divisionId division
      * @param mandatorId mandator
      */
     public static void removeMandator(int divisionId, long mandatorId) {
         try {
             FxEnvironmentImpl structure = ((FxEnvironmentImpl) FxEnvironmentUtils.cacheGet(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME)).deepClone();
             structure.removeMandator(mandatorId);
             FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, structure);
         } catch (FxCacheException e) {
             LOG.error(e, e);
         }
     }
 
     /**
      * Replace the environments languages
      *
      * @param divisionId division
      * @param languages new active languages
      * @throws FxCacheException on errors
      * @since 3.1.4
      */
     public static void updateLanguages(int divisionId, List<FxLanguage> languages) throws FxCacheException {
         if (CacheAdmin.isNewInstallation() || !CacheAdmin.isEnvironmentLoaded())
             return;
         FxEnvironmentImpl structure = ((FxEnvironmentImpl) FxEnvironmentUtils.cacheGet(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME)).deepClone();
         structure.setLanguages(languages);
         FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, structure);
     }
 
     /**
     * Replace the environments user groups
      *
      * @param divisionId division
      * @param userGroups new user groups
      * @since 3.1.4
      */
     public static void updateUserGroups(int divisionId, List<UserGroup> userGroups) {
         if (CacheAdmin.isNewInstallation() || !CacheAdmin.isEnvironmentLoaded())
             return;
         try {
             FxEnvironmentImpl structure = ((FxEnvironmentImpl) FxEnvironmentUtils.cacheGet(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME)).deepClone();
             structure.setUserGroups(userGroups);
             FxEnvironmentUtils.cachePut(divisionId, CacheAdmin.ENVIRONMENT_BASE, CacheAdmin.ENVIRONMENT_RUNTIME, structure);
         } catch (FxCacheException e) {
             LOG.error(e);
         }
     }
 }
