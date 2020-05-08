 package com.dmtprogramming.pathfindercombat;
 
 import com.dmtprogramming.pathfindercombat.database.DatabaseHelper;
 import com.dmtprogramming.pathfindercombat.models.*;
 import com.dmtprogramming.pathfindercombat.modifier.ModifierBase.ModifierField;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class CharacterInfoFragment extends FragmentBase {
 	
 	private static final String TAG = "PFCombat";
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		_view = inflater.inflate(R.layout.character_info_fragment, container, false);
 		
 		setupIntentFilter();
 		
         setupView();
         populateStats("");
 
         setupTriggers();
 		return _view;
 	}
     
     // populate all of the stats from the db model
     // also uses the toggles to change the data
     public void populateStats(String f) {
 			Log.d(TAG, "CharacterInfoFragment: populateStats(" + f + ")");
     	
     	// save some typing
     	PFCharacter c = getCharacter();
			Log.d(TAG, "CharacterInfoFragment: Character: " + c.getName());
     	
     	populateField(R.id.txtCharacter, f, ModifierField._none, c.getName());
     	//populateField(R.id.txtPlayer, f, "player", c.getPlayer());
     	populateField(R.id.txtStr, f, ModifierField._str, String.valueOf(c.getStr()));
     	populateField(R.id.txtDex, f, ModifierField._dex, String.valueOf(c.getDex()));
     	populateField(R.id.txtCon, f, ModifierField._con, String.valueOf(c.getCon()));
     	populateField(R.id.txtInt, f, ModifierField._int, String.valueOf(c.getInt()));
     	populateField(R.id.txtWis, f, ModifierField._wis, String.valueOf(c.getWis()));
     	populateField(R.id.txtCha, f, ModifierField._cha, String.valueOf(c.getCha()));
     	populateField(R.id.txtStrMod, f, ModifierField._str_mod, String.valueOf(c.getStrModDisplay()));
     	populateField(R.id.txtDexMod, f, ModifierField._dex_mod, String.valueOf(c.getDexModDisplay()));
     	populateField(R.id.txtConMod, f, ModifierField._con_mod, String.valueOf(c.getConModDisplay()));
     	populateField(R.id.txtIntMod, f, ModifierField._int_mod, String.valueOf(c.getIntModDisplay()));
     	populateField(R.id.txtWisMod, f, ModifierField._wis_mod, String.valueOf(c.getWisModDisplay()));
     	populateField(R.id.txtChaMod, f, ModifierField._cha_mod, String.valueOf(c.getChaModDisplay()));
     }
 
 	// set up the events for the toggle buttons
     private	 void setupView() {
     	String[] levels = { "1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20" };
     	populateSpinner(R.id.spinClass, getCharacter().getCharacterClass(), PFCharacter.CHARACTER_CLASS_NAMES);
     	populateSpinner(R.id.spinLevel, String.valueOf(getCharacter().getLevel()), levels);
     }
 
     // set up the events for when text fields are updated by the user
     private void setupTriggers() {
     	setupEditTextTrigger(R.id.txtCharacter, "name");
     	//setupEditTextTrigger(R.id.txtPlayer, "player");
     	setupEditTextTrigger(R.id.txtStr, "str");
     	setupEditTextTrigger(R.id.txtDex, "dex");
     	setupEditTextTrigger(R.id.txtCon, "con");
     	setupEditTextTrigger(R.id.txtInt, "int");
     	setupEditTextTrigger(R.id.txtWis, "wis");
     	setupEditTextTrigger(R.id.txtCha, "cha");
     	
     	setupSpinnerTrigger(R.id.spinClass, DatabaseHelper._c_characters_character_class);
     	setupSpinnerTrigger(R.id.spinLevel, DatabaseHelper._c_characters_level);
     }
     
     @Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	public void onAfterUpdateCharacter(String field) {
 		populateStats(field);
 	}
 }
 
