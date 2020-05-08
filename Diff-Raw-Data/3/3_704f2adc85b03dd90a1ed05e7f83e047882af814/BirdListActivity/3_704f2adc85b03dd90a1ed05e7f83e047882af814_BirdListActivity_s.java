 package com.isawabird;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.isawabird.db.DBHandler;
 import com.isawabird.parse.ParseUtils;
 import com.isawabird.utilities.PostUndoAction;
 import com.isawabird.utilities.SwipeDismissListViewTouchListener;
 import com.isawabird.utilities.UndoBarController;
 import com.isawabird.utilities.UndoBarController.UndoListener;
 
 public class BirdListActivity extends Activity {
 
 	private TextView mAddNewButton;
 	private EditText mNewListNameText;
 	private TextView mNewListCancelButton;
 	private TextView mNewListSaveButton;
 	private View mNewListView;
 	private ListView mBirdListView;
 	private ListAdapter mListAdapter;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.mylists);
 
 		mBirdListView = (ListView) findViewById(R.id.mylistView);
 		mAddNewButton = (TextView) findViewById(R.id.btn_add_new_list);
 		mNewListSaveButton = (TextView) findViewById(R.id.btn_new_list_save);
 		mNewListCancelButton = (TextView) findViewById(R.id.btn_new_list_cancel);
 		mNewListNameText = (EditText) findViewById(R.id.editText_new_list_name);
 		mNewListView = (View) findViewById(R.id.layout_new_list);
 
 		mAddNewButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				mNewListView.setVisibility(View.VISIBLE);
 				mAddNewButton.setVisibility(View.INVISIBLE);
 			}
 		});
 
 		final InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		mNewListCancelButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				keyboard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 				mNewListNameText.setText("");
 				mNewListView.setVisibility(View.GONE);
 				mAddNewButton.setVisibility(View.VISIBLE);
 			}
 		});
 
 		// TODO: Read more fields from user to create a new list
 		mNewListSaveButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				keyboard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 				// dh.addBirdList(mNewListNameText.getText(), true);
 				BirdList list = new BirdList(mNewListNameText.getText().toString());
 				try {
 					DBHandler.getInstance(getApplicationContext()).addBirdList(list, true);
 				} catch (ISawABirdException ex) {
 					// TODO : Specify a proper error code if list already exists
 					Toast.makeText(getApplicationContext(), "List already exists. Specify a different name", Toast.LENGTH_SHORT);
 				}
 				Toast.makeText(getBaseContext(), "Added new list :: " + mNewListNameText.getText(), Toast.LENGTH_SHORT).show();
 				mNewListNameText.setText("");
 				mNewListView.setVisibility(View.INVISIBLE);
 			}
 		});
 
 		mListAdapter = new ListAdapter(this, null);
 		mBirdListView.setAdapter(mListAdapter);
 		mBirdListView.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 
 				Bundle b = new Bundle();
 				b.putString("listName", mListAdapter.birdLists.get(position).getListName());
 
 				Intent mySightingIntent = new Intent(getApplicationContext(), SightingsActivity.class);
 				mySightingIntent.putExtras(b);
 				startActivity(mySightingIntent);
 			}
 		});
 
 		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(mBirdListView,
 				new SwipeDismissListViewTouchListener.DismissCallbacks() {
 					@Override
 					public boolean canDismiss(int position) {
 						return true;
 					}
 
 					@Override
 					public void onDismiss(ListView listView, int[] reverseSortedPositions) {
 						for (final int position : reverseSortedPositions) {
 							final BirdList listToRemove = mListAdapter.birdLists.get(position);
 							final String listNameToRemove = listToRemove.getListName();
 
 							PostUndoAction action = new PostUndoAction() {
 								@Override
 								public void action() {
 									DBHandler.getInstance(getApplicationContext()).deleteList(listNameToRemove);
 								}
 							};
 							UndoBarController.show(BirdListActivity.this, listNameToRemove + " removed from the list", new UndoListener() {
 
 								@Override
 								public void onUndo(Parcelable token) {
 									mListAdapter.birdLists.add(position, listToRemove);
 									mListAdapter.notifyDataSetChanged();
 									// TODO Handle case when data has been uploaded to
 									// parse and when it has not been uploaded to parse.
 								}
 							}, action);
 							mListAdapter.birdLists.remove(position);
 						}
 						mListAdapter.notifyDataSetChanged();
 					}
 				});
 		mBirdListView.setOnTouchListener(touchListener);
 		mBirdListView.setOnScrollListener(touchListener.makeScrollListener());
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		new QueryBirdListAsyncTask().execute();
 	}
 
 	public class ListAdapter extends ArrayAdapter<BirdList> {
 
 		private TextView mListNameText;
 		private RadioButton mRadioButton;
 
 		public ArrayList<BirdList> birdLists;
 		private int checkedRowPosition;
 
 		public ListAdapter(Context context, ArrayList<BirdList> birdLists) {
 			super(context, R.layout.mylists_row, birdLists);
 			this.birdLists = birdLists;
 		}
 
 		@Override
 		public int getCount() {
 			if (birdLists == null)
 				return 0;
 			return birdLists.size();
 		}
 
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent) {
 
 			View rowView = convertView;
 
 			if (rowView == null) {
 				LayoutInflater inflater = getLayoutInflater();
 				rowView = inflater.inflate(R.layout.mylists_row, parent, false);
 			}
 
 			mListNameText = (TextView) rowView.findViewById(R.id.mylistsItem_name);
 			mListNameText.setTypeface(Utils.getOpenSansLightTypeface(BirdListActivity.this));
 			mRadioButton = (RadioButton) rowView.findViewById(R.id.radioButton_currList);
 
 			mListNameText.setText(birdLists.get(position).getListName());
 
 			if (birdLists.get(position).getId() == Utils.getCurrentListID()) {
 				mRadioButton.setChecked(true);
 				checkedRowPosition = position;
 			} else {
 				mRadioButton.setChecked(false);
 			}
 
 			mRadioButton.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					Log.i(Consts.TAG, "Active Pos >>> " + position + ", previous checked: " + checkedRowPosition);
 					if (checkedRowPosition == position)
 						return;
 
 					View vMain = ((View) v.getParent());
 					View previousCheckedRow = ((ViewGroup) vMain.getParent()).getChildAt(checkedRowPosition);
 					RadioButton previousCheckedRadio = (RadioButton) previousCheckedRow.findViewById(R.id.radioButton_currList);
 					previousCheckedRadio.setChecked(false);
 
 					checkedRowPosition = position;
 					BirdList newList = birdLists.get(checkedRowPosition);
 					Utils.setCurrentList(newList.getListName(), newList.getId());
 				}
 			});
 			return rowView;
 		}
 	}
 
 	private class QueryBirdListAsyncTask extends AsyncTask<Void, Void, ArrayList<BirdList>> {
 
 		protected ArrayList<BirdList> doInBackground(Void... params) {
 
 			DBHandler dh = DBHandler.getInstance(getApplicationContext());
 			dh = DBHandler.getInstance(getApplicationContext());
 			return dh.getBirdLists(ParseUtils.getCurrentUsername());
 		}
 
 		protected void onPostExecute(ArrayList<BirdList> result) {
 
 			if (result == null || result.size() == 0) {
 				return;
 			}
 			Log.i(Consts.TAG, "List count: " + result.size());
 			mListAdapter.birdLists = result;
 			mListAdapter.notifyDataSetChanged();
 		}
 	}
 }
