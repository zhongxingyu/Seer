 package com.cloudappstudio.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.cloudappstudio.android.R;
 //import com.cloudappstudio.utility.CloudViewEntryParser;
import com.cloudappstudio.utility.CloudeOAuth;
 
 /**
  * An activity that lets the user log in to their google account
  * @author mrjanek <Jesper Lindberg>
  */
 
 public class LoginActivity extends SherlockActivity {
	
 	private AccountManager am;
 	private Activity       activity;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(com.cloudappstudio.android.R.layout.login_view);	
 		am = AccountManager.get(this);
         activity = this;
         final Account[] accounts = am.getAccountsByType("com.google");// Get an instance and retrieve all google accounts.
         final List<String> accountNames = new ArrayList<String>();
         for (Account account : accounts)
             accountNames.add(account.name);
         ListView accountList = (ListView) findViewById(R.id.accountListView);// st_account_list is defined in an XML layout
         accountList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accountNames));
         
         
         accountList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
			public void onItemClick(AdapterView<?> parent, View view, final int pos,
 					long id) {
 				 Log.d("test","cliking");
				 
				 new Thread(new Runnable()
					{
					public void run()
					{
						CloudeOAuth ca = new CloudeOAuth();
						try{
						String token = ca.getToken(accounts[pos], LoginActivity.this);
						String content = ca.getContent("https://cloudappstudio360.appspot.com/api/json/v2/apps.json", token);
						Log.d("debug",content);
						}catch(Exception e){
							
							Log.d("debug","No go!");
						}
					}}).start();
					
				 
				Intent intent = new Intent(getApplicationContext(), WebApplicationsActivity.class);
 				startActivity(intent);
 				
 			}
 		});
         
 	}
 
 }

