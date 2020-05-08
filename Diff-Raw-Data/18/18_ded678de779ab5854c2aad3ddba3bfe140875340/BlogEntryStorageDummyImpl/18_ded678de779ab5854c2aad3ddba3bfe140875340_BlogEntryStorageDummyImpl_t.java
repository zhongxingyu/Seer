 package example.springmvc.model.blog.impl;
 
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.UUID;
 
 import example.springmvc.model.blog.BlogEntry;
 import example.springmvc.model.blog.BlogEntryStorage;
 
 public class BlogEntryStorageDummyImpl implements BlogEntryStorage {
 
 	private Hashtable<String, BlogEntry> data = new Hashtable<>();
 	
 	@Override
 	public BlogEntry byId(String id) {
 		return this.data.get(id);
 	}
 
 	@Override
 	public void saveOrUpdate(BlogEntry entry) {
		String id = entry.getId();
		if (id == null || id.length() == 0) {
 			entry.setId(UUID.randomUUID().toString());
 		}
 		this.data.put(entry.getId(), entry);
 	}
 
 	@Override
 	public List<BlogEntry> findAll() {
 		List<BlogEntry> list = new LinkedList<>();
 		list.addAll(this.data.values());
 		return list;
 	}
 
 }
