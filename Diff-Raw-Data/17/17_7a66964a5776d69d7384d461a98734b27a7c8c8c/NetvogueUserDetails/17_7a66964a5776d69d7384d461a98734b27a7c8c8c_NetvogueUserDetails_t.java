 package org.netvogue.server.webmvc.security;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.netvogue.server.neo4japi.domain.User;
 
 public class NetvogueUserDetails implements UserDetails {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
 	private final User user;
 	
 	NetvogueUserDetails(User user) {
 		this.user = user;
 	}
 	
 	public Collection<? extends GrantedAuthority> getAuthorities() {
 		User.Roles[] roles = user.getRoles();
         if (roles ==null) return Collections.emptyList();
         return Arrays.<GrantedAuthority>asList(roles);
 	}
 
 	public String getPassword() {
 		System.out.println("Password stored is" + user.getPassword());
 		return user.getPassword();
 	}
 
 	public String getUsername() {
 		return user.getEmail();
 	}
 
 	public boolean isAccountNonExpired() {
 		return true;
 	}
 
 	public boolean isAccountNonLocked() {
 		return true;
 	}
 
 	public boolean isCredentialsNonExpired() {
 		return true;
 	}
 
 	public boolean isEnabled() {
 		return user.getAccountEnabled();
 	}
 
 	public User getUser() {
         return user;
     }
 }
