 package org.touchirc.adapter;
 
 /**
  * 
  * Here, a group is an User, the children are the possible options
  * on this user : OPs Commands/Whois/Send a MP. 
  * 
  */
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 import org.touchirc.R;
 import org.touchirc.irc.IrcService;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.TextView;
 
 public class ExpandableUserAdapter extends BaseExpandableListAdapter {
 
 	private IrcService ircService;
 	private Context c;
 	private String [] options = {"OPs Commands","Whois","Send a MP"};
 
 	private LayoutInflater inflater;
 
	public ExpandableUserAdapter (IrcService ircService, Context c){
 		this.ircService = ircService;
 		this.c = c;
 		inflater = LayoutInflater.from(c);
 	}
 
 	@Override
 	public String getChild(int groupPosition, int childPosition) {
 		return this.options[childPosition];
 	}
 
 	@Override
 	public long getChildId(int groupPosition, int childPosition) {
 		return childPosition;
 	}
 
 	@Override
 	public View getChildView(int groupPosition, int childPosition,
 			boolean isLastChild, View convertView, ViewGroup parent) {
 		TextView option;
 
 		if(convertView == null)
 			convertView = inflater.inflate(R.layout.option_item, null);
 		option = (TextView) convertView.findViewById(R.id.optionItemTextView);
 		option.setText(getChild(groupPosition, childPosition));
 
 		return convertView;
 	}
 
 	@Override
 	public int getChildrenCount(int groupPosition) {
 		return this.options.length;
 	}
 
 	@Override
 	public User getGroup(int groupPosition) {
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
 		return users.get(groupPosition);
 	}
 
 	@Override
 	public int getGroupCount() {
 		return ircService.getCurrentChannel().getUsers().size();
 	}
 
 	@Override
 	public long getGroupId(int groupPosition) {
 		return groupPosition;
 	}
 
 	@Override
 	public View getGroupView(int groupPosition, boolean isExpanded,
 			View convertView, ViewGroup parent) {
 		if (convertView == null)
 			convertView = LayoutInflater.from(c).inflate(R.layout.connected_user_item, null);

 		User user = getGroup(groupPosition);
 
 		TextView userTV = (TextView) convertView.findViewById(R.id.userItemTextView);
 
 		userTV.setText(user.getNick().toString());
 
 		// set blank status and override it if the user is more than a normal user
 		userTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_status_blank,0,0,0);
 
 		if(user.isAway()){
 			userTV.setTextColor(convertView.getResources().getColor(R.color.gray));
 			userTV.setTypeface(null,Typeface.ITALIC);
 		}else{
 			userTV.setTextColor(convertView.getResources().getColor(R.color.black));
 			userTV.setTypeface(Typeface.DEFAULT);
 		}
 
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
 
 	@Override
 	public boolean hasStableIds() {
 		return false;
 	}
 
 	@Override
 	public boolean isChildSelectable(int groupPosition, int childPosition) {
 		return true;
 	}
 }
