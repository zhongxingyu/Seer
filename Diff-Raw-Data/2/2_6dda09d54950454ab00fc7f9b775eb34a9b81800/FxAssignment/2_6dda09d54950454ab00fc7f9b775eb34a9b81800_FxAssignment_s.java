 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
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
 
 import com.flexive.shared.content.FxData;
 import com.flexive.shared.content.FxGroupData;
 import com.flexive.shared.exceptions.FxCreateException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.scripting.FxScriptMapping;
 import com.flexive.shared.scripting.FxScriptMappingEntry;
 import com.flexive.shared.value.FxString;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Base class for assignments of a group or property to a type or another group/property
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public abstract class FxAssignment implements Serializable, Comparable<FxAssignment> {
     private static final long serialVersionUID = -6127833297182838935L;
 
     /**
      * Empty list returned if no script mapping is defined for a FxScriptEvent
      */
     private final static long[] EMPTY_SCRIPTMAPPING = new long[0];
 
     /**
      * parent id value if an assignment has no parent
      */
     public static final long NO_PARENT = 0;
 
     /**
      * constant if an assignment has no base assignment
      */
     public static final long NO_BASE = -1;
 
     /**
      * base id value if an assignment belongs to the virtual root type
      */
     public static final long ROOT_BASE = 0;
 
     /**
      * Constant to determine type of the assignment in database; group
      */
     public final static int TYPE_GROUP = 0;
 
 
     /**
      * Constant to determine type of the assignment in database; property
      */
     public final static int TYPE_PROPERTY = 1;
 
     /**
      * Position constant to assign if an assignment should be positioned at the bottom
      */
     public final static int POSITION_BOTTOM = 9000;
 
     /**
      * Internal id of this assignment
      */
     private long assignmentId;
 
     /**
      * Is this assignment enabled at all?
      * Disabled assignments will only show up in the admin area for
      * structure and will be hidden from all other areas
      */
     protected boolean enabled;
 
     /**
      * FxType this assignment belongs to
      */
     private FxType assignedType;
 
     /**
      * Multiplicity of this assignment
      */
     protected FxMultiplicity multiplicity;
 
     /**
      * Default multiplicity, will be auto adjusted if &lt; min or &gt; max
      */
     protected int defaultMultiplicity;
 
     /**
      * Position of this assignment within the same XPath hierarchy
      */
     protected int position;
 
     /**
      * Absolute XPath without indices from the base FxType
      */
     protected String XPath;
 
     /**
      * (optional) alias, if not defined the name of the assigned element
      */
     protected String alias;
 
     /**
      * (optional) parent group
      */
     protected FxGroupAssignment parentGroupAssignment;
 
     /**
      * base assignment (if derived the parent, if not the root assignment, if its a root assignment FxAssignment.ROOT_BASE)
      */
     protected long baseAssignment;
 
     /**
      * (optional) description
      */
     protected FxString label;
 
     /**
      * (optional) hint
      */
     protected FxString hint;
     /**
      * Script mapping, is resolved while loading the environment
      */
     protected Map<FxScriptEvent, long[]> scriptMapping;
 
     private boolean systemInternal;
     List<FxStructureOption> options;
 
     /**
      * Constructor
      *
      * @param assignmentId          internal id of this assignment
      * @param enabled               is this assignment enabled?
      * @param assignedType          the FxType this assignment belongs to
      * @param alias                 an optional alias, if <code>null</code> the original name will be used
      * @param xpath                 absolute XPath without indices from the base FxType
      * @param position              position within the same XPath hierarchy
      * @param multiplicity          multiplicity
      * @param defaultMultiplicity   default multiplicity
      * @param parentGroupAssignment (optional) parent FxGroupAssignment this assignment belongs to
      * @param baseAssignment        base assignment (if derived the parent, if not the root assignment, if its a root assignment FxAssignment.ROOT_BASE)
      * @param label                 (optional) label
      * @param hint                  (optional) hint
      * @param options               options
      */
     protected FxAssignment(long assignmentId, boolean enabled, FxType assignedType, String alias, String xpath,
                            int position, FxMultiplicity multiplicity, int defaultMultiplicity,
                            FxGroupAssignment parentGroupAssignment,
                            long baseAssignment, FxString label, FxString hint, List<FxStructureOption> options) {
         this.alias = (alias != null ? alias.toUpperCase() : null);
         this.assignedType = assignedType;
         this.assignmentId = assignmentId;
         this.multiplicity = multiplicity;
         this.defaultMultiplicity = defaultMultiplicity;
         this.enabled = enabled;
         this.position = position;
         this.parentGroupAssignment = parentGroupAssignment;
         this.baseAssignment = baseAssignment;
         this.XPath = xpath;
         this.label = label;
         this.hint = hint;
         this.systemInternal = false;
         this.options = options;
         if (this.options == null)
             this.options = FxStructureOption.getEmptyOptionList(2);
     }
 
     /**
      * Get the alias of this assignment.
      * Groups and properties may define an alias to allow multiple use of the same group or property but
      * using a different name.
      * An alias is always defined, if not explicitly it is the properties/groups name.
      *
      * @return alias of this assignment
      */
     public String getAlias() {
         return alias;
     }
 
     /**
      * The internal id of this assignment as stored in the database
      *
      * @return internal id of this assignment
      */
     public long getId() {
         return assignmentId;
     }
 
     /**
      * Is this assignment enabled?
      *
      * @return enabled
      */
     public boolean isEnabled() {
         return enabled;
     }
 
     /**
      * Returns if this assignment is the child of another assignment or if it is directly attached to a FxType
      *
      * @return parent assignment
      */
     public boolean hasParentGroupAssignment() {
         return getParentGroupAssignment() != null;
     }
 
     /**
      * If this assignment is assigned to a group, the assignment of the parent group (in the context of the current type)
      *
      * @return parent group assignment of this assignment
      */
     public FxGroupAssignment getParentGroupAssignment() {
         return parentGroupAssignment;
     }
 
 
     /**
      * base assignment (if derived the parent, if not the root assignment, if its a root assignment FxAssignment.ROOT_BASE)
      *
      * @return base assignment
      * @see FxAssignment#ROOT_BASE
      */
     public long getBaseAssignmentId() {
         return baseAssignment;
     }
 
     /**
      * Returns true if the assignment is derived from a supertype.
      *
      * @return true if the assignment is derived from a supertype.
      */
     public boolean isDerivedAssignment() {
         return baseAssignment != FxAssignment.ROOT_BASE;
     }
 
     /**
      * The FxType this assignment is associated with
      *
      * @return FxType this assignment is associated with
      */
     public FxType getAssignedType() {
         return assignedType;
     }
 
     /**
      * Get the XPath of this assignment without indices
      *
      * @return XPath of this assignment without indices
      */
     public synchronized String getXPath() {
         if (XPath != null)
             return XPath;
         //build XPath
         StringBuffer sbXPath = new StringBuffer(200);
         sbXPath.append(this.getAlias());
         FxAssignment parent = this.getParentGroupAssignment();
         while (parent != null) {
             sbXPath.insert(0, parent.getAlias() + "/");
             parent = parent.getParentGroupAssignment();
         }
         sbXPath.insert(0, getAssignedType().getName() + "/");
         XPath = sbXPath.toString();
         return XPath;
     }
 
 
     /**
      * Get the multiplicity of this assignment.
      * Depending on if the assigned element allows overriding of its base multiplicity the base
      * elements multiplicity is returned or the multiplicity of the assignment
      *
      * @return multiplicity of this assignment
      */
     public abstract FxMultiplicity getMultiplicity();
 
     /**
      * Get the default multiplicity (used i.e. in user interfaces editors and determines the amount of values that will
      * be initialized when creating an empty element).
      * <p/>
      * If the set value is &lt; min or &gt; max multiplicity of this assignment it will
      * be auto adjusted to the next valid value without throwing an exception
      *
      * @return default multiplicity
      */
     public int getDefaultMultiplicity() {
         FxMultiplicity m = this.getMultiplicity();
         if (m.isValid(defaultMultiplicity))
             return defaultMultiplicity;
         if (defaultMultiplicity < m.getMin())
             return m.getMin();
         if (defaultMultiplicity > m.getMax())
             return m.getMax();
         return m.getMin();
     }
 
     /**
      * Get the position within the current XPath hierarchy
      *
      * @return position within the current XPath hierarchy
      */
     public int getPosition() {
         return position;
     }
 
     /**
      * Get the assignment label
      *
      * @return the localized label of this assignment
      */
     public FxString getLabel() {
         return label;
     }
 
     /**
      * Return a human-readable string to present this assignment to the user.
      *
      * @return the assignment's name as it should be displayed to the user
      */
     public String getDisplayName() {
         return getDisplayLabel().getBestTranslation();
     }
 
     /**
      * Return a human-readable string to present this assignment to the user.
      *
      * @return the assignment's name as it should be displayed to the user
      */
     public FxString getDisplayLabel() {
         if (label != null && !label.isEmpty()) {
             return label;
         } else if (assignedType != null && assignedType.getDescription() != null
                 && !assignedType.getDescription().isEmpty()) {
             return assignedType.getDescription();
         } else {
             return new FxString(alias);
         }
     }
 
     /**
      * Get the optional hint
      *
      * @return hint
      */
     public FxString getHint() {
         return hint;
     }
 
     /**
      * Check if an option is set for the requested key
      *
      * @param key option key
      * @return if an option is set for the requested key
      */
     public boolean hasOption(String key) {
         return FxStructureOption.hasOption(key, options);
     }
 
     /**
      * Get an option entry for the given key, if the key is invalid or not found a <code>FxStructureOption</code> object
      * will be returned with <code>set</code> set to <code>false</code>, overrideable set to <code>false</code> and value
      * set to an empty String.
      *
      * @param key option key
      * @return the found option or an object that indicates that the option is not set
      */
     public FxStructureOption getOption(String key) {
         return FxStructureOption.getOption(key, options);
     }
 
     /**
      * Get a (unmodifiable) list of all options set for this assignment
      *
      * @return (unmodifiable) list of all options set for this assignment
      */
     public List<FxStructureOption> getOptions() {
         return FxStructureOption.getUnmodifieableOptions(options);
     }
 
     /**
      * Does this assignment have mappings for the requested script event?
      *
      * @param event requested script event
      * @return if mappings exist
      */
     public boolean hasScriptMapping(FxScriptEvent event) {
         return scriptMapping.get(event) != null && scriptMapping.get(event).length > 0;
     }
 
     /**
      * Does this assignment have mappings for any script type?
      *
      * @return if mappings exist
      */
     public boolean hasScriptMappings() {
         //scriptMapping can be null here (and only here!) during 1st type resolve phase when building the environment 
         return scriptMapping != null && scriptMapping.size() > 0;
     }
 
     /**
      * Get the mapped script ids for the requested script type
      *
      * @param event requested script event
      * @return mapped script ids or <code>null</code> if mappings do not exist for this assignment
      */
     public long[] getScriptMapping(FxScriptEvent event) {
        long[] ret = scriptMapping.get(event);
         return ret != null ? ret : EMPTY_SCRIPTMAPPING;
     }
 
     /**
      * Create an empty FxData entry for this assignment
      *
      * @param parent the parent group
      * @param index  the index of the new entry
      * @return FxData
      * @throws FxCreateException on errors
      */
     public abstract FxData createEmptyData(FxGroupData parent, int index) throws FxCreateException;
 
     /**
      * Create a random FxData entry for this assignment
      *
      * @param rnd             the Random to use
      * @param env             environment
      * @param parent          the parent group
      * @param index           the index of the new entry
      * @param maxMultiplicity the maximum multiplicity
      * @return FxData
      * @throws FxCreateException on errors
      */
     public abstract FxData createRandomData(Random rnd, FxEnvironment env, FxGroupData parent, int index, int maxMultiplicity) throws FxCreateException;
 
     /**
      * Resolve preload dependecies after initial loading
      *
      * @param assignments all known assignment
      */
     public void resolvePreloadDependencies(List<FxAssignment> assignments) {
         if (parentGroupAssignment != null && parentGroupAssignment.getAlias() == null) {
             if (parentGroupAssignment.getId() == FxAssignment.NO_PARENT)
                 parentGroupAssignment = null;
             else {
                 for (FxAssignment as : assignments)
                     if (as instanceof FxGroupAssignment && as.getId() == parentGroupAssignment.getId()) {
                         parentGroupAssignment = (FxGroupAssignment) as;
                         return;
                     }
                 parentGroupAssignment = null; //if we reach this, the parent assignment could not be found => clear it
             }
         }
     }
 
 
     /**
      * Resolve references after initial loading
      *
      * @param environment environment for references
      * @throws FxNotFoundException on errors
      */
     public void resolveReferences(FxEnvironment environment) throws FxNotFoundException {
         List<Long> scripts = new ArrayList<Long>(10);
         for (FxScriptMapping sm : environment.getScriptMappings()) {
             for (FxScriptMappingEntry sme : sm.getMappedAssignments()) {
                 if (!sme.isActive())
                     continue;
                 if (sme.getId() == this.getId() && !scripts.contains(sm.getScriptId())) {
                     scripts.add(sm.getScriptId());
                 } else if (sme.isDerivedUsage()) {
                     for (long l : sme.getDerivedIds())
                         if (l == this.getId() && !scripts.contains(sm.getScriptId()))
                             scripts.add(sm.getScriptId());
                 }
             }
         }
         if (this.scriptMapping != null)
             this.scriptMapping.clear();
         else
             this.scriptMapping = new HashMap<FxScriptEvent, long[]>(5);
         for (long scriptId : scripts) {
             FxScriptEvent event = environment.getScript(scriptId).getEvent();
             if (this.scriptMapping.get(event) == null)
                 this.scriptMapping.put(event, new long[]{scriptId});
             else {
                 long[] types = this.scriptMapping.get(event);
                 long[] ntypes = new long[types.length + 1];
                 System.arraycopy(types, 0, ntypes, 0, types.length);
                 ntypes[ntypes.length - 1] = scriptId;
                 this.scriptMapping.put(event, ntypes);
             }
         }
         //if the type has been updated, we need to replace the assigned with the most up-to-date one
         this.assignedType = environment.getType(this.assignedType.getId());
     }
 
     /**
      * Resolve parent dependecies after initial loading
      *
      * @param assignments all known assignments
      */
     public void resolveParentDependencies(List<FxAssignment> assignments) {
         if (!(this instanceof FxGroupAssignment))
             return;
         for (FxAssignment a : assignments)
             if (a.hasParentGroupAssignment() && a.getParentGroupAssignment().getId() == this.getId())
                 ((FxGroupAssignment) this).addAssignment(a);
     }
 
     /**
      * Compare function
      *
      * @param o other assignment to compare to
      * @return compare result
      */
     public int compareTo(FxAssignment o) {
         if (this.position < o.position)
             return -1;
         if (this.position == o.position)
             return 0;
         return 1;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object obj) {
         return obj instanceof FxAssignment && this.getId() == ((FxAssignment) obj).getId();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
         return (int) getId();
     }
 
     /**
      * Sort FxAssignments by their position
      *
      * @param assignments FxAssignments to sort
      * @return sorted List with the assignments
      */
     public static List<FxAssignment> sort(List<FxAssignment> assignments) {
         Collections.sort(assignments);
         return assignments;
     }
 
 
     /**
      * Validate the given value for this assignment.
      *
      * @param value the value to be checked
      * @return true if it is valid for this assignment, false otherwise
      */
     public boolean isValid(Object value) {
         return true;
     }
 
     /**
      * Is this a system internal assignment?
      *
      * @return system internal
      */
     public boolean isSystemInternal() {
         return systemInternal;
     }
 
     /**
      * Mark this assignment as system internal - this is a one-way INTERNAL function!!!
      *
      * @return this
      */
     public FxAssignment _setSystemInternal() {
         this.systemInternal = true;
         return this;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return this.getXPath();
     }
 }
