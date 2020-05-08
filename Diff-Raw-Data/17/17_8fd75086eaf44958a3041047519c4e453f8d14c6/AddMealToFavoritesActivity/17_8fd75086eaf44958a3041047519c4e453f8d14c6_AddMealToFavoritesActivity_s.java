 package com.chips;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ExpandableListView;
 import android.widget.Toast;
 
 import com.chips.adapters.ExpandableFoodListAdapter;
 import com.chips.dataclient.DataPushClient;
 import com.chips.dataclient.FoodClient;
 import com.chips.dataclient.MealClient;
 import com.chips.dataclientactions.PushClientToastOnFailureAction;
 import com.chips.dataclientobservers.ExpandableFoodClientObserver;
 import com.chips.dataclientobservers.UpdateActionDataClientObserver;
 import com.chips.datarecord.FoodRecord;
 import com.chips.datarecord.MealRecord;
 import com.chips.homebar.HomeBar;
 import com.chips.homebar.HomeBarAction;
 import com.chips.user.PersistentUser;
 
 public class AddMealToFavoritesActivity extends DataClientActivity 
         implements HomeBar, Serializable {
 
     private static final long serialVersionUID = -5553313136084359754L;
     
     private static final String BASE_URL 
       = "http://cs110chips.phpfogapp.com/index.php/mobile/";
 	private static final String CREATE_NEW_EMPTY_MEAL
 	  = BASE_URL + "create_new_meal_in_favorites/";
     private static final String ADD_MEAL_TO_FAVORITES_URL
       = BASE_URL + "add_meal_to_favorites/";
     private static final String ADD_FOOD_TO_THIS_FAVORITE_URL
       = BASE_URL + "add_food_to_meal/";
     //TODO get proper quantity update URL
     private static final String QUANTITY_UPDATE_URL
       = BASE_URL;
 
 	private static final int SEARCH_REQUEST_CODE = 0;
 	
 	private List<FoodRecord> currentMeal = new ArrayList<FoodRecord>();
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         HomeBarAction.inflateHomeBarView(this, R.layout.add_meal_to_favorites);
         
         foodClient = new FoodClient();
         ExpandableFoodClientObserver expandableFoodClientObserver
             = new ExpandableFoodClientObserver(this, foodClient);
         
         addClientObserverPair(foodClient, expandableFoodClientObserver);
         
         ExpandableListView favoritesView 
             = (ExpandableListView) findViewById(R.id.mealFoodsListView);
         expandableFoodClientObserver.setListViewLayout(
             favoritesView, 
             foodAdapter = new ExpandableFoodListAdapter(this, currentMeal, 
                     favoritesView, QUANTITY_UPDATE_URL)
         );
         
 //        foodAdapter = new ExpandableFoodListAdapter(this, 
 //                foodClient.getFoodRecords(), favoritesView);
 //        expandableFoodClientObserver.setListViewLayout(
 //            favoritesView, 
 //            foodAdapter
 //        );
         
         setupAddURL();
         setupWebsiteCommunication();
         setupIntents();
     }
     
     private void setupIntents() {
     	Bundle b = new Bundle();
         b.putString(AddFoodActivity.BUNDLE_ADD_KEY, 
                 ADD_FOOD_TO_THIS_FAVORITE_URL);
 
         addFoodToFavoriteIntent 
             = new Intent(this, AddFoodToFavoriteMealActivity.class);
         addFoodToFavoriteIntent.putExtras(b);
     }
     
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
       if (requestCode == 0) {
         if (resultCode == RESULT_OK) {
         
         Bundle extras = intent.getExtras();
         FoodRecord selectedFood = (FoodRecord) extras.get("selectedFood");
         
         currentMeal.add(selectedFood);
         
         } else if (resultCode == RESULT_CANCELED) {
         	// Handle cancel
         	}
       }
     }
     
     public void addFoodToFavoriteClicked(View view) {
         startActivityForResult(addFoodToFavoriteIntent, SEARCH_REQUEST_CODE);
     }
     
     public void saveFavoriteClicked(View view) {
     	// TODO push the new favorite meal to the website here - gives Communication Failed errors.
     	createNewMeal();
     	
     	if( pushMealToAddToFavorites() )   finish();
     	else { Toast.makeText(this, "Problem with pushing to website.", 
                 Toast.LENGTH_LONG).show();
     	}
     }
     
       /* -- Web site Communications -- */
     private void setupAddURL() {    	
         addURL = ADD_MEAL_TO_FAVORITES_URL;
     }
     
     private void createNewMeal() {
 //    	newMealPC = new Client();
     	newFavMeal   = new MealClient();
     	// mealClient -> getMealRecords() returns a list of Meals in format List<MealRecord>
     	
     	newFavMeal.setURL(CREATE_NEW_EMPTY_MEAL, PersistentUser.getSessionID());
     	newFavMeal.logURL();
     	newFavMeal.synchronousLoadClientData();
 
         // get new meal ID from website:
         newMealRecords = newFavMeal.getMealRecords();
         newMealID = Integer.toString( (newMealRecords.get(0)).getId() );
         
         // now we have a meal ID, and can add foods to it using 
         //  add_food_to_meal/(newMealID) for each FoodRecord object in the
         // listview that we build in this activity.
         
     }
     
     private void setupWebsiteCommunication() {
 //        pushClient = new DataPushClient();
     	mealClient = new MealClient();
         foodClient = new FoodClient();
         UpdateActionDataClientObserver updateActionObserver 
             = new UpdateActionDataClientObserver(this, mealClient);
         Toast failureToast = Toast.makeText(this, 
                 "Website Communication Failed", Toast.LENGTH_LONG);
         PushClientToastOnFailureAction action 
             = new PushClientToastOnFailureAction(failureToast);
         updateActionObserver.addAction(action);
         
         addClientObserverPair(mealClient, updateActionObserver);
     }
     
     private boolean pushMealToAddToFavorites() {
     	// Set up new empty meal slot in favorites:
 
 /*    	
     	newMealID = send "BASE_URL/create_new_meal_in_favorites/" to website, get meal ID back  ;
     	
     	 
 */    	
     	List<FoodRecord> favMealFoods = foodAdapter.getFoods();
    	List<String> addFoodArguments = new ArrayList<String>();
     	DataPushClient favoritePushClient = new DataPushClient();
         
        addFoodArguments.add(PersistentUser.getSessionID());
         for (FoodRecord food : favMealFoods) {
             Log.d("Favorite Meal Food", food.toString());
             addFoodArguments.add(food.getId() + "");
             
             favoritePushClient.setURL(
            		(ADD_FOOD_TO_THIS_FAVORITE_URL + newMealID),
             		addFoodArguments
             	);
             favoritePushClient.asynchronousLoadClientData();
             favoritePushClient.logURL();
            
            // Remove top
            addFoodArguments.remove(1);
         }
         
         boolean success = favoritePushClient.lastCompletedPushSuccessful();
         if (!success) {
         	Toast.makeText(this, "Communication Error in new foreach push to website", 
             Toast.LENGTH_LONG).show();
     	}
         
         return success;
         
 //        foodAdapter.removeCheckedFoods();
     	
     	// Will now need to pass add_food_to_meal(session_id, meal_id, food_id, quantity)
     	// There's also remove_food_from_id, which is the same but without a quantity field.
     	
     	// remove meal from favorites is done by remove_meal_from_favorites(session_id, meal_id)
 //        ArrayList<String> addFoodArguments = new ArrayList<String>();
 //        addFoodArguments.add(PersistentUser.getSessionID());
 //        addFoodArguments.add("THIS IS A TEST");
 //        addFoodArguments.add("THIS IS A SECOND TEST");
 //        addFoodArguments.add(foodToAdd.getId() + "");
 //        addFoodArguments.add(quantityField.getText().toString());      
         
 //        pushClient.setURL(addURL, addFoodArguments);
 //        pushClient.logURL();
 //        pushClient.synchronousLoadClientData();
         
 //        boolean success = pushClient.lastCompletedPushSuccessful();
 //        if (!success) {
 //            Toast.makeText(this, "Communication Error", 
 //                    Toast.LENGTH_LONG).show();
 //        }
         
 //        return success;
     }
       // -------------- end website communication --
     
     public void goHomeClicked(View view) {
         HomeBarAction.goHomeClicked(this, view);
     }
     
     public void addFavoriteClicked(View view) {
         HomeBarAction.addFavoriteClicked(this, view);
     }
     
     private Intent addFoodToFavoriteIntent;
     private String addURL;
     private String newMealID;
     private ExpandableFoodListAdapter foodAdapter;
     private List<MealRecord> newMealRecords;
     private MealClient mealClient;
     private MealClient newFavMeal;
     private DataPushClient pushClient;
 //    private DataPushClient newMealPC;
     private FoodClient foodClient;
 }
