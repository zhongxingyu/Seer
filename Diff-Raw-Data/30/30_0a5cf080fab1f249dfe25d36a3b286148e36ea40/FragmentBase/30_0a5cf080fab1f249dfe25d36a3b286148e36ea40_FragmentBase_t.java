 package com.dmtprogramming.pathfindercombat;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.dmtprogramming.pathfindercombat.database.DatabaseHelper;
 import com.dmtprogramming.pathfindercombat.models.*;
 import com.dmtprogramming.pathfindercombat.modifier.ModifierBase;
 import com.dmtprogramming.pathfindercombat.modifier.ModifierBase.ModifierField;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.ForeignCollection;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public abstract class FragmentBase extends Fragment {
 
 	private static final String TAG = "PFCombat";
 	
 	protected View _view;
 	protected List<ModifierBase> _mods;
 	private CharacterUpdateReceiver _receiver;
     
     public abstract void onAfterUpdateCharacter(String field);
     protected abstract void populateStats(String field);
 	
 	private DatabaseHelper databaseHelper = null;
     
     public FragmentBase() {
      	_mods = new ArrayList<ModifierBase>();
     }
     
     public void setupIntentFilter() {
      	IntentFilter filter = new IntentFilter("com.dmtprogramming.pathfindercombat.UPDATE_CHARACTER");
      	_receiver = new CharacterUpdateReceiver(this);
      	getActivity().registerReceiver(_receiver, filter);
     }
     
     @Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
 				Log.d(TAG, "FragmentBase: requestCode == " + requestCode);
 				Log.d(TAG, "FragmentBase: resultCode == " + resultCode);
         
         switch(requestCode) {
         case(2):
         	if (resultCode == Activity.RESULT_OK) { 
 	        	Bundle extras = data.getExtras();
 	        	if (extras != null) {
 	        	
 	        	String field = extras.getString("FIELD");
 	        	if (field != null) {
 	        		updateCharacter(field);
 	        	}
         	}
         }
         break;
         }
     }
     
 	protected DatabaseHelper getHelper() {
 	    if (databaseHelper == null) {
 	        databaseHelper =
 	            OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
 	    }
 	    return databaseHelper;
 	}
     
     // saves the character after a field update and refreshes everything
 	public void updateCharacter(String field) {
 		Log.d(TAG, "FragmentBase: updateCharacter() field = " + field);
 		Dao<PFCharacter, Integer> dao;
 		PFCharacter cha = getCharacter();
 		try {
 			dao = getHelper().getCharacterDao();
 			dao.update(cha);
 			//cha = dao.queryForId((int) cha.getId());
 			Dao<Weapon, Integer> weaponDao = getHelper().getWeaponDao();
 			weaponDao.refresh(cha.getWeapon());
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		PFCombatApplication app = PFCombatApplication.getApplication();
 		app.setCurrentCharacter(cha);
 		
 		Intent intent = new Intent();
 		intent.setAction("com.dmtprogramming.pathfindercombat.UPDATE_CHARACTER");
 		intent.putExtra("field", new String(field));
 		getActivity().sendBroadcast(intent);
 	}
 	
 	public void refreshCharacter(String field) {
 		Log.d(TAG, "FragmentBase: refreshCharacter() field = " + field);
 		Dao<PFCharacter, Integer> dao;
 		PFCharacter cha = getCharacter();
 		try {
 			dao = getHelper().getCharacterDao();
 			dao.refresh(cha);
 			Dao<Weapon, Integer> weaponDao = getHelper().getWeaponDao();
 			weaponDao.refresh(cha.getWeapon());
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		PFCombatApplication app = PFCombatApplication.getApplication();
 		app.setCurrentCharacter(cha);
 
 		Intent intent = new Intent();
 		intent.setAction("com.dmtprogramming.pathfindercombat.UPDATE_CHARACTER");
 		intent.putExtra("field", field);
 		getActivity().sendBroadcast(intent);		
 	}
 	
 	public PFCharacter getCharacter() {
 		PFCombatApplication app = PFCombatApplication.getApplication();
 		return app.getCurrentCharacter();
 	}
 	
     protected void setupEditTextTrigger(int id, String field) {
     	EditText e = (EditText) findViewById(id);
     	e.addTextChangedListener(new CustomTextWatcher(e, field, this));
     }
     
     protected void setupSpinnerTrigger(int id, String field) {
     	Spinner e = (Spinner) findViewById(id);
 		e.setOnItemSelectedListener(new CustomSpinnerWatcher(e, field, this));
     }
 	
 	protected void populateField(int id, String ignore, ModifierField field, String value) {
 		if (!ignore.equals(field)) {
 			View v = findViewById(id);
 			String type = v.getClass().getName();
 			if (type.equals("android.widget.EditText")) {
 				EditText e = (EditText) v;
				//e.clearFocus();
 	    		e.setText(value);
					e.setSelection(e.getText().length());
 			} else if (type.equals("android.widget.TextView")) {
 				TextView e = (TextView) v;
 				e.clearFocus();
 				e.setText(applyToggles(field, value));
 			}
 		}
 	}
 	
 	public void populateSpinner(int id, String selected, String[] options) {
 		Spinner spinner = (Spinner) _view.findViewById(id);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, options);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);
 		
 		for (int i = 0; i < options.length; i++) {
 			String test = options[i];
 			if (test.equals(selected)) {
 				spinner.setSelection(i);
 			}
 		}    	
 	}
 	 
     // apply the stats from all of the toggles to a single field
 	protected String applyToggles(ModifierField field, String value) {
 		Log.d(TAG, "FragmentBase: applyToggles(" + field + ", " + value +")");
 		
 		// apply modifiers
 		for (int i = 0; i < _mods.size(); i++) {
 			ModifierBase mod = _mods.get(i);
 			value = mod.apply(field, value);
 		}
 		Log.d(TAG, "FragmentBase: Toggle modifier applyed");
 		
 		// apply conditions
	  Log.d(TAG, "FragmentBase: " + getCharacter().getConditions());
 		if( getCharacter().getConditions() != null ){
 		  ForeignCollection<Condition> conditions = getCharacter().getConditions();
   		Iterator<Condition> iter = conditions.iterator();
	  	//Log.d(TAG, "FragmentBase: Condition = " + conditions + " Iterator = " + iter);
   		while(iter.hasNext()) {
   			Condition cond = iter.next();
   			ModifierBase mod = cond.getModifier();
   			mod.setEnabled(true);
   			value = mod.apply(field, value);
   			Log.d(TAG, "FragmentBase: applying condition modifier name = " + mod.name());
   		}	
 		}
 		return value;
 	}
 	
     protected View findViewById(int id) {
     	return _view.findViewById(id);
     }
 
 	protected int parseIntField(int id) {
     	View v = findViewById(id);
 		String type = v.getClass().getName();
 		int i = 0;
 		if (type.equals("android.widget.EditText")) {
 			EditText tv = (EditText) v;
 			i = Integer.parseInt(tv.getText().toString());
 		} else if (type.equals("android.widget.TextView")) {
 			TextView tv = (TextView) v;
 			i = Integer.parseInt(tv.getText().toString());
 		} else if (type.equals("android.widget.Spinner")) {
 			Spinner sp = (Spinner) v;
 			i = Integer.parseInt(sp.getSelectedItem().toString());
 		}
 		return i;
     }
     
     @Override
 	public void onResume() {
      	IntentFilter filter = new IntentFilter("com.dmtprogramming.pathfindercombat.UPDATE_CHARACTER");
     	getActivity().registerReceiver(_receiver, filter);
     	refreshCharacter("");
 		super.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		getActivity().unregisterReceiver(_receiver);
 		super.onPause();
 	}
 	
 	@Override
 	public void onDestroy() {
 	    super.onDestroy();
 	    if (databaseHelper != null) {
 	        OpenHelperManager.releaseHelper();
 	        databaseHelper = null;
 	    }
 	}
 }
