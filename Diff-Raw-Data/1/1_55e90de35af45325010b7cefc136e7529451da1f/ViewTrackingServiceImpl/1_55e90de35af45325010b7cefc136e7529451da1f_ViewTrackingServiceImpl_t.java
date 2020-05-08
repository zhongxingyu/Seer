 package com.railinc.jook.service.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 import com.railinc.jook.domain.DomainObject;
 import com.railinc.jook.domain.LastUserView;
 import com.railinc.jook.service.ViewTrackingService;
 
 public class ViewTrackingServiceImpl extends BaseServiceImpl<LastUserView> implements ViewTrackingService {
 
 	@Override
 
 	public void userJustSaw(String user, String application, String resource) {
 		if (user == null) {
 			return;
 		}
 		if (getHibernateTemplate().bulkUpdate("UPDATE LastUserView SET lastViewed=? WHERE app=? AND name=? and user = ?", new Date(), application, resource, user) == 0) {
 			super.saveOrUpdate(new LastUserView(user,application,resource));
 		}
 	}
 	@Override
 	public boolean hasUserSeen(String user, String application, String resource) {
 		if ( null == user ) {
 			return false;
 		}
 		return super.count(createCriteria().add(Restrictions.eq("name", resource)).add(Restrictions.eq("app", application))) > 0;
 	}
 
 	@Override
 	public void resetViewState(String application, String resource) {
 		getHibernateTemplate().bulkUpdate("DELETE FROM LastUserView WHERE app=? AND name=?", application,resource);
 	}
 
 	@Override
 	protected Class<? extends DomainObject> domainClass() {
 		return LastUserView.class;
 	}
 	@Override
 	public boolean userHasNotSeenAll(String user, String viewtrackingAppname,
 			List<? extends Object> itemIds) {
 		if (user == null) {
 			return true;
 		}
 
 		List<String> ids = new ArrayList<String>(itemIds.size());
 		for (Object o : itemIds) {
 			ids.add(String.valueOf(o));
 		}
 		return super.count(
 				createCriteria().add(
 						Restrictions.in("name", ids))
						.add(Restrictions.eq("user", user))
 						.add(Restrictions.eq("app", viewtrackingAppname)
 						)) < ids.size();
 	}
 	@Override
 	public void resetViewState(String application, List<? extends Object> itemIds) {
 		for (Object o : itemIds) {
 			resetViewState(application, String.valueOf(o));
 		}
 	}
 	@Override
 	public void userJustSawItems(String user, String viewtrackingAppname,
 			List<? extends Object> itemIds) {
 		for (Object o : itemIds) {
 			userJustSaw(user, viewtrackingAppname, String.valueOf(o));
 		}
 	}
 	@Override
 	public List<String> whatHasUserSeen(String user,
 			String viewtrackingAppname) {
 		Date now = new Date();
 		DetachedCriteria c = DetachedCriteria.forClass(this.domainClass());
 		c.add(Restrictions.eq("app", viewtrackingAppname));
 		c.add(Restrictions.eq("user", user));
 		c.setProjection(Projections.property("name"));
 		return list(String.class, c);		
 	}
 
 }
