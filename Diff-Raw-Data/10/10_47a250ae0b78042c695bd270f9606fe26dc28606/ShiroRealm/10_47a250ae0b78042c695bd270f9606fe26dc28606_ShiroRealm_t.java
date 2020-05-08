 package cz.muni.fi.pompe.crental.security;
 
 import cz.muni.fi.pompe.crental.dao.DAOEmployee;
 import cz.muni.fi.pompe.crental.dto.AccessRight;
 import cz.muni.fi.pompe.crental.entity.Employee;
 import java.util.Arrays;
 import java.util.List;
 import org.apache.shiro.authc.AccountException;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.AuthenticationInfo;
 import org.apache.shiro.authc.AuthenticationToken;
 import org.apache.shiro.authc.SimpleAuthenticationInfo;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.authz.AuthorizationInfo;
 import org.apache.shiro.authz.SimpleAuthorizationInfo;
 import org.apache.shiro.realm.AuthorizingRealm;
 import org.apache.shiro.subject.PrincipalCollection;
 
 /**
  *
  * @author Patrik Pompe <325292@mail.muni.cz>
  */
 public class ShiroRealm extends AuthorizingRealm {
     private DAOEmployee daoEmployee;
 
     public void setDaoEmployee(DAOEmployee daoemployee) {
         daoEmployee = daoemployee;
     }
 
     @Override
     protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {
         SimpleAuthorizationInfo authInfo = new SimpleAuthorizationInfo();
         boolean admin;
         Long eID = null;
         Principals p = (Principals) pc.getPrimaryPrincipal();
                 
         if (p.getName().equals("rest")) {
             admin = true;
         } else {
             eID = p.getId();
             Employee employee = this.daoEmployee.getEmployeeById(eID);
             admin = employee.getAccessRight() == AccessRight.Admin;
         }
 
         if (admin) {
             authInfo.addRole("admin");
             authInfo.addStringPermission("*");
         } else {
             authInfo.addRole("employee");
 
             List<String> permissions = Arrays.asList(
                 "rent:getAllOfEmployee:" + eID,
                 "request:delete:" + eID,
                 "request:update:" + eID,
                 "request:get:" + eID,
                 "employee:update:" + eID,
                 "employee:get:" + eID,
                 "employee:get:" + eID
             );
             
             authInfo.addStringPermissions(permissions);
         }
 
         return authInfo;
     }
 
     @Override
     protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken at) throws AuthenticationException {
         UsernamePasswordToken upToken = (UsernamePasswordToken) at;
        String restCrentals = "rest";
         
        if (upToken.getUsername().equals(restCrentals) && new String(upToken.getPassword()).equals(restCrentals)) {
            return new SimpleAuthenticationInfo(new Principals(Long.MAX_VALUE, restCrentals), restCrentals, this.getName());
         } else {
             Employee employee = daoEmployee.getEmployeeByName(upToken.getUsername());
             
             if(employee != null && employee.getPassword().equals(new String(upToken.getPassword()))){     
                 return new SimpleAuthenticationInfo(new Principals(employee.getId(), employee.getName()), employee.getPassword(), this.getName());
             }
 
         }
         throw new AccountException("Authentication failed." + upToken.getUsername() + " xxx "+ new String(upToken.getPassword()));
     }
 }
