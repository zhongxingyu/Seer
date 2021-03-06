 package org.bombusim.lime.data;
 
 import java.util.ArrayList;
 
 import org.bombusim.lime.Lime;
 
 import android.database.Cursor;
 
 public class Chat {
 	public static final String UPDATE_CHAT = "org.bombusim.lime.data.UPDATE_CHAT";
 
 	private static final int HISTORY_DEFAULT_CONTEXT_SIZE = 10;
 	
 	Contact visavis;
 	
 	private ArrayList<Message> messages;
 	
 	public Chat(Contact contact) {
 		visavis = contact;
 		messages = new ArrayList<Message>();
 
 		populateFromHistory();
 	}
 	
 	private void populateFromHistory() {
 		ChatHistoryDbAdapter db = new ChatHistoryDbAdapter(Lime.getInstance().getApplicationContext(), visavis.getRosterJid());
 		db.open();
 		
 		Cursor hist = db.getMessageCursor( visavis.getJid(), HISTORY_DEFAULT_CONTEXT_SIZE );
 		
		if (hist.moveToFirst()) do {
 			messages.add(db.getMessageFromCursor(hist));
		} while (hist.moveToNext());
 		
 		hist.close();
 		db.close();
 	}
 
 	public Contact getVisavis() { return visavis; }
 	
 	public int getChatSize() { return messages.size(); }
 	public Message getMessage(int index) { return messages.get(index); }
 	
 	public void addMessage(Message msg) {
 		messages.add(msg);
 		//TODO: unread
 		ChatHistoryDbAdapter db = new ChatHistoryDbAdapter(Lime.getInstance().getApplicationContext(), visavis.getRosterJid());
 		db.open();
 		db.putMessage(msg, -1);
 		db.close();
 	}
 	
 	public int getUnreadCount() {
 		//TODO: return unread messages count
 		return 0;
 	}
 	public void markRead(int index){
 		//TODO: mark unread
 	}
 }
