 /**
  *   Copyright(c) 2010-2011 CodWar Soft
  * 
  *   This file is part of IPDB UrT.
  *
  *   IPDB UrT is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This software is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 package iddb.web.security.service.local;
 
 import iddb.core.model.Player;
 import iddb.core.model.Server;
 import iddb.core.model.User;
 import iddb.core.model.UserServer;
 import iddb.core.model.dao.DAOFactory;
 import iddb.core.model.dao.PlayerDAO;
 import iddb.core.model.dao.ServerDAO;
 import iddb.core.model.dao.UserDAO;
 import iddb.core.model.dao.UserServerDAO;
 import iddb.core.util.PasswordUtils;
 import iddb.exception.EntityDoesNotExistsException;
 import iddb.web.security.dao.Session;
 import iddb.web.security.dao.SessionDAO;
 import iddb.web.security.exceptions.InvalidAccountException;
 import iddb.web.security.exceptions.InvalidCredentialsException;
 import iddb.web.security.exceptions.UserLockedException;
 import iddb.web.security.service.CommonUserService;
 import iddb.web.security.subject.Subject;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DbUserServiceImpl extends CommonUserService {
 
 	private UserDAO userDAO = (UserDAO) DAOFactory.forClass(UserDAO.class);
 	private UserServerDAO userServerDAO = (UserServerDAO) DAOFactory.forClass(UserServerDAO.class);
 	private ServerDAO serverDAO = (ServerDAO) DAOFactory.forClass(ServerDAO.class);
 	private PlayerDAO playerDAO = (PlayerDAO) DAOFactory.forClass(PlayerDAO.class);
 	
 	private SessionDAO sessionDAO;
 	
 	private static Logger log = LoggerFactory.getLogger(DbUserServiceImpl.class);
 	
 	public DbUserServiceImpl() {
 		log.debug("Initialize DbUserServiceImpl");
 		try {
 			sessionDAO = new SessionDAO();
 		} catch (Exception e) {
 			log.error(e.getMessage());
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see iddb.web.security.service.UserService#authenticate(javax.servlet.http.HttpServletRequest, java.lang.String, java.lang.String)
 	 */
 	@Override
 	protected Subject doAuthenticate(HttpServletRequest request, String username,
 			String password) throws InvalidAccountException,
 			InvalidCredentialsException, UserLockedException {
 
 		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
 			throw new InvalidCredentialsException();
 		}
 		
 		User user;
 		try {
 			user = userDAO.get(username);
 		} catch (EntityDoesNotExistsException e) {
 			log.trace("User {} do not exists", username);
 			throw new InvalidAccountException();
 		}
 		
 		if (!PasswordUtils.checkPassword(password, user.getPassword())) {
 			log.trace("User {}. Invalid password.", username);
 			throw new InvalidCredentialsException();
 		}
 
 		Subject subject = buildSubject(user);
 
 		return subject;
 	}
 
 	private Subject buildSubject(User user) {
 		Subject subject = new Subject();
 		subject.setKey(user.getKey());
 		subject.setLoginId(user.getLoginId());
 		subject.setPassword(user.getPassword());
 		subject.setRoles(user.getRoles());
 		return subject;
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.web.security.service.UserService#hasPermission(java.lang.Long)
 	 */
 	@Override
 	public boolean hasPermission(Long server) {
 		Subject subject = this.getCurrentUser();
 		if (!subject.isAuthenticated()) return false;
 		if (subject.isSuperAdmin()) return true;
 		
 		try {
 			userServerDAO.findByUserAndServer(subject.getKey(), server);
 		} catch (EntityDoesNotExistsException e) {
 			log.trace("UserServer {} do not exists for user {}", server.toString(), subject.getLoginId());
 			return false;
 		}
 		
 		return true;
 	}
 
 	@Override
 	public List<Server> listUserServers(Integer level) {
 		Subject subject = this.getCurrentUser();
 		if (!subject.isAuthenticated()) return Collections.emptyList();
 		List<Server> servers = null;
 		if (subject.isSuperAdmin()) {
 			int[] count = new int[1];
 			servers = serverDAO.findEnabled(0, 1000, count);
 		} else {
 			List<UserServer> us = userServerDAO.listUserServers(subject.getKey(), level);
 			servers = new ArrayList<Server>();
 			for (UserServer u : us) {
 				try {
 					servers.add(serverDAO.get(u.getServer()));
 				} catch (EntityDoesNotExistsException e) {
 					log.error(e.getMessage());
 				}
 			}
 		}
 		Collections.sort(servers, new Comparator<Server>() {
 			@Override
 			public int compare(Server o1, Server o2) {
 				return o1.getName().compareTo(o2.getName());
 			}
 		});
 		return servers;
 	}
 	
 	/* (non-Javadoc)
 	 * @see iddb.web.security.service.UserService#hasPersmission(java.lang.Long, java.lang.Integer)
 	 */
 	@Override
 	public boolean hasPermission(Long server, Integer level) {
 		Subject subject = this.getCurrentUser();
 		if (!subject.isAuthenticated()) return false;
 		if (subject.isSuperAdmin()) return true;
 		
 		Player player = getSubjectPlayer(server);
 		
 		if (player != null && player.getLevel() != null && player.getLevel() >= level) {
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.web.security.service.CommonUserService#doLogout(javax.servlet.http.HttpServletRequest)
 	 */
 	@Override
 	protected void doLogout(HttpServletRequest request) {
 		// do nothing
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.web.security.service.UserService#hasAnyServer(java.lang.Integer)
 	 */
 	@Override
 	public boolean hasAnyServer(Integer level) {
 		Subject subject = this.getCurrentUser();
 		if (!subject.isAuthenticated()) return false;
 		if (subject.isSuperAdmin()) return true;
 		return userServerDAO.existsAny(subject.getKey(), level);
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.web.security.service.CommonUserService#findSession(java.lang.String, java.lang.Long, java.lang.String)
 	 */
 	@Override
 	protected Session findSession(String key, Long userId, String ip) {
 		Session s = null;
 		try {
 			s = sessionDAO.get(key, userId, ip);
 		} catch (EntityDoesNotExistsException e) {
 			log.debug(e.getMessage());
 		}
 		return s;
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.web.security.service.CommonUserService#createSession(java.lang.String, java.lang.Long, java.lang.String)
 	 */
 	@Override
 	protected void createSession(String key, Long userId, String ip) {
 		Session session = new Session();
 		session.setKey(key);
 		session.setUserId(userId);
 		session.setIp(ip);
 		sessionDAO.insert(session);
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.web.security.service.CommonUserService#removeSession(java.lang.String)
 	 */
 	@Override
 	protected void removeSession(String key) {
 		sessionDAO.delete(key);
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.web.security.service.CommonUserService#findUser(java.lang.Long)
 	 */
 	@Override
 	protected Subject findUser(Long key) {
 		User u;
 		try {
 			u = userDAO.get(key);
 		} catch (EntityDoesNotExistsException e) {
 			log.error(e.getMessage());
 			return null;
 		}
 		return buildSubject(u);
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.web.security.service.UserService#getSubjectPlayer(java.lang.Long)
 	 */
 	@Override
 	public Player getSubjectPlayer(Long server) {
 		Subject subject = this.getCurrentUser();
 		if (!subject.isAuthenticated()) return null;
 		
 		Player player;
 		player = subject.getServerPlayer().get(server.toString());
 		
 		if (player == null) {
 			player = getUserPlayer(subject, server);
 			if (player != null) {
 				subject.getServerPlayer().put(server.toString(), player);
 			}
 		}
 		return player;
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.web.security.service.UserService#getUser(java.lang.Long)
 	 */
 	@Override
 	public User getUser(Long id) throws EntityDoesNotExistsException {
 		return userDAO.get(id);
 	}
 
 	/* (non-Javadoc)
 	 * @see iddb.web.security.service.UserService#getUserPlayer(iddb.core.model.User, java.lang.Long)
 	 */
 	@Override
 	public Player getUserPlayer(User user, Long server) {
 		UserServer userServer;
 		Player player;
 		try {
 			userServer = userServerDAO.findByUserAndServer(user.getKey(), server);
 		} catch (EntityDoesNotExistsException e) {
 			log.trace("UserServer {} do not exists for user {}", server.toString(), user.getLoginId());
 			return null;
 		}
 		if (userServer.getPlayer() == null || userServer.getPlayer().equals(0)) {
 			log.trace("No associated player for userid {}", user.getLoginId());
 			return null;
 		}
 		
 		try {
 			player = playerDAO.get(userServer.getPlayer());
 		} catch (EntityDoesNotExistsException e) {
 			log.trace("Player {} do not exists", userServer.getPlayer());
 			return null;
 		}
 		return player;
 	}
 
 }
