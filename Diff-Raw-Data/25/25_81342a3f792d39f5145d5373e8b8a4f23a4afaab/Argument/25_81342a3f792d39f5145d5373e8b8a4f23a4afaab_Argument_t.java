 package org.daisy.pipeline.client.models.script;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.daisy.pipeline.client.Pipeline2WS;
 import org.daisy.pipeline.client.Pipeline2WSException;
 import org.daisy.pipeline.utils.XML;
 import org.daisy.pipeline.utils.XPath;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /** A script argument. */
 public abstract class Argument {
 	/** The name of the argument. This isn't necessarily unique; since inputs and options can have the same name. */ 
 	public String name;
 	
 	/** This is the value from the px:role="name" in the script documentation. */
 	public String nicename;
 	
 	/** A description of the option. */
 	public String desc;
 	
 	/** whether or not this argument is required */
 	public Boolean required;
 	
 	/** whether or not multiple selections can be made */
 	public Boolean sequence;
 	
 	/** whether or not the ordering matters (only relevant if sequence==true) */
 	public Boolean ordered;
 	
 	/** MIME types accepted (only relevant if type=anyDirURI or anyFileURI) */
 	public List<String> mediaTypes;
 	
 	/** XSD type */
 	public String xsdType = "";
 	
 	/** Arguments with a output value of "result" or "temp" will only be included when the framework is running in local mode. */
 	public String output;
 	
 	/** Type of underlying argument. Either "input", "parameters", "option" or "output". */
 	public String kind;
 	
 	// ---------- not part of the script metadata itself ----------
 	
 	/** Only relevant for file arguments. If mediaTypeBlacklist is defined, then all XML files are supported for this argument, except those listed in this list. */
 	public List<String> mediaTypeBlacklist;
 	
 	/** For use when rendering the job creation form */
 	public boolean hide;
 	
 	
 	public Argument(Node arg) throws Pipeline2WSException {
 		this.name = parseTypeString(XPath.selectText("@name", arg, Pipeline2WS.ns));
 		this.nicename = parseTypeString(XPath.selectText("@nicename", arg, Pipeline2WS.ns));
 		if (this.nicename == null || "".equals(this.nicename))
 			this.nicename = this.name;
 		this.desc = parseTypeString(XPath.selectText("@desc", arg, Pipeline2WS.ns));
 		this.required = parseTypeBoolean(XPath.selectText("@required", arg, Pipeline2WS.ns));
 		this.sequence = parseTypeBoolean(XPath.selectText("@sequence", arg, Pipeline2WS.ns));
 		this.ordered = parseTypeBoolean(XPath.selectText("@ordered", arg, Pipeline2WS.ns));
 		this.mediaTypes = parseTypeMediaTypes(XPath.selectText("@mediaType", arg, Pipeline2WS.ns));
 		this.xsdType = parseTypeString(XPath.selectText("@type", arg, Pipeline2WS.ns));
 		this.output = parseTypeString(XPath.selectText("@outputType", arg, Pipeline2WS.ns));
 		this.kind = arg.getLocalName(); // TODO "parameters": how to determine that a port is a parameter port?
 		
 		if ("input".equals(this.kind) || "output".equals(this.kind)) {
 			this.xsdType = "anyFileURI";
 			
 			if (this.mediaTypes.size() == 0)
 				this.mediaTypes.add("application/xml");
 		}
 	}
 	
 	/** Helper function for the Script(Document) constructor */
 	private static String parseTypeString(String string) {
 		if (!(string instanceof String))
 			return null;
 		string = string.replaceAll("\"", "'").replaceAll("\\n", " ");
 		if ("".equals(string)) return null;
 		else return string;
 	}
 	
 	/** Helper function for the Script(Document) constructor */
 	private static Boolean parseTypeBoolean(String bool) {
 		if (!(bool instanceof String))
 			return null;
 		if ("false".equals(bool))
 			return false;
 		if ("true".equals(bool))
 			return true;
 		return null;
 	}
 	
 	/** Helper function for the Script(Document) constructor */
 	private static List<String> parseTypeMediaTypes(String mediaTypesString) {
 		if (!(mediaTypesString instanceof String))
 			return new ArrayList<String>();
 		mediaTypesString = parseTypeString(mediaTypesString);
 		String[] mediaTypes = (mediaTypesString==null?"":mediaTypesString).split(" ");
 		List<String> mediaTypesList = new ArrayList<String>();
 		for (String mediaType : mediaTypes) {
 			if ("".equals(mediaType))
 				continue;
 			
 			if ("text/xml".equals(mediaType))
 				mediaTypesList.add("application/xml");
 			else
 				mediaTypesList.add(mediaType);
 		}
 		return mediaTypesList;
 	}
 	
 	public abstract Node asDocumentElement(Document document);
 	
 	public abstract void parseFromJobRequest(Node jobRequest) throws Pipeline2WSException;
 	
 	public String toString() {
 		String xml = XML.toString(asDocumentElement(XML.getXml("<doc/>")));
 		if (xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"))
 			xml = xml.substring(38);
 		return xml;
 	}
 	
 	public void set(Integer i, byte value) { set(i, value+""); }
 	public void set(Integer i, short value) { set(i, value+""); }
 	public void set(Integer i, int value) { set(i, value+""); }
 	public void set(Integer i, long value) { set(i, value+""); }
 	public void set(Integer i, float value) { set(i, value+""); }
 	public void set(Integer i, double value) { set(i, value+""); }
 	public void set(Integer i, boolean value) { set(i, value+""); }
 	public void set(Integer i, Object value) {
 		set(i == null ? null : i+"", value);
 	}
 	
 	public void set(Object key, byte value) { set(key, value+""); }
 	public void set(Object key, short value) { set(key, value+""); }
 	public void set(Object key, int value) { set(key, value+""); }
 	public void set(Object key, long value) { set(key, value+""); }
 	public void set(Object key, float value) { set(key, value+""); }
 	public void set(Object key, double value) { set(key, value+""); }
 	public void set(Object key, boolean value) { set(key, value+""); }
 	public abstract void set(Object key, Object value);
 	
 	public void set(byte value) { set(value+""); }
 	public void set(short value) { set(value+""); }
 	public void set(int value) { set(value+""); }
 	public void set(long value) { set(value+""); }
 	public void set(float value) { set(value+""); }
 	public void set(double value) { set(value+""); }
 	public void set(boolean value) { set(value+""); }
 	public abstract void set(Object value);
 	
 	public void add(byte value) { add(value+""); }
 	public void add(short value) { add(value+""); }
 	public void add(int value) { add(value+""); }
 	public void add(long value) { add(value+""); }
 	public void add(float value) { add(value+""); }
 	public void add(double value) { add(value+""); }
 	public void add(boolean value) { add(value+""); }
 	public abstract void add(Object value);
 	
 	public void remove(byte value) { remove(value+""); }
 	public void remove(short value) { remove(value+""); }
 	public void remove(int value) { remove(value+""); }
 	public void remove(long value) { remove(value+""); }
 	public void remove(float value) { remove(value+""); }
 	public void remove(double value) { remove(value+""); }
 	public void remove(boolean value) { remove(value+""); }
 	public abstract void remove(Object value);
 	
 	public abstract void clear();
 	
 	public abstract String get();
 	
	public String get(byte value) { return get(value+""); }
	public String get(short value) { return get(value+""); }
	public String get(int value) { return get(value+""); }
	public String get(long value) { return get(value+""); }
	public String get(float value) { return get(value+""); }
	public String get(double value) { return get(value+""); }
	public String get(boolean value) { return get(value+""); }
 	public abstract String get(Object key);
 	
 	public abstract int size();
 }
