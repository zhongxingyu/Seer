 /**
  * 
  */
 package org.AndroidShareApp.gui;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import org.AndroidShareApp.R;
 import org.AndroidShareApp.core.NetworkManager;
 import org.AndroidShareApp.core.Person;
 import org.AndroidShareApp.core.SharedByMeItem;
 import org.AndroidShareApp.core.SharedPerson;
 import org.openintents.intents.FileManagerIntents;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 /**
  * @author jonaias
  * 
  */
 public class SharedByMeConfigActivity extends ListActivity implements
 		OnClickListener {
 
 	private EfficientAdapter mAdapter;
 	private EditText mSharedPathEditText;
 
 	private static ArrayList<SharedByMeItem> mSharedByMeItems;
 	private static int mClickPosition;
 	private static SharedByMeItem mNewItem;
 	private static boolean mIsCreating;
 
 	private static final int REQUEST_CODE_ADD_PERSON_TO_SHARE = 1;
 	private static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY = 2;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.shared_by_me_config_activity);
 
 		mSharedByMeItems = NetworkManager.getInstance().getSharedByMeItems();
 
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			mIsCreating = false;
 			mClickPosition = extras.getInt("position");
 		} else {
 			mIsCreating = true;
 			mNewItem = new SharedByMeItem();
 			mSharedByMeItems.add(mNewItem);
 			mClickPosition = mSharedByMeItems.indexOf(mNewItem);
 		}
 
 		mAdapter = new EfficientAdapter(this);
 		setListAdapter(mAdapter);
 
 		mSharedPathEditText = (EditText) findViewById(R.id.sharedPathEditText);
 		if (mIsCreating)
 			mSharedPathEditText.setText(R.string.please_select_a_file);
 		else
 			mSharedPathEditText.setText(mSharedByMeItems.get(mClickPosition)
 					.getFullPath());
 
 		Button selectSharePathButton = (Button) findViewById(R.id.selectSharePathButton);
 		selectSharePathButton.setOnClickListener(this);
 
 		ImageButton buttonDelete = (ImageButton) findViewById(R.id.buttonDelete);
 		buttonDelete.setOnClickListener(this);
 
 		ToggleButton activateToggleButton = (ToggleButton) findViewById(R.id.activateToggleButton);
 		if (mClickPosition != -1)
 			activateToggleButton.setChecked(mSharedByMeItems
 					.get(mClickPosition).isActive());
 		activateToggleButton.setOnClickListener(this);
 
 		Button addPersonToShareButton = (Button) findViewById(R.id.addPersonToShareButton);
 		addPersonToShareButton.setOnClickListener(this);
 
 		addPersonToShareButton.setSelected(true);
 
 		Button sharedByMeBackButton = (Button) findViewById(R.id.sharedByMeBackButton);
 		sharedByMeBackButton.setOnClickListener(this);
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.selectSharePathButton) {
 			callSelectFileActivity();
 		} else if (v.getId() == R.id.activateToggleButton) {
 			ToggleButton button = (ToggleButton) v;
 			mSharedByMeItems.get(mClickPosition).setActive(button.isChecked());
 		} else if (v.getId() == R.id.addPersonToShareButton) {
 			Intent intent = new Intent(this, PersonListActivity.class);
 			startActivityForResult(intent, REQUEST_CODE_ADD_PERSON_TO_SHARE);
 		} else if (v.getId() == R.id.sharedByMeBackButton)
 			finish();
 		else if (v.getId() == R.id.buttonDelete) {
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(R.string.delete_question)
 					.setCancelable(false)
 					.setPositiveButton(R.string.yes,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									finish();
 									mSharedByMeItems.remove(mClickPosition);
 								}
 							})
 					.setNegativeButton(R.string.no,
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dialog.cancel();
 								}
 							});
 			AlertDialog alert = builder.create();
 			alert.show();
 		}
 	}
 
 	private void callSelectFileActivity() {
 		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
 		intent.putExtra(FileManagerIntents.EXTRA_TITLE,
 				R.string.file_select_activity_title);
 		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT,
 				R.string.file_select_button_label);
 
 		try {
 			startActivityForResult(intent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY);
 		} catch (ActivityNotFoundException e) {
 			// No compatible file manager was found.
 			Toast.makeText(this, R.string.cannot_find_file_manager,
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		Log.i("SharedByMeConfigActivity", "Received result with code "
 				+ requestCode);
 
 		switch (requestCode) {
 		case REQUEST_CODE_ADD_PERSON_TO_SHARE: {
 			Bundle returnedData = data.getExtras();
 			String deviceID = returnedData.getString("selectedPerson");
 			Person selectedPerson = NetworkManager.getInstance()
 					.getPersonByDeviceID(deviceID);
 			SharedPerson selectedSharedPerson = new SharedPerson(
 					selectedPerson.getName(), selectedPerson.getDeviceID(),
 					selectedPerson.getIP(), true, false);
 
 			synchronized (mSharedByMeItems) {
 				mSharedByMeItems.get(mClickPosition).managePerson(
 						selectedSharedPerson);
 			}
 		}
 			break;
 		case REQUEST_CODE_PICK_FILE_OR_DIRECTORY: {
 			if (resultCode == RESULT_OK && data != null) {
 				Uri fileUri = data.getData();
 				if (fileUri != null) {
 					String filePath = fileUri.getPath();
 					if (filePath != null) {
 						File aux = new File(filePath + "/./");
 						if (aux.exists())
 							// It is a directory.
 							filePath = filePath + "/";
 
 						if (mIsCreating) {
 							mNewItem.setFullPath(filePath);
 						} else
 							mSharedByMeItems.get(mClickPosition).setFullPath(
 									filePath);
 
 						Log.i("SharedByMeConfigActivity", "User selected path "
 								+ filePath);
 					}
 				}
 			}
 		}
 			break;
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		refreshUi();
 	}
 
 	public void refreshUi() {
 		this.runOnUiThread(new Runnable() {
 			public void run() {
 				mAdapter.notifyDataSetChanged();
 			}
 		});
 		if (mSharedByMeItems.get(mClickPosition).getFullPath() != null)
 			mSharedPathEditText.setText(mSharedByMeItems.get(mClickPosition)
 					.getFullPath());
 	}
 
 	public static class EfficientAdapter extends BaseAdapter implements
 			Filterable {
 		private LayoutInflater mInflater;
 
 		public EfficientAdapter(Context context) {
 			// Cache the LayoutInflate to avoid asking for a new one each time.
 			mInflater = LayoutInflater.from(context);
 		}
 
 		/**
 		 * Make a view to hold each row.
 		 * 
 		 * @see android.widget.ListAdapter#getView(int, android.view.View,
 		 *      android.view.ViewGroup)
 		 */
 		public View getView(final int position, View convertView,
 				ViewGroup parent) {
 
 			// A ViewHolder keeps references to children views to avoid
 			// unnecessary calls
 			// to findViewById() on each row.
 			ViewHolder holder;
 			convertView = mInflater.inflate(R.layout.shared_by_me_config_item,
 					null);
 
 			// Creates a ViewHolder and store references to the two children
 			// views
 			// we want to bind data to.
 			holder = new ViewHolder();
 			holder.peerName = (TextView) convertView
 					.findViewById(R.id.peerNameTextBox);
 			holder.canReadCheckBox = (CheckBox) convertView
 					.findViewById(R.id.canReadCheckBox);
 			holder.canWriteCheckBox = (CheckBox) convertView
 					.findViewById(R.id.canWriteCheckBox);
 
 			holder.canReadCheckBox.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					CheckBox cb = (CheckBox) v;
 					mSharedByMeItems.get(mClickPosition).getSharedPersonList()
 							.get(position).setRead(cb.isChecked());
 				}
 			});
 
 			holder.canWriteCheckBox.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					CheckBox cb = (CheckBox) v;
 					mSharedByMeItems.get(mClickPosition).getSharedPersonList()
 							.get(position).setWrite(cb.isChecked());
 				}
 			});
 
 			ArrayList<SharedPerson> sharedPersonList = mSharedByMeItems.get(
 					mClickPosition).getSharedPersonList();
 
 			holder.peerName.setText(sharedPersonList.get(position).getName());
 			holder.canReadCheckBox.setChecked(sharedPersonList.get(position)
 					.canRead());
 			holder.canWriteCheckBox.setChecked(sharedPersonList.get(position)
 					.canWrite());
 			convertView.setTag(holder);
 
 			return convertView;
 		}
 
 		static class ViewHolder {
 			TextView peerName;
 			CheckBox canReadCheckBox;
 			CheckBox canWriteCheckBox;
 		}
 
 		@Override
 		public Filter getFilter() {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public int getCount() {
 			return mSharedByMeItems.get(mClickPosition).getSharedPersonList()
 					.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return mSharedByMeItems.get(mClickPosition).getSharedPath();
 		}
 	}
 }
