 package com.bettername.thepokemonone;
 
 import java.util.ArrayList;
 import java.util.concurrent.*;
 import com.bettername.thepokemonone.monster.*;
 
 public class User {
 	
 	public String username;
 	public int userID;
 	public boolean isMale;
 	public ArrayList<Item> items;
 	public ArrayList<Monster> monsters;
 	
 	/* Current User Singleton + Static Methods*/
 	private static User currentUser = null;
 	private static boolean isLoadingUser = false;
 	
 	public static User currentUser() {
 		if (currentUser == null) {
 			// exec an asynchronous task
 			
 		}
 		return currentUser;
 	}
 
     public boolean isAnyoneThere() {
         for (int x = 0; x < monsters.size(); x++)
             if (monsters.get(x).isAlive())
                 return true;
 
         // Return False if the for loop completes
         return false;
     }
 	
 	/* Instance Methods + Initializer*/
 	public User() {
 	
 	}
 	
 	/*  */
 	
 }
