 package edu.upenn.cis350;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class CalorieCounterActivity extends Activity {
 	
 	GameLevel level;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         level = new GameLevel(getResources());
         updateDisplayedFood(level.getCurrentFood());
     }
     
    
     public void onSubmitEvent(View view) {
     	EditText userInput = (EditText)findViewById(R.id.calorieInput);
    	// Surround in try-catch to avoid crash on unparseable input
    	int calorieGuess = -1;
    	try {
    		calorieGuess = Integer.parseInt(userInput.getText().toString());
    	} catch (NumberFormatException e) {
    		// Do nothing
    	}
     	level.enterCurrentGuess(calorieGuess);
     	if(level.hasNextFood()) {
     		level.moveToNextFood();
     		updateDisplayedFood(level.getCurrentFood());
     	}
     	else {
     		level.resetLevel(true);
     		updateDisplayedFood(level.getCurrentFood());
     	}
     }
     
     public void updateDisplayedFood(FoodItem currentFood) {
     	// Set food text, set food image, clear user input
     	TextView foodName = (TextView)findViewById(R.id.foodName);
     	foodName.setText(currentFood.getName());
     	ImageView foodImage = (ImageView)findViewById(R.id.foodImage);
     	foodImage.setImageDrawable(currentFood.getImage());
     	EditText calorieGuess = (EditText)findViewById(R.id.calorieInput);
     	calorieGuess.setText("");
     }
     
     public void onQuitEvent(View view) {
     	finish();
     }
 }
