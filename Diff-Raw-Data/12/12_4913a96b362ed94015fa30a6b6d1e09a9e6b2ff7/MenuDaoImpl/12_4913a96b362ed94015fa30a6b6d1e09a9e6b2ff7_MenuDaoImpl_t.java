 package com.res.dao.hibernate.impl;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.springframework.stereotype.Repository;
 
 import com.res.dao.hibernate.MenuDao;
 import com.res.model.Menu;
 
 @Repository("MenuDao")
 public class MenuDaoImpl extends BaseDaoImpl implements MenuDao {
 
 	private static Logger logger = Logger.getLogger(MenuDaoImpl.class);
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Menu> menuByRestaurantId(long restaurantId){
 		StringBuffer sb = new StringBuffer();
 		sb.append("SELECT m.foodCategory FROM Menu m WHERE m.restaurantId = :restaurantId");
		
 		Query query = currentSession().createQuery(sb.toString());
 		query.setLong("restaurantId",restaurantId);
		
 		logger.debug("MenuList size = " + query.list().size());
 		logger.debug("MenuList = " + query.list());
 		return query.list();
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Menu> getMenuByFoodCategory(long restaurantId, String foodCategoryName){
 		StringBuffer sb = new StringBuffer();
 		sb.append("FROM Menu m ");
		sb.append("WHERE m.restaurantId = :restaurantId " +
 				"AND m.foodCategory.foodCategoryName = :foodCategoryName");
		
 		Query query = currentSession().createQuery(sb.toString());
		query.setParameter("restaurantId", restaurantId);
		query.setParameter("foodCategoryName", foodCategoryName);
		
 		logger.debug("FoodList size = " + query.list().size());
 		return query.list();
 	}
 	
 	
 }
