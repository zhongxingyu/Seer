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
 package com.flexive.shared.structure;
 
 import com.flexive.shared.FxContext;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.scripting.FxScriptInfo;
 import com.flexive.shared.scripting.FxScriptMapping;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.workflow.Route;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import com.flexive.shared.workflow.Workflow;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * <p>
  * An environment wrapper that returns only data visible for the calling user.
  * </p>
  * <p>
  * The default FxEnvironment implementation represents the global flexive environment and does
  * not filter contents based on the current user. This class performs additional checks and should
  * be used for retrieving environment data that is displayed to the user.
  * </p>
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public final class FxFilteredEnvironment implements FxEnvironment {
     private static final long serialVersionUID = 4401591013752540846L;
 
     private final FxEnvironment environment;
 
    private UserTicket getTicket() {
        return FxContext.get().getTicket();
    }

     public FxFilteredEnvironment(FxEnvironment environment) {
         this.environment = environment;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<StepDefinition> getStepDefinitions() {
         return environment.getStepDefinitions();
     }
 
     /**
      * {@inheritDoc}
      */
     public StepDefinition getStepDefinition(long id) {
         return environment.getStepDefinition(id);
     }
 
     /**
      * {@inheritDoc}
      */
     public Step getStepByDefinition(long workflowId, long stepDefinitionId) {
         return environment.getStepByDefinition(workflowId, stepDefinitionId);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Step> getStepsByDefinition(long stepDefinitionId) {
         return environment.getStepsByDefinition(stepDefinitionId);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Step> getStepsByWorkflow(long workflowId) {
         return environment.getStepsByWorkflow(workflowId);
     }
 
     /**
      * {@inheritDoc}
      */
     public Step getStep(long stepId) {
         return environment.getStep(stepId);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Step> getSteps() {
         return environment.getSteps();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxDataType> getDataTypes() {
         return environment.getDataTypes();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxDataType getDataType(long id) {
         return environment.getDataType(id);
     }
 
     /**
      * {@inheritDoc}
      */
     public ACL getACL(long id) {
         return environment.getACL(id);
     }
 
     /**
      * {@inheritDoc}
      */
     public ACL getACL(String name) {
         return environment.getACL(name);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<ACL> getACLs() {
         return environment.getACLs();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<ACL> getACLs(ACL.Category category) {
         return environment.getACLs(category);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<ACL> getACLs(long mandatorId) {
         return getACLs(mandatorId, null, true);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<ACL> getACLs(long mandatorId, boolean includeForeignAccessible) {
         return getACLs(mandatorId, null, includeForeignAccessible);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<ACL> getACLs(long mandatorId, ACL.Category category, boolean includeForeignAccessible) {
         final UserTicket ticket = FxContext.get().getTicket();
         if (!ticket.isGlobalSupervisor() && mandatorId != ticket.getMandatorId()) {
             throw new FxNoAccessException("ex.acl.loadAllFailed.foreignMandator").asRuntimeException();
         }
         return environment.getACLs(mandatorId, category, includeForeignAccessible);
     }
 
     /**
      * {@inheritDoc}
      */
     public ACL getDefaultACL(ACL.Category category) {
         return environment.getDefaultACL(category);
     }
 
     /**
      * {@inheritDoc}
      */
     public Workflow getWorkflow(long id) {
         return environment.getWorkflow(id);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Workflow> getWorkflows() {
         return environment.getWorkflows();
     }
 
     /**
      * {@inheritDoc}
      */
     public Mandator getMandator(long id) {
         return environment.getMandator(id);
     }
 
     /**
      * {@inheritDoc}
      */
     public Mandator getMandator(String name) {
         return environment.getMandator(name);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Mandator> getMandators(boolean active, boolean inactive) {
         return environment.getMandators(active, inactive);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxGroup> getGroups(boolean returnReferenced, boolean returnUnreferenced, boolean returnRootGroups, boolean returnSubGroups) {
         return environment.getGroups(returnReferenced, returnUnreferenced, returnRootGroups, returnSubGroups);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxGroup getGroup(long id) {
         return environment.getGroup(id);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxGroup getGroup(String name) {
         return environment.getGroup(name);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxProperty> getProperties(boolean returnReferenced, boolean returnUnreferenced) {
         return environment.getProperties(returnReferenced, returnUnreferenced);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxProperty getProperty(long id) {
         return environment.getProperty(id);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxProperty getProperty(String name) {
         return environment.getProperty(name);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxPropertyAssignment> getPropertyAssignments() {
         final List<FxType> types = getTypes(true, true, true, false);
         final List<FxPropertyAssignment> assignments = new ArrayList<FxPropertyAssignment>(environment.getPropertyAssignments());
         // return only assignments for available types
        for (Iterator<FxPropertyAssignment> iterator = assignments.iterator(); iterator.hasNext(); ) {
             final FxPropertyAssignment assignment = iterator.next();
             if (!types.contains(assignment.getAssignedType())) {
                 iterator.remove();
             }
         }
         return Collections.unmodifiableList(assignments);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxPropertyAssignment> getSystemInternalRootPropertyAssignments() {
         return environment.getSystemInternalRootPropertyAssignments();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxPropertyAssignment> getPropertyAssignments(boolean includeDisabled) {
         return environment.getPropertyAssignments(includeDisabled);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxGroupAssignment> getGroupAssignments() {
         return environment.getGroupAssignments();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxGroupAssignment> getGroupAssignments(boolean includeDisabled) {
         return environment.getGroupAssignments(includeDisabled);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxType> getTypes(boolean returnBaseTypes, boolean returnDerivedTypes, boolean returnTypes, boolean returnRelations) {
         return environment.getTypes(returnBaseTypes, returnDerivedTypes, returnTypes, returnRelations);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxType> getReferencingRelationTypes(long typeId) {
         return environment.getReferencingRelationTypes(typeId);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxAssignment getAssignment(String xPath) {
         return environment.getAssignment(xPath);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxAssignment getAssignment(long assignmentId) {
         return environment.getAssignment(assignmentId);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxAssignment> getDerivedAssignments(long assignmentId) {
         return environment.getDerivedAssignments(assignmentId);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxType getType(String name) {
         return environment.getType(name);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxType getType(long id) {
         return environment.getType(id);
     }
 
     /**
      * {@inheritDoc}
      */
     public Route getRoute(long routeId) {
         return environment.getRoute(routeId);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxScriptInfo> getScripts() {
         return environment.getScripts();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxScriptInfo getScript(long scriptId) {
         return environment.getScript(scriptId);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxScriptMapping> getScriptMappings() {
         return environment.getScriptMappings();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxScriptMapping getScriptMapping(long scriptId) {
         return environment.getScriptMapping(scriptId);
     }
 
     /**
      * {@inheritDoc}
      */
     public long getTimeStamp() {
         return environment.getTimeStamp();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxSelectList> getSelectLists() {
         return environment.getSelectLists();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxSelectList getSelectList(long id) {
         return environment.getSelectList(id);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxSelectList getSelectList(String name) {
         return environment.getSelectList(name);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxSelectListItem getSelectListItem(long id) {
         return environment.getSelectListItem(id);
     }
 
 }
