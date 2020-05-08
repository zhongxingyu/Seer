 package me.taedium.android;
 
 import me.taedium.android.ApplicationGlobals.RecParamType;
 import me.taedium.android.domain.FilterItem;
 import me.taedium.android.domain.FilterItemAdapter;
 import me.taedium.android.view.ViewRecommendation;
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Typeface;
 import android.location.Location;
 import android.os.Bundle;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /*
  * Main Activity
  * This activity is the heart and soul of the application.
  * 
  * Use cases:
  * -First time opening app
  * -User has never logged in/registered
  * -Switches view once user has logged in/registered to allow for adding activity
  * -Enter context info
  * -Get a recommendation and swipe through them
  */
 public class FirstStart extends HeaderActivity {
 	// State Strings
 	public static final String STATE_NUM_PEOPLE = "_state_num_people";
 	public static final String STATE_DURATION = "_state_duration";
 	public static final String STATE_LOCATION_TYPE = "_state_location_type";
     public static final String STATE_USER_NAME = "_user_name";
     
     // Fonts
     public Typeface helvetica;
     
     private static final int ACTIVITY_VIEW = 0;
     private static final int DIALOG_OFFSET = 100;
     private static final int DIALOG_FILTER_PEOPLE = 100;
     private static final int DIALOG_FILTER_ENVIRONMENT = 101;
     private static final int DIALOG_FILTER_LOCATION = 102;
     private static final int DIALOG_FILTER_TIME = 103;
     private static final int DIALOG_FILTER_COST = 104;
     
     // Widgets
     private Button bTaedium;
     private ListView lvOptions;
 
     @Override
     public void onCreate(Bundle savedState) {
         super.onCreate(savedState);
         setTitle(R.string.main_title);
        
         // Deal with portrait vs. landscape orientation
         Configuration config = getResources().getConfiguration();
     	if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
     		setContentView(R.layout.landing_page_portrait);
     	} else {
     		setContentView(R.layout.landing_page_landscape);
     	} 
     	
         //Here so ahal can debug 
         //Intent i = new Intent(this, AddName.class);
         //startActivity(i);
 
         initializeHeader();
         
         // Initialize fonts
         helvetica = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeueCE55Roman.ttf");
         
         // Initialize bTaedium
         bTaedium = (Button) findViewById(R.id.bTaedium);
         bTaedium.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {            	
                 // If using location, get latest GPS coordinates
                 if (ApplicationGlobals.getInstance().isLocationEnabled()) {
                     // Get location
                     Location location = ApplicationGlobals.getInstance().getCurrentLocation();
                     if (location != null) {
                         addRecommendationParam(RecParamType.LAT, Double.toString(location.getLatitude()));
                         addRecommendationParam(RecParamType.LONG, Double.toString(location.getLongitude()));
                         Toast.makeText(FirstStart.this, "Location: Lat=" + location.getLatitude() 
                         		+ " Long=" + location.getLongitude(), Toast.LENGTH_LONG).show();
                     }
                 }
                 Intent i = new Intent(FirstStart.this, ViewRecommendation.class);
                 startActivityForResult(i, ACTIVITY_VIEW);
             }
         });
         
         if(findViewById(R.id.filterText) != null) {
 	        TextView filterTitle = (TextView) findViewById(R.id.filterText);
 	        filterTitle.setTypeface(helvetica);
         }
          
         // Initialize Options ListView      
         lvOptions = (ListView)findViewById(R.id.lvOptions);
         lvOptions.setAdapter(new FilterItemAdapter(this, R.id.list_item_text, getFilterItems()));
         lvOptions.setTextFilterEnabled(true);        
         lvOptions.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             	switch((int)id) {
                     case 0:                    	
                         showDialog(DIALOG_FILTER_PEOPLE);
                         break;
                     case 1:
                         showDialog(DIALOG_FILTER_ENVIRONMENT);
                         break;
                     case 2:
                         showDialog(DIALOG_FILTER_LOCATION);
                         break;
                     case 3:
                         showDialog(DIALOG_FILTER_TIME);
                         break;
                     case 4:
                         showDialog(DIALOG_FILTER_COST);
                         break;
                     default:
                 }
             }
         });
     }
     
     @Override
     protected Dialog onCreateDialog(int id) {
         Button bOk;
         final Dialog dialog;
         dialog = new Dialog(this, R.style.Dialog);
         WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
         lp.dimAmount = 0.0f;
         dialog.getWindow().setAttributes(lp);
         dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
         switch(id) {
             case DIALOG_FILTER_PEOPLE:
                 dialog.setContentView(R.layout.filter_people);
                 dialog.setTitle(getString(R.string.people_dialog_title));
                 bOk = (Button)dialog.findViewById(R.id.bFilterPeopleOk);
                 bOk.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         EditText numPeople = (EditText)dialog.findViewById(R.id.etNumPeople);                        
                         addRecommendationParam(RecParamType.PEOPLE, numPeople.getText().toString());
                         CheckBox childSafe = (CheckBox)dialog.findViewById(R.id.cbChildSafe);
                         addRecommendationParam(RecParamType.KIDFRIENDLY, childSafe.isChecked());
                         
                         // create string for feedback on filter
                         String input = numPeople.getText().toString();
                         String label = "";
                         if (!input.equalsIgnoreCase("") && !input.equalsIgnoreCase("0")) {
                         	label = input;
                         }
                         if (childSafe.isChecked()) {
                         	if (!label.equalsIgnoreCase("")) {
                         		label = label + ", " + getString(R.string.stKidFriendly);
                         	}
                         	else {
                         		String defaultLabel = getResources().
                     				getStringArray(R.array.lvFeedbackDefaultsArray)[DIALOG_FILTER_PEOPLE- DIALOG_OFFSET];
                         		label = defaultLabel + ", " + getString(R.string.stKidFriendly);
                         	}
                         }
                         
                         // Use default feedback label
                         if (label.equalsIgnoreCase("")) {
                         	String defaultLabel = getResources().
                     			getStringArray(R.array.lvFeedbackDefaultsArray)[DIALOG_FILTER_PEOPLE- DIALOG_OFFSET];
                         	setFilterFeedbackLabel(DIALOG_FILTER_PEOPLE - DIALOG_OFFSET, defaultLabel);
                         }
                         else {
                         	setFilterFeedbackLabel(DIALOG_FILTER_PEOPLE - DIALOG_OFFSET, label);
                         }                        
                         
                         dismissDialog(DIALOG_FILTER_PEOPLE);
                     }
                 });
                 break;
             case DIALOG_FILTER_ENVIRONMENT:
                 dialog.setContentView(R.layout.filter_environment);
                 dialog.setTitle(getString(R.string.environment_dialog_title));
                 bOk = (Button)dialog.findViewById(R.id.bFilterEnvironmentOk);
                 bOk.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         CheckBox indoor = (CheckBox)dialog.findViewById(R.id.cbIndoor);
                         addRecommendationParam(RecParamType.INDOOR, indoor.isChecked());
                         CheckBox outdoor = (CheckBox)dialog.findViewById(R.id.cbOutdoor);
                         addRecommendationParam(RecParamType.OUTDOOR, outdoor.isChecked());
                         CheckBox aroundTown = (CheckBox)dialog.findViewById(R.id.cbTown);
                         addRecommendationParam(RecParamType.AROUNDTOWN, aroundTown.isChecked());
                         
                         // create string for feedback on filter
                         int numChecked = 0;
                         String label = "";
                         if (indoor.isChecked()) {
                         	label = getString(R.string.stIndoor);
                         	numChecked ++;
                         }
                         if (outdoor.isChecked()) {
                         	if (numChecked > 0)	label = label + ", " + getString(R.string.stOutdoor);
                         	else label = getString(R.string.stOutdoor);
                         	numChecked ++;
                         }
                         if (aroundTown.isChecked()) {
                         	if (numChecked > 0)	label = label + ", " + getString(R.string.stAroundTown);
                         	else label = getString(R.string.stAroundTown);
                         	numChecked ++;
                         }
                         
                         // if none were checked, use default string
                         if (numChecked > 2 || numChecked < 1) {
                         	String defaultLabel = getResources().
                         		getStringArray(R.array.lvFeedbackDefaultsArray)[DIALOG_FILTER_ENVIRONMENT - DIALOG_OFFSET];
                         	setFilterFeedbackLabel(DIALOG_FILTER_ENVIRONMENT - DIALOG_OFFSET, defaultLabel);                        	
                         }
                         else {
                         	setFilterFeedbackLabel(DIALOG_FILTER_ENVIRONMENT - DIALOG_OFFSET, label);
                         }
                         
                         dismissDialog(DIALOG_FILTER_ENVIRONMENT);
                     }
                 });
                 break;
             case DIALOG_FILTER_LOCATION:
                 dialog.setContentView(R.layout.filter_location);
                 dialog.setTitle(getString(R.string.location_dialog_title));
                 
                 EditText maxDist = (EditText)dialog.findViewById(R.id.etRadius);
                 CheckBox useLoc = (CheckBox)dialog.findViewById(R.id.cbEnableLocation);
                 ApplicationGlobals g = ApplicationGlobals.getInstance();
                 useLoc.setChecked(g.isLocationEnabled());
                 maxDist.setEnabled(g.isLocationEnabled());
                 
                 // Only enable maxDist EditText if the checkbox is checked
                 useLoc.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                     
                     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                         EditText maxDist = (EditText)dialog.findViewById(R.id.etRadius);
                         ApplicationGlobals.getInstance().setLocationEnabled(isChecked);
                         maxDist.setEnabled(isChecked);
                         if (isChecked) {
                         	ApplicationGlobals.getInstance().startLocationListener(FirstStart.this);
                         }
                         else {
                         	ApplicationGlobals.getInstance().stopLocationListener();
                         }
                     }
                 });
                 
                 // Check if GPS is enabled on device, if not then disable controls
                 boolean gpsEnabled = ApplicationGlobals.getInstance().locationIsEnabled(this);
                 useLoc.setEnabled(gpsEnabled);
                 if (!gpsEnabled) {
                     useLoc.setChecked(false);
                     ApplicationGlobals.getInstance().setLocationEnabled(false);
                 }
                 
                 bOk = (Button)dialog.findViewById(R.id.bFilterLocationOk);
                 bOk.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                     	String label = "";
                         if (ApplicationGlobals.getInstance().isLocationEnabled()) {
                             // Get max radius    
                             EditText maxDist = (EditText)dialog.findViewById(R.id.etRadius);                            
                             label = getString(R.string.stLocationEnabled);
                             String radius = maxDist.getText().toString();
                             if (radius.equalsIgnoreCase("")) {
                             	label = label + ", " + getString(R.string.default_radius) + getString(R.string.stDistanceUnit);
                             	radius = getString(R.string.default_radius);
                             }
                             else {
                             	label = label + ", " + radius + getString(R.string.stDistanceUnit);
                             }
                             addRecommendationParam(RecParamType.MAXDIST, radius);
                         }
                         else {
                             removeRecommendationParam(RecParamType.MAXDIST);
                             removeRecommendationParam(RecParamType.LAT);
                             removeRecommendationParam(RecParamType.LONG);
                             label = getResources().getStringArray(R.array.lvFeedbackDefaultsArray)[DIALOG_FILTER_LOCATION - DIALOG_OFFSET];
                         }
                         setFilterFeedbackLabel(DIALOG_FILTER_LOCATION - DIALOG_OFFSET, label);
                         dismissDialog(DIALOG_FILTER_LOCATION);
                     }
                 });
                 break;
             case DIALOG_FILTER_TIME:
                 dialog.setContentView(R.layout.filter_time);
                 dialog.setTitle(getString(R.string.duration_dialog_title));
                 bOk = (Button)dialog.findViewById(R.id.bFilterTimeOk);
                 bOk.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         EditText minDur = (EditText)dialog.findViewById(R.id.etMinDuration);
                         addRecommendationParam(RecParamType.MINDURATION, minDur.getText().toString());
                         EditText maxDur = (EditText)dialog.findViewById(R.id.etMaxDuration);
                         addRecommendationParam(RecParamType.MAXDURATION, maxDur.getText().toString());
                         dismissDialog(DIALOG_FILTER_TIME);
                         
                         // create string for feedback on filter
                         String min = minDur.getText().toString();
                         String max = maxDur.getText().toString();
                         String label = "";
                         if (!min.equalsIgnoreCase("") && !max.equalsIgnoreCase("")) {
                         	int minInt = Integer.parseInt(min);
                         	int maxInt = Integer.parseInt(max);
                         	if (minInt > maxInt) minInt = 0;
                         	label = minInt + " - " + max + " " + getString(R.string.stMinutes);
                         }
                         else if (min.equalsIgnoreCase("") && max.equalsIgnoreCase("")) {
                         	label = getResources().getStringArray(R.array.lvFeedbackDefaultsArray)[DIALOG_FILTER_TIME - DIALOG_OFFSET]; 
                         }
                         else if (min.equalsIgnoreCase("")) {
                         	label = "0 - " + max + " " + getString(R.string.stMinutes);
                         } 
                         else {
                         	label = min + " - " + getString(R.string.stAny) + " " + getString(R.string.stMinutes);
                         }
                         setFilterFeedbackLabel(DIALOG_FILTER_TIME - DIALOG_OFFSET, label);
                     }
                 });
                 break;
             case DIALOG_FILTER_COST:
                 dialog.setContentView(R.layout.filter_cost);
                 dialog.setTitle(getString(R.string.cost_dialog_title));
                 bOk = (Button)dialog.findViewById(R.id.bFilterCostOk);
                 bOk.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         EditText cost = (EditText)dialog.findViewById(R.id.etMaxCost);
                         addRecommendationParam(RecParamType.COST, cost.getText().toString());
                         String costText = cost.getText().toString();
                         String label = "";
                         if (costText.equalsIgnoreCase("")) {
                         	label = getResources().getStringArray(R.array.lvFeedbackDefaultsArray)[DIALOG_FILTER_COST - DIALOG_OFFSET]; 
                         }
                         else if (costText.equalsIgnoreCase("0")) {
                         	label = getString(R.string.stFree);
                         }
                         else {
                         	label = getString(R.string.stMax) + " $" + costText;
                         }
                         dismissDialog(DIALOG_FILTER_COST);
                         setFilterFeedbackLabel(DIALOG_FILTER_COST - DIALOG_OFFSET, label);
                     }
                 });
                 break;
             default:
                 // This activity dialog not known to this activity
                 return super.onCreateDialog(id);
         }
         return dialog;
     }
     
     /*
      * TODO THIS IS VERY BAD. MOVE TO CALLER INSTAED OF BEING STATIC.
      */
     
     // Add a recommendation param for unary parameters
     public static void addRecommendationParam(RecParamType type, boolean selected) {
         ApplicationGlobals g = ApplicationGlobals.getInstance();
         if (g.getRecommendationParams().containsKey(type)) {
             if (selected) return;
             else g.getRecommendationParams().remove(type);
         }
         else if (selected) {
             g.getRecommendationParams().put(type, "");                        
         }
     }
     
     // Add a recommendation param
     public static void addRecommendationParam(RecParamType type, String value) {
         ApplicationGlobals g = ApplicationGlobals.getInstance();
         removeRecommendationParam(type);
         if (!value.equalsIgnoreCase("")) {
             g.getRecommendationParams().put(type, value);
         }
     }
     
     // Remove a recommendation param if it is present
     public static void removeRecommendationParam(RecParamType type) {
         ApplicationGlobals g = ApplicationGlobals.getInstance();
         if (g.getRecommendationParams().containsKey(type)) {
             g.getRecommendationParams().remove(type);
         }
     }
     
     // Set the feedback label for the given filter list item
     private void setFilterFeedbackLabel(int filterId, String label) {    	
         ListView lvOptions = (ListView) findViewById(R.id.lvOptions);
         FilterItemAdapter adapter = (FilterItemAdapter) lvOptions.getAdapter();
         FilterItem item = adapter.getItem(filterId);
        item.feedbackLabel = label;
         adapter.replace(item, filterId);        
         lvOptions.setAdapter(adapter);
         adapter.notifyDataSetChanged();
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     }
     
     // Helper method to get the filter items for making the filter list view
     private FilterItem[] getFilterItems() {
         String [] options = getResources().getStringArray(R.array.lvOptionsArray);
         String [] feedback = getResources().getStringArray(R.array.lvFeedbackDefaultsArray);
         FilterItem[] filterItems = new FilterItem[options.length];
         for (int i = 0; i <options.length; i++) {            
             switch (i) {
             case 0:
                 filterItems[i] = new FilterItem(R.drawable.people32, options[i], feedback[i]);
                 break;
             case 1:
                 filterItems[i] = new FilterItem(R.drawable.cityicon32, options[i], feedback[i]);
                 break;
             case 2:
                 filterItems[i] = new FilterItem(R.drawable.mapsicon32, options[i], feedback[i]);
                 break;
             case 3:
                 filterItems[i] = new FilterItem(R.drawable.clockicon32, options[i], feedback[i]);
                 break;
             case 4:
                 filterItems[i] = new FilterItem(R.drawable.coinsicon32, options[i], feedback[i]);
             }
         }
         return filterItems;
     }
 }
