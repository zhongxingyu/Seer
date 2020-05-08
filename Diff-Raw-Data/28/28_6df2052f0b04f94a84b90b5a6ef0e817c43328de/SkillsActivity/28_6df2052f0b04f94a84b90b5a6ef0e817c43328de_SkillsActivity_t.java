 package uw.cse403.minion;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.os.Bundle;
 import android.os.Debug;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Build;
 
 /**
  * SkillsActivity is an activity that provides the user with the UI they
  * can use to enter and edit skills information about their character. It
  * handles both the population of the UI after loading the relevant information
  * from the local database as well as the task of writing any new or updated
  * information into the local database.
  * @author 
  */
 public class SkillsActivity extends Activity {
 	
 	/** The unique id for a character **/
 	private long charID;
 
 	/**
	 * Displays the skills page and loads in any previously entered information
	 * from the local database.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_skills);
 		charID = this.getIntent().getExtras().getLong("cid");
 		
 		if (TraceControl.TRACE)
 			Debug.startMethodTracing("SkillsActivity_database");
 		
 		loadData();
 		
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
 		getMenuInflater().inflate(R.menu.skills, menu);
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
 	 * Loads all of the skills for the current character from the database.
 	 */
 	public void loadData() {
 		// get ability scores
 		Ability[] abilities = new Ability[6];
 		// these should auto load from DB
 		abilities[0] = new Ability(charID, AbilityName.STRENGTH);
 		abilities[1] = new Ability(charID, AbilityName.DEXTERITY);
 		abilities[2] = new Ability(charID, AbilityName.CONSTITUTION);
 		abilities[3] = new Ability(charID, AbilityName.INTELLIGENCE);
 		abilities[4] = new Ability(charID, AbilityName.WISDOM);
 		abilities[5] = new Ability(charID, AbilityName.CHARISMA);
 
 		// map from skillID to ability score
 		Map<Integer, Integer> skillToAbMod = new HashMap<Integer, Integer>();
 		// get skillIDs and associated ability score IDs
 		Cursor cursor1 = SQLiteHelperRefTables.db.query(SQLiteHelperRefTables.TABLE_REF_SKILLS, 
 				null, null, null, null, null, null);
 		// Columns: COLUMN_S_ID, COLUMN_S_NAME, COLUMN_S_REF_AS_ID
 		if (cursor1.moveToFirst()) {
 			while (!cursor1.isAfterLast()) {
 				int abScoreID = cursor1.getInt(2);
 				skillToAbMod.put(cursor1.getInt(0), abilities[abScoreID].getMod());
 				cursor1.moveToNext();
 			}
 		}
 		cursor1.close();
 		
 		int crafts = 0; // number of Crafts loaded
 		int performs = 0; // number of Performs loaded
 		int professions = 0; // number of Professions loaded
 
 		// get skills from DB
 		Cursor cursor2 = SQLiteHelperSkills.db.query(SQLiteHelperSkills.TABLE_NAME, SQLiteHelperSkills.ALL_COLUMNS, 
 				SQLiteHelperSkills.COLUMN_CHAR_ID + " = " + charID, null, null, null, null);
 		if (cursor2.moveToFirst()) {
 			while (!cursor2.isAfterLast()) { 
 				// Columns: COLUMN_CHAR_ID, COLUMN_REF_S_ID, COLUMN_TITLE, COLUMN_RANKS, COLUMN_MISC_MOD
 				int skillID = cursor2.getInt(1);
 				String title = cursor2.getString(2);
 				int ranks = cursor2.getInt(3);
 				int miscMod = cursor2.getInt(4);
 				Skill skill = new Skill(skillID, title, null, ranks, false);
 				skill.addModifier("skillMod", miscMod);
 				// these will store IDs referring to UI elements
 				int titleFieldID = 0;
 				int totalFieldID = 0;
 				int abModFieldID = 0;
 				int ranksFieldID = 0;
 				int modsFieldID = 0;
 				int arrayID = 0;
 				switch (skillID) {
 				case Skill.ACROBATICS_ID:
 					totalFieldID = R.id.acrobatics_total;
 					abModFieldID = R.id.acrobatics_ab_mod;
 					ranksFieldID = R.id.acrobatics_ranks;
 					modsFieldID = R.id.acrobatics_misc_mod; break;
 				case Skill.APPRAISE_ID:
 					totalFieldID = R.id.appraise_total;
 					abModFieldID = R.id.appraise_ab_mod;
 					ranksFieldID = R.id.appraise_ranks;
 					modsFieldID = R.id.appraise_misc_mod; break;
 				case Skill.BLUFF_ID:
 					totalFieldID = R.id.bluff_total;
 					abModFieldID = R.id.bluff_ab_mod;
 					ranksFieldID = R.id.bluff_ranks;
 					modsFieldID = R.id.bluff_misc_mod; break;
 				case Skill.CLIMB_ID:
 					totalFieldID = R.id.climb_total;
 					abModFieldID = R.id.climb_ab_mod;
 					ranksFieldID = R.id.climb_ranks;
 					modsFieldID = R.id.climb_misc_mod; break;
 				case Skill.CRAFT_ID:
 					crafts++;
 					arrayID = R.array.craft_array;
 					if (crafts == 1) {
 						titleFieldID = R.id.craft1_spinner;
 						totalFieldID = R.id.craft1_total;
 						abModFieldID = R.id.craft1_ab_mod;
 						ranksFieldID = R.id.craft1_ranks;
 						modsFieldID = R.id.craft1_misc_mod;
 					} else if (crafts == 2) {
 						titleFieldID = R.id.craft2_spinner;
 						totalFieldID = R.id.craft2_total;
 						abModFieldID = R.id.craft2_ab_mod;
 						ranksFieldID = R.id.craft2_ranks;
 						modsFieldID = R.id.craft2_misc_mod;
 					} else if (crafts == 3) {
 						titleFieldID = R.id.craft3_spinner;
 						totalFieldID = R.id.craft3_total;
 						abModFieldID = R.id.craft3_ab_mod;
 						ranksFieldID = R.id.craft3_ranks;
 						modsFieldID = R.id.craft3_misc_mod;
 					} break;
 				case Skill.DIPLOMACY_ID:
 					totalFieldID = R.id.diplomacy_total;
 					abModFieldID = R.id.diplomacy_ab_mod;
 					ranksFieldID = R.id.diplomacy_ranks;
 					modsFieldID = R.id.diplomacy_misc_mod; break;
 				case Skill.DISABLE_DEVICE_ID:
 					totalFieldID = R.id.disable_device_total;
 					abModFieldID = R.id.disable_device_ab_mod;
 					ranksFieldID = R.id.disable_device_ranks;
 					modsFieldID = R.id.disable_device_misc_mod; break;
 				case Skill.DISGUISE_ID:
 					totalFieldID = R.id.disguise_total;
 					abModFieldID = R.id.disguise_ab_mod;
 					ranksFieldID = R.id.disguise_ranks;
 					modsFieldID = R.id.disguise_misc_mod; break;
 				case Skill.ESCAPE_ARTIST_ID:
 					totalFieldID = R.id.escape_artist_total;
 					abModFieldID = R.id.escape_artist_ab_mod;
 					ranksFieldID = R.id.escape_artist_ranks;
 					modsFieldID = R.id.escape_artist_misc_mod; break;
 				case Skill.FLY_ID:
 					totalFieldID = R.id.fly_total;
 					abModFieldID = R.id.fly_ab_mod;
 					ranksFieldID = R.id.fly_ranks;
 					modsFieldID = R.id.fly_misc_mod; break;
 				case Skill.HANDLE_ANIMAL_ID:
 					totalFieldID = R.id.handle_animal_total;
 					abModFieldID = R.id.handle_animal_ab_mod;
 					ranksFieldID = R.id.handle_animal_ranks;
 					modsFieldID = R.id.handle_animal_misc_mod; break;
 				case Skill.HEAL_ID:
 					totalFieldID = R.id.heal_total;
 					abModFieldID = R.id.heal_ab_mod;
 					ranksFieldID = R.id.heal_ranks;
 					modsFieldID = R.id.heal_misc_mod; break;
 				case Skill.INTIMIDATE_ID:
 					totalFieldID = R.id.intimidate_total;
 					abModFieldID = R.id.intimidate_ab_mod;
 					ranksFieldID = R.id.intimidate_ranks;
 					modsFieldID = R.id.intimidate_misc_mod; break;
 				case Skill.KNOWLEDGE_ARCANA_ID:
 					totalFieldID = R.id.knowledge_arcana_total;
 					abModFieldID = R.id.knowledge_arcana_ab_mod;
 					ranksFieldID = R.id.knowledge_arcana_ranks;
 					modsFieldID = R.id.knowledge_arcana_misc_mod; break;
 				case Skill.KNOWLEDGE_DUNGEONEERING_ID:
 					totalFieldID = R.id.knowledge_dungeoneering_total;
 					abModFieldID = R.id.knowledge_dungeoneering_ab_mod;
 					ranksFieldID = R.id.knowledge_dungeoneering_ranks;
 					modsFieldID = R.id.knowledge_dungeoneering_misc_mod; break;
 				case Skill.KNOWLEDGE_ENGINEERING_ID:
 					totalFieldID = R.id.knowledge_engineering_total;
 					abModFieldID = R.id.knowledge_engineering_ab_mod;
 					ranksFieldID = R.id.knowledge_engineering_ranks;
 					modsFieldID = R.id.knowledge_engineering_misc_mod; break;
 				case Skill.KNOWLEDGE_GEOGRAPHY_ID:
 					totalFieldID = R.id.knowledge_geography_total;
 					abModFieldID = R.id.knowledge_geography_ab_mod;
 					ranksFieldID = R.id.knowledge_geography_ranks;
 					modsFieldID = R.id.knowledge_geography_misc_mod; break;
 				case Skill.KNOWLEDGE_HISTORY_ID:
 					totalFieldID = R.id.knowledge_history_total;
 					abModFieldID = R.id.knowledge_history_ab_mod;
 					ranksFieldID = R.id.knowledge_history_ranks;
 					modsFieldID = R.id.knowledge_history_misc_mod; break;
 				case Skill.KNOWLEDGE_LOCAL_ID:
 					totalFieldID = R.id.knowledge_local_total;
 					abModFieldID = R.id.knowledge_local_ab_mod;
 					ranksFieldID = R.id.knowledge_local_ranks;
 					modsFieldID = R.id.knowledge_local_misc_mod; break;
 				case Skill.KNOWLEDGE_NATURE_ID:
 					totalFieldID = R.id.knowledge_nature_total;
 					abModFieldID = R.id.knowledge_nature_ab_mod;
 					ranksFieldID = R.id.knowledge_nature_ranks;
 					modsFieldID = R.id.knowledge_nature_misc_mod; break;
 				case Skill.KNOWLEDGE_NOBILITY_ID:
 					totalFieldID = R.id.knowledge_nobility_total;
 					abModFieldID = R.id.knowledge_nobility_ab_mod;
 					ranksFieldID = R.id.knowledge_nobility_ranks;
 					modsFieldID = R.id.knowledge_nobility_misc_mod; break;
 				case Skill.KNOWLEDGE_PLANES_ID:
 					totalFieldID = R.id.knowledge_planes_total;
 					abModFieldID = R.id.knowledge_planes_ab_mod;
 					ranksFieldID = R.id.knowledge_planes_ranks;
 					modsFieldID = R.id.knowledge_planes_misc_mod; break;
 				case Skill.KNOWLEDGE_RELIGION_ID:
 					totalFieldID = R.id.knowledge_religion_total;
 					abModFieldID = R.id.knowledge_religion_ab_mod;
 					ranksFieldID = R.id.knowledge_religion_ranks;
 					modsFieldID = R.id.knowledge_religion_misc_mod; break;
 				case Skill.LINGUISTICS_ID:
 					totalFieldID = R.id.linguistics_total;
 					abModFieldID = R.id.linguistics_ab_mod;
 					ranksFieldID = R.id.linguistics_ranks;
 					modsFieldID = R.id.linguistics_misc_mod; break;
 				case Skill.PERCEPTION_ID:
 					totalFieldID = R.id.perception_total;
 					abModFieldID = R.id.perception_ab_mod;
 					ranksFieldID = R.id.perception_ranks;
 					modsFieldID = R.id.perception_misc_mod; break;
 				case Skill.PERFORM_ID:
 					//if (performs < 2)
 					performs++;
 					arrayID = R.array.perform_array;
 					if (performs == 1) {
 						titleFieldID = R.id.perform1_spinner;
 						totalFieldID = R.id.perform1_total;
 						abModFieldID = R.id.perform1_ab_mod;
 						ranksFieldID = R.id.perform1_ranks;
 						modsFieldID = R.id.perform1_misc_mod;
 					} else if (performs == 2) {
 						titleFieldID = R.id.perform2_spinner;
 						totalFieldID = R.id.perform2_total;
 						abModFieldID = R.id.perform2_ab_mod;
 						ranksFieldID = R.id.perform2_ranks;
 						modsFieldID = R.id.perform2_misc_mod;
 					} break;
 				case Skill.PROFESSION_ID:
 					//if (professions < 2)
 					professions++;
 					arrayID = R.array.profession_array;
 					if (professions == 1) {
 						titleFieldID = R.id.profession1_spinner;
 						totalFieldID = R.id.profession1_total;
 						abModFieldID = R.id.profession1_ab_mod;
 						ranksFieldID = R.id.profession1_ranks;
 						modsFieldID = R.id.profession1_misc_mod;
 					} else if (professions == 2) {
 						titleFieldID = R.id.profession2_spinner;
 						totalFieldID = R.id.profession2_total;
 						abModFieldID = R.id.profession2_ab_mod;
 						ranksFieldID = R.id.profession2_ranks;
 						modsFieldID = R.id.profession2_misc_mod;
 					} break;
 				case Skill.RIDE_ID:
 					totalFieldID = R.id.ride_total;
 					abModFieldID = R.id.ride_ab_mod;
 					ranksFieldID = R.id.ride_ranks;
 					modsFieldID = R.id.ride_misc_mod; break;
 				case Skill.SENSE_MOTIVE_ID:
 					totalFieldID = R.id.sense_motive_total;
 					abModFieldID = R.id.sense_motive_ab_mod;
 					ranksFieldID = R.id.sense_motive_ranks;
 					modsFieldID = R.id.sense_motive_misc_mod; break;
 				case Skill.SLEIGHT_OF_HAND_ID:
 					totalFieldID = R.id.sleight_of_hand_total;
 					abModFieldID = R.id.sleight_of_hand_ab_mod;
 					ranksFieldID = R.id.sleight_of_hand_ranks;
 					modsFieldID = R.id.sleight_of_hand_misc_mod; break;
 				case Skill.SPELLCRAFT_ID:
 					totalFieldID = R.id.spellcraft_total;
 					abModFieldID = R.id.spellcraft_ab_mod;
 					ranksFieldID = R.id.spellcraft_ranks;
 					modsFieldID = R.id.spellcraft_misc_mod; break;
 				case Skill.STEALTH_ID:
 					totalFieldID = R.id.stealth_total;
 					abModFieldID = R.id.stealth_ab_mod;
 					ranksFieldID = R.id.stealth_ranks;
 					modsFieldID = R.id.stealth_misc_mod; break;
 				case Skill.SURVIVAL_ID:
 					totalFieldID = R.id.survival_total;
 					abModFieldID = R.id.survival_ab_mod;
 					ranksFieldID = R.id.survival_ranks;
 					modsFieldID = R.id.survival_misc_mod; break;
 				case Skill.SWIM_ID:
 					totalFieldID = R.id.swim_total;
 					abModFieldID = R.id.swim_ab_mod;
 					ranksFieldID = R.id.swim_ranks;
 					modsFieldID = R.id.swim_misc_mod; break;
 				case Skill.USE_MAGIC_DEVICE_ID:
 					totalFieldID = R.id.use_magic_device_total;
 					abModFieldID = R.id.use_magic_device_ab_mod;
 					ranksFieldID = R.id.use_magic_device_ranks;
 					modsFieldID = R.id.use_magic_device_misc_mod; break;
 				}
 				// put data in UI
 				int abMod = skillToAbMod.get(skill.getID());
 
 				TextView totalField = (TextView) findViewById(totalFieldID);
 				totalField.setText("" + (skill.getTotal() + abMod));
 				
 				TextView abModField = (TextView) findViewById(abModFieldID);
 				abModField.setText("" + abMod);
 				
 				if (skillID == Skill.CRAFT_ID || skillID == Skill.PERFORM_ID 
 						|| skillID == Skill.PROFESSION_ID) {
 					Spinner spinner = (Spinner) findViewById(titleFieldID);
 					ArrayAdapter<CharSequence> myAdap = ArrayAdapter.createFromResource(this, arrayID,
 							R.layout.smaller_multiline_spinner_dropdown_item);
 					spinner.setAdapter(myAdap);
 					int spinnerPosition = myAdap.getPosition(title);
 					//set the default according to value
 					spinner.setSelection(spinnerPosition);
 				}
 
 				EditText ranksEnterField = (EditText) findViewById(ranksFieldID);
 				ranksEnterField.setText("" + skill.getRank());
 				
 				EditText modEnterField = (EditText) findViewById(modsFieldID);
 				modEnterField.setText("" + skill.getModifier("skillMod"));
 
 				cursor2.moveToNext();
 			}
 		}
 		cursor2.close();
 	}
 
 	/**
 	 * Helper class for storing a string and int together
 	 * mostly for making the following code readable
 	 */
 	private class StrInt {
 		int i;
 		String s;
 		public StrInt(String s, int i) {
 			this.i = i;
 			this.s = s;
 		}
 	}
 	
 	/**
 	 * Responds to the Save button click and writes all of the currently
 	 * entered skills information to the local database. Then sends the
 	 * user back to the main character creation screen.
 	 */
 	public void skills(View view) {
 		// TODO write method
 		//Skill sk = new Skill(null, null);
 		ArrayList<Skill> skills = new ArrayList<Skill>();
 
 		// this segment of code is for factoring the code so that it's not so redundant
 		// however the priority is getting this to work, so we can finish this later
 		//		// load skills from DB
 		//		Cursor cursor = SQLiteHelperRefTables.db.query(SQLiteHelperRefTables.TABLE_REF_SKILLS, 
 		//				SQLiteHelperRefTables.ALL_COLUMNS_S, null, null, null, null, null);
 		//		Map<Integer, StrInt> skillsRef = new HashMap<Integer, StrInt>(); 
 		//		// ^ this is essentially a map from skill id to skill name and ability score id
 		//		if (cursor.moveToFirst()) {
 		//			// Columns: COLUMN_S_ID, COLUMN_S_NAME, COLUMN_S_REF_AS_ID
 		//			int id 		= cursor.getInt(0);
 		//			String name = cursor.getString(1);
 		//			int asID 	= cursor.getInt(2);
 		//			skillsRef.put(id, new StrInt(name, asID));
 		//		}
 		//		cursor.close();
 
 		// Acrobatics
 		EditText acrobaticsRanksEnter = (EditText) findViewById(R.id.acrobatics_ranks);
 		EditText acrobaticsMiscEnter = (EditText) findViewById(R.id.acrobatics_misc_mod);
 		String acrobaticsRanks = acrobaticsRanksEnter.getText().toString().trim();
 		String acrobaticsMisc = acrobaticsMiscEnter.getText().toString().trim();
 		if (acrobaticsRanks.matches("")) {
 			acrobaticsRanks = "0";
 		}
 		
 		if (acrobaticsMisc.matches("")) {
 			acrobaticsMisc = "0";
 		}
 
 		if (!acrobaticsRanks.matches("")) {
 			int acrobaticsRank = Integer.parseInt(acrobaticsRanks);
 			Skill skill = new Skill(Skill.ACROBATICS_ID, "Acrobatics", 
 					AbilityName.DEXTERITY, acrobaticsRank, false);
 			if (!acrobaticsMisc.matches("")) {
 				int acrobaticsMod = Integer.parseInt(acrobaticsMisc);
 				skill.addModifier("acrobaticsMod", acrobaticsMod);
 			}
 			skills.add(skill);
 		}
 
 		// Appraise
 		EditText appraiseRanksEnter = (EditText) findViewById(R.id.appraise_ranks);
 		EditText appraiseMiscEnter = (EditText) findViewById(R.id.appraise_misc_mod);
 		String appraiseRanks = appraiseRanksEnter.getText().toString().trim();
 		String appraiseMisc = appraiseMiscEnter.getText().toString().trim();
 		if (appraiseRanks.matches("")) {
 			appraiseRanks = "0";
 		}
 		
 		if (appraiseMisc.matches("")) {
 			appraiseMisc = "0";
 		}
 		if (!appraiseRanks.matches("")) {
 			int appraiseRank = Integer.parseInt(appraiseRanks);
 			Skill skill = new Skill(Skill.APPRAISE_ID, "Appraise", 
 					AbilityName.INTELLIGENCE, appraiseRank, false);
 			if (!appraiseMisc.matches("")) {
 				int appraiseMod = Integer.parseInt(appraiseMisc);
 				skill.addModifier("appraiseMod", appraiseMod);
 			}
 			skills.add(skill);
 		}
 
 		// Bluff
 		EditText bluffRanksEnter = (EditText) findViewById(R.id.bluff_ranks);
 		EditText bluffMiscEnter = (EditText) findViewById(R.id.bluff_misc_mod);
 		String bluffRanks = bluffRanksEnter.getText().toString().trim();
 		String bluffMisc = bluffMiscEnter.getText().toString().trim();
 		if (bluffRanks.matches("")) {
 			bluffRanks = "0";
 		}
 		
 		if (bluffMisc.matches("")) {
 			bluffMisc = "0";
 		}
 		if (!bluffRanks.matches("")) {
 			int bluffRank = Integer.parseInt(bluffRanks);
 			Skill skill = new Skill(Skill.BLUFF_ID, "Bluff", 
 					AbilityName.CHARISMA, bluffRank, false);
 			if (!bluffMisc.matches("")) {
 				int bluffMod = Integer.parseInt(bluffMisc);
 				skill.addModifier("bluffMod", bluffMod);
 			}
 			skills.add(skill);
 		}
 
 		// Climb
 		EditText climbRanksEnter = (EditText) findViewById(R.id.climb_ranks);
 		EditText climbMiscEnter = (EditText) findViewById(R.id.climb_misc_mod);
 		String climbRanks = climbRanksEnter.getText().toString().trim();
 		String climbMisc = climbMiscEnter.getText().toString().trim();
 		if (climbRanks.matches("")) {
 			climbRanks = "0";
 		}
 		
 		if (climbMisc.matches("")) {
 			climbMisc = "0";
 		}
 
 		if (!climbRanks.matches("")) {
 			int climbRank = Integer.parseInt(climbRanks);
 			Skill skill = new Skill(Skill.CLIMB_ID, "Climb", 
 					AbilityName.STRENGTH, climbRank, false);
 			if (!climbMisc.matches("")) {
 				int climbMod = Integer.parseInt(climbMisc);
 				skill.addModifier("climbMod", climbMod);
 			}
 			skills.add(skill);
 		}
 
 		// Craft1
 		Spinner craft1Spinner = (Spinner) findViewById(R.id.craft1_spinner);
 		// Gives a string representation of whatever item is selected in the spinner
 		String craft1 = craft1Spinner.getSelectedItem().toString();
 
 		EditText craft1RanksEnter = (EditText) findViewById(R.id.craft1_ranks);
 		EditText craft1MiscEnter = (EditText) findViewById(R.id.craft1_misc_mod);
 		String craft1Ranks = craft1RanksEnter.getText().toString().trim();
 		String craft1Misc = craft1MiscEnter.getText().toString().trim();
 		if (craft1Ranks.matches("")) {
 			craft1Ranks = "0";
 		}
 		
 		if (craft1Misc.matches("")) {
 			craft1Misc = "0";
 		}
 		if (!craft1Ranks.matches("")) {
 			int craft1Rank = Integer.parseInt(craft1Ranks);
 			Skill skill = new Skill(Skill.CRAFT_ID, "Craft1", craft1, 
 					AbilityName.INTELLIGENCE, craft1Rank, false);
 			if (!craft1Misc.matches("")) {
 				int craft1Mod = Integer.parseInt(craft1Misc);
 				skill.addModifier("craft1Mod", craft1Mod);
 			}
 			skills.add(skill);
 		}
 
 		// Craft2
 		Spinner craft2Spinner = (Spinner) findViewById(R.id.craft2_spinner);
 		// Gives a string representation of whatever item is selected in the spinner
 		String craft2 = craft2Spinner.getSelectedItem().toString();
 
 		EditText craft2RanksEnter = (EditText) findViewById(R.id.craft2_ranks);
 		EditText craft2MiscEnter = (EditText) findViewById(R.id.craft2_misc_mod);
 		String craft2Ranks = craft2RanksEnter.getText().toString().trim();
 		String craft2Misc = craft2MiscEnter.getText().toString().trim();
 		if (craft2Ranks.matches("")) {
 			craft2Ranks = "0";
 		}
 		
 		if (craft2Misc.matches("")) {
 			craft2Misc = "0";
 		}
 		if (!craft2Ranks.matches("")) {
 			int craft2Rank = Integer.parseInt(craft2Ranks);
 			Skill skill = new Skill(Skill.CRAFT_ID, "Craft2", craft2, 
 					AbilityName.INTELLIGENCE, craft2Rank, false);
 			if (!craft2Misc.matches("")) {
 				int craft2Mod = Integer.parseInt(craft2Misc);
 				skill.addModifier("craft2Mod", craft2Mod);
 			}
 			skills.add(skill);
 		}
 
 
 		// Craft3
 		Spinner craft3Spinner = (Spinner) findViewById(R.id.craft3_spinner);
 		// Gives a string representation of whatever item is selected in the spinner
 		String craft3 = craft3Spinner.getSelectedItem().toString();
 
 		EditText craft3RanksEnter = (EditText) findViewById(R.id.craft3_ranks);
 		EditText craft3MiscEnter = (EditText) findViewById(R.id.craft3_misc_mod);
 		String craft3Ranks = craft3RanksEnter.getText().toString().trim();
 		String craft3Misc = craft3MiscEnter.getText().toString().trim();
 		if (craft3Ranks.matches("")) {
 			craft3Ranks = "0";
 		}
 		
 		if (craft3Misc.matches("")) {
 			craft3Misc = "0";
 		}
 		if (!craft3Ranks.matches("")) {
 			int craft3Rank = Integer.parseInt(craft3Ranks);
 			Skill skill = new Skill(Skill.CRAFT_ID, "Craft3", craft3, 
 					AbilityName.INTELLIGENCE, craft3Rank, false);
 			if (!craft3Misc.matches("")) {
 				int craft3Mod = Integer.parseInt(craft3Misc);
 				skill.addModifier("craft3Mod", craft3Mod);
 			}
 			skills.add(skill);
 		}
 
 
 		// Diplomacy
 		EditText diplomacyRanksEnter = (EditText) findViewById(R.id.diplomacy_ranks);
 		EditText diplomacyMiscEnter = (EditText) findViewById(R.id.diplomacy_misc_mod);
 		String diplomacyRanks = diplomacyRanksEnter.getText().toString().trim();
 		String diplomacyMisc = diplomacyMiscEnter.getText().toString().trim();
 		if (diplomacyRanks.matches("")) {
 			diplomacyRanks = "0";
 		}
 		
 		if (diplomacyMisc.matches("")) {
 			diplomacyMisc = "0";
 		}
 		if (!diplomacyRanks.matches("")) {
 			int diplomacyRank = Integer.parseInt(diplomacyRanks);
 			Skill skill = new Skill(Skill.DIPLOMACY_ID, "Diplomacy", 
 					AbilityName.CHARISMA, diplomacyRank, false);
 			if (!diplomacyMisc.matches("")) {
 				int diplomacyMod = Integer.parseInt(diplomacyMisc);
 				skill.addModifier("diplomacyMod", diplomacyMod);
 			}
 			skills.add(skill);
 		}
 
 		// Disable Device
 		EditText disableDeviceRanksEnter = (EditText) findViewById(R.id.disable_device_ranks);
 		EditText disableDeviceMiscEnter = (EditText) findViewById(R.id.disable_device_misc_mod);
 		String disableDeviceRanks = disableDeviceRanksEnter.getText().toString().trim();
 		String disableDeviceMisc = disableDeviceMiscEnter.getText().toString().trim();
 		if (disableDeviceRanks.matches("")) {
 			disableDeviceRanks = "0";
 		}
 		
 		if (disableDeviceMisc.matches("")) {
 			disableDeviceMisc = "0";
 		}
 		if (!disableDeviceRanks.matches("")) {
 			int disableDeviceRank = Integer.parseInt(disableDeviceRanks);
 			Skill skill = new Skill(Skill.DISABLE_DEVICE_ID, "Disable Device", 
 					AbilityName.DEXTERITY, disableDeviceRank, false);
 			if (!disableDeviceRanks.matches("")) {
 				int disableDeviceMod = Integer.parseInt(disableDeviceMisc);
 				skill.addModifier("disableDeviceMod", disableDeviceMod);
 			}
 			skills.add(skill);
 		}
 
 		// Disguise
 		EditText disguiseRanksEnter = (EditText) findViewById(R.id.disguise_ranks);
 		EditText disguiseMiscEnter = (EditText) findViewById(R.id.disguise_misc_mod);
 		String disguiseRanks = disguiseRanksEnter.getText().toString().trim();
 		String disguiseMisc = disguiseMiscEnter.getText().toString().trim();
 		if (disguiseRanks.matches("")) {
 			disguiseRanks = "0";
 		}
 		
 		if (disguiseMisc.matches("")) {
 			disguiseMisc = "0";
 		}
 		if (!disguiseRanks.matches("")) {
 			int disguiseRank = Integer.parseInt(disguiseRanks);
 			Skill skill = new Skill(Skill.DISGUISE_ID, "Disguise", 
 					AbilityName.CHARISMA, disguiseRank, false);
 			if (!disguiseMisc.matches("")) {
 				int disguiseMod = Integer.parseInt(disguiseMisc);
 				skill.addModifier("disguiseMod", disguiseMod);
 			}
 			skills.add(skill);
 		}
 
 		// Escape Artist
 		EditText escapeArtistRanksEnter = (EditText) findViewById(R.id.escape_artist_ranks);
 		EditText escapeArtistMiscEnter = (EditText) findViewById(R.id.escape_artist_misc_mod);
 		String escapeArtistRanks = escapeArtistRanksEnter.getText().toString().trim();
 		String escapeArtistMisc = escapeArtistMiscEnter.getText().toString().trim();
 		if (escapeArtistRanks.matches("")) {
 			escapeArtistRanks = "0";
 		}
 		
 		if (escapeArtistMisc.matches("")) {
 			escapeArtistMisc = "0";
 		}
 		if (!escapeArtistRanks.matches("")) {
 			int escapeArtistRank = Integer.parseInt(escapeArtistRanks);
 			Skill skill = new Skill(Skill.ESCAPE_ARTIST_ID, "Escape Artist", 
 					AbilityName.DEXTERITY, escapeArtistRank, false);
 			if (!escapeArtistMisc.matches("")) {
 				int escapeArtistMod = Integer.parseInt(escapeArtistMisc);
 				skill.addModifier("escapeArtistMod", escapeArtistMod);
 			}
 			skills.add(skill);
 		}
 
 		// Fly
 		EditText flyRanksEnter = (EditText) findViewById(R.id.fly_ranks);
 		EditText flyMiscEnter = (EditText) findViewById(R.id.fly_misc_mod);
 		String flyRanks = flyRanksEnter.getText().toString().trim();
 		String flyMisc = flyMiscEnter.getText().toString().trim();
 		if (flyRanks.matches("")) {
 			flyRanks = "0";
 		}
 		
 		if (flyMisc.matches("")) {
 			flyMisc = "0";
 		}
 		if (!flyRanks.matches("")) {
 			int flyRank = Integer.parseInt(flyRanks);
 			Skill skill = new Skill(Skill.FLY_ID, "Fly", 
 					AbilityName.DEXTERITY, flyRank, false);
 			if (!flyMisc.matches("")) {
 				int flyMod = Integer.parseInt(flyMisc);
 				skill.addModifier("flyMod", flyMod);
 			}
 			skills.add(skill);
 		}
 
 		// Handle Animal
 		EditText handleAnimalRanksEnter = (EditText) findViewById(R.id.handle_animal_ranks);
 		EditText handleAnimalMiscEnter = (EditText) findViewById(R.id.handle_animal_misc_mod);
 		String handleAnimalRanks = handleAnimalRanksEnter.getText().toString().trim();
 		String handleAnimalMisc = handleAnimalMiscEnter.getText().toString().trim();
 		if (handleAnimalRanks.matches("")) {
 			handleAnimalRanks = "0";
 		}
 		
 		if (handleAnimalMisc.matches("")) {
 			handleAnimalMisc = "0";
 		}
 		if (!handleAnimalRanks.matches("")) {
 			int handleAnimalRank = Integer.parseInt(handleAnimalRanks);
 			Skill skill = new Skill(Skill.HANDLE_ANIMAL_ID, "Handle Animal", 
 					AbilityName.CHARISMA, handleAnimalRank, false);
 			if (!handleAnimalMisc.matches("")) {
 				int handleAnimalMod = Integer.parseInt(handleAnimalMisc);
 				skill.addModifier("handleAnimalMod", handleAnimalMod);
 			}
 			skills.add(skill);
 		}
 
 		// Heal
 		EditText healRanksEnter = (EditText) findViewById(R.id.heal_ranks);
 		EditText healMiscEnter = (EditText) findViewById(R.id.heal_misc_mod);
 		String healRanks = healRanksEnter.getText().toString().trim();
 		String healMisc = healMiscEnter.getText().toString().trim();
 		if (healRanks.matches("")) {
 			healRanks = "0";
 		}
 		
 		if (healMisc.matches("")) {
 			healMisc = "0";
 		}
 		if (!healRanks.matches("")) {
 			int healRank = Integer.parseInt(healRanks);
 			Skill skill = new Skill(Skill.HEAL_ID, "Heal", 
 					AbilityName.WISDOM, healRank, false);
 			if (!healMisc.matches("")) {
 				int healMod = Integer.parseInt(healMisc);
 				skill.addModifier("healMod", healMod);
 			}
 			skills.add(skill);
 		}
 
 		// Intimidate
 		EditText intimidateRanksEnter = (EditText) findViewById(R.id.intimidate_ranks);
 		EditText intimidateMiscEnter = (EditText) findViewById(R.id.intimidate_misc_mod);
 		String intimidateRanks = intimidateRanksEnter.getText().toString().trim();
 		String intimidateMisc = intimidateMiscEnter.getText().toString().trim();
 		if (intimidateRanks.matches("")) {
 			intimidateRanks = "0";
 		}
 		
 		if (intimidateMisc.matches("")) {
 			intimidateMisc = "0";
 		}
 		if (!intimidateRanks.matches("")) {
 			int intimidateRank = Integer.parseInt(intimidateRanks);
 			Skill skill = new Skill(Skill.INTIMIDATE_ID, "Intimidate", 
 					AbilityName.CHARISMA, intimidateRank, false);
 			if (!intimidateMisc.matches("")) {
 				int intimidateMod = Integer.parseInt(intimidateMisc);
 				skill.addModifier("intimidateMod", intimidateMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Arcana)
 		EditText knowledgeArcanaRanksEnter = (EditText) findViewById(R.id.knowledge_arcana_ranks);
 		EditText knowledgeArcanaMiscEnter = (EditText) findViewById(R.id.knowledge_arcana_misc_mod);
 		String knowledgeArcanaRanks = knowledgeArcanaRanksEnter.getText().toString().trim();
 		String knowledgeArcanaMisc = knowledgeArcanaMiscEnter.getText().toString().trim();
 		if (knowledgeArcanaRanks.matches("")) {
 			knowledgeArcanaRanks = "0";
 		}
 		
 		if (knowledgeArcanaMisc.matches("")) {
 			knowledgeArcanaMisc = "0";
 		}
 		if (!knowledgeArcanaRanks.matches("")) {
 			int knowledgeArcanaRank = Integer.parseInt(knowledgeArcanaRanks);
 			Skill skill = new Skill(Skill.KNOWLEDGE_ARCANA_ID, "Knowledge (Arcana)", 
 					AbilityName.INTELLIGENCE, knowledgeArcanaRank, false);
 			if (!knowledgeArcanaMisc.matches("")) {
 				int knowledgeArcanaMod = Integer.parseInt(knowledgeArcanaMisc);
 				skill.addModifier("knowledgeArcanaMod", knowledgeArcanaMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Dungeoneering)
 		EditText knowledgeDungeoneeringRanksEnter = (EditText) findViewById(R.id.knowledge_dungeoneering_ranks);
 		EditText knowledgeDungeoneeringMiscEnter = (EditText) findViewById(R.id.knowledge_dungeoneering_misc_mod);
 		String knowledgeDungeoneeringRanks = knowledgeDungeoneeringRanksEnter.getText().toString().trim();
 		String knowledgeDungeoneeringMisc = knowledgeDungeoneeringMiscEnter.getText().toString().trim();
 		if (knowledgeDungeoneeringRanks.matches("")) {
 			knowledgeDungeoneeringRanks = "0";
 		}
 		
 		if (knowledgeDungeoneeringMisc.matches("")) {
 			knowledgeDungeoneeringMisc = "0";
 		}
 		if (!knowledgeDungeoneeringRanks.matches("")) {
 			int knowledgeDungeoneeringRank = Integer.parseInt(knowledgeDungeoneeringRanks);
 			Skill skill = new Skill(Skill.KNOWLEDGE_DUNGEONEERING_ID, "Knowledge (Dungeoneering)", 
 					AbilityName.INTELLIGENCE, knowledgeDungeoneeringRank, false);
 			if (!knowledgeDungeoneeringMisc.matches("")) {
 				int knowledgeDungeoneeringMod = Integer.parseInt(knowledgeDungeoneeringMisc);
 				skill.addModifier("knowledgeDungeoneeringMod", knowledgeDungeoneeringMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Engineering)
 		EditText knowledgeEngineeringRanksEnter = (EditText) findViewById(R.id.knowledge_engineering_ranks);
 		EditText knowledgeEngineeringMiscEnter = (EditText) findViewById(R.id.knowledge_engineering_misc_mod);
 		String knowledgeEngineeringRanks = knowledgeEngineeringRanksEnter.getText().toString().trim();
 		String knowledgeEngineeringMisc = knowledgeEngineeringMiscEnter.getText().toString().trim();
 		if (knowledgeEngineeringRanks.matches("")) {
 			knowledgeEngineeringRanks = "0";
 		}
 		
 		if (knowledgeEngineeringMisc.matches("")) {
 			knowledgeEngineeringMisc = "0";
 		}
 		if (!knowledgeEngineeringRanks.matches("")) {
 			int knowledgeEngineeringRank = Integer.parseInt(knowledgeEngineeringRanks);
 			Skill skill = new Skill(Skill.KNOWLEDGE_ENGINEERING_ID, "Knowledge (Engineering)", 
 					AbilityName.INTELLIGENCE, knowledgeEngineeringRank, false);
 			if (!knowledgeEngineeringMisc.matches("")) {
 				int knowledgeEngineeringMod = Integer.parseInt(knowledgeEngineeringMisc);
 				skill.addModifier("knowledgeEngineeringMod", knowledgeEngineeringMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Geography)
 		EditText knowledgeGeographyRanksEnter = (EditText) findViewById(R.id.knowledge_geography_ranks);
 		EditText knowledgeGeographyMiscEnter = (EditText) findViewById(R.id.knowledge_geography_misc_mod);
 		String knowledgeGeographyRanks = knowledgeGeographyRanksEnter.getText().toString().trim();
 		String knowledgeGeographyMisc = knowledgeGeographyMiscEnter.getText().toString().trim();
 		if (knowledgeGeographyRanks.matches("")) {
 			knowledgeGeographyRanks = "0";
 		}
 		
 		if (knowledgeGeographyMisc.matches("")) {
 			knowledgeGeographyMisc = "0";
 		}
 		if (!knowledgeGeographyRanks.matches("")) {
 			int knowledgeGeographyRank = Integer.parseInt(knowledgeGeographyRanks);
 			Skill skill = new Skill(Skill.KNOWLEDGE_GEOGRAPHY_ID, "Knowledge (Geography)", 
 					AbilityName.INTELLIGENCE, knowledgeGeographyRank, false);
 			if (!knowledgeGeographyMisc.matches("")) {
 				int knowledgeGeographyMod = Integer.parseInt(knowledgeGeographyMisc);
 				skill.addModifier("knowledgeGeographyMod", knowledgeGeographyMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (History)
 		EditText knowledgeHistoryRanksEnter = (EditText) findViewById(R.id.knowledge_history_ranks);
 		EditText knowledgeHistoryMiscEnter = (EditText) findViewById(R.id.knowledge_history_misc_mod);
 		String knowledgeHistoryRanks = knowledgeHistoryRanksEnter.getText().toString().trim();
 		String knowledgeHistoryMisc = knowledgeHistoryMiscEnter.getText().toString().trim();
 		if (knowledgeHistoryRanks.matches("")) {
 			knowledgeHistoryRanks = "0";
 		}
 		
 		if (knowledgeHistoryMisc.matches("")) {
 			knowledgeHistoryMisc = "0";
 		}
 		if (!knowledgeHistoryRanks.matches("")) {
 			int knowledgeHistoryRank = Integer.parseInt(knowledgeHistoryRanks);
 			Skill skill = new Skill(Skill.KNOWLEDGE_HISTORY_ID, "Knowledge (History)", 
 					AbilityName.INTELLIGENCE, knowledgeHistoryRank, false);
 			if (!knowledgeHistoryMisc.matches("")) {
 				int knowledgeHistoryMod = Integer.parseInt(knowledgeHistoryMisc);
 				skill.addModifier("knowledgeHistoryMod", knowledgeHistoryMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Local)
 		EditText knowledgeLocalRanksEnter = (EditText) findViewById(R.id.knowledge_local_ranks);
 		EditText knowledgeLocalMiscEnter = (EditText) findViewById(R.id.knowledge_local_misc_mod);
 		String knowledgeLocalRanks = knowledgeLocalRanksEnter.getText().toString().trim();
 		String knowledgeLocalMisc = knowledgeLocalMiscEnter.getText().toString().trim();
 		if (knowledgeLocalRanks.matches("")) {
 			knowledgeLocalRanks = "0";
 		}
 		
 		if (knowledgeLocalMisc.matches("")) {
 			knowledgeLocalMisc = "0";
 		}
 		if (!knowledgeLocalRanks.matches("")) {
 			int knowledgeLocalRank = Integer.parseInt(knowledgeLocalRanks);
 			Skill skill = new Skill(Skill.KNOWLEDGE_LOCAL_ID, "Knowledge (Local)", 
 					AbilityName.INTELLIGENCE, knowledgeLocalRank, false);
 			if (!knowledgeLocalMisc.matches("")) {
 				int knowledgeLocalMod = Integer.parseInt(knowledgeLocalMisc);
 				skill.addModifier("knowledgeLocalMod", knowledgeLocalMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Nature)
 		EditText knowledgeNatureRanksEnter = (EditText) findViewById(R.id.knowledge_nature_ranks);
 		EditText knowledgeNatureMiscEnter = (EditText) findViewById(R.id.knowledge_nature_misc_mod);
 		String knowledgeNatureRanks = knowledgeNatureRanksEnter.getText().toString().trim();
 		String knowledgeNatureMisc = knowledgeNatureMiscEnter.getText().toString().trim();
 		if (knowledgeNatureRanks.matches("")) {
 			knowledgeNatureRanks = "0";
 		}
 		
 		if (knowledgeNatureMisc.matches("")) {
 			knowledgeNatureMisc = "0";
 		}
 		if (!knowledgeNatureRanks.matches("")) {
 			int knowledgeNatureRank = Integer.parseInt(knowledgeNatureRanks);
 			Skill skill = new Skill(Skill.KNOWLEDGE_NATURE_ID, "Knowledge (Nature)", 
 					AbilityName.INTELLIGENCE, knowledgeNatureRank, false);
 			if (!knowledgeNatureMisc.matches("")) {
 				int knowledgeNatureMod = Integer.parseInt(knowledgeNatureMisc);
 				skill.addModifier("knowledgeNatureMod", knowledgeNatureMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Nobility)
 		EditText knowledgeNobilityRanksEnter = (EditText) findViewById(R.id.knowledge_nobility_ranks);
 		EditText knowledgeNobilityMiscEnter = (EditText) findViewById(R.id.knowledge_nobility_misc_mod);
 		String knowledgeNobilityRanks = knowledgeNobilityRanksEnter.getText().toString().trim();
 		String knowledgeNobilityMisc = knowledgeNobilityMiscEnter.getText().toString().trim();
 		if (knowledgeNobilityRanks.matches("")) {
 			knowledgeNobilityRanks = "0";
 		}
 		
 		if (knowledgeNobilityMisc.matches("")) {
 			knowledgeNobilityMisc = "0";
 		}
 		if (!knowledgeNobilityRanks.matches("")) {
 			int knowledgeNobilityRank = Integer.parseInt(knowledgeNobilityRanks);
 			Skill skill = new Skill(Skill.KNOWLEDGE_NOBILITY_ID, "Knowledge (Nobility)", 
 					AbilityName.INTELLIGENCE, knowledgeNobilityRank, false);
 			if (!knowledgeNobilityMisc.matches("")) {
 				int knowledgeNobilityMod = Integer.parseInt(knowledgeNobilityMisc);
 				skill.addModifier("knowledgeNobilityMod", knowledgeNobilityMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Planes)
 		EditText knowledgePlanesRanksEnter = (EditText) findViewById(R.id.knowledge_planes_ranks);
 		EditText knowledgePlanesMiscEnter = (EditText) findViewById(R.id.knowledge_planes_misc_mod);
 		String knowledgePlanesRanks = knowledgePlanesRanksEnter.getText().toString().trim();
 		String knowledgePlanesMisc = knowledgePlanesMiscEnter.getText().toString().trim();
 		if (knowledgePlanesRanks.matches("")) {
 			knowledgePlanesRanks = "0";
 		}
 		
 		if (knowledgePlanesMisc.matches("")) {
 			knowledgePlanesMisc = "0";
 		}
 		if (!knowledgePlanesRanks.matches("")) {
 			int knowledgePlanesRank = Integer.parseInt(knowledgePlanesRanks);
 			Skill skill = new Skill(Skill.KNOWLEDGE_PLANES_ID, "Knowledge (Planes)", 
 					AbilityName.INTELLIGENCE, knowledgePlanesRank, false);
 			if (!knowledgePlanesMisc.matches("")) {
 				int knowledgePlanesMod = Integer.parseInt(knowledgePlanesMisc);
 				skill.addModifier("knowledgePlanesMod", knowledgePlanesMod);
 			}
 			skills.add(skill);
 		}
 
 		// Knowledge (Religion)
 		EditText knowledgeReligionRanksEnter = (EditText) findViewById(R.id.knowledge_religion_ranks);
 		EditText knowledgeReligionMiscEnter = (EditText) findViewById(R.id.knowledge_religion_misc_mod);
 		String knowledgeReligionRanks = knowledgeReligionRanksEnter.getText().toString().trim();
 		String knowledgeReligionMisc = knowledgeReligionMiscEnter.getText().toString().trim();
 		if (knowledgeReligionRanks.matches("")) {
 			knowledgeReligionRanks = "0";
 		}
 		
 		if (knowledgeReligionMisc.matches("")) {
 			knowledgeReligionMisc = "0";
 		}
 		if (!knowledgeReligionRanks.matches("")) {
 			int knowledgeReligionRank = Integer.parseInt(knowledgeReligionRanks);
 			Skill skill = new Skill(Skill.KNOWLEDGE_RELIGION_ID, "Knowledge (Religion)", 
 					AbilityName.INTELLIGENCE, knowledgeReligionRank, false);
 			if (!knowledgeReligionMisc.matches("")) {
 				int knowledgeReligionMod = Integer.parseInt(knowledgeReligionMisc);
 				skill.addModifier("knowledgeReligionMod", knowledgeReligionMod);
 			}
 			skills.add(skill);
 		}
 
 		// Linguistics
 		EditText linguisticsRanksEnter = (EditText) findViewById(R.id.linguistics_ranks);
 		EditText linguisticsMiscEnter = (EditText) findViewById(R.id.linguistics_misc_mod);
 		String linguisticsRanks = linguisticsRanksEnter.getText().toString().trim();
 		String linguisticsMisc = linguisticsMiscEnter.getText().toString().trim();
 		if (linguisticsRanks.matches("")) {
 			linguisticsRanks = "0";
 		}
 		
 		if (linguisticsMisc.matches("")) {
 			linguisticsMisc = "0";
 		}
 		if (!linguisticsRanks.matches("")) {
 			int linguisticsRank = Integer.parseInt(linguisticsRanks);
 			Skill skill = new Skill(Skill.LINGUISTICS_ID, "Linguistics", 
 					AbilityName.INTELLIGENCE, linguisticsRank, false);
 			if (!linguisticsMisc.matches("")) {
 				int linguisticsMod = Integer.parseInt(linguisticsMisc);
 				skill.addModifier("linguisticsMod", linguisticsMod);
 			}
 			skills.add(skill);
 		}
 
 		// Perception
 		EditText perceptionRanksEnter = (EditText) findViewById(R.id.perception_ranks);
 		EditText perceptionMiscEnter = (EditText) findViewById(R.id.perception_misc_mod);
 		String perceptionRanks = perceptionRanksEnter.getText().toString().trim();
 		String perceptionMisc = perceptionMiscEnter.getText().toString().trim();
 		if (perceptionRanks.matches("")) {
 			perceptionRanks = "0";
 		}
 		
 		if (perceptionMisc.matches("")) {
 			perceptionMisc = "0";
 		}
 		if (!perceptionRanks.matches("")) {
 			int perceptionRank = Integer.parseInt(perceptionRanks);
 			Skill skill = new Skill(Skill.PERCEPTION_ID, "Perception", 
 					AbilityName.WISDOM, perceptionRank, false);
 			if (!perceptionMisc.matches("")) {
 				int perceptionMod = Integer.parseInt(perceptionMisc);
 				skill.addModifier("perceptionMod", perceptionMod);
 			}
 			skills.add(skill);
 		}
 
 		// ... seriously I'm going to refactor this because there is a stupid amount of redundancy
 
 		// Perform1
 		Spinner perform1Spinner = (Spinner) findViewById(R.id.perform1_spinner);
 		// Gives a string representation of whatever item is selected in the spinner
 		String perform1 = perform1Spinner.getSelectedItem().toString();
 
 		EditText perform1RanksEnter = (EditText) findViewById(R.id.perform1_ranks);
 		EditText perform1MiscEnter = (EditText) findViewById(R.id.perform1_misc_mod);
 		String perform1Ranks = perform1RanksEnter.getText().toString().trim();
 		String perform1Misc = perform1MiscEnter.getText().toString().trim();
 		if (perform1Ranks.matches("")) {
 			perform1Ranks = "0";
 		}
 		
 		if (perform1Misc.matches("")) {
 			perform1Misc = "0";
 		}
 		if (!perform1Ranks.matches("")) {
 			int perform1Rank = Integer.parseInt(perform1Ranks);
 			Skill skill = new Skill(Skill.PERFORM_ID, "Perform1", perform1, 
 					AbilityName.CHARISMA, perform1Rank, false);
 			if (!perform1Misc.matches("")) {
 				int perform1Mod = Integer.parseInt(perform1Misc);
 				skill.addModifier("perform1Mod", perform1Mod);
 			}
 			skills.add(skill);
 		}
 
 		// Perform2
 		Spinner perform2Spinner = (Spinner) findViewById(R.id.perform2_spinner);
 		// Gives a string representation of whatever item is selected in the spinner
 		String perform2 = perform2Spinner.getSelectedItem().toString();
 
 		EditText perform2RanksEnter = (EditText) findViewById(R.id.perform2_ranks);
 		EditText perform2MiscEnter = (EditText) findViewById(R.id.perform2_misc_mod);
 		String perform2Ranks = perform2RanksEnter.getText().toString().trim();
 		String perform2Misc = perform2MiscEnter.getText().toString().trim();
 		if (perform2Ranks.matches("")) {
 			perform2Ranks = "0";
 		}
 		
 		if (perform2Misc.matches("")) {
 			perform2Misc = "0";
 		}
 		if (!perform2Ranks.matches("")) {
 			int perform2Rank = Integer.parseInt(perform2Ranks);
 			Skill skill = new Skill(Skill.PERFORM_ID, "Perform2", perform2, 
 					AbilityName.CHARISMA, perform2Rank, false);
 			if (!perform2Misc.matches("")) {
 				int perform2Mod = Integer.parseInt(perform2Misc);
 				skill.addModifier("perform2Mod", perform2Mod);
 			}
 			skills.add(skill);
 		}
 
 		// Profession1
 		Spinner profession1Spinner = (Spinner) findViewById(R.id.profession1_spinner);
 		// Gives a string representation of whatever item is selected in the spinner
 		String profession1 = profession1Spinner.getSelectedItem().toString();
 		EditText profession1RanksEnter = (EditText) findViewById(R.id.profession1_ranks);
 		EditText profession1MiscEnter = (EditText) findViewById(R.id.profession1_misc_mod);
 		String profession1Ranks = profession1RanksEnter.getText().toString().trim();
 		String profession1Misc = profession1MiscEnter.getText().toString().trim();
 		if (profession1Ranks.matches("")) {
 			profession1Ranks = "0";
 		}
 		
 		if (profession1Misc.matches("")) {
 			profession1Misc = "0";
 		}
 		if (!profession1Ranks.matches("")) {
 			int profession1Rank = Integer.parseInt(profession1Ranks);
 			Skill skill = new Skill(Skill.PROFESSION_ID, "Profession1", profession1, 
 					AbilityName.WISDOM, profession1Rank, false);
 			if (!profession1Misc.matches("")) {
 				int profession1Mod = Integer.parseInt(profession1Misc);
 				skill.addModifier("profession1Mod", profession1Mod);
 			}
 			skills.add(skill);
 		}
 
 		// Profession2
 		Spinner profession2Spinner = (Spinner) findViewById(R.id.profession2_spinner);
 		// Gives a string representation of whatever item is selected in the spinner
 		String profession2 = profession2Spinner.getSelectedItem().toString();
 		EditText profession2RanksEnter = (EditText) findViewById(R.id.profession2_ranks);
 		EditText profession2MiscEnter = (EditText) findViewById(R.id.profession2_misc_mod);
 		String profession2Ranks = profession2RanksEnter.getText().toString().trim();
 		String profession2Misc = profession2MiscEnter.getText().toString().trim();
 		if (profession2Ranks.matches("")) {
 			profession2Ranks = "0";
 		}
 		
 		if (profession2Misc.matches("")) {
 			profession2Misc = "0";
 		}
 		if (!profession2Ranks.matches("")) {
 			int profession2Rank = Integer.parseInt(profession2Ranks);
 			Skill skill = new Skill(Skill.PROFESSION_ID, "Profession2", profession2, 
 					AbilityName.WISDOM, profession2Rank, false);
 			if (!profession2Misc.matches("")) {
 				int profession2Mod = Integer.parseInt(profession2Misc);
 				skill.addModifier("profession2Mod", profession2Mod);
 			}
 			skills.add(skill);
 		}
 
 		// Ride
 		EditText rideRanksEnter = (EditText) findViewById(R.id.ride_ranks);
 		EditText rideMiscEnter = (EditText) findViewById(R.id.ride_misc_mod);
 		String rideRanks = rideRanksEnter.getText().toString().trim();
 		String rideMisc = rideMiscEnter.getText().toString().trim();
 		if (rideRanks.matches("")) {
 			rideRanks = "0";
 		}
 		
 		if (rideMisc.matches("")) {
 			rideMisc = "0";
 		}
 		if (!rideRanks.matches("")) {
 			int rideRank = Integer.parseInt(rideRanks);
 			Skill skill = new Skill(Skill.RIDE_ID, "Ride", 
 					AbilityName.DEXTERITY, rideRank, false);
 			if (!rideMisc.matches("")) {
 				int rideMod = Integer.parseInt(rideMisc);
 				skill.addModifier("rideMod", rideMod);
 			}
 			skills.add(skill);
 		}
 
 		// Sense Motive
 		EditText senseMotiveRanksEnter = (EditText) findViewById(R.id.sense_motive_ranks);
 		EditText senseMotiveMiscEnter = (EditText) findViewById(R.id.sense_motive_misc_mod);
 		String senseMotiveRanks = senseMotiveRanksEnter.getText().toString().trim();
 		String senseMotiveMisc = senseMotiveMiscEnter.getText().toString().trim();
 		if (senseMotiveRanks.matches("")) {
 			senseMotiveRanks = "0";
 		}
 		
 		if (senseMotiveMisc.matches("")) {
 			senseMotiveMisc = "0";
 		}
 		if (!senseMotiveRanks.matches("")) {
 			int senseMotiveRank = Integer.parseInt(senseMotiveRanks);
 			Skill skill = new Skill(Skill.SENSE_MOTIVE_ID, "Sense Motive", 
 					AbilityName.WISDOM, senseMotiveRank, false);
 			if (!senseMotiveMisc.matches("")) {
 				int senseMotiveMod = Integer.parseInt(senseMotiveMisc);
 				skill.addModifier("senseMotiveMod", senseMotiveMod);
 			}
 			skills.add(skill);
 		}
 
 		// Sleight of Hand
 		EditText sleightOfHandRanksEnter = (EditText) findViewById(R.id.sleight_of_hand_ranks);
 		EditText sleightOfHandMiscEnter = (EditText) findViewById(R.id.sleight_of_hand_misc_mod);
 		String sleightOfHandRanks = sleightOfHandRanksEnter.getText().toString().trim();
 		String sleightOfHandMisc = sleightOfHandMiscEnter.getText().toString().trim();
 		if (sleightOfHandRanks.matches("")) {
 			sleightOfHandRanks = "0";
 		}
 		
 		if (sleightOfHandMisc.matches("")) {
 			sleightOfHandMisc = "0";
 		}
 		if (!sleightOfHandRanks.matches("")) {
 			int sleightOfHandRank = Integer.parseInt(sleightOfHandRanks);
 			Skill skill = new Skill(Skill.SLEIGHT_OF_HAND_ID, "Sleight of Hand", 
 					AbilityName.DEXTERITY, sleightOfHandRank, false);
 			if (!sleightOfHandMisc.matches("")) {
 				int sleightOfHandMod = Integer.parseInt(sleightOfHandMisc);
 				skill.addModifier("sleightOfHandMod", sleightOfHandMod);
 			}
 			skills.add(skill);
 		}
 
 		// Spellcraft
 		EditText spellcraftRanksEnter = (EditText) findViewById(R.id.spellcraft_ranks);
 		EditText spellcraftMiscEnter = (EditText) findViewById(R.id.spellcraft_misc_mod);
 		String spellcraftRanks = spellcraftRanksEnter.getText().toString().trim();
 		String spellcraftMisc = spellcraftMiscEnter.getText().toString().trim();
 		if (spellcraftRanks.matches("")) {
 			spellcraftRanks = "0";
 		}
 		
 		if (spellcraftMisc.matches("")) {
 			spellcraftMisc = "0";
 		}
 		if (!spellcraftRanks.matches("")) {
 			int spellcraftRank = Integer.parseInt(spellcraftRanks);
 			Skill skill = new Skill(Skill.SPELLCRAFT_ID, "Spellcraft", 
 					AbilityName.INTELLIGENCE, spellcraftRank, false);
 			if (!spellcraftMisc.matches("")) {
 				int spellcraftMod = Integer.parseInt(spellcraftMisc);
 				skill.addModifier("spellcraftMod", spellcraftMod);
 			}
 			skills.add(skill);
 		}
 
 		// Stealth
 		EditText stealthRanksEnter = (EditText) findViewById(R.id.stealth_ranks);
 		EditText stealthMiscEnter = (EditText) findViewById(R.id.stealth_misc_mod);
 		String stealthRanks = stealthRanksEnter.getText().toString().trim();
 		String stealthMisc = stealthMiscEnter.getText().toString().trim();
 		if (stealthRanks.matches("")) {
 			stealthRanks = "0";
 		}
 		
 		if (stealthMisc.matches("")) {
 			stealthMisc = "0";
 		}
 		if (!stealthRanks.matches("")) {
 			int stealthRank = Integer.parseInt(stealthRanks);
 			Skill skill = new Skill(Skill.STEALTH_ID, "Stealth", 
 					AbilityName.DEXTERITY, stealthRank, false);
 			if (!stealthMisc.matches("")) {
 				int stealthMod = Integer.parseInt(stealthMisc);
 				skill.addModifier("stealthMod", stealthMod);
 			}
 			skills.add(skill);
 		}
 
 		// Survival
 		EditText survivalRanksEnter = (EditText) findViewById(R.id.survival_ranks);
 		EditText survivalMiscEnter = (EditText) findViewById(R.id.survival_misc_mod);
 		String survivalRanks = survivalRanksEnter.getText().toString().trim();
 		String survivalMisc = survivalMiscEnter.getText().toString().trim();
 		if (survivalRanks.matches("")) {
 			survivalRanks = "0";
 		}
 		
 		if (survivalMisc.matches("")) {
 			survivalMisc = "0";
 		}
 		if (!survivalRanks.matches("")) {
 			int survivalRank = Integer.parseInt(survivalRanks);
 			Skill skill = new Skill(Skill.SURVIVAL_ID, "Survival", 
 					AbilityName.WISDOM, survivalRank, false);
 			if (!survivalMisc.matches("")) {
 				int survivalMod = Integer.parseInt(survivalMisc);
 				skill.addModifier("survivalMod", survivalMod);
 			}
 			skills.add(skill);
 		}
 
 		// Swim
 		EditText swimRanksEnter = (EditText) findViewById(R.id.swim_ranks);
 		EditText swimMiscEnter = (EditText) findViewById(R.id.swim_misc_mod);
 		String swimRanks = swimRanksEnter.getText().toString().trim();
 		String swimMisc = swimMiscEnter.getText().toString().trim();
 		if (swimRanks.matches("")) {
 			swimRanks = "0";
 		}
 		
 		if (swimMisc.matches("")) {
 			swimMisc = "0";
 		}
 		if (!swimRanks.matches("")) {
 			int swimRank = Integer.parseInt(swimRanks);
 			Skill skill = new Skill(Skill.SWIM_ID, "Swim", 
 					AbilityName.STRENGTH, swimRank, false);
 			if (!swimMisc.matches("")) {
 				int swimMod = Integer.parseInt(swimMisc);
 				skill.addModifier("swimMod", swimMod);
 			}
 			skills.add(skill);
 		}
 
 		// Use Magic Device
 		EditText useMagicDeviceRanksEnter = (EditText) findViewById(R.id.use_magic_device_ranks);
 		EditText useMagicDeviceMiscEnter = (EditText) findViewById(R.id.use_magic_device_misc_mod);
 		String useMagicDeviceRanks = useMagicDeviceRanksEnter.getText().toString().trim();
 		String useMagicDeviceMisc = useMagicDeviceMiscEnter.getText().toString().trim();
 		if (useMagicDeviceRanks.matches("")) {
 			useMagicDeviceRanks = "0";
 		}
 		
 		if (useMagicDeviceMisc.matches("")) {
 			useMagicDeviceMisc = "0";
 		}
 		if (!useMagicDeviceRanks.matches("")) {
 			int useMagicDeviceRank = Integer.parseInt(useMagicDeviceRanks);
 			Skill skill = new Skill(Skill.USE_MAGIC_DEVICE_ID, "Use Magic Device", 
 					AbilityName.CHARISMA, useMagicDeviceRank, false);
 			if (!useMagicDeviceMisc.matches("")) {
 				int useMagicDeviceMod = Integer.parseInt(useMagicDeviceMisc);
 				skill.addModifier("useMagicDeviceMod", useMagicDeviceMod);
 			}
 			skills.add(skill);
 		}
 
 		// clear old data from DB
 		SQLiteHelperSkills.db.delete(SQLiteHelperSkills.TABLE_NAME,
 				SQLiteHelperSkills.COLUMN_CHAR_ID + " = " + charID, null);
 
 		// write all data to DB
 		for (Skill s : skills) {
 			s.writeToDB(charID);
 		}
 
 		// return to character creation main screen
 		Intent intent = new Intent(this, CharCreateMainActivity.class);
 		intent.putExtra("cid", charID);
 		startActivity(intent);
 	}	
 }
