 
 package com.AlphaDevs.Web.Entities;
 
 import com.AlphaDevs.Web.Enums.BillStatus;
 import com.AlphaDevs.Web.Enums.GRNFlags;
 import com.AlphaDevs.Web.Enums.PurchaseType;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.*;
 
 /**
  *
  * @author Mihindu Gajaba Karunarathne 
  * 
  * Alpha Development Team ( Pvt ) Ltd
  * www.AlphaDevs.com
  * Info@AlphaDevs.com
  * 
  */
 
 @Entity
 public class GRN implements Serializable 
 {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE)
     private Long id;
     
     private String grnNo;
     
     @Temporal(javax.persistence.TemporalType.DATE)
     private Date grnDate;
     
     
     @OneToOne
     private Supplier supplier;
     
     private String invNo;
     
     @Enumerated(EnumType.ORDINAL)
     private PurchaseType purchaseType;
     @Enumerated(EnumType.ORDINAL)
     private BillStatus billStatus;
     
     private double CreditPeriod;
     private double TotalAmount;
     
     private double settledAmount;
     
     @Enumerated(EnumType.ORDINAL)
     private GRNFlags grnFlags;
     
     @OneToOne
     private Location location;
     
     
     @OneToMany(mappedBy = "relatedGRN",fetch= FetchType.EAGER,cascade= CascadeType.ALL)
     private List<GRNDetails> gRNDetailss;
     
     @OneToOne
     private Logger logger;
     
     @OneToOne(mappedBy = "relatedGrn",cascade= CascadeType.ALL)
     private GRNPaymentDetails gRNPaymentDetails;
     
     
     
    @OneToMany(mappedBy = "relatedGRNs")
     private List<Properties> extraz;
     
     public GRN(String grnNo, Date grnDate, Supplier supplier, String invNo, PurchaseType purchaseType, double CreditPeriod, double TotalAmount, double settledAmount, GRNFlags grnFlags, Location location, BillStatus billStatus, List<GRNDetails> gRNDetailss, Logger logger, GRNPaymentDetails gRNPaymentDetails) {
         this.grnNo = grnNo;
         this.grnDate = grnDate;
         this.supplier = supplier;
         this.invNo = invNo;
         this.purchaseType = purchaseType;
         this.CreditPeriod = CreditPeriod;
         this.TotalAmount = TotalAmount;
         this.settledAmount = settledAmount;
         this.grnFlags = grnFlags;
         this.location = location;
         this.billStatus = billStatus;
         this.gRNDetailss = gRNDetailss;
         this.logger = logger;
         this.gRNPaymentDetails = gRNPaymentDetails;
     }
 
     public GRN(String grnNo, Date grnDate, Supplier supplier, String invNo, PurchaseType purchaseType, BillStatus billStatus, double CreditPeriod, double TotalAmount, double settledAmount, GRNFlags grnFlags, Location location, List<GRNDetails> gRNDetailss, Logger logger, GRNPaymentDetails gRNPaymentDetails, List<Properties> extraz) {
         this.grnNo = grnNo;
         this.grnDate = grnDate;
         this.supplier = supplier;
         this.invNo = invNo;
         this.purchaseType = purchaseType;
         this.billStatus = billStatus;
         this.CreditPeriod = CreditPeriod;
         this.TotalAmount = TotalAmount;
         this.settledAmount = settledAmount;
         this.grnFlags = grnFlags;
         this.location = location;
         this.gRNDetailss = gRNDetailss;
         this.logger = logger;
         this.gRNPaymentDetails = gRNPaymentDetails;
         this.extraz = extraz;
     }
     
     
     public List<Properties> getExtraz() {
         return extraz;
     }
 
     public void setExtraz(List<Properties> extraz) {
         this.extraz = extraz;
     }
     
     public GRNPaymentDetails getgRNPaymentDetails() {
         return gRNPaymentDetails;
     }
 
     public void setgRNPaymentDetails(GRNPaymentDetails gRNPaymentDetails) {
         this.gRNPaymentDetails = gRNPaymentDetails;
     }
 
     public GRNFlags getGrnFlags() {
         return grnFlags;
     }
 
     public void setGrnFlags(GRNFlags grnFlags) {
         this.grnFlags = grnFlags;
     }
 
     public double getSettledAmount() {
         return settledAmount;
     }
 
     public void setSettledAmount(double settledAmount) {
         this.settledAmount = settledAmount;
     }
 
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     
     public Logger getLogger() {
         return logger;
     }
 
     public void setLogger(Logger logger) {
         this.logger = logger;
     }
 
    
 
     public GRN() {
     }
 
     public List<GRNDetails> getgRNDetailss() {
         return gRNDetailss;
     }
 
     public void setgRNDetailss(List<GRNDetails> gRNDetailss) {
         this.gRNDetailss = gRNDetailss;
     }
 
     
     public double getCreditPeriod() {
         return CreditPeriod;
     }
 
     public void setCreditPeriod(double CreditPeriod) {
         this.CreditPeriod = CreditPeriod;
     }
 
     public double getTotalAmount() {
         return TotalAmount;
     }
 
     public void setTotalAmount(double TotalAmount) {
         this.TotalAmount = TotalAmount;
     }
 
     public BillStatus getBillStatus() {
         return billStatus;
     }
 
     public void setBillStatus(BillStatus billStatus) {
         this.billStatus = billStatus;
     }
 
     public Date getGrnDate() {
         return grnDate;
     }
 
     public void setGrnDate(Date grnDate) {
         this.grnDate = grnDate;
     }
 
     public String getGrnNo() {
         return grnNo;
     }
 
     public void setGrnNo(String grnNo) {
         this.grnNo = grnNo;
     }
 
     public String getInvNo() {
         return invNo;
     }
 
     public void setInvNo(String invNo) {
         this.invNo = invNo;
     }
 
     public Location getLocation() {
         return location;
     }
 
     public void setLocation(Location location) {
         this.location = location;
     }
 
     public PurchaseType getPurchaseType() {
         return purchaseType;
     }
 
     public void setPurchaseType(PurchaseType purchaseType) {
         this.purchaseType = purchaseType;
     }
 
     public Supplier getSupplier() {
         return supplier;
     }
 
     public void setSupplier(Supplier supplier) {
         this.supplier = supplier;
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
         if (!(object instanceof GRN)) {
             return false;
         }
         GRN other = (GRN) object;
         if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return getGrnNo();
     }
     
 }
