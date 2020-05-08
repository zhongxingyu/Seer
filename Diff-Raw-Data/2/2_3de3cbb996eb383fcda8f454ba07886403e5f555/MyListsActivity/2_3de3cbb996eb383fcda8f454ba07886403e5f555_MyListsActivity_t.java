 package edu.Drake.babysteps;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class MyListsActivity extends ListActivity {
 
 	Button newListButton;
 	ListView lv = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_my_lists);
 
 		newListButton = (Button) findViewById(R.id.newListButton);
 		newListButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), ListSettingsActivity.class);
 				startActivity(intent);
 			}
 		});
 
 		this.lv = getListView();
 		String[] packing_lists = getResources().getStringArray(R.array.packing_lists);
		lv.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, R.id.listName, packing_lists));
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Intent intent = new Intent(getApplicationContext(), ChecklistActivity.class);
 				startActivity(intent);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.my_lists, menu);
 		return true;
 	}
 
 }
