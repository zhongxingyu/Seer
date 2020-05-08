 package webServer;
 
 import java.net.InetSocketAddress;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
import webClient.WebMessage;
 import webServer.messages.DropOfferRequest;
 import webServer.messages.ErrorResponse;
 import webServer.messages.GetProfileRequest;
 import webServer.messages.GetProfileResponse;
 import webServer.messages.LaunchOfferRequest;
 import webServer.messages.LaunchOfferResponse;
 import webServer.messages.LoginRequest;
 import webServer.messages.LoginResponse;
 import webServer.messages.LogoutRequest;
 import webServer.messages.OkResponse;
 import webServer.messages.RegisterProfileRequest;
 import webServer.messages.SetProfileRequest;
 import data.LoginCred;
 import data.Service;
 import data.UserEntry;
 import data.UserProfile;
 import data.UserProfile.UserRole;
 
 public class WebServer {
 	static String		name	= "root";
 	static String		pass	= "student";
 	static Connection	conn;
 
 	static {
 		System.out.println("[WebServer:static] Begin");
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		String url = "jdbc:mysql://127.0.0.1:3306/auction_house";
 
 		try {
 			conn = DriverManager.getConnection(url, name, pass);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println("[WebServer:static] End");
 	}
 
 	public byte[] login(byte[] byteReq) {
 		System.out.println("[WebServer:login] Begin");
 
 		Object obj = WebMessage.deserialize(byteReq);
 		if (!(obj instanceof LoginRequest)) {
 			System.out.println("[WebServer:login] Wrong message... waiting LoginRequest");
 			return WebMessage.serialize(new ErrorResponse("Wrong message... waiting LoginRequest"));
 		}
 
 		LoginRequest req = (LoginRequest) obj;
 		LoginCred cred = req.getLoginCred();
 
 		System.out.println("[WebServer:login] cred: " + cred);
 
 		Statement st;
 		try {
 			st = conn.createStatement();
 
 			String query = "SELECT *" + " FROM users" + " WHERE username = " + cred.getUsername() + " AND password = "
 					+ cred.getPassword();
 			ResultSet rs = st.executeQuery(query);
 
 			if (rs.first()) {
 				Integer id = rs.getInt("id");
 
 				String online_as_field = "as_buyer";
 				if (cred.getRole() == UserRole.SELLER) {
 					online_as_field = "as_seller";
 				}
 
 				query = "UPDATE users" + " SET " + online_as_field + " =  1 , address = " + cred.getAddress().getHostName()
 						+ ", port = " + cred.getAddress().getPort() + " WHERE id = " + id;
 				st.executeUpdate(query);
 
 				UserProfile userProfile = new UserProfile();
 				userProfile.setUsername(rs.getString("username"));
 				userProfile.setFirstName(rs.getString("first_name"));
 				userProfile.setLastName(rs.getString("last_name"));
 				userProfile.setPassword(rs.getString("password"));
 				userProfile.setRole(cred.getRole());
 				userProfile.setLocation(rs.getString("location"));
 				
 				st.close();
 				System.out.println("[WebServer:login] End (lLoginResponse)");
 				return WebMessage.serialize(new LoginResponse(userProfile));
 			} else {
 				System.out.println("[WebServer:login] Wrong username or password");
 				return WebMessage.serialize(new ErrorResponse("Wrong username or password"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 
 			return WebMessage.serialize(new ErrorResponse("Something bad happend at server"));
 		}
 	}
 
 	public byte[] logout(byte[] byteReq) {
 		System.out.println("[WebServer:logout] Begin");
 
 		Object obj = WebMessage.deserialize(byteReq);
 		if (!(obj instanceof LogoutRequest)) {
 			System.out.println("[WebServer:logout] Wrong message... waiting LogoutRequest");
 			return WebMessage.serialize(new ErrorResponse("Wrong message... waiting LogoutRequest"));
 		}
 
 		LogoutRequest req = (LogoutRequest) obj;
 		LoginCred cred = req.getCred();
 
 		try {
 			Statement st = conn.createStatement();
 
 			String online_as_field = "as_buyer";
 			if (cred.getRole() == UserRole.SELLER) {
 				online_as_field = "as_seller";
 			}
 
 			String query = "UPDATE users" + " SET " + online_as_field + " = 0 WHERE id = " + cred.getId();
 			st.executeUpdate(query);
 
 			query = "UPDATE services" + " SET active = " + 0 + " WHERE id = " + cred.getId() + " AND user_role = "
 					+ cred.getRole().ordinal();
 			st.executeUpdate(query);
 
 			st.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 
 			return WebMessage.serialize(new ErrorResponse("Something bad happend at server"));
 		}
 
 		System.out.println("[WebServer:logout] End");
 		return WebMessage.serialize(new OkResponse());
 	}
 
 	public byte[] launchOffer(byte[] byteReq) {
 		System.out.println("[WebServer:logout] Begin");
 
 		Object obj = WebMessage.deserialize(byteReq);
 		if (!(obj instanceof LaunchOfferRequest)) {
 			System.out.println("[WebServer:launchOffer] Wrong message... waiting LaunchOfferRequest");
 			return WebMessage.serialize(new ErrorResponse("Wrong message... waiting LaunchOfferRequest"));
 		}
 
 		LaunchOfferRequest req = (LaunchOfferRequest) obj;
 		LoginCred cred = req.getCred();
 		Service service = req.getService();
 
 		try {
 			Statement st = conn.createStatement();
 			String query = "SELECT *" + " FROM services" + " WHERE user_id = " + cred.getId() + " AND name = "
 					+ service.getName() + " AND user_role = " + cred.getRole().ordinal();
 			ResultSet rs = st.executeQuery(query);
 
 			if (rs.first()) {
 				// Activez un serviciu
 				query = "UPDATE services SET active = 1 WHERE user_id = " + cred.getId() + " AND name = "
 						+ service.getName() + " AND user_role = " + cred.getRole().ordinal();
 
 				query = "SELECT * FROM services s JOIN users u ON s.user_id = u.id WHERE s.name = " + service.getName()
 						+ " AND s.user_role = " + UserRole.SELLER.ordinal() + " AND s.active = 1";
 				rs = st.executeQuery(query);
 
 				ArrayList<UserEntry> sellers = new ArrayList<UserEntry>();
 				while (rs.next()) {
 					UserEntry userEntry = new UserEntry();
 					userEntry.setUsername(rs.getString("u.username"));
 					userEntry.setName(rs.getString("u.first_name") + rs.getString("u.last_name"));
 					InetSocketAddress address = new InetSocketAddress(rs.getString("u.address"), rs.getInt("u.port"));
 					userEntry.setAddress(address);
 
 					sellers.add(userEntry);
 				}
 				rs.close();
 				service.setUsers(sellers);
 			} else {
 				// Adaug un serviciu, si daca role-ul e SELLER il activez
 				if (cred.getRole() == UserRole.SELLER) {
 					query = "INSERT INTO services(name, time, price, user_id, active, user_role) VALUES ("
 							+ service.getName() + ", " + service.getTime() + ", " + service.getPrice() + ", "
 							+ cred.getId() + ", " + 1 + ", " + cred.getRole() + ")";
 					st.executeUpdate(query);
 
 					query = "SELECT * FROM services s JOIN users u ON s.user_id = u.id WHERE s.name = "
 							+ service.getName() + " AND s.user_role = " + UserRole.BUYER + " AND s.active = " + 1;
 					rs = st.executeQuery(query);
 
 					ArrayList<UserEntry> buyers = new ArrayList<UserEntry>();
 					while (rs.next()) {
 						UserEntry userEntry = new UserEntry();
 						userEntry.setUsername(rs.getString("u.username"));
 						userEntry.setName(rs.getString("u.first_name") + rs.getString("u.last_name"));
 						InetSocketAddress address = new InetSocketAddress(rs.getString("u.address"),
 								rs.getInt("u.port"));
 						userEntry.setAddress(address);
 
 						buyers.add(userEntry);
 					}
 					rs.close();
 					service.setUsers(buyers);
 				} else {
 					query = "INSERT INTO services(name, time, price, user_id, active, user_role) VALUES ("
 							+ service.getName() + ", " + service.getTime() + ", " + service.getPrice() + ", "
 							+ cred.getId() + ", " + 0 + ", " + cred.getRole() + ")";
 					st.executeUpdate(query);
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println("[WebServer:launchOffer] End");
 		return WebMessage.serialize(new LaunchOfferResponse(service));
 	}
 
 	public byte[] dropOffer(byte[] byteReq) {
 		System.out.println("[WebServer:dropOffer] Begin");
 
 		Object obj = WebMessage.deserialize(byteReq);
 		if (!(obj instanceof DropOfferRequest)) {
 			System.out.println("[WebServer:dropOffer] Wrong message... waiting DropOfferRequest");
 
 			return WebMessage.serialize(new ErrorResponse("Wrong message... waiting DropOfferRequest"));
 		}
 
 		DropOfferRequest req = (DropOfferRequest) obj;
 
 		try {
 			Statement st = conn.createStatement();
 			
 			// TODO: Fix this.
 			String query = "SELECT id FROM users WHERE username = " + req.getUsername();
 			ResultSet rs = st.executeQuery(query);
 
 			Integer id = rs.getInt("id");
 
 			query = "UPDATE services SET active = 0 WHERE name = " + req.getServiceName() + " AND user_id = "
 					+ id + " AND user_role = " + req.getUserRole().ordinal();
 			st.executeUpdate(query);
 			st.close();
 			
 			System.out.println("[WebServer: dropOffer] End");
 			return WebMessage.serialize(new OkResponse());
 		} catch (SQLException e) {
 			e.printStackTrace();
 
 			return WebMessage.serialize(new ErrorResponse("Something bad happend at server"));
 		}
 	}
 
 	public byte[] getProfile(byte[] byteReq) {
 		System.out.println("[WebServer:getProfile] Begin");
 
 		Object obj = WebMessage.deserialize(byteReq);
 		if (!(obj instanceof GetProfileRequest)) {
 			System.out.println("[WebServer:getProfile] Wrong message... waiting GetProfileRequest");
 			
 			return WebMessage.serialize(new ErrorResponse("Wrong message... waiting GetProfileRequest"));
 		}
 
 		GetProfileRequest req = (GetProfileRequest) obj;
 
 		try {
 			Statement st = conn.createStatement();
 			String query = "SELECT * FROM users WHERE username = " + req.getUsername();
 			ResultSet rs = st.executeQuery(query);
 
 			UserProfile profile = null;
 			if (rs.next()) {
 				profile = new UserProfile();
 				profile.setUsername(rs.getString("username"));
 				profile.setFirstName(rs.getString("first_name"));
 				profile.setLastName(rs.getString("last_name"));
 				// TODO: Anybody can see anyone's password;
 				profile.setPassword(rs.getString("password"));
 				profile.setLocation(rs.getString("location"));
 				// profile.setAvatar(avatar);
 			}
 			st.close();
 			
 			System.out.println("[WebServer:getProfile] End");
 			return WebMessage.serialize(new GetProfileResponse(profile));
 		} catch (SQLException e) {
 			e.printStackTrace();
 			
 			return WebMessage.serialize(new ErrorResponse("Something bad happend at server"));
 		}
 	}
 
 	public byte[] setProfile(byte[] byteReq) {
 		System.out.println("[WebServer:setProfile] Begin");
 
 		Object obj = WebMessage.deserialize(byteReq);
 		if (!(obj instanceof SetProfileRequest)) {
 			System.out.println("[WebServer:setProfile] Wrong message... waiting SetProfileRequest");
 
 			return WebMessage.serialize(new ErrorResponse("Wrong message... waiting SetProfileRequest"));
 		}
 
 		SetProfileRequest req = (SetProfileRequest) obj;
 		UserProfile profile = req.getUserProfile();
 
 		try {
 			Statement st = conn.createStatement();
 			String query = "UPDATE users SET first_name = " + profile.getFirstName() + ", last_name = "
 					+ profile.getLastName() + ", password = " + profile.getPassword() + ", location = "
 					+ profile.getLocation() + " WHERE username = " + profile.getUsername();
 			st.executeUpdate(query);
 			st.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 
 			return WebMessage.serialize(new ErrorResponse("Something bad happend at server"));
 		}
 		System.out.println("[WebServer:setProfile] End");
 		return WebMessage.serialize(new OkResponse());
 	}
 
 	public byte[] registerProfile(byte[] byteReq) {
 		System.out.println("Begin");
 
 		Object obj = WebMessage.deserialize(byteReq);
 		if (!(obj instanceof RegisterProfileRequest)) {
 			System.out.println("[WebServer:registerProfile] Wrong message... waiting RegisterProfileRequest");
 
 			return WebMessage.serialize(new ErrorResponse("Wrong message... waiting RegisterProfileRequest"));
 		}
 
 		RegisterProfileRequest req = (RegisterProfileRequest) obj;
 		UserProfile profile = req.getUserProfile();
 
 		try {
 			Statement st = conn.createStatement();
 			String query = "INSERT INTO users(username, first_name, last_name, password, location) VALUES ("
 					+ profile.getUsername() + ", " + profile.getFirstName() + ", " + profile.getLastName() + ", "
 					+ profile.getPassword() + ", " + profile.getLocation() + ")";
 			st.executeUpdate(query);
 			st.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			
 			return WebMessage.serialize(new ErrorResponse("Something bad happend at server"));
 		}
 
 		System.out.println("[WebServer:registerProfile] End");
 		return WebMessage.serialize(new OkResponse());
 	}
 }
