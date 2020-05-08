 package ch.unibe.ese.shopnote.core.sqlite;
 
 import ch.unibe.ese.shopnote.core.Friend;
 import ch.unibe.ese.shopnote.core.Item;
 import ch.unibe.ese.shopnote.core.Recipe;
 import ch.unibe.ese.shopnote.core.ShoppingList;
 import android.content.ContentValues;
 import android.database.sqlite.SQLiteDatabase;
 
 /**
  * This class provides useful functions for updating the database
  * 
  */
 public class SQLiteUpdateHelper {
 
 	private SQLiteDatabase database;
 	private SQLiteReadHelper readHelper;
 
 	public SQLiteUpdateHelper(SQLiteDatabase database, SQLiteReadHelper readHelper) {
 		this.database = database;
 		this.readHelper = readHelper;
 	}
 
 	/**
 	 * Converts a list to a ContentValue, which can be inserted into the
 	 * database
 	 * 
 	 * @param list
 	 * @return ContentValues of the input
 	 */
 	public ContentValues toValue(ShoppingList list) {
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelper.COLUMN_LIST_NAME, list.getName());
 		values.put(SQLiteHelper.COLUMN_LIST_ARCHIVED, list.isArchived() ? 1 : 0);
 		values.put(SQLiteHelper.COLUMN_LIST_SHARED, list.isShared() ? 1 : 0);
 		values.put(SQLiteHelper.COLUMN_LIST_CHANGESCOUNT, list.getChangesCount());
 		values.put(SQLiteHelper.COLUMN_LIST_DUEDATE, list.getDueDate() != null ? list.getDueDate().getTime() : null);
 		this.addShopIfNotExistent(list.getShop());
 		values.put(SQLiteHelper.COLUMN_SHOP_ID, readHelper.getShopId(list.getShop()));
 		return values;
 	}
 
 	/**
 	 * Converts an item to a ContentValue (TABLE_ITEMS)
 	 * 
 	 * @param item
 	 * @return ContentValues of the input
 	 */
 	public ContentValues toValue(Item item) {
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelper.COLUMN_ITEM_NAME, item.getName());
 		values.put(SQLiteHelper.COLUMN_ITEM_PRICE, toString(item.getPrice()));
 		values.put(SQLiteHelper.COLUMN_ITEM_QUANTITY, toString(item.getQuantity()));
 		values.put(SQLiteHelper.COLUMN_ITEM_UNIT, toString(item.getUnit()));
 		return values;
 	}
 
 	/**
 	 * Converts an item and a list to a Contentvalue (TABLE_ITEMTOLIST)
 	 * 
 	 * @param item
 	 * @param list
 	 * @return ContentValues of the input
 	 */
 	public ContentValues toValue(Item item, ShoppingList list) {
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelper.COLUMN_ITEM_ID, item.getId());
 		values.put(SQLiteHelper.COLUMN_LIST_ID, list.getId());
 		values.put(SQLiteHelper.COLUMN_ITEM_BOUGHT, item.isBought() ? 1 : 0);
 		values.put(SQLiteHelper.COLUMN_ITEM_PRICE, toString(item.getPrice()));
 		values.put(SQLiteHelper.COLUMN_ITEM_QUANTITY, toString(item.getQuantity()));
 		values.put(SQLiteHelper.COLUMN_ITEM_UNIT, toString(item.getUnit()));
 		return values;
 	}
 
 	/**
 	 * Converts an friend into a ContentValue (TABLE_FRIENDS)
 	 * 
 	 * @param friends
 	 * @return ContentValues of the input
 	 */
 	public ContentValues toValue(Friend friend) {
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelper.COLUMN_FRIEND_NAME, friend.getName());
 		values.put(SQLiteHelper.COLUMN_FRIEND_PHONENR, friend.getPhoneNr());
 		values.put(SQLiteHelper.COLUMN_FRIEND_HASAPP, friend.hasTheApp() ? 1 : 0);
 		return values;
 	}
 
 	/**
 	 * Converts a list.id and a friend.id into a ContentValue
 	 * (TABLE_FRIENDSTOLIST)
 	 * 
 	 * @param list
 	 * @param friend
 	 * @return ContentValues of the input
 	 */
 	public ContentValues toValue(ShoppingList list, Friend friend) {
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelper.COLUMN_LIST_ID, list.getId());
 		values.put(SQLiteHelper.COLUMN_FRIEND_ID, friend.getId());
 		return values;
 	}
 	
 	/**
 	 * Converts a recipe.id and a friend.id into a ContentValue
 	 * (TABLE_FRIENDSTORECIPE)
 	 * 
 	 * @param recipe
 	 * @param friend
 	 * @return ContentValues of the input
 	 */
 	public ContentValues toValue(Recipe recipe, Friend friend) {
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelper.COLUMN_RECIPE_ID, recipe.getId());
 		values.put(SQLiteHelper.COLUMN_FRIEND_ID, friend.getId());
 		return values;
 	}
 
 	/**
 	 * Converts an recipe into a ContentValue (TABLE_RECIPE)
 	 * 
 	 * @param recipe
 	 * @return ContentValues of the input
 	 */
 	public ContentValues toValue(Recipe recipe) {
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelper.COLUMN_RECIPE_NAME, recipe.getName());
 		values.put(SQLiteHelper.COLUMN_RECIPE_NOTES, recipe.getNotes());
 		values.put(SQLiteHelper.COLUMN_RECIPE_SHOWNOTES, recipe.isNotesVisible());
 		values.put(SQLiteHelper.COLUMN_RECIPE_SHARED, recipe.isShared() ? 1 : 0);
 		values.put(SQLiteHelper.COLUMN_RECIPE_CHANGESCOUNT, recipe.getChangesCount());
 		return values;
 	}
 
 	/**
 	 * Converts an item.id and an recipe.id into a ContentValue
 	 * (TABLE_ITEMTORECIPE)
 	 * 
 	 * @param recipe
 	 * @param item
 	 * @return ContentValues of the input
 	 */
 	public ContentValues toValue(Recipe recipe, Item item) {
 		ContentValues values = new ContentValues();
 		values.put(SQLiteHelper.COLUMN_RECIPE_ID, recipe.getId());
 		values.put(SQLiteHelper.COLUMN_ITEM_ID, item.getId());
 		values.put(SQLiteHelper.COLUMN_ITEM_PRICE, toString(item.getPrice()));
 		values.put(SQLiteHelper.COLUMN_ITEM_QUANTITY, toString(item.getQuantity()));
 		values.put(SQLiteHelper.COLUMN_ITEM_UNIT, toString(item.getUnit()));
 		return values;
 	}
 
 	/**
 	 * Adds a shop to the database if it doesn't exist yet
 	 * 
 	 * @param name
 	 */
 	private void addShopIfNotExistent(String name) {
 		if (readHelper.getShopId(name) == -1) {
 			ContentValues values = new ContentValues();
 			values.put(SQLiteHelper.COLUMN_SHOP_NAME, name);
 			database.insert(SQLiteHelper.TABLE_SHOPS, null, values);
 		}
 	}
 
 	/**
 	 * Adds an item to the database if it doesn't exist yet
 	 * 
 	 * @param item
 	 */
 	public void addItemIfNotExistent(Item item) {
		Item existing = readHelper.getItem(SQLiteHelper.COLUMN_ITEM_NAME, item.getName());
 		long id;
 		if (existing == null) {
 			ContentValues values = this.toValue(item);
 			id = database.insert(SQLiteHelper.TABLE_ITEMS, null, values);
 		} else {
 			id = existing.getId();
 		}
 		if (item.getId() == null)
 			item.setId(id);
 	}
 
 	/**
 	 * @param obj
 	 * @return The String represantation of this object. Is null if number is
 	 *         null.
 	 */
 	private String toString(Object obj) {
 		return obj == null ? null : obj.toString();
 	}
 }
