 package ch.unibe.ese.core.sqlite;
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import ch.unibe.ese.core.Friend;
 import ch.unibe.ese.core.Item;
 import ch.unibe.ese.core.ShoppingList;
 
 /**
  * This Class provides usefull functions for reading the database
  * 
  * @author Stephan
  * 
  */
 public class SQLiteReadHelper {
 
 	private SQLiteDatabase database;
 	private static final String SELECT_ITEM_DATA = "SELECT " //
 			+ SQLiteHelper.TABLE_ITEMS
 			+ "."
 			+ SQLiteHelper.COLUMN_ITEM_ID
 			+ ","
 			+ SQLiteHelper.TABLE_ITEMS
 			+ "."
 			+ SQLiteHelper.COLUMN_ITEM_NAME
 			+ ","
 			+ SQLiteHelper.TABLE_ITEMTOLIST
 			+ "."
 			+ SQLiteHelper.COLUMN_ITEMBOUGHT
 			+ ","
 			+ SQLiteHelper.TABLE_ITEMTOLIST
 			+ "."
 			+ SQLiteHelper.COLUMN_LISTPRICE
 			+ " FROM "
 			+ SQLiteHelper.TABLE_ITEMS
 			+ ","//
 			+ SQLiteHelper.TABLE_ITEMTOLIST
 			+ " WHERE "
 			+ SQLiteHelper.TABLE_ITEMS + "."
 			+ SQLiteHelper.COLUMN_ITEM_ID
 			+ " = " + SQLiteHelper.TABLE_ITEMTOLIST
 			+ "."
 			+ SQLiteHelper.COLUMN_ITEM_ID
 			+ " AND "
 			+ SQLiteHelper.TABLE_ITEMTOLIST + "."
 			+ SQLiteHelper.COLUMN_LIST_ID
 			+ " = ?";;
 
 	public SQLiteReadHelper(SQLiteDatabase database) {
 		this.database = database;
 	}
 
 	/**
 	 * Get a cursor on TABLE_LISTS
 	 * 
 	 * @return
 	 */
 	public Cursor getListCursor() {
 		Cursor cursor = getQueryCursor(SQLiteHelper.TABLE_LISTS,
 				SQLiteHelper.LISTS_COLUMNS, null);
 		cursor.moveToFirst();
 		return cursor;
 	}
 
 	/**
 	 * Get a cursor on TABLE_ITEMTOLIST for a specific listID
 	 * 
 	 * @param list
 	 * @return
 	 */
 	public Cursor getItemCursor(ShoppingList list) {
 		int listId = getListId(list.getName());
 
 		Cursor cursor = database.rawQuery(SELECT_ITEM_DATA, new String[] { ""
 				+ listId });
 		cursor.moveToFirst();
 		return cursor;
 	}

 	/**
 	 * Get a cursor on TABLE_FRIENDS
 	 * 
 	 * @return
 	 */
 	public Cursor getFriendCursor() {
 		Cursor cursor = getQueryCursor(SQLiteHelper.TABLE_FRIENDS,
 				SQLiteHelper.FRIENDS_COLUMNS, null);
 		cursor.moveToFirst();
 		return cursor;
 	}
 
 	/**
 	 * Shortcut for database querys (to save all the nulls)
 	 * 
 	 * @param table
 	 * @param columns
 	 * @param where
 	 * @return
 	 */
 	public Cursor getQueryCursor(String table, String[] columns, String where) {
 		Cursor cursor = database.query(table, columns, where, null, null, null,
 				null);
 		return cursor;
 	}
 
 	/**
 	 * Converts a cursor (at right position) to a shopping list
 	 * 
 	 * @param cursor
 	 * @return
 	 */
 	public ShoppingList cursorToShoppingList(Cursor cursor) {
 		ShoppingList list = new ShoppingList(cursor.getString(1));
 		if (cursor.getLong(2) > 0)
 			list.setDueDate(new Date(cursor.getLong(2)));
 
 		list.setShop(getShopName(cursor.getInt(3)));
 		return list;
 	}
 
 	public Item cursorToItem(Cursor cursor) {
 		Item item = new Item(cursor.getString(1));
 		item.setId(cursor.getInt(0));
 		item.setBought(cursor.getInt(2) == 1);
 		String price = cursor.getString(3);
 		if (price != null && !price.isEmpty())
 			item.setPrice(new BigDecimal(price));
 		return item;
 	}

 	public Friend cursorToFriend(Cursor cursor) {
 		Friend friend = new Friend(cursor.getInt(0), cursor.getString(1));
 		return friend;
 	}
 
 	/**
 	 * Get Shop name with shop ID
 	 * 
 	 * @param shopId
 	 * @return shopname
 	 */
 	public String getShopName(int shopId) {
 		Cursor cursor = database.query(SQLiteHelper.TABLE_SHOPS,
 				SQLiteHelper.SHOPS_COLUMNS, SQLiteHelper.COLUMN_SHOP_ID + "=?",
 				new String[] { "" + shopId }, null, null, null, null);
 		if (cursor.getCount() == 1) {
 			cursor.moveToFirst();
 			return cursor.getString(1);
 		} else {
 			return null;
 		}
 	}
 
 	/**
	 * Get Shop ID with shop name or -1 if the shop does not exist.
 	 * 
 	 * @param shopName
 	 * @return
 	 */
 	public int getShopId(String shopName) {
		if (shopName == null)
			return -1;
 		Cursor cursor = database.query(SQLiteHelper.TABLE_SHOPS,
 				SQLiteHelper.SHOPS_COLUMNS, SQLiteHelper.COLUMN_SHOP_NAME
 						+ "=?", new String[] { shopName }, null, null, null,
 				null);
 		System.err.println("count: " + cursor.getCount());
 		if (cursor.getCount() == 1) {
 			cursor.moveToFirst();
 			return cursor.getInt(0);
 		} else {
 			return -1;
 		}
 	}
 
 	/**
 	 * Get List Name with list ID
 	 * 
 	 * @param listId
 	 * @return
 	 */
 	public String getListName(int listId) {
 		Cursor cursor = database.query(SQLiteHelper.TABLE_LISTS,
 				SQLiteHelper.LISTS_COLUMNS, SQLiteHelper.COLUMN_LIST_ID + "=?",
 				new String[] { "" + listId }, null, null, null, null);
 		if (cursor.getCount() == 1) {
 			return cursor.getString(1);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Get List ID with list Name
 	 * 
 	 * @param listName
 	 * @return
 	 */
 	public int getListId(String listName) {
 		Cursor cursor = database.query(SQLiteHelper.TABLE_LISTS,
 				SQLiteHelper.LISTS_COLUMNS, SQLiteHelper.COLUMN_LIST_NAME
 						+ "=?", new String[] { listName }, null, null, null);
 		if (cursor.getCount() == 1) {
 			cursor.moveToFirst();
 			return cursor.getInt(0);
 		} else {
 			return -1;
 		}
 	}
 
 	/**
 	 * Get Item name with item ID
 	 * 
 	 * @param itemId
 	 * @return
 	 */
 	public String getItemName(int itemId) {
 		Cursor cursor = database.query(SQLiteHelper.TABLE_ITEMS,
 				SQLiteHelper.ITEMS_COLUMNS, SQLiteHelper.COLUMN_ITEM_ID + "=?",
 				new String[] { "" + itemId }, null, null, null, null);
 		if (cursor.getCount() == 1) {
 			cursor.moveToFirst();
 			return cursor.getString(1);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Get Item Id with Item Name
 	 * 
 	 * @param itemName
 	 * @return
 	 */
 	public long getItemId(String itemName) {
 		Cursor cursor = database.query(SQLiteHelper.TABLE_ITEMS,
 				SQLiteHelper.ITEMS_COLUMNS, SQLiteHelper.COLUMN_ITEM_NAME
 						+ "=?", new String[] { itemName }, null, null, null,
 				null);
 		if (cursor.getCount() == 1) {
 			cursor.moveToFirst();
 			return cursor.getLong(0);
 		} else {
 			return -1;
 		}
 	}
 
 	/**
 	 * Get friend name with item ID
 	 * 
 	 * @param itemId
 	 * @return
 	 */
 	public String getFriendName(int friendId) {
 		Cursor cursor = database.query(SQLiteHelper.TABLE_FRIENDS,
				SQLiteHelper.FRIENDS_COLUMNS, SQLiteHelper.COLUMN_FRIEND_ID
						+ "=?", new String[] { "" + friendId }, null, null,
				null, null);
 		if (cursor.getCount() == 2) {
 			cursor.moveToFirst();
 			return cursor.getString(2);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Get Item Id with Item Name
 	 * 
 	 * @param itemName
 	 * @return
 	 */
 	public long getFriendId(String friendName) {
 		Cursor cursor = database.query(SQLiteHelper.TABLE_FRIENDS,
				SQLiteHelper.FRIENDS_COLUMNS, SQLiteHelper.COLUMN_FRIEND_ID
						+ "=?", new String[] { "" + friendName }, null, null,
				null, null);
 		if (cursor.getCount() == 2) {
 			cursor.moveToFirst();
 			return cursor.getLong(0);
 		} else {
 			return -1;
 		}
 	}
 
 	/**
 	 * Check if an item is already in a list (database level)
 	 * 
 	 * @param item
 	 * @param list
 	 * @return true if item is in list
 	 */
 	public boolean isInList(Item item, ShoppingList list) {
 		long itemId = item.getId() == null ? -1 : item.getId();
 		int listId = getListId(list.getName());
 		Cursor cursor = database.query(SQLiteHelper.TABLE_ITEMTOLIST,
 				SQLiteHelper.ITEMTOLIST_COLUMNS, SQLiteHelper.COLUMN_ITEM_ID
 						+ "=? AND " + SQLiteHelper.COLUMN_LIST_ID + "=?",
 				new String[] { "" + itemId, "" + listId }, null, null, null,
 				null);
 		return (cursor.getCount() >= 1);
 	}
 
 }
