 package fi.dy.esav.EreSeller;
 
 import java.lang.Exception;
 
 import android.os.Bundle;
 
 import android.app.Activity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	EditText i_money, i_buy_p, i_sell_p, i_cust_tax, i_ticket;
 	RadioGroup i_tax;
 	RadioButton i_tax_none, i_tax_cust;
 	
 	TextView o_result;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         init();
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     private void init() {
     	
     	i_money = (EditText) findViewById(R.id.money);
     	addTextListener(i_money);
     	i_buy_p = (EditText) findViewById(R.id.buy_price);
     	addTextListener(i_buy_p);
     	i_sell_p = (EditText) findViewById(R.id.sell_price);
     	addTextListener(i_sell_p);
     	i_cust_tax = (EditText) findViewById(R.id.tax_cust_value);
     	addTextListener(i_cust_tax);
     	i_ticket = (EditText) findViewById(R.id.ticket);
     	addTextListener(i_ticket);
     	
     	o_result = (TextView) findViewById(R.id.output);
     	
     	i_tax = (RadioGroup) findViewById(R.id.tax_radiogroup);
     	i_tax.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				update();
 			}
     	});
     	i_tax_none = (RadioButton) findViewById(R.id.tax_none);
     	i_tax_cust = (RadioButton) findViewById(R.id.tax_cust);
     }
     
     private void addTextListener(EditText et) {
     	et.addTextChangedListener(new TextWatcher() {
 
 			public void afterTextChanged(Editable s) {
 				update();
 			}
 
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {				
 			}
 
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
     		
     	});
     }
     
     private void update() {
     	o_result.setText(calculate());
     }
     
     private String calculate() {
     	
     	double money, buy_p, sell_p, ticket, tax;
     	
     	try {
 	    	money  = Double.parseDouble(i_money.getText().toString());
 	    	buy_p  = Double.parseDouble(i_buy_p.getText().toString());
 	    	sell_p = Double.parseDouble(i_sell_p.getText().toString());
 	    	
 	    	if(i_tax.getCheckedRadioButtonId() == i_tax_none.getId()) {
 	    		tax = 0;
 	    	} else if(i_tax.getCheckedRadioButtonId() == i_tax_cust.getId()) {
 	    		tax = Double.parseDouble(i_cust_tax.getText().toString());
 	    	} else {
 	    		tax = 0;
 	    	}
 	    	
 	    	ticket = Double.parseDouble(i_ticket.getText().toString());
     	} catch (NumberFormatException e) {
     		return "Please fill with valid information";
     	}
     	
     	int amount = (int) Math.floor(money / buy_p);
     	
    	double expenditure = amount * buy_p + 2 * ticket;
     	double revenue_taxless = amount * sell_p;
     	double revenue = amount * sell_p * (1 / (1 + tax / 100));
     	
     	double income = revenue - expenditure;
     	
     	return "You get: " 			+ amount 	+ " items\n" +
     		   "They cost (inc. travel): " + expenditure + "\n" +
     		   "They sell for: " 	+ revenue_taxless    + "\n" +
     		   "Profit: " 			+ income;
     }
     
 }
