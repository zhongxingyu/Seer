 package com.twoclams.hww.server;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.twoclams.hww.server.model.House;
 import com.twoclams.hww.server.model.HouseFurniture;
 import com.twoclams.hww.server.model.HouseTile;
 import com.twoclams.hww.server.model.Housewife;
 import com.twoclams.hww.server.model.Husband;
 import com.twoclams.hww.server.model.Passport;
 
 import flexjson.JSONSerializer;
 
 public class BaseController {
     private static final Log logger = LogFactory.getLog(BaseController.class);
 
     protected JSONSerializer getDefaultSerializer() {
         JSONSerializer serializer = new JSONSerializer();
         serializer.exclude("*.class");
         return serializer;
     }
 
     protected Integer[] getSkinTone(HttpServletRequest request) {
         String skinToneString = request.getParameter("skinTone");
         skinToneString = skinToneString.replace("[", "").replace("]", "");
         String[] skinToneStr = skinToneString.split(",");
         Integer[] skinTone = new Integer[3];
         for (int i = 0; i < skinToneStr.length; i++) {
             skinTone[i] = Integer.valueOf(skinToneStr[i]);
         }
         return skinTone;
     }
 
     protected Husband buildHusband(JSONObject husbandJson) throws JSONException {
         Integer salaryFactor = husbandJson.getInt("salaryFactor");
         Integer rareItemThreshold = husbandJson.getInt("rareItemThreshold");
         Integer goOnADateCost = husbandJson.getInt("goOnADateCost");
         Integer shoppingDreadValue = husbandJson.getInt("shoppingDreadValue");
         Integer workSSPReturn = husbandJson.getInt("workSSPReturn");
         Integer shoppingCounts = husbandJson.getInt("shoppingCounts");
         Integer workStressorValue = husbandJson.getInt("workStressorValue");
         Integer watchTheGameCost = husbandJson.getInt("watchTheGameCost");
         Integer outWorking = husbandJson.getInt("outWorking");
         String name = husbandJson.getString("name");
         Integer careerLevel = husbandJson.getInt("careerLevel");
         Integer totalVisits = husbandJson.getInt("totalVisits");
         Integer salary = husbandJson.getInt("salary");
         Integer workBuffTime = husbandJson.getInt("workBuffTime");
         Integer stressMeterValue = husbandJson.getInt("stressMeterValue");
         Integer loveCooldown = husbandJson.getInt("loveCooldown");
         Integer playVideoGameCost = husbandJson.getInt("playVideoGameCost");
         Integer workHours = husbandJson.getInt("workHours");
         Integer kissCost = husbandJson.getInt("kissCost");
         String papayaUserId = husbandJson.getString("papayaUserId");
         Integer citiesVisited = husbandJson.getInt("citiesVisited");
         Integer requiredVisits = husbandJson.getInt("requiredVisits");
         Integer localVisits = husbandJson.getInt("localVisits");
         Integer outShopping = husbandJson.getInt("outShopping");
         Integer stressCooldown = husbandJson.getInt("stressCooldown");
         Integer loveTankValue = husbandJson.getInt("loveTankValue");
         Integer occupation = husbandJson.getInt("occupation");
 
         return new Husband(salaryFactor, rareItemThreshold, goOnADateCost, shoppingDreadValue, workSSPReturn,
                 shoppingCounts, workStressorValue, watchTheGameCost, outWorking, name, careerLevel, totalVisits,
                 salary, workBuffTime, stressMeterValue, loveCooldown, playVideoGameCost, workHours, kissCost,
                 papayaUserId, citiesVisited, localVisits, requiredVisits, outShopping, stressCooldown, loveTankValue,
                 occupation);
     }
 
     protected Housewife buildWife(JSONObject wifeJson) throws JSONException {
         String id = wifeJson.getString("id");
         String wifeName = wifeJson.getString("name");
         if (wifeName == null) {
             wifeName = "MysteryWife";
         }
         Integer socialStatusPoints = wifeJson.getInt("socialStatusPoints");
         Housewife.Type type = Housewife.Type.valueOf(wifeJson.getString("type"));
         JSONArray jsonSkinTone = wifeJson.getJSONArray("skinTone");
         Integer[] skinTone = new Integer[3];
         for (int i = 0; i < jsonSkinTone.length(); i++) {
             skinTone[i] = jsonSkinTone.getInt(i);
         }
         Integer[] mysteryItems = new Integer[]{};
         if (wifeJson.opt("mysteryItems") != null) {
             JSONArray jsonMysteryItems = wifeJson.getJSONArray("mysteryItems");
             mysteryItems = new Integer[jsonMysteryItems.length()];
             for (int i = 0; i < jsonMysteryItems.length(); i++) {
                 mysteryItems[i] = jsonMysteryItems.getInt(i);
             }
         }
         Integer hairColor = wifeJson.getInt("hairColor");
         Integer hairStyle = wifeJson.getInt("hairStyle");
         return new Housewife(id, wifeName, socialStatusPoints, type, skinTone, hairColor, hairStyle, mysteryItems);
     }
 
     protected House buildHouse(String type, String level, Integer itemId, String furnituresJsonStr, String storageJsonStr,
             String customTilesJsonStr) {
         String[] customTiles = customTilesJsonStr.replace("[", "").replace("]", "").split("},");
         List<HouseTile> tiles = new ArrayList<HouseTile>();
         for (String customTile : customTiles) {
             if (StringUtils.isEmpty(customTile)) {
                 continue;
             }
             try {
                 JSONObject jsonTile = new JSONObject(customTile.concat("}"));
                 HouseTile tile = new HouseTile(jsonTile);
                 tiles.add(tile);
             } catch (JSONException e) {
                 logger.error("CustomTile - " + customTile, e);
             }
         }
         String[] furnitures = furnituresJsonStr.replace("[", "").replace("]", "").split("},");
         List<HouseFurniture> houseFurnitures = new ArrayList<HouseFurniture>();
         for (String furniture : furnitures) {
             if (StringUtils.isEmpty(furniture)) {
                 continue;
             }
             try {
                 JSONObject jsonFurniture = new JSONObject(furniture.concat("}"));
                 HouseFurniture houseFurniture = new HouseFurniture(jsonFurniture);
                 houseFurnitures.add(houseFurniture);
             } catch (JSONException e) {
                 logger.error("HouseFurniture - " + furniture, e);
             }
         }
         String[] storage = storageJsonStr.replace("[", "").replace("]", "").split("},");
         List<HouseFurniture> houseStorage = new ArrayList<HouseFurniture>();
         for (String storageItem : storage) {
             if (StringUtils.isEmpty(storageItem)) {
                 continue;
             }
             try {
                 JSONObject jsonStorage = new JSONObject(storageItem.concat("}"));
                 HouseFurniture houseFurniture = new HouseFurniture(jsonStorage);
                 houseStorage.add(houseFurniture);
             } catch (JSONException e) {
                 logger.error("HouseStorage - " + storageItem, e);
             }
         }
         return new House(type, Integer.valueOf(level), houseFurnitures, houseStorage, tiles, itemId);
     }
 
     protected House buildHouse(JSONObject jsonObject) throws JSONException {
         Integer level = jsonObject.getInt("level");
         String papayaUserId = jsonObject.getString("papayaUserId");
         JSONArray furnitures = jsonObject.getJSONArray("furnitures");
         List<HouseFurniture> houseFurnitures = new ArrayList<HouseFurniture>();
         List<Integer> itemIds = new ArrayList<Integer>();
         for (int i = 0; i < furnitures.length(); i++) {
             JSONObject row = furnitures.getJSONObject(i);
             try {
                 HouseFurniture furniture = new HouseFurniture(row);
                 itemIds.add(furniture.getItemId());
                 houseFurnitures.add(furniture);
             } catch (JSONException e) {
                 logger.error("Error adding furniture. ", e);
             }
         }
         JSONArray storage = jsonObject.getJSONArray("storage");
         List<HouseFurniture> houseStorage = new ArrayList<HouseFurniture>();
         for (int i = 0; i < storage.length(); i++) {
             JSONObject row = storage.getJSONObject(i);
             try {
                 HouseFurniture furniture = new HouseFurniture(row);
                 itemIds.add(furniture.getItemId());
                 houseStorage.add(furniture);
             } catch (JSONException e) {
                 logger.error("Error adding storage. ", e);
             }
         }
         List<HouseTile> tiles = new ArrayList<HouseTile>();
         JSONArray customTiles = jsonObject.getJSONArray("customTiles");
         for (int i = 0; i < customTiles.length(); i++) {
             JSONObject row = customTiles.getJSONObject(i);
             try {
                 HouseTile customTile = new HouseTile(row);
                 tiles.add(customTile);
             } catch (JSONException e) {
                 logger.error("Error adding custom tiles. ", e);
             }
         }
         String type = jsonObject.getString("type");
         Collections.sort(itemIds);
         Integer itemId = 1;
         if (!itemIds.isEmpty()) {
             itemId = itemIds.get(itemIds.size()-1);
         }
        return new House(type, level, houseFurnitures, houseStorage, tiles, papayaUserId, itemId);
     }
 
     public Passport buildPassport(JSONObject jsonPassport) throws JSONException {
         String papayaUserId = jsonPassport.getString("id");
         JSONArray buenosAiresSouvenirsJson = jsonPassport.getJSONArray("BuenosAiresSouvenirs");
         JSONArray tokyoSouvenirsJson = jsonPassport.getJSONArray("TokyoSouvenirs");
         JSONArray sydneySouvenirsJson = jsonPassport.getJSONArray("SydneySouvenirs");
         JSONArray londonSouvenirsJson = jsonPassport.getJSONArray("LondonSouvenirs");
         JSONArray parisSouvenirsJson = jsonPassport.getJSONArray("ParisSouvenirs");
         JSONArray sanFranciscoSouvenirsJson = jsonPassport.getJSONArray("SanFranciscoSouvenirs");
         JSONArray datesCompleted = jsonPassport.optJSONArray("escapedDatesCompleted");
 
         Integer tokyoFirstVisit = jsonPassport.getInt("TokyoFirstVisit");
         Integer parisFirstVisit = jsonPassport.getInt("ParisFirstVisit");
         Integer londonFirstVisit = jsonPassport.getInt("londonFirstVisit");
         Integer sanFranciscoFirstVisit = jsonPassport.getInt("SanFranciscoFirstVisit");
         Integer sydneyFirstVisit = jsonPassport.getInt("SydneyFirstVisit");
         Integer buenosAiresFirstVisit = jsonPassport.getInt("BuenosAiresFirstVisit");
         Integer citiesVisited = jsonPassport.getInt("citiesVisited");
         Passport passport = new Passport(papayaUserId, toIntegerArray(buenosAiresSouvenirsJson),
                 toIntegerArray(tokyoSouvenirsJson), toIntegerArray(sydneySouvenirsJson),
                 toIntegerArray(londonSouvenirsJson), toIntegerArray(parisSouvenirsJson),
                 toIntegerArray(sanFranciscoSouvenirsJson), toStringArray(datesCompleted), tokyoFirstVisit,
                 parisFirstVisit, londonFirstVisit, sanFranciscoFirstVisit, sydneyFirstVisit, buenosAiresFirstVisit,
                 citiesVisited);
         return passport;
     }
 
     private static Integer[] toIntegerArray(JSONArray array) throws JSONException {
         List<Integer> integers = new ArrayList<Integer>();
         for (int i = 0; i < array.length(); i++) {
             integers.add(array.getInt(i));
         }
         return integers.toArray(new Integer[] {});
     }
 
     private static String[] toStringArray(JSONArray array) throws JSONException {
         if (array == null) {
             return new String[] {};
         }
         List<String> strings = new ArrayList<String>();
         for (int i = 0; i < array.length(); i++) {
             Object obj = array.opt(i);
             if (obj != null) {
                 strings.add(obj.toString());
             } else {
                 strings.add(null);
             }
         }
         return strings.toArray(new String[] {});
     }
 }
