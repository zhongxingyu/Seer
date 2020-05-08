 package org.svfc57.entities;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Transient;
 
 import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.UserDetails;
 
 @Entity(name="User")
 public class User implements UserDetails {
 
 	@Transient
 	private static final long serialVersionUID = 5905943605972246434L;
 
 	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	public long id;
 	
 	public String username;
 	public String password;
 	public boolean accountNonExpired;
 	public boolean accountNonLocked;
 	public boolean credentialsNonExpired;
 	public boolean enabled;
 	public String authority;
 	//@OneToMany
 	//public Set<Authority> roles;
 /*	
 	public Collection<GrantedAuthority> getAuthorities() {
 		List<GrantedAuthority> authorities = CollectionFactory.newList();
 		for (Authority role : roles) {
 			authorities.add(new GrantedAuthorityImpl(role.getAuthority()));
 		}
 		return authorities;
 	}
 */
 	public Collection<GrantedAuthority> getAuthorities() {
 		List<GrantedAuthority> authorities = CollectionFactory.newList();
 		authorities.add(new GrantedAuthorityImpl(authority));
 		return authorities;
 	}
 	
 	@Override
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	@Override
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	@Override
 	public boolean isAccountNonExpired() {
 		return accountNonExpired;
 	}
 
 	@Override
 	public boolean isAccountNonLocked() {
 		return accountNonLocked;
 	}
 
 	@Override
 	public boolean isCredentialsNonExpired() {
 		return credentialsNonExpired;
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return enabled;
 	}
 }
