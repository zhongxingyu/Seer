 package kabbadi.domain;
 


 import lombok.Getter;
 import lombok.Setter;
 
 import javax.persistence.*;
 import java.util.Date;
 
 @Entity
 @Table(name="Asset")
 @Getter
 @Setter
 public class Asset {
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Integer id;
 
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
     @JoinColumn(name="INVOICE_NUMBER")
     private Invoice invoice;
 }
