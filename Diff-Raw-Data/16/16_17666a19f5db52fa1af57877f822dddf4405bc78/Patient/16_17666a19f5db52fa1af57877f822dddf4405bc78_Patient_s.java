 package hms.models;
 
 import java.util.ArrayList;
 import java.sql.SQLException;
 import java.sql.ResultSet;
 import java.util.Date;
 import java.util.Vector;
 
 import hms.util.Database;
 import hms.util.Priority;
 import hms.util.*;
 
 public class Patient {
 	//TODO create getters and setters
 	public String healthcare_number;
 	public String name;
 	public String phone_number;
 	public String email;
 	private String gender;
 	private String treatment;
 	public String address;
 	private Date birthdate;
 	private String medications;
 	private String special_care;
 	private String history;
 	private String comments;
 	private String emerg_name;
 	private String emerg_phone_number;
 	private String emerg_email;
 	private String in_hospital;
 	public int ward_id;
 	public int room_id;
 	public int bed_id;
 	private Priority priority;
 	public ArrayList<String> errors = new ArrayList<String>();
 	public static final String RESERVED_TEST_NAME = "Forbidden Name";
 
 	public Patient()
 	{}
 
 	public Patient(String healthcare_number, 
 			String name, 
 			String phone_number, 
 			String email,
 			String gender, 
 			String treatment, 
 			String address, 
 			Date birthdate, 
 			String medications, 
 			String special_care, 
 			String history, 
 			String comments, 
 			String emerg_name, 
 			String emerg_phone_number, 
 			String emerg_email,
 			boolean in_hospital,
 			int ward,
 			int room,
 			int bed,
 			Priority priority) {
 		this.name = name;
 		this.phone_number = phone_number;
 		this.email = email;
 		this.healthcare_number = healthcare_number;
 		this.gender = gender;
 		this.treatment = treatment;
 		this.address = address;
 		this.birthdate = birthdate;
 		this.medications = medications;
 		this.special_care = special_care;
 		this.history = history;
 		this.comments = comments;
 		this.emerg_name = emerg_name;
 		this.emerg_phone_number = emerg_phone_number;
 		this.emerg_email = emerg_email;
 		this.in_hospital = in_hospital ? "Y" : "N";
 		this.ward_id = ward;
 		this.room_id = room;
 		this.bed_id = bed;
 		this.priority = priority;
 	}
 
 	/**
 	 * Finds the patient corresponding to the given healthcare number in a patient object. If
 	 * the patient doesn't exist, returns null.
 	 * @param healthcare_number The healthcare number for the patient to be found
 	 * @return Patient object if found, or null if it isn't
 	 */
 	public static Patient find(String healthcare_number) throws SQLException {
 		ResultSet patient = Database.getInstance().executeQuery("SELECT * FROM patient WHERE healthcare_number = '" + healthcare_number + "'");
 		patient.last();
 		if(patient.getRow() == 0) {
 			return null;
 		} else {
 			patient.first();
 		}
 		return new Patient(
 				Encryptor.decode(patient.getString(1)), 
 				Encryptor.decode(patient.getString(2)), 
 				Encryptor.decode(patient.getString(3)),
 				Encryptor.decode(patient.getString(4)), 
 				patient.getString(5), 
 				Encryptor.decode(patient.getString(6)), 
 				Encryptor.decode(patient.getString(7)),
 				patient.getDate(8), 
 				Encryptor.decode(patient.getString(9)), 
 				Encryptor.decode(patient.getString(10)),
 				Encryptor.decode(patient.getString(11)),
 				Encryptor.decode(patient.getString(12)), 
 				Encryptor.decode(patient.getString(13)), 
 				Encryptor.decode(patient.getString(14)), 
 				Encryptor.decode(patient.getString(15)),
 				patient.getBoolean(16), 
 				patient.getInt(17), 
 				patient.getInt(18), 
 				patient.getInt(19), 
 				Priority.fromInteger(patient.getInt(20)));
 	}
 
 	public static Patient[] findByName(String name) throws SQLException {
 		ResultSet patientSet = Database.getInstance().executeQuery("SELECT * FROM patient WHERE name = '" + Encryptor.encode(name) + "'");
 		patientSet.last();
 		int numPatients = 0;
 		if(patientSet.getRow() == 0) {
 			return null;
 		} else {
 			numPatients = patientSet.getRow();
 			patientSet.first();
 		}
 		Patient [] patients = new Patient[numPatients];
 
 		for(int i = 0; i < numPatients; i++) {
 			patients[i] = new Patient(
 					Encryptor.decode(patientSet.getString(1)), 
 					Encryptor.decode(patientSet.getString(2)), 
 					Encryptor.decode(patientSet.getString(3)),
 					Encryptor.decode(patientSet.getString(4)), 
 					patientSet.getString(5), 
 					Encryptor.decode(patientSet.getString(6)), 
 					Encryptor.decode(patientSet.getString(7)),
 					patientSet.getDate(8), 
 					Encryptor.decode(patientSet.getString(9)), 
 					Encryptor.decode(patientSet.getString(10)), 
 					Encryptor.decode(patientSet.getString(11)),
 					Encryptor.decode(patientSet.getString(12)), 
 					Encryptor.decode(patientSet.getString(13)), 
 					Encryptor.decode(patientSet.getString(14)), 
 					Encryptor.decode(patientSet.getString(15)),
 					patientSet.getBoolean(16), 
 					patientSet.getInt(17), 
 					patientSet.getInt(18), 
 					patientSet.getInt(19), 
 					Priority.fromInteger(patientSet.getInt(20)));
 			patientSet.next();
 		}
 		return patients;
 	}
 
 	// /**
 	//  * Finds all patients and returns them as in a patient objects. If
 	//  * no patients exist, null is returnes
 	//  * @return Patients if found, or null if there arent any
 	//  */
 	// public static Vector<Patient> findAllPatients() throws SQLException{
 	// 	ResultSet patient = Database.getInstance().executeQuery("SELECT * FROM patient");
 	// 	if (patient == null) return null;
 	// 	patient.first();
 	// 	Vector<Patient> paitents = new Vector<Patient>();
 	// 	while(!patient.isLast()){
 	// 		paitents.add(new Patient(patient.getString(1), patient.getString(2), patient.getString(3),
 	//                                     patient.getString(4), patient.getString(5), patient.getString(6), patient.getString(7),
 	//                                     patient.getDate(8), patient.getString(9), patient.getString(10), patient.getString(11),
 	//                                     patient.getString(12), patient.getString(13), patient.getString(14), patient.getString(15),
 	//                                     patient.getBoolean(16), patient.getInt(17), patient.getInt(18), patient.getInt(19)) );
 	// 		if(!patient.next()) return null;
 	// 	}
 	// 	return paitents;
 	// }
 
 	/**
 	 * Tries to save the patient to the database. Returns true on a successful save or false
 	 * if the save fails for any reason.
 	 * @return true if the save is successful; false otherwise
 	 */
 	public boolean create() throws SQLException {
 		if(this.name.equals(RESERVED_TEST_NAME)) {
 			return false; // a patient is not allowed to have this name, because it is reserved for testing purposes
 		}
 		
 			int rows_added = Database.getInstance().executeUpdate("INSERT INTO patient VALUES ('" + 
 					Encryptor.encode(this.healthcare_number) + "','" + 
 					Encryptor.encode(this.name) + "','" + 
 					Encryptor.encode(this.phone_number) + "','" + 
 					Encryptor.encode(this.email) + "','" + 
					this.gender + "','" + 
 					Encryptor.encode(this.treatment) + "','" + 
 					Encryptor.encode(this.address) + "','" + 
 					new java.sql.Date(this.birthdate.getTime()) + "','" + 
 					Encryptor.encode(this.medications) + "','" + 
 					Encryptor.encode(this.special_care) + "','" + 
 					Encryptor.encode(this.history) + "','" + 
 					Encryptor.encode(this.comments) + "','" + 
 					Encryptor.encode(this.emerg_name) + "','" + 
 					Encryptor.encode(this.emerg_phone_number) + "','" + 
 					Encryptor.encode(this.emerg_email) + "','"+
 					Encryptor.encode(this.in_hospital) + "','" + 
 					this.ward_id + "','" + 
 					this.room_id + "','" + 
 					this.bed_id + "','" +
 					this.priority.toInteger() + "')");
 			this.errors.clear();
 			return true;
 		
 	}
 
 	/**
 	 * Tries to delete the patient from the database. Returns true on a successful delete or
 	 * false if the delete fails for any reason.
 	 * @return true if the delete is successful; false otherwise
 	 */
 	public boolean delete() throws SQLException {
 		try {
 			int patient = Database.getInstance().executeUpdate("DELETE FROM patient WHERE healthcare_number = '" + this.healthcare_number + "'");
 			if (patient == 0) {
 				this.errors.add("Patient does not exist");
 				return false;
 			}
 			this.errors.clear();
 			return true;
 		} catch (SQLException sqle) {
 			this.errors.add("Could not delete patient");
 			return false;
 		}
 	}
 
 	/**
 	 * Tries to delete a patient with a corresponding healthcare number from the database.
 	 * Static version of delete() for convenience's sake.
 	 * @return true if the delete is successful; false otherwise
 	 */
 	public static boolean deleteFromString(String healthcare_number) {
 		try {
 			int patient = Database.getInstance().executeUpdate("DELETE FROM patient WHERE healthcare_number = '" + healthcare_number + "'");
 			if (patient == 0) {
 				return false;
 			}
 			return true;
 		} catch (SQLException sqle) {
 			return false;
 		}
 	}
 }
