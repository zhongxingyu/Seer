 package org.minispm.security.service;
 
 import com.google.common.base.Objects;
 import org.apache.shiro.authc.*;
 import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
 import org.apache.shiro.authz.AuthorizationInfo;
 import org.apache.shiro.authz.SimpleAuthorizationInfo;
 import org.apache.shiro.realm.AuthorizingRealm;
 import org.apache.shiro.subject.PrincipalCollection;
 import org.apache.shiro.util.ByteSource;
 import org.minispm.core.utils.Encodes;
 import org.minispm.security.entity.User;
 import org.minispm.admin.organization.entity.Staff;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.annotation.PostConstruct;
 import java.io.Serializable;
 
 /**
  * User: shibaoxu
  * Date: 13-1-2
  * Time: 下午5:44
  */
 public class ShiroDbRealm extends AuthorizingRealm {
     private UserService accountService;
     @Override
     protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
         UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
         User user = accountService.findByJobNumber(token.getUsername());
         if (user != null) {
             byte[] salt = Encodes.decodeHex(user.getSalt());
             return new SimpleAuthenticationInfo(new ShiroUser(user.getId(), user.getJobNumber(), user.getName(), user.getStaff()),
                     user.getPassword(), ByteSource.Util.bytes(salt), getName());
         } else {
             return null;
         }
     }
 
     @Override
     protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
         ShiroUser shiroUser = (ShiroUser) principals.getPrimaryPrincipal();
 //        User user = accountService.findByJobNumber(shiroUser.jobNumber);
         SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
         info.addRoles(accountService.getRolesByJobNumber(shiroUser.getJobNumber()));
         info.addStringPermissions(accountService.getPermissionsByUserId(shiroUser.getId()));
         return info;
     }
     /**
      * 设定Password校验的Hash算法与迭代次数.
      */
     @PostConstruct
     public void initCredentialsMatcher() {
         HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(UserService.HASH_ALGORITHM);
         matcher.setHashIterations(UserService.HASH_INTERATIONS);
 
         setCredentialsMatcher(matcher);
     }
 
     public UserService getAccountService() {
         return accountService;
     }
 
     @Autowired
     public void setAccountService(UserService accountService) {
         this.accountService = accountService;
     }
 
     public static class ShiroUser implements Serializable {
         private static final long serialVersionUID = -1373760761780840081L;
         public String id;
         public String jobNumber;
         public String name;
         public Staff staff;
 
         public String getId() {
             return id;
         }
 
         public void setId(String id) {
             this.id = id;
         }
 
         public String getJobNumber() {
             return jobNumber;
         }
 
         public void setJobNumber(String jobNumber) {
             this.jobNumber = jobNumber;
         }
 
         public ShiroUser(String id, String jobNumber, String name, Staff staff) {
             this.id = id;
             this.jobNumber = jobNumber;
             this.name = name;
             this.staff = staff;
         }
 
         public String getName() {
             return name;
         }
 
         public Staff getStaff() {
             return staff;
         }
 
         public void setStaff(Staff staff) {
             this.staff = staff;
         }
 
         /**
          * 本函数输出将作为默认的<shiro:principal/>输出.
          */
         @Override
         public String toString() {
             return jobNumber;
         }
 
         /**
          * 重载hashCode,只计算jobNumber;
          */
         @Override
         public int hashCode() {
             return Objects.hashCode(jobNumber);
         }
 
         /**
          * 重载equals,只计算loginName;
          */
         @Override
         public boolean equals(Object obj) {
             if (this == obj)
                 return true;
             if (obj == null)
                 return false;
             if (getClass() != obj.getClass())
                 return false;
             ShiroUser other = (ShiroUser) obj;
             if (jobNumber == null) {
                 if (other.jobNumber != null)
                     return false;
             } else if (!jobNumber.equals(other.jobNumber))
                 return false;
             return true;
         }
     }
 }
