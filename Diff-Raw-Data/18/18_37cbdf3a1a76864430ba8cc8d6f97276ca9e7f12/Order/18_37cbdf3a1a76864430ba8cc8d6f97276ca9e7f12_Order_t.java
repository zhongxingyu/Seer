 package com.gmail.at.zhuikov.aleksandr.root.domain;
 
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedHashSet;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.validation.GroupSequence;
 import javax.validation.Valid;
 import javax.validation.constraints.Pattern;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.Length;
 import org.hibernate.validator.constraints.NotBlank;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.core.style.ToStringCreator;
 
 
 @Entity
 @Table(name = "T_ORDER")
 @GroupSequence({ Order.class, ComplexValidation.class })
 public class Order {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	protected Long id;
 	
 	protected String customer;
 	
 	protected String email;
 	
 	protected Date date = new Date();
 	
 	@OneToMany(cascade = CascadeType.ALL)
 	@JoinColumn(name = "ORDER_ID")
 	private Collection<Item> items = new LinkedHashSet<Item>();
 
 	protected Order() {
 	}
 
 	public Order(String customer) {
 		this.customer = customer;
 	}
 	
 	@NotBlank
 	@Length(min = 5, max = 30)
	@Pattern(regexp = "[a-zA-Z\\s]*", message = "Only characters allowed")
 	public String getCustomer() {
 		return customer;
 	}
 
 	@Valid
 	@UniqueProductInOrder(groups = ComplexValidation.class)
 	public Collection<Item> getItems() {
 		return items;
 	}
 
 	public void setItems(Collection<Item> items) {
 		this.items = items;
 	}
 
 	public Long getId() {
 		return id;
 	}
 	
 	@NotEmpty
 	@Email
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 	
 	@Override
 	public String toString() {
 		return new ToStringCreator(this).append(customer).toString();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		
 		if (obj == null) {
 			return false;
 		}
 		
 		if (obj == this) {
 			return true;
 		}
 		
 		if (obj.getClass() != getClass()) {
 			return false;
 		}
 
 		Order other = (Order) obj;
 
 		return new EqualsBuilder()
 				.append(customer, other.customer)
 				.append(date, other.date)
 				.isEquals();
 	}
 	
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder()
 				.append(customer)
 				.append(date)
 				.toHashCode();
 	}
 }
 
 interface ComplexValidation {}
