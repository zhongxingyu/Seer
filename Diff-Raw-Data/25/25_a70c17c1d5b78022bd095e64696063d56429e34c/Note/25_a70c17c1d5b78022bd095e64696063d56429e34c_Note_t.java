 package ltg.ps.helioroom;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.JsonNodeFactory;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.mongodb.BasicDBObject;
 
 public class Note {
 	
 	private String type;
 	private String origin;
 	private String anchor;
 	private String color;
 	private String reason;
 	
 	public Note(String type, String origin, String anchor, String color, String reason) {
 		this.type = type;
 		this.origin = origin;
 		this.anchor = anchor;
 		this.color = color;
 		this.reason = reason;
 	}
 	
	public Note(String origin, JsonNode n) {
 		this.type = n.get("type").textValue();
		this.origin = origin;
 		this.anchor = n.get("anchor").textValue();
 		this.color = n.get("color").textValue();
 		this.reason = n.get("reason").textValue();
 	}
 	
 	public Note(BasicDBObject o) {
 		this.type = o.getString("type");
 		this.origin = o.getString("origin");
 		this.anchor = o.getString("anchor");
 		this.color = o.getString("color"); 
 		this.reason = o.getString("reason");
 	}
 
 	public String getType() {
 		return type;
 	}
 	
 	public String getOrigin() {
 		return origin;
 	}
 
 	public String getAnchor() {
 		return anchor;
 	}
 
 	public String getColor() {
 		return color;
 	}
 
 	public String getReason() {
 		return reason;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Note))
 			return false;
 		Note n = (Note) obj;	
		if (!type.equals(n.getType()) || 
				!origin.equals(n.getOrigin()) || 
				!anchor.equals(n.getAnchor()) || 
				!color.equals(n.getColor()) || 
				!reason.equals(n.getReason()))
 			return false;
 		return true;
 	}
 	
 	
 	public JsonNode serialize() {
 		ObjectNode node = JsonNodeFactory.instance.objectNode();
 		node.put("type", type);
 		node.put("origin", origin);
 		node.put("anchor", anchor);
 		node.put("color", color);
 		node.put("reason", reason);
 		return node;
 	}
 	
 }
