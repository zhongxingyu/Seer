 package org.touchirc.adapter;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 import org.touchirc.R;
 import org.touchirc.irc.IrcService;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class UsersListAdapter extends BaseAdapter {
 	
 	private IrcService ircService;
 	private Context c;
 
 	public UsersListAdapter (IrcService ircService, Context c){
 		this.ircService = ircService;
 		this.c = c;
 	}
 
 	@Override
 	public int getCount() {
 		return ircService.getCurrentChannel().getUsers().size();
 	}
 
 	@Override
 	public User getItem(int i) {
 		ArrayList<User> users = new ArrayList<User>(ircService.getCurrentChannel().getUsers());
 		Collections.sort(users, new Comparator<User>() {
 			@Override
 			public int compare(User u1, User u2) {
 				Channel chan = ircService.getCurrentChannel();
 				
 				if(chan.getOps().contains(u1) && !chan.getOps().contains(u2))
 					return -1;
 				if(!chan.getOps().contains(u1) && chan.getOps().contains(u2))
 					return 1;
 				if(chan.getHalfOps().contains(u1) && !chan.getHalfOps().contains(u2))
 					return -1;
 				if(!chan.getHalfOps().contains(u1) && chan.getHalfOps().contains(u2))
 					return 1;
 				if(chan.getVoices().contains(u1) && !chan.getVoices().contains(u2))
 					return -1;
 				if(!chan.getVoices().contains(u1) && chan.getVoices().contains(u2))
 					return 1;
 					
 				return u1.getNick().compareTo(u2.getNick());
 			}
 		});
 		return users.get(i);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		if (convertView == null)
 			convertView = LayoutInflater.from(c).inflate(R.layout.connected_user_item, null);
 		
 		User user = getItem(position);
 		
 		TextView userTV = (TextView) convertView.findViewById(R.id.userItemTextView);
 		
 		userTV.setText(user.getNick().toString());
 		
 		// set blank status and override it if the user is more than a normal user
 		userTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_blank,0,0,0);
 		
 		Channel channel = ircService.getCurrentChannel();
 		if(user.getChannelsOpIn().contains(channel)){
 			userTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_op,0,0,0);
 			return convertView;
 		}
 		if(user.getChannelsHalfOpIn().contains(channel)){
 			userTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_hop,0,0,0);
 			return convertView;
 		}
 		if(user.getChannelsVoiceIn().contains(channel)){
 			userTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_voice,0,0,0);
 			return convertView;
 		}
 		
 		return convertView;		
 	}
 
 }
