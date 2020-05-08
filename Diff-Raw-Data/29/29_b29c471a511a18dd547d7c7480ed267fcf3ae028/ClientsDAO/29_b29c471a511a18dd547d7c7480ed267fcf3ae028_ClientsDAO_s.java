 package cyberprime.entities.dao;
 
 
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import cyberprime.entities.Clients;
 import cyberprime.util.EmailSender;
 
 
 
 
 
 public class ClientsDAO {
 	private static DBController db = new DBController();
 	
 	public static Clients registerClient(Clients clients) {
 		Connection currentCon = db.getConnection();
 		
 		try{
 			String query = "insert into registration(imageHash,imageSize,imageExtension,userId,email,pattern,token,activation) " +
 					"values(?,?,?,?,?,?,?,?)";
 			PreparedStatement pstmt = currentCon.prepareStatement(query);
 			// inserting values
 			pstmt.setString(1, clients.getImageHash());
 			pstmt.setInt(2, clients.getImageSize());
 			pstmt.setString(3, clients.getImageExtension());
 			pstmt.setString(4, clients.getUserId());
 			pstmt.setString(5, clients.getEmail());
 			pstmt.setString(6, clients.getPattern());
 			pstmt.setString(7,clients.getToken());
 			pstmt.setString(8, clients.getActivation());
 			pstmt.executeUpdate();
 		
 	}catch (Exception ex) {
 
 			System.out.println("Registration failed: An Exception has occurred! "+ ex);
 			clients = null;
 			
 		}
 		finally {
 
 			if (currentCon != null) {
 				try {
 					currentCon.close();
 				} catch (Exception e) {
 				}
 
 				currentCon = null;
 			}
 		}
 		return clients;
 	}
 	
 	public static Clients retrieveClient(Clients clients){
 		Connection currentCon = db.getConnection();
 		Clients client = new Clients();
 		ResultSet rs = null;
 		
 		try{
 			String query="select * from registration where imageHash = ?;";
 			PreparedStatement pstmt = currentCon.prepareStatement(query);
 			pstmt.setString(1,clients.getImageHash());
 			rs = pstmt.executeQuery();
 			
 			while(rs.next()){
 				client.setImageHash(rs.getString(1));
 				client.setImageSize(rs.getInt(2));
 				client.setImageExtension(rs.getString(3));
 				client.setUserId(rs.getString(4));
 				client.setEmail(rs.getString(5));
 				client.setDBPattern(rs.getString(6));
 				client.setToken(rs.getString(7));
 				client.setActivation(rs.getString(8));
 			}
 
 		}catch(Exception ex){
 			ex.printStackTrace();
 			client = null;
 		}finally {
 
 			if (currentCon != null) {
 				try {
 					currentCon.close();
 				} catch (Exception e) {
 				}
 
 				currentCon = null;
 			}
 		}
 		return client;
 	}
 	
 	public static boolean checkUser(Clients client){
 		boolean check = false;
 		Connection currentCon = db.getConnection();
 		ResultSet rs = null;
 		Clients c = new Clients();
 		try{
 			String query = "select userId from checkUser where userId = ?;";
 			PreparedStatement pstmt = currentCon.prepareStatement(query);
 			pstmt.setString(1, client.getUserId());
 			rs = pstmt.executeQuery();
 			
 			while(rs.next()){
 				c.setUserId(rs.getString(1));
 			}
 		}catch(Exception ex){
 			ex.printStackTrace();
 		}finally {
 
 			if (currentCon != null) {
 				try {
 					currentCon.close();
 				} catch (Exception e) {
 				}
 
 				currentCon = null;
 			}
 		}
 		
 		if(c.getUserId()!=null){
 			check = true;
 		}
 		
 		else{
 			check = false;
 		}
 		
 		return check;
 		
 	}
 	
 	public static boolean checkEmail(Clients client){
 		boolean check = false;
 		Connection currentCon = db.getConnection();
 		ResultSet rs = null;
 		Clients c = new Clients();
 		try{
 			String query = "select email from checkUser where email = ?;";
 			PreparedStatement pstmt = currentCon.prepareStatement(query);
 			pstmt.setString(1, client.getEmail());
 			rs = pstmt.executeQuery();
 			
 			while(rs.next()){
 				c.setEmail(rs.getString(1));
 			}
 		}catch(Exception ex){
 			ex.printStackTrace();
 		}finally {
 
 			if (currentCon != null) {
 				try {
 					currentCon.close();
 				} catch (Exception e) {
 				}
 
 				currentCon = null;
 			}
 		}
 		if(c.getEmail()!=null){
 			check = true;
 		}
 		
 		else{
 			check = false;
 		}
 		
 		return check;
 		
 	}
 	
 	public static Clients updateClients(Clients clients){
 		Connection currentCon = db.getConnection();
 		
 		try{
 			String query = "update updateClients set imageHash = ?, imageSize =?, imageExtension, email=?, pattern = ? where userId= ?";
 			PreparedStatement pstmt = currentCon.prepareStatement(query);
 			pstmt.setString(1, clients.getImageHash());
 			pstmt.setInt(2, clients.getImageSize());
 			pstmt.setString(3,clients.getImageExtension());
 			pstmt.setString(4, clients.getEmail());
 			pstmt.setString(5,clients.getPattern());
 			pstmt.setString(6, clients.getUserId());
 			pstmt.executeUpdate();
 		}catch(Exception ex){
 			System.out.println("Update failed: An Exception has occurred! "+ ex);
 			clients = null;
 		}		
 		finally {
 
 			if (currentCon != null) {
 				try {
 					currentCon.close();
 				} catch (Exception e) {
 				}
 
 				currentCon = null;
 			}
 		}
 		return clients;
 		
 		
 	}
 
 	public static Clients activateClients(Clients clients){
 		Connection currentCon = db.getConnection();
 		
 		try{
 			String query = "update activation set activation = ?, token = ? where userId= ?";
 			PreparedStatement pstmt = currentCon.prepareStatement(query);
 			pstmt.setString(1, clients.getActivation());
 			pstmt.setString(2, "null");
 			pstmt.setString(3, clients.getUserId());
 			pstmt.executeUpdate();
 		}catch(Exception ex){
 			System.out.println("Update failed: An Exception has occurred! "+ ex);
 			clients = null;
 		}		
 		finally {
 
 			if (currentCon != null) {
 				try {
 					currentCon.close();
 				} catch (Exception e) {
 				}
 
 				currentCon = null;
 			}
 		}
 		return clients;
 		
 		
 	}
 	
 	public static String getActivation(Clients clients){
 		Connection currentCon = db.getConnection();
 		ResultSet rs = null;
 		String activation = "";
 		try{
 			String query = "select activation from activation where userId= ?";
 			PreparedStatement pstmt = currentCon.prepareStatement(query);
 			pstmt.setString(1, clients.getUserId());
 			rs = pstmt.executeQuery();
 			
 			while(rs.next()){
				activation = rs.getString("1");
 			}
 		}catch(Exception ex){
			System.out.println("Update failed: An Exception has occurred! "+ ex);
 			clients = null;
 		}		
 		finally {
 
 			if (currentCon != null) {
 				try {
 					currentCon.close();
 				} catch (Exception e) {
 				}
 
 				currentCon = null;
 			}
 		}
 		return activation;
 		
 	}
 	
 	public static String changePattern(Clients clients){
 		Connection currentCon = db.getConnection();
 		ResultSet rs = null;
 		String pattern = "";
 		try{
 			String selectQuery = "select pattern from loginInfo where userId= ?";
 			PreparedStatement pstmt1 = currentCon.prepareStatement(selectQuery);
 			pstmt1.setString(1, clients.getUserId());
 			rs = pstmt1.executeQuery();
 			while(rs.next()){
 				pattern = rs.getString(1);
 			}
 			
 			String query = "update loginInfo set pattern = ? where userId= ?";
 			PreparedStatement pstmt = currentCon.prepareStatement(query);
 			pstmt.setString(1, clients.getPattern());
 			pstmt.setString(2, clients.getUserId());
 			pstmt.executeUpdate();
 		}catch(Exception ex){
 			System.out.println("Update failed: An Exception has occurred! "+ ex);
 		}		
 		finally {
 
 			if (currentCon != null) {
 				try {
 					currentCon.close();
 				} catch (Exception e) {
 				}
 
 				currentCon = null;
 			}
 		}
 		
 		return pattern;
 	}
 	
 	public static Clients changeImage(Clients clients){
 		Connection currentCon = db.getConnection();
 		Clients c = new Clients();
 		try{
 			String query = "update loginInfo set imageHash = ? where userId= ?";
 			PreparedStatement pstmt = currentCon.prepareStatement(query);
 			pstmt.setString(1, clients.getImageHash());
 			pstmt.setString(2, clients.getUserId());
 			pstmt.executeUpdate();
 		}catch(Exception ex){
 			System.out.println("Update failed: An Exception has occurred! "+ ex);
 			c = null;
 		}		
 		finally {
 
 			if (currentCon != null) {
 				try {
 					currentCon.close();
 				} catch (Exception e) {
 				}
 
 				currentCon = null;
 			}
 		}
 		
 		return c;
 	}
 	
 	public static String retrieveToken(Clients clients){
 		Connection currentCon = db.getConnection();
 		ResultSet rs = null;
 		Clients c = new Clients();
 		c.setUserId(clients.getUserId());
 				try{
 					String query = "select token from Client where userId = ?";
 					PreparedStatement pstmt = currentCon.prepareStatement(query);
 					pstmt.setString(1, clients.getUserId());
 					rs = pstmt.executeQuery();
 					
 					while(rs.next()){
 						c.setToken(rs.getString(1));
 					}
 				}catch(Exception ex){
 					System.out.println("Update failed: An Exception has occurred! "+ ex);
 					clients = null;
 				}		
 				finally {
 		
 					if (currentCon != null) {
 						try {
 							currentCon.close();
 						} catch (Exception e) {
 						}
 		
 						currentCon = null;
 					}
 				}
 				return c.getToken();
 	}
 	
 	public static void main(String args[]) throws NoSuchAlgorithmException, UnsupportedEncodingException{
 
		Clients client = new Clients("57g4hab18bbi9926493e",null,102,".jpg","deanelooiz@hotmail.com","pattern");
		client.setActivation("Pending");
		client.setToken();
		//Clients c = ClientsDAO.registerClient(client);
		client.setPattern("00");
		String oldPattern = ClientsDAO.changePattern(client);
		System.out.println("Old pattern = "+oldPattern);
		System.out.println("New Pattern = "+client.getPattern());
 		//System.out.println(client.getToken());
 //		EmailSender email = new EmailSender(client);
 //		email.sendActivationLink(client.getToken());
 //		Clients client = new Clients();
 //		client.setUserId("userid");
 //		String tokenc = ClientsDAO.retrieveToken(client);
 //		System.out.println(tokenc);
 //		
 //		client.setActivation("Active");
 //		ClientsDAO.activateClients(client);
 		
 		
 //		Clients c = ClientsDAO.retrieveClient(client);
 //		
 //		if(client.getToken().equals(c.getToken())){
 //			client.setActivation("Active");
 //			ClientsDAO.activateClients(client);
 //		}
 //		
 //		Clients c = ClientsDAO.retrieveClient(client);
 //		if(c.getActivation().equalsIgnoreCase("Active")){
 //			System.out.println("Your account has been activated");
 //			
 //			if(client.getImageHash().equals(c.getImageHash()) && client.getPattern().equals(c.getPattern())){
 //				System.out.println("You have been logged in");
 //			}
 //		}
 //		
 //		else{
 //			System.out.println("Please activate your account");
 //		}
 		
 		
 	}
 }
