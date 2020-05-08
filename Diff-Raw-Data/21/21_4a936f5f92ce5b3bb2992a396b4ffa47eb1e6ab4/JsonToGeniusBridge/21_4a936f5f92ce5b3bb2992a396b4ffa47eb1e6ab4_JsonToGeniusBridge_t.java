 package geniuswebsocket;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import negotiator.AgentID;
 import negotiator.Bid;
 import negotiator.Domain;
 import negotiator.actions.Accept;
 import negotiator.actions.Action;
 import negotiator.actions.BidAction;
 import negotiator.actions.EndNegotiation;
 import negotiator.actions.Offer;
 import negotiator.actions.Reject;
 import negotiator.exceptions.NegotiatorException;
 import negotiator.gui.nlp.GrammarToGeniusBridge;
 import negotiator.issue.Value;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Utility functions that bridge between JSON objects 
  * to the corresponding GENIUS Action.
  * 
  * @see negotiator.gui.nlp.GrammarToGeniusBridge
  * @author Erel Segal haLevi
  * @since 2013-02-18
  */
 public class JsonToGeniusBridge {
 	
 	private JsonToGeniusBridge() { /* Prevent initialization */ }
 	
 	
 	/**
 	 * @param json an object such as {"issue1": "value1", "issue2": "value2", ...}
 	 * @param domain
 	 * @param thisAgent
 	 * @param bidTime
 	 * @param partnerLatestBidAction
 	 * @param ourLatestBidAction
 	 * @return a hash-map from issues to values (must be a HashMap because of dependencies in genius.Bid...)
 	 */
 	public static HashMap<Integer, Value> jsonObjectToGeniusBidValues(JSONObject json, Domain domain) throws NegotiatorException, JSONException {
 		HashMap<Integer, Value> bidValues = new HashMap<Integer, Value>();  // collect specific-issue actions
 		for (Iterator<?> iIssue = json.keys(); iIssue.hasNext();) {
 			String issueName = (String)iIssue.next();
 			String valueName = json.getString(issueName);
 			GrammarToGeniusBridge.putIssueValuePairInBid(domain, issueName, valueName, bidValues);
 		}
 		return bidValues;
 	}
 	
 	/**
 	 * Go over all objects in the array, and merge fields with equal names.
 	 * @param objects
 	 * @return the merged object.
 	 * @throws JSONException 
 	 */
 	public static JSONObject deepMergeJsonObjects(JSONObject[] objects) throws JSONException {
 		JSONObject whole = new JSONObject();
 		for (JSONObject part: objects) {
 			for (@SuppressWarnings("unchecked")
 			Iterator<String> iKey = (Iterator<String>)part.keys(); iKey.hasNext();) {
 				String key = (String)iKey.next();
 				Object value = part.get(key);
 				
 				if (!whole.has(key)) {
 					// new value for "key":
 					whole.put(key, value);
 				} else {
 					// existing value for "key" - deep merge:
 					
 				}
 			}
 		}
 		return whole;
 	}
 	
 	
 	public static void keysToLowerCase(JSONObject json) throws JSONException {
 		for (@SuppressWarnings("unchecked")
 		Iterator<String> iKey = (Iterator<String>)json.keys(); iKey.hasNext();) {
 			String key = iKey.next();
 			String keyLowerCase = key.toLowerCase();
 			if (key.equals(keyLowerCase))
 				continue;
 			Object value = json.get(key);
 			json.put(keyLowerCase, value);
 			json.remove(key);
 		}
 	}
 	
 	public static void keysToTitleCase(JSONObject json) throws JSONException {
 		for (@SuppressWarnings("unchecked")
 		Iterator<String> iKey = (Iterator<String>)json.keys(); iKey.hasNext();) {
 			String key = iKey.next();
 			String keyTitleCase = key.substring(0,1).toUpperCase() + key.substring(1).toLowerCase();
 			if (key.equals(keyTitleCase))
 				continue;
 			Object value = json.get(key);
 			json.put(keyTitleCase, value);
 			json.remove(key);
 		}
 	}
 
 	
 	/**
 	 * @see negotiator.gui.nlp.GrammarToGeniusBridge#semanticStringToGeniusAction
 	 */
 	public static List<Action> jsonObjectToGeniusAction(JSONObject json, Domain domain, AgentID thisAgent, Integer bidTime, BidAction opponentLatestBidAction, BidAction speakerLatestBidAction) throws NegotiatorException, JSONException {
 		List<Action> actions = new ArrayList<Action>();
 		if (json==null) return actions; // no actions
 
 		Bid opponentLatestBid = opponentLatestBidAction==null? null: opponentLatestBidAction.getBid();
 		Bid speakerLatestBid = speakerLatestBidAction==null? null: speakerLatestBidAction.getBid();
 		keysToTitleCase(json);
 		if (json.has("Offer")) {
 			JSONObject jsonBid = json.getJSONObject("Offer");
 			HashMap<Integer, Value> demandedBidValues = jsonObjectToGeniusBidValues(jsonBid, domain);
 			if (!demandedBidValues.isEmpty()) {
 				Bid theBid = new Bid(domain, demandedBidValues);
 				if (bidTime!=null)
 					theBid.setTime(bidTime);
 				actions.add(new Offer(thisAgent, theBid));
 			}
 		}
 		if (json.has("accept")) {
 			if (opponentLatestBid==null) 
 				throw new NegotiatorException("What do you accept? I didn't offer anything");
 			actions.add(new Accept(thisAgent, opponentLatestBidAction));
 		}
 		if (json.has("reject")) {
 			if (opponentLatestBid==null) 
 				throw new NegotiatorException("What do you reject? I didn't offer anything");
 			actions.add(new Reject(thisAgent, opponentLatestBidAction));
 		}
 		if (json.has("quit")) {
 			actions.add(new EndNegotiation(thisAgent));
 		}
 		if (json.has("insist")) {
 			if (speakerLatestBid==null) 
 				throw new NegotiatorException("What do you insist? You didn't offer anything");
 			String issueName = json.getString("insist");
 			if (issueName.equals("previous")) {
 				actions.add(new Offer(thisAgent, speakerLatestBid));
 				//copyValuesForNonexistingKeys(agreedBidValues, speakerLatestBid.getValues());
 			} else {
 				//copyValueForIssue(domain, speakerLatestBid, issueName, demandedBidValues);
 			}
 		}
 		/*if (json.has("PartialAgree")) {
 			if (opponentLatestBid==null) 
 				throw new IllegalArgumentException("partial agreement without a base opponent action");
 			String issueName = json.getString("PartialAgree");
 			if (issueName.equals("general")) {
 				copyValuesForNonexistingKeys(agreedBidValues, opponentLatestBid.getValues());
 			} else {
 				copyValueForIssue(domain, opponentLatestBid, issueName, demandedBidValues);
 			}
 		}
 		if (json.has("Append")) {
 			if (speakerLatestBid==null) 
 				throw new IllegalArgumentException("append without a base speaker action");
 			copyValuesForNonexistingKeys(agreedBidValues,speakerLatestBid.getValues());
 		}
 		*/
 		return actions;
 	}
 
 	public static List<Action> jsonObjectToGeniusAction(JSONObject json, Domain domain, AgentID thisAgent) throws NegotiatorException, JSONException {
 		return jsonObjectToGeniusAction(json, domain, thisAgent, null, null, null);
 	}
 	
 	public static JSONObject geniusBidToJsonObject(Bid bid, Domain domain) throws JSONException {
 		JSONObject json = new JSONObject();
 		for (Map.Entry<Integer, Value> entry: bid.getValues().entrySet()) {
 			String issueName = domain.getObjective(entry.getKey()).getName();
 			json.put(issueName, entry.getValue());
 		}
 		return json;
 	}
 	
 	public static JSONObject geniusActionToJsonObject(Action geniusAction, Domain domain) throws JSONException {
 		JSONObject json = new JSONObject();
 		if (geniusAction instanceof BidAction) {
 			JSONObject jsonBid = geniusBidToJsonObject(((BidAction)geniusAction).getBid(), domain);
 			json.put("Offer", jsonBid);
 		} else if (geniusAction instanceof Reject) {
 			JSONObject jsonBid = geniusBidToJsonObject(((Reject)geniusAction).getAcceptedOrRejectedAction().getBid(), domain);
 			json.put("Reject", jsonBid);
 		} else if (geniusAction instanceof Accept) {
			JSONObject jsonBid = geniusBidToJsonObject(((Accept)geniusAction).getAcceptedOrRejectedAction().getBid(), domain);
 			json.put("Accept", jsonBid);
 		} else if (geniusAction instanceof EndNegotiation) {
 			json.put("Quit", true);
 		}
 		return json;
 	}
 	
 	/**
 	 * demo program
 	 */
 	public static void main(String[] args) throws JSONException {
 	}
 }
