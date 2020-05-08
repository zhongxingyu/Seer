 package au.edu.qut.inn372.greenhat.activity;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.support.v4.app.NavUtils;
 import au.edu.qut.inn372.greenhat.bean.Calculation;
 import au.edu.qut.inn372.greenhat.bean.Calculator;
 import au.edu.qut.inn372.greenhat.mediator.CalculatorMediator;
 
 public class BasicInputActivity extends Activity {
 
 	//add this 
 	public final static String EXTRA_MESSAGE = "au.edu.qut.inn372.inn372.greenhat.activity.BasicInputActivity";
 	public final static String EXTRA_MESSAGE2 = "au.edu.qut.inn372.inn372.greenhat.activity.BasicInputActivity2";
 	
 	private TabbedActivity parentTabbedActivity;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_basic_input);
         parentTabbedActivity = (TabbedActivity)this.getParent();
         //getActionBar().setDisplayHomeAsUpEnabled(true);
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_basic_input, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     public void calculate(View view){
     	saveData();
     	
     	parentTabbedActivity.calcEnergyProduction();
     	
     	Intent intent = new Intent(this, TabbedOutputActivity.class);
     	intent.putExtra("Calculator", parentTabbedActivity.getCalculator());
     	
     	startActivity(intent);
     
     }
     
     /*public void reset(View view){
 		// Equipment
 		((EditText)findViewById(R.id.editEquipment_Size)).setText("4.5");
 		((EditText)findViewById(R.id.editEquiment_InverterEfficiency)).setText("90.0");
 		// Roof
 		((EditText)findViewById(R.id.editRoof_LossNorth)).setText("5.0");
 		((EditText)findViewById(R.id.editRoof_LossWest)).setText("15.0");
 		((EditText)findViewById(R.id.editRoof_PercentageNorth)).setText("90.0");
 		((EditText)findViewById(R.id.editRoof_PercentageWest)).setText("10.0");
 		// Location (day light hours)
 		((EditText)findViewById(R.id.editSunlight_Daylight)).setText("4.5");
 		// Current usage
 		((EditText)findViewById(R.id.editUser_UsagePerDay)).setText("50");
     }*/
 
 	/**
 	 * Saves current input data to the calculator bean
 	 */
 	private void saveData() {
 		Calculator calculator = parentTabbedActivity.getCalculator();
 		calculator.getCustomer().getElectricityUsage().setDailyAverageUsage(new Double(((EditText)findViewById(R.id.editRoof_Usage_UsagePerDay)).getText().toString()));
 	}
 	
 	/**
 	 * Populates input fields with data in the calculator bean
 	 */
 	private void loadData() {
 		Calculator calculator = parentTabbedActivity.getCalculator();
 		EditText inputDailyAverage = (EditText)findViewById(R.id.editRoof_Usage_UsagePerDay);
 		inputDailyAverage.setText(new Double(calculator.getCustomer().getElectricityUsage().getDailyAverageUsage()).toString());
 	}
 	
 	@Override
 	public void onPause() {
 		saveData();
 		super.onPause();
 	}
 	
 	@Override
 	public void onResume() {
		loadData();
 		super.onResume();
 	}
     
 }
