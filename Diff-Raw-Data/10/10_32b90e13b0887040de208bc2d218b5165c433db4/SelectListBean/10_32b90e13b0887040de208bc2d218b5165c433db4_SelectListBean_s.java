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
 package com.flexive.war.beans.admin.main;
 
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.FxFormatUtils;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.structure.FxSelectList;
 import com.flexive.shared.structure.FxSelectListEdit;
 import com.flexive.shared.structure.FxSelectListItemEdit;
 import com.flexive.shared.value.FxString;
 
 import javax.faces.model.SelectItem;
 import java.util.*;
 
 /**
  * Bean to display and edit FxSelectList objects and FxSelectListItem objects
  *
  * @author Gerhard Glos (gerhard.glos@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 
 public class SelectListBean {
     //used to filter out the select lists with id's 0 up to DELIMITER and prevent them from being edited
     private static final long SYSTEM_INTERNAL_LISTS_DELIMITER = 0;
     private FxSelectListEdit selectList = null;
     private long selectListId = -1;
     private String selectListName =null;
     private FxString selectListLabel = new FxString("");
     private FxString selectListDescription = new FxString("");
     private boolean selectListAllowDynamicCreation = true;
     private long selectListCreateItemACLId = ACL.Category.SELECTLIST.getDefaultId();
     private long selectListDefaultItemACLId = ACL.Category.SELECTLISTITEM.getDefaultId();
 
     private long listItemId = -1;
     private FxString itemLabel = new FxString("");
     private ACL itemACL = null;
     private String itemData =null;
     private String itemColor =FxFormatUtils.DEFAULT_COLOR;
     private ItemIdSorter sorter = new ItemIdSorter();
 
     /**
      * hack to generate unique id for the UI delete button, which can be used in java script
      *
      * @return unique id string for delete button
      */
     public Map<Long, String> getIdMap() {
         return FxSharedUtils.getMappedFunction(new FxSharedUtils.ParameterMapper<Long, String>() {
             public String get(Object key) {
                 long id = (Long) key;
                 if (id >= 0)
                     return String.valueOf(id);
                 else
                    return "_" + String.valueOf(id * -1);
             }
         });
     }
 
     public FxString getItemLabel() {
         return itemLabel;
     }
 
     public void setItemLabel(FxString itemLabel) {
         this.itemLabel = itemLabel;
     }
 
     public ACL getItemACL() {
         return itemACL;
     }
 
     public void setItemACL(ACL itemACL) {
         this.itemACL = itemACL;
     }
 
     public String getItemData() {
         return itemData;
     }
 
     public void setItemData(String itemData) {
         this.itemData = itemData;
     }
 
     public String getItemColor() {
         return itemColor;
     }
 
     public void setItemColor(String itemColor) {
         this.itemColor = itemColor;
     }
 
     public long getListItemId() {
         return listItemId;
     }
 
     public void setListItemId(long listItemId) {
         this.listItemId = listItemId;
     }
 
     public long getSelectListId() {
         return selectListId;
     }
 
     public void setSelectListId(long selectListId) {
         this.selectListId = selectListId;
     }
 
     public String getSelectListName() {
         return selectListName;
     }
 
     public void setSelectListName(String selectListName) {
         this.selectListName = selectListName;
     }
 
     public FxString getSelectListLabel() {
         return selectListLabel;
     }
 
     public void setSelectListLabel(FxString selectListLabel) {
         this.selectListLabel = selectListLabel;
     }
 
     public FxString getSelectListDescription() {
         return selectListDescription;
     }
 
     public void setSelectListDescription(FxString selectListDescription) {
         this.selectListDescription = selectListDescription;
     }
 
     public boolean isSelectListAllowDynamicCreation() {
         return selectListAllowDynamicCreation;
     }
 
     public void setSelectListAllowDynamicCreation(boolean selectListAllowDynamicCreation) {
         this.selectListAllowDynamicCreation = selectListAllowDynamicCreation;
     }
 
     public long getSelectListCreateItemACLId() {
         return selectListCreateItemACLId;
     }
 
     public void setSelectListCreateItemACLId(long selectListCreateItemACL) {
         this.selectListCreateItemACLId = selectListCreateItemACL;
     }
 
     public ACL getSelectListDefaultItemACL() {
         return CacheAdmin.getEnvironment().getACL(selectListDefaultItemACLId);
     }
 
     public void setSelectListDefaultItemACL(ACL selectListDefaultItemACL) {
         this.selectListDefaultItemACLId = selectListDefaultItemACL.getId();
         setItemACL(selectListDefaultItemACL);
     }
 
     public FxSelectListEdit getSelectList() {
         return selectList;
     }
 
     /**
      * filters out select list items which the current user is not allowed to see (==user doesn't have read permission)
      * and sorts them by id.
      *
      * @return  filtered select list items sorted by id.
      */
     public List<FxSelectListItemEdit> getItems() {
         List<FxSelectListItemEdit> items = new ArrayList<FxSelectListItemEdit>();
         for (FxSelectListItemEdit i : selectList.getEditableItems())
             if (FxJsfUtils.getRequest().getUserTicket().isInRole(Role.SelectListEditor) ||
                     FxJsfUtils.getRequest().getUserTicket().mayReadACL(i.getAcl().getId(), FxJsfUtils.getRequest().getUserTicket().getUserId()))
                     items.add(i);
         Collections.sort(items, sorter);
         return items;
     }
 
     /**
      * Map containing boolean values, if the current user may create select list items
      * for a given select list (id of the select list is used as key).
      *
      * @return  Map containing id's of select lists as keys and if the current user may create
      * select list items as values.
      */
     public Map<Long, Boolean> getMayCreateItems() {
         return new HashMap<Long,Boolean>() {
             public Boolean get(Object key) {
                 if (FxJsfUtils.getRequest().getUserTicket().isInRole(Role.SelectListEditor))
                     return true;
                 else {
                     return FxJsfUtils.getRequest().getUserTicket().mayCreateACL(
                             CacheAdmin.getEnvironment().getSelectList((Long)key).getCreateItemACL().getId(), FxJsfUtils.getRequest().getUserTicket().getUserId());
                 }
             }
         };
     }
 
     public boolean getMayDeleteItems() {
         return FxJsfUtils.getRequest().getUserTicket().isInRole(Role.SelectListEditor)
                 || FxJsfUtils.getRequest().getUserTicket().mayDeleteACL(
                     selectList.getCreateItemACL().getId(), FxJsfUtils.getRequest().getUserTicket().getUserId());
     }
 
     /**
      * Returns if the current user may edit a specific select list item.
      *
      * @return  if the current user may edit a specific select list item
      */
     public Map<FxSelectListItemEdit, Boolean> getMayEditItem() {
         return new HashMap<FxSelectListItemEdit,Boolean>() {
             public Boolean get(Object key) {
                 if (FxJsfUtils.getRequest().getUserTicket().isInRole(Role.SelectListEditor))
                     return true;
                 else {
                     return FxJsfUtils.getRequest().getUserTicket().mayEditACL(
                             ((FxSelectListItemEdit)key).getAcl().getId(), FxJsfUtils.getRequest().getUserTicket().getUserId());
                 }
             }
         };
     }
 
    /**
      * Returns all available select item acl's.
      *
      * @return all available select list item acl's
      *
      */
 
     public List<SelectItem> getSelectListItemACLs() {
         return FxJsfUtils.asSelectList(CacheAdmin.getEnvironment().getACLs(ACL.Category.SELECTLISTITEM), false);
     }
 
     private class ItemIdSorter implements Comparator<FxSelectListItemEdit> {
         public int compare(FxSelectListItemEdit i1, FxSelectListItemEdit i2) {
             if ( i1.getId() >=0 && i2.getId() >=0)
                 return (int) (i1.getId() - i2.getId());
             else if(i1.getId() <0 && i2.getId() <0)
                 return (int) -(i1.getId() - i2.getId());    
             else if (i1.getId() <0 && i2.getId() >=0)
                 return 1;
             else if (i1.getId() >=0 && i2.getId() <0)
                 return -1;
             else //should never happen
                 return 0;
         }
     }
 
     public List<FxSelectList> getSelectLists() {
         return doFilter(CacheAdmin.getEnvironment().getSelectLists());
     }
 
     private List<FxSelectList> doFilter(List<FxSelectList> selectLists) {
         List<FxSelectList> filtered = new ArrayList<FxSelectList>();
         for (FxSelectList s : selectLists) {
             if (s.getId() < 0 || s.getId() > SYSTEM_INTERNAL_LISTS_DELIMITER)
                 filtered.add(s);
         }
         return filtered;
     }
 
     public String showSelectListOverview() {
         reset();
         return "selectListOverview";
     }
 
     /**
      * Function is called ONLY from create.xhtml hence it can be also
      * used to check permissions
      *
      * @return
      */
     public String getResetSelectList() {
         reset();
         if (!FxJsfUtils.getRequest().getUserTicket().isInRole(Role.SelectListEditor))
             new FxFacesMsgErr(new FxNoAccessException("ex.role.notInRole", Role.SelectListEditor.getName())).addToContext();
         return null;
     }
 
     public String showCreateSelectList() {
         reset();
         return "createSelectList";
     }
 
     public String showEditSelectList() {
         return "editSelectList";
     }
 
     private void reset() {
         selectListName = null;
         selectListLabel = new FxString("");
         selectListDescription = new FxString("");
         selectListAllowDynamicCreation = true;
         selectListCreateItemACLId = ACL.Category.SELECTLIST.getDefaultId();
         selectListDefaultItemACLId = ACL.Category.SELECTLISTITEM.getDefaultId();
         itemLabel = new FxString("");
         itemACL = CacheAdmin.getEnvironment().getACL(selectListDefaultItemACLId);
         itemData = null;
         itemColor = FxFormatUtils.DEFAULT_COLOR;
     }
 
     public String createSelectList() {
         try {
             FxPermissionUtils.checkRole(FxJsfUtils.getRequest().getUserTicket(), Role.SelectListEditor);
             selectListId = EJBLookup.getSelectListEngine().save(
                     FxSelectListEdit.createNew(selectListName, selectListLabel,
                             selectListDescription, selectListAllowDynamicCreation,
                             CacheAdmin.getEnvironment().getACL(selectListCreateItemACLId),
                             CacheAdmin.getEnvironment().getACL(selectListDefaultItemACLId))
             );
             reset();
             return initEditing();
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             return null;
         }
     }
 
     public String initEditing() {
         selectList = CacheAdmin.getEnvironment().getSelectList(selectListId).asEditable();
         setSelectListAllowDynamicCreation(selectList.isAllowDynamicItemCreation());
         setSelectListDefaultItemACL(selectList.getNewItemACL());
         setSelectListCreateItemACLId(selectList.getCreateItemACL().getId());
         setSelectListDescription(selectList.getDescription() == null ? new FxString("") : selectList.getDescription());
         setSelectListLabel(selectList.getLabel() == null ? new FxString("") : selectList.getLabel());
         setSelectListName(selectList.getName());
         setItemACL(selectList.getNewItemACL());
 
         return showEditSelectList();
     }
 
     public void deleteSelectList() {
         try {
             FxPermissionUtils.checkRole(FxJsfUtils.getRequest().getUserTicket(), Role.SelectListEditor);
             EJBLookup.getSelectListEngine().remove(CacheAdmin.getEnvironment().getSelectList(selectListId));
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public void deleteListItem() {
         try {
             //check if the user has permission
            if (getMayDeleteItems())
                 selectList.removeItem(listItemId);
             else
                 throw new FxNoAccessException("ex.selectlist.item.remove.noPerm", selectList.getLabel(), selectList.getCreateItemACL().getLabel());
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public void addListItem() {
         try {
             //check if the user has permission
             if (getMayCreateItems().get(getSelectListId())) {
                 new FxSelectListItemEdit(itemACL, selectList, itemLabel, itemData, FxFormatUtils.processColorString("Color",itemColor));
                 itemLabel = new FxString("");
                 itemACL = selectList.getNewItemACL();
                 itemData = null;
                 itemColor = FxFormatUtils.DEFAULT_COLOR;
             }
             //else provoke exception
             else
                 throw new FxNoAccessException("ex.selectlist.item.create.noPerm", selectList.getLabel(), selectList.getCreateItemACL().getLabel());
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public Map<String, String>getColor() {
         return new HashMap<String, String>() {
             public String get(Object key) {
                 String RGBCode = null;
                 try {
                     RGBCode = FxFormatUtils.processColorString("color", (String)key);
                 }
                 catch (Exception e) {
                     //exception is ok, original values are used
                 }
                 return (RGBCode == null ? (String) key : RGBCode);
             }
         };
     }
 
     public String saveSelectList() {
         try {
             //set and check default colors if the user has permission,
             // if not just store them to DB as set
             for (FxSelectListItemEdit i : selectList.getEditableItems()) {
                 if (getMayEditItem().get(i))
                     i.setColor(FxFormatUtils.processColorString("Color", i.getColor()));
             }
             EJBLookup.getSelectListEngine().save(selectList);
             reset();
             return showSelectListOverview();
         }
         catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             return null;
         }
     }
 }
