 /**
 * My Net is free software, you can redistribute it and/or modify
 * it under the terms of GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * You should have received a copy of the the GNU Affero
 * General Public License, along with My Net. If not, see
 *
 * http://www.gnu.org/licenses/agpl.txt
 */
 
 package com.perevillega.mynet.model;
 
 import java.io.Serializable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.GeneratedValue;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 import javax.persistence.UniqueConstraint;
 import org.hibernate.validator.Length;
 import org.hibernate.validator.NotNull;
 
 @Entity
 @NamedQueries({	
 	@NamedQuery(
 		    name="findAllCategories",
 		    query="SELECT OBJECT(category) FROM Category category"
 		    	),
 	@NamedQuery(
 		    name="findCategoryByName",
 		    query="SELECT OBJECT(category) FROM Category category WHERE category.name = :name"
		    	),
	})
 @Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = "name"),
 })
 public class Category implements Serializable
 {
     private Long id;
     private Integer version;    
     private String name;
     private List<Link> links = new ArrayList<Link>();
     private Category parent;
     private List<Category> children = new ArrayList<Category>();
     private boolean selected;
     private int numLinks;
     private int numChildren;
     private String oldName;
     
 
     @Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		result = prime * result + ((version == null) ? 0 : version.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!(obj instanceof Category)) {
 			return false;
 		}
 		Category other = (Category) obj;
 		if (id == null) {
 			if (other.id != null) {
 				return false;
 			}
 		} else if (!id.equals(other.id)) {
 			return false;
 		}
 		if (version == null) {
 			if (other.version != null) {
 				return false;
 			}
 		} else if (!version.equals(other.version)) {
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public String toString() {	
 		return this.name;
 	}
 
 	@Id @GeneratedValue
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Version
     public Integer getVersion() {
         return version;
     }
 
     private void setVersion(Integer version) {
         this.version = version;
     }
 
     @Column(name = "name", nullable = false)
     @Length(max = 40)    
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @OneToMany(mappedBy="category", fetch=FetchType.LAZY)
 	public List<Link> getLinks() {
 		return links;
 	}
 
 	public void setLinks(List<Link> links) {
 		this.links = links;
 	}
 
 
 	@ManyToOne
 	public Category getParent() {
 		return parent;
 	}
 
 	public void setParent(Category parent) {
 		if(!this.equals(parent)){
 			this.parent = parent;
 		}
 	}
 
 	@OneToMany(mappedBy="parent", fetch=FetchType.LAZY)
 	public List<Category> getChildren() {
 		return children;
 	}
 
 	public void setChildren(List<Category> children) {
 		this.children = children;
 	}
 
 	@Transient
 	public boolean isSelected() {
 		return selected;
 	}
 
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 		
 	@NotNull
 	public int getNumLinks() {
 		return numLinks;
 	}
 
 	public void setNumLinks(int numLinks) {
 		this.numLinks = numLinks;
 	}
 
 	@NotNull
 	public int getNumChildren() {
 		return numChildren;
 	}
 
 	public void setNumChildren(int numChildren) {
 		this.numChildren = numChildren;
 	}
 
 }
