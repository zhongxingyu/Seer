 package com.nitrikx.minecraft.bukkit.battlegrounds.team;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
 import com.nitrikx.minecraft.bukkit.battlegrounds.config.ConfigBG;
 
 /**
  * This class is a team stub. Each instance represent a team of the battleground.
  * @author NitriKx
  *
  */
 public class TeamStub implements ConfigurationSerializable{
 	
	@SuppressWarnings("unused")
 	private static Logger log = Logger.getLogger("Battlegrounds");
 	
 	private String name;
 	
 	private List<String> members;
 	
 	private List<String> admins;
 	
 	private Date creationDate;
 	
 	private String creator;
 	
 	private String color;
 	
 	
 	/**
 	 * YAML deserializer.
 	 */
 	@SuppressWarnings("unchecked")
 	public TeamStub(Map<String, Object> map){
 		this.name = (String) map.get("name");
 		this.members = (List<String>) map.get("members");
 		this.admins = (List<String>) map.get("admins");
 		this.creationDate = (Date) map.get("creationDate");
 		this.creator = (String) map.get("creator");
 		this.color = (String) map.get("color");
 	}
 	
 	/**
 	 * Create a team at the current time.
 	 * @param name The name of the team.
 	 * @param members A list of Player BG, which is the list of members (including admins)
 	 * @param admins A list of Player BG, which is the list of admins
 	 * @param creator The name of the player which create the team
 	 * @param color The color of the team
 	 */
 	public TeamStub(String name, List<String> members, List<String> admins, String creator, String color) {
 		this.name = name;
 		this.members = members;
 		this.admins = admins;
 		this.creationDate = new Date();
 		this.creator = creator;
 		this.color = color;
 	}
 	
 	/**
 	 * Create a team, just based on name
 	 * @param name
 	 */
 	public TeamStub(String name){
 		this.name = name;
 		this.members = new ArrayList<String>();
 		this.admins = new ArrayList<String>();
 		this.creationDate = new Date();
 		this.color = TeamColors.None;
 	}
 	
 	/**
 	 * Create an empty team.
 	 */
 	public TeamStub() {
 		this.name = "";
 		this.members = new ArrayList<String>();
 		this.admins = new ArrayList<String>();
 		this.creationDate = new Date();
 		this.creator = "";
 		this.color = TeamColors.None;
 	}
 	
 	/**
 	 * Add a player to the team
 	 * @param player A PlayerBG object, which represent the player to add.
 	 * @return True if player can be added
 	 */
 	public boolean addPlayer(String playerName){
 		
 		boolean canBeAdded = true;
 		
 		//If player has no team and team is not full
 		if(TeamsManager.getInstance().retrievePlayerTeam(playerName).equals("")
 				&& this.members.size() < ConfigBG.maxPlayerPerTeam){
 			this.members.add(playerName);
 		}
 		else{
 			canBeAdded = false;
 		}
 		
 		return canBeAdded;
 	}
 	
 	/**
 	 * Remove a player from the team.
 	 * @param player The name of the player.
 	 * @return True if the player is removed, False else.
 	 */
 	public boolean removePlayer(String player){
 		
 		boolean isDelete = true;
 		
 		//If the player is a member of the team
 		if(this.members.contains(player)){
 			
 			//If the player is an admin
 			if(admins.contains(player)){
 				this.admins.remove(player);
 			}
 			
 			//Remove from the members
 			this.members.remove(player);
 		}
 		else{
 			isDelete = false;
 		}
 		
 		return isDelete;
 	}
 	
 	/**
 	 * Check if a player is a member of the team
 	 * @param playerName
 	 * @return
 	 */
 	public boolean hasPlayer(String playerName){
 		boolean isMember = false;
 		
 		for(String player : this.members){
 			if(player.equalsIgnoreCase(playerName)){
 				isMember = true;
 			}
 		}
 		
 		return isMember;
 	}
 	
 	/**
 	 * Check if a player is an admin of the team
 	 * @param playerName
 	 * @return
 	 */
 	public boolean hasAdmin(String playerName){
 		boolean isAdmin = false;
 		
 		for(String admin : this.admins){
 			if(admin.equalsIgnoreCase(playerName)){
 				isAdmin = true;
 			}
 		}
 		
 		return isAdmin;
 	}
 	
 	/**
 	 * Toggle administrator status for a player.
 	 * @param playerName The name of the player
 	 * @return True if player is now admin, False if not admin.
 	 */
 	public boolean toggleAdministrator(String playerName){
 		boolean isAdmin = false;
 		
 		for(String player : this.members){
 			if(player.equalsIgnoreCase(playerName)){
 				//If player is an administrator
 				if(this.admins.contains(player)){
 					this.admins.remove(player);
 					isAdmin = false;
 				}
 				else{
 					//Set player administrator
 					this.admins.add(playerName);
 					isAdmin = true;
 				}
 				
 			}
 		}
 		return isAdmin;
 	}
 	
 	/**
 	 * YAML serializer.
 	 */
 	public final Map<String, Object> serialize() {
 		Map<String, Object> map = new HashMap<String, Object>();
 		
 		map.put("name", this.name);
 		map.put("members", this.members);
 		map.put("admins", this.admins);
 		map.put("creationDate", this.creationDate);
 		map.put("creator", this.creator);
 		map.put("color", this.color);
 		
 		return map;
 	}
 	
 	/**
 	 * YAML valueOf deserializer.
 	 * @param map
 	 * @return
 	 */
 	public static TeamStub valueOf(Map<String, Object> map){
 		return new TeamStub(map);
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public List<String> getMembers() {
 		return members;
 	}
 
 	public void setMembers(List<String> members) {
 		this.members = members;
 	}
 
 	public List<String> getAdmins() {
 		return admins;
 	}
 
 	public void setAdmins(List<String> admins) {
 		this.admins = admins;
 	}
 
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	public String getCreator() {
 		return creator;
 	}
 
 	public void setCreator(String creator) {
 		this.creator = creator;
 	}
 
 	public String getColor() {
 		return color;
 	}
 
 	public void setColor(String color) {
 		this.color = color;
 	}
 
 }
