 package dbapi.test.system;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import dbapi.api.DBAPI;
 import dbapi.api.DBConfig;
 import dbapi.api.DBQuery;
 import dbapi.api.DBSession;
 import dbapi.api.DBSessionFactory;
 import dbapi.test.model.TestUser;
 import dbapi.test.model.TestUserFriend;
 import dbapi.test.model.TestUserWithEmbeddedCollection;
 
 /**
  * 
  * @author alex
  * 
  *         Nov 5, 2012
  */
 public class MongoCRUDTest
 {
     private static final Logger log = Logger.getLogger(CassandraCRUDTest.class);
 
     private static DBSessionFactory factory;
     private DBSession em;
 
     private final List<TestUserWithEmbeddedCollection> cleanUpQueue = new ArrayList<TestUserWithEmbeddedCollection>();
 
     @BeforeClass
     public static void setUpGlobal()
     {
         BasicConfigurator.configure();
 
         final DBConfig cfg = new DBConfig();
 
         cfg.selectMongoDB();
         cfg.setHost("localhost");
         cfg.setDatabase("strimko-test");
 
         final Set<Class<?>> entities = new HashSet<Class<?>>();
         entities.add(TestUser.class);
         entities.add(TestUserFriend.class);
         entities.add(TestUserWithEmbeddedCollection.class);
 
         factory = DBAPI.getFactory(cfg, entities);
         log.debug("Created factory");
     }
 
     @AfterClass
     public static void tearDownGlobal()
     {
         log.debug("Terminating factory");
         if (factory != null)
         {
             factory.close();
             log.debug("Factory terminated.");
         }
     }
 
     @Before
     public void setUp()
     {
         em = factory.createSession();
         log.debug("Entity Manager created");
     }
 
     @After
     public void tearDown()
     {
         for (final TestUserWithEmbeddedCollection user : cleanUpQueue)
         {
             try
             {
                 em.delete(user);
             }
             catch (final Throwable th)
             {
                 log.error("Failed to clean up user id:" + user.getId(), th);
             }
         }
     }
 
 
 
     @Test
     public void testCreateDeleteUserWithCollection()
     {
         log.info("CRUD scenario started");
 
         final TestUserWithEmbeddedCollection user = new TestUserWithEmbeddedCollection();
         user.setUsername("john");
         user.setPassword("1234");
 
         final TestUserFriend userFriend = new TestUserFriend();
         userFriend.setId("1234");
         userFriend.setUsername("Fake");
 
         user.getFriends().add(userFriend);
 
         em.save(user);
 
         assertNotNull("The ID is missed in the newly created object", user.getId());
 
         cleanUpQueue.add(user);
 
         TestUserWithEmbeddedCollection userCopy = em.get(TestUserWithEmbeddedCollection.class, user.getId());
 
         assertNotNull("Failed to lookup newly created object ", userCopy);
         assertEquals("Saved&Read Objects do not equal", user, userCopy);
         assertTrue("Failed to load embedded collection ", null != userCopy.getFriends() && userCopy.getFriends().size() == 1);
 
         final DBQuery<TestUserWithEmbeddedCollection> query = em.queryOn(TestUserWithEmbeddedCollection.class);
         query.getModel().setUsername("john");
 
 
         final List<TestUserWithEmbeddedCollection> queryRes = em.query(query);
         assertNotNull("Failed search for a newly created object ", queryRes);
         assertTrue("Failed to find newly created object", !queryRes.isEmpty());
         assertTrue("Search returned more then one results", queryRes.size() == 1);
         assertEquals("Saved&Read Objects do not equal", user, queryRes.get(0));
 
        user.setPassword("new");
        em.save(user);

        userCopy = em.get(TestUserWithEmbeddedCollection.class, user.getId());

        assertEquals("Failed to update the password field", user.getPassword(), userCopy.getPassword());
 
         em.delete(user);
 
         cleanUpQueue.remove(user);
 
         userCopy = em.get(TestUserWithEmbeddedCollection.class, user.getId());
 
         assertNull("Found previously deleted object", userCopy);
 
         log.info("CRUD scenario finished.");
     }
 
 }
