 package com.financial.pyramid.service;
 
 import com.financial.pyramid.domain.User;
 import com.financial.pyramid.service.beans.AccountDetails;
 import com.financial.pyramid.service.exception.UserConfirmOverdueException;
 import com.financial.pyramid.service.exception.UserNotFoundException;
 import com.financial.pyramid.web.tree.BinaryTree;
 import com.financial.pyramid.web.form.QueryForm;
 import com.financial.pyramid.web.form.RegistrationForm;
 import org.springframework.security.core.userdetails.UserDetailsService;
 
 import java.util.List;
 
 /**
  * User: Danil
  * Date: 29.05.13
  * Time: 22:15
  */
 public interface UserService extends UserDetailsService {
 
     public void save(User user);
 
     public void delete(Long id);
 
     public User findById(Long id);
 
     public boolean checkEmail(String Email);
 
     public List<User> list();
 
     public List<User> findByQuery(QueryForm form);
 
     public void update(RegistrationForm form);
 
     public User findByEmail(String email);
 
     public BinaryTree getBinaryTree(User user);
 
     public List<User> findUsersByOwner(Long ownerId);
 
     public String createPassword(int n);
 
     public AccountDetails getAccountDetails(User user);
 }
