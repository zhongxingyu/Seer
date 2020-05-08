 package com.bpodgursky.hubris.universe;
 import com.google.common.collect.Lists;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.List;
 
 
 public class Star {
 	public final String name;
 	public final Integer economy;
 	public final Integer econUpgrade;
 	public final Integer ships;
 	public final Integer industry;
 	public final Integer industryUpgrade;
 	public final Integer science;
 	public final Integer scienceUpgrade;
 	public final Integer id;
 	public final Integer x;
 	public final Integer y;
 	public final Integer playerNumber;
 	public final Integer garrisonSize;
 	public final Integer resources;
   public final List<Integer> fleets;
 
 	public Star(String name, Integer playerNumber, Integer economy, Integer econUpgrade, Integer ships, Integer industry,
 			Integer industryUpgrade, Integer science, Integer scienceUpgrade, Integer id, Integer x, Integer y, Integer g, Integer resources, List<Integer> fleets){
 		this.name = name;
 		this.playerNumber = playerNumber;
 		this.economy = economy;
 		this.econUpgrade = econUpgrade;
 		this.ships = ships;
 		this.industry = industry;
 		this.industryUpgrade = industryUpgrade;
 		this.science = science;
 		this.scienceUpgrade = scienceUpgrade;
 		this.id = id;
 		this.x = x;
 		this.y = y;
 		this.garrisonSize = g;
 		this.resources = resources;
 		this.fleets = fleets;
 	}
 
   public Star(Star old, int x, int y) {
     this(old.name, old.playerNumber, old.economy, old.econUpgrade, old.ships, old.industry, old.industryUpgrade,
       old.science, old.scienceUpgrade, old.id, x, y, old.garrisonSize, old.resources, old.fleets);
   }
 
   public Star(Star old, List<Integer> fleets) {
     this(old.name, old.playerNumber, old.economy, old.econUpgrade, old.ships, old.industry, old.industryUpgrade,
       old.science, old.scienceUpgrade, old.id, old.x, old.y, old.garrisonSize, old.resources, fleets);
   }
 	
 	public String toString(){
 		
 		JSONObject json = new JSONObject();
 		try{
 			json.put("name", name);
 			json.put("playerNumber", playerNumber);
 			json.put("economy", economy);
 			json.put("econUpgrade", econUpgrade);
 			json.put("ships", ships);
 			json.put("industry", industry);
 			json.put("industryUpgrade", industryUpgrade);
 			json.put("science", science);
 			json.put("scienceUpgrade", scienceUpgrade);
 			json.put("id", id);
 			json.put("x", x);
 			json.put("y", y);
 			json.put("g", garrisonSize);
 			
 		}catch(JSONException e){
 			e.printStackTrace();
 		}
 		
 		return json.toString();
 	}
 
   public String getName() {
     return name;
   }
 
   public Integer getEconomy() {
     return economy;
   }
 
   public Integer getEconUpgrade() {
     return econUpgrade;
   }
 
   public Integer getShips() {
     return ships;
   }
 
   public Integer getIndustry() {
     return industry;
   }
 
   public Integer getIndustryUpgrade() {
     return industryUpgrade;
   }
 
   public Integer getScience() {
     return science;
   }
 
   public Integer getScienceUpgrade() {
     return scienceUpgrade;
   }
 
   public Integer getId() {
     return id;
   }
 
   public Integer getX() {
     return x;
   }
 
   public Integer getY() {
     return y;
   }
 
   public Integer getPlayerNumber() {
     return playerNumber;
   }
 
   public Integer getGarrisonSize() {
     return garrisonSize;
   }
 
   public Integer getResources() {
     return resources;
   }
 }
