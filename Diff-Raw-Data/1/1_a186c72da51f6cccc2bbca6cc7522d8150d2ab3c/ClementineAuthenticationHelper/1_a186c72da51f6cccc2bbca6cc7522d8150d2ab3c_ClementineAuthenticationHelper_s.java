 package com.mpower.security;
 
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.security.Authentication;
 import org.springframework.security.context.HttpSessionContextIntegrationFilter;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
 import org.springframework.security.providers.cas.CasAuthenticationToken;
 import org.springframework.security.userdetails.ldap.LdapUserDetails;
 
 import com.mpower.domain.GuruSessionData;
 import com.mpower.util.SessionHelper;
 import com.orangeleap.common.security.OrangeLeapRequestLocal;
 import com.orangeleap.common.security.OrangeLeapUsernamePasswordLocal;
 import com.orangeleap.common.security.OrangeLeapAuthenticationProvider.AuthenticationHelper;
 
 public class ClementineAuthenticationHelper implements AuthenticationHelper {
 
 	@Override
 	public void postProcess(Authentication authentication) {
 		Map<String, Object> info = OrangeLeapUsernamePasswordLocal.getOrangeLeapAuthInfo();
 		
 		//When coming in via the api we need to populate this however when via the UI you will get a 
 		// npe and since this data does not need to be populated for the UI I am just catching the exception
 		GuruSessionData sessiondata = null;
 		try{
 			sessiondata = SessionHelper.getGuruSessionData();
 		}catch(NullPointerException npe){
 			//do nothing
 		}
 		
 		if (authentication instanceof UsernamePasswordAuthenticationToken) {
 
 			UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)authentication;
 
 			if (SecurityContextHolder.getContext().getAuthentication() == null) {
 				SecurityContextHolder.getContext().setAuthentication(token);
 			}
 			token.setDetails(info);
 			info.put(OrangeLeapUsernamePasswordLocal.PASSWORD, authentication.getCredentials());
 			if (sessiondata != null){
 				sessiondata.setPassword(authentication.getCredentials().toString());	
 			}
 			
 		} else if (authentication instanceof CasAuthenticationToken) {
 			CasAuthenticationToken token = (CasAuthenticationToken) authentication;
 			if (SecurityContextHolder.getContext().getAuthentication() == null)
 				SecurityContextHolder.getContext().setAuthentication(token);
 			token.setDetails(info);
 			info.put(OrangeLeapUsernamePasswordLocal.PASSWORD, ((LdapUserDetails)authentication.getPrincipal()).getPassword());
 			if (sessiondata != null){
 				sessiondata.setPassword(((LdapUserDetails)authentication.getPrincipal()).getPassword());	
 			}
 		}
 		String userName = ((LdapUserDetails)authentication.getPrincipal()).getUsername();
 		String siteName = userName.substring(userName.indexOf('@') +1);
 
 		if (sessiondata != null){
 			sessiondata.setUsername(userName);
 		}
 
 		info.put(OrangeLeapUsernamePasswordLocal.USER_NAME,userName);
 		info.put(OrangeLeapUsernamePasswordLocal.SITE,siteName);
 		
 
 		//
 		// let's switch schema's here since we know we are authenticated properly now...
 
 		//setting this in the session so that jasperserver will not have a null password
 		HttpServletRequest request = OrangeLeapRequestLocal.getRequest();
 		if (request != null) {
 		   HttpSession session = request.getSession();
 		   if (session != null) session.setAttribute(HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
 		}
 		
 	}
 }
