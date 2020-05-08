 package com.isaacjg.darklight.issues;
 
 import com.ijg.darklight.sdk.core.Issue;
 import com.ijg.darklight.sdk.utils.UATools;
 
 /*
  * UserIssue - An Issue for Darklight Nova Core
  * Copyright  2013 Isaac Grant
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
  * UserIssue is an Issue for Darklight Nova Core that checks if
  * a given user has been removed or disabled
  * 
  * @author Isaac Grant
  */
 
 public class UserIssue extends Issue {
 
 	private String username;
 	
 	public UserIssue() {
 		super("User Issue", "User \"[username]\" has been disabled or removed");
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	
 	@Override
 	public boolean isFixed() {
 		return !UATools.accountActive(username) || !UATools.accountExists(username);
 	}
 
 }
