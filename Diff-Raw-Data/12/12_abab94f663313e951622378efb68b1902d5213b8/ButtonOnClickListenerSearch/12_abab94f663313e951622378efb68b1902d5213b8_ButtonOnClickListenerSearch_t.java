 package com.arrived1.cotozabank;
 
 import com.arrived1.cotozabank.R;
 
 import android.app.Activity;
 import android.text.TextUtils;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class ButtonOnClickListenerSearch implements View.OnClickListener {
 	private Activity actv;
 	private BankList bankList;
 	
 	public ButtonOnClickListenerSearch(Activity activity_, BankList bankList_) {
 		this.actv = activity_;
 		this.bankList = bankList_;
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		EditText bankIdEditText = (EditText)actv.findViewById(R.id.accountNumber2);
     	String textViewString = bankIdEditText.getText().toString();
     	
     	if(TextUtils.isEmpty(textViewString))
    		Toast.makeText(actv.getApplicationContext(), "Najpierw wpisz numer konta!", Toast.LENGTH_SHORT).show();            	
     	else {
     		int bankIdToCheck = Integer.parseInt(textViewString);
     	    Bank bank = this.bankList.findBank(bankIdToCheck);
     	    
     	    if(bank != null) {
     	    	TextView bankNamePrinter = (TextView)actv.findViewById(R.id.bankName);
         	    bankNamePrinter.setText(bank.getName());	
     	    }
     	    else {
    	    	Toast.makeText(actv.getApplicationContext(), "Nie znam takiego banku. Zły numer!", Toast.LENGTH_SHORT).show();
 
     	    	EditText bankName = (EditText)actv.findViewById(R.id.accountNumber2);
     	    	bankName.setText("");
     	    	
     	    	TextView bankNamePrinter = (TextView)actv.findViewById(R.id.bankName);
     	    	bankNamePrinter.setText("");
     	    }
     	    
     	    
     	}
 	}
 
 }
