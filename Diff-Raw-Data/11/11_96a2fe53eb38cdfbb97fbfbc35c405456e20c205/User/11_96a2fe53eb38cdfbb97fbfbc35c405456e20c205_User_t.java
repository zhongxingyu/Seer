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
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.UserDetails;
 
 @Entity
 @Table(name="USER")
 public class User implements UserDetails,Serializable{
 	@Id
 	@GeneratedValue
 	@Column(name="USER_ID")
 	private Long userId;
 	@Column(name = "USERNAME", unique=true, nullable = false, length = 50)
 	private String username;
 	@Column(name = "PASSWORD", nullable = true, length = 50)
 	private String password;
 	
 	@OneToOne(cascade=CascadeType.ALL)
 	@JoinColumn(name="USER_PROFILE_ID")
 	private UserProfile userProfile;
 	
 	@ManyToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
     @JoinTable(name="USER_ROLE",
                joinColumns=@JoinColumn(name="USER_ID"),
               inverseJoinColumns=@JoinColumn(name="ROLE_ID"))
	@Fetch(FetchMode.SELECT)
 	private Collection<Role> roles = new ArrayList<Role>();
 	
	@OneToMany(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST, CascadeType.MERGE})
 	@JoinTable(name = "USER_GROUP",
 	joinColumns = @JoinColumn(name = "USER_ID", unique=false),
 	inverseJoinColumns = @JoinColumn(name = "GROUP_ID", unique=false))
	@Fetch(FetchMode.SELECT)
 	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)	
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
