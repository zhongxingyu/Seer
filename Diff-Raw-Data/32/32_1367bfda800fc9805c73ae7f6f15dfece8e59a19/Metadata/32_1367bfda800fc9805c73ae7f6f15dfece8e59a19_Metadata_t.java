 package com.ideazworld.amber.dao.entity.core;
 
import java.util.List;

 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
 
 import com.amber.ideazworld.commons.model.MetadataType;
 import com.ideazworld.amber.dao.entity.AbstractPkEntity;
 
 /**
  * The persistent class for the meta_data database table.
  * 
  */
 @Entity
 public class Metadata extends AbstractPkEntity {
 	private static final long serialVersionUID = 1L;
 
 	private String name;
 
 	private String description;
 
 	@Enumerated(EnumType.STRING)
 	private MetadataType type;
	
	// bi-directional many-to-one association to Metadata
	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Metadata parent;
	
	@OneToMany(mappedBy="parent")
	private List<Metadata> children;
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public MetadataType getType() {
 		return type;
 	}
 
 	public void setType(MetadataType type) {
 		this.type = type;
 	}

	public Metadata getParent() {
		return parent;
	}

	public void setParent(Metadata parent) {
		this.parent = parent;
	}

	public List<Metadata> getChildren() {
		return children;
	}

	public void setChildren(List<Metadata> children) {
		this.children = children;
	}
 }
