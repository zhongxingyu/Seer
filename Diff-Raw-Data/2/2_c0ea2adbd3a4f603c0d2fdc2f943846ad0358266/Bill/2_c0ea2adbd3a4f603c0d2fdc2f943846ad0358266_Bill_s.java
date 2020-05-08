 package com.marbl.administration.domain;
 
 //<editor-fold defaultstate="collapsed" desc="Imports">
 import java.io.Serializable;
 import java.util.Date;
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlRootElement;
 //</editor-fold>
 
 @Entity
 @XmlRootElement
 public class Bill implements Serializable {
 
     //<editor-fold defaultstate="collapsed" desc="Fields">
     @Id
     @GeneratedValue(strategy = GenerationType.TABLE)
     private Long id;
     private Integer driverBSN;
     private String carTrackerId;
     private Double paymentAmount;
     private Integer paymentMonth;
     private Integer paymentYear;
     @Temporal(javax.persistence.TemporalType.DATE)
     private Date paymentDate;
     private PaymentStatus paymentStatus;
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
     public Long getID() {
         return id;
     }
 
     public void setID(Long ID) {
         this.id = ID;
     }
 
     public Integer getDriverBSN() {
         return driverBSN;
     }
 
     public void setDriverBSN(Integer driverBSN) {
         this.driverBSN = driverBSN;
     }
 
     public String getCarTrackerId() {
         return carTrackerId;
     }
 
     public void setCarTrackerId(String carTrackerId) {
         this.carTrackerId = carTrackerId;
     }
 
     public Double getPaymentAmount() {
         return paymentAmount;
     }
 
     public void setPaymentAmount(Double paymentAmount) {
         this.paymentAmount = paymentAmount;
     }
 
     public Integer getPaymentMonth() {
         return paymentMonth;
     }
 
     public void setPaymentMonth(Integer paymentMonth) {
         this.paymentMonth = paymentMonth;
     }
 
     public Integer getPaymentYear() {
         return paymentYear;
     }
 
     public void setPaymentYear(Integer paymentYear) {
         this.paymentYear = paymentYear;
     }
 
     public Date getPaymentDate() {
         return paymentDate;
     }
 
     public void setPaymentDate(Date paymentDate) {
         this.paymentDate = paymentDate;
     }
 
     public PaymentStatus getPaymentStatus() {
         return paymentStatus;
     }
 
     public void setPaymentStatus(PaymentStatus paymentStatus) {
         this.paymentStatus = paymentStatus;
     }
     
    public Boolean getIsPayed() {
         return paymentStatus != PaymentStatus.OPEN;
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Constructors">
     public Bill() {
         this(0, "", 0D, 0, 0);
     }
 
     public Bill(Integer driverBSN, String carTrackerId, Double paymentAmount,
             Integer paymentMonth, Integer paymentYear) {
 
         this.driverBSN = driverBSN;
         this.carTrackerId = carTrackerId;
         this.paymentAmount = paymentAmount;
         this.paymentMonth = paymentMonth;
         this.paymentYear = paymentYear;
 
         paymentStatus = PaymentStatus.OPEN;
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Methods">
     @Override
     public int hashCode() {
         if (id != null) {
             return id.hashCode();
         } else {
             return 0;
         }
     }
 
     @Override
     public boolean equals(Object obj) {
         return obj instanceof Bill && hashCode() == obj.hashCode();
     }
 
     @Override
     public String toString() {
         return "Bill{" + "id=" + id + '}';
     }
     //</editor-fold>
 }
