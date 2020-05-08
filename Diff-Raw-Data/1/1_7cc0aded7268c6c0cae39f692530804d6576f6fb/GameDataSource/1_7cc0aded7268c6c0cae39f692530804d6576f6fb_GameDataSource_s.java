 package edu.gatech.cs2340.group29.spacemerchant.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import edu.gatech.cs2340.group29.spacemerchant.model.*;
 
 public class GameDataSource
 {
     private static String[] ALL_COLUMNS = { "game", "difficultyLevel", "player"};
 
     private SQLiteDatabase database;
     private DatabaseHelper databaseHelper;
     private Context context;
 
     public GameDataSource(Context context)
     {
         databaseHelper = new DatabaseHelper(context);
     }
 
     public void open() throws SQLiteException
     {
         database = databaseHelper.getWritableDatabase();
     }
 
     public void close()
     {
         databaseHelper.close();
     }
 
     public void createGame(Game game)
     {
         ContentValues values = new ContentValues();
 
         int difficulty = game.getDifficulty();
         Player player  = game.getPlayer();
         
         long playerID = player.getID();
 
         values.put("player", playerID);
         values.put("difficultyLevel", difficulty);
 
         long gameID = database.insert("tb_game", null, values);
 
         game.setID(gameID);
         
         return;
     }
 
     public void deleteGame(Game game)
     {
         long gameID = game.getID();
 
         database.delete("tb_game", "game=" + gameID, null);
     }
 
     public List<Game> getGameList()
     {
         List<Game> games = new ArrayList<Game>();
 
         Cursor cursor = database.query("tb_game", ALL_COLUMNS, null, null,
                 null, null, null);
 
         cursor.moveToFirst();
         
         while( !cursor.isAfterLast() )
         {
             Game game = cursorToGame(cursor);
             
             games.add(game);
             
             cursor.moveToNext();
             
         }
     
         cursor.close();
         return games;
     }
 
     public Game getGameByID(long gameID)
     {
 
         Cursor cursor = database.query("tb_game", ALL_COLUMNS, 
                 "game=" + gameID, null, null, null, null);
 
         cursor.moveToFirst();
 
         Game game = cursorToGame(cursor);
 
         cursor.close();
         
         return game;
     }
 
     public Game cursorToGame(Cursor cursor)
     {
 
         Game game = new Game();
 
         game.setID(cursor.getLong(0));
         game.setDifficulty(cursor.getInt(1));
 
         PlayerDataSource dataSource = new PlayerDataSource(context);
         
         dataSource.open();
         Player player = dataSource.getPlayerByID(cursor.getInt(2));
         dataSource.close();
         
         game.setPlayer(player);
 
         return game;
     }
 }
