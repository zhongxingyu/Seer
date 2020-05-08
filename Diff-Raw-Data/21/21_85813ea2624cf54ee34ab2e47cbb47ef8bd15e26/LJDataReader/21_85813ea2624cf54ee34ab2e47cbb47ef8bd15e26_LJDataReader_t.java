 /**
  * 
  */
 package net.mysocio.connection.readers.lj;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.mysocio.connection.readers.ISourcesGroup;
 import net.mysocio.data.IContact;
 import net.mysocio.data.SocioContact;
 import net.mysocio.data.management.DataManagerFactory;
 
 import org.katkov.lj.ClientsFactory;
 import org.katkov.lj.ConvenientClient;
 import org.katkov.lj.xmlrpc.results.Friend;
 import org.katkov.lj.xmlrpc.results.FriendGroup;
 import org.katkov.lj.xmlrpc.results.UserData;
 
 /**
  * @author Aladdin
  *
  */
 public class LJDataReader {
 	private static final int TIMEOUT = 50000;
 	private UserData login;
 	private List<LjSource> friends = new ArrayList<LjSource>();
 	private FriendGroup[] friendgroups;
 	
 	public void init(LjUser user){
 		ConvenientClient ljConvenientClient = ClientsFactory.getLJConvenientClient();
 		this.login = ljConvenientClient.login(user.getUserName(), user.getPassword(), TIMEOUT);
 		Friend[] friends = ljConvenientClient.getFriends(TIMEOUT);
 		for (Friend friend : friends) {
 			int groupmask = friend.getGroupmask();
 			LjSource source = new LjSource(friend.getUsername(), friend.getFullname(), groupmask);
 			this.friends.add(source);
 			DataManagerFactory.getDataManager().saveSource(source);
 		}
 		this.friendgroups = login.getFriendgroups();
 	}
 	
 	public Set<IContact> getLJFriends(){
 		Set<IContact> contacts = new HashSet<IContact>();
 		//Put friends in groups
 		for (LjSource source : this.friends) {
 			SocioContact friendContact = new SocioContact();
 			friendContact.setName(source.getName());
 			friendContact.addSource(source);
 			DataManagerFactory.getDataManager().saveContact(friendContact);
 			contacts.add(friendContact);
 		}
 		return contacts;
 	}
 	public Set<ISourcesGroup> getSources(){
 		Set<ISourcesGroup> groups = new HashSet<ISourcesGroup>();
 		//Get user friends groups
 		for (FriendGroup group : this.friendgroups){
 			int lgGroupMask = 1 << group.getId();
 			LjFriendsGroup ljGroup = new LjFriendsGroup(group.getName(), lgGroupMask);
 			DataManagerFactory.getDataManager().saveSourcesGroup(ljGroup);
 			groups.add(ljGroup);
 		}
 		LjFriendsGroup noneGroup = new LjFriendsGroup("None", 0);
 		DataManagerFactory.getDataManager().saveSourcesGroup(noneGroup);
 		groups.add(noneGroup);
 		for (LjSource source : this.friends) {
 			int groupmask = source.getGroupMask();
 			// Maximum size of groups is 32, so it's bearable run.
 			for (ISourcesGroup group : groups) {
 				LjFriendsGroup ljGroup = (LjFriendsGroup)group;
 				if ((groupmask&ljGroup.getLgGroupMask()) != 0){
 					ljGroup.addFriend(source);
 				}
 			}
 		}
 		return groups;
 	}
 }
