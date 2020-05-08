 package edu.berkeley.cs160.theccertservice.splist;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.CheckBox;
 import android.widget.TextView;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.graphics.PorterDuff;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 
 public class ListActivity extends Activity implements SensorEventListener {
     private ListView myList;
     private static ArrayList<String> sharedLists = new ArrayList<String>();
     Spinner spinnerList;
     ShoppingList currentItems;
     ArrayList<Item> itemsArray;
     ArrayAdapter<String> listsAdapter;
     ArrayAdapter<Item> itemsAdapter;
     private Button shareButton;
     private long lastUpdate = System.currentTimeMillis();
     private SensorManager sensorManager;
     private float last_x, last_y, last_z;
     private final float SHAKE_THRESHOLD = 1600;
     private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
     
     static ListActivity mainListActivity = null;
     static Timer t;
     
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list);
 		Button button = (Button) findViewById(R.id.delete_list);
 		button.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
 		
 		spinnerList = (Spinner) findViewById(R.id.lists);
 		sharedLists = ShoppingList.allListNames();
 		listsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sharedLists);
 		spinnerList.setAdapter(listsAdapter);
 		spinnerList.setOnItemSelectedListener(new ChooseListListener());
 		mainListActivity = this;
 		
 		final class repeatTask extends TimerTask{
 
 			@Override
 			public void run() {
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("auth_token", MainActivity.authToken);
 				MainActivity.server.getSharedItems(data);
 				MainActivity.server.getFriends(data);
 				MainActivity.server.getItems(data);
 			}			
 		}
 		t = new Timer();
 		t.scheduleAtFixedRate(new repeatTask(), 0, 30000);
 		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 
 		updateItemsList();
 	}
 	
 	public void showCreateListDialog(View v) {
 	    DialogFragment newFragment = new CreateListDialog();
 	    newFragment.show(getFragmentManager(), "createList");
 	    finishText();
 	}
 	
 	public void showDeleteListDialog(View v){
 		Dialog d = deleteDialog();
 		d.show();
 	}
 	
 	public void onFinishCreateList(String listName){
 		sharedLists.add(listName);
 		currentItems = new ShoppingList(listName, MainActivity.userId);
 		spinnerList.setSelection(sharedLists.size() - 1);
 		updateItemsList();
 	}
 	
 	public Dialog deleteDialog(){
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Are you sure you want to delete this list?");
     	builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
     		public void onClick(DialogInterface dialog, int which) {
     			String chosenList = spinnerList.getSelectedItem().toString();
     			sharedLists.remove(chosenList);
     			//need to remove list from real list
     			listsAdapter.notifyDataSetChanged();
     		}
         });
     	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // User cancelled the dialog
             }
         });
     	return builder.create();
 	}
 	
 	public class CreateListDialog extends DialogFragment {
 		private EditText mEditText;
 
 		public CreateListDialog() {
 			// Empty constructor required for DialogFragment
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View view = inflater.inflate(R.layout.create_list, container);
 			mEditText = (EditText) view.findViewById(R.id.list_name);
 			Button create = (Button) view.findViewById(R.id.create_button);
 			Button cancel = (Button) view.findViewById(R.id.cancel_button);
 			create.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	ListActivity parentAct = (ListActivity) getActivity();
 	                parentAct.onFinishCreateList(mEditText.getText().toString());
 	                done();
 	            }
 	        });
 			cancel.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					done();
 				}
 			});
 			getDialog().setTitle("Create List");
 
 			return view;
 		}
 		
 		public void done() {
 			this.dismiss();
 		}
 	}
 	
 	public class ChooseListListener extends Activity implements OnItemSelectedListener {
 		
 		@Override
 		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 			String chosenList = ((Spinner) parent).getSelectedItem().toString();
 			updateItemsList();
 		}
 		
 		@Override
 		public void onNothingSelected(AdapterView<?> parent) {
 		}
 	}
 	
 	public static ArrayList<String> getLists(){
 		return sharedLists;
 	}
 	
 	public void showShareMessageDialog(View view) {
 	    DialogFragment newFragment = new ShareMessageDialog();
 	    newFragment.show(getFragmentManager(), "shareMessage");
 	}
 	
     public void onFinishShareMessageDialog(String inputText) {    	
         Toast.makeText(this, "Message Sent!", Toast.LENGTH_SHORT).show();
     }
     
     public void addItem(String name, Double itemCost, Boolean isShared) {
     	HashMap<String,String> data = new HashMap<String, String>();
     	data.put("auth_token", MainActivity.authToken);
     	data.put("owner", String.valueOf(MainActivity.userId));
     	data.put("name", name);
     	data.put("description", "");
     	data.put("price", itemCost.toString());
     	data.put("quantity", "1");
     	data.put("shared", isShared.toString());
     	data.put("shareFriends", Friend.friendsAsJSONArrayString());
     	data.put("list", currentItems._name);
     	MainActivity.server.addItem(data);
     	
     	Item newItem = new Item(name, isShared, itemCost);
     	currentItems.addItem(newItem);
     	
     	EditText nameView = (EditText) findViewById(R.id.item);
     	CheckBox checkView = (CheckBox) findViewById(R.id.checkbox_share);
     	EditText costView = (EditText) findViewById(R.id.item_cost);
     	
     	//clear edit texts and checkbox
     	nameView.setText("", TextView.BufferType.EDITABLE);
     	checkView.setChecked(false);
     	costView.setText("", TextView.BufferType.EDITABLE);
     	
     	//unfocus from edittext
     	finishText();
     	
     	updateItemsList();
     }
     
     public void addItemToList(View v){
     	if(currentItems == null){
     	    return;
     	}
     
     	EditText nameView = (EditText) findViewById(R.id.item);
     	CheckBox checkView = (CheckBox) findViewById(R.id.checkbox_share);
     	EditText costView = (EditText) findViewById(R.id.item_cost);
     	
     	String itemName = nameView.getText().toString();
     	Boolean isChecked = checkView.isChecked();
     	String costInput = costView.getText().toString();
     	Double itemCost;
     	if (costInput == null || costInput.isEmpty()){
     		itemCost = 0.0;
     	}else{
     		itemCost = Double.parseDouble(costInput);
     	}
     	
     	addItem(itemName, itemCost, isChecked.booleanValue());
     }
     
 	public void updateItemsList() {
 		if(spinnerList.getSelectedItem() != null){
 			currentItems = ShoppingList.getShoppingList(spinnerList.getSelectedItem().toString());
 		} else {
 			if (ShoppingList.hm.keySet().size() > 0) {
 				currentItems = ShoppingList.hm.get(ShoppingList.hm.keySet().toArray()[0]);
				spinnerList.setSelection(sharedLists.indexOf(currentItems._name));
				
 			}
 		}
 		ListView listview = (ListView) findViewById(R.id.item_list);
 		
 		if (currentItems != null) {
 			itemsArray = currentItems.getItems();
 			itemsAdapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, itemsArray);
 			listview.setAdapter(itemsAdapter);
 			listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 		        @Override
 		        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
 		            Item item = (Item) parent.getItemAtPosition(position);
 		            showEditItemDialog(item, itemsAdapter, currentItems);
 		            view.setAlpha(1);
 		        }
 			});
 			
 		}
 	}
 	
 	public void showEditItemDialog(Item item, ArrayAdapter adapter, ShoppingList list){
 	    DialogFragment newFragment = new EditItem(item, adapter, list);
 	    newFragment.show(getFragmentManager(), "editItem");
 	}
 	
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		long t = System.currentTimeMillis();
 		long diffTime = t - lastUpdate;
 		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && diffTime > 100) {
 			lastUpdate = t;
 			float x = event.values[0];
 			float y = event.values[1];
 			float z = event.values[2];
 
 			float accelVal = Math.abs(x+y+z - last_x - last_y - last_z)
                     / diffTime * 10000;
 			if (accelVal >= SHAKE_THRESHOLD) {
 				Log.d("Shake it", "X: " + String.valueOf(x).substring(0, 3) +
 						          " Y: " + String.valueOf(y).substring(0, 3) +
 						          " Z: " + String.valueOf(z).substring(0, 3) +
 						          " accelVal: " + String.valueOf(accelVal).substring(0, 5));
 								
 				speak();
 
 			}	
 			last_x = x;
 			last_y = y;
 			last_z = z;
 		}
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 
 	}
 	
 	public boolean checkVoiceRecognition() {
 		// Check if voice recognition is present
 		PackageManager pm = getPackageManager();
 		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
 				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
 		if (activities.size() == 0) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	public void speak() {
 
 		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
 				.getPackage().getName());
 
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 				RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
 
 		int noOfMatches = 0;
 		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
 		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (checkVoiceRecognition()) {
 			for (Sensor s : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
 				if (s.getType() == Sensor.TYPE_ACCELEROMETER) {
 					sensorManager.registerListener(this, 
 							sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 							SensorManager.SENSOR_DELAY_NORMAL);
 					return;
 				}
 			}
 		}
 		final class repeatTask extends TimerTask{
 
 			@Override
 			public void run() {
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("auth_token", MainActivity.authToken);
 				MainActivity.server.getSharedItems(data);
 				MainActivity.server.getFriends(data);
 				MainActivity.server.getItems(data);
 			}			
 		}
 		t.scheduleAtFixedRate(new repeatTask(), 0, 30000);
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		for (Sensor s : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
 			if (s.getType() == Sensor.TYPE_ACCELEROMETER) {
 				sensorManager.unregisterListener(this);
 				return;
 			}
 		}
 		t.cancel();
 	}
 	
 	private void finishText(){
     	InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
     	imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);
 	}
 	
 	public void updateListNames(){
 		sharedLists = ShoppingList.allListNames();
 		listsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sharedLists);
 		spinnerList.setAdapter(listsAdapter);
 		spinnerList.setOnItemSelectedListener(new ChooseListListener());
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater=getMenuInflater();
 	    inflater.inflate(R.menu.activity_main, menu);
 	    return super.onCreateOptionsMenu(menu);
 
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch(item.getItemId())
 	    {
 	    case R.id.logout:
 	    	//MainActivity.authToken = null;
 	    	HashMap<String, String> data = new HashMap<String, String>();
 			data.put("auth_token", MainActivity.authToken);
 	    	MainActivity.server.logout(data);
 	    	
 	    	
 	    	SharedPreferences.Editor editor = MainActivity.settings.edit();
             editor.putString("token", null);
             editor.commit();
             MainActivity.authToken = null;
             
 	    	Intent intent = new Intent(ListActivity.this, MainActivity.class);
 	    	startActivity(intent);
 	    	return true;
 	       
     	default:
             return super.onOptionsItemSelected(item);
 
 	    }
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
 
 			if (resultCode == RESULT_OK) {
 
 				ArrayList<String> text1 = data
 						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 				String str = text1.get(0);
 				List<String> text = Arrays.asList(str.split("((Add|add)\\s|\\s(too?|two|2|Too?|Two)\\s)"));
 				if (text.size() == 3 && text.get(0).length() == 0) {
 					String itemName = text.get(1);
 					String listName = text.get(2);
 					if (ShoppingList.hm.containsKey(listName) || ShoppingList.hm.containsKey(listName.toLowerCase())) {
 						currentItems = ShoppingList.getShoppingList(listName);
 						spinnerList.setSelection(sharedLists.indexOf(currentItems._name));
 					} else {
 						onFinishCreateList(listName);
 					}
 					addItem(itemName, 0.00, false);
 					showToastMessage("Added " + itemName + " to " + listName + ".");
 				} else {
 					showToastMessage(text1.toString());
 				}
 			
 			} else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR) {
 				showToastMessage("Audio Error");
 			} else if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR) {
 				showToastMessage("Client Error");
 			} else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR) {
 				showToastMessage("Network Error");
 			} else if (resultCode == RecognizerIntent.RESULT_NO_MATCH) {
 				showToastMessage("No Match");
 			} else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR) {
 				showToastMessage("Server Error");
 			}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 	
 	void showToastMessage(String message) {
 		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
 	}
 }
