 package com.raissi.managedbeans;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Component;
 
 import com.raissi.domain.User;
 import com.raissi.security.Authority;
 import com.raissi.security.CustomUserDetails;
 import com.raissi.util.UserRole;
 
@Component("loggedInUser")
 @Scope("session")
 public class LoggedInUser  implements Serializable{
 	private static final long serialVersionUID = -1033377115353626379L;
 	
 	@Inject
 	@Named("rememberMeServices")
 	private RememberMeServices rememberMeServices;
 	
 	private User user;
 
 	public User getUser() {
 		return user;
 	}
	@PostConstruct
	public void init(){
	}
 
 	/**
 	 * Please call this.updateSecurityContext after setting user
 	 * @param user
 	 */
 	public void setUser(User user) {
 		this.user = user;
 	}
 	
 	
 	public void updateSecurityContext(HttpServletRequest request, HttpServletResponse response){
 		List<Authority> auths = new ArrayList<Authority>();
 		if(user.hasRole(UserRole.ADMIN.toString())){
 			auths.add(new Authority("ROLE_ADMIN")); //Role here, like "admin"
 			auths.add(new Authority("ROLE_USER"));
 		}else {
 			auths.add(new Authority(user.getRole())); //Role here, like "admin"
 		}
 		
 		Authentication authentication =  new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null, auths);
 		SecurityContextHolder.getContext().setAuthentication(authentication);
 		
 		HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
 		    @Override public String getParameter(String name) { return "true"; }            
 		};
		System.out.println("---------------Remember me services in update: "+rememberMeServices);
 		rememberMeServices.loginSuccess(wrapper, response, authentication);
 	}
 
 	
 }
