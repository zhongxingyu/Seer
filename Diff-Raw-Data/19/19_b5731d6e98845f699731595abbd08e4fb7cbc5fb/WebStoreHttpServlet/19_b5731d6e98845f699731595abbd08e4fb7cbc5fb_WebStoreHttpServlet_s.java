 package il.ac.telhai.finalProject;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Logger;
 import org.apache.log4j.SimpleLayout;
 import java.io.IOException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import java.util.List;
 
 public class WebStoreHttpServlet extends HttpServlet {
 	private static final long serialVersionUID = -8060931133910859939L;
 	public static final String USER_COOKIE_NAME = "UserCookie";
 	private static Logger logger;
 	static {
 		System.out.println("in the static block");
 		logger = Logger.getLogger("WebStoreHttpServletLogger");
 		BasicConfigurator.configure();
         try
         {
             logger.addAppender(new FileAppender(new SimpleLayout(), "C:\\Users\\eyal\\workspace\\WebStoreHttpServletLogger.txt"));
             logger.info("start logging");
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 	}
 	
 	public void doPost(HttpServletRequest request, HttpServletResponse response)throws IOException, ServletException {
 		String uri = request.getRequestURI();
 		logger.info(" # # # # # # # # # # # # # # # # # # # # # # # # # # # ");
 		logger.info("-I- New URI request(method post): " + uri);
 		
 		if (uri.equals("/finalProjectV1.0/academic/UserLogin/")) {
 			login(request, response);
 		}
 	}
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException, ServletException {
 		String uri = request.getRequestURI();
 		logger.info(" # # # # # # # # # # # # # # # # # # # # # # # # # # # ");
 		logger.info("-I- New URI request(method get): " + uri);
 		
 		 if (uri.equals("/finalProjectV1.0/academic/sendRequestForProduct/"))	{
 			sendRequestForProduct(request, response);
 		} else if (uri.equals("/finalProjectV1.0/academic/sendRequestForProducts/"))	{
 			sendRequestForProducts(request, response);
 		} else if (uri.equals("/finalProjectV1.0/academic/addProduct/"))	{
 			addProduct(request, response);
 		} else if (uri.equals("/finalProjectV1.0/academic/showAllMyProducts/"))	{
 			showAllMyProducts(request, response);
 		} else if (uri.equals("/finalProjectV1.0/academic/SignOut/"))	{
 			logout(request, response);
 		} else if (uri.equals("/finalProjectV1.0/academic/HomePage/"))	{
 			homepage(request, response);
 		} else if (uri.equals("/finalProjectV1.0/academic/Camera/"))	{
 			getServletContext().getRequestDispatcher("/examples/04-jquery-mobile.html").forward(request, response);
 		} else {
			forwardToLoginPage(request, response);
 		}
 	}
 	
 	
 	private void homepage(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		response.sendRedirect("/finalProjectV1.0/home.jsp");
//		getServletContext().getRequestDispatcher("/home.jsp").forward(request, response);
 	}
 	
 	private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		logger.info("user signout started");
 		Cookie userCookie = getUserCookie(request);
 		if (userCookie == null)	{
 			logger.info("Could not find USER_COOKIE_NAME("+USER_COOKIE_NAME+")");
 			return;
 		}
 		userCookie.setMaxAge(0);
 		userCookie.setPath("/");
 		response.addCookie(userCookie);
 		request.getSession().invalidate();
 		homepage(request, response);
 		logger.info("user signout finished");
 	}
 	
 	private void login(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		logger.info(" # # # # # # # # # # # # # # # # # # # # # # # # # # # ");
 		logger.info("-I- new login attempt");
 		try {	
 			WebStoreDAO db  = WebStoreDAO.getProductHybernateDAO();
 			User loginUser = new User(request.getParameter("User"),  request.getParameter("Password"));
 			if (! db.getExistingUser(loginUser.getName()) || ! db.validateUserPassword(loginUser))	{
 				logger.error("ERROR: wrong user name or password");
 				request.getSession().setAttribute("comments", "incorrect  username or password");
 				request.setAttribute("comments", "incorrect  username or password");
 				forwardToLoginPage(request, response);
 				return;
 			}
 			
 			// if we got here username and password are correct
 			setUserCookie(request, response, loginUser.getName());
 			response.sendRedirect("/finalProjectV1.0/home.jsp");
 		} catch (WebStoreDAOException e) {
 			e.printStackTrace();
 		} catch (ServletException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void forwardToLoginPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
 		getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
//		response.sendRedirect("/finalProjectV1.0/login.jsp");
 	}
 	
 	private void addProduct(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		try {
 			WebStoreDAO db  = WebStoreDAO.getProductHybernateDAO();
 			Integer productId = Integer.parseInt(request.getParameter("productId"));
 			Product product = db.getProduct(productId);
 			HttpSession session = request.getSession();
 			String shoppingCartAttributeName = "shoppingCart";
 			ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute(shoppingCartAttributeName);
 			if (shoppingCart == null)	{
 				shoppingCart = new ShoppingCart();
 				shoppingCart.addProduct(product);
 			}	else	{
 				shoppingCart.addProduct(product);
 			}
 			session.setAttribute(shoppingCartAttributeName , shoppingCart);
 		} catch (WebStoreDAOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void showAllMyProducts(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		try {
 			HttpSession session = request.getSession();
 			String shoppingCartAttributeName = "shoppingCart";
 			ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute(shoppingCartAttributeName);
 			request.setAttribute(shoppingCartAttributeName, shoppingCart);
 			
 			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/shoppingCart.jsp");
 			dispatcher.forward(request, response);
 		} catch (ServletException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void sendRequestForProduct(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		try {
 			WebStoreDAO db  = WebStoreDAO.getProductHybernateDAO();
 			Integer productId = Integer.parseInt(request.getParameter("productId"));
 			Product product = db.getProduct(productId);
 			logger.info("-I- Product info: " + product.toString());
 			request.setAttribute("product", product);
 			
 			response.sendRedirect("/finalProjectV1.0/product.jsp");
 //			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/product.jsp");
 //			dispatcher.forward(request, response);
 			
 		} catch (WebStoreDAOException e) {
 			e.printStackTrace();
 		} 
 //		catch (ServletException e) {
 //			e.printStackTrace();
 //		} 
 	}
 	
 	private void sendRequestForProducts(HttpServletRequest request, HttpServletResponse response) throws IOException
 	{
 		try {
 			WebStoreDAO db  = WebStoreDAO.getProductHybernateDAO();
 			logger.info("-I- Request for all products");
 			List<Product> products = db.getProducts();
 			logger.info("-I- Product info: " + products.toString());
 			request.setAttribute("products", products);
 			
 			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/products.jsp");
 			dispatcher.forward(request, response);				
 		} catch (WebStoreDAOException e) {
 			e.printStackTrace();
 		} catch (ServletException e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	/**
 	 * @param request HttpServletRequest object represent the request
 	 * @return the cookie corresponding to the cookie represent the user
 	 */
 	private Cookie getUserCookie(HttpServletRequest request) {
 		Cookie[] cookies = request.getCookies();
 		if (null != cookies)
 			for (int i = 0; i < cookies.length; i++)
 				if (cookies[i].getName().equals(USER_COOKIE_NAME))
 					return (Cookie) cookies[i];
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param request HttpServletRequest object represent the request
 	 * @param response HttpServletResponse object represent the response
 	 * @param loginName the login name to assign to the cookie
 	 * @return true on success else false when the cookie already were assigned 
 	 */
 	private boolean setUserCookie(HttpServletRequest request, HttpServletResponse response, String loginName) {
 		logger.info("setUserCookie started");
 		Cookie[] cookies = request.getCookies();
 		for (int i = 0; i < cookies.length; i++)
 			if (cookies[i].getName().equals(USER_COOKIE_NAME))	{
 				if (cookies[i].getValue().equals(loginName))	{
 					logger.info("setUserCookie finished - cookie already exists");
 				}	else	{
 					logger.info("setUserCookie finished - username has been modified");
 					cookies[i].setValue(loginName);
 				}
 				return false;
 			}
 		// make sure we are not inserting this cookie again
 		Cookie cookie = new Cookie(USER_COOKIE_NAME, loginName);
 		cookie.setPath("/");
 		cookie.setMaxAge(60*60*24*7); //one hour
 		response.addCookie(cookie);
 		logger.info("setUserCookie finished - new cookie has been added");
 		return true;
 	}
 }
