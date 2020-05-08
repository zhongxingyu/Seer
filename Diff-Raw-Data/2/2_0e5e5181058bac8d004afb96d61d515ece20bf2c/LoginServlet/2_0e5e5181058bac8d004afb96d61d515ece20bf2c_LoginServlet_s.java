 package com.login;
 
 import com.login.LoginBean;
 import com.login.LoginDAO;
 
 import java.io.IOException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public class LoginServlet extends javax.servlet.http.HttpServlet {
     private static final long serialVersionUID = 1L;
 
     /**
      * @see HttpServlet#HttpServlet()
      */
     public LoginServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
     /**
      * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
      */
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         HttpSession session = request.getSession(false);
         if (session == null || session.getAttribute("currentSessionUser") == null) {
 
             try {
                 System.out.println("In the Login Servlet");
                 LoginBean user = new LoginBean();
                 user.setUserName(request.getParameter("uname"));
                 user.setPassword(request.getParameter("password"));
                 user = LoginDAO.login(user);
                 if (user.isValid()) {
                     session = request.getSession(true);
                     session.setAttribute("currentSessionUser", user);
                     response.sendRedirect("taskList.og");
                 } else {
                    request.setAttribute("errorMsg", "cos ty narobil");
                     RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
                     dispatcher.forward(request, response);
                 }
             } catch (Throwable exc) {
                 System.out.println(exc);
 
             }
 
         }
         else {
             request.setAttribute("showLogout", session.getAttribute("currentSessionUser"));
             RequestDispatcher dispatcher = request.getRequestDispatcher("index.jsp");
             dispatcher.forward(request, response);
         }
 
 
     }
 
 
         /**
          * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
          */
 
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // TODO Auto-generated method stub
     }
 
 }
 
