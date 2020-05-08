 package com.lo54project.webservice.dao;
 
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.FetchMode;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import com.lo54project.webservice.hibernate.util.HibernateUtil;
 import com.lo54project.webservice.model.Course;
 import com.lo54project.webservice.model.CourseSession;
 
 public enum CourseSessionDao implements DaoInterface {
 	instance;
 
 	private Map<Integer, CourseSession> contentProvider = new HashMap<Integer, CourseSession>();
 
 	@SuppressWarnings("unchecked")
 	private CourseSessionDao() {
 		SessionFactory sf = HibernateUtil.getSessionFactory();
 		Session session = sf.openSession();
 
 		List<CourseSession> coursesessions = new ArrayList<CourseSession>();
 		coursesessions = session.createCriteria(CourseSession.class)
 				.setFetchMode("crs", FetchMode.JOIN)
 				.setFetchMode("loc", FetchMode.JOIN)
 				.list();
 
 		for (CourseSession cs : coursesessions) {
 			contentProvider.put(cs.getId(), cs);
 		}
 
 		session.close();
 	}
 
 	public CourseSession getCourseSession(Integer id_course_session)
 			throws SQLException, ParseException {
 		CourseSession cd = new CourseSession();
 		SessionFactory sf = HibernateUtil.getSessionFactory();
 		Session session = sf.openSession();
 
 		cd = (CourseSession) session.get(CourseSession.class, id_course_session);
 
 		session.close();
 
 		return cd;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Map<Integer, CourseSession> getCourseSessionsWithLimit(String lMin, String lMax) 
 			throws SQLException, ParseException {
 		contentProvider.clear();
 		SessionFactory sf = HibernateUtil.getSessionFactory();
         Session session = sf.openSession();
 
         List<CourseSession> coursesessions = new ArrayList<CourseSession>();
         coursesessions = session.createCriteria(CourseSession.class)
         				.setFetchMode("crs", FetchMode.JOIN)
         				.setFetchMode("loc", FetchMode.JOIN)
         				.addOrder(Order.asc("crs"))
         				.setFirstResult(Integer.parseInt(lMin))
         				.setMaxResults(Integer.parseInt(lMax))
         				.list();
 
         for (CourseSession cs : coursesessions) {
         	contentProvider.put(cs.getId(), cs);
 		}
         
         session.close();
 
 		return contentProvider;
 	}
 	
 
 	@SuppressWarnings("unchecked")
 	public Map<Integer, CourseSession> getCourseSessionByCourseCode(
 			String course_code) throws SQLException, ParseException {
 		Map<Integer, CourseSession> sessions = new HashMap<Integer, CourseSession>();		
 		SessionFactory sf = HibernateUtil.getSessionFactory();
         Session session = sf.openSession();
 
         List<CourseSession> coursesessions = new ArrayList<CourseSession>();
         coursesessions = session.createCriteria(CourseSession.class)
         		.add(Restrictions.eq("course_code", course_code))
         		.setFetchMode("crs", FetchMode.JOIN)
         		.setFetchMode("loc", FetchMode.JOIN)
         		.list();
         
         for (CourseSession cs : coursesessions) {
        	contentProvider.put(cs.getId(), cs);
 		}
         
         session.close();
 
 		return sessions;
 	}
 
 	public Map<Integer, CourseSession> getModel() {
 		return contentProvider;
 	}
 
 	@Override
 	public <T> void create(T o) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public <T> void remove(T o) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public <T> void update(T o) {
 		// TODO Auto-generated method stub
 		
 	}
 }
