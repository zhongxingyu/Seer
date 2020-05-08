 /**
  * @author MetaGalactic Merchants
  * @version 1.0
  * 
  */
 
 package edu.gatech.cs2340.group29.spacemerchant.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import edu.gatech.cs2340.group29.spacemerchant.model.Game;
 import edu.gatech.cs2340.group29.spacemerchant.model.Planet;
 import edu.gatech.cs2340.group29.spacemerchant.model.Player;
 import edu.gatech.cs2340.group29.spacemerchant.model.Universe;
 
 /**
  * The Class GameDataSource.
  */
 public class GameDataSource
 {
     private static String[] ALL_COLUMNS = { "game", "difficulty", "player" };
 
     private static String[] ALL_PLANET_COLUMNS = { "planet", "game", "techLevel",
                                                    "resourceType", "name", "x_coord" ,
                                                    "y_coord"
                                                  };
     
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
         databaseHelper.close();
     }
     
     /**
      * Creates the game.
      *
      * @param game the Game
      * @return the long
      */
     public long createGame( Game game )
     {
         
         Planet currentPlanet = game.getPlanet();
         Universe universe    = game.getUniverse();
         int difficulty       = game.getDifficulty();
         Player player        = game.getPlayer();
         
         // insert planet into database
         
         ContentValues values = new ContentValues();
         
         int techLevel        = currentPlanet.getTechLevel();
         int resourceType     = currentPlanet.getResourceType();
         String name          = currentPlanet.getName();
         int x_coord          = currentPlanet.getX();
         int y_coord          = currentPlanet.getY();
        
         values.put("techLevel", techLevel);
         values.put("resourceType", resourceType);
         values.put("name", name);
         values.put("x_coord", x_coord);
         values.put("y_coord", y_coord);
         
         long currentPlanetID = database.insert( "tb_planet", null, values ); 
         
        
         //insert player into database
         
         PlayerDataSource playerDataSource = new PlayerDataSource( context );
         
         playerDataSource.open();
         long playerID = playerDataSource.createPlayer( player );
         playerDataSource.close();
        
         //insert game into database
         
         values = new ContentValues();
         
         values.put( "player", playerID );
         values.put( "difficulty", difficulty );
         values.put( "planet", currentPlanetID );
         
         long gameID = database.insert( "tb_game", null, values );
    
         game.setID( gameID );
         
         //insert universe into database
         
         ArrayList<Planet> universePlanets = universe.getUniverse();
         
         for( Planet universePlanet: universePlanets )
         {
             values = new ContentValues();
             
            values.put("game", (int)gameID);
             values.put("techLevel", universePlanet.getTechLevel());
             values.put("resourceType", universePlanet.getResourceType());
             values.put("name", universePlanet.getName());
             values.put("x_coord", universePlanet.getX());
             values.put("y_coord", universePlanet.getY());
             
             database.insert( "tb_planet", null, values ); 
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
         
         database.delete( "tb_game", "game=" + gameID, null );
     }
     
     /**
      * Gets the game list.
      *
      * @return the game list
      */
     public List<Game> getGameList()
     {
         List<Game> games = new ArrayList<Game>();
         
         Cursor cursor = database.query( "tb_game", ALL_COLUMNS, null, null, null, null, null );
         
         cursor.moveToFirst();
         
         while ( !cursor.isAfterLast() )
         {
             Game game = cursorToGame( cursor );
             
             games.add( game );
             
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
         
         Cursor cursor = database.query( "tb_game", ALL_COLUMNS, "game=" + gameID, null, null, null, null );
         
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
         
         int gameID             = cursor.getInt(0);
         int difficulty         = cursor.getInt(1);
         long playerID          = cursor.getInt(2); 
         long currentPlanetID   = cursor.getInt(3);
             
         Planet currentPlanet = getPlanetByID(currentPlanetID);
         
         Universe universe = new Universe(difficulty, context);
     
         ArrayList<Planet> planets = getPlanetsByGameID( gameID );
 
         universe.setUniverse(planets);
         
         PlayerDataSource dataSource = new PlayerDataSource( context );
         
         dataSource.open();
         Player player = dataSource.getPlayerByID( playerID );
         dataSource.close();
         
         Game game = new Game( context );
         
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
         
         Planet planet = new Planet(cursor.getString(4), cursor.getInt(5), cursor.getInt(6) );
       
         planet.setTechLevel( cursor.getInt(2) );
         planet.setResourceType( cursor.getInt(3) );
         
         return planet;
     }
 }
