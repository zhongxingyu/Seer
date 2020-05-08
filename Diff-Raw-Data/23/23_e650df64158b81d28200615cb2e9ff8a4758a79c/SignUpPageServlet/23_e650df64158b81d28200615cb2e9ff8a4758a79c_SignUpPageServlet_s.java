 package com.in6k.twitter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 public class SignUpPageServlet extends HttpServlet {
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String login = request.getParameter("login");
         String password = request.getParameter("password");
 
         if (AccountManager.isUnique(login)) {
             request.getSession().setAttribute("authorized", true);
             AccountManager.addAccount(login, password);
             request.getRequestDispatcher("/WEB-INF/pages/registration-form-success.jsp").include(request, response);
             return;
         }
         /*request.setAttribute("error", true);
         request.setAttribute("login", login);*/
        request.getRequestDispatcher("/signup.jsp").include(request, response);
     }
 }
