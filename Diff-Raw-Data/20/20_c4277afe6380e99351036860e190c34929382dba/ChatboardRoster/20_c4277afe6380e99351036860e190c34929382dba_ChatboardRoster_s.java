 package core.im;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.RosterListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
 
 public class ChatboardRoster implements RosterListener{
 	XMPPConnection conn;
 	Vector<Buddy>online;
 	Vector<Buddy>offline;
 	Roster roster;
 	
 	public ChatboardRoster()
 	{
 		conn = null;
 		roster = null;
 		online = new Vector<Buddy>();
 		offline = new Vector<Buddy>();
 	}
 	
 	public ChatboardRoster(XMPPConnection conn)
 	{
 		super();
 		this.conn = conn;
 	}
 	
 	
 	public void pullRoster()
 	{
 		roster = conn.getRoster();
 		updateOnline();
 		roster.addRosterListener(this);	
 	}
 	
 	public boolean updateRoster(String username, String alias, String group)
 	{
 		//check to see if the roster already contains username
 		if(roster.contains(username))
 			return false;
 		//if not, then add the group if it doesn't exist
 		//then add the entry
 		else
 		{
 			if(roster.getGroup(group) == null)
 				roster.createGroup(group);
 			String[] groups = {group};
 			try
 			{
 				roster.createEntry(username, alias, groups);
 			}
 			catch(XMPPException e)
 			{
 				System.out.println("Error adding new entry: " + e);
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean createGroup(String name)
 	{
 		if(roster.getGroup(name) != null)
 		{
 			roster.createGroup(name);
 			return true;
 		}
 		else
 			System.out.println("Group already exists");
 		return false;
 	}
 	
 	public void updateOnline()
 	{
 		Collection<RosterEntry> rosterList = roster.getEntries();
 		Iterator<RosterEntry> iter = rosterList.iterator();
 		try { Thread.sleep(1000); } catch (InterruptedException e) { }
 		while(iter.hasNext())
 		{
 			RosterEntry entry = iter.next();
 			System.out.println(entry.getUser() + ": " + roster.getPresence(entry.getUser()).getType());
 			
 			Buddy b = new Buddy();
 			b.alias = entry.getName();
 			b.groupName = entry.getGroups().iterator().next().getName(); //get first one
 			b.userID = entry.getUser();
 			b.setPresence(roster.getPresence(entry.getUser()));
 			
 			if(!b.getOffline())
 			{
 				b.setStatusMessage(roster.getPresence(entry.getUser()).getStatus());
 				online.add(b);
 			}
 			else
 				offline.add(b);
 		}
 	}
 
 	@Override
 	public void entriesAdded(Collection<String> arg0) {
 		// TODO Auto-generated method stub
 		pullRoster();
 		
 	}
 
 	@Override
 	public void entriesDeleted(Collection<String> arg0) {
 		pullRoster();
 		
 	}
 
 	@Override
 	public void entriesUpdated(Collection<String> arg0) {
 		pullRoster();
 		
 	}
 
 	@Override
 	public void presenceChanged(Presence arg0) {
 		//First get the user
 		//then get the entry
 		//Finally remove the entry from one list, and add to the other
 		//create a new bean to handle it
 		
 		Buddy b = new Buddy();
 		//Get the participant from the presence
 		//Need to parse out some stuff
 		String participant = arg0.getFrom();
 		participant = participant.substring(0, participant.indexOf("/"));
 		b.userID = participant;
 		
 		//Set alias if we can find one
 		b.alias = roster.getEntry(participant).getName();
 		b.setPresence(arg0);
 		b.setStatusMessage(arg0.getStatus());
 		
 		if(!b.getOffline())
 		{
 			for(int i = 0; i< offline.size(); i++)
 			{
 				if(offline.get(i).userID.equals(b.userID))
 				{
 					offline.remove(i);
 					break;
 				}
 			}
 			online.add(b);
 		}
 		else
 		{
 			for(int i = 0; i < online.size(); i++)
 			{
 				if(online.get(i).userID.equals(b.userID))
 				{
 					online.remove(i);
 					break;
 				}
 			}
 			offline.add(b);
 		}
 		
 	}
 
 	public Vector<Buddy> getOnline() {
 		return online;
 	}
 
 	public void setOnline(Vector<Buddy> online) {
 		this.online = online;
 	}
 
 	public Vector<Buddy> getOffline() {
 		return offline;
 	}
 
 	public void setOffline(Vector<Buddy> offline) {
 		this.offline = offline;
 	}
 }
