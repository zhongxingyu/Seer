 package no.larsereb.javaHTML;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class HTMLTag {
 	private List<String> classes;
 	private List<HTMLTag> children;
 	private Map<String, String> styleRules;
 	private Map<String, String> attributes;
 	private String nodeName;
 	private HTMLTag parent;
 	private boolean onlyText;
 	private String text;
 	
 	public HTMLTag(String nodeName) {
 		this.nodeName = nodeName;
 		classes = new ArrayList<String>();
 		styleRules = new HashMap<String, String>();
 		children = new ArrayList<HTMLTag>();
 		attributes = new HashMap<String, String>();
 	}
 
 	@Override
 	public String toString() {
 		return toString(new StringBuffer()).toString();
 	}
 	
 	/**
 	 */
 	public StringBuffer toString(StringBuffer b) {
 		if(onlyText)
 			return b.append(text);
 
 		b.append("<" + nodeName);
 		if(classes.size() > 0) {
 			boolean firstClass = true;
 			b.append(" class=\"");
 			for(String c: classes)
 				if(firstClass) {
 					firstClass = false;
 					b.append(c);
 				} else
 					b.append(" " + c);
 			b.append("\"");
 		}
 		
 		for(Entry<String, String> e: getAttributes().entrySet())
 			b.append(" " + e.getKey() + "=\"" + e.getValue() + "\"");
 
 		if(styleRules.size() > 0) {
 			b.append(" style=\"");
 			for(Entry<String, String> e: styleRules.entrySet())
 				b.append(e.getKey() + ":" + e.getValue() + ";");
 			b.append("\"");
 		}
 
 		b.append(">");
 		
 		if(onlyText)
 			b.append(text);
 		else
 			for(HTMLTag c: children)
 				c.toString(b);
 
 		b.append("</" + nodeName + ">");
 		return b;
 	}
 	
 	public HTMLTag addClass(String c) {
 		classes.add(c);
 		return this;
 	}
 	
 	public Map<String, String> getAttributes() {
 		return attributes;
 	}
 	
 	public String getAttribute(String attribute) {
 		return attributes.get(attribute);
 	}
 	
 	public boolean hasAttribute(String attribute) {
 		return attributes.containsKey(attribute);
 	}
 
 	public HTMLTag setAttribute(String attribute, String value) {
 		getAttributes().put(attribute, value);
 		return this;
 	}
 
 	public HTMLTag addStyleRule(String attribute, String value) {
 		styleRules.put(attribute, value);
 		return this;
 	}
 	
 	public String getNodeName() {
 		return nodeName;
 	}
 
 	public HTMLTag addChild(String nodeName) {
 		HTMLTag	child = new HTMLTag(nodeName);
 		children.add(child);
 		child.setParent(this);
 		return child;
 	}
 	
 	public HTMLTag addChild(HTMLTag node) {
 		children.add(node);
 		return this;
 	}
 
 	/**
 	 * Returns true if the node has a child with this nodeName.
 	 *
 	 * @param nodeName
 	 * @return
 	 */
 	public boolean hasChild(String nodeName) {
 		for(HTMLTag child: children)
 			if(child.getNodeName().equalsIgnoreCase(nodeName))
 				return true;
 		return false;
 	}
 	
 	public List<HTMLTag> getChildren() {
 		return children;
 	}
 
 	public List<HTMLTag> getChildren(String nodeName) {
 		ArrayList<HTMLTag> result = new ArrayList<HTMLTag>();
 		for(HTMLTag child: children)
 			if(child.getNodeName().equalsIgnoreCase(nodeName))
 				result.add(child);
 		return result;
 	}
 
 	public HTMLTag addText(String text) {
 		HTMLTag t = new HTMLTag("text");
 		t.onlyText = true;
 		t.text = text;
 		this.addChild(t);
 		return this;
 	}
 	
 	public HTMLTag root() {
 		if(getParent() == null)
 			return this;
 		return getParent().root();
 	}
 
 	public HTMLTag getParent() {
 		return parent;
 	}
 
 	public void setParent(HTMLTag parent) {
 		this.parent = parent;
 	}
 	
 	public HTMLTag body() {
 		return addChild("body");
 	}
 	
 	public HTMLTag div() {
 		return addChild("div");
 	}
 	
 	public HTMLTag p() {
 		return addChild("div");
 	}
 	
 	public HTMLTag span() {
 		return addChild("span");
 	}
 	
 	public HTMLTag a() {
 		return addChild("a");
 	}
 	
 	private HTMLTag header(String level, String header) {
		addChild(new HTMLTag("h1").addText(header));
 		return this;
 	}
 
 	public HTMLTag h1(String header) {
 		return header("h1", header);
 	}
 
 	public HTMLTag h2(String header) {
 		return header("h2", header);
 	}
 
 	public HTMLTag h3(String header) {
 		return header("h3", header);
 	}
 
 	public HTMLTag h4(String header) {
 		return header("h4", header);
 	}
 
 	public HTMLTag h5(String header) {
 		return header("h5", header);
 	}
 
 	public HTMLTag h6(String header) {
 		return header("h6", header);
 	}
 }
