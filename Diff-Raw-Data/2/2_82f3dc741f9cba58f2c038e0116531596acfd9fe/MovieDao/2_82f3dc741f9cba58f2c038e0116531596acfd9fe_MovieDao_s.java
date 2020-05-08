 package com.github.super8.db;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteStatement;
 
 import com.github.super8.model.Movie;
 
 public class MovieDao implements Dao<Movie> {
 
   private static final String INSERT = "INSERT INTO " + MovieTable.NAME + " ("
       + MovieTable.Columns._ID + ") VALUES (?)";
 
   private SQLiteDatabase db;
   private SQLiteStatement insert;
 
   public MovieDao(SQLiteDatabase db) {
     this.db = db;
     this.insert = db.compileStatement(INSERT);
   }
 
   @Override
   public int save(Movie model) {
     insert.clearBindings();
    insert.bindLong(0, model.getTmdbId());
     insert.executeInsert();
     return model.getTmdbId();
   }
 
   @Override
   public boolean delete(Movie model) {
     int rowsDeleted = db.delete(MovieTable.NAME, MovieTable.Columns._ID + " = ?",
         new String[] { String.valueOf(model.getTmdbId()) });
     return rowsDeleted > 0;
   }
 
   @Override
   public Movie get(int id) {
     // TODO Auto-generated method stub
     return null;
   }
 
 }
