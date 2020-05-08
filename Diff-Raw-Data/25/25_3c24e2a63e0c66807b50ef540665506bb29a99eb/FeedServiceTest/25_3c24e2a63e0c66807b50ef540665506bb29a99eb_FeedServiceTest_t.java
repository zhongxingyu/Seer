 package ar.edu.utn.tacs.group5.service;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import ar.edu.utn.tacs.group5.model.Feed;
 import ar.edu.utn.tacs.group5.model.Item;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 public class FeedServiceTest extends AbstractServiceTest {
 
     private FeedService service = new FeedService();
 
     @Test
     public void testInsert() throws Exception {
         Feed feed = new Feed();
         service.insert(feed);
         assertThat(feed.getKey(), is(notNullValue()));
     }
 
     @Test
     public void testHasDefaultFeed() throws Exception {
         long userId = 123456789L;
         assertFalse(service.hasDefaultFeed(userId));
         Feed feed = new Feed();
         feed.setUserId(userId);
         service.insert(feed);
         assertTrue(service.hasDefaultFeed(userId));
     }
 
     @Test
     public void testAddItem() throws Exception {
         Feed feed = new Feed();
         service.insert(feed);
         service.addItem(feed, new Item());
         feed = service.getByKey(feed.getKey());
         assertThat(feed.getItems().size(), is(1));
     }
 
     @Test(expected = NullPointerException.class)
     public void testAddItemWithNullItem() throws Exception {
         service.addItem(new Feed(), null);
     }
 
     @Test(expected = NullPointerException.class)
     public void testAddItemWithNullFeed() throws Exception {
         service.addItem(null, new Item());
     }
 
     @Test(expected = NullPointerException.class)
     public void testAddItemWithNullFeedAndItem() throws Exception {
         service.addItem(null, null);
     }
 
     @Test
     public void testGetByKey() throws Exception {
         Feed feed = new Feed();
         feed.setTitle("foo");
         feed.setDescription("Foo description");
         service.insert(feed);
         Feed feedByKey = service.getByKey(feed.getKey());
         assertNotNull(feedByKey);
         assertThat(feedByKey.getTitle(), is("foo"));
         assertThat(feedByKey.getDescription(), is("Foo description"));
     }
 
     @Test(expected = NullPointerException.class)
     public void testGetByKeyWithNullKey() throws Exception {
         Key key = null;
         service.getByKey(key);
     }
 
     @Test(expected = NullPointerException.class)
     public void testGetByKeyWithNullStringKey() throws Exception {
         String key = null;
         service.getByKey(key);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testGetByKeyWithInvalidStringKey() throws Exception {
         service.getByKey("xxxx92xxxx42");
     }
 
     @Test
     public void testGetDefaultFeed() throws Exception {
         long userId = 123456789L;
         service.insert(userId);
         Feed feed = service.getDefaultFeed(userId);
         assertNotNull(feed);
        assertThat(feed.getTitle(), is(FeedService.DEFAULT_FEED_TITLE));
    }

    @Test
    public void testGetDefaultFeedWhenHavingMultipleFeeds() throws Exception {
        long userId = 123456789L;
        service.insert(userId);
        Feed feed1 = new Feed();
        feed1.setUserId(userId);
        service.insert(feed1);
        service.insert(feed1);
        service.insert(feed1);
        Feed feed = service.getDefaultFeed(userId);
        assertNotNull(feed);
        assertThat(feed.getTitle(), is(FeedService.DEFAULT_FEED_TITLE));
     }
 
     @Test
     public void testGetDefaultFeedWithInvalidId() throws Exception {
         Feed feed = service.getDefaultFeed(9999999L);
         assertNull(feed);
     }
 
     @Test
     public void testGetByKeyString() throws Exception {
         long userId = 123456789L;
         service.insert(userId);
         Feed feed = service.getDefaultFeed(userId);
         assertNotNull(feed);
         Feed feedByKey = service.getByKey(KeyFactory.keyToString(feed.getKey()));
         assertNotNull(feedByKey);
         assertEquals(feed, feedByKey);
     }
 
 }
