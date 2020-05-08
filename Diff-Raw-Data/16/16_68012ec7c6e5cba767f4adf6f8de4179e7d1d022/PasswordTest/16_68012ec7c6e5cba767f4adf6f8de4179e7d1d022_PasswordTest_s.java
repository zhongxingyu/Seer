 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.user.application;
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

 import org.junit.Test;
import org.junit.Before;
 
 import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
 import static org.hamcrest.CoreMatchers.*;
 
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 
import com.globant.katari.user.SecurityTestUtils;
import com.globant.katari.hibernate.coreuser.domain.Role;
import com.globant.katari.hibernate.coreuser.domain.RoleRepository;
import com.globant.katari.user.SpringTestUtils;
 import com.globant.katari.user.domain.User;
import com.globant.katari.user.domain.UserFilter;
import com.globant.katari.user.domain.UserRepository;
 
 public class PasswordTest {
 
   @Test
   public void validate_successNewUser() {
     Password password = new Password();
 
     password.setNewPassword("new pass");
     password.setConfirmedPassword("new pass");
 
     Errors errors = new BindException(password, password.getClass().getName());
 
     password.validate(null, errors);
     assertThat(errors.hasErrors(), is(false));
   }
 
   @Test
   public void validate_successWithOldPassword() {
     Password password = new Password();
 
     password.setOldPassword("old pass");
     password.setNewPassword("new pass");
     password.setConfirmedPassword("new pass");
 
     Errors errors = new BindException(password, password.getClass().getName());
 
     User user = new User("name", "email");
     user.changePassword("old pass");
 
     password.validate(user, errors);
     assertThat(errors.hasErrors(), is(false));
   }
 
   @Test
   public void validate_failDontMatch() {
     Password password = new Password();
 
     password.setNewPassword("new pass");
     password.setConfirmedPassword("new pass 1");
 
     Errors errors = new BindException(password, password.getClass().getName());
 
     password.validate(null, errors);
     assertThat(errors.hasErrors(), is(true));
   }
 
   @Test
   public void validate_failMissingOriginal() {
     Password password = new Password();
 
     password.setNewPassword("new pass");
     password.setConfirmedPassword("new pass 1");
 
     Errors errors = new BindException(password, password.getClass().getName());
 
     User user = new User("name", "email");
 
     password.validate(user, errors);
     assertThat(errors.hasErrors(), is(true));
   }
 }
 
