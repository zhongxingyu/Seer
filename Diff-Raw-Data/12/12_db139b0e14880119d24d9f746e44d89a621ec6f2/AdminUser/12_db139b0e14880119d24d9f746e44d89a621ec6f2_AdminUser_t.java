 package controllers;
 
 import models.Page;
 import models.Role;
 import models.User;
 import play.Logger;
 import play.data.validation.*;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: reyoung
  * Date: 13-5-27
  * Time: 下午4:18
  * EMail: reyoung@126.com
  * Blog: www.reyoung.me
  */
 public class AdminUser extends BaseAdminController {
     @Check("User+R")
     public static void list(Integer page, Integer pageSize){
         if(page == null || page <1){
             page = 1;
         }
         if(pageSize == null || pageSize<1){
             pageSize = 10;
         }
         List<User> users = User.all().fetch(page,pageSize);
         Page<User> pages = new Page<User>(users,page,pageSize,User.count());
         render(pages);
     }
     @Check("User+W,Role+R")
     public static void create(){
         List<Role> roles =
                 Role.all().fetch();
         render(roles);
     }
 
 
     public static void edit(@Required Long id){
         User user = User.find("LoginName = ?", Security.connected()).first();
         if(!(user.getId().equals(id) || user.UserRole.getUserTablePrivilege().Writable&&
                 user.UserRole.getUserTablePrivilege().Readable&&
                 user.UserRole.getRoleTablePrivilege().Readable)){
             error(530,"Access Denied");
         }
 
         List<Role> roles = Role.all().fetch();
         User model = User.findById(id);
         if(model==null)
             badRequest();
         else
             render("AdminUser/create.html",roles,model);
     }
 
     @Check("User+W")
     public  static void handleCreate(@Required User u,@Required Long RoleId){
 
         if(Validation.hasErrors()){
             Logger.debug("The Input Contains Errors");
             for (play.data.validation.Error e: Validation.errors()){
                 Logger.debug(" %s",e.message());
             }
             badRequest();
         }
         u.UserRole = Role.findById(RoleId);
         Logger.debug("Create User Name = %s, Password = %s",u.LoginName,u.Password);
         u.hashPassword();
         u.save();
         list(null,null);
     }
 
     public static void handleEdit(@Required User u, @Required Long RoleId, @Required Long id){
         User user = User.find("LoginName = ?", Security.connected()).first();
         if(!(user.getId().equals(id) || user.UserRole.getUserTablePrivilege().Writable&&
                 user.UserRole.getUserTablePrivilege().Readable&&
                 user.UserRole.getRoleTablePrivilege().Readable)){
             error(530,"Access Denied");
         }
         if(Validation.hasErrors()){
             badRequest();
         }
         User oldUser = User.findById(id);
         if(oldUser==null)
             badRequest();
 
         assert oldUser != null;
         oldUser.LoginName = u.LoginName;
         if(!u.Password.isEmpty()){
             oldUser.Password = u.Password;
             oldUser.hashPassword();
         }
         oldUser.Department = u.Department;
         oldUser.Email = u.Email;
         oldUser.Telephone = u.Telephone;
         oldUser.Mobile = u.Mobile;
         oldUser.IsMale = u.IsMale;
         oldUser.RealName = u.RealName;
         Logger.debug("Real Name = %s",u.RealName);
         oldUser.UserNumber = u.UserNumber;
         if(!oldUser.UserRole.id.equals(RoleId)){
             Role r = Role.findById(RoleId);
             if(r==null)
                 badRequest();
             oldUser.UserRole = r;
         }
         oldUser.save();
         list(null,null);
     }
    @Check("User+W")
     public static void delete(@Required Long id){



         if(Validation.hasErrors()){
             badRequest();
         }

        User user = (User) renderArgs.get("user");
        if(user.id.equals(id)){
            renderJSON(false);
        }
         int row = 0;
         try{
             row = User.delete("id",id);
         } catch (Throwable ignored){
             renderJSON(false);
         }
         renderJSON(row);
     }
 }
