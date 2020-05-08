 package net.skweez.sipgate.activity;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.skweez.sipgate.CallUtils;
 import net.skweez.sipgate.R;
import net.skweez.sipgate.R.id;
import net.skweez.sipgate.R.layout;
 import net.skweez.sipgate.api.Call;
 import net.skweez.sipgate.db.DataSource;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.ContactsContract.PhoneLookup;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class CallListAdapter extends CursorAdapter {
 
 	private static class ViewHolder {
 		ImageView callStatusIcon;
 		TextView numberText;
 		TextView dateText;
 	}
 
 	private final LayoutInflater inflater;
 
 	/** Used to resolve contact names for phone numbers. */
 	private final ContentResolver contentResolver;
 
 	/** Holds cached names for phone numbers. */
 	private final Map<String, String> contactNamesCache;
 
 	public CallListAdapter(Context context, Cursor cursor) {
 		super(context, cursor);
 		inflater = LayoutInflater.from(context);
 		contentResolver = context.getContentResolver();
 		contactNamesCache = new HashMap<String, String>();
 	}
 
 	@Override
 	public View newView(Context context, Cursor cursor, ViewGroup parent) {
 		View view = inflater.inflate(R.layout.call_list_item, null);
 		CallListAdapter.ViewHolder holder = new CallListAdapter.ViewHolder();
 		holder.numberText = (TextView) view.findViewById(R.id.numberText);
 		holder.dateText = (TextView) view.findViewById(R.id.dateText);
 		holder.callStatusIcon = (ImageView) view
 				.findViewById(R.id.callStatusIcon);
 		view.setTag(holder);
 		return view;
 	}
 
 	@Override
 	public void bindView(View view, Context context, Cursor cursor) {
 		CallListAdapter.ViewHolder holder = (CallListAdapter.ViewHolder) view
 				.getTag();
 		Call call = DataSource.getCallFromCursor(cursor);
 		holder.numberText.setText(resolveName(call.getRemoteNumber()));
 		holder.dateText.setText(call.getTimestamp().toString());
 		holder.callStatusIcon.setImageResource(CallUtils.getImage(call
 				.getStatus()));
 	}
 
 	/**
 	 * Returns the display name for a given phone number.
 	 * 
 	 * @param number
 	 *            The phone number to look up.
 	 * @return The first name for the given number, if more than one exist. The
 	 *         <code>number</code> if the name was not found.
 	 */
 	private String resolveName(String number) {
 
 		String name = contactNamesCache.get(number);
 		if (name != null) {
 			return name;
 		}
 
 		String[] projection = new String[] { PhoneLookup.DISPLAY_NAME };
 
 		Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
 				Uri.encode(number));
 
 		Cursor c = contentResolver.query(contactUri, projection, null, null,
 				null);
 
 		if (c.moveToFirst()) {
 			name = c.getString(c.getColumnIndex(PhoneLookup.DISPLAY_NAME));
 			contactNamesCache.put(number, name);
 			return name;
 		}
 
 		// return the original number if no match was found
 		return number;
 	}
 
 	public Call getCall(int position) {
 		return DataSource.getCallFromCursor((Cursor) getItem(position));
 	}
 }
