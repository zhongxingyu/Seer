 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 
 package com.sapienter.jbilling.server.item;
 
 import org.apache.log4j.Logger;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.item.db.ItemTypeDAS;
 import com.sapienter.jbilling.server.item.db.ItemTypeDTO;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.DescriptionBL;
 import com.sapienter.jbilling.server.util.audit.EventLogger;
 
 import java.util.List;
 
 public class ItemTypeBL {
     private static final Logger LOG = Logger.getLogger(ItemTypeBL.class);
 
     private ItemTypeDAS itemTypeDas = null;
     private ItemTypeDTO itemType = null;
     private EventLogger eLogger = null;
     
     public ItemTypeBL(Integer itemTypeId)  {
         init();
         set(itemTypeId);
     }
     
     public ItemTypeBL() {
         init();
     }
     
     private void init() {
         eLogger = EventLogger.getInstance();        
         itemTypeDas = new ItemTypeDAS();
     }
 
     public ItemTypeDTO getEntity() {
         return itemType;
     }
     
     public void set(Integer id) {
         itemType = itemTypeDas.find(id);
     }
     
     public void create(ItemTypeDTO dto) {
         itemType = new ItemTypeDTO();
         itemType.setEntity(dto.getEntity());
         itemType.setOrderLineTypeId(dto.getOrderLineTypeId());
         itemType.setDescription(dto.getDescription());
         itemType = itemTypeDas.save(itemType);
     }
     
     public void update(Integer executorId, ItemTypeDTO dto) 
             throws SessionInternalError {
         eLogger.audit(executorId, null, Constants.TABLE_ITEM_TYPE, 
                 itemType.getId(), EventLogger.MODULE_ITEM_TYPE_MAINTENANCE, 
                 EventLogger.ROW_UPDATED, null,  
                 itemType.getDescription(), null);
 
         itemType.setDescription(dto.getDescription());
         itemType.setOrderLineTypeId(dto.getOrderLineTypeId());
     }
     
     public void delete(Integer executorId) {
         if (isInUse()) {
             throw new SessionInternalError("Cannot delete a non-empty item type, remove items before deleting.");
         }
 
         LOG.debug("Deleting item type: " + itemType.getId());
         Integer itemTypeId = itemType.getId();
         itemTypeDas.delete(itemType);
         itemTypeDas.flush();
         itemTypeDas.clear();
 
         // now remove all the descriptions 
         DescriptionBL desc = new DescriptionBL();
         desc.delete(Constants.TABLE_ITEM_TYPE, itemTypeId);
 
         eLogger.audit(executorId, null, Constants.TABLE_ITEM_TYPE, itemTypeId,
                 EventLogger.MODULE_ITEM_TYPE_MAINTENANCE, 
                 EventLogger.ROW_DELETED, null, null,null);
 
     }   
 
     public boolean isInUse() {
         return itemTypeDas.isInUse(itemType.getId());
     }
 
     /**
      * Gets the internal category for plan subscription items. If the category does not
      * exist, it will be created.
      *
      * @param entityId entity id
      * @return plan category
      */
     public ItemTypeDTO getInternalPlansType(Integer entityId) {
         return itemTypeDas.getCreateInternalPlansType(entityId);
     }
 
     /**
      * Returns all item types, or an empty array if none found.
      *
      * @return array of item types, empty if none found.
      */
     public ItemTypeWS[] getAllItemTypes() {
         List<ItemTypeDTO> results = new ItemTypeDAS().findAll();
         ItemTypeWS[] types = new ItemTypeWS[results.size()];
 
         int index = 0;
         for (ItemTypeDTO type : results)
             types[index++] = new ItemTypeWS(type);
 
         return types;
     }
 
     /**
      * Checks to see a category with the same description already exists.
      * @param description Description to use to find an existent category.
      * @return <b>true</b> if another category exists. <b>false</b> if no category with the same description exists.
      */
    public boolean exists(String description) {
         if (description == null) {
             LOG.error("exists is being call with a null description");
             return true;
         }
        if (new ItemTypeDAS().findByDescription(description) == null) {
             return false;
         } else {
             return true;
         }
     }
 }
