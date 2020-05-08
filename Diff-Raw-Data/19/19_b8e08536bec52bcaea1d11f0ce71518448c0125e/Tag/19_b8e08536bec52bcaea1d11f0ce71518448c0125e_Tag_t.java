 package edu.rit.asksg.domain;
 
 import flexjson.JSON;
 import flexjson.JSONDeserializer;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
 import org.springframework.roo.addon.json.RooJson;
 import org.springframework.roo.addon.tostring.RooToString;
 
 import javax.persistence.CascadeType;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 @RooJavaBean
 @RooToString
 @RooJpaEntity
 @RooJson
 public abstract class Tag {
 
 	private String name;
 	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
 	private Set<AsksgUser> createdBy = new HashSet<AsksgUser>();
 
 	public static Tag fromJsonToTag(String json) {
 		return new JSONDeserializer<Tag>().use(null, TopicTag.class).deserialize(json);
 	}
 
 	@JSON(include = false)
 	public Set<AsksgUser> getCreatedBy() {
 		return this.createdBy;
 	}
 
	public void setName(String name) {
		this.name = name.trim();
	}

 }
