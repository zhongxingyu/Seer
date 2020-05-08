 package org.motechproject.carereporting.dao.impl;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.motechproject.carereporting.dao.IndicatorValueDao;
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.motechproject.carereporting.domain.IndicatorValueEntity;
 import org.springframework.stereotype.Component;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 @Component
 public class IndicatorValueDaoHibernateImpl extends GenericDaoHibernateImpl<IndicatorValueEntity>
         implements IndicatorValueDao {
     
     private static final String DATE = "date";
 
     @Override
     @SuppressWarnings("unchecked")
     public List<IndicatorValueEntity> getIndicatorValuesForArea(Integer indicatorId, Integer areaId, Integer frequencyId,
                                                                 Date startDate, Date endDate, String category) {
         Criteria criteria = getCurrentSession()
                 .createCriteria(IndicatorValueEntity.class)
                 .add(Restrictions.eq("indicator.id", indicatorId))
                 .add(Restrictions.eq("area.id", areaId))
                 .add(Restrictions.eq("frequency.id", frequencyId))
                 .add(Restrictions.ge(DATE, startDate))
                 .add(Restrictions.lt(DATE, endDate))
                 .addOrder(Order.asc(DATE));
         if (category != null) {
             criteria.add(Restrictions.eq("category", category));
         }
         return new ArrayList<>(new LinkedHashSet<IndicatorValueEntity>(criteria.list()));
     }
 
     @Override
     public void removeByIndicator(IndicatorEntity indicatorEntity) {
         getSessionFactory().getCurrentSession()
                 .createQuery("delete from " + getType().getName() + " where indicator_id = " + indicatorEntity.getId())
                 .executeUpdate();
     }
 
 }
