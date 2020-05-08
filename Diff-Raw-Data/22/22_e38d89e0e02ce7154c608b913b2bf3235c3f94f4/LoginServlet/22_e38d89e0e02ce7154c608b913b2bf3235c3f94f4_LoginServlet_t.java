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



 
             try {
                 System.out.println("In the Login Servlet");
                 LoginBean user = new LoginBean();
                 user.setUserName(request.getParameter("uname"));
                 user.setPassword(request.getParameter("password"));
                 user = LoginDAO.login(user);
                 if (user.isValid()) {
                    HttpSession session = request.getSession(true);
                     session.setAttribute("currentSessionUser", user);
                     response.sendRedirect("taskList.og");
                 } else {
                    response.sendRedirect("indexer.jsp");
                 }
             } catch (Throwable exc) {
                 System.out.println(exc);
 
             }
 


 
 
     }
 
 
         /**
          * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
          */
 
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // TODO Auto-generated method stub
     }
 
 }
 
