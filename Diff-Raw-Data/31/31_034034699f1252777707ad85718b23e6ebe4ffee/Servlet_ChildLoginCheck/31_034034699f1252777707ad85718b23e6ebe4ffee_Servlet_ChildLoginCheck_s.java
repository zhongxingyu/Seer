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
 
 
 public class Servlet_ChildLoginCheck extends HttpServlet{
 	
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
 			
 			List<Child> allChildren = familyMgr.getAllChildren();
 			
 			Child currentChild = null;
 			
 			//retrieving what the user's input here. 
 	        String username=request.getParameter("username");
 	        String password=request.getParameter("password");
 			
 	        //getting the child using manager.
 			for (Child b: allChildren) {
 				if (b.getUserName().equalsIgnoreCase(username)) {
 					currentChild = b;
 				}
 			}	        
 	        
 			//checking whether does the current child exist
 	        if (currentChild == null) {
	        	response.sendRedirect("Error.jsp");
 	        } else {
 	        		        	
 	        	if (currentChild.getUserName().equalsIgnoreCase(username) && currentChild.getPassword().equalsIgnoreCase(password)){
 	        		session.setAttribute("username",currentChild);
 	            	response.sendRedirect("child-dashboard.jsp");
 	            	//System.out.println("Child page");
 	        	} else {
 	        		//System.out.println("Child, wrong credentials.");
	        		response.sendRedirect("Error.jsp");
 	        	}
 	        
 	        }	
 				
 		}
 }
