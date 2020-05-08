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
 package com.flexive.shared.content;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.XPathElement;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.security.LifeCycleInfo;
 import com.flexive.shared.security.PermissionSet;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.FxMultiplicity;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.value.*;
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
     public FxContent(FxPK pk, long typeId, boolean relation, long mandatorId, long aclId, long stepId, int maxVersion, int liveVersion,
                      boolean active, long mainLanguage, FxPK relatedSource, FxPK relatedDestination, int relatedSourcePosition,
                      int relatedDestinationPosition, LifeCycleInfo lifeCycleInfo, FxGroupData data, long binaryPreviewId,
                      long binaryPreviewACL) {
         this.pk = pk;
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
         return aclIds.isEmpty() ? -1 : aclIds.get(0);
     }
 
     /**
      * Set the content ACL id. If more than one ACL was assigned, the additional ACLs are removed
      * before assigning the new ACL.
      *
      * @param aclId the ACL id
      */
     public void setAclId(long aclId) {
         aclIds.clear();
         aclIds.add(aclId);
         //TODO: check if ACL is valid!
         updateSystemInternalProperties();
     }
 
     /**
      * Return all ACLs assigned to this content.
      *
      * @return  all ACLs assigned to this content.
      */
     public List<Long> getAclIds() {
         return Collections.unmodifiableList(aclIds);
     }
 
     public void setAclIds(Collection<Long> aclIds) {
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
         return stepId;
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
         else
             this.liveVersion = this.initialLiveVersion;
         updateSystemInternalProperties();
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
         return mainLanguage;
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
         return active;
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
      * Get all FxData (Group or Property) entries for the given XPath.
      * <p/>
      * Note: If the XPath refers to a group, only its child entries are returned
      * and not the FxData of the group itsself. For accessing the group data itself
      * use {@link #getGroupData(String)} instead.
      *
      * @param XPath requested XPath
      * @return FxData elements for the given XPath
      */
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
      * @param propertyId   the property id requested
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
     public FxGroupData getGroupData(String XPath) {
         FxSharedUtils.checkParameterEmpty(XPath, "XPATH");
         XPath = XPathElement.stripType(XPathElement.toXPathMult(XPath.toUpperCase()));
         //this is a slightly modified version of getData() but since groups may not contain children its safer
         if (StringUtils.isEmpty(XPath) || "/".equals(XPath))
             return getRootGroup();
         List<FxData> currChildren = data.getChildren();
         FxGroupData group = null;
         boolean found;
         for (XPathElement xpe : XPathElement.split(XPath.toUpperCase())) {
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
      * Get the (virtual) root group of this content
      *
      * @return root group
      */
     public FxGroupData getRootGroup() {
         return this.data;
     }
 
     /**
      * Set a properties value, needed groups will be created
      *
      * @param XPath FQ XPath
      * @param value value to apply
      * @return this FxContent instance to allow chained calls
      */
     public FxContent setValue(String XPath, FxValue value) {
         getProperyData(XPath).setValue(value);
         return this;
     }
 
     /**
      * Get the FxPropertyData entry for an XPath.
      * If the entry does not exist yet, it will be created.
      *
      * @param XPath requested xpath
      * @return FxPropertyData
      */
     private FxPropertyData getProperyData(String XPath) {
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
      * Create (if possible and not already exists) the given XPath
      *
      * @param XPath the XPath to create
      */
     private void createXPath(String XPath)  {
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
             if (isXPathValid(XPath, true))
                 return null; //just not set, see FX-473
             throw e;
         }
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
         if (!isXPathValid(XPath, true))
             //noinspection ThrowableInstanceNeverThrown
             throw new FxInvalidParameterException("XPATH", "ex.xpath.element.noProperty", XPath).asRuntimeException();
         final FxEnvironment env = CacheAdmin.getEnvironment();
         long assignmentId = env.getType(getTypeId()).getAssignment(XPath).getId();
         FxGroupData group = getGroupData(XPathElement.stripLastElement(XPath));
         List<String> paths = new ArrayList<String>(10);
         for (FxData data : group.getChildren()) {
             if (data.isGroup())
                 continue;
             if (data.getAssignmentId() == assignmentId)
                 paths.add(data.getXPathFull());
         }
         String[] p = paths.toArray(new String[paths.size()]);
         Arrays.sort(p);
         List<FxValue> values = new ArrayList<FxValue>(p.length);
         for (String xp : p)
             values.add(getValue(xp));
         return values;
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
      * Check if the given XPath is valid for this content.
      * This is a shortcut to the corresponding type's method!
      *
      * @param XPath         the XPath to check
      * @param checkProperty should the XPath point to a property?
      * @return if the XPath is valid or not
      * @see FxType#isXPathValid(String,boolean)
      */
     public boolean isXPathValid(String XPath, boolean checkProperty) {
         return CacheAdmin.getEnvironment().getType(this.getTypeId()).isXPathValid(XPath, checkProperty);
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
         if (delta == 0 || StringUtils.isEmpty(XPath) || "/".equals(XPath))
             return; //nothing to do
         List<FxData> mdata = getData(XPath);
         FxGroupData parent = mdata.get(0).getParent();
         XPathElement last = XPathElement.lastElement(XPath);
         parent.moveChild(last, delta);
     }
 
     /**
      * Remove the property or group denoted by XPath
      *
      * @param XPath the XPath to remove
      */
     public void remove(String XPath) {
         FxSharedUtils.checkParameterEmpty(XPath, "XPATH");
         XPath = XPathElement.stripType(XPath);
         FxData data = null;
         if (!XPathElement.lastElement(XPath).isIndexDefined()) {
             //remove all
             String parentGroup = XPathElement.stripLastElement(XPath);
             data = getGroupData(parentGroup);
             List<FxData> remove = new ArrayList<FxData>(10);
             for (FxData child : ((FxGroupData) data).getChildren())
                 if (child.getXPath().equals(XPath))
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
         _checkGroupValidity(data);
     }
 
     /**
      * Recursively check a group and its properties and subgroups if required properties are present
      *
      * @param data FxGroupData to check
      * @throws FxInvalidParameterException if required properties are not present
      */
     private void _checkGroupValidity(FxGroupData data) throws FxInvalidParameterException {
         if (data.getAssignmentMultiplicity().isOptional() && data.isEmpty())
             return; //if optional groups have required properties or subgroups it still is ok if they are empty!
         for (FxData curr : data.getChildren()) {
             if (curr instanceof FxPropertyData) {
                 ((FxPropertyData) curr).checkRequired();
                 ((FxPropertyData) curr).checkMaxLength();
             } else
                 _checkGroupValidity((FxGroupData) curr);
         }
     }
 
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
 
     private void updateAclProperty() {
         if (containsValue("/ACL") && getPropertyData("/ACL").getOccurances() > aclIds.size()) {
             // remove old ACLs that wouldn't be overwritten otherwise
             remove("/ACL");
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
         clone = new FxContent(pk, typeId, relation, mandatorId, -1, stepId, maxVersion, liveVersion, active,
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
         clone = new FxContent(new FxPK(), typeId, relation, mandatorId, -1, stepId, maxVersion, liveVersion, active,
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
     public void replaceData(FxContent con) {
         if (con == null)
             throw new FxInvalidParameterException("con", "ex.content.import.empty").asRuntimeException();
         if (con.getTypeId() != this.getTypeId()) {
             throw new FxInvalidParameterException("con", "ex.content.import.wrongType",
                     CacheAdmin.getEnvironment().getType(con.getTypeId()).getLabel(),
                     CacheAdmin.getEnvironment().getType(this.getTypeId()).getLabel()).asRuntimeException();
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
      * @throws FxApplicationException on erros
      */
     public synchronized boolean hasCaption() throws FxApplicationException {
         resolveCaption();
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
 }
