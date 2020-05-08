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
 
 	// String userType, userName, password, retypePass, emailAddy, firstName,
 	// lastName, address, city, province, postalCode, phone, fax;
 
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
 		String vechid = request.getParameter("vechid");
 		String inspectId = request.getParameter("inspectid");
 		PrintWriter out = response.getWriter();
 		try {
 
 			String connectionURL = "jdbc:derby://localhost:1527/sun-appserv-samples;create=true";
 			Connection conn = null;
 			ResultSet rs;
 			conn = DriverManager.getConnection(connectionURL);
 			Statement statement = conn.createStatement();
 			if (vechid != null) {
 				String sql = "SELECT * FROM Vehicle WHERE vechid=" + vechid;
 
 				rs = statement.executeQuery(sql);
 
 				rs.next();
 				Vehicle v = new Vehicle();
 
 				// set rs's to Vehicle v
 				v.setVechid(rs.getInt("vechid"));
 				v.setUserid(rs.getInt("userid"));
 				v.setCarClass(rs.getString("class"));
 				v.setCarYear(rs.getString("carYear"));
 				v.setMake(rs.getString("make"));
 				v.setModel(rs.getString("model"));
 				v.setColor(rs.getString("color"));
 				v.setVin(rs.getString("vin"));
 				v.setPlate(rs.getString("plate"));
 				v.setEngine(rs.getString("engine"));
 				v.setTranny(rs.getString("tranny"));
 				v.setOdometer(rs.getString("odometer"));
 				v.setOilType(rs.getString("oilType"));
 				v.setDateolc(rs.getString("DateOLC"));
 				v.setStatus(rs.getString("status"));
 
 				session.setAttribute("vehicle", v);
 				// setDropdowns
 				setDropdowns(request, v);
 				User u = (User) session.getAttribute("user");
 
 				statement = conn.createStatement();
 				sql = "SELECT * FROM Inspection WHERE vechid=" + vechid;
 
 				rs = statement.executeQuery(sql);
 
 				// check if rs has data
 				// if it does send an arrayList of inspections to vech view
 				// if it doesn't just go to vech_view
 				ArrayList<Inspection> inspects = new ArrayList<Inspection>();
 				while (rs.next()) {
 
 					Inspection i = new Inspection();
 
 					// set rs's to Vehicle v
 					i.setVechid(rs.getInt("vechid"));
 					i.setInspectid(rs.getInt("inspectid"));
 					i.setDateoi(rs.getString("dateoi"));
 					i.setNotes(rs.getString("notes"));
 					i.setTpress(rs.getString("tpress"));
 					i.setCataconv(rs.getString("cataconv"));
 					i.setMuffpipes(rs.getString("muffpipes"));
 					i.setExhclamhang(rs.getString("exhclamhang"));
 					i.setFuelfilter(rs.getString("fuelfilter"));
 					i.setEnginefilter(rs.getString("enginefilter"));
 					i.setCabinfilter(rs.getString("cabinfilter"));
 					i.setBrakeline(rs.getString("brakeline"));
 					i.setGaskets(rs.getString("gaskets"));
 					i.setHoses(rs.getString("hoses"));
 					i.setBelts(rs.getString("belts"));
 					i.setWashfluid(rs.getString("washfluid"));
 					i.setDifffluid(rs.getString("difffluid"));
 					i.setBrakefluid(rs.getString("brakefluid"));
 					i.setSteerfluid(rs.getString("steerfluid"));
 					i.setCoolfluid(rs.getString("coolfluid"));
 					i.setTranfluid(rs.getString("tranfluid"));
 					i.setWipeblades(rs.getString("wipeblades"));
 					i.setHorn(rs.getString("horn"));
 					i.setInlights(rs.getString("inlights"));
 					i.setExlights(rs.getString("exlights"));
 
 					inspects.add(i);
 				}
 
 				session.setAttribute("inspects", inspects);
 
 				// disable Service button if Customer
 				if (u.getUsertype().equals("Cust"))
 					session.setAttribute("disabled", "disabled");
 				else
 					session.setAttribute("disabled", null);
 				// disable invocies button for Personal Mechanic
 				if (u.getUsertype().equals("PersonalMech"))
 					session.setAttribute("disableInv", "disabled");
 				else
 					session.setAttribute("disableInv", null);
 
 				response.sendRedirect("http://localhost:8080/Capstone/vech_view.jsp");
			} else if (inspectId != null) {
				int inspectid = Integer.parseInt(request.getParameter("inspectid"));
 				Map<String, String> vechStatus = new HashMap<String, String>(22);
 				ArrayList<Inspection> inspect = (ArrayList<Inspection>) session
 						.getAttribute("inspects");
 
				for(int i = 0; i < inspect.size(); i++){					
					if(inspect.get(i).getInspectid() == inspectid){
 						// Ex Lights
						System.out.println("In " + inspect.get(i).getInspectid());
 						if (inspect.get(i).getExlights().equals("G"))
 							vechStatus.put("exLights", "green");
 						else if (inspect.get(i).getExlights().equals("R"))
 							vechStatus.put("exLights", "red");
 						else if (inspect.get(i).getExlights().equals("Y"))
 							vechStatus.put("exLights", "yellow");
 						// In Lights
 						if (inspect.get(i).getInlights().equals("G"))
 							vechStatus.put("inLights", "green");
 						else if (inspect.get(i).getInlights().equals("R"))
 							vechStatus.put("inLights", "red");
 						else if (inspect.get(i).getInlights().equals("Y"))
 							vechStatus.put("inLights", "yellow");
 						// Horn
 						if (inspect.get(i).getHorn().equals("G"))
 							vechStatus.put("horn", "green");
 						else if (inspect.get(i).getHorn().equals("R"))
 							vechStatus.put("horn", "red");
 						else if (inspect.get(i).getHorn().equals("Y"))
 							vechStatus.put("horn", "yellow");
 						// Wiper blades
 						if (inspect.get(i).getWipeblades().equals("G"))
 							vechStatus.put("wipeblades", "green");
 						else if (inspect.get(i).getWipeblades().equals("R"))
 							vechStatus.put("wipeblades", "red");
 						else if (inspect.get(i).getWipeblades().equals("Y"))
 							vechStatus.put("wipeblades", "yellow");
 						// Transmission
 						if (inspect.get(i).getTranfluid().equals("G"))
 							vechStatus.put("tranfluid", "green");
 						else if (inspect.get(i).getTranfluid().equals("R"))
 							vechStatus.put("tranfluid", "red");
 						else if (inspect.get(i).getTranfluid().equals("Y"))
 							vechStatus.put("tranfluid", "yellow");
 						// Coolant
 						if (inspect.get(i).getCoolfluid().equals("G"))
 							vechStatus.put("coolFluid", "green");
 						else if (inspect.get(i).getCoolfluid().equals("R"))
 							vechStatus.put("coolFluid", "red");
 						else if (inspect.get(i).getCoolfluid().equals("Y"))
 							vechStatus.put("coolFluid", "yellow");
 						// Power Steering
 						if (inspect.get(i).getSteerfluid().equals("G"))
 							vechStatus.put("steerFluid", "green");
 						else if (inspect.get(i).getSteerfluid().equals("R"))
 							vechStatus.put("steerFluid", "red");
 						else if (inspect.get(i).getSteerfluid().equals("Y"))
 							vechStatus.put("steerFluid", "yellow");
 						// Brake Fluid
 						if (inspect.get(i).getBrakefluid().equals("G"))
 							vechStatus.put("brakeFluid", "green");
 						else if (inspect.get(i).getBrakefluid().equals("R"))
 							vechStatus.put("brakeFluid", "red");
 						else if (inspect.get(i).getBrakefluid().equals("Y"))
 							vechStatus.put("brakeFluid", "yellow");
 						// Differential
 						if (inspect.get(i).getDifffluid().equals("G"))
 							vechStatus.put("diffFluid", "green");
 						else if (inspect.get(i).getDifffluid().equals("R"))
 							vechStatus.put("diffFluid", "red");
 						else if (inspect.get(i).getDifffluid().equals("Y"))
 							vechStatus.put("diffFluid", "yellow");
 						// Washer Fluid
 						if (inspect.get(i).getWashfluid().equals("G"))
 							vechStatus.put("washFluid", "green");
 						else if (inspect.get(i).getWashfluid().equals("R"))
 							vechStatus.put("washFluid", "red");
 						else if (inspect.get(i).getWashfluid().equals("Y"))
 							vechStatus.put("washFluid", "yellow");
 						// Belts
 						if (inspect.get(i).getBelts().equals("G"))
 							vechStatus.put("belts", "green");
 						else if (inspect.get(i).getBelts().equals("R"))
 							vechStatus.put("belts", "red");
 						else if (inspect.get(i).getBelts().equals("Y"))
 							vechStatus.put("belts", "yellow");
 						// Hoses
 						if (inspect.get(i).getHoses().equals("G"))
 							vechStatus.put("hoses", "green");
 						else if (inspect.get(i).getHoses().equals("R"))
 							vechStatus.put("hoses", "red");
 						else if (inspect.get(i).getHoses().equals("Y"))
 							vechStatus.put("hoses", "yellow");
 						// Gaskets
 						if (inspect.get(i).getGaskets().equals("G"))
 							vechStatus.put("gaskets", "green");
 						else if (inspect.get(i).getGaskets().equals("R"))
 							vechStatus.put("gaskets", "red");
 						else if (inspect.get(i).getGaskets().equals("Y"))
 							vechStatus.put("gaskets", "yellow");
 						// Brake Line
 						if (inspect.get(i).getBrakeline().equals("G"))
 							vechStatus.put("brakeLine", "green");
 						else if (inspect.get(i).getBrakeline().equals("R"))
 							vechStatus.put("brakeLine", "red");
 						else if (inspect.get(i).getBrakeline().equals("Y"))
 							vechStatus.put("brakeLine", "yellow");
 						// Cabin Filter
 						if (inspect.get(i).getCabinfilter().equals("G"))
 							vechStatus.put("cabinFilter", "green");
 						else if (inspect.get(i).getCabinfilter().equals("R"))
 							vechStatus.put("cabinFilter", "red");
 						else if (inspect.get(i).getCabinfilter().equals("Y"))
 							vechStatus.put("cabinFilter", "yellow");
 						// Engine filter
 						if (inspect.get(i).getEnginefilter().equals("G"))
 							vechStatus.put("enginefilter", "green");
 						else if (inspect.get(i).getEnginefilter().equals("R"))
 							vechStatus.put("enginefilter", "red");
 						else if (inspect.get(i).getEnginefilter().equals("Y"))
 							vechStatus.put("enginefilter", "yellow");
 						// Fuel Filter
 						if (inspect.get(i).getFuelfilter().equals("G"))
 							vechStatus.put("fuelfilter", "green");
 						else if (inspect.get(i).getFuelfilter().equals("R"))
 							vechStatus.put("fuelfilter", "red");
 						else if (inspect.get(i).getFuelfilter().equals("Y"))
 							vechStatus.put("fuelfilter", "yellow");
 						// Exhclamhang
 						if (inspect.get(i).getExhclamhang().equals("G"))
 							vechStatus.put("exhClamHang", "green");
 						else if (inspect.get(i).getExhclamhang().equals("R"))
 							vechStatus.put("exhClamHang", "red");
 						else if (inspect.get(i).getExhclamhang().equals("Y"))
 							vechStatus.put("exhClamHang", "yellow");
 						// Muff Pipes
 						if (inspect.get(i).getMuffpipes().equals("G"))
 							vechStatus.put("muffPipes", "green");
 						else if (inspect.get(i).getMuffpipes().equals("R"))
 							vechStatus.put("muffPipes", "red");
 						else if (inspect.get(i).getMuffpipes().equals("Y"))
 							vechStatus.put("muffPipes", "yellow");
 						// Catalytic Converter
 						if (inspect.get(i).getCataconv().equals("G"))
 							vechStatus.put("cataConv", "green");
 						else if (inspect.get(i).getCataconv().equals("R"))
 							vechStatus.put("cataConv", "red");
 						else if (inspect.get(i).getCataconv().equals("Y"))
 							vechStatus.put("cataConv", "yellow");
 						// Tire Press
 						if (inspect.get(i).getTpress().equals("G"))
 							vechStatus.put("tPress", "green");
 						else if (inspect.get(i).getTpress().equals("R"))
 							vechStatus.put("tPress", "red");
 						else if (inspect.get(i).getTpress().equals("Y"))
 							vechStatus.put("tPress", "yellow");
 						// Notes
 						vechStatus.put("notes", inspect.get(i).getNotes());
 
 						session.setAttribute("status", vechStatus);
						response.sendRedirect("http://localhost:8080/Capstone/view_checklist.jsp");					
 					}
				}		
 			}
 
 		} catch (SQLException e) {
 			out.println(e.getMessage());
 		}
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession();
 		PrintWriter out = response.getWriter();
 		session.setAttribute("error", null);
 		if (request.getParameter("submit") != null) {
 			session.setAttribute("user", null);
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
 					sql = "SELECT * FROM Vehicle WHERE userId=" + u.getUserid();
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
 					session.setAttribute("user", u);
 					response.sendRedirect("http://localhost:8080/Capstone/user_view.jsp");
 				} else
 					out.println("nopass");
 
 			} catch (ArrayIndexOutOfBoundsException e) {
 				response.sendRedirect("http://localhost:8080/Capstone/Login.jsp");
 			} catch (IllegalStateException e) {
 				out.print(e.getMessage());
 			} catch (SQLException e) {
 				session.setAttribute("error",
 						"Username or password is incorrect!");
 				response.sendRedirect("http://localhost:8080/Capstone/Login.jsp");
 			}
 
 		} else if (request.getParameter("changeVehicle") != null) {
 
 			Map<String, String> errors = new HashMap<String, String>(10);
 			Vehicle newVech = new Vehicle();
 			String action = (String) session.getAttribute("action");
 			if (action.equals("editVehicle"))
 				newVech = (Vehicle) session.getAttribute("vehicle");
 			User u = (User) session.getAttribute("user");
 			try {
 
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
 				else if (Pattern.matches("[a-zA-Z0-9]+",
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
 				else if (Pattern.matches("[a-zA-Z]+",
 						request.getParameter("vehicleYear")) == true
 						|| request.getParameter("vehicleYear").length() != 4)
 					errors.put("carYearError",
 							"Please enter a valid vehicle year.<br/>");
 				else {
 					if (Integer.parseInt(request.getParameter("vehicleYear")) < 1900)
 						errors.put("carYearError",
 								"Please enter a valid vehicle year.<br/>");
 					else
 						newVech.setCarYear(request.getParameter("vehicleYear"));
 				}
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
 
 				// SQL stuff
 				String connectionURL = "jdbc:derby://localhost:1527/sun-appserv-samples;create=true";
 				Connection conn = null;
 
 				conn = DriverManager.getConnection(connectionURL);
 
 				Statement statement = conn.createStatement();
 
 				// If there are errors, go back to add_edit page
 				if (!errors.isEmpty()) {
 					session.setAttribute("errors", errors);
 					int fuck2 = newVech.getVechid();
 					session.setAttribute("vehicle", newVech);
 					int fuck = newVech.getVechid();
 					setDropdowns(request, newVech);
 					response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 				} else { // else add vehicle to database and go to user_view
 							// page
 					session.setAttribute("errors", null);
 					// session.setAttribute("vehicle", null);
 					if (action.equals("addVehicle")) {
 
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
 
 						session.setAttribute("vehicle", newVech);
 
 					} else if (action.equals("editVehicle")) {
 						Vehicle v = (Vehicle) session.getAttribute("vehicle");
 
 						int fuck = v.getVechid();
 
 						// UPDATE Vehicle
 						String sql = "UPDATE Vehicle SET class='"
 								+ request.getParameter("vehicleClass")
 								+ "', carYear='"
 								+ request.getParameter("vehicleYear")
 								+ "', make='"
 								+ request.getParameter("vehicleMake")
 								+ "', model='"
 								+ request.getParameter("vehicleModel")
 								+ "', color='"
 								+ request.getParameter("vehicleColor")
 								+ "', vin='"
 								+ request.getParameter("vehicleVIN")
 								+ "', plate='"
 								+ request.getParameter("vehiclePlate")
 								+ "', engine='"
 								+ request.getParameter("engineType")
 								+ "', tranny='"
 								+ request.getParameter("transmissionType")
 								+ "', odometer='"
 								+ request.getParameter("vehicleOdometer")
 								+ "', oilType='"
 								+ request.getParameter("oilType")
 								+ "', DateOLC='"
 								+ request.getParameter("DateOfChange") + "' "
 								+ "WHERE vechID=" + v.getVechid();
 
 						statement.executeUpdate(sql);
 					} else
 						System.out.println("Mashugana");
 
 					String sql = "SELECT * FROM Vehicle WHERE userId="
 							+ u.getUserid();
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
 
 					conn.close();
 					statement.close();
 					session.setAttribute("vehicles", vechList);
 					response.sendRedirect("http://localhost:8080/Capstone/user_view.jsp");
 				}
 			} catch (NullPointerException e) {
 				errors.put("nullError", e.getMessage());
 				session.setAttribute("errors", errors);
 				setDropdowns(request, newVech);
 				session.setAttribute("vehicle", newVech);
 				response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 			} catch (NumberFormatException e) {
 				errors.put("odoError",
 						"Please enter a positive odometer number.<br/>");
 				session.setAttribute("errors", errors);
 				setDropdowns(request, newVech);
 				session.setAttribute("vehicle", newVech);
 				response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 			} catch (SQLException e) {
 				System.out.println(e.getMessage());
 
 			} catch (ParseException e) {
 				errors.put("docError",
 						"Please enter the date in the following format: MM/dd/yyyy<br/>");
 				session.setAttribute("errors", errors);
 				session.setAttribute("vehicle", newVech);
 				setDropdowns(request, newVech);
 				response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 			}
 
 		} else if (request.getParameter("addVehicle") != null) {
 			session.setAttribute("vehicle", null);
 			session.setAttribute("action", "addVehicle");
 			session.setAttribute("errors", null);
 			clearDropdowns(request);
 			response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 
 		} else if ((request.getParameter("editVehicle") != null)) {
 			session.setAttribute("action", "editVehicle");
 			session.setAttribute("errors", null);
 			response.sendRedirect("http://localhost:8080/Capstone/add_edit.jsp");
 
 		} else if ((request.getParameter("servVehicle") != null)) {
 			response.sendRedirect("http://localhost:8080/Capstone/checkList.jsp");
 
 		} else if ((request.getParameter("checkReset") != null)) {
 			response.sendRedirect("http://localhost:8080/Capstone/checkList.jsp");
 
 		} else if (request.getParameter("invoices") != null) {
 			response.sendRedirect("http://localhost:8080/Capstone/invoice_list.jsp");
 
 		} else if ((request.getParameter("checkSubmit") != null)) {
 			try {
 				String connectionURL = "jdbc:derby://localhost:1527/sun-appserv-samples;create=true";
 				Connection conn = null;
 				ResultSet rs;
 				conn = DriverManager.getConnection(connectionURL);
 				Statement statement = conn.createStatement();
 				Inspection inspect = setInspection(request);
 				Vehicle v = (Vehicle) session.getAttribute("vehicle");
 
 				String sql = "INSERT INTO Inspection(dateoi, notes, tpress, cataconv, muffpipes, exhclamhang, fuelfilter, enginefilter, cabinfilter, brakeline, gaskets, hoses, belts, washfluid, difffluid, brakefluid, steerfluid, coolfluid, tranfluid, wipeblades, horn, inlights, exlights,vechid) VALUES("
 						+ "'"
 						+ inspect.getDateoi()
 						+ "',"
 						+ "'"
 						+ inspect.getNotes()
 						+ "',"
 						+ "'"
 						+ inspect.getTpress()
 						+ "',"
 						+ "'"
 						+ inspect.getCataconv()
 						+ "',"
 						+ "'"
 						+ inspect.getMuffpipes()
 						+ "',"
 						+ "'"
 						+ inspect.getExhclamhang()
 						+ "',"
 						+ "'"
 						+ inspect.getFuelfilter()
 						+ "',"
 						+ "'"
 						+ inspect.getEnginefilter()
 						+ "',"
 						+ "'"
 						+ inspect.getCabinfilter()
 						+ "',"
 						+ "'"
 						+ inspect.getBrakeline()
 						+ "',"
 						+ "'"
 						+ inspect.getGaskets()
 						+ "',"
 						+ "'"
 						+ inspect.getHoses()
 						+ "',"
 						+ "'"
 						+ inspect.getBelts()
 						+ "',"
 						+ "'"
 						+ inspect.getWashfluid()
 						+ "',"
 						+ "'"
 						+ inspect.getDifffluid()
 						+ "',"
 						+ "'"
 						+ inspect.getBrakefluid()
 						+ "',"
 						+ "'"
 						+ inspect.getSteerfluid()
 						+ "',"
 						+ "'"
 						+ inspect.getCoolfluid()
 						+ "',"
 						+ "'"
 						+ inspect.getTranfluid()
 						+ "',"
 						+ "'"
 						+ inspect.getWipeblades()
 						+ "',"
 						+ "'"
 						+ inspect.getHorn()
 						+ "',"
 						+ "'"
 						+ inspect.getInlights()
 						+ "',"
 						+ "'"
 						+ inspect.getExlights() + "'," + v.getVechid() + ")";
 				statement.executeUpdate(sql);
 
 				// UPDATE INSPECTION TABLE IN VECH_VIEW
 				sql = "SELECT * FROM Inspection WHERE vechid=" + v.getVechid();
 
 				rs = statement.executeQuery(sql);
 
 				// check if rs has data
 				// if it does send an arrayList of inspections to vech view
 				// if it doesn't just go to vech_view
 				ArrayList<Inspection> inspects = new ArrayList<Inspection>();
 				while (rs.next()) {
 
 					Inspection i = new Inspection();
 
 					// set rs's to Vehicle v
 					i.setVechid(rs.getInt("vechid"));
 					i.setInspectid(rs.getInt("inspectid"));
 					i.setDateoi(rs.getString("dateoi"));
 					i.setNotes(rs.getString("notes"));
 					i.setTpress(rs.getString("tpress"));
 					i.setCataconv(rs.getString("cataconv"));
 					i.setMuffpipes(rs.getString("muffpipes"));
 					i.setExhclamhang(rs.getString("exhclamhang"));
 					i.setFuelfilter(rs.getString("fuelfilter"));
 					i.setEnginefilter(rs.getString("enginefilter"));
 					i.setCabinfilter(rs.getString("cabinfilter"));
 					i.setBrakeline(rs.getString("brakeline"));
 					i.setGaskets(rs.getString("gaskets"));
 					i.setHoses(rs.getString("hoses"));
 					i.setBelts(rs.getString("belts"));
 					i.setWashfluid(rs.getString("washfluid"));
 					i.setDifffluid(rs.getString("difffluid"));
 					i.setBrakefluid(rs.getString("brakefluid"));
 					i.setSteerfluid(rs.getString("steerfluid"));
 					i.setCoolfluid(rs.getString("coolfluid"));
 					i.setTranfluid(rs.getString("tranfluid"));
 					i.setWipeblades(rs.getString("wipeblades"));
 					i.setHorn(rs.getString("horn"));
 					i.setInlights(rs.getString("inlights"));
 					i.setExlights(rs.getString("exlights"));
 
 					inspects.add(i);
 				}
 
 				session.setAttribute("inspects", inspects);
 
 				response.sendRedirect("http://localhost:8080/Capstone/vech_view.jsp");
 			} catch (SQLException e) {
 				out.print(e.getMessage());
 			}
 
 		} else if (request.getParameter("logout") != null) {
 			session.setAttribute("user", null);
 			response.sendRedirect("http://localhost:8080/Capstone/Login.jsp");
 		} else if (request.getParameter("createUser") != null) {
 			Map<String, String> userErrors = new HashMap<String, String>(10);
 			try {
 				String connectionURL = "jdbc:derby://localhost:1527/sun-appserv-samples;create=true";
 				Connection conn = null;
 				ResultSet rs;
 				conn = DriverManager.getConnection(connectionURL);
 				Statement statement = conn.createStatement();
 				User u = new User();
 				String sql = "SELECT * FROM Users";
 
 				rs = statement.executeQuery(sql);
 				// Validation for creating user
 				while (rs.next()) {
 					// User type Validation
 					if (request.getParameter("userTypeDD").equals("Choose")) {
 						userErrors.put("typeError",
 								"Please choose the customer type<br/>");
 					} else {
 						u.setUsertype(request.getParameter("userTypeDD"));
 					}
 					// Username Validation
 					if (request.getParameter("userName").equals("")
 							|| rs.getString("userName").equals(
 									request.getParameter("userName"))) {
 						userErrors.put("userError",
 								"Please enter a valid User Name<br/>");
 					} else {
 						u.setUsername(request.getParameter("userName"));
 					}
 					// Password Validation
 					if (request.getParameter("password").equals("")
 							|| !request.getParameter("password").equals(
 									request.getParameter("retypePass"))) {
 						userErrors.put("passError", "Please enter a password");
 						userErrors.put("rePass",
 								"Please enter a matching password!<br/>");
 					} else {
 						u.setPassword(request.getParameter("password"));
 					}
 					// Email Validation
 					if (request.getParameter("emailAddy").equals("")) {
 						userErrors.put("emailError",
 								"Please enter a email address<br/>");
 					} else if (rs.getString("email").equals(
 							request.getParameter("emailAddy"))) {
 						userErrors
 								.put("errorsSQLEmail",
 										"Email already exsists, Please login or use other email address!<br/>");
 					} else {
 						u.setEmail(request.getParameter("emailAddy"));
 					}
 					// First Name Validation
 					if (request.getParameter("firstName").equals("")) {
 						userErrors.put("firstError",
 								"Please enter your first name<br/>");
 					} else {
 						u.setFirstname(request.getParameter("firstName"));
 					}
 					// Last name Validation
 					if (request.getParameter("lastName").equals("")) {
 						userErrors.put("lastError",
 								"Please enter your last name<br/>");
 					} else {
 						u.setLastname(request.getParameter("lastName"));
 					}
 					// Address Validation
 					if (request.getParameter("address").equals("")) {
 						userErrors.put("addyError",
 								"Please enter your address<br/>");
 					} else {
 						u.setAddress(request.getParameter("address"));
 					}
 					// City Validation
 					if (request.getParameter("city").equals("")) {
 						userErrors.put("cityError",
 								"Please enter your city<br/>");
 					} else {
 						u.setCity(request.getParameter("city"));
 					}
 					// Province Validation
 					if (request.getParameter("province").equals("")) {
 						userErrors.put("provError",
 								"Please enter your province<br/>");
 					} else {
 						u.setProvince(request.getParameter("province"));
 					}
 					// Postal Code Validation
 					if (request.getParameter("postalCode").equals("")
 							|| request.getParameter("postalCode").length() != 6) {
 						userErrors.put("postalError",
 								"Please enter a valid postal code<br/>");
 					} else {
 						u.setPostal(request.getParameter("postalCode"));
 					}
 					// Phone Validation
 					if (request.getParameter("phone").equals("")
 							|| request.getParameter("phone").length() != 10
 							|| Pattern.matches("[0-9]+",
 									request.getParameter("phone")) == false) {
 						userErrors.put("phoneError",
 								"Please enter a valid phone Number<br/>");
 					} else {
 						u.setPhone(request.getParameter("phone"));
 					}
 					// Fax Validation
 					if (request.getParameter("fax").length() != 10
 							|| Pattern.matches("[0-9]+",
 									request.getParameter("fax")) == false) {
 						userErrors.put("faxError",
 								"Please enter a valid fax number<br/>");
 					} else {
 						u.setFax(request.getParameter("fax"));
 					}
 
 					// VALIDATION ENDS
 
 				}
 				if (!userErrors.isEmpty()) {
 					session.setAttribute("errors", userErrors);
 					response.sendRedirect("register.jsp");
 				} else {
 					sql = "INSERT INTO Users(firstName, lastName, address, city, province, postal, phone, fax, email, userName, password, userType) VALUES('"
 							+ u.getFirstname()
 							+ "','"
 							+ u.getLastname()
 							+ "','"
 							+ u.getAddress()
 							+ "','"
 							+ u.getCity()
 							+ "','"
 							+ u.getProvince()
 							+ "','"
 							+ u.getPostal()
 							+ "','"
 							+ u.getPhone()
 							+ "','"
 							+ u.getFax()
 							+ "','"
 							+ u.getEmail()
 							+ "','"
 							+ u.getUsername()
 							+ "','"
 							+ u.getPassword() + "','" + u.getUsertype() + "')";
 					statement.executeUpdate(sql);
 
 					session.setAttribute("newUser", u);
 
 					session.setAttribute("buttonPressed", "createUser");
 					response.sendRedirect("newUser.jsp");
 				}
 
 				statement.close();
 				conn.close();
 
 			} catch (SQLException e) {
 				System.out.println(e.getMessage());
 			}
 
 		} else {
 			try {
 				String connectionURL = "jdbc:derby://localhost:1527/sun-appserv-samples;create=true";
 				Connection conn = null;
 				ResultSet rs;
 				conn = DriverManager.getConnection(connectionURL);
 				Statement statement = conn.createStatement();
 
 				Vehicle v = (Vehicle) session.getAttribute("vehicle");
 
 				String sql = "DELETE FROM Vehicle WHERE vechID="
 						+ v.getVechid();
 
 				statement.executeUpdate(sql);
 
 				User u = (User) session.getAttribute("user");
 
 				sql = "SELECT * FROM Vehicle WHERE userId=" + u.getUserid();
 				rs = statement.executeQuery(sql);
 				ArrayList<Vehicle> vechList = new ArrayList<Vehicle>();
 
 				while (rs.next()) {
 					v = new Vehicle();
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
 			} catch (SQLException e) {
 				System.out.println(e.getMessage());
 			}
 		}
 	}
 
 	private void setDropdowns(HttpServletRequest request, Vehicle v) {
 		HttpSession session = request.getSession();
 		// Set the dropdowns to proper value
 		// Engine
 		if (null == v.getEngine())
 			clearDropdowns(request);
 		else if (v.getEngine().equals("4 Cylinder"))
 			session.setAttribute("selectedV4", "selected");
 		else if (v.getEngine().equals("V6"))
 			session.setAttribute("selectedV6", "selected");
 		else if (v.getEngine().equals("V8"))
 			session.setAttribute("selectedV8", "selected");
 		else if (v.getEngine().equals("V10"))
 			session.setAttribute("selectedV10", "selected");
 		else if (v.getEngine().equals("V12"))
 			session.setAttribute("selectedV12", "selected");
 		else if (v.getEngine().equals("Diesel"))
 			session.setAttribute("selectedD", "selected");
 		// Tranny
 		if (null == v.getTranny())
 			clearDropdowns(request);
 		else if (v.getTranny().equals("Manual"))
 			session.setAttribute("selectedM", "selected");
 		else if (v.getTranny().equals("auto"))
 			session.setAttribute("selectedA", "selected");
 	}
 
 	private void clearDropdowns(HttpServletRequest request) {
 		HttpSession session = request.getSession();
 		// Set the dropdowns to proper value
 		// Engine
 		session.setAttribute("selectedV4", null);
 		session.setAttribute("selectedV6", null);
 		session.setAttribute("selectedV8", null);
 		session.setAttribute("selectedV10", null);
 		session.setAttribute("selectedV12", null);
 		session.setAttribute("selectedD", null);
 		// Tranny
 		session.setAttribute("selectedM", null);
 		session.setAttribute("selectedA", null);
 	}
 
 	private Inspection setInspection(HttpServletRequest request) {
 		Inspection inspect = new Inspection();
 		if (null == request.getParameter("extLights")) {
 			inspect.setExlights("Y");
 		} else if (request.getParameter("extLights").equals("checked")) {
 			inspect.setExlights("G");
 		} else if (request.getParameter("extLights").equals("damaged")) {
 			inspect.setExlights("R");
 		}
 
 		if (null == request.getParameter("intLights")) {
 			inspect.setInlights("Y");
 		} else if (request.getParameter("intLights").equals("checked")) {
 			inspect.setInlights("G");
 		} else if (request.getParameter("intLights").equals("damaged")) {
 			inspect.setInlights("R");
 		}
 
 		if (null == request.getParameter("horn")) {
 			inspect.setHorn("Y");
 		} else if (request.getParameter("horn").equals("checked")) {
 			inspect.setHorn("G");
 		} else if (request.getParameter("horn").equals("damaged")) {
 			inspect.setHorn("R");
 		}
 
 		if (null == request.getParameter("wipBlades")) {
 			inspect.setWipeblades("Y");
 		} else if (request.getParameter("wipBlades").equals("checked")) {
 			inspect.setWipeblades("G");
 		} else if (request.getParameter("wipBlades").equals("damaged")) {
 			inspect.setWipeblades("R");
 		}
 
 		if (null == request.getParameter("transFluid")) {
 			inspect.setTranfluid("Y");
 		} else if (request.getParameter("transFluid").equals("checked")) {
 			inspect.setTranfluid("G");
 		} else if (request.getParameter("transFluid").equals("damaged")) {
 			inspect.setTranfluid("R");
 		}
 
 		if (null == request.getParameter("coolFluid")) {
 			inspect.setCoolfluid("Y");
 		} else if (request.getParameter("coolFluid").equals("checked")) {
 			inspect.setCoolfluid("G");
 		} else if (request.getParameter("coolFluid").equals("damaged")) {
 			inspect.setCoolfluid("R");
 		}
 
 		// Check
 		if (null == request.getParameter("posFluid")) {
 			inspect.setSteerfluid("Y");
 		} else if (request.getParameter("posFluid").equals("checked")) {
 			inspect.setSteerfluid("G");
 		} else if (request.getParameter("posFluid").equals("damaged")) {
 			inspect.setSteerfluid("R");
 		}
 
 		if (null == request.getParameter("brakFluid")) {
 			inspect.setBrakefluid("Y");
 		} else if (request.getParameter("brakFluid").equals("checked")) {
 			inspect.setBrakefluid("G");
 		} else if (request.getParameter("brakFluid").equals("damaged")) {
 			inspect.setBrakefluid("R");
 		}
 
 		if (null == request.getParameter("diffFluid")) {
 			inspect.setDifffluid("Y");
 		} else if (request.getParameter("diffFluid").equals("checked")) {
 			inspect.setDifffluid("G");
 		} else if (request.getParameter("diffFluid").equals("damaged")) {
 			inspect.setDifffluid("R");
 		}
 
 		if (null == request.getParameter("washFluid")) {
 			inspect.setWashfluid("Y");
 		} else if (request.getParameter("washFluid").equals("checked")) {
 			inspect.setWashfluid("G");
 		} else if (request.getParameter("washFluid").equals("damaged")) {
 			inspect.setWashfluid("R");
 		}
 
 		if (null == request.getParameter("belts")) {
 			inspect.setBelts("Y");
 		} else if (request.getParameter("belts").equals("checked")) {
 			inspect.setBelts("G");
 		} else if (request.getParameter("belts").equals("damaged")) {
 			inspect.setBelts("R");
 		}
 
 		if (null == request.getParameter("hoses")) {
 			inspect.setHoses("Y");
 		} else if (request.getParameter("hoses").equals("checked")) {
 			inspect.setHoses("G");
 		} else if (request.getParameter("hoses").equals("damaged")) {
 			inspect.setHoses("R");
 		}
 
 		if (null == request.getParameter("gasket")) {
 			inspect.setGaskets("Y");
 		} else if (request.getParameter("gasket").equals("checked")) {
 			inspect.setGaskets("G");
 		} else if (request.getParameter("gasket").equals("damaged")) {
 			inspect.setGaskets("R");
 		}
 
 		if (null == request.getParameter("brakLines")) {
 			inspect.setBrakeline("Y");
 		} else if (request.getParameter("brakLines").equals("checked")) {
 			inspect.setBrakeline("G");
 		} else if (request.getParameter("brakLines").equals("damaged")) {
 			inspect.setBrakeline("R");
 		}
 
 		if (null == request.getParameter("cabFilter")) {
 			inspect.setCabinfilter("Y");
 		} else if (request.getParameter("cabFilter").equals("checked")) {
 			inspect.setCabinfilter("G");
 		} else if (request.getParameter("cabFilter").equals("damaged")) {
 			inspect.setCabinfilter("R");
 		}
 
 		if (null == request.getParameter("engAirFilter")) {
 			inspect.setEnginefilter("Y");
 		} else if (request.getParameter("engAirFilter").equals("checked")) {
 			inspect.setEnginefilter("G");
 		} else if (request.getParameter("engAirFilter").equals("damaged")) {
 			inspect.setEnginefilter("R");
 		}
 
 		if (null == request.getParameter("fuelFilter")) {
 			inspect.setFuelfilter("Y");
 		} else if (request.getParameter("fuelFilter").equals("checked")) {
 			inspect.setFuelfilter("G");
 		} else if (request.getParameter("fuelFilter").equals("damaged")) {
 			inspect.setFuelfilter("R");
 		}
 
 		if (null == request.getParameter("exhClamps")) {
 			inspect.setExhclamhang("Y");
 		} else if (request.getParameter("exhClamps").equals("checked")) {
 			inspect.setExhclamhang("G");
 		} else if (request.getParameter("exhClamps").equals("damaged")) {
 			inspect.setExhclamhang("R");
 		}
 
 		if (null == request.getParameter("muffPipes")) {
 			inspect.setMuffpipes("Y");
 		} else if (request.getParameter("muffPipes").equals("checked")) {
 			inspect.setMuffpipes("G");
 		} else if (request.getParameter("muffPipes").equals("damaged")) {
 			inspect.setMuffpipes("R");
 		}
 
 		if (null == request.getParameter("setCataconv")) {
 			inspect.setCataconv("Y");
 		} else if (request.getParameter("catConv").equals("checked")) {
 			inspect.setCataconv("G");
 		} else if (request.getParameter("catConv").equals("damaged")) {
 			inspect.setCataconv("R");
 		}
 
 		if (null == request.getParameter("tirePress")) {
 			inspect.setTpress("Y");
 		} else if (request.getParameter("tirePress").equals("checked")) {
 			inspect.setTpress("G");
 		} else if (request.getParameter("tirePress").equals("damaged")) {
 			inspect.setTpress("R");
 		}
 
 		inspect.setNotes(request.getParameter("miscNotes"));
 		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
 		Date doc = new Date();
 
 		inspect.setDateoi(df.format(doc));
 
 		return inspect;
 	}
 }
