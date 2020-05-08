 package com.my.thatplace;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class PlaceActivity extends Activity {
 	private final String TAG = "ThatPlace";
 	
 	// UI Resources
 	private AutoCompleteTextView txt_stn;
 	private Spinner spn_gateNum;
 	private Button btn_add;
 	private Button btn_search;
 	private AutoCompleteTextView txt_place;
 	private ListView lv;
 	
 	// Data
 	public ArrayList<String> items; 
 	ArrayAdapter<String> adapter;
 	private final String[] STATION;
 	private static String mStn, mGate, mPlace;
 	private String mCurItem;
 	private int mPos;
 	private final int DLG_DEL_ITEM = 0;
 	
 	// DB
 	public PlaceDbAdapter mDbAdapter;
 	
 	// Etc
 	InputMethodManager imm;
 
     public PlaceActivity() {
 		super();
 		// TODO Auto-generated constructor stub
		STATION = new String[] {"", "д", "", "", "", "", "", "", "û", "", "û", "", "", "", "", "", "ȭ", "ȭ", "ſ", "ǴԱ", "˾", "渶", "溹", "", "", "", "", "", "͹̳", "", "", "", "", "", "׽", "õ", "", "", "", "Ÿ", "ȭ", "â", "", "", "εд", "", "", "", "", "", "", "", "Ĺ", "", "ȭû", "ȸǻ", "", "", "", "ٸ", "ݰ", "ݸ", "", "õû", "", "ȣ", "", "浿", "", "", "", "ġ", "", "", "͹̳", "ѽŴԱ)", "", "õ", "·", "ѻ꼺Ա", "", "", "뷮", "", "", "", "", "õ", "", "ɰ", "ܴŸ", "ʸ", "", "", "", "", "븲", "Ա", "", "뼺", "߹", "û", "ġ", "ȭ", "", "", "", "", "", "", "õ", "", "", "", "", "ȭ", "", "", "", "", "빮", "빮繮ȭ", "Ա", "õ", "õ߾", "", "", "õ", "", "", "̵", "", "Ҽ", "Ҽ", "", "", "", "", "", "", "õ", "", "û", "", "", "", "ź", "԰", "", "", "", "", "", "", "伺", "", "", "", "", "̱", "̾", "̾ƻŸ", "ݿ", "", "߻", "", "", "", "ȭ", "", "鸶", "鼮", "縮", "", "Ƽ", "", "", "", "", "", "", "", "", "õ", "ȭ", "ΰ", "õ", "", "ұ", "簡", "", "縪", "", "꺻", "꼺", "ﰢ", "Ｚ", "", "", "", "ϼ", "", "", "սʸ", "", "ϵ", "õ", "", "", "빮", "ź", "", "", "Ա", "", "", "", "", "", "", "", "", "", "հ", "", "", "", "ſԱ", "ȯ", "", "", "һ", "ҿ", "۳", "", "ź", "", "", "", "", "", "", "", "", "", "Ա", "ǴԱ", "û", "Ű", "űȣ", "ű", "űõ", "ų", "Ŵ", "Ŵ", "Ŵ", "ŴŸ", "ŵ", "Ÿ", "Ÿ", "Ź", "Źȭ", "Ż", "ż", "ſ", "ſ", "̹", "", "װŸ", "â", "õ", "", "ǳ", "", "ֹ", "ֿ", "ƻ", "ƽ", "", "", "ȱ", "Ȼ", "Ⱦ", "Ⱦ", "ϻ", "б", "ֿ", "ž", "", "", "", "", "ùǽ", "", "", "õû", "õⱳ", "", "̴", "ǳ", "ǵ", "", "", "", "ų", "â", "", "û", "", "", "", "", "", "", "", "", "̵", "", "¼", "¾õ", "øȰ", "սʸ", "ܴ", "", "", "븶", "빮", "", "", "", "", "", "", "", "", "", "Ű", "", "3", "4", "Ա", "", "", "ǿ", "", "̴", "̸", "̼", "", "¿", "δ", "õ", "õ", "ϻ", "Ͽ", "", "ǳ", "", "¹", "", "", "", "߻", "ΰõû", "", "", "⵿", "", "", "3", "5", "տ", "־", "ֿ", "", "߰", "߰", "ߵ", "߶", "߾", "ȭ", "", "", "", "", "", "", "", "â", "â", "õ", "õ", "õȣ", "ö", "ûԱ", "û", "û", "û)", "û)", "û", "õ", "湫", "", "ź", "", "¸Ա", "", "", "", "Ǳ", "ȴ", "ȣ", "", "", "ǳ", "ǳ", "ϰ", "е", "п", "Ѱ", "ѳ", "Ѵ", "ѼԱ", "Ѿ", "Ƽ", "", "", "", "ȭ", "ȫԱ", "ȫ", "ȭ", "ȭ", "ȭ", "ȭ", "ȭ", "ȸ", "ȸ", "ȸ", "ȿâ", "漮", "dmc"};
 	}
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		 // Initialize widgets
 		txt_stn = (AutoCompleteTextView)findViewById(R.id.txt_stn);
 		spn_gateNum = (Spinner)findViewById(R.id.spn_gateNum);
 		btn_add = (Button)findViewById(R.id.btn_add);
 		btn_search = (Button)findViewById(R.id.btn_search);
 		txt_place = (AutoCompleteTextView)findViewById(R.id.txt_place);
 		lv = (ListView)findViewById(R.id.list_result);
 		createUiAdapter();
 		
 		// This is important!
 		items = new ArrayList<String>();
 		adapter = new ArrayAdapter<String>(
 				PlaceActivity.this, android.R.layout.simple_list_item_1, items);
 		lv.setAdapter(adapter);
 		
 		// DB
 		mDbAdapter = new PlaceDbAdapter(this);
 		mDbAdapter.open();
 		
 		/*
 		 * Event Handlers
 		 */		
 		
 	    // Click "Add"
 	    btn_add.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if ( readCurrentEditText(Util.NORMAL) == Util.NG) {
 					return;
 				}
 				mDbAdapter.createPlace(mStn, mGate, mPlace);
 				Toast.makeText(PlaceActivity.this, "߰Ϸ : " + mStn + " / " + mGate + " / " + mPlace, Toast.LENGTH_SHORT).show();
 				txt_place.setText("");
 				search();
 				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 			}
 		});
 
 	    
 	   // Click "Search"
 	    btn_search.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				search();
 				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 			}
 	    });
 	    
 		// Delete an item on the list
 		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@SuppressWarnings("deprecation")
 			@Override
 			public void onItemClick(AdapterView<?> adapterView, View v, int pos,
 					long id) {
 				// TODO Auto-generated method stub
 				mCurItem = items.get(pos);
 				if ("No result".equals(mCurItem)) {
 					return;
 				}
 				mPos = pos;
 				showDialog(DLG_DEL_ITEM);
 			}
 		});
 	    
     }
 
 	public void createUiAdapter() {
 		// Create ArrayAdapters
 		ArrayAdapter<String> adapter_stn = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, STATION);
 		ArrayAdapter<CharSequence> adapter_gateNum = ArrayAdapter.createFromResource(this,
 				R.array.GateNum, android.R.layout.simple_spinner_item);
 //		ArrayAdapter<String> adapter_place = new ArrayAdapter<String>(this,
 //				android.R.layout.simple_dropdown_item_1line, mPlaces);
 		
 		// Specify the layout to use when the list of choices appears
 		adapter_gateNum.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		
 		// Apply the adapter to the spinner
 		txt_stn.setAdapter(adapter_stn);
 		spn_gateNum.setAdapter(adapter_gateNum);
 //		txt_place.setAdapter(adapter_place);
 	}
 	
 	public int readCurrentEditText(int req) {
 		mStn = txt_stn.getText().toString();
 		mGate = spn_gateNum.getSelectedItem().toString();
 		mPlace = txt_place.getText().toString();
 		items.clear();
 		adapter.notifyDataSetChanged();
 		if (mStn.equals("") || (mPlace.equals("")) && (req == Util.NORMAL)) {
 			Toast.makeText(PlaceActivity.this, "/Ҹ Էּ", Toast.LENGTH_SHORT).show();
 			return Util.NG;
 		}
 		return Util.OK;
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     // Dialog
     @Override
     protected Dialog onCreateDialog(int id) {
     	// TODO Auto-generated method stub
     	AlertDialog dlg = null;
 
     	switch(id) {
     	case DLG_DEL_ITEM :
     		dlg = new AlertDialog.Builder(this)
     		.setIcon(R.drawable.ic_launcher)
     		.setTitle(mCurItem)
     		.setMessage(" ұ?")
     		// ư
     		.setPositiveButton("", new DialogInterface.OnClickListener() {
     			@Override
     			public void onClick(DialogInterface dialog, int which) {
     				// TODO Auto-generated method stub
     				//mWillBeDeleted = true;
     				Log.i(TAG, "Dialog. Clicked ");
     				
     				long  col_id =  -1;
     				Cursor c = null;
     				String[] tokens = {null, null, null};
     				
     				int i = 0;
     				for (String token : mCurItem.split(" / ")) {
     					tokens[i++] = token;
     				}
     				Log.i(TAG, "Extracted Tokens : " + tokens[0] + ", " + tokens[1] + ", " + tokens[2]);
     				c = mDbAdapter.fetchPlaceByStnAndPlace(tokens[0],tokens[2]);	
     				c.moveToFirst();
     				
     				// TODO : No result  Ŭ  ״ ó
     				
     				col_id = c.getLong(c.getColumnIndex("_id"));
     				if (col_id >= 0) {
     					Log.i(TAG, "Delete : " + mCurItem + " at colum id : " + col_id);
     					mDbAdapter.deletePlace(col_id);
     					items.remove(mPos);
     					adapter.notifyDataSetChanged();
     				} else {
     					Log.e(TAG, "Delete failed : " + mCurItem + ". colum id is less than zero : " + col_id);
     				}
     				c.close();
     			}
     		})
     		//  ư
     		.setNegativeButton("", new DialogInterface.OnClickListener() {
     			@Override
     			public void onClick(DialogInterface dialog, int which) {
     				// TODO Auto-generated method stub
     				Log.i(TAG, "Dialog. Clicked ");
     			}
     		})
     		// īī 
     		.setNeutralButton("", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					// Text  
 					Intent intent = new Intent(Intent.ACTION_SEND);
 					intent.setType("text/plain");
 					intent.putExtra(Intent.EXTRA_SUBJECT, "[ThatPlace]");
 					intent.putExtra(Intent.EXTRA_TEXT, mCurItem);
 					intent.setPackage("com.kakao.talk");
 					startActivity(intent);
 				}
 			})
     		.create();
     		break;
 
     	default :
     		break;
     	}
     	return dlg;    	
     }
     
     void search() {
     	String db_stn = null;
 		String db_gate = null;
 		String db_place = null;
 		Cursor c = null;
 
 		if ( readCurrentEditText(Util.ONLY_PLACE) == Util.NG) { // input information is not enough then show all places
 			Log.d(TAG, "fetchAll");
 			c = mDbAdapter.fetchAllPlaces();
 		} else {
 			if (mStn.length() != 0 && mPlace.length() == 0) { // otherwise, show corresponding places
 				Log.d(TAG, "fetchPlaceByStn : " + mStn);
 				c = mDbAdapter.fetchPlaceByStn(mStn);
 			} else if (mStn.length() == 0 && mPlace.length() != 0) {
 				Log.d(TAG, "fetchPlaceByPlace : " + mPlace);
 				c = mDbAdapter.fetchPlaceByPlace(mPlace);
 			} else {
 				Log.d(TAG, "fetchPlaceByStnAndPlace : " + mStn + ", " + mPlace);
 				c = mDbAdapter.fetchPlaceByStnAndPlace(mStn, mPlace);
 			}
 		}
 		c.moveToFirst();
 		while(!c.isAfterLast()){
 			StringBuffer sb = new StringBuffer();
 			db_stn = c.getString(1);
 			db_gate = c.getString(2);
 			db_place = c.getString(3);
 			
 			sb.append(db_stn)
 			.append(" / ")
 			.append(db_gate)
 			.append(" / ")
 			.append(db_place);
 			Log.i(TAG, "Search result : " + sb.toString());
 			items.add(sb.toString());
 
 			c.moveToNext();
 		}
 		c.close();
 		
 		if (items.isEmpty()) {
 			items.add("No result");
 		}
 		adapter.notifyDataSetChanged();
     }
     
 }
