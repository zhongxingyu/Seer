 package hms.models;
 
 import java.util.*;
 import java.sql.SQLException;
 import java.sql.ResultSet;
 
 import hms.util.*;
 
 public class Patient implements AbstractModel {
 	private String healthcare_number;
 	private String name;
 	private String phone_number;
 	private String email;
 	private String gender;
 	private String treatment;
 	private String address;
 	private Date birthdate;
 	private String medications;
 	private String special_care;
 	private String history;
 	private String comments;
 	private String emerg_name;
 	private String emerg_phone_number;
 	private String emerg_email;
 	private boolean in_hospital;
 	private int ward_id;
 	private int room_id;
 	private int bed_id;
 	private Priority priority;
 	static final String RESERVED_TEST_NAME = "Forbidden Name";
 
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
 		this.in_hospital = in_hospital;
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
 				Encryptor.decode(patient.getString(5)), 
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
 					Encryptor.decode(patientSet.getString(5)), 
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
 					Encryptor.encode(this.gender) + "','" + 
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
 					this.in_hospital + "','" + 
 					this.ward_id + "','" + 
 					this.room_id + "','" + 
 					this.bed_id + "','" +
 					this.priority.toInteger() + "')");
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
 				return false;
 			}
 			return true;
 		} catch (SQLException sqle) {
 			return false;
 		}
 	}
 	
 	public String getHealthcareNumber() {
 		return this.healthcare_number;
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public String getPhoneNumber() {
 		return this.phone_number;
 	}
 	
 	public String getEmail() {
 		return this.email;
 	}
 	
 	public String getGender() {
 		return this.gender;
 	}
 	
 	public String getTreatment() {
 		return this.treatment;
 	}
 	
 	public String getAddress() {
 		return this.address;
 	}
 	
 	public Date getBirthdate() {
 		return this.birthdate;
 	}
 	
 	public String getMedications() {
 		return this.medications;
 	}
 	
 	public String getSpecialCare() {
 		return this.special_care;
 	}
 	
 	public String getHistory() {
 		return this.history;
 	}
 	
 	public String getComments() {
 		return this.comments;
 	}
 	
 	public String getEmergencyName() {
 		return this.emerg_name;
 	}
 	
 	public String getEmergencyPhoneNumber() {
 		return this.emerg_phone_number;
 	}
 	
 	public String getEmergencyEmail() {
 		return this.emerg_email;
 	}
 	
 	public boolean getInHospital() {
 		return this.in_hospital;
 	}
 	
 	public Priority getPriority() {
 		return priority;
 	}
 	
 	public Ward getWard() {
 		return Ward.find(this.ward_id);
 	}
 	
 	public Room getRoom() {
 		return Room.find(room_id);
 	}
 	
 	public Bed getBed() {
 		return Bed.find(bed_id);
 	}
 }
