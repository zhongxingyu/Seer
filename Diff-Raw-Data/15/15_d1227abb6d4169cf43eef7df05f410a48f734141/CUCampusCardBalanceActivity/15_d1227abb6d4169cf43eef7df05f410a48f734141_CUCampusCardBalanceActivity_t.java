 package ca.carleton.ccsl.cubalance;
 
 import java.text.DateFormat;
 import java.text.NumberFormat;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CUCampusCardBalanceActivity extends Activity
 {
   private final String       TAG      = getClass().getSimpleName();
   private final DateFormat   DATE_FMT = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT);
   private final NumberFormat CASH_FMT = NumberFormat.getCurrencyInstance();

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     
     final Button button = (Button) findViewById(R.id.updateBalanceBtn);
     button.setOnClickListener(new View.OnClickListener() 
     {
       public void onClick(View v)
       {
         fetchBalance();
       }
     });
   }
   
   @Override
   public void onStart()
   {
     super.onStart();
     
     final SharedPreferences settings 
       = getSharedPreferences(CUBalanceSettings.PREFS_NAME, MODE_PRIVATE);
     
     final TextView balance   = (TextView) findViewById(R.id.balanceTxt);
     final TextView updatedAt = (TextView) findViewById(R.id.updatedAtTxt);
 
     Boolean autoUp   = settings.getBoolean(CUBalanceSettings.UPDATE_KEY, false);
     String prefsUser = settings.getString(CUBalanceSettings.USER_KEY, "");
     String prefsPin  = settings.getString(CUBalanceSettings.PIN_KEY,  "");
     String lastBal   = settings.getString(CUBalanceSettings.BAL_KEY,  "");
     String lastUp    = settings.getString(CUBalanceSettings.DATE_KEY, "");
     
     //If the user or pin haven't been changed from the default, we need to show
     //the settings screen before anything meaningful can happen in this Activity
     if(prefsUser.equals("") || prefsPin.equals(""))
     {
       Log.i(TAG, "No first setup done. Launching CUBalanceSettings activity.");
       Intent myIntent = new Intent(getBaseContext(), CUBalanceSettings.class);
       startActivityForResult(myIntent, 0);
     }
     
     //There is a 'cached' balance, set the text box to that value.
     if(!lastBal.equals(""))
       balance.setText(lastBal);
     
     if(!lastUp.equals(""))
       updatedAt.setText("Last Updated: "+ lastUp);
     
     if(autoUp)
       fetchBalance();
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) 
   {
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.menu, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) 
   {
     // Handle item selection
     switch (item.getItemId()) 
     {
       case R.id.menu_settings_item:
         Intent myIntent = new Intent(getBaseContext(), CUBalanceSettings.class);
         startActivityForResult(myIntent, 0);
         return true;
       default:
         return super.onOptionsItemSelected(item);
     }
   }
   
   private void fetchBalance()
   {
     final CUCampusCardBalanceActivity mainUI = this;
     
     final SharedPreferences settings 
       = getSharedPreferences(CUBalanceSettings.PREFS_NAME, MODE_PRIVATE);
     
     String prefsUser = settings.getString(CUBalanceSettings.USER_KEY, "");
     String prefsPin  = settings.getString(CUBalanceSettings.PIN_KEY,  "");
 
     Log.i(TAG, "Spawning a CUBalanceFetcher task.");
     
     try {
       new CUBalanceFetcher(prefsUser, prefsPin, mainUI).execute();
       Toast.makeText(this, "Updating Balance...", Toast.LENGTH_SHORT).show();
     } catch(Exception e) {
       Log.e(TAG, "Unable to instantiate CUBalanceFetcher");
       Log.e(TAG, e.getMessage());
       Log.e(TAG, e.toString());
       
       Toast.makeText(this, "Error updating balance.", Toast.LENGTH_SHORT).show();
     }
   }
 
   public void updateBalance(CUBalanceResult result)
   {
     final TextView  balance   = (TextView) findViewById(R.id.balanceTxt);
     final TextView  updatedAt = (TextView) findViewById(R.id.updatedAtTxt);
     
     if(result.hasError())
     {
       Toast.makeText(this, result.getError(), Toast.LENGTH_SHORT).show();
       return;
     }
 
     final SharedPreferences settings 
       = getSharedPreferences(CUBalanceSettings.PREFS_NAME, MODE_PRIVATE);
     final SharedPreferences.Editor editor = settings.edit();
     
     String dateStr    = DATE_FMT.format(new java.util.Date());
     String balanceStr = CASH_FMT.format(result.getBalance());
 
     Log.i(TAG, "Updating cached balance to "+ balanceStr);
     Log.i(TAG, "Updating cached balance date to "+ dateStr);
     editor.putString(CUBalanceSettings.BAL_KEY, balanceStr);
     editor.putString(CUBalanceSettings.DATE_KEY, dateStr);
     editor.commit();
     
     balance.setText(balanceStr);
     updatedAt.setText("Last Updated: "+ dateStr);
     
     Toast.makeText(this, "Balance updated.", Toast.LENGTH_SHORT).show();
   }
 }
