 // tim is the best
 
 package weatherOracle.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import weatherOracle.filter.*;
 import weatherOracle.notification.NotificationStore;
 
 public class ConditionRuleViewerActivity extends Activity {
 
 	LayoutParams params;
     LinearLayout mainLayout;
     
     
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.condition_rule_viewer_activity);
 		mainLayout = (LinearLayout)findViewById(R.id.condition_activity_linear_layout);
 
 		final Button saveButton = (Button) findViewById(R.id.save_filter_button_conditions);   
 		initializeSaveButtonListener(saveButton);
 
 		CreateAddConditionButton();
 		populateConditionRules();
 		displayConditionRules();
 
     }
     
     private void populateConditionRules(){
 
     	//conditions = FilterMenuActivity.filter.getConditionRules();
     	//conditions = new TreeSet<ConditionRule>();
     	//conditions.add(new ConditionRule("C1", 0, 10));
     }
     
     private void displayConditionRules() {
     	final List<ConditionRule> conditionList = new ArrayList<ConditionRule>(FilterMenuActivity.conditions);
     	for (int i = 0; i < conditionList.size(); i++) {    		
     		final RelativeLayout rl = new RelativeLayout(this);
     		//final Button deleteButton = new Button(this);
     		final TextView textview = new TextView(this);
     		int min = conditionList.get(i).getMinMax().first;
     		int max = conditionList.get(i).getMinMax().second;
     		String range;
     		if (min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) {
     			range = "Any Amount/Value";
     		} else if (min == Integer.MIN_VALUE) {
     			range = "Lower than " +  max + conditionList.get(i).getUnits(conditionList.get(i).getCondition());
     		} else if (max == Integer.MAX_VALUE) {
     			range = "Higher than " + min + conditionList.get(i).getUnits(conditionList.get(i).getCondition());
     		} else {
     			range = min + conditionList.get(i).getUnits(conditionList.get(i).getCondition()) + " - " + max + conditionList.get(i).getUnits(conditionList.get(i).getCondition());
     		}
     		textview.setText(conditionList.get(i).getCondition() + ":\n " + range);
     		textview.setTextSize(2,15);
          	 
     		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
  					LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
  			layoutParams.setMargins(8, 4, 8, 4); // top and bottom margins are 4 so that if two elements
  												 // appear in succession the total separation will be 8
     		
     	  	final Button deleteButton = new Button(this);
           	deleteButton.setText("Delete");
       	
             rl.addView(deleteButton);
     	  	LayoutParams params = (RelativeLayout.LayoutParams)deleteButton.getLayoutParams();
     	  	((android.widget.RelativeLayout.LayoutParams) params).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
     		
     	  	final int index = i;
    	  	 	deleteButton.setOnClickListener(new View.OnClickListener() {
    	  	 		public void onClick(View v) {
    	  	 			AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
    	  	 			builder.setMessage("Are you sure you want to delete this Condition?")
 						.setCancelable(false)
 						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 			   	  	 			FilterMenuActivity.conditions.remove(conditionList.get(index));
 			   	  	 			mainLayout.removeAllViews();
 			   	  	 			displayConditionRules();
 							}
 						})
 						.setNegativeButton("No", new DialogInterface.OnClickListener() {
 							
 							public void onClick(DialogInterface dialog, int which) {
 								// Do Nothing!
 								
 							}
 						});
    	  	 			
    					AlertDialog alert = builder.create();
    					alert.show();
    	  	 		}
    	  	 	});
     	  	
     		rl.addView(textview);
     		rl.setBackgroundResource(R.drawable.main_view_element);
     		mainLayout.addView(rl, layoutParams);
     	}
     }
     
     
 
 	private void CreateAddConditionButton() {
     	Button b = (Button)findViewById(R.id.add_condition_filter_button);
     	 b.setOnClickListener(new View.OnClickListener() {
              public void onClick(View view) {
              	Intent myIntent = new Intent(view.getContext(), ConditionAdderActivity.class);
                 startActivity(myIntent);     
              } 
          });
     	
     }
 
  	public void onResume() {
  		super.onResume();
  		mainLayout.removeAllViews();
     	populateConditionRules();
     	displayConditionRules();
  	}
 
 	public void onWindowFocusChanged(boolean hasFocus){
 		super.onWindowFocusChanged(hasFocus);
 		if(hasFocus) {
 	 		mainLayout.removeAllViews();
 	    	populateConditionRules();
 	    	displayConditionRules();
 		} 
 	}
 	
 	
 	
 	
 	private void initializeSaveButtonListener(Button saveButton){
 		saveButton.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View v)
 			{
 				String currentName = FilterMenuActivity.currentFilterName;
 				boolean filterNameValid = true;
 				boolean editingExistingFilter = false;
 				// checks if filter name specified is already assigned to an existing
 				// filter
 				for (int i = 0; i < HomeMenuActivity.filterList.size(); i++){
 					Filter current = HomeMenuActivity.filterList.get(i);
 					if (FilterMenuActivity.initialFilterName.equals(current.getName())){
 						editingExistingFilter = true;
 					}
 					if(current.getName().equals(FilterMenuActivity.currentFilterName)
 					&& !(editingExistingFilter)){
 						filterNameValid = false;
 					}
 				}
 				
 				// filter name is unique at this point, but not necessarily valid
 				// because it could still be the empty string
 				if(FilterMenuActivity.currentFilterName.trim().equals("")) {
 					filterNameValid = false;
 				}
 				
 				// filter name is valid
 				if(filterNameValid){
 					FilterMenuActivity.filter.removeTimeRules();
 					FilterMenuActivity.filter.addSetOfTimeRules(FilterMenuActivity.times);
 					FilterMenuActivity.filter.setName(FilterMenuActivity.currentFilterName);
 					FilterMenuActivity.filter.removeConditionRules();
         	 		FilterMenuActivity.filter.addSetOfConditionRules(FilterMenuActivity.conditions);
         	 		
 					if(editingExistingFilter){
 						for(int i = 0; i < HomeMenuActivity.filterList.size(); i++){
 							Filter current = HomeMenuActivity.filterList.get(i);
 							if(current.getName().equals(FilterMenuActivity.initialFilterName)){
 								HomeMenuActivity.filterList.remove(i);
 								i--;
 							}
 						}
 					}
 					HomeMenuActivity.filterList.add(FilterMenuActivity.filter);
 					finish();
 				} else {
 					FilterMenuActivity.tabHost.setCurrentTab(0);
 				}
 			}
 		});
 	}
 }
 
 
