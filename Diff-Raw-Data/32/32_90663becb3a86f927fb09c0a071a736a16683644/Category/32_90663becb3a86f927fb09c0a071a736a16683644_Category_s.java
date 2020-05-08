 package com.book.identification.model;
 
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlIDREF;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.hibernate.annotations.IndexColumn;
 import org.hibernate.search.annotations.Indexed;
 
@Indexed
 @Entity
 @XmlRootElement(name = "category")
 public class Category extends EntityBase {
 
 	@IndexColumn(name = "categoryNameIndex")
 	@JoinColumn(unique = true, nullable = false)
 	private String category;
 
 	@OneToMany(fetch = FetchType.LAZY)
 	private Set<Category> subCategories;
 
 	@ManyToOne(optional = true, cascade = CascadeType.ALL)
 	@JoinColumn(nullable = true)
 	private Category parent;
 
 	@XmlElement(name="category_name")
 	public String getCategory() {
 		return category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 	
 	@XmlElement(name="sub_categories")
 	public Set<Category> getSubCategories() {
 		return subCategories;
 	}
 
 	public void setSubCategories(Set<Category> subCategories) {
 		this.subCategories = subCategories;
 	}
 
 	@XmlIDREF
 	@XmlElement(name = "parent_id")
 	public Category getParent() {
 		return parent;
 	}
 
 	public void setParent(Category parent) {
 		this.parent = parent;
 	}
 
 }
