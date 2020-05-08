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
 
 /** Test the ViewUserCommand class.
  */
 public class SaveUserCommandTest {
 
   /** The command to be tested, initialized to create users (with profile and
    * password).
    */
   private SaveUserCommand saveUserCommand;
 
   /** The command to be tested (with password only).
    */
   private SaveUserCommand changePasswordCommand;
 
   /** The user repository.
    */
   private UserRepository userRepository;
 
   /** The role repository.
    */
   private RoleRepository roleRepository;
 
   /** This is a set up method of this TestCase.
    */
   @Before
   public final void setUp() {
     SpringTestUtils.beginTransaction();
     userRepository = (UserRepository) SpringTestUtils.getBean(
         "user.userRepository");
     roleRepository = (RoleRepository) SpringTestUtils.getBean(
         "coreuser.roleRepository");
     saveUserCommand = (SaveUserCommand) SpringTestUtils.getServletBean(
         "editUserCommand");
     changePasswordCommand = (SaveUserCommand) SpringTestUtils.getServletBean(
         "passwordCommand");
     setUpUserRepository();
   }
 
   /** Removes the unneeded users.
    */
   private void setUpUserRepository() {
     for (User user : userRepository.getUsers(new UserFilter())) {
       userRepository.remove(user);
     }
 
     for (Role role : roleRepository.getRoles()) {
       roleRepository.remove(role);
     }
 
     // Add new roles in the user repository.
     roleRepository.save(new Role("member"));
     roleRepository.save(new Role("moderator"));
     roleRepository.save(new Role("owner"));
     roleRepository.save(new Role("ADMINISTRATOR"));
     assertThat(roleRepository.getRoles().size(), is(4));
 
     // Add a user.
     User user = new User("admin", "admin@none");
     user.changePassword("admin");
     user.addRole(roleRepository.getRoles().get(3));
     userRepository.save(user);
 
     user = new User("UserTest", "UserTest@none");
     user.changePassword("pass");
     user.addRole(roleRepository.getRoles().get(1));
     user.addRole(roleRepository.getRoles().get(2));
     user.addRole(roleRepository.getRoles().get(3));
     userRepository.save(user);
 
     User admin = userRepository.findUserByName("admin");
     assertThat(admin.isAdministrator(), is(true));
     // Sets admin as the user performing the actions.
     SecurityTestUtils.setContextUser(admin);
   }
 
   /** Returns an array of roles ids.
    *
    * @param roles List of roles.
    *
    * @return an array of roles ids.
    */
   private List<String> getRolesId(final Collection<Role> roles) {
     List<String> roleIds = new ArrayList<String>();
     for (Role role : roles) {
       roleIds.add(String.valueOf(role.getId()));
     }
     return roleIds;
   }
 
   /** Test Execute. Edit user with same roles.
    */
   @Test
   public final void testExecute_editUserName() {
     //  Add a user.
     User user = new User("newUser", "newUser@none");
     userRepository.save(user);
 
     user = userRepository.findUserByName("newUser");
     String newName = "NewName";
     saveUserCommand.setUserId(user.getId());
     saveUserCommand.getProfile().setName(newName);
     saveUserCommand.getProfile().setEmail(user.getEmail());
     saveUserCommand.getProfile().setRoleIds(getRolesId(user.getRoles()));
     saveUserCommand.execute();
 
     user = userRepository.findUserByName(newName);
     assertThat(user, notNullValue());
   }
 
   /** Test Execute. Edit user with sames roles.
    */
   @Test
   public final void testExecute_editUserSameRoles() {
     User user = userRepository.findUserByName("UserTest");
     int roles = user.getRoles().size();
     saveUserCommand.setUserId(user.getId());
     saveUserCommand.getProfile().setName(user.getName());
     saveUserCommand.getProfile().setEmail(user.getEmail());
 
     // Sets the sames roles.
     saveUserCommand.getProfile().setRoleIds(getRolesId(user.getRoles()));
     saveUserCommand.execute();
 
     // Verify the results.
     user = userRepository.findUserByName("UserTest");
     assertThat(user, notNullValue());
     assertThat(user.getRoles().size(), is(roles));
   }
 
   /** Test Execute. Edit user with new roles.
    */
   @Test
   public final void testExecute_editUserNewRoles() {
     User user = userRepository.findUserByName("UserTest");
     saveUserCommand.setUserId(user.getId());
     saveUserCommand.getProfile().setName(user.getName());
     saveUserCommand.getProfile().setEmail(user.getEmail());
 
     // Sets new roles.
     saveUserCommand.getProfile().setRoleIds(getRolesId(
           roleRepository.getRoles()));
     saveUserCommand.execute();
 
     // Verify the results.
     user = userRepository.findUserByName("UserTest");
     assertThat(user, notNullValue());
     assertThat(roleRepository.getRoles().size(), is(user.getRoles().size()));
   }
 
   /** Test Execute. Edit user with one of existing roles.
    */
   @Test
   public final void testExecute_editUserOneExistingRole() {
     User user = userRepository.findUserByName("UserTest");
     saveUserCommand.setUserId(user.getId());
     saveUserCommand.getProfile().setName(user.getName());
     saveUserCommand.getProfile().setEmail(user.getEmail());
 
     // Sets only one of the existing roles.
     List<Role> roles = new ArrayList<Role>();
     roles.add(user.getRoles().iterator().next());
     saveUserCommand.getProfile().setRoleIds(getRolesId(roles));
     saveUserCommand.execute();
 
     // Verify the results.
     user = userRepository.findUserByName("UserTest");
     assertThat(user, notNullValue());
     assertThat(user.getRoles().size(), is(1));
   }
 
   /** Test Execute. Edit user with one of existing roles.
    */
   @Test
   public final void testExecute_editUserOneExistingAndNewRole() {
     User user = userRepository.findUserByName("UserTest");
     saveUserCommand.setUserId(user.getId());
     saveUserCommand.getProfile().setName(user.getName());
     saveUserCommand.getProfile().setEmail(user.getEmail());
 
     // Sets a new role one of the existing roles.
     List<Role> roles = new ArrayList<Role>();
     roles.add(user.getRoles().iterator().next());
     roles.add(roleRepository.getRoles().get(0));
     saveUserCommand.getProfile().setRoleIds(getRolesId(roles));
     saveUserCommand.execute();
 
     // Verify the results.
     user = userRepository.findUserByName("UserTest");
     assertThat(user, notNullValue());
     assertThat(user.getRoles().size(), is(2));
   }
 
   /** Tests execute for a new user.
    */
   @Test
   public final void testExecute_newUser() {
     saveUserCommand.getProfile().setName("TestUserName");
     saveUserCommand.getProfile().setEmail("mail@mail.com");
     saveUserCommand.getProfile().setRoleIds(getRolesId(
           roleRepository.getRoles()));
     saveUserCommand.execute();
 
     User user = userRepository.findUserByName("TestUserName");
     assertThat(user, notNullValue());
     assertThat(roleRepository.getRoles().size(), is(user.getRoles().size()));
   }
 
   /** Test change password.
    */
   @Test
   public final void testExecute_changePassword() {
 
     // changePasswordCommand
 
     User user = userRepository.findUserByName("UserTest");
 
     changePasswordCommand.setUserId(user.getId());
     changePasswordCommand.getPassword().setOldPassword("pass");
     changePasswordCommand.getPassword().setNewPassword("newPassword");
     changePasswordCommand.getPassword().setConfirmedPassword("newPassword");
     changePasswordCommand.execute();
 
     user = userRepository.findUserByName("UserTest");
     assertThat(user, notNullValue());
     assertThat(user.validatePassword("newPassword"), is(true));
   }
 
   /** Tests that the NewUserValidator correctly validates a valid user.
    */
   @Test
   public final void testValidate_success() throws Exception {
 
     SaveUserCommand user = (SaveUserCommand) SpringTestUtils.getServletBean(
         "createUserCommand");
     user.getProfile().setName("user01");
     user.getProfile().setEmail("user01@hotmail.com");
     user.getPassword().setNewPassword("123456");
     user.getPassword().setConfirmedPassword("123456");
     List<String> roleIds = new ArrayList<String>();
     roleIds.add("1");
     user.getProfile().setRoleIds(roleIds);
 
     Errors errors = new BindException(user, user.getClass().getName());
     user.validate(errors);
     assertThat(errors.hasErrors(), is(false));
   }
 
   /** Tests that the NewUserValidator correctly fails an invalid user.
    */
   @Test
   public final void testValidate_error() throws Exception {
 
     //Fails because it has an empty name.
     SaveUserCommand user = (SaveUserCommand) SpringTestUtils.getServletBean(
         "createUserCommand");
     user.getProfile().setName("");
     user.getProfile().setEmail("user02@hotmail.com");
     user.getPassword().setNewPassword("123456");
     user.getPassword().setConfirmedPassword("123456");
     List<String> roleIds = new ArrayList<String>();
     roleIds.add("1");
     user.getProfile().setRoleIds(roleIds);
 
     Errors errors = new BindException(user, user.getClass().getName());
     user.validate(errors);
     assertThat(errors.getAllErrors().size(), is(1));
 
     /* Fails because it has an empty email and the password lenght is
      * less than 6.
      */
     user.getProfile().setName("User01");
     user.getProfile().setEmail("");
     user.getPassword().setNewPassword("12345");
     user.getPassword().setConfirmedPassword("12345");
 
     errors = new BindException(user, user.getClass().getName());
     user.validate(errors);
     assertThat(errors.getAllErrors().size(), is(2));
   }
 
   /** Test change password validation.
    */
   @Test
   public final void testValidate_changePassword() {
 
     User user = userRepository.findUserByName("UserTest");
 
     changePasswordCommand.setUserId(user.getId());
     changePasswordCommand.getPassword().setOldPassword("passWrong");
     changePasswordCommand.getPassword().setNewPassword("newPasswordWrong");
     changePasswordCommand.getPassword().setConfirmedPassword("newPassword");
     changePasswordCommand.init();
 
     Errors errors = new BindException(changePasswordCommand,
         changePasswordCommand.getClass().getName());
     changePasswordCommand.validate(errors);
 
     assertThat(errors.getAllErrors().size(), is(2));
   }
 
   /* Tests if non admin users cannot create other users.
    */
   @Test
   public final void testExecute_createNoPrivileges() throws Exception {
 
     User userTest = userRepository.findUserByName("UserTest");
     SecurityTestUtils.setContextUser(userTest);
 
     try {
       saveUserCommand.execute();
       fail("Non admin user can create new users");
     } catch (Exception e) {
     }
   }
 
   /* Tests if non admin users cannot modify other users.
    */
   @Test
   public final void testExecute_modifyNoPrivileges() throws Exception {
 
     User userTest = userRepository.findUserByName("UserTest");
     SecurityTestUtils.setContextUser(userTest);
 
     User admin = userRepository.findUserByName("admin");
     saveUserCommand.setUserId(admin.getId());
 
     try {
       saveUserCommand.execute();
       fail("Non admin user can modify other users");
     } catch (Exception e) {
     }
   }
 
   /* Tests if non admin users can modify themselves.
    */
   @Test
   public final void testExecute_modifyMyself() throws Exception {
 
     User userTest = userRepository.findUserByName("UserTest");
     SecurityTestUtils.setContextUser(userTest);
 
     saveUserCommand.setUserId(userTest.getId());
     saveUserCommand.init();
     saveUserCommand.getProfile().setName("Changed name");
 
     saveUserCommand.execute();
 
     userTest = userRepository.findUserByName("Changed name");
     assertThat(userTest, notNullValue());
   }
 }
 
