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
 
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.value.FxValue;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.List;
 
 
 /**
  * FxProperty used for structure editing
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxPropertyEdit extends FxProperty {
 
     private static final long serialVersionUID = 2628385928536891500L;
     private boolean isNew;
     private boolean autoUniquePropertyName = false;
     private int defaultMultiplicity = 1;
 
     /**
      * Make an editable instance of an existing property
      *
      * @param prop original property
      */
     public FxPropertyEdit(FxProperty prop) {
         super(prop.getId(), prop.getName(), prop.getLabel().copy(), prop.getHint().copy(), prop.isSystemInternal(),
                 prop.mayOverrideBaseMultiplicity(), new FxMultiplicity(prop.getMultiplicity()), prop.mayOverrideACL(), new ACL(prop.getACL()),
                 prop.getDataType(), prop.getDefaultValue().copy(), prop.isFulltextIndexed(), prop.getReferencedType(),
                 prop.getReferencedList(), prop.getUniqueMode(), FxStructureOption.cloneOptions(prop.options));
         isNew = false;
     }
 
     /**
      * Create a clone of an existing property with a new name
      *
      * @param base                   property to clone
      * @param newName                new name to assign
      * @param autoUniquePropertyName if the property name already exists, try to find a unique one (by appending "_([counter])").
      *                               Useful if creating property assignments and the used property name is not important
      * @return FxPropertyEdit
      * @throws FxInvalidParameterException on errors
      */
     public static FxPropertyEdit createNew(FxProperty base, String newName, boolean autoUniquePropertyName)
             throws FxInvalidParameterException {
         FxPropertyEdit ret = new FxPropertyEdit(base).setName(newName).setAutoUniquePropertyName(autoUniquePropertyName);
         ret.isNew = true;
         return ret;
     }
 
     /**
      * Create a clone of an existing property with a new name
      *
      * @param base    property to clone
      * @param newName new name to assign (has to be mandatorwide unique!)
      * @return FxPropertyEdit
      */
     public static FxPropertyEdit createNew(FxProperty base, String newName) {
         FxPropertyEdit ret = new FxPropertyEdit(base).setName(newName);
         ret.isNew = true;
         return ret;
     }
 
     /**
      * Constructor
      *
      * @param name                     (mandator wide unique) name of the property
      * @param description              description
      * @param hint                     hint message
      * @param overrideBaseMultiplicity are assignments allowed to override this properties multiplicity?
      * @param multiplicity             multiplicity
      * @param overrideACL              are assignments allowed to override this properties ACL?
      * @param ACL                      ACL
      * @param dataType                 FxDataType
      * @param defaultValue             default value to assign
      * @param fulltextIndexed          should vlues of this property be fulltext indexed?
      * @param referencedType           if dataType is reference this is the referenced type
      * @param referencedList           if dataType is (multi) select list this is the referenced list
      * @param options                  this properties options
      */
     private FxPropertyEdit(String name, FxString description, FxString hint, boolean overrideBaseMultiplicity,
                            FxMultiplicity multiplicity, boolean overrideACL, ACL ACL, FxDataType dataType, FxValue defaultValue,
                            boolean fulltextIndexed, FxType referencedType, FxSelectList referencedList, List<FxStructureOption> options) {
         super(-1, name, description, hint, false, overrideBaseMultiplicity, multiplicity, overrideACL, ACL, dataType, defaultValue,
                 fulltextIndexed, referencedType, referencedList, UniqueMode.None, options);
         setName(name);
         setSearchable(true); //default is searchable
         setOptionOverrideable(FxStructureOption.OPTION_SEARCHABLE, true);
         //if the use html editor option is not set and the datatype is html, set it
         boolean hasHTMLOption = false;
         if (options == null)
             options = FxStructureOption.getEmptyOptionList(5);
         for (FxStructureOption option : options) {
             if (FxStructureOption.OPTION_HTML_EDITOR.equals(option.getKey()))
                 hasHTMLOption = true;
         }
         if (dataType == FxDataType.HTML && !hasHTMLOption) {
             setUseHTMLEditor(true);
             setOptionOverrideable(FxStructureOption.OPTION_HTML_EDITOR, true);
         }
         this.isNew = true;
     }
 
     /**
      * Create a new FxPropertyEdit instance
      *
      * @param name                     (mandator wide unique) name of the property
      * @param description              description
      * @param hint                     hint message
      * @param overrideBaseMultiplicity are assignments allowed to override this properties multiplicity?
      * @param multiplicity             multiplicity
      * @param overrideACL              are assignments allowed to override this properties ACL?
      * @param acl                      ACL
      * @param dataType                 FxDataType
      * @param defaultValue             default value to assign
      * @param fulltextIndexed          should values of this property be fulltext indexed?
      * @param referencedType           if dataType is reference this is the referenced type
      * @param referencedList           if dataType is (multi) select list this is the referenced list
      * @param options                  this properties options
      * @return FxPropertyEdit instance
      */
     public static FxPropertyEdit createNew(String name, FxString description, FxString hint,
                                            boolean overrideBaseMultiplicity, FxMultiplicity multiplicity, boolean overrideACL,
                                            ACL acl, FxDataType dataType, FxString defaultValue, boolean fulltextIndexed,
                                            FxType referencedType, FxSelectList referencedList, List<FxStructureOption> options) {
         return new FxPropertyEdit(name, description, hint, overrideBaseMultiplicity, multiplicity, overrideACL, acl,
                 dataType, defaultValue, fulltextIndexed, referencedType, referencedList, options);
     }
 
     /**
      * Create a new FxPropertyEdit instance - simplified with many defaults (everything set to true except useHTMLEditor)
      *
      * @param name         (mandator wide unique) name of the property
      * @param description  description
      * @param hint         hint message
      * @param multiplicity multiplicity
      * @param acl          ACL
      * @param dataType     FxDataType
      * @return FxPropertyEdit instance
      */
     public static FxPropertyEdit createNew(String name, FxString description, FxString hint,
                                            FxMultiplicity multiplicity, ACL acl, FxDataType dataType) {
         return new FxPropertyEdit(name, description, hint, true, multiplicity, true, acl,
                 dataType, null, true, null, null, FxStructureOption.getEmptyOptionList(5));
     }
 
 
     /**
      * Is this a new property
      *
      * @return if this is a new property
      */
     public boolean isNew() {
         return isNew;
     }
 
     /**
      * If creating a new property and the property name (not assignment! xalias will remain!) is taken probe for and
      * use a unique property name?
      *
      * @return probe for unique property name if a property with the requested xalias already exist?
      */
     public boolean isAutoUniquePropertyName() {
         return autoUniquePropertyName;
     }
 
     /**
      * If creating a new property and the property name (not assignment! xalias will remain!) is taken probe for and
      * use a unique property name?
      *
      * @param autoUniquePropertyName probe for a unique propery name if a property with the requested xalias already exist?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setAutoUniquePropertyName(boolean autoUniquePropertyName) {
         this.autoUniquePropertyName = autoUniquePropertyName;
         return this;
     }
 
     /**
      * Set the properties name (has to be unique!)
      *
      * @param name (unique) name of the property
      * @return the property itself, useful for chained calls
      * @throws com.flexive.shared.exceptions.FxRuntimeException
      *          if the name is empty (uniqueness will be checked during save operation)
      */
     public FxPropertyEdit setName(String name) {
         if (StringUtils.isEmpty(name))
             throw new FxInvalidParameterException("NAME", "ex.general.parameter.empty", "name").asRuntimeException();
         name = name.trim().toUpperCase();
         this.name = name;
         return this;
     }
 
     /**
      * Set the label
      *
      * @param label the label
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setLabel(FxString label) {
         this.label = label;
         return this;
     }
 
     /**
      * Set the hint message
      *
      * @param hint hint message
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setHint(FxString hint) {
         this.hint = hint;
         return this;
     }
 
     /**
      * Set the default value
      *
      * @param defaultValue the new default value
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setHint(FxValue defaultValue) {
         this.defaultValue = defaultValue;
         return this;
     }
 
     /**
      * Set the multiplicity
      *
      * @param multiplicity multiplicity
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setMultiplicity(FxMultiplicity multiplicity) {
         this.multiplicity = multiplicity;
         return this;
     }
 
     /**
      * Set if assignments are allowed to override this properties multiplicity?
      *
      * @param overrideMultiplicity are assignments allowed to override this properties multiplicity?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setOverrideMultiplicity(boolean overrideMultiplicity) {
         this.overrideMultiplicity = overrideMultiplicity;
         return this;
     }
 
     /**
      * Set the ACL
      *
      * @param acl ACL
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setACL(ACL acl) {
         this.ACL = acl;
         return this;
     }
 
     /**
      * Set if assignments of this propery are allowed to override this properties ACL?
      *
      * @param overrideACL are assignments allowed to override this properties ACL?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setOverrideACL(boolean overrideACL) {
         this.overrideACL = overrideACL;
         return this;
     }
 
     /**
      * Set the data type
      *
      * @param dataType data type
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setDataType(FxDataType dataType) {
         if( this.dataType == dataType)
             return this; //no changes
 
         this.dataType = dataType;
         this.defaultValue = getEmptyValue();
         //if the datatype is html, set the option to use the html editor 
         if (dataType == FxDataType.HTML)
             setUseHTMLEditor(true);
         return this;
     }
 
     /**
      * Set multilinguality
      *
      * @param multiLang are values of this property multilingual?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setMultiLang(boolean multiLang) {
         FxStructureOption.setOption(options, FxStructureOption.OPTION_MULTILANG, mayOverrideMultiLang(), multiLang);
         return this;
     }
 
     /**
      * Set if assignments are allowed to override this properties multilanguage setting?
      *
      * @param overrideMultiLang are assignments allowed to override this properties multilanguage setting?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setOverrideMultiLang(boolean overrideMultiLang) {
         FxStructureOption.setOption(options, FxStructureOption.OPTION_MULTILANG, overrideMultiLang, isMultiLang());
         return this;
     }
 
     /**
      * Set multiline display ability
      *
      * @param multiLine render property in multiple lines?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setMultiLine(boolean multiLine) {
         FxStructureOption.setOption(options, FxStructureOption.OPTION_MULTILINE, mayOverrideMultiLine(), multiLine);
         return this;
     }
 
     /**
      * Set if assignments are allowed to override this properties multiline setting?
      *
      * @param overrideMultiLine are assignments allowed to override this properties multiline setting?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setOverrideMultiLine(boolean overrideMultiLine) {
         FxStructureOption.setOption(options, FxStructureOption.OPTION_MULTILINE, overrideMultiLine, isMultiLine());
         return this;
     }
 
     /**
      * Shortcut to set the maximum input length (if applicable to the component)
      *
      * @param maxLength desired maximum input length
      * @return the property itself, useful for chained calls
      * @throws FxInvalidParameterException on errors
      */
     public FxPropertyEdit setMaxLength(int maxLength) throws FxInvalidParameterException {
         FxStructureOption.setOption(options, FxStructureOption.OPTION_MAXLENGTH, mayOverrideMaxLength(), String.valueOf(maxLength));
         return this;
     }
 
     /**
      * Set if assignments are allowed to override this properties maxLength setting?
      *
      * @param overrideMaxLEngth are assignments allowed to override this properties maxLength setting?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setOverrideMaxLength(boolean overrideMaxLEngth) {
         FxStructureOption.setOption(options, FxStructureOption.OPTION_MAXLENGTH, overrideMaxLEngth, String.valueOf(getMaxLength()));
         return this;
     }
 
     /**
      * Set if  this property be used in the visual query editor (UI hint)
      *
      * @param searchable property can be used in the visual query editor
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setSearchable(boolean searchable) {
         FxStructureOption.setOption(options, FxStructureOption.OPTION_SEARCHABLE, mayOverrideSearchable(), searchable);
         return this;
     }
 
     /**
      * Set if assignments are allowed to override this properties multilanguage setting?
      *
      * @param overrideSearchable are assignments allowed to override this properties multilanguage setting?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setOverrideSearchable(boolean overrideSearchable) {
         FxStructureOption.setOption(options, FxStructureOption.OPTION_SEARCHABLE, overrideSearchable, isSearchable());
         return this;
     }
 
     /**
      * Set overview appearance setting
      *
      * @param inOverview overview appearance setting
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setInOverview(boolean inOverview) {
         FxStructureOption.setOption(options, FxStructureOption.OPTION_SHOW_OVERVIEW, mayOverrideInOverview(), inOverview);
         return this;
     }
 
     /**
      * Set if assignments are allowed to override this properties appearance in overviews?
      *
      * @param overrideOverview are assignments allowed to override this properties appearance in overviews?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setOverrideOverview(boolean overrideOverview) {
         FxStructureOption.setOption(options, FxStructureOption.OPTION_SHOW_OVERVIEW, overrideOverview, isInOverview());
         return this;
     }
 
     /**
      * Set if to use an HTML editor to edit values of this property?
      *
      * @param useHTMLEditor use HTML editor to edit values of this property?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setUseHTMLEditor(boolean useHTMLEditor) {
         FxStructureOption.setOption(options, FxStructureOption.OPTION_HTML_EDITOR, mayOverrideInOverview(), useHTMLEditor);
         return this;
     }
 
     /**
      * Set if assignments are allowed to override this properties use of HTML editor setting?
      *
      * @param overrideHTMLEditor are assignments allowed to override this properties use of HTML editor setting?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setOverrideHTMLEditor(boolean overrideHTMLEditor) {
         FxStructureOption.setOption(options, FxStructureOption.OPTION_HTML_EDITOR, overrideHTMLEditor, isUseHTMLEditor());
         return this;
     }
 
     /**
      * Set an option
      *
      * @param key          option key
      * @param overrideable is the option overrideable from assignments?
      * @param value        value of the option
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setOption(String key, boolean overrideable, String value) {
         FxStructureOption.setOption(options, key, overrideable, value);
         return this;
     }
 
     /**
      * Set a boolean option
      *
      * @param key          option key
      * @param overrideable is the option overrideable from assignments?
      * @param value        value of the option
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setOption(String key, boolean overrideable, boolean value) {
         FxStructureOption.setOption(options, key, overrideable, value);
         return this;
     }
 
     /**
      * Change the overrideable status of an option, will only have effect if the option exists!
      *
      * @param key          option key
      * @param overrideable overrideable status
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setOptionOverrideable(String key, boolean overrideable) {
         FxStructureOption opt = getOption(key);
         if (opt.isSet())
             opt.overrideable = overrideable;
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
      * Set if values of this property should be fulltext indexed?
      *
      * @param fulltextIndexed should values of this property be fulltext indexed?
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setFulltextIndexed(boolean fulltextIndexed) {
         this.fulltextIndexed = fulltextIndexed;
         return this;
     }
 
     /**
      * Set the uniqueness mode of this property
      *
      * @param uniqueMode uniqueness mode
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setUniqueMode(UniqueMode uniqueMode) {
         this.uniqueMode = uniqueMode;
         return this;
     }
 
     /**
      * If this propery is a reference to a FxType, set the referenced type
      *
      * @param referencedType if dataType is reference this is the referenced type
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setReferencedType(FxType referencedType) {
         this.referencedType = referencedType;
         return this;
     }
 
     /**
      * If this propery is a (multi) select list, set the referenced list
      *
      * @param referencedList if dataType is a (multi) select list, set the referenced list
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setReferencedList(FxSelectList referencedList) {
        this.referencedList = referencedList;
         return this;
     }
 
     /**
      * Set the default value for this propery
      *
      * @param value default value
      * @return the property itself, useful for chained calls
      */
     public FxPropertyEdit setDefaultValue(FxValue value) {
         this.defaultValue = value;
         return this;
     }
 
     /**
      * Perform some consistency checks, called internally!
      */
     public void checkConsistency() {
         switch (this.getDataType()) {
             case Reference:
                 //check if a reference is set
                 if (!hasReferencedType())
                     throw new FxInvalidParameterException("referencedType", "ex.structure.property.missing.type",
                             this.getName()).asRuntimeException();
                 break;
             case SelectOne:
             case SelectMany:
                 //check if select list is set
                 if (!hasReferencedList())
                     throw new FxInvalidParameterException("referencedList", "ex.structure.property.missing.list",
                             this.getName()).asRuntimeException();
                 break;
             default:
                 break; //no checks
         }
     }
 
     /**
      * <b>This value set will only be used when creating a new property and will be set for the created assignment!</b>
      * Set the default multiplicity (used i.e. in user interfaces editors and determines the amount of values that will
      * be initialized when creating an empty element).
      * <p/>
      * If the set value is &lt; min or &gt; max multiplicity of this assignment it will
      * be auto adjusted to the next valid value without throwing an exception
      *
      * @param defaultMultiplicity the default multiplicity
      * @return this
      */
     public FxPropertyEdit setAssignmentDefaultMultiplicity(int defaultMultiplicity) {
         this.defaultMultiplicity = defaultMultiplicity;
         return this;
     }
 
     /**
      * Get the created assignments default multiplicity
      *
      * @return default multiplicity
      */
     public int getAssignmentDefaultMultiplicity() {
         return defaultMultiplicity;
     }
 
     /**
      * Assign options
      *
      * @param options options to assign
      */
     public void setOptions(List<FxStructureOption> options) {
         this.options = options;
     }
 }
