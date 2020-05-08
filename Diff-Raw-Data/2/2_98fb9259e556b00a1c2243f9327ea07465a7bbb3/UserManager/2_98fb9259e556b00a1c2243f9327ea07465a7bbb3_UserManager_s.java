 package systemHibernateManagers;
 
 import systemHibernateEntities.User;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.hibernate.Session;
 
 import sharedHibernateResources.ConnectionManager;
 
 public class UserManager {
 	
 	//private String DBConfname;
 	private ConnectionManager conn;
 	
 	public UserManager(){
 		//this.DBConfname = DBConfname;
 		System.out.println("HELLLOOO!");
 		conn = new ConnectionManager("");
 		//conn.setDBConfname(DBConfname);
 	}
 	
 	public  ArrayList<User> getUsers(String name){
		ArrayList<User> user = (ArrayList<User>)conn.getTable("User where userName = '"+ name+"'");
 		return user;
 	}
 
 }
