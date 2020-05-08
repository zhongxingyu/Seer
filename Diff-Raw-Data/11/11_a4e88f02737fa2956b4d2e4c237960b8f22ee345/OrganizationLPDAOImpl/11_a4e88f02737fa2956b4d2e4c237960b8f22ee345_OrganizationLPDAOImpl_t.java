 package com.pearson.ed.lplc.dao.impl;
 
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Restrictions;
 
 import com.pearson.ed.lplc.dao.api.OrganizationLPDAO;
 import com.pearson.ed.lplc.dao.impl.common.LPLCBaseDAOImpl;
 import com.pearson.ed.lplc.model.OrganizationLPMapping;
 
 public class OrganizationLPDAOImpl extends LPLCBaseDAOImpl implements
 OrganizationLPDAO {
 	/**
 	 * List License Pool based on organization ID provided.
 	 * 
 	 * @param organizationId
 	 *            organizationId.
 	 * @return List of LicensePoolMapping.
 	 */
 	@SuppressWarnings("unchecked")
 	public List<OrganizationLPMapping> listOrganizationMappingByOrganizationId(
 			String organizationId, int level) {
 		Criteria criteria = getSession().createCriteria(
 				OrganizationLPMapping.class);
		Criterion eqOrganizationId = Restrictions.eq("organization_id", organizationId);
 		Criterion eqRootOrganization = Restrictions.like("organization_level", 0);
 		if (level == 0)
 			criteria.add(eqRootOrganization);
 		return criteria.add(eqOrganizationId).list();
 	}
 	
 
 }
