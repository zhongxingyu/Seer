 package ycp.edu.seniordesign.model;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class Admin {
 	private int id;
 	private String username;
 	private String hashedPassword;
 	private String salt;
 	
 	public Admin(){
 		
 	}
 	
 	public Admin(int id, String username, String hashedPassword, String salt){
 		this.id = id;
 		this.username = username;
 		this.hashedPassword = hashedPassword;
 		this.salt = salt;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getHashedPassword() {
 		return hashedPassword;
 	}
 
 	public void setHashedPassword(String hashedPassword) {
 		this.hashedPassword = hashedPassword;
 	}
 
 	public String getSalt() {
 		return salt;
 	}
 
 	public void setSalt(String salt) {
 		this.salt = salt;
 	}
 	
 	public void loadFrom(ResultSet resultSet) throws SQLException{
 		int index = 1;
 		id = resultSet.getInt(index++);
 		username = resultSet.getString(index++);
 		hashedPassword = resultSet.getString(index++);
 		salt = resultSet.getString(index++);
 	}
 	
 	public void storeTo(PreparedStatement statement) throws SQLException{
 		int index = 1;
 		statement.setInt(index++, id);
 		statement.setString(index++, username);
 		statement.setString(index++, hashedPassword);
 		statement.setString(index++, salt);
 	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		Admin other = (Admin) obj;
		return id == other.id
			&& username.equals(other.username)
			&& hashedPassword.equals(other.hashedPassword)
			&& salt.equals(other.salt);
	}
 }
