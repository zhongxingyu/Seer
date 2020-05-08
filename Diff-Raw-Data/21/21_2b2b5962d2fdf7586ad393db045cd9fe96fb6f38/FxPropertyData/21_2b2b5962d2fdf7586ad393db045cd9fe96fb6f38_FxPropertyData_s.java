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
 
 import com.flexive.shared.FxContext;
 import com.flexive.shared.XPathElement;
 import com.flexive.shared.exceptions.FxContentExceptionCause;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.structure.FxMultiplicity;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.structure.FxStructureOption;
 import com.flexive.shared.structure.GroupMode;
 import com.flexive.shared.value.*;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * FxData extension for properties
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxPropertyData extends FxData {
     private static final long serialVersionUID = -8738710689160073148L;
     private FxValue value;
     private long propertyId;
     private boolean containsDefaultValue;
     private FxStructureOption maxLength;
     private static final Log LOG = LogFactory.getLog(FxPropertyData.class);
 
     public FxPropertyData(String xpPrefix, String alias, int index, String xPath, String xPathFull, int[] indices,
                           long assignmentId, long propertyId, FxMultiplicity assignmentMultiplicity, int pos, FxGroupData parent,
                           FxValue value, boolean systemInternal, FxStructureOption maxLength) {
         super(xpPrefix, alias, index, xPath, xPathFull, indices, assignmentId, assignmentMultiplicity, pos, parent, systemInternal);
         this.value = value;
         this.propertyId = propertyId;
         this.containsDefaultValue = false;
         if (this.value != null)
             this.value.setXPath(xpPrefix + this.getXPathFull());
         this.maxLength=maxLength;
     }
 
     /**
      * Get the assigned value
      *
      * @return value
      */
     public FxValue getValue() {
         return value;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isProperty() {
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isGroup() {
         return false;
     }
 
     /**
      * Get the id of the property used by the assignment
      *
      * @return id of the property used by the assignment
      */
     public long getPropertyId() {
         return propertyId;
     }
 
     /**
      * Get the assignment cast to FxProperyAssignment
      *
      * @return property assignment
      * @since 3.1
      */
     public FxPropertyAssignment getPropertyAssignment() {
         return (FxPropertyAssignment)getAssignment();
     }
 
     /**
      * Set a new value for this property data
      *
      * @param value the value to set
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public void setValue(FxValue value) {
         if (value == null)
             return;
         FxPropertyAssignment pa = (FxPropertyAssignment) this.getAssignment();
         if (value.isMultiLanguage() != pa.isMultiLang()) {
             if (pa.isMultiLang())
                 throw new FxInvalidParameterException("value", "ex.content.value.invalid.multilanguage.ass.multi", this.getXPathFull()).asRuntimeException();
             else
                 throw new FxInvalidParameterException("value", "ex.content.value.invalid.multilanguage.ass.single", this.getXPathFull()).asRuntimeException();
         }
         if (pa.getProperty().isSystemInternal())
             throw new FxInvalidParameterException(pa.getAlias(), "ex.content.value.systemInternal").setAffectedXPath(this.getXPathFull(), FxContentExceptionCause.SysInternalAttempt).asRuntimeException();
         if (!pa.isValid(value))
             throw new FxInvalidParameterException("value", "ex.content.value.invalid",
                     this.getXPathFull(),
                     ((FxPropertyAssignment) this.getAssignment()).getProperty().getDataType(),
                     value.getValueClass().getCanonicalName()).setAffectedXPath(this.getXPathFull(), FxContentExceptionCause.InvalidValueDatatype).asRuntimeException();
         if (pa.hasParentGroupAssignment() && pa.getParentGroupAssignment().getMode() == GroupMode.OneOf) {
             //check if parent group is a one-of and already some other data set
             for (FxData check : this.getParent().getChildren()) {
                 if (!check.isEmpty() && check.getAssignmentId() != this.getAssignmentId())
                     throw new FxInvalidParameterException("value", "ex.content.xpath.group.oneof", this.getXPathFull(), this.getParent().getXPathFull()).asRuntimeException();
             }
         }
         this.containsDefaultValue = false;
         if (this.value == null || FxContext.get().getRunAsSystem()) {
             this.value = value;
             this.value.setXPath(this.xpPrefix + this.getXPathFull());
             getParent().valueChanged(this.value.getXPath(), FxValueChangeListener.ChangeType.Update);
             return;
         }
         if (this.value instanceof FxNoAccess)
             throw new FxNoAccessException("ex.content.value.noaccess").setAffectedXPath(this.getXPathFull(), FxContentExceptionCause.NoAccess).asRuntimeException();
         if (this.value.isReadOnly())
             throw new FxNoAccessException("ex.content.value.readOnly").setAffectedXPath(this.getXPathFull(), FxContentExceptionCause.ReadOnly).asRuntimeException();
         this.value = value;
         this.value.setXPath(this.xpPrefix + this.getXPathFull());
        if(getParent().hasChangeListener() && !value.isEmpty())
             getParent().valueChanged(this.value.getXPath(), FxValueChangeListener.ChangeType.Update);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isEmpty() {
         return this.value == null || this.value.isEmpty();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isRequiredPropertiesPresent() {
         return this.getAssignmentMultiplicity().isRequired();
     }
 
     /**
      * Is the value contained created from a default value?
      *
      * @return value contained created from a default value
      */
     public boolean isContainsDefaultValue() {
         return containsDefaultValue;
     }
 
     /**
      * Set if the value is created from a default value - <b>internal method!</b>
      *
      * @param containsDefaultValue if the value is created from a default value
      */
     public void setContainsDefaultValue(boolean containsDefaultValue) {
         this.containsDefaultValue = containsDefaultValue;
     }
 
     /**
      * Apply the multiplicity to XPath and children if its a group
      */
     @Override
     protected void applyIndices() {
         final List<XPathElement> elements = XPathElement.split(this.getXPathFull());
         if (elements.get(elements.size() - 1).getIndex() != this.getIndex()) {
             elements.get(elements.size() - 1).setIndex(this.getIndex());
             setXPathFull(XPathElement.toXPath(elements));
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void setXPathFull(String xpathFull) {
         this.XPathFull = xpathFull;
         this.indices = XPathElement.getIndices(this.XPathFull);
         if (this.value != null) {
             // apply updated XPath to FxValue (FX-920)
             this.value.setXPath(this.xpPrefix + this.XPathFull);
         }
     }
 
     /**
      * Check if this property is required and present in its minimal multiplicity
      *
      * @throws FxInvalidParameterException if required properties are empty
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown", "UnusedDeclaration"})
     public void checkRequired() throws FxInvalidParameterException {
         if (this.getAssignmentMultiplicity().isOptional())
             return;
         int valid = 0;
         for (FxData curr : getParent().getChildren())
             if (curr.getAssignmentId() == this.getAssignmentId() && !curr.isEmpty())
                 valid++;
         if (valid < this.getAssignmentMultiplicity().getMin()) {
             // print the label of the property in the error msg if the label is present
             throw new FxInvalidParameterException(this.getAlias(), "ex.content.required.missing",
                     this.getAssignment().getDisplayName(true), valid, this.getAssignmentMultiplicity().toString())
                     .setAffectedXPath(this.getXPathFull(), FxContentExceptionCause.RequiredViolated);
         }
     }
 
     /**
      * Check if the maximum length is valid (if applicable)
      *
      * @throws FxInvalidParameterException on errors
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public void checkMaxLength() throws FxInvalidParameterException {
         if (!this.getMaxLength().isSet() || value == null || value.isEmpty() || this.getMaxLength().getIntValue() == -1)
             return;
         //check for max-length compatible data types
         if (this.getValue() instanceof FxHTML || !(this.getValue() instanceof FxString || this.getValue() instanceof FxNumber || this.getValue() instanceof FxLargeNumber)) {
             LOG.warn("Values of type " + this.getValue().getClass().getSimpleName() + " are not compatible with FxStructureOption.OPTION_MAXLENGTH, check omitted!");
             return;
         }
 
         for (long lang : value.getTranslatedLanguages()) {
             if (value.getTranslation(lang).toString().length() > maxLength.getIntValue())
                 throw new FxInvalidParameterException(this.getAlias(), "ex.content.value.invalid.maxLength",
                         this.getAssignment().getDisplayName(true), getMaxLength().getIntValue(), value.toString(), value.toString().length()).setAffectedXPath(this.getXPathFull(), FxContentExceptionCause.MaxlengthViolated);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getCreateableElements() {
         if (this.value != null && this.value instanceof FxNoAccess)
             return 0;
         return super.getCreateableElements();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getRemoveableElements() {
         if (this.value != null && (this.value instanceof FxNoAccess || this.value.isReadOnly()))
             return 0;
         return super.getRemoveableElements();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isRemoveable() {
         return super.isRemoveable()
                 &&  // don't remove system internal assignments, except superfluous /ACL entries 
                 ("ACL".equals(getPropertyAssignment().getProperty().getName()) || !this.isSystemInternal())
                 &&  //only removeable if not null, system internal or not non accessible or readonly
                 (!(this.value != null && (this.value instanceof FxNoAccess || this.value.isReadOnly())) || this.value == null);
     }
 
     /**
      * Returns the value of the assignment's FxStructureOption.MAXLENGTH, or -1 of the option is not set.
      *
      * @return the value of the assignment's FxStructureOption.MAXLENGTH, or -1 of the option is not set
      */
     public FxStructureOption getMaxLength() {
         return maxLength;
     }
 
     /**
      * Return a list of all values of this assignment.
      *
      * @param includeEmpty true to include empty (i.e. newly initialized) values
      * @return a list of all values of this assignment.
      */
     public List<FxValue> getValues(boolean includeEmpty) {
         final List<FxValue> values = new ArrayList<FxValue>();
         for (FxData data : getParent().getChildren()) {
             if (data.getAssignmentId() == getAssignmentId()) {
                 final FxValue value = ((FxPropertyData) data).getValue();
                 if (includeEmpty || !value.isEmpty()) {
                     values.add(value);
                 }
             }
         }
         return values;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object obj) {
         if (this == obj) return true;
         if (!(obj instanceof FxPropertyData))
             return false;
         if (!super.equals(obj))
             return false;
         FxPropertyData comp = (FxPropertyData) obj;
         return this.value.equals(comp.value) && this.propertyId == comp.propertyId;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
         int result;
         result = value.hashCode();
         result = 31 * result + (int) (propertyId ^ (propertyId >>> 32));
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     FxPropertyData copy(FxGroupData parent) {
         return new FxPropertyData(xpPrefix, getAlias(), getIndex(), getXPath(), getXPathFull(),
                 ArrayUtils.clone(getIndices()), getAssignmentId(), getPropertyId(), getAssignmentMultiplicity(),
                 getPos(), parent, value.copy(), isSystemInternal(), maxLength);
     }
 }
