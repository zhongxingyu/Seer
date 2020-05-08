 package models.account;
 
 import javax.persistence.CascadeType;
 import javax.persistence.ManyToOne;
import java.util.Date;
 
 import models.general.Product;
 
 /**
  * @author: Gwenael Alizon <gwenael.alizon@oxiras.com>
  */
 public class AccountProductPeriod extends AccountModel {
 
     @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, optional = false)
     public Product product;
 
     public Integer availableSeats;
 
     public Date endDate;
 
 }
