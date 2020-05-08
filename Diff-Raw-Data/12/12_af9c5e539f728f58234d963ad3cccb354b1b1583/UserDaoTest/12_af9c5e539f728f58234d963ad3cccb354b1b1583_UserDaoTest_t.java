 package com.nearme;
 
 import static org.junit.Assert.*;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import org.junit.Before;
 import org.junit.Test;
 import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
 
 public class UserDaoTest {
 	
 //	private static Logger logger = Logger.getLogger(UserDaoTest.class);
 	
 	private UserDAO uf = null;
 	private MysqlConnectionPoolDataSource dataSource = null;
 	private List<AddressBookEntry> testBook = null;
 	
 	@Before
 	public void setUp() {
 		uf = new UserDAODummyImpl();
 
 		dataSource = new MysqlConnectionPoolDataSource();
 		dataSource.setUser("nearme");
 		dataSource.setPassword("nearme");
 		dataSource.setUrl("jdbc:mysql://localhost/nearme");
 		
 		/* Reset the database to a known good state before each test */
 
 		try {
 			ArbitrarySQLRunner asr = new ArbitrarySQLRunner(dataSource);
 			asr.runSQL(getClass().getResourceAsStream("/Schema.sql"));
 	
 			uf = new UserDAOImpl(dataSource);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 
 		List<IdentityHash> fredHashes = new ArrayList<IdentityHash>();
 		fredHashes.add(new IdentityHash("fred-hash-1"));
 
 		List<IdentityHash> gingerHashes = new ArrayList<IdentityHash>();
 		gingerHashes.add(new IdentityHash("ginger-hash-1"));
 		gingerHashes.add(new IdentityHash("ginger-hash-2"));
 
 		testBook = new ArrayList<AddressBookEntry>();
 		testBook.add(new AddressBookEntry("fred", fredHashes, AddressBookEntry.PERM_SHOWN));
 		testBook.add(new AddressBookEntry("ginger", gingerHashes, AddressBookEntry.PERM_HIDDEN));
 		
 	}
 
 	@Test
 	public void testReadUser() throws SQLException {
 		Calendar c = Calendar.getInstance();
 		c.set(Calendar.YEAR, 2011);
 		c.set(Calendar.MONTH, 10);
 		c.set(Calendar.DAY_OF_MONTH, 11);
 		c.set(Calendar.HOUR_OF_DAY, 0);
 		c.set(Calendar.MINUTE, 0);
 		c.set(Calendar.SECOND, 0);
 		c.set(Calendar.MILLISECOND, 0);
 
 		User testUser = new User(1, "android-123456", "hash-1234567890", new Position(-123.45, 57.89, c.getTime()));
 
 		assertEquals(testUser, uf.read(1));
 		
 		assertEquals(testUser, uf.readByHash("hash-1234567890"));
 		assertNull(uf.readByHash("non-existent-hash"));
 		assertNull(uf.readByHash("android-123456")); 
 
 		assertEquals(testUser, uf.readByDeviceId("android-123456"));
 		assertNull(uf.readByDeviceId("hash-1234567890"));
 		assertNull(uf.readByDeviceId("non-existent-id"));
 	}
 
 	@Test
 	public void testReadAddressBookNonExistentUser() throws SQLException {
 		List<AddressBookEntry> book = uf.getAddressBook(12345); // non-existent user
 		assertNull(book);
 	}
 
 	@Test
 	public void testReadAddressBookEmpty()  throws SQLException {
 		List<AddressBookEntry> book = uf.getAddressBook(2); // existing user, no book
 		assertNotNull(book);
 		assertEquals(0, book.size());
 	}
 
 	@Test
 	public void testReadAddressBookFull() throws SQLException {
 		User u = uf.read(1);
 		List<AddressBookEntry> book = uf.getAddressBook(1); // existing user, with book
 		assertNotNull(book);
 		assertEquals(3, book.size());
 		
 		IdentityHash hash3 = new IdentityHash(3, "hash-aaaaaaaaaa");
 		IdentityHash hash4 = new IdentityHash(4, "hash-bbbbbbbbbb");
 		IdentityHash hash5 = new IdentityHash(5, "hash-cccccccccc");
 
 		ArrayList<IdentityHash> list3 = new ArrayList<IdentityHash>();
 		list3.add(hash3);
 		
 		ArrayList<IdentityHash> list4 = new ArrayList<IdentityHash>();
 		list4.add(hash4);
 		
 		ArrayList<IdentityHash> list5 = new ArrayList<IdentityHash>();
 		list5.add(hash5);
 		
 		assertEquals(new AddressBookEntry(2, u, "Dick", AddressBookEntry.PERM_HIDDEN, list4), book.get(0));
 		assertEquals(new AddressBookEntry(3, u, "Harry", AddressBookEntry.PERM_HIDDEN, list5), book.get(1));
 		assertEquals(new AddressBookEntry(1, u, "Tom", AddressBookEntry.PERM_SHOWN, list3), book.get(2));
 	}
 
 	@Test
 	public void testAddNewUser() throws SQLException {
 		User u = new User();
 		u.setDeviceId("device-id");
 		u.setMsisdnHash("msisdn-hash");
 		assertEquals(User.NO_ID, u.getId());
 		u = uf.write(u);
 		
 		// check the returned User has been allocated a new ID
 		assert(User.NO_ID!=u.getId());
 		
 		// check we can read the user from the database, and it's the same one
 		
 		User u2 = uf.read(u.getId());
 		assertEquals (u2, u);
 	}
 
 	@Test
 	public void testUpdateHashForExistingUser() throws SQLException {
 		int numUsersBefore = countUsers();
 		String newHash = "updated-hash";
 		User u = uf.read(2);
 		u.setMsisdnHash(newHash);
 		uf.write(u);
 		
 		/* Check we have read the same user out */
 		
 		User u2 = uf.read(2);
 		assertEquals(newHash, u2.getMsisdnHash());
 		assertEquals(u, u2);
 		
 		/* Check there are no new users */
 		
 		int numUsersAfter = countUsers();
 		assertEquals(numUsersAfter, numUsersBefore);
 	}
 
 	@Test
 	public void testUpdatePositionForExistingUser() throws SQLException {
 		int numUsersBefore = countUsers();
 		User u = uf.read(2);
 		u.setLastPosition(new Position(1,1,new Date(1410969600000L)));
 		uf.write(u);
 		
 		/* Check we have read the same user out */
 		
 		User u2 = uf.read(2);
 		assertEquals(u, u2);
 		
 		/* Check there are no new users */
 		
 		int numUsersAfter = countUsers();
 		assertEquals(numUsersAfter, numUsersBefore);
 	}
 
 	@Test
 	public void testSetAddressBookForNonExistentUser() throws SQLException {
 		assertEquals(false, uf.setAddressBook(99999, testBook));
 	}
 
 	@Test
 	public void testSetAddressBookForUserWithNoAddressBook() throws SQLException {
 		assertEquals(0, uf.getAddressBook(2).size());
 		assertEquals(true, uf.setAddressBook(2, testBook));
 
 		List<AddressBookEntry> book = uf.getAddressBook(2);
 		assertEquals(2, book.size());
 		assertEquals("fred", book.get(0).getName());
 		assertEquals("ginger", book.get(1).getName());
 	}
 
 	@Test
 	public void testSetAddressBookForUserWithAnAddressBookChangeNone() throws SQLException {
 		assertEquals(3, uf.getAddressBook(1).size());
 		List<AddressBookEntry> book = uf.getAddressBook(1);
 		assertEquals(true, uf.setAddressBook(1, book));
 		assertEquals(3, uf.getAddressBook(1).size());
 	}
 
 	@Test
 	public void testSetAddressBookForUserWithAnAddressBookChangeOne() throws SQLException {
 		assertEquals(3, uf.getAddressBook(1).size());
 		List<AddressBookEntry> book = uf.getAddressBook(1);
 		
 		book.get(1).setName("changed");
 		
 		assertEquals(true, uf.setAddressBook(1, book));
 		
 		List<AddressBookEntry> bookAgain = uf.getAddressBook(1);
 		assertEquals(3, bookAgain.size());
 		// was Dick, Harry, Tom
 
 		// Now is changed, Dick, Tom
 		assertEquals("changed", bookAgain.get(0).getName());
 		assertEquals("Dick", bookAgain.get(1).getName());
 		assertEquals("Tom", bookAgain.get(2).getName());
 	}
 
 	@Test
 	public void testSetAddressBookForUserWithAnAddressBookChangeAll() throws SQLException {
 		assertEquals(3, uf.getAddressBook(1).size());
 		List<AddressBookEntry> book = uf.getAddressBook(1);
 		
 		book.get(0).setName("changed1");
 		book.get(1).setName("changed2");
 		book.get(2).setName("changed3");
 		
 		assertEquals(true, uf.setAddressBook(1, book));
 		
 		List<AddressBookEntry> bookAgain = uf.getAddressBook(1);
 		assertEquals(3, bookAgain.size());
 		assertEquals("changed1", bookAgain.get(0).getName());
 		assertEquals("changed2", bookAgain.get(1).getName());
 		assertEquals("changed3", bookAgain.get(2).getName());
 	}
 	
 
 	@Test
 	public void testSetAddressBookForUserWithAnAddressBookDeleteOne() throws SQLException {
 		assertEquals(3, uf.getAddressBook(1).size());
 		List<AddressBookEntry> book = uf.getAddressBook(1);
 
 		// remove Harry
 		book.remove(1);
 		
 		assertEquals(true, uf.setAddressBook(1, book));
 		
 		List<AddressBookEntry> bookAgain = uf.getAddressBook(1);
 		assertEquals(2, bookAgain.size());
 		assertEquals("Dick", bookAgain.get(0).getName());
 		assertEquals("Tom", bookAgain.get(1).getName());
 		
 	}
 	
 	
 	@Test
 	public void testSetAddressBookSetsPermissionsOK() throws SQLException {
 		assertEquals(0, uf.getAddressBook(2).size());
 		assertEquals(true, uf.setAddressBook(2, testBook));
 
 		List<AddressBookEntry> book = uf.getAddressBook(2);
 		assertEquals(2, book.size());
 		assertEquals("fred", book.get(0).getName());
 		assertEquals(AddressBookEntry.PERM_SHOWN, book.get(0).getPermission());
 		assertEquals("ginger", book.get(1).getName());
 		assertEquals(AddressBookEntry.PERM_HIDDEN, book.get(1).getPermission());
 
 	}
 	
 	@Test
 	public void testGetPermissions() throws SQLException {
 		User u = uf.read(1);
 		List<IdentityHash> perms = uf.getPermissions(u);
 		assertEquals(1, perms.size());
 	}
 
 	@Test
 	public void testSetPermissionsUnknownUser() throws SQLException {
 		User u = new User();
 		u.setId(4);
 		assert(!uf.setPermissions(u, new String[]{}));
 	}
 
 	@Test
 	public void testSetPermissionsExistingUserPermissionAdded() throws SQLException {
 		User u = uf.read(1);
 		// testGetPermissions() has already tested that user 1 has 1 permission in their address book
 
 		String[] perms = {"hash-aaaaaaaaaa","hash-bbbbbbbbbb"};
 		assertTrue(uf.setPermissions(u, perms));
 		
 		// dick and tom should be marked with permissions, harry not
 		List<AddressBookEntry> book = uf.getAddressBook(u.getId());
 		assertEquals(AddressBookEntry.PERM_SHOWN, book.get(0).getPermission());		// Dick
 		assertEquals(AddressBookEntry.PERM_HIDDEN, book.get(1).getPermission());	// Harry
 		assertEquals(AddressBookEntry.PERM_SHOWN, book.get(2).getPermission());		// Tom
 
		// getpermissions should return their hashes, but in any order
 		List<IdentityHash> hashes = uf.getPermissions(u);
 		assertEquals(2, hashes.size());
		if (hashes.get(0).getHash().equals("hash-bbbbbbbbbb")) {
			assertEquals(perms[1], hashes.get(0).getHash());
			assertEquals(perms[0], hashes.get(1).getHash());
		} else {
			assertEquals(perms[1], hashes.get(1).getHash());
			assertEquals(perms[0], hashes.get(0).getHash());
		}
 	}
 
 	@Test
 	public void testSetPermissionsExistingUserPermissionRemoved() throws SQLException {
 		User u = uf.read(1);
 		// testGetPermissions() has already tested that user 1 has 1 permission in their address book
 
 		assertTrue(uf.setPermissions(u, new String[]{})); // should remove all permissions
 
 		// no-one should have any permissions now
 		List<AddressBookEntry> book = uf.getAddressBook(u.getId());
 		assertEquals(AddressBookEntry.PERM_HIDDEN, book.get(0).getPermission());		// Dick
 		assertEquals(AddressBookEntry.PERM_HIDDEN, book.get(1).getPermission());	// Harry
 		assertEquals(AddressBookEntry.PERM_HIDDEN, book.get(2).getPermission());		// Tom
 
 		// getpermissions should return no hashes
 		List<IdentityHash> hashes = uf.getPermissions(u);
 		assertEquals(0, hashes.size());
 	}
 
 	@Test
 	public void testSetPermissionsExistingUserUnlinkedHash() throws SQLException {
 		User u = uf.read(1);
 		// testGetPermissions() has already tested that user 1 has 1 permission in their address book
 		
 		assertFalse(uf.setPermissions(u, new String[]{"hash-1234567890"}));
 	}
 
 	@Test
 	public void testDeleteUserOK() throws SQLException {
 		int beforeUsers = countUsers();
 		int beforeAb = countAddressBookEntries();
 		User u = uf.read(1);
 		uf.deleteUser(u);
 		int afterUsers = countUsers();
 		int afterAb = countAddressBookEntries();
 		assertEquals(beforeUsers-1, afterUsers);
 		assertEquals(beforeAb-3, afterAb);
 	}
 
 	@Test
 	public void testDeleteUserDoesntExist() throws SQLException {
 		int before = countUsers();
 		int beforeAb = countAddressBookEntries();
 		User u = new User();
 		u.setId(999);
 		uf.deleteUser(u);
 		int after = countUsers();
 		int afterAb = countAddressBookEntries();
 		assertEquals(before, after);
 		assertEquals(beforeAb, afterAb);
 	}
 	
 	@Test
 	public void testGetNearestUsersNoUsersNearby() throws SQLException {
 		// look at co-ordinate -123.45, 67.89 - where user ID 2 is, with no users near
 		User u = uf.read(2);
 		List<Poi> ret = uf.getNearestUsers(u, 100);
 		assertNotNull(ret);
 		assertEquals(0, ret.size());
 	}
 
 	@Test
 	public void testGetNearestUsersOneUserNearbyWithoutPermission() throws SQLException {
 		// move user 1 to -123.45, 57.89 - where user 3 is currently sat
 		User u1 = uf.read(1);
 		u1.setLastPosition(new Position(-123.45, 67.89, new Date()));
 		uf.write(u1);
 		
 		// withdraw permission from user 1 to share his location with user 3
 		
 		ArbitrarySQLRunner asr = new ArbitrarySQLRunner(dataSource);
 		asr.runSQL("UPDATE addressBook SET permission = 0 WHERE id = 1");
 
 		User u = uf.read(3);
 
 		// user 1 should *not* turn up in the results now
 		
 		List<Poi> ret = uf.getNearestUsers(u, 100);
 		assertNotNull(ret);
 		assertEquals(0, ret.size());
 	}
 
 	@Test
 	public void testGetNearestUsersOneUserNearbyWithPermission() throws SQLException {
 		// move user 1 to -123.45, 67.89 - where user 3 is currently sat
 		User u1 = uf.read(1);
 		u1.setLastPosition(new Position(-123.45, 67.89, new Date()));
 		uf.write(u1);
 
 		
 		User u = uf.read(3);
 
 		// user 1 should turn up in the results now, within 1m of user 3
 		
 		List<Poi> ret = uf.getNearestUsers(u, 1);
 		assertNotNull(ret);
 		assertEquals(1, ret.size());
 	}
 	
 	@Test
 	public void testGetNearestUsersOneUserOutOfRangeWithPermission() throws SQLException {
 		// move user 1 to -100.45, 57.89 - out of range of user 3
 		User u1 = uf.read(1);
 		u1.setLastPosition(new Position(-100.45, 57.89, new Date()));
 		uf.write(u1);
 
 		
 		User u = uf.read(3);
 
 		// user 1 should no longer turn up in the results now
 		
 		List<Poi> ret = uf.getNearestUsers(u, 100);
 		assertNotNull(ret);
 		assertEquals(0, ret.size());
 	}
 	
 
 	/**
 	 * Simple helper methods, just counts how many users or address book entries there are in the database
 	 * @return
 	 * @throws SQLException
 	 */
 	private int countUsers() throws SQLException {
 		return countQuery("SELECT COUNT(id) FROM user");
 	}
 	
 	private int countAddressBookEntries() throws SQLException {
 		return countQuery("SELECT COUNT(id) FROM addressBook");
 	}
 
 	private int countQuery(String query) throws SQLException {
 		ArbitrarySQLRunner asr = new ArbitrarySQLRunner(dataSource);
 		return asr.runQuery(query);
 	}
 }
