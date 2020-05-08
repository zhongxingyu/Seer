 package rpisdd.rpgme.gamelogic.items;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import rpisdd.rpgme.R;
 import rpisdd.rpgme.gamelogic.player.Player;
 import android.content.Context;
 import android.util.Log;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.stream.JsonReader;
 
 public abstract class Item {
 	private static final String TAG = "Item";
 
 	public static final Item INVALID = new Item("M.", -100, "invalid.png",
 			"WILD M. APPEARED", Integer.MAX_VALUE) {
 		@Override
 		public void useMe(Player p, int index) {
 			Log.wtf(TAG, "Trying to use invalid item.");
 		}
 	};
 
 	private final String name;
 	private final int price;
 	private final String imagePath;
 	private final String offsetImagePath;
 	private final String description;
 	private final int quality;
 
 	protected interface Factory {
 		public Item fromJsonObject(JsonObject o);
 	}
 
 	private static Map<String, Item> loadItems(Context c, Factory f,
 			int resource) {
 		Map<String, Item> items = new HashMap<String, Item>();
 		InputStreamReader r = new InputStreamReader(c.getResources()
 				.openRawResource(resource));
 		JsonReader jr = new JsonReader(new BufferedReader(r));
 		JsonParser jp = new JsonParser();
 		JsonArray jItems = jp.parse(jr).getAsJsonArray();
 		for (Iterator<JsonElement> jIter = jItems.iterator(); jIter.hasNext();) {
 			JsonObject jItem = jIter.next().getAsJsonObject();
 			Item item = f.fromJsonObject(jItem);
 			items.put(item.getName(), item);
 		}
 		return items;
 	}
 
 	public static void load(Context c) {
 		if (!allItems.isEmpty()) {
 			Log.e(TAG, "Items being loaded multiple times from database");
 		}
 		Log.d(TAG, "Loading equipment from JSON");
 		allItems.putAll(loadItems(c, new Factory() {
 			@Override
 			public Item fromJsonObject(JsonObject o) {
 				return new Equipment(o);
 			}
 		}, R.raw.equipment));
 		Log.d(TAG, "Loading consumables from JSON");
 		allItems.putAll(loadItems(c, new Factory() {
 			@Override
 			public Item fromJsonObject(JsonObject o) {
 				return new Consumable(o);
 			}
 		}, R.raw.consumables));
 		Log.d(TAG, "Loading boondogles from JSON");
 		allItems.putAll(loadItems(c, new Factory() {
 			@Override
 			public Item fromJsonObject(JsonObject o) {
 				return new Boondogle(o);
 			}
 		}, R.raw.boondogles));
 
 		sortedItems.addAll(allItems.values());
 		Collections.sort(sortedItems, new Comparator<Item>() {
 			@Override
 			public int compare(Item lhs, Item rhs) {
 				return lhs.getQuality() - rhs.getQuality();
 			}
 		});
 	}
 
 	private static final Map<String, Item> allItems = new HashMap<String, Item>();
 	private static final ArrayList<Item> sortedItems = new ArrayList<Item>();
 
 	public static Item createItemFromName(String aname) {
 		if (allItems.containsKey(aname)) {
 			return allItems.get(aname);
 		} else {
 			Log.e(TAG, "Trying to create an item that doesn't exist.");
 			return Item.INVALID;
 		}
 	}
 
 	public static List<Item> getQualityItems(int quality) {
 		int start = 0;
 		for (start = 0; start < sortedItems.size(); start++) {
 			if (sortedItems.get(start).getQuality() >= quality) {
 				break;
 			}
 		}
 		int end = 0;
 		for (end = start; end < sortedItems.size(); end++) {
			if (sortedItems.get(end).getQuality() > quality) {
 				break;
 			}
 		}
 
 		ArrayList<Item> qualityItems = new ArrayList<Item>();
 		// TODO: Make this distribute items with the same qualities more evenly.
 		for (int j = Math.max(0, start - 2); j < Math.min(end + 2,
 				sortedItems.size() - 1); j++) {
 			qualityItems.add(sortedItems.get(j));
 		}
 		return qualityItems;
 	}
 
 	private Item(String name, int price, String imageName, String description,
 			int quality) {
 		this.name = name;
 		this.price = price;
 		this.imagePath = "file:///android_asset/Items/" + imageName;
 		this.offsetImagePath = "file:///android_asset/Items/av_" + imageName;
 		this.description = description;
 		this.quality = quality;
 	}
 
 	protected Item(JsonObject o) {
 		this(o.get("name").getAsString(), o.get("price").getAsInt(), o.get(
 				"filename").getAsString(), o.get("description").getAsString(),
 				o.get("quality").getAsInt());
 	}
 
 	public int getRefundPrice() {
 		return getPrice() / 2;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public int getPrice() {
 		return price;
 	}
 
 	public String getImagePath() {
 		return imagePath;
 	}
 
 	public String getOffsetImagePath() {
 		return offsetImagePath;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	@Override
 	public String toString() {
 		return name;
 	}
 
 	public boolean isUsable(Player p) {
 		return false;
 	}
 
 	public boolean isEquipment() {
 		return false;
 	}
 
 	public int getQuality() {
 		return quality;
 	}
 
 	public abstract void useMe(Player p, int index);
 
 	public static void giveMeStuff(Player p, String... stuff) {
 		for (String thing : stuff) {
 			p.getInventory().addNewItem(createItemFromName(thing));
 		}
 	}
 }
