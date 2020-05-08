 package org.touchirc.fragments;
 
 import org.touchirc.R;
 import org.touchirc.adapter.ExpandableUserAdapter;
 import org.touchirc.irc.IrcService;
 
 import android.annotation.SuppressLint;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ExpandableListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 @SuppressLint("ValidFragment")
 public class ConnectedUsersFragment extends Fragment {
 
 	private static IrcService ircService;
 	private ExpandableUserAdapter userAdapter;
 
 	public ConnectedUsersFragment(){}
 		
 	public ConnectedUsersFragment(IrcService service){
 		ircService = service;
 	}
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		userAdapter = new ExpandableUserAdapter(ircService, getActivity());
 		
 		// Set initial connectedUserCount Textview
    	((TextView)getActivity().findViewById(R.id.connectedUserCount)).setText(getResources().getString(R.string.users) + " (" + ircService.getCurrentChannel().getUsers().size() + ")");
 
 		// construct the RelativeLayout
 		RelativeLayout v = new RelativeLayout(getActivity());
 		
 		// Construct the ListView
 		ExpandableListView usersLV = (ExpandableListView) container.findViewById(R.id.connectedUserExpandableListView);
 		
 		usersLV.setVerticalFadingEdgeEnabled(false);
 
 		// No child Divider
 		usersLV.setChildDivider(getResources().getDrawable(android.R.color.transparent));
 		
 		usersLV.setAdapter(userAdapter);
 		
 		usersLV.setOnChildClickListener(new OnChildClickListener() {
 			
 			@Override
 			public boolean onChildClick(ExpandableListView parent, View v,
 					int groupPosition, int childPosition, long id) {
 				// TODO Display Dialog Box to answer to the user's query
 				return true;
 			}
 		});
 		
 		usersLV.setGroupIndicator(new ColorDrawable(android.R.color.transparent));
 		
 		// v.addView(usersLV);
 		return v;
 	}

 	public ExpandableUserAdapter getAdapter() {
 		return userAdapter;
 	}
 }
