 package com.mridang.callstats;
 
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.CallLog.Calls;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /*
  * This class is the main class that provides the widget
  */
 public class CallstatsWidget extends DashClockExtension {
 
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
 	 */
 	public void onCreate() {
 
 		super.onCreate();
 		Log.d("CallstatsWidget", "Created");
 		BugSenseHandler.initAndStartSession(this, "a83e054b");
 
 	}
 
 	/*
 	 * @see
 	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
 	 * (int)
 	 */
 	@Override
 	protected void onUpdateData(int arg0) {
 
 		setUpdateWhenScreenOn(true);
 
 		Log.d("CallstatsWidget", "Calculating call statistics");
 		ExtensionData edtInformation = new ExtensionData();
 		edtInformation.visible(true);
 
 		try {
 
 	        Cursor curCalls = getContentResolver().query(Uri.parse("content://call_log/calls"), null, null, null, null);
 
 	        Integer intIncoming = 0;
 	        Integer intTotal = 0;
 	        Integer intOutgoing = 0;
 	        
 	        while (curCalls.moveToNext()) {
 
 	        	switch (curCalls.getInt(curCalls.getColumnIndex(Calls.TYPE))) {
 
 		        	case Calls.INCOMING_TYPE:
 		        		intIncoming = intIncoming + curCalls.getInt(curCalls.getColumnIndex(Calls.DURATION));
 		        		intTotal = intTotal + curCalls.getInt(curCalls.getColumnIndex(Calls.DURATION));
 		        		break;
 	
 		        	case Calls.OUTGOING_TYPE:
 		        		intOutgoing = intOutgoing + curCalls.getInt(curCalls.getColumnIndex(Calls.DURATION));
 		        		intTotal = intTotal + curCalls.getInt(curCalls.getColumnIndex(Calls.DURATION));
 		        		break;
 
 	        	}
 
 	        }
 	        
 	        String strIncoming = "";
 	        String strTotal = "";
 	        String strOutgoing = "";
 	        
 	        intIncoming = intIncoming / 60;
 			if (intIncoming < 60) {
 				strIncoming = getResources().getQuantityString(
 						R.plurals.minutes, intIncoming, intIncoming);
 			} else {
 				strIncoming = getResources().getQuantityString(R.plurals.hours,
 						intIncoming / 60, intIncoming / 60);
 				if (intIncoming % 60 > 0) {
 					strIncoming = String.format(
 							getString(R.string.and),
 							strIncoming,
 							getResources().getQuantityString(R.plurals.minutes,
 									intIncoming % 60, intIncoming % 60));
 				}
 			}
 			Log.v("CallstatsWidget", "Incoming : " + intIncoming);
 			Log.d("CallstatsWidget", "Incoming : " + strIncoming);
 
 			intOutgoing = intOutgoing / 60;
 			if (intOutgoing < 60) {
 				strOutgoing = getResources().getQuantityString(
 						R.plurals.minutes, intOutgoing, intOutgoing);
 			} else {
 				strOutgoing = getResources().getQuantityString(R.plurals.hours,
 						intOutgoing / 60, intOutgoing / 60);
 				if (intOutgoing % 60 > 0) {
 					strOutgoing = String.format(
 							getString(R.string.and),
 							strOutgoing,
 							getResources().getQuantityString(R.plurals.minutes,
 									intOutgoing % 60, intOutgoing % 60));
 				}
 			}
 			Log.v("CallstatsWidget", "Outgoing : " + intOutgoing);
 			Log.d("CallstatsWidget", "Outgoing : " + strOutgoing);
 
			intTotal = intTotal / 60;
 			if (intTotal < 60) {
 				strTotal = getResources().getQuantityString(
 						R.plurals.minutes, intTotal, intTotal);
 			} else {
 				strTotal = getResources().getQuantityString(R.plurals.hours,
 						intTotal / 60, intTotal / 60);
 				if (intTotal % 60 > 0) {
 					strTotal = String.format(
 							getString(R.string.and),
 							strTotal,
 							getResources().getQuantityString(R.plurals.minutes,
 									intTotal % 60, intTotal % 60));
 				}
 			}
 			Log.v("CallstatsWidget", "Total : " + intTotal);
 			Log.d("CallstatsWidget", "Total : " + strTotal);
 			
 			edtInformation
 					.expandedBody((edtInformation.expandedBody() == null ? ""
 							: edtInformation.expandedBody() + "\n")
 							+ String.format(getString(R.string.incoming),
 									strIncoming));
 			edtInformation.status(String.format(
 					getString(R.string.total_calls), strTotal));
 			edtInformation
 					.expandedBody((edtInformation.expandedBody() == null ? ""
 							: edtInformation.expandedBody() + "\n")
 							+ String.format(getString(R.string.outgoing),
 									strOutgoing));
 
 		} catch (Exception e) {
 			Log.e("CallstatsWidget", "Encountered an error", e);
 			BugSenseHandler.sendException(e);
 		}
 
 		edtInformation.icon(R.drawable.ic_dashclock);
 		publishUpdate(edtInformation);
 		Log.d("CallstatsWidget", "Done");
 
 	}
 	
 	/*
 	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
 	 */
 	public void onDestroy() {
 
 		super.onDestroy();
 		Log.d("CallstatsWidget", "Destroyed");
 		BugSenseHandler.closeSession(this);
 
 	}
 
 }
