 package com.apps4you.shared;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.UUID;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.DeserializationConfig;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 import org.codehaus.jackson.map.TreeMapper;
 import org.codehaus.jackson.node.ObjectNode;
 
 import com.apps4you.client.Client;
 
 public class Warrior{
 
 
 
 	public UUID getWarriorId() {
 		return warriorId;
 	}
 	public void setWarriorId(UUID warriorId) {
 		this.warriorId = warriorId;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public int getHealth() {
 		return health;
 	}
 	public void setHealth(int health) {
 		this.health = health;
 	}
 	public Origins getOrigin() {
 		return origin;
 	}
 	public void setOrigin(Origins origin) {
 		this.origin = origin;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	private UUID warriorId;
 	private String  name;
 	private int health;
 	private Origins origin;
 	private String description;
 
 	
 	public Warrior() {
 	
 		// TODO Auto-generated constructor stub
 	}
 	public Warrior(UUID warriorId, String name, int health, Origins origin, String description){
 		
 		this.warriorId = warriorId;
 		this.name = name;
 		this.health = health;
 		this.origin = origin;
 		this.description = description;
 	}
 
 
 	public Warrior(String name, Origins origin, String description){
 		
 		// Need to create a new warriorID
		this.warriorId = UUID.randomUUID();
 		this.name = name;
 		//assuming that we are creating a new warrior for the first time
 		this.health = 100;
 		this.origin = origin;
 		this.description = description;
 	}
 	
 @Override
 	public String toString() {
 		return "Warrior [warriorId=" + warriorId + ", name=" + name
 				+ ", health=" + health + ", origin=" + origin
 				+ ", description=" + description + "]";
 	}
 
 
 
 
 	
 
 	
 }
