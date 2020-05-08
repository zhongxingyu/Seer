 package com.supinfo.supcommerce.model;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.hibernate.validator.constraints.NotBlank;
 import org.hibernate.validator.constraints.NotEmpty;
 
 @Entity
 public class Category implements Serializable {
 	
 	@Id @GeneratedValue
 	private Long id;
 	
 	@NotEmpty @NotBlank
 	private String name;
 	
 	@OneToMany(mappedBy="category")
 	private List<Product> products;
 
 	
 	public Category() { }
 
 	public Category(String name) {
 		this.name = name;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public List<Product> getProducts() {
 		return products;
 	}
 	
 	public void setProducts(List<Product> products) {
 		this.products = products;
 	}
 
 	@Override
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return EqualsBuilder.reflectionEquals(this, obj);
 	}
 
 }
