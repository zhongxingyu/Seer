 package com.isaacjg.darklight.issues;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import com.ijg.darklight.sdk.core.Issue;
 
 /*
  * ConnectivityIssue - An Issue for Darklight Nova Core
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
  * ConnectivityIssue is an Issue for Darklight Nova Core that checks whether
  * or not the system has internet connectivity
  * 
  * @author Isaac Grant
  */
 
 public class ConnectivityIssue extends Issue {
 
	public ConnectivityIssue() {
 		super("Internet Connection", "Internet connectivity has been restored");
 	}
 
 	@Override
 	public boolean isFixed() {
 		try {
 			HttpURLConnection connection = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
 			connection.getContent();
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 }
