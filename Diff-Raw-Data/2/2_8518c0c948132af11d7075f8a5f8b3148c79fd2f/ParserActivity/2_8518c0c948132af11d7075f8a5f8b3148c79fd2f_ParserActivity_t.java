 package ru.spbau.WhereIsMyMoney.gui;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import ru.spbau.WhereIsMyMoney.R;
 
 public class ParserActivity extends Activity {
    String smsText;
     String smsSource;
     int transactionType = 0;
     final String[] types = {"Withdraw", "Deposit"};
 
     public void saveParser(View view) {
         EditText sms = (EditText)findViewById(R.id.sms);
         smsText = sms.getText().toString();
         finish();
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.parser);
 
         Intent intent = getIntent();
         smsText = intent.getStringExtra(SmsViewActivity.BODY);
         smsSource = intent.getStringExtra(SmsViewActivity.SOURCE);
 
         EditText sms = (EditText)findViewById(R.id.sms);
         sms.setText(smsText);
 
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
         Spinner type = (Spinner)findViewById(R.id.type);
         type.setAdapter(adapter);
         type.setSelection(0);
 
         type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 transactionType = position;
             }
 
             public void onNothingSelected(AdapterView<?> parent) {
                 transactionType = 0;
             }
         });
 
     }
 }
