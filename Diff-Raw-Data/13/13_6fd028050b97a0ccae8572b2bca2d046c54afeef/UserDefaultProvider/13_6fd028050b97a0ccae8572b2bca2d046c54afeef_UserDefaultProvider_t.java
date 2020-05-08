 package com.tda.service.provider;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 import com.tda.model.applicationuser.ApplicationUser;
 import com.tda.model.applicationuser.ApplicationUserBuilder;
 import com.tda.model.applicationuser.Authority;
 import com.tda.service.api.ApplicationUserService;
 import com.tda.service.api.AuthorityService;
 import com.tda.service.exception.NoDataFoundException;
 import com.tda.service.exception.SingleResultExpectedException;
 
 public class UserDefaultProvider implements UserProvider {
 
 	private Properties propertiesHolder;
 
 	private ApplicationUserService applicationUserService;
 
 	private AuthorityService authorityService;
 
 	public void init() {
 		try {
 			loadDefaultUsers(propertiesHolder);
 		} catch (Exception e) {
 			// TODO log error in user loading
 		}
 	}
 
 	private void loadDefaultUsers(final Properties properties)
 			throws SingleResultExpectedException, NoDataFoundException {
 		for (Enumeration<Object> en = properties.keys(); en.hasMoreElements();) {
 			String key = (String) en.nextElement();
 			String[] props = properties.getProperty(key).split(",");
 
 			if (props.length < 2) {
 				// TODO Do something about this. At least we need a password and
 				// one role
 			} else {
 				ApplicationUser anUser = null;
 				try {
 					anUser = (ApplicationUser) applicationUserService
 							.loadUserByUsername(key);
 				} catch (UsernameNotFoundException e) {
 					anUser = buildUserFromProps(key, props);
 					applicationUserService.save(anUser);
 				}
 			}
 		}
 	}
 
 	private ApplicationUser buildUserFromProps(String username, String[] props)
 			throws SingleResultExpectedException, NoDataFoundException {
 		List<Authority> authorities = new ArrayList<Authority>();
 		String password = props[0];
 
 		for (int i = 1; i < props.length; i++) {
 			Authority authority = authorityService.findByAuthority(props[i]);
 			authorities.add(authority);
 		}
 
		ApplicationUser user = ApplicationUserBuilder.createApplicationUser()
 				.withUsername(username).withPassword(password)
 				.withAccountNonExpired().withAccountNonLocked()
 				.withCredentialsNonExpired().withAuthorities(authorities)
 				.enabled().build();

		user.setConfirmPassword(password);
		return user;
 	}
 
 	public void setApplicationUserService(
 			ApplicationUserService applicationUserService) {
 		this.applicationUserService = applicationUserService;
 	}
 
 	public void setAuthorityService(AuthorityService authorityService) {
 		this.authorityService = authorityService;
 	}
 
 	public void setPropertiesHolder(Properties propertiesHolder) {
 		this.propertiesHolder = propertiesHolder;
 	}
 
 }
