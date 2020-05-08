 package com.spring.mti.service;
 
 import java.util.List;
 
 import com.spring.mti.model.security.Authorities;
 import com.spring.mti.model.security.Role;
 import com.spring.mti.model.security.Users;
 
 public interface AuthorityService {
 //	public void setPermissionsUser(Users user);
 //	public void setPermissionsAdmin(Users user);
 	public List<String> getAllPermissionsByUserId(long user_id);
 	public void createRole(Role role);
 	public void deleteRole(Role role);
	public List<Object[]> getAllRoles();
 	void setPermissions(Users user, Role role);
 	public Role getRoleByName(String role);
 	List<Authorities> getAllPermissions();
 }
