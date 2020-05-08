 /**
  * @Title: CBSettings.java
  * @Package: com.android.cb.source
  * @Author: Raiden Awkward<raiden.ht@gmail.com>
  * @Date: 2012-4-9
  */
 package com.android.cb.source;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import com.android.cb.support.CBTagsSet;
 
 /**
  * @author raiden
  *
  * @Description global application settings informations
  */
 public class CBSettings {
 
 	public static final String CB_SETTINGS_FILE_PATH = "/sdcard/cb/settings/app_settings.xml";
 
 	public static final String CB_SETTINGS_SOURCE_DIR = "cb.settings.source.dir";
 	public static final String CB_SETTINGS_SOURCE_DIR_DISHES = "cb.settings.source.dir.dishes";
 	public static final String CB_SETTINGS_SOURCE_DIR_ORDERS = "cb.settings.source.dir.orders";
 	public static final String CB_SETTINGS_SOURCE_DIR_SETTINGS = "cb.settings.source.dir.settings";
 	public static final String CB_SETTINGS_DEVICE_ID = "cb.settings.device.id";
 	public static final String CB_SETTINGS_LEFT_BUTTONS_TAGS_FILE = "cb.settings.left.buttons.tags.file";
 	public static final String CB_SETTINGS_ORDER_LOCATIONS_FILE = "cb.settings.left.locations.file";
 
 	public static final String CB_SETTINGS_LEFT_BUTTON_TEXT_SIZE = "cb.settings.left.button.text.size";
 	public static final String CB_SETTINGS_GRIDVIEW_COLUMN_COUNT = "cb.settings.grid.gridview.column.count";
 	public static final String CB_SETTINGS_ORDERING_DIALOG_MAX_ITEM_COUNT = "cb.settings.ordering.dialog.max.item.count";
 
 
 	/**
 	 * map for string options
 	 */
 	protected static HashMap<String, String> SETTINGS_MAP = new HashMap<String, String>();
 
 	static {
 		SETTINGS_MAP.put(CB_SETTINGS_DEVICE_ID, "unknown");
 		SETTINGS_MAP.put(CB_SETTINGS_SOURCE_DIR, "/sdcard/cb");
 		SETTINGS_MAP.put(CB_SETTINGS_SOURCE_DIR_DISHES, "/sdcard/cb/dishes");
 		SETTINGS_MAP.put(CB_SETTINGS_SOURCE_DIR_ORDERS, "/sdcard/cb/orders");
 		SETTINGS_MAP.put(CB_SETTINGS_SOURCE_DIR_SETTINGS, "/sdcard/cb/settings");
 		SETTINGS_MAP.put(CB_SETTINGS_LEFT_BUTTONS_TAGS_FILE, "/sdcard/cb/settings/left_buttons.xml");
 		SETTINGS_MAP.put(CB_SETTINGS_ORDER_LOCATIONS_FILE, "/sdcard/cb/settings/order_locations.xml");
 
 		SETTINGS_MAP.put(CB_SETTINGS_LEFT_BUTTON_TEXT_SIZE, "20");
 		SETTINGS_MAP.put(CB_SETTINGS_GRIDVIEW_COLUMN_COUNT, "3");
 		SETTINGS_MAP.put(CB_SETTINGS_ORDERING_DIALOG_MAX_ITEM_COUNT, "50");
 	}
 
 
 	/**
 	 * methods for settings saving and loading
 	 */
 	public static boolean save() {
 		return save(CB_SETTINGS_FILE_PATH);
 	}
 
 	public static boolean load() {
 		return load(CB_SETTINGS_FILE_PATH);
 	}
 
 	protected static final String XML_TAG_SETTINGS = "Settings";
 
 	public static boolean save(String xmlPath) {
 		if (xmlPath == null)
 			return false;
 
 		String settingsDir = getStringValue(CB_SETTINGS_SOURCE_DIR_SETTINGS);
 		if (settingsDir.length() > 0) {
 			File fileSettingDir = new File(settingsDir);
 			if (fileSettingDir.exists() == false)
 				fileSettingDir.mkdirs();
 		}
 
 		// saving main settings
 		CBXmlWriter writer = new CBXmlWriter(xmlPath);
 		if (writer.open() == false)
 			return false;
 
 		if (writer.writeXmlHeader() == false)
 			return false;
 
 		writer.writeStartTag(XML_TAG_SETTINGS, 0);
 
 		Iterator<Entry<String,String>> iter = SETTINGS_MAP.entrySet().iterator();
 		while (iter.hasNext()) {
 			Entry<String,String> entry = (Entry<String,String>) iter.next();
 			String name = entry.getKey().toString();
 			String value = entry.getValue().toString();
 			writer.writeOneLineTags(name, null, value, 1);
 		}
 
 		writer.writeEndTag(XML_TAG_SETTINGS, 0);
 		writer.close();
 
 		return true;
 	}
 
 	public static boolean load(String xmlPath) {
 		if (xmlPath == null)
 			return false;
 
 		// loading main settings
 		CBXmlParser parser = new CBXmlParser(xmlPath);
 		parser.setCallback(new CBXmlParser.Callback() {
 
 			public void onTagWithValueDetected(String tag, String value,
 					XmlPullParser parser) {
 				SETTINGS_MAP.put(tag, value);
 			}
 
 			public void onStartDocument(XmlPullParser parser) {
 
 			}
 
 			public void onEndDocument(XmlPullParser parser) {
 
 			}
 		});
 
 		if (parser.parse() == false)
 			return false;
 
 		// loading left buttons
 		LEFT_BUTTONS_TAGS_SET = null;
 		if (getLeftButtonTagsSet() == null)
 			return false;
 
 		// loading order location list
 		ORDER_LOCATION_LIST = null;
 		if (getOrderLocationList() == null)
 			return false;
 
 		return true;
 	}
 
 
 	/**
 	 * members and methods for LEFT_BUTTONS_TAG_LIST
 	 */
 
 	protected static CBTagsSet LEFT_BUTTONS_TAGS_SET = null;
 
 	protected static final String XML_TAG_LEFT_BUTTON_LIST = "LeftButtonList";
 	protected static final String XML_TAG_LEFT_BUTTON = "LeftButton";
 
 	public static CBTagsSet getLeftButtonTagsSet() {
 		if (LEFT_BUTTONS_TAGS_SET == null) {
			String xmlPath = CBSettings.getStringValue(CB_SETTINGS_SOURCE_DIR_SETTINGS)
			+ "/"
			+ CBSettings.getStringValue(CB_SETTINGS_LEFT_BUTTONS_TAGS_FILE);

 			return loadLeftButtonTagsSet(xmlPath);
 		}
 
 		return LEFT_BUTTONS_TAGS_SET;
 	}
 
 	public static CBTagsSet loadLeftButtonTagsSet(String xmlPath) {
 		if (xmlPath == null)
 			return null;
 
 		CBXmlParser parser = new CBXmlParser(xmlPath);
 		parser.setCallback(new CBXmlParser.Callback() {
 
 			public void onTagWithValueDetected(String tag, String value,
 					XmlPullParser parser) {
 				if (tag.equalsIgnoreCase(XML_TAG_LEFT_BUTTON)) {
 					if (LEFT_BUTTONS_TAGS_SET != null) {
 						LEFT_BUTTONS_TAGS_SET.add(value);
 					}
 				}
 			}
 
 			public void onStartDocument(XmlPullParser parser) {
 				LEFT_BUTTONS_TAGS_SET = new CBTagsSet();
 			}
 
 			public void onEndDocument(XmlPullParser parser) {
 
 			}
 		});
 
 		parser.parse();
 
 		return LEFT_BUTTONS_TAGS_SET;
 	}
 
 	public static boolean saveLeftButtonTagsSet(String xmlPath) {
 		if (xmlPath == null)
 			return false;
 
 		CBXmlWriter writer = new CBXmlWriter(xmlPath);
 		if (writer.open() == false)
 			return false;
 
 		if (writer.writeXmlHeader() == false)
 			return false;
 
 		writer.writeStartTag(XML_TAG_LEFT_BUTTON_LIST, 0);
 
 		for (int i = 0; i < LEFT_BUTTONS_TAGS_SET.count(); ++i) {
 			String tag = LEFT_BUTTONS_TAGS_SET.get(i);
 			writer.writeOneLineTags(XML_TAG_LEFT_BUTTON, null, tag, 1);
 		}
 
 		writer.writeEndTag(XML_TAG_LEFT_BUTTON_LIST, 0);
 
 		writer.close();
 		return true;
 	}
 
 
 
 	/**
 	 * members and methods for ORDER_LOCATION_LIST
 	 */
 	protected static ArrayList<String> ORDER_LOCATION_LIST = null;
 
 	protected static final String XML_TAG_ORDER_LOCATION_LIST = "OrderLocationList";
 	protected static final String XML_TAG_ORDER_LOCATION = "OrderLocation";
 
 	public static ArrayList<String> getOrderLocationList() {
 		if (ORDER_LOCATION_LIST == null) {
			String xmlPath = CBSettings.getStringValue(CB_SETTINGS_SOURCE_DIR_SETTINGS)
						+ "/"
						+ CBSettings.getStringValue(CB_SETTINGS_ORDER_LOCATIONS_FILE);

 			return loadOrderLocationList(xmlPath);
 		}
 
 		return ORDER_LOCATION_LIST;
 	}
 
 	public static ArrayList<String> loadOrderLocationList(String xmlPath) {
 		if (xmlPath == null)
 			return null;
 
 		CBXmlParser parser = new CBXmlParser(xmlPath);
 		parser.setCallback(new CBXmlParser.Callback() {
 
 			public void onTagWithValueDetected(String tag, String value,
 					XmlPullParser parser) {
 				if (tag.equalsIgnoreCase(XML_TAG_ORDER_LOCATION)) {
 					if (ORDER_LOCATION_LIST != null) {
 						ORDER_LOCATION_LIST.add(value);
 					}
 				}
 			}
 
 			public void onStartDocument(XmlPullParser parser) {
 				ORDER_LOCATION_LIST = new ArrayList<String>();
 			}
 
 			public void onEndDocument(XmlPullParser parser) {
 
 			}
 		});
 
 		parser.parse();
 
 		return ORDER_LOCATION_LIST;
 	}
 
 	public static boolean saveOrderLocationList(String xmlPath) {
 		if (xmlPath == null)
 			return false;
 
 		CBXmlWriter writer = new CBXmlWriter(xmlPath);
 		if (writer.open() == false)
 			return false;
 
 		if (writer.writeXmlHeader() == false)
 			return false;
 
 		writer.writeStartTag(XML_TAG_ORDER_LOCATION_LIST, 0);
 
 		for (int i = 0; i < ORDER_LOCATION_LIST.size(); ++i) {
 			String tag = ORDER_LOCATION_LIST.get(i);
 			writer.writeOneLineTags(XML_TAG_ORDER_LOCATION, null, tag, 1);
 		}
 
 		writer.writeEndTag(XML_TAG_ORDER_LOCATION_LIST, 0);
 
 		writer.close();
 		return true;
 	}
 
 	public static void setStringValue(final String name, String value) {
 		if (name == null)
 			return;
 
 		SETTINGS_MAP.put(name, value);
 	}
 
 	public static String getStringValue(final String name) {
 		String res = SETTINGS_MAP.get(name);
 
 		return (res == null? new String() : res.toString());
 	}
 
 	public static void setIntValue(final String name, int value) {
 		setStringValue(name, String.valueOf(value));
 	}
 
 	public static int getIntValue(final String name) {
 		String str = getStringValue(name);
 
 		return (str.length() > 0 ? Integer.valueOf(str) : 0);
 	}
 
 	public static void setFloatValue(final String name, float value) {
 		setStringValue(name, String.valueOf(value));
 	}
 
 	public static float getFloatValue(final String name) {
 		String str = getStringValue(name);
 
 		return (str.length() > 0 ? Float.valueOf(str) : 0.0f);
 	}
 
 	public static void setBooleanValue(final String name, boolean value) {
 		setStringValue(name, (value == true ? "1" : "0"));
 	}
 
 	public static boolean getBooleanValue(final String name) {
 		String str = getStringValue(name);
 		if (str.length() > 0) {
 			if (str.equalsIgnoreCase("true")
 					|| str.equalsIgnoreCase("1")
 					|| str.equalsIgnoreCase("y")
 					|| str.equalsIgnoreCase("yes"))
 				return true;
 		}
 
 		return false;
 	}
 
 }
