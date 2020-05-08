 package com.praszapps.owetracker.ui.fragment;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.ListFragment;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.praszapps.owetracker.R;
 import com.praszapps.owetracker.adapter.DueAdapter;
 import com.praszapps.owetracker.bo.Due;
 import com.praszapps.owetracker.bo.Friend;
 import com.praszapps.owetracker.database.DatabaseHelper;
 import com.praszapps.owetracker.ui.activity.MainActivity;
 import com.praszapps.owetracker.ui.activity.RootActivity;
 import com.praszapps.owetracker.util.Constants;
 import com.praszapps.owetracker.util.Utils;
 
 public class FriendDueFragment extends ListFragment {
 
 	private View v;
 	private Friend friend, updateFriend;
 	private RootActivity rAct;
 	private TextView textViewOweSummary, emptyTextView;
 	private static TextView textViewDate;
 	private ListView listViewTransactions;
 	private ArrayList<Due> duesList = new ArrayList<Due>();
 	private SQLiteDatabase db;
 	private DueAdapter dueListAdapter;
 	private Dialog d;
 	private EditText editTextAmount, editTextReason;
 	private Spinner spinnerGaveTook;
 	private Button buttonSave;
 	private Due due;
 	private static int calendarYear, calendarMonth, calendarDay;
 	private static Calendar cld;
 	@SuppressLint("SimpleDateFormat")
 	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		//Utils.showLog(getClass().getSimpleName(), "onCreateView() starts", Log.VERBOSE);
 		
 		//Setting the View
 		v = inflater.inflate(R.layout.fragment_owe_details, container, false);
 		//Setting the action bar
 		if(MainActivity.isSinglePane) {
 			ActionBar action = getActivity().getActionBar();
 			action.setDisplayHomeAsUpEnabled(true);
 		}
 		//Initializing views and other variables
 		emptyTextView = (TextView) v.findViewById(R.id.empty_duelist);
 		textViewOweSummary = (TextView) v.findViewById(R.id.textViewOweSummary);
 		listViewTransactions = (ListView) v.findViewById(android.R.id.list);
 		listViewTransactions.setEmptyView(v.findViewById(R.id.empty_duelist));
 		rAct = (RootActivity) getActivity();
 		db = rAct.database;
 		
 		//Getting the data and populating the listview
 		Bundle b = getArguments();
 		if(b != null) {
 			showDetails(b.getString(Constants.BUNDLE_EXTRA_FRIENDID), b.getString(Constants.BUNDLE_EXTRA_CURRENCY));
 			
 		}
 		//Utils.showLog(getClass().getSimpleName(), "onCreateView() ends", Log.VERBOSE);
 		return v;
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.detail_menu, menu);
 		
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		if (v.getId()== android.R.id.list) {
 			
 		    String[] menuItems = getResources().getStringArray(R.array.array_due_item_options);
 		    for (int i = 0; i<menuItems.length; i++) {
 		      menu.add(Menu.NONE, i, i, menuItems[i]);
 		    }
 		}
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		due = dueListAdapter.getItem(info.position);
 		switch(item.getItemId()) {
 		case 0:
 			showEditDueDialog();
 			break;
 		case 1:
 			Utils.showAlertDialog(getActivity(), getResources().getString(R.string.delete_due_alert_title),
 					getResources().getString(R.string.delete_due_alert_msg), null, false, 
 					getResources().getString(R.string.label_yes), 
 					getResources().getString(R.string.label_no), null, new Utils.DialogResponse() {
 				
 				@Override
 				public void onPositive() {
 					DatabaseHelper.deleteDueData(due.getDueId(), db);
 					updateDueList();
 					updateFriendSummary();
 					if(!MainActivity.isSinglePane) {
 						int dueCount = DatabaseHelper.getFriendsWithDuesCount(db);
 						if(dueCount == 0 && OweboardFragment.friendListAdapter.getCount() == 0) {
 							OweboardFragment.totalFriends.setVisibility(TextView.GONE);
 						} else {
 							OweboardFragment.totalFriends.setVisibility(TextView.VISIBLE);
 							OweboardFragment.totalFriends.setText(dueCount+"/"+OweboardFragment.friendListAdapter.getCount()+" "+getResources().getString(
 									R.string.label_owesactions_listview));
 						}
 					}
 				}
 				
 				@Override
 				public void onNeutral() {
 					// Do nothing
 				}
 				
 				@Override
 				public void onNegative() {
 					// Do nothing
 					
 				}
 			});
 			break;
 		}
 		return super.onContextItemSelected(item);
 		
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			//Go back to due list
 			getFragmentManager().popBackStack();
             return true;
             
 		case R.id.item_add_due:
 			showAddDueDialog();
 			return true;
 			
 		case R.id.item_edit_friend:
 			showEditFriendDialog();
 			return true;
 			
 		case R.id.item_close_due:
 			Utils.showAlertDialog(getActivity(), getResources().getString(R.string.delete_reset_friend_alert_title), 
 					getResources().getString(R.string.delete_reset_friend_alert_msg), 
 					null, false, getResources().getString(R.string.label_delete), getResources().getString(R.string.label_reset),
 					getResources().getString(R.string.label_cancel), new Utils.DialogResponse() {
 				
 				@Override
 				public void onPositive() {
 					// Delete all records of dues and friends
 					DatabaseHelper.deleteAllFriendDues(friend.getId(), db);
 					DatabaseHelper.deleteFriendRecord(friend.getId(), db);
 					if(MainActivity.isSinglePane) {
 						FragmentManager fm = getFragmentManager();
 						Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_delete), Toast.LENGTH_SHORT);
 						fm.popBackStackImmediate();
 					} else {
 						updateDueList();
 						OweboardFragment.updateListView();
 						setHasOptionsMenu(false);
 						getActivity().setTitle(getResources().getString(R.string.oweboard_title));
 						int dueCount = DatabaseHelper.getFriendsWithDuesCount(db);
 						if(dueCount == 0 && OweboardFragment.friendListAdapter.getCount() == 0) {
 							OweboardFragment.totalFriends.setVisibility(TextView.GONE);
 						} else {
 							OweboardFragment.totalFriends.setVisibility(TextView.VISIBLE);
 							OweboardFragment.totalFriends.setText(dueCount+"/"+OweboardFragment.friendListAdapter.getCount()+" "+getResources().getString(
 									R.string.label_owesactions_listview));
 						}
 						textViewOweSummary.setVisibility(TextView.GONE);
 						emptyTextView.setText(getResources().getString(R.string.strNoFriendSelected));
 						Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_delete), Toast.LENGTH_SHORT);
 					}
 				}
 				
 				@Override
 				public void onNeutral() {
 					//Do nothing
 				}
 				
 				@Override
 				public void onNegative() {
 					// Delete dues and reset due value to zero
 					DatabaseHelper.deleteAllFriendDues(friend.getId(), db);
 					updateDueList();
 					updateFriendSummary();
 					if(!MainActivity.isSinglePane) {
 						int dueCount = DatabaseHelper.getFriendsWithDuesCount(db);
 						if(dueCount == 0 && OweboardFragment.friendListAdapter.getCount() == 0) {
 							OweboardFragment.totalFriends.setVisibility(TextView.GONE);
 						} else {
 							OweboardFragment.totalFriends.setVisibility(TextView.VISIBLE);
 							OweboardFragment.totalFriends.setText(dueCount+"/"+OweboardFragment.friendListAdapter.getCount()+" "+getResources().getString(
 									R.string.label_owesactions_listview));
 						}
 					}
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_reset), Toast.LENGTH_SHORT);
 				}
 				
 			});
 			return true;
 			
 		}
 		
 		
 		
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void updateFriendSummary() {
 		friend = DatabaseHelper.getFriendData(friend.getId(), db);
 		textViewOweSummary.setText(friend.toString());
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		registerForContextMenu(getListView());
 	}
 
 	public void showDetails(String friendId, String currency) {
 		setHasOptionsMenu(true);
 		textViewOweSummary.setVisibility(TextView.VISIBLE);
 		emptyTextView.setText(getResources().getString(R.string.strNoDueRecordsFound));
 		friend = DatabaseHelper.getFriendData(friendId, db);
 		if(friend != null) {
 			textViewOweSummary.setText(friend.toString());
 			duesList.clear();
 			duesList = DatabaseHelper.getFriendDueList(friendId, db);
 			if(duesList != null) {
 				// Setting the adapter
 				dueListAdapter = new DueAdapter(getActivity(), R.layout.owe_details_list_item, duesList, friend.formatCurrency(friend.getCurrency()), friend.getName());
 				setListAdapter(dueListAdapter);
 				getActivity().setTitle(friend.getName());
 			}
 			
 		} else {
 			
 			//Show error message and close fragment
 			Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_friend_data_get_failure), Toast.LENGTH_SHORT);
 			getFragmentManager().popBackStack();
 		}
 	
 	}
 	
 	private void showAddDueDialog() {
 		d = new Dialog(getActivity());
 		d.setContentView(R.layout.dialog_add_due);
 		d.setTitle(getResources().getString(R.string.add_due_dialog_title));
 		textViewDate = (TextView) d.findViewById(R.id.textViewDate);
 		spinnerGaveTook = (Spinner) d.findViewById(R.id.spinnerGiveTake);
 		ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.string_array_give_take));
 		currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerGaveTook.setAdapter(currencyAdapter);
 		editTextAmount = (EditText) d.findViewById(R.id.editTextAmount);
 		editTextAmount.setHint(R.string.label_hint_enter_amount);
 		editTextReason = (EditText) d.findViewById(R.id.editTextReason);
 		editTextReason.setHint(R.string.label_hint_enter_desc);
 		buttonSave = (Button) d.findViewById(R.id.buttonSave);
 		
 		textViewDate.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				DialogFragment datepicker = new DatePickerFragment();
 				datepicker.show(getFragmentManager(), "datePicker");
 				
 			}
 		});
 		
 		buttonSave.setOnClickListener(new OnClickListener() {
 			private Due addDue;
 			@Override
 			public void onClick(View v) {
 				if(textViewDate.getText().toString().equals(getResources().getString(R.string.label_add_date))) {
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_due_date), Toast.LENGTH_SHORT);
 					return;
 				} else if(spinnerGaveTook.getSelectedItem().toString().equals(getResources().getString(R.string.array_givetake_item_select))) {
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_due_givetake), Toast.LENGTH_SHORT);
 					return;
 				} else if(editTextAmount.getText().toString().equals("") || editTextAmount.getText().toString() == null) {
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_due_amount), Toast.LENGTH_SHORT);
 					return;
 				} else if(editTextReason.getText().toString().equals("") || editTextReason.getText().toString() == null) {
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_due_reason), Toast.LENGTH_SHORT);
 					return;
 				} else {
 					addDue = new Due();
 					addDue.setDueId(Utils.generateUniqueID());
 					addDue.setFriendId(friend.getId());
 					addDue.setDate(cld.getTimeInMillis());
 										
 					// Convert amount according to selection
 					int amt = 0;
 					if(spinnerGaveTook.getSelectedItem().toString().equals(getResources().getString(R.string.array_givetake_item_gave))) {
 						amt = Integer.parseInt(editTextAmount.getText().toString().trim());
 					} else if(spinnerGaveTook.getSelectedItem().toString().equals(getResources().getString(R.string.array_givetake_item_took))) {
 						amt = -(Integer.parseInt(editTextAmount.getText().toString().trim()));
 					}
 					
 					addDue.setAmount(amt);
 					addDue.setReason(editTextReason.getText().toString().trim());
 					
 					if(DatabaseHelper.addDue(addDue, db)) {
 						Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_due_add_success), Toast.LENGTH_SHORT);
 						DatabaseHelper.updateFriendDue(friend.getId(), db);
 						updateDueList();
 						updateFriendSummary();
 						if(!MainActivity.isSinglePane) {
 							int dueCount = DatabaseHelper.getFriendsWithDuesCount(db);
 							if(dueCount == 0 && OweboardFragment.friendListAdapter.getCount() == 0) {
 								OweboardFragment.totalFriends.setVisibility(TextView.GONE);
 							} else {
 								OweboardFragment.totalFriends.setVisibility(TextView.VISIBLE);
 								OweboardFragment.totalFriends.setText(dueCount+"/"+OweboardFragment.friendListAdapter.getCount()+" "+getResources().getString(
 										R.string.label_owesactions_listview));
 							}
 						}
 						d.dismiss();
 					} else {
 						Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_due_add_failure), Toast.LENGTH_SHORT);
 						d.dismiss();
 					}
 					return;
 				}
 			}
 		});
 		d.show();
 	
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void showEditFriendDialog() {
 		d = new Dialog(getActivity());
 		d.setContentView(R.layout.dialog_add__update_friend);
 		d.setTitle(getResources().getString(R.string.edit_friend_dialog_title));
 		final Spinner spinnerCurrency = (Spinner) d.findViewById(R.id.spinnerCurrency);
 		ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.string_array_currency));
 		currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerCurrency.setAdapter(currencyAdapter);
 		spinnerCurrency.setSelection(((ArrayAdapter<String>) spinnerCurrency.getAdapter()).getPosition(friend.getCurrency()));
 		final EditText editTextfriendName = (EditText) d.findViewById(R.id.editTextFriendName);
 		editTextfriendName.setText(friend.getName());
 		Button buttonSave = (Button) d.findViewById(R.id.buttonSave);
 		buttonSave.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 
 				if(editTextfriendName.getText().toString().trim().equals("")|| editTextfriendName.getText().toString().trim() == null) {
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_friend_name), Toast.LENGTH_SHORT);
 					return;
 				} else if(spinnerCurrency.getSelectedItem().toString().equals(getResources().getString(R.string.array_currency_item_select))) {
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_friend_currency), Toast.LENGTH_SHORT);
 					return;
 				} else {
 					//Add data to database
 					updateFriend = new Friend();
 					updateFriend.setId(friend.getId());
 					updateFriend.setName(editTextfriendName.getText().toString().trim());
 					updateFriend.setCurrency(spinnerCurrency.getSelectedItem().toString());
 					
 						if(DatabaseHelper.updateFriend(updateFriend, db)) {
 							Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_update_friend_success), Toast.LENGTH_SHORT);
 							
 							friend = DatabaseHelper.getFriendData(friend.getId(), db);
 							textViewOweSummary.setText(friend.toString());
 							
 							if(!MainActivity.isSinglePane) {
 								OweboardFragment.updateListView();
 							}
 							updateDueList();
 							d.dismiss();
 							
 						} else {
 							Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_update_friend_failure), Toast.LENGTH_SHORT);
 						}
 						getActivity().setTitle(friend.getName());
 					}
 				}
 		});
 		d.show();
 	}
 	
 	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			// Use the current date as the default date in the picker
 			if(!textViewDate.getText().toString().contains("/")) {
 				final Calendar c = Calendar.getInstance();
 				calendarYear = c.get(Calendar.YEAR);
 				calendarMonth = c.get(Calendar.MONTH);
 				calendarDay = c.get(Calendar.DAY_OF_MONTH);
 			} else {
 				calendarYear = cld.get(Calendar.YEAR);
 				calendarMonth = cld.get(Calendar.MONTH);
 				calendarDay = cld.get(Calendar.DAY_OF_MONTH);
 			}
 			// Create a new instance of DatePickerDialog and return it
 			return new DatePickerDialog(getActivity(), this, calendarYear, calendarMonth, calendarDay);
 		}
 
 		public void onDateSet(DatePicker view, int year, int month, int day) {
 			// Do something with the date chosen by the user
 			
 			cld = Calendar.getInstance();
 			cld.set(Calendar.YEAR, year);
 			cld.set(Calendar.MONTH, month);
 			cld.set(Calendar.DATE, day);
 			cld.set(Calendar.HOUR, 0);
 			cld.set(Calendar.MINUTE, 0);
 			cld.set(Calendar.SECOND, 0);
 			cld.set(Calendar.MILLISECOND, 0);
 			if (cld.getTime().after(new Date())){
 				Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_due_invalid_date), Toast.LENGTH_SHORT);
				textViewDate.setText(getResources().getString(R.string.label_click_add_date));
 			}else{
 				calendarDay = day;
 				calendarMonth = month;
 				calendarYear = year;
 				textViewDate.setText("Date: "+dateFormat.format(cld.getTimeInMillis()));
 			}
 			
 			
 			
 		}
 	}
 	
 	@SuppressWarnings({ "unchecked" })
 	private  void showEditDueDialog() {
 		d = new Dialog(getActivity());
 		d.setContentView(R.layout.dialog_add_due);
 		d.setTitle(getResources().getString(R.string.edit_due_dialog_title));
 		textViewDate = (TextView) d.findViewById(R.id.textViewDate);
 		textViewDate.setText(getResources().getString(R.string.label_date)+" "+due.getFormattedDate());
 		spinnerGaveTook = (Spinner) d.findViewById(R.id.spinnerGiveTake);
 		ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.string_array_give_take));
 		currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerGaveTook.setAdapter(currencyAdapter);
 		
 		
 		editTextAmount = (EditText) d.findViewById(R.id.editTextAmount);
 		editTextAmount.setHint(R.string.label_hint_enter_amount);
 		if(due.getAmount() > 0 ) {	
 			spinnerGaveTook.setSelection(((ArrayAdapter<String>) spinnerGaveTook.getAdapter())
 					.getPosition(getResources().getString(R.string.array_givetake_item_gave)));
 			
 		} else {
 			spinnerGaveTook.setSelection(((ArrayAdapter<String>) spinnerGaveTook.getAdapter())
 					.getPosition(getResources().getString(R.string.array_givetake_item_took)));
 		}
 		editTextAmount.setText(Math.abs(due.getAmount())+"");
 		editTextReason = (EditText) d.findViewById(R.id.editTextReason);
 		editTextReason.setHint(R.string.label_hint_enter_desc);
 		editTextReason.setText(due.getReason());
 		buttonSave = (Button) d.findViewById(R.id.buttonSave);
 		
 		textViewDate.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				DialogFragment datepicker = new DatePickerFragment();
 				datepicker.show(getFragmentManager(), "datePicker");
 				
 			}
 		});
 		
 		buttonSave.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if(textViewDate.getText().toString().equals(getResources().getString(R.string.label_add_date))) {
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_due_date), Toast.LENGTH_SHORT);
 					return;
 				} else if(spinnerGaveTook.getSelectedItem().toString().equals(getResources().getString(R.string.array_givetake_item_select))) {
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_due_givetake), Toast.LENGTH_SHORT);
 					return;
 				} else if(editTextAmount.getText().toString().equals("") || editTextAmount.getText().toString() == null) {
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_due_amount), Toast.LENGTH_SHORT);
 					return;
 				} else if(editTextReason.getText().toString().equals("") || editTextReason.getText().toString() == null) {
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_due_reason), Toast.LENGTH_SHORT);
 					return;
 				} else {										
 					// Convert amount according to selection
 					int amt = 0;
 					if(spinnerGaveTook.getSelectedItem().toString().equals(getResources().getString(R.string.array_givetake_item_gave))) {
 						amt = Integer.parseInt(editTextAmount.getText().toString().trim());
 					} else if(spinnerGaveTook.getSelectedItem().toString().equals(getResources().getString(R.string.array_givetake_item_took))) {
 						amt = -(Integer.parseInt(editTextAmount.getText().toString().trim()));
 					}
 
 					if(cld != null) {
 						due.setDate(cld.getTimeInMillis());
 					}
 					due.setAmount(amt);
 					due.setReason(editTextReason.getText().toString().trim());
 					
 					if(DatabaseHelper.updateDue(due, db)) {
 						Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_due_update_success), Toast.LENGTH_SHORT);
 						//DatabaseHelper.updateFriendDue(friend.getId(), db);
 						updateDueList();
 						updateFriendSummary();
 						if(!MainActivity.isSinglePane) {
 							int dueCount = DatabaseHelper.getFriendsWithDuesCount(db);
 							if(dueCount == 0 && OweboardFragment.friendListAdapter.getCount() == 0) {
 								OweboardFragment.totalFriends.setVisibility(TextView.GONE);
 							} else {
 								OweboardFragment.totalFriends.setVisibility(TextView.VISIBLE);
 								OweboardFragment.totalFriends.setText(dueCount+"/"+OweboardFragment.friendListAdapter.getCount()+" "+getResources().getString(
 										R.string.label_owesactions_listview));
 							}
 						}
 						d.dismiss();
 					} else {
 						Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_due_add_failure), Toast.LENGTH_SHORT);
 						d.dismiss();
 					}
 					due = null;
 					return;
 				}
 			}
 		});
 		d.show();
 	
 	
 		
 	}
 	
 	private void updateDueList() {
 		OweboardFragment.updateListView();
 		duesList = DatabaseHelper.getFriendDueList(friend.getId(), db);
 		dueListAdapter.clear();
 		dueListAdapter.setCurrency(friend.formatCurrency(friend.getCurrency()));
 		dueListAdapter.setFriendName(friend.getName());
 		dueListAdapter.addAll(duesList);
 		dueListAdapter.notifyDataSetChanged();
 	}
 	
 	
 }
