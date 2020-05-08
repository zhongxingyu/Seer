 package Servlet;
 
 import Crypto.Encryption;
 import Crypto.EncryptionType;
 import Entity.Group;
 import Entity.StudentInformation;
 import Entity.User;
 import EntityManager.GroupManager;
 import EntityManager.TermsOfServiceManager;
 import EntityManager.UserManager;
 
 import javax.ejb.EJB;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: brdegenaars
  * Date: 3/8/13
  * Time: 7:26 PM
  * To change this template use File | Settings | File Templates.
  */
 @WebServlet("/Start")
 public class StartServlet extends HttpServlet {
     private static final long serialVersionUID = 1L;
 
     private static final String DUMMY_PASSWORD = "dummyPassword";
 
     @EJB
     UserManager userManager;
 
     @EJB
     GroupManager groupManager;
 
     @EJB
     TermsOfServiceManager termsOfServiceManager;
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         try {
             String username = request.getParameter("username");
             String role = request.getParameter("role");
 
             Group group = groupManager.getGroup(role);
             if(group == null) {
                 group = new Group();
                 group.setName(role);
                 groupManager.saveGroup(group);
             }
 
             User user = userManager.getUser(username);
             if(user == null) {
                 user = new User();
                 user.setName(username);
                 user.setPassword(Encryption.digest(DUMMY_PASSWORD, EncryptionType.MD5));
                Set<Group> groups = new HashSet<Group>();
                 groups.add(group);
                 user.setGroups(groups);
                 userManager.saveUser(user);
             }
 
             request.logout();
 
             StudentInformation newStudent = new StudentInformation();
             newStudent.setUserName(username);
             newStudent.setPassword(Encryption.digest(DUMMY_PASSWORD, EncryptionType.MD5));
 
             HttpSession currentSession = request.getSession();
             currentSession.setAttribute("currentUser", newStudent);
 
             request.login(username, DUMMY_PASSWORD);
 
             //request.setAttribute("tos", termsOfServiceManager.getTermsOfServiceById(0));
 
             response.sendRedirect(request.getContextPath());
         } catch(ServletException e) {
             request.getRequestDispatcher("/WEB-INF/login/login_error.jsp").forward(request, response);
         }
     }
 
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
 
         if (request.getParameter("agreeToTOS").equals("off"))
             response.sendRedirect(request.getContextPath());
 
         response.sendRedirect(request.getContextPath() + "/SchoolEnrollment");
     }
 }
