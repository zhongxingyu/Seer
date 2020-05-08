 /*
  * Copyright (C) [2011]  [Pascal Knig]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, see <http://www.gnu.org/licenses/>. 
 */
 package de.sockenklaus.XmlStats.Datasource;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.sockenklaus.XmlStats.Exceptions.XmlStatsException;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class Datasource.
  */
 public abstract class Datasource {
 	
 	/**
 	 * Fetch all players.
 	 *
 	 * @return the array list
 	 */
 	public static ArrayList<String> fetchAllPlayers(){
		File[] files = new File("world/players").listFiles();
 		ArrayList<String> result = new ArrayList<String>();
 		
		for (int i = 0; i < files.length; i++){
			int whereDot = files[i].getName().lastIndexOf('.');
 			
			if (0 < whereDot && whereDot <= files[i].getName().length() - 2){
				String playerName = files[i].getName().substring(0, whereDot);
 				
				result.add(playerName);
 			}
 		}
 		
 		return result;
 	}
 	
 	public static boolean userExists(String player){
 		return fetchAllPlayers().contains(player);
 	}
 	
 	public static List<String> fetchValidUsers(List<String> list) throws XmlStatsException{
 		ArrayList<String> output = new ArrayList<String>();
 		
 		for (String possibleUser : list){
 			if(Datasource.userExists(possibleUser)) output.add(possibleUser);
 		}
 		if(output.isEmpty()) throw new XmlStatsException("No valid user has been found!");
 		else return output;
 	}
 }
