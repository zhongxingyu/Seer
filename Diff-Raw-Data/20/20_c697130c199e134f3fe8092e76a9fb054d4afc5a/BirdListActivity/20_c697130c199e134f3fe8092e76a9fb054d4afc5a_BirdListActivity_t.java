 package com.isawabird;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.isawabird.db.DBHandler;
 import com.isawabird.parse.ParseUtils;
 import com.isawabird.parse.extra.SyncUtils;
 
 public class BirdListActivity extends Activity {
 
 	private EditText mNewListNameText;
 	private TextView mNewListCancelButton;
 	private TextView mNewListSaveButton;
 	private View mNewListView;
 	private ListView mBirdListView;
 	private ListAdapter mListAdapter;
 
 	private int deleteBirdListPosition;
 	private int checkedBirdListPosition;
	private InputMethodManager keyboardManager;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.mylists);
 
 		mBirdListView = (ListView) findViewById(R.id.mylistView);
 		mNewListSaveButton = (TextView) findViewById(R.id.btn_new_list_save);
 		mNewListCancelButton = (TextView) findViewById(R.id.btn_new_list_cancel);
 		mNewListNameText = (EditText) findViewById(R.id.editText_new_list_name);
 		mNewListView = (View) findViewById(R.id.layout_new_list);
 
		keyboardManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		mNewListCancelButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
				keyboardManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 				mNewListNameText.setText("");
 				mNewListView.setVisibility(View.GONE);
 			}
 		});
 
 		// TODO: Read more fields from user to create a new list
 		mNewListSaveButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
				keyboardManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 				String listName = mNewListNameText.getText().toString();
 				if(listName.isEmpty()) {
 					Toast.makeText(BirdListActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
 					return;
 				}
 				BirdList list = new BirdList(listName);
 				new AddBirdListAsyncTask().execute(list);
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
 		registerForContextMenu(mBirdListView);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.list_context_menu, menu);
 	}
 
 	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			switch (which){
 			case DialogInterface.BUTTON_POSITIVE:
 				if(deleteBirdListPosition == -1) return;
 				new DeleteListAsyncTask().execute(deleteBirdListPosition);
 				break;
 
 			case DialogInterface.BUTTON_NEGATIVE:
 				deleteBirdListPosition = -1;
 				break;
 			}
 		}
 	};
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		switch (item.getItemId()) {
 		case R.id.action_delete_list:
 			deleteBirdListPosition  = info.position;
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
 			.setNegativeButton("No", dialogClickListener).show();
 			return true;
 		case R.id.action_set_default:
 			BirdList list = mListAdapter.birdLists.get(info.position);
 			Utils.setCurrentList(list.getListName(), list.getId());
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.list_activity_actions, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle presses on the action bar items
 		switch (item.getItemId()) {
 		case R.id.action_add_list:
 			// show 'add new list' view
 			mNewListView.setVisibility(View.VISIBLE);
			if(mNewListNameText.requestFocus()) {
				keyboardManager.showSoftInput(mNewListNameText, 0);
			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
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
 				checkedBirdListPosition = position;
 			} else {
 				mRadioButton.setChecked(false);
 			}
 
 			mRadioButton.setOnClickListener(new OnClickListener() {
 				public void onClick(View v) {
 					Log.i(Consts.TAG, "Active Pos >>> " + position + ", previous checked: " + checkedBirdListPosition);
 					if (checkedBirdListPosition == position)
 						return;
 
 					View vMain = ((View) v.getParent());
 					View previousCheckedRow = ((ViewGroup) vMain.getParent()).getChildAt(checkedBirdListPosition);
 					RadioButton previousCheckedRadio = (RadioButton) previousCheckedRow.findViewById(R.id.radioButton_currList);
 					previousCheckedRadio.setChecked(false);
 
 					checkedBirdListPosition = position;
 					BirdList newList = birdLists.get(checkedBirdListPosition);
 					Utils.setCurrentList(newList.getListName(), newList.getId());
 				}
 			});
 			return rowView;
 		}
 	}
 
 	private class AddBirdListAsyncTask extends AsyncTask<BirdList, String, BirdList> {
 
 		@Override
 		protected BirdList doInBackground(BirdList... params) {
 			if(params == null || params.length == 0) return null;
 
 			long result = -1;
 			try {
 				result = DBHandler.getInstance(getApplicationContext()).addBirdList(params[0], true);
 			} catch (ISawABirdException e) {
 				e.printStackTrace();
 				if(e.getErrorCode() == ISawABirdException.ERR_LIST_ALREADY_EXISTS) {
 					publishProgress("List already exists. Specify a different name");
 				}
 			}
 			if(result == -1) return null;
 
 			params[0].setId(result);
 			return params[0];
 		}
 
 		@Override
 		protected void onProgressUpdate(String... values) {
 			Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_SHORT).show();
 		}
 
 		@Override
 		protected void onPostExecute(BirdList result) {
 			if(result != null) {
 				Toast.makeText(getBaseContext(), "Added new list :: " + mNewListNameText.getText(), Toast.LENGTH_SHORT).show();
 				mNewListNameText.setText("");
 				mNewListView.setVisibility(View.GONE);
 				if(mListAdapter.birdLists == null) {
 					mListAdapter.birdLists = new ArrayList<BirdList>();
 				}
 				mListAdapter.birdLists.add(0, result);
 				mListAdapter.notifyDataSetChanged();
 			}
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
 
 	private class DeleteListAsyncTask extends AsyncTask<Integer, String, Integer> {
 
 		protected Integer doInBackground(Integer... params) {
 			if(params[0] == -1) return -1;
 
 			BirdList list = mListAdapter.birdLists.get(params[0]);
 			if(list == null) return -1;

 			DBHandler dh = DBHandler.getInstance(getApplicationContext());
 			dh.deleteList(list.getId());
 			return params[0];
 		}
 
 		@Override
 		protected void onPostExecute(Integer position) {
 			int pos = position;
 			if(position != -1) {
 				mListAdapter.birdLists.remove(pos);
 				if(checkedBirdListPosition == position && mListAdapter.birdLists.size() > 0) {
 					BirdList list = mListAdapter.birdLists.get(0);
 					Utils.setCurrentList(list.getListName(), list.getId());
 				}
 				mListAdapter.notifyDataSetChanged();
 				deleteBirdListPosition = -1;
 				SyncUtils.triggerRefresh();
 			}
 		}
 	}
 }
