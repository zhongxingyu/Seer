 package eu.comexis.napoleon.shared.model;
 
 import com.googlecode.objectify.annotation.Subclass;
 
@Subclass(unindexed=true)
 public class PaymentTenant extends Payment {
 
   private Boolean paymentInCash;
   private String number;
   private String communication;
   public PaymentTenant() {
     super();
   }
   public String getCommunication() {
     return communication;
   }
   public String getNumber() {
     return number;
   }
   public Boolean getPaymentInCash() {
     return paymentInCash;
   }
   public void setCommunication(String communication) {
     this.communication = communication;
   }
   public void setNumber(String number) {
     this.number = number;
   }
   public void setPaymentInCash(Boolean paymentInCash) {
     this.paymentInCash = paymentInCash;
   }
 
 }
