 package graphDB.explore;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 import javax.servlet.jsp.JspWriter;
 
 import org.neo4j.cypher.javacompat.ExecutionEngine;
 import org.neo4j.cypher.javacompat.ExecutionResult;
 import org.neo4j.graphdb.*;
 
 
 public class NodeHelper 
 {
 
 	private static Pattern doublePattern = Pattern.compile("-?\\d+(\\.\\d*)?");
 	private static int numberOfDigits = 3;
 
 	public static boolean isNumber(String string) {
 		return doublePattern.matcher(string).matches();
 	}
 	
 	public static boolean isNumeric(String string) {
 		Scanner scanner = new Scanner(string);
 		if (scanner.hasNextInt())
 		    return true;
 		else if (scanner.hasNextDouble())
 			return true;
 		else
 		    return false;
 	}
 
 
 	
 	public static String decimalFormat(String s) {
 		String[] tmp = s.split("\\.");
 		return tmp[0];
 	}
 
 	
 	public static String doubleFormat(String s) {
 		String[] tmp = s.split("\\.");
 		if (tmp.length > 1 && tmp[1].length() > numberOfDigits) {
 			return tmp[0] + "." + tmp[1].substring(0, numberOfDigits);
 		} else {
 			return s;
 		}
 	}
 	
 	public static HashMap<String, String> getProperties(Node theNode)
 	{
 		HashMap<String, String> theProperties = new HashMap<String, String>();		
 		for (String p : theNode.getPropertyKeys()) {
 			String value = "";
 			String s = theNode.getProperty(p).toString();
 			// converting to 3 digits number
 			if (isNumber(s)) {
 				value = doubleFormat(s);
 			} else {
 				value = s;
 			}
 			theProperties.put(p.toString(), value);
 		}
 		return theProperties;
 	}
 	
 
 	public static HashMap<String, HashMap<String, String>> computeListOfAttributes(	Node theNode) 
     {
 		HashMap<String, HashMap<String, String>> listOfAttributes = new HashMap<String, HashMap<String, String>>();
 		
 		for (Relationship rel : theNode.getRelationships()) 
 		{		
 			if (DefaultTemplate.keepRelation(rel.getType().name())) 
 			{
 				Node n = rel.getOtherNode(theNode);
 				if(!listOfAttributes.containsKey(NodeHelper.getType(n)))
 					listOfAttributes.put(NodeHelper.getType(n), new HashMap<String, String>());
 				
 				HashMap<String, String> attributes = listOfAttributes.get(NodeHelper.getType(n));
 				
 				// writing the relation properties
 				for (String k : rel.getPropertyKeys())
 					if (DefaultTemplate.keepAttribute(k))
 						attributes.put(k, k);
 
 				// writing the node properties
 				for (String k : n.getPropertyKeys())
 					if (DefaultTemplate.keepAttribute(k))
 						attributes.put(k, k);
 			}
 		}
 		return listOfAttributes;
 	}
 	
 	public static HashMap<String, HashMap<String, String>> computeListOfAttributes(
 			HashMap<RelationshipType, String> relationsMap, Direction dir,
 			Node theNode) 
     {
 		HashMap<String, HashMap<String, String>> listOfAttributes = new HashMap<String, HashMap<String, String>>();
 		for (RelationshipType relationType : relationsMap.keySet()) {
 			HashMap<String, String> attributes = new HashMap<String, String>();
 
 			for (Relationship rel : theNode.getRelationships(dir, relationType)) {
 				// writing the relation properties
 				for (String k : rel.getPropertyKeys())
 					if (DefaultTemplate.keepAttribute(k))
 						attributes.put(k, k);
 
 				Node n = rel.getOtherNode(theNode);
 
 				// writing the node properties
 				for (String k : n.getPropertyKeys())
 					if (DefaultTemplate.keepAttribute(k))
 						attributes.put(k, k);
 			}
 			listOfAttributes.put(relationType.name(), attributes);
 		}
 		return listOfAttributes;
 	}
 
 	/**
 	 * Get the comment written by a user about the current Node. A comment is a
 	 * relation of the graph with a "text property"
 	 * 
 	 * @param theNode
 	 *            neo4j Node
 	 * @return
 	 */
 	public static String getComments(Node theNode) {
 		String output = "";
 		// output += "{'comment':\"<a href='test.html'>test</a> comment\"},";
 		// output += "{'comment':\"<a href='test.html'>test</a> new comment\"}";
 
 		for (Relationship relation : theNode
 				.getRelationships(Direction.OUTGOING,
 						DynamicRelationshipType.withName("Comment"))) {
 			Node userNode = relation.getOtherNode(theNode);
 			if (userNode.hasProperty("NickName")
 					&& relation.hasProperty("Text")) {
 				String user = userNode.getProperty("NickName").toString();
 				String text = relation.getProperty("Text").toString()
 						.replaceAll("\\r", "<br/>");
 				text = text.replaceAll("\\n", "<br/>");
 				text = text.replaceAll("\\\"", "&#34;");
 				text = text.replaceAll("\\\\", "&#92;");
 
 				output += ",{'comment':\"<a href='index.jsp?id="
 						+ userNode.getId() + "'>" + user + "</a> " + text
 						+ "\"}";
 			}
 		}// */
 			// output +=
 			// "{'comment' : {xtype : 'textareafield', grow : true, name : 'message', fieldLabel : 'Message', anchor : '100%' }},";
 			// output +=
 			// "{'comment':\"<textarea id='idAddCommentText' rows='2' placeholder='Add comment...' style='resize: none; width:100%; white-space:pre;' onkeypress='AddComment(this,event);'></textarea>\"}";//Add
 			// comment routine
 			// output +=
 			// "{'comment':\"<input type='text' name='addCommentInput' />\"}";//Add
 			// comment routine
 		if (output.isEmpty())
 			return "[]";
 		else
 			return "[" + output.substring(1) + "]";
 	}
 
 	public static String getType(Node aNode) {
 		if(aNode.hasProperty("type"))
 			return aNode.getProperty("type").toString();
 		else
 			if(aNode.hasProperty("Type"))
 				return aNode.getProperty("Type").toString();
 			else
 				return "";
 	}
 
 	public static String getNameAndType(Node aNode) {
 		try {
 			if(aNode.hasProperty("Name"))
 				return aNode.getProperty("Name").toString() + " [" + getType(aNode) + "]";
 			else if(aNode.hasProperty("name"))
 				return aNode.getProperty("name").toString() + " [" + getType(aNode) + "]";
 			else
 				return getType(aNode);
 		} catch (Exception e) {
 			return "";
 		}
 	}
 	
 	public static String getName(Node aNode) {
 		try {
 			if(aNode.hasProperty("Name"))
 				return aNode.getProperty("Name").toString();
 			else if(aNode.hasProperty("name"))
 				return aNode.getProperty("name").toString();
 			else
 				return getType(aNode);
 		} catch (Exception e) {
 			return "";
 		}
 	}
 		
 	public static HashMap<String, Long> computeNodeTypes(RelationshipType relationType, Direction dir, Node startNode) 
 	{
 		HashMap<String, Long> listOfNodeTypes = new HashMap<String, Long>();
 		for (Relationship rel : startNode.getRelationships(dir, relationType)) {
 			Node n = rel.getOtherNode(startNode);
 			if (!listOfNodeTypes.containsKey(getType(n)))
 				listOfNodeTypes.put(getType(n), 1l);
 			else
 				listOfNodeTypes.put(getType(n),
 						listOfNodeTypes.get(getType(n)) + 1);
 		}
 		return listOfNodeTypes;
 	}
 
 	private static class NavNode
 	{
 		int   theNodeIndex;
 		ArrayList<Integer> theRelationIndexes = new ArrayList<Integer>();
 		String strToAdd = "";
 		String theRelationInfo = "";
 		Node theNode;
 		int Size = 0;
 		public String getNodeInfo()
 		{
 			return NodeHelper.getNodeInfo(theNode, strToAdd, Size);
 		}
 		
 		public String getRelationInfo()
 		{
 			return theRelationInfo;
 		}
 		
 		public NavNode(int index, Node node)
 		{
 			theNodeIndex = index;
 			strToAdd = "IsRoot:true";
 			theNode = node;
 			Size = 1;
 		}
 		
 		public NavNode(int index, Node node, Relationship relation, int indexOther, int size)
 		{			
 			Size = size;
 			theNodeIndex = index;
 			strToAdd = "relation:'" + relation.getType().name() + "'";
 			theNode = node;			
 			if(relation.getEndNode().getId() == node.getId())
 				AddRelation(relation, indexOther, index);
 			else
 				AddRelation(relation, index, indexOther);
 		}
 		
 		public void AddRelation(Relationship relation, int source, int target)
 		{
 			if(!(theRelationIndexes.contains(source) || theRelationIndexes.contains(target)))
 			{
 				if(!theRelationInfo.isEmpty())
 					theRelationInfo += "},{";
 				theRelationInfo += "source:" + source + ",target:" + target + ",name:'" + relation.getType().name() + "'";
 				if(source == theNodeIndex)
 					theRelationIndexes.add(target);
 				else
 					theRelationIndexes.add(source);
 			}
 		}
 	}
 	
 	public static void printNavigationNodes(JspWriter out, Node theNode, int depth, String varName)
 	{
 		try
 		{
 			HashMap<String, NavNode> result = new HashMap<String, NodeHelper.NavNode>();
 			
 			out.println("var " + varName + " = {nodes:[");
 			NavNode root = new NavNode(0, theNode);
 			//out.println("{" + root.theNodeInfo + "}");
 			
 			result.put(getType(theNode), root);			
 			getNavigationNodes(theNode, depth, result, 0);
 	
 			//Sort map based on index (required by D3.js to map with the correct relations)
 			List<Map.Entry<String, NavNode>> list = new LinkedList<Map.Entry<String, NavNode>>(result.entrySet());
 			 
 	        //sort list based on comparator
 	        Collections.sort(list, new Comparator<Map.Entry<String, NavNode>>() 
 	        {
 				public int compare(Entry<String, NavNode> arg0,	Entry<String, NavNode> arg1) 
 				{
 					if(arg0.getValue().theNodeIndex > arg1.getValue().theNodeIndex)
 						return 1;
 					else if(arg0.getValue().theNodeIndex < arg1.getValue().theNodeIndex)
 						return -1;
 					else
 						return 0;
 				}
 			});
 	        
 			//for (NavNode nav : result.values())
 			//	out.println("{" + nav.theNodeInfo + "},");
 			boolean isFirst = true;
 			for(Map.Entry<String, NavNode> item : list)
 			//for (NavNode nav : result.values())
 			{
 				if(isFirst)
 					isFirst = false;
 				else
 					out.print(",");
 			
 				out.println("{" + item.getValue().getNodeInfo() + "}");
 			}	
 			out.print("],links:[");
 			for (NavNode nav : result.values())
 				if(!nav.theRelationInfo.isEmpty())
 					out.println("{" + nav.getRelationInfo() + "},");
 			out.print("]};");		
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static int CountNodes(Node root, RelationshipType relType)
 	{
 		String query = "START a=node(" + root.getId() + ") MATCH a-[:" + relType.name() + "]-b RETURN COUNT(*)";
 		ExecutionEngine engine = new ExecutionEngine( DefaultTemplate.graphDb() );
 		// VERY IMPORTANT : use the org.neo4j.cypher.javacompat.* and not the org.neo4j.cypher.*
 		// otherwise can't iterate over the ExecutionResult
 		ExecutionResult result = engine.execute( query );
 		for ( Map<String, Object> row : result )
 		{
 		    for ( Object obj : row.values() )
 		    {
 		    	return Integer.parseInt(obj.toString());
 		    }
 		}
 		return 0;
 	}
 	
 	public static HashMap<String, HashMap<String, Integer>> getRelatedNodeTypesAndProperties(Node n){
 		
 		HashMap<String, HashMap<String, Integer>> TypesAndAttributesRelated = new HashMap<String,HashMap<String, Integer>>();
 		String otherNodeType;
 		for (Relationship r : n.getRelationships(Direction.OUTGOING)){
 			if(DefaultTemplate.keepRelation(r.getType().toString())){
 				otherNodeType = getType(r.getOtherNode(n));
 				if (!TypesAndAttributesRelated.containsKey(otherNodeType))
 					TypesAndAttributesRelated.put(otherNodeType, new HashMap<String, Integer>());
 				
 				HashMap<String, Integer> ptr = TypesAndAttributesRelated.get(otherNodeType);
 				for(String property : r.getOtherNode(n).getPropertyKeys())
 					if(!ptr.containsKey(property))
 						ptr.put(property, 1);
 			}
 		}
 		return TypesAndAttributesRelated;
 	}
 	
 	private static HashMap<String, NavNode> getNavigationNodes(Node theNode, int depth, HashMap<String, NavNode> result, int index) throws IOException
 	{	
 		if(depth > 0)
 		{	
 			for (Relationship relation : theNode.getRelationships())
 			{
 				Node theOtherNode = relation.getOtherNode(theNode);
 				if ((theOtherNode.getId() != theNode.getId()) && (DefaultTemplate.keepRelation(relation.getType().name())))
 				{
 					NavNode nav = result.get(getType(theOtherNode));
 					if(nav == null)
 					{
 						nav = new NavNode(result.size(), relation.getOtherNode(theNode), relation, index, 1);//CountNodes(theNode, relation.getType()));
 						//out.println(",{" + nav.theNodeInfo + "}");
 						result.put(getType(relation.getOtherNode(theNode)), nav);
 						getNavigationNodes(relation.getOtherNode(theNode), depth-1, result, nav.theNodeIndex);
 					}
 					else
 					{
 						nav.Size++;
 						if(relation.getEndNode().getId() == theNode.getId())
 							nav.AddRelation(relation, nav.theNodeIndex, index);
 						else
 							nav.AddRelation(relation, index, nav.theNodeIndex);
 					}
 				}
 			}
 		}
 		return result;
 	}
 	
 	public static boolean IsNumber(Object value)
 	{
 		return value instanceof Double;
 	}
 	
 	public static String MakeHtmlFriendly(Object value)
 	{
 		if(value instanceof String)
 		{
 			String str = (String)value;
 			if(str.startsWith("http"))
 				return "<a href=\"" + value + "\">" + value + "</a>";
 		}
 		if(value instanceof Double)
 		{
 			Double d = (Double)value;
 			NumberFormat formatter = new DecimalFormat("#.##");  
 			return formatter.format(d);									
 		}
 		return value + "";
 	}
 
 	public static int PropertyToInt(Object value)
 	{
 		if(value instanceof String)
 			return Integer.parseInt((String) value);
 		
 		if(value instanceof Double)
 		{
 			return (int)Math.round((Double)value);									
 		}
 		
 		if(value instanceof Integer)
 		{
 			return (Integer)value;									
 		}
 		return 0;
 	}
	
 	public static double PropertyToDouble(Object value)
 	{
 		if(value instanceof String)
 			return Double.parseDouble((String) value);
 		
 		if(value instanceof Double)
 		{
 			return (Double)value;									
 		}
 		
 		if(value instanceof Integer)
 		{
 			return (double)(Integer)value;									
 		}
 		return Double.NaN;
 	}
 	
 	private static String getInfo(Node theNode, int size)	
 	{
 		String result = "Node type: <b>" + getType(theNode) + "</b><br><hr>";
 		if(size > 1)
 			result += "Number of elements:" + size + "<br>";
 		else
 		{
 			result += "Node ID :" + theNode.getId() + "<br>";
 			for (String key : theNode.getPropertyKeys())
 				if(DefaultTemplate.keepAttribute(key))
 					result += key + ":\"" + DefaultTemplate.Sanitize(theNode.getProperty(key).toString()).replace('\'', '"') + "\"<br>";//*/				
 		}
 		return result;
 	}
 	private static String getNodeInfo(Node theNode, String toAdd, int size) {
 		String result = "";
 
 		if(size > 1)
 			result += "name:'" + getType(theNode) + "'";
 		else
 			result += "name:'" + getNameAndType(theNode) + "'";
 		
 		if (toAdd != null && !toAdd.isEmpty())
 			result += "," + toAdd;
 		return result 		
 				+ ",type:'" + getType(theNode) + "'"
 				+ ",info:'" + getInfo(theNode, size) + "'"
 				+ ",size:" + size
 				+ ",url:'index.jsp?id=" + theNode.getId() + "'";
 	}	
 }
