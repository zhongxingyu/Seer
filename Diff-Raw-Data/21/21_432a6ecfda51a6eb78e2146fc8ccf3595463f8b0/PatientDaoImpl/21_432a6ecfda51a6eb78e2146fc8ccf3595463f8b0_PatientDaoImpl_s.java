 package com.secondopinion.common.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.secondopinion.common.model.Patient;
 import com.secondopinion.common.model.PatientAllergy;
 import com.secondopinion.common.model.PatientFile;
 import com.secondopinion.common.model.PatientMedication;
 import com.secondopinion.common.model.PatientProcedure;
 import com.secondopinion.common.model.PatientSymptom;
 import com.secondopinion.common.model.User;
 
 @Component
 public class PatientDaoImpl implements PatientDao{
 
 	@Autowired
 	private DataSource dataSource;
 	
 	@Override
 	public void insert(User user, Patient patient) {
 		String sql = "INSERT INTO patient " +
 				"(USER_ID, PATIENT_NAME, DATE_OF_BIRTH, GENDER, LOCATION) VALUES (?, ?, ?, ?, ?)";
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, user.getUserId());
 			ps.setString(2, patient.getName());
 			ps.setDate(3, new java.sql.Date(patient.getDateOfBirth().getTime()));
 			ps.setString(4, patient.getGender());
 			ps.setString(5, patient.getLocation());
 			ps.executeUpdate();
 			ps.close();
  
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
  
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 		
 	}
 
 	@Override
 	public Patient findByPatientId(int patientId) {
 		String sql = "SELECT * FROM patient WHERE PATIENT_ID = ?";
 		 
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, patientId);
 			Patient patient = null;
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				patient = new Patient(
 					rs.getInt("PATIENT_ID"),
 					rs.getInt("USER_ID"),
 					rs.getString("PATIENT_NAME"),
 					rs.getTimestamp("DATE_OF_BIRTH"),
 					rs.getString("GENDER"),
 					rs.getString("LOCATION"), 
 					rs.getString("PROFILE_PIC_URL")
 				);
 			}
 			rs.close();
 			ps.close();
 			return patient;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (conn != null) {
 				try {
 				conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 	
 	@Override
 	public Patient findByUserId(int userId) {
 		String sql = "SELECT * FROM patient WHERE USER_ID = ?";
 		 
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, userId);
 			Patient patient = null;
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				patient = new Patient(
 					rs.getInt("PATIENT_ID"),
 					rs.getInt("USER_ID"),
 					rs.getString("PATIENT_NAME"),
 					rs.getTimestamp("DATE_OF_BIRTH"),
 					rs.getString("GENDER"),
 					rs.getString("LOCATION"),
 					rs.getString("PROFILE_PIC_URL")
 				);
 			}
 			rs.close();
 			ps.close();
 			return patient;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (conn != null) {
 				try {
 				conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 
 	@Override
 	public void updatePatient(Patient patient) {
 		String sql = "UPDATE patient " +
 				"SET PATIENT_NAME=?, DATE_OF_BIRTH=?, GENDER=?, LOCATION=?, PROFILE_PIC_URL=? " +
 				"WHERE PATIENT_ID=?";
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setString(1, patient.getName());
 			ps.setDate(2, new java.sql.Date(patient.getDateOfBirth().getTime()));
 			ps.setString(3, patient.getGender());
 			ps.setString(4, patient.getLocation());
			ps.setInt(5, patient.getPatientId());
			ps.setString(6, patient.getProfilePicUrl());
 			ps.executeUpdate();
 			ps.close();
  
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
  
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 	
 	@Override
 	public List<PatientMedication> fetchPatientMedications(int patientId) {
 		String sql = "SELECT * FROM patient_medication WHERE PATIENT_ID = ?";
 		 
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, patientId);
 			List<PatientMedication> medicationList = new ArrayList<PatientMedication>();
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				PatientMedication medication = new PatientMedication(
 					rs.getInt("MEDICATION_ID"),
 					rs.getInt("PATIENT_ID"),
 					rs.getString("MEDICATION_NAME"),
 					rs.getString("NOTES")
 				);
 				medicationList.add(medication);
 			}
 			rs.close();
 			ps.close();
 			return medicationList;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (conn != null) {
 				try {
 				conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 	
 	@Override
 	public void insertPatientMedication(Patient patient, PatientMedication patientMedication) {
 		String sql = "INSERT INTO patient_medication " +
 				"(PATIENT_ID, MEDICATION_NAME, NOTES) VALUES (?, ?, ?)";
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, patient.getPatientId());
 			ps.setString(2, patientMedication.getMedicationName());
 			ps.setString(3, patientMedication.getNotes());
 			ps.executeUpdate();
 			ps.close();
  
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
  
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 		
 	}
 	
 	@Override
 	public void deletePatientMedication(int medicationId) {
 		String sql = "DELETE FROM patient_medication " +
 				"WHERE MEDICATION_ID=?";
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, medicationId);
 			ps.executeUpdate();
 			ps.close();
  
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
  
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {}
 			}
 		}		
 	}
 
 	@Override
 	public void insertPatientFile(Patient patient, PatientFile patientFile) {
 		String sql = "INSERT INTO patient_file " +
 				"(PATIENT_ID, FILE_NAME, DESCRIPTION, FILE_URL) VALUES (?, ?, ?, ?)";
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, patient.getPatientId());
 			ps.setString(2, patientFile.fileName);
 			ps.setString(3, patientFile.description);
 			ps.setString(4, patientFile.fileUrl);
 			ps.executeUpdate();
 			ps.close();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
  
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 		
 	}
 	@Override
 	public void insertPatientSymptom(Patient patient, PatientSymptom patientSymptom) {
 		String sql = "INSERT INTO patient_symptom " +
 				"(PATIENT_ID, SYMPTOM_NAME, NOTES) VALUES (?, ?, ?)";
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, patient.getPatientId());
 			ps.setString(2, patientSymptom.symptomName);
 			ps.setString(3, patientSymptom.notes);
 			ps.executeUpdate();
 			ps.close();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
  
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 	
 	@Override
 	public void deletePatientSymptom(int symptomId) {
 		String sql = "DELETE FROM patient_symptom " +
 				"WHERE SYMPTOM_ID=?";
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, symptomId);
 			ps.executeUpdate();
 			ps.close();
  
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
  
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {}
 			}
 		}		
 		
 	}
 	
 	@Override
 	public List<PatientSymptom> fetchPatientSymptoms(int patientId) {
 		String sql = "SELECT * FROM patient_symptom WHERE PATIENT_ID = ?";
 		 
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, patientId);
 			List<PatientSymptom> symptomList = new ArrayList<PatientSymptom>();
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				PatientSymptom symptom = new PatientSymptom(
 					rs.getInt("SYMPTOM_ID"),
 					rs.getInt("PATIENT_ID"),
 					rs.getString("SYMPTOM_NAME"),
 					rs.getString("NOTES")
 				);
 				symptomList.add(symptom);
 			}
 			rs.close();
 			ps.close();
 			return symptomList;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (conn != null) {
 				try {
 				conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 	public void insertPatientAllergy(Patient patient,
 			PatientAllergy patientAllergy){
 		String sql = "INSERT INTO patient_allergy " +
 				"(PATIENT_ID, ALLERGY_NAME, NOTES) VALUES (?, ?, ?)";
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, patient.getPatientId());
 			ps.setString(2, patientAllergy.allergyName);
 			ps.setString(3, patientAllergy.notes);
 			ps.executeUpdate();
 			ps.close();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
  
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 		
 	}
 	public List<PatientAllergy> fetchPatientAllergies(int patientId) {
 		String sql = "SELECT * FROM patient_allergy WHERE PATIENT_ID = ?";
 		 
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, patientId);
 			List<PatientAllergy> allergyList = new ArrayList<PatientAllergy>();
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				PatientAllergy allergy = new PatientAllergy(
 					rs.getInt("ALLERGY_ID"),
 					rs.getInt("PATIENT_ID"),
 					rs.getString("ALLERGY_NAME"),
 					rs.getString("NOTES")
 				);
 				allergyList.add(allergy);
 			}
 			rs.close();
 			ps.close();
 			return allergyList;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (conn != null) {
 				try {
 				conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 	public void deletePatientAllergy(int allergyId) {
 		String sql = "DELETE FROM patient_allergy " +
 				"WHERE ALLERGY_ID=?";
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, allergyId);
 			ps.executeUpdate();
 			ps.close();
  
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
  
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {}
 			}
 		}		
 		
 	}
 	
 	public List<PatientProcedure> fetchPatientProcedures(int patientId) {
 		String sql = "SELECT * FROM patient_procedure WHERE PATIENT_ID = ?";
 		 
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, patientId);
 			List<PatientProcedure> procedureList = new ArrayList<PatientProcedure>();
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				PatientProcedure procedure = new PatientProcedure(
 					rs.getInt("PROCEDURE_ID"),
 					rs.getInt("PATIENT_ID"),
 					rs.getString("PROCEDURE_NAME"),
 					rs.getString("NOTES")
 				);
 				procedureList.add(procedure);
 			}
 			rs.close();
 			ps.close();
 			return procedureList;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (conn != null) {
 				try {
 				conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 	public void insertPatientProcedure(Patient patient,
 			PatientProcedure patientProcedure) {
 		String sql = "INSERT INTO patient_procedure " +
 				"(PATIENT_ID, PROCEDURE_NAME, NOTES) VALUES (?, ?, ?)";
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, patient.getPatientId());
 			ps.setString(2, patientProcedure.procedureName);
 			ps.setString(3, patientProcedure.notes);
 			ps.executeUpdate();
 			ps.close();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
  
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 	public void deletePatientProcedure(int procedureId) {
 		String sql = "DELETE FROM patient_procedure " +
 				"WHERE PROCEDURE_ID=?";
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, procedureId);
 			ps.executeUpdate();
 			ps.close();
  
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
  
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {}
 			}
 		}		
 		
 	}
 	
 	public List<PatientFile> fetchPatientFiles(int patientId) {
 		String sql = "SELECT * FROM patient_file WHERE PATIENT_ID = ?";
 		 
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, patientId);
 			List<PatientFile> fileList = new ArrayList<PatientFile>();
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				PatientFile file = new PatientFile(
 					rs.getInt("FILE_ID"),
 					rs.getInt("PATIENT_ID"),
 					rs.getString("FILE_NAME"),
 					rs.getString("DESCRIPTION"),
 					rs.getString("FILE_URL")
 				);
 				fileList.add(file);
 			}
 			rs.close();
 			ps.close();
 			return fileList;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (conn != null) {
 				try {
 				conn.close();
 				} catch (SQLException e) {}
 			}
 		}
 	}
 	
 	public void deletePatientFile(int fileId) {
 		String sql = "DELETE FROM patient_file " +
 				"WHERE FILE_ID=?";
 		Connection conn = null;
  
 		try {
 			conn = dataSource.getConnection();
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ps.setInt(1, fileId);
 			ps.executeUpdate();
 			ps.close();
  
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
  
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {}
 			}
 		}		
 	}
 	
 	
 }
