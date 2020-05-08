 package com.pearson.ed.lplc.dao.impl;
 
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import com.pearson.ed.lplc.dao.api.LicensePoolDAO;
 import com.pearson.ed.lplc.dao.impl.common.LPLCBaseDAOImpl;
 import com.pearson.ed.lplc.model.LicensePoolMapping;
 
 /**
  * LicensePoolMappingDAOImpl is the implementation for user mapping data access
  * objects in the LPLC.
  * 
  * @author RUMBA
  */
 
 public class LicensePoolDAOImpl extends LPLCBaseDAOImpl implements
 		LicensePoolDAO {
 	/**
 	 * Creates a license pool mapping.
 	 * 
 	 * @param lplcMapping
 	 *            the license pool mapping.
 	 */
 	public void createLicensePool(LicensePoolMapping lplcMapping) {
 		setCreateAndModifiedValues(lplcMapping);
 		getHibernateTemplate().save(lplcMapping);
 	}
 
 	/**
 	 * Update license pool.
 	 * 
 	 * @param lplcMapping
 	 *            lplcMapping.
 	 */
 	public void update(LicensePoolMapping lplcMapping) {
 		setModifiedValues(lplcMapping);
 		getHibernateTemplate().saveOrUpdate(lplcMapping);
 	}
 
 	/**
 	 * Find license pool by name.
 	 * 
 	 * @param lplcName
 	 * @return LicensePool mapping.
 	 */
 	public LicensePoolMapping findByLicensePoolId(String lplcId) {
 		Criteria criteria = getSession().createCriteria(
 				LicensePoolMapping.class);
 		Criterion eqLicensePoolId = Restrictions.eq("licensepoolId", lplcId);
 		criteria.add(eqLicensePoolId);
 		return (LicensePoolMapping) criteria.uniqueResult();
 	}
 	/**
      * Get LicensePool for Subscription.
      * @param organizationId organizationId.
      * @param productId productId.
      * @return list of LicensePoolMapping.
      */
 	@SuppressWarnings("unchecked")
 	public List<LicensePoolMapping> findOrganizationMappingToSubscribe(String organizationId,
 			String productId){
 		Criteria criteria = getSession().createCriteria(
 				LicensePoolMapping.class);
 		criteria.createAlias("organizations", "organizations");
		Criterion eqOrganizationId = Restrictions.eq("organizations.organization_id", organizationId);
		Criterion eqProductId = Restrictions.eq("product_id", productId);
 		Criterion eqStartDate = Restrictions.le("start_date", new Date());
 		Criterion eqEndDate = Restrictions.ge("end_date", new Date());
 		Criterion eqDenyNewSubscription = Restrictions.eq("denyManualSubscription",0);
 		Criterion eqDenyNewSubscriptionOrgLevel = Restrictions.eq("organizations.denyManualSubscription",0);
 		criteria.add(eqStartDate).add(eqEndDate).add(eqProductId).add(eqDenyNewSubscription).add(eqDenyNewSubscriptionOrgLevel).add(eqOrganizationId);
 		criteria.addOrder(Order.asc("organizations.organization_level"));
 		criteria.addOrder(Order.desc("end_date"));
 		return criteria.list();
 	}
 
 }
