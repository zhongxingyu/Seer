 package kea.kme.pullpit.server.podio;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import kea.kme.pullpit.server.podio.enums.App;
 
 import com.podio.item.FieldValuesView;
 import com.podio.item.ItemBadge;
 import com.podio.item.ItemsResponse;
 
 public class ArrayParser {
 	private static ArrayParser arrayParser;
 
 	private ArrayParser() {
 	}
 
 	public static ArrayParser getInstance() {
 		if (arrayParser == null)
 			arrayParser = new ArrayParser();
 		return arrayParser;
 	}
 
 	public ArrayList<?> parseArray(App app, ItemsResponse ir) {
 		switch (app.appName) {
 		case "Bands" :
 			return parseBands(ir);
 		}
 		return null;
 	}
 	
 	protected ArrayList<PodioBand> parseBands(ItemsResponse ir) {
 		ArrayList<PodioBand> bands = new ArrayList<PodioBand>();
 		List<ItemBadge> items = ir.getItems();
 		for (ItemBadge ib : items) {
 			int itemID = ib.getId();
 			String name = null;
 			String country = null;
 			int agentID = 0;
 			int promoID = 0;
			Timestamp date = parseTime(ib.getCurrentRevision().getCreatedOn().toString());
 			for (FieldValuesView fvw : ib.getFields()) {
 				switch (fvw.getLabel()) {
 				case "Navn":
 					name = (String) fvw.getValues().get(0).get("value");
 					break;
 				case "Land":
 					country = (String) fvw.getValues().get(0).get("value");
 					break;
 				case "Agent":
 					@SuppressWarnings("unchecked")
 					HashMap<String, Object> agentMap = (HashMap<String, Object>) fvw
 							.getValues().get(0).get("value");
 					agentID = (Integer) agentMap.get("profile_id");
 					break;
 				case "SBP-Promoter":
 					@SuppressWarnings("unchecked")
 					HashMap<String, Object> promoMap = (HashMap<String, Object>) fvw
 							.getValues().get(0).get("value");
 					promoID = (Integer) promoMap.get("user_id");
 				}
 			}
			bands.add(new PodioBand(itemID, name, country, promoID, date, agentID));
 		}
 		return bands;
 	}
	
	private Timestamp parseTime(String date) {
		return Timestamp.valueOf(date);
	}
 }
