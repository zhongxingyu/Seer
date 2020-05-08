 package com.megalogika.sv.model;
 
 import java.io.Serializable;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.Transient;
 
 import org.apache.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
 
 @Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class Confirmation extends TimedEvent implements Serializable {
 	private static final long serialVersionUID = -7428117547697707665L;
 	
 	@Transient
 	protected transient Logger logger = Logger.getLogger(Confirmation.class);
 	
 	private Product product;
 	private String enteredByIp;
 	
 	public Confirmation() {
 		super();
 	}
 	
 	public Confirmation(User user) {
 		this();
 		setActor(user);
 	}	
 	
 	public Confirmation(Product p, User user) {
 		this(user);
 		setProduct(p);
 		if (user != null) setEnteredByIp(user.getUserAddres());
 	}
 
 	@Override
 	public String toString() {
 		return "CONFIRMATION[" + product + " by " + getActor()+"]";
 	}
 	
 	public void setProduct(Product product) {
 		this.product = product;
 	}
 
 	@ManyToOne(cascade={CascadeType.MERGE})
 	public Product getProduct() {
 		return product;
 	}
 	
 	public String getEnteredByIp() {
 		return enteredByIp;
 	}
 
 	public void setEnteredByIp(String enteredByIp) {
 		this.enteredByIp = enteredByIp;
 	}	
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = ((getActor() == null) ? 0 : getActor().hashCode());
 		result = prime * result + ((product == null) ? 0 : product.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (getClass() != obj.getClass())
 			return false;
 		Confirmation other = (Confirmation) obj;
 		if (getActor() == null) {
 			if (other.getActor() != null)
 				return false;
 		} else if (getActor().getId() != other.getActor().getId())
 			return false;		
 		if (product == null) {
 			if (other.product != null)
 				return false;
 		} else if (!product.equals(other.product))
 			return false;
 		logger.debug("EQUAL!");
 		return true;
 	}	
 	
 }
