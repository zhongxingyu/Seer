package com.paragon;
 
import com.paragon.OrderSystem;
 import java.math.BigDecimal;
 import java.util.*;
 import com.paragon.stock.Offer;
 
 /**
  * Created with IntelliJ IDEA.
  * User: fgheorghe
  * Date: 17/08/13
  * Time: 22:44
  * To change this template use File | Settings | File Templates.
  */
 public class ExampleClient {
     public static void main(String[] args) throws Exception{
 
         OrderSystem orderSystem = new OrderSystem();
         String userAuthToken = "fgheorghe@grosan.co.uk";
 
         List<Offer> searchResults = orderSystem.searchForProduct( "Cabernet Sauvignon" );
 
         if ( searchResults.isEmpty() ) {
             System.out.println( "No search results found." );
         } else {
             Offer offer = searchResults.get( 0 );
 
             // some time may pass...
             Thread.sleep( 5 * 1000 );
 
             if ( priceAcceptable( offer.price ) ) {
                 orderSystem.confirmOrder( offer.id, userAuthToken );
             }
         }
     }
 
     private static boolean priceAcceptable( BigDecimal price ) {
         return true;
     }
 }
