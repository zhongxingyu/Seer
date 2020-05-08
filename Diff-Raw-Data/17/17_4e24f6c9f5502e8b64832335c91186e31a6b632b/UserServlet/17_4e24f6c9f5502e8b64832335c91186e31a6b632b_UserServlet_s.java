 package berlin.reiche.virginia;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import berlin.reiche.virginia.model.User;
 
 /**
  * The user servlet which is dedicated for all requests regarding users.
  * 
  * @author Konrad Reiche
  * 
  */
 @SuppressWarnings("serial")
 public class UserServlet extends HttpServlet {
 
     /**
      * File path to the web resources.
      */
     private static final String USER_SITE = "ftl/users/list.ftl";
     private static final String USER_FORM_SITE = "ftl/users/form.ftl";
 
     /**
      * Singleton instance.
      */
     private static UserServlet instance = new UserServlet();
 
 	/**
 	 * Regular expression for matching a user name.
 	 */
 	static final String ID_REGEX = "[a-z0-9]*";
     
     public final static String root = "/users";
     
     /**
      * The constructor is private in order to enforce the singleton pattern.
      */
     private UserServlet() {
 
     }
 
     /**
      * Parses the HTTP request and writes the response by using the template
      * engine.
      */
     @Override
     protected void doGet(HttpServletRequest request,
             HttpServletResponse response) throws ServletException, IOException {
 
         String path = request.getPathInfo();
 
         Map<String, Object> data = AppServlet.getDefaultData();
         Writer writer = response.getWriter();
         AppServlet.checkAccessRights(request, response, root + ((path == null) ? "" : path));
 
         if (path == null) {
             data.put("users", MongoDB.getAll(User.class));
             AppServlet.processTemplate(USER_SITE, data, response.getWriter());
         } else if (path.equals("/")) {
             response.sendRedirect("/users");
         } else if (path.equals("/new")) {
             data.put(AppServlet.REQUEST_HEADLINE_VAR, "New User");
             data.put("isNewEntity", true);
             AppServlet.processTemplate(USER_FORM_SITE, data, writer);
         } else if (path.matches("/edit/" + ID_REGEX)) {
             String name = path.substring("/edit/".length());
             data.put(AppServlet.REQUEST_HEADLINE_VAR, "Edit User");
             data.put("isNewEntity", false);
             data.put("user", MongoDB.get(User.class, name));
             AppServlet.processTemplate(USER_FORM_SITE, data,
                     response.getWriter());
         } else if (path.matches("/delete/" + ID_REGEX)) {
             String name = path.substring("/delete/".length());
             MongoDB.delete(User.class, name);
             response.sendRedirect("/users");
         } else {
             AppServlet.processTemplate(AppServlet.NOT_FOUND_SITE, data, writer);
         }
     }
 
     /**
      * Parses all user HTML form requests and handles them.
      */
     @Override
     protected void doPost(HttpServletRequest request,
             HttpServletResponse response) throws ServletException, IOException {
 
         String path = request.getPathInfo();

         if ("/new".equals(path)) {
             handleUserForm(request, response, null);
        } else if (path.matches("/edit/" + AppServlet.ID_REGEX)) {
             String name = path.substring("/edit/".length());
             User user = MongoDB.get(User.class, name);
             handleUserForm(request, response, user);
         }
     }
 
     /**
      * Handles a room creation and modification requests.
      * 
      * @param request
      *            provides request information for HTTP servlets.
      * @param response
      *            provides HTTP-specific functionality in sending a response.
      * @param user
      *            if this user object is present this is an entity modification
      *            request, else it is an entity creation request.
      * @throws IOException
      *             if an input or output exception occurs.
      */
     private void handleUserForm(HttpServletRequest request,
             HttpServletResponse response, User user) throws IOException {
 
         String name = request.getParameter("name");
         String email = request.getParameter("email");
         boolean isStudent = request.getParameter("isStudent") != null;
         boolean isLecturer = request.getParameter("isLecturer") != null;
         if (user == null) {
             String password = request.getParameter("password");
             user = new User(name, password, email, isStudent, isLecturer);
         } else {
             user.setName(name);
             user.setEmail(email);
             String oldPassword = request.getParameter("oldPassword");
             String newPassword = request.getParameter("newPassword");
            user.changePassword(oldPassword, newPassword);
             user.setStudent(isStudent);
             user.setLecturer(isLecturer);
         }
         MongoDB.store(user);
         response.sendRedirect("/users");

     }
 
     /**
      * @return a singleton instance of {@link UserServlet}.
      */
     public static UserServlet getInstance() {
         return instance;
     }
 
 }
