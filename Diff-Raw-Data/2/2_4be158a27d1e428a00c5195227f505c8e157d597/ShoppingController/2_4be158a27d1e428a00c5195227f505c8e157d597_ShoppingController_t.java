 package controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import model.Order;
 import model.Ordered;
 import model.Product;
 import model.User;
 import dao.OrderDao;
 import dao.ProductDao;
 import dao.UserDao;
 
 /**
  * Servlet implementation class ShoppingController
  */
 
 public class ShoppingController extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static String SHOPPING_CART = "/cart.jsp";
 	private static String CONFIRM_PAYMENT = "/confirm_payment.jsp";
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public ShoppingController() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		ProductDao dao = new ProductDao();
 		HttpSession session = request.getSession();
 		// Get old cart
		List<Ordered> cart = ((ArrayList<Ordered>) session
 				.getAttribute("shopping_cart"));
 
 		String action = request.getParameter("action");
 		// Add to the cart
 		if (action != null && action.equals("add")) {
 			try {
 				int id_product_to_buy = Integer.parseInt(request
 						.getParameter("id"));
 				List<Product> product_to_buy = dao
 						.getProductById(id_product_to_buy);
 				request.setAttribute("product_to_buy", product_to_buy.get(0));
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			}
 		}
 		request.setAttribute("cart", cart);
 
 		RequestDispatcher view = request.getRequestDispatcher(SHOPPING_CART);
 		view.forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		String forward = SHOPPING_CART;
 		ProductDao pdao = new ProductDao();
 		HttpSession session = request.getSession();
 		// Get old cart
 		List<Ordered> cart = ((ArrayList<Ordered>) session
 				.getAttribute("shopping_cart"));
 		String user_name = (String) session.getAttribute("name");
 		if (cart == null) {
 			cart = new ArrayList<Ordered>();
 		}
 		String action = request.getParameter("action");
 		// Add to the cart
 
 		if (action != null && action.equals("add_to_cart")) {
 			try {
 				int quantity = Integer.parseInt(request.getParameter("quantity"));
 				int id_product_buying = Integer.parseInt(request
 						.getParameter("id"));
 				// Create the order
 				Ordered ordered = new Ordered();
 				Product p = pdao.getProductById(id_product_buying).get(0);
 				ordered.setProduct(p);
 				ordered.setQuantity(quantity);
 				// Add it to the cart
 				cart.add(ordered);
 				session.setAttribute("shopping_cart", cart);
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			}
 			request.setAttribute("cart", cart);
 		}
 
 		if (action != null && action.equals("confirm_payment")) {
 			forward = CONFIRM_PAYMENT;
 			OrderDao odao = new OrderDao();
 			UserDao udao = new UserDao();
 			int order_pk = odao.createOrder(udao.getUser(user_name));
 			// Iterate over the cart
 			for (Ordered ordered : cart) {
 				odao.addProduct(ordered.getProduct(), ordered.getQuantity(), order_pk);
 			}
 			
 			request.setAttribute("last_cart", cart);
 			// Clean the cart
 			session.removeAttribute("shopping_cart");
 		}		
 
 		RequestDispatcher view = request.getRequestDispatcher(forward);
 		view.forward(request, response);
 	}
 
 }
