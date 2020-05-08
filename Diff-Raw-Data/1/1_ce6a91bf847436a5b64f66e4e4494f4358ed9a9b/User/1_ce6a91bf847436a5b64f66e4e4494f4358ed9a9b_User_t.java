 package com.Grateds.Reversi;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.UUID;
 import java.util.Vector;
 
 public class User {
 
 	private String Name;   // user name
 	private int Score;    // user score
 	private UUID id;       // UNIQUE user id 
 	private Vector<User> usr = new Vector<User>();
 	
 	public User(){
 		Name = "";
 		Score = 0;
 		id = null;
 	}
 	
 	public void create(String new_name, int new_score){
 		Name = new_name;
 		Score = new_score;
 		id = UUID.randomUUID();
 		writeToFile(this);
 	}
 	
 	public void delete(String id) throws IOException{
 		
 	}
 	
 	private void writeToFile(User usr){
 		FileWriter usersFile = null;
 		PrintWriter pw = null;
 		try{
 			usersFile = new FileWriter("src/main/java/com/Grateds/Reversi/users.txt",true);
 			pw = new PrintWriter(usersFile);
 			pw.print(usr.id+" ");
 			pw.print(usr.Name+" ");
 			pw.println(usr.Score);
 			usersFile.close();
 		}catch (IOException e){
 			e.getStackTrace();
 		}
 	}
 }
