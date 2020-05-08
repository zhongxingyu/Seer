 package com.thoughtworks.twu.model;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 import javax.persistence.*;
 import javax.validation.constraints.DecimalMax;
 import javax.validation.constraints.NotNull;
 import java.math.BigDecimal;
 
 @Entity
 @Table(name = "items")
 public class Item {
 
 	@Id
 	@GeneratedValue
 	private Long id;
 
 	@NotEmpty(message = "Please enter Item Name")
 	private String name;
 
 	@DecimalMax("99999")
     @NotNull
 	private BigDecimal price;
 
 	@NotNull
 	private String description;
 
    @NotNull
     @NotEmpty(message = "Please select Item Type")
     private String type;
 
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
 	public BigDecimal getPrice() {
 		return price;
 	}
 	public void setPrice(BigDecimal price) {
 		this.price = price;
 	}
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
     public String getType() {
         return type;
     }
     public void setType(String type) {
         this.type = type;
     }
 	
 }
