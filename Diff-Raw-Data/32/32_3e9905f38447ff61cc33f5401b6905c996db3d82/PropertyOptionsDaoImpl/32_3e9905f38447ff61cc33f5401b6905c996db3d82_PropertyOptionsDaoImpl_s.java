 package web.dao.impl;
 
 import org.hibernate.criterion.Restrictions;
 import org.springframework.stereotype.Repository;
 import web.dao.PropertyOptionsDao;
 import web.model.Property;
 import web.model.PropertyOptions;
 
 /**
  * @author Romain <ro.foncier@gmail.com>
  */
 
 @Repository
 public class PropertyOptionsDaoImpl extends AbstractDaoImpl<PropertyOptions, Integer> implements PropertyOptionsDao {
 
     protected PropertyOptionsDaoImpl() {
         super(PropertyOptions.class);
     }
     
     @Override
     public PropertyOptions findByProperty(Property property) {
        return findByCriteria(Restrictions.eq("property", property)).get(0);
     }
     
     @Override
     public Boolean savePropertyOptions(PropertyOptions pOptions) {
         if (pOptions == null) {
             return false;
         }
         saveOrUpdate(pOptions);
         return true;
     }
 }
