 /* This list represents the groups on the server */
 import java.util.*;
 
 
 
 public class GroupList implements java.io.Serializable 
 {
 
 	private Hashtable<String, Group> list = new Hashtable<String, Group>();
 	private static final long serialVersionUID = 7600343803563417992L;
 		
 	public synchronized void addGroup(String groupName)
 	{
 		Group newGroup = new Group();
 		list.put(groupName, newGroup);
 	}
 		
 	public synchronized void deleteGroup(String groupName)
 	{
 		list.remove(groupName);
 	}
 	
 	//This checks if group: groupName exists
 	public synchronized boolean checkGroup(String groupName)
 	{
 		if(list.containsKey(groupName))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 		
 	//These are all calls to the Group subclass
 	public synchronized ArrayList<String> getGroupMembers(String groupName)
 	{
 		return list.get(groupName).getMembers();
 	}
 		
 	public synchronized ArrayList<String> getGroupOwners(String groupName)
 	{
 		return list.get(groupName).getOwners();
 	}
 		
 	public synchronized void addMember(String groupName, String member)
 	{
 		list.get(groupName).addMember(member);
 	}
 		
 	public synchronized void removeMember(String groupName, String member)
 	{
 		list.get(groupName).removeMember(member);
 	}
 		
 	public synchronized void addOwner(String groupName, String owner)
 	{
 		list.get(groupName).addOwner(owner);
 	}
 		
 	public synchronized void removeOwner(String groupName, String owner)
 	{
 		list.get(groupName).removeOwner(owner);
 	}
 
 
 	class Group implements java.io.Serializable 
 	{
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -6699986336399821598L;
 		private ArrayList<String> members;
 		private ArrayList<String> owners;
 
 
 		public Group()
 		{
 			members = new ArrayList<String>();
 			owners = new ArrayList<String>();
 		}
 		
 		public ArrayList<String> getMembers()
 		{
 			return members;
 		}
 		
 		public ArrayList<String> getOwners()
 		{
 			return owners;
 		}
 		
 		public void addMember(String member)
 		{
 			members.add(member);
 		}
 		
 		public void removeMember(String member)
 		{
 			if(!members.isEmpty())
 			{
 				if(members.contains(member))
 				{
 					members.remove(members.indexOf(member));
 				}
 			}
 		}
 		
 		public void addOwner(String owner)
 		{
 			owners.add(owner);
 		}
 		
 		public void removeOwner(String owner)
 		{
 			if(!owners.isEmpty())
 			{
 				if(owners.contains(owner))
 				{
 					owners.remove(owners.indexOf(owner));
 				}
 			}
 		}
 	}
 }
