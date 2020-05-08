 package com.ntnu.eit.database;
 
 import com.ntnu.eit.common.model.*;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class Database {
 
 	private Connection connection;
 	private String databaseName;
 
 	/* Initializes connection to the database */
 	public Database() {
 		try {
 			String databaseDriver = "org.apache.derby.jdbc.ClientDriver";
 			Class.forName(databaseDriver).newInstance();
 			databaseName = "jdbc:derby://localhost:1527/drtabletDB";
 		} catch (Exception sql) {
 			System.out.println("DataSourceError: " + sql);
 		}
 	}
 
 	/* Connects to the database */
 	public void connect() {
 		try {
 			connection = DriverManager.getConnection(databaseName, "user",
 					"pass");
 		} catch (SQLException sqle) {
 			System.out.println("Connection Error: " + sqle);
 		}
 	}
 
 	/* Disconnects from the database */
 	public void disconnect() {
 		Cleaner.closeConnection(connection);
 	}
 
 	/**
 	 * This method returns all the departments currently registered in the
 	 * database.
 	 */
 
 	public ArrayList<Department> getDepartments() {
 		PreparedStatement prpstm = null;
 		ResultSet res = null;
 		ArrayList<Department> departments = new ArrayList<Department>();
 		connect();
 		try {
 			prpstm = connection.prepareStatement("SELECT * FROM department");
 			res = prpstm.executeQuery();
 			while (res.next()) {
 				Department d = new Department(res.getInt("department_id"),
 						res.getString("name"));
 				departments.add(d);
 			}
 		} catch (SQLException sqle) {
 			Cleaner.writeMessage(sqle, "@getDepartments()");
 		} finally {
 			Cleaner.closePreparedStatement(prpstm);
 			Cleaner.closeResultSet(res);
 			disconnect();
 		}
 		return departments;
 	}
 
 	/**
 	 * This method returns the patient list for a given department.
 	 */
 	public ArrayList<Patient> getPatientList(int departmentId) {
 		ArrayList<Patient> patients = new ArrayList<Patient>();
 		PreparedStatement prpstm = null;
 		ResultSet res = null;
 		connect();
 		try {
 			prpstm = connection
 					.prepareStatement("SELECT * FROM patient WHERE department_id = ? ORDER BY lastname DESC, firstname DESC");
 			prpstm.setInt(1, departmentId);
 			res = prpstm.executeQuery();
 			while (res.next()) {
 				Patient p = new Patient(res.getInt("patient_id"), departmentId,
 						res.getString("firstname"), res.getString("lastname"),
 						res.getString("social_security_number"), null,
 						res.getInt("picture_offset"));
 				patients.add(p);
 			}
 		} catch (SQLException sqle) {
 			Cleaner.writeMessage(sqle, "@getPatientList()");
 		} finally {
 			Cleaner.closePreparedStatement(prpstm);
 			Cleaner.closeResultSet(res);
 			disconnect();
 		}
 		return patients;
 	}
 
 	/**
 	 * This method returns the picture for a given patient.
 	 */
 	public byte[] getPicture(int patientId) {
 		byte[] picture = null;
 		PreparedStatement prpstm = null;
 		ResultSet res = null;
 		connect();
 		try {
 			prpstm = connection
 					.prepareStatement("SELECT picture FROM patient WHERE patient_id = ?");
 			prpstm.setInt(1, patientId);
 			res = prpstm.executeQuery();
 			if (res.next()) {
 				picture = res.getBytes("picture");
 			}
 		} catch (SQLException sqle) {
 			Cleaner.writeMessage(sqle, "@getPicture()");
 		} finally {
 			Cleaner.closePreparedStatement(prpstm);
 			Cleaner.closeResultSet(res);
 			disconnect();
 
 		}
 		return picture;
 	}
 
 	/**
 	 * This method returns the deviations for a given patient.
 	 */
 	public ArrayList<Deviation> getDeviations(int patientId) {
 		ArrayList<Deviation> deviations = new ArrayList<Deviation>();
 		PreparedStatement prpstm = null;
 		ResultSet res = null;
 		connect();
 		try {
 			prpstm = connection
 					.prepareStatement("SELECT * FROM deviation WHERE patient_id = ? ORDER BY timestamp DESC");
 			prpstm.setInt(1, patientId);
 			res = prpstm.executeQuery();
 			while (res.next()) {
 				Deviation d = new Deviation(res.getInt("deviation_id"),
 						res.getString("description"), res.getDate("timestamp"));
 				deviations.add(d);
 			}
 		} catch (SQLException sqle) {
 			Cleaner.writeMessage(sqle, "@getDeviations()");
 		} finally {
 			Cleaner.closePreparedStatement(prpstm);
 			Cleaner.closeResultSet(res);
 			disconnect();
 
 		}
 		return deviations;
 	}
 
 	/**
 	 * This method creates a new deviation for a given patient.
 	 */
 	public boolean registerDeviation(int deviationId, String description, Date timestamp, int patientId) {
 		boolean ok = true;
         PreparedStatement prpstm = null;
 		connect();
 		try {
 			prpstm = connection
 					.prepareStatement("INSERT INTO deviation(deviation_ID, description, timestamp, patient_ID) VALUES(?,?,?,?)");
 			prpstm.setInt(1, deviationId);
 			prpstm.setString(2, description);
 			prpstm.setDate(3, timestamp);
 			prpstm.setInt(4, patientId);
 			prpstm.executeUpdate();
 		} catch (Exception sqle) {
 			Cleaner.writeMessage(sqle, "@registerDeviation()");
 		} finally {
 			Cleaner.closePreparedStatement(prpstm);
 			disconnect();
 		}
         return ok;
 	}
 
 	/**
 	 * This method updates a given deviation.
 	 */
 	public boolean changeDeviation(int deviationId, String description, Date timestamp) {
 		boolean ok = true;
         PreparedStatement prpstm = null;
 		connect();
 		try {
 			prpstm = connection
 					.prepareStatement("UPDATE deviation SET description = ?, timestamp = ? WHERE deviationId = ?");
 			prpstm.setString(1, description);
 			prpstm.setDate(2, timestamp);
 			prpstm.setInt(3, deviationId);
 			prpstm.executeUpdate();
 		} catch (Exception sqle) {
 			Cleaner.writeMessage(sqle, "@changeDeviation()");
 		} finally {
 			Cleaner.closePreparedStatement(prpstm);
 			disconnect();
 		}
         return ok;
 	}
 
 	/**
 	 * This method returns the tasks for a given patient.
 	 */
 	public ArrayList<Task> getTasks(int patientId) {
 		ArrayList<Task> tasks = new ArrayList<Task>();
 		PreparedStatement prpstm = null;
 		ResultSet res = null;
 		connect();
 		try {
 			prpstm = connection
 					.prepareStatement("SELECT * FROM task WHERE patient_id = ? ORDER BY timestamp DESC");
 			prpstm.setInt(1, patientId);
 			res = prpstm.executeQuery();
 			while (res.next()) {
 				boolean executed = res.getInt("executed") == 1;
 				Date date = (Date) res.getDate("timestamp");
 				Task t = new Task(res.getInt("task_id"),
 						res.getInt("medicine_id"),
 						res.getString("medicine_form"), date,
 						res.getString("dosage"), executed);
 				tasks.add(t);
 			}
 		} catch (SQLException sqle) {
 			Cleaner.writeMessage(sqle, "@getTasks()");
 		} finally {
 			Cleaner.closePreparedStatement(prpstm);
 			Cleaner.closeResultSet(res);
 			disconnect();
 
 		}
 		return tasks;
 	}
 
 	/**
 	 * This method updates a given task.
 	 */
 	public boolean changeTask(int taskId, boolean executed, Date timestamp) {
 		boolean ok = true;
         PreparedStatement prpstm = null;
 		connect();
 		try {
			prpstm = connection
					.prepareStatement("UPDATE task SET execuded = ?, timestamp = ? WHERE taskId = ?");
			if execuded{
                prpstm.setString(1, 1);
             }else{
                prpstm.setString(1, 0);
             }
 			prpstm.setDate(2, timestamp);
 			prpstm.setInt(3, taskId);
 			prpstm.executeUpdate();
 		} catch (Exception sqle) {
 			Cleaner.writeMessage(sqle, "@changeTask()");
 		} finally {
 			Cleaner.closePreparedStatement(prpstm);
 			disconnect();
 		}
         return ok;
 	}
 
 
 	/**
 	 * This method returns the emergency contacts for a given patient.
 	 */
 	public ArrayList<EmergencyContact> getEmergencyContacts(int patientId) {
 		ArrayList<EmergencyContact> emergencyContacts = new ArrayList<EmergencyContact>();
 		PreparedStatement prpstm = null;
 		ResultSet res = null;
 		connect();
 		try {
 			prpstm = connection
 					.prepareStatement("SELECT * FROM emergency_contacts WHERE patient_id = ? ORDER BY lastname DESC, firstname DESC");
 			prpstm.setInt(1, patientId);
 			res = prpstm.executeQuery();
 			while (res.next()) {
 				EmergencyContact ec = new EmergencyContact(
 						res.getInt("emergency_contact_id"),
 						res.getString("firstname"), res.getString("lastname"),
 						res.getString("phone_number"));
 				emergencyContacts.add(ec);
 			}
 		} catch (SQLException sqle) {
 			Cleaner.writeMessage(sqle, "@getEmergencyContacts()");
 		} finally {
 			Cleaner.closePreparedStatement(prpstm);
 			Cleaner.closeResultSet(res);
 			disconnect();
 		}
 		return emergencyContacts;
 	}
 
 	/**
 	 * This method returns the medicine for a given task.
 	 */
 	public ArrayList<Medicine> getMedicines(int taskId) {
 		ArrayList<Medicine> medicines = new ArrayList<Medicine>();
 		PreparedStatement prpstm = null;
 		ResultSet res = null;
 		connect();
 		try {
 			prpstm = connection
 					.prepareStatement("SELECT * FROM medicine WHERE task_id = ?");
 			prpstm.setInt(1, taskId);
 			res = prpstm.executeQuery();
 			while (res.next()) {
 				Medicine m = new Medicine(res.getInt("medicine_id"),
 						res.getString("name"));
 				medicines.add(m);
 			}
 		} catch (SQLException sqle) {
 			Cleaner.writeMessage(sqle, "@getMedicines()");
 		} finally {
 			Cleaner.closePreparedStatement(prpstm);
 			Cleaner.closeResultSet(res);
 			disconnect();
 		}
 		return medicines;
 	}
 
 }
