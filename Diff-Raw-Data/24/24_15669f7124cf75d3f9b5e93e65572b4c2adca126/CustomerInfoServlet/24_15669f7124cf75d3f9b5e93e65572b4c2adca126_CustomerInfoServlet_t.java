 package kimpel;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import kimpel.beans.CustomerInfo;
 import org.apache.commons.beanutils.BeanUtils;
 
 /**
  * Servlet implementation class CustomerInfoServlet
  * 
  * This servlet handles registration of a new customer
  * 
  * Updated for HW1 part 2
  * 
  * Joe Kimpel CS667
  * 
  * 6.16.2012
  * 
  */
 @WebServlet("/CustomerInfoServlet")
 public class CustomerInfoServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
	//Map of user IDs
 	private HashMap<String, CustomerInfo> idMap;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public CustomerInfoServlet() {
         super();
     	idMap = new HashMap<String, CustomerInfo>();
     }
 
 	/**
 	 * This method simply calls doPost
 	 * 
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	/**
 	 * This method receives a request which contains information for registering a new customer.
 	 * If the information is complete, it will return a confirmation page.
 	 * If the information is incomplete, it will return a page requesting the additional info.
 	 * 
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		//Initialize output
 		response.setContentType("text/html");
 		PrintWriter out = response.getWriter();
 		
 		//Create the bean and attempt to populate
 		CustomerInfo cust = new CustomerInfo();
 		try {
 			BeanUtils.populate(cust, request.getParameterMap());
 		} catch (IllegalAccessException e) {
 			//We are checking the state of the bean after, so no action is necessary
 		} catch (InvocationTargetException e) {
 			//We are checking the state of the bean after, so no action is necessary
 		}
 		
 		//Check if the ID is unique, and if so, if it should be saved
 		boolean complete = cust.isComplete();
 		boolean unique;
 		synchronized(idMap){
 			unique = isUnique(cust);
 			if (unique && complete){
 				idMap.put(cust.getCustomerID(), cust);
 			}
 		}
 		
 		if (!unique){
 			complete = false;
 		}
 		
 		//Check and display whether the user completed the form
 		if(complete){
 			out.println(HTMLUtilities.COMPLETE_HEADER);
 		}else{
 			out.println(HTMLUtilities.INCOMPLETE_HEADER);
 			//If the info is not complete, we need to create a form
 			out.println(HTMLUtilities.FORM_OPENER);
 		}
 		
 		if (!unique){
			out.println(HTMLUtilities.idWarning(cust.getCustomerID()));
			cust.setCustomerID("");
 		}
 		
 		//Display entered information and prompts for what is still needed
 		out.println(HTMLUtilities.formEntry(cust.getCustomerID(), "Customer ID", "customerID", complete));
 		out.println(HTMLUtilities.formEntry(cust.getLastName(), "Last Name", "lastName", complete));
 		out.println(HTMLUtilities.formEntry(cust.getFirstName(), "First Name", "firstName", complete));
 		out.println(HTMLUtilities.formEntry(cust.getEmailAddress(), "Email Address", "emailAddress", complete));
 		
 		//Close up the page
 		if(!complete){
 			out.println(HTMLUtilities.FORM_CLOSER);
 		}
 		out.println(HTMLUtilities.FOOTER);
 		out.close();
 	}
 	
 	//This method checks if a proposed ID is unique
 	private boolean isUnique(CustomerInfo cust){
 		if (idMap.containsKey(cust.getCustomerID()))
 			return false;
 		else
 			return true;
 	}
 	
 }
