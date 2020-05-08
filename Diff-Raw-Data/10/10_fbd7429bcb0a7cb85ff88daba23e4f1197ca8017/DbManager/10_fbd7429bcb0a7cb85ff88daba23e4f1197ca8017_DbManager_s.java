 package ca.ualberta.cmput301w13t11.FoodBook.model;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.os.Environment;
 import android.util.Log;
 /**
  * Singleton class that manages the application's database.
  * @author Mark Tupala
  *
  */
 public class DbManager extends FModel<FView> {
 
 	
     // the actual database 
     protected static SQLiteDatabase db;
     protected static DbOpenHelper dbHelper;
     
     // singleton pattern implementation
     private static DbManager instance = null;
     
     //Used to ensure that multiple connections to the database are not opened and
     //also to ensure that multiple database helpers do not open.
     private static boolean has_executed = false;
     
     // name of database file
     private String dbFileName = "RecipeApplicationDb";
 	public String recipesTable = "UserRecipes";
 	public String ingredsTable = "RecipeIngredients";
 	public String photosTable = "RecipePhotos";
     
 
     /**
      * Protected constructor because we're using the singleton pattern.
      */
     protected DbManager(Context context) {
     	super();
     	// open or create the sqlite db accordingly
     	if (DbManager.has_executed == false) {
     		// Use the has_executed flag to ensure we do not open
     		// multiple connections to the database and we do not
     		// create multiple dbHelper objects.
     		dbHelper = new DbOpenHelper(context, dbFileName);
     		db = dbHelper.getWritableDatabase();
     		DbManager.has_executed = true;
     	}
     }
     
     /**
      * @return True if DbManager constructor has been called before, false if not
      */
     public static boolean getHasExecuted()
     {
     	return has_executed;
     }
     
     /**
      * Get instance of the singleton DbManager.
      * @return The instance of the class.
      */
     public static DbManager getInstance(Context context) {
     	if (instance == null) {
     		// db instance doesn't exist, create new one
     		instance = new DbManager(context);
     	}	
     	return instance;
     }
 
     /**
      * Get instance of the singleton DbManager, when we know that it exists
      * @return The instance of the class.
      */
     public static DbManager getInstance() {	
     	return instance;
     }
     
 	/**
 	 * Inserts a recipe into the table.
 	 * @param recipe The Recipe to be stored.
 	 * @param tableName The name of the table into which the recipe is to be stored.
 	 */
 	public void insertRecipe(Recipe recipe, String tableName) 
 	{
 	    ContentValues values = recipe.toContentValues();
 	    db.insert(tableName, null, values);
 	    for (Ingredient ingred : recipe.getIngredients()) {
 	        insertRecipeIngredients(ingred, recipe.getUri());
 	    }
 
 	
 	    for (Photo photo : recipe.getPhotos()) {
 	        insertRecipePhotos(photo, photo.getPhotoBitmap(), recipe.getUri());
 	    }
 	    
 	}
     
     /**
      * Inserts the given Ingredient into the database such that it is associated with the
      * recipe identified by recipeURI.
      * @param ingred The ingredient to be 2 saves pics to inserted.
      * @param recipeURI The URI of the Recipe with which to associate the Ingredient.
      */
     public void insertRecipeIngredients(Ingredient ingred, long recipeURI) {
         ContentValues values = ingred.toContentValues();
         values.put("recipeURI", recipeURI);
         db.insert("RecipeIngredients", null, values);
     }
    
     /**
      * Inserts the given Photo into the database such that it is associated with the
      * recipe identified by recipeURI.
      * @param photo The photo to be inserted.
      * @param recipeURI The URI of the Recipe with which to associate the Photo.
      */
     public boolean insertRecipePhotos(Photo photo, Bitmap bitmap, long recipeURI) {
     	/* We first attempt to store the bitmap associated with the photo to the Db */
     	if (!savePhotoToDevice(bitmap, photo)) {
     		/* Saving failed, return false. */
     		return false;
     	}
     	
     	/* Else, we can safely place the photo information into the database. */
     	ContentValues values = new ContentValues();
     	values.put("recipeURI", recipeURI);
     	values.put("id", photo.getId());
     	values.put("path", photo.getPath());
     	db.insert("RecipePhotos", null, values);
     	/* If we got here, everything was successful. */
     	return true;
     }
     
     /* ******************************************************************************************************** */
     /* *****************************************Temp import for debugging purposes***************************** */
 	protected String resultsTable = "ResultsRecipes";
 	private String getSQL = "SELECT * FROM " + resultsTable;
 	/**
 	 * store results from server.
 	 * @return should i return boolean for success?
 	 */
 	public void storeRecipes(ArrayList<Recipe> recipes) {
 	    db.delete(resultsTable, null, null);
 	    for (Recipe recipe : recipes) {
 	        insertRecipe(recipe, resultsTable);
 	    }
 	    notifyViews();
 	}
     /* ******************************************************************************************************** */
     /* ******************************************************************************************************** */
 
 	
 
     /**
      * Returns an ArrayList of all the Recipes stored in the table.
 	 * @return An ArrayList of all the Recipes stored in the table.
 	 */
 	public ArrayList<Recipe> getRecipes(String query) {
 	    Cursor cursor = db.rawQuery(query, null);
 	    return cursorToRecipes(cursor);
 	}
 
 	/**
 	 * Returns Recipe stored in the table, given recipe's uri
 	 * @return A Recipe stored in the table.
 	 */
 	public Recipe getRecipe(long uri) {
 		String query = new String("SELECT * FROM " + recipesTable + " WHERE URI = " + uri);
 	    Cursor cursor = db.rawQuery(query, null);
 	    return cursorToRecipe(cursor);
 	}
 	
 	/**
 	 * Returns Recipe stored in the table, given recipe's uri
 	 * @return ARecipes stored in the table.
 	 */
 	public Recipe getRecipe(String query) {
 	    Cursor cursor = db.rawQuery(query, null);
 	    return cursorToRecipe(cursor);
 	}
     
     /**
      * Given a cursor, convert it to an ArrayList of Recipes.
      * @param cursor The cursor over which we will iterate to get recipes from.
      * @return An ArrayList of Recipes.
      */
     protected ArrayList<Recipe> cursorToRecipes(Cursor cursor) {
         ArrayList<Recipe> recipes = new ArrayList<Recipe>();
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             long uri = cursor.getLong(0);
             User author = new User(cursor.getString(2));
             String title = cursor.getString(1);
             String instructions = cursor.getString(3);
             ArrayList<Ingredient> ingredients = getRecipeIngredients(uri);
             ArrayList<Photo> photos = getRecipePhotos(uri);
             Recipe recipe = new Recipe(uri, author, title, instructions, ingredients, photos);
             recipes.add(recipe);
             cursor.moveToNext();
         }
         return recipes;
     }
 
 
     /**
     * Given a cursor, convert it to a Recipe.
     * @param cursor The cursor over which we will iterate to get recipes from.
     * @return An ArrayList of Recipes.
     */
    protected Recipe cursorToRecipe(Cursor cursor) {
  
        cursor.moveToFirst();
 
        long uri = cursor.getLong(0);
        User author = new User(cursor.getString(2));
        String title = cursor.getString(1);
        String instructions = cursor.getString(3);
        ArrayList<Ingredient> ingredients = getRecipeIngredients(uri);
        ArrayList<Photo> photos = getRecipePhotos(uri);
        ArrayList<Photo> fullPhotos = getFullPhotos(photos);
        Recipe recipe = new Recipe(uri, author, title, instructions, ingredients, fullPhotos);
 
        if (cursor.getCount()!=0) {
     	   //print error message here
        }
        return recipe;
    }
    // --- 
     
     
     /**
      * Gets all the Ingredients associated with the recipe identified by its URI.
      * @param uri The URI of the recipe whose ingredients we are fetching.
      * @return An ArrayList of the Ingredients associated with the recipe.
      */
     protected ArrayList<Ingredient> getRecipeIngredients(long uri) {
     	Cursor cursor = db.rawQuery("Select * From RecipeIngredients Where recipeURI = " + uri, null);
     	return cursorToIngredients(cursor);
     }
     
     /**
      * Gets all the Photos associated with the recipe identified by its URI.
      * NOTE: "changed to public so that we can retrieve just photos for galleries" - Pablo
      * @param uri The URI of the recipe whose photos we are fetching.
      * @return An ArrayList of the Photos associated with the recipe.
      */
     public ArrayList<Photo> getRecipePhotos(long uri) 
     {
     	Cursor cursor = db.rawQuery("Select * From RecipePhotos Where recipeURI = " + uri, null);
     	return cursorToPhotos(cursor);
     }
     
     /**
      * Removes the given photo from the local storage device.
      * @param photo The photo to be deleted.
      * @return true on success, false on failure
      */
     public boolean removeRecipePhoto(Photo photo) {
     	//String createStatement = 
     	
     	//String.format("Delete From RecipePhotos Where recipeUri = %S and filename = %S", uri, photo.getName()); 
    	int success = db.delete("RecipePhotos", "id = " + photo.getId(), null); 
    	//Log.d("int", Integer.toString(r));
     	Boolean deleted = false;
     	
     		try{
 		    	File file = new File(photo.getPath());
 		        deleted = file.delete();
     		}
     		catch(Exception e){
     			e.printStackTrace();
     		}  	
    	return (success==1 && deleted==true);
     }
         
     /**
      * Deletes the ingredients associated with the recipe specified by the uri (ie.
      * removes them from the RecipeIngredients table).
      * @param uri The uri of the recipe whose ingrediens we would like to remove.
      * @return true on success, false on failure
      */
     public boolean removeRecipeIngredients(long uri) {
     	//String createStatement = 
     	
     	int success = db.delete("RecipeIngredients", "recipeURI = " + uri, null); 
     	    	
     	return (success>=1);
     }
     
     /**
      * Given a cursor, convert it to an ArrayList of Ingredients.
      * @param cursor The cursor over which we will iterate to get ingredients from.
      * @return An ArrayList of Ingredients.
      */
     protected ArrayList<Ingredient> cursorToIngredients(Cursor cursor) {
         ArrayList<Ingredient> ingreds = new ArrayList<Ingredient>();
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             String name = cursor.getString(0);
             String unit = cursor.getString(1);
             float quantity = cursor.getFloat(2);
             Ingredient ingred = new Ingredient(name, unit, quantity);
             ingreds.add(ingred);
             cursor.moveToNext();
         }
         return ingreds;
     }
     
     /**
      * Given a cursor, convert it to an ArrayList of Photos.
      * @param cursor The cursor over which we will iterate to get photos from.
      * @return An ArrayList of Photos.
      */
     protected ArrayList<Photo> cursorToPhotos(Cursor cursor) {
         ArrayList<Photo> photos = new ArrayList<Photo>();
         ArrayList<Photo> fullPhotos = new ArrayList<Photo>();
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             String id = cursor.getString(1);
             String path = cursor.getString(2);
             Photo photo = new Photo(id, path);
             //Log.d("DbManager fetched: photo id", id);
             //Log.d("DbManager fetched: photo path", path);
             photos.add(photo);
             cursor.moveToNext();
         }
         fullPhotos = getFullPhotos(photos);
         return fullPhotos;
     }
 
 
     /**
      * Remove the given recipe from both local storage and the database.
      * TODO: "must include other methods to remove ingredients and other stuff from other tables. -Pablo"
      * @param recipe The recipe to be deleted.
      * @return True on success, False on failure.
      */
     public boolean removeRecipe(Recipe recipe) {
     	
     	Long uri = recipe.getUri();
     	
     	int recipes_removed = 0;
     	boolean deleted_pictures = true;
     	//boolean deleted_ingreds = true;
     	try{
     		//String s = Long.toString(recipe.getUri());
     		//Log.d("uri in String", s);
     		recipes_removed = db.delete("UserRecipes", "URI = " + recipe.getUri(), null);
     		//Log.d("we got past removing recipes", "OK");
     		//String s = Integer.toString(recipes_removed);
     		//Log.d("recipes", s);
     		
     		ArrayList<Photo> photos = getRecipePhotos(uri); 
     		for (Photo p: photos){
     			if (removeRecipePhoto(p)!=true){
     				deleted_pictures=false;
     			}
     		}
   
     		//Log.d("we got past removing photos", "OK");
     		//s = new Boolean(deleted_pictures).toString();
     		//Log.d("photos", s);
     		//Do the same for ingredients
     		// There might be none, so dont check -Pablo
   			//deleted_ingreds = removeRecipeIngredients(uri);
     		removeRecipeIngredients(uri);
   			
   			//Log.d("we got past removing ingredients", "OK");
     		//s = new Boolean(deleted_ingreds).toString();
     		//Log.d("ingreds", s);
     				    		
     	}catch(Exception e){e.printStackTrace();};
     	// No need to check if ingredients cleared
     	//return (recipes_removed==1 && deleted_pictures==true && deleted_ingreds==true);
     	return (recipes_removed==1 && deleted_pictures==true);
     }
     
     /**
      * Saves the given bitmap to the local device.
      * @param bitmap The bitmap to be saved.
      * @param timeStampdId The name of the photo.
      * @return True on success, false on failure.
      */
     private boolean savePhotoToDevice(Bitmap bitmap, Photo photo)
     {
     	String imgPath = photo.getPath(); 	/* the path to the image file on the device */
     	File file = null; 					/* the image file itself */
     	boolean success = false;			/* set to true on successful write of image file to device storage*/
     	boolean worked = false;			/* set to true on successful compression of given bitmap*/
     	FileOutputStream outStream = null;	/* the file write stream */
     	
     	try {
 
     		file = new File(imgPath);
 			outStream = new FileOutputStream(file);
 			
 			worked = bitmap.compress(Bitmap.CompressFormat.PNG, 30, outStream);
 			outStream.flush();
 			outStream.close();
 			success = true;
 			imgPath = file.getAbsolutePath();
 
 		} catch (Exception ex) {
 			// TODO Auto-generated catch block
 			ex.printStackTrace();
 			Log.d("Failed to save image.", "Failed to save image.");
 			return false;
 		} 
 
     	return (success && worked);
     }
     
 	/**
 	 * TODO: better documentation for this function
 	 * Returns a scaled down version of the image defined by filePath.
 	 * @param Where the photo can be found.
 	 * @return A scaled down version of the photo.
 	 */
 	public Bitmap decodeURI(String filePath){
 
 	    Options options = new Options();
 	    options.inJustDecodeBounds = true;
 	    BitmapFactory.decodeFile(filePath, options);
 
 	    // Only scale if we need to 
 	    // (16384 buffer for img processing)
 	    Boolean scaleByHeight = Math.abs(options.outHeight - 100) >= Math.abs(options.outWidth - 100);
 	    if(options.outHeight * options.outWidth * 2 >= 16384){
 	        // Load, scaling to smallest power of 2 that'll get it <= desired dimensions
 	        double sampleSize = scaleByHeight
 	            ? options.outHeight / 100
 	            : options.outWidth / 100;
 	        options.inSampleSize = 
 	            (int)Math.pow(2d, Math.floor(
 	            Math.log(sampleSize)/Math.log(2d)));
 	    }
 
 	    // Do the actual decoding
 	    options.inJustDecodeBounds = false;
 	    options.inTempStorage = new byte[512];  
 	    Bitmap output = BitmapFactory.decodeFile(filePath, options);
 
 	    return output;
 	}
 	
 	/**
 	 * Given the ArrayList of photos with only an id and a pathname, returns a list of photos
 	 * with a byte_array as well.
 	 * TODO: could be modded in the future such that it only modifies the given list of photos -- I'm not sure
 	 * how Java handles references/pointers and all that so I'll defer giving this real consideration until
 	 * other functionality is complete. --Marko
 	 * @param photos
 	 * @return An ArrayList of a photos with the corresponding byte_area representing the img data included.
 	 */
 	private ArrayList<Photo> getFullPhotos(ArrayList<Photo> photos)
 	{
 		if (photos == null || photos.isEmpty()) {
 			return new ArrayList<Photo>();
 		}
 		ArrayList<Photo> fullPhotos = new ArrayList<Photo>();
 	    Options options = new Options();
 	    options.inJustDecodeBounds = false;
 		for (int i = 0; i < photos.size(); i++)
 		{
 			Photo temp = photos.get(i);
 			Photo fullPhoto = new Photo(temp.getId(), temp.getPath(), BitmapFactory.decodeFile(temp.getPath(), options));
 			fullPhotos.add(fullPhoto);
 		}
 		return fullPhotos;
 	}
 }
 
 
 
     
