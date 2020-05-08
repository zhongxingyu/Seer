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
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.XPathElement;
 import com.flexive.shared.exceptions.FxCreateException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.structure.*;
 import org.apache.commons.lang.ArrayUtils;
 
 import java.io.Serializable;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.WeakHashMap;
 
 /**
  * Abstract base class for property and group data
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public abstract class FxData implements Serializable {
     private static final long serialVersionUID = 1268634970583396012L;
 
     public final static int POSITION_TOP = -1;
     public final static int POSITION_BOTTOM = -2;
 
     /**
      * XPath cache Workaround since Strings no longer share the backing array when created via String#substring since JDK 1.7_06+.
      * We would end up with lots of duplicate Strings that previously used the same XPath String data
      * (from the FxASssignment) unless we cache them manually.
      */
     private static final WeakHashMap<String, WeakReference<String>> XP_CACHE = new WeakHashMap<String, WeakReference<String>>();
 
     /**
      * XPath without indices
      */
     private String XPath;
 
     /**
      * XPath with indices
      */
     protected String XPathFull;
 
     /**
      * Indices for each element in the XPath
      */
     protected int[] indices;
 
     /**
      * Id of the associated assignment
      */
     private long assignmentId;
 
     /**
      * Position within same hierarchy level
      */
     private int pos;
 
     /**
      * Parent element (virtual root for the first)
      */
    private FxGroupData parent;
 
     /**
      * XPathElement of this entry
      */
     protected XPathElement xp;
 
     /**
      * xXPath prefix like "FxType name[@pk=..]"
      */
     protected String xpPrefix;
 
     protected FxData(String xpPrefix, String alias, int index, String xPath, String xPathFull, int[] indices, long assignmentId,
                      FxMultiplicity assignmentMultiplicity, int pos, FxGroupData parent, boolean systemInternal) {
         this.xpPrefix = xpPrefix;
         this.XPath = xpCached(XPathElement.stripType(xPath));
         this.XPathFull = xpCached(XPathElement.stripType(xPathFull));
         this.indices = indices;
         this.assignmentId = assignmentId;
         this.pos = pos;
         this.parent = parent;
         this.xp = new XPathElement(alias, index, true);
         if (index != 1)
             applyIndices();
     }
 
     /**
      * Copy constructor.
      *
      * @param other    the FxData instance to be copied
      * @param parent   the new group data parent
      * @since 3.1.7
      */
     protected FxData(FxData other, FxGroupData parent) {
         this.xpPrefix = other.xpPrefix;
         this.XPath = other.XPath;
         this.XPathFull = other.XPathFull;
         this.indices = other.indices.clone();
         this.assignmentId = other.assignmentId;
         this.pos = other.pos;
         this.parent = parent;
         this.xp = new XPathElement(other.xp.getAlias(), other.xp.getIndex(), true);
     }
 
     /**
      * Is this FxData a property or group?
      *
      * @return if this FxData is a property or group
      */
     public abstract boolean isProperty();
 
     /**
      * Is this FxData a property or group?
      *
      * @return if this FxData is a property or group
      */
     public abstract boolean isGroup();
 
     /**
      * Is this data empty?
      *
      * @return empty
      */
     public abstract boolean isEmpty();
 
     /**
      * Replace all data with empty values/groups
      */
     public abstract void setEmpty();
 
     /**
      * Are there any required properties (empty or non-empty) present?
      *
      * @return true if there any required properties (empty or non-empty) present
      * @since 3.1
      */
     public abstract boolean isRequiredPropertiesPresent();
 
     /**
      * Is this a system internal property or group?
      *
      * @return system internal
      */
     public boolean isSystemInternal() {
         return getAssignment().isSystemInternal();
     }
 
     public int getIndex() {
         return xp.getIndex();
     }
 
     /**
      * Set the index of this data entry
      *
      * @param index the index to set
      */
     private void setIndex(int index) {
         this.xp.setIndex(index);
         this.applyIndices();
     }
 
     /**
      * Apply the multiplicity to XPath and children if its a group
      */
     protected abstract void applyIndices();
 
     /**
      * Update the XPath with indices.
      *
      * @param xpathFull the complete XPath including indices.
      * @since 3.1.4
      */
     protected abstract void setXPathFull(String xpathFull);
 
     /**
      * Get the prefix to use for the XPath (Name of the type and primary key)
      *
      * @return prefix to use for the XPath
      */
     public String getXPathPrefix() {
         return xpPrefix;
     }
 
     /**
      * Get the XPath of this FxData
      *
      * @return XPath
      */
     public String getXPath() {
         return XPath;
     }
 
     /**
      * Get this FxData as XPathElement
      *
      * @return XPathElement
      */
     public XPathElement getXPathElement() {
         return xp;
     }
 
     /**
      * Get this FxData's XPath with all indices
      *
      * @return XPath with all indices
      */
     public String getXPathFull() {
         return XPathFull;
     }
 
     /**
      * Get the indices of this FxData (min, max)
      *
      * @return indices of this FxData (min, max)
      */
     public int[] getIndices() {
         return ArrayUtils.clone(indices);
     }
 
     /**
      * Get the id of the assignment of this FxData
      *
      * @return assignment id
      */
     public long getAssignmentId() {
         return assignmentId;
     }
 
     /**
      * Get this FxData's associated assignment
      *
      * @return FxAssignment
      */
     public synchronized FxAssignment getAssignment() {
         if (assignmentId == -1) {
             // root group assignment
             return new FxGroupAssignment(-1, true, CacheAdmin.getEnvironment().getType(FxType.ROOT_ID), "", "", 1, FxMultiplicity.MULT_1_1,
                     1, null, -1, null, null,
                     new FxGroup(-1, "", null, null, true, FxMultiplicity.MULT_1_1, null),
                     GroupMode.AnyOf, null);
         }
         return CacheAdmin.getEnvironment().getAssignment(this.getAssignmentId());
     }
 
     /**
      * May more instances of this element be created within the same hierarchy level
      *
      * @return if more instances of this element may be created within the same hierarchy level
      */
     public boolean mayCreateMore() {
         return getCreateableElements() > 0;
     }
 
     /**
      * Get the number of (same) FxData elements that may be created within the same hierarchy level
      *
      * @return number of (same) FxData elements that may be created within the same hierarchy level
      */
     public int getCreateableElements() {
         if (parent == null)
             return 0; //just one instance of the virtual root group
         final FxMultiplicity assignmentMultiplicity = getAssignmentMultiplicity();
         if (assignmentMultiplicity.getMax() == 1)
             return 0; //we have to be the only instance
         if (assignmentMultiplicity.isUnlimited())
             return FxMultiplicity.N;
         //gather count of same elements
         int count = getOccurances();
         count = assignmentMultiplicity.getMax() - count;
         return count > 0 ? count : 0;
     }
 
     /**
      * Return the number of occurances of this assignment in this
      * FxData instance.
      *
      * @return the number of occurances of this assignment in this FxData instance
      */
     public int getOccurances() {
         return getElements().size();
     }
 
     public List<FxData> getElements() {
         List<FxData> elements = new ArrayList<FxData>();
         for (FxData data : parent.getChildren()) {
             if (data.getAssignmentId() == assignmentId) {
                 elements.add(data);
             }
         }
         return elements;
     }
 
     /**
      * Get the number of elements that may be removed within the same hierarchy level
      *
      * @return number of elements that may be removed within the same hierarchy level
      */
     public int getRemoveableElements() {
         if (parent == null)
             return 0; //just one instance of the virtual root group
         //gather count of same elements
         int count = 0;
         for (FxData curr : parent.getChildren())
             if (curr.getAssignmentId() == this.assignmentId
                     // don't remove system internal assignments, except superfluous /ACL entries  
                     && ("ACL".equals(curr.getAssignment().getAlias()) || !curr.isSystemInternal()))
                 count++;
         count -= getAssignmentMultiplicity().getMin();
         return count > 0 ? count : 0;
     }
 
     /**
      * Compact the indices of assignments from the same type closing gaps, etc
      */
     public void compact() {
         if (parent == null)
             return; //cant compact root group
         int idx = 1;
         int pos = 0;
         boolean foundOther = false;
         for (FxData curr : parent.getChildren()) {
             curr.setPos(pos++);
             if (curr.getAssignmentId() == this.assignmentId) {
                 curr.setIndex(idx++);
                 foundOther = true;
             }
         }
         if (!foundOther && this.getIndex() > 1)
             this.setIndex(1);
     }
 
     /**
      * Create a new instance of this FxData with the next available multiplicity at the requested position
      *
      * @param insertPosition the requested inserting position
      * @return a new FxData object
      * @throws FxNotFoundException         on errors
      * @throws FxInvalidParameterException on errors
      * @throws FxCreateException           on errors
      */
     public synchronized FxData createNew(int insertPosition) throws FxNotFoundException, FxInvalidParameterException, FxCreateException {
         if (!mayCreateMore())
             throw new FxCreateException("ex.content.data.create.maxMultiplicity", this.getXPath(), this.getAssignmentMultiplicity().getMax());
         FxType type = CacheAdmin.getEnvironment().getAssignment(assignmentId).getAssignedType();
         boolean isTop = false;
         if (insertPosition == POSITION_BOTTOM) {
             if (parent.getChildren().size() == 0)
                 insertPosition = 0; //should not be possible to happen but play safe...
             else
                 insertPosition = parent.getChildren().get(parent.getChildren().size() - 1).getPos() + 1;
         } else if (insertPosition < 0 || insertPosition == POSITION_TOP) {
             isTop = true;
             insertPosition = 0;
         }
         int newIndex = 1;
         for (FxData curr : parent.getChildren()) {
             if (curr.getAssignmentId() == this.assignmentId && curr.getIndex() + 1 > newIndex)
                 newIndex = curr.getIndex() + 1;
             if (curr.getPos() >= insertPosition && !curr.isSystemInternal())
                 curr.pos++;
         }
         FxAssignment fxa = type.getAssignment(this.getXPath());
         // we need to check min and max multiplicity
         FxData newData = fxa.createEmptyData(parent, newIndex);
         newData.setPos(insertPosition);
         newData.applyIndices();
         parent.addChild(newData); //adding honors the position!
         if (!isTop)
             compact();
         return newData;
     }
 
     /**
      * Get the multiplicity of the associated assignment
      *
      * @return multiplicity of the associated assignment
      */
     public FxMultiplicity getAssignmentMultiplicity() {
         return getAssignment().getMultiplicity();
     }
 
     /**
      * Get the position of this data element within its hierarchy level
      *
      * @return position of this data element within its hierarchy level
      */
     public int getPos() {
         return pos;
     }
 
     /**
      * Set the position of this data element within its hierarchy level
      *
      * @param pos position to set
      * @return this
      */
     public FxData setPos(int pos) {
         this.pos = pos;
         return this;
     }
 
     /**
      * Get the parent group of this data element
      *
      * @return parent group of this data element
      */
     public FxGroupData getParent() {
         return parent;
     }
 
     /**
      * Getter for the XPath alias (current XPath element name)
      *
      * @return alias
      */
     public String getAlias() {
         return xp.getAlias();
     }
 
     /**
      * Is this data removeable?
      *
      * @return if this data is removeable
      */
     public boolean isRemoveable() {
         return this.getRemoveableElements() > 0;
     }
 
     /**
      * Move this element by delta positions within its parent group
      * If delta is Integer.MAX_VALUE the data will always be placed at the bottom,
      * Integer.MIN_VALUE will always place it at the top.
      *
      * @param delta delta positions to move
      * @since 3.1.5
      */
     public void move(int delta) {
         if (getParent() == null)
             return; //root group can not be moved!
         getParent().moveChild(new XPathElement(this.getAlias(), this.getIndex(), true), delta);
     }
 
     /**
      * Move this elements index delta positions within the same group.
      * Warning: this will cause a repositioning within the parent group as well!
      * If for example this element has ALIAS[5] and the delta is 2, this element will be assigned a new index of 7 and
      * become ALIAS[7], moving the current element at ALIAS[7] to ALIAS[6] (assuming there are at least 7 elements of
      * assignment, else it will be moved to the bottom or respectively top)
      *
      * @param delta delta positions to move the index
      * @since 3.1.5
      */
     public void moveIndex(int delta) {
         if (this.getParent() == null || this.getAssignmentMultiplicity().getMax() == 1)
             return;
         this.compact(); //make sure there are no gaps and indices are ordered
         int total = this.getOccurances();
         int curr = this.getIndex();
         int newIndex = curr + delta;
         //ensure bounds
         if (newIndex < 1) newIndex = 1;
         if (newIndex > total) newIndex = total;
         FxData swap = null;
         for (FxData data : parent.getChildren()) {
             if (data.getAssignmentId() == assignmentId) {
                 if (data.getIndex() == newIndex) {
                     swap = data;
                     break;
                 }
             }
         }
         if (swap != null) {
             getParent().getChildren().remove(this);
             getParent().getChildren().add(swap.getPos(), this);
             this.compact();
         }
     }
 
     /**
      * Ensure that only a single String is used for the given XPath (XPaths are expired automatically
      * when they are no longer referenced).
      *
      * @param xpath     the XPath to be cached
      * @return          the canonical String object for {@code xpath}
      */
     static String xpCached(String xpath) {
         final WeakReference<String> cached;
         synchronized (XP_LOCK) {
             cached = XP_CACHE.get(xpath);
         }
         final String cachedValue = cached != null ? cached.get() : null;
         if (cachedValue != null) {
             return cachedValue;
         }
         synchronized (XP_LOCK) {
             XP_CACHE.put(xpath, new WeakReference<String>(xpath));
         }
         return xpath;
     }
 
     private static final Object XP_LOCK = new Object();
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return this.getXPathFull();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (!(obj instanceof FxData))
             return false;
         FxData comp = (FxData) obj;
         return this.getPos() == comp.getPos() &&
                 !(!this.getXPathFull().equals(comp.getXPathFull()) ||
                         this.getAssignmentId() != comp.getAssignmentId() ||
                         this.isEmpty() != comp.isEmpty()
                 );
     }
 
     @Override
     public int hashCode() {
         int result;
         result = XPathFull.hashCode();
         result = 31 * result + (int) (assignmentId ^ (assignmentId >>> 32));
         result = 31 * result + (isEmpty() ? 1 : 0);
         return result;
     }
 
     /**
      * Create an independent copy of this group or property FxData
      *
      * @param parent parent group
      * @return independent copy
      */
     abstract FxData copy(FxGroupData parent);
 
 }
