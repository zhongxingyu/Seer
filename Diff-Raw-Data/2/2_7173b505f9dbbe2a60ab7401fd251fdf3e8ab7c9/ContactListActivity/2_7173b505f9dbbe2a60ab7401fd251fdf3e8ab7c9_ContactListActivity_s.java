 package com.example.prolog;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.example.prolog.db.ContactsDataSource;
 import com.example.prolog.model.Contact;
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ContactListActivity extends Activity {
 private Context context=this;
 private ContactsDataSource datasource;
 private ArrayList<Contact> contacts = new ArrayList<Contact>();
 private ArrayList<Contact> arr_sort= new ArrayList<Contact>();
 private String[] lv_arr;
 private EditText ed;
 private ListView lv;
 private Button button1;
 private Button button;
 public static final String LOGTAG="EXPLORECA";
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_contact_list);
 
 		Log.i(LOGTAG,"started ContactListActivity");
 		datasource=new ContactsDataSource(this);
 		datasource.open();
 		contacts=(ArrayList<Contact>) datasource.findAllContacts();
 		lv_arr = new String[contacts.size()];
 		Iterator i = contacts.iterator();
 		int j = 0;
 		while(i.hasNext()) {
 			lv_arr[j]=((Contact) i.next()).getName();
 			j++;
 		}
 		lv = (ListView)findViewById(android.R.id.list);
 		lv.setAdapter(new ContactListAdapter(this, R.id.activityContactListTextView, contacts));
 		lv.setOnItemClickListener(new OnItemClickListener() 
 		{
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position,
 					long id) {
 				// TODO Auto-generated method stub
 				 Intent i = new Intent(context, MyTabActivity.class);
 				 i.putExtra("contactId", contacts.get(position).getId());
 				 startActivity(i);
 			}
 	
 		});
 		//lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.countries)));
		ed = (EditText) findViewById(R.id.EditText01);
 		ed.addTextChangedListener( new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				// TODO Auto-generated method stub
                 arr_sort.clear();
                 for (int i = 0; i < lv_arr.length; i++) {
                         if ((lv_arr[i].toLowerCase()).contains(ed.getText().toString().toLowerCase())) {
                             arr_sort.add(contacts.get(i));
                         }
                 }
         		lv.setAdapter(new ContactListAdapter(ContactListActivity.this, R.id.activityContactListTextView, arr_sort));
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		button = (Button) findViewById(R.id.save);
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				startActivity(new Intent(context,AddNewContactActivity.class));
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.contact_list, menu);
 		return true;
 	}
 
 	
 	private class ContactListAdapter extends ArrayAdapter<Contact> {
 
 		
 		private ArrayList<Contact> items;
 		
 		public ContactListAdapter(Context context, int textViewResourceId, ArrayList<Contact> contacts) {
 			super(context, textViewResourceId, contacts);
 			items=contacts;
 			// TODO Auto-generated constructor stub
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			
 			View row = inflater.inflate(R.layout.activity_contact_list_item, parent, false);
 			
 			ImageView iv = (ImageView) row.findViewById(R.id.activityContactListImageView);
 			TextView tv = (TextView) row.findViewById(R.id.activityContactListTextView);
 			Log.i(LOGTAG,"id :"+ items.get(position).getId());
 			tv.setText(items.get(position).getName());
 			iv.setImageResource(R.drawable.ic_launcher);
 			return row;
 		}
 	}
 }
