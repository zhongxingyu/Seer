 package com.shawnhanna.shop;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.preference.PreferenceManager;
 import android.util.Base64InputStream;
 import android.util.Base64OutputStream;
 import android.util.Log;
 
 /**
  * 
  * @author Shawn -
  * @brief This is a singleton storage class that stores to the internal file
  *        storage
  */
 @SuppressWarnings("deprecation")
 public class DataService {
 	private static final String TAG = "DataService";
 
 	private Lock cartLock = new ReentrantLock();
	private Lock dotLock = new ReentrantLock();
 	private Lock listLock = new ReentrantLock();
 	private Lock dbLock = new ReentrantLock();
 	private ArrayList<Item> db = new ArrayList<Item>(50);
 	private ArrayList<Item> cartList = new ArrayList<Item>(50);
 	private ArrayList<Item> checkedList = new ArrayList<Item>(50);
 	private ArrayList<MapDot> dotList = new ArrayList<MapDot>();
 
 	// most recently "selected" item. Must be set before being called
 	private Item selectedItem = null;
 
 	private static DataService singleton;
 
 	private DataService() {
 	}
 
 	public static synchronized DataService getInstance() {
 		if (DataService.singleton == null) {
 			singleton = new DataService();
 			singleton.load();
 		}
 		return singleton;
 	}
 
 	public ArrayList<Item> getCart() {
 		cartLock.lock();
 		@SuppressWarnings("unchecked")
 		ArrayList<Item> retList = (ArrayList<Item>) cartList.clone();
 		cartLock.unlock();
 		return retList;
 	}
 
 	public ArrayList<Item> getChecked() {
 		listLock.lock();
 		@SuppressWarnings("unchecked")
 		ArrayList<Item> retList = (ArrayList<Item>) checkedList.clone();
 		listLock.unlock();
 		return retList;
 	}
 
 	public ArrayList<MapDot> getDots() {
		dotLock.lock();
 		@SuppressWarnings("unchecked")
 		ArrayList<MapDot> retList = (ArrayList<MapDot>) dotList.clone();
		dotLock.unlock();
 		return retList;
 	}
 
 	public void addDot(MapDot m) {
		dotLock.lock();
 		dotList.add(m);
		dotLock.unlock();
 	}
 
 	public ArrayList<Item> getDB() {
 		dbLock.lock();
 		@SuppressWarnings("unchecked")
 		ArrayList<Item> retList = (ArrayList<Item>) db.clone();
 		dbLock.unlock();
 		return retList;
 	}
 
 	// this is not very efficient, feel free to change it if you have time -
 	// john 3/30
 	public ArrayList<Item> getDBMinusCart() {
 		ArrayList<Item> newArray = new ArrayList<Item>();
 		for (int i = 0; i < db.size(); i++) {
 			if (!cartList.contains(db.get(i)))
 				newArray.add(db.get(i));
 		}
 		return newArray;
 	}
 
 	public void addToCart(Item item) {
 		cartLock.lock();
 		if (!this.inCart(item))
 			cartList.add(item);
 		cartLock.unlock();
 	}
 
 	public void addToChecked(Item item) {
 		listLock.lock();
 		if (!this.inChecked(item))
 			checkedList.add(item);
 		listLock.unlock();
 	}
 
 	protected void addToDB(Item item) {
 		dbLock.lock();
 		if (!this.inDB(item))
 			db.add(item);
 		dbLock.unlock();
 	}
 
 	public void removeFromCart(Item item) {
 		cartLock.lock();
 		if (this.inCart(item)) {
 			item.setQuantity(1);
 			cartList.remove(item);
 			if (inChecked(item)) {
 				removeFromChecked(item);
 			}
 		}
 		cartLock.unlock();
 	}
 
 	public void removeFromChecked(Item item) {
 		listLock.lock();
 		if (this.inChecked(item)) {
 			checkedList.remove(item);
 		}
 		listLock.unlock();
 	}
 
 	private boolean inDB(Item item) {
 		for (int i = 0; i < db.size(); i++) {
 			if (db.get(i).equals(item)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean inCart(Item item) {
 		for (int i = 0; i < cartList.size(); i++) {
 			if (cartList.get(i).equals(item)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean inChecked(Item item) {
 		listLock.lock();
 		for (int i = 0; i < checkedList.size(); i++) {
 			if (checkedList.get(i).equals(item)) {
 				return true;
 			}
 		}
 		listLock.unlock();
 		return false;
 	}
 
 	public boolean incrementItem(Item item) {
 		for (int i = 0; i < cartList.size(); i++) {
 			if (cartList.get(i).equals(item)) {
 				cartList.get(i).incrementQuantity();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean decrementItem(Item item) {
 		for (int i = 0; i < cartList.size(); i++) {
 			if (cartList.get(i).equals(item)) {
 				cartList.get(i).decrementQuantity();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean replaceInCart(Item item) {
 		for (int i = 0; i < cartList.size(); i++) {
 			if (cartList.get(i).equals(item)) {
 				cartList.set(i, item);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String getCartPriceAsString() {
 		double price = 0;
 
 		for (int i = 0; i < cartList.size(); i++) {
 			price += (cartList.get(i).getPrice() * cartList.get(i)
 					.getQuantity());
 		}
 
 		NumberFormat formatter = NumberFormat.getCurrencyInstance();
 		return formatter.format(price);
 	}
 
 	public Item getSelectedItem() {
 		return selectedItem;
 	}
 
 	public void setSelectedItem(Item item) {
 		selectedItem = item;
 	}
 
 	public boolean isItemSelected() {
 		return selectedItem != null;
 	}
 
 	public void unSelectItem() {
 		selectedItem = null;
 	}
 
 	/**
 	 * saves the data so it is persistent even after being destroyed by the OS
 	 * Call this when the application is about to be destroyed
 	 */
 	public void save() {
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(ShopActivity.getContext());
 		Editor editor = prefs.edit();
 		editor.putString("cartList", objectToString(cartList));
 		editor.putString("db", objectToString(db));
 		editor.commit();
 	}
 
 	/**
 	 * loads any persistent data, such as the cart, and the database TODO: make
 	 * this work?
 	 */
 	protected void load() {
 
 		addToDB(new Item("Hungry Jack Potatoes - Mashed 15.3oz",
 				"Hungry Jack Potatoes", 1.99, "051500871027", 1, false));
 		addToDB(new Item("San Giorgio Small Rigatoni 1lb", "Rigatoni", 1.25,
 				"033400601249", 2, false));
 		addToDB(new Item("Sweet Baby Ray's Barbecue Sauce - Oringinal 28oz",
 				"Barbeque Sauce", 3.99, "013409352311", 3, false));
 		addToDB(new Item("Aunt Jemima - Original 24floz", "Syrup", 2.99,
 				"013409352311", 4, false));
 		addToDB(new Item("Del Grosso Pasta Sause - Mushroom 24oz",
 				"Pasta Sauce", 1.25, "074908324407", 5, false));
 		addToDB(new Item("Jif Creamy Peanut Butter 48oz", "Peanut Butter",
 				3.00, "051500240946", 6, false));
 		addToDB(new Item("Blue Diamond Almonds Bold - Wasabi & Soy Sauce 16oz",
 				"Wasabi Almonds", 8.00, "041570055373", 7, false));
 		addToDB(new Item("Trident Xtra Care - Peppermint gum - 14 pieces",
 				"Trident Gum", 1.00, "012546673716", 8, false));
 		addToDB(new Item(
 				"Colgate Total Clean Mint Toothpaste: Travel Size 0.75 OZ",
 				"Colgate Total Clean Mint Toothpaste", 1.49, "035000740007", 9,
 				false));
 
 	}
 
 	public Editor getPreferenceEditor() {
 		return PreferenceManager.getDefaultSharedPreferences(
 				ShopActivity.getContext()).edit();
 	}
 
 	public void syncToServer() {
 		throw new UnsupportedOperationException("Not yet implemented");
 	}
 
 	public ArrayList<Item> searchDBByName(String string) {
 		ArrayList<Item> ret = new ArrayList<Item>(db.size());
 		for (int i = 0; i < db.size(); i++) {
 			Item item = db.get(i);
 			if (item.getName().toUpperCase().contains((string.toUpperCase()))
 					|| item.getShortName().toUpperCase()
 							.contains(string.toUpperCase())) {
 				ret.add(item);
 			}
 		}
 		return ret;
 	}
 
 	public ArrayList<Item> searchDBByNameIgnoreCart(String string) {
 		ArrayList<Item> availableItems = new ArrayList<Item>(getDBMinusCart());
 		ArrayList<Item> ret = new ArrayList<Item>(availableItems.size());
 		for (int i = 0; i < availableItems.size(); i++) {
 			Item item = availableItems.get(i);
 			if (item.getName().toUpperCase().contains((string.toUpperCase()))
 					|| item.getShortName().toUpperCase()
 							.contains(string.toUpperCase())) {
 				ret.add(item);
 			}
 		}
 		return ret;
 	}
 
 	private static String objectToString(Serializable object) {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		try {
 			new ObjectOutputStream(out).writeObject(object);
 			byte[] data = out.toByteArray();
 			out.close();
 			out = new ByteArrayOutputStream();
 			Base64OutputStream b64 = new Base64OutputStream(out, 0);
 			b64.write(data);
 			b64.close();
 			out.close();
 			return new String(out.toByteArray());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static Object stringToObject(String encodedObject) {
 		if (encodedObject == null) {
 			Log.d(TAG, "could not create object... string is null");
 			return null;
 		}
 		try {
 			return new ObjectInputStream(new Base64InputStream(
 					new ByteArrayInputStream(encodedObject.getBytes()), 0))
 					.readObject();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
