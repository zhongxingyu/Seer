 package au.edu.qut.inn372.greenhat.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import au.edu.qut.inn372.greenhat.bean.Calculator;
 
 public class CustomerUsageActivity extends Activity {
 	
 	//add this 
 		public final static String EXTRA_MESSAGE = "au.edu.qut.inn372.inn372.greenhat.activity.CustomerUsageActivity";
 		public final static String EXTRA_MESSAGE2 = "au.edu.qut.inn372.inn372.greenhat.activity.CustomerUsageActivity2";
 		
 		private TabbedActivity parentTabbedActivity;
 
 		@Override
 	    public void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.activity_customer_usage_input);
 	        //getActionBar().setDisplayHomeAsUpEnabled(true);
 	        parentTabbedActivity = (TabbedActivity)this.getParent();
 	    }
 		
 		/**
 		 * Refers to the succeding tab
 		 * @param view
 		 */
 		public void viewNext(View view){
 	    	int targetActivity = TabbedActivity.EQUIPMENT_ID;
 	    	parentTabbedActivity.switchTab(targetActivity);
 		}
 		
 		/**
 		 * Refers to the preceding Tab
 		 * @param view
 		 */
 		public void viewBack(View view){
 			Intent intent = new Intent(this, LoginActivity.class);
 	    	
 	    	startActivity(intent);
 		}
 		
 		/**
 		 * Saves current input data to the calculator bean
 		 */
 		private void saveData() {
 			Calculator calculator = parentTabbedActivity.getCalculator();
 			calculator.getCustomer().getElectricityUsage().setDailyAverageUsage(new Double(((EditText)findViewById(R.id.editCustomerUsage_CurrentUsage_UsagePerDay)).getText().toString()));
 		}
 		
 		/**
 		 * Populates input fields with data in the calculator bean
 		 */
 		private void loadData() {
 			Calculator calculator = parentTabbedActivity.getCalculator();
 			EditText inputDailyAverage = (EditText)findViewById(R.id.editCustomerUsage_CurrentUsage_UsagePerDay);
 			inputDailyAverage.setText(new Double(calculator.getCustomer().getElectricityUsage().getDailyAverageUsage()).toString());
 		}
 		
 		@Override
 		public void onPause() {
 			saveData();
 			super.onPause();
 		}
 		
 		@Override
 		public void onResume() {
 			super.onResume();
			loadData();
 		}
 }
