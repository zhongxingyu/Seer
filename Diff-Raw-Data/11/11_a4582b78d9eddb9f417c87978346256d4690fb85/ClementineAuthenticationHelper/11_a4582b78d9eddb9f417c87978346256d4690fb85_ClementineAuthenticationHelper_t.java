 package com.mpower.security;
 
 import java.util.Map;
 
 import org.springframework.security.Authentication;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
 import org.springframework.security.providers.cas.CasAuthenticationToken;
 import org.springframework.security.ui.WebAuthenticationDetails;
 import org.springframework.security.userdetails.ldap.LdapUserDetails;
 
 import com.orangeleap.common.security.OrangeLeapUsernamePasswordLocal;
 import com.orangeleap.common.security.OrangeLeapAuthenticationProvider.AuthenticationHelper;
 
 public class ClementineAuthenticationHelper implements AuthenticationHelper {
 
 	@Override
 	public void postProcess(Authentication authentication) {
 		Map<String, Object> info = OrangeLeapUsernamePasswordLocal.getOrangeLeapAuthInfo();
 		if (authentication instanceof UsernamePasswordAuthenticationToken) {
 
 			UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)authentication;
 
 			if (SecurityContextHolder.getContext().getAuthentication() == null) {
 				SecurityContextHolder.getContext().setAuthentication(token);
 			}
 			token.setDetails(info);
			info.put(OrangeLeapUsernamePasswordLocal.PASSWORD, authentication.getCredentials());
 		} else if (authentication instanceof CasAuthenticationToken) {
 			CasAuthenticationToken token = (CasAuthenticationToken) authentication;
 			if (SecurityContextHolder.getContext().getAuthentication() == null)
 				SecurityContextHolder.getContext().setAuthentication(token);
 			token.setDetails(info);
			info.put(OrangeLeapUsernamePasswordLocal.PASSWORD, ((LdapUserDetails)authentication.getPrincipal()).getPassword());
 		}
 		String userName = ((LdapUserDetails)authentication.getPrincipal()).getUsername();
 		String siteName = userName.substring(userName.indexOf('@') +1);

 		info.put(OrangeLeapUsernamePasswordLocal.USER_NAME,userName);
 		info.put(OrangeLeapUsernamePasswordLocal.SITE,siteName);
		
		//
		// let's switch schema's here since we know we are authenticated properly now...
		
 	}
 }
