 package com.thundermoose.bio.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 
 import com.thundermoose.bio.auth.User;
 
 public class AuthDao {
 
 	private static final String GET_USER = "SELECT * FROM users ";
	private static final String ADD_USER = "INSERT INTO users (user_name, first_name, last_name, password_hash, active) VALUES(?,?,?,?,?)";
 	private static final String UPDATE_USER = "UPDATE users SET first_name=?, last_name=?, password_hash=?, active=? WHERE id=?";
 
 	@Autowired
 	private JdbcTemplate jdbc;
 
 	public User getUser(String username) {
 		return jdbc.queryForObject(GET_USER + "WHERE user_name = ?", new Object[] { username }, new UserMapper());
 	}
 
 	public void createUser(User u) {
 		jdbc.update(ADD_USER, u.getUsername(), u.getFirstName(), u.getLastName(), u.getPassword(), u.getActive());
 	}
 
 	public void updateUser(User u) {
 		jdbc.update(UPDATE_USER, u.getFirstName(), u.getLastName(), u.getPassword(), u.getActive(), u.getId());
 	}
 
 	private class UserMapper implements RowMapper<User> {
 
 		@Override
 		public User mapRow(ResultSet rs, int arg1) throws SQLException {
 			return new User(rs.getLong("id"), rs.getString("user_name"), rs.getString("first_name"), rs.getString("last_name"), rs.getString("password_hash"), rs.getBoolean("active"));
 		}
 
 	}
 }
