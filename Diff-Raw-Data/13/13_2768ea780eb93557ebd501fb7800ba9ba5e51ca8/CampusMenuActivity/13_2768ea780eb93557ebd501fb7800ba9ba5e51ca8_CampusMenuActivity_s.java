 /*
  * Copyright [2012] [Simon Fransson, Dina Zuko]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. 
  */
 package com.chalmers.schmaps;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.View;
 import android.widget.Button;
 
 /**
 *CampusMenu was created to act as an intermediary between the MenuActivity and GoogleMapShowLocation
 *if you've pressed a button to show locations in the areas Johanneberg or Lindholmen. 
 *We have identifiers "JOHANNEBERG" and "LINDHOLMEN" so that when we've pressed the buttons, we can
 *send the identifier to the next Activity for further purposes.
 */
 
 public class CampusMenuActivity extends Activity implements View.OnClickListener {
 	
 	private Intent startMapActivity;
 	private Button johannebergButton;
 	private Button lindholmenButton;
 	private Bundle menuActionChosen;
 	private String actionString;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_campus_menu);
         assignInstances();
     }
     
 	private void assignInstances() {
 		johannebergButton = (Button) findViewById(R.id.johannebergButton);
 		johannebergButton.setOnClickListener(this);
 		lindholmenButton = (Button) findViewById(R.id.lindholmenButton);
 		lindholmenButton.setOnClickListener(this);
 		menuActionChosen = getIntent().getExtras();
 	}
 	
 	/**
 	*onClick(View arg0) uses a switchcase for the buttons to know which identifiers should be set to
 	*the next activity. We send one two identifiers to the next Activity, one to zoom in the correct
 	*area and one, which we get from the previous Activity, to know which table should be used in
 	*the database and mark the correct locations in the area.
 	*/
 	public void onClick(View arg0) {
 		
		String campus = "";
 		
 		switch (arg0.getId())
 		{
 		case R.id.johannebergButton:
			campus = "JOHANNEBERG";
 			break;
 			
 			
 		case R.id.lindholmenButton:
			campus = "LINDHOLMEN";
 			break;
 		}
 		
 		startMapActivity = new Intent("android.intent.action.GOOGLEMAPSHOWLOCATION");
 		setActionString(startMapActivity.getAction());
 		startMapActivity.putExtra("Campus", campus);
 		//Transfer what kind of locations should be drawn
 		startMapActivity.putExtra("Show locations", menuActionChosen.getInt("Show locations"));
 		startActivity(startMapActivity);
 	}
 	
     public String getActionString() {
 		return actionString;
 	}
 
 	public void setActionString(String actionString) {
 		this.actionString = actionString;
 	}
 
 
 }
