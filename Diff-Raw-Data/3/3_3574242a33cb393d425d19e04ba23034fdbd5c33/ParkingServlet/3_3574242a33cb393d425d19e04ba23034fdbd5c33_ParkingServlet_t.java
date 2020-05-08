 package no.europark.parkingBooking;
 
 import java.io.IOException; 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import no.europark.parkingBooking.dao.LocationDao;
 import no.europark.parkingBooking.dao.LocationDaoImpl;
 import no.europark.parkingBooking.dao.ParkingPlaceDao;
 import no.europark.parkingBooking.dao.ParkingPlaceDaoImpl;
 import no.europark.parkingBooking.entity.Location;
 import no.europark.parkingBooking.entity.SearchTerms;
 import no.europark.parkingBooking.entity.User;
 
 public class ParkingServlet extends HttpServlet{
 
 	private static final long serialVersionUID = 1125844790608528788L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		 resp.setContentType("text/html");
 		 PrintWriter writer = resp.getWriter();
 		 if (req.getPathInfo().equals("/timeselect.html")){
 			 setDefaultSearchVariables(req, writer);
 		 } else if (req.getPathInfo().equals("/parkingoptions.html")) {
 			 showParkingOptions(req, writer);
 		 } else if (req.getPathInfo().equals("/login.html")){
 			 LoginForm lform = new LoginForm(req);
 			 lform.show(writer);
 		 } else if (req.getPathInfo().equals("/createuser.html")){
 			 CreateUserForm cform = new CreateUserForm(req);
 			 cform.show(writer);
 		 }
 	}
 
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		resp.setContentType("text/html");
 		PrintWriter writer = resp.getWriter();
 		if (req.getPathInfo().equals("/parkingoptions.html")) {
 			showParkingOptions(req, writer);
 		} else if (req.getPathInfo().equals("/parkingupdate.html")) {
 			showParkingOptions(req, writer);
 		} else if (req.getPathInfo().equals("/login.html")) {
 			resp.sendRedirect("/parking/login.html");
 		} else if (req.getPathInfo().equals("/createuser.html")) {
 			resp.sendRedirect("/parking/createuser.html");
		} else if (req.getPathInfo().equals("/payment.html")) {
			PaymentForm pform = new PaymentForm(req);
			pform.show(writer);
 		} else if (req.getPathInfo().equals("/receipt.html")) {
 			ReceiptForm rform = new ReceiptForm(req);
 			User user = rform.createUser();
 			rform.setUser(user);
 			System.out.println(user.getEmail() + " - " + user.getFirstName() + " - " + user.getLastName() + " - " + user.getPassword() + " - " + user.getMobilePhone());
 			rform.show(writer);
 		}
 	}
 	
 	private void showParkingOptions(HttpServletRequest req, PrintWriter writer) throws IOException {
 		SearchTerms sterms = new SearchTerms(req.getParameter("dateTo"), req.getParameter("hoursFrom"), req.getParameter("dateFrom"), req.getParameter("hoursTo"), req.getParameter("location"));
 		ParkingOptionsForm form = new ParkingOptionsForm(req);
 		ParkingPlaceDao dao = new ParkingPlaceDaoImpl();
 		LocationDao locationdao = new LocationDaoImpl();
 		form.setFormVariables(req.getParameter("dateFrom"),
 							timeListHTMLGenerator(req.getParameter("hoursFrom")),
 							req.getParameter("dateTo"),
 							timeListHTMLGenerator(req.getParameter("hoursTo")),
 							locationListHTMLGenerator(req.getParameter("location"), locationdao.getLocations()));
 		form.setParkingPlaces(dao.getParkingPlaces(sterms));
 		form.setSearchTerms(sterms);
 		form.show(writer);
 	}
 	
 	public static List<String> timeListHTMLGenerator(String selectedTime) {
 		List<String> returnList = new ArrayList<String>();
 		String returnValue, hourCount, minCount;
 		for (int i = 0; i < 24; i++) {
 			if (i < 10) {
 				hourCount = "0" + i;
 			}
 			else {
 				hourCount = i + "";
 			}
 			for (int j = 0; j <= 1; j++) {
 				if (j == 0) {
 					minCount = "00";
 				}
 				else {
 					minCount = "30";
 				}
 				returnValue = hourCount + ":" + minCount;
 				
 				if (returnValue.equals(selectedTime)) {
 					returnList.add("<option value=\"" + returnValue + "\" selected=\"selected\">" + returnValue + "</option>");
 				}
 				else {
 					returnList.add("<option value=\"" + returnValue + "\">" + returnValue + "</option>");
 				}
 			}
 			
 		}
 		return returnList;	
 	}
 	
 	public static List<String> locationListHTMLGenerator(String selectedLocation, List<Location> locationList) {
 		List<String> returnList = new ArrayList<String>();
 		for (Location location : locationList) {
 			if (location.getLocationCode().equals(selectedLocation)) {
 				returnList.add("<option value=\"" + location.getLocationCode() + "\" selected=\"selected\">" + location.getLocationName() + "</option>"); 
 			}
 			else {
 				returnList.add("<option value=\"" + location.getLocationCode() + "\">"+ location.getLocationName() + "</option>"); 
 			}
 		}
 		return returnList;
 	}
 	
 	private void setDefaultSearchVariables(HttpServletRequest req, PrintWriter writer) throws IOException {
 		TimeSelectForm tform = new TimeSelectForm(req);
 		LocationDao locationdao = new LocationDaoImpl();
 		tform.setFormVariables(timeListHTMLGenerator("17:00"), locationListHTMLGenerator("NULL", locationdao.getLocations()));
 		tform.show(writer);
 	}
 }
