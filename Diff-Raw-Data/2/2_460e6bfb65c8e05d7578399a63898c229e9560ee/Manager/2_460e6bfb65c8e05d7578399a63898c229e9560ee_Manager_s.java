 package servlets;
 
 /*
  * Shop.java
  *
  */
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import beans.*;
 
 /**
  * 
  * @author Fredrik �lund, Olle Eriksson
  * @version 1.0
  */
 public class Manager extends HttpServlet {
 
 	private static String jdbcURL = null;
 	private static String manager_page = null;
 	private static String user_page = null;
 	private static String show_products_page = null;
 	private static String show_components_page = null;
 	private static String profile_page = null;
 	private static String detail_page = null;
 	private static String add_product_page = null;
 	private static String add_component_page = null;
 
 	private CompleteProductListBean productList = null;
 	private ComponentListBean componentList = null;
 
 	/**
 	 * Initializes the servlet.
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config); 
 
 		jdbcURL 			 = config.getInitParameter("JDBC_URL");
 		manager_page 		 = config.getInitParameter("MANAGER_PAGE");
 		user_page 			 = config.getInitParameter("USER_PAGE");
 		show_products_page	 = config.getInitParameter("SHOW_PRODUCTS_PAGE");
 		show_components_page = config.getInitParameter("SHOW_COMPONENTS_PAGE");
 		profile_page		 = config.getInitParameter("PROFILE_PAGE");
 		detail_page			 = config.getInitParameter("DETAIL_PAGE");
 		add_product_page 	 = config.getInitParameter("ADD_PRODUCT_PAGE");
 		add_component_page 	 = config.getInitParameter("ADD_COMPONENT_PAGE");
 		
 
 		// get the books from the database using a bean
 		try {
 			this.productList = new CompleteProductListBean(jdbcURL);
 			this.componentList = new ComponentListBean(jdbcURL);
 		} catch (Exception e) {
 			throw new ServletException(e);
 		}
 
 		// servletContext is the same as scope Application
 		// store the productList in application scope
 		ServletContext sc = getServletContext();
 		sc.setAttribute("productList", this.productList);
 		sc.setAttribute("componentList", this.componentList);
 	}
 
 	/**
 	 * Destroys the servlet.
 	 */
 	public void destroy() {
 
 	}
 
 	/**
 	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
 	 * methods.
 	 * 
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 */
 	protected void processRequest(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException,
 			java.io.IOException {
 
 		HttpSession sess = request.getSession();
 		RequestDispatcher rd = null;
 		sess.setAttribute("currentUser", request.getRemoteUser());
 		sess.setAttribute("jdbcURL", jdbcURL);
 		sess.setAttribute("usertype", "manager");
 
 		// check if we should turn on debug
 
 		String debug = request.getParameter("debug");
 		if (debug != null && debug.equals("on"))
 			sess.setAttribute("debug", "on");
 		else if (debug != null && debug.equals("off"))
 			sess.removeAttribute("debug");
 
 		
 
 		/************************************************************/
 		/*						START PAGE							*/
 		/************************************************************/
 		if (request.getParameter("action") == null
 				|| request.getParameter("action").equals("manager")) {
 			if (sess.getAttribute("currentUser") != null) {
 				ProfileBean p = new ProfileBean(jdbcURL);
 				try {
 					p.populate((String) sess.getAttribute("currentUser"));
 				} catch (Exception e) {
 					throw new ServletException("Error loading profile", e);
 				}
 				sess.setAttribute("profile", p);
 			}
 			rd = request.getRequestDispatcher(manager_page);
 			rd.forward(request, response);
 		}
 
 		
 		/************************************************************/
 		/*			SHOW PAGE (COMPONENTs / PRODUCTs)				*/
 		/************************************************************/
 		else if(request.getParameter("action").equals("manage")) {
 			if(request.getParameter("type") == null ||
 					request.getParameter("type").equals("product")) {
 					rd = request.getRequestDispatcher(show_products_page);
 					rd.forward(request, response);
 			}
 			else if(request.getParameter("type").equals("components")) {
 				rd = request.getRequestDispatcher(show_components_page);
 				rd.forward(request, response);
 			}
 			else
 				throw new ServletException("Trying to access something not existing.");
 		}
 		
 		
 		/************************************************************/
 		/*						ADD PRODUCT							*/
 		/************************************************************/
 		else if (request.getParameter("action").equals("addProduct")) {
 			if(request.getParameter("save") != null &&
 					request.getParameter("save").equals("true")) {
 
 				CompleteProductBean bb = new CompleteProductBean();
 				bb.setDescription(request.getParameter("description"));
 				bb.setProduct(request.getParameter("product"));
				bb.setVisbile(true);
 				bb.setProfit(Integer.valueOf(request.getParameter("profit")));
 				bb.add(jdbcURL);
 				
 			}
 			rd = request.getRequestDispatcher(add_product_page);
 			rd.forward(request, response);
 		}
 
 		
 		/************************************************************/
 		/*				UPDATE PRODUCT / COMPONENT					*/
 		/************************************************************/
 		else if (request.getParameter("action").equals("update")) {
 			if(request.getParameter("productid") != null
 					&& request.getParameter("product") != null
 					&& request.getParameter("description") != null
 					&& request.getParameter("profit") != null) {
 				String product = request.getParameter("product");
 				String description = request.getParameter("description");
 				boolean visible = false;
 				if(request.getParameter("visible") != null
 						&& request.getParameter("visible").equals("true")) {
 					visible = true;
 				}
 				int profit = 0;
 				try {
 					profit = Integer.parseInt(request.getParameter("profit"));
 				} catch (NumberFormatException e) {
 					throw new ServletException("Illegal profit specified");
 				}
 				
 				try {
 					System.out.println("Pid: " + request.getParameter("productid"));
 					System.out.println("Des: " + request.getParameter("description"));
 					System.out.println("Tit: " + request.getParameter("product"));
 					System.out.println("Pro: " + request.getParameter("profit"));
 					if(visible)
 						System.out.println("Vis: true");
 					else
 						System.out.println("Vis: false");
 					this.productList.updateProduct(
 							Integer.parseInt(request.getParameter("productid")),
 							product, description, visible, profit);
 				} catch (Exception e) {
 					throw new ServletException(e);
 				}
 				rd = request.getRequestDispatcher(show_products_page);
 				rd.forward(request, response);
 			}
 			else if (request.getParameter("componentid") != null) {
 				
 			}
 			else {
 				throw new ServletException("False update request");
 			}
 		}
 		
 		
 		
 		
 		/************************************************************/
 		/*				ADD COMPONENT TO PRODUCT					*/
 		/************************************************************/
 		else if(request.getParameter("action").equals("addComponent")) {
 			if(request.getParameter("productid") != null
 					&& request.getParameter("componentid") != null
 					&& request.getParameter("quantity") != null) {
 				int q = Integer.parseInt(request.getParameter("quantity"));
 				int pid = Integer.parseInt(request.getParameter("productid"));
 				int cid = Integer.parseInt(request.getParameter("componentid"));
 				try {
 					this.productList.addComponent(pid, cid, q);
 				} catch(Exception e) {
 					throw new ServletException(e);
 				}
 			} else {
 				throw new ServletException(
 						"No productid, componentid or quantity when add component to product");
 			}
 			
 			rd = request.getRequestDispatcher(
 					"manager?action=detail&productid=" + request.getParameter("productid"));
 			rd.forward(request, response);
 		}
 		
 		
 		
 		
 		/************************************************************/
 		/*				REMOVE COMPONENT FROM PRODUCT				*/
 		/************************************************************/
 		else if (request.getParameter("action").equals("removeComponent")) {
 			if (request.getParameter("productid") != null
 					&& request.getParameter("quantity") != null
 					&& request.getParameter("componentid") != null) {
 				int q = Integer.parseInt(request.getParameter("quantity"));
 				int pid = Integer.parseInt(request.getParameter("productid"));
 				int cid = Integer.parseInt(request.getParameter("componentid"));
 				try {
 					this.productList.removeComponent(pid, cid, q);
 				} catch (Exception e) {
 					throw new ServletException(e);
 				}
 			} else {
 				throw new ServletException(
 						"No productid, componentid or quantity when removing component from product");
 			}
 			rd = request.getRequestDispatcher(
 					"manager?action=detail&productid=" + request.getParameter("productid"));
 			rd.forward(request, response);
 		}
 
 		
 		/************************************************************/
 		/*						PRODUCT DETAILS						*/
 		/************************************************************/
 		else if (request.getParameter("action").equals("detail")) {
 			if (request.getParameter("productid") != null) {
 				CompleteProductBean cpb = this.productList.getById(
 						Integer.parseInt(request.getParameter("productid")));
 				request.setAttribute("product", cpb);
 			} else {
 				throw new ServletException("No productid when viewing detail");
 			}
 			rd = request.getRequestDispatcher(detail_page);
 			rd.forward(request, response);
 		}
 
 		
 		/************************************************************/
 		/*					SAVE PRODUCT / COMPONENT				*/
 		/************************************************************/
 		else if (request.getParameter("action").equals("save")) {
 			
 			
 
 			// if we have a shoppingcart, verify that we have
 			// valid userdata, then create an orderbean and
 			// save the order in the database
 
 			/*
 			 * if (shoppingCart != null && request.getParameter("shipping_name")
 			 * != null && request.getParameter("shipping_city") != null &&
 			 * request.getParameter("shipping_zipcode") != null &&
 			 * request.getParameter("shipping_address") != null){ OrderBean ob =
 			 * new OrderBean(jdbcURL, shoppingCart,
 			 * request.getParameter("shipping_name").trim(),
 			 * request.getParameter("shipping_address").trim(),
 			 * request.getParameter("shipping_zipcode").trim(),
 			 * request.getParameter("shipping_city").trim()); try{ String check
 			 * = ob.saveOrder(); if (!check.equals("")) throw new
 			 * ServletException(check, new Exception()); } catch(Exception e){
 			 * throw new ServletException("Error saving order", e); } } else{
 			 * throw new ServletException(
 			 * "Not all parameters are present or no " +
 			 * " shopping cart when saving book"); }
 			 */
 			rd = request.getRequestDispatcher(manager_page);
 			rd.forward(request, response);
 		}
 
 
 		/************************************************************/
 		/*						CHECKOUT							*/
 		/************************************************************/
 		else if (request.getParameter("action").equals("checkout")) {
 			if (sess.getAttribute("currentUser") != null) {
 				ProfileBean p = new ProfileBean(jdbcURL);
 				try {
 					p.populate((String) sess.getAttribute("currentUser"));
 				} catch (Exception e) {
 					throw new ServletException("Error loading profile", e);
 				}
 				sess.setAttribute("profile", p);
 			}
 			response.sendRedirect(manager_page);
 		}
 
 		
 		/************************************************************/
 		/*						LOGOUT								*/
 		/************************************************************/
 		else if (request.getParameter("action").equals("logout")) {
 			sess.invalidate();
 			response.sendRedirect("manager");
 		}
 
 		
 		/************************************************************/
 		/*						PROFILE PAGE						*/
 		/************************************************************/
 		else if (request.getParameter("action").equals("profile")) {
 			HashMap<String, Boolean> role = null;
 			ProfileBean p = new ProfileBean(jdbcURL);
 			try {
 				p.populate((String) sess.getAttribute("currentUser"));
 				role = p.getRoles();
 			} catch (Exception e) {
 				throw new ServletException("Error loading profile", e);
 			}
 			sess.setAttribute("profile", p);
 			Set<String> k = role.keySet();
 			Iterator<String> i = k.iterator();
 			while (i.hasNext()) {
 				String st = i.next();
 				if (request.isUserInRole(st))
 					role.put(st, true);
 			}
 			p.setRole(role);
 			sess.setAttribute("roles", role);
 			rd = request.getRequestDispatcher(manager_page);
 			rd.forward(request, response);
 		}
 		
 
 		/************************************************************/
 		/*						UPDATE PROFILE						*/
 		/************************************************************/
 		else if (request.getParameter("action").equals("profilechange")
 				|| request.getParameter("action").equals("usercreate")) {
 			ProfileBean pb = (ProfileBean) sess.getAttribute("profile");
 			String u;
 			if (request.getParameter("action").equals("profilechange"))
 				u = (String) sess.getAttribute("currentUser");
 			else
 				u = request.getParameter("user");
 			
 			String p1 = request.getParameter("password");
 			String p2 = request.getParameter("password2");
 			String name = request.getParameter("name");
 			String street = request.getParameter("street");
 			String zip = request.getParameter("zip");
 			String city = request.getParameter("city");
 			String country = request.getParameter("country");
 
 			pb.setUser(u);
 			pb.setPassword(p1);
 			pb.setName(name);
 			pb.setStreet(street);
 			pb.setZip(zip);
 			pb.setCity(city);
 			pb.setCountry(country);
 			HashMap<String, Boolean> r =
 					(HashMap<String, Boolean>) sess.getAttribute("roles");
 			Set<String> k = r.keySet();
 			Iterator<String> i = k.iterator();
 			while (i.hasNext()) {
 				String st = i.next();
 				String res = request.getParameter(st);
 				if (res != null)
 					r.put(st, true);
 				else
 					r.put(st, false);
 			}
 			pb.setRole(r);
 
 			// if this a new user, try to add him to the database
 			if (request.getParameter("action").equals("usercreate")) {
 				boolean b;
 				// make sure the the username is not used already
 				try {
 					b = pb.testUser(u);
 				} catch (Exception e) {
 					throw new ServletException("Error loading user table", e);
 				}
 				if (b) {
 					sess.setAttribute("passwordInvalid",
 							"User name already in use");
 					rd = request.getRequestDispatcher(manager_page);
 					rd.forward(request, response);
 					// note that a return is needed here because forward
 					// will not cause our servlet to stop execution, just
 					// forward the request processing
 					return;
 				}
 			}
 
 			// now we know that we have a valid user name
 			// validate all data,
 			boolean b = profileValidate(request, sess);
 			if (!b && request.getParameter("action").equals("profilechange")) {
 				rd = request.getRequestDispatcher(manager_page);
 				rd.forward(request, response);
 			} else if (!b) {
 				rd = request.getRequestDispatcher(manager_page);
 				rd.forward(request, response);
 			}
 			// validated OK, update the database
 			else {
 				ProfileUpdateBean pu = new ProfileUpdateBean(jdbcURL);
 				if (request.getParameter("action").equals("profilechange")) {
 					try {
 						pu.setProfile(pb);
 					} catch (Exception e) {
 						throw new ServletException("Error saving profile", e);
 					}
 					rd = request.getRequestDispatcher(manager_page);
 					rd.forward(request, response);
 				} else {
 					try {
 						pu.setUser(pb);
 					} catch (Exception e) {
 						throw new ServletException("Error saving profile", e);
 					}
 					response.sendRedirect(manager_page);
 				}
 			}
 		}
 
 
 		/************************************************************/
 		/*						CREATE USER							*/
 		/************************************************************/
 		else if (request.getParameter("action").equals("newuser")) {
 			ProfileBean p = new ProfileBean(jdbcURL);
 			try {
 				HashMap<String, Boolean> role = p.getRoles();
 				sess.setAttribute("roles", role);
 			} catch (Exception e) {
 				throw new ServletException("Error loading profile", e);
 			}
 			sess.setAttribute("profile", p);
 			rd = request.getRequestDispatcher(manager_page);
 			rd.forward(request, response);
 		}
 	}
 
 
 	
 	// valide a profile
 	private boolean profileValidate(HttpServletRequest request, HttpSession sess) {
 		// use the attribute "passwordInvalid" as error messages
 		sess.setAttribute("passwordInvalid", null);
 		String u;
 		// get all data
 		if (request.getParameter("action").equals("profilechange"))
 			u = (String) sess.getAttribute("currentUser");
 		else
 			u = request.getParameter("user");
 		String p1 = request.getParameter("password");
 		String p2 = request.getParameter("password2");
 		String name = request.getParameter("name");
 		String street = request.getParameter("street");
 		String zip = request.getParameter("zip");
 		String city = request.getParameter("city");
 		String country = request.getParameter("country");
 		HashMap<String, Boolean> r =
 				(HashMap<String, Boolean>) sess.getAttribute("roles");
 		Set<String> k = r.keySet();
 		int count = 0;
 		Iterator<String> i = k.iterator();
 		while (i.hasNext()) {
 			String st = request.getParameter(i.next());
 			if (st != null)
 				count++;
 		}
 
 		// validate
 		if (count == 0) {
 			sess.setAttribute("passwordInvalid",
 					"You must select at least one role");
 			return false;
 		} else if (u == null || u.length() < 1) {
 			sess.setAttribute("passwordInvalid",
 					"User name must not be empty, retry!");
 			return false;
 		}
 		if (!request.isUserInRole("admin")
 				&& request.getParameter("admin") != null) {
 			sess.setAttribute("passwordInvalid",
 					"You must be in role admin to set role admin");
 			return false;
 		}
 		if (p1 == null || p2 == null || p1.length() < 1) {
 			sess.setAttribute("passwordInvalid",
 					"Password must not be empty, retry!");
 			return false;
 		} else if (!(p1.equals(p2))) {
 			sess.setAttribute("passwordInvalid",
 					"Passwords do not match, retry!");
 			return false;
 		} else if (name == null || name.length() < 1) {
 			sess.setAttribute("passwordInvalid",
 					"Name must not be empty, retry!");
 			return false;
 		} else if (street == null || street.length() < 1) {
 			sess.setAttribute("passwordInvalid",
 					"Street must no be empty, retry!");
 			return false;
 		} else if (zip == null || zip.length() < 1) {
 			sess.setAttribute("passwordInvalid",
 					"Zip code must not be empty, retry!");
 			return false;
 		} else if (city == null || city.length() < 1) {
 			sess.setAttribute("passwordInvalid",
 					"City must not be empty, retry!");
 			return false;
 		} else if (country == null || country.length() < 1) {
 			sess.setAttribute("passwordInvalid",
 					"County must not be empty, retry!");
 			return false;
 		}
 		// validation OK
 
 		return true;
 	}
 
 	// get the shoppingcart, create it if needed
 	private ShoppingBean getCart(HttpServletRequest request) {
 		HttpSession se = null;
 		se = request.getSession();
 		ShoppingBean sb = null;
 		sb = (ShoppingBean) se.getAttribute("shoppingCart");
 		if (sb == null) {
 			sb = new ShoppingBean();
 			se.setAttribute("shoppingCart", sb);
 		}
 		return sb;
 	}
 
 	/**
 	 * Handles the HTTP <code>GET</code> method.
 	 * 
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException,
 			java.io.IOException {
 		processRequest(request, response);
 	}
 
 	/**
 	 * Handles the HTTP <code>POST</code> method.
 	 * 
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException,
 			java.io.IOException {
 		processRequest(request, response);
 	}
 
 	/**
 	 * Returns a short description of the servlet.
 	 */
 	public String getServletInfo() {
 		return "The main BookShop";
 	}
 }
