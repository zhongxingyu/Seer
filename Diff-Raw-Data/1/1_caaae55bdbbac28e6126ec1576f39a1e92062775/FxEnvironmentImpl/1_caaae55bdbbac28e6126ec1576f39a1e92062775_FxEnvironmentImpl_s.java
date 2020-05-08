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
 package com.flexive.core.structure;
 
 import com.flexive.shared.*;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.media.FxMimeTypeWrapper;
 import com.flexive.shared.media.impl.FxMimeType;
 import com.flexive.shared.scripting.FxScriptInfo;
 import com.flexive.shared.scripting.FxScriptMapping;
 import com.flexive.shared.scripting.FxScriptMappingEntry;
 import com.flexive.shared.scripting.FxScriptSchedule;
 import com.flexive.shared.security.*;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.workflow.Route;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import com.flexive.shared.workflow.Workflow;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.*;
 
 import static com.google.common.collect.ImmutableList.Builder;
 
 /**
  * Runtime object for environment metadata held in the cache.
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @SuppressWarnings({"ThrowableInstanceNeverThrown"})
 public final class FxEnvironmentImpl implements FxEnvironment {
     private static final long serialVersionUID = 7107237825721203341L;
 
     private ImmutableList<FxDataType> dataTypes;
     private ImmutableList<ACL> acls;
     private ImmutableList<Workflow> workflows;
     private ImmutableList<FxSelectList> selectLists;
     private ImmutableList<FxGroup> groups;
     private ImmutableList<FxProperty> properties;
     private ImmutableList<FxType> types;
     private ImmutableList<Mandator> mandators;
     private String inactiveMandators = null;
     private String deactivatedTypes = null;
     private ImmutableList<FxAssignment> assignments;
     private ImmutableList<StepDefinition> stepDefinitions;
     private ImmutableList<Step> steps;
     private ImmutableList<FxScriptInfo> scripts;
     private ImmutableList<FxScriptMapping> scriptMappings;
     private ImmutableList<FxScriptSchedule> scriptSchedules;
     private ImmutableList<FxLanguage> languages;
     private ImmutableList<UserGroup> userGroups;
     private FxLanguage systemInternalLanguage;
     private long timeStamp = 0;
     //storage-type-level-mapping
     private Map<String, Map<Long, Map<Integer, List<FxFlatStorageMapping>>>> flatMappings;
 
     // cached lookup tables and lists
     private transient ImmutableList<FxPropertyAssignment> propertyAssignmentsEnabled;
     private transient ImmutableList<FxGroupAssignment> groupAssignmentsEnabled;
     private transient ImmutableMap<Long, FxAssignment> assignmentLookup;
     private transient ImmutableMap<String, FxAssignment> assignmentXPathLookup;
     private transient ImmutableMap<Long, FxProperty> propertyLookup;
     private transient ImmutableMap<String, FxProperty> propertyNameLookup;
     private transient ImmutableList<FxPropertyAssignment> propertyAssignmentsAll;
     private transient ImmutableList<FxPropertyAssignment> propertyAssignmentsSystemInternalRoot;
     private transient ImmutableList<FxGroupAssignment> groupAssignmentsAll;
 
     public FxEnvironmentImpl() {
     }
 
     /**
      * Copy constructor
      *
      * @param e source
      */
     private FxEnvironmentImpl(FxEnvironmentImpl e) {
         this.dataTypes = e.dataTypes;
         this.acls = e.acls;
         this.workflows = e.workflows;
         this.groups = e.groups;
         this.properties = e.properties;
         this.types = e.types;
         this.mandators = e.mandators;
         this.assignments = e.assignments;
         this.stepDefinitions = e.stepDefinitions;
         this.steps = e.steps;
         if (e.scripts != null) {
             this.scripts = e.scripts;
             this.scriptMappings = e.scriptMappings;
             this.scriptSchedules = e.scriptSchedules;
         }
         this.selectLists = e.selectLists;
         this.languages = e.languages;
         this.timeStamp = e.timeStamp;
         this.userGroups = e.userGroups;
 
         // immutable lookup tables can also be shared
         this.propertyAssignmentsEnabled = e.propertyAssignmentsEnabled;
         this.groupAssignmentsEnabled = e.groupAssignmentsEnabled;
         this.assignmentLookup = e.assignmentLookup;
         this.assignmentXPathLookup = e.assignmentXPathLookup;
         this.propertyLookup = e.propertyLookup;
         this.propertyNameLookup = e.propertyNameLookup;
         this.propertyAssignmentsAll = e.propertyAssignmentsAll;
         this.propertyAssignmentsSystemInternalRoot = e.propertyAssignmentsSystemInternalRoot;
         this.groupAssignmentsAll = e.groupAssignmentsAll;
 
         this.resolveFlatMappings();
     }
    }
 
     private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
         ois.defaultReadObject();
 
         initLookupTables();
     }
 
     /**
      * Assignment of all known FxDataType
      *
      * @param dataTypes all known data types
      */
     protected void setDataTypes(List<FxDataType> dataTypes) {
         this.dataTypes = ImmutableList.copyOf(dataTypes);
     }
 
     /**
      * Assign all defined ACL's
      *
      * @param acls all defined ALC's
      */
     protected void setAcls(List<ACL> acls) {
         this.acls = ImmutableList.copyOf(acls);
     }
 
     /**
      * Assign all step definitions
      *
      * @param stepDefinitions all step definitions
      */
     protected void setStepDefinitions(List<StepDefinition> stepDefinitions) {
         this.stepDefinitions = ImmutableList.copyOf(stepDefinitions);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<StepDefinition> getStepDefinitions() {
         return stepDefinitions;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public StepDefinition getStepDefinition(long id) {
         for (StepDefinition sdef : stepDefinitions)
             if (sdef.getId() == id)
                 return sdef;
         throw new FxNotFoundException("ex.stepdefinition.load.notFound", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public StepDefinition getStepDefinition(String name) {
         for (StepDefinition sdef : stepDefinitions)
             if (sdef.getName().toUpperCase().equals(name.toUpperCase()))
                 return sdef;
         throw new FxNotFoundException("ex.stepdefinition.load.notFound", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Step getStepByDefinition(long workflowId, long stepDefinitionId) {
         // Find the step
         for (Step step : steps) {
             if (step.getWorkflowId() != workflowId) continue;
             if (step.getStepDefinitionId() != stepDefinitionId) continue;
             return step;
         }
 
         // Step does not exist
         throw new FxNotFoundException("ex.stepdefinition.notFound.id.workflow", stepDefinitionId, workflowId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Step> getStepsByDefinition(long stepDefinitionId) {
         // Find the step
         ArrayList<Step> list = new ArrayList<Step>();
         for (Step step : steps)
             if (step.getStepDefinitionId() == stepDefinitionId)
                 list.add(step);
         return list;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Step> getStepsByWorkflow(long workflowId) {
         ArrayList<Step> list = new ArrayList<Step>();
         for (Step step : steps)
             if (step.getWorkflowId() == workflowId)
                 list.add(step);
         return list;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Step getStep(long stepId) {
         for (Step step : steps)
             if (step.getId() == stepId)
                 return step;
         throw new FxNotFoundException("ex.step.notFound.id", stepId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Step getStep(long workflowId, String stepName) {
         for (Step step : steps)
             if (step.getWorkflowId() == workflowId)
                 if (stepName.equals(getStepDefinition(step.getStepDefinitionId()).getName()))
                     return step;
         throw new FxNotFoundException("ex.step.notFound.name", stepName).asRuntimeException();
     }
 
     /**
      * Assign all steps
      *
      * @param steps all steps
      */
     protected void setSteps(List<Step> steps) {
         this.steps = ImmutableList.copyOf(steps);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Step> getSteps() {
         return steps;
     }
 
     protected void setWorkflows(List<Workflow> workflows) {
         this.workflows = ImmutableList.copyOf(workflows);
     }
 
     /**
      * Assign all defined mandators
      *
      * @param mandators all defined mandators
      */
     public void setMandators(Mandator[] mandators) {
         this.mandators = ImmutableList.copyOf(mandators);
     }
 
     /**
      * Assign all defined select lists
      *
      * @param lists select lists
      */
     public void setSelectLists(List<FxSelectList> lists) {
         this.selectLists = ImmutableList.copyOf(lists);
     }
 
     /**
      * Assign all defined groups
      *
      * @param groups all defined groups
      */
     protected void setGroups(List<FxGroup> groups) {
         this.groups = ImmutableList.copyOf(groups);
     }
 
     /**
      * Assign all defined properties
      *
      * @param properties all defined properties
      */
     protected void setProperties(List<FxProperty> properties) {
         this.properties = ImmutableList.copyOf(properties);
     }
 
     void initLookupTables() {
         initPropertyLookupTables();
 
         initAssignmentLookupTables();
     }
 
     void initPropertyLookupTables() {
         final ImmutableMap.Builder<Long, FxProperty> propLookupBuilder = ImmutableMap.builder();
         final ImmutableMap.Builder<String, FxProperty> nameLookupBuilder = ImmutableMap.builder();
         for (FxProperty property : properties) {
             propLookupBuilder.put(property.getId(), property);
             nameLookupBuilder.put(property.getName().toUpperCase(), property);
         }
         this.propertyLookup = propLookupBuilder.build();
         this.propertyNameLookup = nameLookupBuilder.build();
     }
 
     void initAssignmentLookupTables() {
         final ImmutableMap.Builder<Long, FxAssignment> assignmentLookup = ImmutableMap.builder();
         final ImmutableMap.Builder<String, FxAssignment> assignmentXPathLookup = ImmutableMap.builder();
         final Builder<FxPropertyAssignment> propertyAssignmentsEnabled = ImmutableList.builder();
         final Builder<FxGroupAssignment> groupAssignmentsEnabled = ImmutableList.builder();
 
         final Builder<FxPropertyAssignment> propertyAssignmentsAll = ImmutableList.builder();
         final List<FxPropertyAssignment> propertyAssignmentsSystemInternalRoot = new ArrayList<FxPropertyAssignment>();
         final Builder<FxGroupAssignment> groupAssignmentsAll = ImmutableList.builder();
         
         for (FxAssignment assignment : assignments) {
             assignmentXPathLookup.put(assignment.getXPath().toUpperCase(), assignment);
             assignmentLookup.put(assignment.getId(), assignment);
             if (assignment.isEnabled()) {
                 if (assignment instanceof FxPropertyAssignment) {
                     propertyAssignmentsEnabled.add((FxPropertyAssignment) assignment);
                 } else {
                     groupAssignmentsEnabled.add((FxGroupAssignment) assignment);
                 }
             }
             
             if (assignment instanceof FxPropertyAssignment) {
                 propertyAssignmentsAll.add((FxPropertyAssignment) assignment);
                 if (((FxPropertyAssignment) assignment).getProperty().isSystemInternal() && assignment.getAssignedTypeId() == 0)
                     propertyAssignmentsSystemInternalRoot.add((FxPropertyAssignment) assignment);
             } else if (assignment instanceof FxGroupAssignment) {
                 groupAssignmentsAll.add((FxGroupAssignment) assignment);
             }
         }
 
         Collections.sort(propertyAssignmentsSystemInternalRoot);
 
         this.groupAssignmentsEnabled = groupAssignmentsEnabled.build();
 
         this.propertyAssignmentsEnabled = propertyAssignmentsEnabled.build();
         this.assignmentLookup = assignmentLookup.build();
         this.assignmentXPathLookup = assignmentXPathLookup.build();
 
         this.propertyAssignmentsAll = propertyAssignmentsAll.build();
         this.propertyAssignmentsSystemInternalRoot = ImmutableList.copyOf(propertyAssignmentsSystemInternalRoot);
         this.groupAssignmentsAll = groupAssignmentsAll.build();
     }
 
     /**
      * Assign all defined types
      *
      * @param fxTypes all defined types
      */
     protected void setTypes(List<FxType> fxTypes) {
         this.types = ImmutableList.copyOf(fxTypes);
     }
 
     /**
      * Assign FxAssignments (mixed groups/properties)
      *
      * @param assignments all assignments (mixed groups/properties)
      */
     protected void setAssignments(List<FxAssignment> assignments) {
         this.assignments = ImmutableList.copyOf(assignments);
     }
 
     /**
      * Set scripts
      *
      * @param scripts all scripts
      */
     public void setScripts(List<FxScriptInfo> scripts) {
         this.scripts = ImmutableList.copyOf(scripts);
     }
 
     /**
      * Set script mappings
      *
      * @param scriptMappings all mappings
      */
     public void setScriptMappings(List<FxScriptMapping> scriptMappings) {
         this.scriptMappings = ImmutableList.copyOf(scriptMappings);
     }
 
     /**
      * Set script schedules
      *
      * @param scriptSchedules all script schedules
      */
     public void setScriptSchedules(List<FxScriptSchedule> scriptSchedules) {
         this.scriptSchedules = ImmutableList.copyOf(scriptSchedules);
     }
 
     /**
      * Set the available languages.
      *
      * @param languages the available languages
      * @since 3.1
      */
     public void setLanguages(List<FxLanguage> languages) {
         this.languages = ImmutableList.copyOf(languages);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxDataType> getDataTypes() {
         return dataTypes;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxDataType getDataType(long id) {
         for (FxDataType dataType : dataTypes)
             if (dataType.getId() == id)
                 return dataType;
         throw new FxNotFoundException("ex.structure.dataType.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ACL getACL(long id) {
         for (ACL acl : acls)
             if (acl.getId() == id)
                 return acl;
         throw new FxNotFoundException("ex.structure.acl.notFound.id", id).asRuntimeException();
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ACL getACL(String name) {
         for (ACL acl : acls)
             if (acl.getName().equals(name))
                 return acl;
         throw new FxNotFoundException("ex.structure.acl.notFound.name", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean aclExists(String name) {
         for (ACL acl : acls)
             if (acl.getName().equals(name))
                 return true;
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<ACL> getACLs() {
         return acls;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<ACL> getACLs(ACLCategory category) {
         List<ACL> result = new ArrayList<ACL>(acls.size());
         for (ACL acl : acls) {
             if (acl.getCategory() == category) {
                 result.add(acl);
             }
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<ACL> getACLs(long mandatorId) {
         return getACLs(mandatorId, null, true);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<ACL> getACLs(long mandatorId, boolean includeForeignAccessible) {
         return getACLs(mandatorId, null, includeForeignAccessible);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<ACL> getACLs(long mandatorId, ACLCategory category, boolean includeForeignAccessible) {
         final UserTicket ticket = FxContext.getUserTicket();
         final List<ACL> result = new ArrayList<ACL>();
         for (ACL acl : acls) {
             if ((acl.getMandatorId() == mandatorId                                          // mandator filter matches
                     || (includeForeignAccessible && ticket.isAssignedToACL(acl.getId())))   // user assigned to mandator-foreign ACL
                     && (category == null || category.equals(acl.getCategory()))) {          // category filter matches
                 result.add(acl);
             }
         }
         Collections.sort(result, new FxSharedUtils.SelectableObjectWithNameSorter());
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ACL getDefaultACL(ACLCategory category) {
         return getACL(category.getDefaultId());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Workflow getWorkflow(long id) {
         for (Workflow wf : workflows)
             if (wf.getId() == id)
                 return wf;
         throw new FxNotFoundException("ex.structure.workflow.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Workflow getWorkflow(String name) {
         for (Workflow wf : workflows)
             if (wf.getName().equals(name))
                 return wf;
         throw new FxNotFoundException("ex.structure.workflow.notFound.id", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Workflow> getWorkflows() {
         return workflows;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Mandator getMandator(long id) {
         for (Mandator mandator : mandators)
             if (mandator.getId() == id)
                 return mandator;
         throw new FxNotFoundException("ex.structure.mandator.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Mandator getMandator(String name) {
         for (Mandator mandator : mandators) {
             if (mandator.getName().equals(name)) {
                 return mandator;
             }
         }
         throw new FxNotFoundException("ex.structure.mandator.notFound.name", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Mandator> getMandators(boolean active, boolean inactive) {
         ArrayList<Mandator> mand = new ArrayList<Mandator>(10);
         for (Mandator mandator : mandators) {
             switch ((mandator.isActive() ? 1 : 0)) {
                 case 1:
                     if (active)
                         mand.add(mandator);
                     break;
                 case 0:
                     if (inactive)
                         mand.add(mandator);
                     break;
 
             }
         }
         return mand;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxGroup> getGroups(boolean returnReferenced, boolean returnUnreferenced,
                                    boolean returnRootGroups, boolean returnSubGroups) {
         if (returnReferenced && returnUnreferenced && returnRootGroups && returnSubGroups) {
             return groups;
         }
         ArrayList<FxGroup> result = new ArrayList<FxGroup>(groups.size());
         boolean add;
         boolean foundRoot, foundSub;
         for (FxGroup group : groups) {
             add = returnReferenced && group.isReferenced();
             if (returnUnreferenced && !group.isReferenced())
                 add = true;
             if (returnReferenced && !returnRootGroups && !returnSubGroups)
                 continue;
             if (add && group.isReferenced() && !(returnRootGroups && returnSubGroups)) {
                 //filter either root or sub groups
                 foundRoot = foundSub = false;
                 for (FxGroupAssignment ga : groupAssignmentsAll) {
                     if (ga.getGroup().getId() == group.getId()) {
                         if (ga.getParentGroupAssignment() == null)
                             foundRoot = true;
                         else
                             foundSub = true;
                     }
                 }
                 if (returnRootGroups && !foundRoot)
                     add = false;
                 if (returnSubGroups && !foundSub)
                     add = false;
             }
             if (add)
                 result.add(group);
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxGroup getGroup(long id) {
         for (FxGroup group : groups)
             if (group.getId() == id)
                 return group;
         throw new FxNotFoundException("ex.structure.group.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxGroup getGroup(String name) {
         for (FxGroup group : groups)
             if (group.getName().equalsIgnoreCase(name))
                 return group;
         throw new FxNotFoundException("ex.structure.group.notFound.name", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxProperty> getProperties(boolean returnReferenced, boolean returnUnreferenced) {
         if (returnReferenced && returnUnreferenced)
             return properties;
         ArrayList<FxProperty> result = new ArrayList<FxProperty>(properties.size());
         for (FxProperty prop : properties) {
             if (returnReferenced && prop.isReferenced())
                 result.add(prop);
             if (returnUnreferenced && !prop.isReferenced())
                 result.add(prop);
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxProperty getProperty(long id) {
         final FxProperty result = propertyLookup.get(id);
         if (result != null) {
             return result;
         }
         throw new FxNotFoundException("ex.structure.property.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxProperty getProperty(String name) {
         final FxProperty property = propertyNameLookup.get(name.toUpperCase());
         if (property != null) {
             return property;
         }
         throw new FxNotFoundException("ex.structure.property.notFound.name", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean propertyExists(String name) {
         return propertyNameLookup.containsKey(name.toUpperCase());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean propertyExistsInType(String typeName, String propertyName) {
         if (!typeExists(typeName))
             throw new FxNotFoundException("ex.structure.type.notFound.name", typeName).asRuntimeException();
 
         final long typeId = getType(typeName).getId();
         for (FxPropertyAssignment a : getPropertyAssignments(true)) {
             if (a.getAssignedTypeId() == typeId && !a.isDerivedAssignment()) {
                 if (propertyName.toUpperCase().equals(a.getProperty().getName()))
                     return true;
             }
         }
 
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean groupExistsInType(String typeName, String groupName) {
         if (!typeExists(typeName))
             throw new FxNotFoundException("ex.structure.type.notFound.name", typeName).asRuntimeException();
 
         final long typeId = getType(typeName).getId();
         for (FxGroupAssignment a : getGroupAssignments(true)) {
             if (a.getAssignedTypeId() == typeId && !a.isDerivedAssignment()) {
                 if (groupName.toUpperCase().equals(a.getGroup().getName()))
                     return true;
             }
         }
 
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean assignmentExists(String xPath) {
         if (xPath != null && xPath.trim().length() > 0) {
             String xPathUpper = xPath.toUpperCase(Locale.ENGLISH);
             if (!XPathElement.isValidXPath(xPathUpper))
                 return false; //avoid exceptions on malformed xpath's
             xPathUpper = XPathElement.toXPathNoMult(xPathUpper);
             return assignmentXPathLookup.containsKey(xPathUpper);
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean groupExists(String name) {
         for (FxGroup check : groups)
             if (check.getName().equalsIgnoreCase(name))
                 return true;
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxPropertyAssignment> getPropertyAssignments() {
         return getPropertyAssignments(false);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxPropertyAssignment> getSystemInternalRootPropertyAssignments() {
         return propertyAssignmentsSystemInternalRoot;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxPropertyAssignment> getPropertyAssignments(boolean includeDisabled) {
         return includeDisabled ? propertyAssignmentsAll : propertyAssignmentsEnabled;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxPropertyAssignment> getPropertyAssignments(long propertyId, boolean includeDisabled) {
         final List<FxPropertyAssignment> assignments = includeDisabled ? propertyAssignmentsAll : propertyAssignmentsEnabled;
         final List<FxPropertyAssignment> result = new ArrayList<FxPropertyAssignment>();
         for (FxPropertyAssignment assignment : assignments) {
             if (assignment.getProperty().getId() == propertyId) {
                 result.add(assignment);
             }
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxGroupAssignment> getGroupAssignments() {
         return getGroupAssignments(false);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxGroupAssignment> getGroupAssignments(boolean includeDisabled) {
         return includeDisabled ? groupAssignmentsAll : groupAssignmentsEnabled;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxGroupAssignment> getGroupAssignments(long groupId, boolean includeDisabled) {
         final List<FxGroupAssignment> assignments = includeDisabled ? groupAssignmentsAll : groupAssignmentsEnabled;
         final List<FxGroupAssignment> result = new ArrayList<FxGroupAssignment>();
         for (FxGroupAssignment assignment : assignments) {
             if (assignment.getGroup().getId() == groupId) {
                 result.add(assignment);
             }
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxType> getTypes() {
         return getTypes(true, true, true, true);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxType> getTypes(boolean returnBaseTypes, boolean returnDerivedTypes,
                                  boolean returnTypes, boolean returnRelations) {
         return _getTypes(returnBaseTypes, returnDerivedTypes, returnTypes, returnRelations);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxType> getReferencingRelationTypes(long typeId) {
         ArrayList<FxType> relTypes = new ArrayList<FxType>();
         List<FxType> relations = getTypes(true, true, false, true);
         for (FxType t : relations) {
             for (FxTypeRelation r : t.getRelations()) {
                 if (r.getDestination().getId() == typeId || r.getSource().getId() == typeId) {
                     relTypes.add(t);
                     break;
                 }
             }
         }
         return relTypes;
     }
 
     /**
      * Get types depending on selection criteria
      *
      * @param returnBaseTypes    return types that are not derived from another type
      * @param returnDerivedTypes return types that are derived from another type
      * @param returnTypes        return FxTypes
      * @param returnRelations    return FxTypes that are relations
      * @return FxType iterator
      */
     private List<FxType> _getTypes(boolean returnBaseTypes, boolean returnDerivedTypes, boolean returnTypes, boolean returnRelations) {
         if (returnBaseTypes && returnDerivedTypes && returnTypes && returnRelations)
             return this.types;
         ArrayList<FxType> ret = new ArrayList<FxType>(this.types.size());
         for (FxType t : types) {
             if (t.getMode() == TypeMode.Relation && returnRelations) {
                 if (t.getParent() == null && returnBaseTypes)
                     ret.add(t);
                 else if (t.getParent() != null && returnDerivedTypes)
                     ret.add(t);
             } else if (t.getMode() != TypeMode.Relation && returnTypes) {
                 if (t.getParent() == null && returnBaseTypes)
                     ret.add(t);
                 else if (t.getParent() != null && returnDerivedTypes)
                     ret.add(t);
             }
         }
         return ret;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxAssignment getAssignment(String xPath) {
         if (xPath != null && xPath.trim().length() > 0) {
             xPath = XPathElement.toXPathNoMult(xPath);
             // XPath is already in upper case
             final FxAssignment assignment = assignmentXPathLookup.get(xPath);
             if (assignment != null) {
                 return assignment;
             }
         }
         throw new FxNotFoundException("ex.structure.assignment.notFound.xpath", xPath).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxPropertyAssignment getPropertyAssignment(String xpath) {
         final FxAssignment assignment = getAssignment(xpath);
         try {
             return (FxPropertyAssignment) assignment;
         } catch (ClassCastException e) {
             throw new FxInvalidParameterException(xpath, "ex.structure.assignment.property.type",
                     xpath, assignment.getClass()).asRuntimeException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxPropertyAssignment getPropertyAssignment(long id) {
         final FxAssignment assignment = getAssignment(id);
         try {
             return (FxPropertyAssignment) assignment;
         } catch (ClassCastException e) {
             throw new FxInvalidParameterException("id", "ex.structure.assignment.property.id",
                     id, assignment.getClass()).asRuntimeException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxAssignment getAssignment(long assignmentId) {
         final FxAssignment result = assignmentLookup.get(assignmentId);
         if (result != null) {
             return result;
         }
         throw new FxNotFoundException("ex.structure.assignment.notFound.id", assignmentId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxAssignment> getDerivedAssignments(long assignmentId) {
         List<FxAssignment> ret = null;
         for (FxAssignment as : assignments)
             if (as.getBaseAssignmentId() == assignmentId) {
                 if (ret == null)
                     ret = new ArrayList<FxAssignment>(5);
                 ret.add(as);
             }
         if (ret == null)
             ret = new ArrayList<FxAssignment>(0);
         else {
             List<FxAssignment> ret2 = new ArrayList<FxAssignment>(0);
             for (FxAssignment as : ret) {
                 ret2.addAll(getDerivedAssignments(as.getId()));
             }
             ret.addAll(ret2);
         }
         return ret;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxPropertyAssignment> getReferencingPropertyAssignments(long propertyId) {
         List<FxPropertyAssignment> assignments = getPropertyAssignments(true);
         List<FxPropertyAssignment> result = new ArrayList<FxPropertyAssignment>();
         for (FxPropertyAssignment assignment : assignments) {
             if (assignment.getProperty().getId() == propertyId && !assignment.isSystemInternal()) {
                 result.add(assignment);
             }
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxType getType(String name) {
         for (FxType type : types)
             if (type.getName().equalsIgnoreCase(name))
                 return type;
         throw new FxNotFoundException("ex.structure.type.notFound.name", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean typeExists(String name) {
         for (FxType type : types)
             if (type.getName().equalsIgnoreCase(name))
                 return true;
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxType getType(long id) {
         for (FxType type : types)
             if (type.getId() == id)
                 return type;
         throw new FxNotFoundException("ex.structure.type.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxType> getTypesForProperty(long propertyId) {
         List<FxType> ret = new ArrayList<FxType>(10);
         for (FxPropertyAssignment as : propertyAssignmentsAll) {
             if (as.getProperty().getId() != propertyId)
                 continue;
             if (ret.contains(as.getAssignedType()))
                 continue;
             ret.add(as.getAssignedType());
         }
         return ret;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Route getRoute(long routeId) {
         for (Workflow workflow : workflows) {
             for (Route route : workflow.getRoutes()) {
                 if (route.getId() == routeId) {
                     return route;
                 }
             }
         }
         throw new FxNotFoundException("ex.structure.route.notFound.id", routeId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxScriptInfo> getScripts() {
         return scripts;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxScriptInfo getScript(long scriptId) {
         for (FxScriptInfo si : this.scripts)
             if (si.getId() == scriptId)
                 return si;
         throw new FxNotFoundException("ex.scripting.notFound.id", scriptId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxScriptInfo getScript(String name) {
         for (FxScriptInfo si : this.scripts)
             if (si.getName().equals(name))
                 return si;
         throw new FxNotFoundException("ex.scripting.notFound.id", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean scriptExists(String name) {
         for (FxScriptInfo si : this.scripts)
             if (si.getName().equals(name))
                 return true;
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxScriptMapping> getScriptMappings() {
         return scriptMappings;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxScriptMapping getScriptMapping(long scriptId) {
         for (FxScriptMapping mapping : this.scriptMappings)
             if (mapping.getScriptId() == scriptId)
                 return mapping;
         getScript(scriptId); //make sure the script exists
         FxScriptMapping mapping = new FxScriptMapping(scriptId, new ArrayList<FxScriptMappingEntry>(0), new ArrayList<FxScriptMappingEntry>(0));
         scriptMappings = updateOrAdd(scriptMappings, mapping);
         return mapping;
     }
 
     /**
      * {@inheritDoc}
      *
      * @since 3.1.2
      */
     @Override
     public List<FxScriptSchedule> getScriptSchedules() {
         return this.scriptSchedules;
     }
 
     /**
      * {@inheritDoc}
      *
      * @since 3.1.2
      */
     @Override
     public FxScriptSchedule getScriptSchedule(long scriptScheduleId) {
         for (FxScriptSchedule ss : this.scriptSchedules)
             if (ss.getId() == scriptScheduleId)
                 return ss;
         throw new FxNotFoundException("ex.scripting.schedule.notFound.id", scriptScheduleId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      *
      * @since 3.1.2
      */
     @Override
     public List<FxScriptSchedule> getScriptSchedulesForScript(long scriptId) {
         List<FxScriptSchedule> result = new ArrayList<FxScriptSchedule>(10);
         for (FxScriptSchedule ss : this.scriptSchedules)
             if (ss.getScriptId() == scriptId)
                 result.add(ss);
         return result;
     }
 
     /**
      * Resolve all missing dependencies
      *
      * @throws FxNotFoundException if a dependency could not be resolved
      */
     protected void resolveDependencies() throws FxNotFoundException {
         for (FxType type : types)
             type.resolveReferences(this);
         //calculate if properties and groups are referenced
         boolean ref;
         if (properties != null)
             for (FxProperty prop : properties) {
                 ref = false;
                 for (FxPropertyAssignment as : this.propertyAssignmentsAll)
                     if (as.getProperty().getId() == prop.getId()) {
                         ref = true;
                     }
                 prop.setReferenced(ref);
             }
         if (groups != null)
             for (FxGroup group : groups) {
                 ref = false;
                 for (FxGroupAssignment as : this.groupAssignmentsAll)
                     if (as.getGroup().getId() == group.getId()) {
                         ref = true;
                     }
                 group.setReferenced(ref);
             }
         for (FxAssignment as : this.propertyAssignmentsAll)
             as.resolveReferences(this);
         for (FxAssignment as : this.groupAssignmentsAll)
             as.resolveReferences(this);
         for (FxSelectList list : this.getSelectLists())
             list._synchronize(this);
         //2nd pass for types (scripting for assignments can only be resolved now)
         for (FxType type : types)
             type.resolveReferences(this);
         this.resolveFlatMappings();
     }
 
     /**
      * resolve flat storage mappings and prepare them by storage and level
      */
     private void resolveFlatMappings() {
         //storage-type-level-mapping
         this.flatMappings = new HashMap<String, Map<Long, Map<Integer, List<FxFlatStorageMapping>>>>(10);
         for (FxPropertyAssignment as : this.propertyAssignmentsAll) {
             if (!as.isFlatStorageEntry())
                 continue;
             FxFlatStorageMapping mapping = as.getFlatStorageMapping();
             if (!flatMappings.containsKey(mapping.getStorage()))
                 flatMappings.put(mapping.getStorage(), new HashMap<Long, Map<Integer, List<FxFlatStorageMapping>>>(10));
             Map<Long, Map<Integer, List<FxFlatStorageMapping>>> typeMap = flatMappings.get(mapping.getStorage());
             final Long assignedTypeId = as.getAssignedTypeId();
             if (!typeMap.containsKey(assignedTypeId))
                 typeMap.put(assignedTypeId, new HashMap<Integer, List<FxFlatStorageMapping>>(10));
             Map<Integer, List<FxFlatStorageMapping>> levelMap = typeMap.get(assignedTypeId);
             final Integer level = mapping.getLevel();
             if (!levelMap.containsKey(level))
                 levelMap.put(level, new ArrayList<FxFlatStorageMapping>(40));
             List<FxFlatStorageMapping> mapList = levelMap.get(level);
             mapList.add(mapping);
         }
     }
 
     /**
      * Update or add an existing ACL
      *
      * @param _acl ACL to update/add
      */
     protected void updateACL(ACL _acl) {
         this.acls = updateOrAdd(this.acls, _acl);
     }
 
     /**
      * Remove an existing ACL
      *
      * @param id ACL to remove
      */
     protected void removeACL(long id) {
         this.acls = remove(this.acls, getACL(id));
     }
 
     /**
      * Update or add a FxType
      *
      * @param type type to update/add
      * @throws FxNotFoundException on dependency errors
      */
     public void updateType(FxType type) throws FxNotFoundException {
         this.types = updateOrAdd(this.types, type);
         resolveDependencies();
     }
 
     /**
      * Add a mandator
      *
      * @param mandator mandator
      */
     protected void addMandator(Mandator mandator) {
         this.mandators = updateOrAdd(this.mandators, mandator);
         this.inactiveMandators = null;
     }
 
     /**
      * Update a mandator, silently fails if the mandator does not exist
      *
      * @param mandator mandator
      */
     public void updateMandator(Mandator mandator) {
         this.mandators = updateOrAdd(this.mandators, mandator);
         this.inactiveMandators = null;
     }
 
     /**
      * Remove a mandator
      *
      * @param mandatorId mandator id to remove
      */
     public void removeMandator(long mandatorId) {
         this.mandators = remove(this.mandators, getMandator(mandatorId));
         this.inactiveMandators = null;
     }
 
     /**
      * Update scripts after changes
      *
      * @param scripts         all scripts
      * @param scriptMapping   all mappings
      * @param scriptSchedules scriptSchedules
      * @throws FxNotFoundException if dependencies can not be resolved
      */
     public void updateScripting(List<FxScriptInfo> scripts, List<FxScriptMapping> scriptMapping, List<FxScriptSchedule> scriptSchedules) throws FxNotFoundException {
         this.scripts = ImmutableList.copyOf(scripts);
         this.scriptMappings = ImmutableList.copyOf(scriptMapping);
         this.scriptSchedules = ImmutableList.copyOf(scriptSchedules);
         resolveDependencies();
     }
 
     /**
      * Perform a 'deep' clone (copy) of this instance
      *
      * @return FxEnvironmentImpl
      */
     public FxEnvironmentImpl deepClone() {
         return new FxEnvironmentImpl(this);
     }
 
     /**
      * Update the timestamp of the environment to the current time
      */
     public void updateTimeStamp() {
         this.timeStamp = System.currentTimeMillis();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public long getTimeStamp() {
         return this.timeStamp;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxSelectList> getSelectLists() {
         return this.selectLists;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxSelectList getSelectList(long id) {
         for (FxSelectList list : this.selectLists)
             if (id == list.getId())
                 return list;
         throw new FxNotFoundException("ex.structure.list.notFound", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxSelectList getSelectList(String name) {
         for (FxSelectList list : this.selectLists)
             if (list.getName().equals(name))
                 return list;
         throw new FxNotFoundException("ex.structure.list.notFound", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean selectListExists(String name) {
         for (FxSelectList list : this.selectLists)
             if (list.getName().equals(name))
                 return true;
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxSelectListItem getSelectListItem(long id) {
         for (FxSelectList list : this.selectLists)
             if (list.containsItem(id))
                 return list.getItem(id);
         throw new FxNotFoundException("ex.structure.list.item.notFound", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxSelectListItem getSelectListItem(FxSelectList list, String name) {
         if (list.containsItem(name))
             return list.getItem(name);
         throw new FxNotFoundException("ex.structure.list.item.notFound.name", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getInactiveMandatorList() {
         if (inactiveMandators != null)
             return inactiveMandators;
         StringBuilder sb = new StringBuilder(50);
         for (Mandator m : mandators) {
             if (!m.isActive()) {
                 if (sb.length() > 0)
                     sb.append(',');
                 sb.append(m.getId());
             }
         }
         inactiveMandators = sb.toString();
         return inactiveMandators;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getDeactivatedTypesList() {
         if (deactivatedTypes != null)
             return deactivatedTypes;
         StringBuilder sb = new StringBuilder(50);
         for (FxType t : types)
             if (t.getState() == TypeState.Unavailable) {
                 if (sb.length() > 0)
                     sb.append(',');
                 sb.append(t.getId());
             }
         deactivatedTypes = sb.toString();
         return deactivatedTypes;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxFlatStorageMapping> getFlatStorageMappings(String storage, long typeId, int level) {
         try {
             final List<FxFlatStorageMapping> mappings = flatMappings.get(storage).get(typeId).get(level);
             return mappings != null ? Collections.unmodifiableList(mappings) : Collections.<FxFlatStorageMapping>emptyList();
         } catch (Exception e) {
             return Collections.emptyList();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxLanguage getLanguage(long id) {
         if (id == FxLanguage.SYSTEM_ID) {
             return FxLanguage.SYSTEM;
         }
         for (FxLanguage language : languages) {
             if (language.getId() == id) {
                 return language;
             }
         }
         throw new FxNotFoundException("ex.language.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxLanguage getLanguage(String isoCode) {
         isoCode = isoCode.toLowerCase();
         if(isoCode.length() > 2) {
             if("en_us".equals(isoCode))
                 return getLanguage(FxLanguage.ENGLISH_US);
             else if("en_uk".equals(isoCode))
                 return getLanguage(FxLanguage.ENGLISH_UK);
             if("de_ch".equals(isoCode))
                 return getLanguage(FxLanguage.GERMAN_CH);
         } else {
             for (FxLanguage language : languages) {
                 if (language.getIso2digit().equals(isoCode)) {
                     return language;
                 }
             }
         }
         throw new FxNotFoundException("ex.language.notFound.iso", isoCode).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isLanguageActive(long id) {
         if (id == FxLanguage.SYSTEM_ID) {
             return true;
         }
         for (FxLanguage language : languages) {
             if (language.getId() == id) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isLanguageActive(String isoCode) {
         isoCode = isoCode.toLowerCase();
         for (FxLanguage language : languages) {
             if (language.getIso2digit().equals(isoCode)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxLanguage> getLanguages() {
         return languages;   // already unmodifiable
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxType getMimeTypeMatch(String mimeType) {
         FxMimeType fxMimeType = FxMimeType.getMimeType(mimeType);
         // no proper match for given mimetype - return DocumentFile
         if (FxMimeType.UNKNOWN.equals(fxMimeType.getType()))
             return getType(FxType.DOCUMENTFILE);
 
         List<FxType> allMimeFxTypes = getType(FxType.DOCUMENTFILE).getDerivedTypes();
 
         FxType out = containsMimeType(allMimeFxTypes, fxMimeType, null);
         return out != null ? out : getType(FxType.DOCUMENTFILE);
     }
 
 
     /**
      * Private method to recursively check a list of FxTypes for a given FxMimeType
      * root == null on first call
      *
      * @param typeList the List&lt;FxType&gt;
      * @param mimeType the instance of FxMimeType
      * @param root     the root node
      * @return the FxType or null/the input root node if nothing found
      */
     private FxType containsMimeType(List<FxType> typeList, FxMimeType mimeType, FxType root) {
         FxType derivedMatch = null;
         for (FxType t : typeList) {
             FxMimeTypeWrapper wrapper = t.getMimeType();
             if (wrapper.contains(mimeType, true)) {
                 // assign root FxType for given (main) mime type
                 if (root == null)
                     root = t;
                 // have we got an exact match?
                 if (wrapper.contains(mimeType)) {
                     return t;
                 }
                 // do we have further derived types for the current one?
                 List<FxType> currentDerivedList = t.getDerivedTypes();
 
                 if (currentDerivedList != null && currentDerivedList.size() > 0) {
                     derivedMatch = containsMimeType(currentDerivedList, mimeType, root);
                 }
             }
         }
         return derivedMatch != null ? derivedMatch : root;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<UserGroup> getUserGroups() {
         return userGroups;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserGroup getUserGroup(long id) {
         for (UserGroup grp : userGroups)
             if (grp.getId() == id)
                 return grp;
         return null; //not found
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserGroup getUserGroup(String name) {
         for (UserGroup grp : userGroups)
             if (grp.getName().equalsIgnoreCase(name))
                 return grp;
         return null; //not found
     }
 
     /**
      * Set user groups
      *
      * @param userGroups user groups
      * @since 3.1.4
      */
     protected void setUserGroups(List<UserGroup> userGroups) {
         this.userGroups = ImmutableList.copyOf(userGroups);
     }
 
     private <T extends SelectableObject> ImmutableList<T> updateOrAdd(List<T> values, T updatedValue) {
         final Builder<T> result = ImmutableList.builder();
         boolean found = false;
         for (T value : values) {
             if (value.getId() == updatedValue.getId()) {
                 result.add(updatedValue);
                 found = true;
             } else {
                 result.add(value);
             }
         }
         if (!found) {
             // add new element
             result.add(updatedValue);
         }
         return result.build();
     }
 
     private <T extends SelectableObject> ImmutableList<T> remove(List<T> values, T removeValue) {
         final Builder<T> result = ImmutableList.builder();
         for (T value : values) {
             if (value.getId() != removeValue.getId()) {
                 result.add(value);
             }
         }
         return result.build();
     }
 }
