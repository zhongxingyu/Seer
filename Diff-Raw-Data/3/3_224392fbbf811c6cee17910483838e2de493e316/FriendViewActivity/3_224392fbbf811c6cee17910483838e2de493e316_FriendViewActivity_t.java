 package android.debtlistandroid;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import console.Main;
 
 import logic.Debt;
 import logic.User;
 
 import requests.FriendRequest;
 import requests.FriendRequest.FriendRequestStatus;
 import session.Session;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.DataSetObserver;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Adapter;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 
 public class FriendViewActivity extends Activity {
 
 	public static final String INCOMING_SEPARATOR_STRING = "incomingSeparatorString";
 	public static final String CONFIRMED_SEPARATOR_STRING = "confirmedSeparatorString";
 	public static final String OUTGOING_SEPARATOR_STRING = "outgoingSeparatorString";
 	public static final String EMPTY_STRING = "empty";
 
 	public ListAdapter adapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_friend_view);
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		List<String> incoming = new ArrayList<String>();
 		List<String> outgoing = new ArrayList<String>();
 		for (int i = 0; i < Session.session.getUser().getNumberOfFriendRequests(); i++) {
 			FriendRequest r = Session.session.getUser().getFriendRequest(i);
 			if(r.getStatus() == FriendRequestStatus.PENDING) {
 				if(r.getFriendUsername().equals(Session.session.getUser().getUsername())) 
 					incoming.add(r.getFromUser().getUsername());
 				else if(r.getFromUser().getUsername().equals(Session.session.getUser().getUsername()))
 					outgoing.add(r.getFriendUsername());
 			}
 		}
 		// Find confirmed friends
 		List<String> friends = new ArrayList<String>();
 		for (int i = 0; i < Session.session.getUser().getNumberOfFriends(); i++) {
 			friends.add(Session.session.getUser().getFriend(i).getUsername());
 		}
 		// Concatenate lists
 		List<String> strings = new ArrayList<String>();
 		if(!incoming.isEmpty()) {
 			strings.add(INCOMING_SEPARATOR_STRING);
 			strings.addAll(incoming);
 		}
 		if(!friends.isEmpty()) {
 			strings.add(CONFIRMED_SEPARATOR_STRING);
 			strings.addAll(friends);
 		}
 		if(!outgoing.isEmpty()) {
 			strings.add(OUTGOING_SEPARATOR_STRING);
 			strings.addAll(outgoing);
 		}
 
 		// If no friends or friend requests, add an empty string to show the info
 		if(strings.isEmpty()) {
 			strings.add(EMPTY_STRING);
 		}
 		
 		// Set adapter
 		this.adapter = new FriendAdapter(this, R.layout.friend_list_view, strings);
 		((ListView) findViewById(R.id.friend_view_list)).setAdapter(this.adapter);
 	}
 
 	class FriendAdapter extends ArrayAdapter<String> {
 
 		private Context context;
 		private List<String> objects;
 		private View lastExpanded;
 		private boolean confirmedSeparatorHasBeenPlaced = false;
 
 		public FriendAdapter(Context context, int resource, List<String> objects) {
 			super(context, resource, objects);
 			this.context = context;
 			this.objects = objects;
 			this.lastExpanded = null;
 		}
 
 		/**
 		 * @return	The selected user name, or null if none are selected
 		 */
 		public String getSelectedUsername() {
 			if (lastExpanded == null) {
 				return null;
 			} else {
 				return ((TextView) lastExpanded.findViewById(R.id.friend_view_user_name)).getText().toString();
 			}
 		}
 		
 		@Override
 		public View getView(int pos, View convertView, ViewGroup parent) {
 			View v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.friend_list_view, null);
 			// Display message if there are no registered friends or friend requests
 			if(objects.size() == 1 && objects.get(0) == EMPTY_STRING) {
 				System.out.println("ADDING INFORMATION STRING");
 				TextView t = (TextView) v.findViewById(R.id.friend_view_user_name);
 				t.setText(v.getResources().getString(R.string.friend_view_no_friends));
 				// Set the color to white
 				t.setBackgroundColor(Color.WHITE);
 				// Set the text color to black
 				t.setTextColor(Color.BLACK);
 				// Set it as visible
 				t.setVisibility(View.VISIBLE);
 			} else {
 				System.out.println("NOT ADDING INFORMATION STRING, BECAUSE WE HAVE: " + objects.get(pos));
 				if(pos == 0) {
 					confirmedSeparatorHasBeenPlaced = false;
 				}
 				TextView sep = (TextView) v.findViewById(R.id.friend_view_separator);
 				sep.setVisibility(View.VISIBLE);
 				String s = objects.get(pos); 
 				if(s == INCOMING_SEPARATOR_STRING) {
 					sep.setText(v.getResources().getString(R.string.friend_view_incoming_separator));
 				} else if(s == CONFIRMED_SEPARATOR_STRING) {
 					sep.setText(v.getResources().getString(R.string.friend_view_confirmed_separator));
 					confirmedSeparatorHasBeenPlaced = true;
 				} else if(s == OUTGOING_SEPARATOR_STRING) {
 					// In case we have no confirmed debts
 					confirmedSeparatorHasBeenPlaced = true;
 					sep.setText(v.getResources().getString(R.string.friend_view_outgoing_separator));
 				} else {
 					sep.setVisibility(View.GONE);
 					TextView t = (TextView) v.findViewById(R.id.friend_view_user_name);
 					t.setVisibility(View.VISIBLE);
 					t.setText(s);
 					// Add buttons for incoming requests
 					System.out.println("Confirmed separator has been placed: " + confirmedSeparatorHasBeenPlaced);
 					v.setOnClickListener(new FriendOnClickListener(!confirmedSeparatorHasBeenPlaced));
 				}
 			}
 			return v;
 		}
 
 		class FriendOnClickListener implements OnClickListener {
 			
 			private boolean isExpandable; 
 			
 			public FriendOnClickListener(boolean isExpandable) {
 				this.isExpandable = isExpandable;
 				System.out.println("Is expandable: " + isExpandable);
 			}
 			
 			@Override
 			public void onClick(View v) {
 				// Hide previously displayed buttons if any
 				if(lastExpanded != null) {
 					lastExpanded.findViewById(R.id.friend_view_accept_incoming).setVisibility(View.GONE);
 					lastExpanded.findViewById(R.id.friend_view_decline_incoming).setVisibility(View.GONE);
 				}
 				if(lastExpanded != v && this.isExpandable) {
 					// Show buttons
 					v.findViewById(R.id.friend_view_accept_incoming).setVisibility(View.VISIBLE);
 					v.findViewById(R.id.friend_view_decline_incoming).setVisibility(View.VISIBLE);
 					lastExpanded = v;
 				} else {
 					System.out.println("Not expandable! " + isExpandable);
 					lastExpanded = null;
 				}
 			}
 		}
 		
 		public void collapse(View v) {
 			v.findViewById(R.id.friend_view_accept_incoming).setVisibility(View.GONE);
 			v.findViewById(R.id.friend_view_decline_incoming).setVisibility(View.GONE);
 		}
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_friend_view, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.friend_view_menu_add_friend:
 			startActivity(new Intent(this, AddFriendView.class));
 			break;
 		case R.id.friend_view_menu_debts:
 			startActivity(new Intent(this, DebtViewActivity.class));
			break;
 		case R.id.friend_view_menu_settings:
 			startActivity(new Intent(this, SettingsActivity.class));
 			break;
 		}
 		return true;
 	}
 
 	public void accept_incoming_friend(View v) {
 		Main.processAcceptDeclineFriend("accept friend " + ((FriendAdapter) adapter).getSelectedUsername());
 //		System.out.println("accept friend " + ((FriendAdapter) adapter).getSelectedUsername());
 		recreate();
 	}
 	
 	public void decline_incoming_friend(View v) {
 		Main.processAcceptDeclineFriend("decline friend " + ((FriendAdapter) adapter).getSelectedUsername());
 //		System.out.println("decline friend " + ((FriendAdapter) adapter).getSelectedUsername());
 		recreate();
 	}
 }
