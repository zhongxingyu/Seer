 package com.trivadis.camel.security.rb;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.camel.CamelAuthorizationException;
 import org.apache.camel.component.shiro.security.ShiroSecurityPolicy;
 import org.apache.camel.spring.SpringRouteBuilder;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.IncorrectCredentialsException;
 import org.apache.shiro.authc.LockedAccountException;
 import org.apache.shiro.authc.UnknownAccountException;
 import org.apache.shiro.authz.Permission;
 import org.apache.shiro.authz.permission.WildcardPermission;
 import org.springframework.stereotype.Component;
 
 /**
  * Java RouteBuilder implementation for the calculateCategoryShiro route. This
  * route requires the <b>trivadis:calculateCategory:*</b> permission.
  * 
  * @author Dominik Schadow, Trivadis GmbH
  * @version 1.0.0
  */
 @Component
 public class CategoryShiroRouteBuilder extends SpringRouteBuilder {
 	@Override
 	public void configure() throws Exception {
 		final byte[] passPhrase = "CamelSecureRoute".getBytes();
 
 		List<Permission> permissionsList = new ArrayList<Permission>();
 		Permission permission = new WildcardPermission(
 				"trivadis:calculateCategory:*");
 		permissionsList.add(permission);
 
 		ShiroSecurityPolicy shiroSecurityPolicy = new ShiroSecurityPolicy(
 				"classpath:shirosecuritypolicy.ini", passPhrase, true,
 				permissionsList);
 
 		onException(UnknownAccountException.class,
 				IncorrectCredentialsException.class,
 				LockedAccountException.class, AuthenticationException.class)
 				.to("mock:authenticationException");
		onException(CamelAuthorizationException.class).handled(true);
 
 		from("direct:calculateCategoryShiro").routeId("calculateCategoryShiro")
 				.policy(shiroSecurityPolicy)
 				.beanRef("categoryBean", "processData");
 	}
 }
