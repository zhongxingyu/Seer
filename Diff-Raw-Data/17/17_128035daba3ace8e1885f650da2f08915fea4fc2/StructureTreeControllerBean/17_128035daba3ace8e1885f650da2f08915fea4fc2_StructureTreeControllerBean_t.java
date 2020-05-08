 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
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
import com.flexive.shared.CacheAdmin;
import com.flexive.shared.structure.FxPropertyAssignment;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import java.util.Map;
 
 /**
  * Provides getters and setters for boolean flags that
  * control the modification of the structure tree
  *
  * @author Gerhard Glos (gerhard.glos@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 
 public class StructureTreeControllerBean {
     public static final String ACTION_RENAME_TYPE = "RENAME_TYPE";
     public static final String ACTION_RENAME_ASSIGNMENT = "RENAME_ASSIGNMENT";
     public static final String ACTION_RELOAD_SELECT_TYPE = "RELOAD_SELECT_TYPE";
     public static final String ACTION_RELOAD_SELECT_ASSIGNMENT = "RELOAD_SELECT_ASSIGNMENT";
     public static final String ACTION_RELOAD_EXPAND_TYPE = "RELOAD_EXPAND_TYPE";
     public static final String ACTION_RELOAD_EXPAND_ASSIGNMENT = "RELOAD_EXPAND_ASSIGNMENT";
     public static final String ACTION_RENAME_SELECT_TYPE = "RENAME_SELECT_TYPE";
     public static final String ACTION_RENAME_SELECT_ASSIGNMENT = "RENAME_SELECT_ASSIGNMENT";
 
     private String action = null;
     private long id = -1;
     private String value = null;
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public String getAction() {
         return action;
     }
 
     public void setAction(String action) {
         this.action = action;
     }
 
     /*
     public static final String KEY_ACTION="ACTION";
     public static final String KEY_ID="ID";
     public static final String KEY_VALUE="VALUE";
 
     private static List<HashMap> treeActions = null;
 
     public Object[] getTreeActions() {
         return treeActions.toArray();
     }
     */
 
     public void addAction(String action, long id, String value) {
         this.action = action;
         this.value = value;
         this.id = id;
     }
 
     public boolean isDoAction() {
         return action != null;
     }
 
     // action listener for script mapping links; supports navigation from scriptEditor to structureEditor
     public void structureOpener(ActionEvent e) {
         FacesContext context = FacesContext.getCurrentInstance();
         Map requestParams = context.getExternalContext().getRequestParameterMap();
         long oid = -1;
         String action = "";
         if (requestParams.get("oid") != null && requestParams.get("action") != null) {
             oid = Long.valueOf(requestParams.get("oid").toString());
             action = requestParams.get("action").toString();
             // get the bean
             StructureTreeControllerBean s = (StructureTreeControllerBean) FxJsfUtils.getManagedBean("structureTreeControllerBean");
             // action for the tree..
             s.addAction(action, oid, null);
         }
         if (oid != -1 && !action.equals("")) {
             this.id = oid;
             this.action = action;
             // value always set to null...
             this.value = null;
         }
     }
 
 }
