 package empower.sal.mapping;
 
 import empower.sal.xml.transform.XMLFormatter;
 import empower.sal.xml.transform.XSLTGenerator;
 import empower.sal.xml.xsd.XSDParser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONSerializer;
 
 abstract public class AbstractMappingManager {	
 	protected static final Logger log = Logger.getLogger( AbstractMappingManager.class);
 	
 	private XSDParser xsdParser;
 	String dataUploadId = null;
 	String mappingId = null;
 	
 	private Schema inputSchema;
 	
 	File targetDefinitionFile = null;
 	String inputFileName = "input.xml";
 
 	JSONObject targetDefinition = null;
 	JSONObject templateCache = null;
 	HashMap<String, JSONObject> elementCache = new HashMap<String, JSONObject>();
 	HashMap<String, JSONObject> parentCache = new HashMap<String, JSONObject>();
 	HashMap<String, JSONObject> groupsCache = new HashMap<String, JSONObject>();
 	
 	JSONObject documentation = null;
 	
 	private String getDocumentationForKey(String key) {
 		/*
 		if(documentation == null) {
 			File file = new File(targetDefinitionFile.getParent() + "/documentation.json");
 				
 			StringBuffer contents = new StringBuffer();
 			try {
 				BufferedReader reader = new BufferedReader(new FileReader(file));
 				if(reader != null) {
 					String line = null;
 					while((line = reader.readLine()) != null) {
 						contents.append(line).append(System.getProperty("line.separator"));
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			documentation = (JSONObject) JSONSerializer.toJSON(contents.toString());
 		}
 		
 		String result = "";
 		if(documentation.has(key)) {
 			result = documentation.getString(key);
 		} else {
 			result = "No documentation for '" + key + "'";
 		}
 		*/
 		
 		String result = "No documentation for '" + key + "'";
 		return result;
 	}
 	
 	public AbstractMappingManager() {
 	}
 	
 	public XSDParser getXSDParser()
 	{
 		if(this.xsdParser == null) {
 			String schemaFileName = targetDefinitionFile.getParent() + "/" + targetDefinition.getString("xsd");
 			this.xsdParser = new XSDParser(schemaFileName);				
 		}
 		
 		return this.xsdParser;
 	}
 	
 	public void setXSDParser(XSDParser parser) {
 		this.xsdParser = parser;
 	}
 	
 //	protected Schema loadInputSchema(String input);
 	abstract protected Schema loadInputSchema(String input, String inputType);
 	
 //	protected JSONObject loadOutputTarget(String output);
 	abstract protected JSONObject loadOutputTarget(String output, String outputType);
 	
 	abstract protected JSONObject loadSavedMapping(String mapping);
 	abstract protected void saveMappings();
 	
 	
 	public JSONObject loadOutputTarget(String output) {		
 		return loadOutputTarget(output, null);
 	}
 
 	public Schema loadInputSchema(String input) {		
 		return loadInputSchema(input, null);
 	}
 
 	public void init(String input, String inputType, String mapping, String output, String outputType) {
 		Schema schema = this.loadInputSchema(input, inputType);
 		JSONObject target = this.loadOutputTarget(output, outputType);
 		JSONObject saved = this.loadSavedMapping(mapping);
 
 		this.init(schema, saved, target);
 	}
 	
 	private void init(Schema input, JSONObject mapping, JSONObject output) {
 		this.templateCache = null;
 		this.groupsCache.clear();
 		this.elementCache.clear();
 		this.parentCache.clear();
 		
 		this.setInputSchema(input);
 		
 		//targetDefinitionFile = new File(output);
 		if(targetDefinitionFile != null) { this.inputFileName = targetDefinitionFile.getParent() + "/input.xml"; }
 
 		if(mapping != null) {
 			targetDefinition = mapping;
 			JSONArray groups = mapping.getJSONArray("groups");
 			for(int i = 0; i < groups.size(); i++) {
 				JSONObject group = groups.getJSONObject(i);
 				JSONObject contents = group.getJSONObject("contents");
 				String element = group.getString("element");
 				this.groupsCache.put(element, contents);
 				this.cacheElements(contents);
 			}
 		} else if(output != null) {
 			targetDefinition = output;
 			JSONArray groups = output.getJSONArray("groups");
 			for(int i = 0; i < groups.size(); i++) {
 				JSONObject group = groups.getJSONObject(i);
 				if(group.has("contents")) {
 					JSONObject contents = group.getJSONObject("contents");
 					String element = group.getString("element");
 					this.groupsCache.put(element, contents);
 					this.cacheElements(contents);
 				}
 			}
 		}
 		
 		// initialise namespaces
 		if(this.targetDefinition.has("namespaces")) {
 			JSONObject object = this.targetDefinition.getJSONObject("namespaces");
 			HashMap<String, String> map = new HashMap<String, String>();
                         
                         if(mapping != null) 
                         {
                         for(Object entry : object.keySet()) {
 				String key = (String) entry;
 				String value = object.getString(key);
                                 map.put(value, key);      
 			}
                         
                         }
                         else{
 			for(Object entry : object.keySet()) {
 				String key = (String) entry;
 				String value = object.getString(key);
 				map.put(key, value);       
 			}
                         }
 			
 			this.getXSDParser().setNamespaces(map);
 		}
 		
 		// initialise mapping definition
 		this.targetDefinition = this.getTargetDefinition();
 
 		// set namespaces
 		JSONObject namespaces = new JSONObject();
 
 		// xsd schema namespaces
 		Map<String, String> acc = this.getXSDParser().getNamespaces();
 		for(Entry<String, String> entry: acc.entrySet()) {
 			String key = entry.getKey();
 			String value = entry.getValue();
 			
 			namespaces = namespaces.element(value, key);
 		}
 		
 		this.targetDefinition = this.targetDefinition.element("namespaces", namespaces);
 				
 		// cache template
 		JSONObject template = null;
 		if(!this.targetDefinition.has("template") || this.targetDefinition.getJSONObject("template").isEmpty()) {
 			template = this.buildTemplate(this.targetDefinition.getJSONObject("item").getString("element"));
 			this.targetDefinition = this.targetDefinition.element("template", template);
 		} else {
 			template = targetDefinition.getJSONObject("template");
 		}
 		
 		this.templateCache = template;
 		this.cacheElements(this.templateCache);
 		
 		this.saveMappings();
 	}
 	
 	public void setArrayFixed(JSONArray array, boolean fixed) {
 		Iterator i = array.iterator();
 		while(i.hasNext()) {
 			JSONObject object = (JSONObject) i.next();
 			object = this.setFixedRecursive(object, fixed);
 		}
 	}
 	
 	public JSONObject setFixed(JSONObject object, boolean fixed) {
 		if(fixed) {
 			if(!object.has("fixed")) {
 				object = object.element("fixed", "");
 			}
 		} else {
 			if(object.has("fixed")) {
 				object.remove("fixed");
 			}
 		}
 		
 		return object;
 	}
 	
 	public JSONObject setFixedRecursive(JSONObject object, boolean fixed) {
 		this.setFixed(object, fixed);
 		if(object.has("attributes")) {
 			this.setArrayFixed(object.getJSONArray("attributes"), fixed);
 		}
 		
 		if(object.has("children")) {
 			this.setArrayFixed(object.getJSONArray("children"), fixed);
 		}
 		
 		return object;
 	}
 	
 	public JSONObject getElementDescription(String element) {
 		//log.debug("requested element description: " + element);
 		if(this.groupsCache.containsKey(element)) {
 //			log.debug("Reading JSON Object " + element + " from cache!");
 			return groupsCache.get(element);
 		} else {
 			JSONObject result = this.getXSDParser().getElementDescription(element);
 			this.groupsCache.put(element, result);
 			this.cacheElements(result);
 			return this.groupsCache.get(element);
 		}
 	}
 	
 	private void cacheElements(JSONObject object) {
 		String id = this.generateUniqueId();
 		object.put("id", id);
 		this.elementCache.put(id, object);
 
 		if(object.has("attributes")) {
 			JSONArray attributes = object.getJSONArray("attributes");
 			for(int i = 0; i < attributes.size(); i++) {
 				JSONObject a = (JSONObject) attributes.get(i);
 				this.cacheElements(a);
 				this.parentCache.put(a.getString("id"), object);
 			}
 		}
 		
 		if(object.has("children")) {
 			JSONArray children = object.getJSONArray("children");
 			for(int i = 0; i < children.size(); i++) {		
 				JSONObject a = (JSONObject) children.get(i);
 				this.cacheElements(a);
 				this.parentCache.put(a.getString("id"), object);
 			}
 		}
 	}
 	
 	private int elementid = 0;
 	private String generateUniqueId() {
 		elementid++;
 		return "" + elementid;
 	}	
 
 	public JSONObject getTargetDefinition() {
 		JSONArray groups = this.targetDefinition.getJSONArray("groups");
 		Iterator i = groups.iterator();
 		while(i.hasNext()) {
 			JSONObject item = (JSONObject) i.next();
 			String element = item.getString("element");
 			item.put("contents", this.getElementDescription(element));
 		}
 		
 		if(!this.targetDefinition.has("template") || this.targetDefinition.getJSONObject("template").isEmpty()) {
 			JSONObject template = this.buildTemplate(this.targetDefinition.getJSONObject("item").getString("element"));
 			this.templateCache = template;
 			this.cacheElements(this.templateCache);
 		}
 
 		this.targetDefinition.put("template", this.templateCache);
 
 		return this.targetDefinition;
 	}
 	
 	private JSONObject buildTemplate(String root) {
 		log.debug("building template element: " + root);
 	    JSONArray groups = this.targetDefinition.getJSONArray("groups");
 //		return this.getXSDParser().buildTemplate(groups, root);
 		return this.getXSDParser().buildTemplateFromComplexType(groups, root);
 	}
 	
 	public Schema getInputSchema() { return inputSchema; }	
 	public void setInputSchema(Schema schema) {
 		this.inputSchema = schema;
 	}
 	
 	public JSONObject setXPathMapping(String source, String target, int index) {
 		MappingElement sourceElement = this.inputSchema.getMappingElement(source);
 		JSONObject targetElement = this.elementCache.get(target);
 		String xpath = sourceElement.getXPath();
 		String type = sourceElement.getType();
 		
 //		setXPathMapping(xpath, targetElement, index);
 		setXPathMapping(xpath, type, targetElement, index);
 		saveMappings();
 		
 		return targetElement;
 	}
 	
 	public void setXPathMapping(String xpath, JSONObject target, int index) {
 		this.setXPathMapping(xpath, null, target, index);
 	}
 	
 	public void setXPathMapping(String xpath, String type, JSONObject target, int index) {
 		JSONArray mappings = target.getJSONArray("mappings");
 		JSONObject mapping = null;
 
 		target.remove("warning");
 
 		if(index > -1) {
 			mapping = mappings.getJSONObject(index);
 			mapping.put("type", "xpath");
 			mapping.put("value", xpath);
 		} else {
 			mapping = new JSONObject();
 			mapping.put("type", "xpath");
 			mapping.put("value", xpath);
 			mappings.add(mapping);
 		}
 		
 		if(mapping != null) {
 			if(target.has("type") && type != null && type.length() > 0) {
                                 if(target.getString("type").equalsIgnoreCase("dateUnion")) {
                                     if(!type.equalsIgnoreCase("date") && !type.equalsIgnoreCase("dateTime") && !type.equalsIgnoreCase("dateUnion")) {
                                         target.element("warning", type);
                                     }
                                 } else if(!target.getString("type").equalsIgnoreCase(type) || (mappings.size() > 1 && !type.equalsIgnoreCase("string"))) {
 					target.element("warning", type);
 				}
 			}
 		}
 
 		//mappings.clear();
 	}
 
 	public JSONObject setXPathFunction(String id, int index, String data) {
 		JSONObject target = this.elementCache.get(id);
 		JSONArray mappings = target.getJSONArray("mappings");
 		JSONObject mapping = null;
 		JSONObject function = (JSONObject) JSONSerializer.toJSON(data);
 
 		if(index > -1) {
 			mapping = mappings.getJSONObject(index);
 			mapping.put("func", function);
 		}
 
 		saveMappings();
 		
 		return target;
 	}
 	
 	public JSONObject clearXPathFunction(String id, int index) {
 		JSONObject target = this.elementCache.get(id);
 		JSONArray mappings = target.getJSONArray("mappings");
 		JSONObject mapping = null;
 
 		if(index > -1) {
 			mapping = mappings.getJSONObject(index);
 			mapping.remove("func");
 		}
 
 		saveMappings();
 		
 		return target;
 	}
 	
 	public JSONObject setConstantValueMapping(String target, String value, int index) {
 		JSONObject targetElement = this.elementCache.get(target);
 		if(targetElement == null) {
 			System.out.println("*** Could not find " + targetElement + " in element cache!");
 		}
 
 		setConstantValueMapping(targetElement, value, index);
 		saveMappings();
 		
 		return targetElement;
 	}
 	
 	public JSONObject setEnumerationValueMapping(String target, String value) {
 		JSONObject targetElement = this.elementCache.get(target);
 		if(targetElement == null) {
 			System.out.println("*** Could not find " + targetElement + " in element cache!");
 		}
 		
 		setEnumerationValueMapping(targetElement, value);
 		saveMappings();
 		
 		return targetElement;
 	}
 	
 	public void setConstantValueMapping(JSONObject target, String value, int index) {
 		JSONArray mappings = target.getJSONArray("mappings");
 		JSONObject mapping = null;
 
 		if(index > -1) {
 			mapping = mappings.getJSONObject(index);
 			mapping.put("type", "constant");
 			mapping.put("value", value);
 		} else {
 			mapping = new JSONObject();
 			mapping.put("type", "constant");
 			mapping.put("value", value);
 			mappings.add(mapping);
 		}
 	}
 
 	public void setEnumerationValueMapping(JSONObject target, String value) {
 		JSONArray mappings = target.getJSONArray("mappings");
 		JSONObject mapping = null;
 
 		mappings.clear();
 		if(value != null && value.length() > 0) {
 			mapping = new JSONObject();
 			mapping.put("type", "constant");
 			mapping.put("value", value);
 			mappings.add(mapping);
 		}
 	}
 
 	public JSONObject addCondition(String target) {
 		JSONObject targetElement = this.elementCache.get(target);
                
                
 		
 		targetElement.put("condition", new JSONObject().element("xpath", "").element("value", ""));
 		saveMappings();
 		
 		return targetElement;
 	}
 	
 	public JSONObject removeCondition(String target) {
 		JSONObject targetElement = this.elementCache.get(target);
 		
 		targetElement.remove("condition");
 		saveMappings();
 		
 		return targetElement;
 	}
 	
 	public JSONObject setConditionXPath(String target, String value) {
 		JSONObject targetElement = this.elementCache.get(target);
 		MappingElement sourceElement = this.inputSchema.getMappingElement(value);
 		String xpath = sourceElement.getXPath();
 		String type = sourceElement.getType();
 		
 		if(targetElement.has("condition")) {
 			log.debug("Set condition xpath for " + target + " to " + xpath);
 			targetElement.getJSONObject("condition").put("xpath", xpath);
 			targetElement.getJSONObject("condition").put("type", type);
 			targetElement.getJSONObject("condition").remove("value");
 			saveMappings();			
 		}
 		
 		return targetElement;		
 	}
 	
 	public JSONObject removeConditionXPath(String target) {
 		JSONObject targetElement = this.elementCache.get(target);
 		
 		if(targetElement.has("condition")) {
 			log.debug("remove condition xpath for " + target);
 			targetElement.getJSONObject("condition").put("xpath", "");
 			saveMappings();			
 		}
 		
 		return targetElement;		
 	}
 		
 	public JSONObject setConditionValue(String target, String value) {
 		JSONObject targetElement = this.elementCache.get(target);
 		
 		if(targetElement.has("condition")) {
 			log.debug("Set condition value for " + target + " to " + value);
 			targetElement.getJSONObject("condition").put("value", value);
 			saveMappings();			
 		}
 		
 		return targetElement;		
 	}
 	
 	public JSONObject removeConditionValue(String target) {
 		JSONObject targetElement = this.elementCache.get(target);
 		
 		if(targetElement.has("condition")) {
 			log.debug("remove condition value for " + target);
 			targetElement.getJSONObject("condition").put("value", "");
 			saveMappings();			
 		}
 		
 		return targetElement;		
 	}
 		
 	public JSONObject removeMappings(String target, int index) {
 		JSONObject targetElement = this.elementCache.get(target);
 
 		removeMappings(targetElement, index);
 		
 		saveMappings();
 
 		return targetElement;
 	}
 	
 	public void  removeMappings(JSONObject target, int index) {
 		JSONArray mappings = target.getJSONArray("mappings");
 		target.remove("warning");
 		
 		if(index > -1) {
 			mappings.remove(index);
 		}
 	}
 	
 	public JSONObject additionalMappings(String target, int index) {
 		JSONObject targetElement = this.elementCache.get(target);
 		JSONArray mappings = targetElement.getJSONArray("mappings");
 
 		JSONObject empty = new JSONObject()
 			.element("type", "empty")
 			.element("value", "");
 		
 		if(index > -1) {
 			mappings.add(index + 1, empty);
 		}
 		
 		saveMappings();
 		
 		return targetElement;
 	}
 	
 	public JSONObject objectForTargetXPath(String xpath) {
 		//System.out.println("objectForTargetXPath: " + xpath);
 
 		if(xpath.startsWith("/")) { xpath = xpath.replaceFirst("/", ""); }
 		String[] tokens = xpath.split("/");
 		if(tokens.length > 0) {
 			JSONObject result = null;
 			JSONObject group = this.groupsCache.get(tokens[0]);
 			System.out.println("objectForTargetXPath token: " + tokens[0]);
 
 			if(group != null) {
 //				System.out.println("group: " + group.getString("name"));
 //				JSONObject content = group.getJSONObject("contents");
 				result = this.objectForTargetXPath(group, xpath);
 				if(result != null) return result;
 			}
 		}
 
 		return null;
 	}
 	
 	public JSONObject objectForTargetXPath(JSONArray array, String xpath) {
 		Iterator i = array.iterator();
 		while(i.hasNext()) {
 			JSONObject object = (JSONObject) i.next();
 			JSONObject result = this.objectForTargetXPath(object, xpath);
 			if(result != null) return result;
 		}
 		return null;
 	}
 	
 	public JSONObject objectForTargetXPath(JSONObject object, String xpath) {
 		System.out.println("objectForTargetXPath: " + object.getString("name") + " - "  + xpath);
 
 		if(xpath.startsWith("/")) { xpath = xpath.replaceFirst("/", ""); }
 		String[] tokens = xpath.split("/");
 		if(tokens.length > 0) {
 			log.debug("looking path:" + xpath + " in object:" + object);
 			if(object.has("name")) {
 				if(tokens[0].equals(object.getString("name"))) {
 					if(tokens.length == 1) {
 						return object;
 					} else {
 						String path = tokens[1];
 						for(int i = 2; i < tokens.length; i++) {
 							path += "/" + tokens[i];
 						}
 	
 						if(path.startsWith("@")) {
 							if(object.has("attributes")) {
 								return this.objectForTargetXPath(object.getJSONArray("attributes"), path);
 							}
 						} else {
 							if(object.has("children")) {
 								return this.objectForTargetXPath(object.getJSONArray("children"), path);
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	public JSONObject duplicateObjectWithXPath(String xpath) {
 		System.out.println("duplicate object: " + xpath);
 		JSONObject result = null;
 		JSONObject object = this.objectForTargetXPath(xpath);
 
 		if(object != null) {
 			result = this.duplicateNode(object.getString("id"));
 			result = this.elementCache.get(result.getJSONObject("duplicate").getString("id"));
 		}
 		
 		return result;
 	}
 	
 	public JSONObject duplicateNode(String id) {
 		JSONObject targetElement = this.elementCache.get(id);
 		JSONObject parent = this.parentCache.get(id);
 
 		JSONObject duplicate = duplicateJSONObject(targetElement);
 
 		JSONArray children = parent.getJSONArray("children");
 		if(children != null) {
 			int index = -1;
 			for(int i = 0; i < children.size(); i++) {
 				JSONObject child = (JSONObject) children.get(i);
 				if(child.getString("id").equals(id)) {
 					index = i;
 					break;
 				}
 			}
 			
 			if(index >= 0) {
 				children.add(index, duplicate);
 				duplicate = children.getJSONObject(index);
 			}
 		} else {
 			JSONArray array = new JSONArray();
 			array.add(duplicate);
 			parent.put("children", array);
 			children = parent.getJSONArray("children");
 			duplicate = children.getJSONObject(0);
 		}
 		
 		this.cacheElements(duplicate);	
 		String duplicateId = duplicate.getString("id");
 		this.parentCache.put(duplicateId, parent);
 
 		this.saveMappings();
 
 		return new JSONObject()
 			.element("parent", parent.getString("id"))
 			.element("original", id)
 			.element("duplicate", duplicate);
 	}
 	
 	public JSONObject removeNode(String id) {
 		JSONObject result = new JSONObject();
 		JSONObject targetElement = this.elementCache.get(id);
 		JSONObject parent = this.parentCache.get(id);
 
 		JSONArray children = parent.getJSONArray("children");
 		if(children != null && !children.isEmpty()) {
 			int targetIndex = -1;
 			int targetCount = 0;
 			for(int i = 0; i < children.size(); i++) {
 				JSONObject child = (JSONObject) children.get(i);
 				if(child.getString("id").equals(id)) {
 					targetIndex = i;
 				}
 			}
 
 			if(targetIndex >= 0) {
 				children.remove(targetIndex);
 				this.elementCache.remove(id);
 				this.parentCache.remove(id);
 			}
 			
 			result = result.element("id", id);
 		} else {
 			result = result.element("error", "could not find target element");
 		}
 		
 		this.saveMappings();
 		
 		return result;
 	}
 	
 	private JSONObject duplicateJSONObject(JSONObject source) {
 		String json = source.toString();
 		JSONObject out = null;
 		
 		out = (JSONObject) JSONSerializer.toJSON(json);
 		out.put("duplicate", "");
 		clearAllMappings(out);
 
 		return out;
 	}
 	
 	private void clearAllMappings(JSONObject object) {
 		JSONArray mappings = object.getJSONArray("mappings");
 		mappings.clear();
 		
 		if(object.has("attributes")) {
 			JSONArray attributes = object.getJSONArray("attributes");
 			for(int i = 0; i < attributes.size(); i++) {
 				JSONObject a = (JSONObject) attributes.get(i);
 				clearAllMappings(a);
 			}
 		}
 		
 		if(object.has("children")) {
 			JSONArray children = object.getJSONArray("children");
 			for(int i = 0; i < children.size(); i++) {		
 				JSONObject a = (JSONObject) children.get(i);
 				clearAllMappings(a);
 			}
 		}
 	}
 	
 	public JSONObject previewTransform()
 	{
 		JSONObject preview = new JSONObject();
 
 		XSLTGenerator xslt = new XSLTGenerator();
 		
 		String templateMatch = "/";
 		String mappings = this.getTargetDefinition().toString();
                 		
                System.out.println("First 1000 caracters mappings = " + mappings.substring(0, 1000));
 		xslt.setTemplateMatch(templateMatch);
                 System.out.println("**** XSLT  = " + xslt.generateFromString(mappings));                
 		
 		String xsl = XMLFormatter.format(xslt.generateFromString(mappings));
 		
 		preview.element("json", "\n" + mappings);
 		preview.element("xsl", xsl);
 		
 		return preview;
 	}
 		
 	public JSONObject mappingElementsUsedInMapping()
 	{
 		JSONObject result = new JSONObject();
 		JSONArray used = new JSONArray();
 		JSONArray not_used = new JSONArray();
 		JSONArray parent_used = new JSONArray();
 
 		
 		JSONObject mappings = this.getTargetDefinition();		
 		Map<String, MappingElement> map = this.inputSchema.getMap();
 		Collection<String> list = MappingSummary.getMappedXPathList(mappings);
 
 		Iterator<String> keys = map.keySet().iterator();
 		
 		while(keys.hasNext()) {
 			String id = keys.next();
 			String xpath = map.get(id).getXPath();
 			for(String xp: list){
 				if(xp.length()>xpath.length()&& xp.indexOf(xpath)>-1){
 			      parent_used.add(id);
 			      break;  
 				}
 			}
 			if(list.contains(xpath)) {
 				used.add(id);
 				
 			} else {
 				not_used.add(id);
 			}
 		}
 		return result.element("used", used).element("not_used", not_used).element("parent_used",parent_used);
 	}
 	
 	public JSONObject getDocumentation(String id) {
 		JSONObject result = new JSONObject();
 		JSONObject targetElement = this.elementCache.get(id);
 		
 		String key = targetElement.getString("name");
 		result.element("title", key);
 		result.element("documentation", this.getDocumentationForKey(key));
 		
 		return result;
 	}
 	
 	public JSONObject initComplexCondition(String id) {
 		String defaultLogicalOp = "AND";
 		boolean conditionInit = false;
 		
 		JSONObject targetElement = this.elementCache.get(id);
 		
 		if(targetElement.has("condition")) {
 			JSONObject condition = targetElement.getJSONObject("condition");
 			if(!condition.has("logicalop")) {
 				condition.element("logicalop", defaultLogicalOp);
 				JSONArray clauses = new JSONArray();
 				JSONObject clause = new JSONObject();
 				if(condition.has("xpath") && condition.getString("xpath").length() > 0) { clause.element("xpath", condition.getString("xpath")); }
 				if(condition.has("value") && condition.getString("value").length() > 0) { clause.element("value", condition.getString("value")); }
 				if(condition.has("relationalop")) { clause.element("relationalop", condition.getString("=")); }
 				clauses.add(clause);
 				condition.element("clauses", clauses);
 				
 				conditionInit = true;
 			}
 		} else {
 			targetElement.element("condition", new JSONObject().element("logicalop", defaultLogicalOp).element("clauses", new JSONArray()));
 			conditionInit = true;
 		}
 
 		if(conditionInit) {
 			saveMappings();
 		}
 		
 		return targetElement.getJSONObject("condition");
 	}
 	
 	public JSONObject addConditionClause(String id, String path, boolean complex)
 	{
 		JSONObject result = new JSONObject();
 		JSONObject targetElement = this.elementCache.get(id);
 		if(targetElement.has("condition")) {
 			JSONObject condition = targetElement.getJSONObject("condition");
 			this.addConditionClause(condition, path, complex);
 			result = condition;
 			
 			saveMappings();
 		}
 		
 		return result;
 	}
 	
 	private void addConditionClause(JSONObject condition, String path, boolean complex) {
 		if(condition.has("clauses")) {
 			addConditionClause(condition.getJSONArray("clauses"), path, complex);
 		}
 	}
 	
 	private void addConditionClause(JSONArray clauses, String path, boolean complex) {
 		JSONObject clause = new JSONObject();
 		
 		if(complex) {
 			clause.element("logicalop", "AND");
 			JSONArray array = new JSONArray();
 			array.add(new JSONObject());
 			clause.element("clauses", array);
 		}
 		
 		if(path.length() == 0) {
 			clauses.add(clause);
 		} else {
 			if(path.contains(".")) {
 				String[] parts = path.split("\\.", 2);
 				System.out.println("'" + path + "' '" + parts[0] + "' '" + parts[1] + "'");
 				int index = Integer.parseInt(parts[0]);
 				addConditionClause(clauses.getJSONObject(index), parts[1], complex);
 			} else {
 				int index = Integer.parseInt(path);
 				addConditionClause(clauses.getJSONObject(index), "", complex);
 			}
 		}
 	}
 	
 	public JSONObject removeConditionClause(String id, String path)
 	{
 		JSONObject result = new JSONObject();
 		JSONObject targetElement = this.elementCache.get(id);
 		if(targetElement.has("condition")) {
 			JSONObject condition = targetElement.getJSONObject("condition");
 			this.removeConditionClause(condition, path);
 			result = condition;
 			
 			saveMappings();
 		}
 		
 		return result;
 	}
 	
 	private void removeConditionClause(JSONObject condition, String path) {
 		if(condition.has("clauses")) {
 			removeConditionClause(condition.getJSONArray("clauses"), path);
 		}
 	}
 	
 	private void removeConditionClause(JSONArray clauses, String path) {
 		if(path.length() > 0) {
 			if(path.contains(".")) {
 				String[] parts = path.split("\\.", 2);
 				int index = Integer.parseInt(parts[0]);
 				if(parts[1].length() > 0) {
 					removeConditionClause(clauses.getJSONObject(index), parts[1]);
 				} else {
 					clauses.remove(index);
 				}
 			} else {
 				int index = Integer.parseInt(path);
 				clauses.remove(index);
 			}
 		}
 	}
 	
 	public JSONObject setConditionClauseKey(String id, String path, String key, String value)
 	{
 		JSONObject result = new JSONObject();
 		JSONObject targetElement = this.elementCache.get(id);
 		if(targetElement.has("condition")) {
 			JSONObject condition = targetElement.getJSONObject("condition");
 			this.setConditionClauseKey(condition, path, key, value);
 			result = condition;
 			
 			saveMappings();
 		}
 		
 		return result;
 	}
 	
 	public JSONObject setConditionClauseXPath(String id, String path, String source)
 	{
 		JSONObject result = new JSONObject();
 		JSONObject targetElement = this.elementCache.get(id);
 		if(targetElement.has("condition")) {
 			MappingElement sourceElement = this.inputSchema.getMappingElement(source);
 			String value = sourceElement.getXPath();
 			String type = sourceElement.getType();
 			JSONObject condition = targetElement.getJSONObject("condition");
 			this.setConditionClauseKey(condition, path, "xpath", value);
 			this.setConditionClauseKey(condition, path, "type", type);
 			this.removeConditionClauseKey(condition, path, "value");
 			result = condition;
 			
 			saveMappings();
 		}
 		
 		return result;
 	}
 	
 	
 	private void setConditionClauseKey(JSONObject condition, String path, String key, String value) {
 		if(path.length() == 0) {
 			if(condition.has(key)) { condition.remove(key); }
 			condition.element(key, value);
 		} else {
 			if(condition.has("clauses")) {
 				JSONArray clauses = condition.getJSONArray("clauses");
 				if(path.contains(".")) {
 					String[] parts = path.split("\\.", 2);
 					int index = Integer.parseInt(parts[0]);
 					setConditionClauseKey(clauses.getJSONObject(index), parts[1], key, value);
 				} else {
 					int index = Integer.parseInt(path);
 					setConditionClauseKey(clauses.getJSONObject(index), "", key, value);
 				}
 			}			
 		}
 	}
 	
 	public JSONObject removeConditionClauseKey(String id, String path, String key)
 	{
 		JSONObject result = new JSONObject();
 		JSONObject targetElement = this.elementCache.get(id);
 		if(targetElement.has("condition")) {
 			JSONObject condition = targetElement.getJSONObject("condition");
 			this.removeConditionClauseKey(condition, path, key);
 			result = condition;
 			
 			saveMappings();
 		}
 		
 		return result;
 	}
 	
 	private void removeConditionClauseKey(JSONObject condition, String path, String key) {
 		if(path.length() == 0) {
 			condition.remove(key);
 		} else {
 			if(condition.has("clauses")) {
 				JSONArray clauses = condition.getJSONArray("clauses");
 				if(path.contains(".")) {
 					String[] parts = path.split("\\.", 2);
 					int index = Integer.parseInt(parts[0]);
 					removeConditionClauseKey(clauses.getJSONObject(index), parts[1], key);
 				} else {
 					int index = Integer.parseInt(path);
 					removeConditionClauseKey(clauses.getJSONObject(index), "", key);
 				}
 			}			
 		}
 	}
 	
 	public JSONObject mappingSummary()
 	{
 		JSONObject object = new JSONObject();
 //		DataUpload du = DB.getDataUploadDAO().findById(Long.parseLong(this.dataUploadId), false);
 		String mappings = this.getTargetDefinition().toString();
 		
 		Collection<String> missing = MappingSummary.getMissingMappings(mappings);
 //		Collection<String> invalid = MappingSummary.getInvalidXPaths(du, mappings);
 		Collection<String> invalid = new ArrayList<String>();
 		Map<String, String> mapped = MappingSummary.getMappedItems(mappings);
 //		Map<String, String> summary = MappingSummary.getSummary(mappings);
 //		JSONObject tree_usage = this.mappingElementsUsedInMapping();
 
 		object = object.element("missing", missing);
 		object = object.element("invalid", invalid);
 		object = object.element("mapped", mapped);
 //		object = object.element("used", tree_usage.getJSONArray("used"));
 //		object = object.element("not_used", tree_usage.getJSONArray("not_used"));
 		//object = object.element("summary", summary);
 		
 		//log.debug(object);
 		
 		return object;
 	}
 }
