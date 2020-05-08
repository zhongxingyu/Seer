 package Cosm;
 
 import java.net.URI;
 import java.net.URL;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.json.JSONObject;
 import org.json.JSONArray;
 
 import Cosm.Client.CosmClient;
 
 public class Cosm {
 	public static final String VERSION = "1.0.0";
 	
 	private CosmClient client;
 
 	public Cosm(String apikey) {
 		client = new CosmClient(apikey);
 	}
 
 	public Cosm(String username,String password) {
 		client = new CosmClient(username,password);
 	}
 
 	/**
 	 * Get a feed by its feed identifier
      * 
      * @param feedid Id of the Pachube feed to retrieve
      * @return Feed which corresponds to the id provided as the parameter
      * @throws CosmException If something goes wrong.
      */
 	public Feed getFeed(int feedid) throws CosmException {
 		try {			
 			HttpGet hr = new HttpGet("http://api.cosm.com/v2/feeds/"+feedid+".json");
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200) {
 				return CosmFactory.toFeed(client.getBody(response));				
 			} else {
 				throw new CosmException(response.getStatusLine().toString());				
 			}			
 		} catch ( Exception e) {		
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());
 		}
 	}
 
 	// get feeds with more search options
 	// TODO: include lat, lon, distance, and distance_units in query
 	/**
 	 * returns a list of feed objects based on a number of optional query parameters. If set to {@link null}, a parameter is ignored.
 	 * 
 	 * @param query Full text {@link String} search parameter. Should return any feeds matching this string
 	 * @param content parameter of type {@link Content} describing the type of results
 	 * @param tag Returns feeds containing datastreams tagged with the search query
 	 * @param user Returns feeds created by the user specified.
 	 * @param units Returns feeds containing datastreams with units specified by the search query.
 	 * @param status Parameter of type {@link Status}
 	 * @param order Parameter of type {@link Order}. Used for ordering the results.
 	 * @param show_user Include user login and user level for each feed. {@link Boolean} with possible values: true, false (default)
 	 * @return Array of {@link Feed} objects
 	 * @throws CosmException
 	 */
 	public Feed[] getFeeds(String query,Content content,String tag,String user,String units,Status status,Order order,Boolean show_user) throws CosmException {
 		String q = "";
 		Boolean bAdd = false;
 		
 		if ( query != null ) {		
 			if ( bAdd ) q += '&';
 			q += "q=" + query;
 			bAdd = true;
 		}
 		if ( content != null ) {
 			if ( bAdd ) q += '&';
 			q += "content=" + content.toString();
 			bAdd = true;
 		}		
 		if ( tag != null ) {
 			if ( bAdd ) q += '&';
 			q += "tag=" + tag;
 			bAdd = true;			
 		}
 		if ( user != null ) {
 			if ( bAdd ) q += '&';
 			q += "user=" + user;
 			bAdd = true;
 		}
 		if ( units != null ) {
 			if ( bAdd ) q += '&';
 			q += "units=" + units;
 			bAdd = true;
 		}
 		if ( status != null ) {
 			if ( bAdd ) q += '&';
 			q += "status=" + status.toString();
 			bAdd = true;
 		}		
 		if ( order != null ) {
 			if ( bAdd ) q += '&';
 			q += "order=" + order.toString();
 			bAdd = true;
 		}		
 		if ( show_user != null ) {
 			if ( bAdd ) q += '&';
 			q += "show_user=" + show_user.toString();
 			bAdd = true;			
 		}
 		
 		
 		
 		
 		try {
 			URI uri = new URI("http","api.cosm.com","/v2/feeds.json",q,null);
 			
 			System.err.println(uri.toASCIIString());
 			
 			HttpGet hr = new HttpGet(uri);
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200) {
 				return CosmFactory.toFeeds(client.getBody(response));				
 			} else {
 				throw new CosmException(response.getStatusLine().toString());				
 			}			
 		} catch ( Exception e) {		
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());
 		}		
 	}
 	
 	// get feeds
 	public Feed[] getFeeds() throws CosmException {
 		//TODO: scrolling is not supported.
 		return getFeeds(null,null,null,null,null,null,null,null);
 	}
 
 	// delete feed
 	public void deleteFeed(Integer feedid) throws CosmException {
 		try {
 			HttpDelete hr = new HttpDelete("http://api.cosm.com/v2/feeds/"+feedid);
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			client.getBody(response);
 			if ( statusLine.getStatusCode() != 200) {
 				throw new CosmException(response.getStatusLine().toString());				
 			}			
 		} catch ( Exception e ) {		
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());
 		}		
 	}
 
 	// create feed
 	public Feed createFeed(Feed feed) throws CosmException {
 		try {
 			HttpPost hr = new HttpPost("http://api.cosm.com/v2/feeds.json");
 			hr.setEntity(new StringEntity(feed.toJSONObject().toString()));
 			HttpResponse response = client.execute(hr);			
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 201 ) {
 				String a[] = response.getHeaders("Location")[0].getValue().split("/");
 				Integer feedid = Integer.parseInt(a[a.length -1]);
 				client.getBody(response);
 				return this.getFeed(feedid);
 			} else {
 				throw new CosmException(response.getStatusLine().toString());								
 			}						
 		} catch ( Exception e) {
 			e.printStackTrace();
 			throw new CosmException("Caught exception in create Feed" + e.getMessage());
 		}
 	}
 
 	// update feed
 	public void updateFeed(Feed feed) throws CosmException {
 		try {
 			HttpPut hr = new HttpPut("http://api.cosm.com/v2/feeds/" + feed.getId() + ".json");
 			hr.setEntity(new StringEntity(feed.toJSONObject().toString()));
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() != 200 ) {
 				throw new CosmException(response.getStatusLine().toString());												
 			}
 		} catch ( Exception e) {
 			e.printStackTrace();
 			throw new CosmException("Caught exception in create Feed");
 		}
 	}
 
 	// create group
 	public Group createGroup(Group group) throws CosmException {
 		try {
 			HttpPost hr = new HttpPost("http://api.cosm.com/v2/groups.json");
 			hr.setEntity(new StringEntity(group.toJSONObject().toString()));
 			HttpResponse response = client.execute(hr);			
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 201 ) {
 				return CosmFactory.toGroup(client.getBody(response));
 			} else {
 				throw new CosmException(response.getStatusLine().toString());								
 			}						
 		} catch ( Exception e) {
 			e.printStackTrace();
 			throw new CosmException("Caught exception in create Group" + e.getMessage());
 		}
 		
 	}
 	
 	// get group
 	public Group getGroup(String groupid) throws CosmException {
 		try {
 			HttpGet hr = new HttpGet("http://api.cosm.com/v2/groups/"+groupid+".json");
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200 ) {
 				return CosmFactory.toGroup(client.getBody(response));
 			} else {
 				throw new CosmException(response.getStatusLine().toString());												
 			}
 		} catch ( Exception e) {
 			e.printStackTrace();
 			throw new CosmException("Caught exception in getGroup" + e.getMessage());
 		}
 	}
 		
 	// get groups
 	public Group[] getGroups() throws CosmException {
 		try {
 			HttpGet hr = new HttpGet("http://api.cosm.com/v2/groups.json");
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200 ) {
 				return CosmFactory.toGroups(client.getBody(response));
 			} else {
 				throw new CosmException(response.getStatusLine().toString());												
 			}
 		} catch ( Exception e ) {
 			throw new CosmException("Caught exception in getGroups");
 		}
 	}
 	
 	// update group
 	public void updateGroup(Group group) throws CosmException {
 		try {
 			HttpPut hr = new HttpPut("http://api.cosm.com/v2/groups/" + group.getGroupid() + ".json");
 			hr.setEntity(new StringEntity(group.toJSONObject().toString()));
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
			String body = client.getBody(response);
			if (( statusLine.getStatusCode() != 200 )&&(  statusLine.getStatusCode() != 201)) {
 				throw new CosmException(response.getStatusLine().toString());																
 			}
 		} catch ( Exception e) {
 			e.printStackTrace();
 			throw new CosmException("Caught exception in update group");
 		}
 	}
 	
 	// delete group
 	public void deleteGroup(String groupid) throws CosmException {
 		try {
 			HttpDelete hr = new HttpDelete("http://api.cosm.com/v2/groups/"+groupid);			
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() != 200 ) {
 				throw new CosmException(response.getStatusLine().toString());																				
 			}
 		} catch ( Exception e) {		
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());
 		}		
 	}
 	
 	// get datastream
 	public Datastream getDatastream(Integer feedid, String datastreamid) throws CosmException {
 		try {
 			HttpGet request = new HttpGet("http://api.cosm.com/v2/feeds/"+feedid+"/datastreams/"+datastreamid+".json");
 			HttpResponse response = client.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200 ) {
 				return CosmFactory.toDatastream(client.getBody(response));
 			} else {
 				throw new HttpException(statusLine.toString());
 			}
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());
 		}
 	}
 	
 	// get datastreams
 	public Datastream[] getDatastreams(Integer feedid) throws CosmException {
 		try {
 			Feed feed = getFeed(feedid);
 			return feed.getDatastreams();
 		} catch ( Exception e ) {
 			throw new CosmException(e.getMessage());
 		}
 
 	}
 	
 	// create datastream
 	public Datastream createDatastream(Integer feedid, Datastream datastream) throws CosmException {
 		try {
 			HttpPost request = new HttpPost("http://api.cosm.com/v2/feeds/"+feedid+"/datastreams.json");
 			JSONObject jo = new JSONObject();
 			jo.put("version", Cosm.VERSION);
 			
 			JSONArray ja = new JSONArray();
 			ja.put(datastream.toJSONObject());
 			jo.put("datastreams",ja);
 			
 			
 			request.setEntity(new StringEntity(jo.toString()));
 			HttpResponse response = client.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 201 ) {
 				String a[] = response.getHeaders("Location")[0].getValue().split("/");
 				String datastreamid = a[a.length -1];
 				client.getBody(response);
 				return this.getDatastream(feedid,datastreamid);
 			} else {
 				throw new HttpException(response.getStatusLine().toString());																
 			}
 		} catch ( Exception e) {
 			e.printStackTrace();
 			throw new CosmException("Caught exception in create Datastream" + e.getMessage());
 		}
 	}
 		
 	// update datastream
 	public void updateDatastream(Integer feedid,String datastreamid, Datastream datastream) throws CosmException {
 		try {
 			HttpPut request = new HttpPut("http://api.cosm.com/v2/feeds/"+feedid+"/datastreams/"+datastreamid+".json");
 			request.setEntity(new StringEntity(datastream.toJSONObject().toString()));
 			HttpResponse response = client.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200 ) {
 				client.getBody(response);
 				return;
 			} else {
 				throw new HttpException(statusLine.toString());
 			}
 		} catch ( Exception e ) {
 			throw new CosmException(e.getMessage());
 		}
 	}
 	
 	// delete datastream
 	public void deleteDatastream(Integer feedid,String datastreamid) throws CosmException {
 		try {
 			HttpDelete hr = new HttpDelete("http://api.cosm.com/v2/feeds/"+feedid+"/datastreams/"+ datastreamid);
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() != 200 ) {
 				throw new CosmException(response.getStatusLine().toString());																				
 			}
 		} catch ( Exception e) {		
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());
 		}		
 	}
 	
 	
 	// create datapoint
 	public void createDatapoint(Integer feedid,String datastreamid,Datapoint datapoint) throws CosmException {
 		try {
 			HttpPost request = new HttpPost("http://api.cosm.com/v2/feeds/"+feedid+"/datastreams/"+datastreamid+"/datapoints.json");
 			JSONObject jo = new JSONObject();
 			JSONArray ja = new JSONArray();
 			ja.put(datapoint.toJSONObject());
 			jo.put("datapoints", ja);
 			request.setEntity(new StringEntity(jo.toString()));
 			HttpResponse response = client.execute(request);			
 			StatusLine statusLine = response.getStatusLine();
 			client.getBody(response);
 			if ( statusLine.getStatusCode() != 200 ) {
 				throw new CosmException(response.getStatusLine().toString());								
 			}						
 		} catch ( Exception e) {
 			e.printStackTrace();
 			throw new CosmException("Caught exception in create datapoint" + e.getMessage());
 		}
 			
 	}
 	
 	// create datapoints
 	public void createDatapoints(Integer feedid,String datastreamid,Datapoint[] datapoints) throws CosmException {
 		try {
 			HttpPost request = new HttpPost("http://api.cosm.com/v2/feeds/"+feedid+"/datastreams/"+datastreamid+"/datapoints.json");
 			JSONObject jo = new JSONObject();
 			
 			JSONArray ja = new JSONArray();
 			for(int i=0;(i<datapoints.length);i++) {			
 				ja.put(datapoints[i].toJSONObject());				
 			}
 			jo.put("datapoints", ja);
 			request.setEntity(new StringEntity(jo.toString()));
 			HttpResponse response = client.execute(request);			
 			StatusLine statusLine = response.getStatusLine();
 			String body = client.getBody(response);
 			if ( statusLine.getStatusCode() != 200 ) {
 				JSONObject ej = new JSONObject(body);
 				throw new CosmException(ej.getString("errors"));
 			}						
 		} catch ( Exception e) {
 			System.err.println(e.getMessage());
 			throw new CosmException("Caught exception in create datapoint" + e.getMessage());
 		}
 			
 	}
 	
 	// update datapoint
 	// Cosm documentation says, it's a post. It is in fact a PUT
 	public void updateDatapoint(Integer feedid,String datastreamid,Datapoint datapoint) throws CosmException {
 		try {
 			HttpPut request = new HttpPut("http://api.cosm.com/v2/feeds/"+ feedid + "/datastreams/"+datastreamid+"/datapoints/"+datapoint.getAt() + ".json");
 			JSONObject jo = new JSONObject();
 			jo.put("value",datapoint.getValue());
 			request.setEntity(new StringEntity(jo.toString()));
 			HttpResponse response = client.execute(request);			
 			StatusLine statusLine = response.getStatusLine();
 			String body = client.getBody(response);
 			if ( statusLine.getStatusCode() != 200 ) {
 				System.err.println(body);
 				if ( body.length() > 0 ) {
 					JSONObject ej = new JSONObject(body);
 					throw new CosmException(ej.getString("errors"));				
 				} else {
 					throw new CosmException(statusLine.toString());
 				}
 			}
 		} catch ( Exception e) {
 			throw new CosmException("Caught exception in update datapoint: " + e.getMessage());
 		}
 	}
 
 	// get a datapoint
 	public Datapoint getDatapoint(Integer feedid, String datastreamid,String at) throws CosmException {
 		try {
 			HttpGet request = new HttpGet("http://api.cosm.com/v2/feeds/"+feedid+"/datastreams/"+datastreamid+"/datapoints/"+ at + ".json");
 			HttpResponse response = client.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200 ) {
 				return CosmFactory.toDatapoint(client.getBody(response));
 			} else {
 				throw new HttpException(statusLine.toString());
 			}
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());
 		}	
 	}
 	
 	// deleting a datapoint
 	public void deleteDatapoint(Integer feedid, String datastreamid,String at) throws CosmException {
 		try {
 			HttpDelete request = new HttpDelete("http://api.cosm.com/v2/feeds/"+feedid+"/datastreams/"+datastreamid+"/datapoints/"+ at);			
 			HttpResponse response = client.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			client.getBody(response);
 			if ( statusLine.getStatusCode() != 200 ) {
 				throw new HttpException(statusLine.toString());
 			}
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());
 		}	
 	}
 	
 	// deleting multiple datapoints
 	public void deleteDatapoints(Integer feedid, String datastreamid,String start, String end, String duration) throws CosmException {
 		try {
 			String url = "http://api.cosm.com/v2/feeds/"+feedid+"/datastreams/"+datastreamid+"/datapoints?";
 			Boolean bAdd = false;
 			if ( start != null ) {
 				if ( bAdd ) url += '&';
 				url += "start=" + start;
 				bAdd = true;
 			}
 			if ( end != null ) {
 				if ( bAdd ) url += '&';
 				url += "end=" + end;
 				bAdd = true;
 			}
 			if ( duration != null ) {
 				if ( bAdd ) url += '&';
 				url += "duration=" + duration;
 				bAdd = true;
 			}
 			HttpDelete request = new HttpDelete(url);
 			HttpResponse response = client.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() != 200 ) {
 				throw new HttpException(statusLine.toString());
 			}
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());
 		}	
 	}
 	
 	// listing all datapoints, historical queries
 	public Datapoint[] getDatapoints(Integer feedid, String datastreamid, String start, String end, String duration,Integer interval, Boolean find_previous, Interval_type interval_type) throws CosmException {
 		//TODO: check if all combinations are valid is missing
 		//TODO: date checking here?
 		try {
 			String url = "http://api.cosm.com/v2/feeds/"+feedid+"/datastreams/"+datastreamid+".json?";
 			
 			Boolean bAdd = false;
 			if ( start != null ) {
 				if ( bAdd ) url += '&';
 				url += "start=" + start;
 				bAdd = true;
 			}
 			if ( end != null ) {
 				if ( bAdd ) url += '&';
 				url += "end=" + end;
 				bAdd = true;
 			}
 			if ( duration != null ) {
 				if ( bAdd ) url += '&';
 				url += "duration=" + duration;
 				bAdd = true;
 			}
 			if ( interval != null ) {
 				if ( bAdd ) url += '&';
 				url += "interval=" + interval;
 				bAdd = true;
 			}
 			if ( find_previous != null ) {
 				if ( bAdd ) url += '&';
 				url += "find_previous=" + find_previous.toString();
 				bAdd = true;
 			}
 			if ( interval_type != null ) {
 				if ( bAdd ) url += '&';
 				url += "interval_type=" + interval_type.toString();
 				bAdd = true;
 			}
 			
 			//System.err.println(url);
 			
 			HttpGet request = new HttpGet(url);
 			HttpResponse response = client.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			String body = client.getBody(response);
 			if ( statusLine.getStatusCode() == 200 ) {
 				return CosmFactory.toDatapoints(body);
 			} else {
 				System.err.println(body);
 				throw new HttpException(statusLine.toString());
 			}
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());			
 		}
 	}
 	
 	// create apikey
 	public Apikey createApikey(Apikey apikey) throws CosmException {
 		try {
 			HttpPost request = new HttpPost("http://api.cosm.com/v2/keys.json");
 			request.setEntity(new StringEntity(apikey.toJSONObject().toString()));
 			HttpResponse response = client.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			String body = client.getBody(response);			
 			if ( statusLine.getStatusCode() == 201 ) {
 				String a[] = response.getHeaders("Location")[0].getValue().split("/");
 				String key = a[a.length -1];				
 				return this.getApikey(key);
 			} else {
 				throw new Exception(body);
 			}
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException("error while creating new apikey");
 		}
 	}
 		
 	// get an apikey
 	public Apikey getApikey(String apikey) throws CosmException {
 		try {
 			HttpGet hr = new HttpGet("http://api.cosm.com/v2/keys/"+apikey+".json");
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200) {
 				return CosmFactory.toApikey(client.getBody(response));				
 			} else {
 				throw new CosmException(response.getStatusLine().toString());				
 			}						
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException("error in getApikey");
 		}
 	}
 		
 	// get a list of apikeys 
 	public Apikey[] getApikeys() throws CosmException {
 		try {
 			HttpGet hr = new HttpGet("http://api.cosm.com/v2/keys.json");
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200) {
 				return CosmFactory.toApikeys(client.getBody(response));				
 			} else {
 				throw new CosmException(response.getStatusLine().toString());				
 			}						
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException("error in getApikey");
 		}
 	}
 	
 	// deleting an apikey
 	public void deleteApikey(String apikey) throws CosmException {
 		try {
 			HttpDelete request = new HttpDelete("http://api.cosm.com/v2/keys/"+ apikey);			
 			HttpResponse response = client.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			client.getBody(response);
 			if ( statusLine.getStatusCode() != 200 ) {
 				throw new HttpException(statusLine.toString());
 			}
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());
 		}	
 		
 	}
 		
 	// get trigger
 	public Trigger getTrigger(Integer id) throws CosmException {
 		try {
 			HttpGet hr = new HttpGet("http://api.cosm.com/v2/triggers/"+id+".json");
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200) {
 				return CosmFactory.toTrigger(client.getBody(response));				
 			} else {
 				throw new CosmException(response.getStatusLine().toString());				
 			}									
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException("error in getTrigger");
 		}
 	}
 	
 	// get triggers
 	public Trigger[] getTriggers() throws CosmException {
 		try {
 			HttpGet hr = new HttpGet("http://api.cosm.com/v2/triggers.json");
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200) {
 				return CosmFactory.toTriggers(client.getBody(response));				
 			} else {
 				throw new CosmException(response.getStatusLine().toString());				
 			}									
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException("error in getTrigger");
 		}
 	}
 	
 	// delete trigger
 	public void deleteTrigger(Integer id) throws CosmException {
 		try {
 			HttpDelete request = new HttpDelete("http://api.cosm.com/v2/triggers/"+ id);			
 			HttpResponse response = client.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			client.getBody(response);
 			if ( statusLine.getStatusCode() != 200 ) {
 				throw new HttpException(statusLine.toString());
 			}
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());
 		}	
 		
 	}
 	
 	// create trigger
 	public Trigger createTrigger(Trigger trigger) throws CosmException {
 		try {
 			HttpPost request = new HttpPost("http://api.cosm.com/v2/triggers.json");
 			request.setEntity(new StringEntity(trigger.toJSONObject().toString()));
 			HttpResponse response = client.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			String body = client.getBody(response);			
 			if ( statusLine.getStatusCode() == 201 ) {
 				String a[] = response.getHeaders("Location")[0].getValue().split("/");
 				Integer id = Integer.parseInt(a[a.length -1]);				
 				return this.getTrigger(id);
 			} else {
 				throw new Exception(body);
 			}
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException("error while creating new apikey");
 		}		
 	}
 
 	// update trigger
 	public void updateTrigger(Trigger trigger) throws CosmException {
 		try {
 			HttpPut request = new HttpPut("http://api.cosm.com/v2/triggers/"+ trigger.getId() + ".json");
 			request.setEntity(new StringEntity(trigger.toJSONObject().toString()));
 			HttpResponse response = client.execute(request);			
 			StatusLine statusLine = response.getStatusLine();
 			String body = client.getBody(response);
 			if ( statusLine.getStatusCode() != 200 ) {
 				System.err.println(body);
 				if ( body.length() > 0 ) {
 					JSONObject ej = new JSONObject(body);
 					throw new CosmException(ej.getString("errors"));				
 				} else {
 					throw new CosmException(statusLine.toString());
 				}
 			}
 		} catch ( Exception e) {
 			throw new CosmException("Caught exception in update trigger: " + e.getMessage());
 		}
 	}
 	
 	// get user
 	public User getUser(String login) throws CosmException {
 		try {
 			HttpGet hr = new HttpGet("http://api.cosm.com/v2/users/"+login+".json");
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200) {
 				return CosmFactory.toUser(client.getBody(response));				
 			} else {
 				throw new CosmException(response.getStatusLine().toString());				
 			}									
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException("error in getUser");
 		}
 	}
 	
 	// get users
 	public User[] getUsers() throws CosmException {
 		try {
 			HttpGet hr = new HttpGet("http://api.cosm.com/v2/users.json");
 			HttpResponse response = client.execute(hr);
 			StatusLine statusLine = response.getStatusLine();
 			if ( statusLine.getStatusCode() == 200) {
 				return CosmFactory.toUsers(client.getBody(response));				
 			} else {
 				throw new CosmException(response.getStatusLine().toString());				
 			}									
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException("error in getUsers");
 		}
 	}
 	
 	// delete user
 	public void deleteUser(String login) throws CosmException {
 		try {
 			HttpDelete request = new HttpDelete("http://api.cosm.com/v2/users/"+ login + ".json");			
 			HttpResponse response = client.execute(request);
 			StatusLine statusLine = response.getStatusLine();
 			client.getBody(response);
 			if ( statusLine.getStatusCode() != 200 ) {
 				throw new HttpException(statusLine.toString());
 			}
 		} catch ( Exception e ) {
 			e.printStackTrace();
 			throw new CosmException(e.getMessage());
 		}			
 	}
 	
 	// update user
 	public void updateUser(String login,User user) throws CosmException {
 		try {
 			HttpPut request = new HttpPut("http://api.cosm.com/v2/users/"+ login + ".json");
 			request.setEntity(new StringEntity(user.toJSONObject().toString()));
 			HttpResponse response = client.execute(request);			
 			StatusLine statusLine = response.getStatusLine();
 			String body = client.getBody(response);
 			if ( statusLine.getStatusCode() != 200 ) {
 				System.err.println(body);
 				if ( body.length() > 0 ) {
 					JSONObject ej = new JSONObject(body);
 					throw new CosmException(ej.getString("errors"));				
 				} else {
 					throw new CosmException(statusLine.toString());
 				}
 			}
 		} catch ( Exception e) {
 			throw new CosmException("Caught exception in update trigger: " + e.getMessage());
 		}
 	}
 	
 	// create user
 	public void createUser(User user) throws CosmException {
 		try {
 			HttpPost request = new HttpPost("http://api.cosm.com/v2/users.json");
 			request.setEntity(new StringEntity(user.toJSONObject().toString()));
 			HttpResponse response = client.execute(request);			
 			StatusLine statusLine = response.getStatusLine();
 			String body = client.getBody(response);
 			if ( statusLine.getStatusCode() == 201 ) {
 				return;
 			} else {
 				if ((body!=null)&&(body.length() > 0 )) {					
 					JSONObject ej = new JSONObject(body);
 					throw new CosmException(ej.getString("errors"));				
 				} else {
 					throw new CosmException(statusLine.toString());
 				}
 			}
 		} catch ( Exception e) {
 			e.printStackTrace();
 			throw new CosmException("Caught exception in update createUser: " + e.getMessage());
 		}
 	}
 		
 	//TODO: get permissions
 	
 	//TODO: show permissions
 		
 }
