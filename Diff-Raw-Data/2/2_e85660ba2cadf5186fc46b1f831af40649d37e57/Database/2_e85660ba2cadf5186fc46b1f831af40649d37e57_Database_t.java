 package suncertify.db.io;
 
 import java.io.File;
 import java.util.Scanner;
 
 import suncertify.shared.Preferences;
 
 public class Database {
 
 	public static final String DB_LOCATION = "DatabaseLocation";
 
 	private String location;
 
 	public Database() {
 		this.init();
 	}
 
 	private void init() {
 		Preferences props = Preferences.getInstance();
 
 		String dbLoc = props.get(DB_LOCATION);
 		if (dbLoc == null) {
 			// TODO: prompt user for File location
 			dbLoc = this.promptForLocation();
 			props.set(DB_LOCATION, dbLoc);
 		}
 
 		this.location = dbLoc;
 	}
 
 	private String promptForLocation() {
 		String location = null;
 		Scanner reader = new Scanner(System.in);
 
 		while (location == null) {
 			System.out.println("Enter location of the database below:");
 			System.out.print(" -> ");
 
 			location = reader.nextLine();
 			if (!isDBFileValid(location)) {
 				location = null;
 			}
 		}
 		reader.close();
 
 		return location;
 	}
 
 	private boolean isDBFileValid(String location) {
 		if (!new File(location).exists()) {
 			return false;
 		}
 		// TODO: Check if Magic Cookie matches
 		// TODO: Check if File is a valid DB file
		return true;
 	}
 
 	public String getLocation() {
 		return this.location;
 	}
 
 }
