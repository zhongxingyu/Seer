 
 package cz.vutbr.fit.gja.gjaddr.persistancelayer;
 
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.util.ServicesEnum;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class for communication with database.
  *
  * @author Bc. Radek Gajdušek <xgajdu07@stud.fit.vutbr.cz>
  * @author Bc. Drahomira Herrmannova <xherrm01@stud.fit.vutbr.cz>
  */
 public class Database implements IDatabase {
 	
 	private DatabaseContacts contacts;
 	private DatabaseGroups groups;
 	private DatabaseGroupsContacts groupsContacts;
 	private DatabaseAuth tokens;
 	
 	private enum TableType {
 		CONTACTS, GROUPS, GROUPSCONTACTS, AUTH, ALL
 	}	
 	
 	private boolean autoCommit = true;	
 	
 	private static Database instance;
 	
 	public static Database getInstance() {
 		if (instance == null) {
 			instance = new Database();
 		}
 		
 		return instance;
 	}
 	
 	private Database() {		
     this.log("DB: Init database"); 
 		this.contacts = new DatabaseContacts();
 		this.groups = new DatabaseGroups();
 		this.groupsContacts = new DatabaseGroupsContacts();
 		this.tokens = new DatabaseAuth();
 	}
 	
 	private void commitChanges(TableType day) {
 		if (this.autoCommit) {
 			switch(day) {
 				case CONTACTS:
 					this.contacts.save();
 					break;
 				case GROUPS:
 					this.groups.save();
 					break;				
 				case GROUPSCONTACTS:
 					this.groupsContacts.save();
 					break;	
 				case AUTH:
 					this.tokens.save();
 					break;
 				case ALL:
 					this.contacts.save();
 					this.groups.save();					
 					this.groupsContacts.save();
           this.tokens.save();
 					break;
 			}
 		}
 	}
   
   public void saveAllData() {
     this.log("DB: Save all data");      
     this.commitChanges(TableType.ALL);
   }
 	
 	public void clearAllData() {
     this.log("DB: Clear all data");     
 		this.contacts.clear();
 		this.groups.clear();
 		this.groupsContacts.clear();
 		this.tokens.clear();
 
 		this.commitChanges(TableType.ALL);
 	} 	
 	
 	@Override // DONE 
 	public List<Contact> getAllContacts() {    
     this.log("DB: Get all contacts");        
 		return this.contacts.getAllContacts();
 	}
 
 	@Override // DONE
 	public List<Contact> getAllContactsFromGroups(List<Group> requiredGroups) {
 		
     if (requiredGroups.isEmpty()) {
       return new ArrayList<Contact>();
     }
     
 		// groupId == -1 indicate all contacts
 		for (Group group : requiredGroups) {
 			if (group.getId() == -1) {
         this.log("DB: Get all contacts from groups [ROOT]");         
 				return this.contacts.getAllContacts();
 			}
 		}		
     
 		List<Integer> contactsId = this.groupsContacts.getContactsIdAssignToGroups(requiredGroups);			
     this.log("DB: Get all contacts from groups " + requiredGroups.toString());          
 		return this.contacts.filterByIds(contactsId);
 	}
   
   public int getNumberOfContactsForGroup (Group group) {
        
    // main group
    if (group.getId() == -1) {
      return this.contacts.getAllContacts().size();
    }
    
     List<Group> groupList = new ArrayList<Group>();
     groupList.add(group);
    
    return this.groupsContacts.getContactsIdAssignToGroups(groupList).size();			
   }
 
 	/**
 	 * Get group by name.
 	 * 
 	 * @param name
 	 * @return
 	 */
   @Override
 	public Group getGroupByName(String name) {
     this.log("DB: Get group by name " + name);          
 		return this.groups.getGroupByName(name);
 	}
 	
 	@Override // DONE
 	public List<Group> getAllGroups() {
     this.log("DB: Get all groups");       
 		return this.groups.getAllGroups();
 	}
 
 	@Override // DONE
 	public List<Group> addNewGroup(String name) {
 
 		if (!this.groups.addNew(name)) {
 			return null;
 		}
     this.log("DB: Added new group with name " + name);    		
     
 		this.commitChanges(TableType.GROUPS);
 		return this.getAllGroups();
 	}
 
 	@Override // DONE
 	public List<Group> removeGroups(List<Group> groupsToRemove) {
 		this.groups.removeGroup(groupsToRemove);
 		this.groupsContacts.removeGroupsEntries(groupsToRemove);
     this.log("DB: Removed " + groupsToRemove.size() + " groups");      
 		this.commitChanges(TableType.GROUPS);
 		this.commitChanges(TableType.GROUPSCONTACTS);			
 		return this.groups.getAllGroups();
 	}
 	
 	@Override // TOTEST
 	public List<Group> renameGroup(Group group, String newName) {
 		if (!this.groups.renameGroup(group, newName)) {
 			return null;
 		}
     this.log("DB: Renamed group " + group.toString());     
 		this.commitChanges(TableType.GROUPS);
 		return this.groups.getAllGroups();
 	}	
 
 	@Override // DONE
 	public List<Contact> addNewContacts(List<Contact> contacts) {
 		this.contacts.addNew(contacts);
     this.log("DB: Added " + contacts.size() + " contacts");       
 		this.commitChanges(TableType.CONTACTS);		
 		return this.contacts.getAllContacts();
 	}
 
 	@Override // DONE
 	public List<Contact> updateContact(Contact contact) {
 		this.contacts.update(contact);
     this.log("DB: Updated contact " + contact.toString());       
 		this.commitChanges(TableType.CONTACTS);					
 		return this.getAllContacts();
 	}
 
 	@Override // DONE
 	public List<Contact> removeContacts(List<Contact> contactsToRemove) {
 		this.contacts.remove(contactsToRemove);		
 		this.groupsContacts.removeContactsEntries(contactsToRemove);
     this.log("DB: Deleted " + contactsToRemove.size() + " contacts");
 		this.commitChanges(TableType.CONTACTS);		
 		this.commitChanges(TableType.GROUPSCONTACTS);				
 		return this.contacts.getAllContacts();
 	}
 
 	@Override // DONE
 	public List<Contact> addContactsToGroup(Group group, List<Contact> contactsToAdd) {
 		this.groupsContacts.addContactsToGroup(group, contactsToAdd);
     this.log("DB: Added " + contactsToAdd.size() + " contacts to the group " + group.getName());    
 		this.commitChanges(TableType.GROUPSCONTACTS);
 		return this.getAllContactsFromGroup(group);
 	}
 	
 	// TOTEST
 	public List<Contact> updateContactsGroup(List<Contact> contacts, List<Group> groups) {
 		this.groupsContacts.updateContactsGroups(contacts, groups);
 		this.commitChanges(TableType.GROUPSCONTACTS);
 		return this.getAllContacts();
 	}	
 	
 	// TOTEST
 	public List<Contact> updateGroupsContacts(List<Group> groups, List<Contact> contacts) {
 		this.groupsContacts.updateGroupsContacts(groups, contacts);     
 		this.commitChanges(TableType.GROUPSCONTACTS);
 		return this.getAllContacts();
 	}		
 
 	@Override // DONE
 	public List<Contact> removeContactsFromGroup(Group group, List<Contact> contactsToRemove) {
 		this.groupsContacts.removeContactsFromGroup(group, contactsToRemove);
     this.log("DB: Removed " + contactsToRemove.size() + " from group " + group.toString());         
 		this.commitChanges(TableType.GROUPSCONTACTS);
 		return this.getAllContactsFromGroup(group);
 	}
 
 	@Override // DONE
 	public List<Group> getAllGroupsForContact(Contact contact) {
 		List<Integer> groupsId = this.groupsContacts.filterByContactId(contact.getId());
     this.log("DB: Get all groups for contact " + contact.toString());             
 		return this.groups.filter(groupsId);
 	}
 	
 	private List<Contact> getAllContactsFromGroup(Group group) {
 		List<Group> requiredGroups = new ArrayList<Group>();
 		requiredGroups.add(group);
     this.log("DB: Get all contacts from group " + group.toString());   		
 		return this.getAllContactsFromGroups(requiredGroups);
 	}
 
 	/* tokens management ******************************************************/
 
 	/**
 	 * Get token specified by service.
 	 * 
 	 * @param service
 	 * @return
 	 */
   @Override
 	public AuthToken getToken(ServicesEnum service) {
 		return this.tokens.get(service.getCode());
 	}
 
 	/**
 	 * Get token specified by service.
 	 *
 	 * @param service
 	 * @return
 	 */
   @Override
 	public AuthToken getToken(Integer service) {
 		return this.tokens.get(service);
 	}
 
 	/**
 	 * Save new token to database.
 	 * 
 	 * @param token
 	 * @return
 	 */
   @Override
 	public AuthToken addToken(AuthToken token) {
 		this.tokens.add(token);
 		this.commitChanges(TableType.AUTH);
 		return this.tokens.get(token.getService());
 	}
 
 	/**
 	 * Remove token from database.
 	 * 
 	 * @param service
 	 */
   @Override
 	public void removeToken(Integer service) {
 		this.tokens.remove(this.tokens.get(service));
 		this.commitChanges(TableType.AUTH);
 	}
 
 	/**
 	 * Remove token from database.
 	 *
 	 * @param service
 	 */
   @Override
 	public void removeToken(ServicesEnum service) {
 		this.removeToken(service.getCode());
 	}
 
 	/* birthday ***************************************************************/
 
 	/**
 	 * Retrieve contacts with birthday within one month.
 	 * 
 	 * @return
 	 */
     @Override
 	public List<Contact> getContactsWithBirtday() {
 		List<Contact> all = this.contacts.getAllContacts();
 		List<Contact> csWithBday = new ArrayList<Contact>();
 		for (Contact c : all) {
             if (c.getDates() == null) {
                 continue;
             }
             for (Event e : c.getDates()) {
                 if (e.getDate() != null && !e.isShowingDisabled() && e.isBirthday() && e.isWithinMonth()) {
                     csWithBday.add(c);
                 }
             }
 		}
 		return csWithBday;
 	}
   
   /**
    * Method for messages logging.
    * @param msg message to log
    */
   private void log(String msg) {
     LoggerFactory.getLogger(this.getClass()).info(msg);
   }    
 }
