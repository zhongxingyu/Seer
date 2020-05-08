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
 
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.value.*;
 
 import java.io.Serializable;
 import java.util.List;
 
 /**
  * property definition
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxProperty extends FxStructureElement implements Serializable {
     private static final long serialVersionUID = 5343222808050017626L;
     protected ACL ACL;
     protected boolean overrideACL;
     protected FxDataType dataType;
     protected boolean fulltextIndexed;
     protected FxType referencedType;
     protected FxSelectList referencedList;
     protected FxValue defaultValue;
     protected boolean systemInternal;
     protected UniqueMode uniqueMode;
 
     public FxProperty(long id, String name, FxMultiplicity multiplicity, ACL acl, FxDataType dataType) {
         this(id, name, null, null, false, false, multiplicity, false, acl, dataType, null, false, null, null,
                 UniqueMode.None, FxStructureOption.getEmptyOptionList(5));
     }
 
     public FxProperty(long id, String name, FxString label, FxString hint, boolean systemInternal, boolean overrideBaseMultiplicity, FxMultiplicity multiplicity,
                       boolean overrideACL, ACL ACL, FxDataType dataType, FxValue defaultValue, boolean fulltextIndexed,
                       FxType referencedType, FxSelectList referencedList, UniqueMode uniqueMode, List<FxStructureOption> options) {
         super(id, name, label, hint, overrideBaseMultiplicity, multiplicity, options);
         this.systemInternal = systemInternal;
         this.overrideACL = overrideACL;
         this.ACL = ACL;
         this.dataType = dataType;
         this.referencedType = referencedType;
         this.referencedList = referencedList;
         if (dataType != null && defaultValue != null) {
             //default value can only be determined once the referenced list is known
             if ((dataType == FxDataType.SelectOne || dataType == FxDataType.SelectMany) &&
                     referencedList == null)
                 this.defaultValue = null;
             else
                 this.defaultValue = defaultValue;
         } else
             this.defaultValue = defaultValue;
         this.multiplicity = multiplicity;
         this.fulltextIndexed = fulltextIndexed;
         this.uniqueMode = uniqueMode;
     }
 
     public FxPropertyEdit asEditable() {
         return new FxPropertyEdit(this);
     }
 
     /**
      * May assignments override this properties ACL?
      *
      * @return if assignments may override this properties ACL?
      */
     public boolean mayOverrideACL() {
         return overrideACL;
     }
 
     /**
      * Get the ACL
      *
      * @return ACL
      */
     public ACL getACL() {
         return ACL;
     }
 
     /**
      * Get the data type
      *
      * @return data type
      */
     public FxDataType getDataType() {
         return dataType;
     }
 
     /**
      * Get the default value
      *
      * @return default value
      */
     public FxValue getDefaultValue() {
         if (defaultValue == null)
             return null;
         final FxValue copy = defaultValue.copy();
         updateEnvironmentData(copy);
         return copy;
     }
 
     /**
      * May assignments override this properties multilanguage setting?
      *
      * @return if assignments may override this properties multilanguage setting?
      */
     public boolean mayOverrideMultiLang() {
         return getOption(FxStructureOption.OPTION_MULTILANG).isOverrideable();
     }
 
     /**
      * Is this property available in multiple languages?
      *
      * @return property is available in multiple languages
      */
     public boolean isMultiLang() {
         return getOption(FxStructureOption.OPTION_MULTILANG).isValueTrue();
     }
 
     /**
      * May assignments override this properties searchable flag?
      *
      * @return if assignments may override this properties searchable flag?
      */
     public boolean mayOverrideSearchable() {
         return getOption(FxStructureOption.OPTION_SEARCHABLE).isOverrideable();
     }
 
     /**
      * Can this property be used in the visual query editor (UI hint)
      *
      * @return if property can be used in the visual query editor
      */
     public boolean isSearchable() {
         return getOption(FxStructureOption.OPTION_SEARCHABLE).isValueTrue();
     }
 
     /**
      * Is this a system-internal property like ID or VERSION?
      *
      * @return property is system internal
      */
     public boolean isSystemInternal() {
         return systemInternal;
     }
 
     /**
      * May assignments override this properties appearance in overviews?
      *
      * @return if assignments may override this properties appearance in overviews?
      */
     public boolean mayOverrideInOverview() {
         return getOption(FxStructureOption.OPTION_SHOW_OVERVIEW).isOverrideable();
     }
 
     /**
      * Does this property appear in overviews?
      *
      * @return if this property appears in overviews
      */
     public boolean isInOverview() {
         return getOption(FxStructureOption.OPTION_SHOW_OVERVIEW).isValueTrue();
     }
 
     /**
      * May assignments override this properties setting wether to use a HTML editor?
      *
      * @return if assignments may override this properties setting wether to use a HTML editor?
      */
     public boolean mayOverrideUseHTMLEditor() {
         return getOption(FxStructureOption.OPTION_HTML_EDITOR).isOverrideable();
     }
 
     /**
      * Use a HTML editor for this property?
      *
      * @return use HTML editor
      */
     public boolean isUseHTMLEditor() {
         return getOption(FxStructureOption.OPTION_HTML_EDITOR).isValueTrue();
     }
 
     /**
      * May assignments override this properties multiline setting?
      *
      * @return if assignments may override this properties multiline setting?
      */
     public boolean mayOverrideMultiLine() {
         return getOption(FxStructureOption.OPTION_MULTILINE).isOverrideable();
     }
 
     /**
      * Show as multiple lines in editors?
      *
      * @return if this property appears in multiple lines
      */
     public boolean isMultiLine() {
         FxStructureOption opt = getOption(FxStructureOption.OPTION_MULTILINE);
         if (opt.isSet()) {
             try {
                 return opt.getIntValue() > 0;
             } catch (Exception e) {
                 return false;
             }
         }
         return false;
     }
 
     /**
      * Get the number of multilines to display or 0 if multiline is not set
      *
      * @return number of multilines to display or 0 if multiline is not set
      */
     public int getMultiLines() {
         FxStructureOption opt = getOption(FxStructureOption.OPTION_MULTILINE);
         if (opt.isSet()) {
             try {
                 return opt.getIntValue();
             } catch (Exception e) {
                 return 0;
             }
         }
         return 0;
     }
 
     /**
      * Shortcut to determine if a max. input length has been set
      *
      * @return has a max. input length been set?
      */
     public boolean hasMaxLength() {
         return hasOption(FxStructureOption.OPTION_MAXLENGTH);
     }
 
     /**
      * Shortcut to get the maximum input length
      *
      * @return maximum input length
      */
     public int getMaxLength() {
         return getOption(FxStructureOption.OPTION_MAXLENGTH).getIntValue();
     }
 
     /**
      * May assignments override this properties maxLength setting?
      *
      * @return if assignments may override this properties maxLength setting?
      */
     public boolean mayOverrideMaxLength() {
         return getOption(FxStructureOption.OPTION_MAXLENGTH).isOverrideable();
     }
 
     /**
      * Is this property fulltext indexed?
      *
      * @return is property fulltext indexed?
      */
     public boolean isFulltextIndexed() {
         if (hasOption(FxStructureOption.OPTION_FULLTEXT)) {
             return getOption(FxStructureOption.OPTION_FULLTEXT).isValueTrue();
         }
         return fulltextIndexed;
     }
 
     /**
      * Does this property references a FxType
      *
      * @return if this property references a FxType
      */
     public boolean hasReferencedType() {
         return referencedType != null;
     }
 
     /**
      * Get referenced FxType
      *
      * @return referenced FxType
      */
     public FxType getReferencedType() {
         return referencedType;
     }
 
     /**
      * Does this property reference a FxSelectList
      *
      * @return if this property references a FxSelectList
      */
     public boolean hasReferencedList() {
         return referencedList != null;
     }
 
     /**
      * Get referenced FxSelectList
      *
      * @return referenced FxSelectList
      */
     public FxSelectList getReferencedList() {
         return referencedList;
     }
 
     /**
      * Get the uniqueness mode of this property
      *
      * @return uniqueness mode of this property
      */
     public UniqueMode getUniqueMode() {
         return uniqueMode;
     }
 
     /**
      * (Internal!) method to resolve referenced type in the second stage of loading the environment
      *
      * @param environment reference to the environment to resolve the referenced type
      */
     public void resolveReferencedType(FxEnvironment environment) {
         if (this.referencedType == null || environment == null)
             return;
         this.referencedType = environment.getType(referencedType.getId());
     }
 
     /**
      * Get an empty FxValue object for this property
      *
      * @return empty FxValue object
      */
     public FxValue getEmptyValue() {
         return getEmptyValue(isMultiLang());
     }
 
     /**
      * Get an empty FxValue instance for this property in single or multi language mode.
      * To be called from property assignments only!
      *
      * @param multiLang multi language
      * @return empty FxValue instance for the data type
      */
     protected FxValue getEmptyValue(boolean multiLang) {
         return getEmptyValue(multiLang, FxLanguage.DEFAULT_ID);
     }
 
 
     /**
      * Updates miscellaneous FxValue information that has to be updated from the environment,
      * like select lists for FxSelectOne objects.
      *
      * @param value  the value to be updated
      */
     public void updateEnvironmentData(FxValue value) {
         // set current select list for select list default values
         if (value instanceof FxSelectOne) {
             ((FxSelectOne) value).setSelectList(getReferencedList());
         } else if (value instanceof FxSelectMany) {
             ((FxSelectMany) value).setSelectList(getReferencedList());
         }
     }
 
     /**
      * Get an empty FxValue instance for this property in single or multi language mode.
      * To be called from property assignments only!
      *
      * @param multiLang multi language
      * @param lang      language to initialize this value for
      * @return empty FxValue instance for the data type
      */
     protected FxValue getEmptyValue(boolean multiLang, long lang) {
         if (this.dataType == null)
             throw new FxApplicationException("ex.structure.dataType.property", getName()).asRuntimeException();
         final FxValue value;
         switch (this.getDataType()) {
             case HTML:
                 value = new FxHTML(multiLang, lang, FxHTML.EMPTY).setEmpty();
                 break;
             case String1024:
             case Text:
                 value = new FxString(multiLang, lang, FxString.EMPTY).setEmpty();
                 break;
             case Number:
                 value = new FxNumber(multiLang, lang, FxNumber.EMPTY).setEmpty();
                 break;
             case LargeNumber:
                 value = new FxLargeNumber(multiLang, lang, FxLargeNumber.EMPTY).setEmpty();
                 break;
             case Float:
                 value = new FxFloat(multiLang, lang, FxFloat.EMPTY).setEmpty();
                 break;
             case Double:
                 value = new FxDouble(multiLang, lang, FxDouble.EMPTY).setEmpty();
                 break;
             case Date:
                 value = new FxDate(multiLang, lang, FxDate.EMPTY).setEmpty();
                 break;
             case DateTime:
                 value = new FxDateTime(multiLang, lang, FxDateTime.EMPTY).setEmpty();
                 break;
             case DateRange:
                 value = new FxDateRange(multiLang, lang, FxDateRange.EMPTY).setEmpty();
                 break;
             case DateTimeRange:
                 value = new FxDateTimeRange(multiLang, lang, FxDateTimeRange.EMPTY).setEmpty();
                 break;
             case Boolean:
                 value = new FxBoolean(multiLang, lang, FxBoolean.EMPTY).setEmpty();
                 break;
             case Binary:
                 value = new FxBinary(multiLang, lang, FxBinary.EMPTY).setEmpty();
                 break;
             case Reference:
                 value = new FxReference(multiLang, lang, FxReference.EMPTY).setEmpty();
                 break;
             case SelectOne:
                if (this.getReferencedList().getItems().size() > 0) {
                    value = new FxSelectOne(multiLang, lang, this.getReferencedList().getItems().get(0)).setEmpty();
                } else {
                    value = new FxSelectOne(lang, multiLang).setEmpty();
                }
                 break;
             case SelectMany:
                 value = new FxSelectMany(multiLang, lang, new SelectMany(this.getReferencedList())).setEmpty();
                 break;
             case InlineReference:
             default:
                 throw new FxNotFoundException("ex.structure.datatype.notImplemented", this.getDataType()).asRuntimeException();
         }
         value.setXPath(getName()); //provide some dummy info for reference editors, etc.
         updateEnvironmentData(value);
         return value;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object obj) {
         return obj instanceof FxProperty && this.getId() == ((FxProperty) obj).getId();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
         return (int) this.getId();
     }
 
     /**
      * Returns if the default value of this property is set.
      *
      * @return  if the default value of this property is set.
      */
     public boolean isDefaultValueSet() {
         return this.defaultValue !=null;
     }
 }
