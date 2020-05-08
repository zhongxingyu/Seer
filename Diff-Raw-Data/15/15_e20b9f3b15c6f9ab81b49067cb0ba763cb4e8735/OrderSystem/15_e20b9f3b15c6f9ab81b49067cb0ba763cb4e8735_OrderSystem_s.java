package com.paragon;
 
 import com.paragon.orders.Order;
 import com.paragon.orders.OrderLedger;
 import com.paragon.stock.Offer;
 import com.paragon.stock.Quote;
 import com.paragon.stock.Warehouse;
 
 import java.math.BigDecimal;
 import java.util.*;
 
 public class OrderSystem implements OrderService {
 
     private static final long MAX_QUOTE_AGE_MILLIS = 20 * 60 * 1000;
 
     public static final BigDecimal STANDARD_PROCESSING_CHARGE = new BigDecimal(5);
     public static final BigDecimal CASE_SIZE = new BigDecimal(12);
 
     private Map<UUID, Quote> quotes = new HashMap<UUID, Quote>();
 
     @Override
     public List<Offer> searchForProduct(String query) {
 
         List<Offer> searchResults = Warehouse.getInstance().searchFor(query);
         for (Offer offer : searchResults) {
             quotes.put(offer.id, new Quote(offer, System.currentTimeMillis()));
         }
         return searchResults;
     }
 
     @Override
     public void confirmOrder(UUID id, String userAuthToken) {
 
         if (!quotes.containsKey(id)) {
             throw new NoSuchElementException("Offer ID is invalid");
         }
 
         Quote quote = quotes.get(id);
 
         long timeNow = System.currentTimeMillis();
 
         if (timeNow - quote.timestamp > MAX_QUOTE_AGE_MILLIS) {
             throw new IllegalStateException("Quote expired, please get a new price");
         }
 
         Order completeOrder = new Order(quote.offer.price.multiply(CASE_SIZE).add(STANDARD_PROCESSING_CHARGE), quote, timeNow, userAuthToken);
 
         OrderLedger.getInstance().placeOrder(completeOrder);
     }
 
 }
