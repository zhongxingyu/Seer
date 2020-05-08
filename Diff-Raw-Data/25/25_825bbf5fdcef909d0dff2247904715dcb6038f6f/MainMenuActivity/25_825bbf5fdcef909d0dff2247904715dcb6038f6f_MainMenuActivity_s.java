 package com.openfridge;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 
 public class MainMenuActivity extends Activity {
 	public static Intent mainMenu, expirationList, expire, itemEdit, shopping, shoppingList;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         MainMenuActivity.mainMenu = new Intent(this, MainMenuActivity.class);
         MainMenuActivity.expirationList = new Intent(this, ExpirationListActivity.class);
         MainMenuActivity.expire = new Intent(this, ExpireActivity.class);
         MainMenuActivity.itemEdit = new Intent(this, ItemEditActivity.class);
         MainMenuActivity.shopping = new Intent(this, ShoppingActivity.class);
         MainMenuActivity.shoppingList = new Intent(this, ShoppingListActivity.class);
         
         setContentView(R.layout.main);
 
         View expB = findViewById(R.id.Exp_button);
         expB.setBackgroundColor(getExpiredColor());
         
         View shoB = findViewById(R.id.Sho_button);
         shoB.setBackgroundColor(getShoppingColor());
     }
 	
 	public int getExpiredColor(){
 		//TODO Use this method to check the state of expired goods and return the appropriate color to set the button
 		
 		return Color.parseColor("#FFD300");
 	}
 	
 	public int getShoppingColor(){
 		//TODO Use this method to check the state of the shopping list and return the appropriate color to set the button
 		
 		return Color.parseColor("#FFFFFF");
 	}
 	
 	public void loadExpiration(View view){
 		startActivity(MainMenuActivity.expirationList);
 	}
 	
 	public void loadShopping(View view){
 		startActivity(MainMenuActivity.shoppingList);
 	}
 }
