 package capstone;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.ejb.EJB;
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
 
 	@EJB
 	SessionBean singletonBean;
 
 	// String userType, userName, password, retypePass, emailAddy, firstName,
 	// lastName, address, city, province, postalCode, phone, fax;
 
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
 		HttpSession session = request.getSession();
 		String vechId = request.getParameter("vechid");
 		PrintWriter out = response.getWriter();
 
 		if (null != vechId) {
 			out.println(vechId);
 		} else
 			out.println("FUCK");
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
 
 			User u = new User();
 
 			try {
 				String connectionURL = "jdbc:derby://localhost:1527/sun-appserv-samples;create=true";
 				Connection conn = null;
 				ResultSet rs;
 				conn = DriverManager.getConnection(connectionURL);
 				Statement statement = conn.createStatement();
 
 				String sql = "SELECT * FROM Users WHERE userName='" + user
 						+ "' AND password='" + pass + "'";
 
 				rs = statement.executeQuery(sql);
 				rs.next();
 
 				if (rs.getString("userName").equals(user)
 						&& rs.getString("password").equals(pass)) {
 					if (!singletonBean.loggedUsers.isEmpty()) {
 						boolean logged = false;
 						for (User logU : singletonBean.loggedUsers) {
 							if (logU.getUsername().equals(user)
 									&& logU.getPassword().equals(pass))
 								logged = true;
 						}
 
 						if (logged) {
 							session.setAttribute("error",
 									"User is already logged in!");
							response.sendRedirect("http://localhost:8080/Capstone/Login.jsp");
 						} else {
 							u.setUserid(rs.getInt("userID"));
 							u.setFirstname(rs.getString("firstName"));
 							u.setLastname(rs.getString("lastName"));
 							u.setAddress(rs.getString("address"));
 							u.setCity(rs.getString("city"));
 							u.setProvince(rs.getString("province"));
 							u.setPostal(rs.getString("postal"));
 							u.setPhone(rs.getString("phone"));
 							u.setFax(rs.getString("fax"));
 							u.setEmail(rs.getString("email"));
 							u.setUsername(rs.getString("userName"));
 							u.setPassword(rs.getString("password"));
 							u.setUsertype(rs.getString("userType"));
 
 							singletonBean.loggedUsers.add(u);
 						}
 
 					} else {
 						u.setUserid(rs.getInt("userID"));
 						u.setFirstname(rs.getString("firstName"));
 						u.setLastname(rs.getString("lastName"));
 						u.setAddress(rs.getString("address"));
 						u.setCity(rs.getString("city"));
 						u.setProvince(rs.getString("province"));
 						u.setPostal(rs.getString("postal"));
 						u.setPhone(rs.getString("phone"));
 						u.setFax(rs.getString("fax"));
 						u.setEmail(rs.getString("email"));
 						u.setUsername(rs.getString("userName"));
 						u.setPassword(rs.getString("password"));
 						u.setUsertype(rs.getString("userType"));
 
 						singletonBean.loggedUsers.add(u);
 					}
 
 					sql = "SELECT * FROM Vehicle";
 					rs = statement.executeQuery(sql);
 					ArrayList<Vehicle> vechList = new ArrayList<Vehicle>();
 
 					while (rs.next()) {
 						Vehicle v = new Vehicle();
 						v.setVechid(rs.getInt("vechID"));
 						v.setUserid(rs.getInt("userID"));
 						v.setCarClass(rs.getString("class"));
 						v.setCarYear(rs.getString("carYear"));
 						v.setMake(rs.getString("make"));
 						v.setModel(rs.getString("model"));
 						v.setColor(rs.getString("color"));
 						v.setVin(rs.getString("vin"));
 						v.setPlate(rs.getString("plate"));
 						v.setEngine(rs.getString("engine"));
 						v.setTranny(rs.getString("Tranny"));
 						v.setOdometer(rs.getString("odometer"));
 						v.setOilType(rs.getString("oilType"));
 						v.setDateolc(rs.getString("DateOLC"));
 						v.setStatus(rs.getString("status"));
 
 						vechList.add(v);
 					}
 
 					statement.close();
 					conn.close();
 
 					session.setAttribute("vehicles", vechList);
 					int userIndex = singletonBean.loggedUsers.indexOf(u);
 					session.setAttribute("user",
 							singletonBean.loggedUsers.get(userIndex));
 					response.sendRedirect("http://localhost:8080/Capstone/user_view.jsp");
 				} else
 					out.println("nopass");
 
				// } catch (ArrayIndexOutOfBoundsException e) {
				// session.setAttribute("error",
				// "Username or password is incorrect!");
				// response.sendRedirect("http://localhost:8080/Capstone/Login.jsp");
 			} catch (IllegalStateException e) {
 				out.print(e.getMessage());
 			} catch (SQLException e) {
 				session.setAttribute("error",
 						"Username or password is incorrect!");
 				response.sendRedirect("http://localhost:8080/Capstone/Login.jsp");
 			}
 
 		} else if (request.getParameter("changeVehicle") != null) {
 			// run add vehicle code
 			Map<String, String> errors = new HashMap<String, String>(10);
 			Vehicle newVech = new Vehicle();
 			try {
 				User u = (User) session.getAttribute("user");
 				String action = (String) session.getAttribute("action");
 
 				newVech.setUserid(u.getUserid());
 				newVech.setStatus("Y");
 
 				// Validate No Matter What
 
 				// Make Validation
 				if (request.getParameter("vehicleMake").equals(""))
 					errors.put("makeError",
 							"Please fill in the make field.<br/>");
 				else if (Pattern.matches("[a-zA-Z]+",
 						request.getParameter("vehicleMake")) == false)
 					errors.put("makeError", "Please enter a valid make.<br/>");
 				else
 					newVech.setMake(request.getParameter("vehicleMake"));
 				// Model Validation
 				if (request.getParameter("vehicleModel").equals(""))
 					errors.put("modelError",
 							"Please fill in the model field.<br/>");
 				else if (Pattern.matches("[a-zA-Z]+",
 						request.getParameter("vehicleModel")) == false)
 					errors.put("modelError", "Please enter a valid model.<br/>");
 				else
 					newVech.setModel(request.getParameter("vehicleModel"));
 				// Color Validation
 				if (request.getParameter("vehicleColor").equals(""))
 					errors.put("colorError",
 							"Please fill in the color field.<br/>");
 				else if (Pattern.matches("[a-zA-Z]+",
 						request.getParameter("vehicleColor")) == false)
 					errors.put("colorError", "Please enter a valid color.<br/>");
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
 				if (request.getParameter("engineType").equals("Select engine"))
 					errors.put("engineError",
 							"Please select a value from the engine dropbox.<br/>");
 				else
 					newVech.setEngine(request.getParameter("engineType"));
 				// VIN Validation
 				if (request.getParameter("vehicleVIN").equals(""))
 					errors.put("vinError", "Please fill in the VIN field.<br/>");
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
 					newVech.setCarClass(request.getParameter("vehicleClass"));
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
 					newVech.setOdometer(request.getParameter("vehicleOdometer"));
 
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
 
 						newVech.setDateolc(request.getParameter("DateOfChange"));
 
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
 					newVech.setTranny(request.getParameter("transmissionType"));
 				// Oil Type Validation
 				if (request.getParameter("oilType").equals(""))
 					errors.put("oilTypeError", "Please enter an oil type.<br/>");
 				else
 					newVech.setOilType(request.getParameter("oilType"));
 
 				// END OF VALIDATION
 
 				// If there are errors, go back to add_edit page
 				if (!errors.isEmpty()) {
 					session.setAttribute("errors", errors);
 					session.setAttribute("vehicle", newVech);
 					response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 				} else { // else add vehicle to database and go to user_view
 							// page
 					session.setAttribute("errors", null);
 					session.setAttribute("vehicle", null);
 					if (action.equals("addVehicle")) {
 
 						String connectionURL = "jdbc:derby://localhost:1527/sun-appserv-samples;create=true";
 						Connection conn = null;
 
 						conn = DriverManager.getConnection(connectionURL);
 
 						Statement statement = conn.createStatement();
 
 						String sql = "insert into vehicle (userID, class, carYear, make, model, color, vin, plate, engine, tranny, odometer, oilType, DateOLC, status) values("
 								+ u.getUserid()
 								+ ", '"
 								+ newVech.getCarClass()
 								+ "', '"
 								+ newVech.getCarYear()
 								+ "', '"
 								+ newVech.getMake()
 								+ "', '"
 								+ newVech.getModel()
 								+ "', '"
 								+ newVech.getColor()
 								+ "', '"
 								+ newVech.getVin()
 								+ "', '"
 								+ newVech.getPlate()
 								+ "', '"
 								+ newVech.getEngine()
 								+ "', '"
 								+ newVech.getTranny()
 								+ "', '"
 								+ newVech.getOdometer()
 								+ "', '"
 								+ newVech.getOilType()
 								+ "', '"
 								+ newVech.getDateolc()
 								+ "', '"
 								+ newVech.getStatus() + "')";
 						statement.executeUpdate(sql);
 
 						// Start grabing vehicles in database put to ArrayList
 						// give Arraylist to user_view
 
 						sql = "SELECT * FROM Vehicle";
 						ResultSet rs = statement.executeQuery(sql);
 						ArrayList<Vehicle> vechList = new ArrayList<Vehicle>();
 
 						while (rs.next()) {
 							Vehicle v = new Vehicle();
 							v.setVechid(rs.getInt("vechID"));
 							v.setUserid(rs.getInt("userID"));
 							v.setCarClass(rs.getString("class"));
 							v.setCarYear(rs.getString("carYear"));
 							v.setMake(rs.getString("make"));
 							v.setModel(rs.getString("model"));
 							v.setColor(rs.getString("color"));
 							v.setVin(rs.getString("vin"));
 							v.setPlate(rs.getString("plate"));
 							v.setEngine(rs.getString("engine"));
 							v.setTranny(rs.getString("Tranny"));
 							v.setOdometer(rs.getString("odometer"));
 							v.setOilType(rs.getString("oilType"));
 							v.setDateolc(rs.getString("DateOLC"));
 							v.setStatus(rs.getString("status"));
 
 							vechList.add(v);
 						}
 
 						statement.close();
 						conn.close();
 
 						session.setAttribute("vehicles", vechList);
 						response.sendRedirect("http://localhost:8080/Capstone/user_view.jsp");
 					} else if (action.equals("editVehicle")) {
 						out.print("EDITEN");
 					} else
 						System.out.println("Mashugana");
 				}
 				// For some reason when this button is pushed and there are no
 				// errors, the page isnt redirected. It instead goes out of the
 				// if statement that checks action attribute
 				// which is a blank page...
 			} catch (NullPointerException e) {
 				session.setAttribute("action", "addVehicle");
 				session.setAttribute("errors", errors);
 				session.setAttribute("vehicle", newVech);
 				response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 			} catch (NumberFormatException e) {
 				errors.put("odoError", "Please enter a positive number.<br/>");
 				session.setAttribute("errors", errors);
 				session.setAttribute("vehicle", newVech);
 				response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 			} catch (SQLException e) {
 				e.getMessage();
 				System.out.println(e.getMessage());
 			} catch (ParseException e) {
 				errors.put("docError",
 						"Please enter the date in the following format: MM/dd/yyyy<br/>");
 				session.setAttribute("errors", errors);
 				session.setAttribute("vehicle", newVech);
 				response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 			}
 
 		} else if (request.getParameter("addVehicle") != null) {
 			session.setAttribute("action", "addVehicle");
 			session.setAttribute("errors", null);
 			response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 
 		} else if (request.getParameter("Logout") != null) {
 
 		}
 
 		else if (request.getParameter("checkSubmit") != null)
 			response.sendRedirect("http://localhost:8080/Capstone/Vehicle_Invoice.jsp");
 
 		else if (request.getParameter("createUser") != null) {
 			// userType = request.getParameter("userTypeDD");
 			// userName = request.getParameter("userName");
 			// password = request.getParameter("password");
 			// retypePass = request.getParameter("retypePass");
 			// emailAddy = request.getParameter("emailAddy");
 			// firstName = request.getParameter("firstName");
 			// lastName = request.getParameter("lastName");
 			// address = request.getParameter("address");
 			// city = request.getParameter("city");
 			// province = request.getParameter("province");
 			// postalCode = request.getParameter("postalCode");
 			// phone = request.getParameter("phone");
 			// fax = request.getParameter("fax");
 			//
 			// session = request.getSession(true);
 			//
 			// /*
 			// * String firstname, String lastname, String address, String city,
 			// * String province, String postal, String phone, String fax,
 			// String
 			// * email, String username, String password, String usertype
 			// */
 			//
 			// User newUser = new User(firstName, lastName, address, city,
 			// province, postalCode, phone, fax, emailAddy, userName,
 			// password, userType);
 			// if (!session.isNew()) {
 			// newUserList = (ArrayList<User>) session
 			// .getAttribute("statename");
 			// } else {
 			// newUserList = new ArrayList<User>();
 			// }
 			// if (newUserList == null) {
 			// newUserList = new ArrayList<User>();
 			// }
 			// newUserList.add(newUser);
 			// session.setAttribute("statename", newUserList);
 			// session.setAttribute("buttonPressed", "createUser");
 			//
 			// RequestDispatcher rd =
 			// request.getRequestDispatcher("newUser.jsp");
 			// rd.forward(request, response);
 
 		}
 	}
 }
