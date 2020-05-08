 package com.mick88.convoytrucking.player;
 
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.mick88.convoytrucking.api.entities.ApiEntity;
 import com.mick88.convoytrucking.houses.HouseEntity;
 import com.mick88.convoytrucking.player.PlayerStat.StatType;
 import com.mick88.util.TimeConverter;
 
 public class PlayerEntity extends ApiEntity
 {
 	private static final long serialVersionUID = 3826293643305147073L;
 	
 	public enum StaffType
 	{
 		none,
 		administrator,
 		moderator,
 		jr_mod,
 	}
 	
 	String name, rank, registrationDate, lastMissionDate;
 	int id, lastSeen, timeOffset, houseId, fines, wanted;
 	boolean policeBadge, vip;
 	StaffType staffType=StaffType.none;
 	String [] achievements;
 	
 	
 	int score, convoyScore, truckLoads,
 		statArtic,
 		statDumper,
 		statTanker,
 		statCement,
 		statTrash,
 		statArmored,
 		statVan,
 		statTow,
 		statCoach,
 		statLimo,
 		statArrests,
 		statGta,
 		statBurglar,
 		statHeist,
 		statPlane,
 		statHeli,
 		statFailed,
 		statOverloads,
 		statOdo,
 		statTime;
 	
 	public PlayerEntity(JSONObject json) throws JSONException
 	{
 		super(json);
 	}
 	
 	
 
 	@Override
 	public void parseJson(JSONObject json) throws JSONException
 	{
 		name = json.getString("player_name");
 		id = json.getInt("player_id");
 		rank = json.getString("rank");
 		lastSeen = json.getInt("lastseen");
 		if (json.isNull("registration_date")) registrationDate = null;
 		else registrationDate = json.getString("registration_date");
 		lastMissionDate = json.getString("last_mission_date");
 
 		if (json.isNull("staff") == false)
 		{
 			staffType = StaffType.valueOf(json.getString("staff"));
 		}
 		
 		JSONArray a = json.getJSONArray("achievements");
 		achievements = new String[a.length()];
 		for (int i=0; i < a.length(); i++)
 		{
 			achievements[i] = a.getString(i);
 		}
 		
 		vip = json.getBoolean("vip");
 		policeBadge = json.getBoolean("police_badge");
 		
 		if (json.isNull("house_id")) houseId = 0;
 		else houseId = json.getInt("house_id");
 		
 		parseStats(json.getJSONObject("stats"));
 	}
 	
 	private void parseStats(JSONObject json) throws JSONException
 	{
 		score = json.getInt("score");
 		convoyScore = json.getInt("convoy_score");
 		truckLoads = json.getInt("TRUCK_LOADS");
 		
 		statArtic = json.getInt("ARTIC");
 		statDumper = json.getInt("DUMPER");
 		statTanker = json.getInt("TANKER");
 		statCement = json.getInt("CEMENT");
 		statTrash = json.getInt("TRASH");
 		statArmored = json.getInt("ARMORED");
 		statVan = json.getInt("VAN");
 		statTow = json.getInt("TOW");
 		statCoach = json.getInt("COACH");
 		statLimo = json.getInt("LIMO");
 		statArrests = json.getInt("ARRESTS");
 		statGta = json.getInt("GTA");
 		statBurglar = json.getInt("BURGLAR");
 		statHeist = json.getInt("HEIST");
 		statPlane = json.getInt("PLANE");
 		statHeli = json.getInt("HELI");
 		statFailed = json.getInt("FAILED");
 		statOverloads = json.getInt("OVERLOADS");
 		statOdo = json.getInt("ODOMETER");
 		statTime = json.getInt("TIME");
 	}
 	
 	public PlayerStat [] getStats()
 	{
 		return new PlayerStat[]
 		{
 			new PlayerStat("Artic", statArtic),
 			new PlayerStat("Dumper", statDumper),
 			new PlayerStat("Fuel delivery", statTanker),
 			new PlayerStat("Cement", statCement),
 			new PlayerStat("Trashmaster", statTrash),
 			
 			new PlayerStat("Coach", statCoach),
 			new PlayerStat("Limousine", statLimo),
 			new PlayerStat("Arrests", statArrests),
 			
 			new PlayerStat("Vehicles stolen", statGta),
 			new PlayerStat("Burglaries", statBurglar),
 			new PlayerStat("Heists", statHeist),
 			
 			new PlayerStat("Airplane flights", statPlane),
 			new PlayerStat("Helicopter flights", statHeli),
 			
 			new PlayerStat("Armored van", statArmored),				
 			new PlayerStat("Van", statVan),
 			new PlayerStat("Towtruck", statTow),
 			
			new PlayerStat("Failed missions", statFailed),
 			new PlayerStat("Overloads", statOverloads),
 			new PlayerStat("Total distance", statOdo, StatType.Distance),
 			new PlayerStat("Time online", statTime, StatType.Time),				
 		};
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 	
 	public int getId()
 	{
 		return id;
 	}
 	
 	public StaffType getStaffType()
 	{
 		return staffType;
 	}
 	
 	public String getRank()
 	{
 		return rank;
 	}
 	
 	public int getScore()
 	{
 		return score;
 	}
 	
 	public String[] getAchievements()
 	{
 		return achievements;
 	}
 	
 	public boolean getVip()
 	{
 		return vip;
 	}
 	
 	public boolean getPoliceBadge()
 	{
 		return policeBadge;
 	}
 	
 	public int getHouseId()
 	{
 		return houseId;
 	}
 	
 	public String getRegistrationDate()
 	{
 		return registrationDate;
 	}
 	
 	public String getLastMissionDate()
 	{
 		return lastMissionDate;
 	}
 	public int getLastSeen()
 	{
 		return lastSeen;
 	}
 	
 	public int getLastSeenInterval()
 	{
 		return (int) ((new Date().getTime()/1000) - getLastSeen());
 	}
 	
 	public CharSequence getLastSeenFormat()
 	{
 		return TimeConverter.breakDownSeconds(getLastSeenInterval());
 	}
 	
 	public int getConvoyScore()
 	{
 		return convoyScore;
 	}
 	
 	public int getTruckLoads()
 	{
 		return truckLoads;
 	}
 	
 }
