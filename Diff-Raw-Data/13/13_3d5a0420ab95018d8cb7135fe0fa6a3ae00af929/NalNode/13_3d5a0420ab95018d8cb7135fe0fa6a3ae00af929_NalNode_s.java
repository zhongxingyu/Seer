 /*
  * Copyright (c) 2013 Patrick Fromberg
  * See BSD 3 clause license in LICENSE.txt or
  * LICENSE.txt or http://opensource.org/licenses/BSD-3-Clause
  */
 
 package nal.antlr;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class NalNode
 {
 	public static String codeFileName;
 	public static List<NalNode> externalFunctions;
 	private static int counterx = 0;
 	final public Map<String, NalNode> nal_functions = new HashMap<String, NalNode>();
 	final public List<NalNode> bind_parameters = new ArrayList<NalNode>();
 	public final List<NalNode> children = new ArrayList<NalNode>();
 	public final List<String> parameters = new ArrayList<String>();
 	public int line;
 	public int lastline;
 	public int position;
 	public NalNode parent;
 	public String nodeType;
 	public String ruleType;
 	public String id;
 	public String value; //fix patrick, check usage scenarios of this member
 	public String containerType;
 	public String elementType;
 	public String size1;
 	public String size2;
 	public boolean isTimeDependent;
 	public boolean isSeedDependent;
 	public boolean hasType;
 	public boolean isLambda;
 	public boolean isHigherOrder;
 	public String pre_comment;
 	public String post_comment;
 	public String intra_comment;
 	public int dimensions = 0;
 	public String unit;
 	public String subunit1;
 	public String subunit2;
 	public String displayName;
 	private int counter = counterx++;
 	private Map<String, String> attributes;
 
 	public NalNode()
 	{
 	}
 
 	public NalNode(NalNode x)
 	{
 		this.id = x.id;
 		this.nodeType = x.nodeType;
 		this.containerType = x.containerType;
 		this.elementType = x.elementType;
 		this.counter = x.counter;
 		this.dimensions = x.dimensions;
 		this.value = x.value;
 		this.size1 = x.size1;
 		this.size2 = x.size2;
 		this.isTimeDependent = x.isTimeDependent;
 		this.isSeedDependent = x.isSeedDependent;
 		this.hasType = x.hasType;
 		this.isLambda = x.isLambda;
 		this.isHigherOrder = x.isHigherOrder;
 	}
 
 	static public NalNode findById(List<NalNode> list, String id)
 	{
 		for (NalNode x : list)
 			if (x.id != null && x.id.equals(id))
 				return x;
 		return null;
 	}
 
 	// convenience method called back by String Templates
 	public boolean getIsInDependent()
 	{
 		return !(isTimeDependent || isSeedDependent);
 	}
 
 	// Called back by StringTemplates
 	public Map<String, String> getNodeTypeDescription()
 	{
 		// fix patrick, rename attributes to nodeTypeForST or solve otherwise
 		if (attributes == null) {
 			attributes = new HashMap<String, String>();
 			attributes.put(nodeType, "true");
 			attributes.put("any", "true");
 			attributes.put(isLambda ? "lambda" : "no lambda", "true");
 			attributes.put(isHigherOrder ? "higher_order" : "no higher_order", "true");
 		}
 		return attributes;
 	}
 
 	// we create a function for each call to a library function so
 	// we need to extend the name to make it unique for each call.
 	// This function is called back by String Templates.
 	public String getUid()
 	{
 		String postfix = "";
 		if (nodeType.equals("library_funccall")) {
 			postfix = "_" + counter;
 		} else if (nodeType.equals("operatorcall")) {
 			postfix = "_" + counter;
 		} else if (nodeType.equals("at_operatorcall")) {
 			postfix = "_" + counter;
 		}
 		return "_nal_" + (id.replace("$", "bind_")) + postfix;
 	}
 
 	// in C 1 hast to be written as 1L to allow type inference.
 	// This function is called back by String Templates.
 	public String getValue2()
 	{
 		return (elementType.equals("long")) ? (value + "L") : value;
 	}
 
 	// If a function parameter is commented, then we will write one parameter per line.
 	// Otherwise we will write the whole function call on one line with all its parameters.
 	// This function is a call back for String Templates
 	public boolean getNeedsCommentSpace()
 	{
 		for (NalNode e : children)
 			if (e.post_comment != null || e.getNeedsCommentSpace())
 				return true;
 		return false;
 	}
 }
