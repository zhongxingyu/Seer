 package com.dedaulus.cinematty.framework;
 
 import android.app.Application;
 import android.app.SearchManager;
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import com.dedaulus.cinematty.CinemattyApplication;
 import com.dedaulus.cinematty.R;
 import com.dedaulus.cinematty.framework.tools.Constants;
 import com.dedaulus.cinematty.framework.tools.DataConverter;
 
 import java.util.Map;
 
 /**
  * User: Dedaulus
  * Date: 27.02.12
  * Time: 19:53
  */
 public class SearchSuggestionsProvider extends ContentProvider {
     private static final UriMatcher uriMatcher;
     public static final String AUTHORITY = "com.dedaulus.cinematty.provider";
     private static final String TABLE_NAME = "search_suggestions";
     private static final int SS = 1;
 
     private CinemattyApplication app;
 
     static {
         uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
         uriMatcher.addURI(AUTHORITY, TABLE_NAME, SS);
     }
 
     @Override
     public boolean onCreate() {
         Application app = (Application)getContext();
         if (app == null) return false;
         this.app = (CinemattyApplication)app;
         return true;
     }
 
     @Override
     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
         MatrixCursor cursor = new MatrixCursor(new String[] {
                 BaseColumns._ID,
                 SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                 SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA,
                 SearchManager.SUGGEST_COLUMN_TEXT_1,
                 SearchManager.SUGGEST_COLUMN_TEXT_2,
                 SearchManager.SUGGEST_COLUMN_ICON_1
         });
 
         if (selectionArgs[0].length() > 1) {
             String searchString = selectionArgs[0];
            String pattern = new StringBuilder().append("(?i).* [\"«„”‘]*").append(searchString).append(".*|(?i)^[\"«„”‘]*").append(searchString).append(".*").append("|(?i).*-").append(searchString).append(".*").toString();
             int i = 0;
 
             Map<String, Cinema> cinemas = app.getSettings().getCinemas();
             for (String caption : cinemas.keySet()) {
                 if (caption.matches(pattern)) {
                     MatrixCursor.RowBuilder row = cursor.newRow();
                     row.add(i);
                     row.add(caption);
                     row.add(Constants.CINEMA_TYPE_ID);
                     row.add(caption);
                     row.add(cinemas.get(caption).getAddress());
                     row.add(R.drawable.ic_search_cinema);
                     ++i;
                 }
             }
 
             Map<String, Movie> movies = app.getSettings().getMovies();
             for (String caption : movies.keySet()) {
                 if (caption.matches(pattern)) {
                     MatrixCursor.RowBuilder row = cursor.newRow();
                     row.add(i);
                     row.add(caption);
                     row.add(Constants.MOVIE_TYPE_ID);
                     row.add(caption);
                     row.add(DataConverter.genresToString(movies.get(caption).getGenres().values()));
                     row.add(R.drawable.ic_search_movie);
                     ++i;
                 }
             }
 
             Map<String, MovieActor> actors = app.getSettings().getActors();
             for (String caption : actors.keySet()) {
                 if (caption.matches(pattern)) {
                     MatrixCursor.RowBuilder row = cursor.newRow();
                     row.add(i);
                     row.add(caption);
                     row.add(Constants.ACTOR_TYPE_ID);
                     row.add(caption);
                     row.add(null);
                     row.add(R.drawable.ic_search_actor);
                     ++i;
                 }
             }
         }
 
         return cursor;
     }
 
     @Override
     public String getType(Uri uri) {
         return null;
     }
 
     @Override
     public Uri insert(Uri uri, ContentValues contentValues) {
         return null;
     }
 
     @Override
     public int delete(Uri uri, String s, String[] strings) {
         return 0;
     }
 
     @Override
     public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
         return 0;
     }
 }
