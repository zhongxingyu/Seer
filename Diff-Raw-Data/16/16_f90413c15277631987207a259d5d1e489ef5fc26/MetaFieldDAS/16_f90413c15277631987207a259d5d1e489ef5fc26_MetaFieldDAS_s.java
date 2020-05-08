 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.metafields.db;
 
 import com.sapienter.jbilling.server.util.db.AbstractDAS;
 import org.hibernate.Session;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import java.util.List;
 
 /**
  * @author Brian Cowdery
  * @since 03-Oct-2011
  */
 public class MetaFieldDAS extends AbstractDAS<MetaField> {
 
     @SuppressWarnings("unchecked")
     public List<MetaField> getAvailableFields(Integer entityId, EntityType entityType) {
         DetachedCriteria query = DetachedCriteria.forClass(MetaField.class);
         query.add(Restrictions.eq("entity.id", entityId));
         query.add(Restrictions.eq("entityType", entityType));
         query.addOrder(Order.asc("displayOrder"));
         return getHibernateTemplate().findByCriteria(query);
     }
 
     @SuppressWarnings("unchecked")
     public MetaField getFieldByName(Integer entityId, EntityType entityType, String name) {
         DetachedCriteria query = DetachedCriteria.forClass(MetaField.class);
         query.add(Restrictions.eq("entity.id", entityId));
         query.add(Restrictions.eq("entityType", entityType));
         query.add(Restrictions.eq("name", name));
         List<MetaField> fields = getHibernateTemplate().findByCriteria(query);
         return !fields.isEmpty() ? fields.get(0) : null;
     }
 
     public void deleteMetaFieldValuesForEntity(EntityType entityType, int metaFieldId) {
         Session session = getSession();
         String deleteFromEntitiesSql = "delete from ";
         switch (entityType) {
            case INVOICE:
                deleteFromEntitiesSql += " invoice_meta_field_map ";
                break;
            case CUSTOMER:
                deleteFromEntitiesSql += " customer_meta_field_map ";
                break;
           case ITEM:
                deleteFromEntitiesSql += " item_meta_field_map ";
                break;
            case ORDER:
                deleteFromEntitiesSql += " order_meta_field_map ";
                break;
            case PAYMENT:
                deleteFromEntitiesSql += " payment_meta_field_map ";
                break;
         }
         deleteFromEntitiesSql += " where meta_field_value_id in " +
                 "(select val.id from meta_field_value val where meta_field_name_id = " + metaFieldId + " )";
         session.createSQLQuery(deleteFromEntitiesSql).executeUpdate();
 
         String deleteValuesHql = "delete from " + MetaFieldValue.class.getSimpleName() + " where field.id = ?";
         getHibernateTemplate().bulkUpdate(deleteValuesHql, metaFieldId);
     }
 }
