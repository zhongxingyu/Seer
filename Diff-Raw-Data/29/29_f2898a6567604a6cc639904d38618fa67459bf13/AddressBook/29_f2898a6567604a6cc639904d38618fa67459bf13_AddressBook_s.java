 package com.untzuntz.ustack.data;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.bson.types.ObjectId;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.BasicDBObjectBuilder;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.untzuntz.ustack.exceptions.InvalidSiteAccountName;
 import com.untzuntz.ustack.exceptions.InvalidUserAccountName;
 import com.untzuntz.ustack.main.UAppCfg;
 import com.untzuntz.ustack.main.UOpts;
 import com.untzuntz.ustack.uisupport.UEntryError;
 
 /**
  * The address book object allows you to manage an address book per user.
  * 
  * An address book can 'subscribe' to other address books for common address book access
  * 
  * @author jdanner
  *
  */
 public class AddressBook extends UntzDBObject {
 
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(AddressBook.class);
 	
 	public String getCollectionName() { return "addressBooks"; }
 	
 	public static DBCollection getDBCollection() {
 		return new AddressBook().getCollection();
 	}
 
 	/** Do not use */
 	private AddressBook() {}
 	
 	private AddressBook(String name)
 	{
 		// setup basic values on account
 		setName(name);
 		put("created", new Date());
 		entries = new Vector<AddressBookEntry>();
 	}
 
 	public String getAddressBookId() {
 		return get("_id") + "";
 	}
 
 	/** Return the name of the database that houses the 'users' collection */
 	public static final String getDatabaseName() {
 		
 		if (UOpts.getString(UAppCfg.DATABASE_ADDRBOOK_COL) != null)
 			return UOpts.getString(UAppCfg.DATABASE_ADDRBOOK_COL);
 		
 		return UOpts.getAppName();
 		
 	}
 	
 	public void setName(String name) {
 		put("name", name);
 	}
 	
 	public String getName() {
 		return getString("name");
 	}
 
 	/**
 	 * Generate a AddressBook object from the MongoDB object
 	 * @param user
 	 */
 	public AddressBook(DBObject object) {
 		super(object);
 		
 		entries = new Vector<AddressBookEntry>();
 		reloadEntries();
 	}
 
 	/**
 	 * Reloads all the address book entries and subscription values
 	 */
 	private void reloadEntries()
 	{
 		entries.clear();
 
 		boolean needSave = false;
 		// load entries from this address book
 		loadAll(this);
 		
 		// load entries from subscribed address books
 		BasicDBList subscrList = getSubscriptionList();
 		for (int i = 0; i < subscrList.size(); i++)
 		{
 			DBObject map = (DBObject)subscrList.get(i);
 			String id = (String)map.get("addrBookId");
 			if (id.equalsIgnoreCase(getAddressBookId())) // don't try to load myself
 				continue;
 			
 			AddressBook subscribedBook = AddressBook.getById( id );
 			if (subscribedBook != null && !subscribedBook.getAddressBookId().equalsIgnoreCase(getAddressBookId()))
 			{
 				logger.info("Loading Subscribed Book '" + subscribedBook.getAddressBookId() + "'");
 				loadAll(subscribedBook);
 			}
 			else
 			{
 				logger.info("Removing subscribed address book '" + map.get("addrBookId") + "/" + map.get("name") + "' -- it does not exist (Removed from '" + get("_id") + "'");
 				subscrList.remove(i);
 				needSave = true;
 				i--;
 			}
 		}
 
 		if (needSave)
 			save();
 	}
 	
 	private List<AddressBookEntry> entries;
 	
 	public List<AddressBookEntry> getEntries() {
 		return entries;
 	}
 	
 	public AddressBookEntry getEntryByInternalLinkId(String id) {
 
 		if (id == null)
 			return null;
 		
 		for (AddressBookEntry a : entries)
 		{
 			if (id.equalsIgnoreCase( a.getLink() ))
 				return a;
 		}
 
 		return null;
 	}
 	
 	public List<AddressBookEntry> getRecent(String userName, String type, int count)
 	{
 		List<AddressBookEntry> ret = new Vector<AddressBookEntry>();
 		
 		Collections.sort(entries, new LastUsedByUser(userName));
 		
 		for (int i = 0; ret.size() < count && i < entries.size(); i++)
 		{
 			if (type.equalsIgnoreCase(entries.get(i).getType()))
 				ret.add(entries.get(i));
 		}
 		
 		return ret;
 	}
 	
 	public class LastUsedByUser implements Comparator<AddressBookEntry> {
 		
 		private String userName;
 		
 		public LastUsedByUser(String user) { 
 			userName = user;
 		}
 
 		public int compare(AddressBookEntry o1, AddressBookEntry o2) {
 			
 			Date lu1 = o1.getLastUsed(userName);
 			Date lu2 = o2.getLastUsed(userName);
 			
 			if (lu1 == null && lu2 == null)
 				return 0;
 			
 			if (lu1 == null)
 				return 1;
 			
 			if (lu2 == null)
 				return -1;
 			
 			if (lu1.after(lu2))
 				return -1;
 			
 			return 1;
 		}
 		
 	}
 	
 	public BasicDBList getEntryList() {
 		BasicDBList ret = (BasicDBList)get("entryList");
 		if (ret == null)
 			ret = new BasicDBList();
 		
 		return ret;
 	}
 	
 	public void setEntryList(BasicDBList list)
 	{
 		put("entryList", list);
 	}
 
 	/**
 	 * Subscribes this address book to another address book
 	 * 
 	 * @param existingBook
 	 * @param actor
 	 */
 	public void subscribeTo(AddressBook existingBook, String actor)
 	{
 		BasicDBList subList = getSubscriptionList();
 		boolean found = false;
 		for (int i = 0; i < subList.size(); i++)
 		{
 			DBObject map = (DBObject)subList.get(i);
 			AddressBook subscribedBook = AddressBook.getById( (String)map.get("addrBookId") );
 			if (subscribedBook != null)
 			{
 				found = true;
 				map.put("name", subscribedBook.getName());
 			}
 		}
 
 		if (!found && !existingBook.getAddressBookId().equalsIgnoreCase(getAddressBookId()))
 		{
 			DBObject map = new BasicDBObject("addrBookId", existingBook.getAddressBookId());
 			map.put("name", existingBook.getName());
 			map.put("created", new Date());
 			map.put("subscribedBy", actor);
 			subList.add(map);
 		}
 		
 		setSubscriptionList(subList);
 		
 		loadAll(existingBook);
 	}
 
 	/**
 	 * Unsubscribes this address book from another address book
 	 * @param existingBook
 	 */
 	public void unsubscribeFrom(AddressBook existingBook)
 	{
 		BasicDBList subList = getSubscriptionList();
 		for (int i = 0; i < subList.size(); i++)
 		{
 			DBObject map = (DBObject)subList.get(i);
 			if (existingBook.getAddressBookId().equalsIgnoreCase( (String)map.get("addrBookId") ))
 			{
 				subList.remove(i);
 				i--;
 			}
 		}
 		setSubscriptionList(subList);
 		
 		reloadEntries();
 	}
 	
 	public BasicDBList getSubscriptionList() {
 		BasicDBList ret = (BasicDBList)get("subscrList");
 		if (ret == null)
 			ret = new BasicDBList();
 		
 		return ret;
 	}
 	
 	public void setSubscriptionList(BasicDBList list)
 	{
 		put("subscrList", list);
 	}
 
 	private void loadAll(AddressBook book)
 	{
 		if (book == null)
 			return;
 		
 		BasicDBList entryList = book.getEntryList();
 		logger.info("Loading " + entryList.size() + " entries from book '" + book.getAddressBookId() + "'");
 		for (int i = 0; i < entryList.size(); i++)
 		{
 			AddressBookEntry entry = new AddressBookEntry((DBObject)entryList.get(i));
 			entry.put("srcAddrBookId", book.getAddressBookId());
 			loadEntry(entry);
 		}
 	}
 	
 	/**
 	 * Loads the entry to the in-memory address book (this is where subscribed address books are loaded)
 	 * 
 	 * @param entry
 	 */
 	private boolean loadEntry(AddressBookEntry entry)
 	{
 		String matchCheck = entry.getMatch();
 		for (AddressBookEntry curEntry : entries)
 		{
 			if (matchCheck.equalsIgnoreCase( curEntry.getMatch() )) // already have it in this address book
 			{
 				if (getAddressBookId().equalsIgnoreCase(entry.getString("srcAddrBookId")))
 				{
 					// update display name text
 					BasicDBList entryList = getEntryList();
 					for (int i = 0; i < entryList.size(); i++)
 					{
 						DBObject chk = (DBObject)entryList.get(i);
 						if (entry.getMatch().equalsIgnoreCase( (String)chk.get("match") ))
 						{
 							chk.put("displayVal", entry.getString("displayVal"));
 							curEntry.setDisplayValue(entry.getString("displayVal"));
 						}
 					}
 					setEntryList(entryList);
 				}
 				
 				return false;
 			}
 		}
 		
 		entries.add(entry);
 		return true;
 	}
 
 	/**
 	 * Adds an entry to this address book, checks for existing matching entries first
 	 * @param entry
 	 */
 	public void addEntry(AddressBookEntry entry)
 	{
 		if (loadEntry(entry))
 		{
 			BasicDBList entryList = getEntryList();
 			entryList.add(entry);
 			setEntryList(entryList);
 		}
 	}
 	
 	public void updateEntry(AddressBookEntry entry)
 	{
 		BasicDBList entryList = getEntryList();
 		boolean found = false;
 		for (int i = 0; !found && i < entryList.size(); i++)
 		{
 			DBObject test = (DBObject)entryList.get(i);
			if (entry.getMatch().equalsIgnoreCase( (String)test.get("match") ))
 			{
 				found = true;
 				entryList.set(i, entry);
 			}
 		}
 
 		if (!found)
 			entryList.add(entry);
 		
 		setEntryList(entryList);
 	}
 
 	/**
 	 * Removes an entry from this address book only (not subscribed books)
 	 * @param entry
 	 */
 	public void removeEntry(AddressBookEntry entry)
 	{
 		BasicDBList entryList = getEntryList();
 		for (int i = 0; i < entryList.size(); i++)
 		{
 			DBObject chk = (DBObject)entryList.get(i);
 			if (entry.getMatch().equalsIgnoreCase( (String)chk.get("match") ))
 			{
 				entryList.remove(i);
 				i--;
 			}
 		}
 	}
 	
 	public AddressBookEntry getByGroupId(String groupId)
 	{
 		if (groupId == null)
 			return null;
 		
 		groupId = groupId.trim();
 		
 		List<AddressBookEntry> entries = getEntries();
 		for (AddressBookEntry e : entries)
 		{
 			if (groupId.equalsIgnoreCase((String)e.get("groupId")))
 				return e;
 		}
 		
 		return null;
 
 	}
 	
 	public AddressBookEntry getEntryByDisplayName(String name)
 	{
 		if (name == null)
 			return null;
 		
 		name = name.trim();
 		
 		List<AddressBookEntry> entries = getEntries();
 		for (AddressBookEntry e : entries)
 		{
 			if (e.getDisplayValue().equalsIgnoreCase(name))
 				return e;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Updates an entry's usage time
 	 * @param match
 	 * @param actor
 	 */
 	public void updateUsage(String match, String displayVal, String actor)
 	{
 		BasicDBList entryList = getEntryList();
 		for (int i = 0; i < entryList.size(); i++)
 		{
 			DBObject chk = (DBObject)entryList.get(i);
 			if (match.equalsIgnoreCase( (String)chk.get("match") ))
 			{
 				AddressBookEntry entry = new AddressBookEntry(chk);
 				entry.setDisplayValue(displayVal);
 				entry.markUsed(actor);
 				entryList.set(i, entry);
 			}
 		}
 		setEntryList(entryList);
 	}
 	
 	/**
 	 * Get a address book by id
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public static AddressBook getById(String id)
 	{
 		if (id == null)
 			return null;
 		
 		DBObject book = new AddressBook().getCollection().findOne(BasicDBObjectBuilder.start("_id", new ObjectId(id)).get());
 		
 		if (book == null)
 			return null;
 		
 		return new AddressBook(book);
 	}
 
 	/**
 	 * Get a address book by siteId
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public static AddressBook getBySiteId(String siteId) throws InvalidSiteAccountName
 	{
 		if (siteId == null)
 			return null;
 		
 		DBObject book = new AddressBook().getCollection().findOne(BasicDBObjectBuilder.start("siteId", siteId).get());
 		
 		if (book == null)
 		{
 			SiteAccount site = SiteAccount.getSiteById(siteId);
 			if (site == null)
 				throw new InvalidSiteAccountName("Unknown ID");
 			
 			AddressBook newBook = new AddressBook(site.getSiteName());
 			newBook.put("siteId", siteId);
 			return newBook;
 		}
 		
 		return new AddressBook(book);
 	}
 	
 	/**
 	 * Get a address book by userId
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public static AddressBook getByUserId(String userId) throws InvalidUserAccountName
 	{
 		if (userId == null)
 			return null;
 		
 		DBObject book = new AddressBook().getCollection().findOne(BasicDBObjectBuilder.start("userId", userId).get());
 		
 		if (book == null)
 		{
 			UserAccount user = UserAccount.getUserById(userId);
 			if (user == null)
 				throw new InvalidUserAccountName("Unknown ID");
 			
 			AddressBook newBook = new AddressBook(user.getUserName());
 			newBook.put("userId", userId);
 			return newBook;
 		}
 		
 		return new AddressBook(book);
 	}
 	
 	/**
 	 * Get a address book by name
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public static AddressBook getByName(String name)
 	{
 		if (name == null)
 			return null;
 		
 		DBObject book = new AddressBook().getCollection().findOne(BasicDBObjectBuilder.start("name", name).get());
 		
 		if (book == null)
 			return new AddressBook(name);
 		
 		return new AddressBook(book);
 	}
 	
 }
