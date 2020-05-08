 package com.checkers.server.services;
 
 import com.checkers.server.Consts;
 import com.checkers.server.Context;
 import com.checkers.server.beans.User;
 import com.checkers.server.dao.UserDao;
 import com.checkers.server.exceptions.LogicException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Service;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 /**
  *
  *
  * @author Pavel Kuchin
  */
 @Service("userService")
 public class UserServiceImpl implements UserService {
 
     @Autowired
     private UserDao userDao;
 
     @PreAuthorize("hasRole('ROLE_ADMIN')")
     @Override
     public void newUser(User user) throws LogicException {
 
         if(user.getLogin() == null){
             throw new LogicException(6L, "Login field should be filled");
         } else if(user.getPassword() == null){
             throw new LogicException(6L, "Password field should be filled");
         } else if(user.getEmail() == null){
             throw new LogicException(6L, "Email field should be filled");
         }
 
         user.setUuid(null);
         user.setCreated(new Date());
         user.setModified(new Date());
         user.setLastLogin(new Date());
 
         userDao.newUser(user);
     }
 
     @Override
     public void regUser(User user) throws LogicException {
 
         if(user.getLogin() == null){
             throw new LogicException(6L, "Login field should be filled");
         } else if(user.getPassword() == null){
             throw new LogicException(6L, "Password field should be filled");
         } else if(user.getEmail() == null){
             throw new LogicException(6L, "Email field should be filled");
         }
 
         // Fields autofill
         user.setUuid(null);
 
         //TODO User should be enabled only after email verification
         user.setEnabled(true);
 
         user.setRole("ROLE_USER");
 
         user.setCreated(new Date());
         user.setModified(new Date());
         user.setLastLogin(new Date());
 
         userDao.newUser(user);
     }
 
     @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_USER')")
     @Override
     public User getUser(Long uuid) throws LogicException {
         if(uuid == null){
             uuid = userDao.getUserByLogin(Context.getAuthLogin()).getUuid();
         }
 
         User user = userDao.getUser(uuid);
 
             return user;
     }
 
     @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_USER')")
     @Override
     public User getUserByLogin(String login) throws LogicException {
         return userDao.getUserByLogin(login);
     }
 
     @PreAuthorize("hasRole('ROLE_ADMIN')")
     @Override
     public void delUser(Long uuid) throws LogicException {
         userDao.delUser(uuid);
     }
 
     @PreAuthorize("hasAnyRole('ROLE_ADMIN,ROLE_USER')")
     @Override
     public User modUser(Long uuid, User user) throws LogicException {
         User authUser = userDao.getUserByLogin(Context.getAuthLogin());
 
         if(uuid == null){
             uuid = authUser.getUuid();
         }
 
         User origin = userDao.getUser(uuid);
 
         Collection<SimpleGrantedAuthority> authorities =
                 (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
 
         if(authorities.contains(Consts.ROLE_ADMIN)){
             if(user.getPassword() != null){
                 origin.setPassword(user.getPassword());
             }
 
             if(user.getEnabled() != null){
                 origin.setEnabled(user.getEnabled());
             }
 
             if(user.getRole() != null){
                 origin.setRole(user.getRole());
             }
 
            if(user.getLogin() != null){
                origin.setLogin(user.getLogin());
            }
             if(user.getFirstName() != null){
                 origin.setFirstName(user.getFirstName());
             }
             if(user.getLastName() != null){
                 origin.setLastName(user.getLastName());
             }
             if(user.getEmail() != null){
                 origin.setEmail(user.getEmail());
             }
 
         } else if(authorities.contains(Consts.ROLE_USER)){
             if(!authUser.getUuid().equals(origin.getUuid())){
                 throw new LogicException(7L, "As User you can't modify other users");
             }
 
             //TODO password should be changed in other request with old password verification
             if(user.getPassword() != null){
                 origin.setPassword(user.getPassword());
             }
 
             if(user.getFirstName() != null){
                 origin.setFirstName(user.getFirstName());
             }
             if(user.getLastName() != null){
                 origin.setLastName(user.getLastName());
             }
 
             //TODO email should be changed in other request with email verification
             if(user.getEmail() != null){
                 origin.setEmail(user.getEmail());
             }
 
         }
 
         origin.setModified(new Date());
 
         userDao.modUser(origin);
 
         return userDao.getUser(uuid);
     }
 
     @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
     @Override
     public List<User> getUsers() {
         return userDao.getUsers();
     }
 }
