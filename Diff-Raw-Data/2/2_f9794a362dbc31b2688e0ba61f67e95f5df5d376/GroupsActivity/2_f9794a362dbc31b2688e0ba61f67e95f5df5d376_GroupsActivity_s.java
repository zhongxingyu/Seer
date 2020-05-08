 package com.teamsexy.helloTabs;
 
 import android.app.ListActivity;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 
 public class GroupsActivity extends ListActivity {
     /** Called when the activity is first created. */
 	Cursor model = null;
 	GroupHelper helper = null;
 	SpotAdapter adapter = null;
 
 	EditText name=null;
 	
 	
 	public final static String ID_EXTRA="apt.tutorial._ID";
 	
 	@Override 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.groups_main);
 
         //make the save button green
         //save.getBackground().setColorFilter(new LightingColorFilter(0xFF00FF00, 0xFFAAFFFF));
         
         helper= new GroupHelper(this);
         model=helper.getAll();
         startManagingCursor(model);
         adapter = new SpotAdapter(model);
         setListAdapter(adapter);
 
     }
 	
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 		helper.close(); 
 	}
 
 	  @Override
 	public void onListItemClick(ListView list, View view, int position, long id) {
 		Intent i = new Intent(GroupsActivity.this, GroupDetailForm.class);
 
 		i.putExtra(ID_EXTRA, String.valueOf(id));
 		System.out.println("about to start intent");
 		startActivity(i);
 	}
 	  
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		new MenuInflater(this).inflate(R.menu.group_options, menu);
 		return (super.onCreateOptionsMenu(menu));
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.newSpot) {
 			startActivity(new Intent(GroupsActivity.this, GroupDetailForm.class));
 			return true;
 		} else if (item.getItemId() == R.id.settings){
 			 Log.d("about to enter settingsactivity.java", "@@@");
 			startActivity(new Intent(GroupsActivity.this, SettingsActivity.class));
 			return true;
 		}
 		return (super.onOptionsItemSelected(item));
 	}
 	
 	class SpotAdapter extends CursorAdapter {
 		SpotAdapter(Cursor c) {
 			super(GroupsActivity.this, c);
 		}
 	
 		@Override
 		public void bindView(View row, Context ctxt, Cursor c) {
 			SpotHolder holder=(SpotHolder)row.getTag();
 			holder.populateFrom(c, helper); 
 		}
 		
 		@Override
 		public View newView(Context ctxt, Cursor c,ViewGroup parent) { 
 			LayoutInflater inflater=getLayoutInflater();
 			View row=inflater.inflate(R.layout.group_row, parent, false);
 			SpotHolder holder=new SpotHolder(row);
 			row.setTag(holder);
 		    return(row);
 		  }
 	} // end of class SpotAdapter
 	
 	
 	static class SpotHolder {
 		private TextView name = null;
 //		private TextView address = null;
 		private ImageView icon = null;
 
 		SpotHolder(View row) {
 			name = (TextView)row.findViewById(R.id.title);
 			icon = (ImageView) row.findViewById(R.id.icon);
 		}
 
 		void populateFrom(Cursor c, GroupHelper helper) {
 			name.setText(helper.getName(c));
 			icon.setImageResource(R.drawable.group_icon);
 //			address.setText(helper.getAddress(c));
 //			
 //			if (helper.getType(c).equals("happy_hr")) {
 //				icon.setImageResource(R.drawable.happy_hr_spot);
 //			} else {
 //				icon.setImageResource(R.drawable.my_spot);
 //			}
 		}
 	} //end of SpotHolder class
 	
 
 
 }
