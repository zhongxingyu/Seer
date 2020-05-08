 package com.bitty.notifyme;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.bitty.utils.Convert;
 
 public class DailyNotificationsActivity extends ListActivity
 {
 
 	private TextView dayText;
 	private Button returnHomeButton;
 	private Context mContext;
 	private ArrayList<NotifyMeItem> notifyMeArray;
 	private ArrayList<DailyNotificationsItem> itemArray = new ArrayList<DailyNotificationsItem>();
 
 	private NotificationAdapter adapter;
 	private MyBroadCastReceiver receiver;
 	private NotifyApplication app;
 
 	@SuppressWarnings("unchecked")
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.daily_notifications);
 
 		app = (NotifyApplication) getApplication();
 
 		Typeface font = Typeface.createFromAsset(this.getAssets(), "fonts/VarelaRound-Regular.ttf");
 		Typeface font2 = Typeface.createFromAsset(this.getAssets(), "fonts/DINEngschrift-Regular.ttf");
 
 		dayText = (TextView) findViewById(R.id.day_text);
 		dayText.setTypeface(font);
 		dayText.setText(app.getCurrentDayName());
 
 		returnHomeButton = (Button) findViewById(R.id.return_home_btn);
 		returnHomeButton.setTypeface(font2);
 		returnHomeButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v)
 			{
 				finish();
 			}
 		});
 
 		notifyMeArray = app.getDailyNotificationArray();
 		adapter = new NotificationAdapter(this);
 		this.setListAdapter(adapter);
 
 		receiver = new MyBroadCastReceiver();
 	}
 
 	@Override
 	protected void onResume()
 	{
 		adapter.notifyDataSetChanged();
 		registerReceiver(receiver, new IntentFilter(DailyNotificationsItem.DELETE_ITEM));
 	}
 
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		unregisterReceiver(receiver);
 	}
 
 	private void onDeleteNotification(int _arrayIndex, long _db_ID)
 	{
 		long dbID = _db_ID;
 
 		ReminderManager reminderMngr = new ReminderManager(this);
 
 		// Remove alert for all notifications because DB resets key IDs. Alarm
 		// Id needs to be a reference
 		// to the database key
 		for (int i = 0; i < notifyMeArray.size(); i++)
 		{
 			NotifyMeItem item = notifyMeArray.get(i);
 			reminderMngr.clearReminder(Convert.safeLongToInt(item.getDB_ID()));
 		}
 
 		// Delete from DB
 		NotifyDBAdapter notifyDB = ((NotifyApplication) getApplication()).getNotifyDB();
 		notifyDB.removeNotification(dbID);
 		notifyDB.open();
 
 		// Remove from array
 		notifyMeArray.remove(_arrayIndex);
 
 		if (notifyMeArray.size() == 0)
 		{
 			finish();
 		} else
 		{
 			// reset array and reset notification alarms
 			notifyMeArray.clear();
 			ArrayList<NotifyMeItem> temp = notifyDB.getNotifyItemsByDay(app.getCurrentDayID());
 			app.setDailyNotificationArray(temp);
 
 			notifyMeArray = app.getDailyNotificationArray();
 
 			for (int j = 0; j < notifyMeArray.size(); j++)
 			{
 				NotifyMeItem item = notifyMeArray.get(j);
 				reminderMngr.setReminder(item.getHour(), item.getMinutes(), item.getDay(), item.getDB_ID());
 			}
 		}
 		adapter.notifyDataSetChanged();
 	}
 
 	/*
 	 * Adapter for Daily Notification Items
 	 */
 	public class NotificationAdapter extends BaseAdapter
 	{
 
 		public NotificationAdapter(Context c)
 		{
 			mContext = c;
 		}
 
 		public int getCount()
 		{
 			return notifyMeArray.size();
 		}
 
 		public Object getItem(int position)
 		{
 			return itemArray.get(position);
 		}
 
 		public long getItemId(int position)
 		{
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent)
 		{
 
 			NotifyMeItem notifyItem = (NotifyMeItem) notifyMeArray.get(position);
 
 			DailyNotificationsItem item = new DailyNotificationsItem(mContext);
 			item.setDB_ID(notifyItem.getDB_ID());
 			item.setArrayIndex(position);
 			item.setContent(notifyItem.getHour(), notifyItem.getMinutes(), notifyItem.getTrains(), notifyItem
 					.getTrainType());
 			itemArray.add(item);
 			return item;
 		}
 	}
 
 	/*
 	 * Broadcast receiver
 	 */
 	public class MyBroadCastReceiver extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive(Context context, Intent intent)
 		{
 			int arrayIndex = intent.getIntExtra("array_index", -1);
 			long db_ID = intent.getLongExtra("db_ID", -1);
 			onDeleteNotification(arrayIndex, db_ID);
 		}
 	}
 }
