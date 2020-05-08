 package com.praszapps.owetracker.ui.fragment;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import android.annotation.SuppressLint;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.ListFragment;
 import android.support.v7.view.ActionMode;
 import android.support.v7.view.ActionMode.Callback;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
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
 import com.praszapps.owetracker.util.Utils.DialogResponse;
 
 public class DueFragment extends ListFragment {
 
 	private View v;
 	private static Friend friend;
 	private RootActivity rAct;
 	private static TextView textViewOweSummary;
 	private static TextView emptyTextView;
 	private static EditText editTextDate;
 	private ListView listViewTransactions;
 	private static ArrayList<Due> duesList = new ArrayList<Due>();
 	private static SQLiteDatabase db;
 	private static DueAdapter dueListAdapter;
 	private Dialog d;
 	private EditText editTextAmount, editTextReason;
 	private Spinner spinnerGaveTook;
 	private Button buttonSave;
 	private Due due;
 	private static int calendarYear, calendarMonth, calendarDay;
 	private static Calendar cld = Calendar.getInstance();
 	private SharedPreferences owetrackerPrefs = RootActivity.owetrackerPrefs;
 	private DialogFragment datepicker;
 	@SuppressWarnings("unused")
 	private ActionMode mActionMode = null;
 	@SuppressLint("SimpleDateFormat")
 	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
 	private View dueItemView = null;
 	
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		//Utils.showLog(getClass().getSimpleName(), "onCreateView() starts", Log.VERBOSE);
 		
 		//Setting the View
 		v = inflater.inflate(R.layout.fragment_due, container, false);
 		//Setting the action bar
 		if(MainActivity.isSinglePane) {
 			((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 		//Initializing views and other variables
 		emptyTextView = (TextView) v.findViewById(R.id.empty_duelist);
 		textViewOweSummary = (TextView) v.findViewById(R.id.textViewOweSummary);
 		listViewTransactions = (ListView) v.findViewById(android.R.id.list);
 		listViewTransactions.setEmptyView(v.findViewById(R.id.empty_duelist));
 		listViewTransactions.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				dueItemView = view;
 				due = dueListAdapter.getItem(position);
 				mActionMode = ((MainActivity) getActivity()).startSupportActionMode(mCallback);
 				dueItemView.setSelected(true);
 				return true;
 			}
 		});
 		
 		
 		rAct = (RootActivity) getActivity();
 		db = rAct.database;
 		
 		//Getting the data and populating the listview
 		Bundle b = getArguments();
 		if(b != null) {
 			showDetails(b.getString(Constants.BUNDLE_EXTRA_FRIENDID), b.getString(Constants.BUNDLE_EXTRA_CURRENCY));
 			
 		}
 		datepicker = new DatePickerFragment();
 		//Utils.showLog(getClass().getSimpleName(), "onCreateView() ends", Log.VERBOSE);
 		return v;
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.detail_menu, menu);
 		
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			
 			if (owetrackerPrefs.getBoolean(Constants.DONT_SHOW_AGAIN, false)) {
 				//Utils.showToast(getActivity(), "Dont show again is true", Toast.LENGTH_SHORT);
 				//Go back to due list
 				getFragmentManager().popBackStack();
 	            return true;
 			} else {
 				//Utils.showToast(this, "Dont show again is false", Toast.LENGTH_SHORT);
 				long launch_count = owetrackerPrefs.getLong(Constants.LAUNCH_COUNT, 0)+1;
 			     owetrackerPrefs.edit().putLong(Constants.LAUNCH_COUNT, launch_count).commit();
 			     Long date_firstLaunch = owetrackerPrefs.getLong(Constants.DATE_FIRST_LAUNCH, 0);
 			     if (date_firstLaunch == 0) {
 			            date_firstLaunch = System.currentTimeMillis();
 			            owetrackerPrefs.edit().putLong(Constants.DATE_FIRST_LAUNCH, date_firstLaunch).commit();
 			     }
 			     
 			  // Wait at least n days before opening
 			  //Utils.showToast(getActivity(), "launch count is --- "+launch_count, Toast.LENGTH_SHORT);
 			  if (launch_count >= Constants.LAUNCHES_UNTIL_PROMPT) {
 				  //Utils.showToast(getActivity(), "It has number of launches needed to prompt", Toast.LENGTH_SHORT);
 				  //Utils.showToast(getActivity(), "Difference --- "+(date_firstLaunch - System.currentTimeMillis()), Toast.LENGTH_SHORT);
 				  if (System.currentTimeMillis() >= date_firstLaunch + 
						  (/*Constants.DAYS_UNTIL_PROMPT * 24 * 60 * */60 * 1000)) {
 					  //Utils.showToast(getActivity(), "It will show prompt", Toast.LENGTH_SHORT);
 					  Utils.showAlertDialog(getActivity(), getResources().getString(R.string.rate_dialog_title),
 							  getResources().getString(R.string.rate_dialog_msg), null, false, 
 							  getResources().getString(R.string.rate_dialog_now), 
 							  getResources().getString(R.string.rate_dialog_never), 
 							  getResources().getString(R.string.rate_dialog_later), new DialogResponse() {
 						@Override
 						public void onPositive() {
 							Utils.goToGooglePlayPage(getActivity());
 							owetrackerPrefs.edit().putBoolean(Constants.DONT_SHOW_AGAIN, true).commit();
 							//Go back to due list
 							getFragmentManager().popBackStack();
 						}
 						
 						@Override
 						public void onNeutral() {
 							owetrackerPrefs.edit().putLong(Constants.LAUNCH_COUNT, 0).commit();
 							owetrackerPrefs.edit().putLong(Constants.DATE_FIRST_LAUNCH, 
 									System.currentTimeMillis()).commit();
 							//Go back to due list
 							getFragmentManager().popBackStack();
 						}
 						
 						@Override
 						public void onNegative() {
 							owetrackerPrefs.edit().putBoolean(Constants.DONT_SHOW_AGAIN, true).commit();
 							//Go back to due list
 							getFragmentManager().popBackStack();
 						}
 						
 						
 					});
 			    	return true;
 				  } else {
 					  //Utils.showToast(getActivity(), "It still wont show prompt", Toast.LENGTH_SHORT);
 					//Go back to due list
 						getFragmentManager().popBackStack();
 			            return true;
 				  }
 			  } else {
 				  //Utils.showToast(getActivity(), "It wont show prompt", Toast.LENGTH_SHORT);
 				  //Go back to due list
 				  getFragmentManager().popBackStack();
 		            return true;
 			  }
 			}
             
 		case R.id.item_add_due:
 			showDueDialog(Constants.MODE_ADD, null);
 			if(!MainActivity.isSinglePane) {
 				new OweboardFragment().updateListView();
 			}
 			return true;
 			
 		case R.id.item_reset_due:
 			
 			if(duesList.size() == 0) {
 				// Show error if there are no dues to reset
 				Utils.showToast(getActivity(), getResources().getString(R.string.no_dues_to_reset), Toast.LENGTH_SHORT);
 			} else {
 				
 				Utils.showAlertDialog(getActivity(),  
 						getResources().getString(R.string.label_reset), 
 						getResources().getString(R.string.label_sure),
 						null, false, getResources().getString(R.string.label_yes), 
 						getResources().getString(R.string.label_no),
 						null, new Utils.DialogResponse() {
 					
 					@Override
 					public void onPositive() {
 						// Delete dues and reset due value to zero
 						DatabaseHelper.deleteAllFriendDues(friend.getId(), db);
 						updateDueList(friend.getId());
 						updateFriendSummary();
 						if(!MainActivity.isSinglePane) {
 							int dueCount = DatabaseHelper.getFriendsWithDuesCount(db);
 							if(dueCount == 0 && OweboardFragment.getFriendListAdapter().getCount() == 0) {
 								OweboardFragment.getTotalFriends().setVisibility(TextView.GONE);
 							} else {
 								OweboardFragment.getTotalFriends().setVisibility(TextView.VISIBLE);
 								OweboardFragment.getTotalFriends().setText(dueCount+"/"+OweboardFragment.getFriendListAdapter().getCount()+" "+getResources().getString(
 										R.string.label_owesactions_listview));
 							}
 							new OweboardFragment().updateListView();
 						}
 						Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_reset), Toast.LENGTH_SHORT);
 					}
 					
 					@Override
 					public void onNeutral() {
 						//Do nothing
 					}
 					
 					@Override
 					public void onNegative() {
 						//Do nothing
 					}
 					
 				});
 				return true;
 			}
 		}
 		
 		
 		
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void updateFriendSummary() {
 		friend = DatabaseHelper.getFriendData(friend.getId(), db);
 		if(friend != null) {
 			textViewOweSummary.setText(friend.toString().split(":")[1].trim());
 		}
 		
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
 			textViewOweSummary.setText(friend.toString().split(":")[1].trim());
 			duesList.clear();
 			duesList = DatabaseHelper.getFriendDueList(friendId, db);
 			if(duesList != null) {
 				// Setting the adapter
 				dueListAdapter = new DueAdapter(getActivity(), R.layout.owe_details_list_item, duesList, friend.getCurrency(), friend.getName());
 				setListAdapter(dueListAdapter);
 				((MainActivity)getActivity()).getSupportActionBar().setTitle(friend.getName());
 			}
 			
 		} else {
 			
 			//Show error message and close fragment
 			Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_friend_data_get_failure), Toast.LENGTH_SHORT);
 			getFragmentManager().popBackStack();
 		}
 	
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void showDueDialog(String opMode, final String dueId) {
 		final String mode = opMode;
 		d = new Dialog(getActivity());
 		d.setContentView(R.layout.dialog_add_due);
 		d.setTitle(getResources().getString(R.string.add_due_dialog_title));
 		editTextDate = (EditText) d.findViewById(R.id.editTextDate);
 		spinnerGaveTook = (Spinner) d.findViewById(R.id.spinnerGiveTake);
 		ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.string_array_give_take));
 		currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerGaveTook.setAdapter(currencyAdapter);
 		editTextAmount = (EditText) d.findViewById(R.id.editTextAmount);
 		editTextAmount.setHint(getResources().getString(R.string.label_hint_enter_amount)+" ("+friend.getCurrency()+")");
 		editTextReason = (EditText) d.findViewById(R.id.editTextReason);
 		editTextReason.setHint(R.string.label_hint_enter_desc);
 		buttonSave = (Button) d.findViewById(R.id.buttonSave);
 		cld = Calendar.getInstance();
 		if(mode.equals(Constants.MODE_EDIT)) {
 			editTextDate.setText(getResources().getString(R.string.label_date)+" "+due.getFormattedDate());
 			cld.setTimeInMillis(due.getDate());
 			editTextAmount.setText(Math.abs(due.getAmount())+"");		
 			if(due.getAmount() > 0 ) {	
 				spinnerGaveTook.setSelection(((ArrayAdapter<String>) spinnerGaveTook.getAdapter())
 						.getPosition(getResources().getString(R.string.array_givetake_item_gave)));
 				
 			} else {
 				spinnerGaveTook.setSelection(((ArrayAdapter<String>) spinnerGaveTook.getAdapter())
 						.getPosition(getResources().getString(R.string.array_givetake_item_took)));
 			}
 			editTextAmount.setText(Math.abs(due.getAmount())+"");
 			editTextReason.setText(due.getReason());
 		}
 		
 		editTextDate.setKeyListener(null);
 		editTextDate.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				editTextDate.setEnabled(false);
 				datepicker.show(getFragmentManager(), "datePicker");
 			}
 		});
 		
 		buttonSave.setOnClickListener(new OnClickListener() {
 			private Due addDue;
 			@Override
 			public void onClick(View v) {
 				if(editTextDate.getText().toString().equals("")) {
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
 					addDue.setDate(cld.getTimeInMillis());
 					addDue.setFriendId(friend.getId());
 					// Convert amount according to selection
 					int amt = 0;
 					if(spinnerGaveTook.getSelectedItem().toString().equals(getResources().getString(R.string.array_givetake_item_gave))) {
 						amt = Integer.parseInt(editTextAmount.getText().toString().trim());
 					} else if(spinnerGaveTook.getSelectedItem().toString().equals(getResources().getString(R.string.array_givetake_item_took))) {
 						amt = -(Integer.parseInt(editTextAmount.getText().toString().trim()));
 					}
 					
 					addDue.setAmount(amt);
 					addDue.setReason(editTextReason.getText().toString().trim());
 					Boolean success = false;
 					if(mode.equals(Constants.MODE_ADD)) {
 						addDue.setDueId(Utils.generateUniqueID());
 						success = DatabaseHelper.addDue(addDue, db);
 					} else if(mode.equals(Constants.MODE_EDIT)) {
 						if(dueId!=null) {
 							addDue.setDueId(dueId);
 						} else {
 							Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_due_add_failure), Toast.LENGTH_SHORT);
 							d.dismiss();
 							return;
 						}
 						success = DatabaseHelper.updateDue(addDue, db);
 					}
 					
 					
 					if(success) {
 						DatabaseHelper.updateFriendDue(friend.getId(), db);
 						updateDueList(friend.getId());
 						updateFriendSummary();
 						if(!MainActivity.isSinglePane) {
 							int dueCount = DatabaseHelper.getFriendsWithDuesCount(db);
 							if(dueCount == 0 && OweboardFragment.getFriendListAdapter().getCount() == 0) {
 								OweboardFragment.getTotalFriends().setVisibility(TextView.GONE);
 							} else {
 								OweboardFragment.getTotalFriends().setVisibility(TextView.VISIBLE);
 								OweboardFragment.getTotalFriends().setText(dueCount+"/"+OweboardFragment.getFriendListAdapter().getCount()+" "+getResources().getString(
 										R.string.label_owesactions_listview));
 							}
 						}
 						if(mode.equals(Constants.MODE_ADD)) {
 							Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_due_add_success), Toast.LENGTH_SHORT);
 							
 							if(RootActivity.owetrackerPrefs.getBoolean(Constants.IS_ADDING_FIRST_DUE, true)) {
 								Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_long_press), Toast.LENGTH_LONG);
 								RootActivity.owetrackerPrefs.edit().putBoolean(Constants.IS_ADDING_FIRST_DUE, false).commit();
 							}
 							
 						} else if(mode.equals(Constants.MODE_EDIT)) {
 							Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_due_update_success), Toast.LENGTH_SHORT);
 							
 						}
 						if(!MainActivity.isSinglePane) {
 							new OweboardFragment().updateListView();
 						}
 						listViewTransactions.setSelected(false);
 						d.dismiss();
 					} else {
 						Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_due_add_failure), Toast.LENGTH_SHORT);
 						listViewTransactions.setSelected(false);
 						d.dismiss();
 					}
 				}
 			}
 		});
 		d.show();
 	
 	}
 	
 	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			// Set the calendar
 			calendarYear = cld.get(Calendar.YEAR);
 			calendarMonth = cld.get(Calendar.MONTH);
 			calendarDay = cld.get(Calendar.DAY_OF_MONTH);
 			// Create a new instance of DatePickerDialog and return it
 			return new DatePickerDialog(getActivity(), this, calendarYear,
 					calendarMonth, calendarDay);
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
 				editTextDate.setText(getResources().getString(R.string.label_add_date));
 			}else{
 				calendarDay = day;
 				calendarMonth = month;
 				calendarYear = year;
 				editTextDate.setText(getResources().getString(R.string.label_date)+" "+dateFormat.format(cld.getTimeInMillis()));
 			}
 			
 			editTextDate.setEnabled(true);
 			
 		}
 	}
 	
 	
 	public static void updateSummary(Friend updateFriend) {
 		textViewOweSummary.setText(updateFriend.toString());
 	}
 	
 	public void handleFriendDelete() {
 		textViewOweSummary.setVisibility(TextView.GONE);
 		emptyTextView.setText(Constants.NO_FRIENDS);
 	}
 	
 	public static void updateDueList(String friendId) {
 		duesList = DatabaseHelper.getFriendDueList(friendId, db);
 		
 		if(dueListAdapter != null) {
 			dueListAdapter.clear();
 			dueListAdapter.setCurrency(friend.getCurrency());
 			dueListAdapter.setFriendName(friend.getName());
 			for(int i = 0; i<duesList.size(); i++) {
 				dueListAdapter.add(duesList.get(i));
 			}
 			dueListAdapter.notifyDataSetChanged();
 		}
 	}
 	
 	private android.support.v7.view.ActionMode.Callback mCallback = new Callback() {
 		
 		@Override
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 			return false;
 		}
 		
 		@Override
 		public void onDestroyActionMode(ActionMode mode) {
 			mActionMode = null;
 			dueItemView.setSelected(false);
 			mode = null;
 		}
 		
 		@Override
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 			// Inflate a menu resource providing context menu items
 	        MenuInflater inflater = mode.getMenuInflater();
 	        mode.setTitle(due.getFormattedDate());
 	        mode.setSubtitle(due.getReason());
 	        inflater.inflate(R.menu.due_context_menu, menu);
 	        return true;
 		}
 		
 		@Override
 		public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
 			
 			switch (item.getItemId()) {
 			case R.id.item_edit_due:
 				showDueDialog(Constants.MODE_EDIT, due.getDueId());
 				mode.finish();
 				break;
 			case R.id.item_delete_due:
 				Utils.showAlertDialog(
 						getActivity(),
 						getResources().getString(
 								R.string.delete_due_alert_title),
 						getResources().getString(R.string.delete_due_alert_msg),
 						null, false,
 						getResources().getString(R.string.label_yes),
 						getResources().getString(R.string.label_no), null,
 						new Utils.DialogResponse() {
 
 							@Override
 							public void onPositive() {
 								DatabaseHelper.deleteDueData(due.getDueId(), db);
 								updateDueList(friend.getId());
 								updateFriendSummary();
 								if (!MainActivity.isSinglePane) {
 									new OweboardFragment().updateListView();
 									
 									int dueCount = DatabaseHelper.getFriendsWithDuesCount(db);
 									if (dueCount == 0 && OweboardFragment.getFriendListAdapter().getCount() == 0) {
 										OweboardFragment.getTotalFriends().setVisibility(TextView.GONE);
 									} else {
 										OweboardFragment.getTotalFriends().setVisibility(TextView.VISIBLE);
 										OweboardFragment.getTotalFriends().setText(dueCount
 												+ "/"
 												+ OweboardFragment.getFriendListAdapter()
 														.getCount()
 												+ " "
 												+ getResources()
 														.getString(
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
 				mode.finish();
 				break;
 			}
 			return true;
 		}
 	};
 	
 	
 }
