 package security;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import usersManagement.BusinessRole;
 import usersManagement.Permission;
 import usersManagement.Role;
 import usersManagement.RolePermission;
 import usersManagement.SystemRole;
 import usersManagement.User;
 
 public class Principal {
 
 	private String name;
 	private String surname;
 	private String email;
 	private String serialNumber;
 
 	private Map<String, List<String>> permissionsPerRole;
 	private Map<String, String> departmentPerRole;
 
 
 	public Principal() {
 		// TODO: di serialNumber che ci metto?
 		serialNumber = "Guest";
 		permissionsPerRole = new HashMap<>();
 		departmentPerRole = new HashMap<>();
 
 		stringifyRole(new SystemRole(RolePermission.GUEST));
 	}
 
 
 	// public Principal(String name, String surname, String email, String
 	// serialNumber, RolePermission role, List<String> depthsCodes) {
 	// super();
 	// this.name = name;
 	// this.surname = surname;
 	// this.email = email;
 	// this.serialNumber = serialNumber;
 	// this.rolePermission = role;
 	// this.belongingDepthsCodes = depthsCodes;
 	// setPermissions(role.getPermissions());
 	//
 	// }
 
 	public Principal(User u) {
 		super();
 		this.name = u.getName();
 		this.surname = u.getSurname();
 		this.email = u.getEmail();
 		this.serialNumber = u.getSerialNumber();
 		
 		this.permissionsPerRole = new HashMap<>();
 		this.departmentPerRole = new HashMap<>();
 
 		for (Role role : u.getRoles()) {
 			stringifyRole(role);
 		}
 
 	}
 
 
 	public String getName() {
 		return name;
 	}
 
 
 	public String getSurname() {
 		return surname;
 	}
 
 
 	public String getEmail() {
 		return email;
 	}
 
 
 	public String getSerialNumber() {
 		return serialNumber;
 	}
 
 
 	public List<String> getBelongingDepthsCodes() {
 		return new ArrayList<>(departmentPerRole.values());
 	}
 
 
 	public boolean hasPermission(Permission toCheck) {
 		System.out.println(">>>>>>>>>>> Principal:\npermission to check: " + toCheck);
 		System.out.println("owned permissions: " + permissionsPerRole.values());
 		
 		String permissionString = toCheck.toString();
 		for(List<String> p : permissionsPerRole.values()){
 			if(p.contains(permissionString)){
 				System.out.println("OK\n<<<<<<<<<<<");
 				return true;
 			}
 		}
 		
 		System.out.println("NO\n<<<<<<<<<<<");
 		return false;
 	}
 
 
 	public boolean hasRolePermission(RolePermission toCheck) {
 		return hasRolePermission(toCheck.toString());
 	}
 
 
	private boolean hasRolePermission(String toCheck) {
 		System.out.println(">>>>>>>>>>> Principal:\n role to check: " + toCheck);
 		System.out.println("owned roles: " + permissionsPerRole.keySet());
 		System.out.println("<<<<<<<<<<<");
 		
 		return permissionsPerRole.keySet().contains(toCheck);
 	}
 
 
 	private void stringifyRole(Role role) {
 		// Lo so, è un nome bellissimo!
 
 		_addPermissionFromeRole(role);
 
 		if (role instanceof BusinessRole) {
 			_addDepartmentFromRole((BusinessRole) role);
 		}
 
 	}
 
 
 	private void _addPermissionFromeRole(Role role) {
 		List<String> permissionsToAdd = new ArrayList<>();
 
 		for (Permission p : role.getPermissions()) {
 			permissionsToAdd.add(p.toString());
 		}
 
 		permissionsPerRole.put(role.getRolePermission().toString(), permissionsToAdd);
 	}
 
 
 	private void _addDepartmentFromRole(BusinessRole role) {
 		departmentPerRole.put(role.getRolePermission().toString(), role.getDepartment().toString());
 	}
 
 }
