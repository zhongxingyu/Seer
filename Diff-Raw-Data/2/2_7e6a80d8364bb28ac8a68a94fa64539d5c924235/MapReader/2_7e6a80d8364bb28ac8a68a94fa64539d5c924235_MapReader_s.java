 package com.group7.dragonwars.engine;
 
 /* Generates a GameField based on a flat text file. Test solution. */
 
 import java.util.*;
 import org.json.*;
 
 public class MapReader {
 
     public static GameMap readMap(List<String> mapLines) throws JSONException {
         String jsonSource = "";
 
         for (String s : mapLines)
             jsonSource += s + "\n";
 
         JSONObject m = new JSONObject(jsonSource);
         String mapName = m.getString("mapName");
         Integer sizeX = m.getInt("sizeX");
         Integer sizeY = m.getInt("sizeY");
         Integer players = m.getInt("players");
 
         JSONObject fs = m.getJSONObject("fields");
         JSONObject bs = m.getJSONObject("buildings");
         JSONObject us = m.getJSONObject("units");
         JSONArray terrain = m.getJSONArray("terrain");
         JSONArray startingBuildingPos = m.getJSONArray("startingBuildingPos");
 
 
         /* Make a fake player list for now */
         List<Player> playerList = new ArrayList<Player>();
         for (Integer i = 0; i < players; ++i)
             playerList.add(new Player("Player " + i));
 
         /* Fill in a HashMap for look-up */
         HashMap<Character, JSONObject> fields = new HashMap<Character, JSONObject>();
         Iterator<?> iter = fs.keys();
 
         while (iter.hasNext()) {
             String key = (String) iter.next(); /* We have to cast ;_; */
             fields.put(key.charAt(0), fs.getJSONObject(key));
         }
 
         HashMap<Character, GameField> fieldsInfo = new HashMap<Character, GameField>();
         iter = fs.keys();
 
         while (iter.hasNext()) {
             String key = (String) iter.next();
             fieldsInfo.put(key.charAt(0), new MapReader.TerrainGetter(fields).apply(key.charAt(0)));
         }
 
         /* HashMap for buildings */
         HashMap<Character, JSONObject> buildings = new HashMap<Character, JSONObject>();
         Iterator<?> bIter = bs.keys();
 
         while (bIter.hasNext()) {
             String key = (String) bIter.next();
             buildings.put(key.charAt(0), bs.getJSONObject(key));
         }
 
         HashMap<Character, Building> buildingsInfo = new HashMap<Character, Building>();
         bIter = bs.keys();
 
         while (bIter.hasNext()) {
             String key = (String) bIter.next();
             buildingsInfo.put(key.charAt(0), new MapReader.BuildingGetter(buildings).apply(key.charAt(0)));
         }
 
         /* HashMap for units to be used for the current map throughout the game */
         HashMap<Character, Unit> units = new HashMap<Character, Unit>();
         Iterator<?> uIter = us.keys();
 
         while (uIter.hasNext()) {
             String key = (String) uIter.next();
             units.put(key.charAt(0), new MapReader.UnitGetter().apply(us.getJSONObject(key)));
         }
 
 
         List<List<GameField>> grid = MapReader.listifyJSONArray(new MapReader.TerrainGetter(fields), terrain);
         //List<List<Building>> buildingGrid = MapReader.listifyJSONArray(new MapReader.BuildingGetter(buildings), buildingPos);
 
         MapReader.setBuildings(grid, playerList, buildings, startingBuildingPos);
 
         return new GameMap(grid, units, buildingsInfo, fieldsInfo);
 
     }
 
     private static <O> List<List<O>> listifyJSONArray
     (FuncEx<Character, O, JSONException> f, JSONArray xs) throws JSONException {
         List<List<O>> v = new ArrayList<List<O>>();
         List<List<Character>> cs = new ArrayList<List<Character>>();
 
         for (Integer i = 0; i < xs.length(); i++) {
             String s = xs.getString(i);
             List<Character> t = new ArrayList<Character>();
 
             for (Integer j = 0; j < s.length(); j++)
                 t.add(s.charAt(j));
 
             cs.add(t);
         }
 
         for (List<Character> ys : cs)
             v.add(map(f, ys));
 
         return v;
     }
 
    private static void setBuildings(List<List<Gamefield>> grid, List<Player> players,
                                      HashMap<Character, Building> buildings, JSONArray posInfo) throws JSONException {
         Log.d(TAG, "Running setBuildings");
         for (Integer i = 0; i < posInfo.size(); ++i) {
             JSONObject buildingInfo = posInfo.getJSONObject(i);
             Building building = buildings.get(buildingInfo.getString("building").charAt(0));
             Integer playerOwner = buildingInfo.getInteger("owner");
             Integer posX = buildingInfo.getInteger("posX");
             Integer posX = buildingInfo.getInteger("posY");
 
             /* TODO proper choice of player */
             if (playerOwner == 0)
                 building.setOwner(new Player("Gaia"));
             else
                 building.setOwner(players.get(playerOwner - 1));
 
             GameField gf = grid.get(posY).get(posX);
             gf.setBuilding(building);
 
         }
         Log.d(TAG, "Leaving setBuildings");
 
     }
 
     private static class BuildingGetter implements FuncEx<Character, Building, JSONException> {
 
         private HashMap<Character, JSONObject> map;
 
         public BuildingGetter(HashMap<Character, JSONObject> m) {
             this.map = m;
         }
 
         public Building apply(Character c) throws JSONException {
             if (!this.map.containsKey(c))
                 return null; /* TODO throw MapException */
 
             JSONObject f = this.map.get(c);
 
             String name = f.getString("name");
             String file = f.getString("file");
             String pack = f.getString("package");
             String dir = f.getString("dir");
             Integer captureDifficulty = f.getInt("captureDifficulty");
             Double attackBonus = f.getDouble("attackBonus");
             Double defenseBonus = f.getDouble("defenseBonus");
             Boolean goalBuilding = f.getBoolean("goalBuilding");
 
             return new Building(name, captureDifficulty, attackBonus,
                                 defenseBonus, goalBuilding, file, dir, pack);
 
         }
     }
 
     private static class UnitGetter implements FuncEx<JSONObject, Unit, JSONException> {
         public Unit apply(JSONObject f) throws JSONException {
 
             String name = f.getString("name");
             String file = f.getString("file");
             String pack = f.getString("package");
             String dir = f.getString("dir");
             Boolean flying = f.getBoolean("flying");
             Double maxHealth = f.getDouble("maxHealth");
             Integer maxMovement = f.getInt("maxMovement");
             Double attack = f.getDouble("attack");
             Double meleeDefense = f.getDouble("meleeDefense");
             Double rangeDefense = f.getDouble("rangeDefense");
 
             if (f.getBoolean("ranged"))
                 return new RangedUnit(name, maxHealth, maxMovement, attack, meleeDefense,
                                       rangeDefense, f.getDouble("minRange"), f.getDouble("maxRange"),
                                       flying, file, dir, pack);
 
             return new Unit(name, maxHealth, maxMovement, attack, meleeDefense,
                             rangeDefense, flying, file, dir, pack);
 
         }
     }
 
 
     private static <I, O, E extends Exception> List<O> map(FuncEx<I, O, E> f, List<I> ls) throws E {
         List<O> os = new ArrayList<O>();
 
         for (I l : ls)
             os.add(f.apply(l));
 
         return os;
     }
 
     private static class TerrainGetter implements FuncEx<Character, GameField, JSONException> {
 
         private HashMap<Character, JSONObject> map;
 
         public TerrainGetter(HashMap<Character, JSONObject> m) {
             this.map = m;
         }
 
         public GameField apply(Character c) throws JSONException {
             if (!this.map.containsKey(c))
                 return null; /* TODO throw MapException */
 
             JSONObject f = this.map.get(c);
 
             String name = f.getString("name");
             String file = f.getString("file");
             String pack = f.getString("package");
             String dir = f.getString("dir");
             Boolean accessible = f.getBoolean("accessible");
             Boolean flightOnly = accessible ? f.getBoolean("flightOnly") : false;
             Double movementModifier = f.getDouble("movementModifier");
             Double attackModifier = f.getDouble("attackModifier");
             Double defenseModifier = f.getDouble("defenseModifier");
 
             return new GameField(name, movementModifier, attackModifier, defenseModifier,
                                  accessible, flightOnly, file, dir, pack);
         }
     }
 }
