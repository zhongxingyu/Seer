 package com.example.payback;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
import android.widget.TextView;
 
 public class Transaction5Activity extends Activity {
 	ListView listView;
 	List<String> data = new ArrayList<String>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_transaction5);
 
 	    Bundle oldbundle = getIntent().getExtras();
 	    
 	    int transCostInt = oldbundle.getInt("Transaction1transCost");
 
 	    DecimalFormat dec = new DecimalFormat("0.00");
        float percen = transCostInt/100;
         String transCoststring = "$" + dec.format(percen);
 	    
 	    String transCommentString = oldbundle.getString("Transaction1transComment");
 	    ArrayList<Friend> transselected = oldbundle.getParcelableArrayList("Transaction2selected");
 	    int translenderamountInt = oldbundle.getInt("Transaction3lenderamount");
 	    
 	    DecimalFormat dec2 = new DecimalFormat("0.00");
        float percen2 = translenderamountInt/100;
        String translenderamountstring = "$" + dec.format(percen);
 	    
 	    
 	    boolean button1Selected = oldbundle.getBoolean("Transaction3button1Selected");
 	    boolean button2Selected = oldbundle.getBoolean("Transaction3button2Selected");
 
 		
 
 	    data.add("Amount: " + transCoststring);
 	    data.add("Comment: " + transCommentString);
 	    for(int i = 0; i < transselected.size(); i++){
	    	data.add(transselected.get(i).toString());
 	    }
 	    data.add("LendAmt: " + translenderamountstring);
 	    data.add("Auto: " +String.valueOf(button1Selected));
 	    data.add("Manual: " +String.valueOf(button2Selected));
 
 
 	    
 
 	    listView = (ListView) findViewById(R.id.listviewforplaceholderdata);
 	    listView.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_contact_iteminlist, data));
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.transaction5, menu);
 		return true;
 	}
 
 	public void showTrans4(View view)
     {
 		//NOT FINISHED TODO
 		
 	    Bundle oldbundle = getIntent().getExtras();
 	    
 	    int transCostInt = oldbundle.getInt("Transaction1transCost");
 	    String transCommentString = oldbundle.getString("Transaction1transComment");
 	    ArrayList<Friend> transselected = oldbundle.getParcelableArrayList("Transaction2selected");
 	    int translenderamountInt = oldbundle.getInt("Transaction3lenderamount");
 	    boolean button1Selected = oldbundle.getBoolean("Transaction3button1Selected");
 	    boolean button2Selected = oldbundle.getBoolean("Transaction3button2Selected");
 
     	Intent intent = new Intent(this, Transaction4Activity.class);
         Bundle Bundle = new Bundle();
         
         Bundle.putInt("Transaction1transCost", transCostInt);
         Bundle.putString("Transaction1transComment", transCommentString);
         Bundle.putParcelableArrayList("Transaction2selected", transselected);
         Bundle.putInt("Transaction3lenderamount", translenderamountInt);
         Bundle.putBoolean("Transaction3button1Selected", button1Selected);
         Bundle.putBoolean("Transaction3button2Selected", button2Selected);
         
         //put in new data here from this transaction page
         
         intent.putExtras(Bundle);
         startActivity(intent);
     }
 	
 	public void showMainMenu(View view)
     {
     	Intent intent = new Intent(this, MainActivity.class);
         startActivity(intent);
         this.finish();
     }
 }
