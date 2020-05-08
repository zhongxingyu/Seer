 package edu.stanford.junction.sample.partyware;
 
 import java.util.*;
 import org.json.JSONObject;
 import org.json.JSONException;
 import edu.stanford.junction.props2.*;
 import edu.stanford.junction.extra.JSONObjWrapper;
 
 public class PartyProp extends Prop {
 
 	// A client-only helper to prevent multiple votes for the same vid
 	private HashSet<String> voteHistory = new HashSet<String>();
 
 	public PartyProp(String propName, String propReplicaName, IPropState s){
 		super(propName, propReplicaName, s);
 	}
 
 	public PartyProp(String propName){
 		this(propName, propName + (new Random()).nextInt(), new PartyState());
 	}
 
 	public IProp newFresh(){
 		return new PartyProp(getPropName(), getPropReplicaName(), new PartyState());
 	}
 
 	public void forceChangeEvent(){
 		dispatchChangeNotification(EVT_ANY, null);
 	}
 
 	protected IPropState reifyState(JSONObject obj){
 		return new PartyState(obj);
 	}
 
 	protected JSONObject newAddObjOp(JSONObject item){
 		JSONObject obj = new JSONObject();
 		try{
 			obj.put("type", "addObj");
 			obj.put("item", item);
 		}catch(JSONException e){}
 		return obj;
 	}
 
 	protected JSONObject newDeleteObjOp(JSONObject item){
 		JSONObject obj = new JSONObject();
 		try{
 			obj.put("type", "deleteObj");
 			obj.put("itemId", item.optString("id"));
 		}catch(JSONException e){}
 		return obj;
 	}
 
 	protected JSONObject newDeleteObjOp(String itemId){
 		JSONObject obj = new JSONObject();
 		try{
 			obj.put("type", "deleteObj");
 			obj.put("itemId", itemId);
 		}catch(JSONException e){}
 		return obj;
 	}
 
 	protected JSONObject newVoteOp(String itemId, int count){
 		JSONObject obj = new JSONObject();
 		try{
 			obj.put("type", "vote");
 			obj.put("itemId", itemId);
 			obj.put("count", count);
 		}
 		catch(JSONException e){}
 		return obj;
 	}
 
 	protected void addObj(JSONObject item){
 		addOperation(newAddObjOp(item));
 	}
 
 	protected void deleteObj(JSONObject item){
 		addOperation(newDeleteObjOp(item));
 	}
 
 	protected void deleteObj(String itemId){
 		addOperation(newDeleteObjOp(itemId));
 	}
 
 
 	/////////////////////////
     // Conveniance helpers //
     /////////////////////////
 
 	public String getName(){
 		return withState(new IWithStateAction<String>(){
 				public String run(IPropState state){
 					return ((PartyState)state).getName();
 				}
 			});
 	}
 
 	public JSONObject getUser(final String id){
 		return withState(new IWithStateAction<JSONObject>(){
 				public JSONObject run(IPropState state){
 					return ((PartyState)state).getUser(id);
 				}
 			});
 	}
 
 	public List<JSONObject> getRelationships(){
 		return withState(new IWithStateAction<List<JSONObject>>(){
 				public List<JSONObject> run(IPropState state){
 					return ((PartyState)state).getRelationships();
 				}
 			});
 	}
 
 	public JSONObject getRelationship(final String fromId, final String toId){
 		return withState(new IWithStateAction<JSONObject>(){
 				public JSONObject run(IPropState state){
 					return ((PartyState)state).getRelationship(fromId, toId);
 				}
 			});
 	}
 
 	public Map<String,List<String>> computeShortestPaths(final String sourceId){
 		return withState(new IWithStateAction<Map<String,List<String>>>(){
 				public Map<String,List<String>> run(IPropState state){
 					return ((PartyState)state).computeShortestPaths(sourceId);
 				}
 			});
 	}
 
 	// Note: empty path means it's the path from self to self.
 	public String prettyPathString(final String selfId, final List<String> path){
 		if(path.isEmpty()){
 			return "It's you!";
 		}
 		else{
 			String pathStr = "";
 			String curId = selfId;
 			for(String id : path){
 				JSONObject rel = getRelationship(id, curId);
 				String relType = rel.optString("relType");
				JSONObject user = getUser(id);
				String name = user.optString("name");
 				String a = relType.matches("^[aeiou].+") ? "an" : "a";
 				if(curId.equals(selfId)){
 					pathStr = "You are " + a + " " + relType + " of " + name;
 				}
 				else{
 					pathStr = pathStr + ", who is " + a + " " + relType + " of " + name;
 				}
 				curId = id;
 			}
 			return pathStr + ".";
 		}
 	}
 
 	public List<JSONObject> getImages(){
 		return withState(new IWithStateAction<List<JSONObject>>(){
 				public List<JSONObject> run(IPropState state){
 					return ((PartyState)state).getImages();
 				}
 			});
 	}
 
 	public List<JSONObject> getUsers(){
 		return withState(new IWithStateAction<List<JSONObject>>(){
 				public List<JSONObject> run(IPropState state){
 					return ((PartyState)state).getUsers();
 				}
 			});
 	}
 
 	public List<JSONObject> getYoutubeVids(){
 		return withState(new IWithStateAction<List<JSONObject>>(){
 				public List<JSONObject> run(IPropState state){
 					return ((PartyState)state).getYoutubeVids();
 				}
 			});
 	}
 
 	public void updateUser(String userId, String name, String email, String imageUrl){
 		addObj(newUserObj(userId, name, email, imageUrl));
 	}
 
 	public void addImage(String userId, String url, String thumbUrl, String caption, long time){
 		addObj(newImageObj(userId, url, thumbUrl, caption, time));
 	}
 
 	public void upvoteVideo(String id){
 		if(!(voteHistory.contains(id))){
 			addOperation(newVoteOp(id, 1));
 			voteHistory.add(id);
 		}
 	}
 
 	public void downvoteVideo(String id){
 		if(!(voteHistory.contains(id))){
 			addOperation(newVoteOp(id, -1));
 			voteHistory.add(id);
 		}
 	}
 
 	public boolean alreadyVotedFor(String id){
 		return voteHistory.contains(id);
 	}
 
 	public void addRelationship(String[] relationships, String[] reverseRelationships, String fromUserId, String toUserId, String relType){
 		List<String> rels = Arrays.asList(relationships);
 		List<String> reverseRels = Arrays.asList(reverseRelationships);
 		if(!(rels.contains(relType))){
 			throw new IllegalStateException("Unknown relationship type: " + relType);
 		}
 		int index = rels.indexOf(relType);
 		String reverseRelType = reverseRels.get(index);
 		addObj(newRelationship(fromUserId, toUserId, relType));
 		addObj(newRelationship(toUserId, fromUserId, reverseRelType));
 	}
 
 	public void deleteRelationship(String fromUserId, String toUserId){
 		deleteObj(PartyState.relationshipId(fromUserId, toUserId));
 		deleteObj(PartyState.relationshipId(toUserId, fromUserId));
 	}
 
 	protected JSONObject newRelationship(String fromUserId, String toUserId, String relType){
 		String id = PartyState.relationshipId(fromUserId, toUserId);
 		JSONObject obj = new JSONObject();
 		try{
 			obj.put("id", id);
 			obj.put("type", "relationship");
 			obj.put("from", fromUserId);
 			obj.put("to", toUserId);
 			obj.put("relType", relType);
 		}
 		catch(JSONException e){}
 		return obj;
 	}
 
 	public void addYoutube(String userId, String videoId, String thumbUrl, String caption, long time){
 		addObj(newYoutubeObj(userId, videoId, thumbUrl, caption, time));
 	}
 
 	protected JSONObject newImageObj(String userId, String url, String thumbUrl, String caption, long time){
 		JSONObject obj = newHTTPResourceObj("image", userId, url, caption, time);
 		try{
 			obj.put("thumbUrl", thumbUrl);
 		}catch(JSONException e){}
 		return obj;
 	}
 
 	protected JSONObject newYoutubeObj(String userId, String videoId, 
 									   String thumbUrl, String caption, long time){
 		JSONObject obj = newHTTPResourceObj("youtube", userId, null, caption, time);
 		try{
 			obj.put("videoId", videoId);
 			obj.put("thumbUrl", thumbUrl);
 			obj.put("votes", 0);
 		}catch(JSONException e){}
 		return obj;
 	}
 
 	protected JSONObject newHTTPResourceObj(String type, String userId, 
 											String url, String caption, long time){
 		JSONObject obj = new JSONObject();
 		try{
 			obj.put("id", (UUID.randomUUID()).toString());
 			obj.put("type", type);
 			obj.put("url", url);
 			obj.put("time", time);
 			obj.put("caption", caption);
 			obj.put("owner", userId);
 		}catch(JSONException e){}
 		return obj;
 	}
 
 	protected JSONObject newUserObj(String userId, String name, String email, String imageUrl) {
 		JSONObject obj = new JSONObject();
 		try{
 			obj.put("id", userId);
 			obj.put("type", "user");
 			obj.put("name", name);
 			obj.put("email", email);
 			obj.put("imageUrl", imageUrl);
 		}catch(JSONException e){}
 		return obj;
 	}
 
 
 //////////////////////////
 // The State definition //
 //////////////////////////
 
 	static class PartyState implements IPropState {
 
 		private HashMap<String, JSONObject> objects = new HashMap<String, JSONObject>();
 		private String name = "Unnamed Party";
 		private int hashCode = 0;
 
 		public PartyState(PartyState other){
 			if(other != null){
 				this.name = other.getName();
 				Iterator it = other.objects.entrySet().iterator();
 				while (it.hasNext()) {
 					Map.Entry<String,JSONObject> pair = 
 						(Map.Entry<String,JSONObject>)it.next();
 					if(pair.getValue() instanceof JSONObjWrapper){
 						JSONObjWrapper obj = (JSONObjWrapper)pair.getValue();
 						objects.put(pair.getKey(), (JSONObject)obj.clone());
 					}
 				}
 			}
 		}
 
 		public PartyState(JSONObject obj){
 			this.name = obj.optString("name");
 			JSONObject jsonObjects = obj.optJSONObject("objects");
 			if(jsonObjects != null){
 				Iterator it = jsonObjects.keys();
 				while(it.hasNext()){
 					Object key = it.next();
 					Object val = jsonObjects.opt(key.toString());
 					if(val instanceof JSONObject){
 						JSONObject eaObj = (JSONObject)val;
 						String id = eaObj.optString("id");
 						objects.put(id, new JSONObjWrapper(eaObj));
 					}
 				}
 			}
 		}
 
 		public PartyState(){
 			this((PartyState)null);
 		}
 
 		public static String relationshipId(String fromUserId, String toUserId){
 			return "_" + fromUserId + "_to_" + toUserId + "_";
 		}
 
 		public String getName(){
 			return this.name;
 		}
 		
 		public IPropState applyOperation(JSONObject op){
 			String type = op.optString("type");
 			if(type.equals("addObj")){
 				JSONObject item = op.optJSONObject("item");
 				String id = item.optString("id");
 				objects.put(id, (new JSONObjWrapper(item)));
 			}
 			else if(type.equals("deleteObj")){
 				String id = op.optString("itemId");
 				objects.remove(id);
 			}
 			else if(type.equals("setName")){
 				String name = op.optString("name");
 				this.name = name;
 			}
 			else if(type.equals("vote")){
 				String id = op.optString("itemId");
 				int count = op.optInt("count");
 				JSONObject o = objects.get(id);
 				if(o != null){
 					int cur = o.optInt("votes");
 					try{
 						o.put("votes", cur + count);
 					}
 					catch(JSONException e){
 						e.printStackTrace(System.err);
 					}
 				}
 				else{
 					System.err.println("Couldn't find object for id: " + id);
 				}
 			}
 			else{
 				throw new IllegalStateException("Unrecognized operation: " + type);
 			}
 			return this;
 		}
 
 		protected void updateHashCode(){
 			this.hashCode = 0;
 			Iterator<JSONObject> it = objects.values().iterator();
 			while (it.hasNext()){
 				JSONObject ea = it.next();
 				String id = ea.optString("id");
 				this.hashCode ^= id.hashCode();
 			}
 			this.hashCode ^= this.name.hashCode();
 		}
 
 		public int hashCode(){
 			return this.hashCode;
 		}
 
 		public JSONObject toJSON(){
 			JSONObject obj = new JSONObject();
 			JSONObject jsonObjects = new JSONObject();
 			try{
 				obj.put("name", this.name);
 				obj.put("objects", jsonObjects);
 			}
 			catch(JSONException e){
 				e.printStackTrace(System.err);
 				throw new IllegalStateException("toJson failed in PartyProp!");
 			}
 			Iterator it = objects.entrySet().iterator();
 			while (it.hasNext()) {
 				Map.Entry<String,JSONObject> pair = 
 					(Map.Entry<String,JSONObject>)it.next();
 				try{
 					JSONObjWrapper wrapper = (JSONObjWrapper)pair.getValue();
 					jsonObjects.put(String.valueOf(pair.getKey()), 
 									wrapper.getRaw());
 				}
 				catch(JSONException e){
 					e.printStackTrace(System.err);
 					throw new IllegalStateException("toJson failed in PartyProp!");
 				}
 			}
 			return obj;
 		}
 
 		public JSONObject getUser(String id){
 			return objects.get(id);
 		}
 
 		public List<JSONObject> getImages(){
 			List<JSONObject> images = getObjectsOfType("image");
 			sortByTime(images, true);
 			return Collections.unmodifiableList(images);
 		}
 
 		public List<JSONObject> getRelationships(){
 			List<JSONObject> rels = getObjectsOfType("relationship");
 			return Collections.unmodifiableList(rels);
 		}
 
 		public JSONObject getRelationship(String fromId, String toId){
 			return objects.get(relationshipId(fromId, toId));
 		}
 
 		class QEl implements Comparable<QEl>{
 			final public String id;
 			final public long dist;
 
 			public QEl(String id, long dist){
 				this.id = id;
 				this.dist = dist;
 			}
 
 			@Override
 			public int compareTo(QEl el){
 				if(this.dist < el.dist) return -1;
 				if(this.dist > el.dist) return 1;
 				return 0;
 			}
 			@Override
 			public int hashCode(){
 				return id.hashCode();
 			}
 			@Override
 			public boolean equals(Object el){
 				if(el instanceof QEl){
 					QEl e = (QEl)el;
 					return this.id.equals(e.id);
 				}
 				else{
 					return false;
 				}
 			}
 			@Override
 			public String toString(){
 				return id + "@" + dist;
 			}
 		}
 
 		class Q extends PriorityQueue<QEl>{
 			@Override
 			public boolean offer(QEl el) {
 				if (contains(el)) {
 					return false;
 				} else {
 					return super.offer(el);
 				}
 			}
 			public boolean update(QEl el, long value) {
 				if (!contains(el)) {
 					return false;
 				} else {
 					remove(el);
 					offer(new QEl(el.id, value));
 					return true;
 				}
 			}
 
 			public boolean remove(Object el){
 				// XXX This is a work-around for bug 6207984 on oracle's java bug list.
 				// would like to just use q.remove(source)
 				return removeAll(Collections.singletonList(el));
 			}
 		}
 
 		// Result should map each userId id to paths consisting of the chain of userIds
 		// starting at sourceId (non-inclusive) and continuing to id (inclusive).
 		//
 		// Run Dijkstra's Alg. on relationship graph. All edges have cost 1
 		public Map<String,List<String>> computeShortestPaths(final String sourceId){
 
 			Map<String,Long> dist = new HashMap<String,Long>();
 			Map<String,String> previous = new HashMap<String,String>();
 			Map<String,Set<String>> neighbors = new HashMap<String,Set<String>>();
 			Q q = new Q();
 
 
 			// Dijkstra initialization
 
 			Iterator<JSONObject> it = objects.values().iterator();
 			while (it.hasNext()) {
 				JSONObject ea = it.next();
 				String type = ea.optString("type");
 
 				// For each edge..
 				if(type.equals("relationship")){
 					String fromId = ea.optString("from");
 					String toId = ea.optString("to");
 
 					// Add each node to q
 					// Init the dist structure
 					q.offer(new QEl(fromId, (long)Integer.MAX_VALUE));
 					q.offer(new QEl(toId, (long)Integer.MAX_VALUE));
 
 					dist.put(fromId, (long)Integer.MAX_VALUE);
 					dist.put(toId, (long)Integer.MAX_VALUE);
 				
 					// Update neighbors of from node 
 					// (relations are one-directional)
 					Set<String> neibs = neighbors.get(fromId);
 					if(neibs == null) neibs = new HashSet<String>();
 					neibs.add(toId);
 					neighbors.put(fromId, neibs);
 				}
 			}
 
 			// Source has 0 cost
 			QEl source = new QEl(sourceId, 0L);
 			q.remove(source);
 			q.offer(source);
 			dist.put(sourceId, 0L);
 
 			// The null path
 			previous.put(sourceId, sourceId);
 
 			// Dijkstra run..
 			while(!(q.isEmpty())){
 				QEl u = q.poll();
 				System.out.println("looking at " + u);
 				if(u.dist == (long)Integer.MAX_VALUE){
 					break;
 				}
 				Set<String> neibs = neighbors.get(u.id);
 				if(neibs != null){
 					for(String vId : neibs){
 						QEl tmp = new QEl(vId, 0L);
 						System.out.println("looking neighbor " + tmp);
 						if(q.contains(tmp)){
 							
 							// Note, edge cost is constant 1
 							long alt = dist.get(u.id) + 1;
 							
 							if(alt < dist.get(vId)){
 								dist.put(vId, alt);
 								q.update(tmp, alt);
 								previous.put(vId, u.id);
 							}
 						}
 					}
 				}
 			}
 
 			// Reconstruct the result paths from contents of previous.
 			Map<String,List<String>> result = new HashMap<String,List<String>>();
 			for (Map.Entry<String, String> entry : previous.entrySet()){
 				ArrayList<String> path = new ArrayList<String>();
 				String destination = entry.getKey();
 				String id = destination;
 				System.out.println("[ ");
 				while(!(id.equals(sourceId))){
 					System.out.println("entry on path " + id);
 					path.add(id);
 					id = previous.get(id);
 				}
 				System.out.println("]");
 				Collections.reverse(path);
 				result.put(destination, path);
 			}
 			return result;
 		}
 
 		public List<JSONObject> getUsers(){
 			List<JSONObject> users = getObjectsOfType("user");
 			Collections.sort(users, new Comparator<JSONObject>(){
 					public int compare(JSONObject o1, JSONObject o2) {
 						return o2.optString("name").compareTo(o1.optString("name"));
 					}
 				});
 			return Collections.unmodifiableList(users);
 		}
 
 		public List<JSONObject> getYoutubeVids(){
 			List<JSONObject> vids = getObjectsOfType("youtube");
 			sortByVotes(vids, true);
 			return Collections.unmodifiableList(vids);
 		}
 
 		protected List<JSONObject> getObjectsOfType(String tpe){
 			ArrayList<JSONObject> objs = new ArrayList<JSONObject>();
 			Iterator<JSONObject> it = objects.values().iterator();
 			while (it.hasNext()) {
 				JSONObject ea = it.next();
 				String type = ea.optString("type");
 				if(type.equals(tpe)){
 					objs.add(ea);
 				}
 			}
 			return objs;
 		}
 
 
 		private void sortByTime(List<JSONObject> input, final boolean newToOld){
 			Collections.sort(input, new Comparator<JSONObject>(){
 					public int compare(JSONObject o1, JSONObject o2) {
 						if(newToOld){
 							return (int)(o2.optLong("time") - o1.optLong("time"));
 						}
 						else{
 							return (int)(o1.optLong("time") - o2.optLong("time"));
 						}
 					}
 				});
 		}
 
 		private void sortByVotes(List<JSONObject> input, final boolean highToLow){
 			Collections.sort(input, new Comparator<JSONObject>(){
 					public int compare(JSONObject o1, JSONObject o2) {
 						if(highToLow){
 							int v1 = (int)(o1.optInt("votes"));
 							int v2 = (int)(o2.optInt("votes"));
 							long t1 = (o1.optLong("time"));
 							long t2 = (o2.optLong("time"));
 							if(v1 == v2) return (int)(t1 - t2);
 							else return (v2 - v1);
 						}
 						else{
 							return (int)(o1.optInt("votes") - o2.optInt("votes"));
 						}
 					}
 				});
 		}
 
 
 		public IPropState copy(){
 			return new PartyState(this);
 		}
 	}
 
 }
