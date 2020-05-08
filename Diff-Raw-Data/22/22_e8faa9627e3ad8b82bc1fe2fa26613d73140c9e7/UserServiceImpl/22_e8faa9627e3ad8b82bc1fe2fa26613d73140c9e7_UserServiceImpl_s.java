 /**
  * 
  */
 package com.example.server.service;
 
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpSessionBindingEvent;
 import javax.servlet.http.HttpSessionBindingListener;
 
 import org.apache.log4j.Logger;
 
 import com.example.client.exception.LoginFailureException;
 import com.example.client.model.User;
 import com.example.client.service.UserService;
 import com.example.server.dao.UserDao;
 import com.example.server.security.BCrypt;
 
 /**
  * Implementation of {@link UserService}
  */
 public class UserServiceImpl extends AbstractService implements UserService, HttpSessionBindingListener {
 	
 	private UserDao userDao;
 	
 	private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class);
 	
 
 	/* (non-Javadoc)
 	 * @see com.example.client.service.LoginService#login(com.example.client.model.User)
 	 */
 	@Override
 	public String login(User user) throws LoginFailureException {
 		
 		try {
 			if(user == null || user.isValid() == false) {
 				LOGGER.error("Bogus user data was received "+user);
 				throw new LoginFailureException();
 			}
 			
 			final User found = userDao.findByUserName(user.getUsername());
 			if(found == null) {
 				throw new LoginFailureException("Sorry, we couldn't locate you in our records.");
 			}
 			
 			// validate the password
 			if(BCrypt.checkpw(user.getPassword(), found.getPassword())) {
 				final String id = httpSession.getId();
				// nullify the password
				found.setPassword(null);
				sessionMap.put(id, found);
 				return id;
 			} else {
 				throw new LoginFailureException();
 			}
 		} catch(Exception e) {
 			if(e instanceof LoginFailureException) {
 				// re-throw
 				throw (LoginFailureException)e;
 			}
 			LOGGER.error(e);
 			throw new LoginFailureException();
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see com.example.client.service.UserService#logout(java.lang.String)
 	 */
 	@Override
 	public void logout(String sessionId) {
 		if(httpSession.getId().equals(sessionId)) {
 			sessionMap.remove(sessionId);
 			httpSession.invalidate();
 		} else {
 			LOGGER.error("Session Ids are not equal: "+sessionId + " != httpsession id: "+httpSession.getId());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.example.client.service.UserService#isLoggedIn(java.lang.String)
 	 */
 	@Override
 	public User isLoggedIn(String sessionId) throws LoginFailureException {
 		if(sessionMap.containsKey(sessionId)) {
 			return sessionMap.get(sessionId);
 		} else {
 			throw new LoginFailureException();
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see com.example.server.service.AbstractService#setHttpSession(javax.servlet.http.HttpSession)
 	 */
 	@Override
 	public void setHttpSession(HttpSession session) {
 		super.setHttpSession(session);
 		httpSession.setAttribute("SessionBindingListener", this);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.http.HttpSessionBindingEvent)
 	 */
 	@Override
 	public void valueBound(HttpSessionBindingEvent evt) {
 		// no-op
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
 	 */
 	@Override
 	public void valueUnbound(HttpSessionBindingEvent evt) {
 		sessionMap.remove(evt.getSession().getId());
 	}	
 	
 	/**
 	 * Inject the {@link UserDao} this service depends on
 	 * @param dao
 	 */
 	public void setUserDao(UserDao dao) {
 		this.userDao = dao;
 	}
 }
