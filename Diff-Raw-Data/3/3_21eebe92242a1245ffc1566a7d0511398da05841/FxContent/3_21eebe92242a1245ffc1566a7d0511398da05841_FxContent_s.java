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
 package com.flexive.shared.content;
 
 import com.flexive.shared.*;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.security.LifeCycleInfo;
 import com.flexive.shared.security.PermissionSet;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.*;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.StringUtils;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * A content instance
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxContent implements Serializable {
 
     private static final long serialVersionUID = -7014370829966212118L;
 
     /**
      * Topmost position for relation positioning
      */
     public final static int POSITION_TOP = Integer.MIN_VALUE;
 
     /**
      * Bottommost position for relation positioning
      */
     public final static int POSITION_BOTTOM = Integer.MAX_VALUE;
     private FxPK pk;
     private long typeId;
     private long mandatorId;
     private final List<Long> aclIds = Lists.newArrayListWithExpectedSize(5);
     private long stepId;
     private int maxVersion;
     private int liveVersion;
     private long mainLanguage;
     private FxLock lock;
 
     private boolean active;
 
     private int initialLiveVersion;
     private boolean relation;
     private FxPK relatedSource;
     private FxPK relatedDestination;
     private int relatedSourcePosition;
 
     private int relatedDestinationPosition;
 
     private LifeCycleInfo lifeCycleInfo;
 
     private FxGroupData data;
     private long binaryPreviewId;
     private long binaryPreviewACL;
 
     private volatile boolean captionResolved = false;
     private volatile boolean hasCaption = false;
     private volatile FxString caption = null;
 
     /**
      * Constructor
      *
      * @param pk                         primary key
      * @param lock                       lock of this content, if not locked a lock with type <code>FxLockType.None</code> will be set
      * @param typeId                     used type id
      * @param relation                   is this a content for a relation?
      * @param mandatorId                 mandator id
      * @param aclId                      ACL id
      * @param stepId                     step id
      * @param maxVersion                 max. version for this instance
      * @param liveVersion                live version for this instance (0=no live version exists)
      * @param active                     is this instance active
      * @param mainLanguage               main language
      * @param relatedSource              related source instance (only if this is a relation)
      * @param relatedDestination         related destination instance (only if this is a relation)
      * @param relatedSourcePosition      position for source instance (only if this is a relation)
      * @param relatedDestinationPosition position for destination instance (only if this is a relation)
      * @param lifeCycleInfo              lifecycle
      * @param data                       data
      * @param binaryPreviewId            id of the preview binary
      * @param binaryPreviewACL           id of the ACL of the preview binary
      */
     public FxContent(FxPK pk, FxLock lock, long typeId, boolean relation, long mandatorId, long aclId, long stepId, int maxVersion, int liveVersion,
                      boolean active, long mainLanguage, FxPK relatedSource, FxPK relatedDestination, int relatedSourcePosition,
                      int relatedDestinationPosition, LifeCycleInfo lifeCycleInfo, FxGroupData data, long binaryPreviewId,
                      long binaryPreviewACL) {
         this.pk = pk;
         this.lock = lock;
         this.typeId = typeId;
         this.relation = relation;
         this.mandatorId = mandatorId;
         if (aclId != -1) {
             this.aclIds.add(aclId);
         }
         this.stepId = stepId;
         this.maxVersion = maxVersion;
         this.liveVersion = liveVersion;
         this.initialLiveVersion = liveVersion;
         this.mainLanguage = mainLanguage;
         this.relatedSource = relatedSource;
         this.relatedDestination = relatedDestination;
         this.relatedSourcePosition = relatedSourcePosition;
         this.relatedDestinationPosition = relatedDestinationPosition;
         this.lifeCycleInfo = lifeCycleInfo;
         this.data = data;
         this.active = active;
         this.binaryPreviewId = binaryPreviewId;
         this.binaryPreviewACL = binaryPreviewACL;
         updateSystemInternalProperties();
     }
 
     /**
      * Getter for the primary key
      *
      * @return primary key
      */
     public FxPK getPk() {
         return pk;
     }
 
     /**
      * Getter for the Id
      *
      * @return id
      */
     public long getId() {
         return pk.getId();
     }
 
     /**
      * Getter for the version
      *
      * @return version
      */
     public int getVersion() {
         return pk.getVersion();
     }
 
     /**
      * Getter for the type id
      *
      * @return type id
      */
     public long getTypeId() {
         return typeId;
     }
 
     /**
      * Getter for the mandator id
      *
      * @return mandator id
      */
     public long getMandatorId() {
         return mandatorId;
     }
 
     /**
      * Getter for the first ACL id
      *
      * @return ACL id
      * @deprecated use {@link #getAclIds()}
      */
     @Deprecated
     public long getAclId() {
         final List<Long> acls = getAclIds();
         return acls.isEmpty() ? -1 : acls.get(0);
     }
 
     /**
      * Set the content ACL id. If more than one ACL was assigned, the additional ACLs are removed
      * before assigning the new ACL.
      *
      * @param aclId the ACL id
      */
     public void setAclId(long aclId) {
         setAclIds(Arrays.asList(aclId));
     }
 
     /**
      * Return all ACLs assigned to this content.
      *
      * @return all ACLs assigned to this content.
      */
     public List<Long> getAclIds() {
         try {
             List<Long> ret = new ArrayList<Long>(aclIds.size());
             for (FxValue val : getValues("/ACL")) {
                 if (!(val instanceof FxLargeNumber))
                     continue;
                 //"reverse" the order since adding them in updateAclProperty() places them at the beginning
                 //resulting in a reverse order
                 ret.add(0, ((FxLargeNumber) val).getDefaultTranslation());
             }
             return Collections.unmodifiableList(ret);
         } catch (NullPointerException e) {
             return Collections.unmodifiableList(aclIds); //might happen during initialization of new contents
         }
     }
 
     /**
      * Sets the new ACL ids and performs necessary enviroment synchronisatons.
      *
      * @param aclIds ACL ids
      */
     public void setAclIds(Collection<Long> aclIds) {
         if (aclIds.isEmpty()) {
             throw new FxUpdateException("ex.content.noACL", pk).asRuntimeException();
         }
         // check if all ACLs are usable
         final FxEnvironment env = CacheAdmin.getEnvironment();
         for (Long id : aclIds) {
             final ACL acl = env.getACL(id);
             if (acl.getCategory() != ACLCategory.INSTANCE) {
                 throw new FxUpdateException(
                         "ex.content.invalidACLType", acl.getName(), acl.getCategory(), ACLCategory.INSTANCE
                 ).asRuntimeException();
             }
         }
         this.aclIds.clear();
         this.aclIds.addAll(aclIds);
         updateSystemInternalProperties();
     }
 
     /**
      * Getter for the step id
      *
      * @return step id
      */
     public long getStepId() {
         try {
             return ((FxLargeNumber) getValue("/STEP")).getDefaultTranslation();
         } catch (NullPointerException e) {
             return stepId; //might happen during initialization of new contents
         }
     }
 
     /**
      * Set the workflow step id
      *
      * @param stepId workflow step id
      */
     public void setStepId(long stepId) {
         //TODO: check if step is valid
         this.stepId = stepId;
         if (CacheAdmin.getEnvironment().getStep(stepId).isLiveStep())
             this.liveVersion = this.getPk().getVersion();
         else {
             if (pk.isNew())
                 this.liveVersion = 0;
             else
                 this.liveVersion = this.initialLiveVersion;
         }
         updateSystemInternalProperties();
     }
 
     /**
      * Set the workflow step using the given {@link StepDefinition step definition ID}.
      *
      * @param stepDefinitionId  the step definition ID
      * @return  true if the step was changed
      * @throws  FxRuntimeException  if the given step definition ID does
      * not exist for the content's workflow
      * @since   3.1
      */
     public boolean setStepByDefinition(long stepDefinitionId) {
         final FxEnvironment env = CacheAdmin.getEnvironment();
         final FxType type = env.getType(getTypeId());
         for (Step step : type.getWorkflow().getSteps()) {
             if (step.getStepDefinitionId() == stepDefinitionId) {
                 if (getStepId() == step.getId()) {
                     return false;   // no update necessary
                 } else {
                     setStepId(step.getId());
                     return true;
                 }
             }
         }
         throw new FxInvalidParameterException(
                 "stepDefinitionId",
                 "ex.content.step.definition.invalid",
                 stepDefinitionId, type.getWorkflow().getName()
         ).asRuntimeException();
     }
 
     /**
      * Get the max version of this content
      *
      * @return max version of this content
      */
     public int getMaxVersion() {
         return maxVersion;
     }
 
     /**
      * Is this content instance the max version
      *
      * @return if content instance the max version
      */
     public boolean isMaxVersion() {
         return pk.getVersion() == maxVersion || pk.isNew();
     }
 
     /**
      * Get the live version of this content or 0 if no live version exists
      *
      * @return live version of this content or 0 if no live version exists
      */
     public int getLiveVersion() {
         return liveVersion;
     }
 
     /**
      * Is this content instance the live version
      *
      * @return if content instance the live version
      */
     public boolean isLiveVersion() {
         return pk.getVersion() == liveVersion || (pk.isNew() && liveVersion == 1);
     }
 
     /**
      * Checks if the given PK matches the content PK. This method allows to match PKs with generic
      * version information (including {@link FxPK#LIVE} or {@link FxPK#MAX}), which the
      * {@link FxPK#equals(Object)} FxPK equals method cannot do.
      *
      * @param otherPk the PK to be matched
      * @return true if otherPk matches this content
      */
     public boolean matchesPk(FxPK otherPk) {
         return pk.equals(otherPk) || (pk.getId() == otherPk.getId()
                 && ((otherPk.getVersion() == FxPK.MAX && isMaxVersion()) || (otherPk.getVersion() == FxPK.LIVE && isLiveVersion())));
     }
 
     /**
      * Get the main language
      *
      * @return main language
      */
     public long getMainLanguage() {
         try {
             return ((FxLargeNumber) getValue("/MAINLANG")).getDefaultTranslation();
         } catch (NullPointerException e) {
             return mainLanguage; //might happen during initialization of new contents
         }
     }
 
     /**
      * Set the main language
      *
      * @param mainLanguage main language
      */
     public void setMainLanguage(long mainLanguage) {
         this.mainLanguage = mainLanguage;
         updateSystemInternalProperties();
     }
 
     /**
      * Is this content active?
      *
      * @return content is active
      */
     public boolean isActive() {
         try {
             return ((FxBoolean) getValue("/ISACTIVE")).getDefaultTranslation();
         } catch (NullPointerException e) {
             return active; //might happen during initialization of new contents
         }
     }
 
     /**
      * (De-)activate this content
      *
      * @param active active flag
      */
     public void setActive(boolean active) {
         this.active = active;
         updateSystemInternalProperties();
     }
 
     /**
      * Is this content a relation?
      *
      * @return content is relation
      */
     public boolean isRelation() {
         return relation;
     }
 
     /**
      * If this is a relation get the assigned "from" (or source) instance
      *
      * @return the assigned "from" (or source) instance
      */
     public FxPK getRelatedSource() {
         return relatedSource;
     }
 
     /**
      * Set the primary key of the source relation
      *
      * @param src source relation
      * @return this
      */
     public FxContent setRelatedSource(FxPK src) {
         this.relatedSource = src;
         return this;
     }
 
     /**
      * If this is a relation get the assigned "to" (or destination) instance
      *
      * @return the assigned "to" (or destination) instance
      */
     public FxPK getRelatedDestination() {
         return relatedDestination;
     }
 
     /**
      * Set the primary key of the destination relation
      *
      * @param dst destination relation
      * @return this
      */
     public FxContent setRelatedDestination(FxPK dst) {
         this.relatedDestination = dst;
         return this;
     }
 
     /**
      * Get the position for the source content instance
      *
      * @return position for the source content instance
      */
     public int getRelatedSourcePosition() {
         return relatedSourcePosition;
     }
 
 
     /**
      * Get the position for the destination content instance
      *
      * @return position for the destination content instance
      */
     public int getRelatedDestinationPosition() {
         return relatedDestinationPosition;
     }
 
     public void setRelatedDestinationPosition(int relatedDestinationPosition) {
         this.relatedDestinationPosition = relatedDestinationPosition;
     }
 
     public void setRelatedSourcePosition(int relatedSourcePosition) {
         this.relatedSourcePosition = relatedSourcePosition;
     }
 
     /**
      * Get the lifecycle information
      *
      * @return lifecycle information
      */
     public LifeCycleInfo getLifeCycleInfo() {
         return lifeCycleInfo;
     }
 
     /**
      * Is this content locked?
      *
      * @return if this content is locked
      */
     public boolean isLocked() {
         return lock.getLockType() != FxLockType.None;
     }
 
     /**
      * Get the lock of this content
      *
      * @return lock of this content
      */
     public FxLock getLock() {
         return lock;
     }
 
     /**
      * Get all FxData (Group or Property) entries for the given XPath.
      * <p/>
      * Note: If the XPath refers to a group, only its child entries are returned
      * and not the FxData of the group itsself. For accessing the group data itself
      * use {@link #getGroupData(String)} instead.
      *
      * @param XPath requested XPath
      * @return FxData elements for the given XPath
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public List<FxData> getData(String XPath) {
         List<FxData> base = data.getChildren();
         if (StringUtils.isEmpty(XPath) || "/".equals(XPath))
             return base;
         List<FxData> ret = base;
         boolean found;
         for (XPathElement xpe : XPathElement.split(XPath.toUpperCase())) {
             found = false;
             for (FxData curr : ret) {
                 if (curr.getXPathElement().equals(xpe)) {
                     if (curr.isProperty()) {
                         ret = new ArrayList<FxData>(1);
                         ret.add(curr);
                         return ret;
                     } else {
                         ret = ((FxGroupData) curr).getChildren();
                         found = true;
                         break;
                     }
                 }
             }
             if (!found)
                 throw new FxNotFoundException("ex.content.xpath.notFound", XPath).asRuntimeException();
         }
         return ret;
     }
 
     /**
      * Get the FxPropertyData entry for the given XPath
      *
      * @param XPath requested XPath
      * @return FxPropertyData entry for the given XPath
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public FxPropertyData getPropertyData(String XPath) {
         FxSharedUtils.checkParameterEmpty(XPath, "XPATH");
         XPath = XPathElement.stripType(XPath);
         List<FxData> found = getData(XPath);
         if (found.size() != 1 || !(found.get(0) instanceof FxPropertyData))
             throw new FxInvalidParameterException("XPATH", "ex.xpath.element.noProperty", XPath).asRuntimeException();
         return (FxPropertyData) found.get(0);
     }
 
     /**
      * Get a list of all FxPropertyData entries that are assigned to propertyId
      *
      * @param propertyId   the property id requested. -1 to return all properties.
      * @param includeEmpty include empty data instances?
      * @return list of all FxPropertyData entries that are assigned to propertyId
      */
     public List<FxPropertyData> getPropertyData(long propertyId, boolean includeEmpty) {
         return getRootGroup().getPropertyData(propertyId, includeEmpty);
     }
 
     /**
      * Get the FxGroupData entry for the given XPath
      *
      * @param XPath requested XPath
      * @return FxGroupData entry for the given XPath
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public FxGroupData getGroupData(String XPath) {
         FxSharedUtils.checkParameterEmpty(XPath, "XPATH");
         XPath = XPathElement.stripType(XPathElement.toXPathMult(XPath.toUpperCase()));
         //this is a slightly modified version of getData() but since groups may not contain children its safer
         if (StringUtils.isEmpty(XPath) || "/".equals(XPath))
             return getRootGroup();
         List<FxData> currChildren = data.getChildren();
         FxGroupData group = null;
         boolean found;
         for (XPathElement xpe : XPathElement.split(XPath)) {
             found = false;
             for (FxData curr : currChildren) {
                 if (curr.getXPathElement().equals(xpe)) {
                     if (curr.isProperty()) {
                         throw new FxInvalidParameterException("XPATH", "ex.xpath.element.noGroup", XPath).asRuntimeException();
                     } else {
                         currChildren = ((FxGroupData) curr).getChildren();
                         group = ((FxGroupData) curr);
                         found = true;
                         break;
                     }
                 }
             }
             if (!found)
                 throw new FxNotFoundException("ex.content.xpath.notFound", XPath).asRuntimeException();
         }
         return group;
     }
 
     /**
      * Get the FxGroupData entry for the given group assignment.
      *
      * @param assignmentId  the group assignment ID
      * @return FxGroupData  entry for the given assignment
      * @since 3.1.4
      */
     public FxGroupData getGroupData(long assignmentId) {
         return data.getGroup(assignmentId);
     }
 
 
     /**
      * Get the (virtual) root group of this content
      *
      * @return root group
      */
     public FxGroupData getRootGroup() {
         return this.data;
     }
 
     /**
      * Get the maximum index of the last element of an XPath.
      * The multiplicity of the last element is checked.
      * Lets say group and property (/group/property) each have a max. multiplicity > 1:
      * "/group/property" -> returns the maximum multiplicity of property in group[1]
      * "/group[2]/property" -> returns the maximum multiplicity of property in group[2]
      * "/group" -> returns the maximum multipliciy of group
      *
      * @param XPath xpath to get the maximum multiplicity for
      * @return maximum multiplicity of the xpath or <code>0</code> if it does not exist (yet)
      * @since 3.1.4
      */
     public int getMaxIndex(String XPath) {
         XPath = XPathElement.stripType(XPath);
         String xp = XPathElement.toXPathMult(XPath).toUpperCase(); //make sure all multiplicities are set
         XPathElement last = XPathElement.lastElement(xp);
         xp = XPathElement.stripLastElement(xp);
         if (!containsXPath(xp))
             return 0;
         int max = 0;
         for (FxData data : getData(xp)) {
             if (data.getXPathElement().getAlias().equals(last.getAlias())) {
                 if (max < data.getXPathElement().getIndex())
                     max = data.getXPathElement().getIndex();
             }
         }
         return max;
     }
 
     /**
      * Convenience method to compact (=remove all empty groups and properties that are not required).
      * This method is equivalent to calling <code>getRootGroup().compact()</code>.
      *
      * @return this
      * @since 3.1.4
      */
     public FxContent compact() {
         getRootGroup().compact();
         return this;
     }
 
     /**
      * Add a value for a XPath.
      * This method helps when setting values for properties with a multiplicity > 1 by adding new entries.
      * If e.g. /property[2] is the current property with the highest multiplicity, a /property[3] entry will be created.
      *
      * @param XPath XPath to add a value for
      * @param value FxValue to add
      * @return XPath of the added element
      * @since 3.1.4
      */
     public String addValue(String XPath, FxValue value) {
         XPath = XPathElement.stripType(XPath);
         if (containsXPath(XPath)) {
             if (getValue(XPath).isEmpty()) {
                 //set empty value first
                 setValue(XPath, value);
                 return XPathElement.toXPathMult(XPath);
             }
         }
         FxPropertyData pd = getPropertyDataCreate(XPathElement.buildXPath(true,
                 XPathElement.stripLastElement(XPath),
                 XPathElement.lastElement(XPath).getAlias() + "[" + (getMaxIndex(XPath) + 1) + "]"));
         pd.setValue(value);
         return pd.getXPathFull();
     }
 
     /**
      * Depending on the underlying FxValue's multilanguage setting set either the default
      * translation or the single language value.
      * This method helps when setting values for properties with a multiplicity > 1 by adding new entries.
      * If e.g. /property[2] is the current property with the highest multiplicity, a /property[3] entry will be created.
      *
      * @param XPath XPath to add a value for
      * @param value the value (has to match the FxValue's data type)
      * @return XPath of the added element
      * @since 3.1.4
      */
     @SuppressWarnings({"unchecked"})
     public String addValue(String XPath, Object value) {
         if (value instanceof FxValue)
             return this.addValue(XPath, (FxValue) value);
         XPath = XPathElement.stripType(XPath);
         String createdXPath;
         if (!containsValue(XPath)) {
             createXPath(XPath);
             createdXPath=XPathElement.toXPathMult(XPath);
         } else {
             FxPropertyData pd = getPropertyDataCreate(XPathElement.stripLastElement(XPath) + "/" + XPathElement.lastElement(XPath).getAlias() +
                 "[" + (getMaxIndex(XPath) + 1) + "]");
             createdXPath = pd.getXPathFull();
         }
         FxValue val = getPropertyData(createdXPath).getValue();
         if (val.isMultiLanguage())
             val.setDefaultTranslation(value);
         else
             val.setValue(value);
         return createdXPath;
     }
 
     /**
      * Add (or create) another group. The group to create is the last element in the requested XPath. 
      *
      * @param XPath XPath with the group alias as last element
      * @return XPath of the added group
      * @since 3.1.4
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public String addGroup(String XPath) {
         XPath = XPathElement.stripType(XPath);
         if (!isGroupXPath(XPath))
             throw new FxInvalidParameterException("XPATH", "ex.xpath.element.noGroup", XPath).asRuntimeException();
         int index = 1;
         if (containsXPath(XPath))
             index = getMaxIndex(XPath) + 1;
         String xpGroup = XPathElement.toXPathMult(XPath);
         xpGroup = XPathElement.buildXPath(true, XPathElement.stripLastElement(xpGroup), XPathElement.lastElement(XPath).getAlias() + "[" + index + "]");
 
         FxGroupAssignment ga = CacheAdmin.getEnvironment().getType(this.getTypeId()).getGroupAssignment(xpGroup);
         try {
             getRootGroup().addGroup(xpGroup, ga, FxData.POSITION_BOTTOM);
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
         return xpGroup;
     }
 
     /**
      * Get the FxPropertyData entry for an XPath and create a new entry if it does not exist yet.
      *
      * @param XPath requested xpath
      * @return FxPropertyData
      * @since 3.1
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public FxPropertyData getPropertyDataCreate(String XPath) {
         XPath = XPathElement.stripType(XPath);
         createXPath(XPath);
         List<FxData> prop = getData(XPath);
         if (prop.size() != 1)
             throw new FxInvalidParameterException("XPATH", "ex.xpath.element.ambiguous.property", XPath).asRuntimeException();
         if (!(prop.get(0) instanceof FxPropertyData))
             throw new FxInvalidParameterException("XPATH", "ex.xpath.element.noProperty", XPath).asRuntimeException();
         return ((FxPropertyData) prop.get(0));
     }
 
     /**
      * Set a properties value, needed groups will be created
      *
      * @param XPath FQ XPath
      * @param value value to apply
      * @return this FxContent instance to allow chained calls
      */
     public FxContent setValue(String XPath, FxValue value) {
         getPropertyDataCreate(XPath).setValue(value);
         return this;
     }
 
     /**
      * Depending on the underlying FxValue's multilanguage setting set either the default
      * translation or the single language value
      *
      * @param XPath requested XPath
      * @param value the value (has to match the FxValue's data type)
      * @return this FxContent instance to allow chained calls
      * @since 3.0.2
      */
     @SuppressWarnings({"unchecked"})
     public FxContent setValue(String XPath, Object value) {
         if (value instanceof FxValue)
             return this.setValue(XPath, (FxValue) value);
         XPath = XPathElement.stripType(XPath);
         if (!containsValue(XPath))
             createXPath(XPath);
         FxValue val = getPropertyData(XPath).getValue();
         if (val.isMultiLanguage())
             val.setDefaultTranslation(value);
         else
             val.setValue(value);
         return this;
     }
 
     /**
      * Depending on the underlying FxValue's multilanguage setting set either the
      * translation in the requested language or the single language value
      *
      * @param XPath      requested XPath
      * @param languageId requested language (ignored if single value)
      * @param value      the value (has to match the FxValue's data type)
      * @return this FxContent instance to allow chained calls
      * @since 3.0.2
      */
     @SuppressWarnings({"unchecked"})
     public FxContent setValue(String XPath, long languageId, Object value) {
         FxValue val = getPropertyData(XPath).getValue();
         if (val.isMultiLanguage())
             val.setTranslation(languageId, value);
         else
             val.setValue(value);
         return this;
     }
 
     /**
      * Convenience method which saves this FxContent and returns the loaded instance.
      *
      * @return saved FxContent
      * @throws FxApplicationException on errors
      * @since 3.0.2
      */
     public FxContent save() throws FxApplicationException {
         final ContentEngine ce = EJBLookup.getContentEngine();
         return ce.load(ce.save(this));
     }
 
     /**
      * Convenience method which saves this FxContent in a new version and returns the loaded instance.
      *
      * @return saved FxContent
      * @throws FxApplicationException on errors
      * @since 3.1
      */
     public FxContent saveNewVersion() throws FxApplicationException {
         final ContentEngine ce = EJBLookup.getContentEngine();
         return ce.load(ce.createNewVersion(this));
     }
 
     /**
      * Create (if possible and not already exists) the given XPath which has to point to a property
      *
      * @param XPath the XPath to create
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     private void createXPath(String XPath) {
         FxEnvironment env = CacheAdmin.getEnvironment();
         if (!env.getType(this.getTypeId()).isXPathValid(XPath, true))
             throw new FxInvalidParameterException("XPATH", "ex.content.xpath.set.invalid", XPath, env.getType(getTypeId()).getName()).asRuntimeException();
         XPath = XPath.toUpperCase();
         List<XPathElement> elements = XPathElement.split(XPath);
         FxGroupData currGroup = this.getRootGroup();
         boolean found;
         List<XPathElement> missing = new ArrayList<XPathElement>(20);
         for (int i = 0; i < elements.size(); i++) {
             found = false;
             missing.clear();
             for (int m = 1; m < elements.get(i).getIndex(); m++)
                 missing.add(new XPathElement(elements.get(i).getAlias(), m, true));
             for (FxData currData : currGroup.getChildren()) {
                 if (currData.getXPathElement().equals(elements.get(i))) {
                     if (currData instanceof FxPropertyData)
                         return; //last element reached and it exists
                     found = true;
                     currGroup = (FxGroupData) currData;
                     break;
                 } else if (missing.contains(currData.getXPathElement())) {
                     missing.remove(currData.getXPathElement());
                 }
             }
             if (found)
                 continue;
             if (missing.size() > 0) {
                 List<XPathElement> missingPath = new ArrayList<XPathElement>(i + 1);
                 missingPath.addAll(elements.subList(0, i));
                 for (XPathElement currMissing : missing) {
                     missingPath.add(currMissing);
 //                    System.out.println("Creating missing: "+XPathElement.toXPath(missingPath));
                     currGroup.addEmptyChild(XPathElement.toXPath(missingPath), FxData.POSITION_BOTTOM);
                     missingPath.remove(missingPath.size() - 1);
                 }
             }
             //create the group or property
 //            System.out.println("Creating: "+XPathElement.toXPath(elements.subList(0, i+1)));
             FxData added = currGroup.addEmptyChild(XPathElement.toXPath(elements.subList(0, i + 1)), FxData.POSITION_BOTTOM);
             if (added instanceof FxGroupData)
                 currGroup = (FxGroupData) added;
         }
     }
 
     /**
      * Get the value of a (property) XPath.
      * This is actually a convenience method that internally calls <code>getPropertyData(XPath).getValue()</code>.
      * If the XPath is valid but no value is set, <code>null</code> will be returned
      *
      * @param XPath requested XPath
      * @return FxValue or <code>null</code> if no value is set
      * @see #getPropertyData(String)
      */
     public FxValue getValue(String XPath) {
         try {
             return getPropertyData(XPath).getValue();
         } catch (FxRuntimeException e) {
             if (isPropertyXPath(XPath))
                 return null; //just not set, see FX-473
             throw e;
         }
     }
 
     /**
      * Get the value of a given class (eg FxString) and (property) XPath.
      * This is actually a convenience method that internally calls <code>getPropertyData(XPath).getValue()</code> and
      * casts the value to the requested class.
      * If the XPath is valid but no value is set or the value is not of the requested class,
      * <code>null</code> will be returned
      *
      * @param clazz return only instances of this class (eg FxString.class)
      * @param XPath requested XPath
      * @return FxValue or <code>null</code> if no value is set or not of the requested class
      * @see #getValue(String)
      * @since 3.1.4
      */
     public <T extends FxValue> T getValue(Class<T> clazz, String XPath) {
         FxValue value = getValue(XPath);
         if (value != null && clazz.isInstance(value))
             return clazz.cast(value);
         else
             return null;
     }
 
     /**
      * Get all values of a given XPath, ordered by multiplicty.
      * If the assignment has a max. multiplicity of 1 return a list with a single entry
      *
      * @param XPath requested XPath
      * @return all values of a given XPath, ordered by multiplicty
      * @since 3.1
      */
     public List<FxValue> getValues(String XPath) {
         // XPath check can be skipped because it is performed by FxType#getAssignment below
         //if (!isXPathValid(XPath, true))
         //noinspection ThrowableInstanceNeverThrown
         //   throw new FxInvalidParameterException("XPATH", "ex.xpath.element.noProperty", XPath).asRuntimeException();
         final FxEnvironment env = CacheAdmin.getEnvironment();
         long assignmentId = env.getType(getTypeId()).getAssignment(XPath).getId();
         FxGroupData group = getGroupData(XPathElement.stripLastElement(XPath));
         List<FxPropertyData> values = new ArrayList<FxPropertyData>(10);
         for (FxData data : group.getChildren()) {
             if (data.isGroup())
                 continue;
             if (data.getAssignmentId() == assignmentId)
                 values.add((FxPropertyData)data);
         }
         Collections.sort(values, new Comparator<FxPropertyData>() {
            @Override
             public int compare(FxPropertyData o1, FxPropertyData o2) {
                 return ((Integer)o1.getIndex()).compareTo(o2.getIndex());
             }
         });
         List<FxValue> result = new ArrayList<FxValue>(values.size());
         for (FxPropertyData data : values)
             result.add(data.getValue());
         return result;
     }
 
     /**
      * Get all values of a given class (eg FxString) and XPath, ordered by multiplicty.
      * If the assignment has a max. multiplicity of 1 return a list with a single entry
      *
      * @param clazz collect only instances of this class (eg FxString.class)
      * @param XPath requested XPath
      * @return all values of a given XPath, ordered by multiplicty
      * @since 3.1.4
      */
     public <T extends FxValue> List<T> getValues(Class<T> clazz, String XPath) {
         final FxEnvironment env = CacheAdmin.getEnvironment();
         long assignmentId = env.getType(getTypeId()).getAssignment(XPath).getId();
         FxGroupData group = getGroupData(XPathElement.stripLastElement(XPath));
         List<FxPropertyData> values = new ArrayList<FxPropertyData>(10);
         for (FxData data : group.getChildren()) {
             if (data.isGroup())
                 continue;
             if (data.getAssignmentId() == assignmentId) {
                 if( clazz.isInstance(((FxPropertyData) data).getValue()))
                     values.add((FxPropertyData)data);
             }
         }
         Collections.sort(values, new Comparator<FxPropertyData>() {
            @Override
             public int compare(FxPropertyData o1, FxPropertyData o2) {
                 return ((Integer)o1.getIndex()).compareTo(o2.getIndex());
             }
         });
         List<T> result = new ArrayList<T>(values.size());
         for (FxPropertyData data : values)
             result.add(clazz.cast(data.getValue()));
         return result;
     }
 
     /**
      * Check if a value exists for the given XPath that is not empty
      *
      * @param XPath the XPath to check
      * @return if a value exists for the given XPath that is not empty
      */
     public boolean containsValue(String XPath) {
         final FxValue value = getValue(XPath);
         return value != null && !value.isEmpty();
     }
 
     /**
      * Check if a XPath exists in the content instance. The XPath may refer either to a property
      * or a group.
      *
      * @param XPath the XPath to check
      * @return if a value exists for the given XPath that is not empty
      * @since 3.1
      */
     public boolean containsXPath(String XPath) {
         try {
             if (getValue(XPath) != null) {
                 return true;
             }
         } catch (FxRuntimeException e) {
             // ignore, XPath exists but doesn't point to a property
         }
         try {
             getGroupData(XPath);
             return true;
         } catch (FxRuntimeException e1) {
             return false;
         }
     }
 
     /**
      * Check if the given XPath is valid for this content.
      * This is a shortcut to the corresponding type's method!
      *
      * @param XPath         the XPath to check
      * @param checkProperty should the XPath point to a property?
      * @return if the XPath is valid or not
      * @see FxType#isXPathValid(String,boolean)
      * @deprecated use #isGroupXPath(String or #isPropertyXPath(String) instead
      */
     public boolean isXPathValid(String XPath, boolean checkProperty) {
         return CacheAdmin.getEnvironment().getType(this.getTypeId()).isXPathValid(XPath, checkProperty);
     }
 
     /**
      * Check if the given XPath is a valid group XPath for this content.
      * This is a shortcut to the corresponding type's method!
      *
      * @param XPath         the XPath to check
      * @return if the XPath is valid or not
      * @see FxType#isXPathValid(String,boolean)
      * @since 3.1.4
      */
     public boolean isGroupXPath(String XPath) {
         return CacheAdmin.getEnvironment().getType(this.getTypeId()).isXPathValid(XPath, false);
     }
 
     /**
      * Check if the given XPath is a valid property XPath for this content.
      * This is a shortcut to the corresponding type's method!
      *
      * @param XPath         the XPath to check
      * @return if the XPath is valid or not
      * @see FxType#isXPathValid(String,boolean)
      * @since 3.1.4
      */
     public boolean isPropertyXPath(String XPath) {
         return CacheAdmin.getEnvironment().getType(this.getTypeId()).isXPathValid(XPath, true);
     }
 
     /**
      * Drop all data and create random entries with a maximum multiplicity for testing purposes
      *
      * @param maxMultiplicity the maximum multiplicity for groups
      * @return this
      */
     public FxContent randomize(int maxMultiplicity) {
         Random r = new Random();
         FxEnvironment env = CacheAdmin.getEnvironment();
         this.data = env.getType(this.getTypeId()).createRandomData(pk, env, r, maxMultiplicity);
         initSystemProperties();
         return this;
     }
 
     /**
      * Drop all data and create random entries for testing purposes
      *
      * @return this
      * @throws FxCreateException           on errors
      * @throws FxNotFoundException         on errors
      * @throws FxInvalidParameterException on errors
      */
     public FxContent randomize() throws FxCreateException, FxNotFoundException, FxInvalidParameterException {
         return randomize(FxMultiplicity.RANDOM_MAX);
     }
 
     /**
      * Move data (group or property) within its hierarchy for <code>delta</code>
      * positions up or down depending on the sign of <code>delta</code> without wrapping
      * around if top or bottom position is reached.
      * If delta is Integer.MAX_VALUE the data will always be placed at the bottom,
      * Integer.MIN_VALUE will always place it at the top.
      *
      * @param XPath FQ XPath
      * @param delta relative number of positions to move
      * @throws FxInvalidParameterException for invalid XPath
      * @throws FxNotFoundException         XPath does not exist for this content
      */
     public void move(String XPath, int delta) throws FxInvalidParameterException, FxNotFoundException {
         if (delta == 0 || StringUtils.isEmpty(XPath))
             return; //nothing to do
         XPath = XPathElement.stripType(XPath);
         if ("/".equals(XPath))
             return; //nothing to do
 
         FxGroupData parent;
         if (isGroupXPath(XPath)) {
             //group
             parent = getGroupData(XPath);
             if (!parent.isRootGroup())
                 parent = parent.getParent();
         } else {
             //property
             List<FxData> mdata = getData(XPath);
             parent = mdata.get(0).getParent();
         }
         XPathElement last = XPathElement.lastElement(XPath);
         parent.moveChild(last, delta);
     }
 
     /**
      * Sorts the current data (=root data) according to the order of the type
      *
      * @since 3.1.1
      */
     public void sortData() {
         FxPropertySorter.getSorterForType(typeId).applyOrder(data);
     }
 
     /**
      * Remove the property or group denoted by XPath
      *
      * @param XPath the XPath to remove
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public void remove(String XPath) {
         FxSharedUtils.checkParameterEmpty(XPath, "XPATH");
         XPath = XPathElement.stripType(XPath);
         FxData data = null;
         final XPathElement lastElement = XPathElement.lastElement(XPath);
         if (!lastElement.isIndexDefined()) {
             //remove all
             String parentGroup = XPathElement.stripLastElement(XPath);
             data = getGroupData(parentGroup);
             List<FxData> remove = new ArrayList<FxData>(10);
             for (FxData child : ((FxGroupData) data).getChildren())
                 if (child.getAlias().equals(lastElement.getAlias()))
                     remove.add(child);
             ((FxGroupData) data).removeChildren(remove);
             return;
         }
         XPath = XPathElement.toXPathMult(XPath);
         List<FxData> found = getData(XPath);
         if (found.size() == 1) {
             if (found.get(0).getXPathFull().equals(XPath))
                 data = found.get(0); //property
         }
         //getData(String XPath) returns empty list for empty groups
         if (data == null && found.isEmpty()) {
             //fetch group
             data = getGroupData(XPath);
         }
         if (data == null && found.get(0).getParent() != null && found.get(0).getParent().getXPathFull().equals(XPath))
             data = found.get(0).getParent(); //group with single or multiple properties->get parent
         if (data == null || data.getParent() == null)
             throw new FxNoAccessException("ex.content.xpath.remove.invalid", XPath).asRuntimeException();
 
         data.getParent().removeChild(data);
     }
 
     /**
      * Check if all required properties are present and valid, etc.
      *
      * @throws FxInvalidParameterException if required properties are not present or the content is not valid
      */
     public void checkValidity() throws FxInvalidParameterException {
 //        _checkGroupValidity(data);
             checkGroupValidityNonRec(data);
     }
 
     /**
      * check a group and its properties and subgroups if required properties are present
      *
      * @param data FxGroupData to check
      * @throws FxInvalidParameterException if required properties are not present
      * @since 3.1.4
      */
     private void checkGroupValidityNonRec(FxGroupData data) throws FxInvalidParameterException {
         if (data.getAssignmentMultiplicity().isOptional() && data.isEmpty())
             return; //if optional groups have required properties or subgroups it still is ok if they are empty!
 
 //        long start = System.nanoTime();
         final FxEnvironment env = CacheAdmin.getEnvironment();
         final FxType currentType = env.getType(typeId);
         final int len = currentType.getName().length();
 
         //  xpath, multiplicity        
         Map<String, Integer> curMults = new HashMap<String, Integer>();
 
         // only groups
         Set<String> processedXpaths = new HashSet<String>();
 
         // afterwards create a (linked) list with all groups of the current data-root and do as before instead of recursive checking
         List<FxGroupData> allData = new LinkedList<FxGroupData>();
         allData.add(data);
         String currentXPath = "";
 
         while (allData.size() > 0) {
             FxGroupData currData = allData.remove(0);
             currentXPath = currData.getXPath();
             // we just need keep track of the current group
             curMults.clear();
             for (FxData curr : currData.getChildren()) {
                 final String xPath = curr.getXPath();
                 // so if the current XPath is known we increase the counter otherwise we set it to 1
                 Integer tmp = curMults.get(xPath);
                 if (tmp == null){
                     tmp = 0;
                     curMults.put(xPath, 1);
                 }
                 else curMults.put(xPath, tmp + 1);
                 // if the property or group is empty we need to undo the increment of current multiplicities
                 if (curr.isEmpty()) {
                     if (curr instanceof FxGroupData) {
                         if (curr.getAssignmentMultiplicity().isOptional()) {
                             curMults.put(xPath, tmp);
                         }
                     } else {
                         curMults.put(xPath, tmp);
                     }
                 } else {
                     // if we got a group we add it to the list so that it is checked later (only if it is not empty)
                     if (curr instanceof FxGroupData) {
                         allData.add((FxGroupData) curr);
                     } else {
                         ((FxPropertyData) curr).checkMaxLength();
                     }
                 }
             }
             // build / get the needed group (according to the currentXpath)
             List<FxAssignment> curList;
             if (currentXPath.length() == 1) { // build the root group containing groups and properties
                 curList = new ArrayList<FxAssignment>(30);
                 curList.addAll(currentType.getAssignedGroups());
                 curList.addAll(currentType.getAssignedProperties());
             } else { // get all other (non root) groups from the environment. (from a hashmap)
                 FxGroupAssignment tmp = (FxGroupAssignment) env.getAssignment(currentType.getName() + currentXPath);
                 curList = tmp.getAssignments();
             }
             // after we build a list with the properties of the current group, we check if all found properties has a valid length
             for (FxAssignment curAss : curList) {
                 String key = curAss.getXPath().substring(len);
                 Integer curMult = curMults.get(key);
                 if (curMult == null)
                     curMult = 0;
                 if (!curAss.getMultiplicity().isValid(curMult)) {
                     final FxInvalidParameterException error;
                     if (curMult > 0)
                         error = new FxInvalidParameterException(/*curAss.getAlias()*/key, "ex.content.required.missing",
                                 curAss.getDisplayName(true), curMult, curAss.getMultiplicity().toString());
                     else
                         error = new FxInvalidParameterException(/*curAss.getAlias()*/key, "ex.content.required.missing.none",
                                 /*curAss.getDisplayName(true)*/ key, curAss.getMultiplicity().toString());
                     // notify caller or invalid XPath
                     error.setAffectedXPath(curAss.getXPath(), FxContentExceptionCause.RequiredViolated);
                     throw error;
                 }
             }
             // mark the xpath to be removed
             processedXpaths.add(currentXPath);
         }
 
         // First create a list containing all groups in the root
         List<FxGroupAssignment> allTypeGroup = new LinkedList<FxGroupAssignment>();
         allTypeGroup.addAll(currentType.getAssignedGroups());
 
         // instead a recursion have a (linked)list where we put groups at the end and add the first group as long
         // as there are groups
         while (allTypeGroup.size() > 0) {
             FxGroupAssignment currData = allTypeGroup.remove(0);
             String xPath = currData.getXPath().substring(len);
             // if we don't processed a xpath, check if it was optional
             if (!processedXpaths.contains(xPath)) {
                 groups : for (FxAssignment currAss : currData.getAssignments()) {
                     // optional groups could be missing but we have to add them so if they are present, they need to have the right fields
                     FxGroupAssignment parentGroupAssignment = currAss.getParentGroupAssignment();
                     while (parentGroupAssignment != null) {
                         if (parentGroupAssignment.getMultiplicity().isOptional())   // if one of the parent groups are optional, skip the group
                             break groups ;
                         parentGroupAssignment = parentGroupAssignment.getParentGroupAssignment();
                     }
                     if (!currAss.getMultiplicity().isOptional()) {
                         final FxInvalidParameterException error = new FxInvalidParameterException(currAss.getXPath(), "ex.content.required.missing.none",
                                 currAss.getDisplayName(true), currAss.getMultiplicity().toString());
                         error.setAffectedXPath(xPath, FxContentExceptionCause.RequiredViolated);
                         throw error;
                     }
                 }
 
             }
             allTypeGroup.addAll(currData.getAssignedGroups());
         }
     }
 
 
     /*
      * Recursively check a group and its properties and subgroups if required properties are present
      *
      * @param data FxGroupData to check
      * @throws FxInvalidParameterException if required properties are not present
      *
      private void _checkGroupValidity(FxGroupData data) throws FxInvalidParameterException {
         if (data.getAssignmentMultiplicity().isOptional() && data.isEmpty())
             return; //if optional groups have required properties or subgroups it still is ok if they are empty!
         for (FxData curr : data.getChildren()) {
             if (curr instanceof FxPropertyData) {
                 final FxPropertyData propertyData = (FxPropertyData) curr;
                 propertyData.checkRequired();
                 propertyData.checkMaxLength();
             } else
                 _checkGroupValidity((FxGroupData) curr);
         }
     }*/
 
     public FxContent initSystemProperties() {
         FxEnvironment env = CacheAdmin.getEnvironment();
         FxType type = env.getType(this.getTypeId());
         FxValue value;
         for (FxPropertyAssignment sp : env.getSystemInternalRootPropertyAssignments()) {
             if (sp.getAlias().equals("ID"))
                 value = new FxLargeNumber(false, this.getId());
             else if (sp.getAlias().equals("VERSION"))
                 value = new FxNumber(false, this.getVersion());
             else if (sp.getAlias().equals("TYPEDEF"))
                 value = new FxLargeNumber(false, this.getTypeId());
             else if (sp.getAlias().equals("MANDATOR"))
                 value = new FxLargeNumber(false, this.getMandatorId());
             else if (sp.getAlias().equals("STEP"))
                 value = new FxLargeNumber(false, this.getStepId());
             else if (sp.getAlias().equals("MAX_VER"))
                 value = new FxNumber(false, this.getMaxVersion());
             else if (sp.getAlias().equals("LIVE_VER"))
                 value = new FxNumber(false, this.getLiveVersion());
             else if (sp.getAlias().equals("ISMAX_VER"))
                 value = new FxBoolean(false, this.isMaxVersion());
             else if (sp.getAlias().equals("ISLIVE_VER"))
                 value = new FxBoolean(false, this.isLiveVersion());
             else if (sp.getAlias().equals("ISACTIVE"))
                 value = new FxBoolean(false, this.isActive());
             else if (sp.getAlias().equals("MAINLANG"))
                 value = new FxLargeNumber(false, this.getMainLanguage());
             else if (sp.getAlias().equals("CREATED_BY"))
                 value = new FxLargeNumber(false, this.getLifeCycleInfo().getCreatorId());
             else if (sp.getAlias().equals("CREATED_AT"))
                 value = new FxDateTime(false, new Date(this.getLifeCycleInfo().getCreationTime()));
             else if (sp.getAlias().equals("MODIFIED_BY"))
                 value = new FxLargeNumber(false, this.getLifeCycleInfo().getModificatorId());
             else if (sp.getAlias().equals("MODIFIED_AT"))
                 value = new FxDateTime(false, new Date(this.getLifeCycleInfo().getModificationTime()));
             else if (sp.getAlias().equals("RELSRC"))
                 value = new FxReference(false, type.isRelation() ? new ReferencedContent(this.getRelatedSource()) : FxReference.EMPTY);
             else if (sp.getAlias().equals("RELDST"))
                 value = new FxReference(false, type.isRelation() ? new ReferencedContent(this.getRelatedDestination()) : FxReference.EMPTY);
             else if (sp.getAlias().equals("RELPOS_SRC"))
                 value = new FxNumber(false, type.isRelation() ? this.getRelatedSourcePosition() : FxNumber.EMPTY);
             else if (sp.getAlias().equals("RELPOS_DST"))
                 value = new FxNumber(false, type.isRelation() ? this.getRelatedDestinationPosition() : FxNumber.EMPTY);
             else
                 value = null;
             if (value != null) {
                 FxPropertyAssignment thispa = (FxPropertyAssignment) env.getAssignment(type.getName() + "/" + sp.getAlias());
                 this.data.addProperty(XPathElement.toXPathMult("/" + thispa.getAlias()), thispa, value, thispa.getPosition());
             }
         }
         updateAclProperty();
 
         return this;
     }
 
     /**
      * Update all system internal properties that provide setters to reflect changes in the FxPropertyData's
      */
     private void updateSystemInternalProperties() {
         FxLargeNumber _long = (FxLargeNumber) getValue("/STEP");
         _long.setValue(stepId);
 
         updateAclProperty();
 
         FxLargeNumber _langlong = (FxLargeNumber) getValue("/MAINLANG");
         _langlong.setValue(mainLanguage);
 
         FxBoolean _bool = (FxBoolean) getValue("/ISACTIVE");
         _bool.setValue(isActive());
     }
 
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     private void updateAclProperty() {
         if (containsValue("/ACL") && getPropertyData("/ACL").getOccurances() > aclIds.size()) {
             // remove old ACLs that wouldn't be overwritten otherwise
             int index = 2;
             final int max = getPropertyData("/ACL").getPropertyAssignment().getMultiplicity().getMax();
             while (index <= max && getValue("/ACL[" + index + "]") != null) {
                 remove("/ACL[" + index + "]");
                 index++;
             }
         }
         int index = 1;
         final FxEnvironment environment = CacheAdmin.getEnvironment();
         for (long aclId : aclIds) {
             final ACL acl = environment.getACL(aclId);
             if (acl.getCategory() != ACLCategory.INSTANCE) {
                 throw new FxInvalidParameterException(
                         "aclId", "ex.content.invalidACLType", acl.getName(), acl.getCategory(), ACLCategory.INSTANCE
                 ).asRuntimeException();
             }
             final String xpath = "/ACL[" + (index++) + "]";
             if (getValue(xpath) == null) {
                 createXPath(xpath);
             }
             setValue(xpath, aclId);
             try {
                 //since ACL is a system property, move it to the top because it is not visible in editors and can mess up positioning (see FX-649)
                 move(xpath, Integer.MIN_VALUE);
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
     }
 
     /**
      * Get a list of all property XPaths contained in this content in correct order
      *
      * @return list of all property XPaths contained in this content in correct order
      */
     public List<String> getAllPropertyXPaths() {
         List<String> xpaths = new ArrayList<String>(30);
         _addXPaths(xpaths, this.getRootGroup(), false, null);
         return xpaths;
     }
 
     /**
      * Get a list of all XPaths contained in this content in correct order
      *
      * @param groupPostfix String to append to found groups (useful to append "/" to kind of mark those XPaths as group XPaths)
      * @return list of all XPaths contained in this content in correct order
      */
     public List<String> getAllXPaths(String groupPostfix) {
         List<String> xpaths = new ArrayList<String>(30);
         _addXPaths(xpaths, this.getRootGroup(), true, groupPostfix);
         return xpaths;
     }
 
     /**
      * Recursively add all xpaths
      *
      * @param xpaths        list of xpaths to build
      * @param group         the current group to process
      * @param includeGroups include groups?
      * @param groupPostfix  String to append to found groups (useful to append "/" to kind of mark those XPaths as group XPaths)
      */
     private void _addXPaths(List<String> xpaths, FxGroupData group, boolean includeGroups, String groupPostfix) {
         for (FxData child : group.getChildren()) {
             if (child instanceof FxGroupData) {
                 if (includeGroups)
                     xpaths.add(child.getXPathFull() + groupPostfix);
                 _addXPaths(xpaths, (FxGroupData) child, includeGroups, groupPostfix);
             } else if (child instanceof FxPropertyData)
                 xpaths.add(child.getXPathFull());
         }
     }
 
     /**
      * Is a preview available that is not a default image?
      *
      * @return preview available
      */
     public boolean isPreviewAvailable() {
         return binaryPreviewId >= 0;
     }
 
     /**
      * Id of the binary used for previews
      *
      * @return id of the binary used for previews
      */
     public long getBinaryPreviewId() {
         return binaryPreviewId;
     }
 
     /**
      * ACL that is needed to view the preview image
      *
      * @return ACL that is needed to view the preview image
      */
     public long getBinaryPreviewACL() {
         return binaryPreviewACL;
     }
 
     /**
      * Set the binary preview to an XPath.
      * Illegal or non-existing values will be ignored!
      *
      * @param XPath the XPath of the requested binary to set as preview
      */
     public void setBinaryPreview(String XPath) {
         try {
             FxPropertyData data = this.getPropertyData(XPath);
             if (!(data.getValue() instanceof FxBinary))
                 return;
             binaryPreviewId = ((FxBinary) data.getValue()).getDefaultTranslation().getId();
             binaryPreviewACL = ((FxPropertyAssignment) data.getAssignment()).getACL().getId();
         } catch (Exception e) {
             //ignore
         }
     }
 
     /**
      * Set the binary preview.
      * Illegal or non-existing values will be ignored!
      *
      * @param binaryId if of the requested binary to set as preview
      */
     public void setBinaryPreview(long binaryId) {
         FxPropertyData data = checkPreviewIdExists(binaryId, this.getRootGroup().getChildren());
         if (data == null)
             return;
         binaryPreviewId = ((FxBinary) data.getValue()).getDefaultTranslation().getId();
         binaryPreviewACL = ((FxPropertyAssignment) data.getAssignment()).getACL().getId();
     }
 
     /**
      * Internal method that tries to find a matching preview image.
      * Searches for images and then regular binaries (preview is then set matching the mime type display).
      * If neither are found the BinaryDescriptor.SYS_UNKNOWN image is used
      *
      * @see com.flexive.shared.value.BinaryDescriptor#SYS_UNKNOWN
      */
     public void resolveBinaryPreview() {
         if (binaryPreviewId >= 0) {
             //check if the image (still) exists
             if (checkPreviewIdExists(binaryPreviewId, this.getRootGroup().getChildren()) == null) {
                 resetBinaryPreview();
                 resolveBinaryPreview();
             }
             return;
         }
         FxPropertyData bin = resolveFirstImageData(this.getRootGroup().getChildren());
         if (bin == null)
             bin = resolveFirstBinaryData(this.getRootGroup().getChildren());
         if (bin == null)
             resetBinaryPreview();
         else {
             binaryPreviewId = ((FxBinary) bin.getValue()).getDefaultTranslation().getId();
             binaryPreviewACL = ((FxPropertyAssignment) bin.getAssignment()).getACL().getId();
         }
     }
 
     /**
      * Check if an image with the given id exists in this FxContent instance
      *
      * @param binaryPreviewId the binary preview id to search
      * @param groupData       the group data entries to inspect
      * @return <code>true</code> if it exists
      */
     private FxPropertyData checkPreviewIdExists(long binaryPreviewId, List<FxData> groupData) {
         FxPropertyData ret;
         for (FxData data : groupData) {
             if (data.isGroup()) {
                 ret = checkPreviewIdExists(binaryPreviewId, ((FxGroupData) data).getChildren());
                 if (ret != null)
                     return ret;
             }
             if (data.isProperty() && data instanceof FxPropertyData && ((FxPropertyData) data).getValue() instanceof FxBinary) {
                 FxBinary bin = (FxBinary) ((FxPropertyData) data).getValue();
                 if (!bin.isEmpty() && bin.getDefaultTranslation().getId() == binaryPreviewId)
                     return (FxPropertyData) data;
             }
         }
         return null;
     }
 
     /**
      * Find the first available binary data value and return it
      *
      * @param groupData the group data entries to inspect
      * @return the first available binary data value or <code>null</code>
      */
     private FxPropertyData resolveFirstBinaryData(List<FxData> groupData) {
         FxPropertyData ret = null;
         for (FxData data : groupData) {
             if (data.isGroup())
                 ret = resolveFirstBinaryData(((FxGroupData) data).getChildren());
             if (ret != null)
                 return ret;
             if (data.isProperty() && data instanceof FxPropertyData && ((FxPropertyData) data).getValue() instanceof FxBinary) {
                 FxBinary bin = (FxBinary) ((FxPropertyData) data).getValue();
                 if (!bin.isEmpty())
                     return (FxPropertyData) data;
             }
         }
         return ret;
     }
 
     /**
      * Find the first available image data value and return it
      *
      * @param groupData the group data entries to inspect
      * @return the first available image data value or <code>null</code>
      */
     private FxPropertyData resolveFirstImageData(List<FxData> groupData) {
         FxPropertyData ret = null;
         for (FxData data : groupData) {
             if (data.isGroup())
                 ret = resolveFirstImageData(((FxGroupData) data).getChildren());
             if (ret != null)
                 return ret;
             if (data.isProperty() && data instanceof FxPropertyData && ((FxPropertyData) data).getValue() instanceof FxBinary) {
                 FxBinary bin = (FxBinary) ((FxPropertyData) data).getValue();
                 if (!bin.isEmpty() && bin.getDefaultTranslation().isImage())
                     return (FxPropertyData) data;
             }
         }
         return ret;
     }
 
     /**
      * Reset the preview image to show the default BinaryDescriptor.SYS_UNKNOWN image.
      *
      * @see BinaryDescriptor#SYS_UNKNOWN
      */
     public void resetBinaryPreview() {
         this.binaryPreviewId = BinaryDescriptor.SYS_UNKNOWN;
         this.binaryPreviewACL = 1;
     }
 
     /**
      * Create an independent copy of this FxContent
      *
      * @return a copy of this FxContent
      */
     public FxContent copy() {
         FxContent clone;
         clone = new FxContent(pk, lock, typeId, relation, mandatorId, -1, stepId, maxVersion, liveVersion, active,
                 mainLanguage, relatedSource, relatedDestination, relatedSourcePosition, relatedDestinationPosition,
                 lifeCycleInfo, data.copy(null), binaryPreviewId, binaryPreviewACL);
         clone.setAclIds(aclIds);
         return clone;
     }
 
     /**
      * Get an independent copy of this FxContent which is a new instance (can be saved as a copy)
      *
      * @return a copy of this FxContent with a new pk
      */
     public FxContent copyAsNewInstance() {
         FxContent clone;
         clone = new FxContent(new FxPK(), FxLock.noLockPK(), typeId, relation, mandatorId, -1, stepId, maxVersion, liveVersion, active,
                 mainLanguage, relatedSource, relatedDestination, relatedSourcePosition, relatedDestinationPosition,
                 lifeCycleInfo, data.copy(null), binaryPreviewId, binaryPreviewACL);
         clone.setAclIds(aclIds);
         return clone;
     }
 
     /**
      * Load all FxContent instances from properties of type FxReference
      *
      * @param ce ContentEngine
      * @throws FxApplicationException on errors
      */
     public void loadReferences(ContentEngine ce) throws FxApplicationException {
         List<FxReference> references = getRootGroup().getReferences(true);
 //        int refcount = 0;
 //        long time = System.currentTimeMillis();
         for (FxReference ref : references) {
             if (ref.isEmpty() || !ref.isValid())
                 continue;
             if (ref.isMultiLanguage()) {
                 for (long lang : ref.getTranslatedLanguages()) {
                     ReferencedContent r = ref.getTranslation(lang);
                     if (!r.hasContent() && r.isAccessGranted()) {
                         r.setContent(ce.load(r));
 //                        refcount++;
                     }
                 }
             } else {
                 if (!ref.getDefaultTranslation().hasContent() && ref.getDefaultTranslation().isAccessGranted())
                     ref.getDefaultTranslation().setContent(ce.load(ref.getDefaultTranslation()));
 //                refcount++;
             }
         }
 //        System.out.println("=> Loading " + refcount + " references took " + (System.currentTimeMillis() - time) + "[ms]");
     }
 
     /**
      * Returns the permission set for the calling user.
      *
      * @return the permission set for the calling user.
      */
     public PermissionSet getPermissions() {
         final FxType type = CacheAdmin.getEnvironment().getType(typeId);
         final long stepAclId = CacheAdmin.getEnvironment().getStep(stepId).getAclId();
         return FxPermissionUtils.getPermissionUnion(aclIds, type, stepAclId, lifeCycleInfo.getCreatorId(), mandatorId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == null || !(obj instanceof FxContent))
             return false;
         FxContent other = (FxContent) obj;
         if (!pk.equals(other.pk))
             return false;
         if (active != other.active || relation != other.relation || !lifeCycleInfo.equals(other.lifeCycleInfo))
             return false;
         FxDelta delta;
         try {
             delta = FxDelta.processDelta(this, other);
         } catch (FxApplicationException e) {
             return false;
         }
         return !delta.changes();
     }
 
     @Override
     public int hashCode() {
         int result;
         result = pk.hashCode();
         result = 31 * result + (active ? 1 : 0);
         result = 31 * result + (relation ? 1 : 0);
         result = 31 * result + lifeCycleInfo.hashCode();
         return result;
     }
 
     /**
      * Replace our data with data from another content
      *
      * @param con other content to take data from
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public void replaceData(FxContent con) {
         if (con == null)
             throw new FxInvalidParameterException("con", "ex.content.import.empty").asRuntimeException();
         if (con.getTypeId() != this.getTypeId()) {
             throw new FxInvalidParameterException("con", "ex.content.import.wrongType",
                     CacheAdmin.getEnvironment().getType(con.getTypeId()).getDisplayName(),
                     CacheAdmin.getEnvironment().getType(this.getTypeId()).getDisplayName()).asRuntimeException();
         }
         removeData();
         for (FxData d : con.data.getChildren()) {
             if (d.isSystemInternal())
                 continue;
             this.data.addChild(d);
         }
         this.setAclIds(con.getAclIds());
         this.setActive(con.isActive());
 //        this.setBinaryPreview(con.getBinaryPreviewId()); //TODO: fix me
         this.setMainLanguage(con.getMainLanguage());
         this.setRelatedDestination(con.getRelatedDestination());
         this.setRelatedDestinationPosition(con.getRelatedDestinationPosition());
         this.setRelatedSource(con.getRelatedSource());
         this.setRelatedSourcePosition(con.getRelatedSourcePosition());
         this.setStepId(con.getStepId());
     }
 
     /**
      * Remove all non-system data recursively
      */
     private void removeData() {
         for (FxData d : data.getChildren()) {
             if (d.isSystemInternal())
                 continue;
             data.getChildren().remove(d);
             removeData();
             return;
         }
     }
 
     /**
      * Check if this content instance has a caption property assigned
      *
      * @return <code>true</code> if a caption property is assigned to this content instance
      */
     public synchronized boolean hasCaption() {
         try {
             resolveCaption();
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
         return hasCaption;
     }
 
     /**
      * Get the caption value of this instance.
      * If no caption property is assigned, this method will return <code>null</code>
      *
      * @return caption or <code>null</code> if not assigned
      */
     public FxString getCaption() {
         if (!captionResolved)
             try {
                 resolveCaption();
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         return caption;
     }
 
     /**
      * Internally resolve if a caption property is present
      *
      * @throws FxApplicationException on errors
      */
     private synchronized void resolveCaption() throws FxApplicationException {
         if (captionResolved)
             return;
         long captionPropertyId = EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_CAPTION_PROPERTY);
         _resolveCaption(data, captionPropertyId);
         captionResolved = true;
     }
 
     /**
      * Recurse through all property data to find a caption value
      *
      * @param gdata             group to examine
      * @param captionPropertyId id of the caption property
      */
     private void _resolveCaption(FxGroupData gdata, long captionPropertyId) {
         for (FxData check : gdata.getChildren()) {
             if (check instanceof FxPropertyData) {
                 if (((FxPropertyData) check).getPropertyId() == captionPropertyId) {
                     caption = (FxString) ((FxPropertyData) check).getValue();
                     hasCaption = true;
                     return;
                 }
             } else if (check instanceof FxGroupData)
                 _resolveCaption((FxGroupData) check, captionPropertyId);
         }
     }
 
     /**
      * Update the lock of this instance.
      * Internal use only to reflect changes to lock status for cached contents
      *
      * @param lock the new lock to apply
      */
     public void updateLock(FxLock lock) {
         this.lock = lock;
     }
 }
