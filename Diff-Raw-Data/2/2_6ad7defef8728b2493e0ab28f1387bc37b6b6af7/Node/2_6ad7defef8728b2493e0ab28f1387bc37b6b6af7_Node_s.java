 package ch.uzh.agglorecommender.clusterer.treecomponent;
 
 import java.io.Serializable;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import ch.uzh.agglorecommender.clusterer.treesearch.ClassitMaxCategoryUtilitySearcher;
 
 
 public class Node implements INode, Comparable<Node>, Serializable {
 
 	/**
 	 * Determines if a de-serialized file is compatible with this class.
 	 * <br>
 	 * <br>
 	 * Maintainers must change this value if and only if the new version
 	 * of this class is not compatible with old versions.
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Node id counter.
 	 */
 	private static long idCounter = 0;
 	
 	/**
 	 * The unique id of this node.
 	 */
 	private long id = idCounter++;
 
 	/**
 	 * The node type.
 	 */
 	private final ENodeType nodeType;
 
 	/**
 	 * The attributes of the node.
 	 * Stores for each node that is an
 	 * attribute of this node a mapping
 	 * to an IAttribute object.
 	 */
 	private Map<INode, IAttribute> attributes;
 	
 	/**
 	 * The meta info of the node.
 	 */
 	private Map<String, String> meta;
 
 	/**
 	 * The children of this node.
 	 */
 	private Set<INode> children = new HashSet<INode>();
 
 	/**
 	 * The parent of this node.
 	 * Is null s long as the node was not merged.
 	 */
 	private INode parent = null;
 	
 	/**
 	 * The id of the represented data item in the data set.
 	 * Is null if the size of this cluster is > 1.
 	 */
 	private final Integer dataSetId;
 
 	/**
 	 * The category utility of the merge of this nodes children or 1 if node is leaf
 	 */
 	private double categoryUtility;
 
 	/**
 	 * All nodes with recently changed attributes.
 	 */
 	private static Set<INode> dirtySet = new HashSet<INode>();
 
 	public Node(ENodeType nodeType, int dataSetId, Map<String,String> meta) {
 		this.nodeType = nodeType;
 		this.dataSetId = dataSetId;
 		categoryUtility = 1.0;
 		this.meta = meta;
 	}
 	
 	/**
 	 * Node constructor
 	 * @param nodeType Type of the node
 	 * @param children List of children
 	 * @param attributes Map of INode and IAttributes
 	 */
 	public Node(ENodeType nodeType, Collection<INode> children, Map<INode, IAttribute> attributes, double categoryUtility, Map<String, String> meta) {
 		this.nodeType = nodeType;
 		if (children != null) {
 			for (INode child : children) {
 				this.children.add(child);
 				child.setParent(this);
 			}	
 		}
 		this.attributes = attributes;
 		this.dataSetId = null;
 		this.categoryUtility = categoryUtility;
 		this.meta = meta;
 	}
 
 	@Override
 	public String getAttributesString() {
 
 		List<INode> keyList = new ArrayList<INode>(attributes.keySet());
 		Collections.sort(keyList, new NodeIdComparator());
 		String s = "";
 		for (INode node : keyList) {
 			s = s.concat(node.toString()).concat(": ").concat(attributes.get(node).toString()).concat(";\t");
 		}
 
 		if (s.length() == 0) {
 			return "no_attributes";
 		} else {
 			return s.substring(0, s.length()-1);
 		}
 	}
 
 	public Map<String, String> getMeta() {
 		return meta;
 	}
 
 	@Override
 	public int compareTo(Node o) {
 		return ((Long)this.getId()).compareTo((Long)o.getId());
 	}
 
 	@Override
 	public Iterator<INode> getChildren() {
 		return children.iterator();
 	}
 
 	@Override
 	public boolean isChild(INode possibleChild) {
 		return children.contains(possibleChild);
 	}
 
 	@Override
 	public INode getParent() {
 		return parent;
 	}
 
 	@Override
 	public void addChild(INode child) {
 		this.children.add(child);
 		child.setParent(this);
 	}
 
 	@Override
 	public boolean removeChild(INode child) {
 		if (children.remove(child)) {
 			child.setParent(null);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public INode setParent(INode parent) {
 		INode prevP = this.parent;
 		this.parent = parent;
 		return prevP;
 	}
 
 	@Override
 	public boolean isLeaf() {
 		if (this.children.isEmpty()) return true;
 		return false;
 	}
 
 	@Override
 	public boolean isRoot() {
 		if (this.parent == null) return true;
 		return false;
 	}
 
 	@Override
 	public void setAttributes(Map<INode, IAttribute> movies) {
 		dirtySet.add(this);
 		this.attributes = movies;
 	}
 
 	@Override
 	public Set<INode> getAttributeKeys() {
 		return Collections.unmodifiableSet(attributes.keySet());
 	}
 
 	@Override
 	public IAttribute getAttributeValue(INode node) {
 		return attributes.get(node);
 	}
 	
 //	@Override
 //	public String getAttributesType() {
 //		// hack
 //		String type = "";
 //		Set<INode> attributeKeys = attributes.keySet();
 //		for(INode attributeKey : attributeKeys) {
 //			type = attributes.get(attributeKey).get;
 //		}
 //		return type;
 //	}
 
 	@Override
 	public String toString() {
 		return getClass().getSimpleName().concat(" Node").concat(" ").concat(String.valueOf(id));
 	}
 
 	@Override
 	public long getId() {
 		return id;
 	}
 
 	@Override
 	public void addAttribute(INode node, IAttribute attribute) {
 		dirtySet.add(this);
 		attributes.put(node, attribute);		
 	}
 
 	@Override
 	public ENodeType getNodeType() {
 		return nodeType;
 	}
 
 	@Override
 	public boolean hasAttribute(INode attribute) {
 		return attributes.containsKey(attribute);
 	}
 
 	@Override
 	public IAttribute removeAttribute(INode attribute) {
 		dirtySet.add(this);
 		return attributes.remove(attribute);
 
 	}	
 
 	@Override
 	public int getChildrenCount() {
 		return children.size();
 
 	}
 
 	@Override
 	public int getNumberOfNodesInSubtree() {
 		int res = children.size();
 		for (INode child : children) {
 			res += child.getNumberOfNodesInSubtree();
 		}
 		return res;
 	}
 
 	@Override
 	public int getNumberOfLeafNodes() {
 		if (isLeaf()) return 1;
 		int sum = 0;
 		for (INode child : children) {
 			sum += child.getNumberOfLeafNodes();
 		}
 		return sum;
 	}
 	
 	@Override
 	public String getAttributeHTMLLabelString() {
 		DecimalFormat formater = new DecimalFormat("#.####");
 		String description = "<html><head><style>td { width:40px; border:1px solid black; text-align: center; }</style></head>"
 				+ "<body> Node: "
 				+ getId()
 				+ "<br> data set id: "
 				+ getSimpleDataSetIdString(this)
 				+ "<br> Category Utility: "
 				+ formater.format(getCategoryUtility())
 				+ "<br><table><tr>";
 
 		List<INode> merge = new ArrayList<INode>();
 		merge.add(this);
 
 
 //		Set<INode> attributeKeys = getAttributeKeys();
 		List<Node> atKeyLi = new ArrayList(attributes.keySet());
 		Collections.sort(atKeyLi);
 		if (attributes.values().iterator().next() instanceof ClassitAttribute) {
 			// Header
 			description += "<td>attr</td><td>data set id</td><td>type</td><td>mean</td><td>std</td><td>support</td><td>meta</td></tr>";
 
 			// Data
 			for(INode attributeKey : atKeyLi) {
 				 
 				IAttribute attributeValue = getAttributeValue(attributeKey);
 				description += "<tr><td>" + attributeKey.getId() + "</td>" +
 						"<td>" + getSimpleDataSetIdString(attributeKey) + "</td>"+
 						"<td>" + attributeKey.getNodeType() + "</td>"+
         				"<td>" + formater.format(attributeValue.getSumOfRatings()/attributeValue.getSupport())+ "</td>" +
        				"<td>" + formater.format(ClassitMaxCategoryUtilitySearcher.calcStdDevOfNumericAttribute(attributeKey, merge)) + "</td>" +
         				"<td>" + attributeValue.getSupport()+ "</td>" +
         				"<td>" + attributeKey.getMeta() + "</td>" +
         				"</tr>";
 			}
 		}
 		if (attributes.values().iterator().next() instanceof CobwebAttribute) {
 			// Header
 			description += "<td>attr</td><td>data set id</td><td>type</td><td width='150'>value -> probability</td></tr>";
 
 			// Data
 			for(INode attributeKey : atKeyLi) {
 				
 				IAttribute attributeValue = getAttributeValue(attributeKey);
 				description += "<tr><td>" + attributeKey.getId() + "</td>" +
 							"<td>" + getSimpleDataSetIdString(attributeKey) + "</td>"+
 							"<td>" + attributeKey.getNodeType().toString() + "</td>";
 				Iterator<Entry<Object,Double>> values = attributeValue.getProbabilities();
 				description += "<td>";
 				while ( values.hasNext() ){
 					Entry<Object,Double> tempEntry = values.next();
 					description += tempEntry.getKey().toString() + " -> " +
 							formater.format(tempEntry.getValue().doubleValue()) + "<br>";
 
 				}
 
 				description += "</td></tr>";
 			}
 		}
 		return description += "</table></body></html>";
 	}
 	
 	private String getSimpleDataSetIdString(INode node) {
 		String dataSetIdsString = "";
 		List<Integer> ids = node.getDataSetIds();
 		if (ids.size() > 1) {
 			dataSetIdsString = ids.get(0) + ", " + ids.get(1) + " ...";
 		}
 		if (node.getDataSetIds().size() == 1) {
 			dataSetIdsString = ids.get(0).toString();
 		}
 		return dataSetIdsString;
 	}
 
 //	private void writeObject(ObjectOutputStream oos) throws IOException {
 //		oos.defaultWriteObject();
 //
 //		oos.writeLong(idCounter);
 //	}
 //
 //	private void readObject(ObjectInputStream ois) throws IOException,
 //			ClassNotFoundException {
 //		ois.defaultReadObject();
 //
 //		idCounter = ois.readLong();
 //
 //	}
 	
 	@Override
 	public void setId(long id) {
 		this.id = id;
 	}
 	
 	@Override
 	public List<Integer> getDataSetIds() {
 		List<Integer> li = new ArrayList<Integer>();
 		return getDataSetIds(li);
 	}
 	
 	private List<Integer> getDataSetIds(List<Integer> li) {
 		if (isLeaf()) {
 			li.add(dataSetId);
 		} else {
 			for (INode child : children) {
 				((Node)child).getDataSetIds(li);
 			}
 		}
 		return li;
 	}
 	
 	@Override
 	public double getCategoryUtility() {
 		return categoryUtility;
 	}
 	
 	@Override
 	public boolean isDirty() {
 		return dirtySet.contains(this);
 	}
 	
 	@Override
 	public void setClean() {
 		dirtySet.remove(this);
 		
 	}
 	
 	/**
 	 * Gets the set of dirty nodes.
 	 * @return all nodes with recently changed attributes.
 	 */
 	public static Set<INode> getAllDirtyNodes() {
 		return dirtySet;
 	}
 
 	@Override
 	public long getDatasetId() {
 		return dataSetId;
 	}
 	
 }
