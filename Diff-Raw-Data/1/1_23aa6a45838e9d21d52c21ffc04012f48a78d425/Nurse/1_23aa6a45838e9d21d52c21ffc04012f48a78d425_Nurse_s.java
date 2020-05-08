 package hms.models;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import hms.util.Database;
 
 public class Nurse {
 	
 	private String name;
 	private String phone_number;
 	private String pager_number;
 	private String email_address;
 	private String address;
 	private String sin;
 	private String id_number;
 	private String gender;
 	private int salary;
 	
 	public ArrayList<String> errors = new ArrayList<String>();
 	
 	
 	public Nurse(String name, String phone_number, String pager_number, String email_address, String address, String sin, String id_number, String gender, int salary) {
 		this.name = name;
 		this.phone_number = phone_number;
 		this.pager_number = pager_number;
 		this.email_address = email_address;
 		this.address = address;
 		this.sin = sin;
 		this.id_number = id_number;
 		this.gender = gender;
 		this.salary = salary;
 	}
 	
 	/**
 	 * Finds the nurse corresponding to the given id number in a nurse object. If
 	 * the nurse doesn't exist, returns null.
 	 * @param id_number The id number for the patient to be found
 	 * @return Nurse object if found, or null if it isn't
 	 */
 	public static Nurse find(String id_number) throws SQLException {
 		ResultSet nurse = Database.getInstance().executeQuery("SELECT * FROM nurse WHERE id_number = '" + id_number + "'");
 		nurse.last();
 		if(nurse.getRow() == 0) {
 			return null;
 		} else {
 			nurse.first();
 		}
 		return new Nurse(nurse.getString(1), nurse.getString(2), nurse.getString(3), nurse.getString(4), 
 				nurse.getString(5), nurse.getString(6), nurse.getString(7), nurse.getString(8), 
 				Integer.parseInt(nurse.getString(9)));
 	}
 	
 	/**
 	 * Finds all nurses and returns them as in nurse objects. If
 	 * no nurses exist, null is returned
 	 * @return Nurse if found, or null if there arent any
 	 */
 	public static Vector<Nurse> findAllPatients() throws SQLException{
 		ResultSet nurse = Database.getInstance().executeQuery("SELECT * FROM nurse");
 		if (nurse == null) return null;
 		nurse.first();
 		Vector<Nurse> nurses = new Vector<Nurse>();
 		while(!nurse.isLast()){
 			nurses.add(new Nurse(nurse.getString(1), nurse.getString(2), nurse.getString(3), nurse.getString(4), 
 					nurse.getString(5), nurse.getString(6), nurse.getString(7), nurse.getString(8), 
 					Integer.parseInt(nurse.getString(9))) );
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
 				this.salary + "')");
 			this.errors.clear();
 			return true;
 		} catch (SQLException sqle) {
 			this.errors.add("Couldn't add to database");
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
 				this.errors.add("Nurse does not exist");
 				return false;
 			}
 			this.errors.clear();
 			return true;
 		} catch (SQLException sqle) {
 			this.errors.add("Could not delete nurse");
 			return false;
 		}
 	}
 }
}
