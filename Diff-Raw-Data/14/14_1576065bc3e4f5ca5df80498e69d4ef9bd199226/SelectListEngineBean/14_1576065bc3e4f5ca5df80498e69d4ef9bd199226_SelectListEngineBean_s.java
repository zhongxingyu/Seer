 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
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
 package com.flexive.ejb.beans.structure;
 
 import com.flexive.core.Database;
 import static com.flexive.core.DatabaseConst.*;
 import com.flexive.core.LifeCycleInfoImpl;
 import com.flexive.core.structure.StructureLoader;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.cache.FxCacheException;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxCreateException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.interfaces.SelectListEngine;
 import com.flexive.shared.interfaces.SelectListEngineLocal;
 import com.flexive.shared.interfaces.SequencerEngine;
 import com.flexive.shared.interfaces.SequencerEngineLocal;
 import com.flexive.shared.structure.FxSelectList;
 import com.flexive.shared.structure.FxSelectListEdit;
 import com.flexive.shared.structure.FxSelectListItem;
 import com.flexive.shared.structure.FxSelectListItemEdit;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * SelectListEngine implementation
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Stateless(name = "SelectListEngine")
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class SelectListEngineBean implements SelectListEngine, SelectListEngineLocal {
 
     private static transient Log LOG = LogFactory.getLog(SelectListEngineBean.class);
     @Resource
     javax.ejb.SessionContext ctx;
 
     @EJB
     SequencerEngineLocal seq;
 
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long save(FxSelectListEdit list) throws FxApplicationException {
         final boolean newList = list.isNew();
         boolean changes = newList;
         long id = list.getId();
         if (newList) {
             id = createList(list);
         } else {
             if (list.changes()) {
                 updateList(list);
                 changes = true;
             }
         }
         
         if (!newList) {
             //remove selct items that are no longer referenced by this list
             List<FxSelectListItem> originalItems = CacheAdmin.getEnvironment().getSelectList(list.getId()).getItems();
             List<FxSelectListItem> editedItems = list.getItems();
             for (FxSelectListItem i : originalItems) {
                boolean found = false;
                for (FxSelectListItem j: editedItems){
                     if (i.getId() == j.getId()) {
                         found = true;
                         break;
                     }
                 }
                 if (!found)
                     remove(i);
             }
         }
 
         FxSelectListItemEdit e;
         long defaultItemId = 0;
         long currentItemId;
         if (list.hasDefaultItem())
             defaultItemId = list.getDefaultItem().getId();
         for (FxSelectListItem item : list.getItems()) {
             e = (item instanceof FxSelectListItemEdit ? (FxSelectListItemEdit) item : item.asEditable());
             if (newList) {
                 currentItemId = createItem(e);
                 //get the new created default item id
                 if (defaultItemId < 0 && e.getId() == defaultItemId)
                     defaultItemId = currentItemId;
             } else if( e.isNew() ) {
                     createItem(e);
                     changes = true;
             } else if (e.changes()) {
                 //check if the user is permitted to edit this specific item
                 if (FxContext.get().getTicket().isInRole(Role.SelectListEditor) || FxContext.get().getTicket().mayCreateACL(e.getAcl().getId(), -1)) {
                     updateItem(e);
                     changes = true;
                 }
                  else
                     throw new FxNoAccessException("ex.role.notInRole", Role.SelectListEditor.getName());
             }
         }
         updateDefaultItem(id, defaultItemId);
         if (!changes) {
             FxSelectListItem orgDef = CacheAdmin.getEnvironment().getSelectList(id).getDefaultItem();
             if ((orgDef == null && defaultItemId != 0) || (orgDef != null && defaultItemId <= 0) ||
                     (orgDef != null && defaultItemId != 0 && orgDef.getId() != defaultItemId))
                 changes = true;
         }
         try {
             if (changes)
                 StructureLoader.reload(null);
         } catch (FxCacheException e1) {
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e1, "ex.cache", e1.getMessage());
         }
         return id;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long save(FxSelectListItemEdit item) throws FxApplicationException {
         long id = item.getId();
         if (item.isNew())
             id = createItem(item);
         else if (item.changes()) {
             FxPermissionUtils.checkRole(FxContext.get().getTicket(), Role.SelectListEditor);
             updateItem(item);
         } else
             return id;
         try {
             StructureLoader.reload(null);
         } catch (FxCacheException e1) {
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e1, "ex.cache", e1.getMessage());
         }
         return id;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void remove(FxSelectList list) throws FxApplicationException {
 //        System.out.println("Removing list " + list.getLabel());
         FxPermissionUtils.checkRole(FxContext.get().getTicket(), Role.SelectListEditor);
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //fix list references
             StringBuilder sb = new StringBuilder(500).append("UPDATE ").append(TBL_SELECTLIST).append(" SET PARENTID=? WHERE PARENTID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setNull(1, java.sql.Types.INTEGER);
             ps.setLong(2, list.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //list translations
             sb.append("DELETE FROM ").append(TBL_SELECTLIST).append(ML).append(" WHERE ID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setLong(1, list.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //item translations
             sb.append("DELETE FROM ").append(TBL_SELECTLIST_ITEM).append(ML).append(" WHERE ID IN(SELECT DISTINCT ID FROM ").
                     append(TBL_SELECTLIST_ITEM).append(" WHERE LISTID=?)");
             ps = con.prepareStatement(sb.toString());
             ps.setLong(1, list.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //fix item references
             sb.append("UPDATE ").append(TBL_SELECTLIST_ITEM).append(" i1, ").append(TBL_SELECTLIST_ITEM).
                     append(" i2 SET i1.PARENTID=? WHERE i1.PARENTID=i2.ID AND i2.LISTID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setNull(1, java.sql.Types.INTEGER);
             ps.setLong(2, list.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //items
             sb.append("DELETE FROM ").append(TBL_SELECTLIST_ITEM).append(" WHERE LISTID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setLong(1, list.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //the entry itself
             sb.append("DELETE FROM ").append(TBL_SELECTLIST).append(" WHERE ID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setLong(1, list.getId());
             ps.executeUpdate();
             StructureLoader.reload(null);
         } catch (SQLException e) {
             e.printStackTrace();
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCacheException e1) {
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e1, "ex.cache", e1.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void remove(FxSelectListItem item) throws FxApplicationException {
 //        System.out.println("Removing item " + item.getLabel());
         if (!(FxContext.get().getTicket().isInRole(Role.SelectListEditor) || FxContext.get().getTicket().mayDeleteACL(item.getList().getCreateItemACL().getId(), FxContext.get().getTicket().getUserId())))
             throw new FxNoAccessException("ex.selectlist.item.remove.noPerm", item.getList().getLabel(),
                         item.getAcl().getLabel());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //references (parent items)
             StringBuilder sb = new StringBuilder(500).append("UPDATE ").append(TBL_SELECTLIST_ITEM).append(" SET PARENTID=? WHERE PARENTID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setNull(1, java.sql.Types.INTEGER);
             ps.setLong(2, item.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //translations
             sb.append("DELETE FROM ").append(TBL_SELECTLIST_ITEM).append(ML).append(" WHERE ID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setLong(1, item.getId());
             ps.executeUpdate();
             ps.close();
             sb.setLength(0);
             //the entry itself
             sb.append("DELETE FROM ").append(TBL_SELECTLIST_ITEM).append(" WHERE ID=?");
             ps = con.prepareStatement(sb.toString());
             ps.setLong(1, item.getId());
             ps.executeUpdate();
             StructureLoader.reload(null);
         } catch (SQLException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCacheException e1) {
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e1, "ex.cache", e1.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     private long createList(FxSelectListEdit list) throws FxApplicationException {
         FxPermissionUtils.checkRole(FxContext.get().getTicket(), Role.SelectListEditor);
         checkValidListParameters(list);
         long newId = seq.getId(SequencerEngine.System.SELECTLIST);
         list._synchronizeId(newId);
 //        System.out.println("Creating list " + list.getLabel() + " new id is " + newId);
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //                                                            1  2        3    4
             ps = con.prepareStatement("INSERT INTO " + TBL_SELECTLIST + "(ID,PARENTID,NAME,ALLOW_ITEM_CREATE," +
                     //5              6            7
                     "ACL_CREATE_ITEM,ACL_ITEM_NEW,DEFAULT_ITEM)VALUES(?,?,?,?,?,?,?)");
             ps.setLong(1, newId);
             if (list.hasParentList())
                 ps.setLong(2, list.getParentList().getId());
             else
                 ps.setNull(2, java.sql.Types.INTEGER);
             ps.setString(3, list.getName().trim());
             ps.setBoolean(4, list.isAllowDynamicItemCreation());
             ps.setLong(5, list.getCreateItemACL().getId());
             ps.setLong(6, list.getNewItemACL().getId());
             ps.setNull(7, java.sql.Types.INTEGER);
             ps.executeUpdate();
             Database.storeFxString(new FxString[]{list.getLabel(), list.getDescription()},
                     con, TBL_SELECTLIST, new String[]{"LABEL", "DESCRIPTION"}, "ID", newId);
             return newId;
         } catch (SQLException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     /**
      * Update a select lists default item by setting it to <code>null</code> or its value for the list
      *
      * @param listId        the affected list
      * @param defaultItemId the default item to set (no checks for correct lists are made here!!)
      * @throws FxApplicationException on errors
      */
     private void updateDefaultItem(long listId, long defaultItemId) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //                                                                        1          2
             ps = con.prepareStatement("UPDATE " + TBL_SELECTLIST + " SET DEFAULT_ITEM=? WHERE ID=?");
             if (defaultItemId <= 0)
                 ps.setNull(1, java.sql.Types.INTEGER);
             else
                 ps.setLong(1, defaultItemId);
             ps.setLong(2, listId);
             ps.executeUpdate();
         } catch (SQLException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     private void updateList(FxSelectListEdit list) throws FxApplicationException {
         if (!list.changes())
             return;
         FxPermissionUtils.checkRole(FxContext.get().getTicket(), Role.SelectListEditor);
         checkValidListParameters(list);
 //        System.out.println("Updating list " + list.getLabel());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //                                                                    1      2                   3
             ps = con.prepareStatement("UPDATE " + TBL_SELECTLIST + " SET PARENTID=?,NAME=?,ALLOW_ITEM_CREATE=?," +
                     //               4                  5          6
                    "ACL_CREATE_ITEM=?,ACL_ITEM_NEW=? WHERE ID=?)");
             if (list.hasParentList())
                 ps.setLong(1, list.getParentList().getId());
             else
                 ps.setNull(1, java.sql.Types.INTEGER);
             ps.setString(2, list.getName().trim());
             ps.setBoolean(3, list.isAllowDynamicItemCreation());
             ps.setLong(4, list.getCreateItemACL().getId());
             ps.setLong(5, list.getNewItemACL().getId());
             ps.executeUpdate();
             Database.storeFxString(new FxString[]{list.getLabel(), list.getDescription()},
                     con, TBL_SELECTLIST, new String[]{"LABEL", "DESCRIPTION"}, "ID", list.getId());
         } catch (SQLException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
 
     }
 
     private long createItem(FxSelectListItemEdit item) throws FxApplicationException {
         checkValidItemParameters(item);
         UserTicket ticket = FxContext.get().getTicket();
         if(!ticket.isInRole(Role.SelectListEditor)) {
             //check the lists ACL
             if(! (ticket.mayCreateACL(item.getList().getCreateItemACL().getId(), ticket.getUserId())) )
                 throw new FxNoAccessException("ex.selectlist.item.create.noPerm", item.getList().getLabel(), 
                         item.getList().getCreateItemACL().getLabel());
         }
         long newId = seq.getId(SequencerEngine.System.SELECTLIST_ITEM);
 //        System.out.println("Creating item " + item.getLabel() + " for list with id " + item.getList().getId());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //                                                                 1  2   3        4      5    6
             ps = con.prepareStatement("INSERT INTO " + TBL_SELECTLIST_ITEM + "(ID,ACL,PARENTID,LISTID,DATA,COLOR," +
                     //7         8          9           10          11      12       13
                     "CREATED_BY,CREATED_AT,MODIFIED_BY,MODIFIED_AT,DBIN_ID,DBIN_VER,DBIN_QUALITY)VALUES" +
                     "(?,?,?,?,?,?,?,?,?,?,?,?,?)");
             ps.setLong(1, newId);
             ps.setLong(2, item.getAcl().getId());
             if (item.hasParentItem())
                 ps.setLong(3, item.getParentItem().getId());
             else
                 ps.setNull(3, java.sql.Types.INTEGER);
             ps.setLong(4, item.getList().getId());
             ps.setString(5, item.getData());
             ps.setString(6, item.getColor());
             LifeCycleInfoImpl.store(ps, 7, 8, 9, 10);
             ps.setLong(11, item.getIconId());
             ps.setInt(12, item.getIconVer());
             ps.setInt(13, item.getIconQuality());
             ps.executeUpdate();
             Database.storeFxString(item.getLabel(),
                     con, TBL_SELECTLIST_ITEM, "LABEL", "ID", newId);
             return newId;
         } catch (SQLException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     private void updateItem(FxSelectListItemEdit item) throws FxApplicationException {
         if (!item.changes())
             return;
         checkValidItemParameters(item);
 //        System.out.println("Updating item " + item.getLabel());
         if (!(FxContext.get().getTicket().isInRole(Role.SelectListEditor) || FxContext.get().getTicket().mayEditACL(item.getAcl().getId(), FxContext.get().getTicket().getUserId())))
             throw new FxNoAccessException("ex.selectlist.item.update.noPerm", item.getList().getLabel(), 
                         item.getAcl().getLabel());
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             //                                                                   1          2      3       4
             ps = con.prepareStatement("UPDATE " + TBL_SELECTLIST_ITEM + " SET ACL=?,PARENTID=?,DATA=?,COLOR=?," +
                     //           5             6         7          8              9          10
                     "MODIFIED_BY=?,MODIFIED_AT=?,DBIN_ID=?,DBIN_VER=?,DBIN_QUALITY=? WHERE ID=?");
             ps.setLong(1, item.getAcl().getId());
             if (item.hasParentItem())
                 ps.setLong(2, item.getParentItem().getId());
             else
                 ps.setNull(2, java.sql.Types.INTEGER);
             ps.setString(3, item.getData());
             ps.setString(4, item.getColor());
             LifeCycleInfoImpl.updateLifeCycleInfo(ps, 5, 6);
             ps.setLong(7, item.getIconId());
             ps.setInt(8, item.getIconVer());
             ps.setInt(9, item.getIconQuality());
             ps.setLong(10, item.getId());
             ps.executeUpdate();
             Database.storeFxString(item.getLabel(),
                     con, TBL_SELECTLIST_ITEM, "LABEL", "ID", item.getId());
         } catch (SQLException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
     }
 
     private void checkValidListParameters(FxSelectListEdit list) throws FxInvalidParameterException {
         //TODO: codeme!
         if (list.getName() == null || list.getName().equals(""))
             throw new FxInvalidParameterException("Name", "ex.selectlist.name.empty");
     }
 
     private void checkValidItemParameters(FxSelectListItemEdit item) throws FxInvalidParameterException {
         //TODO: codeme!
         if (item.getLabel() == null || item.getLabel().getIsEmpty()) {
             throw new FxInvalidParameterException("Label", "ex.selectlist.item.label.emty");
         }
     }
 }
