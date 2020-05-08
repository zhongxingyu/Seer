 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.user.application;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.Validate;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 
 import com.globant.katari.hibernate.coreuser.domain.Role;
 import com.globant.katari.hibernate.coreuser.domain.RoleRepository;
 import com.globant.katari.user.domain.User;
 import com.globant.katari.user.domain.UserRepository;
 
 /** The profile (user data except the password) handling part of the user
  * command.
  */
 public class Profile {
 
   /** The class logger.
    */
   private static Log log = LogFactory.getLog(Profile.class);
 
   /** The name of the user.
    */
   private String name;
 
   /** The email of the user.
    */
   private String email;
 
   /** The ids of the user's roles.
    */
   private List<String> roleIds;
 
   /**
    * The roles for the user.
    */
   private List<Role> roles;
 
   /** Returns the name of the user.
    *
    * @return the name.
    */
   public String getName() {
     return name;
   }
 
   /** Sets the user name.
    *
    * @param theName The user name. It cannot be null.
    */
   public void setName(final String theName) {
     Validate.notNull(theName, "The user name cannot be null");
     name = theName;
   }
 
   /** Returns the email of the user.
    *
    * @return the email address.
    */
   public String getEmail() {
     return email;
   }
 
   /** Sets the email.
    *
    * @param theEmail The email. It cannot be null.
    */
   public void setEmail(final String theEmail) {
     Validate.notNull(theEmail, "The email address cannot be null");
     email = theEmail;
   }
 
   /** Gets the ids of the user's roles.
    *
    * @return Returns the ids of the user's roles. It cannot be null.
    */
   public List<String> getRoleIds() {
     return roleIds;
   }
 
   /** Sets the ids of the user's roles.
    *
    * @param theRoleIds The roles id. If it is null, the user has no roles.
    */
   public void setRoleIds(final List<String> theRoleIds) {
     roleIds = theRoleIds;
   }
 
   /** Gets the roles of the user.
    *
    * @return The user roles, or null if the user has no roles..
    */
   public List<Role> getRoles() {
     return roles;
   }
 
   /**
    * Sets the roles for the usr.
    * @param theRoles
    *          the user roles, if it is null, the user has no roles.
    */
   public void setRoles(final List<Role> theRoles) {
     this.roles = theRoles;
   }
 
   /** Initializes the profile form the specified user.
    *
    * @param user The user loaded from the database. It cannot be null.
    */
   void init(final User user) {
     Validate.notNull(user, "The user cannot be null.");
 
     if (name == null) {
       setName(user.getName());
     }
     if (email == null) {
       setEmail(user.getEmail());
     }
     // TODO define if default roleIds state is null or empty
     if (roleIds == null) {
       List<String> rolesId = new ArrayList<String>();
       for (Role role : user.getRoles()) {
         rolesId.add(String.valueOf(role.getId()));
       }
       setRoleIds(rolesId);
     }
     if (roles == null) {
       roles = new ArrayList<Role>(user.getRoles());
     }
   }
 
   /** Validates the user profile, checking for not null name, email and non
    * duplicate user names.
    *
    * @param userRepository to load the user by name to check for duplicate user
    * name. It cannot be null.
    *
    * @param userId the user id to validate for duplicates user names. It is 0
    * for a newly created user.
    *
    * @param errors Contextual state about the validation process. It can not be
    * null.
    */
   public void validate(final UserRepository userRepository, final long userId,
       final Errors errors) {
     log.trace("Entering validate");
     Validate.notNull(userRepository, "The user repository cannot be null");
     ValidationUtils.rejectIfEmptyOrWhitespace(errors, "profile.name",
         "required");
     ValidationUtils.rejectIfEmptyOrWhitespace(errors, "profile.email",
         "required");
 
     // Checks if the user name is duplicated.
     User user = userRepository.findUserByName(name);
     if (null != user && userId != user.getId()) {
       errors.rejectValue("profile.name", "exist");
     }
     log.trace("Leaving validate");
   }
 
  /** If it is a new user, the user is created assigning name, email and roles.
    *
   * The password it changed too. For a preexistent user, the changes to the
   * previously loaded user are appied, given the name, email and roles.
    *
    * @param roleRepository to get the roles. It cannot be null.
    *
    * @param theUser The loaded user if preexistent or null if new user to be
    * created.
    *
    * @return the modified user.
    */
   public User apply(final RoleRepository roleRepository, final User theUser) {
     log.trace("Entering apply");
     Validate.notNull(roleRepository, "The role repository cannot be null");
     User user = theUser;
     if (user == null) {
     // New user
       user = new User(getName(), getEmail());
     } else {
       // Existing user.
       user.modify(getName(), getEmail());
       // Remove existing roles.
       user.getRoles().clear();
     }
     // Add the new roles.
     if (roleIds != null) {
       List<Role> newRoles = roleRepository.getRoles(roleIds);
       for (Role role : newRoles) {
         user.addRole(role);
       }
     }
     if (roles != null) {
       for (Role role : roles) {
         user.addRole(roleRepository.findRole(role.getId()));
       }
     }
     log.trace("Leaving apply");
     return user;
   }
 }
 
