 package org.motechproject.ghana.national.service;
 
 import org.motechproject.ghana.national.domain.UserType;
 import org.motechproject.ghana.national.repository.AllUserTypes;
 import org.motechproject.mrs.exception.UserAlreadyExistsException;
 import org.motechproject.mrs.model.User;
 import org.motechproject.mrs.services.MRSUserAdaptor;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 @Service
 public class UserService {
     private MRSUserAdaptor userAdaptor;
     private AllUserTypes allUserTypes;
 
     public UserService() {
     }
 
     @Autowired
     public UserService(AllUserTypes allUserTypes, MRSUserAdaptor userAdaptor) {
         this.allUserTypes = allUserTypes;
         this.userAdaptor = userAdaptor;
     }
 
     @ApiSession
     public HashMap saveUser(User user) throws UserAlreadyExistsException {
         return userAdaptor.saveUser(user);
     }
 
     public String changePasswordByEmailId(String emailId){
         String password ="";
         try{
             password = userAdaptor.setNewPasswordForUser(emailId);
        }catch(UsernameNotFoundException e){
             password = "";
         }
         return password;
     }
 
     public List<String> fetchAllRoles() {
         List<String> roles = new ArrayList<String>();
         for (UserType userType : allUserTypes.getAll()) roles.add(userType.name());
         return roles;
     }
 }
