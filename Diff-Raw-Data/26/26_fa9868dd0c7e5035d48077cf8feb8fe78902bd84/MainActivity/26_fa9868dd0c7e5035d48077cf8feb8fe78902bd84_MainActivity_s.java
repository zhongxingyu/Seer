 package au.id.teda.broadband.usage.ui;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.FragmentManager;
 import android.widget.TextView;
 import au.id.teda.broadband.usage.R;
 import au.id.teda.broadband.usage.authenticator.AuthenticatorActivity;
 import au.id.teda.broadband.usage.helper.AccountInfoHelper;
 import au.id.teda.broadband.usage.helper.AccountStatusHelper;
 import au.id.teda.broadband.usage.util.NetworkUtilities;
 
 public class MainActivity extends SherlockFragmentActivity {
 	
 	private static final String DEBUG_TAG = "bbusage";
 	
 	private AccountInfoHelper mAccount;
 	
 	private FragmentManager mFragmentManager;
 	
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         // Set up the action bar.
         final ActionBar mActionBar = getSupportActionBar();
         // Set action bar icon for navigation
         mActionBar.setDisplayHomeAsUpEnabled(true);
 
         // Check to see if account has been authenticated
         mAccount = new AccountInfoHelper(this);
         if(!mAccount.isAccountAuthenticated()){
         	Intent authenticatorActivityIntent = new Intent(this, AuthenticatorActivity.class);
     		startActivity(authenticatorActivityIntent);
         } else {
         	loadTextViews();
         }
         
     }
 
 	/**
 	 *  Handler for passing messages from other classes
 	 */
     public Handler handler = new Handler() {
         public void handleMessage(Message msg) {
         	
         	loadTextViews();
         }
     };
     
     // Create options menu
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.options_menu, menu);
         return true;
     }
     
     // Handle options menu clicks
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_settings:
                 Intent settingsActivityIntent = new Intent(getBaseContext(), SettingsActivity.class);
                 startActivity(settingsActivityIntent);
                 return true;
         case R.id.menu_refresh:
         		NetworkUtilities mNetworkUtilities = new NetworkUtilities(this);
         		mNetworkUtilities.getXmlData(item, handler);
                 return true;
         default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     public void loadTextViews(){
     	TextView mUsernameTV = (TextView) findViewById(R.id.activity_main_username_tv);
     	TextView mProductPlanTV = (TextView) findViewById(R.id.activity_main_product_plan_tv);
     	TextView mCurrentMonthTV = (TextView) findViewById(R.id.activity_main_current_month_tv);
     	TextView mRolloverNumberDaysTV = (TextView) findViewById(R.id.activity_main_days_number_tv);
     	TextView mRolloverDateTV = (TextView) findViewById(R.id.activity_main_days_date_tv);
     	TextView mPeakDataNumberTV = (TextView) findViewById(R.id.activity_main_peak_number_tv);
     	TextView mPeakQuotaTV = (TextView) findViewById(R.id.activity_main_peak_quota_tv);
     	TextView mPeakDataTV = (TextView) findViewById(R.id.activity_main_peak_used_tv);
     	TextView mOffpeakDataNumberTV = (TextView) findViewById(R.id.activity_main_offpeak_number_tv);
     	TextView mOffpeakQuotaTV = (TextView) findViewById(R.id.activity_main_offpeak_quota_tv);
     	TextView mOffpeakDataTV = (TextView) findViewById(R.id.activity_main_offpeak_used_tv);
     	TextView mUpTimeNumberTV = (TextView) findViewById(R.id.activity_main_uptime_number_tv);
     	TextView mIpAddresTV = (TextView) findViewById(R.id.activity_main_uptime_ip_tv);
     	
     	AccountInfoHelper info = new AccountInfoHelper(this);
     	AccountStatusHelper status = new AccountStatusHelper(this);
     	
     	mUsernameTV.setText(info.getAccountUsername());
     	mProductPlanTV.setText(info.getProductPlan());
     	mCurrentMonthTV.setText(status.getCurrentMonthString());
     	mRolloverNumberDaysTV.setText(status.getDaysToGoString());
     	mRolloverDateTV.setText(status.getRolloverDateString());
     	mPeakDataNumberTV.setText(status.getPeakDataUsedGbString());
     	mPeakQuotaTV.setText(info.getPeakQuotaString());
     	mPeakDataTV.setText(status.getPeakShapedString());
     	mOffpeakDataNumberTV.setText(status.getOffpeakDataUsedGbString());
     	mOffpeakQuotaTV.setText(info.getOffpeakQuotaString());
     	mOffpeakDataTV.setText(status.getOffpeakShapedString());
    	//mUpTimeNumberTV.setText(Integer.toString(status.getUpTimeDays()));
     	mIpAddresTV.setText(status.getIpAddressStrng());
     }
 
 }
