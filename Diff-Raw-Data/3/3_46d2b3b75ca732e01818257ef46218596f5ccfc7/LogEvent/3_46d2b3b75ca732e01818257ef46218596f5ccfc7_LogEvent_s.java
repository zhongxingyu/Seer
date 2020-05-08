 package io.datamine.DataMineClient.DataWrappers;
 
 import java.util.HashMap;
 
 public class LogEvent {
 	public Chunk chunk;
 	public World world;
 	public String type;
 	public HashMap<String, Object> additional;
 	public Boolean stackable = false;
 	
 	public LogEvent(World w, Chunk c, String type, Boolean stackable, HashMap<String, Object> additional)
 	{
 		this.world = w;
 		this.chunk = c;
 		this.type = type;
 		this.additional = additional;
 		this.stackable = stackable;
 	}
 	
 	public LogEvent(World w, Chunk c, String type, Boolean stackable)
 	{
 		this(w, c, type, stackable, new HashMap<String, Object>());
 	}
 	
 	public LogEvent(World w, Chunk c, String type)
 	{
 		this(w, c, type, false, new HashMap<String, Object>());
 	}
 	
 	public HashMap<String, Object> toHashMap()
 	{
 		HashMap<String, Object> ret = new HashMap<String, Object>();
 		
		ret.put("chunk", this.chunk.toHashMap());
 		ret.put("type", this.type);
 		ret.put("additional", this.additional);
 		
 		return ret;
 	}
 }
