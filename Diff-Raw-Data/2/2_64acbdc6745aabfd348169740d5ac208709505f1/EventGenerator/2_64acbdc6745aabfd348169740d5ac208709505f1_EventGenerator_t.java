 /******************************************************
  * Copyright (C) 2012 Anton Pirogov                   *
  * Licensed under the GNU GENERAL PUBLIC LICENSE      *
  * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
  ******************************************************/
 package shoptool;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 /**
  *
  * @author Anton Pirogov
  */
 public class EventGenerator {
 	// From SimpleServer events implementation
 	private static String escape(String str, String chars) {
 		String ret = str;
 		for (int i = 0; i < chars.length(); i++) {
 			String c = chars.substring(i, i + 1);
 			ret = ret.replaceAll(c, "\\\\" + c);
 		}
 		return ret;
 	}
 
 	private static String fromHash(HashMap<String, String> val) {
 		String s = "{";
 
 		if (val.size() > 0) {
 			for (String key : val.keySet()) {
 				String t = val.get(key);
 				key = escape(key, ",:");
 				t = escape(t, ",:");
 				s += key + ":" + t + ",";
 			}
 			s = s.substring(0, s.length() - 1);
 		}
 
 		s += "}";
 		return s;
 	}
 
 	private static String fromArray(ArrayList<String> val) {
 		String s = "[";
 		while (val.size() != 0) {
 			String t = val.remove(0);
 			t = escape(t, ",");
 			s += t;
 
 			if (val.size() != 0) {
 				s += ",";
 			}
 		}
 		s += "]";
 		return s;
 	}
 
 	// Generation funcs
 
 	public static String generateShops(LinkedList<ShopInterface> shops,
 			boolean withstatic) {
 		String output = "";
 
 		// Load static NPC logic code for output
 		if (withstatic) {
 			InputStream input = Util.class
 					.getResourceAsStream("static.xml");
 			BufferedReader in = new BufferedReader(new InputStreamReader(input));
 			String line = null;
 			try {
 				while ((line = in.readLine()) != null)
 					output += line + "\n";
 			} catch (IOException e) {
 				System.out.println("Error while reading!");
 				return null;
 			}
 		}
 
 		// Now generate
 		output += "<!-- Generated logic: put into your <config> section too -->\n";
 
    output += "<!-- The shop areas: put into your <dimesion> section -->\n";
 		output += generateAreas(shops);
 		output += "\n";
 		output += generateAreaList(shops);
 		output += generateAreaBots(shops);
 		output += generateBotCoords(shops);
 		output += "\n";
 		output += generateListBuy(shops);
 		output += generateListSell(shops);
 		output += generateListStock(shops);
 		output += generateStockConf(shops);
 
 		// return string for output
 		return output;
 	}
 
 	// ----
 
 	private static String generateAreas(LinkedList<ShopInterface> shops) {
 		String open = "\n    <dimension name=\"Earth\">\n";
 		String close = "    </dimension>\n";
 
 		String content = "";
 		for (ShopInterface s : shops) {
 			String shopname = s.name();
 			String startcoord = s.startCoord();
 			String endcoord = s.endCoord();
 
 			content += "        <area name=\"" + shopname;
 			content += "\" start=\"" + startcoord + "\" end=\"" + endcoord
 					+ "\" ";
 			content += "event=\"storeArea\" />\n";
 		}
 
 		return open + content + close;
 	}
 
 	private static String generateAreaList(LinkedList<ShopInterface> shops) {
 		final String ATTNAME = "store_areas";
 		String open = "    <event name=\"" + ATTNAME + "\" value=\"";
 		String close = "\" />\n";
 
 		ArrayList<String> arr = new ArrayList<String>();
 		for (ShopInterface s : shops) {
 			arr.add(s.name());
 		}
 
 		return open + fromArray(arr) + close;
 	}
 
 	private static String generateAreaBots(LinkedList<ShopInterface> shops) {
 		final String ATTNAME = "store_areaBots";
 		String open = "    <event name=\"" + ATTNAME + "\" value=\"";
 		String close = "\" />\n";
 
 		HashMap<String, String> hash = new HashMap<String, String>();
 		for (ShopInterface s : shops) {
 			String shopname = s.name();
 			String botname = s.vendorName();
 			hash.put(shopname, botname);
 		}
 
 		return open + fromHash(hash) + close;
 	}
 
 	private static String generateBotCoords(LinkedList<ShopInterface> shops) {
 		final String ATTNAME = "store_botCoords";
 		String open = "    <event name=\"" + ATTNAME + "\" value=\"";
 		String close = "\" />\n";
 
 		HashMap<String, String> hash = new HashMap<String, String>();
 		for (ShopInterface s : shops) {
 			String botname = s.vendorName();
 			String coordinate = "["+s.botCoord()+"]";
 			hash.put(botname, coordinate);
 		}
 
 		return open + fromHash(hash) + close;
 	}
 
 	private static String generateListBuy(LinkedList<ShopInterface> shops) {
 		final String ATTNAME = "store_listBuy";
 		String open = "    <event name=\"" + ATTNAME + "\" value=\"";
 		String close = "\" />\n";
 
 		HashMap<String, String> hash = new HashMap<String, String>();
 		for (ShopInterface s : shops) {
 			HashMap<String, String> entry = new HashMap<String, String>();
 			for (PriceListItemInterface i : s.pricelist().items()) {
 				if (i.isBuy()) {
 					entry.put(String.valueOf(i.id()), String.valueOf(i.priceBuy()));
 				}
 			}
 			hash.put(s.name(), fromHash(entry));
 		}
 
 		return open + fromHash(hash) + close;
 	}
 
 	private static String generateListSell(LinkedList<ShopInterface> shops) {
 		final String ATTNAME = "store_listSell";
 		String open = "    <event name=\"" + ATTNAME + "\" value=\"";
 		String close = "\" />\n";
 
 		HashMap<String, String> hash = new HashMap<String, String>();
 		for (ShopInterface s : shops) {
 			HashMap<String, String> entry = new HashMap<String, String>();
 			for (PriceListItemInterface i : s.pricelist().items()) {
 				if (i.isSell()) {
 					entry.put(String.valueOf(i.id()), String.valueOf(i.priceSell()));
 				}
 			}
 			hash.put(s.name(), fromHash(entry));
 		}
 
 		return open + fromHash(hash) + close;
 	}
 
 	private static String generateListStock(LinkedList<ShopInterface> shops) {
 		final String ATTNAME = "store_listStock";
 		String open = "    <event name=\"" + ATTNAME + "\" value=\"";
 		String close = "\" />\n";
 
 		HashMap<String, String> hash = new HashMap<String, String>();
 		for (ShopInterface s : shops) {
 			HashMap<String, String> entry = new HashMap<String, String>();
 			for (PriceListItemInterface i : s.pricelist().items()) {
 				if (!(i.isBuy() || i.isSell())) {
 					continue;
 				}
 				entry.put(String.valueOf(i.id()),
 						  String.valueOf(i.normalStock()));
 			}
 			hash.put(s.name(), fromHash(entry));
 		}
 
 		return open + fromHash(hash) + close;
 	}
 
 	private static String generateStockConf(LinkedList<ShopInterface> shops) {
 		final String ATTNAME = "store_stockConf";
 		String open = "    <event name=\"" + ATTNAME + "\" value=\"";
 		String close = "\" />\n";
 
 		HashMap<String, String> hash = new HashMap<String, String>();
 		for (ShopInterface s : shops) {
 			HashMap<String, String> entry = new HashMap<String, String>();
 			for (PriceListItemInterface i : s.pricelist().items()) {
 				if (!(i.isBuy() || i.isSell())) {
 					continue;
 				}
 				ArrayList<String> arr = new ArrayList<String>();
 				arr.add(String.valueOf(i.normalStock()));
 				arr.add(String.valueOf(i.maxStock()));
 				arr.add(String.valueOf(i.stockUpdateTime() * 1000)); // convert
 																		// to ms
 				entry.put(String.valueOf(i.id()), fromArray(arr));
 			}
 			hash.put(s.name(), fromHash(entry));
 		}
 
 		return open + fromHash(hash) + close;
 	}
 }
