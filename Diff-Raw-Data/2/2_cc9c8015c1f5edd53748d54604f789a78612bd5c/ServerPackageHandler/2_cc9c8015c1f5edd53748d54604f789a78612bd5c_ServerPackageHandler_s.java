 package net;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import javax.swing.JOptionPane;
 
 import model.Event;
 
 import datapackage.*;
 import encryption.PasswordEncryption;
 
 /*
  * Takes a DataPackage as argument, and returns the Package that should be sent to the client.
  **/
 
 public class ServerPackageHandler {
 	//DataPackage pack;
 	DBConnection connection;
 	
 	public ArrayList<DataPackage> HandlePackage(DataPackage pack){
 		ArrayList<DataPackage> returnPackages = new ArrayList<DataPackage>();
 		System.out.println("Debugpoint #1");
 		if (pack instanceof LoginPackage){
 			System.out.println("Debugpoint #2");
 			try {
 				if (HandleLoginPackage(pack)){
 					System.out.println("Debugpoint #3");
 					ErrorPackage errorPack = new ErrorPackage(ErrorType.OK,"All is well, user may pass",1,1);
 					returnPackages.add(errorPack);
 				}
 				else{
 					System.out.println("Debugpoint #4");
 					ErrorPackage errorPack = new ErrorPackage(ErrorType.WRONG_PASSWORD,"The user SHALL NOT PASS", 1, 1);
 					returnPackages.add(errorPack);
 				}
 			} catch (IOException e) {
 				System.out.println("Debugpoint #5");
 				System.out.println("Good God, it crashed!");
 				e.printStackTrace();
 			}
 		}
 		else if(pack instanceof CalendarRequestPackage){
 			System.out.println("CalReqPack Received");
 			try {
				HandleCalendarRequestPackage(pack);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return returnPackages;
 	}
 
 	private DBMethods ConnectToDB() throws IOException{
 				//load properties
 				System.out.println("Debugpoint #6");
 				Properties prop = new Properties();
 		        InputStream in = PackageSender.class.getResourceAsStream("Properties.properties");
 		        prop.load(in);
 		        //connect to DB
 				connection = new DBConnection(prop);
 				connection.connect();
 				Connection c = connection.getConnection();
 				Statement s = connection.getStatement();
 				DBMethods method = new DBMethods();
 				method.setConnection(c);
 				method.setStatement(s);
 				
 				return method;
 	}
 
 	private void DisconnectFromDB() throws SQLException{
 		connection.close();
 	}
 
 	private boolean HandleLoginPackage(DataPackage pack) throws IOException {
 		DBMethods method = ConnectToDB();
 		//store pack as LoginPackage, must be checked before method is called
 		LoginPackage loginpack = (LoginPackage)pack;
 		byte[] hash = null;
 		byte[] salt = null;
 		//exec logincheck
 		String username = loginpack.getUsername();
 		//Check to see if user exists
 		try {
 			if (method.isExcistingUser(username)){
 				try{
 					hash = method.getStoredHash(loginpack.getUsername(), "password");
 					salt = method.getStoredHash(loginpack.getUsername(), "salt");
 				}
 				catch (SQLException e){
 					System.out.println("Something went horribly wrong in the DB");
 					e.printStackTrace();
 					return false;
 				}
 			}
 			else{
 				return false;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 		try {
 			DisconnectFromDB();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if (PasswordEncryption.checkPassword(loginpack.getPassword(), salt, hash)){
 			return true;
 		}
 		else{
 			return false;
 		}
 		
 	}
 	
 	private ArrayList<DataPackage> HandleCalendarRequestPackage(DataPackage pack) throws IOException, SQLException {
 		CalendarRequestPackage CalReq = (CalendarRequestPackage)pack;
 		String name = CalReq.getName();
 		Integer group = CalReq.getGroup();
 		ArrayList<Event> events = new ArrayList<Event>();
 		ArrayList<DataPackage> responseList = new ArrayList<DataPackage>();
 		if ((CalReq.getName() != null) && (CalReq.getGroup() == null)){
 			DBMethods method = ConnectToDB();
 			events = method.loadEvents(name);
 			DisconnectFromDB();
 			
 			responseList = ConstructPackageListFromEvents(events);
 			return responseList;
 		}
 		else if ((CalReq.getGroup() != null) && (CalReq.getName() == null)){
 			DBMethods method = ConnectToDB();
 			//get groups cal
 			DisconnectFromDB();
 			return null;
 		}
 		else{
 			JOptionPane.showMessageDialog(null, "Malformated CalReqPackage");
 			return null;
 		}
 	}
 	
 	private ArrayList<DataPackage> ConstructPackageListFromEvents(ArrayList<Event> events){
 		ArrayList<DataPackage> returnList = new ArrayList<DataPackage>();
 		Event event;
 		for (int i=0;i<events.size();i++){
 			event = events.get(i);
 			EventPackage pack = new EventPackage(i, events.size(), event);
 			returnList.add(pack);
 		}
 		return returnList;
 	}
 	
 	//Should probably not be used, will cause massive connection problems...
 	private void SendResponsePackage(DataPackage pack) throws IOException{
 		PackageSender sender = new PackageSender();
 		sender.sendPackage(pack);
 		
 	}
 }
