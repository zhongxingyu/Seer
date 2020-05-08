 package ufly.frs;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import ufly.entities.Customer;
 
 @SuppressWarnings("serial")
 public class FlightManagerProfile extends UflyServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 		throws IOException,ServletException
 	{	
 		
 		String pageToInclude= getServletConfig().getInitParameter("action");
 
 			if (getLoggedInUser(req.getSession())!=null)
 			{
				req.getRequestDispatcher("/FlightManagerProfile.jsp")
 				.forward(req,resp);
 			}
 			else{
 				resp.sendRedirect("/");
 			}
 			
 		
 		
 		
 		
 		
 		
 		//if (getLoggedInUser(req.getSession())!=null)
 		//{
 		//	Customer loggedInCustomer = Customer.getCustomer(getLoggedInUser(req.getSession()).getEmailAddr());
 		//	req.setAttribute("customerFirstName", loggedInCustomer.getFirstName());
 		//	req.setAttribute("customerLastName", loggedInCustomer.getLastName());
 		//	req.getRequestDispatcher("/FlightManagerProfile.jsp")
 		//	.forward(req,resp);
 		//}
 		//else{
 		//	resp.sendRedirect("/");
 		//}
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 		throws IOException,ServletException
 	{
 		
 		resp.setContentType("text/plain");
 		
 		
 	}
 }
