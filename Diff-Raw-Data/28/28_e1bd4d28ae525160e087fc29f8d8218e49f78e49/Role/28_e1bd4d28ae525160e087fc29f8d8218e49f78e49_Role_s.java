 /**
 * My Net is free software, you can redistribute it and/or modify
 * it under the terms of GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * You should have received a copy of the the GNU Affero
 * General Public License, along with My Net. If not, see
 *
 * http://www.gnu.org/licenses/agpl.txt
 */
 
 package com.perevillega.mynet.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Lob;
 import javax.persistence.ManyToMany;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 import javax.persistence.Version;
 import org.hibernate.validator.Length;
 import org.hibernate.validator.NotNull;
 
 @Entity
 @Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = "name"),		
 	})
 @NamedQueries({
 @NamedQuery(
 	    name="findAllRoles",
 	    query="SELECT OBJECT(role) FROM Role role"
 	    	)
 })
 public class Role implements Serializable
 {
     private Long id;
     private Integer version;
     private String name;
     private boolean selected;
     private Set<User> users = new HashSet<User>();
     private int numUsers;
     private String description;
     
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((version == null) ? 0 : version.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof Role))
 			return false;
 		Role other = (Role) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (version == null) {
 			if (other.version != null)
 				return false;
 		} else if (!version.equals(other.version))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {	
 		return this.name;
 	}
 
     @Id @GeneratedValue
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Version
     public Integer getVersion() {
         return version;
     }
 
     private void setVersion(Integer version) {
         this.version = version;
     }
 
     @Length(max = 20)
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
     
     @ManyToMany(mappedBy = "roles", fetch=FetchType.LAZY)
     public Set<User> getUsers() {
 		return users;
 	}
 
 	public void setUsers(Set<User> users) {
 		this.users = users;
 	}
     
 	public void removeUser(User user) {
 		this.users.remove(user);
 	}
 		
 	public void addUser(User user) {
 		this.users.add(user);
 	}
 
 	@Transient
 	public boolean isSelected() {
 		return selected;
 	}
 
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 
 	@NotNull
 	public int getNumUsers() {
 		return numUsers;
 	}
 
 	public void setNumUsers(int numUsers) {
 		this.numUsers = numUsers;
 	}
 	
 	public void remove(){		
     	for(User u: this.users) {
     		u.removeRole(this);
     	}		
 	}
 	
 	@Lob
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String shortDescription() {
 		String tmp = "";
 		if (this.description != null) {
 			int endIndex = Math.min(30, this.description.length());
 			tmp = this.description.substring(0, endIndex);
 			if (endIndex >= 30) {
 				tmp += "[...]";
 			}
 		}
 		return tmp;
 	}
 }
