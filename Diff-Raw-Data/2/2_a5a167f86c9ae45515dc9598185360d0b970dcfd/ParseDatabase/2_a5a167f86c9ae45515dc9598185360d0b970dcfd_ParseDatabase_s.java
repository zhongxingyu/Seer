 package com.example.myapp;
 
 import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseFile;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 /**
  * A wrapper class for transmitting and receiving data from parse.com
  * 
  * @author Fable
  * 
  */
 public class ParseDatabase {
 	public ParseDatabase(Context context) {
 		Parse.initialize(context, "2TGrIyvNfLwNy3kM8OnZLAQGtSW2f6cR3k9oxHak",
 				"Y8xlSKdSilJBepTNIJqthpbJ9KeppDWCdNUQdYFX");
 
 	}
 
 	/**
 	 * 
 	 * @param sell
 	 *            : sellable
 	 * @return a parseobject
 	 */
 	public static ParseObject createSellableParseObj(Sellable sell) {
 		ParseObject obj = new ParseObject("Sellable");
 		obj.put(MITBAYActivity.NAME, sell.getName());
 		obj.put(MITBAYActivity.PRICE, sell.getPrice());
 		obj.put(MITBAYActivity.TYPE, sell.getType());
 		obj.put(MITBAYActivity.CONDITION, sell.getCondition());
		obj.put(MITBayActivity., sell.getSeller().getName());
 		obj.put("enabled", sell.isEnabled());
 		obj.put("description", sell.getDescription());
 		return obj;
 	}
 
 	/**
 	 * saves sellable on parse server
 	 * 
 	 * @param sell
 	 *            : sellable
 	 */
 	public String sendSellableToServer(Sellable sell) {
 		ParseObject sellobj = createSellableParseObj(sell);
 		if (sell.getImages() != null) {
 			ParseObject bigpic = storeBigPicture(sell);
 			byte[] data = ParseDatabase.scaledBitmapToByteArray(sell
 					.getImages());
 			ParseFile file = new ParseFile("picture.png", data);
 			file.saveInBackground();
 			sellobj.put("pic", file);
 			sellobj.put("bigpic", bigpic);
 			sellobj.saveInBackground();
 		}
 		return sellobj.getObjectId();
 	}
 
 	private ParseObject storeBigPicture(Sellable sell) {
 		ParseObject sellobjpic = new ParseObject("ImageSellable");
 		byte[] bigpic = ParseDatabase.bitmapToByteArray(sell.getImages());
 		ParseFile file2 = new ParseFile("bigpicture.png", bigpic);
 		file2.saveInBackground();
 		this.associateParseObjWithParseFile(file2, sellobjpic);
 		return sellobjpic;
 	}
 
 	/**
 	 * sends parseobject to server
 	 * 
 	 * @param obj
 	 *            : parseobject
 	 */
 	public void sendParseObjToServer(ParseObject obj) {
 		obj.saveEventually();
 	}
 
 	/**
 	 * 
 	 * @param user
 	 *            : instance of User
 	 * @param name
 	 *            : name of sellable
 	 * @return parsequery you can use to query in parse server
 	 */
 	public ParseQuery getSellableWithNameAndUser(User user, String name) {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.whereEqualTo("seller", user.getName());
 		query.whereEqualTo("name", name);
 		return query;
 	}
 
 	/**
 	 * This is a factory function that will return the ArrayList of Sellable
 	 * specify with the UserName and the name of the item. One Example of use is
 	 * .getListOfSellableWithNameAndUser(tran,"6.005") will return the arrayList
 	 * of all item with name 6.005 sold by user tran (or Null if nothing was
 	 * found)
 	 * 
 	 * @param user
 	 *            User the seller
 	 * @param name
 	 *            The name of the item
 	 * @return ArrayList of Sellabe object, null if nothing is found
 	 * @throws ParseException
 	 */
 	public ArrayList<Sellable> getListOfSellableWithNameAndUser(User user,
 			String name) throws ParseException {
 		ParseQuery query = getSellableWithNameAndUser(user, name);
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param user
 	 *            : instance of User
 	 * @return a list of sellable objects sold by user
 	 */
 	public ParseQuery getSellableOfUser(User user) {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.whereEqualTo("seller", user.getName());
 		return query;
 	}
 
 	/**
 	 * This is a factory function that will return the ArrayList of Sellable
 	 * specify with the UserName . One Example of use is
 	 * .getListOfSellableWithNameAndUser(tran,"6.005") will return the arrayList
 	 * of all items sold by user tran (or Null if nothing was found)
 	 * 
 	 * @param user
 	 *            User the seller
 	 * @return ArrayList of Sellabe object, or empty ArrayList if not found
 	 * @throws ParseException
 	 */
 	public ArrayList<Sellable> getListSellableOfUser(User user)
 			throws ParseException {
 		ParseQuery query = getSellableOfUser(user);
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Gets a specific sellable from Textbook, Electronics, Transportation, Misc
 	 * 
 	 * @param type
 	 *            : Selltype enum
 	 * @return a list of sellable objects of that selltype
 	 */
 	public ParseQuery getType(String type) {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.whereEqualTo("type", type);
 		return query;
 	}
 
 	/**
 	 * Gets an ArraList of Sellable from Textbook, Electronics, Transportation,
 	 * Misc Example of use .getListType("TEXT BOOK") will return the list of all
 	 * TEXTBOOK sold
 	 * 
 	 * @param type
 	 *            : String of type i.e ELECTRONIC
 	 * @return a list of sellable objects of that selltype or empty ArrayList if
 	 *         not found
 	 * @throws ParseException
 	 */
 	public ArrayList<Sellable> getListType(String type) throws ParseException {
 		ParseQuery query = getType(type);
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @return a list of sellable objects that are enabled
 	 */
 	public ParseQuery getEnabled() {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.whereEqualTo("enabled", true);
 		query.orderByDescending("createdAt");
 		return query;
 	}
 
 	/**
 	 * Return an ArrayList of Sellable Objects THAT still active but not yet
 	 * delete from the server)
 	 * 
 	 * @return a list of sellable objects that are enabled or empty ArrayList if
 	 *         not found
 	 * @throws ParseException
 	 */
 	public ArrayList<Sellable> getListEnabled() throws ParseException {
 		ParseQuery query = getEnabled();
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @return a list of sellable objects that are disabled
 	 */
 	public ParseQuery getDisabled() {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.whereEqualTo("enabled", false);
 		return query;
 	}
 
 	/**
 	 * Return an ArrayList of Sellable Objects that is inactive(someone click on
 	 * buy but not yet delete from the server)
 	 * 
 	 * @return a list of sellable objects that are disabled or empty ArrayList
 	 *         if not found
 	 * @throws ParseException
 	 */
 	public ArrayList<Sellable> getListDisabled() throws ParseException {
 		ParseQuery query = getDisabled();
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param cond
 	 *            : Condition enum, parameter of sellable
 	 * @return a list of sellables that have the specified condition
 	 */
 	public ParseQuery getCondition(String cond) {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.whereEqualTo("enabled", true);
 		query.whereEqualTo("condition", cond);
 		return query;
 	}
 
 	/**
 	 * Return an ArrayList of Sellable Objects that matches the condition (i.e
 	 * NEW, OLD, ACCEPTABLE)
 	 * 
 	 * @return an ArrayList of sellable objects that matches the condtion or
 	 *         empty ArrayList if not found
 	 * @throws ParseException
 	 */
 	public ArrayList<Sellable> getListCondition(String cond)
 			throws ParseException {
 		ParseQuery query = getCondition(cond);
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param date
 	 *            : instance of Date
 	 * @return a list of sellable objects created on date
 	 */
 	public ParseQuery getDate(Date date) {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.whereEqualTo("enabled", true);
 		query.whereEqualTo("date", date);
 		return query;
 	}
 
 	/**
 	 * Get an ArrayList of Sellable Object that is sold on a Specific Date
 	 * 
 	 * @param date
 	 *            : instance of Date
 	 * @return an ArrayList of sellable objects created on date
 	 */
 	public ArrayList<Sellable> getListDate(Date date) throws ParseException {
 		ParseQuery query = getDate(date);
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @return a list of all sellable objects in the parse server
 	 * @throws ParseException
 	 */
 	public ParseQuery getAllSellable() throws ParseException {
 		ParseQuery query = new ParseQuery("Sellable");
 		return query;
 	}
 	
 	/**
 	 * 
 	 * @return a list of all enabled sellable objects in the parse server
 	 * @throws ParseException
 	 */
 	public ParseQuery getAllEnabledSellable() throws ParseException {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.whereEqualTo("enabled", true);
 		return query;
 	}
 
 	/**
 	 * get All of the Sellable object from the server
 	 * 
 	 * @return an arrayList of all sellable objects in the parse server
 	 * @throws ParseException
 	 */
 	public ArrayList<Sellable> getListAllSellable() throws ParseException {
 		ParseQuery query = getAllSellable();
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * get All of the enabled Sellable object from the server
 	 * 
 	 * @return an arrayList of all sellable objects in the parse server
 	 * @throws ParseException
 	 */
 	public ArrayList<Sellable> getListAllEnabledSellable() throws ParseException {
 		ParseQuery query = getAllEnabledSellable();
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param skip
 	 *            : number of entries to skip
 	 * @return a list of all sellable objects that skip the specified number of
 	 *         entries
 	 */
 	public ParseQuery getAllSellableSetSkip(int skip) {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.whereEqualTo("enabled", true);
 		query.setSkip(skip);
 		return query;
 	}
 
 	/**
 	 * get an ArrayList of Sellable objects that equals = total - skip
 	 * 
 	 * @param skip
 	 *            : number of entries to skip
 	 * @return an arrayList of all sellable objects that skip the specified
 	 *         number of entries
 	 * @throws ParseException
 	 */
 	public ArrayList<Sellable> getListAllSellableSetSkip(int skip)
 			throws ParseException {
 		ParseQuery query = getAllSellableSetSkip(skip);
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * return the total number of sellable object current on the server
 	 * (everything)
 	 * 
 	 * @return an integer represents the total number of Sellable objects from
 	 *         server
 	 * @throws ParseException
 	 */
 	public int getTotalSellable() throws ParseException {
 		ParseQuery query = getAllSellable();
 		return query.count();
 	}
 	
 	/**
 	 * return the total number of enabled sellable object current on the server
 	 * (everything)
 	 * 
 	 * @return an integer represents the total number of Sellable objects from
 	 *         server
 	 * @throws ParseException
 	 */
 	public int getEnabledTotalSellable() throws ParseException {
 		ParseQuery query = getAllEnabledSellable();
 		return query.count();
 	}
 	
 
 	/**
 	 * 
 	 * @param name
 	 *            : name of sellable
 	 * @return parsequery you can use to query in parse server
 	 */
 	public ParseQuery getSellableWithName(String name) {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.whereEqualTo("enabled", true);
 		query.whereEqualTo("name", name);
 		return query;
 	}
 
 	/**
 	 * get the ArrayList of item with matching name
 	 * 
 	 * @param name
 	 *            : name of sellable
 	 * @return arrayList of Sellable with name match, or empty ArrayList if not
 	 *         found
 	 * @throws ParseException
 	 */
 	public ArrayList<Sellable> getListSellableWithName(String name)
 			throws ParseException {
 		ParseQuery query = getSellableWithName(name);
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Sorts the results in ascending order by the parameter field
 	 * 
 	 * @param parameter
 	 *            : parameter eg: name, price
 	 * @return: a list of objects sorted by parameter
 	 */
 	public ParseQuery returnInOrderByAscending(String parameter) {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.whereEqualTo("enabled", true);
 		query.orderByAscending(parameter);
 
 		return query;
 	}
 
 	/**
 	 * Sorts the results in ascending order by the parameter field
 	 * 
 	 * @param parameter
 	 *            : parameter eg: name, price
 	 * @return: an ArrayList of objects sorted by parameter
 	 * @throws ParseException
 	 */
 	public ArrayList<Sellable> returnListInOrderByAscending(String parameter)
 			throws ParseException {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.orderByAscending(parameter);
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Sorts the results in ascending order by the parameter field
 	 * 
 	 * @param parameter
 	 *            : parameter eg: name, price
 	 * @return: a list of objects sorted by parameter
 	 */
 	public ParseQuery returnInOrderByDescending(String parameter) {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.whereEqualTo("enabled", true);
 		query.orderByDescending(parameter);
 		return query;
 	}
 
 	/**
 	 * Sorts the results in descending order by the parameter field
 	 * 
 	 * @param parameter
 	 *            : parameter eg: name, price
 	 * @return: an ArrayList of objects sorted by parameter
 	 * @throws ParseException
 	 */
 	public ArrayList<Sellable> returnListInOrderByDescending(String parameter)
 			throws ParseException {
 		ParseQuery query = new ParseQuery("Sellable");
 		query.orderByDescending(parameter);
 		ArrayList<Sellable> result = new ArrayList<Sellable>();
 		int total = 0;
 		try {
 			total = query.count();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch blo
 			e.printStackTrace();
 		}
 		if (total > 0) {
 			for (ParseObject obj : query.find()) {
 				result.add(ParseDatabase.createSellableWithParse(obj));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Converts a sellable with a parseobject
 	 * 
 	 * @param obj
 	 *            : parseobject
 	 * @return a sellable that has the same parameters as obj
 	 */
 	public static Sellable createSellableWithParse(ParseObject obj) {
 
 		String name = (String) obj.get("name");
 		String price = (String) obj.get("price");
 
 		String description = (String) obj.get("description");
 		String type = (String) obj.get("type");
 		String condition = (String) obj.get("condition");
 		String seller = (String) obj.get("seller");
 		String id = (String) obj.getObjectId();
 		String email = (String) obj.get("seller");
 		User user = new User(seller, email);
 		Sellable sell = new Sellable(user, name, price, type, description,
 				condition, null);
 		sell.setId(id);
 		return sell;
 	}
 	
 	public static ParseObject createUserObject(String username, String email){
 		ParseObject user = new ParseObject("UserObject");
 		user.put("USERNAME", username);
 		user.put("EMAIL", email);
 		user.put("LOCATION", "Location not set");
 		JSONArray request = new JSONArray();
 		JSONArray selling = new JSONArray();
 		JSONArray buying = new JSONArray();
 		user.put("requesteditems", request);
 		user.put("requesteditems", selling);
 		user.put("buyingitems", buying);
 		user.put("sellingitems", selling);
 		return user;
 	}
 
 	/**
 	 * Converts a bitmap to a byte array so that it can be stored as a parse
 	 * file
 	 * 
 	 * @param bmp
 	 * @return a byte array
 	 */
 	public static byte[] bitmapToByteArray(Bitmap bmp) {
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
 		byte[] byteArray = stream.toByteArray();
 		return byteArray;
 	}
 
 	/**
 	 * First scales and then converts a bitmap to a byte array so that it can be
 	 * stored as a parse file
 	 * 
 	 * @param bmp
 	 * @return a byte array
 	 */
 	public static byte[] scaledBitmapToByteArray(Bitmap bmp) {
 		Bitmap scaled = Bitmap.createScaledBitmap(bmp, 50, 50, true);
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		scaled.compress(Bitmap.CompressFormat.PNG, 100, stream);
 		byte[] byteArray = stream.toByteArray();
 		return byteArray;
 	}
 
 	public ParseFile sendByteArrayToServer(byte[] data) {
 		ParseFile file = new ParseFile("picture.png", data);
 		file.saveInBackground();
 		return file;
 	}
 
 	public String associateParseObjWithParseFile(ParseFile file, ParseObject obj) {
 		obj.put("pic", file);
 		obj.saveInBackground();
 		return obj.getObjectId();
 	}
 
 }
