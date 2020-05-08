 /*
  *    Copyright 2012 Michael Potter
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 /*
  * RecipeData.java - Database access layer and import/export functionality. 
  */
 
 package net.potterpcs.recipebook;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.os.Environment;
 import android.text.TextUtils;
 
 public class RecipeData {
 
 //	private static final String TAG = "RecipeData";
 	
 	// Database version and filename
 	static final int DB_VERSION = 9;
 	static final String DB_FILENAME = "recipebook.db";
 	
 	// Definitions for the recipe table
 	static final String RECIPES_TABLE = "recipes";
 	public static final String RT_ID = "_id";
 	public static final String RT_NAME = "name";
 	public static final String RT_DESCRIPTION = "description";
 	public static final String RT_RATING = "rating";
 	public static final String RT_CREATOR = "creator";
 	public static final String RT_DATE = "date";
 	public static final String RT_SERVING = "serving";
 	public static final String RT_TIME = "time";
 	public static final String RT_PHOTO = "photo";
 	public static final String[] RECIPES_FIELDS = { RT_ID, RT_NAME,
 			RT_DESCRIPTION, RT_RATING, RT_CREATOR, RT_DATE, RT_SERVING, RT_TIME, RT_PHOTO };
 	
 	// Definitions for the ingredients table
 	// TODO Add ingredient photos?
 	static final String INGREDIENTS_TABLE = "ingredients";
 	public static final String IT_ID = "_id";
 	public static final String IT_NAME = "name";
 	public static final String IT_RECIPE_ID = "recipe_id";
 	public static final String[] INGREDIENTS_FIELDS = { IT_ID, IT_NAME };
 	
 	// Definitions for the directions table
 	static final String DIRECTIONS_TABLE = "directions";
 	public static final String DT_ID = "_id";
 	public static final String DT_STEP = "step";
 	public static final String DT_SEQUENCE = "sequence";
 	public static final String DT_PHOTO = "photo";
 	public static final String DT_RECIPE_ID = "recipe_id";
 	public static final String[] DIRECTIONS_FIELDS = { DT_ID, DT_STEP, DT_SEQUENCE, DT_PHOTO };
 	
 	// Definitions for the tags table
 	static final String TAGS_TABLE = "tags";
 	public static final String TT_ID = "_id";
 	public static final String TT_TAG = "tag";
 	public static final String TT_RECIPE_ID = "recipe_id";
 	public static final String[] TAGS_FIELDS = { TT_ID, TT_TAG };
 	
 	// Definitions for the cache table
 	static final String CACHE_TABLE = "imagecache";
 	public static final String CT_ID = "_id";
 	public static final String CT_URI = "uri";
 	public static final String CT_CACHED = "cached";
 	public static final String[] CACHE_FIELDS = { CT_URI, CT_CACHED };
 	
 	public static class Recipe {
 		// These don't really need to be private, because it's just a data class
 		long id;
 		String name;
 		String description;
 		float rating;
 		String creator;
 		String date;
 		int serving;
 		int time;
 		String[] ingredients;
 		String[] directions;
 		String[] directions_photos;
 		String[] tags;
 		String photo;
 		
 		// convert the Recipe to plain text for sharing
 		// takes a Context so that it can load resource strings
 		public String toText(Context ctx) {
 			StringBuilder sb = new StringBuilder();
 			Resources res = ctx.getResources();
 			sb.append(name + ' ' + res.getString(R.string.rvby) + ' ' + creator + '\n');
 			sb.append(description + '\n');
 			sb.append(res.getString(R.string.rvserves) + ' ');
 			sb.append(serving);
 			sb.append('\n' + res.getString(R.string.recipetime) + ' ');
 			sb.append(String.format(res.getString(R.string.minutesecond), time/60, time%60));
 			sb.append("\n\n" + res.getString(R.string.ingredientstext) + '\n');
 			for (String ing : ingredients) {
 				sb.append("* " + ing + '\n');
 			}
 			sb.append('\n' + res.getString(R.string.directionstext) + '\n');
 			
 			// we need to iterate by index to preserve step numbers
 			for (int i = 0; i < directions.length; ++i) {
 				// array indices are 0-based, recipes are 1-based
 				sb.append(i+1);
 				sb.append(". " + directions[i] + '\n');
 			}
 			return sb.toString();
 		}
 		
 		// convert the Recipe to JSON for exporting 
 		public String toJSONString() {
 			return toJSON().toString();
 		}
 		
 		// create a JSON object from a Recipe
 		public JSONObject toJSON() {
 			JSONObject jo = new JSONObject();
 			try {
 				jo.put(RT_ID, id);
 				jo.put(RT_NAME, name);
 				jo.put(RT_DESCRIPTION, description);
 				jo.put(RT_RATING, rating);
 				jo.put(RT_CREATOR, creator);
 				jo.put(RT_DATE, date);
 				jo.put(RT_SERVING, serving);
 				jo.put(RT_TIME, time);
 				jo.put(RT_PHOTO, photo);
 				
 				JSONArray ji = new JSONArray();
 				for (int i = 0; i < ingredients.length; i++) {
 					ji.put(ingredients[i]);
 				}
 				jo.put(INGREDIENTS_TABLE, ji);
 				
 				JSONArray jd = new JSONArray();
 				for (int d = 0; d < directions.length; d++) {
 					JSONObject diro = new JSONObject();
 					diro.put(DT_STEP, directions[d]);
 					diro.put(DT_PHOTO, directions_photos[d]);
 					jd.put(diro);
 				}
 				jo.put(DIRECTIONS_TABLE, jd);
 				
 				JSONArray jt = new JSONArray();
 				for (int t = 0; t < tags.length; t++) {
 					jt.put(tags[t]);
 				}
 				jo.put(TAGS_TABLE, jt);
 				
 			} catch (JSONException e) {
 //				e.printStackTrace();
 			}
 			return jo;
 		}
 		
 		// Create a Recipe object from a JSON string
 		public static Recipe parseJSON(String json) {
 			try {
 				return Recipe.parseJSON(new JSONObject(json));
 			} catch (JSONException e) {
 //				Log.i(TAG, e.toString());
 				return null;
 			}
 		}
 			
 		public static Recipe parseJSON(JSONObject jo) {
 			Recipe r = new Recipe();
 			// Basic fields
 			r.id = jo.optLong(RT_ID);
 			r.name = jo.optString(RT_NAME);
 			r.description = jo.optString(RT_DESCRIPTION);
 			r.rating = (float) jo.optDouble(RT_RATING);
 			r.creator = jo.optString(RT_CREATOR);
 			r.date = jo.optString(RT_DATE);
 			r.serving = jo.optInt(RT_SERVING);
 			r.time = jo.optInt(RT_TIME);
 			r.photo = jo.optString(RT_PHOTO);
 			
 			// Remove local links from imported recipes
 			// TODO sharing, etc.
 			if (!r.photo.startsWith("http")) {
 				r.photo = "";
 			}
 
 			JSONArray ji = jo.optJSONArray(INGREDIENTS_TABLE);
 			JSONArray jd = jo.optJSONArray(DIRECTIONS_TABLE);
 			JSONArray jt = jo.optJSONArray(TAGS_TABLE);
 
 			// Ingredients
 			if (ji != null) {
 				r.ingredients = new String[ji.length()];
 				for (int i = 0; i < r.ingredients.length; i++) {
 					r.ingredients[i] = ji.optString(i);
 				}
 			}
 
 			// Directions (remember that these are ordered!)
 			if (jd != null) {
 				r.directions = new String[jd.length()];
 				r.directions_photos = new String[jd.length()];
 				for (int d = 0; d < r.directions.length; d++) {
 					JSONObject diro = jd.optJSONObject(d);
 					if (diro != null) {
 						r.directions[d] = diro.optString(DT_STEP);
 						r.directions_photos[d] = diro.optString(DT_PHOTO);
 						
 						// remove local links
 						// TODO sharing, etc.
 						if (!r.directions_photos[d].startsWith("http")) {
 							r.directions_photos[d] = "";
 						}
 					} else {
 						r.directions[d] = jd.optString(d);
 						r.directions_photos[d] = null;
 					}
 				}
 			}
 
 			// Tags
 			if (jt != null) {
 				r.tags = new String[jt.length()];
 				for (int t = 0; t < r.tags.length; t++) {
 					r.tags[t] = jt.optString(t);
 				}
 			}
 			return r;
 		}
 	}
 	
 	class DbHelper extends SQLiteOpenHelper {
 		private static final String RT_FOREIGN_KEY = " integer references " + RECIPES_TABLE + "("
 							+ RT_ID + ") deferrable initially deferred";
 		private Context app;
 
 		public DbHelper(Context context) {
 			super(context, DB_FILENAME, null, DB_VERSION);
 			app = context;
 		}
 		
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 //			Log.i(TAG, "Creating database: " + DB_FILENAME);
 			
 			createRecipeTable(db);
 			createSecondaryTables(db);
 			createCacheTable(db);
 			
 			// Import starter recipes
 			// TODO Make actual starter recipes, instead of tutorials
 			InputStream is = null;
 			try {
 				is = app.getResources().openRawResource(R.raw.starter);
 				byte[] buffer = new byte[is.available()];
 				is.read(buffer);
 				JSONArray ja = new JSONArray(new String(buffer));
 				for (int i = 0; i < ja.length(); i++) {
 					Recipe r = Recipe.parseJSON(ja.getJSONObject(i));
 		
 					ContentValues values = createRecipeForInsert(r);
 					long rowid = db.insertWithOnConflict(RECIPES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
 					
 					if (r.ingredients != null) {
 						for (String ing : r.ingredients) {
 							ContentValues cvi = createIngredientsCV(rowid, ing);
 							db.insertWithOnConflict(INGREDIENTS_TABLE, null, cvi, SQLiteDatabase.CONFLICT_IGNORE);
 						}
 					}
 
 					if (r.directions != null) {
 						int step = 1;
 						for (String dir : r.directions) {
 							ContentValues cdirs = 
 								createDirectionsCV(rowid, step, dir, r.directions_photos[step-1]);
 							db.insertWithOnConflict(DIRECTIONS_TABLE, null, cdirs, SQLiteDatabase.CONFLICT_IGNORE);
 							step++;
 						}
 					}
 
 					if (r.tags != null) {
 						for (String tag : r.tags) {
 							ContentValues ctags = createTagsCV(rowid, tag);
 							db.insertWithOnConflict(TAGS_TABLE, null, ctags, SQLiteDatabase.CONFLICT_IGNORE);
 						}
 					}
 				}
 			} catch (IOException e) {
 //				Log.e(TAG, e.toString());
 			} catch (JSONException e) {
 				// TODO handle and try to continue
 //				Log.e(TAG, e.toString());
 			} finally {
 				if (is != null) {
 					try {
 						is.close();
 					} catch (IOException e) {
 //						Log.e(TAG, e.toString());
 					}
 				}
 			}
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			if (db.isReadOnly()) {
 				db = SQLiteDatabase.openDatabase(db.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
 //				Log.i(TAG, "Successfully opened read-write database");
 			} else {
 				if (oldVersion <= 8) {
 					createCacheTable(db);
 				}
 				if (oldVersion <= 7) {
 					db.execSQL("alter table " + DIRECTIONS_TABLE + " add column " + DT_PHOTO + " text");
 				} else if (oldVersion < 7) {
 					db.beginTransaction();
 					db.execSQL("drop table " + INGREDIENTS_TABLE);
 					db.execSQL("drop table " + DIRECTIONS_TABLE);
 					db.execSQL("drop table " + TAGS_TABLE);
 					db.execSQL("drop table " + RECIPES_TABLE);
 					this.onCreate(db);
 					db.endTransaction();
 				}
 			}
 		}
 
 		private void createRecipeTable(SQLiteDatabase db) {
 			db.execSQL("create table " + RECIPES_TABLE + " (" 
 					+ RT_ID + " integer primary key, "
 					+ RT_NAME + " text, " 
 					+ RT_DESCRIPTION + " text, " 
 					+ RT_RATING + " real, "
 					+ RT_CREATOR + " text, " 
 					+ RT_DATE + " text, " 
 					+ RT_SERVING + " integer, " 
 					+ RT_PHOTO + " text, " 
 					+ RT_TIME + " integer, "
 					+ createUnique(RT_NAME, RT_DESCRIPTION) + ")");
 		}
 		
 		private void createSecondaryTables(SQLiteDatabase db) {
 			db.execSQL("create table " + INGREDIENTS_TABLE + " (" 
 					+ IT_ID + " integer primary key, " 
 					+ IT_NAME + " text, " 
 					+ IT_RECIPE_ID + RT_FOREIGN_KEY + ")");
 			
 			db.execSQL("create table " + DIRECTIONS_TABLE + " (" 
 					+ DT_ID + " integer primary key, "
 					+ DT_STEP + " text, " 
 					+ DT_SEQUENCE + " int, " 
 					+ DT_PHOTO + " text, "
 					+ DT_RECIPE_ID + RT_FOREIGN_KEY + ")");
 			
 			db.execSQL("create table " + TAGS_TABLE + " (" 
 					+ TT_ID + " integer primary key, "
 					+ TT_TAG + " text, " 
 					+ TT_RECIPE_ID + RT_FOREIGN_KEY + ")");
 		}
 		
 		private void createCacheTable(SQLiteDatabase db) {
 			db.execSQL("create table " + CACHE_TABLE + " ("
 					+ CT_ID + " integer primary key, "
 					+ CT_URI + " text, "
 					+ CT_CACHED + ")");
 		}
 
 		private String createUnique(String... args) {
 			return "unique (" + TextUtils.join(",", args) + ")";
 		}
 	}
 	
 	private final DbHelper dbHelper;
 
 	public RecipeData(Context context) {
 		dbHelper = new DbHelper(context);
 //		Log.i(TAG, "Initialized database version " + dbHelper.getReadableDatabase().getVersion());
 	}
 	
 	public void close() {
 		dbHelper.close();
 	}
 	
 	private Cursor queryBuilder(String selection, String[] selectionArgs, String sortBy) {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		String outerJoin = createOuterJoin(RECIPES_TABLE, TAGS_TABLE, RT_ID, TT_RECIPE_ID);
 		String[] fields = RECIPES_FIELDS;
 		fields[0] = RECIPES_TABLE + "." + RT_ID;
 		return db.query(outerJoin, fields, selection, selectionArgs, fields[0], null, sortBy);
 	}
 	
 	public Cursor query(String search, String tag, int min, int max, String sortBy) {
 		// this is the "multi-query" method, that searches by name, tag, and time
 //		Log.i(TAG, "multi-query: search = " + search + ", tag = " + tag + ", time = " + min + " to " + max + ", sort by = " + sortBy);
 		String like = createLikePattern();
 		String time = createTimeComparisonPattern();
 		String searchPart = "(" + like + ")";
 		String timePart = "(" + time + ")";
 		String tagPart = "(" + TT_TAG + " = ?)";
 		// We have to match like this because of Android bug 3153
 		String match = "%" + search + "%";
 				
 		ArrayList<String> parts = new ArrayList<String>();
 		ArrayList<String> args = new ArrayList<String>();
 		if (search != null) {
 			parts.add(searchPart);
 			args.add(match);
 			args.add(match);
 		}
 		if (!(min == 0 && max == 0)) {
 			parts.add(timePart);
 			args.add(Integer.toString(max));
 			args.add(Integer.toString(min));
 		}
 		if (tag != null) {
 			parts.add(tagPart);
 			args.add(tag);
 		}
 		String selection = TextUtils.join(" and ", parts);	
 		String[] selectionArgs = (String[]) args.toArray(new String[args.size()]);
 //		Log.i(TAG, selection + ", " + selectionArgs + ", " + sortBy);
		return queryBuilder(selection, selectionArgs, sortBy);
 	}
 	
 	private String createInnerJoin(String left, String right, String left_id, String right_id) {
 		String table1id = left + "." + left_id;
 		String table2id = right + "." + right_id;
 		return left + " inner join " + right + " on " + table1id + " = " + table2id;
 	}
 	
 	private String createOuterJoin(String left, String right, String left_id, String right_id) {
 		String table1id = left + "." + left_id;
 		String table2id = right + "." + right_id;
 		return left + " left outer join " + right + " on " + table1id + " = " + table2id;
 	}
 	
 	private String createLikePattern() {
 		// See above re: query bug #3153
 //		return RT_NAME + " like '%" + strings[0] + "%' or " + RT_DESCRIPTION + " like '%" + strings[1] + "%'";
 		return RT_NAME + " like ? or " + RT_DESCRIPTION + " like ?";
 	}
 	
 	private String createTimeComparisonPattern() {
 		return RT_TIME + " <= ? and " + RT_TIME + " >= ?";
 	}
 	
 	public long getLastInsertRecipeId() {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		Cursor c = db.rawQuery("select max(_id) from " + RECIPES_TABLE, null);
 		c.moveToFirst();
 		long id = c.getLong(0);
 		c.close();
 		return id;
 	}
 	
 	public long getNumberOfRecipes() {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		Cursor c = db.query(RECIPES_TABLE, new String[] { "_id" }, null, null, null, null, null);
 		long num = c.getCount();
 		c.close();
 		return num;
 	}
 	
 	public Cursor getAllRecipes(String sortBy) {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		return db.query(RECIPES_TABLE, RECIPES_FIELDS, null, null, null,
 				null, sortBy);
 	}
 	
 	public Cursor getAllRecipes() {
 		return getAllRecipes(RT_NAME);
 	}
 	
 	public Cursor getAllRecipes(String[] columns, String sortBy) {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		return db.query(RECIPES_TABLE, columns, null, null, null, null, sortBy);
 	}
 	
 	public Cursor getAllRecipes(String[] columns) {
 		return getAllRecipes(columns, RT_NAME);
 	}
 	
 	public Cursor getMatchingRecipes(String match, String sortBy) {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		// We have to match like this because of Android bug 3153
 		String fixed = "%" + match + "%";
 		return db.query(RECIPES_TABLE, RECIPES_FIELDS, createLikePattern(), new String[] { fixed, fixed }, null,
 				null, sortBy);
 	}
 	
 	public Cursor getMatchingRecipes(String match) {
 		return getMatchingRecipes(match, RT_NAME);
 	}
 	
 	public Cursor getMatchingRecipesByTime(int max, int min, String sortBy) {
 		String[] times = { Integer.toString(max), Integer.toString(min) };
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		return db.query(RECIPES_TABLE, RECIPES_FIELDS, createTimeComparisonPattern(), times, null, null, sortBy);
 	}
 	
 	public Cursor getMatchingRecipesByTime(int max, int min) {
 		return getMatchingRecipesByTime(max, min, RT_NAME);
 	}
 	
 	public Cursor getSingleRecipe(long rid) {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		return db.query(RECIPES_TABLE, RECIPES_FIELDS, RT_ID + " = ?", 
 				new String[] { Long.toString(rid) }, null, null, null);
 	}
 	
 	public Recipe getSingleRecipeObject(long rid) {
 		Recipe r = new Recipe();
 		Cursor c = getSingleRecipe(rid);
 		c.moveToFirst();
 		r.id = c.getLong(c.getColumnIndex(RT_ID));
 		r.name = c.getString(c.getColumnIndex(RT_NAME));
 		r.description = c.getString(c.getColumnIndex(RT_DESCRIPTION));
 		r.rating = c.getFloat(c.getColumnIndex(RT_RATING));
 		r.creator = c.getString(c.getColumnIndex(RT_CREATOR));
 		r.date = c.getString(c.getColumnIndex(RT_DATE));
 		r.serving = c.getInt(c.getColumnIndex(RT_SERVING));
 		r.time = c.getInt(c.getColumnIndex(RT_TIME));
 		r.photo = c.getString(c.getColumnIndex(RT_PHOTO));
 		
 		r.ingredients = getRecipeIngredientStrings(rid);
 		r.directions = getRecipeDirectionStrings(rid);
 		r.directions_photos = getRecipeDirectionPhotoStrings(rid);
 		r.tags = getRecipeTagStrings(rid);
 		
 		c.close();
 		return r;
 	}
 	
 	public Cursor getRecipeIngredients(long rid) {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		return db.query(INGREDIENTS_TABLE, INGREDIENTS_FIELDS, IT_RECIPE_ID + " = ?", 
 				new String[] { Long.toString(rid) }, null, null, null);
 	}
 	
 	public String[] getRecipeIngredientStrings(long rid) {
 		Cursor c = getRecipeIngredients(rid);
 		String[] ings = new String[c.getCount()];
 		
 		c.moveToFirst();
 		while (!c.isAfterLast()) {
 			ings[c.getPosition()] = c.getString(c.getColumnIndex(IT_NAME));
 			c.moveToNext();
 		}
 		
 		c.close();
 		return ings;
 	}
 	
 	public Cursor getRecipeDirections(long rid) {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		return db.query(DIRECTIONS_TABLE, DIRECTIONS_FIELDS, DT_RECIPE_ID + " = ?", 
 				new String[] { Long.toString(rid) }, null, null, DT_SEQUENCE);
 	}
 
 	public String[] getRecipeDirectionStrings(long rid) {
 		// NOTE: the returned array is in database sequential order
 		Cursor c = getRecipeDirections(rid);
 		String[] dirs = new String[c.getCount()];
 		
 		c.moveToFirst();
 		while (!c.isAfterLast()) {
 			dirs[c.getPosition()] = c.getString(c.getColumnIndex(DT_STEP));
 			c.moveToNext();
 		}
 		
 		c.close();
 		return dirs;
 	}
 	
 	private String[] getRecipeDirectionPhotoStrings(long rid) {
 		// NOTE: the returned array is in database sequential order
 		Cursor c = getRecipeDirections(rid);
 		String[] dirs = new String[c.getCount()];
 		
 		c.moveToFirst();
 		while (!c.isAfterLast()) {
 			dirs[c.getPosition()] = c.getString(c.getColumnIndex(DT_PHOTO));
 			c.moveToNext();
 		}
 		
 		c.close();
 		return dirs;
 	}
 
 	public Cursor getRecipeTags(long rid) {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		return db.query(TAGS_TABLE, TAGS_FIELDS, TT_RECIPE_ID + " = ?", 
 				new String[] { Long.toString(rid) }, null, null, null);
 	}
 
 	public String[] getRecipeTagStrings(long rid) {
 		Cursor c = getRecipeTags(rid);
 		String[] tags = new String[c.getCount()];
 		
 		c.moveToFirst();
 		while (!c.isAfterLast()) {
 			tags[c.getPosition()] = c.getString(c.getColumnIndex(TT_TAG));
 			c.moveToNext();
 		}
 		
 		c.close();
 		return tags;
 	}
 	
 	public Cursor getRecipesByTag(String tag, String sortBy) {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		String[] fields = RECIPES_FIELDS;
 		fields[0] = RECIPES_TABLE + "." + RT_ID;
 		return db.query(createInnerJoin(RECIPES_TABLE, TAGS_TABLE, RT_ID, TT_RECIPE_ID), fields,
 				TT_TAG + " = ?", new String[] { tag }, null, null, sortBy);
 	}
 	
 	public Cursor getRecipesByTag(String tag) {
 		return getRecipesByTag(tag, RT_NAME);
 	}
 	
 	public Cursor getAllTags() {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		return db.query(TAGS_TABLE, TAGS_FIELDS, null, null, TT_TAG, null, TT_TAG);
 	}
 	
 	public boolean recipeHasTag(long rid, String tag) {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		Cursor c = db.query(TAGS_TABLE, TAGS_FIELDS, "(" + TT_TAG + " = ?) and (" + TT_RECIPE_ID + " = ?)", 
 				new String[] { tag, Long.toString(rid) }, null, null, null);
 		boolean hasTag = c.getCount() != 0;
 		c.close();
 		return hasTag;
 	}
 	
 	public int insertRecipe(ContentValues values) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		int ret = -1;
 		try {
 			ret = (int) db.insert(RECIPES_TABLE, null, values);
 		} finally {
 			db.close();
 		}
 		return ret;
 	}
 	
 	public void insertIngredients(ContentValues values) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		try {
 			db.insertWithOnConflict(INGREDIENTS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
 		} finally {
 			db.close();
 		}
 	}
 	
 	public void insertDirections(ContentValues values) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		try {
 			db.insertWithOnConflict(DIRECTIONS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
 		} finally {
 			db.close();
 		}
 	}
 
 	public void insertTags(ContentValues values) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		try {
 			db.insertWithOnConflict(TAGS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
 		} finally {
 			db.close();
 		}
 	}
 
 	public int insertRecipe(Recipe r) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		int ret = -1;
 		try {
 			ContentValues values = createRecipeForInsert(r);
 			long rowid = db.insert(RECIPES_TABLE, null, values);
 
 			if (r.ingredients != null) {
 				for (String ing : r.ingredients) {
 					ContentValues cvi = createIngredientsCV(rowid, ing);
 					db.insertWithOnConflict(INGREDIENTS_TABLE, null, cvi, SQLiteDatabase.CONFLICT_IGNORE);
 				}
 			}
 
 			if (r.directions != null) {
 				int step = 1;
 				for (String dir : r.directions) {
 					ContentValues cdirs = createDirectionsCV(rowid, step, dir, r.directions_photos[step-1]);
 					db.insertWithOnConflict(DIRECTIONS_TABLE, null, cdirs, SQLiteDatabase.CONFLICT_IGNORE);
 					step++;
 				}
 			}
 
 			if (r.tags != null) {
 				for (String tag : r.tags) {
 					ContentValues ctags = createTagsCV(rowid, tag);
 					db.insertWithOnConflict(TAGS_TABLE, null, ctags, SQLiteDatabase.CONFLICT_IGNORE);
 				}
 			}
 			ret = (int) rowid;
 		} finally {
 			db.close();
 		}
 		return ret;
 	}
 
 	public static ContentValues createTagsCV(long rowid, String tag) {
 		ContentValues ctags = new ContentValues();
 		ctags.put(TT_TAG, tag);
 		ctags.put(TT_RECIPE_ID, rowid);
 		return ctags;
 	}
 
 	public static ContentValues createDirectionsCV(long rowid, int step, String dir, String photo) {
 		ContentValues cdirs = new ContentValues();
 		cdirs.put(DT_STEP, dir);
 		cdirs.put(DT_SEQUENCE, step);
 		cdirs.put(DT_PHOTO, photo);
 		cdirs.put(DT_RECIPE_ID, rowid);
 		return cdirs;
 	}
 
 	public static ContentValues createIngredientsCV(long rowid, String ing) {
 		ContentValues cvi = new ContentValues();
 		cvi.put(IT_NAME, ing);
 		cvi.put(IT_RECIPE_ID, rowid);
 		return cvi;
 	}
 
 	public static ContentValues createRecipeForInsert(Recipe r) {
 		ContentValues values = new ContentValues();
 		values.put(RT_NAME, r.name);
 		values.put(RT_DESCRIPTION, r.description);
 		values.put(RT_RATING, r.rating);
 		values.put(RT_CREATOR, r.creator);
 		values.put(RT_DATE, r.date);
 		values.put(RT_SERVING, r.serving);
 		values.put(RT_TIME, r.time);
 		values.put(RT_PHOTO, r.photo);
 		return values;
 	}
 	
 	public void deleteRecipe(long rid) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		try {
 			db.delete(INGREDIENTS_TABLE, IT_RECIPE_ID + " = ?", new String[] { Long.toString(rid) });
 			db.delete(DIRECTIONS_TABLE, DT_RECIPE_ID + " = ?", new String[] { Long.toString(rid) });
 			db.delete(TAGS_TABLE, TT_RECIPE_ID + " = ?", new String[] { Long.toString(rid) });
 			db.delete(RECIPES_TABLE, RT_ID + " = ?", new String[] { Long.toString(rid) });
 		} finally {
 			db.close();
 		}
 	}
 	
 	public int updateRecipe(Recipe r) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		int ret = -1;
 		try {
 			long rid = r.id;
 			String[] whereArgs = { Long.toString(rid) };
 			ret = db.update(RECIPES_TABLE, createRecipeForInsert(r), RT_ID + " = ?", 
 					new String[] { Long.toString(r.id) });
 
 				// TODO until we can figure out a smarter way to update
 				db.delete(INGREDIENTS_TABLE, IT_RECIPE_ID + " = ?", whereArgs);
 				for (String ing : r.ingredients) {
 					db.insertWithOnConflict(INGREDIENTS_TABLE, null, createIngredientsCV(rid, ing), 
 							SQLiteDatabase.CONFLICT_IGNORE);
 				}
 
 				db.delete(DIRECTIONS_TABLE, DT_RECIPE_ID + " = ?", whereArgs);
 				int step = 1;
 				for (String dir : r.directions) {
 					db.insertWithOnConflict(DIRECTIONS_TABLE, null, 
 							createDirectionsCV(rid, step, dir, r.directions_photos[step-1]), 
 							SQLiteDatabase.CONFLICT_IGNORE);
 					step++;
 				}
 
 				db.delete(TAGS_TABLE, TT_RECIPE_ID + " = ?", whereArgs);
 				for (String tag : r.tags) {
 					db.insertWithOnConflict(TAGS_TABLE, null, createTagsCV(rid, tag), 
 							SQLiteDatabase.CONFLICT_IGNORE);
 				}
 		} finally {
 			db.close();
 		}
 		return ret;
 	}
 	
 	public int updateRecipe(long rid, ContentValues values) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		int ret = -1;
 		try {
 			ret = db.update(RECIPES_TABLE, values, RT_ID + " = ?", 
 					new String[] { Long.toString(rid) });
 		} finally {
 			db.close();
 		}
 		return ret;
 	}
 	
 	public void updateIngredients(long rid, ContentValues values) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		try {
 			db.updateWithOnConflict(INGREDIENTS_TABLE, values, IT_RECIPE_ID + " = ?", 
 					new String[] { Long.toString(rid) }, SQLiteDatabase.CONFLICT_IGNORE);
 		} finally {
 			db.close();
 		}		
 	}
 	
 	public void updateDirections(long rid, ContentValues values) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		try {
 			db.updateWithOnConflict(DIRECTIONS_TABLE, values, DT_RECIPE_ID + " = ?", 
 					new String[] { Long.toString(rid) }, SQLiteDatabase.CONFLICT_IGNORE);
 		} finally {
 			db.close();
 		}
 	}
 	
 	public void updateTags(long rid, ContentValues values) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		try {
 			db.updateWithOnConflict(TAGS_TABLE, values, TT_RECIPE_ID + " = ?", 
 					new String[] { Long.toString(rid) }, SQLiteDatabase.CONFLICT_IGNORE);
 		} finally {
 			db.close();
 		}
 	}
 	
 	public String findCacheEntry(String uri) {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		Cursor c = db.query(CACHE_TABLE, CACHE_FIELDS, CT_URI + " = ?", 
 				new String[] { uri }, null, null, null);
 		if (c.getCount() > 0) {
 			c.moveToFirst();
 			String s = c.getString(c.getColumnIndex(CT_CACHED));
 			c.close();
 			return s;
 		} else {
 			c.close();
 			return null;
 		}
 	}
 	
 	public boolean isCached(String uri) {
 		return findCacheEntry(uri) != null;
 	}
 	
 	public String getOldestCacheEntry() {
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		Cursor c = db.query(CACHE_TABLE, CACHE_FIELDS, null, null, null, null, CT_CACHED, "1");
 		if (c.getCount() > 0) {
 			c.moveToFirst();
 			String s = c.getString(c.getColumnIndex(CT_URI));
 			c.close();
 			return s;
 		} else {
 			c.close();
 			return null;
 		}
 	}
 	
 	public void insertCacheEntry(String uri, String cached) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(CT_URI, uri);
 		values.put(CT_CACHED, cached);
 		
 		if (isCached(uri)) {
 			db.update(CACHE_TABLE, values, CT_URI + " = ?", new String[] { cached });
 		} else {
 			db.insert(CACHE_TABLE, null, values);
 		}
 	}
 	
 	public void removeCacheEntry(String uri) {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		
 		if (isCached(uri)) {
 			db.delete(CACHE_TABLE, CT_URI + "= ?", new String[] { uri });
 		}
 	}
 	
 	public String removeOldestCacheEntry() {
 		// returns the filename of the (now-deleted) oldest cache entry
 		String uri = getOldestCacheEntry();
 		String filename = findCacheEntry(uri);
 		removeCacheEntry(uri);
 		return filename;
 	}
 	
 	public void clearCache() {
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		db.delete(CACHE_TABLE, null, null);
 	}
 	
 	// TODO rewrite the import/export to allow user choices
 	public ArrayList<Recipe> importRecipes(String path) throws IOException {
 		File f = new File(URI.create(path));
 		FileInputStream fis = new FileInputStream(f);
 		byte[] buffer = new byte[fis.available()];
 		fis.read(buffer);
 		String st = new String(buffer);
 		return parseJsonRecipes(st);
 	}
 
 	public ArrayList<Recipe> parseJsonRecipes(String st) {
 		ArrayList<Recipe> recipes = new ArrayList<Recipe>();
 		try {
 			JSONArray ja = new JSONArray(st);
 			for (int i = 0; i < ja.length(); i++) {
 				JSONObject jo = ja.getJSONObject(i);
 				Recipe r = Recipe.parseJSON(jo);
 				if (r != null) {
 					recipes.add(r);
 				}
 			}
 		} catch (JSONException e) {
 //			Log.e(TAG, e.toString());
 			// TODO handle exception
 		}
 		return recipes;
 	}
 	
 	public void insertImportedRecipes(ArrayList<Recipe> recipes) {
 		for (Recipe r : recipes) {
 			if (r != null) {
 				insertRecipe(r);
 			}
 		}
 	}
 	
 	public String exportRecipes(long[] ids) throws IOException {
 		// TODO file selection, sharing, etc.
 		String filename = "exported-recipes-" + System.currentTimeMillis() + ".rcp";
 		File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
 		File export = new File(sd, filename);
 		FileOutputStream fos = null;
 		sd.mkdirs();
 		try {
 			fos = new FileOutputStream(export);
 			JSONArray ja = new JSONArray();
 			for (long i : ids) {
 				ja.put(getSingleRecipeObject(i).toJSON());
 			}
 			byte[] buffer = ja.toString().getBytes();
 			fos.write(buffer);
 			return export.toString();
 		} catch (FileNotFoundException e) {
 //			Log.e(TAG, e.toString());
 			return null;
 		} finally {
 			if (fos != null) {
 				fos.close();
 			}
 		}
 	}
 }
