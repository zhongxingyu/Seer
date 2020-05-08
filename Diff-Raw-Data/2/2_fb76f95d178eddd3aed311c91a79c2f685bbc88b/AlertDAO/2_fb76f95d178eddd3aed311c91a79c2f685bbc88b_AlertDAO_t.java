 package backend.DAO;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import backend.SQLDatabase;
 
 import shared.Alert;
 import shared.Alert.AlertType;
 import shared.Doctor;
 
 public class AlertDAO implements SQLDAO<Alert, Integer> {
 
 	private static final String TABLE = "alerts";
 
 	public void eraseTable() throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("TRUNCATE TABLE ");
 		sb.append(AlertDAO.TABLE);
 		PreparedStatement ps = conn.prepareStatement(sb.toString());
 
 		ps.execute();
 	}
 
 	@Override
 	public Alert insert(Alert dao) throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("INSERT INTO ");
 		sb.append(AlertDAO.TABLE);
 		sb.append(" ( `id`,   `name`, `description`, `type`, `date_time`, `read`, `for_doctor_id` ) ");
 		sb.append(" VALUES ");
 		sb.append(" ( NULL, ?,    ?,           ?,    ?,         ?,    ?             );");
 		
 		PreparedStatement ps = conn.prepareStatement(sb.toString(), Statement.RETURN_GENERATED_KEYS);
 
 		ps.setString(1, dao.name);
 		ps.setString(2, dao.description);
 		ps.setString(3, dao.getType().name());
 		ps.setTimestamp(4, dao.date);
 		ps.setBoolean(5, dao.read);
 		ps.setInt(6, dao.forDoctor.id);
 
 		// Try and add it
 		ps.executeUpdate();
 
 		ResultSet rs = ps.getGeneratedKeys();
 		if (rs.next()){
 			// Retrieve the newly created id and pass back the Alert object
 		    dao.id = rs.getInt(1);
 		    return dao;
 		}
 		else
 			throw new SQLException("Coul not retrieve updated values");
 	}
 
 	@Override
 	public boolean update(Alert dao) throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("UPDATE ");
 		sb.append(AlertDAO.TABLE);
 		sb.append(" SET ");
 		sb.append("name = ?, ");
 		sb.append("description = ?, ");
 		sb.append("type = ?, ");
 		sb.append("date_time = ?, ");
 		sb.append("read = ? ");
 		sb.append("for_doctor_id = ? ");
 		sb.append(" WHERE id = ?");
 		
 		PreparedStatement ps = conn.prepareStatement(sb.toString());
 
 		ps.setString(1, dao.name);
 		ps.setString(2, dao.description);
 		ps.setString(3, dao.getType().name());
 		ps.setTimestamp(4, dao.date);
 		ps.setBoolean(5, dao.read);
 		ps.setInt(6, dao.forDoctor.id);
 		ps.setInt(7, dao.id);
 
 		// Try and add it
 		ps.execute();
 
 		return ps.getUpdateCount() > 0;
 	}
 
 	@Override
 	public boolean delete(Alert dao) throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("DELETE FROM ");
 		sb.append(AlertDAO.TABLE);
 		sb.append(" WHERE id = ?");
 		
 		PreparedStatement ps = conn.prepareStatement(sb.toString());
 
 		ps.setInt(1, dao.id);
 
 		// Try and add it
 		ps.execute();
 
 		return ps.getUpdateCount() > 0;
 	}
 
 	@Override
 	public ArrayList<Alert> findAll() throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT id FROM ");
 		sb.append(AlertDAO.TABLE);
 		PreparedStatement ps = conn.prepareStatement(sb.toString());
 		
 		// Try and add it
 		ResultSet result = ps.executeQuery();
 
 		ArrayList<Alert> list = new ArrayList<Alert>();
 		
 		while (result.next()) {
 			list.add(findByPrimaryKey(result.getInt("id")));
 		}
 
 		return list;
 	}
 
 	@Override
 	public Alert findByPrimaryKey(Integer key) throws SQLException, IllegalArgumentException {
 		Connection conn = SQLDatabase.getConnection();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT * FROM ");
 		sb.append(AlertDAO.TABLE);
 		sb.append(" WHERE id = ?");
 		PreparedStatement ps = conn.prepareStatement(sb.toString());
 		
 		ps.setInt(1, key);
 		
 		// Try and add it
 		ResultSet result = ps.executeQuery();
 
 		DoctorDAO docDAO = new DoctorDAO();
 		
 		if (result.next()) {
 			AlertType type = AlertType.valueOf(result.getString("type"));
 			Doctor doc = docDAO.findByPrimaryKey(result.getInt("for_doctor_id"));
 			return new Alert(
 					result.getInt("id"),
 					result.getString("name"),
 					result.getString("description"),
 					type,
 					result.getTimestamp("date_time"),
 					result.getBoolean("read"),
 					doc);
 		}
 		else
 			return null;
 	}
 
 	public ArrayList<Alert> findByDoctor(Doctor doc) throws SQLException {
 		Connection conn = SQLDatabase.getConnection();
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT id FROM ");
 		sb.append(AlertDAO.TABLE);
 		sb.append(" WHERE for_doctor_id = ? ");
		sb.append(" AND `read` = FALSE ");
 		PreparedStatement ps = conn.prepareStatement(sb.toString());
 		
 		ps.setInt(1, doc.id);
 		
 		// Try and add it
 		ResultSet result = ps.executeQuery();
 
 		ArrayList<Alert> list = new ArrayList<Alert>();
 		
 		while (result.next()) {
 			list.add(findByPrimaryKey(result.getInt("id")));
 		}
 
 		return list;
 	}
 }
