 package net.m_kawato.tabletpos;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 
 public class Transaction {
     private Context context;
     public long transactionId;
     public String routeCode;
     public String placeCode;
     public BigDecimal creditAmount;
     public List<OrderItem> orderItems = new ArrayList<OrderItem>();
 
     public Transaction(Context context, long transactionId, String routeCode, String placeCode, BigDecimal creditAmount) {
         this.context = context;
         this.transactionId = transactionId;
         this.routeCode = routeCode;
         this.placeCode = placeCode;
         this.creditAmount = creditAmount;
     }
 
     public void clearOrderItems() {
         this.orderItems.clear();
     }
 
     public void addOrderItem(OrderItem orderItem) {
         if (! this.orderItems.contains(orderItem)) {
             this.orderItems.add(orderItem);
         }
     }
 
     public BigDecimal getTotalAmount() {
         BigDecimal totalAmount = new BigDecimal(0);
         for(OrderItem orderItem: orderItems) {
             totalAmount = totalAmount.add(orderItem.getAmount());
         }
         return totalAmount;
     }
 
     public String getFormattedTotalAmount() {
         return getTotalAmount().toString() + " " + context.getString(R.string.currency);
     }
 
     public BigDecimal getCashAmount() {
         return getTotalAmount().subtract(this.creditAmount);
     }
 
     public String getFormattedCashAmount() {
         return getCashAmount().toString() + " " + context.getString(R.string.currency);
     }
 
     public BigDecimal getCreditAmount() {
         return this.creditAmount;
     }
 
     public String getFormattedCreditAmount() {
         return this.creditAmount.toString() + " " + context.getString(R.string.currency);
         
     }
 }
