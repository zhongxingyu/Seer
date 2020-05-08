 package com.credera.trails.dao.impl;
 
 import java.util.List;
 
 import org.hibernate.FetchMode;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.CriteriaSpecification;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.sql.JoinType;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.credera.trails.dao.RouteDao;
 import com.credera.trails.model.Region;
 import com.credera.trails.model.Route;
 
 @Transactional
 @Repository("routeDao")
 public class RouteDaoImpl implements RouteDao {
 
 	@Autowired
 	private SessionFactory sessionFactory;
 	
 	public void saveRoute(Route route) {
 		sessionFactory.getCurrentSession().saveOrUpdate(route);
 	}
 
 	public void deleteRoute(Route route) {
 		sessionFactory.getCurrentSession().delete(route);
 	}
 
 	public Route getRoute(Long id) {
 		return (Route) sessionFactory.getCurrentSession().get(Route.class, id);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Route> getAllRoutes() {
 		return (List<Route>) sessionFactory.getCurrentSession().createCriteria(Route.class).list();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Route> getAllRoutesByRegion(Region region) {
 		return (List<Route>) sessionFactory.getCurrentSession()
 				.createCriteria(Route.class)
 				.createAlias("region", "region")
 				//.createAlias("comments", "comments", JoinType.LEFT_OUTER_JOIN)
 				//.createAlias("directions", "directions", JoinType.LEFT_OUTER_JOIN)
				//.createAlias("ratings", "ratings", JoinType.LEFT_OUTER_JOIN)
 				//.setFetchMode("comments", FetchMode.SELECT)
 				//.setFetchMode("directions", FetchMode.SELECT)
				//.setFetchMode("ratings", FetchMode.JOIN)
 				
 				.add(Restrictions.eq("region.id", region.getId()))
 				.list();
 	}
 
 	@SuppressWarnings("unchecked")
 	public Route getRouteByUrlFriendlyName(String urlFriendlyName) {
 		return (Route) sessionFactory.getCurrentSession()
 				.createCriteria(Route.class)
 				.createAlias("comments", "comments", JoinType.LEFT_OUTER_JOIN)
 				.createAlias("directions", "directions", JoinType.LEFT_OUTER_JOIN)
				//.createAlias("ratings", "ratings", JoinType.LEFT_OUTER_JOIN)
 				.setFetchMode("comments", FetchMode.SELECT)
 				.setFetchMode("directions", FetchMode.SELECT)
				//.setFetchMode("ratings", FetchMode.SELECT)
 				.add(Restrictions.eq("urlFriendlyName", urlFriendlyName))
 				.uniqueResult();
 
 		//return results.size() > 0 ? results.get(0) : null;
 	}
 
 }
