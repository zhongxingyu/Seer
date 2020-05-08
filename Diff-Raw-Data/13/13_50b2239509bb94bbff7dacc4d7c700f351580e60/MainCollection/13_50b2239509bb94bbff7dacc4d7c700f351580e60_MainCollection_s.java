 /*
    MainCollection.java
    Copyright (C) 2013  Augusto Queiroz
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package javaCode.collection;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ConnectException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.TreeSet;
 import javaCode.collection.util.ApiKeysManager;
 import javaCode.collection.util.IOManager;
 import javaCode.collection.util.TraceManager;
 import javaCode.collection.util.URLManager;
 import javaCode.pojo.Event;
 import javaCode.pojo.Group;
 import javaCode.pojo.GroupTopics;
 import javaCode.pojo.Member;
 import javaCode.pojo.RSVP;
 import javaCode.pojo.Results;
 
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 
 public class MainCollection {
 
 	/*
 	 * List of OBJECT types
 	 */
 	public static final String GROUP = "group", EVENT = "event",
 			MEMBER = "member", TOPIC = "topic", VENUE = "venue", RSVP = "rsvp";
 
 	/*
 	 * List of RELATION between OBJECTs
 	 */
	public static final String GROUP_TOPIC = "group_topic",
 			GROUP_MEMBER = "group_member";
 
 	/*
 	 * PROPERTIES file variables
 	 */
 	public static String[] cities;
 
 	public static URLConnection urlConn;
 	public final static int URL_CONNECTION_TIMEOUT = 10 * 1000; // IN MILIS
 	public final static int URL_READ_TIMEOUT = 10 * 1000; // IN MILIS
 	public final static int SLEEP_INTERVAL_BETWEEN_TRIALS = 5 * 1000; // IN
 																		// MILIS
 
 	private static void findGroupsByCity(int cityIndex) throws IOException,
 			InterruptedException {
 
 		// Set the initial offset
 		int offset = TraceManager.getOffset();
 
 		ObjectMapper mapper = new ObjectMapper();
 
 		/*
 		 * Get all groups
 		 */
 		boolean keepFetching = true;
 
 		// Read the group id's already stored
 		TreeSet<Long> allGroupIds;
 		try {
 			allGroupIds = IOManager.readObjectAllIds(GROUP);
 		} catch (IOException e) {
 			// There is no id yet...
 			allGroupIds = new TreeSet<Long>();
 		}
 
 		// Read the group id's per city already stored
 		TreeSet<Long> groupIdsPerCity;
 		try {
 			groupIdsPerCity = IOManager.readObjectIdsPerCity(cityIndex, GROUP);
 		} catch (IOException e) {
 			// There is no id yet...
 			groupIdsPerCity = new TreeSet<Long>();
 		}
 
 		String key = "";
 		do {
 			// Check and stop for 1 hour, if needed
 			key = ApiKeysManager.getKey();
 
 			// Parse the JSON object directly from the URL
 			print("Fetching URL...");
 
 			Group[] groupArray;
 			try {
 				urlConn = new URL(URLManager.getFindGroupsURLByCity(key,
 						cityIndex, offset)).openConnection();
 				urlConn.setConnectTimeout(URL_CONNECTION_TIMEOUT);
 				urlConn.setReadTimeout(URL_READ_TIMEOUT);
 
 				if (!ApiKeysManager.checkConnectionCondition(urlConn))
 					continue;
 				else
 					println("");
 
 				groupArray = mapper.readValue(
 						new InputStreamReader(urlConn.getInputStream(),
 								IOManager.CHAR_SET), Group[].class);
 
 			} catch (SocketTimeoutException e) {
 				println("                " + e.getMessage());
 				Thread.sleep(SLEEP_INTERVAL_BETWEEN_TRIALS);
 				continue;
 			} catch (ConnectException ex) {
 				println("                " + ex.getMessage());
 				Thread.sleep(SLEEP_INTERVAL_BETWEEN_TRIALS);
 				continue;
 			}
 
 			ArrayList<Group> newGroups = new ArrayList<Group>();
 
 			print("        Processing data (" + groupArray.length
 					+ " group(s)):");
 			for (int i = 0; i < groupArray.length; i++) {
 				Long groupId = groupArray[i].getId();
 				if (!allGroupIds.contains(groupId)) {
 					allGroupIds.add(groupId);
 					groupIdsPerCity.add(groupId);
 					newGroups.add(groupArray[i]);
 				}// else clause {Removes the object}
 			}
 			println(" DONE");
 
 			// Rewrite the all group ids file
 			IOManager.writeObjectAllIds(GROUP, allGroupIds);
 
 			// Rewrite the group ids file per city
 			IOManager.writeObjectIdsPerCity(cityIndex, GROUP, groupIdsPerCity);
 
 			// Persist the new json objects, if there is any
 			if (!newGroups.isEmpty()) {
 				IOManager.appendJsonObjects(GROUP, newGroups.toArray());
 			}
 
 			// Rewrite the trace file
 			TraceManager.writeTraceFile(cityIndex,
 					TraceManager.FINDGROUPS_INDEX, 0, 0, 0, offset);
 
 			// Checks if there is any more data to fetch
 			if (groupArray.length >= URLManager.PAGE_SIZE
 					&& offset < (URLManager.MAX_OFFSET * 200)) {
 				offset++;
 			} else {
 				keepFetching = false;
 			}
 
 			System.gc();
 
 		} while (keepFetching);
 	}
 
 	private static void getMembersByGroup(int cityIndex) throws IOException,
 			InterruptedException {
 
 		// Read the group id's per city already stored
 		TreeSet<Long> groupIdsPerCity;
 		try {
 			groupIdsPerCity = IOManager.readObjectIdsPerCity(cityIndex, GROUP);
 		} catch (IOException e) {
 			// There is no id. So, there is nothing to do.
 			return;
 		}
 
 		// Set the initial groupIndex
 		int groupIndex = TraceManager.getGroupIndex();
 
 		// Set the initial offset
 		int offset = TraceManager.getOffset();
 
 		ObjectMapper mapper = new ObjectMapper();
 
 		// Read the member id's already stored
 		TreeSet<Long> allMemberIds;
 		try {
 			allMemberIds = IOManager.readObjectAllIds(MEMBER);
 		} catch (IOException e) {
 			// There is no id yet...
 			allMemberIds = new TreeSet<Long>();
 		}
 
 		String key = "";
 
 		// Foreach groupId of the given city do...
 		int i = 0;
 		for (Long groupId : groupIdsPerCity) {
 
 			if (i++ < groupIndex)
 				continue;
 
 			println("        Group " + groupId + " (" + (groupIndex + 1) + "/"
 					+ groupIdsPerCity.size() + ")");
 
 			boolean hasMoreData = true;
 
 			do {
 				// Check and stop for 1 hour, if needed
 				key = ApiKeysManager.getKey();
 
 				print("Fetching URL...");
 				Results<Member> memberResults;
 				try {
 					urlConn = new URL(URLManager.getMembersURLByGroup(key,
 							groupId, offset)).openConnection();
 					urlConn.setConnectTimeout(URL_CONNECTION_TIMEOUT);
 					urlConn.setReadTimeout(URL_READ_TIMEOUT);
 
 					if (!ApiKeysManager.checkConnectionCondition(urlConn))
 						continue;
 
 					memberResults = mapper.readValue(new InputStreamReader(
 							urlConn.getInputStream(), IOManager.CHAR_SET),
 							new TypeReference<Results<Member>>() {
 							});
 
 				} catch (SocketTimeoutException e) {
 					println("");
 					println("                " + e.getMessage());
 					Thread.sleep(SLEEP_INTERVAL_BETWEEN_TRIALS);
 					continue;
 				} catch (ConnectException ex) {
 					println("");
 					println("                " + ex.getMessage());
 					Thread.sleep(SLEEP_INTERVAL_BETWEEN_TRIALS);
 					continue;
 				}
 
 				int totalObjects = memberResults.getMeta().getTotal_count();
 				println("("
 						+ ((totalObjects <= 0) ? 0 : (offset + 1))
 						+ "/"
 						+ (int) Math.ceil(totalObjects
 								/ (double) URLManager.PAGE_SIZE) + ")");
 
 				ArrayList<Member> newMembers = new ArrayList<Member>();
 				TreeSet<Long> groupMemberIds = new TreeSet<Long>();
 
 				print("            Processing data ("
 						+ memberResults.getResults().size() + " member(s)):");
 				for (Member member : memberResults.getResults()) {
 
 					groupMemberIds.add(member.getId());
 					if (!allMemberIds.contains(member.getId())) {
 
 						allMemberIds.add(member.getId());
 						newMembers.add(member);
 					}// else clause {Ignores the repeated object}
 				}
 				println(" DONE");
 
 				// Rewrite the all member ids file
 				IOManager.writeObjectAllIds(MEMBER, allMemberIds);
 
 				// Persist the new json objects, if there is any
 				if (!newMembers.isEmpty()) {
 					IOManager.appendJsonObjects(MEMBER, newMembers.toArray());
 				}
 
 				// Persist the json relation between group and members
 				if (!groupMemberIds.isEmpty()) {
 					IOManager.appendJsonRelations(GROUP_MEMBER, groupId,
 							groupMemberIds);
 				}
 
 				// Rewrite the trace file
 				TraceManager.writeTraceFile(cityIndex,
 						TraceManager.MEMBERS_BYGROUP_INDEX, groupIndex, 0, 0,
 						offset);
 
 				if (memberResults.getMeta().getTotal_count() > (URLManager.PAGE_SIZE * (offset + 1))
 						&& offset < URLManager.MAX_OFFSET) {
 					offset++;
 				} else {
 					hasMoreData = false;
 				}
 
 				System.gc();
 
 			} while (hasMoreData);
 
 			// Reset the offset
 			offset = 0;
 
 			// Increment the groupIndex
 			groupIndex++;
 		}
 	}
 
 	private static void getEventsByGroup(int cityIndex) throws IOException,
 			InterruptedException {
 
 		// Read the group id's per city already stored
 		ArrayList<Long> groupIdsPerCity;
 		try {
 			groupIdsPerCity = IOManager.readObjectIdsPerCityList(cityIndex,
 					GROUP);
 		} catch (IOException e) {
 			// There is no id. So, there is nothing to do.
 			return;
 		}
 
 		// Set the initial groupIndex
 		int groupIndex = TraceManager.getGroupIndex();
 
 		// Set the initial offset
 		int offset = TraceManager.getOffset();
 
 		ObjectMapper mapper = new ObjectMapper();
 
 		// Read the member id's already stored
 		TreeSet<String> allEventIds;
 		try {
 			allEventIds = IOManager.readEventAllIds(EVENT);
 
 		} catch (IOException e) {
 			// There is no id yet...
 			allEventIds = new TreeSet<String>();
 		}
 
 		String key = "";
 		final int groupsPerCall = 25;
 
 		// Foreach group do...
 		for (; groupIndex < groupIdsPerCity.size();) {
 			int lastIndex = Math.min(groupIndex + groupsPerCall - 1,
 					groupIdsPerCity.size() - 1);
 			List<Long> groupIds = groupIdsPerCity.subList(groupIndex,
 					lastIndex + 1);
 
 			println("        Groups (" + (groupIndex + 1) + " to "
 					+ (lastIndex + 1) + "/" + groupIdsPerCity.size() + ")");
 
 			boolean hasMoreData = true;
 			do {
 				// Check and stop for 1 hour, if needed
 				key = ApiKeysManager.getKey();
 
 				print("Fetching URL...");
 
 				Results<Event> eventResults;
 				try {
 					// Parse the JSON object directly from the URL
 					urlConn = new URL(URLManager.getEventsURLByGroup(key,
 							groupIds, offset)).openConnection();
 					urlConn.setConnectTimeout(URL_CONNECTION_TIMEOUT);
 					urlConn.setReadTimeout(URL_READ_TIMEOUT);
 
 					if (!ApiKeysManager.checkConnectionCondition(urlConn))
 						continue;
 
 					eventResults = mapper.readValue(new InputStreamReader(
 							urlConn.getInputStream(), IOManager.CHAR_SET),
 							new TypeReference<Results<Event>>() {
 							});
 				} catch (SocketTimeoutException e) {
 					println("");
 					println("                " + e.getMessage());
 					Thread.sleep(SLEEP_INTERVAL_BETWEEN_TRIALS);
 					continue;
 				} catch (ConnectException ex) {
 					println("");
 					println("                " + ex.getMessage());
 					Thread.sleep(SLEEP_INTERVAL_BETWEEN_TRIALS);
 					continue;
 				}
 
 				int totalObjects = eventResults.getMeta().getTotal_count();
 				println("("
 						+ ((totalObjects <= 0) ? 0 : (offset + 1))
 						+ "/"
 						+ (int) Math.ceil(totalObjects
 								/ (double) URLManager.PAGE_SIZE) + ")");
 
 				ArrayList<Event> newEvents = new ArrayList<Event>();
 
 				print("            Processing data ("
 						+ eventResults.getResults().size() + " event(s)):");
 				for (Event e : eventResults.getResults()) {
 					if (!allEventIds.contains(e.getId())) {
 						allEventIds.add(e.getId());
 						newEvents.add(e);
 					}
 				}
 				println(" DONE");
 
 				// Rewrite the all member ids file
 				IOManager.writeEventAllIds(EVENT, allEventIds);
 
 				// Persist the new json objects, if there is any
 				if (!newEvents.isEmpty()) {
 					IOManager.appendJsonObjects(EVENT, newEvents.toArray());
 				}
 
 				// Rewrite the trace file
 				TraceManager.writeTraceFile(cityIndex,
 						TraceManager.EVENTS_BYGROUP_INDEX, groupIndex, 0, 0,
 						offset);
 
 				// Checks if there is more data to fetch
 				if (eventResults.getMeta().getTotal_count() > (URLManager.PAGE_SIZE * (offset + 1))
 						&& offset < URLManager.MAX_OFFSET) {
 					offset++;
 				} else {
 					hasMoreData = false;
 				}
 
 				System.gc();
 
 			} while (hasMoreData);
 
 			// Reset the offset
 			offset = 0;
 
 			// Increment the groupIndex
 			groupIndex = lastIndex + 1;
 		}
 	}
 
 	/**
 	 * It get the topics of all groups in a JSON object, this object is stored
 	 * in a JSON file with a prefix like groupTopic and updates the JSON file
 	 * with topics of the given city and the topic id's file.
 	 * 
 	 * @param cityIndex
 	 * @throws JsonParseException
 	 * @throws JsonMappingException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	private static void getGroupTopicsByGroup(int cityIndex)
 			throws IOException, InterruptedException {
 
 		// Read the group id's per city already stored
 		ArrayList<Long> groupIdsPerCity;
 		try {
 			groupIdsPerCity = IOManager.readObjectIdsPerCityList(cityIndex,
 					GROUP);
 		} catch (IOException e) {
 			// There is no id. So, there is nothing to do.
 			return;
 		}
 
 		// Set the initial groupIndex
 		int groupIndex = TraceManager.getGroupIndex();
 
 		// Set the initial offset
 		int offset = TraceManager.getOffset();
 
 		ObjectMapper mapper = new ObjectMapper();
 		String key = "";
 		final int groupsPerCall = 50;
 
 		// Foreach group do...
 		for (; groupIndex < groupIdsPerCity.size();) {
 			int lastIndex = Math.min(groupIndex + groupsPerCall - 1,
 					groupIdsPerCity.size() - 1);
 			List<Long> groupIds = groupIdsPerCity.subList(groupIndex,
 					lastIndex + 1);
 
 			println("        Groups (" + (groupIndex + 1) + " to "
 					+ (lastIndex + 1) + "/" + groupIdsPerCity.size() + ")");
 
 			boolean hasMoreData = true;
 			do {
 				// Check and stop for 1 hour, if needed
 				key = ApiKeysManager.getKey();
 
 				// Read all the members of the group
 				print("Fetching URL...");
 
 				Results<GroupTopics> groupTopicsResults;
 				try {
 					urlConn = new URL(URLManager.getGroupsURLByGroupId(key,
 							groupIds, offset)).openConnection();
 					urlConn.setConnectTimeout(URL_CONNECTION_TIMEOUT);
 					urlConn.setReadTimeout(URL_READ_TIMEOUT);
 
 					if (!ApiKeysManager.checkConnectionCondition(urlConn))
 						continue;
 
 					groupTopicsResults = mapper.readValue(
 							new InputStreamReader(urlConn.getInputStream(),
 									IOManager.CHAR_SET),
 							new TypeReference<Results<GroupTopics>>() {
 							});
 				} catch (SocketTimeoutException e) {
 					println("");
 					println("                " + e.getMessage());
 					Thread.sleep(SLEEP_INTERVAL_BETWEEN_TRIALS);
 					continue;
 				} catch (ConnectException ex) {
 					println("");
 					println("                " + ex.getMessage());
 					Thread.sleep(SLEEP_INTERVAL_BETWEEN_TRIALS);
 					continue;
 				}
 
 				int totalObjects = groupTopicsResults.getMeta()
 						.getTotal_count();
 				println("("
 						+ ((totalObjects <= 0) ? 0 : (offset + 1))
 						+ "/"
 						+ (int) Math.ceil(totalObjects
 								/ (double) URLManager.PAGE_SIZE) + ")");
 
				println("            Persisting data ("
 						+ groupTopicsResults.getResults().size()
						+ " groups(s)): DONE");
 
 				// Persist the new Topics (if it isn't empty)
 				if (!groupTopicsResults.getResults().get(0).getTopics()
 						.isEmpty()) {
 					IOManager.appendJsonObjects(GROUP_TOPIC, groupTopicsResults
 							.getResults().toArray());
 				}// else clause {Ignores the repeated object}

 				// Rewrite the trace file
 				TraceManager.writeTraceFile(cityIndex,
 						TraceManager.GROUPTOPICS_BYGROUP_INDEX, groupIndex, 0,
 						0, offset);
 
 				// Checks if there is more data to fetch
 				if (groupTopicsResults.getMeta().getTotal_count() > (URLManager.PAGE_SIZE * (offset + 1))
 						&& offset < URLManager.MAX_OFFSET) {
 					offset++;
 				} else {
 					hasMoreData = false;
 				}
 
 				System.gc();
 
 			} while (hasMoreData);
 
 			// Reset the offset
 			offset = 0;
 
 			// Increment the groupIndex
 			groupIndex = lastIndex + 1;
 		}
 	}
 
 	private static void getRSVPsByEvents(int cityIndex) throws IOException,
 			InterruptedException {
 
 		// Read all event id's
 		ArrayList<String> allEventsIds;
 		try {
 			allEventsIds = IOManager.readEventAllIdsList(EVENT);
 		} catch (IOException e) {
 			// There is no id yet...
 			allEventsIds = new ArrayList<String>();
 		}
 
 		// Set the initial members
 		int eventIndex = TraceManager.getEventIndex();
 
 		// Set the initial offset
 		int offset = TraceManager.getOffset();
 
 		ObjectMapper mapper = new ObjectMapper();
 		String key = "";
 		final int eventsPerCall = 50;
 
 		// Foreach member do...
 		for (; eventIndex < allEventsIds.size();) {
 			int lastIndex = Math.min(eventIndex + eventsPerCall - 1,
 					(allEventsIds.size() - 1));
 			List<String> eventIds = allEventsIds.subList(eventIndex,
 					lastIndex + 1);
 
 			println("        RSVPs (" + (eventIndex + 1) + " to "
 					+ (lastIndex + 1) + "/" + allEventsIds.size() + ")");
 
 			boolean hasMoreData = true;
 			do {
 				// Check and stop for 1 hour at most, if needed
 				key = ApiKeysManager.getKey();
 				print("Fetching URL...");
 
 				Results<RSVP> rsvpResults = null;
 				try {
 					urlConn = new URL(URLManager.getRSVPsURLByEvents(key,
 							eventIds, offset)).openConnection();
 					urlConn.setConnectTimeout(URL_CONNECTION_TIMEOUT);
 					urlConn.setReadTimeout(URL_READ_TIMEOUT);
 
 					if (!ApiKeysManager.checkConnectionCondition(urlConn))
 						continue;
 
 					rsvpResults = mapper.readValue(new InputStreamReader(
 							urlConn.getInputStream(), IOManager.CHAR_SET),
 							new TypeReference<Results<RSVP>>() {
 							});
 				} catch (SocketTimeoutException e) {
 					println("");
 					println("                " + e.getMessage());
 					Thread.sleep(SLEEP_INTERVAL_BETWEEN_TRIALS);
 					continue;
 				} catch (ConnectException ex) {
 					println("");
 					println("                " + ex.getMessage());
 					Thread.sleep(SLEEP_INTERVAL_BETWEEN_TRIALS);
 					continue;
 				}
 
 				int totalObjects = rsvpResults.getMeta().getTotal_count();
 				println("("
 						+ ((totalObjects <= 0) ? 0 : (offset + 1))
 						+ "/"
 						+ (int) Math.ceil(totalObjects
 								/ (double) URLManager.PAGE_SIZE) + ")");
 
 				print("            Processing data ("
 						+ rsvpResults.getResults().size() + " rsvp(s)):");
 				println(" DONE");
 
 				// Persist the new json objects, if there is any
 				if (!rsvpResults.getResults().isEmpty()) {
 					IOManager.appendJsonObjects(RSVP, rsvpResults.getResults()
 							.toArray());
 				}
 
 				// Rewrite the trace file
 				TraceManager.writeTraceFile(cityIndex,
 						TraceManager.RSVPS_BYEVENT_INDEX, 0, 0, eventIndex,
 						offset);
 
 				// Checks if there is more data to fetch
 				if (rsvpResults.getMeta().getTotal_count() > (URLManager.PAGE_SIZE * (offset + 1))
 						&& offset < URLManager.MAX_OFFSET) {
 					offset++;
 				} else {
 					hasMoreData = false;
 				}
 
 				System.gc();
 
 			} while (hasMoreData);
 
 			// Reset the offset
 			offset = 0;
 
 			// Increment the eventIndex
 			eventIndex = lastIndex + 1;
 		}
 	}
 
 	/*
 	 * PRINT METHODS
 	 */
 
 	private static void print(String s) {
 		System.out.print(s);
 	}
 
 	private static void println(String s) {
 		System.out.println(s);
 	}
 
 	/*
 	 * READ PROPERTIES
 	 */
 	public static void readPropertiesFile() throws FileNotFoundException {
 		Scanner sc = new Scanner(new File("properties.txt"));
 		String[] entries = null, newKeys = null, keyNames = null;
 		while (sc.hasNext()) {
 			entries = sc.nextLine().split("=");
 			if (entries[0].equals("API_KEYS")) {
 				newKeys = entries[1].split(",");
 			} else if (entries[0].equals("API_KEYS_NAMES")) {
 				keyNames = entries[1].split(",");
 			} else if (entries[0].equals("CITIES")) {
 				cities = entries[1].split(",");
 			}
 		}
 		sc.close();
 
 		ApiKeysManager.setKeys(newKeys, keyNames);
 	}
 
 	public static void main(String[] args) throws IOException,
 			InterruptedException {
 
 		// Set the API_KEY and CITIES from the properties.txt file
 		readPropertiesFile();
 
 		// Read the trace.txt file
 		TraceManager.readTraceFile();
 
 		// Set the initial cityIndex
 		int cityIndex = TraceManager.getCityIndex();
 
 		// Set the initial method index
 		int methodIndex = TraceManager.getMethodIndex();
 		boolean reset = false;
 		for (; cityIndex < cities.length; cityIndex++) {
 
 			println("CITY: " + cities[cityIndex]);
 
 			if (methodIndex <= TraceManager.FINDGROUPS_INDEX) {
 				if (reset) {
 					TraceManager.resetTraceData(cityIndex,
 							TraceManager.FINDGROUPS_INDEX, 0, 0, 0, 0);
 				}
 				println("    Find Groups by City...");
 				findGroupsByCity(cityIndex);
 				reset = true;
 			}
 			if (methodIndex <= TraceManager.MEMBERS_BYGROUP_INDEX) {
 				if (reset) {
 					TraceManager.resetTraceData(cityIndex,
 							TraceManager.MEMBERS_BYGROUP_INDEX, 0, 0, 0, 0);
 				}
 				println("    Get Members by Group...");
 				getMembersByGroup(cityIndex);
 				reset = true;
 			}
 			if (methodIndex <= TraceManager.EVENTS_BYGROUP_INDEX) {
 				if (reset) {
 					TraceManager.resetTraceData(cityIndex,
 							TraceManager.EVENTS_BYGROUP_INDEX, 0, 0, 0, 0);
 				}
 				println("    Get Events by Group...");
 				getEventsByGroup(cityIndex);
 				reset = true;
 			}
 			if (methodIndex <= TraceManager.GROUPTOPICS_BYGROUP_INDEX) {
 				if (reset) {
 					TraceManager.resetTraceData(cityIndex,
 							TraceManager.GROUPTOPICS_BYGROUP_INDEX, 0, 0, 0, 0);
 				}
 				println("    Get GroupTopics by Group...");
 				getGroupTopicsByGroup(cityIndex);
 				reset = true;
 			}
 
 			// Reset the method index
 			methodIndex = TraceManager.FINDGROUPS_INDEX;
 		}
 
 		// Get the RSVPs for all events (independent from city)
 		if (methodIndex <= TraceManager.RSVPS_BYEVENT_INDEX) {
 			println("All Cities:");
 			if (reset) {
 				// The cityIndex should be equals to the city list length
 				// (avoiding the for loop)
 				TraceManager.resetTraceData(cityIndex,
 						TraceManager.RSVPS_BYEVENT_INDEX, 0, 0, 0, 0);
 			}
 			println("    Get RSVPs by Events...");
 			getRSVPsByEvents(cityIndex);
 			reset = true;
 		}
 
 		println("FINISHED! =D");
 	}
 }
