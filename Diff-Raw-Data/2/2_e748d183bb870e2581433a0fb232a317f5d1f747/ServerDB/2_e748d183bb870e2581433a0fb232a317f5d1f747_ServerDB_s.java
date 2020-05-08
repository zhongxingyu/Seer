 package edu.colorado.csci3308.inventory;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 
 public class ServerDB {
 	
 	private static String path = "";
 	
 	public static void setPath(String path) {
 		ServerDB.path = path;
 	}
 	
 	public static ServerList getAllServers() {
 		ServerList servers = new ServerList();
 		ResultSet result = query("SELECT server_id, hostname, ip_addr, mac_addr, total_memory, " +
 				"top_height, location_id, rack_id, processor_1_id, processor_2_id, s.mb_id, " +
 				"manufacturer, model, mb_url,  s.model_id, height, model_name, max_data_drives, " +
 				"mfg_name, " +
 				"p1.processor_name AS p1_processor_name, p1.processor_speed AS p1_processor_speed, " +
 				"p1.num_cores AS p1_num_cores, " +
 				"p2.processor_name AS p2_processor_name, p2.processor_speed AS p2_processor_speed, " +
 				"p2.num_cores AS p2_num_cores " +
 				"FROM servers s " +
 				"JOIN motherboards mb ON s.mb_id = mb.mb_id  " +
 				"JOIN chassis_models cm ON cm.model_id = s.model_id  " +
 				"JOIN chassis_manufacturers cman ON cman.mfg_id = cm.mfg_id " +
 				"JOIN processors p1 ON s.processor_1_id = p1.processor_id " +
 				"JOIN processors p2 ON s.processor_2_id = p2.processor_id ");
 		
 		try {
 			while (result.next()) {
 				Integer serverId = result.getInt("server_id");
 				String hostname = result.getString("hostname");
 				String ipAddress = result.getString("ip_addr");
 				String macAddress = result.getString("mac_addr");
 				Integer totalMemory = result.getInt("total_memory");
 				Integer topHeight = result.getInt("top_height");
 				Integer locationId = result.getInt("location_id");
 				Integer rackId = result.getInt("rack_id");
 				
 				// Motherboard data
 				Integer mbId = result.getInt("mb_id");
 				String mbManufacturer = result.getString("manufacturer");
 				String mbModel = result.getString("model");
 				String mbUrl = result.getString("mb_url");
 				
 				// Model data
 				Integer modelId = result.getInt("model_id");
 				String modelName = result.getString("model_name");
 				Integer height = result.getInt("height");
 				Integer maxDataDrives = result.getInt("max_data_drives");
 				String mfgName = result.getString("mfg_name");
 				
 				// Processor 1 data
 				Integer processor1Id = result.getInt("processor_1_id");
 				String processor1Name = result.getString("p1_processor_name");
 				Double processor1Speed = result.getDouble("p1_processor_speed");
 				Integer processor1NumCores = result.getInt("p1_num_cores");
 				
 				// Processor 2 data
 				Integer processor2Id = result.getInt("processor_2_id");
 				String processor2Name = result.getString("p2_processor_name");
 				Double processor2Speed = result.getDouble("p2_processor_speed");
 				Integer processor2NumCores = result.getInt("p2_num_cores");
 				
 				
 				Motherboard motherboard = new Motherboard(mbId, mbManufacturer, mbModel, mbUrl);
 				ChassisModel model = new ChassisModel(modelId, modelName, height, 
 						maxDataDrives, mfgName);
 				
 				Processor processor1 = new Processor(processor1Id, processor1Name, 
 						processor1Speed, processor1NumCores);
 				Processor processor2 = new Processor(processor2Id, processor2Name, 
 						processor2Speed, processor2NumCores);
 				
 				// TODO get cards from database
 				
 				servers.add(new Server(serverId, 
 									   hostname, 
 									   ipAddress, 
 									   macAddress, 
 									   totalMemory,
 									   topHeight,
 									   locationId,
 									   rackId,
 									   model,
 									   motherboard,
 									   processor1,
 									   processor2));
 			}
 			result.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return servers;
 	}
 	
 	public static int getServerCount(){
 		 ResultSet result = query("SELECT COUNT(*) AS count FROM servers");
 		 int count;
 		try {
 			count = result.getInt("count");
 			return count;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return 0;
 		}
 		
 	}
 	
 	public static ProcessorList getAllProcessors() {
 		ProcessorList processors = new ProcessorList();
 		ResultSet result = query("SELECT * FROM processors");
 		
 		try {
 			while (result.next()) {
 				Processor processor = new Processor(result.getInt(1), 
 													result.getString(2), 
 													result.getDouble(3), 
 													result.getInt(4));
 				processors.add(processor);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return processors;
 	}
 	
 	public static User getUserById(Integer id){
 		User usr = null;
 		
 		ResultSet result = query("SELECT * FROM users WHERE user_id = " + id);
 		
 		try {
 			String username = result.getString("user_name");
 			
 			usr = new User(id, username);
 			result.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return usr;
 	}
 	
 	public static User getUserByName(String name){
 		User usr = null;
 		String query = "SELECT * FROM users WHERE user_name = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setString(1, name);
 			ResultSet result = pstmt.executeQuery();
 			if (result.next()) {
 				Integer userId = result.getInt(1);
 				String userName = result.getString(2);
 				usr = new User(userId, userName);
 			}
 			result.close();
 			pstmt.close();
 		} catch (SQLException e) {
 			System.out.println("Error in getUserByName");
 			e.printStackTrace();
 		}
 		return usr;
 	}
 	
 	public static void addUser(User user){
 		Integer userId = user.getUserId();
 		String userName = user.getUserName();
 		
 		String query = "INSERT INTO users (user_id, user_name) VALUES (?, ?)";
 		
 		PreparedStatement pstmt = getPreparedStatement(query);
 		
 		try {
 			pstmt.setInt(1, userId);
 			pstmt.setString(2, userName);
 			
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e){
 			e.printStackTrace();
 		}
 	}
 		
 	public static Server getServerById(Integer serverId) {
 		Server server = null;
 		
 		ResultSet result = query("SELECT server_id, hostname, ip_addr, mac_addr, total_memory, " +
 				"top_height, location_id, rack_id, processor_1_id, processor_2_id, s.mb_id, " +
 				"manufacturer, model, mb_url,  s.model_id, height, model_name, max_data_drives, " +
 				"mfg_name, " +
 				"p1.processor_name AS p1_processor_name, p1.processor_speed AS p1_processor_speed, " +
 				"p1.num_cores AS p1_num_cores, " +
 				"p2.processor_name AS p2_processor_name, p2.processor_speed AS p2_processor_speed, " +
 				"p2.num_cores AS p2_num_cores " +
 				"FROM servers s " +
 				"JOIN motherboards mb ON s.mb_id = mb.mb_id  " +
 				"JOIN chassis_models cm ON cm.model_id = s.model_id  " +
 				"JOIN chassis_manufacturers cman ON cman.mfg_id = cm.mfg_id " +
 				"JOIN processors p1 ON s.processor_1_id = p1.processor_id " +
 				"JOIN processors p2 ON s.processor_2_id = p2.processor_id " +
 				"WHERE server_id = " + serverId);
 		try {
 			String hostname = result.getString("hostname");
 			String ipAddress = result.getString("ip_addr");
 			String macAddress = result.getString("mac_addr");
 			Integer totalMemory = result.getInt("total_memory");
 			Integer topHeight = result.getInt("top_height");
 			Integer locationId = result.getInt("location_id");
 			Integer rackId = result.getInt("rack_id");
 			
 			// Motherboard data
 			Integer mbId = result.getInt("mb_id");
 			String mbManufacturer = result.getString("manufacturer");
 			String mbModel = result.getString("model");
 			String mbUrl = result.getString("mb_url");
 			
 			// Model data
 			Integer modelId = result.getInt("model_id");
 			String modelName = result.getString("model_name");
 			Integer height = result.getInt("height");
 			Integer maxDataDrives = result.getInt("max_data_drives");
 			String mfgName = result.getString("mfg_name");
 			
 			// Processor 1 data
 			Integer processor1Id = result.getInt("processor_1_id");
 			String processor1Name = result.getString("p1_processor_name");
 			Double processor1Speed = result.getDouble("p1_processor_speed");
 			Integer processor1NumCores = result.getInt("p1_num_cores");
 			
 			// Processor 2 data
 			Integer processor2Id = result.getInt("processor_2_id");
 			String processor2Name = result.getString("p2_processor_name");
 			Double processor2Speed = result.getDouble("p2_processor_speed");
 			Integer processor2NumCores = result.getInt("p2_num_cores");
 			
 			
 			Motherboard motherboard = new Motherboard(mbId, mbManufacturer, mbModel, mbUrl);
 			ChassisModel model = new ChassisModel(modelId, modelName, height, 
 					maxDataDrives, mfgName);
 			
 			Processor processor1 = new Processor(processor1Id, processor1Name, 
 					processor1Speed, processor1NumCores);
 			Processor processor2 = new Processor(processor2Id, processor2Name, 
 					processor2Speed, processor2NumCores);
 			
 			// TODO get cards from database
 			
 			server = new Server(serverId, 
 								hostname, 
 								ipAddress, 
 							    macAddress, 
 						 	    totalMemory,
 								topHeight,
 								locationId,
 								rackId,
 								model,
 								motherboard,
 								processor1,
 								processor2);
 			result.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return server;
 	}
 	
 	public static Motherboard getMotherboardById(Integer motherboardId) {
 		Motherboard motherboard = null;
 		ResultSet result = query("SELECT * FROM motherboards WHERE mb_id = " + motherboardId);
 
 		try {
 			motherboard = new Motherboard(result.getInt("mb_id"), 
 										  result.getString("manufacturer"), 
 										  result.getString("model"), 
 										  result.getString("mb_url"));
 			result.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return motherboard;
 	}
 	
 	public static Processor getProcessorById(Integer processorId) {
 		Processor processor = null;
 		ResultSet result = query("SELECT * FROM processors WHERE processor_id = " + processorId);
 		
 		try {
 			processor = new Processor(result.getInt("processor_id"), 
 									  result.getString("processor_name"), 
 									  result.getDouble("processor_speed"), 
 									  result.getInt("num_cores"));
 			result.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return processor;
 	}
 	
 	public static ChassisModel getChassisModelById(Integer modelId) {
 		ChassisModel model = null;
 		ResultSet result = query("SELECT * FROM chassis_models cmod " +
 								 "JOIN chassis_manufacturers cman " +
 								 "ON cmod.mfg_id = cman.mfg_id " +
 								 "WHERE cmod.model_id = " + modelId);
 		
 		try {
 			model = new ChassisModel(result.getInt(1), 
 									 result.getString(2), 
 									 result.getInt(3), 
 									 result.getInt(4), 
 									 result.getString(7));
 			result.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return model;
 	}
 	
 	/***
 	 * Adds a server to the database given a Server object
 	 * 
 	 * The server object only needs the following fields:
 	 *    hostname
 	 *    ip address
 	 *    mac address
 	 *    total memory
 	 *    
 	 * The rest, if null, are given a default value or inserted as null
 	 */
 	public static void addServer(Server server) {
 		String hostname = server.getHostname();
 		String ipAddress = server.getIpAddress();
 		String macAddress = server.getMacAddress();
 		Integer totalMemory = server.getTotalMemory();
 		Integer topHeight = server.getTopHeight();
 		Integer locationId = server.getLocationId();
 		Integer rackId = server.getRackId();
 		Integer motherboardId = server.getMotherboard() == null ? 1 : server.getMotherboard().getId(); //
 		Integer modelId = server.getChassisModel() == null ? 1 : server.getChassisModel().getId(); //
 		Integer processor1Id = server.getProcessor1() == null ? 1 : server.getProcessor1().getId(); //
 		Integer processor2Id = server.getProcessor2() == null ? 1 : server.getProcessor2().getId(); //
 		
 		String query = "INSERT INTO servers " +
 				"(hostname, ip_addr, mac_addr, total_memory, top_height, location_id, " +
 				"rack_id, mb_id, model_id, processor_1_id, processor_2_id," +
 				"card_1_id, card_2_id, card_3_id, card_4_id, card_5_id)" +
 				"VALUES " +
 				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 		
 		PreparedStatement pstmt = getPreparedStatement(query);
 		
 		try {
 			pstmt.setString(1, hostname);
 			pstmt.setString(2, ipAddress);
 			pstmt.setString(3, macAddress);
 			pstmt.setInt(4, totalMemory);
 			
 			if (topHeight == null) { pstmt.setNull(5, Types.INTEGER); }
 			else { pstmt.setInt(5, topHeight); }
 			
 			if (locationId == null) { pstmt.setInt(6, 1); }
 			else { pstmt.setInt(6, locationId); }
 			
 			if (rackId == null) { pstmt.setNull(7, Types.INTEGER); }
 			else { pstmt.setInt(7, rackId); }
 			
 			pstmt.setInt(8, motherboardId);
 			pstmt.setInt(9, modelId);
 			pstmt.setInt(10, processor1Id);
 			pstmt.setInt(11, processor2Id);
 			pstmt.setNull(12, Types.INTEGER);
 			pstmt.setNull(13, Types.INTEGER);
 			pstmt.setNull(14, Types.INTEGER);
 			pstmt.setNull(15, Types.INTEGER);
 			pstmt.setNull(16, Types.INTEGER);
 			
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/***
 	 * Updates a server to the database given a Server object
 	 * 
 	 * The server object only needs the following fields:
 	 *    hostname
 	 *    ip address
 	 *    mac address
 	 *    total memory
 	 *    
 	 * The rest, if null, are given a default value or inserted as null
 	 */
 	public static void updateServer(Server server) {
 		Integer id = server.getServerId();
 		String hostname = server.getHostname();
 		String ipAddress = server.getIpAddress();
 		String macAddress = server.getMacAddress();
 		Integer totalMemory = server.getTotalMemory();
 		Integer topHeight = server.getTopHeight();
 		Integer locationId = server.getLocationId();
 		Integer rackId = server.getRackId();
 		Integer motherboardId = server.getMotherboard() == null ? 1 : server.getMotherboard().getId(); //
 		Integer modelId = server.getChassisModel() == null ? 1 : server.getChassisModel().getId(); //
 		Integer processor1Id = server.getProcessor1() == null ? 1 : server.getProcessor1().getId(); //
 		Integer processor2Id = server.getProcessor2() == null ? 1 : server.getProcessor2().getId(); //
 		
 		String query = "UPDATE servers SET hostname = ?, ip_addr = ?, mac_addr = ?, total_memory = ?, top_height = ?, location_id = ?, " +
 				"rack_id = ?, mb_id = ?, model_id = ?, processor_1_id = ?, processor_2_id = ?," +
 				"card_1_id = ?, card_2_id = ?, card_3_id = ?, card_4_id = ?, card_5_id = ?" +
 				"WHERE server_id = ?";
 		
 		PreparedStatement pstmt = getPreparedStatement(query);
 		
 		try {
 			pstmt.setString(1, hostname);
 			pstmt.setString(2, ipAddress);
 			if (macAddress == null) {pstmt.setNull(3, Types.INTEGER);}
 			else { pstmt.setString(3, macAddress);}
 			if (totalMemory == null) {pstmt.setNull(4, Types.INTEGER);}
 			else { pstmt.setInt(4, totalMemory);}
 			
 			if (topHeight == null) { pstmt.setNull(5, Types.INTEGER); }
 			else { pstmt.setInt(5, topHeight); }
 			
 			if (locationId == null) { pstmt.setInt(6, 1); }
 			else { pstmt.setInt(6, locationId); }
 			
 			if (rackId == null) { pstmt.setNull(7, Types.INTEGER); }
 			else { pstmt.setInt(7, rackId); }
 			
 			pstmt.setInt(8, motherboardId);
 			pstmt.setInt(9, modelId);
 			pstmt.setInt(10, processor1Id);
 			pstmt.setInt(11, processor2Id);
 			pstmt.setNull(12, Types.INTEGER);
 			pstmt.setNull(13, Types.INTEGER);
 			pstmt.setNull(14, Types.INTEGER);
 			pstmt.setNull(15, Types.INTEGER);
 			pstmt.setNull(16, Types.INTEGER);
 			pstmt.setInt(17, id);
 			
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void addProcessor(Processor processor) {
 		String query = "INSERT INTO processors " +
 				"(processor_name, processor_speed, num_cores) " +
 				"VALUES (?, ?, ?)";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setString(1, processor.getName());
 			pstmt.setDouble(2, processor.getFrequency());
 			pstmt.setInt(3, processor.getNumberOfCores());
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void deleteServer(Integer serverId) {
 		String query = "DELETE FROM servers WHERE server_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, serverId);
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void deleteAll() throws ClassNotFoundException, SQLException{
 		query("drop table if exists servers");
 	}
 	
 	public static void deleteProcessor(Integer processorId) {
 		String query = "DELETE FROM processors WHERE processor_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, processorId);
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static Reservation getReservationByReservationId(Integer reservationId) {
 		Reservation res = null;
 		String query = "SELECT * FROM reservations WHERE reservation_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, reservationId);
 			ResultSet result = pstmt.executeQuery();
 			
 			if (result.next()) {
 				Integer userId = result.getInt(2);
 				Integer serverId = result.getInt(3);
 				Long startDate = result.getLong(4);
 				Long endDate = result.getLong(5);
 				res = new Reservation(reservationId, userId, serverId, startDate, endDate);
 			}
 
 			result.close();
 			pstmt.close();
 		} catch (SQLException e) {
 			System.out.println("Error in getReservationByReservationId");
 			e.printStackTrace();
 		}
 		return res;
 	}
 	
 	public static Reservation getReservationByServerId(Integer serverId) {
 		Reservation res = null;
 		String query = "SELECT * FROM reservations WHERE server_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, serverId);
 			ResultSet result = pstmt.executeQuery();
 			
 			if (result.next()) {
 				Integer reservationId = result.getInt(1);
 				Integer userId = result.getInt(2);
 				Long startDate = result.getLong(4);
 				Long endDate = result.getLong(5);
 				res = new Reservation(reservationId, userId, serverId, startDate, endDate);
 			}
 
 			result.close();
 			pstmt.close();
 		} catch (SQLException e) {
 			System.out.println("Error in getReservationByReservationId");
 			e.printStackTrace();
 		}
 		return res;
 	}
 	
 	public static void addReservation(Reservation reservation) {
 		String query = "INSERT INTO reservations " +
 				"(user_id, server_id, start_date, end_date)" +
 				"VALUES (?, ?, ?, ?)";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, reservation.getUserId());
 			pstmt.setInt(2, reservation.getServerId());
 			pstmt.setLong(3, reservation.getStartDate().getTimeInMillis());
 			pstmt.setLong(4, reservation.getEndDate().getTimeInMillis());
 			pstmt.executeUpdate();
 			pstmt.close();
 			
 		} catch (SQLException e) {
 			System.out.println("Error in addReservation");
 			e.printStackTrace();
 		}
 	}
 	
 	public static void deleteReservation(Integer reservationId) {
 		String query = "DELETE FROM reservations WHERE reservation_id = ?";
 		PreparedStatement pstmt = getPreparedStatement(query);
 		try {
 			pstmt.setInt(1, reservationId);
 			pstmt.executeUpdate();
 			pstmt.close();
 		} catch (SQLException e) {
 			System.out.println("Error in deleteReservation");
 			e.printStackTrace();
 		}
 	}
 	
 	private static ResultSet query(String query) {
 		ResultSet result = null;
 		
 		try {
 			Class.forName("org.sqlite.JDBC");
 
 			Connection con = DriverManager.getConnection("jdbc:sqlite:" + path);
 
 			Statement stmt = con.createStatement();
 			result = stmt.executeQuery(query);
 			
 		} catch (ClassNotFoundException e) {
 			System.out.println(e);
 		} catch (SQLException e) {
 			System.out.println(e);
 		}
 		
 		return result;
 	}
 	
 	private static PreparedStatement getPreparedStatement(String string) {
 		PreparedStatement pstmt = null;
 		
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Connection con = DriverManager.getConnection("jdbc:sqlite:" + path);
 			
 			pstmt = con.prepareStatement(string);
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		return pstmt;
 	}
 	//TODO - fix path here
 	public static void main(String[] args) {
 		setPath("/home/ben/git/3308/Inventory/WebContent/WEB-INF/db/servers.db");
 	}   
 
 }
