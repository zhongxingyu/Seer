 package com.Grupp01.gymapp;
 
 import java.util.ArrayList;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 
 public class Ovningar extends SherlockActivity implements OnClickListener, OnItemClickListener {
 	
 	public final static String EXTRA_EXERCISE_NAME = "com.Grupp01.gymapp.message";
 	private Dialog dialog;
 	private Intent intent;
 	private ArrayList<String> listElements;
 	private ArrayAdapter<String> element_adapter;
 	private ListView listView;
 	private Button add_Button;
 	private Button cancel_Button;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.ovningar);
         dialog = new Dialog(this);
         intent = new Intent(this, AddExercise.class);
         getSupportActionBar().setHomeButtonEnabled(true);
     	dialog.setContentView(R.layout.dialog);
     	dialog.setTitle("New Exercise");
         add_Button = (Button) dialog.findViewById(R.id.add_Button);
     	add_Button.setOnClickListener(this);
     	cancel_Button = (Button) dialog.findViewById(R.id.cancel_Button);
     	cancel_Button.setOnClickListener(this);
         createListView();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getSupportMenuInflater();
     	inflater.inflate(R.menu.ovningar, menu);
         return true;
     }
     
     public void createListView()
     {
     	listView = (ListView)findViewById(R.id.theList);
     	listElements = new ArrayList<String>();
     	listElements.add("ett");
     	listElements.add("tv");
     	listElements.add("tre");
     	listElements.add("fyra");
     	listElements.add("fem");
     	listElements.add("sex");
     	listElements.add("sju");
     	element_adapter = new ArrayAdapter<String>(this, R.layout.thelist_row, listElements);
     	listView.setAdapter(element_adapter);
     	listView.setOnItemClickListener(this);
     }
 
     //lyssnar metoderna brjar hr
 	@Override
 	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
 		if(item.getItemId() == R.id.menu_addExe)
 		{
 	    	dialog.show();
 		}
 		else if(item.getItemId() == android.R.id.home)
 		{
 			//Taget frn developer.android.com
 			Intent parentActivityIntent = new Intent(this, MainActivity.class);
             parentActivityIntent.addFlags(
                     Intent.FLAG_ACTIVITY_CLEAR_TOP |
                     Intent.FLAG_ACTIVITY_NEW_TASK);
             startActivity(parentActivityIntent);
             finish();
 
 		}
 			return false;
 	}
 
 	@Override
 	public void onClick(View view)
 	{
 		if(view == add_Button)
 		{
 		intent.putExtra(EXTRA_EXERCISE_NAME, ((EditText) dialog.findViewById(R.id.exerciseName)).getText().toString());
 		dialog.dismiss();
 		startActivity(intent);
 		}
 		else if(view == cancel_Button)
 		{
 			dialog.dismiss();
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> adapt, View view, int position, long id)
 	{
 				//listElements.add("tta");
 				//element_adapter.notifyDataSetChanged();
 				String test = listView.getItemAtPosition(position).toString();
 				Intent intent = new Intent(this, View_Exercise.class);
 				intent.putExtra("test", test);
 				startActivity(intent);
 	}
 }
