 package com.paragon;
 
 import com.paragon.orders.Order;
 import com.paragon.stock.Offer;
 import com.paragon.stock.Quote;
 import com.paragon.stock.Warehouse;
 
 import java.math.BigDecimal;
 import java.util.*;
 
 public class OrderSystem implements OrderService {
 
    public static final long RELATIVE_ONE_MINUTE = 1 * 60 * 10;
 
     public static final BigDecimal CASE_SIZE = new BigDecimal(12);
 
     private Map<UUID, Quote> quotes = new HashMap<UUID, Quote>();
     private FulfillmentService fulfillmentService;
 
     @Override
     public void add(FulfillmentService fulfillmentService) {
         this.fulfillmentService = fulfillmentService;
     }
 
     @Override
     public List<Offer> searchForProduct(String query) {
 
         List<Offer> searchResults = Warehouse.getInstance().searchFor(query);
         for (Offer offer : searchResults) {
             quotes.put(offer.id, new Quote(offer, System.currentTimeMillis()));
         }
         return searchResults;
     }
 
     @Override
     public Order confirmOrder(Quote quote, String userAuthToken) {
 
         if (quote != null) {
             return new Order(totalPrice(quote), quote, System.currentTimeMillis(), userAuthToken);
         }
         else {
             return null;
         }
      }
 
     @Override
     public String updateOrderLedger(Order order) {
 
         if (order != null) {
             this.fulfillmentService.placeOrder(order);
             return order.summary();
         }
         else {
             return "Sorry, the price is no longer valid";
         }
 
     }
 
     @Override
     public BigDecimal totalPrice(Quote quote) {
 
         BigDecimal casePrice = quote.offer.price.multiply(CASE_SIZE);
 
         if (timeOutOccurred(quote.timestamp, RELATIVE_ONE_MINUTE * 2)) {
             if (timeOutOccurred(quote.timestamp, RELATIVE_ONE_MINUTE * 10)) {
                 return casePrice.add(new BigDecimal(20));
             }
             else {
                 return casePrice.add(casePrice.divide(new BigDecimal(20)).min(new BigDecimal(10)));
             }
         }
         else {
             return casePrice;
         }
     }
 
     @Override
     public Quote validQuote(UUID uuid)  {
 
         Quote quote = quotes.get(uuid);
 
         if (quote == null || timeOutOccurred(quote.timestamp, RELATIVE_ONE_MINUTE * 20)) {
              return null;
         }
         else {
             return quote;
         }
     }
 
     public static boolean timeOutOccurred(long pastTime, long maxTimeDifference) {
         return maxTimeDifference < System.currentTimeMillis() - pastTime;
     }
 }
