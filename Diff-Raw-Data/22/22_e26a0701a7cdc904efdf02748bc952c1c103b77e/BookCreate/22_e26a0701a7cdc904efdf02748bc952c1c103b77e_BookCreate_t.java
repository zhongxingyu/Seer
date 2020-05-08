 package ufly.frs;
 
 import java.io.IOException;
 import java.util.Date;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ufly.entities.*;
 
 @SuppressWarnings("serial")
 public class BookCreate extends UflyServlet {
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException,ServletException
 	{
 		//printParam(req,resp)
 		SimpleDateFormat convertToDate = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 		printParam(req,resp);
 //		String CreditCardNo = (String)req.getParameter("creditCard");
 		Integer numberOfFlights= Integer.parseInt((String)req.getParameter("numberOfFlights"));
		
		//check to see if there is a user logged in
		Customer localUser = (Customer) getLoggedInUser(req.getSession());
		if (localUser==null)
		{
			//should not happen
			throw new NullLoginUser();
		}
 		for(Integer i=0;i<numberOfFlights;i++)
 		{
 			//Parse all the Posted values
 			String FlightNo = (String)req.getParameter("flightNo["+i.toString()+"]");
 			String departureStr = (String)req.getParameter("Date["+i.toString()+"]");
 			String[] seatStr = ((String)req.getParameter("seat["+i.toString()+"]")).split(" ");
 			Meal meal = Meal.valueOf((String)req.getParameter("meal["+i.toString()+"]"));
 			
 			Date departureDate = convertToDate.parse(departureStr, new ParsePosition(0));
 			Flight f=Flight.getFlight(FlightNo, departureDate);
 			Seat seat=f.getSeatingArrangement().getSeatByRowCol(Integer.parseInt(seatStr[0]),seatStr[1].charAt(0) );
 			
			
 			FlightBooking fb= new FlightBooking(localUser,f,seat,meal);
 			//Now associate this flightbooking with a 
 			seat.setFlightBooking(fb.getConfirmationNumber());
 			f.addFlightBooking(fb.getConfirmationNumber());
 			localUser.addFlightBooking(fb.getConfirmationNumber());
 			localUser.addLoyaltyPoints(f.getPriceInCents()/100);
 		}
 		resp.sendRedirect("/_ah/admin/datastore?kind=Customer");
 	}
 }
