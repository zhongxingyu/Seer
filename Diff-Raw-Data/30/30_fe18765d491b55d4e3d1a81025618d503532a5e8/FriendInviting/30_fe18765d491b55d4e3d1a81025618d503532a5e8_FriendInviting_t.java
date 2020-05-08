 package net.ircubic.eventmap;
 
 import android.app.ListActivity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.widget.SimpleCursorAdapter;
 
 public class FriendInviting extends ListActivity {
 
 	public static final int INVITE = 0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Cursor c = managedQuery(FriendProvider.CONTENT_URI, null, null, null, null);
		String from[] = {FriendProvider.KEY_NAME};
		int to[] = {R.id.friendName};
 		SimpleCursorAdapter ca = new SimpleCursorAdapter(this, R.layout.friend_row, c, from, to);
 		setListAdapter(ca);
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 }
