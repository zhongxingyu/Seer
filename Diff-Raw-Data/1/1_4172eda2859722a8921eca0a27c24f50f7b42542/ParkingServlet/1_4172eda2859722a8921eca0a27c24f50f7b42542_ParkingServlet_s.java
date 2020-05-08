 package no.europark.parkingBooking;
 
 import java.io.IOException;  
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import no.europark.parkingBooking.dao.LocationDao;
 import no.europark.parkingBooking.dao.LocationDaoMock;
 import no.europark.parkingBooking.dao.ParkingPlaceDao;
 import no.europark.parkingBooking.dao.ParkingPlaceDaoMock;
 import no.europark.parkingBooking.entity.Booking;
 import no.europark.parkingBooking.entity.Location;
 import no.europark.parkingBooking.entity.ParkingPlace;
 import no.europark.parkingBooking.entity.TimeSpan;
 import no.europark.parkingBooking.entity.User;
 
 public class ParkingServlet extends HttpServlet{
 
 	private static final long serialVersionUID = 1125844790608528788L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		 resp.setContentType("text/html");
 		 PrintWriter writer = resp.getWriter();
 		 if (req.getPathInfo().equals("/timeselect.html")){
 			 setDefaultSearchVariables(req, writer);
 		 }
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		resp.setContentType("text/html");
 		PrintWriter writer = resp.getWriter();
 		if (req.getPathInfo().equals("/parkingoptions.html")) {
 			try {
 				showParkingOptions(req, writer);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else if (req.getPathInfo().equals("/login.html")) {
 			addParkingPlaceToBooking(req);
 			LoginForm lform = new LoginForm(req);
 			lform.show(writer);
 		} else if (req.getPathInfo().equals("/createuser.html")) {
 			CreateUserForm cform = new CreateUserForm(req);
 			cform.show(writer);
 		} else if (req.getPathInfo().equals("/payment.html")) {
 			addUserToBooking(req);
 			PaymentForm pform = new PaymentForm(req);
 			Booking booking = getBooking(req);
 			pform.setUser(booking.getUser());
 			pform.setLocation(booking.getLocation());
 			pform.setParkingPlace(booking.getParkingPlace());
 			try {
 				pform.setDateFrom(Utility.dateToString(booking.getTimeSpan().getDateFrom()));
 				pform.setDateTo(Utility.dateToString(booking.getTimeSpan().getDateTo()));
 				pform.setHoursFrom(booking.getTimeSpan().getHoursFrom());
 				pform.setHoursTo(booking.getTimeSpan().getHoursTo());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			pform.setTotalPrice(booking.getTimeSpan(), booking.getParkingPlace());
 			pform.show(writer);
 		} else if (req.getPathInfo().equals("/receipt.html")) {
 			ReceiptForm rform = new ReceiptForm(req);
 			Booking booking = getBooking(req);
 			rform.setUser(booking.getUser());
			System.out.println("USER" + booking.getUser().getEmail());
 			rform.setLocation(booking.getLocation());
 			rform.setParkingPlace(booking.getParkingPlace());
 			try {
 				rform.setDateFrom(Utility.dateToString(booking.getTimeSpan().getDateFrom()));
 				rform.setDateTo(Utility.dateToString(booking.getTimeSpan().getDateTo()));
 				rform.setHoursFrom(booking.getTimeSpan().getHoursFrom());
 				rform.setHoursTo(booking.getTimeSpan().getHoursTo());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			rform.setTotalPrice(booking.getTimeSpan(), booking.getParkingPlace());
 			rform.show(writer);
 		}
 	}
 	
 	private void addTimeSpanToBooking(HttpServletRequest req, TimeSpan timeSpan) {
 		HttpSession session = req.getSession(true);
         Booking newBooking = new Booking(timeSpan, null, null, null);
         session.setAttribute("Booking", newBooking);
 	}
 	
 	private void addLocationToBooking(HttpServletRequest req, Location location) {
 		HttpSession session = req.getSession(true);
 		Booking booking = (Booking) session.getAttribute("Booking");
         if (booking != null) {
         	booking.setLocation(location);
             session.setAttribute("Booking", booking);
         }
 	}
 	
 	private void addParkingPlaceToBooking(HttpServletRequest req) {
 		String parkingPlaceName = req.getParameter("parkingPlace");
 		ParkingPlaceDao parkingDao = new ParkingPlaceDaoMock();
 		ParkingPlace parkingPlace = parkingDao.getParkingPlace(parkingPlaceName);
 		
 		HttpSession session = req.getSession(true);
 		Booking booking = (Booking) session.getAttribute("Booking");
         if (booking != null) {
         	booking.setParkingPlace(parkingPlace);
             session.setAttribute("Booking", booking);
         }
 	}
 	
 	private void addUserToBooking(HttpServletRequest req) {
 		HttpSession session = req.getSession(true);
 		Booking booking = (Booking) session.getAttribute("Booking");
         if (booking != null) {
         	User user = new User(req.getParameter("email"), req.getParameter("firstname"), req.getParameter("lastname"), req.getParameter("pwd"), req.getParameter("mobilephone"));
         	booking.setUser(user);
             session.setAttribute("Booking", booking);
         }
 	}
 	
 	private Booking getBooking(HttpServletRequest req) {
 		HttpSession session = req.getSession(true);
 		Booking booking = (Booking) session.getAttribute("Booking");
 		return booking;
 	}
 	
 	private void showParkingOptions(HttpServletRequest req, PrintWriter writer) throws Exception {
 		Date dateFrom = Utility.stringToDate(req.getParameter("dateFrom")); 
 		Date dateTo = Utility.stringToDate(req.getParameter("dateTo"));
 		String hoursFrom = req.getParameter("hoursFrom");
 		String hoursTo = req.getParameter("hoursTo");
 		TimeSpan timeSpan = new TimeSpan(dateFrom, dateTo, hoursFrom, hoursTo);
 		addTimeSpanToBooking(req, timeSpan);
 		
 		String locationCode = req.getParameter("location");
 		LocationDao locationDao = new LocationDaoMock();
 		Location loc = locationDao.getLocation(locationCode);
 		addLocationToBooking(req, loc);
 		
 		ParkingPlaceDao parkingPlaceDao = new ParkingPlaceDaoMock();
 
 		ParkingOptionsForm form = new ParkingOptionsForm(req);
 		form.setSearchVariables(req.getParameter("dateFrom"),
 							timeListHTMLGenerator(req.getParameter("hoursFrom")),
 							req.getParameter("dateTo"),
 							timeListHTMLGenerator(req.getParameter("hoursTo")),
 							locationListHTMLGenerator(locationCode, locationDao.getLocations()));
 		form.setParkingPlaces(parkingPlaceDao.getParkingPlaces(locationCode, timeSpan), Utility.daysBetween(dateFrom, dateTo));
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
 		LocationDao locationdao = new LocationDaoMock();
 		tform.setFormVariables(timeListHTMLGenerator("17:00"), locationListHTMLGenerator("NULL", locationdao.getLocations()));
 		tform.show(writer);
 	}
 }
