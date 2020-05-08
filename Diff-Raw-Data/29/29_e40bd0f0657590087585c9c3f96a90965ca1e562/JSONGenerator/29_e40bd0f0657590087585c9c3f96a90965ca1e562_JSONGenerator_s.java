 package ee.stacc.transformer.client.data.json;
 
 import java.util.Map;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 
 import ee.stacc.transformer.client.data.CollectedDataValue;
 import ee.stacc.transformer.client.data.GeneratedDataGroupsCollection;
 import ee.stacc.transformer.client.data.GeneratedDataGroup;
 import ee.stacc.transformer.client.data.GeneratedDataValue;
 import ee.stacc.transformer.client.data.AtomicDataValue;
 import ee.stacc.transformer.client.data.CollectedDataGroup;
 import ee.stacc.transformer.client.data.CollectedDataGroupsCollection;
 import ee.stacc.transformer.client.mapping.Mapping;
 import ee.stacc.transformer.client.mapping.MappingElement;
 import ee.stacc.transformer.client.mapping.RepeatingMappingsGroup;
 
 /**
  * To generate JSON messages.
  * 
  * @author Rainer Villido
  *
  */
 public class JSONGenerator {
 	
 	//JSON element names.
 	private static String PROPERTIES = "properties";
 	private static String TYPE = "type";
 	private static String OBJECT = "object";
 	private static String ARRAY = "array";
 	private static String ITEMS = "items";
 	private static String DEFAULT = "default";
 	
 	//private Map<String, CollectedDataValue> dataValues; //Key: path
 	private JsonDataPackage jsonDataPackage;
 	
 	public JSONGenerator(JsonDataPackage jsonDataPackage) {
 		GWT.log("Initiating JSON generator", null);
 		this.jsonDataPackage = jsonDataPackage;
 	}
 	
 	//Generate a new message from the data in a data package.
 	public JSONValue transformToJSON() {
 		JSONObject schema = jsonDataPackage.getJsonSchema();
 		GWT.log("Got following schema: "+schema.toString(), null);
 		
 		return transformToJSON(schema, "", jsonDataPackage.getDataValues());
 	}
 
 	/**
 	 * To transform schema to a JSON object. It is a recursive method.
 	 * @param schema	schema to transform.
 	 * @param path
 	 * @return
 	 */
 	public JSONValue transformToJSON(JSONValue schema, String path, Map<String, GeneratedDataValue> dataValues) {
 		//TODO: refactor and make it look less complex
 		//TODO: what if all elements in a repeatable element group have default values set. How to avoid infinite loop and how many elements to send then? 
 		
 		if(schema.isObject()!=null) {
 			JSONObject jsonSchema = schema.isObject();
 
 			String type = jsonSchema.containsKey(TYPE) ? jsonSchema.get(TYPE).isString().stringValue() : null;
 			if(ARRAY.equalsIgnoreCase(type) && jsonSchema.containsKey(ITEMS)) {
 				//fill the arrays in the schema.
 				
 				JSONObject items = jsonSchema.get(ITEMS).isObject();
 				JSONArray array = new JSONArray();
 				
 				if(dataValues.get(path) != null && dataValues.get(path).isGeneratedDataGroupsCollection() != null) {
 					GeneratedDataGroupsCollection dataCollection = dataValues.get(path).isGeneratedDataGroupsCollection();
 					int i = 0;
 					for(GeneratedDataGroup dataGroup: dataCollection.getFinishedDataGroups()) {
 						JSONValue arrayValue = transformToJSON(items, path, dataGroup.getAssembledData());
 						array.set(i, arrayValue);
 						i++;
 					}
 				}
 				
 				return array;
 			}
 			else if(OBJECT.equalsIgnoreCase(type) && jsonSchema.containsKey(PROPERTIES)) {
 				//Handle object properties
 				JSONObject properties = jsonSchema.get(PROPERTIES).isObject();
 				JSONObject resultJSON = new JSONObject();
 				
 				for(String propertyName: properties.keySet()) {
 					//For each property that the sub-schema has
 					JSONValue propertySchema = properties.get(propertyName);
 					resultJSON.put(propertyName, transformToJSON(propertySchema, path+"/"+propertyName, dataValues));
 				}
 				
 				return resultJSON;
 				
 			}
 			else {
 				//if schema doesn't have properties, meaning that it is an atomic data value
 				//Get return a collected value according to the path
 				if(dataValues.containsKey(path)) {
 					AtomicDataValue values = (AtomicDataValue)dataValues.get(path);	//TODO: check if it indeed is atomic data value
 					JSONValue jsonValue = values.getJson(type);
 					return jsonValue;	
 				}
 				else if (jsonDataPackage.getDataFrame().getConstantValues().containsKey(path)) {
 					//If a constant data value is set in the mappings then return the constant data value.
 					String constantValue = jsonDataPackage.getDataFrame().getConstantValues().get(path);
 					return JsonDataValue.generateJSONValue(constantValue, type);
 				}
 				else if(jsonSchema.containsKey(DEFAULT)) {
 					//If the schema has stored a default value.
 					//Then return the default value
 					return jsonSchema.get(DEFAULT);
 				}
 				else {
 					//If collected data values doesn't have right value stored then it means that the mappings are incomplete.
 					//We will return an empty string instead.
 					GWT.log("Incorrect path: "+path, null);
 					GWT.log("Didn't find proper value stored according to the path. The mappings are incomplete", null);
 					return new JSONString("");
 				}
 			}
 		}
 		else 
 			return new JSONObject();
 	}
 	
 	/**
 	 * Method for getting the value according to the path.
 	 * @param jsonValue
 	 * @param path
 	 * @return
 	 */
 	private static JSONValue getJsonDataValue(JSONValue jsonValue, String path) {
 		String[] pathlets = path.split("/");
 		
 		for(String part:pathlets) {
 			if(part.isEmpty())
 				continue;
 			if(jsonValue.isObject() != null) {
 				jsonValue = jsonValue.isObject().get(part);
 			}
 			//TODO: what if it's an array (in case of a repeating element
 			else {
 				GWT.log("Parsing of JSON data failed with path "+path, null);
 				GWT.log("Part is :"+part, null);
 				GWT.log("JSONValue is: "+jsonValue.toString(), null);
 			}
 		}
 		
 		return jsonValue;
 	}
 	
 	/**
 	 * Retrieve a JSON object of a data value from the message object according to the path and mapping.
 	 * @param data	message object.
 	 * @param path	path of the data value.
 	 * @param mapping	mapping representing the data value.
 	 * @return
 	 */
 	public static JSONValue getJsonDataValue(JavaScriptObject data, String path, Mapping mapping) {
 		if(mapping.isRepeatingMappingsGroup() != null && path.equals("/")) {
 			return getJsonDataValue(new JSONArray(data), path);
 		}
 		else {
 			return getJsonDataValue(new JSONObject(data), path);
 		}
 	}
 
 
 	/***
 	 * Generate data values groups collection out of the json data.
 	 * @param mappingGroup	mappings group that describes the data.
 	 * @return	collection of data value groups.
 	 */
 	public static CollectedDataGroupsCollection getJsonDataGroups(JSONArray jsonArray, RepeatingMappingsGroup mappingGroup) {
 		CollectedDataGroupsCollection groupsCollection = new CollectedDataGroupsCollection(mappingGroup);
 		
 		String superElementPaht = mappingGroup.getPath();
 		
 		for(int i = 0; i < jsonArray.size(); i++) {
 			JSONValue jsonData = jsonArray.get(i);
 			
 			CollectedDataGroup valuesGroup = new CollectedDataGroup(mappingGroup);
 			for(Mapping mapping: mappingGroup.getMappingsSet()) {
 				String longPath = mapping.getPath();
 				String path = longPath.replaceFirst(superElementPaht, "");
				
				if(mapping.isMappingElement() != null) {
					JSONValue jsonDataValue = getJsonDataValue(jsonData, path);
           MappingElement mappingElement = mapping.isMappingElement();
           JsonDataValue dataValue = new JsonDataValue(jsonDataValue, mappingElement);
           for (String globalRef : mappingElement.getGlobalReference()) {
             valuesGroup.addDataValue(globalRef, dataValue);
           }
				}
 				else if(mapping.isRepeatingMappingsGroup() != null) {
					
					RepeatingMappingsGroup repMappingsGroup = mapping.isRepeatingMappingsGroup();
					JSONArray jsonDataValue = getJsonDataValue(jsonData, path).isArray();
					CollectedDataGroupsCollection collectedCollection = getJsonDataGroups(jsonDataValue, repMappingsGroup);
 					
 					for(String globalRef: repMappingsGroup.getMappings().keySet()) {
 						valuesGroup.addDataValue(globalRef, collectedCollection);
 					}
 					
 				}
 			}
 			groupsCollection.addCollectedDataGroup(valuesGroup);
 		}
 		
 		return groupsCollection;
 	}
 	
 	public static CollectedDataValue getJsonDataValue(JSONValue jsonDataValue, Mapping mapping) {
 		
 		if(mapping.isMappingElement() != null) {
 			JsonDataValue dataValue = new JsonDataValue(jsonDataValue, mapping.isMappingElement());
       // at this point, data value must have a certain semantic reference
 			String globalRef = mapping.isMappingElement().getFirstGlobalReference();
 			dataValue.setGlobalReference(globalRef);
 			return dataValue;
 		}
 		else if(mapping.isRepeatingMappingsGroup() != null) {
 			RepeatingMappingsGroup mappingGroup = mapping.isRepeatingMappingsGroup();
 			JSONArray jsonArray = jsonDataValue.isArray();
 			CollectedDataGroupsCollection groupsCollection = JSONGenerator.getJsonDataGroups(jsonArray, mappingGroup);
 			return groupsCollection;
 		}
 		
 		return null;
 	}
 }
