 package com.jpii.roketgamer;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.roketgamer.achievement.Achievement;
 import com.jpii.roketgamer.auth.APIKey;
 import com.jpii.roketgamer.auth.AuthStatus;
 import com.jpii.roketgamer.auth.Session;
 import com.jpii.roketgamer.leaderboard.Leaderboard;
 
 public class RoketGamer {
 	
 	private String version = "0.1a";
 	private String serverLocation = Constants.SERVER_LOCATION;
 	private APIKey key;
 	private Player player;
 	private AuthStatus status;
 	private Session session;
 	
 	/**
 	 * Initialize RoketGamer. Returns <code>AuthStatus</code>.
 	 * @param key
 	 * @param player
 	 * @param forceAuth
 	 * @return
 	 */
 	public AuthStatus init(APIKey key, Player player) {
 		this.key = key;
 		this.player = player;
 		
 		try {
 			URL url = new URL(serverLocation + "/api/1.0/auth/login.php?key=" + key.getKey() + "&username=" + player.getName()
 					+ "&password=" + player.getPassword().getPassword());
			System.out.println(url);
 
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 
 			String result = in.readLine();
 			System.out.println(result);
			if (result.equals("true")) {
 				session = new Session(in.readLine());
 				status = AuthStatus.GOOD;
			} else if (result.equals("false")) {
				String msg = in.readLine();
				
				if(msg.equals("Invalid API key")) {
 					status = AuthStatus.INVALID_API_KEY;
				} else if (msg.equals("Invalid user")) {
 					status = AuthStatus.BAD;
				} else if(msg.equals("API offline")) {
 					status = AuthStatus.OFFLINE;
 				}
 			} else {
 				status = AuthStatus.UNKNOWN;
 			}
 
 			in.close();
 		} catch (Exception e) { }
 		
 		return status;
 	}
 	
 	/**
 	 * Returns current <code>APIKey</code>
 	 * @return
 	 */
 	public APIKey getKey() {
 		return key;
 	}
 	
 	/**
 	 * Return current <code>Player</code>
 	 * @return
 	 */
 	public Player getPlayer() {
 		return player;
 	}
 	
 	/**
 	 * Return current <code>Session</code>
 	 * @return
 	 */
 	public Session getSession() {
 		return session;
 	}
 	
 	/**
 	 * Return current <code>AuthStatus</code>
 	 * @return
 	 */
 	public AuthStatus getStatus() {
 		return status;
 	}
 	
 	/**
 	 * Return RoketGamer version
 	 * @return
 	 */
 	public String getVersion() {
 		return version;
 	}
 	
 	/**
 	 * Return RoketGamer server location
 	 * @return
 	 */
 	public String getServerLocation() {
 		return serverLocation;
 	}
 	
 	/**
 	 * Submit a leaderboard score. Returns if operation is successful.
 	 * @param leaderboard
 	 * @param score
 	 * @return
 	 */
 	public boolean submitScore(Leaderboard leaderboard, int score) {
 		try {
 			URL url = new URL(leaderboard.getLocation() + "?session=" + session.getSessionID().trim()  + "&score=" + score);
 			
 			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 
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
 	 * Submit an achievement. Returns if operation is successful.
 	 * @param achievement
 	 * @param id
 	 * @return
 	 */
 	public boolean submitAchievement(Achievement achievement, int id) {
 		try {
 			URL url = new URL(achievement.getLocation() + "?session=" + session.getSessionID().trim() + "&id=" + id);
 
 			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 
 			String result = in.readLine();
 			if (result.contains("true")) {
 				in.close();
 				return true;
 				
 			} else {
 				in.close();
 				return false;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 }
