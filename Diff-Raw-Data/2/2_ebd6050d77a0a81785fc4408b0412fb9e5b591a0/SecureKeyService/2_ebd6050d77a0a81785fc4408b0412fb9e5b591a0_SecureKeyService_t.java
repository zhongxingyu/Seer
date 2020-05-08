 package ru.korpse.screenshots.core.services;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.stereotype.Service;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 
 @Service
 public class SecureKeyService {
 	
 	private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
 	
 	private String getHash(String str) {
         MessageDigest md5 ;        
         StringBuffer  hexString = new StringBuffer();
         try {
             md5 = MessageDigest.getInstance("md5");
             md5.reset();
             md5.update(str.getBytes()); 
                         
             byte messageDigest[] = md5.digest();
                         
             for (int i = 0; i < messageDigest.length; i++) {
                 hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
             }
         } 
         catch (NoSuchAlgorithmException e) {                        
             return e.toString();
         }
         
         return hexString.toString();
     }
 	
 	public String getSecureKey(String address, String uploadUrl) {
 		return getHash(getHash(address + "fuck") + getHash(uploadUrl + "fuck"));
 	}
 	
 	public void save(String key, String address) {
 		Entity item = new Entity("SecureKey", key);
 		item.setProperty("Address", address);
 		item.setProperty("created", new Date());
 		datastoreService.put(item);
 	}
 	
 	public String getAddrByKey(String key) {
 		if (StringUtils.isEmpty(key)) {
 			return null;
 		}
 		try {
 			Entity keyEntity = datastoreService.get(KeyFactory.createKey("SecureKey", key));
 			return (String) keyEntity.getProperty("Address");
 		} catch (EntityNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public void deleteKey(String key) {
 		if (StringUtils.isEmpty(key)) {
 			return;
 		}
 		datastoreService.delete(KeyFactory.createKey("SecureKey", key));
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void deleteOld() {
 		Query q = new Query("SecureKey");
 		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -2);
 		q.addFilter("created", FilterOperator.LESS_THAN, calendar.getTime());
 		PreparedQuery pq = datastoreService.prepare(q);
 		for (Entity item : pq.asIterable()) {
 			datastoreService.delete(item.getKey());
 		}
 	}
 }
