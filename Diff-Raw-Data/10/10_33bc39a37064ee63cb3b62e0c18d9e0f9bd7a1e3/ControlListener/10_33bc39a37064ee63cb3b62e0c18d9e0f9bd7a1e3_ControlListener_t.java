 package core.im;
 
 import gui.MessageDialog;
 
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Queue;
 import java.util.Set;
 
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManager;
 import org.jivesoftware.smack.ChatManagerListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.packet.Message;
 
 import sun.rmi.server.WeakClassHashMap;
 
 import core.abstraction.Controller;
 import core.whiteboard.WhiteboardPanel;
 
 public class ControlListener implements ChatManagerListener, ListDataListener, Controller {
 
 	public XMPPConnection conn;
 	public HashMap<String, MessageDialog> messages;
 	public ChatboardMessage msg;
 	public String userID;
 	public HashMap<String, Chat> chats;
 	public HashMap<String, WhiteboardPanel> whiteboards;
 	
 	public ControlListener()
 	{
 		conn = null;
 		messages = new HashMap<String, MessageDialog>();
 		msg = new ChatboardMessage();
 		chats = new HashMap<String, Chat>();
 		userID = "";
 		whiteboards = new HashMap<String, WhiteboardPanel>();
 	}
 
 	public ControlListener(XMPPConnection c, String from)
 	{
 		conn = c;
 		messages = new HashMap<String, MessageDialog>();
 		msg = new ChatboardMessage(c, from);
 		userID = from;
 		chats = new HashMap<String, Chat>();
 		whiteboards = new HashMap<String, WhiteboardPanel>();
 	}
 	
 	public void createChatboardMessage(String from)
 	{
 		msg = new ChatboardMessage(conn, from);
 	}
 	
 	public void addDataListener()
 	{
 		msg.listeners.add(this);
 	}
 	
 	public void addWhiteboard(String from, WhiteboardPanel whiteboard)
 	{
 		whiteboards.put(from, whiteboard);
 	}
 	
 	public Chat createChat(String userID)
 	{
 		ChatManager manager = conn.getChatManager();
 		Chat chat = manager.createChat(userID, msg);
 		return chat;
 	}
 	
 	public boolean sendMessage(String from, String message)
 	{
 		try
 		{
 			Chat chat = chats.get(from);
 			if(chat == null)
 				chat = createChat(from);
 			msg.sendMessage(chat, message);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	@Override
 	public void chatCreated(Chat arg0, boolean arg1) {
 		arg0.addMessageListener(msg);
 		String from = arg0.getParticipant();
 		if(arg0.getParticipant().indexOf('/') != -1)
 			from = arg0.getParticipant().substring(0, arg0.getParticipant().indexOf('/'));
 		chats.put(from, arg0);
 		
 		if(messages.get(from) == null)
 		{
 			MessageDialog dialog = new MessageDialog(from);
			dialog.addController(this);
 			messages.put(from, dialog);
 		}
 	}
 	
 	public void addDialog(MessageDialog dialog, String name)
 	{
 		
 		messages.put(name,  dialog);
 	}
 	
 
 	@Override
 	public void contentsChanged(ListDataEvent e) {
 		Queue<IM> q = msg.queue;
 		while(!q.isEmpty())
 		{
 			IM im = q.remove();
 			MessageDialog d = messages.get(im.from);
 			if(d == null)
 			{
 				d = new MessageDialog(im.from);
				d.addController(this);
 				messages.put(im.from, d);
 			}
 			d.receiveMessage(im);
 		}
 		
 	}
 
 	@Override
 	public void intervalAdded(ListDataEvent e) {
 		// TODO Auto-generated method stub
 		System.out.println("Received message queue");
 		Queue <Message> q = msg.whiteBoardQueue;
 		while(!q.isEmpty())
 		{
 			Message message = q.remove();
 			String from = message.getFrom();
 			if(from.indexOf("/") != -1)
 			{
 				String client = from.substring(from.indexOf("/") + 1);
 				from = from.substring(0, from.indexOf("/"));
 				if(!client.equalsIgnoreCase("chatboard"))
 					continue;
 				//now get the q and check if there's anything to draw
 				try
 				{
 					Queue<String> theQ = (Queue<String>)message.getProperty("whiteboardqueue");
 					if(theQ == null)
 					{
 						System.out.println("queue is empty");
 						continue;
 					}
 					WhiteboardPanel p = whiteboards.get(from);
 					p.applyQueue(theQ);
 				}
 				catch(Exception e1)
 				{
 					e1.printStackTrace();
 					continue;
 				}
 
 			}
 			else
 				continue;
 		}
 		
 	}
 
 	@Override
 	public void intervalRemoved(ListDataEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void removeDialog(String key) {
 		// TODO Auto-generated method stub
 		messages.remove(key);
 	}
 
 }
