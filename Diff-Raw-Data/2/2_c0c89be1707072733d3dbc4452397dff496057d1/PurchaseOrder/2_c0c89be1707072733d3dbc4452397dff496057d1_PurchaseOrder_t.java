 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mne.advertmanager.model;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Set;
 import javax.persistence.*;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @author Nina Eidelshtein and Misha Lebedev
  */
 @Entity
 @Table(name = "purchase_order")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "PurchaseOrder.findAll", query = "SELECT p FROM PurchaseOrder p"),
     @NamedQuery(name = "PurchaseOrder.findById", query = "SELECT p FROM PurchaseOrder p WHERE p.id = ?"),
     @NamedQuery(name = "PurchaseOrder.findByIdList", query = "SELECT p FROM PurchaseOrder p WHERE p.id in (:poIdList)"),    
     @NamedQuery(name = "PurchaseOrder.findByAffProgramId", query = "SELECT p FROM PurchaseOrder p WHERE p.affProgram.id = ?"),
     @NamedQuery(name = "PurchaseOrder.findByStatus", query = "SELECT p FROM PurchaseOrder p WHERE p.status = ?"),
    @NamedQuery(name = "PurchaseOrder.findByOrdertime", query = "SELECT p FROM PurchaseOrder p WHERE p.ordertime = ?")
 })
 
 
 
 public class PurchaseOrder implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     public static final String PO_STAT_QUERY =
             "select  poId, aclCount ,access_log.id as aclId " +
             "from   access_log inner join ( select " +
             "count(*) as aclCount,access_log.ip_address as ip , purchase_order.id as poId " +
             "from access_log inner join purchase_order on purchase_order.ip_address = access_log.ip_address " +
             "where purchase_order.affprogram_id = ? group by  access_log.ip_address) subQ " +
             "on access_log.ip_address = subQ.ip";
     
     public static final String PO_AMOUNT_BY_DATE_QUERY = 
             "select DATE_FORMAT(ordertime,'%Y-%m-%d') as purchaseDay ,count(*) as poAmount from  purchase_order " +
             "where  affprogram_id=? group by purchaseDay order by purchaseDay";
     
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id")
     private Integer id;
     
     @Size(min = 0, max = 256)
     @Column(name = "status")
     private String status;
 
     @Size(min = 0, max = 256)
     @Column(name = "tracking_id")
     private String trackingID;
     
     @Size(min = 0, max = 256)
     @Column(name = "country")    
     private String country;
 
     @Size(min =0, max = 256)
     @Column(name = "city") 
     private String city;
     
     
     @Column(name = "po_sum") 
     private float po_sum;
     @Column(name = "commision")
     private float commision;
     
     @Size(min = 0, max = 256)
     @Column(name = "ip_address")
     private String ipAddress;    
     
     @Size(max = 256)
     @Column(name = "cn")
     private String countryName;
     
     @Size(max = 2)
     @Column(name = "cc")
     private String countryCode;
     
     
     @Size(min = 0, max = 256)
     @Column(name = "original_order_id")
     private String originalOrderID;
     
 
     @Size(min = 0, max = 16)
     @Column(name = "currency")
     private String currency;    
     
 
     @Column(name = "access_amount")
     private int accessAmount;        
     
     
     @Column(name = "ordertime")
     @Temporal(TemporalType.TIMESTAMP)
     private Date ordertime;
     
     @JoinColumn(name = "affprogram_id", referencedColumnName = "id")
     @ManyToOne(optional = false)
     private AffProgram affProgram;
     
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "po")
     private Set<AccessLog> aclSet;
     
     
     @JoinColumn(name = "partner_id", referencedColumnName = "id")
     @ManyToOne(optional = true)
     private Partner partner;
 
     public PurchaseOrder() {
     }
 
     public PurchaseOrder(Integer id) {
         this.id = id;
     }
 
     public PurchaseOrder(Integer id, String status, Date ordertime) {
         this.id = id;
         this.status = status;
         this.ordertime = ordertime;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getStatus() {
         return status;
     }
 
     public void setStatus(String status) {
         this.status = status;
     }
 
     public Date getOrdertime() {
         return ordertime;
     }
 
     public void setOrdertime(Date ordertime) {
         this.ordertime = ordertime;
     }
 
     public void setAclSet(Set<AccessLog> aclSet) {
         this.aclSet = aclSet;
     }
 
     public AffProgram getAffProgram() {
         return affProgram;
     }
 
     public void setAffProgram(AffProgram affProgram) {
         this.affProgram = affProgram;
     }
 
     public void setOriginalOrderId(String itemValue) {
         originalOrderID = itemValue;
     }
 
     public void setOriginalOrderStatus(String itemValue) {
         status = itemValue;;
     }
 
     public void setTrackingID(String itemValue) {
         trackingID = itemValue;
     }
 
     public void setCountry(String itemValue) {
         country = itemValue;
     }
 
     public void setCity(String itemValue) {
         city = itemValue;
     }
 
     public void setPOSum(float itemValue) {
         po_sum = itemValue;
     }
 
     public void setCommision(float itemValue) {
        commision = itemValue;
     }
 
     public void setPartner(Partner partner) {
         this.partner = partner;
     }
 
     public void setIPAddress(String itemValue) {
         ipAddress = itemValue;
     }
 
     public void setCurrency(String currency) {
         this.currency = currency;
     }
 
 
 
     public String getCity() {
         return city;
     }
 
     public float getCommision() {
         return commision;
     }
 
     public String getCountry() {
         return country;
     }
 
     public String getCurrency() {
         return currency;
     }
 
     public String getIpAddress() {
         return ipAddress;
     }
 
     public String getOriginalOrderID() {
         return originalOrderID;
     }
 
     public Partner getPartner() {
         return partner;
     }
 
     public float getSum() {
         return po_sum;
     }
 
     public String getTrackingID() {
         return trackingID;
     }
 
     public String getCountryCode() {
         return countryCode;
     }
 
     public void setCountryCode(String countryCode) {
         this.countryCode = countryCode;
     }
 
     public String getCountryName() {
         return countryName;
     }
 
     public void setCountryName(String countryName) {
         this.countryName = countryName;
     }
 
     public int getAccessAmount() {
         return accessAmount;
     }
 
     public void setAccessAmount(int accessAmount) {
         this.accessAmount = accessAmount;
     }
 
     
     
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (id != null ? id.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof PurchaseOrder)) {
             return false;
         }
         PurchaseOrder other = (PurchaseOrder) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         
       
         return true;
     }
 
     @Override
     public String toString() {
         return "com.mne.advertmanager.model.PurchaseOrder[ id=" + id + " ]";
     }
 }
