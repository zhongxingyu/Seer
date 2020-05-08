 package com.reimDev.reimWeb.server.dao;
 
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 import com.reimDev.reimWeb.client.entity.BlogComment;
 import com.reimDev.reimWeb.client.entity.BlogEntry;
 
 public class BlogDao extends Dao {
 //	private static Logger log = LoggerFactory.getLogger(BlogDao.class);
 	
 	public List<BlogEntry> getAllBlogEntries() {
 		Objectify ofy = ObjectifyService.begin();
 		
		List<BlogEntry> entries = ofy.query(BlogEntry.class).order("-created").list();
 //		log.debug("retrieved "+entries.size()+" BlogEntries");
 		return entries;
 	}
 	
 	public void addBlogEntry(BlogEntry entry) {
 		Objectify ofy = ObjectifyService.begin();
 		ofy.put(entry);
 	}
 	
 	public int getNumberOfBlogEntries() {
 		Objectify ofy = ObjectifyService.begin();
 		List<BlogEntry> list = ofy.query(BlogEntry.class).list();
 		return list.size();
 	}
 	
 	public int getNumberOfBlogEntries(Date from, Date to) {
 		return getEntriesByDate(from, to).size();
 	}
 	
 	public BlogEntry getEntryByTitle(String title) {
 		Objectify ofy = ObjectifyService.begin();
 		return ofy.query(BlogEntry.class).filter("title", title).get();
 	}
 	
 	public BlogEntry getByEntryId(int id) {
 		Objectify ofy = ObjectifyService.begin();
 		return ofy.query(BlogEntry.class).filter("entryId", id).get();
 	}
 	
 	public BlogEntry getEntry(String id) {
 		Objectify ofy = ObjectifyService.begin();
 		return ofy.query(BlogEntry.class).filter("id", id).get();
 	}
 	
 	public List<BlogEntry> getEntriesByDate(Date from, Date to) {
 		Objectify ofy = ObjectifyService.begin();
		List<BlogEntry> list = ofy.query(BlogEntry.class).order("-created").filter("created >=", from).filter("created <=", to).list();
 		return list;
 	}
 	
 	public List<BlogComment> getComments(String entryId) {
 		Objectify ofy = ObjectifyService.begin();
		List<BlogComment> list = ofy.query(BlogComment.class).order("-created").filter("entryId", entryId).list();
 		return list;		
 	}
 }
