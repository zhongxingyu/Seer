 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
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
 package com.flexive.war.beans.admin.structure;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.beans.ActionBean;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.shared.*;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.scripting.FxScriptInfo;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.value.FxValue;
 import com.flexive.war.javascript.tree.StructureTreeWriter;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.faces.model.SelectItem;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Bean behind propertyAssignmentEditor.xhtml, propertyEditor.xhtml and propertyOptionEditor to
  * edit FxPropertyAssignment and FxProperty objects
  *
  * @author Gerhard Glos (gerhard.glos@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 
 
 public class PropertyEditorBean implements ActionBean {
     private static final Log LOG = LogFactory.getLog(PropertyEditorBean.class);
     //private String gotoPropertyAssignment = null;
     private long propertyId = -1;
     //private FxLanguage defaultLanguage = null;
     private String assignmentOptionValue = null;
     private String assignmentOptionKey = null;
     private String propertyOptionValue = null;
     private String propertyOptionKey = null;
     private boolean propertyOptionOverridable = true;
     private OptionWrapper.WrappedOption optionFiler = null;
     FxPropertyAssignmentEdit assignment = null;
     private String minMultiplicity = null;
     private String maxMultiplicity = null;
     private String propertyMinMultiplicity = null;
     private String propertyMaxMultiplicity = null;
     private OptionWrapper optionWrapper = null;
     //private boolean allowDefaultLanguage = false;
     private FxPropertyEdit property = null;
     private String parentXPath = null;
     private FxType parentType = null;
     //checker to restore system language
     private boolean originalLanguageSystemLanguage = false;
     //checker if current user may edit the property
     private boolean structureManagement = false;
     //checker for the editMode: if not in edit mode,
     // save and delete buttons are not rendered by the gui
     private boolean editMode = false;
     //assignment script editor tab
     private ScriptListWrapper scriptWrapper = null;
     private int scriptListFiler = -1;
     private FxScriptInfo selectedScriptInfo = null;
     private long selectedScriptEventId = -1;
     private boolean selectedDerivedUsage = false;
     private boolean selectedActive = true;
     private int defaultMultiplicity = -1;
 
     /*
     public String getGotoPropertyAssignment() {
         return gotoPropertyAssignment;
     }
     */
     /*
     public void setGotoPropertyAssignment(String gotoPropertyAssignment) {
         this.gotoPropertyAssignment = gotoPropertyAssignment;
     }
     */
 
     public boolean isSystemInternal() {
         return property.isSystemInternal();
     }
 
     public boolean isPropertyMayOverrideACL() {
         return property.mayOverrideACL();
     }
 
     public void setPropertyMayOverrideACL(boolean b) {
         property.setOverrideACL(b);
     }
 
     public boolean isPropertyMayOverrideBaseMultiplicity() {
         return property.mayOverrideBaseMultiplicity();
     }
 
     public void setPropertyMayOverrideBaseMultiplicity(boolean b) {
         property.setOverrideMultiplicity(b);
     }
 
     public FxPropertyEdit getProperty() {
         return property;
     }
 
     public void setProperty(FxPropertyEdit property) {
         this.property = property;
     }
 
     public void setAcl(long aclid) {
         try {
             assignment.setACL(CacheAdmin.getEnvironment().getACL(aclid));
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public long getPropertyAcl() {
         return getProperty().getACL().getId();
     }
 
     public void setPropertyAcl(long aclid) {
         try {
             getProperty().setACL(CacheAdmin.getEnvironment().getACL(aclid));
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public long getAcl() {
         return assignment.getACL().getId();
     }
 
     public String getAlias() {
         return assignment.getAlias();
     }
 
     public void setAlias(String alias) {
         try {
             assignment.setAlias(alias);
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public int getDefaultMultiplicity() {
         return defaultMultiplicity;
     }
 
     public void setDefaultMultiplicity(int defaultMultiplicity) {
         this.defaultMultiplicity = defaultMultiplicity;
     }
 
     public FxValue getDefaultValue() {
         //check if multi language settings have changed and adjust the default value
         if (assignment.isMultiLang() != assignment.getDefaultValue().isMultiLanguage()) {
             assignment.setDefaultValue(assignment.getEmptyValue());
             /*
             FxValue v = assignment.getEmptyValue();
             if (assignment.getDefaultValue().getBestTranslation() != null)
                 v.setValue(assignment.getDefaultValue().getBestTranslation());
             setDefaultValue(v);
             */
         }
         return assignment.getDefaultValue();
     }
 
     public void setDefaultValue(FxValue val) {
         if (val != null && assignment.getDefaultValue() != null) {
             if (!assignment.getDefaultValue().getClass().equals(assignment.getProperty().getEmptyValue().getClass())) {
                 if (!property.getEmptyValue().getClass().equals(property.getEmptyValue().getClass()))
                     property.setDefaultValue(property.getEmptyValue());
                 assignment.setDefaultValue(assignment.getProperty().getEmptyValue());
                 return;
             }
         }
         try {
             assignment.setDefaultValue(val);
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public FxValue getPropertyDefaultValue() {
         //check if multi language settings have changed and adjust the default value
         if (property.isMultiLang() != property.getDefaultValue().isMultiLanguage()) {
             property.setDefaultValue(property.getEmptyValue());
             /*
             FxValue v = property.getEmptyValue();
             if (property.getDefaultValue().getBestTranslation() != null)
                 v.setValue(property.getDefaultValue().getBestTranslation());
             setPropertyDefaultValue(v);
             */
         }
         return property.getDefaultValue();
     }
 
     public void setPropertyDefaultValue(FxValue val) {
         if (val == null)
             return;
         if (!val.getClass().equals(property.getEmptyValue().getClass())) {
             property.setDefaultValue(property.getEmptyValue());
             return;
         }
         try {
             if (val.getClass().equals(property.getDefaultValue().getClass()))
                 property.setDefaultValue(val);
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public FxString getDefaultLabel() {
         return assignment.getDisplayLabel();
     }
 
     public void setDefaultLabel(FxString label) {
         try {
             assignment.setLabel(label);
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
 
     public FxString getLabel() {
         return assignment.getLabel();
     }
 
     public void setLabel(FxString label) {
         try {
             assignment.setLabel(label);
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public FxString getPropertyLabel() {
         return getProperty().getLabel();
     }
 
     public void setPropertyLabel(FxString label) {
         try {
             getProperty().setLabel(label);
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public boolean isEnabled() {
         return assignment.isEnabled();
     }
 
     public void setEnabled(boolean b) {
         assignment.setEnabled(b);
     }
 
     public FxString getHint() {
         return assignment.getHint();
     }
 
     public void setHint(FxString hint) {
         try {
             assignment.setHint(hint);
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public FxString getPropertyHint() {
         return getProperty().getHint();
     }
 
     public void setPropertyHint(FxString hint) {
         try {
             getProperty().setHint(hint);
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public String getPropertyName() {
         return getProperty().getName();
     }
 
     public void setPropertyName(String name) {
         try {
             getProperty().setName(name);
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public String getMinMultiplicity() {
         return minMultiplicity;
     }
 
     public void setMinMultiplicity(String minMultiplicity) {
         this.minMultiplicity = minMultiplicity;
     }
 
     public String getMaxMultiplicity() {
         return maxMultiplicity;
     }
 
     public void setMaxMultiplicity(String maxMultiplicity) {
         this.maxMultiplicity = maxMultiplicity;
     }
 
     public String getPropertyMinMultiplicity() {
         return propertyMinMultiplicity;
     }
 
     public void setPropertyMinMultiplicity(String minMultiplicity) {
         this.propertyMinMultiplicity = minMultiplicity;
     }
 
     public String getPropertyMaxMultiplicity() {
         return propertyMaxMultiplicity;
     }
 
     public void setPropertyMaxMultiplicity(String maxMultiplicity) {
         this.propertyMaxMultiplicity = maxMultiplicity;
     }
 
     public OptionWrapper.WrappedOption getOptionFiler() {
         return optionFiler;
     }
 
     public void setOptionFiler(OptionWrapper.WrappedOption optionFiler) {
         this.optionFiler = optionFiler;
     }
 
     public FxPropertyAssignmentEdit getAssignment() {
         return assignment;
     }
 
     public FxLanguage getDefaultLanguage() {
         //workaround for the system language, which is not loadable
         if (assignment.getDefaultLanguage() == FxLanguage.SYSTEM_ID) {
             return FxLanguage.DEFAULT;
         } else {
             try {
                 return EJBLookup.getLanguageEngine().load(assignment.getDefaultLanguage());
             } catch (FxApplicationException e) {
                 new FxFacesMsgErr(e).addToContext();
                 return FxLanguage.DEFAULT;
             }
         }
     }
 
     public void setDefaultLanguage(FxLanguage defaultLanguage) {
         assignment.setDefaultLanguage(defaultLanguage.getId());
     }
 
     public void setAssignment(FxPropertyAssignmentEdit assignment) {
         this.assignment = assignment;
     }
 
     public FxDataType getPropertyDataType() {
         return getProperty().getDataType();
     }
 
     /**
      * Set the property's data type and update referenced type and referenced list accordingly
      *
      * @param d the data type
      */
     public void setPropertyDataType(FxDataType d) {
         property.setDataType(d);
         /*
         property.setDefaultValue(property.getDefaultValue());
         assignment.setDefaultValue(assignment.getDefaultValue());
         */
         if (d == FxDataType.HTML)
             optionWrapper.setOption(true, FxStructureOption.OPTION_HTML_EDITOR, true);
         if (!isPropertySelectList() && getPropertyReferencedList() != -1) {
             setPropertyReferencedList(-1);
         }
         if (!isPropertyReference() && getPropertyReferencedType() != -1) {
             setPropertyReferencedType(-1);
         }
     }
 
     public boolean isPropertyFulltextIndexed() {
         return getProperty().isFulltextIndexed();
     }
 
     public void setPropertyFulltextIndexed(boolean b) {
         getProperty().setFulltextIndexed(b);
     }
 
     public boolean isPropertyAutoUniquePropertyName() {
         return getProperty().isAutoUniquePropertyName();
     }
 
     public void setPropertyAutoUniquePropertyName(boolean b) {
         getProperty().setAutoUniquePropertyName(b);
     }
 
     public UniqueMode getPropertyUniqueMode() {
         return getProperty().getUniqueMode();
     }
 
     public boolean isPropertyUsedInInstance() {
         boolean result = true;
         try {
             result = !property.isNew() && (EJBLookup.getAssignmentEngine().getPropertyInstanceCount(propertyId) > 0);
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
         return result;
     }
 
     public void setPropertyUniqueMode(UniqueMode u) {
         getProperty().setUniqueMode(u);
     }
 
     public long getPropertyReferencedList() {
         if (property.getReferencedList() == null) {
             return -1;
         }
         return property.getReferencedList().getId();
     }
 
     public void setPropertyReferencedList(long id) {
         if (id == -1) {
             property.setReferencedList(null);
         } else {
             property.setReferencedList(CacheAdmin.getEnvironment().getSelectList(id));
         }
     }
 
     public boolean getPropertyHasReferencedType() {
         return property.hasReferencedType();
     }
 
     public long getPropertyReferencedType() {
         if (getProperty().getReferencedType() != null) {
             return getProperty().getReferencedType().getId();
         } else return -1;
     }
 
     /**
      * Returns a all available Types as List&lt;SelectItem&gt; and adds an empty element for null.
      *
      * @return available Types including a dummy value for null.
      */
     public List<SelectItem> getTypes() {
         List<FxType> typesList = CacheAdmin.getFilteredEnvironment().getTypes(true, true, true, false);
         final List<SelectItem> result = new ArrayList<SelectItem>(typesList.size() + 1);
         final UserTicket ticket = FxContext.get().getTicket();
         result.add(new SelectItem((long) -1, ""));
         for (SelectableObjectWithLabel item : typesList) {
             result.add(new SelectItem(item.getId(), item.getLabel().getBestTranslation(ticket)));
         }
         return result;
     }
 
     public void setPropertyReferencedType(long id) {
         if (id != -1) {
             getProperty().setReferencedType(CacheAdmin.getEnvironment().getType(id));
         } else {
             getProperty().setReferencedType(null);
         }
     }
 
     public long getPropertyId() {
         return propertyId;
     }
 
     public void setPropertyId(long propertyId) {
         this.propertyId = propertyId;
     }
 
     public String getAssignmentOptionValue() {
         return assignmentOptionValue;
     }
 
     public void setAssignmentOptionValue(String optionValue) {
         this.assignmentOptionValue = optionValue;
     }
 
     public String getAssignmentOptionKey() {
         return assignmentOptionKey;
     }
 
     public void setAssignmentOptionKey(String optionKey) {
         this.assignmentOptionKey = optionKey;
     }
 
     public boolean isPropertyOptionOverridable() {
         return propertyOptionOverridable;
     }
 
     public void setPropertyOptionOverridable(boolean propertyOptionOverridable) {
         this.propertyOptionOverridable = propertyOptionOverridable;
     }
 
     public void addAssignmentOption() {
         try {
             optionWrapper.addOption(optionWrapper.getAssignmentOptions(),
                     assignmentOptionKey, assignmentOptionValue, false);
             assignmentOptionKey = null;
             assignmentOptionValue = null;
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public String getPropertyOptionValue() {
         return propertyOptionValue;
     }
 
     public void setPropertyOptionValue(String propertyOptionValue) {
         this.propertyOptionValue = propertyOptionValue;
     }
 
     public String getPropertyOptionKey() {
         return propertyOptionKey;
     }
 
     public void setPropertyOptionKey(String propertyOptionKey) {
         this.propertyOptionKey = propertyOptionKey;
     }
 
     public void addPropertyOption() {
         try {
             optionWrapper.addOption(optionWrapper.getStructureOptions(),
                     propertyOptionKey, propertyOptionValue, propertyOptionOverridable);
             propertyOptionKey = null;
             propertyOptionValue = null;
             propertyOptionOverridable = true;
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public void deleteAssignmentOption() {
         optionWrapper.deleteOption(optionWrapper.getAssignmentOptions(), optionFiler);
     }
 
     public void deletePropertyOption() {
         optionWrapper.deleteOption(optionWrapper.getStructureOptions(), optionFiler);
     }
 
     public OptionWrapper getOptionWrapper() {
         return optionWrapper;
     }
 
     /**
      * Hack in order to use command buttons to submit the form values
      * and update the view of GUI elements
      */
     public void doNothing() {
     }
 
     /**
      * Returns all property assignments that are referencing this property which the
      * current user may see, excluding the system internal assignments.
      *
      * @return a list of property assignments that are referencing this property.
      */
     public List<FxPropertyAssignment> getReferencingPropertyAssignments() {
         List<FxPropertyAssignment> assignments = CacheAdmin.getFilteredEnvironment().getPropertyAssignments(true);
         List<FxPropertyAssignment> result = new ArrayList<FxPropertyAssignment>();
         for (FxPropertyAssignment assignment : assignments) {
             if (assignment.getProperty().getId() == property.getId() && !assignment.isSystemInternal()) {
                 result.add(assignment);
             }
         }
         return result;
     }
 
     /**
      * If the assignment is derived, return the base assignments XPath
      *
      * @return the base assignments XPath if derived
      */
     public String getBaseAssignmentXPath() {
         if (assignment.isDerivedAssignment())
             return CacheAdmin.getEnvironment().getAssignment(assignment.getBaseAssignmentId()).getXPath();
         else
             return "";
     }
 
     public boolean isPropertyMultiLanguage() {
         return property.isMultiLang();
     }
 
     public void setPropertyMultiLanguage(boolean b) throws FxInvalidParameterException {
         FxStructureOption propOption = property.getOption(FxStructureOption.OPTION_MULTILANG);
         boolean multilang = assignment != null ? isMultiLang() : propOption.isValueTrue();
         if (propOption.isSet()) {
             if (b == multilang) {
                 property.setOption(FxStructureOption.OPTION_MULTILANG, propOption.isOverrideable(), b);
             } else {
                 property.setOption(FxStructureOption.OPTION_MULTILANG, true, b);
                if (assignment !=null)
                    assignment.setOption(FxStructureOption.OPTION_MULTILANG, !b);
             }
         } else {
             if (b && multilang)
                 property.setOption(FxStructureOption.OPTION_MULTILANG, true, b);
             else if (b && !multilang)
                 property.setOption(FxStructureOption.OPTION_MULTILANG, false, b);
         }
     }
 
     /**
      * Returns if the option FxStructureOption.OPTION_MULTILANG is set.
      * This option controls the multilingualism of a property.
      *
      * @return true if the generic option FxStructureOption.OPTION_MULTILANG is set.
      */
 
     public boolean isMultiLang() {
         return assignment.isMultiLang();
     }
 
     /**
      * Sets the FxStructureOption.OPTION_MULTILANG option defensively by considering
      * option overriding.
      *
      * @param b boolean to set the option
      */
     public void setMultiLang(boolean b) {
         try {
             FxStructureOption propOption = property.getOption(FxStructureOption.OPTION_MULTILANG);
             if (b && !isMultiLang()) {
                 if (propOption.isSet()) {
                     if (propOption.isValueTrue())
                         //option of property has the right value, clear ovverriding option
                         assignment.clearOption(FxStructureOption.OPTION_MULTILANG);
                     else {
                         //option of property has the wrong value ->overide it
                         property.clearOption(FxStructureOption.OPTION_MULTILANG);
                         property.setOption(FxStructureOption.OPTION_MULTILANG, true, false);
                         assignment.clearOption(FxStructureOption.OPTION_MULTILANG);
                         assignment.setOption(FxStructureOption.OPTION_MULTILANG, true);
                     }
                 } else {
                     //property option is not set yet, set assignment option only
                     assignment.clearOption(FxStructureOption.OPTION_MULTILANG);
                     assignment.setOption(FxStructureOption.OPTION_MULTILANG, true);
                 }
             } else if (!b && isMultiLang()) {
                 if (propOption.isSet()) {
                     if (!propOption.isValueTrue())
                         //option of property has the right value, clear ovverriding option
                         assignment.clearOption(FxStructureOption.OPTION_MULTILANG);
                     else {
                         //option of property has the wrong value ->ovveride it
                         property.clearOption(FxStructureOption.OPTION_MULTILANG);
                         property.setOption(FxStructureOption.OPTION_MULTILANG, true, true);
                         assignment.clearOption(FxStructureOption.OPTION_MULTILANG);
                         assignment.setOption(FxStructureOption.OPTION_MULTILANG, false);
                     }
                 } else {
                     //property option is not set yet, set assignment option only
                     assignment.clearOption(FxStructureOption.OPTION_MULTILANG);
                     assignment.setOption(FxStructureOption.OPTION_MULTILANG, false);
                 }
             }
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public String getParseRequestParameters() {
         try {
             String action = FxJsfUtils.getParameter("action");
             if (StringUtils.isBlank(action)) {
                 return null;
             } else if ("openInstance".equals(action)) {
                 editMode = false;
                 long assId = FxJsfUtils.getLongParameter("id", -1);
                 FxPropertyAssignmentEdit assignment = ((FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(assId)).asEditable();
                 setAssignment(assignment);
                 setProperty(assignment.getPropertyEdit());
                 setPropertyId(assignment.getProperty().getId());
                 initEditing();
             } else if ("editInstance".equals(action)) {
                 editMode = true;
                 long assId = FxJsfUtils.getLongParameter("id", -1);
                 FxPropertyAssignmentEdit assignment = ((FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(assId)).asEditable();
                 setAssignment(assignment);
                 setProperty(assignment.getPropertyEdit());
                 setPropertyId(assignment.getProperty().getId());
                 initEditing();
             } else if ("createProperty".equals(action)) {
                 editMode = true;
                 assignment = null;
                 parentXPath = null;
                 parentType = null;
                 originalLanguageSystemLanguage = false;
 
                 long id = FxJsfUtils.getLongParameter("id");
                 String nodeType = FxJsfUtils.getParameter("nodeType");
 
                 parentXPath = "/";
 
                 if (StructureTreeWriter.DOC_TYPE_TYPE.equals(nodeType) ||
                         StructureTreeWriter.DOC_TYPE_TYPE_RELATION.equals(nodeType)) {
                     parentType = CacheAdmin.getEnvironment().getType(id);
                 }
 
                 if (StructureTreeWriter.DOC_TYPE_GROUP.equals(nodeType)) {
                     FxGroupAssignment ga = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(id);
                     parentType = ga.getAssignedType();
                     parentXPath = XPathElement.stripType(ga.getXPath());
                 }
 
                 property = FxPropertyEdit.createNew("NEWPROPERTY", new FxString(""), new FxString(""),
                         FxMultiplicity.MULT_0_1, CacheAdmin.getEnvironment().getDefaultACL(ACL.Category.STRUCTURE),
                         FxDataType.Text);
                 initNewPropertyEditing();
             } else if ("assignProperty".equals(action)) {
                 editMode = false;
                 structureManagement = FxJsfUtils.getRequest().getUserTicket().isInRole(Role.StructureManagement);
                 if (structureManagement) {
                     long id = FxJsfUtils.getLongParameter("id");
                     String nodeType = FxJsfUtils.getParameter("nodeType");
 
                     parentXPath = "/";
 
                     if (StructureTreeWriter.DOC_TYPE_TYPE.equals(nodeType)
                             || StructureTreeWriter.DOC_TYPE_TYPE_RELATION.equals(nodeType)) {
                         parentType = CacheAdmin.getEnvironment().getType(id);
                     }
 
                     if (StructureTreeWriter.DOC_TYPE_GROUP.equals(nodeType)) {
                         FxGroupAssignment ga = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(id);
                         parentType = ga.getAssignedType();
                         parentXPath = XPathElement.stripType(ga.getXPath());
                     }
 
                     long assignmentId = EJBLookup.getAssignmentEngine().save(FxPropertyAssignmentEdit.createNew(assignment, parentType, assignment.getAlias(), parentXPath), false);
                     StructureTreeControllerBean s = (StructureTreeControllerBean) FxJsfUtils.getManagedBean("structureTreeControllerBean");
                     s.addAction(StructureTreeControllerBean.ACTION_RELOAD_SELECT_ASSIGNMENT, assignmentId, "");
                 }
             }
         } catch (Throwable t) {
             LOG.error("Failed to parse request parameters: " + t.getMessage(), t);
             new FxFacesMsgErr(t).addToContext();
         }
 
         return null;
     }
 
     public void toggleEditMode() {
         editMode = !editMode;
     }
 
     public boolean getEditMode() {
         return editMode;
     }
 
     public boolean isStructureManagement() {
         return structureManagement;
     }
 
     /**
      * Initializes variables and does workarounds so editing of an existing property and
      * property assignment is possible via the webinterface
      */
     private void initEditing() {
         structureManagement = FxJsfUtils.getRequest().getUserTicket().isInRole(Role.StructureManagement);
         if (!assignment.isNew())
             scriptWrapper = new ScriptListWrapper(assignment.getId(), false);
 
         this.defaultMultiplicity = assignment.getDefaultMultiplicity();
         setMinMultiplicity(FxMultiplicity.getIntToString(assignment.getMultiplicity().getMin()));
         setMaxMultiplicity(FxMultiplicity.getIntToString(assignment.getMultiplicity().getMax()));
         setPropertyMinMultiplicity(FxMultiplicity.getIntToString(property.getMultiplicity().getMin()));
         setPropertyMaxMultiplicity(FxMultiplicity.getIntToString(property.getMultiplicity().getMax()));
 
         optionWrapper = new OptionWrapper(property.getOptions(), assignment.getOptions(), true);
 
         try {
             //workaround for the system language, which is not loadable:
             //set default language as language during the editing process
             //if the property assignment didn't become multilang and antoher language was
             //assigned, ->restore the system language in the applyChanges method
             if (assignment.getDefaultLanguage() == FxLanguage.SYSTEM_ID) {
                 originalLanguageSystemLanguage = true;
             } else {
                 setDefaultLanguage(EJBLookup.getLanguageEngine().load(assignment.getDefaultLanguage()));
             }
         } catch (Throwable t) {
             LOG.error("Failed to initialize the Editing process: " + t.getMessage(), t);
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     /**
      * Returns if the FxProperty's Data Type is reference or inlinereference
      * in order to enable or disable gui elements.
      *
      * @return true if the data type is reference
      */
     public boolean isPropertyReference() {
         if (property.getDataType() == null)
             return false;
         else
             return (property.getDataType().getId() == FxDataType.InlineReference.getId() ||
                     property.getDataType().getId() == FxDataType.Reference.getId());
     }
 
 
     /**
      * Initializes variables necessarry for creating a new property via the web interface.
      * during the creation process, new properties don't have assignments yet.
      */
     private void initNewPropertyEditing() {
         structureManagement = FxJsfUtils.getRequest().getUserTicket().isInRole(Role.StructureManagement);
         property.setAutoUniquePropertyName(false);
         setPropertyMinMultiplicity(FxMultiplicity.getIntToString(property.getMultiplicity().getMin()));
         setPropertyMaxMultiplicity(FxMultiplicity.getIntToString(property.getMultiplicity().getMax()));
         optionWrapper = new OptionWrapper(property.getOptions(), null, true);
         property.setDataType(FxDataType.String1024);
     }
 
     /**
      * Returns if the Fxproperty's Data Type is  SelectOne or SelectMany
      * in order to enable or disable gui elements.
      *
      * @return true if the data type is select list
      */
     public boolean isPropertySelectList() {
         if (property.getDataType() == null)
             return false;
         else
             return (property.getDataType().getId() == FxDataType.SelectMany.getId() ||
                     property.getDataType().getId() == FxDataType.SelectOne.getId());
     }
 
 
     /**
      * Stores a newly created property in DB
      */
     public void createProperty() {
         if (FxJsfUtils.getRequest().getUserTicket().isInRole(Role.StructureManagement)) {
             try {
                 applyPropertyChanges();
                 long assignmentId;
                 if (parentType != null)
                     assignmentId = EJBLookup.getAssignmentEngine().createProperty(parentType.getId(), property, parentXPath);
                 else
                     assignmentId = EJBLookup.getAssignmentEngine().createProperty(property, parentXPath);
                 StructureTreeControllerBean s = (StructureTreeControllerBean) FxJsfUtils.getManagedBean("structureTreeControllerBean");
                 s.addAction(StructureTreeControllerBean.ACTION_RELOAD_SELECT_ASSIGNMENT, assignmentId, "");
             }
             catch (Throwable t) {
                 new FxFacesMsgErr(t).addToContext();
             }
         } else
             new FxFacesMsgErr(new FxApplicationException("ex.role.notInRole", "StructureManagement")).addToContext();
     }
 
     /**
      * Forward property and property assignment changes to the DB
      */
     public void saveChanges() {
         if (FxJsfUtils.getRequest().getUserTicket().isInRole(Role.ScriptManagement))
             try {
                 if (!property.isNew())
                     saveScriptChanges();
             }
             catch (Throwable t) {
                 new FxFacesMsgErr(t).addToContext();
             }
         else
             new FxFacesMsgInfo("StructureEditor.info.notInRole.scriptManagement").addToContext();
 
         if (FxJsfUtils.getRequest().getUserTicket().isInRole(Role.StructureManagement)) {
             try {
                 applyPropertyChanges();
                 EJBLookup.getAssignmentEngine().save(property);
                 savePropertyAssignmentChanges();
                 StructureTreeControllerBean s = (StructureTreeControllerBean) FxJsfUtils.getManagedBean("structureTreeControllerBean");
                 s.addAction(StructureTreeControllerBean.ACTION_RENAME_SELECT_ASSIGNMENT, assignment.getId(), assignment.getDisplayName());
             }
             catch (Throwable t) {
                 new FxFacesMsgErr(t).addToContext();
             }
         } else
             new FxFacesMsgInfo("StructureEditor.info.notInRole.structureManagement").addToContext();
     }
 
     /**
      * Apply all changes to the property assignment which are still cached in
      * the view (property options, multiplicity, label, scripts) and forward them to DB
      *
      * @throws FxApplicationException if the label is invalid
      */
     private void savePropertyAssignmentChanges() throws FxApplicationException {
         if (assignment.getLabel().getIsEmpty()) {
             throw new FxApplicationException("ex.structureEditor.noLabel");
         }
         int min = FxMultiplicity.getStringToInt(minMultiplicity);
         int max = FxMultiplicity.getStringToInt(maxMultiplicity);
 
         //save multilang option
         FxStructureOption multilang = assignment.getOption(FxStructureOption.OPTION_MULTILANG);
 
         //delete current options
         while (!assignment.getOptions().isEmpty()) {
             String key = assignment.getOptions().get(0).getKey();
             assignment.clearOption(key);
         }
         //add edited options
         List<FxStructureOption> newAssignmentOptions = optionWrapper.asFxStructureOptionList(optionWrapper.getAssignmentOptions());
         for (FxStructureOption o : newAssignmentOptions) {
             assignment.setOption(o.getKey(), o.getValue());
         }
 
         //restore multilang option
         if (multilang.isSet())
             assignment.setOption(multilang.getKey(), multilang.getValue());
 
         //in any case restore the system language for systeminternal properties
         if (isSystemInternal() && originalLanguageSystemLanguage) {
             assignment.setDefaultLanguage(FxLanguage.SYSTEM_ID);
         }
 
         if (!isSystemInternal()
                 || FxJsfUtils.getRequest().getUserTicket().isInRole(Role.GlobalSupervisor)) {
             if (getProperty().mayOverrideBaseMultiplicity()) {
                 FxJsfUtils.checkMultiplicity(min, max);
                 assignment.setMultiplicity(new FxMultiplicity(min, max));
             }
             assignment.setDefaultMultiplicity(this.defaultMultiplicity);
 
             if (originalLanguageSystemLanguage && !isMultiLang()) {
                 assignment.setDefaultLanguage(FxLanguage.SYSTEM_ID);
             }
             EJBLookup.getAssignmentEngine().save(assignment, false);
         }
     }
 
     /**
      * Apply all changes to the property which are still cached in
      * the view (property options, multiplicity, label)
      *
      * @throws FxApplicationException if the label is invalid
      */
     private void applyPropertyChanges() throws FxApplicationException {
         if (property.getLabel().getIsEmpty()) {
             throw new FxApplicationException("ex.structureEditor.noLabel");
         }
 
         int min = FxMultiplicity.getStringToInt(propertyMinMultiplicity);
         int max = FxMultiplicity.getStringToInt(propertyMaxMultiplicity);
 
         FxJsfUtils.checkMultiplicity(min, max);
 
         //save multilang option
         FxStructureOption multilang = property.getOption(FxStructureOption.OPTION_MULTILANG);
 
         //delete current options
         while (!property.getOptions().isEmpty()) {
             String key = property.getOptions().get(0).getKey();
             property.clearOption(key);
         }
         //add edited options
         List<FxStructureOption> newGroupOptions = optionWrapper.asFxStructureOptionList(optionWrapper.getStructureOptions());
         for (FxStructureOption o : newGroupOptions) {
             property.setOption(o.getKey(), o.isOverrideable(), o.getValue());
         }
 
         //restore multilang option
         if (multilang.isSet())
             property.setOption(multilang.getKey(), multilang.isOverrideable(), multilang.getValue());
 
         if (!isSystemInternal() || FxJsfUtils.getRequest().getUserTicket().isInRole(Role.GlobalSupervisor)) {
             property.setMultiplicity(new FxMultiplicity(min, max));
         }
     }
 
 
     /**
      * Show the PropertyAssignmentEditor
      *
      * @return the next page
      */
     public String showPropertyAssignmentEditor() {
         return "propertyAssignmentEditor";
     }
 
     /**
      * Show the PropertyEditor
      *
      * @return the next page
      */
     public String showPropertyEditor() {
         return "propertyEditor";
     }
 
     /**
      * Show the OptionEditor
      *
      * @return the next page
      */
     public String showPropertyOptionEditor() {
         return "propertyOptionEditor";
     }
 
     /**
      * ************** script editor tab begin ***********************
      */
 
     public String showAssignmentScriptEditor() {
         return "assignmentScriptEditor";
     }
 
     /**
      * called from the script editor; to open an instance where the script is assigned to
      *
      * @return type editor page
      */
     public String gotoAssignmentScriptEditor() {
         editMode = false;
         long propId = FxJsfUtils.getLongParameter("oid", -1);
         setPropertyId(propId);
         FxPropertyAssignmentEdit assignment = ((FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(propId)).asEditable();
         setAssignment(assignment);
         setProperty(assignment.getPropertyEdit());
         initEditing();
         return showAssignmentScriptEditor();
     }
 
     public ScriptListWrapper getScriptWrapper() {
         return scriptWrapper;
     }
 
     public int getScriptCount() {
         return scriptWrapper == null ? 0 : scriptWrapper.getScriptList().size();
     }
 
     public int getScriptListFiler() {
         return scriptListFiler;
     }
 
     public void setScriptListFiler(int scriptListFiler) {
         this.scriptListFiler = scriptListFiler;
     }
 
     public void removeScript() {
         scriptWrapper.remove(scriptListFiler);
     }
 
     public FxScriptInfo getSelectedScriptInfo() {
         return selectedScriptInfo;
     }
 
     public void setSelectedScriptInfo(FxScriptInfo selectedScriptInfo) {
         this.selectedScriptInfo = selectedScriptInfo;
     }
 
     public long getSelectedScriptEventId() {
         if (selectedScriptInfo != null)
             return selectedScriptInfo.getEvent().getId();
         else return -1;
         //return selectedScriptEventId;
     }
 
     public void setSelectedScriptEventId(long selectedScriptEventId) {
         this.selectedScriptEventId = selectedScriptEventId;
     }
 
     public boolean isSelectedDerivedUsage() {
         return selectedDerivedUsage;
     }
 
     public void setSelectedDerivedUsage(boolean selectedDerivedUsage) {
         this.selectedDerivedUsage = selectedDerivedUsage;
     }
 
     public boolean isSelectedActive() {
         return selectedActive;
     }
 
     public void setSelectedActive(boolean selectedActive) {
         this.selectedActive = selectedActive;
     }
 
     public void addScript() {
         try {
             scriptWrapper.add(selectedScriptInfo.getId(), selectedScriptEventId, selectedDerivedUsage, selectedActive);
             this.selectedScriptInfo = CacheAdmin.getFilteredEnvironment().getScripts().get(0);
             this.selectedScriptInfo.getEvent().getId();
         }
         catch (Throwable t) {
             //TODO: print error message, a4j tags do not support faces message erros
             //new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public Map<Long, String> getAssignmentNameForId() {
         return new HashMap<Long, String>() {
             public String get(Object key) {
                 return CacheAdmin.getFilteredEnvironment().getAssignment((Long) key).getXPath();
             }
         };
     }
 
     /**
      * Saves script assignment changes to DB.
      *
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on errors
      */
     private void saveScriptChanges() throws FxApplicationException {
         for (ScriptListWrapper.ScriptListEntry e : scriptWrapper.getDelta(assignment.getId(), false)) {
             if (e.getId() == ScriptListWrapper.ID_SCRIPT_ADDED)
                 EJBLookup.getScriptingEngine().createAssignmentScriptMapping(e.getScriptEvent(), e.getScriptInfo().getId(), assignment.getId(), e.isActive(), e.isDerivedUsage());
             else if (e.getId() == ScriptListWrapper.ID_SCRIPT_REMOVED)
                 EJBLookup.getScriptingEngine().removeAssignmentScriptMappingForEvent(e.getScriptInfo().getId(), assignment.getId(), e.getScriptEvent());
             else if (e.getId() == ScriptListWrapper.ID_SCRIPT_UPDATED)
                 EJBLookup.getScriptingEngine().updateAssignmentScriptMappingForEvent(e.getScriptInfo().getId(), assignment.getId(), e.getScriptEvent(), e.isActive(), e.isDerivedUsage());
         }
     }
 
     /****script editor tab end*********/
 
 }
