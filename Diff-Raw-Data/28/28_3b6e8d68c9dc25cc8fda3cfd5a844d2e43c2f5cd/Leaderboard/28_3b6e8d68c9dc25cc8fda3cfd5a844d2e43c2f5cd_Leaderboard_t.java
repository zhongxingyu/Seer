 /*
  * Copyright (C) 2013 RoketGamer <http://roketgamer.com> and contributors.
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
 
 package com.roketgamer.leaderboard;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
import java.net.URLConnection;
 import java.util.ArrayList;
 
import com.roketgamer.Player;
 import com.roketgamer.RoketGamer;
 
 public class Leaderboard {
 	
 	private int id;
 	
 	/**
 	 * Create a new <code>Leaderboard</code>.
 	 * @param id
 	 */
 	public Leaderboard(int id) {
 		this.id = id;
 	}
 	
 	/**
 	 * Get <code>Leaderboard</code> ID.
 	 * @return
 	 */
 	public int getID() {
 		return id;
 	}
 	
 	/**
 	 * Submit a leaderboard score. Returns if operation is successful.
 	 * @param leaderboard
 	 * @param score
 	 * @return
 	 */
 	public boolean submit(int score) {
 		try {
 			URL url = new URL(RoketGamer.SERVER_LOCATION + "/api/" + RoketGamer.VERSION + "/leaderboard/submit.php?session=" + RoketGamer.getInstance().getSession().getSessionKey().trim() + "&id=" + getID() + "&score=" + score);
 			
			URLConnection connection = url.openConnection();
		    connection.addRequestProperty("Protocol", "Http/1.1");
		    connection.addRequestProperty("Connection", "keep-alive");
		    connection.addRequestProperty("Keep-Alive", "1000");
		    connection.addRequestProperty("User-Agent", "Web-Agent");

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
 			String result = in.readLine();
 			if (result.contains("true")) {
 				in.close();
 				return true;
 			} else {
 				in.close();
 				return false;
 			}
 
 		} catch (Exception e) {
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Get scores on a leaderboard.
 	 * @param entries Number of scores to return.
 	 * @return
 	 */
 	public ArrayList<LeaderboardEntry> getScores(int entries) {
 		try {
 			URL url = new URL(RoketGamer.SERVER_LOCATION + "/api/" + RoketGamer.VERSION + "/leaderboard/view.php?session=" + RoketGamer.getInstance().getSession().getSessionKey().trim() + "&id=" + getID() + "&entries=" + entries);
 			
			URLConnection connection = url.openConnection();
		    connection.addRequestProperty("Protocol", "Http/1.1");
		    connection.addRequestProperty("Connection", "keep-alive");
		    connection.addRequestProperty("Keep-Alive", "1000");
		    connection.addRequestProperty("User-Agent", "Web-Agent");

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
 			String result = in.readLine();
 			
			if (result.contains("false") || result.contains("Bad session")) {
 				in.close();
 				return new ArrayList<LeaderboardEntry>();
 			} else {
 				ArrayList<LeaderboardEntry> scores = new ArrayList<LeaderboardEntry>();
 				scores.add(new LeaderboardEntry(result, Integer.parseInt(in.readLine())));
 				
 				result = in.readLine();
 				
 				while(!result.contains("true")) {
					System.out.println(result);
 					scores.add(new LeaderboardEntry(result, Integer.parseInt(in.readLine())));
 					result = in.readLine();
 				}
 				
 				in.close();
 				return scores;
 			}
 
 		} catch (Exception e) {
			e.printStackTrace();
 		}
 		
 		return new ArrayList<LeaderboardEntry>();
 	}
 }
