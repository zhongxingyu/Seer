 package com.ryliu.j2ee.lab01;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class LoginServlet
  */
 public class LoginServlet extends HttpServlet {
     private static final long serialVersionUID = 1L;
 
     /**
      * Represents the login user name.
      */
     private static final String USER_NAME = "je1124061";
 
     /**
      * Represents the login password.
      */
     private static final String PASSWORD = "283245";
 
     /**
      * @see HttpServlet#HttpServlet()
      */
     public LoginServlet() {
         super();
     }
 
     /**
      * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         if (USER_NAME.equals(request.getParameter("username")) && PASSWORD.equals(request.getParameter("password"))) {
             HttpSession session = request.getSession(true);
             session.setAttribute("accessToken", USER_NAME);
             session.setMaxInactiveInterval(-1);
 
             String redirectUrl = "index.jsp";
             if (session.getAttribute("redirectUrl") != null) {
                 redirectUrl = (String) session.getAttribute("redirectUrl");
                session.removeAttribute("redirectUrl");
             }
             response.sendRedirect(redirectUrl);
         } else {
             response.sendRedirect("login.jsp?error=true");
         }
     }
 
 }
