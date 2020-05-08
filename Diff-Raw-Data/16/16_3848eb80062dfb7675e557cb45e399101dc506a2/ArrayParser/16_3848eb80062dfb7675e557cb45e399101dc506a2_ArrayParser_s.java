 package kea.kme.pullpit.server.podio;
 
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import kea.kme.pullpit.server.podio.enums.App;
 
 import com.podio.item.FieldValuesView;
 import com.podio.item.ItemBadge;
 import com.podio.item.ItemsResponse;
 
 public class ArrayParser {
 	private static ArrayParser arrayParser;
 	private static final Logger log = Logger.getLogger(ArrayParser.class.getName());
 
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
 			Timestamp time = null;
 			SimpleDateFormat formatter, FORMATTER;
 			formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 			String dateString = ib.getCurrentRevision().getCreatedOn().toString();
 			Date date;
 			try {
 				date = formatter.parse(dateString);
 			FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 			time = Timestamp.valueOf(FORMATTER.format(date));
 			} catch (ParseException e) {
 				log.warning("Could not parse! " + e.getMessage());
 			}
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
			bands.add(new PodioBand(itemID, name, country, promoID, time, new PodioAgent(itemID, agentID)));
 		}
 		return bands;
 	}
 
 	public ArrayList<PodioVenue> parseVenues(ItemsResponse ir) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ArrayList<PodioShow> parseShows(ItemsResponse ir) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
