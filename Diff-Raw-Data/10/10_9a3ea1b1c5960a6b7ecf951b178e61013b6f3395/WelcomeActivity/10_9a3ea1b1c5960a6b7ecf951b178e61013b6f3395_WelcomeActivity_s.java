 package capsulemanager.arpia49.com;
 
 import java.io.IOException;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class WelcomeActivity extends Activity {
 	final Random myRandom = new Random();
 	SharedPreferences sp = null;
 	Button buttonGenerate;
 	Button buttonList;
 	Button buttonTake;
 	TextView capsuleName;
 	TextView capsuleTotal;
 	TextView capsuleDescription;
 	TextView capsuleIntensity;
 	TextView capsuleSuggestions;
 	String lastCapsuleStr;
     DataBaseHelper myDbHelper = new DataBaseHelper(this);
     String name;
     int number;
     public final int INVISIBLE = 4;
     public final int VISIBLE = 0;
     
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
         try {
         	myDbHelper.createDataBase();
         }
         catch (IOException ioe) {
         	throw new Error("Unable to create database");
         }
         try {
         	myDbHelper.openDataBase();
         }catch(SQLException sqle){
         	throw sqle;
         }
 		sp = PreferenceManager.getDefaultSharedPreferences(this);
 		buttonGenerate = (Button) findViewById(R.id.buttonRandom);
 		buttonList = (Button) findViewById(R.id.buttonEditStock);
 		buttonTake = (Button) findViewById(R.id.buttonTakeIt);
 
 		capsuleName = (TextView) findViewById(R.id.capsuleName);
 		capsuleTotal = (TextView) findViewById(R.id.capsuleTotal);
 		capsuleDescription = (TextView) findViewById(R.id.capsuleDescription);
 		capsuleIntensity = (TextView) findViewById(R.id.capsuleIntensity);
 		capsuleSuggestions = (TextView) findViewById(R.id.capsuleSuggestions);
 		lastCapsuleStr = sp.getString("lastCapsuleStr", "");
 		updateScreen(true, myDbHelper.coffeeInfo(lastCapsuleStr));
 
 		buttonGenerate.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				buttonTake.setVisibility(0);
 				updateScreen(false, myDbHelper.randomCoffee());
 			}
 		});
 
 		buttonList.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent myIntent = new Intent(WelcomeActivity.this,
 						CapsulesListActivity.class);
 				WelcomeActivity.this.startActivity(myIntent);
 			}
 		});
 
 		buttonTake.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 		        try {
 		        	myDbHelper.updateDataBase();
 					number--;
 					myDbHelper.updateContent(name, number);
 					capsuleTotal.setText(getString(R.string.capsuleTotal) + " "
 							+ number);
 					buttonTake.setVisibility(4);
 					SharedPreferences.Editor editor = sp.edit();
 					editor.putString("lastCapsuleStr", name);
 					editor.commit();
 		        }catch(SQLException sqle){
 		        	throw sqle;
 		        }
 			}
 		});
 	}
 	
 	void updateScreen(boolean remember, Cursor cursor){
 		lastCapsuleStr = sp.getString("lastCapsuleStr", "");
 		String suggestions = "";
 		if (cursor.getCount() != 0) {
 			if (cursor.moveToFirst()) {
 				capsuleDescription.setVisibility(VISIBLE);
 				capsuleTotal.setVisibility(VISIBLE);
 				capsuleIntensity.setVisibility(VISIBLE);
 				capsuleSuggestions.setVisibility(VISIBLE);
 				number = cursor.getInt(cursor.getColumnIndex("total"));
 				name = cursor.getString(cursor.getColumnIndex("name"));
				if(remember){
					name = this.getString(R.string.lastCoffeeText) + " " +name;
				}
				capsuleName.setText(name);
 				capsuleTotal.setText(getString(R.string.capsuleTotal) + " "
 						+ number);
 				if(cursor.getInt(cursor.getColumnIndex("category")) != 9){
 					String description = (String) getResources().getText(
 							getResources().getIdentifier(name.replace(" ", "_"),
 									"string", "capsulemanager.arpia49.com"));
 					capsuleDescription.setText(getString(R.string.capsuleDescription)
 							+ " " + description);
 				}
 				else{
 					capsuleDescription.setText(getString(R.string.capsuleDescription)
 							+ " " + cursor.getString(cursor.getColumnIndex("description")));
 				}
 				capsuleIntensity.setText(getString(R.string.capsuleIntensity) + " "
 						+ cursor.getInt(cursor.getColumnIndex("intensity")));
 				if (cursor.getInt(cursor.getColumnIndex("milk")) == 1) {
 					suggestions = getString(R.string.milk);
 				}
 				if (cursor.getInt(cursor.getColumnIndex("ristretto")) == 1) {
 					suggestions += " " + getString(R.string.ristretto);
 				}
 				if (cursor.getInt(cursor.getColumnIndex("espresso")) == 1) {
 					suggestions += " " + getString(R.string.espresso);
 				}
 				if (cursor.getInt(cursor.getColumnIndex("lungo")) == 1) {
 					suggestions += " " + getString(R.string.lungo);
 				}
 				capsuleSuggestions.setText(getString(R.string.capsuleSuggestions)
 						+ " " + suggestions.trim().replace(" ", ", "));
 				if (cursor != null && !cursor.isClosed()) {
 					cursor.close();
 				}
 			}
 		}else{
 			capsuleName.setText(R.string.welcomeText);
 			capsuleDescription.setVisibility(INVISIBLE);
 			capsuleTotal.setVisibility(INVISIBLE);
 			capsuleIntensity.setVisibility(INVISIBLE);
 			capsuleSuggestions.setVisibility(INVISIBLE);
 		}
 	}
 }
