 package ufly.frs;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ufly.entities.Airport;
 import ufly.entities.Customer;
 import ufly.entities.PMF;
 @SuppressWarnings("serial")
 public class Test extends HttpServlet{
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException 
     {
 		
 		String test=request.getParameter("test");
		if( test == null )
		{
			response.setContentType("text/plain");
			response.getWriter().println("<h2>welcome to the test servlet</h2>");
		}
		else if( test.equalsIgnoreCase("Customer") == true )
 		{
 			testCustomer(request,response);
 			return;
 		}
 		else if( test.equalsIgnoreCase("Airport") == true )
 		{
 			testAirport(request,response);
 			return;
 		}
 	
     }
 	private void testAirport(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		if(request.getParameter("callSign")==null)
 		{
 			
 			List<Airport> la = Airport.getAllAirports();
 			Iterator<Airport> it = la.iterator();
 			response.getWriter().println("<ul>");
 			while (it.hasNext())
 			{
 				response.getWriter().println("<li>"+it.next().toString()+"</li>");
 			
 			}
 			response.getWriter().println("</ul>");
 		}
 		else
 		{
 			String city = request.getParameter("city");
 			String csign = request.getParameter("callSign");
 			if(city ==null || csign == null)
 			{
 				response.getWriter().println("Need both city or callsign");
 				return;
 			}
 			new Airport(csign,city);
 			response.sendRedirect("/entityTest");
 		}
 	}
 	
 	private void testCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		if(request.getParameter("emailAddr")==null)
 		{
 			PersistenceManager pm = PMF.get().getPersistenceManager();
 			try {
 	            // TODO make this a query a class method
 	            Query q = pm.newQuery(Customer.class);
 	            /**
 	             * in order to check this we need to check every element to see if it 
 	             * is of type User, too much work, plus we should not get any
 	             * other type. We just suppress the warning
 	             */
 	            @SuppressWarnings("unchecked")
 				List<Customer> results = (List<Customer>) q.execute();
 	            Iterator<Customer> it = results.iterator();
 	            // Print all Users in the datastore
 	            response.getWriter().println("Customers registered: ");
 	            response.getWriter().println("<ul>");
 	            Customer c;
 				while (it.hasNext())
 				{
 					c = it.next();
 					response.getWriter().println("<li>"+c.getFirstName() + " " + c.getLastName() +"</li>");
 				
 				}
 				response.getWriter().println("</ul>");
 	        } finally {
 	            pm.close();
 	        }		
 		}
 		else
 		{
 			String emailAddr = request.getParameter("emailAddr");
 			String password = request.getParameter("password");
 			String firstName = request.getParameter("firstName");
 			String lastName = request.getParameter("lastName");
 			if(emailAddr == null || password == null || firstName == null || lastName == null)
 			{
 				response.getWriter().println("Missing parameters");
 				return;
 			}
 			new Customer(emailAddr,password,firstName,lastName);
 			response.sendRedirect("/entityTest?test=Customer");
 		}
 	}
 }
