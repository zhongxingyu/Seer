 package com.btc.test.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.btc.test.entity.User;
 
 @Repository
 public class UserDao {
 
 	@Autowired
 	public SessionFactory sessionFactory;
 
 	public SessionFactory getSessionFactory() {
 		return sessionFactory;
 	}
 
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	public List<User> selectUser(String userID) {
 		Session session = this.getSessionFactory().openSession();
 		session.beginTransaction();
		List<Object[]> rs = session.createSQLQuery("select * from test.user where id="+userID).list();
		List<User> userList = new ArrayList<User>();
		if(rs.size() >0) {
			for(Object[] obj: rs) {
				String idString = obj[0].toString();
				String nameString = obj[1].toString();
				Integer age = (Integer) obj[2];
				String genderString = obj[3].toString();
				User user = new User();
				user.setAge(age);
				user.setGender(genderString);
				user.setId(idString);
				user.setName(nameString);
				userList.add(user);
				System.out.println(user);
			}
			
		}
 		session.getTransaction().commit();
 		session.close();
		return userList;
 	}
 	
 	
 	public List<User> selectUser() {
 		Session session = this.getSessionFactory().openSession();
 		session.beginTransaction();
 		List<User> rs = session.createQuery("from User").list();
 		session.getTransaction().commit();
 		session.close();
 		return rs;
 	}
 	/*public List<User> selectUser() {
 		Session session = this.getSessionFactory().openSession();
 		session.beginTransaction();
 		List<Object[]> rs = session.createSQLQuery("select * from test.user").list();
 		List<User> userList = new ArrayList<User>();
 		if(rs.size() >0) {
 			for(Object[] obj: rs) {
 				String idString = obj[0].toString();
 				String nameString = obj[1].toString();
 				Integer age = (Integer) obj[2];
 				String genderString = obj[3].toString();
 				User user = new User();
 				user.setAge(age);
 				user.setGender(genderString);
 				user.setId(idString);
 				user.setName(nameString);
 				userList.add(user);
 				System.out.println(user);
 			}
 			
 		}
 		session.getTransaction().commit();
 		session.close();
 		return userList;
 	}*/
 }
