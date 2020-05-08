 package edu.upenn.cis350;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.parse.FindCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 
 public class ShowNotifications extends ListActivity {
 
 	private Filter filter;
 	
 	private enum Filter {
 		ALL, NEW;
 	}
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Parse.initialize(this, Settings.APPLICATION_ID, Settings.CLIENT_ID);
 
 		final ProgressDialog dialog = ProgressDialog.show(this, "", 
 				"Loading. Please wait...", true);
 		dialog.setCancelable(true);
 
 		ParseUser user = ParseUser.getCurrentUser();
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			filter = (Filter) extras.get("filter");
 		}
 		if (filter == null) {
 			filter = Filter.NEW;
 		}
 		
 		ParseQuery query = new ParseQuery("Notification");
 		
 		if (Filter.NEW.equals(filter)) {
			query.setLimit(10);
 		} else if (Filter.ALL.equals(filter)) {
 			// no-op
 		}
 		
 		query.whereEqualTo("user", user.getObjectId());
 		query.addAscendingOrder("isRead");
		query.addDescendingOrder("createdAt");
 
 		query.findInBackground(new FindCallback() {
 
 			@Override
 			public void done(List<ParseObject> notifications, ParseException e) {
 				if (e == null) {
 					ArrayList<ListItem> newNotifications = new ArrayList<ListItem>();
 					ArrayList<ListItem> readNotifications = new ArrayList<ListItem>();
 					for (ParseObject n : notifications) {
 						if (!n.getBoolean("isRead")) {
 							if ("event".equals(n.getString("type"))) {
 								newNotifications.add(new ListItem(n, ListItem.Type.EVENT));								
 							} else if ("message".equals(n.getString("type"))) {
 								newNotifications.add(new ListItem(n, ListItem.Type.MESSAGE));
 							} else {
 								newNotifications.add(new ListItem(n, ListItem.Type.NONE));
 							}
 						} else {
 							if ("event".equals(n.getString("type"))) {
 								readNotifications.add(new ListItem(n, ListItem.Type.EVENT));								
 							} else if ("message".equals(n.getString("type"))) {
 								readNotifications.add(new ListItem(n, ListItem.Type.MESSAGE));
 							} else {
 								readNotifications.add(new ListItem(n, ListItem.Type.NONE));
 							}
 						}
 					}
 					List<ListItem> list = new ArrayList<ListItem>();
 					list.add(new ListItem("New Notifications", ListItem.Type.HEADER));
 					list.addAll(newNotifications);
 					list.add(new ListItem("Read Notifications", ListItem.Type.HEADER));
 					list.addAll(readNotifications);
 
 					setListAdapter(new NotificationAdapter(getApplicationContext(), list));
 				} else {
 					Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
 					return;
 				}
 				dialog.cancel();					
 			}
 		});
 
 	}
 
 	@Override
 	public void onResume() {
 		onCreate(new Bundle());
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.notifications_menu, menu);
 		return true;
 	}
 	
 	/**
 	 * Method that gets called when the menuitem is clicked
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int id = item.getItemId();
 		if (id == R.id.refresh){
 			Intent i = new Intent(this, ShowNotifications.class);
 			finish();
 			startActivity(i);
 			return true;
 		} else if (id == R.id.markNotificationsRead) {
 			ParseQuery query = new ParseQuery("Notification");
 			query.whereEqualTo("user", ParseUser.getCurrentUser().getObjectId());
 			query.whereEqualTo("isRead", false);
 			query.findInBackground(new FindCallback() {
 
 				@Override
 				public void done(List<ParseObject> notes, ParseException e) {
 					if (e == null) {
 						for (ParseObject obj : notes) {
 							obj.put("isRead", true);
 							obj.saveEventually();
 						}
 					}
 				}
 				
 			});
 			Intent i = new Intent(this, ShowNotifications.class);
 			finish();
 			startActivity(i);
 			return true;
 		} else if (id == R.id.showAllNotifications) {
 			Intent i = new Intent(this, ShowNotifications.class);
 			i.putExtra("filter", Filter.ALL);
 			finish();
 			startActivity(i);
 			return true;
 		}
 		return false;
 	}
 	
 	private class NotificationAdapter extends ArrayAdapter<ListItem> {
 
 		private List<ListItem> notifications;
 
 		public NotificationAdapter(Context context, List<ListItem> notifications) {
 			super(context, 0, notifications);
 			this.notifications = notifications;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 			final ListItem item = notifications.get(position);
 
 			if (item != null) {
 				if (ListItem.Type.HEADER.equals(item.getType())) {
 					/* This is a section header */
 					String title = (String) item.getData();
 					v = vi.inflate(R.layout.list_divider, null);
 
 					v.setOnClickListener(null);
 					v.setOnLongClickListener(null);
 					v.setLongClickable(false);
 
 					final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
 					sectionView.setText(title);
 
 				} else if (ListItem.Type.EVENT.equals(item.getType())){
 					/* This is a real list item */
 					final ParseObject notification = (ParseObject) item.getData();
 					v = vi.inflate(R.layout.notification_list_item, null);
 
 					TextView text = (TextView) v.findViewById(R.id.notificationText);
 					text.setText(notification.getString("text"));
 
 					v.setOnClickListener(new OnClickListener() {
 
 						@Override
 						public void onClick(View v) {
 							Intent i = new Intent(getApplicationContext(), ShowEvent.class);
 							i.putExtra("eventKey", notification.getString("id"));
 							notification.put("isRead", true);
 							notification.saveEventually();
 							startActivity(i);
 						}
 
 					});
 				} else if (ListItem.Type.MESSAGE.equals(item.getType())) {
 					final ParseObject notification = (ParseObject) item.getData();
 					v = vi.inflate(R.layout.notification_list_item, null);
 
 					TextView text = (TextView) v.findViewById(R.id.notificationText);
 					text.setText(notification.getString("text"));
 
 					v.setOnClickListener(new OnClickListener() {
 
 						@Override
 						public void onClick(View v) {
 							Intent i = new Intent(getApplicationContext(), ShowMessage.class);
							i.putExtra("messageID", notification.getString("id"));
 							notification.put("isRead", true);
 							notification.saveEventually();
 							startActivity(i);
 						}
 
 					});
 				} else if (ListItem.Type.NONE.equals(item.getType())) {
 					final ParseObject notification = (ParseObject) item.getData();
 					v = vi.inflate(R.layout.notification_list_item, null);
 
 					TextView text = (TextView) v.findViewById(R.id.notificationText);
 					text.setText(notification.getString("text"));
 
 					v.setOnClickListener(null);
 					v.setOnLongClickListener(null);
 					v.setLongClickable(false);
 				}
 			}
 			return v;
 		}
 	}
 
 }
