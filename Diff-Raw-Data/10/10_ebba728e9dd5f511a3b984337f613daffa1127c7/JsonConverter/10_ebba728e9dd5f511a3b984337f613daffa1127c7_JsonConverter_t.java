 package il.ac.huji.chores.server.parse;
 
 import il.ac.huji.chores.*;
 import il.ac.huji.chores.Chore.CHORE_STATUS;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 public class JsonConverter {
 
     @SuppressWarnings("deprecation")
 	public static  String whereConditionToJson(Map<String,Object> whereParams) {
 		JSONObject whereCond = new JSONObject(whereParams);
 		String whereString = whereCond.toString();
 		StringBuilder json = new StringBuilder();
 		json.append("where=");
 		json.append(URLEncoder.encode(whereString));
 		
 		return json.toString();
 	}
 
 	public static String convertChoreToJson(Chore chore){
 		JSONObject json = new JSONObject();
 		json.put("name", chore.getName());
 		json.put("status", chore.getStatus().toString());
 		SimpleDateFormat format = new SimpleDateFormat("Z");
 		json.put("startsFrom",chore.getStartsFrom().getTime());
 		json.put("deadline",chore.getDeadline().getTime());
 		json.put("coins", chore.getCoinsNum());
 		json.put("choreInfoId", chore.getChoreInfoId());
 		json.put("apartment",chore.getApartment());
 		json.put("assignedTo",chore.getAssignedTo());
 		json.put("funFact",chore.getFunFact());
 		return json.toString();
 	}
 	
 	public static String convertCoinsToJson(Coins coins){
 		JSONObject json = new JSONObject();
 		json.put("username", coins.getUsername());
 		json.put("dept",coins.getDept());
 		json.put("coinsCollected", coins.getCoinsCollected());
 		return json.toString();
 	}
 	
 	public static Chore convertJsonToChore(JSONObject obj) {
 		Chore chore = new ApartmentChore();
 		chore.setName(obj.getString("name"));
 		chore.setStatus(CHORE_STATUS.valueOf(obj.getString("status")));
 		chore.setAssignedTo(obj.getString("assignedTo"));
 		chore.setDeadline(new Date(obj.getLong("deadline")));
 		chore.setStartsFrom(new Date(obj.getLong("startsFrom")));
 		chore.setCoinsNum(obj.getInt("coins"));
 		chore.setChoreInfoId(obj.getString("choreInfoId"));
 		chore.setApartment(obj.getString("apartment"));
 		chore.setApartment(obj.getString("funFact"));
		chore.setId(obj.getString("objectId"));
 		
 		return chore;
 
 	}
 	
 	public static Coins convertJsonToCoins(JSONObject obj){
 		Coins coins = new Coins();
 		coins.setUsername(obj.getString("username"));
 		coins.setDept(obj.getInt("dept"));
 		coins.setCoinsCollected(obj.getInt("coinsCollected"));
 		coins.setId(obj.getString("objectId"));
 		return coins;
 	}
 	
 	public static RoommatesApartment convertJsonToApartment(JSONObject obj) {
 		RoommatesApartment apt = new RoommatesApartment();
 		List<String> roommates = obj.getJSONArray("Roommates").getStringList();
 		apt.setRoommates(roommates);
 		try{
 		apt.setDivisionDay(obj.getString("divisionDay"));
 		}
 		catch(JSONException e){
 			apt.setDivisionDay(null);
 		}
 		try{
 		apt.setDivisionFrequency(obj.getString("frequency"));
 		}
 		catch(JSONException e){
 			apt.setDivisionFrequency(null);
 		}
 		try{
 			apt.setLastDivision(new Date(obj.getLong("lastDivision")));
 		}
 		catch(JSONException e){
 			apt.setDivisionFrequency(null);
 		}
 		try{
 			apt.setId(obj.getString("objectId"));
 		}
 		catch(JSONException e){
 			apt.setDivisionFrequency(null);
 		}
 		apt.setName(obj.getString("apartmentName"));
 		return apt;
 	}
 
 	public static List<RoommatesApartment> convertJsonArrayToApartmentList(
 			JSONArray arr) {
 		List<RoommatesApartment> aptList = new ArrayList<RoommatesApartment>();
 		List<JSONObject> objList = arr.getObjectsList();
 		for (JSONObject obj : objList) {
 			aptList.add(convertJsonToApartment(obj));
 		}
 		return aptList;
 	}
 
 	public static List<ChoreInfo> convertJsonArrayToChoreInfoList(JSONArray arr) {
 		List<ChoreInfo> aptList = new ArrayList<ChoreInfo>();
 		List<JSONObject> objList = arr.getObjectsList();
 		for (JSONObject obj : objList) {
 			aptList.add(convertJsonToChoreInfo(obj));
 		}
 		return aptList;
 	}
 
 	public static List<Roommate> convertArrToRoommatesList(JSONArray arr) {
 		List<Roommate> aptList = new ArrayList<Roommate>();
 		List<JSONObject> objList = arr.getObjectsList();
 		for (JSONObject obj : objList) {
 			aptList.add(convertJsonToRoommate(obj));
 		}
 		return aptList;
 	}
 
 	public static Roommate convertJsonToRoommate(JSONObject obj) {
 		Roommate roommate = new Roommate();
 //		roommate.setCoinsCollected(obj.getInt("coinsCollected"));
 //        try {
 //		roommate.setDebt(obj.getInt("coins"));
 //        } catch (JSONException e) {
 //            // TODO [yl] Log it!
 //            System.err.println(String.format("JSONException: %s", e.getMessage()));
 //            roommate.setDebt(0);
 //        }
 		roommate.setId(obj.getString("objectId"));
 		roommate.setUsername(obj.getString("username"));
 		return roommate;
 	}
 
 	public static ChoreInfo convertJsonToChoreInfo(JSONObject obj) {
 		ChoreInfo choreInfo = new ChoreInfoInstance();
 		choreInfo.setChoreInfoID(obj.getString("objectId"));
 		choreInfo.setChoreInfoName(obj.getString("choreName"));
 		choreInfo.setCoins(obj.getInt("coins"));
 		choreInfo.setHowMany(obj.getInt("frequency"));
 		choreInfo.setIsEveryone(obj.getBoolean("isEveryone"));
 		choreInfo.setPeriod(obj.getString("period"));
 		return choreInfo;
 
 	}
 
 }
