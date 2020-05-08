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
 package com.sapienter.jbilling.server.item.db;
 
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.db.AbstractDAS;
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 public class ItemTypeDAS extends AbstractDAS<ItemTypeDTO> {
 
     public static final String PLANS_INTERNAL_CATEGORY_NAME = "plans";
 
     /**
      * Returns true if the given item type ID is in use.
      *
      * @param typeId type id
      * @return true if in use, false if not
      */
     public boolean isInUse(Integer typeId) {
         Criteria criteria = getSession().createCriteria(getPersistentClass())
                 .add(Restrictions.eq("id", typeId))
                 .createAlias("items", "item")
                 .add(Restrictions.eq("item.deleted", 0)) // item type contains non-deleted items
                 .setProjection(Projections.count("item.id"));
 
         criteria.setComment("ItemTypeDTO.isInUse");
 
         return (criteria.uniqueResult() != null && ((Integer) criteria.uniqueResult()) > 0);
     }
 
     /**
      * Returns the internal category for plan subscription items.
      *
      * @param entityId entity id
      * @return plans internal category
      */
     public ItemTypeDTO getCreateInternalPlansType(Integer entityId) {
         Criteria criteria = getSession().createCriteria(getPersistentClass())
                 .add(Restrictions.eq("description", PLANS_INTERNAL_CATEGORY_NAME))
                 .add(Restrictions.eq("internal", true))
                 .add(Restrictions.eq("entity.id", entityId));
 
         ItemTypeDTO type = (ItemTypeDTO) criteria.uniqueResult();
 
         // create if category does not exist
         if (type == null) {
             type = new ItemTypeDTO();
             type.setEntity(new CompanyDTO(entityId));
             type.setDescription(PLANS_INTERNAL_CATEGORY_NAME);
             type.setInternal(true);
             type.setOrderLineTypeId(Constants.ORDER_LINE_TYPE_ITEM);
 
             type = save(type);
         }
 
         return type;
     }
 
     /**
      * Returns the category that has the specified description.
      * @param description Description.
      * @return The category that matches the description or null if no category was found.
      */
    public ItemTypeDTO findByDescription(Integer entityId, String description) {
         Criteria criteria = getSession().createCriteria(ItemTypeDTO.class)
        		.add(Restrictions.eq("entity.id", entityId))
                 .add(Restrictions.eq("description", description));
 
         return (ItemTypeDTO) criteria.uniqueResult();
     }
 }
