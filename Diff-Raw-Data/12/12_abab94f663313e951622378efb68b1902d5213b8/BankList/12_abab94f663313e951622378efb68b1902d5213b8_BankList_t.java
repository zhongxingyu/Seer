 package com.arrived1.cotozabank;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Vector;
 
 import com.arrived1.cotozabank.R;
 
 import android.app.Activity;
 import android.widget.Toast;
 
 
 
 public class BankList {
 	private Activity actv;
 	public Vector<Bank> banks;
 	
 	
 	public BankList(Activity activity) {
 		this.actv = activity;
 		this.banks = new Vector<Bank>();
 		
 		try {
 			ReadFromFile();
 		} catch (IOException e) {
			Toast.makeText(actv.getApplicationContext(), "Problems: " + e.getMessage(), Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	public Bank findBank(int bankId) {
 		for(int i = 0; i < banks.size(); i++) {
 			if(banks.elementAt(i).getId() == bankId) {
 				return banks.elementAt(i);
 			}
 		}
 		return null;
 	}
 	
 	
 	private void ReadFromFile() throws IOException {
 		String str = "";
 		
 		InputStream is = actv.getResources().openRawResource(R.drawable.banks);
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		
 		if (is != null) {							
 			while ((str = reader.readLine()) != null) {	
 				Bank bank = new Bank(str);
 				banks.addElement(bank);
 //				System.out.println("DUPA, Bank id: " + bank.getId() + " Bank name: " + bank.getName());
 			}				
 		}		
 		
 		is.close();		
 	}
 }
