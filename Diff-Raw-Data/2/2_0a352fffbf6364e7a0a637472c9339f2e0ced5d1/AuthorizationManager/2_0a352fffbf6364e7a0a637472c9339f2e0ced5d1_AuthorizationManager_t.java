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
 package br.octahedron.figgo.modules.authorization.manager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import br.octahedron.cotopaxi.eventbus.EventBus;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.figgo.modules.ApplicationDomainModuleSpec.ActionSpec;
 import br.octahedron.figgo.modules.DataAlreadyExistsException;
 import br.octahedron.figgo.modules.DataDoesNotExistsException;
 import br.octahedron.figgo.modules.DomainModuleSpec;
 import br.octahedron.figgo.modules.Module;
 import br.octahedron.figgo.modules.ModuleSpec.Type;
 import br.octahedron.figgo.modules.authorization.data.DomainUser;
 import br.octahedron.figgo.modules.authorization.data.DomainUserDAO;
 import br.octahedron.figgo.modules.authorization.data.Role;
 import br.octahedron.figgo.modules.authorization.data.RoleDAO;
 import br.octahedron.util.Log;
 
 /**
  * This entity is responsible to manage authorization issues, such as roles operations
  * (create/delete role, add/remove user, add/remove activity) and check user authorizations.
  * 
  * It's a global module working in the default namespace mostly, except for {@link DomainUserDAO}.
  * 
  * @author Danilo Queiroz
  * @author Vítor Avelino
  */
 public class AuthorizationManager {
 	
 	public static final String USERS_ROLE_NAME = "USERS";
 	public static final String ADMINS_ROLE_NAME = "ADMINS";
 
 	private static final Log logger = new Log(AuthorizationManager.class);
 
 	private RoleDAO roleDAO = new RoleDAO();
 	private DomainUserDAO domainUserDAO = new DomainUserDAO();
 
 	@Inject
 	private GoogleAuthorizer googleAuthorizer;
 	
 	@Inject
 	private EventBus eventBus;
 	
 	/**
 	 * @param googleAuthorizer
 	 *            the googleAuthorizer to set
 	 */
 	public void setGoogleAuthorizer(GoogleAuthorizer googleAuthorizer) {
 		this.googleAuthorizer = googleAuthorizer;
 	}
 	
 	/**
 	 * @param eventBus the eventBus to set
 	 */
 	public void setEventBus(EventBus eventBus) {
 		this.eventBus = eventBus;
 	}
 	
 	/**
 	 * Used by tests
 	 */
 	protected void setRoleDAO(RoleDAO roleDAO) {
 		this.roleDAO = roleDAO;
 	}
 
 	/**
 	 * Used by tests
 	 */
 	protected void setDomainUserDAO(DomainUserDAO domainUserDAO) {
 		this.domainUserDAO = domainUserDAO;
 	}
 
 	/**
 	 * Checks if the role exists or not on the specific domain.
 	 * 
 	 * @param domain
 	 *            domain that the role belongs to
 	 * @param roleName
 	 *            roleName of the role
 	 * 
 	 * @return <code>true</code> if exists the given role at the given domain, <code>false</code>
 	 *         otherwise.
 	 */
 	protected boolean existsRole(String roleName) {
 		return this.roleDAO.exists(roleName);
 	}
 
 	/**
 	 * Returns the role of a specific domain.
 	 * 
 	 * @param domain
 	 *            domain that the role belongs to
 	 * @param roleName
 	 *            roleName of the role
 	 * 
 	 * @return a role that belongs to the domain and matches the roleName
 	 * 
 	 * @throws DataDoesNotExistsException
 	 *             if theres no such role
 	 */
 	protected Role getRole(String roleName) {
 		if (this.existsRole(roleName)) {
 			return this.roleDAO.get(roleName);
 		} else {
 			throw new DataDoesNotExistsException("There's no role " + roleName);
 		}
 	}
 	
 	/**
 	 * Creates a new role for a given domain if it doesn't exist yet.
 	 * 
 	 * @param domain
 	 *            domain that the role will belong to
 	 * @param roleName
 	 *            roleName of the role
 	 * 
 	 * @throws DataAlreadyExistsException
 	 *             if the role already exists
 	 */
 	public void createRole(String roleName) {
 		if (!this.existsRole(roleName)) {
 			Role role = new Role(roleName);
 			this.roleDAO.save(role);
 		} else {
 			throw new DataAlreadyExistsException("Already exists role " + roleName);
 		}
 	}
 
 	/**
 	 * Creates a new role with some activities associated with for a given domain if it doesn't
 	 * exist yet.
 	 * 
 	 * @param roleName
 	 *            roleName to be added on domain
 	 * @param activities
 	 *            activities to be set on role
 	 * 
 	 * @throws DataAlreadyExistsException
 	 *             if the role already exists
 	 */
 	public void createRole(String roleName, Collection<String> activities) {
 		if (!this.existsRole(roleName)) {
 			Role role = new Role(roleName);
 			role.addActivities(activities);
 			this.roleDAO.save(role);
 		} else {
 			throw new DataAlreadyExistsException("Already exists role " + roleName);
 		}
 	}
 
 	/**
 	 * Removes a role if exists for a given domain.
 	 * 
 	 * @param roleName
 	 *            roleName of the role
 	 * 
 	 * @throws DataDoesNotExistsException
 	 *             if theres no role with the given name at the given domain.
 	 */
 	public void removeRole(String roleName) {
 		if (this.existsRole(roleName)) {
 			this.roleDAO.delete(roleName);
 		} else {
 			throw new DataDoesNotExistsException("There's no role " + roleName);
 		}
 	}
 
 	/**
 	 * Adds the given users to a specific role.
 	 * 
 	 * @param roleName
 	 *            roleName to have its users updated
 	 * @param users
 	 *            users to be added on role
 	 */
 	public void addUsersToRole(String roleName, String... users) {
 		Role role = this.getRole(roleName);
 		role.addUsers(users);
 	}
 
 	/**
 	 * Adds the given activities to a specific role of a specific domain.
 	 * 
 	 * @param roleName
 	 *            roleName to have its activities updated
 	 * @param activities
 	 *            activities to be added on role
 	 */
 	public void addActivitiesToRole(String roleName, String... activities) {
 		Role role = this.getRole(roleName);
 		role.addActivities(activities);
 	}
 
 	/**
 	 * Removes the given activities to a specific role of a specific domain.
 	 * 
 	 * @param roleName
 	 *            roleName to have its activities updated
 	 * @param activities
 	 *            activities to be removed of role
 	 */
 	public void removeActivitiesToRole(String roleName, String... activities) {
 		Role role = this.getRole(roleName);
 		role.removeActivities(activities);
 	}
 
 	/**
 	 * Removes a user from a specific role belonging to a specific domain.
 	 * 
 	 * @param roleName
 	 *            roleName to retrieve role
 	 * @param user
 	 *            user to be removed from role
 	 * @param domain 
 	 */
 	public void removeUserFromRole(String roleName, String user, String domain) {
 		Role role = this.getRole(roleName);
 		role.removeUser(user);
 		
 		// background process to check if still has permission at the given domain
 		this.eventBus.publish(new UserRemovedFromRoleEvent(user, domain));
 	}
 
 	/**
 	 * Removes user from roles if associated with, of a specific domain.
 	 * 
 	 * @param userId
 	 *            username to be removed from role
 	 * @param userId 
 	 */
 	public void removeUserFromRoles(String userId, String domain) {
 		for (Role role : this.getUserRoles(userId)) {
 			role.removeUser(userId);
 		}
 	
 		// background process to check if still has permission at the given domain
 		this.eventBus.publish(new UserRemovedFromRoleEvent(userId, domain));
 	}
 
 	/**
 	 * Returns all roles of a specific domain.
 	 * 
 	 * @return a collection of {@link Role}
 	 */
 	public Collection<Role> getRoles() {
 		return this.roleDAO.getAll();
 	}
 
 	/**
 	 * Returns all roles of a user of a specific domain.
 	 * 
 	 * @param username
 	 *            username to retrieve the roles
 	 * 
 	 * @return a collection of {@link Role} with all roles belonging to user
 	 */
 	public Collection<Role> getUserRoles(String username) {
 		return this.roleDAO.getUserRoles(username);
 	}
 
 	/**
 	 * @param users
 	 * @return
 	 */
 	public Map<String, Collection<Role>> getUsersRoles(String[] users) {
 		Map<String, Collection<Role>> result = new HashMap<String, Collection<Role>>();
 		for (String user : users) {
 			result.put(user, this.getUserRoles(user));
 		}
 		return result;
 	}
 
 	/**
 	 * Checks if username belongs to a specific role of a specific domain.
 	 * 
 	 * @return <code>true</code> if the given user is authorized to perform the given activity,
 	 *         <code>false</code> otherwise.
 	 */
 	public boolean isAuthorized(String username, String activityName) {
 		return this.googleAuthorizer.isApplicationAdmin() || this.roleDAO.existsRoleFor(username, activityName);
 	}
 
 	/**
 	 * Returns all activities available on the system by asking all the modules its actions.
 	 * 
 	 * @return a collection of {@link String} representing the activities available on the system
 	 */
 	public Collection<String> getActivities() {
 		// FIXME maybe cache?
 		// TODO why not APLICATION_DOMAIN activities too?
 		List<String> activities = new LinkedList<String>();
 		for (Module module : Module.values()) {
 			if (module.getModuleSpec().getModuleType() == Type.DOMAIN) {
 				for (ActionSpec action : ((DomainModuleSpec) module.getModuleSpec()).getModuleActions()) {
 					activities.add(action.getAction());
 				}
 			}
 		}
 		return activities;
 	}
 
 	/**
 	 * Returns all domains that a user belongs at least to a one role.
 	 * 
 	 * @return a collection of {@link String} representing all the domains that a user has at least
 	 *         one role.
 	 */
 	public Collection<String> getActiveUserDomains(String username) {
 		Collection<DomainUser> domains = this.domainUserDAO.getDomains(username);
 		logger.debug("User %s has %d domain", username, domains.size());
 		Collection<String> domainNames = new ArrayList<String>(domains.size());
 		for (DomainUser d : domains) {
 			if(d.isActive()) {
 				domainNames.add(d.getDomain());
 			}
 		}
 		return domainNames;
 	}
 
 	/**
 	 * TODO comment
 	 * 
 	 * @param userId
 	 * @param isActive
 	 * @param isActive
 	 */
 	public void createDomainUser(String domain, String userId, boolean isActive) {
 		DomainUser domainUser = new DomainUser(userId, domain, isActive);
 		this.domainUserDAO.save(domainUser);
 	}
 
 	/**
 	 * TODO comment
 	 * 
 	 * @return
 	 */
 	public Collection<DomainUser> getActiveUsers(String domain) {
 		return this.domainUserDAO.getActiveUsers(domain);
 	}
 
 	/**
 	 * TODO comment
 	 * 
 	 * @return
 	 */
 	public Collection<DomainUser> getNonActiveUsers(String domain) {
 		return this.domainUserDAO.getNonActiveUsers(domain);
 	}
 
 	/**
 	 * TODO comment
 	 * 
 	 * @param domain
 	 * @param userId
 	 */
 	public void activateDomainUser(String domain, String userId) {
		DomainUser domainUser = this.domainUserDAO.get(userId, domain);
 		domainUser.markAsActive();
 		this.eventBus.publish(new UserActivatedEvent(userId, domain));
 	}
 
 	/**
 	 * TODO comment
 	 * 
 	 * @param userId
 	 */
 	public void removeDomainUser(String domain, String userId) {
 		DomainUser domainUser = this.domainUserDAO.get(userId, domain);
 		this.domainUserDAO.delete(domainUser);
 	}
 
 }
