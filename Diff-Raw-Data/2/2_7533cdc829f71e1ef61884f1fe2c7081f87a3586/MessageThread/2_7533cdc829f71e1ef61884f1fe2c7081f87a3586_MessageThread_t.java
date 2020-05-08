 package com.allplayers.android;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 
 public class MessageThread extends ListActivity
 {
 	private ArrayList<MessageThreadData> messageThreadList;
 	private boolean hasMessages = false;
 	private String jsonResult = "";
 	private int threadIDInt;
 	
 	ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(2);
 	
 	/** Called when the activity is first created. */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		MessageData message = Globals.currentMessage;
 		String threadID = message.getThreadID();
 		threadIDInt = Integer.parseInt(threadID);
 		
		APCI_RestServices.putMessage(threadIDInt, 0, "");
		
 		jsonResult = APCI_RestServices.getUserMessagesByThreadId(threadID);
 
 		HashMap<String, String> map;
 		
 		MessageThreadMap messages = new MessageThreadMap(jsonResult);
 		messageThreadList = messages.getMessageThreadData();
 
 		Collections.sort(messageThreadList, new Comparator()
 		{
 			public int compare(Object o1, Object o2)
 			{
 				MessageThreadData m1 = (MessageThreadData) o1;
 				MessageThreadData m2 = (MessageThreadData) o2;
 				return m2.getTimestampString().compareToIgnoreCase(m1.getTimestampString());
 			}
 		});
 		
 		if(!messageThreadList.isEmpty())
 		{
 			hasMessages = true;
 			
 			for(int i = 0; i < messageThreadList.size(); i++)
 			{
 				map = new HashMap<String, String>();
 				map.put("line1", messageThreadList.get(i).getMessageBody());
 				map.put("line2", "From: " + messageThreadList.get(i).getSenderName() + " - " + messageThreadList.get(i).getDateString());
 				list.add(map);
 			}
 		}
 		else
 		{
 			hasMessages = false;
 			
 			map = new HashMap<String, String>();
 			map.put("line1", "You have no new messages.");
 			map.put("line2", "");
 			list.add(map);
 		}
 		
 		String[] from = { "line1", "line2" };
 
 		int[] to = { android.R.id.text1, android.R.id.text2 };
 
 		SimpleAdapter adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2, from, to);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id)
 	{
 		super.onListItemClick(l, v, position, id);
 		
 		if(hasMessages)
 		{
 			Globals.currentMessageThread = messageThreadList.get(position);
 			
 			Intent intent = new Intent(MessageThread.this, MessageViewSingle.class);
 			startActivity(intent);
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.markreadmenu, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 	    switch (item.getItemId())
 	    {
 	    	case R.id.reply:
 		{
 	    		startActivity(new Intent(MessageThread.this, MessageReply.class));
 	    		return true;
 		}
 	    	case R.id.markRead:
 		{
 				APCI_RestServices.putMessage(threadIDInt, 1, "");
 				startActivity(new Intent(MessageThread.this, MessageInbox.class));
 		    	finish();
 		    	return true;
 		}
 	    	default:	return super.onOptionsItemSelected(item);
 	    }
 	}
 }
