 package com.praszapps.owetracker.ui.fragment;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.MenuItemCompat;
 import android.support.v7.app.ActionBar;
 import android.support.v7.view.ActionMode;
 import android.support.v7.view.ActionMode.Callback;
 import android.support.v7.widget.SearchView;
 import android.support.v7.widget.SearchView.OnQueryTextListener;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.praszapps.owetracker.R;
 import com.praszapps.owetracker.adapter.FriendAdapter;
 import com.praszapps.owetracker.bo.Friend;
 import com.praszapps.owetracker.database.DatabaseHelper;
 import com.praszapps.owetracker.ui.activity.MainActivity;
 import com.praszapps.owetracker.ui.activity.RootActivity;
 import com.praszapps.owetracker.util.Constants;
 import com.praszapps.owetracker.util.Utils;
 
 public class OweboardFragment extends ListFragment {
 
 	private View v;
 	private static TextView totalFriends;
 	private TextView emptyView;
 	private ListView listViewOwelist;
 	private static FriendAdapter friendListAdapter, searchListAdapter;
 	private EditText editTextfriendName;
 	private Spinner spinnerCurrency;
 	private Button buttonSave;
 	private Dialog d;
 	private RootActivity rAct;
 	private SearchView searchView = null; 
 	private static ArrayList<Friend> friendList = null, searchList = null;
 	private static SQLiteDatabase db;
 	private OnFriendNameClickListener mFriendName;
 	private Boolean isInSearchMode = false;
 	private Friend friendData = null;
 	private ActionMode mActionMode = null;
 	private View listItemView = null;
 	private static LayoutInflater layoutInflater;
 	
 	public static TextView getTotalFriends() {
 		return totalFriends;
 	}
 
 	public static void setTotalFriends(TextView totalFriends) {
 		OweboardFragment.totalFriends = totalFriends;
 	}
 
 	public static FriendAdapter getFriendListAdapter() {
 		return friendListAdapter;
 	}
 
 	public static void setFriendListAdapter(FriendAdapter friendListAdapter) {
 		OweboardFragment.friendListAdapter = friendListAdapter;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		//Utils.showLog(getClass().getSimpleName(), "onCreateView() starts", Log.VERBOSE);
 		layoutInflater = getLayoutInflater(savedInstanceState);
 		v = inflater.inflate(R.layout.fragment_oweboard, container, false);
 		emptyView = (TextView) v.findViewById(R.id.empty_friendlist);
 		rAct = (RootActivity) getActivity();
 		db = rAct.database;
 		setTotalFriendListView();
 		totalFriends = (TextView) v.findViewById(R.id.listFriends);
 		
 		ActionBar aBar = ((MainActivity)getActivity()).getSupportActionBar();
 		setHasOptionsMenu(true);
 		aBar.setDisplayHomeAsUpEnabled(false);
 		aBar.setHomeButtonEnabled(false);
 		aBar.setTitle(R.string.oweboard_title);
 		//Utils.showLog(getClass().getSimpleName(), "onCreateView() ends", Log.VERBOSE);
 		return v;
 	}
 	
 	private void setTotalFriendListView() {
 		listViewOwelist = (ListView) v.findViewById(android.R.id.list);
 		emptyView.setText(getResources().getString(R.string.strNoRecordsFound));
 		listViewOwelist.setEmptyView(emptyView);
 		listViewOwelist.setAdapter(friendListAdapter);
 		listViewOwelist.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				listItemView = view;
 				friendData = friendListAdapter.getItem(position);
 				mActionMode = ((MainActivity) getActivity()).startSupportActionMode(mCallback);
 				listItemView.setSelected(true);
 				return true;
 			}
 		});
 		isInSearchMode = false;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		friendList = DatabaseHelper.getAllFriends(db);
 		if(friendList != null) {
 			friendListAdapter = new FriendAdapter(getActivity(), R.layout.oweboard_list_item,
 					friendList);
 			setListAdapter(friendListAdapter);
 			updateFriendCount();
 		} else {
 			updateFriendCount();
 		}
 	}
 	
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		
 		if(isInSearchMode) {
 			mFriendName.OnFriendNameClick(searchList.get(position).getId(), searchList.get(position).getCurrency());
 			searchView.clearFocus();
 		} else {
 			mFriendName.OnFriendNameClick(friendList.get(position).getId(), friendList.get(position).getCurrency());
 		}
 		
 		if(!MainActivity.isSinglePane) {
 			v.setSelected(true);
 		}
 		
 		if(mActionMode != null) {
 			mActionMode.finish();
 		}
 	}
 	
 	
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.oweboard_menu, menu);
 	
 		searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.item_search));
 	    searchView.setOnQueryTextListener(new OnQueryTextListener() {
 			
 			@Override
 			public boolean onQueryTextSubmit(String searchString) {
 				new SearchAsyncTask().execute(searchString);
 				return true;
 			}
 			
 			@Override
 			public boolean onQueryTextChange(String searchString) {
 				if(!searchString.equals("")) {
 					new SearchAsyncTask().execute(searchString);
 				} else {
 					listViewOwelist.setAdapter(friendListAdapter);
 					isInSearchMode = false;
 				}
 				return false;
 			}
 			
 		});
 	    
 	    searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
 	        @Override
 	        public void onFocusChange(View view, boolean queryTextFocused) {
 	            if(!queryTextFocused) {
 	                searchView.setQuery("", false);
 	                isInSearchMode = false;
 	                emptyView.setText(getResources().getString(R.string.strNoRecordsFound));
 	            }
 	        }
 	    });
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if(item.getItemId() == R.id.item_add_friend) {
 			//Utils.showLog(getClass().getSimpleName(), "Adding a friend", Log.VERBOSE);
 			//Show UI to add a friend
 			showFriendDialog(Constants.MODE_ADD, null);
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void updateFriendCount() {
 		int dueCount = DatabaseHelper.getFriendsWithDuesCount(db);
 		if(dueCount == 0 && friendListAdapter.getCount() == 0) {
 			totalFriends.setVisibility(TextView.GONE);
 		} else {
 			totalFriends.setVisibility(TextView.VISIBLE);
 			totalFriends.setText(dueCount+"/"+friendListAdapter.getCount()+" "+getResources().getString(
 					R.string.label_owesactions_listview));
 		}
 		 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if(MainActivity.isSinglePane) {
 			updateListView();
 		}
 		updateFriendCount();
 	}
 	
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		// This makes sure that the container activity has implemented
 		// the callback interface. If not, it throws an exception
 		try {
 			mFriendName = (OnFriendNameClickListener) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString()
 					+ " must implement OnFriendNameClickListener");
 		}
 	}
 		
 	@SuppressWarnings("unchecked")
 	private void showFriendDialog(final String modeOfOp, final Friend friendToUpdate) {
 
 		d = new Dialog(getActivity());
 		d.setContentView(R.layout.dialog_add_update_friend);
 		spinnerCurrency = (Spinner) d.findViewById(R.id.spinnerCurrency);
 		ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.string_array_currency));
 		currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerCurrency.setAdapter(currencyAdapter);
 		
 		editTextfriendName = (EditText) d.findViewById(R.id.editTextFriendName);
 		buttonSave = (Button) d.findViewById(R.id.buttonSave);
 		
 		if(modeOfOp.equals(Constants.MODE_ADD)) {
 			d.setTitle(getResources().getString(R.string.add_friend_title));
 		} else if(modeOfOp.equals(Constants.MODE_EDIT)) {
 			editTextfriendName.setText(friendToUpdate.getName());
 			d.setTitle(getResources().getString(R.string.edit_friend_dialog_title));
 			spinnerCurrency.setSelection(((ArrayAdapter<String>) spinnerCurrency.getAdapter()).getPosition(Utils.getArrayItemFromCurrency(friendToUpdate.getCurrency())));
 		}
 		
 		
 		buttonSave.setOnClickListener(new View.OnClickListener() {
 			
 			private Friend addFriend;
 
 			@Override
 			public void onClick(View v) {
 				
 				if(editTextfriendName.getText().toString().trim().equals("")|| editTextfriendName.getText().toString().trim() == null) {
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_friend_name), Toast.LENGTH_SHORT, layoutInflater);
 					return;
 				} else if(spinnerCurrency.getSelectedItem().toString().equals(getResources().getString(R.string.array_currency_item_select))) {
 					Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_friend_currency), Toast.LENGTH_SHORT, layoutInflater);
 					return;
 				} else {
 					//Add data to database
 					addFriend = new Friend();
 					if(modeOfOp.equals(Constants.MODE_ADD)) {
 						addFriend.setId(Utils.generateUniqueID());
 					} else if(modeOfOp.equals(Constants.MODE_EDIT)) {
 						addFriend.setId(friendToUpdate.getId());
 					}
 					
 					addFriend.setName(editTextfriendName.getText().toString().trim());
 					addFriend.setCurrency(Utils.getCurrencyFromArrayItem(spinnerCurrency.getSelectedItem().toString()));
 					if(modeOfOp.equals(Constants.MODE_ADD)) {
 						//Check if name is same as one's already in the list and prompt to add another one
 						
 						if(isFriendNameExist(addFriend.getName()) && friendList != null ) {
 							Utils.showAlertDialog(getActivity(), getResources().getString(R.string.alertdialog_friend_title), getResources().getString(R.string.alertdialog_addfriend_message), null, false, getResources().getString(R.string.label_yes), getResources().getString(R.string.label_no), null, new Utils.DialogResponse() {
 								
 								@Override
 								public void onPositive() {
 									if(DatabaseHelper.createFriendRecord(addFriend, db)) {
 										Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_friend_success), Toast.LENGTH_SHORT, layoutInflater);
 										d.dismiss();
 										updateListView();
 										updateFriendCount();
 									} else {
 										Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_friend_failure), Toast.LENGTH_SHORT, layoutInflater);
 									}
 								}
 								
 								@Override
 								public void onNeutral() {
 									// No response;
 									
 								}
 								
 								@Override
 								public void onNegative() {
 									Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_update_friend_negative), Toast.LENGTH_SHORT, layoutInflater);
 									return;
 								}
 							});
 						} else {
 							if(DatabaseHelper.createFriendRecord(addFriend, db)) {
 								Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_friend_success), Toast.LENGTH_SHORT, layoutInflater);
 								d.dismiss();
 								updateListView();
 								updateFriendCount();
 							} else {
 								Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_add_friend_failure), Toast.LENGTH_SHORT, layoutInflater);
 							}
 							
 						}
 						
 					} else if(modeOfOp.equals(Constants.MODE_EDIT)) {
 						if(DatabaseHelper.updateFriend(addFriend, db)) {
 							Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_update_friend_success), Toast.LENGTH_SHORT, layoutInflater);
 							
 							if(!(MainActivity.isSinglePane)) {
 								DueFragment.updateSummary(DatabaseHelper.getFriendData(addFriend.getId(), db));
 								DueFragment.updateDueList(friendToUpdate.getId());
 								((MainActivity)getActivity()).getSupportActionBar().setTitle(addFriend.getName());
 							}
 							
 							updateListView();
 							d.dismiss();
 							
 						} else {
 							Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_update_friend_failure), Toast.LENGTH_SHORT, layoutInflater);
 						}
 
 					}
 				}			
 			}
 		});
 		d.show();
 	
 	}
 	
 	public static Boolean isFriendNameExist(String friendName) {
 		if(friendList != null) {
 			for (int i = 0; i < friendList.size(); i++) {
 				if(friendList.get(i).getName().equals(friendName)) {
 					return true;
 				}
 			}
 			return false;
 		} else {
 			return false;
 		}
 	}
 
 	public void updateListView() {
 		friendList = DatabaseHelper.getAllFriends(db);
 		friendListAdapter.clear();
 		for(int i = 0; i<friendList.size(); i++) {
 			friendListAdapter.add(friendList.get(i));
 		}
 		friendListAdapter.notifyDataSetChanged();
 		
 	}
 	
 	public interface OnFriendNameClickListener {
 		public void OnFriendNameClick(String friendId, String currency);
 		
 	}
 	
 	/**
 	 * This AsyncTask shall search for a friend's name in the list
 	 * @author Prasannajeet Pani
 	 *
 	 */
 	private class SearchAsyncTask extends AsyncTask<String, Void, String> {
 		
 		@Override
 		protected void onPreExecute() {
 			searchList = new ArrayList<Friend>();
 		}
 
 		@Override
 		protected String doInBackground(String... params) {
 			
 			for(Friend friend : friendList) {
 		        if(friend.getName() != null && (friend.getName().trim().toLowerCase().startsWith(params[0].trim().toLowerCase()) 
 		        		|| friend.getName().trim().toLowerCase().contains(params[0].trim().toLowerCase()))) {
 		        	searchList.add(friend);
 		        }
 			}
 			
 			if(searchList.size() > 0) {
 				return params[0];
 			} else {
 				return null;
 			}
 		}
 		
 		@Override
 		protected void onPostExecute(String result) {
 			if(result!=null) {
 				searchListAdapter = new FriendAdapter(getActivity(), R.layout.oweboard_list_item, searchList);
 				listViewOwelist.setAdapter(searchListAdapter);
 			} else {
 				emptyView.setText(getResources().getString(R.string.strNoResults));
 				searchListAdapter = null;
 			}
 			listViewOwelist.setAdapter(searchListAdapter);
 			isInSearchMode = true;
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
 			if(MainActivity.isSinglePane) {
 				listItemView.setSelected(false);
 			}
 			mode = null;
 		}
 		
 		@Override
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 			// Inflate a menu resource providing context menu items
 	        MenuInflater inflater = mode.getMenuInflater();
 	        mode.setTitle(friendData.getName());
	        mode.setSubtitle("");
 	        inflater.inflate(R.menu.friend_context_menu, menu);
 	        return true;
 		}
 		
 		@Override
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 			switch (item.getItemId()) {
 			case R.id.item_edit_friend:
 				showFriendDialog(Constants.MODE_EDIT, friendData);
 				mode.finish();
 				break;
 			case R.id.item_delete_friend:
 				
 				Utils.showAlertDialog(getActivity(), getResources().getString(R.string.label_delete_friend), 
 						getResources().getString(R.string.label_sure), null, false, getResources().getString(R.string.label_yes), 
 						getResources().getString(R.string.label_no), null, new Utils.DialogResponse() {
 					
 					@Override
 					public void onPositive() {
 						// Delete all records of dues and friends
 						DatabaseHelper.deleteAllFriendDues(friendData.getId(), db);
 						DatabaseHelper.deleteFriendRecord(friendData.getId(), db);
 						updateListView();
 						
 						if(!MainActivity.isSinglePane) {
 							
 							DueFragment.updateDueList(friendData.getId());
 							new DueFragment().handleFriendDelete();
 							((MainActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.oweboard_title));
 							((MainActivity)getActivity()).getDueFrag().setHasOptionsMenu(false);
 						}
 						
 						int dueCount = DatabaseHelper.getFriendsWithDuesCount(db);
 						if(dueCount == 0 && OweboardFragment.friendListAdapter.getCount() == 0) {
 							totalFriends.setVisibility(TextView.GONE);
 						} else {
 							totalFriends.setVisibility(TextView.VISIBLE);
 							totalFriends.setText(dueCount+"/"+OweboardFragment.friendListAdapter.getCount()+" "+getResources().getString(
 									R.string.label_owesactions_listview));
 						}
 						Utils.showToast(getActivity(), getResources().getString(R.string.toast_msg_delete), Toast.LENGTH_SHORT, layoutInflater);
 						
 					}
 					
 					@Override
 					public void onNeutral() {
 						// No action
 					}
 					
 					@Override
 					public void onNegative() {
 						// No action
 					}
 				});
 				mode.finish();
 				break;
 			}
 			return true;
 		}
 	};
 	
 }
