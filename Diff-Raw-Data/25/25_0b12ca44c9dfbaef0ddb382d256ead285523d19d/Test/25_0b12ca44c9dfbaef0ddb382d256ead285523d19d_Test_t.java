 package ufly.frs;
 
 import java.io.IOException;
 
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ufly.entities.Airport;
 import ufly.entities.Customer;
 import ufly.entities.Flight;
 import ufly.entities.FlightBooking;
 import ufly.entities.FlightManager;
 import ufly.entities.FlightStaff;
 import ufly.entities.Seat;
 import ufly.entities.SeatingArrangement;
 
 @SuppressWarnings("serial")
 public class Test extends HttpServlet{
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws IOException 
     {
 		
 		String test=request.getParameter("test");
 		response.setContentType("text/html");
 		response.getWriter().println("<html><body><h2>welcome to the test servlet</h2>");
 		if( test == null )
 		{
 
 		}
 		else if( test.equalsIgnoreCase("Customer") == true )
 		{
 			testCustomer(request,response);
 		}
 		else if( test.equalsIgnoreCase("FlightManager") == true )
 		{
 			testFlightManager(request,response);
 		}
 		else if( test.equalsIgnoreCase("FlightStaff") == true )
 		{
 			testFlightStaff(request,response);
 		}
 		else if( test.equalsIgnoreCase("Airport") == true )
 		{
 			testAirport(request,response);
 		}
 		else if( test.equalsIgnoreCase("Flight") == true )
 		{
 			testFlight(request, response);
 		}
 		else if( test.equalsIgnoreCase("Seat") == true )
 		{
 			testSeat(request, response);
 		}
 		else if( test.equalsIgnoreCase("SeatingArrangement") == true )
 		{
 			testSeatingArrangement(request, response);
 		}
 		else if( test.equalsIgnoreCase("FlightBooking") == true )
 		{
 			testFlightBooking(request, response);
 		}
 		response.getWriter().println("</body></html>");
 	
     }
 	
 
 	private void testFlight(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		/**
 		 * URIs that to test flight:
 		 * Assuminging you have YVR and LAX in your database
 		 * http://localhost:8888/entityTest?test=Flight&flightno=abc&origin=YVR&destination=LAX&date=2012/11/11&leaveTime=4:30&arriveTime=8:30
 		 */
 		response.getWriter().println("<h3>testing flights</h3>");
 		String flightno = request.getParameter("flightno");
 		String origin = request.getParameter("origin");
 		String destination = request.getParameter("destination");
 		String departure = request.getParameter("departure");
 		String arrival = request.getParameter("arrival");
 		String mealTypes = request.getParameter("mealsTypes");
 		String aircraftModel = request.getParameter("aircraftModel");
 	
 		
 		
 		if(flightno==null && origin != null && destination == null && departure !=  null && arrival == null && mealTypes== null && aircraftModel == null)
 		{
 			Airport OriginAirport = Airport.getAirportByCallSign(origin);
 			GregorianCalendar day = new GregorianCalendar();
 			day.set(Integer.parseInt(departure.split("/")[0]),
 					Integer.parseInt(departure.split("/")[1])-1,
 					Integer.parseInt(departure.split("/")[2]),0,0,0);
 			System.out.println(day.getTime().toString());
 			List<Flight> results=Flight.getFlightsOriginDate(OriginAirport, day.getTime());
 			
 			if (!results.isEmpty() )
 			{
 				Iterator<Flight> it = results.iterator();
 	            // Print all Flights in the datastore
 	            response.getWriter().println("Flights added with origin "+origin+":");
 	            response.getWriter().println("<ul>");
 	            Flight f;
 				while (it.hasNext())
 				{
 					f = it.next();
 					response.getWriter().println("<li>"+ f.toString() +"</li>");
 				}
 				response.getWriter().println("</ul>");	
 			}else{
 				response.getWriter().print("no flight exists with origin "+origin);
 			}
 				
 		}else if(request.getParameter("flightno")==null)
 		{	
 			List<Flight> results = Flight.getAllFlights();
             Iterator<Flight> it = results.iterator();
             // Print all Flights in the datastore
             response.getWriter().println("Flights added: ");
             response.getWriter().println("<ul>");
             Flight f;
 			while (it.hasNext())
 			{
 				f = it.next();
 				response.getWriter().println("<li>"+ f.toString() +"</li>");
 			}
 			response.getWriter().println("</ul>");
         		
 		}else{
 			new Flight(flightno, origin, destination, departure, arrival, mealTypes, aircraftModel);
 			
 			response.sendRedirect("/entityTest?test=Flight");
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
 			response.sendRedirect("/entityTest?test=Airport");
 		}
 	}
 	
 	private void testCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		if(request.getParameter("emailAddr")==null)
 		{   
 	        List<Customer> la = Customer.getAllCustomers();
 			Iterator<Customer> it = la.iterator();
 			response.getWriter().println("<ul>");
 			Customer c;
 			while (it.hasNext())
 			{
 				c = it.next();
 				response.getWriter().println("<li>" + c.toString() + "</li>");
 			}
 			response.getWriter().println("</ul>");
 			
 			// Test for JoelV
 			c = Customer.getCustomer("email");
 			response.getWriter().println();
 			response.getWriter().println("Retrieving one particular Customer (email hardcoded as 'email')");
 			if( c != null)
 			{
 				response.getWriter().println("<ul>");
 				response.getWriter().println("<li>"+ c.getFirstName() + " " + c.getLastName() + ", email: " + c.getEmailAddr() + "</li>");
				if( c.checkPassword("pw") )
					response.getWriter().println("Password matches 'pw'");
				else
					response.getWriter().println("Password does not match 'pw'");
 				response.getWriter().println("</ul>");
 			}
 			else
 			{
 				response.getWriter().println("Customer not found.");
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
 	
 	private void testFlightManager(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		if(request.getParameter("emailAddr")==null)
 		{
 			List<FlightManager> results = FlightManager.getAllFlightManager();
             Iterator<FlightManager> it = results.iterator();
             // Print all FlightManagers in the datastore
             response.getWriter().println("FlightManagers registered: ");
             response.getWriter().println("<ul>");
             FlightManager fm;
 			while (it.hasNext())
 			{
 				fm = it.next();
 				response.getWriter().println("<li>"+ fm.getEmailAddr() +"</li>");
 			}
 			response.getWriter().println("</ul>");
         		
 		}
 		else
 		{
 			// Add new FlightManager here. Optional.
 		}
 	}
 	
 	private void testFlightStaff(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		if(request.getParameter("emailAddr")==null)
 		{
 			List<FlightStaff> results = FlightStaff.getAllFlightStaff();
             Iterator<FlightStaff> it = results.iterator();
             // Print all FlightManagers in the datastore
             response.getWriter().println("FlightStaff registered: ");
             response.getWriter().println("<ul>");
             FlightStaff fs;
 			while (it.hasNext())
 			{
 				fs = it.next();
 				response.getWriter().println("<li>"+ fs.getEmailAddr() +"</li>");
 			}
 			response.getWriter().println("</ul>");
 	    }
 		else
 		{
 			// Add new FlightManager here. Optional.
 		}
 	}
 	
 	private void testSeat(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		List<Seat> results = Seat.getAllSeat();
         Iterator<Seat> it = results.iterator();
         // Print all Seats in the datastore
         response.getWriter().println("Seats added: ");
         response.getWriter().println("<ul>");
         Seat s;
 		while (it.hasNext())
 		{
 			s = it.next();
 			response.getWriter().println("<li>"+ "ID: " + s.getKey().toString() + ", row: " + s.getRowNumber() + ", col: " + s.getColumn() +"</li>");
 			if( s.getFlightBooking() != null )
 				response.getWriter().println("FlightBooking: " + s.getFlightBooking());
 		}
 		response.getWriter().println("</ul>");
 	}
 	
 	private void testSeatingArrangement(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		List<SeatingArrangement> results = SeatingArrangement.getAllSeatingArrangement();
         Iterator<SeatingArrangement> it = results.iterator();
         // Print all SeatingArrangements in the datastore
         response.getWriter().println("SeatingArrangements added: ");
         response.getWriter().println("<ul>");
         SeatingArrangement sa;
 		while (it.hasNext())
 		{
 			sa = it.next();
 			response.getWriter().println("<li>"+ "ID: " + sa.getKey().toString() + ", #rows: " + sa.getNumRows() + ", #cols: " + sa.getNumColumns() +"</li>");
 		}
 		response.getWriter().println("</ul>");
     		
 	}
 	
 	private void testFlightBooking(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		List<FlightBooking> results = FlightBooking.getAllFlightBookings();
 		Iterator<FlightBooking> it = results.iterator();
         // Print all FlightBookings in the datastore
         response.getWriter().println("FlightBookings added: ");
         response.getWriter().println("<ul>");
         FlightBooking fb;
 		while (it.hasNext())
 		{
 			fb = it.next();
 			response.getWriter().println("<li>"+ fb.toString() +"</li>");
 		}
 		response.getWriter().println("</ul>");
 	}
 }
