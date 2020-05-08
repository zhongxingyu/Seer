 package com.xerox.amazonws.fps;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 
 public class Amount implements Comparable, Serializable {
     private BigDecimal amount;
     private final String currencyCode;
 
     public static final String USD = "USD";
     public static final Amount ZERO_USD = new Amount(BigDecimal.ZERO, USD);
 
     public Amount(BigDecimal amount, String currencyCode) {
         this.amount = amount;
         this.currencyCode = currencyCode;
     }
 
     public Amount(double amount, String currencyCode) {
        this.amount = new BigDecimal(Double.toString(amount));
         this.currencyCode = currencyCode;
     }
 
     public BigDecimal getAmount() {
         return amount;
     }
 
     public String getCurrencyCode() {
         return currencyCode;
     }
 
     public Amount add(int amount, String currencyCode) {
         return add(new BigDecimal(amount), currencyCode);
     }
 
     public Amount add(BigDecimal amount, String currencyCode) {
         if (!this.currencyCode.equals(currencyCode))
             throw new IllegalArgumentException("Can't add some " + currencyCode + " to some " + this.currencyCode);
         this.amount = this.amount.add(amount);
         return this;
     }
 
     public int compareTo(Object o) {
         if (o == null)
             return -1;
         if (!(o instanceof Amount))
             return -1;
         Amount other = (Amount) o;
         if (!getCurrencyCode().equals(other.getCurrencyCode()))
             return getCurrencyCode().compareTo(other.getCurrencyCode());
         else
             return getAmount().compareTo(other.getAmount());
     }
 
     public static Amount parseAmount(String value) {
         String[] strings = value.split(" ");
         return new Amount(new BigDecimal(strings[0]), strings[1]);
     }
 
     @Override
     public String toString() {
         return "Amount{" +
                 "amount=" + amount +
                 ", currencyCode='" + currencyCode + '\'' +
                 '}';
     }
 }
