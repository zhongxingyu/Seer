 package uw.cse403.minion;
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Debug;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Build;
 
 /**
  * CharCreateMainActivity is an activity that provides the user a UI with which
  * they can access all the individual elements of their character. It also allows
  * them to save their character to a remote database.
  * @author Elijah Elefson (elefse)
  */
 public class CharCreateMainActivity extends Activity {
 	/** Class constants for string representations **/
 	private static final String CHARACTER_ID = "cid";
 	private static final String PHP_ADDRESS = "http://homes.cs.washington.edu/~elefse/uploadCharacter.php";
 
 	/** The current user's username **/
 	private String username;
 
 	/** The unique id for a character **/
 	private long charID;
 
 	/*
 	 * Testing Results:
 	 * Lots of CPU time is spent drawing the graphics, with little spent on the rest of our app.
 	 * Nothing to be optimized.
 	 */
 	/**
 	 * Displays the character creation main page.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		if (TraceControl.TRACE)
 			Debug.startMethodTracing("CharCreateMainActivity_onCreate");
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_char_create_main);
 		username = SaveSharedPreference.getPersistentUserName(CharCreateMainActivity.this);
 		try {
 			charID = this.getIntent().getExtras().getLong(CHARACTER_ID);
 		} catch (Exception e) {
 			charID = CharacterDataSource.getNewID();
 			// disable buttons until Basic Info is completed.
 			Button b1 = (Button)findViewById(R.id.ability_scores); 
 			Button b2 = (Button)findViewById(R.id.skills); 
 			Button b3 = (Button)findViewById(R.id.combat); 
 			Button b4 = (Button)findViewById(R.id.saving_throws); 
 			Button b5 = (Button)findViewById(R.id.done_button); 
 			b1.setEnabled(false);
 			b2.setEnabled(false);
 			b3.setEnabled(false);
 			b4.setEnabled(false);
 			b5.setEnabled(false);
 		}
 		// Show the Up button in the action bar.
 		setupActionBar();
 		
 		if (TraceControl.TRACE)
 			Debug.stopMethodTracing();
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	/**
 	 * Creates Options Menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.char_create_main, menu);
 		return true;
 	}
 
 	/**
 	 * Sets up the Up button
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * Repsonds to button click and goes to the Basic Information UI.
 	 * @param view The current view
 	 */
 	public void gotoBasicInfo(View view) {
 		Intent intent = new Intent(this, BasicInfoActivity.class);
 		intent.putExtra(CHARACTER_ID, charID);
 		startActivity(intent);
 	}
 
 	/**
 	 * Repsonds to button click and goes to the Ability Scores UI.
 	 * @param view The current view
 	 */
 	public void gotoAbilityScores(View view) {
 		Intent intent = new Intent(this, AbilityScoresActivity.class);
 		intent.putExtra(CHARACTER_ID, charID);
 		startActivity(intent);
 	}
 
 	/**
 	 * Repsonds to button click and goes to the Skills UI.
 	 * @param view The current view
 	 */
 	public void gotoSkills(View view) {
 		Intent intent = new Intent(this, SkillsActivity.class);
 		intent.putExtra(CHARACTER_ID, charID);
 		startActivity(intent);
 	}
 
 	/**
 	 * Repsonds to button click and goes to the Combat UI.
 	 * @param view The current view
 	 */
 	public void gotoCombat(View view) {
 		Intent intent = new Intent(this, CombatActivity.class);
 		intent.putExtra(CHARACTER_ID, charID);
 		startActivity(intent);
 	}
 
 	/**
 	 * Repsonds to button click and goes to the Saving Throws UI.
 	 * @param view The current view
 	 */
 	public void gotoSavingThrows(View view) {
 		Intent intent = new Intent(this, SavingThrowsActivity.class);
 		intent.putExtra(CHARACTER_ID, charID);
 		startActivity(intent);
 	}
 
 	/**
 	 * Repsonds to the Done button click by uploading the character data to the
 	 * remote database and sending the user back to the Main Character Creation page.
 	 * @param view The current view
 	 */
 	public void gotoCharacterList(View view) {
 		// Checks for internet connectivity
 		if (ConnectionChecker.hasConnection(this)) {
 			// Updates login credentials on remote database
 			UploadCharacterTask task = new UploadCharacterTask(this);
 			task.execute(username);
 		} else {
 			Toast.makeText(getApplicationContext(), "No network available", Toast.LENGTH_LONG).show();
 		}
 	}
 
 
 	/**
 	 * UploadCharacterTask is a private inner class that allows requests to be made to the remote
 	 * MySQL database parallel to the main UI thread. It uploads any currently filled out character
 	 * information to the remote database for storage.
 	 */
 	private class UploadCharacterTask extends AsyncTask<String, Void, String> {
 		private Context context;
 		private ProgressDialog dialog;
 
 		/**
 		 * Constructs a new UploadCharacterTask object.
 		 * @param context The current Activity's context
 		 */
 		private UploadCharacterTask (Context context) {
 			this.context = context;
 			dialog = new ProgressDialog(context);
 		}
 
 		/**
 		 * Begins a dialog to show that the character is uploading.
 		 */
 		@Override
 		protected void onPreExecute() {
 			this.dialog.setMessage("Uploading character...");
 			this.dialog.show();
 		}
 
 		/**
 		 * Makes the HTTP request and returns the result as a String.
 		 */
 		@Override
 		protected String doInBackground(String... args) {
 
 			// Creates JSON object for basic character information
 			CharacterDescription baseInfo = new CharacterDescription(charID);
 			JSONObject basicInfo = new JSONObject();
 			try {
 				long id = charID;
 				basicInfo.put("local_id", id);
 				basicInfo.put("name", baseInfo.name);
 
 				basicInfo.put("alignment", baseInfo.alignment);
 				basicInfo.put("level", baseInfo.level);
 				basicInfo.put("diety", baseInfo.deity);
 				basicInfo.put("homeland", baseInfo.homeLand);
 				basicInfo.put("race", baseInfo.race);
 				basicInfo.put("size", baseInfo.size);
 				basicInfo.put("gender", baseInfo.gender);
 				basicInfo.put("age", baseInfo.age);
 				basicInfo.put("height", baseInfo.height);
 				basicInfo.put("weight", baseInfo.weight);
 				basicInfo.put("hair", baseInfo.hair);
 				basicInfo.put("eyes", baseInfo.eyes);
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			// Creates JSON object for character abilities
 			Ability[] abilities = new Ability[6];
 			abilities[0] = new Ability(charID, 0);
 			abilities[1] = new Ability(charID, 1);
 			abilities[2] = new Ability(charID, 2);
 			abilities[3] = new Ability(charID, 3);
 			abilities[4] = new Ability(charID, 4);
 			abilities[5] = new Ability(charID, 5);
 
 			JSONObject abilityObject = new JSONObject();
 			JSONArray abilityScores = new JSONArray();
 			try {
 				for(int i = 0; i < 6; i++) {
 					JSONObject ability = new JSONObject();
 					ability.put("score", abilities[i].getTotal());
 					ability.put("mod", abilities[i].getMod());
 					abilityScores.put(ability);
 				}
 				abilityObject.put("abilities", abilityScores);
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}    	
 
 			// Creates JSON object for character skills
 			Cursor cursor = SQLiteHelperSkills.db.query(SQLiteHelperSkills.TABLE_NAME, SQLiteHelperSkills.ALL_COLUMNS, 
 					SQLiteHelperSkills.COLUMN_CHAR_ID + " = " + charID, null, null, null, null);
 			JSONObject skillsObject = new JSONObject();
 			JSONArray skills = new JSONArray();
 			try {
 				if (cursor.moveToFirst()) {
 					while (!cursor.isAfterLast()) { 
 						// Columns: COLUMN_CHAR_ID, COLUMN_REF_S_ID, COLUMN_RANKS, COLUMN_MISC_MOD
 						int skillID = cursor.getInt(1);
 						int ranks = cursor.getInt(2);
 						int miscMod = cursor.getInt(3);
 						JSONObject skill = new JSONObject();
 						if(skillID == 5 || skillID == 26 || skillID == 27) {
 							String skillTitle = cursor.getString(2);
 							skill.put("title", skillTitle);
 						}
 						skill.put("ref_id", skillID);
 						skill.put("ranks", ranks);
 						skill.put("misc_mod", miscMod);
 						skills.put(skill);
 						cursor.moveToNext();
 					}
 					skillsObject.put("skills", skills);
 				}
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			cursor.close();
 			
 			try {
 				if (skills.toString().equals("[]")) {
 					skillsObject = new JSONObject();
 					skills = new JSONArray();
 					for(int i = 1; i <= 35; i++) {
 						int skillID = i;
 						int ranks = 0;
 						int miscMod = 0;
 						if(skillID == 5) {
 							for(int j = 0; j < 3; j++) {
 								JSONObject skill = new JSONObject();
 								skill.put("ref_id", skillID);
 								skill.put("ranks", ranks);
 								skill.put("misc_mod", miscMod);
 								skills.put(skill);
 							}
 						} else if (skillID == 26 || skillID == 27) {
 							for(int j = 0; j < 2; j++) {
 								JSONObject skill = new JSONObject();
 								skill.put("ref_id", skillID);
 								skill.put("ranks", ranks);
 								skill.put("misc_mod", miscMod);
 								skills.put(skill);
 							}
 						} else {
 							JSONObject skill = new JSONObject();
 							skill.put("ref_id", skillID);
 							skill.put("ranks", ranks);
 							skill.put("misc_mod", miscMod);
 							skills.put(skill);
 						}
 					}
 					skillsObject.put("skills", skills);
 				}
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			// Creates JSON object for character combat scores
 			Combat combat = new Combat(charID);
 			JSONObject combatObject = new JSONObject();
 			try {
 				combatObject.put("hp_total", combat.baseHP);
 				combatObject.put("hp_dr", combat.damageReduction);
 				combatObject.put("speed_base", combat.speedBase);
 				combatObject.put("speed_armor", combat.speedArmor);
 				combatObject.put("init_misc_mod", combat.initModifier);
 				combatObject.put("base_attack_bonus", combat.bAb);
 
 				combatObject.put("armor_bonus", combat.getArmorModifier("armorBonus"));
 				combatObject.put("shield_bonus", combat.getArmorModifier("armorShield"));
 				combatObject.put("nat_armor", combat.getArmorModifier("armorNatural"));
 				combatObject.put("deflection_mod", combat.getArmorModifier("armorDeflection"));
 				combatObject.put("misc_mod", combat.getArmorModifier("armorMisc"));
 
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			// Creates JSON object for character saving throws
 			Cursor cursor2 = SQLiteHelperSavingThrows.db.query(SQLiteHelperSavingThrows.TABLE_NAME, SQLiteHelperSavingThrows.ALL_COLUMNS, 
 					SQLiteHelperSavingThrows.COLUMN_CHAR_ID + " = " + charID, null, null, null, null);
 			JSONObject savingThrowsObject = new JSONObject();
 			JSONArray savingThrows = new JSONArray();
 			try {
 				if (cursor2.moveToFirst()) {
 					while (!cursor2.isAfterLast()) { 
 						// Columns: COLUMN_CHAR_ID, COLUMN_REF_ST_ID, COLUMN_BASE_SAVE, COLUMN_MAGIC_MOD,
 						// COLUMN_MISC_MOD, COLUMN_TEMP_MOD
 						int throwID = cursor2.getInt(1);
 						String name;
 						if (throwID == 1) {
 							name = "CONSTITUTION";
 						} else if (throwID == 2) {
 							name = "DEXTERITY";
 						} else {
 							name = "WISDOM";
 						}
 						int base_save = cursor2.getInt(2);
 
 						int magic_mod = cursor2.getInt(3);
 						int misc_mod = cursor2.getInt(4);
 						int temp_mod = cursor2.getInt(5);
 
 						JSONObject savingThrow = new JSONObject();
 						savingThrow.put("ref_id", throwID);
 						savingThrow.put("name", name);
 						savingThrow.put("base_save", base_save);
 						savingThrow.put("magic_mod", magic_mod);
 						savingThrow.put("misc_mod", misc_mod);
 						savingThrow.put("temp_mod", temp_mod);
 						savingThrows.put(savingThrow);
 						cursor2.moveToNext();
 					}
 					savingThrowsObject.put("savingThrows", savingThrows);
 				}
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			cursor2.close();
 			
 			
 			try {
 				if (savingThrows.toString().equals("[]")) {
 					savingThrowsObject = new JSONObject();
 					savingThrows = new JSONArray();
 					for(int i = 1; i <= 3; i++) {
 						int throwID = i;
 						String name;
 						if (throwID == 1) {
 							name = "CONSTITUTION";
 						} else if (throwID == 2) {
 							name = "DEXTERITY";
 						} else {
 							name = "WISDOM";
 						}
 						int base_save = 0;
 
 						int magic_mod = 0;
 						int misc_mod = 0;
 						int temp_mod = 0;
 
 						JSONObject savingThrow = new JSONObject();
 						savingThrow.put("ref_id", throwID);
 						savingThrow.put("name", name);
 						savingThrow.put("base_save", base_save);
 						savingThrow.put("magic_mod", magic_mod);
 						savingThrow.put("misc_mod", misc_mod);
 						savingThrow.put("temp_mod", temp_mod);
 						savingThrows.put(savingThrow);
 					}
 					savingThrowsObject.put("savingThrows", savingThrows);
 				}
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			// the data to send
 			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 			postParameters.add(new BasicNameValuePair("username", username));
 			postParameters.add(new BasicNameValuePair("basicInfo", basicInfo.toString()));
 			postParameters.add(new BasicNameValuePair("abilities", abilityObject.toString()));
 			postParameters.add(new BasicNameValuePair("skills", skillsObject.toString()));
 			postParameters.add(new BasicNameValuePair("combat", combatObject.toString()));
 			postParameters.add(new BasicNameValuePair("savingThrows", savingThrowsObject.toString()));
 			String result = null;
 
 			//http post
 			String res;
 			try{
 				result = CustomHttpClient.executeHttpPost(PHP_ADDRESS, postParameters);
 				res = result.toString();       
 			} catch (Exception e) {   
 				res = e.toString();
 			}
 			return res;
 		}
 
 		/**
 		 * Parses the String result and directs to the correct Activity
 		 */
 		@Override
 		protected void onPostExecute(String result) {
 			try {
 		        dialog.dismiss();
 		        dialog = null;
 		    } catch (Exception e) {
 		        // nothing
 		    }
 			Intent intent = new Intent(context, CharactersActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			finish();
 		}
 	}	 
 }
