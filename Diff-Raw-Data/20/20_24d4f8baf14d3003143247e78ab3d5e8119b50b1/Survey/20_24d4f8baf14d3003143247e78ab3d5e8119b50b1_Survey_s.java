 package data;
 
 import java.util.ArrayList;
 
 public class Survey {
 
 	public final int id;
 	private String title;
 	public ArrayList<Question> questions;
 	public boolean completed;
 	public User user;
 	
 	/*
 	 * Survey
 	 */
 	
 	/**
 	 * Default constructor for JSP pages. SHOULD NEVER BE USED.
 	 */
 	public Survey(){
 		id=-1;
 	}
 	
 	/**
 	 * Basic constructor
 	 * @param id This Survey's unique identifier
 	 */
 	public Survey(int id){
 		this.id = id;
 	}
 	
 	public int getId(){
 		return id;
 	}
 	
 	/**
 	 * Gets the title of this Survey
 	 * @return The title
 	 */
 	public String getTitle(){
 		return title;
 	}
 	
 	/**
 	 * Sets the title of this Survey
 	 * @param title The new title of this Survey
 	 * @return TRUE if setting was successful
 	 */
 	public boolean setTitle(String title){
 		this.title = title;
 		return true;
 	}
 
 	
 	
 	/*
 	 * User
 	 */
 	
 	/**
 	 * Gets all Users connected to this Survey
 	 * @return A list of Users
 	 */
 	public ArrayList<User> users(){
 		ArrayList<User> users = Dao.users(this);
 		return users;
 	}
 	
 	/**
 	 * Adds a User to this Survey
 	 * @param user The new User
 	 * @return The new list of Users connected to this Survey
 	 */
 	public ArrayList<User> addUser(User user){
 		Dao.connectUserToSurvey(user, this);
 		return users();
 	}
 	
 	/*
 	 * Tools
 	 */
 	
 	/**
 	 * Prints the title and questions of this Survey
 	 */
 	public String toString(){
 		String result = "";
 		
 		result += "*** "+id+" - "+getTitle()+" ***<br><br>";
 		
 //		for (int i=0; i < questions.size(); i++){
 //			result += (i+1)+" - "+questions.get(i).toString(true)+"<br><br>";
 //		}
 		
 		return result;
 	}
 }
