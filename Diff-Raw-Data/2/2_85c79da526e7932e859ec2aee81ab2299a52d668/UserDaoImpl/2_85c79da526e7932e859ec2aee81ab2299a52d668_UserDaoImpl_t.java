 package org.mdissjava.mdisscore.model.dao.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.mdissjava.mdisscore.model.dao.UserDao;
 import org.mdissjava.mdisscore.model.dao.hibernate.HibernateUtil;
 import org.mdissjava.mdisscore.model.pojo.Address;
 import org.mdissjava.mdisscore.model.pojo.Configuration;
 import org.mdissjava.mdisscore.model.pojo.User;
 
 
 public class UserDaoImpl implements UserDao {
 	
 	public UserDaoImpl() {
 		HibernateUtil.openSessionFactory();
 	}	
	
 	@Override
 	public boolean emailAllReadyExists(String email)
 	{
 		Session session = HibernateUtil.getSession();
 
 		int num = (Integer) session.createQuery("from User where email = '"+email+"'").list().size();
 		if(num>0)
 			return true;
 		else
 			return false;
 	}
 	
 	@Override
 	public boolean nickAllReadyExists(String nick)
 	{
 		Session session = HibernateUtil.getSession();
 
 		int num = (Integer) session.createQuery("from User where nick = '"+nick+"'").list().size();
 		if(num>0)
 			return true;
 		else
 			return false;
 	}
 	
 	@Override
 	public void addUser(User user) {
 		if(user != null){
 			Session session = HibernateUtil.getSession();
 			Transaction tx = session.beginTransaction();
 			session.save(user);
 			tx.commit();
 		}
 	}
 
 	@Override
 	public void deleteUser(User user) {
 		if (user != null) {
 			Session session = HibernateUtil.getSession();
 			Transaction tx = session.beginTransaction();
 			session.delete(user);
 			tx.commit();			
 		}
 	}
 		
 	@Override
 	public User getUserById( int id ) {	  
 
 		User user = null;
 		Session session = HibernateUtil.getSession();
 
 		Query q = session.createQuery("" + "from User as user "
 				+ "where user.id =" + id);
 		user = (User) q.uniqueResult();
 
 		
 		return user;
 	
 	}
 	
 	
 	@Override
 	public void loggedIn(ObjectId id){
 	   Date now = new Date();
 	   
 	
 	}
 		
 
 	@Override
 	public void updateUser(User user) {
 		
 		if (user != null) {
 			Session session = HibernateUtil.getSession();
 			Transaction tx = session.beginTransaction();
 			session.update(user);
 			tx.commit();
 		}		
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<User> findFriends(User user) {
 		List<User> users = new ArrayList<User>();
 		Session session = HibernateUtil.getSession();
 
 		Query q = session.createQuery("" + "from friends as users "
 				+ "where friends.userId =" + user.getId());
 		users =  q.list();
 		
 		return users;
 	}
 
 	@Override
 	public void addFriend(int userid, int friendid) {
 
 		
 	}
 
 	@Override
 	public void deleteFriend(int userid, int friendid) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public User getUserByName(String username) {
 		
 		User user = null;
 		Session session = HibernateUtil.getSession();
 		
 		Query q = session.createQuery("" + "from User as user where user.nick =" + "'" + username + "'");
 		user = (User) q.uniqueResult();
 		
 		return user;
 	}
 
 
 
 }
