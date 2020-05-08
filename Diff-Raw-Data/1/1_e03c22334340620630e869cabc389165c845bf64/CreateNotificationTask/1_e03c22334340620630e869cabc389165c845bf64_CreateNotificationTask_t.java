 package com.hackathon.locateme.asynctasks;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.hackathon.locateme.AcceptLocationSMSActivity;
 import com.hackathon.locateme.GlobalConstants;
 import com.hackathon.locateme.HomeActivity;
 import com.hackathon.locateme.IncomingListActivity;
 import com.hackathon.locateme.R;
 import com.hackathon.locateme.utility.IncomingDBAdapter;
 import com.hackathon.locateme.utility.LocationUtility;
 
 public class CreateNotificationTask extends AsyncTask<Void, Void, Void> {
 	
 	public static final int INCOMING_LOCATION = 0;
 	public static final int DECLINED_LOCATION = 1;
 	public static final int ACCEPTED_LOCATION = 2;
 	private static final String TAG = CreateNotificationTask.class.getName();
 	
 	private Context mContext;
 	private String mPhoneNumber;
 	private String mMessage;
 	private String mTitle;
 	private String[] mData;
 	private double[] mCoordinates;
 	private Intent mIntent;
 	
 	public CreateNotificationTask(Context context, String data, int type) 
 	{
 		mContext = context;
 		mData = data.split("\n");
 		mIntent = createNotificationIntent(type);
 	}
 
 	@Override
 	protected Void doInBackground(Void... arg0) 
 	{
 		NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		Notification n = new Notification(R.drawable.icon, mPhoneNumber + " sent a location", System.currentTimeMillis());
 		n.flags |= Notification.FLAG_AUTO_CANCEL;
 		
 		PendingIntent intent = PendingIntent.getActivity(mContext, 0, mIntent, 0);
 		n.setLatestEventInfo(mContext, mMessage, mTitle, intent);
 		nm.notify(GlobalConstants.INITIAL_SENT_LOCATION, n);
 		return null;
 	}
 	
 	private Intent createNotificationIntent(int type)
 	{
 		Intent i = null;
 		String phoneNumber = mData[1];
 		String name;
 		switch (type)
 		{
 		case INCOMING_LOCATION:
 			double[] coordinates = LocationUtility.convertStringToLatLong(mData[2]);
 			name = mData[3];
 			i = new Intent(mContext, AcceptLocationSMSActivity.class);
 			i.putExtra(GlobalConstants.PHONE_NUMBER_KEY, phoneNumber);
 			i.putExtra(GlobalConstants.LOCATION_KEY, coordinates);
 			i.putExtra(GlobalConstants.NAME, name);
 			i.setAction("" + System.currentTimeMillis());
 			
 			mMessage = "Location data sent from " + phoneNumber;
 			mTitle = "New Location from " + phoneNumber;
 			break;
 		case DECLINED_LOCATION:
 			IncomingDBAdapter db = new IncomingDBAdapter(mContext);
 			db.open();
 			Cursor cur = db.fetchEntry(phoneNumber);
 			name = cur.getString(cur.getColumnIndex(IncomingDBAdapter.KEY_NAME));
 			// delete from record. guess she's just not that into you.
 			db.deleteEntry(cur.getInt(cur.getColumnIndex(IncomingDBAdapter.KEY_ROWID)));
 			db.close();
 			i = new Intent(mContext, HomeActivity.class);
 			
 			mMessage = name + " did not accept your location.";
 			mTitle = "Incoming contact accepted";
 			break;
 		case ACCEPTED_LOCATION:
 			db = new IncomingDBAdapter(mContext);
 			db.open();
 			cur = db.fetchEntry(phoneNumber);
 			name = cur.getString(cur.getColumnIndex(IncomingDBAdapter.KEY_NAME));
 			db.updateEntry(cur.getColumnIndex(IncomingDBAdapter.KEY_ROWID),
 					null, null, "true");
 			db.close();
 			i = new Intent(mContext, IncomingListActivity.class);
 			
 			mMessage = name + " accepted your location";
 			mTitle = "Incoming contact accepted";
 		}
 		return i;
 	}
 }
