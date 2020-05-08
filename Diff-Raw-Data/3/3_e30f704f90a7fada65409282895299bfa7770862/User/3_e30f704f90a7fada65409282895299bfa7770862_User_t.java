 package com.web.archetype.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
import javax.persistence.Transient;
 
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.UserDetails;
 
 @Entity
 @NamedQueries({
 	@NamedQuery(name="User.findByUsername",
 			query="SELECT u FROM User u WHERE u.username = :username")})
 public class User implements Serializable, UserDetails {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Long id;
 	
 	@Column
 	private String name;
 	
 	@Column
 	private String email;
 	
 	@Column
 	private String username;
 	
 	@Column
 	private String password;
 	
 	@ManyToMany(fetch=FetchType.EAGER)
 	private List<Role> roles;
 	
 	@Transient
 	public Collection<GrantedAuthority> getAuthorities() {
 		List<GrantedAuthority> result = new ArrayList<GrantedAuthority>();
 		for (Role role : roles) {
 			result.add(new GrantedAuthorityImpl(role.getName()));
 		}
 		return result;
 	}
 	
 	@Transient
 	public boolean isAccountNonExpired() {
 		return true;
 	}
 	
 	@Transient
 	public boolean isAccountNonLocked() {
 		return true;
 	}
 	
 	@Transient
 	public boolean isCredentialsNonExpired() {
 		return true;
 	}
 	
 	@Transient
 	public boolean isEnabled() {
 		return true;
 	}
 	
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getEmail() {
 		return email;
 	}
 	
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	
 	public String getUsername() {
 		return username;
 	}
 	
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 	
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((email == null) ? 0 : email.hashCode());
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result
 				+ ((password == null) ? 0 : password.hashCode());
 		result = prime * result
 				+ ((username == null) ? 0 : username.hashCode());
 		return result;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		User other = (User) obj;
 		if (email == null) {
 			if (other.email != null)
 				return false;
 		} else if (!email.equals(other.email))
 			return false;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (password == null) {
 			if (other.password != null)
 				return false;
 		} else if (!password.equals(other.password))
 			return false;
 		if (username == null) {
 			if (other.username != null)
 				return false;
 		} else if (!username.equals(other.username))
 			return false;
 		return true;
 	}
 
 }
