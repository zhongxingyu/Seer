 package com.eventfabric.model;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Map;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 
 public class Event {
 	private String channel;
 	private ObjectNode value;
 
 	/**
 	 * Create an object based on the JSON Tree Model (used internally).
 	 * 
 	 * @see <a href=
 	 *      "http://wiki.fasterxml.com/JacksonInFiveMinutes#Tree_Model_Example"
 	 *      >Jackson's Tree Model example</a>
 	 */
 	public Event(String channel, ObjectNode value) {
 
        this.channel = channel;
 		this.value = value;
 	}
 
 	/**
 	 * Create an object based on the JSON Simple Data Binding.
 	 * 
 	 * @see <a href=
 	 *      "http://wiki.fasterxml.com/JacksonInFiveMinutes#Simple_Data_Binding_Example"
 	 *      >Jacksons's Simple Data Binding example</a>
 	 */
	public Event(String channel, Map<String, Object> value) {
 
        this.channel = channel;
 
 		ObjectMapper mapper = new ObjectMapper();
 		try {
 			// Auto-convert Map to a JSON string...
 			StringWriter str = new StringWriter();
 			mapper.writeValue(str, value);
 
 			// ... and parse it back into a tree node
 			ObjectNode node = mapper
 					.readValue(str.toString(), ObjectNode.class);
 			this.value = node;
 		} catch (IOException e) {
 			this.value = mapper.createObjectNode();
 		}
 	}
 
 	public ObjectNode toJSON() {
 		ObjectMapper mapper = new ObjectMapper();
 		ObjectNode root = mapper.createObjectNode();
 
 		root.put("channel", channel);
 		root.put("value", value);
 
 		return root;
 	}
 
 	public String toJSONString() throws IOException {
 		ObjectNode json = toJSON();
 
 		ObjectMapper mapper = new ObjectMapper();
 
 		return mapper.writeValueAsString(json);
 	}

    public String getChannel() {
        return this.channel;
    }

    public ObjectNode getValue() {
        return this.value;
    }
 }
