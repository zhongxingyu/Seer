 package com.twistlet.falcon.security.service;
 
import java.util.Collection;
 
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 public class SecurityContextServiceImpl implements SecurityContextService {
 
 	@Override
 	public SecurityContext getContext() {
 		return SecurityContextHolder.getContext();
 	}
 
 	@Override
 	public Authentication getAuthentication() {
 		return getContext().getAuthentication();
 	}
 
 	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return getAuthentication().getAuthorities();
 	}
 }
