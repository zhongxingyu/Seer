 package com.twistlet.falcon.security.service;
 
import java.util.List;
 
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContext;
 
 public interface SecurityContextService {
 	SecurityContext getContext();
 
 	Authentication getAuthentication();
 
	List<GrantedAuthority> getAuthorities();
 }
