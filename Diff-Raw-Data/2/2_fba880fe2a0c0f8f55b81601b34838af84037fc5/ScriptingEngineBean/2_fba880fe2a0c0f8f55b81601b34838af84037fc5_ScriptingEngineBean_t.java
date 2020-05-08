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
 package com.flexive.ejb.beans;
 
 import com.flexive.core.Database;
 import com.flexive.core.security.FxPrincipal;
 import com.flexive.core.security.UserTicketImpl;
 import com.flexive.core.security.UserTicketStore;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.core.structure.FxEnvironmentImpl;
 import com.flexive.core.structure.StructureLoader;
 import com.flexive.shared.*;
 import com.flexive.shared.configuration.Parameter;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.ScriptingEngine;
 import com.flexive.shared.interfaces.ScriptingEngineLocal;
 import com.flexive.shared.interfaces.SequencerEngineLocal;
 import com.flexive.shared.scripting.*;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.FxAssignment;
 import com.flexive.shared.structure.FxType;
 import com.google.common.collect.Lists;
 import groovy.lang.Binding;
 import groovy.lang.GroovyShell;
 import groovy.lang.Script;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.groovy.control.CompilationFailedException;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import javax.security.auth.Subject;
 import java.io.File;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.security.Principal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import static com.flexive.core.DatabaseConst.*;
 import static com.flexive.shared.EJBLookup.getDivisionConfigurationEngine;
 
 /**
  * ScriptingEngine implementation
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Stateless(name = "ScriptingEngine", mappedName = "ScriptingEngine")
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class ScriptingEngineBean implements ScriptingEngine, ScriptingEngineLocal {
 
     // Our logger
     private static final Log LOG = LogFactory.getLog(ScriptingEngineBean.class);
 
     @Resource
     javax.ejb.SessionContext ctx;
 
     @EJB
     SequencerEngineLocal seq;
 
 
     /**
      * Static helper class for deployment specific caches and settings
      */
     static class LocalScriptingCache {
 
         /**
          * Cache for compile groovy scripts
          */
         static ConcurrentMap<Long, Script> groovyScriptCache = new ConcurrentHashMap<Long, Script>(50);
 
         /**
          * Timestamp of the script cache
          */
         static volatile long scriptCacheTimestamp = -1;
 
         /**
          * Scripts by Event cache
          */
         static volatile Map<FxScriptEvent, List<Long>> scriptsByEvent = new HashMap<FxScriptEvent, List<Long>>(10);
 
         static volatile List<FxScriptRunInfo> runOnceInfos = null;
 
         static Script checkoutScript(long scriptId, FxScriptInfo si, String code) throws FxInvalidParameterException {
             Script script = LocalScriptingCache.groovyScriptCache.get(scriptId);
             if (script == null) {
                 try {
                     GroovyShell shell = new GroovyShell();
                     script = shell.parse(code);
                 } catch (CompilationFailedException e) {
                     throw new FxInvalidParameterException(si.getName(), "ex.general.scripting.compileFailed", si.getName(), e.getMessage());
                 } catch (Throwable t) {
                     throw new FxInvalidParameterException(si.getName(), "ex.general.scripting.exception", si.getName(), t.getMessage());
                 }
                 LocalScriptingCache.groovyScriptCache.putIfAbsent(scriptId, script);
             }
             return null;
         }
 
         static void checkinScript(long scriptId, Script script) {
 
         }
 
         /**
          * Execute a script.
          * This method does not check the calling user's role nor does it cache scripts.
          * It should only be used to execute code from the groovy console or code that is not to be expected to
          * be run more than once.
          *
          * @param name    name of the script, extension is needed to choose interpreter
          * @param binding bindings to apply
          * @param code    the script code
          * @return last script evaluation result
          * @throws FxApplicationException on errors
          */
         static FxScriptResult internal_runScript(String name, FxScriptBinding binding, String code) throws FxApplicationException {
             if (name == null)
                 name = "unknown";
             if (name.indexOf('.') < 0)
                 throw new FxInvalidParameterException(name, "ex.general.scripting.noExtension", name);
             if (!FxSharedUtils.isGroovyScript(name) && FxSharedUtils.USE_JDK6_EXTENSION) {
                 try {
                     return (FxScriptResult) Class.forName("com.flexive.core.JDK6Scripting").
                             getMethod("runScript", new Class[]{String.class, FxScriptBinding.class, String.class}).
                             invoke(null, name, binding, code);
                 } catch (Throwable t) {
                     LOG.error("Exception running script [" + name + "]: " + t.getMessage(), t);
                     if (t instanceof FxApplicationException)
                         throw (FxApplicationException) t;
                     if (t instanceof java.lang.reflect.InvocationTargetException && t.getCause() != null) {
                         if (t.getCause() instanceof FxApplicationException)
                             throw (FxApplicationException) t.getCause();
                         throw new FxInvalidParameterException(name, t.getCause(), "ex.general.scripting.exception", name, t.getCause().getMessage()).asRuntimeException();
                     }
                     throw new FxInvalidParameterException(name, t, "ex.general.scripting.exception", name, t.getMessage()).asRuntimeException();
                 }
             }
             if (binding != null) {
                 if (!binding.getProperties().containsKey("ticket"))
                     binding.setVariable("ticket", FxContext.getUserTicket());
                 if (!binding.getProperties().containsKey("environment"))
                     binding.setVariable("environment", CacheAdmin.getEnvironment());
                 binding.setVariable("scriptname", name);
             }
             if (FxSharedUtils.isGroovyScript(name)) {
                 //we prefer the native groovy binding
                 try {
                     GroovyShell shell = new GroovyShell();
                     Script script = shell.parse(code);
                     if (binding != null)
                         script.setBinding(new Binding(binding.getProperties()));
                     Object result = script.run();
                     return new FxScriptResult(binding, result);
                 } catch (Throwable e) {
                     LOG.error("Exception running script [" + name + "]: " + e.getMessage(), e);
                     if (e instanceof FxApplicationException)
                         throw (FxApplicationException) e;
                     throw new GenericScriptException(name, LOG, e, "ex.general.scripting.exception", name, e.getMessage());
                 }
             }
             throw new FxInvalidParameterException(name, "ex.general.scripting.needJDK6", name).asRuntimeException();
         }
 
     }
 
     private static final Object RUNONCE_LOCK = new Object();
 
     /**
      * Marker exception that wraps a script exception (used for more accurate error reporting).
      */
     public static class GenericScriptException extends FxInvalidParameterException {
         private static final long serialVersionUID = 6437523270242591223L;
 
         private GenericScriptException(String parameter, Log log, Throwable cause, String key, Object... values) {
             super(parameter, log, cause, key, values);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public String loadScriptCode(long scriptId) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         String code;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             //                                                    1
             sql = "SELECT SDATA FROM " + TBL_SCRIPTS + " WHERE ID=?";
             ps = con.prepareStatement(sql);
             ps.setLong(1, scriptId);
             ResultSet rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 throw new FxNotFoundException("ex.scripting.notFound.id", scriptId);
             code = rs.getString(1);
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxLoadException(LOG, exc, "ex.scripting.load.failed", scriptId, exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
         }
         return code;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<FxScriptInfo> getScriptInfos() throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         ArrayList<FxScriptInfo> slist = new ArrayList<FxScriptInfo>();
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             //            1     2     3     4    5
             sql = "SELECT ID, SNAME,SDESC,STYPE,ACTIVE FROM " + TBL_SCRIPTS + " ORDER BY ID";
             ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
 
             while (rs != null && rs.next()) {
                 slist.add(new FxScriptInfo(rs.getInt(1), FxScriptEvent.getById(rs.getLong(4)), rs.getString(2),
                         rs.getString(3), rs.getBoolean(5)));
             }
 
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxLoadException(LOG, exc, "ex.scripts.load.failed", exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
         }
         return slist;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void updateScriptInfo(long scriptId, FxScriptEvent event, String name, String description, String code, boolean active) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         boolean success = false;
         try {
             if (code == null)
                 code = "";
             // Obtain a database connection
             con = Database.getDbConnection();
             //                                          1       2       3       4    5            6
             sql = "UPDATE " + TBL_SCRIPTS + " SET SNAME=?,SDESC=?,SDATA=?,STYPE=?,ACTIVE=? WHERE ID=?";
             ps = con.prepareStatement(sql);
             ps.setString(1, name);
             ps.setString(2, description);
             ps.setString(3, code);
             ps.setLong(4, event.getId());
             ps.setBoolean(5, active);
             ps.setLong(6, scriptId);
             ps.executeUpdate();
             success = true;
         } catch (SQLException exc) {
             throw new FxUpdateException(LOG, exc, "ex.scripting.update.failed", name, exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
             if (!success)
                 EJBUtils.rollback(ctx);
             else
                 StructureLoader.reloadScripting(FxContext.get().getDivisionId());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void updateScriptInfo(FxScriptInfoEdit script) throws FxApplicationException {
         updateScriptInfo(script.getId(), script.getEvent(), script.getName(), script.getDescription(), script.getCode(), script.isActive());
     }
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void updateScriptCode(long scriptId, String code) throws FxApplicationException {
         FxScriptInfo si = CacheAdmin.getEnvironment().getScript(scriptId);
         updateScriptInfo(si.getId(), si.getEvent(), si.getName(), si.getDescription(), code, si.isActive());
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<Long> getByScriptEvent(FxScriptEvent scriptEvent) {
         long timeStamp = CacheAdmin.getEnvironment().getTimeStamp();
         if (timeStamp != LocalScriptingCache.scriptCacheTimestamp)
             resetLocalCaches(timeStamp);
         List<Long> cached = LocalScriptingCache.scriptsByEvent.get(scriptEvent);
         if (cached != null)
             return cached;
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         List<Long> ret = new ArrayList<Long>(10);
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             //                                                              1
             sql = "SELECT DISTINCT ID FROM " + TBL_SCRIPTS + " WHERE STYPE=? ORDER BY ID";
             ps = con.prepareStatement(sql);
             ps.setLong(1, scriptEvent.getId());
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next())
                 ret.add(rs.getLong(1));
         } catch (SQLException exc) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage()).asRuntimeException();
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
         }
         LocalScriptingCache.scriptsByEvent.put(scriptEvent, Collections.unmodifiableList(ret));
         return ret;
     }
 
     /**
      * Reset all local caches in use
      *
      * @param timeStamp new timestamp to use for comparing the script cache
      */
     private void resetLocalCaches(long timeStamp) {
         LocalScriptingCache.scriptCacheTimestamp = timeStamp;
         LocalScriptingCache.groovyScriptCache.clear();
         LocalScriptingCache.scriptsByEvent.clear();
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"unchecked"})
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<FxScriptRunInfo> getRunOnceInformation() throws FxApplicationException {
         if (LocalScriptingCache.runOnceInfos != null) {
             return Lists.newArrayList(LocalScriptingCache.runOnceInfos);
         } else {
             return getDivisionConfigurationEngine().get(SystemParameters.DIVISION_RUNONCE_INFOS);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxScriptInfo createScript(FxScriptEvent event, String name, String description, String code) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         FxScriptInfo si;
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         boolean success = false;
         try {
             si = new FxScriptInfo(seq.getId(FxSystemSequencer.SCRIPTS), event, name, description, true);
             if (code == null)
                 code = "";
             // Obtain a database connection
             con = Database.getDbConnection();
             //                                      1  2     3     4     5       6
             sql = "INSERT INTO " + TBL_SCRIPTS + " (ID,SNAME,SDESC,SDATA,STYPE,ACTIVE) VALUES (?,?,?,?,?,?)";
             ps = con.prepareStatement(sql);
             ps.setLong(1, si.getId());
             ps.setString(2, si.getName());
             ps.setString(3, si.getDescription());
             ps.setString(4, code);
             ps.setLong(5, si.getEvent().getId());
             ps.setBoolean(6, si.isActive());
             ps.executeUpdate();
             success = true;
         } catch (SQLException exc) {
             if (StorageManager.isUniqueConstraintViolation(exc))
                 throw new FxEntryExistsException("ex.scripting.name.notUnique", name);
             throw new FxCreateException(LOG, exc, "ex.scripting.create.failed", name, exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
             if (!success)
                 EJBUtils.rollback(ctx);
             else
                 StructureLoader.reloadScripting(FxContext.get().getDivisionId());
         }
         return si;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxScriptInfo createScriptFromLibrary(FxScriptEvent event, String libraryname, String name, String description) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         String code = FxSharedUtils.loadFromInputStream(FxSharedUtils.getResourceStream("fxresources/scripts/library/" + libraryname), -1);
         if (code == null || code.length() == 0)
             throw new FxNotFoundException("ex.scripting.load.library.failed", libraryname);
         return createScript(event, name, description, code);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxScriptInfo createScriptFromDropLibrary(String dropName, FxScriptEvent event, String libraryname, String name, String description) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         String code = FxSharedUtils.loadFromInputStream(FxSharedUtils.getResourceStream(dropName + "Resources/scripts/library/" + libraryname), -1);
         if (code == null || code.length() == 0) { // this might be a jar file
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Failed to locate script in regular application library, checking for drop application in classpath JARs");
             }
             try {
                 final FxDropApplication dropApplication = FxSharedUtils.getDropApplication(dropName);
                 final Map<String, String> libraries = dropApplication.loadTextResources("scripts/library/");
                 for (Map.Entry<String, String> libEntry : libraries.entrySet()) {
                     if (libEntry.getKey().endsWith('/' + libraryname) || libEntry.getKey().endsWith(File.separator + libraryname)) {
                         code = libEntry.getValue();
                         break;
                     }
                 }
             } catch (Exception e) {
                 LOG.error("Failed to load library scripts for " + dropName + " from JAR file: " + e.getMessage(), e);
             }
         }
 
         if (code == null || code.length() == 0) {
             throw new FxNotFoundException("ex.scripting.load.library.failed", libraryname);
         }
         return createScript(event, name, description, code);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void remove(long scriptId) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         boolean success = false;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             sql = "DELETE FROM " + TBL_SCRIPT_MAPPING_ASSIGN + " WHERE SCRIPT=?";
             ps = con.prepareStatement(sql);
             ps.setLong(1, scriptId);
             ps.executeUpdate();
             ps.close();
             sql = "DELETE FROM " + TBL_SCRIPT_MAPPING_TYPES + " WHERE SCRIPT=?";
             ps = con.prepareStatement(sql);
             ps.setLong(1, scriptId);
             ps.executeUpdate();
             ps.close();
             //                                                    1
             sql = "DELETE FROM " + TBL_SCRIPTS + " WHERE ID=?";
             ps = con.prepareStatement(sql);
             ps.setLong(1, scriptId);
             ps.executeUpdate();
             success = true;
         } catch (SQLException exc) {
             throw new FxRemoveException(LOG, exc, "ex.scripting.remove.failed", scriptId, exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
             if (!success)
                 EJBUtils.rollback(ctx);
             else
                 StructureLoader.reloadScripting(FxContext.get().getDivisionId());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxScriptResult runScript(String scriptName, FxScriptBinding binding) throws FxApplicationException {
         return runScript(CacheAdmin.getEnvironment().getScript(scriptName).getId(), binding);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxScriptResult runScript(long scriptId, FxScriptBinding binding) throws FxApplicationException {
         FxScriptInfo si = CacheAdmin.getEnvironment().getScript(scriptId);
 
         if (!si.isActive()) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Script [" + si.getName() + "], Id " + si.getId() + " is deactivated and will not be run!");
             }
             return new FxScriptResult(binding, null);
         }
 
         if (!FxSharedUtils.isGroovyScript(si.getName()))
             return LocalScriptingCache.internal_runScript(si.getName(), binding, loadScriptCode(si.getId()));
 
         if (si.getEvent() == FxScriptEvent.Manual)
             FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptExecution);
 
         long timeStamp = CacheAdmin.getEnvironment().getTimeStamp();
         if (timeStamp != LocalScriptingCache.scriptCacheTimestamp)
             resetLocalCaches(timeStamp);
         Script script = LocalScriptingCache.groovyScriptCache.get(scriptId);
         if (script == null) {
             try {
                 GroovyShell shell = new GroovyShell();
                 script = shell.parse(loadScriptCode(scriptId));
             } catch (CompilationFailedException e) {
                throw new FxInvalidParameterException(si.getName(), LOG, "ex.general.scripting.compileFailed", si.getName(), e.getMessage());
             } catch (Throwable t) {
                 throw new FxInvalidParameterException(si.getName(), "ex.general.scripting.exception", si.getName(), t.getMessage());
             }
             LocalScriptingCache.groovyScriptCache.putIfAbsent(scriptId, script);
         }
 
         if (binding == null)
             binding = new FxScriptBinding();
         if (!binding.getProperties().containsKey("ticket"))
             binding.setVariable("ticket", FxContext.getUserTicket());
         if (!binding.getProperties().containsKey("environment"))
             binding.setVariable("environment", CacheAdmin.getEnvironment());
         binding.setVariable("scriptname", si.getName());
 
         try {
             Object result;
             synchronized (script) {
                 script.setBinding(new Binding(binding.getProperties()));
                 result = script.run();
             }
             return new FxScriptResult(binding, result);
         } catch (Throwable e) {
             if (e instanceof FxApplicationException)
                 throw (FxApplicationException) e;
             LOG.error("Scripting error: " + e.getMessage(), e);
             throw new FxInvalidParameterException(si.getName(), "ex.general.scripting.exception", si.getName(), e.getMessage());
         } finally {
             // don't leave binding in script cache
             synchronized (script) {
                 script.setBinding(null);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxScriptResult runScript(long scriptId) throws FxApplicationException {
         return runScript(scriptId, null);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxScriptMappingEntry createAssignmentScriptMapping(FxScriptEvent scriptEvent, long scriptId, long assignmentId, boolean active, boolean derivedUsage) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         FxScriptMappingEntry sm;
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         boolean success = false;
         //check existance
         CacheAdmin.getEnvironment().getScript(scriptId);
         checkAssignmentScriptConsistency(scriptId, assignmentId, scriptEvent, active, derivedUsage);
         try {
             long[] derived;
             if (!derivedUsage)
                 derived = new long[0];
             else {
                 List<FxAssignment> ass = CacheAdmin.getEnvironment().getDerivedAssignments(assignmentId);
                 derived = new long[ass.size()];
                 for (int i = 0; i < ass.size(); i++)
                     derived[i] = ass.get(i).getId();
             }
             sm = new FxScriptMappingEntry(scriptEvent, scriptId, active, derivedUsage, assignmentId, derived);
             // Obtain a database connection
             con = Database.getDbConnection();
             sql = "INSERT INTO " + TBL_SCRIPT_MAPPING_ASSIGN + " (ASSIGNMENT,SCRIPT,DERIVED_USAGE,ACTIVE,STYPE) VALUES " +
                     //1,2,3,4,5
                     "(?,?,?,?,?)";
             ps = con.prepareStatement(sql);
             ps.setLong(1, sm.getId());
             ps.setLong(2, sm.getScriptId());
             ps.setBoolean(3, sm.isDerivedUsage());
             ps.setBoolean(4, sm.isActive());
             ps.setLong(5, sm.getScriptEvent().getId());
             ps.executeUpdate();
             success = true;
         } catch (SQLException exc) {
             if (StorageManager.isUniqueConstraintViolation(exc))
                 throw new FxEntryExistsException("ex.scripting.mapping.assign.notUnique", scriptId, assignmentId);
             throw new FxCreateException(LOG, exc, "ex.scripting.mapping.assign.create.failed", scriptId, assignmentId, exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
             if (!success)
                 EJBUtils.rollback(ctx);
             else
                 StructureLoader.reloadScripting(FxContext.get().getDivisionId());
         }
         return sm;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxScriptMappingEntry createAssignmentScriptMapping(long scriptId, long typeId, boolean active, boolean derivedUsage) throws FxApplicationException {
         FxScriptInfo si = CacheAdmin.getEnvironment().getScript(scriptId);
         return createAssignmentScriptMapping(si.getEvent(), scriptId, typeId, active, derivedUsage);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxScriptMappingEntry createTypeScriptMapping(FxScriptEvent scriptEvent, long scriptId, long typeId, boolean active, boolean derivedUsage) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         FxScriptMappingEntry sm;
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         boolean success = false;
         //check existance
         CacheAdmin.getEnvironment().getScript(scriptId);
         //check consistency
         checkTypeScriptConsistency(scriptId, typeId, scriptEvent, active, derivedUsage);
         try {
             long[] derived;
             if (!derivedUsage)
                 derived = new long[0];
             else {
                 List<FxType> types = CacheAdmin.getEnvironment().getType(typeId).getDerivedTypes();
                 derived = new long[types.size()];
                 for (int i = 0; i < types.size(); i++)
                     derived[i] = types.get(i).getId();
             }
             sm = new FxScriptMappingEntry(scriptEvent, scriptId, active, derivedUsage, typeId, derived);
             // Obtain a database connection
             con = Database.getDbConnection();
             sql = "INSERT INTO " + TBL_SCRIPT_MAPPING_TYPES + " (TYPEDEF,SCRIPT,DERIVED_USAGE,ACTIVE,STYPE) VALUES " +
                     //1,2,3,4,5
                     "(?,?,?,?,?)";
             ps = con.prepareStatement(sql);
             ps.setLong(1, sm.getId());
             ps.setLong(2, sm.getScriptId());
             ps.setBoolean(3, sm.isDerivedUsage());
             ps.setBoolean(4, sm.isActive());
             ps.setLong(5, sm.getScriptEvent().getId());
             ps.executeUpdate();
             success = true;
         } catch (SQLException exc) {
             if (StorageManager.isUniqueConstraintViolation(exc))
                 throw new FxEntryExistsException("ex.scripting.mapping.type.notUnique", scriptId, typeId);
             throw new FxCreateException(LOG, exc, "ex.scripting.mapping.type.create.failed", scriptId, typeId, exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
             if (!success)
                 EJBUtils.rollback(ctx);
             else
                 StructureLoader.reloadScripting(FxContext.get().getDivisionId());
         }
         return sm;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxScriptMappingEntry createTypeScriptMapping(long scriptId, long typeId, boolean active, boolean derivedUsage) throws FxApplicationException {
         FxScriptInfo si = CacheAdmin.getEnvironment().getScript(scriptId);
         return createTypeScriptMapping(si.getEvent(), scriptId, typeId, active, derivedUsage);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeAssignmentScriptMapping(long scriptId, long assignmentId) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         boolean success = false;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             //                                                                1                2
             sql = "DELETE FROM " + TBL_SCRIPT_MAPPING_ASSIGN + " WHERE SCRIPT=? AND ASSIGNMENT=?";
             ps = con.prepareStatement(sql);
             ps.setLong(1, scriptId);
             ps.setLong(2, assignmentId);
             ps.executeUpdate();
             success = true;
         } catch (SQLException exc) {
             throw new FxRemoveException(LOG, exc, "ex.scripting.mapping.assign.remove.failed", scriptId, assignmentId, exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
             if (!success)
                 EJBUtils.rollback(ctx);
             else
                 StructureLoader.reloadScripting(FxContext.get().getDivisionId());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeAssignmentScriptMappingForEvent(long scriptId, long assignmentId, FxScriptEvent event) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         boolean success = false;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             //                                                                1                2
             sql = "DELETE FROM " + TBL_SCRIPT_MAPPING_ASSIGN + " WHERE SCRIPT=? AND ASSIGNMENT=? AND STYPE=?";
             ps = con.prepareStatement(sql);
             ps.setLong(1, scriptId);
             ps.setLong(2, assignmentId);
             ps.setLong(3, event.getId());
             ps.executeUpdate();
             success = true;
         } catch (SQLException exc) {
             throw new FxRemoveException(LOG, exc, "ex.scripting.mapping.assign.remove.failed", scriptId, assignmentId, exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
             if (!success)
                 EJBUtils.rollback(ctx);
             else
                 StructureLoader.reloadScripting(FxContext.get().getDivisionId());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeTypeScriptMapping(long scriptId, long typeId) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         boolean success = false;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             //                                                               1                2
             sql = "DELETE FROM " + TBL_SCRIPT_MAPPING_TYPES + " WHERE SCRIPT=? AND TYPEDEF=?";
             ps = con.prepareStatement(sql);
             ps.setLong(1, scriptId);
             ps.setLong(2, typeId);
             ps.executeUpdate();
             success = true;
         } catch (SQLException exc) {
             throw new FxRemoveException(LOG, exc, "ex.scripting.mapping.type.remove.failed", scriptId, typeId, exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
             if (!success)
                 EJBUtils.rollback(ctx);
             else
                 StructureLoader.reloadScripting(FxContext.get().getDivisionId());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeTypeScriptMappingForEvent(long scriptId, long typeId, FxScriptEvent event) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         boolean success = false;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             //                                                               1                2
             sql = "DELETE FROM " + TBL_SCRIPT_MAPPING_TYPES + " WHERE SCRIPT=? AND TYPEDEF=? AND STYPE=?";
             ps = con.prepareStatement(sql);
             ps.setLong(1, scriptId);
             ps.setLong(2, typeId);
             ps.setLong(3, event.getId());
             ps.executeUpdate();
             success = true;
         } catch (SQLException exc) {
             throw new FxRemoveException(LOG, exc, "ex.scripting.mapping.type.remove.failed", scriptId, typeId, exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
             if (!success)
                 EJBUtils.rollback(ctx);
             else
                 StructureLoader.reloadScripting(FxContext.get().getDivisionId());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxScriptMappingEntry updateAssignmentScriptMappingForEvent(long scriptId, long assignmentId, FxScriptEvent event, boolean active, boolean derivedUsage) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         FxScriptMappingEntry sm;
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         boolean success = false;
         //check script existance
         CacheAdmin.getEnvironment().getScript(scriptId);
         checkAssignmentScriptConsistency(scriptId, assignmentId, event, active, derivedUsage);
         try {
             long[] derived;
             if (!derivedUsage)
                 derived = new long[0];
             else {
                 List<FxAssignment> ass = CacheAdmin.getEnvironment().getDerivedAssignments(assignmentId);
                 derived = new long[ass.size()];
                 for (int i = 0; i < ass.size(); i++)
                     derived[i] = ass.get(i).getId();
             }
             sm = new FxScriptMappingEntry(event, scriptId, active, derivedUsage, assignmentId, derived);
             // Obtain a database connection
             con = Database.getDbConnection();
             //                                                                1        2                  3            4
             sql = "UPDATE " + TBL_SCRIPT_MAPPING_ASSIGN + " SET DERIVED_USAGE=?,ACTIVE=? WHERE ASSIGNMENT=? AND SCRIPT=? AND STYPE=?";
             ps = con.prepareStatement(sql);
             ps.setBoolean(1, sm.isDerivedUsage());
             ps.setBoolean(2, sm.isActive());
             ps.setLong(3, sm.getId());
             ps.setLong(4, sm.getScriptId());
             ps.setLong(5, sm.getScriptEvent().getId());
             ps.executeUpdate();
             success = true;
         } catch (SQLException exc) {
             if (StorageManager.isUniqueConstraintViolation(exc))
                 throw new FxEntryExistsException("ex.scripting.mapping.assign.notUnique", scriptId, assignmentId);
             throw new FxUpdateException(LOG, exc, "ex.scripting.mapping.assign.update.failed", scriptId, assignmentId, exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
             if (!success)
                 EJBUtils.rollback(ctx);
             else
                 StructureLoader.reloadScripting(FxContext.get().getDivisionId());
         }
         return sm;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxScriptMappingEntry updateTypeScriptMappingForEvent(long scriptId, long typeId, FxScriptEvent event, boolean active, boolean derivedUsage) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptManagement);
         FxScriptMappingEntry sm;
         Connection con = null;
         PreparedStatement ps = null;
         String sql;
         boolean success = false;
         //check existance
         CacheAdmin.getEnvironment().getScript(scriptId);
         //check consistency
         checkTypeScriptConsistency(scriptId, typeId, event, active, derivedUsage);
         try {
             long[] derived;
             if (!derivedUsage)
                 derived = new long[0];
             else {
                 List<FxType> types = CacheAdmin.getEnvironment().getType(typeId).getDerivedTypes();
                 derived = new long[types.size()];
                 for (int i = 0; i < types.size(); i++)
                     derived[i] = types.get(i).getId();
             }
             sm = new FxScriptMappingEntry(event, scriptId, active, derivedUsage, typeId, derived);
             // Obtain a database connection
             con = Database.getDbConnection();
             //                                                               1        2             3          4          5
             sql = "UPDATE " + TBL_SCRIPT_MAPPING_TYPES + " SET DERIVED_USAGE=?,ACTIVE=? WHERE TYPEDEF=? AND SCRIPT=? AND STYPE=?";
             ps = con.prepareStatement(sql);
             ps.setBoolean(1, sm.isDerivedUsage());
             ps.setBoolean(2, sm.isActive());
             ps.setLong(3, sm.getId());
             ps.setLong(4, sm.getScriptId());
             ps.setLong(5, sm.getScriptEvent().getId());
             ps.executeUpdate();
             success = true;
         } catch (SQLException exc) {
             if (StorageManager.isUniqueConstraintViolation(exc))
                 throw new FxEntryExistsException("ex.scripting.mapping.type.notUnique", scriptId, typeId);
             throw new FxUpdateException(LOG, exc, "ex.scripting.mapping.type.update.failed", scriptId, typeId, exc.getMessage());
         } finally {
             Database.closeObjects(ScriptingEngineBean.class, con, ps);
             if (!success)
                 EJBUtils.rollback(ctx);
             else
                 StructureLoader.reloadScripting(FxContext.get().getDivisionId());
         }
         return sm;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void executeRunOnceScripts() throws FxApplicationException {
         final long start = System.currentTimeMillis();
         runOnce(SystemParameters.DIVISION_RUNONCE, "flexive", "fxresources", "[fleXive]");
         if (LOG.isInfoEnabled()) {
             LOG.info("Executed flexive run-once scripts in " + (System.currentTimeMillis() - start) + "ms");
         }
     }
 
     /**
      * Execute all runOnce scripts in the resource denoted by prefix if param is "false"
      *
      * @param param           boolean parameter that will be flagged as "true" once the scripts are run
      * @param dropName        the drop application name
      * @param prefix          resource directory prefix
      * @param applicationName the corresponding application name (for debug messages)
      * @throws FxApplicationException on errors
      */
     private void runOnce(Parameter<Boolean> param, String dropName, String prefix, String applicationName) throws FxApplicationException {
         synchronized (RUNONCE_LOCK) {
             try {
                 Boolean executed = getDivisionConfigurationEngine().get(param);
                 if (executed) {
                     return;
                 }
             } catch (FxApplicationException e) {
                 LOG.error(e);
                 return;
             }
             //noinspection unchecked
             final ArrayList<FxScriptRunInfo> divisionRunOnceInfos = Lists.newArrayList(
                     getDivisionConfigurationEngine().get(SystemParameters.DIVISION_RUNONCE_INFOS)
             );
 
             LocalScriptingCache.runOnceInfos = new CopyOnWriteArrayList<FxScriptRunInfo>();
             LocalScriptingCache.runOnceInfos.addAll(divisionRunOnceInfos);
 
             FxContext.get().setExecutingRunOnceScripts(true);
             try {
                 executeInitializerScripts("runonce", dropName, prefix, applicationName, new RunonceScriptExecutor(applicationName));
             } finally {
                 FxContext.get().setExecutingRunOnceScripts(false);
             }
 
             // TODO: this fails to update the script infos when the transaction was rolled back because of a script error
             FxContext.get().runAsSystem();
             try {
                 divisionRunOnceInfos.clear();
                 divisionRunOnceInfos.addAll(LocalScriptingCache.runOnceInfos);
                 getDivisionConfigurationEngine().put(SystemParameters.DIVISION_RUNONCE_INFOS, divisionRunOnceInfos);
                 getDivisionConfigurationEngine().put(param, true);
                 LocalScriptingCache.runOnceInfos = null;
             } finally {
                 FxContext.get().stopRunAsSystem();
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void executeDropRunOnceScripts(Parameter<Boolean> param, String dropName) throws FxApplicationException {
         if (!FxSharedUtils.getDrops().contains(dropName))
             throw new FxInvalidParameterException("dropName", "ex.scripting.drop.notFound", dropName);
         runOnce(param, dropName, dropName + "Resources", dropName);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void executeStartupScripts() {
         executeInitializerScripts("startup", "flexive", "fxresources", "[fleXive]", new NonFailingScriptExecutor());
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void executeDropStartupScripts(String dropName) throws FxApplicationException {
         if (!FxSharedUtils.getDrops().contains(dropName))
             throw new FxInvalidParameterException("dropName", "ex.scripting.drop.notFound", dropName);
         executeInitializerScripts("startup", dropName, dropName + "Resources", "drop " + dropName,
                 new NonFailingScriptExecutor());
     }
 
     private void executeInitializerScripts(String folder, String dropName, String prefix, String applicationName,
                                            ScriptExecutor scriptExecutor) {
         if (LOG.isInfoEnabled()) {
             LOG.info("Executing " + folder + " scripts for " + applicationName);
         }
         final InputStream scriptIndex = FxSharedUtils.getResourceStream(prefix + "/scripts/" + folder + "/scriptindex.flexive");
         if (scriptIndex != null) {
             // load cached file list from script index file
             for (String entry : FxSharedUtils.loadFromInputStream(scriptIndex, -1).replaceAll("\r", "").split("\n")) {
                 final String[] values = entry.split("\\|"); // name, size
                 if (StringUtils.isNotBlank(values[0])) {
                     scriptExecutor.runScript(
                             values[0],
                             FxSharedUtils.loadFromInputStream(
                                     FxSharedUtils.getResourceStream(prefix + "/scripts/" + folder + "/" + values[0])
                             )
                     );
                 }
             }
         } else {
             // scan classpath from shared resource JAR
             try {
                 final FxDropApplication dropApplication = FxSharedUtils.getDropApplication(dropName);
                 final Map<String, String> resources = dropApplication.loadTextResources("scripts/" + folder + "/");
                 if (resources.isEmpty()) {
                     if (LOG.isInfoEnabled()) {
                         LOG.info("Found no scripts in drop " + dropName + " in path \"scripts/" + folder + "/\"");
                     }
                 } else {
                     // sort by name
                     final List<String> names = Lists.newArrayList(resources.keySet());
                     Collections.sort(names);
 
                     // execute scripts
                     for (String name : names) {
                         scriptExecutor.runScript(
                                 // extract filename
                                 name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf(File.separator)) + 1),
                                 // load script code
                                 resources.get(name)
                         );
                     }
                 }
             } catch (Exception e) {
                 LOG.error("Failed to load " + folder + " scripts for " + dropName + " from JAR file: " + e.getMessage(), e);
             }
         }
     }
 
     /**
      * Helper interface to provide customized "script executors" (startup, runonce scripts).
      */
     private interface ScriptExecutor {
         /**
          * Run the given script.
          *
          * @param name the script filename (extension determines language)
          * @param code the code to be executed
          * @return true if script evaluation should continue
          */
         boolean runScript(String name, String code);
     }
 
     private class NonFailingScriptExecutor implements ScriptExecutor {
         /**
          * {@inheritDoc}
          */
         public boolean runScript(String name, String code) {
             try {
                 if (LOG.isInfoEnabled()) {
                     LOG.info("Running script: " + name);
                 }
                 runInitializerScript(name, code);
             } catch (Exception e) {
                 LOG.error("Failed to run initializer script: " + e.getMessage(), e);
             }
             // continue unless rollback performed due to a script error
             return !ctx.getRollbackOnly();
         }
     }
 
     private class RunonceScriptExecutor implements ScriptExecutor {
         private final String applicationName;
 
         private RunonceScriptExecutor(String applicationName) {
             this.applicationName = applicationName;
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean runScript(String name, String code) {
             FxScriptRunInfo runInfo = new FxScriptRunInfo(System.currentTimeMillis(), applicationName, name);
             // TODO: write in tree cache for cluster support
             LocalScriptingCache.runOnceInfos.add(runInfo);
             try {
                 if (LOG.isInfoEnabled()) {
                     LOG.info("[" + applicationName + " :: " + name + "]");
                 }
                 runInitializerScript(name, code);
                 runInfo.endExecution(true);
             } catch (Exception e) {
                 if (LOG.isWarnEnabled()) {
                     LOG.warn("Failed to execute " + name + ": " + e.getMessage(), e);
                 }
                 runInfo.endExecution(false);
                 final Throwable reported = e instanceof GenericScriptException ? e.getCause() : e;
                 StringWriter sw = new StringWriter();
                 reported.printStackTrace(new PrintWriter(sw));
                 runInfo.setErrorMessage(sw.toString());
             }
             try {
                 Thread.sleep(50); //play nice for GUI list updates ...
             } catch (InterruptedException e) {
                 //ignore
             }
             return !ctx.getRollbackOnly();
         }
     }
 
     private void runInitializerScript(String name, String code) throws FxApplicationException {
         final UserTicket originalTicket = FxContext.getUserTicket();
         FxContext.get().runAsSystem();
         try {
             FxScriptBinding binding = new FxScriptBinding();
             UserTicket ticket = ((UserTicketImpl) UserTicketImpl.getGuestTicket()).cloneAsGlobalSupervisor();
             binding.setVariable("ticket", ticket);
             FxContext.get().overrideTicket(ticket);
 
             LocalScriptingCache.internal_runScript(name, binding, code);
         } finally {
             FxContext.get().stopRunAsSystem();
             FxContext.get().overrideTicket(originalTicket);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxScriptResult runScript(String name, FxScriptBinding binding, String code) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.getUserTicket(), Role.ScriptExecution);
         return LocalScriptingCache.internal_runScript(name, binding, code);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<String[]> getAvailableScriptEngines() throws FxApplicationException {
         List<String[]> ret = null;
         if (FxSharedUtils.USE_JDK6_EXTENSION) {
             try {
                 //noinspection unchecked
                 ret = (List<String[]>) Class.forName("com.flexive.core.JDK6Scripting").
                         getMethod("getAvailableScriptEngines", new Class[0]).
                         invoke(null);
             } catch (Exception e) {
                 LOG.error(e);
             }
         }
         if (ret == null)
             ret = new ArrayList<String[]>(2);
         ret.add(0, new String[]{"groovy", "groovy: Groovy v" + FxSharedUtils.getBundledGroovyVersion() + " (Bundled GroovyShell v" + FxSharedUtils.getBundledGroovyVersion() + ")"});
         ret.add(0, new String[]{"gy", "gy: Groovy v" + FxSharedUtils.getBundledGroovyVersion() + " (Bundled GroovyShell v" + FxSharedUtils.getBundledGroovyVersion() + ")"});
         return ret;
     }
 
     /**
      * {@inheritDoc}
      */
     public FxScriptMapping loadScriptMapping(long scriptId) throws FxLoadException, SQLException {
         FxScriptMapping mapping;
         PreparedStatement ps_a = null, ps_t = null;
         String sql;
         Connection con = Database.getDbConnection();
 
         List<FxScriptMappingEntry> e_ass;
         List<FxScriptMappingEntry> e_types;
         FxEnvironmentImpl environment = ((FxEnvironmentImpl) CacheAdmin.getEnvironment()).deepClone();
 
         try {
             //            1          2             3      4
             sql = "SELECT ASSIGNMENT,DERIVED_USAGE,ACTIVE,STYPE FROM " + TBL_SCRIPT_MAPPING_ASSIGN + " WHERE SCRIPT=?";
             ps_a = con.prepareStatement(sql);
             sql = "SELECT TYPEDEF,DERIVED_USAGE,ACTIVE,STYPE FROM " + TBL_SCRIPT_MAPPING_TYPES + " WHERE SCRIPT=?";
             ps_t = con.prepareStatement(sql);
             ResultSet rs;
 
             ps_a.setLong(1, scriptId);
             ps_t.setLong(1, scriptId);
             rs = ps_a.executeQuery();
             e_ass = new ArrayList<FxScriptMappingEntry>(20);
             e_types = new ArrayList<FxScriptMappingEntry>(20);
             while (rs != null && rs.next()) {
                 long[] derived;
                 if (!rs.getBoolean(2))
                     derived = new long[0];
                 else {
                     List<FxAssignment> ass = environment.getDerivedAssignments(rs.getLong(1));
                     derived = new long[ass.size()];
                     for (int i = 0; i < ass.size(); i++)
                         derived[i] = ass.get(i).getId();
                 }
                 e_ass.add(new FxScriptMappingEntry(FxScriptEvent.getById(rs.getLong(4)), scriptId, rs.getBoolean(3), rs.getBoolean(2), rs.getLong(1), derived));
             }
             rs = ps_t.executeQuery();
             while (rs != null && rs.next()) {
                 long[] derived;
                 if (!rs.getBoolean(2))
                     derived = new long[0];
                 else {
                     List<FxType> types = environment.getType(rs.getLong(1)).getDerivedTypes();
                     derived = new long[types.size()];
                     for (int i = 0; i < types.size(); i++)
                         derived[i] = types.get(i).getId();
                 }
                 e_types.add(new FxScriptMappingEntry(FxScriptEvent.getById(rs.getLong(4)), scriptId, rs.getBoolean(3), rs.getBoolean(2), rs.getLong(1), derived));
             }
             mapping = new FxScriptMapping(scriptId, e_types, e_ass);
 
 
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, exc, "ex.scripting.mapping.load.failed", exc.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxLoadException(e);
         } finally {
             try {
                 if (ps_t != null)
                     ps_t.close();
             } catch (SQLException e) {
                 //ignore
             }
             Database.closeObjects(ScriptingEngineBean.class, con, ps_a);
         }
         return mapping;
 
     }
 
     /**
      * Checks if an assignment script mapping is consistent with
      * derived assignment script mappings that are already present in the DB.
      *
      * @param scriptId     scriptId
      * @param assignmentId assignmentId
      * @param event        script event
      * @param active       active
      * @param derivedUsage derivedUsage
      * @throws FxEntryExistsException on errors
      */
     public void checkAssignmentScriptConsistency(long scriptId, long assignmentId, FxScriptEvent event, boolean active, boolean derivedUsage) throws FxEntryExistsException {
         // in case script is set to active,
         // check if an active derived script assignment for this event already exists for this assignment
         if (active) {
             List<FxScriptMappingEntry> scriptMappings = CacheAdmin.getFilteredEnvironment().getScriptMapping(scriptId).getMappedAssignments();
             for (FxScriptMappingEntry sme : scriptMappings) {
                 if (sme.isActive() && sme.getScriptEvent().getId() == event.getId()) {
                     for (int i = 0; i < sme.getDerivedIds().length; i++) {
                         if (sme.getDerivedIds()[i] == assignmentId)
                             throw new FxEntryExistsException("ex.scripting.mapping.assign.derived.active.exists",
                                     CacheAdmin.getEnvironment().getScript(scriptId).getName(),
                                     event.getName(), CacheAdmin.getEnvironment().getAssignment(assignmentId).getXPath());
                     }
                 }
             }
             // in case of derived usage and active,
             // check if an active script assignment for this event already exists for a sub-assignment
             if (derivedUsage) {
                 for (FxAssignment a : CacheAdmin.getEnvironment().getDerivedAssignments(assignmentId)) {
                     for (FxScriptMappingEntry sme : scriptMappings) {
                         if (sme.getId() == a.getId() && sme.isActive() && sme.getScriptEvent().getId() == event.getId()) {
                             throw new FxEntryExistsException("ex.scripting.mapping.assign.active.derived.exists",
                                     CacheAdmin.getEnvironment().getScript(scriptId).getName(),
                                     event.getName(), a.getXPath());
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Checks if a type script mapping is consistent with
      * derived type script mappings that are already present in the DB.
      *
      * @param scriptId     scriptId
      * @param typeId       typeId
      * @param event        script event
      * @param active       active
      * @param derivedUsage derivedUsage
      * @throws FxEntryExistsException if check fails
      */
     public void checkTypeScriptConsistency(long scriptId, long typeId, FxScriptEvent event, boolean active, boolean derivedUsage) throws FxEntryExistsException {
         // in case script is set to active,
         // check if an active derived script assignment for this event already exists for this type
         if (active) {
             List<FxScriptMappingEntry> scriptMappings = CacheAdmin.getFilteredEnvironment().getScriptMapping(scriptId).getMappedTypes();
             for (FxScriptMappingEntry sme : scriptMappings) {
                 if (sme.isActive() && sme.getScriptEvent().getId() == event.getId()) {
                     for (int i = 0; i < sme.getDerivedIds().length; i++) {
                         if (sme.getDerivedIds()[i] == typeId)
                             throw new FxEntryExistsException("ex.scripting.mapping.type.derived.active.exists",
                                     CacheAdmin.getEnvironment().getScript(scriptId).getName(),
                                     event.getName(),
                                     CacheAdmin.getEnvironment().getType(typeId).getName());
                     }
                 }
             }
             // in case of derived usage and active,
             // check if an active script assignment for this event already exists for a sub-type
             if (derivedUsage) {
                 for (FxType t : CacheAdmin.getEnvironment().getType(typeId).getDerivedTypes()) {
                     for (FxScriptMappingEntry sme : scriptMappings) {
                         if (sme.getId() == t.getId() && sme.isActive() && sme.getScriptEvent().getId() == event.getId()) {
                             throw new FxEntryExistsException("ex.scripting.mapping.type.active.derived.exists",
                                     CacheAdmin.getEnvironment().getScript(scriptId).getName(),
                                     event.getName(),
                                     t.getName());
                         }
                     }
                 }
             }
         }
     }
 }
