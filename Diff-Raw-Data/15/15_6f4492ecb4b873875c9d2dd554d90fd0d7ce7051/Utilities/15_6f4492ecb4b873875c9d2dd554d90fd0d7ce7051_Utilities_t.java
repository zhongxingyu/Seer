 /*
  * Copyright 2012 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.vendsy.bartsy.venue.utils;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.vendsy.bartsy.venue.db.DatabaseManager;
 import com.vendsy.bartsy.venue.model.Category;
 import com.vendsy.bartsy.venue.model.Cocktail;
 import com.vendsy.bartsy.venue.model.Ingredient;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 
 /**
  * Helper class providing methods and constants common to other classes in the
  * app.
  */
 public final class Utilities {
 
 	/**
 	 * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
 	 */
 	static final String SERVER_URL = "";
 
 	/**
 	 * Google API project id registered to use GCM.
 	 */
 //	public static final String SENDER_ID = "227827031375";
 	public static final String SENDER_ID = "605229245886"; //public server
 	
 	/**
 	 * Tag used on log messages.
 	 */
 
 	static final String TAG = "GCMDemo";
 
 	/**
 	 * Intent used to display a message in the screen.
 	 */
 	public static final String DISPLAY_MESSAGE_ACTION = "com.vendsy.bartsy.venue.DISPLAY_MESSAGE";
 
 	/**
 	 * Intent's extra that contains the message to be displayed.
 	 */
 	public static final String EXTRA_MESSAGE = "message";
 
 	/**
 	 * Notifies UI to display a message.
 	 * <p>
 	 * This method is defined in the common helper because it's used both by the
 	 * UI and the background service.
 	 * 
 	 * @param context
 	 *            application's context.
 	 * @param message
 	 *            message to be displayed.
 	 */
 	public static void displayMessage(Context context, String message) {
 		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
 		intent.putExtra(EXTRA_MESSAGE, message);
 		context.sendBroadcast(intent);
 	}
 	
 	/**
 	 * To prepare progress dialog
 	 * 
 	 * @param context
 	 * @return
 	 */
 	public static ProgressDialog progressDialog(Context context, String message){
 		ProgressDialog mProgressDialog = new ProgressDialog(context);
 		
 		// To configure the loading dialog
         mProgressDialog.setMessage(message);
         mProgressDialog.setIndeterminate(false);
         mProgressDialog.setCancelable(true);
         
         return mProgressDialog;
 	}
 	
 	/**
 	 * To read ingredients data from CSV file and save in the db
 	 * 
 	 * @param context
 	 */
 	public static void saveIngredientsFromCSVFile(Context context){
 		String data[] = null;
         // Try to read ingredients data from CSV file 
         try {
         	// Initialize the CSVReader object with CSV file by using InputStream
             CSVReader reader = new CSVReader(new InputStreamReader(context.getAssets().open(Constants.INGREDIENTS_CSV_FILE)));
             data = reader.readNext();
             
             ArrayList<Category> categories = new ArrayList<Category>();
             Category category = null;
             
             //Loop until the end of the line
             while(data!=null) {
                 data = reader.readNext();
                 
                 // To ignore empty categories and empty types
                 if(data != null && data.length==3 && !data[1].trim().equals("") && !data[2].trim().equals("")) {
                 	// To set the properties for Ingredient model
                 	Ingredient ingredient = new Ingredient();
                 	ingredient.setName(data[0]);
                 	
                 	// To avoid duplicate categories in the list
                	category = getExistingCategoryInList(categories, data[2], data[1]);
                	if(category==null){
                 		category = new Category();
                 		category.setName(data[2]);
                 		category.setType(data[1]);
                 		// Here, Category is saving in the  DB
                     	DatabaseManager.getInstance().saveSection(category);
                     	
                 		categories.add(category);
                 	}
                 	ingredient.setCategory(category);
                 	
                 	// Here saving in the DB
                 	DatabaseManager.getInstance().saveIngredient(ingredient);
                 } 
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 	}
 	
 	/**
	 * To check category is already exist or not. If it is already exist then it will return the object otherwise it will return null
 	 * 
 	 * @param list
 	 * @param category
 	 * @param type
 	 * @return
 	 */
	public static Category getExistingCategoryInList(ArrayList<Category> list, String category, String type){
 		for (Category categoryItem : list) {		
 			if(category.equals(categoryItem.getName()) && type.equals(categoryItem.getType())){
				return categoryItem;
 			}
 		}
		return null;
 	}
 	
 	/**
 	 * To read Cocktails data from CSV file and save in the db
 	 * 
 	 * @param context
 	 */
 	public static void saveCocktailsFromCSVFile(Context context){
 		String data[] = null;
         // Try to read ingredients data from CSV file 
         try {
         	// Initialize the CSVReader object with CSV file by using InputStream
             CSVReader reader = new CSVReader(new InputStreamReader(context.getAssets().open(Constants.COCKTAILS_CSV_FILE)));
             // To skip excel sheet headings
             data = reader.readNext();
             
             //Loop until the end of the line
             while(data!=null) {
                 data = reader.readNext();
                 
                 // To ignore empty categories and empty types
                 if(data != null && data.length>=6 && !data[1].trim().equals("") && !data[3].trim().equals("")) {
                 	// To set the properties for Ingredient model
                 	Cocktail cocktail = new Cocktail();
                 	cocktail.setName(data[0]);
                 	// For now it is hard coded for category. 
                 	cocktail.setCategory(Category.COCKTAILS_TYPE);
                 	
                 	cocktail.setAlcohol(data[2]);
                 	cocktail.setGlassType(data[3]);
                 	cocktail.setIngredients(data[4]);
                 	cocktail.setInstructions(data[5]);
                 	
                 	// Here saving in the DB
                 	DatabaseManager.getInstance().saveCocktail(cocktail);
                 } 
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 	}
 }
