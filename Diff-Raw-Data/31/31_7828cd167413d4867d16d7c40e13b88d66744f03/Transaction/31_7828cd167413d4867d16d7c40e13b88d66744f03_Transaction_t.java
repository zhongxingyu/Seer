 package project.model;
 
 import java.math.BigDecimal;
 
 public final class Transaction {
     public static enum Type {
         DEPOSIT(true, true), INTEREST(true, false), LOAN_INTEREST(false, false), WITHDRAWAL(false, true), FEE(false, false);
 
         private final boolean isPositive;
 
         private final boolean canRepeat;
 
         private Type(boolean isPositive, boolean canRepeat) {
             this.isPositive = isPositive;
             this.canRepeat = canRepeat;
         }
 
         public boolean isPositive() {
             return isPositive;
         }
 
         public boolean canRepeat() {
             return canRepeat;
         }
 
         @Override
         public String toString() {
             return name().toLowerCase().replace('_', ' ');
         }
     }
 
     public static enum FraudStatus {
         NOT_FLAGGED {
             @Override
             public String toString() {
                 return "";
             }
         }, FLAGGED, REVERSED;
 
         @Override
         public String toString() {
             return name().toLowerCase().replace('_', ' ');
         }
     }
 
     private final Type type;
 
     private final BigDecimal amount;
 
     /**
      * The account balance after this transaction was completed.
      */
     private final BigDecimal balance;
 
     private final int timestamp;
 
     private FraudStatus fraudStatus;
 
     public Transaction(Type type, BigDecimal amount, BigDecimal balance) {
         this.type = type;
         this.amount = amount;
         this.balance = balance;
         this.timestamp = Bank.getInstance().getCurrentMonth();
         this.fraudStatus = FraudStatus.NOT_FLAGGED;
     }
 
     public Type getType() {
         return type;
     }
 
     public BigDecimal getAmount() {
         return amount;
     }
 
     public BigDecimal getBalance() {
         return balance;
     }
 
     public int getTimestamp() {
         return timestamp;
     }
 
     public FraudStatus getFraudStatus() {
         return fraudStatus;
     }
 
     public void setFraudStatus(FraudStatus fraudStatus) {
         this.fraudStatus = fraudStatus;
     }
 
     @Override
     public String toString() {
        BigDecimal displayAmount = type.isPositive() ? amount : amount.negate();
        return String.format("%-16s $%14.2f $%14.2f %5d    %-8s", type, displayAmount, balance, timestamp, fraudStatus);
     }
 }
