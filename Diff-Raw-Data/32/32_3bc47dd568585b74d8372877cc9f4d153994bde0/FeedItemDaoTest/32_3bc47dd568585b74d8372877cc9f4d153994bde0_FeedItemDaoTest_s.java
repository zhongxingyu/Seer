 package mdettlaff.cloudreader.dao;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceException;
 
 import mdettlaff.cloudreader.domain.Feed;
 import mdettlaff.cloudreader.domain.FeedItem;
 import mdettlaff.cloudreader.test.AbstractPersistenceTest;
 
 import org.hibernate.exception.ConstraintViolationException;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 
 public class FeedItemDaoTest extends AbstractPersistenceTest {
 
 	@PersistenceContext
 	private EntityManager em;
 
 	@Autowired
 	private FeedItemDao dao;
 
 	@Test
 	@Transactional
 	public void testFindFeeds() {
 		// exercise
 		List<Feed> results = dao.findFeeds();
 		// verify
 		assertEquals(59, results.size());
 		Feed feed1 = results.get(results.size() - 2);
 		assertEquals("url1", feed1.getUrl());
 		assertEquals("My feed 1", feed1.getTitle());
 		assertFalse(feed1.getItems().isEmpty());
 		Feed feed2 = results.get(results.size() - 1);
 		assertEquals("url2", feed2.getUrl());
 	}
 
 	@Test
 	@Transactional
 	public void testFind() {
 		// exercise
 		List<FeedItem> results = dao.find(false, 3, Arrays.asList("item-0002", "item-0004"));
 		// verify
 		assertEquals(3, results.size());
 		FeedItem result1 = results.get(0);
 		assertEquals("item-0001", result1.getGuid());
 		assertEquals(false, result1.isRead());
 		assertEquals("My item 1", result1.getTitle());
 		assertEquals("url1", result1.getFeed().getUrl());
 		assertEquals("My feed 1", result1.getFeed().getTitle());
 		assertEquals("item-0003", results.get(1).getGuid());
 		assertEquals("item-0005", results.get(2).getGuid());
 	}
 
 	@Test
 	@Transactional
 	public void testFind_EmptyList() {
 		// prepare data
 		List<String> guidsToExclude = new ArrayList<>();
 		// exercise
 		List<FeedItem> results = dao.find(true, 1, guidsToExclude);
 		// verify
 		assertEquals(1, results.size());
 		FeedItem result = results.get(0);
 		assertEquals("item-0007", result.getGuid());
 		assertEquals(true, result.isRead());
 		assertEquals("My item 7", result.getTitle());
 		assertEquals("url2", result.getFeed().getUrl());
 		assertEquals("My feed 2", result.getFeed().getTitle());
 	}
 
 	@Test
 	@Transactional
 	public void testCount() {
 		// exercise
 		long result = dao.count(false);
 		// verify
 		assertEquals(6, result);
 	}
 
 	@Test
 	@Transactional
 	public void testSaveFeed_Create() {
 		// prepare data
 		Feed feed = new Feed("savedurl");
 		feed.setTitle("My saved feed");
 		List<FeedItem> items = new ArrayList<>();
 		items.add(prepareItem("item-a001", "My saved item 1", feed));
 		items.add(prepareItem("item-a002", "My saved item 2", feed));
 		feed.setItems(items);
 		// exercise
 		long result = dao.saveFeed(feed);
 		// verify
 		assertEquals(2, result);
 		Feed newFeed = em.find(Feed.class, "savedurl");
 		assertNotNull("feed was not saved successfully", newFeed);
 		assertEquals("savedurl", newFeed.getUrl());
 		assertEquals("My saved feed", newFeed.getTitle());
 		List<FeedItem> newItems = newFeed.getItems();
 		assertEquals(2, newItems.size());
 		assertSame(newFeed, newItems.get(0).getFeed());
 		assertEquals("item-a001", newItems.get(0).getGuid());
 		assertEquals("My saved item 1", newItems.get(0).getTitle());
 		assertSame(newFeed, newItems.get(1).getFeed());
 		assertEquals("item-a002", newItems.get(1).getGuid());
 	}
 
 	@Test
 	@Transactional
 	public void testSaveFeed_Update() {
 		// prepare data
 		Feed feed = new Feed("url2");
 		feed.setTitle("My updated feed");
 		feed.setLink("My updated link");
 		List<FeedItem> items = new ArrayList<>();
 		items.add(prepareItem("item-b001", "My added item 1", feed));
 		items.add(prepareItem("item-0007", "My non-updated item", feed));
 		feed.setItems(items);
 		// exercise
 		long result = dao.saveFeed(feed);
 		// verify
 		assertEquals(1, result);
 		Feed updatedFeed = em.find(Feed.class, "url2");
 		assertEquals("url2", updatedFeed.getUrl());
 		assertEquals("My updated feed", updatedFeed.getTitle());
 		assertEquals("My updated link", updatedFeed.getLink());
 		List<FeedItem> newItems = updatedFeed.getItems();
 		assertEquals(6, newItems.size());
 		assertSame(updatedFeed, newItems.get(5).getFeed());
 		assertEquals("item-b001", newItems.get(5).getGuid());
 		assertEquals("My added item 1", newItems.get(5).getTitle());
 		assertSame(updatedFeed, newItems.get(4).getFeed());
 		assertEquals("item-0007", newItems.get(4).getGuid());
 		assertEquals(true, newItems.get(4).isRead());
 	}
 
 	@Test
 	@Transactional
 	public void testUpdateRead() {
 		// exercise
 		dao.updateRead("item-0001", true);
 		// verify
 		FeedItem result = em.find(FeedItem.class, "item-0001");
 		assertEquals(true, result.isRead());
 	}
 
 	private FeedItem prepareItem(String guid, String title, Feed feed) {
 		FeedItem item = new FeedItem();
 		item.setGuid(guid);
 		item.setTitle(title);
 		item.setFeed(feed);
 		item.setDownloadDate(new Date());
 		return item;
 	}
 
 	@Test
 	@Transactional
 	public void shouldThrowExceptionForDuplicateFeedUrl() {
 		// prepare data
 		Feed feed = new Feed("url1");
 		// exercise
 		try {
 			em.persist(feed);
 			em.flush();
 			fail();
 		} catch (PersistenceException e) {
 			assertTrue(e.getCause() instanceof ConstraintViolationException);
 		}
 	}
 }
