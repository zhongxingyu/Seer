 package hms.models;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import hms.util.*;
 
 public class Nurse implements AbstractModel {
 
 	private String name;
 	private String phone_number;
 	private String pager_number;
 	private String email_address;
 	private String address;
 	private String sin;
 	private int id_number;
 	private String gender;
 	private int salary;
 	private int wardNumber;
 	private String password;
 
 	public Nurse(String name, String phone_number, String pager_number, String email_address, String address, String sin, int id_number, String gender, int salary, int ward, String password) {
 		this.name = name;
 		this.phone_number = phone_number;
 		this.pager_number = pager_number;
 		this.email_address = email_address;
 		this.address = address;
 		this.sin = sin;
 		this.id_number = id_number;
 		this.gender = gender;
 		this.salary = salary;
 		this.wardNumber = ward;
 		this.password = password;
 	}
 
 	/**
 	 * Finds the nurse corresponding to the given id number in a nurse object. If
 	 * the nurse doesn't exist, returns null.
 	 * @param id_number The id number for the patient to be found
 	 * @return Nurse object if found, or null if it isn't
 	 */
 	public static Nurse find(int id_number) throws SQLException {
 		ResultSet nurse = Database.getInstance().executeQuery("SELECT * FROM nurse WHERE id_number = '" + id_number + "'");
 		nurse.last();
 		if(nurse.getRow() == 0) {
 			return null;
 		} else {
 			nurse.first();
 		}
 		return new Nurse(nurse.getString(1), nurse.getString(2), nurse.getString(3), nurse.getString(4), 
 				nurse.getString(5), nurse.getString(6), Integer.parseInt(nurse.getString(7)), nurse.getString(8), 
 				Integer.parseInt(nurse.getString(9)), Integer.parseInt(nurse.getString(10)), Encryptor.decode(nurse.getString(11)));
 	}
 
 	/**
 	 * Finds all nurses and returns them as in nurse objects. If
 	 * no nurses exist, null is returned
 	 * @return Nurse if found, or null if there arent any
 	 */
 	public static Vector<Nurse> findAllNurses() throws SQLException{
 		ResultSet nurse = Database.getInstance().executeQuery("SELECT * FROM nurse");
 		if (nurse == null) return null;
 		nurse.first();
 		Vector<Nurse> nurses = new Vector<Nurse>();
 		while(!nurse.isLast()){
 			nurses.add(new Nurse(nurse.getString(1), nurse.getString(2), nurse.getString(3), nurse.getString(4), 
 					nurse.getString(5), nurse.getString(6), Integer.parseInt(nurse.getString(7)), nurse.getString(8), 
 					Integer.parseInt(nurse.getString(9)), Integer.parseInt(nurse.getString(10)), Encryptor.decode(nurse.getString(11)) ));
 			if(!nurse.next()) return null;
 		}
 		return nurses;
 	}
 
 	/**
 	 * Tries to save the nurse to the database. Returns true on a successful save or false
 	 * if the save fails for any reason.
 	 * @return true if the save is successful; false otherwise
 	 */
 	public boolean create() throws SQLException {
 		try {
 			int rows_added = Database.getInstance().executeUpdate("INSERT INTO nurse VALUES ('" + 
 					this.name + "','" + this.phone_number + "','" + this.pager_number + "','" + 
 					this.email_address + "','" + this.address + "','" + this.sin + "','" + 
 					this.id_number + "','" + this.gender + "','" + 
 					this.salary + "','" + this.wardNumber + "','" + Encryptor.encode(this.password) + "')");
 			return true;
 		} catch (SQLException sqle) {
 			return false;
 		}
 	}
 
 	/**
 	 * Tries to delete the nurse from the database. Returns true on a successful delete or
 	 * false if the delete fails for any reason.
 	 * @return true if the delete is successful; false otherwise
 	 */
 	public boolean delete() throws SQLException {
 		try {
 			int nurse = Database.getInstance().executeUpdate("DELETE FROM nurse WHERE id_number = '" + this.id_number + "'");
 			if (nurse == 0) {
 				return false;
 			}
 			return true;
 		} catch (SQLException sqle) {
 			return false;
 		}
 	}
 
 	/**
 	 * Tries to delete the nurse specified from idNumber from the database. Returns true on a 
 	 * successful delete or false if the delete fails.
 	 * @param idNumber
 	 * @return true if successful, false otherwise.
 	 */
 	public static boolean deleteFromInteger(int idNumber) {
 		try {
 			int nurse = Database.getInstance().executeUpdate("DELETE FROM nurse WHERE id_number = '" + idNumber + "'");
 			if (nurse == 0) {
 				return false;
 			}
 			return true;
 		} catch (SQLException sqle) {
 			return false;
 		}
 	}
 
 	/**
 	 * Generates an ID number that is not being used by any other nurses. Starts at 1 and increments until an unused one is found.
 	 * 
 	 * @return String IDNumber
 	 */
 	public static int generateIDNumber(){
 		int id = 1;
 
 		try {
 			ResultSet nurses = Database.getInstance().executeQuery("SELECT id_number FROM nurse");
 			if(nurses == null){}
 			else {
 				int i = 0;
 				while(true){
 					boolean incremented = false;
 					nurses.first();
 					while(!nurses.isAfterLast()) {
 						if(id == nurses.getInt("id_number")) {
 							incremented = true;
 							id++;
 							break;
 						}
 						nurses.next();
 					}
 					if(incremented == false) {
 						//did not find an existing nurse with that id number
 						break;
 					}
 				}
 			}
 		} catch(SQLException e) {
 			//error occurred. Ignore for now.
 		}
 		return id;
 	}
 
 	public static boolean authenticate(String username, String password) throws SQLException {
 		ResultSet user = null;
 		try{
 			user = Database.getInstance().executeQuery("SELECT id_number, password, COUNT(*) FROM nurse WHERE id_number = '" + Integer.parseInt(username) + "'");
 		}catch(NumberFormatException e){
 			return false;
 		}
 		user.next();
 		if (user.getInt(3) == 0) {
 			return false;
 		}
 		if (user.getString("password") == null || user.getString("password").equals(Encryptor.decode(password))) {
 			return true;
 		}
 		return false;
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public String getPhoneNumber() {
 		return this.phone_number;
 	}
 	
 	public String getPagerNumber() {
 		return this.pager_number;
 	}
 	
 	public String getEmail() {
 		return this.email_address;
 	}
 	
 	public String getAddress() {
 		return this.address;
 	}
 	
 	public String getSIN() {
 		return this.sin;
 	}
 	
 	public int getID() {
		return this.id;
 	}
 	
 	public String getGender() {
 		return this.gender;
 	}
 	
 	public int getSalary() {
 		return this.salary;
 	}
 	
 	public Ward getWard() {
 		return Ward.find(this.wardNumber);
 	}
 	
 	public String getPassword() {
 		return this.password;
 	}
 }
