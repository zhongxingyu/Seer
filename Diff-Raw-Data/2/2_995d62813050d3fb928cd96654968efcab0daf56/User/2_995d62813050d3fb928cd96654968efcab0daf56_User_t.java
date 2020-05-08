 // Copyright 2008 Thiago H. de Paula Figueiredo
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package br.com.arsmachina.authentication.entity;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.hibernate.validator.Email;
 import org.hibernate.validator.Length;
 import org.hibernate.validator.NotNull;
 
 /**
  * Class that represents an application user. Each user can belong to any number of
  * {@link Permission}s. When a given user belongs to a {@link Permission}, but cannot be granted
  * some {@link Permission} in that group, this permission must be added to the list of removed
  * permissions (<code>removedPermissions</code> property).
  * 
  * @author Thiago H. de Paula Figueiredo
  */
 @Entity
 @Table(name = "`user`")
 final public class User implements Comparable<User>, Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Minimum e-mail length.
 	 */
 	public static final int MINIMUM_EMAIL_LENGTH = 3;
 
 	/**
 	 * Minimum e-mail length.
 	 */
 	public static final int MAXIMUM_EMAIL_LENGTH = 50;
 
 	/**
 	 * Minimum login length.
 	 */
 	public static final int MINIMUM_LOGIN_LENGTH = 2;
 
 	/**
 	 * Maximum e-mail length.
 	 */
 	public static final int MAXIMUM_LOGIN_LENGTH = 50;
 
 	/**
 	 * Minimum name length.
 	 */
 	public static final int MINIMUM_NAME_LENGTH = 2;
 
 	/**
 	 * Maximum name length.
 	 */
 	public static final int MAXIMUM_NAME_LENGTH = 50;
 
 	/**
 	 * Minimum name length.
 	 */
 	public static final int MINIMUM_PASSWORD_LENGTH = 6;
 
 	/**
 	 * Maximum name length.
 	 */
 	public static final int MAXIMUM_PASSWORD_LENGTH = 40;
 
 	private Integer id;
 
 	private String login;
 
 	private String name;
 
 	private String email;
 
 	private boolean credentialsExpired = false;
 
 	private boolean enabled = true;
 
 	private boolean expired = false;
 
 	private boolean locked = false;
 
 	private boolean loggedIn = false;
 
 	private String password;
 
 	private List<PermissionGroup> permissionGroups = new ArrayList<PermissionGroup>();
 
 	private List<Permission> removedPermissions = new ArrayList<Permission>();
 
 	private List<Role> roles = new ArrayList<Role>();
 
 	/**
 	 * Adds a permission group to this user.
 	 * 
 	 * @param permissionGroup a {@link PermissionGroup}.
 	 */
 	public void add(PermissionGroup permissionGroup) {
 
 		if (permissionGroups.contains(permissionGroup) == false) {
 			permissionGroups.add(permissionGroup);
 		}
 
 	}
 
 	/**
 	 * Adds a role to this user.
 	 * 
 	 * @param role a {@link Role}.
 	 */
 	public void add(Role role) {
 
		if (hasRole(role.getClass()) == false) {
 			role.setUser(this);
 			roles.add(role);
 		}
 
 	}
 
 	/**
 	 * Tells if this user has some a given role type.
 	 * 
 	 * @param <R> a {@link Role} subclass.
 	 * @param roleClass a {@link Class}.
 	 * @return a <code>boolean</code>.
 	 */
 	public <R extends Role> boolean hasRole(Class<R> roleClass) {
 		return getRole(roleClass) != null;
 	}
 
 	/**
 	 * Adds a removed permission to this user.
 	 * 
 	 * @param permission a {@link PermissionGroup}.
 	 */
 	public void addRemovedPermission(Permission permission) {
 
 		if (removedPermissions.contains(permission) == false) {
 			removedPermissions.add(permission);
 		}
 
 	}
 
 	public int compareTo(User o) {
 		return getName().compareToIgnoreCase(o.getName());
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		User other = (User) obj;
 		if (login == null) {
 			if (other.login != null) {
 				return false;
 			}
 		}
 		else if (!login.equals(other.login)) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Returns the value of the <code>email</code> property.
 	 * 
 	 * @return a {@link String}.
 	 */
 	@Column(length = MAXIMUM_EMAIL_LENGTH)
 	@Email
 	@Length(min = User.MINIMUM_EMAIL_LENGTH, max = User.MAXIMUM_EMAIL_LENGTH)
 	public String getEmail() {
 		return email;
 	}
 
 	/**
 	 * Returns the value of the <code>id</code> property.
 	 * 
 	 * @return a {@link Integer}.
 	 */
 	@Id
 	@GeneratedValue
 	public Integer getId() {
 		return id;
 	}
 
 	/**
 	 * Returns the value of the <code>login</code> property.
 	 * 
 	 * @return a {@link String}.
 	 */
 	@Column(nullable = false, unique = true, length = MAXIMUM_LOGIN_LENGTH)
 	@NotNull
 	@Length(min = User.MINIMUM_LOGIN_LENGTH, max = User.MAXIMUM_LOGIN_LENGTH)
 	public String getLogin() {
 		return login;
 	}
 
 	/**
 	 * Returns the value of the <code>name</code> property.
 	 * 
 	 * @return a {@link String}.
 	 */
 	@Column(nullable = false, length = MAXIMUM_NAME_LENGTH)
 	@NotNull
 	@Length(min = User.MINIMUM_NAME_LENGTH, max = User.MAXIMUM_NAME_LENGTH)
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Returns the value of the <code>password</code> property.
 	 * 
 	 * @return a {@link String}.
 	 */
 	@Column(nullable = false, length = MAXIMUM_PASSWORD_LENGTH)
 	@NotNull
 	@Length(min = User.MINIMUM_PASSWORD_LENGTH, max = User.MAXIMUM_PASSWORD_LENGTH)
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * Returns the value of the <code>permissionGroups</code> property.
 	 * 
 	 * @return a {@link List<PermissionGroup>}.
 	 */
 	@ManyToMany
 	@OrderBy("name asc")
 	@JoinTable(name = "user_permissiongroup", joinColumns = @JoinColumn(name = "user_id", nullable = false), inverseJoinColumns = @JoinColumn(name = "permissiongroup_id", nullable = false))
 	public List<PermissionGroup> getPermissionGroups() {
 		return permissionGroups;
 	}
 
 	/**
 	 * Returns an unmodifiable list containing all the permissions granted to this user. It is
 	 * comprised by the sum of all permissions in its permission groups, except the ones in its
 	 * removed permissions list.
 	 * 
 	 * @return a {@link List} of {@link Permission}s.
 	 */
 	@Transient
 	final public List<Permission> getPermissions() {
 
 		Set<Permission> permissions = new HashSet<Permission>();
 
 		for (PermissionGroup group : getPermissionGroups()) {
 
 			for (Permission permission : group.getPermissions()) {
 				permissions.add(permission);
 			}
 
 		}
 
 		for (Permission permission : getRemovedPermissions()) {
 			permissions.remove(permission);
 		}
 
 		ArrayList<Permission> list = new ArrayList<Permission>(permissions);
 
 		Collections.sort(list);
 
 		return Collections.unmodifiableList(list);
 
 	}
 
 	/**
 	 * Returns the value of the <code>removedPermissions</code> property.
 	 * 
 	 * @return a {@link List<Permission>}.
 	 */
 	@ManyToMany
 	@OrderBy("name asc")
 	@JoinTable(name = "user_removedpermission", joinColumns = @JoinColumn(name = "user_id", nullable = false), inverseJoinColumns = @JoinColumn(name = "permission_id", nullable = false))
 	public List<Permission> getRemovedPermissions() {
 		return removedPermissions;
 	}
 
 	/**
 	 * Given a {@link Class} object, returns the corresponding {@link Role} instance or null if this
 	 * user has no such role.
 	 * 
 	 * @param <T> a {@link Role} subclass.
 	 * @param clasz a {@link Class<T>}.
 	 * @return a {@link #T}.
 	 */
 	@SuppressWarnings("unchecked")
 	public final <T extends Role> T getRole(Class<T> clasz) {
 
 		T role = null;
 
 		for (Role r : getRoles()) {
 			if (r.getClass().equals(clasz)) {
 				role = (T) r;
 				break;
 			}
 		}
 
 		return role;
 
 	}
 
 	/**
 	 * Returns the value of the <code>roles</code> property.
 	 * 
 	 * @return a {@link List<Role>}.
 	 */
 	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
 	public List<Role> getRoles() {
 		return roles;
 	}
 
 	/**
 	 * Tells if this user has at least one of a set of permissions.
 	 * 
 	 * @param permissionName an array of {@link String}s.
 	 * @return a <code>boolean</code>.
 	 */
 	public boolean hasPermission(String... permissionNames) {
 
 		boolean result = false;
 
 		for (PermissionGroup permissionGroup : permissionGroups) {
 			
 			if (permissionGroup.hasPermission(permissionNames)) {
 				result = true;
 				break;
 			}
 
 		}
 
 		return result;
 
 	}
 
 	@Override
 	public int hashCode() {
 		return login != null ? login.hashCode() : super.hashCode();
 	}
 
 	/**
 	 * Is this user's credentials expired?
 	 * 
 	 * @return a {@link boolean}.
 	 */
 	public boolean isCredentialsExpired() {
 		return credentialsExpired;
 	}
 
 	/**
 	 * Is this user's account enabled?.
 	 * 
 	 * @return a {@link boolean}.
 	 */
 	public boolean isEnabled() {
 		return enabled;
 	}
 
 	/**
 	 * Is this user's account expired?
 	 * 
 	 * @return a {@link boolean}.
 	 */
 	public boolean isExpired() {
 		return expired;
 	}
 
 	/**
 	 * Is this user's account locked?
 	 * 
 	 * @return a {@link boolean}.
 	 */
 	public boolean isLocked() {
 		return locked;
 	}
 
 	/**
 	 * Is this user's logged in now?
 	 * 
 	 * @return a {@link boolean}.
 	 */
 	public boolean isLoggedIn() {
 		return loggedIn;
 	}
 
 	/**
 	 * Removes a role from this user.
 	 * 
 	 * @param role a {@link Role}.
 	 */
 	public void remove(Role role) {
 		permissionGroups.remove(role);
 	}
 
 	/**
 	 * Removes all roles from a given type from this user.
 	 * 
 	 * @param roleClass a {@link Class}.
 	 */
 	public <T extends Role> void removeRole(Class<T> roleClass) {
 
 		for (Iterator<Role> i = roles.iterator(); i.hasNext();) {
 
 			Role role = i.next();
 
 			if (role.getClass().equals(roleClass)) {
 				i.remove();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Removes a permission group from this user.
 	 * 
 	 * @param permissionGroup a {@link PermissionGroup}.
 	 */
 	public void remove(PermissionGroup permissionGroup) {
 		permissionGroups.remove(permissionGroup);
 	}
 
 	/**
 	 * Removes a removed permission from this user.
 	 * 
 	 * @param permission a {@link Permission}.
 	 */
 	public void removeRemovedPermission(Permission permission) {
 		removedPermissions.remove(permission);
 	}
 
 	/**
 	 * Changes the value of the <code>credentialsExpired</code> property.
 	 * 
 	 * @param credentialsExpired a {@link boolean}.
 	 */
 	public void setCredentialsExpired(boolean credentialsExpired) {
 		this.credentialsExpired = credentialsExpired;
 	}
 
 	/**
 	 * Changes the value of the <code>email</code> property.
 	 * 
 	 * @param email a {@link String}.
 	 */
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	/**
 	 * Changes the value of the <code>enabled</code> property.
 	 * 
 	 * @param enabled a {@link boolean}.
 	 */
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	/**
 	 * Changes the value of the <code>expired</code> property.
 	 * 
 	 * @param expired a {@link boolean}.
 	 */
 	public void setExpired(boolean accountExpired) {
 		this.expired = accountExpired;
 	}
 
 	/**
 	 * Changes the value of the <code>id</code> property.
 	 * 
 	 * @param id a {@link Integer}.
 	 */
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	/**
 	 * Changes the value of the <code>locked</code> property.
 	 * 
 	 * @param locked a {@link boolean}.
 	 */
 	public void setLocked(boolean accountLocked) {
 		this.locked = accountLocked;
 	}
 
 	/**
 	 * Changes the value of the <code>loggedIn</code> property.
 	 * 
 	 * @param loggedIn a {@link boolean}.
 	 */
 	public void setLoggedIn(boolean loggedIn) {
 		this.loggedIn = loggedIn;
 	}
 
 	/**
 	 * Changes the value of the <code>login</code> property.
 	 * 
 	 * @param login a {@link String}.
 	 */
 	public void setLogin(String login) {
 		this.login = login;
 	}
 
 	/**
 	 * Changes the value of the <code>name</code> property.
 	 * 
 	 * @param name a {@link String}.
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Changes the value of the <code>password</code> property.
 	 * 
 	 * @param password a {@link String}.
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	/**
 	 * Changes the value of the <code>permissionGroups</code> property.
 	 * 
 	 * @param permissionGroups a {@link List<PermissionGroup>}.
 	 * @deprecated Use {@link #add(PermissionGroup)} and {@link #remove(PermissionGroup)} instead.
 	 */
 	@Deprecated
 	public void setPermissionGroups(List<PermissionGroup> permissionGroups) {
 		this.permissionGroups = permissionGroups;
 	}
 
 	/**
 	 * Changes the value of the <code>removedPermissions</code> property.
 	 * 
 	 * @param removedPermissions a {@link List<Permission>}.
 	 * @deprecated Use {@link #addRemovedPermission(Permission)} and
 	 * {@link #removeRemovedPermisson(Permission)} instead.
 	 */
 	@Deprecated
 	public void setRemovedPermissions(List<Permission> removedRoles) {
 		this.removedPermissions = removedRoles;
 	}
 
 	/**
 	 * Changes the value of the <code>roles</code> property.
 	 * 
 	 * @param roles a {@link List<Role>}.
 	 * @deprecated Use {@link #add(Role)} and {@link #remove(Role)} instead.
 	 */
 	@Deprecated
 	public void setRoles(List<Role> roles) {
 		this.roles = roles;
 	}
 
 	/**
 	 * Returns the <code>name</code> property.
 	 * 
 	 * @return a {@link String}.
 	 */
 	@Override
 	public String toString() {
 		return getName();
 	}
 
 }
