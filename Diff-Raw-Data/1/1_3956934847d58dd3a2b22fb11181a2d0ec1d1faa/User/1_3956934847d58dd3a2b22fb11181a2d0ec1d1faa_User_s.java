 /**
  * Swift Ticket -- Back End
  *
  * Copyright (C) 2013, Jonathan Gillett, Daniel Smullen, and Rayan Alfaheid
  * All rights reserved.
  *
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * User Provides methods and attributes for interacting with users,
  * which are entries representing events stored in the current user
  * accounts file.
  * 
  * @author Jonathan Gillett
  * @author Daniel Smullen
  * @author Rayan Alfaheid
  */
 public class User
 {
     private String username;
     private String type;
     private Double credit;
     private Double creditAdded;
 
 
     /**
      * Constructor for the class. Accepts and populates class attributes with values passed in.
      *      
      * @param username String for the user's name.
      * @param type String for the user's type.
      * @param credit Dollar value for the amount of credit the user has in their account.
      */
     public User(String username, String type, Double credit)
     {
         this.username = username;
         this.type = type;
         this.credit = credit;
     }
 
     /**
      * getUserName Access method for the user's name.
      * 
      * @return Returns a string containing the user's name.
      */
     public String getUsername()
     {
         return this.username;
     }
 
     /**
      * getType Access method for the user's type.
      * 
      * @return Returns a string containing the user's account type.
      */
     public String getType()
     {
         return this.type;
     }
 
     /**
      * getCredit Access method for the user's credit amount.
      * 
      * @return Returns the amount of credit the user has in their
      * account, in dollars.
      */
     public Double getCredit()
     {
         return this.credit;
     }
 
     /**
      * setCredit Mutator method, used to set the user's credit amount.
      * 
      * @param credit Dollar value to set the user's credit to.
      */
     public void setCredit(Double credit)
     {
         this.credit = credit;
     }
 
     /**
      * setCreditAdded Mutator method, used to set the amount of credit
      * that has been added to the user's account this session.
      * 
      * @param creditAdded Dollar value to set the amount of credit added
      * this session to.
      */
     public void setCreditAdded(Double creditAdded)
     {
         this.creditAdded = creditAdded;
     }
     
     /**
      * getCreditAdded Access method for the amount of credit that has
      * been added to this user's account this session.
      * 
      * @return Returns the amount of credit that has been added to the user's
      * account this session, in dollars.
      */
     public Double getCreditAdded()
     {
         return this.creditAdded;
     }
 
 }
