 /**
  * <copyright>
  *
  * Copyright (c) 2010 http://www.big.tuwien.ac.at All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * </copyright>
  */
 package formel0api.beans;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 import formel0api.Game;
 
 /**
  * Class representing a player playing in a {@link Game}.
  */
 @ManagedBean(name="player")
 @SessionScoped
 public class Player {
 
     /**
      * History of the positions of the player
      */
     private List<Integer> history = new ArrayList<Integer>();
     /**
      * The name of this user
      */
     private String name;
     /**
      * The current position of the user's car
      */
     private int position = 0;
     
     /**
      * the password for the login
      */
     private String password = "";
     
     private String firstName="";
     
     private String lastName="";
     
     private String gender="";
     
     private String dateOfBirth="";
     
     private int lastDiceScore = 0;
     /**
      * Initializes a {@link Player} 
      *
      */
     public Player() {
         setPosition(0);
     }
 
     /**
      * Returns the name of this player.
      *
      * @return the name
      */
     public String getName() {
         return name;
     }
     
     public void setName(String name) {
     	this.name = name;
     }
     
     public String getPassword() {
     	return this.password;
     }
     
     public void setPassword(String password) {
     	this.password = password;
     }
 
     public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public String getGender() {
 		return gender;
 	}
 
 	public void setGender(String gender) {
 		this.gender = gender;
 	}
 
 	public String getDateOfBirth() {
 		return dateOfBirth;
 	}
 
 	public void setDateOfBirth(String dateOfBirth) {
 		this.dateOfBirth = dateOfBirth;
 	}
 
 	/**
      * Return the actual position of this player's car
      *
      * @return the actual position of this player's car
      */
     public int getPosition() {
         return this.position;
     }
 
     /**
      * Sets the actual position of this player's car
      *
      * @param pos actual position of this player's car
      */
     public void setPosition(int pos) {
         this.position = pos;
         history.add(new Integer(pos));
     }
     
     public void setLastDiceScore(int dice) {
     	this.lastDiceScore = dice;
     }
     
     public int getLastDiceScore() {
     	return this.lastDiceScore;
     }
 
     /**
      * Returns the position at time (now - t) (i.e., if t=0 returns the current
      * position, if t=1 returns last position, etc.)
      *
      * @param t position at time (now - t)
      */
     public int getPositionMinusT(int t) {
         int index = history.size() - 1 - t;
         if (index >= 0 && index < history.size()) {
             return history.get(index);
         }
         return -1;
     }
 }
