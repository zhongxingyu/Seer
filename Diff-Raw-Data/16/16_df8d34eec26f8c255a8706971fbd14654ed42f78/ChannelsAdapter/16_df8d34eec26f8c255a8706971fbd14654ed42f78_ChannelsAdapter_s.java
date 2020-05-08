 package com.ipbox.fragments;
 
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.ipbox.R;
 import com.ipbox.activites.BaseActivity;
 import com.ipbox.playlist.Channel;
 import com.ipbox.playlist.Playlist;
 
 /**
  * User: gsd
  * Date: 5/29/12
  * Time: 12:51 PM
  */
 public class ChannelsAdapter extends ArrayAdapter<Channel> {
 	protected final Playlist _list;
 	protected final BaseActivity _activity;
 
 	public ChannelsAdapter(FragmentActivity activity, Playlist list) {
 		super(activity, R.layout.channel_list_item, R.id.channel_title, list.getChannels());
 		_list = list;
 		_activity = (BaseActivity) activity;
 	}
 
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent) {
 		View view = super.getView(position, convertView, parent);
 
		final Channel channel = _list.getChannel(position);
 
 		ImageView channelIcon = (ImageView) view.findViewById(R.id.channel_icon);
 		channelIcon.setImageDrawable(_activity.getResources().getDrawable(channel.getIconId()));
		/*channelIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				_activity.loadChannel(channel);
			}
		});  */
 		TextView subText = (TextView) view.findViewById(R.id.channel_sub_text);
 		subText.setText(channel.getUrl());
 
 		return view;
 	}
 }
