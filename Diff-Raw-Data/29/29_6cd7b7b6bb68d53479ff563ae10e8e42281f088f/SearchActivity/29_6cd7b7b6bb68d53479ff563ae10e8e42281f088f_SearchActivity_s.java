 package no.uib.nutritionapplication;
 
 import java.util.ArrayList;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class SearchActivity extends Activity
 {

 private ListView list;
 private EditText edText;
 private String listview_array[] = { "Bread", "Rice", "Croissant", "Cheese", "Burger", "Soda", "Juice", "Chocolate", "Butter", "Jam" };
 private ArrayList<String> array_sort= new ArrayList<String>();
 int textlength = 0;
 
 public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.activity_search);
 
 	list = (ListView) findViewById(R.id.ListView01);
 	edText = (EditText) findViewById(R.id.EditText01);
 	list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listview_array));
 	
	// Start Petter code
 	list.setOnItemClickListener(new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long rowId) {
 			
			String product = listview_array[position].toString();
			
			Intent i=new Intent(SearchActivity.this, AddMealActivity.class);
			i.putExtra("product", product);
			
			startActivity(i);
			
			
 		}
 	});
	// End Petter code
 	
 	edText.addTextChangedListener(new TextWatcher() {
 
 		public void afterTextChanged(Editable s){
 			// Abstract Method of TextWatcher Interface.
 		}
 
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			// Abstract Method of TextWatcher Interface.
 		}
 		
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 			textlength = edText.getText().length();
 			array_sort.clear();
 			
 			for (int i = 0; i < listview_array.length; i++) {
 				if (textlength <= listview_array[i].length()) {
 					if(edText.getText().toString().equalsIgnoreCase((String)listview_array[i].subSequence(0,textlength))){
 						array_sort.add(listview_array[i]);
 					}
                 }
 			}
 
 			list.setAdapter(new ArrayAdapter<String>(SearchActivity.this,android.R.layout.simple_list_item_1, array_sort));
 			}
 		}
 	);
 	}
 }
