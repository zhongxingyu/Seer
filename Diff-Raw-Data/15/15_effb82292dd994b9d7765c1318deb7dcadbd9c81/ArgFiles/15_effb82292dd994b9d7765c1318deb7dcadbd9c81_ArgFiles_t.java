 package org.daisy.pipeline.client.models.script.arguments;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.daisy.pipeline.client.Pipeline2WS;
 import org.daisy.pipeline.client.Pipeline2WSException;
 import org.daisy.pipeline.client.models.script.Argument;
 import org.daisy.pipeline.utils.XPath;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /** An argument of type "anyFileURI" and sequence="true" */
 public class ArgFiles extends Argument {
 	
 	public List<String> hrefs = new ArrayList<String>();
 	
 	public ArgFiles(Node arg) throws Pipeline2WSException {
 		super(arg);
 	}
 	
 	/**
 	 * Returns an XML Element representation of the file sequence, compatible with a jobRequest document.
 	 * 
 	 * Example (option):
 	 * <d:option name="stylesheets">
 	 *     <d:item value="main.css"/>
 	 *     <d:item value="responsive.css"/>
 	 * </d:option>
 	 * 
 	 * Example (input):
 	 * <d:input name="source">
 	 *     <d:item value="chapter1.xhtml"/>
 	 *     <d:item value="chapter2.xhtml"/>
 	 * </d:input>
 	 * 
 	 * @param document The document used to create the element.
 	 * @return
 	 */
 	@Override
 	public Element asDocumentElement(Document document) {
 		if (hrefs.size() == 0)
 			return null;
 		
 		Element element;
 		if ("option".equals(kind))
 			element = document.createElement("option");
		else if ("output".equals(kind))
			element = document.createElement("output");
 		else
 			element = document.createElement("input");
 		
 		element.setAttribute("name", name);
 		
 		for (String href : hrefs) {
 			Element item = document.createElement("item");
 			item.setAttribute("value", href+"");
 			element.appendChild(item);
 		}
 		
 		return element;
 	}
 	
 	/**
 	 * Populates the file sequence with hrefs from the provided document element.
 	 * This is the inverse of the "Element asDocumentElement(Document)" method.
 	 * 
 	 * @param option
 	 * @return
 	 * @throws Pipeline2WSException
 	 */
 	@Override
 	public void parseFromJobRequest(Node jobRequest) throws Pipeline2WSException {
 		hrefs.clear();
 		List<Node> items = XPath.selectNodes("/*/d:"+kind+"[@name='"+name+"']/d:item", jobRequest, Pipeline2WS.ns);
 		for (Node item : items) {
 			add(XPath.selectText("@value", item, Pipeline2WS.ns));
 		}
 	}
 	
 	@Override
 	public void set(Object key, Object value) {
 		Integer i = null;
 		try { i = Integer.parseInt(key+""); }
 		catch (NumberFormatException e) {
 			Pipeline2WS.logger().warn("Unable to parse integer: "+key);
 		}
 		
 		if (value != null)
 			hrefs.set(i, value.toString());
 	}
 	
 	@Override
 	public void set(Object value) {
 		clear();
 		add(value);
 	}
 
 	@Override
 	public void add(Object value) {
 		if (value != null)
 			hrefs.add(value.toString());
 	}
 
 	@Override
 	public void remove(Object value) {
 		hrefs.remove(value);
 	}
 
 	@Override
 	public void clear() {
 		hrefs.clear();
 	}
 
 	@Override
 	public String get() {
 		// there are multiple hrefs to return; don't return anything
 		return null;
 	}
 
 	@Override
 	public String get(Object key) {
 		Integer i = null;
 		try { i = Integer.parseInt(key+""); }
 		catch (NumberFormatException e) {
 			Pipeline2WS.logger().warn("Unable to parse integer: "+key);
 		}
 		
 		return hrefs.get(i);
 	}
 
 	@Override
 	public int size() {
 		return hrefs.size();
 	}
 	
 }
