 package com.supinfo.supcommerce.model;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 
 import javax.persistence.Basic;
import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.hibernate.validator.constraints.NotBlank;
 import org.hibernate.validator.constraints.NotEmpty;
 
 @Entity
 public class Product implements Serializable {
 	
 	@Id @GeneratedValue
 	private Long id;
 	
	@Column(unique=true) 
 	@NotEmpty @NotBlank
 	private String name;
 	
 	@Lob @NotEmpty @NotBlank
 	private String description;
 	
 	@NotNull @Min(0)
 	private BigDecimal price;
 	
 	@ManyToOne @JoinColumn
 	private Category category;
 	
 	@Lob @Size(max=500 * 1000)
 	@Basic(fetch=FetchType.LAZY)
 	private byte[] picture;
 	
 	
 	public Product() { }
 
 	public Product(String name, String description,
 			BigDecimal price, Category category, byte[] picture) {
 		this.name = name;
 		this.description = description;
 		this.price = price;
 		this.category = category;
 		this.picture = picture;
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
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public BigDecimal getPrice() {
 		return price;
 	}
 
 	public void setPrice(BigDecimal price) {
 		this.price = price;
 	}
 
 	public Category getCategory() {
 		return category;
 	}
 
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 	
 	public byte[] getPicture() {
 		return picture;
 	}
 	
 	public void setPicture(byte[] picture) {
 		this.picture = picture;
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
