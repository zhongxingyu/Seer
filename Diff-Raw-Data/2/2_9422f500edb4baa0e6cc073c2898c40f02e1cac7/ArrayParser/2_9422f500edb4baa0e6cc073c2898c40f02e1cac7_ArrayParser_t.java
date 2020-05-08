 package kea.kme.pullpit.server.podio;
 
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
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
 
 	@SuppressWarnings("unchecked")
 	protected ArrayList<PodioBand> parseBands(ItemsResponse ir) {
 		ArrayList<PodioBand> bands = new ArrayList<PodioBand>();
 		List<ItemBadge> items = ir.getItems();
 		for (ItemBadge ib : items) {
 			int itemID = ib.getId();
 			String bandName = null;
 			String country = null;
 			int promoID = 0;
 			PodioContact[] contacts = null;
 			PodioAgent[] agents = null;
 			Date time = getTime(ib.getCurrentRevision().getCreatedOn().toString());
 			for (FieldValuesView fvw : ib.getFields()) {
 				switch (fvw.getLabel()) {
 				case "Navn":
 					bandName = (String) fvw.getValues().get(0).get("value");
 					break;
 				case "Land":
 					country = (String) fvw.getValues().get(0).get("value");
 					break;
 				case "Agent":
 					int noOfAgents = fvw.getValues().size();
 					agents = new PodioAgent[noOfAgents];
 					contacts = new PodioContact[noOfAgents];
 					for (int i = 0; i < noOfAgents; i++) {
 						HashMap<String, Object> agentMap = (HashMap<String, Object>) fvw
 							.getValues().get(i).get("value");
 						int agentID = 0;
 						String name = "";
 						String phone = "";
 						String email = "";
 						for (String s : agentMap.keySet()) {
 							switch (s) {
 							case "profile_id":
 								agentID = (Integer) agentMap.get("profile_id");
 								break;
 							case "name":
 								name = (String) agentMap.get("name");
 								break;
 							case "phone":
 								phone = (String) ((ArrayList<Object>)agentMap.get("phone")).get(0);
 								break;
 							case "mail":
 								email = (String) ((ArrayList<Object>)agentMap.get("mail")).get(0);
 							}
 						}
 					agents[i] = new PodioAgent(itemID, agentID);
 					contacts[i] = new PodioContact(agentID, email, phone, name);
 					}
 					break;
 				case "SBP-Promoter":
 					HashMap<String, Object> promoMap = (HashMap<String, Object>) fvw
 							.getValues().get(0).get("value");
 					promoID = (Integer) promoMap.get("user_id");
 				}
 			}
 			bands.add(new PodioBand(itemID, bandName, country, promoID, time));
 			if (agents != null) {
 				StaticImporter.getInstance().addAgents(agents);
 				StaticImporter.getInstance().addContacts(contacts);
 			}
 		}
 		return bands;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected ArrayList<PodioVenue> parseVenues(ItemsResponse ir) {
 		ArrayList<PodioVenue> venues = new ArrayList<PodioVenue>();
 		List<ItemBadge> items = ir.getItems();
 		for (ItemBadge ib : items) {
 			int itemID = ib.getId();
 			String venueName = null;
 			int capacity = 0;
 			PodioContact[] contacts = null;
 			PodioBooker[] bookers = null;
 			Date time = getTime(ib.getCurrentRevision().getCreatedOn().toString());
 			for (FieldValuesView fvw : ib.getFields()) {
 				switch (fvw.getLabel()) {
 				case "Navn":
 					venueName = (String) fvw.getValues().get(0).get("value");
 					break;
 				case "Kapacitet":
 					capacity = Double.valueOf((String)fvw.getValues().get(0).get("value")).intValue();
 					break;
 				case "Kontakt":
 					int noOfBookers = fvw.getValues().size();
 					bookers = new PodioBooker[noOfBookers];
 					contacts = new PodioContact[noOfBookers];
 					for (int i = 0; i < noOfBookers; i++) {
 					HashMap<String, Object> agentMap = (HashMap<String, Object>) fvw
 							.getValues().get(i).get("value");
 					int bookerID = 0;
 					String name = "";
 					String phone = "";
 					String email = "";
 					for (String s : agentMap.keySet()) {
 						switch (s) {
 						case "profile_id":
 							bookerID = (Integer) agentMap.get("profile_id");
 							break;
 						case "name":
 							name = (String) agentMap.get("name");
 							break;
 						case "phone":
 							phone = (String) ((ArrayList<Object>)agentMap.get("phone")).get(0);
 							break;
 						case "mail":
 							email = (String) ((ArrayList<Object>)agentMap.get("mail")).get(0);
 						}
 					}
 					bookers[i] = new PodioBooker(itemID, bookerID);
					contacts[i] = new PodioContact(bookerID, email, phone, name);
 					}
 					break;
 				}
 			}
 			venues.add(new PodioVenue(itemID, venueName, capacity, time));
 			if (bookers != null) {
 				StaticImporter.getInstance().addBookers(bookers);
 				StaticImporter.getInstance().addContacts(contacts);
 			}
 		}
 		log.info("Venue parsing yielded " + StaticImporter.getInstance().getContacts().length + " contacts");
 		return venues;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected ArrayList<PodioShow> parseShows(ItemsResponse ir) {
 		ArrayList<PodioShow> shows = new ArrayList<PodioShow>();
 		List<ItemBadge> items = ir.getItems();
 		for (ItemBadge ib : items) {
 			int showID = ib.getId();
 			int bandID = 0;
 			Date date = null;
 			int state = 0;
 			PodioShowVenue[] showVenues = null;
 			String comments = null;
 			int promoID = 0;
 			Date lastEdit = getTime(ib.getCurrentRevision().getCreatedOn().toString());
 			for (FieldValuesView fvw : ib.getFields()) {
 				switch (fvw.getLabel()) {
 				case "Band":
 					HashMap<String, Object> map = (HashMap<String, Object>)fvw.getValues().get(0).get("value");
 					bandID = (Integer) map.get("item_id");
 					break;
 				case "Datovælger":
 					String dateString = (String)fvw.getValues().get(0).get("start_date");
 					date = getDate(dateString);
 					break;
 				case "SBP-promoter":
 					HashMap<String, Object> promoMap = (HashMap<String, Object>) fvw
 					.getValues().get(0).get("value");
 					promoID = (Integer) promoMap.get("user_id");
 					break;
 				case "Status":
 					state = (Integer) ((HashMap<String, Object>)fvw.getValues().get(0).get("value")).get("id");
 					break;
 				case "Deal-memo":
 					comments = (String)fvw.getValues().get(0).get("value");
 					break;
 				case "Venue":
 					int noOfVenues = fvw.getValues().size();
 					showVenues = new PodioShowVenue[noOfVenues];
 					for (int i = 0; i < noOfVenues; i++) {
 					HashMap<String, Object> showVenueMap = (HashMap<String, Object>) fvw
 							.getValues().get(i).get("value");
 					int venueID = (Integer) showVenueMap.get("item_id");
 					showVenues[i] = new PodioShowVenue(showID, venueID);
 					}
 					break;
 				}
 			}
 			shows.add(new PodioShow(showID, bandID, date, promoID, state, comments, lastEdit));
 			StaticImporter.getInstance().addShowVenue(showVenues);
 		}
 		return shows;
 	}
 	
 	private Date getTime(String timeAsString) {
 		SimpleDateFormat formatter, FORMATTER;
 		formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 		Date date;
 		Timestamp time = null;
 		try {
 			date = formatter.parse(timeAsString);
 		FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 		time = Timestamp.valueOf(FORMATTER.format(date));
 		return time;
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	private Date getDate(String dateAsString) {
 		SimpleDateFormat formatter;
 		formatter = new SimpleDateFormat("yyyy-MM-dd");
 		Date date;
 		try {
 			date = formatter.parse(dateAsString);
 			return date;
 		} catch (ParseException e) {
 			return null;
 		}
 	}
 }
