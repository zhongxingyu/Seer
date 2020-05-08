 package no.ntnu.stud.fallprevention;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
import android.content.Intent;
 import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class Related extends Activity {
 	ArrayList<Contact> contacts = new ArrayList<Contact>();
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_related);
 		//set inn getContactsfrom Database
 		contacts.add(new Contact("Tore", "Hansen", 42341515));
 		ListView listView = (ListView) findViewById(R.id.listView1);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  
			
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						fireEvent();
					}  
			      }); 
 		ArrayList<String> listItems = new ArrayList<String>();
 		for (Contact c : contacts) {
 			listItems.add(c.toString());
 		}
 		final ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, listItems);
 		listView.setAdapter( adapter );
 		
 		
 		
 	}
	protected void fireEvent() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, ContactPerson.class);
		startActivity(intent);
	}
 
 
 
 }
