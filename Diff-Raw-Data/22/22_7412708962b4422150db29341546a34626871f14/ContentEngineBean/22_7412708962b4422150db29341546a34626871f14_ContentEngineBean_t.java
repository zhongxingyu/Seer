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
 import com.flexive.core.LifeCycleInfoImpl;
 import com.flexive.core.conversion.ConversionEngine;
 import com.flexive.core.flatstorage.FxFlatStorageManager;
 import com.flexive.core.storage.ContentStorage;
 import com.flexive.core.storage.LockStorage;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.core.storage.binary.FxBinaryUtils;
 import com.flexive.shared.*;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.content.*;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.*;
 import com.flexive.shared.scripting.FxScriptBinding;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.security.ACLPermission;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.BinaryDescriptor;
 import com.flexive.shared.value.FxBinary;
 import com.flexive.shared.workflow.Step;
 import com.thoughtworks.xstream.converters.ConversionException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.*;
 
 /**
  * Content Engine implementation
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Stateless(name = "ContentEngine", mappedName = "ContentEngine")
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class ContentEngineBean implements ContentEngine, ContentEngineLocal {
 
     private static final Log LOG = LogFactory.getLog(ContentEngineBean.class);
     @Resource
     javax.ejb.SessionContext ctx;
 
     @EJB
     SequencerEngineLocal seq;
 
     @EJB
     ScriptingEngineLocal scripting;
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxContent initialize(long typeId, long mandatorId, long prefACL, long prefStep, long prefLang)
             throws FxApplicationException {
         UserTicket ticket = FxContext.getUserTicket();
         FxEnvironment environment;
         environment = CacheAdmin.getEnvironment();
         FxPermissionUtils.checkMandatorExistance(mandatorId);
         FxPermissionUtils.checkTypeAvailable(typeId, false);
         FxType type = environment.getType(typeId);
         //security check start
         if (type.isUseTypePermissions() && !ticket.mayCreateACL(type.getACL().getId(), ticket.getUserId()))
             throw new FxNoAccessException("ex.acl.noAccess.create", type.getACL().getName());
         //security check end
         long acl = prefACL;
         try {
             environment.getACL(acl);
         } catch (Exception e) {
             acl = type.hasDefaultInstanceACL() ? type.getDefaultInstanceACL().getId() : ACLCategory.INSTANCE.getDefaultId();
             if (!ticket.isGlobalSupervisor() && type.isUseInstancePermissions() &&
                     !(ticket.mayCreateACL(acl, ticket.getUserId()) &&
                             ticket.mayReadACL(acl, ticket.getUserId()) &&
                             ticket.mayEditACL(acl, ticket.getUserId()))) {
                 //get best fit if possible
                 Long[] acls = ticket.getACLsId(ticket.getUserId(), ACLCategory.INSTANCE, ACLPermission.CREATE, ACLPermission.EDIT, ACLPermission.READ);
                 if (acls.length > 0)
                     acl = acls[0];
                 else
                     throw new FxNoAccessException("ex.content.noSuitableACL", type.getName());
             }
         }
         long step = -2;
         for (Step check : type.getWorkflow().getSteps()) {
             if (ticket.mayCreateACL(check.getAclId(), ticket.getUserId()) &&
                     ticket.mayReadACL(check.getAclId(), ticket.getUserId()) &&
                     ticket.mayEditACL(check.getAclId(), ticket.getUserId())) {
                 if (check.getId() == prefStep) {
                     step = check.getId();
                     break;
                 } else if (step == -2)
                     step = check.getId(); //first match
             }
         }
         if (step < 0)
             throw new FxInvalidParameterException("STEP", "ex.content.noSuitableStep", type.getName(), type.getWorkflow().getName());
         long lang = prefLang;
         try {
             environment.getLanguage(lang);
         } catch (FxRuntimeException e) {
             lang = ticket.getLanguage().getId();
         }
         FxPK sourcePK = null, destinationPK = null;
         int sourcePos = 0, destinationPos = 0;
         if (type.isRelation()) {
             sourcePK = FxPK.createNewPK();
             destinationPK = FxPK.createNewPK();
             sourcePos = destinationPos = 0;
         }
         FxContent content = new FxContent(FxPK.createNewPK(), FxLock.noLockPK(), type.getId(), type.isRelation(), mandatorId, acl, step, 1,
                 environment.getStep(step).isLiveStep() ? 1 : 0, true, lang,
                 sourcePK, destinationPK, sourcePos, destinationPos,
                 LifeCycleInfoImpl.createNew(ticket), type.createEmptyData(type.buildXPathPrefix(FxPK.createNewPK())), BinaryDescriptor.SYS_UNKNOWN, 1).initSystemProperties();
         //scripting after start
         FxScriptBinding binding = null;
         long[] scripts = type.getScriptMapping(FxScriptEvent.AfterContentInitialize);
         if (scripts != null)
             for (long script : scripts) {
                 if (binding == null)
                     binding = new FxScriptBinding();
                 binding.setVariable("content", content);
                 scripting.runScript(script, binding);
             }
         //scripting after end
         return content;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxContent initialize(long typeId) throws FxApplicationException {
         FxType type = CacheAdmin.getEnvironment().getType(typeId);
         UserTicket ticket = FxContext.getUserTicket();
         return initialize(type.getId(), ticket.getMandatorId(),
                 type.hasDefaultInstanceACL() ? type.getDefaultInstanceACL().getId() : -1 /*invalid ACL will cause a lookup for best-fit ACL*/,
                 -1 /*invalid Step will cause a lookup for best-fit Step*/, ticket.getLanguage().getId());
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxContent initialize(String typeName) throws FxApplicationException {
         return initialize(CacheAdmin.getEnvironment().getType(typeName).getId());
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxContent load(FxPK pk) throws FxApplicationException {
 //        long time = System.currentTimeMillis();
 //        System.out.println("=> Cache check for " + pk);
         FxCachedContent cachedContent = CacheAdmin.getCachedContent(pk);
         FxEnvironment env = CacheAdmin.getEnvironment();
         Connection con = null;
         PreparedStatement ps = null;
         try {
             if (cachedContent == null) {
 //                System.out.println("=> Cache miss for " + pk);
                 ContentStorage storage = StorageManager.getContentStorage(pk.getStorageMode());
                 StringBuilder sql = new StringBuilder(2000);
                 con = Database.getDbConnection();
                 FxContent rawContent = storage.contentLoad(con, pk, env, sql).copy();
                 cachedContent = new FxCachedContent(rawContent, storage.getContentSecurityInfo(con, pk, rawContent));
                 CacheAdmin.cacheContent(cachedContent);
 //                System.out.println("=> Cached " + pk);
             } else {
 //                System.out.println("=> Cache hit for " + pk);
             }
             FxContent content = cachedContent.getContent().copy();
 
             //security check start
             UserTicket ticket = FxContext.getUserTicket();
             FxType type = env.getType(cachedContent.getContent().getTypeId());
             FxPermissionUtils.checkPermission(ticket, content.getLifeCycleInfo().getCreatorId(), ACLPermission.READ, type,
                     cachedContent.getSecurityInfo().getStepACL(),
                     cachedContent.getSecurityInfo().getContentACLs(), true);
             FxPermissionUtils.checkMandatorExistance(content.getMandatorId());
             FxPermissionUtils.checkTypeAvailable(type.getId(), true);
             if (type.isUsePropertyPermissions() && !ticket.isGlobalSupervisor()) {
                 //wrap with FxNoAccess or set to readonly when appropriate
                 FxPermissionUtils.wrapNoAccessValues(ticket, cachedContent.getSecurityInfo(),
                         content, type, env);
             }
             //security check end
             //scripting after start
             FxScriptBinding binding = null;
             long[] scripts = env.getType(content.getTypeId()).getScriptMapping(FxScriptEvent.AfterContentLoad);
             if (scripts != null)
                 for (long script : scripts) {
                     if (binding == null)
                         binding = new FxScriptBinding();
                     binding.setVariable("content", content);
                     scripting.runScript(script, binding);
                 }
             //scripting after end
             content.getRootGroup().removeEmptyEntries();
             content.getRootGroup().compactPositions(true);
             return content;
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, ps);
 //            System.out.println("=> Load time: " + (System.currentTimeMillis() - time));
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxContentContainer loadContainer(long id) throws FxApplicationException {
         FxContentVersionInfo vi = getContentVersionInfo(new FxPK(id));
         List<FxContent> contents = new ArrayList<FxContent>(vi.getVersionCount());
         for (int ver : vi.getVersions())
             contents.add(load(new FxPK(id, ver)));
         return new FxContentContainer(vi, contents);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxPK save(FxContent content) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         FxPK pk;
         FxScriptEvent beforeAssignmentScript, afterAssignmentScript;
         try {
             FxEnvironment env = CacheAdmin.getEnvironment();
             FxPermissionUtils.checkMandatorExistance(content.getMandatorId());
             FxPermissionUtils.checkTypeAvailable(content.getTypeId(), false);
             ContentStorage storage = StorageManager.getContentStorage(content.getPk().getStorageMode());
             con = Database.getDbConnection();
 
             //security check start
             FxType type = env.getType(content.getTypeId());
             Step step = env.getStep(content.getStepId());
             UserTicket ticket = FxContext.getUserTicket();
             if (content.getPk().isNew()) {
                 FxPermissionUtils.checkPermission(ticket, ticket.getUserId(), ACLPermission.CREATE, type, step.getAclId(), content.getAclIds(), true);
                 beforeAssignmentScript = FxScriptEvent.BeforeAssignmentDataCreate;
                 afterAssignmentScript = FxScriptEvent.AfterAssignmentDataCreate;
             } else {
                 FxPermissionUtils.checkPermission(ticket, ACLPermission.EDIT, getContentSecurityInfo(content.getPk()), true);
                 beforeAssignmentScript = FxScriptEvent.BeforeAssignmentDataSave;
                 afterAssignmentScript = FxScriptEvent.AfterAssignmentDataSave;
             }
             if (type.isUsePropertyPermissions() && !ticket.isGlobalSupervisor() && content.getPk().isNew())
                 FxPermissionUtils.checkPropertyPermissions(content, ACLPermission.CREATE);
             //security check end
 
             content = prepareSave(con, storage, content);
 
             FxScriptBinding binding = null;
             long[] typeScripts;
             //scripting before start
             //type scripting
             typeScripts = type.getScriptMapping(
                     content.getPk().isNew()
                             ? FxScriptEvent.BeforeContentCreate
                             : FxScriptEvent.BeforeContentSave);
             if (typeScripts != null)
                 for (long script : typeScripts) {
                     if (binding == null)
                         binding = new FxScriptBinding();
                     binding.setVariable("content", content);
                     Object result = scripting.runScript(script, binding).getResult();
                     if (result != null && result instanceof FxContent) {
                         content = (FxContent) result;
                     }
                 }
             //assignment scripting
             if (type.hasScriptedAssignments()) {
                 binding = null;
                 for (FxAssignment as : type.getScriptedAssignments(beforeAssignmentScript)) {
                     if (binding == null)
                         binding = new FxScriptBinding();
                     binding.setVariable("assignment", as);
                     for (long script : as.getScriptMapping(beforeAssignmentScript)) {
                         binding.setVariable("content", content);
                         Object result = scripting.runScript(script, binding).getResult();
                         if (result != null && result instanceof FxContent) {
                             content = (FxContent) result;
                         }
                     }
                 }
             }
             //scripting before end
             if (content.getPk().isNew()) {
                 pk = storage.contentCreate(con, env, null, seq.getId(FxSystemSequencer.CONTENT), content);
                 if (LOG.isDebugEnabled())
                     LOG.debug("creating new content for type " + CacheAdmin.getEnvironment().getType(content.getTypeId()).getName());
             } else {
                 pk = storage.contentSave(con, env, null, content, EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_FQN_PROPERTY));
             }
             //scripting after start
             //assignment scripting
             if (type.hasScriptedAssignments()) {
                 binding = new FxScriptBinding();
                 binding.setVariable("pk", pk);
                 for (FxAssignment as : type.getScriptedAssignments(afterAssignmentScript)) {
                     binding.setVariable("assignment", as);
                     for (long script : as.getScriptMapping(afterAssignmentScript)) {
                         scripting.runScript(script, binding);
                     }
                 }
             }
             //type scripting
             typeScripts = env.getType(content.getTypeId()).getScriptMapping(
                     content.getPk().isNew()
                             ? FxScriptEvent.AfterContentCreate
                             : FxScriptEvent.AfterContentSave);
             if (typeScripts != null)
                 for (long script : typeScripts) {
                     if (binding == null)
                         binding = new FxScriptBinding();
                     binding.setVariable("pk", pk);
                     scripting.runScript(script, binding);
                 }
             //scripting after end
             return pk;
         } catch (FxNotFoundException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e);
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
 //            if (Database.isUniqueConstraintViolation(e))
 //                throw new FxEntryExistsException("ex.structure.property.exists", property.getName(), (parentXPath.length() == 0 ? "/" : parentXPath));
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxInvalidParameterException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e);
         } catch (FxCreateException e) {
             EJBUtils.rollback(ctx);
             throw e;
         } catch (FxNoAccessException e) {
             EJBUtils.rollback(ctx);
             throw e;
         } catch (Throwable t) {
             EJBUtils.rollback(ctx);
             if (t instanceof FxApplicationException)
                 throw new FxCreateException(t); //no logging
             else
                 throw new FxCreateException(LOG, t);
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, ps);
             if (ctx.getRollbackOnly()) {
                 FxBinaryUtils.removeTXFiles();
             } else
                 FxBinaryUtils.resetTXFiles();
             if (!ctx.getRollbackOnly())
                 CacheAdmin.expireCachedContent(content.getId());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxPK createNewVersion(FxContent content) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         try {
             //security check start
             FxEnvironment env = CacheAdmin.getEnvironment();
             FxType type = env.getType(content.getTypeId());
             Step step = env.getStep(content.getStepId());
             UserTicket ticket = FxContext.getUserTicket();
             FxPermissionUtils.checkMandatorExistance(content.getMandatorId());
             FxPermissionUtils.checkTypeAvailable(type.getId(), false);
 
             con = Database.getDbConnection();
             //check edit permission on current version
             final ContentStorage storage = StorageManager.getContentStorage(content.getPk().getStorageMode());
             FxContentSecurityInfo si = storage.getContentSecurityInfo(con, content.getPk(), null);
             FxPermissionUtils.checkPermission(FxContext.getUserTicket(), ACLPermission.EDIT, si, true);
 
             FxPermissionUtils.checkPermission(ticket, ticket.getUserId(), ACLPermission.CREATE, type, step.getAclId(), content.getAclIds(), true);
             FxContent prepared = prepareSave(con, storage, content);
             //security check end
             return storage.contentCreateVersion(con, CacheAdmin.getEnvironment(), null, prepared);
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxNotFoundException e) {
             EJBUtils.rollback(ctx);
             throw e;
         } catch (FxApplicationException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e);
         } catch (Exception e) {
             EJBUtils.rollback(ctx);
             System.out.println("===> generic exception!!! " + e.getClass().getCanonicalName());
             throw new FxCreateException(LOG, e);
         } catch (Throwable t) {
             EJBUtils.rollback(ctx);
             System.out.println("===> generic throwable!!! " + t.getClass().getCanonicalName());
             throw new FxCreateException(LOG, t);
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, ps);
             if (!ctx.getRollbackOnly())
                 CacheAdmin.expireCachedContent(content.getId());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxContent prepareSave(FxContent content) throws FxApplicationException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return prepareSave(con, StorageManager.getContentStorage(content.getPk().getStorageMode()), content);
         } catch (FxNotFoundException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e);
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxInvalidParameterException e) {
             EJBUtils.rollback(ctx);
             throw new FxCreateException(e);
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * Prepare a content for a save or create operation (resolves binaries for script processing, etc.).
      *
      * @param con     an open and valid connection
      * @param storage content storage to use
      * @param content the content to prepare
      * @return the prepared content
      * @throws FxApplicationException on errors
      * @throws SQLException           on errors
      */
     protected FxContent prepareSave(Connection con, ContentStorage storage, FxContent content) throws FxApplicationException, SQLException {
         storage.prepareSave(con, content);
         FxScriptBinding binding = null;
         long[] scripts;
         //scripting before start
         scripts = CacheAdmin.getEnvironment().getType(content.getTypeId()).getScriptMapping(
                 content.getPk().isNew()
                         ? FxScriptEvent.PrepareContentCreate
                         : FxScriptEvent.PrepareContentSave);
         if (scripts != null)
             for (long script : scripts) {
                 if (binding == null)
                     binding = new FxScriptBinding();
                 binding.setVariable("content", content);
                 Object result = scripting.runScript(script, binding).getResult();
                 if (result != null && result instanceof FxContent) {
 //                        System.out.println("setting result");
                     content = (FxContent) result;
                 }
             }
         //scripting before end
         return content;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void remove(FxPK pk) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         try {
             ContentStorage storage = StorageManager.getContentStorage(pk.getStorageMode());
             con = Database.getDbConnection();
             final FxEnvironment env = CacheAdmin.getEnvironment();
             //security check start
             if (!(FxContext.get().getRunAsSystem() || FxContext.getUserTicket().isGlobalSupervisor())) {
                 FxContentVersionInfo cvi = storage.getContentVersionInfo(con, pk.getId());
                 FxPK currPK;
                 for (int currVer : cvi.getVersions()) {
                     currPK = new FxPK(pk.getId(), currVer);
                     FxContentSecurityInfo si = storage.getContentSecurityInfo(con, currPK, null);
                     FxPermissionUtils.checkPermission(FxContext.getUserTicket(), ACLPermission.DELETE, si, true);
                 }
             }
             //security check end
             FxScriptBinding binding = null;
             long[] scripts;
             FxContentSecurityInfo securityInfo = storage.getContentSecurityInfo(con, pk, null);
             try {
                 FxPermissionUtils.checkMandatorExistance(securityInfo.getMandatorId());
                 FxPermissionUtils.checkTypeAvailable(securityInfo.getTypeId(), false);
             } catch (FxNotFoundException e) {
                 throw new FxRemoveException(e);
             }
             long typeId = securityInfo.getTypeId();
             FxType type = env.getType(typeId);
 
             //scripting before start
             //type scripting
             scripts = type.getScriptMapping(FxScriptEvent.BeforeContentRemove);
             if (scripts != null)
                 for (long script : scripts) {
                     if (binding == null)
                         binding = new FxScriptBinding();
                     binding.setVariable("pk", pk);
                     if (!binding.getProperties().containsKey("securityInfo"))
                         binding.setVariable("securityInfo", securityInfo);
                     scripting.runScript(script, binding);
                 }
             //assignment scripting
             if (type.hasScriptedAssignments()) {
                 binding = new FxScriptBinding();
                 binding.setVariable("pk", pk);
                 for (FxAssignment as : type.getScriptedAssignments(FxScriptEvent.BeforeAssignmentDataDelete)) {
                     binding.setVariable("assignment", as);
                     for (long script : as.getScriptMapping(FxScriptEvent.BeforeAssignmentDataDelete)) {
                         scripting.runScript(script, binding);
                     }
                 }
             }
             //scripting before end
             storage.contentRemove(con, type, pk);
             //scripting after start
             //assignment scripting
             if (type.hasScriptedAssignments()) {
                 binding = new FxScriptBinding();
                 binding.setVariable("pk", pk);
                 for (FxAssignment as : type.getScriptedAssignments(FxScriptEvent.AfterAssignmentDataDelete)) {
                     binding.setVariable("assignment", as);
                     for (long script : as.getScriptMapping(FxScriptEvent.AfterAssignmentDataDelete)) {
                         scripting.runScript(script, binding);
                     }
                 }
             }
             //type scripting
             scripts = type.getScriptMapping(FxScriptEvent.AfterContentRemove);
             if (scripts != null)
                 for (long script : scripts) {
                     if (binding == null)
                         binding = new FxScriptBinding();
                     binding.setVariable("pk", pk);
                     if (!binding.getProperties().containsKey("securityInfo"))
                         binding.setVariable("securityInfo", securityInfo);
                     scripting.runScript(script, binding);
                 }
             //scripting after end
         } catch (FxRemoveException e) {
             EJBUtils.rollback(ctx);
             throw e;
         } catch (FxNotFoundException e) {
             //not found is ok for removed instances
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxRemoveException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, ps);
             if (!ctx.getRollbackOnly())
                 CacheAdmin.expireCachedContent(pk.getId());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeVersion(FxPK pk) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         try {
             ContentStorage storage = StorageManager.getContentStorage(pk.getStorageMode());
             con = Database.getDbConnection();
             //security check start
             FxContentSecurityInfo si = StorageManager.getContentStorage(pk.getStorageMode()).getContentSecurityInfo(con, pk, null);
             FxType type = CacheAdmin.getEnvironment().getType(si.getTypeId());
             FxPermissionUtils.checkMandatorExistance(si.getMandatorId());
             FxPermissionUtils.checkTypeAvailable(type.getId(), false);
             FxPermissionUtils.checkPermission(FxContext.getUserTicket(), ACLPermission.DELETE, si, true);
             //security check end
 
             storage.contentRemoveVersion(con, type, pk);
         } catch (FxNotFoundException e) {
             EJBUtils.rollback(ctx);
             throw e;
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxRemoveException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, ps);
             if (!ctx.getRollbackOnly())
                 CacheAdmin.expireCachedContent(pk.getId());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public int removeForType(long typeId) throws FxApplicationException {
         Connection con = null;
         try {
             FxType type = CacheAdmin.getEnvironment().getType(typeId);
             FxPermissionUtils.checkTypeAvailable(typeId, false);
             ContentStorage storage = StorageManager.getContentStorage(type.getStorageMode());
             con = Database.getDbConnection();
             long[] scriptsBefore = null;
             long[] scriptsAfter = null;
 
             FxScriptBinding binding = null;
             UserTicket ticket = FxContext.getUserTicket();
             if (CacheAdmin.getEnvironment().getType(typeId).getScriptMapping(FxScriptEvent.BeforeContentRemove) != null)
                 scriptsBefore = CacheAdmin.getEnvironment().getType(typeId).getScriptMapping(FxScriptEvent.BeforeContentRemove);
             if (CacheAdmin.getEnvironment().getType(typeId).getScriptMapping(FxScriptEvent.AfterContentRemove) != null)
                 scriptsAfter = CacheAdmin.getEnvironment().getType(typeId).getScriptMapping(FxScriptEvent.AfterContentRemove);
             List<FxPK> pks = null;
             if (scriptsBefore != null || scriptsAfter != null || !ticket.isGlobalSupervisor() || type.isTrackHistory()) {
                 binding = new FxScriptBinding();
                 pks = storage.getPKsForType(con, type, false);
                 if (!ticket.isGlobalSupervisor() || scriptsBefore != null) {
                     for (FxPK pk : pks) {
                         //security check start
                         FxContentSecurityInfo si = storage.getContentSecurityInfo(con, pk, null);
                         if (!ticket.isGlobalSupervisor())
                             FxPermissionUtils.checkPermission(FxContext.getUserTicket(), ACLPermission.DELETE, si, true);
                         //note: instances of deactivated mandators are silently ignored and erased on purposed!
                         //security check end
                         //scripting before start
                         //type scripting
                         if (scriptsBefore != null) {
                             binding.setVariable("pk", pk);
                             binding.setVariable("securityInfo", si);
                             for (long script : scriptsBefore)
                                 scripting.runScript(script, binding);
                         }
                         //assignment scripting
                         if (type.hasScriptedAssignments()) {
                             binding = new FxScriptBinding();
                             binding.setVariable("pk", pk);
                             for (FxAssignment as : type.getScriptedAssignments(FxScriptEvent.BeforeAssignmentDataDelete)) {
                                 binding.setVariable("assignment", as);
                                 for (long script : as.getScriptMapping(FxScriptEvent.BeforeAssignmentDataDelete)) {
                                     scripting.runScript(script, binding);
                                 }
                             }
                         }
                         //scripting before end
                     }
                 }
             }
             int removed = storage.contentRemoveForType(con, type);
             if (scriptsAfter != null) {
                 if (pks == null)
                     pks = storage.getPKsForType(con, type, false);
                 for (FxPK pk : pks) {
                     binding.getProperties().remove("securityInfo");
                     //scripting after start
                     //assignment scripting
                     if (type.hasScriptedAssignments()) {
                         binding = new FxScriptBinding();
                         binding.setVariable("pk", pk);
                         for (FxAssignment as : type.getScriptedAssignments(FxScriptEvent.AfterAssignmentDataDelete)) {
                             binding.setVariable("assignment", as);
                             for (long script : as.getScriptMapping(FxScriptEvent.AfterAssignmentDataDelete)) {
                                 scripting.runScript(script, binding);
                             }
                         }
                     }
                     //type scripting
                     if (scriptsBefore != null) {
                         binding.setVariable("pk", pk);
                         for (long script : scriptsAfter)
                             scripting.runScript(script, binding);
                     }
                     //scripting after end
                 }
             }
             if (type.isTrackHistory()) {
                 HistoryTrackerEngine trackerEngine = EJBLookup.getHistoryTrackerEngine();
                 for (FxPK pk : pks)
                     trackerEngine.track(type, pk, null, "history.content.removed");
             }
             return removed;
         } catch (FxNotFoundException e) {
             EJBUtils.rollback(ctx);
             throw new FxRemoveException(e);
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxRemoveException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
             if (!ctx.getRollbackOnly())
                 CacheAdmin.expireCachedContents();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxContentSecurityInfo getContentSecurityInfo(FxPK pk) throws FxApplicationException {
         FxSharedUtils.checkParameterNull(pk, "pk");
         Connection con = null;
         try {
             ContentStorage storage = StorageManager.getContentStorage(pk.getStorageMode());
             con = Database.getDbConnection();
             return storage.getContentSecurityInfo(con, pk, null);
         } catch (FxNotFoundException e) {
             throw new FxLoadException(e);
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxContentVersionInfo getContentVersionInfo(FxPK pk) throws FxApplicationException {
         FxSharedUtils.checkParameterNull(pk, "pk");
         Connection con = null;
         try {
             ContentStorage storage = StorageManager.getContentStorage(pk.getStorageMode());
             con = Database.getDbConnection();
             return storage.getContentVersionInfo(con, pk.getId());
         } catch (FxNotFoundException e) {
             throw new FxLoadException(e);
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public int getReferencedContentCount(FxPK pk) throws FxApplicationException {
         FxSharedUtils.checkParameterNull(pk, "pk");
         Connection con = null;
         try {
             ContentStorage storage = StorageManager.getContentStorage(pk.getStorageMode());
             con = Database.getDbConnection();
             return storage.getReferencedContentCount(con, pk.getId()) +
                     (int) FxFlatStorageManager.getInstance().getReferencedContentCount(con, pk.getId());
         } catch (FxNotFoundException e) {
             throw new FxLoadException(e);
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public List<FxPK> getPKsForType(long typeId, boolean onePkPerInstance) throws FxApplicationException {
         if (!FxContext.getUserTicket().isGlobalSupervisor()) {
             throw new FxNoAccessException("ex.content.type.getAll.noPermission");
         }
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return StorageManager.getContentStorage(TypeStorageMode.Hierarchical).getPKsForType(con,
                     CacheAdmin.getEnvironment().getType(typeId), onePkPerInstance);
         } catch (Exception e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public long getBinaryId(FxPK pk, String xpath, FxLanguage language, boolean fallbackToDefault) throws FxApplicationException {
         FxSharedUtils.checkParameterNull(pk, "pk");
         FxSharedUtils.checkParameterNull(xpath, "xpath");
         Connection con = null;
         try {
             ContentStorage storage = StorageManager.getContentStorage(pk.getStorageMode());
             con = Database.getDbConnection();
             FxContent co = load(pk);
             UserTicket ticket = FxContext.getUserTicket();
             if (xpath.equals("/")) {
                 if (!ticket.isGlobalSupervisor()) {
                     FxType type = CacheAdmin.getEnvironment().getType(co.getTypeId());
                     if (type.isUsePermissions()) {
                         FxContentSecurityInfo si = storage.getContentSecurityInfo(con, pk, null);
                         FxPermissionUtils.checkPermission(ticket, ACLPermission.READ, si, true);
                     }
                 }
                 return co.getBinaryPreviewId();
             }
             FxPropertyData pd = co.getPropertyData(xpath);
             if (!pd.getValue().isEmpty() && pd.getValue() instanceof FxBinary) {
                 final FxBinary bin = (FxBinary) pd.getValue();
                 if (language == null)
                     return bin.getBestTranslation(ticket.getLanguage()).getId();
                 if (fallbackToDefault)
                     return bin.getBestTranslation(language).getId();
                 if (!bin.translationExists(language.getId()))
                     throw new FxInvalidParameterException("language", "ex.content.value.notTranslated", language);
                 return bin.getTranslation(language).getId();
             }
             throw new FxInvalidParameterException("XPATH", "ex.content.binary.xpath.invalid", xpath);
         } catch (FxNotFoundException e) {
             throw new FxLoadException(e);
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public long getBinaryId(FxPK pk, String xpath, FxLanguage language) throws FxApplicationException {
         return getBinaryId(pk, xpath, language, false);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public String getBinaryMetaData(long id) {
         if (!FxContext.getUserTicket().isGlobalSupervisor())
             return "";
         Connection con = null;
         try {
             con = Database.getDbConnection();
             return StorageManager.getContentStorage(TypeStorageMode.Hierarchical).getBinaryMetaData(con, id);
         } catch (FxNotFoundException e) {
             LOG.error(e);
         } catch (SQLException e) {
             LOG.error(e);
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
         return "";
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxContent importContent(String xml, boolean newInstance) throws FxApplicationException {
         FxContent co;
         try {
             co = (FxContent) ConversionEngine.getXStream().fromXML(xml);
         } catch (ConversionException e) {
             String key;
             Iterator i = e.keys();
             String path = "unknown";
             String line = "unknown";
             while (i.hasNext()) {
                 key = (String) i.next();
                 if ("path".equals(key))
                     path = e.get(key);
                 else if ("line number".equals(key))
                     line = e.get(key);
 //                System.out.println("Key ["+key+"] -> "+e.get(key));
             }
             throw new FxApplicationException("ex.content.import.conversionError", path, line, e.getShortMessage());
         } catch (Exception e) {
             throw new FxApplicationException("ex.content.import.error", e.getMessage());
         }
         return co;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public String exportContent(FxContent content) throws FxApplicationException {
         return ConversionEngine.getXStream().toXML(content);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxLock lock(FxLockType lockType, FxPK pk) throws FxLockException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             FxLock lock = StorageManager.getLockStorage().lock(con, lockType, pk);
             FxCachedContent cachedContent = CacheAdmin.getCachedContent(pk);
             if (cachedContent != null) {
                 cachedContent.updateLock(lock);
                 CacheAdmin.cacheContent(cachedContent);
             }
             return lock;
         } catch (SQLException e) {
             throw new FxLockException(e, "ex.db.sqlError", e.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxLockException(e);
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxLock lock(FxLockType lockType, FxPK pk, long duration) throws FxLockException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             FxLock lock = StorageManager.getLockStorage().lock(con, lockType, pk, duration);
             FxCachedContent cachedContent = CacheAdmin.getCachedContent(pk);
             if (cachedContent != null) {
                 cachedContent.updateLock(lock);
                 CacheAdmin.cacheContent(cachedContent);
             }
             return lock;
         } catch (SQLException e) {
             throw new FxLockException(e, "ex.db.sqlError", e.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxLockException(e);
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxLock takeOverLock(FxLock lock) throws FxLockException {
         if (!lock.isContentLock())
             return lock;
         Connection con = null;
         try {
             con = Database.getDbConnection();
             FxLock newLock = StorageManager.getLockStorage().takeOver(con, lock);
             FxCachedContent cachedContent = CacheAdmin.getCachedContent(lock.getLockedPK());
             if (cachedContent != null) {
                 cachedContent.updateLock(newLock);
                 CacheAdmin.cacheContent(cachedContent);
             }
             return newLock;
         } catch (SQLException e) {
             throw new FxLockException(e, "ex.db.sqlError", e.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxLockException(e);
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxLock takeOverLock(FxPK pk) throws FxLockException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             final LockStorage lockStorage = StorageManager.getLockStorage();
             final FxLock lock = lockStorage.getLock(con, pk);
             FxLock newLock;
             if (lock.isLocked())
                 newLock = lockStorage.takeOver(con, lock);
             else
                 newLock = lockStorage.lock(con, FxLockType.Loose, pk);
             FxCachedContent cachedContent = CacheAdmin.getCachedContent(pk);
             if (cachedContent != null) {
                 cachedContent.updateLock(newLock);
                 CacheAdmin.cacheContent(cachedContent);
             }
             return newLock;
         } catch (SQLException e) {
             throw new FxLockException(e, "ex.db.sqlError", e.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxLockException(e);
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxLock extendLock(FxLock lock, long duration) throws FxLockException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             FxLock newLock = StorageManager.getLockStorage().extend(con, lock, duration);
             FxCachedContent cachedContent = CacheAdmin.getCachedContent(lock.getLockedPK());
             if (cachedContent != null) {
                 cachedContent.updateLock(newLock);
                 CacheAdmin.cacheContent(cachedContent);
             }
             return newLock;
         } catch (SQLException e) {
             throw new FxLockException(e, "ex.db.sqlError", e.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxLockException(e);
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public FxLock extendLock(FxPK pk, long duration) throws FxLockException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             final LockStorage lockStorage = StorageManager.getLockStorage();
             final FxLock lock = lockStorage.getLock(con, pk);
             FxLock newLock;
             if (lock.isLocked())
                 newLock = lockStorage.extend(con, lock, duration);
             else
                 newLock = lockStorage.lock(con, FxLockType.Loose, pk, duration);
             FxCachedContent cachedContent = CacheAdmin.getCachedContent(pk);
             if (cachedContent != null) {
                 cachedContent.updateLock(newLock);
                 CacheAdmin.cacheContent(cachedContent);
             }
             return newLock;
         } catch (SQLException e) {
             throw new FxLockException(e, "ex.db.sqlError", e.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxLockException(e);
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxLock getLock(FxPK pk) {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             FxCachedContent cachedContent = CacheAdmin.getCachedContent(pk);
             if (cachedContent != null)
                 return cachedContent.getContent().getLock();
             return StorageManager.getLockStorage().getLock(con, pk);
         } catch (SQLException e) {
             //noinspection ThrowableInstanceNeverThrown
             throw new FxLockException(e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         } catch (FxNotFoundException e) {
             throw e.asRuntimeException();
         } catch (FxLockException e) {
             throw e.asRuntimeException();
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void unlock(FxPK pk) throws FxLockException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             final LockStorage lockStorage = StorageManager.getLockStorage();
             final FxLock lock = lockStorage.getLock(con, pk);
             if (lock.isLocked())
                 lockStorage.unlock(con, pk);
             FxCachedContent cachedContent = CacheAdmin.getCachedContent(pk);
             if (cachedContent != null) {
                 cachedContent.updateLock(FxLock.noLockPK());
                 CacheAdmin.cacheContent(cachedContent);
             }
         } catch (SQLException e) {
             throw new FxLockException(e, "ex.db.sqlError", e.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxLockException(e);
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public List<FxLock> getLocks(FxLockType lockType, long userId, long typeId, String resource) throws FxLockException {
         Connection con = null;
         try {
             con = Database.getDbConnection();
             final LockStorage lockStorage = StorageManager.getLockStorage();
             return lockStorage.getLocks(con, lockType, userId, typeId, resource);
         } catch (SQLException e) {
             throw new FxLockException(e, "ex.db.sqlError", e.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxLockException(e);
         } finally {
             Database.closeObjects(ContentEngineBean.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void convertContentType(FxPK contentPK, long destinationTypeId, boolean allowLossy, boolean allVersions) throws FxApplicationException {
         final FxContent content = load(contentPK);
         final FxType sourceType = CacheAdmin.getEnvironment().getType(content.getTypeId());
         FxPermissionUtils.checkTypeAvailable(destinationTypeId, false);
         final FxType destinationType = CacheAdmin.getEnvironment().getType(destinationTypeId);
         final UserTicket ticket = FxContext.getUserTicket();
         Connection con = null;
         final long sourceTypeId = sourceType.getId();
         try {
             if (sourceTypeId == destinationTypeId)
                 throw new FxContentTypeConversionException("ex.content.typeconversion.desteqSourceError");
 
             // only a supervisor or a user whose ACL both belongs to the current content, to the current type and the destination type may
             // move the content
             boolean userPermitted = false;
             if (ticket.isGlobalSupervisor() || ticket.isMandatorSupervisor())
                 userPermitted = true;
             if (!userPermitted) {
                 if (!FxPermissionUtils.currentUserInACLList(ticket, Arrays.asList(sourceType.getACL().getId())))
                     throw new FxContentTypeConversionException("ex.content.typeconversion.sourceTypeACLError");
                 if (!FxPermissionUtils.currentUserInACLList(ticket, Arrays.asList(destinationType.getACL().getId())))
                     throw new FxContentTypeConversionException("ex.content.typeconversion.destinationTypeACLError");
                 if (!FxPermissionUtils.currentUserInACLList(ticket, content.getAclIds()))
                     throw new FxContentTypeConversionException("ex.content.typeconversion.notInContentACL");
             }
 
             // either destination ist derived from source or vice versa, or both types have to have the same supertype
             if(sourceType.getParent() == null && destinationType.getParent() == null)
                 throw new FxContentTypeConversionException("ex.content.typeconversion.derivedTypeError");
             if(!sourceType.isDerivedFrom(destinationTypeId) && !destinationType.isDerivedFrom(sourceTypeId)) {
                 // check for an indirect parentage (same supertype)
                 FxType o1 = null;
                 FxType o2 = null;
                 for(FxType t = sourceType; t != null; t = t.getParent())
                     o1 = t;
                 for(FxType t = destinationType; t != null; t = t.getParent())
                     o2 = t;
 
                 if(o1.getId() != o2.getId())
                     throw new FxContentTypeConversionException("ex.content.typeconversion.derivedTypeError");
             }
 
             /**
              * Inner class to convert a List of FxAssignments to a Map of ids / XPaths w/o leading types
              * The method omits systeminternal fields
              */
             final class XPathConverter {
                 Map<Long, String> convertNoTypesAsMap(List<FxAssignment> ass, boolean propAssignsOnly) {
                     final Map<Long, String> out = new HashMap<Long, String>(ass.size());
                     for (FxAssignment a : ass) {
                         if (!a.isSystemInternal()) {
                             if (propAssignsOnly && a instanceof FxPropertyAssignment)
                                 out.put(a.getId(), XPathElement.stripType(a.getXPath()));
                             else if(!propAssignsOnly)
                                 out.put(a.getId(), XPathElement.stripType(a.getXPath()));
                         }
                     }
                     return out;
                 }
             }
 
             final List<FxAssignment> sourceTypeAssignments = sourceType.getAllAssignments();
             final List<FxAssignment> destinationTypeAssignments = destinationType.getAllAssignments();
             final XPathConverter xPathConverter = new XPathConverter();
             // determine outcome for lossy / lossless conversion - evaluation is based on XPaths (their names, resp)
             final Map<Long, String> sourcePathsMap = xPathConverter.convertNoTypesAsMap(sourceTypeAssignments, true);
             final Map<Long, String> destPathsMap = xPathConverter.convertNoTypesAsMap(destinationTypeAssignments, true);
             final List<String> sourcePaths = new ArrayList<String>(sourcePathsMap.values());
             Collections.sort(sourcePaths);
             final List<String> destPaths = new ArrayList<String>(destPathsMap.values());
             Collections.sort(destPaths);
 
             final FxDiff diff = new FxDiff(sourcePaths, destPaths);
             final List<FxDiff.Difference> diffList = diff.diff();
             // a map which holds assignment ids / xpaths which will be removed from the source content (lossy conversion)
             final Map<Long, String> sourceRemoveMap = new HashMap<Long, String>(2);
             if (diffList.size() > 0) { // check if the differences have an impact on !lossy conversion
                 for (FxDiff.Difference d : diffList) {
                     if (d.getDeletedStart() == d.getDeletedEnd() && d.getAddedStart() == d.getAddedEnd()) {
                         throw new FxContentTypeConversionException("ex.content.typeconversion.xpathsDiffError");
                     } else if (d.getDeletedStart() == d.getDeletedEnd() && d.getAddedStart() != d.getAddedEnd()) {
                         // lossy conversion check
                         if(!allowLossy) {
                             throw new FxContentTypeConversionException("ex.content.typeconversion.sourceLossError");
                         } else { // add to list of xPaths / assignments t.b. removed
                             // sourcePathsMap.
                             final String removePath = sourcePaths.get(d.getDeletedStart());
                            for (Map.Entry<Long, String> pathEntry : sourcePathsMap.entrySet()) {
                                if(pathEntry.getValue().equals(removePath))
                                    sourceRemoveMap.put(pathEntry.getKey(), removePath);
                             }
                         }
                     } // else: del start != del end (del end is neg.) and add start & end the same, then dest has an add. prop
                 }
             }
 
             // a map for all sourceTypePropAssignmentIds vs. destinationTypePropAssignmentIds,
             // where null values mean that the destination assignments do not exist (data will be lost)
             final Map<Long, Long> assignmentMap = new HashMap<Long, Long>(5);
             // a List containing all destination assignments which are in the flatstore
             final List<Long> flatStoreAssignments = new ArrayList<Long>(5);
             final List<Long> nonFlatSourceAssignments = new ArrayList<Long>(5);
             final List<Long> nonFlatDestinationAssignments = new ArrayList<Long>(5);
 
             for(FxAssignment a : sourceTypeAssignments) {
                 if(a.isSystemInternal() || a instanceof FxGroupAssignment)
                     continue;
 
                 final String sourceXPath = XPathElement.stripType(a.getXPath());
                 // get the corresponding assignment from the destination type
                 final String destinationXPath =  destinationType.getName() + sourceXPath;
                 if(destPaths.contains(sourceXPath) && CacheAdmin.getEnvironment().assignmentExists(destinationXPath)) {
                     // if the dest assignment exists, test that the datatype is the same as for the source
                     final FxAssignment destAssignment = CacheAdmin.getEnvironment().getAssignment(destinationXPath);
                     if(a instanceof FxPropertyAssignment) {
                         if(!(destAssignment instanceof FxPropertyAssignment))
                             throw new FxContentTypeConversionException("ex.content.typeconversion.destneqprop", destAssignment.getId());
 
                         final FxDataType sourceDT = ((FxPropertyAssignment)a).getProperty().getDataType();
                         final FxDataType destDT = ((FxPropertyAssignment)destAssignment).getProperty().getDataType();
                         if(sourceDT != destDT)
                             throw new FxContentTypeConversionException("ex.content.typeconversion.destDTneqsourceDT", destinationXPath);
                     }
                     if(((FxPropertyAssignment)destAssignment).isFlatStorageEntry()) {
                         flatStoreAssignments.add(destAssignment.getId());
                     } else {
                         nonFlatDestinationAssignments.add(destAssignment.getId());
                     }
                     if(!((FxPropertyAssignment)a).isFlatStorageEntry()) {
                         nonFlatSourceAssignments.add(a.getId());
                     }
                     assignmentMap.put(a.getId(), destAssignment.getId());
                 } else {
                     assignmentMap.put(a.getId(), null);
                 }
             }
 
             // check if we have any mandatory assignments in our destination type and if we've got data for it
             for(FxAssignment a : destinationTypeAssignments) {
                 if(a instanceof FxPropertyAssignment && !a.isSystemInternal()) {
                     final int minMult = a.getMultiplicity().getMin();
                     if(minMult > 0) {
                         final String destXPath = XPathElement.stripType(a.getXPath());
                         if(content.getData(destXPath).size() < minMult)
                             throw new FxContentTypeConversionException("ex.content.typeconversion.destMultiplicityError", destXPath, a.getId(), minMult);
                     }
                 }
             }
 
             // passed all tests, start moving the content after retrieving all versions
             con = Database.getDbConnection();
             ContentStorage storage = StorageManager.getContentStorage(contentPK.getStorageMode());
             FxEnvironment env = CacheAdmin.getEnvironment();
             storage.convertContentType(con, contentPK, sourceTypeId, destinationTypeId, allVersions, assignmentMap, flatStoreAssignments,
                     nonFlatSourceAssignments, nonFlatDestinationAssignments, sourcePathsMap, destPathsMap, sourceRemoveMap, env);
 
         } catch (FxApplicationException e) {
             EJBUtils.rollback(ctx);
             throw new FxContentTypeConversionException("ex.content.typeconversion.error", e.getMessage(), contentPK, sourceTypeId, destinationType.getId());
         } catch(SQLException e) {
             EJBUtils.rollback(ctx);
             LOG.error(e);
         } finally { // flush cache after update was completed
             Database.closeObjects(ContentEngineBean.class, con, null);
             CacheAdmin.expireCachedContent(contentPK.getId());
         }
     }
 }
