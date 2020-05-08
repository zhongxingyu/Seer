 package com.example.shoppinghistory;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.Spinner;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class FragmentTab1 extends SherlockFragment {
 
 	static final String KEY_CAT = "cat";
 	static final String KEY_DATE = "date";
 	static final String KEY_PRICE = "price";
 	static final String KEY_TITLE = "title";
 	static final String KEY_SUBTITLE = "sub";
 	static final String KEY_CORNER = "corner";
 	ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();
 	Button dd;
 	int year;
 	int month;
 	int day;
 	String cat;
 	String date;
 	int price;
 	SherlockFragmentActivity parent;
 	View mView;
 	ShopListViewAdapter mAdapterList;
 	ListView lv;
 	ArrayList<Integer> idArray;
 	int selectedListItem;
 	List<ShopList> clist;
 
 	private static final int MY_DATE_DIALOG_ID = 3;
 
 	@Override
 	public SherlockFragmentActivity getSherlockActivity() {
 		return super.getSherlockActivity();
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 
 	}
 
 	@Override
 	public void setUserVisibleHint(boolean isVisibleToUser) {
 		super.setUserVisibleHint(isVisibleToUser);
 
 		if (isVisibleToUser) {
 			
 			Log.d("visiblefr1", ""+MainActivity.fr1Imp);
 			parent = getSherlockActivity();
 			if (isVisibleToUser) {
 				if (MainActivity.fr1Imp == true) {
 					MainActivity.fr1Imp = false;
 					Log.d("fragment1", "visible import");
 					refreshCurrentFragment();
 				}
 
 				setBkg();
 				// itemList.clear();
 				// mAdapterList.notifyDataSetChanged();
 
 				if (mAdapterList != null) {
 					mAdapterList.notifyDataSetChanged();
 				}
 
 				else {
 					Log.d("fr1", "kva anyjat enek a szarnak");
 				}
 			}
 
 		} else {
 			Log.d("fr1vis", "fos");
 		}
 
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// Get the view from fragmenttab2.xml
 		mView = inflater.inflate(R.layout.fragmenttab1, container, false);
 		registerForContextMenu(mView.findViewById(R.id.listitem_lv));
 		setHasOptionsMenu(true);
 		
 		setBkg();
 		/*
 		 * boolean dark_bkg = true; parent = getSherlockActivity();
 		 * setHasOptionsMenu(true);
 		 * 
 		 * 
 		 * if (parent instanceof MainActivity) { dark_bkg = ((MainActivity)
 		 * parent).dark_bkg; }
 		 * 
 		 * if (!dark_bkg) { mView.setBackgroundColor(Color.WHITE);
 		 * 
 		 * } else { mView.setBackgroundColor(Color.BLACK); }
 		 */
 		// ViewGroup vg = (ViewGroup)findViewById(R.id.vg);
 
 		dd = (Button) mView.findViewById(R.id.add_item_button);
 		lv = (ListView) mView.findViewById(R.id.listitem_lv);
 
 		idArray = new ArrayList<Integer>();
 		DatabaseHandler db = new DatabaseHandler(getSherlockActivity());
 		List<ShopList> clist = db.getAllShops();
 		itemList.clear();
 		for (ShopList s : clist) {
 			HashMap<String, String> map = new HashMap<String, String>();
 			// color =
 			map.put(KEY_TITLE, s.getTypeName());
 			map.put(KEY_SUBTITLE, s.getDate());
 			map.put(KEY_CORNER, "" + s.getPrice() + " ");
 			itemList.add(map);
 			idArray.add(s.getId());
 			Log.d("shop_id", ""+s.getId());
 		}
 		// adapter.notifyDataSetChanged();
 
 		db.close();
 
 		// ArrayList<HashMap<String, String>> labelList = new
 		// ArrayList<HashMap<String, String>>();
 
 		// final LabelListAdapter adapter = new LabelListAdapter(getActivity(),
 		// labelList);
 
 		// lv.setAdapter(adapter);
 
 		mAdapterList = new ShopListViewAdapter(getActivity(), itemList);
 		lv.setAdapter(mAdapterList);
 
 		dd.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 
 				// custom dialog
 				final Dialog dialog = new Dialog(getActivity());
 
 				dialog.setContentView(R.layout.customdialog);
 				dialog.setTitle("Add new item");
 
 				// set the custom dialog components - text, image and button
 				final EditText text_price = (EditText) dialog
 						.findViewById(R.id.editText1);
 				// text.setText("Android custom dialog example!");
 				final DatePicker dp = (DatePicker) dialog
 						.findViewById(R.id.datePicker1);
 				// dp.setCalendarViewShown(false);
 				int currentapiVersion = android.os.Build.VERSION.SDK_INT;
 				if (currentapiVersion >= 11) {
 					try {
 						Method m = dp.getClass().getMethod(
 								"setCalendarViewShown", boolean.class);
 						m.invoke(dp, false);
 					} catch (Exception e) {
 					} // eat exception in our case
 				}
 				Button dialogButton = (Button) dialog
 						.findViewById(R.id.dialogbutton);
 				// if button is clicked, close the custom dialog
 				DatabaseHandler db = new DatabaseHandler(getSherlockActivity());
 				List<String> list = new ArrayList<String>();
 				list = db.getCategories();
 				db.close();
 				String[] array_spinner;
 				int i = 0;
 				/*
 				 * for(String c: cat) { Log.d("cat", c); array_spinner[i++]=c; }
 				 */
 				final Spinner spin = (Spinner) dialog
 						.findViewById(R.id.spinner1);
 				final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
 						getActivity(),
 						android.R.layout.simple_spinner_dropdown_item, list);
 				spin.setAdapter(adapter);
 				dialogButton.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 
						if (text_price.length() > 0) {
 
 							year = dp.getYear();
 							month = dp.getMonth() + 1;
 							day = dp.getDayOfMonth();
 							// updateDisplay();
 							// Log.d("datepicker", ""+year+" "+month+" "+day);
 
 							if (month < 10 && day < 10) {
 								date = "" + year + "-0" + month + "-0" + day;
 								Log.d("datepicker1", date);
 							} else if (month < 10 && day >= 10) {
 								date = "" + year + "-0" + month + "-" + day;
 								Log.d("datepicker2", date);
 							} else if (month >= 10 && day < 10) {
 								date = "" + year + "-" + month + "-0" + day;
 								Log.d("datepicker3", date);
 							} else if (month >= 10 && day >= 10) {
 								date = "" + year + "-" + month + "-" + day;
 								Log.d("datepicker4", date);
 							}
 
 							cat = spin.getSelectedItem().toString();
 							price = Integer.parseInt(text_price.getText()
 									.toString());
 							Log.d("result", "" + date + " " + cat + " " + price);
 
 							ShopList slist = new ShopList(cat, price, date);
 							DatabaseHandler db = new DatabaseHandler(
 									getSherlockActivity());
 
 							db.addContact(slist);
 							db.close();
 
 							HashMap<String, String> map = new HashMap<String, String>();
 							// color =
 							map.put(KEY_TITLE, cat);
 							map.put(KEY_SUBTITLE, date);
 							map.put(KEY_CORNER, "" + price + " ");
 							itemList.add(map);
 							// lv.setAdapter(adapter);
 							mAdapterList.notifyDataSetChanged();
 
 							refreshLastFewElements(10);
 							dialog.dismiss();
 						}
 					}
 				});
 
 				dialog.show();
 			}
 		});
 
 		/*
 		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 
             public boolean onItemLongClick(AdapterView<?> adapterView, View view,
                     int index, long arg3) {
                  // item: "string line" that user click
                  //View v = (View)lv.getItemAtPosition(index);
                 
                  Log.d("Item has been clicked is :",  " "+ index);
                 return true;
             }
 }); */
 
 		return mView;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		setUserVisibleHint(true);
 	}
 
 	/*
 	 * public boolean onMenuItemSelected(int featureId, MenuItem item) {
 	 * DatabaseHandler db; switch (item.getItemId()) {
 	 * 
 	 * case R.id.imp_exp_btn: // write your code here Log.d("fr1", "imp");
 	 * return true;
 	 * 
 	 * case R.id.settings_btn:
 	 * 
 	 * // dark_bkg = false; Log.d("fr1", "set"); return true;
 	 * 
 	 * default: break;
 	 * 
 	 * } return true; }
 	 */
 
 	/*
 	 * 
 	 * public boolean onOptionsItemSelected(MenuItem item) { DatabaseHandler db;
 	 * switch (item.getItemId()) {
 	 * 
 	 * case R.id.settings_btn: // write your code here
 	 * 
 	 * // db = new DatabaseHandler(this); db.exportDB(); db.close();
 	 * 
 	 * Log.d("MainActivity", "import"); parent = getSherlockActivity();
 	 * 
 	 * if (parent instanceof MainActivity) { ((MainActivity) parent).dark_bkg =
 	 * false;
 	 * 
 	 * }
 	 * 
 	 * refreshFragments();
 	 * 
 	 * return true;
 	 * 
 	 * 
 	 * default: break;
 	 * 
 	 * } return super.onOptionsItemSelected(item); }
 	 */
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		DatabaseHandler db;
 		switch (item.getItemId()) {
 
 		case R.id.settings_btn:
 
 			settingsMenu();
 			return true;
 		case R.id.exp_btn:
 
 			db = new DatabaseHandler(getActivity());
 			db.exportDB();
 			db.close();
 
 			return true;
 		case R.id.imp_btn:
 
 			importMenu();
 
 			return true;
 		case R.id.last_10:
 
 			refreshLastFewElements(10);
 
 			return true;
 		case R.id.last_20:
 
 			refreshLastFewElements(20);
 
 			return true;
 		case R.id.all_items:
 
 			refreshLastFewElements(0);
 
 			return true;
 		default:
 			break;
 
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void settingsMenu() {
 		if (MainActivity.dark_bkg == true) {
 			MainActivity.dark_bkg = false;
 			mView.setBackgroundColor(Color.parseColor("#f1f1f2"));
 		} else {
 			MainActivity.dark_bkg = true;
 			mView.setBackgroundColor(Color.BLACK);
 		}
 		// itemList.clear();
 
 		mAdapterList.notifyDataSetChanged();
 	}
 
 	public void importMenu() {
 		DatabaseHandler db;
 		db = new DatabaseHandler(getActivity());
 		try {
 			db.importDB("/sdcard/shoppingManager");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		db.close();
 		MainActivity.fr2Imp = true;
 		MainActivity.fr1Imp = true;
 		Log.d("fragment1", "menu import");
 		//MainActivity.fr3Imp = true;
 		refreshCurrentFragment();
 
 	}
 
 	public void refreshCurrentFragment() {
 		
 		itemList.clear();
 		mAdapterList.notifyDataSetChanged();
 		Log.d("fr1-refresh", "refressss");
 		FragmentTab1 fragment = new FragmentTab1();
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		ft.replace(R.id.fragment1_container, fragment);
 		// ft.addToBackStack(null);
 		ft.commit();
 	}
 	/*
 	 * @Override public void onCreateOptionsMenu(Menu menu, MenuInflater
 	 * inflater) { // TODO Add your menu entries here
 	 * super.onCreateOptionsMenu(menu, inflater); inflater.inflate(R.menu.main2,
 	 * menu); }
 	 */
 	
 	@Override
 	public void onPrepareOptionsMenu(Menu menu) {
 
 		MenuItem item = menu.findItem(R.id.imp_btn);
 		MenuItem item2 = menu.findItem(R.id.exp_btn);
 		MenuItem item3 = menu.findItem(R.id.last_10);
 		MenuItem item4 = menu.findItem(R.id.last_20);
 		MenuItem item5 = menu.findItem(R.id.all_items);
 		MenuItem item6 = menu.findItem(R.id.order_daily);
 		MenuItem item7 = menu.findItem(R.id.order_weekly);
 		MenuItem item8 = menu.findItem(R.id.order_monthly);
 		MenuItem item9 = menu.findItem(R.id.back_btn);
 		item.setVisible(true);
 		item2.setVisible(true);
 		item3.setVisible(true);
 		item4.setVisible(true);
 		item5.setVisible(true);
 		item6.setVisible(false);
 		item7.setVisible(false);
 		item8.setVisible(false);
 		item9.setVisible(false);
 		super.onPrepareOptionsMenu(menu);
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 	      super.onCreateContextMenu(menu, v, menuInfo);
 	      Log.d("fr1", "nyom");
 	      if (v.getId()==R.id.listitem_lv) {
 	    	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
 	    	  selectedListItem = info.position;
 	    	  //menu.setHeaderTitle(""+info.position + " "+ idArray.get(selectedListItem));
 	    	  menu.setHeaderTitle("Menu");
 	    	  android.view.MenuInflater inflater = getActivity().getMenuInflater();
 	          inflater.inflate(R.menu.main2, menu);
 	      }
 	}
 	
 	
 	public boolean onContextItemSelected(android.view.MenuItem item) {
 		DatabaseHandler db = new DatabaseHandler(getActivity());
 		//db.deleteShopRow(idArray.get(selectedListItem));
 		if(item.getItemId() == R.id.delete_btn)
         {
             Log.d("ContextCheck","EDIT!");
             //toast = Toast.makeText(this, "Edit!", Toast.LENGTH_SHORT);
             //toast.show();
             //db.deleteContact(clist.get(selectedListItem));
            db.deleteShopRow(idArray.get(selectedListItem));
             //db.deleteContact(clist.get(selectedListItem));
         }
 		Log.d("nyomos", ""+selectedListItem + " "+ idArray.get(selectedListItem));
 		refreshLastFewElements(10);
 		db.close();
 	  return true;
 	}
 	
 	public void refreshLastFewElements(int num)
 	{
 		DatabaseHandler db = new DatabaseHandler(getSherlockActivity());
 		clist = db.getLastFewItems(num);
 		itemList.clear();
 		idArray.clear();
 		for (ShopList s : clist) {
 			HashMap<String, String> map = new HashMap<String, String>();
 			// color =
 			map.put(KEY_TITLE, s.getTypeName());
 			map.put(KEY_SUBTITLE, s.getDate());
 			map.put(KEY_CORNER, "" + s.getPrice() + " ");
 			itemList.add(map);
 			idArray.add(s.getId());
 			Log.d("idarray", ""+s.getId());
 		}
 		// adapter.notifyDataSetChanged();
 
 		db.close();
 
 		mAdapterList.notifyDataSetChanged();
 		
 		//refreshCurrentFragment();
 		
 	}
 	
 	public void setBkg()
 	{
 		if (MainActivity.dark_bkg == false && mView != null) {
 			// ((MainActivity) parent).activePage = 1;
 			mView.setBackgroundColor(Color.parseColor("#f1f1f2"));
 		} else if (MainActivity.dark_bkg == true && mView != null) {
 			mView.setBackgroundColor(Color.BLACK);
 		}
 	}
 }
