 /*
  * Copyright (C) 2012 RoketGamer <http://roketgamer.com> and contributors.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.roketgamer.util;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import com.roketgamer.Player;
 import com.roketgamer.RoketGamer;
 
 public class PlayerUtils {
 	
 	/**
 	 * Convert username to user ID. Returns -1 if not found or
 	 * if operation fails.
 	 * @param username
 	 * @return
 	 */
 	public static int toId(String username) {
 		try {
 			URL url = new URL(RoketGamer.SERVER_LOCATION + "/api/" + RoketGamer.VERSION + "/public/getuserid.php?user=" + username);
 			
 			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 
 			String result = in.readLine();
			if(!result.equals(null))
				return Integer.parseInt(result);
			else
				return -1;
 		} catch (Exception e) { 
 			return -1;
 		}
 	}
 	
 	/**
 	 * Convert user ID to username. Returns an empty String
 	 * if not found or if operation fails.
 	 * @param id
 	 * @return
 	 */
 	public static String toString(int id) {
 		try {
 			URL url = new URL(RoketGamer.SERVER_LOCATION + "/api/" + RoketGamer.VERSION + "/public/getusername.php?id=" + id);
 			
 			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 
 			String result = in.readLine();
			
			if(!result.equals(null))
				return result;
			else
				return "";
 		} catch (Exception e) { 
 			return "";
 		}
 	}
 	
 	/**
 	 * Get a user's number of friends.
 	 * @param username
 	 * @return
 	 */
 	public static int getFriendCount(String username) {
 		// TODO: Implement
 		return 0;
 	}
 	
 	/**
 	 * Get a user's number of friends.
 	 * @param id
 	 * @return
 	 */
 	public static int getFriendCount(int id) {
 		// TODO: Implement
 		return 0;
 	}
 	
 	/**
 	 * Get a user's number of friends.
 	 * @param player
 	 * @return
 	 */
 	public static int getFriendCount(Player player) {
 		// TODO: Implement
 		return 0;
 	}
 	
 	/**
 	 * Get URL to a user's Gravatar (profile) image.
 	 * @param username
 	 * @return
 	 */
 	public static String getGravatarUrl(String username) {
 		// TODO: Implement
 		return "";
 	}
 	
 	/**
 	 * Get URL to a user's Gravatar (profile) image.
 	 * @param id
 	 * @return
 	 */
 	public static String getGravatarUrl(int id) {
 		// TODO: Implement
 		return "";
 	}
 	
 	/**
 	 * Get URL to a user's Gravatar (profile) image.
 	 * @param player
 	 * @return
 	 */
 	public static String getGravatarUrl(Player player) {
 		// TODO: Implement
 		return "";
 	}
 }
