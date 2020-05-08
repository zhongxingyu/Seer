 package com.chap.memo.memoNodes;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.regex.Pattern;
 
 import net.sf.json.JSONObject;
 import com.eaio.uuid.UUID;
 
 public class MemoNode implements Comparable<MemoNode> {
 	public static UUID ROOT = new UUID("00000000-0000-002a-0000-000000000000");
 	
 	MemoReadBus readBus = MemoReadBus.getBus();
 	MemoWriteBus writeBus = MemoWriteBus.getBus();
 	long lastUpdate= new Date().getTime();
 	
 	private UUID uuid;
 	private NodeValue value = null;
 	private final ArcList parents;
 	private final ArcList children;
 	
 	public static MemoNode getRootNode() {
 		MemoReadBus readBus = MemoReadBus.getBus();
 		MemoNode result = readBus.find(ROOT);
 		if (result == null) {
 			result = new MemoNode(ROOT, "root");
 		}
 		return result;
 	}
 	
 	@Override
 	public int compareTo(MemoNode o) {
 		if (this.getId().equals(o.getId())) return 0;
 		return (int) ((this.getTimestamp() - o.getTimestamp())%1);
 	}
 	
 	@Override
 	public int hashCode(){
 		return this.getId().hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object o){
 		if (o instanceof MemoNode) {
 			return this.getId().equals(((MemoNode)o).getId());
 		} else {
 			return false;
 		}
 	}
 
 	public MemoNode(UUID uuid){
 		this.uuid=uuid;
 		this.parents=new ArcList(uuid,0);
 		this.children=new ArcList(uuid,1);		
 	}
 	public MemoNode(NodeValue value, ArcList parents, ArcList children){
 		this.uuid = value.getId();
 		this.value=value;
 		this.parents=parents;
 		this.children=children;
 	}
 	public MemoNode(UUID id, byte[] value, UUID[] children, UUID[] parents){
 		this.uuid=id;
 		this.value=writeBus.store(id, value);
 		this.parents=new ArcList(id,0);
 		this.children=new ArcList(id,1);
 		for (UUID child: children){
 			this.addChild(child);
 		}
 		for (UUID parent: parents){
 			this.addParent(parent);
 		}
 	}
 	public MemoNode(NodeValue value){
		if (value!=null){
			this.uuid=value.getId();
			this.value=value;
		} else {
			this.uuid=new UUID();
			this.value=null;
		}
 		this.parents=new ArcList(this.uuid,0);
 		this.children=new ArcList(this.uuid,1);
 	}
 	public MemoNode(byte[] value){
 		this.uuid=new UUID();
 		this.value=writeBus.store(this.uuid, value);
 		this.parents=new ArcList(this.uuid,0);
 		this.children=new ArcList(this.uuid,1);
 	}
 	public MemoNode(String value){
 		this.uuid=new UUID();
 		this.value=writeBus.store(this.uuid, value.getBytes());
 		this.parents=new ArcList(this.uuid,0);
 		this.children=new ArcList(this.uuid,1);
 	}
 	public MemoNode(UUID uuid,byte[] value){
 		this.uuid=uuid;
 		this.value=writeBus.store(uuid, value);
 		this.parents=new ArcList(this.uuid,0);
 		this.children=new ArcList(this.uuid,1);
 	}
 	public MemoNode(UUID uuid,String value){
 		this.uuid=uuid;
 		this.value=writeBus.store(uuid, value.getBytes());		
 		this.parents=new ArcList(this.uuid,0);
 		this.children=new ArcList(this.uuid,1);
 	}
 	public void update(byte[] value){
 		this.value=writeBus.store(this.getId(), value);
 	}
 	public void update(String value){
 		this.value=writeBus.store(this.getId(), value.getBytes());
 	}
 	public void addParent(UUID parent){
 		parents.addNode(parent);
 	}
 	public void addChild(UUID child){
 		children.addNode(child);
 	}
 	public void delParent(UUID parent){
 		parents.delNode(parent);
 	}
 	public void delChild(UUID child){
 		children.delNode(child);
 	}
 	public void addParent(MemoNode parent){
 		addParent(parent.getId());
 	}
 	public void addChild(MemoNode child){
 		addChild(child.getId());		
 	}
 	public void delParent(MemoNode parent){
 		delParent(parent.getId());
 	}
 	public void delChild(MemoNode child){
 		delChild(child.getId());			
 	}
 
 	public byte[] getValue(){
 		if (this.value == null || readBus.valueChanged(lastUpdate)){
 			this.value=readBus.getValue(this.uuid);
 			lastUpdate=new Date().getTime();
 		}
 		return this.value == null?null:this.value.getValue();
 	}
 	public String getStringValue(){
 		byte[] res = getValue();
 		if (res != null) return new String(getValue());
 		return "";
 	}
 	public byte[] valueAt(long timestamp){
 		NodeValue oldValue = readBus.getValue(getId(), timestamp);
 		return oldValue.getValue();
 	}
 	public ArrayList<MemoNode> history(){
 		ArrayList<MemoNode> result = readBus.findAll(getId());
 		if (!result.get(result.size()-1).equals(this)){
 			result.add(this);	
 		}
 		return result;
 	}
 	public UUID getId(){
 		return this.uuid;
 	}
 	public long getTimestamp(){
 		return Math.max(this.value.getTimestamp_long(),Math.max(this.children.getTimestamp_long(),this.parents.getTimestamp_long()));
 	}
 	public long getValueTimestamp(){
 		return this.value.getTimestamp_long();
 	}
 	public long getParentTimestamp(){
 		return this.parents.getTimestamp_long();
 	}
 	public long getChildTimestamp(){
 		return this.children.getTimestamp_long();
 	}
 	public ArrayList<MemoNode> getChildren(){
 		return this.children.getNodes();
 	}
 
 	public ArrayList<MemoNode> getParents(){
 		return this.parents.getNodes();
 	}
 
 	public ArrayList<MemoNode> getChildrenByStringValue(String value, int topx){
 		ArrayList<MemoNode> result = new ArrayList<MemoNode>(
 				this.children.getLength());
 		for (MemoNode child : getChildren()) {
 			if (child.getStringValue().equals(value)) {
 				result.add(child);
 				if (topx > 0 && result.size() >= topx)
 					return result;
 			}
 		}
 		return result;
 	}
 	public ArrayList<MemoNode> getChildrenByRegEx(Pattern regex, int topx){
 		ArrayList<MemoNode> result = new ArrayList<MemoNode>(
 				this.children.getLength());
 		for (MemoNode child : getChildren()) {
 			if (regex.matcher(child.getStringValue()).matches()) {
 				result.add(child);
 				if (topx > 0 && result.size() >= topx)
 					return result;
 			}
 		}
 		return result;
 	}
 	public ArrayList<MemoNode> getChildrenByRange(int lower, int upper, int topx){
 		//TODO: store integers differently? Not as String...
 		ArrayList<MemoNode> result = new ArrayList<MemoNode>(
 				this.children.getLength());
 		for (MemoNode child : getChildren()) {
 			try {
 				int value = Integer.parseInt(child.getStringValue());
 				if (value >= lower && value <= upper) {
 					result.add(child);
 					if (topx > 0 && result.size() >= topx)
 						return result;
 				}
 			} catch (NumberFormatException e) {
 			}
 		}
 		return result;
 	}
 	public String getPropertyValue(String propName){
 		ArrayList<MemoNode> properties = getChildrenByStringValue(propName, 1);
 		if (properties.size() == 1) {
 			ArrayList<MemoNode> values = properties.get(0).getChildren();
 			if (values.size() != 1)
 				System.out.println("Warning, property with multiple values");
 			if (values.size() >= 1)
 				return values.get(0).getStringValue();
 		}
 		return "";
 	}
 	public void setPropertyValue(String propName, String propValue){
 		ArrayList<MemoNode> properties = getChildrenByStringValue(propName, 1);
 		switch (properties.size()) {
 		case 0:
 			MemoNode value = new MemoNode(propValue.getBytes());
 			MemoNode property = new MemoNode(propName.getBytes());
 			property.addChild(value.getId());
 			this.addChild(property.getId());
 		case 1:
 			ArrayList<MemoNode> values = properties.get(0).getChildren();
 			if (values.size() == 1) {
 				values.get(0).update(propValue.getBytes());
 			}
 			// explicit no-break
 		default:
 			System.out
 					.println("Error, incorrect properties found, skipping setPropertyValue("
 							+ propName + "," + propValue + ")!");
 		}
 	}
 	private class StepState{
 		
 		private boolean matched = false;
 		public StepState(boolean matched,String reason,MemoQuery query,MemoNode toCompare){
 			//System.out.println(toCompare.getValue()+"/"+query.value+" -> returning: "+matched+" reason:"+reason);
 			this.setMatched(matched);
 		}
 		public boolean isMatched() {
 			return matched;
 		}
 		public void setMatched(boolean matched) {
 			this.matched = matched;
 		}
 	}
 	
 	private StepState doStep(boolean preamble, MemoQuery query, MemoNode toCompare,
 						     ArrayList<MemoNode> results, HashSet<MemoNode> seenNodes, 
 						     ArrayList<MemoNode> patterns, int topX, HashMap<String,String> arguments){
 		
 		MemoNode step = query.node;
 		//System.out.println("checking node:" + toCompare.getValue() + "/" + query.value + "("+preamble+")");
 
 		if (!query.match(toCompare)) return new StepState(false,"Node doesn't match.",query,toCompare);
 		if (seenNodes.contains(toCompare)) return new StepState(true,"Loop/Multipath detected",query,toCompare);
 		if (preamble) {
 			for (MemoNode pattern : patterns){
 				StepState res = doStep(false,MemoQuery.parseQuery(pattern.getChildren().get(0), arguments),
 									   toCompare,null,new HashSet<MemoNode>(),null,0,arguments);
 				if (res.matched){
 					results.add(toCompare);
 					return new StepState(true,"Node matches pattern! Added to result, no need to search deeper.",query,toCompare);
 				}
 			}
 		}
 		seenNodes.add(toCompare);
 		
 		ArrayList<MemoNode> nextPats = step.getChildren();
 		int toMatchNo = nextPats.size();
 		if (toMatchNo == 0) return new StepState(true,"End of pattern",query,toCompare);
 		
 		ArrayList<MemoNode> children = toCompare.getChildren();		
 		if (!preamble && children.size() < toMatchNo) return new StepState(false,"Too little children for pattern",query,toCompare);
 		
 		ArrayList<MemoQuery> queries = new ArrayList<MemoQuery>(toMatchNo);
 		HashSet<MemoQuery> foundQueries = new HashSet<MemoQuery>(toMatchNo);
 		for (MemoNode nextPat : nextPats) {
 			queries.add(MemoQuery.parseQuery(nextPat, arguments));
 		}
 		MemoQuery[] queryArray = { new MemoQuery() };
 		queryArray = queries.toArray(queryArray);
 		Arrays.sort(queryArray);
 		
 		for (MemoNode child : children) {
 			for (MemoQuery iQuery : queryArray) {
 				if (foundQueries.contains(iQuery)) continue;
 				StepState res = doStep(preamble,iQuery,child,results,seenNodes,patterns,topX, arguments);
 				
 				if (topX > 0 && results.size() >= topX) return new StepState(true,"TopX results reached, returning!",query,toCompare);
 				if (preamble || !res.isMatched()) continue;
 				//Return on fully matched pattern
 				foundQueries.add(iQuery);
 				if (foundQueries.size() == queryArray.length) return new StepState(true,"Pattern fully matched",query,toCompare);
 			}
 		}
 		if (preamble) return new StepState(true,"preamble return.",query,toCompare);
 		return new StepState(false,"Pattern didn't match entirely.",query,toCompare);
 	}
 
 	public ArrayList<MemoNode> search(ArrayList<MemoNode> preambles,
 			ArrayList<MemoNode> patterns, int topx, HashMap<String,String> arguments) {
 
 		ArrayList<MemoNode> result = new ArrayList<MemoNode>(topx>0?Math.min(200,topx):200);
 		HashSet<MemoNode> seenNodes = new HashSet<MemoNode>(200);
 		
 		if (patterns.size() <= 0) {
 			System.out.println("Warning, empty algorithm used.");
 			return result;
 		}
 		
 		for (MemoNode preamble : preambles) {
 			doStep(true,MemoQuery.parseQuery(preamble.getChildren().get(0),arguments),(MemoNode) this,result,seenNodes,patterns,topx,arguments);
 		}
 		return result;
 	}
 
 	public ArrayList<MemoNode> search(MemoNode algorithm, int topx, HashMap<String,String> arguments) {
 		ArrayList<MemoNode> preambles = algorithm.getChildrenByStringValue(
 				"PreAmble", -1);
 		ArrayList<MemoNode> patterns = algorithm.getChildrenByStringValue("Pattern",
 				-1);
 		return this.search(preambles, patterns, topx, arguments);
 	}
 
 	public ArrayList<MemoNode> search(MemoNode preamble, MemoNode pattern,
 			int topx, HashMap<String,String> arguments) {
 		ArrayList<MemoNode> preambles = new ArrayList<MemoNode>(1);
 		preambles.add(preamble);
 		ArrayList<MemoNode> patterns = new ArrayList<MemoNode>(1);
 		patterns.add(pattern);
 		return this.search(preambles, patterns, topx, arguments);
 	}
 
 	public String toJSONString(int depth){
 		JSONTuple tuple = this.toJSON(depth);
 		JSONObject result = new JSONObject().
 							element("nodes", tuple.nodes).
 							element("links",tuple.links);
 		return result.toString();
 	}
 
 	public JSONTuple toJSON(int depth) {
 		JSONTuple result = new JSONTuple();
 		
 		ArrayList<MemoNode> children = this.getChildren();
 		if (depth-- > 0) {
 			for (MemoNode child : children) {
 				result = result.merge(child.toJSON(depth));
 				result.links.add(
 						new JSONObject().
 						element("from", this.getId().toString()).
 						element("to",child.getId().toString()));
 			}
 		}
 
 		result.nodes.add(new JSONObject().
 						 element("id",this.getId().toString()).
 						 element("title", this.getStringValue()));
 
 		return result;
 	}
 }
 class MemoQuery implements Comparable<MemoQuery> {
 	public enum Type {
 		Equal, Regex, Range, Any
 	};
 
 	static HashMap<MemoNode, MemoQuery> queryCache = new HashMap<MemoNode, MemoQuery>();
 
 	MemoNode node = null;
 	Type type = Type.Equal;
 	String value = "";
 	boolean hasArg=false;
 	java.util.regex.Pattern regex = null;
 	int lower = 0;
 	int upper = 0;
 
 	@Override
 	public int compareTo(MemoQuery arg0) {
 		return this.type.compareTo(arg0.type);
 	}
 
 	public static MemoQuery parseQuery(MemoNode step,HashMap<String,String> arguments) {
 		if (queryCache.containsKey(step)) {
 			MemoQuery cached = queryCache.get(step);
 			if (!cached.hasArg){
 				return cached;
 			};
 		}
 		MemoQuery result = new MemoQuery();
 		result.node = step;
 		String query = step.getStringValue();
 		if (query.equals("any")) {
 			result.type = MemoQuery.Type.Any;
 		} else if (query.startsWith("equal;")) {
 			result.type = MemoQuery.Type.Equal;
 			result.value = query.substring(6);
 			if (result.value.startsWith("arg(")){
 				result.value=arguments.get(result.value.substring(4,result.value.length()-1));
 				//System.out.println("Setting arg value to:"+result.value);
 				result.hasArg=true;
 			}
 		} else if (query.startsWith("regex;")) {
 			result.type = MemoQuery.Type.Regex;
 			result.regex = Pattern.compile(query.substring(6));
 		} else if (query.startsWith("range:")) {
 			result.type = MemoQuery.Type.Range;
 			String[] parts = query.substring(6).split(";");
 			result.lower = Integer.parseInt(parts[0]);
 			result.upper = Integer.parseInt(parts[0]);
 		} else {
 			result.type = MemoQuery.Type.Equal;
 			result.value = query;
 			if (result.value.startsWith("arg(")){
 				result.value=arguments.get(result.value.substring(4,result.value.length()-1));
 				result.hasArg=true;
 			}
 		}
 		if (!result.hasArg) queryCache.put(step, result);
 		return result;
 	}
 
 	public boolean match(MemoNode node) {
 		switch (this.type) {
 		case Equal:
 			//System.out.println("Checking "+node.getId()+":"+node.getStringValue()+" against "+this.value);
 			return node.getStringValue().equals(this.value);
 		case Regex:
 			return this.regex.matcher(node.getStringValue()).matches();
 		case Range:
 			try {
 				int value = Integer.parseInt(node.getStringValue());
 				return value >= this.lower && value <= this.upper;
 			} catch (Exception e) {
 				return false;
 			}
 		case Any:
 			return true;
 		default:
 			System.out.println("MemoQuery: Unknown enum value???");
 			return false;
 		}
 	}
 }
 
