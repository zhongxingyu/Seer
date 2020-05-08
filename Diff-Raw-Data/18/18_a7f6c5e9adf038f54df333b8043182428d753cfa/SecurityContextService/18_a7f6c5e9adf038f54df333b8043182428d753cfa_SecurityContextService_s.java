 package com.twistlet.falcon.security.service;
 
import java.util.Collection;
 
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContext;
 
 public interface SecurityContextService {
 	SecurityContext getContext();
 
 	Authentication getAuthentication();
 
	Collection<? extends GrantedAuthority> getAuthorities();
 }
