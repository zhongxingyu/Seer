 package models.wowapi;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Date;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.stream.JsonReader;
 
 import models.raidtracker.Raid;
 import models.raidtracker.RaidBossKills;
 import models.raidtracker.RaidItem;
 import models.raidtracker.RaidMember;
 import models.raidtracker.RaidType;
 import models.raidtracker.RaidPool;
 import models.raidtracker.RaidZones;
 import models.wowapi.character.Avatar;
 import models.wowapi.resources.Icon;
 import models.wowapi.resources.Item;
 import models.wowapi.resources.ItemClass;
 import models.wowapi.resources.ItemQuality;
 import models.wowapi.resources.ItemSlot;
 import models.wowapi.resources.ItemSubClass;
 import play.Logger;
 import play.Play;
 import play.libs.WS;
 import play.libs.XPath;
 import play.libs.WS.HttpResponse;
 import utils.Tools;
 
 public class WoWHead {
 	public static long WEEKLYUPDATE = 604800000;
 	public static long QUATERDAYUPDATE = 21600000;
 	
	public static String ITEMURL = "http://de.wowhead.com/item=";
	
 	public WoWHead() {
 
 	}
 
 //	private void fetchTestItem() {
 //		// TODO Auto-generated method stub
 //		Long id = 78878L;
 //		//id = 52722L;
 //		id = 73435L;
 //		checkItem(id);
 //	}
 //	
 	
 //	public static Item checkItem(Item item) {
 //		Item item = Item.setItem(id);
 //		Boolean update = false;
 //		try {
 //			if (checkUpdate(new Date(), item.lastUpdate, WoWHead.WEEKLYUPDATE)) {
 //				update = true;
 //			}
 //		} catch (NullPointerException e) {
 //			update = true;
 //		}
 //		if (update) {
 //			Logger.info("Fetching Item: " + id);
 //			return fetchItem(id);
 //		}
 //		return item;
 //	}
 	public static Item checkItem(Item item) {
 		Boolean update = false;
 		try {
 			if (checkUpdate(new Date(), item.lastUpdate, WoWHead.WEEKLYUPDATE)) {
 				update = true;
 			}
 		} catch (NullPointerException e) {
 			update = true;
 		}
 		if (update) {
 			Logger.info("Fetching Item: " + (item.name == null ? item.itemId : item.name));
 			return fetchItemByName(item);
 		}
 		return item;
 	}
 	
 	public static Item fetchItemByName(Item checkedItem) {
 		Document xmlDoc = null;
 		if (checkedItem.name == null && checkedItem.itemId != null) {
			xmlDoc = getXMLDocument(ITEMURL,checkedItem.itemId);
 			Node itemInfo = XPath.selectNode("/wowhead/item", xmlDoc);
 			
 			String name = XPath.selectText("name", itemInfo);
 			checkedItem.name = name;
 			checkedItem.save();
 			return fetchItem(checkedItem,itemInfo);
 		}
 		if (checkedItem.name != null && checkedItem.itemId == null) {
 			xmlDoc = getXMLDocument(ITEMURL,checkedItem.name);
 			Node itemInfo = XPath.selectNode("/wowhead/item", xmlDoc);
 			checkedItem.itemId = Long.parseLong(itemInfo.getAttributes().getNamedItem("id").getNodeValue());
 			checkedItem.save();
 			return fetchItem(checkedItem,itemInfo);
 		}
 		
 		return null;
 		
 	}
 	
 	private static Item fetchItem(Item item, Node itemInfo) {
 		
 		Long level = Long.parseLong(XPath.selectText("level", itemInfo));
 		item.level = level;
 		
 		Float gearScore = Float.parseFloat(XPath.selectText("gearScore", itemInfo));
 		item.gearScore = gearScore;
 		
 		String quality = XPath.selectText("quality", itemInfo);
 		Long qualityId = Long.parseLong(XPath.selectText("quality/@id", itemInfo));
 		ItemQuality itemQuality = ItemQuality.setItemQuality(qualityId, quality);
 		item.itemQuality = itemQuality;
 		
 		String itemClass = XPath.selectText("class", itemInfo);
 		Long itemClassId = Long.parseLong(XPath.selectText("class/@id", itemInfo));
 		ItemClass ic = ItemClass.setItemClass(itemClassId, itemClass);
 		item.itemClass = ic;
 		
 		String itemSubClass = XPath.selectText("subclass", itemInfo);
 		Long itemSubClassId = Long.parseLong(XPath.selectText("subclass/@id", itemInfo));
 		ItemSubClass isc = ItemSubClass.setItemSubClass(itemSubClassId, itemSubClass);
 		item.itemSubClass = isc;
 		
 		String icon = XPath.selectText("icon", itemInfo);
 		Long iconId = Long.parseLong(XPath.selectText("icon/@displayId", itemInfo));
 		Icon i = Icon.setIcon(iconId, icon);
 		item.icon = i;
 		
 		String slot = XPath.selectText("inventorySlot", itemInfo);
 		Long slotId = Long.parseLong(XPath.selectText("inventorySlot/@id", itemInfo));
 		ItemSlot itemSlot = ItemSlot.setItemSlot(slotId, slot);
 		item.slot = itemSlot;
 		
 		String tooltip = XPath.selectText("htmlTooltip", itemInfo);
 		item.tooltip = tooltip;
 		
 		String json = "{" + XPath.selectText("json", itemInfo) + "}";
 		item.json = json;
 		
 		String jsonEquip = "{" + XPath.selectText("jsonEquip", itemInfo) + "}";
 		item.jsonEquip = jsonEquip;
 		
 		String link = XPath.selectText("link", itemInfo);
 		item.link = link;
 
 		JsonObject oJson = new JsonParser().parse(json).getAsJsonObject();
 		String memberName = "dps";
 		if (oJson.has(memberName)) {
 			item.dps = oJson.get(memberName).getAsFloat();
 		}
 		memberName = "reqlevel";
 		if (oJson.has(memberName)) {
 			item.reqlevel = oJson.get(memberName).getAsLong();
 		}
 		memberName = "speed";
 		if (oJson.has(memberName)) {
 			item.speed = oJson.get(memberName).getAsFloat();
 		}
 		memberName = "armor";
 		if (oJson.has(memberName)) {
 			item.armor = oJson.get(memberName).getAsFloat();
 		}
 		
 		memberName = "dura";
 		if (oJson.has(memberName)) {
 			item.dura = oJson.get(memberName).getAsFloat();
 		}
 		
 		memberName = "heroic";
 		if (oJson.has(memberName)) {
 			item.heroic = oJson.get(memberName).getAsLong();
 		}
 		memberName = "reqclass";
 		if (oJson.has(memberName)) {
 			item.reqclass = oJson.get(memberName).getAsLong();
 		}
 		
 		if (!jsonEquip.contains("{null}")) {
 			oJson = new JsonParser().parse(jsonEquip).getAsJsonObject();
 
 			memberName = "int";
 			if (oJson.has(memberName)) {
 				item.inte = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "str";
 			if (oJson.has(memberName)) {
 				item.str = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "spi";
 			if (oJson.has(memberName)) {
 				item.spi = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "sta";
 			if (oJson.has(memberName)) {
 				item.sta = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "agi";
 			if (oJson.has(memberName)) {
 				item.agi = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "dmgmax1";
 			if (oJson.has(memberName)) {
 				item.dmgmax1 = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "dmgmin1";
 			if (oJson.has(memberName)) {
 				item.dmgmin1 = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "dmgtype1";
 			if (oJson.has(memberName)) {
 				item.dmgtype1 = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "mledmgmax";
 			if (oJson.has(memberName)) {
 				item.mledmgmax = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "mledmgmin";
 			if (oJson.has(memberName)) {
 				item.mledmgmin = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "mledps";
 			if (oJson.has(memberName)) {
 				item.mledps = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "mlespeed";
 			if (oJson.has(memberName)) {
 				item.mlespeed = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "nsockets";
 			if (oJson.has(memberName)) {
 				item.nsockets = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "socket1";
 			if (oJson.has(memberName)) {
 				item.socket1 = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "socket2";
 			if (oJson.has(memberName)) {
 				item.socket2 = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "socket3";
 			if (oJson.has(memberName)) {
 				item.socket3 = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "socket4";
 			if (oJson.has(memberName)) {
 				item.socket4 = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "socket5";
 			if (oJson.has(memberName)) {
 				item.socket5 = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "socketbonus";
 			if (oJson.has(memberName)) {
 				item.socketbonus = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "buyprice";
 			if (oJson.has(memberName)) {
 				item.buyprice = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "hastertng";
 			if (oJson.has(memberName)) {
 				item.hastertng = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "exprtng";
 			if (oJson.has(memberName)) {
 				item.exprtng = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "sellprice";
 			if (oJson.has(memberName)) {
 				item.sellprice = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "itemset";
 			if (oJson.has(memberName)) {
 				item.itemset = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "mastrtng";
 			if (oJson.has(memberName)) {
 				item.mastrtng = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "dodgertng";
 			if (oJson.has(memberName)) {
 				item.dodgertng = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "hitrtng";
 			if (oJson.has(memberName)) {
 				item.dodgertng = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "parryrtng";
 			if (oJson.has(memberName)) {
 				item.parryrtng = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "critstrkrtng";
 			if (oJson.has(memberName)) {
 				item.critstrkrtng = oJson.get(memberName).getAsFloat();
 			}
 			memberName = "resirtng";
 			if (oJson.has(memberName)) {
 				item.resirtng = oJson.get(memberName).getAsFloat();
 			}
 			
 		}
 
 		String url = "http://eu.battle.net/wow/de/item/" + item.itemId + "/tooltip?";
 		item.armoryTooltipURL = url;
 		HttpResponse hr = WS.url(url).get();
 		
 		if (hr.success()) {
 			item.armoryTooltip = hr.getString();
 		}
 		
 		item.lastUpdate = new Date();
 		item.save();
 		return item;
 	}
 	
 	private static Document getXMLDocument(String url, Long id) {
 		
 		String itemURL = url + id + "&xml";
 		
 		Logger.info("Fetch URL: " + itemURL);
 
 		Document xml = WS.url(itemURL).get().getXml();
 
 		return xml;
 	}
 private static Document getXMLDocument(String url, String name) {
 		
 		String itemURL = null;
 		try {
 			itemURL = url + URLEncoder.encode(name, "UTF-8") + "&xml";
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Logger.info("Fetch URL: " + itemURL);
 
 		Document xml = WS.url(itemURL).get().getXml();
 
 		return xml;
 	}
 	private static boolean checkUpdate(Date currDate, Date lastDate, long intervall) {
 		if ((currDate.getTime() - intervall) > lastDate.getTime()) {
 			return true;
 		}
 		return false;
 	}
 }
