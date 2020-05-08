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
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 
 import org.hibernate.validator.Length;
 import org.hibernate.validator.NotNull;
 import org.hibernate.validator.Size;
 
 /**
  * Class that represents a group of permissions. Each one can belong to an
  * {@link UserGroup} or belong to all of them (<code>owner</code> property set
  * to null).
  * 
  * @author Thiago H. de Paula Figueiredo
  */
 @Entity
 @Table(name = "permissiongroup")
 final public class PermissionGroup implements Comparable<Permission>,
 		Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Minimum name length.
 	 */
 	public static final int MINIMUM_NAME_LENGTH = 2;
 
 	/**
 	 * Maximum name length.
 	 */
 	public static final int MAXIMUM_NAME_LENGTH = 50;
 
 	/**
 	 * Name of the permission group that contains all users.
 	 */
 	public static final String ALL_USERS_PERMISSION_GROUP_NAME = "All users";
 
 	private Integer id;
 
 	private String name;
 
 	private List<Permission> permissions = new ArrayList<Permission>();
 
	private PermissionGroup owner;
 
 	private boolean shared;
 
 	/**
 	 * No-arg constructor.
 	 */
 	public PermissionGroup() {
 	}
 
 	/**
 	 * Constructor that receives a name.
 	 * 
 	 * @param name a {@link String}. It cannot be null.
 	 * @throws IllegalArgumentException if <code>name</code> is null.
 	 */
 	public PermissionGroup(String name) {
 
 		if (name == null) {
 			throw new IllegalArgumentException("Parameter name cannot be null");
 		}
 
 		this.name = name;
 
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return name != null ? name.hashCode() : super.hashCode();
 	}
 
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
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
 		final PermissionGroup other = (PermissionGroup) obj;
 		if (name == null) {
 			if (other.name != null) {
 				return false;
 			}
 		} else if (!name.equals(other.name)) {
 			return false;
 		}
 		return true;
 
 	}
 
 	/**
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	public int compareTo(Permission o) {
 		return getName().compareToIgnoreCase(o.getName());
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
 	 * Changes the value of the <code>id</code> property.
 	 * 
 	 * @param id a {@link Integer}.
 	 */
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	/**
 	 * Returns the value of the <code>name</code> property.
 	 * 
 	 * @return a {@link String}.
 	 */
 	@NotNull
 	@Length(min = MINIMUM_NAME_LENGTH, max = MAXIMUM_NAME_LENGTH)
 	@Column(nullable = false, unique = true, length = MAXIMUM_NAME_LENGTH)
 	public String getName() {
 		return name;
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
 	 * Returns the value of the <code>permissions</code> property.
 	 * 
 	 * @return a {@link List<Permission>}.
 	 */
 	@ManyToMany
 	@JoinTable(name = "permissiongroup_permission", joinColumns = @JoinColumn(name = "permissiongroup_id", nullable = false), inverseJoinColumns = @JoinColumn(name = "permission_id", nullable = false))
 	@OrderBy("name asc")
 	@Size(min = 1, max = 100)
 	public List<Permission> getPermissions() {
 		return permissions;
 	}
 
 	/**
 	 * Retorna o valor da propriedade <code>owner</code>.
 	 * 
 	 * @return o valor de <code>owner</code>.
 	 */
	@ManyToOne
 	@JoinColumn(name = "owner_id")
	public PermissionGroup getOwner() {
 		return owner;
 	}
 
 	/**
 	 * Retorna o valor da propriedade <code>shared</code>.
 	 * 
 	 * @return o valor de <code>shared</code>.
 	 */
 	public boolean isShared() {
 		return shared;
 	}
 
 	/**
 	 * Altera o valor da propriedade <code>shared</code>.
 	 * 
 	 * @param <code>shared</code> o novo valor da propriedade
 	 *        <code>shared</code>.
 	 */
 	public void setShared(boolean shared) {
 		this.shared = shared;
 	}
 
 	/**
 	 * Altera o valor da propriedade <code>owner</code>.
 	 * 
 	 * @param <code>owner</code> o novo valor da propriedade <code>owner</code>.
 	 */
	public void setOwner(PermissionGroup owner) {
 		this.owner = owner;
 	}
 
 	/**
 	 * Adds a permission to this group.
 	 * 
 	 * @param permission a {@link Permission}.
 	 */
 	public void add(Permission permission) {
 
 		if (permissions.contains(permission) == false) {
 			permissions.add(permission);
 		}
 
 	}
 
 	/**
 	 * Removes a permission from this group.
 	 * 
 	 * @param permission a {@link Permission}.
 	 */
 	public void remove(Permission permission) {
 		permissions.remove(permission);
 	}
 
 	/**
 	 * Changes the value of the <code>permissions</code> property.
 	 * 
 	 * @param permissions a {@link List<Permission>}.
 	 * @deprecated Use {@link #add(Permission)} and {@link #remove(Permission)}
 	 *             instead.
 	 */
 	public void setPermissions(List<Permission> permissions) {
 		this.permissions = permissions;
 	}
 
 	/**
 	 * Tells if this user has at least one of a set of permissions.
 	 * 
 	 * @param permissionName an array of {@link String}s.
 	 * @return a <code>boolean</code>.
 	 */
 	public boolean hasPermission(String... permissionNames) {
 
 		boolean result = false;
 
 		outer: for (Permission permission : permissions) {
 
 			for (String permissionName : permissionNames) {
 
 				if (permission.getName().equals(permissionName)) {
 					result = true;
 					break outer;
 				}
 
 			}
 
 		}
 
 		return result;
 
 	}
 
 	/**
 	 * Returns the <code>name</code> property.
 	 * 
 	 * @return a {@link String}.
 	 */
 	@Override
 	public String toString() {
 		return name;
 	}
 
 }
