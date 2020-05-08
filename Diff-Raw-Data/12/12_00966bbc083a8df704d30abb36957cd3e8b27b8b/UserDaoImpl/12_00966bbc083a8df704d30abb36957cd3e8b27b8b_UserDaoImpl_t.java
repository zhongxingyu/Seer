 package org.mdissjava.mdisscore.model.dao.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Projections;
 import org.mdissjava.mdisscore.model.dao.UserDao;
 import org.mdissjava.mdisscore.model.dao.hibernate.HibernateUtil;
 import org.mdissjava.mdisscore.model.pojo.User;
 
 
 public class UserDaoImpl implements UserDao {
 
 	private Session session;	
 	
 	public UserDaoImpl() {
 		
 	}	
 	
 	@Override
 	public void addUser(User user) {
 		if(user != null){
 			session = HibernateUtil.getSession();
 			Transaction tx = session.beginTransaction();
 			session.save(user);
 			tx.commit();
 		}
 	}
 
 
 	@Override
 	public void updateUser(User user) {		
 		if (user != null) {
 			session = HibernateUtil.getSession();
 			Transaction tx = session.beginTransaction();
 			session.update(user);
 			tx.commit();
 		}		
 	}
 	
 	@Override
 	public void deleteUser(User user) {
 		if (user != null) {
 			session = HibernateUtil.getSession();
 			Transaction tx = session.beginTransaction();
 			session.delete(user);
 			tx.commit();
 		}
 	}
 	
 	@Override
 	public boolean emailAlreadyExists(String email) {
 		session = HibernateUtil.getSession();
 		int num = (Integer) session.createQuery("from User where email = '"+email+"'").list().size();
 		if(num>0)
 			return true;
 		else
 			return false;
 	}
 	
 	@Override
 	public boolean nickAlreadyExists(String nick) {
 		session = HibernateUtil.getSession();
 
 		int num = (Integer) session.createQuery("from User where nick = '"+nick+"'").list().size();
 		if(num>0)
 			return true;
 		else
 			return false;
 	}
 	
 		
 	@Override
 	public User getUserById( int id ) {	  
 		User user = null;
 		session = HibernateUtil.getSession();
 		Query q = session.createQuery("" + "from User as user "
 				+ "where user.id =" + id);
 		user = (User) q.uniqueResult();	
 		return user;	
 	}
 	
 	@Override
 	public User getUserByNick(String nick) {		
 		User user = null;
 		session = HibernateUtil.getSession();
 
 		Query q = session.createQuery("" + "from User as user where user.nick =" + "'" + nick + "'");
 		user = (User) q.uniqueResult();
 		return user;
 	}
 	
 	
 	@Override
 	public void loggedIn(ObjectId id){
 	   Date now = new Date();
 	  	
 	}
 		
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<User> findFollows(String userId, int pageNumber, int maxResults) {
 		List<User> users = new ArrayList<User>();
 		Session session = HibernateUtil.getSession();
 
 		Query q = session.createQuery("Select follows from User as user "
 				+ " where user.nick = '" + userId + "'");
 		q = q.setFirstResult(maxResults * (pageNumber - 1));		
 	    q.setMaxResults(maxResults);						
 		users =  (List<User>) q.list();
 		return users;
 	}
 
 	@Override
 	public void addFollow(String userNickname,  User follow) {
 		session = HibernateUtil.getSession();
 		User user = getUserByNick(userNickname);						
 		user.addFollow(follow);		
 		follow.addFollower(user);		
 		
 		updateUser(user);
 		updateUser(follow);
 	}
 
 	@Override
 	public void deleteFollow(String userNickname, User follow) {
 		session = HibernateUtil.getSession();
 		User user = getUserByNick(userNickname);						
 		user.removeFollow(follow);		
		follow.removeFollower(user);				
		session.update(user);
		session.update(follow);
 		
 	}
 
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<User> findFollowers(String userId, int pageNumber, int maxResults) {
 		List<User> users = new ArrayList<User>();
 		Session session = HibernateUtil.getSession();
 
 		Query q = session.createQuery("Select followers from User as user "
 				+ " where user.nick = '" + userId + "'");
 		q = q.setFirstResult(maxResults * (pageNumber - 1));		
 	    q.setMaxResults(maxResults);
 		users =  (List<User>) q.list();
 		return users;
 	}
 
 	@Override
 	public void addFollower(String userNickname, User follower) {
 		session = HibernateUtil.getSession();
 		User user = getUserByNick(userNickname);				
 		user.addFollower(follower);				
 		session.save(user);		
 	}
 
 	@Override
 	public void deleteFollower(String userNickname, User follower) {
 		session = HibernateUtil.getSession();
 		User user = getUserByNick(userNickname);				
		user.removeFollow(follower);		
		session.update(user);						
 	}
 
 	@Override
 	public void activateUser(int userid) {
 		User usuario=this.getUserById(userid);
 		if(usuario!=null)
 		{
 			usuario.setActive(true);
 			this.updateUser(usuario);
 		}
 	}
 	
 	@Override
 	public void deactivateUser(int userid) {
 		User usuario=this.getUserById(userid);
 		if(usuario!=null)
 		{
 			usuario.setActive(false);
 			this.updateUser(usuario);
 		}		
 	}	
 	
 	@Override
 	public boolean followsUser(String userNickname, User follower){
 		session = HibernateUtil.getSession();
 		User user = getUserByNick(userNickname);
 		if (user.getFollows().contains(follower)){
 			return true;
 		}
 		else{
 			return false;
 		}
 		
 	}
 	
 	@Override
 	public User getUserByEmail( String email ) {	  
 		User user = null;
 		session = HibernateUtil.getSession();
 		Query q = session.createQuery("" + "from User as user "
 				+ "where user.email ='" + email + "'");
 		user = (User) q.uniqueResult();	
 		return user;	
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<User> getUserByRole(String role) {
 		List<User> users = new ArrayList<User>();
 		
 		Session session = HibernateUtil.getSession();
 		Query q = session.createQuery("" + "from User as user "
 				+ " where user.role = '" + role + "'");
 		
 		users = (List<User>) q.list();
 		
 		return users;
 	}
 
 	@Override
 	public int getTotalUsers() {
 		session = HibernateUtil.getSession();
 		Criteria criteria = session.createCriteria(User.class);	 
 		criteria.setProjection(Projections.rowCount()); 
 		return ((Long)criteria.list().get(0)).intValue();
 	}
 
 	@Override
 	public String getAvatar(String userId) {
 		session = HibernateUtil.getSession();
 		Query q = session.createQuery("Select avatar from User as user "
 				+ " where user.nick = '" + userId + "'");
 		return q.uniqueResult().toString();
 		
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<User> findAllUsers(int pageNumber, int maxResults) {
 		session = HibernateUtil.getSession();
 		Query q = session.createQuery("from User"); 
 		q = q.setFirstResult(maxResults * (pageNumber - 1));		
 	    q.setMaxResults(maxResults);
 		return (List<User>)  q.list();
 	}
 
 
 
 }
