 package ufly.frs_test;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ufly.entities.Airport;
 
 
 @SuppressWarnings("serial")
 public class AirportTest extends HttpServlet {
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		req.getRequestDispatcher("AirportTest.jsp").forward(req, resp);
 	}
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 		throws IOException,ServletException
 	{
 		String city = req.getParameter("city");
 		String callsign = req.getParameter("callsign");
 		String coordinates = req.getParameter("coordinates");
 		
 		new Airport(callsign, city, coordinates);
		resp.sendRedirect("/entityTest?test=Airport");
 	}
 }
