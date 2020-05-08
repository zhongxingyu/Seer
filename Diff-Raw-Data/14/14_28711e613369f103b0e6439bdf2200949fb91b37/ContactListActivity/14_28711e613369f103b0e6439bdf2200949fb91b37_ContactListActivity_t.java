 package com.example.AndroidContactViewer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.*;
 
 import com.example.AndroidContactViewer.datastore.ContactDataSource;
 
 public class ContactListActivity extends ListActivity {
     ContactListActivity _activity = null;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         _activity = this;
 
 		setContentView(R.layout.contact_list);
 		ToolbarConfig toolbar = new ToolbarConfig(this, "Contacts");
 
 		// setup the about button
 		Button button = toolbar.getToolbarRightButton();
 		button.setText("About");
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				Toast.makeText(
 						ContactListActivity.this,
 						"This is a sample application made for SENG 5199-1 in the MSSE program.",
 						Toast.LENGTH_LONG).show();
 			}
 		});
 		
 		button = toolbar.getToolbarLeftButton();
 		button.setText("Search");
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				onSearchRequested();
 			}
 		});
 		
 
 		// initialize the list view
 		ContactDataSource datasource = new ContactDataSource(this);
 		datasource.open();
 		setListAdapter(new ContactAdapter(this, R.layout.contact_list_item, datasource.all()));
 		datasource.close();
 		
 		Intent intent = getIntent();
        String action = intent.getAction();
		if(action != null && action.equals(Intent.ACTION_SEARCH)) {
     		String query = intent.getStringExtra(SearchManager.QUERY);
     		((ContactAdapter)getListAdapter()).getFilter().filter(query);
        }
 		
 		ListView lv = getListView();
 		lv.setTextFilterEnabled(true);
         lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
             @Override
             public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                 _activity.closeContextMenu();
                 return false;
             }
         });
 
         // setup context menu
         registerForContextMenu(lv);
 	}
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.action_menu, menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         switch (item.getItemId()) {
             case R.id.call:
                 Toast.makeText(this, "Call", 5).show();
                 return true;
             case R.id.message:
                 Toast.makeText(this, "Message", 5).show();
                 return true;
             case R.id.email:
                 Toast.makeText(this, "email", 5).show();
                 return true;
             case R.id.profile:
                 Intent myIntent = new Intent(getBaseContext(), ContactViewActivity.class);
                 myIntent.putExtra("ContactID", ((ContactAdapter)getListAdapter()).getItem(info.position).getContactId());
                 startActivity(myIntent);
                 return true;
             default:
                 return super.onContextItemSelected(item);
         }
     }
 
     @Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 
         this.openContextMenu(v);
 
 	}
 
 	/*
 	 * We need to provide a custom adapter in order to use a custom list item
 	 * view.
 	 */
 	private class ContactAdapter extends ArrayAdapter<Contact> {
 
 		public ContactAdapter(Context context, int textViewResourceId,
 				List<Contact> objects) {
 			super(context, textViewResourceId, objects);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = getLayoutInflater();
 			View item = inflater.inflate(R.layout.contact_list_item, parent, false);
 
 			Contact contact = getItem(position);
 			((TextView) item.findViewById(R.id.item_name)).setText(contact
 					.getName());
 			((TextView) item.findViewById(R.id.item_title)).setText(contact
 					.getTitle());
 			((TextView) item.findViewById(R.id.item_phone)).setText(contact
 					.getDefaultContactPhone());
 
 			return item;
 		}
 	}
 
 }
