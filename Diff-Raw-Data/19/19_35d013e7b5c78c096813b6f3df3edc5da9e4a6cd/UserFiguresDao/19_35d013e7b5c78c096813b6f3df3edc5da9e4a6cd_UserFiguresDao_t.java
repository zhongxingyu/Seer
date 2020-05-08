 package com.jinengo.reporting.service.user;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Repository;
 
 import com.jinengo.reporting.model.user.AggrUserFigures;
 
 
 @Repository
 public class UserFiguresDao {
 	
 	@Autowired
 	@Qualifier("sessionFactoryDW")
 	private SessionFactory sessionFactoryDw;
 	
 	/**
 	 * Monthly sum of a user specific keyfigure for a given year
 	 * 
 	 * @param userId
 	 * @param keyFigure
 	 * @param year
 	 * @return list - list of user figures
 	 */
 	public List<AggrUserFigures> getKeyFigureSum(int userId, String keyFigure, int year) {
 	
 		Session session = getSessionFactoryDw().openSession();
 		String keyColumn = StringEscapeUtils.escapeSql(keyFigure);
 		String hql = "select sum(" + keyColumn + "), year, month " +
 						"from AggrUserFigures " +
 						"where jinengoUserID = :userId " +
 						"and year = :year " +
 						"group by year, month";
 		
 		Query query = session.createQuery(hql);
 		query.setParameter("userId", userId);
 		query.setParameter("year", year);
		List<AggrUserFigures> res = query.list(); 
		session.close();
		return res;
 	}
 	
 	/**
 	 * Monthly keyfigure average per km for a given year
 	 * 
 	 * @param userId
 	 * @param keyFigure
 	 * @param year
 	 * @return list - list of user figures
 	 */
 	public List<AggrUserFigures> getKeyFigureAvg(int userId, String keyFigure, int year) {
 	
 		Session session = getSessionFactoryDw().openSession();
 		String keyColumn = StringEscapeUtils.escapeSql(keyFigure);
 		String hql = "select sum(" + keyColumn + ") / sum(sumDistance), year, month " +
 						"from AggrUserFigures " +
 						"where jinengoUserID = :userId " +
 						"and year = :year " +
 						"group by year, month";
 		
 		Query query = session.createQuery(hql);
 		query.setParameter("userId", userId);
 		query.setParameter("year", year);
 		
 		return query.list();
 	}
 	
 	/**
 	 * Get key figures for each transportation type
 	 * 
 	 * @param userId
 	 * @param keyFigure
 	 * @param year
 	 * @param month
 	 * @return list - list of key figures
 	 */
 	public List<AggrUserFigures> getTransportation(int userId, String keyFigure, int year, int month) {
 		
 		Session session = getSessionFactoryDw().openSession();
 		String keyColumn = StringEscapeUtils.escapeSql(keyFigure);
 		
 		String hql = "select " + keyColumn + ", year, month, transportationType " +
 						"from AggrUserFigures " +
 						"where year = :year " +
 						"and jinengoUserID = :userId";
 		
 		Query query = session.createQuery(hql);
 		query.setParameter("userId", userId);
 		query.setParameter("year", year);
 		
 		return query.list();
 	}	
 	
 	public SessionFactory getSessionFactoryDw() {
 		return sessionFactoryDw;
 	}
 
 	public void setSessionFactoryDw(SessionFactory sessionFactoryDw) {
 		this.sessionFactoryDw = sessionFactoryDw;
 	}
 }
