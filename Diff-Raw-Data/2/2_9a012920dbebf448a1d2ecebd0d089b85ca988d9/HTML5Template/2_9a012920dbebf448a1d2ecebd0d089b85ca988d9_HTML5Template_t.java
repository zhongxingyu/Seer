 package org.qrone.r7.parser;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import se.fishtank.css.selectors.NodeSelectorException;
 
 public class HTML5Template implements HTML5Writer, NodeProcessor{
 	private List<Object> list = new ArrayList<Object>();
 	private StringBuilder b = new StringBuilder();
 	
 	private HTML5OM om;
 	private Set<HTML5OM> xomlist;
 	
 	private HTML5Template(HTML5OM om, Set<HTML5OM> xomlist){
 		this.om = om;
 		this.xomlist = xomlist;
 		list.add(b);
 	}
 	
 	public HTML5Template(HTML5OM om){
 		this(om, new HashSet<HTML5OM>());
 	}
 	
 	public void append(String key, String value){
 		b.append(value);
 	}
 	
 	public void append(HTML5Template t){
 		list.add(t);
 		b = new StringBuilder();
 		list.add(b);
 	}
 
 	public void append(char str){
 		b.append(String.valueOf(str));
 	}
 
 	public void append(CharSequence str){
 		b.append(str);
 	}
 	
 	public void append(String str){
 		b.append(str);
 	}
 	
 	@Override
 	public String toString() {
 		StringBuffer b = new StringBuffer();
 		for (Iterator<Object> i = list.iterator(); i
 				.hasNext();) {
 			b.append(i.next().toString());
 		}
 		return b.toString();
 	}
 
	private boolean initialized = false;
 	private Map<String, NodeLister> selectmap = new Hashtable<String, NodeLister>();
 	private Map<Element, NodeLister> nodemap;
 
 	public void set(String selector, final String value){
 		set(selector, new NodeLister() {
 			@Override
 			public void accept(HTML5Template t, HTML5Element e) {
 				t.append(value);
 			}
 		});
 	}
 	
 	public void set(String selector, NodeLister lister){
 		selectmap.put(selector, lister);
 		initialized = false;
 	}
 
 	@Override
 	public boolean isTarget(Element node) {
 		return nodemap.containsKey(node);
 	}
 
 	@Override
 	public void processTarget(HTML5OM om, Element node) {
 		NodeLister o = nodemap.get(node);
 		if(o != null){
 			HTML5Template t = new HTML5Template(om, xomlist);
 			o.accept(t, new HTML5Element(om, (Element)node));
 			append(t);
 		}
 	}
 	
 	private Map<String, Iterator<Node>> selecting
 		= new Hashtable<String, Iterator<Node>>();
 	public void visit(String selector){
 		initialize(om);
 		if(selecting.containsKey(selector)){
 			Iterator<Node> iter = selecting.get(selector);
 			if(iter != null){
 				if(!iter.hasNext())
 					iter = om.select(selector).iterator();
 				om.process(this, this, iter.next(), null, xomlist);
 			}
 		}else{
 			Set<Node> nodes = om.select(selector);
 			if(nodes != null && !nodes.isEmpty()){
 				Iterator<Node> iter = nodes.iterator();
 				selecting.put(selector, iter);
 				om.process(this, this, iter.next(), null, xomlist);
 			}else{
 				selecting.put(selector, null);
 			}
 		}
 	}
 	
 	public void visit(HTML5Element e){
 		initialize(om);
 		e.getOM().process(this, this, e.get(), null, xomlist);
 	}
 
 	public void visit(HTML5OM om) {
 		initialize(om);
 		om.process(this, this, null, null, xomlist);
 	}
 	
 	public String output() {
 		initialize(om);
 		om.process(this, xomlist);
 		return toString();
 	}
 	
 	private void initialize(HTML5OM om){
 		if(!initialized){
 			nodemap = new Hashtable<Element, NodeLister>();
 			for (Iterator<Entry<String, NodeLister>> iterator = selectmap.entrySet().iterator(); iterator
 					.hasNext();) {
 				Entry<String, NodeLister> e = iterator.next();
 				Set<Node> set = om.select(e.getKey());
 				if(set != null){
 					for (Iterator<Node> iter = set.iterator(); iter.hasNext();) {
 						Node n = iter.next();
 						if(n instanceof Element)
 							nodemap.put((Element)n, e.getValue());
 					}
 				}
 			}
 		}
 	}
 	
 }
