 package org.jcandystore.views;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.math.BigDecimal;
 import java.util.Date;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.jcandystore.model.Orders;
 import org.jcandystore.services.OrdersService;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class CheckoutServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
 	protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws IOException {
 		PrintWriter out = resp.getWriter();
 		resp.setContentType("text/html;charset=UTF-8");
 
 		// retrieve connected user
 		UserService userService = UserServiceFactory.getUserService();
 		User user = userService.getCurrentUser();
 		
 		if (user == null) {
 			resp.sendRedirect(userService.createLoginURL(request.getRequestURI()));
 		}
 		
 		OrdersService orderService = new OrdersService();
 
 		// retrieve grand total
 		HttpSession session = request.getSession();
 		// check for empty session/cart
 		if ( !session.getAttributeNames().hasMoreElements() ) {
 			out.println("<h2>Empty cart!</h2>");
 			out.println("<br/><a href=\"/\">Back Home</a>");			
 		}
 		
 		// retrieve grandTotal (rather than recalculate)
 		Float grandTotal = (Float)session.getAttribute("grandTotal");		
 
 		// create empty JPA entity
 		Orders newOrder = new Orders();
 		newOrder.setTotalPrice(new BigDecimal(grandTotal));
 		
 		newOrder.setUserId(user.getEmail());
 		
 		int id = (user.getUserId()+new Date()).hashCode();
 		newOrder.setOrderId(Math.abs(id));
 
 		// (leave other attributes empty)
 		
 		System.out.println("about to persist Order : " + newOrder);
 		
 		// persist object using JPA
 		orderService.create(newOrder);
 		
 		out.println("<h2>Order taken!</h2>");
 		out.println("<h4>Now processing shipping...</a>");
 		out.println("<br/><p><a href=\"/\">Back Home</a></p>");
 		
 		// Send detailed order (content of session) to backoffice queue for shipping
 		// Work with LineItems, ... TODO ...
 		
 		// clear session
 		session.invalidate();
 	}
 
 }
