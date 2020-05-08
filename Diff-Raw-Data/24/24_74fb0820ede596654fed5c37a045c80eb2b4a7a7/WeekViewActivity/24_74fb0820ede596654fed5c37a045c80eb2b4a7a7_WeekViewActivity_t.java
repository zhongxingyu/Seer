 package giulio.frasca.silencesched.weekview;
 
 
 import giulio.frasca.lib.TimeFunctions;
 import giulio.frasca.silencesched.R;
 import giulio.frasca.silencesched.Schedule;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WeekViewActivity extends Activity{
 
 	//private TableRow sunRow,monRow,tueRow,wedRow,thuRow,friRow,satRow;
 	private RadioButton sunRadio,monRadio,tueRadio,wedRadio,thuRadio,friRadio,satRadio;
 	private DayBar sunBar,monBar,tueBar,wedBar,thuBar,friBar,satBar;
 	private Schedule schedule;
 	private final String PREF_FILE = "ncsusilencepreffile2";
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.weekview);
         SharedPreferences settings = getSharedPreferences(PREF_FILE,Context.MODE_PRIVATE);
         schedule = new Schedule(settings);
         
         /**
         Button editView = (Button) findViewById(R.id.editView);
         editView.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 Intent myIntent = new Intent(view.getContext(), EditEventActivity.class);
                 startActivityForResult(myIntent, 0);
             }
         });*/
 
         
         
         initComponents();
         loadDaySched(schedule);
     }
     
     public void loadDaySched(Schedule schedule){
 //    	LinkedList<RingerSettingBlock> allBlocks = schedule.getList();
 //    	sunBar.loadApplicableBlocks(allBlocks);
 //    	monBar.loadApplicableBlocks(allBlocks);
 //    	tueBar.loadApplicableBlocks(allBlocks);
 //    	wedBar.loadApplicableBlocks(allBlocks);
 //    	thuBar.loadApplicableBlocks(allBlocks);
 //    	friBar.loadApplicableBlocks(allBlocks);
 //    	satBar.loadApplicableBlocks(allBlocks);
     	sunBar.loadApplicableBlocks(schedule);
     	monBar.loadApplicableBlocks(schedule);
     	tueBar.loadApplicableBlocks(schedule);
     	wedBar.loadApplicableBlocks(schedule);
     	thuBar.loadApplicableBlocks(schedule);
     	friBar.loadApplicableBlocks(schedule);
     	satBar.loadApplicableBlocks(schedule);
     }
     
     public void initComponents(){
     	TextView silentKeyItem = (TextView)findViewById(R.id.silentKeyItem);
     	TextView vibKeyItem = (TextView)findViewById(R.id.vibKeyItem);
     	TextView normKeyItem = (TextView)findViewById(R.id.normKeyItem);
     	
 //    	sunRow = (TableRow)findViewById(R.id.sunRow);
 //    	monRow = (TableRow)findViewById(R.id.monRow);
 //    	tueRow = (TableRow)findViewById(R.id.tueRow);
 //    	wedRow = (TableRow)findViewById(R.id.wedRow);
 //    	thuRow = (TableRow)findViewById(R.id.thuRow);
 //    	friRow = (TableRow)findViewById(R.id.friRow);
 //    	satRow = (TableRow)findViewById(R.id.satRow);
     	
     	sunRadio = (RadioButton)findViewById(R.id.sunRadio);
     	monRadio = (RadioButton)findViewById(R.id.monRadio);
     	tueRadio = (RadioButton)findViewById(R.id.tueRadio);
     	wedRadio = (RadioButton)findViewById(R.id.wedRadio);
     	thuRadio = (RadioButton)findViewById(R.id.thuRadio);
     	friRadio = (RadioButton)findViewById(R.id.friRadio);
     	satRadio = (RadioButton)findViewById(R.id.satRadio);
     	
     	sunBar = (DayBar)findViewById(R.id.sunBar);
     	monBar = (DayBar)findViewById(R.id.monBar);
     	tueBar = (DayBar)findViewById(R.id.tueBar);
     	wedBar = (DayBar)findViewById(R.id.wedBar);
     	thuBar = (DayBar)findViewById(R.id.thuBar);
     	friBar = (DayBar)findViewById(R.id.friBar);
     	satBar = (DayBar)findViewById(R.id.satBar);
     	
     	sunBar.setDayName("Sunday");
     	monBar.setDayName("Monday");
     	tueBar.setDayName("Tuesday");
     	wedBar.setDayName("Wednesday");
     	thuBar.setDayName("Thursday");
     	friBar.setDayName("Friday");
     	satBar.setDayName("Saturday");
     	
     	updateRadios();
     	silentKeyItem.setTextColor(Color.rgb(178,34,34));
     	vibKeyItem.setTextColor(Color.rgb(255,185,0));
     	normKeyItem.setTextColor(Color.rgb(0, 100 ,0));
     }
     
     
     public void updateRadios(){
     	long today = System.currentTimeMillis();
    		sunRadio.setChecked(TimeFunctions.isSunday(today));
    		monRadio.setChecked(TimeFunctions.isMonday(today));
    		tueRadio.setChecked(TimeFunctions.isTuesday(today));
    		wedRadio.setChecked(TimeFunctions.isWednesday(today));
    		thuRadio.setChecked(TimeFunctions.isThursday(today));
    		friRadio.setChecked(TimeFunctions.isFriday(today));
    		satRadio.setChecked(TimeFunctions.isSaturday(today));
    		
     }
     /**
      * Prints a temporary 'tooltip' style reminder on the GUI
      * 
      * @param msg - a short message to print
      */
     public void toastMessage(String msg){
     	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
     }
     
     /**
 	 * Prints a logcat message with a customdebug tag
 	 * 
 	 * @param message - the message to include with the logcat packet
 	 */
     public void logcatPrint(String message){
     	Log.v("customdebug",message + " | sent from " +this.getClass().getSimpleName());
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.visualizer_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.backButton:
             	this.finish();
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     @Override
     public void onPause(){
     	super.onPause();
     	logcatPrint("pausing");
     }
     
     public void onResume(){
     	super.onResume();
     	logcatPrint("resuming");
     	//loadDaySched(schedule);
     	this.onCreate(new Bundle());
     	
     }
     
 }
