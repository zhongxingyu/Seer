 package it.dariofabbri.ivncr.security.shiro;
 
 import it.dariofabbri.ivncr.model.security.Permission;
 import it.dariofabbri.ivncr.model.security.Role;
 import it.dariofabbri.ivncr.model.security.User;
 import it.dariofabbri.ivncr.service.local.ServiceFactory;
 import it.dariofabbri.ivncr.service.local.security.SecurityService;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.shiro.authc.AccountException;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.AuthenticationInfo;
 import org.apache.shiro.authc.AuthenticationToken;
 import org.apache.shiro.authc.UnknownAccountException;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.authz.AuthorizationException;
 import org.apache.shiro.authz.AuthorizationInfo;
 import org.apache.shiro.authz.SimpleAuthorizationInfo;
 import org.apache.shiro.realm.AuthorizingRealm;
 import org.apache.shiro.subject.PrincipalCollection;
 
 public class DatabaseBackedRealm extends AuthorizingRealm {
 
 	@Override
 	protected AuthenticationInfo doGetAuthenticationInfo(
 			AuthenticationToken token) throws AuthenticationException {
 
 		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
 		String username = upToken.getUsername();
 
 		// Null username is invalid.
 		//
 		if (username == null) {
 			throw new AccountException(
 					"Null usernames are not allowed by this realm.");
 		}
 
 		// Lookup user.
 		//
 		SecurityService ss = ServiceFactory.createSecurityService();
 		User user = ss.getByUsername(username);
 		if (user == null)
 			throw new UnknownAccountException("No account found for user [" + username + "]");
 		
 		// Extract digested password informations.
 		//
 		String digest = user.getDigest();
 		String salt = user.getSalt();
 		Integer iterations = user.getIterations();
 
 		// Create authentication info.
 		//
 		String realm = getName();
 		SaltedWithIterationAuthenticationInfo info = 
 				new SaltedWithIterationAuthenticationInfo(username, digest, realm);
 		
 		// Set up digest info.
 		//
 		info.setIterations(iterations);
 		info.setSalt(salt);
 
 		// Set up user details as a secondary principal.
 		//
 		info.addPrincipal(user, realm);
 		
 		// Always clean up cached authorization after a login.
 		//
 		clearCachedAuthorizationInfo(info.getPrincipals());
 
 		return info;
 	}
 
 	@Override
 	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
 
 		// Null usernames are invalid.
 		//
 		if (principals == null) {
 			throw new AuthorizationException(
 					"PrincipalCollection method argument cannot be null.");
 		}

		// Extract username from principal.
		//
 		String username = (String) getAvailablePrincipal(principals);
 		
 		// Look up roles.
 		//
 		SecurityService ss = ServiceFactory.createSecurityService();
 		List<Role> rolesList = ss.getUserRoles(username);
 		Set<String> roles = new HashSet<String>();
 		for(Role r : rolesList)
 			roles.add(r.getRolename());
 		
 		// Look up permissions.
 		//
 		List<Permission> permissionsList = ss.getUserPermissions(username);
 		Set<String> permissions = new HashSet<String>();
 		for(Permission p : permissionsList)
 			permissions.add(p.getPermissionString());
 		
 		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roles);
 		info.setStringPermissions(permissions);
 		
 		return info;
 	}
 
 }
