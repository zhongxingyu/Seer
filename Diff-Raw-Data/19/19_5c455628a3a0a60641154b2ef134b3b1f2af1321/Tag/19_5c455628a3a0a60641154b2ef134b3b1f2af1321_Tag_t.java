 package com.aneesh.gae.domain;
 
import java.util.ArrayList;
 import java.util.List;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.IdentityType;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 @PersistenceCapable(identityType=IdentityType.APPLICATION)
 public class Tag {
 	
 	@PrimaryKey
 	@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
 	private Long id;
 
 	private String tag;
 	
 	@Persistent(mappedBy="tagObjects")
	private List<QuickThought> quickThoughts = new ArrayList<QuickThought>();
 	
 	private Tag(){
 	}
 
 	public Tag(String tag) {
 		this.tag = tag;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Tag other = (Tag) obj;
 		if (tag == null) {
 			if (other.tag != null)
 				return false;
 		} else if (!tag.equals(other.tag))
 			return false;
 		return true;
 	}
 
 	public String fooBar() {
 		return tag;
 	}
 
 	@Override
 	public String toString() {
 		return "Tag [tag=" + tag + "]";
 	}
 
 	public List<QuickThought> thought() {
 		return quickThoughts;
 	}
 
 	public void add(QuickThought quickThought) {
 		quickThoughts.add(quickThought);
 	}
 }
