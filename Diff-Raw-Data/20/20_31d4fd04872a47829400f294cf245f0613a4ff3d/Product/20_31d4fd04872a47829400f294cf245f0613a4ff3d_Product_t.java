 package com.eshop.domain;
 
 import java.math.BigDecimal;
 import java.sql.Blob;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="products")
 public class Product {
 	@Id
 	@GeneratedValue
 	@Column(name="id")
 	private Integer id;
 	
 	@Column(name="name")
 	private String name;
 	
 	@Column(name="price")
 	private BigDecimal price;
 	
 	@Column(name="mime")
 	private String mime;
 	
 	@Column(name="file")
 	@Lob
 	private Blob file;
 	
 	@Column(name="isdeleted")
	private boolean isdeleted;
 	
 	@ManyToOne(cascade=CascadeType.ALL)
 	@JoinColumn(name="categoryid")
 	private Category category;
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public BigDecimal getPrice() {
 		return price;
 	}
 
 	public void setPrice(BigDecimal price) {
 		this.price = price;
 	}
 
 	public String getMime() {
 		return mime;
 	}
 
 	public void setMime(String mime) {
 		this.mime = mime;
 	}
 
 	public Blob getFile() {
 		return file;
 	}
 
 	public void setFile(Blob file) {
 		this.file = file;
 	}
 
 	public boolean isDeleted() {
		return isdeleted;
 	}
 
 	public void setDeleted(boolean isDeleted) {
		this.isdeleted = isDeleted;
 	}
 
 	public Category getCategory() {
 		return category;
 	}
 
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 }
