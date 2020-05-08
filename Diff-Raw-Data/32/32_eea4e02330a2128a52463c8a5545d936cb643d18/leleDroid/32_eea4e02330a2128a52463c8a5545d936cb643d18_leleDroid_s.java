 package com.vasilakos.LeleDroid;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 public class leleDroid extends Activity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 	}
 
 	public void onStart() {
 		super.onStart();
 		ListView strList = (ListView) findViewById(R.id.list);
 
 		List<Str> strs = Str.getStrList();
 		ListAdapter adapter = new StrListAdapter(this, strs,
 				android.R.layout.simple_list_item_2, new String[] {
 						Str.KEY_NAME, Str.KEY_DATE }, new int[] {
 						android.R.id.text1, android.R.id.text2 });
 
 		strList.setOnItemClickListener(new ListView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> a, View v, int i, long l) {
 				Intent view = new Intent(leleDroid.this,
 						com.vasilakos.LeleDroid.Details.class);
 				Bundle b = new Bundle();
 				b.putInt("id", i + 1);
 				view.putExtras(b);
 				startActivity(view);
 			}
 		});
 
 		strList.setAdapter(adapter);
 
 		strList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
 			public void onCreateContextMenu(ContextMenu menu, View v,
 					ContextMenu.ContextMenuInfo menuInfo) {
 				menu.add(0, 1, 0, R.string.details);
 				menu.add(0, 2, 0, R.string.chart);
 				menu.add(0, 3, 0, R.string.edit);
 				menu.add(0, 4, 0, R.string.delete);
 			}
 		});
 	}
 
 	public boolean onContextItemSelected(MenuItem item) {
 		final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
 				.getMenuInfo();
 		Bundle b = new Bundle();
 
 		switch (item.getItemId()) {
 
 			/* View Str details */
 		case 1:
 			Intent view = new Intent(this,
 					com.vasilakos.LeleDroid.Details.class);
 			b.putInt("id", menuInfo.position + 1);
 			view.putExtras(b);
 			startActivity(view);
 			return true;
 			
 			/* View Str chart dialog */
 		case 2:
 			Intent viewChart = new Intent(this,
 					com.vasilakos.LeleDroid.ChartView.class);
 			b.putInt("id", menuInfo.position + 1);
 			viewChart.putExtras(b);
 			startActivity(viewChart);
 			return true;
 
 			/* Edit Str */
 		case 3:
 			Intent edit = new Intent(this,
 					com.vasilakos.LeleDroid.Properties.class);
 			b.putInt("id", menuInfo.position + 1);
 			edit.putExtras(b);
 			startActivity(edit);
 			return true;
 
 			/* Delete Str */
 		case 4:
 			
 			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					switch (which) {
 					case DialogInterface.BUTTON_POSITIVE:
 						if (Str.deleteStrFromId(menuInfo.position + 1)) {
 							Intent intent = getIntent();
 							finish();
 							startActivity(intent);
 							return;
 						}
 						break;
 
 					case DialogInterface.BUTTON_NEGATIVE:
 						return;
 					}
 				}
 			};
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(getResources().getString(R.string.delete))
 					.setPositiveButton(R.string.ok, dialogClickListener)
 					.setMessage(getResources().getString(R.string.deleteMessage))
 					.setNegativeButton(R.string.cancel, dialogClickListener)
 					.setTitle(getResources().getString(R.string.deleteConfirmation))
 					.show();
 
 			return false;
 		}
 		return false;
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, R.string.add);
		menu.add(0, 2, 0, R.string.info);
		menu.add(0, 3, 0, R.string.donate);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case 1:
 			onAddButtonClick(null);
 			return true;
 		case 2:
 			onInfoButtonClick(null);
 			return true;
 		case 3:
 			Intent browse = new Intent(
 					Intent.ACTION_VIEW,
 					Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=M5HYBKFQYS84S&lc=GR&item_name=Donation%20to%20LeleDroid%20application&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted"));
 			startActivity(browse);
 			return true;
 		}
 		return false;
 	}
 
 	public void onAddButtonClick(View v) {
 		Intent str = new Intent(this, com.vasilakos.LeleDroid.Properties.class);
 		startActivity(str);
 	}
 
 	public void onInfoButtonClick(View v) {
 		Intent str = new Intent(this, com.vasilakos.LeleDroid.Info.class);
 		startActivity(str);
 	}
 
 }
