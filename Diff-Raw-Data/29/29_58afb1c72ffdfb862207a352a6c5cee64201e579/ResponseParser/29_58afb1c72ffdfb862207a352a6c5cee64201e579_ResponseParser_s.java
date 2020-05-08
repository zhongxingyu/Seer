 package com.example.solarpanelmanager.api.parsers;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 
 import net.minidev.json.JSONArray;
 import net.minidev.json.JSONObject;
 import net.minidev.json.JSONValue;
 
 import com.example.solarpanelmanager.api.responses.BaseResponse;
 import com.example.solarpanelmanager.api.responses.Event;
 import com.example.solarpanelmanager.api.responses.EventsResponse;
 import com.example.solarpanelmanager.api.responses.HistoryResponse;
 import com.example.solarpanelmanager.api.responses.SnapshotResponse;
 import com.example.solarpanelmanager.api.responses.ViewChargeConstraintsResponse;
 
 public class ResponseParser {
 
 	public static BaseResponse parseBasicResponse(String response) {
 		if (response == "") {
 			return new BaseResponse(null, 500, null);
 		}
 
 		// Call the parse response for the basic map, and then create the proper
 		// response object
 		JSONObject json = (JSONObject) JSONValue.parse(response);
 		return new BaseResponse((String) json.get(MessageKeys.MESSAGE_TYPE),
 				(Integer) json.get(MessageKeys.RESPONSE_CODE), (String) json.get(MessageKeys.RESPONSE_MESSAGE));
 	}
 
 	public static HistoryResponse parseHistoryResponse(String response) {
 		// Call the parse response for the basic map, and then create the proper
 		// response object
 		JSONObject json = (JSONObject) JSONValue.parse(response);
 
 		int result = (Integer) json.get(MessageKeys.RESPONSE_CODE);
 		String message = (String) json.get(MessageKeys.RESPONSE_MESSAGE);
 
 		if (result != 200) {
 			return new HistoryResponse(result, message);
 		}
 
 		JSONArray snapshots = (JSONArray) json.get(MessageKeys.HISTORY_DATA);
 		ArrayList<SnapshotResponse> snapshotResponses = new ArrayList<SnapshotResponse>();
 		// SnapshotResponse[] snapshotResponses = new
 		// SnapshotResponse[snapshots.size()];
 
 		for (int i = 0; i < snapshots.size(); i++) {
 			// snapshotResponses[i] = parseSnapshotResponse(((JSONObject)
 			// snapshots.get(i)).toJSONString());
 			snapshotResponses.add(parseSnapshotResponse(((JSONObject) snapshots.get(i)).toJSONString()));
 		}
 
 		return new HistoryResponse(result, message, snapshotResponses);
 	}
 
 	public static SnapshotResponse parseSnapshotResponse(String response) {
 		// Call the parse response for the basic map, and then create the proper
 		// response object
		JSONObject json = (JSONObject) (JSONValue.parse(response));
 		System.out.println(response);
 		int result = 200;
 		if (json.containsKey(MessageKeys.RESPONSE_CODE)) {
 			result = (Integer) json.get(MessageKeys.RESPONSE_CODE);
 		}
 		String message = (String) json.get(MessageKeys.RESPONSE_MESSAGE);
 
 		if (result != 200) {
 			return new SnapshotResponse(result, message);
 		}
 
 		if (json.containsKey(MessageKeys.RESPONSE_CODE)) {
 			result = (Integer) json.get(MessageKeys.RESPONSE_CODE);
 		}
 
 		long longTimestamp = getLong(json.get(MessageKeys.SNAPSHOT_TIMESTAMP));
 
 		double batteryVoltage = getDouble(json.get(MessageKeys.SNAPSHOT_BATTERY_VOLTAGE));
 		double batteryCurrent = getDouble(json.get(MessageKeys.SNAPSHOT_BATTERY_CURRENT));
 		double PVVoltage = getDouble(json.get(MessageKeys.SNAPSHOT_PANEL_VOLTAGE));
 		double PVCurrent = getDouble(json.get(MessageKeys.SNAPSHOT_PANEL_CURRENT));
 
 		int percent = (Integer) json.get(MessageKeys.SNAPSHOT_BATTERY_PERCENT);
 
 		return new SnapshotResponse(result, message, longTimestamp, percent, batteryVoltage, PVCurrent, PVVoltage,
 				batteryCurrent);
 	}
 
 	private static double getDouble(Object obj) {
 		if (obj instanceof BigDecimal) {
 			BigDecimal dec = (BigDecimal) obj;
 			return dec.doubleValue();
 		} else {
 			return (Double) obj;
 		}
 	}
 
 	public static EventsResponse parseEventsResponse(String response) {
 		JSONObject json = (JSONObject) (JSONValue.parse(response));
 
 		int result = (Integer) json.get(MessageKeys.RESPONSE_CODE);
 		String message = (String) json.get(MessageKeys.RESPONSE_MESSAGE);
 
 		if (result != 200) {
 			return new EventsResponse(result, message);
 		}
 
 		JSONArray eventsArray = (JSONArray) json.get(MessageKeys.EVENTS_DATA);
 		ArrayList<Event> events = new ArrayList<Event>();
 
 		for (int i = 0; i < eventsArray.size(); i++) {
 			events.add(parseEvent((JSONObject) eventsArray.get(i)));
 		}
 
 		return new EventsResponse(result, message, events);
 	}
 
 	private static Event parseEvent(JSONObject json) {
 		String id = (String) json.get(MessageKeys.EVENT_ID);
 		long firstTime = getLong(json.get(MessageKeys.EVENT_FIRST_TIME));
 		long duration = getLong(json.get(MessageKeys.EVENT_DURATION));
 		long interval = getLong(json.get(MessageKeys.EVENT_INTERVAL));
 		return new Event(id, firstTime, duration, interval);
 	}
 
 	private static long getLong(Object obj) {
 		if (obj instanceof Integer) {
 			return (Integer) obj;
 		} else {
 			return (Long) obj;
 		}
 	}
 
 	public static ViewChargeConstraintsResponse parseViewChargeConstraintsResponse(String response) {
 		System.out.println(response);
 		JSONObject json = (JSONObject) (JSONValue.parse(response));
 
 		int result = (Integer) json.get(MessageKeys.RESPONSE_CODE);
 		String message = (String) json.get(MessageKeys.RESPONSE_MESSAGE);
 
 		if (result != 200) {
 			return new ViewChargeConstraintsResponse(result, message);
 		}
 
 		int max = (Integer) json.get(MessageKeys.CHARGE_MAX);
 		int min = (Integer) json.get(MessageKeys.CHARGE_MIN);
 		return new ViewChargeConstraintsResponse(result, message, max, min);
 	}
 
 }
