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
 
    private static final String[] DELETE_INDICATOR_QUERIES = {
            "delete from dashboard_app.indicator_value where indicator_id = :indicatorId",
            "delete from dashboard_app.report where indicator_id = :indicatorId",
            "delete from dashboard_app.indicator where indicator_id = :indicatorId"};

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

    @Override
    public void remove(IndicatorEntity indicatorEntity) {
        for (String query: DELETE_INDICATOR_QUERIES) {
            getCurrentSession().createSQLQuery(query)
                    .setParameter("indicatorId", indicatorEntity.getId())
                    .executeUpdate();
        }
    }

 }
