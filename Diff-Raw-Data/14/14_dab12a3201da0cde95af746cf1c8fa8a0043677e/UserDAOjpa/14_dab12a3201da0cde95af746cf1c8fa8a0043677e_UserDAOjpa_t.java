 package ch.boxi.togetherLess.dataAccess.user.dao;
 
 import java.util.Date;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import ch.boxi.togetherLess.dataAccess.jpaHelper.AbstractHibernateDAO;
 import ch.boxi.togetherLess.dataAccess.user.dao.exception.ActivationCodeOutOfDateException;
 import ch.boxi.togetherLess.dataAccess.user.dao.exception.UserAllreadyActivatedException;
 import ch.boxi.togetherLess.dataAccess.user.dao.exception.UserDoesNotExistException;
 import ch.boxi.togetherLess.dataAccess.user.dto.ActivationCode;
 import ch.boxi.togetherLess.dataAccess.user.dto.CookieLogin;
 import ch.boxi.togetherLess.dataAccess.user.dto.Login;
 import ch.boxi.togetherLess.dataAccess.user.dto.User;
 import ch.boxi.togetherLess.dataAccess.user.dto.UserLogin;
 import ch.boxi.togetherLess.dataAccess.user.dto.UserState;
 
 public class UserDAOjpa extends AbstractHibernateDAO implements UserDAO {
 
 	@Override
 	public User getUser(Integer id) {
 		return null;
 	}
 
 	@Override
 	public User register(String userName, String password, String firstName,
 			String lastName, String email, int targetWeight, Date targetDate) {
 		Session session = takeTransaction();
 		UserLogin login = new UserLogin(userName, password, null);
 		Set<Login> logins = new TreeSet<>();
 		logins.add(login);
 		
 		ActivationCode activationCode = new ActivationCode(null, 10);
 		
 		User user = new User(
 				null, // ID will be Set by JPA
 				firstName, 
 				lastName, 
 				email, 
 				targetWeight, 
 				UserState.registered, 
 				new Date(), 
 				targetDate, 
 				logins,
 				activationCode);
 		
 		login.setUser(user);
 		activationCode.setUser(user);
 		
 		session.save(user);
 		session.save(login);
 		session.save(activationCode);
 		session.getTransaction().commit();
 		return user;
 	}
 	
 	@Override
 	public User getUser(String userName) {
 		Session session = takeTransaction();
 		Query query = session.createQuery("from UserLogin where username = '" + userName + "'");
 		UserLogin userLogin = (UserLogin)query.uniqueResult();
 		session.close();
 		if(userLogin == null){
 			throw new UserDoesNotExistException();
 		}
 		return userLogin.getUser();
 	}
 
 	@Override
 	public User login(String sessionID) {
 		Session session = takeTransaction();
 		Query query = session.createQuery("from CookieLogin where sessionID = '" + sessionID + "'");
 		CookieLogin cookieLogin = (CookieLogin) query.uniqueResult();
 		session.close();
 		if(cookieLogin == null){
 			throw new UserDoesNotExistException();
 		}
 		return cookieLogin.getUser();
 	}
 
 	@Override
 	public void clearCach() {
 		// NOOP: We have a DB no Memory cache
 	}
 
 	@Override
 	public void addCookieLogin(User user, CookieLogin cookieLogin) {
 		Session session = takeTransaction();
 		cookieLogin.setUser(user);
 		user.getLogins().add(cookieLogin);
 		session.save(cookieLogin);
		session.getTransaction().commit();
 	}
 
 	@Override
 	public boolean isUserNameFree(String userName) {
 		Session session = takeTransaction();
 		Query query = session.createQuery("from UserLogin where username = '" + userName + "'");
 		UserLogin userLogin = (UserLogin)query.uniqueResult();
 		session.close();
 		return userLogin == null;
 	}
 
 	@Override
 	public void activateUser(String activationCode) {
 		Session session = takeTransaction();
 		Query query = session.createQuery("from ActivationCode where code = '" + activationCode + "'");
 		ActivationCode code = (ActivationCode)query.uniqueResult();
 		if(code != null){
 			if(code.getValidUntil().before(new Date())){
 				throw new ActivationCodeOutOfDateException();
 			} else if(code.getUser().getState() == UserState.active){
 				throw new UserAllreadyActivatedException();
 			} else{
 				User user = code.getUser();
 				user.setState(UserState.active);
				session.merge(user);
				session.getTransaction().commit();
 			}
 		} else{
 			throw new UserDoesNotExistException();
 		}
 	}
 
 	@Override
 	public void addActivationCode(User user, ActivationCode activationCode) {
 		Session session = takeTransaction();
 		user.setActivatinCode(activationCode);
 		activationCode.setUser(user);
		session.merge(user);
		session.merge(activationCode);
		session.getTransaction().commit();
 	}
 
 }
