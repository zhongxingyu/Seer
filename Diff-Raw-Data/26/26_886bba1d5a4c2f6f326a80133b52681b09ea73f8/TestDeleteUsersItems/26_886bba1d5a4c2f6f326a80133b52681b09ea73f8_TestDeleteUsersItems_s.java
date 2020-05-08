 package com.motm.test;
 
 
import java.util.GregorianCalendar;
 import com.motm.models.Account;
 import com.motm.models.FileAccountManager;
 import com.motm.models.FileItemManager;
 import com.motm.models.Item;
 import com.motm.models.interfaces.AccountManager;
 import com.motm.models.interfaces.ItemManager;
 import junit.framework.TestCase;
 
 /**
  * @author Will Holton
  **/
 
 public class TestDeleteUsersItems extends TestCase {
 	private ItemManager itemManager;
	private Account account;
 	private AccountManager accountManager;
 
 	@Override
 	protected void setUp() throws Exception
 	{
 		super.setUp();
 		
 		// ran before each test
 		accountManager = new FileAccountManager();
		account = new Account(1, "", "", "", "", Account.State.UNLOCKED, 0);
 		itemManager = new FileItemManager();
 	}
 
 	@Override
 	protected void tearDown() throws Exception
 	{
 		super.tearDown();
 		
 		// ran after each test
 		accountManager = null;
		account = null;
 		itemManager = null;
 	}
 
 	/*
 	 *  Test if items created by the user are deleted
 	 */
 	public void testDeleteUsersItems()
 	{
 		Integer item1Id = 0;
 		Integer item2Id = 0;
 		// creates items owned by account.
 		try {
 			item1Id = itemManager.createItem(1, "testName","testLocation", "testReward", Item.Type.FOUND, "testCategory", "testDescription");
 			item2Id = itemManager.createItem(1, "testName","testLocation", "testReward", Item.Type.FOUND, "testCategory", "testDescription");
 		} catch(Exception e) {
 			assertTrue(false);
 		}
 		// delete the account's items.
 		itemManager.deleteUsersItems(1);
 		// passes if the items created are now deleted.
 		assertNull(itemManager.getItem(item1Id));
 		assertNull(itemManager.getItem(item2Id));
 	}
 	
 	/*
 	 *  Test if items not created by the user are deleted.
 	 */
 	public void testDeleteOthersItems()
 	{
 		Integer accountsItemId = 0;
 		Integer notAccountsItemId = 0;
 		// creates item owned by account.
 		try {
 			accountsItemId = itemManager.createItem(1, "testName","testLocation", "testReward", Item.Type.FOUND, "testCategory", "testDescription");
 		} catch(Exception e) {
 			assertTrue(false);
 		}		
 		// creates items not owned by account
 		try {
 			notAccountsItemId = itemManager.createItem(2, "testName","testLocation", "testReward", Item.Type.FOUND, "testCategory", "testDescription");
 		} catch(Exception e) {
 			assertTrue(false);
 		}		
 		Item notAccountsItem = itemManager.getItem(notAccountsItemId);
 		// delete the account's items.
 		itemManager.deleteUsersItems(1);
 		// passes if the item with id "accountsItemId" is deleted and "notAccountsItemId" still exists.
 		assertNull(itemManager.getItem(accountsItemId));
 		assertSame(itemManager.getItem(notAccountsItemId), notAccountsItem);
 	}
 	
 	/*
 	 *  Test if deleteUsersItems(int ownerId) works correctly if the user has no items.
 	 */
	public void testDeleteUsersNonexistantItems()
 	{
 		Integer itemId = 0;
 		// creates item not owned by account
 		try {
 			itemId = itemManager.createItem(2, "testName","testLocation", "testReward", Item.Type.FOUND, "testCategory", "testDescription");
 		} catch(Exception e) {
 			assertTrue(false);
 		}	
 		// gets the size of the list of all items before deleting.
 		int itemListSize1 = itemManager.getAllItems().size();
 		// delete the account's items.
 		itemManager.deleteUsersItems(1);
 		// size of the list after deleting the users items.
 		int itemListSize2 = itemManager.getAllItems().size();
 		// passes if the sizes are equal (unchanged by the call to deleteUsersItems)
 		assertEquals(itemListSize1, itemListSize2);
 	}
 	
 	/*
 	 *  Test if deleteUsersItems(int ownerId) works correctly if the user doesn't exist.
 	 */
	public void testDeleteNonExistantUsersItems()
 	{
		Integer itemId = 0;
 		// creates item not owned by account
 		try {
			itemId = itemManager.createItem(2, "testName","testLocation", "testReward", Item.Type.FOUND, "testCategory", "testDescription");
 		} catch(Exception e) {
 			assertTrue(false);
 		}	
 		// gets the size of the list of all items before deleting.
 		int itemListSize1 = itemManager.getAllItems().size();
 		// delete the original account.
 		accountManager.deleteAccount(1);
 		// delete the account's items.
 		itemManager.deleteUsersItems(1);
 		// size of the list after deleting the users items.
 		int itemListSize2 = itemManager.getAllItems().size();
 		// passes if the sizes are equal (unchanged by the call to deleteUsersItems)
 		assertEquals(itemListSize1, itemListSize2);
 	}
 
 }
