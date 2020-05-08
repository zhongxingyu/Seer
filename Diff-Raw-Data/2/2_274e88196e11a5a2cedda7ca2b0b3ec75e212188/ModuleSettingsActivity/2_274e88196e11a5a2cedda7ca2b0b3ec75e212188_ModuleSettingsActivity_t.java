 package com.eps_hioa_2013.JointAttentionResearchApp;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.NumberPicker;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.Toast;
 
 //ModuleSettingsActivity gets called, when you create a new Module or like to edit an existing one
 public class ModuleSettingsActivity extends Activity {
 	public final static String MODULENUMBER = "com.eps_hioa_2013.JointAttentionResearchApp.MODULENUMBER";	
 	public final static String EXTRA_SESSION = "com.eps_hioa_2013.JointAttentionResearchApp.EXTRA_SESSION";
 	public final static String EXTRA_ROUNDSTOPLAY = "com.eps_hioa_2013.JointAttentionResearchApp.EXTRA_ROUNDSTOPLAY";
 	public final static String EXTRA_TIME = "com.eps_hioa_2013.JointAttentionResearchApp.EXTRA_TIME";
 
 	Bundle bundle;
 	Session mysession;	
 
 	NumberPicker npMinutes;
 	NumberPicker npSeconds;
 	NumberPicker npRoundsToPlay;
 
 	//Example to explain the oncoming 4 lines: When you have an donald.jpg in the Elementsfolder on your tablet:
 	//	There will be an Element on position 0 with the name donald in preactions
 	//	The appropriate Button is in buttonPreactions on position 0
 	//	The appropriate Checkbox is in checkboxPreactions also on position 0
 	//In that way, the Element, Button and the CheckBox can be connected to each other.
 	//The donald Element will of course also be in Signals and Actions because its a picture
 	private List<CheckBox> checkboxPreactions = new ArrayList<CheckBox>();
 	private List<Button> buttonPreactions = new ArrayList<Button>();
 	private List<Button> buttonPreactions2 = new ArrayList<Button>();
 	private List<Element> preactions = new ArrayList<Element>();
 	private int preactionsCounter = 0;
 
 	private List<CheckBox> checkboxSignals = new ArrayList<CheckBox>();
 	private List<Button> buttonSignals = new ArrayList<Button>();
 	private List<Element> signals = new ArrayList<Element>();
 	private int signalsCounter = 0;
 
 	private List<CheckBox> checkboxActions = new ArrayList<CheckBox>();
 	private List<Button> buttonActions = new ArrayList<Button>();
 	private List<Element> actions = new ArrayList<Element>();
 	private int actionsCounter = 0;
 
 	private List<CheckBox> checkboxRewards = new ArrayList<CheckBox>();
 	private List<Button> buttonRewards = new ArrayList<Button>();
 	private List<Element> rewards = new ArrayList<Element>();
 	private int rewardsCounter = 0;
 
 	private int currentModuleNumber = -1;
 	private Queue<String> elementChangeQueue;
 	private Queue<String> elementChangeQueue2;
 	private String currentElement;
 	private String[] locationArray = {"topleft", "topmid", "topright", "midleft", "midmid", "midright", "bottomleft", "bottommid", "bottomright"};
 	private String[] durationArray = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"};
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		System.out.println("ModuleSettingsActivity started");
 		super.onCreate(savedInstanceState);
 
 		//set full screen
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		//addPreferencesFromResource(R.xml.settings); 
 		setContentView(R.layout.activity_module_settings);
 
 
 		elementChangeQueue = new LinkedList<String>();
		elementChangeQueue2 = new LinkedList<String>();
 		Intent intent = getIntent();
 		mysession = (Session) intent.getSerializableExtra(ModuleActivity.EXTRA_SESSION);
 		String modulenumber = (intent.getStringExtra(ModuleActivity.MODULENUMBER));
 		this.currentModuleNumber = Integer.parseInt(modulenumber);
 
 		//shows the Elements at the right place and saves them in the Membervariables
 		setupDynamicElementList(mysession.getElementlist());
 
 		configureNumberPickers(); //just some settings for the NumberPickers; nothing special
 
 		loadModuleSettings(modulenumber); //loads and shows all the saved Settings of a module
 
 		//hides keyboard until user presses a field
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 	}
 
 
 	public void loadModuleSettings(String modulenumber) {
 
 		if(Integer.parseInt(modulenumber) != -1) 
 		{
 			EditText editText2 = (EditText) findViewById(R.id.editText2); //name gets loaded
 			editText2.setText(getNameOfModule(modulenumber));
 
 			EditText editText1 = (EditText) findViewById(R.id.editText1); //descrition gets loaded
 			editText1.setText(getDescriptionOfModule(modulenumber));
 			Toast.makeText(getApplicationContext(), "MODULE" + this.currentModuleNumber + " loaded", Toast.LENGTH_SHORT).show();
 
 			for(int i = 0; i < preactionsCounter; i++) //the states of all checkboxes get loaded
 			{	    		
 				Boolean b = getBooleanOfModule(modulenumber, preactions.get(i).getName() + "preaction");
 				checkboxPreactions.get(i).setChecked(b);
 			}
 
 			for(int i = 0; i < signalsCounter; i++) //the states of all checkboxes get loaded
 			{	    		
 				Boolean b = getBooleanOfModule(modulenumber, signals.get(i).getName() + "signal");
 				checkboxSignals.get(i).setChecked(b);
 			}
 
 			for(int i = 0; i < actionsCounter; i++) //the states of all checkboxes get loaded
 			{	    		
 				Boolean b = getBooleanOfModule(modulenumber, actions.get(i).getName() + "action");
 				checkboxActions.get(i).setChecked(b);
 			}
 
 			for(int i = 0; i < rewardsCounter; i++) //the states of all checkboxes get loaded
 			{	    		
 				Boolean b = getBooleanOfModule(modulenumber, rewards.get(i).getName() + "reward");
 				checkboxRewards.get(i).setChecked(b);
 			}
 
 
 		}
 		else {/*nothing to do here*/}
 	}
 
 	public void onclick_start_game(View view)
 	{	
 
 		onclick_save(view);		
 		mysession.updateStatistics("Clicked on Start Module");
 		Intent intent = new Intent(this, GameActivity.class);
 		intent.putExtra(MODULENUMBER, Integer.toString(currentModuleNumber));
 		intent.putExtra(EXTRA_ROUNDSTOPLAY, npRoundsToPlay.getValue());
 		intent.putExtra(EXTRA_TIME, calculateTimeToPlayInSeconds());
 		bundle = new Bundle();
 		bundle.putSerializable(EXTRA_SESSION, (Serializable) mysession);
 		intent.putExtras(bundle);
 		startActivity(intent);
 		finish();
 	}
 
 	public void onclick_save(View view)
 	{	
 		EditText editText2 = (EditText) findViewById(R.id.editText2);
 		String module_name = editText2.getText().toString();
 
 		EditText editText1 = (EditText) findViewById(R.id.editText1);
 		String module_description = editText1.getText().toString();
 
 		//if the modulecounter is smaller than -1 it gets set to -1
 		if(getModulecounterOutOfPreferences() < -1) resetModulecounterInPreferences();
 
 
 		String nameForModule = null;
 		if(this.currentModuleNumber == -1) //jumps in here if new modules gets added
 		{
 			//modulecounter gets iterated
 			incrementModulecounterInPreferences();     	
 			//the Name for the new Module gets created. It is like MODULE0, MODULE1, MODULE2, ...
 			nameForModule = "MODULE"+getModulecounterOutOfPreferences();
 			this.currentModuleNumber = getModulecounterOutOfPreferences();
 		}
 		else //jumps in here if existing module gets saved
 		{
 			nameForModule = "MODULE"+this.currentModuleNumber;
 		}
 
 
 		//saves the variables into the ShardPreferences for the current Module 
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameForModule, 0);
 		SharedPreferences.Editor editor = pref_modulesettings.edit();       
 		// editor is cleared for boxes that were checked once but are not anymore.
 		editor.clear(); //TODO editor clears empty boxes but also empty locations.
 		editor.putString("module_name", module_name);
 		editor.putString("module_description", module_description);
 
 		//ask all Preaction TextBoxes if checked or unchecked
 		for(int i = 0; i < preactionsCounter; i++)
 		{
 			Boolean checked = false;
 			if(checkboxPreactions.get(i).isChecked())
 			{
 				checked = true;	   
 				editor.putBoolean(preactions.get(i).getName() + "preaction", checked);
 				//saves true (checked) or false (unchecked) as a Boolean named after the Element in the Shared Pref
 			}
 
 
 		}
 		//ask all Signals TextBoxes if checked or unchecked
 		for(int i = 0; i < signalsCounter; i++)
 		{
 			Boolean checked = false;
 			if(checkboxSignals.get(i).isChecked())
 			{
 				checked = true;
 				editor.putBoolean(signals.get(i).getName() + "signal", checked);	    		
 				//saves true (checked) or false (unchecked) as a Boolean named after the Element in the Shared Pref
 			}
 		}
 		//ask all Action TextBoxes if checked or unchecked
 		for(int i = 0; i < actionsCounter; i++)
 		{
 			Boolean checked = false;
 			if(checkboxActions.get(i).isChecked())
 			{
 				checked = true;
 				editor.putBoolean(actions.get(i).getName() + "action", checked);
 				//saves true (checked) or false (unchecked) as a Boolean named after the Element in the Shared Pref
 			}
 		}
 		//ask all Rewards TextBoxes if checked or unchecked
 		for(int i = 0; i < preactionsCounter; i++)
 		{
 			Boolean checked = false;
 			if(checkboxRewards.get(i).isChecked())
 			{
 				checked = true;
 				editor.putBoolean(rewards.get(i).getName() + "reward", checked);
 				//saves true (checked) or false (unchecked) as a Boolean named after the Element in the Shared Pref
 			}	    	
 		}
 		
 		//saving location
 		while(!elementChangeQueue.isEmpty())
 		{
 			editor.putString(elementChangeQueue.poll() + "location", elementChangeQueue.poll());
 		}
 		
 		//saving duration
 		while(!elementChangeQueue2.isEmpty())
 		{
 			editor.putString(elementChangeQueue2.poll() + "duration", elementChangeQueue2.poll());
 		}
 		
 		//checking if Settings are ok to start the game
 		//todo: expand this!
 		if((module_name == null) || (module_name.equals("")))
 		{
 			Toast.makeText(getApplicationContext(), "Modulename empty", Toast.LENGTH_SHORT).show();
 		}
 		else
 		{
 			editor.commit();        
 			Toast.makeText(getApplicationContext(), "Saved as MODULE" + this.currentModuleNumber, Toast.LENGTH_SHORT).show();
 			finish(); //goes back to last Activity (ModuleActivity)
 		}
 
 	}
 
 	private void setupDynamicElementList(List<Element> elements)
 	{
 		for(int i = 0; i < elements.size(); i++)
 		{
 			if(elements.get(i) instanceof ElementPicture)
 			{
 				//creates CheckBox and Button and shows it in the right place and adds both in the Membervariables
 				addElementToList(elements.get(i).getName(), "Preactions", true);
 				preactions.add(preactionsCounter, elements.get(i)); //adds the Element in the Membervariables
 				preactionsCounter++; //counter how many elements are in this array
 				addElementToList(elements.get(i).getName(), "Actions", true);
 				actions.add(actionsCounter, elements.get(i));
 				actionsCounter++;
 				addElementToList(elements.get(i).getName(), "Signals", true);
 				signals.add(signalsCounter, elements.get(i));
 				signalsCounter++;
 				addElementToList(elements.get(i).getName(), "Rewards", true);
 				rewards.add(rewardsCounter, elements.get(i));
 				rewardsCounter++;
 			}
 			if((elements.get(i) instanceof ElementVideo) || (elements.get(i) instanceof ElementSound))
 			{
 				addElementToList(elements.get(i).getName(), "Rewards", false);
 				rewards.add(rewardsCounter, elements.get(i));
 				rewardsCounter++;
 			}
 			if(elements.get(i) instanceof ElementSound)
 			{
 				addElementToList(elements.get(i).getName(), "Signals", false);
 				signals.add(signalsCounter, elements.get(i));
 				signalsCounter++;
 			}
 		}
 	}
 
 	//adds a Checkbox for an Element to the Settingsscreen
 	//WARNING: elementType must be either "Preactions", "Signals", "Actions" or "Rewards"
 	public void addElementToList(String elementName, String elementType, boolean buttonNeeded)
 	{		
 		TableLayout table = null;
 		String currentLocation = "";
 		String currentDuration = "";
 
 		if(elementType == "Preactions"){
 			currentLocation = getElementLocation(Integer.toString(currentModuleNumber), elementName + "Preaction");
 			currentDuration = getElementDuration(Integer.toString(currentModuleNumber), elementName + "Preaction");
 			table = (TableLayout) findViewById(R.id.Preactions);
 			//creates new Tablerow and sets it into the new Tablelayout
 			TableRow newTablerow = new TableRow(this);
 			table.addView(newTablerow);
 
 			//creates new Checkbox with the name of the element and sets it into the Tablerow
 			checkboxPreactions.add(preactionsCounter, new CheckBox(this));
 			checkboxPreactions.get(preactionsCounter).setText(elementName);
 
 			newTablerow.addView(checkboxPreactions.get(preactionsCounter));
 
 			//creates new Button for choosing the LOCATION of the Element and sets it also into the Tablerow 
 			buttonPreactions.add(preactionsCounter, new Button(this));
 			buttonPreactions.get(preactionsCounter).setTag( elementName + "Preaction");				
 			//creates new Button for choosing the DURATION of the Element and sets it also into the Tablerow 
 			buttonPreactions2.add(preactionsCounter, new Button(this));
 			buttonPreactions2.get(preactionsCounter).setTag( elementName + "Preaction2");
 			if(buttonNeeded == true)
 			{
 				buttonPreactions.get(preactionsCounter).setText(currentLocation);
 				buttonPreactions.get(preactionsCounter).setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					currentElement = (String) v.getTag();
 					showElementPositionDiaglog();
 					}
 				});		
 					
 				buttonPreactions2.get(preactionsCounter).setText(currentDuration);
 				buttonPreactions2.get(preactionsCounter).setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					currentElement = (String) v.getTag();
 					showElementDurationDiaglog();
 					}
 				});	
 			}
 			else
 			{
 				buttonPreactions.get(preactionsCounter).setVisibility(View.GONE);
 				buttonPreactions2.get(preactionsCounter).setVisibility(View.GONE);
 			}
 			newTablerow.addView(buttonPreactions.get(preactionsCounter));
 			newTablerow.addView(buttonPreactions2.get(preactionsCounter));			
 		}
 
 		if(elementType == "Signals"){
 			currentLocation =  getElementLocation(Integer.toString(currentModuleNumber), elementName + "Signal");
 			table = (TableLayout) findViewById(R.id.Signals);
 			//creates new Tablerow and sets it into the new Tablelayout
 			TableRow newTablerow = new TableRow(this);
 			table.addView(newTablerow);
 
 			//creates new Checkbox with the name of the element and sets it into the Tablerow
 			checkboxSignals.add(signalsCounter, new CheckBox(this));
 			checkboxSignals.get(signalsCounter).setText(elementName);
 			//	checkboxPreactions.add(preactionsCounter, (checkboxPreactions.get(preactionsCounter)));
 
 			newTablerow.addView(checkboxSignals.get(signalsCounter));
 
 				//creates new Button and sets it also into the Tablerow
 			buttonSignals.add(signalsCounter, new Button(this));	
 			buttonSignals.get(signalsCounter).setTag( elementName + "Signal");
 			if(buttonNeeded == true)
 			{
 				buttonSignals.get(signalsCounter).setText(currentLocation);
 				buttonSignals.get(signalsCounter).setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						currentElement = (String) v.getTag();
 						showElementPositionDiaglog();
 					}
 				});		
 			}
 			else
 			{
 				buttonSignals.get(signalsCounter).setVisibility(View.GONE);
 			}
 			newTablerow.addView(buttonSignals.get(signalsCounter)); 
 		
 		}
 
 		if(elementType == "Actions"){
 			currentLocation =  getElementLocation(Integer.toString(currentModuleNumber), elementName + "Action");
 			table = (TableLayout) findViewById(R.id.Actions);
 			//creates new Tablerow and sets it into the new Tablelayout
 			TableRow newTablerow = new TableRow(this);
 			table.addView(newTablerow);
 
 			//creates new Checkbox with the name of the element and sets it into the Tablerow
 			checkboxActions.add(actionsCounter, new CheckBox(this));
 			checkboxActions.get(actionsCounter).setText(elementName);
 			//	checkboxPreactions.add(preactionsCounter, (checkboxPreactions.get(preactionsCounter)));
 
 			newTablerow.addView(checkboxActions.get(actionsCounter));
 
 				//creates new Button and sets it also into the Tablerow
 			buttonActions.add(actionsCounter, new Button(this));
 			buttonActions.get(actionsCounter).setTag(elementName + "Action");
 			if(buttonNeeded == true)
 			{
 				buttonActions.get(actionsCounter).setText(currentLocation);
 				buttonActions.get(actionsCounter).setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						currentElement = (String) v.getTag();
 						showElementPositionDiaglog();
 					}
 				});			
 			}
 			else
 			{
 				buttonActions.get(actionsCounter).setVisibility(View.GONE);
 			}
 			newTablerow.addView(buttonActions.get(actionsCounter)); 		
 			
 		}
 
 		if(elementType == "Rewards"){
 			currentLocation =  getElementLocation(Integer.toString(currentModuleNumber), elementName + "Reward");
 			table = (TableLayout) findViewById(R.id.Rewards);
 			//creates new Tablerow and sets it into the new Tablelayout
 			TableRow newTablerow = new TableRow(this);
 			table.addView(newTablerow);
 
 			//creates new Checkbox with the name of the element and sets it into the Tablerow
 			checkboxRewards.add(rewardsCounter, new CheckBox(this));
 			checkboxRewards.get(rewardsCounter).setText(elementName);
 			//	checkboxPreactions.add(preactionsCounter, (checkboxPreactions.get(preactionsCounter)));
 
 			newTablerow.addView(checkboxRewards.get(rewardsCounter));
 			
 				//creates new Button and sets it also into the Tablerow
 			buttonRewards.add(rewardsCounter, new Button(this));
 			buttonRewards.get(rewardsCounter).setTag(elementName + "Reward");
 			if(buttonNeeded == true)
 			{
 				buttonRewards.get(rewardsCounter).setText(currentLocation);
 				buttonRewards.get(rewardsCounter).setOnClickListener(new View.OnClickListener() {
 					public void onClick(View v) {
 						currentElement = (String) v.getTag();
 						showElementPositionDiaglog();
 					}
 				});
 			}
 			else
 			{
 				buttonRewards.get(rewardsCounter).setVisibility(View.GONE);
 			}
 			newTablerow.addView(buttonRewards.get(rewardsCounter)); 	
 			
 		}
 
 		if(table == null){ //error
 			Toast.makeText(getApplicationContext(),		
 					"Error in: public void addElementToList(String elementName, String elementType); WRONG elementType",
 					Toast.LENGTH_LONG).show();
 		}       
 	}
 
 	private void configureNumberPickers() {
 		npMinutes = (NumberPicker) findViewById(R.id.npMinutes);
 		npMinutes.setMaxValue(999);
 		npMinutes.setMinValue(0);
 
 		npSeconds = (NumberPicker) findViewById(R.id.npSeconds);
 		npSeconds.setMaxValue(59);
 		npSeconds.setMinValue(0);
 
 		npRoundsToPlay = (NumberPicker) findViewById(R.id.npRoundsToPlay);
 		npRoundsToPlay.setMaxValue(999);
 		npRoundsToPlay.setMinValue(1);		
 	}
 
 	private int calculateTimeToPlayInSeconds() {
 		int minutes = npMinutes.getValue();
 		int seconds = npSeconds.getValue();	
 		return ((minutes*60)+seconds);
 	}
 
 	//shows the popup when you click on the Edit-button
 	public void showElementPositionDiaglog() {
 		DialogFragment newFragment = new ElementPositionDialog();
 		newFragment.show(getFragmentManager(), "dialogsettings");
 	}
 	
 	public void showElementDurationDiaglog() {
 		DialogFragment newFragment = new ElementDurationDialog();
 		newFragment.show(getFragmentManager(), "dialogsettings");
 	}
 
 	//returns the counter for the modules
 	//this Method is also present in ModuleActivity.java; This should be solved in a better way
 	public int getModulecounterOutOfPreferences() {
 		SharedPreferences pref_modulecounter = getSharedPreferences("counter", 0); 
 		int modulecounter = pref_modulecounter.getInt("modulecounter", 0);
 		return modulecounter;
 	}
 
 	//increments the modulecounter
 	public void incrementModulecounterInPreferences() {		
 		SharedPreferences pref_modulecounter = getSharedPreferences("counter", 0);
 		SharedPreferences.Editor editor = pref_modulecounter.edit();
 		int count = getModulecounterOutOfPreferences() + 1;
 		editor.putInt("modulecounter", count);
 		editor.commit();
 	}
 
 	//decrements the modulecounter
 	public void decrementModulecounterInPreferences() {		
 		SharedPreferences pref_modulecounter = getSharedPreferences("counter", 0);
 		SharedPreferences.Editor editor = pref_modulecounter.edit();
 		int count = getModulecounterOutOfPreferences() - 1;
 		editor.putInt("modulecounter", count);
 		editor.commit();
 	}
 
 	//sets the modulecounter to -1. When a new module gets added, it has the number 0
 	//this Method is also present in ModuleActivity.java; This should be solved in a better way
 	public void resetModulecounterInPreferences() {
 		SharedPreferences pref_modulecounter = getSharedPreferences("counter", 0);
 		SharedPreferences.Editor editor = pref_modulecounter.edit();
 		int count = -1;
 		editor.putInt("modulecounter", count);
 		editor.commit();
 	}
 
 	public String getNameOfModule(String i)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String nameOfModule = pref_modulesettings.getString("module_name", ACCESSIBILITY_SERVICE);
 		return nameOfModule;				
 	}
 
 	public String getDescriptionOfModule(String i)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String nameOfDescrition = pref_modulesettings.getString("module_description", ACCESSIBILITY_SERVICE);
 		return nameOfDescrition;		
 	}
 
 	public Boolean getBooleanOfModule(String i, String elementName)
 	{	
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		Boolean nameOfDescription = pref_modulesettings.getBoolean(elementName, false);        
 		return nameOfDescription;
 	}
 
 	public String getElementLocation(String i, String elementName)
 	{
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String location = pref_modulesettings.getString(elementName + "location", "location");
 		return location;
 	}
 	
 	public String getElementDuration(String i, String elementName)
 	{
 		String nameOfModulePref = "MODULE" + i;
 		SharedPreferences pref_modulesettings = getSharedPreferences(nameOfModulePref, 0);  
 		String location = pref_modulesettings.getString(elementName + "duration", "duration");
 		return location;
 	}
 
 	
 	@SuppressLint("ValidFragment")
 	//Position of an Element Popup
 	public class ElementPositionDialog extends DialogFragment {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle("Element Location settings")
 			.setItems(locationArray, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					// The 'which' argument contains the index position
 					// of the selected item
 					elementChangeQueue.add(currentElement);
 					elementChangeQueue.add(locationArray[which]);	
 				}
 			});
 			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// User cancelled the dialog
 				}
 			});
 			return builder.create();
 		}
 	}
 	@SuppressLint("ValidFragment")
 	//Duration of an Element Popup
 	public class ElementDurationDialog extends DialogFragment {
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setTitle("Element Duration settings")
 			.setItems(durationArray, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					// The 'which' argument contains the index position
 					// of the selected item
 					elementChangeQueue2.add(currentElement);
 					elementChangeQueue2.add(durationArray[which]);	
 				}
 			});
 			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// User cancelled the dialog
 				}
 			});
 			return builder.create();
 		}
 	}
 }
