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
 		if(pageToInclude.equals("index") )
 		{
 		
 			if (getLoggedInUser(req.getSession())!=null)
 			{
 				Customer loggedInCustomer =(Customer)getLoggedInUser(req.getSession());
 				req.setAttribute("customerFirstName", loggedInCustomer.getFirstName());
 				req.setAttribute("customerLastName", loggedInCustomer.getLastName());
 				req.setAttribute("loyaltyPoints", loggedInCustomer.getLoyaltyPoints());
 				req.getRequestDispatcher("customerProfile.jsp")
 				.forward(req,resp);
 			}
 			else{
 				resp.sendRedirect("/");
 			}
 		}else if (pageToInclude.equals("edit") )
 		{
 			if (getLoggedInUser(req.getSession())!=null)
 			{
 				Customer loggedInCustomer = Customer.getCustomer(getLoggedInUser(req.getSession()).getEmailAddr());
 				req.setAttribute("customerFirstName", loggedInCustomer.getFirstName());
 				req.setAttribute("customerLastName", loggedInCustomer.getLastName());
 				req.setAttribute("customerEmail", loggedInCustomer.getEmailAddr());
 				req.getRequestDispatcher("/customerProfile_edit.jsp")
 				.forward(req,resp);
 			}
 			else{
 				resp.sendRedirect("/");
 			}
 			
 		}
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 		throws IOException,ServletException
 	{
 		String pageToInclude= getServletConfig().getInitParameter("action");
 		if (pageToInclude.equals("edit") )
 		{
 			if (getLoggedInUser(req.getSession())!=null)
 			{
 				Customer loggedInCustomer = Customer.getCustomer(getLoggedInUser(req.getSession()).getEmailAddr());
 				String firstName= req.getParameter("fname");
 				String lastName= req.getParameter("lname");
 				String newPw = req.getParameter("newpassword");
 				String confirmPw = req.getParameter("confirmnewpass"); // TO-DO: verify that newPw == confirmPw
 				
 					if(!newPw.equals(confirmPw) && (newPw != null) && (confirmPw != null))
 					{
 						req.setAttribute("customerEmail", loggedInCustomer.getEmailAddr());
 						req.setAttribute("customerFirstName", firstName);
 						req.setAttribute("customerLastName", lastName);
						req.setAttribute("errorMsg", "passwordMismatch");
 						req.getRequestDispatcher("/customerProfile_edit.jsp")
 							.forward(req,resp);
 						return;
 					}else{
 						loggedInCustomer.changeFirstName(firstName);
 						loggedInCustomer.changeLastName(lastName);
 						if(newPw!=null){
 							loggedInCustomer.changePw(newPw);
 						}
 						req.setAttribute("customerFirstName", loggedInCustomer.getFirstName());
 						req.setAttribute("customerLastName", loggedInCustomer.getLastName());
 						req.setAttribute("customerEmail", loggedInCustomer.getEmailAddr());
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
