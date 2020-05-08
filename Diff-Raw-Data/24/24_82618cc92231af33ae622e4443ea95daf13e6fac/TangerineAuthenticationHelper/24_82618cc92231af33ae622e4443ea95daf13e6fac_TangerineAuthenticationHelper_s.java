 package com.orangeleap.tangerine.security;
 
 import org.springframework.security.Authentication;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
 
 import com.orangeleap.common.security.OrangeLeapAuthenticationProvider.AuthenticationHelper;
 
 /*
  * Used for non-CAS auth to populate user details 
  */
 public class TangerineAuthenticationHelper implements AuthenticationHelper  {
 	

 	private TangerineSessionInformationFilter tangerineSessionInformationFilter;
 	
 	public void setTangerineSessionInformationFilter(TangerineSessionInformationFilter tangerineSessionInformationFilter) {
 		this.tangerineSessionInformationFilter = tangerineSessionInformationFilter;
 	}
 	
 	@Override
 	public void postProcess(Authentication authentication) {
 		
 		if (!(authentication instanceof UsernamePasswordAuthenticationToken)) return;
 		
 		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)authentication;
 
 		// Needed by tangerineUserHelper
 		if (SecurityContextHolder.getContext().getAuthentication() == null) SecurityContextHolder.getContext().setAuthentication(token);
 		
 		if (token.getDetails() == null) {
 			token.setDetails(new TangerineAuthenticationDetails());
 		}
 		tangerineSessionInformationFilter.loadTangerineDetails(token);
		
		
 	}
 
 	
 }
