 package com.openfridge;
 
 import java.math.BigInteger;
 import java.util.Observable;
 import java.util.Observer;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.view.View;
 
 //DONE no color icons FR
 //DONE Colors need to actually return correctly JW
 //TODO Checking database on regular basis  SC/JW
 //TODO Come up with way to link to a hidden shopping list    :later
 //TODO Optimize accesses to data, get it when we need, and not otherwise   :later
 //DONE get data on program start JW
 //DONE get data asynchronously JW
 //TODO Separate network code, make more locally capable FR JW
 
 
 public class MainMenuActivity extends Activity implements Observer {
 	private Intent expirationList, shoppingList;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         String android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
         
         BigInteger idBigInt = new BigInteger(
                 ((android_id==null) ? "1" : android_id), 16
         );
         
         idBigInt 
             = idBigInt.mod(new BigInteger(Integer.toString(Integer.MAX_VALUE)));
         int idInt = idBigInt.intValue();
         DataClient.getInstance().setUID(idInt);
         
         //mainMenu = new Intent(this, MainMenuActivity.class);
         expirationList = new Intent(this, ExpirationListActivity.class);
         //expire = new Intent(this, ExpireActivity.class);
         //itemEdit = new Intent(this, ItemEditActivity.class);
         //shopping = new Intent(this, ShoppingActivity.class);
         shoppingList = new Intent(this, ShoppingListActivity.class);
         setContentView(R.layout.main);
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		DataClient.getInstance().deleteObserver(this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		DataClient.getInstance().addObserver(this);
 		update(null,null);
 		DataClient.getInstance().reloadFoods();
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
         View expB = findViewById(R.id.Exp_button);
         expB.setBackgroundColor(DataClient.getInstance().getExpirationListColor());
         
         View shoB = findViewById(R.id.Sho_button);
         shoB.setBackgroundColor(DataClient.getInstance().getShoppingListColor());
 	}
 	public void loadExpiration(View view){
 		startActivity(expirationList);
 	}
 	
 	public void loadShopping(View view) {
 		startActivity(shoppingList);
 	}
 }
 /* 
  * Notes about what connects to what:
  * 
  * MainMenu -> ExpirationList & ShoppingList (if it has items)
  * ExpirationList -> Expire, ItemEdit (as a new item)
  * Expire -> ExpirationList & ItemEdit (to edit the item in question)
  * ItemEdit -> ExpirationList
  * ShoppingList -> Shopping (adding items handled on-page)
  * Shopping -> ShopingList
  * */
  
