 package edu.rosehulman.grocerydroid.test;
 
 import java.util.ArrayList;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.test.ActivityInstrumentationTestCase2;
 import edu.rosehulman.grocerydroid.MainActivity;
 import edu.rosehulman.grocerydroid.MyApplication;
 import edu.rosehulman.grocerydroid.db.DatabaseHelper;
 import edu.rosehulman.grocerydroid.db.ItemDataAdapter;
 import edu.rosehulman.grocerydroid.model.Item;
 import edu.rosehulman.grocerydroid.model.ItemUnitLabel;
 
 /**
  * Tests Item and ItemDataAdapter.
  * 
  * @author Matthew Boutell. Created Mar 26, 2012.
  */
 public class ItemTest extends ActivityInstrumentationTestCase2<MainActivity> {
 
 	private MainActivity mActivity;
 	private Item bananas;
 	private int idToDelete = 3;
 	private Item oranges;
 	private Item beef;
 
 	private ItemDataAdapter ida;
 	private DatabaseHelper dbHelper;
 	private static float EPSILON = 0.0000001f;
 
 	/**
 	 * Calls another constructor with the given hardcoded info.
 	 * 
 	 * @param activityClass
 	 */
 	public ItemTest() {
 		// super("edu.rosehulman.grocerydroid", MainActivity.class);
 		super(MainActivity.class);
 	}
 
 	/**
 	 * Purges the database
 	 */
 	public void purgeDb() {
 		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
 		db.beginTransaction();
 		this.dbHelper.onUpgrade(db, 0, 0);
 		db.setTransactionSuccessful();
 		db.endTransaction();
 		// db.close();
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		this.mActivity = this.getActivity();
 		this.dbHelper = DatabaseHelper.createInstance(this.mActivity);
 
 		this.bananas = new Item(this.idToDelete, 1, "Bananas", 4, 2, 1.50f, 1,
 				ItemUnitLabel.bag, true, 28, 4);
 		this.oranges = new Item(18, 1, "Oranges", 2, 2, 3.00f, 1,
 				ItemUnitLabel.bag, true, 27, 5);
 		this.beef = new Item(19, 2, "Beef", 3, 1, 4.50f, 1, ItemUnitLabel.lb,
 				true, 30, 8);
 
 		this.ida = new ItemDataAdapter();
 		this.ida.open();
 	}
 
 	/**
 	 * Tests to see if the Application was instantiated, and thus also if the
	 * singleon instance of DatabaseHelper was created.
 	 */
 	public void testApplicationInstantiated() {
 		assertNotNull(MyApplication.getInstance());
 		assertNotNull(this.dbHelper);
 	}
 
 	/**
 	 * Tests the item operation.
 	 */
 	public void testConstructFullItem() {
 		assertEquals(this.idToDelete, this.bananas.getId());
 		assertEquals("Bananas", this.bananas.getName());
 		assertEquals(4, this.bananas.getnStock());
 		assertEquals(2, this.bananas.getnBuy());
 		assertEquals(1.50, this.bananas.getPrice(), EPSILON);
 		assertEquals(1, this.bananas.getUnitSize(), EPSILON);
 		assertEquals(ItemUnitLabel.bag, this.bananas.getUnitLabel());
 		assertTrue(this.bananas.isBought());
 		assertEquals(28, this.bananas.getStockIdx());
 		assertEquals(4, this.bananas.getShopIdx());
 	}
 
 	/**
 	 * Tests the item operation.
 	 */
 	public void testIncrement() {
 		this.bananas.incrementNumberToBuy();
 		this.bananas.incrementNumberToBuy();
 		assertEquals(4, this.bananas.getnBuy());
 	}
 
 	/**
 	 * Tests the item operation.
 	 */
 	public void testResetNumberToBuy() {
 		this.bananas.resetNumberToBuy();
 		assertEquals(0, this.bananas.getnBuy());
 	}
 
 	/**
 	 * Tests the item operation.
 	 */
 	public void testTotalPrice() {
 		assertEquals(3.0f, this.bananas.totalPrice(), EPSILON);
 		this.beef.resetNumberToBuy();
 		assertEquals(0.0f, this.beef.totalPrice(), EPSILON);
 	}
 
 	/**
 	 * Tests the item operation.
 	 */
 	public void testTotalSpent() {
 		assertEquals(3.0f, this.bananas.totalSpent(), EPSILON);
 		this.bananas.setBought(false);
 		assertEquals(0.0f, this.bananas.totalSpent(), EPSILON);
 		this.beef.resetNumberToBuy();
 		assertEquals(0.0f, this.beef.totalPrice(), EPSILON);
 	}
 
 	/**
 	 * Tests the item operation.
 	 */
 	public void testToString() {
 		String expected = String.format("%d 1 Bananas (2/4) 2K/1.0 bag B 28 4",
 				this.idToDelete);
 		assertEquals(expected, this.bananas.toString());
 	}
 
 	/**
 	 * Tests the item operation.
 	 */
 	public void testShortString() {
 		// CONSIDER do this when I write it.
 	}
 
 	/**
 	 * Tests the item operation.
 	 */
 	public void testInsert() {
 		purgeDb();
 		this.ida.insertItem(this.bananas);
 		this.ida.insertItem(this.bananas);
 		this.ida.insertItem(this.oranges);
 		this.ida.insertItem(this.beef);
 
 		// TODO: test that it's in the DB. Currently just use a utility to
 		// check.
 	}
 
 	/**
 	 * Tests the item operation.
 	 */
 	public void testUpdate() {
 		purgeDb();
 		this.ida.insertItem(this.bananas);
 		this.bananas.setName("Apples");
 		this.ida.updateItem(this.bananas);
 	}
 
 	/**
 	 * Tests the item operation.
 	 */
 	public void testDelete() {
 		purgeDb();
 		this.ida.insertItem(this.bananas);
 		assertFalse(this.ida.deleteItem(this.beef));
 		assertTrue(this.ida.deleteItem(this.bananas));
 	}
 
 	/**
 	 * Tests the item operation.
 	 */
 	public void testLoadAllItemsWithListId() {
 		purgeDb();
 		this.ida.insertItem(this.bananas);
 		this.ida.insertItem(this.oranges);
 		this.ida.insertItem(this.beef);
 
 		ArrayList<Item> items = new ArrayList<Item>();
 		long shoppingListId = 1;
 		this.ida.loadAllItemsWithListId(items, shoppingListId);
 
 		// The first one in the DB should be the matching item.
 		// The IDs may not match, though, so we don't test them.
 		this.bananas.setId(items.get(0).getId());
 		assertEquals(this.bananas, items.get(0));
 		assertEquals(this.oranges, items.get(1));
 		assertEquals(2, items.size());
 	}
 
 	/**
 	 * Tests the item operation.
 	 */
 	public void testDeleteAllItemsWithListId() {
 		purgeDb();
 		this.ida.insertItem(this.bananas);
 		this.ida.insertItem(this.oranges);
 		this.ida.insertItem(this.beef);
 
 		long shoppingListId = 1;
 		int nDeleted = this.ida.deleteAllItemsWithListId(shoppingListId);
 		assertEquals(2, nDeleted);
 	}
 }
