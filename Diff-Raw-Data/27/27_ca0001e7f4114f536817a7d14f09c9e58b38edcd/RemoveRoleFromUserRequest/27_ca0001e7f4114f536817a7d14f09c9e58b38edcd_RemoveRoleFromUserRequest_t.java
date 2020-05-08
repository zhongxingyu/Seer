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
  * Remove a role from a user previously given to him.
  * 
  * @author Felix Wiemuth
  */
 public class RemoveRoleFromUserRequest extends AbstractRequest {
     private int eventID;
     private String userEmail;
     private UserRole role;
 
     /**
      * Create the request.
      * 
      * @param eventID
      *            the ID of the event where a user should lose a role
      * @param userEmail
      *            the email of the user who should lose the role
      * @param role
      *            the role to remove from the user
      */
     public RemoveRoleFromUserRequest(int eventID, String userEmail,
 	    UserRole role) {
 	this.eventID = eventID;
 	this.userEmail = userEmail;
 	this.role = role;
     }
 
    public int getEventID() {
	return eventID;
     }
 
    public String getUserEmail() {
	return userEmail;
     }
 
    public UserRole getRole() {
	return role;
     }
 }
