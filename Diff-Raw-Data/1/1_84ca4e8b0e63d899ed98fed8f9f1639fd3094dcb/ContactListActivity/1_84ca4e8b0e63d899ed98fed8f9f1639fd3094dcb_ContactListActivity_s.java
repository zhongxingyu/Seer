 package com.example.AndroidContactViewer;
 
 import java.util.List;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import android.view.View.OnClickListener;
 
 import com.example.AndroidContactViewer.datastore.ContactDataSource;
 
 public class ContactListActivity extends ListActivity implements OnClickListener {
 	private boolean filtered = false;
     private ContactListActivity _activity = null;
     protected ContactAdapter contact_adapter;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         _activity = this;
 
 		setContentView(R.layout.contact_list);
 		ToolbarConfig toolbar = new ToolbarConfig(this, "Contacts");
 
 		// setup the about button
 		Button button = toolbar.getToolbarRightButton();
 		button.setText("New Contact");
 		button.setOnClickListener(this);
 
 		
 		toolbar.hideLeftButton();
 		
 
 		// initialize the list view
 		ContactDataSource datasource = new ContactDataSource(this);
 		datasource.open();
         contact_adapter = new ContactAdapter(this, R.layout.contact_list_item, datasource.all());
 		setListAdapter(contact_adapter);
 		datasource.close();
 		
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
 
         //Setup Search
         EditText search_box = (EditText)findViewById(R.id.search_box);
         search_box.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 ContactListActivity.this.contact_adapter.getFilter().filter(charSequence);
             }
 
             @Override
             public void afterTextChanged(Editable editable) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
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
     public void onClick(View v) {
         switch(v.getId()) {
             case R.id.toolbar_right_button:
                 Intent myIntent = new Intent(getBaseContext(), ContactEditActivity.class);
                 myIntent.putExtra("ContactID", 0);
                 startActivity(myIntent);
                 break;
             default:
                 Toast.makeText(
                         ContactListActivity.this,
                         "Unknown click",
                         Toast.LENGTH_SHORT).show();
                 break;
         }
     }
 
     public boolean onSearchRequested() {
         EditText search_box = (EditText)findViewById(R.id.search_box);
         search_box.requestFocus();
 
         // Return false so that Android doesn't try to run an actual search dialog.
         return false;
     }
     
     /* (non-Javadoc)
 	 * @see android.app.Activity#onBackPressed()
 	 */
 	@Override
 	public void onBackPressed() {
         if(filtered){
         	ContactListActivity.this.contact_adapter.getFilter().filter("");
         	EditText search_box = (EditText)findViewById(R.id.search_box);
         	search_box.setText("");
         	filtered = false;
         } else{
         	super.onBackPressed();        	
         }
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
