 package com.couchbase.plugin.client;
 
 import com.couchbase.client.CouchbaseClient;
 import com.couchbase.client.CouchbaseConnectionFactory;
 import net.spy.memcached.internal.OperationFuture;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tgrall
  * Date: 6/26/13
  * Time: 2:00 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CouchbaseClientWithVersioning extends CouchbaseClient {
 
     private static final Logger logger = Logger.getLogger(CouchbaseClientWithVersioning.class.getName());
 
     public static final String VERSION_NUMBER_PREFIX = "::v";
     public static final String VERSION_COUNTER_SUFFIX = "_version";
 
 	private boolean automaticVersionning = false;
 
 
     public CouchbaseClientWithVersioning(List<URI> baseList, String bucketName, String pwd) throws IOException {
         super(baseList, bucketName, pwd);
     }
 
     public CouchbaseClientWithVersioning(List<URI> baseList, String bucketName, String user, String pwd) throws IOException {
         super(baseList, bucketName, user, pwd);
     }
 
     public CouchbaseClientWithVersioning(CouchbaseConnectionFactory cf) throws IOException {
         super(cf);
     }
 
 	@Override
 	public OperationFuture<Boolean> set(String key, Object value) {
 		if (this.isAutomaticVersionning()) {
 				return this.set(key, value, true);
 		}
 		return super.set(key, value);
 	}
 
 	public OperationFuture<Boolean> set(String key, Object value, boolean versionIt)  {
         if (versionIt) {
             Object obj = this.get(key);
             if (obj != null) {
                 // get the next version
                long version = this.incr(key.concat(VERSION_COUNTER_SUFFIX), 1, 1);
                String keyForVersion = key.concat(VERSION_NUMBER_PREFIX).concat(Long.toString(version));
 				try {
 					this.set(keyForVersion, obj).get();
 				} catch (Exception e) {
 					logger.severe("Cannot save version "+ version + " for key "+ key +" - Error:"+ e.getMessage() );
 				}
 				return super.set(key, value);
             } else {
                 // if the key does not exist create it, so no version
                 return super.set(key, value);
             }
         } else {
             return super.set(key, value);
         }
     }
 
 
     public OperationFuture<Boolean> replace(String key, String value) throws ExecutionException, InterruptedException {
         Object obj = this.get(key);
         if (obj != null) {
             // get the next version
            long version = this.incr(key.concat(VERSION_COUNTER_SUFFIX), 1, 1);
            String keyForVersion = key.concat(VERSION_NUMBER_PREFIX).concat(Long.toString(version));
             this.add(keyForVersion, obj).get();
             return super.set(key, value);
         } else {
             // if the key does not exist create it, so no version
             return super.replace(key, value);
         }
     }
 
 
     /**
      * This function is used to retrieve a specific version of a document/value
      * if the value does not exist the function returns null.
      *
      * @param key
      * @param version
      * @return  the value for the specific version
      */
     public Object get(String key, int version) {
        return super.get(key.concat(VERSION_NUMBER_PREFIX).concat(Integer.toString(version)));
     }
 
 
     /**
      * This function returns all the versions -including the current one- of documents/values
      *
      * @param key
      * @return a map of K,V of all the versions associated with this key
      */
     public Map<String, Object> getAllVersions(String key) {
         Map<String, Object>  returnValue = null;
         List keys = new ArrayList();
         keys.add( key );
         // TODO: check if the get bulk is the proper approach (no sorting)
         Object maxVersionNumObj = super.get(key + CouchbaseClientWithVersioning.VERSION_COUNTER_SUFFIX);
         if ( maxVersionNumObj != null ) {
           int maxVersionNum = Integer.parseInt( (String)maxVersionNumObj );
           for( int i = 1; i<=maxVersionNum ; i++ ) {
               StringBuilder sb = new StringBuilder(key);
               sb.append( CouchbaseClientWithVersioning.VERSION_NUMBER_PREFIX ).append(i);
               keys.add( sb.toString()  );
           }
             returnValue = this.getBulk(keys);
         } else {
             returnValue = this.getBulk(keys);
         }
         return returnValue;
     }
 
 
 	/**
 	 * Delete a version of the document
 	 * @param key
 	 * @param version
 	 * @return whether or not the operation was performed
 	 */
     public  OperationFuture<java.lang.Boolean> deleteVersion(String key, int version) {
        return super.delete(key.concat(VERSION_NUMBER_PREFIX).concat(Integer.toString(version)));
     }
 
 
 	/**
 	 * Delete the key, the version counter and all versions associated to this key
 	 * @param key
 	 * @return whether or not the operation was performed
 	 */
 	public OperationFuture<Boolean> delete(String key) {
 
         // need to delete all the version first
        Object vObject = this.get(key.concat(VERSION_COUNTER_SUFFIX));
         if (vObject != null) {
 
             long biggerVersion = Long.parseLong((String) vObject);
             try {
                 // delete all the versions
                 for (int i = 1; i <= biggerVersion; i++) {
                     String versionKey = key + VERSION_NUMBER_PREFIX + i;
                     super.delete(versionKey).get();
                 }
 
                 // delete the counter
                super.delete(key.concat(VERSION_COUNTER_SUFFIX)).get();
 
             } catch (InterruptedException e) {
                 e.printStackTrace();
             } catch (ExecutionException e) {
                 e.printStackTrace();
             }
         } else {
             return super.delete(key);
         }
         return super.delete(key);
     }
 
 
 	public boolean isAutomaticVersionning() {
 		return automaticVersionning;
 	}
 
 	public void setAutomaticVersionning(boolean automaticVersionning) {
 		this.automaticVersionning = automaticVersionning;
 	}
 }
