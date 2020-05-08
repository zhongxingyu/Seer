 /**
  * 
  */
 package ltg.hg;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import ltg.commons.SimpleRESTClient;
 import ltg.commons.ltg_event_handler.LTGEvent;
 import ltg.commons.ltg_event_handler.SingleChatLTGEventHandler;
 import ltg.commons.ltg_event_handler.SingleChatLTGEventListener;
 import ltg.hg.model.HungerGamesModel;
 
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.JsonNodeFactory;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.MongoClient;
 
 /**
  * @author gugo
  *
  */
 public class HungerGamesMasterBot implements Observer {
 	// Software components
 	private SimpleRESTClient src = new SimpleRESTClient();
 	private SingleChatLTGEventHandler eh;
 	private DB db = null;
 
 	// Model data
 	private String run_id = null;
 	private HungerGamesModel hg = null;
 
 	
 	public HungerGamesMasterBot(String usernameAndPass, String groupChatID, String mongoDBId, String run_id) {
 
 		// ---------------------------------------
 		// Init event handler and connect to Mongo
 		// ---------------------------------------
 		eh =  new SingleChatLTGEventHandler(usernameAndPass+"@ltg.evl.uic.edu", usernameAndPass, groupChatID+"@conference.ltg.evl.uic.edu");
 		try {
 			db = new MongoClient("localhost").getDB(mongoDBId);
 		} catch (UnknownHostException e) {
 			System.err.println("Impossible to connect to MongoDB, terminating...");
 			System.exit(0);
 		}
 
 
 		// ------------------------------------------------------
 		// Fetch the roster, the configuration and init the model
 		// ------------------------------------------------------
 		this.run_id = run_id;
 		initializeModelAndState();
 
 
 		// ----------------------------
 		//Register XMPP event listeners
 		// ----------------------------
 		eh.registerHandler("rfid_update", new SingleChatLTGEventListener() {
 			public void processEvent(LTGEvent e) {
 				hg.updateTagLocation(
 						e.getPayload().get("id").textValue(), 
 						e.getPayload().get("departure").textValue(), 
 						e.getPayload().get("arrival").textValue()
 						);
 			}
 		});
 
 		eh.registerHandler("start_bout", new SingleChatLTGEventListener() {
 			public void processEvent(LTGEvent e) {
 				hg.setCurrentState("foraging");
 				saveState();
 			}
 		});
 
 		eh.registerHandler("stop_bout", new SingleChatLTGEventListener() {
 			public void processEvent(LTGEvent e) {
 				hg.setCurrentState("completed");
 				saveState();
 			}
 		});
 		
 		
 		eh.registerHandler("reset_bout", new SingleChatLTGEventListener() {
 			public void processEvent(LTGEvent e) {
 				resetHGModel();
 				hg.setFullState(
 						e.getPayload().get("habitat_configuration_id").textValue(),
 						e.getPayload().get("bout_id").textValue(),
 						"ready"
 						);
 				saveState();
 			}
 		});
 
 
 		// -------------------------
 		// Start the event listener
 		// -------------------------
 		System.out.println("Hunger Games Master Bot started...");
 		eh.runSynchronously();
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void update(Observable o, Object arg) {
 		saveStatsInDB(hg.getStats());
 		sendNotifications((Map<String, List<String>>) arg);
 	}
 
 
 	private void sendNotifications(Map<String, List<String>> notifications) {
 		for (String v: notifications.get("victims"))
 			sendKillTagEvent(v);
 		for (String r: notifications.get("resurrections"))
 			sendResurrectTagEvent(r);
 	}
 
 
 	private void sendKillTagEvent(String v) {
 		ObjectNode payload = JsonNodeFactory.instance.objectNode(); 
 		payload.put("id", v);
 		eh.generateEvent("kill_tag", payload);
 	}
 	
 	
 	private void sendResurrectTagEvent(String r) {
 		ObjectNode payload = JsonNodeFactory.instance.objectNode(); 
 		payload.put("id", r);
 		eh.generateEvent("resurrect_tag", payload);
 	}
 
 
 	private void initializeModelAndState() {
 		resetHGModel();
 		loadState();
 	}
 
 
 	private void resetHGModel() {
 		if (hg!=null) {
 			hg.clean();
 			hg.deleteObservers();
 		}
 		ArrayNode roster = null;
 		BasicDBObject patchesConfiguration = null;
 		try {
 			roster = (ArrayNode) src.get("http://ltg.evl.uic.edu:9000/runs/"+run_id).get("data").get("roster");
 			patchesConfiguration = (BasicDBObject) db.getCollection("configuration").findOne(new BasicDBObject("run_id", run_id));
 			if (roster.size()==0 || patchesConfiguration==null)
 				throw new IOException();
 		} catch (Exception e) {
 			System.err.println("Impossible to fetch roster and/or configuration, terminating...");
 			System.exit(0);
 		}
 		hg = new HungerGamesModel(roster, patchesConfiguration);
 		hg.addObserver(this);
 	}
 
 	private void loadState() {
 		BasicDBObject state = (BasicDBObject) db.getCollection("state").findOne(new BasicDBObject("run_id", run_id)).get("state");
 		hg.setFullState(
 				state.getString("current_habitat_configuration"), 
 				state.getString("current_bout_id"), 
 				state.getString("current_state")
 				);
 	}
 
 	private void saveState() {
 		BasicDBObject tmp_state = new BasicDBObject()
 		.append("current_habitat_configuration", hg.getCurrentHabitatConfiguration())
 		.append("current_bout_id", hg.getCurrentBoutId())
 		.append("current_state", hg.getCurrentState());
 		db.getCollection("state").update(new BasicDBObject("run_id", run_id), 
 				new BasicDBObject("run_id", run_id).append("state", tmp_state) );
 	}
 
 	private void saveStatsInDB(BasicDBObject stats) {
 		stats.append("run_id", run_id)
 			.append("habitat_configuration", hg.getCurrentHabitatConfiguration())
 			.append("bout_id", hg.getCurrentBoutId());
 		BasicDBObject query = new BasicDBObject()
 				.append("run_id", run_id)
 				.append("habitat_configuration", hg.getCurrentHabitatConfiguration())
 				.append("bout_id", hg.getCurrentBoutId());
 		db.getCollection("statistics").update(query, stats, true, false);
 	}
 
 
 
 
 	/**
 	 * MAIN Parses CLI arguments and launches an instance of the master bot
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if (args.length != 4 || 
 				args[0]==null || args[0].isEmpty() || 
 				args[1]==null || args[1].isEmpty() || 
 				args[2]==null || args[2].isEmpty() ||
 				args[3]==null || args[3].isEmpty()) {
 			System.out.println("Need to specify the username/password (eg. hg-bots#master), "
 					+ "chatroom ID (eg. hg-test), "
 					+ "MongoDB (e.g. hunger-games-fall-13) "
 					+ "and run_id (e.g period-1). Terminating...");
 			System.exit(0);
 		}
 		new HungerGamesMasterBot(args[0], args[1], args[2], args[3]);      
 	}
 
 }
