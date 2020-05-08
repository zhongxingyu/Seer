 package org.motechproject.carereporting.dao.impl;
 
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Restrictions;
 import org.motechproject.carereporting.dao.IndicatorDao;
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.springframework.stereotype.Component;
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 
 @Component
 public class IndicatorDaoHibernateImpl extends GenericDaoHibernateImpl<IndicatorEntity> implements IndicatorDao {
 
     @Override
     @SuppressWarnings("unchecked")
     public Set<IndicatorEntity> getIndicatorsByClassificationId(Integer classificationId) {
         Criteria criteria = getCurrentSession()
                 .createCriteria(IndicatorEntity.class)
                 .createAlias("classifications", "classification")
                 .add(Restrictions.eq("classification.id", classificationId));
         return new LinkedHashSet<IndicatorEntity>(criteria.list());
     }
 
     @Override
     public IndicatorEntity getIndicatorByName(String name) {
         Criteria criteria = getCurrentSession()
                 .createCriteria(IndicatorEntity.class)
                 .add(Restrictions.eq("name", name));
         return (IndicatorEntity) criteria.uniqueResult();
     }
 }
