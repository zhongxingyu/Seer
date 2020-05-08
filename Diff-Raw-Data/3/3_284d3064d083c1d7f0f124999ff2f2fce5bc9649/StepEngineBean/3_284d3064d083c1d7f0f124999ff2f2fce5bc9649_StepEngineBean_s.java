 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
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
 package com.flexive.ejb.beans.workflow;
 
 import com.flexive.core.Database;
 import static com.flexive.core.DatabaseConst.*;
 import com.flexive.core.structure.StructureLoader;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.SequencerEngine;
 import com.flexive.shared.interfaces.SequencerEngineLocal;
 import com.flexive.shared.interfaces.StepEngine;
 import com.flexive.shared.interfaces.StepEngineLocal;
 import com.flexive.shared.security.ACL.Category;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.security.UserGroup;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import com.flexive.shared.workflow.StepPermission;
 import com.flexive.shared.workflow.StepPermissionEdit;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 
 /**
  * The StepImpl class provides functions  to create, alter and query steps
  * within a workflow.
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Stateless(name = "StepEngine")
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class StepEngineBean implements StepEngine, StepEngineLocal {
 
     private static final transient Log LOG = LogFactory.getLog(StepEngineBean.class);
 
     @EJB
     private SequencerEngineLocal seq;
     @Resource
     private SessionContext ctx;
 
     /**
      * Check relate permissions for steps?
      */
     public static final boolean USE_RELATE_PERM = false;
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long createStep(Step step)
             throws FxApplicationException {
         UserTicket ticket = FxContext.get().getTicket();
         // Security checks
         FxPermissionUtils.checkRole(ticket, Role.WorkflowManagement);
 
         // Create the new step
         Statement stmt = null;
         String sql;
         Connection con = null;
         boolean success = false;
         try {
 
             // Obtain a database connection
             con = Database.getDbConnection();
 
             // Create any needed unique target step
             try {
                 StepDefinition def;
                 def = CacheAdmin.getEnvironment().getStepDefinition(step.getStepDefinitionId());
                 if (def.getUniqueTargetId() != -1) {
                     createStep(new Step(-1, def.getUniqueTargetId(), step.getWorkflowId(), step.getAclId()));
                 }
             } catch (FxLoadException exc) {
                 throw new FxCreateException(LOG, "ex.step.create.uniqueTargets", exc, exc.getMessage());
             } catch (FxNotFoundException exc) {
                 throw new FxCreateException("ex.stepdefinition.load.notFound", step.getStepDefinitionId());
             }
 
             // Create the step
             long newStepId = seq.getId(SequencerEngine.System.STEP);
             try {
                 stmt = con.createStatement();
                 sql = "INSERT INTO " + TBL_STEP + " (ID, STEPDEF, WORKFLOW, ACL) VALUES (" + newStepId + ","
                         + step.getStepDefinitionId() + "," + step.getWorkflowId() + "," + step.getAclId() + ")";
                 stmt.executeUpdate(sql);
             } catch (SQLException exc) {
                 // Ignore unique constraint.
                 if (!Database.isUniqueConstraintViolation(exc)) {
                     throw exc;
                 }
                 // get existing workflow, return its ID
                 sql = "SELECT ID FROM " + TBL_STEP + " WHERE STEPDEF="
                         + step.getStepDefinitionId() + " AND WORKFLOW=" + step.getWorkflowId();
                 ResultSet rs = stmt.executeQuery(sql);
                 if (rs != null && rs.next()) {
                     // return existing step ID as "new step ID"
                     newStepId = rs.getLong(1);
                 } else {
                     throw new FxCreateException(LOG, "ex.step.exists.load", exc, exc.getMessage());
                 }
             }
 
             // Refresh all UserTicket that are affected.
             // Do NOT use refreshHavingWorkflow since this function adds a workflow to tickets, so this function would
             // have no effect.
             // TODO
             //UserTicketImpl.refreshHavingACL(ACLId);
             success = true;
 
             // Return the new id
             return newStepId;
 
         } catch (SQLException exc) {
             throw new FxCreateException(LOG, "ex.step.create", exc, exc.getMessage());
         } finally {
             if (!success) {
                 ctx.setRollbackOnly();
             } else {
                 StructureLoader.reloadWorkflows(FxContext.get().getDivisionId());
             }
             Database.closeObjects(StepEngineBean.class, con, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<StepPermission> loadAllStepsForUser(long userId) throws FxApplicationException {
         UserTicket ticket = FxContext.get().getTicket();
         // Select all step ids
         final String sql =
                 //                 1    ,   2         ,      3
                 "SELECT DISTINCT step.ID,aclug.ACL,step.WORKFLOW,"
                         // 4        ,  5       ,   6     ,  7         ,   8      ,      9        , 10
                         + " aclug.PEDIT,aclug.PREAD,aclug.PREMOVE,aclug.PEXPORT,aclug.PREL,aclug.PCREATE,step.STEPDEF"
                         + " FROM " + TBL_ACLS + " acl," + TBL_ASSIGN_ACLS + " aclug," + TBL_STEP + " step"
                         + " WHERE"
                         + " aclug.ACL=acl.ID"
                         + " AND acl.CAT_TYPE=" + Category.WORKFLOW.getId()
                         + " AND aclug.USERGROUP IN (SELECT DISTINCT USERGROUP FROM " + TBL_ASSIGN_GROUPS
                         + " WHERE ACCOUNT=" + userId + " AND USERGROUP<>" + UserGroup.GROUP_OWNER + ")"
                         + " AND step.ACL=acl.ID";
 
         // Security
         if (!ticket.isGlobalSupervisor()) {
             if (ticket.getUserId() != userId) {
                 FxNoAccessException na = new FxNoAccessException("You may not load the Steps for a other user");
                 if (LOG.isInfoEnabled()) LOG.info(na);
                 throw na;
             }
         }
 
         // Obtain a database connection
         Connection con = null;
         Statement stmt = null;
         try {
             con = Database.getDbConnection();
 
             // Load all steps in the database
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             //ArrayList result = new ArrayList(50);
             Hashtable<Integer, StepPermission> result = new Hashtable<Integer, StepPermission>(50);
 
             while (rs != null && rs.next()) {
                 // Fill in a step object
                 Integer stepId = rs.getInt(1);
                 int workflowId = rs.getInt(3);
                 boolean mayEdit = rs.getBoolean(4);
                 boolean mayRead = rs.getBoolean(5);
                 boolean mayDelete = rs.getBoolean(6);
                 boolean mayExport = rs.getBoolean(7);
                 boolean mayRelate = rs.getBoolean(8);
                 boolean mayCreate = rs.getBoolean(9);
                 int stepDefinitionId = rs.getInt(10);
                 StepPermissionEdit data;
                 StepPermission stepPerm = result.get(stepId);
                 if (stepPerm == null) {
                     data = new StepPermissionEdit(new StepPermission(stepId, stepDefinitionId, workflowId,
                             mayRead, mayEdit, mayRelate, mayDelete, mayExport, mayCreate));
                 } else {
                     data = new StepPermissionEdit(stepPerm);
                     if (mayDelete) data.setMayDelete(true);
                     if (mayEdit) data.setMayEdit(true);
                     if (mayExport) data.setMayExport(true);
                     if (mayRelate) data.setMayRelate(true);
                     if (mayRead) data.setMayRead(true);
                     if (mayCreate) data.setMayCreate(true);
                 }
                 result.put(stepId, data);
             }
 
             return new ArrayList<StepPermission>(result.values());
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, "ex.step.load.user", exc, userId, exc.getMessage());
         } finally {
             Database.closeObjects(StepEngineBean.class, con, stmt);
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeSteps(long workflowId) throws FxApplicationException {
         deleteStep(workflowId, true);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeStep(long stepId) throws FxApplicationException {
         deleteStep(stepId, false);
     }
 
 
     /**
      * Deletes all steps within a workflow, or a specific step itself.
      * <p/>
      * The caller has to be within ROLE_WORKFLOWMANAGEMENT.<br>
      * <br>
      *
      * @param objectId   the workflow id if parameter isWorkflow equals true, or a specific step id
      * @param isWorkflow if true the objectId parameter refers to a workflow, if false the objectId refers to
      *                   a specific step id
      * @throws FxApplicationException TODO
      */
     private void deleteStep(long objectId, boolean isWorkflow)
             throws FxApplicationException {
         UserTicket ticket = FxContext.get().getTicket();
         // Security checks
         FxPermissionUtils.checkRole(ticket, Role.WorkflowManagement);
         final FxEnvironment env = CacheAdmin.getEnvironment();
 
         long workflowId = -1;
         // Check if the workflow exists at all, throws FxNotFoundException
         if (isWorkflow)
             workflowId = env.getWorkflow(objectId).getId();
 
         // Create the new step
         Connection con = null;
         Statement stmt = null;
         String sql;
         boolean success = false;
         try {
 
             // Obtain a database connection
             con = Database.getDbConnection();
 
             // May only delete a single step if its not the unique target of a other step
             if (!isWorkflow) {
                 Step step = env.getStep(objectId);
                 stmt = con.createStatement();
                 sql = "SELECT COUNT(*) FROM " + TBL_STEP + " WHERE WORKFLOW=" + step.getWorkflowId()
                         + " AND STEPDEF IN (SELECT def.ID FROM " + TBL_STEPDEFINITION + " def, " + TBL_STEP
                         + " stp WHERE stp.ID=" + step.getId() + " AND stp.STEPDEF=def.UNIQUE_TARGET)";
                 ResultSet rs = stmt.executeQuery(sql);
                 int count = 0;
                 if (rs != null && rs.next()) count = rs.getInt(1);
                 if (count != 0) {
                     throw new FxEntryInUseException("ex.step.delete.uniqueTarget", objectId);
                 }
                 stmt.close();
             }
 
             // Delete all routes that use the step(s)
             if (isWorkflow) {
                 sql = "DELETE FROM " + TBL_ROUTES + " WHERE FROM_STEP in (select id from "
                         + TBL_STEP + " WHERE WORKFLOW=" + workflowId + ")";
             } else {
                 sql = "DELETE FROM " + TBL_ROUTES + " WHERE FROM_STEP=" + objectId + " OR TO_STEP=" + objectId;
             }
             stmt = con.createStatement();
             stmt.executeUpdate(sql);
             stmt.close();
 
             // Delete the step(s) itself
             stmt = con.createStatement();
             sql = "DELETE FROM " + TBL_STEP + " WHERE " + (isWorkflow ? "WORKFLOW=" : "ID=") + objectId;
             stmt.executeUpdate(sql);
             success = true;
         } catch (SQLException exc) {
             if (isWorkflow) {
                 if (Database.isForeignKeyViolation(exc))
                     throw new FxRemoveException("ex.step.delete.workflow.inUse", env.getWorkflow(objectId).getName());
                 throw new FxRemoveException(LOG, "ex.step.delete.workflow", exc, exc.getMessage());
             } else {
                 if (Database.isForeignKeyViolation(exc)) {
                     String stepName;
                     try {
                         Step step = env.getStep(objectId);
                         stepName = env.getStepDefinition(step.getStepDefinitionId()).getName();
                     } catch (Exception e) {
                         stepName = "unknown";
                     }
                     throw new FxRemoveException("ex.step.delete.inUse", stepName);
                 }
                 throw new FxRemoveException(LOG, "ex.step.delete", exc, exc.getMessage());
             }
         } finally {
             if (!success) {
                 ctx.setRollbackOnly();
             } else {
                StructureLoader.reloadWorkflows(FxContext.get().getDivisionId());
             }
             Database.closeObjects(StepEngineBean.class, con, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void updateStep(long stepId, long aclId)
             throws FxApplicationException {
         UserTicket ticket = FxContext.get().getTicket();
         // Security checks
         FxPermissionUtils.checkRole(ticket, Role.WorkflowManagement);
 
         // Load the step
         try {
             CacheAdmin.getEnvironment().getStep(stepId);
         } catch (Exception exc) {
             throw new FxUpdateException(exc);
         }
 
         // Do work ..
         Statement stmt = null;
         String sql;
         Connection con = null;
         boolean success = false;
         try {
 
             // Obtain a database connection
             con = Database.getDbConnection();
 
             // Update the step
             stmt = con.createStatement();
             sql = "UPDATE " + TBL_STEP + " SET ACL=" + aclId + " WHERE ID=" + stepId;
             int ucount = stmt.executeUpdate(sql);
 
             // Is the step defined at all?
             if (ucount == 0) {
                 throw new FxNotFoundException("ex.step.notFound.id", stepId);
             }
 
             // Update the active UserTickets
             // Refresh all tickets having the new acl (workflow access might be added) and refreshHavingUser all that
             // have the affected workflow (workflow access may be removed)
             // TODO
             //UserTicketImpl.refreshHavingACL(aclId);
             //UserTicketImpl.refreshHavingWorkflow(stp.getWorkflowId());
             success = true;
 
         } catch (SQLException exc) {
             throw new FxUpdateException(LOG, "ex.step.update", exc, exc.getMessage());
         } finally {
             if (!success) {
                 ctx.setRollbackOnly();
             } else {
                 StructureLoader.reloadWorkflows(FxContext.get().getDivisionId());
             }
             Database.closeObjects(StepEngineBean.class, con, stmt);
         }
     }
 }
