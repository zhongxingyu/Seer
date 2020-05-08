 /*
  * Copyright (C) 2013 UniCoPA
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
 package unicopa.copa.base.com.request;
 
 import unicopa.copa.base.UserRole;
 
 /**
  * Appoint a role to a user for a specific event. Deputies can appoint
  * rightholders, owners can appoint rightholders and deputies.
  * 
  * @author Felix Wiemuth
  */
 public class AddRoleToUserRequest extends AbstractRequest {
     private int eventID;
     private String userEmail;
     private UserRole role;
 
     /**
      * Create the request.
      * 
      * @param eventID
      *            the ID of the event where a user should get a role
      * @param userEmail
      *            the email of the user who should get the role
      * @param role
      *            the role to give to the user
      */
     public AddRoleToUserRequest(int eventID, String userEmail, UserRole role) {
 	this.eventID = eventID;
 	this.userEmail = userEmail;
 	this.role = role;
     }
 
    public void setEventID(int eventID) {
	this.eventID = eventID;
     }
 
    public void setUserEmail(String userEmail) {
	this.userEmail = userEmail;
     }
 
    public void setRole(UserRole role) {
	this.role = role;
     }
 }
