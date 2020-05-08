 package ru.korpse.screenshots.core.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.stereotype.Repository;
 
 import ru.korpse.screenshots.entities.Shot;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 
 @Repository
 public class ShotDao {
 	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
 
 	private Shot convert(Entity item) {
 		Shot result = new Shot();
 		result.setKey(KeyFactory.keyToString(item.getKey()));
 		result.setBlobKey((String) item.getProperty("blobKey"));
 		result.setCreated((Date) item.getProperty("created"));
 		return result;
 	}
 
 	private Entity convert(Shot item) {
 		Entity result = new Entity("Shot");
 		result.setProperty("blobKey", item.getBlobKey());
 		result.setProperty("created", item.getCreated());
 		return result;
 	}
 	
 	public void save(Shot item) {
 		if (StringUtils.isEmpty(item.getBlobKey())) {
 			return;
 		}
 		Key key = datastore.put(convert(item));
 		item.setKey(KeyFactory.keyToString(key));
 	}
 
 	public Shot get(String keyString) throws EntityNotFoundException {
 		if (StringUtils.isEmpty(keyString)) {
 			return null;
 		}
 		return convert(datastore.get(KeyFactory.stringToKey(keyString)));
 	}
 	
 	@SuppressWarnings("deprecation")
 	public List<Shot> getOlderThan(Date created) {
 		Query q = new Query("Shot");
 		q.addFilter("created", Query.FilterOperator.LESS_THAN, created);
 		List<Shot> result = new ArrayList<Shot>();
 		for (Entity e : datastore.prepare(q).asIterable()) {
 			result.add(convert(e));
 		}
 		return result;
 	}
 	
 	public void delete(Shot item) {
 		blobstoreService.delete(new BlobKey(item.getBlobKey()));
 		datastore.delete(KeyFactory.stringToKey(item.getKey()));
 	}
 }
