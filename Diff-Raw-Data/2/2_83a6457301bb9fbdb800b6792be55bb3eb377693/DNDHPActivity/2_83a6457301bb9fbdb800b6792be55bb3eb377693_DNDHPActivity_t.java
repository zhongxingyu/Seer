 //git repository: "git@github.com:codonnell/dnd-hp-tracker.git"
 package com.admteal.dndhp;
 
 import com.admteal.dndhp.R;
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.*;	
 import android.os.Bundle;
 		
 public class DNDHPActivity extends Activity {
     public int currentHP, currentHS, currentOngo, currentEntry, showWork, currentSurges = 0;
 	public int currentDeathSaves = 3;

 	//Create the calculator function buttons
 	public Button inputAdd, inputSub, inputClear, inputHS;
 
 	//Create the ongoing function buttons
 	public Button ongoAdd, ongoSub, inputOngo, inputDS;
 
 	//Create the surges function buttons
 	public Button surgesAdd, surgesSub, inputSurges;
 
 	//Create the Death Saves function buttons
 	public Button DSAdd, DSSub;
 	
 	//public Button inputDS	= (Button) findViewById(R.id.inputDS); //This button does nothing right now
 	
 	public TextView currentEntryView, currentHPView;
 	public LinearLayout showWorkLayout;
 	public ScrollView showWorkScroller;
 	
     /** Called when the activity is first created. */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);        
         setContentView(R.layout.main);
     	
         //Set the calculator number buttons and their onClickListeners
         for (int i = 0; i < 10; i++) {
         	String buttonID = "input" + Integer.toString(i);
         	int resourceID = getResources().getIdentifier(buttonID, "id", "com.admteal.dndhp");
             Button b = (Button) findViewById(resourceID);
             final int j = i; //allows passing i into a new View.OnClickListener's onClick
             b.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					currentEntryViewUpdater(j);					
 				}
 			});
         }
 
     	//Create the calculator function buttons
     	inputAdd	= (Button) findViewById(R.id.inputAdd);
     	inputAdd.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showWorkUpdater(currentEntry);
 			}
 		});
     	inputSub	= (Button) findViewById(R.id.inputSub);
     	inputSub.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showWorkUpdater(currentEntry * -1);
 			}
 		});
     	inputClear	= (Button) findViewById(R.id.inputClear);
     	inputClear.setOnLongClickListener(new View.OnLongClickListener() {			
 			public boolean onLongClick(View v) {	
 				clearEntry();
 				showWorkLayout.removeAllViews();				
 				return true; //stops click event from also being processed
 			}
 		});
     	inputClear.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {	
 				clearEntry();
 			}
 		});
     	inputHS		= (Button) findViewById(R.id.inputHS);
     	inputHS.setOnClickListener(new View.OnClickListener() {			
 			public void onClick(View v) {
 				showWorkUpdater(currentHS);
 			}
 		});
     	// Long click to set currentEntry into Healing surge value, and display it on the button
     	inputHS.setOnLongClickListener(new View.OnLongClickListener() {
 			public boolean onLongClick(View v) {
 				currentHS = currentEntry;
 				inputHS.setText("HS: " + Integer.toString(currentEntry));
 				clearEntry();
 				return true;
 			}
 		});
 
     	//Create the ongoing function buttons
     	ongoAdd	= (Button) findViewById(R.id.ongoAdd);
     	ongoAdd.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				ongoUpdater("+");
 			}
 		});
     	ongoSub	= (Button) findViewById(R.id.ongoSub);
     	ongoSub.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				ongoUpdater("-");
 			}
 		});
     	inputOngo	= (Button) findViewById(R.id.inputOngo);
     	inputOngo.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showWorkUpdater(currentOngo * -1); //current ongo stores the number backwards, so it is inverted first
 			}
 		});
 
     	//Create the surges function buttons
     	surgesAdd	= (Button) findViewById(R.id.surgesAdd);
     	surgesAdd.setOnClickListener (new View.OnClickListener() {			
 			public void onClick(View v) {
 				surgesUpdater("+");
 			}
 		});
     	surgesSub	= (Button) findViewById(R.id.surgesSub);
     	surgesSub.setOnClickListener (new View.OnClickListener() {			
 			public void onClick(View v) {
 				surgesUpdater("-");
 			}
 		});
     	inputSurges = (Button) findViewById(R.id.inputSurges);
     	inputSurges.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (currentSurges == 0) {
 					return;
 				}
 				showWorkUpdater(currentHS);
 				surgesUpdater("-");
 			}
 		});
 
     	//Create the Death Saves function buttons
     	DSAdd		= (Button) findViewById(R.id.DSAdd);
     	DSAdd.setOnClickListener (new View.OnClickListener() {			
 			public void onClick(View v) {
 				DSUpdater("+");
 			}
 		});
     	DSSub		= (Button) findViewById(R.id.DSSub);
     	DSSub.setOnClickListener (new View.OnClickListener() {			
 			public void onClick(View v) {
 				DSUpdater("-");
 			}
 		});
     	
     	inputDS		= (Button) findViewById(R.id.inputDS);
     	
     	currentEntryView= (TextView) findViewById(R.id.currentEntryView);
     	showWorkScroller= (ScrollView) findViewById(R.id.showWorkScroller);
     	showWorkLayout	= (LinearLayout) findViewById(R.id.showWorkLayout);
     	currentHPView	= (TextView) findViewById(R.id.currentHPView); 
     }
     
     @Override
     protected void onResume() {
         super.onResume();
     	currentEntryView.setText(Integer.toString(currentEntry));
     	currentHPView.setText(Integer.toString(currentHP));
     	inputSurges.setText("Surges: " + Integer.toString(currentSurges));
     	ongoUpdater("");
     	DSUpdater("");
     	surgesUpdater("");
 		inputHS.setText("HS: " + Integer.toString(currentHS));
     }
     
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
     	savedInstanceState.putInt("currentEntry", currentEntry);
     	savedInstanceState.putInt("currentHP", currentHP);
     	savedInstanceState.putInt("currentSurges", currentSurges);
     	savedInstanceState.putInt("currentOngo", currentOngo);
     	savedInstanceState.putInt("currentDeathSaves", currentDeathSaves);
     	savedInstanceState.putInt("currentSurges", currentSurges);
     	savedInstanceState.putInt("currentHS", currentHS);
     }
     
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
     	super.onRestoreInstanceState(savedInstanceState);
     	currentEntry = savedInstanceState.getInt("currentEntry");
     	currentHP = savedInstanceState.getInt("currentHP");
     	currentSurges = savedInstanceState.getInt("currentSurges");
     	currentOngo = savedInstanceState.getInt("currentOngo");
     	currentDeathSaves = savedInstanceState.getInt("currentDeathSaves");
     	currentSurges = savedInstanceState.getInt("currentSurges");
     	currentHS = savedInstanceState.getInt("currentHS");
     }
     
     public void currentEntryViewUpdater(int updateWith) {
     	if (currentEntry * 10 > 999) {return;} //Max 3 digit number
     	currentEntry = currentEntry * 10 + updateWith;
     	currentEntryView.setText(Integer.toString(currentEntry));
     }
     
     public void showWorkUpdater(int value) {
     	String operation = "";
     	TextView adjustment = new TextView(this);   
     	TextView sum = new TextView(this); 	
     	//First we must pick our operation.
     	if (value > 0) {
     		operation = "+";
         	adjustment.setTextColor(Color.GREEN);
     	} else if (value < 0) {
         	adjustment.setTextColor(Color.RED);
     	}
     	currentHP += value;
     	//First line shows how much was added or subtracted as +n or -n
     	adjustment.setText(operation + Integer.toString(value));
     	adjustment.setGravity(Gravity.RIGHT);
     	adjustment.setTextSize(14 * this.getResources().getDisplayMetrics().density + 0.5f); //16 px converted to 16 dip
     	//Second line shows new total number
     	sum.setText(Integer.toString(currentHP));
     	sum.setGravity(Gravity.LEFT);
     	sum.setTextSize(14 * this.getResources().getDisplayMetrics().density + 0.5f);  //16 px converted to 16 dip
     	//Now commit those lines to the view
     	showWorkLayout.addView(adjustment);
     	showWorkLayout.addView(sum);
     	clearEntry();
     	
     	currentHPView.setText(Integer.toString(currentHP));
     }
     
     public void DSUpdater(String how) {
     	if (how == "+") {
     		currentDeathSaves++;
     	} else if (how == "-") {
     		currentDeathSaves--;
     	}
     	inputDS.setText("Death Saves: " + Integer.toString(currentDeathSaves));
     }
     
     public void surgesUpdater(String how) {
     	if (how == "+") {
     		currentSurges++;
     	} else if (how == "-") {
     		currentSurges--;
     	}
     	inputSurges.setText("Surges: " + Integer.toString(currentSurges));
     }
     
     public void ongoUpdater(String how) {
     	String dotOrHot;
     	String valueToUse;
     	//Pick operation and adjust currentOngo number
     	if (how == "+") {
     		currentOngo++;
     	} else if (how == "-") {
     		currentOngo--;
     	}
     	//It's a regen if it is under 0, otherwise it is ongoing
     	if (currentOngo < 0) {
     		dotOrHot = "Regen: ";
     		valueToUse = Integer.toString(currentOngo * -1);
     	} else {
     		dotOrHot = "Ongoing: ";
     		valueToUse = Integer.toString(currentOngo);
     	}
     	//Change the button text to reflect variable
     	inputOngo.setText(dotOrHot + valueToUse);
     }
     
     public void clearEntry() {
 		currentEntry = 0;
 		currentEntryView.setText(Integer.toString(currentEntry));
     }
 }
