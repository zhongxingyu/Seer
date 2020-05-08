 package ch.unibe.ese.shopnote.core;
 
 import android.app.Activity;
 import android.test.AndroidTestCase;
 import ch.unibe.ese.shopnote.core.FriendsManager;
 import ch.unibe.ese.shopnote.core.sqlite.SQLitePersistenceManager;
 import ch.unibe.ese.shopnote.share.SyncManager;
 
 public class FriendsManagerTest extends AndroidTestCase {
 	
 	private FriendsManager manager;
 
 	public void setUp() throws Exception {
 		getContext().deleteDatabase("shoppinglist.db");
 		manager = new FriendsManager(new SQLitePersistenceManager(getContext()), new SyncManager(), null);
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		getContext().deleteDatabase("shoppinglist.db");
 	}
 	
 	public void testAddFriend() {
 		assertTrue(manager.getFriendsList().isEmpty());
 		Friend friend1 = new Friend("12345678", "friend1");
 		manager.addFriend(friend1);
 		assertEquals(1, manager.getFriendsList().size());
 		assertEquals(friend1, manager.getFriendsList().get(0));
 	}
 	
 	public void testRemoveFriend() {
 		assertTrue(manager.getFriendsList().isEmpty());
 		Friend friend1 = new Friend("12345678", "friend1");
 		manager.addFriend(friend1);
 		assertEquals(1, manager.getFriendsList().size());
 		
 		manager.removeFriend(friend1);
 		assertTrue(manager.getFriendsList().isEmpty());
 	}
 	
 	public void testUpdateFriend() {
 		assertTrue(manager.getFriendsList().isEmpty());
 		Friend friend1 = new Friend("12345678", "friend1");
 		manager.addFriend(friend1);
 		assertEquals(1, manager.getFriendsList().size());
 		assertEquals("friend1", manager.getFriendsList().get(0).getName());
 		
 		friend1.setName("friend2");
 		manager.update(friend1);
 		assertEquals(1, manager.getFriendsList().size());
 		assertEquals("friend2", manager.getFriendsList().get(0).getName());
 	}
 	
 	public void testGetFriendFromNr() {
 		assertTrue(manager.getFriendsList().isEmpty());
 		Friend friend1 = new Friend("12345678", "friend1");
 		manager.addFriend(friend1);
 		
 		assertEquals(friend1, manager.getFriend(friend1.getId()));
 	}
 }
