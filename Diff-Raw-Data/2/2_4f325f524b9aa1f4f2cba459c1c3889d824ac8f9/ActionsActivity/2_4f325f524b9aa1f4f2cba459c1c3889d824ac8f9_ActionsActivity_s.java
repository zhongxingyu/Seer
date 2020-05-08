 package org.projectx.icetool;
 
 import android.os.Bundle;
 import android.provider.Settings;
 
 public class ActionsActivity extends ScriptedActivity {
 	public boolean onItemSelected(String itemAction, String itemDescription) {
 				
 		ICETool.getInstance().getTabHost().setCurrentTab(ICETool.TAB_CONSOLE);		
 		
 		// Custom commands here
 		if (itemAction.equals("blnon")) {
 		  Settings.System.putInt(getContentResolver(), "USE_BUTTONS_ON_NOTIFICATION", 1);
 		  ICETool.getInstance().getConsoleView().append("BLN enabled" + "\n");
		} else if (itemAction.equals("blnon")) {
 		  Settings.System.putInt(getContentResolver(), "USE_BUTTONS_ON_NOTIFICATION", 0);
 		  ICETool.getInstance().getConsoleView().append("BLN disabled" + "\n");		  
 		}
 
 		return true;
 	}
 	
 	public void onCreate(Bundle savedInstanceState) {		
 		this.actions      = getResources().getStringArray(R.array.actions_array);
 		this.descriptions = getResources().getStringArray(R.array.descriptions_array);
 		super.onCreate(savedInstanceState);		
 	}	
 }
