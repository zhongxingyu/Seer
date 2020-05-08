 /*
 New BSD License
 Copyright (c) 2012, MyBar Team All rights reserved.
 mybar@turbotorsk.se
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 �	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 �	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 �	Neither the name of the MyBar nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package se.turbotorsk.mybar.controller;
 
 import java.util.LinkedList;
 
 import se.turbotorsk.mybar.model.Data;
 import se.turbotorsk.mybar.model.Drink;
 import se.turbotorsk.mybar.model.Ingredient;
 import se.turbotorsk.mybar.model.MyBar;
 
 /**
  * This activity handles the Controller.
  */
 public class Controller {
 	private static int myBarID = 1;
 	private final static DrinkManager dm = new DrinkManager();
 
 	public static LinkedList<Drink> getMyBar() {
 		// dm.getMyBar(data.getMyIngredients(this.myBarID),
 		// data.getAllDrinks(this.contentResolver)); Implement this in data....
 		return null;
 	}
 
 	/**
 	 * This method is used for getting an ingredient by its ID.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public static Drink getIngredientById(int id) {
 		return null;
 	}
 
 	// ---------- Methods for get and set favorites ----------
 
 	/**
 	 * This method is used for adding a drink to favorite.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public static boolean addFavoriteDrink(int id) {
 		return false;
 	}
 
 	/**
 	 * This method is used for fetching all favorite-drinks.
 	 * 
 	 * @return
 	 */
 	public static LinkedList<Drink> getFavoritDrinks() {
 		return null;
 	}
 
 	public static int isFavorite(int id) {
 		Drink drink = Data.getDrinkByID(id);
 		return drink.getFavorite();
 	}
 
 	public static int rating(int id) {
 		Drink drink = Data.getDrinkByID(id);
 		return drink.getRating();
 	}
 
 	// Methods for Ingredient categories.
 
 	public static boolean addMyBarCat(String Name) {
 		return false; // data.AddMyBarCat(String name); Add this method in data.
 	}
 
 	public static boolean addIngredientToList(int ingredientID) {
 		// data.AddIngredientToList(ingredientID, this.myBarID); Add this method
 		// in data.
 		return false;
 	}
 
 	public static boolean changeMyBarID(int myBarID) {
 		if (true) { // Kolla om ID finns
 			Controller.myBarID = myBarID;
 			return true;
 		} else
 			return false;
 	}
 
 	public static String[][] listMyBarIngredientCategories() {
 		// data.listMyBarIngredientCategories(this.myBarID); Add this method in
 		// data.
 		return null;
 	}
 	
 	public static LinkedList<Ingredient> getMyIngredients(){
 		LinkedList<Ingredient> list = new LinkedList<Ingredient>(); 
 		for(MyBar mybar: Data.getAllMyBar()){
			list.add( Data.getIngredientByID(mybar.getIngredientID());	
 		}
 		return list;
 	}
 }
