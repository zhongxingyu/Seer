 package capstone;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceUnit;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class MechanicServlet
  */
 @WebServlet("/MechanicServlet")
 public class MechanicServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	@PersistenceUnit
 	EntityManagerFactory emf;
 	String userType, userName, password, retypePass, emailAddy, firstName,
 			lastName, address, city, province, postalCode, phone, fax;
 
 	ArrayList<User> newUserList = new ArrayList<User>();
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public MechanicServlet() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession();
 		PrintWriter out = response.getWriter();
 		if (request.getParameter("submit") != null) {
 			String user = request.getParameter("userName");
 			String pass = request.getParameter("password");
 
 			User u = null;
 
 			try {
 				u = (User) emf
 						.createEntityManager()
 						.createQuery(
 								"SELECT u  FROM User u WHERE u.username='"
 										+ user + "' AND u.password='" + pass
 										+ "'").getResultList().get(0);
 			} catch (ArrayIndexOutOfBoundsException e) {
 				session.setAttribute("error",
 						"Username or password is incorrect!");
 				response.sendRedirect("http://localhost:8080/Capstone/Login.jsp");
 			}
 
 			if (null != u) {
 				if (user.equals(u.getUsername())
 						&& pass.equals(u.getPassword())) {
 					session.setAttribute("user", u);
 					response.sendRedirect("http://localhost:8080/Capstone/user_view.jsp");
 
 				} else {
 					session.setAttribute("error",
 							"Username or password is incorrect!");
 					response.sendRedirect("http://localhost:8080/Capstone/Login.jsp");
 
 				}
 			} else {
 				session.setAttribute("error",
 						"Username or password is incorrect!");
 				try {
 					response.sendRedirect("http://localhost:8080/Capstone/Login.jsp");
 				} catch (IllegalStateException e) {
 					out.print(e.getMessage());
 				}
 
 			}
 
 		} else if (request.getParameter("changeVehicle") != null) {
 			// run add vehicle code
 			Map<String, String> errors = new HashMap<String, String>(10);
 			try {
 				User u = (User) session.getAttribute("user");
 				String action = (String) session.getAttribute("action");
 				if (action.equals("addVehicle")) {
 					Vehicle newVech = new Vehicle();
 
 					// Make Validation
 					if (request.getParameter("vehicleMake").equals(""))
 						errors.put("makeError",
 								"Please fill in the make field.<br/>");
 					else if (Pattern.matches("[a-zA-Z]+",
 							request.getParameter("vehicleMake")) == false)
 						errors.put("makeError",
 								"Please enter a valid make.<br/>");
 					else
 						newVech.setMake(request.getParameter("vehicleMake"));
 					// Model Validation
 					if (request.getParameter("vehicleModel").equals(""))
 						errors.put("modelError",
 								"Please fill in the model field.<br/>");
 					else if (Pattern.matches("[a-zA-Z]+",
 							request.getParameter("vehicleModel")) == false)
 						errors.put("modelError",
 								"Please enter a valid model.<br/>");
 					else
 						newVech.setModel(request.getParameter("vehicleModel"));
 					// Color Validation
 					if (request.getParameter("vehicleColor").equals(""))
 						errors.put("colorError",
 								"Please fill in the color field.<br/>");
 					else if (Pattern.matches("[a-zA-Z]+",
 							request.getParameter("vehicleColor")) == false)
 						errors.put("colorError",
 								"Please enter a valid color.<br/>");
 					else
 						newVech.setColor(request.getParameter("vehicleColor"));
 					// Car Year Validation
 					if (request.getParameter("vehicleYear").equals(""))
 						errors.put("carYearError",
 								"Please fill in the vehicle year field.<br/>");
 					else if (Pattern.matches("[0-9]+",
 							request.getParameter("vehicleYear")) == false
 							&& request.getParameter("vehicleYear").length() != 4)
 						errors.put("carYearError",
 								"Please enter a valid vehicle year.<br/>");
 					else
 						newVech.setCarYear(request.getParameter("vehicleYear"));
 					// Engine Validation
 					if (request.getParameter("engineType").equals(
 							"Select engine"))
 						errors.put("engineError",
 								"Please select a value from the engine dropbox.<br/>");
 					else
 						newVech.setEngine(request.getParameter("engineType"));
 					// VIN Validation
 					if (request.getParameter("vehicleVIN").equals(""))
 						errors.put("vinError",
 								"Please fill in the VIN field.<br/>");
					/*
					 * else if (Pattern.matches("[0-9]+",
					 * request.getParameter("vehicleVIN")) == false)
					 * errors.put("vinError", "Please enter a valid VIN.<br/>");
					 */
 					else
 						newVech.setVin(request.getParameter("vehicleVIN"));
 					// Plate Validation
 					if (request.getParameter("vehiclePlate").equals(""))
 						errors.put("plateError",
 								"Please fill in the plate field.<br/>");
 					else
 						newVech.setPlate(request.getParameter("vehiclePlate"));
 					// Car Class Validation
 					if (request.getParameter("vehicleClass").equals(""))
 						errors.put("carClassError",
 								"Please fill in the car class field.<br/>");
 					else if (Pattern.matches("[a-zA-Z]+",
 							request.getParameter("vehicleClass")) == false)
 						errors.put("carClassError",
 								"Please enter a valid car class.<br/>");
 					else
 						newVech.setCarClass(request
 								.getParameter("vehicleClass"));
 					// Odometer Validation
 
 					if (request.getParameter("vehicleOdometer").equals(""))
 						errors.put("odoError",
 								"Please fill in the odometer field.<br/>");
 					else if (Integer.parseInt(request
 							.getParameter("vehicleOdometer")) < 0)
 						errors.put("odoError",
 								"Please enter a positive number.<br/>");
 					else if (Pattern.matches("[0-9]+",
 							request.getParameter("vehicleOdometer")) == false)
 						errors.put("odoError",
 								"Please enter a positive number.<br/>");
 					else
 						newVech.setOdometer(request
 								.getParameter("vehicleOdometer"));
 
 					// Date of last change Validation
 					if (request.getParameter("DateOfChange").equals(""))
 						errors.put("docError",
 								"Please fill in the date of the vehicle's last oil change.<br/>");
 					else {
 						SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
 						Date doc = new Date();
 
 						doc = df.parse(request.getParameter("DateOfChange"));
 						if (df.format(doc).equals(
 								request.getParameter("DateOfChange"))) {
 
 							newVech.setDateolc(doc);
 
 						} else {
 							errors.put("docError",
 									"Please enter the date in the following format: MM/dd/yyyy<br/>");
 						}
 
 					}
 					// Tranny Validation
 					if (request.getParameter("transmissionType").equals(
 							"Select transmission"))
 						errors.put("trannyError",
 								"Please select a transmission.<br/>");
 					else
 						newVech.setTranny(request
 								.getParameter("transmissionType"));
 					// Oil Type Validation
 					if (request.getParameter("oilType").equals(""))
 						errors.put("oilTypeError",
 								"Please enter an oil type.<br/>");
					/*
					 * else if (Pattern.matches("[a-zA-Z][0-9]+",
					 * request.getParameter("oilType")) == false)
					 * errors.put("oilTypeError",
					 * "Please enter a valid oil type.<br/>");
					 */
 					else
 						newVech.setOilType(request.getParameter("oilType"));
 
 					// If there are errors, go back to add_edit page
 					if (!errors.isEmpty()) {
 						session.setAttribute("errors", errors);
 						session.setAttribute("vehicle", newVech);
 						response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 					} else { // else add vehicle to database and go to user_view
 								// page
 						emf.createEntityManager().persist(newVech);
 						emf.createEntityManager().close();
 						session.setAttribute("errorVech", null);
 
 						response.sendRedirect("http://localhost:8080/Capstone/user_view.jsp");
 					}
 				}
 			} catch (NullPointerException e) {
 				session.setAttribute("action", "addVehicle");
 			} catch (NumberFormatException e) {
 				errors.put("odoError", "Please enter a positive number.<br/>");
 			} catch (ParseException e) {
 				errors.put("docError",
 						"Please enter the date in the following format: MM/dd/yyyy<br/>");
 			}
 
 		} else if (request.getParameter("addVehicle") != null) {
 			session.setAttribute("action", "addVehicle");
 			session.setAttribute("errors", null);
 			response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 
 		}
 
 		else if (request.getParameter("checkSubmit") != null)
 			response.sendRedirect("http://localhost:8080/Capstone/Vehicle_Invoice.jsp");
 
 		else if (request.getParameter("createUser") != null) {
 			userType = request.getParameter("userTypeDD");
 			userName = request.getParameter("userName");
 			password = request.getParameter("password");
 			retypePass = request.getParameter("retypePass");
 			emailAddy = request.getParameter("emailAddy");
 			firstName = request.getParameter("firstName");
 			lastName = request.getParameter("lastName");
 			address = request.getParameter("address");
 			city = request.getParameter("city");
 			province = request.getParameter("province");
 			postalCode = request.getParameter("postalCode");
 			phone = request.getParameter("phone");
 			fax = request.getParameter("fax");
 
 			session = request.getSession(true);
 
 			/*
 			 * String firstname, String lastname, String address, String city,
 			 * String province, String postal, String phone, String fax, String
 			 * email, String username, String password, String usertype
 			 */
 
 			User newUser = new User(firstName, lastName, address, city,
 					province, postalCode, phone, fax, emailAddy, userName,
 					password, userType);
 			if (!session.isNew()) {
 				newUserList = (ArrayList<User>) session
 						.getAttribute("statename");
 			} else {
 				newUserList = new ArrayList<User>();
 			}
 			if (newUserList == null) {
 				newUserList = new ArrayList<User>();
 			}
 			newUserList.add(newUser);
 			session.setAttribute("statename", newUserList);
 			session.setAttribute("buttonPressed", "createUser");
 
 			RequestDispatcher rd = request.getRequestDispatcher("newUser.jsp");
 			rd.forward(request, response);
 
 		}
 	}
 }
