 package com.sgcib.currencyconverter.converter;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 
 public class Money {
     @JsonProperty public double quantity;
     @JsonProperty public String currency;
 
     public Money() {
     }
 
     public Money( double quantity, String currency) {
         this.quantity = quantity;
         this.currency = currency;
     }
 
     @Override
     public String toString() {
         return "Money{" +
             "quantity=" + quantity +
             ", currency='" + currency + '\'' +
             '}';
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Money money = (Money) o;
        if (Math.abs(money.quantity - quantity) > 0.00001 ) return false;
         if (currency != null ? !currency.equals(money.currency) : money.currency != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result;
         long temp;
         temp = Double.doubleToLongBits(quantity);
         result = (int) (temp ^ (temp >>> 32));
         result = 31 * result + (currency != null ? currency.hashCode() : 0);
         return result;
     }
 }
