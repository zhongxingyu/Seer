 package com.dbstar.settings;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.BaseAdapter;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.dbstar.settings.display.OutputSetConfirm;
 import com.dbstar.settings.utils.DisplaySettings;
 import com.dbstar.settings.utils.SettingsCommon;
 import com.dbstar.settings.utils.SoundSettings;
 import com.dbstar.settings.utils.Utils;
 
 public class GDMultimediaSettingsActivity extends GDBaseActivity {
 	private static final String TAG = "GDMultimediaSettingsActivity";
 
 	private ListView mVideoOutputView, mAudioOutputView;
 	private ListAdapter mVideoOutputModeAdapter, mAudioOutputModeAdapter;
 	private String[] mVideoModeEntries;
 	private String[] mAudioModeEntries, mAudioModeEntriesStr, mAudioModeValues;
 	ArrayList<OutputMode> mVideoModes = new ArrayList<OutputMode>();
 	ArrayList<OutputMode> mAudioModes = new ArrayList<OutputMode>();
 
 	private String[] mCVBSValues;
 	private int mCVBSIndex;
 	boolean mHasCVBSOutput;
 
 	private static final int DefaultFrequecy50Hz = 0;
 	private static final int DefaultFrequecy60Hz = 1;
 	boolean mHasDefaultFrequency;
 	String mDefaultFrequency;
 	private CharSequence[] mDefaultFrequencyEntries;
 
 	OutputMode mDefaultVideoMode;
 
 	int mVideoSelectedMode = -1;
 	int mVideoLastSelectedMode = -1;
 
 	int mLastSelectedItem = -1;
 	int mSelectedItem = -1;
 	View mLastSelectedView = null, mSelectedView = null;
 
 	int mAudioSelectedMode = -1;
 
 	int mAudioLastSelectedItem = -1;
 	int mAudioSelectedItem = -1;
 	View mAudioLastSelectedView = null, mAudioSelectedView = null;
 
 	class OutputMode {
 		public String modeStr;
 		public String modeValue;
 		public String frequecy;
 		public boolean isSelected;
 
 		public OutputMode() {
 			modeStr = "";
 			modeValue = "";
 			frequecy = "";
 			isSelected = false;
 		}
 
 		public boolean equals(OutputMode other) {
 			return modeValue.equalsIgnoreCase(other.modeValue)
 					&& frequecy.equalsIgnoreCase(other.frequecy);
 		}
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.multimedia_settings);
 
 		initializeView();
 
 		// Intent intent = getIntent();
 		// mMenuPath = intent.getStringExtra(INTENT_KEY_MENUPATH);
 		// showMenuPath(mMenuPath.split(MENU_STRING_DELIMITER));
 
 		String audioMode = SoundSettings.getAudioOutputMode();
 		if (!audioMode.isEmpty()) {
 
 			for (int i = 0; i < mAudioModes.size(); i++) {
 				OutputMode aMode = mAudioModes.get(i);
 				if (aMode.modeValue.equalsIgnoreCase(audioMode)) {
 					aMode.isSelected = true;
 					mAudioSelectedMode = i;
 				} else {
 					aMode.isSelected = false;
 				}
 			}
		} else {
			mAudioSelectedMode = 0;
			setAudioModeSelected(0, true);
 		}
 
 		// Video output mode
 		mHasCVBSOutput = Utils.hasCVBSMode();
 		if (mHasCVBSOutput) {
 			String valCVBSmode = DisplaySettings.getCVBSOutpuMode();
 			mCVBSValues = getResources().getStringArray(
 					R.array.cvbsmode_entries);
 			if (!valCVBSmode.equals("null")) {
 				mCVBSIndex = DisplaySettings.findIndexOfEntry(valCVBSmode,
 						mCVBSValues);
 			}
 		}
 
 		// default frequency
 		mHasDefaultFrequency = Utils.platformHasDefaultTVFreq();
 		Log.d(TAG, "has default frequency = " + mHasDefaultFrequency);
 		if (mHasDefaultFrequency) {
 			mDefaultFrequency = DisplaySettings.getDefaultFrequency();
 			if (mDefaultFrequency.isEmpty()) {
 				mDefaultFrequency = mDefaultFrequencyEntries[DefaultFrequecy60Hz]
 						.toString();
 			}
 		}
 
 		Log.d(TAG, "default fq " + mDefaultFrequency);
 
 		// output mode
 		String videoMode = DisplaySettings.getOutpuMode();
 
 		Log.d(TAG, "default mode " + videoMode);
 
 		mVideoSelectedMode = -1;
 
 		if (videoMode.isEmpty()) {
 			OutputMode mode = mVideoModes.get(mVideoModes.size() - 1);
 			mode.isSelected = true; // default is 720p
 			mVideoSelectedMode = mVideoModes.size() - 1;
 			Log.d(TAG, "default output mode = " + mode.modeStr + " value "
 					+ mode.modeValue);
 		} else {
 			for (int i = 1; i < mVideoModes.size(); i++) {
 				OutputMode mode = mVideoModes.get(i);
 				boolean selected = false;
 				if (mode.modeValue.equals(videoMode)) {
 					if (mHasDefaultFrequency) {
 						if (mode.frequecy.equalsIgnoreCase(mDefaultFrequency)) {
 							selected = true;
 						}
 					} else {
 						if (mode.frequecy.equalsIgnoreCase("60Hz")) {
 							selected = true;
 						}
 					}
 				}
 
 				mode.isSelected = selected;
 				if (selected) {
 					mVideoSelectedMode = i;
 				}
 			}
 
 			if (mVideoSelectedMode < 0) {
 				mVideoSelectedMode = 0;
 				OutputMode mode = mVideoModes.get(0);
 				mode.isSelected = true;
 			}
 		}
 
 		mVideoOutputModeAdapter.notifyDataSetChanged();
 		mAudioOutputModeAdapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		View v = mAudioOutputView.getSelectedView();
 		Log.d(TAG, "1 ================ v " + v);
 		showSelectedItem(v, false);
 
 		mVideoOutputView.requestFocus();
 	}
 
 	public void initializeView() {
 		super.initializeView();
 
 		mVideoModeEntries = getResources().getStringArray(
 				R.array.outputmode_entries);
 
 		mAudioModeEntriesStr = getResources().getStringArray(
 				R.array.digit_audio_output_entries_str);
 
 		mAudioModeEntries = getResources().getStringArray(
 				R.array.digit_audio_output_entries);
 
 		mAudioModeValues = getResources().getStringArray(
 				R.array.digit_audio_output_entries_values);
 
 		mDefaultFrequencyEntries = getResources().getStringArray(
 				R.array.default_frequency_entries);
 
 		OutputMode mode = new OutputMode();
 		mode.modeStr = mVideoModeEntries[0];
 		mode.modeValue = "auto";
 		mVideoModes.add(mode);
 
 		mode = new OutputMode();
 		mode.modeStr = mVideoModeEntries[1];
 		mode.modeValue = "1080p";
 		mode.frequecy = mDefaultFrequencyEntries[DefaultFrequecy50Hz]
 				.toString();
 		mVideoModes.add(mode);
 
 		mode = new OutputMode();
 		mode.modeStr = mVideoModeEntries[2];
 		mode.modeValue = "1080p";
 		mode.frequecy = mDefaultFrequencyEntries[DefaultFrequecy60Hz]
 				.toString();
 		mVideoModes.add(mode);
 
 		mode = new OutputMode();
 		mode.modeStr = mVideoModeEntries[3];
 		mode.modeValue = "1080i";
 		mode.frequecy = mDefaultFrequencyEntries[DefaultFrequecy50Hz]
 				.toString();
 		mVideoModes.add(mode);
 
 		mode = new OutputMode();
 		mode.modeStr = mVideoModeEntries[4];
 		mode.modeValue = "720p";
 		mode.frequecy = mDefaultFrequencyEntries[DefaultFrequecy60Hz]
 				.toString();
 		mVideoModes.add(mode);
 
 		for (int i = 0; i < mVideoModes.size(); i++) {
 			OutputMode vMode = mVideoModes.get(i);
 			Log.d(TAG, "video mode " + vMode.modeStr + " " + vMode.modeValue
 					+ " f " + vMode.frequecy);
 		}
 
 		for (int i = 0; i < mAudioModeEntries.length; i++) {
 			OutputMode aMode = new OutputMode();
 			aMode.modeStr = mAudioModeEntriesStr[i];
 			aMode.modeValue = mAudioModeEntries[i];
 			mAudioModes.add(aMode);
 
 			Log.d(TAG, "audio mode " + aMode.modeStr + " " + aMode.modeValue);
 		}
 
 		mVideoOutputView = (ListView) findViewById(R.id.video_outputmode_list);
 		mAudioOutputView = (ListView) findViewById(R.id.audio_outputmode_list);
 
 		mVideoOutputModeAdapter = new ListAdapter(this);
 		mAudioOutputModeAdapter = new ListAdapter(this);
 
 		mVideoOutputModeAdapter.setDataSet(mVideoModes);
 		mAudioOutputModeAdapter.setDataSet(mAudioModes);
 
 		mVideoOutputView.setAdapter(mVideoOutputModeAdapter);
 		mAudioOutputView.setAdapter(mAudioOutputModeAdapter);
 
 		mVideoOutputView.setOnItemClickListener(mOnVideoModeClickedListener);
 		mAudioOutputView.setOnItemClickListener(mOnAudioModeSelectedListener);
 
 		mVideoOutputView.setOnItemSelectedListener(mVideoItemSelectedListener);
 		mAudioOutputView.setOnItemSelectedListener(mAudioItemSelectedListener);
 
 		mVideoOutputView.setOnFocusChangeListener(mFocusChangeListener);
 		mAudioOutputView.setOnFocusChangeListener(mFocusChangeListener);
 
 		mVideoOutputView.setOnKeyListener(mVideoKeyListener);
 		mAudioOutputView.setOnKeyListener(mAudioKeyListener);
 	}
 
 	OnItemClickListener mOnVideoModeClickedListener = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View v, int position,
 				long id) {
 			onModeChanged(position);
 		}
 
 	};
 
 	private void onModeChanged(int modeIndex) {
 		if (mVideoSelectedMode == modeIndex)
 			return;
 
 		mVideoLastSelectedMode = mVideoSelectedMode;
 		mVideoSelectedMode = modeIndex;
 
 		if (mVideoLastSelectedMode >= 0) {
 			setVideoModeSelected(mVideoLastSelectedMode, false, false);
 		}
 
 		if (modeIndex > 0) {
 			setVideoModeSelected(modeIndex, true, true);
 
 			OutputMode mode = mVideoModes.get(modeIndex);
 			Intent intent = new Intent(this, OutputSetConfirm.class);
 			intent.putExtra("set_mode", mode.modeValue);
 			if (mHasCVBSOutput) {
 				intent.putExtra("cvbs_mode", mCVBSIndex);
 			}
 			startActivityForResult(intent, SettingsCommon.GET_USER_OPERATION);
 		} else {
 			// "auto" mode
 			setVideoModeSelected(modeIndex, true, false);
 			
 			OutputMode mode = mVideoModes.get(mVideoModes.size() - 1);
 			Intent intent = new Intent(this, OutputSetConfirm.class);
 			intent.putExtra("set_mode", mode.modeValue);
 			if (mHasCVBSOutput) {
 				intent.putExtra("cvbs_mode", mCVBSIndex);
 			}
 			startActivityForResult(intent, SettingsCommon.GET_USER_OPERATION);
 		}
 
 		mVideoOutputModeAdapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case (SettingsCommon.GET_USER_OPERATION):
 			if (resultCode == Activity.RESULT_OK) {
 				OutputMode mode = mVideoModes.get(mVideoSelectedMode);
 				Intent saveIntent = new Intent(
 						SettingsCommon.ACTION_OUTPUTMODE_SAVE);
 				saveIntent.putExtra(SettingsCommon.OUTPUT_MODE, mode.modeValue);
 				sendBroadcast(saveIntent);
 
 			} else if (resultCode == Activity.RESULT_CANCELED) {
 				if (mVideoLastSelectedMode >= 0) {
 					setVideoModeSelected(mVideoLastSelectedMode, true, true);
 				}
 
 				setVideoModeSelected(mVideoSelectedMode, false, false);
 
 				// set the selected mode
 				mVideoSelectedMode = mVideoLastSelectedMode;
 
 				mVideoOutputModeAdapter.notifyDataSetChanged();
 			}
 			break;
 		}
 	}
 
 	OnItemClickListener mOnAudioModeSelectedListener = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View v, int position,
 				long id) {
 			if (mAudioSelectedMode >= 0) {
 				setAudioModeSelected(mAudioSelectedMode, false);
 			}
 
 			mAudioSelectedMode = position;
 			setAudioModeSelected(mAudioSelectedMode, true);
 
 			mAudioOutputModeAdapter.notifyDataSetChanged();
 		}
 
 	};
 
 	OnItemSelectedListener mAudioItemSelectedListener = new OnItemSelectedListener() {
 
 		@Override
 		public void onItemSelected(AdapterView<?> parent, View view,
 				int position, long id) {
 
 			if (!mAudioOutputView.hasFocus())
 				return;
 
 			mAudioLastSelectedView = mAudioSelectedView;
 			mAudioSelectedView = view;
 
 			showSelectedItem(mAudioLastSelectedView, false);
 			showSelectedItem(mAudioSelectedView, true);
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> arg0) {
 		}
 
 	};
 
 	OnItemSelectedListener mVideoItemSelectedListener = new OnItemSelectedListener() {
 
 		@Override
 		public void onItemSelected(AdapterView<?> parent, View view,
 				int position, long id) {
 
 			mLastSelectedView = mSelectedView;
 			mSelectedView = view;
 
 			showSelectedItem(mLastSelectedView, false);
 			showSelectedItem(mSelectedView, true);
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> arg0) {
 		}
 
 	};
 
 	View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
 
 		@Override
 		public void onFocusChange(View v, boolean hasFocus) {
 
 			if (v.getId() == R.id.video_outputmode_list) {
 				showSelectedItem(mSelectedView, hasFocus);
 			} else if (v.getId() == R.id.audio_outputmode_list) {
 
 				if (mAudioSelectedView == null) {
 					mAudioSelectedView = mAudioOutputView.getChildAt(0);
 				}
 				showSelectedItem(mAudioSelectedView, hasFocus);
 
 			}
 		}
 	};
 
 	void showSelectedItem(View v, boolean show) {
 
 		if (v != null) {
 			ListAdapter.ViewHolder holder = (ListAdapter.ViewHolder) v.getTag();
 			holder.modeViewHighlight.setVisibility(show ? View.VISIBLE
 					: View.GONE);
 			holder.modeView.setVisibility(show ? View.GONE : View.VISIBLE);
 
 			Log.d(TAG, "view " + holder.modeView.getText() + " show=" + show);
 		}
 	}
 
 	private void setVideoModeSelected(int position, boolean selected,
 			boolean setFrequency) {
 
 		Log.d(TAG, "set " + position + " " + selected + " " + setFrequency);
 
 		OutputMode mode = mVideoModes.get(position);
 		mode.isSelected = selected;
 
 		if (setFrequency) {
 			DisplaySettings.setDefaultFrequency(mode.frequecy);
 		}
 	}
 
 	private void setAudioModeSelected(int position, boolean selected) {
 		OutputMode mode = mAudioModes.get(position);
 		mode.isSelected = selected;
 
 		if (selected) {
 			String value = mAudioModeValues[position];
 			SoundSettings.setAudioOutputMode(mode.modeValue, value);
 		}
 	}
 
 	private View.OnKeyListener mVideoKeyListener = new View.OnKeyListener() {
 
 		@Override
 		public boolean onKey(View v, int keyCode, KeyEvent event) {
 
 			if (event.getAction() == KeyEvent.ACTION_DOWN) {
 				if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
 					return handleUpKey(mVideoOutputView);
 				} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
 					return handleDownKey(mVideoOutputView);
 				}
 
 			}
 
 			return false;
 		}
 	};
 
 	private View.OnKeyListener mAudioKeyListener = new View.OnKeyListener() {
 
 		@Override
 		public boolean onKey(View v, int keyCode, KeyEvent event) {
 			if (event.getAction() == KeyEvent.ACTION_DOWN) {
 				if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
 					return handleUpKey(mAudioOutputView);
 				} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
 					return handleDownKey(mAudioOutputView);
 				}
 			}
 
 			return false;
 		}
 	};
 
 	boolean handleUpKey(ListView listView) {
 		if (listView.getSelectedItemPosition() == 0) {
 			listView.setSelection(listView.getCount() - 1);
 			return true;
 		}
 
 		return false;
 	}
 
 	boolean handleDownKey(ListView listView) {
 		if (listView.getSelectedItemPosition() == listView.getCount() - 1) {
 			listView.setSelection(0);
 			return true;
 		}
 
 		return false;
 	}
 
 	private class ListAdapter extends BaseAdapter {
 
 		public class ViewHolder {
 			TextView modeViewHighlight;
 			TextView modeView;
 			CheckBox checkBox;
 		}
 
 		private ArrayList<OutputMode> mDataSet = null;
 
 		public ListAdapter(Context context) {
 		}
 
 		public void setDataSet(ArrayList<OutputMode> dataSet) {
 			mDataSet = dataSet;
 		}
 
 		@Override
 		public int getCount() {
 			int count = 0;
 			if (mDataSet != null) {
 				count = mDataSet.size();
 			}
 			return count;
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ViewHolder holder = null;
 			if (null == convertView) {
 				LayoutInflater inflater = getLayoutInflater();
 				convertView = inflater.inflate(R.layout.checked_listitem,
 						parent, false);
 
 				holder = new ViewHolder();
 				holder.modeView = (TextView) convertView
 						.findViewById(R.id.title);
 				holder.modeViewHighlight = (TextView) convertView
 						.findViewById(R.id.highlight_title);
 				holder.checkBox = (CheckBox) convertView
 						.findViewById(R.id.checked_indicator);
 
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			holder.modeView.setText(mDataSet.get(position).modeStr);
 			holder.modeViewHighlight.setText(mDataSet.get(position).modeStr);
 			holder.checkBox.setChecked(mDataSet.get(position).isSelected);
 
 			return convertView;
 		}
 	}
 }
