 package cz.muni.fi.publishsubscribe.matchingtree.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import cz.muni.fi.publishsubscribe.matchingtree.Event;
 import cz.muni.fi.publishsubscribe.matchingtree.MatchingTree;
 import cz.muni.fi.publishsubscribe.matchingtree.Subscription;
 
 public class MachingTreeTestCase {
 
 	private MatchingTree tree;
 
 	private Subscription apache1000Subscription;
 	private Subscription apacheSubscription1;
 	private Subscription apacheSubscription2;
 	private Subscription processId1000Subscription;
 	private Subscription processId2000Subscription;
 	private Subscription everyEventSubscription;
	private Subscription postgreSql1000Subscription;
 	private Subscription postgreSqlSubscription;
 
 	@Before
 	public void prepareTree() {
 		tree = new MatchingTree();
 
 		// Apache, 1000
 		apache1000Subscription = new Subscription();
 		apache1000Subscription.setApplication("Apache");
 		apache1000Subscription.setProcessId(1000L);
 		tree.preprocess(apache1000Subscription);
 
 		// Apache - two subscriptions
 		apacheSubscription1 = new Subscription();
 		apacheSubscription1.setApplication("Apache");
 		tree.preprocess(apacheSubscription1);
 
 		apacheSubscription2 = new Subscription();
 		apacheSubscription2.setApplication("Apache");
 		tree.preprocess(apacheSubscription2);
 
 		// 1000
 		processId1000Subscription = new Subscription();
 		processId1000Subscription.setProcessId(1000L);
 		tree.preprocess(processId1000Subscription);
 
 		// 2000
 		processId2000Subscription = new Subscription();
 		processId2000Subscription.setProcessId(2000L);
 		tree.preprocess(processId2000Subscription);
 
 		// *
 		everyEventSubscription = new Subscription();
 		tree.preprocess(everyEventSubscription);
 
 		// PostgreSQL, 3000
		postgreSql1000Subscription = new Subscription();
		postgreSql1000Subscription.setApplication("PostgreSQL");
		postgreSql1000Subscription.setProcessId(3000L);
		tree.preprocess(postgreSql1000Subscription);
 
 		// PostgreSQL
 		postgreSqlSubscription = new Subscription();
 		postgreSqlSubscription.setApplication("PostgreSQL");
 		tree.preprocess(postgreSqlSubscription);
 	}
 
 	@Test
 	public void testOneMatchingSubscriber() {
 		Event event = new Event(0L, "Foo", 1234L, 1);
 		List<Subscription> subscriptions = tree.match(event);
 		assertEquals(1, subscriptions.size());
 		assertEquals(everyEventSubscription, subscriptions.get(0));
 	}
 
 	@Test
 	public void testApacheEvent() {
 		Event event = new Event(0L, "Apache", 1234L, 1);
 		List<Subscription> subscriptions = tree.match(event);
 		assertEquals(3, subscriptions.size());
 		assertTrue(subscriptions.contains(everyEventSubscription));
 		assertTrue(subscriptions.contains(apacheSubscription1));
 		assertTrue(subscriptions.contains(apacheSubscription2));
 	}
 	
 	@Test
 	public void testApache1000Event() {
 		Event event = new Event(0L, "Apache", 1000L, 1);
 		List<Subscription> subscriptions = tree.match(event);
 		assertEquals(5, subscriptions.size());
 		assertTrue(subscriptions.contains(everyEventSubscription));
 		assertTrue(subscriptions.contains(apache1000Subscription));
 		assertTrue(subscriptions.contains(apacheSubscription1));
 		assertTrue(subscriptions.contains(apacheSubscription2));
 		assertTrue(subscriptions.contains(processId1000Subscription));
 	}
 	
 	@Test
 	public void testApache2000Event() {
 		Event event = new Event(0L, "Apache", 2000L, 1);
 		List<Subscription> subscriptions = tree.match(event);
 		assertEquals(4, subscriptions.size());
 		assertTrue(subscriptions.contains(everyEventSubscription));
 		assertTrue(subscriptions.contains(apacheSubscription1));
 		assertTrue(subscriptions.contains(apacheSubscription2));
 		assertTrue(subscriptions.contains(processId2000Subscription));
 	}
 	
 	@Test
 	public void testPostgreSqlEvent() {
 		Event event = new Event(0L, "PostgreSQL", 2000L, 1);
 		List<Subscription> subscriptions = tree.match(event);
 		assertEquals(3, subscriptions.size());
 		assertTrue(subscriptions.contains(everyEventSubscription));
 		assertTrue(subscriptions.contains(processId2000Subscription));
 		assertTrue(subscriptions.contains(postgreSqlSubscription));
 	}
 	
 	@Test
	public void testProcessId100Event() {
 		Event event = new Event(0L, "Foo", 1000L, 1);
 		List<Subscription> subscriptions = tree.match(event);
 		assertEquals(2, subscriptions.size());
 		assertTrue(subscriptions.contains(everyEventSubscription));
 		assertTrue(subscriptions.contains(processId1000Subscription));
 	}
 }
