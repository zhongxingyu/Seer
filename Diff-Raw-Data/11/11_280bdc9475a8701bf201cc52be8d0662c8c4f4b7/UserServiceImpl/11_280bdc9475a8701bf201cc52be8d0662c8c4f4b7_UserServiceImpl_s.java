 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.server.services;
 
 import org.accesointeligente.client.services.UserService;
 import org.accesointeligente.model.User;
 import org.accesointeligente.server.*;
 import org.accesointeligente.shared.*;
 
 import net.sf.gilead.core.PersistentBeanManager;
 import net.sf.gilead.gwt.PersistentRemoteService;
 
 import org.hibernate.Criteria;
 import org.hibernate.FetchMode;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 
 import java.util.*;
 
 public class UserServiceImpl extends PersistentRemoteService implements UserService {
 	private static final long serialVersionUID = -389660005156048126L;
 	private PersistentBeanManager persistentBeanManager;
 
 	public UserServiceImpl() {
 		persistentBeanManager = HibernateUtil.getPersistentBeanManager();
 		setBeanManager(persistentBeanManager);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void login(String email, String password) throws LoginException, ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 		List<User> result = new ArrayList<User>();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(User.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.add(Restrictions.eq("email", email));
 			criteria.setFetchMode("activities", FetchMode.JOIN);
 			result = (List<User>) criteria.list();
 			hibernate.getTransaction().commit();
 		} catch (Throwable ex) {
 			if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 
 			}
 
 			throw new ServiceException();
 		}
 
 		if (result.size() != 1) {
 			throw new LoginException();
 		} else {
 			User user = result.get(0);
 
 			if (!BCrypt.checkpw(password, user.getPassword())) {
 				throw new LoginException();
 			} else {
 				try {
 					hibernate = HibernateUtil.getSession();
 					hibernate.beginTransaction();
 					user.setLastLoginDate(new Date());
 					hibernate.update(user);
 					hibernate.getTransaction().commit();
 					SessionUtil.setAttribute(getThreadLocalRequest().getSession(), "sessionId", UUID.randomUUID().toString());
 					SessionUtil.setAttribute(getThreadLocalRequest().getSession(), "user", (User) persistentBeanManager.clone(user));
 				} catch (Throwable ex) {
 					if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 						hibernate.getTransaction().rollback();
 					}
 
 					throw new ServiceException();
 				}
 			}
 		}
 	}
 
 	@Override
 	public User register(User user) throws RegisterException, ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(User.class);
 			criteria.add(Restrictions.eq("email", user.getEmail()));
 
 			if (criteria.list().size() == 1) {
 				throw new RegisterException();
 			}
 
 			user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
 
 			hibernate.save(user);
 			hibernate.getTransaction().commit();
 			return (User) persistentBeanManager.clone(user);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 
 			if (ex instanceof RegisterException) {
 				throw (RegisterException) ex;
 			} else {
 				throw new ServiceException();
 			}
 		}
 	}
 
 	@Override
 	public Boolean checkPass(String email, String password) throws LoginException, ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 		User user = null;
 
 		try {
 			Criteria criteria = hibernate.createCriteria(User.class);
 			criteria.add(Restrictions.eq("email", email));
 			user = (User) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 
 		if (user == null) {
 			throw new LoginException();
 		} else {
 			if (!BCrypt.checkpw(password, user.getPassword())) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 	}
 
 	@Override
 	public void updateUser(User user) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			user = (User) persistentBeanManager.merge(user);
 
 			Criteria criteria = hibernate.createCriteria(User.class);
 			criteria.add(Restrictions.eq("id", user.getId()));
 			User actualUser = (User) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 
 			if (user.getPassword() != null && !actualUser.getPassword().equals(user.getPassword())) {
 				user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
 			}
 
 			hibernate = HibernateUtil.getSession();
 			hibernate.beginTransaction();
 			hibernate.update(user);
 			hibernate.getTransaction().commit();
 			SessionUtil.setAttribute(getThreadLocalRequest().getSession(), "user", (User) persistentBeanManager.clone(user));
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Boolean checkEmailExistence(String email) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 		User user = null;
 
 		try {
 			Criteria criteria = hibernate.createCriteria(User.class);
 			criteria.add(Restrictions.eq("email", email));
 			user = (User) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 
 		if (user == null) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	public void resetPassword(String email) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 		String newPassword = null;
 
 		try {
 			Criteria criteria = hibernate.createCriteria(User.class);
 			criteria.add(Restrictions.eq("email", email));
 			User user = (User) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 
 			newPassword = RandomPassword.getRandomString(8);
 			user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
 
 			hibernate = HibernateUtil.getSession();
 			hibernate.beginTransaction();
 			hibernate.update(user);
 			hibernate.getTransaction().commit();
 
 			Emailer emailer = new Emailer();
 			emailer.setRecipient(user.getEmail());
 			emailer.setSubject(ApplicationProperties.getProperty("email.password.recovery.subject"));
			emailer.setBody(String.format(ApplicationProperties.getProperty("email.password.recovery.body"), (user.getGender().equals(Gender.FEMALE)) ? "Sra. " : "Sr. ", user.getFirstName(), newPassword) + ApplicationProperties.getProperty("email.signature"));
 			emailer.connectAndSend();
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 }
