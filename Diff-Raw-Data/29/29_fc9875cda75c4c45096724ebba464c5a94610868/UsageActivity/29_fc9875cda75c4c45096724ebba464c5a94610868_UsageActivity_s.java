 /*
 	Copyright (C) 2010 Ben Van Daele (vandaeleben@gmail.com)
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package be.benvd.mvforandroid;
 
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.Contacts;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.DatePicker;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import be.benvd.mvforandroid.data.DatabaseHelper;
 import be.benvd.mvforandroid.data.MVDataService;
 import be.benvd.mvforandroid.data.DatabaseHelper.Usage;
 
 import com.commonsware.cwac.wakeful.WakefulIntentService;
 
 @SuppressWarnings("deprecation")
 public class UsageActivity extends Activity {
 
 	public DatabaseHelper helper;
 
 	private BroadcastReceiver updatedReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			model.requery();
 			UsageActivity.this.getParent().setProgressBarIndeterminateVisibility(false);
 		}
 	};
 
 	private BroadcastReceiver exceptionReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, getString(R.string.exception_message), Toast.LENGTH_SHORT).show();
 			UsageActivity.this.getParent().setProgressBarIndeterminateVisibility(false);
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.usage);
 
 		helper = new DatabaseHelper(this);
 		updateView();
 	}
 
 	public void updateView() {
 		Button updateButton = (Button) findViewById(R.id.update_button);
 		updateButton.setOnClickListener(new OnClickListener() {
 			private OnDateSetListener datePicked = new OnDateSetListener() {
 				@Override
 				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 					Calendar lower = GregorianCalendar.getInstance();
 					lower.set(Calendar.YEAR, year);
 					lower.set(Calendar.MONTH, monthOfYear);
 					lower.set(Calendar.DAY_OF_MONTH, dayOfMonth);
 					lower.set(Calendar.HOUR_OF_DAY, 0);
 					lower.set(Calendar.MINUTE, 0);
 					lower.set(Calendar.SECOND, 0);
 
 					Calendar upper = (Calendar) lower.clone();
 					upper.add(Calendar.DAY_OF_MONTH, 1);
 
 					UsageActivity.this.getParent().setProgressBarIndeterminateVisibility(true);
 					Intent i = new Intent(UsageActivity.this, MVDataService.class);
 					i.setAction(MVDataService.UPDATE_USAGE);
 					i.putExtra(MVDataService.UPDATE_USAGE_STARTTIME, lower.getTimeInMillis());
 					i.putExtra(MVDataService.UPDATE_USAGE_ENDTIME, upper.getTimeInMillis());
 					WakefulIntentService.sendWakefulWork(UsageActivity.this, i);
 				}
 			};
 
 			@Override
 			public void onClick(View v) {
 				Calendar c = GregorianCalendar.getInstance();
 				DatePickerDialog datePicker = new DatePickerDialog(UsageActivity.this, datePicked,
 						c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
 				datePicker.show();
 			}
 		});
 
 		ListView usageList = (ListView) findViewById(R.id.usage_list);
 		model = helper.usage.get(Usage.ORDER_BY_DATE, false);
 		usageList.setAdapter(new UsageAdapter(model));
 
 		OnItemClickListener onItemClickListener = new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Cursor c = helper.usage.get(id);
 				if (c.moveToFirst()) {
 					if (helper.usage.getType(c) == Usage.TYPE_DATA)
 						return;
 
 					String contact = helper.usage.getContact(c);
 					String contactId = getContactIdFromNumber(UsageActivity.this, contact);
 					if (!contactId.equals("")) {
 						Intent intent = new Intent();
 						intent.setAction(Intent.ACTION_VIEW);
 						intent.setData(Uri.withAppendedPath(Contacts.People.CONTENT_URI, Uri.encode(contactId)));
 						startActivity(intent);
 					} else {
 						Intent intent = new Intent();
 						intent.setAction(Intent.ACTION_DIAL);
 						intent.setData(Uri.parse("tel:" + contact));
 						startActivity(intent);
 					}
 				}
 				c.close();
 			}
 		};
 		usageList.setOnItemClickListener(onItemClickListener);
 	}
 
 	public static String getContactIdFromNumber(Context context, String number) {
 		// define the columns I want the query to return
 		String[] projection = new String[] { Contacts.People._ID };
 
 		// encode the phone number and build the filter URI
 		Uri contactUri = Uri.withAppendedPath(Contacts.People.CONTENT_FILTER_URI, Uri.encode(getContactNameFromNumber(
 				context, number)));
 
 		// query time
 		Cursor c = context.getContentResolver().query(contactUri, projection, null, null, null);
 
 		// if the query returns 1 or more results
 		// return the first result
 		if (c.moveToFirst()) {
 			String id = c.getString(c.getColumnIndex(Contacts.People._ID));
 			return id;
 		}
 
 		// return empty string if not found
 		return "";
 	}
 
 	public static String getContactNameFromNumber(Context context, String number) {
 		// define the columns I want the query to return
 		String[] projection = new String[] { Contacts.Phones.DISPLAY_NAME, Contacts.Phones.NUMBER };
 
 		// encode the phone number and build the filter URI
 		Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(number));
 
 		// query time
 		Cursor c = context.getContentResolver().query(contactUri, projection, null, null, null);
 
 		// if the query returns 1 or more results
 		// return the first result
 		if (c.moveToFirst()) {
 			String name = c.getString(c.getColumnIndex(Contacts.Phones.DISPLAY_NAME));
 			return name;
 		}
 
 		// return the original number if no match was found
 		return number;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(updatedReceiver, new IntentFilter(MVDataService.USAGE_UPDATED));
 		registerReceiver(exceptionReceiver, new IntentFilter(MVDataService.EXCEPTION));
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(updatedReceiver);
 		unregisterReceiver(exceptionReceiver);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		helper.close();
 	}
 
 	private static DecimalFormat currencyFormat = new DecimalFormat("#.##");
 	private static SimpleDateFormat formatTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
 	private static DecimalFormat dataFormat = new DecimalFormat("#.##");
 
 	private Cursor model;
 
 	private static String formatCurrency(double amount) {
 		return currencyFormat.format(amount) + "€";
 
 	}
 
 	private static String formatTime(long timestamp) {
 		return formatTime.format(new Date(timestamp));
 	}
 
 	private static String formatBytes(Context c, long bytes) {
 		if (bytes < 1048576) {
 			return dataFormat.format((double) bytes / 1024) + " " + c.getString(R.string.kilobytes);
 		} else {
 			return dataFormat.format((double) bytes / 1048576) + " " + c.getString(R.string.megabytes);
 		}
 	}
 
 	private static String formatDuration(long duration) {
 		int hours = (int) (duration / 3600);
 		int minutes = (int) ((duration / 60) - (hours * 60));
 		int seconds = (int) (duration % 60);
 		String result = "";
 		if (hours != 0)
 			result += String.format("%02d:", hours);
 		result += String.format("%02d:%02d", minutes, seconds);
 		return result;
 	}
 
 	static class UsageHolder {
 		private ImageView logo = null;
 		private TextView title = null, cost = null, date = null, duration = null;
 
 		UsageHolder(View listItem) {
 			logo = (ImageView) listItem.findViewById(R.id.usage_logo);
 			title = (TextView) listItem.findViewById(R.id.usage_title);
 			cost = (TextView) listItem.findViewById(R.id.usage_cost);
 			date = (TextView) listItem.findViewById(R.id.usage_date);
 			duration = (TextView) listItem.findViewById(R.id.usage_duration);
 		}
 
 		void populateFrom(Cursor c, DatabaseHelper helper) {
 			cost.setText(formatCurrency(helper.usage.getCost(c)));
 			date.setText(formatTime(helper.usage.getTimestamp(c)));
 			duration.setText("xx");
 
 			switch (helper.usage.getType(c)) {
 				case DatabaseHelper.Usage.TYPE_DATA: {
 					logo.setImageResource(R.drawable.credit_data);
 					title.setText("Data");
 					duration.setText(formatBytes(duration.getContext(), helper.usage.getduration(c)));
 					break;
 				}
 				case DatabaseHelper.Usage.TYPE_MMS: {
 					logo.setImageResource(R.drawable.credit_sms);
 					title.setText(getContactNameFromNumber(title.getContext(), helper.usage.getContact(c)));
 					duration.setText("");
 					break;
 				}
 				case DatabaseHelper.Usage.TYPE_SMS: {
 					if (helper.usage.isIncoming(c))
 						logo.setImageResource(R.drawable.sms_incoming);
 					else
 						logo.setImageResource(R.drawable.sms_outgoing);
 					title.setText(getContactNameFromNumber(title.getContext(), helper.usage.getContact(c)));
 					duration.setText("");
 					break;
 				}
 				case DatabaseHelper.Usage.TYPE_VOICE: {
 					if (helper.usage.isIncoming(c))
 						logo.setImageResource(R.drawable.call_incoming);
 					else
 						logo.setImageResource(R.drawable.call_outgoing);
 					title.setText(getContactNameFromNumber(title.getContext(), helper.usage.getContact(c)));
 					duration.setText(formatDuration(helper.usage.getduration(c)));
 					break;
 				}
 			}
 		}
 	}
 
 	class UsageAdapter extends CursorAdapter {
 
 		UsageAdapter(Cursor c) {
 			super(UsageActivity.this, c);
 		}
 
 		@Override
 		public void bindView(View row, Context ctxt, Cursor c) {
 			UsageHolder holder = (UsageHolder) row.getTag();
 			holder.populateFrom(c, helper);
 		}
 
 		@Override
 		public View newView(Context ctxt, Cursor c, ViewGroup parent) {
 			LayoutInflater inflater = getLayoutInflater();
 			View listItem = inflater.inflate(R.layout.usage_list_item, parent, false);
 			UsageHolder holder = new UsageHolder(listItem);
 			listItem.setTag(holder);
 			return listItem;
 		}
 	}
 
 }
