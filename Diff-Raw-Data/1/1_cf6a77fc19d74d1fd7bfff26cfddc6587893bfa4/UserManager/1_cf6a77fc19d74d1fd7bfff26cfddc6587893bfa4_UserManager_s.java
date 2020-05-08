 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.user.manager;
 
 import java.util.Collection;
import java.util.Collections;
 
 import br.octahedron.figgo.modules.user.data.User;
 import br.octahedron.figgo.modules.user.data.UserDAO;
 
 /**
  * @author Erick Moreno
  */
 public class UserManager {
 
 	private UserDAO userDAO = new UserDAO();
 
 	/**
 	 * @param userDAO
 	 *            the userDAO to set
 	 */
 	protected void setUserDAO(UserDAO userDAO) {
 		this.userDAO = userDAO;
 	}
 
 	/**
 	 * Creates a {@link User} with passed parameters
 	 */
 	public User createUser(String userId, String name, String phoneNumber, String description) {
 		User user = new User(userId, name, phoneNumber, description);
 		this.userDAO.save(user);
 		return user;
 	}
 
 	/**
 	 * Updates {@link User} properties with passed parameters
 	 */
 	public User updateUser(String userId, String name, String phoneNumber, String description) {
 		User user = this.userDAO.get(userId);
 		user.setName(name);
 		user.setPhoneNumber(phoneNumber);
 		user.setDescription(description);
 		// This object will be updated to the DB by JDO persistence manager
 		return user;
 	}
 
 	/**
 	 * Gets the {@link User} with the given id
 	 * 
 	 * @return the {@link User} with the given id, if exists, or <code>null</code>, if doesn't
 	 *         exists a user with the given id.
 	 */
 	public User getUser(String userId) {
 		return this.userDAO.get(userId);
 	}
 
 	/**
 	 * Checks if exists a {@link User} with the given id.
 	 * 
 	 * @return <code>true</code> if exists, <code>false</code> otherwise.
 	 */
 	public boolean existsUser(String userId) {
 		return this.userDAO.exists(userId);
 	}
 
 	/**
 	 * Updates a {@link User} avatar key with the generated blob key.
 	 * 
 	 * @param userId
 	 * @param avatarKey
 	 *            Blob key generated when uploaded avatar
 	 */
 	public void updateAvatarKey(String userId, String avatarKey) {
 		User user = this.userDAO.get(userId);
 		user.setAvatarKey(avatarKey);
 	}
 
 	/**
 	 * Retrieves a collection of {@link User} that its name or email starts with a term.
 	 * 
 	 * @param term
 	 * @return
 	 */
 	public Collection<User> getUsersStartingWith(String term) {
 		return this.userDAO.getUsersStartingWith(term);
 	}
 	
 	/**
 	 * Retrivies a collection of {@link User} which has its names in <code>usersId</code>.
 	 * 
 	 * @param term
 	 * @return
 	 */
 	public Collection<User> getUsersIn(String[] usersId) {
 		return this.userDAO.getUsersIn(usersId);
 	}
 }
