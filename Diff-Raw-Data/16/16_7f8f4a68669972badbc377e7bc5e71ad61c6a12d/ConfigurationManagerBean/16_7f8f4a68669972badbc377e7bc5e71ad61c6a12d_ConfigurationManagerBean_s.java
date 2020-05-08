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
 package com.flexive.war.beans.admin;
 
 import com.flexive.faces.beans.MessageBean;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.configuration.*;
 import com.flexive.shared.configuration.parameters.ParameterFactory;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.interfaces.ConfigurationEngine;
 import com.flexive.shared.scripting.FxScriptRunInfo;
 
 import javax.faces.event.ActionEvent;
 import javax.faces.model.SelectItem;
 import javax.faces.model.SelectItemGroup;
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.util.*;
 
 /**
  * Configuration Bean
  *
  * @author Laszlo Hernadi (lazlo.hernadi@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class ConfigurationManagerBean {
     private List<MultiTable> allTables = new ArrayList<MultiTable>();
     private Hashtable<String, Parameter<Serializable>> allParams = new Hashtable<String, Parameter<Serializable>>();
 
     /**
      * The current table, so the table with the current scope
      */
     private MultiTable curTable;
 
     private Integer selectedKey = 0;
 
     private SelectItemGroup allKeys = new SelectItemGroup();
     private static MessageBean mb = MessageBean.getInstance();
 
     private final static String BOOLEAN_KEY = "Boolean";
     private final static String LONG_KEY = "Long";
     private final static String STRING_KEY = "String";
     private final static String INTEGER_KEY = "Integer";
     private final static String ARRAY_LIST_KEY = "java.util.ArrayList";
 
     private final static int BOOLEAN_VALUE = 1;
     private final static int INTEGER_VALUE = 2;
     private final static int LONG_VALUE = 3;
     private final static int STRING_VALUE = 4;
     private final static int ARRAY_LIST_VALUE = 5;
 
     private final static int NO_PANEL = 0;
     private final static int EDIT_PANEL = 1;
     private final static int NEW_PANEL = 2;
     private final static int CONFIRM_PANEL = 3;
 
     private int curPanel = 0;
 
     private final static Hashtable<String, Integer> TYPE_LUT = new Hashtable<String, Integer>();
     private final Hashtable<String, Integer> PARAMETER_TYPES = new Hashtable<String, Integer>();
     private final Hashtable<String, TableRow> allRows = new Hashtable<String, TableRow>();
     private final ArrayList<String> tableHeader = new ArrayList<String>();
 
     private String curEditName = " ";
     private String curEdit_STR_Value = "";
     private boolean curEdit_Bool_Value = false;
     private boolean editBool = false;
     private String newName = "";
     private boolean strAlreadySet = false;
     private boolean showValues = true;
 
     private final static String STYLE_CLASS_VISIBLE = "visible";
     private final static String STYLE_CLASS_HIDDEN = "hidden";
     private final static String STYLE_CLASS_RED = "red";
 
     private final static int TA_LENGTH = 40;
 
     private Integer selectedType = null;
     private final static SelectItemGroup typeSelectList = new SelectItemGroup(mb.getMessage("SysParamConfig.type"), mb.getMessage("SysParamConfig.type"), false, new SelectItem[0]);
 
     private Integer selectedPath = 0;
     private SelectItemGroup pathSelectList = null;
 
     private ArrayList<ParameterScope> allScopes = null;
     private String currentPathStr = "";
 
     static {
         SelectItem[] curSelects = new SelectItem[]{
                new SelectItem(0, mb.getMessage("SysParamConfig.selectType"), "", true),   // if removed, watch out for the index
                 new SelectItem(BOOLEAN_VALUE, BOOLEAN_KEY),
                 new SelectItem(INTEGER_VALUE, INTEGER_KEY),
                 new SelectItem(LONG_VALUE, LONG_KEY),
                 new SelectItem(STRING_VALUE, STRING_KEY)
         };
         typeSelectList.setSelectItems(curSelects);
 
         TYPE_LUT.put(BOOLEAN_KEY, BOOLEAN_VALUE);
         TYPE_LUT.put(LONG_KEY, LONG_VALUE);
         TYPE_LUT.put(STRING_KEY, STRING_VALUE);
         TYPE_LUT.put(INTEGER_KEY, INTEGER_VALUE);
         TYPE_LUT.put(ARRAY_LIST_KEY, ARRAY_LIST_VALUE);
     }
 
     ConfigurationEngine ce = EJBLookup.getConfigurationEngine();
 
     public ConfigurationManagerBean() {
         initHeader();
         init(true);
 
        selectedType = 0;
     }
 
     /**
      * Init the Table header, maybe we should use MessageBean...
      */
     private void initHeader() {
         tableHeader.clear();
         tableHeader.add(mb.getMessage("SysParamConfig.path"));
         tableHeader.add(mb.getMessage("SysParamConfig.name"));
         tableHeader.add(mb.getMessage("SysParamConfig.type"));
         tableHeader.add(mb.getMessage("SysParamConfig.value"));
         tableHeader.add(mb.getMessage("SysParamConfig.actions"));
     }
 
 
     /**
      * initializes the SelectLists and the Tables should be called after edit + delete operations
      *
      * @param changeIDs indicates if this method should change the selected IDs or not
      */
     private void init(boolean changeIDs) {
         initHeader();
         allTables.clear();
         Hashtable<String, List<TableRow>> tableBody = new Hashtable<String, List<TableRow>>();
         List<String> curValue = new ArrayList<String>();
         ArrayList<String> tmpAL;
         ArrayList<SelectItem> tmpKeys = new ArrayList<SelectItem>();
         ArrayList<String> pathList = new ArrayList<String>(40);
         List<TableRow> tmpTable;
         String curGroup;
         String tmpType;
         boolean editable;
         Serializable value;
 
         Map<String, ParameterScope> scopeLUT = new Hashtable<String, ParameterScope>();
         List<String> tmpScopes = new ArrayList<String>();
 
         curPanel = NO_PANEL;
         String tmpName;
         int i;
         try {
             /** init all the Parameter Scopes and put them into the SelectList */
             allScopes = new ArrayList<ParameterScope>();
             for (Field tmpF : ParameterScope.class.getFields()) {
                 tmpName = tmpF.getName();
                 if (!tmpName.endsWith("_ONLY") && !tmpName.equals("GLOBAL")) {
                     tmpScopes.add(tmpF.getName());
                 }
                 try {
                     scopeLUT.put(tmpF.getName(), (ParameterScope) tmpF.get(null));
                 } catch (IllegalAccessException e) {
                     new FxFacesMsgErr(e).addToContext();
                 } catch (Throwable t) {
                     new FxFacesMsgErr(t).addToContext();
                 }
             }
 
             String[] sortedScopes = new String[tmpScopes.size()];
             tmpScopes.toArray(sortedScopes);
             Arrays.sort(sortedScopes);
             for (String s : sortedScopes) {
                 tmpTable = new ArrayList<TableRow>();
                 tableBody.put(s, tmpTable);
                 allScopes.add(scopeLUT.get(s));
             }
 
             /** In the first loop we check all the Values, their types, save the types in the TypeLUT and build the
              * table body */
 
             Map<ParameterData, Serializable> allData = ce.getAll();
             //noinspection unchecked
             for (ParameterData<Serializable> key : allData.keySet()) {
                 curGroup = key.getPath().getScope().toString();
                 tmpTable = tableBody.get(curGroup);
                 if (tmpTable == null) {
                     tmpTable = new ArrayList<TableRow>();
                     tableBody.put(curGroup, tmpTable);
                 }
                 value = allData.get(key);
                 tmpType = value.getClass().toString().replace("class ", "");
                 tmpType = tmpType.replace("java.lang.", "");
 
                 tmpAL = new ArrayList<String>(4);
 
                 tmpAL.add(key.getPath().getValue());
                 tmpAL.add(key.getKey());
                 tmpAL.add(tmpType);
                 curValue.clear();
                 editable = false;
                 Integer tmpI = TYPE_LUT.get(tmpType);
                 if (tmpI != null) {
                     PARAMETER_TYPES.put(key.getKey(), tmpI);
                     if (tmpI == ARRAY_LIST_VALUE) {
                         if (showValues) {
                             try {
                                 @SuppressWarnings({"unchecked"}) List<FxScriptRunInfo> tmpA = (List<FxScriptRunInfo>) value;
                                 for (FxScriptRunInfo sr : tmpA) {
                                     curValue.add(mb.getMessage((sr.isSuccessful() ? "SysParamConfig.value.ok" : "SysParamConfig.value.error"), sr.getName(), (sr.getEndTime() - sr.getStartTime())));
                                 }
                             } catch (Throwable t) {
                                 curValue.add(value.toString());
                             }
                         }
                     } else {
                         curValue.add(value.toString());
                         editable = true;
                     }
                 } else if (showValues) {
                     curValue.add(value.toString());
                 }
                 TableRow tmpTableRow = new TableRow(key.getKey(), tmpAL, editable, curValue);
                 tmpTable.add(tmpTableRow);
                 allRows.put(key.getKey(), tmpTableRow);
                 //noinspection unchecked
                 allParams.put(key.getKey(), ParameterFactory.newInstance((Class<Serializable>) value.getClass(), key));
             }
 
             /** Now we create the scope Select list with the amount of Values in each scope */
             i = 0;
             for (String s : sortedScopes) {
                 List<TableRow> curRows = tableBody.get(s);
                 TableRow[] sortedRows = new TableRow[curRows.size()];
                 tmpKeys.add(new SelectItem(i, s + " (" + curRows.size() + ")"));
                 curRows.toArray(sortedRows);
                 Arrays.sort(sortedRows);
                 curRows.clear();
                 curRows.addAll(Arrays.asList(sortedRows));
                 allTables.add(new MultiTable(s, tableHeader, curRows, i));
                 i++;
             }
             /** Now let us get all the system parameter paths and add them to the right select List */
             for (Field tmpF : SystemParameterPaths.class.getFields()) {
                 i = 2;
                 while (i-- > 0) {
                     try {
                         pathList.add(((SystemParameterPaths) tmpF.get(null)).getValue());
                         i = -1;
                         break;
                     } catch (IllegalAccessException e) {
                         tmpF.setAccessible(true);
                     } catch (Throwable t) {
                         // not type of SystemParameterPaths... so don't care
                     }
                 }
             }
             String[] sortedPaths = new String[pathList.size()];
             pathList.toArray(sortedPaths);
             Arrays.sort(sortedPaths);
             SelectItem[] tmpPaths = new SelectItem[pathList.size()];
             i = 0;
             for (String s : sortedPaths) {
                 tmpPaths[i++] = new SelectItem(i, s);
             }
             pathSelectList = new SelectItemGroup(mb.getMessage("SysParamConfig.path"), mb.getMessage("SysParamConfig.path"), false, tmpPaths);
             if (changeIDs) {
                 selectedPath = 0;
                 currentPathStr = tmpPaths[0].getLabel();
             }
 
             SelectItem[] tmpK = new SelectItem[tmpKeys.size()];
             tmpKeys.toArray(tmpK);
             allKeys.setSelectItems(tmpK);
             if (changeIDs) {
                 curTable = allTables.get(0);
             } else {
                 curTable = allTables.get(selectedKey);
             }
 
         } catch (FxApplicationException e) {
             new FxFacesMsgErr(e).addToContext();
         }
     }
 
     /**
      * This method is called when we select an other scope at the top of the page
      *
      * @return always an empty String
      */
     public String changeTable() {
         curTable = allTables.get(selectedKey);
         curPanel = NO_PANEL;
         return "";
     }
 
     /**
      * This method is called when we select an other Type at the creating "form"
      */
     public void changeType() {
         curPanel = NEW_PANEL;
         editBool = selectedType == BOOLEAN_VALUE;
     }
 
     /**
      * creates a new value
      */
     public void createNew() {
         Class<? extends Serializable> curClass = null;
         Serializable value = null;
         ParameterPath curPath = null;
         // to show all errors at once
         boolean isNOT_OK = false;
         try {
             curPath = new ParameterPathBean(currentPathStr, allScopes.get(selectedKey));
 
         } catch (Throwable e) {
             new FxFacesMsgErr(e).addToContext();
             new FxFacesMsgErr("SysParamConfig.err.unknown", currentPathStr).addToContext();
             isNOT_OK = true;
         }
         new FxFacesMsgInfo("SysParamConfig.info.path", currentPathStr).addToContext();
         if (newName.length() <= 0) {
             new FxFacesMsgErr("SysParamConfig.err.noName").addToContext();
             isNOT_OK = true;
         }
         try {
             switch (selectedType) {
                 case BOOLEAN_VALUE:
                     curClass = Boolean.class;
                     value = curEdit_Bool_Value;
                     break;
                 case INTEGER_VALUE:
                     curClass = Integer.class;
                     value = new Integer(curEdit_STR_Value);
                     break;
                 case LONG_VALUE:
                     curClass = Long.class;
                     value = new Long(curEdit_STR_Value);
                     break;
                 case STRING_VALUE:
                     curClass = String.class;
                     value = curEdit_STR_Value;
                     break;
                 default:
                     throw new IllegalArgumentException(mb.getMessage("SysParamConfig.err.unknown.value"));
             }
         } catch (NumberFormatException nfe) {
             new FxFacesMsgErr(nfe, "SysParamConfig.err.numberFormat", nfe.getMessage()).addToContext();
             isNOT_OK = true;
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             isNOT_OK = true;
         }
         if (isNOT_OK) {
             return;
         }
 
         @SuppressWarnings({"unchecked"})
         Parameter<Serializable> tmpP = ParameterFactory.newInstance((Class) curClass, curPath, newName, value);
         try {
             if (ce.tryGet(tmpP, tmpP.getData().getKey(), true).getFirst()) {
                 throw new IllegalArgumentException(mb.getMessage("SysParamConfig.err.key.exist"));
             }
             ce.put(tmpP, value);
             new FxFacesMsgInfo("SysParamConfig.info.created").addToContext();
             init(false);
             curPanel = NO_PANEL;
         } catch (FxApplicationException e) {
             new FxFacesMsgErr(e).addToContext();
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     /**
      * calls the save() method
      *
      * @param ae not used
      * @see ConfigurationManagerBean#save()
      */
     @SuppressWarnings({"UnusedDeclaration"})
     public void commitItemEditing(ActionEvent ae) {
         save();
     }
 
     /**
      * calls the cancel method
      *
      * @param ae not used
      * @see ConfigurationManagerBean#cancel()
      */
     @SuppressWarnings({"UnusedDeclaration"})
     public void cancelItemEditing(ActionEvent ae) {
         cancel();
     }
 
     public MultiTable getCurTable() {
         return curTable;
     }
 
     public List<MultiTable> getAllTables() {
         return allTables;
     }
 
     public Integer getSelectedType() {
         return selectedType;
     }
 
     /**
      * used by the change of of a SelectList
      *
      * @param selectedType the new selected type
      */
     public void setSelectedType(Integer selectedType) {
         this.selectedType = selectedType;
     }
 
     public boolean isInvalidType() {
         return this.selectedType < 1;
     }
 
     public Integer getSelectedKey() {
         return selectedKey;
     }
 
     public void setSelectedKey(Integer selectedKey) {
         this.selectedKey = selectedKey;
     }
 
     public SelectItem[] getAllKeys() {
         return allKeys.getSelectItems();
     }
 
     public boolean isShowEdit() {
         return curPanel == EDIT_PANEL;
     }
 
     public String getCurEditName() {
         return curEditName;
     }
 
     public void setCurEditName(String curEditName) {
         this.curEditName = curEditName;
     }
 
     public boolean isCurEdit_Bool_Value() {
         return curEdit_Bool_Value;
     }
 
     public void setCurEdit_Bool_Value(boolean curEdit_Bool_Value) {
         this.curEdit_Bool_Value = curEdit_Bool_Value;
     }
 
     public String getCurEdit_STR_Value() {
         strAlreadySet = false;
         return curEdit_STR_Value;
     }
 
     public void setCurEdit_STR_Value(String curEdit_STR_Value) {
         boolean t = this.curEdit_STR_Value.length() <= TA_LENGTH;
         if (t && !strAlreadySet) {
             strAlreadySet = true;
             this.curEdit_STR_Value = curEdit_STR_Value;
         }
     }
 
     public String getCurEdit_TA_Value() {
         strAlreadySet = false;
         return curEdit_STR_Value;
     }
 
     public void setCurEdit_TA_Value(String curEdit_TA_Value) {
         boolean t = this.curEdit_STR_Value.length() > TA_LENGTH;
         if (t && !strAlreadySet) {
             strAlreadySet = true;
             this.curEdit_STR_Value = curEdit_TA_Value;
         }
     }
 
     public String getCurEdit_STR_Backup_Value() {
         return curEdit_STR_Value;
     }
 
     public void setCurEdit_STR_Backup_Value(String curEdit_STR_Backup_Value) {
         this.curEdit_STR_Value = curEdit_STR_Backup_Value;
     }
 
     /**
      * calls only the editCurItem method
      *
      * @param e not used
      * @see ConfigurationManagerBean#editCurItem()
      */
     @SuppressWarnings({"UnusedDeclaration"})
     public void editCurItem(ActionEvent e) {
         editCurItem();
     }
 
     /**
      * is called when the edit commandIcon is pressed
      */
     public void editCurItem() {
         Integer tmpI = PARAMETER_TYPES.get(curEditName);
         if (tmpI == null) {
             init(false);
             tmpI = PARAMETER_TYPES.get(curEditName);
         }
         if (tmpI == null) return;
         String tmpStr;
         try {
             tmpStr = ce.get(allParams.get(curEditName)).toString();
         } catch (FxApplicationException e) {
             new FxFacesMsgErr(e).addToContext();
             return;
         }
         curEdit_Bool_Value = false;
         curEdit_STR_Value = "";
         switch (tmpI) {
             case INTEGER_VALUE:
             case LONG_VALUE:
             case STRING_VALUE:
                 curEdit_STR_Value = tmpStr;
                 curPanel = EDIT_PANEL;
                 editBool = false;
                 break;
             case BOOLEAN_VALUE:
                 curEdit_Bool_Value = Boolean.parseBoolean(tmpStr);
                 curPanel = EDIT_PANEL;
                 editBool = true;
                 break;
             default:
                 break;
         }
     }
 
     /**
      * saves the changes of an edit
      */
     public void save() {
         curPanel = NO_PANEL;
         Integer tmpI = PARAMETER_TYPES.get(curEditName);
         if (tmpI == null) return;
         Parameter<Serializable> curP = allParams.get(curEditName);
 
         try {
             switch (tmpI) {
                 case INTEGER_VALUE:
                     ce.put(curP, Integer.parseInt(curEdit_STR_Value));
                     break;
                 case LONG_VALUE:
                     ce.put(curP, Long.parseLong(curEdit_STR_Value));
                     break;
                 case STRING_VALUE:
                     ce.put(curP, curEdit_STR_Value);
                     break;
                 case BOOLEAN_VALUE:
                     ce.put(curP, curEdit_Bool_Value);
                     break;
                 default:
                     break;
             }
         } catch (NumberFormatException nfe) {
             new FxFacesMsgErr(nfe, "SysParamConfig.err.numberFormat", nfe.getMessage()).addToContext();
             return;
         } catch (FxApplicationException ae) {
             new FxFacesMsgErr(ae).addToContext();
             return;
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             return;
         }
 
         List<String> tmpList = allRows.get(curEditName).getCurValue();
         tmpList.remove(tmpList.size() - 1);
         if (editBool) {
             tmpList.add(curEdit_Bool_Value + "");
         } else {
             tmpList.add(curEdit_STR_Value);
         }
         init(false);
     }
 
     /**
      * @return the length of the String value + 20 but at least 30
      */
     public int getStrLen() {
         return Math.max(curEdit_STR_Value.length() + 20, 30);
     }
 
     public boolean isEditBool() {
         return editBool;
     }
 
     /**
      * called from the edit and create forms
      */
     public void cancel() {
         curPanel = NO_PANEL;
     }
 
    public SelectItemGroup getTypeSelectList() {
        return typeSelectList;
     }
 
     /**
      * @return <code>true</code> if the create-new-panel should be visible
      */
     public boolean isShowNew() {
         return curPanel == NEW_PANEL;
     }
 
     /**
      * @return <code>true</code> if the confirm-delete-panel should be visible
      */
     public boolean isShowConfirm() {
         return curPanel == CONFIRM_PANEL;
     }
 
     /**
      * calls only the showConfirm method
      *
      * @param ae not used
      * @see ConfigurationManagerBean#showConfirm()
      */
     @SuppressWarnings({"UnusedDeclaration"})
     public void showConfirm(ActionEvent ae) {
         showConfirm();
     }
 
     /**
      * calls only the deletKey method
      *
      * @param ae not used
      * @see ConfigurationManagerBean#deleteKey()
      */
     @SuppressWarnings({"UnusedDeclaration"})
     public void deleteItem(ActionEvent ae) {
         deleteKey();
     }
 
     public void showConfirm() {
         curPanel = CONFIRM_PANEL;
     }
 
     /**
      * calls only showNew
      *
      * @param ae not used
      * @see ConfigurationManagerBean#showNew()
      */
     @SuppressWarnings({"UnusedDeclaration"})
     public void showNew(ActionEvent ae) {
         showNew();
     }
 
     /**
     * shows the create-new-panel and set the name and the value to an empty string, and the type to "not choosen"
      */
     public void showNew() {
         curPanel = NEW_PANEL;
 
        selectedType = 0;
         newName = "";
         curEdit_STR_Value = "";
         curEdit_Bool_Value = false;
         editBool = false;
     }
 
     public String getNewName() {
         return newName;
     }
 
     public void setNewName(String newName) {
         this.newName = newName;
     }
 
     public SelectItemGroup getPathSelectList() {
         return pathSelectList;
     }
 
     public Integer getSelectedPath() {
         return selectedPath;
     }
 
     public void setSelectedPath(Integer selectedPath) {
         this.selectedPath = selectedPath;
     }
 
     /**
      * @return <code>true</code> if the edit-value is longer then <code>TA_LENGTH</code> chars
      */
     public boolean isTa() {
         return curEdit_STR_Value.length() > TA_LENGTH;
     }
 
     public void changePath() {
         currentPathStr = pathSelectList.getSelectItems()[selectedPath - 1].getLabel();
     }
 
     /**
      * an empty method wich is called from in an AJAX request to submit the changes in the selected scope
      */
     public void changeScope() {
     }
 
     public void changeShowValues() {
         String tmpPath = currentPathStr;
         Integer tmpKey = selectedKey;
         init(false);
         currentPathStr = tmpPath;
         selectedKey = tmpKey;
         changeTable();
     }
 
     /**
      * calls only the createNew method
      *
      * @param ae not used
      * @see ConfigurationManagerBean#createNew()
      */
     @SuppressWarnings({"UnusedDeclaration"})
     public void createNewItem(ActionEvent ae) {
         createNew();
     }
 
     public boolean isShowValues() {
         return showValues;
     }
 
     public void setShowValues(boolean showValues) {
         this.showValues = showValues;
     }
 
     public void deleteKey() {
         Parameter<Serializable> curP = allParams.get(curEditName);
         try {
             ce.remove(curP);
         } catch (FxApplicationException e) {
             new FxFacesMsgErr(e).addToContext();
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
         curPanel = NO_PANEL;
         init(false);
     }
 
     public String getCurrentPathStr() {
         return currentPathStr;
     }
 
     public String getBoolVisible() {
         return editBool ? STYLE_CLASS_VISIBLE : STYLE_CLASS_HIDDEN;
     }
 
     public String getStringVisible() {
         return (!editBool && !isTa()) ? STYLE_CLASS_VISIBLE : STYLE_CLASS_HIDDEN;
     }
 
     public String getBoolVisibleType() {
         return (editBool && !isInvalidType()) ? STYLE_CLASS_VISIBLE : STYLE_CLASS_HIDDEN;
     }
 
     public String getStringVisibleType() {
         return (!editBool && !isInvalidType()) ? STYLE_CLASS_VISIBLE : STYLE_CLASS_HIDDEN;
     }
 
     public String getTAVisible() {
         return (!editBool && isTa()) ? STYLE_CLASS_VISIBLE : STYLE_CLASS_HIDDEN;
     }
 
     public String getErrorClass() {
         return isInvalidType() ? STYLE_CLASS_RED : STYLE_CLASS_HIDDEN;
     }
 
     public String getColorStyle() {
         return isInvalidType() ? STYLE_CLASS_RED : "";
     }
 
     /**
      * The setter method of the current path
      * the <code>gnoreNextPathStrSet</code> is used to ignore the setting-back when some path is selected from the select list
      *
      * @param currentPathStr the currentPath to set
      * @see ConfigurationManagerBean#changePath()
      * @see ConfigurationManagerBean#setSelectedPath(Integer)
      */
     public void setCurrentPathStr(String currentPathStr) {
         this.currentPathStr = currentPathStr;
     }
 
     /**
      * An utility class for a Table with a name, a List of table header and a list of TableRow
      *
      * @see TableRow
      */
     public static class MultiTable implements Serializable {
         private final String name;
         private final int tID;
         private final List<String> tableHeader;
         private final List<TableRow> tableBody;
 
         /**
          * @param name        the name of the table
          * @param tableHeader the header of the table
          * @param tableBody   the body of the table (each row as an TableRow)
          * @param tID         the current id of the table
          * @see TableRow
          */
         public MultiTable(String name, List<String> tableHeader, List<TableRow> tableBody, int tID) {
             this.name = name;
             this.tableHeader = tableHeader;
             this.tableBody = tableBody;
             this.tID = tID;
         }
 
         public String getName() {
             return name;
         }
 
         public List<String> getTableHeader() {
             return tableHeader;
         }
 
         public List<TableRow> getTableBody() {
             return tableBody;
         }
 
         public int getTID() {
             return tID;
         }
     }
 
     /**
      * An utility class for a table-row
      */
     public static class TableRow implements Serializable, Comparable {
         private final String key;
         private final List<String> tabValues;
         private final boolean editable;
         private final List<String> curValue;
 
         /**
          * @param key       an unique key in the Table, can be used as key in the bean
          * @param tabValues a list of values of the table
          * @param editable  indicates if the table should be editable
          * @param curValue  a list of the value field. Because all values of an array / list is added seperated
          */
         public TableRow(String key, List<String> tabValues, boolean editable, List<String> curValue) {
             this.key = key;
             this.tabValues = tabValues;
             this.editable = editable;
             this.curValue = new ArrayList<String>(curValue);
         }
 
         public String getKey() {
             return key;
         }
 
         public int compareTo(Object o) {
             try {
                 TableRow other = (TableRow) o;
                 return key.compareTo(other.key);
             } catch (Throwable t) {
                 return 0;
             }
         }
 
         public List<String> getTabValues() {
             return tabValues;
         }
 
         public boolean isEditable() {
             return editable;
         }
 
         public List<String> getCurValue() {
             return curValue;
         }
 
         /**
          * @return <code>true</code> if the value field has more than one entry
          */
         public boolean isMulti() {
             return curValue.size() > 1;
         }
     }
 }
