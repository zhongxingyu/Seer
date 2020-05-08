 package controller;
 
 import crypto.Encryption;
 import crypto.EncryptionType;
 import manager.GroupManager;
 import manager.UserManager;
 import model.Group;
 import model.User;
 import sun.security.krb5.EncryptionKey;
 
 import javax.ejb.EJB;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 @WebServlet(urlPatterns = "/login")
 public class LoginServlet extends HttpServlet {
     @EJB
     UserManager userManager;
     @EJB
     GroupManager groupManager;
 
     private static final String DUMMY_PASSWORD = "dummypassword";
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         try{
             // grabs the users username and role to authenticate their login
             String username = request.getParameter("username");
             String role = request.getParameter("role");
 
             // if the users role does not exist in the database, create a new role in the Group table
              Group group = groupManager.getGroup(role);
              if(group == null){
                  group = new Group();
                  group.setName(role);
                  groupManager.saveGroup(group);
              }
 
             // if the users username does not exist in the database, create a new username in the User table
              User user = userManager.getUser(username);
              if(user == null){
                  user = new User();
                  user.setName(username);
                  user.setPassword(Encryption.digest(DUMMY_PASSWORD, EncryptionType.MD5));
                  Set<Group> groups = new HashSet<Group>();
                  groups.add(group);
                  user.setGroups(groups);
                  userManager.saveUser(user);
              }
 
             request.logout();
             request.login(username, DUMMY_PASSWORD);
             response.sendRedirect(request.getContextPath());
         }catch(ServletException e){
             // if the user logs in incorrectly, redirect the user to an error page to let them know
             request.getRequestDispatcher("/WEB-INF/login/login_error.jsp").forward(request, response);
          }
     }
 }
