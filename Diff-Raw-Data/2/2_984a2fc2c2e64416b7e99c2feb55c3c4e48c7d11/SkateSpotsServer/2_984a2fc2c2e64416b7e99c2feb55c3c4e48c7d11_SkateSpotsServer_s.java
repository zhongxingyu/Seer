 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import org.simpleframework.http.Request;
 import org.simpleframework.http.Response;
 import org.simpleframework.http.ResponseWrapper;
 import org.simpleframework.http.Status;
 import org.simpleframework.http.core.Container;
 import org.simpleframework.http.core.ContainerServer;
 import org.simpleframework.transport.Server;
 import org.simpleframework.transport.connect.Connection;
 import org.simpleframework.transport.connect.SocketConnection;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.JsonPrimitive;
 
 public class SkateSpotsServer implements Container {
 
 	private final Executor executor;
 
 	public static void main(String[] list) throws Exception {
 		Container container = new SkateSpotsServer();
 		Server server = new ContainerServer(container);
 		@SuppressWarnings("resource")
 		Connection connection = new SocketConnection(server);
 		SocketAddress address = new InetSocketAddress(11337);
 		connection.connect(address);
 	}
 
 	public SkateSpotsServer() {
 		this.executor = Executors.newFixedThreadPool(10);
 	}
 
 	@Override
 	public void handle(Request request, Response response) {
 		Task task = new Task(request, response);
 		executor.execute(task);
 	}
 
 	public static class Task implements Runnable {
 
 		private final ResponseWrapper response;
 		private final Request request;
 		private PrintStream body;
 		private java.sql.Connection con; 
 		private Statement st;
 		private ResultSet res;
 
 		public Task(Request request, Response response) {
 			this.response = new ResponseWrapper(response);
 			this.request = request;
 			this.response.setContentType("application/json");
 			this.response.setValue("Server", "Skate Spots - Server 1.0");
 		}
 
 		@Override
 		public void run() {
 			try {
 				body = this.response.getPrintStream();
 				String content = request.getContent();
 				JsonObject obj = new JsonParser().parse(content).getAsJsonObject();
 				if (obj.get("key").getAsString().equals("ourKey")) { //TODO Define our key
 					Integer type = obj.get("type").getAsInt();
 					switch (type) {
 					case 0: login(obj);
 					break;
 					case 1: createUser(obj);
 					break;
 					case 2: setCurrentLocation(obj);
 					break;
 					case 3: getCurrentLocations(obj);
 					break;
 					case 4: createSkateSpot(obj);
 					break;
 					case 5: getSkateSpots(obj);
 					break;
 					default: response.setStatus(Status.BAD_REQUEST);
 					System.out.println("BAD_REQUEST");
 					break;
 					}
 				}
 				body.close();
 			} catch (IOException e) {
 				response.setStatus(Status.INTERNAL_SERVER_ERROR);
 				e.printStackTrace();
 			}
 		}
 
 		private void login(JsonObject obj) {
 			try {
 				// Creating required strings
 				String email = '"'+obj.get("email").getAsString()+'"';
 				String password = '"'+obj.get("password").getAsString()+'"';
 				String checkUser = "SELECT * FROM users WHERE email="+email+" AND pass="+password+";";
 				// Establish dbconnection and a statement, and execute the prepared sql
 				con = new DatabaseConnection().getDatabaseConnection();
 				st = con.createStatement();
 				res = st.executeQuery(checkUser);
 				System.out.println(new Timestamp(new Date().getTime())+": "+email+" is trying to login");
 				if (res.next()) {
 					JsonObject responseObj = new JsonObject();
 					responseObj.add("displayname", new JsonPrimitive(res.getString("displayname")));
 					// email and password matches
 					System.out.println(new Timestamp(new Date().getTime())+": "+email+" has been accepted");
 					response.setStatus(Status.OK);
 					body.println(responseObj.toString());
 				} else {
 					// wrong email or password
 					System.out.println(new Timestamp(new Date().getTime())+": "+email+" has been rejected");
 					response.setStatus(Status.BAD_REQUEST);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				response.setStatus(Status.INTERNAL_SERVER_ERROR);
 			} finally {
 				close();
 			}
 		}
 
 		private void createUser(JsonObject obj) {
 			try {
 				// Creating required strings
 				String email = '"'+obj.get("email").getAsString()+'"';
 				String password = '"'+obj.get("password").getAsString()+'"';
 				String displayname = '"'+obj.get("displayname").getAsString()+'"';
 				String bluid = '"'+obj.get("bluid").getAsString()+'"';
 				String checkIfExists = "SELECT * FROM users WHERE email="+email+";";
 				// Establish dbconnection and a statement, and execute the prepared sql
 				con = new DatabaseConnection().getDatabaseConnection();
 				st = con.createStatement();
 				res = st.executeQuery(checkIfExists);
 				System.out.println(new Timestamp(new Date().getTime())+": "+"Attempts to create user:"+email);
 				if (!res.next()) {
 					// User does not exist and is therefore created
 					String createUser = "INSERT INTO users(email,pass,displayname,bluid) VALUES ("+email+
 											", "+password+", "+displayname+", "+bluid+");";
 					st.execute(createUser);
 					System.out.println(new Timestamp(new Date().getTime())+": "+"User does not exist: Created user");
 					response.setStatus(Status.OK);
 				} else {
 					// User with the given email already exists
 					System.out.println(new Timestamp(new Date().getTime())+": "+"User already exists: User not created");
 					response.setStatus(Status.BAD_REQUEST);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				response.setStatus(Status.INTERNAL_SERVER_ERROR);
 			} finally {
 				close();
 			}
 		}
 		
 		private void setCurrentLocation(JsonObject obj) {
 			try {
 				// Creating required strings
 				String email = '"'+obj.get("email").getAsString()+'"';
 				double latitude = obj.get("latitude").getAsDouble();
 				double longitude = obj.get("longitude").getAsDouble();
 				String updateLocation = "UPDATE users SET latitude="+latitude+", longitude="+longitude+" WHERE email="+email+";";
 				// Establish dbconnection and a statement, and execute the prepared sql
 				con = new DatabaseConnection().getDatabaseConnection();
 				st = con.createStatement();
 				st.execute(updateLocation);
 				System.out.println(new Timestamp(new Date().getTime())+": "+"Updated the location of "+email);
 				// We had success
 				response.setStatus(Status.OK);
 			} catch (Exception e) {
 				e.printStackTrace();
 				response.setStatus(Status.INTERNAL_SERVER_ERROR);
 			} finally {
 				close();
 			}
 		}
 
 		private void getCurrentLocations(JsonObject obj) {
 			try {
 				// Creating required strings
 				String email = obj.get("email").getAsString();
 				String subQuery = "SELECT email, displayname, latitude, longitude, locationtime "+
 									"FROM users "+
 									"WHERE latitude IS NOT NULL AND longitude IS NOT NULL";
 				String getUserLocations = "SELECT email, displayname, latitude, longitude "+
 											"FROM ("+subQuery+") withOutNull "+
 											"WHERE email<>'"+email+"' "+
 											"AND DATE_SUB(NOW(), INTERVAL 1 HOUR) < locationtime";
 				// Establish dbconnection and a statement, and execute the prepared sql
 				con = new DatabaseConnection().getDatabaseConnection();
 				st = con.createStatement();
 				res = st.executeQuery(getUserLocations);
 				JsonArray resLocations = new JsonArray();
 				while (res.next()) {
 					String resEmail = res.getString("email");
 					String resDisplayname = res.getString("displayname");
 					Double resLatitude = res.getDouble("latitude");
 					Double resLongitude = res.getDouble("longitude");
 					JsonObject resRow = new JsonObject();
 					resRow.add("email", new JsonPrimitive(resEmail));
 					resRow.add("displayname", new JsonPrimitive(resDisplayname));
 					resRow.add("latitude", new JsonPrimitive(resLatitude));
 					resRow.add("longitude", new JsonPrimitive(resLongitude));
 					resLocations.add(resRow);
 				}
 				System.out.println(new Timestamp(new Date().getTime())+": "+email+" requested currenct location of other users.");
 				response.setStatus(Status.OK);
 				body.println(resLocations.toString());
 			} catch (Exception e) {
 				e.printStackTrace();
 				response.setStatus(Status.INTERNAL_SERVER_ERROR);
 			} finally {
 				close();
 			}
 		}
 
 		private void createSkateSpot(JsonObject obj) {
 			try {
 				// Creating required strings
 				String author = obj.get("author").getAsString();
 				String name = obj.get("name").getAsString();
 				String description = obj.get("description").getAsString();
 				String spottype = obj.get("spottype").getAsString();
 				Double latitude = obj.get("latitude").getAsDouble();
 				Double longitude = obj.get("longitude").getAsDouble();
 				JsonArray wifi = obj.get("wifi").getAsJsonArray();
 				String newSkateSpot = "INSERT INTO skatespots(author,name,description,type,latitude,longitude) VALUES ('"+
 						author+"', '"+name+"', '"+description+"', '"+spottype+"', "+latitude+", "+longitude+");";
 				String getLastInsertId = "SELECT LAST_INSERT_ID();";
 				// Establish dbconnection and a statement, and execute the prepared sql
 				con = new DatabaseConnection().getDatabaseConnection();
 				st = con.createStatement();
 				st.execute(newSkateSpot);
 				res = st.executeQuery(getLastInsertId);
 				System.out.println(new Timestamp(new Date().getTime())+": "+author+" created a new skatespot called "+name);
 				res.next();
 				int lastInsertID = res.getInt(1);
 				if (wifi.size() == 0){
 					System.out.println("NO WIFI!!!");
 				}
 				if (wifi.size() > 0) {
 					Iterator<JsonElement> iterator = wifi.iterator();
 					while (iterator.hasNext()) {
 						String ssid = iterator.next().getAsString();
 						String wifiToInsert = "INSERT INTO wifi VALUES("+lastInsertID+", '"+ssid+"');";
 						st.executeUpdate(wifiToInsert);
 						System.out.println(new Timestamp(new Date().getTime())+": wifi with ssid "+ssid+" attached to skatespot (id:"+lastInsertID+")");
 					}
 				}
 				
 			} catch (Exception e) {
 				e.printStackTrace();
 				response.setStatus(Status.INTERNAL_SERVER_ERROR);
 			} finally {
 				close();
 			}
 			
 		}
 
 		private void getSkateSpots(JsonObject obj) {
 			try {
 				// Creating required strings
 				String allSkateSpots = "SELECT * FROM skatespots;";
 				// Establish dbconnection and a statement, and execute the prepared sql
 				con = new DatabaseConnection().getDatabaseConnection();
 				st = con.createStatement();
 				res = st.executeQuery(allSkateSpots);
 				JsonArray jsonArray = new JsonArray();
 				while (res.next()) {
 					JsonObject resRow = new JsonObject();
 					int id = res.getInt("id");
 					resRow.add("id", new JsonPrimitive(id));
 					resRow.add("name", new JsonPrimitive(res.getString("name")));
 					resRow.add("description", new JsonPrimitive(res.getString("description")));
 					resRow.add("spottype", new JsonPrimitive(res.getString("type")));
 					resRow.add("author", new JsonPrimitive(res.getString("author")));
 					resRow.add("latitude", new JsonPrimitive(res.getDouble("latitude")));
 					resRow.add("longitude", new JsonPrimitive(res.getDouble("longitude")));
 					JsonArray wifi = new JsonArray();
					String getWifi = "SELECT ssid FROM wifi WHERE id="+id+";";
 					ResultSet retrievedWifi = st.executeQuery(getWifi);
 					while (retrievedWifi.next()) {
 						wifi.add(new JsonPrimitive(retrievedWifi.getString(1)));
 					}
 					resRow.add("wifi", wifi);
 				}
 				System.out.println(new Timestamp(new Date().getTime())+": user "+obj.get("email").getAsString()+" request current skatespots");
 				response.setStatus(Status.OK);
 				body.println(jsonArray.toString());
 			} catch (Exception e) {
 				response.setStatus(Status.BAD_REQUEST);
 				e.printStackTrace();
 			}
 		}
 
 		// Closes the remains of the database connection
 		private void close() {
 			try {
 				if (res != null) {
 					res.close();
 				}
 				if (st != null) {
 					st.close();
 				}
 				if (con != null) {
 					con.close();
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
