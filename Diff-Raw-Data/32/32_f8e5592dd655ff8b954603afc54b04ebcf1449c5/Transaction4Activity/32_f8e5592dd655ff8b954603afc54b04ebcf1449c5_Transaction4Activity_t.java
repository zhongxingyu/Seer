 package com.example.payback;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Transaction4Activity extends TitleActivity {
 
 	 ListView listView;
 	 EditText editing;
 	 TextView text;
 
 	 ArrayList<Map<String, String>> data;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		   super.onCreate(savedInstanceState);
 			modifyTitle("Borrowers' Share",R.layout.activity_transaction4);
 
 		    listView = (ListView) findViewById(R.id.Listofselected);
 		    text = (TextView) findViewById(R.id.borrowername);
 		    editing = (EditText) findViewById(R.id.borroweramount);
 		
 		    data = buildData();
 		    String[] from = { "friendname", "data" };
 		    int[] to = { R.id.borrowername, R.id.borroweramount };
 
 		    Transaction4_listview_adapter adapter = new Transaction4_listview_adapter(this, data,
 		        R.layout.activity_transaction4_listviewitem, from, to);
 		    
 		   listView.setAdapter(adapter);
 	}	    
 
 			  
 	private ArrayList<Map<String, String>> buildData() {
 	    Bundle oldbundle = getIntent().getExtras();
 	    ArrayList<Friend> transselected = oldbundle.getParcelableArrayList("Transaction2selected");
 	    ArrayList<Integer> lendsharelist = oldbundle.getIntegerArrayList("Transaction3borroweramountlist");
 		
 	    ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
 	    
 	    for(int i = 0; i < transselected.size(); i++){
 	    	
 	    list.add(putData(transselected.get(i).toString(), String.valueOf(lendsharelist.get(i))));
 
 	    }
 	    return list;
 	}
 
 	private HashMap<String, String> putData(String friendname, String data) {
 		HashMap<String, String> item = new HashMap<String, String>();
 		item.put("friendname", friendname);
 		item.put("data", data);
 		return item;
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.transaction4, menu);
 		return true;
 	}
 	
 	public void showTrans3(View view)
     {
 		
 	    Bundle oldbundle = getIntent().getExtras();
 	    
 	    int transCostInt = oldbundle.getInt("Transaction1transCost");
 	    String transCommentString = oldbundle.getString("Transaction1transComment");
 	    ArrayList<Friend> transselected = oldbundle.getParcelableArrayList("Transaction2selected");
 
     	Intent intent = new Intent(this, Transaction3Activity.class);
         Bundle Bundle = new Bundle();
         
         Bundle.putInt("Transaction1transCost", transCostInt);
         Bundle.putString("Transaction1transComment", transCommentString);
         Bundle.putParcelableArrayList("Transaction2selected", transselected);
         
         intent.putExtras(Bundle);
         startActivity(intent);
     }
 	
	public void checktomoveontotrans5(View view){
	    Bundle oldbundle = getIntent().getExtras();
	    int transCostInt = oldbundle.getInt("Transaction1transCost");
	    int translenderamountInt = oldbundle.getInt("Transaction3lenderamount");



        int counting = 0;
        for(int i = 0; i < data.size(); i++){
        	counting = counting + Integer.parseInt(data.get(i).get("data"));
        }
        if(counting != (transCostInt - translenderamountInt)){
			Toast.makeText(getApplicationContext(), "("+String.valueOf((transCostInt - translenderamountInt)-counting) 
					+ ") Placeholder Money ERROR", Toast.LENGTH_SHORT).show();
        }
        else{
        	showTrans5(view);
        }
        
		
		
	}
	
 	public void showTrans5(View view)
     {
 	    Bundle oldbundle = getIntent().getExtras();
 	    
 	    int transCostInt = oldbundle.getInt("Transaction1transCost");
 	    String transCommentString = oldbundle.getString("Transaction1transComment");
 	    ArrayList<Friend> transselected = oldbundle.getParcelableArrayList("Transaction2selected");
 	    int translenderamountInt = oldbundle.getInt("Transaction3lenderamount");
 	    
 //	    ArrayList<Integer> lendsharelist = oldbundle.getIntegerArrayList("Transaction3borroweramountlist");
 	    
 	    boolean button1Selected = oldbundle.getBoolean("Transaction3button1Selected");
 	    boolean button2Selected = oldbundle.getBoolean("Transaction3button2Selected");
 
     	Intent intent = new Intent(this, Transaction5Activity.class);
         Bundle Bundle = new Bundle();
         
         Bundle.putInt("Transaction1transCost", transCostInt);
         Bundle.putString("Transaction1transComment", transCommentString);
         Bundle.putParcelableArrayList("Transaction2selected", transselected);
         Bundle.putInt("Transaction3lenderamount", translenderamountInt);
 
         ArrayList<Integer> lendsharelist = new ArrayList<Integer>();
         for(int i = 0; i < data.size(); i++){
         	lendsharelist.add(Integer.parseInt(data.get(i).get("data")));
         }
 
         Bundle.putIntegerArrayList("Transaction3borroweramountlist", lendsharelist);
         
         Bundle.putBoolean("Transaction3button1Selected", button1Selected);
         Bundle.putBoolean("Transaction3button2Selected", button2Selected);
                
         intent.putExtras(Bundle);
         startActivity(intent);
     }
 
 }
