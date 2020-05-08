 package com.vaguehope.takeshi.model;
 
 import java.util.List;
 
 import net.vz.mongodb.jackson.ObjectId;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 
 import com.google.common.base.Objects;
 
 public class Castle {
 
	@ObjectId @JsonProperty("_id") private String id;
 	private String name;
 	private List<CastleNode> nodes;
 
 	public Castle () {}
 
 	public Castle (String name) {
 		this.name = name;
 	}
 
 	public String getId () {
 		return this.id;
 	}
 
 	public String getName () {
 		return this.name;
 	}
 
 	public List<CastleNode> getNodes () {
 		return this.nodes;
 	}
 
 	@Override
 	public String toString () {
 		return Objects.toStringHelper(this)
 				.add("id", this.id)
 				.add("nodes", this.nodes)
 				.toString();
 	}
 
 }
