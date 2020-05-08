 package throwaway;
 
 import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 
 
 public class JavaDataStoreTest {
 
     private final LocalServiceTestHelper helper =
         new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 
     @Before
     public void setUp() {
         helper.setUp();
     }
 
     @After
     public void tearDown() {
         helper.tearDown();
     }
     
     @Test
     public void linkedButNotRelatedEntities(){
     	DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
     	Entity house = new Entity("house");
     	ds.put(house);
     	//user exists  before houses do
     	Entity user = new Entity("user");
     	ds.put(user);
     	Entity uhr = new Entity("userHouseRelation",user.getKey());
         uhr.setProperty("house", house.getKey());        
         ds.put(uhr);
         Entity getUhr = ds.prepare(new Query("userHouseRelation",user.getKey())).asIterable().iterator().next();
     	assertEquals(1,ds.prepare(new Query("house",(Key) getUhr.getProperty("house"))).countEntities());
     	
     	Entity account = new Entity("account", uhr.getKey());
     	ds.put(account);
     }
     
     @Test
     public void oneToMany(){
     	DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
     	Entity parent = new Entity("maclau");
     	Key put = ds.put(parent);
     	final Entity entity = new Entity("yam", put);
     	final Entity entity2 = new Entity("yam", put);
     	ds.put(new ArrayList(){{add(entity);add(entity2);}});
     	
 		assertEquals(2,ds.prepare(new Query("yam").setAncestor(put)).countEntities());
 		
     }
 
     @Test
     public void testInsertParentAndThenChild() {
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
 		assertEquals(0, ds.prepare(new Query("yam")).countEntities(withLimit(10)));
		Entity parent = new Entity("maclau");
		Key put = ds.put(parent);
 		//parent must have been persisted first
		Entity entity = new Entity("yam", put);
 		entity.setProperty("varent", "b");
 		ds.put(entity);
		assertEquals(1, ds.prepare(new Query("yam")).countEntities(withLimit(10)));
     }
     
     public void testUser(){
     	UserService userService = com.google.appengine.api.users.UserServiceFactory.getUserService();
     	//userService.
     }
 
 
 }
