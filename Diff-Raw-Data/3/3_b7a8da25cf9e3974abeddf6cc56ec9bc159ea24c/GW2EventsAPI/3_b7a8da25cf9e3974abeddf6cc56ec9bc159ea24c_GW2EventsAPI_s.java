 package ca.bsolomon.gw2event.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import ca.bsolomon.gw2event.api.dao.Build;
 import ca.bsolomon.gw2event.api.dao.Event;
 import ca.bsolomon.gw2event.api.dao.EventStatus;
 import ca.bsolomon.gw2event.api.dao.EventStatusMapper;
 import ca.bsolomon.gw2event.api.dao.Names;
 import ca.bsolomon.gw2event.api.util.SSLConn;
 
 public class GW2EventsAPI {
 
 	private static final String API_GUILDWARS2_URL = "http://api.guildwars2.com/";
 	private static final String API_VERSION = "v1/";
 	
 	private static final String EVENT_NAMES_JSON = "event_names.json";
 	private static final String MAP_NAMES_JSON = "map_names.json";
 	private static final String WORLD_NAMES_JSON = "world_names.json";
 	private static final String EVENTS_JSON = "events.json?";
 	private static final String EVENT_DETAILS_JSON = "event_details.json";
 	
 	private static final String MAP_ID = "map_id=";
 	private static final String WORLD_ID = "world_id=";
 	private static final String EVENT_ID = "event_id=";
 	private static final String BUILD_VERSION = "build.json";
 	
 	public static Map<String, String> eventIdToName = new HashMap<String, String>();
 	public static Map<String, String> eventIdToMap = new HashMap<String, String>();
 	public static Map<String, String> mapIdToName = new HashMap<String, String>();
 	public static Map<Integer, String> worldIdToName = new HashMap<Integer, String>();
 	
 	private HttpClient httpclient;
 	private static SSLConn sslConn = new SSLConn();
 
 	private static ObjectMapper objectMapper = new ObjectMapper();
 	
 	public static void generateEventIds() {
 		HttpClient httpclient = sslConn.createConnection();
 		
 		HttpGet httppost = new HttpGet(API_GUILDWARS2_URL+API_VERSION+EVENT_NAMES_JSON);
 		
 		try {
 	        // Add your data
 	        HttpResponse response = httpclient.execute(httppost);
 
 	        BufferedReader rd = new BufferedReader
 	        		  (new InputStreamReader(response.getEntity().getContent()));
 	        		    
 	        StringBuffer longline = new StringBuffer();
     		String line = "";
     		while ((line = rd.readLine()) != null) {
     			longline.append(line);
     		}
     		List<Names> result = objectMapper.readValue(longline.toString(), 
     				objectMapper.getTypeFactory().constructCollectionType(List.class, Names.class));
     		
     		for (int i=0;i< result.size();i++) {
     			Names obj = result.get(i);
     			
     			eventIdToName.put(obj.getId(), obj.getName());
     		}
 	    } catch (ClientProtocolException e) {
 	    	e.printStackTrace();
 	    } catch (IOException e) {
 	    	e.printStackTrace();
 	    } finally {
 	    	httppost.releaseConnection();
 	    }
 	}
 	
 	public static void generateMapIds() {
 		HttpClient httpclient = sslConn.createConnection();
 		
 		HttpGet httppost = new HttpGet(API_GUILDWARS2_URL+API_VERSION+MAP_NAMES_JSON);
 		
 		try {
 	        // Add your data
 	        HttpResponse response = httpclient.execute(httppost);
 
 	        BufferedReader rd = new BufferedReader
 	        		  (new InputStreamReader(response.getEntity().getContent()));
 	        		    
 	        StringBuffer longline = new StringBuffer();
     		String line = "";
     		while ((line = rd.readLine()) != null) {
     			longline.append(line);
     		}
     		List<Names> result = objectMapper.readValue(longline.toString(), 
     				objectMapper.getTypeFactory().constructCollectionType(List.class, Names.class));
     		
     		for (int i=0;i< result.size();i++) {
     			Names obj = result.get(i);
     			
     			mapIdToName.put(obj.getId(), obj.getName());
     		}
 	    } catch (ClientProtocolException e) {
 	    	e.printStackTrace();
 	    } catch (IOException e) {
 	    	e.printStackTrace();
 	    } finally {
 	    	httppost.releaseConnection();
 	    }
 	}
 	
 	public List<Names> queryEventIds() {
 		if (httpclient == null)
 			httpclient = sslConn.createConnection();
 		
 		HttpGet httppost = new HttpGet(API_GUILDWARS2_URL+API_VERSION+EVENT_NAMES_JSON);
 		
 		try {
 	        // Add your data
 	        HttpResponse response = httpclient.execute(httppost);
 
 	        BufferedReader rd = new BufferedReader
 	        		  (new InputStreamReader(response.getEntity().getContent()));
 	        		    
 	        StringBuffer longline = new StringBuffer();
     		String line = "";
     		while ((line = rd.readLine()) != null) {
     			longline.append(line);
     		}
     		List<Names> result = objectMapper.readValue(longline.toString(), 
     				objectMapper.getTypeFactory().constructCollectionType(List.class, Names.class));
     		
     		return result;
 	    } catch (ClientProtocolException e) {
 	    	e.printStackTrace();
 	    } catch (IOException e) {
 	    	e.printStackTrace();
 	    } finally {
 	    	httppost.releaseConnection();
 	    }
 		
 		return null;
 	}
 	
 	//http://stackoverflow.com/questions/16695527/how-to-map-a-json-attribute-name-to-a-java-field-value
 	public EventStatusMapper queryEventStatus() {
 		if (httpclient == null)
 			httpclient = sslConn.createConnection();
 		
 		HttpGet httppost = new HttpGet(API_GUILDWARS2_URL+API_VERSION+EVENT_DETAILS_JSON);
 		
 		try {
 	        // Add your data
 	        HttpResponse response = httpclient.execute(httppost);
 
 	        BufferedReader rd = new BufferedReader
 	        		  (new InputStreamReader(response.getEntity().getContent()));
 	        		    
 	        StringBuffer longline = new StringBuffer();
     		String line = "";
     		while ((line = rd.readLine()) != null) {
     			longline.append(line);
     		}
     		EventStatusMapper result = objectMapper.readValue(longline.toString(), 
     				EventStatusMapper.class);
     		
     		return result;
 	    } catch (ClientProtocolException e) {
 	    	e.printStackTrace();
 	    } catch (IOException e) {
 	    	e.printStackTrace();
 	    } finally {
 	    	httppost.releaseConnection();
 	    }
 		
 		return null;
 	}
 	
 	public static void generateNAWorldIds() {
 		HttpClient httpclient = sslConn.createConnection();
 		
 		HttpGet httppost = new HttpGet(API_GUILDWARS2_URL+API_VERSION+WORLD_NAMES_JSON);
 		
 		try {
 	        // Add your data
 	        HttpResponse response = httpclient.execute(httppost);
 
 	        BufferedReader rd = new BufferedReader
 	        		  (new InputStreamReader(response.getEntity().getContent()));
 	        		    
 	        StringBuffer longline = new StringBuffer();
     		String line = "";
     		while ((line = rd.readLine()) != null) {
     			longline.append(line);
     		}
     		List<Names> result = objectMapper.readValue(longline.toString(), 
     				objectMapper.getTypeFactory().constructCollectionType(List.class, Names.class));
     		
     		for (int i=0;i< result.size();i++) {
     			Names obj = result.get(i);
     			
     			Integer worldId = Integer.parseInt(obj.getId());
     			
     			if (worldId < 2000)
     				worldIdToName.put(worldId, obj.getName());
     		}
 	    } catch (ClientProtocolException e) {
 	    	e.printStackTrace();
 	    } catch (IOException e) {
 	    	e.printStackTrace();
 	    } finally {
 	    	httppost.releaseConnection();
 	    }
 	}
 
 	public List<Event> queryServer(int worldId, int mapId) {
 		if (httpclient == null)
 			httpclient = sslConn.createConnection();
 		
 		HttpGet httppost = new HttpGet(API_GUILDWARS2_URL+API_VERSION+EVENTS_JSON+WORLD_ID+worldId+"&"+MAP_ID+mapId);
 		
 		try {
 	        // Add your data
 	        HttpResponse response = httpclient.execute(httppost);
 
 	        BufferedReader rd = new BufferedReader
 	        		  (new InputStreamReader(response.getEntity().getContent()));
 	        		    
 	        StringBuffer longline = new StringBuffer();
     		String line = "";
     		while ((line = rd.readLine()) != null) {
     			longline.append(line);
     		}
     		List<Event> result = objectMapper.readValue(longline.substring(10), 
     				objectMapper.getTypeFactory().constructCollectionType(List.class, Event.class));
     		
     		return result;
 		} catch (ClientProtocolException e) {
 	    	e.printStackTrace();
 	    } catch (IOException e) {
 	    	e.printStackTrace();
 	    } finally {
 	    	httppost.releaseConnection();
 	    }	
 		
 		return null;
 	}
 	
 	public Event queryServer(String worldId,String eventId) {
 		if (httpclient == null)
 			httpclient = sslConn.createConnection();
 		
 		HttpGet httppost = new HttpGet(API_GUILDWARS2_URL+API_VERSION+EVENTS_JSON+EVENT_ID+eventId+"&"+WORLD_ID+worldId);
 		
 		try {
 	        // Add your data
 	        HttpResponse response = httpclient.execute(httppost);
 
 	        BufferedReader rd = new BufferedReader
 	        		  (new InputStreamReader(response.getEntity().getContent()));
 	        		    
 	        StringBuffer longline = new StringBuffer();
     		String line = "";
     		while ((line = rd.readLine()) != null) {
     			longline.append(line);
     		}
     		List<Event> result = objectMapper.readValue(longline.substring(10), 
     				objectMapper.getTypeFactory().constructCollectionType(List.class, Event.class));
     		
     		return result.get(0);
 		} catch (ClientProtocolException e) {
 			System.out.println("Protocol exception");
 	    } catch (IOException e) {
 	    	System.out.println("Can not connect to server");
 	    } catch (Exception e) {
 	    	return null;
 	    } finally {
 	    	httppost.releaseConnection();
 	    }
 		
 		return null;
 	}
 	
 	public List<Event> queryServer(String eventId) {
 		if (httpclient == null)
 			httpclient = sslConn.createConnection();
 		
 		HttpGet httppost = new HttpGet(API_GUILDWARS2_URL+API_VERSION+EVENTS_JSON+EVENT_ID+eventId);
 		
 		try {
 	        // Add your data
 	        HttpResponse response = httpclient.execute(httppost);
 
 	        BufferedReader rd = new BufferedReader
 	        		  (new InputStreamReader(response.getEntity().getContent()));
 	        		    
 	        StringBuffer longline = new StringBuffer();
     		String line = "";
     		while ((line = rd.readLine()) != null) {
     			longline.append(line);
     		}
     		List<Event> result = objectMapper.readValue(longline.substring(10), 
     				objectMapper.getTypeFactory().constructCollectionType(List.class, Event.class));
     		
     		return result;
 		} catch (ClientProtocolException e) {
 	    	System.out.println("Protocol exception");
 	    } catch (IOException e) {
 	    	System.out.println("Can not connect to server");
 	    } finally {
 	    	httppost.releaseConnection();
 	    }
 		
 		return null;
 	}
 	
 	public List<Event> queryServerEventStatus(String worldId) {
 		if (httpclient == null)
 			httpclient = sslConn.createConnection();
 		
 		HttpGet httppost = new HttpGet(API_GUILDWARS2_URL+API_VERSION+EVENTS_JSON+WORLD_ID+worldId);
 		
 		try {
 	        // Add your data
 	        HttpResponse response = httpclient.execute(httppost);
 
 	        BufferedReader rd = new BufferedReader
 	        		  (new InputStreamReader(response.getEntity().getContent()));
 	        		    
 	        StringBuffer longline = new StringBuffer();
     		String line = "";
     		while ((line = rd.readLine()) != null) {
     			longline.append(line);
     		}
     		List<Event> result = objectMapper.readValue(longline.substring(10), 
     				objectMapper.getTypeFactory().constructCollectionType(List.class, Event.class));
     		
     		return result;
 		} catch (ClientProtocolException e) {
 	    	System.out.println("Protocol exception");
 	    } catch (IOException e) {
 	    	System.out.println("Can not connect to server");
 	    } finally {
 	    	httppost.releaseConnection();
 	    }
 		
 		return null;
 	}
 	
 	public List<Event> queryMapEventStatus(String mapId) {
 		if (httpclient == null)
 			httpclient = sslConn.createConnection();
 		
 		HttpGet httppost = new HttpGet(API_GUILDWARS2_URL+API_VERSION+EVENTS_JSON+MAP_ID+mapId);
 		
 		try {
 	        // Add your data
 	        HttpResponse response = httpclient.execute(httppost);
 
 	        BufferedReader rd = new BufferedReader
 	        		  (new InputStreamReader(response.getEntity().getContent()));
 	        		    
 	        StringBuffer longline = new StringBuffer();
     		String line = "";
     		while ((line = rd.readLine()) != null) {
     			longline.append(line);
     		}
     		List<Event> result = objectMapper.readValue(longline.substring(10), 
     				objectMapper.getTypeFactory().constructCollectionType(List.class, Event.class));
     		
     		return result;
 		} catch (ClientProtocolException e) {
 	    	System.out.println("Protocol exception");
 	    } catch (IOException e) {
 	    	System.out.println("Can not connect to server");
 	    } finally {
 	    	httppost.releaseConnection();
 	    }
 		
 		return null;
 	}
 
 	public String getEventMap(String eventId) {
 		if (eventIdToMap.containsKey(eventId))
 			return  mapIdToName.get(eventIdToMap.get(eventId));
 		
 		if (httpclient == null)
 			httpclient = sslConn.createConnection();
 		
 		HttpGet httppost = new HttpGet(API_GUILDWARS2_URL+API_VERSION+EVENTS_JSON+EVENT_ID+eventId);
 		
 		try {
 	        // Add your data
 	        HttpResponse response = httpclient.execute(httppost);
 
 	        BufferedReader rd = new BufferedReader
 	        		  (new InputStreamReader(response.getEntity().getContent()));
 	        		    
 	        StringBuffer longline = new StringBuffer();
     		String line = "";
     		while ((line = rd.readLine()) != null) {
     			longline.append(line);
     		}
     		List<Event> result = objectMapper.readValue(longline.substring(10), 
     				objectMapper.getTypeFactory().constructCollectionType(List.class, Event.class));
     		
     		Event obj = result.get(0);
 			
 			eventIdToMap.put(eventId, obj.getMapId());
 			
 			return mapIdToName.get(obj.getMapId());
 		} catch (ClientProtocolException e) {
 	    	System.out.println("Protocol exception");
 	    } catch (IOException e) {
 	    	System.out.println("Can not connect to server");
 	    } finally {
 	    	httppost.releaseConnection();
 	    }
 		
 		return null;
 	}
 	
 	public String queryBuildVersion() {
 		if (httpclient == null)
 			httpclient = sslConn.createConnection();
 		
 		HttpGet httppost = new HttpGet(API_GUILDWARS2_URL+API_VERSION+BUILD_VERSION);
 		
 		try {
 	        // Add your data
 	        HttpResponse response = httpclient.execute(httppost);
 
 	        BufferedReader rd = new BufferedReader
 	        		  (new InputStreamReader(response.getEntity().getContent()));
 	        		    
 	        StringBuffer longline = new StringBuffer();
     		String line = "";
     		while ((line = rd.readLine()) != null) {
     			longline.append(line);
     		}
     		Build result = objectMapper.readValue(longline.toString(), 
     				Build.class);
     		
     		return result.getBuildId();
 		} catch (ClientProtocolException e) {
 	    	e.printStackTrace();
 	    } catch (IOException e) {
 	    	e.printStackTrace();
 	    } finally {
 	    	httppost.releaseConnection();
 	    }	
 		
 		return null;
 	}
 }
