 package ee.ttu.ecomm.core.domain;
 
import javax.persistence.*;
 import javax.xml.bind.annotation.XmlTransient;
 import java.math.BigDecimal;
 import java.util.Date;
 
 @Entity
 @Table(name = "CONTRACT")
 public class Contract implements Logged {
 
     Long id;
     String contractNumber;
     String name;
     String description;
     Date validFrom;
     Date validTo;
     String note;
     BigDecimal valueAmount;
     Customer customer;
     Address address;
     Date created;
     Date updated;
 
     @Id
     @Column(name = "contract")
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Column(name = "cnt_number")
     public String getContractNumber() {
         return contractNumber;
     }
 
     public void setContractNumber(String contractNumber) {
         this.contractNumber = contractNumber;
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
 
     public Date getValidFrom() {
         return validFrom;
     }
 
     public void setValidFrom(Date validFrom) {
         this.validFrom = validFrom;
     }
 
     public Date getValidTo() {
         return validTo;
     }
 
     public void setValidTo(Date validTo) {
         this.validTo = validTo;
     }
 
     public String getNote() {
         return note;
     }
 
     public void setNote(String note) {
         this.note = note;
     }
 
     public BigDecimal getValueAmount() {
         return valueAmount;
     }
 
     public void setValueAmount(BigDecimal valueAmount) {
         this.valueAmount = valueAmount;
     }
 
    @ManyToOne
    @JoinColumn(name="customer")
     public Customer getCustomer() {
         return customer;
     }
 
     public void setCustomer(Customer customer) {
         this.customer = customer;
     }
 
    @ManyToOne
    @JoinColumn(name="cst_address")
     public Address getAddress() {
         return address;
     }
 
     public void setAddress(Address address) {
         this.address = address;
     }
 
     @Override
     public Date getCreated() {
         return created;
     }
 
     @Override
     public void setCreated(Date created) {
         this.created = created;
     }
 
     @Override
     public Date getUpdated() {
         return updated;
     }
 
     @Override
     public void setUpdated(Date updated) {
         this.updated = updated;
     }
 }
