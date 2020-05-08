 package com.youthministry.domain;
  
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
  
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.UserDetails;
 
 @Entity
 @Table(name = "user")
 public class User implements UserDetails,Serializable{
 	@Id
 	@GeneratedValue
 	@Column(name="USER_ID")
 	private Long userId;
	@Column(name = "USERNAME", nullable = false, length = 50)
 	private String username;
 	@Column(name = "PASSWORD", nullable = false, length = 50)
 	private String password;
 	
 	@OneToOne(cascade=CascadeType.ALL)
 	private UserProfile userProfile;
 	
 	@OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
 	@Column(name="roles",unique=false)
 	private Collection<Role> roles = new ArrayList<Role>();
 	
 	@OneToMany(cascade=CascadeType.ALL)
 	private Collection<Group> groups = new ArrayList<Group>();
 	
 	public User() {
 		
 	}
 	
 	public User(String username, String password, UserProfile userProfile, Set<Role> roles) {
 		this.username = username;
 		this.password = password;
 		this.userProfile = userProfile;
 		this.roles = roles;
 	}
 	
 	public Long getUserId() {
 		return this.userId;
 	}
 	
 	public void setUserId(Long userId) {
 		this.userId = userId;
 	}
 	
 	@Override
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	@Override
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public UserProfile getUserProfile() {
 		return userProfile;
 	}
 
 	public void setUserProfile(UserProfile userProfile) {
 		this.userProfile = userProfile;
 	}
 	
 	public Collection<Role> getRoles() {
 		return roles;
 	}
 	
 	public void setRoles(Collection<Role> roles) {
 		this.roles = roles;
 	}
 
 	public Collection<Group> getGroups() {
 		return groups;
 	}
 
 	public void setGroups(Collection<Group> groups) {
 		this.groups = groups;
 	}
 	
 	@Override
 	public Collection<GrantedAuthority> getAuthorities() {
 
 		ArrayList l1 = new ArrayList();
 
 		for (Role role : roles) {
 			l1.add(new GrantedAuthorityImpl(role.getName()));
 		}
 		return l1;
 	}
 
 	@Override
 	public boolean isAccountNonExpired() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public boolean isAccountNonLocked() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public boolean isCredentialsNonExpired() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 
 	@Override
 	public boolean isEnabled() {
 		// TODO Auto-generated method stub
 		return true;
 	}
 	
 }
