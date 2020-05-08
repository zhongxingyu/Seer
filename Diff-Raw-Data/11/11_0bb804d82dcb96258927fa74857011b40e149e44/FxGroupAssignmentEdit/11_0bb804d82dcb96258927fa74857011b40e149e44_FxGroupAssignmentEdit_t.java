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
 
 import com.flexive.shared.XPathElement;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.List;
 
 /**
  * FxGroupAssignment for editing
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxGroupAssignmentEdit extends FxGroupAssignment {
 
     private static final long serialVersionUID = 5823011785222672556L;
     private boolean isNew;
 
     /**
      * Ctor to make a FxGroupAssignment editable
      *
      * @param ga FxGroupAssignment to make editable
      */
     public FxGroupAssignmentEdit(FxGroupAssignment ga) {
         super(ga.getId(), ga.isEnabled(), ga.getAssignedType(), ga.getAlias(), ga.getXPath(),
                 ga.getPosition(), new FxMultiplicity(ga.getMultiplicity()), ga.getDefaultMultiplicity(), ga.getParentGroupAssignment(),
                 ga.getBaseAssignmentId(), ga.getLabel().copy(),
                 ga.getHint().copy(), ga.getGroup().asEditable(), ga.getMode(), FxStructureOption.cloneOptions(ga.options));
         this.isNew = false;
     }
 
     /**
      * Constructor to create a new FxGroupAssignmentEdit from an existing FxGroupAssignment as a new one for a given type with a
      * new alias and a given parentXPath
      *
      * @param ga          original group assignment
      * @param type        type to assign it
      * @param alias       new alias
      * @param parentXPath parent XPath within the type to assign
      * @param parent      optional parent assignment if already known (prevents lookup of parentXPath if valid)
      * @throws com.flexive.shared.exceptions.FxNotFoundException
      *          if parentXPath is invalid
      * @throws com.flexive.shared.exceptions.FxInvalidParameterException
      *          if parentXPath is invalid
      */
     private FxGroupAssignmentEdit(FxGroupAssignment ga, FxType type, String alias, String parentXPath, FxAssignment parent) throws FxNotFoundException, FxInvalidParameterException {
         super(-1, ga.isEnabled(), type, alias, XPathElement.buildXPath(false, parentXPath, alias),
                 ga.getPosition(), new FxMultiplicity(ga.getMultiplicity()), ga.getDefaultMultiplicity(), ga.getParentGroupAssignment(),
                 ga.getId(), ga.getLabel().copy(), ga.getHint().copy(), ga.getGroup(), ga.getMode(), FxStructureOption.cloneOptions(ga.options));
         if (parent == null && !"/".equals(parentXPath)) {
             //check parentXPath unless its top level (root)
             parent = type.getAssignment(parentXPath);
             if (parent != null && parent instanceof FxPropertyAssignment)
                 throw new FxInvalidParameterException("parentXPath", "ex.structure.assignment.noGroup", parentXPath);
         }
         if (alias == null)
             setXPath("/"); //got to be root
         if (parent == null)
             parentGroupAssignment = null;
         else
             parentGroupAssignment = (FxGroupAssignment) parent;
         isNew = true;
     }
 
     /**
      * Is this a new instance?
      *
      * @return new instance?
      */
     public boolean isNew() {
         return isNew;
     }
 
     /**
      * Set the enabled flag
      *
      * @param enabled enabled flag
      * @return this
      */
     public FxGroupAssignmentEdit setEnabled(boolean enabled) {
         this.enabled = enabled;
         return this;
     }
 
     /**
      * Set the multiplicity of this assignment. May only be set if the group this assignment belongs to allows
      * overriding the multiplicity.
      *
      * @param multiplicity new multiplicity
      * @return this
      * @throws FxInvalidParameterException on errors
      */
     public FxGroupAssignmentEdit setMultiplicity(FxMultiplicity multiplicity) throws FxInvalidParameterException {
         if (!getGroup().mayOverrideBaseMultiplicity() && !multiplicity.equals(getGroup().getMultiplicity()))
             throw new FxInvalidParameterException("MULTIPLICITY", "ex.structure.override.group.forbidden", "Multiplicity", getGroup().getName());
         this.multiplicity = multiplicity;
         return this;
     }
 
     /**
      * Set the default multiplicity (the number of elements created upon initialization)
      *
      * @param defaultMultiplicity the default multiplicity
      * @return this
      */
     public FxGroupAssignmentEdit setDefaultMultiplicity(int defaultMultiplicity) {
         if (this.getMultiplicity().isValid(defaultMultiplicity)) {
             this.defaultMultiplicity = defaultMultiplicity;
             return this;
         }
         if (defaultMultiplicity < this.getMultiplicity().getMin())
             this.defaultMultiplicity = this.getMultiplicity().getMin();
         if (defaultMultiplicity > this.getMultiplicity().getMax())
             this.defaultMultiplicity = this.getMultiplicity().getMax();
         return this;
     }
 
     /**
      * Set the new absolute position
      *
      * @param position new absolute position
      * @return this
      */
     public FxGroupAssignmentEdit setPosition(int position) {
         this.position = position;
         return this;
     }
 
     /**
      * Set a new alias, will affect the xpath as well
      *
      * @param alias new alias
      * @return this
      * @throws FxInvalidParameterException on errors
      */
     public FxGroupAssignmentEdit setAlias(String alias) throws FxInvalidParameterException {
         if (StringUtils.isEmpty(alias))
             throw new FxInvalidParameterException("ALIAS", "ex.structure.assignment.noAlias");
        //only react to alias changes
        if (!this.alias.trim().toUpperCase().equals(alias.trim().toUpperCase())) {
            this.alias = alias.trim().toUpperCase();
            List<XPathElement> xpe = XPathElement.split(this.XPath);
            xpe.set(xpe.size() - 1, new XPathElement(this.alias, 1, true));
            this.XPath = XPathElement.toXPathNoMult(xpe);
        }
         return this;
     }
 
     /**
      * Set the XPath, will affect the alias as well
      *
      * @param XPath new XPath
      * @return this
      * @throws FxInvalidParameterException on errors
      */
     public FxGroupAssignmentEdit setXPath(String XPath) throws FxInvalidParameterException {
         if (StringUtils.isEmpty(XPath) || !XPathElement.isValidXPath(XPath))
             throw new FxInvalidParameterException("XPATH", "ex.structure.assignment.noXPath");
         this.XPath = XPath.trim().toUpperCase();
         //synchronize back the alias unless we're the virtual root group
         if (!"/".equals(this.XPath))
             this.alias = XPathElement.lastElement(this.XPath).getAlias();
         return this;
     }
 
     /**
      * Set the label
      *
      * @param label label
      * @return this
      */
     public FxGroupAssignmentEdit setLabel(FxString label) {
         this.label = label;
         return this;
     }
 
     /**
      * Set the hint message
      *
      * @param hint hint message
      * @return this
      */
     public FxGroupAssignmentEdit setHint(FxString hint) {
         this.hint = hint;
         return this;
     }
 
     /**
      * Set the GroupMode for this group assignment (any-of, one-of)
      *
      * @param mode group mode
      * @return this
      */
     public FxGroupAssignmentEdit setMode(GroupMode mode) {
         this.mode = mode;
         return this;
     }
 
     /**
      * Create a new FxGroupAssignmentEdit from an existing FxGroupAssignment as a new one for a given type with a
      * new alias and a given parentXPath
      *
      * @param ga          original group assignment
      * @param type        type to assign it
      * @param alias       new alias
      * @param parentXPath parent XPath within the type to assign
      * @param parent      optional parent assignment if already known
      * @return new FxGroupAssignmentEdit
      * @throws com.flexive.shared.exceptions.FxNotFoundException
      *          if parentXPath is invalid
      * @throws com.flexive.shared.exceptions.FxInvalidParameterException
      *          if parentXPath is invalid
      */
     public static FxGroupAssignmentEdit createNew(FxGroupAssignment ga, FxType type, String alias, String parentXPath, FxAssignment parent) throws FxNotFoundException, FxInvalidParameterException {
         return new FxGroupAssignmentEdit(ga, type, alias, parentXPath, parent);
     }
 
     /**
      * Create a new FxGroupAssignmentEdit from an existing FxGroupAssignment as a new one for a given type with a
      * new alias and a given parentXPath
      *
      * @param ga          original group assignment
      * @param type        type to assign it
      * @param alias       new alias
      * @param parentXPath parent XPath within the type to assign
      * @return new FxGroupAssignmentEdit
      * @throws com.flexive.shared.exceptions.FxNotFoundException
      *          if parentXPath is invalid
      * @throws com.flexive.shared.exceptions.FxInvalidParameterException
      *          if parentXPath is invalid
      */
     public static FxGroupAssignmentEdit createNew(FxGroupAssignment ga, FxType type, String alias, String parentXPath) throws FxNotFoundException, FxInvalidParameterException {
         return new FxGroupAssignmentEdit(ga, type, alias, parentXPath, null);
     }
 
 
     /**
      * Sets the parent group assignment.
      *
      * @param parent the parent group assignment.
      */
     public void setParentGroupAssignment(FxGroupAssignment parent) {
         this.parentGroupAssignment = parent;
     }
 
     /**
      * Remove any references of derived usage
      */
     public void clearDerivedUse() {
         this.parentGroupAssignment = null;
         this.baseAssignment = NO_BASE;
     }
 
     /**
      * Set an option
      *
      * @param key   option key
      * @param value value of the option
      * @return the assignment itself, useful for chained calls
      * @throws FxInvalidParameterException if the property does not allow overriding
      */
     public FxGroupAssignmentEdit setOption(String key, String value) throws FxInvalidParameterException {
 
         FxStructureOption gOpt = getGroup().getOption(key);
         if (gOpt.isSet() && !gOpt.isOverrideable())
             throw new FxInvalidParameterException(key, "ex.structure.override.group.forbidden", key, getGroup().getName());
 
         FxStructureOption.setOption(options, key, true, value);
         return this;
     }
 
     /**
      * Set a boolean option
      *
      * @param key   option key
      * @param value value of the option
      * @return the assignment itself, useful for chained calls
      * @throws FxInvalidParameterException if the property does not allow overriding
      */
     public FxGroupAssignmentEdit setOption(String key, boolean value) throws FxInvalidParameterException {
 
         FxStructureOption gOpt = getGroup().getOption(key);
         if (gOpt.isSet() && !gOpt.isOverrideable())
             throw new FxInvalidParameterException(key, "ex.structure.override.group.forbidden", key, getGroup().getName());
 
         FxStructureOption.setOption(options, key, true, value);
         return this;
     }
 
     /**
      * Clear an option entry
      *
      * @param key option name
      */
     public void clearOption(String key) {
         FxStructureOption.clearOption(options, key);
     }
 
     /**
      * Assign options
      *
      * @param options options to assign
      */
     public void setOptions(List<FxStructureOption> options) {
         this.options = options;
     }
 
     /**
      * Returns the group of this assignment as editable.
      *
      * @return the editable group object.
      */
     public FxGroupEdit getGroupEdit() {
         if (!(group instanceof FxGroupEdit))
             throw new FxApplicationException("ex.structure.noEditableGroup").asRuntimeException();
         else return (FxGroupEdit) group;
     }
 
 }
