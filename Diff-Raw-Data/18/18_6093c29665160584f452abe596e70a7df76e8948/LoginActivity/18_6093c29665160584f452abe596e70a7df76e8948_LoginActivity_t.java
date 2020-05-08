 package com.friedran.appengine.dashboard.gui;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
import android.widget.Spinner;
 import com.friedran.appengine.dashboard.R;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class LoginActivity extends Activity {
    protected Spinner mAccountSpinner;

     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
 
         List<String> accountNames = new ArrayList<String>();
         for (Account account : AccountManager.get(this).getAccounts()) {
             accountNames.add(account.name);
         }
 
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, accountNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAccountSpinner = (Spinner)
                findViewById(R.id.login_account_spinner);
        mAccountSpinner.setAdapter(adapter);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
     }
 }
