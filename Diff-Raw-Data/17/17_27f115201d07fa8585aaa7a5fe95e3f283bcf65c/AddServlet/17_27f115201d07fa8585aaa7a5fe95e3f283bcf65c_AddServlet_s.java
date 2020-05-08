 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.homework.hw3;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.homework.hw3.hsql.DbAccess;
 
 public class AddServlet extends HttpServlet {
 
	public static boolean contextHasRan = false;
	
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	request.setAttribute("context_has_ran", contextHasRan);
     	request.getRequestDispatcher("WEB-INF/JSP/hw3/Add.jsp").forward(request, response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     	DbAccess db = new DbAccess();
         String name = request.getParameter("name");
         String code = request.getParameter("code");
         db.insertData(name, code);
         response.sendRedirect("Search");
     }
 
 }
