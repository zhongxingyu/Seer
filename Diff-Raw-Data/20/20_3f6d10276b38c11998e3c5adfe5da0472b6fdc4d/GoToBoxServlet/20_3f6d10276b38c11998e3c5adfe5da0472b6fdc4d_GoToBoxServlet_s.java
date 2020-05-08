 package edu.colorado.csci3308.inventory.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.Servlet;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.colorado.csci3308.inventory.Card;
 import edu.colorado.csci3308.inventory.Reservation;
 import edu.colorado.csci3308.inventory.Server;
 import edu.colorado.csci3308.inventory.ServerDB;
 import edu.colorado.csci3308.inventory.User;
 
 /**
  * Servlet implementation class GoToBoxServlet
  */
 @WebServlet("/GoToBoxServlet")
 public class GoToBoxServlet extends HttpServlet implements Servlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		//This should take the box number from the BoxList page and send it as info to the Box.jsp page
 		ServerDB.setPath(request.getServletContext().getRealPath("WEB-INF/db/servers.db")); //get path to the database
 		String forward="Box.jsp"; //This is the page that we are going to go to.
 		
 		String iString = request.getParameter("BoxID");
 		Integer i = 1;
 		
 		try {
 			i = Integer.parseInt(iString);
 		} catch (NumberFormatException e) {
 			System.out.println("Error parsing string to int");
 			i = -1;
 		}
 		Server s = ServerDB.getServerById(i);
 		if(s== null) //If the server doesn't exist for some reason do nothing
 			return;
 		
 		request.getSession().setAttribute("BoxID", s.getServerId());
 		request.getSession().setAttribute("HostName", s.getHostname());
 		request.getSession().setAttribute("IpAddress", s.getIpAddress());
 		request.getSession().setAttribute("MacAddress", s.getMacAddress());
 		request.getSession().setAttribute("TotalMemory", s.getTotalMemory());
 		request.getSession().setAttribute("ScanDate", s.getServerScanDate());
 		Card tempCard1;
 		if(s.numCards() >=1) {tempCard1 = s.getCard(1);} else {tempCard1 = new Card();}
 		request.getSession().setAttribute("Card1", tempCard1);
 		Card tempCard2;
 		if(s.numCards() >=2) {tempCard2 = s.getCard(2);} else {tempCard2 = new Card();}
 		request.getSession().setAttribute("Card2", tempCard2);
 		request.getSession().setAttribute("TotalMemory", s.getTotalMemory());
 		request.getSession().setAttribute("Processor1", s.getProcessor1());
 		request.getSession().setAttribute("Processor2", s.getProcessor2());		
 		request.getSession().setAttribute("Motherboard", s.getMotherboard());
 		request.getSession().setAttribute("RackID", s.getRackId());
 		request.getSession().setAttribute("Chassis", s.getChassisModel());
		request.getSession().setAttribute("LocationId", s.getLocationId());
 		
 		Reservation res = ServerDB.getReservationByServerId(s.getServerId());
 		if(res != null){
 			request.getSession().setAttribute("isReserved", new Boolean(true));
 			User user = ServerDB.getUserById(res.getUserId());
 			if(user != null)
 				request.getSession().setAttribute("Reserved", user.getUserName());
 			else
 				request.getSession().setAttribute("Reserved", "Username not valid");
 			request.getSession().setAttribute("ReservedFrom", res.getStartDate().toString());
 			request.getSession().setAttribute("ReservedTo", res.getEndDate().toString());
 		}
 		
 		RequestDispatcher view = request.getRequestDispatcher(forward); //Dispatches to new JSP
 		//sets the BoxID field in the Box JSP
 		view.forward(request, response);
 	}
 
 }
