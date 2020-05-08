 package kabbadi.domain;
 
 import lombok.Getter;
 import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
 
 import javax.persistence.*;
 import java.util.Date;
 
 @Entity
 @Table(name="Asset")
 @Getter
 @Setter
 public class Asset {
 
     final static public String ASSET_NUMBER = "assetNumber";
     @Id
    @GenericGenerator(name="auto-increment", strategy = "increment")
    @GeneratedValue(generator = "auto-increment")
     private Integer asset_id;
 
     private String assetNumber;
     private String modelName;
     private String registeredOffice;
     private String serialNumber;
     private String registeredTo;
     private String status;
     private Boolean isLeased;
     private Date leasingExpiration;
     private Date maintenanceExpiration;
     private String leasingCompany;
 
     @ManyToOne
     @JoinColumn(name="invoice_id")
     private Invoice invoice;
 }
