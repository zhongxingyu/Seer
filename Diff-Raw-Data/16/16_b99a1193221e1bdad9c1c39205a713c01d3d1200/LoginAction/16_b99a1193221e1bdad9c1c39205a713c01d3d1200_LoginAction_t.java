 package org.ai.action;
 
 import com.ai.model.Role;
 import com.ai.model.RolePermission;
 import com.ai.model.User;
 import com.ai.model.UserRole;
 import com.ai.service.RoleService;
 import com.ai.service.UserService;
 import com.ai.service.impl.RoleServiceImpl;
 import com.ai.service.impl.UserServiceImpl;
 import com.ai.util.HibernateUtil;
 import org.ai.util.Return;
 import org.hibernate.Session;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ishara
  * Date: 1/1/13
  * Time: 11:39 AM
  * To change this template use File | Settings | File Templates.
  */
 public class LoginAction {
     private String un;
     private String pw;
 
     ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
 
     UserService userService = (UserServiceImpl) applicationContext.getBean("userService");
     RoleService roleService = (RoleServiceImpl) applicationContext.getBean("roleService");
 
     //action
     public String execute() {
         if (un.equals("")) {
             return Return.SUCCESS.getReturnCode();
         } else {
            saveRecord(un,pw);
             return Return.HOME.getReturnCode();
         }
     }
 
     private void saveRecord(String useName, String password){
         User user = new User(null, useName, password);
 
         UserRole userRole = new UserRole(null, roleService.findRoleById(new Long(1)));
         userRole.setUser(user);
 
         RolePermission rolePermission = new RolePermission(null, "LOGIN");
         rolePermission.setRoleId(userRole);
 
         user.getUserRoles().add(userRole);
         userRole.getRolePermissions().add(rolePermission);
 
         userService.saveUser(user);
 
     }
 
     public String getUn() {
         return un;
     }
 
     public void setUn(String un) {
         this.un = un;
     }
 
     public String getPw() {
         return pw;
     }
 
     public void setPw(String pw) {
         this.pw = pw;
     }
 }
