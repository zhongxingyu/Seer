 package net.m_kawato.tabletpos;
 
 import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

 import android.content.Context;
 
 public class OrderItem {
     private Context context;
     public Product product;
     public int quantity;
     
     public OrderItem(Context context, Product product, int quantity) {
         this.context = context;
         this.product = product;
         this.quantity = quantity;
     }
 
     public BigDecimal getAmount() {
         int numBoxes = this.quantity / this.product.numPiecesInBox;
         int numPieces = this.quantity - numBoxes * this.product.numPiecesInBox;
         BigDecimal amountPieces = this.product.unitPrice.multiply(new BigDecimal(numPieces));
         BigDecimal amountBoxes = this.product.unitPriceBox.multiply(new BigDecimal(numBoxes * this.product.numPiecesInBox));
         return amountPieces.add(amountBoxes);        
     }
 
     public String getFormattedAmount() {
         return this.getAmount().toString() + " " + context.getString(R.string.currency);
     }
 
     public String toString() {
         return String.format("OrderItem:product=%s,quantity=%d", this.product, this.quantity);
     }
 
 }
