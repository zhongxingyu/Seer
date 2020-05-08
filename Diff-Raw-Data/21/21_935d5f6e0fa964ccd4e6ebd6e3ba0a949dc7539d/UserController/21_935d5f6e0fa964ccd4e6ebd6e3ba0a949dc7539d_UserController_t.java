 package de.skuzzle.polly.core.internal.httpv2;
 
 import java.util.List;
 import java.util.Map;
 
 import de.skuzzle.polly.http.annotations.Get;
 import de.skuzzle.polly.http.annotations.OnRegister;
 import de.skuzzle.polly.http.annotations.Param;
 import de.skuzzle.polly.http.annotations.Post;
 import de.skuzzle.polly.http.api.AlternativeAnswerException;
 import de.skuzzle.polly.http.api.Controller;
 import de.skuzzle.polly.http.api.answers.HttpAnswer;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.PersistenceManagerV2.Atomic;
 import de.skuzzle.polly.sdk.PersistenceManagerV2.Write;
 import de.skuzzle.polly.sdk.User;
 import de.skuzzle.polly.sdk.UserManager;
 import de.skuzzle.polly.sdk.exceptions.ConstraintException;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.InvalidUserNameException;
 import de.skuzzle.polly.sdk.exceptions.RoleException;
 import de.skuzzle.polly.sdk.exceptions.UnknownUserException;
 import de.skuzzle.polly.sdk.exceptions.UserExistsException;
 import de.skuzzle.polly.sdk.httpv2.GsonHttpAnswer;
 import de.skuzzle.polly.sdk.httpv2.PollyController;
 import de.skuzzle.polly.sdk.httpv2.SuccessResult;
 import de.skuzzle.polly.sdk.httpv2.WebinterfaceManager;
 import de.skuzzle.polly.sdk.httpv2.html.HTMLTable;
 import de.skuzzle.polly.sdk.roles.RoleManager;
 
 
 public class UserController extends PollyController {
     
     
     public static void createUserTable(MyPolly myPolly) {
         final HTMLTable<User> table = new HTMLTable<>("userList", 
             new UserDataSource(myPolly.users()), myPolly);
         
         table.getBaseContext().put("myPolly", myPolly);
         myPolly.webInterface().getServer().addHttpEventHandler("/api/allUsers", table);
     }
 
     public UserController(MyPolly myPolly) {
         super(myPolly);
     }
 
     
     
     @Override
     protected Controller createInstance() {
         return new UserController(this.getMyPolly());
     }
     
     
     
     @Get(value = "pages/users", name = "User Manager")
     @OnRegister({
         WebinterfaceManager.ADD_MENU_ENTRY,
         "Admin",
         "List, modify, add and delete polly user accounts.",
         RoleManager.ADMIN_PERMISSION
     })
     public HttpAnswer users() throws AlternativeAnswerException {
         this.requirePermissions(RoleManager.ADMIN_PERMISSION);
         final Map<String, Object> c = this.createContext("templatesv2/users.html");
         c.put("users", this.getMyPolly().users().getRegisteredUsers());
         return this.makeAnswer(c);
     }
     
     
     
     @Get("/pages/tableTest")
     public HttpAnswer tableTest() {
         return this.makeAnswer(this.createContext("templatesv2/table_sample.html"));
     }
     
     
     
     
     @Get("pages/editUser")
     public HttpAnswer editUser(@Param("userId") int id) 
             throws AlternativeAnswerException {
         
         final User user = this.getMyPolly().users().getUser(id);
         
         if (user == null) {
             // TODO: 
         }
         
         if (this.getSessionUser() != user) {
             this.requirePermissions(RoleManager.ADMIN_PERMISSION);
         }
         
         final Map<String, Object> c = this.createContext("templatesv2/edit.user.html");
         c.put("editUser", user);
         return this.makeAnswer(c);
     }
 
     
     
     @Get("/api/deleteUser")
     public HttpAnswer deleteUser(@Param("id") int id) throws AlternativeAnswerException {
         this.requirePermissions(RoleManager.ADMIN_PERMISSION);
         
         final UserManager um = this.getMyPolly().users();
         final User user = um.getUser(id);
      
         if (user == null) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, "No user with id '" + id +"'"));
         }
         
         try {
             um.deleteUser(user);
         } catch (UnknownUserException e) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, "Unknown user"));
         } catch (DatabaseException e) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, "Database exception while deleting user"));
         }
         return new GsonHttpAnswer(200, 
             new SuccessResult(true, user.getName() + " has been deleted"));
     }
     
     
     
     /*@Get("/api/listUsers")
     public HttpAnswer listUsers() {
         if (!this.getMyPolly().roles().hasPermission(
             this.getSessionUser(), RoleManager.ADMIN_PERMISSION)) {
         
             return HttpAnswers.newStringAnswer("Required permission: " + 
                 RoleManager.ADMIN_PERMISSION);
         }
         final Map<String, Object> c = this.createContext("");
         c.put("users", this.getMyPolly().users().getRegisteredUsers());
         return HttpAnswers.newTemplateAnswer("templatesv2/users.list.html", c);
     }*/
     
     
     
     @Post("api/setPassword")
     public HttpAnswer setPassword(
         @Param("userId") int userId,
         @Param("newPassword") final String newPassword, 
         @Param("retype") String retype) throws DatabaseException, AlternativeAnswerException {
         
         final User user = this.getSessionUser();
         if (user == null || user.getId() != userId) {
             // session has no id OR session user is not the edited user:
             // then you need admin permissions to edit the attributes
             this.requirePermissions(RoleManager.ADMIN_PERMISSION);
         }
         
         final User target = this.getMyPolly().users().getUser(userId);
         if (target == null) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, "User does not exist"));
         } else if (!newPassword.equals(retype)) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, "Password and retype mismatch"));
         } else {
             this.getMyPolly().persistence().writeAtomic(new Atomic() {
                 
                 @Override
                 public void perform(Write write) {
                     target.setPassword(newPassword);
                 }
             });
             return new GsonHttpAnswer(200, new SuccessResult(true, "Password changed"));
         }
     }
     
     
     
     
     @Post("/api/addUser")
     public HttpAnswer addUser(
         @Param("newName") String name, 
         @Param("newPassword") String password, 
         @Param(value = "initialRoles", typeHint = String.class) List<String> roles) 
                 throws AlternativeAnswerException {
         
         this.requirePermissions(RoleManager.ADMIN_PERMISSION);
         
         final UserManager um = this.getMyPolly().users();
         final RoleManager rm = this.getMyPolly().roles();
             
         try {
             final User newUser = um.addUser(name, password);
             for (final String roleName : roles) {
                 rm.assignRole(newUser, roleName);
             }
         } catch (InvalidUserNameException e) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, "Username has invalid format"));
         } catch (UserExistsException e) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, "Username already exists"));
         } catch (DatabaseException e) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, "Database error while storing new user"));
         } catch (RoleException e) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, "Error while assigning initial roles"));
         }
         return new GsonHttpAnswer(200, 
             new SuccessResult(true, "User '" + name + "' added."));
     }
     
     
     
     public final static class SetAttributeResult extends SuccessResult {
         public String newValue;
 
         public SetAttributeResult(boolean success, String msg, String newValue) {
             super(success, msg);
             this.newValue = newValue;
         }
     }
     
     
    
     @Get("api/setAttribute")
     public HttpAnswer setAttribute(
         @Param("userId") int userId,
         @Param("attribute") String attribute, 
         @Param(value = "value", treatEmpty = true, ifEmptyValue = "") String value) 
             throws AlternativeAnswerException {
         
         final User user = this.getSessionUser();
        final User target = this.getMyPolly().users().getUser(userId);
        if (user == null || user != target) {
             // session has no id OR session user is not the edited user:
             // then you need admin permissions to edit the attributes
             this.requirePermissions(RoleManager.ADMIN_PERMISSION);
         }
         
         if (target == null) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, "User does not exist"));
         }
         try {
             final String newValue = this.getMyPolly().users().setAttributeFor(
                 user, target, attribute, value);
             
             return new GsonHttpAnswer(200, 
                new SetAttributeResult(true, "", newValue));            
         } catch (DatabaseException e) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, 
                     "Error while persisting new attribute value"));
         } catch (ConstraintException e) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, 
                     value + " is not a valid value for " + attribute));
         }
     }
     
     
     
     @Get("api/addRole")
     public HttpAnswer addRole(
         @Param("userId") int userId, 
         @Param("role") String role) throws AlternativeAnswerException {
 
         this.requirePermissions(RoleManager.ADMIN_PERMISSION);
         
         final User target = this.getMyPolly().users().getUser(userId);
         
         if (target == null) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, "User does not exist"));
         } else {
             try {
                 this.getMyPolly().roles().assignRole(target, role);
                 return new GsonHttpAnswer(200, new SuccessResult(true, 
                     "Role has been added"));
             } catch (RoleException e) {
                 return new GsonHttpAnswer(200, 
                     new SuccessResult(false, e.getMessage()));
             } catch (DatabaseException e) {
                 return new GsonHttpAnswer(200, new SuccessResult(false, 
                     "Database error while assigning role to user"));
             }
         }
     }
     
     
     
     @Get("api/removeRole")
     public HttpAnswer removeRole(
         @Param("userId") int userId, 
         @Param("role") String role) throws AlternativeAnswerException {
 
         this.requirePermissions(RoleManager.ADMIN_PERMISSION);
         final User target = this.getMyPolly().users().getUser(userId);
         
         if (target == null) {
             return new GsonHttpAnswer(200, 
                 new SuccessResult(false, "User does not exist"));
         } else {
             try {
                 if (target.isPollyAdmin() && role.equals(RoleManager.ADMIN_ROLE)) {
                     return new GsonHttpAnswer(200, new SuccessResult(false, 
                         "Can not remove admin role from polly main admin"));
                 }
                 this.getMyPolly().roles().removeRole(target, role);
                 return new GsonHttpAnswer(200, new SuccessResult(true, 
                     "Role has been removed"));
             } catch (RoleException e) {
                 return new GsonHttpAnswer(200, 
                     new SuccessResult(false, e.getMessage()));
             } catch (DatabaseException e) {
                 return new GsonHttpAnswer(200, new SuccessResult(false, 
                     "Database error while assigning role to user"));
             }
         }
     }
 }
