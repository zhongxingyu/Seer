 package com.allplayers.android;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import java.util.List;
 
 public class MessageAdapter extends BaseAdapter
 {
 	private Context context;
 
 	private List<MessageData> messageDataList;
 
 	public MessageAdapter(Context context, List<MessageData> listMessageData) 
 	{
 		this.context = context;
 		messageDataList = listMessageData;
 	}
 
 	public View getView(int position, View convertView, ViewGroup viewGroup) 
 	{
 		MessageData entry = messageDataList.get(position);
 		if (convertView == null) 
 		{
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 					convertView = inflater.inflate(R.layout.inboxrow, null);
 		}
 		TextView senderName = (TextView) convertView.findViewById(R.id.senderName);
 		senderName.setText(entry.getLastSender());
 
 		TextView date = (TextView) convertView.findViewById(R.id.date);
 		date.setText(entry.getDateString());
 
 		TextView subject = (TextView) convertView.findViewById(R.id.subject);
 		subject.setText(entry.getSubject());
 
 		TextView body = (TextView) convertView.findViewById(R.id.body);
 		body.setText(entry.getMessageBody());
 		
 		ImageView unreadIcon = (ImageView) convertView.findViewById(R.id.unreadIcon);
		if(Integer.parseInt(entry.getNew()) == 0)
 		{
			unreadIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.placeholder));
 		}
		else
 		{
			unreadIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.unread_message_icon));
 		}
 
 		return convertView;
 	}
 
 	public int getCount() 
 	{
 		return messageDataList.size();
 	}
 
 	public Object getItem(int position) 
 	{
 		return messageDataList.get(position);
 	}
 
 	public long getItemId(int position) 
 	{
 		return position;
 	}
 }
