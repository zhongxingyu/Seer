 package playlist.controller;
 
 import playlist.exceptions.UserExistsException;
 import playlist.exceptions.UserLoginException;
 import playlist.model.StatisticsDAO;
 import playlist.model.UserDAO;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
 /**
  * DataStax Academy Sample Application
  *
  * Copyright 2013 DataStax
  *
  */
 
 public class LoginServlet extends HttpServlet {
 
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
     // User creation and login is via the post method.  Logout is with a get.
 
     String button = request.getParameter("button");
     button = button == null ? "" : button;
 
     if (button.contentEquals("login")) {
       doLogin(request, response);
     } else if (button.contentEquals("newAccount")) {
       doCreateUser(request, response);
     }
 
   }
 
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
     StatisticsDAO.increment_counter("page hits: login");
 
     String button = request.getParameter("button");
     button = button == null ? "" : button;
 
     if (button.contentEquals("logout")) {
       doLogout(request, response);
     }
     else
     {
       getServletContext().getRequestDispatcher("/login.jsp").forward(request,response);
     }
   }
 
 
   private void doLogin  (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     String email = request.getParameter("email");
     String password = request.getParameter("password");
 
     if (email.isEmpty()) {
       request.setAttribute("error", "Email Can Not Be Blank");
       getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
       return;
 
     }
 
     try {
       UserDAO user = UserDAO.validateLogin(email, password);
       HttpSession httpSession = request.getSession(true);
       httpSession.setAttribute("user", user);
 
     } catch (UserLoginException e) {
 
       // Go back to the user screen with an error
 
       StatisticsDAO.increment_counter("failed login attempts");
 
       request.setAttribute("error", "Email or Password is Invalid");
       getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
       return;
     }
 
     StatisticsDAO.increment_counter("valid login attempts");
 
     response.sendRedirect("playlists");
 
   }
 
 
   private void doCreateUser  (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     String email = request.getParameter("email");
     String password = request.getParameter("password");
 
     if (email.isEmpty()) {
       request.setAttribute("error", "Email Can Not Be Blank");
       getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
       return;
     }
 
     HttpSession httpSession = request.getSession(true);
 
     // Add the user.  If it's successful, create a login session for it
     try {
 
       StatisticsDAO.increment_counter("users");
 
 
       UserDAO newUser = UserDAO.addUser(email, password);
 
 
       // Create the user's login session so this application recognizes the user as having logged in
       httpSession.setAttribute("user", newUser);
 
     } catch (UserExistsException e) {
 
       // Go back to the user screen with an error
 
       request.setAttribute("error", "User Already Exists");
       getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
       return;
 
     }
 
     response.sendRedirect("playlists");
 
   }
 
   private void doLogout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     HttpSession session = request.getSession();
     if (session != null) {
       session.setAttribute("user", null);
     }
 
    response.sendRedirect("login");
 
   }
 
 
 }
