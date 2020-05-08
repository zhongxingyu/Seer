 package com.untzuntz.ustack.data;
 
 import java.util.Date;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 
 /**
  * A single address book entry
  * 
  * @author jdanner
  *
  */
 public class AddressBookEntry extends BasicDBObject {
 
 	private static final long serialVersionUID = 1L;
 	
 	@SuppressWarnings("unused")
 	private AddressBookEntry() {}
 	
 	public AddressBookEntry(UserAccount user)
 	{
 		put("match", user.getUserName());
 		put("created", new Date());
 		setDisplayValue(user.getFullName() + " <" + user.getUserName() + ">");
 		setType("user");
 		setLink(user.getUserName());
 	}
 	
 	public AddressBookEntry(SiteAccount site)
 	{
 		put("match", site.getSiteId());
 		put("created", new Date());
 		setDisplayValue(site.getSiteName());
 		setType("site");
 		setLink(site.getSiteId());
 	}
 	
 	public AddressBookEntry(DBObject input)
 	{
 		putAll(input);
 	}
 	
 	public AddressBookEntry(String match)
 	{
 		put("match", match);
 		put("created", new Date());
 		setLink(match);
 	}
 
 	public String getMatch() { 
 		return getString("match"); 
 	}
 	
 	public void setDisplayValue(String val) {
 		put("displayVal", val);
 	}
 	
 	public String getDisplayValue() {
 		return getString("displayVal");
 	}
 	
 	public void setType(String val) {
 		put("type", val);
 	}
 	
 	public String getType() {
 		return getString("type");
 	}
 	
 	public void setLink(String val) {
 		put("internalLink", val);
 	}
 	
 	public String getLink() {
 		return getString("internalLink");
 	}
 	
 	/**
 	 * Only valid for 'group' types
 	 * @param display
 	 * @param name
 	 */
 	public void addGroupMember(String display, String internalId, String internalLink)
 	{
 		BasicDBList memberList = (BasicDBList)get("memberList");
 		if (memberList == null)
 			memberList = new BasicDBList();
 
 		DBObject member = null;
 		for (int i = 0; member == null && i < memberList.size(); i++)
 		{
 			DBObject test = (DBObject)memberList.get(i);
			if (internalLink.equals( (String)test.get("siteId") ))
				member = test;
			if (internalLink.equals( (String)test.get("userName") ))
 				member = test;
 		}
 
 		if (member == null)
 		{
 			member = new BasicDBObject();
 			member.put(internalId, internalLink);
 		}
 		
 		member.put("displayVal", display);
		memberList.add(member);
 
 		put("memberList", memberList);
 	}
 	
 	public void removeGroupMember(String internalLink)
 	{
 		BasicDBList memberList = (BasicDBList)get("memberList");
 		if (memberList == null)
 			memberList = new BasicDBList();
 
 		for (int i = 0; i < memberList.size(); i++)
 		{
 			DBObject member = (DBObject)memberList.get(i);
 			if (internalLink.equals( (String)member.get("siteId") ))
 				memberList.remove(i);
 			if (internalLink.equals( (String)member.get("userName") ))
 				memberList.remove(i);
 		}
 	}
 
 	/**
 	 * Returns the last time the 'actor' used this entry
 	 * 
 	 * @param actor
 	 * @return
 	 */
 	public Date getLastUsed(String actor)
 	{
 		BasicDBList usageList = getUsageList();
 		for (int i = 0; i < usageList.size(); i++)
 		{
 			DBObject item = (DBObject)usageList.get(i);
 			if (actor.equalsIgnoreCase( (String)item.get("actor") ))
 				return (Date)item.get("lastUsed");
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Indicates a particular entry in the address book was used by an actor
 	 * 
 	 * @param actor
 	 */
 	public void markUsed(String actor)
 	{
 		// try to find existing record
 		BasicDBList usageList = getUsageList();
 		DBObject target = null;
 		for (int i = 0; target == null && i < usageList.size(); i++)
 		{
 			DBObject item = (DBObject)usageList.get(i);
 			if (actor.equalsIgnoreCase( (String)item.get("actor") ))
 				target = item;
 		}
 		
 		if (target == null)
 		{
 			target = new BasicDBObject("actor", actor);
 			usageList.add(target);
 			setUsageList(usageList);
 		}
 		
 		target.put("lastUsed", new Date());
 	}
 	
 	private BasicDBList getUsageList() {
 		BasicDBList ret = (BasicDBList)get("usageList");
 		if (ret == null)
 			ret = new BasicDBList();
 		
 		return ret;
 	}
 	
 	private void setUsageList(BasicDBList list)
 	{
 		put("usageList", list);
 	}
 }
