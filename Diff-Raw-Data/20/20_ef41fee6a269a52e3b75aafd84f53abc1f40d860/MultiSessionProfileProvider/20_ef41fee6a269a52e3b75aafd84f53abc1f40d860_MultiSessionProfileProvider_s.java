 package org.beanfuse.security.dao;
 
 import java.util.Iterator;
 import java.util.List;
 
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
		entityDao.initialize(profile.getCategoryProfiles());
 		// initialize profile.categoryprofiles
		for (Iterator iter = profile.getCategoryProfiles().keySet().iterator(); iter.hasNext();) {
			Long categoryId = (Long) iter.next();
			CategoryProfile categoryProfile = (CategoryProfile) profile.getCategoryProfiles().get(
					categoryId);
			entityDao.initialize(categoryProfile);
 		}
 		return profile;
 	}
 
 	public Long getSessionProfileId() {
 		return sessionProfileId;
 	}
 
 	public void setSessionProfileId(Long sessionProfileId) {
 		this.sessionProfileId = sessionProfileId;
 	}
 
 }
