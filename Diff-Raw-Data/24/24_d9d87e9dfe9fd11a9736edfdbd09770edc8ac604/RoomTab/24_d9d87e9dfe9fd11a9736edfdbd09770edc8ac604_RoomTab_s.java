 package com.willluongo.asbestos.gui;
 
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 
 import com.madhackerdesigns.jinder.Room;
 import com.madhackerdesigns.jinder.models.Message;
 import com.madhackerdesigns.jinder.models.User;
 
 public class RoomTab {
 	private Room room = null;
 	private Message lastMessage = new Message();
	private SortedSet<User> users = null;
	private SortedSet<Long> userIds = new TreeSet<Long>();
 	private Hashtable<Long, User> userCache = new Hashtable<Long, User>();
 
 
 	// UI Elements
 	private TabItem tbtmRoom = null;
 	private TabFolder folder;
 	private Text text_messages;
 	private Text text;
 
 	public RoomTab(Room room, TabFolder folder) {
 		this.room = room;
 		this.folder = folder;
 		create();
 	}
 
 	private void create() {
 		tbtmRoom = new TabItem(folder, SWT.NONE);
 		tbtmRoom.setText(room.name);
 
 		Composite composite = new Composite(folder, SWT.NONE);
 		tbtmRoom.setControl(composite);
 
 		text_messages = new Text(composite, SWT.READ_ONLY | SWT.WRAP
 				| SWT.V_SCROLL | SWT.MULTI);
 		text_messages.setBounds(0, 0, 430, 217);
 
 		text = new Text(composite, SWT.BORDER);
 		text.addTraverseListener(new TraverseListener() {
 			public void keyTraversed(TraverseEvent e) {
 				if ((e.detail == SWT.TRAVERSE_RETURN)
 						&& (text.getText().length() > 0)) {
 					try {
 						room.speak(text.getText());
 					} catch (IOException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 					text.setText("");
 					update();
 				}
 
 			}
 		});
 		text.setBounds(0, 223, 430, 19);
 	}
 
 	public void update() {
 		List<Message> lastUpdate = null;
 		try {
 			if (lastMessage.id == null)
 				lastUpdate = room.recent();
 			else
 				lastUpdate = room.recent(lastMessage.id);
 			for (Message msg : lastUpdate) {
 //				log.debug(msg.created_at + " " + msg.type);
 				if (msg.type.equals("TextMessage")) {
 					if (!(msg.equals(lastMessage))) {
 						if (!(userCache.containsKey(msg.user_id))) {
 							userCache.put(msg.user_id, room.user(msg.user_id));
 						}
 						text_messages.append(userCache.get(msg.user_id).name
 								.concat(": ").concat(msg.body.concat("\n")));
 
 						lastMessage = msg;
 					}
 
 				}
 			}
 
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 	
 	public void join() throws IOException
 	{
 		room.join();
 	}
 
 
 }
