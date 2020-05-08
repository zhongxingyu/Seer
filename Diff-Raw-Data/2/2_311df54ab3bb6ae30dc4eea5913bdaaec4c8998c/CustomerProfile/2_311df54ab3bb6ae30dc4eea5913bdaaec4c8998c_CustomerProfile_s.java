 package ufly.frs;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import ufly.entities.Customer;
 
 @SuppressWarnings("serial")
 public class CustomerProfile extends UflyServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 		throws IOException,ServletException
 	{	
 		
 		String pageToInclude= getServletConfig().getInitParameter("action");
 		Customer loggedInUser=null;
 		try {
 			loggedInUser = (Customer) getLoggedInUser(req.getSession());
 		} catch (UserInactivityTimeout e) {
 			resp.sendRedirect("/?errorMsg=Sorry, you have been logged out because you have been inactive too long");
 			return;
 		} catch(ClassCastException e){
 		}
 		
 		if(pageToInclude.equals("index") )
 		{
 
 			
 			if (loggedInUser!=null)
 			{
 				req.setAttribute("customerFirstName", loggedInUser.getFirstName());
 				req.setAttribute("customerLastName", loggedInUser.getLastName());
 				req.setAttribute("loyaltyPoints", loggedInUser.getLoyaltyPoints());
 				req.getRequestDispatcher("customerProfile.jsp")
 				.forward(req,resp);
 			}
 			else{
 				resp.sendRedirect("/?errorMsg=Must be logged in as a customer to view the Customer Profile");
 			}
 		}else if (pageToInclude.equals("edit") )
 		{
 			if (loggedInUser !=null)
 			{
 				
 				req.setAttribute("customerFirstName", loggedInUser.getFirstName());
 				req.setAttribute("customerLastName", loggedInUser.getLastName());
 				req.setAttribute("customerEmail", loggedInUser.getEmailAddr());
 				req.getRequestDispatcher("/customerProfile_edit.jsp")
 				.forward(req,resp);
 			}
 			else{
 				resp.sendRedirect("/?errorMsg=Must be logged in as a customer to view the Customer Profile");
 			}
 			
 		}
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 		throws IOException,ServletException
 	{
 		String pageToInclude= getServletConfig().getInitParameter("action");
 		Customer loggedInUser=null;
 		try {
 			loggedInUser = (Customer) getLoggedInUser(req.getSession());
 		} catch (UserInactivityTimeout e) {
 			resp.sendRedirect("/?errorMsg=Sorry, you have been logged out because you have been inactive too long");
 		} catch(ClassCastException e){
 		}
 		if (pageToInclude.equals("edit") )
 		{
 			if (loggedInUser!=null)
 			{
 				String firstName= req.getParameter("fname");
 				String lastName= req.getParameter("lname");
 				String newPw = req.getParameter("newpassword");
 				String confirmPw = req.getParameter("confirmnewpass"); // TO-DO: verify that newPw == confirmPw
 				
 					if((newPw == null) || (confirmPw == null) || newPw.equals("") || !newPw.equals(confirmPw) )
 					{
 						req.setAttribute("customerEmail", loggedInUser.getEmailAddr());
 						req.setAttribute("customerFirstName", firstName);
 						req.setAttribute("customerLastName", lastName);
						req.setAttribute("errorMsg", "Password Mismatch!");
 						req.getRequestDispatcher("/customerProfile_edit.jsp")
 							.forward(req,resp);
 						return;
 					}else{
 						loggedInUser.changeFirstName(firstName);
 						loggedInUser.changeLastName(lastName);
 						if(newPw!=null){
 							loggedInUser.changePw(newPw);
 						}
 						req.setAttribute("customerFirstName", loggedInUser.getFirstName());
 						req.setAttribute("customerLastName", loggedInUser.getLastName());
 						req.setAttribute("customerEmail", loggedInUser.getEmailAddr());
 						req.setAttribute("successMsg", "Updated Profile Successfully");
 						req.getRequestDispatcher("/customerProfile_edit.jsp")
 							.forward(req,resp);
 					}
 			}
 			else{
 				resp.sendRedirect("/");
 			}
 			
 		}
 		
 	}
 }
