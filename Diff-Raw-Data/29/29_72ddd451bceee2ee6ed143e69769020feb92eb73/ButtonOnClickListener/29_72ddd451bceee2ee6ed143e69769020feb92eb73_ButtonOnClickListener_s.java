 package com.example.cotozabank;
 
 import android.app.Activity;
 import android.text.TextUtils;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class ButtonOnClickListener implements View.OnClickListener {
 	private Activity actv;
 	private BankList bankList;
 	
 	public ButtonOnClickListener(Activity activity_, BankList bankList_) {
 		this.actv = activity_;
 		this.bankList = bankList_;
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		EditText bankIdEditText = (EditText)actv.findViewById(R.id.accountNumber2);
     	String textViewString = bankIdEditText.getText().toString();
     	
     	if(TextUtils.isEmpty(textViewString)) {
     		Toast.makeText(actv.getApplicationContext(), "Najpierw wpisz numer konta !!!", Toast.LENGTH_LONG).show();            	
     	}
     	else {
     		int bankIdToCheck = Integer.parseInt(textViewString);
     	    Bank bank = this.bankList.findBank(bankIdToCheck);
     	    
    	    TextView bankNamePrinter = (TextView)actv.findViewById(R.id.bankName);
    	    bankNamePrinter.setText(bank.getName());
     	}
		
 	}
 
 }
