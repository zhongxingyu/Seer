 package org.daisy.pipeline.client.models.script.arguments;
 
 import org.daisy.pipeline.client.Pipeline2WS;
 import org.daisy.pipeline.client.Pipeline2WSException;
 import org.daisy.pipeline.client.models.script.Argument;
 import org.daisy.pipeline.utils.XPath;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /** An argument of type "anyFileURI" */
 public class ArgFile extends Argument {
 	
 	public String href = null;
 	
 	public ArgFile(Node arg) throws Pipeline2WSException {
 		super(arg);
 	}
 	
 	/**
 	 * Returns an XML Element representation of the file argument, compatible with a jobRequest document.
 	 * 
 	 * Example (option):
 	 * <d:option name="stylesheet">main.css</d:option>
 	 * 
 	 * Example (input):
 	 * <d:input name="source">content.xhtml</d:input>
 	 * 
 	 * @param document The document used to create the element.
 	 * @return
 	 */
 	@Override
 	public Element asDocumentElement(Document document) {
 		if (href == null)
 			return null;
 		
 		Element element;
 		if ("option".equals(kind)) {
 			element = document.createElement("option");
 			element.setAttribute("name", name);
 			
 			element.setTextContent(href+"");
 			
		} else if ("output".equals(kind)) {
			element = document.createElement("output");
			element.setAttribute("name", name);
			
			Element item = document.createElement("item");
			item.setAttribute("value", href+"");
			element.appendChild(item);
			
 		} else {
 			element = document.createElement("input");
 			element.setAttribute("name", name);
 			
 			Element item = document.createElement("item");
 			item.setAttribute("value", href+"");
 			element.appendChild(item);
 		}
 		return element;
 	}
 	
 	/**
 	 * Sets the href to the values from the provided document element.
 	 * This is the inverse of the "Element asDocumentElement(Document)" method.
 	 * 
 	 * @param option
 	 * @return
 	 * @throws Pipeline2WSException
 	 */
 	@Override
 	public void parseFromJobRequest(Node jobRequest) throws Pipeline2WSException {
 		Pipeline2WS.logger().debug("parsing jobRequest...");
 		if ("option".equals(kind)) {
 			String href = XPath.selectText("/*/d:option[@name='"+name+"']", jobRequest, Pipeline2WS.ns);
 			set(href);
 			
 		} else {
 			Node item = XPath.selectNode("/*/d:"+kind+"[@name='"+name+"']/d:item", jobRequest, Pipeline2WS.ns);
 			String href = XPath.selectText("@value", item, Pipeline2WS.ns);
 			set(href);
 		}
 	}
 	
 	@Override
 	public void set(Object key, Object value) {
 		Integer i = null;
 		try { i = Integer.parseInt(key+""); }
 		catch (NumberFormatException e) {
 			Pipeline2WS.logger().warn("Unable to parse integer: "+key);
 		}
 		
 		if (i == 0)
 			set(value);
 	}
 	
 	@Override
 	public void set(Object value) {
 		href = value == null ? null : value.toString();
 	}
 
 	@Override
 	public void add(Object value) {
 		set(value);
 	}
 
 	@Override
 	public void remove(Object value) {
 		if (value != null && value.equals(href))
 			clear();
 	}
 
 	@Override
 	public void clear() {
 		href = null;
 	}
 	
 	@Override
 	public String get() {
 		return href;
 	}
 	
 	@Override
 	public String get(Object key) {
 		Integer i = null;
 		try { i = Integer.parseInt(key+""); }
 		catch (NumberFormatException e) {
 			Pipeline2WS.logger().warn("Unable to parse integer: "+key);
 		}
 		
 		if (i == 0)
 			return get();
 		else
 			return null;
 	}
 
 	@Override
 	public int size() {
 		return href == null ? 0 : 1;
 	}
 	
 }
