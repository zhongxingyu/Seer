 package lobstre.chlist;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import lobstre.chlist.LockFreeLinkedList.NextLink;
 import lobstre.chlist.LockFreeLinkedList.Node;
 
 import org.junit.Test;
 
 public class TestLockFreeLinkedList {
 
 	@Test
 	public void testInsert () {
 		final LockFreeLinkedList ll = new LockFreeLinkedList ();
 		testInsert (ll, -1, "-1");
 		testInsert (ll, 0, "0");
 		testInsert (ll, 1, "1");
 		
 		for (int i = 5; i < 25; i++) {
 			testInsert (ll, i, Integer.toString (i));
 		}
 	}
 
 	private void testInsert (final LockFreeLinkedList ll, final int expectedKey, final String expectedValue) {
 		final Node inserted = ll.insert (expectedKey, expectedValue);
 		checkNode (inserted, expectedKey, expectedValue);
 		
 		final Node search = ll.search (expectedKey);
 		checkNode (search, expectedKey, expectedValue);
 	}
 
 	private void checkNode (final Node node, final int expectedKey, final String expectedValue) {
 		assertNotNull (node);
 		assertNull (node.backlink);
 		assertEquals (expectedKey, node.key);
 		assertEquals (expectedValue, node.value);
 		final NextLink next = node.next ();
 		assertFalse (next.flag);
 		assertFalse (next.mark);
 		assertNotNull (next.node);
 	}
 }
