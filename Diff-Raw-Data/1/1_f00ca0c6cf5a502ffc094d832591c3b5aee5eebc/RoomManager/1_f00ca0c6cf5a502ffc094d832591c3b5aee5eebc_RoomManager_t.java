 package ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.TreeItem;
 import org.jibble.pircbot.IrcUser;
 
 import connection.Connection;
 
 import shared.Customs;
 import shared.Message;
 
 public class RoomManager {
 
 	private static MainWindow m;
 	
 	public static Customs colorset;
 	
 	public static CopyOnWriteArrayList<Message> pmQueue = new CopyOnWriteArrayList<Message>();
 	public static CopyOnWriteArrayList<Message> queue = new CopyOnWriteArrayList<Message>();
 	
 	public static CopyOnWriteArrayList<Room> rooms = new CopyOnWriteArrayList<Room>();
 
 	public static void createRoom(final Composite c, final int style, final String channel, final Connection connection, final int layout) {
         if(!m.getDisplay().isDisposed()){
         	m.getDisplay().asyncExec (new Runnable () {
             	public void run () {
 					if(canAddRoom(connection, channel)){
 						Room r = new Room(c, style, layout);
 						r.setChannel(new Channel((CTabFolder)c, channel, connection));
 						r.setServerConnection(connection);
 						r.instantiate();
 						rooms.add(r);
 						updateWho(r);
 						for(String s : connection.getTopics().keySet()){
 							if(s.equals(channel)){
 								changeTopic(r, connection.getTopics().get(channel));
 							}
 						}
 					}
             	}
             }
 		);}
 	}
 	
 	public static void manageQueue() {
         if(!m.getDisplay().isDisposed()){
             m.getDisplay().asyncExec (new Runnable () {
                public void run () {
             	   for(Message mes : queue) {
             		   filterMessage(mes);
             		   queue.remove(mes);
             	   }
                }
             });
         }
 	}
 	
 	public static void changeTopic(final Room c, final String t) {
 		if(c == null){return;}
         if(!m.getDisplay().isDisposed()){
             m.getDisplay().asyncExec (new Runnable () {
                public void run () {
             	   c.setTopic(t);
                }
             });
         }
 	}
 	
 	public static void updateWho(final Room c) {
 		if(c == null){return;}
         if(!m.getDisplay().isDisposed()){
             m.getDisplay().asyncExec (new Runnable () {
                public void run () {
             	   if(c.getWho()==null || 
             			   c.getServerConnection()==null || 
             			   c.getServerConnection().getUsers() == null ||
             			   c.getChannel()==null || 
             			   c.getChannel().getChannelName()==null)return;
 
             	   c.getWho().removeAll();
             	   
             	   //					 none , voice, ops  , hops , owner, admin
             	   boolean[] contains = {false, false, false, false, false, false};
             	   ArrayList<IrcUser> users = new ArrayList<IrcUser>();
             	   try {
             	   for(IrcUser u : c.getServerConnection().getUsers().get(c.getChannel().getChannelName())){
             		   if(u.isAdmin())			{ contains[5] = true;}
             		   else if(u.isFounder())	{ contains[4] = true;}
             		   else if(u.isHalfop())	{ contains[3] = true;}
             		   else if(u.isOp())		{ contains[2] = true;}
             		   else if(u.hasVoice()) 	{ contains[1] = true;}
             		   else 					{ contains[0] = true;}
             		   users.add(u);
             	   }
             	   } catch(Exception e){
             		   
             	   }
             	   Collections.sort(users, new Comparator<IrcUser>() {
             		   public int compare(IrcUser arg0, IrcUser arg1) {
             			   return arg0.toString().compareTo(arg1.toString());
             		   }
 					});
             	   
             	   CopyOnWriteArrayList<IrcUser> moarUsers = new CopyOnWriteArrayList<IrcUser>();
             	   moarUsers.addAll(users);
 
             	   if(contains[5]){
                 	   TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
                 	   t.setText("Admins");
                 	   for(IrcUser s : moarUsers){
                 		   if(s.isAdmin()){
                 			   UserTreeItem i = new UserTreeItem(t, SWT.NONE, s, c.getServerConnection(), c.getChannel());
                 			   i.getTree().setText(s.toString());
                 			   moarUsers.remove(s);
                 		   }
                 	   }
                 	   t.setExpanded(true);
             	   }
             	   if(contains[4]){
                 	   TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
                 	   t.setText("Founder(s)");
                 	   for(IrcUser s : moarUsers){
                 		   if(s.isFounder()){
                 			   UserTreeItem i = new UserTreeItem(t, SWT.NONE, s, c.getServerConnection(), c.getChannel());
                 			   i.getTree().setText(s.getNick());
                 			   moarUsers.remove(s);
                 		   }
                 	   }
                 	   t.setExpanded(true);
             	   }
             	   if(contains[3]){
                 	   TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
                 	   t.setText("Half-Ops");
                 	   for(IrcUser s : moarUsers){
                 		   if(s.isHalfop()){
                 			   UserTreeItem i = new UserTreeItem(t, SWT.NONE, s, c.getServerConnection(), c.getChannel());
                 			   i.getTree().setText(s.getNick());
                 			   moarUsers.remove(s);
                 		   }
                 	   }
                 	   t.setExpanded(true);
             	   }
             	   if(contains[2]){
                 	   TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
                 	   t.setText("Ops");
                 	   for(IrcUser s : moarUsers){
                 		   if(s.isOp()){
                 			   UserTreeItem i = new UserTreeItem(t, SWT.NONE, s, c.getServerConnection(), c.getChannel());
                 			   i.getTree().setText(s.getNick());
                 			   moarUsers.remove(s);
                 		   }
                 	   }
                 	   t.setExpanded(true);
             	   }
             	   if(contains[1]){
                 	   TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
                 	   t.setText("Voice");
                 	   for(IrcUser s : moarUsers){
                 		   if(s.hasVoice()){
                 			   UserTreeItem i = new UserTreeItem(t, SWT.NONE, s, c.getServerConnection(), c.getChannel());
                 			   i.getTree().setText(s.getNick());
                 			   moarUsers.remove(s);
                 		   }
                 	   }
                 	   t.setExpanded(true);
             	   }
             	   if(contains[0]){
                 	   TreeItem t = new TreeItem(c.getWho(), SWT.NONE);
                 	   t.setText("Normal");
                 	   for(IrcUser s : moarUsers){
             			   UserTreeItem i = new UserTreeItem(t, SWT.NONE, s, c.getServerConnection(), c.getChannel());
             			   i.getTree().setText(s.getNick());
                 	   }
                 	   t.setExpanded(true);
             	   }
                }
             });
         }
 	}
 	
 	public static void setMain(MainWindow w){
 		m = w;
 	}
 	
 	public static MainWindow getMain(){
 		return m;
 	}
 	
 	public static boolean canAddRoom(Connection c, String s){
 		for(Room n : getRooms()){
 			if(n.getServerConnection().equals(c) && n.getChannel().getChannelName().equals(s)){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public static void filterMessage(Message m){
 		for(Room r : rooms){
 			if(m.getConnection().equals(r.getServerConnection()) && r.getChannel().getChannelName().equals(m.getChannel())){
 
 				r.getOutput().append("<"+m.getSender() + "> "+m.getContent());
				r.getOutput().setSelection(r.getOutput().getText().length()); //scroll the ouput control down
 				
 				for(String s : m.getContent().split(" ")){
 					if(s.contains("://")){
 						//TODO: make this conform to the global list, and make the global list work
 						Color blue = new Color(r.getOutput().getDisplay(),0,0,255);
 						StyleRange styleRange = new StyleRange();
 						styleRange.start = r.getOutput().getCharCount() - m.getContent().length() + m.getContent().indexOf(s)-1;
 						styleRange.length = s.length();
 						styleRange.foreground = blue;
 						styleRange.data = s;
 						styleRange.underline=true;
 						styleRange.underlineStyle = SWT.UNDERLINE_LINK;
 						r.getOutput().setStyleRange(styleRange);
 					}
 				}			
 			}
 		}
 	}
 
 	public static CopyOnWriteArrayList<Room> getRooms() {
 		return rooms;
 	}
 	
 }
