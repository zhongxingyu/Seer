 package com.mtgi.analytics.xml;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Collection;
 
 import net.sf.saxon.om.VirtualNode;
 
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 public class XSLLibrary {
 
 	private static ThreadLocal<Serializer> serializer = new ThreadLocal<Serializer>();
 	
 	/**
 	 * Serialize an XSLT data object to text.  XML elements are serialized as XML
 	 * strings; all other data types (attributes, literals, etc) are serialized to
 	 * flat text.
 	 */
     public static String serialize(Object node) throws IOException {
 
     	//null matched node serializes to null
     	if (node == null)
     		return null;
     	
     	//because XSLT is weakly typed, we have to do a bit
     	//of introspection to handle various data types.
     	
     	//collection of nodes get just get appended together recursively
     	if (node instanceof Collection) {
     		Collection<?> coll = (Collection<?>)node;
     		if (coll.isEmpty()) //empty collection same as null
     			return null;
     		
     		StringBuffer buf = new StringBuffer();
     		for (Object n : coll)
     			buf.append(serialize(n));
     		return buf.toString();
     	}
     	
     	//unwrap Saxon nodes for processing.
     	if (node instanceof VirtualNode)
     		node = ((VirtualNode)node).getUnderlyingNode();
 
     	//elements serialize to XML text
     	if (node instanceof Element) {
     		Serializer s = serializer.get();
     		if (s == null)
     			serializer.set(s = new Serializer());
     		return s.serialize((Element)node);
     	}
     	//attributes, text, CDATA, etc just print text content.
     	if (node instanceof Node)
     		return ((Node)node).getNodeValue();
     	
     	//literals just to-string.
     	return node.toString();
     }
     
     /** 
      * serialize the given value to String with {@link #serialize}, 
      * and then quote the result as a SQL literal.
      */
     public static String quoteSql(Object value) throws IOException {
     	String str = serialize(value);
     	if (str == null)
     		return "null";
     	return "'" + str.replace("'", "''") + "'";
     }
     
     public static String quoteCsv(Object value) throws IOException {
     	String str = serialize(value);
     	if (str == null)
     		return "";
     	StringBuffer escaped = new StringBuffer(2 + str.length());
     	escaped.append('"');
     	for (int i = 0; i < str.length(); ++i) {
     		char c = str.charAt(i);
     		switch (c) {
     		case '\n':
     			//convert \r\n or \n to single \r.
     			if (i == 0 || str.charAt(i - 1) != '\r')
     				escaped.append('\r');
     			break;
    		case '\\':
     		case '"':
    			escaped.append('\\');
     		default:
     			escaped.append(c);
 				break;
     		}
     	}
     	escaped.append('"');
     	return escaped.toString();
     }
     
     private static class Serializer {
     	
     	private XMLSerializer serializer;
     	private ByteArrayOutputStream output;
     	private OutputFormat format;
     	
     	public Serializer() {
     		serializer = new XMLSerializer();
     		output = new ByteArrayOutputStream();
     		format = new OutputFormat();
     		format.setOmitXMLDeclaration(true);
     	}
     	
     	public String serialize(Element node) throws IOException {
 	    	serializer.reset();
 	    	output.reset();
 			serializer.setOutputByteStream(output);
 			serializer.setOutputFormat(format);
 			serializer.serialize(node);
 			return new String(output.toByteArray());
     	}
     	
     }
 }
