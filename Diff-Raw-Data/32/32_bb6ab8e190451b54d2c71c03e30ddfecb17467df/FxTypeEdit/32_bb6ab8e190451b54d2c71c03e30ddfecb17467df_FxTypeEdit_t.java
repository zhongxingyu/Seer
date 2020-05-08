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
 package com.flexive.shared.structure;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.media.FxMimeTypeWrapper;
 import com.flexive.shared.media.impl.FxMimeType;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.security.LifeCycleInfo;
 import com.flexive.shared.value.FxReference;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.workflow.Workflow;
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.StringUtils;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * FxType used for structure editing
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxTypeEdit extends FxType implements Serializable {
     private static final long serialVersionUID = 3089716188411029437L;
 
     private boolean isNew;
     protected boolean changed;
     private boolean enableParentAssignments;
     private boolean removeInstancesWithRelationTypes;
     private List<FxTypeRelation> originalRelations;
     private List<FxStructureOption> defaultOptions = Lists.newArrayList();
 
     /**
      * Constructor
      *
      * @param name                       name of the type
      * @param label                      label
      * @param acl                        type ACL
      * @param workflow                   workflow to use
      * @param parent                     parent type or <code>null</code> if not derived
      * @param enableParentAssignments    if parent is not <code>null</code> enable all derived assignments from the parent?
      * @param storageMode                the storage mode
      * @param category                   category mode (system, user)
      * @param mode                       type mode (content, relation)
      * @param language                   language mode
      * @param state                      type state (active, locked, etc)
      * @param permissions                permissions bit coded
      * @param multipleContentACLs        if multiple ACLs per content are enabled
      * @param includedInSupertypeQueries if this type should be included in supertype queries
      * @param trackHistory               track history
      * @param historyAge                 max. age of the history to track
      * @param maxVersions                max. number of versions to keep, if &lt; 0 unlimited
      * @param maxRelSource               max. number of instance related as source
      * @param maxRelDestination          max. number of instances related as destination
      * @param options                    List of FxStructureOptions
      */
     private FxTypeEdit(String name, FxString label, ACL acl, Workflow workflow, FxType parent,
                        boolean enableParentAssignments,
                        TypeStorageMode storageMode, TypeCategory category, TypeMode mode,
                        LanguageMode language, TypeState state, byte permissions, boolean multipleContentACLs,
                        boolean includedInSupertypeQueries,
                        boolean trackHistory, long historyAge, long maxVersions, int maxRelSource,
                        int maxRelDestination, List<FxStructureOption> options) {
         super(-1, acl, workflow, name, label, parent, storageMode, category, mode, language, state, permissions,
                 multipleContentACLs, includedInSupertypeQueries, trackHistory, historyAge,
                 maxVersions, maxRelSource, maxRelDestination, null, null, new ArrayList<FxTypeRelation>(5), options);
         FxSharedUtils.checkParameterMultilang(label, "label");
         this.enableParentAssignments = enableParentAssignments;
         this.isNew = true;
         this.changed = true;
         this.removeInstancesWithRelationTypes = false;
         this.relations = new ArrayList<FxTypeRelation>(super.getRelations());
         this.originalRelations = new ArrayList<FxTypeRelation>(super.getRelations());
     }
 
     /**
      * Create a new FxTypeEdit instance for editing and updating an existing FxType
      *
      * @param type the FxType to edit
      */
     public FxTypeEdit(FxType type) {
         super(type.getId(), type.getACL(), type.getWorkflow(),
                 type.getName(), type.getLabel(), type.getParent(), type.getStorageMode(), type.getCategory(),
                 type.getMode(), type.getLanguage(), type.getState(), type.permissions, type.isMultipleContentACLs(),
                 type.isIncludedInSupertypeQueries(), type.isTrackHistory(), type.getHistoryAge(), type.getMaxVersions(), type.getMaxRelSource(),
                 type.getMaxRelDestination(), type.getLifeCycleInfo(), type.getDerivedTypes(), type.getRelations(), FxStructureOption.cloneOptions(type.options));
         FxSharedUtils.checkParameterMultilang(label, "label");
         this.isNew = false;
         this.scriptMapping = type.scriptMapping;
         //make lists editable again
         this.assignedGroups = new ArrayList<FxGroupAssignment>(type.assignedGroups);
         this.assignedProperties = new ArrayList<FxPropertyAssignment>(type.assignedProperties);
         this.relations = new ArrayList<FxTypeRelation>(type.relations);
         this.changed = false;
         this.removeInstancesWithRelationTypes = false;
         this.originalRelations = new ArrayList<FxTypeRelation>(super.getRelations());
         this.icon = type.getIcon();
     }
 
     /**
      * Create a new FxTypeEdit instance for creating a new FxType
      *
      * @param name name of the type
      * @return FxTypeEdit instance for creating a new FxType
      */
     public static FxTypeEdit createNew(String name) {
         return createNew(name, new FxString(FxLanguage.DEFAULT_ID, name),
                 CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId()), null);
     }
 
     /**
      * Create a new FxTypeEdit instance for creating a derived FxType
      *
      * @param name           name of the type
      * @param parentTypeName parent type name
      * @return FxTypeEdit instance for creating a new FxType
      * @since 3.1
      */
     public static FxTypeEdit createNew(String name, String parentTypeName) {
         return createNew(name, CacheAdmin.getEnvironment().getType(parentTypeName));
     }
 
     /**
      * Create a new FxTypeEdit instance for creating a derived FxType
      *
      * @param name       name of the type
      * @param parentType the parent type ID
      * @return FxTypeEdit instance for creating a new FxType
      * @since 3.0.3
      */
     public static FxTypeEdit createNew(String name, FxType parentType) {
         return createNew(name, new FxString(FxLanguage.DEFAULT_ID, name),
                 parentType == null
                         ? CacheAdmin.getEnvironment().getACL(ACLCategory.STRUCTURE.getDefaultId())
                         : parentType.getACL(),
                 parentType);
     }
 
     /**
      * Create a new FxTypeEdit instance for creating a new FxType
      *
      * @param name         name of the type
      * @param parentTypeId the parent type ID
      * @return FxTypeEdit instance for creating a new FxType
      * @since 3.0.3
      */
     public static FxTypeEdit createNew(String name, long parentTypeId) {
         final FxEnvironment env = CacheAdmin.getEnvironment();
         final FxType parent = env.getType(parentTypeId);
         return createNew(name, new FxString(FxLanguage.DEFAULT_ID, name), parent.getACL(), parent);
     }
 
     /**
      * Create a new FxTypeEdit instance for creating a new FxType
      *
      * @param name  name of the type
      * @param label label
      * @param acl   type ACL
      * @return FxTypeEdit instance for creating a new FxType
      */
     public static FxTypeEdit createNew(String name, FxString label, ACL acl) {
         return createNew(name, label, acl, null);
     }
 
     /**
      * Create a new FxTypeEdit instance derived from an existing FxType
      *
      * @param name   name of the type
      * @param label  label
      * @param acl    type ACL
      * @param parent parent type or <code>null</code> if not derived
      * @return FxTypeEdit instance for creating a new FxType
      */
     public static FxTypeEdit createNew(String name, FxString label, ACL acl, FxType parent) {
         List<FxStructureOption> options = parent == null ? FxStructureOption.getEmptyOptionList(2) : parent.getInheritedOptions();
         if(parent != null && parent.isMimeTypeSet()) {
             options = removeMimeTypes(options);
         }
 
         if (parent == null) {
             return createNew(name, label, acl,
                     CacheAdmin.getEnvironment().getWorkflows().get(0),
                    null, false, TypeStorageMode.Hierarchical,
                     TypeCategory.User, TypeMode.Content, LanguageMode.Multiple, TypeState.Available,
                     getDefaultTypePermissions(), false, 0, -1, 0, 0, options);
         } else {
             return createNew(name, label, acl,
                     parent.getWorkflow(),
                     parent, true, parent.getStorageMode(),
                     TypeCategory.User, parent.getMode(),
                     parent.getLanguage(), TypeState.Available,
                     parent.getBitCodedPermissions(), parent.isTrackHistory(), parent.getHistoryAge(),
                     parent.getMaxVersions(), parent.getMaxRelSource(), parent.getMaxRelDestination(),
                     options);
         }
     }
 
     /**
      * Get the default permission set for new types
      *
      * @return default permission set for new types
      */
     private static byte getDefaultTypePermissions() {
         return FxPermissionUtils.encodeTypePermissions(true, false, true, true);
     }
 
     /**
      * Automatically remove the mime type configuration (not the key, values only) for any types which are created
      * as derived types from a DOCUMENTFILE type parent and set a dummy mime type as a placeholder (application/*)
      * // TODO: this behaviour is subject to a possible change
      * 
      * @param typeOptions the type options
      * @return type options w/o MIMETYPE values
      */
     private static List<FxStructureOption> removeMimeTypes(List<FxStructureOption> typeOptions) {
         final List<FxStructureOption> out = FxStructureOption.cloneOptions(typeOptions);
         if(FxStructureOption.hasOption(FxStructureOption.OPTION_MIMETYPE, out)) {
             FxStructureOption.setOption(out, FxStructureOption.OPTION_MIMETYPE, true, true, new FxMimeTypeWrapper(FxMimeType.PLACEHOLDER).toString());
         }
 
         return out;
     }
 
     /**
      * Create a new FxTypeEdit instance for creating a new FxType (no structure options)
      *
      * @param name                    name of the type
      * @param label                   label
      * @param acl                     type ACL
      * @param workflow                workflow to use
      * @param parent                  parent type or <code>null</code> if not derived
      * @param enableParentAssignments if parent is not <code>null</code> enable all derived assignments from the parent?
      * @param storageMode             the storage mode
      * @param category                category mode (system, user)
      * @param mode                    type mode (content, relation)
      * @param language                language mode
      * @param state                   type state (active, locked, etc)
      * @param permissions             permissions bit coded
      * @param trackHistory            track history
      * @param historyAge              max. age of the history to track
      * @param maxVersions             max. number of versions to keep, if &lt; 0 unlimited
      * @param maxRelSource            max. number of instance related as source
      * @param maxRelDestination       max. number of instance related as destination
      * @return FxTypeEdit instance for creating a new FxType
      */
     public static FxTypeEdit createNew(String name, FxString label, ACL acl, Workflow workflow,
                                        FxType parent, boolean enableParentAssignments,
                                        TypeStorageMode storageMode, TypeCategory category, TypeMode mode,
                                        LanguageMode language, TypeState state, byte permissions,
                                        boolean trackHistory, long historyAge, long maxVersions, int maxRelSource,
                                        int maxRelDestination) {
         return new FxTypeEdit(name, label, acl, workflow, parent, enableParentAssignments,
                 storageMode, category, mode, language, state, permissions, parent == null || parent.isMultipleContentACLs(),
                 parent == null || parent.isIncludedInSupertypeQueries(), trackHistory, historyAge,
                 maxVersions, maxRelSource, maxRelDestination, null);
     }
 
     /**
      * Create a new FxTypeEdit instance for creating a new FxType
      *
      * @param name                    name of the type
      * @param label                   label
      * @param acl                     type ACL
      * @param workflow                workflow to use
      * @param parent                  parent type or <code>null</code> if not derived
      * @param enableParentAssignments if parent is not <code>null</code> enable all derived assignments from the parent?
      * @param storageMode             the storage mode
      * @param category                category mode (system, user)
      * @param mode                    type mode (content, relation)
      * @param language                language mode
      * @param state                   type state (active, locked, etc)
      * @param permissions             permissions bit coded
      * @param trackHistory            track history
      * @param historyAge              max. age of the history to track
      * @param maxVersions             max. number of versions to keep, if &lt; 0 unlimited
      * @param maxRelSource            max. number of instance related as source
      * @param maxRelDestination       max. number of instance related as destination
      * @param options                 list of structure options
      * @return FxTypeEdit instance for creating a new FxType
      */
     public static FxTypeEdit createNew(String name, FxString label, ACL acl, Workflow workflow,
                                        FxType parent, boolean enableParentAssignments,
                                        TypeStorageMode storageMode, TypeCategory category, TypeMode mode,
                                        LanguageMode language, TypeState state, byte permissions,
                                        boolean trackHistory, long historyAge, long maxVersions, int maxRelSource,
                                        int maxRelDestination, List<FxStructureOption> options) {
         return new FxTypeEdit(name, label, acl, workflow, parent, enableParentAssignments,
                 storageMode, category, mode, language, state, permissions, parent == null || parent.isMultipleContentACLs(),
                 parent == null || parent.isIncludedInSupertypeQueries(), trackHistory, historyAge,
                 maxVersions, maxRelSource, maxRelDestination, options);
     }
 
     /**
      * Set the LifeCycleInfo - this can only be assigned when there is no LifeCycleInfo available
      * (which is when it is <code>null</code>)
      *
      * @param lifeCycleInfo the lifeCycleInfo to assign if the current is <code>null</code>
      */
     public void setLifeCycleInfo(LifeCycleInfo lifeCycleInfo) {
         if (this.lifeCycleInfo == null)
             this.lifeCycleInfo = lifeCycleInfo;
     }
 
     /**
      * Is this a new type or updating an existing type?
      *
      * @return new or existing type?
      */
     public boolean isNew() {
         return isNew;
     }
 
     /**
      * If FxTypeRelation entries are removed, remove all affected instances?
      *
      * @return remove all affected instances if FxTypeRelation entries are removed?
      */
     public boolean isRemoveInstancesWithRelationTypes() {
         return removeInstancesWithRelationTypes;
     }
 
     /**
      * Set if affected instances should be removed if FxTypeRelations are removed
      *
      * @param removeInstancesWithRelationTypes
      *         if affected instances should be removed if FxTypeRelations are removed
      */
     public void setRemoveInstancesWithRelationTypes(boolean removeInstancesWithRelationTypes) {
         this.removeInstancesWithRelationTypes = removeInstancesWithRelationTypes;
     }
 
     /**
      * Enable parent assignments if creating a derived type?
      * This method has no effect if editing an existing type!
      *
      * @return are parent assignments enabled?
      */
     public boolean isEnableParentAssignments() {
         return enableParentAssignments;
     }
 
     /**
      * Enable parent assignments if creating a derived type?
      * This method has no effect if editing an existing type!
      *
      * @param enableParentAssignments are parent assignments enabled?
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setEnableParentAssignments(boolean enableParentAssignments) {
         this.enableParentAssignments = enableParentAssignments;
         this.changed = true;
         return this;
     }
 
     /**
      * Assign a new ACL
      *
      * @param ACL ACL to assign for this type
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setACL(ACL ACL) {
         this.ACL = ACL;
         this.changed = true;
         return this;
     }
 
     /**
      * Set the workflow to use
      *
      * @param workflow the workflow to use
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setWorkflow(Workflow workflow) {
         this.workflow = workflow;
         this.changed = true;
         return this;
     }
 
     /**
      * Set the name of this type
      *
      * @param name the name of this type
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setName(String name) {
         if (StringUtils.isEmpty(name))
             return this;
         this.name = name.toUpperCase().trim();
         this.changed = true;
         return this;
     }
 
 
     /**
      * Set the description (=label) of this type
      *
      * @param description description
      * @return the type itself, useful for chained calls
      * @deprecated replaced by {@link #setLabel(com.flexive.shared.value.FxString)}
      */
     @Deprecated
     public FxTypeEdit setDescription(FxString description) {
         return setLabel(description);
     }
 
     /**
      * Set the label of this type
      *
      * @param label label
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setLabel(FxString label) {
         FxSharedUtils.checkParameterMultilang(label, "label");
         this.label = label;
         this.changed = true;
         return this;
     }
 
     /**
      * Set the types category (user, system)
      *
      * @param category the category to set
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setCategory(TypeCategory category) {
         this.category = category;
         this.changed = true;
         return this;
     }
 
     /**
      * Set this type's mode.
      * Setting does not check anything, if the requested mode is really available and possible is determined during saving
      *
      * @param mode requested mode
      * @return this
      */
     public FxTypeEdit setMode(TypeMode mode) {
         this.mode = mode;
         this.changed = true;
         return this;
     }
 
     /**
      * Set the language mode to use for this type
      *
      * @param language language mode to use
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setLanguage(LanguageMode language) {
         this.language = language;
         this.changed = true;
         return this;
     }
 
     /**
      * Set the state of this type
      *
      * @param state the state of this type
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setState(TypeState state) {
         this.state = state;
         this.changed = true;
         return this;
     }
 
     /**
      * Set the preview icon of this type
      *
      * @param icon the (optional) preview icon of this type
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setIcon(FxReference icon) {
         this.icon = icon;
         this.changed = true;
         return this;
     }
 
     /**
      * Set the bit coded permissions of this type
      *
      * @param permissions bit coded permissions
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setPermissions(byte permissions) {
         this.permissions = permissions;
         this.changed = true;
         return this;
     }
 
     /**
      * Set usage of instance permissions
      *
      * @param use use instance permissions?
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setUseInstancePermissions(boolean use) {
         if (use == this.isUseInstancePermissions())
             return this;
         this.permissions ^= FxPermissionUtils.PERM_MASK_INSTANCE;
         this.changed = true;
         return this;
     }
 
     /**
      * Set usage of property permissions
      *
      * @param use use property permissions?
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setUsePropertyPermissions(boolean use) {
         if (use == this.isUsePropertyPermissions())
             return this;
         this.permissions ^= FxPermissionUtils.PERM_MASK_PROPERTY;
         this.changed = true;
         return this;
     }
 
     /**
      * Set usage of step permissions
      *
      * @param use use step permissions?
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setUseStepPermissions(boolean use) {
         if (use == this.isUseStepPermissions())
             return this;
         this.permissions ^= FxPermissionUtils.PERM_MASK_STEP;
         this.changed = true;
         return this;
     }
 
     /**
      * Set usage of type permissions
      *
      * @param use use type permissions?
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setUseTypePermissions(boolean use) {
         if (use == this.isUseTypePermissions())
             return this;
         this.permissions ^= FxPermissionUtils.PERM_MASK_TYPE;
         this.changed = true;
         return this;
     }
 
     /**
      * Track history for this type
      *
      * @param trackHistory track history?
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setTrackHistory(boolean trackHistory) {
         this.trackHistory = trackHistory;
         this.changed = true;
         return this;
     }
 
     /**
      * Set the max. age of history entries
      *
      * @param historyAge max. age of history entries
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setHistoryAge(long historyAge) {
         this.historyAge = (historyAge > 0 ? historyAge : 0);
         this.changed = true;
         return this;
     }
 
     /**
      * Set the max. number of versions to keep, if negative unlimited
      *
      * @param maxVersions max. number of versions to keep, if negative unlimited
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setMaxVersions(long maxVersions) {
         this.maxVersions = (maxVersions < -1 ? -1 : maxVersions);
         this.changed = true;
         return this;
     }
 
     /**
      * Set the max. number related source instances for this type, if negative unlimited
      *
      * @param maxRelSource max. number related source instances for this type, if negative or zero unlimited
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setMaxRelSource(int maxRelSource) {
         this.maxRelSource = maxRelSource < 0 ? 0 : maxRelSource;
         this.changed = true;
         return this;
     }
 
     /**
      * Enable or disable multiple ACLs per content instance.
      *
      * @param value true to enable multiple ACLs
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setMultipleContentACLs(boolean value) {
         this.multipleContentACLs = value;
         return this;
     }
 
     /**
      * Enable or disable inclusion of this type in supertype queries
      *
      * @param value true to enable
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setIncludedInSupertypeQueries(boolean value) {
         this.includedInSupertypeQueries = value;
         return this;
     }
 
     /**
      * Set the max. number related destination instances for this type, if negative or zero unlimited
      *
      * @param maxRelDestination max. number related destination instances for this type, if negative unlimited
      * @return the type itself, useful for chained calls
      */
     public FxTypeEdit setMaxRelDestination(int maxRelDestination) {
         this.maxRelDestination = maxRelDestination < 0 ? 0 : maxRelDestination;
         this.changed = true;
         return this;
     }
 
     /**
      * Find a relation in the given list that matches source and destination type
      *
      * @param rel  the relation to find
      * @param list the list to search
      * @return found relation or <code>null</code>
      */
     private FxTypeRelation findRelationInList(FxTypeRelation rel, List<FxTypeRelation> list) {
         for (FxTypeRelation check : list) {
             if (check.getSource().getId() == rel.getSource().getId() &&
                     check.getDestination().getId() == rel.getDestination().getId())
                 return check;
         }
         return null;
     }
 
     /*
      * Validate the source and destination of a relation.
      * @param rel      the relation to validate.
      * @throws FxInvalidParameterException if the relation is invalid.
      */
 
     private void validateRelation(FxTypeRelation rel) throws FxInvalidParameterException {
         if (rel.getSource().isRelation()) {
             throw new FxInvalidParameterException("ex.structure.type.relation.wrongTarget", this.getName(), rel.getSource().getName());
         }
         if (rel.getDestination().isRelation())
             throw new FxInvalidParameterException("ex.structure.type.relation.wrongTarget", this.getName(), rel.getDestination().getName());
     }
 
     /*
      * Validate if the current relation sources and destination multiplicites together with
      * newRelationToAdd don't exceed the given maxima (maxRelSource or maxRelDestination).
      *
      * @param   newRelationToAdd    the relation to be added.
      * @throws  FxInvalidParameterException if (maxRelSource or maxRelDestination is exceeded.
      *
     private void validateRelationMultiplicity(FxTypeRelation newRelationToAdd) throws FxInvalidParameterException {
         long source = newRelationToAdd.getMaxSource();
         long dest = newRelationToAdd.getMaxDestination();
 
         if (source == 0 && getMaxRelSource() >= 0)
             throw new FxInvalidParameterException("ex.structure.type.relation.maxRelSourceExceeded",
                     this.getName(), newRelationToAdd.getSource().getName(),
                     newRelationToAdd.getDestination().getName(), getMaxRelSource());
 
         else if (dest == 0 && getMaxRelDestination() >= 0)
             throw new FxInvalidParameterException("ex.structure.type.relation.maxRelDestExceeded",
                     this.getName(), newRelationToAdd.getSource().getName(),
                     newRelationToAdd.getDestination().getName(), getMaxRelDestination());
 
         for (FxTypeRelation rel : relations) {
             if (getMaxRelSource() >= 0) {
                 if (rel.getMaxSource() == 0 || rel.getMaxSource() + source > getMaxRelSource()) {
                     throw new FxInvalidParameterException("ex.structure.type.relation.maxRelSourceExceeded",
                             this.getName(), rel.getSource().getName(),
                             rel.getDestination().getName(), getMaxRelSource());
                 } else
                     source += rel.getMaxSource();
             }
             if (getMaxRelDestination() >= 0) {
                 if (rel.getMaxDestination() == 0 || rel.getMaxDestination() + dest > getMaxRelDestination()) {
                     throw new FxInvalidParameterException("ex.structure.type.relation.maxRelDestExceeded",
                             this.getName(), rel.getSource().getName(),
                             rel.getDestination().getName(), getMaxRelDestination());
                 } else
                     dest += rel.getMaxDestination();
             }
         }
     }
     */
 
     /**
      * Return if the max source or destination settings for two FxTypeRelations differ
      *
      * @param o1 relation 1
      * @param o2 relation 2
      * @return differ
      */
     private boolean relationDiffers(FxTypeRelation o1, FxTypeRelation o2) {
         return o1.getMaxDestination() != o2.getMaxDestination() || o1.getMaxSource() != o2.getMaxSource();
     }
 
     /**
      * Get a list of all FxTypeRelations that have been removed
      *
      * @return list of all FxTypeRelations that have been removed
      */
     public List<FxTypeRelation> getRemovedRelations() {
         List<FxTypeRelation> list = new ArrayList<FxTypeRelation>(5);
         for (FxTypeRelation rel : originalRelations)
             if (findRelationInList(rel, relations) == null)
                 list.add(rel);
         return list;
     }
 
     /**
      * Get a list of all FxTypeRelations that have been added
      *
      * @return list of all FxTypeRelations that have been added
      */
     public List<FxTypeRelation> getAddedRelations() {
         List<FxTypeRelation> list = new ArrayList<FxTypeRelation>(5);
         for (FxTypeRelation rel : relations)
             if (findRelationInList(rel, originalRelations) == null)
                 list.add(rel);
         return list;
     }
 
     /**
      * Get a list of all FxTypeRelations that have been updated
      *
      * @return list of all FxTypeRelations that have been updated
      */
     public List<FxTypeRelation> getUpdatedRelations() {
         List<FxTypeRelation> list = new ArrayList<FxTypeRelation>(5);
         FxTypeRelation curr;
         for (FxTypeRelation rel : relations) {
             curr = findRelationInList(rel, originalRelations);
             if ((curr != null && relationDiffers(curr, rel)) ||
                     (rel instanceof FxTypeRelationEdit && ((FxTypeRelationEdit) rel).isChanged()))
                 list.add(rel);
         }
         return list;
     }
 
     /**
      * Add or update a relation
      *
      * @param relation the relation to add or update
      * @return this
      * @throws FxInvalidParameterException if the relation is invalid.
      */
     public FxTypeEdit addRelation(FxTypeRelation relation) throws FxInvalidParameterException {
         validateRelation(relation);
         if (relations.contains(relation)) {
             relations.remove(relation);
             relations.add(relation);
         } else
             relations.add(relation);
         return this;
     }
 
     /**
      * Set the default options for new assignments created with
      * {@link #addProperty(String, FxDataType)}.
      *
      * @param defaultOptions the default options
      */
     public void setDefaultOptions(List<FxStructureOption> defaultOptions) {
         this.defaultOptions.clear();
         this.defaultOptions.addAll(defaultOptions);
     }
 
     /**
      * Add a new assignment to this type. The assignment is saved instantly and is returned to the caller.
      *
      * @param name     the assignment name
      * @param dataType the data type
      * @return the created assignment
      * @throws FxApplicationException on errors
      * @since 3.1
      */
     public FxPropertyAssignmentEdit addProperty(String name, FxDataType dataType) throws FxApplicationException {
         final String alias = getAssignmentAlias(name);
         // create property
         final FxPropertyEdit prop = FxPropertyEdit.createNew(
                 alias,
                 new FxString(true, alias),
                 new FxString(true, ""),
                 FxMultiplicity.MULT_0_1,
                 CacheAdmin.getEnvironment().getDefaultACL(ACLCategory.STRUCTURE),
                 dataType
         );
         prop.setAutoUniquePropertyName(true);
         prop.setOptions(defaultOptions);
         final long propId = EJBLookup.getAssignmentEngine().createProperty(
                 getId(),
                 prop,
                 getAssignmentParent(name)
         );
         return CacheAdmin.getEnvironment().getPropertyAssignment(propId).asEditable();
     }
 
     /**
      * Add a new group to this type. The group assignment is saved instantly and is returned to the caller.
      *
      * @param name the group name
      * @return the group assignment
      * @throws FxApplicationException on errors
      * @since 3.1
      */
     public FxGroupAssignmentEdit addGroup(String name) throws FxApplicationException {
         final String alias = getAssignmentAlias(name);
         final FxGroupEdit group = FxGroupEdit.createNew(
                 alias,
                 new FxString(true, alias),
                 new FxString(true, ""),
                 false,
                 FxMultiplicity.MULT_0_1
         );
         final long groupId = EJBLookup.getAssignmentEngine().createGroup(
                 getId(),
                 group,
                 getAssignmentParent(name)
         );
         return ((FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(groupId)).asEditable();
     }
 
     private String getAssignmentParent(String name) {
         return name.indexOf('/') == -1
                 ? "/"
                 : (name.charAt(0) == '/' ? "" : "/")
                 + name.substring(0, name.lastIndexOf('/'));
     }
 
     private String getAssignmentAlias(String name) {
         return name.indexOf('/') == -1 ? name : name.substring(name.lastIndexOf('/') + 1);
     }
 
 
     /**
      * Add or update a relation
      *
      * @param relation the relation to add or update
      * @throws FxInvalidParameterException if the relation is invalid.
      */
     public void updateRelation(FxTypeRelation relation) throws FxInvalidParameterException {
         addRelation(relation);
     }
 
     /**
      * Remove a relation
      *
      * @param relation the relation to remove
      */
     public void removeRelation(FxTypeRelation relation) {
         relations.remove(relation);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxGroupAssignment> getAssignedGroups() {
         return assignedGroups;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FxPropertyAssignment> getAssignedProperties() {
         return assignedProperties;
     }
 
     /**
      * Set structure options, overwrites any set options
      *
      * @param options a List of FxTypeStructureOptions
      * @return itself
      *
      * @since 3.1
      */
     public FxTypeEdit setOptions(List<FxStructureOption> options) {
         this.options = options;
         return this;
     }
 
     /**
      * Set an overridable option (not inherited by derived types if it doesn't exist)
      *
      * @param key   option key
      * @param value value of the option
      * @return the assignment itself, useful for chained calls
      * @throws FxInvalidParameterException if the property does not allow overriding
      *
      * @since 3.1
      */
     public FxTypeEdit setOption(String key, String value) throws FxInvalidParameterException {
         if (FxStructureOption.hasOption(key, options)) {
             FxStructureOption o = this.getOption(key);
             setOption(key, value, o.isOverrideable(), o.getIsInherited());
         } else {
             setOption(key, value, true, false);
             this.changed = true;
         }
         return this;
     }
 
     /**
      * Set an option
      * 
      * @param key   option key
      * @param value value of the option
      * @param overridable may derived types override the option?
      * @param isInherited will the option be inherited by derived types?
      * @return the assignment itself, useful for chained calls
      * @throws FxInvalidParameterException if the property does not allow overriding
      *
      * @since 3.1
      */
     public FxTypeEdit setOption(String key, String value, boolean overridable, boolean isInherited) throws FxInvalidParameterException {
         if(FxStructureOption.OPTION_MIMETYPE.equals(key)) {
             if(!mayAssignMimeType())
                 throw new FxInvalidParameterException(key, "ex.structure.type.mimetype.err");
         }
         final FxStructureOption pOpt = getOption(key);
         if (parent != null) {
             if (pOpt.isSet() && !pOpt.isOverrideable() && pOpt.getIsInherited()) {
                 // check if it was inherited from the supertype
                 final FxStructureOption parentOption = parent.getOption(pOpt.getKey());
                 if(parentOption.isSet() && parentOption.getIsInherited() && (!parentOption.equals(pOpt) || !pOpt.getValue().equals(value)))
                     throw new FxInvalidParameterException(key, "ex.structure.type.override.forbidden", key, parent.getName());
             }
         }
         FxStructureOption.setOption(options, key, overridable, isInherited, value);
         this.changed = true;
         return this;
     }
 
     /**
      * Set an overridable boolean option (not inherited by derived types if it doesn't exist)
      *
      * @param key   option key
      * @param value value of the option
      * @return the assignemnt itself, useful for chained calls
      * @throws FxInvalidParameterException if the property does not allow overriding
      *
      * @since 3.1
      */
     public FxTypeEdit setOption(String key, boolean value) throws FxInvalidParameterException {
         if (FxStructureOption.hasOption(key, options)) {
             FxStructureOption o = this.getOption(key);
             setOption(key, value, o.isOverrideable(), o.getIsInherited());
         } else {
             setOption(key, value, true, false);
             this.changed = true;
         }
         return this;
     }
 
     /**
      * Set a boolean option
      * 
      * @param key   option key
      * @param value value of the option
      * @param overridable may derived types override the option?
      * @param isInherited is the option inherited by derived types?
      * @return the assignemnt itself, useful for chained calls
      * @throws FxInvalidParameterException if the property does not allow overriding
      *
      * @since 3.1
      */
     public FxTypeEdit setOption(String key, boolean value, boolean overridable, boolean isInherited) throws FxInvalidParameterException {
 
         final FxStructureOption pOpt = getOption(key);
         if (parent != null) {
             if (pOpt.isSet() && !pOpt.isOverrideable() && pOpt.getIsInherited()) {
                 // check if it was inherited from the supertype
                 final FxStructureOption parentOption = parent.getOption(pOpt.getKey());
                 if(parentOption.isSet() && parentOption.getIsInherited() && (!parentOption.equals(pOpt) || !pOpt.isValueTrue() == value))
                     throw new FxInvalidParameterException(key, "ex.structure.type.override.forbidden", key, parent.getName());
             }
         }
         FxStructureOption.setOption(options, key, overridable, isInherited, value);
         this.changed = true;
         return this;
     }
 
     /**
      * Clear an option entry
      *
      * @param key option name
      *
      * @since 3.1
      */
     public void clearOption(String key) {
         FxStructureOption.clearOption(options, key);
     }
 
     /**
      * Set 1* MimeTypes for a given FxType. The mimetype can only be set for descendents of "DOCUMENTFILE"
      * 
      * @param mimeTypeWrapper the MimeType to set
      * @return this
      *
      * @since 3.1
      */
     public FxTypeEdit setMimeType(FxMimeTypeWrapper mimeTypeWrapper) {
         if(!mayAssignMimeType())
             throw new FxApplicationException("ex.structure.type.mimetype.err", this).asRuntimeException();
 
         FxStructureOption.setOption(options, FxStructureOption.OPTION_MIMETYPE, true, true, mimeTypeWrapper.toString());
         this.changed = true;
         return this;
     }
 
     /**
      * Checks whether a mime type may be assigned to the given FxType. Mime types can only be assigned to "DOCUMENTFILE"
      * or any of its derived types
      * 
      * @return returns true if a mime type may be assigned to this FxType
      *
      * @since 3.1
      */
     public boolean mayAssignMimeType() {
         return name.equals(DOCUMENTFILE) || parent != null && this.isDerivedFrom(DOCUMENTFILE);
     }
 
     /**
      * Have changes been made?
      *
      * @return changes made
      */
     public boolean isChanged() {
         return changed || getAddedRelations().size() > 0 || getRemovedRelations().size() > 0 || getUpdatedRelations().size() > 0;
     }
 
     /**
      * Save the type and return the saved instance.
      *
      * @return the saved instance.
      * @since 3.1
      * @throws FxApplicationException on errors
      */
     public FxTypeEdit save() throws FxApplicationException {
         final long id = EJBLookup.getTypeEngine().save(this);
         return CacheAdmin.getEnvironment().getType(id).asEditable();
     }
 }
