 package com.gmail.samos6.samscookbook;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.SeekBar;
 import android.widget.SlidingDrawer;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 @SuppressWarnings("deprecation")
 public class RecipeViewActivity extends Activity implements OnSeekBarChangeListener {
 	
 
 	TextView txtRecipeName;
 	TextView txtServings;
 	TextView txtAuthor;
 	TextView txtNumReviews;
 	TextView txtIngredientList;
 	TextView txtCookingDirections;
 	TextView txtCookTime;
 	TextView txtPrepTime;
 	RatingBar rtbRating;
 	ImageView imgPicture;
 	SeekBar seekBar;
 	SlidingDrawer slidingDrawer;
 	Button btnReviews;
 	Button btnSave;
 	Button btnFavorite;
 	Button btnEdit;
 	Button btnTimer;
 	Button handle;
 	
 	//preference access
 	SharedPreferences prefs;
 	String userName="";
 	String firstName="";
 	String token="";
 	
 	String recipeName;
 	String author= "";
 	String servings= "";
 	String numRatings= "";
 	String ingredientList= "";
 	String cookingDirections= "";
 	String rating= "";
 	String cookTime= "Cook time: ";
 	String prepTime= "Prep time: ";
 	String rawImage= "";
 	String recipeId= "";
 	
 	int ingredientNum;
 	String[][] ingredientArray;
 	String [][] tempArray;
 	
 	//check to see if pic exist
 	int hasImage=0;
 	
 	//used to set font
 	Typeface typeFace; 
 	
 	// single ingredient url
 	String urlGetRecipeDetails;	
 	String urlRoot;				//stores the root url
     String imageUrl = "";		//stores the file path
     
     // ImageLoader class instance
     Images_ImageLoader imgLoader;
     NutritionDecripter nutrition;
 	
 	//used to see if user canceled the AsyncTask
     Boolean bCancelled=false;
     Boolean successful=false;
 	String message="";
 
 	// Progress Dialog
 	private ProgressDialog pDialog;
 
 	// JSON parser class
 	JSONParser jsonParser = new JSONParser();
 
 	//Instantiating the SQLite database
 	final DatabaseHandler db = new DatabaseHandler(this);
 	
 	// JSON Node names
 	private static final String TAG_SUCCESS = "success";
 	private static final String TAG_MESSAGE = "message";
 	private static final String TAG_PRODUCT = "product";
 	private static final String TAG_RECIPENAME = "recipeName";
 	private static final String TAG_RECIPEID = "recipeId";
 	private static final String TAG_AUTHOR = "author";
 	private static final String TAG_NUMRATINGS = "numRatings";
 	private static final String TAG_INGREDIENTLIST = "ingredientList";
 	private static final String TAG_COOKINGDIRECTIONS = "cookingDirections";
 	private static final String TAG_RATING = "rating";
 	private static final String TAG_COOKTIME = "cookTime";
 	private static final String TAG_PREPTIME = "prepTime";
 	private static final String TAG_SERVINGS = "servings";
 	private static final String TAG_IMAGEURL = "imageUrl";
 	private static final String TAG_HASIMAGE = "hasImage";
 	private static final String TAG_PROTEIN = "protein";
 	private static final String TAG_CALORIES = "calories";
 	private static final String TAG_CARBS = "carbs";
 	private static final String TAG_FAT = "fat";
 	private static final String TAG_TYPE = "type";
 	private static final String TAG_DESCRIPTION = "description";
 	
 	private static final int INGREDIENTNAME = 0;
 	private static final int INGREDIENTID = 1;
 	private static final int AMOUNT = 2;
 	private static final int MEASUREMENT = 3;
 	private static final int CALORIES = 4;
 	private static final int PROTEIN = 5;
 	private static final int FAT = 6;
 	private static final int CARBS = 7;
 	private static final int TYPE = 8;
 	private static final int DESCRIPTION = 9;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.view_recipe);
 		
 		imgLoader = new Images_ImageLoader(getApplicationContext());
 		nutrition = new NutritionDecripter();
 		
 		//setting user name and password from preferences
 		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		userName =prefs.getString("nickName", "guest");
 		firstName =prefs.getString("firstName", "guest");
 		token =prefs.getString("token", "");
 		
 		//getting url from resources
 		urlGetRecipeDetails = getResources().getString(R.string.urlGetRecipeDetails);
 		urlRoot = getResources().getString(R.string.urlRoot);
 		
 		// getting ingredient details from intent
 		Intent intent = getIntent();
 		
 		// getting data past from intent
 		recipeName = intent.getStringExtra(TAG_RECIPENAME);
 			
 		// Initializing all of the text fields and buttons
 		btnReviews = (Button) findViewById(R.id.btnRecipeViewViewRatings);
 		btnSave = (Button) findViewById(R.id.btnRecipeViewSaveRecipe);
 		btnFavorite = (Button) findViewById(R.id.btnRecipeViewFavoriteRecipe);
 		btnEdit = (Button) findViewById(R.id.btnRecipeViewEdit);
 		btnTimer = (Button) findViewById(R.id.btnRecipeViewGetTimer);
 		handle = (Button) findViewById(R.id.handle);
 		
 		txtRecipeName = (TextView) findViewById(R.id.txtRecipeViewRecipeName);
 		txtServings = (TextView) findViewById(R.id.txtRecipeViewServings);
 		txtAuthor = (TextView) findViewById(R.id.txtRecipeViewAuthor);
 		txtNumReviews= (TextView) findViewById(R.id.txtRecipeViewNumReviews);
 		txtIngredientList= (TextView) findViewById(R.id.txtRecipeViewIngredientList);
 		txtCookingDirections= (TextView) findViewById(R.id.txtRecipeViewDirections);
 		txtCookTime= (TextView) findViewById(R.id.txtRecipeViewCooktime);
 		txtPrepTime= (TextView) findViewById(R.id.txtRecipeViewPreptime);
 		imgPicture= (ImageView) findViewById(R.id.recipeViewImage);	
 		slidingDrawer= (SlidingDrawer) findViewById(R.id.recipeViewSlidingDrawer);
 		rtbRating= (RatingBar) findViewById(R.id.recipeViewRatingBar);
 		seekBar= (SeekBar) findViewById(R.id.seekBarRecipeView);
 			
 		//setting the font type from assets		
 		typeFace = Typeface.createFromAsset(getAssets(), "fonts/KELMSCOT.ttf");
 		btnReviews.setTypeface(typeFace);
 		btnSave.setTypeface(typeFace);
 		btnFavorite.setTypeface(typeFace);
 		btnEdit.setTypeface(typeFace);
 		btnTimer.setTypeface(typeFace);
 		txtRecipeName.setTypeface(typeFace);
 		txtServings.setTypeface(typeFace);
 		txtAuthor.setTypeface(typeFace);
 		txtNumReviews.setTypeface(typeFace);
 		txtIngredientList.setTypeface(typeFace);
 		txtCookingDirections.setTypeface(typeFace);
 		txtCookTime.setTypeface(typeFace);
 		txtPrepTime.setTypeface(typeFace);
 
         ((TextView)findViewById(R.id.handle)).setTypeface(typeFace);
         ((TextView)findViewById(R.id.tv1)).setTypeface(typeFace);
         ((TextView)findViewById(R.id.tv2)).setTypeface(typeFace);
         ((TextView)findViewById(R.id.tv3)).setTypeface(typeFace);
         ((TextView)findViewById(R.id.tv4)).setTypeface(typeFace);
         ((TextView)findViewById(R.id.tv5)).setTypeface(typeFace);
         ((TextView)findViewById(R.id.tv6)).setTypeface(typeFace);
 		
 		
 		//log.d("ViewRecipe_just in", recipeName);
 
 		new GetRecipeDetails().execute();
 		
 		seekBar.setOnSeekBarChangeListener(this);
 		
         slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
             @Override
             public void onDrawerOpened() {
             	handle.setBackgroundResource(R.drawable.chevron_down);
             }
         });
  
         slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
             @Override
             public void onDrawerClosed() {
             	handle.setBackgroundResource(R.drawable.chevron);
             }
         });
         
         
 		// See Reviews button click event
 		btnReviews.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				
 				if(!userName.equalsIgnoreCase("guest")){
 					//log.d("ViewRecipe_btnReviews onclick", "inside");
 					Intent intent = new Intent(getApplicationContext(), ListRecipeCommentsActivity.class);
 					
 					// sending recipeName to next activity
 					intent.putExtra(TAG_RECIPENAME, recipeName);
 					
 					// starting new activity and expecting some response back
 					startActivity(intent);
 				}else{
 					Toast.makeText(getApplicationContext(), getString(R.string.pLoginInFirst), Toast.LENGTH_LONG).show();
 				}
 
 			}
 		});
 		
 		// Save Recipe For latter button click event
 		btnSave.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				
 				//log.d("ViewRecipe_btnSave onclick", "inside");
 				db.addSavedRecipe(recipeId);
 				Toast.makeText(getApplicationContext(), recipeName+" "+getString(R.string.hasBeenSaved), Toast.LENGTH_LONG).show();
 
 			}
 		});
 		
 		// Favorite Recipe button click event
 		btnFavorite.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				
 				//log.d("ViewRecipe_btnFavorite onclick", "inside");
 				db.addFavoriteRecipe(recipeId);
 				Toast.makeText(getApplicationContext(), recipeName+" "+getString(R.string.hasBeenFav), Toast.LENGTH_LONG).show();
 
 			}
 		});
 		
 		// Favorite Recipe button click event
 		btnTimer.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 						
 				//log.d("CreateRecipe_btnTakePhoto onclick", "inside");
 				PackageManager pm = getPackageManager();
 				Intent intent = pm.getLaunchIntentForPackage("com.google.android.deskclock");
 				startActivity(intent);
 
 			}
 		});
 		
 		// Favorite Recipe button click event
 		btnEdit.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				
 				//log.d("ViewRecipe_btnEdit onclick", "inside");
 				
 				if(userName.equalsIgnoreCase(author)){
 					Intent intent = new Intent(getApplicationContext(), EditRecipeActivity.class);
 					
 					// sending recipeName to next activity
 					intent.putExtra(TAG_RECIPENAME, recipeName);
 					
 					//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 					// starting new activity and expecting some response back
 					startActivityForResult(intent, 100);
 				}else{
 					Toast.makeText(getApplicationContext(), getString(R.string.sorry)+" "+userName+" "+getString(R.string.only)+" "+author+" "+getString(R.string.canEditThis), Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 
 	}
 	
 	// Initiating Menu XML file (menu.xml)
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
         MenuInflater menuInflater = getMenuInflater();
         menuInflater.inflate(R.menu.recipe_view_menu, menu);
         return true;
     }
 	
 	// Response from EditRecipe Activity
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		// if result code 100
 		if (resultCode == 100) {
 			// if result code 100 is received 
 			// means user may have changed the recipe
 			// reload this screen again
 			Intent intent = getIntent();
 			recipeName = intent.getStringExtra(TAG_RECIPENAME);
 			finish();
 			startActivity(intent);
 		}
 
 	}
 	
 	
 	/***************************************************************************************************
 	 *  									void addDetails()
 	 *  
 	 *  			This method adds all of the recipe details from the query. 
 	 * 
 	 ****************************************************************************************************/
 	public void addDetails(){
 
 		seekBar.setMax((Integer.parseInt(servings)*4)-1);
 		seekBar.setProgress(Integer.parseInt(servings)-1);
 		
 		//log.d("RecipeView_addDetails image=", rawImage);
 		txtRecipeName.setText(recipeName);
 		txtAuthor.setText(author);
 		txtNumReviews.setText(numRatings);
 		txtIngredientList.setText(getIngredientList(ingredientArray));
		txtCookingDirections.setText(cookingDirections);
 		txtCookTime.setText(cookTime);
 		txtPrepTime.setText(prepTime);
 		txtServings.setText(servings);
 		if(hasImage==1)
 			imgLoader.DisplayImage(urlRoot+imageUrl, imgPicture);
 		rtbRating.setRating(Float.valueOf(rating)/2);
 		
 	}
 	
 	private String getIngredientList(String[][] array){
 		String ingredientList="";
 		
 		String measurement="";
 
 		for(int i=0; i<array.length; i++){
 			
 			measurement = array[i][MEASUREMENT];
 			//adds the plural's' to the end of measurement
 			if(nutrition.amountToFloat(array[i][AMOUNT])>1)
 				measurement+="s";
 			
 			//screens the possibility that no unit is needed
 			if(measurement.equalsIgnoreCase("no unit")||measurement.equalsIgnoreCase("no units"))
 				measurement="";
 			else
 				measurement+=" of";
 			
 			
 			ingredientList +="> "+array[i][AMOUNT]+" "+measurement+" "
 							+array[i][DESCRIPTION]+" "+array[i][INGREDIENTNAME]+"\n";
 		}	
 		
 		return ingredientList;
 	}
 	
 	/**
 	 * Turns a String into <hours> h, <minutes> min
 	 * @param time string (in minutes)
 	 * @return <hours> h, <minutes> min
 	 */
 	public String getTimeInMH(String st){
 		
 		int t = Integer.parseInt(st);
 	     //calculating minutes and hours
 	     int hours = t / 60; 
 	     int minutes = t % 60;
 	     String time="";
 	     	if(hours==0)
 	     		time= minutes+" min";
 	     	else
 	     		time= hours+" h, "+minutes+" min";
 		return time;
 	}
 	
 	/**
 	 * Enables user to cancel the AsychTask by hitting the back button
 	 */
 	OnCancelListener cancelListener=new OnCancelListener(){
 	    @Override
 	    public void onCancel(DialogInterface arg0){
 	    	//used to see if user canceled the AsyncTask
 	    	bCancelled=true;
 	        finish();
 	    }
 	};
 
 	/**
 	 * Background Async Task to Get complete recipe details
 	 * */
 	class GetRecipeDetails extends AsyncTask<String, String, String> {
 
 		/**
 		 * Before starting background thread Show Progress Dialog
 		 * */
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			pDialog = new ProgressDialog(RecipeViewActivity.this);
 			pDialog.setMessage(getString(R.string.loadingRecipeDetails));
 			pDialog.setIndeterminate(false);
 			pDialog.setCancelable(true);
 			pDialog.setOnCancelListener(cancelListener);
 			pDialog.show();
 		}
 
 		/**
 		 * Getting product details in background thread
 		 * */
 		@Override
 		protected String doInBackground(String... args) {
 
 					// Check for success tag
 					int success;
 					//if the AsyncTask has Not been cancelled then continue
 					if(!bCancelled) try {
 						// Building Parameters
 						List<NameValuePair> params = new ArrayList<NameValuePair>();
 						params.add(new BasicNameValuePair(TAG_RECIPENAME, recipeName));
 
 						// getting Ingredient details by making HTTP request
 						JSONObject json = jsonParser.makeHttpRequest( urlGetRecipeDetails, "POST", params);
 
 						// check your log for json response
 						//log.d("Single recipe Details", json.toString());
 						
 						//reset variable just in case
 						successful=false;
 						
 						// json success tag
 						success = json.getInt(TAG_SUCCESS);
 						if (success == 1) {
 							
 							successful=true;
 							// successfully received product details
 							JSONArray products = json.getJSONArray(TAG_PRODUCT); // JSON Array
 							
 							// get first ingredient object from JSON Array
 							JSONObject product = products.getJSONObject(0);
 
 							//Getting details from the query
 							//log.d("RecipeView_DoinBackGround", "setting all of the details");
 							author = product.getString(TAG_AUTHOR);
 							numRatings = product.getString(TAG_NUMRATINGS);
 							//ingredientList = product.getString(TAG_INGREDIENTLIST);
 							cookingDirections = product.getString(TAG_COOKINGDIRECTIONS);
 							rating = product.getString(TAG_RATING);
 							prepTime += getTimeInMH(product.getString(TAG_PREPTIME));
 							cookTime += getTimeInMH(product.getString(TAG_COOKTIME));
 							imageUrl = product.getString(TAG_IMAGEURL);
 							servings = product.getString(TAG_SERVINGS);
 							recipeId = product.getString(TAG_RECIPEID);
 							hasImage = Integer.parseInt(product.getString(TAG_HASIMAGE));
 							
 							ingredientNum = Integer.parseInt(product.getString("numIngredients"));
 							
 							ingredientArray = new String [ingredientNum][10];
 							
 							//getting nutrition info from db
 							for(int i=0; i<ingredientNum; i++){
 								
 								ingredientArray[i][INGREDIENTNAME] = product.getString("ingredientName"+i);
 								ingredientArray[i][INGREDIENTID] = product.getString("ingredientId"+i);
 								ingredientArray[i][AMOUNT] = product.getString("amount"+i);
 								ingredientArray[i][MEASUREMENT] = product.getString("measurement"+i);
 								ingredientArray[i][CALORIES] = product.getString(TAG_CALORIES+i);
 								ingredientArray[i][FAT] = product.getString(TAG_FAT+i);
 								ingredientArray[i][PROTEIN] = product.getString(TAG_PROTEIN+i);
 								ingredientArray[i][CARBS] = product.getString(TAG_CARBS+i);
 								ingredientArray[i][TYPE] = product.getString(TAG_TYPE+i);
 								ingredientArray[i][DESCRIPTION] = product.getString(TAG_DESCRIPTION+i);
 								
 							}
 
 							//log.d("recipeView ingredientNum:", Integer.toString(ingredientNum));
 
 						}else{	
 							// recipe with that name not found
 							message= json.getString(TAG_MESSAGE);
 							//log.d("Recipe View failed: ", message);
 						}
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 
 					
 					//log.d("RecipeView_DoinBackGround", "before return");
 			return null;
 		}
 
 
 		/**
 		 * After completing background task Dismiss the progress dialog
 		 * **/
 		@Override
 		protected void onPostExecute(String file_url) {
 			//log.d("RecipeView_PostExecute", "in post execute");
 			//is called to add all of the details to the fields
 			addDetails();
 			// dismiss the dialog once got all details
 			pDialog.dismiss();
 		}
 	}
 	
 	
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {		
 		tempArray= stringCopy(ingredientArray, ingredientNum);
 		
 		
 		//inc progress b/c we dont want it to be 0
 		progress++;
 		
 		if(progress>0){
 			
 			txtServings.setText(Integer.toString(progress));
 			for(int i=0; i<tempArray.length;i++){
 				float am = nutrition.amountToFloat(tempArray[i][AMOUNT]);
 				float serv= progress/Float.valueOf(servings);
 				//Log.d("seekbar in for", "am= "+am+", serv="+serv);
 				tempArray[i][AMOUNT]=nutrition.amountToString( am *serv );	
 			}
 			
 			txtIngredientList.setText(getIngredientList(tempArray));
 		}
 		
 	}
 	
 	private String[][] stringCopy(String[][] array, int size){
 		
 		String [][] newArray= new String[size][10];
 		
 		for(int i=0; i<array.length;i++){
 			newArray[i][INGREDIENTNAME] = array[i][INGREDIENTNAME];
 			newArray[i][INGREDIENTID] = array[i][INGREDIENTID];
 			newArray[i][AMOUNT] = array[i][AMOUNT];
 			newArray[i][MEASUREMENT] = array[i][MEASUREMENT];
 			newArray[i][CALORIES] = array[i][CALORIES];
 			newArray[i][PROTEIN] = array[i][PROTEIN];
 			newArray[i][FAT] = array[i][FAT];
 			newArray[i][CARBS] = array[i][CARBS];
 			newArray[i][TYPE] = array[i][TYPE];
 			newArray[i][DESCRIPTION] = array[i][DESCRIPTION];
 		}
 		
 		return newArray;
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 
 	 @Override
 	    public boolean onOptionsItemSelected(MenuItem item){
 	 
 	        switch (item.getItemId()){
 	 
 	        case R.id.menuHome:
 	        	Intent i = new Intent(getApplicationContext(), MainScreenActivity.class);
 				// Closing all previous activities
 				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(i);
 	            return true;
 	            
 	        case R.id.share: 	            //					market://details?id=com.example.android.jetboy
 	            String message = firstName+" "+getString(R.string.likes)+" '"+recipeName+"' "+getString(R.string.from)+" "+getString(R.string.app_name)+" @ http://bit.ly/Y6SR2b";
         		Intent share = new Intent(Intent.ACTION_SEND);
         		share.setType("text/plain");
         		share.putExtra(Intent.EXTRA_TEXT, message);
         		//share.putExtra(Intent.EXTRA_SUBJECT, message);
 
         		startActivity(Intent.createChooser(share, getString(R.string.howShare)));
 	            return true;
 	            
 	        case R.id.nutritionInfo:
 	        	
 	        	Map<String, Float> info = new HashMap<String, Float>(); 
 				
 	        	float sumProtein= 0.0f;
 	        	float sumFat= 0.0f;
 	        	float sumCalorie= 0.0f;
 	        	float sumCarbs= 0.0f;
 	        	for(int n=0; n<tempArray.length; n++){
 	        		info = nutrition.getIngredientFacts(tempArray[n][MEASUREMENT], tempArray[n][TYPE], 
 	        				tempArray[n][AMOUNT], tempArray[n][CALORIES], 
 	        				tempArray[n][FAT], tempArray[n][CARBS], tempArray[n][PROTEIN]);
 	        		
 	        		
 	        		sumCalorie += info.get(TAG_CALORIES);
 	        		sumFat += info.get(TAG_FAT);
 	        		sumProtein += info.get(TAG_PROTEIN);
 	        		sumCarbs += info.get(TAG_CARBS);
 	        		Log.d("recipeView, getNut", "sumCalorie:"+sumCalorie+", sumFat:"+sumFat+", sumProtein:"+sumProtein+", sumCarbs:"+sumCarbs);
 	        	}
 	        	
 	        	String serv = txtServings.getText().toString();
 	        	
 	        	Intent intent = new Intent(getApplicationContext(), NutritionInfoActivity.class);
 	    		intent.putExtra("calories", sumCalorie);
 	    		intent.putExtra("fat", sumFat);
 	    		intent.putExtra("protein", sumProtein);
 	    		intent.putExtra("servings", serv);
 	    		intent.putExtra("carbs", sumCarbs);
 				startActivity(intent);
 	            return true;
 	 
 	        default:
 	            return super.onOptionsItemSelected(item);
 	        }
 	    }
 
 }
