 package webServer;
 
 import java.net.InetSocketAddress;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import config.WebServerConfig;
 
 import webServer.messages.DropOfferRequest;
 import webServer.messages.ErrorResponse;
 import webServer.messages.GetProfileRequest;
 import webServer.messages.GetProfileResponse;
 import webServer.messages.LaunchOfferRequest;
 import webServer.messages.LaunchOfferResponse;
 import webServer.messages.LoadOffersRequest;
 import webServer.messages.LoadOffersResponse;
 import webServer.messages.LoginRequest;
 import webServer.messages.LoginResponse;
 import webServer.messages.LogoutRequest;
 import webServer.messages.OkResponse;
 import webServer.messages.RegisterProfileRequest;
 import webServer.messages.RemoveOfferRequest;
 import webServer.messages.SetProfileRequest;
 import data.LoginCred;
 import data.Service;
 import data.Service.Status;
 import data.UserEntry;
 import data.UserProfile;
 import data.UserProfile.UserRole;
 
 public class WebServer {
 	static Connection	conn;
 
 	static {
 		System.out.println("[WebServer:static] Begin");
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		String url = "jdbc:mysql://" + WebServerConfig.DB_SERVER_ADDRESS + ":" + WebServerConfig.DB_SERVER_PORT + "/"
 				+ WebServerConfig.DB_NAME;
 
 		try {
 			conn = DriverManager.getConnection(url, WebServerConfig.DB_USER, WebServerConfig.DB_PASSWORD);
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
 
 		try {
 			Statement st1 = conn.createStatement();
 
 			String query = "SELECT * FROM users WHERE username = '" + cred.getUsername() + "' AND password = '"
 					+ cred.getPassword() + "'";
 			ResultSet rs1 = st1.executeQuery(query);
 
 			if (rs1.first()) {
 				Integer id = rs1.getInt("id");
 
 				String online_as_field = "as_buyer";
 				if (cred.getRole() == UserRole.SELLER) {
 					online_as_field = "as_seller";
 				}
 
 				Statement st2 = conn.createStatement();
 				query = "UPDATE users" + " SET " + online_as_field + " =  1 , address = '"
						+ cred.getAddress().getAddress().getCanonicalHostName() + "', port = " + cred.getAddress().getPort() + " WHERE id = "
 						+ id;
 				st2.executeUpdate(query);
 
 				UserProfile userProfile = new UserProfile();
 				userProfile.setUsername(rs1.getString("username"));
 				userProfile.setFirstName(rs1.getString("first_name"));
 				userProfile.setLastName(rs1.getString("last_name"));
 				userProfile.setPassword(rs1.getString("password"));
 				userProfile.setRole(cred.getRole());
 				userProfile.setLocation(rs1.getString("location"));
 
 				cred.setId(id);
 				
 				st1.close();
 				st2.close();
 				System.out.println("[WebServer:login] End (LoginResponse)");
 				return WebMessage.serialize(new LoginResponse(userProfile, cred));
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
 
 			query = "UPDATE services" + " SET active = 0 WHERE user_id = " + cred.getId() + " AND user_role = "
 					+ cred.getRole().ordinal();
 			System.out.println("[WebServer:logout] " + query);
 			st.executeUpdate(query);
 
 			st.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 
 			return WebMessage.serialize(new ErrorResponse("Something bad happend at server"));
 		}
 
 		System.out.println("[WebServer:logout] End");
 		return WebMessage.serialize(new OkResponse());
 	}
 	
 	public byte[] loadOffers(byte[] byteReq) {
 		System.out.println("[WebServer:loadOffers] Begin");
 		ArrayList<Service> services = new ArrayList<Service>();
 
 		Object obj = WebMessage.deserialize(byteReq);
 		if (!(obj instanceof LoadOffersRequest)) {
 			System.out.println("[WebServer:launchOffer] Wrong message... waiting LaunchOfferRequest");
 			return WebMessage.serialize(new LoadOffersResponse(services));
 		}
 		
 		LoadOffersRequest req = (LoadOffersRequest) obj;
 		LoginCred cred = req.getCred();
 		System.out.println("cred: " + cred);
 
 		try {
 			String query = "SELECT * FROM services WHERE user_id = ? AND user_role = ?";
 		
 			PreparedStatement ps = conn.prepareStatement(query);
 			ps.setInt(1, cred.getId());
 			ps.setInt(2, cred.getRole().ordinal());
 			ResultSet rs = ps.executeQuery();
 			
 			while (rs.next()) {
 				Service service = new Service(rs.getString("name"));
 
 				service.setTime(rs.getTimestamp("time").getTime());
 				service.setPrice(rs.getDouble("price"));
 
 				services.add(service);
 			}
 			
 			rs.close();
 			ps.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.println("[WebServer:loadOffers] End");
 		return WebMessage.serialize(new LoadOffersResponse(services));
 	}
 
 	public byte[] launchOffer(byte[] byteReq) {
 		System.out.println("[WebServer:launchOffers] Begin");
 
 		Object obj = WebMessage.deserialize(byteReq);
 		if (!(obj instanceof LaunchOfferRequest)) {
 			System.out.println("[WebServer:launchOffer] Wrong message... waiting LaunchOfferRequest");
 			return WebMessage.serialize(new ErrorResponse("Wrong message... waiting LaunchOfferRequest"));
 		}
 
 		LaunchOfferRequest req = (LaunchOfferRequest) obj;
 		LoginCred cred = req.getLoginCred();
 		Service service = req.getService();
 		
 		System.out.println("service: " + service);
 
 		try {
 			String query = "SELECT * FROM services WHERE user_id = ? AND name = ? AND user_role = ?";
 
 			PreparedStatement ps1 = conn.prepareStatement(query);
 			ps1.setInt(1, cred.getId());
 			ps1.setString(2, service.getName());
 			ps1.setInt(3, cred.getRole().ordinal());
 			ResultSet rs1 = ps1.executeQuery();
 			
 			Boolean found = rs1.first();
 			
 			rs1.close();
 			ps1.close();
 
 			if (found) {
 				System.out.println("[WebService:launchOffer] Service found");
 				
 				if (service.getStatus() != Status.NEW) {
 					System.out.println("[WebService:launchOffer] activating...");
 					query = "UPDATE services SET active = 1 WHERE user_id = ? AND name = ? AND user_role = ?";
 
 					PreparedStatement ps2 = conn.prepareStatement(query);
 					ps2.setInt(1, cred.getId());
 					ps2.setString(2, service.getName());
 					ps2.setInt(3, cred.getRole().ordinal());
 					ps2.executeUpdate();
 					ps2.close();
 				}
 				
 				query = "SELECT * FROM services s JOIN users u ON s.user_id = u.id WHERE s.name = ? AND s.user_role = ? AND s.active = ?";
 				
 				PreparedStatement ps2 = conn.prepareStatement(query);
 				ps2.setString(1, service.getName());
 				if (cred.getRole() == UserRole.BUYER) {
 					ps2.setInt(2, UserRole.SELLER.ordinal());
 				} else {
 					ps2.setInt(2, UserRole.BUYER.ordinal());
 				}
 				ps2.setInt(3, 1);
 				ResultSet rs2 = ps2.executeQuery();
 
 				ArrayList<UserEntry> sellers = new ArrayList<UserEntry>();
 				while (rs2.next()) {
 					UserEntry userEntry = new UserEntry();
 					userEntry.setUsername(rs2.getString("u.username"));
 					userEntry.setName(rs2.getString("u.first_name") + rs2.getString("u.last_name"));
 					InetSocketAddress address = new InetSocketAddress(rs2.getString("u.address"), rs2.getInt("u.port"));
 					userEntry.setAddress(address);
 
 					System.out.println("userEntry: " + userEntry);
 					sellers.add(userEntry);
 				}
 				rs2.close();
 				ps2.close();
 				service.setUsers(sellers);
 				System.out.println("service: " + service);
 			} else {
 				if (cred.getRole() == UserRole.SELLER) {
 					System.out.println("[WebServer:launchOffer] Inserting new service as seller ");
 					query = "INSERT INTO services(name, time, price, user_id, active, user_role) VALUES (?, ?, ?, ?, 1, ?)";
 					
 					PreparedStatement ps2 = conn.prepareStatement(query);
 					ps2.setString(1, service.getName());
 					ps2.setTimestamp(2, new Timestamp(service.getTime()));
 					ps2.setDouble(3, service.getPrice());
 					ps2.setInt(4, cred.getId());
 					ps2.setInt(5, UserRole.SELLER.ordinal());
 					ps2.executeUpdate();
 					ps2.close();
 					
 					query = "SELECT * FROM services s JOIN users u ON s.user_id = u.id WHERE s.name = ? AND s.user_role = ? AND s.active = 1";
 					ps2 = conn.prepareStatement(query);
 					ps2.setString(1, service.getName());
 					ps2.setInt(2, UserRole.BUYER.ordinal());
 					ResultSet rs2 = ps2.executeQuery();
 
 					ArrayList<UserEntry> buyers = new ArrayList<UserEntry>();
 					while (rs2.next()) {
 						UserEntry userEntry = new UserEntry();
 						userEntry.setUsername(rs2.getString("u.username"));
 						userEntry.setName(rs2.getString("u.first_name") + rs2.getString("u.last_name"));
 						InetSocketAddress address = new InetSocketAddress(rs2.getString("u.address"),
 								rs2.getInt("u.port"));
 						userEntry.setAddress(address);
 
 						buyers.add(userEntry);
 					}
 					rs2.close();
 					ps2.close();
 					service.setUsers(buyers);
 				} else {
 					System.out.println("[WebServer:launchOffer] Inserting new service as buyer");
 					query = "INSERT INTO services(name, time, price, user_id, active, user_role) VALUES (?, ?, ?, ?, 0, ?)";
 					
 					PreparedStatement ps2 = conn.prepareStatement(query);
 					ps2.setString(1, service.getName());
 					ps2.setTimestamp(2, new Timestamp(service.getTime()));
 					ps2.setDouble(3,  service.getPrice());
 					ps2.setInt(4, cred.getId());
 					ps2.setInt(5, cred.getRole().ordinal());
 					ps2.executeUpdate();
 					
 					ps2.close();
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
 		LoginCred cred = req.getLoginCred();
 
 		try {
 			Statement st = conn.createStatement();
 
 			String query = "UPDATE services SET active = 0 WHERE name = '" + req.getServiceName() + "' AND user_id = " + cred.getId()
 					+ " AND user_role = " + cred.getRole().ordinal();
 			st.executeUpdate(query);
 			st.close();
 
 			System.out.println("[WebServer: dropOffer] End");
 			return WebMessage.serialize(new OkResponse());
 		} catch (SQLException e) {
 			e.printStackTrace();
 
 			return WebMessage.serialize(new ErrorResponse("Something bad happend at server"));
 		}
 	}
 	
 	public byte[] removeOffer(byte[] byteReq) {
 		System.out.println("[WebServer:removeOffer] Begin");
 		
 		Object obj = WebMessage.deserialize(byteReq);
 		if (!(obj instanceof RemoveOfferRequest)) {
 			System.out.println("[WebServer:dropOffer] Wrong message... waiting removeOfferRequest");
 
 			return WebMessage.serialize(new ErrorResponse("Wrong message... waiting removeOfferRequest"));	
 		}
 		
 		RemoveOfferRequest req = (RemoveOfferRequest) obj;
 		LoginCred cred = req.getLoginCred();
 		
 		try {
 			String query = "DELETE FROM services WHERE name = ? AND user_id = ? AND user_role = ?";
 			
 			PreparedStatement  ps = conn.prepareStatement(query);
 			ps.setString(1, req.getServiceName());
 			ps.setInt(2, cred.getId());
 			ps.setInt(3, cred.getRole().ordinal());
 			ps.executeUpdate();
 			ps.close();
 			
 			System.out.println("[WebServer:removeOffer] End");
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
 			String query = "SELECT * FROM users WHERE username = '" + req.getUsername() +"'";
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
 			String query = "UPDATE users SET first_name = '" + profile.getFirstName() + "', last_name = '"
 					+ profile.getLastName() + "', password = '" + profile.getPassword() + "', location = '"
 					+ profile.getLocation() + "' WHERE username = '" + profile.getUsername() + "'";
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
