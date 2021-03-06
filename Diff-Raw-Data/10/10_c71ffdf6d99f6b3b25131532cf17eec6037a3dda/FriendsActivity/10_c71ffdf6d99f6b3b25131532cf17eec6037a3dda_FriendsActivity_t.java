 package edu.berkeley.cs160.theccertservice.splist;
 
 import java.util.HashMap;
 
 import edu.berkeley.cs160.theccertservice.splist.FeedActivity.CreateFeedDialog;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ExpandableListView.OnChildClickListener;
 
 public class FriendsActivity extends Activity {
 
 	static ExpandableListView exv;
 	Button addFriend;
 	EditText friendEmail;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.friends);
 		displayFeeds();
 		addFriend = (Button) findViewById(R.id.AddFriend);
 		friendEmail = (EditText) findViewById(R.id.friendEmail);
 		addFriend.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("auth_token",MainActivity.authToken);
 				data.put("email",friendEmail.getText().toString());
 				MainActivity.server.requestFriend(data);
 				friendEmail.setText("");
 				Toast.makeText(v.getContext(),"Your friend request has been sent",Toast.LENGTH_SHORT).show();
 
 			}
 		});
 	}
 	
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater=getMenuInflater();
 	    inflater.inflate(R.menu.activity_main, menu);
 	    return super.onCreateOptionsMenu(menu);
 
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch(item.getItemId())
 	    {
 	    case R.id.logout:
 	    	//MainActivity.authToken = null;
 	    	HashMap<String, String> data = new HashMap<String, String>();
 			data.put("auth_token", MainActivity.authToken);
 	    	MainActivity.server.logout(data);
 	    	
 	    	
 	    	SharedPreferences.Editor editor = MainActivity.settings.edit();
             editor.putString("token", null);
             editor.commit();
             MainActivity.authToken = null;
             
 	    	Intent intent = new Intent(FriendsActivity.this, MainActivity.class);
 	    	startActivity(intent);
 	    	return true;
 	       
     	default:
             return super.onOptionsItemSelected(item);
 
 	    }
 
 	}
 	
 	public void friendRequest(View view) {
 		
 	}
 	
 	private void displayFeeds(){
 		exv = (ExpandableListView)findViewById(R.id.expandableListView2);
 		exv.setAdapter(new FriendAdapter(this));
 		exv.setOnChildClickListener(new OnChildClickListener(){
 
 			@Override
 			public boolean onChildClick(ExpandableListView parent, View v,
 					int groupPosition, int childPosition, long id) {
 				if(groupPosition==0){
 					showFeedDialog(groupPosition, childPosition);
 					exv.collapseGroup(groupPosition);  
 					exv.expandGroup(groupPosition);
 					exv.collapseGroup(groupPosition+1);  
 					exv.expandGroup(groupPosition+1);
 				}
 				return false;
 			}
 			
 		});
 	
 	}
 	
 	public void showFeedDialog(int groupPosition, int childPosition) {
 	    DialogFragment newFragment = new CreateFeedDialog(groupPosition, childPosition);
 	    newFragment.show(getFragmentManager(), "createFeedDialog");
 	}
 	
 	public static void refreshEXV() {
 		if (exv != null) {
 			FriendsActivity.exv.collapseGroup(0);  
 			FriendsActivity.exv.expandGroup(0);
 			FriendsActivity.exv.collapseGroup(1);  
 			FriendsActivity.exv.expandGroup(1);
 		}
 	}
 	
 	public class CreateFeedDialog extends DialogFragment {
 		
 		int groupPosition;
 		int childPosition;
 		TextView requestEmail;
 		Button requestAccept;
 		Button requestReject;
 		
 		public CreateFeedDialog(int groupPosition, int childPosition) {
 			// Empty constructor required for DialogFragment
 			this.groupPosition = groupPosition;
 			this.childPosition = childPosition;
 		}
 		
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View view = inflater.inflate(R.layout.friend_request, container);
 			requestEmail = (TextView) view.findViewById(R.id.requestEmail);
 			requestEmail.setText(FriendAdapter.friendsRequest.get(childPosition).email);
 			
 			requestAccept = (Button) view.findViewById(R.id.requestAccept);
 			requestReject = (Button) view.findViewById(R.id.requestReject);
 			
 			requestAccept.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					HashMap<String, String> data = new HashMap<String, String>();
 					data.put("auth_token",MainActivity.authToken);
 					data.put("email", FriendAdapter.friendsRequest.get(childPosition).email);
 					MainActivity.server.acceptFriend(data);
					Friend f = FriendAdapter.friendsRequest.get(childPosition);
					FriendAdapter.friendsRequest.remove(f);
					FriendAdapter.friends.add(f);
					
 					exv.collapseGroup(groupPosition);  
 					exv.expandGroup(groupPosition);
					exv.collapseGroup(groupPosition+1);  
					exv.expandGroup(groupPosition+1);
					done();
 				}
 				
 			});
 					
 			requestReject.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					HashMap<String, String> data = new HashMap<String, String>();
 					data.put("auth_token",MainActivity.authToken);
 					data.put("id", String.valueOf(FriendAdapter.friendsRequest.get(childPosition).id));
 					MainActivity.server.removeFriend(data);	
 					FriendAdapter.friendsRequest.remove(FriendAdapter.friendsRequest.get(childPosition));
 					done();
 					exv.collapseGroup(groupPosition);  
 					exv.expandGroup(groupPosition);
 				}			
 			});
 	
 			return view;
 		}
 		
 		public void done() {
 			this.dismiss();
 		}
 	}
 }
