 package com.spilgames.qa.tester;
 
 import com.sun.jersey.api.view.Viewable;
 import org.javatuples.Pair;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Path("/")
 public class Payment {
 
   private static final List<Pair<Integer, Double>> amountBorders = new ArrayList<Pair<Integer, Double>>(){
     private static final long serialVersionUID = -7430551132715613867L;
 
   {
     add(new Pair<Integer, Double>(5, 0.5));
     add(new Pair<Integer, Double>(5, 0.4));
     add(new Pair<Integer, Double>(10, 0.3));
     add(new Pair<Integer, Double>(20, 0.2));
     add(new Pair<Integer, Double>(20, 0.15));
    add(new Pair<Integer, Double>(999, 0.1));
   }};
 
   @Path("/buyCoins")
   @GET
   @Produces("text/plain")
   public Response command(
       @QueryParam("method") PaymentMethod method,
       @QueryParam("amount") int amount) {
 
     Map<String, Object> data = new HashMap<String, Object>();
     try {
       data.put("amount", calculatePayment(method, calculateAmount(amount)));
     } catch (InvalidInputException e) {
       data.put("error", e.getMessage());
     }
     return Response.ok(new Viewable("/payment", data)).build();
 
   }
 
   @Path("/coins")
   @GET
   @Produces("text/plain")
   public Response command2(@QueryParam("amount") int amount) {
 
     Map<String, Object> data = new HashMap<String, Object>();
     data.put("amount", calculateAmount(amount));
     return Response.ok(new Viewable("/payment", data)).build();
 
   }
 
   private double calculateAmount(int amount) {
     double ret = 0;
     int remainingAmount = amount;
     for (Pair<Integer, Double> border : amountBorders) {
       int inc = getPart(remainingAmount, border.getValue0());
       ret += inc * border.getValue1();
       remainingAmount -= inc;
     }
     return ret;
   }
 
   private int getPart(int amount, int border) {
     return (amount < border) ? amount : border;
   }
 
   private double calculatePayment(PaymentMethod method, double amount) throws InvalidInputException {
     switch (method) {
       case CREDITCARD:
         if (amount < 100) {
           return amount * 1.02;
         }
         break;
       case IDEAL:
         if (amount < 50) {
           return (double) (amount + 1);
         }
         break;
       case PAYPAL:
         if (amount < 20) {
           throw new InvalidInputException("Your amount is too low");
         }
         break;
     }
     return (double) amount;
   }
 
 }
