 package com.steamedpears.comp3004.models;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public final class Asset {
 
     //prevent making this real
     private Asset(){}
 
     //resources
     public static final String ASSET_WOOD =     "w";
     public static final String ASSET_STONE =    "s";
     public static final String ASSET_CLAY =     "c";
     public static final String ASSET_ORE =      "o";
 
     //synthetics
     public static final String ASSET_LOOM =     "l";
     public static final String ASSET_PAPYRUS =  "p";
     public static final String ASSET_GLASS =    "g";
 
     //science
     public static final String ASSET_SCIENCE_1 = "tab";
     public static final String ASSET_SCIENCE_2 = "comp";
     public static final String ASSET_SCIENCE_3 = "gear";
 
     //abilities
     public static final String ASSET_DISCARD =      "discard";
     public static final String ASSET_DOUBLE_PLAY =  "lastcard";
     public static final String ASSET_BUILD_FREE =   "freeBuild";
     public static final String ASSET_GUILD_COPY =   "guildCopy";
 
     //misc
     public static final String ASSET_GOLD =             "coin";
     public static final String ASSET_VICTORY_POINTS =   "victory";
     public static final String ASSET_MILITARY_POWER =   "army";
     public static final String ASSET_MILITARY_DEFEAT =  "defeat";
     public static final String ASSET_MILITARY_VICTORY = "win";
     public static final String ASSET_WONDER_STAGES =    "wonderstages";
 
     //colors
     public static final String COLOR_RED =          "red";
     public static final String COLOR_YELLOW =       "yellow";
     public static final String COLOR_GREY =         "gray";
     public static final String COLOR_PURPLE =       "purple";
     public static final String COLOR_GREEN =        "green";
     public static final String COLOR_BROWN =        "brown";
 
     // for enumeration
     public static final String[] AssetTypes = {
             ASSET_WOOD, ASSET_STONE, ASSET_CLAY, ASSET_ORE, ASSET_LOOM, ASSET_PAPYRUS, ASSET_GLASS,
             ASSET_SCIENCE_1, ASSET_SCIENCE_2, ASSET_SCIENCE_3, ASSET_GOLD, ASSET_VICTORY_POINTS,
             ASSET_MILITARY_POWER, ASSET_WONDER_STAGES
     };
 
     //utility methods
     public static int getAsset(Map<String, Integer> map, String key){
         if(map==null){
             return 0;
         }else{
             if(map.containsKey(key)){
                 return map.get(key);
             }else{
                 return 0;
             }
         }
     }
 
     public static Map<String, Integer> convertJSONToAssetMap(JsonObject obj){
         Map<String, Integer> map = new HashMap<String, Integer>();
 
         Set<Map.Entry<String, JsonElement>> entrySet = obj.entrySet();
         for(Map.Entry<String, JsonElement> entry: entrySet){
             map.put(entry.getKey(), entry.getValue().getAsInt());
         }
 
         return map;
     }
 
     public static Set<String> convertJSArrayToSet(JsonArray arr){
         Set<String> set = new HashSet<String>();
 
         for(JsonElement entry: arr){
             set.add(entry.getAsString());
         }
 
         return set;
     }
 
     public static Map<String, Integer> convertJSONToAssetMap(JsonObject obj, String key){
         return obj.has(key) ? convertJSONToAssetMap(obj.getAsJsonObject(key)) : new HashMap<String, Integer>();
     }
 
     public static Set<String> convertJSArrayToSet(JsonObject obj, String key){
         return obj.has(key) ? convertJSArrayToSet(obj.getAsJsonArray(key)) : new HashSet<String>();
     }
 
     public static Map<String, Integer> sumAssets(Collection<Map<String, Integer>> assets){
         Map<String, Integer> result = new HashMap<String, Integer>();
         for(Map<String, Integer> assetMap: assets){
             for(String key: assetMap.keySet()){
                 if(result.containsKey(key)){
                     result.put(key, result.get(key)+assetMap.get(key));
                 }else{
                     result.put(key, assetMap.get(key));
                 }
             }
         }
 
         return result;
     }
 
     public static Map<String, Integer> sumAssets(Map<String, Integer> first, Map<String, Integer> last){
         Collection<Map<String, Integer>> col = new ArrayList<Map<String, Integer>>();
         col.add(first);
         col.add(last);
         return sumAssets(col);
     }
 
     public static Map<String, Integer> subtractAssets(Map<String, Integer> first, Map<String, Integer> last){
         Map<String, Integer> result = new HashMap<String, Integer>();
         for(String key: first.keySet()){
             int difference = first.get(key) - getAsset(last, key);
             if(difference>0){
                 result.put(key, difference);
             }
         }
         return result;
     }
 
     public static boolean existsValidChoices(List<Set<String>> choices, Map<String, Integer> cost){
         return recursiveSearchForValidChoices(choices, cost, 0);
     }
 
     private static boolean recursiveSearchForValidChoices(List<Set<String>> choices, Map<String, Integer> cost, int index){
         Set<String> choice = choices.get(index);
         //loop over every asset in this choice
         for(String asset: choice){
             //if this asset would help pay for the cost, try it
             if(cost.containsKey(asset)){
                 cost.put(asset, cost.get(asset)-1);
                 //see if we've found a solution
                 boolean foundSolution = true;
                 for(int value: cost.values()){
                     if(value>0){
                         foundSolution = false;
                         break;
                     }
                 }
                 //if that didn't solve it, recurse on the next choice
                 if(!foundSolution && index+1<choices.size()){
                     foundSolution = recursiveSearchForValidChoices(choices, cost, index+1);
                 }
                 //if we found a solution, return true; otherwise put the asset back and try the next asset in the choice
                 if(foundSolution){
                     return true;
                }else{
                    cost.put(asset, cost.get(asset)+1);
                 }
             }
         }
         //if we've gotten to here, no choice in this subtree works
         return false;
     }
 
 }
