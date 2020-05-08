 package org.theroskelleys.player_score;
 
 import android.app.*;
 import android.content.Intent;
 import android.os.*;
 import android.widget.*;
 import java.util.*;
 import android.content.*;
 import android.preference.*;
 import com.google.gson.*;
 import android.view.*;
 
 public class Settings extends ListActivity
 {
 	ArrayList<String> names;
 	SharedPreferences sp;
 	Gson g;
 	ListView lv;
 	ArrayAdapter<String> aa;
 	EditText et;
 	String nameAtPos;
 	
     @Override
     public void onCreate(Bundle savedInstanceState)
 	{
         super.onCreate(savedInstanceState);
         setContentView(R.layout.settings);
 		
 		fillHandles();
 		fillLV();
     }
     
 	private void fillHandles()
 	{
 		lv = getListView();
 		sp = PreferenceManager.getDefaultSharedPreferences(this);
 		g = new Gson();
 		et = (EditText)findViewById(R.id.et_newName);
 		names = new ArrayList<String>();
 		
 		//set click listener on the lv
 		lv.setClickable(true);
 		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3){
 				nameAtPos = (String)lv.getItemAtPosition(position);
 				AlertDialog.Builder builder = new AlertDialog.Builder(arg1.getContext());
 				builder
 					.setMessage("Are you sure you want to remove " + nameAtPos + "?")
 					.setCancelable(true)
 					.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
 						public void onClick(DialogInterface dialog, int which) {
 							names.remove(nameAtPos);
 							aa.notifyDataSetChanged();
 						}
 					})
 					.setNegativeButton("No", new DialogInterface.OnClickListener(){
 						public void onClick(DialogInterface dialog, int which) {
 						}
 					});
 				builder.create().show();
 				return true;
 			}
 		});
 		
 		//set enter listener on et
 		et.setOnKeyListener(new View.OnKeyListener(){
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent e){
 				if((e.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
 					addName();
 					return true;
 				}
 				return false;
 			}
 		});
 	}
     
     public void buttonHandler(View v)
 	{
     	switch (v.getId())
 		{
 			case R.id.btn_add:
 				addName();
 				break;
 			case R.id.btn_done:
 				//save the data in the list view into preferences
 				saveLV();
 				
 				//then load start screen
 				Intent i = new Intent(this, MainActivity.class);
 				startActivity(i);
 				break;
     	}
     }
 	
 	private void addName(){
 		//capture new name
 		String newName = et.getText().toString();
 		
 		//append to arraylist
 		names.add(newName);
 		
 		//redraw list
 		aa.notifyDataSetChanged();
 		et.setText("");
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void fillLV()
 	{
 		//read from sp
 		String jsonNames = sp.getString("names", "");
 		
 		//deserialize json
 		names = (ArrayList<String>)g.fromJson(jsonNames, ArrayList.class);
 		
		if(names == null) {
			names = new ArrayList<String>();
		}
 		//set content to arraylist
 		aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
 		setListAdapter(aa);
 	}
 	
 	private void saveLV()
 	{
 		//serialize list to json
 		String jsonNames = g.toJson(names);
 		
 		//store json into prefs
 		SharedPreferences.Editor ed = sp.edit();
 		ed.putString("names", jsonNames);
 		ed.commit();
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		fillHandles();
 		fillLV();
 	}
 	
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		saveLV();
 	}
 }
