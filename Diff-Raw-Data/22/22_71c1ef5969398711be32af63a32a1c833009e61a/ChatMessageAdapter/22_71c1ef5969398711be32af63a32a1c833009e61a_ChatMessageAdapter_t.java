 package com.quanleimu.adapter;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.ChatMessage;
 import com.quanleimu.entity.compare.MsgTimeComparator;
 import com.quanleimu.imageCache.SimpleImageLoader;
 
 public class ChatMessageAdapter extends BaseAdapter {
 
 	private List<ChatMessage> msgList = new ArrayList<ChatMessage>();
 	private String myId;
 	private LayoutInflater inflater;
 	
 	
 	private String myIcon;
 	private boolean iamBoy = true;
 	
 	private String targetIcon;
 	private boolean targetIsBoy = true;
 	
 	public ChatMessageAdapter(String myId)
 	{
 //		this.msgList = itemList;
 		this.myId = myId;
 	}
 	
 	public void setMyProfile(String myProfileImg, boolean isBoy)
 	{
 		this.myIcon = myProfileImg;
 		this.iamBoy = isBoy;
 		this.notifyDataSetChanged();
 	}
 	
 	public void setTargetProfile(String targetUserImg, boolean isBoy)
 	{
 		this.targetIcon = targetUserImg;
 		this.targetIsBoy = isBoy;
 		this.notifyDataSetChanged();
 	}
 	
 	public void appendData(ChatMessage msg) //FIXME: shall we do merge???
 	{
 		List<ChatMessage> newList = new ArrayList<ChatMessage>();
 		newList.addAll(msgList);
		
		if (msgList.size() > 0)
		{
			ChatMessage lastMsg = msgList.get(msgList.size()-1);
			long lastTime = lastMsg.getTimestamp();
			if (lastTime > msg.getTimestamp())
			{
				msg.setTimestamp(lastTime + 1); //
			}
		}
 		newList.add(msg);
 		
 		refreshData(newList);
 	}
 	
	public void updateMessageTimestamp(long hashCode, long time)
	{
		for (ChatMessage msg : msgList)
		{
			if (msg.hashCode() == hashCode)
			{
				msg.setTimestamp(time);
			}
		}
	}
	
 	
 	public void refreshData(List<ChatMessage> newList)
 	{
 		Collections.sort(newList, new MsgTimeComparator());
 		msgList = newList;
 		this.notifyDataSetChanged();
 	}
 
 	
 	
 	@Override
 	public int getCount() {
 		return msgList.size();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		return msgList.get(arg0);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		
 		if (inflater == null)
 		{
 			inflater = LayoutInflater.from(parent.getContext());
 		}
 		
 		View item = convertView;
 		if (item == null)
 		{
 			item = inflater.inflate(R.layout.item_im_message, null);
 		}
 		
 		ChatMessage msg = msgList.get(position);
 		final boolean isMine = myId.equalsIgnoreCase(msg.getFrom());
 		
 		loadMessageItem(isMine, item, msg);
 		
 		
 		
 		
 		return item;
 	}
 	
 	
 	private void loadMessageItem(boolean isMine, View parent, ChatMessage msg)
 	{
 		View send = parent.findViewById(R.id.my_item);
 		View receive = parent.findViewById(R.id.received_item);
 		
 		send.setVisibility(isMine ? View.VISIBLE : View.GONE);
 		receive.setVisibility(isMine ? View.GONE : View.VISIBLE);
 		
 		ImageView iv = isMine ? (ImageView)send.findViewById(R.id.myIcon) : (ImageView) receive.findViewById(R.id.targetIcon);
 		if(iv != null){
 			if(isMine){
 				if(this.myIcon != null && !this.myIcon.equals("") && !this.myIcon.equals("null")){
 					iv.setTag(myIcon);
 					SimpleImageLoader.showImg(iv, myIcon, null, parent.getContext());
 				}else if(!iamBoy){
 					iv.setImageResource(R.drawable.pic_my_avator_girl);
 				}
 			}else {
 				if(this.targetIcon != null && !targetIcon.equals("") && !targetIcon.equals("null")){
 					iv.setTag(targetIcon);
 					SimpleImageLoader.showImg(iv, targetIcon, null, parent.getContext());
 				}else if(!targetIsBoy){
 					iv.setImageResource(R.drawable.pic_my_avator_girl);
 				}
 			}
 		}
 		
 		View msgItem = isMine ? send : receive;
 		View msgParent = msgItem.findViewById(R.id.im_message_content_parent);
 		TextView textView = (TextView) msgItem.findViewById(R.id.im_message_content);
 		textView.setText(msg.getMessage());
 		
 		msgParent.setPadding(msgParent.getPaddingLeft(), msgParent.getPaddingTop()/10, msgParent.getPaddingRight(), msgParent.getPaddingBottom()/10);
 	}
 
 }
