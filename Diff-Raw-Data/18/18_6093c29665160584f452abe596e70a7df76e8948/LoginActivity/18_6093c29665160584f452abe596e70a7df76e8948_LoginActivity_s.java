 package com.friedran.appengine.dashboard.gui;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
 import com.friedran.appengine.dashboard.R;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class LoginActivity extends Activity {
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.login);
 
         List<String> accountNames = new ArrayList<String>();
         for (Account account : AccountManager.get(this).getAccounts()) {
             accountNames.add(account.name);
         }
 
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, accountNames);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.login_account_text_view);
        textView.setAdapter(adapter);
     }
 
     @Override
     protected void onResume() {
         super.onResume();

     }
 }
