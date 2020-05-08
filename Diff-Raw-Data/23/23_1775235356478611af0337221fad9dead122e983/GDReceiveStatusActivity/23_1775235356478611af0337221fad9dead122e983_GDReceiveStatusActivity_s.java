 package com.dbstar.app;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.dbstar.R;
 import com.dbstar.service.ClientObserver;
 import com.dbstar.service.GDDataProviderService;
 import com.dbstar.model.EventData;
 import com.dbstar.model.GDDiskInfo;
 import com.dbstar.model.ReceiveEntry;
 import com.dbstar.util.StringUtil;
 
 public class GDReceiveStatusActivity extends GDBaseActivity {
 
 	private static final String TAG = "GDReceiveStatusActivity";
 	private static final int PageSize = 10;
 
 	private static final int UpdatePeriodInMills = 5000;
 	private static final int UpdatePeriodInSecs = 5;
 	private int mPageNumber = 0;
 	private int mPageCount = 0;
 
 	private List<ReceiveEntry[]> mPageDatas = null;
 	private DownloadProgressAdapter mAdapter = null;
 
 	private static final int MSG_UPDATEPROGRESS = 0;
 
 	private static final int SignalStateNone = -1;
 	private static final int SignalStateOff = 0;
 	private static final int SignalStateOn = 1;
 
 	private int mSignalState = SignalStateNone;
 
 	private ClientObserver mObserver = null;
 
 	ListView mListView;
 	TextView mDownloadSpeedView;
 	TextView mDiskInfoView;
 	TextView mPageItemsView;
 	TextView mPageNumberView;
 	TextView mSignalStatusView;
 
 	String mStatusWaitting, mStatusDownloading, mStatusFinished, mStatusFailed;
 	String mTextDi, mTextYe, mTextGong, mTextTiao;
 
 	Drawable mReceiveItemLightBackground, mReceiveItemDarkBackground;
 
 	private Handler mUIUpdateHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_UPDATEPROGRESS: {
 				if (mService != null && mBound) {
 					mService.getDownloadStatus(mObserver);
 				}
 				break;
 			}
 			default:
 				break;
 			}
 		}
 	};
 
 	private void callTask() {
 		mUIUpdateHandler.sendEmptyMessage(MSG_UPDATEPROGRESS);
 	}
 
 	Timer mTimer = new Timer();
 	TimerTask mTask = null;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mObserver = this;
 
 		setContentView(R.layout.download_status_view);
 
 		mPageDatas = new LinkedList<ReceiveEntry[]>();
 
 		initializeView();
 
 		// Intent intent = getIntent();
 		// mMenuPath = intent.getStringExtra(INTENT_KEY_MENUPATH);
 		// showMenuPath(mMenuPath.split(MENU_STRING_DELIMITER));
 
 		mTask = new TimerTask() {
 			public void run() {
 				callTask();
 			}
 		};
 	}
 
 	public void onStart() {
 		super.onStart();
 
 		// showMenuPath(mMenuPath.split(MENU_STRING_DELIMITER));
 	}
 
 	public void onStop() {
 		super.onStop();
 
 		mTimer.cancel();
 		mTimer.purge();
 
 		mService.stopGetTaskInfo();
 	}
 
 	public void onServiceStart() {
 		super.onServiceStart();
 
 		mService.startGetTaskInfo();
 		mTimer.schedule(mTask, 1000, UpdatePeriodInMills);
 	}
 
 	public void onServiceStop() {
 		super.onServiceStop();
 	}
 
 	private void loadPrevPage() {
 		Log.d(TAG, "loadPrevPage count=" + mPageCount + " number= "
 				+ mPageNumber);
 
 		if ((mPageNumber - 1) >= 0) {
 			Log.d(TAG, "loadPrevPage");
 
 			mPageNumber--;
 			// mPageNumberView.setText(formPageText(mPageNumber));
 			updatePageInfoView();
 
 			ReceiveEntry[] entries = mPageDatas.get(mPageNumber);
 			mAdapter.setDataSet(entries);
 			mListView.clearChoices();
 			mListView.setSelection(0);
 			mAdapter.notifyDataSetChanged();
 		}
 	}
 
 	private void loadNextPage() {
 		Log.d(TAG, "loadNextPage count=" + mPageCount + " number= "
 				+ mPageNumber);
 
 		if (mPageCount == 0)
 			return;
 
 		if ((mPageNumber + 1) < mPageCount) {
 
 			mPageNumber++;
 			// mPageNumberView.setText(formPageText(mPageNumber));
 			updatePageInfoView();
 
 			ReceiveEntry[] entries = mPageDatas.get(mPageNumber);
 			mAdapter.setDataSet(entries);
 			mListView.clearChoices();
 			mListView.setSelection(0);
 			mAdapter.notifyDataSetChanged();
 		}
 	}
 
 	// void updateEntryData(ReceiveEntry oldEntry, ReceiveEntry newEntry) {
 	// oldEntry.RawProgress = newEntry.RawProgress;
 	// oldEntry.RawTotal = newEntry.RawTotal;
	// oldEntry.ConverSize();
 	// }
 
 	void updatePageData(ReceiveEntry[] pageEntries,
 			ArrayList<ReceiveEntry> allEntries) {
 
 		Log.d(TAG, "update page size=" + pageEntries.length + " total = "
 				+ allEntries.size());
 
 		ArrayList<ReceiveEntry> toRemoves = new ArrayList<ReceiveEntry>();
 		for (int i = 0; i < pageEntries.length; i++) {
 			for (int j = 0; j < allEntries.size(); j++) {
 				if (pageEntries[i].Id.equals(allEntries.get(j).Id)) {
 					// updateEntryData(pageEntries[i], allEntries.get(j));
 					pageEntries[i] = allEntries.get(j);
 					// remove not used items
 					toRemoves.add(allEntries.get(j));
 				}
 			}
 		}
 
 		if (toRemoves.size() > 0) {
 			allEntries.removeAll(toRemoves);
 		}
 	}
 
 	void addNewPageDatas(ArrayList<ReceiveEntry> entries) {
 		while (entries.size() > 0) {
 			int pageSize = 0;
 			if (entries.size() >= PageSize) {
 				pageSize = PageSize;
 			} else {
 				pageSize = entries.size();
 			}
 
 			if (pageSize > 0) {
 				ReceiveEntry[] newEntries = new ReceiveEntry[pageSize];
 				for (int j = 0; j < pageSize; j++) {
 					newEntries[j] = entries.get(0);
 					entries.remove(0);
 				}
 				Log.d(TAG, "add page size " + newEntries.length);
 				mPageDatas.add(newEntries);
 			}
 		}
 	}
 
 	void updatePagesData(ArrayList<ReceiveEntry> entries) {
 		for (int i = 0; i < mPageDatas.size(); i++) {
 			ReceiveEntry[] oldEntries = mPageDatas.get(i);
 			if (oldEntries.length == PageSize) {
 				updatePageData(oldEntries, entries);
 			} else {
 				// last page is not full
 				break;
 			}
 		}
 
 		int lastPageNumber = mPageDatas.size() - 1;
 
 		if (mPageDatas.get(lastPageNumber).length < PageSize) {
 			// the old last page is not full, update it
 			ReceiveEntry[] lastEntries = mPageDatas.get(lastPageNumber);
 			mPageDatas.remove(lastPageNumber);
 			updatePageData(lastEntries, entries);
 
 			ReceiveEntry[] newEntries = null;
 			if (entries.size() > 0) {
 				int pageSize = lastEntries.length + entries.size();
 				pageSize = pageSize < PageSize ? pageSize : PageSize;
 
 				newEntries = new ReceiveEntry[pageSize];
 				int i = 0;
 				for (i = 0; i < lastEntries.length; i++) {
 					newEntries[i] = lastEntries[i];
 				}
 
 				for (; i < newEntries.length; i++) {
 					newEntries[i] = entries.get(0);
 					entries.remove(0);
 				}
 			} else {
 				newEntries = lastEntries;
 			}
 
 			mPageDatas.add(newEntries);
 		}
 
 		addNewPageDatas(entries);
 	}
 
 	long computeEntriesSize(ReceiveEntry[] entries) {
 		long size = 0;
 		for (int i = 0; i < entries.length; i++) {
 			size += entries[i].RawProgress;
 		}
 
 		return size;
 	}
 
 	long computeAllPagesSize(List<ReceiveEntry[]> pages) {
 		long size = 0;
 		for (int i = 0; i < pages.size(); i++) {
 			size += computeEntriesSize(pages.get(i));
 		}
 		return size;
 	}
 
 	public void updateData(int type, Object key, Object data) {
 		if (type != GDDataProviderService.REQUESTTYPE_GETDOWNLOADSTATUS)
 			return;
 
 		ReceiveEntry[] entries = (ReceiveEntry[]) data;
 
 		if (entries != null && entries.length > 0) {
 
 			long preSize = 0;
 			long curSize = 0;
 
 			preSize = computeAllPagesSize(mPageDatas);
 			curSize = computeEntriesSize(entries);
 
 			float speed = (float) ((curSize - preSize) / 1024)
 					/ (float) UpdatePeriodInSecs;
 			String strSpeed = StringUtil.formatFloatValue(speed) + "KB/s";
 			mDownloadSpeedView.setText(strSpeed);
 
 			ArrayList<ReceiveEntry> entriesList = new ArrayList<ReceiveEntry>();
 			for (int i = 0; i < entries.length; i++) {
 				entriesList.add(entries[i]);
 			}
 
 			// Log.d(TAG, "1 page size=" + mPageDatas.size() + " entry size="
 			// + entriesList.size());
 			if (mPageDatas.size() > 0) {
 				updatePagesData(entriesList);
 			} else {
 				addNewPageDatas(entriesList);
 			}
 
 			mPageCount = mPageDatas.size();
 
 			if (mPageNumber > (mPageCount - 1)) {
 				mPageNumber = mPageCount - 1;
 			}
 
 			// update current page
 			// mPageNumberView.setText(formPageText(mPageNumber));
 			mAdapter.setDataSet(mPageDatas.get(mPageNumber));
 			mAdapter.notifyDataSetChanged();
 
 			GDDiskInfo.DiskInfo diskInfo = null;
 			if (mBound) {
 				String disk = mService.getStorageDisk();
 				if (disk == null || disk.isEmpty())
 					return;
 
 				diskInfo = GDDiskInfo.getDiskInfo(disk, true);
 				if (diskInfo != null) {
 					// String diskSpaceStr = diskInfo.DiskSpace + "/"
 					// + diskInfo.DiskSize;
 					// Log.d(TAG, " disk space = " + diskSpaceStr);
 					mDiskInfoView.setText(diskInfo.DiskSpace);
 				}
 			}
 
 			updatePageInfoView();
 		}
 	}
 
 	void updatePageInfoView() {
 		int pageNumber = mPageCount > 0 ? (mPageNumber + 1) : 0;
 		String pagesInfo = mTextDi + pageNumber + mTextYe;
 		pagesInfo += "/" + mTextGong + (mPageCount) + mTextYe;
 
 		mPageNumberView.setText(pagesInfo);
 
 		int startItem = 0;
 		int endItem = 0;
 		int totalItems = 0;
 
 		if (mPageCount > 0) {
 			startItem = mPageNumber * PageSize + 1;
 			endItem = startItem + mPageDatas.get(mPageNumber).length - 1;
 			totalItems = (mPageCount - 1) * PageSize;
 			totalItems += mPageDatas.get(mPageCount - 1).length;
 		}
 
 		String itemsInfo = mTextDi + startItem + "~" + endItem + mTextTiao;
 		itemsInfo += "/" + mTextGong + totalItems + mTextTiao;
 		mPageItemsView.setText(itemsInfo);
 	}
 
 	@Override
 	public void notifyEvent(int type, Object event) {
 
 		if (type == EventData.EVENT_DATASIGNAL) {
 			EventData.DataSignalEvent signalEvent = (EventData.DataSignalEvent) event;
 
 			mSignalState = signalEvent.hasSignal ? SignalStateOn
 					: SignalStateOff;
 
 			setSignalState(mSignalState);
 		}
 
 	}
 
 	void setSignalState(int state) {
 		String stateStr = "";
 		switch (state) {
 		case SignalStateOn: {
 			stateStr = this.getResources().getString(
 					R.string.receivestatus_status_hassignal);
 			break;
 		}
 		case SignalStateOff: {
 			stateStr = this.getResources().getString(
 					R.string.receivestatus_status_nosignal);
 			break;
 		}
 		default: {
 			break;
 		}
 		}
 
 		if (mSignalStatusView != null) {
 			mSignalStatusView.setText(stateStr);
 		}
 	}
 
 	public void initializeView() {
 		// super.initializeView();
 
 		mReceiveItemLightBackground = getResources().getDrawable(
 				R.drawable.receive_item_light_bg);
 		mReceiveItemDarkBackground = getResources().getDrawable(
 				R.drawable.receive_item_dark_bg);
 
 		mStatusWaitting = getResources().getString(
 				R.string.receivestatus_status_waitting);
 		mStatusDownloading = getResources().getString(
 				R.string.receivestatus_status_downloading);
 		mStatusFinished = getResources().getString(
 				R.string.receivestatus_status_finished);
 
 		mStatusFailed = getResources().getString(
 				R.string.receivestatus_status_failed);
 
 		mTextDi = getResources().getString(R.string.text_di);
 		mTextYe = getResources().getString(R.string.text_ye);
 		mTextGong = getResources().getString(R.string.text_gong);
 		mTextTiao = getResources().getString(R.string.text_tiao);
 
 		mDownloadSpeedView = (TextView) findViewById(R.id.download_speed);
 		mSignalStatusView = (TextView) findViewById(R.id.signal_status);
 		mDiskInfoView = (TextView) findViewById(R.id.disk_info);
 		mPageNumberView = (TextView) findViewById(R.id.download_pages);
 		mPageItemsView = (TextView) findViewById(R.id.download_items);
 
 		mAdapter = new DownloadProgressAdapter(this);
 		mListView = (ListView) findViewById(R.id.download_view);
 		mListView.setAdapter(mAdapter);
 
 		mListView.setOnKeyListener(new View.OnKeyListener() {
 
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				boolean ret = false;
 				int action = event.getAction();
 				if (action == KeyEvent.ACTION_DOWN) {
 					switch (keyCode) {
 
 					case KeyEvent.KEYCODE_DPAD_UP: {
 						loadPrevPage();
 						ret = true;
 						break;
 					}
 					case KeyEvent.KEYCODE_DPAD_DOWN: {
 						loadNextPage();
 						ret = true;
 						break;
 					}
 					default:
 						break;
 					}
 
 				}
 				return ret;
 			}
 		});
 	}
 
 	public class DownloadProgressAdapter extends BaseAdapter {
 
 		ReceiveEntry[] mDataSet = null;
 
 		public void setDataSet(ReceiveEntry[] dataSet) {
 			mDataSet = dataSet;
 		}
 
 		private class ItemHolder {
 			TextView Name;
 			ProgressBar ProgressView;
 			TextView Status;
 			TextView Progress;
 			TextView PercentView;
 		}
 
 		public DownloadProgressAdapter(Context context) {
 		}
 
 		public int getCount() {
 			int size = 0;
 
 			if (mDataSet != null) {
 				size = mDataSet.length;
 			}
 
 			// Log.d(TAG, " DownloadProgressAdapter count = " + size);
 			return size;
 		}
 
 		public Object getItem(int position) {
 			return null;
 		}
 
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ItemHolder holder = null;
 
 			if (convertView == null) {
 				holder = new ItemHolder();
 				convertView = getLayoutInflater().inflate(
 						R.layout.download_status_item, parent, false);
 
 				holder.Name = (TextView) convertView
 						.findViewById(R.id.text_name);
 
 				holder.ProgressView = (ProgressBar) convertView
 						.findViewById(R.id.progress_bar);
 
 				holder.Status = (TextView) convertView
 						.findViewById(R.id.text_status);
 
 				holder.Progress = (TextView) convertView
 						.findViewById(R.id.text_progress);
 
 				holder.PercentView = (TextView) convertView
 						.findViewById(R.id.text_percent);
 
 				convertView.setTag(holder);
 			} else {
 				holder = (ItemHolder) convertView.getTag();
 			}
 
 			holder.Name.setText(mDataSet[position].Name);
 
 			holder.ProgressView.setProgress(mDataSet[position].nProgress);
 
 			String status = mDataSet[position].RawProgress > 0 ? mStatusDownloading
 					: mStatusWaitting;
 			holder.Status.setText(status);
 
 			String strProgress = mDataSet[position].Progress + "/"
 					+ mDataSet[position].Total;
 			holder.Progress.setText(strProgress);
 
 			holder.PercentView.setText(mDataSet[position].Percent);
 
 			if (position % 2 == 0) {
 				convertView.setBackgroundDrawable(mReceiveItemLightBackground);
 			} else {
 				convertView.setBackgroundDrawable(mReceiveItemDarkBackground);
 			}
 
 			return convertView;
 		}
 	}
 
 }
