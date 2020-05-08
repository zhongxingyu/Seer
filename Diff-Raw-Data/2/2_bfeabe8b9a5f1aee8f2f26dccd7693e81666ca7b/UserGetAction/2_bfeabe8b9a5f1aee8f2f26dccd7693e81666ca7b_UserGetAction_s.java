 /**
  * 
  */
 package com.globalmesh.action.user;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.globalmesh.dao.MovieDetailDAO;
 import com.globalmesh.dao.SaleDAO;
 import com.globalmesh.dao.UserDAO;
 import com.globalmesh.dto.BookingDetails;
 import com.globalmesh.dto.Sale;
 import com.globalmesh.dto.User;
 import com.globalmesh.util.Constants;
 import com.globalmesh.util.Utility;
 
 /**
  * @author Dil
  *
  */
 @SuppressWarnings("serial")
 public class UserGetAction extends HttpServlet {
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		String email = (String) req.getSession().getAttribute("email");
 		if(email == null){
 			req.setAttribute("msgClass", Constants.MSG_CSS_ERROR);
 			req.setAttribute("message", Utility.getCONFG().getProperty(Constants.LOGIN_NEED_MESSAGE));
 			req.getRequestDispatcher("/messages.jsp").forward(req, resp);
 		} else {
 			User user = null;
 			
 			user = UserDAO.INSTANCE.getUserByEmail(email);
 			
 			Calendar from = Calendar.getInstance();
 			from.add(Calendar.DATE, -1);
 			Calendar to = Calendar.getInstance();
			to.add(Calendar.DATE, 15);
 			
 			DateFormat showDateFormat = new SimpleDateFormat("yyyy-MM-dd");
 			DateFormat showTimeFormat = new SimpleDateFormat("hh:mm a");
 			
 			List<Sale> userSale = SaleDAO.INSTANCE.listSalesFromTOByUser(from.getTime(), to.getTime(), user.getUserId());
 			List<BookingDetails> bookings = new ArrayList<BookingDetails>(userSale.size());
 			
 			for (Sale sale : userSale) {
 				BookingDetails b = new BookingDetails();
 				b.setShowDate(showDateFormat.format(sale.getShowDate()));
 				b.setShowTime(showTimeFormat.format(sale.getShowDate()));
 				b.setTransactionDate(showDateFormat.format(sale.getTransactionDate()));
 				b.setSeatNumbers(sale.getSeats());
 				b.setMovieName(MovieDetailDAO.INSTANCE.getMovieById(sale.getMovie()).getMovieName());
 				b.setSaleId(sale.getId());
 				
 				bookings.add(b);
 			}
 			
 			req.setAttribute("bookings", bookings);
 			req.setAttribute("user", user);
 			req.setAttribute("msgClass", Constants.MSG_CSS_INFO);
 			req.setAttribute("message", Utility.getCONFG().getProperty(Constants.USER_PROFILE_UPDATE_INFO));
 			req.getRequestDispatcher("/profile.jsp").forward(req, resp);
 		}
 	}
 }
