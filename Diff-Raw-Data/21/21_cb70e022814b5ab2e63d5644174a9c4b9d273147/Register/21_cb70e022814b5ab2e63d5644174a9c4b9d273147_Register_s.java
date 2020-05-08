 package servlets;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 class Register {
 	String user;
 	String pwd;
 	String mail;
 	String www;
 	String birthday;
 	String description;
 
 	public Register(String user, String pwd, String mail,String www, String birthday, String description) {
 		this.user = user;
 		this.pwd = pwd;
 		this.mail = mail;
 		this.www= www;
 		this.birthday = birthday;
 		this.description = description;
 
 	}
 
 	
 	// It checks if the choosen username is available!
 
 	
 	boolean isValid() throws SQLException {
 		boolean result = false;
 		boolean user2 = false;
 		boolean mailb = false;
 
 		Connection connection = DriverManager.getConnection(
 				"jdbc:postgresql://10.7.20.170:5432/postgres", "ds_group3",
 				"Phie5pia");
 
 		Statement stmt = connection.createStatement();
 		ResultSet rs = stmt
 				.executeQuery("SELECT * FROM member WHERE login ='" + user
 						+ "'");
 		
 
 		String user1 = "";
 		String mail1 = "";
 
 		while (rs.next()) {
 			user1 = rs.getString("login");
 		
			
			
 		}
 
 		if (user1.length() == 0) { //It doesn't find any login with that mean -> IT IS AVAILABLE THE NAME
 
 			user2 = true;
 		}
 
 		else {
 
 			user2 = false;
 		}
 
 
 		if (mail1.length() == 0) { //Se non trova la mail -> AVAILABLE!
 
 			mailb = true;
 		}
 
 		else {
 
 			mailb = false;
 		}
 
 		if (mailb && user2) {
 			result = true;
 		}
 		return result;
 	}
 	
 	
 	public void add() throws SQLException, ParseException{
 		
 		Connection connection = DriverManager.getConnection(
 				"jdbc:postgresql://10.7.20.170:5432/postgres", "ds_group3",
 				"Phie5pia");
 		

	
 		
 		Statement stmt = connection.createStatement();
 		if (www.length() != 0 && birthday.length() != 0 && description.length() != 0){
 		java.util.Date date =  new SimpleDateFormat("yyyy-MM-dd").parse(birthday);
 	    stmt.executeUpdate("INSERT INTO member(login,member_password,mail,www,birthday,description)" + 
 		"VALUES('"+user+"','"+pwd+"','"+mail+"', '"+www+"', '"+date+"', '"+description+"')");
 	    		
 		}
 		
 		
 		if (www.length() == 0 && birthday.length()==0 && description.length()==0){
 		    stmt.executeUpdate("INSERT INTO member(login,member_password,mail)" + 
 			"VALUES('"+user+"','"+pwd+"','"+mail+"')");
 		  	}
 		
 		if (www.length() != 0 && birthday.length()==0 && description.length()==0){
 		    stmt.executeUpdate("INSERT INTO member(login,member_password,mail,www)" + 
 			"VALUES('"+user+"','"+pwd+"','"+mail+"', '"+www+"')");
 		  	}
 		
 		if (www.length() != 0 && birthday.length()!=0 && description.length()==0){
 			java.util.Date date =  new SimpleDateFormat("yyyy-MM-dd").parse(birthday);
 		    stmt.executeUpdate("INSERT INTO member(login,member_password,mail,www)" + 
 			"VALUES('"+user+"','"+pwd+"','"+mail+"', '"+www+"', '"+date+"')");
 		  	}
 	    	
 		if (www.length() == 0 && birthday.length()!=0 && description.length()!=0){
 			java.util.Date date =  new SimpleDateFormat("yyyy-MM-dd").parse(birthday);
 		    stmt.executeUpdate("INSERT INTO member(login,member_password,mail,birthday,description)" + 
 			"VALUES('"+user+"','"+pwd+"','"+mail+"', '"+date+"', '"+description+"')");
 		  	}
 		
 		
 		if (www.length() != 0 && birthday.length()==0 && description.length()!=0){
 	    stmt.executeUpdate("INSERT INTO member(login,member_password,mail,www,description)" + 
 		"VALUES('"+user+"','"+pwd+"','"+mail+"', '"+www+"', '"+description+"')");
 
 	}
 		
 
 	
 	
 	
 	}
 
 }
