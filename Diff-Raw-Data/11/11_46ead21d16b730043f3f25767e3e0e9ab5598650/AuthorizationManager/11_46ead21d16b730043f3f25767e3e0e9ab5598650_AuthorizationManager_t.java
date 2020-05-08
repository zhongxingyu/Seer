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
 
 import static br.octahedron.figgo.modules.authorization.data.Role.createRoleKey;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TreeSet;
 
 import br.octahedron.figgo.modules.DataAlreadyExistsException;
 import br.octahedron.figgo.modules.DataDoesNotExistsException;
 import br.octahedron.figgo.modules.DomainModuleSpec;
 import br.octahedron.figgo.modules.Module;
 import br.octahedron.figgo.modules.ApplicationDomainModuleSpec.ActionSpec;
 import br.octahedron.figgo.modules.ModuleSpec.Type;
 import br.octahedron.figgo.modules.authorization.data.Role;
 import br.octahedron.figgo.modules.authorization.data.RoleDAO;
import br.octahedron.figgo.modules.user.data.User;
 import br.octahedron.util.Log;
 
 /**
  * This entity is responsible to manage authorization issues, such as roles operations
  * (create/delete role, add/remove user, add/remove activity) and check user authorizations.
  * 
  * It's a global module, working in the default namespace.
  * 
  * @author Danilo Queiroz
  * @author Vítor Avelino
  */
 public class AuthorizationManager {
 	
 	private static final Log logger = new Log(AuthorizationManager.class);
 
 	private GoogleAuthorizer googleAuthorizer = new GoogleAuthorizer();
 	private RoleDAO roleDAO = new RoleDAO();
 
 	/**
 	 * Used to inject a mock object on tests.
 	 * 
 	 * @param roleDAO
 	 *            the roleDAO to set
 	 */
 	protected void setRoleDAO(RoleDAO roleDAO) {
 		this.roleDAO = roleDAO;
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
 	public void createRole(String domain, String roleName) {
 		if (!this.existsRole(domain, roleName)) {
 			Role role = new Role(domain, roleName);
 			this.roleDAO.save(role);
 		} else {
 			throw new DataAlreadyExistsException("Already exists role " + roleName + " at domain " + domain);
 		}
 	}
 
 	/**
 	 * Creates a new role with some activities associated with for a given domain if it doesn't
 	 * exist yet.
 	 * 
 	 * @param domain
 	 *            domain that the role will belong to
 	 * @param roleName
 	 *            roleName to be added on domain
 	 * @param activities
 	 *            activities to be set on role
 	 * 
 	 * @throws DataAlreadyExistsException
 	 *             if the role already exists
 	 */
 	public void createRole(String domain, String roleName, Collection<String> activities) {
 		if (!this.existsRole(domain, roleName)) {
 			Role role = new Role(domain, roleName);
 			role.addActivities(activities);
 			this.roleDAO.save(role);
 		} else {
 			throw new DataAlreadyExistsException("Already exists role " + roleName + " at domain " + domain);
 		}
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
 	public boolean existsRole(String domain, String roleName) {
 		return this.roleDAO.exists(createRoleKey(domain, roleName));
 	}
 
 	/**
 	 * Removes a role if exists for a given domain.
 	 * 
 	 * @param domain
 	 *            domain that the role belongs to
 	 * @param roleName
 	 *            roleName of the role
 	 * 
 	 * @throws DataDoesNotExistsException
 	 *             if theres no role with the given name at the given domain.
 	 */
 	public void removeRole(String domain, String roleName) {
 		if (this.existsRole(domain, roleName)) {
 			this.roleDAO.delete(createRoleKey(domain, roleName));
 		} else {
 			throw new DataDoesNotExistsException("There's no role " + roleName + " at domain " + domain);
 		}
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
 	public Role getRole(String domain, String roleName) {
 		if (this.existsRole(domain, roleName)) {
 			return this.roleDAO.get(createRoleKey(domain, roleName));
 		} else {
 			throw new DataDoesNotExistsException("There's no role " + roleName + " at domain " + domain);
 		}
 	}
 
 	/**
 	 * Adds the given users to a specific role.
 	 * 
 	 * @param domain
 	 *            domain that the role belongs to
 	 * @param roleName
 	 *            roleName to have its users updated
 	 * @param users
 	 *            users to be added on role
 	 */
 	public void addUsersToRole(String domain, String roleName, Collection<String> users) {
 		Role role = this.getRole(domain, roleName);
 		role.addUsers(users);
 	}
 
 	/**
 	 * Adds the given users to a specific role.
 	 * 
 	 * @param domain
 	 *            domain that the role belongs to
 	 * @param roleName
 	 *            roleName to have its users updated
 	 * @param users
 	 *            users to be added on role
 	 */
 	public void addUsersToRole(String domain, String roleName, String... users) {
 		Role role = this.getRole(domain, roleName);
 		role.addUsers(users);
 	}
 
 	/**
 	 * Adds the given activities to a specific role of a specific domain.
 	 * 
 	 * @param domain
 	 *            domain that the role belongs to
 	 * @param roleName
 	 *            roleName to have its activities updated
 	 * @param activities
 	 *            activities to be added on role
 	 */
 	public void addActivitiesToRole(String domain, String roleName, String... activities) {
 		Role role = this.getRole(domain, roleName);
 		role.addActivities(activities);
 	}
 
 	/**
 	 * Adds the given activities to a specific role of a specific domain.
 	 * 
 	 * @param domain
 	 *            domain that the role belongs to
 	 * @param roleName
 	 *            roleName to have its activities updated
 	 * @param activities
 	 *            activities to be added on role
 	 */
 	public void addActivitiesToRole(String domain, String roleName, Collection<String> activities) {
 		Role role = this.getRole(domain, roleName);
 		role.addActivities(activities);
 	}
 
 	/**
 	 * Removes a user from a specific role belonging to a specific domain.
 	 * 
 	 * @param domain
 	 *            domain that the role belongs to
 	 * @param roleName
 	 *            roleName to retrieve role
 	 * @param user
 	 *            user to be removed from role
 	 */
 	public void removeUserFromRole(String domain, String roleName, String user) {
 		Role role = this.getRole(domain, roleName);
 		role.removeUser(user);
 	}
 
 	/**
 	 * Returns all domains that a user belongs at least to a one role.
 	 * 
 	 * @return a collection of {@link String} representing all the domains that a user has at least
 	 *         one role.
 	 */
 	public Collection<String> getUserDomains(String username) {
 		Collection<String> domains = Collections.emptySet();  
 		Collection<Role> roles = this.roleDAO.getUserRoles(username);
 
 		if (!roles.isEmpty()) {
 			domains = new TreeSet<String>();
 			for (Role role : roles) {
 				domains.add(role.getDomain());
 			}
 		}
 		logger.debug("User %s has %d domain", username, domains.size());
 		return domains;
 	}
 
 	/**
 	 * Returns all roles of a user of a specific domain.
 	 * 
 	 * @param username
 	 *            username to retrieve the roles
 	 * 
 	 * @return a collection of {@link Role} with all roles belonging to user
 	 */
 	public Collection<Role> getUserRoles(String domain, String username) {
 		return this.roleDAO.getUserRoles(domain, username);
 	}
 
 	/**
 	 * Returns all roles of a specific domain.
 	 * 
 	 * @return a collection of {@link Role}
 	 */
 	public Collection<Role> getRoles(String domain) {
 		return this.roleDAO.getAll(domain);
 	}
 
 	/**
 	 * Checks if username belongs to a specific role of a specific domain.
 	 * 
 	 * @return <code>true</code> if the given user is authorized to perform the given activity at
 	 *         the given domain, <code>false</code> otherwise.
 	 */
 	public boolean isAuthorized(String domainName, String username, String activityName) {
 		return this.googleAuthorizer.isApplicationAdmin() || this.roleDAO.existsRoleFor(domainName, username, activityName);
 	}
 
 	/**
 	 * Removes user from roles if associated with, of a specific domain.
 	 * 
 	 * @param domain
 	 *            domain that the roles belongs to
 	 * @param username
 	 *            username to be removed from role
 	 */
 	public void removeUserFromRoles(String domain, String username) {
 		for (Role role : this.getUserRoles(domain, username)) {
 			role.removeUser(username);
 		}
 	}
 
 	/**
 	 * Returns all activities available on the system by asking all the modules its actions.
 	 * 
 	 * @return a collection of {@link String} representing the activities available on the system
 	 */
 	public Collection<String> getAcitivities() {
 		// FIXME maybe cache?
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
 	 * Updates the activities of a specific role of a specific domain.
 	 * 
 	 * @param domain
 	 *            domain that the role belongs to
 	 * @param roleName
 	 *            roleName to have its activities updated
 	 * @param activities
 	 *            activities to be set on role
 	 */
 	public void updateRoleActivities(String domain, String roleName, Collection<String> activities) {
 		Role role = this.getRole(domain, roleName);
 		role.updateActivities(activities);
		this.roleDAO.save(role);
 	}
 
 }
