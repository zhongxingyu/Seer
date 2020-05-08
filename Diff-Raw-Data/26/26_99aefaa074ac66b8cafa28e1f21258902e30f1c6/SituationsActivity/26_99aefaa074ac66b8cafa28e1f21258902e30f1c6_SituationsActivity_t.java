 package com.christianapps.heavensadv.activities;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.christianapps.heavensadv.values.Situation;
 
 public class SituationsActivity extends ListActivity {
     
 	private ArrayList<Situation> situations = new ArrayList<Situation>();
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
         
           processMenus();
 
           setListAdapter(new ArrayAdapter<Situation>(this, android.R.layout.simple_list_item_1, this.situations));
           	
 	  	  ListView lv = getListView();
 	  	  lv.setTextFilterEnabled(true);	  	  
 	  	  
 	  	  lv.setOnItemClickListener(new OnItemClickListener() {
 	  	    public void onItemClick(AdapterView<?> parent, View view,
 	  	        int position, long id) {
 	  	      
 	  	      Intent myIntent = new Intent(view.getContext(), BibleVersesActivity.class);
 	  	      
 	  	      Situation situation = situations.get((int)id);
 	  	      myIntent.putExtra("situation", situation);
 
 	  	      startActivity(myIntent);	  	    
 	  	    }
 	  	  });
     }
     
     private void processMenus()
     {
     	String[] situations_array = getResources().getStringArray(com.christianapps.heavensadv.R.array.situations_array);
     	int array_length = situations_array.length;
     	this.situations.clear();
     	for (int i=0; i<array_length; i++){
     		String fullText = situations_array[i];
 			Situation situation = parseSituation(fullText);  
     		this.situations.add(situation);
     	}
     }
     
     private Situation parseSituation(String text){
     	StringTokenizer tokenizer = new StringTokenizer(text, "|");  
     	String id = tokenizer.nextToken();    	
     	int array_id = getResources().getIdentifier(id.concat("_array"), "array", "com.christianapps.heavensadv");    	
     	String[] verses_array = getResources().getStringArray(array_id);
 
     	Situation situation = Situation.parse(text, verses_array);
     	return situation; 	
     }
 
 }
