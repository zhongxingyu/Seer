 package models;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import play.data.validation.Constraints.Required;
 
 
 public class User{
 
 	public interface All {}
 
 	@Required
 	public String username;
 	
 	public String password;
 	public String name;
 	public String alias;
 	public String primaryEmail;
 	public String alternateEmail;
 	public String personalEmail;
 	public List<Task> tasks;
 	public List<Role> roles;
 	public Date lastLogin;
 	public boolean loggedin;
 	public String supervisor;
 	public String group;
 	
 	
 	public static User findById(Connection connection, String id) {
 
 		User user =null;
 		  try{
 		  PreparedStatement ps = connection.prepareStatement("select username,name,password,supervisor,lastlogin,pri_email,alt_email,psnl_email,alias from user_dtl where username=? ");
 
 		   ps.setString(1, id);
 		 
 		  ps.execute();
 		  ResultSet rs =  ps.getResultSet();
 		  
 		 while(rs.next()){
 		     user= new User();
 			 user.username = rs.getString(1);
 			 user.name     = rs.getString(2);
 			 user.password = rs.getString(3);
 			 user.supervisor= rs.getString(4);
 			 user.lastLogin = rs.getTimestamp(5);
 			 user.primaryEmail= rs.getString(6);
 			 user.alternateEmail=rs.getString(7);
 			 user.personalEmail=rs.getString(8);
 			 user.alias=rs.getString(9);
 		 }
 		  System.out.println("executed for user select ["+user.name+"]" );
 		  ps.close();
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 		  
 		  return user;
 	}
 	
 	public static List<User> findBySupervisor(Connection connection, String id) {
 		List<User> list = new ArrayList<User>();
 		
 		  try{
 		  PreparedStatement ps = connection.prepareStatement("select username,name,password,supervisor,lastlogin,pri_email,alt_email,psnl_email,alias from user_dtl where supervisor=? ");
 
 		   ps.setString(1, id);
 		   
 		 
 		  ps.execute();
 		  ResultSet rs =  ps.getResultSet();
 		  
 		 while(rs.next()){
 			 User user = new User();
 			 user.username = rs.getString(1);
 			 user.name     = rs.getString(2);
 			 user.password = rs.getString(3);
 			 user.supervisor= rs.getString(4);
 			 user.lastLogin = rs.getTimestamp(5);
 			 user.primaryEmail= rs.getString(6);
 			 user.alternateEmail=rs.getString(7);
 			 user.personalEmail=rs.getString(8);
 			 user.alias=rs.getString(9);
 			 list.add(user);
 		 }
 		  System.out.println("executed for user select ["+id+"]" );
 		  ps.close();
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 		  
 		  return list;
 	}
 
 
 	public static List<String> findSubOrdinates(Connection connection, String id) {
 		List<String> list = new ArrayList<String>();
 		
 		  try{
 			  Statement st = connection.createStatement();
			  ResultSet rs  =	  st.executeQuery("with userlist(username) as(select suv.username from user_dtl as suv where username='"+id+"' union all select usr.username from user_dtl as usr inner join userlist as ul on usr.supervisor = ul.username where usr.supervisor is not null) select username from userlist");
 			  
 	//	  PreparedStatement ps = connection.prepareStatement("with userlist(username) as(select suv.username from user_dtl as suv where username =? union all select usr.username from user_dtl as usr inner join userlist as ul on usr.supervisor = ul.username where usr.supervisor is not null) select username from userlist");
 	//	   ps.setString(1, id);
 	//	  ResultSet rs =  ps.executeQuery();
 		  
 		 while(rs.next()){
 			 String usr = rs.getString(1);
 			 System.out.println("user for "+ id + " [ "+usr );
 			 list.add(usr);
 		 }
 		  System.out.println("executed for user-supervisor ["+id+"]" );
 		//  ps.close();
 		  st.close();
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 		  
 		  return list;
 	}
 	
 	
 	
 	public static User create(Connection connection, User user) {
 
 		
 		  try{
 		  PreparedStatement ps = connection.prepareStatement("insert into user_dtl(username,name,password,supervisor) values(?,?,?,?)");
 
 		   ps.setString(1, user.getUsername());
 		   ps.setString(2, user.getName());
 		   ps.setString(3, user.getPassword());
 		   ps.setString(4, "344613");
 		 
 		  ps.executeUpdate();
 		 
 		  System.out.println("executed for user insert ["+ user.getUsername()+"]" );
 		  ps.close();
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 		  
 		  return user;
 	}
 	
 	
 	public static User update(Connection connection, User user) {
 
 		
 		  try{
 		  PreparedStatement ps = connection.prepareStatement("update user_dtl set name=?,password=?,supervisor=?,pri_email=?,alt_email=?,psnl_email=?,alias=? where username=?");
 
 		 
 		   ps.setString(1, user.name);
 		   ps.setString(2, user.password);
 		   ps.setString(3, "344613");
 		   ps.setString(4, user.primaryEmail);
 		   ps.setString(5, user.alternateEmail);
 		   ps.setString(6, user.personalEmail);
 		   ps.setString(7, user.alias);
 		   
 		   ps.setString(8, user.username);
 		   
 		   ps.executeUpdate();
 		 
 		  System.out.println("executed for user update ["+ user.getUsername()+"]" );
 		  ps.close();
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 		  
 		  return user;
 	}
 	
 
 	public String getUsername() {
 	    return username;
 	}
 
 	public void setUsername(String username) {
 	    this.username = username;
 	}
 
 	public String getPassword() {
 	    return password;
 	}
 
 	public void setPassword(String password) {
 	    this.password = password;
 	}
 
 	public String getName() {
 	    return name;
 	}
 
 	public void setName(String name) {
 	    this.name = name;
 	}
 
 	public List<Task> getTasks() {
 	    return tasks;
 	}
 
 	public void setTasks(List<Task> tasks) {
 	    this.tasks = tasks;
 	}
 
 	public List<Role> getRoles() {
 	    return roles;
 	}
 
 	public void setRoles(List<Role> roles) {
 	    this.roles = roles;
 	}
 
 	public Date getLastLogin() {
 	    return lastLogin;
 	}
 
 	public void setLastLogin(Date lastLogin) {
 	    this.lastLogin = lastLogin;
 	}
 
 	public boolean isLoggedin() {
 	    return loggedin;
 	}
 
 	public void setLoggedin(boolean loggedin) {
 	    this.loggedin = loggedin;
 	}
 
 	public String getSupervisor() {
 	    return supervisor;
 	}
 
 	public void setSupervisor(String supervisor) {
 	    this.supervisor = supervisor;
 	}
 
 	public String getGroup() {
 	    return group;
 	}
 
 	public void setGroup(String group) {
 	    this.group = group;
 	}
 
 
 
 }
