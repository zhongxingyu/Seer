 package models.account;
 
import java.util.Date;
 import javax.persistence.CascadeType;
import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
 
 import models.general.Product;
 
 /**
  * @author: Gwenael Alizon <gwenael.alizon@oxiras.com>
  */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name="id", columnNames = {"naturalId", "account_id"})})
 public class AccountProductPeriod extends AccountModel {
 
     @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, optional = false)
     public Product product;
 
     public Integer availableSeats;
 
     public Date endDate;
 
 }
