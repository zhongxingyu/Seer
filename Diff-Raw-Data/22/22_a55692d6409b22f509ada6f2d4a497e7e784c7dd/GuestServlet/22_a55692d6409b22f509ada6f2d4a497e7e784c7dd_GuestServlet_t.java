 package guest;
 
 import java.io.IOException;
 import javax.ejb.EJB;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @WebServlet(name = "GuestServlet", urlPatterns = { "/guest", "", "/" })
 public class GuestServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	// Injected DAO EJB:
 	@EJB
 	GuestDao guestDao;
 	
 	@EJB
 	HotelDao hotelDao;
 
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		// Display the list of guests
 		request.setAttribute("count", guestDao.getCountByDefaultObject());
 		request.setAttribute("hotels", hotelDao.getAllHotels());
 		request.setAttribute("guests", guestDao.getAllGuests());
 		request.getRequestDispatcher("/guest.jsp").forward(request, response);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		// Handle a new guest:
 		String name = request.getParameter("name");
 		if (request.getParameter("Add") != null) {
 			if (name != null) {
 				String option = request.getParameter("Option");
 				if (option.equals("Regular")) {
 					guestDao.persist(new RegularGuest(name, new Address("default regular address for simplicity")));
 				} else {
 					guestDao.persist(new VipGuest(name, "default vip status for simplicity"));
 				}
 			}
 		} else if (request.getParameter("Delete") != null) {
 			if (name != null)
 				guestDao.deleteByName(name);
 
 			
 		} else if (request.getParameter("Update") != null) {
 			if (name != null) {
 				String newName = name;
 				String id = request.getParameter("id");
 				if (id != null) {
 					guestDao.updateNameById(id, newName);
 				}
 			}
		} else if (request.getParameter("Search") != null) {
			if (name != null) {
				request.setAttribute("searchResult", guestDao.searchHotelByCustomerName(name));
			}
 		}
 		// Display the list of guests:
 		doGet(request, response);
 	}
 
 }
