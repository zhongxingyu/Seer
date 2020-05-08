 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pl.bitethebet.repository;
 
 import java.util.List;
 import org.springframework.stereotype.Repository;
 import pl.bitethebet.model.UserAccount;
 import pl.bitethebet.repository.common.CrudRepository;
 
 /**
  *
  * @author mrowkam
  */
 @Repository
 public class UserAccountRepository extends CrudRepository<UserAccount> {
 
     public UserAccount findByUsername(String username) {
         List<UserAccount> us = (List<UserAccount>) findBySingleParamQuery(UserAccount.class, "username == " + username);
        System.out.println(us.get(0).getUsername());
         return us.get(0);
     }
 }
