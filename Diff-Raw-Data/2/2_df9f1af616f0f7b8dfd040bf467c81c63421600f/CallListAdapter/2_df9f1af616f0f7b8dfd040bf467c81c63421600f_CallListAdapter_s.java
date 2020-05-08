 package net.skweez.sipgate;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import net.skweez.sipgate.api.Call;
 import net.skweez.sipgate.api.ECallStatus;
 import net.skweez.sipgate.model.AccountInfo;
 import android.app.Activity;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.ContactsContract.PhoneLookup;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * @author Michael Kanis
  */
 public class CallListAdapter extends BaseAdapter implements Observer {
 
 	static class ViewHolder {
 		ImageView callStatusIcon;
 		TextView numberText;
 		TextView dateText;
 	}
 
 	private final AccountInfo callHistory;
 
 	private final LayoutInflater inflater;
 
 	private final Activity activity;
 
 	private final Map<String, String> contactNamesCache;
 
 	public CallListAdapter(Activity activity, AccountInfo callHistory) {
 		this.activity = activity;
 		this.callHistory = callHistory;
 
 		callHistory.addObserver(this);
 
 		inflater = LayoutInflater.from(activity);
 
 		contactNamesCache = new HashMap<String, String>();
 	}
 
 	public int getCount() {
 		return callHistory.getCallHistorySize();
 	}
 
 	public Call getItem(int position) {
 		return callHistory.getCall(position);
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		ViewHolder holder;
 		if (convertView == null) {
 			convertView = inflater.inflate(R.layout.call_list_item, null);
 
 			holder = new ViewHolder();
 			holder.callStatusIcon = (ImageView) convertView
 					.findViewById(R.id.callStatusIcon);
 			holder.numberText = (TextView) convertView
 					.findViewById(R.id.numberText);
 			holder.dateText = (TextView) convertView
 					.findViewById(R.id.dateText);
 
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		Call call = getItem(position);
 
 		holder.numberText.setText(getContactNameFromNumber(call.getRemoteURI()
				.toString()));
 		holder.dateText.setText(call.getTimestamp().toString());
 		holder.callStatusIcon.setImageResource(getImage(call.getStatus()));
 
 		return convertView;
 	}
 
 	/**
 	 * Returns the display name for a given phone number.
 	 * 
 	 * Code initially copied from
 	 * http://www.vbsteven.be/blog/android-getting-the-contact-name-of-
 	 * a-phone-number/
 	 * 
 	 * @param number
 	 *            The phone number to look up.
 	 * @return The first name for the given number, if more than one exist.
 	 *         <code>number</code> if the name was not found.
 	 */
 	private String getContactNameFromNumber(String number) {
 
 		String name = contactNamesCache.get(number);
 		if (name != null) {
 			return name;
 		}
 
 		// define what the query should return
 		String[] projection = new String[] { PhoneLookup.DISPLAY_NAME };
 
 		Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
 				Uri.encode(number));
 
 		Cursor c = activity.getContentResolver().query(contactUri, projection,
 				null, null, null);
 
 		if (c.moveToFirst()) {
 			name = c.getString(c.getColumnIndex(PhoneLookup.DISPLAY_NAME));
 			contactNamesCache.put(number, name);
 			return name;
 		}
 
 		// return the original number if no match was found
 		return number;
 	}
 
 	/**
 	 * Returns the status image for a given call status (missed, outgoing,
 	 * incoming).
 	 */
 	private int getImage(ECallStatus status) {
 		switch (status) {
 		case ACCEPTED:
 			return R.drawable.ic_call_log_list_incoming_call;
 		case MISSED:
 			return R.drawable.ic_call_log_list_missed_call;
 		case OUTGOING:
 			return R.drawable.ic_call_log_list_outgoing_call;
 		default:
 			throw new IllegalArgumentException();
 		}
 	}
 
 	/** Notifies the view that it needs to update itself. */
 	public void update(Observable observable, Object data) {
 		notifyDataSetChanged();
 	}
 
 }
