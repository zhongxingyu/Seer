 package edu.berkeley.cs160.theccertservice.splist;
 
 import java.util.HashMap;
 
 import edu.berkeley.cs160.theccertservice.splist.FeedActivity.CreateFeedDialog;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
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
 				// TODO Auto-generated method stub
 				
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("auth_token",MainActivity.authToken);
 				data.put("email",friendEmail.getText().toString());
 				MainActivity.server.requestFriend(data);
 				friendEmail.setText("");
 				Toast.makeText(v.getContext(),"Your friend request has been sent",Toast.LENGTH_SHORT).show();
 
 			}
 		});
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
 				// TODO Auto-generated method stub
 				if(groupPosition==0){
 					//String name = FriendAdapter.friendsRequest.get(childPosition);
 					//if(!FriendAdapter.friends.contains(name)){
 						//FriendAdapter.friendsRequest.remove(name);
 						//FriendAdapter.friends.add(name);
 						
 					//}
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
 					// TODO Auto-generated method stub
 					HashMap<String, String> data = new HashMap<String, String>();
 					data.put("auth_token",MainActivity.authToken);
 					data.put("email", FriendAdapter.friendsRequest.get(childPosition).email);
 					MainActivity.server.acceptFriend(data);
 				}
 				
 			});
 			
 			
 			requestReject.setOnClickListener(new View.OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 			});
 			
 			return view;
 		}
 	}
 }
