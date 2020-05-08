 package wise2.converter.converters;
 
 import org.dom4j.Node;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Converts a Wise 2 DataGrid step into a Wise 4 Table step
  * @author geoffreykwan
  */
 public class TableConverter extends Converter {
 
 	/**
 	 * Get the JSONObject that we will put into the step. Since wise 2
 	 * does not provide any data in the export, we are just going to
 	 * create an empty table step
 	 * @param stepNode the xml step node
 	 * @return the step JSONObject
 	 */
 	protected JSONObject parseStepNode(Node stepNode) {
 		JSONObject stepNodeJSONObject = new JSONObject();
 		
 		try {
 			stepNodeJSONObject.put("numColumns", 0);
 			stepNodeJSONObject.put("numRows", 0);
 			stepNodeJSONObject.put("prompt", "");
 			stepNodeJSONObject.put("tableData", new JSONArray());
 			stepNodeJSONObject.put("type", getType());
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		return stepNodeJSONObject;
 	}
 	
 	/**
 	 * Determines the icon type for the step
 	 */
 	protected String getClassType() {
		return "table";
 	}
 
 	/**
 	 * Get the node type
 	 */
 	protected String getNodeType() {
 		return "TableNode";
 	}
 
 	/**
 	 * Get the name of the file we will write the JSON to
 	 */
 	protected String getStepFileName(int stepCounter) {
 		return "node_" + stepCounter + ".ta";
 	}
 
 	/**
 	 * Get the type that will be an attribute in the step JSON
 	 */
 	protected String getType() {
 		return "Table";
 	}
 }
