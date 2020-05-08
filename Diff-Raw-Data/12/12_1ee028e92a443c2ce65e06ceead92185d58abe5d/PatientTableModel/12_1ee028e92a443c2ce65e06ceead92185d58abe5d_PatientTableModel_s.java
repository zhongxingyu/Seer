 /**
  * Code adapted from http://www.pioverpi.net/2009/07/02/jtables-jdbc-pt-4-putting-it-all-together/
  */
 package hms.models;
 
 import javax.swing.table.AbstractTableModel;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import hms.util.*;
 
 public class PatientTableModel extends AbstractTableModel {
 	private final String[] columnNames = {"Health #", "Name", "Gender", "Birthdate", "In Hospital?", "Priority"};
 	private Patient[] patients;
 	
 	/**
	 * Constructs a new PatientTableModel. Sets the columnNames and columnClasses and content
	 * instance variables. If they are inaccessible, sets them to arrays of empty strings.
 	 */
 	public PatientTableModel() {
 		try {
 			getTableContents();
 		} catch (SQLException sqle) {
 			patients = new Patient[0];
 		}
 	}
 	
 	/**
 	 * Returns the number of columns in the table.
 	 * @return The number of columns in the table.
 	 */
 	public int getColumnCount() {
 		return columnNames.length;
 	}
 	
 	/**
 	 * Returns the number of rows in the table.
 	 * @return The number of rows in the table.
 	 */
 	public int getRowCount() {
 		return patients.length;
 	}
 	
 	/**
 	 * Returns the object in the database in the row'th row and the col'th column in that
 	 * row.
 	 * @param row The row of the object to be returned
 	 * @param col The column of the object to be returned in the row
 	 * @return The object at the specified location in the database
 	 */
 	public Object getValueAt(int row, int col) {
 		Patient patient = patients[row];
 		switch (col) {
 			case 0: return patient.getHealthcareNumber();
 			case 1: return patient.getName();
 			case 2: return patient.getGender();
 			case 3: return patient.getBirthdate();
 			case 4: return patient.getInHospital();
 			case 5: return patient.getPriority();
 			default: return "";
 		}
 	}
 	
 	/**
	 * Cells are not editable because we have the create and edit forms
 	 * @return false
 	 */
 	public boolean isCellEditable(int row, int col) {
 		return false;
 	}
 	
 	/**
 	 * Returns the name of the given column
 	 * @param col The column to find the name of
 	 * @return The name of the given column
 	 */
 	public String getColumnName(int col) {
 		return columnNames[col];
 	}
 	
 	/**
	 * Gets all of the table contents and stores them in an array of an array of Objects
	 * so we do not need to query the database all of the time.
 	 */
 	private void getTableContents() throws SQLException {
 		ResultSet patientsResult = Database.getInstance().executeQuery("SELECT * FROM patient");
 		
 		ArrayList<Patient> patientList = new ArrayList<Patient>();
 		
 		for (int i = 0; patientsResult.next(); i++) {
 			patientList.add(new Patient(Encryptor.decode(patientsResult.getString(1)), 
 									  Encryptor.decode(patientsResult.getString(2)), 
 									  Encryptor.decode(patientsResult.getString(3)),
 									  Encryptor.decode(patientsResult.getString(4)), 
 									  Encryptor.decode(patientsResult.getString(5)), 
 									  Encryptor.decode(patientsResult.getString(6)), 
 									  Encryptor.decode(patientsResult.getString(7)),
 									  patientsResult.getDate(8),
 									  Encryptor.decode(patientsResult.getString(9)), 
 									  Encryptor.decode(patientsResult.getString(10)),
 									  Encryptor.decode(patientsResult.getString(11)),
 									  Encryptor.decode(patientsResult.getString(12)), 
 									  Encryptor.decode(patientsResult.getString(13)), 
 									  Encryptor.decode(patientsResult.getString(14)), 
 									  Encryptor.decode(patientsResult.getString(15)),
 									  patientsResult.getBoolean(16), 
 									  patientsResult.getInt(17), 
 									  patientsResult.getInt(18), 
 									  patientsResult.getInt(19), 
 									  Priority.fromInteger(patientsResult.getInt(20))));
 		}
 		
 		patients = new Patient[patientList.size()];
 		for (int i = 0; i < patients.length; i++) {
 			patients[i] = patientList.get(i);
 		}
 		
 		patientsResult.close();
 	}
 	
 	/**
 	 * Called when the table data has been changed. Re-fetches the table contents from
 	 * the database and then calls the superclass' implementation.
 	 */
 	public void fireTableDataChanged() {
 		try {
 			getTableContents();
 		} catch (SQLException sqle) {
 			// Do nothing. Keep old contents
 		}
 		super.fireTableDataChanged();
 	}
 }
