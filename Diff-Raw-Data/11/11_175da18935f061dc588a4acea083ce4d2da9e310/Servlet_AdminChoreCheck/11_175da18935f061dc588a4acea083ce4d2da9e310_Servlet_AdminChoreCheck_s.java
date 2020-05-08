 package controller;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.io.*;
 import entity.*;
 import manager.*;
 import java.util.*;
 
 
 public class Servlet_AdminChoreCheck  extends HttpServlet{
 
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
 		
 		//getting managers
 		ChoreManager choreMgr = ChoreManager.getInstance();
 		
 		//getting all the parameters from the user form.
 		String choreCheckType = request.getParameter("choreCheckType");
 		
 		String chore[]= request.getParameterValues("chore");
 		
		System.out.println("Chore check type: " + choreCheckType);
 		
 		//if user is going to check uncompleted chores and delete it...
 		if (choreCheckType.equalsIgnoreCase("unCompletedChores")) {
			System.out.println("Uncompleted Chores");
 			
 			String a="";
 			String choice=request.getParameter("choice");	
 			
 			if(chore != null && choice.equals("Yes")) {
				System.out.println("Chore is not empty and yes!");	
 				for(int i=0; i<chore.length; i++){
 					a=chore[i];
 					int aInt = Integer.parseInt(a);
 					Chore getChore=choreMgr.getChore(aInt);
 							
 					if(getChore.getChoreID() == aInt){
 						choreMgr.getAllChores().remove(choreMgr.getChore(aInt));
 					}
 				}
 			response.sendRedirect("admin-dashboard.jsp");
 			}
 			  
		} else { //if user is going to approve the completed chores. 
 			
 			//getting the user selected chore. 
 			for (String a: chore) {
 				
 			}
 		}
 		
 			
 	}
 
 }
