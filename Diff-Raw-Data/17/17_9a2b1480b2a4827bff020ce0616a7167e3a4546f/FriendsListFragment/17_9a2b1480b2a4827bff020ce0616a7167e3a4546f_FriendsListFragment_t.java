 package com.ese2013.mub;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.ese2013.mub.social.FriendRequest;
 import com.ese2013.mub.social.LoginService;
 import com.ese2013.mub.social.SocialManager;
 import com.ese2013.mub.social.User;
 import com.ese2013.mub.util.Observer;
 
 public class FriendsListFragment extends Fragment {
 
 	private ListView friends;
 	private FriendsListAdapter adapter;
 	private LayoutInflater inflater;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		this.inflater = inflater;
 		View view = inflater.inflate(R.layout.fragment_friends, container, false);
 		friends = (ListView) view.findViewById(R.id.friends_list);
 		adapter = new FriendsListAdapter();
 		friends.setAdapter(adapter);
 
 		TextView showMessage = (TextView) view.findViewById(R.id.no_friends_text_view);
 
 		if (LoginService.isLoggedIn())
 			showMessage.setText(R.string.no_friends);
 		else
 			showMessage.setText(R.string.not_loged_in);
 
 		friends.setEmptyView(showMessage);
 
 		setHasOptionsMenu(true);
 		return view;
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		if (LoginService.isLoggedIn())
 			inflater.inflate(R.menu.friend_list_menu, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.add_friend_button:
 
 			// TODO
 			View dialogView = inflater.inflate(R.layout.add_friends_dialog, null);
 			EditText edit = (EditText) dialogView.findViewById(R.id.enter_name);
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			builder.setView(dialogView);
 			builder.setTitle(R.string.add_friend);
 			builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			});
 			builder.setPositiveButton("OK", new PositiveButtonListener(edit, getActivity()));
 			builder.create().show();
 
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onPause() {
 		onDestroyOptionsMenu();
 		super.onPause();
 	}
 
 	@Override
 	public void onDestroy() {
 		onDestroyOptionsMenu();
 		super.onDestroy();
 	}
 
 	private class FriendsListAdapter extends BaseAdapter implements Observer {
 		private List<User> friends = new ArrayList<User>();
 		private List<FriendRequest> requests = new ArrayList<FriendRequest>();
 		private LayoutInflater inflater;
 
 		public FriendsListAdapter() {
 			super();
 			SocialManager.getInstance().addObserver(this);
 			SocialManager.getInstance().loadFriends();
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = convertView;
 			if (view == null)
 				inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 			if (position < requests.size()) {
 				view = inflater.inflate(R.layout.friend_request_layout, null);
 				FriendRequest friendRequest = requests.get(position);
 				TextView requestName = (TextView) view.findViewById(R.id.friend_name_request);
 				requestName.setText(friendRequest.getFrom().getNick());
 				ImageButton cancelRequestButton = (ImageButton) view.findViewById(R.id.cancel_request);
 				cancelRequestButton.setOnClickListener(new AnswerFriendRequestListener(friendRequest, false));
 				ImageButton acceptRequestButton = (ImageButton) view.findViewById(R.id.accept_request);
 				acceptRequestButton.setOnClickListener(new AnswerFriendRequestListener(friendRequest, true));
 			} else {
 				view = inflater.inflate(R.layout.friend_entry_layout, null);
 				User friend = friends.get(position - requests.size());
 				TextView friendName = (TextView) view.findViewById(R.id.friend_name);
 				friendName.setText(friend.getNick());
 				ImageButton deleteFriend = (ImageButton) view.findViewById(R.id.delete_friend);
 				deleteFriend.setOnClickListener(new DeleteFriendListener(friend));
 			}
 
 			return view;
 		}
 		
 		@Override
 		public int getCount() {
 			return friends.size() + requests.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			if (position <= requests.size())
 				return requests.get(position);
 			else
 				return friends.get(position - requests.size());
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public void onNotifyChanges() {
 			friends = LoginService.getLoggedInUser().getFriends();
 			requests = LoginService.getLoggedInUser().getFriendRequests();
 			notifyDataSetChanged();
 		}
 	}
 }
