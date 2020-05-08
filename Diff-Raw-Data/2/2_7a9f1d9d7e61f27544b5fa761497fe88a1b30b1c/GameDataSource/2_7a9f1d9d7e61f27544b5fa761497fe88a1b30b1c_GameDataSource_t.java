 /**
  * @author MetaGalactic Merchants
  * @version 1.0
  * 
  */
 
 package edu.gatech.cs2340.group29.spacemerchant.util;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import edu.gatech.cs2340.group29.spacemerchant.model.Game;
 import edu.gatech.cs2340.group29.spacemerchant.model.Inventory;
 import edu.gatech.cs2340.group29.spacemerchant.model.Item;
 import edu.gatech.cs2340.group29.spacemerchant.model.Planet;
 import edu.gatech.cs2340.group29.spacemerchant.model.Player;
 import edu.gatech.cs2340.group29.spacemerchant.model.Ship;
 import edu.gatech.cs2340.group29.spacemerchant.model.Universe;
 
 /**
  * The Class GameDataSource.
  */
 public class GameDataSource
 {
     private static String[] ALL_GAME_COLUMNS = { "game", "planet", "difficulty", "name", 
         "money", "pilotSkillPoints", "fighterSkillPoints", "traderSkillPoints", 
         "engineerSkillPoints", "head", "body", "legs", "feet", "fuselage", "cabin",
         "boosters"};
 
     private static String[] ALL_PLANET_COLUMNS = { "planet", "game", "techLevel",
         "resourceType", "name", "xCoord" ,"yCoord", "money", "base", "land", "cloud" };
     
 	private static String[] ALL_ITEM_COLUMNS = { "item", "game", "type", 
 		"name", "drawable" };
     
     private SQLiteDatabase  database;
     private DatabaseHelper  databaseHelper;
     private Context         context;
     
     /**
      * Instantiates a new game data source.
      *
      * @param context the Context
      */
     public GameDataSource( Context context )
     {
         this.context = context;
         databaseHelper = new DatabaseHelper( context );
     }
     
     /**
      * Open.
      *
      * @throws SQLiteException the sQ lite exception
      */
     public void open() throws SQLiteException
     {
         database = databaseHelper.getWritableDatabase();
     }
     
     /**
      * Close.
      */
     public void close()
     {
     	database.close();
         databaseHelper.close();
     }
  
     /**
      * Inserts a planet into database without associated gameID.
      * Used to store currentPlanet planet object
      * @param planet
      * @return long planetID
      */
     private long createPlanet( Planet planet )
     {
         return createPlanet(planet, -1);
     }
    
     /**
      * Inserts planet into database. 
      * @param planet
      * @param gameID
      * @return long planetID
      */
     private long createPlanet( Planet planet, long gameID )
     {
         int techLevel     = planet.getTechLevel();
         int resourceType  = planet.getResourceType();
         String planetName = planet.getName();
         int xCoord        = planet.getX();
         int yCoord        = planet.getY();
         int money  		  = planet.getMoney();
         int base		  = planet.getBase();
         int land 		  = planet.getLand();
         int cloud 		  = planet.getCloud();
         
         ContentValues values = new ContentValues();
         
         if( gameID > 0 )
         {
             values.put("game", gameID);
         }
         
         values.put("techLevel", techLevel);
         values.put("resourceType", resourceType);
         values.put("name", planetName);
         values.put("xCoord", xCoord);
         values.put("yCoord", yCoord);
         values.put("money", money);
         values.put("base", base);
         values.put("land", land);
         values.put("cloud", cloud);
         
         long planetID = database.insert( "tb_planet", null, values ); 
         
         return planetID;
     }
 
     /**
      * Inserts inventory item into SQLite database
      * @param item
      * @param gameID
      * @return long itemID
      */
     private long createItem( Item item, long gameID)
     {
         ContentValues values = new ContentValues();
         
         String itemName  = item.getName();
         int drawable     = item.getDrawable();
         int itemType     = item.getType();
         
         values.put( "game", gameID );
         values.put( "type", itemType );
         values.put( "name", itemName );
         values.put( "drawable", drawable );
             
         long itemID = database.insert( "tb_item", null, values); 
         
         return itemID;
    
     }
    
     /**
      *  Updates game in SQLite database
      */
     public long updateGame( Game game )
     {
     	long gameID = game.getGameID();
 
         //remove currentPlanet from database
         String query = "" 
         		+ "delete from tb_planet where planet in "
         	   	+ "		(select planet from tb_game where game = ? ) ";
        
         database.rawQuery( query, new String[] { Long.toString(gameID) } );
         
         //remove all saved inventory from database
         
         database.delete( "tb_item",   "game = ?" , new String[] { Long.toString(gameID) });
         	   	
         Planet currentPlanet = game.getPlanet();
         Player player        = game.getPlayer();
         Ship ship            = player.getShip();
         Inventory inventory  = player.getInventory();
         
         // insert currentPlanet into database
       
         long currentPlanetID = createPlanet( currentPlanet );
 
         //insert game, player, ship into database
         
         int money        = player.getMoney();
         int[] stats      = player.getStats();
         int head         = player.getHead();
         int body         = player.getBody();
         int legs         = player.getLegs();
         int feet         = player.getFeet();
         int fuselage     = ship.getFuselage();
         int cabin        = ship.getCabin();
         int boosters     = ship.getBoosters();
         
         ContentValues values = new ContentValues();
         
         values.put("planet",currentPlanetID);
         values.put("money",money);
         values.put("pilotSkillPoints",stats[0]);
         values.put("fighterSkillPoints",stats[1]);
         values.put("traderSkillPoints",stats[2]);
         values.put("engineerSkillPoints",stats[3]);
         values.put("head",head);
         values.put("body",body);
         values.put("legs",legs);
         values.put("feet",feet);
         values.put("fuselage",fuselage);
         values.put("cabin",cabin);
         values.put("boosters",boosters);
         
         database.update( "tb_game", values, "game = ?" , new String[] { Long.toString(gameID) });
         
         //insert inventory into database
         
         LinkedList<Item>[] inventoryItems = inventory.getContents();
         
         for( LinkedList<Item> inventoryItemsByType : inventoryItems )
         {
             
             for( Item item : inventoryItemsByType)
             {
                 createItem(item, gameID);
             }
         } 
         
         return gameID;
     }
     
     /**
      * Inserts game and associated planet, player, inventory 
      * and item objects into SQLite database
      *
      * @param Game game to be stored
      * @return long gameID
      */
     public long createGame( Game game )
     {
     	
         Planet currentPlanet = game.getPlanet();
         Universe universe    = game.getUniverse();
         Player player        = game.getPlayer();
         Ship ship            = player.getShip();
         
         // insert planet into database
       
         ContentValues values = new ContentValues();
         long currentPlanetID = createPlanet( currentPlanet );
 
         //insert game, player, ship into database
         
         int difficulty   = game.getDifficulty();
         String name      = player.getName();
         int money        = player.getMoney();
         int[] stats      = player.getStats();
         int head         = player.getHead();
         int body         = player.getBody();
         int legs         = player.getLegs();
         int feet         = player.getFeet();
         int fuselage     = ship.getFuselage();
         int cabin        = ship.getCabin();
         int boosters     = ship.getBoosters();
         
         values = new ContentValues();
         
         values.put("difficulty",difficulty);
         values.put("planet",currentPlanetID);
         values.put("name",name);
         values.put("money",money);
         values.put("pilotSkillPoints",stats[0]);
         values.put("fighterSkillPoints",stats[1]);
         values.put("traderSkillPoints",stats[2]);
         values.put("engineerSkillPoints",stats[3]);
         values.put("head",head);
         values.put("body",body);
         values.put("legs",legs);
         values.put("feet",feet);
         values.put("fuselage",fuselage);
         values.put("cabin",cabin);
         values.put("boosters",boosters);
  
         long gameID = database.insert( "tb_game", null, values );
    
         game.setID( gameID );
         
        
         //insert universe into database
         
         ArrayList<Planet> universePlanets = universe.getUniverse();
         
         for( Planet universePlanet: universePlanets )
         {
             createPlanet(universePlanet, gameID);
         }
         
         
         return gameID;
     }
     
     /**
      * Delete game.
      *
      * @param game the Game
      */
     public void deleteGame( Game game ) 
     {
         long gameID = game.getID();
         
         //remove currentPlanet from database
         String query = "" 
         		+ "delete from tb_planet where planet in "
         	   	+ "		(select planet from tb_game where game = ? ) ";
         
         database.rawQuery( query, new String[] { Long.toString(gameID) } );
         
         database.delete( "tb_game", "game=" + gameID, null );
         database.delete( "tb_item", "game=" + gameID, null );
         database.delete( "tb_planet", "game=" + gameID, null );
     }
     
     /**
      * Gets the game list.
      *
      * @return the game list
      */
     public List<Game> getGameList()
     {
         List<Game> games = new ArrayList<Game>();
         
         Cursor cursor = database.query( "tb_game", ALL_GAME_COLUMNS, null, null, null, null, null );
         
         cursor.moveToFirst();
         
         while ( !cursor.isAfterLast() )
         {
             Game game = cursorToGame( cursor );
             
             games.add(game);
             
             cursor.moveToNext();
             
         }
         
         cursor.close();
         return games;
     }
     
     /**
      * Gets the game by id.
      *
      * @param gameID the long
      * @return the game by id
      */
     public Game getGameByID( long gameID )
     {
         
         Cursor cursor = database.query( "tb_game", ALL_GAME_COLUMNS, "game=" + gameID, null, null, null, null );
         
         cursor.moveToFirst();
         
         Game game = cursorToGame( cursor );
         
         cursor.close();
         
         return game;
     }
 
     /**
      * Gets the planet by id.
      *
      * @param planetID the long
      * @return the planet by id
      */
     public Planet getPlanetByID( long planetID )
     {
         
         Cursor cursor = database.query( "tb_planet", ALL_PLANET_COLUMNS, "planet=" + planetID, null, null, null, null );
         
         cursor.moveToFirst();
         
         Planet planet = cursorToPlanet( cursor );
         
         cursor.close();
         
         return planet;
     }
     /**
      * Gets the planets by game id.
      *
      * @param gameID the long
      * @return the planets by game id
      */
     public ArrayList<Planet> getPlanetsByGameID( long gameID )
     {
         
         ArrayList<Planet> planets = new ArrayList<Planet>();
         
         Cursor cursor = database.query( "tb_planet", ALL_PLANET_COLUMNS, "game=" + gameID, 
                                         null, null, null, null );
        
         cursor.moveToFirst();
         
         while ( !cursor.isAfterLast() )
         {
             Planet planet = cursorToPlanet( cursor );
             
             planets.add( planet );
             
             cursor.moveToNext();
             
         }
         
         cursor.close();
         return planets;
     }
     
     /**
      * Cursor to game.
      *
      * @param cursor the Cursor
      * @return the game
      */
     public Game cursorToGame( Cursor cursor )
     {
         
         int gameID              = cursor.getInt(0);
         long currentPlanetID    = cursor.getInt(1);
         int difficulty          = cursor.getInt(2);
         String name             = cursor.getString(3);
         int money               = cursor.getInt(4);
         int pilotSkillPoints    = cursor.getInt(5);
         int fighterSkillPoints  = cursor.getInt(6);
         int traderSkillPoints   = cursor.getInt(7);
         int engineerSkillPoints = cursor.getInt(8);
         int head                = cursor.getInt(9);
         int body                = cursor.getInt(10); 
         int legs                = cursor.getInt(11);
         int feet                = cursor.getInt(12);
         int fuselage            = cursor.getInt(13);
         int cabin               = cursor.getInt(14);
         int boosters            = cursor.getInt(15);
       
         //instantiate all the objects that are nested in the game object
         
         Ship ship            = new Ship();
        Inventory inventory  = new Inventory( engineerSkillPoints );
         Player player        = new Player();
         Universe universe    = new Universe(difficulty, context);
         Planet currentPlanet = getPlanetByID(currentPlanetID);
         Game game            = new Game( context );
  
         //set up ship object
         
         ship.setFuselage(fuselage);
         ship.setCabin(cabin);
         ship.setBoosters(boosters);
         
         //set up inventory object
         
         ArrayList<Item> items = getItemsByGameID(gameID);
         Item[] playerInventoryItems = items.toArray(new Item[items.size()]);
         
         inventory.addAll(playerInventoryItems);
       
         //set up player object
         
         player.setName( name );
         player.setMoney( money );
 
         int[] stats = {pilotSkillPoints, fighterSkillPoints, traderSkillPoints, engineerSkillPoints};
         player.setStats( stats );
         
         player.setHead(head);
         player.setBody(body);
         player.setLegs(legs);
         player.setFeet(feet);
         
         player.setShip(ship);
         player.setInventory(inventory);
        
         //set up universe object
         
         ArrayList<Planet> planets = getPlanetsByGameID( gameID );
 
         universe.setUniverse(planets);
 
         //set up game object 
         
         game.setID( gameID );
         game.setDifficulty( difficulty );
         game.setPlayer( player );
         game.setPlanet( currentPlanet );
         game.setUniverse( universe );
        
         return game;
     }
 
     /**
      * Cursor to planet.
      *
      * @param cursor the Cursor
      * @return the planet
      */
     public Planet cursorToPlanet( Cursor cursor )
     {
         
         Planet planet = new Planet(cursor.getString(4), cursor.getInt(5), cursor.getInt(6), context );
       
         planet.setTechLevel(cursor.getInt(2));
         planet.setResourceType(cursor.getInt(3));
         planet.setMoney(cursor.getInt(7));
         planet.setBase(cursor.getInt(8));
         planet.setLand(cursor.getInt(9));
         planet.setCloud(cursor.getInt(10));
         
         return planet;
     }    
    
     /** 
      * Converts database cursor into item
      * @param cursor
      * @return Item object
      */
     private Item cursorToItem(Cursor cursor)
     {
         int type      = cursor.getInt(2);
         String name   = cursor.getString(3);
         int drawable  = cursor.getInt(4);
        
         Item item = new Item(type, name, drawable);
         
         return item;
         
     }
    
     /**
      * Gets all of the inventory items associated with a specified game
      * @param gameID
      * @return ArrayList of games
      */
     private ArrayList<Item> getItemsByGameID( long gameID )
     {
         
         ArrayList<Item> items = new ArrayList<Item>();
        
         Cursor cursor = database.query( "tb_item", ALL_ITEM_COLUMNS, "game=" + gameID, 
                                         null, null, null, null );
         
         cursor.moveToFirst();
         
         while( !cursor.isAfterLast() )
         {
             items.add((cursorToItem(cursor)));
             cursor.moveToNext();
         }
         
         cursor.close();
    
         return items;
     } 
 }
