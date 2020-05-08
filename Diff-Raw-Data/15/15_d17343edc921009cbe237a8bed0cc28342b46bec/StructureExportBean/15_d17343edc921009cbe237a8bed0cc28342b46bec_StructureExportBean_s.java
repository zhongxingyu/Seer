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
 
 package com.flexive.war.beans.admin.main;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxInvalidStateException;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.FxAssignment;
 import com.flexive.shared.structure.FxGroupAssignment;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.structure.export.StructureExporter;
 import com.flexive.shared.structure.export.StructureExporterCallback;
 import com.flexive.shared.tree.FxTreeNode;
 import org.apache.commons.lang.StringUtils;
 
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 /**
  * Bean to access the StructureExporterCallback interface / GroovyScriptExporter
  *
  * @author Christopher Blasnik (c.blasnik@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @see com.flexive.shared.structure.export.StructureExporterCallback;
  */
 public class StructureExportBean {
 
     private StructureExporterCallback callback;
     private boolean exportWithoutDependencies = false; // default: export w/ dependencies
     private List<FxType> typeList;
     private List<FxType> exportedTypesList;
     private Map<Long, Boolean> assignmentIsGroupMap; // dump all assignment ids in there to see whether they are goups
     private List<FxGroupAssignment> exportedGroupsList;
     private List<FxType> expDepTypeList; // list of exported dep. types
     private List<FxGroupAssignment> expDepGroupList; // list of exported dep. groups
     private boolean showAllTypes = true; // initial value
     private boolean showBaseTypes;
     private boolean showDerivedTypes;
     private boolean showRelations; // not yet supported
     private ConcurrentMap<Long, Boolean> exportIds = new ConcurrentHashMap<Long, Boolean>();
     private boolean exportPerformed = false;
     private boolean hasDependencies;
     private Map<FxAssignment, FxAssignment> dependentOnMapping;
     // String instead of boolean used because of richfaces bug #7206 (JIRA)
     private String open_1 = "false";
     private String open_2 = "false";
     private String open_3 = "false";
     private String open_4 = "true"; // Groovy result panel
     private String open_5 = "false";
     private String open_6 = "false";
     private boolean allTypesMarked;
     private String typeIdsXML;
     private List<StructureExporterCallback> dependencyStructures;
     private List<FxType> typesWithScripts;
     private List<FxAssignment> assignsWithScripts;
     private boolean showScripts = true;
     private Map<Long, Map<String, List<Long>>> typeScripts;
     private Map<Long, Map<String, List<Long>>> assignScripts;
     private Map<Long, List<String>> typeScriptEvents;
     private Map<Long, List<String>> assignScriptEvents;
     private Map<Long, String> scriptIdNameMap;
     private long currentScriptId = -1L;
     private String currentScriptCode;
     private GroovyScriptExporterBean gbean;
     private String userLang = "en";
     private String toggleEditor = "later";
 
     // constructor
     public StructureExportBean() {
         gbean = new GroovyScriptExporterBean();
         parseRequestParameters();
     }
 
     /**
      * Parse the request parameters and perform actions as requested.
      */
     private void parseRequestParameters() {
         try {
             String action = FxJsfUtils.getParameter("action");
             if (StringUtils.isBlank(action)) {
                 // no action requested
                 return;
             }
             if(exportIds.size() > 0)
                     exportIds = new ConcurrentHashMap<Long, Boolean>();
 
             if ("exportType".equals(action)) {
                 final long typeId = FxJsfUtils.getLongParameter("typeId", FxTreeNode.ROOT_NODE);
                 exportIds.put(typeId, true);
                 export();
             } else if ("exportTypeNoDeps".equals(action)) {
                 final long typeId = FxJsfUtils.getLongParameter("typeId", FxTreeNode.ROOT_NODE);
                 exportIds.put(typeId, true);
                 setExportWithoutDependencies(true);
                 export();
             }
         } catch (Exception e) {
             // silent death
         }
     }
 
     /**
      * Action method for exporting structures.
      *
      * @return returns the same page or &quot;structureExportResult&quot; in case export results are available
      */
     public String export() {
         final UserTicket ticket = FxContext.getUserTicket();
         if (!ticket.isInRole(Role.StructureManagement)) {
             new FxFacesMsgErr("StructureExport.msg.err.notinrole").addToContext();
             return "";
         }
 
         clearExportResults(); // clean up first
 
         // retrieve all ids marked for export
         final List<Long> markedIds = new ArrayList<Long>();
         for (Long idMarkedForExport : exportIds.keySet()) {
             if (exportIds.get(idMarkedForExport)) {
                 markedIds.add(idMarkedForExport);
             }
         }
 
         if (markedIds.size() > 0) {
             exportedTypesList = new ArrayList<FxType>();
             if (markedIds.size() == 1) {
                 callback = StructureExporter.newInstance(markedIds.get(0), exportWithoutDependencies);
                 exportPerformed = true;
             } else {
                 callback = StructureExporter.newInstance(markedIds, exportWithoutDependencies);
                 exportPerformed = true;
             }
 
             // check dependencies
             if (!exportWithoutDependencies) {
                 if (callback.getHasDependencies()) {
                     hasDependencies = true;
                     try {
                         dependencyStructures = callback.getDependencyStructures();
                         populateDependentOnMapping(callback.getDependentOnMapping());
                         // retrieve types from dep structure
                         expDepTypeList = new ArrayList<FxType>(dependencyStructures.size());
                         expDepGroupList = new ArrayList<FxGroupAssignment>();
 
                         for (StructureExporterCallback c : dependencyStructures) {
                             final FxType currentType = CacheAdmin.getEnvironment().getType(c.getTypeId());
                             expDepTypeList.add(currentType);
                             if (c.getGroupAssignments() != null) {
                                 expDepGroupList.addAll(c.getGroupAssignments().keySet());
                                 // populate isGroup list
                                 populateAssignmentIsGroupMap(c.getTypeAssignments().get(currentType));
                                 for (FxGroupAssignment ga : expDepGroupList) { // additionally
                                     populateAssignmentIsGroupMap(c.getGroupAssignments().get(ga));
                                 }
                             }
                         }
                     } catch (FxInvalidStateException e) {
                         // do nothing
                     }
 
                 }
             }
 
             // add to list of all exported types
             exportedTypesList.addAll(callback.getTypeAssignments().keySet());
 
             // populate the GroupAssignments map
             for (FxType t : exportedTypesList) {
                 populateAssignmentIsGroupMap(callback.getTypeAssignments().get(t));
             }
 
             // list of exported groups
             if (callback.getGroupAssignments() != null) {
                 exportedGroupsList = new ArrayList<FxGroupAssignment>(callback.getGroupAssignments().keySet().size());
                 exportedGroupsList.addAll(callback.getGroupAssignments().keySet());
                 for (FxGroupAssignment ga : exportedGroupsList) {
                     populateAssignmentIsGroupMap(callback.getGroupAssignments().get(ga));
                 }
             }
 
             if (showScripts)
                 populateScriptVars();
 
             // init the groovy export by passing the callback, reset first
             gbean.resetExporter().setCallback(callback);
 
             return "structureExportResult";
         }
         return "";
     }
 
     /**
      * @return resurns the list of types
      */
     public List<FxType> getTypeList() {
         typeList = new ArrayList<FxType>();
         if (isShowAllTypes())
             typeList = CacheAdmin.getEnvironment().getTypes();
         else if (showRelations && !showBaseTypes && !showDerivedTypes)
             typeList = CacheAdmin.getEnvironment().getTypes(true, showDerivedTypes, false, showRelations);
         else if (showDerivedTypes && !showBaseTypes && !showRelations)
             typeList = CacheAdmin.getEnvironment().getTypes(showBaseTypes, showDerivedTypes, true, showRelations);
         else
             typeList = CacheAdmin.getEnvironment().getTypes(showBaseTypes, showDerivedTypes, true, showRelations);
         return typeList;
     }
 
     /**
      * Populate the list with info about assignments being groups
      *
      * @param in a List of FxAssignments
      */
     private void populateAssignmentIsGroupMap(List<FxAssignment> in) {
         if (in != null) {
             if (assignmentIsGroupMap == null) {
                 assignmentIsGroupMap = new HashMap<Long, Boolean>();
             }
             for (FxAssignment a : in) {
                 if (!assignmentIsGroupMap.containsKey(a.getId())) {
                     assignmentIsGroupMap.put(a.getId(), a instanceof FxGroupAssignment);
                 }
             }
         }
     }
 
     public void setTypeList(List<FxType> typeList) {
         this.typeList = typeList;
     }
 
     /**
      * Action method to be called when marking the checkboxes of all types in the UI
      */
     public void markAllTypes() {
         if (allTypesMarked) {
             for (Long id : exportIds.keySet()) {
                 exportIds.replace(id, true);
             }
         } else {
             for (Long id : exportIds.keySet()) {
                 exportIds.replace(id, false);
             }
         }
 
 
     }
 
     /**
      * Action method: Export the selected types as an XML structure
      */
     public void exportAsXML() {
         typeIdsXML = "";
         final StringBuilder s = new StringBuilder(50);
 
         for (Long id : exportIds.keySet()) {
             if (exportIds.get(id)) {
                 s.append(id)
                         .append("/");
             }
         }
         typeIdsXML = s.toString();
         if (typeIdsXML.length() > 1)
             typeIdsXML = typeIdsXML.substring(0, typeIdsXML.lastIndexOf("/"));
     }
 
     /**
      * Populates the Map containing the dependentOnMapping
      *
      * @param dependentOnMapping Map containing assignment ids (keys) and the assignment ids they depend on (values)
      */
     private void populateDependentOnMapping(Map<Long, Long> dependentOnMapping) {
         if (dependentOnMapping == null) {
             this.dependentOnMapping = new HashMap<FxAssignment, FxAssignment>();
             return;
         }
 
         this.dependentOnMapping = new HashMap<FxAssignment, FxAssignment>(dependentOnMapping.size());
         for (Long id : dependentOnMapping.keySet()) {
             this.dependentOnMapping.put(CacheAdmin.getEnvironment().getAssignment(id),
                     CacheAdmin.getEnvironment().getAssignment(dependentOnMapping.get(id)));
         }
     }
 
     /**
      * Populate the script variables s.t. they can be displayed properly
      */
     private void populateScriptVars() {
         // init
         typeScripts = callback.getTypeScriptMapping();
         assignScripts = callback.getAssignmentScriptMapping();
         typesWithScripts = new ArrayList<FxType>(typeScripts.size());
         assignsWithScripts = new ArrayList<FxAssignment>(assignScripts.size());
         typeScriptEvents = new HashMap<Long, List<String>>();
         assignScriptEvents = new HashMap<Long, List<String>>();
         scriptIdNameMap = new HashMap<Long, String>();
 
 
         // type scripts
         if (typeScripts.size() > 0) {
             for (Long typeId : typeScripts.keySet()) {
                 typesWithScripts.add(CacheAdmin.getEnvironment().getType(typeId));
                 final Map<String, List<Long>> eventScriptMap = typeScripts.get(typeId);
                 typeScriptEvents.put(typeId, new ArrayList<String>(eventScriptMap.keySet()));
 
                 // get all the script ids, put them into the scriptIdNameMap and retrieve the corresponding script names
                 for (String eventName : eventScriptMap.keySet()) {
                     // iterate through script ids
                     for (Long scriptId : eventScriptMap.get(eventName)) {
                         scriptIdNameMap.put(scriptId, CacheAdmin.getEnvironment().getScript(scriptId).getName());
                     }
                 }
             }
         }
 
         // assignment scripts
         if (assignScripts.size() > 0) {
             for (Long assignId : assignScripts.keySet()) {
                 assignsWithScripts.add(CacheAdmin.getEnvironment().getAssignment(assignId));
 
                 final Map<String, List<Long>> eventScriptMap = assignScripts.get(assignId);
                 assignScriptEvents.put(assignId, new ArrayList<String>(eventScriptMap.keySet()));
 
                 // add the remaining script ids / names to the scriptIdNameMap
                 for (String eventName : eventScriptMap.keySet()) {
                     for (Long scriptId : eventScriptMap.get(eventName)) {
                         scriptIdNameMap.put(scriptId, CacheAdmin.getEnvironment().getScript(scriptId).getName());
                     }
                 }
             }
         }
     }
 
     /**
      * Action method to be called from the modalpanel displaying the script code
      */
     public void cancelScriptPanel() {
         // remains void
     }
 
     /**
      * Action method rendering the script code for the current script to be displayed
      * @throws FxApplicationException if the code could not be loaded
      */
     public void renderScriptCode() throws FxApplicationException {
         if (currentScriptId > 0) {
             currentScriptCode = EJBLookup.getScriptingEngine().loadScriptCode(currentScriptId);
         }
     }
 
     public StructureExporterCallback getCallback() {
         return callback;
     }
 
     public void setCallback(StructureExporterCallback callback) {
         this.callback = callback;
     }
 
     public boolean isExportWithoutDependencies() {
         return exportWithoutDependencies;
     }
 
     public void setExportWithoutDependencies(boolean exportWithoutDependencies) {
         this.exportWithoutDependencies = exportWithoutDependencies;
         gbean.setWithoutDependencies(exportWithoutDependencies);
     }
 
     /**
      * @return returns showall types if all other options are set to "true"
      */
     public boolean isShowAllTypes() {
         if (showBaseTypes && showRelations && showDerivedTypes)
             showAllTypes = true;
         return showAllTypes;
     }
 
     public void setShowAllTypes(boolean showAllTypes) {
         this.showAllTypes = showAllTypes;
     }
 
     public boolean isShowBaseTypes() {
         return showBaseTypes;
     }
 
     public void setShowBaseTypes(boolean showBaseTypes) {
         this.showBaseTypes = showBaseTypes;
     }
 
     public boolean isShowDerivedTypes() {
         return showDerivedTypes;
     }
 
     public void setShowDerivedTypes(boolean showDerivedTypes) {
         this.showDerivedTypes = showDerivedTypes;
     }
 
     public boolean isShowRelations() {
         return showRelations;
     }
 
     public void setShowRelations(boolean showRelations) {
         this.showRelations = showRelations;
     }
 
     public ConcurrentMap<Long, Boolean> getExportIds() {
         return exportIds;
     }
 
     public void setExportIds(ConcurrentMap<Long, Boolean> exportIds) {
         this.exportIds = exportIds;
     }
 
     public boolean isExportPerformed() {
         return exportPerformed;
     }
 
     public void setExportPerformed(boolean exportPerformed) {
         this.exportPerformed = exportPerformed;
     }
 
     public List<FxType> getExportedTypesList() {
         return exportedTypesList;
     }
 
     public void setExportedTypesList(List<FxType> exportedTypesList) {
         this.exportedTypesList = exportedTypesList;
     }
 
     public boolean isHasDependencies() {
         return hasDependencies;
     }
 
     public void setHasDependencies(boolean hasDependencies) {
         this.hasDependencies = hasDependencies;
     }
 
     public List<StructureExporterCallback> getDependencyStructures() {
         return dependencyStructures;
     }
 
     public void setDependencyStructures(List<StructureExporterCallback> dependencyStructures) {
         this.dependencyStructures = dependencyStructures;
     }
 
     public List<FxType> getExpDepTypeList() {
         return expDepTypeList;
     }
 
     public void setExpDepTypeList(List<FxType> expDepTypeList) {
         this.expDepTypeList = expDepTypeList;
     }
 
     public List<FxGroupAssignment> getExportedGroupsList() {
         return exportedGroupsList;
     }
 
     public void setExportedGroupsList(List<FxGroupAssignment> exportedGroupsList) {
         this.exportedGroupsList = exportedGroupsList;
     }
 
     public Map<Long, Boolean> getAssignmentIsGroupMap() {
         return assignmentIsGroupMap;
     }
 
     public void setAssignmentIsGroupMap(Map<Long, Boolean> assignmentIsGroupMap) {
         this.assignmentIsGroupMap = assignmentIsGroupMap;
     }
 
     public List<FxGroupAssignment> getExpDepGroupList() {
         return expDepGroupList;
     }
 
     public void setExpDepGroupList(List<FxGroupAssignment> expDepGroupList) {
         this.expDepGroupList = expDepGroupList;
     }
 
     public String getOpen_1() {
         return open_1;
     }
 
     public void setOpen_1(String open_1) {
         this.open_1 = open_1;
     }
 
     public String getOpen_2() {
         return open_2;
     }
 
     public void setOpen_2(String open_2) {
         this.open_2 = open_2;
     }
 
     public String getOpen_3() {
         return open_3;
     }
 
     public void setOpen_3(String open_3) {
         this.open_3 = open_3;
     }
 
     public String getOpen_4() {
         return open_4;
     }
 
     public void setOpen_4(String open_4) {
         this.open_4 = open_4;
     }
 
     public String getOpen_5() {
         return open_5;
     }
 
     public void setOpen_5(String open_5) {
         this.open_5 = open_5;
     }
 
     public String getOpen_6() {
         return open_6;
     }
 
     public void setOpen_6(String open_6) {
         this.open_6 = open_6;
     }
 
     public boolean isAllTypesMarked() {
         return allTypesMarked;
     }
 
     public void setAllTypesMarked(boolean allTypesMarked) {
         this.allTypesMarked = allTypesMarked;
     }
 
     public Map<FxAssignment, FxAssignment> getDependentOnMapping() {
         return dependentOnMapping;
     }
 
     public void setDependentOnMapping(Map<FxAssignment, FxAssignment> dependentOnMapping) {
         this.dependentOnMapping = dependentOnMapping;
     }
 
     public String getTypeIdsXML() {
         return typeIdsXML;
     }
 
     public void setTypeIdsXML(String typeIdsXML) {
         this.typeIdsXML = typeIdsXML;
     }
 
     public List<FxType> getTypesWithScripts() {
         return typesWithScripts;
     }
 
     public void setTypesWithScripts(List<FxType> typesWithScripts) {
         this.typesWithScripts = typesWithScripts;
     }
 
     public List<FxAssignment> getAssignsWithScripts() {
         return assignsWithScripts;
     }
 
     public void setAssignsWithScripts(List<FxAssignment> assignsWithScripts) {
         this.assignsWithScripts = assignsWithScripts;
     }
 
     public boolean isShowScripts() {
         return showScripts;
     }
 
     public void setShowScripts(boolean showScripts) {
         this.showScripts = showScripts;
     }
 
     public Map<Long, Map<String, List<Long>>> getTypeScripts() {
         return typeScripts;
     }
 
     public void setTypeScripts(Map<Long, Map<String, List<Long>>> typeScripts) {
         this.typeScripts = typeScripts;
     }
 
     public Map<Long, Map<String, List<Long>>> getAssignScripts() {
         return assignScripts;
     }
 
     public void setAssignScripts(Map<Long, Map<String, List<Long>>> assignScripts) {
         this.assignScripts = assignScripts;
     }
 
     public Map<Long, List<String>> getTypeScriptEvents() {
         return typeScriptEvents;
     }
 
     public void setTypeScriptEvents(Map<Long, List<String>> typeScriptEvents) {
         this.typeScriptEvents = typeScriptEvents;
     }
 
     public Map<Long, String> getScriptIdNameMap() {
         return scriptIdNameMap;
     }
 
     public void setScriptIdNameMap(Map<Long, String> scriptIdNameMap) {
         this.scriptIdNameMap = scriptIdNameMap;
     }
 
     public long getCurrentScriptId() {
         return currentScriptId;
     }
 
     public void setCurrentScriptId(long currentScriptId) {
         this.currentScriptId = currentScriptId;
     }
 
     public String getCurrentScriptCode() {
         return currentScriptCode;
     }
 
     public void setCurrentScriptCode(String currentScriptCode) {
         this.currentScriptCode = currentScriptCode;
     }
 
     public Map<Long, List<String>> getAssignScriptEvents() {
         return assignScriptEvents;
     }
 
     public void setAssignScriptEvents(Map<Long, List<String>> assignScriptEvents) {
         this.assignScriptEvents = assignScriptEvents;
     }
 
     public boolean isDeleteStructures() {
         return gbean.isDeleteStructures();
     }
 
     public void setDeleteStructures(boolean deleteStructures) {
         gbean.setDeleteStructures(deleteStructures);
     }
 
     public void setScriptCode(String scriptCode) {
         gbean.setScriptCode(scriptCode);
     }
 
     public String getScriptCode() {
         return gbean.getScriptCode();
     }
 
     public void setGenerateImportStatements(boolean generateImportStatements) {
         gbean.setGenerateImportStatements(generateImportStatements);
     }
 
     public boolean isGenerateImportStatements() {
         return gbean.isGenerateImportStatements();
     }
 
     public void setDefaultsOnly(boolean defaultsOnly) {
         gbean.setDefaultsOnly(defaultsOnly);
     }
 
     public boolean isGenerateScriptAssignments() {
         return gbean.isGenerateScriptAssignments();
     }
 
     public void setGenerateScriptAssignments(boolean generateScriptAssignments) {
         gbean.setGenerateScriptAssignments(generateScriptAssignments);
     }
 
     public boolean isScriptOverride() {
         return gbean.isScriptOverride();
     }
 
     public void setScriptOverride(boolean scriptOverride) {
         gbean.setScriptOverride(scriptOverride);
     }
 
     public boolean isDefaultsOnly() {
         return gbean.isDefaultsOnly();
     }
 
     public void setAddSystemTypes(boolean addSystemTypes) {
         gbean.setAddSystemTypes(addSystemTypes);
     }
 
     public boolean isAddSystemTypes() {
         return gbean.isAddSystemTypes();
     }
 
     public String getUserLang() {
         userLang = FxContext.getUserTicket().getLanguage().getIso2digit();
         return userLang;
     }
 
     public void setUserLang(String userLang) {
         this.userLang = userLang;
     }
 
     /**
      * Action method: export groovy script as a file - inits the ExportServlet via its return value
      *
      * @return the URL to invoke the ExportServlet
      */
     public String exportGroovyScript() {
         FacesContext context = FacesContext.getCurrentInstance();
         HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
         HttpSession session = request.getSession();
         session.setAttribute("groovyScriptCode", getScriptCode());
         try {
             context.getExternalContext().redirect("export/exportGroovyScript");
         } catch(IOException e) {
             new FxFacesMsgErr(e).addToContext();
         }
         return "structureExportResult";
     }
 
     /**
      * Show the tab for the structure results
      *
      * @return the view id
      */
     public String showResultTab() {
         return "structureExportResult";
     }
 
     /**
      * Show the main tab for the structure export
      *
      * @return the view id
      */
     public String showMainTab() {
         return "structureExport";
     }
 
     /**
      * Action method to clear any export results
      */
     public void clearExportResults() {
         callback = null;
         exportedTypesList = null;
         exportPerformed = false;
         hasDependencies = false;
         expDepTypeList = null;
         exportedGroupsList = null;
         assignmentIsGroupMap = null;
         typesWithScripts = null;
         assignsWithScripts = null;
         scriptIdNameMap = null;
         currentScriptId = -1L;
         currentScriptCode = null;
         typeScriptEvents = null;
         assignScriptEvents = null;
     }
 
     public GroovyScriptExporterBean getGbean() {
         return gbean;
     }
 
     public void setGbean(GroovyScriptExporterBean gbean) {
         this.gbean = gbean;
     }
 
     /**
      * (dummy) Action method f. the copy2clipboard js function
      */
     public void copy2clipboard() {
         // remains void
     }
 
     public String getToggleEditor() {
         return toggleEditor;
     }
 
     public void setToggleEditor(String toggleEditor) {
         this.toggleEditor = toggleEditor;
     }
 }
