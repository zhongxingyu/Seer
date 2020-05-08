 package pipeline2.models;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import pipeline2.Pipeline2WS;
 import pipeline2.Pipeline2WSResponse;
 import pipeline2.models.script.Argument;
 import pipeline2.models.script.Author;
 import pipeline2.models.script.Homepage;
 import play.Logger;
 import play.libs.XPath;
 import utils.XML;
 
 /** Information about the current script. */
 public class Script {
 	
 	public String id;
 	public String href;
 	public String nicename;
 	public String desc;
 	public Homepage homepage;
 	public Author author;
 	public List<Argument> arguments;
 	
 	/** List of mime types that are supported by multiple file arguments, and thus cannot be automatically assigned to a single argument. */
 	public List<String> mediaTypeBlacklist;
 	
	/** For use when rendering the job creation form; not part of the script metadata */
	public boolean hideAdvancedOptions;
	
 	// ---------- Constructors ----------
 	
 	public Script() {
 		this.arguments = new ArrayList<Argument>();
 		this.mediaTypeBlacklist = new ArrayList<String>();
 	}
 	
 	public Script(Pipeline2WSResponse response) {
 		this(XPath.selectNode("/d:script", response.asXml(), Pipeline2WS.ns));
 	}
 	
 	/** Parse a Script XML document retrieved from the Pipeline 2 Web Service and create Helper function for the Script(Document) constructor */
 	public Script(Node scriptXml) {
 		this();
 		
 		this.id = XPath.selectText("@id", scriptXml, Pipeline2WS.ns);
 		this.href = XPath.selectText("@href", scriptXml, Pipeline2WS.ns);
 		this.nicename = XPath.selectText("d:nicename", scriptXml, Pipeline2WS.ns);
 		this.desc = XPath.selectText("d:description", scriptXml, Pipeline2WS.ns);
 		this.homepage = new Homepage(XPath.selectText("d:homepage", scriptXml, Pipeline2WS.ns), "");
 		
 		List<Node> inputNodes = XPath.selectNodes("d:input", scriptXml, Pipeline2WS.ns);
 		List<Node> optionNodes = XPath.selectNodes("d:option", scriptXml, Pipeline2WS.ns);
 		List<Node> outputNodes = XPath.selectNodes("d:output", scriptXml, Pipeline2WS.ns);
 		
 		for (Node inputNode : inputNodes) {
 			Argument arg = new Argument();
 			
 			arg.name = parseTypeString(XPath.selectText("@name", inputNode, Pipeline2WS.ns));
 			arg.nicename = parseTypeString(XPath.selectText("@nicename", inputNode, Pipeline2WS.ns));
 			if (arg.nicename == null || "".equals(arg.nicename))
 				arg.nicename = arg.name;
 			arg.desc = parseTypeString(XPath.selectText("@desc", inputNode, Pipeline2WS.ns));
 			arg.required = parseTypeBoolean(XPath.selectText("@required", inputNode, Pipeline2WS.ns), true);
 			arg.sequence = parseTypeBoolean(XPath.selectText("@sequence", inputNode, Pipeline2WS.ns), false);
 			arg.ordered = parseTypeBoolean(XPath.selectText("@ordered", inputNode, Pipeline2WS.ns), true);
 			arg.mediaTypes = parseTypeMediaTypes(XPath.selectText("@mediaType", inputNode, Pipeline2WS.ns));
 			if (arg.mediaTypes.size() == 0)
 				arg.mediaTypes.add("application/xml");
 			arg.xsdType = "anyFileURI";
 			arg.output = ""; // irrelevant, but let's give it a value
 			arg.kind = "input"; // TODO "parameters": is there a @kind attribute in the Web API?
 			
 			this.arguments.add(arg);
 		}
 
 		for (Node optionNode : optionNodes) {
 			Argument arg = new Argument();
 			
 			arg.name = parseTypeString(XPath.selectText("@name", optionNode, Pipeline2WS.ns));
 			arg.nicename = parseTypeString(XPath.selectText("@nicename", optionNode, Pipeline2WS.ns));
 			if (arg.nicename == null || "".equals(arg.nicename))
 				arg.nicename = arg.name;
 			arg.desc = parseTypeString(XPath.selectText("@desc", optionNode, Pipeline2WS.ns));
 			arg.required = parseTypeBoolean(XPath.selectText("@required", optionNode, Pipeline2WS.ns), true);
 			arg.sequence = parseTypeBoolean(XPath.selectText("@sequence", optionNode, Pipeline2WS.ns), false);
 			arg.ordered = parseTypeBoolean(XPath.selectText("@ordered", optionNode, Pipeline2WS.ns), true);
 			arg.mediaTypes = parseTypeMediaTypes(XPath.selectText("@mediaType", optionNode, Pipeline2WS.ns));
 			arg.xsdType = parseTypeString(XPath.selectText("@type", optionNode, Pipeline2WS.ns));
 			arg.output = parseTypeString(XPath.selectText("@output", optionNode, Pipeline2WS.ns));
 			arg.kind = "option";
 			
 			this.arguments.add(arg);
 		}
 
 		for (Node outputNode : outputNodes) {
 			Argument arg = new Argument();
 			
 			arg.name = parseTypeString(XPath.selectText("@name", outputNode, Pipeline2WS.ns));
 			arg.nicename = parseTypeString(XPath.selectText("@nicename", outputNode, Pipeline2WS.ns));
 			if (arg.nicename == null || "".equals(arg.nicename))
 				arg.nicename = arg.name;
 			arg.desc = parseTypeString(XPath.selectText("@desc", outputNode, Pipeline2WS.ns));
 			arg.required = parseTypeBoolean(XPath.selectText("@required", outputNode, Pipeline2WS.ns), true);
 			arg.sequence = parseTypeBoolean(XPath.selectText("@sequence", outputNode, Pipeline2WS.ns), false);
 			arg.ordered = parseTypeBoolean(XPath.selectText("@ordered", outputNode, Pipeline2WS.ns), true);
 			arg.mediaTypes = parseTypeMediaTypes(XPath.selectText("@mediaType", outputNode, Pipeline2WS.ns));
 			if (arg.mediaTypes.size() == 0)
 				arg.mediaTypes.add("application/xml");
 			arg.xsdType = "anyFileURI";
 			arg.output = parseTypeString(XPath.selectText("@output", outputNode, Pipeline2WS.ns));
 			arg.kind = "output";
 			
 			this.arguments.add(arg);
 		}
 		
 		Map<String,Integer> mediaTypeOccurences = new HashMap<String,Integer>();
 		for (Argument arg : this.arguments) {
 			for (String mediaType : arg.mediaTypes) {
 				if (mediaTypeOccurences.containsKey(mediaType)) {
 					mediaTypeOccurences.put(mediaType, mediaTypeOccurences.get(mediaType)+1);
 				} else {
 					mediaTypeOccurences.put(mediaType, 1);
 				}
 			}
 		}
 		for (String mediaType : mediaTypeOccurences.keySet()) {
 			if (mediaTypeOccurences.get(mediaType) > 1)
 				this.mediaTypeBlacklist.add(mediaType);
 		}
 	}
 	
 	public static List<Script> getScripts(Pipeline2WSResponse response) {
 		List<Script> scripts = new ArrayList<Script>();
 		
 		List<Node> scriptNodes = XPath.selectNodes("/d:scripts/d:script", response.asXml(), Pipeline2WS.ns);
 		for (Node scriptNode : scriptNodes) {
 			scripts.add(new Script(scriptNode));
 		}
 		
 		return scripts;
 	}
 	
 	/** Helper function for the Script(Document) constructor */
 	private static String parseTypeString(String string) {
 		if (!(string instanceof String))
 			return null;
 		return string.replaceAll("\"", "'").replaceAll("\\n", " ");
 	}
 	
 	/** Helper function for the Script(Document) constructor */
 	private static boolean parseTypeBoolean(String bool, boolean def) {
 		if (!(bool instanceof String))
 			return def;
 		if ("false".equals(bool))
 			return false;
 		if ("true".equals(bool))
 			return true;
 		return def;
 	}
 	
 	/** Helper function for the Script(Document) constructor */
 	private static List<String> parseTypeMediaTypes(String mediaTypesString) {
 		if (!(mediaTypesString instanceof String))
 			return new ArrayList<String>();
 		Logger.debug("mediaTypeString: '"+mediaTypesString+"'");
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
 	
 }
