 package local.tagavatar.server;
 
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.sql.*;
 import org.apache.commons.codec.digest.DigestUtils;
 
 import com.mysql.jdbc.PreparedStatement;
 import org.json.*;
 
 public class Users {
 	
 	private String name;
 	private String email;
 	private String bio;
 	private String url;
 	private String location;
 	private Connection con;
 	
 	public Users(){
 		Settings s=new Settings();
 		this.con=s.get_connection();
 	}
 	
 	public void set_name(String arg){
 		this.name = arg;
 	}
 	
 	public void set_email(String arg){
 		this.email = arg;
 	}
 	
 	public void set_bio(String arg){
		this.bio = arg;
 	}
 	
 	public void set_url(String arg){
 		this.url = arg;
 	}
 	
 	public void set_location(String arg){
 		this.location = arg;
 	}
 	
 	public String get_name(){
 		return this.name;
 	}
 	
 	public String get_email(){
 		return this.email;
 	}
 	
 	public String get_bio(){
 		return this.bio;
 	}
 	
 	public String get_url(){
 		return this.url;
 	}
 	
 	public String get_location(){
 		return this.location;
 	}
 	
 	public String create(String name, String email, String username, String password){
 		try{
 			MessageDigest digest = MessageDigest.getInstance("MD5");
 	        
 	        //Update input string in message digest
 	        digest.update(password.getBytes(), 0, password.length());
 	 
 	        //Converts message digest value in base 16 (hex) 
 	        password = new BigInteger(1, digest.digest()).toString(16);
 			String sql="INSERT INTO users(name,email,username,password,bio,url,location) VALUES('"+name+"','"+email+"','"+username+"','"+password+"','','','')";
 			Statement st=this.con.createStatement();
 			st.executeUpdate(sql);
 			return "User added successfully!";
 		}catch(SQLException e){
 			return e.getMessage();
 		}catch(NoSuchAlgorithmException e2){
 			return "No such algo!";
 		}
 	}
 	
 	public String view(String my_username, String username){
 		try{
 			String sql="SELECT users.name,users.email,users.bio,users.url,users.location,following.username,following.following FROM users,following WHERE (users.username=following.following AND following.following='"+username+"' AND following.username='"+my_username+"')";
 			Statement st=this.con.createStatement();
 			ResultSet rs=st.executeQuery(sql);
 			JSONObject json=new JSONObject();
 			while(rs.next()){
 				json.put("name", rs.getString("name"));
 				json.put("email", rs.getString("email"));
 				json.put("bio", rs.getString("bio"));
 				json.put("my_username", rs.getString("username"));
 				json.put("username", rs.getString("following"));
 				json.put("url", rs.getString("url"));
 				json.put("location", rs.getString("location"));
 			}
 			return json.toString();
 		}catch(Exception e){
 			return null;
 		}
 	}
 	
	public void my_info(String username){
		try{
			String sql="SELECT * FROM users WHERE username='"+username+"'";
			Statement st=this.con.createStatement();
			ResultSet rs=st.executeQuery(sql);
			while(rs.next()){
				set_name(rs.getString("name"));
				set_email(rs.getString("email"));
				set_bio(rs.getString("bio"));
				set_url(rs.getString("url"));
				set_location(rs.getString("location"));
			}
		}catch(Exception e){
			
		}
	}
	
 }
