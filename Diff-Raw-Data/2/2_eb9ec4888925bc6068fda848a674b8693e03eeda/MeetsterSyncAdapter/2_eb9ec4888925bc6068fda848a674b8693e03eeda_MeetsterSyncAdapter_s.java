 package com.russia.meetster.data;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 
 import com.russia.meetster.MeetsterApplication;
 import com.russia.meetster.utils.NetworkUtils;
 import com.russia.meetster.utils.YLSQLRow;
 
 import android.accounts.Account;
 import android.content.AbstractThreadedSyncAdapter;
 import android.content.ContentProviderClient;
 import android.content.Context;
 import android.content.SyncResult;
 import android.os.Bundle;
 
 public class MeetsterSyncAdapter extends AbstractThreadedSyncAdapter {
 	
 	private Context mContext;
 
 	public MeetsterSyncAdapter(Context context, boolean autoInitialize) {
 		super(context, autoInitialize);
 		this.mContext = context;
 	}
 
 	@Override
 	public void onPerformSync(Account account, Bundle extras, String authority,
 			ContentProviderClient provider, SyncResult syncResult) {
 		MeetsterApplication mApp = ((MeetsterApplication) mContext.getApplicationContext());
 		
 		List<MeetsterEvent> unsyncedLocalEvents = MeetsterDataManager.getUnsyncedEvents(mContext);		
 		
 		List<MeetsterEvent> unsyncedRemoteEvents;
 		try {
 			// Get events...
 			unsyncedRemoteEvents = NetworkUtils.syncEvents(mContext, mApp.getCurrentUserId(), mApp.getLastSyncTime(), unsyncedLocalEvents);
 			
 			Date latestEventSynced = new Date(mApp.getLastSyncTime());
 			
 			for (MeetsterEvent e : unsyncedRemoteEvents) {
 				e.setSynced(true);
 				MeetsterDataManager.writeEvent(mContext, e);
 				
 				Date newDate = e.getCreationTime();
 				if (latestEventSynced.before(newDate))
					latestEventSynced = newDate;
 			}
 			
 			mApp.setLastSyncTime(latestEventSynced);
 			
 			// Get any new users we might need from these events
 			List<Long> unsyncedInviters = MeetsterDataManager.getUnsyncedInviterIds(mContext);
 			
 			if (unsyncedInviters.size() > 0) {
 				List<MeetsterFriend> newUsers = NetworkUtils.downloadUsers(unsyncedInviters);
 				for (MeetsterFriend f : newUsers) {
 					MeetsterDataManager.writeUser(mContext, f);
 				}
 			}
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 }
