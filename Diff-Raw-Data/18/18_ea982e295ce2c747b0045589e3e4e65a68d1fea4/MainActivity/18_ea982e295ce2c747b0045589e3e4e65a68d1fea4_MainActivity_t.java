 package com.example.combatcalculator;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 import com.tree.combatcalculator.AtkVar;
 import com.tree.combatcalculator.AttackCalculator;
 import com.tree.combatcalculator.AttackModel;
 import com.tree.combatcalculator.DefendModel;
 import com.tree.combatcalculator.Weapon;
 
 public class MainActivity extends FragmentActivity {
 	public final static String ATTACKER = "com.example.myfirstapp.ATTACKER";
 	public final static String DEFENDER = "com.example.myfirstapp.DEFENDER";
 	public final static String WEAPONS = "com.example.myfirstapp.WEAPONS";
 	public final static String SITUATION = "com.example.myfirstapp.SITUATION";
 	public final static String FOCUS = "com.example.myfirstapp.FOCUS";
 	public final static String ATTACKER_VARS = "com.example.myfirstapp.ATTACKER_VARS";
 	public final static String DEFENDER_VARS = "com.example.myfirstapp.DEFENDER_VARS";
 	public final static String WEAPONS_VARS = "com.example.myfirstapp.WEAPONS_VARS";
 	public final static String SITUATION_VARS = "com.example.myfirstapp.SITUATION_VARS";
 	public final static String ATK_CALC = "com.example.myfirstapp.ATK_CALC";
 	public final static String OPTIMIZATION = "com.example.myfirstapp.OPTIMIZATION";
 	
 	
 	
 	//initialized at the beginning of the program because it has to read xml to initialize
 	private AttackCalculator atkCalc;
 	
 	//variable lists for the atkvar users
 	private ArrayList<AtkVar> situation;
 	private ArrayList<AttackProperty> attackerVars;
 	private ArrayList<AttackProperty> defenderVars;
 	private ArrayList<ArrayList<AttackProperty>> weaponsVars;
 	
 	private ViewGroup mWeaponGroup;
 
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_main);
 		
 		mWeaponGroup = (ViewGroup) findViewById(R.id.container);
 		
 		
 		
 		boolean saved;
 		
 		 // Check whether we're recreating a previously destroyed instance
 	    if (savedInstanceState != null) {
 	        // Restore value of members from saved state
 	    	situation = savedInstanceState.getParcelableArrayList(SITUATION_VARS);
 	    	attackerVars = savedInstanceState.getParcelableArrayList(ATTACKER_VARS);
 	    	defenderVars = savedInstanceState.getParcelableArrayList(DEFENDER_VARS);
 	    	weaponsVars = (ArrayList<ArrayList<AttackProperty>>) savedInstanceState.getSerializable(WEAPONS_VARS);
 	    	atkCalc = savedInstanceState.getParcelable(ATK_CALC);
 	    	ArrayList<Weapon> weapons = savedInstanceState.getParcelableArrayList(WEAPONS);
 	    	
 	    	if (weapons.size() >= mWeaponGroup.getChildCount()){
 	    		mWeaponGroup.removeAllViews();
 	    		initWeapons(weapons);
 	    	}
 	    	
 	    	saved = true;
 	    	
 	    	
 	    } else {
 	        //otherwise create new ones when a new activity is created
 	    	mWeaponGroup.removeAllViews();
 	    	situation = new ArrayList<AtkVar>();
 	    	attackerVars = new ArrayList<AttackProperty>();
 	    	defenderVars = new ArrayList<AttackProperty>();
 	    	weaponsVars = new ArrayList<ArrayList<AttackProperty>>();
 	    	saved = false;
 	    	
 	    	
 	    	
 	    	
 	    }
 	    
 	    
 	    setupSpinner(R.id.mat_entry, R.array.atk_array, saved, true);
 		setupSpinner(R.id.rat_entry, R.array.atk_array, saved, true);
 		setupSpinner(R.id.focus_entry, R.array.focus_array, saved, false);
 		setupSpinner(R.id.def_entry, R.array.def_array, saved, true);
 		setupSpinner(R.id.arm_entry, R.array.arm_array, saved, true);
 
 		
 		
 
 	}
 	
 	/**
      *Initializes a spinner with the given array id and spinner id, sets it to default value if it 
      *isn't coming from a previous screen
     */
 	private void setupSpinner(int entry, int array, boolean saved, boolean useAve){
 		
 		Spinner spinner = (Spinner) findViewById(entry);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
 		        array, android.R.layout.simple_spinner_item);
 		
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);
 		
 		if (useAve)
 			spinner.setSelection(spinner.getCount()/2);
 		
 		
 	}
 	
 	/**
      *Initializes a spinner with the given array id and spinner id, sets it to default value if it 
      *isn't coming from a previous screen
     */
 	private void setupSpinner(Spinner spinner, int array, boolean saved, boolean useAve){
 		
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
 		        array, android.R.layout.simple_spinner_item);
 		
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);
 		
 		if (useAve)
 			spinner.setSelection(spinner.getCount()/2);
 		
 		
 	}
 	
 	/**
      *Defaults all the spinners
     */
 	private void setDefaultSpinners(){
 		
 		
 	}
 	
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 	    // Save the user's current state
 	    savedInstanceState.putParcelableArrayList(SITUATION_VARS, situation);
 	    savedInstanceState.putParcelableArrayList(ATTACKER_VARS, attackerVars);
 	    savedInstanceState.putParcelableArrayList(DEFENDER_VARS, defenderVars);
 	    savedInstanceState.putParcelableArrayList(WEAPONS, getWeapons(mWeaponGroup));
 	    savedInstanceState.putSerializable(WEAPONS_VARS, weaponsVars);
 	    
 	    
 	    // Always call the superclass so it can save the view hierarchy state
 	    super.onSaveInstanceState(savedInstanceState);
 	}
 	
 	public void onDestroy() {
 		
 		//mWeaponGroup.removeAllViews();
 		
         super.onDestroy();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	
 	
 	/**
      * Creates the various objects from the fields that were filled
      * and sends them to the CalcDisplayActivity for processing and display
     */
 	public void sendToCalc(View view){
 		
 		String errorMessage = checkValidity();
 		
 		if (errorMessage == null){
 		
 			Intent intent = new Intent(this, CalcDisplayActivity.class);
 			
 			int focus = prepAttacker(intent);
 			prepDefender(intent);
 			prepSituation(intent);
 			//prepWeapons(intent);
 			
 			intent.putExtra(OPTIMIZATION, 0);
 			intent.putExtra(FOCUS, focus);
 			
 		    startActivity(intent);
 	    
 		}else{
 			
 			DialogFragment newFragment = new ValidFragment();
 			Bundle args = new Bundle();
 
 			args.putString(ValidFragment.TYPE_ERROR, errorMessage);
 
 			newFragment.setArguments(args);
 			
 			newFragment.show(getSupportFragmentManager(), "validity");
 
 		}
 		
 	}
 	
 	
 	/**
      * Checks to make sure all the variables needed for the calculator to work are present, returns null if they
      * are, otherwise returns a string with the correct message to tell the user what is wrong
     */
 	private String checkValidity(){
 		
 		if (mWeaponGroup.getChildCount() == 0)
 			return "Need at least one weapon";
 		
 		
 		return null;
 	}
 	
 	/**
      * Sends the current data on the attacker to the options dialog, requests an attacker response
     */
 	public void sendToAttackerOptions(View view){
 		
 		Intent intent = new Intent(this, OptionsActivity.class);
 		
 		intent.putParcelableArrayListExtra(OptionsActivity.PREV_CHOICE, attackerVars);
 		intent.putExtra(OptionsActivity.TYPE_CHOSEN, ConfigManager.ATTACKER);
 		
 	    startActivityForResult(intent, ConfigManager.ATTACKER_REQ);
 
 	}
 	
 	/**
      * Sends the current data on the defender to the options dialog, requests an defender response
     */
 	public void sendToDefenderOptions(View view){
 		
 		Intent intent = new Intent(this, OptionsActivity.class);
 		
 		intent.putParcelableArrayListExtra(OptionsActivity.PREV_CHOICE, defenderVars);
 		intent.putExtra(OptionsActivity.TYPE_CHOSEN, ConfigManager.DEFENDER);
 		
 	    startActivityForResult(intent, ConfigManager.DEFENDER_REQ);
 		
 	}
 	
 	/**
      * Sends the current data on the defender to the options dialog, requests an defender response
     */
 	public void sendToWeaponOptions(View view){
 		
 		Intent intent = new Intent(this, OptionsActivity.class);
 		
 		int currentWeapon = mWeaponGroup.indexOfChild((ViewGroup)view.getParent().getParent().getParent());
 		
 		intent.putParcelableArrayListExtra(OptionsActivity.PREV_CHOICE, weaponsVars.get(currentWeapon));
 		
 		intent.putExtra(OptionsActivity.TYPE_CHOSEN, ConfigManager.WEAPONS);
 		
 		intent.putExtra(OptionsActivity.NUM_CHOSEN, currentWeapon);
 		
 	    startActivityForResult(intent, ConfigManager.WEAPON_REQ);
 		
 	}
 	
 		
 
 	@Override
     protected void onActivityResult(int pRequestCode, int resultCode, Intent data){
 		
 		System.out.println("Returned: " + resultCode);
 	
 		if (resultCode == -1)
 			finish();
 		else if (resultCode == ConfigManager.ATTACKER_REQ){
 			
 			ArrayList<AttackProperty> newList = data.getParcelableArrayListExtra(OptionsActivity.CHANGED);
 			for (AttackProperty atk : newList)
 				System.out.println("Returned: " + atk.toString());
 			
 			if (newList != null)
 				attackerVars = new ArrayList<AttackProperty>(newList);
 			
 		}else if (resultCode == ConfigManager.DEFENDER_REQ){
 			
 			ArrayList<AttackProperty> newList = data.getParcelableArrayListExtra(OptionsActivity.CHANGED);
 			
 			if (newList != null)
 				defenderVars = new ArrayList<AttackProperty>(newList);
 			
 		}else if (resultCode == ConfigManager.WEAPON_REQ){
 			
 			ArrayList<AttackProperty> newList = data.getParcelableArrayListExtra(OptionsActivity.CHANGED);
 			int location = data.getIntExtra(OptionsActivity.NUM_CHOSEN, 0);
 			
 			if (newList != null)
 				weaponsVars.set(location, newList);
 			
 		}
 			
 		
 			
 	}
 
 	
 	/**
      * Sets up the correct variables for the attacker, based on entries
     */
 	private int prepAttacker(Intent intent){
 		
 		int focus = 0;
 		
 		AttackModel attacker = new AttackModel(0, 0);
 		
 	    Spinner matText = (Spinner) findViewById(R.id.mat_entry);
 	    Spinner ratText = (Spinner) findViewById(R.id.rat_entry);
 	    Spinner focusText = (Spinner) findViewById(R.id.focus_entry);
 	    //EditText numAtkText = (EditText) findViewById(R.id.num_atk_entry);
 
 	    attacker.setMat(Integer.parseInt(matText.getSelectedItem().toString()));
 	
 	    attacker.setRat(Integer.parseInt(ratText.getSelectedItem().toString()));
 
 	    focus = Integer.parseInt(focusText.getSelectedItem().toString());
 	    
 	    //if (numAtkText.getText().toString() != "")
 	    //	attacker.setFocus(Integer.parseInt(numAtkText.getText().toString()));
 	    
 	    //attacker.setVariables(attackerVars);
 	    
 	    prepWeapons(attacker);
 	    
 	    attacker.addVariables(AttackProperty.convertAttackProperties(attackerVars));
 	    
 	    intent.putExtra(ATTACKER, attacker);
 	    
 	    return focus;
 	    
 	}
 	
 	
 	/**
      * Sets up the correct variables for the defender, based on entries
     */
 	private void prepDefender(Intent intent){
 		
 		
 		DefendModel defender = new DefendModel(0,0);
 		
 	    Spinner defText = (Spinner) findViewById(R.id.def_entry);
 	    Spinner ArmText = (Spinner) findViewById(R.id.arm_entry);
 	    EditText healthText = (EditText) findViewById(R.id.health_entry);
 
 	    
 	    defender.setDef(Integer.parseInt(defText.getSelectedItem().toString()));
 	    
 	    defender.setArm(Integer.parseInt(ArmText.getSelectedItem().toString()));
 	    
 	    if (healthText.getText().toString() != "")
 	    	defender.setHealth(Integer.parseInt(healthText.getText().toString()));
 	    
	    defender.addVariables(AttackProperty.convertAttackProperties(defenderVars));
 	    
 	    intent.putExtra(DEFENDER, defender);
 	    
 		
 	}
 	
 	/**
      * Sets up the correct variables for the situation, based on entries
      * if it is charging, add it, since it will not have been added prior to this
     */
 	private void prepSituation(Intent intent){
 		
 		situation = new ArrayList<AtkVar>();
 		
 		CheckBox checkCharging = (CheckBox) findViewById(R.id.charging_entry);
 		
 		if (checkCharging.isChecked()){
 			situation.add(new AtkVar(AtkVar.CHARGING));
 			situation.add(new AtkVar(AtkVar.CHARGE_DAMAGE));
 		}
 		
 		CheckBox checkRanged = (CheckBox) findViewById(R.id.ranged_situation_entry);
 		
 		if (checkRanged.isChecked()){
 			situation.add(new AtkVar(AtkVar.RANGED));
 		}
 		
 		intent.putParcelableArrayListExtra(SITUATION, situation);
 		
 	}
 	
 	private void setSituationVars(ArrayList<AtkVar> newVars){ 
 		situation = new ArrayList<AtkVar>(newVars);
 	}
 	
 	/**
      * Gathers all the weapon variables from the various viewgroups and converts it into an arraylist for the attacker
     */
 	private void prepWeapons(AttackModel attacker){
 		
 		ArrayList<Weapon> weapons = new ArrayList<Weapon>();
 		
 		//loop through entire group of weapons and add each one
 		for (int i = 0; i < mWeaponGroup.getChildCount(); i++){
 		
 			ViewGroup curView = (ViewGroup) mWeaponGroup.getChildAt(i);
 			
 			ArrayList<AtkVar> weaponVars = new ArrayList<AtkVar>();
 			
 			CheckBox checkRanged = (CheckBox) curView.findViewById(R.id.ranged_entry);
 			
 			Spinner rofText = (Spinner) curView.findViewById(R.id.rof_entry);
 			Spinner powText = (Spinner) curView.findViewById(R.id.pow_entry);
 			
 			int rof;
 			
 			if (rofText.getSelectedItem().toString().equals("Infinite"))
 				rof = -1;
 			else
 				rof = Integer.parseInt(rofText.getSelectedItem().toString());
 			
 			Weapon w = new Weapon(Integer.parseInt(powText.getSelectedItem().toString()),
 								  checkRanged.isChecked(),
 								  rof,
 								  weaponVars);
 		    
 		   weapons.add(w);
 		   
 		   System.out.println("weapon: " + i + " is ranged=" + w.getRanged());
 		    
 		}
 		
 		attacker.setWeapons(weapons);
 		
 	}
 	
 	
 	/**
      * Gets all the weapons from the given viewgroup, or null if there aren't any
     */
 	private ArrayList<Weapon> getWeapons(ViewGroup weaponViews){
 		
 		ArrayList<Weapon> weapons = new ArrayList<Weapon>();
 		
 		//loop through entire group of weapons and add each one
 		for (int i = 0; i < mWeaponGroup.getChildCount(); i++){
 		
 			ViewGroup curView = (ViewGroup) mWeaponGroup.getChildAt(i);
 			
 			ArrayList<AtkVar> weaponVars = new ArrayList<AtkVar>();
 			
 			//get the correct view for the given weapon
 			CheckBox checkRanged = (CheckBox) curView.findViewById(R.id.ranged_entry);
 			Spinner rofText = (Spinner) curView.findViewById(R.id.rof_entry);
 			Spinner powText = (Spinner) curView.findViewById(R.id.pow_entry);
 		    
 			//get the values from the spinners and create a new weapon
 			int rof;
 			
 			if (rofText.getSelectedItem().toString().equals("Infinite"))
 				rof = -1;
 			else
 				rof = Integer.parseInt(rofText.getSelectedItem().toString());
 			
 			
 			Weapon w = new Weapon(Integer.parseInt(powText.getSelectedItem().toString()),
 								  checkRanged.isChecked(),
 								  rof,
 								  weaponVars);
 		    
 		   weapons.add(w);
 		    
 		}
 		
 		return weapons;
 		
 		
 	}
 	
 	/**
      * Adds a weapon list item to the view group for the weapons
     */
 	public void addWeapons(View view) {
         // Instantiate a new "row" view.
         final ViewGroup newView = (ViewGroup) LayoutInflater.from(this).inflate(
                 R.layout.weapon_item, mWeaponGroup, false);
         
         setupSpinner((Spinner)newView.findViewById(R.id.pow_entry), R.array.pow_array, false, true);
         setupSpinner((Spinner)newView.findViewById(R.id.rof_entry), R.array.rof_array, false, false);
         
         weaponsVars.add(0, new ArrayList<AttackProperty>());
 
         // Set a click listener for the "X" button in the row that will remove the row.
         newView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            
         	public void onClick(View view) {
                 // Remove the row from its parent (the container view).
                 // Because mContainerView has android:animateLayoutChanges set to true,
                 // this removal is automatically animated.
         		weaponsVars.remove(findIndexOfWeapon(newView, mWeaponGroup));
             	mWeaponGroup.removeView(newView);
 
                 // If there are no rows remaining, show the empty view.
                 //if (mWeaponGroup.getChildCount() == 0) {
                     //findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                 //}
             }
         });
 
         // Because mContainerView has android:animateLayoutChanges set to true,
         // adding this view is automatically animated.
         mWeaponGroup.addView(newView, 0);
     }
 	
 	/**
      * Adds the weapons to the viewgroup for the weapons
     */
 	private void initWeapons(ArrayList<Weapon> weapons){
 		
 		
 		System.out.println("number of weapons groups: " + mWeaponGroup.getChildCount());
 		
 		for (int i = weapons.size()-1; i >= 0; i--){
 			
 			addWeapons(findViewById(R.id.Button20));
 			
 			ViewGroup curView = (ViewGroup) mWeaponGroup.getChildAt(i);
 			
 			
 			CheckBox checkRanged = (CheckBox) curView.findViewById(R.id.ranged_entry);
 			checkRanged.setChecked(weapons.get(i).getRanged());
 			Spinner rofText = (Spinner) curView.findViewById(R.id.rof_entry);
 			
 			//set rof to correct value
 			if (weapons.get(i).getROF() == -1)
 				rofText.setSelection(0);
 			else
 				rofText.setSelection(weapons.get(i).getROF());
 			
 			//set power to correct value
 			Spinner powText = (Spinner) curView.findViewById(R.id.pow_entry);
 			powText.setSelection(weapons.get(i).getPow()-6);
 				
 			
 		}
 		
 		
 	}
 	
 	private int findIndexOfWeapon(View weaponView, ViewGroup weaponGroup){
 		
 		for (int i = 0; i < weaponGroup.getChildCount(); i++){
 			
 			View view = weaponGroup.getChildAt(i);
 			
 			if (view.equals(weaponView))
 				return i;
 		
 			
 		}
 		
 		return -1;
 	}
 	
 	
 
 }
