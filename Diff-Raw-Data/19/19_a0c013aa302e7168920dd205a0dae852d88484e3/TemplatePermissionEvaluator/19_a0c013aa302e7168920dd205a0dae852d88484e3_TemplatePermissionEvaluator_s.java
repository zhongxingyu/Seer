 package org.dedeler.template.security;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.List;
 
 import org.dedeler.template.model.Privilege;
 import org.dedeler.template.model.Role;
 import org.springframework.security.access.PermissionEvaluator;
 import org.springframework.security.core.Authentication;
 import org.springframework.stereotype.Component;
 
 @Component
 public class TemplatePermissionEvaluator implements PermissionEvaluator {
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
 
		Collection<Role> authorities = (Collection<Role>) authentication.getAuthorities();
		for (Role role : authorities) {
			List<Privilege> privileges = role.getPrivileges();
			for (Privilege privilege : privileges) {
				if (permission.equals(privilege.getName())) {
					return true;
				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
 		return hasPermission(authentication, null, permission);
 	}
 
 }
