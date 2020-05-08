 package controller;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 import entity.*;
 import manager.*;
 
 import java.util.*;
 
 
 public class Servlet_ParentLoginCheck extends HttpServlet{
 	
 	//overwrites the doGet method
 		public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
 			processRequest(request,response);
 		}
 		
 		//overwrites the doPost method
 		public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 			processRequest(request,response);
 		}
 		
 		public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 			
 			HttpSession session = request.getSession(true);
 			
 			//taking out all managers
 			FamilyManager familyMgr = FamilyManager.getInstance();
 			ChoreManager choreMgr = ChoreManager.getInstance();
 			ShopManager shopMgr = ShopManager.getInstance();
 			
 			List<Parent> allParents = familyMgr.getAllParent();
 			
 			Parent currentParent = null;
 			
 			//retrieving what the user's input here. 
 	        String username=request.getParameter("username");
 	        String password=request.getParameter("password");
			System.out.println(username);
 	      //getting the child using manager.
 			for (Parent b: allParents) {
 				if (b.getUserName().equalsIgnoreCase(username)) {
 					currentParent = b;
 				}
 			}     	
 	        
 			//check if such parent exist in the database.
 	        if (currentParent == null) {
 	        	//System.out.println("No such parent.");
	        	response.sendRedirect("admin-login.jsp");
 	        } else {
 	        	
 	        	if (currentParent.getUserName().equalsIgnoreCase(username) && currentParent.getPassword().equalsIgnoreCase(password)){
 	        		session.setAttribute("username",currentParent);
 	        		response.sendRedirect("admin-dashboard.jsp");
 	        		//System.out.println("Parent page");
 	        	} else {
 	        		//System.out.println("Parent, wrong credentials.");
	        		response.sendRedirect("admin-login.jsp");
 	        	}
 	        	
 	        }
 				
 		}
 }
