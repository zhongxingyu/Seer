 package backend;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import shared.Doctor;
 import shared.Patient;
 
 public class PatientDAO implements SQLDAO<Patient, Integer> {
 
 	private static final String TABLE = "patients";
 
 	@Override
 	public Patient insert(Patient dao) throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("INSERT INTO ");
 		sb.append(PatientDAO.TABLE);
 		sb.append(" ( `id`,   `name`, `email`, `sms_email` ) ");
 		sb.append(" VALUES ");
 		sb.append(" ( NULL,    ?,      ?,       ?          );");
 
 		PreparedStatement ps = conn.prepareStatement(sb.toString(),
 				Statement.RETURN_GENERATED_KEYS);
 
 		ps.setString(1, dao.name);
 		ps.setString(2, dao.email);
 		ps.setString(3, dao.smsEmail);
 
 		// Try and add it
 		ps.executeUpdate();
 
 		ResultSet rs = ps.getGeneratedKeys();
 		if (rs.next()) {
 			// Retrieve the newly created id and pass back the Alert object
 			dao.id = rs.getInt(1);
 			return dao;
 		} else
 			throw new SQLException("Coul not retrieve updated values");
 	}
 
 	@Override
 	public boolean update(Patient dao) throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("UPDATE ");
 		sb.append(PatientDAO.TABLE);
 		sb.append(" SET ");
 		sb.append("name = ?, ");
 		sb.append("email = ?, ");
 		sb.append("sms_email = ? ");
 		sb.append(" WHERE id = ?");
 		
 		PreparedStatement ps = conn.prepareStatement(sb.toString());
 
 		ps.setString(1, dao.name);
 		ps.setString(2, dao.email);
 		ps.setString(3, dao.smsEmail);
 		ps.setInt(4, dao.id);
 
 		// Try and add it
 		ps.execute();
 
		return ps.getUpdateCount() > 0;
	}
 
 	@Override
 	public boolean delete(Patient dao) throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("DELETE FROM ");
 		sb.append(PatientDAO.TABLE);
 		sb.append(" WHERE id = ?");
 
 		PreparedStatement ps = conn.prepareStatement(sb.toString());
 
 		ps.setInt(1, dao.id);
 
 		// Try and add it
 		ps.execute();
 
 		return ps.getUpdateCount() > 0;
 
 	}
 
 	@Override
 	public List<Patient> findAll() throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT id FROM ");
 		sb.append(PatientDAO.TABLE);
 		PreparedStatement ps = conn.prepareStatement(sb.toString());
 
 		// Try and add it
 		ResultSet result = ps.executeQuery();
 
 		List<Patient> list = new ArrayList<Patient>();
 
 		while (result.next()) {
 			list.add(findByPrimaryKey(result.getInt("id")));
 		}
 
 		return list;
 	}
 
 	@Override
 	public Patient findByPrimaryKey(Integer key) throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT * FROM ");
 		sb.append(PatientDAO.TABLE);
 		sb.append(" WHERE id = ?");
 		PreparedStatement ps = conn.prepareStatement(sb.toString());
 
 		ps.setInt(1, key);
 
 		// Try and add it
 		ResultSet result = ps.executeQuery();
 
 		if (result.next()) {
 			return new Patient(result.getInt("id"), result.getString("name"),
 					result.getString("email"), result.getString("sms_email"));
 		} else
 			return null;
 	}
 
 }
