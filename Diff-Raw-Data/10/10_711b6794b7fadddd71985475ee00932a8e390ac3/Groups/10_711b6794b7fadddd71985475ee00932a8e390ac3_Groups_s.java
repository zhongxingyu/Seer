 package eecs.berkeley.edu.cs294;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 
 public class Groups extends Activity {
 	/** Called when the activity is first created. */
 	TableLayout tl_group_list;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		setContentView(R.layout.groups);
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
 
 		TextView tv_custom_title = (TextView) findViewById(R.id.tv_custom_title);
 		tv_custom_title.setText("Groups");
 
 		final ImageButton ib_custom_add = (ImageButton) findViewById(R.id.ib_custom_add);
 		if (ib_custom_add != null) {
 			ib_custom_add.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(v.getContext(), AddGroup.class);
 					startActivityForResult(intent, 0);
 				}
 			});
 		}
 
 		tl_group_list = (TableLayout) findViewById(R.id.tl_group_lists);
 
 		populate();
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 
 		if (resultCode == Activity.RESULT_OK) {
 			tl_group_list.removeAllViews();
 			populate();
 			
 		}
 	}
 
 	private void populate() {
 		List<String> groupnames = ToDo_Replica.dh.select_all_group_name();
 
 		for (String groupname : groupnames) {
 			TableRow row = new TableRow(this);		
 			row.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 
 			TextView tv_groupname= new TextView(this);
 			tv_groupname.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));			
 			tv_groupname.setText(groupname);
 			tv_groupname.setTextSize(30);
 
 			row.addView(tv_groupname);
 			row.setContentDescription(tv_groupname.getText());
 			row.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(v.getContext(), PreviewGroup.class);
 					intent.putExtra("group_select", v.getContentDescription());
 					startActivityForResult(intent, 3);
 				}
 			});
 			registerForContextMenu(row);
 			tl_group_list.addView(row);
 		}
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		menu.setHeaderTitle(v.getContentDescription());
		menu.add(0, Integer.parseInt(ToDo_Replica.dh.select_group("name", v.getContentDescription().toString()).get(DatabaseHelper.GROUP_ID_INDEX_G)), 0, "leave group");
		menu.add(0, Integer.parseInt(ToDo_Replica.dh.select_group("name", v.getContentDescription().toString()).get(DatabaseHelper.GROUP_ID_INDEX_G)), 1, "delete group");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem menuItem) {
 		List<String> groups = ToDo_Replica.dh.select_all_groups("g_id");
		List<String> oldEntry = ToDo_Replica.dh.select_group("g_id", 
				groups.get(menuItem.getItemId() - 1));
 		
 		switch(menuItem.getOrder()) {
 		case 0:
 			
 			if (oldEntry.get(DatabaseHelper.GROUP_ID_INDEX_G).equalsIgnoreCase("1") == false) {
 				ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 				NetworkInfo netInfo = connManager.getActiveNetworkInfo();
 
 				if(netInfo == null) {
 					Log.d("DEBUG", "--------------- No internet connection --------- ");
 				}
 
 				if (netInfo.isConnected()) {
 					ServerConnection.pushRemote(oldEntry, 
 							ServerConnection.GROUP_SERVER_UPDATE,
 							ServerConnection.UNSUBSCRIBE_REQUEST);
 				}
 			}
 			
 			ToDo_Replica.dh.delete_group("g_id", ""+menuItem.getItemId());
 			tl_group_list.removeAllViews();
 			populate();
 			return true;
 		case 1:
 			if (oldEntry.get(DatabaseHelper.GROUP_ID_INDEX_G).equalsIgnoreCase("1") == false) {
 				ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 				NetworkInfo netInfo = connManager.getActiveNetworkInfo();
 
 				if(netInfo == null) {
 					Log.d("DEBUG", "--------------- No internet connection --------- ");
 				}
 
 				if (netInfo.isConnected()) {
 					ServerConnection.pushRemote(oldEntry, 
 							ServerConnection.GROUP_SERVER_UPDATE,
 							ServerConnection.DELETE_REQUEST);
 				}
 			}
 			Log.d("DEBUG", "g_id: " + menuItem.getItemId());
 
 			ToDo_Replica.dh.delete_group("g_id", ""+menuItem.getItemId());
 			tl_group_list.removeAllViews();
 			populate();
 			return true;
 		}
 		return false;
 	};
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.layout.group_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()){
 		case R.id.m_add_group:
 			startActivityForResult(new Intent(this, AddGroup.class), 1);
 			return true;
 		}
 		return false;
 	}
 }
