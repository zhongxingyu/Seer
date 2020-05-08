 package org.beanfuse.security.dao;
 
import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
import java.util.Map;
 
 import org.beanfuse.persist.hibernate.BaseDaoHibernate;
 import org.beanfuse.security.UserCategory;
 import org.beanfuse.security.online.CategoryProfile;
 import org.beanfuse.security.online.SessionProfile;
 
 public class MultiSessionProfileProvider extends BaseDaoHibernate implements SessionProfileProvider {
 
 	private Long sessionProfileId;
 
 	public List getProfiles() {
 		List profiles = entityDao.loadAll(CategoryProfile.class);
 		for (Iterator iterator = profiles.iterator(); iterator.hasNext();) {
 			CategoryProfile profile = (CategoryProfile) iterator.next();
 			profile.setCategory((UserCategory) entityDao.get(UserCategory.class, profile
 					.getCategory().getId()));
 		}
 		return profiles;
 	}
 
 	public SessionProfile getProfile() {
 		// read min(id) as needed
 		if (null == sessionProfileId) {
 			List profileIds = entityDao.searchHQLQuery("select min(id) from "
 					+ SessionProfile.class.getName());
 			if (profileIds.isEmpty()) {
 				return null;
 			} else {
 				sessionProfileId = (Long) profileIds.get(0);
 			}
 		}
 		SessionProfile profile = (SessionProfile) entityDao.get(SessionProfile.class,
 				sessionProfileId);
		List<CategoryProfile> categoryProfiles = entityDao.searchHQLQuery("from CategoryProfile");
 		// initialize profile.categoryprofiles
		Map newCategoryProfiles=new HashMap();
		for (Iterator iterator = categoryProfiles.iterator(); iterator.hasNext();) {
			CategoryProfile cp = (CategoryProfile) iterator.next();
			Long categoryId=cp.getCategory().getId();
			cp.setCategory((UserCategory) entityDao.get(UserCategory.class, categoryId));
			newCategoryProfiles.put(categoryId, cp);
 		}
		profile.setCategoryProfiles(newCategoryProfiles);
 		return profile;
 	}
 
 	public Long getSessionProfileId() {
 		return sessionProfileId;
 	}
 
 	public void setSessionProfileId(Long sessionProfileId) {
 		this.sessionProfileId = sessionProfileId;
 	}
 
 }
