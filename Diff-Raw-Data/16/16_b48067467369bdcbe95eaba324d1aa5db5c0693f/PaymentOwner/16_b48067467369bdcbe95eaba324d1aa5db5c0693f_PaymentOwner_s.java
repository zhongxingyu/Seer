 package eu.comexis.napoleon.shared.model;
 
 import com.googlecode.objectify.annotation.Subclass;
 
@Subclass(unindexed=true)
 public class PaymentOwner extends Payment {
   private Float balance;
   private Float previousbalance;
   private Float rentWithoutFee;
   private Float fee;
   private FeeUnit feeUnit;
   private Long nbrPeriod;
   
 
   public Float getPreviousbalance() {
     return previousbalance;
   }
 
   public void setPreviousbalance(Float previousbalance) {
     this.previousbalance = previousbalance;
   }
 
   public Float getRentWithoutFee() {
     return rentWithoutFee;
   }
 
   public void setRentWithoutFee(Float rentWithoutFee) {
     this.rentWithoutFee = rentWithoutFee;
   }
 
   public Float getFee() {
     return fee;
   }
 
   public void setFee(Float fee) {
     this.fee = fee;
   }
 
   public FeeUnit getFeeUnit() {
     return feeUnit;
   }
 
   public void setFeeUnit(FeeUnit feeUnit) {
     this.feeUnit = feeUnit;
   }
 
   public Long getNbrPeriod() {
     return nbrPeriod;
   }
 
   public void setNbrPeriod(Long nbrPeriod) {
     this.nbrPeriod = nbrPeriod;
   }
 
   public PaymentOwner() {
     super();
   }
 
   public Float getBalance() {
     return balance;
   }
 
   public void setBalance(Float balance) {
     this.balance = balance;
   }
 }
