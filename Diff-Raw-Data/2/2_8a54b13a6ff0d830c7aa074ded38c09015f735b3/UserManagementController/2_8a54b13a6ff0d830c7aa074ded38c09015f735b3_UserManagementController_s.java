 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.greenland.memorycards.controller;
 
 /**
  *
  * @author jurijspe
  */
 import com.greenland.memorycards.model.User;
 
 import com.greenland.memorycards.service.UserManager;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 public class UserManagementController implements Controller {
 
     private UserManager userManager;
 
     public void setUserManager(UserManager userManager) {
         this.userManager = userManager;
     }
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Override
     public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         // The below code smells. TODO: Refactor
 
         logger.info("User Management Controller");
         Map<String, Object> model = new HashMap<String, Object>();
         String actionName = "";
         int userId = -1;
         Enumeration e = request.getParameterNames();
         while (e.hasMoreElements()) {
             String parameterName = (String) e.nextElement();
             if (parameterName.equalsIgnoreCase("action")) {
                 actionName = (String) request.getParameter("action");
             }
             if (parameterName.equalsIgnoreCase("id")) {
                 userId = Integer.valueOf((String) request.getParameter("id"));
             }
 //            logger.info("Action name: " + actionName);
         }
         processAction(request, actionName, userId, model);
         // Display all USERS
         List<User> userList = userManager.getAllUsers();
         model.put("userList", userList);
         return new ModelAndView("manageUsers", "model", model);
     }
 
     private void processAction(HttpServletRequest request, String actionName, int userId, Map<String, Object> model) {
         boolean create = false;
         boolean delete = false;
         boolean update = false;
 
         if (actionName.equalsIgnoreCase("actioncreate")) {
             create = true;
         }
         if (actionName.equalsIgnoreCase("actiondelete")) {
             delete = true;
         }
         if (actionName.equalsIgnoreCase("actionupdate")) {
             update = true;
         }
         displayForms(actionName, userId, request, model);
         performActions(update, request, delete, create, userId);
     }
 
     private void performActions(boolean update, HttpServletRequest request, boolean delete, boolean create, int userId) throws NumberFormatException {
         // Form actions
 
         if (update) {
             logger.info("Updating ... ");
             User userToBeUpdated = userManager.getUser(userId);
 
             // Could use formBacking Object instead. TODO: Refactor
             User formUser = new User();
             formUser.setEmail((String) request.getParameter("email"));
             formUser.setPassword((String) request.getParameter("password"));
             formUser.setfName((String) request.getParameter("userFName"));
             formUser.setlName((String) request.getParameter("userLName"));
             User updatedUser = userManager.combineUsers(userToBeUpdated, formUser);
             userManager.updateUser(updatedUser);
         }
 
         if (delete) {
             logger.info("Deleting ...");
             userManager.deleteUserWithId(userId);
         }
 
         if (create) {
             logger.info("Creating ...");
             User formUser = new User();
             formUser.setEmail((String) request.getParameter("email"));
             formUser.setPassword((String) request.getParameter("password"));
             formUser.setfName((String) request.getParameter("userFName"));
             formUser.setlName((String) request.getParameter("userLName"));
             userManager.createNewUser(formUser);
         }
     }
 
     private void displayForms(String actionName, int userId, HttpServletRequest request, Map<String, Object> model) throws NumberFormatException {
         logger.info("Displaying form for action "+actionName);
         if (actionName.equalsIgnoreCase("edit")) {
             logger.info("Show user to Edit (id=" + userId + ")");
             User userToEdit = userManager.getUser(userId);
             model.put("userToEdit", (User) userToEdit);
         }
         if (actionName.equalsIgnoreCase("delete")) {
             logger.info("Show user to delete (id=" + userId + ")");
             User userToDelete = userManager.getUser(userId);
             model.put("userToDelete", (User) userToDelete);
         }
         if (actionName.equalsIgnoreCase("create")) {
             logger.info("Show Create New User Form");
             model.put("userToCreate", new User());
         }
     }
 }
