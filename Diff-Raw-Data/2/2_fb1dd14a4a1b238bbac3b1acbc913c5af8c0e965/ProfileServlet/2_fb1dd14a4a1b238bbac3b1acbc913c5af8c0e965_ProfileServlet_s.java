 //Created By Ilan Godik
 package servlets;
 
 import db.DB;
 import db.RealDB;
 import model.User;
 import util.Validation.IntValidator;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
 public class ProfileServlet extends HttpServlet {
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         req.setCharacterEncoding("UTF-8");
         resp.setCharacterEncoding("UTF-8");
         HttpSession session = req.getSession();
         User user = (User) session.getAttribute("user");
         if (user != null) {
             RealDB db = new RealDB();
             boolean failed = false;
             String username = req.getParameter("username");
             if (username.contains(" ")) {
                 fail(req, "username");
                 failed = true;
             }
             if(entries(db, "username", username) != 0 && !username.equals(user.username)){
                 fail(req,"username-taken");
                 failed = true;
             }
             String email = req.getParameter("email");
             if (email.contains(" ")) {
                 fail(req, "email");
                 failed = true;
             }
             if(entries(db, "email", email) != 0 && !email.equals(user.email)){
                 fail(req, "email-taken");
                 failed = true;
             }
             String firstName = req.getParameter("firstName");
             if (firstName.contains(" ")) {
                 fail(req, "firstName");
                 failed = true;
             }
             String lastName = req.getParameter("lastName");
             if (lastName.contains(" ")) {
                 fail(req, "lastName");
                 failed = true;
             }
             String birthYear = req.getParameter("birthYear");
             IntValidator validator = new IntValidator();
             if (!validator.isValid(birthYear)) {
                 fail(req, "birthYear");
                 failed = true;
             }
             if (!failed) {
                 User updated = new User(user.id, username, user.passHash, email, firstName, lastName, Integer.parseInt(birthYear), user.isAdmin);
                 updated.save(db);
                 session.setAttribute("user", updated);
                 req.setAttribute("success", "");
             }
         }
 
        req.getRequestDispatcher("/profile/profile.jsp").forward(req,resp);
     }
 
     private void fail(HttpServletRequest req, String field) {
         req.setAttribute("fail-"+field, new Object());
     }
 
     private int entries(DB db, String field, String value) {
         return db.select("SELECT " + field + " FROM users WHERE " + field + "='" + value + "';").length;
     }
 }
