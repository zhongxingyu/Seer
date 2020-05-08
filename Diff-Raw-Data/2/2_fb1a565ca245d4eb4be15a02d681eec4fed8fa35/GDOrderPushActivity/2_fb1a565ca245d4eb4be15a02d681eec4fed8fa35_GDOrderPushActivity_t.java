 package com.dbstar.app;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 
 import com.dbstar.R;
 import com.dbstar.model.GDDiskInfo;
 import com.dbstar.model.GuideListItem;
 import com.dbstar.model.ReceiveEntry;
 import com.dbstar.service.GDDataProviderService;
 import com.dbstar.util.StringUtil;
 import com.dbstar.widget.GDAdapterView.OnItemSelectedListener;
 import com.dbstar.widget.GDAdapterView;
 import com.dbstar.widget.GDGridView;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class GDOrderPushActivity extends GDBaseActivity {
 
 	private static final String TAG = "GDOrderPushActivity";
 
 	private static final int TIMELINE_ITEMS_COUNT = 7;
 	private static final int RECEIVE_ITEMS_COUNT = 8;
 
 	GDGridView mTimelineView = null;
 	TimelineAdapter mTimelineAdapter = null;
 	GDGridView mListView = null;
 	ReceiveItemsAdapter mReceiveItemAdapter;
 	View mTimelineItemFousedView = null;
 
 	Drawable mTimelineItemIconNormal, mTimelineItemIconFocused,
 			mTimelineItemTextFocusedBackground, mReceiveItemLightBackground,
 			mReceiveItemDarkBackground, mReceiveItemFocusedBackground,
 			mReceiveItemChecked, mReceiveItemUnchecked;
 
 	List<ReceiveTask[]> mTaskPages = null;
 	int mTasksPageNumber;
 	int mTasksPageCount;
 	ReceiveTask mCurrentTask;
 
 	ReceiveItem[] mReceiveItemCurrentPage;
 	int mReceiveItemIndex = -1;
 
 	class ReceiveItem {
 
 		GuideListItem Item;
 
 		public String Type() {
 			return Item.ColumnType;
 		}
 
 		public String Title() {
 			return Item.Name;
 		}
 
 		public boolean isReceive() {
 			return Item.isSelected;
 		}
 
 		public void setIsReceive(boolean receive) {
 			Item.isSelected = receive;
 		}
 
 		public boolean isModified() {
 			return Item.isSelected != Item.originalSelected;
 		}
 	}
 
 	static class ReceiveTask {
 
 		public String Date;
 
 		public String DisplayDate;
 
 		int ItemsPageNumber;
 		int ItemsPageCount;
 
 		int ItemsCount;
 		ArrayList<GuideListItem> allItems;
 
 		public List<ReceiveItem[]> ItemPages;
 
 		public ReceiveTask() {
 			ItemPages = null;
 		}
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.orderpush_view);
 
 		initializeView();
 	}
 
 	protected void initializeView() {
 		// no menu path, so need to call base method
 		// super.initializeView();
 
 		mTimelineItemIconNormal = getResources().getDrawable(
 				R.drawable.timeline_button_normal);
 		mTimelineItemIconFocused = getResources().getDrawable(
 				R.drawable.timeline_button_focused);
 		mTimelineItemTextFocusedBackground = getResources().getDrawable(
 				R.drawable.timeline_date_focused);
 		mReceiveItemLightBackground = getResources().getDrawable(
 				R.drawable.listitem_light_bg);
 		mReceiveItemDarkBackground = getResources().getDrawable(
 				R.drawable.listitem_dark_bg);
 		mReceiveItemFocusedBackground = getResources().getDrawable(
 				R.drawable.receive_item_focused_bg);
 		mReceiveItemChecked = getResources().getDrawable(
 				R.drawable.checkbox_checked);
 		mReceiveItemUnchecked = getResources().getDrawable(
 				R.drawable.checkbox_unchecked);
 
 		mTimelineView = (GDGridView) findViewById(R.id.timeline);
 		mTimelineAdapter = new TimelineAdapter(this);
 		mTimelineView.setAdapter(mTimelineAdapter);
 
 		mListView = (GDGridView) findViewById(R.id.receive_list);
 		mReceiveItemAdapter = new ReceiveItemsAdapter(this);
 		mListView.setAdapter(mReceiveItemAdapter);
 
 		mTimelineView.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(GDAdapterView<?> parent, View view,
 					int position, long id) {
 
 				ReceiveTask[] tasks = mTaskPages.get(mTasksPageNumber);
 				mCurrentTask = tasks[position];
 				mReceiveItemCurrentPage = mCurrentTask.ItemPages
 						.get(mCurrentTask.ItemsPageNumber);
 				mReceiveItemAdapter.setDataSet(mReceiveItemCurrentPage);
 				mReceiveItemAdapter.notifyDataSetChanged();
 				mListView.setSelection(0);
 				mListView.invalidate();
 			}
 
 			@Override
 			public void onNothingSelected(GDAdapterView<?> parent) {
 
 			}
 
 		});
 
 		mListView.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(GDAdapterView<?> parent, View view,
 					int position, long id) {
 
 				if (mReceiveItemIndex >= 0) {
 					View oldSel = mListView.getChildAt(mReceiveItemIndex);
 					Drawable d = position % 2 == 0 ? mReceiveItemLightBackground
 							: mReceiveItemDarkBackground;
 					if (oldSel != null) {
 						oldSel.setBackgroundDrawable(d);
 					}
 				}
 
 				mReceiveItemIndex = position;
 				view.setBackgroundDrawable(mReceiveItemFocusedBackground);
 			}
 
 			@Override
 			public void onNothingSelected(GDAdapterView<?> parent) {
 
 			}
 
 		});
 
 		mTimelineView.setFocusable(true);
 		mTimelineView.requestFocus();
 
 		mListView.setFocusable(true);
 //		mListView.setOnKeyListener(mReceiveItemsKeyListener);
 	}
 
 	public void onServiceStart() {
 		super.onServiceStart();
 
 		if (mService != null) {
 			mService.getAllGuideList(this);
 		}
 	}
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (mService != null && mTaskPages != null && mTaskPages.size() > 0) {
 				ArrayList<GuideListItem> items = new ArrayList<GuideListItem>();
 				for (int i = 0; i < mTaskPages.size(); i++) {
 					ReceiveTask[] tasks = mTaskPages.get(i);
 					for (int j = 0; j < tasks.length; j++) {
 						ReceiveTask task = tasks[j];
 						for (int k = 0; k < task.ItemPages.size(); k++) {
 							ReceiveItem[] listItems = task.ItemPages.get(k);
 							for (int l = 0; l < listItems.length; l++) {
 								ReceiveItem item = listItems[l];
 								if (item.isModified()) {
 									items.add(item.Item);
 								}
 							}
 						}
 					}
 				}
 
 				if (items.size() > 0) {
 					mService.updateGuideList(this, (GuideListItem[]) items
 							.toArray(new GuideListItem[items.size()]));
 				}
 			}
 		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
 
 			int index = mTimelineView.getSelectedItemPosition();
 			int pageSize = Math.min(TIMELINE_ITEMS_COUNT,
 					mTimelineAdapter.getCount());
 			if (index == (pageSize - 1)) {
 				if (mTasksPageNumber < (mTasksPageCount - 1)) {
 					mTasksPageNumber++;
 					mReceiveItemIndex = -1;
 					ReceiveTask[] tasks = mTaskPages.get(mTasksPageNumber);
 					mTimelineAdapter.setDataSet(tasks);
 					mTimelineAdapter.notifyDataSetChanged();
 					mTimelineView.setSelection(0);
 				}
 			} else {
 				if (index >= 0 && index < (pageSize - 1)) {
 					mTimelineView.setSelection(index + 1);
 				} else {
 					mTimelineView.setSelection(0);
 				}
 			}
 
 			return true;
 
 		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
 			int index = mTimelineView.getSelectedItemPosition();
 			if (index == 0) {
 				if (mTasksPageNumber > 0) {
 					mTasksPageNumber--;
 					mReceiveItemIndex = -1;
 					ReceiveTask[] tasks = mTaskPages.get(mTasksPageNumber);
 					mTimelineAdapter.setDataSet(tasks);
 					mTimelineAdapter.notifyDataSetChanged();
 					mTimelineView.setSelection(TIMELINE_ITEMS_COUNT - 1);
 				}
 			} else {
 				if (index > 0) {
 					mTimelineView.setSelection(index - 1);
 				} else {
 					mTimelineView.setSelection(TIMELINE_ITEMS_COUNT - 1);
 				}
 			}
 
 			return true;
 
 		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
 			if (mCurrentTask == null) {
 				return true;
 			}
 
 			int pageNumber = mCurrentTask.ItemsPageNumber;
 			int index = mListView.getSelectedItemPosition();
 			if (index == 0) {
 				if (pageNumber > 0) {
 					mCurrentTask.ItemsPageNumber--;
 					pageNumber = mCurrentTask.ItemsPageNumber;
 
 					mReceiveItemIndex = -1;
 					mReceiveItemCurrentPage = mCurrentTask.ItemPages
 							.get(pageNumber);
 					mReceiveItemAdapter.setDataSet(mReceiveItemCurrentPage);
 					mReceiveItemAdapter.notifyDataSetChanged();
 					mListView.setSelection(RECEIVE_ITEMS_COUNT - 1);
 				}
 			} else {
 
 				if (index > 0) {
 					mListView.setSelection(index - 1);
 				} else {
 					mListView.setSelection(RECEIVE_ITEMS_COUNT - 1);
 				}
 			}
 
 			return true;
 
 		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
 			if (mCurrentTask == null) {
 				return true;
 			}
 
 			int pageNumber = mCurrentTask.ItemsPageNumber;
 
 			int index = mListView.getSelectedItemPosition();
 
 			int pageSize = Math.min(RECEIVE_ITEMS_COUNT,
 					mReceiveItemAdapter.getCount());
 
 			if (index == (pageSize - 1)) {
 				if (pageNumber < (mCurrentTask.ItemsPageCount - 1)) {
 					mCurrentTask.ItemsPageNumber++;
 					pageNumber = mCurrentTask.ItemsPageNumber;
 
 					mReceiveItemIndex = -1;
 					mReceiveItemCurrentPage = mCurrentTask.ItemPages
 							.get(pageNumber);
 					mReceiveItemAdapter.setDataSet(mReceiveItemCurrentPage);
 					mReceiveItemAdapter.notifyDataSetChanged();
 
 					mListView.setSelection(0);
 				}
 			} else {
 				if (index >= 0 && index < (pageSize - 1)) {
 					mListView.setSelection(index + 1);
 				} else {
 					mListView.setSelection(0);
 				}
 			}
 
 			return true;
 		} else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
 			int index = mListView.getSelectedItemPosition();
 			if (index >= 0 && index < mReceiveItemAdapter.getCount()) {
 				ReceiveItem item = mReceiveItemCurrentPage[index];
 				item.setIsReceive(!item.isReceive());
 
 				mReceiveItemAdapter.notifyDataSetChanged();
 			}
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	public void updateData(int type, Object key, Object data) {
 		if (type != GDDataProviderService.REQUESTTYPE_GETGUIDELIST)
 			return;
 
 		GuideListItem[] items = (GuideListItem[]) data;
 		if (items != null && items.length > 0) {
 
 			Log.d(TAG, "items count=" + items.length);
 
 			mTaskPages = new LinkedList<ReceiveTask[]>();
 
 			ArrayList<ReceiveTask> allTasks = new ArrayList<ReceiveTask>();
 			for (int i = 0; i < items.length; i++) {
 				ReceiveTask task = getTaskByDate(allTasks, items[i].Date);
 				if (task == null) {
 					task = addTaskOrderByDate(allTasks, items[i].Date);
 				}
 
 				if (task.allItems == null) {
 					task.allItems = new ArrayList<GuideListItem>();
 				}
 
 				task.allItems.add(items[i]);
 			}
 
 			while (allTasks.size() > 0) {
 				int pageSize = 0;
 				if (allTasks.size() > TIMELINE_ITEMS_COUNT) {
 					pageSize = TIMELINE_ITEMS_COUNT;
 				} else {
 					pageSize = allTasks.size();
 				}
 				ReceiveTask[] tasks = new ReceiveTask[pageSize];
 				for (int i = 0; i < tasks.length; i++) {
 					tasks[i] = allTasks.get(0);
 					allTasks.remove(0);
 
 					formItemsPages(tasks[i]);
 				}
 				mTaskPages.add(tasks);
 				mTasksPageCount++;
 			}
 
 			mTasksPageNumber = 0;
 			if (mTaskPages.size() > 0) {
 				mTimelineAdapter.setDataSet(mTaskPages.get(0));
 				mTimelineAdapter.notifyDataSetChanged();
 
 				mTimelineView.setSelection(0);
 			}
 		}
 	}
 
 	void formItemsPages(ReceiveTask task) {
 		task.ItemPages = new ArrayList<ReceiveItem[]>();
 		task.ItemsPageNumber = 0;
 		ArrayList<GuideListItem> items = task.allItems;
 		task.ItemsCount = items.size();
 		while (items.size() > 0) {
 			int pageSize = RECEIVE_ITEMS_COUNT;
 			if (items.size() < RECEIVE_ITEMS_COUNT) {
 				pageSize = items.size();
 			}
 
 			ReceiveItem[] listItems = new ReceiveItem[pageSize];
 			for (int i = 0; i < listItems.length; i++) {
 				ReceiveItem item = new ReceiveItem();
 				item.Item = items.get(0);
 				items.remove(0);
 				listItems[i] = item;
 			}
 			task.ItemPages.add(listItems);
 			task.ItemsPageCount++;
 		}
 	}
 
 	ReceiveTask getTaskByDate(ArrayList<ReceiveTask> tasks, String date) {
 		for (int i = 0; i < tasks.size(); i++) {
 			if (tasks.get(i).Date.equals(date))
 				return tasks.get(i);
 		}
 
 		return null;
 	}
 
 	ReceiveTask addTaskOrderByDate(ArrayList<ReceiveTask> tasks, String date) {
 		ReceiveTask task = new ReceiveTask();
 
 		if (date.length() == "yyyy-mm-dd".length()) {
			date = date + " 00:00:00";
 		}
 
 		task.Date = date;
 		String strDate = task.Date.substring(0, "yyyy-mm-dd".length());
 		String month = strDate.substring(5, 7);
 		String day = strDate.substring(8);
 
 		task.DisplayDate = month + mResource.CH_Month + day + mResource.CH_Day;
 
 		if (tasks.size() == 0) {
 			tasks.add(task);
 			Log.d(TAG, "add task 0 " + date);
 			return task;
 		}
 
 		int i = 0;
 		for (i = 0; i < tasks.size(); i++) {
 			if (isDateGreater(date, tasks.get(i).Date) == 1) {
 				continue;
 			} else {
 				break;
 			}
 		}
 
 		if (i == tasks.size()) {
 			tasks.add(task);
 			Log.d(TAG, "add task " + tasks.size() + " " + (i + 1) + " " + date);
 		} else {
 			tasks.add(i, task);
 			Log.d(TAG, "add task " + tasks.size() + " " + (i) + " " + date);
 		}
 
 		return task;
 	}
 
 	// date1 is greater than date2
 	// return value: -1: less 0:equal 1:greater
 	static int isDateGreater(String dateStr1, String dateStr2) {
 
 		Log.d(TAG, "date1 = " + dateStr1 + " date2=" + dateStr2);
 
 		final String formats = "yyyy-MM-dd HH:mm:ss";
 
 		try {
 			SimpleDateFormat sdf = new SimpleDateFormat(formats);
 			Date date1 = sdf.parse(dateStr1);
 			Date data2 = sdf.parse(dateStr2);
 
 			return date1.compareTo(data2);
 		} catch (ParseException e) {
 			e.printStackTrace();
 
 			return -2;
 		}
 	}
 
 	View.OnKeyListener mReceiveItemsKeyListener = new View.OnKeyListener() {
 
 		@Override
 		public boolean onKey(View v, int keyCode, KeyEvent event) {
 			int action = event.getAction();
 			if (action == KeyEvent.ACTION_DOWN) {
 				Log.d(TAG, " ---- key code =  " + keyCode);
 				switch (keyCode) {
 				case KeyEvent.KEYCODE_ENTER:
 				case KeyEvent.KEYCODE_DPAD_CENTER: {
 					int index = mListView.getSelectedItemPosition();
 					if (index >= 0 && index < mReceiveItemAdapter.getCount()) {
 						ReceiveItem item = mReceiveItemCurrentPage[index];
 						item.setIsReceive(!item.isReceive());
 
 						mReceiveItemAdapter.notifyDataSetChanged();
 					}
 					return true;
 				}
 				}
 			}
 			return false;
 		}
 	};
 
 	private class TimelineAdapter extends BaseAdapter {
 
 		private ReceiveTask[] mDataSet = null;
 
 		public class ViewHolder {
 			TextView timeView;
 			ImageView iconView;
 		}
 
 		public TimelineAdapter(Context context) {
 		}
 
 		public void setDataSet(ReceiveTask[] dataSet) {
 			mDataSet = dataSet;
 		}
 
 		@Override
 		public int getCount() {
 			int count = 0;
 			if (mDataSet != null) {
 				count = mDataSet.length;
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
 
 			ViewHolder holder = new ViewHolder();
 			if (position == mTimelineView.getSelectedItemPosition()) {
 				if (mTimelineItemFousedView == null) {
 					mTimelineItemFousedView = getLayoutInflater().inflate(
 							R.layout.timeline_item_focused, parent, false);
 
 					holder.timeView = (TextView) mTimelineItemFousedView
 							.findViewById(R.id.time_view);
 					holder.iconView = (ImageView) mTimelineItemFousedView
 							.findViewById(R.id.icon);
 					mTimelineItemFousedView.setTag(holder);
 				}
 				convertView = mTimelineItemFousedView;
 			} else {
 				if (convertView == mTimelineItemFousedView) {
 					convertView = null;
 				}
 			}
 
 			if (null == convertView) {
 				convertView = getLayoutInflater().inflate(
 						R.layout.timeline_item, parent, false);
 				holder.timeView = (TextView) convertView
 						.findViewById(R.id.time_view);
 				holder.iconView = (ImageView) convertView
 						.findViewById(R.id.icon);
 
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			holder.timeView.setText(mDataSet[position].DisplayDate);
 
 			return convertView;
 		}
 	}
 
 	private class ReceiveItemsAdapter extends BaseAdapter {
 
 		private ReceiveItem[] mDataSet = null;
 
 		public class ViewHolder {
 			TextView typeView;
 			TextView titleView;
 			ImageView checkerView;
 		}
 
 		public ReceiveItemsAdapter(Context context) {
 		}
 
 		public void setDataSet(ReceiveItem[] dataSet) {
 			mDataSet = dataSet;
 		}
 
 		@Override
 		public int getCount() {
 			int count = 0;
 			if (mDataSet != null) {
 				count = mDataSet.length;
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
 
 			ViewHolder holder = new ViewHolder();
 
 			if (null == convertView) {
 				LayoutInflater inflater = getLayoutInflater();
 				convertView = inflater.inflate(R.layout.receive_item, parent,
 						false);
 
 				holder.typeView = (TextView) convertView
 						.findViewById(R.id.type_view);
 				holder.titleView = (TextView) convertView
 						.findViewById(R.id.title_view);
 				holder.checkerView = (ImageView) convertView
 						.findViewById(R.id.checker_view);
 
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			holder.typeView.setText(mDataSet[position].Type());
 			holder.titleView.setText(mDataSet[position].Title());
 			Drawable d = mDataSet[position].isReceive() ? mReceiveItemChecked
 					: mReceiveItemUnchecked;
 			holder.checkerView.setImageDrawable(d);
 
 			if (position == mListView.getSelectedItemPosition()) {
 				convertView
 						.setBackgroundDrawable(mReceiveItemFocusedBackground);
 				holder.typeView.setTextColor(Color.WHITE);
 				holder.titleView.setTextColor(Color.WHITE);
 			} else {
 				holder.typeView.setTextColor(Color.BLACK);
 				holder.titleView.setTextColor(Color.BLACK);
 				if (position % 2 == 0) {
 					convertView
 							.setBackgroundDrawable(mReceiveItemLightBackground);
 				} else {
 					convertView
 							.setBackgroundDrawable(mReceiveItemDarkBackground);
 				}
 			}
 			return convertView;
 		}
 	}
 }
